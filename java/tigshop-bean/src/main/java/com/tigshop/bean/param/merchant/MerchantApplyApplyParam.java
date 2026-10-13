package com.tigshop.bean.param.merchant;

import com.tigshop.bean.enums.merchant.MerchantApplyStatusEnum;
import com.tigshop.bean.model.merchant.MerchantApply;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 前端入驻申请审核数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "入驻申请审核数据对象")
public class MerchantApplyApplyParam {

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "类型，1：个人，2：企业")
    private Integer type;

    @NotNull(message = "基础数据不能为空")
    @Schema(description = "基础数据json")
    private BaseData baseData;

    @NotNull(message = "商户数据不能为空")
    @Schema(description = "商户数据JSON")
    private MerchantData merchantData;

    @NotNull(message = "店铺数据不能为空")
    @Schema(description = "店铺数据json")
    private ShopData shopData;

    // 新增 BaseData 类
    @Data
    @Schema(description = "基础数据")
    public static class BaseData {
        @Schema(description = "类型")
        private Integer type;

        @Schema(description = "公司名称")
        private String companyName;

        @Schema(description = "营业执照地址省份")
        private List<Integer> licenseAddrProvince;

        @Schema(description = "营业执照地址")
        private String businessLicenseAddress;

        @Schema(description = "经营范围")
        private String businessScope;

        @Schema(description = "营业执照编号")
        private String businessLicenseId;

        @Schema(description = "经营期限类型")
        private Integer operatingTermType;

        @Schema(description = "经营期限类型日期")
        private List<String> operatingTermTypeDate;

        @Schema(description = "经营期限类型结束日期")
        private String operatingTermTypeEnd;

        @Schema(description = "证书有效期类型")
        private Integer certificateValidityPeriod;

        @Schema(description = "证书有效期日期")
        private List<String> certificateValidityPeriodDate;

        @Schema(description = "证书有效期结束日期")
        private String certificateValidityPeriodEnd;

        @Schema(description = "营业执照正面照片")
        private List<PicInfo> frontOfPhoto;

        @Schema(description = "营业执照背面照片")
        private List<PicInfo> backOfPhoto;

        @Schema(description = "证件类型")
        private Integer documentType;

        @Schema(description = "法人名称")
        private String corporateName;

        @Schema(description = "证件号码")
        private String documentNumber;

        @Schema(description = "生日")
        private String birthday;

        @Schema(description = "性别")
        private Integer sex;

        @Schema(description = "居住地址")
        private String residentialAddress;

        @Schema(description = "补充信息")
        private List<PicInfo> supplementaryInformation;

        @Schema(description = "营业执照地址省份名称")
        private String licenseAddrProvinceName;
    }

    @Data
    @Schema(description = "商家数据")
    public static class MerchantData {
        @Schema(description = "联系人姓名")
        private String contactName;

        @Schema(description = "联系人电话")
        private String contactPhone;

        @Schema(description = "联系人邮箱")
        private String contactEmail;

        @Schema(description = "银行存款")
        private String bankDeposit;

        @Schema(description = "银行支行")
        private String bankBranch;

        @Schema(description = "银行卡号")
        private String bankCard;

        @Schema(description = "商家名称")
        private String merchantName;

        @Schema(description = "客服电话")
        private String customerServicePhone;

        @Schema(description = "详细地址")
        private String detailedAddress;

        @Schema(description = "业务地址")
        private List<Integer> businessAddress;

        @Schema(description = "营业执照图片")
        private List<PicInfo> businessLicenseImg;

        @Schema(description = "开户许可证")
        private List<PicInfo> accountOpeningPermit;

        @Schema(description = "业务地址名称")
        private String businessAddressName;
    }

    @Data
    @Schema(description = "店铺数据")
    public static class ShopData {
        @Schema(description = "店铺标题")
        private String shopTitle;

        @Schema(description = "客服电话")
        private String customerServicePhone;

        @Schema(description = "详细地址")
        private String detailedAddress;

        @Schema(description = "联系人手机号")
        private String contactMobile;

        @Schema(description = "描述")
        private String description;

        @Schema(description = "店铺Logo")
        private List<PicInfo> shopLogo;
    }

    // 新增 PicInfo 类
    @Data
    @Schema(description = "图片信息")
    public static class PicInfo {
        @Schema(description = "图片缩略图")
        private String picThumb;

        @Schema(description = "图片URL")
        private String picUrl;

        @Schema(description = "图片名称")
        private String picName;

        @Schema(description = "URL")
        private String url;

        @Schema(description = "UID")
        private Long uid;

        @Schema(description = "状态")
        private String status;
    }

    public MerchantApply createMerchantApply(String needCheck) {
        int status = "1".equals(needCheck) ? MerchantApplyStatusEnum.PENDING_REVIEW.getCode() : MerchantApplyStatusEnum.AUDIT_PASS.getCode();
        Long auditTime = MerchantApplyStatusEnum.AUDIT_PASS.getCode() == status ? StringUtils.getCurrentTime() : null;

        return MerchantApply.builder()
                .type(this.baseData.getType())
                .companyName(this.baseData.getCompanyName())
                .corporateName(this.baseData.getCorporateName())
                .userId(SecurityUtils.getCurrentUserId())
                .status(status)
                .auditTime(auditTime)
                .shopTitle(this.shopTitle)
                .addTime(StringUtils.getCurrentTime())
                .baseData(JsonUtil.toJson(this.baseData))
                .merchantData(JsonUtil.toJson(this.merchantData))
                .shopData(JsonUtil.toJson(this.shopData))
                .build();
    }

}
