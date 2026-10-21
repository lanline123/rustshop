export interface ThemeStyleFormResult {
    item: ThemeStyleFormState;
    color: ColorList[];
}

export interface ThemeStyleFormState {
    themeId?: number;
    check?: boolean;
    "--general"?: string; //主色
    "--main-bg"?: string; //主背景色
    "--main-btn-hover-bg"?: string; //主背景按钮悬停色
    "--main-bg-gradient"?: string; //主背景渐变色
    "--main-text"?: string; //主背景文字颜色
    "--vice-bg"?: string; //副背景色
    "--vice-text"?: string; //副文字颜色
    "--icon"?: string; //图标
    "--price"?: string; //价格颜色
    "--tag-text"?: string; //标签文字颜色
    "--tag-bg"?: string; //标签背景颜色
}

export interface ColorList {
    themeId: number;
    check: boolean;
    "--general"?: string; //主色
    "--main-bg"?: string; //主背景色
    "--main-btn-hover-bg"?: string; //主背景按钮悬停色
    "--main-bg-gradient"?: string; //主背景渐变色
    "--main-text"?: string; //主背景文字颜色
    "--vice-bg"?: string; //副背景色
    "--vice-text"?: string; //副文字颜色
    "--icon"?: string; //图标
    "--price"?: string; //价格颜色
    "--tag-text"?: string; //标签文字颜色
    "--tag-bg"?: string; //标签背景颜色
    "--primary-light-3"?: string;
    "--primary-light-5"?: string;
    "--primary-light-7"?: string;
    "--primary-light-8"?: string;
    "--primary-light-9"?: string;
    "--primary-dark-2"?: string;
    "--ump-main-bg"?: string;
    "--ump-main-text"?: string;
    "--ump-vice-bg"?: string;
    "--ump-vice-text"?: string;
    "--ump-icon"?: string;
    "--ump-price"?: string;
    "--ump-tag-text"?: string;
    "--ump-tag-bg"?: string;
    "--ump-coupon-bg"?: string;
    "--ump-border"?: string;
    "--ump-start-bg"?: string;
    "--ump-end-bg"?: string;
}
