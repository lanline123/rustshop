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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 配送员/服务员统计数据VO
 *
 * @author Tigshop团队
 * @create 2024-12-16
 */
@Data
@Schema(description = "配送员/服务员统计数据VO")
public class DeliveryOrderStatisticsVO {

    @Schema(description = "0未关联当前用户 1-配送员，2-服务员")
    private Integer type;

    @Schema(description = "待取货/待服务数量")
    private Integer pendingCount;

    @Schema(description = "配送中/服务中数量")
    private Integer inProgressCount;

    @Schema(description = "今日已完成数量")
    private Integer completedTodayCount;

    @Schema(description = "今日已完成金额")
    private BigDecimal completedTodayAmount;

    @Schema(description = "累计已完成数量")
    private Integer completedTotalCount;

    @Schema(description = "累计已完成金额")
    private BigDecimal completedTotalAmount;
}
