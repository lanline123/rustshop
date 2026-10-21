<template>
    <div
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
        <div v-if="isEmpty" class="module-ad-empty">
            <div class="desc">{{ $t('点击添加标签') }}</div>
        </div>
        <div v-else class="tag-nav-scroll">
            <div v-for="(group, index) in module.groupList" :key="index" class="tag-nav-item" :style="itemStyle(index)">
                <span class="tag-nav-text" :style="textStyle">{{ group.title }}</span>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, type Ref } from "vue";
import { ModuleType, ModuleTagNavType } from "@/types/decorate/decorate.d";
import { mergeDefaultModule, defaultTagNav, formatCommonStyle } from "@/views/decorate/decorate/src/modules/";

const module = defineModel<ModuleType & ModuleTagNavType>("module") as Ref<ModuleType & ModuleTagNavType>;
mergeDefaultModule(module.value, defaultTagNav);

const commonStyle = computed(() => formatCommonStyle((module.value.moduleStyle ?? {}) as any));

const isEmpty = computed(() => !module.value.groupList || module.value.groupList.length === 0);

// 标签文字样式：颜色 + 粗细 + 文字背景色（默认透明）
const textStyle = computed(() => {
    let str = `color:${module.value.titleColor || "#ffffff"};`;
    str += `font-weight:${module.value.textWeight ?? 700};`;
    return str;
});

// 单个标签样式：水滴形造型 + 按 index%6 循环取渐变色 + 间距/内边距
const itemStyle = (index: number) => {
    const colors = module.value.categoryColors ?? [];
    const c = colors.length ? colors[index % colors.length] : ({} as { colorA?: string; colorB?: string });
    const a = c.colorA || "#cccccc";
    const b = c.colorB || c.colorA || "#cccccc";
    let str = `background-image:linear-gradient(to bottom, ${a}, ${b});`;
    str += "border-radius:50% 0 50% 50%;"; // 右上尖角，其余圆角，完美圆形水滴
    // 动态计算宽高，保证其始终为正圆形。当 groupPadding 为默认 10 时，大小为 52px * 52px
    const size = 32 + (module.value.groupPadding ?? 10) * 2;
    str += `width:${size}px;height:${size}px;`;
    str += `margin-right:${module.value.groupGap ?? 10}px;`;
    return str;
};
</script>
<style lang="less" scoped>
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
    &:last-child {
        margin-right: 0 !important;
    }
}
.tag-nav-text {
    font-size: 16px;
    line-height: 1.2;
    white-space: nowrap;
}
.module-ad-empty {
    height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f7f8fa;
    color: #969799;
    font-size: 13px;
}
</style>
