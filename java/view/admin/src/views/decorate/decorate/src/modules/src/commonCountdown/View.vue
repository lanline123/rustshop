<template>
    <div class="countdown-box">
        <el-countdown
            v-show="!$slots.default"
            ref="countdownRef"
            @change="handleChange"
            @finish="handleFinish"
            :value-style="valueStyle"
            :format="format"
            :value="currentCountdownValue"
        />

        <template v-if="$slots.default">
            <slot :days="formatTime.days" :hours="formatTime.hours" :minutes="formatTime.minutes" :seconds="formatTime.seconds" />
        </template>
    </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";

interface Props {
    countdownType: "dailyCycle" | "specifyTime";
    valueStyle?: Record<string, any>;
    dailyTime?: number[] | number;
    specifyTime?: number;
    format?: string;
}

const props = withDefaults(defineProps<Props>(), {
    countdownType: "dailyCycle",
    valueStyle: () => ({}),
    dailyTime: () => [],
    specifyTime: 0,
    format: "HH:mm:ss"
});

const emit = defineEmits<{
    change: [time: number];
    finish: [];
}>();

const countdownRef = ref<any>(null);

const currentCountdownValue = computed(() => {
    if (props.countdownType === "specifyTime") {
        return props.specifyTime;
    }

    if (props.countdownType === "dailyCycle") {
        const timeInfo = getDailyTimeInfo();
        const now = Date.now();

        if (Array.isArray(props.dailyTime) && now < timeInfo.dailyStartTime) {
            return timeInfo.dailyStartTime;
        }

        return timeInfo.dailyEndTime;
    }

    return 0;
});

const getDailyTimeInfo = () => {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);

    let dailyStartTime = 0;
    let dailyEndTime = 0;

    if (typeof props.dailyTime === "number") {
        const endHour = new Date(props.dailyTime).getHours();
        const endMinute = new Date(props.dailyTime).getMinutes();
        const endSecond = new Date(props.dailyTime).getSeconds();

        dailyEndTime = new Date(today.setHours(endHour, endMinute, endSecond, 0)).getTime();

        if (dailyEndTime <= Date.now()) {
            dailyEndTime = new Date(tomorrow.setHours(endHour, endMinute, endSecond, 0)).getTime();
        }
    } else if (Array.isArray(props.dailyTime) && props.dailyTime.length >= 2) {
        const startTime = new Date(props.dailyTime[0]);
        const endTime = new Date(props.dailyTime[1]);

        const startHour = startTime.getHours();
        const startMinute = startTime.getMinutes();
        const startSecond = startTime.getSeconds();

        const endHour = endTime.getHours();
        const endMinute = endTime.getMinutes();
        const endSecond = endTime.getSeconds();

        dailyStartTime = new Date(today.setHours(startHour, startMinute, startSecond, 0)).getTime();
        dailyEndTime = new Date(today.setHours(endHour, endMinute, endSecond, 0)).getTime();

        if (dailyStartTime <= Date.now()) {
            dailyStartTime = new Date(tomorrow.setHours(startHour, startMinute, startSecond, 0)).getTime();
        }

        if (dailyEndTime <= Date.now()) {
            dailyEndTime = new Date(tomorrow.setHours(endHour, endMinute, endSecond, 0)).getTime();
        }
    }

    return {
        dailyStartTime,
        dailyEndTime
    };
};

const countdownTime = ref<number>(0);

const formatTime = computed(() => {
    const totalSeconds = Math.floor(countdownTime.value / 1000);
    const days = Math.floor(totalSeconds / (24 * 60 * 60));
    const hours = Math.floor((totalSeconds % (24 * 60 * 60)) / (60 * 60));
    const minutes = Math.floor((totalSeconds % (60 * 60)) / 60);
    const seconds = totalSeconds % 60;

    return {
        days: days.toString().padStart(2, "0"),
        hours: hours.toString().padStart(2, "0"),
        minutes: minutes.toString().padStart(2, "0"),
        seconds: seconds.toString().padStart(2, "0")
    };
});

const handleChange = (time: number) => {
    countdownTime.value = time; // 更新倒计时值
    emit("change", time);
};

const handleFinish = () => {
    emit("finish");
};

watch(
    () => [props.countdownType, props.dailyTime, props.specifyTime],
    () => {},
    { deep: true }
);

// 暴露组件方法
defineExpose({
    countdownRef
});
</script>

<style lang="less" scoped>
.countdown-box {
    display: inline-block;
}
</style>
