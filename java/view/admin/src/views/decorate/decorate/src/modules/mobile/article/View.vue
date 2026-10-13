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
                commonStyle.moduleContentStyle.innerPadding
            "
        > {{ $t('文章列表') }} </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed, Ref } from "vue";
import { ModuleType, ModuleArticleType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultModuleStyle, defaultContentStyle, formatCommonStyle, defaultAnimation } from "@/views/decorate/decorate/src/modules/";
import { imageFormat } from "@/utils/format";

const url = {
    picId: 1516,
    picThumb: "https://oss.tigshop.com/img/gallery/202411/1732167975uqaGwJU2aHObkmOmpb.png?x-oss-process=image/resize,m_pad,h_200,h_200",
    picUrl: "https://oss.tigshop.com/img/gallery/202411/1732167975uqaGwJU2aHObkmOmpb.png",
    picName: "FhZQpeGm_m6BK7kV9qd0Phm5ToF"
};
const module = defineModel<ModuleType & ModuleArticleType>("module") as Ref<ModuleType & ModuleArticleType>;
const defaultModule = ref({
    style: 1, //模块风格选择 1: 大图展示, 2: 两列展示(纵向) 3: 两列展示(横向)
    articleCategoryId: [], //文章分类ID
    articleNum: 10, //文章数量
    showTime: 1, //文章日期显示 0: 不显示 1: 显示
    showPageView: 1, //文章阅读数显示 0: 不显示 1: 显示
    showLike: 1, //文章点赞数显示 0: 不显示 1: 显示

    picRadius: 0, // 图片圆角
    articleNameWeight: 400, // 文章名称字体粗细
    moduleColor: {
        // 模块颜色设置
        articleNameColor: "#333333", // 文章名称颜色
        articleTimeColor: "#999999", // 文章日期颜色
        articleViewColor: "#999999", // 文章阅读数颜色
        articleLikeColor: "#999999", // 文章点赞颜色
        statisticColor: "#999999", // 统计数字颜色
    },
    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
});
mergeDefaultModule(module.value, defaultModule.value);
const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle ?? {}, module.value.contentStyle ?? {}, module.value.subTitle ?? {});
});
</script>
<style lang="less" scoped></style>
