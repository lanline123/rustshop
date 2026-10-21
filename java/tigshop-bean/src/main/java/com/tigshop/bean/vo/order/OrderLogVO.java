// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 订单日VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "订单日志参数")
public class OrderLogVO {
    @Schema(description = "日志ID")
    private Integer logId;

    /**
     * 被操作的订单id
     */
    @Schema(description = "被操作的订单ID")
    private Integer orderId;

    /**
     * 订单号
     */
    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "操作者")
    private String operator;

    /**
     * 操作管理员（后台）
     */
    @Schema(description = "操作管理员（后台）")
    private Integer adminId;

    /**
     * 操作会员（前台）
     */
    @Schema(description = "操作会员（前台）")
    private Integer userId;

    /**
     * 操作备注
     */
    @Schema(description = "操作备注")
    private String description;

    /**
     * 操作时间
     */
    @Schema(description = "操作时间")
    private String logTime;

    /**
     * 店铺id
     */
    @Schema(description = "店铺ID")
    private Integer shopId;
}