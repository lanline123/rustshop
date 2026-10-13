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

import lombok.Getter;

/**
 * 邮件模板类型
 *
 * @author 邮件模板实体类
 * @create  2024-12-25
 */
@Getter
public enum MailTemplateType {
    SEND_PASSWORD("send_password", "发送密码模板 [send_password]"),
    ORDER_CONFIRM("order_confirm", "订单确认模板 [order_confirm]"),
    DELIVER_NOTICE("deliver_notice", "发货通知模板 [deliver_notice]"),
    ORDER_CANCEL("order_cancel", "订单取消模板 [order_cancel]"),
    ORDER_INVALID("order_invalid", "订单无效模板 [order_invalid]"),
    SEND_BONUS("send_bonus", "发送红包模板 [send_bonus]"),
    GROUP_BUY("group_buy", "团购商品模板 [group_buy]"),
    REGISTER_VALIDATE("register_validate", "邮件验证模板 [register_validate]"),
    VIRTUAL_CARD("virtual_card", "虚拟卡片模板 [virtual_card]"),
    ATTENTION_LIST("attention_list", "关注管理 [attention_list]"),
    REMIND_OF_NEW_ORDER("remind_of_new_order", "新订单提醒模板 [remind_of_new_order]"),
    GOODS_BOOKING("goods_booking", "缺货回复模板 [goods_booking]"),
    USER_MESSAGE("user_message", "留言回复模板 [user_message]"),
    RECOMMENT("recomment", "用户评论回复模板 [recomment]"),
    ORDER_PAY_EMAIL("order_pay_email", "订单付款通知 [order_pay_email]"),
    REGISTER_CODE("register_code", "登录注册验证码 [register_code]");

    private final String code;
    private final String description;

    MailTemplateType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String fromCode(String code) {
        for (MailTemplateType type : MailTemplateType.values()) {
            if (type.getCode().equals(code)) {
                return type.description;
            }
        }
        return null;
    }
}