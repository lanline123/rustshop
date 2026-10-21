<template>
    <TotalContainer :module="module.frame" :class="'module-ad-con module-white_line ad-style__' + module.lineType + ' ad-line_style__' + module.lineStyle">
        <div class="white-line-warp">
            <div class="white-line-con">
                <div class="white-line-item">
                    <div class="item-content">
                        <div class="item-line" />
                    </div>
                </div>
            </div>
        </div>
    </TotalContainer>
</template>

<script lang="ts" setup>
//pc端装修分割线组件
import { computed } from "vue";
import { TotalContainer } from "@/components/decorate/index";
import { mergeDefaultModule, formatFrame } from "@/views/decorate/decorate/src/modules/index";
import { pcWhiteLineDefaults } from "./index";
import type { PcWhiteLineModule } from "./types";

const props = defineProps<{ module: PcWhiteLineModule }>();
const module = props.module;

// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, pcWhiteLineDefaults);

const { frame } = module;

// 格式化 frame 设置
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
</script>
<style lang="less" scoped>
/*线条*/
.white-line-warp {
    width: 100%;
    box-sizing: border-box;

    .white-line-con {
        //padding-top: 15px;
        //padding-bottom: 15px;

        .white-line-item {
            height: 1px;
            width: 100%;
            border-bottom: 1px solid #e5e5e5;
        }
    }
}
.ad-line_style__2 .white-line-warp .white-line-con .white-line-item {
    border-bottom: 1px dashed #e5e5e5;
}
.ad-line_style__3 .white-line-warp .white-line-con .white-line-item {
    border-bottom: 1px dotted #e5e5e5;
}
.ad-style__1 .white-line-warp .white-line-con .white-line-item {
    border-bottom: none;
}
</style>
