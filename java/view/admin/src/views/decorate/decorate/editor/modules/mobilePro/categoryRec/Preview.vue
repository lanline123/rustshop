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
                commonStyle.moduleContentStyle.boxRadius +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding
            "
        >
            <div class="categoryrec">
                <div class="item-box">
                    <template v-for="item in module.categoryList" :key="item">
                        <div class="li" :style="AnimationFormat + `border-radius: ${module.radius}px;background-color:${module.backgroundColor}`">
                            <div
                                class="item"
                                :style="
                                    `padding: ${module.categoryPadding}px 15px; border-radius: ${module.radius}px; background-color:${item.backgroundColor};` +
                                    borderStyle
                                "
                            >
                                <img class="img" :src="imageFormat(imageFormat(item.catPic?.picUrl))" alt="" />
                                <div
                                    class="tit"
                                    :style="`font-size: ${module.subTitle?.titleFontSize}px; font-weight: ${module.subTitle?.titleFontBold}; color: ${item.titleColor ? item.titleColor : module.subTitle?.titleColor}`"
                                >
                                    {{ item.title }}
                                </div>
                            </div>
                        </div>
                    </template>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { mergeDefaultModule, formatCommonStyle } from "@/views/decorate/decorate/src/modules/index";
import { imageFormat } from "@/utils/format";
import { categoryRecDefaults } from "./index";
import type { CategoryRecModule } from "./types";

const props = defineProps<{ module: CategoryRecModule }>();
const module = props.module;
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, categoryRecDefaults);
const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle ?? {}, module.contentStyle ?? {}, module.subTitle ?? {});
});
const AnimationFormat = computed(() => {
    return `transform: translate(0px, 0px) scale(1) rotateY(0deg);opacity:1;`;
});
const transitionDuration = (index: number) => {
    return `transition-duration:0s;`;
};
const borderStyle = computed(() => {
    let str = "";
    if (Number(module.catBorder) === 1) {
        str = `border: ${module.catBorderWidth || 1}px solid ${module.catBorderColor || "#ffffff"};`;
    }
    return str;
});
</script>
<style lang="less" scoped>
.decorate-slide-box::-webkit-scrollbar {
    display: none;
    height: 0;
}
.module-ad-con {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center top;
    position: relative;
}
.categoryrec {
    overflow: hidden;
}
.item-box {
    width: calc(100% + 10px);
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    box-sizing: border-box;

    .li {
        margin: 5px 10px 5px 0;

        .item {
            height: 30px;
            display: flex;
            text-align: center;
            overflow: hidden;
            align-items: center;
            box-sizing: content-box;
            .tit {
                height: 24px;
                line-height: 24px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .img {
                margin-right: 5px;
                height: 100%;
                width: auto;
                display: block;
            }
        }
    }
}
</style>
