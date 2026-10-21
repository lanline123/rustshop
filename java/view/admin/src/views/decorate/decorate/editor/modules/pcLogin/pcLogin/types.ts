/**
 * PC 登录页模块 - 类型与默认值
 */

export interface PcLoginModule {
    /** 登录页模式：1=经典(BaseLogin) / 2=现代(OverseasLogin) */
    loginMode?: 1 | 2;
    /** 背景图 */
    backgroundPic?: {
        picUrl?: string;
        picThumb?: string;
    };
    /** 背景色 */
    backgroundColor?: string;
    /** 头部高度，单位 px */
    headerHeight?: number;
    /** Logo 宽度，单位 px（配合 object-fit: contain 防止变形） */
    logoWidth?: number;
    /** Logo 高度，单位 px（配合 object-fit: contain 防止变形） */
    logoHeight?: number;
    /** 头部是否显示阴影 */
    headerShadow?: boolean;
    /** 头部是否显示欢迎语与登录/注册互链 */
    showWelcome?: boolean;
    /** 登录区(login-warp)最小高度，单位 px，取值 620–800 */
    warpHeight?: number;
    // 装修容器通用字段
    type?: string;
    moduleIndex?: number;
}

export const pcLoginDefaults: PcLoginModule = {
    // 默认现代版，保持当前线上默认渲染 OverseasLogin
    loginMode: 2,
    backgroundPic: { picUrl: "", picThumb: "" },
    backgroundColor: "",
    // 头部默认对齐前台 CommonLoginHeader：70px 高、带阴影
    headerHeight: 70,
    // Logo 默认尺寸，配合 object-fit: contain 防止变形
    logoWidth: 140,
    logoHeight: 40,
    headerShadow: true,
    // 默认展示头部欢迎语与登录/注册互链
    showWelcome: true,
    // 登录区最小高度默认 620（与滑块下限一致，保证中间档位可见）
    warpHeight: 620
};
