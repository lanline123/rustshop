// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Objects;

import static com.tigshop.common.utils.TigUtils.getJarDir;

/**
 * @author Tigshop团队
 * @create 2024年11月11日 14:39
 */
@Service
public class PayServiceFactory {

    @Resource
    private ConfigService configService;

    //取配置的file_upload_dir
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 获取指定支付方式的 WxPayService
    public WxPayService getWxPayV3Service(String clientType) {
        // 根据支付方式类型从数据库中获取不同的配置信息
        String appId = switch (clientType) {
            case "wechat", "pc", "h5", "bar-code", "pos" -> {
                ConfigPO wechatAppIdConfig = configService.getConfigByCode(SettingsEnum.WECHAT_APP_ID.getBizCode());
                yield wechatAppIdConfig.getBizVal();
            }
            case "miniProgram" -> {
                ConfigPO wechatMiniProgramAppIdConfig = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID.getBizCode());
                yield wechatMiniProgramAppIdConfig.getBizVal();
            }
            case "app" -> {
                ConfigPO wechatPayAppIdConfig = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_APP_ID.getBizCode());

                yield wechatPayAppIdConfig.getBizVal();
            }
            default -> null;
        };

        // 根据支付方式选择不同的配置
        WxPayConfig wxPayConfig = new WxPayConfig();
        if (StrUtil.isNotBlank(appId)) {
            wxPayConfig.setAppId(appId);
        }
        ConfigPO wechatPayMchidConfig = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_MCHID.getBizCode());
        wxPayConfig.setMchId(wechatPayMchidConfig.getBizVal());
        // 获取当前 JAR 包所在目录
        String jarDir = getJarDir();

        // 在 JAR 目录下创建 uploadDir 文件夹
        File pemDir = FileUtil.mkdir(jarDir + File.separator + uploadDir);
        String absolutePath = pemDir.getAbsolutePath();

        // 打印路径
        System.out.println("PEM 目录路径: " + absolutePath);


        // 读取私钥文件
        wxPayConfig.setPrivateKeyPath(absolutePath + File.separator + "apiclient_key.pem");
        wxPayConfig.setPrivateCertPath(absolutePath + File.separator + "apiclient_cert.pem");


        ConfigPO wechatPaySerialNoConfig = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_SERIAL_NO.getBizCode());
        String configuredSerialNo = wechatPaySerialNoConfig != null ? wechatPaySerialNoConfig.getBizVal() : null;
        String certSerialNo = resolveCertSerialNo(configuredSerialNo,
                new File(absolutePath + File.separator + "apiclient_cert.pem"));
        if (StrUtil.isNotBlank(certSerialNo)) {
            wxPayConfig.setCertSerialNo(certSerialNo);
        }
        ConfigPO wechatPayKeyConfig = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_KEY.getBizCode());
        wxPayConfig.setApiV3Key(wechatPayKeyConfig.getBizVal());

        ConfigPO wechatPayCheckTypeConfig = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_CHECK_TYPE.getBizCode());
        if (Objects.equals(wechatPayCheckTypeConfig.getBizVal(), "2")) {
            try{
                ConfigPO wechatPayPublicKeyIdConfig = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_PUBLIC_KEY_ID.getBizCode());
                wxPayConfig.setPublicKeyId(wechatPayPublicKeyIdConfig.getBizVal());
            }catch (Exception e){

            }
            wxPayConfig.setPublicKeyPath(absolutePath + File.separator + "public_key.pem");
            try{
                ConfigPO wechatPayPublicKeyConfig = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_PUBLIC_KEY_ID.getBizCode());
                wxPayConfig.setPublicKeyId(wechatPayPublicKeyConfig.getBizVal());
            }catch (Exception e){

            }
        }

        ConfigPO wechatPayV2KeyPO = configService.getConfigByCode(SettingsEnum.WECHAT_PAY_V2_KEY.getBizCode());
        if (Objects.nonNull(wechatPayV2KeyPO)) {
            wxPayConfig.setMchKey(wechatPayV2KeyPO.getBizVal());
        }

        // 创建 WxPayService 实例并设置配置
        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(wxPayConfig);

        wxPayService.getConfig().getVerifier().getValidCertificate();

        return wxPayService;
    }

    /**
     * 解析 v3 请求签名用的商户证书序列号。
     * <p>已配置「商户证书序列号」时直接使用（去空白）；未配置时交由 wx-java 从商户 API 证书
     * {@code apiclient_cert.pem} 自动推导——但若该证书文件也不存在，则提前抛出可操作提示，
     * 避免最终只暴露微信「商户证书序列号有误」这类难以自查的报错。
     *
     * @param configuredSerialNo 后台配置的序列号，可为空或 {@code null}
     * @param apiClientCertFile  商户 API 证书文件（{@code apiclient_cert.pem}）
     * @return 需显式设置的序列号；返回 {@code null} 表示留空、交由 SDK 从证书推导
     */
    static String resolveCertSerialNo(String configuredSerialNo, File apiClientCertFile) {
        if (StrUtil.isNotBlank(configuredSerialNo)) {
            return configuredSerialNo.trim();
        }
        if (apiClientCertFile == null || !apiClientCertFile.exists()) {
            throw new GlobalException("微信支付未配置商户证书序列号，且未上传商户API证书(apiclient_cert.pem)，"
                    + "请在『支付设置』上传商户API证书或填写商户证书序列号后重试");
        }
        return null;
    }

    /**
     * 获取服务商（电商收付通）模式的 WxPayService。
     * <p>平台商户号、证书与密钥完全沿用 {@link #getWxPayV3Service(String)}，仅额外附加二级商户信息。
     * 二级商户号按订单解析后由调用方传入，不再读全局 {@code WECHAT_PAY_SUB_MCHID}——该配置在多店铺场景下
     * 无法表达「哪一笔进哪个二级商户」。供合单支付、普通服务商支付和服务商退款使用。
     *
     * @param clientType 客户端类型，取值与 {@link #getWxPayV3Service(String)} 一致
     * @param subMchId   二级商户号，必填
     * @param subAppId   二级商户绑定的 appId，可为空
     * @return 带子商户信息的 WxPayService
     */
    public WxPayService getWxPayV3PartnerService(String clientType, String subMchId, String subAppId) {
        if (StrUtil.isBlank(subMchId)) {
            throw new GlobalException("服务商模式支付缺少二级商户号");
        }
        WxPayService wxPayService = getWxPayV3Service(clientType);
        WxPayConfig wxPayConfig = wxPayService.getConfig();
        wxPayConfig.setSubMchId(subMchId);
        if (StrUtil.isNotBlank(subAppId)) {
            wxPayConfig.setSubAppId(subAppId);
        }
        return wxPayService;
    }

}
