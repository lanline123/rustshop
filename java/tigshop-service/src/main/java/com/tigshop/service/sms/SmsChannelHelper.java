// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.sms;

import com.tigshop.bean.dto.setting.SmsSendDTO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.setting.SmsProviderEnum;
import com.tigshop.common.utils.SmsUtils;

import java.util.List;
import java.util.Map;

import static com.tigshop.bean.enums.setting.SettingsEnum.*;

/**
 * 短信通道路由工具（阿里云国内接口 / 国际接口）。
 *
 * 设计（CR-20260723：仅阿里云保留通道概念，单套账号）：
 * - 账号：四家服务商均只有一套账号；本类只负责把对应账号填入 dto。
 * - 通道：仅阿里云通过 config {@code smsAliyunChannel}（复选：1=+86国内、2=其他海外）
 *   决定走国内接口还是国际接口：
 *     · 同时含 1、2 → 按号码自动识别（海外号码走国际，其余走国内）；
 *     · 仅含 2 → 全部走国际；
 *     · 仅含 1 / 空 / 非法 → 全部走国内。
 * - 腾讯/华为/火山不受通道影响。
 *
 * 无 Spring 依赖（纯静态），由持有 ConfigService 的调用方加载 config 后调用，
 * 避免与 ConfigServiceImpl 形成循环依赖。
 *
 * @author Tigshop团队
 */
public final class SmsChannelHelper {

    /** 通道取值：+86 国内 */
    public static final String CHANNEL_DOMESTIC = "1";
    /** 通道取值：其他海外 */
    public static final String CHANNEL_OVERSEA = "2";

    private SmsChannelHelper() {
    }

    /**
     * 是否海外号码（空号视为国内，不误路由到国际接口）。
     */
    public static boolean isOverseaNumber(String mobile) {
        return mobile != null && !mobile.isBlank() && !SmsUtils.isChineseDomesticNumber(mobile);
    }

    /**
     * 阿里云通道决策：根据通道开关配置 + 号码，判定是否走国际接口。
     *
     * @param channelConfig config {@code smsAliyunChannel} 原始值（如 "1"、"2"、"1,2"）
     * @param mobile        目标号码
     * @return true=走海外通道（SendMessageToGlobe，明文 + SenderID）；false=走国内通道（SendSms，签名 + 模板）
     */
    public static boolean useInternationalChannel(String channelConfig, String mobile) {
        boolean domestic = containsChannel(channelConfig, CHANNEL_DOMESTIC);
        boolean oversea = containsChannel(channelConfig, CHANNEL_OVERSEA);
        if (domestic && oversea) {
            // 两者都勾：按号码自动识别
            return isOverseaNumber(mobile);
        }
        if (oversea) {
            // 仅勾海外：全部走国际
            return true;
        }
        // 仅勾国内 / 空 / 非法：全部走国内
        return false;
    }

    /**
     * 渲染海外明文文案：把模板中的 {@code ${key}} / {@code {key}} 占位符替换为业务参数值。
     * 用于 SendMessageToGlobe 的 Message（海外无模板ID，必须传明文）。
     *
     * @param template 带占位符的海外明文文案（message_template.content_oversea）
     * @param params   业务参数（key -> value），null 时不替换
     * @return 替换后的明文；template 为 null 时返回 null
     */
    public static String renderPlaintext(String template, Map<String, ?> params) {
        if (template == null || template.isBlank()) {
            return template;
        }
        String result = template;
        if (params != null) {
            for (Map.Entry<String, ?> e : params.entrySet()) {
                if (e.getKey() == null) {
                    continue;
                }
                String value = e.getValue() == null ? "" : String.valueOf(e.getValue());
                result = result.replace("${" + e.getKey() + "}", value)
                        .replace("{" + e.getKey() + "}", value);
            }
        }
        return result;
    }

    private static boolean containsChannel(String channelConfig, String target) {
        if (channelConfig == null || channelConfig.isBlank()) {
            return false;
        }
        for (String part : channelConfig.split(",")) {
            if (target.equals(part.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 该服务商账号相关的全部配置键（供调用方一次性加载）。
     */
    public static List<SettingsEnum> configKeys(SmsProviderEnum provider) {
        return switch (provider) {
            case ALIYUN -> List.of(SMS_KEY_ID, SMS_KEY_SECRET, SMS_SIGN_NAME, SMS_SENDER_ID);
            case TENCENT -> List.of(
                    SMS_TENCENT_SECRET_ID, SMS_TENCENT_SECRET_KEY, SMS_TENCENT_SDK_APP_ID, SMS_TENCENT_SIGN_NAME);
            case HUAWEI -> List.of(
                    SMS_HUAWEI_APP_KEY, SMS_HUAWEI_APP_SECRET, SMS_HUAWEI_CHANNEL, SMS_HUAWEI_SIGN_NAME, SMS_HUAWEI_ENDPOINT);
            case VOLCENGINE -> List.of(
                    SMS_VOLCENGINE_ACCESS_KEY, SMS_VOLCENGINE_SECRET_KEY, SMS_VOLCENGINE_SMS_ACCOUNT, SMS_VOLCENGINE_SIGN_NAME);
        };
    }

    /**
     * 为 dto 填充服务商账号配置（单套账号）。
     *
     * @param dto      待填充的发送参数
     * @param provider 服务商
     * @param cfg      已加载的配置 Map（key 为 bizCode），至少覆盖 {@link #configKeys} 返回的键
     */
    public static void fillProviderConfig(SmsSendDTO dto, SmsProviderEnum provider, Map<String, String> cfg) {
        switch (provider) {
            case ALIYUN -> {
                dto.setAccessKeyId(cfg.get(SMS_KEY_ID.getBizCode()));
                dto.setAccessKeySecret(cfg.get(SMS_KEY_SECRET.getBizCode()));
                dto.setSignName(cfg.get(SMS_SIGN_NAME.getBizCode()));
                dto.setSenderId(cfg.get(SMS_SENDER_ID.getBizCode()));
            }
            case TENCENT -> {
                dto.setAccessKeyId(cfg.get(SMS_TENCENT_SECRET_ID.getBizCode()));
                dto.setAccessKeySecret(cfg.get(SMS_TENCENT_SECRET_KEY.getBizCode()));
                dto.setSdkAppId(cfg.get(SMS_TENCENT_SDK_APP_ID.getBizCode()));
                dto.setSignName(cfg.get(SMS_TENCENT_SIGN_NAME.getBizCode()));
            }
            case HUAWEI -> {
                dto.setAccessKeyId(cfg.get(SMS_HUAWEI_APP_KEY.getBizCode()));
                dto.setAccessKeySecret(cfg.get(SMS_HUAWEI_APP_SECRET.getBizCode()));
                dto.setChannel(cfg.get(SMS_HUAWEI_CHANNEL.getBizCode()));
                dto.setSignName(cfg.get(SMS_HUAWEI_SIGN_NAME.getBizCode()));
                dto.setEndpoint(cfg.get(SMS_HUAWEI_ENDPOINT.getBizCode()));
            }
            case VOLCENGINE -> {
                dto.setAccessKeyId(cfg.get(SMS_VOLCENGINE_ACCESS_KEY.getBizCode()));
                dto.setAccessKeySecret(cfg.get(SMS_VOLCENGINE_SECRET_KEY.getBizCode()));
                dto.setSmsAccount(cfg.get(SMS_VOLCENGINE_SMS_ACCOUNT.getBizCode()));
                dto.setSignName(cfg.get(SMS_VOLCENGINE_SIGN_NAME.getBizCode()));
            }
        }
    }
}
