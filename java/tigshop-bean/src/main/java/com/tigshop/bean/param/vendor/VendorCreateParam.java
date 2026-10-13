// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.param.vendor;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/7/4 10:19
 */
@Data
public class VendorCreateParam {

    @Schema(description = "类型 1个人 2企业")
    private Integer type;

    @Schema(description = "供应商logo")
    private String vendorLogo;

    @Schema(description = "管理员信息")
    private VendorCreateParam.AdminInfo adminData;

    @Schema(description = "供应商信息")
    private VendorCreateParam.VendorData vendorData;

    @Schema(description = "个人信息")
    private VendorCreateParam.PersonData personData;

    @Schema(description = "供应商名称")
    private String vendorName;

    /**
     * 管理员信息
     */
    @Data
    @Schema(description = "管理员信息")
    public static class AdminInfo {
        @Schema(description = "类型")
        private Integer type;

        @Schema(description = "用户ID")
        private Integer userId;

        @Schema(description = "管理员ID")
        private Integer adminId;
    }

    /**
     * 供应商信息
     */
    @Data
    @Schema(description = "供应商信息")
    public static class VendorData {

        @Schema(description = "供应商名称")
        private String vendorName;

        @Schema(description = "客服电话")
        private String customerServicePhone;

        @Schema(description = "供应商地址")
        private List<Integer> vendorAddress;

        @Schema(description = "详细地址")
        private String detailedAddress;

        @Schema(description = "联系人姓名")
        private String contactName;

        // 此手机后续作为登录账户
        @Schema(description = "联系人手机")
        private String contactPhone;

        @Schema(description = "联系人邮箱")
        private String contactEmail;

        @Schema(description = "开户银行")
        private String bankDeposit;

        @Schema(description = "支行名称")
        private String bankBranch;

        @Schema(description = "银行卡号")
        private String bankCard;

        @Schema(description = "补充信息")
        private List<VendorCreateParam.PicInfo> additionalImg;


        @Schema(description = "企业名称")
        private String companyName;

        @Schema(description = "企业名称注册地址")
        private List<Integer> licenseAddrProvince;

        @Schema(description = "企业名称详细地址")
        private String businessLicenseAddress;

        @Schema(description = "企业经营范围")
        private String businessScope;

        @Schema(description = "企业同一社会信用码")
        private String businessLicenseId;

        @Schema(description = "经营期限类型")
        private Integer operatingTermType;

        @Schema(description = "营业期限")
        private List<String> operatingTermTypeDate;

        @Schema(description = "营业期限长期有效")
        private String operatingTermTypeEnd;

    }

    /**
     * 个人信息
     */
    @Data
    @Schema(description = "个人信息")
    public static class PersonData {
        @Schema(description = "证件类型")
        private Integer documentType;

        @Schema(description = "姓名")
        private String documentName;

        @Schema(description = "证件号码")
        private String documentNumber;

        @Schema(description = "出生日期")
        private String birthday;

        @Schema(description = "证书有效期")
        private Integer certificateValidityPeriod;

        @Schema(description = "证书有效期日期")
        private List<String> certificateValidityPeriodDate;

        @Schema(description = "证书有效期日期")
        private String certificateValidityPeriodEnd;

        @Schema(description = "居住地址")
        private String residentialAddress;

        @Schema(description = "性别")
        private Integer sex;

        @Schema(description = "正面照片")
        private String frontOfPhoto;

        @Schema(description = "背面照片")
        private String backOfPhoto;

        @Schema(description = "补充信息")
        private List<VendorCreateParam.PicInfo> additionalImg;
    }

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
}
