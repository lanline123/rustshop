package com.tigshop.bean.vo.wholesale;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批发商品批量添加结果 VO。
 *
 * <p>契约见 api-contract §4.3：{@code successCount} 表示新建配置数，
 * {@code skippedIds} 表示因"已配置批发能力"而跳过的 productId（幂等不报错，CD-023）。
 *
 * <p>归属校验（CR-20260610-162600 / CD-039 多租户，取代原 CR-20260603-141000 平台边界）：
 * 非当前操作者名下商品不阻断整批，逐项进入 {@code failedItems}，{@code errorCode=5314}。
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Data
@Schema(description = "批发商品批量添加结果")
public class WholesaleProductBatchAddVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "成功转换为批发商品的数量")
    private Integer successCount;

    @Schema(description = "跳过的 productId 列表（已是批发商品的进入此列表，不视为失败）")
    private List<Integer> skippedIds;

    @Schema(description = "失败项列表（非本店铺商品等，逐项报告，不阻断整批）")
    private List<FailedItem> failedItems;

    /**
     * 批量添加失败项（商品归属校验等业务校验失败时逐项记录）。
     */
    @Data
    @Schema(description = "批量添加失败项")
    public static class FailedItem implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "失败的商品 ID")
        private Integer productId;

        @Schema(description = "业务错误码（如 5314 非本店铺商品）")
        private Integer errorCode;

        @Schema(description = "失败原因文案")
        private String errorMessage;

        public FailedItem() {
        }

        public FailedItem(Integer productId, Integer errorCode, String errorMessage) {
            this.productId = productId;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }
    }
}
