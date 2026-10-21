<template></template>
<script lang="ts" setup>
import { ref } from 'vue';
const defaultFrame = {
    // 结构设置
    'backgroundColor': '',
    'innerPadding': 0,
    'boxRadius': 0,
    'boxPadding': 10,
    'boxPaddingTop': 5,
    'boxPaddingBottom': 5,
}
defineExpose({
    defaultFrame
});
</script>