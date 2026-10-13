package com.tigshop.bean.vo.merchant;

import cn.hutool.json.JSONObject;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 商家表VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "商家表参数")
public class MerchantApplyDetailVO {
    @Schema(description = "商户认证日期")
    private String addTime;

    @Schema(description = "基础数据json")
    private BaseData baseData;

    @Schema(description = "企业名称")
    private String companyName;

    @Schema(description = "联系人名称")
    private String contactName;

    @Schema(description = "联系人手机")
    @Sensitive(type = SensitiveType.PHONE)
    private String contactMobile;

    @Schema(description = "申请主体信息")
    private String corporateName;

    @Schema(description = "入驻申请ID")
    private Integer merchantApplyId;

    @Schema(description = "商户数据JSON")
    private MerchantData merchantData;

    @Schema(description = "审核时间")
    private Long auditTime;

    @Schema(description = "审核管理员ID(admin_user.admin_id)，0 表示系统自动通过或未审核")
    private Integer auditAdminId;

    @Schema(description = "审核管理员姓名，0 或管理员已删除时返回空串")
    private String auditAdminName;

    @Schema(description = "店铺数据json")
    private JSONObject shopData;

    @Schema(description = "状态，1：正常，2：取消资格")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "类型，1：个人，2：企业")
    private Integer type;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "用户数据")
    private initial initialUserInfo;

    @Data
    @Schema(description = "用户信息")
    public static class initial {
        @Schema(description = "用户名")
        private String username;

        @Schema(description = "用户手机")
        @Sensitive(type = SensitiveType.PHONE)
        private String mobile;

        @Schema(description = "初始密码")
        private String initialPassword;
    }

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
        @Sensitive(type = SensitiveType.PHONE)
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
        @Sensitive(type = SensitiveType.PHONE)
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