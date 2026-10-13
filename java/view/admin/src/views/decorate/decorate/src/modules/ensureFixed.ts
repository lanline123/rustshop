/**
 * 装修模块「固定模块」幂等注入工具
 *
 * 通过 fixed 标志位驱动编辑器对该类模块的特殊操作约束：
 * - 不可删除 / 不可复制 / 不可隐藏
 * - 只可上下移动
 *
 * 当 moduleList 中缺少声明的固定模块时，按声明的 anchorAfter 优先级
 * 在「最后一个匹配项」之后插入；若所有 anchor 均不匹配，则追加到末尾。
 */

import { isB2b, isStore } from "@/utils/version";
import i18n from "@/i18n";

type FixedModuleSpec = {
    /** 构造一条新的 fixed module，moduleIndex 用调用时间戳 */
    make: () => any;
    /** 判定 moduleList 中是否已存在该 fixed module（兼容 kebab / camel 两种命名） */
    matches: (m: any) => boolean;
    /** 锚点优先级数组：按顺序定位最后一个匹配项，在它之后插入 */
    anchorAfter: Array<(m: any) => boolean>;
    /** 展示条件：返回 false 时不注入，并清理已存在的同类模块 */
    enabled?: () => boolean;
};

const isType = (...types: string[]) => (m: any) => !!m && types.includes(m.type);

export const FIXED_MODULES_BY_PAGE_TYPE: Record<number, FixedModuleSpec[]> = {
    // 会员中心
    2: [
        {
            make: () => ({
                type: "fixedFunctionalArea",
                url: "mobileMember",
                label: i18n.global.t("固定功能区"),
                fixed: true,
                isShow: true,
                moduleIndex: Date.now(),
                module: {}
            }),
            matches: isType("fixedFunctionalArea", "fixed-functional-area"),
            anchorAfter: [isType("myWallet", "my-wallet"), isType("orderEntry", "order-entry", "orderEntryV2", "order-entry-v2")],
            enabled: () => isB2b() || isStore()
        }
    ]
};

/**
 * 在 list 中找到最后一个匹配 matcher 的下标
 */
const findLastIndex = (list: any[], matcher: (m: any) => boolean): number => {
    for (let i = list.length - 1; i >= 0; i--) {
        if (matcher(list[i])) return i;
    }
    return -1;
};

/**
 * 按 anchorAfter 优先级，返回应插入的下标（splice 在该下标处插入）
 * 找不到任何 anchor 时返回 list.length（追加到末尾）
 */
const findInsertIndex = (list: any[], anchorAfter: Array<(m: any) => boolean>): number => {
    for (const matcher of anchorAfter) {
        const idx = findLastIndex(list, matcher);
        if (idx !== -1) return idx + 1;
    }
    return list.length;
};

/**
 * 幂等保证：
 * - moduleList 中已有该 fixed module → 仅回写 fixed: true 与标准 type / url，不重复插入
 * - moduleList 中没有 → 按 anchorAfter 优先级插入
 */
export function ensureFixedModules(list: any[], pageType: number): any[] {
    if (!Array.isArray(list)) return list;
    const specs = FIXED_MODULES_BY_PAGE_TYPE[pageType] ?? [];
    specs.forEach((spec) => {
        if (spec.enabled && !spec.enabled()) {
            for (let i = list.length - 1; i >= 0; i--) {
                if (spec.matches(list[i])) list.splice(i, 1);
            }
            return;
        }
        const existing = list.find(spec.matches);
        if (existing) {
            const proto = spec.make();
            if (existing.type !== proto.type) existing.type = proto.type;
            if (!existing.url) existing.url = proto.url;
            if (existing.fixed !== true) existing.fixed = true;
            return;
        }
        const insertAt = findInsertIndex(list, spec.anchorAfter);
        list.splice(insertAt, 0, spec.make());
    });
    return list;
}
