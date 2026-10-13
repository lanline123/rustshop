/**
 * 装修模块注册表（移植自 Vort-Admin module-registry，按 Tigshop 契约适配）
 *
 * 与 Vort 的差异：
 * - type 保留 camelCase（契约冻结，见开发文档第六章第 1 条）
 * - 新增必填 url 字段：模块所在分类目录，随模块实例写入并保存（契约字段）
 * - requiredFeature 运行时检查改为 enabled 谓词（承接 isStore/isB2b 等版本判断）
 *
 * 只包含注册表基础能力，具体模块的注册在各自 index.ts 中完成。
 */

import type { Component } from "vue";
import type { DecorateModuleBase } from "@/types/decorate/editorModule";
import { DecorateType, type DecorateTypeValue } from "./constants";

/** 模块分类目录（= 旧架构 url 六类 + pcCustomPage） */
export type ModuleUrl =
    | "mobile"
    | "mobilePro"
    | "mobilePromotion"
    | "mobileMember"
    | "mobileCateCenter"
    | "mobileProductDetail"
    | "mobileLogin"
    | "mobileSearch"
    | "pc"
    | "pcCustomPage"
    | "pcSearch"
    | "pcLogin";

/**
 * 适用平台
 * - mobile: 移动端（decorateType=1）
 * - pc-home: PC 首页（decorateType=2）
 * - pc-custom: PC 自定义页（decorateType=3）
 */
export type ModulePlatform = "mobile" | "mobile-login" | "mobile-search" | "pc-home" | "pc-custom" | "pc-search" | "pc-login";

/**
 * 工具栏分组（移动端生效，对应旧 ToolBar 的 collapse 分区）
 * - basic：基础模块（mobile）
 * - other：其它模块（mobilePromotion，旧"其它模块"分区）
 * - live：直播模块（live/videoNumber，需显式指定）
 * - advanced：高级模块（mobilePro，旧独立页签）
 * - member / cateCenter：会员页 / 分类中心页
 * - pcNew：PC 首页可视化模块（新套，需显式指定）
 * 未指定时按 url 自动映射，mobilePromotion 默认归 other
 */
export type ToolbarGroup = "basic" | "other" | "live" | "advanced" | "member" | "cateCenter" | "productDetail" | "pc" | "pcNew" | "searchResult" | "loginConfig";

/** 模块位置约束 */
export type ModulePlacement = "top" | "bottom";

/** 模块定义 */
export interface ModuleDefinition {
    /** 模块类型标识（唯一，camelCase） */
    type: string;
    /** 显示名称 */
    label: string;
    /** 工具栏图标（图片地址或 icon class，与旧 ToolBar 项一致） */
    icon?: string;
    /** 模块分类目录（契约字段，写入模块实例并随保存持久化） */
    url: ModuleUrl;
    /** 适用平台 */
    platform: ModulePlatform;
    /** 预览组件（懒加载） */
    preview: () => Promise<{ default: Component }>;
    /** 设置组件（懒加载） */
    settings: () => Promise<{ default: Component }>;
    /** 默认值工厂函数（必须与旧 defaultXxx 常量逐字段一致，契约） */
    getDefaultValue: () => object;
    /** 是否在工具栏显示 */
    visible?: boolean;
    /** 排序权重（越小越靠前） */
    order?: number;
    /** 工具栏分组（未指定时按 url 自动映射） */
    toolbarGroup?: ToolbarGroup;
    /**
     * 运行时展示条件（承接旧 ToolBar 的 isStore()/isB2b() 等版本判断）
     * 返回 false 时不在工具栏显示
     */
    enabled?: () => boolean;

    // ==================== 模块约束配置 ====================
    /** 位置约束："top" 必须位于列表第一位；"bottom" 必须位于最后一位 */
    placement?: ModulePlacement;
    /** 独占模式：添加后不允许再添加其他模块 */
    exclusive?: boolean;
    /** 店铺端专属：仅 adminType=shop 显示；未设置则仅非店铺端显示 */
    shopOnly?: boolean;
}

// 注册键为 type@url：pc 与 pcCustomPage 存在同 type 不同实现的模块
// （pcImageAd/pcCoupon/pcSeckill/pcGroupon），type 是冻结契约不可改名，靠 url 区分
const moduleRegistry = new Map<string, ModuleDefinition>();

const keyOf = (type: string, url: string) => `${type}@${url}`;

/** 注册模块 */
export function registerModule(definition: ModuleDefinition) {
    const key = keyOf(definition.type, definition.url);
    if (moduleRegistry.has(key)) {
        console.warn(`[ModuleRegistry] 模块 "${key}" 已存在，将被覆盖`);
    }
    moduleRegistry.set(key, {
        visible: true,
        order: 100,
        ...definition
    });
}

/**
 * 获取模块定义
 * @param url 不传时返回第一个 type 匹配项（同 type 跨平台模块务必传 url）
 */
export function getModuleDefinition(type: string, url?: string): ModuleDefinition | undefined {
    if (url) {
        return moduleRegistry.get(keyOf(type, url));
    }
    for (const def of moduleRegistry.values()) {
        if (def.type === type) return def;
    }
    return undefined;
}

/** 获取所有模块定义（按 order 升序） */
export function getAllModules(): ModuleDefinition[] {
    return Array.from(moduleRegistry.values()).sort((a, b) => (a.order ?? 100) - (b.order ?? 100));
}

/**
 * 创建新模块实例（moduleIndex 由 store.addModule 写入）
 * url 必须写入实例：动态加载路径的一部分，且随保存 JSON 持久化（契约）
 */
export function createModuleInstance(type: string, url?: string): Omit<DecorateModuleBase, "moduleIndex"> | null {
    const definition = getModuleDefinition(type, url);
    if (!definition) {
        console.error(`[ModuleRegistry] 未找到模块类型: ${type}`);
        return null;
    }

    return {
        type: definition.type,
        url: definition.url,
        label: definition.label,
        isShow: true,
        module: definition.getDefaultValue() as Record<string, any>
    };
}

/** 获取预览组件 */
export async function getPreviewComponent(type: string, url?: string): Promise<Component | null> {
    const definition = getModuleDefinition(type, url);
    if (!definition) return null;

    try {
        const module = await definition.preview();
        return module.default;
    } catch (error) {
        console.error(`[ModuleRegistry] 加载预览组件失败: ${type}`, error);
        return null;
    }
}

/** 获取设置组件 */
export async function getSettingsComponent(type: string, url?: string): Promise<Component | null> {
    const definition = getModuleDefinition(type, url);
    if (!definition) return null;

    try {
        const module = await definition.settings();
        return module.default;
    } catch (error) {
        console.error(`[ModuleRegistry] 加载设置组件失败: ${type}`, error);
        return null;
    }
}

// ==================== 上下文过滤（统一入口）====================

/**
 * 页面上下文规则
 * key: `${decorateType}-${pageType}`
 * 对应旧三套 ToolBar 的硬编码面板（toolList/memberList/cateCenterList + PC 两套）
 */
interface PageContextRule {
    /** 允许的工具栏分组白名单 */
    allowedGroups: ToolbarGroup[];
    /** 平台约束 */
    allowedPlatforms: ModulePlatform[];
}

const PAGE_CONTEXT_RULES: Record<string, PageContextRule> = {
    // 移动端首页：基础 + 其它 + 直播 + 高级（对齐旧 PAGE_ALLOWED_GROUPS[1]）
    "1-1": {
        allowedGroups: ["basic", "other", "live", "advanced"],
        allowedPlatforms: ["mobile"]
    },
    // 移动端会员中心：仅会员模块
    "1-2": {
        allowedGroups: ["member"],
        allowedPlatforms: ["mobile"]
    },
    // 移动端活动页：基础 + 其它 + 直播（无高级，对齐旧 PAGE_ALLOWED_GROUPS[3]）
    "1-3": {
        allowedGroups: ["basic", "other", "live"],
        allowedPlatforms: ["mobile"]
    },
    // 移动端分类中心：仅分类中心模块
    "1-4": {
        allowedGroups: ["cateCenter"],
        allowedPlatforms: ["mobile"]
    },
    // 移动端商品详情：仅商品详情模块（A1/A2/A3 互斥）
    "1-8": {
        allowedGroups: ["productDetail"],
        allowedPlatforms: ["mobile"]
    },
    // PC 搜索页：仅搜索结果模块
    "2-6": {
        allowedGroups: ["searchResult"],
        allowedPlatforms: ["pc-search"]
    },
    // PC 登录页：仅登录页配置模块
    "2-5": {
        allowedGroups: ["loginConfig"],
        allowedPlatforms: ["pc-login"]
    },
    // 移动端登录页：仅移动端登录页配置模块（与 PC 登录页复用 loginConfig 分组，靠 platform 隔离）
    "1-5": {
        allowedGroups: ["loginConfig"],
        allowedPlatforms: ["mobile-login"]
    },
    // 移动端搜索页：仅搜索结果模块（与 PC 搜索页复用 searchResult 分组，靠 platform 隔离）
    "1-6": {
        allowedGroups: ["searchResult"],
        allowedPlatforms: ["mobile-search"]
    }
};

/** PC 首页版本：new=可视化新套，old=旧占位套（两套互斥，由 store.homeStyleVersion 决定） */
export type PcHomeVersion = "new" | "old";

/** PC 端按 decorateType 区分平台，不走 pageType 分组 */
const PC_RULES: Record<number, PageContextRule> = {
    [DecorateType.PC_HOME]: {
        // pcNew = 可视化新模块（kebab-case），pc = 旧占位模块；实际展示哪一套由 homeVersion 收窄
        allowedGroups: ["pcNew", "pc"],
        allowedPlatforms: ["pc-home"]
    },
    [DecorateType.PC_CUSTOM]: {
        allowedGroups: ["pc"],
        allowedPlatforms: ["pc-custom"]
    }
};

const DEFAULT_RULE: PageContextRule = {
    allowedGroups: ["basic", "other"],
    allowedPlatforms: ["mobile"]
};

function resolveContextRule(decorateType: DecorateTypeValue, pageType: number): PageContextRule {
    const pageRule = PAGE_CONTEXT_RULES[`${decorateType}-${pageType}`];
    if (pageRule) return pageRule;
    if (decorateType !== DecorateType.MOBILE) {
        return PC_RULES[decorateType] ?? DEFAULT_RULE;
    }
    return DEFAULT_RULE;
}

/** 获取模块的工具栏分组（优先 toolbarGroup，否则按 url 映射） */
export function getModuleToolbarGroup(m: ModuleDefinition): ToolbarGroup {
    if (m.toolbarGroup) return m.toolbarGroup;
    switch (m.url) {
        case "mobile":
            return "basic";
        case "mobilePromotion":
            return "other";
        case "mobilePro":
            return "advanced";
        case "mobileMember":
            return "member";
        case "mobileCateCenter":
            return "cateCenter";
        case "mobileProductDetail":
            return "productDetail";
        case "mobileLogin":
            return "loginConfig";
        case "mobileSearch":
            return "searchResult";
        case "pcSearch":
            return "searchResult";
        case "pcLogin":
            return "loginConfig";
        default:
            return "pc";
    }
}

/**
 * 判断 PC 首页模块属于新套（可视化）还是旧套（占位）
 * 依据注册定义的 toolbarGroup：pcNew=新套，pc=旧套；非 pc-home 模块返回 null
 */
export function getPcHomeModuleVersion(type: string, url?: string): PcHomeVersion | null {
    const def = getModuleDefinition(type, url);
    if (!def || def.platform !== "pc-home") return null;
    return getModuleToolbarGroup(def) === "pcNew" ? "new" : "old";
}

/**
 * 按上下文获取模块列表（统一入口，取代旧三套 ToolBar 的硬编码面板）
 * @param decorateType 1=移动端，2=PC首页，3=PC自定义页
 * @param pageType 移动端页面类型：1=首页，2=会员中心，4=分类中心（PC 端忽略）
 * @param options.isShop 是否店铺端（adminType=shop）
 * @param options.homeVersion PC 首页版本：new 仅展示 pcNew 套，old 仅展示 pc 套；不传时不收窄（两套都给）
 */
export function getModulesByContext(
    decorateType: DecorateTypeValue,
    pageType: number,
    options?: { isShop?: boolean; homeVersion?: PcHomeVersion }
): ModuleDefinition[] {
    const rule = resolveContextRule(decorateType, pageType);
    const isShop = options?.isShop ?? false;

    // PC 首页：按版本把 allowedGroups 收窄到对应单套，实现新旧不共存（搜索页等专属 pageType 不参与）
    let allowedGroups = rule.allowedGroups;
    if (decorateType === DecorateType.PC_HOME && pageType === 1 && options?.homeVersion) {
        allowedGroups = options.homeVersion === "new" ? (["pcNew"] as ToolbarGroup[]) : (["pc"] as ToolbarGroup[]);
    }

    return getAllModules().filter((m) => {
        if (m.visible === false) return false;
        if (m.enabled && !m.enabled()) return false;
        if (m.shopOnly && !isShop) return false;
        if (!rule.allowedPlatforms.includes(m.platform)) return false;
        const group = getModuleToolbarGroup(m);
        if (!allowedGroups.includes(group)) return false;
        return true;
    });
}

/** 获取当前上下文可用的工具栏分组（ModuleToolbar 决定显示哪些分组标签） */
export function getAvailableToolbarGroups(decorateType: DecorateTypeValue, pageType: number): ToolbarGroup[] {
    return resolveContextRule(decorateType, pageType).allowedGroups;
}

// ==================== 模块约束查询 ====================

/** 获取模块的位置约束 */
export function getModulePlacement(type: string): ModulePlacement | undefined {
    return getModuleDefinition(type)?.placement;
}

/** 是否独占模块 */
export function isExclusiveModule(type: string): boolean {
    return getModuleDefinition(type)?.exclusive === true;
}

/** 是否置顶模块 */
export function isTopModule(type: string): boolean {
    return getModulePlacement(type) === "top";
}

/** 是否置底模块 */
export function isBottomModule(type: string): boolean {
    return getModulePlacement(type) === "bottom";
}
