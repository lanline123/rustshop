package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 库存预警通知总开关
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "库存预警通知总开关")
public class StockWarningSettingVO {

    @Schema(description = "归属主体ID：0=平台自营")
    private Integer shopId;

    @Schema(description = "库存预警通知总开关：0=关闭，1=开启")
    private Integer notifyEnabled;

    @Schema(description = "未配置商品/SKU 使用的默认库存下限")
    private Integer defaultLowerLimit;
}
