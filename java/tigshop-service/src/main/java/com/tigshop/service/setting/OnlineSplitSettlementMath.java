package com.tigshop.service.setting;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 线上分账结算账务纯函数（CR-20260731-131859）。
 * <p>平台余额 portion = paidAmount − wechatOnline；二级户 portion 不进 shopMoney/vendorMoney。
 */
public final class OnlineSplitSettlementMath {

    private OnlineSplitSettlementMath() {
    }

    /**
     * 平台余额 portion = max(0, paidAmount − wechatOnline)。
     */
    public static BigDecimal balancePortion(BigDecimal paidAmount, BigDecimal wechatOnline) {
        BigDecimal paid = nullToZero(paidAmount);
        BigDecimal online = nullToZero(wechatOnline);
        return paid.subtract(online).max(BigDecimal.ZERO);
    }

    /**
     * 结算时平台侧净入账：余额 portion 减去「线上优先」后落在余额上的退款，max(0)。
     * <p>退款优先冲减二级户实收，超出部分才冲余额。
     */
    public static BigDecimal shopMoneyNetOnSettle(BigDecimal paidAmount, BigDecimal wechatOnline, BigDecimal totalRefund) {
        BigDecimal balance = balancePortion(paidAmount, wechatOnline);
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal balanceRefund = nullToZero(totalRefund).subtract(nullToZero(wechatOnline)).max(BigDecimal.ZERO);
        return balance.subtract(balanceRefund).max(BigDecimal.ZERO);
    }

    /**
     * 退款镜像：本笔店铺退款中应扣 shopMoney 的余额份额（按 paidAmount 中余额占比分摊，不扣服务费）。
     */
    public static BigDecimal shopMoneyDecrOnRefund(BigDecimal shopRefundAmount, BigDecimal paidAmount, BigDecimal wechatOnline) {
        BigDecimal refund = nullToZero(shopRefundAmount);
        if (refund.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal paid = nullToZero(paidAmount);
        BigDecimal balance = balancePortion(paid, wechatOnline);
        if (balance.compareTo(BigDecimal.ZERO) <= 0 || paid.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return refund.multiply(balance).divide(paid, 2, RoundingMode.HALF_UP);
    }

    /**
     * 供应商平台 portion = vendorAmount × balancePortion / paidAmount（纯二级户 → 0）。
     */
    public static BigDecimal vendorPlatformPortion(BigDecimal vendorAmount, BigDecimal paidAmount, BigDecimal wechatOnline) {
        BigDecimal vendor = nullToZero(vendorAmount);
        BigDecimal paid = nullToZero(paidAmount);
        BigDecimal balance = balancePortion(paid, wechatOnline);
        if (vendor.compareTo(BigDecimal.ZERO) <= 0 || paid.compareTo(BigDecimal.ZERO) <= 0
                || balance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return vendor.multiply(balance).divide(paid, 2, RoundingMode.HALF_UP);
    }

    /**
     * 供应商退款中应扣 vendorMoney 的平台份额。
     */
    public static BigDecimal vendorMoneyDecrOnRefund(BigDecimal vendorRefundAmount, BigDecimal paidAmount, BigDecimal wechatOnline) {
        return shopMoneyDecrOnRefund(vendorRefundAmount, paidAmount, wechatOnline);
    }

    private static BigDecimal nullToZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
