<template>
    <div
        class="theme-toolbar"
        :class="{
            'pc-home-toolbar': store.decorateType === 2 && !isPcSearchPage && !isPcLoginPage,
            'pc-custom-toolbar': store.decorateType === 3,
            'pc-search-toolbar': isPcSearchPage,
            'pc-login-toolbar': isPcLoginPage
        }"
    >
        <div class="theme-menu-con show">
            <div class="theme-con-box">
                <!-- PC 首页 / PC 搜索页无页签，沿用旧 PcToolBar / PcSearchToolBar -->
                <el-tabs v-if="store.decorateType !== 2" v-model="activeTab" class="demo-tabs">
                    <el-tab-pane v-for="tab in visibleTabs" :key="tab.name" :label="$t(tab.label)" :name="tab.name"></el-tab-pane>
                </el-tabs>
                <div class="inside">
                    <el-collapse v-model="activeNames">
                        <el-collapse-item v-for="section in activeSections" :key="section.group" :title="section.label" :name="section.group">
                            <template #title>
                                <div class="modules-head J_ModuleListHead">
                                    <span>{{ $t(section.label) }}</span>
                                </div>
                            </template>
                            <draggable
                                class="module-list"
                                item-key="type"
                                :list="section.items"
                                ghost-class="sortable-ghost"
                                chosen-class="toolChosenClass"
                                animation="300"
                                :sort="false"
                                :force-fallback="true"
                                :fallback-on-body="true"
                                :fallback-tolerance="3"
                                fallback-class="module-toolbar-drag-fallback"
                                :group="{ name: 'advanced', pull: 'clone', put: false }"
                                :clone="cloneModule"
                            >
                                <template #item="{ element }">
                                    <div class="list-item tool-list-item" @click="onClickAdd(element)">
                                        <span v-if="element.icon" class="pic" v-html="element.icon"></span>
                                        <span v-if="element.label" class="content" :title="$t(element.label)">{{ $t(element.label) }}</span>
                                    </div>
                                </template>
                            </draggable>
                        </el-collapse-item>
                    </el-collapse>
                    <div style="height: 50px"></div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, computed } from "vue";
import draggable from "vuedraggable";
import { message } from "ant-design-vue";
import {
    getModulesByContext,
    getModuleToolbarGroup,
    createModuleInstance,
    type ModuleDefinition,
    type ToolbarGroup
} from "../config/module-registry";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import { PageType } from "../config/constants";

const store = useDecorateEditorStore();
const isPcSearchPage = computed(() => store.decorateType === 2 && store.pageType === PageType.SEARCH);
const isPcLoginPage = computed(() => store.decorateType === 2 && store.pageType === PageType.LOGIN);

// 店铺端判断与旧 ToolBar 一致（shopId 非 0），不进 store
const shopId = localStorage.getItem("shopId");
const isShop = !!shopId && shopId !== "0";

const GROUP_LABELS: Record<ToolbarGroup, string> = {
    basic: "基础模块",
    other: "其它模块",
    live: "直播模块",
    advanced: "高级模块",
    member: "会员模块",
    cateCenter: "分类中心模块",
    productDetail: "商品详情模块",
    pc: "PC模块",
    pcNew: "PC首页模块",
    searchResult: "搜索结果模块",
    loginConfig: "登录页模块"
};

// 页签结构对齐旧 ToolBar：basic/other/live 同属"基础模块"页签，advanced/member/cateCenter 各自独立页签
const TAB_OF_GROUP: Record<ToolbarGroup, string> = {
    basic: "toolList",
    other: "toolList",
    live: "toolList",
    advanced: "advancedList",
    member: "memberList",
    cateCenter: "cateCenterList",
    productDetail: "productDetailList",
    pc: "toolList",
    pcNew: "toolList",
    searchResult: "toolList",
    loginConfig: "loginList"
};

const TAB_LABELS: Record<string, string> = {
    toolList: "基础模块",
    advancedList: "高级模块",
    memberList: "会员模块",
    cateCenterList: "分类中心模块",
    productDetailList: "商品详情",
    loginList: "登录页模块"
};

const availableModules = computed(() =>
    getModulesByContext(store.decorateType, store.pageType, { isShop, homeVersion: store.homeStyleVersion })
);

interface Section {
    group: ToolbarGroup;
    label: string;
    items: ModuleDefinition[];
}

const sections = computed<Section[]>(() => {
    const map = new Map<ToolbarGroup, ModuleDefinition[]>();
    availableModules.value.forEach((m) => {
        const group = getModuleToolbarGroup(m);
        if (!map.has(group)) map.set(group, []);
        map.get(group)!.push(m);
    });
    const order: ToolbarGroup[] = ["pcNew", "basic", "other", "live", "advanced", "member", "cateCenter", "productDetail", "searchResult", "loginConfig", "pc"];
    return order
        .filter((g) => map.has(g))
        .map((g) => ({
            group: g,
            // PC 首页：新旧两套均称"PC首页模块"（版本由顶部栏切换标识，分组名保持一致）；
            // PC 自定义页(decorateType=3)的 pc 组仍称"基础模块"
            label: g === "pc" ? (store.decorateType === 2 ? "PC首页模块" : "基础模块") : GROUP_LABELS[g],
            items: map.get(g)!
        }));
});

const visibleTabs = computed(() => {
    const tabNames = new Set(sections.value.map((s) => TAB_OF_GROUP[s.group]));
    return ["toolList", "advancedList", "memberList", "cateCenterList", "productDetailList", "loginList"].filter((t) => tabNames.has(t)).map((t) => ({ name: t, label: TAB_LABELS[t] }));
});

const activeTab = ref("");
const defaultTab = computed(() => visibleTabs.value[0]?.name ?? "toolList");
const currentTab = computed(() => (visibleTabs.value.some((t) => t.name === activeTab.value) ? activeTab.value : defaultTab.value));

const activeSections = computed(() => sections.value.filter((s) => TAB_OF_GROUP[s.group] === currentTab.value));

const activeNames = ref<string[]>(["pcNew", "basic", "other", "live", "advanced", "member", "cateCenter", "productDetail", "searchResult", "loginConfig", "pc"]);

function buildInstance(def: ModuleDefinition) {
    const instance = createModuleInstance(def.type, def.url);
    if (!instance) return null;
    return { ...instance, moduleIndex: Date.now() };
}

/** 拖拽 clone：校验失败返回 {}（无 moduleIndex），由预览区 @add 识别并移除，与旧 ToolBar 机制一致 */
function cloneModule(def: ModuleDefinition) {
    const validation = store.canAddModule(def.type);
    if (!validation.ok) {
        message.warning(validation.reason);
        return {};
    }
    return buildInstance(def) ?? {};
}

function onClickAdd(def: ModuleDefinition) {
    const instance = buildInstance(def);
    if (!instance) return;
    const { moduleIndex, ...rest } = instance;
    void moduleIndex;
    const result = store.addModule(rest);
    if (typeof result !== "number") {
        message.warning(result.reason);
    }
}
</script>
<style lang="scss" scoped>
.theme-con-box {
    .modules-head {
        width: auto !important;
    }
    .inside {
        padding-right: 8px;
    }
    :deep(.el-tabs__item) {
        padding: 0;
        margin: 5px 20px;
    }
    :deep(.el-collapse) {
        border: none !important;
    }
    :deep(.el-collapse-item__header) {
        border: none !important;
    }
    :deep(.el-collapse-item__wrap) {
        border: none !important;
    }
    :deep(.el-collapse-item__content) {
        padding-bottom: 10px;
        line-height: 0.8;
    }
    :deep(.el-collapse-icon-position-right .el-collapse-item__header) {
        padding-right: 0 !important;
    }
}

/* fallback 拖拽使用鼠标事件模拟，禁止从工具栏拖动时触发浏览器文本选择 */
.module-list,
.module-list * {
    -webkit-user-select: none;
    user-select: none;
}

/* PC 自定义页：面板宽度对齐旧 PcCustomPageToolBar 的 295px 覆盖
   （decorate-page-pc-custom 的 decorate-page-wrap 从 295px 起，全局 280px 会在边框处留白条） */
.pc-custom-toolbar .theme-menu-con,
.pc-search-toolbar .theme-menu-con,
.pc-login-toolbar .theme-menu-con {
    width: 295px !important;
}

/* PC 首页·旧版：整行文字项对齐旧 PcToolBar 的 scoped 覆盖
   （旧版面板仅 150px 宽，通用 33.333% 网格会把纯文字 label 挤成竖排）。
   新版面板 295px，沿用全局卡片网格（图标+文字），与 PC 自定义页一致，故此覆盖仅限旧版 */
.decorate-page-pc-old .pc-home-toolbar .theme-con-box .module-list .list-item {
    border: 1px dashed #ebedf0;
    color: #646566;
    background: #f7f8fa;
    box-sizing: border-box;
    font-size: 12px;
    border-radius: 0;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    margin-bottom: 10px;
}
/* 内联 SVG 模块（搜索/登录等）：走 currentColor，默认 #7d7e80，悬浮变白 */
.theme-con-box .module-list .list-item.tool-list-item {
    user-select: none;

    .pic {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        color: #7d7e80;
    }
    &:hover .pic {
        color: #fff;
    }
}
.decorate-page-pc-old .pc-home-toolbar .theme-con-box .module-list .list-item:hover {
    background: #edf4ff;
    border-color: #3773da;
    color: #155bd4;
}

/* fallback 拖拽浮层会挂到 body，已脱离 .theme-con-box，需自带完整卡片样式。 */
:global(.module-toolbar-drag-fallback) {
    box-sizing: border-box !important;
    display: flex !important;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 120px !important;
    min-width: 120px !important;
    padding: 8px 10px !important;
    border: 1px solid var(--tig-primary);
    border-radius: 4px;
    background: #ffffff;
    color: #333333;
    text-align: center;
    cursor: grabbing;
    user-select: none;
}

:global(.module-toolbar-drag-fallback .pic) {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 32px;
    color: #7d7e80;
}

:global(.module-toolbar-drag-fallback .pic i) {
    font-size: 28px;
    color: #7d7e80;
}

:global(.module-toolbar-drag-fallback .content) {
    display: block;
    width: 100%;
    height: 20px;
    line-height: 20px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
}
</style>
