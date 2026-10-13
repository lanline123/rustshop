package com.tigshop.bean.vo.setting.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 基础配置
 *
 * @author kidd
 * @since 2025/4/3 16:20
 */
@Schema(description = "基础配置")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasicSettingsVO {

    // *** 商城信息 ***

    @ConfigItemField(SettingsEnum.SHOP_LOGO)
    @Schema(description = "商城LOGO")
    private String shopLogo;

    @ConfigItemField(SettingsEnum.LIGHT_SHOP_LOGO)
    @Schema(description = "lightShopLogo")
    private String lightShopLogo;

    @ConfigItemField(SettingsEnum.SHOP_NAME)
    @Schema(description = "商城简称")
    private String shopName;

    @ConfigItemField(value = SettingsEnum.SHOP_COMPANY)
    @Schema(description = "版权信息; 1-系统默认，0-自定义")
    private Integer shopCompany;

    @ConfigItemField(SettingsEnum.DEFAULT_COPYRIGHT)
    @Schema(description = "默认版权")
    private String defaultCopyright;

    @ConfigItemField(value = SettingsEnum.SHOP_COMPANY_TXT)
    @Schema(description = "版权信息自定义值")
    private String shopCompanyTxt;

    @ConfigItemField(value = SettingsEnum.POWERED_BY_STATUS)
    @Schema(description = "标题栏版权隐藏；1-隐藏，0-显示")
    private Integer poweredByStatus;

    @ConfigItemField(value = SettingsEnum.POWERED_BY)
    @Schema(description = "技术支持标识；1-系统默认，0-自定义")
    private Integer poweredBy;

    @ConfigItemField(SettingsEnum.DEFAULT_TECH_SUPPORT)
    @Schema(description = "默认技术支持")
    private String defaultTechSupport;

    @ConfigItemField(value = SettingsEnum.POWERED_BY_LOGO)
    @Schema(description = "技术支持标识logo")
    private String poweredByLogo;

    @ConfigItemField(SettingsEnum.KEFU_ADDRESS)
    @Schema(description = "企业地址")
    private String kefuAddress;

    // *** 备案信息 ***

    @ConfigItemField(SettingsEnum.SHOP_ICP_NO)
    @Schema(description = "ICP备案号")
    private String shopIcpNo;

    @ConfigItemField(SettingsEnum.SHOP_ICP_NO_URL)
    @Schema(description = "ICP备案链接")
    private String shopIcpNoUrl;

    @ConfigItemField(SettingsEnum.SHOP_110_NO)
    @Schema(description = "公安备案号")
    private String shop110No;

    @ConfigItemField(SettingsEnum.SHOP_110_LINK)
    @Schema(description = "公安备案链接")
    private String shop110Link;

    // *** 经营许可证 ***

    @ConfigItemField(SettingsEnum.BUSINESS_LICENSE_ENABLED)
    @Schema(description = "经营许可证显示开关；1-开启，0-关闭")
    private Integer businessLicenseEnabled;

    @ConfigItemField(SettingsEnum.BUSINESS_LICENSE)
    @Schema(description = "增值电信业务经营许可证(ICP)证号")
    private String businessLicense;

    @ConfigItemField(SettingsEnum.EDI_LICENSE)
    @Schema(description = "EDI经营许可证号")
    private String ediLicense;

    @ConfigItemField(SettingsEnum.BUSINESS_LICENSE_TYPE)
    @Schema(description = "经营许可证类型，可多选：icp、edi")
    private List<String> businessLicenseType;

    // *** 商城状态 ***

    @ConfigItemField(SettingsEnum.CLOSE_ORDER)
    @Schema(description = "关闭下单；0-否，1-是")
    private Integer closeOrder;

    @ConfigItemField(SettingsEnum.SHOP_REG_CLOSED)
    @Schema(description = "关闭注册；0-否，1-是")
    private Integer shopRegClosed;

}
