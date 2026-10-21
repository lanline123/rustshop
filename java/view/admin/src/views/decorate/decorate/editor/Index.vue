<template>
    <div
        class="decorate-page"
        :class="{
            'decorate-page-pc': props.decorateType === 2 && store.pageType !== 6,
            'decorate-page-pc-custom': props.decorateType === 3 || (props.decorateType === 2 && store.pageType === 6),
            'decorate-page-search': props.decorateType === 2 && store.pageType === 6,
            'decorate-page-pc-new': isPcNewCanvas,
            'decorate-page-pc-old': props.decorateType === 2 && store.pageType !== 6 && store.homeStyleVersion === 'old'
        }"
    >
        <div class="decorate-topbar-warp">
            <div class="decorate-topbar-con">
                <div class="topbar-left">
                    <div class="topbar-logo"></div>
                    <div class="topbar-now-txt cursor-pointer" @click="onLogout()">
                        <i class="iconfont-admin icon-tuichu" style="margin-right: 8px"></i>退出编辑
                    </div>
                    <div class="topbar-now-txt topbar-now-txt-edit">
                        <span class="tit">正在装修：</span>
                        <el-input
                            v-if="isEditingDecorateTitle"
                            ref="decorateTitleInputRef"
                            v-model="decorateTitleDraft"
                            class="topbar-now-txt-input"
                            size="small"
                            :maxlength="50"
                            placeholder="请输入页面名称"
                            @blur="commitDecorateTitle"
                            @keyup.enter="commitDecorateTitle"
                        />
                        <span v-else class="topbar-now-txt-title" @click="startEditDecorateTitle">
                            {{ store.decorateTitle || "未命名页面" }}
                        </span>
                    </div>

                </div>
                <el-space class="topbar-right">
                    <el-button class="btn" text @click="onPreview()" :disabled="decorateDisabled">预览</el-button>
                    <el-button class="btn" text @click="onSavetoDraft()" :disabled="decorateDisabled">存至草稿</el-button>
                    <el-button @click="onPublish()" type="primary" :disabled="decorateDisabled">保存并发布</el-button>
                </el-space>
            </div>
        </div>
        <main class="pcMainContent">
            <ModuleToolbar></ModuleToolbar>
            <perfect-scrollbar class="decorate-page-wrap">
                <Alert
                    v-if="store.hasDraft"
                    class="decorate-page-alert"
                    message="该页面有一条草稿记录，您可以还原草稿继续编辑。"
                    type="warning"
                    show-icon
                    closable
                >
                    <template #action>
                        <el-button @click="continueEditDraft">继续编辑</el-button>
                    </template>
                </Alert>
                <!-- 画布恒定 1600 复刻前台满宽观感；屏幕不够宽时由横向视口平移查看（吸底拖拽条 / 触摸板横滑），或用缩放控件整体缩放 -->
                <CanvasViewport :enabled="isPcNewCanvas" :base-width="1600" v-model:zoom="canvasZoom">
                    <ResponsiveBox :baseWidth="1600">
                        <template #default="{ scale }">
                            <div
                                class="decorate-page-window"
                                :style="[
                                    pageWindowGradientStyle ||
                                        'background-position: top center;' +
                                            format.backgroundColor +
                                            format.backgroundImage +
                                            format.backgroundRepeat +
                                            format.backgroundSize,
                                    canvasZoomStyle
                                ]"
                            >
                                <div ref="modulesWarpRef" class="theme-modules-warp">
                                    <div
                                        v-if="![2, 3].includes(decorateType) && ![6, 8].includes(store.pageType)"
                                        @click="store.selectPage()"
                                        :class="
                                            'list-item modules-item modules-item-topbar' +
                                            (store.editMode === 'page' ? ' module-item-active' : '') +
                                            (topbarImmersive ? ' modules-item-topbar--immersive' : '')
                                        "
                                        draggable="false"
                                    >
                                        <Page v-model:module="store.pageModule"></Page>
                                    </div>
                                    <!-- PC 首页·新版：画布顶部 PC 头部预览；搜索页仅展示不可进入头部设置；
                                     登录页(pageType=5)不用公共头部——前台登录页是专属 LoginHeader，画在 pcLogin 模块内 -->
                                    <div
                                        v-if="decorateType === 2 && store.homeStyleVersion === 'new' && store.pageType !== 5"
                                        :class="[
                                            'pc-header-preview-warp',
                                            'modules-item',
                                            {
                                                'module-item-active': store.editMode === 'pcHeader' && store.pageType !== 6,
                                                'pc-header-preview-warp--readonly': store.pageType === 6
                                            }
                                        ]"
                                        draggable="false"
                                        @click="store.pageType !== 6 && store.selectPcHeader()"
                                    >
                                        <PcHeaderPreview :config="store.pageModule.pcHeader" :page-type="store.pageType" />
                                        <div v-if="isPcNewCanvas" class="module-sticky-frame">
                                            <span class="module-sticky-frame__edge"></span>
                                            <span class="module-sticky-frame__edge module-sticky-frame__edge--right"></span>
                                        </div>
                                    </div>
                                    <draggable
                                        class="gallery-list-ul"
                                        item-key="moduleIndex"
                                        :list="store.moduleList"
                                        ghost-class="sortable-ghost"
                                        chosen-class="chosenClass"
                                        animation="300"
                                        :empty-insert-threshold="80"
                                        :force-fallback="true"
                                        :fallback-on-body="true"
                                        :fallback-tolerance="3"
                                        @end="onDragEnd"
                                        @add="onDragAdd"
                                        :group="{ name: 'advanced', pull: false, put: true }"
                                        :style="{ minHeight: store.pageType === 6 && store.moduleList.length > 0 ? 'auto' : '700px' }"
                                    >
                                        <template #item="{ element, index }">
                                            <div
                                                :class="['list-item modules-item', { 'module-item-active': store.activeModuleIndex === index }]"
                                                draggable="false"
                                                @click="store.selectModule(index)"
                                            >
                                                <div class="module-mask" v-if="!element.isShow">
                                                    <i class="ico-decorate icon-dec-mimayincang" style="font-size: 25px"></i>
                                                </div>
                                                <ModuleRenderer v-if="element.type" :module="element" :decorate-id="store.decorateId"></ModuleRenderer>
                                                <!-- pc-new 画布横滚时模块左右缘可能滚出可视区：选中态左右边线用 sticky 吸附在可视边缘，避免选中框被裁 -->
                                                <div v-if="isPcNewCanvas" class="module-sticky-frame">
                                                    <span class="module-sticky-frame__edge"></span>
                                                    <span class="module-sticky-frame__edge module-sticky-frame__edge--right"></span>
                                                </div>
                                                <!-- pc-new 画布：标签/操作条移到滚动容器外的槽位轨道（见 CanvasViewport 的 rails 插槽） -->
                                                <div class="module-label" v-if="!isPcNewCanvas">
                                                    <div class="label-name">{{ element.label }}</div>
                                                </div>
                                                <div
                                                    class="module-operate"
                                                    v-if="!isPcNewCanvas && index === store.activeModuleIndex && store.editMode === 'module'"
                                                >
                                                    <div class="module-operate-item" @click.stop="onMoveUp(index)">
                                                        <i class="ico-decorate icon-dec-shangyi"></i>
                                                        <div class="opt-tip">上移</div>
                                                    </div>
                                                    <div class="module-operate-item" @click.stop="onMoveDown(index)">
                                                        <i class="ico-decorate icon-dec-xiayi"></i>
                                                        <div class="opt-tip">下移</div>
                                                    </div>
                                                    <div class="module-operate-item" v-if="!element.fixed" @click.stop="store.toggleModuleVisibility(index)">
                                                        <i :class="'ico-decorate ' + (element.isShow ? 'icon-dec-mimaxianshi' : 'icon-dec-mimayincang')"></i>
                                                        <div class="opt-tip">显示</div>
                                                    </div>
                                                    <div class="module-operate-item" v-if="!element.fixed" @click.stop="onCopyModule(index)">
                                                        <i class="ico-decorate icon-dec-fuzhi1"></i>
                                                        <div class="opt-tip">复制</div>
                                                    </div>
                                                    <div class="module-operate-item" v-if="!element.fixed" @click.stop="store.removeModule(index)">
                                                        <i class="ico-decorate icon-dec-shanchu"></i>
                                                        <div class="opt-tip">删除</div>
                                                    </div>
                                                </div>
                                            </div>
                                        </template>
                                    </draggable>
                                </div>
                            </div>
                            <!-- 搜索页/PC 登录页/商品详情页装修不展示页面设置（页面配置全部收敛在各自模块内） -->
                            <div
                                class="decorate-page-config-warp"
                                v-if="!((props.decorateType === 2 && store.pageType === 5) || [6, 8].includes(store.pageType))"
                            >
                                <div class="decorate-page-config-con">
                                    <div class="page-config-item" @click="store.selectPage()">
                                        <i class="ico-decorate icon-dec-yemianshezhi"></i><span>页面设置</span>
                                    </div>
                                </div>
                            </div>
                        </template>
                    </ResponsiveBox>
                    <!-- 槽位轨道：标签/操作条脱离横向滚动容器，始终可见；top 按画布内模块实测位置对齐 -->
                    <template #rails>
                        <div v-if="isPcNewCanvas" ref="railLeftRef" class="canvas-viewport__rail canvas-viewport__rail--left" :style="{ left: railGap + 'px' }">
                            <div
                                v-for="(element, index) in store.moduleList"
                                v-show="railReady && railTops[index] !== undefined"
                                :key="index"
                                class="module-label"
                                :style="{ top: (railTops[index] || 0) + 'px' }"
                                @click="store.selectModule(index)"
                            >
                                <div class="label-name">{{ element.label }}</div>
                            </div>
                        </div>
                        <div
                            v-if="isPcNewCanvas && railReady && railActiveModule && store.editMode === 'module' && railTops[railActiveIndex] !== undefined"
                            class="canvas-viewport__rail canvas-viewport__rail--right"
                            :style="{ right: railGap + 'px' }"
                        >
                            <div class="module-operate" :style="{ top: railTops[railActiveIndex] + 'px' }">
                                <div class="module-operate-item" @click.stop="onMoveUp(railActiveIndex)">
                                    <i class="ico-decorate icon-dec-shangyi"></i>
                                    <div class="opt-tip">上移</div>
                                </div>
                                <div class="module-operate-item" @click.stop="onMoveDown(railActiveIndex)">
                                    <i class="ico-decorate icon-dec-xiayi"></i>
                                    <div class="opt-tip">下移</div>
                                </div>
                                <div class="module-operate-item" v-if="!railActiveModule.fixed" @click.stop="store.toggleModuleVisibility(railActiveIndex)">
                                    <i :class="'ico-decorate ' + (railActiveModule.isShow ? 'icon-dec-mimaxianshi' : 'icon-dec-mimayincang')"></i>
                                    <div class="opt-tip">显示</div>
                                </div>
                                <div class="module-operate-item" v-if="!railActiveModule.fixed" @click.stop="onCopyModule(railActiveIndex)">
                                    <i class="ico-decorate icon-dec-fuzhi1"></i>
                                    <div class="opt-tip">复制</div>
                                </div>
                                <div class="module-operate-item" v-if="!railActiveModule.fixed" @click.stop="store.removeModule(railActiveIndex)">
                                    <i class="ico-decorate icon-dec-shanchu"></i>
                                    <div class="opt-tip">删除</div>
                                </div>
                            </div>
                        </div>
                    </template>
                </CanvasViewport>
            </perfect-scrollbar>
            <SettingPanel></SettingPanel>
        </main>
        <!-- 新建模式（仅移动端）：自动弹出对应页面类型的模板库，选择空白或模板作为装修起点 -->
        <DialogForm
            v-if="isAddMode"
            ref="tplDialogRef"
            :showHeader="false"
            :showFooter="false"
            width="1192px"
            :bodyStyle="{ padding: 0 }"
            dialogClass="template-library-dialog"
            path="decorate/mobileDecorate/TemplateLibrary"
            :params="{ decorateType: props.decorateType, pageType: store.pageType, scene: 'editor' }"
            @okCallback="onTemplatePicked"
        >
            <span style="display: none"></span>
        </DialogForm>
    </div>
</template>
<script lang="ts" setup>
import { DialogForm } from "@/components/dialog";
import { ArrowDown } from "@element-plus/icons-vue";
import { ref, onMounted, onUnmounted, computed, nextTick, provide, watch } from "vue";
import { useRouter } from "vue-router";
import request from "@/utils/request";
import { loadDraftData, saveDraft, decorateDetail } from "@/api/decorate/decorate";
import draggable from "vuedraggable";
import { message, Alert, Modal } from "ant-design-vue";
import { urlWapFormat, baseDirFormat } from "@/utils/format";
import { getLocalesList } from "@/api/multilingual/languagesList";
import { delDecorate, updateDecorateFiled, updateDecorate } from "@/api/decorate/mobileDecorate";
import { saveMemberCenterDecorate, getMemberCenterDecorateSetting } from "@/api/setting/config";
import { isOverseas } from "@/utils/version";
import "@/views/decorate/decorate/src/css/decorate.less";
import "@/views/decorate/decorate/src/css/module.less";
import { useDecorateStore } from "@/store/decorate";
import { useDecorateEditorStore } from "@/store/decorateEditor";
import { useThemeStore } from "@/store/theme";
import { getAdminType } from "@/utils/storage";
import { ResponsiveBox } from "@/components/responsive-box";
import { useConfigStore } from "@/store/config";
import Page from "@/views/decorate/decorate/src/modules/src/commonPage/View.vue";
import ModuleToolbar from "./components/ModuleToolbar.vue";
import ModuleRenderer from "./components/ModuleRenderer.vue";
import SettingPanel from "./components/SettingPanel.vue";
import CanvasViewport from "./components/CanvasViewport.vue";
import PcHeaderPreview from "./modules/pcHome/_shared/pc-header/PcHeaderPreview.vue";
// 触发全部模块自注册（legacy + 已迁移四件套）
import "./modules/index";

const { decorateInfo, setDecorateId, setLocaleId, setIsDefault } = useDecorateStore();
const config = useConfigStore();

message.config({ top: "64px" });
const store = useDecorateEditorStore();

const props = defineProps({
    decorateType: {
        type: Number,
        default: 1
    }
});

// PC 首页·新版画布（与根节点 decorate-page-pc-new class 同条件）
const isPcNewCanvas = computed(() => props.decorateType === 2 && store.pageType !== 6 && store.homeStyleVersion === "new");

// 画布缩放（仅 pc-new）：用 CSS zoom 而非 transform scale —— zoom 参与布局，
// 横滚宽度、拖拽排序命中、rails 的 rect 实测都天然按视觉尺寸计算，无需坐标换算与占位补偿。
// 只作用于画布窗口本体，不能上提到外层包装：页面设置浮标（position: fixed）会被一并缩放错位。
const canvasZoom = ref(1);
const canvasZoomStyle = computed(() => (isPcNewCanvas.value && canvasZoom.value !== 1 ? { zoom: canvasZoom.value } : null));

// —— pc-new 槽位轨道：模块标签/操作条位于横向滚动容器之外，纵向位置按画布内模块实测对齐 ——
const modulesWarpRef = ref<HTMLElement | null>(null);
const railLeftRef = ref<HTMLElement | null>(null);
const railTops = ref<number[]>([]);
// 画布不满滚动区时（宽屏居中留白）轨道内收贴住画布边缘；可滚动时留白为 0，轨道贴滚动区边缘
const railGap = ref(0);
const railReady = computed(() => railTops.value.length > 0 && railTops.value.length === store.moduleList.length);
const railActiveIndex = computed(() => (typeof store.activeModuleIndex === "number" ? store.activeModuleIndex : -1));
const railActiveModule = computed(() => (railActiveIndex.value >= 0 ? store.moduleList[railActiveIndex.value] : undefined));

let railResizeObserver: ResizeObserver | null = null;
let railMutationObserver: MutationObserver | null = null;
let railMeasureFrame: number | null = null;

const resetRailMeasurement = () => {
    railTops.value = [];
    railGap.value = 0;
};

const measureRailTops = () => {
    const warp = modulesWarpRef.value;
    const rail = railLeftRef.value;
    if (!isPcNewCanvas.value || !warp || !rail) {
        resetRailMeasurement();
        return;
    }

    const list = warp.querySelector<HTMLElement>(":scope > .gallery-list-ul");
    const header = warp.querySelector<HTMLElement>(":scope > .pc-header-preview-warp");
    const items = list ? Array.from(list.querySelectorAll<HTMLElement>(":scope > .modules-item")) : [];
    const viewportEl = rail.parentElement;
    const windowEl = warp.parentElement;
    if (!list || !viewportEl || !windowEl) {
        resetRailMeasurement();
        return;
    }

    // 列表有 min-height 兜底：单个模块高度变化（异步预览/图片加载）不一定改变总高，
    // 必须逐项观察才能触发重测；对已观察元素重复 observe 是幂等的，不会循环触发。
    if (railResizeObserver) {
        railResizeObserver.observe(warp);
        railResizeObserver.observe(viewportEl);
        railResizeObserver.observe(list);
        if (header) railResizeObserver.observe(header);
        items.forEach((el) => railResizeObserver!.observe(el));
    }

    // ModuleRenderer 的异步组件尚未挂载时，模块项里只有 pc-new 的 sticky 边线，
    // 不能把同一个 top 提交给所有 rails。不要用高度作为唯一条件：空白模块允许配置为 0 高度。
    const layoutReady =
        items.length === store.moduleList.length &&
        items.every((el) =>
            Array.from(el.children).some(
                (child) => !child.classList.contains("module-sticky-frame") && !child.classList.contains("module-mask")
            )
        );
    if (!layoutReady) {
        resetRailMeasurement();
        return;
    }

    // 轨道与画布同处一个纵向滚动流，rect 差值不受纵向滚动位置影响
    const originTop = rail.getBoundingClientRect().top;
    railTops.value = items.map((el) => el.getBoundingClientRect().top - originTop);
    // 可滚动时画布左缘=滚动区左缘（差值 ≤0 被钳到 0），无需随横滚重测
    railGap.value = Math.max(0, Math.round(windowEl.getBoundingClientRect().left - viewportEl.getBoundingClientRect().left - 100));
};

const scheduleRailMeasurement = () => {
    if (railMeasureFrame !== null) return;
    railMeasureFrame = window.requestAnimationFrame(() => {
        railMeasureFrame = null;
        measureRailTops();
    });
};

const setupRailObservers = () => {
    railResizeObserver?.disconnect();
    railMutationObserver?.disconnect();
    railResizeObserver = null;
    railMutationObserver = null;
    resetRailMeasurement();

    const warp = modulesWarpRef.value;
    const rail = railLeftRef.value;
    if (!isPcNewCanvas.value || !warp || !rail) return;

    railResizeObserver = new ResizeObserver(scheduleRailMeasurement);
    railMutationObserver = new MutationObserver(scheduleRailMeasurement);
    railMutationObserver.observe(warp, {
        childList: true,
        subtree: true,
        attributes: true,
        attributeFilter: ["class", "style"]
    });
    scheduleRailMeasurement();
};

watch(
    [railLeftRef, modulesWarpRef, isPcNewCanvas],
    setupRailObservers,
    { flush: "post" }
);

// 等高模块换位时总高不变，ResizeObserver 感知不到：监听列表增删/换序重测
watch(
    () => store.moduleList.slice(),
    () => {
        resetRailMeasurement();
        nextTick(scheduleRailMeasurement);
    }
);

// CSS zoom 不改变元素本地盒尺寸（clientWidth/offsetHeight 不变），ResizeObserver 不触发，
// 但 getBoundingClientRect 的视觉位置已变：缩放后必须主动重测轨道位置
watch(canvasZoom, () => {
    resetRailMeasurement();
    nextTick(scheduleRailMeasurement);
});

onUnmounted(() => {
    if (railMeasureFrame !== null) {
        window.cancelAnimationFrame(railMeasureFrame);
        railMeasureFrame = null;
    }
    railResizeObserver?.disconnect();
    railResizeObserver = null;
    railMutationObserver?.disconnect();
    railMutationObserver = null;
});

// PC 自定义页依赖主题信息（旧 PcCustomPage.vue 入口壳的行为）
if (props.decorateType === 3) {
    useThemeStore().getThemeInfo();
}

const query = useRouter().currentRoute.value.query;
const pageTypeFromQuery = Number(query.pageType) || 1;
const parentId = Number(decorateInfo.decorateId) || 0;
const languageId = ref<any>(Number(decorateInfo.localeId) || 0);
// 新建模式：通过 act=add 或无 id 进入，进编辑器后自动弹模板库，延迟到首次保存/预览时才真正创建
// 注意：切换到「尚未建过的语言」时 URL 也是 id=0，但带有 parentId + 非默认语言，属于语言子页创建，不能当新建模板
const DEFAULT_DECORATE_TITLE = "页面名称";
const initialId = Number(query.id) || 0;
const isDefault = ref(decorateInfo.isDefault);
// 语言子页上下文：非默认语言且有父模板；新建页面(act=add)已由列表重置 decorateInfo，不参与
const isLocaleChild = String(query.act) !== "add" && !isDefault.value && parentId > 0;
const isAddMode = ref<boolean>(props.decorateType === 1 && (String(query.act) === "add" || (!initialId && !isLocaleChild)));
const isEditingDecorateTitle = ref(false);
const decorateTitleDraft = ref("");
const decorateTitleInputRef = ref<any>(null);
const isCommittingDecorateTitle = ref(false);
const decorateDisabled = ref(false);
const decorateChildren = ref<any[]>([]);
// 当前页是否为该类型的默认页（isHome=1）。仅 isHome=1 的个人中心页才需要在发布后切换 C 端新旧版
const currentIsHome = ref(0);
// 进入移动端个人中心页编辑器时预读的 C 端「新旧版」开关，发布时据此判断是否需要切换（避免重复写入）
const currentUseNewMemberCenter = ref<number | null>(null);

// 页面设置初始骨架（与旧 Index.vue 的 modules.pageModule 初始值一致）
const initialPageModule = () => ({
    type: "page",
    module: {},
    backgroundRepeat: "",
    backgroundSize: "",
    style: 0,
    // 头部风格默认“默认风格”(1)；缺省时前端会落到简约风格，故初始骨架必须显式带上
    headerStyle: 1
});

// 进入即重置并写入基础上下文（详情返回前 toolbar/预览区依赖 decorateType/pageType）
store.reset();
store.initDecorate({
    decorateId: initialId,
    decorateType: props.decorateType as 1 | 2 | 3,
    pageType: pageTypeFromQuery,
    decorateTitle: (query.title as string) || "",
    pageModule: initialPageModule(),
    localeId: languageId.value,
    parentId,
    isDefault: isDefault.value
});

const _decorateDetail = async () => {
    try {
        const result = await decorateDetail({
            id: store.decorateId,
            decorateType: props.decorateType,
            localeId: isLocaleChild ? languageId.value : 0,
            parentId: isLocaleChild ? parentId : 0
        });
        if (result !== null) {
            decorateChildren.value = result.children ?? [];
            currentIsHome.value = Number((result as any).isHome ?? 0);
            store.initDecorate({
                decorateId: result.decorateId || initialId,
                decorateType: props.decorateType as 1 | 2 | 3,
                pageType: pageTypeFromQuery,
                decorateTitle: result.decorateTitle || (query.title as string) || "",
                pageModule: Object.assign({}, store.pageModule, (result.data && result.data.pageModule) || {}),
                moduleList: (result.data && result.data.moduleList) || [],
                hasDraft: !!result.draftData,
                localeId: languageId.value,
                parentId,
                isDefault: isDefault.value
            });
        }
    } catch (error: any) {
        message.error(error.message);
    }
};

const localesList = ref<any[]>([]);
const _getLocalesList = async () => {
    try {
        const result = await getLocalesList({ size: 999, isEnabled: 1 });
        if (languageId.value == "") {
            result.records.map((item: any) => {
                if (item.isDefault) {
                    languageId.value = item.id;
                }
            });
        }
        if (languageId.value == "") {
            message.error("当前默认语言未发布，请先发布默认语言！");
            decorateDisabled.value = true;
        }
        localesList.value = result.records;
    } catch (error: any) {
        message.error(error.message);
    }
};
if (isAddMode.value) {
    // 新建模式：不拉详情，初始化默认名（空白页固定模块已由 initDecorate 注入），等待模板库选择
    store.decorateTitle = DEFAULT_DECORATE_TITLE;
} else {
    _decorateDetail();
}

// 移动端个人中心页编辑器：预读当前 C 端使用的新旧版开关，发布时据此判断是否需要切换
const fetchMemberCenterSwitch = async () => {
    if (!(props.decorateType === 1 && store.pageType === 2)) return;
    try {
        const res: any = await getMemberCenterDecorateSetting();
        currentUseNewMemberCenter.value = Number(res?.useNewMemberCenter ?? 1);
    } catch {
        currentUseNewMemberCenter.value = null;
    }
};
fetchMemberCenterSwitch();

const startEditDecorateTitle = () => {
    if (decorateDisabled.value) return;
    decorateTitleDraft.value = store.decorateTitle || "";
    isEditingDecorateTitle.value = true;
    nextTick(() => {
        decorateTitleInputRef.value?.focus?.();
    });
};

const commitDecorateTitle = async () => {
    if (isCommittingDecorateTitle.value) return;
    const val = (decorateTitleDraft.value || "").trim();
    const oldVal = (store.decorateTitle || "").trim();
    if (val === oldVal) {
        isEditingDecorateTitle.value = false;
        return;
    }
    if (!val) {
        message.warning("页面名称不能为空");
        return;
    }
    // 新建模式：尚未创建记录，仅更新本地名称，待首次保存/预览时随 create 提交
    if (!store.decorateId) {
        store.decorateTitle = val;
        isEditingDecorateTitle.value = false;
        return;
    }
    isCommittingDecorateTitle.value = true;
    try {
        await updateDecorateFiled({ id: store.decorateId, field: "decorateTitle", val });
        store.decorateTitle = val;
        message.success("页面名称修改成功");
        isEditingDecorateTitle.value = false;
    } catch (error: any) {
        message.error(error.message || "修改失败");
    } finally {
        isCommittingDecorateTitle.value = false;
    }
};

// 从其他页面复制：与模板导入同一归一化链（url 回填 + 固定模块注入 + pageModule 合并）
const copyDecorate = async (id: any) => {
    try {
        const result = await decorateDetail({
            id: id,
            decorateType: props.decorateType
        });
        store.applyTemplate({
            moduleList: (result.data && result.data.moduleList) || [],
            pageModule: (result.data && result.data.pageModule) || {}
        });
    } catch (error: any) {
        message.error(error.message);
    }
};

// 新建模式：自动弹出模板库 + 处理选择回调
const tplDialogRef = ref();
onMounted(() => {
    if (isAddMode.value) {
        nextTick(() => tplDialogRef.value?.show?.());
    }
});

/** 模板库选择回调：使用模板则填充真实装修 JSON；新建空白则保持空白页 */
const onTemplatePicked = (result: any) => {
    if (result?.useTemplate && result.template?.json) {
        store.applyTemplate(result.template.json);
    }
    // result.blank：保持空白页（已注入固定模块），无需处理
};

// 上移/下移/复制：约束校验失败时提示
const onMoveUp = (index: number) => {
    const result = store.moveUp(index);
    if (result !== true) message.warning(result.reason);
};
const onMoveDown = (index: number) => {
    const result = store.moveDown(index);
    if (result !== true) message.warning(result.reason);
};
const onCopyModule = (index: number) => {
    if (store.hasExclusiveModule) {
        message.warning("页面包含独占模块，不能复制或添加其他模块");
        return;
    }
    store.copyModule(index);
};

const onDragEnd = (e: any) => {
    store.selectModule(e.newIndex);
};
/** 拖拽放入：toolbar clone 校验失败时返回 {}（无 moduleIndex），在此移除 */
const onDragAdd = (e: any) => {
    const index = e.newIndex;
    const newItem = store.moduleList[index];
    if (newItem && !newItem.moduleIndex) {
        store.moduleList.splice(index, 1);
        return;
    }
    store.selectModule(index);
};

// 校验拼团活动（与旧编辑器一致）
const validateGrouponActivity = () => {
    const grouponModules = store.moduleList.filter((item: any) => item.type === "pcGroupon");
    if (grouponModules.length > 0) {
        for (const moduleItem of grouponModules) {
            if (!moduleItem.module?.grouponActivityItem) {
                message.warning("请为拼团组件选择拼团活动");
                return false;
            }
        }
    }
    return true;
};

// 校验点单卡片：图片模式必须配图；图文模式下图/标题/描述至少填一个
const validateStoreOrderCard = () => {
    const list = store.moduleList || [];
    for (let i = 0; i < list.length; i++) {
        const item: any = list[i];
        if (item?.type !== "storeOrderCard") continue;
        const mod: any = item.module || {};
        const cards: any[] = Array.isArray(mod.cardList) ? mod.cardList : [];
        const isImageMode = Number(mod.cardStyle) === 2;
        const hasEmpty = cards.some((c: any) => {
            if (isImageMode) return !c?.picUrl && !c?.picThumb;
            return !c?.picUrl && !c?.picThumb && !c?.title && !c?.desc;
        });
        if (hasEmpty) {
            store.selectModule(i);
            message.warning("存在未配置的点单卡片");
            return false;
        }
    }
    return true;
};

/**
 * 延迟创建：仅新建场景（id=0）在首次「预览/存草稿/发布」时校验名称并创建记录。
 * 返回 false 表示被拦截（名称未填或仍为默认值，或创建失败）。
 */
const ensureCreated = async (): Promise<boolean> => {
    if (store.decorateId) return true;
    // 语言子页：由 draft/publish 按 parentId+localeId 创建挂到父模板下，禁止 create 出顶层列表记录
    if (isLocaleChild) return true;
    const title = (store.decorateTitle || "").trim();
    if (!title || title === DEFAULT_DECORATE_TITLE) {
        message.warning("请先修改页面名称");
        return false;
    }
    try {
        const result: any = await updateDecorate("create", {
            decorateType: props.decorateType,
            pageType: store.pageType,
            decorateTitle: title
        });
        const newId = result?.decorateId ?? result?.id ?? 0;
        if (!newId) {
            message.error("创建失败");
            return false;
        }
        store.decorateId = newId;
        setDecorateId(newId);
        isAddMode.value = false;
        // history 模式：保留 base 路径，仅替换 query，便于刷新恢复且不触发组件重载
        const previewUrlQuery = query.previewUrl ? `&previewUrl=${encodeURIComponent(String(query.previewUrl))}` : "";
        history.replaceState(null, "", `${location.pathname}?id=${newId}&pageType=${store.pageType}${previewUrlQuery}`);
        return true;
    } catch (error: any) {
        message.error(error.message);
        return false;
    }
};

/** 语言子页首次保存由后端按 parentId+localeId 建记录，回填其 id 供预览/改名/删除使用 */
const syncLocaleChildId = async () => {
    if (store.decorateId || !isLocaleChild) return;
    try {
        const result = await decorateDetail({
            id: 0,
            decorateType: props.decorateType,
            localeId: languageId.value,
            parentId
        });
        if (result?.decorateId) {
            store.decorateId = result.decorateId;
        }
    } catch {
        // 回填失败不影响本次保存结果
    }
};

/** 组装保存参数：语言子页去掉 id，走后端 parentId+localeId 创建/更新子记录 */
const buildSavePayload = (data: Record<string, any>) => {
    const payload: any = {
        id: store.decorateId,
        decorateType: props.decorateType,
        pageType: store.pageType,
        localeId: isLocaleChild ? languageId.value : 0,
        parentId: isLocaleChild ? parentId : 0,
        data
    };
    if (payload.parentId) {
        delete payload.id;
    }
    return payload;
};

const onSavetoDraft = async () => {
    if (!validateGrouponActivity()) {
        return;
    }
    if (!validateStoreOrderCard()) {
        return;
    }
    if (!(await ensureCreated())) return;
    try {
        await saveDraft(buildSavePayload(store.getSaveData()));
        await syncLocaleChildId();
        message.success("操作成功");
        store.markAsSaved();
    } catch (error: any) {
        message.error(error.message);
    }
};

const onPublish = async () => {
    if (!validateGrouponActivity()) {
        return;
    }
    if (!validateStoreOrderCard()) {
        return;
    }
    if (!(await ensureCreated())) return;
    const saveData = store.getSaveData();
    // 个人中心页：pageModule 标记为新版，供前台识别
    if (store.pageType === 2) {
        saveData.pageModule = {
            ...saveData.pageModule,
            homeStyleVersion: "new"
        };
    }
    request({
        url: "decorate/decorate/publish",
        method: "post",
        data: buildSavePayload(saveData)
    })
        .then(async (result) => {
            await syncLocaleChildId();
            message.success("操作成功");
            store.markAsSaved();
            // 仅当发布的是移动端个人中心「默认页」(isHome=1) 时，才把 C 端应用为新版；
            // 且当前已是新版则跳过，避免无意义的重复写入
            if (props.decorateType === 1 && store.pageType === 2) {
                const alreadyNew = Number(currentUseNewMemberCenter.value) === 1;
                if (currentIsHome.value && !alreadyNew) {
                    try {
                        await saveMemberCenterDecorate({ useNewMemberCenter: 1 });
                        currentUseNewMemberCenter.value = 1;
                    } catch (e) {
                        /* 应用新版失败不影响发布结果 */
                    }
                }
            }
        })
        .catch((error) => {
            message.error(error.message);
        });
};

const format = computed(() => {
    const backgroundRepeat: any = {
        1: "no-repeat",
        2: "repeat",
        3: "repeat-y",
        4: "repeat-x",
        5: "no-repeat"
    };
    const backgroundSize: any = {
        1: "100% auto",
        2: "auto 100%",
        3: "100% 100%",
        4: "100% auto"
    };
    // 搜索页无「页面设置」入口：预览容器背景对齐前台，取搜索结果模块的 backgroundColor
    // （PC 首页则取 pageModule.backgroundColor）；PC 与移动端各自的搜索模块 type 不同
    let pageBg = store.pageModule.backgroundColor as string | undefined;
    if (store.pageType === 6) {
        const isSearchMod = (type?: string) => type === "pcSearchResult" || type === "mobileSearchResult";
        const searchMod = (isSearchMod(store.activeModule?.type) ? store.activeModule : null) || store.moduleList.find((m) => isSearchMod(m.type));
        pageBg = (searchMod?.module as { backgroundColor?: string } | undefined)?.backgroundColor || pageBg || "#f5f5f5";
    }
    return {
        backgroundColor: pageBg ? "background-color:" + pageBg + ";" : "",
        backgroundImage: (store.pageModule.backgroundImage as any)?.picUrl
            ? "background-image: url(" + (store.pageModule.backgroundImage as any)?.picUrl + ");"
            : "",
        backgroundRepeat: "background-repeat:" + backgroundRepeat[store.pageModule.backgroundRepeat as number] + ";",
        backgroundSize: "background-size:" + backgroundSize[store.pageModule.backgroundSize as number] + ";"
    };
});

// 沉浸式标题：仅个人中心页（pageType=2）且 pageModule.immersive 开启
const topbarImmersive = computed(() => {
    return store.pageType === 2 && !!(store.pageModule as any)?.immersive;
});

// 个人中心页样式 2/3/4 的默认渐变色（与移动端 userInfo3 保持一致）
const USER_INFO_GRADIENT_DEFAULTS: Record<number, { colorA: string; colorB: string }> = {
    2: { colorA: "#E0FCED", colorB: "#FDF1F3" },
    3: { colorA: "#F1EFE9", colorB: "#E7F0E6" },
    4: { colorA: "#FBD9E1", colorB: "#FDEDEF" }
};

// 顶部背景渐变方向映射（与移动端 userInfo3 一致）
const USER_INFO_GRADIENT_DIR_MAP: Record<string, string> = {
    upDown: "to bottom",
    leftRight: "to right",
    diagonal: "135deg"
};

// 取首个用户信息模块（userInfo3 / 老 userInfo）样式 2/3/4 的渐变色、方向与样式号
const userInfoGradientColors = computed(() => {
    const first = store.moduleList[0];
    if (!first || first.url !== "mobileMember") return null;
    if (first.type !== "userInfo3" && first.type !== "userInfo") return null;
    const mod = first.module as any;
    const style = Number(mod?.layoutStyle);
    if (style !== 2 && style !== 3 && style !== 4) return null;
    const def = USER_INFO_GRADIENT_DEFAULTS[style] ?? { colorA: "#fcf6fc", colorB: "#f3f3fe" };
    const gradient = mod?.gradient || {};
    return {
        style,
        colorA: gradient.colorA || def.colorA,
        colorB: gradient.colorB || def.colorB,
        gradientType: gradient.gradientType || "leftRight",
        memberBarShown: mod?.memberBar?.show !== false
    };
});

// 沉浸式 + 首模块为带渐变的 userInfo3 时：渐变只画在 .decorate-page-window 背景上「一层连续渐变带」，
// 连续覆盖标题栏与首模块之下（缝隙也被填满），组件自身渐变置透明透出 —— 单层渐变，天然无接缝（与样式 2/3 同机制）。
// - 样式 2/3：整段「渐变 + 从上到下渐隐到 #f5f5f5（338px）」。
// - 样式 4：实色渐变铺到「成长值卡一半」处后硬切到页面底色 #f5f5f5；该硬切边正好落在成长值卡内、
//   被卡片本身遮住，因此视觉上渐变精确止于成长值卡一半（高度限定）。下方高度按预览固定示例数据估算。
const STYLE4_BAND_HEIGHT_WITH_CARD = 226;
const STYLE4_BAND_HEIGHT_NO_CARD = 170;
const pageWindowGradientStyle = computed(() => {
    const c = userInfoGradientColors.value;
    if (!topbarImmersive.value || !c) return "";
    const dir = USER_INFO_GRADIENT_DIR_MAP[c.gradientType] || "to right";
    if (c.style === 4) {
        const h = c.memberBarShown ? STYLE4_BAND_HEIGHT_WITH_CARD : STYLE4_BAND_HEIGHT_NO_CARD;
        return `background-image: linear-gradient(${dir}, ${c.colorA}, ${c.colorB}) !important; background-size: 100% ${h}px !important; background-repeat: no-repeat !important; background-color: #f5f5f5 !important;`;
    }
    return `background-image: linear-gradient(to bottom, transparent 0%, #f5f5f5 338px), linear-gradient(${dir}, ${c.colorA}, ${c.colorB}) !important; background-size: 100% 338px !important; background-repeat: no-repeat !important; background-color: #f5f5f5 !important;`;
});

// 沉浸式且首模块为 userInfo3 样式 2/3/4 时，页面容器已统一绘制连续渐变带，
// 通知 userInfo3/View.vue 把卡片自身渐变背景置透明（与移动端 immersive 行为一致），
// 由页面那层连续渐变透出，避免组件再画一层渐变造成与标题栏区域的颜色分离/接缝。
provide(
    "memberImmersiveActive",
    computed(() => topbarImmersive.value && !!userInfoGradientColors.value)
);

const continueEditDraft = async () => {
    try {
        const result = await loadDraftData(store.decorateId);
        store.restoreFromDraft({
            pageModule: result.pageModule ?? store.pageModule,
            moduleList: result.moduleList ?? []
        });
    } catch (error: any) {
        message.error(error.message);
    }
};

const onPreview = async () => {
    if (!(await ensureCreated())) return;
    try {
        await saveDraft(buildSavePayload(store.getSaveData()));
        await syncLocaleChildId();

        message.success("操作成功");
        store.markHasDraft(false);
        setTimeout(() => {
            let domain = config.get("pcDomain") ? config.get("pcDomain") : window.location.origin;
            // 自定义预览地址
            if (query.previewUrl) {
                // 店铺首页
                if (query.previewUrl === "/pages/shop/index") {
                    const shopId = localStorage.getItem("shopId") || "";
                    const previewUrl = config.get("h5Domain")
                        ? config.get("h5Domain") + query.previewUrl
                        : window.location.origin + "/mobile" + query.previewUrl;
                    window.open(previewUrl + "?shopId=" + shopId + "&previewId=" + store.decorateId);
                }
            } else {
                if (props.decorateType == 1) {
                    window.open(urlWapFormat(getMobilePreviewPath(store.pageType, Number(store.decorateId))));
                } else if (props.decorateType == 2) {
                    // PC 搜索页装修（pageType=6）预览落到 /search，其余落到首页
                    const pcPreviewPath = store.pageType === 6 ? "/search" : "/";
                    window.open(`${domain}${pcPreviewPath}?previewId=${store.decorateId}`);
                } else if (props.decorateType == 3) {
                    window.open(`${domain}/activity?previewId=${store.decorateId}`);
                }
            }
        }, 800);
    } catch (error: any) {
        message.error(error.message);
    }
};

const { VITE_BASE_DIR } = import.meta.env;
const PAGE_TYPE_TAB_MAP: Record<number, string> = { 1: "home", 4: "classify", 2: "user", 3: "activity", 5: "login", 6: "search", 8: "productDetail" };
const getMobilePreviewPath = (pageType: number, previewId: number): string => {
    switch (pageType) {
        case 4:
            return `/pages/productCate/index?previewId=${previewId}`;
        case 2:
            return `/pages/member/index?previewId=${previewId}`;
        case 3:
            return `/pages/activity/index?id=${previewId}`;
        case 5:
            return `/pages/login/index?previewId=${previewId}`;
        case 6:
            return `/pages/search/result?previewId=${previewId}`;
        case 8:
            return `/pages/product/index?previewId=${previewId}`;
        default:
            return `/?previewId=${previewId}`;
    }
};

const onLogout = () => {
    if (getAdminType() === "admin") {
        if (props.decorateType == 1) {
            const tab = PAGE_TYPE_TAB_MAP[store.pageType] ?? "home";
            window.location.href = `${baseDirFormat(VITE_BASE_DIR) ?? ""}/decorate-info/mobile-decorate/list?tab=${tab}`;
        } else {
            window.location.href = `${baseDirFormat(VITE_BASE_DIR) ?? ""}/decorate-info/pc-decorate/list/`;
        }
    } else {
        window.location.href = `${baseDirFormat(VITE_BASE_DIR) ?? ""}/setting/mobile-shop-decorate/`;
    }
};

const languageName = computed(() => {
    return localesList.value.find((item) => item.id == languageId.value)?.isDefault
        ? `${localesList.value.find((item) => item.id == languageId.value)?.language}(默认)`
        : localesList.value.find((item) => item.id == languageId.value)?.language || "";
});

const changeLanguage = (localeId: any, isDefaultFlag: number) => {
    languageId.value = localeId;
    setLocaleId(localeId);
    if (isDefaultFlag == 1) {
        setIsDefault(true);
        navigateToNewWindow(decorateInfo.decorateId);
    } else {
        setIsDefault(false);
        const child = decorateChildren.value.find((item: any) => item.localeId == localeId);
        if (child) {
            navigateToNewWindow(child.decorateId);
        } else {
            // 尚未建过该语言：id=0，靠 decorateInfo 里的 parentId 在保存时创建子记录
            navigateToNewWindow(0, store.decorateTitle);
        }
    }
};

const navigateToNewWindow = (id?: number, title?: string) => {
    // 切换语言必须带上 pageType，否则 Number(query.pageType)||1 会回落成首页，搜索/登录等专页工具栏丢失
    const pageTypeQuery = `&pageType=${store.pageType}`;
    const previewQuery = query.previewUrl ? `&previewUrl=${encodeURIComponent(String(query.previewUrl))}` : "";
    let fullPath = "";
    if (props.decorateType === 1) {
        fullPath = `${window.location.origin}${baseDirFormat(VITE_BASE_DIR) ?? ""}/decorate/index?id=${id}&title=${title || ""}${pageTypeQuery}${previewQuery}`;
    } else if (props.decorateType === 2) {
        fullPath = `${window.location.origin}${baseDirFormat(VITE_BASE_DIR) ?? ""}/decorate/pc?id=${id}&title=${title || ""}${pageTypeQuery}`;
    } else if (props.decorateType === 3) {
        fullPath = `${window.location.origin}${baseDirFormat(VITE_BASE_DIR) ?? ""}/decorate/pcCustomPage?id=${id}&title=${title || ""}${pageTypeQuery}`;
    }
    window.location.href = fullPath;
};

const _delDecorate = () => {
    Modal.confirm({
        title: "提示",
        content: "删除后将无法恢复，确认删除该模板？",
        onOk: async () => {
            try {
                await delDecorate({ id: store.decorateId });
                message.success("操作成功");
                setTimeout(() => {
                    onLogout();
                }, 300);
            } catch (error: any) {
                message.error(error.message);
            }
        }
    });
};
</script>
<style lang="less" scoped>
.decorate-page-alert {
    margin: 30px auto;
    max-width: 500px;
    font-size: 14px;
}
.lang-item-active {
    color: var(--tig-primary);
}

:deep(.responsive-box) {
    margin: 0 auto;
}

/* fallback 拖拽使用鼠标事件模拟，画布排序时不应选中预览里的文字 */
:deep(.gallery-list-ul),
:deep(.gallery-list-ul *) {
    -webkit-user-select: none;
    user-select: none;
}

.topbar-now-txt-edit {
    display: flex;
    align-items: center;
    height: 30px;

    .topbar-now-txt-title {
        display: inline-block;
        height: 30px;
        line-height: 30px;
        padding: 0 12px;
        border: 1px solid transparent;
        border-radius: 2px;
        cursor: pointer;
        margin-bottom: 1px;

        &:hover {
            border-color: #e9ecf0;
        }
    }

    .topbar-now-txt-input {
        width: 200px;

        :deep(.el-input__wrapper) {
            height: 30px;
            padding-left: 12px;
        }
    }
}
</style>
