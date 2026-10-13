# TigShop Rust 重构版

基于原 **Java Spring Boot 3.3.4 + MyBatis Plus** 的 B2B2C 多商户商城
（`java-b2b2b-pro-v5.8.57`）重构为 **Rust + Axum + SQLx** 实现。

> **数据库零迁移**：Rust 版直接复用原 Java 系统的 MySQL 库表结构与数据
> （字段名通过 `#[sqlx(rename_all = "snake_case")]` 严格对齐）。

## 目录结构

```
rust/
├── Cargo.toml              # 工作区（workspace）与统一依赖版本
├── rust-toolchain.toml     # 固定工具链
├── config.yaml             # 运行配置（对应原 application.yaml）
├── Dockerfile              # 多阶段构建镜像
├── docker-compose.yml      # mysql + redis + rabbitmq + app
├── migrations/             # SQL 迁移（幂等）
├── docs/                   # 由工具生成的 API 契约与文档
│   ├── api-contract.json   #   精简契约（嵌入二进制）
│   ├── api-inventory.*     #   全量清单（json/csv/md）
│   ├── openapi.json        #   OpenAPI 3.1 骨架
│   └── api-missing.csv     #   未实现接口（开发待办）
├── tools/                  # Python 工具链
│   ├── schema_parser.py    #   pure.sql -> 权威 schema -> 实体
│   ├── java_api_parser.py  #   Java 注解 -> 接口契约
│   └── check_api_coverage.py  # Rust 路由 vs 契约 覆盖率
└── crates/
    ├── core/               # 领域层（原 tigshop-bean + tigshop-common）
    │   ├── error.rs        #   统一错误 + ApiResponse（兼容 ResultWrapper JSON）
    │   ├── types/          #   分页 PageQuery/PageResult、状态枚举
    │   ├── entities/       #   数据库实体（原 bean/model/**）
    │   └── models/         #   DTO/VO（原 bean/dto|query|vo）
    ├── common/             # 配置、日志（原 logback.xml）、工具函数
    ├── infrastructure/     # 数据库连接池、Repository、Redis、队列、存储
    ├── security/           # JWT、密码哈希、认证中间件
    ├── services/           # 业务服务（原 tigshop-service）
    │   ├── auth.rs  product.rs  cart.rs
    │   ├── order.rs payment.rs user.rs  paylog.rs
    ├── api/
    │   ├── openapi/        # API 契约与覆盖率端点（/openapi/*）
    │   ├── admin/          # 管理后台 API（原 tigshop-adminapi，/adminapi）
    │   └── public/         # 商城前台 API（原 tigshop-api，/api）
    └── bin/                # 可执行入口（双端口 8080/8181）
```

## 技术映射

| 原 Java | Rust | 说明 |
|---------|------|------|
| Spring Boot 3.3 (Tomcat) | Axum 0.8 + Tokio | 异步 Web 框架 |
| MyBatis Plus 3.5.7 | SQLx 0.8（编译期可选校验） | 类型安全 SQL |
| HikariCP | SQLx MySqlPool | 连接池，保留 `ONLY_FULL_GROUP_BY` 兼容 |
| Spring Security + java-jwt | jsonwebtoken (HS256) + 中间件 | `require_auth` |
| BCryptPasswordEncoder | bcrypt（兼容历史 `$2a$` 密码）+ Argon2 | 双算法识别 |
| Spring Data Redis | redis-rs ConnectionManager | `get_or_load` 缓存模式 |
| Spring AMQP | 队列抽象（预留 amqpd） | 事件/异步任务 |
| Jackson | serde_json | 字段命名兼容 camelCase |
| ResultWrapper / BizException | `tigshop_core::Error` + `ApiResponse` | 响应 JSON 完全一致 |
| Lombok @Data | derive(Serialize, Deserialize, FromRow) | |
| BigDecimal | rust_decimal | 金额精确运算 |
| logback.xml | tracing + tracing-appender | 按天切割，保留 30 天 |

## 快速开始

### 本地开发

```powershell
# 1. 准备 Rust 工具链（一次性）
#    已通过 rustup 安装，或将安装脚本加入 PATH

# 2. 配置连接（编辑 rust/config.yaml 或环境变量）
$env:TIG_DATASOURCE__URL = "mysql://root:root@127.0.0.1:3306/tigshop-java"
$env:TIG_REDIS__HOST = "127.0.0.1"

# 3. 编译并运行
cd rust/crates
cargo run -p tigshop-server
```

### Docker 一键启动

```powershell
cd rust
docker compose up -d --build
# 管理端: http://localhost:8080/adminapi/health
# 商城端: http://localhost:8181/api/health
```

## API 对照

> 下表路径已与原 Java 契约逐条核对。完整清单见
> [docs/API-INVENTORY.md](docs/API-INVENTORY.md)，覆盖率见
> `python tools/check_api_coverage.py`。

### 管理端（前缀 `/adminapi`）

| 方法 | 路径 | 对应原 Java |
|------|------|------------|
| POST | `/adminapi/login/signin` | Spring Security `LoginFilter.setFilterProcessesUrl` |
| POST | `/adminapi/login/refresh` | JWT 刷新（Rust 新增） |
| GET | `/adminapi/health` | 健康检查（Rust 新增） |
| GET | `/adminapi/authority/adminUser/list` | `AdminUserController.list` |
| GET | `/adminapi/authority/adminUser/mineDetail` | `AdminUserController.mineDetail` |
| GET | `/adminapi/product/product/list` \| `detail` | `ProductController` |
| GET | `/adminapi/product/category/getAllCategory` | `CategoryController.getAllCategory` |
| GET | `/adminapi/product/brand/list` | `BrandController.list` |
| GET | `/adminapi/order/order/list` \| `detail` | `OrderController` |
| POST | `/adminapi/order/order/deliver` \| `cancelOrderByAdmin` | `OrderController` |
| GET/POST | `/adminapi/content/article/*` | `ArticleController` |
| GET/POST | `/adminapi/content/articleCategory/*` | `ArticleCategoryController` |
| GET/POST | `/adminapi/promotion/coupon/*` | `CouponController` |
| GET | `/adminapi/setting/shippingTpl/list` 等 | `ShippingTplController` 等 |
| GET | `/adminapi/user/userRank/list` 等 | `UserRankController` 等 |
| GET/POST | `/adminapi/promotion/seckill/*` | `SeckillController` |
| GET/POST | `/adminapi/promotion/timeDiscount/*` | `TimeDiscountController` |
| POST | `/adminapi/system/profile/password` | 修改密码（Rust 新增） |

### 商城端（前缀 `/api`）

| 方法 | 路径 | 对应原 Java |
|------|------|------------|
| GET | `/api/product/product/list` \| `detail` | `ProductController` |
| POST | `/api/user/login/signin` | Spring Security `ClientLoginFilter` |
| POST | `/api/user/regist/registAct` | `RegisterController.register` |
| GET | `/api/user/user/detail` | `UserController.detail` |
| GET | `/api/category/category/all` | `CategoryController.getAllCategory` |
| GET | `/api/cart/cart/list` | `CartController.list` |
| POST | `/api/cart/cart/addToCart` \| `updateItem` \| `updateCheck` \| `removeItem` \| `clear` | `CartController` |
| POST | `/api/order/check/submit` | `CheckController.submit` |
| GET | `/api/user/order/list` \| `detail` | `UserOrderController` |
| POST | `/api/user/order/cancelOrder` | `UserOrderController.cancelOrder` |
| GET/POST | `/api/user/coupon/*` | `UserCouponController` |
| GET | `/api/article/article/list` \| `newsInfo` | `ArticleController` |
| GET | `/api/user/pointsLog/list` | `PointsLogController.list` |
| POST | `/api/order/pay/notify/{payType}` | `PayController.notify` |

响应统一为原格式：

```json
{ "code": 0, "msg": "success", "data": {...}, "timestamp": 1718000000000 }
```

## 核心业务规则（已实现）

- **下单**：校验可购 → 逐行扣库存（`UPDATE ... WHERE stock >= n` 原子防超卖）
  → 任一行失败回滚已扣库存 → 写 `order`+`order_item`（事务）→ 清理购物车行
- **取消订单**：原子状态流转 `0 → 40`（防并发重复取消）→ 回滚库存
- **支付回调**：`pay_status = 2` 幂等（重复回调直接跳过），成功后 `order_status = 10`
- **发货**：状态流转 `10 → 20`（仅待发货可发）
- **登录**：参数校验 → 用户存在 → 停用检查 → BCrypt 校验 → 签发 access+refresh JWT
- **余额**：扣款用 `UPDATE ... WHERE balance >= ?` 原子防负

## API 契约与覆盖率（本仓库的接口基线）

原 Java 系统的接口**没有独立文档文件**——`java/05-API 接口文档大全.md`
只覆盖 8 个模块（约 38 KB），而实际接口有 1500+ 个。真正的契约在源码里：
每个控制器都带 Swagger `@Operation` / `@Tag` 注解。

因此本仓库把「从 Java 注解提取接口清单」做成了工具，并把它作为 Rust 重写的
验收基线：

```powershell
python tools/java_api_parser.py --all        # 生成契约与文档
python tools/check_api_coverage.py           # 计算 Rust 实现覆盖率
python tools/check_api_coverage.py --strict  # CI：有未实现接口则失败
```

### 提取结果（v5.8.57 权威数字）

| 指标 | 数值 |
|------|------|
| 控制器 | **251**（含 2 个 Spring Security 登录过滤器） |
| HTTP 接口 | **1564** |
| 有 `@Operation` 文档 | 1529（**97.8%**） |
| 带权限表达式 | 864 |
| 解析失败路径 | **0** |

按模块：`adminapi` 1231 条、`api` 333 条。

> 注：`--summary` 首次报告 1561 条，修复解析器后为 1564 条。
> 差异来自 `@GetMapping({"/a","/b"})` 多路径映射展开与数组形式的
> `@RequestMapping({"/x"})`。

### 生成物

| 文件 | 用途 |
|------|------|
| `docs/api-contract.json` | 精简契约，**嵌入 Rust 二进制**（555 KB） |
| `docs/api-inventory.json` | 全量清单，含参数、返回类型、源码行号（2.4 MB） |
| `docs/api-inventory.csv` | 表格版，可直接导入 Excel |
| `docs/API-INVENTORY.md` | 人读版，按模块/Tag 分组 |
| `docs/openapi.json` | OpenAPI 3.1 骨架 |
| `docs/api-missing.csv` | 未实现接口清单（开发待办） |

### 运行时查询

服务启动后可直接查询契约与进度（管理端与商城端各自挂载）：

| 路由 | 用途 |
|------|------|
| `GET /openapi/summary` | 覆盖率汇总（适合 CI/监控断言） |
| `GET /openapi/coverage` | 完整报告：按模块 + 差集 |
| `GET /openapi/missing` | 未实现接口清单 |
| `GET /openapi/contract` | 分页查询原接口（`module`/`tag`/`keyword`） |

覆盖率不是手填的：`crates/api/{admin,public}` 各自声明已注册路由
（`implemented_routes()`），`tools/check_api_coverage.py` 再从源码**静态提取**
axum 路由交叉校验，两边不一致会暴露出来。

### ⚠️ 已修正：路由路径与原契约不符（P0）

**问题**：早期实现的路由路径是**臆造的**，与 Java 契约不匹配。
例如订单域写成 `/adminapi/order/list`，而真实路径是
`/adminapi/order/order/list`（控制器前缀重复一次）；购物车写成
`/api/cart/list`，真实为 `/api/cart/cart/list`。

**影响**：这类错误 `cargo check` 与单元测试**完全无法发现**——路由字符串
不参与编译校验。前端按原契约调用会直接 404，且因为「接口存在」而难以定位。

**修正**：以 `docs/api-contract.json` 为准逐条对齐，并把校验固化为工具：

| 修正前 | 修正后（真实路径） |
|--------|-------------------|
| `/adminapi/order/list` | `/adminapi/order/order/list` |
| `/adminapi/order/ship` | `/adminapi/order/order/deliver` |
| `/adminapi/product/list` | `/adminapi/product/product/list` |
| `/adminapi/product/category/tree` | `/adminapi/product/category/getAllCategory` |
| `/adminapi/login` | `/adminapi/login/signin` |
| `/api/cart/add` | `/api/cart/cart/addToCart` |
| `/api/order/create` | `/api/order/check/submit` |
| `/api/order/list` | `/api/user/order/list` |
| `/api/user/login` | `/api/user/login/signin` |
| `/api/user/register` | `/api/user/regist/registAct` |

同时移除了契约中不存在的端点（如 `/adminapi/promotion/coupon/statusCount`、
`/api/cart/update` 的 PUT 方法），并把 Rust 新增端点
（秒杀/限时折扣专区分页、支付回调等）在文档中明确标注为 `extra_routes`，
避免「自造接口」被误当成已实现。

### 🔧 解析器修复记录

初版解析器有 3 类真实缺陷，均已被自动化测试锁定：

1. **包裹括号**：`@RequestMapping(("/api/user/account"))` 被解析成
   `("/api/user/account")`，影响 **111** 条接口。
2. **数组形式**：`@RequestMapping({"/api/common/verification"})` 与
   `@GetMapping({"/a","/b"})` 未展开。
3. **权限表达式**：`hasPermissionOrSelf('x',#id)`、`isPlatformSuperAdmin()`、
   `permitAll()` 未清洗，权限串里残留注解语法。

登录端点也一度缺失——它不走 `@RestController`，而由 Spring Security 的
`setFilterProcessesUrl` 注册，现单独解析 `tigshop-security` 模块。

## 迁移进度（如实评估）

> **重要更正**：早期版本的本节曾把骨架写成"已完成"。经与
> `java/database/pure.sql`（322 张表，权威 schema）逐字段核对后，实际情况如下。

### 规模对比

| 维度 | Java 原系统 | Rust 现状 | 覆盖率 |
|------|------------|-----------|--------|
| 代码量 | 3,503 文件 / 270,801 行 | 110 文件 / 21,655 行 | ~8% |
| 实体 model | 268 | **321**（全部表） | **100%** ✅ |
| HTTP 接口 | **1564** | **81** | **5.2%** |
| — adminapi | 1231 | 48 | 3.9% |
| — api | 333 | 33 | 9.9% |

> 接口覆盖率由 `tools/check_api_coverage.py` 实测，可用
> `--json` 导出 `docs/api-coverage.json` 复核。

> 实体层已达 100%：`pure.sql` 的 322 张表（含增量补丁后 321 张有效表）
> 全部生成 Rust 结构体，字段名与可空性均对齐真实库。

### ✅ 已解决：实体与真实库表不匹配（P0）

**问题**：早期手工编写的 21 个实体中，有 10 个字段名与真实库不符
（如 `ShippingTpl.tpl_id` 实际应为 `shipping_tpl_id`），核心表普遍缺列
（`Order` 漏 23/83、`Product` 漏 22/81）。因仓储层用运行时 `query_as::<_, T>()`
而非编译期 `query!` 宏，`cargo check` 与单元测试**无法发现**这类错误，
一旦连接真实 MySQL 即大面积报 `Unknown column`。

**解决**：新增 `tools/schema_parser.py`，从 `pure.sql` + 全部 63 个增量补丁
（含 125 条 `PREPARE` 包裹的动态 DDL）解析出 v5.8.57 的**最终表结构**，
自动生成全部实体：

```powershell
python tools/schema_parser.py --dump-tables       # 列出 321 张表及列数
python tools/schema_parser.py --table product     # 查看某表最终列定义
python tools/schema_parser.py --gen-entities      # 生成全部实体
```

关键设计：
- **可空性严格对齐**：NOT NULL 列用裸类型（`i32`/`String`/`Decimal`），
  可空列才用 `Option<T>`（实测 66% 列 NOT NULL / 33% 可空）。
  早期"全部包 Option"的做法会导致业务代码满屏 `unwrap_or_default`。
- **结构与业务分离**：`entities/*.rs` 由生成器产出（勿手改），
  业务方法（`is_on_sale`/`available_stock`/`status_label` 等）
  集中在手工维护的 `entities/business.rs`，重新生成不会覆盖。
- **支持全部 DDL 形态**：`CREATE TABLE [LIKE]` / `ALTER ADD|MODIFY|CHANGE|DROP` /
  `DROP TABLE` / 多组 `RENAME TABLE` / 动态 DDL 字符串提取。

### 🛡️ 防回归机制（两道自动化检查）

实体字段漂移之所以能逃过编译，根因是 SQL 列名不参与编译校验。
为此补了两道检查：

**1. `tools/check_sql_columns.py` — 仓储层 SQL 列名静态校验**

扫描 `infrastructure/src/db/repo/*.rs` 中的 SQL 字面量，逐一对权威 schema
校验表名与列名。**该工具在本轮修复中实际找出了 6 处真实错误**
（`logistics.rs` 的 `tpl_id`/`tpl_name`/`company_id`/`type_id`，
`member.rs` 的 `upgrade_amount`/`sort`/`tag_id`），这些错误 `cargo check`
全部漏过。

```powershell
python tools/check_sql_columns.py            # 报告
python tools/check_sql_columns.py --strict   # CI 模式（有问题则非零退出）
```

**2. `crates/core/tests/schema_consistency.rs` — 实体字段快照比对**

`tools/gen_schema_snapshot.py` 把 27 张核心表的 546 个列导出为
`tests/schema_snapshot.txt`，Rust 测试断言实体**未缺少**任何快照列。
字段一旦漂移，`cargo test` 立即失败并提示重新生成。

```powershell
python tools/gen_schema_snapshot.py   # 刷新快照（schema 升级后）
cargo test -p tigshop-core --test schema_consistency
```

**3. `tools/check_api_coverage.py` — 接口契约覆盖率**

从 Java 控制器抽取全部 `@RequestMapping` 组合，与 Rust 侧
`implemented_routes()` 显式声明比对，输出覆盖率与缺失清单：

```powershell
python tools/check_api_coverage.py    # 当前：已实现 81 / 1564（5.2%）
```

### 完成度清单

- [x] 工作区与依赖治理（统一 workspace 依赖版本）
- [x] 基础框架：配置/日志/JWT/密码/缓存降级/Docker
- [x] **321 张表的实体全覆盖**（自动生成，字段名与可空性均对齐真实库）
- [x] 核心链路服务：auth / product / cart / order / user / catalog /
      content / coupon / member / logistics / payment(trait)
- [x] 仓储层 SQL 列名校验通过（81 条语句，0 未知表 / 0 未知列）
- [x] 防回归机制：schema 快照测试（27 张表 / 546 列）+ SQL 列名校验
      + API 契约覆盖率工具 + 契约完整性测试
- [x] **API 契约基线**：1564 个接口从 Java 注解提取，路径/权限/文档齐全，
      并已修正早期臆造路由（详见上文 P0 章节）
- [ ] **接口实现：81 / 1564（5.2%）** —— 这是最关键的缺口。
      可用 `python tools/check_api_coverage.py --missing-csv docs/api-missing.csv`
      导出待办清单，按模块推进
- [x] **营销域垂直切片 1：限时秒杀**（`seckill` + `seckill_item`）
      —— 实体/仓储/服务/API/测试全链路，含防超卖原子扣减
- [x] **营销域垂直切片 2：限时折扣**（`time_discount` + `time_discount_item`）
      —— 三种折扣类型（折扣率/立减/固定价）、**跨活动冲突校验**
      （同商品同时间窗不得同时参与限时折扣与秒杀）
- [x] **财务域垂直切片 1：支付日志**（`paylog` + `paylog_refund`）
      —— 支付单复用判定（含定金预售例外）、**分账渠道快照不可变绑定**、
      按 `order_type` 隔离三类业务（商品订单/充值单/付费会员）、
      退款超额校验
- [x] **财务域垂直切片 2：退款申请**（`refund_apply` + `refund_log`）
      —— **可退额度 = 实付 - 已占用**（已取消/已拒绝的申请不再占用额度）、
      审核三态（处理中/退回售后/拒绝）、拒绝原因必填 ≤255 字、
      条件更新防并发重复审核、线下转账确认留痕
- [x] **支付渠道切片：余额支付**（`balance_pay`）
      —— 首个真实落地的支付渠道（站内余额直接扣减，无需外部 SDK）：
      抵扣上限 `min(应付, 余额)`、原子扣款防透支（`WHERE balance >= ?`）、
      全额付清走 `mark_paid` / 部分抵扣走 `apply_partial_payment`、
      失败回滚余额与流水、退款归属校验防越权加钱
      ⚠️ 关键陷阱（已由测试钉死）：`paylog.pay_status=1` 表已付，
      而 `order.pay_status=2` 表已付 —— 两套编号不同，混用会致流水与订单不一致
- [ ] 营销域其余部分：拼团 `groupon*`、优惠券发放批次、礼品卡
      `gift_card*`、积分兑换 `points_exchange`、抽奖 `lottery*`、
      砍价 `bargain*`、买家秀 `buyer_show*`、签到 `sign*`
- [ ] 财务域其余部分：结算 `statement*`、提现 `user_withdraw_apply`、
      发票 `user_invoice`、分账 `profit_sharing_*`
- [ ] 仓储层：15 / 248（需按域补全，建议改用 `sqlx::query!` 编译期校验）
- [ ] 业务域补全：`finance`(26 表) `setting`(21) `o2o`(20)
      `vendor`(19) `purchase`(12) `shop`(10) `deposit`(9) `salesman`(9)
      `lang`(8) `log`(8) `premium`(7) `decorate`(6) `im`(6) `wholesale`(5)
      `merchant`(4) `staff`(4) `wechat`(4) `print`(3) `video`(2) `cart`(2)
      `panel`(1) —— 实体已就绪，缺 service/repo/api 三层
- [ ] 支付渠道实现（Java 有 43 个 pay 服务，Rust 0 个）
      **注意：`/api/order/pay/notify/{payType}` 当前未做渠道签名校验，
      生产前必须补齐——回调端点无签名校验等于可被伪造支付成功**
- [ ] 消息队列发布/消费（现仅有 `health_check` 空壳）
- [ ] 对象存储 OSS/COS/S3/七牛（现仅本地）
- [ ] Elasticsearch 搜索（现仅有配置结构体）
- [ ] 定时任务、Feign 远程调用、滑块验证码、OAuth、短信、
      Excel 导入导出、视频首帧、IM/WebSocket、多语言
- [ ] 若干 `TODO(域实现者)` 占位（catalog 分类树、coupon 领取规则、
      logistics 区域运费、member 等级升级等）
- [ ] 前端接口全量回归测试

## 验证状态

```powershell
cd rust/crates
cargo check --workspace --all-targets -j 2   # ✅ 0 error / 0 warning
cargo test  --workspace -j 2                 # ✅ 206 passed / 0 failed

cd rust
python tools/check_sql_columns.py --strict   # ✅ 0 未知表 / 0 未知列
python tools/check_api_coverage.py           # 已实现 108 / 1564（6.9%）
python tools/java_api_parser.py --check      # ✅ 0 畸形路径
```

契约相关的自动化测试：

| 测试文件 | 锁定内容 |
|----------|----------|
| `crates/api/openapi/tests/contract_integrity.rs` | 路径无畸形、模块前缀正确、文档覆盖率 ≥95%、权限已清洗、登录端点存在 |
| `crates/api/openapi/tests/http_endpoints.rs` | `/openapi/*` 真实 HTTP 行为（分页、过滤、覆盖率一致性） |

> 编译与单测通过**仅证明语法与纯逻辑正确**，不校验 SQL 列名与路由路径。
> SQL 列名由 `tools/check_sql_columns.py` 兜底；路由路径由
> `tools/check_api_coverage.py` 兜底。

注意：本机内存较低（可用 <2GB 时 rustc 报 os error 1455/1450），
编译请用 `-j 1` 或 `-j 2` 限制并行度。**不要并发跑两个 cargo 命令**——
会破坏 `target/` 元数据，报 `invalid metadata files for crate`。

## 工具一览

| 工具 | 作用 |
|------|------|
| `tools/schema_parser.py` | 解析 pure.sql + 增量补丁为权威 schema；生成全部实体 |
| `tools/gen_schema_snapshot.py` | 导出核心表列快照，供 Rust 测试比对 |
| `tools/check_sql_columns.py` | 静态校验仓储层 SQL 的表名/列名与 schema 一致 |
| `tools/java_api_parser.py` | 从 Java 注解抽取接口清单，生成 `docs/` 全部契约文件 |
| `tools/check_api_coverage.py` | 静态提取 Rust 路由，与原契约比对并输出覆盖率 |

### 新增接口的标准流程

每实现一个接口，必须让契约与实现保持一致，否则 CI 会暴露差异：

```powershell
# 1. 在 docs/api-contract.json 中查到目标接口的真实路径
python tools/check_api_coverage.py --missing-csv docs/api-missing.csv
#    （在 docs/api-missing.csv 中按模块/关键词筛选待办）

# 2. 按契约路径注册路由，并在同文件 implemented_routes() 中登记

# 3. 校验：多余路由与未实现接口都会列出来
python tools/check_api_coverage.py

# 4. 回归
cd crates; cargo test --workspace -j 2
```

升级数据库版本时（新增 `*update.sql`）：

```powershell
python tools/schema_parser.py --gen-entities      # 1. 重新生成实体
python tools/gen_schema_snapshot.py               # 2. 刷新快照
python tools/check_sql_columns.py --strict        # 3. 校验仓储层 SQL
python tools/java_api_parser.py --all             # 4. 刷新接口契约
python tools/check_api_coverage.py                # 5. 覆盖率
cd crates
cargo check --workspace --all-targets -j 2        # 6. 编译
cargo test --workspace -j 2                       # 7. 测试
```

## 测试

```powershell
cd rust/crates
cargo test --workspace    # 单元测试（实体规则/JWT/密码/金额计算/契约完整性）
cargo clippy --workspace  # 静态检查（workspace 级 deny warnings）
```
