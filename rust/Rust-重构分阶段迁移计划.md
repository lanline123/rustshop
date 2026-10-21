# TigShop Java → Rust 分阶段迁移计划

> **适用对象**：`java-b2b2b-pro-v5.8.57`（Spring Boot 3.3.4 + MyBatis Plus 多模块 B2B2C 商城）
> **配套代码**：本仓库 `rust/` 工作区（Axum + SQLx，已实现核心交易链路骨架）
> **版本**：V1.0 ｜ **策略**：绞杀者模式（Strangler Fig）+ 按模块灰度切流

---

## 1. 现状盘点（迁移规模基线）

### 1.1 Java 后端模块

| 模块 | Java 文件数 | 职责 | 迁移归宿（Rust crate） |
|---|---:|---|---|
| `tigshop-bean` | 2036 | 实体/DTO/Query/VO | `crates/core`（entities + models） |
| `tigshop-service` | 960 | 业务服务 + Mapper（27 个域） | `crates/services` + `crates/infrastructure` |
| `tigshop-adminapi` | 184 控制器 / **1234 个方法级接口** | 管理后台 REST | `crates/api/admin` |
| `tigshop-api` | 76 控制器 / **332 个方法级接口** | 商城前台 REST | `crates/api/public` |
| `tigshop-common` | 205 | 工具、配置、切面 | `crates/common` |
| `tigshop-security` | 28 | 认证授权 | `crates/security`（已完成） |
| `tigshop-feign` | 14 | 服务间调用 | 网关内联，单体化后删除 |

### 1.2 需保留的既有资产（不重写）

- **前端三端**：`view/Tigshop-Pc`（Nuxt3）、`view/admin`（Vue3+Vite）、`view/uniapp`（多端）—— 只保证 API 契约不变。
- **数据库**：MySQL 8 库表结构与历史数据零迁移，实体用 `#[sqlx(rename_all = "snake_case")]` 对齐。
- **中间件**：MySQL / Redis / RabbitMQ / Elasticsearch 服务本体保留，仅替换客户端库。
- **官方 SDK 薄集成**：微信/支付宝/短信/地图等，用 `reqwest` + trait 封装，灰度期可由 Rust 网关反代回 Java 服务。

### 1.3 Rust 工作区当前进度

| 层 | 已完成 | 未完成 |
|---|---|---|
| 实体 | AdminUser/Product/ProductSku/User/Order/OrderItem/Cart（7 张） | 其余 ~65 张表对应实体 |
| 仓储 | 5 个 repo（分页、原子库存扣减、状态机） | 营销/财务/商户仓储 |
| 服务 | auth / product / cart / order / payment / user（6 个） | 优惠券、秒杀、拼团、分销、售后、支付渠道实现 |
| API | 28 条路由（登录、商品、购物车、订单、用户、健康检查） | 剩余 ~1538 个接口 |
| 基础设施 | 连接池、缓存降级、本地存储、队列抽象、JWT/BCrypt | RabbitMQ 真实消费、ES 客户端、OSS |

⚠️ **已发现的契约偏差（P0，必须先修）**：当前 Rust 路由与 Java 原路径不一致，例如：

| Java 原路径（前端实际调用） | Rust 当前路径 | 问题 |
|---|---|---|
| `GET /api/cart/cart/list` | `/api/cart/list` | 少一层资源段 |
| `POST /api/order/pay/...` | `/api/order/create` | 命名与前缀均不同 |
| `GET /adminapi/product/product/list` | `/adminapi/product/list` | 少一层资源段 |
| `GET /adminapi/order/order/list` | `/adminapi/order/list` | 少一层资源段 |

**要求**：阶段 0 必须让 Rust 路由与 Java 原路径逐字符对齐（或在网关做前缀改写），否则前端零改动切流无从谈起。

---

## 2. 重构 / 保留决策总表

### 2.1 用 Rust 重构（按优先级）

| 优先级 | 模块 | Java 位置 | 理由 |
|---|---|---|---|
| **P0** | 登录认证 / JWT / RBAC | `tigshop-security`、login 控制器 | 已完成，作为样板 |
| **P0** | 商品浏览 / SKU / 分类 | `service/product`、`mapper/product` | 高频读、缓存重 |
| **P0** | 购物车 / 下单 / 库存 / 订单状态机 | `service/cart`、`service/order` | 高并发强一致，Rust 核心价值 |
| **P0** | 支付编排 / 回调幂等 / 余额 | `service/payment`、finance | 零 GC 停顿 + `rust_decimal` 精确金额 |
| **P1** | 用户中心 / 地址 / 收藏 / 优惠券领取 | `service/user`、`service/authorization` | 边界清晰 |
| **P1** | 秒杀 / 拼团 / 抢券（营销高并发段） | `service/promotion` | 并发红利最大 |
| **P1** | 管理后台订单/商品/库存管理接口 | `adminapi/order`、`adminapi/product` | 交易闭环 |
| **P2** | 价格计算引擎（阶梯批发价、促销叠加、跨境税费） | 散落在 service 各处 | 纯函数、易测试、类型安全 |
| **P2** | 定时任务 / MQ 消费者 | `service/job`、Spring AMQP | tokio 常驻任务 |
| **P2** | 报表统计（面板/销售/访问） | `adminapi/panel` | 聚合查询性能敏感 |

### 2.2 保留不重写

| 资产 | 处理方式 |
|---|---|
| Nuxt3 商城 / Vue3 后台 / UniApp 三端前端 | 原样保留，仅回归接口契约 |
| MySQL 表结构与历史数据 | 零迁移复用 |
| MySQL/Redis/RabbitMQ/Elasticsearch 服务 | 保留，换客户端 |
| 装修（decorate）、内容（article）、相册、IM、打印、O2O 等长尾后台 | **长期代理给 Java**，或最后低优先级迁移 |
| 微信/支付宝/Apple Pay 等第三方官方 SDK | trait 抽象 + reqwest，灰度期反代 Java |
| 65 个 SQL 脚本 / 数据初始化 | 保留为唯一 schema 事实源 |

---

## 3. 总体架构：绞杀者模式

```
                    ┌─────────────────────────────┐
   Nuxt3/Vue3/UniApp│   Rust 网关（Axum，双端口）    │
        ───────────▶│  :8080 /adminapi  :8181 /api │
                    └───────┬─────────────┬───────┘
                            │             │
              已迁移模块     │             │ 未迁移模块（白名单路由）
              Rust 直接处理  │             │ 反向代理到 Java Spring Boot
                            ▼             ▼
                    ┌──────────────┐  ┌──────────────────┐
                    │ Rust 服务层   │  │ Java tigshop-*     │
                    │ + SQLx/Redis │  │ （逐步缩小，最终下线）│
                    └──────┬───────┘  └────────┬─────────┘
                           └───── 同一个 MySQL ────┘
```

**切流规则**：
1. 网关维护一份「模块 → 处理方」路由表（配置文件热加载）；
2. 每切一个模块：先**影子流量比对**（Rust 处理但不返回，或双发比对响应）→ 再 **5% / 50% / 100%** 放量；
3. 任何阶段可按模块秒级回滚到 Java 代理；
4. 双方共享同一个 MySQL，保证数据一致性，无需双写（同库直读写）。

---

## 4. 分阶段迁移计划

### 阶段 0：契约对齐与地基（2 周）【前置，不做完不进入阶段 1】

**范围**
- 修复 Rust 路由与 Java 原路径的偏差（见 §1.3 表）；
- 从 260 个控制器自动生成**接口全量清单**（HTTP 方法、路径、权限、DTO 字段），作为后续每阶段的验收母本；
- 搭建 Rust 网关的「按前缀路由到 Java」反向代理能力；
- 建立契约回归测试框架（录制 Java 响应 → 回放比对 Rust 响应）。

**接口清单（交付物）**
- `api-inventory-public.md`：332 个前台接口
- `api-inventory-admin.md`：1234 个后台接口

**验收标准**
- [ ] 随机抽样 100 个接口，Rust 路径与 Java 逐字符一致；
- [ ] 录制回放框架能在 CI 中跑通，输出 JSON diff 报告；
- [ ] 网关代理未匹配路由到 Java 的成功率 100%；
- [ ] `cargo clippy --workspace` 无告警，`cargo test --workspace` 全绿。

---

### 阶段 1：认证与权限（2 周）【切流模块 #1】

**接口清单**

前台 `tigshop-api`（`/api/user/login`、`/api/user/register` 等，约 40 个）：
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/user/login` `/api/user/login/sendMobileCode` | 账号/短信登录 |
| POST | `/api/user/login/sendEmailCode` `/checkEmail` `/checkMobile` | 验证码 |
| POST | `/api/user/login/forgetPassword` | 忘记密码 |
| POST | `/api/user/login/wechatEvent` `/bindWechat` `/unbindWechat` | 微信绑定（可先代理） |
| POST | `/api/user/register` | 注册 |
| POST | `/adminapi/login` `/adminapi/login/sendMobileCode` | 管理端登录 |
| POST | `/adminapi/login/refresh` | JWT 刷新 |

后台权限（`adminapi/authority`，30 个）：
| 控制器 | 接口数 |
|---|---:|
| `AdminUserController` | 13 |
| `AdminRoleController` | 7 |
| `AuthorityController` | 10 |
| `AdminLogController` | 操作日志 |

**验收标准**
- [ ] 旧 Java 库中 BCrypt `$2a$` 历史密码可直接登录（双算法识别）；
- [ ] access + refresh token 双令牌流程与 Java 版 JSON 完全一致；
- [ ] RBAC：菜单/按钮/数据范围三类权限校验通过用例 ≥ 120 条；
- [ ] 管理端登录失败锁定、验证码、异地登录日志与 Java 行为一致；
- [ ] **切流**：`/api/user/login*`、`/adminapi/login*` 100% 走 Rust 持续 48 小时无回滚。

---

### 阶段 2：商品与目录（3 周）【切流模块 #2】

**前台接口（`/api/product/product` 17 个、`/api/category/category`、`/api/search/search` 等，约 70 个）**
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/product/product/list` | 商品列表（分页/筛选/排序） |
| GET | `/api/product/product/detail` `/share` | 详情（含 `id+sn` 变体） |
| GET | `/api/product/product/isCollect` `/getCoupon` `/getComment` | 详情附属数据 |
| GET | `/api/product/product/getCommentList` `/getFeedbackList` | 评价/问答分页 |
| GET | `/api/product/product/getProductAvailability` `/getBatchProductAvailability` | 库存可售性 |
| POST | `/api/product/product/promotion` `/getProductAmount` | 价格试算 |
| GET | `/api/category/category/...` | 分类树 |
| GET | `/api/search/search/...`（3 个接口） | 搜索（ES 保留，换客户端） |

**后台接口（`adminapi/product`，约 120 个）**
| 控制器 | 接口数 | 说明 |
|---|---:|---|
| `ProductController` | 22 | CRUD/审核/批量/回收站/复制/上架 ES |
| `CategoryController` / `BrandController` | ~25 | 分类品牌 |
| `ProductStockController` / `ProductInventoryLogController` / `StockWarningController` | ~30 | 库存与流水 |
| `CommentController` / `ProductGroupController` / `AttributesTplController` | ~40 | 评价、分组、属性模板 |

**验收标准**
- [ ] 商品列表/详情响应 JSON 与 Java 版 **字段级 diff 为零**（对 1000 条真实商品抽样）；
- [ ] SKU 组合、阶梯批发价、上下架状态口径与 Java 一致；
- [ ] ES 搜索结果与 Java 版 Top100 查询结果一致（或先整体代理 ES 调用）；
- [ ] 商品详情 P99 延迟 ≤ Java 版的 50%；
- [ ] 后台商品编辑全流程（新建→审核→上架→库存调整）验收通过；
- [ ] **切流**：商品读接口 100% Rust 运行 72 小时，后台写接口 50% 灰度一周。

---

### 阶段 3：购物车、下单与订单履约（4 周）【切流模块 #3，核心】

**前台接口**
| 控制器（Java 原路径） | 接口数 | 关键端点 |
|---|---:|---|
| `/api/cart/cart` | 8 | `list` `getCount` `addToCart` `updateItem` `updateCheck` `removeItem` `clear` `getCouponDiscount` |
| `/api/user/order` | 9 | `list` `detail` `orderNum` `cancelOrder` `reasonConfig` `delOrder` `confirmReceipt` `shippingInfo` `buyAgain` |
| `/api/order/check` | 结算确认 | 下单前置校验 |
| `/api/order/pay` | 11 | 支付发起（本阶段仅编排，渠道实现在阶段 4） |

**后台接口（`adminapi/order`，约 60 个）**
| 控制器 | 接口数 | 关键端点 |
|---|---:|---|
| `OrderController` | 38 | `list` `statusCount` `detail` `deliver` `cancelOrder` `setPaid` `modifyMoney` `modifyConsignee` `modifyShipping` `modifyProduct` `batch` `batchDeliverImport` `orderExport` |
| `OrderLogController` | 订单日志 | `list` `create` |
| `ShopOrderConfigController` | 2 | 订单配置 |
| `AftersalesController` | 10 | 售后：`list` `detail` `create` `update` `receive` `complete` |

**核心业务规则（必须逐条有测试）**
1. 下单：可购校验 → 逐行 `UPDATE ... WHERE stock >= n` 原子扣减 → 任一行失败回滚已扣库存 → 写 `order` + `order_item`（单事务）→ 清理购物车；
2. 取消：`0 → 40` 原子状态流转防并发重复取消 → 回滚库存；
3. 发货：`10 → 20`，仅待发货可发；管理员发货/批量导入发货；
4. 售后状态机与退款触发；
5. 金额全程 `rust_decimal`，禁用浮点。

**验收标准**
- [ ] **并发超卖测试**：1000 并发抢 100 库存，成交数 = 100，无超卖、无负库存；
- [ ] 下单失败回滚：注入中途错误，库存与订单表零残留；
- [ ] 订单状态机全路径（0/10/20/30/40 及售后分支）用例覆盖 100%；
- [ ] 管理端 38 个订单接口全部通过契约回放；
- [ ] 与 Java 影子双发比对：1000 笔真实订单试算金额差异 = 0；
- [ ] **切流**：前台下单链路 5% → 50% → 100%，每档观察 ≥ 72 小时，丢单/错单 = 0。

---

### 阶段 4：支付、结算与财务（4–6 周）【切流模块 #4，最高风险】

**范围接口**
| 区域 | 路径 | 说明 |
|---|---|---|
| 支付发起 | `/api/order/pay/*`（11 个） | 微信/支付宝/余额/Apple Pay 编排 |
| 支付回调 | 各渠道 notify + `/api/order/pay/profitSharing/notify` | **验签 + 幂等** |
| 充值 | `/api/user/rechargeOrder` | 余额充值 |
| 前台账务 | `/api/user/account`、`WithDrawApplyClientController` | 余额、提现 |
| 后台财务 | `adminapi/finance`（18 个控制器，约 150 个接口） | `Paylog` `RefundApply` `RefundLog` `Statement` `ProfitSharing` `SubMerchant` `UserWithdrawApply` `CreditSettlement` `AccountPanel` |
| 押金 | `adminapi/deposit`（9 个控制器） | 保证金体系 |

**技术要求**
- 每渠道一个 trait：`PaymentChannel { create_order, close, verify_callback, refund }`，实现体先**反代 Java**，再逐渠道原生替换；
- 回调：验签 → 幂等键去重 → 状态机落库 → 补偿任务，全部事务化；
- 对账：每日定时任务比对渠道账单与本地 `paylog`。

**验收标准**
- [ ] 沙箱环境全渠道走通；真实小额（0.1 元）微信/支付宝支付+退款各 20 笔成功；
- [ ] 重复回调 100 次仅生效 1 次（幂等测试）；
- [ ] 伪造回调（错误签名）100% 拒绝；
- [ ] 资金类单测覆盖：余额扣减 `UPDATE ... WHERE balance >= ?` 防负、分账、提现冻结；
- [ ] 与 Java 版并行跑 2 周对账，金额差异 = 0；
- [ ] **切流**：支付渠道逐个切，切一个观察一周；随时可回退。

> **硬性红线**：本阶段未完成验收，绝不放量；任何资金差异立即全量回滚 Java 并冻结该渠道。

---

### 阶段 5：营销与促销（4 周）【切流模块 #5】

**范围接口**
| 区域 | 控制器/路径 | 说明 |
|---|---|---|
| 优惠券 | `adminapi/promotion/coupon`（15 个）、`/api/promotion/coupon`、`/api/user/coupon` | 发放/领取/核销 |
| 秒杀 | `adminapi/promotion/SeckillController` | 高并发核心 |
| 拼团 | `GrouponPromotion*`（3 控制器）+ `/api/promotion/groupon*` | 成团状态机 |
| 抽奖/积分/礼品卡 | `LotteryController`、`PointsExchange`、`GiftCard*`、`SignInSetting` | |
| 限时折扣/满减 | `TimeDiscountController`、`ProductPromotionController`、`PromotionController` | 价格引擎联动 |

**验收标准**
- [ ] 价格试算引擎：促销叠加顺序、互斥规则与 Java 版对拍 500 组差异 = 0；
- [ ] 秒杀：10000 并发下超卖 = 0，活动库存原子性保持；
- [ ] 优惠券一人一次、过期、叠加限制用例全过；
- [ ] **切流**：非大促期灰度 2 周；大促期间冻结切流。

---

### 阶段 6：用户、营销触达与商户（3 周）【切流模块 #6】

**范围接口**
| 区域 | 控制器 | 说明 |
|---|---|---|
| 会员 | `adminapi/user`（User/MemberTag/UserRank/PointsLog 等，约 80 个） | 会员体系 |
| 分销 | `adminapi/salesman`（10 个控制器）+ `/api/salesman/*` | 业务员/佣金 |
| 商户入驻 | `adminapi/merchant`（3 个）+ `/api/merchant/merchant` | 多商户 |
| B2B 批发 | `adminapi/wholesale`（4 个）+ `/api/wholesale` | 阶梯价、询价 |
| 消息/搜索后台 | `adminapi/msg`、`adminapi/search` | |

**验收标准**
- [ ] 分销佣金计算与 Java 版历史数据回放一致；
- [ ] 商户结算边界（平台/商户/供应商）账务核对通过；
- [ ] B2B 阶梯价、最小起订量、询价流程验收通过。

---

### 阶段 7：长尾模块与 Java 下线（2–4 周，可选）

**范围**：装修（decorate）、内容（article/相册）、打印（print）、IM、O2O、代理购物（proxyshopping）、统计面板（panel）、系统设置（setting）。

**建议**：
- 面板统计类（聚合查询多）→ 迁 Rust，性能收益明显；
- 装修/内容/IM 类（低频 CRUD、JSON 大对象）→ **评估后可长期保留 Java**，或最后迁移；
- 全部迁移完成后：下线 Java 进程与 `tigshop-feign`，归档源码。

**验收标准**
- [ ] 全量接口清单（1566 个）100% 标注处置：Rust 实现 / Java 代理 / 已废弃；
- [ ] Java 连续 30 天零流量；
- [ ] 压测：Rust 单实例 QPS ≥ Java 版 3 倍，P99 ≤ Java 版 50%，内存 ≤ 128MB。

---

## 5. 测试与回归策略（贯穿全程）

| 层级 | 手段 | 频率 |
|---|---|---|
| 单元测试 | `cargo test --workspace`（实体规则/JWT/密码/金额/状态机） | 每次提交 |
| 契约回放 | 录制 Java 真实响应 → Rust 回放 diff | 每阶段 CI |
| 影子流量 | 网关双发，比对响应 JSON | 每次切流前 |
| 并发压测 | 超卖、抢券、重复回调专项 | 阶段 3/4/5 |
| 资金对账 | 每日渠道账单 vs 本地 paylog | 阶段 4 起常驻 |
| 全链路回归 | 前端三端真实操作冒烟（下单/支付/售后/后台发货） | 每次放量前 |

---

## 6. 风险登记册

| 等级 | 风险 | 缓解措施 |
|---|---|---|
| P0 | 资金/回调不一致 | 阶段 4 沙箱+小额真实验证，未验收不放量，秒级回滚 |
| P0 | 路由契约偏差导致前端报错 | 阶段 0 先对齐全部路径并建立契约 CI |
| P1 | 覆盖遗漏（1566 接口） | 全量接口清单逐条打标，阶段 7 查漏 |
| P1 | 团队 Rust 生产经验不足 | 从低风险模块起步；阶段 0/1 配置代码评审门槛 |
| P1 | 三方 SDK（微信/支付宝）无 Rust 版 | trait + reqwest 封装，灰度期反代 Java |
| P2 | 大促期间故障 | 切流窗口避开大促，保留一键回退开关 |
| P2 | 历史数据口径 | 零迁移复用 MySQL，禁止改表结构 |

---

## 7. 里程碑汇总

| 阶段 | 内容 | 工期 | 出口条件 |
|---|---|---:|---|
| 0 | 契约对齐 + 接口清单 + 网关代理 | 2 周 | 100 接口抽样零偏差 |
| 1 | 认证与 RBAC | 2 周 | 登录链路 100% Rust 48h |
| 2 | 商品与目录 | 3 周 | 商品读 100% Rust 72h |
| 3 | 购物车/下单/订单/售后 | 4 周 | 零超卖零丢单，100% 放量 |
| 4 | 支付/结算/财务 | 4–6 周 | 对账零差异 |
| 5 | 营销促销 | 4 周 | 价格对拍零差异 |
| 6 | 会员/分销/商户/B2B | 3 周 | 账务核对通过 |
| 7 | 长尾 + Java 下线 | 2–4 周 | Java 30 天零流量 |
| **合计** | | **约 24–30 周** | |

---

## 8. 每阶段通用 DoD（Definition of Done）

1. 本阶段接口清单全部标注状态（已迁/代理/废弃）；
2. `cargo clippy --workspace` 零告警、`cargo test --workspace` 全绿；
3. 契约回放 diff = 0，影子比对无差异；
4. 监控就绪：延迟、错误率、业务指标（下单量/支付成功率）看板；
5. 回滚开关演练通过（从 Rust 切回 Java ≤ 1 分钟）；
6. 运维文档与配置（`config.yaml`）更新。

---

*生成日期：2026-10-13 ｜ 数据基线：源码静态统计（前台 332 + 后台 1234 = 1566 个方法级接口）*
