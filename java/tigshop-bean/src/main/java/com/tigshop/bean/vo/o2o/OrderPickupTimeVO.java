// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.o2o;


import com.tigshop.bean.dto.o2o.pickup.PickupTimeJson;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/8/23 16:02
 */
@Data
@Schema(description = "自提设置项VO")
@Builder
public class OrderPickupTimeVO {

    @Schema(description = "自提时间状态：0-禁用，1-启用")
    private Integer pickupTimeStatus;

    @Schema(description = "自提时间段")
    List<PickupTimeJson.TimeStartAndEnd> timeList;

    @Schema(description = "门店id")
    private Integer shopId;

    @Schema(description = "门店id")
    PickupTimeJson.appointmentTime appointmentTime;
}
