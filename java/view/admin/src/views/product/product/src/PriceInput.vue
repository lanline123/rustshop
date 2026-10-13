/**
 * @file 价格输入组件
 * @description 商品价格输入框，自动添加货币符号
 */
<template>
    <TigInput classType="tig-form-input" type="decimal" v-model="modelValue" :disabled="disabled" :placeholder="placeholder">
        <template v-if="slot" #append> {{ config.dollarSign }} </template>
    </TigInput>
</template>
<script lang="ts" setup>
import { useConfigStore } from "@/store/config";
const config = useConfigStore().config;
const props = defineProps({
    disabled: {
        type: Boolean,
        default: false
    },
    placeholder: {
        type: String,
        default: ""
    },
    dataType: {
        type: Number,
        default: 2
    },
    dataId: {
        type: Number,
        default: 0
    },
    slot: {
        type: Boolean,
        default: true
    }
});
const emit = defineEmits(["update:modelValue"]);
const modelValue = defineModel<any>("modelValue", { default: "" });
</script>
<style scoped lang="less"></style>
