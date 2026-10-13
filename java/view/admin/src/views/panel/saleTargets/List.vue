<template>
    <div class="container">
        <div class="content_wrapper">
            <a-spin :spinning="loading">
                <div class="card-menu">
                    <div class="card-menu-item">
                        <div class="card-menu-item-title">{{ $t('订单总数') }}</div>
                        <div class="card-menu-item-value">{{ filterState?.orderNum }}</div>
                    </div>
                    <div class="card-menu-item">
                        <div class="card-menu-item-title">{{ $t('订单商品总数') }}</div>
                        <div class="card-menu-item-value">{{ filterState?.orderProductNum }}</div>
                    </div>
                    <div class="card-menu-item">
                        <div class="card-menu-item-title">{{ $t('订单总金额') }}</div>
                        <div class="card-menu-item-value">{{ filterState?.orderTotalAmount }}</div>
                    </div>
                    <div class="card-menu-item">
                        <div class="card-menu-item-title">{{ $t('会员总数') }}</div>
                        <div class="card-menu-item-value">{{ filterState?.userNum }}</div>
                    </div>
                    <div class="card-menu-item">
                        <div class="card-menu-item-title">{{ $t('消费会员总数') }}</div>
                        <div class="card-menu-item-value">{{ filterState?.consumerMembershipNum }}</div>
                    </div>
                    <div class="card-menu-item">
                        <div class="card-menu-item-title">{{ $t('人均消费数') }}</div>
                        <div class="card-menu-item-value">{{ filterState?.capitaConsumption }}</div>
                    </div>
                    <div class="card-menu-item">
                        <div class="card-menu-item-title">{{ $t('访问数') }}</div>
                        <div class="card-menu-item-value">{{ filterState?.clickCount }}</div>
                    </div>
                    <div class="card-menu-item">
                        <div class="card-menu-item-title">{{ $t('访问转化率') }}</div>
                        <div class="card-menu-item-value">{{ filterState?.clickRate }}%</div>
                    </div>
                    <div class="card-menu-item">
                        <div class="card-menu-item-title">{{ $t('订单转化率') }}</div>
                        <div class="card-menu-item-value">{{ filterState?.orderRate }}%</div>
                    </div>
                    <div class="card-menu-item">
                        <div class="card-menu-item-title">{{ $t('消费会员比率') }}</div>
                        <div class="card-menu-item-value">{{ filterState?.consumerMembershipRate }}%</div>
                    </div>
                    <div class="card-menu-item">
                        <div class="card-menu-item-title">{{ $t('购买率') }}</div>
                        <div class="card-menu-item-value">{{ filterState?.purchaseRate }}%</div>
                    </div>
                </div>
            </a-spin>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from "vue";
import { getQuotaSales } from "@/api/panel/saleTargets";
import { FilterResult } from "@/types/panel/saleTargets.d";

const loading = ref(false);
const filterState = ref<FilterResult>({
    orderNum: "0",
    orderProductNum: "0",
    orderTotalAmount: "0",
    userNum: "0",
    consumerMembershipNum: "0",
    capitaConsumption: "0",
    clickCount: "0",
    clickRate: "0",
    orderRate: "0",
    consumerMembershipRate: "0",
    purchaseRate: "0"
});

const getData = async () => {
    loading.value = true;
    try {
        const result = await getQuotaSales();
        filterState.value = result;
    } catch (error) {
        console.error(error);
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    getData();
});
</script>

<style lang="less" scoped>
.card-menu {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    gap: 25px;
    .card-menu-item {
        width: 230px;
        padding: 20px;

        background-color: #f7f8fa;
        border-radius: 10px;

        .card-menu-item-title {
            font-size: 14px;
            color: #777;
        }
        .card-menu-item-value {
            font-size: 24px;
            color: #555;
            display: block;
            font-weight: bold;
            padding: 10px 0;
        }
    }
}
@media only screen and (max-width: 767px) {
    .card-menu-item {
        width: 100% !important;
    }
}
</style>
