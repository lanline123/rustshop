// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.deposit;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 店铺保证金变动日志表model
 *
 * @author Tigshop团队
 */
@Data
@TableName("shop_deposit_log")
@Schema(description = "店铺保证金变动日志")
public class ShopDepositLog {
    @TableId(value = "shop_deposit_log_id", type = IdType.AUTO)
    @Schema(description = "店铺保证金变动日志ID")
    private Integer shopDepositLogId;

    @Schema(description = "持有者类型 1店铺 2供应商")
    private Integer ownerType;

    @Schema(description = "持有者id(店铺为shopId,供应商为vendorId)")
    private Integer ownerId;

    @Schema(description = "变动类型")
    private Integer type;

    @Schema(description = "变动金额")
    private BigDecimal changeAmount;

    @Schema(description = "变动前金额")
    private BigDecimal beforePaid;

    @Schema(description = "变动后金额")
    private BigDecimal afterPaid;

    @Schema(description = "关联ID")
    private Integer relatedId;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "操作管理员ID")
    private Integer adminId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Long addTime;
}
