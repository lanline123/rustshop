package com.tigshop.bean.param.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 进件申请提交参数
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "进件申请提交参数")
public class MerchantApplicationSubmitParam {

    @Schema(description = "店铺ID（兼容字段，店铺/供应商端由登录态推导，可不传）")
    private Long shopId;

    @Schema(description = "支付渠道：WECHAT / HUIFU，默认 WECHAT")
    private String payChannel;

    @NotBlank(message = "请选择主体类型")
    @Schema(description = "主体类型 2-企业 4-个体工商户 2401-个人（微信小微商户）")
    private String organizationType;

    @Schema(description = "企业微信进件：法人是否为唯一最终受益人；本轮店铺/门店仅支持明确确认true")
    private Boolean owner;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "商户简称")
    private String merchantShortName;

    @NotBlank(message = "请填写店铺链接")
    @Schema(description = "店铺主页链接，须为HTTP或HTTPS地址")
    private String storeUrl;

    @Schema(description = "营业执照号")
    private String businessLicenseNumber;

    @Schema(description = "法人姓名")
    private String legalPerson;

    @Schema(description = "营业执照有效期开始")
    private String licensePeriodBegin;

    @Schema(description = "营业执照有效期结束")
    private String licensePeriodEnd;

    @Schema(description = "身份证姓名")
    private String idCardName;

    @Schema(description = "身份证号")
    private String idCardNumber;

    @Schema(description = "身份证有效期开始")
    private String cardPeriodBegin;

    @Schema(description = "身份证有效期结束")
    private String cardPeriodEnd;

    @Schema(description = "客服电话")
    private String servicePhone;

    @Schema(description = "结算规则ID")
    private String settlementId;

    @Schema(description = "特殊资质类型")
    private String qualificationType;

    @Schema(description = "银行账户类型 74-对公 75-对私")
    private String bankAccountType;

    @Schema(description = "开户名")
    private String bankAccountName;

    @Schema(description = "银行账号")
    private String bankAccountNumber;

    @Schema(description = "开户银行")
    private String bankName;

    @Schema(description = "开户支行")
    private String bankBranchName;

    @Schema(description = "营业执照照片MediaId")
    private String licenseCopy;

    @Schema(description = "身份证人像面MediaId")
    private String idCardCopy;

    @Schema(description = "身份证国徽面MediaId")
    private String idCardNational;

    @Schema(description = "超管类型 65-法人 66-经办人")
    private String contactType;

    @Schema(description = "超管姓名")
    private String contactName;

    @Schema(description = "超管身份证号")
    private String contactIdNumber;

    @Schema(description = "超管手机号")
    private String contactPhone;

    @Schema(description = "超管邮箱")
    private String contactEmail;
}
