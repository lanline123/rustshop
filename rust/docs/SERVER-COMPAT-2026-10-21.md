# 服务器兼容修复记录（2026-10-21）

> 后续修订固定策略见 [`COMPAT-STRATEGY.md`](COMPAT-STRATEGY.md)：
> 保留原版前端，只做「数据库兼容 SQL + Rust 契约兼容」，不切换 `rust/web`。

线上：`https://www.yjxj.cn/`（原版 PC）、`https://www.yjxj.cn/admin`（原版 admin）。
目标：**继续使用原版前端**，修 Rust 后端与数据库兼容问题，不改原版页面。

## 1. 数据库 schema 兼容（已执行）

服务器数据库直接导入 Java `pure.sql`，与 Rust sqlx 解码类型不一致。
已按顺序执行并验证：

| 问题 | 迁移脚本 | 结果 |
|---|---|---|
| `INT UNSIGNED` → Rust `i32` 解码失败 | `tools/compat/fix_unsigned_schema.sql` | 剩余 57 个均为 `DECIMAL UNSIGNED`，不影响 |
| `JSON` 列 → Rust `String` 解码失败 | `tools/compat/fix_json_schema.sql` | `remaining_json_columns = 0` |
| `DATE` 列 → Rust `Option<String>` 解码失败 | `tools/compat/fix_date_schema.sql` | `remaining_date_columns = 0` |

换库/重新导入 Java `pure.sql` 后，直接导入合并版脚本即可（可重复执行，末尾自带校验）：

```bash
mysql -h 127.0.0.1 -u root -p 数据库名 < rust/数据库兼容修正.sql
```

文件：`rust/数据库兼容修正.sql`

代表错误：

- `admin_id ... INT UNSIGNED`
- `extra_data ... JSON`
- `date ... DATE`

## 2. 后端响应结构调整（已改代码）

原版前端按 Java VO 读取，Rust 部分端点结构与 Java 不一致：

| 端点 | 问题 | 修复 |
|---|---|---|
| `GET /adminapi/panel/panel/index` | `realTimeData` 缺 `today*`/`yesterday*`/growth 字段，首页 `toString()` 崩溃 | `services/src/analytics/panel.rs` 补齐 `ConsoleData`/`RealTimeData` |
| `GET /adminapi/setting/logisticsCompany/list` | 返回裸数组、字段 `companyId/companyName`；前端按 `records` + `logisticsId/logisticsName` 读，订单页 `length` 崩溃 | `api/admin/src/routes/logistics/shipping_list.rs` 改为 `PageResult` + 原字段 |
| `GET /api/common/recommend/guessLike` | 返回 `PageResult`，前端直接 `(res || []).filter` | `services/src/system/recommend.rs` 改为 `Vec` |
| `GET /api/user/order/detail` | 返回 `{order,items}` 且 `OrderVO` 缺 `shopId`，原版详情读取平铺字段并请求 `shop/detail` | `api/public/src/lib.rs` 改为平铺 + items；`OrderVO` 增加 `shopId`；状态文案按 Java 0–5 映射 |
| `GET /adminapi/msg/adminMsg/msgCount?startTime=` | 空字符串不能解析为 `Option<i64>`，SSE 400 | `api/admin/src/routes/system/admin_msg.rs` 空串按 `None` |
| 所有 public optional 路由 | 原版前端未登录也发 `Authorization: Bearer null`，Rust 返回 401，登录页/注册页公共接口全挂 | 42 个 `optional_auth` 把非法 token 当游客；`require_auth` 仍 401 |
| `POST /api/search/search/getProduct` | 原版分类页传 `cat:"17"` 字符串，Rust `Option<i32>` 反序列化 400，列表空 | `ProductSearchClientQuery` 数字 ID 字段兼容字符串 |
| 业务错误响应 | Rust 把 `Biz` 错误也返回 HTTP 400，前端提示“Request failed with status code 400” | `AppError` 恢复 Java 语义：业务错误 HTTP 200 + code/msg |
| 统计/会员页 | `salesStatistics` 缺 `salesStatisticsData`、销售指标缺平铺字段、新增会员 `dateType` 字符串 400 | `services/src/analytics/*` 按原版 VO 补齐并兼容字符串参数 |

## 3. 新二进制构建与部署

服务器 5G 内存，release/dev 编译 `tigshop-server` 均被 OOM Kill。
已通过 GitHub Actions 构建并部署：

- 仓库：`https://github.com/lanline123/rustshop`（已清理，只保留 `rust/` + `.github` + `.gitignore`）
- 构建：`.github/workflows/build-rust.yml`（rockylinux:8 / glibc 2.28）
- 最新部署：Release `build-25`，对应提交 `b26f0dfbdf039bd79a2c95709123db7551f4e06c`
- 产物：`rustshop`（45,203,088 bytes，sha256 `64c591ba3082d5c5f5b376ce725dded1c127cff6f9c398a6cde6b983ae26f0c0`）
- 部署：备份旧二进制为 `tigshop.bak.before_build25`，替换 `/www/wwwroot/tigshop-rust/tigshop` 后重启
- 临时 nginx/Python shim 已停用并删除；请求直接走新 Rust 代码

> 原版前端未改；旧二进制只用于回滚。

## 4. 账号

原版 admin 数据库密码 hash 与用户给的 `admin123` 不匹配，已重置为
`admin123`（`admin_user.username='admin'`）。PC 账号 `123/123` 正常。

## 5. 登录/页面验证

- `POST /adminapi/login/signin` `admin/admin123` → `code=0`
- `POST /api/user/login/signin` `123/123` → `code=0`
- 原版 admin 页面（Edge headless 实测）：
  - `/admin/product/list`、`/admin/order/list`、`/admin/user/list`、
    `/admin/setting/base-basic`、`/admin/setting/logistics`、
    `/admin/promotion/coupon/list` 均无 JS 异常。
- 原版 PC 页面（Edge headless 实测）：
  - `/`、`/cart`、`/member`、`/member/order/list`、`/member/coupon/list`、
    `/member/address/list`、`/item/SN000002` 均无 JS 异常。
- `GET /api/common/log?click=1` → 200（DATE 修复后）。
- `GET /api/user/login/getWxLoginUrl` → 200，返回真实公众号二维码 URL + ticket。
- 管理端支付设置接口：
  - `GET /adminapi/setting/config/wechatPaySettings` → 200（微信支付已配置）；
  - `GET /adminapi/setting/config/aliPaySettings` → 200（支付宝 AppID 已配置）。
- 服务器 ES：`GET https://127.0.0.1:9200/_cluster/health` → `status=green`。
- `GET /api/order/check/getAvailablePaymentType` → 200（在线支付可用）。
- build-25 部署后实测：
  - 带 `Authorization: Bearer null` 的公共接口 → 200（登录页不再 401）；
  - `/member/login`、`/member/register` 无 401 / 网络错误；
  - `/list/?cat=17` 正常显示商品；`cat=34`（水墨画）数据库本身无商品；
  - `/admin/statistics-order|sale|goods|sale-targets|new-members` 无 JS 异常；
  - `sendMobileCode` → HTTP 200 + code 1001「短信通道未配置」。

## 6. GitHub 编译

仓库：`https://github.com/lanline123/rustshop`

Actions：`.github/workflows/build-rust.yml`（rockylinux:8 / glibc 2.28）。

最新成功产物：Release `build-25`，对应提交 `b26f0dfbdf039bd79a2c95709123db7551f4e06c`。

后续由人工手动触发构建，不自动构建。

`.github/workflows/build-rust.yml` 已存在：推送到 `main` 或手动
`workflow_dispatch` 后会在 `rockylinux:8` 构建 release 并发布 Release。
