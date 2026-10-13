<template>
   <div v-if="styleOptions.btnType || styleOptions.text">
       <div
            class="goods-btn"
            :style="
                `height: ${Number(styleOptions.paddingTopBottom || 0) + 24}px; padding-right: ${styleOptions.text ? `${Number(styleOptions.paddingLeftRight || 0) + 10}px` : 0}; border-radius: ${styleOptions.radius}px; color: ${styleOptions.textColor}; font-weight: 400;` +
                background + paddingLeft
            "
        >
            <div
                v-if="styleOptions.btnType"
                class="btn-icon"
                :style="`margin-left: ${styleOptions.text ? '5px' : 0}; margin-right: ${styleOptions.text ? '-2px' : 0}px; width: ${iconBoxWidth}px; background: none; color: ${styleOptions.iconColor}; font-weight: 400; font-size: ${iconSizeVal}px;`"
            >
                <template v-if="styleOptions.btnType === 1">
                    <span class="iconfont-h5 icon-gouwuche"></span>
                </template>
                <template v-if="styleOptions.btnType === 2">
                    <span class="iconfont-h5 icon-gouwuche1"></span>
                </template>
                <template v-if="styleOptions.btnType === 3">
                    <span class="iconfont-h5 icon-jia"></span>
                </template>
                <template v-if="styleOptions.btnType === 4">
                    <span class="iconfont-h5 icon-jia1"></span>
                </template>
                <template v-if="styleOptions.btnType === 5 && iconImageSrc">
                    <img class="btn-icon-img" :src="imageFormat(iconImageSrc)" :style="`width: ${iconSizeVal}px; height: ${iconSizeVal}px;`" alt="" />
                </template>
            </div>
            <div class="btn-text" v-if="styleOptions.text" :style="`font-size: ${textSizeVal}px;`">{{ styleOptions.text }}</div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { gradientMap } from "@/views/decorate/decorate/src/modules/";
import { imageFormat, pickPic } from "@/utils/format";
import { computed } from "vue";
const props = defineProps({
    styleOptions: {
        type: Object,
        default: () => ({
            showBtn: 1, //是否显示按钮
            btnType: 1, //按钮类型
            text: "", //按钮文字
            textColor: "#ffffff", //按钮文字颜色
            iconColor: "#ffffff", //按钮图标颜色
            gradientType: "leftRight", //按钮背景渐变类型
            gradientColorA: "#333333", //按钮背景渐变颜色A
            gradientColorB: "#333333", //按钮背景渐变颜色B
            radius: 16, //按钮圆角
            paddingTopBottom: 0, //按钮高度
            paddingLeftRight: 10 //按钮宽度
        })
    }
});

// 新增字段对历史草稿做兜底：mergeDefaultModule 只浅合并，旧 buyBtnStyle 对象可能缺这些子字段
const iconSizeVal = computed(() => props.styleOptions.iconSize ?? 14);
const textSizeVal = computed(() => props.styleOptions.textSize ?? 12);
const iconBoxWidth = computed(() => Math.max(iconSizeVal.value, 24));
const iconImageSrc = computed(() => {
    const img = props.styleOptions.iconImage;
    if (!img || typeof img !== "object") return "";
    return pickPic(img);
});

const background = computed(() => {
    let str = "";
    if (props.styleOptions.gradientType !== "purity") {
        str = `background-image: linear-gradient(${gradientMap[props.styleOptions.gradientType]}, ${props.styleOptions.gradientColorA}, ${props.styleOptions.gradientColorB});`;
    } else {
        str = `background-color:${props.styleOptions.gradientColorA};`;
    }
    return str;
});

const paddingLeft = computed(() => {
    let str = "padding-left:0";
    if (props.styleOptions.text) {
        str = `padding-left: ${props.styleOptions.paddingLeftRight}px;`;
        if (!props.styleOptions.btnType) {
            str = `padding-left: ${Number(props.styleOptions.paddingLeftRight || 0) + 10}px;`;
        }
    }
    return str;
});
</script>

<style lang="less" scoped>
.goods-btn {
    margin: 0 auto;
    padding-right: 10px;
    min-width: 24px;
    height: 24px;
    line-height: 24px;
    text-align: center;
    overflow: hidden;
    white-space: nowrap;
    display: inline-flex;
    align-items: center;
    justify-content: center;
}

.btn-icon {
    font-size: 14px;
    width: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
}

.btn-icon-img {
    display: inline-block;
    vertical-align: middle;
    object-fit: contain;
}

.btn-text {
    font-size: 12px;
    white-space: nowrap;
}
</style>
