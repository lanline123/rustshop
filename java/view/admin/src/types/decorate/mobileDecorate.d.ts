// 列表查询时筛选参数类型
export interface FilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    parentId?: number;
    keyword?: string;
    decorateType?: number | string;
    pageType?: number | string;
    isEnabled?: number;
    status?: number;
}

// 获取列表返回参数类型
export interface FilterResult {
    records: FilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface FilterState {
    decorateId: number;
    decorateTitle?: string;
    sortOrder?: number;
    decorateType?: number;
    pageType?: number;
    isHome?: number;
    status?: number;
    updateTime?: string;
    bindedH5Id?: number;
    bingdingH5Name?: string;
    children?: DecorateChildItem[];
    /** 首页装修版本：new=新版 / old=旧版（仅 PC 首页返回） */
    homeStyleVersion?: "new" | "old";
}

export interface DecorateChildItem {
    decorateId: number;
    localeId: number;
    language?: string;
}

// 获取详情返回参数类型
export interface FormResult {
    item: FormState;
}
export interface FormState {
    decorateTitle?: string;
    decorateDesc?: string;
    sortOrder?: string;
}

// ===================== 底部导航编辑 =====================
// 单个菜单项（在既有 navList 结构上保持兼容）
export interface NavMenuItem {
    _id?: number; // 仅前端拖拽排序用的稳定 key，保存时会被剥离
    picTitle?: string; // 菜单名称
    picThumb?: string; // 未选中图标缩略图
    picUrl?: string; // 未选中图标原图
    picActiveThumb?: string; // 选中图标缩略图
    picActiveUrl?: string; // 选中图标原图
    picLink?: any; // 跳转链接（SelectLink 的 LinkType 对象）
    picId?: string | number;
    picName?: string;
}

// 底部导航模块数据（整体存入装修 JSON）
export interface NavModuleData {
    navName?: string; // 导航名称
    backgroundColorType?: 1 | 2; // 1=系统色 2=自定义颜色
    backgroundColor?: string; // 自定义背景色
    cornerStyle?: 1 | 2 | 3; // 1=直角 2=圆角 3=大圆角
    iconColorType?: 1 | 2; // 1=系统配色 2=自定义配置
    selectedTextColor?: string; // 选中-文本色
    unselectedTextColor?: string; // 未选-文本色
    navList: NavMenuItem[]; // 菜单列表（最多 5）
}
