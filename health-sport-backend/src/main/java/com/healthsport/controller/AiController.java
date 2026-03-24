package com.healthsport.controller;

import com.healthsport.dto.AiAdviceRequestDTO;
import com.healthsport.service.AiService;
import com.healthsport.utils.Result;
import com.healthsport.utils.SecurityUtils;
import com.healthsport.vo.AiAdviceVO;
import com.healthsport.utils.RateLimiter;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 健康建议接口
 */
@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiService aiService;
    private final RateLimiter rateLimiter;

    public AiController(AiService aiService, RateLimiter rateLimiter) {
        this.aiService = aiService;
        this.rateLimiter = rateLimiter;
    }

    /**
     * 获取 AI 个性化健康建议
     */
    @PostMapping("/advice")
    public Result<AiAdviceVO> getAdvice(@Valid @RequestBody AiAdviceRequestDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        AiAdviceVO advice = aiService.getAdvice(userId, dto);
        return Result.success(advice);
    }

    /**
     * 查询今日 AI 建议剩余次数
     */
    @GetMapping("/remain")
    public Result<Map<String, Integer>> getRemaining() {
        Long userId = SecurityUtils.getCurrentUserId();
        int remaining = rateLimiter.getRemaining(userId);
        if (remaining == Integer.MAX_VALUE) {
            return Result.success(Map.of("remaining", -1));
        }

        return Result.success(Map.of("remaining", remaining));
    }
}
