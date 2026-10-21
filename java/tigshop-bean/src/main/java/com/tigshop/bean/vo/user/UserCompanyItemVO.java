package com.tigshop.bean.vo.user;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 会员企业认证详情VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "会员企业认证视图详情对象")
public class UserCompanyItemVO implements Serializable {

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "类型文本")
    private String typeText;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人手机号")
    @Sensitive(type = SensitiveType.PHONE)
    private String contactMobile;

    @Schema(description = "公司名称")
    private String companyName;

    @Schema(description = "公司数据")
    private CompanyDataVO companyData;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "审核时间")
    private String auditTime;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "用户信息")
    private UserVO user;

    @Data
    public static class CompanyDataVO{
        @Schema(description = "证件正面照片")
        private List<PhotoVO> frontOfPhoto;

        @Schema(description = "证件背面照片")
        private List<PhotoVO> backOfPhoto;

        @Schema(description = "许可证地址省份")
        private List<Integer> licenseAddrProvince;

        @Schema(description = "补充信息")
        private List<PicInfo> supplementaryInformation;

        @Schema(description = "营业执照图片")
        private List<PhotoVO> businessLicenseImg;

        @Schema(description = "经营期限类型")
        private Integer operatingTermType;

        @Schema(description = "经营期限类型日期")
        private List<String> operatingTermTypeDate;

        @Schema(description = "经营期限类型结束日期")
        private String operatingTermTypeEnd;

        @Schema(description = "证书有效期")
        private Integer certificateValidityPeriod;

        @Schema(description = "证书有效期日期")
        private List<String> certificateValidityPeriodDate;

        @Schema(description = "证书有效期结束日期")
        private String certificateValidityPeriodEnd;

        @Schema(description = "公司名称")
        private String corporateName;

        @Schema(description = "公司名称")
        private String companyName;

        @Schema(description = "联系电话")
        @Sensitive(type = SensitiveType.PHONE)
        private String contactPhone;

        @Schema(description = "居住地址")
        private String residentialAddress;

        @Schema(description = "生日")
        private String birthday;

        @Schema(description = "证件类型")
        private Integer documentType;

        @Schema(description = "证件号码")
        private String documentNumber;

        @Schema(description = "地址信息")
        private String licenseAddrProvinceName;

        @Schema(description = "企业经营范围")
        private String businessScope;

        @Schema(description = "统一社会信用代码")
        private String businessLicenseId;
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

    @Data
    public static class PhotoVO{
        @Schema(description = "图片名称")
        private String picName;

        @Schema(description = "图片缩略图")
        private String picThumb;

        @Schema(description = "图片URL")
        private String picUrl;
    }

    @Data
    public static class UserVO {
        @Schema(description = "用户id")
        private Integer userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "手机号")
        @Sensitive(type = SensitiveType.PHONE)
        private String mobile;

        @Schema(description = "是否企业认证")
        private Integer isCompanyAuth;
    }
}
