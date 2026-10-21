import { t } from "@/utils/i18n";
import pagesJson from "@/pages.json";
import { baseUrl } from "@/utils/requestBaseUrl";

export { formatArguments } from "@/utils/requestArguments";
export { isShowB2bPriceText } from "@/utils/b2bPrice";

/**
 * 是否为多商户站点，读取构建时环境变量 VITE_IS_MERCHANT
 */
export const isMerchant = () => {
    return import.meta.env.VITE_IS_MERCHANT == 1;
};
export const isPro = () => {
    return import.meta.env.VITE_IS_PRO == 1;
};
export const isB2B = () => {
    return import.meta.env.VITE_IS_B2B == 1;
};
export const isOverseas = () => {
    return import.meta.env.VITE_IS_OVERSEAS == 1;
};
export const isDemo = () => {
    return import.meta.env.VITE_IS_DEMO == 1;
};
/**
 * 是否为 o2o（多门店）站点
 * 版本区分总闸，读取构建时环境变量 VITE_IS_STORE
 */
export const isStore = () => {
    return import.meta.env.VITE_IS_STORE == 1;
};
/**
 * isStore 别名，语义与 PC 端命名对齐
 */
export const isO2o = isStore;

/**
 * 判断是否是json字符串
 * @param str
 * @returns boolean
 */
export const isJsonString = (str: string): boolean => {
    if (typeof str !== "string") return false;
    try {
        const result = JSON.parse(str);
        const type = Object.prototype.toString.call(result);
        return type === "[object Object]" || type === "[object Array]";
    } catch (e) {
        return false;
    }
};

/**
 * 判断是否是url
 * @param str
 * @returns
 */
export function isUrl(str: string): boolean {
    return str.indexOf("http://") != -1 || str.indexOf("https://") != -1;
}

/**
 * 获取tabbar
 */
export function getTabbarPages() {
    return pagesJson.tabBar.list.map((item) => {
        return `/${item.pagePath}`;
    });
}

/**
 * 判断是否是数字
 * @param value
 * @returns
 */
export function isNumber(value: any): boolean {
    return typeof value === "number" && !isNaN(value);
}

/**
 * @description 用于获取用户传递值的px值  如果用户传递了"xxpx"或者"xxrpx"，取出其数值部分，如果是"xxxrpx"还需要用过uni.rpx2px进行转换
 * @param {number|string} value 用户传递值的px值
 * @param {boolean} unit 是否返回带单位的字符串
 * @returns {number|string}
 */
export function getPx(value: number | string, unit: boolean = false): number | string {
    if (isNumber(value)) {
        return unit ? `${value}px` : Number(value);
    }
    // 如果带有rpx，先取出其数值部分，再转为px值
    if (typeof value === "string" && /(rpx|upx)$/.test(value)) {
        return unit ? `${uni.upx2px(parseInt(value))}px` : Number(uni.upx2px(parseInt(value)));
    }
    return unit ? `${parseInt(String(value))}px` : parseInt(String(value));
}

/**
 * @param {Number} len uuid的长度
 * @param {Boolean} firstU 将返回的首字母置为"u"
 * @param {Nubmer} radix 生成uuid的基数(意味着返回的字符串都是这个基数),2-二进制,8-八进制,10-十进制,16-十六进制
 */
export function guid(len: number = 32, firstU: boolean = true, radix?: number): string {
    const chars: string[] = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".split("");
    const uuid: string[] = [];
    const usedRadix = radix || chars.length;

    if (len) {
        // 如果指定uuid长度,只是取随机的字符,0|x为位运算,能去掉x的小数位,返回整数位
        for (let i = 0; i < len; i++) uuid[i] = chars[0 | (Math.random() * usedRadix)];
    } else {
        let r: number;
        // rfc4122标准要求返回的uuid中,某些位为固定的字符
        uuid[8] = uuid[13] = uuid[18] = uuid[23] = "-";
        uuid[14] = "4";

        for (let i = 0; i < 36; i++) {
            if (!uuid[i]) {
                r = 0 | (Math.random() * 16);
                uuid[i] = chars[i == 19 ? (r & 0x3) | 0x8 : r];
            }
        }
    }
    // 移除第一个字符,并用u替代,因为第一个字符为数值时,该guuid不能用作id或者class
    if (firstU) {
        uuid.shift();
        return `u${uuid.join("")}`;
    }
    return uuid.join("");
}

export function addUnit(value: number | string, unit = "px") {
    return isNumber(value) ? `${value}${unit}` : value;
}

export function delay(value = 30): Promise<boolean> {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve(true);
        }, value);
    });
}

/**
 * 拼接 querystring（会过滤 undefined/null/空字符串，并对 value 做 encodeURIComponent）
 * @example buildQueryString({ a: 1, b: "x" }) => "?a=1&b=x"
 * @example buildQueryString({ a: 1 }, "&") => "&a=1"
 */
export function buildQueryString(query?: Record<string, unknown>, prefix: "?" | "&" = "?"): string {
    if (!query) return "";
    const entries = Object.entries(query).filter(([, value]) => value !== undefined && value !== null && value !== "");
    if (entries.length === 0) return "";
    const qs = entries
        .map(([key, value]) => {
            const encodedValue = typeof value === "object" ? encodeURIComponent(JSON.stringify(value)) : encodeURIComponent(String(value));
            return `${encodeURIComponent(String(key))}=${encodedValue}`;
        })
        .join("&");
    return `${prefix}${qs}`;
}

/**
 * 获取门店/店铺主页 URL：
 * - O2O（VITE_IS_STORE=1）跳门店首页：/pages/store/index?id=xxx（含平台自营 shopId=0）
 * - 非 O2O 跳店铺首页：/pages/shop/index?shopId=xxx
 */
export function getStoreOrShopHomeUrl(shopId: string | number, extraQuery?: Record<string, unknown>): string {
    if (shopId === undefined || shopId === null) return "";
    const idStr = String(shopId);
    // O2O 平台自营门店使用 shopId=0，允许进入门店首页；普通版本的 0 仍视为无效店铺。
    if (!idStr || (idStr === "0" && !isO2o())) return "";

    const base = isO2o() ? `/pages/store/index?id=${idStr}` : `/pages/shop/index?shopId=${idStr}`;
    if (!extraQuery || Object.keys(extraQuery).length === 0) return base;

    const prefix: "?" | "&" = base.includes("?") ? "&" : "?";
    return `${base}${buildQueryString(extraQuery, prefix)}`;
}

export type ToStoreOrShopHomeOptions = {
    extraQuery?: Record<string, unknown>;
    /**
     * navigateTo：新开页面
     * redirect：复用项目内 redirect 封装（默认）
     */
    navType?: "navigateTo" | "redirect";
};

/**
 * 统一「进店/去商家主页」跳转（按 isO2o 自动选择门店/店铺主页）
 */
export function toStoreOrShopHome(shopId: string | number, options?: ToStoreOrShopHomeOptions) {
    const url = getStoreOrShopHomeUrl(shopId, options?.extraQuery);
    if (!url) return;

    if (options?.navType === "navigateTo") {
        uni.navigateTo({ url });
        return;
    }
    redirect({ url });
}

/**
 * 跳转页面
 */
interface redirectOptions {
    url: string;
    mode?: "switchTab" | "navigateTo" | "reLaunch" | "redirectTo";
    param?: AnyObject;
    success?: Function;
    fail?: Function;
    complete?: Function;
}
export const redirect = (redirect: redirectOptions) => {
    let { url, mode = "navigateTo", param, success, fail, complete } = redirect;
    if (isUrl(url)) {
        // #ifdef H5
        window.open(url, "_blank");
        return;
        // #endif
        uni.navigateTo({
            url: `/pages/webviewPage/index?url=${url}`
        });
    }
    mode = mode;
    const tabBar = getTabbarPages();
    tabBar.includes(url) && (mode = "switchTab");
    mode != "switchTab" && param && Object.keys(param).length && (url += uni.$u.queryParams(param));
    switch (mode) {
        case "switchTab":
            uni.switchTab({
                url,
                success: () => {
                    success && success();
                },
                fail: () => {
                    fail && fail();
                },
                complete: () => {
                    complete && complete();
                }
            });
            break;
        case "navigateTo":
            uni.navigateTo({
                url,
                success: () => {
                    success && success();
                },
                fail: () => {
                    fail && fail();
                },
                complete: () => {
                    complete && complete();
                }
            });
            break;
        case "reLaunch":
            uni.reLaunch({
                url,
                success: () => {
                    success && success();
                },
                fail: () => {
                    fail && fail();
                },
                complete: () => {
                    complete && complete();
                }
            });
            break;
        case "redirectTo":
            uni.redirectTo({
                url,
                success: () => {
                    success && success();
                },
                fail: () => {
                    fail && fail();
                },
                complete: () => {
                    complete && complete();
                }
            });
            break;
    }
};

/**
 * 复制
 * @param {Object} message
 * @param {Object} callback
 */
export function copy(value: any, callback: any) {
    // #ifdef H5
    const oInput = document.createElement("input"); //创建一个隐藏input（重要！）
    oInput.value = value; //赋值
    oInput.setAttribute("readonly", "readonly");
    document.body.appendChild(oInput);
    oInput.select(); // 选择对象
    document.execCommand("Copy"); // 执行浏览器复制命令
    oInput.className = "oInput";
    oInput.style.display = "none";
    uni.hideKeyboard();

    typeof callback == "function" && callback();
    // #endif

    // #ifdef MP || APP-PLUS
    uni.setClipboardData({
        data: value,
        success: () => {
            typeof callback == "function" && callback();
        },
        fail: (res) => {
            // 在隐私协议中没有声明chooseLocation:fail api作用域
            if (res.errMsg && res.errno) {
                if (res.errno == 104) {
                    uni.showToast({ title: res.errMsg, icon: "none" });
                } else if (res.errno == 112) {
                    uni.showToast({ title: res.errMsg, icon: "none" });
                } else {
                    uni.showToast({ title: res.errMsg, icon: "none" });
                }
            }
        }
    });
    // #endif
}

/**
 * 手机号隐藏
 */
export function mobileConceal(mobile: string): string {
    return mobile.substring(0, 3) + "****" + mobile.substr(mobile.length - 4);
}

/**
 * 判断是否是微信浏览器
 */
export function isWeixinBrowser(): boolean {
    // #ifndef H5
    return false;
    // #endif
    const ua = navigator.userAgent.toLowerCase();
    return /micromessenger/.test(ua) ? true : false;
}

/**
 * 判断 H5 当前是否为移动端浏览器。
 * 兼容鸿蒙微信等 UA 标识不稳定，但具备触摸能力的 WebView 场景。
 */
export function isMobileBrowser(): boolean {
    // #ifndef H5
    return false;
    // #endif
    const ua = navigator.userAgent.toLowerCase();
    const mobilePattern = /android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini|mobile|phone|harmonyos|openharmony|hm os|arkweb/i;
    const hasTouchCapability = typeof window !== "undefined" && ("ontouchstart" in window || navigator.maxTouchPoints > 1);
    return mobilePattern.test(ua) || (isWeixinBrowser() && hasTouchCapability);
}

export function isDesktopBrowser(): boolean {
    return !isMobileBrowser();
}

/**
 * 获取当前路由
 */
export const currRoute = () => {
    const pages = getCurrentPages();
    const route = pages[pages.length - 1];
    return route ? route.route : "";
};

/**
 * 获取上一页路由
 */

export const prevRoute = () => {
    const pages = getCurrentPages();
    const route = pages[pages.length - 2];
    return route ? route.route : "";
};

/**
 * 获取当前所有路径
 */
export const currFullPath = () => {
    const pages = getCurrentPages();
    const route = pages[pages.length - 1];
    return route ? route.$page.fullPath : "";
};

/* 获取元素布局信息 */
interface Rect {
    top: number;
    left: number;
    width: number;
    height: number;
    bottom: number;
    right: number;
    dataset: object;
    id: string;
}
export function getElementRect(selector: string, instance?: any): Promise<Rect | null>;
export function getElementRect(selector: string, instance: any, all: true): Promise<Rect[]>;
export function getElementRect(selector: string, instance?: any, all?: boolean): Promise<Rect | Rect[] | null> {
    return new Promise((resolve) => {
        // 创建选择器查询对象
        const query = instance ? uni.createSelectorQuery().in(instance) : uni.createSelectorQuery();

        // all=true：查询匹配到的所有元素
        if (all) {
            query
                .selectAll(selector)
                .boundingClientRect((data: any) => {
                    resolve(Array.isArray(data) ? data : []);
                })
                .exec();
            return;
        }

        // 默认：仅查询第一个匹配元素
        query
            .select(selector)
            .boundingClientRect((data: any) => {
                resolve(data ? data : null);
            })
            .exec();
    });
}

export function getImageInfo(src: string): Promise<UniApp.GetImageInfoSuccessData> {
    return new Promise((resolve, reject) => {
        uni.getImageInfo({
            src,
            success: (res) => {
                resolve(res);
            },
            fail: (err) => {
                reject(err);
            }
        });
    });
}

export function getDownloadFileInfo(url: string): Promise<UniApp.DownloadSuccessData> {
    return new Promise((resolve, reject) => {
        uni.downloadFile({
            url,
            success: (res: UniApp.DownloadSuccessData) => {
                resolve(res);
            },
            fail: (err) => {
                reject(err);
            }
        });
    });
}

/**
 * 静态资源输出
 * @param path
 * @returns
 */
export function staticResource(path: string, type = "images"): string {
    if (isUrl(path)) {
        return path;
    } else if (isUrl(import.meta.env.VITE_STATIC_RESOURCE)) {
        return `${import.meta.env.VITE_STATIC_RESOURCE}/${type}/${path}`;
    } else {
        return `${baseUrl}/${import.meta.env.VITE_STATIC_RESOURCE}/${type}/${path}`;
    }
}

export const maskNumber = (num: number | string) => {
    const data = num.toString();
    if (data.length < 4) {
        return data;
    } else {
        return data.slice(0, 2) + "********" + data.slice(-2);
    }
};

export const saveImageToAlbum = (filePath: string) => {
    // #ifdef MP
    return saveImageToMp(filePath);
    // #endif
    // #ifdef APP-PLUS
    return saveImageToApp(filePath);
    // #endif
};
export const saveImageToMp = (filePath: string) => {
    return new Promise((resolve, reject) => {
        uni.getSetting({
            success(res) {
                if (!res.authSetting["scope.writePhotosAlbum"]) {
                    // 请求授权写入相册
                    uni.authorize({
                        scope: "scope.writePhotosAlbum",
                        success() {
                            // 用户已同意授权
                            uni.saveImageToPhotosAlbum({
                                filePath,
                                success: (res) => {
                                    resolve(res);
                                },
                                fail: (err) => {
                                    reject(err);
                                }
                            });
                        },
                        fail() {
                            // 用户不同意授权
                            uni.showModal({
                                title: t("需要权限"),
                                content: t("需要您授权保存图片到相册"),
                                showCancel: false,
                                confirmText: t("去设置"),
                                success(modalRes) {
                                    if (modalRes.confirm) {
                                        // 引导用户去开启授权
                                        uni.openSetting({
                                            success(settingRes) {
                                                if (settingRes.authSetting["scope.writePhotosAlbum"]) {
                                                    // 用户开启了授权，现在可以保存图片
                                                    uni.saveImageToPhotosAlbum({
                                                        filePath,
                                                        success: (res) => {
                                                            resolve(res);
                                                        },
                                                        fail: (err) => {
                                                            reject(err);
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
                } else {
                    uni.saveImageToPhotosAlbum({
                        filePath,
                        success: (res) => {
                            resolve(res);
                        },
                        fail: (err) => {
                            reject(err);
                        }
                    });
                }
            }
        });
    });
};

export const saveImageToApp = (filePath: string) => {
    return new Promise((resolve, reject) => {
        uni.saveImageToPhotosAlbum({
            filePath,
            success: (res) => {
                resolve(res);
            },
            fail: (err) => {
                reject(err);
            }
        });
    });
};

export const moneyFormat = (money: string): string => {
    return isNaN(parseFloat(money)) ? money : parseFloat(money).toFixed(2);
};

export const formatRichText = (html: string) => {
    let newContent = html.replace(/<img[^>]*>/gi, function (match, capture) {
        match = match.replace(/style="[^"]+"/gi, "").replace(/style='[^']+'/gi, "");
        match = match.replace(/width="[^"]+"/gi, "").replace(/width='[^']+'/gi, "");
        match = match.replace(/height="[^"]+"/gi, "").replace(/height='[^']+'/gi, "");
        return match;
    });
    newContent = newContent.replace(/style="[^"]+"/gi, function (match, capture) {
        match = match.replace(/width:[^;]+;/gi, "max-width:100%;").replace(/width:[^;]+;/gi, "max-width:100%;");
        return match;
    });
    newContent = newContent.replace(/<br[^>]*\/>/gi, "");
    newContent = newContent.replace(/\<img/gi, '<img style="max-width: 100%;height: auto;display: block;margin-top: 0;margin-bottom: 0;"');
    return newContent;
};

export const trim = (str: string): string => {
    return str.replace(/^\s+|\s+$/g, "");
};

/**
 * 规格数据项
 */
interface SkuDataItem {
    name?: string;
    value?: string;
    attrValue?: string;
    label?: string;
    [key: string]: any;
}

/**
 * 解析 skuData：可能是数组，也可能是 JSON 字符串
 */
export function parseSkuData(skuData: unknown): SkuDataItem[] {
    if (Array.isArray(skuData)) return skuData as SkuDataItem[];
    if (typeof skuData === "string" && skuData.trim()) {
        try {
            const parsed = JSON.parse(skuData);
            if (Array.isArray(parsed)) return parsed as SkuDataItem[];
        } catch {
            // 非合法 JSON，按空规格处理
        }
    }
    return [];
}

/**
 * 把规格数据格式化为展示文本
 */
export function getSkuText(skuData: unknown, separator = " / "): string {
    return parseSkuData(skuData)
        .map((item) => {
            const name = item?.name ?? "";
            const value = item?.value ?? "";
            if (!value) return "";
            return name ? `${name}:${value}` : value;
        })
        .filter(Boolean)
        .join(separator);
}

/**
 * 获取用于购物车等紧凑场景展示的规格值列表，不包含规格名
 */
export function getSkuValueList(skuData: unknown): string[] {
    let source: unknown = skuData;

    if (typeof source === "string" && source.trim()) {
        try {
            source = JSON.parse(source);
        } catch {
            return [source.trim()];
        }
    }

    const items = Array.isArray(source) ? source : source == null ? [] : [source];

    return items
        .map((item) => {
            if (item == null) return "";
            if (typeof item === "string" || typeof item === "number" || typeof item === "boolean") return String(item).trim();
            if (typeof item !== "object") return "";

            const skuItem = item as SkuDataItem;
            const value = skuItem.value ?? skuItem.attrValue ?? skuItem.label ?? skuItem.name;
            return value == null ? "" : String(value).trim();
        })
        .filter(Boolean);
}

/**
 * 把规格数据格式化为仅包含规格值的展示文本
 */
export function getSkuValueText(skuData: unknown, separator = " / "): string {
    return getSkuValueList(skuData).join(separator);
}

/**
 * 是否存在可展示的规格文本
 */
export function hasSkuText(skuData: unknown): boolean {
    return getSkuText(skuData).length > 0;
}

/**
 * 解析 querystring（支持带/不带 ? 前缀，并对 key/value 做 decodeURIComponent）
 * @example parseQueryString("?a=1&b=x") => { a: "1", b: "x" }
 * @example parseQueryString("a=1&b=x%20y") => { a: "1", b: "x y" }
 */
export function parseQueryString(query?: string): Record<string, string> {
    if (!query) return {};
    const normalizedQuery = String(query)
        .trim()
        .replace(/^[?#&]/, "");
    if (!normalizedQuery) return {};

    return normalizedQuery.split("&").reduce<Record<string, string>>((acc, pair) => {
        if (!pair) return acc;
        const [rawKey, ...rawValueParts] = pair.split("=");
        if (!rawKey) return acc;

        const key = decodeURIComponent(rawKey);
        const value = decodeURIComponent(rawValueParts.join("=") || "");
        acc[key] = value;
        return acc;
    }, {});
}

/**
 * 安全地向 URL 追加 query 参数
 * - 自动判断使用 `?` 还是 `&`（首参用 ?）
 * - 自动去重：若 URL 已存在同名参数则先剥离，避免二次转发出现 `?k=A&k=B`
 * - 保留 hash（#xxx）：参数始终拼在 hash 之前
 * @example appendQueryParam("/p/index", "a", 1) => "/p/index?a=1"
 * @example appendQueryParam("/p/index?id=1", "a", 2) => "/p/index?id=1&a=2"
 * @example appendQueryParam("/p/index?a=1#tab", "a", 2) => "/p/index?a=2#tab"
 */
export function appendQueryParam(url: string, key: string, value: string | number | undefined | null): string {
    if (!url) return url;
    if (value === undefined || value === null || value === "") return url;
    const safeKey = encodeURIComponent(String(key));
    const safeValue = encodeURIComponent(String(value));

    let hash = "";
    let main = url;
    const hashIdx = url.indexOf("#");
    if (hashIdx >= 0) {
        hash = url.slice(hashIdx);
        main = url.slice(0, hashIdx);
    }

    const qIdx = main.indexOf("?");
    let path = main;
    let query = "";
    if (qIdx >= 0) {
        path = main.slice(0, qIdx);
        query = main.slice(qIdx + 1);
    }

    if (query) {
        const re = new RegExp("(^|&)" + safeKey.replace(/[.*+?^${}()|[\]\\]/g, "\\$&") + "=[^&]*", "g");
        query = query.replace(re, "").replace(/^&/, "").replace(/&&+/g, "&");
    }

    const pair = `${safeKey}=${safeValue}`;
    const newQuery = query ? `${query}&${pair}` : pair;
    return `${path}?${newQuery}${hash}`;
}

export function downloadImage(url: string) {
    return new Promise((resolve, reject) => {
        // 获取当前平台
        const platform = uni.getSystemInfoSync().platform;
        console.log(platform);

        // 小程序或APP
        uni.downloadFile({
            url: url, // 图片文件的URL
            success: (res) => {
                if (res.statusCode === 200) {
                    //#ifdef MP-WEIXIN
                    saveImageToMp(res.tempFilePath);
                    //#endif

                    //#ifdef APP-PLUS
                    resolve(t("图片下载成功"));
                    saveImageToApp(res.tempFilePath);
                    //#endif
                } else {
                    reject("下载失败，状态码：" + res.statusCode);
                }
            },
            fail: (err) => {
                reject(err);
            }
        });

        // #ifdef H5
        const a = document.createElement("a");
        a.href = url;
        console.log(url);

        a.download = t("发票.png"); // 获取文件名
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        resolve(t("图片下载成功"));
        //#endif
    });
}
