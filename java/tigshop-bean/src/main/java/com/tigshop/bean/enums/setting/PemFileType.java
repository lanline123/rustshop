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

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.enums.setting;

import com.tigshop.common.exception.GlobalException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 密钥文件名称类型
 *
 * @author Tigshop团队
 * @create  2024/12/26
 */
@Schema(description = "密钥文件名称类型")
@Getter
public enum PemFileType {

    API_CLIENT_CERT(1, "apiclient_cert.pem"),
    API_CLIENT_KEY(2, "apiclient_key.pem"),
    PUBLIC_KEY(3, "public_key.pem"),
    APPLE_PAY_MERCHANT_IDENTITY_CERT(4, "apple_pay_merchant_id.pem"),
    APPLE_PAY_MERCHANT_IDENTITY_KEY(5, "apple_pay_merchant_id_key.pem"),
    APPLE_PAY_PROCESSING_PRIVATE_KEY(6, "apple_pay_payment_processing_key.pem");

    private final int code;
    private final String description;

    PemFileType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PemFileType fromCode(Integer code) {
        for (PemFileType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new GlobalException("错误的文件");
    }
}