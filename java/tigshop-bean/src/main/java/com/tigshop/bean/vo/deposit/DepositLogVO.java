// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.deposit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 保证金交易明细/流水行（详情-交易记录 与 全局交易明细 共用）
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "保证金交易明细行")
public class DepositLogVO {

    @Schema(description = "流水id")
    private Integer shopDepositLogId;

    @Schema(description = "持有者类型 DepositOwnerType：1店铺 2供应商")
    private Integer ownerType;

    @Schema(description = "持有者类型文案（店铺/供应商）")
    private String ownerTypeText;

    @Schema(description = "持有者id（店铺为shopId，供应商为vendorId）")
    private Integer ownerId;

    @Schema(description = "持有者名称（店铺名或供应商名，统一展示）")
    private String ownerName;

    @Schema(description = "主体类型（商户性质）1个人 2企业；店铺取商户性质、供应商取供应商性质")
    private Integer subjectType;

    @Schema(description = "主体类型文案（个人/企业）")
    private String subjectTypeText;

    @Schema(description = "店铺id（兼容字段，仅店铺行填充）")
    private Integer shopId;

    @Schema(description = "店铺名称（兼容字段，仅店铺行填充）")
    private String shopTitle;

    @Schema(description = "商户名称（兼容字段，仅店铺行填充）")
    private String merchantName;

    @Schema(description = "交易类型 DepositLogType")
    private Integer type;

    @Schema(description = "交易类型文案")
    private String typeText;

    @Schema(description = "变动金额（带符号）")
    private BigDecimal changeAmount;

    @Schema(description = "变动前实缴")
    private BigDecimal beforePaid;

    @Schema(description = "变动后实缴")
    private BigDecimal afterPaid;

    @Schema(description = "关联单id（充值/退还单等）")
    private Integer relatedId;

    @Schema(description = "关联订单id（平台扣除/赔付时）")
    private Integer orderId;

    @Schema(description = "操作管理员id")
    private Integer adminId;

    @Schema(description = "流水备注（入账时写入的备注）")
    private String remark;

    @Schema(description = "凭证（充值/退还类从关联单回填，扣款/调整类为空）")
    private String payVoucher;

    @Schema(description = "支付方式（收款账户类型 1银行卡 2支付宝 3微信，仅充值类有）")
    private Integer payeeAccountType;

    @Schema(description = "支付方式文案")
    private String payeeAccountTypeText;

    @Schema(description = "关联单备注（充值=申请备注 / 退还=退款原因，仅充值/退还类）")
    private String relatedRemark;

    @Schema(description = "关联单审核备注（仅充值/退还类）")
    private String relatedAuditRemark;

    @Schema(description = "商户认证状态（1正常 2取消资格，仅店铺类有）")
    private Integer merchantStatus;

    @Schema(description = "商户认证状态文案")
    private String merchantStatusText;

    @Schema(description = "时间")
    private String addTime;
}
