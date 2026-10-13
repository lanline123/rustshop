// 公众号菜单与自动回复配置 - 类型定义
// 配套文档：docs/公众号菜单与自动回复配置-接口契约.md v1.0

// ---- 通用 ----

/** MyBatis-Plus 分页结果包装 */
export interface PageResult<T> {
    records: T[];
    total: number;
    size: number;
    current: number;
    pages: number;
}

/** 自动回复 / 关键词回复共用的回复内容载荷（契约 §0.4） */
export interface ReplyPayload {
    /** 1=文本 2=图文 */
    msgType: 1 | 2;
    /** msgType=1 时必填；文本内容，≤600 字符，支持变量占位符 */
    content?: string;
    /** msgType=2 时必填；图文标题，≤64 字符 */
    newsTitle?: string;
    /** msgType=2 时必填；HTTPS 可访问的图片 URL（来自 FormAddGallery） */
    newsCoverUrl?: string;
    /** msgType=2 时必填；图文摘要，≤120 字符 */
    newsDescription?: string;
    /** msgType=2 时必填；图文卡片点击跳转的 http/https URL */
    newsUrl?: string;
}

// ---- M1 公众号菜单（契约 §1） ----

/** 菜单点击规则（叶子节点必填） */
export interface WxMenuRule {
    type: "click" | "view" | "miniprogram";
    /**
     * type=click：微信回调 EventKey（与发布到微信的 click.key 一致）。
     * 编辑态不可见、不可填；由后端 saveAndPublish 统一分配并在 menu_json 内保留，≤128。
     */
    eventKey?: string;
    /** type=click：1=文本 2=图文 */
    msgType?: 1 | 2;
    /** type=click 且 msgType=1 */
    content?: string;
    /** type=click 且 msgType=2 */
    newsTitle?: string;
    newsCoverUrl?: string;
    newsDescription?: string;
    newsUrl?: string;
    /** type=view 时必填；http/https 合法 URL */
    url?: string;
    /** type=miniprogram 时必填；小程序 appid */
    appid?: string;
    /** type=miniprogram 时必填；形如 pages/xxx/xxx */
    pagepath?: string;
    /** type=miniprogram 时的备用网页（老客户端 fallback） */
    miniprogramUrl?: string;
}

/** 菜单节点（一级 / 二级共用） */
export interface WxMenuNode {
    /** 前端生成的临时 uuid，落库保留用于编辑态稳定 key */
    id: string;
    /** 菜单名称；一级 ≤4 汉字(8字节)、二级 ≤8 汉字(16字节) */
    name: string;
    /** 同级排序，0 起递增 */
    sortOrder: number;
    /** 二级菜单，长度 ≤ 5；存在时忽略 rule 字段 */
    children?: WxMenuNode[];
    /** 叶子菜单必填 */
    rule?: WxMenuRule;
}

/** 菜单完整结构（落库存储格式 wechat_menu.menu_json） */
export interface WxMenuTree {
    /** 一级菜单，长度 ≤ 3 */
    buttons: WxMenuNode[];
}

/** GET /wechat/menu 响应 */
export interface WxMenuVO {
    /** 单例；空表示从未保存过 */
    id: number | null;
    /** 若从未保存，返回 { buttons: [] } */
    tree: WxMenuTree;
    /** "YYYY-MM-DD HH:mm:ss" */
    publishedAt: string | null;
    updatedAt: string | null;
}

/** POST /wechat/menu/saveAndPublish 入参 */
export interface SaveAndPublishDTO {
    tree: WxMenuTree;
}

/** POST /wechat/menu/saveAndPublish 响应 */
export interface SaveAndPublishVO {
    /**
     * 微信推送是否成功：
     *  - true：本次提交的新菜单 JSON 已同步落库；
     *  - false：仅发布失败，DB 保留旧菜单以便与微信侧一致（CLICK 回调继续走旧 key）。
     */
    published: boolean;
    /** published=false 时的拼接文案："发布失败（{errcode}）：{errmsg}" */
    errMsg: string | null;
    publishedAt: string | null;
}

// ---- M2 关键字回复（契约 §2） ----

/** 关键字匹配项 */
export interface KeywordRuleKeyword {
    /** 关键字文本，≤30 */
    keyword: string;
    /** 1=全匹配 2=半匹配 */
    matchType: 1 | 2;
}

/** 规则列表 VO（列表页使用，不展开关键字 / 回复明细） */
export interface KeywordRuleVO {
    id: number;
    ruleName: string;
    /** 关键字数量 */
    keywordCount: number;
    /** 回复内容数量 */
    replyCount: number;
    /** 关键字列表（按 sort_order 升序返回，由前端自行拼接展示） */
    keywords: string[];
    /** 文本类型回复数量（msgType=1） */
    textReplyCount: number;
    /** 图文类型回复数量（msgType=2） */
    newsReplyCount: number;
    /** 1=随机回复一条 2=回复全部 */
    replyMode: 1 | 2;
    /** 0=禁用 1=启用 */
    enabled: 0 | 1;
    /** "YYYY-MM-DD HH:mm:ss" */
    createdAt: string;
    updatedAt: string;
}

/** 规则详情 VO（编辑页使用，展开全部子项） */
export interface KeywordRuleDetailVO {
    id: number;
    ruleName: string;
    /** 1=随机回复一条 2=回复全部 */
    replyMode: 1 | 2;
    /** 0=禁用 1=启用 */
    enabled: 0 | 1;
    /** 已按 sortOrder 升序 */
    keywords: KeywordRuleKeyword[];
    /** 已按 sortOrder 升序 */
    replies: ReplyPayload[];
    createdAt: string;
    updatedAt: string;
}

/** GET /wechat/keywordRule/list Query */
export interface KeywordRuleListDTO {
    /** 1-based */
    page: number;
    /** 默认 15 */
    size: number;
    sortField?: string;
    sortOrder?: string;
    /** 模糊搜索：规则名 */
    ruleName?: string;
    /** 下拉筛选 */
    enabled?: 0 | 1;
}

/** POST /wechat/keywordRule/save 入参（新建 / 更新共用；id 存在即更新） */
export interface KeywordRuleSaveDTO {
    /** 为空表示新建，非空表示更新 */
    id?: number;
    ruleName: string;
    /** 1=随机回复一条 2=回复全部 */
    replyMode: 1 | 2;
    /** 0=禁用 1=启用 */
    enabled: 0 | 1;
    keywords: KeywordRuleKeyword[];
    replies: ReplyPayload[];
}

/** POST /wechat/keywordRule/save 响应 */
export interface KeywordRuleSaveVO {
    id: number;
}

// ---- 预览 ----

/** 预览气泡消息（用于编辑器右侧预览面板） */
export interface PreviewMessage {
    /** user=用户消息（左侧）、mp=公众号消息（右侧） */
    role: "user" | "mp";
    payload: ReplyPayload;
}
