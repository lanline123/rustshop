package com.tigshop.bean.vo.workbuddy;

import com.tigshop.bean.dto.workbuddy.ProductDraftDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class ProductValidationVO {
    private String previewId;
    private int total;
    private boolean valid;
    /** 所有商品的阻塞字段并集，方便 AI 一轮只询问当前缺项。 */
    private List<String> missingFields = new ArrayList<>();
    private List<Item> items = new ArrayList<>();

    @Data
    public static class Item {
        private int index;
        private String productName, productSn;
        private boolean valid;
        private List<FieldError> errors = new ArrayList<>();
        /** 非阻塞提醒，例如 AI 建议值需要用户确认。 */
        private List<String> warnings = new ArrayList<>();
        /** 字段来源，避免把 AI 建议误说成用户真实输入。 */
        private Map<String, String> source = new LinkedHashMap<>();
        /** 服务端收到并归一化后的草稿。 */
        private ProductDraftDTO product;
        private List<String> missingFields = new ArrayList<>();
    }

    @Data
    public static class FieldError {
        private String field, code, message;
    }
}
