/**
 * 装修编辑器状态管理（移植自 Vort-Admin decorateEditor，按 Tigshop 契约适配）
 *
 * 与 Vort 的差异：
 * - decorateType 含 3（PC 自定义页），无 pcHeader 三态
 * - getSaveData 整体透传：不做字段剔除（契约红线第六章第 6 条）
 * - 不做 moduleIndex 去重（契约红线第七章第 7 条）
 * - initDecorate / restoreFromDraft 内置旧编辑器的加载侧归一化链
 * - 固定模块（fixed）不可删除 / 复制 / 隐藏，约束收敛到 store
 */

import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { cloneDeep } from "lodash-es";
import type { DecorateModuleBase, PageModule, DecorateData } from "@/types/decorate/editorModule";
import {
    getModulePlacement,
    isExclusiveModule,
    isTopModule,
    isBottomModule,
    getPcHomeModuleVersion,
    type PcHomeVersion
} from "@/views/decorate/decorate/editor/config/module-registry";
import { updateModuleListWithUrls, ensureFixedModules } from "@/views/decorate/decorate/editor/modules/normalize";
import { DecorateType, type DecorateTypeValue } from "@/views/decorate/decorate/editor/config/constants";
import { pcHeaderDefaults, type PcHeaderConfig } from "@/views/decorate/decorate/editor/modules/pcHome/_shared/pc-header/types";

export interface ModuleValidationResult {
    ok: boolean;
    reason?: string;
}

/** 编辑模式：page=页面设置，module=模块设置，pcHeader=PC头部设置 */
export type EditMode = "page" | "module" | "pcHeader" | null;

/**
 * 合并 PC 头部默认值
 * 后端可能只下发部分字段；若不补全，保存时会只提交「已有字段 + 被修改字段」导致丢字段
 */
function mergePcHeaderDefaults(raw?: Partial<PcHeaderConfig> | null): PcHeaderConfig {
    const incoming = (raw ?? {}) as Partial<PcHeaderConfig>;
    const memberCenterItems = Array.isArray(incoming.memberCenterItems)
        ? incoming.memberCenterItems.map((item) => {
              const legacyItem = item as NonNullable<PcHeaderConfig["memberCenterItems"]>[number] & { title?: string; link?: string };
              const link = legacyItem.picLink || { link: legacyItem.link || "" };
              const normalizedLink = String(link.link || "").replace(/\/$/, "");
              const isCouponLink = normalizedLink === "/member/coupon/list";
              // 仅在历史数据没有标题时提供默认值，允许用户后续编辑标题。
              const title = legacyItem.picTitle ?? legacyItem.title ?? (isCouponLink ? "我的优惠券" : "");
              return {
                  ...legacyItem,
                  picTitle: title,
                  picDesc: legacyItem.picDesc || "",
                  picLink: {
                      ...link,
                      label: isCouponLink ? "我的优惠券" : link.label || link.name || title,
                      name: isCouponLink ? "我的优惠券" : link.name || title
                  }
              };
          })
        : pcHeaderDefaults.memberCenterItems;
    return {
        ...pcHeaderDefaults,
        ...incoming,
        showHomeCategory: incoming.showHomeCategory ?? pcHeaderDefaults.showHomeCategory,
        categoryMarginTop: incoming.categoryMarginTop ?? pcHeaderDefaults.categoryMarginTop,
        mainNavPaddingTop: incoming.mainNavPaddingTop ?? pcHeaderDefaults.mainNavPaddingTop,
        mainNavPaddingBottom: incoming.mainNavPaddingBottom ?? pcHeaderDefaults.mainNavPaddingBottom,
        logoPic: { ...pcHeaderDefaults.logoPic, ...(incoming.logoPic ?? {}) },
        cartPic: { ...pcHeaderDefaults.cartPic, ...(incoming.cartPic ?? {}) },
        benefitNoticeGroups: Array.isArray(incoming.benefitNoticeGroups) ? incoming.benefitNoticeGroups : pcHeaderDefaults.benefitNoticeGroups,
        memberCenterItems
    };
}

/**
 * 商品详情装修风格契约升级：历史值按模块类型归一为 A1/A2/A3。
 * 模块 type 是稳定标识，因此即使旧 decorateStyle 缺失或值为旧命名，也以 type 为准。
 */
function normalizeProductDetailStyles(list: DecorateModuleBase[]): DecorateModuleBase[] {
    const styleByType: Record<string, "A1" | "A2" | "A3"> = {
        productDetailA1: "A1",
        productDetailA2: "A2",
        productDetailA3: "A3"
    };
    list.forEach((item) => {
        const style = styleByType[item.type];
        if (style) {
            item.module ??= {};
            item.module.decorateStyle = style;
        }
    });
    return list;
}

export const useDecorateEditorStore = defineStore("decorateEditor", () => {
    // ==================== 状态 ====================

    const decorateId = ref<string | number>("");

    /** 装修类型：1=移动端，2=PC首页，3=PC自定义页 */
    const decorateType = ref<DecorateTypeValue>(1);

    /** 页面类型（移动端）：1=首页，2=会员中心，4=分类中心 */
    const pageType = ref<number>(1);

    /**
     * PC 首页装修版本（仅 decorateType=2 有意义）
     * - new：可视化新套（kebab-case 模块 + PC 头部预览）
     * - old：旧占位套（camelCase 模块，无头部）
     * 新旧两套互斥，不可共存
     */
    const homeStyleVersion = ref<PcHomeVersion>("new");

    const decorateTitle = ref<string>("");

    const pageModule = ref<PageModule>({});

    const moduleList = ref<DecorateModuleBase[]>([]);

    /** 当前选中的模块索引（null 表示未选中） */
    const activeModuleIndex = ref<number | null>(null);

    const editMode = ref<EditMode>(null);

    const hasDraft = ref<boolean>(false);

    const isDirty = ref<boolean>(false);

    // 多语言相关
    const localeId = ref<string | number>(0);
    const parentId = ref<string | number>(0);
    const isDefault = ref<boolean>(true);

    // ==================== 计算属性 ====================

    const activeModule = computed(() => {
        if (activeModuleIndex.value === null) return null;
        return moduleList.value[activeModuleIndex.value] ?? null;
    });

    const activeModuleData = computed(() => {
        return activeModule.value?.module ?? null;
    });

    const moduleCount = computed(() => moduleList.value.length);

    const hasExclusiveModule = computed(() => {
        return moduleList.value.some((m) => isExclusiveModule(m.type));
    });

    const topModuleCount = computed(() => {
        return moduleList.value.filter((m) => isTopModule(m.type)).length;
    });

    const bottomModuleCount = computed(() => {
        return moduleList.value.filter((m) => isBottomModule(m.type)).length;
    });

    /** 画布上「新套」PC 首页模块数量 */
    const pcHomeNewModuleCount = computed(() => {
        return moduleList.value.filter((m) => getPcHomeModuleVersion(m.type, m.url) === "new").length;
    });

    /** 画布上「旧套」PC 首页模块数量 */
    const pcHomeOldModuleCount = computed(() => {
        return moduleList.value.filter((m) => getPcHomeModuleVersion(m.type, m.url) === "old").length;
    });

    /**
     * 加载侧推断 PC 首页版本：优先用已保存的 pageModule.homeStyleVersion，
     * 否则按现有模块归属判定（含新套→新版，含旧套→旧版），空页默认新版
     */
    function inferHomeStyleVersion(list: DecorateModuleBase[], pm: PageModule): PcHomeVersion {
        const saved = (pm as { homeStyleVersion?: unknown })?.homeStyleVersion;
        if (saved === "new" || saved === "old") return saved;
        if (list.some((m) => getPcHomeModuleVersion(m.type, m.url) === "new")) return "new";
        if (list.some((m) => getPcHomeModuleVersion(m.type, m.url) === "old")) return "old";
        return "new";
    }

    // ==================== 模块约束校验 ====================

    /**
     * 检查是否可以添加模块
     * @param targetIndex 目标位置（可选，默认末尾）
     */
    function canAddModule(type: string, targetIndex?: number): ModuleValidationResult {
        const list = moduleList.value;
        const placement = getModulePlacement(type);
        const isExclusive = isExclusiveModule(type);

        if (hasExclusiveModule.value) {
            return { ok: false, reason: "页面已包含独占模块，不能添加其他模块" };
        }

        if (isExclusive && list.length > 0) {
            return { ok: false, reason: "该模块为独占模块，需要清空页面后才能添加" };
        }

        if (placement === "top") {
            const validIndex = topModuleCount.value;
            if (targetIndex !== undefined && targetIndex > validIndex) {
                return { ok: false, reason: "该模块必须置顶，不能添加到此位置" };
            }
        } else if (placement === "bottom") {
            const validIndex = list.length - bottomModuleCount.value;
            if (targetIndex !== undefined && targetIndex < validIndex) {
                return { ok: false, reason: "该模块必须置底，不能添加到此位置" };
            }
        } else {
            if (targetIndex !== undefined && targetIndex < topModuleCount.value) {
                return { ok: false, reason: "不能在置顶模块之上添加其他模块" };
            }
            const bottomStart = list.length - bottomModuleCount.value;
            if (targetIndex !== undefined && targetIndex > bottomStart) {
                return { ok: false, reason: "不能在置底模块之下添加其他模块" };
            }
        }

        return { ok: true };
    }

    /** 检查是否可以移动模块 */
    function canMoveModule(fromIndex: number, toIndex: number): ModuleValidationResult {
        const list = moduleList.value;
        if (fromIndex < 0 || fromIndex >= list.length || toIndex < 0 || toIndex >= list.length) {
            return { ok: false, reason: "无效的位置" };
        }
        if (fromIndex === toIndex) {
            return { ok: true };
        }

        const movingModule = list[fromIndex];
        if (!movingModule) {
            return { ok: false, reason: "未找到要移动的模块" };
        }

        const movingPlacement = getModulePlacement(movingModule.type);
        const topCount = topModuleCount.value;
        const bottomCount = bottomModuleCount.value;
        const bottomStart = list.length - bottomCount;

        if (movingPlacement === "top") {
            if (toIndex >= topCount) {
                return { ok: false, reason: "置顶模块不能移动到非置顶区域" };
            }
        } else if (movingPlacement === "bottom") {
            if (toIndex < bottomStart) {
                return { ok: false, reason: "置底模块不能移动到非置底区域" };
            }
        } else {
            if (toIndex < topCount) {
                return { ok: false, reason: "不能将模块移动到置顶模块之上" };
            }
            if (toIndex >= bottomStart && bottomCount > 0) {
                return { ok: false, reason: "不能将模块移动到置底模块之下" };
            }
        }

        return { ok: true };
    }

    /** 按置顶/置底约束计算有效插入位置 */
    function getValidInsertIndex(type: string, preferredIndex?: number): number {
        const list = moduleList.value;
        const placement = getModulePlacement(type);

        if (placement === "top") {
            return topModuleCount.value;
        } else if (placement === "bottom") {
            return list.length - bottomModuleCount.value;
        } else {
            const minIndex = topModuleCount.value;
            const maxIndex = list.length - bottomModuleCount.value;

            if (preferredIndex === undefined) {
                return maxIndex;
            }

            return Math.max(minIndex, Math.min(preferredIndex, maxIndex));
        }
    }

    // ==================== 初始化 ====================

    /**
     * 初始化装修数据（详情加载 / 从其他页面复制 / 模板导入共用）
     * 内置旧编辑器加载链：url 回填 + 固定模块注入
     */
    function initDecorate(data: {
        decorateId: string | number;
        decorateType: DecorateTypeValue;
        pageType?: number;
        decorateTitle?: string;
        pageModule?: PageModule;
        moduleList?: DecorateModuleBase[];
        hasDraft?: boolean;
        localeId?: string | number;
        parentId?: string | number;
        isDefault?: boolean;
    }) {
        decorateId.value = data.decorateId;
        decorateType.value = data.decorateType;
        pageType.value = data.pageType ?? 1;
        decorateTitle.value = data.decorateTitle ?? "";
        pageModule.value = data.pageModule ?? {};

        // PC 首页：补全 pcHeader 默认值（随首页 pageModule 一起保存/加载）
        if (decorateType.value === DecorateType.PC_HOME) {
            pageModule.value.pcHeader = mergePcHeaderDefaults(pageModule.value.pcHeader);
        }

        const list = normalizeProductDetailStyles(updateModuleListWithUrls(data.moduleList ?? []));
        ensureFixedModules(list, pageType.value);
        moduleList.value = list;

        homeStyleVersion.value = decorateType.value === DecorateType.PC_HOME ? inferHomeStyleVersion(list, pageModule.value) : "new";

        hasDraft.value = data.hasDraft ?? false;
        localeId.value = data.localeId ?? 0;
        parentId.value = data.parentId ?? 0;
        isDefault.value = data.isDefault ?? true;

        activeModuleIndex.value = null;
        editMode.value = null;
        isDirty.value = false;
    }

    /**
     * 从草稿恢复
     * 与旧编辑器一致：此路径只注入固定模块，不回填 url（草稿数据保存时已带 url）
     */
    function restoreFromDraft(draftData: DecorateData) {
        pageModule.value = draftData.pageModule ?? pageModule.value;
        const list = normalizeProductDetailStyles(draftData.moduleList ?? []);
        ensureFixedModules(list, pageType.value);
        moduleList.value = list;
        if (decorateType.value === DecorateType.PC_HOME) {
            pageModule.value.pcHeader = mergePcHeaderDefaults(pageModule.value.pcHeader);
            homeStyleVersion.value = inferHomeStyleVersion(list, pageModule.value);
        }
        hasDraft.value = false;
        activeModuleIndex.value = null;
        editMode.value = null;
    }

    /**
     * 应用模板（模板库选择回调）
     * 与旧编辑器一致：url 回填 + 固定模块注入，pageModule 合并而非替换
     */
    function applyTemplate(json: { moduleList?: DecorateModuleBase[]; pageModule?: PageModule }) {
        const list = normalizeProductDetailStyles(updateModuleListWithUrls(json.moduleList ?? []));
        ensureFixedModules(list, pageType.value);
        moduleList.value = list;
        Object.assign(pageModule.value, json.pageModule ?? {});
        if (decorateType.value === DecorateType.PC_HOME) {
            homeStyleVersion.value = inferHomeStyleVersion(list, pageModule.value);
        }
        isDirty.value = true;
    }

    // ==================== 选择 ====================

    function selectModule(index: number) {
        if (index >= 0 && index < moduleList.value.length) {
            activeModuleIndex.value = index;
            editMode.value = "module";
        }
    }

    function selectPage() {
        activeModuleIndex.value = null;
        editMode.value = "page";
    }

    /** 选中 PC 头部装修（仅 PC 首页，进入头部设置面板） */
    function selectPcHeader() {
        if (decorateType.value !== DecorateType.PC_HOME) return;
        activeModuleIndex.value = null;
        editMode.value = "pcHeader";
        pageModule.value.pcHeader = mergePcHeaderDefaults(pageModule.value.pcHeader);
    }

    function clearSelection() {
        activeModuleIndex.value = null;
        editMode.value = null;
    }

    /**
     * 切换 PC 首页版本（新版/旧版，仅 decorateType=2）
     * @param version 目标版本
     * @param clearOther 是否清空「另一套」模块（新旧不可共存，切换前需调用方确认）
     */
    function setHomeStyleVersion(version: PcHomeVersion, clearOther = false) {
        if (version !== "new" && version !== "old") return;
        if (version === homeStyleVersion.value) return;

        let removed = 0;
        if (clearOther) {
            const removeVersion: PcHomeVersion = version === "new" ? "old" : "new";
            const before = moduleList.value.length;
            moduleList.value = moduleList.value.filter((m) => getPcHomeModuleVersion(m.type, m.url) !== removeVersion);
            removed = before - moduleList.value.length;
        }

        homeStyleVersion.value = version;
        pageModule.value.homeStyleVersion = version;

        // 清空了模块（选中项可能失效）时收起模块设置面板，避免残留无效选中
        if (removed > 0) {
            activeModuleIndex.value = null;
            if (editMode.value === "module") editMode.value = null;
        }
        isDirty.value = true;
    }

    // ==================== 模块操作 ====================

    /**
     * 添加模块
     * @param module 模块数据（含 url，由 createModuleInstance 生成）
     * @param index 插入位置（可选）
     * @param skipValidation 跳过约束校验（内部使用）
     * @returns 成功返回 moduleIndex，失败返回校验结果
     */
    function addModule(module: Omit<DecorateModuleBase, "moduleIndex">, index?: number, skipValidation = false): number | ModuleValidationResult {
        if (!skipValidation) {
            const validation = canAddModule(module.type, index);
            if (!validation.ok) {
                return validation;
            }
        }

        const validIndex = getValidInsertIndex(module.type, index);

        const newModule: DecorateModuleBase = {
            ...module,
            moduleIndex: Date.now(),
            isShow: module.isShow ?? true
        };

        moduleList.value.splice(validIndex, 0, newModule);
        activeModuleIndex.value = validIndex;

        editMode.value = "module";
        isDirty.value = true;

        return newModule.moduleIndex;
    }

    /** 更新指定模块数据（浅合并） */
    function updateModule(index: number, data: Partial<DecorateModuleBase["module"]>) {
        const targetModule = moduleList.value[index];
        if (targetModule) {
            targetModule.module = {
                ...targetModule.module,
                ...data
            };
            isDirty.value = true;
        }
    }

    /** 更新当前激活模块（浅合并） */
    function updateActiveModule(data: Partial<DecorateModuleBase["module"]>) {
        if (activeModuleIndex.value !== null) {
            updateModule(activeModuleIndex.value, data);
        }
    }

    /** 替换当前激活模块的全部数据 */
    function replaceActiveModuleData(data: DecorateModuleBase["module"]) {
        if (activeModuleIndex.value !== null) {
            const targetModule = moduleList.value[activeModuleIndex.value];
            if (targetModule) {
                targetModule.module = data;
                isDirty.value = true;
            }
        }
    }

    /** 局部更新当前激活模块（只更新传入字段，适合单字段频繁更新） */
    function patchActiveModuleData(patch: Record<string, unknown>) {
        if (activeModuleIndex.value !== null) {
            const currentModule = moduleList.value[activeModuleIndex.value];
            if (currentModule) {
                // 数组值浅拷贝出新引用再写入：localData 与 store 的数组常共享同一引用（deepMerge 对数组不拷贝），
                // 若把同引用原样赋回，Preview 的 computed(deepMerge) 不会因数组「内容原地变化」而重算，导致画布不回显。
                const next: Record<string, unknown> = {};
                for (const key in patch) {
                    const value = patch[key];
                    next[key] = Array.isArray(value) ? [...value] : value;
                }
                Object.assign(currentModule.module, next);
                isDirty.value = true;
            }
        }
    }

    /** 更新嵌套字段（如 frame.boxPadding） */
    function patchNestedField(parentKey: string, childKey: string, value: unknown) {
        if (activeModuleIndex.value !== null) {
            const currentModule = moduleList.value[activeModuleIndex.value];
            if (currentModule && currentModule.module) {
                const parent = currentModule.module[parentKey];
                if (parent && typeof parent === "object") {
                    (parent as Record<string, unknown>)[childKey] = value;
                    isDirty.value = true;
                }
            }
        }
    }

    /** 更新页面设置 */
    function updatePageModule(data: Partial<PageModule>) {
        pageModule.value = { ...pageModule.value, ...data };
        isDirty.value = true;
    }

    /** 更新 PC 头部配置（局部合并，随首页 pageModule.pcHeader 保存） */
    function updatePcHeader(data: Partial<PcHeaderConfig>) {
        const current = mergePcHeaderDefaults(pageModule.value.pcHeader);
        pageModule.value = {
            ...pageModule.value,
            pcHeader: { ...current, ...data }
        };
        isDirty.value = true;
    }

    /** 删除模块（固定模块不可删除） */
    function removeModule(index: number) {
        if (index >= 0 && index < moduleList.value.length) {
            if (moduleList.value[index]?.fixed) return;
            moduleList.value.splice(index, 1);

            if (moduleList.value.length === 0) {
                activeModuleIndex.value = null;
                editMode.value = null;
            } else if (activeModuleIndex.value !== null) {
                if (activeModuleIndex.value >= moduleList.value.length) {
                    activeModuleIndex.value = moduleList.value.length - 1;
                } else if (activeModuleIndex.value > index) {
                    activeModuleIndex.value--;
                }
            }

            isDirty.value = true;
        }
    }

    /** 复制模块（固定模块不可复制） */
    function copyModule(index: number) {
        const source = moduleList.value[index];
        if (source && !source.fixed) {
            const copied = cloneDeep(source);
            copied.moduleIndex = Date.now();

            moduleList.value.splice(index + 1, 0, copied);
            activeModuleIndex.value = index + 1;
            isDirty.value = true;
        }
    }

    /**
     * 移动模块
     * @returns 成功返回 true，失败返回校验结果
     */
    function moveModule(fromIndex: number, toIndex: number, skipValidation = false): true | ModuleValidationResult {
        if (fromIndex === toIndex) {
            return true;
        }

        if (!skipValidation) {
            const validation = canMoveModule(fromIndex, toIndex);
            if (!validation.ok) {
                return validation;
            }
        }

        if (fromIndex >= 0 && fromIndex < moduleList.value.length && toIndex >= 0 && toIndex < moduleList.value.length) {
            const [movedModule] = moduleList.value.splice(fromIndex, 1);
            if (movedModule) {
                moduleList.value.splice(toIndex, 0, movedModule);
                activeModuleIndex.value = toIndex;
                isDirty.value = true;
            }
        }
        return true;
    }

    function moveUp(index: number): true | ModuleValidationResult {
        if (index <= 0) {
            return true;
        }
        return moveModule(index, index - 1);
    }

    function moveDown(index: number): true | ModuleValidationResult {
        if (index >= moduleList.value.length - 1) {
            return true;
        }
        return moveModule(index, index + 1);
    }

    /** 切换模块显隐（固定模块不可隐藏） */
    function toggleModuleVisibility(index: number) {
        const targetModule = moduleList.value[index];
        if (targetModule && !targetModule.fixed) {
            targetModule.isShow = !targetModule.isShow;
            isDirty.value = true;
        }
    }

    // ==================== 数据获取 ====================

    /**
     * 获取保存数据（契约出口）
     * 整体透传：不做任何字段剔除/白名单，未知字段原样保留
     */
    function getSaveData(): DecorateData {
        const pm = cloneDeep(pageModule.value);
        // PC 首页：把当前版本写入保存数据（透传新增字段，不破坏旧契约）
        if (decorateType.value === DecorateType.PC_HOME) {
            (pm as { homeStyleVersion?: PcHomeVersion }).homeStyleVersion = homeStyleVersion.value;
            // 头部装修仅新版首页：旧版不携带 pcHeader，避免前台据此误判为新版头部
            if (homeStyleVersion.value !== "new") {
                delete (pm as { pcHeader?: unknown }).pcHeader;
            }
        } else {
            // 移动端 / PC 自定义页不含头部装修配置（契约守护：仅 PC 首页新版有 pcHeader）
            delete (pm as { pcHeader?: unknown }).pcHeader;
        }
        return {
            pageModule: pm,
            moduleList: cloneDeep(moduleList.value)
        };
    }

    /** 获取指定模块数据 */
    function getModuleData<T = Record<string, unknown>>(index: number): T | null {
        const targetModule = moduleList.value[index];
        if (targetModule) {
            return targetModule.module as T;
        }
        return null;
    }

    // ==================== 状态管理 ====================

    function markAsSaved() {
        isDirty.value = false;
        hasDraft.value = false;
    }

    function markHasDraft(value: boolean) {
        hasDraft.value = value;
    }

    function reset() {
        decorateId.value = "";
        decorateType.value = 1;
        pageType.value = 1;
        homeStyleVersion.value = "new";
        decorateTitle.value = "";
        pageModule.value = {};
        moduleList.value = [];
        activeModuleIndex.value = null;
        editMode.value = null;
        hasDraft.value = false;
        isDirty.value = false;
        localeId.value = 0;
        parentId.value = 0;
        isDefault.value = true;
    }

    return {
        // 状态
        decorateId,
        decorateType,
        pageType,
        homeStyleVersion,
        decorateTitle,
        pageModule,
        moduleList,
        activeModuleIndex,
        editMode,
        hasDraft,
        isDirty,
        localeId,
        parentId,
        isDefault,

        // 计算属性
        activeModule,
        activeModuleData,
        moduleCount,
        hasExclusiveModule,
        topModuleCount,
        bottomModuleCount,
        pcHomeNewModuleCount,
        pcHomeOldModuleCount,

        // 约束校验
        canAddModule,
        canMoveModule,
        getValidInsertIndex,

        // 初始化
        initDecorate,
        restoreFromDraft,
        applyTemplate,

        // 选择
        selectModule,
        selectPage,
        selectPcHeader,
        clearSelection,
        setHomeStyleVersion,

        // 模块操作
        addModule,
        updateModule,
        updateActiveModule,
        replaceActiveModuleData,
        patchActiveModuleData,
        patchNestedField,
        updatePageModule,
        updatePcHeader,
        removeModule,
        copyModule,
        moveModule,
        moveUp,
        moveDown,
        toggleModuleVisibility,

        // 数据获取
        getSaveData,
        getModuleData,

        // 状态管理
        markAsSaved,
        markHasDraft,
        reset
    };
});
