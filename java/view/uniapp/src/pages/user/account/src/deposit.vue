<template>
    <view class="recharge-box">
        <view v-if="filterState.length > 0" class="recharge-wrap">
            <block v-for="(item, index) in filterState" :key="index">
                <view v-if="item.rechargeId != 0" class="recharge-item" :class="{ selected: item.selected }" @click="selectCard(index)">
                    <view class="recharge-amount">
                        <format-price
                            base
                            :show-text="false"
                            :price-data="item.money"
                            :decimals-style="{
                                fontSize: '25rpx'
                            }"
                            :currency-style="{
                                fontSize: '23rpx'
                            }"
                        />
                    </view>
                    <view v-if="Number(item.discountMoney) > 0" class="recharge-give">
                        {{ $t("赠送") }}
                        <format-price base :show-text="false" :price-data="item.discountMoney" :is-bottom="false" :font-style="{ fontSize: '11px' }" />
                    </view>
                </view>
                <view v-else class="recharge-item" :class="{ selected: item.selected }" @click="selectCard(index)">
                    <view class="recharge-amount">
                        <block v-if="item.selected">
                            <up-input v-model="pay.amount" type="digit" color="var(--color)" input-align="center" border="none" />
                        </block>
                        <block v-else>
                            <view style="height: 42rpx" />
                        </block>
                    </view>
                    <view class="recharge-give">{{ $t("自定义金额") }}</view>
                </view>
            </block>
        </view>
    </view>
    <tig-fixed-placeholder background-color="#fff">
        <view class="btn-box">
            <tig-button class="btn" plain @click="backDetail"> {{ $t("取消充值") }} </tig-button>
            <tig-button class="btn" @click="onSubmit"> {{ $t("提交申请") }} </tig-button>
        </view>
    </tig-fixed-placeholder>
</template>

<script lang="ts" setup>
/**
 * 余额充值组件
 * - 展示预设充值金额选项（含赠送金额）
 * - 支持自定义金额输入
 * - 提交后跳转支付页面
 */
import { ref, onBeforeMount } from "vue";
import { getDepositList, updateRechargeOrder } from "@/api/user/account";
import type { DepositFilterState } from "@/types/user/account";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const filterState = ref<DepositFilterState[]>([]);
const __getDepositList = async () => {
    uni.showLoading({
        title: t("加载中")
    });
    try {
        const result = await getDepositList();
        filterState.value = result;
        //自定义金额
        let temp: any = {
            rechargeId: 0
        };
        filterState.value.push(temp);
        filterState.value.forEach((item: any) => {
            item.selected = false;
        });
    } catch (error: any) {
        console.log(error.message);
    } finally {
        uni.hideLoading();
    }
};
onBeforeMount(() => {
    __getDepositList();
});

const inputFocus = ref(false);
const selectCard = (index: number) => {
    index === 5 ? (inputFocus.value = true) : (inputFocus.value = false);
    filterState.value.forEach((item, idx) => {
        item.selected = idx === index;
    });
};

const pay = ref<any>({
    id: 0,
    amount: 0,
    money: "",
    payType: "recharge"
});
const onSubmit = () => {
    let flag = false;
    filterState.value.forEach((item, idx) => {
        if (item.selected) {
            flag = true;
            pay.value.id = item.rechargeId;
            if (item.rechargeId != 0) {
                pay.value.amount = null;
                pay.value.money = item.money;
            } else {
                pay.value.money = pay.value.amount;
            }
        }
    });
    if (!pay.value.money) {
        return uni.showToast({
            title: t("请输入充值金额"),
            icon: "none"
        });
    }
    if (Number(pay.value.money) <= 0) {
        return uni.showToast({
            title: t("请输入大于0的充值金额"),
            icon: "none"
        });
    }
    if (flag) {
        __updateRechargeOrder(pay.value);
    } else {
        uni.showToast({
            title: t("请选择充值金额"),
            icon: "none"
        });
    }
};

const __updateRechargeOrder = async (data: object) => {
    try {
        const result = await updateRechargeOrder(data);
        uni.navigateTo({
            url: `/pages/order/cashier?orderId=${result.orderId}&type=recharge`
        });
    } catch (error: any) {
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 1000
        });
    }
};

const emit = defineEmits(["backDetail"]);
const backDetail = () => {
    emit("backDetail");
};
</script>
<style lang="scss" scoped>
.recharge-box {
    margin: 0 10px;
    margin-top: 20px;
    border-radius: 10rpx;
    background: #fff;
    .recharge-wrap {
        padding: 20rpx;
        display: flex;
        flex-wrap: wrap;
        align-content: flex-start;
        .recharge-item {
            width: 31.3%;
            border: 2rpx solid #eee;
            padding: 30rpx 0;
            text-align: center;
            font-size: 28rpx;
            border-radius: 10rpx;
            color: var(--general);
            margin: 1%;
            &.selected {
                border: 2rpx solid var(--general);
            }
            &:last-child {
                background: #fff;
            }
            &:last-child .recharge-amount {
                margin: 0 20rpx 4rpx 20rpx;
            }
            .recharge-amount {
                font-weight: 500;
                font-size: 32rpx;
            }
            .recharge-give {
                font-size: 22rpx;
                font-weight: 300;
            }
        }
    }
}

:deep(.uni-easyinput__content-input) {
    height: 40rpx;
}
:deep(.uniui-clear) {
    display: none;
}
:deep(.is-input-border) {
    color: var(--general) !important;
}
:deep(.is-input-border.is-focused) {
    border-color: var(--general) !important;
}

.btn-box {
    padding: 25rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    gap: 20rpx;

    .btn {
        width: 48%;
        font-size: 28rpx;
    }
}
</style>
