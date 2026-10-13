<template>
    <TotalContainer :module="module.frame" :class="'module-ad-con module-white_blank'">
        <div class="white-blank-warp">
            <div class="white-blank-con" :style="styleFormat.backgroundColor + styleFormat.height" />
        </div>
    </TotalContainer>
</template>

<script lang="ts" setup>
//pc端装修空白辅助组件
import { computed } from "vue";
import { TotalContainer } from "@/components/decorate/index";
import type { PcWhiteBlankModule } from "./types";

// 旧 View.vue 即纯 props 组件且无默认值烧录，保持一致（契约）
const props = defineProps<{ module: PcWhiteBlankModule }>();
const module = props.module;

const styleFormat = computed(() => {
    return {
        backgroundColor: module.backgroundColor ? `background-color: ${module.backgroundColor};` : "",
        height: module.blankHeight ? `height: ${module.blankHeight}px;` : "height: 30px;"
    };
});
</script>
<style lang="less" scoped>
/*空白*/
.white-blank-warp {
    width: 100%;
    box-sizing: border-box;

    .white-blank-con {
        width: 100%;
        height: 30px;
        min-height: 10px;
    }
}
</style>
