package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 库存预警导入结果
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "库存预警导入结果")
public class StockWarningImportResultVO {

    @Schema(description = "读取到的有效数据行数")
    private Integer totalCount = 0;

    @Schema(description = "成功数量")
    private Integer successCount = 0;

    @Schema(description = "失败数量")
    private Integer failCount = 0;

    @Schema(description = "失败明细")
    private List<FailItem> failItems = new ArrayList<>();

    public void addSuccess() {
        this.totalCount++;
        this.successCount++;
    }

    /**
     * @param rowNumber Excel 中的行号，含表头，便于用户直接定位
     * @param skuSn 该行填写的 SKU 编码
     */
    public void addFail(int rowNumber, String skuSn, String reason) {
        addFail(rowNumber, null, skuSn, reason);
    }

    public void addFail(int rowNumber, String productSn, String skuSn, String reason) {
        FailItem item = new FailItem();
        item.setRowNumber(rowNumber);
        item.setProductSn(productSn);
        item.setSkuSn(skuSn);
        item.setReason(reason);
        this.failItems.add(item);
        this.totalCount++;
        this.failCount++;
    }

    @Data
    @Schema(description = "导入失败项")
    public static class FailItem {

        @Schema(description = "Excel 行号（含表头）")
        private Integer rowNumber;

        @Schema(description = "该行填写的商品编码")
        private String productSn;

        @Schema(description = "该行填写的SKU编码")
        private String skuSn;

        @Schema(description = "失败原因")
        private String reason;
    }
}
