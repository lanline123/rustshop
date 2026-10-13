<template>
    <el-checkbox v-model="temp">
        <slot></slot>
    </el-checkbox>
</template>
<script lang="ts" setup>
import { computed } from 'vue'

const props = defineProps({
    modelValue: {
        type: Number,
        default: 0
    },
});
const emit = defineEmits(['update:modelValue']);
const temp = computed({
    get: () => props.modelValue > 0,
    set: (newValue) => emit('update:modelValue', newValue ? 1 : 0)
});
</script>
