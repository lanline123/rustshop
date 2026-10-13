// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.enums.setting;

import lombok.Getter;

/**
 * @author Tigshop团队
 * @create 2025年03月04日 14:36
 */
@Getter
public enum MessageTemplateType {

    USER_ORDER(1, "user_order", "用户下单"),
    USER_PAY(2, "user_pay", "用户支付"),
    USER_SHIPPING(3, "user_shipping", "用户收货"),
    SHOP_ORDER(5, "shop_order", "商家下单"),
    SHOP_PAY(6, "shop_pay", "商家支付"),
    INVOICE(7, "invoice", "发票"),
    MSG_CODE(8, "code", "短信验证码"),
    USER_CERTIFICATION(9, "user_certification", "用户认证"),
    MERCHANT_APPLY_SUCCESS(10, "merchant_apply_success", "商家申请成功"),
    MERCHANT_APPLY_REFUSE(11, "merchant_apply_refuse", "商家申请拒绝");

    private final int messageId;
    private final String code;
    private final String desc;

    MessageTemplateType(int messageId, String code, String desc) {
        this.messageId = messageId;
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     * @param code code
     * @return 枚举
     */
    public static MessageTemplateType getByCode(String code) {
        for (MessageTemplateType messageTemplateType : values()) {
            if (messageTemplateType.getCode().equals(code)) {
                return messageTemplateType;
            }
        }
        return null;
    }
}
