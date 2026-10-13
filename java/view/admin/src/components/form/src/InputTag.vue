/**
 * @file 标签输入组件
 * @description 支持多标签输入的表单组件，可通过输入或选择方式添加多个标签
 * @features
 * - 支持字符串（逗号分隔）或数组两种数据格式
 * - 支持手动输入创建新标签
 * - 支持回车快速添加标签
 * - 支持过滤搜索
 * - 隐藏下拉选项，专注于标签输入场景
 */
<template>
    <el-select class="input-tag-warp" style="width:100%" :teleported="false" v-model="tagValue" multiple filterable allow-create default-first-option :reserve-keyword="false"
        placeholder="" popper-class="input-tag-popper-warp" @change="onChange" :filter-method='onfilter' @keyup.enter="onEnter">
    </el-select>
</template>
<script setup lang="ts">
import { ref, toRefs, onUnmounted, computed } from "vue"

/**
 * 组件属性定义
 * @property {string|Array} modelValue - 标签值，支持逗号分隔字符串或数组
 * @property {string} splitString - 字符串模式下的分隔符，默认逗号
 */
const props = defineProps({
    modelValue: { type: [String, Array], required: true, default: '' || 0 },
    splitString: {
        type: String,
        value: ','
    }
})
const emit = defineEmits(['update:modelValue']);

/** 内部标签数组 */
const tagValue = ref();
/** 原始输入值 */
const modelValue = ref();
/** 分隔符 */
const splitString = ref();

// 初始化：根据输入类型转换为数组
modelValue.value = props.modelValue
splitString.value = props.splitString || ','
if (typeof modelValue.value === 'string') {
    tagValue.value = modelValue.value ? modelValue.value.split(splitString.value) : [];
    console.log(modelValue.value, tagValue.value)
} else {
    tagValue.value = modelValue.value
}

/**
 * 标签变更处理
 * @description 根据原始数据类型，输出对应格式的值
 * @param {any} value - 变更后的值
 */
const onChange = (value:any) => {
    if (typeof modelValue.value === 'string') {
        emit('update:modelValue', tagValue.value.join(splitString.value))
    } else {
        emit('update:modelValue', tagValue.value)
    }
}

/** 过滤输入缓存 */
const filterInput = ref();

/**
 * 过滤方法
 * @description 缓存用户输入，用于回车添加新标签
 * @param {any} e - 输入值
 */
const onfilter = (e:any) => {
    if (e) filterInput.value = e
}

/**
 * 回车事件处理
 * @description 按回车时将输入内容添加为新标签（不重复）
 */
const onEnter = () => {
    if (tagValue.value.indexOf(filterInput.value) === -1) {
        if (filterInput.value) {
            tagValue.value.push(filterInput.value)
            filterInput.value = '';
        }
    }

}
</script>
<style lang="less" scoped>
.input-tag-warp :deep(.el-tag) {
    border: none !important;
}


.input-tag-warp :deep(.input-tag-popper-warp) {
    display: none;
}

.input-tag-warp :deep(.el-input__suffix) {
    display: none;
}
</style>