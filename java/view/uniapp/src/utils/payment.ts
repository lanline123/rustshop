declare const plus: any;

export interface OpenExternalPaymentOptions {
    clientType: string;
    onFail?: () => void;
}

const isInIframe = () => {
    try {
        return window.self !== window.top;
    } catch {
        return true;
    }
};

const redirectToTopLevel = (url: string): boolean => {
    let launched = false;
    // #ifdef H5
    if (isInIframe()) {
        const link = document.createElement("a");
        link.href = url;
        link.target = "_top";
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    } else {
        window.location.assign(url);
    }
    launched = true;
    // #endif
    return launched;
};

/**
 * 按客户端能力打开第三方收银台。
 * - H5/公众号：顶层页面跳转
 * - App：系统浏览器（APP-PLUS）或受控 web-view（HarmonyOS）
 * - 小程序：受控 web-view；目标域名仍需在对应平台配置业务域名
 */
export const openExternalPaymentUrl = (rawUrl: unknown, options: OpenExternalPaymentOptions): boolean => {
    const url = typeof rawUrl === "string" ? rawUrl.trim() : "";
    if (!/^https:\/\//i.test(url)) return false;

    const { clientType, onFail } = options;

    if (clientType === "h5" || clientType === "wechat") {
        return redirectToTopLevel(url);
    }

    if (clientType === "app") {
        let launched = false;
        // #ifdef APP-PLUS
        plus.runtime.openURL(url, () => onFail?.());
        launched = true;
        // #endif

        // #ifdef APP-HARMONY
        uni.navigateTo({
            url: `/pages/webviewPage/index?url=${encodeURIComponent(url)}`,
            fail: () => onFail?.()
        });
        launched = true;
        // #endif
        return launched;
    }

    if (clientType === "miniProgram") {
        let launched = false;
        // #ifdef MP
        uni.navigateTo({
            url: `/pages/webviewPage/index?url=${encodeURIComponent(url)}`,
            fail: () => onFail?.()
        });
        launched = true;
        // #endif
        return launched;
    }

    return false;
};
