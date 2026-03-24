package com.healthsport.service.impl;

import com.healthsport.dto.AiAdviceRequestDTO;
import com.healthsport.exception.BusinessException;
import com.healthsport.service.AiService;
import com.healthsport.utils.RateLimiter;
import com.healthsport.vo.AiAdviceVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 健康建议服务实现
 * 负责：限流检查 → Redis 缓存命中 → 调用 Python FastAPI → 缓存结果
 */
@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    private final RestTemplate restTemplate;
    private final RateLimiter rateLimiter;
    private final String aiServiceBaseUrl;

    public AiServiceImpl(RestTemplate restTemplate,
                         RateLimiter rateLimiter,
                         @Value("${spring.ai.service.base-url}") String aiServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.rateLimiter = rateLimiter;
        this.aiServiceBaseUrl = aiServiceBaseUrl;
    }

    @Override
    @Cacheable(value = "aiAdvice", keyGenerator = "userKeyGenerator")
    public AiAdviceVO getAdvice(Long userId, AiAdviceRequestDTO dto) {
        // ① 限流检查
        rateLimiter.check(userId);

        // ② 构造请求体
        Map<String, Object> body = buildRequestBody(userId, dto);

        // ③ 调用 Python FastAPI
        AiAdviceVO advice = callAiService(body);

        // ④ 返回剩余次数
        advice.setRemainingDaily(rateLimiter.getRemaining(userId));

        return advice;
    }

    private Map<String, Object> buildRequestBody(Long userId, AiAdviceRequestDTO dto) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("advice_type", dto.getAdviceType());
        body.put("weight", dto.getWeight());
        body.put("exercise_minutes_today", dto.getExerciseMinutesToday());
        body.put("calories_intake_today", dto.getCaloriesIntakeToday());
        body.put("calories_burned_today", dto.getCaloriesBurnedToday());
        body.put("goal", dto.getGoal());
        body.put("extra_question", dto.getExtraQuestion());
        body.put("record_date",
                dto.getRecordDate() != null ? dto.getRecordDate().toString() : LocalDate.now().toString());
        return body;
    }

    private AiAdviceVO callAiService(Map<String, Object> body) {
        String url = aiServiceBaseUrl + "/agent/advice";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = restTemplate.postForObject(url, entity, Map.class);
            if (responseBody == null) {
                throw new BusinessException(502, "AI 服务返回空响应");
            }

            String content = (String) responseBody.get("content");
            String summary = responseBody.get("summary") != null
                    ? (String) responseBody.get("summary")
                    : (content != null && content.length() > 80
                    ? content.substring(0, 80) + "…" : content);

            return AiAdviceVO.builder()
                    .adviceType((String) responseBody.getOrDefault("advice_type", "GENERAL"))
                    .content(content)
                    .summary(summary)
                    .generatedAt(LocalDateTime.now())
                    .build();

        } catch (HttpStatusCodeException e) {
            String responseText = e.getResponseBodyAsString();
            log.error("调用 AI 服务失败: status={}, body={}", e.getStatusCode(), responseText, e);

            String brief = responseText;
            if (brief != null && brief.length() > 180) {
                brief = brief.substring(0, 180) + "...";
            }

            throw new BusinessException(
                    502,
                    "AI 服务返回异常状态：" + e.getStatusCode().value() +
                            (brief != null && !brief.isBlank() ? "，详情：" + brief : "")
            );
        } catch (Exception e) {
            String msg = e.getMessage();
            log.error("调用 AI 服务失败: {}", msg, e);
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }

            if (msg != null && msg.contains("Read timed out")) {
                throw new BusinessException(504, "AI 服务响应超时（>120s），请检查模型是否可用或网络是否通畅");
            }
            if (msg != null && msg.contains("Connection refused")) {
                throw new BusinessException(502, "无法连接 AI 服务，请确认 ai-service（Python FastAPI）已启动并监听 8000 端口");
            }

            throw new BusinessException(
                    502,
                    "AI 服务暂时不可用：" + (msg != null ? msg.substring(0, Math.min(msg.length(), 180)) : "未知错误")
            );
        }
    }
}
