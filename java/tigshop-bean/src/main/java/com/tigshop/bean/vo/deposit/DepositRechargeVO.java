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
 * 店铺保证金充值单VO。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "店铺保证金充值单VO")
public class DepositRechargeVO {

    @Schema(description = "充值单id")
    private Integer shopDepositRechargeId;

    @Schema(description = "持有者类型 1店铺 2供应商")
    private Integer ownerType;

    @Schema(description = "持有者类型文本")
    private String ownerTypeText;

    @Schema(description = "主体类型（商户性质）1个人 2企业")
    private Integer subjectType;

    @Schema(description = "主体类型文案（个人/企业）")
    private String subjectTypeText;

    @Schema(description = "持有者id（店铺为shopId，供应商为vendorId）")
    private Integer ownerId;

    @Schema(description = "持有者名称（店铺名/供应商名）")
    private String ownerName;

    @Schema(description = "店铺id（兼容字段，值同 ownerId）")
    private Integer shopId;

    @Schema(description = "店铺名称（兼容字段，仅店铺维度回填）")
    private String shopName;

    @Schema(description = "充值金额")
    private BigDecimal amount;

    @Schema(description = "支付凭证")
    private String payVoucher;

    @Schema(description = "申请备注")
    private String remark;

    @Schema(description = "充值到的收款账户id")
    private Integer payeeAccountId;

    @Schema(description = "收款账户类型 1银行卡 2支付宝 3微信")
    private Integer payeeAccountType;

    @Schema(description = "收款账户类型文本")
    private String payeeAccountTypeText;

    @Schema(description = "收款账户户名")
    private String payeeAccountName;

    @Schema(description = "收款账户账号")
    private String payeeAccountNo;

    @Schema(description = "收款账户银行名称")
    private String payeeBankName;

    @Schema(description = "收款账户银行支行")
    private String payeeBankBranch;

    @Schema(description = "状态 0待审核 1已通过 2未通过")
    private Integer status;

    @Schema(description = "状态文本 待审核/已通过/未通过")
    private String statusText;

    @Schema(description = "审核管理员id")
    private Integer auditAdminId;

    @Schema(description = "审核时间(秒级时间戳)")
    private Long auditTime;

    @Schema(description = "审核时间文本")
    private String auditTimeText;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "申请时间(秒级时间戳)")
    private Long addTime;

    @Schema(description = "申请时间文本")
    private String addTimeText;
}
