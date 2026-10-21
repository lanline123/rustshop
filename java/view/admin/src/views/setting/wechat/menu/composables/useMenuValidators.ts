// 公众号菜单 - 校验工具
// 配套：types/setting/wechat.d.ts、docs/公众号菜单与自动回复配置-接口契约.md §1
// 与后端 WechatMenuServiceImpl 校验保持一致：
//   - 一级名称 ≤ 8 字节（GBK），二级 ≤ 16 字节
//   - rule.type=click → msgType 1|2；文本或图文字段按类型校验（eventKey 可由后端保存时补齐）
//   - rule.type=view → url 必填且 ^https?://
//   - rule.type=miniprogram → appid 必填、pagepath 必填且 ^pages/.+；miniprogramUrl 可选且若填须 http(s)
//
// 前端额外收紧（提交前拦截，不与后端冲突）：
//   - 菜单名称仅允许中文汉字 + 英文字母 + 阿拉伯数字，禁止空格 / 标点 / emoji 等
//   - http(s) URL 必须包含合法主机名结构（至少一个点分段），避免 `https://a` 这类半截地址绕过

import type { WxMenuNode, WxMenuRule, WxMenuTree } from "@/types/setting/wechat.d";

/**
 * 估算字符串的 GBK 字节长度。
 * 简化策略：ASCII（codePoint < 128）按 1 字节，其余按 2 字节。
 * 对中文/全角符号与微信侧统计一致，emoji 等极端字符不在菜单文案合理输入范围。
 */
export function gbkByteLength(str: string): number {
    if (!str) return 0;
    let len = 0;
    for (let i = 0; i < str.length; i++) {
        const code = str.charCodeAt(i);
        len += code < 128 ? 1 : 2;
    }
    return len;
}

/** 一级名称字节上限 */
export const NAME_BYTE_LIMIT_LEVEL_1 = 8;
/** 二级名称字节上限 */
export const NAME_BYTE_LIMIT_LEVEL_2 = 16;
/** 一级菜单数量上限 */
export const ROOT_MAX = 3;
/** 单个一级下二级菜单数量上限 */
export const CHILD_MAX = 5;

// 菜单名称字符白名单：中文汉字（基本区） + 英文大小写字母 + 阿拉伯数字
// 不允许空格、全角/半角标点、emoji、控制字符等任何其它字符
const NAME_CHAR_REG = /^[\u4e00-\u9fa5a-zA-Z0-9]+$/;

/**
 * 校验菜单名称，返回错误消息或 null。
 */
export function validateName(name: string, level: 1 | 2): string | null {
    const trimmed = (name ?? "").trim();
    if (!trimmed) {
        return level === 1 ? "请输入一级菜单名称" : "请输入二级菜单名称";
    }
    if (!NAME_CHAR_REG.test(trimmed)) {
        return "菜单名称仅支持中英文和数字";
    }
    const limit = level === 1 ? NAME_BYTE_LIMIT_LEVEL_1 : NAME_BYTE_LIMIT_LEVEL_2;
    if (gbkByteLength(trimmed) > limit) {
        return level === 1
            ? "菜单名称不超过 4 个汉字或 8 个字母"
            : "菜单名称不超过 8 个汉字或 16 个字母";
    }
    return null;
}

/**
 * http(s) URL 校验正则。
 * 后端仅做 `^https?://` 前缀校验；前端在提交前进一步收紧，要求包含合法主机名结构，
 * 避免用户只写 `https://` 或 `https://a` 等半截地址。规则：
 *  - 协议：http(s)
 *  - 主机：至少一个点分段，各段由字母 / 数字 / 连字符组成，不以连字符开头或结尾
 *  - 可选端口：1-5 位数字
 *  - 可选路径 / 查询 / 锚点：以 /、?、# 开头，禁止空白字符
 */
const HTTP_URL_REG = /^https?:\/\/(?!-)[A-Za-z0-9-]+(?<!-)(\.(?!-)[A-Za-z0-9-]+(?<!-))+(:\d{1,5})?([/?#]\S*)?$/i;
/** 小程序 pagepath 必须以 pages/ 开头 */
const PAGE_PATH_REG = /^pages\/.+/;

/**
 * 单条 rule 校验错误，包含字段定位信息。
 *  - field：用于在表单中高亮对应字段；"type" 表示响应类型本身非法
 */
export interface RuleError {
    field:
        | "type"
        | "msgType"
        | "content"
        | "url"
        | "appid"
        | "pagepath"
        | "miniprogramUrl"
        | "newsTitle"
        | "newsDescription"
        | "newsCoverUrl"
        | "newsUrl";
    message: string;
}

/**
 * 校验单个 rule，返回当前分支的全部字段错误。
 * 用于表单在一次提交时同时展示多个错误提示。
 */
export function validateRuleErrors(rule: WxMenuRule | undefined | null): RuleError[] {
    if (!rule || !rule.type) {
        return [{ field: "type", message: "请选择响应类型" }];
    }
    switch (rule.type) {
        case "click": {
            if (rule.msgType !== 1 && rule.msgType !== 2) {
                return [{ field: "msgType", message: "请选择消息类型（文本或图文）" }];
            }
            if (rule.msgType === 1) {
                const text = (rule.content ?? "").trim();
                const errors: RuleError[] = [];
                if (!text) errors.push({ field: "content", message: "请输入要发送的文本内容" });
                if (text.length > 600) errors.push({ field: "content", message: "文本内容不可超过 600 字符" });
                return errors;
            }
            const title = (rule.newsTitle ?? "").trim();
            const desc = (rule.newsDescription ?? "").trim();
            const cover = (rule.newsCoverUrl ?? "").trim();
            const link = (rule.newsUrl ?? "").trim();
            const errors: RuleError[] = [];
            if (!title) errors.push({ field: "newsTitle", message: "请输入图文标题" });
            if (title.length > 64) errors.push({ field: "newsTitle", message: "图文标题不可超过 64 字符" });
            if (!desc) errors.push({ field: "newsDescription", message: "请输入图文摘要" });
            if (desc.length > 120) errors.push({ field: "newsDescription", message: "图文摘要不可超过 120 字符" });
            if (!cover) errors.push({ field: "newsCoverUrl", message: "请上传封面图" });
            if (!link) errors.push({ field: "newsUrl", message: "请输入跳转链接" });
            if (link && !HTTP_URL_REG.test(link)) errors.push({ field: "newsUrl", message: "跳转链接需以 http:// 或 https:// 开头" });
            return errors;
        }
        case "view": {
            const url = (rule.url ?? "").trim();
            const errors: RuleError[] = [];
            if (!url) errors.push({ field: "url", message: "请输入跳转链接" });
            if (url && !HTTP_URL_REG.test(url)) errors.push({ field: "url", message: "跳转链接需以 http:// 或 https:// 开头" });
            if (url.length > 1024) errors.push({ field: "url", message: "跳转链接长度不可超过 1024 字符" });
            return errors;
        }
        case "miniprogram": {
            const appid = (rule.appid ?? "").trim();
            const pagepath = (rule.pagepath ?? "").trim();
            const fallback = (rule.miniprogramUrl ?? "").trim();
            const errors: RuleError[] = [];
            if (!appid) errors.push({ field: "appid", message: "请输入小程序 AppId" });
            if (!pagepath) {
                errors.push({ field: "pagepath", message: "请输入小程序路径" });
            } else if (!PAGE_PATH_REG.test(pagepath)) {
                errors.push({ field: "pagepath", message: "小程序路径必须以 pages/ 开头" });
            }
            if (fallback && !HTTP_URL_REG.test(fallback)) {
                errors.push({ field: "miniprogramUrl", message: "备用网页链接需以 http:// 或 https:// 开头" });
            }
            return errors;
        }
        default:
            return [{ field: "type", message: "响应类型非法" }];
    }
}

/**
 * 将 click 规则规范为可编辑的「发送消息」结构（兼容旧数据）。
 * 不再生成/回填 eventKey：由后端 saveAndPublish 时统一分配并在 menu_json 中保留。
 */
export function migrateMenuClickRule(rule: WxMenuRule | undefined): WxMenuRule | undefined {
    if (!rule || rule.type !== "click") {
        return rule;
    }
    const out: WxMenuRule = { ...rule };
    if (out.msgType !== 1 && out.msgType !== 2) {
        out.msgType = 1;
    }
    if (out.msgType === 1) {
        out.content = out.content ?? "";
        delete out.newsTitle;
        delete out.newsCoverUrl;
        delete out.newsDescription;
        delete out.newsUrl;
    } else {
        out.newsTitle = out.newsTitle ?? "";
        out.newsDescription = out.newsDescription ?? "";
        out.newsCoverUrl = out.newsCoverUrl ?? "";
        out.newsUrl = out.newsUrl ?? "";
        delete out.content;
    }
    return out;
}

/**
 * 校验单个 rule，返回带字段定位的错误对象或 null。
 */
export function validateRule(rule: WxMenuRule | undefined | null): RuleError | null {
    return validateRuleErrors(rule)[0] ?? null;
}

/**
 * 全树校验返回的单条错误。
 *  - nodeId 为 null 表示树级错误（无可定位节点，如空树、超限）
 *  - nodeId 非空时与 tree 中某个一级或二级节点的 id 对应，可用于联动选中 / 高亮
 */
export interface MenuTreeError {
    nodeId: string | null;
    message: string;
}

/**
 * 全树校验，返回错误列表（可能为空）。
 * 校验项：
 *  - 至少 1 个一级菜单
 *  - 一级 ≤ 3、二级 ≤ 5
 *  - 每个节点名称按 level 校验
 *  - 叶子节点（无 children）必须配置且通过 rule 校验
 *  - 含 children 的一级节点 rule 可不填（点击仅展开子菜单）
 */
export function validateTree(tree: WxMenuTree | undefined | null): MenuTreeError[] {
    const errors: MenuTreeError[] = [];
    const buttons = tree?.buttons ?? [];
    if (buttons.length === 0) {
        errors.push({ nodeId: null, message: "请至少添加 1 个一级菜单" });
        return errors;
    }
    if (buttons.length > ROOT_MAX) {
        errors.push({ nodeId: null, message: `一级菜单最多 ${ROOT_MAX} 个` });
    }

    buttons.forEach((root, i) => {
        const rootLabel = root.name?.trim() || `第 ${i + 1} 个一级菜单`;

        const nameErr = validateName(root.name ?? "", 1);
        if (nameErr) errors.push({ nodeId: root.id ?? null, message: `「${rootLabel}」${nameErr}` });

        const hasChildren = Array.isArray(root.children) && root.children.length > 0;
        if (hasChildren) {
            const children = root.children as WxMenuNode[];
            if (children.length > CHILD_MAX) {
                errors.push({ nodeId: root.id ?? null, message: `「${rootLabel}」二级菜单最多 ${CHILD_MAX} 个` });
            }
            children.forEach((child, j) => {
                const childLabel = child.name?.trim() || `第 ${j + 1} 个二级菜单`;
                const cNameErr = validateName(child.name ?? "", 2);
                if (cNameErr) {
                    errors.push({ nodeId: child.id ?? null, message: `「${rootLabel}」-「${childLabel}」${cNameErr}` });
                }

                const ruleErr = validateRule(child.rule);
                if (ruleErr) {
                    errors.push({ nodeId: child.id ?? null, message: `「${rootLabel}」-「${childLabel}」${ruleErr.message}` });
                }
            });
        } else {
            const ruleErr = validateRule(root.rule);
            if (ruleErr) errors.push({ nodeId: root.id ?? null, message: `「${rootLabel}」${ruleErr.message}` });
        }
    });

    return errors;
}

/**
 * 生成稳定的临时 id；提交前 toSaveDTO 会移除 tmp_ 前缀的 id 让后端生成。
 */
export function genNodeId(): string {
    const c = (typeof globalThis !== "undefined" ? (globalThis as any).crypto : undefined) as
        | { randomUUID?: () => string }
        | undefined;
    if (c?.randomUUID) {
        return `tmp_${c.randomUUID()}`;
    }
    return `tmp_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`;
}

/**
 * 判断 id 是否前端临时生成，提交时需要清除以便后端重新分配。
 */
export function isTempId(id: string | undefined | null): boolean {
    return !!id && id.startsWith("tmp_");
}
