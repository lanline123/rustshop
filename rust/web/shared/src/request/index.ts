/**
 * 共享请求基座。
 *
 * 两端（admin / pc）共用同一套「响应解包 + 401 处理 + 错误提示」逻辑，
 * 差异（baseURL、token 键名、附加请求头、登录跳转）通过 `createRequest` 的配置注入。
 *
 * 设计约束（来自审计结论，勿破坏）：
 * 1. 成功判定只认 `code === 0`，与 Rust `ApiResponse::success` 一致；
 * 2. 错误文案优先读 `message`，回退 `msg`（Rust 两个字段都输出）；
 * 3. 401 必须清除本地 token 并跳登录，且**不重复弹错误**（跳转本身已是反馈）；
 * 4. 二进制响应（blob/arraybuffer，用于导出）必须原样返回，不参与解包。
 */
import axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
} from 'axios';
import { BizError, UnauthorizedError, type ApiResponse } from '../types';

export interface RequestOptions {
  /** 接口前缀，如 `/adminapi` 或 `/api` */
  baseURL: string;
  /** 读取本地 token 的键名，如 `tig_token` / `tig_user_token` */
  tokenKey: string;
  /** 额外固定请求头（如 `X-CLIENT-TYPE`、`X-ADMIN-TYPE`） */
  headers?: Record<string, string>;
  /** 登录页路径，401 时跳转 */
  loginPath?: string;
  /** 401 时清理哪些 localStorage 键（默认只清 tokenKey） */
  clearKeys?: string[];
  /** 错误提示回调；不传则静默（由调用方自行提示） */
  onError?: (message: string) => void;
  /** 401 处理回调；不传则用默认 `location.href` 跳转 */
  onUnauthorized?: () => void;
  timeout?: number;
}

export interface RequestClient {
  <T = unknown>(config: AxiosRequestConfig): Promise<T>;
  instance: AxiosInstance;
  /** 运行时覆盖额外请求头（如登录后设置店铺 ID） */
  setHeader(key: string, value: string | null): void;
}

/** 判断是否为二进制响应（导出场景）。 */
function isBinary(config: AxiosRequestConfig): boolean {
  return config.responseType === 'blob' || config.responseType === 'arraybuffer';
}

export function createRequest(options: RequestOptions): RequestClient {
  const {
    baseURL,
    tokenKey,
    headers = {},
    loginPath = '/login',
    clearKeys = [],
    onError,
    onUnauthorized,
    timeout = 30000,
  } = options;

  /** 运行时动态头（登录后可能变化，如 X-Shop-Id） */
  const dynamicHeaders: Record<string, string> = {};

  const instance = axios.create({
    baseURL,
    timeout,
    headers: { 'Content-Type': 'application/json', ...headers },
  });

  instance.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem(tokenKey);
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      for (const [k, v] of Object.entries(dynamicHeaders)) {
        config.headers[k] = v;
      }
      return config;
    },
    (error) => Promise.reject(error),
  );

  instance.interceptors.response.use(
    // 说明：axios 的类型声明要求响应拦截器返回 `AxiosResponse`，但本基座的设计是
    // 「成功时直接解包出 data」，因此这里必须放宽返回类型。调用方通过
    // `RequestClient` 的泛型签名拿到正确类型，不受影响。
    ((response: AxiosResponse<ApiResponse>) => {
      // 导出类接口：原样返回完整 response（含 headers，供取文件名）
      if (isBinary(response.config)) {
        return response;
      }

      const res = response.data;

      // 非标准包（例如某些接口直接返回数组/字符串）：原样返回，避免误判
      if (res === null || typeof res !== 'object' || !('code' in res)) {
        return res;
      }

      if (res.code === 0) {
        return res.data;
      }

      const message = res.message || res.msg || '请求失败';

      if (res.code === 401) {
        for (const key of [tokenKey, ...clearKeys]) {
          localStorage.removeItem(key);
        }
        if (onUnauthorized) {
          onUnauthorized();
        } else if (window.location.pathname !== loginPath) {
          const redirect = encodeURIComponent(
            window.location.pathname + window.location.search,
          );
          window.location.href = `${loginPath}?redirect=${redirect}`;
        }
        return Promise.reject(new UnauthorizedError(message));
      }

      onError?.(message);
      return Promise.reject(new BizError(res.code, message));
    }) as unknown as (response: AxiosResponse) => AxiosResponse,
    (error) => {
      // HTTP 层错误（网络中断、5xx、超时）
      const status = error?.response?.status;
      if (status === 401 || status === 403) {
        for (const key of [tokenKey, ...clearKeys]) {
          localStorage.removeItem(key);
        }
        if (onUnauthorized) {
          onUnauthorized();
        } else if (window.location.pathname !== loginPath) {
          window.location.href = loginPath;
        }
        return Promise.reject(new UnauthorizedError());
      }

      const message =
        error?.response?.data?.message ||
        error?.response?.data?.msg ||
        error?.message ||
        '网络异常，请稍后重试';
      onError?.(message);
      return Promise.reject(error);
    },
  );

  const client = (<T = unknown>(config: AxiosRequestConfig): Promise<T> =>
    instance(config) as unknown as Promise<T>) as RequestClient;

  client.instance = instance;
  client.setHeader = (key: string, value: string | null) => {
    if (value === null) {
      delete dynamicHeaders[key];
    } else {
      dynamicHeaders[key] = value;
    }
  };

  return client;
}
