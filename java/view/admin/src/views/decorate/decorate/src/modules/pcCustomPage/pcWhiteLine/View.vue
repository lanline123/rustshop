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
import { computed, ref, Ref } from "vue";
import { TotalContainer } from "@/components/decorate/index";
import { ModuleType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame } from "@/views/decorate/decorate/src/modules/";

// 使用 defineModel 实现双向绑定，确保设置生效
const module = defineModel<ModuleType>("module", { default: () => ({}) }) as Ref<ModuleType>;

// 合并默认值，确保 frame 有初始值
const defaultModule = ref({
    lineType: 2,
    lineStyle: 1,
    frame: defaultFrame
});
mergeDefaultModule(module.value, defaultModule.value);

const { frame } = module.value;

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
