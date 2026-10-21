// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.setting;

import com.tigshop.bean.dto.setting.SearchSaveParam;
import com.tigshop.bean.dto.setting.DingMessageConfigDTO;
import com.tigshop.bean.vo.workbuddy.WorkBuddyKeySettingsVO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.setting.PemFileType;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.param.settings.config.*;
import com.tigshop.bean.vo.config.*;
import com.tigshop.bean.vo.setting.SearchSettingsVO;
import com.tigshop.bean.vo.setting.config.*;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.im.ShopKefuConfigService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.LocalStorageService;
import com.tigshop.service.workbuddy.WorkBuddyKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * 设置控制器
 *
 * @author kidd
 * @since 2025/4/1 09:37
 */
@RequiredArgsConstructor
@Tag(name = "设置")
@RestController
@RequestMapping("/adminapi/setting/config")
public class ConfigController {

    private final ConfigService configService;

    private final LocalStorageService localStorageService;

    private final TranslatePackage translatePackage;

    private final TigshopProperties tigshopProperties;

    private final ShopKefuConfigService shopKefuConfigService;

    private final WorkBuddyKeyService workBuddyKeyService;

    @Operation(summary = "获取商城设置-基础信息")
    @PreAuthorize("@pms.hasPermission('configManage','config')")
    @GetMapping("/basicSettings")
    public BasicSettingsVO getBasicSettings() {
        return configService.getBasicSettings();
    }

    @Operation(summary = "保存商城设置-基础信息")
    @PreAuthorize("@pms.hasPermission('saveBasicManage')")
    @PostMapping("/saveBasic")
    public void saveBasic(@RequestBody @Validated BasicSaveParam param) {
        configService.saveBasic(param);
    }

    @Operation(summary = "获取商城设置-商品设置")
    @PreAuthorize("@pms.hasPermission('configManage','config','productManage')")
    @GetMapping("/productSettings")
    public ProductSettingsVO getProductSettings() {
        return configService.getProductSettings();
    }

    @Operation(summary = "保存商城设置-商品设置")
    @PreAuthorize("@pms.hasPermission('saveSettingProductManage')")
    @PostMapping("/saveProduct")
    public void saveProduct(@RequestBody @Validated ProductSaveParam param) {
        configService.saveProduct(param);
    }

    @Operation(summary = "获取商城设置-交易设置")
    @PreAuthorize("@pms.hasPermission('configManage','config')")
    @GetMapping("/shoppingSettings")
    public ShoppingSettingsVO getShoppingSettings() {
        return configService.getShoppingSettings();
    }

    @Operation(summary = "保存商城设置-交易设置")
    @PreAuthorize("@pms.hasPermission('saveSettingShoppingManage')")
    @PostMapping("/saveShopping")
    public void saveShoppingSettings(@RequestBody @Validated ShoppingSaveParam param) {
        configService.saveShoppingSettings(param);
    }

    @Operation(summary = "获取商城设置-订单设置")
    @PreAuthorize("@pms.hasPermission('configManage','config')")
    @GetMapping("/orderSettings")
    public OrderSettingsVO getOrderSettings() {
        return configService.getOrderSettings();
    }

    @Operation(summary = "保存商城设置-订单设置")
    @PreAuthorize("@pms.hasPermission('saveSettingsOrderManage')")
    @PostMapping("/saveOrder")
    public void saveOrderSettings(@RequestBody @Validated OrderSettingsSaveParam param) {
        configService.saveOrderSettings(param);
    }

    @Operation(summary = "获取商城设置-客服设置")
    @PreAuthorize("@pms.hasPermission('configManage','config','kefuSetting')")
    @GetMapping("/kefuSettings")
    public KefuSettingsVO getKefuSettings() {
        Integer shopId = resolveKefuShopId();
        if (shopId > 0) {
            return shopKefuConfigService.getKefuSettings(shopId);
        }
        return configService.getKefuSettings();
    }

    @Operation(summary = "保存商城设置-客服设置")
    @PreAuthorize("@pms.hasPermission('saveSettingKefuManage')")
    @PostMapping("/saveKefu")
    public void saveKefuSettings(@RequestBody @Validated KefuSaveParam param) {
        Integer shopId = resolveKefuShopId();
        if (shopId > 0) {
            shopKefuConfigService.saveKefuSettings(shopId, param);
            return;
        }
        configService.saveKefuSettings(param);
    }

    private Integer resolveKefuShopId() {
        if (AdminTypeEnum.ADMIN.getCode().equals(HeaderUtils.getAdminType())) {
            return 0;
        }
        Integer shopId = HeaderUtils.getShopId();
        if (shopId == null || shopId <= 0) {
            throw new GlobalException("无法获取当前店铺信息");
        }
        return shopId;
    }

    @Operation(summary = "获取配送设置-配送设置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/shippingSettings")
    public ShippingSettingsVO getShippingSettings() {
        return configService.getShippingSettings();
    }

    @Operation(summary = "保存配送设置-配送设置")
    @PreAuthorize("@pms.hasPermission('settingSaveShippingManage')")
    @PostMapping("/saveShipping")
    public void saveShippingSettings(@RequestBody @Validated ShippingSaveParam param) {
        configService.saveShippingSettings(param);
    }

    @Operation(summary = "获取系统设置-全局设置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/globalSettings")
    public GlobalSettingsVO getGlobalSettings() {
        return configService.getGlobalSettings();
    }

    @Operation(summary = "保存系统设置-全局设置")
    @PreAuthorize("@pms.hasPermission('saveSettingsGlobalManage')")
    @PostMapping("/saveGlobal")
    public GlobalSettingsVO saveGlobalSettings(@RequestBody @Validated GlobalSettingsSaveParam param) {
        return configService.saveGlobalSettings(param);
    }

    @Operation(summary = "获取 MCP 密钥设置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/workbuddySettings")
    public WorkBuddyKeySettingsVO getWorkBuddySettings() {
        return workBuddyKeyService.settings();
    }

    @Operation(summary = "生成 MCP 密钥")
    @PreAuthorize("@pms.hasPermission('saveSettingsGlobalManage')")
    @PostMapping("/generateWorkbuddyKey")
    public WorkBuddyKeyGenerateResult generateWorkbuddyKey() {
        WorkBuddyKeyGenerateResult result = new WorkBuddyKeyGenerateResult();
        result.setKey(workBuddyKeyService.generateKey());
        return result;
    }

    @Operation(summary = "复制 MCP 密钥")
    @PreAuthorize("@pms.isPlatformSuperAdmin()")
    @PostMapping("/revealWorkbuddyKey")
    public WorkBuddyKeyGenerateResult revealWorkbuddyKey() {
        WorkBuddyKeyGenerateResult result = new WorkBuddyKeyGenerateResult();
        result.setKey(workBuddyKeyService.revealKey());
        return result;
    }

    @lombok.Data
    public static class WorkBuddyKeyGenerateResult {
        private String key;
        private Long expiresAt;
    }

    @Operation(summary = "获取系统设置-登录设置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/loginSettings")
    public LoginSettingsVO getLoginSettings() {
        return configService.getLoginSettings();
    }

    @Operation(summary = "保存系统设置-登录设置")
    @PreAuthorize("@pms.hasPermission('saveSettingsSaveLoginManage')")
    @PostMapping("/saveLogin")
    public void saveLoginSettings(@RequestBody @Validated LoginSettingsSaveParam param) {
        configService.saveLoginSettings(param);
    }

    @Operation(summary = "获取系统设置-支付基础设置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/basicPaySettings")
    public BasicPaySettingsVO getBasicPaySettings() {
        return configService.getBasicPaySettings();
    }

    @Operation(summary = "保存系统设置-支付基础设置")
    @PreAuthorize("@pms.hasPermission('uploadFileModifyManage')")
    @PostMapping("/saveBasicPay")
    public void saveBasicPaySettings(@RequestBody @Validated BasicPaySettingsSaveParam param) {
        configService.saveBasicPaySettings(param);
    }

    @Operation(summary = "获取系统设置-微信支付设置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/wechatPaySettings")
    public WechatPaySettingsVO getWechatPaySettings() {
        return configService.getWechatPaySettings();
    }

    @Operation(summary = "保存系统设置-微信支付设置")
    @PreAuthorize("@pms.hasPermission('uploadFileModifyManage')")
    @PostMapping("/saveWechatPay")
    public void saveWechatPaySettings(@RequestBody @Validated WechatPaySettingsSaveParam param) {
        configService.saveWechatPaySettings(param);
    }

    @Operation(summary = "获取小程序风控配置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/miniProgramRiskSettings")
    public MiniProgramRiskSettingsVO getMiniProgramRiskSettings() {
        return configService.getMiniProgramRiskSettings();
    }

    @Operation(summary = "保存小程序风控配置")
    @PreAuthorize("@pms.hasPermission('uploadFileModifyManage')")
    @PostMapping("/saveMiniProgramRisk")
    public void saveMiniProgramRiskSettings(@RequestBody @Validated MiniProgramRiskSettingsSaveParam param) {
        configService.saveMiniProgramRiskSettings(param);
    }

    @Operation(summary = "获取小程序直播/视频号独立小程序配置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/wechatLiveSettings")
    public WechatLiveSettingsVO getWechatLiveSettings() {
        return configService.getWechatLiveSettings();
    }

    @Operation(summary = "保存小程序直播/视频号独立小程序配置")
    @PreAuthorize("@pms.hasPermission('uploadFileModifyManage')")
    @PostMapping("/saveWechatLive")
    public void saveWechatLiveSettings(@RequestBody @Validated WechatLiveSettingsSaveParam param) {
        configService.saveWechatLiveSettings(param);
    }

    @Operation(summary = "生成平台证书")
    @PreAuthorize("@pms.hasPermission('uploadFileModifyManage')")
    @PostMapping("/createPlatformCertificate")
    public void createPlatformCertificate() {
        configService.createPlatformCertificate();
    }

    @Operation(summary = "上传密钥文件")
    @PreAuthorize("@pms.hasPermission('uploadFileModifyManage')")
    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam("type") Integer type, @RequestParam("file") MultipartFile file) {
        return localStorageService.upload(file, PemFileType.fromCode(type).getDescription());
    }

    @Operation(summary = "获取系统设置-支付宝支付设置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/aliPaySettings")
    public AliPaySettingsVO getAliPaySettings() {
        return configService.getAliPaySettings();
    }

    @Operation(summary = "保存系统设置-支付宝支付设置")
    @PreAuthorize("@pms.hasPermission('uploadFileModifyManage')")
    @PostMapping("/saveAliPay")
    public void saveAliPaySettings(@RequestBody @Validated AliPaySettingsSaveParam param) {
        configService.saveAliPaySettings(param);
    }

    @Operation(summary = "获取系统设置-YaBandPay支付设置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/yaBandPaySettings")
    public YaBandPaySettingsVO getYaBandPaySettings() {
        return configService.getYaBandPaySettings();
    }

    @Operation(summary = "保存系统设置-YaBandPay支付设置")
    @PreAuthorize("@pms.hasPermission('uploadFileModifyManage')")
    @PostMapping("/saveYaBandPay")
    public void saveYaBandPaySettings(@RequestBody @Validated YaBandPaySettingsSaveParam param) {
        configService.saveYaBandPaySettings(param);
    }

    @Operation(summary = "获取系统设置-线下支付设置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/offlinePaySettings")
    public OfflinePaySettingsVO getOfflinePaySettings() {
        return configService.getOfflinePaySettings();
    }

    @Operation(summary = "保存系统设置-线下支付设置")
    @PreAuthorize("@pms.hasPermission('uploadFileModifyManage')")
    @PostMapping("/saveOfflinePay")
    public void saveOfflinePaySettings(@RequestBody @Validated OfflinePaySettingsSaveParam param) {
        configService.saveOfflinePaySettings(param);
    }

    @Operation(summary = "获取系统设置-PayPal支付设置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/payPalSettings")
    public PayPalSettingsVO getPayPalSettings() {
        return configService.getPayPalSettings();
    }

    @Operation(summary = "保存系统设置-PayPal支付设置")
    @PreAuthorize("@pms.hasPermission('uploadFileModifyManage')")
    @PostMapping("/savePayPal")
    public void savePayPalSettings(@RequestBody @Validated PayPalSettingsSaveParam param) {
        configService.savePayPalSettings(param);
    }


    // Apple Pay 设置端点已拆至 ApplePayConfigController（VORT 门控 common.pay.applePay 专属，整文件可删）



    @Operation(summary = "获取系统设置-云支付设置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/yunPaySettings")
    public YunPaySettingsVO getYunPaySettings() {
        return configService.getYunPaySettings();
    }

    @Operation(summary = "保存系统设置-云支付设置")
    @PreAuthorize("@pms.hasPermission('uploadFileModifyManage')")
    @PostMapping("/saveYunPay")
    public void saveYunPaySettings(@RequestBody @Validated YunPaySettingsSaveParam param) {
        configService.saveYunPaySettings(param);
    }

    // 汇付/拉卡拉设置端点已拆至 HuifuConfigController / LakalaConfigController（VORT 门控 common.pay.* 专属，整文件可删）

    @Operation(summary = "获取系统设置-邮箱配置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/mailSettings")
    public MailSettingsVO getMailSettings() {
        return configService.getMailSettings();
    }

    @Operation(summary = "保存系统设置-邮箱配置")
    @PreAuthorize("@pms.hasPermission('mailTemplatesUpdateManage')")
    @PostMapping("/saveMail")
    public void saveMail(@RequestBody @Validated MailSaveParam param) {
        configService.saveMail(param);
    }

    @Operation(summary = "发送测试邮件")
    @PreAuthorize("@pms.hasPermission('sendTestEmailModifyManage')")
    @PostMapping("/sendTestEmail")
    public void sendTestEmail(@RequestParam("testMailAddress") String testMailAddress) {
        configService.sendTestMail(testMailAddress);
    }

    @Operation(summary = "获取消息设置-通知配置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/notifySettings")
    public NotifySettingsVO getNotifySettings() {
        return configService.getNotifySettings();
    }

    @Operation(summary = "保存消息设置-通知配置")
    @PreAuthorize("@pms.hasPermission('saveSettingNotifyManage')")
    @PostMapping("/saveNotify")
    public void saveNotify(@RequestBody @Validated NotifySaveParam param) {
        configService.saveNotify(param);
    }

    @Operation(summary = "获取会员认证配置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/authSettings")
    public AuthSettingsVO getAuthSettings() {
        return configService.getAuthSettings();
    }

    @Operation(summary = "保存会员认证配置")
    @PreAuthorize("@pms.hasPermission('saveAuthSettingsManage')")
    @PostMapping("/saveAuth")
    public void saveAuth(@RequestBody @Validated AuthSaveParam param) {
        configService.saveAuth(param);
    }

    @Operation(summary = "获取售后配置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/afterSalesSettings")
    public AfterSalesSettingsVO getAfterSalesSettings() {
        AfterSalesSettingsVO afterSalesSettings = configService.getAfterSalesSettings();
        return afterSalesSettings;
    }

    @Operation(summary = "保存售后配置")
    @PreAuthorize("@pms.hasPermission('settingSaveAfterSalesManage')")
    @PostMapping("/saveAfterSales")
    public void saveAfterSales(@RequestBody @Validated AfterSaleSaveParam param) {
        configService.saveAfterSales(param);
    }

    @Operation(summary = "获取主题风格配置")
    @PreAuthorize("@pms.hasPermission('config','decorate')")
    @GetMapping("/themeStyleSettings")
    public ThemeStyleSettingsVO getThemeStyleSettings() {
        return configService.getThemeStyleSettings();
    }

    @Operation(summary = "保存主题风格配置")
    @PreAuthorize("@pms.hasPermission('themeStyleUpdateManage')")
    @PostMapping("/saveThemeStyle")
    public void saveThemeStyle(@RequestBody @Validated ThemeStyleSaveParam param) {
        configService.saveThemeStyle(param);
    }

    @Operation(summary = "获取分类页装修配置")
    @PreAuthorize("@pms.hasPermission('config','decorate')")
    @GetMapping("/categoryDecorateSettings")
    public CategoryDecorateSettingsVO getCategoryDecorateSettings() {
        return configService.getCategoryDecorateSettings();
    }

    @Operation(summary = "保存分类页装修配置")
    @PreAuthorize("@pms.hasPermission('themeCategoryDecorateManage')")
    @PostMapping("/saveCategoryDecorate")
    public void saveCategoryDecorate(@RequestBody @Validated CategoryDecorateSaveParam param) {
        configService.saveCategoryDecorate(param);
    }

    @Operation(summary = "获取商品详情页风格配置")
    @PreAuthorize("@pms.hasPermission('config','decorate')")
    @GetMapping("/productDetailStyleSettings")
    public ProductDetailStyleSettingsVO getProductDetailStyleSettings() {
        return configService.getProductDetailStyleSettings();
    }

    @Operation(summary = "保存商品详情页风格配置")
    @PreAuthorize("@pms.hasPermission('pcProductStyleSaveManage')")
    @PostMapping("/saveProductDetailStyle")
    public void saveProductDetailStyle(@RequestBody @Validated ProductDetailStyleSaveParam param) {
        configService.saveProductDetailStyle(param);
    }

    @Operation(summary = "获取搜索页风格配置")
    @PreAuthorize("@pms.hasPermission('config','decorate')")
    @GetMapping("/searchStyleSettings")
    public SearchStyleSettingsVO getSearchStyleSettings() {
        return configService.getSearchStyleSettings();
    }

    @Operation(summary = "保存搜索页风格配置")
    @PreAuthorize("@pms.hasPermission('pcSearchStyleSaveManage')")
    @PostMapping("/saveSearchStyle")
    public void saveSearchStyle(@RequestBody @Validated SearchStyleSaveParam param) {
        configService.saveSearchStyle(param);
    }

    @Operation(summary = "获取移动端搜索风格配置")
    @PreAuthorize("@pms.hasPermission('config','decorate')")
    @GetMapping("/mobileSearchStyleSettings")
    public MobileSearchStyleSettingsVO getMobileSearchStyleSettings() {
        return configService.getMobileSearchStyleSettings();
    }

    @Operation(summary = "保存移动端搜索风格配置")
    @PreAuthorize("@pms.hasPermission('mobileSearchStyleSaveManage')")
    @PostMapping("/saveMobileSearchStyle")
    public void saveMobileSearchStyle(@RequestBody @Validated MobileSearchStyleSaveParam param) {
        configService.saveMobileSearchStyle(param);
    }

    @Operation(summary = "获取个人中心页装修配置")
    @PreAuthorize("@pms.hasPermission('mobileUserManage')")
    @GetMapping("/memberCenterDecorateSettings")
    public MemberCenterDecorateSettingsVO getMemberCenterDecorateSettings() {
        return configService.getMemberCenterDecorateSettings();
    }

    @Operation(summary = "保存个人中心页装修配置")
    @PreAuthorize("@pms.hasPermission('mobileUserManage')")
    @PostMapping("/saveMemberCenterDecorate")
    public void saveMemberCenterDecorate(@RequestBody @Validated MemberCenterDecorateSaveParam param) {
        configService.saveMemberCenterDecorate(param);
    }

    @Operation(summary = "获取店铺配置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/shopSettings")
    public ShopSettingsVO getShopSettings() {
        return configService.getShopSettings();
    }

    @Operation(summary = "保存店铺配置")
    @PreAuthorize("@pms.hasPermission('shopSettingsUpdateManage')")
    @PostMapping("/saveShop")
    public void saveShop(@RequestBody @Validated ShopSaveParam param) {
        configService.saveShop(param);
    }

    @Operation(summary = "获取买家秀配置")
    @GetMapping("/buyerShowSettings")
    @PreAuthorize("@pms.hasPermission('buyerShowManage')")
    public BuyerShowSettingsVO getBuyerShowSettings() {
        return configService.getBuyerShowSettings();
    }

    @Operation(summary = "保存买家秀配置")
    @PostMapping("/saveBuyerShow")
    @PreAuthorize("@pms.hasPermission('buyerShowSettingsUpdateManage')")
    public void saveBuyerShow(@RequestBody @Validated BuyerShowSaveParam param) {
        configService.saveBuyerShow(param);
    }

    @Operation(summary = "获取商户配置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/merchantSettings")
    public MerchantSettingsVO getMerchantSettings() {
        return configService.getMerchantSettings();
    }

    @Operation(summary = "保存商户配置")
    @PreAuthorize("@pms.hasPermission('adminMerchantSettingsUpdateManage')")
    @PostMapping("/saveMerchant")
    public void saveMerchant(@RequestBody @Validated MerchantSaveParam param) {
        configService.saveMerchant(param);
    }


    @Operation(summary = "根据配置编码获取配置")
    @GetMapping("/getAdmin")
    public GetAdminVO getAdmin() {
        return configService.getAdmin();
    }

    @Operation(summary = "admin 初始化配置")
    @GetMapping("/getAdminBase")
    public GetAdminBaseVO getAdminBase() {
        return configService.getAdminBase();
    }

    @Operation(summary = "获取基础配置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/basicConfig")
    public ConfigBasicVO getBasicConfig() {
        return configService.getConfigBasic();
    }

    @Operation(summary = "获取图标icon")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/getIcon")
    public IconSettingsVO getIcon() {
        return configService.getIcon();
    }

    @Operation(summary = "根据配置编码获取配置")
    @PreAuthorize("@pms.hasPermission('config')")
    @GetMapping("/getBase")
    public ConfigPO getBaseConfig(@RequestParam String code) {
        return configService.getConfigByCode(code != null ? code : "base");
    }

    @Operation(summary = "获取供应商配置")
    @PreAuthorize("@pms.hasPermission('vendorSettingManage')")
    @GetMapping("/vendorSettings")
    public VendorSettingsVO getVendorSettings() {
        return configService.getVendorSettings();
    }

    @Operation(summary = "保存供应商配置")
    @PreAuthorize("@pms.hasPermission('vendorSettingUpdateManage')")
    @PostMapping("/saveVendor")
    public void saveVendor(@RequestBody @Validated VendorSaveParam param) {
        configService.saveVendor(param);
    }

    @Operation(summary = "获取登录协议")
    @GetMapping("/getLoginProtocol")
    public GetLoginProtocolVO getLoginProtocol() {
        return configService.getLoginProtocol();
    }

    @Operation(summary = "获取登录协议内容  termsOfService:服务协议；privacyPolicy:隐私政策；afterSalesService:售后服务")
    @GetMapping("/getLoginProtocolContent")
    public GetLoginProtocolContentVO getLoginProtocolContentVO(String code) {
        return configService.getLoginProtocolContentVO(code);
    }

    @Operation(summary = "保存登录协议")
    @PostMapping("/saveLoginProtocol")
    @PreAuthorize("@pms.hasPermission('protocolUpdateManagement')")
    public void saveLoginProtocol(@RequestBody @Validated LoginProtocolParam param) {
        configService.saveLoginProtocol(param);
    }

    @Operation(summary = "获取结算与分账配置")
    @GetMapping("/profitSharingSettings")
    public ProfitSharingSettingsVO getProfitSharingSettings() {
        return configService.getProfitSharingSettings();
    }

    @Operation(summary = "保存结算与分账配置")
    @PreAuthorize("@pms.hasPermission('profitsharingSettingUpdateManage')")
    @PostMapping("/saveProfitSharing")
    public void saveProfitSharing(@RequestBody @Validated ProfitSharingSaveParam param) {
        configService.saveProfitSharing(param);
    }

    @Operation(summary = "获取提现配置")
    @GetMapping("/withdrawalSettings")
    public WithdrawalSettingsVO getWithdrawalSettings() {
        return configService.getWithdrawalSettings();
    }

    @Operation(summary = "保存提现配置")
    @PostMapping("/saveWithdrawal")
    public void saveWithdrawal(@RequestBody @Validated WithdrawalSaveParam param) {
        configService.saveWithdrawal(param);
    }

    @Operation(summary = "获取会员余额提现配置")
    @GetMapping("/memberWithdrawalSettings")
    public MemberWithdrawalSettingsVO getMemberWithdrawalSettings() {
        return configService.getMemberWithdrawalSettings();
    }

    @Operation(summary = "保存会员余额提现配置")
    @PostMapping("/saveMemberWithdrawal")
    public void saveMemberWithdrawal(@RequestBody @Validated MemberWithdrawalSaveParam param) {
        configService.saveMemberWithdrawal(param);
    }

    @Operation(summary = "获取分销佣金提现配置")
    @GetMapping("/commissionWithdrawalSettings")
    public CommissionWithdrawalSettingsVO getCommissionWithdrawalSettings() {
        return configService.getCommissionWithdrawalSettings();
    }

    @Operation(summary = "保存分销佣金提现配置")
    @PostMapping("/saveCommissionWithdrawal")
    public void saveCommissionWithdrawal(@RequestBody @Validated CommissionWithdrawalSaveParam param) {
        configService.saveCommissionWithdrawal(param);
    }

    @Operation(summary = "获取门店配置")
    @GetMapping("/storeSettings")
    public GetStoreVO getStoreSettings() {
        return configService.getStoreSettings();
    }

    @Operation(summary = "保存门店配置")
    @PreAuthorize("@pms.hasPermission('storeSettingsUpdateManage')")
    @PostMapping("/saveStore")
    public void saveStore(@RequestBody @Validated StoreSaveParam param) {
        configService.saveStore(param);
    }

//    @Operation(summary = "获取web Amap 配置  类型为：web/webjs/mini")
//    @GetMapping("/amapSettings")
//    public AmapVO amapSettings(String type) {
//        return configService.getAmapSettings(type);
//    }
//
//    @Operation(summary = "保存web Amap 配置")
//    @PreAuthorize("@pms.hasPermission('storeSettingsUpdateManage')")
//    @PostMapping("/saveAmapSettings")
//    public void saveAmapSettings(@RequestBody @Validated AmapParam param) {
//        configService.saveAmapSettings(param);
//    }

    @Operation(summary = "获取web Amap 配置")
    @GetMapping("/amapAllSettings")
    public AmapAllVO amapAllSettings(@RequestParam(value = "use", required = false) Integer use) {
        return configService.getAllAmapSettings(use);
    }

    @Operation(summary = "保存web Amap 配置")
    @PreAuthorize("@pms.hasPermission('storeSettingsUpdateManage')")
    @PostMapping("/saveAllAmapSettings")
    public void saveAllAmapSettings(@RequestBody @Validated AmapAllParam param) {
        configService.saveAllAmapSettings(param);
    }

    @Operation(summary = "获取地图服务商配置")
    @GetMapping("/mapSettings")
    public MapSettingsVO mapSettings(@RequestParam(value = "use", required = false) Integer use) {
        return configService.getMapSettings(use);
    }

    @Operation(summary = "保存地图服务商配置")
    @PreAuthorize("@pms.hasPermission('storeSettingsUpdateManage')")
    @PostMapping("/saveMapSettings")
    public void saveMapSettings(@RequestBody @Validated MapSettingsParam param) {
        configService.saveMapSettings(param);
    }

    @Operation(summary = "保存个性化设置")
    @PostMapping("/savePersonalizedSetting")
    public void savePersonalizedSetting(@RequestBody @Validated PersonalizedSettingSaveParam param) {
        configService.savePersonalizedSetting(param);
    }

    @Operation(summary = "获取个性化设置")
    @GetMapping("/getPersonalizedSetting")
    public PersonalizedSettingVO getPersonalizedSetting() {
        return configService.getPersonalizedSetting();
    }


    @Operation(summary = "商品搜索配置")
    @GetMapping("/searchSettings")
    public SearchSettingsVO getSearchSettings() {
        return configService.getSearchSettings();
    }

    @Operation(summary = "保存商品搜索配置")
    @PostMapping("/saveSearch")
    public void saveSearch(@RequestBody @Validated SearchSaveParam param) {
        configService.saveSearch(param);
    }

    @Operation(summary = "获取平台门店配置")
    @GetMapping("/storeSelfOperatedSettings")
    public GetStoreSelfOperatedVO getStoreSelfOperatedSettings() {
        return configService.getStoreSelfOperatedSettings();
    }

    @Operation(summary = "保存平台门店配置")
    @PreAuthorize("@pms.hasPermission('storeSettingsUpdateManage')")
    @PostMapping("/saveStoreSelfOperated")
    public void saveStore(@RequestBody @Validated StoreSelfOperatedSaveParam param) {
        configService.saveStoreSelfOperated(param);
    }


    @Operation(summary = "保存钉钉配置")
    @PostMapping("/saveDingConfig")
    public void saveDingConfig(@RequestBody @Validated DingMessageConfigDTO dingMessageConfigDTO) {
        configService.saveOrUpdateDingConfig(dingMessageConfigDTO);
    }

    @Operation(summary = "保存企业微信配置")
    @PostMapping("/saveWXWorkConfig")
    public void saveWXWorkConfig(@RequestBody @Validated WXWorkMessageConfig dingMessageConfigDTO) {
        configService.saveOrUpdateWXWorkConfig(dingMessageConfigDTO);
    }

    @Operation(summary = "获取钉钉配置")
    @GetMapping("/getDingConfig")
    public DingMessagePushConfigVo getDingConfig() {
        return configService.getDingMessagePushConfig();
    }

    @Operation(summary = "获取企业微信配置")
    @GetMapping("/getWXWorkConfig")
    public WXWorkMessagePushConfigVo getWXWorkConfig() {
        return configService.getWxWorkMessagePushConfig();
    }

    @Operation(summary = "获取收银台订单设置")
    @GetMapping("/getCashierOrderSettings")
    public CashierOrderSettingsVO getCashierOrderSettings() {
        return configService.getCashierOrderSettings();
    }

    @Operation(summary = "保存收银台订单设置")
    @PostMapping("/saveCashierOrderSettings")
    public void saveCashierOrderSettings(@RequestBody @Validated CashierOrderSettingsSaveParam param) {
        configService.saveCashierOrderSettings(param);
    }




}
