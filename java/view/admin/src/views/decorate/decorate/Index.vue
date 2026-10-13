<template>
    <div
        class="decorate-page"
        :class="{
            'decorate-page-pc': props.decorateType === 2 && pageType !== 6,
            'decorate-page-pc-custom': props.decorateType === 3 || (props.decorateType === 2 && pageType === 6)
        }"
    >
        <div class="decorate-topbar-warp">
            <div class="decorate-topbar-con">
                <div class="topbar-left">
                    <div class="topbar-logo"></div>
                    <div class="topbar-now-txt cursor-pointer" @click="onLogout()">
                        <i class="iconfont-admin icon-tuichu" style="margin-right: 8px"></i>{{ $t("退出编辑") }}
                    </div>
                    <div class="topbar-now-txt topbar-now-txt-edit">
                        <span class="tit">{{ $t("正在装修：") }}</span>
                        <el-input
                            v-if="isEditingDecorateTitle"
                            ref="decorateTitleInputRef"
                            v-model="decorateTitleDraft"
                            class="topbar-now-txt-input"
                            size="small"
                            :maxlength="50"
                            :placeholder="$t('请输入页面名称')"
                            @blur="commitDecorateTitle"
                            @keyup.enter="commitDecorateTitle"
                        />
                        <span v-else class="topbar-now-txt-title" @click="startEditDecorateTitle">
                            {{ decorateTitle || $t("未命名页面") }}
                        </span>
                    </div>

                </div>
                <el-space class="topbar-right">
                    <el-button class="btn" text @click="onPreview()" :disabled="decorateDisabled">{{ $t("预览") }}</el-button>
                    <el-button class="btn" text @click="onSavetoDraft()" :disabled="decorateDisabled">{{ $t("存至草稿") }}</el-button>
                    <el-button @click="onPublish()" type="primary" :disabled="decorateDisabled">{{ $t("保存并发布") }}</el-button>
                </el-space>
            </div>
        </div>
        <main class="pcMainContent">
            <ToolBar v-model:modules="modules" v-if="props.decorateType === 1" :page-type="pageType" @item-added="onAdd"></ToolBar>
            <PcToolBar v-model:modules="modules" v-if="props.decorateType === 2 && pageType !== 6" @item-added="onAdd"></PcToolBar>
            <PcCustomPageToolBar v-model:modules="modules" v-if="props.decorateType === 3" @item-added="onAdd"></PcCustomPageToolBar>
            <PcSearchToolBar v-model:modules="modules" v-if="props.decorateType === 2 && pageType === 6" @item-added="onAdd"></PcSearchToolBar>
            <perfect-scrollbar class="decorate-page-wrap">
                <Alert
                    v-if="hasDraftData"
                    class="decorate-page-alert"
                    :message="$t('该页面有一条草稿记录，您可以还原草稿继续编辑。')"
                    type="warning"
                    show-icon
                    closable
                >
                    <template #action>
                        <el-button @click="continueEditDraft">{{ $t("继续编辑") }}</el-button>
                    </template>
                </Alert>
                <ResponsiveBox :baseWidth="1600">
                    <template #default="{ scale }">
                        <!--                        <div :style="[{ transform: 'scale(' + scale + ')', transformOrigin: '0 0'}]">-->
                        <div
                            class="decorate-page-window"
                            :style="
                                pageWindowGradientStyle ||
                                'background-position: top center;' +
                                    format.backgroundColor +
                                    format.backgroundImage +
                                    format.backgroundRepeat +
                                    format.backgroundSize
                            "
                        >
                            <div class="theme-modules-warp">
                                <div
                                    v-if="![2, 3].includes(decorateType)"
                                    @click="onEditPage"
                                    :class="
                                        'list-item modules-item modules-item-topbar' +
                                        (editVisiable === 'page' ? ' module-item-active' : '') +
                                        (topbarImmersive ? ' modules-item-topbar--immersive' : '')
                                    "
                                    draggable="false"
                                >
                                    <Page v-model:module="modules.pageModule"></Page>
                                </div>
                                <draggable
                                    class="gallery-list-ul"
                                    item-key="moduleIndex"
                                    :list="modules.moduleList"
                                    ghost-class="sortable-ghost"
                                    chosen-class="chosenClass"
                                    animation="300"
                                    @start=""
                                    @end="onEnd"
                                    @add="onAdd"
                                    :group="{ name: 'advanced', pull: false, put: true }"
                                    style="min-height: 700px"
                                >
                                    <template #item="{ element, index }">
                                        <div
                                            :class="['list-item modules-item', { 'module-item-active': editModuleIndex === index }]"
                                            draggable="false"
                                            @click="onEditComponent(index)"
                                        >
                                            <div class="module-mask" v-if="!element.isShow">
                                                <i class="ico-decorate icon-dec-mimayincang" style="font-size: 25px"></i>
                                            </div>
                                            <Modules
                                                v-if="element.type"
                                                v-model:module="element.module"
                                                v-model:moduleType="element.type"
                                                v-model:moduleUrl="element.url"
                                                :moduleIndex="element.moduleIndex"
                                                :decorateId="id"
                                            ></Modules>
                                            <div class="module-label">
                                                <div class="label-name">{{ $t(element.label) }}</div>
                                            </div>
                                            <div class="module-operate" v-if="index === editModuleIndex && editVisiable == 'list'">
                                                <div class="module-operate-item" @click.stop="onModuleUp(index)">
                                                    <i class="ico-decorate icon-dec-shangyi"></i>
                                                    <div class="opt-tip">{{ $t("上移") }}</div>
                                                </div>
                                                <div class="module-operate-item" @click.stop="onModuleDown(index)">
                                                    <i class="ico-decorate icon-dec-xiayi"></i>
                                                    <div class="opt-tip">{{ $t("下移") }}</div>
                                                </div>
                                                <div class="module-operate-item" v-if="!element.fixed" @click.stop="onModuleShow(index)">
                                                    <i :class="'ico-decorate ' + (element.isShow ? 'icon-dec-mimaxianshi' : 'icon-dec-mimayincang')"></i>
                                                    <div class="opt-tip">{{ $t("显示") }}</div>
                                                </div>
                                                <div class="module-operate-item" v-if="!element.fixed" @click.stop="onModuleCopy(index)">
                                                    <i class="ico-decorate icon-dec-fuzhi1"></i>
                                                    <div class="opt-tip">{{ $t("复制") }}</div>
                                                </div>
                                                <div class="module-operate-item" v-if="!element.fixed" @click.stop="onModuleDel(index)">
                                                    <i class="ico-decorate icon-dec-shanchu"></i>
                                                    <div class="opt-tip">{{ $t("删除") }}</div>
                                                </div>
                                            </div>
                                        </div>
                                    </template>
                                </draggable>
                            </div>
                        </div>
                        <!-- PC 搜索页装修不展示页面设置 -->
                        <div class="decorate-page-config-warp" v-if="!(props.decorateType === 2 && pageType === 6)">
                            <div class="decorate-page-config-con">
                                <div class="page-config-item" @click="onEditPage">
                                    <i class="ico-decorate icon-dec-yemianshezhi"></i><span>{{ $t("页面设置") }}</span>
                                </div>
                            </div>
                        </div>
                        <!--                        </div>-->
                    </template>
                </ResponsiveBox>
            </perfect-scrollbar>
            <div class="decorate-edit-wrap">
                <!-- <editComponent
                    v-if="editModuleIndex !== null && editVisiable == 'list' && modules.moduleList[editModuleIndex]"
                    v-model:module="modules.moduleList[editModuleIndex].module"
                ></editComponent> -->
                <editComponent
                    v-if="editModuleIndex !== null && editVisiable === 'list' && modules.moduleList[editModuleIndex]"
                    v-model:module="modules.moduleList[editModuleIndex].module"
                ></editComponent>
                <PageEdit v-if="editVisiable === 'page'" v-model:module="modules.pageModule" :page-type="pageType"></PageEdit>
            </div>
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
            :params="{ decorateType: props.decorateType, pageType: pageType, scene: 'editor' }"
            @okCallback="onTemplatePicked"
        >
            <span style="display: none"></span>
        </DialogForm>
    </div>
</template>
<script lang="ts" setup>
import { DialogForm } from "@/components/dialog";
import { ArrowDown } from "@element-plus/icons-vue";
import { ref, defineAsyncComponent, onMounted, shallowRef, computed, nextTick, provide } from "vue";
import { useRouter } from "vue-router";
import request from "@/utils/request";
import { loadDraftData, saveDraft, decorateDetail } from "@/api/decorate/decorate";
import ToolBar from "./src/ToolBar.vue";
import PcToolBar from "./src/PcToolBar.vue";
import PcCustomPageToolBar from "./src/PcCustomPageToolBar.vue";
import PcSearchToolBar from "./src/PcSearchToolBar.vue";
import Modules from "./src/Modules.vue";
import Page from "./src/modules/src/commonPage/View.vue";
import PageEdit from "./src/modules/src/commonPage/Edit.vue";
import draggable from "vuedraggable";
import { message, Alert, Modal } from "ant-design-vue";
import { cloneDeep, set } from "lodash";
import { toCamelCase } from "@/utils/util";
import { urlWapFormat, urlFormat, baseDirFormat } from "@/utils/format";
import { ModulesType, EditResult } from "@/types/decorate/decorate.d";
import { ModuleTypeList } from "@/views/decorate/decorate/src/modules/editIndex";
import { ensureFixedModules } from "@/views/decorate/decorate/src/modules/ensureFixed";
import { shouldBuildMemberDefault, buildMemberCenterDefaultModules } from "@/views/decorate/decorate/src/modules/memberCenterFallback";
import { shouldBuildCategoryDefault, buildCategoryCenterDefaultModules } from "@/views/decorate/decorate/src/modules/categoryCenterFallback";
import { getLocalesList } from "@/api/multilingual/languagesList";
import { delDecorate, updateDecorateFiled, updateDecorate } from "@/api/decorate/mobileDecorate";
import { saveMemberCenterDecorate, getMemberCenterDecorateSetting } from "@/api/setting/config";
import { isOverseas } from "@/utils/version";
import "@/views/decorate/decorate/src/css/decorate.less";
import "@/views/decorate/decorate/src/css/module.less";
import { useDecorateStore } from "@/store/decorate";
import { getAdminType } from "@/utils/storage";
import { ResponsiveBox } from "@/components/responsive-box";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const { decorateInfo, setLocaleId, setIsDefault } = useDecorateStore();
import { useConfigStore } from "@/store/config";
const config = useConfigStore();
const props = defineProps({
    decorateType: {
        type: Number,
        default: 1
    }
});
const query = useRouter().currentRoute.value.query;
const id = ref<number>(Number(query.id) || 0);
const pageType = ref<number>(Number(query.pageType) || 1);
const parentId = ref<number>(Number(decorateInfo.decorateId) || 0);
const languageId = ref<any>(Number(decorateInfo.localeId) || 0);
const decorateTitle = ref<string>((query.title as string) || "");
// 新建模式：通过 act=add 或无 id 进入，进编辑器后自动弹模板库，延迟到首次保存/预览时才真正创建
// 注意：切换到「尚未建过的语言」时 URL 也是 id=0，但带有 parentId + 非默认语言，属于语言子页创建，不能当新建模板
const DEFAULT_DECORATE_TITLE = t("页面名称");
const isDefault = ref(decorateInfo.isDefault);
// 语言子页上下文：非默认语言且有父模板；新建页面(act=add)已由列表重置 decorateInfo，不参与
const isLocaleChild = String(query.act) !== "add" && !isDefault.value && parentId.value > 0;
const isAddMode = ref<boolean>(
    props.decorateType === 1 && (String(query.act) === "add" || (!id.value && !isLocaleChild))
);
const isEditingDecorateTitle = ref(false);
const decorateTitleDraft = ref("");
const decorateTitleInputRef = ref<any>(null);
const isCommittingDecorateTitle = ref(false);
const hasDraftData = ref(false);
const decorateDisabled = ref(false);
const modules = ref<ModulesType>({
    // 页面模块
    pageModule: {
        type: "page",
        module: {},
        backgroundRepeat: "",
        backgroundSize: "",
        style: 0,
        // 头部风格默认“默认风格”(1)；缺省时前端会落到简约风格，故初始骨架必须显式带上
        headerStyle: 1
    },
    // 模块列
    moduleList: []
});
// 编辑状态 list|page
const editVisiable = ref<string>("");
// 动态编辑组件
const editComponent = shallowRef();
// 当前编辑的组件index
const editModuleIndex = ref<number | null>(null);
// 桶迁移兼容：folder 已迁移到新桶的 type，强制把历史草稿烧录的旧 url 纠正过来（加载时自愈）
const RELOCATED_TYPE_URLS: Record<string, string> = {
    newcomerCombo: "mobilePro",
    limitedSpecial: "mobilePro"
};
const updateModuleListWithUrls = (moduleList: any = [], moduleTypeList: any = []) => {
    const typeToUrlMap = {};
    // 将 ModuleTypeList 转换为 type 到 url 的映射
    moduleTypeList.forEach(({ type, url }) => {
        typeToUrlMap[type] = url;
    });
    // 遍历 moduleList 更新 url
    moduleList.forEach((module: any) => {
        if (RELOCATED_TYPE_URLS[module.type]) {
            // 已迁移桶的模块：强制纠正 url，避免旧草稿指向已不存在的旧桶路径
            module.url = RELOCATED_TYPE_URLS[module.type];
        } else if (!module.url) {
            // 如果 module.url 为空，通过 type 获取 url
            const correspondingUrl = typeToUrlMap[module.type];
            if (correspondingUrl) {
                module.url = correspondingUrl; // 更新模块的 url
            }
        }
    });
    console.log(moduleList, "moduleListmoduleListmoduleList");
    return moduleList;
};
const decorateChildren = ref<any[]>([]);
// 当前页是否为该类型的默认页（isHome=1）。仅 isHome=1 的个人中心页才需要在发布后切换 C 端新旧版
const currentIsHome = ref(0);
// 进入移动端个人中心页编辑器时预读的 C 端「新旧版」开关，发布时据此判断是否需要切换（避免重复写入）
const currentUseNewMemberCenter = ref<number | null>(null);
const _decorateDetail = async () => {
    try {
        const result = await decorateDetail({
            id: id.value,
            decorateType: props.decorateType,
            localeId: isLocaleChild ? languageId.value : 0,
            parentId: isLocaleChild ? parentId.value : 0
        });
        if (result !== null) {
            let moduleList = (result.data && result.data.moduleList) || [];
            let pageModule = (result.data && result.data.pageModule) || {};
            // 会员中心兜底：老会员页用户（无装修内容）时，自动拼装一套默认会员中心模块
            if (pageType.value === 2 && shouldBuildMemberDefault(moduleList)) {
                moduleList = await buildMemberCenterDefaultModules();
            }
            // 分类页兜底：老分类页用户（无装修内容）时，按老配置拼装对应分类中心模块
            if (pageType.value === 4 && shouldBuildCategoryDefault(moduleList)) {
                moduleList = await buildCategoryCenterDefaultModules();
            }
            modules.value.moduleList = updateModuleListWithUrls(moduleList, ModuleTypeList);
            ensureFixedModules(modules.value.moduleList, pageType.value);
            decorateTitle.value = result.decorateTitle || (query.title as any) || "";
            decorateChildren.value = result.children ?? [];
            currentIsHome.value = Number((result as any).isHome ?? 0);
            id.value = result.decorateId || (query.id as any) || 0;
            Object.assign(modules.value.pageModule, pageModule);
            hasDraftData.value = result.draftData ? true : false;
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};

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
            message.error(t("当前默认语言未发布，请先发布默认语言！"));
            decorateDisabled.value = true;
        }
        localesList.value = result.records;
    } catch (error: any) {
        message.error(error.message);
    }
};
if (isAddMode.value) {
    // 新建模式：不拉详情，初始化默认名 + 空白页固定模块，等待模板库选择
    decorateTitle.value = DEFAULT_DECORATE_TITLE;
    ensureFixedModules(modules.value.moduleList, pageType.value);
} else {
    _decorateDetail();
}

// 移动端个人中心页编辑器：预读当前 C 端使用的新旧版开关，发布时据此判断是否需要切换
const fetchMemberCenterSwitch = async () => {
    if (!(props.decorateType === 1 && pageType.value === 2)) return;
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
    decorateTitleDraft.value = decorateTitle.value || "";
    isEditingDecorateTitle.value = true;
    nextTick(() => {
        decorateTitleInputRef.value?.focus?.();
    });
};

const commitDecorateTitle = async () => {
    if (isCommittingDecorateTitle.value) return;
    const val = (decorateTitleDraft.value || "").trim();
    const oldVal = (decorateTitle.value || "").trim();
    if (val === oldVal) {
        isEditingDecorateTitle.value = false;
        return;
    }
    if (!val) {
        message.warning(t("页面名称不能为空"));
        return;
    }
    // 新建模式：尚未创建记录，仅更新本地名称，待首次保存/预览时随 create 提交
    if (!id.value) {
        decorateTitle.value = val;
        isEditingDecorateTitle.value = false;
        return;
    }
    isCommittingDecorateTitle.value = true;
    try {
        await updateDecorateFiled({ id: id.value, field: "decorateTitle", val });
        decorateTitle.value = val;
        message.success(t("页面名称修改成功"));
        isEditingDecorateTitle.value = false;
    } catch (error: any) {
        message.error(error.message || t("修改失败"));
    } finally {
        isCommittingDecorateTitle.value = false;
    }
};

const copyDecorate = async (id: any) => {
    try {
        const result = await decorateDetail({
            id: id,
            decorateType: props.decorateType
        });
        let moduleList = (result.data && result.data.moduleList) || [];
        let pageModule = (result.data && result.data.pageModule) || {};
        modules.value.moduleList = updateModuleListWithUrls(moduleList, ModuleTypeList);
        ensureFixedModules(modules.value.moduleList, pageType.value);
        Object.assign(modules.value.pageModule, pageModule);
    } catch (error: any) {
        message.error(error.message);
    } finally {
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
        const json = result.template.json;
        modules.value.moduleList = updateModuleListWithUrls(json.moduleList || [], ModuleTypeList);
        ensureFixedModules(modules.value.moduleList, pageType.value);
        Object.assign(modules.value.pageModule, json.pageModule || {});
    }
    // result.blank：保持空白页（已注入固定模块），无需处理
};

// 组件编辑
const _import = (url: string, path: string) => defineAsyncComponent(() => import(`./src/modules/${url}/${path}/Edit.vue`));
const onEditComponent = (index: number) => {
    if (index == null) return;
    editModuleIndex.value = index;
    modules.value.moduleList = modules.value.moduleList.map((item) => ({ ...item }));
    editComponent.value = _import(modules.value.moduleList[index]?.url, toCamelCase(modules.value.moduleList[index]?.type));
    editVisiable.value = "list";
};
// 页面编辑
const onEditPage = () => {
    editVisiable.value = "page";
    modules.value.moduleList = modules.value.moduleList.map((item) => ({ ...item }));
};
// 上移
const onModuleUp = (index: number) => {
    if (index == 0) return;
    modules.value.moduleList.splice(index - 1, 0, modules.value.moduleList.splice(index, 1)[0]);
    editModuleIndex.value = index - 1;
};
// 下移
const onModuleDown = (index: number) => {
    if (index + 1 == modules.value.moduleList.length) return;
    modules.value.moduleList.splice(index + 1, 0, modules.value.moduleList.splice(index, 1)[0]);
    editModuleIndex.value = index + 1;
};
// 显示/隐藏
const onModuleShow = (index: number) => {
    if (modules.value.moduleList[index]?.fixed) return;
    modules.value.moduleList[index].isShow = modules.value.moduleList[index].isShow == true ? false : true;
};
const EXCLUSIVE_TYPES = new Set(["cateCenterV1", "cateCenterV2"]);
const hasExclusiveModule = () => modules.value.moduleList?.some((m: any) => EXCLUSIVE_TYPES.has(m.type));

// 复制
const onModuleCopy = (index: number) => {
    if (modules.value.moduleList[index]?.fixed) return;
    if (hasExclusiveModule()) {
        message.warning(t("页面包含独占模块，不能复制或添加其他模块"));
        return;
    }
    const copiedItem = cloneDeep({ ...modules.value.moduleList[index] });
    copiedItem.moduleIndex = Date.now();
    modules.value.moduleList.splice(index + 1, 0, copiedItem);
    editModuleIndex.value = index + 1;
};
// 删除
const onModuleDel = (index: number) => {
    if (modules.value.moduleList[index]?.fixed) return;
    modules.value.moduleList.splice(index, 1);
    if (modules.value.moduleList.length === 0) {
        editModuleIndex.value = null;
    } else {
        editModuleIndex.value = index - 1;
    }
    onEditComponent(editModuleIndex.value as number);
};
const onEnd = (e: any) => {
    editModuleIndex.value = e.newIndex;
};
const onAdd = (e: any) => {
    const index = e.newIndex;
    const newItem = modules.value.moduleList[index];
    if (newItem && !newItem.moduleIndex) {
        modules.value.moduleList.splice(index, 1);
        return;
    }
    const exclusiveCount = modules.value.moduleList.filter((m: any) => EXCLUSIVE_TYPES.has(m.type)).length;
    if (EXCLUSIVE_TYPES.has(newItem?.type) && modules.value.moduleList.length > 1) {
        message.warning(t("独占模块只能在空页面中添加，请先清空已有模块"));
        modules.value.moduleList.splice(index, 1);
        return;
    }
    if (!EXCLUSIVE_TYPES.has(newItem?.type) && exclusiveCount > 0) {
        message.warning(t("页面已包含独占模块，不能添加其他模块"));
        modules.value.moduleList.splice(index, 1);
        return;
    }
    editModuleIndex.value = index;
    onEditComponent(e.newIndex);
};
// 校验拼团活动（移动端 groupon / PC 端 pcGroupon）
const validateGrouponActivity = () => {
    const list = modules.value.moduleList || [];
    for (let i = 0; i < list.length; i++) {
        const item = list[i];
        if (item.type !== "groupon" && item.type !== "pcGroupon") continue;
        if (item.module?.grouponActivityItem) {
            if (item.module.activitySelectError) {
                item.module.activitySelectError = false;
            }
            continue;
        }
        if (!item.module) item.module = {};
        item.module.activitySelectError = true;
        editVisiable.value = "list";
        onEditComponent(i);
        return false;
    }
    return true;
};

// 校验点单卡片：图片模式必须配图；图文模式下图/标题/描述至少填一个
// 命中未配置的模块时，自动激活到该模块编辑，方便用户直接修改
const validateStoreOrderCard = () => {
    const list = modules.value.moduleList || [];
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
            onEditComponent(i);
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
    if (id.value) return true;
    // 语言子页：由 draft/publish 按 parentId+localeId 创建挂到父模板下，禁止 create 出顶层列表记录
    if (isLocaleChild) return true;
    const title = (decorateTitle.value || "").trim();
    if (!title || title === DEFAULT_DECORATE_TITLE) {
        message.warning(t("请先修改页面名称"));
        return false;
    }
    try {
        const result: any = await updateDecorate("create", {
            decorateType: props.decorateType,
            pageType: pageType.value,
            decorateTitle: title
        });
        id.value = result?.decorateId ?? result?.id ?? 0;
        if (!id.value) {
            message.error(t("创建失败"));
            return false;
        }
        isAddMode.value = false;
        // history 模式：保留 base 路径，仅替换 query，便于刷新恢复且不触发组件重载
        history.replaceState(null, "", `${location.pathname}?id=${id.value}&pageType=${pageType.value}`);
        return true;
    } catch (error: any) {
        message.error(error.message);
        return false;
    }
};

/** 语言子页首次保存由后端按 parentId+localeId 建记录，回填其 id 供预览/改名/删除使用 */
const syncLocaleChildId = async () => {
    if (id.value || !isLocaleChild) return;
    try {
        const result = await decorateDetail({
            id: 0,
            decorateType: props.decorateType,
            localeId: languageId.value,
            parentId: parentId.value
        });
        if (result?.decorateId) {
            id.value = result.decorateId;
        }
    } catch {
        // 回填失败不影响本次保存结果
    }
};

/** 组装保存参数：语言子页去掉 id，走后端 parentId+localeId 创建/更新子记录 */
const buildSavePayload = (data: Record<string, any>) => {
    const payload: any = {
        id: id.value,
        decorateType: props.decorateType,
        pageType: pageType.value,
        localeId: isLocaleChild ? languageId.value : 0,
        parentId: isLocaleChild ? parentId.value : 0,
        data
    };
    if (payload.parentId) {
        delete payload.id;
    }
    return payload;
};

const onSavetoDraft = async () => {
    // 校验拼团活动
    if (!validateGrouponActivity()) {
        return;
    }
    // 校验点单卡片
    if (!validateStoreOrderCard()) {
        return;
    }
    if (!(await ensureCreated())) return;
    try {
        await saveDraft(buildSavePayload(modules.value));
        await syncLocaleChildId();
        message.success(t("操作成功"));
        hasDraftData.value = false;
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};
const onPublish = async () => {
    // 校验拼团活动
    if (!validateGrouponActivity()) {
        return;
    }
    // 校验点单卡片
    if (!validateStoreOrderCard()) {
        return;
    }
    if (!(await ensureCreated())) return;
    const data: any = buildSavePayload(modules.value);
    request({
        url: "decorate/decorate/publish",
        method: "post",
        data
    })
        .then(async (result) => {
            await syncLocaleChildId();
            message.success(t("操作成功"));
            hasDraftData.value = false;
            // 仅当发布的是移动端个人中心「默认页」(isHome=1) 时，才把 C 端应用为新版；
            // 且当前已是新版则跳过，避免无意义的重复写入
            if (props.decorateType === 1 && pageType.value === 2) {
                const isHomePage = Number((result as any)?.isHome ?? currentIsHome.value) === 1;
                const alreadyNew = Number(currentUseNewMemberCenter.value) === 1;
                if (isHomePage && !alreadyNew) {
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
    // 搜索页无「页面设置」入口：预览容器背景取搜索结果模块的 backgroundColor（对齐 PC 首页 pageModule）
    let pageBg = modules.value.pageModule.backgroundColor as string | undefined;
    if (pageType.value === 6) {
        const list = modules.value.moduleList || [];
        const activeIdx = editModuleIndex.value;
        const active = activeIdx !== null ? list[activeIdx] : null;
        const searchMod = (active?.type === "pcSearchResult" ? active : null) || list.find((m: any) => m.type === "pcSearchResult");
        pageBg = searchMod?.module?.backgroundColor || pageBg || "#f5f5f5";
    }
    return {
        backgroundColor: pageBg ? "background-color:" + pageBg + ";" : "",
        backgroundImage: modules.value.pageModule.backgroundImage?.picUrl
            ? "background-image: url(" + modules.value.pageModule.backgroundImage?.picUrl + ");"
            : "",
        backgroundRepeat: "background-repeat:" + backgroundRepeat[modules.value.pageModule.backgroundRepeat] + ";",
        backgroundSize: "background-size:" + backgroundSize[modules.value.pageModule.backgroundSize] + ";"
    };
});
// 沉浸式标题：仅个人中心页（pageType=2）且 pageModule.immersive 开启
const topbarImmersive = computed(() => {
    return pageType.value === 2 && !!(modules.value.pageModule as any)?.immersive;
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
    const first = modules.value.moduleList[0];
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
// 样式 4 渐变带高度（px，自页面窗口顶部起算，含沉浸式标题栏）：显示成长卡时到「卡一半」，隐藏时仅到头部底。
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
        const result = await loadDraftData(id.value);
        modules.value.moduleList.length = 0;
        modules.value.moduleList = result.moduleList ?? [];
        ensureFixedModules(modules.value.moduleList, pageType.value);
        modules.value.pageModule = result.pageModule ?? [];
        hasDraftData.value = false;
        editVisiable.value = "";
        editModuleIndex.value = null;
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};

const onPreview = async () => {
    if (!(await ensureCreated())) return;
    try {
        // 先存草稿（语言子页走 parentId+localeId）
        await saveDraft(buildSavePayload(modules.value));
        await syncLocaleChildId();

        // message.success("操作成功");
        hasDraftData.value = false;
        // 1秒后执行
        setTimeout(() => {
            // 自定义预览地址
            if (query.previewUrl) {
                // 店铺首页
                if (query.previewUrl === "/pages/shop/index") {
                    const shopId = localStorage.getItem("shopId") || "";
                    const previewUrl = config.get("h5Domain")
                        ? config.get("h5Domain") + query.previewUrl
                        : window.location.origin + "/mobile" + query.previewUrl;
                    window.open(previewUrl + "?shopId=" + shopId + "&previewId=" + id.value);
                }
            } else {
                if (props.decorateType == 1) {
                    window.open(urlWapFormat(getMobilePreviewPath(pageType.value, id.value)));
                } else if (props.decorateType == 2) {
                    // PC 搜索页装修（pageType=6）预览落到 /search，其余落到首页
                    const pcPreviewPath = pageType.value === 6 ? "/search" : "/";
                    window.open(`${window.location.origin}${pcPreviewPath}?previewId=${id.value}`);
                } else if (props.decorateType == 3) {
                    window.open(`${window.location.origin}/activity?previewId=${id.value}`);
                }
            }
        }, 800);
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
};
const { VITE_BASE_DIR } = import.meta.env;
const PAGE_TYPE_TAB_MAP: Record<number, string> = { 1: "home", 4: "classify", 2: "user", 3: "activity", 5: "login" };
const getMobilePreviewPath = (pageType: number, previewId: number): string => {
    switch (pageType) {
        case 4:
            return `/pages/productCate/index?previewId=${previewId}`;
        case 2:
            return `/pages/member/index?previewId=${previewId}`;
        case 3:
            return `/pages/activity/index?id=${previewId}`;
        default:
            return `/?previewId=${previewId}`;
    }
};
const onLogout = () => {
    if (getAdminType() === "admin") {
        if (props.decorateType == 1) {
            const tab = PAGE_TYPE_TAB_MAP[pageType.value] ?? "home";
            window.location.href = `${baseDirFormat(VITE_BASE_DIR) ?? ""}/decorate-info/mobile-decorate/list?tab=${tab}`;
        } else {
            window.location.href = `${baseDirFormat(VITE_BASE_DIR) ?? ""}/decorate-info/pc-decorate/list/`;
        }
    } else {
        window.location.href = `${baseDirFormat(VITE_BASE_DIR) ?? ""}/setting/mobile-shop-decorate/`;
    }
};
const localesList = ref<any[]>([]);
const languageName = computed(() => {
    return localesList.value.find((item) => item.id == languageId.value)?.isDefault
        ? t("{language}(默认)", { language: localesList.value.find((item) => item.id == languageId.value)?.language })
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
            navigateToNewWindow(0, decorateTitle.value);
        }
    }
};

const navigateToNewWindow = (id?: number, title?: string) => {
    // 切换语言必须带上 pageType，否则 Number(query.pageType)||1 会回落成首页，搜索/登录等专页工具栏丢失
    const pageTypeQuery = `&pageType=${pageType.value}`;
    let fullPath = "";
    if (props.decorateType === 1) {
        fullPath = `${window.location.origin}${baseDirFormat(VITE_BASE_DIR) ?? ""}/decorate/index?id=${id}&title=${title || ""}${pageTypeQuery}`;
    } else if (props.decorateType === 2) {
        fullPath = `${window.location.origin}${baseDirFormat(VITE_BASE_DIR) ?? ""}/decorate/pc?id=${id}&title=${title || ""}${pageTypeQuery}`;
    } else if (props.decorateType === 3) {
        fullPath = `${window.location.origin}${baseDirFormat(VITE_BASE_DIR) ?? ""}/decorate/pcCustomPage?id=${id}&title=${title || ""}${pageTypeQuery}`;
    }
    window.location.href = fullPath;
};

const _delDecorate = () => {
    Modal.confirm({
        title: t("提示"),
        content: t("删除后将无法恢复，确认删除该模板？"),
        onOk: async () => {
            try {
                const result = await delDecorate({ id: id.value });
                message.success(t("操作成功"));
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

// 装修画布模块 wrapper：每个模块建立独立堆叠上下文，
// 让 DOM 顺序后面的模块（下方模块）稳定覆盖前面的模块（上方模块），
// 从而支持模块通过负上边距上移与上一模块贴合/叠压时不被遮挡。
:deep(.gallery-list-ul) > .modules-item {
    position: relative;
    isolation: isolate;
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
