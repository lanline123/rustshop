package com.tigshop.bean.vo.workbuddy;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 商品分析统一响应，携带统计口径和可解释推荐。 */
@Data
public class ProductAnalysisVO {
    private Map<String, Object> metrics;
    private List<Map<String, Object>> rankings = new ArrayList<>();
    private List<Map<String, Object>> recommendations = new ArrayList<>();
    private String dataScope;
    private Integer sampleSize;
    private LocalDateTime generatedAt;
    /** 本次样本按商品创建时间过滤的开始时间。 */
    private LocalDateTime dataStartTime;
    /** 本次样本按商品创建时间过滤的结束时间。 */
    private LocalDateTime dataEndTime;
    private Double confidence;
    private String fallbackMessage;
}
