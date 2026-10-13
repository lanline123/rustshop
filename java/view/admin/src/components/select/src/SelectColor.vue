/**
 * @file 颜色选择器组件
 * @description 基于 Element Plus 的颜色选择器，预置常用颜色
 * @features
 * - 支持透明度选择
 * - 预置 14 种常用颜色
 * - 支持多种颜色格式（HEX/RGB/HSL等）
 */
<template>
    <el-color-picker v-model="color" show-alpha :predefine="predefineColors"/>
</template>
<script setup lang="ts">
import { ref, onMounted } from "vue"
const color = defineModel<string>("color", { default:''});
const predefineColors = ref([
  '#ff4500',
  '#ff8c00',
  '#ffd700',
  '#90ee90',
  '#00ced1',
  '#1e90ff',
  '#c71585',
  'rgba(255, 69, 0, 0.68)',
  'rgb(255, 120, 0)',
  'hsv(51, 100, 98)',
  'hsva(120, 40, 94, 0.5)',
  'hsl(181, 100%, 37%)',
  'hsla(209, 100%, 56%, 0.73)',
  '#c7158577',
])
</script>

<style lang="less" scoped>
</style>
