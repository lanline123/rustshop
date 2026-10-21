package com.tigshop.bean.vo.workbuddy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "WorkBuddy商品导入结果")
public class ProductImportResultVO {

    @Schema(description = "本次导入的幂等标识")
    private String requestId;

    @Schema(description = "本次导入依据的预览标识")
    private String previewId;

    @Schema(description = "提交的商品总数")
    private int total;

    @Schema(description = "创建成功数")
    private int successCount;

    @Schema(description = "创建失败数")
    private int failureCount;

    @Schema(description = "逐商品结果，顺序与预览一致")
    private List<Item> items = new ArrayList<>();

    @Data
    @Schema(description = "单个商品的导入结果")
    public static class Item {

        @Schema(description = "在提交列表中的序号，从1开始")
        private int index;

        @Schema(description = "是否创建成功")
        private boolean success;

        @Schema(description = "创建成功的商品ID；失败时为空")
        private Integer productId;

        @Schema(description = "商品名称")
        private String productName;

        @Schema(description = "商品编码")
        private String productSn;

        @Schema(description = "商品落库状态：0下架，1上架；失败时为空")
        private Integer productStatus;

        @Schema(description = "失败时的机器可读错误码")
        private String errorCode;

        @Schema(description = "失败原因描述")
        private String message;

        @Schema(description = "面向使用者的后续处理建议")
        private String suggestion;

        @Schema(description = "该失败是否可重试")
        private boolean retryable;

        /** 创建后回查状态，与创建成功状态分开表达。 */
        private Verification verification;
    }

    @Data
    @Schema(description = "商品创建后的写后核对结果")
    public static class Verification {

        @Schema(description = "verified、mismatch 或 failed")
        private String status;

        @Schema(description = "已核对的字段")
        private List<String> checkedFields = new ArrayList<>();

        @Schema(description = "不一致字段")
        private List<String> mismatches = new ArrayList<>();

        @Schema(description = "核对说明")
        private String message;
    }
}
