// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.user;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 会员企业认证创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "会员企业认证参数")
public class UserCompanyCreateDTO {
    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "公司数据")
    private CompanyDataVO companyData;

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
        private String contactPhone;

        @Schema(description = "居住地址")
        private String residentialAddress;

        @Schema(description = "生日")
        private String birthday;

        @Schema(description = "证件类型")
        private Integer documentType;

        @Schema(description = "证件号码")
        private String documentNumber;
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
