/**
 * 移动端登录页模块 - 类型与默认值
 *
 * 默认值单一来源：复用旧编辑器 defaultMobileLogin（契约，逐字段一致）。
 * 与 uniapp baseLogin.vue 各 CSS 变量兜底对齐：
 * 链接色/按钮色留空表示继承主题（var(--general)/var(--main-bg)），不写死。
 */

import { defaultMobileLogin } from "@/views/decorate/decorate/src/modules/index";

export interface MobileLoginModule {
    /** 标题色（如“账号登录”） */
    titleColor?: string;
    /** 次要文字色（提示/说明/忘记密码/协议说明） */
    subTextColor?: string;
    /** 输入框文字色 */
    inputTextColor?: string;
    /** 输入框占位符色 */
    placeholderColor?: string;
    /** 链接色（立即注册、协议链接） */
    linkColor?: string;
    /** 登录按钮色 */
    buttonColor?: string;
    /** 分割线 / 输入下划线色 */
    dividerColor?: string;
    /** 内容区左右边距（px，Admin 直接用 px，uniapp 按 px*2 rpx 换算） */
    contentPaddingX?: number;
    /** 内容区顶部间距（px，Admin 直接用 px，uniapp 按 px*2 rpx 换算） */
    contentPaddingTop?: number;
    // 装修容器通用字段
    type?: string;
    url?: string;
    moduleIndex?: number;
    [key: string]: any;
}

/** 与旧 defaultMobileLogin 逐字段一致（契约，单一来源） */
export const mobileLoginDefaults = defaultMobileLogin as MobileLoginModule;
