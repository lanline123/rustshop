<template>
    <div v-html="html" />
</template>

<script lang="ts" setup>
import { computed } from "vue";
import emojiElements from "./emojiElements";
import { escapeHtml } from "@/utils/sanitize";
const props = defineProps({
    content: {
        type: String,
        default: ""
    }
});

/**
 * 安全渲染：
 * - 只把形如 `[表情名]` 且在白名单中的内容替换成 <img>
 * - 其它所有文本一律 escapeHtml，避免 v-html 注入
 */
const html = computed(() => renderEmojiHtml(props.content, emojiElements));

function renderEmojiHtml(input: string, elements: { name: string; id: string }[]): string {
    if (!input) return "";

    const byName = new Map<string, string>();
    elements.forEach((e) => byName.set(e.name, e.id));

    const re = /\[([^\[\]]+)\]/g;
    let out = "";
    let lastIndex = 0;
    let m: RegExpExecArray | null;

    while ((m = re.exec(input))) {
        // 普通文本片段
        out += escapeHtml(input.slice(lastIndex, m.index));

        const name = m[1];
        const id = byName.get(name);
        if (id) {
            // 只允许输出固定域名的 emoji 图片
            out += `<img class="emoji" src="http://oss.tigshop.com/official/emoji/${encodeURIComponent(id)}.png" alt="${escapeHtml(name)}">`;
        } else {
            // 未识别的表情：原样作为文本展示（包含中括号）
            out += escapeHtml(m[0]);
        }

        lastIndex = re.lastIndex;
    }

    // 末尾剩余文本
    out += escapeHtml(input.slice(lastIndex));
    return out;
}
</script>

<style lang="less" scoped>
.emoji {
    width: 20px;
    height: 20px;
}
</style>
