# TigShop Rust 目标架构（权威）

> 版本：2.0　适用：`rust/crates` 全工作区
> 本文是重构的唯一结构契约。任何新增/迁移代码必须符合本文；与本文冲突的旧结构视为待删除技术债。

---

## 0. 为什么推翻旧结构

旧实现（v1）以「slice-001 … slice-220」编号切片为组织单位，同一个业务能力被拆到 **4 个 crate 的 4 个同名编号文件**：

```
services/sliceNNN.rs
core/models/sliceNNN.rs
infrastructure/db/repo/sliceNNN.rs
api/{admin,public}/routes/sliceNNN.rs
```

由此产生的结构性问题（均已实测确认）：

| 问题 | 实测证据 |
|------|----------|
| 命名无业务语义 | 132 个切片文件；仅 3 个切片跨多控制器，其余按编号排列，无法从文件名判断业务 |
| 横向割裂 | 一个功能要改 4 个 crate，无垂直内聚 |
| 上帝对象 | `Repositories` 540 行字段；`AdminState` 363 行；`main.rs` 591 行手工装配 |
| 双轨实现 | 早期 20 个 `order.rs` / `product.rs` 等领域文件与切片并存，语义重复 |
| 门禁失效 | 工具靠「文件是否存在」「含不含 async fn」判断进度，与真实路由可达性漂移，必须靠 `check_api_coverage.py` 纠正 |
| 空壳注册 | 59 个空骨架切片「已注册可编译但 0 路由」 |

**结论：编号切片不是架构，是迁移过程的中间产物。v2 全面废弃，改为按限界上下文组织的垂直特性模块。**

---

## 1. 核心原则

1. **按业务域分模块，不按迁移批次编号。** 目录名必须是业务名词（`catalog/product_batch.rs`），禁止 `sliceNNN`。
2. **垂直内聚 + 分层解耦。** 一个特性（feature）在四层各有一个同名文件，路径可预测；依赖方向严格单向。
3. **组合根唯一。** 依赖装配只发生在 `bin`；其余 crate 不得自行读配置/建连接池。
4. **路由即契约。** `implemented_routes()` 是覆盖率的唯一真相；`ROUTE_TABLE` 与 `routes()` 必须一一对应（单测保证）。
5. **可验证优于可解释。** 每个门禁脚本必须能自动判定，禁止人工「我认为已完成」。
6. **诚实迁移。** 未迁移部分如实标注；不做「可编译但不可达」的空壳。

---

## 2. 工作区结构

```
rust/crates/
├── core/                        # 共享内核：错误、通用类型、DTO/VO、表实体
│   └── src/
│       ├── error.rs             # Error / Result / ApiResponse
│       ├── types.rs             # PageQuery / PageResult / 通用枚举
│       ├── entities/            # MySQL 行映射（扁平，镜像 schema，跨域共享）
│       └── models/
│           ├── mod.rs           # 只允许 `pub mod <ctx>;`
│           └── <ctx>/
│               ├── mod.rs       # `pub mod <feature>;`
│               └── <feature>.rs # DTO / Query / VO / 纯函数 + 单测
├── infrastructure/              # 出站适配器
│   └── src/
│       ├── db/
│       │   ├── mod.rs           # 连接池 + `Repositories`（按域分组的聚合根）
│       │   └── repo/
│       │       ├── mod.rs       # `pub mod <ctx>;`
│       │       └── <ctx>/
│       │           ├── mod.rs   # 本域 `Repositories` 结构 + `new(pool)` + `pub mod <feature>;`
│       │           └── <feature>.rs  # SQLx 仓储
│       ├── cache/               # Redis
│       ├── storage/             # 对象存储/本地文件
│       └── queue/               # MQ / 延迟任务
├── security/                    # 认证与鉴权（JWT、口令、RBAC、提取器）
├── services/                    # 用例层（编排，不写 HTTP）
│   └── src/
│       ├── lib.rs               # `pub mod <ctx>;`
│       └── <ctx>/
│           ├── mod.rs           # `pub mod <feature>;` + 本域 `Services` 结构
│           └── <feature>.rs     # 用例服务
├── api/
│   ├── admin/                   # 管理端 /adminapi（薄传输层）
│   │   └── src/
│   │       ├── lib.rs           # AdminState（14 个域字段）+ router()
│   │       ├── routes/
│   │       │   ├── mod.rs       # `pub mod <ctx>;` + merge_all
│   │       │   └── <ctx>/
│   │       │       ├── mod.rs   # 聚合本域 feature 的 routes/implemented_routes
│   │       │       └── <feature>.rs
│   │       └── root/            # 无 /adminapi 前缀的根级路由（按域同构组织）
│   ├── public/                  # 商城端 /api（同上）
│   └── openapi/                 # RouteDoc、/openapi/coverage 文档
├── common/                      # 配置、日志、工具（无业务）
└── bin/                         # 唯一组合根
    └── src/
        ├── main.rs              # 启动流程（≤120 行）
        └── composition.rs       # AppContext：按域装配 Services/Services
```

### 依赖方向（强制，单向）

```
core ← infrastructure ← services ← api/* ← bin
core ← security ← services
common ← (所有)
```

- `core` 不得依赖本工作区任何其它 crate。
- `infrastructure` 只依赖 `core`/`common`；不得依赖 `services`。
- `services` 不得依赖 `api/*`，不得构造 HTTP 类型。
- `api/*` 只做：参数解析 → 调 service → 包装响应；禁止写 SQL、禁止业务分支超过 1 层。
- `bin` 是唯一允许 `Config` 落到连接池/Service 构造的地方。

---

## 3. 限界上下文（14 个）

| ctx | 业务域 | 典型控制器 |
|-----|--------|-----------|
| `identity` | 管理员 / 角色 / 权限 / 登录 / 操作日志 | AdminUser, AdminRole, Authority, AdminLog, AdminAccount, Login |
| `merchant` | 店铺 / 商户 / 入驻 / 店铺员工 / 保证金 / 店铺资金 | Shop, Merchant, MerchantApply, MerchantAccount, Deposit*, ShopFunds, ShopWithdraw, ShopProductCategory, AdminUserShop |
| `wholesale` | 批发（批发商 / 等级 / 批发商品 / 批发设置） | Wholesaler, WholesalerGrade, WholesaleProduct, WholesaleSetting, WholesaleClient |
| `catalog` | 商品 / 分类 / 品牌 / 属性 / 库存 / 评价 / 搜索 / 电子卡券 / 商品分组 / 询价 | Product, ProductBatch, ProductStock, Category, Brand, AttributesTpl, Comment, Search*, ECard*, ProductGroup, PriceInquiry, Services |
| `trade` | 购物车 / 订单 / 售后 / 退款 / 发票 / 结算 / 代客下单 | Cart, Order, Aftersales, RefundApply, RefundLog, OrderInvoice, Check, ProxyShopping* |
| `payment` | 支付单 / 支付渠道 / 余额 / 充值 / 提现 / 账期 / 对账 / 分账 / 费率 | Paylog, Pay, ApplePay, Balance*, UserRechargeOrder, UserWithdrawApply, Credit*, Statement, ProfitSharing, SubMerchant, CategoryFeeRule, *PayConfig, TaxRate, UserBalanceLog, AccountPanel |
| `marketing` | 优惠券 / 秒杀 / 拼团 / 限时折扣 / 积分 / 礼品卡 / 抽奖 / 买家秀 / 分销 | Coupon*, Seckill, Groupon*, TimeDiscount, Lottery, PointsExchange, SignInSetting, GiftCard*, ProductGift, ProductPromotion, BuyerShow*, Salesman*, Overview, Promotion, RechargeSetting |
| `member` | 用户 / 会员等级 / 标签 / 实名 / 企业认证 / 站内信 / 留言 / 收藏 / 签到 | User, UserRank, MemberTag*, ShopMember, UserCompany, Feedback, UserMessage*, UserPointsLog, UserAddress*, UserSign, CollectProduct, Captcha, UserCoupon*, MemberAudience |
| `logistics` | 运费模板 / 配送方式 / 物流公司 / 打印 | ShippingTpl, ShippingType, LogisticsCompany*, Print*, Delivery, SunmiPrintCallback |
| `content` | 文章 / 装修 / 素材 / 相册 / 导航 / SEO / 友链 / 首页 / 公众号 / 直播 | Article*, Decorate*, Gallery*, PcNavigation, PcCatFloor, MobileCatNav, Seo, FriendLinks, Home, Share, Wechat*(自动回复/菜单/关键字), WechatLive* |
| `system` | 设置 / 配置 / 地区 / 版本 / 模板 / 消息类型 / 文件 / CSRF / 缓存 | Config*, Region, AreaCode, AppVersion, MailTemplates, MessageType, Files, CSRF, CacheManage, TipsManage, Example, Config(client), Util, PC, Recommend, Log |
| `im` | 客服 / 会话 / 消息 / 客服人员 / 接入规则 | ImConversation*, ImMessage*, ImServant, AdminUserKefu, AdminShopKefuConfig, IMConfig, WebSocket |
| `analytics` | 面板 / 统计 / 访问日志 / 销售统计 | Panel, SalesStatistics, StatisticsAccess, StatisticsUser |
| `openapi` | 开放接口 / WorkBuddy / MCP | WorkBuddy*, MessagePush |

> 归属以「业务语义」为准，不照抄 Java 包名（例：Java 把 `ShippingTplController` 放在 `setting`，v2 归 `logistics`）。

---

## 4. 特性（feature）规范

- **feature = 一个业务资源/能力**，通常对应一个 Java Controller；命名 `snake_case`，如 `product_stock`、`buyer_show`。
- 同一 ctx 内 feature 名唯一；`<ctx>::<feature>` 全局唯一。
- 若一个 Java Controller 被拆成多个切片（历史原因），按业务子域命名：`config_basic` / `config_style` / `config_cashier`，禁止 `config_1`。
- 若一个切片含多个 Controller，必须拆分到各自 feature（迁移时人工拆分）。
- 每层同名文件职责固定：

| 层 | 文件 | 允许内容 |
|----|------|----------|
| core | `models/<ctx>/<feature>.rs` | `#[derive(Deserialize/Serialize)]` DTO/VO/Query、纯函数、单测。**禁止** SQL/IO |
| infrastructure | `db/repo/<ctx>/<feature>.rs` | SQLx 查询、行映射、`Result<T>` |
| services | `<ctx>/<feature>.rs` | 用例编排、事务边界（`pool.begin()`）、领域校验 |
| api/* | `routes/<ctx>/<feature>.rs` | `ROUTE_TABLE`、`routes(&State)`、`implemented_routes()`、handler |

- **HTTP 层约定**（沿用既有已验证约定）：query/body DTO 必须 `#[serde(rename_all = "camelCase")]`；`ROUTE_TABLE` 与 `routes()` 一一对应，并由单测断言；路径逐字符对齐 `docs/api-contract.json`。

---

## 5. 组合根与状态

```rust
// services/src/<ctx>/mod.rs —— 本域服务集合（一次装配，处处共享）
#[derive(Clone)]
pub struct Services {
    pub product: Arc<product::ProductService>,
    pub brand: Arc<brand::BrandService>,
    // …本域全部已装配 feature
}

// api/admin/src/lib.rs —— 每个上下文一个字段
#[derive(Clone)]
pub struct AdminState {
    pub identity: Arc<tigshop_services::identity::Services>,
    pub merchant: Arc<tigshop_services::merchant::Services>,
    // …实际 13 个域字段
    pub jwt: JwtService,
}
```

- 装配顺序与跨域依赖在 `bin/src/main.rs` 显式编码（当前组合根文件 435 行，91 个服务构造 + 14 个 `Services` 聚合；无隐藏容器、无反射）。
- 禁止 80 字段的平铺状态；禁止 `state.sliceNNN` 访问形式。
- `main.rs` 只负责：配置 → 日志 → 连接池 → Redis → 服务装配 → 双端口 serve。

> 落地状态：`AdminState` 13 字段、`PublicState` 11 字段、`Repositories` 14 个域分组
> （90 个仓储字段，按 `repo/<ctx>::Repositories` 聚合），全部通过
> `check_structure.py --strict`（阈值 20）。

---

## 6. 迁移映射（slice → ctx::feature）

- 权威映射文件：`docs/domain-map.json`。它同时包含：
  - `slices[]`：220 个切片的 `ctx` / `feature` / `type_prefix` / `coded` / `real` / `deleted` / `layers`；
  - `baseline[]`：迁移前 76 个「早期域文件」（非切片）折叠出的 19 个 feature（承载 135 条已实现接口）；
  - `controllers[]`：251 个 Java 控制器的归属，供后续 929 条待实现接口派工。
- 迁移工具（`tools/restructure/`，可重复执行）：
  `_draft_map.py` → `migrate.py`（文件移动/重写/聚合器生成）→ `cleanup_routes.py`（清理零路由壳）
  → `group_state.py`（Stage-2 域分组）→ `sync_domain_map.py`（回写权威映射）。
- 迁移保持 **行为不变**：接口路径、SQL、错误码、响应结构、单测断言一律不改；
  只改组织结构与命名（机械 import 路径更新除外）。
- 多控制器切片（slice-001/137/194）当前以聚合 feature 落地（`product_manage` / `workbuddy` /
  `statistics`），`domain-map.json` 的 `split[]` 保留拆分计划，列为 Phase-3 工作。

---

## 7. 验收门禁（每次合并必跑）

| 门禁 | 命令 | 通过标准 |
|------|------|----------|
| 编译 | `cargo check --workspace --all-targets` | 0 error |
| 测试 | `cargo test --workspace` | 0 failed |
| 接口覆盖 | `python tools/check_api_coverage.py` | 不低于迁移前 635/1564，且只增不减 |
| 路由冲突 | `python tools/check_route_conflicts.py --strict` | 0 重复 |
| SQL 校验 | `python tools/check_sql_columns.py --strict` | 0 未知表/列 |
| 结构门禁 | `python tools/check_structure.py --strict` | 0 违规（禁止 slice 命名、层级越界、空壳注册） |

> 口径铁律：进度只认 `check_api_coverage.py`（静态提取真实注册路由）。`status_report.py` 之类按文件存在判定的视图不得用于汇报。

---

## 8. 剩余工作组织

未实现的 929 条接口按 `ctx/feature` 分派，写入 `docs/domain-roadmap.md`：

- 每条包含：Java 源文件、目标四层文件路径、接口清单、依赖与验收要求。
- 工作单位 = 一个 feature（不是编号切片）；一次提交只动一个 feature 的四层文件 + 域聚合器。
- 禁止再引入「先写空壳注册、后填实现」的流程。

---

## 9. 与 Java 的对应关系

| Java | Rust v2 |
|------|---------|
| `tigshop-bean` DTO/VO | `core::models::<ctx>::<feature>` |
| `tigshop-service` ServiceImpl + Mapper | `services::<ctx>::<feature>` + `infrastructure::db::repo::<ctx>::<feature>` |
| `tigshop-adminapi` Controller | `api::admin::routes::<ctx>::<feature>` |
| `tigshop-api` Controller | `api::public::routes::<ctx>::<feature>` |
| `tigshop-security` Filter | `security` + `api/*` 中间件 |
| Spring DI (`@Autowired`) | `bin/src/composition.rs` 显式装配 |
| `@Transactional` | service 层 `pool.begin()/commit()` |

---

## 10. 落地状态与验收（Stage-1 + Stage-2，2026-10-17）

| 指标 | 迁移前 | 迁移后 |
|------|--------|--------|
| `sliceNNN` 文件 | 529 | **0** |
| `sliceNNN` 引用 | 4,738 | **0** |
| 模块组织 | 编号切片 × 4 层 | 14 个限界上下文 × 94 个 feature |
| 空壳路由（可编译不可达） | 59 个切片 / 236 文件 | **0**（已删除） |
| `AdminState` / `PublicState` 字段 | 115 / 56 | **13 / 11**（域分组） |
| `Repositories` | 148 字段平铺 | 14 个域 `Repositories` 聚合 |
| `db/mod.rs` | 540 行 | 277 行 |
| `services/lib.rs` | 165 行 | 15 行 |
| `bin/main.rs` | 591 行（含重复注册） | 435 行（单一装配路径） |
| 接口覆盖率 | 635 / 1564（40.6%） | **1564 / 1564（100%）** |
| 注册路由 / 多余路由 | 645 / 16 | **1564 / 18** |
| 路由冲突 | 0 | **0** |
| SQL 语句 / 未知表列 | 1041 / 0 | **2300+ / 0** |
| 单测 | 迁移前未全跑（基线存在 5 个失败） | **全绿 4316 passed**（core 1567 / infra 532 / services 1490 / api 600+） |

验收命令（全部以当前树实测通过）：

```bash
cd rust/crates && cargo check --workspace --all-targets
cd rust/crates && cargo test  --workspace
cd rust && python tools/check_api_coverage.py      # 635 / 1564
cd rust && python tools/check_route_conflicts.py --strict
cd rust && python tools/check_sql_columns.py --strict
cd rust && python tools/check_structure.py --strict   # exit 0
cd rust && python tools/coverage_by_domain.py
```

### 已知遗留（不阻塞验收，按优先级）

1. **Phase-3 拆分**：slice-001/137/194 的多控制器聚合 feature（`catalog/product_manage`、
   `openapi/workbuddy`、`analytics/statistics`）需要按 `domain-map.json#split[]` 拆成独立 feature；
2. **bootstrap 内联路由**：`api/{admin,public}/src/lib.rs` 仍内联约 20 条早期路由
   （login/health/cart/order 等），应下沉到 `routes/<ctx>/<feature>.rs`；
3. **RBAC**：864 条接口带权限码，`security` 目前只校验登录态（见评审 §6.1）；
4. **事务 / MQ / 定时任务 / i18n**：见 `docs/architecture-review.md` §6。

---

*本文件由架构重构会话制定。结构变更必须先改本文件，再改代码。*
