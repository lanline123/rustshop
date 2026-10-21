// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.constant.msg;

import java.math.BigDecimal;

/**
 * 订单相关的后台消息（AdminMsg）标准文案模板。
 * <p>
 * <b>抽离背景（2026-04-22）</b>：{@code MessageServiceImpl} 里"新订单 / 已付款订单"两类 AdminMsg
 * 的 title / content 原本是硬编码散落在 4 段复制粘贴代码里。当打印链路也要复用同样的文案做
 * 云打印机语音播报（商米 {@code pushContent.media_text}）时，统一到此常量类，
 * 避免"后台通知改了，打印语音没改"这种跑偏。
 * <p>
 * <b>模板一览</b>：
 * <ul>
 *     <li>{@link #newOrderTitle(String, BigDecimal)} / {@link #newOrderContent(String)}
 *         —— {@code AdminMsgTypeEnum.NEW_ORDER(11)} 新订单消息</li>
 *     <li>{@link #paidOrderTitle(String, BigDecimal)} / {@link #paidOrderContent(String)}
 *         —— {@code AdminMsgTypeEnum.PAID_ORDER(12)} 已付款订单消息。<b>打印语音播报复用此 title</b>。</li>
 * </ul>
 *
 * @author Tigshop团队
 * @since 2026/4/22
 */
public final class OrderAdminMsgTemplate {

    private OrderAdminMsgTemplate() {
    }

    // ===========================================================
    // 新订单（AdminMsgTypeEnum.NEW_ORDER, msgType = 11）
    // ===========================================================

    /**
     * 新订单消息 title：{@code "您有新的订单：{orderSn}，金额：{totalAmount}"}
     */
    public static String newOrderTitle(String orderSn, BigDecimal totalAmount) {
        return "您有新的订单：" + safe(orderSn) + "，金额：" + safe(totalAmount);
    }

    /**
     * 新订单消息 content：{@code "您有新的订单：{orderSn}，请注意查看"}
     */
    public static String newOrderContent(String orderSn) {
        return "您有新的订单：" + safe(orderSn) + "，请注意查看";
    }

    // ===========================================================
    // 已付款订单（AdminMsgTypeEnum.PAID_ORDER, msgType = 12）
    // ===========================================================

    /**
     * 已付款订单消息 title：{@code "您的订单已支付完成：{orderSn}，金额：{totalAmount}"}
     * <p>
     * <b>商米打印机语音播报复用此 title 作为 {@code pushContent.media_text}</b>。
     * 触发时机：仅 {@code PrintTriggerTypeEnum.AUTO}（订单支付成功后的自动打印），手动补打不播。
     */
    public static String paidOrderTitle(String orderSn, BigDecimal totalAmount) {
        return "您的订单已支付完成：" + safe(orderSn) + "，金额：" + safe(totalAmount);
    }

    /**
     * 已付款订单消息 content：{@code "您的订单：【{orderSn}】已支付完成，请注意查看"}
     */
    public static String paidOrderContent(String orderSn) {
        return "您的订单：【" + safe(orderSn) + "】已支付完成，请注意查看";
    }

    // ===========================================================
    // 语音播报专用（voiceContentType 分支）
    // ===========================================================

    /**
     * 仅金额播报文案：{@code "收款{totalAmount}元"}
     * <p>
     * 对应 {@code voiceContentType=1}（仅金额）。
     */
    public static String paidOrderAmountOnly(BigDecimal totalAmount) {
        return "收款" + safe(totalAmount) + "元";
    }

    /**
     * 根据 voiceContentType 返回播报文案：
     * <ul>
     *   <li>{@code 1} → 仅金额：{@code "收款{totalAmount}元"}</li>
     *   <li>其他（含 null / 0）→ 订单号+金额：{@code "您的订单已支付完成：{orderSn}，金额：{totalAmount}"}</li>
     * </ul>
     */
    public static String paidOrderVoice(String orderSn, BigDecimal totalAmount, Integer voiceContentType) {
        if (Integer.valueOf(1).equals(voiceContentType)) {
            return paidOrderAmountOnly(totalAmount);
        }
        return paidOrderTitle(orderSn, totalAmount);
    }

    // ===========================================================
    // 内部工具
    // ===========================================================

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String safe(BigDecimal n) {
        return n == null ? "0" : n.toPlainString();
    }
}
