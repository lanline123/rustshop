# TigShop Rust v2 架构评审（迁移前预检 / Pre-flight）

> 评审对象：`docs/ARCHITECTURE.md` v2.0、`tools/check_*.py`、`tools/restructure/migrate.py`、`docs/domain-map.json`、`docs/_migration-map.json`，以及迁移前的 `rust/crates` 现状。
> 评审基线：`git HEAD = df28f2ca`（"checkpoint: pre-restructure baseline -- cargo check green, coverage 635/1564 (40.6%)"）。
> 评审时刻：2026-10-17 22:12（Asia/Shanghai）。方法：静态读取 + 只读门禁脚本；**未运行 cargo**。
> 本评审的结论只认实测：所有数字都可按文中的文件/行号复算。
>
> ⚠️ 说明：评审期间 Stage-1 迁移（`tools/restructure/migrate.py`，PID 22576，22:11:40 启动）已经在并行执行。下文的 D1–D4 是对**工具源码**的静态判定；无论迁移是否已经跑完，这些缺陷都需要对照产物复核一遍再接受结果。

---

## 0. 结论（先给判决）

**方向是对的，执行物不合格；Stage-1 产物现在不能当作 v2 交付。**

- 用「限界上下文 + 四层同名 feature 文件」替换 `sliceNNN` 编号切片，是正确选择：旧结构 529 个 slice 文件、4,738 处 `sliceNNN` 引用、`AdminState` 115 字段、`Repositories` 148 字段、`main.rs` 591 行手工装配（实测，见 §4），这些确实是重构要解决的问题，而不是重构本身。
- 但**架构文档描述的终态与迁移工具实际产出的终态不是同一个东西**：`migrate.py` 自己写明 "Structure of state/repository fields stays flat in this stage (named `<ctx>_<feature>`)"（`migrate.py:2-11`），也就是把 115 字段的上帝状态改名为 `<ctx>_<feature>` 后**继续保留**，而 `ARCHITECTURE.md §5` 与 `check_structure.py:102`（`MAX_STATE_FIELDS = 20`）要求的是 14 个域字段。Stage-1 跑完，结构门禁**必然 FAIL**。
- 迁移工具本身至少有 4 处会直接导致产物编译不过的缺陷（D1–D4），而且 `main()` 没有任何产物校验步骤；「跑完 → cargo check 失败 → 在 19GB target 上排查」是最坏的执行顺序。
- 权威映射存在两份且互相矛盾：`domain-map.json`（`check_structure`/`coverage_by_domain`/`plan_domains` 读它）与 `_migration-map.json`（`migrate.py` 读它）在 220 个切片中有 **50 个 ctx/feature 不一致，其中 28 个是已编码切片、12 个连 ctx 都不同**（§5.1）。Stage-1 之后按域覆盖率与后续 929 条接口的路由表都会错位。
- 分类学（14 个上下文）覆盖不全：`ARCHITECTURE.md §3` 的表格只覆盖 179/221 个 Java 控制器；**42 个控制器 / 187 条接口（12.0%）在 14 个上下文里没有明确归属**（§1）。这会直接变成 929 条剩余接口的归属真空。
- 真正影响「这套结构能不能承载整个 B2B2B 单体」的问题是服务层耦合：Java 侧 288 个 ServiceImpl 之间有 **802 条依赖边、529 条跨包边、24 组双向依赖、21 个包的强连通分量**（§2）。v2 的「显式构造函数注入」表达不了这些环；现在 Rust 已迁移的 40.6% 只有 5 条跨域依赖边，不能用来证明这套组合根设计可行。

**判决：结构选型 PASS；`ARCHITECTURE.md` 作为目标契约 PARTIAL；迁移工具与门禁 FAIL。Stage-1 结果必须先修 M1–M4 再验收；M2/M5/M6 必须在开始剩余 929 条接口之前完成。**

---

## 1. 分类学：14 个限界上下文是否成立

### 1.1 覆盖度实测

用 `docs/api-contract.json`（1564 接口 / 251 控制器）对 `ARCHITECTURE.md §3` 的表格做匹配（按表中 token 字面 + `*` 通配，大小写不敏感）：

| 指标 | 数值 |
|---|---|
| 契约控制器总数 | 251（契约中出现者为 221 个） |
| §3 表格能明确归属的控制器 | 179 |
| **无任何上下文归属的控制器** | **42（187 条接口，12.0%）** |
| 已被覆盖的接口 | 1377 / 1564（88.0%） |

按上下文统计（含 `*` 通配匹配，仅统计表中明确点名的控制器）：

| ctx | 控制器 | 接口 | 判断 |
|---|---|---|---|
| identity | 6 | 54 | 偏小，可接受（登录/权限天然独立） |
| merchant | 16 | 125 | 合理 |
| wholesale | 5 | 30 | 小但内聚，保留 |
| catalog | 14 | 166 | 偏大（商品/分类/品牌/属性/库存/评价/搜索/卡券/询价/服务） |
| trade | 15 | 118 | 合理 |
| payment | 16 | 77 | 合理 |
| marketing | 31 | 224 | **过大**：优惠券/秒杀/拼团/折扣/抽奖/积分/礼品卡/买家秀/分销/充值设置 10+ 能力族 |
| member | 15 | 109 | 合理 |
| logistics | 8 | 45 | 合理 |
| content | 23 | 169 | **偏大**：文章/装修/素材/导航/SEO/友链/首页/公众号/直播 |
| system | 15 | 168 | **杂物间**：配置/地区/版本/模板/消息类型/文件/CSRF/缓存/示例/PC/推荐/日志 |
| im | 7 | 48 | 合理 |
| analytics | 4 | 16 | 小但独立，保留 |
| openapi | 4 | 28 | 小但独立，保留 |

结论：**边界数量（14）合理，但表的「典型控制器」列不是可执行契约**。它是按 Java 包名 + 业务语义混写的 prose，既不能机检，也已经漏掉 12% 的接口。`docs/domain-map.json` 的 `controllers[]`（251 条）补了这个洞，但 `ARCHITECTURE.md §3` 没有回链到它，且 §6 引用的权威映射文件名与实际使用的文件不一致（见 §5.1）。

### 1.2 未归属控制器与建议归属（42 个）

| 控制器（接口数） | 现在无归属的原因 | 建议 ctx | 说明 |
|---|---|---|---|
| `AfterSalesController`(10) | 表里写的是 `Aftersales`，与 Java 类名 `AfterSales` 拼写不一致，机检必然漏 | trade | 纯拼写问题，trade 已声明售后 |
| `UserOrderController`(9) / `OrderLogController`(2) / `OrderConfigController`(7) / `ShopOrderConfigController`(2) / `OrderInvoiceClientController`(3) | 表里只有 `Order`/`OrderInvoice`，未覆盖 *Client* 与配置类 | trade | |
| `UserInvoiceController`(7) / `UserInvoiceClientController`(4) | 同上 | trade | 发票属交易单据 |
| `ProductInventoryLogController`(1) / `StockWarningController`(10) | 表里 `ProductStock` 不含库存预警/流水 | catalog | 库存预警是 catalog 核心能力 |
| `UserPriceInquiryController`(4) | 表里 `PriceInquiry` 不含 Client 变体 | catalog | |
| `UserCouponController`(5) | 表里 `UserCoupon*` 在 member，而 `Coupon*` 在 marketing | marketing | 优惠券生命周期应单属 marketing，member 不应持券 |
| `LotteryActivityController`(8) / `ExchangeController`(3) | 表里 `Lottery`/`PointsExchange` 不含 Activity/Exchange | marketing | |
| `ProductStyleController`(13) | 表里没有；Java 包在 promotion，但语义是商品展示样式 | marketing（建议）或 content | 二选一必须写死，不然 084/085 两个已编码切片会被分到两个 ctx |
| `MerchantApplicationController`(10) | Java 包 `finance`，表里 payment 未列「进件」 | payment | 支付进件，不是商户入驻 |
| `MerchantClientController`(5) / `ShopClientController`(10) / `ShopClientCategoryController`(2) | 表里 `Merchant`/`Shop`/`ShopProductCategory` 不含 *Client* 变体 | merchant | |
| `AccountController`(4) / `VendorDepositShopController`(5) | 表里 `Deposit*` 不含 Vendor 变体 | merchant | 店铺资金/供应商保证金 |
| `AdminMsgController`(7) | 无「消息」上下文，被拆到 identity/member/openapi | 新增 `notification` 或放 identity | 建议建 15th ctx 或明确归 identity（后台消息） |
| `WechatAutoReplyController`(6) / `WechatKeywordRuleController`(5) / `WechatMenuController`(2) / `WechatKefuCapabilityController`(1) / `WechatVideoAccountController`(5) / `ApiWechatLiveController`(1) | 表里 `Wechat*（自动回复/菜单/关键字）` 与 `WechatLive*` 只覆盖了部分类名 | content | 建议把表中 token 改成精确类名/前缀通配 |
| `HuifuConfigController`(2) / `LakalaConfigController`(2) / `YeepayConfigController`(2) | 表里 `*PayConfig` 只覆盖 `XxxPayConfig`，不覆盖 `XxxConfig` | payment | 支付渠道配置 |
| `RechargeOrderController`(7) / `WithDrawApplyClientController`(7) | 表里是 `UserRechargeOrder`/`UserWithdrawApply`，与真实类名不一致 | payment | |
| `RegisterController`(1) / `CaptchaApiController`(3) / `PointsLogController`(1) / `UserCompanyClientController`(3) / `UserFeedbackClientController`(2) / `UserMemberTagController`(5) | 表里 `Captcha`/`UserCompany`/`Feedback`/`UserPointsLog`/`MemberTag*` 不含 Client/Ip 变体 | member | |
| `LoginFilter`(1) / `ClientLoginFilter`(1) | 表里 `Login` 不含 Filter 类 | identity | Filter 也是契约端点，不能漏 |
| `LicensedController`(3) / `ShortUrlController`(1) | 表里没有 | system | 许可证/短链属平台设施 |

> 建议把这张表直接变成 `domain-map.json` 的 `controllers[].ctx/feature`，并在 `check_structure.py` 里加一条门禁：**契约中的每个 (controller, module) 必须在 domain-map 中有 ctx/feature**（当前 251 条看似覆盖，但没有门禁保证以后不漂移）。

### 1.3 应拆/应并的上下文

- **marketing 必拆**：224+ 接口、31+ 控制器、10+ 能力族，且是交易链路（订单用券/秒杀/拼团/折扣）的最大入边来源。建议只保留 `promotion`（Coupon/Seckill/Groupon/TimeDiscount/Lottery/GiftCard/Points）与 `growth`（Salesman*/BuyerShow*/Overview）两个子域，否则 `marketing::Services` 会变成 ~33 个字段的新上帝对象（见 §4.4）。
- **system 是杂物间**：配置、地区、版本、模板、消息类型、文件、CSRF、缓存、PC/推荐/示例/日志都在里面。建议至少把「平台配置 + 文件/存储 + 版本/模板」与「地区/地址字典」分开，后者被几乎所有域的请求 DTO 使用，属于共享内核而不是某个业务上下文。
- **不要根据规模删小上下文**：wholesale(30)/analytics(16)/openapi(28)/im(48) 小但内聚，删了反而制造耦合。
- **跨域共享服务不是上下文**：`ConfigService` 在 Java 侧被 89 个 ServiceImpl 依赖、`AdminLogService` 20、`GalleryPicService` 17、`AdminMsgService` 16（§2.2 实测）。把 `ConfigService` 放进 `system`、`GalleryPic` 放进 `content`、`AdminLog` 放进 `identity`，会让 12 个上下文对 system/identity/content 产生「伪业务依赖」。建议在 `infrastructure`/`security`/共享 `support` 模块里定义 `Settings`、`AuditLog`、`MediaStore`、`Notify` 端口，由组合根注入，而不是跨 ctx 构造服务。

---

## 2. 分层与依赖方向：`core ← infrastructure ← services ← api ← bin` 是否可达

### 2.1 crate 级依赖方向：可达，且现状已基本满足

- 当前 `Cargo.toml` 依赖图与目标一致（`core` 不依赖工作区 crate；`infrastructure` 只依赖 core/common；`services` 不依赖 api；`bin` 是唯一装配点）。
- `check_structure.py --strict` 在迁移前后都报 **依赖方向违规 0**（评审期间实测）。
- 但有两处需要明确取舍，否则 `check_structure.py` 的 `ALLOWED_DEPS`（`check_structure.py:81-101`）会与文档不一致：
  1. `api/admin/Cargo.toml` 直接依赖 `tigshop-infrastructure`，而代码里只有一处注释引用（`api/admin/src/routes/slice208.rs:17`）。v2 §2 说 api 只做「参数解析 → 调 service」，应删掉这条依赖或明确「api 可用基础设施只读类型」。
  2. `core` 依赖 `sqlx`，且 `core/src/models` 里有 3 处 `sqlx::FromRow`（`core/src/models/slice003.rs:664,700`、`slice011.rs:152`）。v2 §4 说 core 模型「禁止 SQL/IO」。实体放 `core/entities` 用 sqlx 可以，但 DTO/VO 文件里出现 FromRow 应视为违规并在门禁里查。

### 2.2 真正的风险：服务层有环，显式构造函数注入表达不了

对 `java/tigshop-service` 的 288 个 `*ServiceImpl.java` 做依赖图（提取对其它 `*Service` 接口的引用，且该接口存在实现）：

| 指标 | 实测 |
|---|---|
| 服务间依赖边 | 802 |
| 跨 Java 包依赖边 | 529（66%） |
| 双向依赖（2-cycle） | **24 组** |
| 包级强连通分量 | **1 个 21 个包的大环**（authority/cart/content/decorate/deposit/finance/im/lang/merchant/msg/msgTemplate/oauth/order/pay/product/promotion/salesman/setting/shop/user/wholesale） |
| fan-in Top | ConfigService 89、ProductService 36、ProductSkuService 25、OrderService 22、AdminLogService 20、UserService 19、GalleryPicService 17、AdminMsgService 16 |
| fan-out Top | OrderCheckService 54、OrderService 44、ProductService 44、RefundApplyService 23 |

把其中与 v2 上下文相关的 2-cycle 拎出来（按 §3 的 ctx 映射）：

| Java 双向依赖 | v2 中会变成 | 影响 |
|---|---|---|
| `ShopService` ↔ `OrderService` | merchant ↔ trade | 构造函数注入无法同时构造两个 Arc（先有鸡还是先有蛋） |
| `ProductService` ↔ `UserService` | catalog ↔ member | 同上 |
| `ProductService` ↔ `TimeDiscountService` | catalog ↔ marketing | 同上 |
| `ProductService` ↔ `DepositLifecycleService` / `WholesaleProductConfigService` | catalog ↔ merchant / wholesale | 同上 |
| `OrderService` ↔ `GrouponPromotionRecordService` / `SalesmanOrderService` | trade ↔ marketing | 同上 |
| `PaymentService` ↔ `RefundApplyService` | payment ↔ trade | 同上 |
| `ConfigService` ↔ `DepositSettingService` / `HuifuConfigService` / `WechatOAuthService` | system ↔ merchant/payment/content | 配置服务成为全局环中心（fan-in 89） |
| `AccountService` ↔ `ShopWithdrawService` | payment ↔ merchant | 同上 |

**结论**：v2 §5 的示例 `trade::Services::new(repos, &catalog)` 对当前已迁移的 5 条跨域边（`cart→product`、`order→cart/product`、`slice027→012`、`slice029→001`、`slice030/092→010`）成立，但对剩余 929 条接口背后的 Java 服务图**不成立**。不能用「现在能编过」来证明组合根设计成立。

**建议（按优先级）**：

1. **端口/查询接口（首选）**：在 `core` 或独立 `contracts` crate 定义窄 trait，如 `CatalogQuery`（商品/库存/价格查询）、`SettingsPort`、`AuditPort`、`MediaPort`、`MemberQuery`。拥有方 ctx 实现，组合根以 `Arc<dyn ...>` 注入消费方。这样跨域边变成 DAG（消费方 → trait），环被切断，且可以在 `check_structure.py` 里强制「services 不得直接依赖其它 ctx 的 Services 结构」。
2. **共享内核服务（次选）**：Config/AuditLog/Media/Notify 这类 fan-in 两位数的服务不做成 ctx 内 Service，而是 `infrastructure` 里的可注入能力（Settings 可从 DB 读 + 进程内缓存）。
3. **两阶段初始化（最后手段）**：对确实互引的少数服务用 `Arc<OnceLock<...>>`/`Arc<Mutex<Option<...>>>` 打破构造顺序。代价是运行期才暴露装配错误，必须配启动自检。
4. 不要用「合并上下文」来消环：把 trade 和 merchant 合了会得到一个 300+ 接口的巨型域，比一个显式 trait 更糟。

### 2.3 数据库层：repo 不是「按表归属」的

v2 §2 把 `db/repo/<ctx>/<feature>.rs` 描述为按域分组。实测（对 `crates/infrastructure/src/db/repo` 全部 148 个文件抽取 SQL 表名）：

- **48 个 repo 文件跨 ≥2 个域前缀的表**。例如 `repo/slice002.rs`（订单）读 13 张表，覆盖 order/aftersales/comment/groupon/logistics_company/paylog/product/shop/user/wechat_live_order_rel；`repo/slice001.rs`（商品）读 30 张表，横跨 catalog/user/order/config/coupon/shop/shipping_tpl/short_url。
- 跨上下文**写**入较少但存在：按表前缀归属启发式检查，5 个 repo 文件同时写 ≥2 个 ctx 的表，例如 `repo/slice014.rs` 写 merchant(shop)/catalog(product)/identity(admin_user_shop)；`repo/slice137.rs` 写 catalog+content；`repo/slice108.rs` 写 catalog(product)+merchant(shop_product_category)。

这不是错误——模块化单体的库本来就是共享 schema——但**架构文档必须写清楚规则**，否则「按 ctx 分 repo」会让人误以为库也按域隔离：

- 允许：跨 ctx 的 SELECT/JOIN（读）；
- 禁止：跨 ctx 的 INSERT/UPDATE/DELETE，必须走拥有方 ctx 的 service（或事件）；
- 在 `check_structure.py` 加 SQL 级门禁：repo 文件里的写操作目标表必须属于本 ctx 的表清单（表清单放 `domain-map.json`）。

---

## 3. Feature 粒度：feature-per-controller 是否合适

### 3.1 数据

| 事实 | 数据来源 |
|---|---|
| 220 个切片，132 个有文件，73 个有真实路由，59 个是空壳（236 个文件） | `docs/_migration-map.json`、`slices.json` |
| 多控制器切片 5 个（已实现 3 个） | `slices.json`：slice-001（4 ctrl/43 API）、slice-137（4 ctrl/28）、slice-194（2 ctrl/4）、slice-112（2）、slice-158（2） |
| 同一 Java 控制器被拆到 ≥2 个切片：**13 个**（任务说明只列了 8 个） | `slices.json`：ConfigController 跨 4 个切片（187/188/189/207）；Search/User/AppVersion/BuyerShow/Account/ProductStyle/Region/SalesmanMaterial/Salesman/Comment/GiftCardGroups/DecorateDiscrete 各 2 个 |
| 跨 admin/public 两个模块的切片：14 个（已实现 4 个：001/004/021/140） | `slices.json` `modules` + api 目录实测 |
| 契约中不属于任何切片的接口：81 条 | `api-contract.json` 减 `slices.json` |
| 由「早期域文件」而非切片实现的接口：135 条（占已实现 635 的 21%） | 迁移前路由实测（`api/*/src/routes/<domain>.rs`） |

### 3.2 判断

**feature-per-controller 作为默认粒度是合适的**（1564 接口 / 251 控制器，大多数控制器 5–15 接口），但有 3 个结构性问题必须在规则里解决，否则迁移后的树会「看起来像 v2，实际不是」：

1. **多控制器切片不能原样移动**。`migrate.py` 对 slice-001/137/194 是整文件移动到单一 feature（实测迁移映射：slice-001 → `catalog/product_manage`），这直接违反 `ARCHITECTURE.md §4` 的「若一个切片含多个 Controller，必须拆分到各自 feature」。`domain-map.json` 虽然给出了 `split[]`，但 `migrate.py` **不读 `split[]`**。要么迁移时人工拆分（3 个切片，其中 slice-001 有 43 条接口、是商品域核心，优先），要么把规则改成「迁移期允许聚合 feature，拆分列入后续任务」，不能两头都不写。
2. **同一控制器的接口被拆到两个 feature**。典型：`MemberTagGroupController` 的 `list` 在早期域 `routes/member.rs`（迁到 `member/member_lists`），其余 6 条在 slice-039（迁到 `member/member_tag_group`）——见迁移前 `api/admin/src/routes/slice039.rs:26-32` 的注释。`migrate.py` 的 `BASE_*` 表把早期域文件整体映射成单个 baseline feature（`member/member_lists`），于是同一个控制器落在两个 feature。**规则缺失**：是没有「controller → feature 必须 1:1」的门禁的。
3. **admin/public 同 feature**：14 个切片同时有管理端与商城端接口，v2 §4 说「一个特性在四层各一个同名文件」，但这类 feature 实际需要 **5 个文件**（`api/admin/routes/<ctx>/<feature>.rs` + `api/public/routes/<ctx>/<feature>.rs` + service + repo + model）。文档必须补一条：「跨端 feature 在 api 层有两个同名文件；`<ctx>::<feature>` 唯一，但 api 模块内各自唯一」。否则迁移/后续开发会重复创建或互相覆盖。

### 3.3 路由表的三重登记问题

现在每个路由文件要维护：`routes()` 里的 `.route()`、`implemented_routes()` 的手写列表、`routes/mod.rs` 的 merge 列表；`bin/main.rs` 还有 state 字段与构造。实测：`ROUTE_TABLE` 只在 35 个文件出现（`api/**` 共 175 个路由文件），`implemented_routes` 函数 171 个，`state.sliceNNN` 访问 450 处。v2 §4 要求每个 feature 都有 `ROUTE_TABLE`，等于把 1564 条路由再手抄一遍。

**建议**：用 `macro_rules! routes { ... }` 从一张 `(METHOD, PATH, HANDLER, JAVA_REF, PERM)` 表同时生成 `routes()`、`ROUTE_TABLE`、`implemented_routes()`。这在 axum 0.8 可行（方法标识符 + handler path 都能进宏），能把「三重登记」降为「一张表」。这也顺带解决 §6 的权限元数据挂载点（见 §6.1）。`implemented_routes()` 本身不是覆盖率门禁的数据源（`check_api_coverage.py` 只解析 `.route()` 字面量），它是运行时 `/openapi/coverage` 的第二真相，越早消除重复越好。

---

## 4. 组合根：per-context `Services` + 14 字段 State 的设计评审

### 4.1 现状（迁移前实测）

| 对象 | 规模 | 证据 |
|---|---|---|
| `AdminState` | 115 字段（99 个 slice 字段 + 16 个域/`jwt`） | `api/admin/src/lib.rs:134`（迁移前 175-291 行） |
| `PublicState` | 56 字段（39 个 slice 字段） | `api/public/src/lib.rs:71`（迁移前 91-147 行） |
| `Repositories` | 148 字段 / 564 行文件 | `infrastructure/src/db/mod.rs:260`（迁移前 struct 270-409、new 411-563） |
| `main.rs` | 591 行；126 个 `Arc::new(...)` 构造 | `bin/src/main.rs` |
| `state.sliceNNN` 访问 | 450 处 | 全 `api/**` 实测 |
| `services::AppState` | 仅被自身定义引用（死代码） | `services/src/lib.rs:173-180` |

迁移工具已经暴露过装配的脆弱性：`main.rs:258-260` 的注释写着「slice029 需要 slice001 作为依赖，只在此处构造一次（此前被注册工具重复插入 4 次，其中 3 次参数个数错误）」，`main.rs:411-412`、`519` 的缩进错位也是自动合并留下的痕迹。

### 4.2 v2 设计对，但 Stage-1 没有实现它

- `ARCHITECTURE.md §5`：`AdminState` 14 个域字段 + 每域 `Services` 结构 + `composition.rs`。
- `migrate.py:2-11`：Stage-1 保持 flat，字段命名 `<ctx>_<feature>`；`gen_services()` 只生成 `pub mod <ctx>;` 和 `pub mod <feature>;`，**不生成任何 `Services` 结构**（`migrate.py` 的 `gen_services` 一节）。
- 实测当前 `migrate.py` 的 `build_world()`：73 个真实切片 + 21 个 baseline = 94 个 feature，368 次移动 + 236 次删除 = **604 个文件操作**；Stage-1 之后预计 `AdminState` 74 字段、`PublicState` 36 字段、`Repositories` 90 字段（按删除的 59 个空壳切片的字段数从当前 115/56/148 中扣除）。**结构门禁阈值是 20**（`check_structure.py:102`），立即 FAIL；文档 §5 的「禁止 80 字段平铺状态」也没有达成。

**这必须二选一，不能含糊**：

- **A（推荐）同一次迁移完成分组**：`by_ctx` 里已经有每个 feature 的四层文件与 `Arc<FeatureService>` 信息，生成 `services/src/<ctx>/mod.rs` 的 `Services { pub <feature>: Arc<XService>, ... }` + `ApiState { pub <ctx>: Arc<ctx::Services>, pub jwt }` 是机械工作。当前跨上下文依赖只有 5 条（`cart→product`、`order→cart/product`、`slice027→012`、`slice029→001`、`slice030/092→010`），且无环，一次做完全可行；做完后 `check_structure.py` 的状态门禁从「必然 FAIL」变成「验收通过」。
- **B（次选）明确分阶段**：改 `ARCHITECTURE.md` 增加「Stage-1/Stage-2」章节，把 `MAX_STATE_FIELDS` 门禁延后到 Stage-2，并在任务板上建 Stage-2 任务。**但不能像现在这样文档写终态、工具做中间态、门禁按终态判**——三方不一致必然导致「门禁红着合并」。

### 4.3 `Arc`/`Clone` 与构造顺序的具体坑

- 当前服务之间是**按值克隆**：`CartService::new(repos: Arc<Repositories>, product: ProductService)`（`services/src/cart.rs:32` 附近）、`OrderService::new(repos, product: ProductService, cart: CartService)`（`services/src/order.rs:26-33`）。因此 `ProductService` 必须 `#[derive(Clone)]`（`services/src/product.rs:15`），而它内含 `Option<CacheService>`（Redis ConnectionManager 的 clone 是廉价句柄，问题不大），但**每构造一次就多一份服务实例**，缓存/本地状态类服务会出隐蔽的重复状态。
- v2 应统一规则：**Services 内的字段一律 `Arc<FeatureService>`，构造只发生一次**；服务之间不得 `(*other).clone()`。
- 跨域 `Services` 引用也用 `Arc`：`trade::Services { catalog: Arc<catalog::Services> }` 会持有整个 catalog（25+ feature）而不是一个窄接口；这就是 §2.2 推荐端口 trait 的原因。
- `State` 是 `#[derive(Clone)]` 且 axum 每个请求会 clone 一次 state（`State<AdminState>` 提取）。14 字段 = 每请求 14 次 Arc 克隆；115 字段 = 115 次，量级不大但属于「上帝状态的日常税」。
- **构造顺序必须由 `composition.rs` 显式编码**：`config → pool → cache → settings/audit/media → 各 ctx Services`。当前 `main.rs` 里存在「服务构造函数内部读环境变量」的违规：`main.rs:368-370` 注释说明 `Slice014Service::new` 内部读 `TIG_IS_O2O`，这正是因为 ConfigService 未迁移。v2 §1.3「唯一组合根」要求这类配置读取回到 `bin`，否则 `services` 层永远无法独立测试。

---

## 5. 迁移风险清单（Top 12，含实测证据）

> 风险按「会不会导致错误安静地发生」排序，而不是按概率。

### 5.1 R1 两套映射互相矛盾（最高风险：会静默产生错误结构）

实测：

- `docs/domain-map.json`：220 切片 / 251 控制器 / 132 coded，`check_structure.py`、`coverage_by_domain.py`、`plan_domains.py` 都读它。
- `docs/_migration-map.json`：迁移工具**实际**读它（`migrate.py:160`），73 real / 59 deleted / 21 baseline。
- 两者 ctx/feature 不一致 **50 处**，其中 **28 个是已编码切片**、**12 个 ctx 不同**：

| 切片 | domain-map | _migration-map | 后果 |
|---|---|---|---|
| slice-073 | member/user_account | payment/user_account | 迁移到 payment，按域覆盖率仍按 member 统计 |
| slice-085 | catalog/product_style | marketing/product_style_admin | 同上 |
| slice-156 | system/short_url | catalog/short_url | 同上 |
| slice-160 | marketing/points_exchange_client | catalog/exchange | 同上 |
| slice-203 | trade/refund_apply | payment/refund_apply | **与文档 §3「trade 拥有 RefundApply」冲突** |
| slice-002/014/026/135/216/217 | trade/order、merchant/shop、payment/paylog … | trade/order_admin、merchant/shop_manage … | 名字不同，coverage_by_domain 找不到文件 → 按域覆盖率虚低 |
| slice-153/067/072/077/078/165/204 | identity/member/catalog/trade/im | member/member/payment/payment/system/payment | 未实现切片的后续路线图会挂错域 |

更危险的是：`domain-map.json` 里有 **6 个已编码切片的 feature 名与 baseline（早期域）feature 同名**（slice-002→(trade,order) vs baseline order、slice-014→(merchant,shop)、slice-026→(payment,paylog)、slice-135→(content,article)、slice-216→time_discount、slice-217→(marketing,seckill)）。如果 `migrate.py` 改成读 domain-map，这 6 处会**两个源文件写同一个目标文件、后者覆盖前者**，静默丢代码。`_migration-map` 通过 `order_admin`/`shop_manage` 等后缀避开了，但这说明 domain-map 目前**不能直接驱动迁移**。

**Mitigation**：一张权威表。让 `domain-map.json` 成为唯一输入，包含：`slices[]`（ctx/feature/type_prefix/real/deleted/split/layers）+ `baseline[]`（76 个早期域文件 → feature）+ `controllers[]`；`_migration-map.json` 由同一脚本生成，`migrate.py` 读 domain-map；加一条「任一 coded 切片的目标路径与 baseline 目标路径重叠 → 报错退出」的断言。

### 5.2 R2 迁移工具会把产物编成一棵坏树（静态判定，需对照产物复核）

| ID | 缺陷 | 证据 | 后果 |
|---|---|---|---|
| D1 | `gen_repos` 用文件里**第一个** `pub struct` 当 Repository 类型，没有按 `*Repository` 后缀选择 | `migrate.py:124`（`def first_struct(text)`）+ `migrate.py:383`；实测 73 个 repo 目标里 **25 个**首结构不是 Repository：`repo/slice001.rs` 首个是 `ProductListFilter`、`repo/slice002.rs` 是 `OrderPatch`、`repo/slice137.rs` 是 `ProductRow`、`repo/identity/authority.rs` 是 `AuthorityPageFilter` … | 生成的 `Repositories` 字段类型错误 + 对 helper struct 调 `::new(pool)` → 编译失败 |
| D2 | `rewrite_all` 的「裸 slice 名 → feature 名」规则会先把 `AdminState`/`PublicState` 的**字段名**改成 `<feature>`，而路由文件里的 `state.sliceNNN` 已被前一步改成 `state.<ctx>_<feature>` | `migrate.py` 的 `rewrite_all`：field 规则在前、裸 token 规则在后；`patch_state_struct` 再用 `flat_field.get(fname)` 已查不到（key 已变） | 声明字段名（`product_manage`）与访问名（`catalog_product_manage`）不一致，~450 处 `state.*` 访问 → E0609 |
| D3 | `patch_main` 用 `(\w+):\s*([^,\n]+),` 解析 state 字面量，**丢掉 shorthand 字段** | `migrate.py:661`；`bin/src/main.rs` 迁移前实测 AdminState 1 个（`refund_apply,`，387 行）、PublicState 13 个（`user,`/`coupon,`/…，496-508 行） | 生成的 `let admin_state = AdminState { … }` 缺字段 → E0063 |
| D4 | baseline（早期域）模块的**同 crate 路径**没重写：规则只对 slice kind 加 `\b<key>::` | `migrate.py` 的 `path_rules`（仅 `if e["kind"] == "slice"` 分支）；实测 `services/src/cart.rs:12 use crate::product::ProductService;`、`services/src/order.rs:14-15 use crate::cart::CartService; use crate::product::ProductService;` | 迁移后 `crate::product` / `crate::cart` 不存在 → E0432 |

另外：

- `main()` 只有 `check_cross_refs()`（检查对已删切片的引用）和 `--dry-run`；**没有任何产物自检**（不校验生成代码里 `state.X` 与 struct 字段、`Repositories` 字段与 repo 类型、每个 `pub mod` 是否有对应文件）。`execute()` 失败会留下半迁移的树（评审期间实测到中间态：文件已移动、`routes/mod.rs` 与 `lib.rs` 仍是旧的）。
- 旧版 docstring 声称「Requires a clean git tree; `git checkout -- crates` restores」，当前版本已无该检查。好消息是 Lead 在迁移前提交了 `df28f2ca` 基线，回滚可用；但工具自身不应依赖人工纪律。
- 幂等性：`execute()` 对不存在的源文件 `continue`（静默跳过），重复执行不会失败也不会恢复。建议加「检测到 `crates/**/slice*.rs` 已不存在但 `docs/_migration-map.json` 仍存在 → 拒绝运行」。

**Mitigation（最低限度）**：修 D1–D4；加一个 read-only 的 `tools/restructure/verify_tree.py`：解析生成的 `AdminState`/`PublicState`/`Repositories`/`mod.rs` 与所有 `.route()`，断言 (a) 每个 state 字段都有构造与字面量赋值；(b) 每个 `state.X` 访问都有声明；(c) 每个 `mod` 都有文件；(d) `.route()` 总数 == 645、契约覆盖 == 635。先跑 verify，再跑 cargo。

### 5.3 R3 门禁看不见新树（安静通过 = 最危险）

- **`check_sql_columns.py` 在迁移后是空转**：`REPO_DIR.glob("*.rs")` 非递归（`check_sql_columns.py:31,209`）。迁移后 repo 文件在 `db/repo/<ctx>/<feature>.rs`，扫描结果是「1 个仓储文件 / 0 条 SQL 语句 / 未知表 0 / ✅ 通过」（评审期间在已移动的树上实测）。这是典型的「门禁绿了，但什么都没查」。必须改 `rglob("*.rs")`，并在报告里输出「扫描文件数」的硬下限（例如 <100 直接 FAIL）。
- **覆盖率/冲突工具不识别 `.nest()`**：`check_api_coverage.py` 按目录前缀推断（`api/admin` → `/adminapi`，`root/**` → 无前缀，`check_api_coverage.py:126-173`），`check_route_conflicts.py` 同理（`check_route_conflicts.py:95-103`）。实测已有两个方向的偏差：`api/admin/src/lib.rs` 的 `/login/signin` 实际挂在**应用根**（`admin/src/lib.rs:278`，`.nest("/adminapi", protected)` 在 282 行），工具却计为 `/adminapi/login/signin` 已实现；`api/public/src/lib.rs` 的 `/health` 实际在根，工具报成 `/api/health` 多余路由。v2 若在任何 ctx 内部再引入一层 `.nest("/<ctx>")`，覆盖率会同时出现假阳性和假阴性。**Mitigation**：工具解析 `nest` 链；或写死约定「每个 `.route()` 必须写 ниже crate 挂载点的完整路径，禁止在 routes/ 内部 nest」并用门禁检查 `nest` 关键字；再加一个启动后 HTTP 冒烟测试（3 条已知路由 + 1 条 root 路由）。
- **`check_structure.py` 的空壳检测会把 baseline 的「聚合文件」误判**：迁移后实测 8 个空壳（`content/pc_cat_floor.rs`、`content/pc_navigation.rs`、`public/routes/logistics/shipping_list.rs` 等），其中部分是早期域文件迁移后 `routes()` 里确实没有 `.route()`（只有 `public_routes()`/`authorized_routes()`）。门禁需要按「文件内任一 routes 变体」判断，否则会逼人写假的空路由。

### 5.4 R4 taxonomy 真空（187 条接口没有家）

见 §1。42 个控制器/187 条接口在 14 个 ctx 中无归属。风险不是「现在错」，而是 **929 条剩余接口的路线图会按错误的 ctx/feature 派工**（`plan_domains.py` 依赖 `domain-map.json`）。Mitigation：把 §1.2 的表并入 domain-map，并加门禁。

### 5.5 R5 admin/public 同 feature 的拆分与鉴权语义

- 14 个切片跨 admin/public（已实现 4 个）。public 侧现有**三套约定并存**：`routes(state)`（切片自带鉴权中间件）、`public_routes()`（匿名）、`authorized_routes()`（外层统一 `require_auth`）——见迁移前 `api/public/src/routes/mod.rs:3-17` 的警告「外层不得再套 require_auth，否则双重鉴权或把匿名可访问接口变成必须登录」。
- `migrate.py` 生成 public 聚合时保留了三套函数（`routes/public_routes/authorized_routes`），但**没有逐路由的 auth 元数据**。同一个 feature 里既有匿名又有需登录接口时，只能靠实现者记得把 handler 放进哪个函数——这是最容易在迁移中静默改变鉴权语义的地方。
- **Mitigation**：`ROUTE_TABLE` 带 `auth: Public|Jwt|Optional` 字段；生成器按 `auth` 分组；加集成测试：对迁移前 645 条已注册路由逐一比对「匿名访问返回 401/200」的期望（至少对 public 侧 333 条中已实现的 142 条做 golden test）。

### 5.6 R6 重复注册 panic（axum 启动期 panic，编译期不报）

现状 0 冲突（迁移前实测 645 条注册、`check_route_conflicts.py` 0）。迁移中的真实风险是**旧聚合器与新聚合器并存**（评审期间已观察到 `routes/mod.rs` 保留 235 个 `mod sliceNNN` 而文件已移动/删除）。axum `Router::merge` 的重复只在 `serve` 时 panic，`cargo check/test` 不一定覆盖到所有分支。Mitigation：迁移作为一个原子操作（删旧聚合器与移动同批完成）；`check_route_conflicts.py --strict` 必须在前后各跑一次；冒烟测试里真正 `Router::merge` 全部路由并 `oneshot` 一条请求。

### 5.7 R7 测试面：2,557 个 test fn、207 个文件引用 sliceNNN

实测：`#[cfg(test)]` 出现在 292 个文件；`#[test]/#[tokio::test]` 共 **2,557** 个（core 986、services 1,021、infrastructure 293、api 237、security 14、common 6）；**207 个文件中测试代码含 `sliceNNN` 引用**。`ARCHITECTURE.md §6` 要求「单测断言一律不改」，但重命名模块路径/类型名必然要改 `use crate::sliceNNN::...` 与测试模块引用——这不是「断言不变」，而是「允许机械改 import」。

**Mitigation**：(a) 门禁加 test 数量下限（≥2,550），防止迁移中「测试被删=绿」；(b) 迁移前记录 `cargo test --workspace -- --list` 清单，迁移后 diff；(c) 文档把「断言不变」改成「断言与期望值不变；import/模块路径允许 codemod 机械更新」。

### 5.8 R8 `#[serde(rename_all="camelCase")]` 回归

core models 里 `rename_all = "camelCase"` 出现 **909 次**（835 个 pub struct，含 enum/多个属性），路由层还有大量手写 `#[derive(Deserialize)] #[serde(rename_all="camelCase")]`。重命名 codemod 会整文件改写（`rewrite_all` 对每个文件 `read → regex → write`），属性本身不在改名规则里，理论上安全；但**没有任何门禁验证 JSON 契约**：`docs/api-contract.json` 只有路径/方法，没有字段级 schema；现有 `core/tests/schema_consistency.rs` 只校实体列，不校 serde 大小写。

**Mitigation**：(a) 迁移后统计 `rename_all` 数量（应从 909 起只增不减）；(b) 对已实现接口做 golden JSON：用迁移前的 handler 单元测试固定 3–5 个复杂响应的 JSON（camelCase 字段名、金额字符串、时间为毫秒），迁移后逐字节比对；(c) 新增门禁：DTO 结构体若字段名不是 single-word，必须有 `rename_all` 或逐字段 `rename`（可静态检测）。

### 5.9 R9 构建成本与并行纪律

- `rust/crates/target/debug` 实测 **19.09 GB**（另有 `target-verify/debug` 0.02 GB）。迁移会移动/重写 604 个文件，路径变化使 6 个 crate 的增量编译基本失效，等于一次全量重建。
- 团队已有教训：`docs/SESSION-B-LANE.md:26-27` 明确写「本机 13.7GB，并发编译会 OOM 并伪造编译错误；会话 A 拥有唯一编译入口」。迁移后如果多个 teammate 同时 `cargo check`，会重演。
- **Mitigation**：迁移窗口串行，只有一个编译入口；先 `cargo check -p tigshop-core` 等逐 crate 升序验证（core → infrastructure → services → api → bin），而不是直接 `--workspace`；`CARGO_INCREMENTAL` 保持默认，避免大 target 反复清理。

### 5.10 R10 root 路由与早期域文件没有进入设计正文

- root 路由：8 个文件、17 条已实现（admin 13 / public 4），其中 2 个文件为空壳。v2 §2 只说 root/「按域同构组织」，但没说 root 的 feature 是否与 `routes/` 同名共用四层文件（当前实现是共用 service，例如 `slice140` 的 IM 根路由与普通路由共用 `services/slice140`）。
- 早期域文件（76 个）：20 services / 17 repos / 15 models / 12 admin routes / 12 public routes，承载 **135 条已实现接口（占 635 的 21%）**，其中 **67 条不在任何切片计划里**（`api-contract.json` 中 Article 8、Cart 6、Coupon 6、Product 5、Seckill 5、TimeDiscount 5、Pay 5 …）。`ARCHITECTURE.md §6` 只把「220 个切片」说成权威映射来源，完全没有提这 76 个文件。`migrate.py` 用 `BASE_*` 硬编码表覆盖了它们，但**文档与门禁不知道这件事**：如果以后有人依据 §6 重新生成映射，这 135 条接口就无家可归。
- **Mitigation**：§6 补「baseline 76 文件 → feature」小节；`domain-map.json` 加 `baseline[]`；覆盖率门禁按「635 只增不减」保留（现在是对的），并额外要求「已实现接口在 domain-map 中必须可解析到 feature」。

### 5.11 R11 行尾/文件格式

实测 32 个 `.rs` 是 CRLF（含 `core/src/entities/*.rs`、`api/admin/src/routes/decorate.rs` 等），迁移工具 `write()` 强制 `newline="\n"`（`migrate.py:118-121`）。被重写的文件会整体变成 LF——不影响运行，但让 `git diff` 变成整文件变更，**破坏了评审可读性**，也让「行为不变」无法用 diff 审查。Mitigation：迁移前后各加一个纯格式 commit（统一 LF），或迁移工具保留原行尾。

### 5.12 R12 删除 59 个空壳切片的连带引用

`migrate.py` 删除 236 个空壳文件（59 个切片 × 4 层），`check_cross_refs` 会拦「非 use 语句里引用被删切片」，但**对 `use` 行会直接跳过**（假定会被删除），而清理 `use` 的 `drop_deleted_imports()` 只处理 3 个文件（`bin/main.rs`、`api/admin/src/lib.rs`、`api/public/src/lib.rs`）。如果某个路由/模型文件 `use tigshop_core::models::slice034::X`，则不会被清理也不会被报告 → 编译错误。Mitigation：把 `use` 行也纳入 check_cross_refs 的 **problem** 列表，或让 `drop_deleted_imports` 覆盖全树。

---

## 6. v2 尚未覆盖的部分（按严重度）

### 6.1 RBAC 权限矩阵（功能缺口 No.1）

- 契约：**864 / 1564 条接口**带 `permissions`，共 **309 个不同权限码**（`api-contract.json`）。
- Rust：`security/src/web.rs` 只有 `require_auth`/`optional_auth`（117/33 处使用），`AuthUser` 里有 `role_id` 但没有权限判定；全仓库 **0 处代码级 `has_permission`**（83 处 `hasPermission` 全部出现在 slice 的文档注释里，来自逐字抄 Java 注解）。切片注释自己承认这一点，例如迁移前 `api/admin/src/routes/slice039.rs:36-39`：「`require_auth` 目前只校验登录态，权限矩阵未实现…当前任何已登录管理员都能调用本域全部接口」。
- v2 文档完全没提权限归属；`ROUTE_TABLE` 也没有权限字段。**这是安全回归，不只是架构债**。必须在 v2 契约里加：`ROUTE_TABLE` 带 `perm: Option<&'static str>`；`security` 提供 `require_perm` extractor/middleware；`check_api_coverage` 对已实现接口校验权限码与契约一致（目前只校验路径/方法）。

### 6.2 事务边界

- Java：`@Transactional` **506 处**（960 个 service 文件）。
- Rust：`pool.begin()` 仅 **19 处 / 7 个 service 文件**（order、slice037/039/041/087/137/139）。
- v2 §9 只写「service 层 `pool.begin()/commit()`」，没有规定：哪些用例必须事务、repo 是否允许自开事务、跨 ctx 写如何保持一致性。建议：在 `Services` 层定义 `UnitOfWork`/`Tx` 句柄（repo 方法接受 `&mut Tx` 而不是自取 pool），并把「一个用例一个事务」写进 feature 模板；迁移后按 Java `@Transactional` 清单补齐。

### 6.3 MQ / 延迟任务 / 定时任务

- Java：`@RabbitListener` 18、`rabbitTemplate` 41、`@Scheduled` 12（自动确认收货、自动同意退货、打印队列、IM 自动回复、订单超时取消）。
- Rust：`infrastructure/src/queue/mod.rs` 只有 25 行，`QueueService` 只有 `health_check()`（连 publish/consume 都没有）；0 个定时任务；切片注释里多处明确写「RabbitMQ 未迁移」（`services/src/slice120.rs:53-57`、`slice140.rs:26-28`、`slice009.rs:21`）。
- v2 没有事件/消息层设计。建议：`domain events` + `outbox` 表（与事务同提交），`infrastructure/queue` 提供 publisher trait；延迟任务（订单超时）必须有 owner（建议 trade ctx 的 scheduler 或独立 worker crate），不能继续散落在 handler 里用 `tokio::spawn`。

### 6.4 缓存归属

- 只有 `ProductService` 等 2 个 service 用 `CacheService`（`product.rs` 里 key 硬编码 `product:detail:{id}`）；Java 侧没有 `@Cacheable`，缓存是手写 Redisson 逻辑。
- v2 不定义「谁拥有哪个缓存 key/失效策略」。在多 ctx 下，`order` 取消订单要失效 `product:detail` 会变成跨 ctx 的隐式耦合。建议：每个 ctx 在本域 service 内封装 cache key 前缀（`catalog:product:{id}`），跨域只通过 service 方法（不直接操作对方 key）；缓存失效规则写进 feature 模板。

### 6.5 配置/设置服务（被低估的跨切面）

- `ConfigService` 是 Java 服务图 fan-in 第一名（89）。Rust 目前只有 3 个切片的**注释**提到它「未迁移」（`services/src/slice006.rs:26`、`slice011.rs:50`、`slice021.rs:27`）；`slice014` 直接在构造函数里读 `TIG_IS_O2O` 环境变量（`bin/src/main.rs:368-370` 注释）。
- v2 把 `Config` 放进 `system` ctx 是错的抽象层级：它应该是 `infrastructure` 的 `Settings` 能力（DB settings 表 + 进程内缓存 + 热更新），所有 ctx 以只读端口注入。否则 `system::Services` 会成为所有 ctx 的构造依赖，组合根顺序被绑死。

### 6.6 i18n

- Java：`tigshop-common/src/main/resources/i18n/messages.properties`；Rust：**未迁移**，多处切片注释写「i18n 未迁移，直接内联中文」（`core/src/models/slice147.rs:151-154`、`slice030.rs:51`、`services/src/slice028.rs:74`）。workspace 里 `i18n-embed` 依赖从未使用。
- 是否迁移是产品决策，但 v2 文档必须明确「本期不做 i18n，文案放 core model 常量」或「做，统一走 MessageHelper 端口」；不能既不说、又继续在每个 feature 里散落中文字面量。

### 6.7 其它未定义项

- **错误边界**：`ApiResponse`/`AppError` 全局可用，但「ctx 内错误 → HTTP 状态/业务码」的映射没有归属（现在每个 handler 自己 `map_err`）。
- **审计日志**：`AdminLogService` fan-in 20，Rust 无对应实现；`AdminMsgService` fan-in 16 同理。
- **WebSocket**：IM 的 `send/setRead` 推送未迁移（`api/public/src/root/slice140.rs:22` 注释）。
- **搜索**：`config.yaml` 有 elasticsearch 配置，但 Rust 代码没有 ES 客户端；Java 的 SearchController 依赖 ES。
- **文件存储**：`infrastructure/storage` 存在但切片 019 的 S3/OSS/COS/Qiniu 实现是 Rust 内联的，v2 未定义 storage 端口归属。
- **代码生成/构建**：`api/openapi` 用 `build.rs` 内嵌 `api-contract.json`（`Cargo.toml` build-dependencies），迁移后路径不变，OK；但 `check_api_coverage.py` 的 `CRATE_PREFIX` 写死了 `admin`/`public` 目录名，v2 不能再改 crate 目录名（改就是门禁重写）。

---

## 7. 总判决与建议优先级

### 7.1 判决

**这套结构适合 1564 接口的 B2B2B 模块化单体**——按域组织、垂直内聚、组合根唯一、路由即契约，这些原则都对，且针对的是真实问题（529 个 slice 文件、4,738 处编号引用、115 字段状态、591 行 main）。**但当前交付物不是可验收的 v2**：

1. 迁移工具产出的终态是「改名后的 flat state」，与文档/门禁的 14 域终态不一致（§4.2）；
2. 工具本身有 4 处必现的编译级缺陷、且无产物校验（§5.2）；
3. 两套映射互相矛盾，且 domain-map 直接参与迁移会覆盖 6 个 baseline feature（§5.1）；
4. 分类学漏掉 12% 的接口（§1）；
5. 跨域服务环、RBAC、事务、MQ 这些「决定它能不能跑完 1564 条」的问题还没有设计答案（§2、§6）。

### 7.2 迁移前必须完成（M1–M6）

| # | 必须做 | 为什么现在是阻塞 |
|---|---|---|
| **M1** | 修 `migrate.py` 的 D1–D4 + 加 `verify_tree.py` 产物自检 + git 干净/幂等守卫；先 `--dry-run`，再在基线 commit 上跑，产物过自检后才 cargo | 否则产物必编译失败，且失败点在 19GB target 上排查成本最高 |
| **M2** | 单一权威映射：`domain-map.json` 增加 `baseline[]`/`real`/`deleted`/`layers`/`type_prefix`，`migrate.py` 改读它；`_migration-map.json` 由同一脚本生成并断言一致；加「目标路径重叠 → 直接失败」 | 现有 50 处不一致（28 个已编码）会让按域覆盖率与后续路线图全部错位 |
| **M3** | Stage-1 内完成 per-ctx `Services` 与分组 State（或正式写成两阶段并把门禁延后 + 建 Stage-2 任务） | 当前工具产出 74/36 字段 State，`MAX_STATE_FIELDS=20` 立即红；文档 §5 不成立 |
| **M4** | 修门禁盲区：`check_sql_columns.py` 改 `rglob` + 扫描文件数下限；覆盖率/冲突工具识别 `nest`（或写死禁止 nest 的约定 + 冒烟测试） | 现在 SQL 门禁在迁移后是「0 条 SQL ✅」的空转；覆盖率已有 1 假阳 + 1 假阴 |
| **M5** | 分类学补丁：§3 表改为机检的 `controllers[]`（251 条必须全覆盖），补 §1.2 的 42 个控制器；marketing 拆 promotion/growth；Config/Audit/Media/Notify 提升为端口而非 ctx | 否则 929 条接口的派工与 `plan_domains.py` 都会错 |
| **M6** | 跨域依赖策略：定义 `CatalogQuery`/`SettingsPort`/`AuditPort` 等窄 trait；`check_structure.py` 禁止 services 直接依赖其它 ctx 的 `Services`；DB 规则（跨 ctx 只读允许、写禁止）+ SQL 门禁 | 剩余接口会被 Java 服务图的 24 组双向依赖打穿；不先定规则，后面每加一个 feature 都在制造环 |

### 7.3 迁移后/下一阶段再做（按优先级）

1. **RBAC 矩阵**（864 条接口 / 309 权限码，当前 0 执行）——安全缺口，优先级高于任何新功能；
2. **事务 / outbox / MQ / 定时任务**（Java 506/18/12 处，Rust 19/0/0）；
3. **路由表宏**（`ROUTE_TABLE` 只在 35/175 文件；`implemented_routes` 171 处重复）；把权限/auth 元数据一起挂进表；
4. **测试防回退**：test 数量下限 2,550、`--list` 清单 diff、golden JSON；
5. **serde 契约门禁**：`rename_all` 计数只增不减 + 3–5 个复杂响应 golden；
6. **i18n / 搜索 / 存储 / WebSocket** 的归属决策；
7. **评审可读性**：统一 LF、迁移前后 diff 分段（纯移动 commit vs 逻辑改动 commit）。

### 7.4 做对了的地方（保持不变）

- 废弃编号切片、删除 59 个空壳（236 文件）、禁止「可编译但不可达」的注册——方向正确，且删除清单可机器验证；
- 覆盖率只认「真实注册路由」而不是文件存在（`check_api_coverage.py` 的静态 `.route(` 提取 + 635 底线口径）；
- 依赖方向门禁（`check_structure.py` 的 `ALLOWED_DEPS`）、空壳检测、上帝状态阈值——阈值本身是对的（20 字段），只是迁移工具没做到；
- `check_route_conflicts.py` 对 axum `merge` panic 的前置检查（现在 0 冲突）；
- 迁移前提交了 `df28f2ca` 基线并保留 `git log --follow`（§6 的诉求），回滚有据。

---

## 附：评审数据快照（可复算）

| 指标 | 值 | 来源/方法 |
|---|---|---|
| 契约接口/控制器 | 1564 / 251 | `api-contract.json` |
| 已实现覆盖率 | 635（40.6%）；admin 493/1231、api 142/333 | `check_api_coverage.py`（迁移前后实测一致） |
| 注册路由/多余路由 | 645 / 16 | 同上 |
| 路由冲突 | 0 | `check_route_conflicts.py` |
| 迁移前结构门禁 | 529 slice 文件 / 528 mod / 75 layout / 69 空壳 / 2 上帝状态 | `check_structure.py` |
| 迁移后结构门禁（评审时中间态） | 0 slice 文件 / 235 mod / 0 layout / 8 空壳 / 2 上帝状态 / FAIL 243 | 同上 |
| sliceNNN 引用 | 4,738 处（core 250、services 1,489、infra 779、api 1,679、bin 541） | 全树 regex |
| 空壳切片 | 58 个 slice id / 60 个路由文件 / 迁移删除 236 文件 | `_migration-map.json` |
| 迁移动作 | 368 move + 236 delete = 604 个文件操作；73 real + 21 baseline = 94 feature | `migrate.py build_world()` 实测 |
| Java 服务依赖图 | 802 边 / 529 跨包 / 24 个 2-cycle / 21 包 SCC | 288 个 ServiceImpl 静态扫描 |
| Java 事务/调度/MQ | @Transactional 506 / @Scheduled 12 / @RabbitListener 18 / rabbitTemplate 41 | `java/tigshop-service` 扫描 |
| 契约权限 | 864 接口 / 309 权限码；Rust 执行 0 | `api-contract.json` + `security/src/web.rs` |
| 测试面 | 2,557 test fn / 292 文件；207 文件测试引用 sliceNNN | 全树扫描 |
| 构建体积 | `crates/target/debug` 19.09 GB | 目录实测 |
| serde | 核心模型 909 处 `rename_all="camelCase"` | `core/src/models` 扫描 |
