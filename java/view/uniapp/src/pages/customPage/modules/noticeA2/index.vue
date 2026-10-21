<template>
    <view
        v-if="show && list.length"
        class="module-ad-con"
        :style="commonStyle.moduleStyle.boxPaddingTopBottom + commonStyle.moduleStyle.boxPadding + commonStyle.moduleStyle.backgroundColor"
    >
        <view
            class="module-ad-content"
            :style="
                commonStyle.moduleContentStyle.backgroundColor +
                commonStyle.moduleContentStyle.backgroundImage +
                commonStyle.moduleContentStyle.backgroundSize +
                commonStyle.moduleContentStyle.boxRadius2 +
                commonStyle.moduleContentStyle.boxShadow +
                commonStyle.moduleContentStyle.paddingTopBottom +
                commonStyle.moduleContentStyle.paddingLeftRight
            "
        >
            <view class="main">
                <view class="headlines" style="width: calc(100% + 0px)">
                    <template v-if="module.noticeIconType !== 'hide'">
                        <view class="headlines-icon">
                            <image
                                class="headlines-iconImg"
                                :class="{ 'default-icon': module.noticeIconType === 'default' }"
                                :src="imageFormat(module.iconPic?.picUrl)"
                                :mode="module.noticeIconType === 'default' ? '' : 'heightFix'"
                            />
                        </view>
                    </template>

                    <view class="headlines-container" @click="urlFormat(module.nociceLink) && redirect({ url: urlFormat(module.nociceLink) })">
                        <template v-if="module.nociceEffect === 'scroll'">
                            <view :style="width">
                                <view
                                    class="headlines-text"
                                    :style="`justify-content:left; color:${module.contentColor}; animation-duration: ${module.scrollSpeed}s;`"
                                >
                                    <template v-for="(item, index) in list" :key="index">
                                        <view class="headlines-item">{{ $t(item) }}</view>
                                    </template>
                                </view>
                            </view>
                        </template>
                        <template v-if="module.nociceEffect === 'still'">
                            <view :style="width">
                                <view class="" :style="`justify-content:${module.textAlignment}; color:${module.contentColor};`">
                                    <view class="headlines-item">{{ $t(module.noticeContent) }}</view>
                                </view>
                            </view>
                        </template>
                    </view>
                    <view v-if="Number(module.closeIcon) === 1" class="cross" @click="show = false">
                        <span class="iconfont-h5 icon-chacha" :style="`color:${module.contentColor};`" />
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { formatCommonStyle, copyArray } from "@/components/modules";
import { urlFormat, imageFormat } from "@/utils/format";
import { redirect } from "@/utils";

const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});
const commonStyle = computed(() => {
    return formatCommonStyle(
        props.module.moduleStyle ?? {
            boxMarginTop: 5,
            boxMarginBottom: 0,
            boxMargin: 10,
            backgroundColor: "",
            backgroundPic: {
                picUrl: "",
                picThumb: ""
            },
            backgroundPicFillType: "cover"
        },
        props.module.contentStyle ?? {
            paddingTop: 0,
            paddingBottom: 0,
            paddingLeftRight: 0,
            padding: 0,
            boxRadiusTop: 0,
            boxRadiusBottom: 0,
            gradientType: "upDown",
            gradientColorA: "#ffffff",
            gradientColorB: "#ffffff",
            backgroundPic: {
                picUrl: "",
                picThumb: ""
            },
            backgroundPicFillType: "cover",
            boxShadow: 0
        },
        props.module.subTitle ?? {}
    );
});
const list = computed(() => {
    if (props.module.noticeContent) {
        return copyArray(props.module.noticeContent, 3);
    }
    return [];
});

const width = computed(() => {
    let str = "width:auto";
    if (props.module.noticeContent && props.module.nociceEffect === "scroll") {
        const { digits, chineseChars, englishLetters, englishSymbols, chineseSymbols } = countCharacters(props.module.noticeContent);
        str = `width: ${digits * 8 + chineseChars * 14 + englishLetters * 8 + englishSymbols * 4 + chineseSymbols * 14 + 20}px;`;
        return str;
    }
    return str;
});
function countCharacters(str: string) {
    const result = {
        digits: 0,
        chineseChars: 0,
        englishLetters: 0,
        englishSymbols: 0,
        chineseSymbols: 0
    };

    for (let i = 0; i < str.length; i++) {
        const char = str[i];

        // 检测数字
        if (/\d/.test(char)) {
            result.digits++;
        }
        // 检测汉字
        else if (/[\u4e00-\u9fa5]/.test(char)) {
            result.chineseChars++;
        }
        // 检测英文字母
        else if (/[a-zA-Z]/.test(char)) {
            result.englishLetters++;
        }
        // 检测英文符号
        else if (/[!"#$%&'()*+,-./:;<=>?@[\\\]^_`{|}~]/.test(char)) {
            result.englishSymbols++;
        }
        // 检测中文符号
        else if (/[\u3000-\u303f\u309b-\u309f\u30fb\uff00-\uffef]/.test(char)) {
            result.chineseSymbols++;
        }
    }

    return result;
}

const show = ref(true);
</script>

<style lang="scss" scoped>
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
.main {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0px;
    border: none;
    border-radius: 0px;
    background-color: transparent;
    overflow: hidden;
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center top;
}
.headlines {
    display: flex;
    align-items: center;
    height: 30px;
}

.headlines-icon {
    height: 30px;
    display: flex;
    align-items: center;
}
.headlines-iconImg {
    height: 30px;
    display: block;
    margin-right: 5px;
    overflow: hidden;

    &.default-icon {
        width: 20px;
        height: 20px;
        padding-left: 5px;
    }
}

.headlines-container {
    height: 30px;
    flex: 1;
    line-height: 30px;
    overflow: hidden;
    font-size: 14px;
    font-weight: 400;
}
.headlines-text {
    position: relative;
    display: flex;
    animation-duration: 10s;
    animation: left 5000ms linear infinite running;
}
.headlines-item {
    flex-shrink: 0;
    white-space: nowrap;
    padding-right: 20px;
}
.cross {
    width: 28px;
    text-align: center;
}
@keyframes left {
    0% {
        left: -100%;
    }
    100% {
        left: -200%;
    }
}
</style>
