<template>
    <view
        class="module-ad-con module-tag_nav"
        :style="
            commonStyle.moduleStyle.boxPaddingTop +
            commonStyle.moduleStyle.boxPaddingBottom +
            commonStyle.moduleStyle.boxPadding +
            commonStyle.moduleStyle.backgroundColor +
            commonStyle.moduleStyle.backgroundImage +
            commonStyle.moduleStyle.backgroundSize
        "
    >
        <view v-if="groupList.length" class="tag-nav-scroll">
            <view
                v-for="(group, index) in groupList"
                :key="index"
                class="tag-nav-item"
                :style="itemStyle(index)"
                @click="urlFormat(group.link) && redirect({ url: urlFormat(group.link) })"
            >
                <text class="tag-nav-text" :style="textStyle">{{ $t(group.title) }}</text>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { formatCommonStyle } from "@/components/modules";
import { urlFormat } from "@/utils/format";
import { redirect } from "@/utils";

const props = defineProps({
    module: {
        type: Object,
        default: () => ({})
    }
});

const commonStyle = computed(() => formatCommonStyle(props.module.moduleStyle ?? {}));

// Fallback 默认值与 admin defaultTagNav 同源
// (Tigshop-Admin/src/views/decorate/decorate/src/modules/index.ts)。
// admin 的 mergeDefaultModule 通常会补齐字段，移动端用相同默认值兜底，
// 保证老数据 / 不完整 payload 与画布渲染一致。
const groupList = computed(() => props.module.groupList ?? []);
const textWeight = computed(() => props.module.textWeight ?? 700);
const groupGap = computed(() => {
    const v = props.module.groupGap;
    return v === undefined || v === null ? 10 : Number(v) || 0;
});
const groupPadding = computed(() => {
    const v = props.module.groupPadding;
    return v === undefined || v === null ? 10 : Number(v) || 0;
});
const categoryColors = computed(() => props.module.categoryColors ?? []);
const titleColor = computed(() => props.module.titleColor ?? "#ffffff");
const titleBgColor = computed(() => props.module.titleBgColor ?? "rgba(255,255,255,0)");

// 标签文字样式：颜色 + 粗细 + 文字背景色（默认透明）
const textStyle = computed(() => {
    let str = `color:${titleColor.value || "#ffffff"};`;
    str += `font-weight:${textWeight.value};`;
    if (titleBgColor.value) {
        str += `background-color:${titleBgColor.value};`;
    }
    return str;
});

// 单个标签样式：水滴形造型 + 按 index%len 循环取渐变色 + 间距
// idx 在严格 TS 下从 v-for 推断为 number | string，内部统一转 number
const itemStyle = (idx: number | string): string => {
    const i = typeof idx === "number" ? idx : Number(idx);
    const colors = categoryColors.value;
    const c: any = colors.length ? colors[i % colors.length] : {};
    const a = c.colorA || "#cccccc";
    const b = c.colorB || c.colorA || "#cccccc";
    let str = `background-image:linear-gradient(to bottom, ${a}, ${b});`;
    str += "border-radius:50% 0 50% 50%;"; // 右上尖角，其余圆角，水滴形
    // 动态宽高保证正圆水滴：groupPadding 默认 10 时为 52×52
    const size = 32 + groupPadding.value * 2;
    str += `width:${size}px;height:${size}px;`;
    if (i !== groupList.value.length - 1) {
        str += `margin-right:${groupGap.value}px;`;
    }
    return str;
};
</script>

<style lang="scss" scoped>
.module-tag_nav {
    background-size: cover;
    background-repeat: no-repeat;
    background-position: center top;
    position: relative;
}
.tag-nav-scroll {
    display: flex;
    flex-wrap: nowrap;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    &::-webkit-scrollbar {
        display: none;
        height: 0;
    }
}
.tag-nav-item {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
}
.tag-nav-text {
    font-size: 16px;
    line-height: 1.2;
    white-space: nowrap;
    border-radius: 4px;
    padding: 0 4px;
}
</style>
