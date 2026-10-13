import { useConfigStore } from "@/store/config";
import { returnAvatar } from "@/utils/avatar";
import { extractContent } from "@/utils/util";

// 格式化图片地址 支持本地图片
export function imageFormat(path: string | undefined, isLocal?: boolean) {
    const config = useConfigStore();
    if (!path) {
        return "";
    }
    return path.includes("http") ? path : (isLocal ? path : config.get("storageUrl") + path);
}

interface PicSource {
    picUrl?: string;
    picThumb?: string;
    picLarge?: string;
}

// 按 picUrl > picThumb > picLarge 取图，不拼接域名
export function pickPic(source: PicSource | null | undefined): string {
    if (!source) {
        return "";
    }
    return source.picUrl || source.picThumb || source.picLarge || "";
}

// 按 picUrl > picThumb > picLarge 取图并拼接 storageUrl 前缀
export function productImage(source: PicSource | null | undefined): string {
    return imageFormat(pickPic(source));
}

//格式化头像
export function avatarFormat(avatar: string) {
    if (extractContent(String(avatar)) == "def") {
        return returnAvatar(avatar);
    } else {
        return imageFormat(avatar);
    }
}
// 格式化金额
export function priceFormat(price: number | string | undefined | null, currencyFormat = true) {
    const config = useConfigStore();
    const currency = config.get("dollarSign") ?? "¥";
    const value = price ?? 0;
    const num = typeof value === "string" ? parseFloat(value) : value;
    const safeNum = Number.isFinite(num) ? Math.round((num + Number.EPSILON) * 100) / 100 : 0;
    if (currencyFormat) {
        // 负数时负号置于货币符号之前，如 -¥5.00
        const sign = safeNum < 0 ? "-" : "";
        return sign + currency + Math.abs(safeNum).toFixed(2);
    } else {
        return safeNum.toFixed(2);
    }
}

// 货币符号（如 ¥ / $），取全局配置 dollarSign，跨境版主货币联动后自动更新
// 用于【展示】场景：符号放数字前
export function currencySymbol(): string {
    const config = useConfigStore();
    return (config.get("dollarSign") as string) ?? "¥";
}

// 货币中文单位（如 元 / 美元），取全局配置 amountUnit（映射自 dollarSignCn）
// 用于【输入】场景的单位后缀、文案中的货币名称
export function currencyUnit(): string {
    const config = useConfigStore();
    return (config.get("amountUnit") as string) ?? "元";
}

// 链接格式化
export function urlFormat(params: string | { path: string; [key: string]: any; platform?: string }): string {
    const config = useConfigStore();
    const clientDefaultUse = config.get("clientDefaultUse") || 0;
    const domain = clientDefaultUse == 1 ? config.get("h5Domain") : config.get("pcDomain");

    if (typeof params === "string") {
        return domain + params;
    } else {
        switch (params.path) {
            case "product":
                if (clientDefaultUse == 1) {
                    return (domain ? domain : "/mobile") + "/pages/product/index?id=" + params.id + "";
                } else {
                    return domain + "/item/" + params.sn + "";
                }
            default:
                return domain + params.path;
        }
    }
}
export function urlWapFormat(params: string | { path: string; [key: string]: any }): string {
    const config = useConfigStore();
    const linkUrl = window.location.origin;
    const domain = config.get("h5Domain") ? config.get("h5Domain") : linkUrl + "/mobile";
    if (typeof params === "string") {
        return domain + params;
    } else {
        switch (params.path) {
            case "product":
                return domain + "/item/?id=" + params.id + "";
            default:
                return domain + params.path;
        }
    }
}
export function convertNullsToEmptyStrings(obj: any) {
    if (obj && typeof obj === "object") {
        for (const key in obj) {
            if (obj[key] === null || obj[key] === undefined || obj[key] === "null" || obj[key] === "undefined") {
                obj[key] = "";
            } else if (typeof obj[key] === "object") {
                // 递归处理嵌套对象
                convertNullsToEmptyStrings(obj[key]);
            }
        }
        return obj;
    }
    return {};
}

//格式化头像
export function baseDirFormat(path: string) {
    if (path) {
        const result = /^\//.test(path);
        return result ? path : `/${path}`;
    } else {
        return "";
    }
}

/**
 ** 减法函数，用来得到精确的减法结果
 ** 说明：javascript的减法结果会有误差，在两个浮点数相减的时候会比较明显。这个函数返回较为精确的减法结果。
 ** 调用：accSub(arg1,arg2)
 ** 返回值：arg1减arg2的精确结果
 **/
export function accSub(arg1: any = 0, arg2: any) {
    let r1, r2, m, n;
    try {
        r1 = arg1.toString().split(".")[1].length;
    } catch (e) {
        r1 = 0;
    }
    try {
        r2 = arg2.toString().split(".")[1].length;
    } catch (e) {
        r2 = 0;
    }
    m = Math.pow(10, Math.max(r1, r2)); //last modify by deeka //动态控制精度长度
    n = r1 >= r2 ? r1 : r2;
    return ((arg1 * m - arg2 * m) / m).toFixed(n);
}

// 提取用于授权校验的根域名：去协议/路径/参数/www 前缀（保留端口），与官网授权绑定域名保持同源同格式
export function formatDomain(domain: string): string {
    let cleanDomain = domain.replace(/^https?:\/\//, "");
    cleanDomain = cleanDomain.split("/")[0].split("?")[0].split("#")[0];
    cleanDomain = cleanDomain.replace(/^www\./, "");
    return cleanDomain;
}

