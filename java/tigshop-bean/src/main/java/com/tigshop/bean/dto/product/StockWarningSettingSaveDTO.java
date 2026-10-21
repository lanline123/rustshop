package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 库存预警通知总开关保存参数
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "库存预警通知总开关保存参数")
public class StockWarningSettingSaveDTO {

    @Schema(description = "归属主体：0=平台自营，>0=店铺或门店；店铺/门店固定为自身")
    private Integer shopId;

    @Schema(description = "库存预警通知总开关：0=关闭，1=开启")
    @NotNull(message = "库存预警通知开关不能为空")
    private Integer notifyEnabled;
}
