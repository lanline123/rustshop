// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 短信发送参数
 * @author Tigshop团队
 * @create 2025年03月05日 17:52
 */
@Data
@Schema(description = "短信发送参数")
public class SmsSendDTO implements Serializable {

    @Schema(description = "短信服务商；1-阿里云，2-腾讯云，3-华为云，4-火山引擎")
    private Integer smsProvider;

    @Schema(description = "accessKeyId / SecretId / AppKey")
    private String accessKeyId;

    @Schema(description = "accessKeySecret / SecretKey / AppSecret")
    private String accessKeySecret;

    @Schema(description = "手机号码")
    private String phoneNumbers;

    @Schema(description = "短信签名")
    private String signName;

    @Schema(description = "短信模板")
    private String templateCode;

    @Schema(description = "短信模板参数")
    private String templateParam;

    @Schema(description = "腾讯云SDK AppID")
    private String sdkAppId;

    @Schema(description = "华为云短信通道号")
    private String channel;

    @Schema(description = "华为云接入地址")
    private String endpoint;

    @Schema(description = "火山引擎短信应用ID")
    private String smsAccount;

    @Schema(description = "国际短信明文内容（阿里云海外通道 SendMessageToGlobe 的 Message，已完成占位符替换）")
    private String messageContent;

    @Schema(description = "国际短信类型：OTP/NOTIFY/MKT（阿里云海外通道用）")
    private String internationalMessageType;

    @Schema(description = "国际短信发送方标识 SenderID（阿里云海外通道 SendMessageToGlobe 的 From）")
    private String senderId;

    @Schema(description = "阿里云是否走海外通道；true=SendMessageToGlobe（明文），false/null=SendSms（模板）")
    private Boolean useInternational;

    public SmsSendDTO() {
    }

    public SmsSendDTO(String accessKeyId, String accessKeySecret, String phoneNumbers, String signName, String templateCode, String templateParam) {
        this.smsProvider = 1;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.phoneNumbers = phoneNumbers;
        this.signName = signName;
        this.templateCode = templateCode;
        this.templateParam = templateParam;
    }
}