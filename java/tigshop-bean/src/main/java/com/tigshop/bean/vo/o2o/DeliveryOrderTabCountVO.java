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

/**
 * 我的派送订单各 tab 数量
 *
 * @author Tigshop团队
 * @create 2026-05-14
 */
@Data
@Schema(description = "我的派送订单各 tab 数量")
public class DeliveryOrderTabCountVO {

    @Schema(description = "全部订单数量（含待派发/待取货/进行中/已完成/已取消）")
    private Long all = 0L;

    @Schema(description = "待取货/待服务数量（status=20）")
    private Long pending = 0L;

    @Schema(description = "配送中/服务中数量（status=30）")
    private Long inProgress = 0L;

    @Schema(description = "已完成数量（status=40）")
    private Long completed = 0L;
}
