/**
 * 装修编辑器（新架构）基础类型
 *
 * 契约约束：保存 JSON 与旧编辑器逐字段一致（见开发文档第六章）。
 * 因此模块与页面设置均带索引签名——编辑器不认识的字段必须原样透传，
 * 不得收窄为白名单结构。
 */

/** 装修模块基础结构 */
export interface DecorateModuleBase {
    /** 模块唯一标识（Date.now() 时间戳） */
    moduleIndex: number;
    /** 模块类型（camelCase，契约冻结） */
    type: string;
    /** 模块所在分类目录，动态加载路径的一部分，随保存持久化（契约字段） */
    url?: string;
    /** 显示名称 */
    label?: string;
    /** 是否显示 */
    isShow?: boolean;
    /** 固定模块标记（不可删除/复制/隐藏，见 ensureFixed） */
    fixed?: boolean;
    /** 模块数据 */
    module: Record<string, any>;
    /** 未知字段透传 */
    [key: string]: any;
}

/** 页面设置（透传结构） */
export interface PageModule {
    backgroundColor?: string;
    backgroundImage?: string | Record<string, any> | null;
    backgroundRepeat?: string | number;
    backgroundSize?: string | number;
    style?: number;
    /** 页面标题沉浸式（仅会员中心页使用） */
    immersive?: boolean;
    [key: string]: any;
}

/** 装修数据结构（getSaveData 输出 / 接口 data 字段） */
export interface DecorateData {
    pageModule: PageModule;
    moduleList: DecorateModuleBase[];
}
