<template>
    <tig-popup v-model:show="showPopup" :z-index="999" background-color="#fff" @close="handlePopupClose">
        <view class="datepop">
            <view class="header">
                <view class="title">{{ displayTitle }}</view>
                <view class="sub">
                    <text class="month">{{ monthLabel }}</text>
                    <text v-if="multiple" class="count">{{ $t("（已选") }}{{ selectedDates.length }}/{{ maxSelectable }}）</text>
                </view>
            </view>

            <view class="week">
                <view v-for="w in weekLabels" :key="w" class="week-item">{{ w }}</view>
            </view>

            <view class="grid">
                <view v-for="(cell, idx) in calendarCells" :key="idx" class="cell" :class="{ empty: cell.isEmpty }" @click="handleCellClick(cell)">
                    <view v-if="!cell.isEmpty" class="day" :class="{ selected: isSelected(cell.value), today: cell.isToday }">{{ cell.text }}</view>
                </view>
            </view>

            <tig-fixed-placeholder background-color="#fff">
                <view class="btns">
                    <tig-button class="btn" @click="handleConfirm">{{ $t("确认") }}</tig-button>
                </view>
            </tig-fixed-placeholder>
        </view>
    </tig-popup>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
/**
 * 日期选择弹窗组件
 * - 日历形式展示可选日期
 * - 支持设置可选日期范围（minDate/maxDate）
 * - 用于预约服务日期选择
 */
import { computed, ref, watch } from "vue";

const { t } = useI18n();
type CalendarCell = {
    isEmpty: boolean;
    text?: string;
    value?: string; // YYYY-MM-DD
    isToday?: boolean;
};

const props = defineProps({
    // 是否多选
    multiple: {
        type: Boolean,
        default: false
    },
    // 多选时最多可选几个
    maxSelect: {
        type: Number,
        default: 1
    },
    // 默认选中（单选传 string，多选传 string[]，格式 YYYY-MM-DD）
    defaultSelected: {
        type: [String, Array],
        default: () => []
    },
    title: {
        type: String,
        default: ""
    }
});

const displayTitle = computed(() => props.title || t("选择日期"));

const emit = defineEmits<{
    (e: "confirm", value: string | string[]): void;
    (e: "cancel"): void;
}>();

const showPopup = defineModel<boolean>("showPopup", { default: false });
const selectedDates = ref<string[]>([]);

const now = new Date();
const year = ref(now.getFullYear());
const month = ref(now.getMonth()); // 0-11

const weekLabels = [t("周一"), t("周二"), t("周三"), t("周四"), t("周五"), t("周六"), t("周日")];

const pad2 = (n: number) => (n < 10 ? `0${n}` : `${n}`);
const formatYmd = (d: Date) => `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`;
const todayYmd = formatYmd(new Date());

const maxSelectable = computed(() => {
    if (!props.multiple) return 1;
    const m = Number(props.maxSelect);
    return Number.isFinite(m) && m > 0 ? m : 9999;
});

const monthLabel = computed(() => `${year.value}${t("年")}${month.value + 1}${t("月")}`);

const daysInMonth = computed(() => new Date(year.value, month.value + 1, 0).getDate());
const firstWeekday = computed(() => new Date(year.value, month.value, 1).getDay()); // 0(日)-6(六)
// 以周一为第一天：把周日(0)挪到最后
const leadingEmpty = computed(() => (firstWeekday.value + 6) % 7);

const calendarCells = computed<CalendarCell[]>(() => {
    const res: CalendarCell[] = [];

    for (let i = 0; i < leadingEmpty.value; i++) {
        res.push({ isEmpty: true });
    }

    for (let d = 1; d <= daysInMonth.value; d++) {
        const date = new Date(year.value, month.value, d);
        const value = formatYmd(date);
        res.push({
            isEmpty: false,
            text: String(d),
            value,
            isToday: value === todayYmd
        });
    }

    const total = Math.ceil(res.length / 7) * 7;
    while (res.length < total) {
        res.push({ isEmpty: true });
    }

    return res;
});

const normalizeDefaultSelected = () => {
    const v: any = props.defaultSelected;
    if (Array.isArray(v)) return v.filter(Boolean);
    if (typeof v === "string" && v) return [v];
    return [];
};

watch(
    () => showPopup.value,
    (val) => {
        if (!val) return;
        const init = normalizeDefaultSelected();
        selectedDates.value = props.multiple ? init.slice(0, maxSelectable.value) : init.slice(0, 1);
    }
);

const isSelected = (v?: string) => !!v && selectedDates.value.includes(v);

const handleCellClick = (cell: CalendarCell) => {
    if (cell.isEmpty || !cell.value) return;

    const value = cell.value;

    if (!props.multiple) {
        selectedDates.value = selectedDates.value[0] === value ? [] : [value];
        return;
    }

    const idx = selectedDates.value.indexOf(value);
    if (idx >= 0) {
        selectedDates.value.splice(idx, 1);
        return;
    }

    if (selectedDates.value.length >= maxSelectable.value) {
        uni.showToast({
            title: t("最多选择{0}个日期", [maxSelectable.value]),
            icon: "none"
        });
        return;
    }

    selectedDates.value = [...selectedDates.value, value].sort();
};

const handlePopupClose = () => {
    showPopup.value = false;
};

const handleConfirm = () => {
    const result = props.multiple ? selectedDates.value.slice().sort() : selectedDates.value[0] || "";
    console.log(result);
    emit("confirm", result);
    showPopup.value = false;
};
</script>

<style scoped lang="scss">
.datepop {
    height: 60vh;
    width: 100%;
    background-color: #fff;
    border-radius: 32rpx 32rpx 0 0;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    padding: 30rpx;
    box-sizing: border-box;
}

.header {
    padding-bottom: 20rpx;
    .title {
        font-size: 32rpx;
        color: #2a2a2a;
        font-weight: 600;
    }
    .sub {
        margin-top: 10rpx;
        font-size: 26rpx;
        color: #9a9a9a;
        .count {
            margin-left: 10rpx;
        }
    }
}

.week {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 10rpx;
    padding: 10rpx 0 20rpx 0;
    .week-item {
        text-align: center;
        font-size: 24rpx;
        color: #9a9a9a;
    }
}

.grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 10rpx;
    flex: 1;
    align-content: start;
    padding-bottom: 20rpx;
}

.cell {
    height: 84rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    &.empty {
        opacity: 0;
    }
}

.day {
    width: 64rpx;
    height: 64rpx;
    border-radius: 8rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28rpx;
    color: #2a2a2a;
    background-color: transparent;
    border: 2rpx solid transparent;

    &.today {
        border-color: rgba(255, 114, 13, 0.35);
    }

    &.selected {
        background-color: var(--general);
        color: #fff;
        border-color: var(--general);
    }
}

.btns {
    padding: 25rpx;
    display: flex;
    gap: 20rpx;
    .btn {
        flex: 1;
    }
}
</style>
