// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.enums.msg;


import lombok.Getter;

/**
 * @author Tigshop团队
 * @create 2025/5/26 10:31
 * 对应模板消息模块
 */
@Getter
public enum MessageTypeEnum {
    NEW_ORDER(1, "会员下单时发送短信"),
    ORDER_PAY(2, "用户支付成功通知"),
    ORDER_SHIPPING(3, "发货通知"),
    ORDER_REFUND(4, "退款成功通知"),
    NEW_ORDER_SHOP(5, "会员下单商家短信"),
    ORDER_PAY_SHOP(6, "会员支付完成商家短信"),
    ORDER_INVOICE(7, "发票开具成功短信通知"),
    VERIFICATION_CODE(8, "获取验证码"),
    USER_COMPANY_AUTH(9, "会员实名认证通知"),
    MERCHANT_SETTED_IN_SUCCESS(10, "商家入驻成功"),
    MERCHANT_SETTED_IN_FAIL(11, "商家入驻审核失败"),
    ;

    private final int id;
    private final String name;

    MessageTypeEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
