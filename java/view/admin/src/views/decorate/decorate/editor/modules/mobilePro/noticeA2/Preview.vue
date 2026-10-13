<template>
    <div
        class="module-ad-con"
        :style="commonStyle.moduleStyle.boxPaddingTopBottom + commonStyle.moduleStyle.boxPadding + commonStyle.moduleStyle.backgroundColor"
    >
        <div
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
            <div class="main">
                <div class="headlines" style="width: calc(100% + 0px)">
                    <div class="headlines-icon" v-if="module.noticeIconType !== 'hide'">
                        <img
                            class="headlines-iconImg"
                            :style="`${module.noticeIconType === 'default' ? 'padding: 5px' : ''}`"
                            :src="imageFormat(module.iconPic?.picUrl)"
                            alt=""
                            srcset=""
                        />
                    </div>
                    <div class="headlines-container">
                        <template v-if="module.nociceEffect === 'scroll'">
                            <div :style="width">
                                <div
                                    class="headlines-text"
                                    :style="`justify-content:left; color:${module.contentColor}; animation-duration: ${module.scrollSpeed}s;`"
                                >
                                    <template v-for="(item, index) in list" :key="index">
                                        <div class="headlines-item">{{ item }}</div>
                                    </template>
                                </div>
                            </div>
                        </template>
                        <template v-if="module.nociceEffect === 'still'">
                            <div :style="width">
                                <div class="" :style="`justify-content:${module.textAlignment}; color:${module.contentColor};`">
                                    <div class="headlines-item">{{ module.noticeContent }}</div>
                                </div>
                            </div>
                        </template>
                    </div>
                    <div class="cross" v-if="module.closeIcon">
                        <span class="iconfont-h5 icon-chacha" :style="`color:${module.contentColor};`"></span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed } from "vue";
import { mergeDefaultModule, formatCommonStyle, copyArray } from "@/views/decorate/decorate/src/modules/index";
import { imageFormat } from "@/utils/format";
import { noticeA2Defaults } from "./index";
import type { NoticeA2Module } from "./types";

const props = defineProps<{ module: NoticeA2Module }>();
const module = props.module;
const list = computed(() => {
    if (module.noticeContent) {
        return copyArray(module.noticeContent, 3);
    }
    return [];
});
// 旧数据缺字段时烧录默认值（原地写回 store 模块对象，与旧 View.vue 行为一致，契约）
mergeDefaultModule(module, noticeA2Defaults);
const commonStyle = computed(() => {
    return formatCommonStyle(module.moduleStyle ?? {}, module.contentStyle ?? {}, module.subTitle ?? {});
});
console.log("commonStyle", commonStyle.value);

const width = computed(() => {
    let str = "width:auto";
    if (module.noticeContent && module.nociceEffect === "scroll") {
        const { digits, chineseChars, englishLetters, englishSymbols, chineseSymbols } = countCharacters(module.noticeContent);
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
    font-size: 18px;
    text-align: center;
    line-height: 30px;
}
.headlines-iconImg {
    height: 30px;
    display: block;
    margin-right: 5px;
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
