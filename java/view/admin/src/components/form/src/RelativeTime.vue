/**
 * @file 相对时间显示组件
 * @description 将时间戳转换为相对时间显示（如"3分钟前"、"2小时前"）
 * @features
 * - 自动计算相对时间（基于 dayjs）
 * - 智能刷新间隔（近期时间刷新频率高，远期时间刷新频率低）
 * - 中文本地化显示
 * - 组件卸载时自动清理定时器
 */
<template>
    <span>{{ formattedTime }}</span>
</template>

<script lang="ts" setup>
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import "dayjs/locale/zh-cn";
import { ref, computed, watchEffect } from "vue";

// 启用 dayjs 相对时间插件和中文本地化
dayjs.extend(relativeTime);
dayjs.locale("zh-cn");

/**
 * 组件属性定义
 * @property {string} time - 需要转换的时间字符串
 */
const props = defineProps({
    time: {
        type: String,
        default: ""
    }
});

/** 当前时间的响应式引用，用于触发重新计算 */
const now = ref(dayjs());

/**
 * 计算相对时间
 * @returns {string} 相对时间文本，如"几秒前"、"3分钟前"
 */
const formattedTime = computed(() => {
    return dayjs(props.time).from(now.value);
});

/**
 * 智能刷新逻辑
 * @description 根据时间差动态调整刷新间隔，优化性能
 * - 1分钟以内：每10秒刷新
 * - 1小时以内：每分钟刷新
 * - 24小时以内：每10分钟刷新
 * - 1周以内：每小时刷新
 * - 超过1周：每天刷新
 */
watchEffect((onCleanup) => {
    const diffInSeconds = dayjs().diff(dayjs(props.time), 'second');
    let refreshInterval;

    // 设置刷新间隔时间
    if (diffInSeconds < 60) {
        refreshInterval = 10000; // 1分钟以内，每10秒刷新
    } else if (diffInSeconds < 3600) {
        refreshInterval = 60000; // 1小时以内，每分钟刷新
    } else if (diffInSeconds < 86400) {
        refreshInterval = 600000; // 1小时到24小时，每10分钟刷新
    } else if (diffInSeconds < 604800) {
        refreshInterval = 3600000; // 1天到1周，每小时刷新
    } else {
        refreshInterval = 86400000; // 超过1周，每天刷新
    }

    // 设置定时器来更新 `now`，从而触发 `formattedTime` 的重新计算
    const intervalId = setInterval(() => {
        now.value = dayjs(); // 更新当前时间
    }, refreshInterval);

    // 在组件卸载时清除定时器
    onCleanup(() => clearInterval(intervalId));
});
</script>

<style lang="less" scoped></style>
