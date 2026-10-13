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
import { ref, computed, Ref } from "vue";
import { imageFormat } from "@/utils/format";
import { ModuleType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame, defaultIcoPic, copyArray } from "@/views/decorate/decorate/src/modules/";
// 在modules加入要使用的模块
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType>("module") as Ref<ModuleType>;
defaultFrame.backgroundColor = "transparent";
const defaultModule = ref({
    text: t('公告内容'),
    icoPic: defaultIcoPic,
    nociceEffect: "scroll", // 公告滚动效果 滚动 scroll  静止 still
    frame: defaultFrame
});
const list = computed(() => {
    if (module.value.text) {
        return copyArray(module.value.text, 3);
    }
    return [];
});
mergeDefaultModule(module.value, defaultModule.value);
module.value.icoPic.picUrl = "https://oss.tigshop.com/img/gallery/202508/1754895669bP3iJSUiXSBoRB1VQq.png";
const { frame } = module.value;
const frameFormat = computed(() => {
    return formatFrame(frame ?? {});
});
const itemFormat = computed(() => {
    return {
        itemBackgroundColor: `background-color:${module?.value?.frame?.itemBackgroundColor};`,
        itemRadius: `border-radius:${module?.value?.frame?.itemRadius}px;`,
        itemHeight: `height:${module?.value?.frame?.itemHeight}px;`,
        textColor: `color:${module?.value?.frame?.textColor};`
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
