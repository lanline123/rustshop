/**
 * @file 左侧菜单组件
 * @description 后台管理系统的左侧导航菜单
 * @features
 * - 一级菜单+二级子菜单
 * - 响应式折叠
 * - 暗色/亮色主题支持
 * - 移动端抽屉模式
 */
<template>
    <div :class="{ active: menusStore.menuActive, 'shop-admin': adminType === 'shop', dark: themeInfo.navTheme === 'dark', 'locale-en': locale === 'en' }"
        class="body-menu">
        <el-scrollbar v-if="themeInfo.layout == 'default' || screenWidth < 900" class="main-menu">
            <ul v-if="menus" class="main-menu-ul">
                <template v-for="(menu, key) in menus" :key="menu.meta.authoritySn">
                    <li v-if="menu.meta.isShow"
                        :class="'main-menu-item menu_' + menu.meta.authoritySn + ' ' + (menu.meta.current ? 'current' : '')">
                        <a :href="'/' + menu.path" class="menu-tit" @click.prevent="actionPush(menu, true)">
                            <i :class="menu.meta.authorityIco"></i>
                            <span class="nav-tit">{{ $t((menu.meta.authorityName || '').trim()) }}</span>
                        </a>
                    </li>
                </template>
                <li style="height: 80px"></li>
            </ul>
        </el-scrollbar>
        <div :class="'child-menu ' + (menusStore.childMenuShow ? 'child-menu-show' : 'child-menu-hide')">
            <template v-for="(menu, key) in menus" :key="menu.meta.authoritySn">
                <el-scrollbar v-if="menu.meta.current && menu.meta.isShow" :always="true" class="child_warp">
                    <div class="child-tit">{{ $t((menu.meta.authorityName || '').trim()) }}</div>
                    <ul>
                        <template v-for="(child, ckey) in menu.children" :key="child.meta.authoritySn">
                            <template v-if="child.children">
                                <li v-if="child.meta.isShow" :class="'menu-item ' + (child.meta.current ? 'cur' : '')">
                                    <a class="menu-item-a" @click="child.meta.current = !child.meta.current">
                                        <div class="menu-item-title">
                                            <i :class="child.meta.authorityIco" class="tit-ico"></i>
                                            <span class="nav-title">{{ $t((child.meta.authorityName || '').trim()) }}</span>
                                        </div>
                                        <i class="nav-icon iconfont-admin icon-xjt"></i>
                                    </a>
                                    <div v-if="child.meta.current" class="menu-chider">
                                        <ul class="menu-list">
                                            <template v-for="(childer, mCkey) in child.children">
                                                <li v-if="childer.meta.isShow"
                                                    :class="'menu-item menu-chider-item ' + (childer.meta.current ? 'current' : '')">
                                                    <a class="" @click.prevent="actionPush(childer, false, menu)">
                                                        <span class="nav-title">{{ $t((childer.meta.authorityName || '').trim()) }}</span>
                                                    </a>
                                                </li>
                                            </template>
                                        </ul>
                                    </div>
                                </li>
                            </template>
                            <template v-else>
                                <li v-if="child.meta.isShow"
                                    :class="'menu-item ' + (routerMatched[1].name == `${child.meta.authoritySn}` ? 'current' : '')">
                                    <a :target="child.blank ? '_blank' : ''" class="menu-item-a"
                                        @click.prevent="actionPush(child)">
                                        <div class="menu-item-title">
                                            <i :class="child.meta.authorityIco" class="tit-ico"></i>
                                            <span class="nav-title">{{ $t((child.meta.authorityName || '').trim()) }}</span>
                                        </div>
                                    </a>
                                </li>
                            </template>
                        </template>
                        <li style="height: 80px"></li>
                    </ul>
                </el-scrollbar>
            </template>
            <div v-if="false" id="menu-bar-btn" class="menu-bar-btn ng-scope"
                @click="menusStore.childMenuShow = !menusStore.childMenuShow">
                <div class="menu-bar-inner">
                    <div class="menu-bar-bg"></div>
                    <div class="product-navbar-collapse">
                        <span class="icon-collapse-left iconfont">&#xe673;</span>
                        <span class="icon-collapse-right iconfont">&#xe673;</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div :class="{ active: menusStore.menuActive }" class="body-menu-mask"
        @click="menusStore.menuActive = !menusStore.menuActive"></div>
</template>
<script lang="ts" setup>
import { ref, watch, nextTick, computed, onBeforeUnmount, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { useMenusStore } from "@/store/menu";
import { useThemeStore } from "@/store/theme";
import { ensureVisibleCurrentMenu } from "./menuCurrent";
import { message } from "ant-design-vue";

const { themeInfo } = useThemeStore();
const { locale } = useI18n();
/** 管理员类型 */
const adminType = ref(localStorage.getItem("adminType"));

const menusStore = useMenusStore();
const router = useRouter();
const store = useMenusStore();
/** 当前路由匹配数组 */
const routerMatched = ref<any>([]);

routerMatched.value = router.currentRoute.value.matched;

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

/** 计算菜单列表 */
const menus = computed<RouteItem[]>(() => {
    return store.routers ? store.routers : [];
});

/**
 * 更新菜单激活状态
 * @param {any} menuItems - 菜单项数组
 * @param {number} level - 菜单层级
 * @param {boolean} route - 是否根据路由更新
 * @param {boolean} current - 当前状态
 */
const updateCurrentStatus = (menuItems: any, level = 0, route = false, current = true) => {
    menuItems.forEach((menuItem: any) => {
        if (route == true && level == 1) {
            if (routerMatched.value[level] && routerMatched.value[level].name === menuItem.meta.authoritySn) {
                menuItem.meta.current = true;
            }
        } else {
            menuItem.meta.current = routerMatched.value[level] ? routerMatched.value[level].name === menuItem.meta.authoritySn : false;
        }
        if (menuItem.children) {
            updateCurrentStatus(menuItem.children, level + 1, route, current);
        }
    });
};

/**
 * 拼接路径
 * @param {string} baseUrl - 基础路径
 * @param {string} url - 子路径
 */
const resolvePath = (baseUrl: string, url: string) => {
    return baseUrl === "/" ? `${baseUrl}${url}` : `${baseUrl}/${url}`;
};

/**
 * 跳转页面
 * @param {any} menu - 菜单项
 * @param {boolean} isMain - 是否主菜单
 * @param {any} parentMenu - 父菜单
 */
const actionPush = (menu: any, isMain = false, parentMenu?: any) => {
    // 工作台类入口：在新标签打开目标路由
    if (menu.meta?.targetRouteName) {
        const resolved = router.resolve({ name: menu.meta.targetRouteName });
        window.open(resolved.href, "_blank");
        return;
    }
    let path = parentMenu ? resolvePath(parentMenu.path, menu.path) : menu.path;
    if (window.innerWidth <= 756) {
        if (isMain) {
            menus.value.forEach((element: any) => {
                element.meta.current = false;
            });
            menu.meta.current = true;
        } else {
            if (!menu.meta.blank) {
                router.push(path);
            } else {
                window.open(path, "_blank");
            }
            menusStore.menuActive = false;
        }
    } else {
        if (isMain) {
            menus.value.forEach((element: any) => {
                if (element.children && element.children.length > 0) {
                    element.children.forEach((child: any) => {
                        child.meta.current = true;
                    });
                }
            });
        }

        if (!menu.meta.blank) {
            router.push(path);
        } else {
            window.open(path, "_blank");
        }
        nextTick();
        menusStore.childMenuShow = true;
    }
};

/** 监听路由和菜单初始化，更新菜单状态 */
watch([() => router.currentRoute.value.matched, () => menus.value], ([matched]) => {
    routerMatched.value = matched;
    updateCurrentStatus(menus.value, 0, true, false);
    ensureVisibleCurrentMenu(menus.value);
}, { immediate: true });

/** 屏幕宽度 */
const screenWidth = ref(window.innerWidth);

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
<style lang="less" scoped>
.body-menu {
    z-index: 10;
    position: relative;
    height: 100%;
    display: flex;

    .main-menu {
        background: #eef2ff;
        width: 112px;
        height: 100%;
        overflow: hidden;
        z-index: 20;

        .main-menu-ul {
            padding-top: 7px;
        }

        .menu-tit {
            border-radius: 8px;
            height: 38px;
            line-height: 38px;
            margin-bottom: 10px;
        }

        li {
            padding: 0 10px;

            i {
                height: 38px;
                width: 36px;
            }
        }

        li.current .menu-tit {
            height: 38px;
            line-height: 38px;
        }

        li:hover .menu-tit {
            background-color: rgba(255, 255, 255, 0.16);
        }
    }

    .child-menu {
        background: #fff;
        width: 236px;
        height: 100%;
        box-shadow: 0px 0px 16px 0px rgba(0, 0, 0, 0.035);
        padding: 0 10px;

        :deep(.child_warp) {
            .el-scrollbar__bar {
                display: none;
            }
        }

        .child_warp {
            position: relative;
            height: 100%;

            .menu-item {
                margin-top: 8px;
            }
        }
    }
}

.main-menu-logo {
    display: block;
    text-align: center;
    min-height: 85px;
}

.main-menu-logo img {
    width: 80%;
    margin: 20px 0;
}

.main-menu .menu-tit {
    color: #4a5a78;
    cursor: pointer;
    overflow: hidden;
    position: relative;
    font-size: 14px;
    display: flex;
    align-items: center;
    justify-content: flex-start;
}

.main-menu .menu-tit i {
    color: #4a5a78;
    flex-shrink: 0;
    font-size: 18px;
    text-align: center;
    display: block;
}

.main-menu .menu-tit a {
    color: #fff;
}

.main-menu .menu-tit .nav-tit {
    vertical-align: middle;
    display: inline-block;
    text-align: left;
}

.main-menu li.current .menu-tit {
    background: #fff;
    color: var(--tig-primary);
    box-shadow: 0 5px 25px 1px rgba(0, 0, 0, 0.05);
}

.main-menu li.current .menu-tit i {
    color: var(--tig-primary);
}

.child-menu .child-tit {
    height: 50px;
    line-height: 50px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-weight: bold;
    color: #3b3b3b;
    font-size: 18px;
    border-bottom: 1px solid #f2f3f7;
    padding-left: 5px;
}

.child-menu li a.menu-item-a {
    color: #222;
    height: 40px;
    line-height: 40px;
    box-sizing: border-box;
    border-radius: 4px;
    font-size: 14px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    padding-left: 5px;

    .menu-item-title {
        display: flex;

        i.tit-ico {
            width: 20px;
        }
    }
}

.child-menu li a.menu-item-a i.nav-icon {
    font-size: 12px;
    color: #bdbfc7;
    text-indent: 0;
    top: -1px;
    transform: rotate(0deg);
    display: block;
    transition: all 0.2s ease 0s;
}

.child-menu li.cur a.menu-item-a i.nav-icon {
    transform: rotate(180deg);
}

.child-menu li.cat a.menu-item-a {
    height: 32px;
    line-height: 32px;
}

.child-menu li a:hover {
    color: var(--tig-primary);
}

.child-menu li.menu-item.current a {
    font-weight: 600;
    color: var(--tig-primary);
    border-radius: 4px;
}

.menu-list {
    display: flex;
    flex-wrap: wrap;
    gap: 5px 10px;
    margin-left: 15px;
}

.child-menu .menu-chider a {
    color: #8a8c99;
    display: block;
    height: 35px;
    line-height: 35px;
    box-sizing: border-box;
    text-indent: 10px;
    width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.main-menu .menu_panel i.nav-icon::before {
    content: "\e7ca";
    font-size: 16px;
}

.main-menu .menu_product i.nav-icon::before {
    content: "\e74e";
    font-size: 17px;
}

.main-menu .menu_promotion i.nav-icon::before {
    content: "\e645";
}

.main-menu .menu_order i.nav-icon::before {
    content: "\e642";
}

.main-menu .menu_decorate i.nav-icon::before {
    content: "\e648";
    font-size: 20px;
}

.main-menu .menu_finance i.nav-icon::before {
    content: "\e647";
    font-size: 18px;
}

.main-menu .menu_stats i.nav-icon::before {
    content: "\e613";
    font-size: 17px;
}

.main-menu .menu_content i.nav-icon::before {
    content: "\e6f0";
    font-size: 17px;
}

.main-menu .menu_user i.nav-icon::before {
    content: "\e8b1";
}

.main-menu .menu_authority i.nav-icon::before {
    content: "\e641";
    font-size: 16px;
}

.main-menu .menu_setting i.nav-icon::before {
    content: "\e646";
}

.main-menu .menu_template i.nav-icon::before {
    content: "\e63a";
}

.main-menu .menu_backup i.nav-icon::before {
    content: "\e62a";
}

.main-menu .menu_distribution i.nav-icon::before {
    content: "\e640";
    font-size: 16px;
}

.main-menu .menu_email i.nav-icon::before {
    content: "\e744";
    font-size: 16px;
}

.main-menu .menu_store i.nav-icon::before {
    content: "\e643";
    font-size: 18px;
}

.main-menu .menu_team i.nav-icon::before {
    content: "\e649";
    font-size: 19px;
}

.menu-bar-btn *,
.menu-bar-btn *::before,
.menu-bar-btn *::after {
    box-sizing: border-box;
}

.menu-bar-btn {
    height: 50px;
    left: -20px;
    position: absolute;
    top: 50%;
    transition: all 0.2s ease 0s;
    width: 20px;
    z-index: 10;
    color: #f7f7f7;
}

.menu-bar-btn:hover .product-navbar-collapse {
    left: 0;
    left: auto;
    right: 0;
}

.menu-bar-btn:hover .menu-bar-bg {
    border-bottom: 8px solid transparent;
    border-left: 20px solid #d9dee4;
    border-top: 8px solid transparent;
    border-color: transparent #f7f7f7 transparent -moz-use-text-color;
    border-style: solid solid solid none;
    border-width: 8px 20px 8px medium;
}

.menu-bar-btn .menu-bar-inner {
    overflow: hidden;
    position: relative;
    top: -50%;
}

.menu-bar-btn .product-navbar-collapse {
    cursor: pointer;
    height: 50px;
    position: relative;
    text-align: center;
    transition:
        all 0.1s ease 0s,
        all 0.1s ease 0s;
    left: auto;
    right: -4px;
}

.menu-bar-btn .menu-bar-bg {
    border-bottom: 9px solid transparent;
    border-left: 13px solid #d9dee4;
    border-top: 9px solid transparent;
    height: 50px;
    left: 0;
    position: absolute;
    top: 0;
    transition:
        all 0.1s ease 0s,
        all 0.1s ease 0s;
    width: 0;
    border-color: transparent #f7f7f7 transparent -moz-use-text-color;
    border-style: solid solid solid none;
    border-width: 9px 13px 9px medium;
    left: auto;
    right: 0;
}

.menu-bar-btn .icon-collapse-left {
    display: none;
    font-size: 18px;
    line-height: 50px;
    vertical-align: text-top;
    vertical-align: text-top;
}

.menu-bar-btn .icon-collapse-right {
    display: inline-block;
    font-size: 18px;
    line-height: 50px;
    vertical-align: text-top;
    vertical-align: text-top;
    transform: rotate(180deg);
    color: #7a8599;
}

.child-menu-show {
    z-index: 1;
}

.child-menu-show .viewFramework-product-navbar-bg,
.child-menu-show .viewFramework-product-navbar {
    width: 160px;
}

.child-menu-show .viewFramework-product-body {
    left: 160px;
}

.child-menu-show .menu-bar-btn {
    left: 140px;
}

.child-menu-hide .menu-bar-btn {
    left: -20px;
}

.child-menu-show .menu-bar-btn .product-navbar-collapse {
    left: auto;
    right: -7px;
}

.child-menu-show .menu-bar-btn .product-navbar-collapse>span {
    color: #7a8599;
}

.child-menu-show .menu-bar-btn .menu-bar-bg {
    border-color: transparent #f7f7f7 transparent -moz-use-text-color;
    border-style: solid solid solid none;
    border-width: 9px 13px 9px medium;
    left: auto;
    right: 0;
}

.child-menu-show .menu-bar-btn .icon-collapse-left {
    display: inline;
}

.child-menu-show .menu-bar-btn .icon-collapse-right {
    display: none;
}

.child-menu-show .menu-bar-btn:hover .product-navbar-collapse {
    left: auto;
    right: 0;
}

.child-menu-show .menu-bar-btn:hover .menu-bar-bg {
    border-color: transparent #f7f7f7 transparent -moz-use-text-color;
    border-style: solid solid solid none;
    border-width: 8px 20px 8px medium;
}

.child-menu-hide {
    .child_warp {
        overflow: hidden;
        width: 0;
    }
}

.body-menu-mask {
    display: none;
}

@media only screen and (max-width: 767px) {
    .body-menu {
        transform: translateX(-100%);
        position: fixed;
        left: 0;
        top: 58px;
        bottom: 0;
        z-index: 1010;
        transition:
            transform 0.3s cubic-bezier(0.7, 0.3, 0.1, 1),
            box-shadow 0.3s cubic-bezier(0.7, 0.3, 0.1, 1),
            -webkit-transform 0.3s cubic-bezier(0.7, 0.3, 0.1, 1),
            -webkit-box-shadow 0.3s cubic-bezier(0.7, 0.3, 0.1, 1);
        width: 267px;

        &.active {
            visibility: visible;
            transform: translateX(0px);
        }
    }

    .main-menu {
        position: absolute;
        z-index: 9;
    }

    .child-menu {
        position: absolute;
        border-right: 0;
        box-shadow: none;
    }

    .active .child-menu {
        box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
        left: 112px;
    }

    .body-menu-mask {
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgb(0, 0, 0);
        opacity: 0;
        z-index: 1000;
        position: fixed;
        transition: all 0.5s;
        visibility: hidden;
        display: block;

        &.active {
            visibility: inherit;
            display: block;
            opacity: 0.5;
        }
    }

    .menu-bar-btn {
        display: none;
    }
}

.shop-admin,
.dark {
    .main-menu {
        background-color: #030021;
        color: #fff;

        .menu-tit {
            border-radius: 8px;
            height: 38px;
            line-height: 38px;
            color: #fff;
            margin-bottom: 10px;

            i {
                color: #fff;
            }
        }

        li {
            padding: 0 10px;

            i {
                color: #fff;
                height: 38px;
                width: 38px;
            }
        }

        li.current .menu-tit {
            background-color: rgba(255, 255, 255, 0.16);
            height: 38px;
            line-height: 38px;
            color: #fff;

            i {
                color: #fff;
            }
        }

        li:hover .menu-tit {
            background-color: rgba(255, 255, 255, 0.16);
        }
    }
}

/* 英文菜单：单行展示、左对齐，加宽侧栏避免裁切；文案单词首字母大写 */
.body-menu.locale-en {
    .nav-tit,
    .nav-title,
    .child-tit {
        text-transform: capitalize;
    }

    .main-menu {
        width: 160px;

        .menu-tit {
            height: 38px;
            line-height: 38px;
            overflow: hidden;
            padding: 0 6px;
            align-items: center;
            justify-content: flex-start;
        }

        li {
            i {
                height: 38px;
                flex-shrink: 0;
            }
        }

        li.current .menu-tit,
        li:hover .menu-tit {
            height: 38px;
            line-height: 38px;
        }

        .menu-tit .nav-tit {
            flex: 1;
            min-width: 0;
            text-align: left;
            line-height: 38px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
    }

    /* 二级子菜单：一行只显示一个 */
    .child-menu {
        .menu-list {
            flex-direction: column;
            flex-wrap: nowrap;
            gap: 0;
        }

        .menu-chider a {
            width: 100%;
        }
    }
}

.shop-admin.locale-en,
.dark.locale-en {
    .main-menu {
        .menu-tit,
        li.current .menu-tit {
            height: 38px;
            line-height: 38px;

            i {
                height: 38px;
            }
        }
    }
}

@media only screen and (max-width: 767px) {
    .body-menu.locale-en.active .child-menu {
        left: 160px;
    }
}
</style>
