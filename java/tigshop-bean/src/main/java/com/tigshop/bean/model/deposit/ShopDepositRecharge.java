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
 * 店铺保证金充值表model
 *
 * @author Tigshop团队
 */
@Data
@TableName("shop_deposit_recharge")
@Schema(description = "店铺保证金充值")
public class ShopDepositRecharge {
    @TableId(value = "shop_deposit_recharge_id", type = IdType.AUTO)
    @Schema(description = "店铺保证金充值ID")
    private Integer shopDepositRechargeId;

    @Schema(description = "持有者类型 1店铺 2供应商")
    private Integer ownerType;

    @Schema(description = "持有者id(店铺为shopId,供应商为vendorId)")
    private Integer ownerId;

    @Schema(description = "充值金额")
    private BigDecimal amount;

    @Schema(description = "支付凭证")
    private String payVoucher;

    @Schema(description = "充值到的收款账户id(shop_deposit_payee_account)")
    private Integer payeeAccountId;

    @Schema(description = "收款账户类型快照 1银行卡 2支付宝 3微信")
    private Integer payeeAccountType;

    @Schema(description = "收款账户快照JSON(类型/户名/账号/银行)")
    private String payeeSnapshot;

    @Schema(description = "申请备注")
    private String remark;

    @Schema(description = "状态")
    private Integer status;

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
