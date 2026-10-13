<template>
    <div class="order-template">
        <div class="title-row">
            <div class="shop-tit" v-if="order.shop && order.shop.shopTitle">
                {{ order.shop.shopTitle || "-" }}
            </div>
            <div class="order-sn">{{ $t('订单编号：{orderSn}', { orderSn: order.orderSn }) }}</div>
        </div>
        <div class="user-info">
            <template v-if="!order.isPickup">
                <div class="item">
                    <div class="label">{{ $t('收货人：') }}</div>
                    <div class="value" >{{ order.consignee || "-" }}</div>
                </div>
                <div class="item">
                    <div class="label">{{ $t('收货地址：') }}</div>
                    <div class="value">{{ order.userAddress || "-" }}</div>
                </div>
            </template>
            <template v-if="order.isPickup">
                <div class="item">
                    <div class="label">{{ $t('收货人：') }}</div>
                    <div class="value" >{{ "-" }}</div>
                </div>
                <div class="item" v-if="order.pickupDetail">
                    <div class="label">{{ $t('自提点：') }}</div>
                    <div class="value">{{ order.pickupDetail.shopRegionNames.join('') }}{{ order.pickupDetail.address }}</div>
                </div>
            </template>

            <div class="item" v-if="!order.isPickup">
                <div class="label">{{ $t('配送时间：') }}</div>
                <div class="value">{{ $t('尽快送达') }}</div>
            </div>
            <div class="item" v-if="order.isPickup">
                <div class="label">{{ $t('自提时间：') }}</div>
                <div class="value">{{ order.expectPickupTime }}</div>
            </div>
            <div class="item">
                <div class="label">{{ $t('买家昵称：') }}</div>
                <div class="value">{{ order.user.username || $t('匿名') }}</div>
            </div>
            <div class="item">
                <div class="label">{{ $t('下单时间：') }}</div>
                <div class="value">{{ order.addTime || "-" }}</div>
            </div>
            <div class="item">
                <div class="label">{{ $t('支付时间：') }}</div>
                <div class="value">{{ order.payTime || "-" }}</div>
            </div>
            <div class="item">
                <div class="label">{{ $t('支付方式：') }}</div>
                <div class="value">{{ order.payTypeId === 1 ? $t('在线支付') : order.payTypeId === 2 ? $t('货到付款') : $t('线下支付') }}</div>
            </div>
            <div class="item">
                <div class="label">{{ $t('买家备注：') }}</div>
                <div class="value">{{ order.buyerNote || "-" }}</div>
            </div>
        </div>
        <table border="1">
            <tbody>
                <tr align="center" class="firstRow">
                    <td align="center">
                        <span>{{ $t('序号') }}</span>
                    </td>
                    <td align="center">
                        <span>{{ $t('商品名称') }}</span>
                    </td>
                    <td align="center">
                        <span>{{ $t('商品规格') }}</span>
                    </td>
                    <td align="center">
                        <span>{{ $t('商品编号') }}</span>
                    </td>
                    <td align="center">
                        <span>{{ $t('商品编码') }}</span>
                    </td>
                    <td align="center">
                        <span>{{ $t('单价') }}</span>
                    </td>
                    <td align="center">
                        <span>{{ $t('数量') }}</span>
                    </td>
                    <td align="center">
                        <span>{{ $t('小计') }}</span>
                    </td>
                </tr>
                <tr v-for="(item, index) in order.items">
                    <td align="center">
                        {{ index + 1 }}
                    </td>
                    <td>
                        {{ item.productName }}
                    </td>
                    <td>
                        {{ item.skuValue || $t('无规格') }}
                    </td>
                    <td align="center">
                        <span>{{ item.productSn }} </span>
                    </td>
                    <td align="center">
                        <span style="font-size: 12px">
                            {{ item.skuSn || "-" }}
                        </span>
                    </td>
                    <td align="center">
                        <span style="font-weight: 700">
                            {{ priceFormat(item.price) }}
                        </span>
                    </td>
                    <td align="center">
                        <span>{{ item.quantity }}</span>
                    </td>
                    <td align="center">
                        <span style="font-weight: 700">
                            {{ priceFormat(item.subtotal) }}
                        </span>
                    </td>
                </tr>
                <tr>
                    <td colspan="9">
                        <div class="order-info">
                            <div class="item">
                                <div class="label">{{ $t('商品总数：') }}</div>
                                <div class="value">{{ totalQuantity(order.items) }}</div>
                            </div>
                            <div class="item">
                                <div class="label">{{ $t('商品总价：') }}</div>
                                <div class="value">{{ priceFormat(order.productAmount) }}</div>
                            </div>
                            <div class="item">
                                <div class="label">{{ $t('运费：') }}</div>
                                <div class="value">{{ priceFormat(order.shippingFee) }}</div>
                            </div>
                            <div class="item">
                                <div class="label">{{ $t('附加费用：') }}</div>
                                <div class="value">{{ priceFormat(order.serviceFee) }}</div>
                            </div>
                            <div class="item">
                                <div class="label">{{ $t('优惠券：') }}</div>
                                <div class="value">{{ priceFormat(order.couponAmount) }}</div>
                            </div>
                            <div class="item">
                                <div class="label">{{ $t('实付金额：') }}</div>
                                <div class="value">{{ priceFormat(order.totalAmount) }}</div>
                            </div>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
        <div class="tips" v-if="printMode === 'one'">{{ $t('{shopName} {host}打印时间：{printTime}', { shopName: order.shopName, host: order.host, printTime: order.printTime }) }}</div>
    </div>
</template>

<script setup>
import { priceFormat } from "@/utils/format";
const totalQuantity = (items) => {
    let total = 0;
    items.forEach((item) => {
        total += item.quantity;
    });
    return total;
};
defineProps({
    order: Object,
    printMode: String
});
</script>
