package com.tigshop.bean.utils;

import com.tigshop.bean.vo.order.OrderItemVO;
import com.tigshop.bean.vo.order.OrderVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 供应商订单商品价格组装工具。
 *
 * <p>订单商品的供货价必须使用下单时保存的快照；非后台上下文只保留销售价口径，
 * 避免将供应商成本信息返回给买家端。</p>
 */
public final class VendorOrderPriceCalculator {

    private static final int MONEY_SCALE = 2;

    private VendorOrderPriceCalculator() {
    }

    /**
     * 按订单和当前请求上下文组装供应商商品价格。
     *
     * @param order         订单
     * @param adminContext  是否为后台请求
     */
    public static void apply(OrderVO order, boolean adminContext) {
        if (order == null) {
            return;
        }
        List<OrderItemVO> items = order.getItems();
        if (items == null) {
            order.setSupplyTotalAmount(null);
            return;
        }
        BigDecimal supplyTotalAmount = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        boolean hasVendorLine = false;
        for (OrderItemVO item : items) {
            if (item == null) {
                continue;
            }
            // 仅决定是否暴露供货价独立字段；既有 price/subtotal 始终保持平台口径。
            // 行级 vendorId 兼容父订单/混合订单，不能用它改写既有金额字段。
            boolean vendorLine = (item.getVendorId() != null && item.getVendorId() > 0)
                    || (order.getVendorId() != null && order.getVendorId() > 0);
            if (!adminContext || !vendorLine) {
                item.setSupplyPrice(null);
                item.setSupplySubtotal(null);
                continue;
            }
            hasVendorLine = true;
            BigDecimal supplyPrice = scale(item.getVendorProductSupplyPrice());
            item.setSupplyPrice(supplyPrice);
            BigDecimal supplySubtotal = calculateSupplySubtotal(item.getPrice(), supplyPrice, item.getQuantity());
            item.setSupplySubtotal(supplySubtotal);
            supplyTotalAmount = supplyTotalAmount.add(supplySubtotal);
        }
        order.setSupplyTotalAmount(hasVendorLine ? supplyTotalAmount : null);
    }

    /**
     * 计算一行的供货价小计。没有历史供货价快照时回退到平台售价，
     * 与供应商端现有历史订单兼容策略一致。
     */
    public static BigDecimal calculateSupplySubtotal(BigDecimal platformPrice,
                                                      BigDecimal supplyPrice,
                                                      Integer quantity) {
        BigDecimal unitPrice = supplyPrice != null ? supplyPrice : platformPrice;
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal scale(BigDecimal value) {
        return value == null ? null : value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
