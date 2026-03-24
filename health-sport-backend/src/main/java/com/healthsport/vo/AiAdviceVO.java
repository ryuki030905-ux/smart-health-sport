package com.healthsport.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 健康建议返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAdviceVO {

    /** 建议类型：DIET / EXERCISE / GENERAL */
    private String adviceType;

    /** AI 返回的完整建议文本 */
    private String content;

    /** 建议摘要（一句话） */
    private String summary;

    /** 今日剩余可用次数 */
    private Integer remainingDaily;

    /** 生成时间 */
    private LocalDateTime generatedAt;
}
