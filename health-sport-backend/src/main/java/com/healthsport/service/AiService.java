package com.healthsport.service;

import com.healthsport.dto.AiAdviceRequestDTO;
import com.healthsport.vo.AiAdviceVO;

/**
 * AI 健康建议服务接口
 */
public interface AiService {

    /**
     * 获取 AI 个性化健康建议
     *
     * @param userId 当前登录用户 ID
     * @param dto    请求参数
     * @return AI 建议结果
     */
    AiAdviceVO getAdvice(Long userId, AiAdviceRequestDTO dto);
}
