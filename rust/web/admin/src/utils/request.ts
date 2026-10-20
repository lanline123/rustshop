/**
 * 管理端请求实例。
 *
 * 共享基座见 `@tigshop/shared/request`；这里只注入 admin 端差异：
 * - baseURL `/adminapi`
 * - token 键 `tig_token`
 * - 附加头 `X-ADMIN-TYPE` / `X-CLIENT-TYPE` / `X-Locale-Code`
 * - 401 清 token 并跳 `/login`
 *
 * 与原实现的差异：`X-Shop-Id` 不再在拦截器里每次读 localStorage，
 * 而是由登录/切换店铺时调用 `setShopId()` 显式设置（避免隐式全局状态）。
 * 为兼容旧行为，初始化时仍从 `tig_shop_id` 回填一次。
 */
import { ElMessage } from 'element-plus';
import { createRequest } from '@tigshop/shared/request';

const request = createRequest({
  baseURL: '/adminapi',
  tokenKey: 'tig_token',
  headers: {
    'X-ADMIN-TYPE': 'admin',
    'X-CLIENT-TYPE': 'admin',
    'X-Locale-Code': 'zh-CN',
  },
  loginPath: '/login',
  onError: (message) => ElMessage.error(message),
});

/** 店铺后台场景：切换店铺后调用，写入 `X-Shop-Id`。 */
export function setShopId(shopId: number | string | null) {
  request.setHeader('X-Shop-Id', shopId === null ? null : String(shopId));
  if (shopId === null) {
    localStorage.removeItem('tig_shop_id');
  } else {
    localStorage.setItem('tig_shop_id', String(shopId));
  }
}

// 兼容旧行为：启动时回填已持久化的店铺 ID
const persistedShopId = localStorage.getItem('tig_shop_id');
if (persistedShopId) {
  request.setHeader('X-Shop-Id', persistedShopId);
}

export default request;
