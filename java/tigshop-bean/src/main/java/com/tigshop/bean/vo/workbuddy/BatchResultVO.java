package com.tigshop.bean.vo.workbuddy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量操作的执行结果。
 */
@Data
@Schema(description = "WorkBuddy批量操作结果")
public class BatchResultVO {

    /** 操作的中文名，如"批量下架"。 */
    private String operation;

    private Integer successCount;

    /** 实际被操作的商品，供用户核对。 */
    private List<ProductSummaryVO> affected = new ArrayList<>();

    /** 执行后的连带后果。 */
    private List<String> consequences = new ArrayList<>();

    private List<Failure> failed = new ArrayList<>();

    @Data
    public static class Failure {
        private Integer productId;
        private String productName;
        private String message;
    }
}
