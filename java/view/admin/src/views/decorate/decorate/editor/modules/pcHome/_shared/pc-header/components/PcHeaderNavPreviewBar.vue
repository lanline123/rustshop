<template>
    <ul class="sub-nav" :style="subNavStyle">
        <li v-for="(item, index) in navItems" :key="index">
            <a>{{ item }}</a>
        </li>
    </ul>
</template>

<script lang="ts" setup>
import { computed } from "vue";

const props = withDefaults(
    defineProps<{
        paddingTop?: number;
        paddingBottom?: number;
        /** 导航项文案，缺省用演示导航 */
        items?: string[];
    }>(),
    {
        paddingTop: 0,
        paddingBottom: 0,
        items: undefined
    }
);

const FALLBACK_NAV = ["首页", "导航1", "导航2"];
const navItems = computed(() => (props.items && props.items.length ? props.items : FALLBACK_NAV));

const subNavStyle = computed(() => {
    const paddingTop = Math.max(0, Number(props.paddingTop ?? 0) || 0);
    const paddingBottom = Math.max(0, Number(props.paddingBottom ?? 0) || 0);
    return {
        "--pc-header-nav-margin-top": `${paddingTop}px`,
        "--pc-header-nav-margin-bottom": `${paddingBottom}px`
    } as Record<string, string>;
});
</script>

<style lang="less" scoped>
.sub-nav {
    display: flex;
    align-items: center;
    justify-content: flex-start;
    min-height: 42px;
    height: auto;
    gap: 45px;
    row-gap: 8px;
    flex-wrap: wrap;
    margin: 0;
    margin-top: var(--pc-header-nav-margin-top, 0px);
    margin-bottom: var(--pc-header-nav-margin-bottom, 0px);
    min-width: 0;
    width: 100%;
    box-sizing: border-box;
    padding: 0;

    li {
        text-align: center;
        font-size: 14px;
        color: #1a1a1a;
        font-family: "Trebuchet MS", "Lucida Sans Unicode", "Lucida Grande", "Lucida Sans", Arial, sans-serif;

        a {
            color: #1a1a1a !important;
        }
    }
}
</style>
