package com.tigshop.service.pay.applepay;

import cn.hutool.core.io.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

import static com.tigshop.common.utils.TigUtils.getJarDir;

/**
 * Apple Pay 证书文件定位器。
 *
 * <p>证书通过后台 {@code uploadFile?type=4|5|6} 上传，落盘于 {@code {jarDir}/{file.upload-dir}/} 下，
 * 文件名由 {@link com.tigshop.bean.enums.setting.PemFileType} 定义（与微信 apiclient_cert.pem 同套路）。</p>
 *
 * @author Tigshop团队
 */
@Component
public class ApplePayCertLocator {

    /** 商户身份证书文件名（PemFileType code=4） */
    public static final String MERCHANT_IDENTITY_CERT = "apple_pay_merchant_id.pem";
    /** 商户身份证书私钥文件名（PemFileType code=5） */
    public static final String MERCHANT_IDENTITY_KEY = "apple_pay_merchant_id_key.pem";
    /** 支付处理证书私钥文件名（PemFileType code=6） */
    public static final String PROCESSING_PRIVATE_KEY = "apple_pay_payment_processing_key.pem";

    @Value("${file.upload-dir}")
    private String uploadDir;

    private File certDir() {
        return FileUtil.mkdir(getJarDir() + File.separator + uploadDir);
    }

    public File merchantIdentityCertFile() {
        return new File(certDir(), MERCHANT_IDENTITY_CERT);
    }

    public File merchantIdentityKeyFile() {
        return new File(certDir(), MERCHANT_IDENTITY_KEY);
    }

    public File processingPrivateKeyFile() {
        return new File(certDir(), PROCESSING_PRIVATE_KEY);
    }
}
