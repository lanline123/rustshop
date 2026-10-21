// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting;

import com.tigshop.bean.dto.login.RegisterEmailDTO;
import com.tigshop.bean.dto.login.RegisterSmsDTO;
import com.tigshop.bean.dto.setting.ConfigCustomerDTO;
import com.tigshop.bean.dto.setting.DingMessageConfigDTO;
import com.tigshop.bean.vo.config.*;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.param.settings.config.*;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import com.tigshop.bean.vo.setting.config.*;
import com.tigshop.bean.vo.setting.SearchSettingsVO;
import com.tigshop.bean.dto.setting.SearchSaveParam;
import com.tigshop.bean.vo.user.WechatLoginUrlVO;
import com.tigshop.service.common.BaseService;

import java.util.List;
import java.util.Map;

/**
 * 设置配置服务
 *
 * @author kidd
 * @since 2025/4/1 10:40
 */
public interface ConfigService extends BaseService<ConfigPO> {

    /**
     * 获取基础配置
     */
    BasicSettingsVO getBasicSettings();

    /**
     * 保存基础配置
     */
    void saveBasic(BasicSaveParam param);

    /**
     * 获取商品配置
     */
    ProductSettingsVO getProductSettings();

    /**
     * 保存商品配置
     */
    void saveProduct(ProductSaveParam param);

    /**
     * 获取交易设置
     */
    ShoppingSettingsVO getShoppingSettings();

    /**
     * 保存交易设置
     */
    void saveShoppingSettings(ShoppingSaveParam param);

    /**
     * 获取订单配置项
     */
    OrderSettingsVO getOrderSettings();

    /**
     * 保存订单配置项
     */
    void saveOrderSettings(OrderSettingsSaveParam param);

    /**
     * 获取客服配置
     */
    KefuSettingsVO getKefuSettings();

    /**
     * 保存客服配置
     */
    void saveKefuSettings(KefuSaveParam param);

    /**
     * 获取物流配置
     */
    ShippingSettingsVO getShippingSettings();

    /**
     * 保存物流配置
     */
    void saveShippingSettings(ShippingSaveParam param);

    /**
     * 获取全局配置项
     */
    GlobalSettingsVO getGlobalSettings();

    /**
     * 保存全局配置项
     */
    GlobalSettingsVO saveGlobalSettings(GlobalSettingsSaveParam param);

    /**
     * 获取登录配置项
     */
    LoginSettingsVO getLoginSettings();

    /**
     * 保存登录配置项
     */
    void saveLoginSettings(LoginSettingsSaveParam param);

    /**
     * 获取基础支付配置项
     */
    BasicPaySettingsVO getBasicPaySettings();

    /**
     * 保存基础支付配置项
     */
    void saveBasicPaySettings(BasicPaySettingsSaveParam param);

    /**
     * 获取微信支付配置
     */
    WechatPaySettingsVO getWechatPaySettings();

    /**
     * 保存微信支付配置
     */
    void saveWechatPaySettings(WechatPaySettingsSaveParam param);

    /**
     * 获取支付宝配置项
     */
    AliPaySettingsVO getAliPaySettings();

    /**
     * 保存支付宝配置项
     */
    void saveAliPaySettings(AliPaySettingsSaveParam param);

    /**
     * 获取易宝支付配置项
     */
    YaBandPaySettingsVO getYaBandPaySettings();

    /**
     * 保存易宝支付配置项
     */
    void saveYaBandPaySettings(YaBandPaySettingsSaveParam param);

    /**
     * 获取线下支付配置项
     */
    OfflinePaySettingsVO getOfflinePaySettings();

    /**
     * 保存线下支付配置项
     */
    void saveOfflinePaySettings(OfflinePaySettingsSaveParam param);

    /**
     * 获取paypal配置项
     */
    PayPalSettingsVO getPayPalSettings();

    /**
     * 保存paypal配置项
     */
    void savePayPalSettings(PayPalSettingsSaveParam param);


    // Apple Pay 配置读写已拆至 ApplePayConfigService（VORT 门控 common.pay.applePay 专属，整文件可删）

    /**
     * 获取云支付配置
     */
    YunPaySettingsVO getYunPaySettings();

    /**
     * 保存云支付配置
     */
    void saveYunPaySettings(YunPaySettingsSaveParam param);

    // 汇付/拉卡拉配置读写已拆至 HuifuConfigService / LakalaConfigService（VORT 门控 common.pay.* 专属，整文件可删）

    /**
     * 获取邮箱配置
     */
    MailSettingsVO getMailSettings();

    /**
     * 保存邮箱配置
     */
    void saveMail(MailSaveParam param);

    /**
     * 获取通知配置
     */
    NotifySettingsVO getNotifySettings();

    /**
     * 保存通知配置
     */
    void saveNotify(NotifySaveParam param);

    /**
     * 获取会员认证配置
     */
    AuthSettingsVO getAuthSettings();

    /**
     * 保存会员认证配置
     */
    void saveAuth(AuthSaveParam param);

    /**
     * 获取售后配置
     */
    AfterSalesSettingsVO getAfterSalesSettings();

    /**
     * 保存售后配置
     */
    void saveAfterSales(AfterSaleSaveParam param);

    /**
     * 获取主题风格配置
     */
    ThemeStyleSettingsVO getThemeStyleSettings();

    /**
     * 保存主题风格配置
     */
    void saveThemeStyle(ThemeStyleSaveParam param);

    /**
     * 获取分类装饰配置
     */
    CategoryDecorateSettingsVO getCategoryDecorateSettings();

    /**
     * 保存分类装饰配置
     */
    void saveCategoryDecorate(CategoryDecorateSaveParam param);

    /**
     * 获取商品详情页风格配置
     */
    ProductDetailStyleSettingsVO getProductDetailStyleSettings();

    /**
     * 保存商品详情页风格配置
     */
    void saveProductDetailStyle(ProductDetailStyleSaveParam param);

    /**
     * 获取商品详情页风格（前台用，默认样式一）
     */
    Integer getProductDetailStyleType();

    /**
     * 获取搜索页风格配置
     */
    SearchStyleSettingsVO getSearchStyleSettings();

    /**
     * 保存搜索页风格配置
     */
    void saveSearchStyle(SearchStyleSaveParam param);

    /**
     * 获取搜索页风格（前台用，默认样式一）
     */
    Integer getSearchStyleType();

    /**
     * 获取移动端搜索风格配置
     */
    MobileSearchStyleSettingsVO getMobileSearchStyleSettings();

    /**
     * 保存移动端搜索风格配置
     */
    void saveMobileSearchStyle(MobileSearchStyleSaveParam param);

    /**
     * 获取移动端搜索风格（前台用，默认 1-普通商品搜索）
     */
    Integer getMobileSearchStyleType();

    /**
     * 获取个人中心页装修配置
     */
    MemberCenterDecorateSettingsVO getMemberCenterDecorateSettings();

    /**
     * 保存个人中心页装修配置
     */
    void saveMemberCenterDecorate(MemberCenterDecorateSaveParam param);

    /**
     * 获取店铺配置
     */
    ShopSettingsVO getShopSettings();

    /**
     * 保存店铺配置
     */
    void saveShop(ShopSaveParam param);

    /**
     * 获取买家秀配置
     */
    BuyerShowSettingsVO getBuyerShowSettings();

    /**
     * 保存买家秀配置
     */
    void saveBuyerShow(BuyerShowSaveParam param);

    /**
     * 获取商户配置
     */
    MerchantSettingsVO getMerchantSettings();

    /**
     * 保存商户配置
     */
    void saveMerchant(MerchantSaveParam param);


    /**
     * 获取配置项中的data
     */
    GetAdminVO getAdmin();

    /**
     * admin 初始化配置
     */
    GetAdminBaseVO getAdminBase();

    /**
     * 生成平台证书
     */
    void createPlatformCertificate();

    /**
     * 通过code查询单个配置项
     */
    ConfigPO getConfigByCode(String code);

    /**
     * 获取基础配置
     */
    ConfigBasicVO getConfigBasic();

    /**
     * 发送测试邮件
     */
    void sendTestMail(String testMailAddress);

    /**
     * 发送短信（仅用于验证码）
     *
     * @param dto 短信内容
     * @return boolean
     */
    boolean sendSms(RegisterSmsDTO dto);


    /**
     * 发送短信（仅用于验证码）
     *
     * @param dto 短信内容
     * @return boolean
     */
    String sendCode(RegisterSmsDTO dto);

    /**
     * 发送短信
     *
     * @param dto           短信内容
     * @param templateId    短信模板id
     * @param templateParam 短信模板参数
     * @return boolean
     */
    boolean sendSms(RegisterSmsDTO dto, String templateId, Map<String, Object> templateParam);

    /**
     * 发送短信
     *
     * @param dto           短信内容
     * @param templateId    短信模板id
     * @param templateParam 短信模板参数
     * @return boolean
     */
    boolean sendSms(RegisterSmsDTO dto, String templateId, String templateParam);


    // *** 外部 ***

    /**
     * 获取前台基础配置
     *
     * @param previewId 预览id
     * @return map
     */
    Map<String, Object> getBase(Integer previewId);

    /**
     * 获取配置项
     */
    String getConfigVal(SettingsEnum setting);

    /**
     * 分账总开关（三渠道级联父开关）：默认开，仅显式配置为 0 时关闭（零回归）。
     * 总关时微信/汇付/支付宝三渠道分账均不生效。
     */
    boolean isProfitSharingEnabled();

    /**
     * 获取快速登录设置
     *
     * @return map
     */
    Map<String, Object> getQuickLoginSetting();

    /**
     * 获取前台客户配置
     *
     * @return ConfigCustomerDTO
     */
    ConfigCustomerDTO getConfigCustomer();

    Map<String, String> getConfigMapBySettings(List<SettingsEnum> settings);

    /**
     * 获得微信授权url
     */
    WechatLoginUrlVO getWechatLoginUrl(String redirectUrl);

    /**
     * 获取初始化配置项
     */
    InitConfigSettingsVO getInitConfigSettings(Integer previewId);

    /**
     * 获取主题配置项
     */
    ThemeSettingsVO getThemeSettings();

    /**
     * 获取图标icon
     */
    IconSettingsVO getIcon();

    /**
     * 获取配置项
     */
    <T> T getSettings(Class<T> clazz);

    /**
     * 新增配置项
     */
    void saveConfigSettings(ConfigSaveParam param);

    /**
     * 同步货币符号 / 中文配置（跨境主货币联动）
     *
     * @param symbol 货币符号，写入 dollarSign；空白则跳过
     * @param name   货币中文名称，写入 dollarSignCn；空白则跳过
     */
    void syncCurrencyConfig(String symbol, String name);

    /**
     * 获取货币符号（dollarSign），空白时回退默认 ¥
     */
    String getCurrencySymbol();

    /**
     * 获取货币中文名称（dollarSignCn），空白时回退默认 元
     */
    String getCurrencyName();


    /**
     * 发送邮箱验证码
     *
     * @param dto 参数
     */
    void sendEmail(RegisterEmailDTO dto);

    /**
     * 获取供应商配置项
     */
    VendorSettingsVO getVendorSettings();

    /**
     * 保存供应商配置项
     */
    void saveVendor(VendorSaveParam param);

    /**
     * 获取登录协议
     */
    GetLoginProtocolVO getLoginProtocol();

    /**
     * 保存登录协议
     */
    void saveLoginProtocol(LoginProtocolParam param);

    /**
     * 获取登录协议内容
     */
    GetLoginProtocolContentVO getLoginProtocolContentVO(String configCode);

    /**
     * 获取结算与分账配置
     * @return ProfitSharingSettingsVO
     */
    ProfitSharingSettingsVO getProfitSharingSettings();

    /**
     * 保存结算与分账配置
     * @param param ProfitSharingSaveParam
     */
    void saveProfitSharing(ProfitSharingSaveParam param);

    /**
     * 获取提现配置项
     * @return WithdrawalSettingsVO
     */
    WithdrawalSettingsVO getWithdrawalSettings();

    /**
     * 保存提现配置项
     * @param param WithdrawalSaveParam
     */
    void saveWithdrawal(WithdrawalSaveParam param);

    /**
     * 获取会员余额提现配置（与分账/佣金提现独立，D1107）
     * @return MemberWithdrawalSettingsVO
     */
    MemberWithdrawalSettingsVO getMemberWithdrawalSettings();

    /**
     * 保存会员余额提现配置（与分账/佣金提现独立，D1107）
     * @param param MemberWithdrawalSaveParam
     */
    void saveMemberWithdrawal(MemberWithdrawalSaveParam param);

    /**
     * 获取分销佣金提现配置（独立于会员余额提现，D1107）
     * @return CommissionWithdrawalSettingsVO
     */
    CommissionWithdrawalSettingsVO getCommissionWithdrawalSettings();

    /**
     * 保存分销佣金提现配置（独立于会员余额提现，D1107）
     * @param param CommissionWithdrawalSaveParam
     */
    void saveCommissionWithdrawal(CommissionWithdrawalSaveParam param);

    /**
     * 获取门店配置项
     *
     * @return
     */
    GetStoreVO getStoreSettings();

    /**
     * 保存门店配置项
     *
     * @param param
     */
    void saveStore(StoreSaveParam param);

    /**
     * 获取高德地图配置项
     *
     * @return
     */
    AmapVO getAmapSettings(String type);

    /**
     * 保存高德地图配置项
     *
     * @param param
     */
    void saveAmapSettings(AmapParam param);

    AmapAllVO getAllAmapSettings(Integer use);

    void saveAllAmapSettings(AmapAllParam param);

    /**
     * 获取地图服务商设置（服务商选择 + 高德 + 天地图）
     *
     * @param use 1-取真值供服务端使用，其余-凭据掩码后返回给前端
     */
    MapSettingsVO getMapSettings(Integer use);

    /**
     * 保存地图服务商设置
     */
    void saveMapSettings(MapSettingsParam param);

    GetStoreSelfOperatedVO getStoreSelfOperatedSettings();

    void saveStoreSelfOperated(StoreSelfOperatedSaveParam param);



    /**
     * 保存个性化配置项
     *
     */
    void savePersonalizedSetting(PersonalizedSettingSaveParam param);

    /**
     * 获取个性化配置项
     *
     */
    PersonalizedSettingVO getPersonalizedSetting();

    /**
     * 获取搜索配置项
     * @return SearchSettingsVO
     */
    SearchSettingsVO getSearchSettings();

    /**
     * 获取搜索配置项
     * @param param SearchSaveParam
     */
    void saveSearch(SearchSaveParam param);

    /**
     * 检查ES连接状态
     * @return Map<String, Object>
     */
    Map<String, Object> checkEsConnection();

    /**
     * 获取配置值
     * @param configCode 配置代码
     * @param key 配置键
     * @return 配置值
     */
    String getConfigValue(String configCode, String key);



    void saveOrUpdateDingConfig(DingMessageConfigDTO dingMessageConfigDTO);

    void saveOrUpdateWXWorkConfig(WXWorkMessageConfig dingMessageConfigDTO);

    DingMessagePushConfigVo getDingMessagePushConfig();

    WXWorkMessagePushConfigVo getWxWorkMessagePushConfig();

    /**
     * 获取收银台订单配置项
     * @return CashierOrderSettingsVO
     */
    CashierOrderSettingsVO getCashierOrderSettings();

    /**
     * 保存收银台订单配置项
     * @param param CashierOrderSettingsSaveParam
     */
    void saveCashierOrderSettings(CashierOrderSettingsSaveParam param);


    String getStorageUrl();

    /**
     * 获取小程序风控配置（与直播解耦，始终可用）
     */
    MiniProgramRiskSettingsVO getMiniProgramRiskSettings();

    /**
     * 保存小程序风控配置（与直播解耦，始终可用）
     */
    void saveMiniProgramRiskSettings(MiniProgramRiskSettingsSaveParam param);

    /**
     * 获取小程序直播/视频号独立小程序配置
     */
    WechatLiveSettingsVO getWechatLiveSettings();

    /**
     * 保存小程序直播/视频号独立小程序配置
     */
    void saveWechatLiveSettings(WechatLiveSettingsSaveParam param);
}
