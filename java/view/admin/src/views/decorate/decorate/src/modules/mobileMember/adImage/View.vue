<template>
    <div class="module-ad-con module-member-ad-image" :style="frameFormat.boxPadding + frameFormat.boxPaddingTop + frameFormat.boxPaddingBottom">
        <div class="module-ad-content">
            <div class="ad-image-wrap" :style="wrapStyle">
                <div class="ad-image-container" :style="containerStyle">
                    <template v-if="!module.pic?.picUrl">
                        <div class="module-ad-empty">
                            <div class="image-empty-bg"></div>
                            <div class="desc">{{ $t('点击添加图片') }}</div>
                        </div>
                    </template>
                    <template v-else>
                        <img class="ad-image-pic" :src="module.pic.picUrl" alt="广告图" />
                    </template>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, Ref } from "vue";
import { ModuleType, ModuleFrameType, ModuleIcoPicType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultFrame, formatFrame } from "@/views/decorate/decorate/src/modules/";

interface AdImageLink {
    link?: string;
    name?: string;
    label?: string;
    data?: Record<string, any>;
}
type ModuleAdImageType = Omit<ModuleType, "frame"> & {
    pic?: ModuleIcoPicType;
    picLink?: AdImageLink;
    frame: ModuleFrameType;
};

const module = defineModel<ModuleAdImageType>("module") as Ref<ModuleAdImageType>;

const defaultPic = {
    picUrl: "https://oss.tigshop.com/img/gallery/202605/1779689214Nbzv6Iw3ldEVFucDjc.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200",
    picThumb: "https://oss.tigshop.com/img/gallery/202605/1779689214Nbzv6Iw3ldEVFucDjc.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200"
};
const defaultInnerPadding = 0;
const defaultModule = {
    pic: { ...defaultPic },
    picLink: { link: "", name: "" },
    frame: {
        ...defaultFrame,
        backgroundColor: "",
        boxRadius: 12,
        innerPadding: defaultInnerPadding,
        boxPadding: 5,
        boxPaddingTop: 10,
        boxPaddingBottom: 0
    }
};
mergeDefaultModule(module.value, defaultModule);

const frame = computed(() => module.value.frame ?? {});
const frameFormat = computed(() => formatFrame(frame.value));

const wrapStyle = computed(() => ({
    backgroundColor: frame.value.backgroundColor || "transparent",
    borderRadius: `${frame.value.boxRadius ?? 0}px`
}));

const containerStyle = computed(() => ({
    padding: `${frame.value.innerPadding ?? defaultInnerPadding}px`
}));
</script>
<style lang="less" scoped>
.ad-image-wrap {
    box-sizing: border-box;
    overflow: hidden;
}
.ad-image-container {
    box-sizing: border-box;
}
.ad-image-pic {
    width: 100%;
    display: block;
}
</style>
