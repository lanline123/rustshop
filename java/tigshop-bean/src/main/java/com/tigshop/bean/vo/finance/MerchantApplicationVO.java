package com.tigshop.bean.vo.finance;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 进件申请视图对象
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "进件申请视图对象")
public class MerchantApplicationVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "申请主体类型:SHOP店铺/门店 VENDOR供应商")
    private String applicantType;

    @Schema(description = "申请主体ID")
    private Long applicantId;

    @Schema(description = "申请主体名称(店铺名/供应商名)")
    private String applicantName;

    @Schema(description = "支付渠道")
    private String payChannel;

    @Schema(description = "业务申请编号")
    private String businessCode;

    @Schema(description = "微信申请单号")
    private String applymentId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "原因标题：已驳回状态为『驳回原因』，其它状态（如草稿因渠道异常退回）为『失败原因』，无原因时为空")
    private String reasonLabel;

    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    @Schema(description = "子商户号")
    private String subMchId;

    @Schema(description = "签约链接")
    private String signUrl;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "商户简称")
    private String merchantShortName;

    @Schema(description = "店铺主页链接")
    private String storeUrl;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "主体类型")
    private String organizationType;

    @Schema(description = "营业执照号")
    private String businessLicenseNumber;

    @Schema(description = "法人姓名")
    private String legalPerson;

    @Schema(description = "银行账户类型")
    private String bankAccountType;

    @Schema(description = "结算账户名称")
    private String bankAccountName;

    @Schema(description = "结算账号（可脱敏）")
    private String bankAccountNumber;

    @Schema(description = "开户银行")
    private String bankName;

    @Schema(description = "开户支行")
    private String bankBranchName;

    @Schema(description = "提交请求快照(脱敏)")
    private String requestSnapshot;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifyTime;
}
