/**
 * PC 商城端格式化工具（价格 / 图片 / 时间 / 订单状态）。
 */
import dayjs from 'dayjs';
import { resolveImageUrl } from '@/composables/useSeo';

/** 价格统一保留 2 位小数，兼容后端 Decimal 序列化的 number/string。 */
export function formatPrice(value: number | string | null | undefined): string {
  const n = Number(value ?? 0);
  if (!Number.isFinite(n)) return '0.00';
  return n.toFixed(2);
}

/** 整数部分（用于大字号展示）。 */
export function priceInteger(value: number | string | null | undefined): string {
  return formatPrice(value).split('.')[0];
}

/** 小数部分（两位）。 */
export function priceDecimal(value: number | string | null | undefined): string {
  return formatPrice(value).split('.')[1] ?? '00';
}

/** 图片地址（相对路径补 `/static` 前缀）。 */
export function imageUrl(value: string | null | undefined): string {
  return resolveImageUrl(value);
}

/** Unix 秒 → `YYYY-MM-DD HH:mm:ss`。 */
export function formatTime(seconds: number | null | undefined): string {
  if (!seconds || seconds <= 0) return '-';
  return dayjs.unix(seconds).format('YYYY-MM-DD HH:mm:ss');
}

/** 订单状态码 → 文案（对齐后端 `OrderVO.statusLabel`，仅在缺省时兜底）。 */
export const ORDER_STATUS_TEXT: Record<number, string> = {
  0: '待付款',
  1: '待发货',
  2: '待收货',
  3: '待评价',
  4: '已完成',
  5: '已取消',
  6: '已关闭',
  7: '售后中',
};

/** 订单状态文案（优先用后端 `statusLabel`）。 */
export function orderStatusText(status: number, label?: string): string {
  if (label) return label;
  return ORDER_STATUS_TEXT[status] ?? `状态${status}`;
}

/** 订单状态标签类型（Element Plus tag type）。 */
export function orderStatusTagType(status: number): 'success' | 'warning' | 'danger' | 'info' | '' {
  switch (status) {
    case 0:
      return 'warning';
    case 1:
    case 2:
      return '';
    case 3:
      return 'success';
    case 4:
      return 'info';
    case 5:
    case 6:
      return 'danger';
    default:
      return 'info';
  }
}
