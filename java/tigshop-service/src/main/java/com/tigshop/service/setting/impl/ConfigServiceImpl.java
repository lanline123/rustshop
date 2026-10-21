// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.microsoft.schemas.office.visio.x2012.main.PageType;
import com.tigshop.bean.annotations.settings.ConfigIgnoreField;
import com.tigshop.bean.annotations.settings.ConfigInnerParam;
import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.dto.login.RegisterEmailDTO;
import com.tigshop.bean.dto.login.RegisterSmsDTO;
import com.tigshop.bean.dto.setting.*;
import com.tigshop.bean.dto.setting.SearchSaveParam;
import com.tigshop.bean.dto.setting.ConfigCustomerDTO;
import com.tigshop.bean.dto.setting.ConfigStorageDTO;
import com.tigshop.bean.dto.setting.SmsSendDTO;
import com.tigshop.bean.dto.shop.StoreExtendedDto;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.common.StorageType;
import com.tigshop.bean.enums.decorate.DecorateType;
import com.tigshop.bean.enums.finance.CreditOrderSettlementStatusEnum;
import com.tigshop.bean.enums.msg.MessageTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.setting.SmsProviderEnum;
import com.tigshop.service.sms.SmsChannelHelper;
import com.tigshop.bean.feign.wechat.CreateQrcodeResult;
import com.tigshop.bean.feign.wechat.GetTokenResult;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.decorate.Decorate;
import com.tigshop.bean.model.finance.CreditOrder;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.setting.MessageTemplate;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.model.user.UserRankConfig;
import com.tigshop.bean.param.settings.config.*;
import com.tigshop.bean.param.settings.config.base.ConfigSaveParam;
import com.tigshop.bean.param.user.CreateQrcodeParam;
import com.tigshop.bean.vo.config.*;
import com.tigshop.bean.vo.setting.SearchSettingsVO;
import com.tigshop.bean.vo.setting.config.*;
import com.tigshop.bean.vo.user.WechatLoginUrlVO;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.constant.setting.ConfigConstants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.service.oauth.WechatOAuthService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import com.tigshop.helper.SettingsHelper;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.setting.ConfigMapper;
import com.tigshop.mapper.setting.RegionMapper;
import com.tigshop.mapper.user.UserRankConfigMapper;
import com.tigshop.security.common.handler.CaptchaHandler;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.decorate.DecorateService;
import com.tigshop.bean.vo.deposit.DepositSettingVO;
import com.tigshop.service.deposit.DepositSettingService;
import com.tigshop.service.finance.CreditOrderService;
import com.tigshop.service.product.ProductEsService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.AfterSalesReasonConfigService;
import com.tigshop.service.setting.MailTemplatesService;
import com.tigshop.service.setting.MessageTemplateService;
import com.tigshop.mapper.shop.ShopMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import com.tigshop.service.setting.ConfigValCache;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.tigshop.bean.enums.setting.MessageTemplateType.MSG_CODE;
import static com.tigshop.bean.enums.setting.MessageTypeEnum.MESSAGE_TYPE_MSG;
import static com.tigshop.bean.enums.setting.SettingsEnum.*;
import static com.tigshop.bean.enums.setting.SettingsEnum.AFTER_SALES_SERVICE;
import static com.tigshop.bean.enums.setting.SettingsEnum.DEFAULT_COMPANY;
import static com.tigshop.bean.enums.setting.SettingsEnum.SHOP_DESC;
import static com.tigshop.bean.enums.setting.SettingsEnum.SHOP_ICP_NO;
import static com.tigshop.bean.enums.setting.SettingsEnum.SHOP_KEYWORDS;
import static com.tigshop.bean.enums.setting.SettingsEnum.SHOP_NAME;
import static com.tigshop.bean.enums.setting.SettingsEnum.THEME_STYLE;
import static com.tigshop.common.constant.Constants.*;
import static com.tigshop.common.constant.ExceptionConstants.CODE_PARAM_ERROR;
import static com.tigshop.common.constant.ExceptionConstants.PARAM_ERROR;
import static com.tigshop.common.constant.setting.ConfigConstants.*;
import static com.tigshop.common.enums.DataType.SETTING;

/**
 * 设置配置服务实现
 *
 * @author kidd
 * @since 2025/4/1 10:40
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ConfigServiceImpl extends BaseServiceImpl<ConfigMapper, ConfigPO> implements ConfigService {

    private final MessageTemplateService messageTemplateService;

    private final CaptchaHandler captchaHandler;

    private final SettingsHelper settingsHelper;

    private final AfterSalesReasonConfigService afterSalesReasonConfigService;

    private final RedisTemplate<String, String> redisTemplate;

    private final RegionMapper regionMapper;

    private final DecorateService decorateService;

    private final RabbitTemplate rabbitTemplate;

    private final UserRankConfigMapper userRankConfigMapper;

    private final TranslatePackageImpl translatePackage;
    private final MessageHelper messageHelper;

    private final WechatApiClient wechatApiClient;
    @Lazy
    @Resource
    private WechatOAuthService wechatOAuthService;

    private final AdminUserMapper adminUserMapper;

    private final MailTemplatesService mailTemplatesService;

    private final TigshopProperties tigshopProperties;

    private final ProductMapper productMapper;

    private final ProductEsService productEsService;

    @Value("${icoTigCss}")
    private String icoTigCss;


    // 保证金设置服务：与本类互相依赖，@Lazy 注入打破启动期循环依赖
    @org.springframework.context.annotation.Lazy
    @org.springframework.beans.factory.annotation.Autowired
    private DepositSettingService depositSettingService;

    private final ApplicationContext applicationContext;

    private final ShopMapper shopMapper;

    private final ConfigValCache configValCache;

    private final CreditOrderService creditOrderService;

    @Override
    public BasicSettingsVO getBasicSettings() {
        return getSettings(BasicSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveBasic(BasicSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public ProductSettingsVO getProductSettings() {
        return getSettings(ProductSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveProduct(ProductSaveParam param) {
        if (param.getCustomProductNameLength() != null) {
            int maxLength = 750;
            if (param.getCustomProductNameLength() > maxLength) {
                throw new GlobalException("自定义商品名称长度不能超过最大名称长度" + maxLength);
            }
        }
        this.saveConfigSettings(param);
    }

    @Override
    public ShoppingSettingsVO getShoppingSettings() {
        return getSettings(ShoppingSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveShoppingSettings(ShoppingSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public OrderSettingsVO getOrderSettings() {
        OrderSettingsVO settings = getSettings(OrderSettingsVO.class);
        AfterSalesReasonConfigService.ReasonConfig reasonConfig = afterSalesReasonConfigService.getConfig();
        settings.setAfterSalesReasonEnabled(reasonConfig.enabled());
        settings.setAfterSalesReasonList(reasonConfig.reasons());
        return settings;
    }

    @Transactional
    @Override
    public void saveOrderSettings(OrderSettingsSaveParam param) {
        afterSalesReasonConfigService.normalizeForSave(param);
        this.saveConfigSettings(param);
    }

    @Override
    public KefuSettingsVO getKefuSettings() {
        KefuSettingsVO settings = this.getSettings(KefuSettingsVO.class);

        // 数据加密
        settings.encryptData();

        return settings;
    }

    @Transactional
    @Override
    public void saveKefuSettings(KefuSaveParam param) {
        KefuSettingsVO settings = this.getKefuSettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    @Override
    public ShippingSettingsVO getShippingSettings() {
        ShippingSettingsVO settings = this.getSettings(ShippingSettingsVO.class);

        // 数据加密
        settings.encryptData();

        return settings;
    }

    @Transactional
    @Override
    public void saveShippingSettings(ShippingSaveParam param) {
        ShippingSettingsVO settings = this.getShippingSettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    @Override
    public GlobalSettingsVO getGlobalSettings() {
        GlobalSettingsVO settings = getSettings(GlobalSettingsVO.class);

        // 数据加密
        settings.encryptData();

        // 处理商城默认区域 [ 110000, 110100, 110102 ]
        settings.setShopDefaultRegions(settings.getShopDefaultRegions());

        // 查询国家
        List<Region> countries = regionMapper.selectList(new LambdaQueryWrapper<Region>().eq(Region::getParentId, 0));
        settings.setCountries(countries);

        // 处理登录账户是商户的情况，配置返回默认值
        Integer adminId = SecurityUtils.getCurrentAdminId();
        Long shopCount = adminUserMapper.selectCount(Wrappers.lambdaQuery(AdminUser.class).eq(AdminUser::getAdminId, adminId).eq(AdminUser::getAdminType, AdminTypeEnum.SHOP.getCode()));
        if (shopCount > 0) {
            settings.setNavTheme("dark");
            settings.setLayout("topMenu");
        }

        return settings;
    }

    @Transactional
    @Override
    public GlobalSettingsVO saveGlobalSettings(GlobalSettingsSaveParam param) {
        GlobalSettingsVO settings = this.getGlobalSettings();

        param.noUpdate(settings);

        if (param.getWorkbuddyApiKeyExpiresAt() != null
                && param.getWorkbuddyApiKeyExpiresAt() <= System.currentTimeMillis() / 1000) {
            throw new GlobalException("WorkBuddy 密钥有效期必须晚于当前时间", 400);
        }

        this.saveConfigSettings(param);
        return this.getGlobalSettings();
    }

    @Override
    public LoginSettingsVO getLoginSettings() {
        LoginSettingsVO settings = getSettings(LoginSettingsVO.class);

        // 数据加密
        settings.encryptData();

        return settings;
    }

    @Transactional
    @Override
    public void saveLoginSettings(LoginSettingsSaveParam param) {
        LoginSettingsVO settings = this.getLoginSettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    @Override
    public BasicPaySettingsVO getBasicPaySettings() {
        BasicPaySettingsVO settings = this.getSettings(BasicPaySettingsVO.class);

        // 获取各支付方式的开启状态
        WechatPaySettingsVO wechatSettings = this.getSettings(WechatPaySettingsVO.class);
        AliPaySettingsVO aliSettings = this.getSettings(AliPaySettingsVO.class);
        PayPalSettingsVO paypalSettings = this.getSettings(PayPalSettingsVO.class);
        OfflinePaySettingsVO offlineSettings = this.getSettings(OfflinePaySettingsVO.class);
        YaBandPaySettingsVO yabandSettings = this.getSettings(YaBandPaySettingsVO.class);
        YunPaySettingsVO yunpaySettings = this.getSettings(YunPaySettingsVO.class);

        // 获取所有已开启的支付方式
        List<String> allEnabledMethods = new ArrayList<>();
        if (wechatSettings != null && Integer.valueOf(1).equals(wechatSettings.getUseWechat())) {
            allEnabledMethods.add("wechat");
        }
        if (aliSettings != null && Integer.valueOf(1).equals(aliSettings.getUseAlipay())) {
            allEnabledMethods.add("alipay");
        }
        if (paypalSettings != null && Integer.valueOf(1).equals(paypalSettings.getUsePaypal())) {
            allEnabledMethods.add("paypal");
        }
//        if (offlineSettings != null && Integer.valueOf(1).equals(offlineSettings.getUseOffline())) {
//            allEnabledMethods.add("offline");
//        }
        if (yabandSettings != null && Integer.valueOf(1).equals(yabandSettings.getUseYabanpay())) {
            allEnabledMethods.add("yabanpay");
        }
        if (yunpaySettings != null && Integer.valueOf(1).equals(yunpaySettings.getUseYunpay())) {
            allEnabledMethods.add("yunpay");
        }
        // Apple Pay（全版本可见，收单通道待选型）
        String useApplePayVal = this.getConfigVal(SettingsEnum.USE_APPLE_PAY);
        if (StrUtil.isNotBlank(useApplePayVal) && Integer.parseInt(useApplePayVal) == 1) {
            allEnabledMethods.add("applepay");
        }
        // 汇付斗拱（开启后微信/支付宝均可用）
        if ("1".equals(this.getConfigVal(SettingsEnum.USE_HUIFU))) {
            allEnabledMethods.add("huifuWechat");
            allEnabledMethods.add("huifuAlipay");
        }
        // 拉卡拉（开启后微信/支付宝均可用）
        if ("1".equals(this.getConfigVal(SettingsEnum.USE_LAKALA))) {
            allEnabledMethods.add("lakalaWechat");
            allEnabledMethods.add("lakalaAlipay");
        }
        // 易宝（开启后微信/支付宝均可用）
        if ("1".equals(this.getConfigVal(SettingsEnum.USE_YEEPAY))) {
            allEnabledMethods.add("yeepayWechat");
            allEnabledMethods.add("yeepayAlipay");
        }

        // 根据已开启的支付方式过滤paymentOrder
        String paymentOrder = settings.getPaymentOrder();
        if (StrUtil.isNotBlank(paymentOrder)) {
            // 过滤未开启的支付方式，保留已开启的
            List<String> existingMethods = Arrays.stream(paymentOrder.split(","))
                    .map(String::trim)
                    .filter(method -> isPaymentEnabled(method, wechatSettings, aliSettings, paypalSettings,
                            offlineSettings, yabandSettings, yunpaySettings))
                    .collect(Collectors.toList());
            
            // 将新开启但不在paymentOrder中的支付方式追加到末尾
            for (String method : allEnabledMethods) {
                if (!existingMethods.contains(method)) {
                    existingMethods.add(method);
                }
            }
            settings.setPaymentOrder(String.join(",", existingMethods));
        } else {
            // paymentOrder为空时，自动拼接所有已开启的支付方式
            settings.setPaymentOrder(String.join(",", allEnabledMethods));
        }


        return settings;
    }

    /**
     * 判断支付方式是否开启
     */
    private boolean isPaymentEnabled(String method, WechatPaySettingsVO wechat, AliPaySettingsVO ali,
                                     PayPalSettingsVO paypal,
                                     OfflinePaySettingsVO offline, YaBandPaySettingsVO yaband,
                                     YunPaySettingsVO yunpay) {
        return switch (method.toLowerCase()) {
            case "wechat" -> wechat != null && Integer.valueOf(1).equals(wechat.getUseWechat());
            case "alipay" -> ali != null && Integer.valueOf(1).equals(ali.getUseAlipay());
            case "paypal" -> paypal != null && Integer.valueOf(1).equals(paypal.getUsePaypal());
            case "offline" -> offline != null && Integer.valueOf(1).equals(offline.getUseOffline());
            case "yabanpay" -> yaband != null && Integer.valueOf(1).equals(yaband.getUseYabanpay());
            case "yunpay" -> yunpay != null && Integer.valueOf(1).equals(yunpay.getUseYunpay());
            case "applepay" -> "1".equals(this.getConfigVal(SettingsEnum.USE_APPLE_PAY));
            case "huifuwechat", "huifualipay" -> "1".equals(this.getConfigVal(SettingsEnum.USE_HUIFU));
            case "lakalawechat", "lakalaalipay" -> "1".equals(this.getConfigVal(SettingsEnum.USE_LAKALA));
            case "yeepaywechat", "yeepayalipay" -> "1".equals(this.getConfigVal(SettingsEnum.USE_YEEPAY));
            default -> false;
        };
    }

    @Transactional
    @Override
    public void saveBasicPaySettings(BasicPaySettingsSaveParam param) {
        validateCloseCreditPay(param);
        this.saveConfigSettings(param);
    }

    private void validateCloseCreditPay(BasicPaySettingsSaveParam param) {
        if (!Integer.valueOf(0).equals(param.getUseCredit())) {
            return;
        }
        Long activeCreditOrderCount = creditOrderService.lambdaQuery()
                .in(CreditOrder::getSettlementStatus, List.of(
                        CreditOrderSettlementStatusEnum.UNSETTLED.getCode(),
                        CreditOrderSettlementStatusEnum.REFUNDING.getCode()
                ))
                .count();
        if (activeCreditOrderCount != null && activeCreditOrderCount > 0) {
            throw new GlobalException(messageHelper.get("error.credit.close.has.pending.order"));
        }
    }

    @Override
    public WechatPaySettingsVO getWechatPaySettings() {
        WechatPaySettingsVO settings = this.getSettings(WechatPaySettingsVO.class);

        // 数据加密
        settings.encryptData();

        return settings;
    }

    @Transactional
    @Override
    public void saveWechatPaySettings(WechatPaySettingsSaveParam param) {
        WechatPaySettingsVO settings = this.getWechatPaySettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    @Override
    public AliPaySettingsVO getAliPaySettings() {
        AliPaySettingsVO settings = this.getSettings(AliPaySettingsVO.class);

        // 数据加密
        settings.encryptData();

        return settings;
    }

    @Transactional
    @Override
    public void saveAliPaySettings(AliPaySettingsSaveParam param) {
        AliPaySettingsVO settings = this.getAliPaySettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    @Override
    public YaBandPaySettingsVO getYaBandPaySettings() {
        YaBandPaySettingsVO settings = this.getSettings(YaBandPaySettingsVO.class);

        // 数据加密
        settings.encryptData();

        return settings;
    }

    @Transactional
    @Override
    public void saveYaBandPaySettings(YaBandPaySettingsSaveParam param) {
        YaBandPaySettingsVO settings = this.getYaBandPaySettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    @Override
    public OfflinePaySettingsVO getOfflinePaySettings() {
        return this.getSettings(OfflinePaySettingsVO.class);
    }

    @Transactional
    @Override
    public void saveOfflinePaySettings(OfflinePaySettingsSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public PayPalSettingsVO getPayPalSettings() {
        PayPalSettingsVO settings = this.getSettings(PayPalSettingsVO.class);

        // 数据加密
        settings.encryptData();

        settings.setPaypalCurrencyList(JSON.parseArray(settings.getPaypalCurrencyListStr()));

        return settings;
    }

    @Transactional
    @Override
    public void savePayPalSettings(PayPalSettingsSaveParam param) {
        PayPalSettingsVO settings = this.getPayPalSettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }


    // Apple Pay 配置读写已拆至 ApplePayConfigService（VORT 门控 common.pay.applePay 专属，整文件可删）



    @Override
    public YunPaySettingsVO getYunPaySettings() {
        YunPaySettingsVO settings = this.getSettings(YunPaySettingsVO.class);

        // 数据加密
        settings.encryptData();

        return settings;
    }

    @Transactional
    @Override
    public void saveYunPaySettings(YunPaySettingsSaveParam param) {
        YunPaySettingsVO settings = this.getYunPaySettings();

        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    // 汇付/拉卡拉配置读写已拆至 HuifuConfigService / LakalaConfigService（VORT 门控 common.pay.* 专属，整文件可删）

    @Override
    public MailSettingsVO getMailSettings() {
        return this.getSettings(MailSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveMail(MailSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public NotifySettingsVO getNotifySettings() {
        NotifySettingsVO settings = this.getSettings(NotifySettingsVO.class);

        // 数据加密
        settings.encryptData();

        return settings;
    }

    @Transactional
    @Override
    public void saveNotify(NotifySaveParam param) {
        validateSmsProviderConfig(param);

        NotifySettingsVO settings = this.getNotifySettings();
        param.noUpdate(settings);

        this.saveConfigSettings(param);
    }

    private void validateSmsProviderConfig(NotifySaveParam param) {
        if (param.getSmsProvider() == null) {
            return;
        }
        SmsProviderEnum provider = SmsProviderEnum.fromCode(param.getSmsProvider());
        switch (provider) {
            case ALIYUN -> {
                requireNotBlank(param.getSmsKeyId(), "短信帐户用户名");
                requireNotBlank(param.getSmsKeySecret(), "短信帐户密码");
                requireNotBlank(param.getSmsSignName(), "短信签名");
                // 通道开关（仅跨境版提交该字段）：提交时至少勾选一个通道
                if (param.getSmsAliyunChannel() != null && param.getSmsAliyunChannel().isBlank()) {
                    throw new GlobalException("请至少选择一个服务商通道");
                }
            }
            case TENCENT -> {
                requireNotBlank(param.getSmsTencentSecretId(), "腾讯云SecretId");
                requireNotBlank(param.getSmsTencentSecretKey(), "腾讯云SecretKey");
                requireNotBlank(param.getSmsTencentSdkAppId(), "腾讯云SDK AppID");
                requireNotBlank(param.getSmsTencentSignName(), "腾讯云短信签名");
            }
            case HUAWEI -> {
                requireNotBlank(param.getSmsHuaweiAppKey(), "华为云APP Key");
                requireNotBlank(param.getSmsHuaweiAppSecret(), "华为云APP Secret");
                requireNotBlank(param.getSmsHuaweiChannel(), "华为云短信通道号");
                requireNotBlank(param.getSmsHuaweiSignName(), "华为云短信签名");
                requireNotBlank(param.getSmsHuaweiEndpoint(), "华为云接入地址");
            }
            case VOLCENGINE -> {
                requireNotBlank(param.getSmsVolcengineAccessKey(), "火山引擎AccessKey ID");
                requireNotBlank(param.getSmsVolcengineSecretKey(), "火山引擎SecretAccessKey");
                requireNotBlank(param.getSmsVolcengineSmsAccount(), "火山引擎短信应用ID");
                requireNotBlank(param.getSmsVolcengineSignName(), "火山引擎短信签名");
            }
        }
    }

    private void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new GlobalException(fieldName + "不能为空");
        }
    }

    @Override
    public AuthSettingsVO getAuthSettings() {
        return this.getSettings(AuthSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveAuth(AuthSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public AfterSalesSettingsVO getAfterSalesSettings() {
        return getSettings(AfterSalesSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveAfterSales(AfterSaleSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public ThemeStyleSettingsVO getThemeStyleSettings() {
        return this.getSettings(ThemeStyleSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveThemeStyle(ThemeStyleSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public CategoryDecorateSettingsVO getCategoryDecorateSettings() {
        return this.getSettings(CategoryDecorateSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveCategoryDecorate(CategoryDecorateSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public ProductDetailStyleSettingsVO getProductDetailStyleSettings() {
        return this.getSettings(ProductDetailStyleSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveProductDetailStyle(ProductDetailStyleSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public Integer getProductDetailStyleType() {
        String val = this.getConfigVal(SettingsEnum.PRODUCT_DETAIL_STYLE_TYPE);
        if (StrUtil.isBlank(val)) {
            return 1;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    @Override
    public SearchStyleSettingsVO getSearchStyleSettings() {
        return this.getSettings(SearchStyleSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveSearchStyle(SearchStyleSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public Integer getSearchStyleType() {
        String val = this.getConfigVal(SettingsEnum.SEARCH_STYLE_TYPE);
        if (StrUtil.isBlank(val)) {
            return 1;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    @Override
    public MobileSearchStyleSettingsVO getMobileSearchStyleSettings() {
        return this.getSettings(MobileSearchStyleSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveMobileSearchStyle(MobileSearchStyleSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public Integer getMobileSearchStyleType() {
        String val = this.getConfigVal(SettingsEnum.MOBILE_SEARCH_STYLE_TYPE);
        if (StrUtil.isBlank(val)) {
            return 1;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    @Override
    public MemberCenterDecorateSettingsVO getMemberCenterDecorateSettings() {
        return this.getSettings(MemberCenterDecorateSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveMemberCenterDecorate(MemberCenterDecorateSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public ShopSettingsVO getShopSettings() {
        return this.getSettings(ShopSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveShop(ShopSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public BuyerShowSettingsVO getBuyerShowSettings() {
        return this.getSettings(BuyerShowSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveBuyerShow(BuyerShowSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public MerchantSettingsVO getMerchantSettings() {
        return this.getSettings(MerchantSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveMerchant(MerchantSaveParam param) {
        this.saveConfigSettings(param);
    }


    @Override
    public GetAdminVO getAdmin() {
        GetAdminVO settings = this.getSettings(GetAdminVO.class);

        // 判断是否设置了商城LOGO，如果设置了需要加上图片访问域名
        boolean hasUrl = StrUtil.startWith(settings.getShopLogo(), "https://") || StrUtil.startWith(settings.getShopLogo(), "http://");
        if (StringUtils.isNotBlank(settings.getShopLogo()) && !hasUrl) {
            String storageUrl = getStorageUrl();
            settings.setShopLogo(storageUrl + settings.getShopLogo());
        }

        return settings;
    }

    @Override
    public GetAdminBaseVO getAdminBase() {
        GetAdminBaseVO settings = this.getSettings(GetAdminBaseVO.class);

        String storageUrl = this.getStorageUrl();
        settings.setStorageUrl(storageUrl);

        settings.setVersionType(tigshopProperties.getVersion());
        settings.setVersion(tigshopProperties.getVersionNum());

        // 处理登录账户是商户的情况，配置返回默认值
        Integer shopId = HeaderUtils.getShopId();
        if (shopId != null && shopId > 0) {
            settings.setLayout("topMenu");
        }
        Integer vendorId = HeaderUtils.getVendorId();
        if (vendorId != null && vendorId > 0) {
            settings.setLayout("topMenu");
        }
        if (List.of(AdminTypeEnum.STORE.getCode(), AdminTypeEnum.PICKUP.getCode()).contains(HeaderUtils.getAdminType())) {
            settings.setLayout("topMenu");
            settings.setPrimaryColor("blue");
        }
        if (Objects.equals(AdminTypeEnum.POS.getCode(), HeaderUtils.getAdminType())) {
            settings.setLayout("topMenu");
        }

        // 获取店铺自提配置（非 O2O 构建无自提 Bean，固定为关闭）
        settings.setIsOpenShopPickup(NO);

        // 店铺保证金开关（非保证金构建无此 Bean，固定为关闭）
        settings.setIsOpenDeposit(NO);
        DepositSettingVO depositSetting = depositSettingService.detail();
        settings.setIsOpenDeposit(depositSetting != null && depositSetting.getDepositEnable() != null
                ? depositSetting.getDepositEnable() : NO);

        return settings;
    }

    @Override
    public <T> T getSettings(Class<T> clazz) {
        return instantiateAndPopulate(clazz);
    }

    private <T> T instantiateAndPopulate(Class<T> clazz) {
        T instance = createInstance(clazz);

        ReflectionUtils.doWithFields(clazz, field -> {
            ReflectionUtils.makeAccessible(field);

            // 内嵌处理嵌套配置
            if (field.isAnnotationPresent(ConfigInnerParam.class)) {
                Object innerValue = instantiateAndPopulate(field.getType());
                setFieldValue(instance, field, innerValue, "无法设置内部参数");
                return;
            }

            // 处理普通配置项
            ConfigItemField annotation = field.getAnnotation(ConfigItemField.class);
            if (annotation != null) {
                Object value = settingsHelper.get(annotation.value());
                setFieldValue(instance, field, value, "设置字段值失败");
            }
        });

        return instance;
    }

    private <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new GlobalException("无法创建对象实例: " + clazz.getSimpleName());
        }
    }

    private void setFieldValue(Object target, Field field, Object value, String errorMsg) {
        try {
            ReflectionUtils.setField(field, target, value);
        } catch (Exception e) {
            throw new GlobalException(field.getName() + "，" + errorMsg);
        }
    }

    @Transactional
    @Override
    public void saveConfigSettings(ConfigSaveParam param) {
        List<ConfigPO> configs = extractConfigs(param);

        configs.stream().filter(config -> config.getBizVal() != null).forEach(settingsHelper::set);
        List<String> changedBizCodes = configs.stream()
                .map(ConfigPO::getBizCode)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        configValCache.invalidateAndBroadcast(changedBizCodes);
    }

    @Transactional
    @Override
    public void syncCurrencyConfig(String symbol, String name) {
        if (StrUtil.isNotBlank(symbol)) {
            settingsHelper.set(DOLLAR_SIGN, symbol);
        }
        if (StrUtil.isNotBlank(name)) {
            settingsHelper.set(DOLLAR_SIGN_CN, name);
        }
    }

    @Override
    public String getCurrencySymbol() {
        String val = this.getConfigVal(DOLLAR_SIGN);
        return StrUtil.isBlank(val) ? "¥" : val;
    }

    @Override
    public String getCurrencyName() {
        String val = this.getConfigVal(DOLLAR_SIGN_CN);
        return StrUtil.isBlank(val) ? "元" : val;
    }

    @Override
    public void sendEmail(RegisterEmailDTO dto) {
        if (!Validator.isEmail(dto.getEmail())) {
            throw new GlobalException("邮箱地址不正确");
        }
        // 验证验证码是否正确
        if (!captchaHandler.validateCaptcha(dto.getVerifyToken())) {
            // 如果验证码错误，抛出异常
            throw new GlobalException("获取验证码失败，需要先进行行为验证");
        }
        // 随机生成验证码
        int code = RandomUtil.randomInt(100000, 999999);
        // 初始化短信模板
        /*MailTemplates mailTemplates = mailTemplatesService.getOne(new LambdaQueryWrapper<MailTemplates>()
                .eq(MailTemplates::getTemplateCode, MailTemplateType.REGISTER_CODE.getCode()));

        String templateContent = mailTemplates.getTemplateContent();*/

        MessageTemplate messageTemplate = messageTemplateService.getOne(Wrappers.lambdaQuery(MessageTemplate.class).eq(MessageTemplate::getMessageId, MessageTypeEnum.VERIFICATION_CODE.getId()).eq(MessageTemplate::getType, 7).last("limit 1"));
        // 发送短信
        MailUtil.send(loadEmailConfig(), dto.getEmail(), messageTemplate.getTemplateName(), messageTemplate.getContent().replace("${code}", String.valueOf(code)), true);

        //存入redis
        redisTemplate.opsForValue().set(dto.getEvent() + dto.getEmail(), String.valueOf(code), 5, TimeUnit.MINUTES);
    }

    @Override
    public VendorSettingsVO getVendorSettings() {
        return this.getSettings(VendorSettingsVO.class);
    }

    @Override
    @Transactional
    public void saveVendor(VendorSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public GetLoginProtocolVO getLoginProtocol() {
        return this.getSettings(GetLoginProtocolVO.class);
    }

    @Override
    @Transactional
    public void saveLoginProtocol(LoginProtocolParam param) {
        switch (SettingsEnum.fromKey(param.getCode())) {
            case TERMS_OF_SERVICE:
                settingsHelper.set(TERMS_OF_SERVICE_SHOW, param.getShow());
                settingsHelper.set(TERMS_OF_SERVICE, param.getContent());
                break;
            case PRIVACY_POLICY:
                settingsHelper.set(PRIVACY_POLICY_SHOW, param.getShow());
                settingsHelper.set(PRIVACY_POLICY, param.getContent());
                break;
            case AFTER_SALES_SERVICE:
                settingsHelper.set(AFTER_SALES_SERVICE_SHOW, param.getShow());
                settingsHelper.set(AFTER_SALES_SERVICE, param.getContent());
                break;
        }
    }

    @Override
    public GetLoginProtocolContentVO getLoginProtocolContentVO(String configCode) {
        switch (SettingsEnum.fromKey(configCode)) {
            case TERMS_OF_SERVICE:
                Integer termsOfServiceShow = settingsHelper.get(TERMS_OF_SERVICE_SHOW);
                String termsOfService = settingsHelper.get(TERMS_OF_SERVICE);
                return new GetLoginProtocolContentVO(termsOfServiceShow, termsOfService);
            case PRIVACY_POLICY:
                Integer privacyPolicyShow = settingsHelper.get(PRIVACY_POLICY_SHOW);
                String privacyPolicy = settingsHelper.get(PRIVACY_POLICY);
                return new GetLoginProtocolContentVO(privacyPolicyShow, privacyPolicy);
            case AFTER_SALES_SERVICE:
                Integer afterSalesServiceShow = settingsHelper.get(AFTER_SALES_SERVICE_SHOW);
                String afterSalesService = settingsHelper.get(AFTER_SALES_SERVICE);
                return new GetLoginProtocolContentVO(afterSalesServiceShow, afterSalesService);
            default:
                return null;
        }
    }

    @Override
    public ProfitSharingSettingsVO getProfitSharingSettings() {
        return this.getSettings(ProfitSharingSettingsVO.class);
    }

    @Override
    @Transactional
    public void saveProfitSharing(ProfitSharingSaveParam param) {
        String channel = param.getProfitSharingOnlineChannel();
        if (channel != null && !channel.isBlank()
                && !"wechat".equals(channel) && !"huifu".equals(channel) && !"alipay".equals(channel)) {
            throw new GlobalException("不支持的线上分账渠道: " + channel);
        }
        applicationContext.getBean(ConfigService.class).saveConfigSettings(param);
    }

    @Override
    public WithdrawalSettingsVO getWithdrawalSettings() {
        return this.getSettings(WithdrawalSettingsVO.class);
    }

    @Override
    @Transactional
    public void saveWithdrawal(WithdrawalSaveParam param) {
        applicationContext.getBean(ConfigService.class).saveConfigSettings(param);
    }

    @Override
    public MemberWithdrawalSettingsVO getMemberWithdrawalSettings() {
        return this.getSettings(MemberWithdrawalSettingsVO.class);
    }

    @Override
    @Transactional
    public void saveMemberWithdrawal(MemberWithdrawalSaveParam param) {
        applicationContext.getBean(ConfigService.class).saveConfigSettings(param);
    }

    @Override
    public CommissionWithdrawalSettingsVO getCommissionWithdrawalSettings() {
        return this.getSettings(CommissionWithdrawalSettingsVO.class);
    }

    @Override
    @Transactional
    public void saveCommissionWithdrawal(CommissionWithdrawalSaveParam param) {
        applicationContext.getBean(ConfigService.class).saveConfigSettings(param);
    }

    @Override
    public SearchSettingsVO getSearchSettings() {
        SearchSettingsVO searchSettings = new SearchSettingsVO();

        // 获取搜索类型配置
        String searchType = getConfigValue(ConfigConstants.SEARCH, "selectSearchType");
        if (StrUtil.isNotBlank(searchType)) {
            searchSettings.setSelectSearchType(searchType);
        }

        // 检查ES连接状态
        try {
            Map<String, Object> response = productEsService.checkConnection();
            searchSettings.setEsConnected((Boolean) response.get("connected"));
        } catch (Exception e) {
            searchSettings.setEsConnected(false);
        }

        return searchSettings;
    }

    @Override
    public void saveSearch(SearchSaveParam param) {
        Map<String, Object> searchConfig = new HashMap<>();
        searchConfig.put("selectSearchType", param.getSelectSearchType());

        // 直接保存配置
        LambdaQueryWrapper<ConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConfigPO::getBizCode, ConfigConstants.SEARCH);
        ConfigPO config = this.getOne(queryWrapper);

        if (config == null) {
            config = new ConfigPO();
            config.setBizCode(ConfigConstants.SEARCH);
        }
        config.setBizVal(JsonUtil.toJson(searchConfig));
        this.saveOrUpdate(config);
    }

    @Override
    public Map<String, Object> checkEsConnection() {
        try {
            Map<String, Object> response = productEsService.checkConnection();
            return response;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("connected", false);
            result.put("message", "ES连接检查失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public GetStoreVO getStoreSettings() {
        return this.getSettings(GetStoreVO.class);
    }

    @Transactional
    @Override
    public void saveStore(StoreSaveParam param) {
        GetStoreVO settings = this.getSettings(GetStoreVO.class);
        this.saveConfigSettings(param);
        if (settings.getStoreIndependentGoods() == 1 && param.getStoreIndependentGoods() == 0) {
            // 下架所有自建商品
            productMapper.update(new LambdaUpdateWrapper<Product>().set(Product::getProductStatus, NO).ne(Product::getShopId, 0));
        }
    }

    @Override
    public AmapVO getAmapSettings(String type) {
        AmapVO amapVO = new AmapVO();
        if (type.equals("web")) {
            GetAmapVO settings = this.getSettings(GetAmapVO.class);
            BeanUtil.copyProperties(settings, amapVO);
            return amapVO;
        }
        if (type.equals("webjs")) {
            GetAmapJSVO settings = this.getSettings(GetAmapJSVO.class);
            BeanUtil.copyProperties(settings, amapVO);
            return amapVO;
        }
        if (type.equals("mini")) {
            GetAmapMiniVO settings = this.getSettings(GetAmapMiniVO.class);
            BeanUtil.copyProperties(settings, amapVO);
            return amapVO;
        }
        return null;
    }

    @Override
    @Transactional
    public void saveAmapSettings(AmapParam param) {
        String type = param.getType();
        if (type.equals("web")) {
            AmapSaveParam saveParam = new AmapSaveParam();
            BeanUtil.copyProperties(param, saveParam);
            this.saveConfigSettings(saveParam);
        }
        if (type.equals("webjs")) {
            AmapJsSaveParam saveParam = new AmapJsSaveParam();
            BeanUtil.copyProperties(param, saveParam);
            this.saveConfigSettings(saveParam);
        }
        if (type.equals("mini")) {
            AmapMiniSaveParam saveParam = new AmapMiniSaveParam();
            BeanUtil.copyProperties(param, saveParam);
            this.saveConfigSettings(saveParam);
        }
    }

    @Override
    public AmapAllVO getAllAmapSettings(Integer use) {
        AmapAllVO settings = this.getSettings(AmapAllVO.class);
        if (use == null || use == 0) {
            settings.encryptData();
        }
        return settings;
    }

    @Override
    @Transactional
    public void saveAllAmapSettings(AmapAllParam param) {
        AmapAllVO amapAllVO = this.getSettings(AmapAllVO.class);
        param.noUpdate(amapAllVO);
        this.saveConfigSettings(param);
    }

    @Override
    public MapSettingsVO getMapSettings(Integer use) {
        MapSettingsVO settings = this.getSettings(MapSettingsVO.class);
        if (use == null || use == 0) {
            settings.encryptData();
        }
        return settings;
    }

    @Override
    @Transactional
    public void saveMapSettings(MapSettingsParam param) {
        // 必须先拿库中真值做 noUpdate，否则前端回传的掩码串会被当成真实凭据写库
        MapSettingsVO settings = this.getSettings(MapSettingsVO.class);
        param.noUpdate(settings);
        this.saveConfigSettings(param);
    }

    @Override
    public void savePersonalizedSetting(PersonalizedSettingSaveParam param) {
        applicationContext.getBean(ConfigService.class).saveConfigSettings(param);
    }

    @Override
    public PersonalizedSettingVO getPersonalizedSetting() {
        return this.getSettings(PersonalizedSettingVO.class);
    }

    @Override
    public GetStoreSelfOperatedVO getStoreSelfOperatedSettings() {
        GetStoreSelfOperatedVO settings = this.getSettings(GetStoreSelfOperatedVO.class);

        // 添加空值检查和数据清理
        String regionStr = settings.getStoreSelfOperatedRegion();
        if (ObjectUtil.isNotEmpty(regionStr)) {
            // 过滤掉空字符串并转换为整数列表
            List<Integer> list = Arrays.stream(regionStr.split(",")).map(String::trim).filter(s -> !s.isEmpty()).map(Integer::parseInt).toList();

            if (ObjectUtil.isNotEmpty(list)) {
                LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(Region::getRegionId, list);
                List<Region> regions = regionMapper.selectList(queryWrapper);
                settings.setStoreSelfOperatedRegions(regions.stream().map(Region::getRegionName).toList());
            }
        }

        if (ObjectUtil.isNotEmpty(settings.getStoreSelfOperatedContactJson())) {
            settings.setShopContactConfig(JSONUtil.toList(settings.getStoreSelfOperatedContactJson(), StoreExtendedDto.ShopContactConfig.class));
        }
        if (ObjectUtil.isNotEmpty(settings.getStoreSelfOperatedOpenCloseJson())) {
            settings.setShopOpenCloseConfig(JSONUtil.toBean(settings.getStoreSelfOperatedOpenCloseJson(), StoreExtendedDto.ShopOpenCloseConfig.class));
        }
        if (ObjectUtil.isNotEmpty(settings.getStoreSelfOperatedTipsJson())) {
            settings.setShopTips(JSONUtil.toList(settings.getStoreSelfOperatedTipsJson(), Integer.class));
        }

        return settings;
    }

    @Override
    public void saveStoreSelfOperated(StoreSelfOperatedSaveParam param) {
        param.setStoreSelfOperatedContactJson(param.getShopContactConfig() != null
                ? JSONUtil.toJsonStr(param.getShopContactConfig()) : "[]");
        param.setStoreSelfOperatedOpenCloseJson(param.getShopOpenCloseConfig() != null
                ? JSONUtil.toJsonStr(param.getShopOpenCloseConfig()) : "");
        param.setStoreSelfOperatedTipsJson(param.getShopTips() != null
                ? JSONUtil.toJsonStr(param.getShopTips()) : "[]");
        applicationContext.getBean(ConfigService.class).saveConfigSettings(param);
    }


    private List<ConfigPO> extractConfigs(Object obj) {
        List<ConfigPO> configs = new ArrayList<>();

        ReflectionUtils.doWithFields(obj.getClass(), field -> {
            ReflectionUtils.makeAccessible(field);
            Object fieldValue = ReflectionUtils.getField(field, obj);

            // 嵌套字段递归处理
            if (fieldValue != null && field.isAnnotationPresent(ConfigInnerParam.class)) {
                configs.addAll(extractConfigs(fieldValue));
                return;
            }

            // 忽略字段
            if (field.isAnnotationPresent(ConfigIgnoreField.class)) {
                return;
            }

            // 获取字段名或枚举定义的 bizCode
            String bizCode = field.getName();
            if (field.isAnnotationPresent(ConfigItemField.class)) {
                ConfigItemField annotation = field.getAnnotation(ConfigItemField.class);
                bizCode = annotation.value().getBizCode();
            }

            if (fieldValue instanceof List<?>) {
                // 处理 List 类型，转换为 JSON 字符串存储
                String jsonValue = JSONUtil.toJsonStr(fieldValue);
                configs.add(new ConfigPO(bizCode, jsonValue));
            } else {
                configs.add(new ConfigPO(bizCode, fieldValue != null ? String.valueOf(fieldValue) : null));
            }
        });

        return configs;
    }


    @Override
    public void createPlatformCertificate() {
        // ConfigPO wechatPayMchidConfig = this.getConfigByCode(WECHAT_PAY_MCHID.getBizCode());
        // ConfigPO wechatPaySerialNoConfig = this.getConfigByCode(WECHAT_PAY_SERIAL_NO.getBizCode());
        // ConfigPO wechatPayKeyConfig = this.getConfigByCode(WECHAT_PAY_KEY.getBizCode());

        // SignUtils.sign()
        //
        // // 初始化商户配置
        // Config config =
        //         new RSAAutoCertificateConfig.Builder()
        //                 .merchantId(wechatPayMchidConfig.getBizVal())
        //                 // 使用 com.wechat.pay.java.core.util
        //                 // 中的函数从本地文件中加载商户私钥，商户私钥会用来生成请求的签名
        //                 .privateKeyFromPath(wechatPayKeyConfig.getBizVal())
        //                 .merchantSerialNumber(wechatPaySerialNoConfig.getBizVal())
        //                 .apiV3Key(apiV3Key)
        //                 .build();
        //
        // // 初始化证书服务
        // CertificateService certificateService = new CertificateService.Builder().config(config).build();
        // // 设置商户apiV3密钥，apiV3密钥用于解密下载证书
        // String certificate = certificateService.downloadCertificate(apiV3Key.getBytes(StandardCharsets.UTF_8));
        // // 将证书写入文件
        // Files.write(Paths.get(CERT_PATH), certificate.getBytes());
    }


    @Override
    public ConfigBasicVO getConfigBasic() {
        // 1. 获取父ID为0的所有地区
        List<Region> regionList = regionMapper.selectList(new LambdaQueryWrapper<Region>().eq(Region::getParentId, 0));
        // 2. 查询所有code中包含base的配置
        Map<String, Map<String, Object>> configMap = getConfigsByCode(BASE);

        // 3. 存储设置
        List<SettingsEnum> settings = List.of(STORAGE_TYPE, STORAGE_LOCAL_URL, STORAGE_OSS_URL, STORAGE_OSS_ACCESS_KEY_ID, STORAGE_OSS_ACCESS_KEY_SECRET, STORAGE_OSS_BUCKET, STORAGE_OSS_REGION, STORAGE_COS_URL, STORAGE_COS_SECRET_ID, STORAGE_COS_SECRET_KEY, STORAGE_COS_BUCKET, STORAGE_COS_REGION);
        Map<String, String> storageConfigMap = this.getConfigMapBySettings(settings);
        if (storageConfigMap != null) {
            ConfigStorageDTO configStorage = new ConfigStorageDTO(storageConfigMap);
            // 将密钥写回 storageConfigMap
            processStorageSecrets(configStorage, storageConfigMap);
        }

        // 4. 敏感信息混淆的配置处理
        Map<String, String> sensitiveKeys = Map.of(BASE_SMS, "sms_key_secret", BASE_API_WECHAT, "wechat_appSecret", BASE_API_MINI_PROGRAM, "wechat_miniProgram_secret", BASE_API_APP_PAY, "wechat_pay_app_secret", BASE_API_LANG, "lang_volcengine_secret");

        // 遍历配置项，批量混淆敏感数据
        sensitiveKeys.forEach((configKey, secretKey) -> {
            Map<String, Object> config = configMap.get(configKey);
            if (config != null) {
                hideSecret(config, secretKey);
            }
        });

        return new ConfigBasicVO(regionList, configMap);
    }

    /**
     * 处理存储配置中的敏感信息
     *
     * @param configStorage    存储配置对象
     * @param storageConfigMap 原始的 map，用于将混淆后的值放回 map
     */
    private void processStorageSecrets(ConfigStorageDTO configStorage, Map<String, String> storageConfigMap) {
        if (configStorage == null || configStorage.getStorageType() == null) {
            return; // 避免空指针异常
        }
        // oss密钥混淆处理
        String ossSecret = configStorage.getStorageOssAccessKeySecret();
        String hiddenOssSecret = StringUtils.hide(ossSecret, 12, 18);
        storageConfigMap.put(STORAGE_OSS_ACCESS_KEY_SECRET.getBizCode(), hiddenOssSecret);

        // cos密钥混淆处理
        String cosSecret = configStorage.getStorageCosSecretKey();
        String hiddenCosSecret = StringUtils.hide(cosSecret, 12, 18);
        storageConfigMap.put(STORAGE_COS_SECRET_KEY.getBizCode(), hiddenCosSecret);
    }

    /**
     * 隐藏配置中的敏感信息
     *
     * @param map         配置数据
     * @param propertyKey 配置项的key
     */
    private void hideSecret(Map<String, Object> map, String propertyKey) {
        if (map == null || !map.containsKey(propertyKey)) {
            return; // 防止空指针异常
        }
        Object secretValue = map.get(propertyKey);
        if (secretValue instanceof String secret) {
            map.put(propertyKey, StringUtils.hide(secret, 12, 18));
        }
    }


    @Override
    public ConfigPO getConfigByCode(String code) {
        if (StrUtil.isEmpty(code)) {
            throw new GlobalException(CODE_PARAM_ERROR);
        }

        ConfigPO configPO = this.lambdaQuery().eq(ConfigPO::getBizCode, code).eq(ConfigPO::getIsDel, 0).one();
        if (configPO == null) {
            throw new GlobalException(CONFIG_NOT_EXIST);
        }

        return configPO;
    }

    @Override
    public void sendTestMail(String testMailAddress) {
        MailUtil.send(loadEmailConfig(), testMailAddress, "测试邮件", "这是一封测试邮件，收到此邮件代表着您的邮箱服务器设置正确！", false);
    }

    /**
     * 加载邮件配置
     */
    private MailAccount loadEmailConfig() {

        // 创建邮件账户对象
        MailAccount account = new MailAccount();
        account.setHost(this.getConfigVal(SMTP_HOST));
        account.setPort(Integer.parseInt(this.getConfigVal(SMTP_PORT)));
        account.setAuth(true);
        account.setFrom(this.getConfigVal(SMTP_MAIL));
        account.setUser(this.getConfigVal(SMTP_USER));
        account.setPass(this.getConfigVal(SMTP_PASS));
        account.setSslEnable(true);
        return account;
    }

    @Override
    public Map<String, Object> getBase(Integer previewId) {

        // 冷启动批量预载：一次 IN 查询把全部配置载入 ConfigValCache，使后续 50+ 次 getConfigVal 全部命中内存
        preloadAllConfigs();

        // 主题id
        JSONObject themeStyleJsonObject = JSONUtil.parseObj(this.getConfigVal(THEME_STYLE));
        int themeId = themeStyleJsonObject.getInt("themeId");
        String shopName = translatePackage.translate(this.getConfigVal(SHOP_NAME), SETTING.getCode());
        String shopTitle = translatePackage.translate(this.getConfigVal(SHOP_TITLE), SETTING.getCode());
        String shopTitleSuffix = translatePackage.translate(this.getConfigVal(SHOP_TITLE_SUFFIX), SETTING.getCode());
        String shopLogo = this.getConfigVal(SHOP_LOGO);
        String shopKeywords = translatePackage.translate(this.getConfigVal(SHOP_KEYWORDS), SETTING.getCode());
        String shopDesc = translatePackage.translate(this.getConfigVal(SHOP_DESC), SETTING.getCode());
        String icoImg = this.getConfigVal(ICO_IMG);

        String autoRedirectVal = this.getConfigVal(AUTO_REDIRECT);
        int autoRedirect = StrUtil.isBlank(autoRedirectVal) ? 1 : Integer.parseInt(autoRedirectVal);

        String h5Domain = this.getConfigVal(H5_DOMAIN);
        String pcDomain = this.getConfigVal(PC_DOMAIN);
        String adminDomain = this.getConfigVal(ADMIN_DOMAIN);
        String shopIcpNo = translatePackage.translate(this.getConfigVal(SHOP_ICP_NO), SETTING.getCode());

        String shopIcpNoUrlVal = this.getConfigVal(SHOP_ICP_NO_URL);
        String shopIcpNoUrl = StrUtil.isBlank(shopIcpNoUrlVal) ? "" : "https://beian.miit.gov.cn";

        String shop110No = this.getConfigVal(SHOP_110_NO);

        String shopIcpNoLinkVal = this.getConfigVal(SHOP_110_LINK);
        String shop110Link = StrUtil.isBlank(shopIcpNoLinkVal) ? "" : "https://beian.mps.gov.cn/#/query/webSearch";

        String companyAddress = this.getConfigVal(COMPANY_ADDRESS);

        String isOpenMobileAreaCodeVal = this.getConfigVal(IS_OPEN_MOBILE_AREA_CODE);
        int isOpenMobileAreaCode = StrUtil.isBlank(isOpenMobileAreaCodeVal) ? 0 : Integer.parseInt(isOpenMobileAreaCodeVal);

        String closeOrderVal = this.getConfigVal(CLOSE_ORDER);
        int closeOrder = StrUtil.isBlank(closeOrderVal) ? 0 : Integer.parseInt(closeOrderVal);

        String shopRegClosedVal = this.getConfigVal(SHOP_REG_CLOSED);
        int shopRegClosed = StrUtil.isBlank(shopRegClosedVal) ? 0 : Integer.parseInt(shopRegClosedVal);

        String isIdentityVal = this.getConfigVal(IS_IDENTITY);
        int isIdentity = StrUtil.isBlank(isIdentityVal) ? 0 : Integer.parseInt(isIdentityVal);

        String isEnquiryVal = this.getConfigVal(IS_ENQUIRY);
        int isEnquiry = StrUtil.isBlank(isEnquiryVal) ? 0 : Integer.parseInt(isEnquiryVal);

        // 获取存储地址
        String storageUrl = getStorageUrl();

        String dollarSignVal = this.getConfigVal(DOLLAR_SIGN);
        String dollarSign = StrUtil.isBlank(dollarSignVal) ? "¥" : dollarSignVal;

        String dollarSignCnVal = this.getConfigVal(DOLLAR_SIGN_CN);
        String dollarSignCn = StrUtil.isBlank(dollarSignCnVal) ? "元" : dollarSignCnVal;

        String showSelledCountVal = this.getConfigVal(SHOW_SELLED_COUNT);
        int showSelledCount = StrUtil.isBlank(showSelledCountVal) ? 1 : Integer.parseInt(showSelledCountVal);

        String showMarketpriceVal = this.getConfigVal(SHOW_MARKETPRICE);
        int showMarketprice = StrUtil.isBlank(showMarketpriceVal) ? 1 : Integer.parseInt(showMarketpriceVal);

        // 微信配置
        String openWechatOauth = this.getConfigVal(OPEN_WECHAT_OAUTH);
        // 商户设置
        String personApplyEnabled = this.getConfigVal(PERSON_APPLY_ENABLED);

        // 客服设置
        int show_service = Integer.parseInt(this.getConfigVal(KEFU_TYPE)) > 0 ? 1 : 0;
        String kefuPhone = this.getConfigVal(KEFU_PHONE);
        String kefuTime = this.getConfigVal(KEFU_TIME);

        // 声明
        String shopCompany = this.getConfigVal(SHOP_COMPANY);
        String poweredByStatus = this.getConfigVal(POWERED_BY_STATUS);
        String poweredBy = this.getConfigVal(POWERED_BY);
        // 自动生成许可数据
        String isEnterprise = this.getConfigVal(IS_ENTERPRISE);
        String deCopyright = this.getConfigVal(DE_COPYRIGHT);

        // 基础产品类别装修
        Integer categoryDecorateType = Integer.parseInt(this.getConfigVal(PRODUCT_CATEGORY_DECORATE_TYPE));

        // 购物信息
        String canInvoice = this.getConfigVal(CAN_INVOICE);
        String invoiceAdded = this.getConfigVal(INVOICE_ADDED);

        // 店铺信息
        String defaultShopName = this.getConfigVal(DEFAULT_SHOP_NAME);
        // 支付信息
        String useSurplus = this.getConfigVal(USE_SURPLUS);
        String usePoints = this.getConfigVal(USE_POINTS);
        String useCoupon = this.getConfigVal(USE_COUPON);
        // 会员余额提现开关（D1107），下发给 PC/H5 控制提现入口显隐
        String memberWithdrawalEnabled = this.getConfigVal(SettingsEnum.MEMBER_WITHDRAWAL_ENABLED);
        // 分销佣金提现开关（D1107），下发给 PC/H5 控制佣金提现入口显隐
        String commissionWithdrawalEnabled = this.getConfigVal(SettingsEnum.COMMISSION_WITHDRAWAL_ENABLED);

        // 基础API公司数据
        String typeVal = this.getConfigVal(TYPE);
        int companyDataType = StrUtil.isBlank(typeVal) ? 2 : Integer.parseInt(typeVal);
        String companyDataTips = this.getConfigVal(TIPS);
        String version_type = tigshopProperties.getVersion();

        // 商品搜索设置
        String enableAttributeFilter = this.getConfigVal(ENABLE_ATTRIBUTE_FILTER);

        // 存进来
        Map<String, Object> propertiesMap = new HashMap<>();

        // 添加 themeId
        propertiesMap.put("themeStyle", themeStyleJsonObject);
        propertiesMap.put("themeId", themeId);

        // 添加 商城配置
        propertiesMap.put("shopName", shopName);
        propertiesMap.put("shopTitle", shopTitle);
        propertiesMap.put("shopTitleSuffix", shopTitleSuffix);
        propertiesMap.put("shopLogo", shopLogo);
        propertiesMap.put("shopKeywords", shopKeywords);
        propertiesMap.put("shopDesc", shopDesc);
        propertiesMap.put("icoImg", icoImg);
        propertiesMap.put("autoRedirect", autoRedirect);
        propertiesMap.put("h5Domain", h5Domain);
        propertiesMap.put("pcDomain", pcDomain);
        propertiesMap.put("adminDomain", adminDomain);
        propertiesMap.put("shopIcpNo", shopIcpNo);
        propertiesMap.put("shopIcpNoUrl", shopIcpNoUrl);
        propertiesMap.put("shop110No", shop110No);
        propertiesMap.put("shop110Link", shop110Link);
        propertiesMap.put("companyAddress", companyAddress);
        propertiesMap.put("isOpenMobileAreaCode", isOpenMobileAreaCode);
        propertiesMap.put("closeOrder", closeOrder);
        propertiesMap.put("shopRegClosed", shopRegClosed);
        propertiesMap.put("isIdentity", isIdentity);
        propertiesMap.put("isEnquiry", isEnquiry);

        // 获取存储地址
        propertiesMap.put("storageUrl", storageUrl);

        // 商品配置
        propertiesMap.put("dollarSign", dollarSign);
        propertiesMap.put("dollarSignCn", dollarSignCn);
        propertiesMap.put("showSelledCount", showSelledCount);
        propertiesMap.put("showMarketprice", showMarketprice);

        // 微信配置
        propertiesMap.put("openWechatOauth", openWechatOauth);

        // 商户设置
        propertiesMap.put("personApplyEnabled", personApplyEnabled);

        // 客服设置
        propertiesMap.put("showService", show_service);
        propertiesMap.put("kefuPhone", kefuPhone);
        propertiesMap.put("kefuTime", kefuTime);

        // 声明
        propertiesMap.put("shopCompany", shopCompany);
        propertiesMap.put("poweredByStatus", poweredByStatus);
        propertiesMap.put("poweredBy", poweredBy);

        // 自动生成许可数据
        propertiesMap.put("isEnterprise", isEnterprise);
        propertiesMap.put("deCopyright", deCopyright);

        // 基础产品类别装修
        propertiesMap.put("categoryDecorateType", categoryDecorateType);

        // 购物信息
        UserRankConfig growConfig = userRankConfigMapper.selectOne(new LambdaQueryWrapper<UserRankConfig>().eq(UserRankConfig::getCode, "grow_config"));
        propertiesMap.put("growUpSetting", JSONUtil.parse(growConfig.getData()));
        propertiesMap.put("canInvoice", canInvoice);
        propertiesMap.put("invoiceAdded", invoiceAdded);

        // 店铺信息
        propertiesMap.put("defaultShopName", defaultShopName);

        // 支付信息
        propertiesMap.put("useSurplus", useSurplus);
        propertiesMap.put("usePoints", usePoints);
        propertiesMap.put("useCoupon", useCoupon);
        propertiesMap.put("memberWithdrawalEnabled", memberWithdrawalEnabled);
        propertiesMap.put("commissionWithdrawalEnabled", commissionWithdrawalEnabled);

        // 基础API公司数据
        propertiesMap.put("companyDataType", companyDataType);
        propertiesMap.put("companyDataTips", companyDataTips);
        propertiesMap.put("versionType", version_type);

        shopCompany = propertiesMap.containsKey("deCopyright") ? propertiesMap.get("shopCompany").toString() : DEFAULT_COMPANY.getBizDesc();
        propertiesMap.put("shopCompany", shopCompany);
        propertiesMap.put("decoratePageConfig", decorateService.getPcIndexDecoratePageConfig(previewId));

        propertiesMap.put("enableAttributeFilter", enableAttributeFilter);
        return propertiesMap;
    }

    @Override
    public boolean sendSms(RegisterSmsDTO dto) {
        // 验证验证码是否正确
        if (!captchaHandler.validateCaptcha(dto.getVerifyToken())) {
            // 如果验证码错误，抛出异常
            throw new GlobalException("获取验证码失败，需要先进行行为验证");
        }
        // 随机生成验证码
        int code = RandomUtil.randomInt(100000, 999999);
        // 初始化短信模板
        MessageTemplate messageTemplate = messageTemplateService.buildMessageTemplate(MSG_CODE.getMessageId(), MESSAGE_TYPE_MSG.getCode());

        // 构造短信模板参数
        String templateParam = "{\"code\":\"" + code + "\"}";
        String templateId = messageTemplate.getTemplateId();
        // 发送短信（阿里云海外通道走 SendMessageToGlobe 明文）
        boolean isSend = dispatchSms(dto, templateId, messageTemplate.getContentOversea(), templateParam);
        if (isSend) {
            //存入redis
            redisTemplate.opsForValue().set(dto.getEvent() + dto.getMobile(), String.valueOf(code), 5, TimeUnit.MINUTES);
        }
        return isSend;
    }

    @Override
    public String sendCode(RegisterSmsDTO dto) {
        // 随机生成验证码
        int code = RandomUtil.randomInt(100000, 999999);
        // 初始化短信模板
        MessageTemplate messageTemplate = messageTemplateService.buildMessageTemplate(MSG_CODE.getMessageId(), MESSAGE_TYPE_MSG.getCode());

        String templateParam = "{\"code\":\"" + code + "\"}";
        String templateId = messageTemplate.getTemplateId();
        boolean isSend = dispatchSms(dto, templateId, messageTemplate.getContentOversea(), templateParam);
        if (isSend) {
            //存入redis
            redisTemplate.opsForValue().set(dto.getEvent() + dto.getMobile(), String.valueOf(code), 5, TimeUnit.MINUTES);
        }

        return String.valueOf(code);
    }

    @Override
    public boolean sendSms(RegisterSmsDTO dto, String templateId, Map<String, Object> templateParam) {
        String templateParamStr = JSON.toJSONString(templateParam);
        return this.sendSms(dto, templateId, templateParamStr);
    }

    @Override
    public boolean sendSms(RegisterSmsDTO dto, String templateId, String templateParam) {
        return dispatchSms(dto, templateId, null, templateParam);
    }

    /**
     * 组装并投递短信到 RabbitMQ。阿里云按通道开关（smsAliyunChannel）+ 号码决定走国内模板还是海外明文：
     * 命中海外通道时用 {@code contentOversea}（占位符替换后）走 SendMessageToGlobe（明文 + SenderID）；
     * 海外文案未配置则抛业务异常，禁止发送。
     */
    private boolean dispatchSms(RegisterSmsDTO dto, String templateId, String contentOversea, String templateParam) {
        String providerVal = this.getConfigVal(SMS_PROVIDER);
        Integer providerCode = (providerVal != null && !providerVal.isEmpty()) ? Integer.parseInt(providerVal) : 1;
        SmsProviderEnum providerEnum = SmsProviderEnum.fromCode(providerCode);

        SmsSendDTO smsSend = new SmsSendDTO();
        smsSend.setSmsProvider(providerEnum.getCode());
        smsSend.setPhoneNumbers(dto.getMobile());
        smsSend.setTemplateParam(templateParam);
        smsSend.setTemplateCode(templateId);

        // 阿里云：按通道开关（smsAliyunChannel）+ 号码决定走国内模板（SendSms）/ 海外明文（SendMessageToGlobe）
        if (providerEnum == SmsProviderEnum.ALIYUN) {
            String channel = this.getConfigVal(SMS_ALIYUN_CHANNEL);
            boolean useInternational = SmsChannelHelper.useInternationalChannel(channel, dto.getMobile());
            smsSend.setUseInternational(useInternational);
            if (useInternational) {
                if (contentOversea == null || contentOversea.isBlank()) {
                    log.error("走海外通道但未配置海外短信明文文案，禁止发送。号码：{}，国内模板：{}", dto.getMobile(), templateId);
                    throw new GlobalException("未配置海外短信文案，无法发送短信");
                }
                // 海外明文：用业务参数替换占位符后作为 SendMessageToGlobe 的 Message；验证码类为 OTP
                Map<String, Object> params = JSON.parseObject(templateParam);
                smsSend.setMessageContent(SmsChannelHelper.renderPlaintext(contentOversea, params));
                smsSend.setInternationalMessageType("OTP");
            }
        }
        // 填充服务商账号配置（单套账号，阿里云含 SenderID）
        Map<String, String> cfg = this.getConfigMapBySettings(SmsChannelHelper.configKeys(providerEnum));
        SmsChannelHelper.fillProviderConfig(smsSend, providerEnum, cfg);

        log.info("发送短信消息到 RabbitMQ，服务商：{}，号码：{}", providerEnum.getName(), dto.getMobile());
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.SEND_SMS_ROUTING_KEY, smsSend);
        return true;
    }

    @Override
    public Map<String, String> getConfigMapBySettings(List<SettingsEnum> settings) {
        if (CollUtil.isEmpty(settings)) {
            return Collections.emptyMap();
        }

        List<String> bizCods = settings.stream().map(SettingsEnum::getBizCode).toList();
        List<ConfigPO> configs = this.lambdaQuery().in(ConfigPO::getBizCode, bizCods).list();

        return configs.stream().collect(Collectors.toMap(ConfigPO::getBizCode, ConfigPO::getBizVal));
    }

    @Override
    public WechatLoginUrlVO getWechatLoginUrl(String redirectUrl) {
        String clientType = HeaderUtils.getClientType();

        switch (clientType) {
            case "wechat" -> {
                log.error(redirectUrl);
                WxMpDefaultConfigImpl configStorage = new WxMpDefaultConfigImpl();
                configStorage.setAppId(this.getConfigVal(SettingsEnum.WECHAT_APP_ID));
                configStorage.setSecret(this.getConfigVal(SettingsEnum.WECHAT_APP_SECRET));
                configStorage.setToken(this.getConfigVal(SettingsEnum.WECHAT_SERVER_TOKEN));
                configStorage.setAesKey(this.getConfigVal(SettingsEnum.WECHAT_SERVER_SECRET));
                WxMpService service = new WxMpServiceImpl();
                service.setWxMpConfigStorage(configStorage);
                // 1. 生成随机 state
                String state = UUID.randomUUID().toString().replace("-", "");
                redisTemplate.opsForValue().set("wechat::state:" + state, "valid", 300);
                String url = service.getOAuth2Service().buildAuthorizationUrl(redirectUrl, WxConsts.OAuth2Scope.SNSAPI_USERINFO, state);
                return new WechatLoginUrlVO(url);
            }
            case "pc" -> {
                String accessToken = wechatOAuthService.getMpAccessToken();
                if (StrUtil.isBlank(accessToken)) {
                    throw new GlobalException("获取微信access_token失败");
                }
                // 获取小程序二维码
                CreateQrcodeParam createQrcodeParam = CreateQrcodeParam.builder().expireSeconds(300).actionName("QR_STR_SCENE").actionInfo(CreateQrcodeParam.ActionInfo.builder().scene(Map.of("scene_id", RandomUtil.randomInt(0, 10000), "scene_str", RandomUtil.randomString(12))).build()).build();
                CreateQrcodeResult createQrcodeResult = wechatApiClient.qrcodeCreate(accessToken, createQrcodeParam);
                Integer errcode = createQrcodeResult == null ? null : createQrcodeResult.getErrcode();
                if (Objects.equals(errcode, 40001) || Objects.equals(errcode, 40014) || Objects.equals(errcode, 42001)) {
                    log.warn("微信公众号 access_token 已失效，强制刷新后重试二维码创建: errcode={}", errcode);
                    accessToken = wechatOAuthService.refreshMpAccessToken();
                    if (StrUtil.isBlank(accessToken)) {
                        throw new GlobalException("刷新微信access_token失败");
                    }
                    createQrcodeResult = wechatApiClient.qrcodeCreate(accessToken, createQrcodeParam);
                }
                if (createQrcodeResult == null) {
                    throw new GlobalException("获取微信登录二维码失败：微信返回空响应");
                }
                if (createQrcodeResult.getErrcode() != null && createQrcodeResult.getErrcode() != 0) {
                    throw new GlobalException(StrUtil.format("获取微信登录二维码失败：{}({})",
                            createQrcodeResult.getErrmsg(), createQrcodeResult.getErrcode()));
                }
                if (StrUtil.isBlank(createQrcodeResult.getUrl()) || StrUtil.isBlank(createQrcodeResult.getTicket())) {
                    throw new GlobalException("获取微信登录二维码失败：微信返回的二维码信息不完整");
                }
                return new WechatLoginUrlVO(createQrcodeResult.getUrl(), createQrcodeResult.getTicket());
            }
            default -> throw new GlobalException("不支持的终端类型");
        }
    }

    @Override
    public InitConfigSettingsVO getInitConfigSettings(Integer previewId) {
        InitConfigSettingsVO settings = this.getSettings(InitConfigSettingsVO.class);

        String storageUrl = getStorageUrl();

        UserRankConfig growConfig = userRankConfigMapper.selectOne(Wrappers.lambdaQuery(UserRankConfig.class).eq(UserRankConfig::getCode, "grow_config"));

        String decorateData = "";
        Integer headerStyle = 1;
        if (previewId != null) {
            Decorate decorate = decorateService.lambdaQuery().eq(Decorate::getDecorateId, previewId).one();
            if (decorate != null) {
                decorateData = decorate.getData();
            }
        } else {
            Decorate decorate = decorateService.lambdaQuery().eq(Decorate::getShopId, 0).eq(Decorate::getDecorateType, DecorateType.PC.getCode()).eq(Decorate::getIsHome, 1).eq(Decorate::getStatus, 1)
                    .last("limit 1").one();
            if (decorate != null) {
                decorateData = decorate.getData();
            }
            try {
                headerStyle = JSONUtil.parseObj(decorateData).getJSONObject("pageModule").getInt("headerStyle");
            } catch (Exception e) {
                log.debug(e.getMessage());
            }
        }

        settings.setVersion(tigshopProperties.getVersionNum());
        settings.assembleData(storageUrl, growConfig, JSONUtil.parseObj(decorateData).get("pageModule").toString(), tigshopProperties.getVersion(), headerStyle);

        // 是否存在个人中心页
        Decorate memberCenterPage = decorateService.lambdaQuery().eq(Decorate::getDecorateType, DecorateType.H5.getCode()).eq(Decorate::getPageType, 2).eq(Decorate::getIsHome, 1).last("limit 1").one();
        settings.setNewMemberCenterDecorate(memberCenterPage != null);
        return settings;
    }

    @Override
    public ThemeSettingsVO getThemeSettings() {
        return this.getSettings(ThemeSettingsVO.class);
    }

    @Override
    public IconSettingsVO getIcon() {
        List<String> icoTig = new ArrayList<>();
        String tigClass = "";
        List<String> icoDefined = new ArrayList<>();
        String definedClass = "";

        String data = redisTemplate.opsForValue().get(icoTigCss);

        if (data == null) {
            String content = HttpUtil.get(icoTigCss);
            if (content != null) {
                icoTig = extractIcons(content);
                tigClass = extractFontFamily(content);
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("ico_tig", icoTig);
                dataMap.put("tig_class", tigClass);
                redisTemplate.opsForValue().set(icoTigCss, JSON.toJSONString(dataMap));
            }
        } else {
            Map<String, Object> dataMap = (Map<String, Object>) JSON.parse(data);
            icoTig = (List<String>) dataMap.get("ico_tig");
            tigClass = (String) dataMap.get("tig_class");
        }

        // 自定义 ico
        ConfigPO icoDefinedCssConfig = this.getConfigByCode(ICO_DEFINED_CSS.getBizCode());
        if (icoDefinedCssConfig != null && icoDefinedCssConfig.getBizVal().startsWith("http") && icoDefinedCssConfig.getBizVal().endsWith(".css")) {
            String definedData = redisTemplate.opsForValue().get(icoDefinedCssConfig.getBizVal());
            if (definedData == null) {
                String content = HttpUtil.get(icoDefinedCssConfig.getBizVal());
                if (content != null) {
                    icoDefined = extractIcons(content);
                    definedClass = extractFontFamily(content);
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("ico_defined", icoDefined);
                    dataMap.put("defined_class", definedClass);
                    redisTemplate.opsForValue().set(icoDefinedCssConfig.getBizVal(), JSON.toJSONString(dataMap));
                }
            } else {
                Map<String, Object> dataMap = (Map<String, Object>) JSON.parse(definedData);
                icoDefined = (List<String>) dataMap.get("ico_defined");
                definedClass = (String) dataMap.get("defined_class");
            }
        }

        return new IconSettingsVO(icoTig, tigClass, icoDefined, definedClass);
    }

    private List<String> extractIcons(String content) {
        List<String> icons = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\.(.*?)\\:before").matcher(content);
        while (matcher.find()) {
            icons.add(matcher.group(1));
        }
        return icons;
    }

    private String extractFontFamily(String content) {
        Matcher matcher = Pattern.compile("font-family:\\s*\"([^\"]+)\";").matcher(content);
        return matcher.find() ? matcher.group(1) : "";
    }

    @Override
    public String getConfigVal(SettingsEnum setting) {
        String bizCode = setting.getBizCode();
        return configValCache.get(bizCode, () -> {
            ConfigPO config = this.lambdaQuery().eq(ConfigPO::getBizCode, bizCode).eq(ConfigPO::getIsDel, 0).one();
            return config != null ? config.getBizVal() : "";
        });
    }

    @Override
    public boolean isProfitSharingEnabled() {
        // 分账总开关（级联父）：默认开，仅显式配置为 0 时关闭（缺失键/空值/1 均视为开，零回归）
        return !"0".equals(getConfigVal(SettingsEnum.PROFIT_SHARING_ENABLED));
    }

    /**
     * 冷启动批量预载所有配置到 {@link ConfigValCache}：一次查询写入全部 bizCode→value，
     * 让 {@code getBase} 后续 50+ 次 {@code getConfigVal} 全部命中内存（冷启动 DB 往返 50+ → 1）。
     * 以 {@link SettingsEnum#THEME_STYLE} 作哨兵：缓存已热时跳过，不影响命中场景。
     */
    private void preloadAllConfigs() {
        if (configValCache.isCached(THEME_STYLE.getBizCode())) {
            return;
        }
        List<ConfigPO> all = this.lambdaQuery().eq(ConfigPO::getIsDel, 0).list();
        Map<String, String> values = new HashMap<>();
        for (ConfigPO config : all) {
            if (StrUtil.isNotBlank(config.getBizCode())) {
                // 与 getConfigVal 的 one() 语义一致（bizCode 唯一），重复时保留首个
                values.putIfAbsent(config.getBizCode(), config.getBizVal() != null ? config.getBizVal() : "");
            }
        }
        configValCache.warmUp(values);
    }

    @Override
    public Map<String, Object> getQuickLoginSetting() {
        int wechatLogin = 0;
        String headerValue = HeaderUtils.getHeaderValue(X_CLIENT_TYPE);
        if (StrUtil.isNotBlank(headerValue)) {
            switch (headerValue) {
                case "wechat", "pc" -> wechatLogin = Integer.parseInt(getConfigVal(OPEN_WECHAT_OAUTH));
                case "miniProgram" -> wechatLogin = 1;
            }
        }
        int showOauth = wechatLogin;
        return Map.of("wechatLogin", wechatLogin, "showOauth", showOauth);
    }

    @Override
    public ConfigCustomerDTO getConfigCustomer() {
        ConfigCustomerDTO configCustomerDTO = new ConfigCustomerDTO();

        String h5Domain = this.getConfigVal(H5_DOMAIN);
        String corpId = "";
        String url = "";
        // 服务类型
        String serviceType = this.getConfigVal(KEFU_TYPE);
        // 公开类型
        String openType = this.getConfigVal(KEFU_YZF_TYPE);
        switch (serviceType) {
            case "1" -> {
                String cmsSign = this.getConfigVal(KEFU_YZF_SIGN);
                corpId = this.getConfigVal(CORP_ID);
                url = StrUtil.format("{}{}", TENCENT_CUSTOMER_SERVICE_URL, cmsSign);
            }
            case "2" -> {
                String cmsWorkWechatId = this.getConfigVal(KEFU_WORKWX_ID);
                corpId = this.getConfigVal(CORP_ID);
                url = StrUtil.format("{}{}", QY_CUSTOMER_SERVICE_URL, cmsWorkWechatId);
            }
            case "3" -> url = this.getConfigVal(KEFU_CODE);
        }
        configCustomerDTO.setCorpId(corpId);
        configCustomerDTO.setUrl(url);
        configCustomerDTO.setOpenType(Integer.parseInt(openType));
        int serviceTypeValue = Integer.parseInt(serviceType);
        configCustomerDTO.setServiceType(serviceTypeValue);
        configCustomerDTO.setShow(serviceTypeValue > 0 ? 1 : 0);
        configCustomerDTO.setH5Domain(h5Domain);
        return configCustomerDTO;
    }

    /**
     * 根据code获取多个配置（主要用于返回多个base配置）
     *
     * @param code code
     * @return Map
     */
    public Map<String, Map<String, Object>> getConfigsByCode(String code) {
        LambdaQueryWrapper<ConfigPO> queryWrapper = Wrappers.lambdaQuery();
        if (code == null) {
            throw new GlobalException(PARAM_ERROR);
        }
        queryWrapper.like(ConfigPO::getBizCode, code);
        List<ConfigPO> configPOList = this.list(queryWrapper);
        return getConfigMap(configPOList);
    }

    /**
     * 将配置数据转换为Map
     *
     * @param configPOList 配置数据列表
     * @return Map
     */
    public Map<String, Map<String, Object>> getConfigMap(List<ConfigPO> configPOList) {
        Map<String, Map<String, Object>> configMap = new HashMap<>();

        // 遍历并处理每条记录
        for (ConfigPO configPO : configPOList) {
            String code = configPO.getBizCode();
            String data = configPO.getBizVal();

            // 将 JSON 字符串 data 转换为 Map<String, Object>
            Map<String, Object> dataMap = JsonUtil.jsonToMap(data);
            configMap.put(code, dataMap);
        }
        return configMap;
    }

    /**
     * 获取存储URL
     */
    public String getStorageUrl() {
        // 获取里面的存储类型，以确定前端需要哪些储存配置
        String storageTypeVal = this.getConfigVal(STORAGE_TYPE);
        // 获取存储类型对应的URL
        StorageType storageTypeEnum = StorageType.fromTypeCode(storageTypeVal);
        // 获取存储URL
        return this.getConfigVal(storageTypeEnum.getSettingsEnum());
    }


    public String getConfigValue(String configCode, String key) {
        try {
            Map<String, Object> configData = getConfigData(configCode);
            if (configData != null && configData.containsKey(key)) {
                return configData.get(key).toString();
            }
            return null;
        } catch (Exception e) {
            log.error("获取配置值失败: configCode={}, key={}", configCode, key, e);
            return null;
        }
    }

    @Override
    public void saveOrUpdateDingConfig(DingMessageConfigDTO dingMessageConfigDTO) {


        String configVal = getConfigVal(DING_APP_SECRET);
        if (StringUtils.isNotEmpty(configVal)) {
            update(Wrappers.lambdaUpdate(ConfigPO.class).eq(ConfigPO::getBizCode, DING_APP_SECRET.getBizCode()).set(ConfigPO::getBizVal, dingMessageConfigDTO.getDingAppSecret()));
        } else {
            ConfigPO newDingAppSecret = new ConfigPO(DING_APP_SECRET.getBizCode(), dingMessageConfigDTO.getDingAppSecret());
            save(newDingAppSecret);
        }

        String dingTalkRobotToken = getConfigVal(DING_TALK_ROBOT_TOKEN);
        if (StringUtils.isNotEmpty(dingTalkRobotToken)) {
            update(Wrappers.lambdaUpdate(ConfigPO.class).eq(ConfigPO::getBizCode, DING_TALK_ROBOT_TOKEN.getBizCode()).set(ConfigPO::getBizVal, dingMessageConfigDTO.getDingTalkRobotToken()));
        } else {
            ConfigPO newDingTalkRobotToken = new ConfigPO(DING_TALK_ROBOT_TOKEN.getBizCode(), dingMessageConfigDTO.getDingTalkRobotToken());
            save(newDingTalkRobotToken);
        }
    }

    @Override
    public void saveOrUpdateWXWorkConfig(WXWorkMessageConfig wxMessageConfigDTO) {

        String wxWebhookUrl = getConfigVal(WXWORK_WEBHOOK_URL);
        if (StringUtils.isNotEmpty(wxWebhookUrl)) {
            update(Wrappers.lambdaUpdate(ConfigPO.class).eq(ConfigPO::getBizCode, WXWORK_WEBHOOK_URL.getBizCode()).set(ConfigPO::getBizVal, wxMessageConfigDTO.getWxworkWebhookUrl()));
        } else {
            ConfigPO newWxWebhookUrl = new ConfigPO(WXWORK_WEBHOOK_URL.getBizCode(), wxMessageConfigDTO.getWxworkWebhookUrl());
            save(newWxWebhookUrl);
        }
    }

    @Override
    public WXWorkMessagePushConfigVo getWxWorkMessagePushConfig() {
        WXWorkMessagePushConfigVo WXWorkMessagePushConfigVo = new WXWorkMessagePushConfigVo();

        String configVal = getConfigVal(WXWORK_WEBHOOK_URL);
        if (StringUtils.isNotEmpty(configVal)) {
            WXWorkMessagePushConfigVo.setWxworkWebhookUrl(configVal);
        }

        return WXWorkMessagePushConfigVo;
    }

    @Override
    public DingMessagePushConfigVo getDingMessagePushConfig() {
        DingMessagePushConfigVo dingMessagePushConfigVo = new DingMessagePushConfigVo();
        String configVal = getConfigVal(DING_TALK_ROBOT_TOKEN);
        if (StringUtils.isNotEmpty(configVal)) {
            dingMessagePushConfigVo.setDingTalkRobotToken(configVal);
        }

        String dingAppSecret = getConfigVal(DING_APP_SECRET);
        if (StringUtils.isNotEmpty(dingAppSecret)) {
            dingMessagePushConfigVo.setDingAppSecret(dingAppSecret);
        }

        return dingMessagePushConfigVo;
    }

    @Override
    public CashierOrderSettingsVO getCashierOrderSettings() {
        Integer shopId = HeaderUtils.getShopId();
        CashierOrderSettingsVO vo = new CashierOrderSettingsVO();

        if (shopId == null) {
            throw new GlobalException("店铺不存在");
        }

        if (shopId == 0) {
            CashierOrderSettingsVO settings = this.getSettings(CashierOrderSettingsVO.class);
            if (settings.getShopCommonRemarks() == null) {
                vo.setShopCommonRemarks(List.of());
            }
            return settings;
        }

        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new GlobalException("店铺不存在");
        }

        vo.setAutoEraseZero(ObjectUtil.defaultIfNull(shop.getAutoEraseZero(), 0));
        vo.setAutoDeductionPoints(ObjectUtil.defaultIfNull(shop.getAutoDeductionPoints(), 0));
        vo.setDefaultCollectionMethod(ObjectUtil.defaultIfNull(shop.getDefaultCollectionMethod(), 1));
        vo.setAutoAddCartAfterScan(ObjectUtil.defaultIfNull(shop.getAutoAddCartAfterScan(), 1));
        vo.setAutoPrintOnlineOrder(ObjectUtil.defaultIfNull(shop.getAutoPrintOnlineOrder(), 1));
        vo.setAutoPrintOfflineOrder(ObjectUtil.defaultIfNull(shop.getAutoPrintOfflineOrder(), 1));
        List<String> remarks = Collections.emptyList();
        if (StrUtil.isNotBlank(shop.getShopCommonRemarks())) {
            try {
                remarks = JsonUtil.jsonToList(shop.getShopCommonRemarks(), String.class);
            } catch (Exception ignored) {
            }
        }
        vo.setShopCommonRemarks(remarks);
        // 处理商品来源配置
        List<Integer> productSource = List.of(0);
        if (StrUtil.isNotBlank(shop.getPosProductSource())) {
            try {
                productSource = JsonUtil.jsonToList(shop.getPosProductSource(), Integer.class);
            } catch (Exception ignored) {
            }
        }
        vo.setPosProductSource(productSource);
        return vo;
    }

    @Transactional
    @Override
    public void saveCashierOrderSettings(CashierOrderSettingsSaveParam param) {
        Integer shopId = HeaderUtils.getShopId();
        if (shopId == null) {
            throw new GlobalException("店铺不存在");
        }
        if (shopId == 0) {
            this.saveConfigSettings(param);
            return;
        }
        LambdaUpdateWrapper<Shop> updateWrapper = Wrappers.lambdaUpdate(Shop.class)
                .eq(Shop::getShopId, shopId)
                .set(param.getAutoEraseZero() != null, Shop::getAutoEraseZero, param.getAutoEraseZero())
                .set(param.getAutoDeductionPoints() != null, Shop::getAutoDeductionPoints, param.getAutoDeductionPoints())
                .set(param.getDefaultCollectionMethod() != null, Shop::getDefaultCollectionMethod, param.getDefaultCollectionMethod())
                .set(param.getAutoAddCartAfterScan() != null, Shop::getAutoAddCartAfterScan, param.getAutoAddCartAfterScan())
                .set(param.getAutoPrintOnlineOrder() != null, Shop::getAutoPrintOnlineOrder, param.getAutoPrintOnlineOrder())
                .set(param.getAutoPrintOfflineOrder() != null, Shop::getAutoPrintOfflineOrder, param.getAutoPrintOfflineOrder())
                .set(param.getShopCommonRemarks() != null, Shop::getShopCommonRemarks, JsonUtil.toJson(param.getShopCommonRemarks()))
                .set(param.getPosProductSource() != null, Shop::getPosProductSource, JsonUtil.toJson(param.getPosProductSource()));

        shopMapper.update(null, updateWrapper);
    }


    /**
     * 获取配置数据
     */
    private Map<String, Object> getConfigData(String configCode) {
        LambdaQueryWrapper<ConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConfigPO::getBizCode, configCode);
        ConfigPO config = this.getOne(queryWrapper);

        if (config != null && StrUtil.isNotBlank(config.getBizVal())) {
            return JsonUtil.jsonToMap(config.getBizVal());
        }
        return null;
    }

    @Override
    public MiniProgramRiskSettingsVO getMiniProgramRiskSettings() {
        return this.getSettings(MiniProgramRiskSettingsVO.class);
    }

    @Transactional
    @Override
    public void saveMiniProgramRiskSettings(MiniProgramRiskSettingsSaveParam param) {
        this.saveConfigSettings(param);
    }

    @Override
    public WechatLiveSettingsVO getWechatLiveSettings() {
        WechatLiveSettingsVO settings = this.getSettings(WechatLiveSettingsVO.class);
        settings.encryptData();
        return settings;
    }

    @Transactional
    @Override
    public void saveWechatLiveSettings(WechatLiveSettingsSaveParam param) {
        WechatLiveSettingsVO settings = this.getWechatLiveSettings();
        param.noUpdate(settings);
        this.saveConfigSettings(param);
    }

}
