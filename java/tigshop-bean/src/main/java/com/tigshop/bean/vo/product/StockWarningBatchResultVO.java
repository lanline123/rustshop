package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 库存预警批量设置结果
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "库存预警批量设置结果")
public class StockWarningBatchResultVO {

    @Schema(description = "成功数量")
    private Integer successCount = 0;

    @Schema(description = "失败数量")
    private Integer failCount = 0;

    @Schema(description = "失败明细")
    private List<FailItem> failItems = new ArrayList<>();

    public void addSuccess() {
        this.successCount++;
    }

    public void addFail(Integer productId, Integer skuId, String reason) {
        FailItem item = new FailItem();
        item.setProductId(productId);
        item.setSkuId(skuId);
        item.setReason(reason);
        this.failItems.add(item);
        this.failCount++;
    }

    @Data
    @Schema(description = "批量设置失败项")
    public static class FailItem {

        @Schema(description = "商品ID")
        private Integer productId;

        @Schema(description = "SKU ID")
        private Integer skuId;

        @Schema(description = "失败原因")
        private String reason;
    }
}
