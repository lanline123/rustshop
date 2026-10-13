/**
 * @file 手机号码卡片组件
 * @description 显示脱敏的手机号，支持点击眼睛图标切换显示完整号码
 * @features
 * - 手机号自动脱敏（保留前3后4）
 * - 座机号智能识别处理
 * - 演示模式禁止查看完整号码
 */
<template>
    <div class="mobile" v-if="mobileValue != '--'">
        {{ mobileValue }}
        <i v-if="mobileValue" @click.stop="toggleVisibility" class="iconfont-admin icon-cu" :class="[showMobile ? 'icon-yanjing-kai' : 'icon-yanjing-guan']"></i>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { message } from "ant-design-vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps<{ mobile: string | undefined }>();
/** 是否显示完整手机号 */
const showMobile = ref(false);
/** 是否为演示模式 */
const isDemo = ref(import.meta.env.VITE_IS_DEMO || 0);

/** 切换手机号显示/隐藏状态 */
const toggleVisibility = () => {
    if (isDemo.value == 1) {
        console.log("====", isDemo.value);
        message.error(t('您无权限查看'));
    } else {
        showMobile.value = !showMobile.value;
    }
};

/**
 * 计算显示的手机号值
 * @description 根据显示状态返回完整或脱敏后的手机号
 * @returns {string} 处理后的手机号
 */
const mobileValue = computed(() => {
    if (showMobile.value) return props.mobile;
    if (typeof props.mobile !== "string") return "--";
    
    // 去除非数字字符用于长度判断
    const numericMobile = props.mobile.replace(/\D/g, "");
    if (numericMobile.length <= 6) return props.mobile;

    // 智能判断号码类型
    const isLandline = /^0\d{2,3}-\d{7,8}$/.test(props.mobile);
    let digitCount = 0;
    let masked = "";
    let hideCount = 0;
    let hideStart = 0;

    // 设置不同的加密策略
    if (isLandline) {
        // 座机号：区号不加密，主号保留前2后2位
        const [areaCode, mainNumber] = props.mobile.split('-');
        if (mainNumber.length > 4) {
            const hideCount = mainNumber.length - 4;
            const regex = new RegExp(`(.{2})(.{${hideCount}})(.{2})`);
            masked = areaCode + '-' + mainNumber.replace(regex, '$1' + '*'.repeat(hideCount) + '$3');
        } else {
            masked = props.mobile;
        }
    } else {
        // 手机号/其他号码：保留前3后4位
        for (const char of props.mobile) {
            if (/[0-9]/.test(char)) {
                digitCount++;
                const shouldHide = digitCount > 3 && digitCount <= numericMobile.length - 4;
                masked += shouldHide ? '*' : char;
            } else {
                masked += char; // 保留非数字字符
            }
        }
    }

    return masked;
});
</script>

<style scoped lang="less">
.mobile {
    display: inline-flex;
    flex-direction: row;
    gap: 6px;
    align-items: center;

    .icon-cu {
        cursor: pointer;
    }
}
</style>
