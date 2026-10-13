<template>
    <div class="countdown-box">
        <up-count-down v-show="!$slots.default" :format="format" :time="countdownTime" @change="onChange">
            <template v-for="(item, index) in formatArray" :key="index">
                <text class="time-item" :style="timeStyle">
                    {{ formatTime(timeData[TIME_UNIT_MAP[item]]) }}
                    <template v-if="!zh">
                        <text class="time-item-unit" :style="unitStyle">
                            {{ index < formatArray.length - 1 ? ":" : "" }}
                        </text>
                    </template>
                    <template v-else>
                        <text class="time-item-unit" :style="unitStyle">
                            {{ TEXT_MAP[item] }}
                        </text>
                    </template>
                </text>
            </template>
        </up-count-down>

        <template v-if="$slots.default">
            <slot :days="timeData.days" :hours="timeData.hours" :minutes="timeData.minutes" :seconds="timeData.seconds" />
        </template>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

// 常量定义
const TEXT_MAP: Record<string, string> = {
    DD: t("天"),
    HH: t("时"),
    mm: t("分"),
    ss: t("秒")
} as const;

const TIME_UNIT_MAP: Record<string, string> = {
    DD: "days",
    HH: "hours",
    mm: "minutes",
    ss: "seconds"
} as const;

// Props 接口定义
interface Props {
    countdownType?: "dailyCycle" | "specifyTime";
    valueStyle?: Record<string, any>;
    dailyTime?: number[] | number | string;
    specifyTime?: number | string;
    format?: string;
    color?: string;
    zh?: boolean;
    timeStyle?: Record<string, any>;
    unitStyle?: Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
    countdownType: "dailyCycle",
    valueStyle: () => ({}),
    dailyTime: () => [],
    specifyTime: "",
    format: "HH:mm:ss",
    color: "#000",
    zh: false,
    timeStyle: () => ({}),
    unitStyle: () => ({})
});

// 响应式数据
const timeData = ref<Record<string, string>>({});

// 计算属性
const formatArray = computed(() => props.format.split(":"));

const getCurrentTimestamp = (): number => Date.now();

const formatTime = (time: string): string => {
    return time?.toString().padStart(2, "0") || "00";
};

// 优化后的时间信息计算
const timeInfo = computed(() => {
    const now = new Date();
    const result = {
        dailyStartTime: 0,
        dailyEndTime: 0
    };

    if (!props.dailyTime) return result;

    if (Array.isArray(props.dailyTime)) {
        if (props.dailyTime.length >= 2) {
            const startHour = new Date(Number(props.dailyTime[0])).getHours();
            const endHour = new Date(Number(props.dailyTime[1])).getHours();

            result.dailyStartTime = new Date(now.setHours(startHour, 0, 0, 0)).getTime();
            result.dailyEndTime = new Date(now.setHours(endHour, 0, 0, 0)).getTime();
        }
    } else {
        const hour = new Date(Number(props.dailyTime)).getHours();
        result.dailyEndTime = new Date(now.setHours(hour, 0, 0, 0)).getTime();
    }

    return result;
});

// 计算倒计时时间
const countdownTime = computed(() => {
    const currentTime = getCurrentTimestamp();

    if (props.countdownType === "dailyCycle") {
        // 检查是否在开始时间之前
        if (Array.isArray(props.dailyTime) && currentTime < Number(props.dailyTime[0])) {
            const remainingTime = timeInfo.value.dailyStartTime - currentTime;
            return Math.max(remainingTime, 0);
        } else {
            // 计算到结束时间的剩余时间
            const remainingTime = timeInfo.value.dailyEndTime - currentTime;
            return Math.max(remainingTime, 0);
        }
    } else {
        // 指定时间倒计时
        const remainingTime = Number(props.specifyTime) - currentTime;
        return Math.max(remainingTime, 0);
    }
});

// 事件处理
const onChange = (e: Record<string, string>) => {
    timeData.value = e;
};
</script>

<style lang="scss" scoped>
.countdown-box {
    .time-item {
        color: v-bind(color) !important;
        font-size: 12px;

        &-unit {
            margin-left: 2px;
        }
    }
}
</style>
