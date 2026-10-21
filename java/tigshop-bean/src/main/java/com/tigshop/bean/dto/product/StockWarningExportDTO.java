package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存预警导出参数
 *
 * @author Tigshop团队
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "库存预警导出参数")
public class StockWarningExportDTO extends StockWarningListDTO {

    /**
     * 逗号分隔的行标识，每项格式 productId_skuId，单规格商品 skuId 为 0。
     * 例：1001_0,1002_55
     */
    @NotBlank(message = "请先选择要导出的数据")
    @Schema(description = "勾选行标识，逗号分隔，每项为 productId_skuId", example = "1001_0,1002_55")
    private String ids;
}
