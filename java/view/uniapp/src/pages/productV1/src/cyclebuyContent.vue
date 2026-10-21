<template>
    <view v-if="hasCycleInfo" class="cycleBuyContent">
        <view class="topBg" />
        <view class="content">
            <view class="header" @click="openPopup">
                <view class="title">{{ $t("周期购") }}</view>
                <view class="tip">{{ $t("一次下单 多次发货") }}</view>
                <view class="iconfont-h5 icon-a-gengduo20 right" />
            </view>
            <view class="form">
                <view class="formItem">
                    <view class="label">{{ $t("单期次数") }}</view>
                    <view class="value">{{ cycleInfo.timesPerPeriod || 1 }}{{ $t("次") }}</view>
                </view>
                <view class="formItem">
                    <view class="label">{{ $t("配送方式") }}</view>
                    <view class="value">{{ cycleInfo.cycleTypeName || "-" }}</view>
                </view>
                <view class="formItem">
                    <view class="label">{{ $t("配送期数") }}</view>
                    <view class="value">{{ minPeriodText }}</view>
                </view>
                <view class="formItem">
                    <view class="label">{{ $t("最早配送") }}</view>
                    <view class="value">{{ earliestDateText }}</view>
                </view>
            </view>
        </view>
    </view>

    <tig-popup v-model:show="showPopup" :z-index="999" :show-close="true" @close="handlePopupClose">
        <scroll-view scroll-y="true" class="popupContent">
            <view class="popupTitle">{{ $t("周期配送") }}</view>
            <view class="form">
                <view class="formItem">
                    <view class="label">{{ $t("配送期数") }}</view>
                    <view class="value">{{ periodNumsText }}</view>
                </view>
                <view class="formItem">
                    <view class="label">{{ $t("单期次数") }}</view>
                    <view class="value">{{ cycleInfo.timesPerPeriod || 1 }}{{ $t("次") }}</view>
                </view>
                <view class="formItem">
                    <view class="label">{{ $t("配送方式") }}</view>
                    <view class="value">{{ cycleInfo.cycleTypeName || "-" }}</view>
                </view>
                <view v-if="deliveryDaysText" class="formItem">
                    <view class="label">{{ $t("配送日期") }}</view>
                    <view class="value">{{ deliveryDaysText }}</view>
                </view>
                <view class="formItem">
                    <view class="label">{{ $t("最早配送") }}</view>
                    <view class="value">{{ earliestDateText }}</view>
                </view>
                <view v-if="deliveryTimeSlotsText" class="formItem">
                    <view class="label">{{ $t("配送时段") }}</view>
                    <view class="value">{{ deliveryTimeSlotsText }}</view>
                </view>
            </view>
        </scroll-view>
    </tig-popup>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
/**
 * 周期购内容组件
 * - 周期配置选择（周期类型、配送频次）
 * - 计算并展示周期购总价
 * - 预览配送计划
 */
import type { CycleConfig } from "@/types/product/product";
import { ref, computed } from "vue";

const { t } = useI18n();
const props = defineProps<{
    cycleInfo?: CycleConfig;
}>();

const showPopup = ref(false);

// 是否有周期购信息
const hasCycleInfo = computed(() => {
    return props.cycleInfo && Object.keys(props.cycleInfo).length > 0;
});

// 内部使用的 cycleInfo
const cycleInfo = computed(() => props.cycleInfo || {});

// 最小起订期数文本
const minPeriodText = computed(() => {
    const specs = cycleInfo.value.specs || [];
    if (specs.length === 0) return "-";
    const minPeriod = Math.min(...specs.map((s) => s.periodNum));
    return t("{0}期起订", [minPeriod]);
});

// 所有期数文本
const periodNumsText = computed(() => {
    const specs = cycleInfo.value.specs || [];
    if (specs.length === 0) return "-";
    return specs.map((s) => `${s.periodNum}${t("期")}`).join("、");
});

// 最早配送日期文本
const earliestDateText = computed(() => {
    const date = cycleInfo.value.earliestDeliveryDate;
    if (!date) return "-";
    // 格式化日期，如 "2024-12-10" -> "12月10日"
    const parts = date.split("-");
    if (parts.length >= 3) {
        const month = parseInt(parts[1], 10);
        const day = parseInt(parts[2], 10);
        return `${month}${t("月")}${day}${t("日")}`;
    }
    return date;
});

// 配送日期文本（星期或日期）
const deliveryDaysText = computed(() => {
    // 优先显示星期
    if (cycleInfo.value.deliveryWeekdayNames && cycleInfo.value.deliveryWeekdayNames.length > 0) {
        return cycleInfo.value.deliveryWeekdayNames.join("、");
    }
    // 其次显示日期
    if (cycleInfo.value.deliveryDays && cycleInfo.value.deliveryDays.length > 0) {
        return cycleInfo.value.deliveryDays.map((d) => `${d}${t("日")}`).join("、");
    }
    return "";
});

// 配送时段文本
const deliveryTimeSlotsText = computed(() => {
    const reservationTimes = cycleInfo.value.reservationTimes || [];
    if (reservationTimes.length > 0) {
        return reservationTimes.map((t) => `${t.startTime}-${t.endTime}`).join("、");
    }
    const slots = cycleInfo.value.deliveryTimeSlots || [];
    if (slots.length === 0) return "";
    return slots.join("、");
});

const handlePopupClose = () => {
    showPopup.value = false;
};

const openPopup = () => {
    showPopup.value = true;
};
</script>

<style scoped lang="scss">
.cycleBuyContent {
    width: 100%;
    height: 227rpx;
    border-radius: 8px;
    position: relative;
    background: #fff;
    overflow: hidden;
    margin-top: 20rpx;

    .topBg {
        position: absolute;
        width: 100%;
        height: 50rpx;
        background: linear-gradient(to bottom, rgba(251, 66, 78, 0.2) 0%, #fff 100%);
    }

    .content {
        padding: 20rpx;
        box-sizing: border-box;
        position: absolute;
        width: 100%;
        height: 100%;
        z-index: 9;

        .header {
            display: flex;
            align-items: center;
            line-height: 40rpx;

            .title {
                font-weight: bold;
                font-size: 28rpx;
                color: #2a2a2a;
                margin-right: 20rpx;
            }

            .tip {
                font-weight: 500;
                font-size: 24rpx;
                color: #9a9a9a;
            }

            .right {
                margin-left: auto;
                font-size: 20rpx;
                color: #9a9a9a;
            }
        }

        .form {
            width: calc(100% + 80rpx);
            transform: translateX(-45rpx);
            display: flex;
            align-items: center;
            margin-top: 40rpx;

            .formItem {
                width: 25%;
                display: flex;
                gap: 20rpx;
                flex-direction: column;
                align-items: center;

                &:not(:last-child) {
                    position: relative;

                    &::after {
                        content: "";
                        position: absolute;
                        right: 0;
                        width: 1rpx;
                        height: 100%;
                        background: #f5f5f5;
                    }
                }

                .label {
                    font-weight: 500;
                    font-size: 24rpx;
                    color: #9a9a9a;
                }

                .value {
                    font-weight: 500;
                    font-size: 24rpx;
                    color: #2a2a2a;

                    &:first-child {
                        align-self: flex-start;
                    }
                }
            }
        }
    }
}

.popupContent {
    height: 900rpx;
    padding: 30rpx;
    box-sizing: border-box;

    .popupTitle {
        text-align: center;
        color: #000;
        font-weight: 600;
        font-size: 30rpx;
    }

    .form {
        display: flex;
        flex-direction: column;
        margin-top: 70rpx;
        gap: 60rpx;

        .formItem {
            display: flex;
            gap: 30rpx;

            .label {
                font-weight: 500;
                font-size: 25rpx;
                color: #9a9a9a;
                flex-shrink: 0;
            }

            .value {
                font-weight: 500;
                font-size: 25rpx;
                color: #2a2a2a;
            }
        }
    }
}
</style>
