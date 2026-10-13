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
import { ref, computed, Ref } from "vue";
import { ModuleType, ModuleNoticeA2Type } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultModuleStyle, defaultContentStyle, formatCommonStyle, copyArray } from "@/views/decorate/decorate/src/modules/";
import { imageFormat } from "@/utils/format";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const module = defineModel<ModuleType & ModuleNoticeA2Type>("module") as Ref<ModuleType & ModuleNoticeA2Type>;
const list = computed(() => {
    if (module.value.noticeContent) {
        return copyArray(module.value.noticeContent, 3);
    }
    return [];
});
const defaultModule = ref({
    noticeContent: t('本店新用户首单0元起送，更多大牌好物低至6.6折起！！'), // 公告内容
    nociceLink: {
        // 公告链接
        link: {
            link: "",
            title: ""
        }
    },
    nociceEffect: "scroll", // 公告滚动效果 滚动 scroll  静止 still
    scrollSpeed: 13, // 滚动效果为 scroll 时有效, 公告滚动耗时  13 秒
    textAlignment: "center", // 滚动效果 still 时有效, 公告内容对齐方式 左对齐 left  居中 center  右对齐 right
    noticeIconType: "default", // 公告图标类型  default 默认 hide 隐藏  custom 自定义
    iconPic: {
        picUrl: "",
        picThumb: ""
    }, // 自定义公告图标
    contentColor: "#333333", // 公告文字颜色
    closeIcon: 0, // 关闭图标 1 显示 0 隐藏
    moduleStyle: defaultModuleStyle, // 模块样式
    contentStyle: defaultContentStyle // 内容样式
});
mergeDefaultModule(module.value, defaultModule.value);
const commonStyle = computed(() => {
    return formatCommonStyle(module.value.moduleStyle ?? {}, module.value.contentStyle ?? {}, module.value.subTitle ?? {});
});
console.log("commonStyle", commonStyle.value);

const width = computed(() => {
    let str = "width:auto";
    if (module.value.noticeContent && module.value.nociceEffect === "scroll") {
        const { digits, chineseChars, englishLetters, englishSymbols, chineseSymbols } = countCharacters(module.value.noticeContent);
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
