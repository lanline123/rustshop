<template>
    <tig-popup v-model:show="show">
        <view class="pickuptime-popup">
            <view class="popup-title">{{ $t("请选择自提时间") }}</view>
            <view class="pickuptime-content">
                <view class="pickuptime-menu">
                    <template v-if="list[0] && list[0].length > 0">
                        <view class="menu-item" :class="{ active: pickupDateType === 0 }" @click="changePickupDateType(0)">{{ todayLabel }}</view>
                    </template>

                    <template v-if="list[1] && list[1].length > 0">
                        <view class="menu-item" :class="{ active: pickupDateType === 1 }" @click="changePickupDateType(1)">{{ tomorrowLabel }}</view>
                    </template>

                    <view class="menu-item" :class="{ active: pickupDateType === 2 }" @click="changePickupDateType(2)">{{ dayAfterTomorrowLabel }}</view>
                </view>
                <view class="pickuptime">
                    <scroll-view class="pickuptime-scroll" scroll-y :scroll-with-animation="true" :scroll-into-view="scrollIntoView">
                        <template v-for="(item, index) in list" :key="index">
                            <template v-if="item.length > 0">
                                <view :id="`list${index}`" class="pickuptime-list">
                                    <template v-for="(time, timeIndex) in item" :key="timeIndex">
                                        <view
                                            class="time-item"
                                            :class="{ active: getTimeItemChecked(index, time) }"
                                            @click="handleChangePickupTime(index, time)"
                                        >
                                            <view class="item-value">{{ index ? getDateLabel(index) : "" }} {{ time.startTime }} - {{ time.endTime }}</view>
                                            <template v-if="getTimeItemChecked(index, time)">
                                                <view class="item-checkbox"> <tig-checkbox :checked="true" size="16" /></view>
                                            </template>
                                        </view>
                                    </template>
                                </view>
                            </template>
                        </template>
                    </scroll-view>
                </view>
            </view>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
/**
 * 自提时间选择组件
 * - 选择自提日期（今天/明天/后天）
 * - 选择具体自提时间段
 * - 根据门店配置显示可选时间
 */
import { computed, ref, watchEffect } from "vue";
import { filterDayList } from "@/utils/store";

const { t } = useI18n();
const props = defineProps<{
    timeList: any[];
    currentShippingData: any;
    appointmentTime: any;
}>();

const emit = defineEmits(["change"]);

const show = defineModel({
    type: Boolean,
    default: false
});

const pickupDateType = ref(props.currentShippingData?.pickupDateType);

watchEffect(() => {
    if (!show.value) return;

    pickupDateType.value = props.currentShippingData?.pickupDateType || 0;
});

const list = computed(() => {
    const result: any[] = [];
    const maxDays = 3;

    const disabledDays = props.appointmentTime?.timeUnit === "d" ? props.appointmentTime.timeNum : 0;

    for (let i = 0; i < maxDays; i++) {
        if (i < disabledDays) {
            result.push([]);
            continue;
        }

        const dayList = i === 0 ? filterDayList(props.timeList || [], props.appointmentTime) : props.timeList || [];
        result.push(dayList);
    }

    return result;
});

const handleChangePickupTime = (index: number, time: any) => {
    emit("change", {
        pickupDateType: index,
        pickupTime: `${time.startTime}-${time.endTime}`
    });
    pickupDateType.value = index;
    show.value = false;
};

const scrollIntoView = ref("");

const changePickupDateType = (type: number) => {
    pickupDateType.value = type;
    scrollIntoView.value = `list${type}`;
};

const getTimeItemChecked = (index: number, time: any) => {
    if (index !== props.currentShippingData.pickupDateType) return false;

    return props.currentShippingData?.pickupTime === `${time.startTime} - ${time.endTime}`;
};

const weekDays = [t("周日"), t("周一"), t("周二"), t("周三"), t("周四"), t("周五"), t("周六")];

// 计算日期标签的函数
const getDateLabel = (daysOffset: number) => {
    const date = new Date();
    date.setDate(date.getDate() + daysOffset);

    const weekDay = weekDays[date.getDay()];

    switch (daysOffset) {
        case 0:
            return t("今天({0})", [weekDay]);
        case 1:
            return t("明天({0})", [weekDay]);
        case 2:
            return t("后天({0})", [weekDay]);
        default:
            return `${weekDay}`;
    }
};

const todayLabel = computed(() => getDateLabel(0));
const tomorrowLabel = computed(() => getDateLabel(1));
const dayAfterTomorrowLabel = computed(() => getDateLabel(2));
</script>

<style lang="scss" scoped>
.pickuptime-popup {
    height: 60vh;
    display: flex;
    flex-direction: column;
    background-color: #f5f5f5;
    width: 100%;
    border-radius: 32rpx 32rpx 0rpx 0rpx;
    overflow: hidden;

    .title {
        padding-left: 0;
        height: 90rpx;
        line-height: 50rpx;
        padding-bottom: 40rpx;
        background-color: #fff;
        padding: 40rpx 40rpx 0;
    }

    .pickuptime-content {
        flex: 1;
        display: flex;
        overflow: hidden;

        .pickuptime-menu {
            width: 208rpx;

            .menu-item {
                padding: 37rpx 0;
                text-align: center;
                font-size: 28rpx;
                color: #262626;

                &.active {
                    color: var(--general);
                    background-color: #fff;
                }
            }
        }

        .pickuptime {
            flex: 1;
            background-color: #f5f5f5;
        }

        .pickuptime-scroll {
            height: 100%;

            .pickuptime-list {
                width: 100%;
                background-color: #fff;
                padding: 0 20rpx 0 50rpx;
                margin-bottom: 10rpx;

                .time-item {
                    font-size: 28rpx;
                    color: #2a2a2a;
                    border-bottom: 1px solid #f5f5f5;
                    padding: 37rpx 0 47rpx 0;
                    display: flex;
                    align-items: center;

                    .item-checkbox {
                        margin-left: auto;
                    }

                    &.active {
                        color: var(--general);
                    }

                    &:last-child {
                        border-bottom: none;
                    }
                }
            }
        }
    }
}
</style>
