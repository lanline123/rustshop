# 服务器兼容修复记录（2026-10-21）

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

## 3. 临时兼容层（服务器 OOM 无法编译时）

服务器 5G 内存，release/dev 编译 `tigshop-server` 均被 OOM Kill。
在当前旧二进制上，通过 nginx + Python shim 让原版前端可用：

- 脚本：`/www/wwwroot/tigshop-rust/panel_index_shim.py`
- nginx：`/www/server/panel/vhost/nginx/extension/www.yjxj.cn/panel_index_shim.conf`
- 仅接管：
  - `/adminapi/panel/panel/index`
  - `/adminapi/setting/logisticsCompany/list`
  - `/adminapi/setting/logisticsCompanyConfig/list`
  - `/api/common/recommend/guessLike`
  - `/adminapi/msg/adminMsg/msgCount`（空 `startTime` 重定向补 0）

> 这是临时层；在内存 ≥16G 的机器/CI 编译新二进制后，应移除该 nginx include，
> 让请求直接走 Rust 新代码。

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

## 6. GitHub 编译

本机仓库没有配置 `git remote`，也无法提供 GitHub 凭据，因此当前只能本地提交。
在 GitHub 仓库配置好 remote 后执行：

```bash
git push origin master
```

`.github/workflows/build-rust.yml` 已存在：推送到 `main` 或手动
`workflow_dispatch` 后会在 `rockylinux:8` 构建 release 并发布 Release。
