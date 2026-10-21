/**
 * 通用工具。
 */

/**
 * 解析图片 URL。
 *
 * Rust 后端上传接口返回的可能是相对路径（如 `gallery/xxx.png`）或已带 `/static` 前缀；
 * 这里统一补成可访问 URL，避免各处重复判断。
 */
export function resolveImageUrl(url?: string | null, staticPrefix = '/static'): string {
  if (!url) return '';
  if (/^https?:\/\//i.test(url)) return url;
  if (url.startsWith(staticPrefix)) return url;
  if (url.startsWith('/')) return url;
  return `${staticPrefix}/${url.replace(/^\/+/, '')}`;
}

/**
 * 金额格式化。
 *
 * 后端 `Decimal` 序列化为字符串或数字（rust_decimal 默认字符串），
 * 统一转数字后保留两位；非法值回退 `0.00`。
 */
export function formatMoney(value: unknown, digits = 2): string {
  const n = typeof value === 'number' ? value : Number(value);
  if (!Number.isFinite(n)) return (0).toFixed(digits);
  return n.toFixed(digits);
}

/**
 * 时间戳格式化。
 *
 * 注意口径：Rust 侧订单/装修等表多为 **秒级** `i32`，而 VO 层有时转毫秒。
 * 这里按「小于 1e11 视为秒」自动判定，避免出现 1970 年。
 */
export function formatTime(
  value: unknown,
  pattern: 'date' | 'datetime' = 'datetime',
): string {
  if (value === null || value === undefined || value === '') return '';
  let ts = Number(value);
  if (!Number.isFinite(ts) || ts <= 0) return '';
  // 秒级时间戳（10 位）→ 毫秒
  if (ts < 1e11) ts *= 1000;
  const d = new Date(ts);
  if (Number.isNaN(d.getTime())) return '';
  const pad = (n: number) => String(n).padStart(2, '0');
  const date = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
  if (pattern === 'date') return date;
  return `${date} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

/** 深拷贝（结构化克隆，回退 JSON）。 */
export function cloneDeep<T>(value: T): T {
  if (typeof structuredClone === 'function') {
    try {
      return structuredClone(value);
    } catch {
      // 含函数等不可克隆值时回退
    }
  }
  return JSON.parse(JSON.stringify(value)) as T;
}

/** 去掉对象里的空字符串 / null / undefined（用于查询参数）。 */
export function compact<T extends Record<string, unknown>>(obj: T): Partial<T> {
  const out: Record<string, unknown> = {};
  for (const [k, v] of Object.entries(obj)) {
    if (v === '' || v === null || v === undefined) continue;
    if (Array.isArray(v) && v.length === 0) continue;
    out[k] = v;
  }
  return out as Partial<T>;
}
