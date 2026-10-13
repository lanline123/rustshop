/**
 * @file 响应式顶部菜单组件
 * @description 水平一级菜单，根据屏幕宽度自动折叠到"更多"下拉菜单
 * @features
 * - 响应式菜单项显示
 * - 超出部分折叠到下拉菜单
 * - 当前激活菜单高亮
 */
<template>
    <div class="responsive-top-menu">
        <div
            v-for="(menu, index) in visibleMenus"
            :key="`menu-${menu.meta.authoritySn}`"
            class="menu-item"
            :class="[themeInfo.navTheme, { current: isActive(menu) }]"
            @click="actionPush(menu)"
        >
            {{ $t((menu.meta.authorityName || '').trim()) }}
        </div>

        <div v-if="hiddenMenus.length > 0" class="more-container" @mouseenter="showDropdown = true" @mouseleave="showDropdown = false">
            <div class="menu-item" :class="themeInfo.navTheme"> {{ $t('更多') }} <i class="iconfont-admin icon-sanjiaoxing"></i>
            </div>
            <transition name="fade">
                <div v-show="showDropdown" class="dropdown-menu">
                    <div class="dropdown-box">
                        <div
                            v-for="menu in hiddenMenus"
                            :key="`hidden-${menu.meta.authoritySn}`"
                            class="dropdown-item"
                            :class="{ current: isActive(menu) }"
                            @click="handleDropdownClick(menu)"
                        >
                            {{ $t((menu.meta.authorityName || '').trim()) }}
                        </div>
                    </div>
                </div>
            </transition>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
import { useMenusStore } from "@/store/menu";
import { useThemeStore } from "@/store/theme";

const { themeInfo } = useThemeStore();
const router = useRouter();
const store = useMenusStore();
/** 是否显示下拉菜单 */
const showDropdown = ref(false);
/** 屏幕宽度 */
const screenWidth = ref(window.innerWidth);

/** 路由项类型定义 */
interface RouteItem {
    path: string;
    name: string;
    component?: any;
    redirect?: string;
    meta: {
        title?: string;
        is404?: boolean;
        isShowInHeader?: boolean;
        queryParam?: string;
        [key: string]: any;
    };
    children?: RouteItem[];
    [key: string]: any;
}

/** 响应式断点 */
const BREAKPOINTS = {
    SM: 640,
    MD: 768,
    LG: 1024,
    XL: 1280
};

/** 计算最大可见菜单项数量 */
const maxVisibleItems = computed(() => {
    if (screenWidth.value >= BREAKPOINTS.XL) return 12;
    if (screenWidth.value >= BREAKPOINTS.LG) return 8;
    if (screenWidth.value >= BREAKPOINTS.MD) return 5;
    if (screenWidth.value >= BREAKPOINTS.MD) return 3;
    return 1;
});

/** 菜单列表 */
const menus = computed(() => store.routers || []);

/** 可见的菜单项 */
const visibleMenus = computed(() => {
    const filtered = menus.value.filter((menu) => menu.meta.isShow);
    return filtered.slice(0, maxVisibleItems.value);
});

/** 折叠到"更多"的菜单项 */
const hiddenMenus = computed(() => {
    const filtered = menus.value.filter((menu) => menu.meta.isShow);
    return filtered.slice(maxVisibleItems.value);
});

/**
 * 判断菜单是否激活
 * @param {RouteItem} menu - 菜单项
 */
const isActive = (menu: RouteItem) => {
    return router.currentRoute.value.matched[0].name === menu.name;
};

/**
 * 点击菜单跳转
 * @param {RouteItem} menu - 菜单项
 */
const actionPush = (menu: RouteItem) => {
    router.push(menu.path);
};

/**
 * 点击下拉菜单项
 * @param {RouteItem} menu - 菜单项
 */
const handleDropdownClick = (menu: RouteItem) => {
    actionPush(menu);
    showDropdown.value = false;
};

/** 处理窗口大小变化 */
const handleResize = () => {
    screenWidth.value = window.innerWidth;
};

onMounted(() => {
    window.addEventListener("resize", handleResize);
});

onBeforeUnmount(() => {
    window.removeEventListener("resize", handleResize);
});
</script>

<style scoped lang="less">
.responsive-top-menu {
    display: flex;
    align-items: center;
    gap: 8px;
    position: relative;

    .menu-item {
        padding: 10px 16px;
        cursor: pointer;
        white-space: nowrap;
        border-radius: 4px;
        transition: all 0.2s ease;
        color: #b6babf;
        &:hover {
            background: #9c9c9c2e;
        }
        &.current {
            color: #fff;
            font-weight: bold;
        }

        &.light {
            color: #788d9b;
            &:hover {
                background: #f7f7f7;
            }
        }
    }

    .more-container {
        position: relative;
        .dropdown-menu {
            position: absolute;
            left: 0;
            top: 45px;
            background: white;
            min-width: 120px;
            border-radius: 4px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
            z-index: 1000;
            .dropdown-box {
                padding: 10px;
                .dropdown-item {
                    cursor: pointer;
                    transition: all 0.2s ease;
                    padding: 10px;
                    gap: 10px;

                    &:hover {
                        background: var(--tig-item-active-bg);
                        color: var(--tig-text-hover);
                    }
                }
            }
        }
    }

    .fade-enter-active,
    .fade-leave-active {
        transition: opacity 0.2s ease;
    }

    .fade-enter-from,
    .fade-leave-to {
        opacity: 0;
    }
}
</style>
