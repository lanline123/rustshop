<template>
    <rich-text :nodes="html" />
</template>

<script lang="ts" setup>
/**
 * 表情HTML渲染组件
 * - 将表情代码 [xxx] 转换为表情图片，普通文本做 HTML 转义并保留换行
 */
import { computed } from "vue";
import { renderTextWithEmojiHtml } from "./renderTextWithEmoji";

const props = defineProps({
    content: {
        type: String,
        default: ""
    }
});

const html = computed(() => renderTextWithEmojiHtml(props.content));
</script>

<style lang="scss" scoped>
:deep(.emoji) {
    width: 40rpx;
    height: 40rpx;
}
</style>
