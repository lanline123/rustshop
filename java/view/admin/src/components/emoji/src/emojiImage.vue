<template>
    <img v-if="imgSrc" :src="imgSrc" />
</template>

<script lang="ts" setup>
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
    imgSrc.value = "http://oss.tigshop.com/official/emoji/" + imgSrcId + ".png";
}
</script>

<style lang="less"></style>
