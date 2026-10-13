<template>
    <view class="date-stock-box">
        <scroll-view scroll-x class="date-stock-scroll">
            <view class="date-list">
                <template v-for="(item, index) in dateStockList" :key="index">
                    <view class="date-item" :class="{ active: reservationDate === item.stockDate }" @click="handleChange(index)">
                        <view class="day">{{ item.day }}</view>
                        <view class="weekday">{{ item.weekDay }}</view>
                    </view>
                </template>
            </view>
        </scroll-view>
        <view class="time-quantum-box">
            <template v-for="item in timeQuantumList" :key="item.productReservationTimeId">
                <view
                    class="quantum-item"
                    :class="{ active: productReservationTimeId === item.productReservationTimeId, disabled: reservationDate && item.availableStock === 0 }"
                    @click="handleSelect(item)"
                >
                    <view class="item-time"> {{ item.startTime }}-{{ item.endTime }}</view>
                    <template v-if="reservationDate">
                        <view class="item-desc">{{ $t("【可约") }}{{ item.availableStock }}】</view>
                    </template>
                </view>
            </template>
        </view>
    </view>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
/**
 * 预约日期库存选择卡片组件
 * - 横向滚动选择预约日期
 * - 根据日期显示可用时间段及库存
 * - 用于预约服务时间选择
 */
import { computed } from "vue";
import type { DateStockList, TimeSlotStockList } from "@/types/order/check";

const { t } = useI18n();
const props = defineProps<{
    dateStockList: DateStockList[];
}>();

const reservationDate = defineModel({
    type: String,
    default: ""
});

const productReservationTimeId = defineModel("productReservationTimeId", {
    type: Number,
    default: 0
});

/**
 * 当前月份切换后，父组件会清空 reservationDate，但子组件不能继续沿用
 * 上一个月份的下标（例如上月选中第 2 天，而本月只有第 1 天）。
 * 始终按当前月份的数据重新解析选中日期，找不到时回退到首个日期。
 */
const currentIndex = computed(() => {
    if (props.dateStockList.length === 0) return null;

    const selectedIndex = props.dateStockList.findIndex((item) => item.stockDate === reservationDate.value);
    return selectedIndex >= 0 ? selectedIndex : 0;
});

const timeQuantumList = computed(() => {
    let arr: TimeSlotStockList[] = [];

    if (currentIndex.value !== null) {
        arr = props.dateStockList[currentIndex.value]?.timeSlotStockList || [];
    }

    // console.log("timeQuantumList", arr);

    return arr;
});

const handleChange = (index: number) => {
    reservationDate.value = props.dateStockList[index].stockDate;
    productReservationTimeId.value = 0;
};

const handleSelect = (data: TimeSlotStockList) => {
    if (data.availableStock === 0) return;

    if (productReservationTimeId.value === data.productReservationTimeId) return;

    if (!reservationDate.value) {
        return uni.showToast({
            title: t("请选择日期"),
            icon: "none"
        });
    }

    productReservationTimeId.value = data.productReservationTimeId;
};
</script>

<style lang="scss" scoped>
.date-stock-box {
    padding-top: 20rpx;
    .date-stock-scroll {
        width: 100%;

        .date-list {
            display: flex;

            .date-item {
                min-width: 125rpx;
                height: 110rpx;
                display: flex;
                align-items: center;
                justify-content: center;
                flex-direction: column;
                font-size: 30rpx;
                row-gap: 5rpx;
                font-weight: 600;

                &.active {
                    color: var(--general);

                    .weekday {
                        color: var(--general);
                    }
                }

                .weekday {
                    font-size: 20rpx;
                    color: #9e9e9e;
                    font-weight: 400;
                }
            }
        }
    }

    .time-quantum-box {
        padding: 30rpx 30rpx;
        background-image: linear-gradient(180deg, hsla(0, 0%, 96%, 0.6), hsla(0, 0%, 100%, 0) 4.93333vw, #fff);
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 20rpx;

        .quantum-item {
            border: 1px solid transparent;
            background: #fafafa;
            width: 100%;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 20rpx;
            row-gap: 8rpx;
            border-radius: 10rpx;
            min-height: 109rpx;

            &.active {
                border-color: var(--primary-light-7);
                background: var(--primary-light-9);
                color: var(--general);

                .item-desc {
                    color: var(--general);
                }
            }

            &.disabled {
                color: #bfbfbf;
                background-color: #f2f2f2;

                .item-desc {
                    color: #bfbfbf;
                }
            }

            .item-time {
                font-size: 25rpx;
            }

            .item-desc {
                font-size: 22rpx;
                color: #757575;
            }
        }
    }
}
</style>
