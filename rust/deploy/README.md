# 部署说明（Rust 版）

## 1. Docker Compose（推荐先在有 Docker 的机器验证）

```bash
cd rust
docker compose up -d --build
```

- MySQL 首次启动只执行 `deploy/mysql-init/00-pure.sql`（由
  `python tools/compat/prepare_docker_mysql.py` 从 `../java/database/pure.sql`
  生成，已去除 `INT UNSIGNED`，避免 sqlx 解码失败）。
- 如需重新初始化数据库，删除 `mysql-data` volume 后重启。
- 应用默认端口：管理端 `8080`、C 端 `8181`；健康检查 `/adminapi/health`，
  指标 `/metrics`。

> 注意：本仓库开发机没有 Docker，compose 文件已修正但尚未实际拉起验证；
> 首次部署请保留 `docker compose logs -f mysql app` 观察初始化与迁移。

## 2. 前端 / Nginx

- `deploy/nginx.conf` 是参考反代配置：`/adminapi`、`/im`、`/static`、
  `/adminws` → 8080；`/api`、`/ws` → 8181；`/metrics` → 8080。
- **后端 8181 只提供 `/api` JSON，不返回任何 HTML**。PC 前端是独立 SPA，必须由 nginx 静态托管。
  （历史上的 Leptos SSR fallback 已于 2026-10-20 下线，见 `rust/crates/bin/src/main.rs` 注释。）

### 前端 dist 构建（重写路线，2026-10-20 起）

| 端 | 源码 | 构建 | 部署到 |
|---|---|---|---|
| 管理端 | `admin-web/` | `pnpm install && pnpm build` | `/usr/share/nginx/html/admin` |
| PC 商城 | `pc-web/` | `pnpm install && pnpm build` | `/usr/share/nginx/html/pc` |
| uniapp | `java/view/uniapp` | 各自小程序/H5 流程 | — |

> ⚠️ 原版前端 `java/view/{admin,Tigshop-Pc}` **不再是部署来源**，仅作为重写时的对照基准
> （详见 `docs/FRONTEND-MIGRATION-TASKS.md`）。
>
> ⚠️ `pc-web` 的 `vite.config.ts` 未设 `base`，默认按站点根 `/` 访问；若要挂 `/pc/` 子路径，
> 需 `vite build --base=/pc/` 并同步 `createWebHistory('/pc/')`。推荐给 PC 独立域名。

- 本地开发也可用 `nginx -c /path/to/deploy/nginx.conf` 验证路由。
- 开发期两端各起 Vite dev server：admin `:3000`（代理 `/adminapi`→8080）、
  pc `:3001`（代理 `/api`→8181）。

## 3. 环境变量（关键）

| 变量 | 说明 |
|---|---|
| `TIG_DATASOURCE__URL` | MySQL URL（compose 已注入） |
| `TIG_REDIS__HOST` / `TIG_RABBITMQ__HOST` | Redis / RabbitMQ |
| `TIG_JWT__SECRET` | 生产必须替换默认值 |
| `TIG_SERVER__ADMIN_PORT` / `TIG_SERVER__API_PORT` | 8080 / 8181 |
| `TIG_SCHEDULER__ENABLED` | 是否启用轮询任务 |
| `OPEN_TEMPLATE_BASE_URL` | 装修模板 OpenTemplate API（缺省 `https://api.tigshop.com/index`） |

## 4. 验收清单

1. `curl http://127.0.0.1:8080/adminapi/health` → `{"data":"UP",...}`
2. `curl http://127.0.0.1:8080/metrics` → Prometheus 文本
3. 管理端登录（`admin/admin123`）→ token/adminType 正常
4. 前端经 nginx 登录、商品列表、下单、支付回调、IM WS 正常
5. `docker compose ps` 中 mysql/app 为 healthy/running
