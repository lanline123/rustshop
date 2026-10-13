<template>
    <tig-image v-if="imgSrc" :src="imgSrc" />
</template>

<script lang="ts" setup>
/**
 * 表情图片组件
 * - 根据表情名称显示对应图片
 */
import { computed, ref } from "vue";
import emojiElements from "./emojiElements";
const props = defineProps({
    name: {
        type: String,
        default: ""
    },
    id: {
        type: [String, Number],
        default: ""
    }
});

function getIdByName(elements: { name: string; id: string }[], name: string): string | undefined {
    return elements.find((element) => element.name === name)?.id;
}
const imgSrc = ref("");

let imgSrcId = <string | undefined>"";
// 使用函数
if (props.id) {
    imgSrcId = String(props.id);
} else {
    imgSrcId = getIdByName(emojiElements, props.name);
}
if (imgSrcId !== undefined) {
    imgSrc.value = "https://oss.tigshop.com/official/emoji/" + imgSrcId + ".png";
}
</script>

<style lang="scss"></style>
