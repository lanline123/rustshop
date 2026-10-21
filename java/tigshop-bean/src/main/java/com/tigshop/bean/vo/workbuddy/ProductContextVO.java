package com.tigshop.bean.vo.workbuddy;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ProductContextVO {
    private boolean hasData;
    private Integer categoryId;
    private String categoryName;
    private Integer sampleSize;
    private Metrics metrics;
    private List<Map<String, Object>> patterns = new ArrayList<>();
    private List<ProductSummaryVO> samples = new ArrayList<>();
    private Fallback fallback;
    /** 从历史商品编码推断出的客户习惯；低置信度时必须让 AI 询问用户。 */
    private EncodingInference encoding;
    /** 可供 AI 生成草稿的建议值，值均带 source/confidence，不代表用户已确认。 */
    private Map<String, Object> defaults = new java.util.LinkedHashMap<>();

    @Data
    public static class Metrics {
        private Double salesAvg;
        private Double salesMedian;
        private Double stockAvg;
        private Double stockMedian;
        private BigDecimal priceMin;
        private BigDecimal priceMedian;
        private BigDecimal priceMax;
        private Integer productCount;
    }
    @Data
    public static class Fallback {
        private String code;
        private String message;
        public Fallback() { }
        public Fallback(String code, String message) { this.code = code; this.message = message; }
    }

    @Data
    public static class EncodingInference {
        private String pattern;
        private String prefix;
        private String separator;
        private Integer numberLength;
        private String nextCandidate;
        private Double confidence;
        private boolean needsConfirmation;
        private String reason;
        private List<String> samples = new ArrayList<>();
    }
}
