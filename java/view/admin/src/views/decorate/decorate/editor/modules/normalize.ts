/**
 * 加载侧归一化工具（契约行为，从旧 Index.vue 原样迁移）
 *
 * 调用时机与旧编辑器严格一致：
 * - 详情加载 / 从其他页面复制 / 模板导入：updateModuleListWithUrls + ensureFixedModules
 * - 草稿还原：仅 ensureFixedModules（旧编辑器不在此路径回填 url）
 */

import type { DecorateModuleBase } from "@/types/decorate/editorModule";
import { ModuleTypeList } from "@/views/decorate/decorate/src/modules/editIndex";

// 桶迁移兼容：folder 已迁移到新桶的 type，强制把历史草稿烧录的旧 url 纠正过来（加载时自愈）
const RELOCATED_TYPE_URLS: Record<string, string> = {
    newcomerCombo: "mobilePro",
    limitedSpecial: "mobilePro"
};

const typeToUrlMap: Record<string, string> = {};
ModuleTypeList.forEach(({ type, url }) => {
    typeToUrlMap[type] = url;
});

/**
 * 为已保存数据回填模块 url（原地修改并返回，与旧实现一致）
 * ModuleTypeList 非全量：不在其中且自身无 url 的模块保持原样
 */
export function updateModuleListWithUrls(moduleList: DecorateModuleBase[] = []): DecorateModuleBase[] {
    moduleList.forEach((module) => {
        if (RELOCATED_TYPE_URLS[module.type]) {
            module.url = RELOCATED_TYPE_URLS[module.type];
        } else if (!module.url) {
            const correspondingUrl = typeToUrlMap[module.type];
            if (correspondingUrl) {
                module.url = correspondingUrl;
            }
        }
    });
    return moduleList;
}

// 固定模块注入与旧编辑器共用同一实现，避免迁移期双源漂移
export { ensureFixedModules, FIXED_MODULES_BY_PAGE_TYPE } from "@/views/decorate/decorate/src/modules/ensureFixed";
