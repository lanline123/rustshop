<template>
    <div
        class="module-ad-con"
        :style="
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.backgroundSize +
            commonStyle.moduleStyle.backgroundImage
        "
    >
        <div
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius2 +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding +
                commonStyle.moduleContentStyle.backgroundColor
            "
        >
            <div class="imagetext">
                <div class="image-box" :style="`border: ${module.picBorder}px solid ${module.picBorderColor}; border-radius: ${module.picRadius}px`">
                    <img class="img" :src="imageFormat(module.picUrl?.picUrl)" alt="" />
                </div>
                <div class="intro-box">
                    <div
                        class="title"
                        :style="`color: ${module.titColor}; text-align:  ${module.titTextAlignment}; margin-bottom: ${module.titMarginBottom}px`"
                    >
                        {{ module.title }}
                    </div>
                    <div class="intro" :style="`color: ${module.descColor}; text-align: ${module.descTextAlignment}`">
                        {{ module.description }}
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, Ref } from "vue";
import { ModuleType, ModuleImageTextComponentType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultModuleStyle, defaultContentStyle, formatCommonStyle } from "@/views/decorate/decorate/src/modules/";
import { imageFormat } from "@/utils/format";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const picUrl = {
    picUrl: "https://oss.tigshop.com/img/gallery/202411/1732607856bEOJCFY5XLpF59AK7v.png",
    picThumb: "https://oss.tigshop.com/img/gallery/202411/1732607856bEOJCFY5XLpF59AK7v.png?x-oss-process=image/resize,m_pad,h_200,h_200",
    picId: 1518,
    picName: "FkByzE0nQeaPjfsodjaNYht8a6S"
};
const module = defineModel<ModuleType & ModuleImageTextComponentType>("module") as Ref<ModuleType & ModuleImageTextComponentType>;
const defaultModule = ref({
    picUrl: picUrl, //海波设置
    title: t('关于我们'), //标题文字
    description: t('我们是一个全类目的商城平台，应有尽有。其中包括休闲零食、日用百货、医疗健康、美容护理等类目，让你不出门就能买到你想要的。'), //图片介绍文字
    picBorder: 0, //图片边框
    picBorderColor: "#EEEEEE", //图片边框颜色
    picRadius: 0, //图片圆角
    titTextAlignment: "center", //标题对齐方式
    titMarginBottom: 10, //标题介绍间距
    titColor: "#333333", //标题颜色
    descTextAlignment: "center", //介绍对齐方式
    descColor: "#333333", //介绍颜色

    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
});
mergeDefaultModule(module.value, defaultModule.value);
const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle ?? {}, module.value.contentStyle ?? {}, module.value.subTitle ?? {});
});
</script>
<style lang="less" scoped>
.module-ad-con {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center top;
    position: relative;
}
.image-box {
    overflow: hidden;
    .img {
        width: 100%;
        display: block;
    }
}

.intro-box {
    padding: 15px 10px 5px;
    .title {
        font-size: 20px;
        font-weight: 700;
    }
    .intro {
        font-size: 14px;
        line-height: 24px;
        width: 100%;
        overflow: hidden;
        white-space: pre-wrap;
    }
}
</style>
