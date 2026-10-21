import axios, { AxiosResponse, AxiosRequestConfig, AxiosError } from "axios";
import type { AxiosInstance, InternalAxiosRequestConfig } from "axios";
import { useMenusStore } from "@/store/menu";
import { message } from "ant-design-vue";
import { formatArguments } from "@/utils/util";
import { loginOut } from "@/utils/storage";
import { isS2b2c, isStore, isMerchant } from "@/utils/version";
import { getLocale } from "@/i18n";
import { isPosWorkbenchPath, normalizeRequestAdminType } from "@/utils/requestAdminType";
const { VITE_BASE_URL, VITE_REQUEST_URL_PREFIX, VITE_BASE_DIR } = import.meta.env;

/** 最终写入 X-ADMIN-TYPE 的值：收银台固定 pos，其余用真实登录身份 */
const resolveRequestAdminType = (): string => {
    if (typeof window !== "undefined" && isPosWorkbenchPath(window.location.pathname, VITE_BASE_DIR)) {
        return "pos";
    }
    return normalizeRequestAdminType(localStorage.getItem("adminType"));
};

const requestUrl = {
    prefix: VITE_BASE_URL + VITE_REQUEST_URL_PREFIX, // 所有的请求地址前缀部分
    headers: () => {
        const accessToken = localStorage.getItem("accessToken");
        const headers: any = {
            "X-ADMIN-TYPE": resolveRequestAdminType(),
            "X-ClIENT-TYPE": 'admin',
            "X-Locale-Code": getLocale()
        };
        if (accessToken) {
            headers["Authorization"] = 'Bearer ' + accessToken;
        }
        if (isStore() || isMerchant()) {
            headers["X-Shop-Id"] = localStorage.getItem("shopId") || 0;
        }
        return headers;
    }
};

// 扩展 AxiosRequestConfig 接口以包含 prefix 参数
interface CustomAxiosRequestConfig extends AxiosRequestConfig {
    prefix?: string;
    noErrorTip?: boolean;
}

// ============================================================================
// 统一登出处理（并发 401 只触发一次 logout + 一条用户提示）
// ============================================================================

let isLoggingOut = false;

function handleUnauthorized() {
    if (isLoggingOut) return;
    // 只有在确实有登录状态的情况下才提示并登出
    const accessToken = localStorage.getItem("accessToken");
    if (!accessToken) return;

    // 持续为 true 直到整页 reload 重建 JS 状态，避免在本轮登出过程中再触发提示
    isLoggingOut = true;
    message.error("登录已过期，请重新登录");

    // 过期强制登出：不再调用后端 user/user/logout（已过期 token 调它也只会再 401 触发死循环），
    // 直接本地清理 + 整页刷新；用户主动点"退出登录"仍走 store.logout()，行为不变。
    setTimeout(() => {
        try {
            loginOut();
            const menus = useMenusStore();
            menus.clearMenus();
        } catch (e) {
            // 清理失败也要确保能刷新页面回到登录态
        }
        window.location.reload();
    }, 200);
}

// ============================================================================
// 单例 axios 实例（拦截器只注册一次，避免并发请求各自独立触发提示）
// ============================================================================

const defaultBaseURL = `${VITE_BASE_URL}${VITE_REQUEST_URL_PREFIX}`;

const axiosInstance: AxiosInstance = axios.create({
    baseURL: defaultBaseURL,
    timeout: 60000,
    headers: { "Content-Type": "application/json" }
});

axiosInstance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const accessToken = localStorage.getItem("accessToken");
        if (accessToken) {
            config.headers!["Authorization"] = `Bearer ${accessToken}`;
        }
        if (isStore() || isMerchant()) {
            config.headers!["X-Shop-Id"] = localStorage.getItem("shopId") || 0;
        }
        // POS 充值等接口自带 admin-type，覆盖会丢掉收银场景标识
        if (!config.headers!.has("X-ADMIN-TYPE")) {
            config.headers!.set("X-ADMIN-TYPE", resolveRequestAdminType());
        }
        config.headers!["X-ClIENT-TYPE"] = 'admin';
        config.headers!["X-Locale-Code"] = getLocale();
        config.params = formatArguments(config.params);
        return config;
    },
    (error: AxiosError) => Promise.reject(error)
);

axiosInstance.interceptors.response.use(
    (response: AxiosResponse) => response,
    (error: any) => {
        if (error.response) {
            const data = error.response.data;
            const status = error.response.status;
            if (status === 403 || status === 401) {
                // 禁止访问、无效 Token：交给统一登出处理（并发 401 只弹一次）
                handleUnauthorized();
                return new Promise(() => {});
            } else if (data && (data.message || data.msg)) {
                if (!error.config?.noErrorTip) {
                    message.error(data.message || data.msg);
                }
            }
        }
        return Promise.reject(error) as any;
    }
);

type ApiResponse<T> = T & {
    code: number;
    message: string;
    data: T;
};
export function request<T>(config: CustomAxiosRequestConfig): Promise<ApiResponse<T>> {
    // 支持 config.prefix：临时覆盖 baseURL，不影响全局单例默认值
    const customPrefix = config.prefix;
    if (customPrefix) {
        config.baseURL = `${VITE_BASE_URL}${customPrefix}`;
    }

    return axiosInstance(config).then((response: AxiosResponse<ApiResponse<T>>) => {
        const apiResponse = response.data;
        if (config.responseType) {
            if (apiResponse.code > 0) {
                return Promise.reject(apiResponse.data);
            }
            return apiResponse;
        } else {
            if (apiResponse.code === 401) {
                // 业务层 401：登录过期，走统一登出处理并阻断后续 .catch
                handleUnauthorized();
                return new Promise(() => {}) as any;
            } else if (apiResponse.code > 0 && apiResponse.code != 401) {
                return Promise.reject(apiResponse);
            } else {
                return apiResponse.data as any;
            }
        }
    }).catch(error => {
        // 登出过程中吞掉其它并发请求的错误，避免二次弹窗
        if (isLoggingOut) return new Promise(() => {}) as any;
        // 处理错误，确保返回的错误也是一个 ApiResponse<T> 类型的对象
        return Promise.reject({
            code: error.code,
            message: error.message,
            data: error?.data ?? null
        } as ApiResponse<T>);
    });
}

export default request;

export { requestUrl, axiosInstance };
