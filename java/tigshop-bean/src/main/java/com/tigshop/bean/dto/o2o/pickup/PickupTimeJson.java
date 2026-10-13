// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.o2o.pickup;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/8/23 16:13
 */
@Data
@Schema(description = "自提时间")
public class PickupTimeJson {

    @Schema(description = "1=每天的固定时间段")
    private Integer type;

    @Schema(description = "自提时间段")
    private List<TimeStartAndEnd> timeList;

    @Schema(description = "预约自提相关")
    private appointmentTime appointmentTime = new appointmentTime();


    @Data
    @Schema(description = "时间段")
    public static class TimeStartAndEnd {
        @Schema(description = "开始时间")
        private String startTime;
        @Schema(description = "结束时间")
        private String endTime;
    }

    @Data
    @Schema(description = "预约自提时间")
    public static class appointmentTime {
        @Schema(description = "数量")
        private Integer timeNum = 0;
        @Schema(description = "d/h/m")
        private String timeUnit = "d";
    }
}
