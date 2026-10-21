package com.tigshop.bean.enums.msg;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 管理员消息分类
 *
 * @author tigshop
 * @create 2024-12-25
 */
@Getter
public enum AdminMsgTypeEnum {
    TRANSACTION(1, "交易消息", 0, true, true),
    PRODUCT(2, "商品消息", 0, true, true),
    AFTER_SALE(3, "售后服务", 0, true, true),
    FINANCE(6, "财务消息", 0, true, true),
    SHOP_SERVICE(4, "店铺服务", 0, true, false),
    OTHER(5, "其它消息", 0, true, true),

    NEW_ORDER(11, "新订单", 1, true, true),
    PAID_ORDER(12, "已付款订单", 1, true, true),
    ORDER_COMPLETED(13, "订单完成", 1, true, true),
    // 历史兼容：订单问题/订单咨询不再作为消息中心分类展示，保留编号供旧消息和接口兼容。
    ORDER_PROBLEM(15, "订单问题", 1, false, false),
    ORDER_CONSULTATION(16, "订单咨询", 1, false, false),
    QUICK_DELIVERY(54, "发货提醒", 1, true, true),
    STOCK_WARNING(21, "商品库存预警", 2, true, true),
    OUT_OF_STOCK(22, "商品无货", 2, true, true),
    PRODUCT_OUT_OF_STOCK(23, "商品下架", 2, true, true),
    PRODUCT_AUDIT_NOTIFICATION(24, "商品审核通知", 2, true, true),
    PRODUCT_CONSULTATION(25, "商品咨询", 2, true, true),
    PRODUCT_INQUIRY(26, "商品询价", 2, true, true),
    ORDER_CANCELED(31, "订单取消", 1, true, true),
    AFTER_SALE_REQUEST(32, "售后申请", 3, true, true),
    WITHDRAWAL_REQUEST(33, "提现申请", 6, true, true),
    INVOICE_QUALIFICATION_AUDIT(34, "发票资质审核", 6, true, true),
    INVOICE_REQUEST(35, "发票申请", 6, true, true),
    REFUND(36, "售后退款", 6, true, true),
    OFFLINE_PAYMENT_AUDIT(37, "线下支付审核", 6, true, true),
    SHOP_ENTRY_APPLICATION(41, "店铺入驻申请", 4, true, false),
    SHOP_QUALIFICATION_MODIFICATION(42, "店铺资质修改", 4, true, false),
    SHOP_VIOLATION(43, "店铺违规", 4, true, false),
    DEPOSIT_WARNING(44, "保证金预警", 6, true, true),
    SYSTEM_MESSAGE(51, "系统消息", 5, true, true),
    PENDING_TASK(52, "待办任务", 5, true, true),
    FEEDBACK(53, "意见反馈", 5, true, true),
    SUGGESTION(55, "建议", 5, true, true),
    COMPLAINT(56, "投诉", 5, true, true),
    OTHER_FEEDBACK(57, "其它反馈", 5, true, true);

    private final int catId;
    private final String catName;
    private final int parentId;
    private final boolean shopShow;
    private final boolean vendorShow;

    AdminMsgTypeEnum(int catId, String catName, int parentId, boolean shopShow, boolean vendorShow) {
        this.catId = catId;
        this.catName = catName;
        this.parentId = parentId;
        this.shopShow = shopShow;
        this.vendorShow = vendorShow;
    }

    public static Optional<AdminMsgTypeEnum> getByCatId(Integer catId) {
        return catId == null ? Optional.empty()
                : Arrays.stream(values()).filter(type -> type.catId == catId).findFirst();
    }

}
