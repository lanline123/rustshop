<template>
    <pickerDatePop ref="uPickerRef" v-model="show" :default-index="currentIndexs" :columns="columns" @confirm="confirm" @change="changeHandler">
        <template #default>
            <view class="picker-box-header">
                <view class="header-item" :class="{ active: currentTimeIndex === 0 }" @click="handleTime(0)">
                    <view class="header-item-text">{{ $t("开始时间") }}</view>
                    <view class="header-item-value">{{ times[0] ?? $t("请选择") }}</view>
                </view>
                <view class="header-item" :class="{ active: currentTimeIndex === 1 }" @click="handleTime(1)">
                    <view class="header-item-text">{{ $t("结束时间") }}</view>
                    <view class="header-item-value">{{ times[1] ?? $t("请选择") }}</view>
                </view>
            </view>
        </template>
    </pickerDatePop>
</template>

<script setup lang="ts">
import { computed, onBeforeMount, ref } from "vue";
import { formatDate } from "@/utils/format";
import { useI18n } from "vue-i18n";
import pickerDatePop from "./pickerDatePop.vue";

const { t } = useI18n();

const props = defineProps({
    modelValue: {
        type: Boolean,
        default: false
    },
    minYears: {
        type: [Number, String],
        default: 1970
    }
});
const show = computed({
    get() {
        return props.modelValue;
    },
    set(val) {
        emit("update:modelValue", val);
    }
});
const emit = defineEmits(["change", "update:modelValue", "confirm"]);
const uPickerRef = ref();

const years = computed(() => {
    let years = [];
    for (let i = Number(props.minYears); i <= new Date().getFullYear(); i++) {
        years.push(i);
    }
    return years;
});
const months = computed(() => {
    let months = [];
    for (let i = 1; i <= 12; i++) {
        months.push(i);
    }
    return months;
});
const days = computed(() => {
    let days = [];
    let dayNum = getDayNum(years.value[currentIndexs.value[0]], months.value[currentIndexs.value[1]]);
    for (let i = 1; i <= dayNum; i++) {
        days.push(i);
    }
    return days;
});
const columns = computed(() => {
    return [years.value, months.value, days.value];
});
const changeHandler = (e: any) => {
    const { values, indexs } = e;
    currentIndexs.value = indexs;
    times.value[currentTimeIndex.value] = values.join(".");
    sendvalue.value[currentTimeIndex.value] = values.join("-");
};
const confirm = (e: any) => {
    if (!times.value[0] || !times.value[1]) {
        return uni.showToast({
            title: t("请选择开始时间和结束时间"),
            icon: "none"
        });
    }
    const val = compareDates(times.value[1], times.value[0]);
    if (val === -1) {
        return uni.showToast({
            title: t("结束时间必须大于等于开始时间"),
            icon: "none"
        });
    }
    emit("confirm", sendvalue.value);
    emit("update:modelValue", false);
};
const getDayNum = (year: number, month: number): number => {
    if (month === 2) {
        return isLeapYear(year) ? 29 : 28;
    } else if ([4, 6, 9, 11].includes(month)) {
        return 30;
    }
    return 31;
};
const isLeapYear = (year: number): boolean => {
    return (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0;
};
const compareDates = (dateStr1: string, dateStr2: string): number => {
    const date1 = new Date(dateStr1.split("-").join("/"));
    const date2 = new Date(dateStr2.split("-").join("/"));

    if (date1.getTime() > date2.getTime()) {
        return 1;
    } else if (date1.getTime() < date2.getTime()) {
        return -1;
    } else {
        return 0;
    }
};

const currentIndexs = ref([0, 0, 0]);
const setDefaultIndex = (date: Date) => {
    const dateText = formatDate(date);
    const [year, month, day] = dateText.split("-");
    currentIndexs.value = [years.value.indexOf(Number(year)), months.value.indexOf(Number(month)), days.value.indexOf(Number(day))];
};

const times = ref<any>([]);
const sendvalue = ref<any>([]);
const currentTimeIndex = ref(0);
const handleTime = (index: number) => {
    currentTimeIndex.value = index;
    if (times.value[index]) {
        setDefaultIndex(new Date(times.value[index].split(".").join("-")));
    }
};

onBeforeMount(() => {
    setDefaultIndex(new Date());
});
</script>

<style lang="scss" scoped>
.picker-box-header {
    display: flex;
    margin-top: 30rpx;
    .header-item {
        flex: 1;
        padding: 15px;
        position: relative;
        &.active::after {
            content: "";
            position: absolute;
            height: 2px;
            width: 90%;
            background-color: var(--general);
            bottom: 0;
            right: 0;
        }
        .header-item-text {
            color: #999;
            line-height: 18px;
        }

        .header-item-value {
            font-size: 20px;
            margin-top: 6px;
            line-height: 28px;
            color: #333;
        }
    }
}
</style>
