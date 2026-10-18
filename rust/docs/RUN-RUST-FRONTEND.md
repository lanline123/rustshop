# 仅用 Rust 后端 + 现有前端运行指南

> 目标：**不改前端代码**，用 `rust/` 的后端替换原 Java 服务。
> 本文记录本地验证过的步骤（Windows + MySQL 8.4），以及已做的兼容层改动和已知缺口。

## 0. 前置

- Rust 工具链（`rustup` / `cargo`）
- MySQL 8.x（本地验证用 8.4.9；`pure.sql` 也可导入 8.0）
- 可选：Redis（连不上会自动降级为无缓存模式，不阻塞启动）
- Node/pnpm：仅用于构建前端静态资源（前端代码不改）

## 1. 准备数据库

`rust/config.yaml` 默认连接 `mysql://root:root@127.0.0.1:3306/tigshop-java`。

```sql
CREATE DATABASE `tigshop-java` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

导入完整表结构（`pure.sql` 只有结构；`demo.sql` 带演示数据，含 admin/admin123 账号）：

```bash
mysql -h 127.0.0.1 -u root -p --default-character-set=utf8mb4 tigshop-java \
  -e "source D:/tigshop/java-b2b2b-pro-v5.8.57/java/database/demo.sql"
```

### 1.1 关键：把 `INT UNSIGNED` 归一化为 SIGNED

Java dump 里大量主键/状态列是 `INT UNSIGNED`，而 Rust 模型按 `i32/i64` 解码，sqlx 在
运行时会报 `mismatched types ... INT UNSIGNED`。Java 端被替换后可以安全地把这些列改成有符号
（dump 无外键约束；超范围哨兵值会被钳制到有符号上限）：

```bash
cd rust
python tools/compat/mysql_signed_schema.py gen  ../alters.sql
python tools/compat/mysql_signed_schema.py apply ../alters.sql
```

验证：`SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='tigshop-java' AND COLUMN_TYPE LIKE '%unsigned%';`
应为 `0`。

> 若换库/重新导入 dump，需要重新执行 1.1。

## 2. 启动后端

```bash
cd rust
cargo build -p tigshop-server          # 或 cargo run -p tigshop-server
./crates/target/debug/tigshop.exe
```

- 管理端：`http://127.0.0.1:8080/adminapi/...`
- 商城端：`http://127.0.0.1:8181/api/...`
- 健康检查：`http://127.0.0.1:8080/adminapi/health`、`http://127.0.0.1:8181/api/health`
- Redis 不存在时日志会提示「降级为无缓存模式」，可继续使用。

常用环境变量：

| 变量 | 说明 |
|---|---|
| `TIG_DATASOURCE__URL` | MySQL URL（覆盖 config.yaml） |
| `TIG_JWT__SECRET` | 生产必须替换 |
| `TIG_FILE__UPLOAD_DIR` | 本地上传目录，默认 `./uploads` |
| `RUST_LOG` | `info,tigshop=info,sqlx=warn` |

上传文件由后端直接提供：`GET /static/<kind>/<file>`（管理端 8080、商城端 8181 均可）。

## 3. 前端部署（nginx 示例）

前端代码保持不变，只改部署代理：

```nginx
# 管理后台静态资源（pnpm build 产物，base 为 /admin）
location /admin/ {
    root /srv/tigshop;               # /srv/tigshop/admin/...
    try_files $uri $uri/ /admin/index.html;
}
# 后台 API
location /adminapi/ {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
# 上传文件（后端本地存储）
location /static/ {
    proxy_pass http://127.0.0.1:8080;
}
# 商城 PC / H5（Nuxt .output/public 或 uniapp H5 构建产物）
location / {
    root /srv/tigshop/pc;
    try_files $uri $uri/ /index.html;
}
location /api/ {
    proxy_pass http://127.0.0.1:8181;
}
```

前端环境（`java/view/admin/.env.production`）保持 `VITE_REQUEST_URL_PREFIX=/adminapi`、
`VITE_BASE_DIR=admin`；商城端 `VITE_API_PREFIX=/api`。

## 4. 冒烟验证清单

```bash
# 1) 登录（返回 data.token / data.adminType，与原 Java 一致）
curl -s -X POST http://127.0.0.1:8080/adminapi/login/signin \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123","loginType":"password","captchaUid":"","verifyToken":null}'

# 2) 菜单权限（前端据此渲染路由）
curl -s -H "Authorization: Bearer $TOKEN" \
  http://127.0.0.1:8080/adminapi/authority/authority/getAuthority

# 3) 分页列表（records/size/current/pages 为 Java MyBatis-Plus 形状）
curl -s -H "Authorization: Bearer $TOKEN" \
  'http://127.0.0.1:8080/adminapi/product/product/list?page=1&size=15'

# 4) 图片上传（返回 /static/...；相册列表可见）
curl -s -H "Authorization: Bearer $TOKEN" \
  -F 'file=@logo.png;type=image/png' \
  'http://127.0.0.1:8080/adminapi/setting/galleryPic/uploadImg?galleryId=0'

# 5) 静态文件可访问
curl -I http://127.0.0.1:8080/static/gallery/<file>.png
```

## 5. 本轮为「前端不变」做的兼容层改动

| 改动 | 原因 |
|---|---|
| 登录响应补 `token` / `adminType` | Java `LoginVO` 只有这两个字段，前端读 `result.token` |
| `PageResult` 序列化同时输出 `records/total/size/current/pages` | 前端 `useListRequest` 读 `result.records` |
| 请求参数接受 `size`（兼容 `pageSize`） | 前端分页参数名是 `size` |
| 响应体同时输出 `msg` 与 `message` | Java `AjaxResult.message`，前端错误提示读 `message` |
| `/adminapi/login/signin`、`/adminapi/health` 挂到正确前缀 | 之前注册在根路径，前端请求 404 |
| `/api/...` 公开路由（登录/注册/商品列表）挂到 `/api` | 同上 |
| `/static` 静态文件服务 + 本地上传目录 | 前端上传组件依赖返回可访问 URL |
| `galleryPic/uploadImg`、`galleryVideoInfo/uploadVideo` 真正落盘 | 图片/视频上传是后台日常操作 |
| MySQL unsigned → signed 归一化 | sqlx 解码 `INT UNSIGNED` 直接失败 |
| 修正 `product_manage` 列表占位符/绑定数不一致 | 触发 MySQL `Malformed communication packet` |
| 修正 axum 0.8 路由参数写法（`{roomId}`） | 服务启动时 panic |

## 6. 已知缺口（不影响后端启动，但对应页面/流程不可用）

- **支付**：所有渠道 `create/createDepositPayment` 仍返回「渠道未迁移」，订单无法在线/线下/余额支付。
- **短信/邮件验证码**：发送类接口返回未迁移（账号密码登录可用）。
- **微信生态**：小程序码、公众号菜单推送、视频号直播、微信登录 / OAuth。
- **Excel 导入导出**：降级为 JSON 或明确报错。
- **IM WebSocket**：无长连接基础设施，客服消息不会实时推送（REST 接口可用）。
- **定时任务 / MQ**：订单自动关闭/收货、退款、分账、优惠券过期等后台流程未运行。
- **RBAC**：仅校验登录态，未实现方法级权限（前端菜单隐藏不等于接口受保护）。
- **多租户**：JWT 无 `shop_id`，部分接口用请求头/query 兜底，多店铺隔离不完整。
- **支付证书 / APP 更新包上传**：`setting/config/uploadFile`、`setting/appVersion/uploadFile` 仍是占位（可用同款 LocalStorage 补）。

## 7. 测试与门禁

```bash
cd rust/crates && cargo test --workspace      # 全绿
cd rust && python tools/check_structure.py --strict
python tools/check_route_conflicts.py
python tools/check_sql_columns.py --strict
```
