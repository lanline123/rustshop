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

const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});

const styleFormat = computed(() => {
    return {
        backgroundColor: props.module.backgroundColor ? `background-color: ${props.module.backgroundColor};` : "",
        height: props.module.blankHeight ? `height: ${props.module.blankHeight}px;` : "height: 30px;"
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
