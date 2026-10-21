/**
 * 商城端请求实例。
 *
 * 共享基座见 `@tigshop/shared/request`；这里只注入 pc 端差异：
 * - baseURL `/api`
 * - token 键 `tig_user_token`
 * - 附加头 `X-CLIENT-TYPE: pc`
 * - 401 清 token 并跳 `/login`（带 redirect 回跳）
 */
import { ElMessage } from 'element-plus';
import { createRequest } from '@tigshop/shared/request';

const request = createRequest({
  baseURL: '/api',
  tokenKey: 'tig_user_token',
  headers: {
    'X-CLIENT-TYPE': 'pc',
  },
  loginPath: '/login',
  onError: (message) => ElMessage.error(message),
});

export default request;
