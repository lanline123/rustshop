// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单日志创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "订单日志参数")
public class OrderLogCreateDTO {

    @Schema(description = "被操作的订单id")
    private Integer orderId;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "操作管理员（后台）")
    private Integer adminId;

    @Schema(description = "操作会员（前台）")
    private Integer userId;

    @Schema(description = "操作备注")
    private String description;

    @Schema(description = "操作时间")
    private Long logTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "订单日志描述")
    private String orderLogDesc;
}
