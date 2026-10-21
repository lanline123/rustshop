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
 * 店铺保证金退还/提现单VO。
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "店铺保证金退还/提现单VO")
public class DepositRefundVO {

    @Schema(description = "退还单id")
    private Integer shopDepositRefundId;

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

    @Schema(description = "退还类型 1部分退还 2全部退还")
    private Integer type;

    @Schema(description = "退还类型文本")
    private String typeText;

    @Schema(description = "退还金额")
    private BigDecimal amount;

    @Schema(description = "收款信息")
    private String payeeInfo;

    @Schema(description = "退还原因")
    private String reason;

    @Schema(description = "状态 0待审核 1已通过 2未通过")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "打款凭证")
    private String payVoucher;

    @Schema(description = "审核管理员id")
    private Integer auditAdminId;

    @Schema(description = "审核/打款时间(秒级时间戳)")
    private Long auditTime;

    @Schema(description = "审核/打款时间文本")
    private String auditTimeText;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "申请时间(秒级时间戳)")
    private Long addTime;

    @Schema(description = "申请时间文本")
    private String addTimeText;
}
