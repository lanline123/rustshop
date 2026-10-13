// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

import static com.tigshop.common.constant.merchant.MerchantConstants.MERCHANT_ID_NOT_NULL;

/**
  * 商家表更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "商家表参数")
public class MerchantUpdateDTO {
    @Schema(description = "商家表ID")
    @NotNull(message = MERCHANT_ID_NOT_NULL)
    private Integer merchantId;

    @Schema(description = "基础数据")
    private MerchantUpdateDTO.BaseData baseData;

    @Schema(description = "商家数据")
    private MerchantUpdateDTO.MerchantData merchantData;
    /**
     * 基础数据
     */
    @Data
    @Schema(description = "基础数据")
    public static class BaseData {
        @Schema(description = "类型")
        private Integer type;

        @Schema(description = "证书有效期类型")
        private Integer certificateValidityPeriod;

        @Schema(description = "经营期限类型")
        private Integer operatingTermType;

        @Schema(description = "营业执照正面照片")
        private List<MerchantCreateDTO.Photo> frontOfPhoto;

        @Schema(description = "营业执照背面照片")
        private List<MerchantCreateDTO.Photo> backOfPhoto;

        @Schema(description = "许可证地址省份")
        private List<String> licenseAddrProvince;

        @Schema(description = "补充信息")
        private List<MerchantCreateDTO.PicInfo> supplementaryInformation;

        @Schema(description = "证件类型")
        private Integer documentType;

        @Schema(description = "企业名称")
        private String companyName;
        @Schema(description = "法人名称")
        private String corporateName;

        @Schema(description = "证件号码")
        private String documentNumber;

        @Schema(description = "生日")
        private String birthday;

        @Schema(description = "性别")
        private Integer sex;

        @Schema(description = "证书有效期日期")
        private List<String> certificateValidityPeriodDate;

        @Schema(description = "居住地址")
        private String residentialAddress;

        @Schema(description = "正面照片临时URL")
        private String frontOfPhotoTemp;

        @Schema(description = "背面照片临时URL")
        private String backOfPhotoTemp;

        private String businessLicenseAddress;

        @Schema(description = "营业执照ID")
        private String businessLicenseId;

        @Schema(description = "营业范围")
        private String businessScope;

        @Schema(description = "经营期限类型日期")
        private List<String> operatingTermTypeDate;

        @Schema(description = "经营期限类型结束日期")
        private String operatingTermTypeEnd;

        @Schema(description = "证书有效期结束日期")
        private String certificateValidityPeriodEnd;

        @Schema(description = "营业执照地址省份名称")
        private String licenseAddrProvinceName;

    }

    /**
     * 商家数据
     */
    @Data
    @Schema(description = "商家数据")
    public static class MerchantData {
        @Schema(description = "营业地址")
        private List<String> businessAddress;

        @Schema(description = "开户许可证")
        private List<Map<String, Object>> accountOpeningPermit;

        @Schema(description = "营业执照图片")
        private List<Map<String, Object>> businessLicenseImg;

        private String accountOpeningPermitTemp;

        @Schema(description = "银行支行")
        private String bankBranch;

        @Schema(description = "银行卡号")
        private String bankCard;

        @Schema(description = "开户银行")
        private String bankDeposit;

        private String businessLicenseImgTemp;

        @Schema(description = "联系人邮箱")
        private String contactEmail;

        @Schema(description = "联系人姓名")
        private String contactName;

        @Schema(description = "联系人电话")
        private String contactPhone;

        @Schema(description = "客服电话")
        private String customerServicePhone;

        @Schema(description = "详细地址")
        private String detailedAddress;

        @Schema(description = "商家名称")
        private String merchantName;

        @Schema(description = "业务地址名称")
        private String businessAddressName;
    }
}
