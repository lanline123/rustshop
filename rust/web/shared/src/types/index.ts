/**
 * 后端统一响应包。
 *
 * 对应 Rust 侧 `tigshop_core::error::ApiResponse`：
 * 序列化时同时输出 `msg` 与 `message`（兼容 Java 原版的 `AjaxResult.message`），
 * 成功时 `code === 0`，`data` 为业务载荷。
 */
export interface ApiResponse<T = unknown> {
  code: number;
  /** 与 `message` 同值，Rust 为兼容 Java 同时输出两个字段 */
  msg?: string;
  message: string;
  data: T;
  timestamp?: number;
}

/**
 * 分页结果。
 *
 * 对应 Rust 侧 `tigshop_core::types::PageResult`，序列化同时输出
 * `records/total/size/current/pages`（Java MyBatis-Plus `Page` 形状）
 * 与 `list/totalPage/pageSize/page`（Rust 原生形状）。
 *
 * 前端统一读 `records`（与原版一致），读不到再回退 `list`。
 */
export interface PageResult<T = unknown> {
  records?: T[];
  list?: T[];
  total: number;
  size?: number;
  current?: number;
  pages?: number;
  totalPage?: number;
  pageSize?: number;
  page?: number;
}

/** 从分页结果里取数组，兼容两种形状。 */
export function pageItems<T>(page: PageResult<T> | T[] | null | undefined): T[] {
  if (!page) return [];
  if (Array.isArray(page)) return page;
  return page.records ?? page.list ?? [];
}

/** 业务错误（`code !== 0`）。 */
export class BizError extends Error {
  readonly code: number;

  constructor(code: number, message: string) {
    super(message);
    this.name = 'BizError';
    this.code = code;
  }
}

/** 登录态失效错误（401），供调用方静默处理。 */
export class UnauthorizedError extends BizError {
  constructor(message = '登录状态已失效，请重新登录') {
    super(401, message);
    this.name = 'UnauthorizedError';
  }
}
