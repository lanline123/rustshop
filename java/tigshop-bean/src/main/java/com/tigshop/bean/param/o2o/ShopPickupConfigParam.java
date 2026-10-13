// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.param.o2o;


import com.tigshop.bean.dto.o2o.pickup.PickupEndJson;
import com.tigshop.bean.dto.o2o.pickup.PickupTimeJson;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop团队
 * @create 2025/8/23 16:02
 */
@Data
@Schema(description = "自提设置项VO")
public class ShopPickupConfigParam {

    private Integer shopId;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "完成备货状态：0-禁用，1-启用")
    private Integer stockingStatus;

    @Schema(description = "自提时间状态：0-禁用，1-启用")
    private Integer pickupTimeStatus;

    @Schema(description = "自提时间JSON")
    private PickupTimeJson pickupTimeJson;

    @Schema(description = "提货有效期状态：0-禁用，1-启用")
    private Integer pickupEndStatus;

    @Schema(description = "提货有效期JSON")
    private PickupEndJson pickupEndJson;
}
