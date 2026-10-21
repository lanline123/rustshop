/**
 * @file 列表开关组件
 * @description 轻量级开关，用于列表中快速切换状态并自动调用API
 * @features
 * - 点击自动调用API更新状态
 * - 支持 beforeChange 拦截
 * - 自定义开关值映射
 */
<template>
    <div @click="switchChange" :class="'table-switch table-switch-' + (selectChecked ? 'on' : 'off')">
        <i></i>
    </div>
</template>
<script setup lang="ts">
import { computed, toRefs } from "vue";
import request from "@/utils/request";
import { message } from "ant-design-vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    /** 状态切换API函数 */
    requestApi: { type: Function, default: request },
    /** API请求参数 */
    params: { type: Object, default: {} as any },
    /** 当前选中状态 */
    checked: { type: [Boolean, Number], default: Number },
    /** 开关值映射：[关闭值, 开启值] */
    switchingCode: { type: Array, default: [0, 1] },
    /** 开启状态对应的索引 */
    switchingIndex: { type: Number, default: 1 },
    /** 切换前的钩子函数，返回false阻止切换 */
    beforeChange: { type: Function, default: null },
    /** 是否禁用 */
    disabled: { type: Boolean, default: false }
});

/** 计算当前是否为选中状态 */
const selectChecked = computed(() => {
    return checked.value == props.switchingCode[props.switchingIndex] ? 1 : 0;
});

const emit = defineEmits(["update:checked", "callback", "updateData"]);
const { checked } = toRefs(props);

/**
 * 切换开关状态
 * @description 执行beforeChange钩子后调用API更新状态
 */
const switchChange = async () => {
    if (props.disabled) {
        return;
    }
    // 如果提供了 beforeChange 回调，先执行它
    if (props.beforeChange) {
        const result = await props.beforeChange();
        if (result === false) {
            return;
        }
    }

    props
        .requestApi({ ...props.params, val: checked.value == props.switchingCode[1] ? props.switchingCode[0] : props.switchingCode[1] })
        .then((result: any) => {
            emit("update:checked", checked.value == props.switchingCode[1] ? props.switchingCode[0] : props.switchingCode[1]);
            emit("callback");
            emit("updateData");
            message.success(t('操作成功'));
        })
        .catch((error: any) => {
            message.error(error.message);
        });
};
</script>
