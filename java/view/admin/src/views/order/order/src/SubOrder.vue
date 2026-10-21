<template>
    <div class="container-card">
        <div class="title">{{ $t('子订单列表') }}</div>

        <div class="table-container">
            <a-spin :spinning="loading">
                <!-- 表头：保持与 order/List.vue 一致（去掉多选/操作列） -->
                <table class="custom-table">
                    <thead>
                        <tr>
                            <th>{{ $t('订单信息(下单时间排序)') }}</th>
                                <th>{{ isVendorAdmin ? $t('小计') : $t('订单金额') }}</th>
                            <th>{{ $t('会员名称') }}</th>
                            <th>{{ $t('收货人信息') }}</th>
                            <th style="text-align: start;white-space: nowrap;">{{ $t('物流/支付信息') }}</th>
                            <th style="text-align: start">{{ $t('订单状态') }}</th>
                        </tr>
                    </thead>
                </table>

                <!-- 数据：每个订单一张表（与 List.vue 一致的结构/样式） -->
                <table v-for="item in list" :key="item.orderId" class="custom-table">
                    <thead>
                        <tr class="data-tr">
                            <th colspan="6">
                                <span class="check-box">{{ $t('订单编号：{orderSn}', { orderSn: item.orderSn }) }}</span>
                                <el-divider direction="vertical" />
                                <span class="check-box">{{ $t('下单时间：{addTime}', { addTime: item.addTime }) }}</span>
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(product, index) in item.items" :key="product.itemId || index">
                            <td>
                                <div class="displayRow flex flex-align-center flex-justify-between">
                                    <ProductCard
                                        :picThumb="product.picThumb"
                                        :productId="product.productId"
                                        :productName="product.productName"
                                        :productType="product.productType"
                                        :url="product.url"
                                        :skuData="product.skuData"
                                    />
                                    <div class="displayColumn textR whiteSN widthAuto">
                                        <div>{{ priceFormat(isVendorAdmin ? getSupplierOrderItemPrice(product) : product.price) }}</div>
                                        <div>× {{ product.quantity }}</div>
                                    </div>
                                </div>
                            </td>

                            <td v-if="index === 0" :rowspan="item.items.length">
                                <div class="displayColumn textR fz14">
                                    <div>{{ priceFormat(isVendorAdmin ? getSupplierOrderTotal(item) : item.totalAmount) }}</div>
                                    <div class="gray">{{ $t('(含{getOrderShippingFeeLabel}：{shippingFee})', { getOrderShippingFeeLabel: getOrderShippingFeeLabel(item), shippingFee: item.shippingFee }) }}</div>
                                </div>
                            </td>

                            <td v-if="index === 0" :rowspan="item.items.length">
                                <div class="displayColumn textL">
                                    <div>{{ item.user?.username || item.username || "--" }}</div>
                                </div>
                            </td>

                            <td v-if="index === 0" :rowspan="item.items.length">
                                <div class="displayColumn fz12 textL">
                                    <div class="gray"> {{ $t('收货人：') }}<span class="black">{{ maskString(item.consignee || "", 1, 1) || "--" }}</span>
                                    </div>
                                    <div class="gray"> {{ $t('手机号：') }} <span class="black">
                                            <MobileCard :mobile="item.mobile" />
                                        </span>
                                    </div>
                                    <div class="gray user-address">
                                        <span class="label"> {{ $t('收货地址：') }} </span>
                                        <span class="black line1 value">
                                            {{ maskString(item.userAddress || "", 16, 0) || "--" }}
                                        </span>
                                    </div>
                                </div>
                            </td>

                            <td v-if="index === 0" :rowspan="item.items.length">
                                <div class="displayColumn textL">
                                    <div>
                                        <span>{{ item.payTypeId === 1 ? $t('在线支付') : item.payTypeId === 2 ? $t('货到付款') : $t('线下支付') }}</span>
                                    </div>
                                    <div v-if="item.logisticsName">
                                        {{ item.shippingTypeName }}
                                        <span class="gray" v-if="item.shippingTypeId !== 3">{{ item.logisticsName }}</span>
                                    </div>
                                </div>
                            </td>

                            <td v-if="index === 0" :rowspan="item.items.length">
                                <span
                                    :class="{
                                        orange: item.orderStatus === 0,
                                        black: item.orderStatus === 1 || item.orderStatus === 2 || item.orderStatus === 4,
                                        gray: item.orderStatus === 3,
                                        green: item.orderStatus === 5
                                    }"
                                >
                                    {{ item.orderStatusName }}
                                </span>
                            </td>
                        </tr>
                    </tbody>
                </table>

                <div v-if="total === 0" class="empty-warp">
                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                </div>
            </a-spin>
        </div>

        <div v-if="total > 0" class="pagination-con">
            <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="load" />
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, reactive, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { Pagination, ProductCard } from "@/components/list";
import MobileCard from "@/components/list/src/MobileCard.vue";
import { getOrderList } from "@/api/order/order";
import { useConfigStore } from "@/store/config";
import type { OrderFilterParams, OrderFilterState } from "@/types/order/order.d";
import { priceFormat } from "@/utils/format";
import { maskString } from "@/utils/util";
import { getOrderShippingFeeLabel } from "@/utils/orderShippingFee";
import { getSupplierOrderItemPrice, getSupplierOrderItemSubtotal } from "@/utils/orderSupplierPrice";

const props = defineProps({
    orderIds: {
        type: Array as unknown as () => number[],
        default: () => []
    }
});

const config = useConfigStore();
const isVendorAdmin = localStorage.getItem("adminType") === "vendor";
const loading = ref(false);
const total = ref(0);
const list = ref<OrderFilterState[]>([]);

const filterParams = reactive<OrderFilterParams>({
    page: 1,
    size: config.get("pageSize"),
    sortField: "orderId",
    sortOrder: "desc"
});

const normalizeOrderIds = (ids: number[]) => {
    return (ids || []).filter((x) => typeof x === "number" && !Number.isNaN(x)).join(",");
};

const getSupplierOrderTotal = (item: any) =>
    Array.isArray(item?.items) ? item.items.reduce((total: number, product: any) => total + getSupplierOrderItemSubtotal(product), 0) : 0;

const load = async () => {
    const orderIds = normalizeOrderIds(props.orderIds);
    if (!orderIds) {
        list.value = [];
        total.value = 0;
        return;
    }

    loading.value = true;
    try {
        const result = await getOrderList({ ...filterParams, orderIds });
        list.value = result.records || [];
        total.value = result.total || 0;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

watch(
    () => props.orderIds,
    () => {
        filterParams.page = 1;
        load();
    },
    { deep: true }
);

onMounted(() => {
    load();
});
</script>

<style lang="less" scoped>
.container-card {
    border: 1px solid #ececec;
    box-shadow: 2px 2px 5px rgba(203, 193, 193, 0.2);
    border-radius: 2px;
    padding: 20px;
    background: #fff;
    margin-bottom: 20px;
}

.title {
    font-size: 16px;
    line-height: 25px;
    color: #323233;
    margin-bottom: 15px;
    font-weight: 700;
}

/* 下面这段 table 样式直接对齐 order/List.vue 的 custom-table 视觉（去掉多选列/操作列后仍保持一致） */
.table-container {
    .custom-table {
        width: 100%;
        margin-bottom: 20px;
        background-color: #f5f6fa;
        border: 1px solid #f0f0f0;
        border-collapse: collapse;
        height: 60px;

        thead,
        tbody {
            tr {
                height: 45px;
                border-top: 1px solid #f0f0f0;
                border-right: 1px solid #f0f0f0;
            }

            th,
            td {
                padding: 15px 12px;
                vertical-align: middle;
                border-bottom: 1px solid #f0f0f0;

                &:first-child {
                    width: 30%;
                    min-width: 260px;
                    border-left: 1px solid #f0f0f0;
                    text-align: start;
                }

                &:nth-child(2) {
                    width: 12%;
                }

                &:nth-child(3) {
                    width: 12%;
                }

                &:nth-child(4) {
                    width: 24%;
                }

                &:nth-child(5) {
                    width: 12%;
                }

                &:nth-child(6) {
                    width: 10%;
                }
            }

            .data-tr {
                height: 45px;

                th {
                    font-weight: normal;
                    text-align: start;
                }
            }

            td {
                background-color: white;
                border-right: 1px solid #f0f0f0;
            }
        }

        .check-box {
            vertical-align: middle;
        }

        .textR {
            text-align: right;
        }

        .textL {
            text-align: left;
        }

        .displayRow {
            display: flex;
            flex-direction: row;
            gap: 8px;
        }

        .displayColumn {
            display: flex;
            flex-direction: column;
        }

        .whiteSN {
            white-space: nowrap;
        }

        .widthAuto {
            width: auto;
        }

        .fz12 {
            font-size: 12px;
        }

        .fz14 {
            font-size: 14px;
        }

        .black {
            color: #333333;
        }

        .orange {
            color: #ed6a0c;
        }

        .gray {
            color: #999999;
        }

        .green {
            color: #00a870;
        }
    }
}
</style>
