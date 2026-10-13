package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 库存预警规则批量保存参数
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "库存预警规则批量保存参数")
public class StockWarningBatchSaveDTO {

    @Schema(description = "归属主体：0=平台自营，>0=店铺或门店")
    private Integer shopId;

    @Schema(description = "待设置的商品/SKU 列表")
    @NotEmpty(message = "请至少选择一个商品")
    private List<Target> targets;

    @Schema(description = "预警开关：0=关闭，1=开启")
    @NotNull(message = "预警开关不能为空")
    private Integer warnEnabled;

    @Schema(description = "库存下限")
    @NotNull(message = "库存下限不能为空")
    @Min(value = 0, message = "库存下限不能小于0")
    private Integer lowerLimit;

    @Schema(description = "库存上限")
    @NotNull(message = "库存上限不能为空")
    @Min(value = 0, message = "库存上限不能小于0")
    private Integer upperLimit;

    @Data
    @Schema(description = "商品/SKU 标识")
    public static class Target {

        @Schema(description = "商品ID")
        private Integer productId;

        @Schema(description = "SKU ID；0 或不传表示单规格商品")
        private Integer skuId;
    }
}
