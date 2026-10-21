// **---------------------------------------------------------------------+
// ** 文件 -- ${DESCRIPTION}
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 回调
 *
 * @author Jayce
 * @create 2024年10月22日 13:50
 */
@Data
@Schema(description = "回调操作")
public class PayNotifyDTO {
    @Schema(description = "回调通知的唯一编号")
    private String id;

    @Schema(description = "回调通知的创建时间")
    private String createTime;

    @Schema(description = "事件类型")
    private String eventType;

    @Schema(description = "通知的资源数据类型，固定为encrypt-resource")
    private String resourceType;

    @Schema(description = "回调数据")
    private returnResource resource;

    @Schema(description = "微信支付对回调内容的摘要备注")
    private String summary;

    @Data
    @Schema(description = "回调数据")
    public static class returnResource {
        @Schema(description = "回调数据密文的加密算法类型，目前为AEAD_AES_256_GCM")
        private String algorithm;

        @Schema(description = "参与解密的附加数据，该字段可能为空")
        private String associatedData;

        @Schema(description = "参与解密的随机串。")
        private String nonce;

        @Schema(description = "回调数据密文签名，商户需要将此签名验签")
        private String originalType;

        @Schema(description = "Base64编码后的回调数据密文，商户需Base64解码并使用APIV3密钥解密")
        private String ciphertext;
    }

}
