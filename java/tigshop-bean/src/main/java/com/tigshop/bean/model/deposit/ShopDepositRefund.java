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
 * 店铺保证金退款表model
 *
 * @author Tigshop团队
 */
@Data
@TableName("shop_deposit_refund")
@Schema(description = "店铺保证金退款")
public class ShopDepositRefund {
    @TableId(value = "shop_deposit_refund_id", type = IdType.AUTO)
    @Schema(description = "店铺保证金退款ID")
    private Integer shopDepositRefundId;

    @Schema(description = "持有者类型 1店铺 2供应商")
    private Integer ownerType;

    @Schema(description = "持有者id(店铺为shopId,供应商为vendorId)")
    private Integer ownerId;

    @Schema(description = "退款类型")
    private Integer type;

    @Schema(description = "退款金额")
    private BigDecimal amount;

    @Schema(description = "收款信息")
    private String payeeInfo;

    @Schema(description = "退款原因")
    private String reason;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "支付凭证")
    private String payVoucher;

    @Schema(description = "审核管理员ID")
    private Integer auditAdminId;

    @Schema(description = "审核时间")
    private Long auditTime;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "创建时间")
    private Long addTime;

    @Schema(description = "更新时间")
    private Long updateTime;
}
