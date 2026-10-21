// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.config;

import com.alibaba.fastjson.JSON;
import com.tigshop.bean.annotations.settings.ConfigInnerParam;
import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 获取存储配置返回参数
 *
 * @author Jayce
 * @create 2024年11月11日 17:33
 */
@Data
@Schema(description = "获取存储配置返回参数")
public class GetAdminBaseVO {

    @ConfigItemField(SettingsEnum.ICO_DEFINED_CSS)
    @Schema(description = "Ico定义的CSS")
    private String icoDefinedCss;

    @ConfigItemField(SettingsEnum.DOLLAR_SIGN)
    @Schema(description = "货币符号")
    private String dollarSign;

    @ConfigItemField(SettingsEnum.DOLLAR_SIGN_CN)
    @Schema(description = "货币中文")
    private String dollarSignCn;

    @ConfigItemField(SettingsEnum.STORAGE_TYPE)
    @Schema(description = "存储类型")
    private Integer storageType;

    @ConfigItemField(SettingsEnum.CLIENT_DEFAULT_USE)
    @Schema(description = "前台默认跳转 0 pc ,1 h5")
    private Integer clientDefaultUse;

    @ConfigItemField(SettingsEnum.PC_DOMAIN)
    @Schema(description = "PC域名")
    private String pcDomain;

    @ConfigItemField(SettingsEnum.H5_DOMAIN)
    @Schema(description = "H5域名")
    private String h5Domain;

    @ConfigItemField(SettingsEnum.UPLOAD_MAX_SIZE)
    @Schema(description = "上传文件最大尺寸，单位：MB")
    private Integer uploadMaxSize;

    @ConfigItemField(SettingsEnum.IMAGE_UPLOAD_MAX_SIZE)
    @Schema(description = "图片上传最大尺寸，单位：MB，支持小数")
    private BigDecimal imageUploadMaxSize;

    @ConfigItemField(value = SettingsEnum.SHOP_COMPANY)
    @Schema(description = "版权信息; 1-系统默认，0-自定义")
    private Integer shopCompany;

    @ConfigItemField(value = SettingsEnum.SHOP_COMPANY_TXT)
    @Schema(description = "版权信息自定义值")
    private String shopCompanyTxt;

    @ConfigItemField(value = SettingsEnum.POWERED_BY)
    @Schema(description = "技术支持标识；1-系统默认，0-自定义")
    private Integer poweredBy;

    @ConfigItemField(value = SettingsEnum.POWERED_BY_LOGO)
    @Schema(description = "技术支持标识logo")
    private String poweredByLogo;

    @ConfigItemField(value = SettingsEnum.POWERED_BY_STATUS)
    @Schema(description = "标题栏版权隐藏；1-隐藏，0-显示")
    private Integer poweredByStatus;

    @ConfigItemField(SettingsEnum.LAYOUT)
    @Schema(description = "导航模式")
    private String layout;

    @ConfigItemField(SettingsEnum.NAV_THEME)
    @Schema(description = "外观")
    private String navTheme;

    @ConfigItemField(SettingsEnum.PRIMARY_COLOR)
    @Schema(description = "主题色")
    private String primaryColor;

    @ConfigItemField(SettingsEnum.ADMIN_LIGHT_LOGO)
    @Schema(description = "后台LOGO")
    private String adminLightLogo;

    @ConfigItemField(SettingsEnum.VERSION_INFO_HIDDEN)
    @Schema(description = "是否隐藏版本信息; 0-否，1-是")
    private Integer versionInfoHidden;

    @ConfigItemField(SettingsEnum.DEFAULT_COPYRIGHT)
    @Schema(description = "默认版权")
    private String defaultCopyright;

    @ConfigItemField(SettingsEnum.DEFAULT_TECH_SUPPORT)
    @Schema(description = "默认技术支持")
    private String defaultTechSupport;

    // *** Other ***

    @Schema(description = "存储URL")
    private String storageUrl;

    @Schema(description = "版本类型")
    private String versionType;

    @Schema(description = "版本号")
    private String version;

    // 提现设置
    @Schema(description = "提现设置")
    @ConfigInnerParam
    private WithdrawSettingVO withdrawSettingVO;

    @Schema(description = "是否开启到店自提")
    private Integer isOpenShopPickup;

    @Schema(description = "是否开启店铺保证金；0-否，1-是")
    private Integer isOpenDeposit;

    @ConfigItemField(SettingsEnum.SHOP_MEMBER_ENABLED)
    @Schema(description = "是否开启店铺会员；0-否，1-是（店铺端据此判定冻结态）")
    private Integer isOpenShopMember;

    @ConfigItemField(SettingsEnum.SHOP_MEMBER_ENABLED)
    @Schema(description = "店铺会员功能总开关；0-关闭 1-启用")
    private Integer shopMemberEnabled;

    @ConfigItemField(SettingsEnum.SHOP_MEMBER_DESENSITIZE)
    @Schema(description = "店铺客户信息脱敏；0-关闭 1-启用")
    private Integer shopMemberDesensitize;

    @ConfigItemField(SettingsEnum.SHOP_MEMBER_ALLOW_ADD)
    @Schema(description = "店铺添加客户；0-关闭 1-启用")
    private Integer shopMemberAllowAdd;

    @ConfigItemField(SettingsEnum.SHOP_MEMBER_ALLOW_IMPORT)
    @Schema(description = "店铺导入客户；0-关闭 1-启用")
    private Integer shopMemberAllowImport;

    @ConfigItemField(value = SettingsEnum.STORE_SELF_OPERATED_NAME)
    @Schema(description = "多门店名称")
    private String shopTitle;

    @ConfigItemField(SettingsEnum.CUSTOM_PRODUCT_NAME_LENGTH)
    @Schema(description = "自定义商品名称长度")
    private Integer customProductNameLength;

    @ConfigItemField(SettingsEnum.PRODUCT_EDIT_STOCK_MODIFY)
    @Schema(description = "商品编辑库存是否可修改；0-否，1-是（默认1）")
    private Integer productEditStockModify;

    @ConfigItemField(SettingsEnum.SPEC_IMAGE_MODE)
    @Schema(description = "规格图片上传范围；1-仅第一组规格可上传图片，2-全部规格可上传图片（默认1）")
    private Integer specImageMode;

    @Data
    public static class WithdrawSettingVO {
        @ConfigItemField(SettingsEnum.WITHDRAWAL_RECEIPT_METHOD)
        @Schema(description = "线下收款方式，数组：1-银行卡，2-支付宝，3-微信")
        private JSON withdrawalReceiptMethod;

        @ConfigItemField(SettingsEnum.WITHDRAWAL_ENABLED)
        @Schema(description = "是否开通提现，0-不开通，1-开通")
        private Integer withdrawalEnabled;

        @ConfigItemField(SettingsEnum.WITHDRAWAL_MIN_AMOUNT)
        @Schema(description = "提现限额-最小金额")
        private BigDecimal minAmount;

        @ConfigItemField(SettingsEnum.WITHDRAWAL_MAX_AMOUNT)
        @Schema(description = "提现限额-最大金额")
        private BigDecimal maxAmount;

        @ConfigItemField(SettingsEnum.WITHDRAWAL_FREQUENCY_UNIT)
        @Schema(description = "提现频次单位，1-天，2-周，3-月")
        private Integer withdrawalFrequencyUnit;

        @ConfigItemField(SettingsEnum.WITHDRAWAL_FREQUENCY_COUNT)
        @Schema(description = "提现频次数量")
        private Integer withdrawalFrequencyCount;

         @ConfigItemField(SettingsEnum.WITHDRAWAL_REVIEW_METHOD)
         @Schema(description = "提现审核方式，1-人工审核")
         private Integer withdrawalReviewMethod;

        @ConfigItemField(SettingsEnum.WITHDRAWAL_DESCRIPTION)
        @Schema(description = "提现说明")
        private String withdrawalDescription;
    }
}
