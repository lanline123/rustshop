// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.utils;

import com.aliyun.teaopenapi.models.Config;
import com.tigshop.common.exception.GlobalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * 阿里云短信工具类。
 *
 * 路由策略（CR-20260727-135600）：
 * - 国内号码 / 国内通道：{@link #sendTemplateSms} → SDK {@code com.aliyun:dysmsapi20170525} 的 SendSms，
 *   endpoint {@code dysmsapi.aliyuncs.com}，签名 + 国内模板 Code + 参数。
 * - 海外号码 / 海外通道：{@link #sendGlobeSms} → SDK {@code com.aliyun:dysmsapi20180501} 的 SendMessageToGlobe，
 *   endpoint {@code dysmsapi.ap-southeast-1.aliyuncs.com}，SenderID(From) + 明文 Message + Type。
 *
 * 说明：阿里云 SendSms 不支持国际短信、SendMessageToGlobe 不支持中国内地，两者不可混用；
 * 海外无"模板ID"概念，必须传明文内容。
 *
 * @author Tigshop团队
 */
public class SmsUtils {

    private static final Logger log = LoggerFactory.getLogger(SmsUtils.class);

    private static final String DOMESTIC_ENDPOINT = "dysmsapi.aliyuncs.com";
    private static final String INTERNATIONAL_ENDPOINT = "dysmsapi.ap-southeast-1.aliyuncs.com";
    private static final String DEFAULT_INTERNATIONAL_TYPE = "NOTIFY";

    private static final Pattern CHINA_MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern CHINA_MOBILE_WITH_CODE_PATTERN = Pattern.compile("^86[1][3-9]\\d{9}$");

    /**
     * 判断是否为中国大陆手机号
     */
    public static boolean isChineseDomesticNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return false;
        }
        String cleaned = phoneNumber.replace("+", "").replace(" ", "").replace("-", "");
        return CHINA_MOBILE_PATTERN.matcher(cleaned).matches()
                || CHINA_MOBILE_WITH_CODE_PATTERN.matcher(cleaned).matches();
    }

    /**
     * 统一的短信发送结果
     */
    public static class AliyunSmsSendResult {
        private final boolean success;
        private final String code;
        private final String message;

        public AliyunSmsSendResult(boolean success, String code, String message) {
            this.success = success;
            this.code = code;
            this.message = message;
        }

        public static AliyunSmsSendResult ok() { return new AliyunSmsSendResult(true, "OK", "发送成功"); }
        public static AliyunSmsSendResult fail(String code, String message) { return new AliyunSmsSendResult(false, code, message); }

        public boolean isSuccess() { return success; }
        public String getCode() { return code; }
        public String getMessage() { return message; }
    }

    // ============================== 国内短信（SendSms 2017-05-25） ==============================

    /**
     * 发送国内短信（模板方式）。
     *
     * @param accessKeyId     AccessKey Id
     * @param accessKeySecret AccessKey Secret
     * @param phoneNumbers    中国大陆手机号
     * @param signName        短信签名
     * @param templateCode    国内短信模板 Code
     * @param templateParam   模板参数 JSON（如 {"code":"123456"}）
     */
    public static AliyunSmsSendResult sendTemplateSms(String accessKeyId,
                                                      String accessKeySecret,
                                                      String phoneNumbers,
                                                      String signName,
                                                      String templateCode,
                                                      String templateParam) {
        log.info("阿里云国内短信(SendSms) -> endpoint: {}, 号码: {}, 签名: {}, 模板: {}",
                DOMESTIC_ENDPOINT, phoneNumbers, signName, templateCode);
        try {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret);
            config.endpoint = DOMESTIC_ENDPOINT;

            com.aliyun.dysmsapi20170525.Client client = new com.aliyun.dysmsapi20170525.Client(config);

            com.aliyun.dysmsapi20170525.models.SendSmsRequest request =
                    new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                            .setPhoneNumbers(phoneNumbers)
                            .setSignName(signName)
                            .setTemplateCode(templateCode)
                            .setTemplateParam(templateParam);

            com.aliyun.dysmsapi20170525.models.SendSmsResponse response = client.sendSms(request);
            String code = response.getBody().getCode();
            String message = response.getBody().getMessage();
            if ("OK".equals(code)) {
                return AliyunSmsSendResult.ok();
            }
            return AliyunSmsSendResult.fail(code, message);
        } catch (Exception e) {
            log.error("阿里云国内短信发送异常：{}", e.getMessage(), e);
            throw new GlobalException(e.getMessage());
        }
    }

    // ============================ 国际/港澳台短信（SendMessageToGlobe 2018-05-01） ============================

    /**
     * 发送国际/港澳台短信（明文方式）。
     *
     * @param accessKeyId     AccessKey Id
     * @param accessKeySecret AccessKey Secret
     * @param phoneNumbers    国际区号+号码（如 85290550955，不带 +）
     * @param senderId        发送方标识 SenderID（From，可空；部分国家需报备）
     * @param message         明文短信内容（已完成占位符替换）
     * @param type            短信类型 OTP/NOTIFY/MKT（空则默认 NOTIFY）
     */
    public static AliyunSmsSendResult sendGlobeSms(String accessKeyId,
                                                   String accessKeySecret,
                                                   String phoneNumbers,
                                                   String senderId,
                                                   String message,
                                                   String type) {
        String msgType = (type == null || type.isBlank()) ? DEFAULT_INTERNATIONAL_TYPE : type;
        log.info("阿里云国际短信(SendMessageToGlobe) -> endpoint: {}, 号码: {}, SenderID: {}, 类型: {}",
                INTERNATIONAL_ENDPOINT, phoneNumbers, senderId, msgType);
        try {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret);
            config.endpoint = INTERNATIONAL_ENDPOINT;

            com.aliyun.dysmsapi20180501.Client client = new com.aliyun.dysmsapi20180501.Client(config);

            com.aliyun.dysmsapi20180501.models.SendMessageToGlobeRequest request =
                    new com.aliyun.dysmsapi20180501.models.SendMessageToGlobeRequest()
                            .setTo(phoneNumbers)
                            .setMessage(message)
                            .setType(msgType);
            if (senderId != null && !senderId.isBlank()) {
                request.setFrom(senderId);
            }

            com.aliyun.dysmsapi20180501.models.SendMessageToGlobeResponse response = client.sendMessageToGlobe(request);
            String code = response.getBody().getResponseCode();
            String desc = response.getBody().getResponseDescription();
            if ("OK".equals(code)) {
                return AliyunSmsSendResult.ok();
            }
            return AliyunSmsSendResult.fail(code, desc);
        } catch (Exception e) {
            log.error("阿里云国际短信发送异常：{}", e.getMessage(), e);
            throw new GlobalException(e.getMessage());
        }
    }
}
