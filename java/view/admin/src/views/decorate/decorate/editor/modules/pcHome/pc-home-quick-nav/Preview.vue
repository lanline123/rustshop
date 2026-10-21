<template>
    <!-- 快捷导航 - 使用 BEM 命名规范（dec-pc-home-quick-nav），内部包含原生 class -->
    <div class="dec-pc-home-quick-nav" :style="cssVars">
        <div class="nav_container fs_mod">
            <div v-if="!navItems.length" class="dec-pc-home-quick-nav__empty">请在设置中添加导航项</div>
            <template v-else>
                <a
                    v-for="(item, index) in navItems"
                    :key="item.picId || index"
                    class="dec-pc-home-quick-nav__item"
                    :href="toHref(item.picLink)"
                    target="_blank"
                    rel="noopener noreferrer"
                    :title="item.picTitle"
                >
                    <img v-if="getIconUrl(item)" class="dec-pc-home-quick-nav__icon" :src="getIconUrl(item)" :alt="item.picTitle || ''" />
                    <!-- 文字 -->
                    <span class="dec-pc-home-quick-nav__text">{{ item.picTitle }}</span>
                </a>
            </template>
        </div>
    </div>
</template>

<script setup lang="ts">
/**
 * PC 首页快捷导航模块 - 预览组件
 * 渲染 nav_container fs_mod 的 DOM 结构
 * 图左文右布局，样式通过 CSS 变量驱动
 */
import { computed } from "vue";
import { productImage } from "@/utils/format";
import type { LinkItem, PicListItem } from "../_shared/types";
import type { PcHomeQuickNavModule } from "./types";
import { pcHomeQuickNavDefaults } from ".";
import { deepMerge } from "../../../utils/object";

defineOptions({ name: "PcHomeQuickNavPreview" });

// 链接类型（同 Vort 公共 LinkType：字符串或链接对象）
type LinkType = string | LinkItem;

const props = defineProps<{
    module: Record<string, unknown>;
}>();

/**
 * 合并默认值后的模块配置
 */
const moduleConfig = computed<PcHomeQuickNavModule>(() => {
    return deepMerge(pcHomeQuickNavDefaults, props.module as Partial<PcHomeQuickNavModule>);
});

/**
 * 导航项列表（PicListItem 格式）
 */
const navItems = computed<PicListItem[]>(() => {
    const list = moduleConfig.value.items;
    return Array.isArray(list) ? list : [];
});

/**
 * CSS 变量（传递动态值到样式）
 */
const cssVars = computed(() => {
    const data = moduleConfig.value;
    const frame = data.frame;
    const style = data.style;
    const containerAlign = style?.containerAlign ?? 1;
    const containerJustify = containerAlign === 2 ? "center" : containerAlign === 3 ? "flex-end" : "flex-start";

    return {
        // 容器级变量
        "--dec-box-padding": `${frame?.boxPadding ?? 0}px`,
        "--dec-box-padding-top": `${frame?.boxPaddingTop ?? 0}px`,
        "--dec-box-padding-bottom": `${frame?.boxPaddingBottom ?? 0}px`,
        "--dec-box-radius": `${frame?.boxRadius ?? 0}px`,
        "--dec-bg-color": frame?.backgroundColor || "transparent",
        // 文字变量
        "--dec-font-size": `${style?.fontSize ?? 14}px`,
        "--dec-text-color": style?.textColor || "#333333",
        "--dec-hover-color": style?.hoverColor || "#e1251b",
        // 布局变量
        "--dec-container-justify": containerJustify,
        "--dec-item-gap": `${style?.itemGap ?? 20}px`,
        "--dec-single-line": style?.singleLine === 1 ? "nowrap" : "wrap",
        // 新增样式变量
        "--dec-font-weight": style?.fontWeight ?? 400,
        "--dec-line-height": `${style?.lineHeight ?? 20}px`,
        "--dec-icon-size": `${style?.iconSize ?? 16}px`,
        "--dec-icon-gap": `${style?.iconGap ?? 4}px`
    };
});

function getIconUrl(item: PicListItem): string {
    return productImage(item);
}

/**
 * 将 LinkType 转换为 href 字符串
 */
function toHref(link?: LinkType): string {
    if (!link) return "javascript:;";
    if (typeof link === "string") return link || "javascript:;";
    return link.link || "javascript:;";
}
</script>

<style lang="less" src="./preview.less" />
