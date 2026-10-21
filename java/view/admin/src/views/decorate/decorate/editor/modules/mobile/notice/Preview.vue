<template>
    <div :class="'module-ad-con module-notice'" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="notice-ad-warp">
            <div class="notice-con" :style="frameFormat.backgroundColor + ' ' + frameFormat.boxRadius + itemFormat.itemRadius">
                <div class="notice-item">
                    <div class="item-content">
                        <div class="item-notice-con" :style="itemFormat.itemBackgroundColor + itemFormat.itemHeight">
                            <div class="item-notice-ico"><img :src="imageFormat(module?.icoPic.picUrl)" /></div>
                            <div class="item-notice-list">
                                <template v-if="module.nociceEffect === 'scroll'">
                                    <div class="item-notice-input scroll-animation" :style="`color:${itemFormat.textColor};`">
                                        <template v-for="(item, index) in list" :key="index">
                                            <div class="headlines-item">{{ item }}</div>
                                        </template>
                                    </div>
                                </template>
                                <template v-if="module.nociceEffect === 'still'">
                                    <div class="item-notice-input" :style="`color:${itemFormat.textColor};`">
                                        <div class="headlines-item">{{ module?.text }}</div>
                                    </div>
                                </template>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { imageFormat } from "@/utils/format";
import { mergeDefaultModule, defaultFrame, formatFrame, copyArray } from "@/views/decorate/decorate/src/modules/index";
import { noticeDefaults } from "./index";
import type { NoticeModule } from "./types";

const props = defineProps<{ module: NoticeModule }>();
const module = props.module;
// 旧 View.vue 渲染时的全局副作用，原样保留（契约）
defaultFrame.backgroundColor = "transparent";
const list = computed(() => {
    if (module.text) {
        return copyArray(module.text, 3);
    }
    return [];
});
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, noticeDefaults);
module.icoPic.picUrl = "https://oss.tigshop.com/img/gallery/202508/1754895669bP3iJSUiXSBoRB1VQq.png";
const { frame } = module;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
const itemFormat = computed(() => {
    return {
        itemBackgroundColor: `background-color:${module?.frame?.itemBackgroundColor};`,
        itemRadius: `border-radius:${module?.frame?.itemRadius}px;`,
        itemHeight: `height:${module?.frame?.itemHeight}px;`,
        textColor: `color:${module?.frame?.textColor};`
    };
});
</script>
<style lang="less" scoped>
.notice-con{
    overflow: hidden;
}
.item-notice-list {
    flex: 1;
    overflow: hidden;
    position: relative;
}

.item-notice-input {
    display: inline-flex;
    align-items: center;
    color: #555555;
    white-space: nowrap;

    &.scroll-animation {
        animation: marquee-scroll 13s linear infinite;
    }
}

.headlines-item {
    display: inline-block;
    padding-right: 30px;
}

@keyframes marquee-scroll {
    0% {
        transform: translateX(0);
    }
    100% {
        transform: translateX(-50%);
    }
}
</style>
