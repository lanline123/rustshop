<template>
    <view
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
        <view
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.innerPadding +
                (module.contentStyle?.paddingTop ? commonStyle.moduleContentStyle.paddingTop : '')
            "
        >
            <view class="categoryrec">
                <view class="item-box">
                    <template v-for="(item, index) in module.categoryList" :key="index">
                        <view
                            class="li"
                            :style="
                                AnimationFormat + transitionDuration(index) + `border-radius: ${module.radius}px;background-color:${module.backgroundColor}`
                            "
                            @click="urlFormat(item.link) && redirect({ url: urlFormat(item.link) })"
                        >
                            <view
                                class="item"
                                :style="
                                    `padding: ${module.categoryPadding}px 15px; border-radius: ${module.radius}px; background-color:${item.backgroundColor};` +
                                    borderStyle
                                "
                            >
                                <image v-if="item.catPic?.picUrl" class="img" mode="heightFix" :src="imageFormat(item.catPic.picUrl)" alt="" />
                                <view
                                    class="tit"
                                    :style="`font-size: ${module.subTitle?.titleFontSize}px; font-weight: ${module.subTitle?.titleFontBold}; color: ${
                                        item.titleColor ? item.titleColor : module.subTitle?.titleColor
                                    }`"
                                >
                                    {{ $t(item.title) }}
                                </view>
                            </view>
                        </view>
                    </template>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { formatCommonStyle } from "@/components/modules";
import { urlFormat, imageFormat } from "@/utils/format";
import { redirect } from "@/utils";
import { useAnimation } from "@/hooks/useDecorate";
const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});
const { AnimationFormat, show, transitionDuration } = useAnimation(".item-box", props.module.animation);

const commonStyle = computed(() => {
    return formatCommonStyle(props.module.moduleStyle ?? {}, props.module.contentStyle ?? {}, props.module.subTitle ?? {});
});

const borderStyle = computed(() => {
    let str = "";
    if (Number(props.module.catBorder) === 1) {
        str = `border: ${props.module.catBorderWidth || 1}px solid ${props.module.catBorderColor || "#ffffff"};`;
    }
    return str;
});
</script>

<style lang="scss" scoped>
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
