# TigShop Rust 重构：剩余工作路线图（v2 · 14 个限界上下文）

> **版本** 5.8.57　·　**结构契约** [`rust/docs/ARCHITECTURE.md`](ARCHITECTURE.md)（权威，v2）　·　**映射源** [`rust/docs/domain-map.json`](domain-map.json)
>
> **口径铁律**：进度只认 `python rust/tools/check_api_coverage.py`（静态提取真实注册路由）。
> 本文件的「已实现 / 剩余」来自该工具对重构后 `rust/crates` 的最新静态提取（635 / 1564）。
> 工作单位 = **一个 feature**（不是编号切片）；一次提交只动一个 feature 的四层文件 + 域聚合器。

---

## 0. 总览

| 指标 | 数值 | 说明 |
|------|------|------|
| 契约接口总数 | **1564** | `docs/api-contract.json`（246 个有端点的控制器 + 5 个零端点类） |
| 已实现 | **635** | 其中 554 条来自真实切片实现，81 条来自「切片清单生成前」的基线模块（附录 B） |
| **剩余未实现** | **929** | 落在 **149** 个 `ctx::feature` 上（附录 A 为逐条清单） |
| 切片 | 220 | **73 个真实实现**（coded=true，已由 codemod 迁入 14 个 ctx）；**59 个空脚手架**（已删除）；**88 个从未开始** |
| 限界上下文 | 14 | `identity / merchant / wholesale / catalog / trade / payment / marketing / member / logistics / content / system / im / analytics / openapi` |
| feature 槽位 | 228 | 220 个切片的 `ctx::feature` + 8 个多控制器切片的拆分子特性；其中 **79** 个已无剩余工作 |
| Java 控制器 | 251 | 全部映射到 `ctx::feature`（`domain-map.json.controllers[]`） |
| 已迁移基线模块 | 19 | 早期手写领域文件已由 codemod 迁入 19 个 `ctx::feature`（§3），无需人工认领 |

口径细节：

1. **73 真实 / 59 脚手架 / 88 未开始。** `docs/_migration-map.json` 的 `real=true`（73）才是真实实现；
   59 个切片文件是「已注册可编译、0 路由」的空脚手架，codemod 已删除；88 个切片从未生成代码。
   因此 `domain-map.json` 中只有这 73 个切片 `coded=true`。
2. **81 条基线接口不在任何切片中。** 它们在 220 切片清单生成之前就已实现（`slices.json` 的 1483 = 1564 − 81），
   现已随 19 个基线模块一起迁入目标 feature（附录 B 给出接口与迁移后文件），不再有剩余工作。
3. **多控制器切片需人工拆分。** `slice-001 / slice-112 / slice-137 / slice-158 / slice-194` 一个文件含多个控制器，
   `domain-map.json` 的 `split[]` 给出子特性；其中 `slice-001 / 137 / 194` 是真实实现，必须手工拆成多个 feature 文件。
4. **同名控制器已消歧。** 同名控制器跨切片共 13 组（`Search / User / AppVersion / BuyerShow / Account / ProductStyle / Region / Config / Comment / DecorateDiscrete / GiftCardGroups / Salesman / SalesmanMaterial`），
   已全部映射到不同 feature；228 个槽位 0 冲突。其中 59 个脚手架切片用到的 8 个重名/保留名在 `domain-map.json` 中带后缀（见 §2 表与各切片 notes）。

---

## 1. 优先级与并行策略

| 优先级 | 上下文 | 判定依据 |
|--------|--------|----------|
| **P0** | `identity` · `system` · `catalog` | 平台/基础设施：登录鉴权、配置、商品与分类是其余域的前置 |
| **P1** | `trade` · `payment` · `member` · `merchant` · `wholesale` · `logistics` | B2B 交易主链路（订单/支付/会员/店铺/批发/履约） |
| **P2** | `marketing` · `content` · `analytics` · `im` · `openapi` | 营销玩法、内容装修、统计、客服、开放接口：不阻塞主链路 |

并行建议：

- **先 P0 后 P1/P2**：同一 ctx 内的 feature 之间无写冲突，可并行认领；跨 ctx 有依赖的 feature 按 ctx 顺序领取。
- **共享聚合点串行改**：`core/src/models/mod.rs`、`services/src/lib.rs`、`infrastructure/src/db/repo/mod.rs`、
  `api/admin/src/routes/mod.rs`、`api/public/src/routes/mod.rs`、`AdminState/PublicState` 由每 ctx 一人统一追加。
- **一个 feature = 一个提交**：四层文件 + 域 `mod.rs` 注册 + 聚合器注册 + 覆盖率只增不减。

跨域依赖（装配时显式传入，禁止 `services` 反向依赖 `api`）：

- `trade`：依赖 catalog（商品/SKU/库存）与 member（收货地址）；跨域依赖通过 `trade::Services::new(repos, &catalog, &member)` 显式传入。
- `payment`：依赖 trade（订单/退款单）与 merchant（店铺资金）；账期/对账另需 member。
- `merchant`：依赖 payment（资金/保证金账户）与 member（店铺会员）。
- `logistics`：依赖 trade（发货/订单）与 merchant（店铺地址）。
- `marketing`：依赖 catalog（商品）、member（人群/等级）、trade（订单结算）。
- `content`：依赖 catalog（商品）用于装修/直播商品位。
- `analytics`：只读依赖各域聚合，不反向依赖。
- `im`：依赖 member（用户）与 merchant（店铺/客服配置）。
- `openapi`：依赖 catalog/trade 的用例层，不直连其 repo。

---

## 2. 分域路线图

> 每个 ctx 一节；表格只列 **仍有剩余接口** 的 feature（按剩余数降序），已完成特性在表下列出。
> `admin` = `/adminapi`（`api/admin`），`client` = `/api`（`api/public`），单位：接口条数。

### 2.1 `identity`（P0）— 管理员 / 角色 / 权限 / 登录 / 操作日志

剩余 **14** / 共 **36** 条接口；**2** 个特性待迁移，3 个已完成。
切片：真实实现 3 · 脚手架（已删）0 · 未开始 2。

| # | feature | 切片 | Java 控制器（源文件） | admin | client | 剩余/总计 | 状态 |
|---|---------|------|----------------------|-------|--------|-----------|------|
| 1 | `admin_account` | slice-196 | AdminAccountController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/account/AdminAccountController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 2 | `admin_role` | slice-180 | AdminRoleController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/authority/AdminRoleController.java</sub> | 7 | 0 | **7**/7 | 未开始 |

已完成（无需认领）：`admin_log`、`admin_user`、`authority`

<details><summary>本域四层目标路径模板</summary>

```
core         : crates/core/src/models/identity/<feature>.rs
infra        : crates/infrastructure/src/db/repo/identity/<feature>.rs
services     : crates/services/src/identity/<feature>.rs
api admin    : crates/api/admin/src/routes/identity/<feature>.rs
api public   : crates/api/public/src/routes/identity/<feature>.rs
注册点       : core/models/mod.rs · db/repo/mod.rs · services/lib.rs · routes/mod.rs
               · services/identity/mod.rs 的 Services 字段 · AdminState/PublicState 域字段
```

</details>

### 2.2 `system`（P0）— 设置 / 配置 / 地区 / 版本 / 模板 / 消息类型 / 文件 / CSRF / 缓存

剩余 **122** / 共 **178** 条接口；**11** 个特性待迁移，11 个已完成。
切片：真实实现 11 · 脚手架（已删）10 · 未开始 1。

| # | feature | 切片 | Java 控制器（源文件） | admin | client | 剩余/总计 | 状态 |
|---|---------|------|----------------------|-------|--------|-----------|------|
| 1 | `config_basic` | slice-187 | ConfigController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/ConfigController.java</sub> | 50 | 0 | **50**/50 | 未开始（脚手架已删） |
| 2 | `config_style` | slice-188 | ConfigController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/ConfigController.java</sub> | 50 | 0 | **50**/50 | 未开始（脚手架已删） |
| 3 | `admin_msg` | slice-165 | AdminMsgController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/msg/AdminMsgController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 4 | `licensed` | slice-186 | LicensedController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/LicensedController.java</sub> | 3 | 0 | **3**/3 | 未开始（脚手架已删） |
| 5 | `util` | slice-206 | UtilController<br><sub>tigshop-api/com/tigshop/api/controller/common/UtilController.java</sub> | 0 | 3 | **3**/3 | 未开始（脚手架已删） |
| 6 | `config_cashier` | slice-189 | ConfigController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/ConfigController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |
| 7 | `csrf` | slice-171 | CSRFController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/common/CSRFController.java</sub><br>CSRFController<br><sub>tigshop-api/com/tigshop/api/controller/common/CSRFController.java</sub> | 1 | 1 | **2**/2 | 未开始（脚手架已删） |
| 8 | `recommend` | slice-130 | RecommendController<br><sub>tigshop-api/com/tigshop/api/controller/common/RecommendController.java</sub> | 0 | 2 | **2**/2 | 未开始（脚手架已删） |
| 9 | `app_version_client` | slice-143 | AppVersionController<br><sub>tigshop-api/com/tigshop/api/controller/app/AppVersionController.java</sub> | 0 | 1 | **1**/1 | 未开始（脚手架已删） |
| 10 | `log` | slice-168 | LogController<br><sub>tigshop-api/com/tigshop/api/controller/common/LogController.java</sub> | 0 | 1 | **1**/1 | 未开始（脚手架已删） |
| 11 | `tips_manage` | slice-132 | TipsManageController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/common/TipsManageController.java</sub> | 1 | 0 | **1**/1 | 未开始（脚手架已删） |

已完成（无需认领）：`app_version_admin`、`area_code`、`cache_manage`、`config_client`、`example`、`files`、`mail_templates`、`message_type`、`pc`、`region`、`region_client`

<details><summary>本域四层目标路径模板</summary>

```
core         : crates/core/src/models/system/<feature>.rs
infra        : crates/infrastructure/src/db/repo/system/<feature>.rs
services     : crates/services/src/system/<feature>.rs
api admin    : crates/api/admin/src/routes/system/<feature>.rs
api public   : crates/api/public/src/routes/system/<feature>.rs
注册点       : core/models/mod.rs · db/repo/mod.rs · services/lib.rs · routes/mod.rs
               · services/system/mod.rs 的 Services 字段 · AdminState/PublicState 域字段
```

</details>

### 2.3 `catalog`（P0）— 商品 / 分类 / 品牌 / 属性 / 库存 / 评价 / 搜索 / 电子卡券 / 询价

剩余 **95** / 共 **176** 条接口；**14** 个特性待迁移，6 个已完成。
切片：真实实现 7 · 脚手架（已删）4 · 未开始 7。

| # | feature | 切片 | Java 控制器（源文件） | admin | client | 剩余/总计 | 状态 |
|---|---------|------|----------------------|-------|--------|-----------|------|
| 1 | `product_manage` | slice-001 | ProductController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/product/ProductController.java</sub><br>ProductController<br><sub>tigshop-api/com/tigshop/api/controller/product/ProductController.java</sub> | 20 | 14 | **32**/34 | 部分（2/34） |
| 2 | `comment` | slice-195 | CommentController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/product/CommentController.java</sub> | 8 | 0 | **8**/8 | 未开始 |
| 3 | `e_card_group` | slice-152 | ECardGroupController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/product/ECardGroupController.java</sub> | 8 | 0 | **8**/8 | 未开始 |
| 4 | `product_group` | slice-080 | ProductGroupController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/product/ProductGroupController.java</sub> | 8 | 0 | **8**/8 | 未开始 |
| 5 | `e_card` | slice-151 | ECardController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/product/ECardController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 6 | `services` | slice-083 | ServicesController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/product/ServicesController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 7 | `comment_client` | slice-071 | CommentController<br><sub>tigshop-api/com/tigshop/api/controller/user/CommentController.java</sub> | 0 | 5 | **5**/5 | 未开始 |
| 8 | `price_inquiry` | slice-211 | PriceInquiryController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/product/PriceInquiryController.java</sub> | 5 | 0 | **5**/5 | 未开始 |
| 9 | `product_search` | slice-001 | SearchController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/search/SearchController.java</sub><br>SearchController<br><sub>tigshop-api/com/tigshop/api/controller/search/SearchController.java</sub><br>SearchGuessController<br><sub>tigshop-api/com/tigshop/api/controller/search/SearchGuessController.java</sub> | 0 | 4 | **4**/4 | 真实实现（0 路由?） |
| 10 | `exchange` | slice-160 | ExchangeController<br><sub>tigshop-api/com/tigshop/api/controller/product/ExchangeController.java</sub> | 0 | 3 | **3**/3 | 未开始（脚手架已删） |
| 11 | `product_style_client` | slice-084 | ProductStyleController<br><sub>tigshop-api/com/tigshop/api/controller/product/ProductStyleController.java</sub> | 0 | 3 | **3**/3 | 未开始（脚手架已删） |
| 12 | `product_batch` | slice-001 | ProductBatchController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/product/ProductBatchController.java</sub> | 5 | 0 | **2**/5 | 部分（3/5） |
| 13 | `search_admin` | slice-051 | SearchController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/search/SearchController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |
| 14 | `product_inventory_log` | slice-100 | ProductInventoryLogController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/product/ProductInventoryLogController.java</sub> | 1 | 0 | **1**/1 | 未开始（脚手架已删） |

已完成（无需认领）：`attributes_tpl`、`brand`、`category`、`product_stock`、`short_url`、`stock_warning`

<details><summary>本域四层目标路径模板</summary>

```
core         : crates/core/src/models/catalog/<feature>.rs
infra        : crates/infrastructure/src/db/repo/catalog/<feature>.rs
services     : crates/services/src/catalog/<feature>.rs
api admin    : crates/api/admin/src/routes/catalog/<feature>.rs
api public   : crates/api/public/src/routes/catalog/<feature>.rs
注册点       : core/models/mod.rs · db/repo/mod.rs · services/lib.rs · routes/mod.rs
               · services/catalog/mod.rs 的 Services 字段 · AdminState/PublicState 域字段
```

</details>

### 2.4 `trade`（P1）— 购物车 / 订单 / 售后 / 退款 / 发票 / 结算 / 代客下单

剩余 **60** / 共 **106** 条接口；**9** 个特性待迁移，8 个已完成。
切片：真实实现 9 · 脚手架（已删）5 · 未开始 3。

| # | feature | 切片 | Java 控制器（源文件） | admin | client | 剩余/总计 | 状态 |
|---|---------|------|----------------------|-------|--------|-----------|------|
| 1 | `order_admin` | slice-002 | OrderController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/order/OrderController.java</sub> | 30 | 0 | **30**/30 | 真实实现（0 路由?） |
| 2 | `proxy_shopping_cart` | slice-033 | ProxyShoppingCartController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/proxyshopping/ProxyShoppingCartController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 3 | `check` | slice-184 | CheckController<br><sub>tigshop-api/com/tigshop/api/controller/order/CheckController.java</sub> | 0 | 6 | **6**/6 | 未开始 |
| 4 | `user_order` | slice-070 | UserOrderController<br><sub>tigshop-api/com/tigshop/api/controller/user/UserOrderController.java</sub> | 0 | 6 | **6**/6 | 未开始 |
| 5 | `order_invoice_client` | slice-066 | OrderInvoiceClientController<br><sub>tigshop-api/com/tigshop/api/controller/user/OrderInvoiceClientController.java</sub> | 0 | 3 | **3**/3 | 未开始（脚手架已删） |
| 6 | `cart_summary` | slice-200 | CartController<br><sub>tigshop-api/com/tigshop/api/controller/cart/CartController.java</sub> | 0 | 2 | **2**/2 | 未开始（脚手架已删） |
| 7 | `order_log` | slice-181 | OrderLogController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/order/OrderLogController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |
| 8 | `proxy_shopping_delivery` | slice-034 | ProxyShoppingDeliveryController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/proxyshopping/ProxyShoppingDeliveryController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |
| 9 | `shop_order_config` | slice-133 | ShopOrderConfigController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/order/ShopOrderConfigController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |

已完成（无需认领）：`aftersales`、`aftersales_client`、`proxy_shopping_order_check`、`proxy_shopping_pickup_contact`、`proxy_shopping_product`、`proxy_shopping_user`、`proxy_shopping_user_address`、`proxy_shopping_user_coupon`

<details><summary>本域四层目标路径模板</summary>

```
core         : crates/core/src/models/trade/<feature>.rs
infra        : crates/infrastructure/src/db/repo/trade/<feature>.rs
services     : crates/services/src/trade/<feature>.rs
api admin    : crates/api/admin/src/routes/trade/<feature>.rs
api public   : crates/api/public/src/routes/trade/<feature>.rs
注册点       : core/models/mod.rs · db/repo/mod.rs · services/lib.rs · routes/mod.rs
               · services/trade/mod.rs 的 Services 字段 · AdminState/PublicState 域字段
```

</details>

### 2.5 `payment`（P1）— 支付单 / 渠道 / 余额 / 充值 / 提现 / 账期 / 对账 / 分账

剩余 **115** / 共 **131** 条接口；**24** 个特性待迁移，3 个已完成。
切片：真实实现 3 · 脚手架（已删）10 · 未开始 14。

| # | feature | 切片 | Java 控制器（源文件） | admin | client | 剩余/总计 | 状态 |
|---|---------|------|----------------------|-------|--------|-----------|------|
| 1 | `merchant_application` | slice-202 | MerchantApplicationController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/finance/MerchantApplicationController.java</sub> | 10 | 0 | **10**/10 | 未开始 |
| 2 | `category_fee_rule` | slice-079 | CategoryFeeRuleController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/finance/CategoryFeeRuleController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 3 | `order_invoice` | slice-077 | OrderInvoiceController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/finance/OrderInvoiceController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 4 | `recharge_order` | slice-074 | RechargeOrderController<br><sub>tigshop-api/com/tigshop/api/controller/user/RechargeOrderController.java</sub> | 0 | 7 | **7**/7 | 未开始 |
| 5 | `refund_log` | slice-204 | RefundLogController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/finance/RefundLogController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 6 | `user_invoice` | slice-078 | UserInvoiceController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/finance/UserInvoiceController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 7 | `user_recharge_order` | slice-049 | UserRechargeOrderController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/finance/UserRechargeOrderController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 8 | `user_withdraw_apply` | slice-131 | UserWithdrawApplyController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/finance/UserWithdrawApplyController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 9 | `with_draw_apply_client` | slice-068 | WithDrawApplyClientController<br><sub>tigshop-api/com/tigshop/api/controller/user/WithDrawApplyClientController.java</sub> | 0 | 7 | **7**/7 | 未开始 |
| 10 | `credit_settlement` | slice-198 | CreditSettlementController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/finance/CreditSettlementController.java</sub> | 6 | 0 | **6**/6 | 未开始 |
| 11 | `credit_user` | slice-129 | CreditUserController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/finance/CreditUserController.java</sub> | 6 | 0 | **6**/6 | 未开始 |
| 12 | `pay` | slice-182 | PayController<br><sub>tigshop-api/com/tigshop/api/controller/order/PayController.java</sub> | 0 | 6 | **6**/6 | 未开始 |
| 13 | `statement` | slice-099 | StatementController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/finance/StatementController.java</sub> | 6 | 0 | **6**/6 | 未开始 |
| 14 | `apple_pay` | slice-183 | ApplePayController<br><sub>tigshop-api/com/tigshop/api/controller/order/ApplePayController.java</sub> | 0 | 4 | **4**/4 | 未开始 |
| 15 | `profit_sharing` | slice-056 | ProfitSharingController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/finance/ProfitSharingController.java</sub> | 3 | 0 | **3**/3 | 未开始（脚手架已删） |
| 16 | `refund_apply_admin` | slice-203 | RefundApplyController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/finance/RefundApplyController.java</sub> | 10 | 0 | **3**/10 | 未开始（脚手架已删） |
| 17 | `sub_merchant` | slice-093 | SubMerchantController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/finance/SubMerchantController.java</sub> | 3 | 0 | **3**/3 | 未开始（脚手架已删） |
| 18 | `apple_pay_config` | slice-190 | ApplePayConfigController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/ApplePayConfigController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |
| 19 | `huifu_config` | slice-193 | HuifuConfigController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/HuifuConfigController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |
| 20 | `lakala_config` | slice-191 | LakalaConfigController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/LakalaConfigController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |
| 21 | `user_account` | slice-073 | AccountController<br><sub>tigshop-api/com/tigshop/api/controller/user/AccountController.java</sub> | 0 | 2 | **2**/2 | 未开始（脚手架已删） |
| 22 | `yeepay_config` | slice-192 | YeepayConfigController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/YeepayConfigController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |
| 23 | `account_panel` | slice-197 | AccountPanelController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/finance/AccountPanelController.java</sub> | 1 | 0 | **1**/1 | 未开始（脚手架已删） |
| 24 | `credit_order` | slice-199 | CreditOrderController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/finance/CreditOrderController.java</sub> | 1 | 0 | **1**/1 | 未开始（脚手架已删） |

已完成（无需认领）：`paylog_batch`、`tax_rate`、`user_balance_log`

<details><summary>本域四层目标路径模板</summary>

```
core         : crates/core/src/models/payment/<feature>.rs
infra        : crates/infrastructure/src/db/repo/payment/<feature>.rs
services     : crates/services/src/payment/<feature>.rs
api admin    : crates/api/admin/src/routes/payment/<feature>.rs
api public   : crates/api/public/src/routes/payment/<feature>.rs
注册点       : core/models/mod.rs · db/repo/mod.rs · services/lib.rs · routes/mod.rs
               · services/payment/mod.rs 的 Services 字段 · AdminState/PublicState 域字段
```

</details>

### 2.6 `member`（P1）— 用户 / 会员等级 / 标签 / 实名 / 企业认证 / 站内信 / 留言 / 收藏 / 签到

剩余 **71** / 共 **137** 条接口；**14** 个特性待迁移，8 个已完成。
切片：真实实现 8 · 脚手架（已删）7 · 未开始 7。

| # | feature | 切片 | Java 控制器（源文件） | admin | client | 剩余/总计 | 状态 |
|---|---------|------|----------------------|-------|--------|-----------|------|
| 1 | `login` | slice-153 | LoginController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/user/LoginController.java</sub><br>LoginController<br><sub>tigshop-api/com/tigshop/api/controller/user/LoginController.java</sub> | 1 | 15 | **16**/16 | 未开始 |
| 2 | `shop_member` | slice-101 | ShopMemberController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/user/ShopMemberController.java</sub> | 9 | 0 | **9**/9 | 未开始 |
| 3 | `user_address_client` | slice-149 | UserAddressClientController<br><sub>tigshop-api/com/tigshop/api/controller/user/UserAddressClientController.java</sub> | 0 | 7 | **7**/7 | 未开始 |
| 4 | `user_message` | slice-069 | UserMessageController<br><sub>tigshop-api/com/tigshop/api/controller/user/UserMessageController.java</sub> | 0 | 6 | **6**/6 | 未开始 |
| 5 | `user_member_tag` | slice-150 | UserMemberTagController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/user/UserMemberTagController.java</sub> | 5 | 0 | **5**/5 | 未开始 |
| 6 | `user_message_log` | slice-040 | UserMessageLogController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/user/UserMessageLogController.java</sub> | 5 | 0 | **5**/5 | 未开始（脚手架已删） |
| 7 | `user_invoice_client` | slice-067 | UserInvoiceClientController<br><sub>tigshop-api/com/tigshop/api/controller/user/UserInvoiceClientController.java</sub> | 0 | 4 | **4**/4 | 未开始 |
| 8 | `user_price_inquiry` | slice-072 | UserPriceInquiryController<br><sub>tigshop-api/com/tigshop/api/controller/user/UserPriceInquiryController.java</sub> | 0 | 4 | **4**/4 | 未开始 |
| 9 | `captcha_api` | slice-170 | CaptchaApiController<br><sub>tigshop-api/com/tigshop/api/controller/user/CaptchaApiController.java</sub> | 0 | 3 | **3**/3 | 未开始（脚手架已删） |
| 10 | `collect_product` | slice-082 | CollectProductController<br><sub>tigshop-api/com/tigshop/api/controller/user/CollectProductController.java</sub> | 0 | 3 | **3**/3 | 未开始（脚手架已删） |
| 11 | `user_company_client` | slice-148 | UserCompanyClientController<br><sub>tigshop-api/com/tigshop/api/controller/user/UserCompanyClientController.java</sub> | 0 | 3 | **3**/3 | 未开始（脚手架已删） |
| 12 | `member_audience` | slice-036 | MemberAudienceController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/user/MemberAudienceController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |
| 13 | `user_feedback_client` | slice-065 | UserFeedbackClientController<br><sub>tigshop-api/com/tigshop/api/controller/user/UserFeedbackClientController.java</sub> | 0 | 2 | **2**/2 | 未开始（脚手架已删） |
| 14 | `user_sign` | slice-214 | UserSignController<br><sub>tigshop-api/com/tigshop/api/controller/user/UserSignController.java</sub> | 0 | 2 | **2**/2 | 未开始（脚手架已删） |

已完成（无需认领）：`feedback`、`member_tag`、`member_tag_group`、`user_center`、`user_company`、`user_manage`、`user_points_log`、`user_rank`

<details><summary>本域四层目标路径模板</summary>

```
core         : crates/core/src/models/member/<feature>.rs
infra        : crates/infrastructure/src/db/repo/member/<feature>.rs
services     : crates/services/src/member/<feature>.rs
api admin    : crates/api/admin/src/routes/member/<feature>.rs
api public   : crates/api/public/src/routes/member/<feature>.rs
注册点       : core/models/mod.rs · db/repo/mod.rs · services/lib.rs · routes/mod.rs
               · services/member/mod.rs 的 Services 字段 · AdminState/PublicState 域字段
```

</details>

### 2.7 `merchant`（P1）— 店铺 / 商户 / 入驻 / 店铺员工 / 保证金 / 店铺资金

剩余 **93** / 共 **156** 条接口；**15** 个特性待迁移，7 个已完成。
切片：真实实现 7 · 脚手架（已删）3 · 未开始 12。

| # | feature | 切片 | Java 控制器（源文件） | admin | client | 剩余/总计 | 状态 |
|---|---------|------|----------------------|-------|--------|-----------|------|
| 1 | `deposit_recharge` | slice-103 | DepositRechargeController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/deposit/DepositRechargeController.java</sub> | 10 | 0 | **10**/10 | 未开始 |
| 2 | `admin_user_shop` | slice-107 | AdminUserShopController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/shop/AdminUserShopController.java</sub> | 9 | 0 | **9**/9 | 未开始 |
| 3 | `deposit_account` | slice-104 | DepositAccountController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/deposit/DepositAccountController.java</sub> | 8 | 0 | **8**/8 | 未开始 |
| 4 | `merchant` | slice-088 | MerchantController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/merchant/MerchantController.java</sub> | 8 | 0 | **8**/8 | 未开始 |
| 5 | `merchant_apply` | slice-050 | MerchantApplyController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/merchant/MerchantApplyController.java</sub> | 8 | 0 | **8**/8 | 未开始 |
| 6 | `shop_funds` | slice-110 | ShopFundsController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/shop/ShopFundsController.java</sub> | 8 | 0 | **8**/8 | 未开始 |
| 7 | `shop_withdraw` | slice-086 | ShopWithdrawController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/shop/ShopWithdrawController.java</sub> | 12 | 0 | **8**/12 | 未开始 |
| 8 | `deposit_refund` | slice-105 | DepositRefundController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/deposit/DepositRefundController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 9 | `merchant_account` | slice-089 | MerchantAccountController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/merchant/MerchantAccountController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 10 | `deposit_rule` | slice-046 | DepositRuleController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/deposit/DepositRuleController.java</sub> | 6 | 0 | **6**/6 | 未开始 |
| 11 | `deposit_shop` | slice-109 | DepositShopController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/deposit/DepositShopController.java</sub> | 5 | 0 | **5**/5 | 未开始 |
| 12 | `deposit_payee_account` | slice-045 | DepositPayeeAccountController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/deposit/DepositPayeeAccountController.java</sub> | 4 | 0 | **4**/4 | 未开始 |
| 13 | `deposit_log` | slice-102 | DepositLogController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/deposit/DepositLogController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |
| 14 | `deposit_setting` | slice-047 | DepositSettingController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/deposit/DepositSettingController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |
| 15 | `shop_account_log` | slice-111 | AccountController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/shop/AccountController.java</sub> | 2 | 0 | **1**/2 | 未开始（脚手架已删） |

已完成（无需认领）：`merchant_client`、`order_config`、`shop_client`、`shop_client_category`、`shop_manage`、`shop_product_category`、`vendor_deposit_shop`

<details><summary>本域四层目标路径模板</summary>

```
core         : crates/core/src/models/merchant/<feature>.rs
infra        : crates/infrastructure/src/db/repo/merchant/<feature>.rs
services     : crates/services/src/merchant/<feature>.rs
api admin    : crates/api/admin/src/routes/merchant/<feature>.rs
api public   : crates/api/public/src/routes/merchant/<feature>.rs
注册点       : core/models/mod.rs · db/repo/mod.rs · services/lib.rs · routes/mod.rs
               · services/merchant/mod.rs 的 Services 字段 · AdminState/PublicState 域字段
```

</details>

### 2.8 `wholesale`（P1）— 批发商 / 等级 / 批发商品 / 批发设置

剩余 **27** / 共 **30** 条接口；**4** 个特性待迁移，1 个已完成。
切片：真实实现 1 · 脚手架（已删）1 · 未开始 3。

| # | feature | 切片 | Java 控制器（源文件） | admin | client | 剩余/总计 | 状态 |
|---|---------|------|----------------------|-------|--------|-----------|------|
| 1 | `wholesale_product` | slice-121 | WholesaleProductController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/wholesale/WholesaleProductController.java</sub> | 13 | 0 | **13**/13 | 未开始 |
| 2 | `wholesaler` | slice-123 | WholesalerController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/wholesale/WholesalerController.java</sub> | 8 | 0 | **8**/8 | 未开始 |
| 3 | `wholesaler_grade` | slice-122 | WholesalerGradeController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/wholesale/WholesalerGradeController.java</sub> | 4 | 0 | **4**/4 | 未开始 |
| 4 | `wholesale_setting` | slice-124 | WholesaleSettingController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/wholesale/WholesaleSettingController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |

已完成（无需认领）：`wholesale_client`

<details><summary>本域四层目标路径模板</summary>

```
core         : crates/core/src/models/wholesale/<feature>.rs
infra        : crates/infrastructure/src/db/repo/wholesale/<feature>.rs
services     : crates/services/src/wholesale/<feature>.rs
api admin    : crates/api/admin/src/routes/wholesale/<feature>.rs
api public   : crates/api/public/src/routes/wholesale/<feature>.rs
注册点       : core/models/mod.rs · db/repo/mod.rs · services/lib.rs · routes/mod.rs
               · services/wholesale/mod.rs 的 Services 字段 · AdminState/PublicState 域字段
```

</details>

### 2.9 `logistics`（P1）— 运费模板 / 配送方式 / 物流公司 / 打印

剩余 **9** / 共 **42** 条接口；**2** 个特性待迁移，6 个已完成。
切片：真实实现 6 · 脚手架（已删）1 · 未开始 1。

| # | feature | 切片 | Java 控制器（源文件） | admin | client | 剩余/总计 | 状态 |
|---|---------|------|----------------------|-------|--------|-----------|------|
| 1 | `print` | slice-119 | PrintController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/print/PrintController.java</sub> | 8 | 0 | **8**/8 | 未开始 |
| 2 | `print_log` | slice-118 | PrintLogController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/print/PrintLogController.java</sub> | 1 | 0 | **1**/1 | 未开始（脚手架已删） |

已完成（无需认领）：`logistics_company`、`logistics_company_config`、`print_config`、`shipping_tpl`、`shipping_type`、`sunmi_print_callback`

<details><summary>本域四层目标路径模板</summary>

```
core         : crates/core/src/models/logistics/<feature>.rs
infra        : crates/infrastructure/src/db/repo/logistics/<feature>.rs
services     : crates/services/src/logistics/<feature>.rs
api admin    : crates/api/admin/src/routes/logistics/<feature>.rs
api public   : crates/api/public/src/routes/logistics/<feature>.rs
注册点       : core/models/mod.rs · db/repo/mod.rs · services/lib.rs · routes/mod.rs
               · services/logistics/mod.rs 的 Services 字段 · AdminState/PublicState 域字段
```

</details>

### 2.10 `marketing`（P2）— 优惠券 / 秒杀 / 拼团 / 限时折扣 / 积分 / 礼品卡 / 抽奖 / 买家秀 / 分销

剩余 **172** / 共 **225** 条接口；**30** 个特性待迁移，6 个已完成。
切片：真实实现 6 · 脚手架（已删）8 · 未开始 21。

| # | feature | 切片 | Java 控制器（源文件） | admin | client | 剩余/总计 | 状态 |
|---|---------|------|----------------------|-------|--------|-----------|------|
| 1 | `groupon_promotion` | slice-127 | GrouponPromotionController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/promotion/GrouponPromotionController.java</sub><br>GrouponPromotionController<br><sub>tigshop-api/com/tigshop/api/controller/promotion/GrouponPromotionController.java</sub> | 8 | 4 | **12**/12 | 未开始 |
| 2 | `salesman_content` | slice-058 | SalesmanContentController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/salesman/SalesmanContentController.java</sub><br>SalesmanContentController<br><sub>tigshop-api/com/tigshop/api/controller/salesman/SalesmanContentController.java</sub> | 8 | 2 | **10**/10 | 未开始 |
| 3 | `coupon_manage` | slice-035 | CouponController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/promotion/CouponController.java</sub> | 9 | 0 | **9**/9 | 未开始 |
| 4 | `product_promotion` | slice-142 | ProductPromotionController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/promotion/ProductPromotionController.java</sub> | 9 | 0 | **9**/9 | 未开始 |
| 5 | `gift_card` | slice-158 | GiftCardController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/promotion/GiftCardController.java</sub> | 8 | 0 | **8**/8 | 未开始 |
| 6 | `gift_card_groups` | slice-159 | GiftCardGroupsController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/promotion/GiftCardGroupsController.java</sub> | 8 | 0 | **8**/8 | 未开始 |
| 7 | `groupon_promotion_item` | slice-212 | GrouponPromotionItemController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/promotion/GrouponPromotionItemController.java</sub> | 8 | 0 | **8**/8 | 未开始 |
| 8 | `lottery_activity` | slice-126 | LotteryActivityController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/promotion/LotteryActivityController.java</sub> | 8 | 0 | **8**/8 | 未开始 |
| 9 | `salesman_admin` | slice-060 | SalesmanController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/salesman/SalesmanController.java</sub> | 9 | 0 | **8**/9 | 未开始 |
| 10 | `salesman_product` | slice-062 | SalesmanProductController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/salesman/SalesmanProductController.java</sub><br>SalesmanProductController<br><sub>tigshop-api/com/tigshop/api/controller/salesman/SalesmanProductController.java</sub> | 6 | 2 | **8**/8 | 未开始 |
| 11 | `points_exchange` | slice-161 | PointsExchangeController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/promotion/PointsExchangeController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 12 | `product_gift` | slice-138 | ProductGiftController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/promotion/ProductGiftController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 13 | `recharge_setting` | slice-048 | RechargeSettingController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/promotion/RechargeSettingController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 14 | `salesman_material` | slice-167 | SalesmanMaterialController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/salesman/SalesmanMaterialController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 15 | `sign_in_setting` | slice-162 | SignInSettingController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/promotion/SignInSettingController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 16 | `gift_card_client` | slice-158 | GiftCardGroupsController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/promotion/GiftCardGroupsController.java</sub><br>GiftCardGroupsController<br><sub>tigshop-api/com/tigshop/api/controller/promotion/GiftCardGroupsController.java</sub> | 0 | 6 | **6**/6 | 未开始 |
| 17 | `groupon_promotion_record` | slice-128 | GrouponPromotionRecordController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/promotion/GrouponPromotionRecordController.java</sub> | 5 | 0 | **5**/5 | 未开始 |
| 18 | `lottery` | slice-125 | LotteryController<br><sub>tigshop-api/com/tigshop/api/controller/promotion/LotteryController.java</sub> | 0 | 5 | **5**/5 | 未开始 |
| 19 | `salesman_client` | slice-061 | SalesmanController<br><sub>tigshop-api/com/tigshop/api/controller/salesman/SalesmanController.java</sub> | 0 | 5 | **5**/5 | 未开始 |
| 20 | `salesman_group` | slice-055 | SalesmanGroupController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/salesman/SalesmanGroupController.java</sub> | 5 | 0 | **5**/5 | 未开始 |
| 21 | `promotion` | slice-169 | PromotionController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/promotion/PromotionController.java</sub> | 4 | 0 | **4**/4 | 未开始 |
| 22 | `salesman_order` | slice-057 | SalesmanOrderController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/salesman/SalesmanOrderController.java</sub><br>SalesmanOrderController<br><sub>tigshop-api/com/tigshop/api/controller/salesman/SalesmanOrderController.java</sub> | 3 | 1 | **4**/4 | 未开始 |
| 23 | `salesman_material_client` | slice-059 | SalesmanMaterialController<br><sub>tigshop-api/com/tigshop/api/controller/salesman/SalesmanMaterialController.java</sub> | 0 | 3 | **3**/3 | 未开始（脚手架已删） |
| 24 | `seckill_batch` | slice-217 | SeckillController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/promotion/SeckillController.java</sub> | 3 | 0 | **3**/3 | 未开始（脚手架已删） |
| 25 | `groupon_records` | slice-213 | GrouponRecordsController<br><sub>tigshop-api/com/tigshop/api/controller/promotion/GrouponRecordsController.java</sub> | 0 | 2 | **2**/2 | 未开始（脚手架已删） |
| 26 | `overview` | slice-063 | OverviewController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/salesman/OverviewController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |
| 27 | `salesman_config` | slice-064 | SalesmanConfigController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/salesman/SalesmanConfigController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |
| 28 | `salesman_card_invite` | slice-117 | SalesmanCardInviteController<br><sub>tigshop-api/com/tigshop/api/controller/salesman/SalesmanCardInviteController.java</sub> | 0 | 1 | **1**/1 | 未开始（脚手架已删） |
| 29 | `salesman_customer_transaction` | slice-094 | SalesmanCustomerTransactionController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/salesman/SalesmanCustomerTransactionController.java</sub> | 1 | 0 | **1**/1 | 未开始（脚手架已删） |
| 30 | `time_discount_batch` | slice-216 | TimeDiscountController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/promotion/TimeDiscountController.java</sub> | 1 | 0 | **1**/1 | 未开始（脚手架已删） |

已完成（无需认领）：`buyer_show`、`buyer_show_client`、`buyer_show_comment`、`buyer_show_group`、`product_style_admin`、`salesman_material_category`

<details><summary>本域四层目标路径模板</summary>

```
core         : crates/core/src/models/marketing/<feature>.rs
infra        : crates/infrastructure/src/db/repo/marketing/<feature>.rs
services     : crates/services/src/marketing/<feature>.rs
api admin    : crates/api/admin/src/routes/marketing/<feature>.rs
api public   : crates/api/public/src/routes/marketing/<feature>.rs
注册点       : core/models/mod.rs · db/repo/mod.rs · services/lib.rs · routes/mod.rs
               · services/marketing/mod.rs 的 Services 字段 · AdminState/PublicState 域字段
```

</details>

### 2.11 `content`（P2）— 文章 / 装修 / 素材 / 相册 / 导航 / SEO / 友链 / 首页 / 公众号 / 直播

剩余 **105** / 共 **174** 条接口；**19** 个特性待迁移，10 个已完成。
切片：真实实现 6 · 脚手架（已删）8 · 未开始 14。

| # | feature | 切片 | Java 控制器（源文件） | admin | client | 剩余/总计 | 状态 |
|---|---------|------|----------------------|-------|--------|-----------|------|
| 1 | `home` | slice-219 | HomeController<br><sub>tigshop-api/com/tigshop/api/controller/home/HomeController.java</sub> | 0 | 18 | **16**/18 | 未开始 |
| 2 | `wechat_live` | slice-112 | WechatLiveController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/content/WechatLiveController.java</sub> | 14 | 0 | **14**/14 | 未开始 |
| 3 | `gallery_pic` | slice-154 | GalleryPicController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/GalleryPicController.java</sub> | 10 | 0 | **10**/10 | 未开始 |
| 4 | `gallery` | slice-155 | GalleryController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/GalleryController.java</sub> | 9 | 0 | **9**/9 | 未开始 |
| 5 | `friend_links` | slice-076 | FriendLinksController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/FriendLinksController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 6 | `gallery_video` | slice-146 | GalleryVideoController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/GalleryVideoController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 7 | `gallery_video_info` | slice-145 | GalleryVideoInfoController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/GalleryVideoInfoController.java</sub> | 6 | 0 | **6**/6 | 未开始 |
| 8 | `wechat_auto_reply` | slice-054 | WechatAutoReplyController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/wechat/WechatAutoReplyController.java</sub> | 6 | 0 | **6**/6 | 未开始 |
| 9 | `wechat_keyword_rule` | slice-052 | WechatKeywordRuleController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/wechat/WechatKeywordRuleController.java</sub> | 5 | 0 | **5**/5 | 未开始 |
| 10 | `wechat_video_account` | slice-179 | WechatVideoAccountController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/content/WechatVideoAccountController.java</sub> | 5 | 0 | **5**/5 | 未开始 |
| 11 | `article_batch` | slice-135 | ArticleController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/content/ArticleController.java</sub><br>ArticleController<br><sub>tigshop-api/com/tigshop/api/controller/article/ArticleController.java</sub> | 1 | 2 | **3**/3 | 未开始（脚手架已删） |
| 12 | `article_category` | slice-136 | ArticleCategoryController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/content/ArticleCategoryController.java</sub> | 3 | 0 | **3**/3 | 未开始（脚手架已删） |
| 13 | `decorate_template` | slice-176 | DecorateTemplateController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/decorate/DecorateTemplateController.java</sub> | 3 | 0 | **3**/3 | 未开始（脚手架已删） |
| 14 | `wechat_live_goods` | slice-113 | WechatLiveGoodsController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/content/WechatLiveGoodsController.java</sub> | 3 | 0 | **3**/3 | 未开始（脚手架已删） |
| 15 | `wechat_live_order_sync` | slice-114 | WechatLiveOrderSyncController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/content/WechatLiveOrderSyncController.java</sub> | 3 | 0 | **3**/3 | 未开始（脚手架已删） |
| 16 | `wechat_menu` | slice-116 | WechatMenuController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/wechat/WechatMenuController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |
| 17 | `api_wechat_live` | slice-112 | ApiWechatLiveController<br><sub>tigshop-api/com/tigshop/api/controller/content/ApiWechatLiveController.java</sub> | 0 | 1 | **1**/1 | 未开始 |
| 18 | `decorate_promote` | slice-174 | DecoratePromoteController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/decorate/DecoratePromoteController.java</sub> | 3 | 0 | **1**/3 | 未开始（脚手架已删） |
| 19 | `wechat_kefu_capability` | slice-053 | WechatKefuCapabilityController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/setting/wechat/WechatKefuCapabilityController.java</sub> | 1 | 0 | **1**/1 | 未开始（脚手架已删） |

已完成（无需认领）：`decorate_discrete`、`decorate_discrete_client`、`decorate_manage`、`decorate_share`、`mobile_cat_nav`、`pc_cat_floor`、`pc_navigation`、`seo`、`share`、`wechat_live_sync_callback`

<details><summary>本域四层目标路径模板</summary>

```
core         : crates/core/src/models/content/<feature>.rs
infra        : crates/infrastructure/src/db/repo/content/<feature>.rs
services     : crates/services/src/content/<feature>.rs
api admin    : crates/api/admin/src/routes/content/<feature>.rs
api public   : crates/api/public/src/routes/content/<feature>.rs
注册点       : core/models/mod.rs · db/repo/mod.rs · services/lib.rs · routes/mod.rs
               · services/content/mod.rs 的 Services 字段 · AdminState/PublicState 域字段
```

</details>

### 2.12 `analytics`（P2）— 面板 / 统计 / 访问日志 / 销售统计

剩余 **12** / 共 **16** 条接口；**2** 个特性待迁移，2 个已完成。
切片：真实实现 1 · 脚手架（已删）0 · 未开始 2。

| # | feature | 切片 | Java 控制器（源文件） | admin | client | 剩余/总计 | 状态 |
|---|---------|------|----------------------|-------|--------|-----------|------|
| 1 | `panel` | slice-218 | PanelController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/panel/PanelController.java</sub> | 7 | 0 | **7**/7 | 未开始 |
| 2 | `sales_statistics` | slice-215 | SalesStatisticsController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/panel/SalesStatisticsController.java</sub> | 5 | 0 | **5**/5 | 未开始 |

已完成（无需认领）：`statistics`、`statistics_user`

<details><summary>本域四层目标路径模板</summary>

```
core         : crates/core/src/models/analytics/<feature>.rs
infra        : crates/infrastructure/src/db/repo/analytics/<feature>.rs
services     : crates/services/src/analytics/<feature>.rs
api admin    : crates/api/admin/src/routes/analytics/<feature>.rs
api public   : crates/api/public/src/routes/analytics/<feature>.rs
注册点       : core/models/mod.rs · db/repo/mod.rs · services/lib.rs · routes/mod.rs
               · services/analytics/mod.rs 的 Services 字段 · AdminState/PublicState 域字段
```

</details>

### 2.13 `im`（P2）— 客服 / 会话 / 消息 / 客服人员 / 接入规则

剩余 **34** / 共 **48** 条接口；**3** 个特性待迁移，4 个已完成。
切片：真实实现 4 · 脚手架（已删）2 · 未开始 1。

| # | feature | 切片 | Java 控制器（源文件） | admin | client | 剩余/总计 | 状态 |
|---|---------|------|----------------------|-------|--------|-----------|------|
| 1 | `im_conversation` | slice-097 | ImConversationController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/im/ImConversationController.java</sub><br>ImConversationController<br><sub>tigshop-api/com/tigshop/api/controller/im/ImConversationController.java</sub> | 11 | 12 | **23**/23 | 未开始（脚手架已删） |
| 2 | `admin_user_kefu` | slice-096 | AdminUserKefuController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/im/AdminUserKefuController.java</sub> | 9 | 0 | **9**/9 | 未开始 |
| 3 | `admin_shop_kefu_config` | slice-098 | AdminShopKefuConfigController<br><sub>tigshop-adminapi/com/tigshop/adminapi/controller/im/AdminShopKefuConfigController.java</sub> | 2 | 0 | **2**/2 | 未开始（脚手架已删） |

已完成（无需认领）：`im_config`、`im_message`、`im_servant`、`web_socket`

<details><summary>本域四层目标路径模板</summary>

```
core         : crates/core/src/models/im/<feature>.rs
infra        : crates/infrastructure/src/db/repo/im/<feature>.rs
services     : crates/services/src/im/<feature>.rs
api admin    : crates/api/admin/src/routes/im/<feature>.rs
api public   : crates/api/public/src/routes/im/<feature>.rs
注册点       : core/models/mod.rs · db/repo/mod.rs · services/lib.rs · routes/mod.rs
               · services/im/mod.rs 的 Services 字段 · AdminState/PublicState 域字段
```

</details>

### 2.14 `openapi`（P2）— 开放接口 / WorkBuddy / MCP

剩余 **0** / 共 **28** 条接口；**0** 个特性待迁移，4 个已完成。
切片：真实实现 1 · 脚手架（已删）0 · 未开始 0。


已完成（无需认领）：`message_push`、`workbuddy`、`workbuddy_product_import`、`workbuddy_product_intelligence`

<details><summary>本域四层目标路径模板</summary>

```
core         : crates/core/src/models/openapi/<feature>.rs
infra        : crates/infrastructure/src/db/repo/openapi/<feature>.rs
services     : crates/services/src/openapi/<feature>.rs
api admin    : crates/api/admin/src/routes/openapi/<feature>.rs
api public   : crates/api/public/src/routes/openapi/<feature>.rs
注册点       : core/models/mod.rs · db/repo/mod.rs · services/lib.rs · routes/mod.rs
               · services/openapi/mod.rs 的 Services 字段 · AdminState/PublicState 域字段
```

</details>

---

## 3. 已迁移的基线模块（19 个保留 feature，无需人工认领）

这些 feature 在切片清单生成之前就已有手写实现，codemod 已把它们迁入 v2 目录。
它们没有切片编号，`domain-map.json` 也不把它们当切片分配；名称已在对应 ctx 中保留，任何切片都不得占用同名 feature。

| ctx::feature | 业务内容 | 涉及接口（基线，已实现） |
|--------------|----------|--------------------------|
| `catalog::category_all` | 全量分类树（`getAllCategory` / `/api/category/all`） | 0 |
| `catalog::product` | 商品基线查询（`/api/product/product/detail|list` 等） | 0 |
| `content::article` | 文章基线（后台 CRUD + C 端列表，含 `ArticleCategoryClientController`） | 2 |
| `content::decorate` | 装修基线（`DecorateController` 18 个端点） | 0 |
| `identity::auth` | 登录/注册过滤器（Spring Security `setFilterProcessesUrl`） | 3 |
| `logistics::shipping_list` | 配送/运费列表基线 | 0 |
| `marketing::coupon` | 优惠券基线（后台 + C 端领券） | 6 |
| `marketing::salesman` | 分销基线 | 0 |
| `marketing::seckill` | 秒杀基线 | 0 |
| `marketing::time_discount` | 限时折扣基线 | 0 |
| `member::member_lists` | 会员列表/积分明细基线 | 1 |
| `member::user` | 用户基线（`/api/user/user/detail` 等） | 0 |
| `merchant::shop` | 店铺基线查询 | 0 |
| `payment::balance_pay` | 余额支付 | 0 |
| `payment::gateway` | 支付网关/回调（含分账回调 `ProfitSharingNotifyController`） | 0 |
| `payment::paylog` | 交易日志基线 | 0 |
| `payment::refund_apply` | 退款申请基线 | 0 |
| `trade::cart` | 购物车基线（`api/public/src/lib.rs` 早期路由） | 0 |
| `trade::order` | 订单基线（订单列表/详情/发货等早期实现） | 0 |

> 合计 81 条基线接口（清单见附录 B）。这些接口已计入「已实现 635」，
> 若在对应 feature 中重构实现，必须保持路径/响应不变并同步更新 `implemented_routes()`。

---

## 附录 A. 未实现接口清单（929 条，按 `ctx::feature`）

> 来自 `api-contract.json` 减去 `check_api_coverage.py` 已实现集合；
> 每行：`METHOD path` · 摘要 · `Controller#handler` · 权限码（有则标注）。

### A.1 `identity`

#### identity::admin_account — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\account\AdminAccountController.java`
- 目标四层：`crates/core/src/models/identity/admin_account.rs` · `crates/infrastructure/src/db/repo/identity/admin_account.rs` · `crates/services/src/identity/admin_account.rs` · `crates/api/admin/src/routes/identity/admin_account.rs` · `crates/api/public/src/routes/identity/admin_account.rs`
- [ ] `POST /adminapi/admin/adminAccount/bindMainAccount` — 根据主账号ID和店铺ID/供应商ID绑定账号 · `AdminAccountController#bindMainAccount` · 权限 `adminAccountBindMainAccount` · auth=`jwt+perm`
- [ ] `POST /adminapi/admin/adminAccount/deleteAdminUser` — 删除管理员账号 · `AdminAccountController#deleteAdminUser` · 权限 `adminAccountUpdateManager` · auth=`jwt+perm`
- [ ] `GET /adminapi/admin/adminAccount/getMainAccount` — 根据店铺或供应商ID查询主账号信息 · `AdminAccountController#getMainAccount`
- [ ] `GET /adminapi/admin/adminAccount/pageAdminUser` — 账号管理列表 · `AdminAccountController#pageAdminUser`
- [ ] `GET /adminapi/admin/adminAccount/pageShopOrVendor` — 根据主账号ID和类型查询账号列表 · `AdminAccountController#pageShopOrVendor`
- [ ] `POST /adminapi/admin/adminAccount/updateMainAccount` — 修改主账号信息 · `AdminAccountController#updateMainAccount` · 权限 `adminAccountUpdateManager` · auth=`jwt+perm`
- [ ] `POST /adminapi/admin/adminAccount/updateMainAccountPwd` — 修改主账号密码 · `AdminAccountController#updateMainAccountPwd` · 权限 `adminAccountUpdateManager` · auth=`jwt+perm`

#### identity::admin_role — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\authority\AdminRoleController.java`
- 目标四层：`crates/core/src/models/identity/admin_role.rs` · `crates/infrastructure/src/db/repo/identity/admin_role.rs` · `crates/services/src/identity/admin_role.rs` · `crates/api/admin/src/routes/identity/admin_role.rs` · `crates/api/public/src/routes/identity/admin_role.rs`
- [ ] `POST /adminapi/authority/adminRole/batch` — 批量操作 · `AdminRoleController#batch` · 权限 `adminRoleUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/authority/adminRole/create` — 创建角色 · `AdminRoleController#create` · 权限 `adminRoleUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/authority/adminRole/del` — 删除角色 · `AdminRoleController#del` · 权限 `adminRoleDelManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/authority/adminRole/detail` — 角色详情 · `AdminRoleController#detail` · 权限 `adminRoleManage,ShopRoleManage,vendorRoleManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/authority/adminRole/list` — 角色列表 · `AdminRoleController#list` · 权限 `adminRoleManage,ShopRoleManage,vendorRoleManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/authority/adminRole/update` — 更新角色 · `AdminRoleController#update` · 权限 `adminRoleUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/authority/adminRole/updateField` — 更新字段 · `AdminRoleController#updateField` · 权限 `adminRoleUpdateManage` · auth=`jwt+perm`

### A.2 `system`

#### system::config_basic — 50 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\ConfigController.java`
- 目标四层：`crates/core/src/models/system/config_basic.rs` · `crates/infrastructure/src/db/repo/system/config_basic.rs` · `crates/services/src/system/config_basic.rs` · `crates/api/admin/src/routes/system/config_basic.rs` · `crates/api/public/src/routes/system/config_basic.rs`
- [ ] `GET /adminapi/setting/config/afterSalesSettings` — 获取售后配置 · `ConfigController#getAfterSalesSettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/aliPaySettings` — 获取系统设置-支付宝支付设置 · `ConfigController#getAliPaySettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/authSettings` — 获取会员认证配置 · `ConfigController#getAuthSettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/basicPaySettings` — 获取系统设置-支付基础设置 · `ConfigController#getBasicPaySettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/basicSettings` — 获取商城设置-基础信息 · `ConfigController#getBasicSettings` · 权限 `configManage,config` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/createPlatformCertificate` — 生成平台证书 · `ConfigController#createPlatformCertificate` · 权限 `uploadFileModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/generateWorkbuddyKey` — 生成 MCP 密钥 · `ConfigController#generateWorkbuddyKey` · 权限 `saveSettingsGlobalManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/globalSettings` — 获取系统设置-全局设置 · `ConfigController#getGlobalSettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/kefuSettings` — 获取商城设置-客服设置 · `ConfigController#getKefuSettings` · 权限 `configManage,config,kefuSetting` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/loginSettings` — 获取系统设置-登录设置 · `ConfigController#getLoginSettings` · 权限 `config` · auth=`anonymous`
- [ ] `GET /adminapi/setting/config/mailSettings` — 获取系统设置-邮箱配置 · `ConfigController#getMailSettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/miniProgramRiskSettings` — 获取小程序风控配置 · `ConfigController#getMiniProgramRiskSettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/notifySettings` — 获取消息设置-通知配置 · `ConfigController#getNotifySettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/offlinePaySettings` — 获取系统设置-线下支付设置 · `ConfigController#getOfflinePaySettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/orderSettings` — 获取商城设置-订单设置 · `ConfigController#getOrderSettings` · 权限 `configManage,config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/payPalSettings` — 获取系统设置-PayPal支付设置 · `ConfigController#getPayPalSettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/productSettings` — 获取商城设置-商品设置 · `ConfigController#getProductSettings` · 权限 `configManage,config,productManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/revealWorkbuddyKey` — 复制 MCP 密钥 · `ConfigController#revealWorkbuddyKey` · 权限 `isPlatformSuperAdmin` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveAfterSales` — 保存售后配置 · `ConfigController#saveAfterSales` · 权限 `settingSaveAfterSalesManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveAliPay` — 保存系统设置-支付宝支付设置 · `ConfigController#saveAliPaySettings` · 权限 `uploadFileModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveAuth` — 保存会员认证配置 · `ConfigController#saveAuth` · 权限 `saveAuthSettingsManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveBasic` — 保存商城设置-基础信息 · `ConfigController#saveBasic` · 权限 `saveBasicManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveBasicPay` — 保存系统设置-支付基础设置 · `ConfigController#saveBasicPaySettings` · 权限 `uploadFileModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveGlobal` — 保存系统设置-全局设置 · `ConfigController#saveGlobalSettings` · 权限 `saveSettingsGlobalManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveKefu` — 保存商城设置-客服设置 · `ConfigController#saveKefuSettings` · 权限 `saveSettingKefuManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveLogin` — 保存系统设置-登录设置 · `ConfigController#saveLoginSettings` · 权限 `saveSettingsSaveLoginManage` · auth=`anonymous`
- [ ] `POST /adminapi/setting/config/saveMail` — 保存系统设置-邮箱配置 · `ConfigController#saveMail` · 权限 `mailTemplatesUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveMiniProgramRisk` — 保存小程序风控配置 · `ConfigController#saveMiniProgramRiskSettings` · 权限 `uploadFileModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveNotify` — 保存消息设置-通知配置 · `ConfigController#saveNotify` · 权限 `saveSettingNotifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveOfflinePay` — 保存系统设置-线下支付设置 · `ConfigController#saveOfflinePaySettings` · 权限 `uploadFileModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveOrder` — 保存商城设置-订单设置 · `ConfigController#saveOrderSettings` · 权限 `saveSettingsOrderManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/savePayPal` — 保存系统设置-PayPal支付设置 · `ConfigController#savePayPalSettings` · 权限 `uploadFileModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveProduct` — 保存商城设置-商品设置 · `ConfigController#saveProduct` · 权限 `saveSettingProductManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveShipping` — 保存配送设置-配送设置 · `ConfigController#saveShippingSettings` · 权限 `settingSaveShippingManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveShopping` — 保存商城设置-交易设置 · `ConfigController#saveShoppingSettings` · 权限 `saveSettingShoppingManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveThemeStyle` — 保存主题风格配置 · `ConfigController#saveThemeStyle` · 权限 `themeStyleUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveWechatLive` — 保存小程序直播/视频号独立小程序配置 · `ConfigController#saveWechatLiveSettings` · 权限 `uploadFileModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveWechatPay` — 保存系统设置-微信支付设置 · `ConfigController#saveWechatPaySettings` · 权限 `uploadFileModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveYaBandPay` — 保存系统设置-YaBandPay支付设置 · `ConfigController#saveYaBandPaySettings` · 权限 `uploadFileModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveYunPay` — 保存系统设置-云支付设置 · `ConfigController#saveYunPaySettings` · 权限 `uploadFileModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/sendTestEmail` — 发送测试邮件 · `ConfigController#sendTestEmail` · 权限 `sendTestEmailModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/shippingSettings` — 获取配送设置-配送设置 · `ConfigController#getShippingSettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/shoppingSettings` — 获取商城设置-交易设置 · `ConfigController#getShoppingSettings` · 权限 `configManage,config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/themeStyleSettings` — 获取主题风格配置 · `ConfigController#getThemeStyleSettings` · 权限 `config,decorate` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/uploadFile` — 上传密钥文件 · `ConfigController#uploadFile` · 权限 `uploadFileModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/wechatLiveSettings` — 获取小程序直播/视频号独立小程序配置 · `ConfigController#getWechatLiveSettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/wechatPaySettings` — 获取系统设置-微信支付设置 · `ConfigController#getWechatPaySettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/workbuddySettings` — 获取 MCP 密钥设置 · `ConfigController#getWorkBuddySettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/yaBandPaySettings` — 获取系统设置-YaBandPay支付设置 · `ConfigController#getYaBandPaySettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/yunPaySettings` — 获取系统设置-云支付设置 · `ConfigController#getYunPaySettings` · 权限 `config` · auth=`jwt+perm`

#### system::config_style — 50 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\ConfigController.java`
- 目标四层：`crates/core/src/models/system/config_style.rs` · `crates/infrastructure/src/db/repo/system/config_style.rs` · `crates/services/src/system/config_style.rs` · `crates/api/admin/src/routes/system/config_style.rs` · `crates/api/public/src/routes/system/config_style.rs`
- [ ] `GET /adminapi/setting/config/amapAllSettings` — 获取web Amap 配置 · `ConfigController#amapAllSettings`
- [ ] `GET /adminapi/setting/config/basicConfig` — 获取基础配置 · `ConfigController#getBasicConfig` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/buyerShowSettings` — 获取买家秀配置 · `ConfigController#getBuyerShowSettings` · 权限 `buyerShowManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/categoryDecorateSettings` — 获取分类页装修配置 · `ConfigController#getCategoryDecorateSettings` · 权限 `config,decorate` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/commissionWithdrawalSettings` — 获取分销佣金提现配置 · `ConfigController#getCommissionWithdrawalSettings`
- [ ] `GET /adminapi/setting/config/getAdmin` — 根据配置编码获取配置 · `ConfigController#getAdmin`
- [ ] `GET /adminapi/setting/config/getAdminBase` — admin 初始化配置 · `ConfigController#getAdminBase`
- [ ] `GET /adminapi/setting/config/getBase` — 根据配置编码获取配置 · `ConfigController#getBaseConfig` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/getDingConfig` — 获取钉钉配置 · `ConfigController#getDingConfig`
- [ ] `GET /adminapi/setting/config/getIcon` — 获取图标icon · `ConfigController#getIcon` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/getLoginProtocol` — 获取登录协议 · `ConfigController#getLoginProtocol` · auth=`anonymous`
- [ ] `GET /adminapi/setting/config/getLoginProtocolContent` — 获取登录协议内容  termsOfService:服务协议；privacyPolicy:隐私政策；afterSalesService:售后服务 · `ConfigController#getLoginProtocolContentVO` · auth=`anonymous`
- [ ] `GET /adminapi/setting/config/getPersonalizedSetting` — 获取个性化设置 · `ConfigController#getPersonalizedSetting`
- [ ] `GET /adminapi/setting/config/getWXWorkConfig` — 获取企业微信配置 · `ConfigController#getWXWorkConfig`
- [ ] `GET /adminapi/setting/config/mapSettings` — 获取地图服务商配置 · `ConfigController#mapSettings`
- [ ] `GET /adminapi/setting/config/memberCenterDecorateSettings` — 获取个人中心页装修配置 · `ConfigController#getMemberCenterDecorateSettings` · 权限 `mobileUserManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/memberWithdrawalSettings` — 获取会员余额提现配置 · `ConfigController#getMemberWithdrawalSettings`
- [ ] `GET /adminapi/setting/config/merchantSettings` — 获取商户配置 · `ConfigController#getMerchantSettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/mobileSearchStyleSettings` — 获取移动端搜索风格配置 · `ConfigController#getMobileSearchStyleSettings` · 权限 `config,decorate` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/productDetailStyleSettings` — 获取商品详情页风格配置 · `ConfigController#getProductDetailStyleSettings` · 权限 `config,decorate` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/profitSharingSettings` — 获取结算与分账配置 · `ConfigController#getProfitSharingSettings`
- [ ] `POST /adminapi/setting/config/saveAllAmapSettings` — 保存web Amap 配置 · `ConfigController#saveAllAmapSettings` · 权限 `storeSettingsUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveBuyerShow` — 保存买家秀配置 · `ConfigController#saveBuyerShow` · 权限 `buyerShowSettingsUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveCategoryDecorate` — 保存分类页装修配置 · `ConfigController#saveCategoryDecorate` · 权限 `themeCategoryDecorateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveCommissionWithdrawal` — 保存分销佣金提现配置 · `ConfigController#saveCommissionWithdrawal`
- [ ] `POST /adminapi/setting/config/saveDingConfig` — 保存钉钉配置 · `ConfigController#saveDingConfig`
- [ ] `POST /adminapi/setting/config/saveLoginProtocol` — 保存登录协议 · `ConfigController#saveLoginProtocol` · 权限 `protocolUpdateManagement` · auth=`anonymous`
- [ ] `POST /adminapi/setting/config/saveMapSettings` — 保存地图服务商配置 · `ConfigController#saveMapSettings` · 权限 `storeSettingsUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveMemberCenterDecorate` — 保存个人中心页装修配置 · `ConfigController#saveMemberCenterDecorate` · 权限 `mobileUserManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveMemberWithdrawal` — 保存会员余额提现配置 · `ConfigController#saveMemberWithdrawal`
- [ ] `POST /adminapi/setting/config/saveMerchant` — 保存商户配置 · `ConfigController#saveMerchant` · 权限 `adminMerchantSettingsUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveMobileSearchStyle` — 保存移动端搜索风格配置 · `ConfigController#saveMobileSearchStyle` · 权限 `mobileSearchStyleSaveManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/savePersonalizedSetting` — 保存个性化设置 · `ConfigController#savePersonalizedSetting`
- [ ] `POST /adminapi/setting/config/saveProductDetailStyle` — 保存商品详情页风格配置 · `ConfigController#saveProductDetailStyle` · 权限 `pcProductStyleSaveManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveProfitSharing` — 保存结算与分账配置 · `ConfigController#saveProfitSharing` · 权限 `profitsharingSettingUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveSearch` — 保存商品搜索配置 · `ConfigController#saveSearch`
- [ ] `POST /adminapi/setting/config/saveSearchStyle` — 保存搜索页风格配置 · `ConfigController#saveSearchStyle` · 权限 `pcSearchStyleSaveManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveShop` — 保存店铺配置 · `ConfigController#saveShop` · 权限 `shopSettingsUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveStore` — 保存门店配置 · `ConfigController#saveStore` · 权限 `storeSettingsUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveStoreSelfOperated` — 保存平台门店配置 · `ConfigController#saveStore` · 权限 `storeSettingsUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveVendor` — 保存供应商配置 · `ConfigController#saveVendor` · 权限 `vendorSettingUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveWXWorkConfig` — 保存企业微信配置 · `ConfigController#saveWXWorkConfig`
- [ ] `POST /adminapi/setting/config/saveWithdrawal` — 保存提现配置 · `ConfigController#saveWithdrawal`
- [ ] `GET /adminapi/setting/config/searchSettings` — 商品搜索配置 · `ConfigController#getSearchSettings`
- [ ] `GET /adminapi/setting/config/searchStyleSettings` — 获取搜索页风格配置 · `ConfigController#getSearchStyleSettings` · 权限 `config,decorate` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/shopSettings` — 获取店铺配置 · `ConfigController#getShopSettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/storeSelfOperatedSettings` — 获取平台门店配置 · `ConfigController#getStoreSelfOperatedSettings`
- [ ] `GET /adminapi/setting/config/storeSettings` — 获取门店配置 · `ConfigController#getStoreSettings`
- [ ] `GET /adminapi/setting/config/vendorSettings` — 获取供应商配置 · `ConfigController#getVendorSettings` · 权限 `vendorSettingManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/withdrawalSettings` — 获取提现配置 · `ConfigController#getWithdrawalSettings`

#### system::admin_msg — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\msg\AdminMsgController.java`
- 目标四层：`crates/core/src/models/system/admin_msg.rs` · `crates/infrastructure/src/db/repo/system/admin_msg.rs` · `crates/services/src/system/admin_msg.rs` · `crates/api/admin/src/routes/system/admin_msg.rs` · `crates/api/public/src/routes/system/admin_msg.rs`
- [ ] `GET /adminapi/msg/adminMsg/broadcastList` — 语音播报内容列表（含商品摘要，供播报抽屉一次性渲染） · `AdminMsgController#broadcastList`
- [ ] `GET /adminapi/msg/adminMsg/homeSummary` — 首页消息摘要（未读总数 + 最新一条） · `AdminMsgController#homeSummary`
- [ ] `GET /adminapi/msg/adminMsg/list` — 获取列表 · `AdminMsgController#list`
- [ ] `GET /adminapi/msg/adminMsg/msgCount` — `AdminMsgController#getMsgCount`
- [ ] `GET /adminapi/msg/adminMsg/msgTypeArr` — 获取消息类别 · `AdminMsgController#msgTypeArr`
- [ ] `POST /adminapi/msg/adminMsg/setAllReaded` — 设置全部已读 · `AdminMsgController#setAllReaded`
- [ ] `POST /adminapi/msg/adminMsg/setReaded` — 设置为单个已读 · `AdminMsgController#setReaded`

#### system::licensed — 3 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\LicensedController.java`
- 目标四层：`crates/core/src/models/system/licensed.rs` · `crates/infrastructure/src/db/repo/system/licensed.rs` · `crates/services/src/system/licensed.rs` · `crates/api/admin/src/routes/system/licensed.rs` · `crates/api/public/src/routes/system/licensed.rs`
- [ ] `GET /adminapi/setting/licensed/index` — 授权信息 · `LicensedController#getLicensedIndex`
- [ ] `POST /adminapi/setting/licensed/saveLicensed` — 保存授权 · `LicensedController#saveLicensed` · 权限 `licensed` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/licensed/update` — 更新授权 · `LicensedController#update` · 权限 `licensedModifyManage` · auth=`jwt+perm`

#### system::util — 3 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\common\UtilController.java`
- 目标四层：`crates/core/src/models/system/util.rs` · `crates/infrastructure/src/db/repo/system/util.rs` · `crates/services/src/system/util.rs` · `crates/api/admin/src/routes/system/util.rs` · `crates/api/public/src/routes/system/util.rs`
- [ ] `GET /api/common/util/miniCode` — 获取小程序二维码 · `UtilController#miniCode` · auth=`optional`
- [ ] `GET /api/common/util/miniScene` — 解析小程序码 scene 短码 · `UtilController#miniScene` · auth=`optional`
- [ ] `GET /api/common/util/qrCode` — 获取二维码 · `UtilController#qrCode` · auth=`optional`

#### system::config_cashier — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\ConfigController.java`
- 目标四层：`crates/core/src/models/system/config_cashier.rs` · `crates/infrastructure/src/db/repo/system/config_cashier.rs` · `crates/services/src/system/config_cashier.rs` · `crates/api/admin/src/routes/system/config_cashier.rs` · `crates/api/public/src/routes/system/config_cashier.rs`
- [ ] `GET /adminapi/setting/config/getCashierOrderSettings` — 获取收银台订单设置 · `ConfigController#getCashierOrderSettings`
- [ ] `POST /adminapi/setting/config/saveCashierOrderSettings` — 保存收银台订单设置 · `ConfigController#saveCashierOrderSettings`

#### system::csrf — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\common\CSRFController.java`；`tigshop-api\src\main\java\com\tigshop\api\controller\common\CSRFController.java`
- 目标四层：`crates/core/src/models/system/csrf.rs` · `crates/infrastructure/src/db/repo/system/csrf.rs` · `crates/services/src/system/csrf.rs` · `crates/api/admin/src/routes/system/csrf.rs` · `crates/api/public/src/routes/system/csrf.rs`
- [ ] `GET /adminapi/common/csrf/create` — `CSRFController#create`
- [ ] `GET /api/common/csrf/create` — `CSRFController#create` · auth=`optional`

#### system::recommend — 2 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\common\RecommendController.java`
- 目标四层：`crates/core/src/models/system/recommend.rs` · `crates/infrastructure/src/db/repo/system/recommend.rs` · `crates/services/src/system/recommend.rs` · `crates/api/admin/src/routes/system/recommend.rs` · `crates/api/public/src/routes/system/recommend.rs`
- [ ] `GET /api/common/recommend/getProductIds` — 获取商品ids · `RecommendController#getProductIds` · auth=`optional`
- [ ] `GET /api/common/recommend/guessLike` — 猜你喜欢 · `RecommendController#getProductList` · auth=`optional`

#### system::app_version_client — 1 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\app\AppVersionController.java`
- 目标四层：`crates/core/src/models/system/app_version_client.rs` · `crates/infrastructure/src/db/repo/system/app_version_client.rs` · `crates/services/src/system/app_version_client.rs` · `crates/api/admin/src/routes/system/app_version_client.rs` · `crates/api/public/src/routes/system/app_version_client.rs`
- [ ] `POST /api/appVersion/getAppUpdate` — 详情 · `AppVersionController#getAppUpdate` · auth=`optional`

#### system::log — 1 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\common\LogController.java`
- 目标四层：`crates/core/src/models/system/log.rs` · `crates/infrastructure/src/db/repo/system/log.rs` · `crates/services/src/system/log.rs` · `crates/api/admin/src/routes/system/log.rs` · `crates/api/public/src/routes/system/log.rs`
- [ ] `GET /api/common/log` — 创建日志记录 · `LogController#create` · auth=`optional`

#### system::tips_manage — 1 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\common\TipsManageController.java`
- 目标四层：`crates/core/src/models/system/tips_manage.rs` · `crates/infrastructure/src/db/repo/system/tips_manage.rs` · `crates/services/src/system/tips_manage.rs` · `crates/api/admin/src/routes/system/tips_manage.rs` · `crates/api/public/src/routes/system/tips_manage.rs`
- [ ] `GET /adminapi/common/tipsManage/list` — 提示管理列表 · `TipsManageController#list`

### A.3 `catalog`

#### catalog::product_manage — 32 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\product\ProductController.java`；`tigshop-api\src\main\java\com\tigshop\api\controller\product\ProductController.java`
- 目标四层：`crates/core/src/models/catalog/product_manage.rs` · `crates/infrastructure/src/db/repo/catalog/product_manage.rs` · `crates/services/src/catalog/product_manage.rs` · `crates/api/admin/src/routes/catalog/product_manage.rs` · `crates/api/public/src/routes/catalog/product_manage.rs`
- [ ] `POST /adminapi/product/product/audit` — 产品审核 · `ProductController#audit` · 权限 `productModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/product/auditAgain` — 产品再次审核 · `ProductController#auditAgain` · 权限 `productModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/product/batch` — 批量操作 · `ProductController#batchOperation` · 权限 `productModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/product/config` — 商品配置 · `ProductController#config` · 权限 `productManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/product/copy` — 产品复制 · `ProductController#copy` · 权限 `productModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/product/descImages` — 商品描述图片列表（供配置描述图本地化 dataType=7） · `ProductController#descImages` · 权限 `productManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/product/getParticiple` — 更新关键词 · `ProductController#getParticiple` · 权限 `productManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/product/getPromoteData` — 获取推广数据 · `ProductController#getPromoteData` · 权限 `productManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/product/getWaitingCheckedCount` — 待审核商品数量 · `ProductController#getWaitingCheckedCount` · 权限 `productManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/product/initAllToEs` — 初始化所有商品到ES · `ProductController#initAllToEs` · 权限 `productManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/product/post` — 商品推广（包含H5、PC、小程序） · `ProductController#post` · 权限 `productManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/product/recycle` — 产品回收 · `ProductController#recycle` · 权限 `productModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/product/saveShareInfo` — 保存商品分享信息 · `ProductController#saveShareInfo` · 权限 `productManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/product/selectProductList` — sku商品列表 · `ProductController#selectProductList` · 权限 `productManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/product/shareInfo` — 获取商品分享信息 · `ProductController#shareInfo` · 权限 `productManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/product/shippingTplList` — 运费模板 · `ProductController#shippingTplList` · 权限 `productManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/product/skuList` — 分页获取商品SKU列表（对应商品详情 productList，默认每页50条，支持关键字/库存过滤与排序） · `ProductController#skuList` · 权限 `productManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/product/updateField` — 更新字段 · `ProductController#updateField` · 权限 `productModifyManage` · auth=`jwt+perm`
- [ ] `GET /api/product/product/getBatchProductAvailability` — 批量获取商品可用活动与数据 · `ProductController#getBatchProductAvailability` · auth=`optional`
- [ ] `GET /api/product/product/getComment` — 获取商品评论 · `ProductController#getComment` · auth=`optional`
- [ ] `GET /api/product/product/getCommentList` — 获取商品评论列表 · `ProductController#getCommentList` · auth=`optional`
- [ ] `GET /api/product/product/getCoupon` — 获取商品优惠券 · `ProductController#getCoupon` · auth=`optional`
- [ ] `GET /api/product/product/getFeedbackList` — 获取商品反馈列表 · `ProductController#getFeedbackList` · auth=`optional`
- [ ] `GET /api/product/product/getHistoryProduct` — 获取当前用户历史购买过的商品信息 · `ProductController#getHistoryProduct` · auth=`optional`
- [ ] `GET /api/product/product/getPresaleDepositAmount` — 获取预售的定金价格 · `ProductController#getPresaleDepositAmount` · auth=`optional`
- [ ] `POST /api/product/product/getProductAmount` — 获取商品数量 · `ProductController#getProductAmount` · auth=`optional`
- [ ] `GET /api/product/product/getProductAvailability` — 商品可用活动与数据 · `ProductController#getProductAvailability` · auth=`optional`
- [ ] `GET /api/product/product/getRelated` — 获取相关商品 · `ProductController#getRelated` · auth=`optional`
- [ ] `GET /api/product/product/isCollect` — 商品是否收藏 · `ProductController#isCollectMethod` · auth=`optional`
- [ ] `POST /api/product/product/priceInquiry` — 商品询价 · `ProductController#priceInquiry` · auth=`optional`
- [ ] `POST /api/product/product/promotion` — 商品活动 · `ProductController#getProductsPromotion` · auth=`optional`
- [ ] `GET /api/product/product/share` — 商品分享落地页 · `ProductController#sharePage` · auth=`optional`

#### catalog::comment — 8 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\product\CommentController.java`
- 目标四层：`crates/core/src/models/catalog/comment.rs` · `crates/infrastructure/src/db/repo/catalog/comment.rs` · `crates/services/src/catalog/comment.rs` · `crates/api/admin/src/routes/catalog/comment.rs` · `crates/api/public/src/routes/catalog/comment.rs`
- [ ] `POST /adminapi/product/comment/batch` — 批量操作 · `CommentController#batch` · 权限 `commentModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/comment/create` — 创建 · `CommentController#create` · 权限 `commentModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/comment/del` — 删除 · `CommentController#del` · 权限 `commentModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/comment/detail` — 获取详情 · `CommentController#detail` · 权限 `commentManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/comment/list` — 获取列表 · `CommentController#list` · 权限 `commentManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/comment/replyComment` — 回复评论 · `CommentController#replyComment` · 权限 `commentModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/comment/update` — 更新 · `CommentController#update` · 权限 `commentModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/comment/updateField` — 更新字段 · `CommentController#updateField` · 权限 `commentModifyManage` · auth=`jwt+perm`

#### catalog::e_card_group — 8 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\product\ECardGroupController.java`
- 目标四层：`crates/core/src/models/catalog/e_card_group.rs` · `crates/infrastructure/src/db/repo/catalog/e_card_group.rs` · `crates/services/src/catalog/e_card_group.rs` · `crates/api/admin/src/routes/catalog/e_card_group.rs` · `crates/api/public/src/routes/catalog/e_card_group.rs`
- [ ] `POST /adminapi/product/eCardGroup/batch` — 批量操作 · `ECardGroupController#batch` · 权限 `eCardManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/eCardGroup/create` — 创建 · `ECardGroupController#create` · 权限 `eCardManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/eCardGroup/del` — 删除 · `ECardGroupController#del` · 权限 `eCardManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/eCardGroup/detail` — 获取详情 · `ECardGroupController#detail` · 权限 `eCardManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/eCardGroup/import` — 批量导入 · `ECardGroupController#batchImport` · 权限 `eCardManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/eCardGroup/list` — 获取列表 · `ECardGroupController#list` · 权限 `eCardManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/eCardGroup/update` — 更新 · `ECardGroupController#update` · 权限 `eCardManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/eCardGroup/updateField` — 更新字段 · `ECardGroupController#updateField` · 权限 `eCardManage` · auth=`jwt+perm`

#### catalog::product_group — 8 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\product\ProductGroupController.java`
- 目标四层：`crates/core/src/models/catalog/product_group.rs` · `crates/infrastructure/src/db/repo/catalog/product_group.rs` · `crates/services/src/catalog/product_group.rs` · `crates/api/admin/src/routes/catalog/product_group.rs` · `crates/api/public/src/routes/catalog/product_group.rs`
- [ ] `POST /adminapi/product/productGroup/batch` — 批量操作 · `ProductGroupController#batch` · 权限 `productGroupModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/productGroup/clientList` — 获取列表 · `ProductGroupController#client` · 权限 `productGroupManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/productGroup/create` — 创建 · `ProductGroupController#create` · 权限 `productGroupModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/productGroup/del` — 删除 · `ProductGroupController#del` · 权限 `productGroupModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/productGroup/detail` — 获取详情 · `ProductGroupController#detail` · 权限 `productGroupManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/productGroup/list` — 获取列表 · `ProductGroupController#list` · 权限 `productGroupManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/productGroup/update` — 更新 · `ProductGroupController#update` · 权限 `productGroupModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/productGroup/updateField` — 更新字段 · `ProductGroupController#updateField` · 权限 `productGroupModifyManage` · auth=`jwt+perm`

#### catalog::e_card — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\product\ECardController.java`
- 目标四层：`crates/core/src/models/catalog/e_card.rs` · `crates/infrastructure/src/db/repo/catalog/e_card.rs` · `crates/services/src/catalog/e_card.rs` · `crates/api/admin/src/routes/catalog/e_card.rs` · `crates/api/public/src/routes/catalog/e_card.rs`
- [ ] `POST /adminapi/product/eCard/batch` — 批量操作 · `ECardController#batch` · 权限 `eCardManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/eCard/create` — 创建 · `ECardController#create` · 权限 `eCardManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/eCard/del` — 删除 · `ECardController#del` · 权限 `eCardManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/eCard/detail` — 获取详情 · `ECardController#detail` · 权限 `eCardManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/eCard/list` — 获取列表 · `ECardController#list` · 权限 `eCardManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/eCard/update` — 更新 · `ECardController#update` · 权限 `eCardManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/eCard/updateField` — 更新字段 · `ECardController#updateField` · 权限 `eCardManage` · auth=`jwt+perm`

#### catalog::services — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\product\ServicesController.java`
- 目标四层：`crates/core/src/models/catalog/services.rs` · `crates/infrastructure/src/db/repo/catalog/services.rs` · `crates/services/src/catalog/services.rs` · `crates/api/admin/src/routes/catalog/services.rs` · `crates/api/public/src/routes/catalog/services.rs`
- [ ] `POST /adminapi/product/productServices/batch` — 批量操作商品服务 · `ServicesController#batch` · 权限 `productServicesModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/productServices/create` — 创建商品服务 · `ServicesController#create` · 权限 `productServicesModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/productServices/del` — 删除商品服务 · `ServicesController#del` · 权限 `productServicesModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/productServices/detail` — 商品服务详情 · `ServicesController#detail` · 权限 `productServicesManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/productServices/list` — 商品服务列表 · `ServicesController#list` · 权限 `productServicesManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/productServices/update` — 更新商品服务 · `ServicesController#update` · 权限 `productServicesModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/productServices/updateField` — 商品服务字段修改 · `ServicesController#updateField` · 权限 `productServicesModifyManage` · auth=`jwt+perm`

#### catalog::comment_client — 5 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\user\CommentController.java`
- 目标四层：`crates/core/src/models/catalog/comment_client.rs` · `crates/infrastructure/src/db/repo/catalog/comment_client.rs` · `crates/services/src/catalog/comment_client.rs` · `crates/api/admin/src/routes/catalog/comment_client.rs` · `crates/api/public/src/routes/catalog/comment_client.rs`
- [ ] `GET /api/user/comment/detail` — 评论详情 · `CommentController#detail` · auth=`optional`
- [ ] `POST /api/user/comment/evaluate` — 商品评价 / 晒单 · `CommentController#evaluate` · auth=`optional`
- [ ] `GET /api/user/comment/list` — 已评价列表 · `CommentController#list` · auth=`optional`
- [ ] `GET /api/user/comment/showedList` — 晒单列表 · `CommentController#showedList` · auth=`optional`
- [ ] `GET /api/user/comment/subNum` — 评论晒单数量 · `CommentController#subNum` · auth=`optional`

#### catalog::price_inquiry — 5 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\product\PriceInquiryController.java`
- 目标四层：`crates/core/src/models/catalog/price_inquiry.rs` · `crates/infrastructure/src/db/repo/catalog/price_inquiry.rs` · `crates/services/src/catalog/price_inquiry.rs` · `crates/api/admin/src/routes/catalog/price_inquiry.rs` · `crates/api/public/src/routes/catalog/price_inquiry.rs`
- [ ] `POST /adminapi/product/priceInquiry/batch` — 鎵归噺鎿嶄綔 · `PriceInquiryController#batch` · 权限 `priceInquiryModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/product/priceInquiry/del` — 鍒犻櫎 · `PriceInquiryController#del` · 权限 `priceInquiryModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/priceInquiry/detail` — 鑾峰彇璇︽儏 · `PriceInquiryController#detail`
- [ ] `GET /adminapi/product/priceInquiry/list` — 鑾峰彇鍒楄〃 · `PriceInquiryController#list`
- [ ] `POST /adminapi/product/priceInquiry/reply` — 鍥炲 · `PriceInquiryController#reply` · 权限 `priceInquiryModifyManage` · auth=`jwt+perm`

#### catalog::product_search — 4 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\search\SearchController.java`；`tigshop-api\src\main\java\com\tigshop\api\controller\search\SearchController.java`；`tigshop-api\src\main\java\com\tigshop\api\controller\search\SearchGuessController.java`
- 目标四层：`crates/core/src/models/catalog/product_search.rs` · `crates/infrastructure/src/db/repo/catalog/product_search.rs` · `crates/services/src/catalog/product_search.rs` · `crates/api/admin/src/routes/catalog/product_search.rs` · `crates/api/public/src/routes/catalog/product_search.rs`
- [ ] `GET /api/search/search/getAttributeSuggestions` — 获取属性搜索建议 · `SearchController#getAttributeSuggestions` · auth=`optional`
- [ ] `POST /api/search/search/getFilter` — 商品筛选 · `SearchController#getFilter` · auth=`optional`
- [ ] `POST /api/search/search/getProduct` — 商品列表 · `SearchController#list` · auth=`optional`
- [ ] `GET /api/search/searchGuess/index` — 搜索关键词联想 · `SearchGuessController#getSearchGuessList` · auth=`optional`

#### catalog::exchange — 3 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\product\ExchangeController.java`
- 目标四层：`crates/core/src/models/catalog/exchange.rs` · `crates/infrastructure/src/db/repo/catalog/exchange.rs` · `crates/services/src/catalog/exchange.rs` · `crates/api/admin/src/routes/catalog/exchange.rs` · `crates/api/public/src/routes/catalog/exchange.rs`
- [ ] `POST /api/product/exchange/addToCart` — 添加积分商品购物车 · `ExchangeController#addExchangeProductToCart` · auth=`optional`
- [ ] `GET /api/product/exchange/detail` — 积分兑换详情 · `ExchangeController#detail` · auth=`optional`
- [ ] `GET /api/product/exchange/list` — 获取列表 · `ExchangeController#list` · auth=`optional`

#### catalog::product_style_client — 3 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\product\ProductStyleController.java`
- 目标四层：`crates/core/src/models/catalog/product_style_client.rs` · `crates/infrastructure/src/db/repo/catalog/product_style_client.rs` · `crates/services/src/catalog/product_style_client.rs` · `crates/api/admin/src/routes/catalog/product_style_client.rs` · `crates/api/public/src/routes/catalog/product_style_client.rs`
- [ ] `GET /api/product/productStyle/activeStyle` — 获取商品当前生效样式 · `ProductStyleController#getActiveStyle` · auth=`optional`
- [ ] `GET /api/product/productStyle/batchActiveStyle` — 批量获取商品生效样式 · `ProductStyleController#batchGetActiveStyles` · auth=`optional`
- [ ] `GET /api/product/productStyle/batchActiveStyleBySn` — 批量获取商品生效样式（通过商品编号SN） · `ProductStyleController#batchGetActiveStylesBySns` · auth=`optional`

#### catalog::product_batch — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\product\ProductBatchController.java`
- 目标四层：`crates/core/src/models/catalog/product_batch.rs` · `crates/infrastructure/src/db/repo/catalog/product_batch.rs` · `crates/services/src/catalog/product_batch.rs` · `crates/api/admin/src/routes/catalog/product_batch.rs` · `crates/api/public/src/routes/catalog/product_batch.rs`
- [ ] `GET /adminapi/product/productBatch/productBatchDeal` — 商品导出Excel · `ProductBatchController#productBatchDeal` · 权限 `productBatchAddManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/product/productBatch/productService/list` — 商品服务列表 · `ProductBatchController#list` · 权限 `productBatchAddManage` · auth=`jwt+perm`

#### catalog::search_admin — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\search\SearchController.java`
- 目标四层：`crates/core/src/models/catalog/search_admin.rs` · `crates/infrastructure/src/db/repo/catalog/search_admin.rs` · `crates/services/src/catalog/search_admin.rs` · `crates/api/admin/src/routes/catalog/search_admin.rs` · `crates/api/public/src/routes/catalog/search_admin.rs`
- [ ] `GET /adminapi/search/aggregate` — 综合搜索 · `SearchController#aggregate` · 权限 `productManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/search/product` — 商品搜索 · `SearchController#product` · 权限 `productManage` · auth=`jwt+perm`

#### catalog::product_inventory_log — 1 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\product\ProductInventoryLogController.java`
- 目标四层：`crates/core/src/models/catalog/product_inventory_log.rs` · `crates/infrastructure/src/db/repo/catalog/product_inventory_log.rs` · `crates/services/src/catalog/product_inventory_log.rs` · `crates/api/admin/src/routes/catalog/product_inventory_log.rs` · `crates/api/public/src/routes/catalog/product_inventory_log.rs`
- [ ] `GET /adminapi/product/productInventoryLog/list` — 获取列表 · `ProductInventoryLogController#list` · 权限 `productInventoryLogManage` · auth=`jwt+perm`

### A.4 `trade`

#### trade::order_admin — 30 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\order\OrderController.java`
- 目标四层：`crates/core/src/models/trade/order_admin.rs` · `crates/infrastructure/src/db/repo/trade/order_admin.rs` · `crates/services/src/trade/order_admin.rs` · `crates/api/admin/src/routes/trade/order_admin.rs` · `crates/api/public/src/routes/trade/order_admin.rs`
- [ ] `POST /adminapi/order/order/batch` — 批量操作 · `OrderController#batch` · 权限 `splitStoreOrderManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/order/order/batchDeliverImport` — 批量发货Excel导入 · `OrderController#batchDeliverImport` · 权限 `orderDeliverManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/order/order/cancelOrder` — 取消订单 · `OrderController#cancelOrder` · 权限 `cancelOrderManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/order/order/changeOrderStatus` — 修改订单状态 · `OrderController#changeOrderStatus`
- [ ] `POST /adminapi/order/order/confirmReceipt` — 确认收货 · `OrderController#confirmReceipt` · 权限 `orderConfirmReceiptManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/order/order/delOrder` — 删除订单 · `OrderController#delOrder` · 权限 `delOrderManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/order/order/downloadDeliverTemplate` — 下载批量发货模板 · `OrderController#downloadDeliverTemplate`
- [ ] `GET /adminapi/order/order/exportItemInfo` — 标签详情 · `OrderController#exportItemInfo`
- [ ] `POST /adminapi/order/order/getAddProductInfo` — 添加商品时获取商品信息 · `OrderController#getAddProductInfo`
- [ ] `POST /adminapi/order/order/getBatchDeliverImportErrorData` — 获取批量发货导入失败的数据（导出错误Excel） · `OrderController#getBatchDeliverImportErrorData` · 权限 `orderDeliverManage` · auth=`anonymous`
- [ ] `GET /adminapi/order/order/getExportItemList` — 订单导出标签列表 · `OrderController#getExportItemList`
- [ ] `GET /adminapi/order/order/getOrderPageConfig` — 获取订单列表配置 · `OrderController#getOrderPageConfig`
- [ ] `POST /adminapi/order/order/modifyConsignee` — 修改收货人信息 · `OrderController#modifyConsignee` · 权限 `orderModifyConsigneeManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/order/order/modifyMoney` — 修改订单金额 · `OrderController#modifyMoney` · 权限 `orderModifyMoneyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/order/order/modifyProduct` — 修改商品信息 · `OrderController#modifyProduct` · 权限 `modifyProductManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/order/order/modifyShipping` — 修改配送信息 · `OrderController#modifyShipping` · 权限 `orderModifyShippingManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/order/order/orderExport` — 订单导出 · `OrderController#exportItemInfo`
- [ ] `GET /adminapi/order/order/orderPrint` — 打印订单 · `OrderController#orderPrint`
- [ ] `GET /adminapi/order/order/orderPrintBill` — 打印电子面单 · `OrderController#orderPrintWaybill`
- [ ] `GET /adminapi/order/order/parentDetail` — 获取父订单详情 · `OrderController#parentOrderDetail`
- [ ] `GET /adminapi/order/order/printSeveral` — 批量打印 · `OrderController#printSeveral`
- [ ] `POST /adminapi/order/order/remindDeliver` — 催发货 · `OrderController#remindDeliver` · 权限 `remindDeliver` · auth=`jwt+perm`
- [ ] `POST /adminapi/order/order/saveExportItem` — 保存导出标签信息 · `OrderController#saveExportItem`
- [ ] `POST /adminapi/order/order/setAdminNote` — 设置管理员备注 · `OrderController#setAdminNote` · 权限 `setAdminNoteManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/order/order/setConfirm` — 设置订单确认 · `OrderController#setConfirm` · 权限 `setConfirmManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/order/order/setPaid` — 设置已付款 · `OrderController#setPaid` · 权限 `setPaidManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/order/order/severalDetail` — 批量详情 · `OrderController#severalDetail`
- [ ] `GET /adminapi/order/order/shippingInfo` — 物流信息 · `OrderController#shippingInfo`
- [ ] `POST /adminapi/order/order/splitStoreOrder` — 订单拆分 · `OrderController#splitStoreOrder` · 权限 `splitStoreOrderManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/order/order/statusCount` — 统计各订单状态的数量 · `OrderController#statusCount`

#### trade::proxy_shopping_cart — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\proxyshopping\ProxyShoppingCartController.java`
- 目标四层：`crates/core/src/models/trade/proxy_shopping_cart.rs` · `crates/infrastructure/src/db/repo/trade/proxy_shopping_cart.rs` · `crates/services/src/trade/proxy_shopping_cart.rs` · `crates/api/admin/src/routes/trade/proxy_shopping_cart.rs` · `crates/api/public/src/routes/trade/proxy_shopping_cart.rs`
- [ ] `POST /adminapi/proxyShopping/cart/addToCart` — 添加商品到购物车 · `ProxyShoppingCartController#addProductToCart` · 权限 `proxyShoppingManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/proxyShopping/cart/clearCart` — 清空购物车 · `ProxyShoppingCartController#clearCart` · 权限 `proxyShoppingManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/proxyShopping/cart/getCount` — 获取购物车商品数量 · `ProxyShoppingCartController#getCount` · 权限 `proxyShoppingManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/proxyShopping/cart/list` — 获取购物车列表 · `ProxyShoppingCartController#list` · 权限 `proxyShoppingManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/proxyShopping/cart/removeItem` — 删除购物车商品 · `ProxyShoppingCartController#removeItem` · 权限 `proxyShoppingManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/proxyShopping/cart/updateCheck` — 更新选中状态 · `ProxyShoppingCartController#updateCheck` · 权限 `proxyShoppingManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/proxyShopping/cart/updateItem` — 更新购物车商品 · `ProxyShoppingCartController#updateItem` · 权限 `proxyShoppingManage` · auth=`jwt+perm`

#### trade::check — 6 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\order\CheckController.java`
- 目标四层：`crates/core/src/models/trade/check.rs` · `crates/infrastructure/src/db/repo/trade/check.rs` · `crates/services/src/trade/check.rs` · `crates/api/admin/src/routes/trade/check.rs` · `crates/api/public/src/routes/trade/check.rs`
- [ ] `GET /api/order/check/getAvailablePaymentType` — 获取可用支付方式 · `CheckController#getAvailablePaymentType` · auth=`optional`
- [ ] `GET /api/order/check/getInvoice` — 获得上次订单发票信息 · `CheckController#getInvoice` · auth=`optional`
- [ ] `GET /api/order/check/getStoreShippingType` — 获得店铺配送方式 · `CheckController#getStoreShippingType` · auth=`optional`
- [ ] `POST /api/order/check/index` — 获取列表 · `CheckController#index` · auth=`optional`
- [ ] `POST /api/order/check/update` — 更新 · `CheckController#update` · auth=`optional`
- [ ] `POST /api/order/check/updateCoupon` — 更新优惠券 · `CheckController#updateCoupon` · auth=`optional`

#### trade::user_order — 6 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\user\UserOrderController.java`
- 目标四层：`crates/core/src/models/trade/user_order.rs` · `crates/infrastructure/src/db/repo/trade/user_order.rs` · `crates/services/src/trade/user_order.rs` · `crates/api/admin/src/routes/trade/user_order.rs` · `crates/api/public/src/routes/trade/user_order.rs`
- [ ] `POST /api/user/order/buyAgain` — 再次购买 · `UserOrderController#buyAgain` · auth=`optional`
- [ ] `POST /api/user/order/confirmReceipt` — 确认收货 · `UserOrderController#confirmReceipt` · auth=`optional`
- [ ] `POST /api/user/order/delOrder` — 删除订单 · `UserOrderController#delOrder` · auth=`optional`
- [ ] `GET /api/user/order/orderNum` — 订单数量 · `UserOrderController#orderNum` · auth=`optional`
- [ ] `GET /api/user/order/reasonConfig` — 获取买家售后与取消订单共用原因配置 · `UserOrderController#reasonConfig` · auth=`optional`
- [ ] `GET /api/user/order/shippingInfo` — 物流信息 · `UserOrderController#shippingInfo` · auth=`optional`

#### trade::order_invoice_client — 3 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\user\OrderInvoiceClientController.java`
- 目标四层：`crates/core/src/models/trade/order_invoice_client.rs` · `crates/infrastructure/src/db/repo/trade/order_invoice_client.rs` · `crates/services/src/trade/order_invoice_client.rs` · `crates/api/admin/src/routes/trade/order_invoice_client.rs` · `crates/api/public/src/routes/trade/order_invoice_client.rs`
- [ ] `POST /api/user/orderInvoice/create` — 发票申请 · `OrderInvoiceClientController#create` · auth=`optional`
- [ ] `GET /api/user/orderInvoice/detail` — 发票详情 · `OrderInvoiceClientController#detail` · auth=`optional`
- [ ] `POST /api/user/orderInvoice/update` — 更新发票申请数据 · `OrderInvoiceClientController#update` · auth=`optional`

#### trade::cart_summary — 2 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\cart\CartController.java`
- 目标四层：`crates/core/src/models/trade/cart_summary.rs` · `crates/infrastructure/src/db/repo/trade/cart_summary.rs` · `crates/services/src/trade/cart_summary.rs` · `crates/api/admin/src/routes/trade/cart_summary.rs` · `crates/api/public/src/routes/trade/cart_summary.rs`
- [ ] `GET /api/cart/cart/getCount` — 获取数量 · `CartController#getCount` · auth=`optional`
- [ ] `GET /api/cart/cart/getCouponDiscount` — 获取优惠券折扣 · `CartController#getCouponDiscount` · auth=`optional`

#### trade::order_log — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\order\OrderLogController.java`
- 目标四层：`crates/core/src/models/trade/order_log.rs` · `crates/infrastructure/src/db/repo/trade/order_log.rs` · `crates/services/src/trade/order_log.rs` · `crates/api/admin/src/routes/trade/order_log.rs` · `crates/api/public/src/routes/trade/order_log.rs`
- [ ] `POST /adminapi/order/orderLog/create` — 创建 · `OrderLogController#create` · 权限 `orderLogModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/order/orderLog/list` — 获取列表 · `OrderLogController#list`

#### trade::proxy_shopping_delivery — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\proxyshopping\ProxyShoppingDeliveryController.java`
- 目标四层：`crates/core/src/models/trade/proxy_shopping_delivery.rs` · `crates/infrastructure/src/db/repo/trade/proxy_shopping_delivery.rs` · `crates/services/src/trade/proxy_shopping_delivery.rs` · `crates/api/admin/src/routes/trade/proxy_shopping_delivery.rs` · `crates/api/public/src/routes/trade/proxy_shopping_delivery.rs`
- [ ] `GET /adminapi/proxyShopping/delivery/getDeliveryOptions` — 获取代客下单购物车商品配送方式 · `ProxyShoppingDeliveryController#getDeliveryOptions` · 权限 `proxyShoppingManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/proxyShopping/delivery/getStoreShippingType` — 获取店铺配送方式 · `ProxyShoppingDeliveryController#getStoreShippingType` · 权限 `proxyShoppingManage` · auth=`jwt+perm`

#### trade::shop_order_config — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\order\ShopOrderConfigController.java`
- 目标四层：`crates/core/src/models/trade/shop_order_config.rs` · `crates/infrastructure/src/db/repo/trade/shop_order_config.rs` · `crates/services/src/trade/shop_order_config.rs` · `crates/api/admin/src/routes/trade/shop_order_config.rs` · `crates/api/public/src/routes/trade/shop_order_config.rs`
- [ ] `GET /adminapi/order/config/detail` — 鑾峰彇閰嶇疆璇︽儏 · `ShopOrderConfigController#detail`
- [ ] `POST /adminapi/order/config/save` — 淇濆瓨閰嶇疆 · `ShopOrderConfigController#save` · 权限 `orderConfigModifyManage` · auth=`jwt+perm`

### A.5 `payment`

#### payment::merchant_application — 10 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\finance\MerchantApplicationController.java`
- 目标四层：`crates/core/src/models/payment/merchant_application.rs` · `crates/infrastructure/src/db/repo/payment/merchant_application.rs` · `crates/services/src/payment/merchant_application.rs` · `crates/api/admin/src/routes/payment/merchant_application.rs` · `crates/api/public/src/routes/payment/merchant_application.rs`
- [ ] `POST /adminapi/finance/merchantApplication/apply` — 提交进件申请（店铺/供应商端） · `MerchantApplicationController#apply`
- [ ] `POST /adminapi/finance/merchantApplication/approve` — 审核通过（提交微信进件） · `MerchantApplicationController#approve` · 权限 `merchantApplicationApprove` · auth=`jwt+perm`
- [ ] `GET /adminapi/finance/merchantApplication/current` — 当前登录主体最新进件申请（店铺/供应商端） · `MerchantApplicationController#current`
- [ ] `GET /adminapi/finance/merchantApplication/detail` — 进件申请详情 · `MerchantApplicationController#detail` · 权限 `merchantApplicationManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/finance/merchantApplication/list` — 进件申请分页列表（平台审核列表） · `MerchantApplicationController#list` · 权限 `merchantApplicationManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/finance/merchantApplication/media` — 进件证件原图预览 · `MerchantApplicationController#media` · 权限 `merchantApplicationManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/merchantApplication/refresh` — 刷新进件状态 · `MerchantApplicationController#refresh` · 权限 `merchantApplicationRefresh` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/merchantApplication/reject` — 审核拒绝 · `MerchantApplicationController#reject` · 权限 `merchantApplicationReject` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/merchantApplication/submit` — 平台直接提交进件（进件管理） · `MerchantApplicationController#submit` · 权限 `merchantApplicationSubmit` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/merchantApplication/uploadMedia` — 上传进件媒体文件 · `MerchantApplicationController#uploadMedia`

#### payment::category_fee_rule — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\finance\CategoryFeeRuleController.java`
- 目标四层：`crates/core/src/models/payment/category_fee_rule.rs` · `crates/infrastructure/src/db/repo/payment/category_fee_rule.rs` · `crates/services/src/payment/category_fee_rule.rs` · `crates/api/admin/src/routes/payment/category_fee_rule.rs` · `crates/api/public/src/routes/payment/category_fee_rule.rs`
- [ ] `POST /adminapi/finance/categoryFeeRule/checkConflict` — 冲突预检 · `CategoryFeeRuleController#checkConflict` · 权限 `categoryFeeRuleManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/categoryFeeRule/del` — 删除 · `CategoryFeeRuleController#del` · 权限 `categoryFeeRuleUpdate` · auth=`jwt+perm`
- [ ] `GET /adminapi/finance/categoryFeeRule/detail` — 详情 · `CategoryFeeRuleController#detail` · 权限 `categoryFeeRuleManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/categoryFeeRule/list` — 分页列表 · `CategoryFeeRuleController#list` · 权限 `categoryFeeRuleManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/categoryFeeRule/save` — 新建 · `CategoryFeeRuleController#save` · 权限 `categoryFeeRuleUpdate` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/categoryFeeRule/update` — 编辑 · `CategoryFeeRuleController#update` · 权限 `categoryFeeRuleUpdate` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/categoryFeeRule/updateStatus` — 启用/禁用 · `CategoryFeeRuleController#updateStatus` · 权限 `categoryFeeRuleUpdate` · auth=`jwt+perm`

#### payment::order_invoice — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\finance\OrderInvoiceController.java`
- 目标四层：`crates/core/src/models/payment/order_invoice.rs` · `crates/infrastructure/src/db/repo/payment/order_invoice.rs` · `crates/services/src/payment/order_invoice.rs` · `crates/api/admin/src/routes/payment/order_invoice.rs` · `crates/api/public/src/routes/payment/order_invoice.rs`
- [ ] `POST /adminapi/finance/orderInvoice/batch` — 批量操作 · `OrderInvoiceController#batch` · 权限 `orderInvoiceBatchManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/orderInvoice/create` — 创建 · `OrderInvoiceController#create` · 权限 `orderInvoiceUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/orderInvoice/del` — 删除 · `OrderInvoiceController#del` · 权限 `orderInvoiceDelManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/finance/orderInvoice/detail` — 获取详情 · `OrderInvoiceController#detail`
- [ ] `GET /adminapi/finance/orderInvoice/list` — 获取列表 · `OrderInvoiceController#list`
- [ ] `POST /adminapi/finance/orderInvoice/update` — 更新 · `OrderInvoiceController#update` · 权限 `orderInvoiceUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/orderInvoice/updateField` — 更新字段 · `OrderInvoiceController#updateField` · 权限 `orderInvoiceUpdateManage` · auth=`jwt+perm`

#### payment::recharge_order — 7 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\user\RechargeOrderController.java`
- 目标四层：`crates/core/src/models/payment/recharge_order.rs` · `crates/infrastructure/src/db/repo/payment/recharge_order.rs` · `crates/services/src/payment/recharge_order.rs` · `crates/api/admin/src/routes/payment/recharge_order.rs` · `crates/api/public/src/routes/payment/recharge_order.rs`
- [ ] `GET /api/user/rechargeOrder/checkStatus` — 获取充值支付状态 · `RechargeOrderController#checkStatus` · auth=`optional`
- [ ] `POST /api/user/rechargeOrder/create` — 充值支付 · `RechargeOrderController#create` · auth=`optional`
- [ ] `GET /api/user/rechargeOrder/list` — 充值记录列表 · `RechargeOrderController#list` · auth=`optional`
- [ ] `POST /api/user/rechargeOrder/pay` — 充值支付 · `RechargeOrderController#pay` · auth=`optional`
- [ ] `GET /api/user/rechargeOrder/paymentList` — 充值支付方式 · `RechargeOrderController#paymentList` · auth=`optional`
- [ ] `GET /api/user/rechargeOrder/setting` — 充值配置 · `RechargeOrderController#settingPage` · auth=`optional`
- [ ] `POST /api/user/rechargeOrder/update` — 充值申请 · `RechargeOrderController#update` · auth=`optional`

#### payment::refund_log — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\finance\RefundLogController.java`
- 目标四层：`crates/core/src/models/payment/refund_log.rs` · `crates/infrastructure/src/db/repo/payment/refund_log.rs` · `crates/services/src/payment/refund_log.rs` · `crates/api/admin/src/routes/payment/refund_log.rs` · `crates/api/public/src/routes/payment/refund_log.rs`
- [ ] `POST /adminapi/finance/refundLog/batch` — 批量操作 · `RefundLogController#batch`
- [ ] `POST /adminapi/finance/refundLog/create` — 创建 · `RefundLogController#create`
- [ ] `POST /adminapi/finance/refundLog/del` — 删除 · `RefundLogController#del`
- [ ] `GET /adminapi/finance/refundLog/detail` — 获取详情 · `RefundLogController#detail`
- [ ] `GET /adminapi/finance/refundLog/list` — 获取列表 · `RefundLogController#list`
- [ ] `POST /adminapi/finance/refundLog/update` — 更新 · `RefundLogController#update`
- [ ] `POST /adminapi/finance/refundLog/updateField` — 更新字段 · `RefundLogController#updateField`

#### payment::user_invoice — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\finance\UserInvoiceController.java`
- 目标四层：`crates/core/src/models/payment/user_invoice.rs` · `crates/infrastructure/src/db/repo/payment/user_invoice.rs` · `crates/services/src/payment/user_invoice.rs` · `crates/api/admin/src/routes/payment/user_invoice.rs` · `crates/api/public/src/routes/payment/user_invoice.rs`
- [ ] `POST /adminapi/finance/userInvoice/batch` — 批量操作 · `UserInvoiceController#batch` · 权限 `userInvoiceBatchManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/userInvoice/create` — 创建 · `UserInvoiceController#create`
- [ ] `POST /adminapi/finance/userInvoice/del` — 删除 · `UserInvoiceController#del` · 权限 `userInvoiceDelManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/finance/userInvoice/detail` — 获取详情 · `UserInvoiceController#detail`
- [ ] `GET /adminapi/finance/userInvoice/list` — 获取列表 · `UserInvoiceController#list`
- [ ] `POST /adminapi/finance/userInvoice/update` — 更新 · `UserInvoiceController#update` · 权限 `userInvoiceUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/userInvoice/updateField` — 更新字段 · `UserInvoiceController#updateField`

#### payment::user_recharge_order — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\finance\UserRechargeOrderController.java`
- 目标四层：`crates/core/src/models/payment/user_recharge_order.rs` · `crates/infrastructure/src/db/repo/payment/user_recharge_order.rs` · `crates/services/src/payment/user_recharge_order.rs` · `crates/api/admin/src/routes/payment/user_recharge_order.rs` · `crates/api/public/src/routes/payment/user_recharge_order.rs`
- [ ] `POST /adminapi/finance/userRechargeOrder/batch` — 批量操作 · `UserRechargeOrderController#batch` · 权限 `userRechargeOrderBatchManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/userRechargeOrder/create` — 创建 · `UserRechargeOrderController#create` · 权限 `userRechargeOrderUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/userRechargeOrder/del` — 删除 · `UserRechargeOrderController#del` · 权限 `userRechargeOrderDelManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/finance/userRechargeOrder/detail` — 获取详情 · `UserRechargeOrderController#detail`
- [ ] `GET /adminapi/finance/userRechargeOrder/list` — 获取列表 · `UserRechargeOrderController#list`
- [ ] `POST /adminapi/finance/userRechargeOrder/update` — 更新 · `UserRechargeOrderController#update` · 权限 `userRechargeOrderUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/userRechargeOrder/updateField` — 更新字段 · `UserRechargeOrderController#updateField`

#### payment::user_withdraw_apply — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\finance\UserWithdrawApplyController.java`
- 目标四层：`crates/core/src/models/payment/user_withdraw_apply.rs` · `crates/infrastructure/src/db/repo/payment/user_withdraw_apply.rs` · `crates/services/src/payment/user_withdraw_apply.rs` · `crates/api/admin/src/routes/payment/user_withdraw_apply.rs` · `crates/api/public/src/routes/payment/user_withdraw_apply.rs`
- [ ] `POST /adminapi/finance/userWithdrawApply/batch` — 批量操作 · `UserWithdrawApplyController#batch` · 权限 `userWithdrawApplyBatchManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/userWithdrawApply/create` — 创建 · `UserWithdrawApplyController#create` · 权限 `userWithdrawApplyUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/userWithdrawApply/del` — 删除 · `UserWithdrawApplyController#del` · 权限 `userWithdrawApplyDelManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/finance/userWithdrawApply/detail` — 获取详情 · `UserWithdrawApplyController#detail`
- [ ] `GET /adminapi/finance/userWithdrawApply/list` — 获取列表 · `UserWithdrawApplyController#list`
- [ ] `POST /adminapi/finance/userWithdrawApply/update` — 更新 · `UserWithdrawApplyController#update` · 权限 `userWithdrawApplyUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/userWithdrawApply/updateField` — 更新字段 · `UserWithdrawApplyController#updateField` · 权限 `userWithdrawApplyUpdateManage` · auth=`jwt+perm`

#### payment::with_draw_apply_client — 7 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\user\WithDrawApplyClientController.java`
- 目标四层：`crates/core/src/models/payment/with_draw_apply_client.rs` · `crates/infrastructure/src/db/repo/payment/with_draw_apply_client.rs` · `crates/services/src/payment/with_draw_apply_client.rs` · `crates/api/admin/src/routes/payment/with_draw_apply_client.rs` · `crates/api/public/src/routes/payment/with_draw_apply_client.rs`
- [ ] `GET /api/user/withdrawApply/accountDetail` — 提现账号详情 · `WithDrawApplyClientController#accountDetail` · auth=`optional`
- [ ] `POST /api/user/withdrawApply/apply` — 提现申请 · `WithDrawApplyClientController#apply` · auth=`optional`
- [ ] `POST /api/user/withdrawApply/applyCommission` — 佣金提现申请（D1107） · `WithDrawApplyClientController#applyCommission` · auth=`optional`
- [ ] `POST /api/user/withdrawApply/createAccount` — 创建提现账号 · `WithDrawApplyClientController#createAccount` · auth=`optional`
- [ ] `POST /api/user/withdrawApply/delAccount` — 删除提现账号 · `WithDrawApplyClientController#delAccount` · auth=`optional`
- [ ] `GET /api/user/withdrawApply/list` — 列表 · `WithDrawApplyClientController#list` · auth=`optional`
- [ ] `POST /api/user/withdrawApply/updateAccount` — 修改提现账号 · `WithDrawApplyClientController#updateAccount` · auth=`optional`

#### payment::credit_settlement — 6 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\finance\CreditSettlementController.java`
- 目标四层：`crates/core/src/models/payment/credit_settlement.rs` · `crates/infrastructure/src/db/repo/payment/credit_settlement.rs` · `crates/services/src/payment/credit_settlement.rs` · `crates/api/admin/src/routes/payment/credit_settlement.rs` · `crates/api/public/src/routes/payment/credit_settlement.rs`
- [ ] `POST /adminapi/credit/settlement/cancel` — 撤销结算 · `CreditSettlementController#cancel` · 权限 `termPayModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/credit/settlement/history` — 历史结算列表 · `CreditSettlementController#history` · 权限 `termPayManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/credit/settlement/historyOrders` — 历史结算订单明细 · `CreditSettlementController#historyOrders` · 权限 `termPayManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/credit/settlement/pendingOrders` — 待结算订单列表 · `CreditSettlementController#pendingOrders` · 权限 `termPayManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/credit/settlement/submit` — 确认结算 · `CreditSettlementController#submit` · 权限 `termPayModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/credit/settlement/userList` — 结算会员列表 · `CreditSettlementController#userList` · 权限 `termPayManage` · auth=`jwt+perm`

#### payment::credit_user — 6 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\finance\CreditUserController.java`
- 目标四层：`crates/core/src/models/payment/credit_user.rs` · `crates/infrastructure/src/db/repo/payment/credit_user.rs` · `crates/services/src/payment/credit_user.rs` · `crates/api/admin/src/routes/payment/credit_user.rs` · `crates/api/public/src/routes/payment/credit_user.rs`
- [ ] `POST /adminapi/credit/user/create` — 添加授信会员 · `CreditUserController#create` · 权限 `termMemberModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/credit/user/delete` — 删除授信会员 · `CreditUserController#delete` · 权限 `termMemberModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/credit/user/detail` — 授信会员详情 · `CreditUserController#detail` · 权限 `termMemberManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/credit/user/list` — 授信会员列表 · `CreditUserController#list` · 权限 `termMemberManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/credit/user/update` — 修改授信会员 · `CreditUserController#update` · 权限 `termMemberModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/credit/user/updateField` — 更新授信会员字段（用于启用/禁用等） · `CreditUserController#updateField` · 权限 `termMemberModifyManage` · auth=`jwt+perm`

#### payment::pay — 6 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\order\PayController.java`
- 目标四层：`crates/core/src/models/payment/pay.rs` · `crates/infrastructure/src/db/repo/payment/pay.rs` · `crates/services/src/payment/pay.rs` · `crates/api/admin/src/routes/payment/pay.rs` · `crates/api/public/src/routes/payment/pay.rs`
- [ ] `GET /api/order/pay/checkStatus` — 检测订单支付状态 · `PayController#checkStatus` · auth=`optional`
- [ ] `GET /api/order/pay/create` — 支付订单创建（尾款支付/普通订单支付） · `PayController#create` · auth=`optional`
- [ ] `GET /api/order/pay/createDepositPayment` — 预售定金支付创建 · `PayController#createDepositPayment` · auth=`optional`
- [ ] `GET /api/order/pay/getPayLog` — 检测订单支付状态 · `PayController#getPayLog` · auth=`optional`
- [ ] `GET /api/order/pay/index` — 订单支付 · `PayController#index` · auth=`optional`
- [ ] `GET /api/order/pay/paymentList` — 支付方式列表（下单前收银台，无需订单） · `PayController#paymentList` · auth=`optional`

#### payment::statement — 6 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\finance\StatementController.java`
- 目标四层：`crates/core/src/models/payment/statement.rs` · `crates/infrastructure/src/db/repo/payment/statement.rs` · `crates/services/src/payment/statement.rs` · `crates/api/admin/src/routes/payment/statement.rs` · `crates/api/public/src/routes/payment/statement.rs`
- [ ] `GET /adminapi/finance/statement/exportStatement` — 导出对账单 · `StatementController#exportStatement`
- [ ] `GET /adminapi/finance/statement/exportStatementStatistics` — 导出对账单统计 · `StatementController#exportStatementStatistics`
- [ ] `GET /adminapi/finance/statement/getStatementList` — 对账单列表 · `StatementController#getStatementList`
- [ ] `GET /adminapi/finance/statement/getStatementQueryConfig` — 查询字段 · `StatementController#getStatementQueryConfig`
- [ ] `GET /adminapi/finance/statement/getStatementStatisticsList` — 对账单统计 · `StatementController#getStatementStatisticsList`
- [ ] `POST /adminapi/finance/statement/saveStatementDownload` — 保存对账单下载信息 · `StatementController#saveStatementDownload`

#### payment::apple_pay — 4 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\order\ApplePayController.java`
- 目标四层：`crates/core/src/models/payment/apple_pay.rs` · `crates/infrastructure/src/db/repo/payment/apple_pay.rs` · `crates/services/src/payment/apple_pay.rs` · `crates/api/admin/src/routes/payment/apple_pay.rs` · `crates/api/public/src/routes/payment/apple_pay.rs`
- [ ] `POST /api/order/pay/applepay/confirm` — Apple Pay 支付确认（自建原生流程：原始 token） · `ApplePayController#applePayConfirm` · auth=`optional`
- [ ] `POST /api/order/pay/applepay/confirmByPaymentMethod` — Apple Pay 支付确认（PSP 前端驱动：paymentMethodId，如 Stripe） · `ApplePayController#applePayConfirmByPaymentMethod` · auth=`optional`
- [ ] `POST /api/order/pay/applepay/validateMerchant` — Apple Pay 商户校验 · `ApplePayController#applePayValidateMerchant` · auth=`optional`
- [ ] `POST /api/order/pay/notify/ums` — 银联商务(UMS) Apple Pay 支付结果异步通知 · `ApplePayController#umsApplePayNotify` · auth=`optional`

#### payment::profit_sharing — 3 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\finance\ProfitSharingController.java`
- 目标四层：`crates/core/src/models/payment/profit_sharing.rs` · `crates/infrastructure/src/db/repo/payment/profit_sharing.rs` · `crates/services/src/payment/profit_sharing.rs` · `crates/api/admin/src/routes/payment/profit_sharing.rs` · `crates/api/public/src/routes/payment/profit_sharing.rs`
- [ ] `GET /adminapi/finance/profitSharing/list` — 分账单分页列表 · `ProfitSharingController#list`
- [ ] `POST /adminapi/finance/profitSharing/manualFinish` — 手动完结分账单 · `ProfitSharingController#manualFinish` · 权限 `profitSharingSettlementFinish` · auth=`jwt+perm`
- [ ] `POST /adminapi/finance/profitSharing/submitBatch` — 批量发起分账 · `ProfitSharingController#submitBatch` · 权限 `profitSharingSettlementSubmit` · auth=`jwt+perm`

#### payment::refund_apply_admin — 3 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\finance\RefundApplyController.java`
- 目标四层：`crates/core/src/models/payment/refund_apply_admin.rs` · `crates/infrastructure/src/db/repo/payment/refund_apply_admin.rs` · `crates/services/src/payment/refund_apply_admin.rs` · `crates/api/admin/src/routes/payment/refund_apply_admin.rs` · `crates/api/public/src/routes/payment/refund_apply_admin.rs`
- [ ] `POST /adminapi/finance/refundApply/batch` — 批量操作 · `RefundApplyController#batch`
- [ ] `POST /adminapi/finance/refundApply/del` — 删除 · `RefundApplyController#del`
- [ ] `POST /adminapi/finance/refundApply/updateField` — 更新字段 · `RefundApplyController#updateField`

#### payment::sub_merchant — 3 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\finance\SubMerchantController.java`
- 目标四层：`crates/core/src/models/payment/sub_merchant.rs` · `crates/infrastructure/src/db/repo/payment/sub_merchant.rs` · `crates/services/src/payment/sub_merchant.rs` · `crates/api/admin/src/routes/payment/sub_merchant.rs` · `crates/api/public/src/routes/payment/sub_merchant.rs`
- [ ] `POST /adminapi/finance/subMerchant/bind` — 手动绑定子商户 · `SubMerchantController#bind` · 权限 `subMerchantBind` · auth=`jwt+perm`
- [ ] `GET /adminapi/finance/subMerchant/list` — 子商户分页列表 · `SubMerchantController#list`
- [ ] `POST /adminapi/finance/subMerchant/unbind` — 解绑子商户 · `SubMerchantController#unbind` · 权限 `subMerchantUnbind` · auth=`jwt+perm`

#### payment::apple_pay_config — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\ApplePayConfigController.java`
- 目标四层：`crates/core/src/models/payment/apple_pay_config.rs` · `crates/infrastructure/src/db/repo/payment/apple_pay_config.rs` · `crates/services/src/payment/apple_pay_config.rs` · `crates/api/admin/src/routes/payment/apple_pay_config.rs` · `crates/api/public/src/routes/payment/apple_pay_config.rs`
- [ ] `GET /adminapi/setting/config/applePaySettings` — 获取系统设置-Apple Pay支付设置 · `ApplePayConfigController#getApplePaySettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveApplePay` — 保存系统设置-Apple Pay支付设置 · `ApplePayConfigController#saveApplePaySettings` · 权限 `uploadFileModifyManage` · auth=`jwt+perm`

#### payment::huifu_config — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\HuifuConfigController.java`
- 目标四层：`crates/core/src/models/payment/huifu_config.rs` · `crates/infrastructure/src/db/repo/payment/huifu_config.rs` · `crates/services/src/payment/huifu_config.rs` · `crates/api/admin/src/routes/payment/huifu_config.rs` · `crates/api/public/src/routes/payment/huifu_config.rs`
- [ ] `GET /adminapi/setting/config/huifuSettings` — 获取系统设置-汇付斗拱支付设置 · `HuifuConfigController#getHuifuSettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveHuifu` — 保存系统设置-汇付斗拱支付设置 · `HuifuConfigController#saveHuifuSettings` · 权限 `uploadFileModifyManage` · auth=`jwt+perm`

#### payment::lakala_config — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\LakalaConfigController.java`
- 目标四层：`crates/core/src/models/payment/lakala_config.rs` · `crates/infrastructure/src/db/repo/payment/lakala_config.rs` · `crates/services/src/payment/lakala_config.rs` · `crates/api/admin/src/routes/payment/lakala_config.rs` · `crates/api/public/src/routes/payment/lakala_config.rs`
- [ ] `GET /adminapi/setting/config/lakalaSettings` — 获取系统设置-拉卡拉支付设置 · `LakalaConfigController#getLakalaSettings` · 权限 `config` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/config/saveLakala` — 保存系统设置-拉卡拉支付设置 · `LakalaConfigController#saveLakalaSettings` · 权限 `uploadFileModifyManage` · auth=`jwt+perm`

#### payment::user_account — 2 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\user\AccountController.java`
- 目标四层：`crates/core/src/models/payment/user_account.rs` · `crates/infrastructure/src/db/repo/payment/user_account.rs` · `crates/services/src/payment/user_account.rs` · `crates/api/admin/src/routes/payment/user_account.rs` · `crates/api/public/src/routes/payment/user_account.rs`
- [ ] `GET /api/user/account/commissionList` — 佣金账户明细列表（D1107） · `AccountController#commissionList` · auth=`optional`
- [ ] `GET /api/user/account/list` — 账户信息列表 · `AccountController#list` · auth=`optional`

#### payment::yeepay_config — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\YeepayConfigController.java`
- 目标四层：`crates/core/src/models/payment/yeepay_config.rs` · `crates/infrastructure/src/db/repo/payment/yeepay_config.rs` · `crates/services/src/payment/yeepay_config.rs` · `crates/api/admin/src/routes/payment/yeepay_config.rs` · `crates/api/public/src/routes/payment/yeepay_config.rs`
- [ ] `POST /adminapi/setting/config/saveYeepay` — 保存系统设置-易宝支付设置 · `YeepayConfigController#saveYeepaySettings` · 权限 `uploadFileModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/config/yeepaySettings` — 获取系统设置-易宝支付设置 · `YeepayConfigController#getYeepaySettings` · 权限 `config` · auth=`jwt+perm`

#### payment::account_panel — 1 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\finance\AccountPanelController.java`
- 目标四层：`crates/core/src/models/payment/account_panel.rs` · `crates/infrastructure/src/db/repo/payment/account_panel.rs` · `crates/services/src/payment/account_panel.rs` · `crates/api/admin/src/routes/payment/account_panel.rs` · `crates/api/public/src/routes/payment/account_panel.rs`
- [ ] `GET /adminapi/finance/accountPanel/list` — 获取数据 · `AccountPanelController#list`

#### payment::credit_order — 1 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\finance\CreditOrderController.java`
- 目标四层：`crates/core/src/models/payment/credit_order.rs` · `crates/infrastructure/src/db/repo/payment/credit_order.rs` · `crates/services/src/payment/credit_order.rs` · `crates/api/admin/src/routes/payment/credit_order.rs` · `crates/api/public/src/routes/payment/credit_order.rs`
- [ ] `GET /adminapi/credit/order/list` — 账期订单列表 · `CreditOrderController#list` · 权限 `termOrderManage` · auth=`jwt+perm`

### A.6 `member`

#### member::login — 16 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\user\LoginController.java`；`tigshop-api\src\main\java\com\tigshop\api\controller\user\LoginController.java`
- 目标四层：`crates/core/src/models/member/login.rs` · `crates/infrastructure/src/db/repo/member/login.rs` · `crates/services/src/member/login.rs` · `crates/api/admin/src/routes/member/login.rs` · `crates/api/public/src/routes/member/login.rs`
- [ ] `POST /adminapi/login/sendMobileCode` — 发送短信验证码 · `LoginController#sendMobileCode` · auth=`anonymous`
- [ ] `POST /api/user/login/bindMobile` — 绑定手机 · `LoginController#bindMobile` · auth=`anonymous?`
- [ ] `POST /api/user/login/bindWechat` — 绑定微信公众号 · `LoginController#bindWechat` · auth=`anonymous?`
- [ ] `POST /api/user/login/checkEmail` — 校验邮箱,仅用于重置密码 · `LoginController#checkEmail` · auth=`anonymous?`
- [ ] `POST /api/user/login/checkMobile` — 校验手机号,仅用于重置密码 · `LoginController#checkMobile` · auth=`anonymous?`
- [ ] `POST /api/user/login/forgetPassword` — 修改密码 · `LoginController#forgetPassword` · auth=`anonymous?`
- [ ] `POST /api/user/login/getMobile` — 获取用户手机号 · `LoginController#getMobile` · auth=`anonymous?`
- [ ] `GET /api/user/login/getQuickLoginSetting` — 获取快捷登录设置 · `LoginController#getQuickLoginSetting` · auth=`anonymous?`
- [ ] `GET /api/user/login/getWxLoginInfoByCode` — 通过微信code获得微信用户信息 · `LoginController#getWxLoginInfoByCode` · auth=`anonymous?`
- [ ] `GET /api/user/login/getWxLoginUrl` — 获得微信授权url · `LoginController#getWechatLoginUrl` · auth=`anonymous?`
- [ ] `POST /api/user/login/sendEmailCode` — 发送邮箱验证码 · `LoginController#sendEmailCode` · auth=`anonymous?`
- [ ] `POST /api/user/login/sendMobileCode` — 发送短信验证码 · `LoginController#sendMobileCode` · auth=`anonymous?`
- [ ] `GET /api/user/login/unbindWechat` — 解绑微信公众号 · `LoginController#unbindWechat` · auth=`anonymous?`
- [ ] `POST /api/user/login/updateUserOpenid` — 获取用户openid · `LoginController#updateUserOpenId` · auth=`anonymous?`
- [ ] `POST /api/user/login/wechatEvent` — 检测用户扫码后处理事件 · `LoginController#wechatEvent` · auth=`anonymous?`
- [ ] `ANY /api/user/login/wechatServer` — 微信服务器校验 · `LoginController#wechatServer` · auth=`anonymous?`

#### member::shop_member — 9 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\user\ShopMemberController.java`
- 目标四层：`crates/core/src/models/member/shop_member.rs` · `crates/infrastructure/src/db/repo/member/shop_member.rs` · `crates/services/src/member/shop_member.rs` · `crates/api/admin/src/routes/member/shop_member.rs` · `crates/api/public/src/routes/member/shop_member.rs`
- [ ] `POST /adminapi/user/shopMember/add` — 店铺新增客户 · `ShopMemberController#add` · 权限 `shopMemberCustomerManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/user/shopMember/detail` — 会员详情 · `ShopMemberController#detail` · 权限 `shopMemberManage,shopMemberCustomerManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/user/shopMember/downloadImportTemplate` — 下载店铺客户导入模板 · `ShopMemberController#downloadImportTemplate` · 权限 `shopMemberCustomerManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/user/shopMember/import` — 店铺导入客户 · `ShopMemberController#importMembers` · 权限 `shopMemberCustomerManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/user/shopMember/importErrorData` — 导出导入失败行 · `ShopMemberController#importErrorData` · 权限 `shopMemberCustomerManage` · auth=`anonymous`
- [ ] `GET /adminapi/user/shopMember/list` — 会员列表 · `ShopMemberController#list` · 权限 `shopMemberManage,shopMemberCustomerManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/user/shopMember/setting/get` — 读取店铺会员总开关 · `ShopMemberController#settingGet` · 权限 `shopMemberSettingManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/user/shopMember/setting/save` — 保存店铺会员总开关 · `ShopMemberController#settingSave` · 权限 `shopMemberSettingManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/user/shopMember/shopList` — 平台端店铺列表（含会员数） · `ShopMemberController#shopList` · 权限 `shopMemberManage` · auth=`jwt+perm`

#### member::user_address_client — 7 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\user\UserAddressClientController.java`
- 目标四层：`crates/core/src/models/member/user_address_client.rs` · `crates/infrastructure/src/db/repo/member/user_address_client.rs` · `crates/services/src/member/user_address_client.rs` · `crates/api/admin/src/routes/member/user_address_client.rs` · `crates/api/public/src/routes/member/user_address_client.rs`
- [ ] `POST /api/user/address/create` — 创建收货地址 · `UserAddressClientController#create` · auth=`optional`
- [ ] `POST /api/user/address/del` — 删除收货地址 · `UserAddressClientController#del` · auth=`optional`
- [ ] `GET /api/user/address/detail` — 收货地址详情 · `UserAddressClientController#detail` · auth=`optional`
- [ ] `GET /api/user/address/list` — 收货地址列表 · `UserAddressClientController#list` · auth=`optional`
- [ ] `POST /api/user/address/setSelected` — 设置选中状态 · `UserAddressClientController#setSelected` · auth=`optional`
- [ ] `GET /api/user/address/tagList` — 收货地址常用标签列表 · `UserAddressClientController#tagList` · auth=`optional`
- [ ] `POST /api/user/address/update` — 修改收货地址 · `UserAddressClientController#update` · auth=`optional`

#### member::user_message — 6 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\user\UserMessageController.java`
- 目标四层：`crates/core/src/models/member/user_message.rs` · `crates/infrastructure/src/db/repo/member/user_message.rs` · `crates/services/src/member/user_message.rs` · `crates/api/admin/src/routes/member/user_message.rs` · `crates/api/public/src/routes/member/user_message.rs`
- [ ] `POST /api/user/message/del` — 删除 · `UserMessageController#del` · auth=`optional`
- [ ] `GET /api/user/message/list` — 获取列表 · `UserMessageController#list` · auth=`optional`
- [ ] `GET /api/user/message/unreadCount` — 获取未读消息统计 · `UserMessageController#getUnreadMessageCount` · auth=`optional`
- [ ] `POST /api/user/message/updateAllRead` — 设置为全部已读 · `UserMessageController#setAllMessageRead` · auth=`optional`
- [ ] `POST /api/user/message/updateMessageRead` — 设置为已读 · `UserMessageController#setMessageRead` · auth=`optional`
- [ ] `POST /api/user/message/updateOrderMessageRead` — 设置订单消息为已读 · `UserMessageController#setOrderMessageRead` · auth=`optional`

#### member::user_member_tag — 5 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\user\UserMemberTagController.java`
- 目标四层：`crates/core/src/models/member/user_member_tag.rs` · `crates/infrastructure/src/db/repo/member/user_member_tag.rs` · `crates/services/src/member/user_member_tag.rs` · `crates/api/admin/src/routes/member/user_member_tag.rs` · `crates/api/public/src/routes/member/user_member_tag.rs`
- [ ] `POST /adminapi/user/userMemberTag/batchBindTags` — 批量给用户打标签 · `UserMemberTagController#batchBindTags` · 权限 `memberTagUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/user/userMemberTag/batchUnbindTags` — 批量取消用户标签 · `UserMemberTagController#batchUnbindTags` · 权限 `memberTagUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/user/userMemberTag/bindTags` — 给用户打标签 · `UserMemberTagController#bindTags` · 权限 `memberTagUpdateManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/user/userMemberTag/listByUser` — 查询用户的所有标签 · `UserMemberTagController#listByUser` · 权限 `memberTagManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/user/userMemberTag/unbindTags` — 取消用户标签 · `UserMemberTagController#unbindTags` · 权限 `memberTagUpdateManage` · auth=`jwt+perm`

#### member::user_message_log — 5 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\user\UserMessageLogController.java`
- 目标四层：`crates/core/src/models/member/user_message_log.rs` · `crates/infrastructure/src/db/repo/member/user_message_log.rs` · `crates/services/src/member/user_message_log.rs` · `crates/api/admin/src/routes/member/user_message_log.rs` · `crates/api/public/src/routes/member/user_message_log.rs`
- [ ] `POST /adminapi/user/userMessageLog/batch` — 批量操作 · `UserMessageLogController#batch` · 权限 `userMessageLogModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/user/userMessageLog/create` — 创建会员消息 · `UserMessageLogController#create` · 权限 `userMessageLogModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/user/userMessageLog/del` — 删除会员消息 · `UserMessageLogController#del` · 权限 `userMessageLogModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/user/userMessageLog/list` — 会员消息列表 · `UserMessageLogController#list` · 权限 `messageLogManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/user/userMessageLog/recall` — 撤回 · `UserMessageLogController#recall` · 权限 `userMessageLogModifyManage` · auth=`jwt+perm`

#### member::user_invoice_client — 4 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\user\UserInvoiceClientController.java`
- 目标四层：`crates/core/src/models/member/user_invoice_client.rs` · `crates/infrastructure/src/db/repo/member/user_invoice_client.rs` · `crates/services/src/member/user_invoice_client.rs` · `crates/api/admin/src/routes/member/user_invoice_client.rs` · `crates/api/public/src/routes/member/user_invoice_client.rs`
- [ ] `POST /api/user/invoice/create` — 增票资质创建 · `UserInvoiceClientController#create` · auth=`optional`
- [ ] `GET /api/user/invoice/detail` — 增票资质详情 · `UserInvoiceClientController#detail` · auth=`optional`
- [ ] `GET /api/user/invoice/getStatus` — 判断当前用户的增票资质是否审核通过 · `UserInvoiceClientController#getStatus` · auth=`optional`
- [ ] `POST /api/user/invoice/update` — 增票资质修改 · `UserInvoiceClientController#update` · auth=`optional`

#### member::user_price_inquiry — 4 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\user\UserPriceInquiryController.java`
- 目标四层：`crates/core/src/models/member/user_price_inquiry.rs` · `crates/infrastructure/src/db/repo/member/user_price_inquiry.rs` · `crates/services/src/member/user_price_inquiry.rs` · `crates/api/admin/src/routes/member/user_price_inquiry.rs` · `crates/api/public/src/routes/member/user_price_inquiry.rs`
- [ ] `POST /api/user/priceInquiry/del` — 删除询价（仅用户侧隐藏） · `UserPriceInquiryController#del` · auth=`optional`
- [ ] `GET /api/user/priceInquiry/detail` — 获取当前登录用户询价详情 · `UserPriceInquiryController#detail` · auth=`optional`
- [ ] `GET /api/user/priceInquiry/list` — 获取当前登录用户询价列表 · `UserPriceInquiryController#list` · auth=`optional`
- [ ] `POST /api/user/priceInquiry/reply` — 用户回复商家 · `UserPriceInquiryController#reply` · auth=`optional`

#### member::captcha_api — 3 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\user\CaptchaApiController.java`
- 目标四层：`crates/core/src/models/member/captcha_api.rs` · `crates/infrastructure/src/db/repo/member/captcha_api.rs` · `crates/services/src/member/captcha_api.rs` · `crates/api/admin/src/routes/member/captcha_api.rs` · `crates/api/public/src/routes/member/captcha_api.rs`
- [ ] `POST /api/common/verification/captcha` — 获取验证码 · `CaptchaApiController#captcha` · auth=`anonymous?`
- [ ] `POST /api/common/verification/check` — 校验验证码 · `CaptchaApiController#check` · auth=`optional`
- [ ] `POST /api/common/verification/get` — 获取验证码(废弃) · `CaptchaApiController#get` · auth=`optional`

#### member::collect_product — 3 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\user\CollectProductController.java`
- 目标四层：`crates/core/src/models/member/collect_product.rs` · `crates/infrastructure/src/db/repo/member/collect_product.rs` · `crates/services/src/member/collect_product.rs` · `crates/api/admin/src/routes/member/collect_product.rs` · `crates/api/public/src/routes/member/collect_product.rs`
- [ ] `POST /api/user/collectProduct/cancel` — 取消商品收藏 · `CollectProductController#cancel` · auth=`optional`
- [ ] `GET /api/user/collectProduct/list` — 商品收藏列表 · `CollectProductController#list` · auth=`optional`
- [ ] `POST /api/user/collectProduct/save` — 商品收藏 · `CollectProductController#save` · auth=`optional`

#### member::user_company_client — 3 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\user\UserCompanyClientController.java`
- 目标四层：`crates/core/src/models/member/user_company_client.rs` · `crates/infrastructure/src/db/repo/member/user_company_client.rs` · `crates/services/src/member/user_company_client.rs` · `crates/api/admin/src/routes/member/user_company_client.rs` · `crates/api/public/src/routes/member/user_company_client.rs`
- [ ] `POST /api/user/company/apply` — 实名申请 · `UserCompanyClientController#apply` · auth=`optional`
- [ ] `GET /api/user/company/detail` — 实名认证详情 · `UserCompanyClientController#detail` · auth=`optional`
- [ ] `GET /api/user/company/myApply` — 我的实名认证申请 · `UserCompanyClientController#myApply` · auth=`optional`

#### member::member_audience — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\user\MemberAudienceController.java`
- 目标四层：`crates/core/src/models/member/member_audience.rs` · `crates/infrastructure/src/db/repo/member/member_audience.rs` · `crates/services/src/member/member_audience.rs` · `crates/api/admin/src/routes/member/member_audience.rs` · `crates/api/public/src/routes/member/member_audience.rs`
- [ ] `POST /adminapi/user/memberAudience/describe` — 人群条件文字描述 · `MemberAudienceController#describe`
- [ ] `POST /adminapi/user/memberAudience/estimate` — 预估人群命中人数 · `MemberAudienceController#estimate`

#### member::user_feedback_client — 2 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\user\UserFeedbackClientController.java`
- 目标四层：`crates/core/src/models/member/user_feedback_client.rs` · `crates/infrastructure/src/db/repo/member/user_feedback_client.rs` · `crates/services/src/member/user_feedback_client.rs` · `crates/api/admin/src/routes/member/user_feedback_client.rs` · `crates/api/public/src/routes/member/user_feedback_client.rs`
- [ ] `GET /api/user/feedback/list` — 订单咨询/留言列表 · `UserFeedbackClientController#list` · auth=`optional`
- [ ] `POST /api/user/feedback/submit` — 提交留言 · `UserFeedbackClientController#submit` · auth=`optional`

#### member::user_sign — 2 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\user\UserSignController.java`
- 目标四层：`crates/core/src/models/member/user_sign.rs` · `crates/infrastructure/src/db/repo/member/user_sign.rs` · `crates/services/src/member/user_sign.rs` · `crates/api/admin/src/routes/member/user_sign.rs` · `crates/api/public/src/routes/member/user_sign.rs`
- [ ] `GET /api/user/sign/index` — 绛惧埌棣栭〉 · `UserSignController#getUserSignInfoVo` · auth=`optional`
- [ ] `GET /api/user/sign/sign` — 绛惧埌 · `UserSignController#sign` · auth=`optional`

### A.7 `merchant`

#### merchant::deposit_recharge — 10 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\deposit\DepositRechargeController.java`
- 目标四层：`crates/core/src/models/merchant/deposit_recharge.rs` · `crates/infrastructure/src/db/repo/merchant/deposit_recharge.rs` · `crates/services/src/merchant/deposit_recharge.rs` · `crates/api/admin/src/routes/merchant/deposit_recharge.rs` · `crates/api/public/src/routes/merchant/deposit_recharge.rs`
- [ ] `POST /adminapi/deposit/recharge/apply` — 店铺端发起充值申请 · `DepositRechargeController#apply`
- [ ] `POST /adminapi/deposit/recharge/audit` — 平台端审核充值单(通过入账/驳回) · `DepositRechargeController#audit` · 权限 `depositRechargeAudit` · auth=`jwt+perm`
- [ ] `GET /adminapi/deposit/recharge/detail` — 平台端充值单详情 · `DepositRechargeController#detail` · 权限 `depositPaymentAuditManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/deposit/recharge/list` — 平台端充值单列表 · `DepositRechargeController#list` · 权限 `depositPaymentAuditManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/deposit/recharge/payeeAccountOptions` — 充值可选收款账户列表(店铺/供应商端，仅启用) · `DepositRechargeController#payeeAccountOptions`
- [ ] `GET /adminapi/deposit/recharge/shopPaymentAuditDetail` — 店铺端款项审核详情(按auditType取充值/退还单，校验归属) · `DepositRechargeController#shopPaymentAuditDetail`
- [ ] `GET /adminapi/deposit/recharge/shopPaymentAuditList` — 店铺端款项审核列表(充值+退还合并，按提交时间倒序) · `DepositRechargeController#shopPaymentAuditList`
- [ ] `POST /adminapi/deposit/recharge/vendorApply` — 供应商端发起充值申请 · `DepositRechargeController#vendorApply`
- [ ] `GET /adminapi/deposit/recharge/vendorPaymentAuditDetail` — 供应商端款项审核详情(按auditType取充值/退还单，校验归属) · `DepositRechargeController#vendorPaymentAuditDetail`
- [ ] `GET /adminapi/deposit/recharge/vendorPaymentAuditList` — 供应商端款项审核列表(充值+退还合并，按提交时间倒序) · `DepositRechargeController#vendorPaymentAuditList`

#### merchant::admin_user_shop — 9 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\shop\AdminUserShopController.java`
- 目标四层：`crates/core/src/models/merchant/admin_user_shop.rs` · `crates/infrastructure/src/db/repo/merchant/admin_user_shop.rs` · `crates/services/src/merchant/admin_user_shop.rs` · `crates/api/admin/src/routes/merchant/admin_user_shop.rs` · `crates/api/public/src/routes/merchant/admin_user_shop.rs`
- [ ] `POST /adminapi/merchant/adminUserShop/batch` — 批量删除店铺员工 · `AdminUserShopController#batch` · 权限 `adminUserShopModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/merchant/adminUserShop/checkIsAdmin` — 判断当前用户是否为当前店铺/门店的主账号 · `AdminUserShopController#checkIsAdmin`
- [ ] `POST /adminapi/merchant/adminUserShop/create` — 创建店铺员工 · `AdminUserShopController#create` · 权限 `adminUserShopModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/merchant/adminUserShop/del` — 删除店铺员工 · `AdminUserShopController#del` · 权限 `adminUserShopModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/merchant/adminUserShop/detail` — 获取详情 · `AdminUserShopController#detail`
- [ ] `GET /adminapi/merchant/adminUserShop/info` — 获取店铺员工信息 · `AdminUserShopController#info`
- [ ] `GET /adminapi/merchant/adminUserShop/list` — 获取列表 · `AdminUserShopController#list`
- [ ] `POST /adminapi/merchant/adminUserShop/modifyUser` — 修改店铺员工用户 · `AdminUserShopController#modifyUser` · 权限 `adminUserShopModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/merchant/adminUserShop/update` — 更新店铺员工 · `AdminUserShopController#update` · 权限 `adminUserShopModifyManage` · auth=`jwt+perm`

#### merchant::deposit_account — 8 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\deposit\DepositAccountController.java`
- 目标四层：`crates/core/src/models/merchant/deposit_account.rs` · `crates/infrastructure/src/db/repo/merchant/deposit_account.rs` · `crates/services/src/merchant/deposit_account.rs` · `crates/api/admin/src/routes/merchant/deposit_account.rs` · `crates/api/public/src/routes/merchant/deposit_account.rs`
- [ ] `POST /adminapi/deposit/account/adjust` — 手动调整应缴 · `DepositAccountController#adjust` · 权限 `depositAdjust` · auth=`jwt+perm`
- [ ] `GET /adminapi/deposit/account/adjustLogList` — 详情-调整记录 · `DepositAccountController#adjustLogList` · 权限 `depositAccountManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/deposit/account/deduct` — 赔付扣除 · `DepositAccountController#deduct` · 权限 `depositDeduct` · auth=`jwt+perm`
- [ ] `GET /adminapi/deposit/account/detail` — 账户详情-基本信息 · `DepositAccountController#detail` · 权限 `depositAccountManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/deposit/account/list` — 账户分页总览 · `DepositAccountController#list` · 权限 `depositAccountManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/deposit/account/logList` — 详情-交易记录 · `DepositAccountController#logList` · 权限 `depositAccountManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/deposit/account/runDueCheck` — 手动触发保证金到期扫描(测试/运维用,免登录免鉴权) · `DepositAccountController#runDueCheck` · 权限 `depositAccountManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/deposit/account/sendWarning` — 发送保证金预警 · `DepositAccountController#sendWarning` · 权限 `depositSendWarning` · auth=`jwt+perm`

#### merchant::merchant — 8 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\merchant\MerchantController.java`
- 目标四层：`crates/core/src/models/merchant/merchant.rs` · `crates/infrastructure/src/db/repo/merchant/merchant.rs` · `crates/services/src/merchant/merchant.rs` · `crates/api/admin/src/routes/merchant/merchant.rs` · `crates/api/public/src/routes/merchant/merchant.rs`
- [ ] `POST /adminapi/merchant/merchant/batch` — 批量操作 · `MerchantController#batch` · 权限 `merchantModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/merchant/merchant/create` — 创建 · `MerchantController#create` · 权限 `merchantModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/merchant/merchant/del` — 删除 · `MerchantController#del` · 权限 `merchantModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/merchant/merchant/detail` — 获取详情 · `MerchantController#detail`
- [ ] `GET /adminapi/merchant/merchant/list` — 获取列表 · `MerchantController#list`
- [ ] `POST /adminapi/merchant/merchant/update` — 更新 · `MerchantController#update` · 权限 `merchantModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/merchant/merchant/updateField` — 更新字段 · `MerchantController#updateField` · 权限 `merchantModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/merchant/merchant/uploadLicense` — 入驻营业执照专用上传 · `MerchantController#uploadLicense` · 权限 `merchantModifyManage` · auth=`jwt+perm`

#### merchant::merchant_apply — 8 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\merchant\MerchantApplyController.java`
- 目标四层：`crates/core/src/models/merchant/merchant_apply.rs` · `crates/infrastructure/src/db/repo/merchant/merchant_apply.rs` · `crates/services/src/merchant/merchant_apply.rs` · `crates/api/admin/src/routes/merchant/merchant_apply.rs` · `crates/api/public/src/routes/merchant/merchant_apply.rs`
- [ ] `POST /adminapi/merchant/apply/audit` — 审核操作 · `MerchantApplyController#audit` · 权限 `merchantApplyModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/merchant/apply/batch` — 批量操作 · `MerchantApplyController#batch` · 权限 `merchantApplyModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/merchant/apply/config` — 获取状态参数 · `MerchantApplyController#config`
- [ ] `POST /adminapi/merchant/apply/del` — 删除 · `MerchantApplyController#del` · 权限 `merchantApplyModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/merchant/apply/detail` — 获取详情 · `MerchantApplyController#detail`
- [ ] `GET /adminapi/merchant/apply/list` — 获取列表 · `MerchantApplyController#list`
- [ ] `POST /adminapi/merchant/apply/update` — 更新 · `MerchantApplyController#update` · 权限 `merchantApplyModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/merchant/apply/updateField` — 更新字段 · `MerchantApplyController#updateField` · 权限 `merchantApplyModifyManage` · auth=`jwt+perm`

#### merchant::shop_funds — 8 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\shop\ShopFundsController.java`
- 目标四层：`crates/core/src/models/merchant/shop_funds.rs` · `crates/infrastructure/src/db/repo/merchant/shop_funds.rs` · `crates/services/src/merchant/shop_funds.rs` · `crates/api/admin/src/routes/merchant/shop_funds.rs` · `crates/api/public/src/routes/merchant/shop_funds.rs`
- [ ] `GET /adminapi/merchant/shopAccount/applyShopAgreement` — 申请入驻协议 · `ShopFundsController#applyShopAgreement`
- [ ] `POST /adminapi/merchant/shopAccount/batch` — 批量操作 · `ShopFundsController#batch`
- [ ] `POST /adminapi/merchant/shopAccount/create` — 创建 · `ShopFundsController#create`
- [ ] `POST /adminapi/merchant/shopAccount/del` — 删除 · `ShopFundsController#del`
- [ ] `GET /adminapi/merchant/shopAccount/detail` — 获取详情 · `ShopFundsController#detail`
- [ ] `GET /adminapi/merchant/shopAccount/list` — 获取列表 · `ShopFundsController#list`
- [ ] `POST /adminapi/merchant/shopAccount/update` — 更新 · `ShopFundsController#update`
- [ ] `POST /adminapi/merchant/shopAccount/updateField` — 更新字段 · `ShopFundsController#updateField`

#### merchant::shop_withdraw — 8 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\shop\ShopWithdrawController.java`
- 目标四层：`crates/core/src/models/merchant/shop_withdraw.rs` · `crates/infrastructure/src/db/repo/merchant/shop_withdraw.rs` · `crates/services/src/merchant/shop_withdraw.rs` · `crates/api/admin/src/routes/merchant/shop_withdraw.rs` · `crates/api/public/src/routes/merchant/shop_withdraw.rs`
- [ ] `POST /adminapi/merchant/shopWithdraw/batch` — 批量操作 · `ShopWithdrawController#batch`
- [ ] `POST /adminapi/merchant/shopWithdraw/create` — 创建 · `ShopWithdrawController#create` · 权限 `merchantShopWithdrawModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/merchant/shopWithdraw/del` — 删除 · `ShopWithdrawController#del`
- [ ] `GET /adminapi/merchant/shopWithdraw/dualBalance` — 双余额查询（线下余额 + 三渠道子商户可用余额） · `ShopWithdrawController#dualBalance`
- [ ] `GET /adminapi/merchant/shopWithdraw/summaryList` — 店铺提现汇总列表 · `ShopWithdrawController#summaryList`
- [ ] `POST /adminapi/merchant/shopWithdraw/update` — 更新 · `ShopWithdrawController#update` · 权限 `merchantShopWithdrawModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/merchant/shopWithdraw/updateField` — 更新字段 · `ShopWithdrawController#updateField`
- [ ] `POST /adminapi/merchant/shopWithdraw/uploadPayVoucher` — 上传打款凭证 · `ShopWithdrawController#uploadPayVoucher` · 权限 `merchantShopWithdrawModifyManage` · auth=`jwt+perm`

#### merchant::deposit_refund — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\deposit\DepositRefundController.java`
- 目标四层：`crates/core/src/models/merchant/deposit_refund.rs` · `crates/infrastructure/src/db/repo/merchant/deposit_refund.rs` · `crates/services/src/merchant/deposit_refund.rs` · `crates/api/admin/src/routes/merchant/deposit_refund.rs` · `crates/api/public/src/routes/merchant/deposit_refund.rs`
- [ ] `POST /adminapi/deposit/refund/apply` — 店铺端发起退还/提现申请 · `DepositRefundController#apply`
- [ ] `POST /adminapi/deposit/refund/audit` — 平台端一步式审核退还单(通过即出账完成/驳回) · `DepositRefundController#audit` · 权限 `depositRefundAudit` · auth=`jwt+perm`
- [ ] `GET /adminapi/deposit/refund/detail` — 平台端退还单详情 · `DepositRefundController#detail` · 权限 `depositPaymentAuditManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/deposit/refund/fullRefundCheck` — 店铺端查询能否全额退还保证金 · `DepositRefundController#fullRefundCheck`
- [ ] `GET /adminapi/deposit/refund/list` — 平台端退还单列表 · `DepositRefundController#list` · 权限 `depositPaymentAuditManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/deposit/refund/vendorApply` — 供应商端发起退还/提现申请 · `DepositRefundController#vendorApply`
- [ ] `GET /adminapi/deposit/refund/vendorFullRefundCheck` — 供应商端查询能否全额退还保证金 · `DepositRefundController#vendorFullRefundCheck`

#### merchant::merchant_account — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\merchant\MerchantAccountController.java`
- 目标四层：`crates/core/src/models/merchant/merchant_account.rs` · `crates/infrastructure/src/db/repo/merchant/merchant_account.rs` · `crates/services/src/merchant/merchant_account.rs` · `crates/api/admin/src/routes/merchant/merchant_account.rs` · `crates/api/public/src/routes/merchant/merchant_account.rs`
- [ ] `POST /adminapi/merchant/account/batch` — 批量操作 · `MerchantAccountController#batch` · 权限 `accountModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/merchant/account/config` — 获取配置 · `MerchantAccountController#config`
- [ ] `POST /adminapi/merchant/account/create` — 创建 · `MerchantAccountController#create` · 权限 `accountModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/merchant/account/del` — 删除 · `MerchantAccountController#del` · 权限 `accountModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/merchant/account/detail` — 获取详情 · `MerchantAccountController#detail`
- [ ] `GET /adminapi/merchant/account/list` — 获取列表 · `MerchantAccountController#list`
- [ ] `POST /adminapi/merchant/account/update` — 更新 · `MerchantAccountController#update` · 权限 `accountModifyManage` · auth=`jwt+perm`

#### merchant::deposit_rule — 6 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\deposit\DepositRuleController.java`
- 目标四层：`crates/core/src/models/merchant/deposit_rule.rs` · `crates/infrastructure/src/db/repo/merchant/deposit_rule.rs` · `crates/services/src/merchant/deposit_rule.rs` · `crates/api/admin/src/routes/merchant/deposit_rule.rs` · `crates/api/public/src/routes/merchant/deposit_rule.rs`
- [ ] `POST /adminapi/deposit/rule/create` — 创建规则 · `DepositRuleController#create` · 权限 `depositRuleUpdate` · auth=`jwt+perm`
- [ ] `POST /adminapi/deposit/rule/del` — 删除规则 · `DepositRuleController#del` · 权限 `depositRuleUpdate` · auth=`jwt+perm`
- [ ] `GET /adminapi/deposit/rule/detail` — 获取规则详情 · `DepositRuleController#detail` · 权限 `depositRuleManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/deposit/rule/list` — 获取规则列表 · `DepositRuleController#list` · 权限 `depositRuleManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/deposit/rule/status` — 启用/停用规则 · `DepositRuleController#status` · 权限 `depositRuleUpdate` · auth=`jwt+perm`
- [ ] `POST /adminapi/deposit/rule/update` — 更新规则 · `DepositRuleController#update` · 权限 `depositRuleUpdate` · auth=`jwt+perm`

#### merchant::deposit_shop — 5 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\deposit\DepositShopController.java`
- 目标四层：`crates/core/src/models/merchant/deposit_shop.rs` · `crates/infrastructure/src/db/repo/merchant/deposit_shop.rs` · `crates/services/src/merchant/deposit_shop.rs` · `crates/api/admin/src/routes/merchant/deposit_shop.rs` · `crates/api/public/src/routes/merchant/deposit_shop.rs`
- [ ] `GET /adminapi/deposit/shop/frozenDetailList` — 冻结明细(平台扣款记录) · `DepositShopController#frozenDetailList`
- [ ] `GET /adminapi/deposit/shop/logList` — 店铺端交易流水 · `DepositShopController#logList`
- [ ] `GET /adminapi/deposit/shop/overview` — 保证金总览 · `DepositShopController#overview`
- [ ] `GET /adminapi/deposit/shop/protocol` — 保证金协议 · `DepositShopController#protocol`
- [ ] `GET /adminapi/deposit/shop/warning` — 首页保证金预警横幅 · `DepositShopController#warning`

#### merchant::deposit_payee_account — 4 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\deposit\DepositPayeeAccountController.java`
- 目标四层：`crates/core/src/models/merchant/deposit_payee_account.rs` · `crates/infrastructure/src/db/repo/merchant/deposit_payee_account.rs` · `crates/services/src/merchant/deposit_payee_account.rs` · `crates/api/admin/src/routes/merchant/deposit_payee_account.rs` · `crates/api/public/src/routes/merchant/deposit_payee_account.rs`
- [ ] `POST /adminapi/deposit/payeeAccount/create` — 创建收款账户 · `DepositPayeeAccountController#create` · 权限 `depositPayeeAccountUpdate` · auth=`jwt+perm`
- [ ] `POST /adminapi/deposit/payeeAccount/del` — 删除收款账户 · `DepositPayeeAccountController#del` · 权限 `depositPayeeAccountUpdate` · auth=`jwt+perm`
- [ ] `GET /adminapi/deposit/payeeAccount/list` — 获取收款账户分页列表 · `DepositPayeeAccountController#list` · 权限 `depositPayeeAccountManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/deposit/payeeAccount/update` — 更新收款账户 · `DepositPayeeAccountController#update` · 权限 `depositPayeeAccountUpdate` · auth=`jwt+perm`

#### merchant::deposit_log — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\deposit\DepositLogController.java`
- 目标四层：`crates/core/src/models/merchant/deposit_log.rs` · `crates/infrastructure/src/db/repo/merchant/deposit_log.rs` · `crates/services/src/merchant/deposit_log.rs` · `crates/api/admin/src/routes/merchant/deposit_log.rs` · `crates/api/public/src/routes/merchant/deposit_log.rs`
- [ ] `GET /adminapi/deposit/log/detail` — 交易明细详情 · `DepositLogController#detail` · 权限 `depositLogManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/deposit/log/list` — 全局交易明细分页 · `DepositLogController#list` · 权限 `depositLogManage` · auth=`jwt+perm`

#### merchant::deposit_setting — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\deposit\DepositSettingController.java`
- 目标四层：`crates/core/src/models/merchant/deposit_setting.rs` · `crates/infrastructure/src/db/repo/merchant/deposit_setting.rs` · `crates/services/src/merchant/deposit_setting.rs` · `crates/api/admin/src/routes/merchant/deposit_setting.rs` · `crates/api/public/src/routes/merchant/deposit_setting.rs`
- [ ] `GET /adminapi/deposit/setting/detail` — 获取保证金设置 · `DepositSettingController#detail` · 权限 `depositSettingManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/deposit/setting/save` — 保存保证金设置 · `DepositSettingController#save` · 权限 `depositSettingSave` · auth=`jwt+perm`

#### merchant::shop_account_log — 1 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\shop\AccountController.java`
- 目标四层：`crates/core/src/models/merchant/shop_account_log.rs` · `crates/infrastructure/src/db/repo/merchant/shop_account_log.rs` · `crates/services/src/merchant/shop_account_log.rs` · `crates/api/admin/src/routes/merchant/shop_account_log.rs` · `crates/api/public/src/routes/merchant/shop_account_log.rs`
- [ ] `GET /adminapi/merchant/shopAccount/index` — 获取统计数据 · `AccountController#index`

### A.8 `wholesale`

#### wholesale::wholesale_product — 13 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\wholesale\WholesaleProductController.java`
- 目标四层：`crates/core/src/models/wholesale/wholesale_product.rs` · `crates/infrastructure/src/db/repo/wholesale/wholesale_product.rs` · `crates/services/src/wholesale/wholesale_product.rs` · `crates/api/admin/src/routes/wholesale/wholesale_product.rs` · `crates/api/public/src/routes/wholesale/wholesale_product.rs`
- [ ] `POST /adminapi/wholesale/product/add` — 添加批发商品（从商品库选择） · `WholesaleProductController#add`
- [ ] `POST /adminapi/wholesale/product/batchAdd` — 批量添加批发商品（一次选多个普通商品转批发） · `WholesaleProductController#batchAdd`
- [ ] `POST /adminapi/wholesale/product/batchDelete` — 批量删除批发商品 · `WholesaleProductController#batchDelete`
- [ ] `POST /adminapi/wholesale/product/buyRule/batchSave` — 起批量批量设置（CR-20260604-1042；一次给 N 个 product 应用同一对 dimension + moq） · `WholesaleProductController#buyRuleBatchSave`
- [ ] `GET /adminapi/wholesale/product/buyRule/detail` — 起批规则详情 · `WholesaleProductController#buyRuleDetail`
- [ ] `POST /adminapi/wholesale/product/buyRule/save` — 起批量设置（CR-20260604-0937：统一字段 moq） · `WholesaleProductController#buyRuleSave`
- [ ] `POST /adminapi/wholesale/product/multiple/save` — 倍数购买保存（按 SKU 批量） · `WholesaleProductController#multipleSave`
- [ ] `GET /adminapi/wholesale/product/page` — 批发商品分页 · `WholesaleProductController#page`
- [ ] `POST /adminapi/wholesale/product/price/batchSetByGrade` — 批量设置类型售价 · `WholesaleProductController#batchSetByGrade`
- [ ] `POST /adminapi/wholesale/product/price/batchSetCommon` — 批量设置通用售价 · `WholesaleProductController#batchSetCommon`
- [ ] `GET /adminapi/wholesale/product/price/detail` — 批发价详情 · `WholesaleProductController#priceDetail`
- [ ] `POST /adminapi/wholesale/product/price/save` — 批发价保存（单规格 + 多规格统一） · `WholesaleProductController#priceSave`
- [ ] `GET /adminapi/wholesale/product/selectable/page` — 可选批发商品分页（当前操作者名下商品 + 剔除已配置批发；CR-20260603-1700 / CR-20260610 多租户） · `WholesaleProductController#selectablePage`

#### wholesale::wholesaler — 8 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\wholesale\WholesalerController.java`
- 目标四层：`crates/core/src/models/wholesale/wholesaler.rs` · `crates/infrastructure/src/db/repo/wholesale/wholesaler.rs` · `crates/services/src/wholesale/wholesaler.rs` · `crates/api/admin/src/routes/wholesale/wholesaler.rs` · `crates/api/public/src/routes/wholesale/wholesaler.rs`
- [ ] `POST /adminapi/wholesale/wholesaler/add` — 添加批发商 · `WholesalerController#add`
- [ ] `POST /adminapi/wholesale/wholesaler/changeGrade` — 批发商改等级 · `WholesalerController#changeGrade`
- [ ] `POST /adminapi/wholesale/wholesaler/import` — 批量导入批发商 · `WholesalerController#importBatch`
- [ ] `GET /adminapi/wholesale/wholesaler/page` — 批发商分页 · `WholesalerController#page`
- [ ] `POST /adminapi/wholesale/wholesaler/remove` — 按 ID 直接移除批发商身份 · `WholesalerController#remove`
- [ ] `POST /adminapi/wholesale/wholesaler/removeByGrade` — 按等级移除批发商 · `WholesalerController#removeByGrade`
- [ ] `GET /adminapi/wholesale/wholesaler/selectableUserPage` — 可选为批发商的会员分页（含 isWholesaler 标识，添加批发商选会员用） · `WholesalerController#selectableUserPage`
- [ ] `POST /adminapi/wholesale/wholesaler/setStatus` — 批发商启停 · `WholesalerController#setStatus`

#### wholesale::wholesaler_grade — 4 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\wholesale\WholesalerGradeController.java`
- 目标四层：`crates/core/src/models/wholesale/wholesaler_grade.rs` · `crates/infrastructure/src/db/repo/wholesale/wholesaler_grade.rs` · `crates/services/src/wholesale/wholesaler_grade.rs` · `crates/api/admin/src/routes/wholesale/wholesaler_grade.rs` · `crates/api/public/src/routes/wholesale/wholesaler_grade.rs`
- [ ] `POST /adminapi/wholesale/grade/delete` — 等级删除（软删；绑定批发商→5305 拦截，默认等级→5306 拦截） · `WholesalerGradeController#delete` · 权限 `wholesalerGradeDeleteManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/wholesale/grade/list` — 等级列表 · `WholesalerGradeController#list`
- [ ] `POST /adminapi/wholesale/grade/save` — 等级新增/编辑 · `WholesalerGradeController#save` · 权限 `wholesalerGradeSaveManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/wholesale/grade/setStatus` — 等级启停 · `WholesalerGradeController#setStatus` · 权限 `wholesalerGradeSetStatusManage` · auth=`jwt+perm`

#### wholesale::wholesale_setting — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\wholesale\WholesaleSettingController.java`
- 目标四层：`crates/core/src/models/wholesale/wholesale_setting.rs` · `crates/infrastructure/src/db/repo/wholesale/wholesale_setting.rs` · `crates/services/src/wholesale/wholesale_setting.rs` · `crates/api/admin/src/routes/wholesale/wholesale_setting.rs` · `crates/api/public/src/routes/wholesale/wholesale_setting.rs`
- [ ] `GET /adminapi/wholesale/setting/get` — 查询批发设置 · `WholesaleSettingController#get`
- [ ] `POST /adminapi/wholesale/setting/save` — 保存批发设置 · `WholesaleSettingController#save`

### A.9 `logistics`

#### logistics::print — 8 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\print\PrintController.java`
- 目标四层：`crates/core/src/models/logistics/print.rs` · `crates/infrastructure/src/db/repo/logistics/print.rs` · `crates/services/src/logistics/print.rs` · `crates/api/admin/src/routes/logistics/print.rs` · `crates/api/public/src/routes/logistics/print.rs`
- [ ] `POST /adminapi/print/print/create` — 新增打印机 · `PrintController#create` · 权限 `baseReceiptUpdateManage,shopBaseReceiptUpdateManage,posReceiptPrintUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/print/print/del` — 删除 · `PrintController#del` · 权限 `baseReceiptUpdateManage,shopBaseReceiptUpdateManage,posReceiptPrintUpdateManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/print/print/detail` — 获取详情 · `PrintController#detail` · 权限 `baseReceiptManage,shopBaseReceiptManage,posReceiptPrint` · auth=`jwt+perm`
- [ ] `GET /adminapi/print/print/devicePrinters` — 查询收银台设备可用打印机 · `PrintController#devicePrinters`
- [ ] `POST /adminapi/print/print/hasEnabled` — 是否有启用的打印机 · `PrintController#hasEnabled`
- [ ] `GET /adminapi/print/print/list` — 获取列表 · `PrintController#list` · 权限 `baseReceiptManage,shopBaseReceiptManage,posReceiptPrint` · auth=`jwt+perm`
- [ ] `POST /adminapi/print/print/update` — 编辑打印机 · `PrintController#update` · 权限 `baseReceiptUpdateManage,shopBaseReceiptUpdateManage,posReceiptPrintUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/print/print/updateField` — 更新字段 · `PrintController#updateField` · 权限 `baseReceiptUpdateManage,shopBaseReceiptUpdateManage,posReceiptPrintUpdateManage` · auth=`jwt+perm`

#### logistics::print_log — 1 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\print\PrintLogController.java`
- 目标四层：`crates/core/src/models/logistics/print_log.rs` · `crates/infrastructure/src/db/repo/logistics/print_log.rs` · `crates/services/src/logistics/print_log.rs` · `crates/api/admin/src/routes/logistics/print_log.rs` · `crates/api/public/src/routes/logistics/print_log.rs`
- [ ] `GET /adminapi/print/printLog/list` — 获取打印日志分页列表 · `PrintLogController#list` · 权限 `baseReceiptManage,shopBaseReceiptManage,posReceiptPrint` · auth=`jwt+perm`

### A.10 `marketing`

#### marketing::groupon_promotion — 12 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\promotion\GrouponPromotionController.java`；`tigshop-api\src\main\java\com\tigshop\api\controller\promotion\GrouponPromotionController.java`
- 目标四层：`crates/core/src/models/marketing/groupon_promotion.rs` · `crates/infrastructure/src/db/repo/marketing/groupon_promotion.rs` · `crates/services/src/marketing/groupon_promotion.rs` · `crates/api/admin/src/routes/marketing/groupon_promotion.rs` · `crates/api/public/src/routes/marketing/groupon_promotion.rs`
- [ ] `GET /adminapi/promotion/groupon/clientList` — 获取客户端列表 · `GrouponPromotionController#clientList` · 权限 `grouponPromotionManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/groupon/create` — 创建 · `GrouponPromotionController#create` · 权限 `grouponPromotionModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/groupon/del` — 删除 · `GrouponPromotionController#del` · 权限 `grouponPromotionModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/groupon/detail` — 获取详情 · `GrouponPromotionController#detail` · 权限 `grouponPromotionManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/groupon/finish` — 立即结束 · `GrouponPromotionController#finish` · 权限 `grouponPromotionModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/groupon/list` — 获取列表 · `GrouponPromotionController#list` · 权限 `grouponPromotionManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/groupon/promotionPromote` — 活动推广 · `GrouponPromotionController#post` · 权限 `grouponPromotionManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/groupon/update` — 更新 · `GrouponPromotionController#update` · 权限 `grouponPromotionModifyManage` · auth=`jwt+perm`
- [ ] `GET /api/promotion/groupon/detail` — 客户端详情 · `GrouponPromotionController#clientDetail` · auth=`optional`
- [ ] `GET /api/promotion/groupon/getGrouponInfo` — 商品id获取拼团活动信息 · `GrouponPromotionController#getGrouponInfo` · auth=`optional`
- [ ] `GET /api/promotion/groupon/imageList` — 拼团图片列表 · `GrouponPromotionController#grouponPromotionImages` · auth=`optional`
- [ ] `GET /api/promotion/groupon/itemList` — 拼团活动商品列表 · `GrouponPromotionController#itemList` · auth=`optional`

#### marketing::salesman_content — 10 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\salesman\SalesmanContentController.java`；`tigshop-api\src\main\java\com\tigshop\api\controller\salesman\SalesmanContentController.java`
- 目标四层：`crates/core/src/models/marketing/salesman_content.rs` · `crates/infrastructure/src/db/repo/marketing/salesman_content.rs` · `crates/services/src/marketing/salesman_content.rs` · `crates/api/admin/src/routes/marketing/salesman_content.rs` · `crates/api/public/src/routes/marketing/salesman_content.rs`
- [ ] `POST /adminapi/salesman/content/batch` — 批量操作 · `SalesmanContentController#batch` · 权限 `salesmanContentModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/salesman/content/config` — 获取配置 · `SalesmanContentController#config`
- [ ] `POST /adminapi/salesman/content/create` — 创建 · `SalesmanContentController#create` · 权限 `salesmanContentModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/salesman/content/del` — 删除 · `SalesmanContentController#del` · 权限 `salesmanContentModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/salesman/content/detail` — 获取详情 · `SalesmanContentController#detail`
- [ ] `GET /adminapi/salesman/content/list` — 获取列表 · `SalesmanContentController#list`
- [ ] `POST /adminapi/salesman/content/update` — 更新 · `SalesmanContentController#update` · 权限 `salesmanContentModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/salesman/content/updateField` — 更新字段 · `SalesmanContentController#updateField` · 权限 `salesmanContentModifyManage` · auth=`jwt+perm`
- [ ] `GET /api/salesman/content/detail` — 获取详情 · `SalesmanContentController#detail` · auth=`optional`
- [ ] `GET /api/salesman/content/list` — 获取列表 · `SalesmanContentController#list` · auth=`optional`

#### marketing::coupon_manage — 9 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\promotion\CouponController.java`
- 目标四层：`crates/core/src/models/marketing/coupon_manage.rs` · `crates/infrastructure/src/db/repo/marketing/coupon_manage.rs` · `crates/services/src/marketing/coupon_manage.rs` · `crates/api/admin/src/routes/marketing/coupon_manage.rs` · `crates/api/public/src/routes/marketing/coupon_manage.rs`
- [ ] `POST /adminapi/promotion/coupon/batch` — 批量操作 · `CouponController#batch` · 权限 `promotionCouponModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/coupon/config` — 营销配置项 · `CouponController#config` · 权限 `couponManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/coupon/create` — 创建 · `CouponController#create` · 权限 `promotionCouponModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/coupon/del` — 删除 · `CouponController#del` · 权限 `promotionCouponModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/coupon/issue/batch` — 优惠券发放批次列表 · `CouponController#issueBatch` · 权限 `couponManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/coupon/issue/log` — 优惠券发放日志列表 · `CouponController#issueLog` · 权限 `couponManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/coupon/issue/preview` — 手动发放优惠券预览 · `CouponController#issuePreview` · 权限 `couponManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/coupon/update` — 更新 · `CouponController#update` · 权限 `promotionCouponModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/coupon/updateField` — 更新字段 · `CouponController#updateField` · 权限 `promotionCouponModifyManage` · auth=`jwt+perm`

#### marketing::product_promotion — 9 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\promotion\ProductPromotionController.java`
- 目标四层：`crates/core/src/models/marketing/product_promotion.rs` · `crates/infrastructure/src/db/repo/marketing/product_promotion.rs` · `crates/services/src/marketing/product_promotion.rs` · `crates/api/admin/src/routes/marketing/product_promotion.rs` · `crates/api/public/src/routes/marketing/product_promotion.rs`
- [ ] `POST /adminapi/promotion/productPromotion/batch` — 批量操作 · `ProductPromotionController#batch` · 权限 `productPromotionModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/productPromotion/config` — 获取配置 · `ProductPromotionController#config`
- [ ] `GET /adminapi/promotion/productPromotion/conflict` — 活动冲突列表 · `ProductPromotionController#conflictPage`
- [ ] `POST /adminapi/promotion/productPromotion/create` — 创建 · `ProductPromotionController#create` · 权限 `productPromotionModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/productPromotion/del` — 删除 · `ProductPromotionController#del` · 权限 `productPromotionModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/productPromotion/detail` — 获取详情 · `ProductPromotionController#detail`
- [ ] `GET /adminapi/promotion/productPromotion/list` — 获取列表 · `ProductPromotionController#list`
- [ ] `POST /adminapi/promotion/productPromotion/update` — 更新 · `ProductPromotionController#update` · 权限 `productPromotionModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/productPromotion/updateField` — 更新字段 · `ProductPromotionController#updateField` · 权限 `productPromotionModifyManage` · auth=`jwt+perm`

#### marketing::gift_card — 8 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\promotion\GiftCardController.java`
- 目标四层：`crates/core/src/models/marketing/gift_card.rs` · `crates/infrastructure/src/db/repo/marketing/gift_card.rs` · `crates/services/src/marketing/gift_card.rs` · `crates/api/admin/src/routes/marketing/gift_card.rs` · `crates/api/public/src/routes/marketing/gift_card.rs`
- [ ] `POST /adminapi/promotion/giftCards/delete` — 礼品卡删除 · `GiftCardController#deleteGiftCards` · 权限 `giftCardUpdateManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/giftCards/detail` — 礼品卡详情 · `GiftCardController#detail`
- [ ] `GET /adminapi/promotion/giftCards/exportList` — 礼品卡列表导出 · `GiftCardController#exportList`
- [ ] `GET /adminapi/promotion/giftCards/giftCardExchangeDetail` — 礼品卡兑换详情 · `GiftCardController#giftCardExchangeDetail`
- [ ] `POST /adminapi/promotion/giftCards/importList` — 礼品卡列表导入 · `GiftCardController#importList` · 权限 `giftCardUpdateManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/giftCards/importTemplate` — 礼品卡导入模板 · `GiftCardController#importTemplate`
- [ ] `GET /adminapi/promotion/giftCards/list` — 礼品卡列表 · `GiftCardController#list`
- [ ] `POST /adminapi/promotion/giftCards/updateCardStatus` — 礼品卡修改可用状态 · `GiftCardController#updateGiftCardsAvailableStatus`

#### marketing::gift_card_groups — 8 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\promotion\GiftCardGroupsController.java`
- 目标四层：`crates/core/src/models/marketing/gift_card_groups.rs` · `crates/infrastructure/src/db/repo/marketing/gift_card_groups.rs` · `crates/services/src/marketing/gift_card_groups.rs` · `crates/api/admin/src/routes/marketing/gift_card_groups.rs` · `crates/api/public/src/routes/marketing/gift_card_groups.rs`
- [ ] `POST /adminapi/promotion/giftCardGroups/create` — 创建礼品卡分组 · `GiftCardGroupsController#createGiftCardGroups` · 权限 `giftCardUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/giftCardGroups/delete` — 删除礼品卡分组 · `GiftCardGroupsController#deleteGiftCardGroups` · 权限 `giftCardUpdateManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/giftCardGroups/detail` — 礼品卡分组详情 · `GiftCardGroupsController#detail`
- [ ] `GET /adminapi/promotion/giftCardGroups/getGiftCardGroups` — 获取礼品卡分组 · `GiftCardGroupsController#getGiftCardGroupsByCardType`
- [ ] `POST /adminapi/promotion/giftCardGroups/giftCardGroupProductsUpdate` — 礼品卡绑定商品管理 · `GiftCardGroupsController#giftCardGroupProductsUpdate` · 权限 `giftCardUpdateManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/giftCardGroups/list` — 礼品卡分组列表 · `GiftCardGroupsController#list`
- [ ] `POST /adminapi/promotion/giftCardGroups/update` — 修改礼品卡分组 · `GiftCardGroupsController#updateGiftCardGroups` · 权限 `giftCardUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/giftCardGroups/updateStatus` — 修改礼品卡分组状态 · `GiftCardGroupsController#updateStatus` · 权限 `giftCardUpdateManage` · auth=`jwt+perm`

#### marketing::groupon_promotion_item — 8 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\promotion\GrouponPromotionItemController.java`
- 目标四层：`crates/core/src/models/marketing/groupon_promotion_item.rs` · `crates/infrastructure/src/db/repo/marketing/groupon_promotion_item.rs` · `crates/services/src/marketing/groupon_promotion_item.rs` · `crates/api/admin/src/routes/marketing/groupon_promotion_item.rs` · `crates/api/public/src/routes/marketing/groupon_promotion_item.rs`
- [ ] `POST /adminapi/promotion/groupon/item/batch` — 鎵归噺鎿嶄綔 · `GrouponPromotionItemController#batch` · 权限 `grouponPromotionManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/groupon/item/create` — 鍒涘缓 · `GrouponPromotionItemController#create` · 权限 `grouponPromotionModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/groupon/item/del` — 鍒犻櫎 · `GrouponPromotionItemController#del` · 权限 `grouponPromotionModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/groupon/item/detail` — 鑾峰彇璇︽儏 · `GrouponPromotionItemController#detail` · 权限 `grouponPromotionManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/groupon/item/list` — 鑾峰彇鍒楄〃 · `GrouponPromotionItemController#list` · 权限 `grouponPromotionManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/groupon/item/post` — 鎺ㄥ箍 · `GrouponPromotionItemController#post` · 权限 `grouponPromotionManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/groupon/item/productList` — 鎷煎洟鍟嗗搧鍒楄〃 · `GrouponPromotionItemController#productList` · 权限 `grouponPromotionManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/groupon/item/update` — 鏇存柊 · `GrouponPromotionItemController#update` · 权限 `grouponPromotionModifyManage` · auth=`jwt+perm`

#### marketing::lottery_activity — 8 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\promotion\LotteryActivityController.java`
- 目标四层：`crates/core/src/models/marketing/lottery_activity.rs` · `crates/infrastructure/src/db/repo/marketing/lottery_activity.rs` · `crates/services/src/marketing/lottery_activity.rs` · `crates/api/admin/src/routes/marketing/lottery_activity.rs` · `crates/api/public/src/routes/marketing/lottery_activity.rs`
- [ ] `POST /adminapi/promotion/lotteryActivity/create` — 创建抽奖活动 · `LotteryActivityController#create` · 权限 `lotteryActivityModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/lotteryActivity/delete` — 删除抽奖活动 · `LotteryActivityController#delete` · 权限 `lotteryActivityModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/lotteryActivity/detail` — 抽奖活动详情 · `LotteryActivityController#detail` · 权限 `lotteryManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/lotteryActivity/end` — 结束抽奖活动 · `LotteryActivityController#end` · 权限 `lotteryActivityModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/lotteryActivity/list` — 抽奖活动列表 · `LotteryActivityController#list` · 权限 `lotteryManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/lotteryActivity/update` — 编辑抽奖活动 · `LotteryActivityController#update` · 权限 `lotteryActivityModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/lotteryActivity/userManage` — 用户管理列表 · `LotteryActivityController#userManage` · 权限 `lotteryManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/lotteryActivity/userRecord` — 用户中奖详情列表 · `LotteryActivityController#userRecord` · 权限 `lotteryManage` · auth=`jwt+perm`

#### marketing::salesman_admin — 8 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\salesman\SalesmanController.java`
- 目标四层：`crates/core/src/models/marketing/salesman_admin.rs` · `crates/infrastructure/src/db/repo/marketing/salesman_admin.rs` · `crates/services/src/marketing/salesman_admin.rs` · `crates/api/admin/src/routes/marketing/salesman_admin.rs` · `crates/api/public/src/routes/marketing/salesman_admin.rs`
- [ ] `GET /adminapi/salesman/salesman/commissionDetails` — 分销员佣金详情 · `SalesmanController#commissionDetails`
- [ ] `POST /adminapi/salesman/salesman/create` — 创建 · `SalesmanController#create` · 权限 `salesmanModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/salesman/salesman/customerList` — 客户数量接口 · `SalesmanController#customerList`
- [ ] `GET /adminapi/salesman/salesman/detail` — 获取详情 · `SalesmanController#detail`
- [ ] `GET /adminapi/salesman/salesman/ranking` — 分销员排行 · `SalesmanController#ranking`
- [ ] `GET /adminapi/salesman/salesman/salesmanList` — 分销员下拉列表 · `SalesmanController#salesmanList`
- [ ] `GET /adminapi/salesman/salesman/statisticalDetails` — 分销员明细 · `SalesmanController#statisticalDetails`
- [ ] `POST /adminapi/salesman/salesman/update` — 更新 · `SalesmanController#update` · 权限 `salesmanModifyManage` · auth=`jwt+perm`

#### marketing::salesman_product — 8 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\salesman\SalesmanProductController.java`；`tigshop-api\src\main\java\com\tigshop\api\controller\salesman\SalesmanProductController.java`
- 目标四层：`crates/core/src/models/marketing/salesman_product.rs` · `crates/infrastructure/src/db/repo/marketing/salesman_product.rs` · `crates/services/src/marketing/salesman_product.rs` · `crates/api/admin/src/routes/marketing/salesman_product.rs` · `crates/api/public/src/routes/marketing/salesman_product.rs`
- [ ] `GET /adminapi/salesman/product/analysis` — 商品成交分析 · `SalesmanProductController#analysis`
- [ ] `GET /adminapi/salesman/product/config` — 配置 · `SalesmanProductController#config`
- [ ] `POST /adminapi/salesman/product/create` — 创建 · `SalesmanProductController#create` · 权限 `salesmanProductModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/salesman/product/detail` — 获取详情 · `SalesmanProductController#detail`
- [ ] `GET /adminapi/salesman/product/list` — 获取列表 · `SalesmanProductController#list`
- [ ] `POST /adminapi/salesman/product/update` — 更新 · `SalesmanProductController#update` · 权限 `salesmanProductModifyManage` · auth=`jwt+perm`
- [ ] `GET /api/salesman/product/detail` — 客户端分销商品详情 · `SalesmanProductController#detail` · auth=`optional`
- [ ] `GET /api/salesman/product/list` — 获取列表 · `SalesmanProductController#list` · auth=`optional`

#### marketing::points_exchange — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\promotion\PointsExchangeController.java`
- 目标四层：`crates/core/src/models/marketing/points_exchange.rs` · `crates/infrastructure/src/db/repo/marketing/points_exchange.rs` · `crates/services/src/marketing/points_exchange.rs` · `crates/api/admin/src/routes/marketing/points_exchange.rs` · `crates/api/public/src/routes/marketing/points_exchange.rs`
- [ ] `POST /adminapi/promotion/pointsExchange/batch` — 批量操作 · `PointsExchangeController#batch` · 权限 `promotionPointsExchangeModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/pointsExchange/create` — 创建 · `PointsExchangeController#create` · 权限 `promotionPointsExchangeModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/pointsExchange/del` — 删除 · `PointsExchangeController#del` · 权限 `promotionPointsExchangeModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/pointsExchange/detail` — 获取详情 · `PointsExchangeController#detail` · 权限 `pointsExchangeManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/pointsExchange/list` — 获取列表 · `PointsExchangeController#list` · 权限 `pointsExchangeManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/pointsExchange/update` — 更新 · `PointsExchangeController#update` · 权限 `promotionPointsExchangeModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/pointsExchange/updateField` — 更新字段 · `PointsExchangeController#updateField` · 权限 `promotionPointsExchangeModifyManage` · auth=`jwt+perm`

#### marketing::product_gift — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\promotion\ProductGiftController.java`
- 目标四层：`crates/core/src/models/marketing/product_gift.rs` · `crates/infrastructure/src/db/repo/marketing/product_gift.rs` · `crates/services/src/marketing/product_gift.rs` · `crates/api/admin/src/routes/marketing/product_gift.rs` · `crates/api/public/src/routes/marketing/product_gift.rs`
- [ ] `POST /adminapi/promotion/productGift/batch` — 批量操作 · `ProductGiftController#batch` · 权限 `productGiftModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/productGift/create` — 创建 · `ProductGiftController#create` · 权限 `productGiftModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/productGift/del` — 删除 · `ProductGiftController#del` · 权限 `productGiftModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/productGift/detail` — 获取详情 · `ProductGiftController#detail` · 权限 `productGiftManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/productGift/list` — 获取列表 · `ProductGiftController#list` · 权限 `productGiftManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/productGift/update` — 更新 · `ProductGiftController#update` · 权限 `productGiftModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/productGift/updateField` — 更新字段 · `ProductGiftController#updateField` · 权限 `productGiftModifyManage` · auth=`jwt+perm`

#### marketing::recharge_setting — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\promotion\RechargeSettingController.java`
- 目标四层：`crates/core/src/models/marketing/recharge_setting.rs` · `crates/infrastructure/src/db/repo/marketing/recharge_setting.rs` · `crates/services/src/marketing/recharge_setting.rs` · `crates/api/admin/src/routes/marketing/recharge_setting.rs` · `crates/api/public/src/routes/marketing/recharge_setting.rs`
- [ ] `POST /adminapi/promotion/rechargeSetting/batch` — 批量操作 · `RechargeSettingController#batch` · 权限 `rechargeSettingModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/rechargeSetting/create` — 创建 · `RechargeSettingController#create` · 权限 `rechargeSettingModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/rechargeSetting/del` — 删除 · `RechargeSettingController#del` · 权限 `rechargeSettingModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/rechargeSetting/detail` — 获取详情 · `RechargeSettingController#detail` · 权限 `rechargeSettingManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/rechargeSetting/list` — 获取列表 · `RechargeSettingController#list` · 权限 `rechargeSettingManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/rechargeSetting/update` — 更新 · `RechargeSettingController#update` · 权限 `rechargeSettingModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/rechargeSetting/updateField` — 更新字段 · `RechargeSettingController#updateField` · 权限 `rechargeSettingModifyManage` · auth=`jwt+perm`

#### marketing::salesman_material — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\salesman\SalesmanMaterialController.java`
- 目标四层：`crates/core/src/models/marketing/salesman_material.rs` · `crates/infrastructure/src/db/repo/marketing/salesman_material.rs` · `crates/services/src/marketing/salesman_material.rs` · `crates/api/admin/src/routes/marketing/salesman_material.rs` · `crates/api/public/src/routes/marketing/salesman_material.rs`
- [ ] `POST /adminapi/salesman/material/batch` — 批量操作 · `SalesmanMaterialController#batch` · 权限 `salesmanMaterialModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/salesman/material/create` — 创建 · `SalesmanMaterialController#create` · 权限 `salesmanMaterialModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/salesman/material/del` — 删除 · `SalesmanMaterialController#del` · 权限 `salesmanMaterialModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/salesman/material/detail` — 获取详情 · `SalesmanMaterialController#detail`
- [ ] `GET /adminapi/salesman/material/list` — 获取列表 · `SalesmanMaterialController#list`
- [ ] `POST /adminapi/salesman/material/update` — 更新 · `SalesmanMaterialController#update` · 权限 `salesmanMaterialModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/salesman/material/updateField` — 更新字段 · `SalesmanMaterialController#updateField` · 权限 `salesmanMaterialModifyManage` · auth=`jwt+perm`

#### marketing::sign_in_setting — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\promotion\SignInSettingController.java`
- 目标四层：`crates/core/src/models/marketing/sign_in_setting.rs` · `crates/infrastructure/src/db/repo/marketing/sign_in_setting.rs` · `crates/services/src/marketing/sign_in_setting.rs` · `crates/api/admin/src/routes/marketing/sign_in_setting.rs` · `crates/api/public/src/routes/marketing/sign_in_setting.rs`
- [ ] `POST /adminapi/promotion/signInSetting/batch` — 批量操作签到配置 · `SignInSettingController#batch` · 权限 `signInSettingModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/signInSetting/create` — 创建 · `SignInSettingController#create` · 权限 `signInSettingModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/signInSetting/del` — 删除签到配置 · `SignInSettingController#del` · 权限 `signInSettingModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/signInSetting/detail` — 获取详情 · `SignInSettingController#detail` · 权限 `signInSettingManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/signInSetting/list` — 获取列表 · `SignInSettingController#list` · 权限 `signInSettingManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/signInSetting/update` — 更新 · `SignInSettingController#update` · 权限 `signInSettingModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/signInSetting/updateField` — 更新字段 · `SignInSettingController#updateField` · 权限 `signInSettingModifyManage` · auth=`jwt+perm`

#### marketing::gift_card_client — 6 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\promotion\GiftCardGroupsController.java`；`tigshop-api\src\main\java\com\tigshop\api\controller\promotion\GiftCardGroupsController.java`
- 目标四层：`crates/core/src/models/marketing/gift_card_client.rs` · `crates/infrastructure/src/db/repo/marketing/gift_card_client.rs` · `crates/services/src/marketing/gift_card_client.rs` · `crates/api/admin/src/routes/marketing/gift_card_client.rs` · `crates/api/public/src/routes/marketing/gift_card_client.rs`
- [ ] `POST /api/promotion/giftCardGroups/claim` — 领取礼品卡 · `GiftCardGroupsController#claim` · auth=`optional`
- [ ] `GET /api/promotion/giftCardGroups/detail` — 礼品卡详情 · `GiftCardGroupsController#detail` · auth=`optional`
- [ ] `POST /api/promotion/giftCardGroups/exchange` — 兑换礼品卡 · `GiftCardGroupsController#exchange` · auth=`optional`
- [ ] `GET /api/promotion/giftCardGroups/getTransferLink` — 获取转赠链接 · `GiftCardGroupsController#getTransferLink` · auth=`optional`
- [ ] `GET /api/promotion/giftCardGroups/isGiftCardExpired` — 礼品卡是否过期 · `GiftCardGroupsController#isGiftCardExpired` · auth=`optional`
- [ ] `POST /api/promotion/giftCardGroups/transfer` — 转赠礼品卡 · `GiftCardGroupsController#transfer` · auth=`optional`

#### marketing::groupon_promotion_record — 5 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\promotion\GrouponPromotionRecordController.java`
- 目标四层：`crates/core/src/models/marketing/groupon_promotion_record.rs` · `crates/infrastructure/src/db/repo/marketing/groupon_promotion_record.rs` · `crates/services/src/marketing/groupon_promotion_record.rs` · `crates/api/admin/src/routes/marketing/groupon_promotion_record.rs` · `crates/api/public/src/routes/marketing/groupon_promotion_record.rs`
- [ ] `POST /adminapi/promotion/groupon/record/batchGroup` — 批量手动成团 · `GrouponPromotionRecordController#batchManualGroup` · 权限 `grouponPromotionManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/groupon/record/groupMemberList` — 团员列表 · `GrouponPromotionRecordController#groupMemberList` · 权限 `grouponPromotionManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/groupon/record/manualGroup` — 手动成团 · `GrouponPromotionRecordController#manualGroup` · 权限 `grouponPromotionManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/groupon/record/openJoinList` — 开团记录 · `GrouponPromotionRecordController#openJoinList` · 权限 `grouponPromotionManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/groupon/record/orderList` — 订单记录 · `GrouponPromotionRecordController#orderList` · 权限 `grouponPromotionManage` · auth=`jwt+perm`

#### marketing::lottery — 5 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\promotion\LotteryController.java`
- 目标四层：`crates/core/src/models/marketing/lottery.rs` · `crates/infrastructure/src/db/repo/marketing/lottery.rs` · `crates/services/src/marketing/lottery.rs` · `crates/api/admin/src/routes/marketing/lottery.rs` · `crates/api/public/src/routes/marketing/lottery.rs`
- [ ] `POST /api/promotion/lottery/confirmReceive` — 确认领取商品奖品 · `LotteryController#confirmReceive` · auth=`optional`
- [ ] `POST /api/promotion/lottery/draw` — 执行抽奖 · `LotteryController#draw` · auth=`optional`
- [ ] `GET /api/promotion/lottery/info` — 获取抽奖活动信息 · `LotteryController#info` · auth=`optional`
- [ ] `GET /api/promotion/lottery/myRecords` — 我的中奖记录 · `LotteryController#myRecords` · auth=`optional`
- [ ] `GET /api/promotion/lottery/winners` — 获取中奖名单 · `LotteryController#winners` · auth=`optional`

#### marketing::salesman_client — 5 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\salesman\SalesmanController.java`
- 目标四层：`crates/core/src/models/marketing/salesman_client.rs` · `crates/infrastructure/src/db/repo/marketing/salesman_client.rs` · `crates/services/src/marketing/salesman_client.rs` · `crates/api/admin/src/routes/marketing/salesman_client.rs` · `crates/api/public/src/routes/marketing/salesman_client.rs`
- [ ] `GET /api/salesman/salesman/customerDetail` — 获取我的客户详情 · `SalesmanController#customerDetail` · auth=`optional`
- [ ] `GET /api/salesman/salesman/customerList` — 获取我的客户列表 · `SalesmanController#customerList` · auth=`optional`
- [ ] `POST /api/salesman/salesman/customerRemark` — 更新客户备注 · `SalesmanController#customerRemark` · auth=`optional`
- [ ] `GET /api/salesman/salesman/detail` — 获取详情 · `SalesmanController#detail` · auth=`optional`
- [ ] `GET /api/salesman/salesman/userInfo` — 获取分销员用户基础信息 · `SalesmanController#getUserInfo` · auth=`optional`

#### marketing::salesman_group — 5 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\salesman\SalesmanGroupController.java`
- 目标四层：`crates/core/src/models/marketing/salesman_group.rs` · `crates/infrastructure/src/db/repo/marketing/salesman_group.rs` · `crates/services/src/marketing/salesman_group.rs` · `crates/api/admin/src/routes/marketing/salesman_group.rs` · `crates/api/public/src/routes/marketing/salesman_group.rs`
- [ ] `POST /adminapi/salesman/group/create` — 创建 · `SalesmanGroupController#create` · 权限 `salesmanGroupModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/salesman/group/del` — 删除 · `SalesmanGroupController#del` · 权限 `salesmanGroupModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/salesman/group/detail` — 获取详情 · `SalesmanGroupController#detail`
- [ ] `GET /adminapi/salesman/group/list` — 获取列表 · `SalesmanGroupController#list`
- [ ] `POST /adminapi/salesman/group/update` — 更新 · `SalesmanGroupController#update` · 权限 `salesmanGroupModifyManage` · auth=`jwt+perm`

#### marketing::promotion — 4 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\promotion\PromotionController.java`
- 目标四层：`crates/core/src/models/marketing/promotion.rs` · `crates/infrastructure/src/db/repo/marketing/promotion.rs` · `crates/services/src/marketing/promotion.rs` · `crates/api/admin/src/routes/marketing/promotion.rs` · `crates/api/public/src/routes/marketing/promotion.rs`
- [ ] `POST /adminapi/promotion/promotion/create` — 创建 · `PromotionController#create` · 权限 `promotionManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/promotion/getPromotionCount` — 获取活动数量 · `PromotionController#getPromotionCount` · 权限 `promotionManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/promotion/list` — 获取列表 · `PromotionController#list` · 权限 `promotionManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/promotion/promotion/update` — 更新 · `PromotionController#update` · 权限 `promotionManage` · auth=`jwt+perm`

#### marketing::salesman_order — 4 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\salesman\SalesmanOrderController.java`；`tigshop-api\src\main\java\com\tigshop\api\controller\salesman\SalesmanOrderController.java`
- 目标四层：`crates/core/src/models/marketing/salesman_order.rs` · `crates/infrastructure/src/db/repo/marketing/salesman_order.rs` · `crates/services/src/marketing/salesman_order.rs` · `crates/api/admin/src/routes/marketing/salesman_order.rs` · `crates/api/public/src/routes/marketing/salesman_order.rs`
- [ ] `POST /adminapi/salesman/order/batch` — 批量操作 · `SalesmanOrderController#batch` · 权限 `salesmanOrderBatch` · auth=`jwt+perm`
- [ ] `GET /adminapi/salesman/order/list` — 获取列表 · `SalesmanOrderController#list`
- [ ] `POST /adminapi/salesman/order/settlement` — 人工结算 · `SalesmanOrderController#settlement` · 权限 `salesmanSettlementManage` · auth=`jwt+perm`
- [ ] `GET /api/salesman/order/list` — 获取列表 · `SalesmanOrderController#clientList` · auth=`optional`

#### marketing::salesman_material_client — 3 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\salesman\SalesmanMaterialController.java`
- 目标四层：`crates/core/src/models/marketing/salesman_material_client.rs` · `crates/infrastructure/src/db/repo/marketing/salesman_material_client.rs` · `crates/services/src/marketing/salesman_material_client.rs` · `crates/api/admin/src/routes/marketing/salesman_material_client.rs` · `crates/api/public/src/routes/marketing/salesman_material_client.rs`
- [ ] `GET /api/salesman/material/category` — 获取分类 · `SalesmanMaterialController#category` · auth=`optional`
- [ ] `GET /api/salesman/material/detail` — 获取详情 · `SalesmanMaterialController#detail` · auth=`optional`
- [ ] `GET /api/salesman/material/list` — 获取列表 · `SalesmanMaterialController#apiPage` · auth=`optional`

#### marketing::seckill_batch — 3 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\promotion\SeckillController.java`
- 目标四层：`crates/core/src/models/marketing/seckill_batch.rs` · `crates/infrastructure/src/db/repo/marketing/seckill_batch.rs` · `crates/services/src/marketing/seckill_batch.rs` · `crates/api/admin/src/routes/marketing/seckill_batch.rs` · `crates/api/public/src/routes/marketing/seckill_batch.rs`
- [ ] `POST /adminapi/promotion/seckill/batch` — 批量操作 · `SeckillController#batch` · 权限 `seckillModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/seckill/listForDecorate` — 获取秒杀商品列表 · `SeckillController#listForDecorate` · 权限 `seckillManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/promotion/seckill/listForHomePreview` — 装修预览：秒杀商品列表（与 C 端首页同口径） · `SeckillController#listForHomePreview` · 权限 `seckillManage` · auth=`jwt+perm`

#### marketing::groupon_records — 2 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\promotion\GrouponRecordsController.java`
- 目标四层：`crates/core/src/models/marketing/groupon_records.rs` · `crates/infrastructure/src/db/repo/marketing/groupon_records.rs` · `crates/services/src/marketing/groupon_records.rs` · `crates/api/admin/src/routes/marketing/groupon_records.rs` · `crates/api/public/src/routes/marketing/groupon_records.rs`
- [ ] `GET /api/promotion/groupon/record/details` — 鎷煎洟娲诲姩璇︽儏 · `GrouponRecordsController#clientDetails` · auth=`optional`
- [ ] `GET /api/promotion/groupon/record/list` — 鎷煎洟娲诲姩璁板綍鍒楄〃 · `GrouponRecordsController#clientList` · auth=`optional`

#### marketing::overview — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\salesman\OverviewController.java`
- 目标四层：`crates/core/src/models/marketing/overview.rs` · `crates/infrastructure/src/db/repo/marketing/overview.rs` · `crates/services/src/marketing/overview.rs` · `crates/api/admin/src/routes/marketing/overview.rs` · `crates/api/public/src/routes/marketing/overview.rs`
- [ ] `GET /adminapi/salesman/overview/coreSummary` — 获取详情 · `OverviewController#coreSummary`
- [ ] `GET /adminapi/salesman/overview/coreTrend` — 获取列表 · `OverviewController#coreTrend`

#### marketing::salesman_config — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\salesman\SalesmanConfigController.java`
- 目标四层：`crates/core/src/models/marketing/salesman_config.rs` · `crates/infrastructure/src/db/repo/marketing/salesman_config.rs` · `crates/services/src/marketing/salesman_config.rs` · `crates/api/admin/src/routes/marketing/salesman_config.rs` · `crates/api/public/src/routes/marketing/salesman_config.rs`
- [ ] `GET /adminapi/salesman/config/detail` — 获取详情 · `SalesmanConfigController#detail`
- [ ] `POST /adminapi/salesman/config/save` — 更新 · `SalesmanConfigController#save` · 权限 `salesmanConfigModifyManage` · auth=`jwt+perm`

#### marketing::salesman_card_invite — 1 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\salesman\SalesmanCardInviteController.java`
- 目标四层：`crates/core/src/models/marketing/salesman_card_invite.rs` · `crates/infrastructure/src/db/repo/marketing/salesman_card_invite.rs` · `crates/services/src/marketing/salesman_card_invite.rs` · `crates/api/admin/src/routes/marketing/salesman_card_invite.rs` · `crates/api/public/src/routes/marketing/salesman_card_invite.rs`
- [ ] `POST /api/salesman/salesman/acceptCardInvite` — 接受名片邀请，一键成为下级分销员 · `SalesmanCardInviteController#acceptCardInvite` · auth=`optional`

#### marketing::salesman_customer_transaction — 1 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\salesman\SalesmanCustomerTransactionController.java`
- 目标四层：`crates/core/src/models/marketing/salesman_customer_transaction.rs` · `crates/infrastructure/src/db/repo/marketing/salesman_customer_transaction.rs` · `crates/services/src/marketing/salesman_customer_transaction.rs` · `crates/api/admin/src/routes/marketing/salesman_customer_transaction.rs` · `crates/api/public/src/routes/marketing/salesman_customer_transaction.rs`
- [ ] `GET /adminapi/salesman/customerTransaction/list` — 获取列表 · `SalesmanCustomerTransactionController#listCustomerTransaction`

#### marketing::time_discount_batch — 1 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\promotion\TimeDiscountController.java`
- 目标四层：`crates/core/src/models/marketing/time_discount_batch.rs` · `crates/infrastructure/src/db/repo/marketing/time_discount_batch.rs` · `crates/services/src/marketing/time_discount_batch.rs` · `crates/api/admin/src/routes/marketing/time_discount_batch.rs` · `crates/api/public/src/routes/marketing/time_discount_batch.rs`
- [ ] `POST /adminapi/promotion/timeDiscount/batch` — 批量操作 · `TimeDiscountController#batch` · 权限 `timeDiscountModifyManage` · auth=`jwt+perm`

### A.11 `content`

#### content::home — 16 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\home\HomeController.java`
- 目标四层：`crates/core/src/models/content/home.rs` · `crates/infrastructure/src/db/repo/content/home.rs` · `crates/services/src/content/home.rs` · `crates/api/admin/src/routes/content/home.rs` · `crates/api/public/src/routes/content/home.rs`
- [ ] `GET /api/home/home/friendLinks` — 友情链接 · `HomeController#friendLinks` · auth=`optional`
- [ ] `GET /api/home/home/getCoupon` — 首页优惠券 · `HomeController#getCoupon` · auth=`optional`
- [ ] `GET /api/home/home/getCustomerServiceConfig` — 获取前台客户配置 · `HomeController#getConfigCustomer` · auth=`optional`
- [ ] `GET /api/home/home/getRecommend` — 首页 · `HomeController#getRecommend` · auth=`optional`
- [ ] `GET /api/home/home/getSeckill` — 获取秒杀 · `HomeController#getSeckill` · auth=`optional`
- [ ] `GET /api/home/home/h5Category` — H5分类页装修 · `HomeController#h5Category` · auth=`optional`
- [ ] `GET /api/home/home/h5Member` — H5个人中心装修 · `HomeController#h5Member` · auth=`optional`
- [ ] `GET /api/home/home/h5ProductDetail` — H5商品详情页装修 · `HomeController#h5ProductDetail` · auth=`optional`
- [ ] `GET /api/home/home/h5Search` — 移动端搜索页装修 · `HomeController#h5Search` · auth=`optional`
- [ ] `GET /api/home/home/memberDecorate` — 个人中心 · `HomeController#memberDecorate` · auth=`optional`
- [ ] `GET /api/home/home/mobileLogin` — 移动端登录页装修 · `HomeController#mobileLogin` · auth=`anonymous?`
- [ ] `GET /api/home/home/mobileNav` — 移动端导航 · `HomeController#mobileNav` · auth=`optional`
- [ ] `GET /api/home/home/o2oIndex` — 门店首页装修 · `HomeController#o2oIndex` · auth=`optional`
- [ ] `GET /api/home/home/pcHeader` — PC头部装修配置 · `HomeController#pcHeader` · auth=`optional`
- [ ] `GET /api/home/home/pcLogin` — PC登录页装修 · `HomeController#pcLogin` · auth=`anonymous?`
- [ ] `GET /api/home/home/pcSearch` — PC搜索页装修 · `HomeController#pcSearch` · auth=`optional`

#### content::wechat_live — 14 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\content\WechatLiveController.java`
- 目标四层：`crates/core/src/models/content/wechat_live.rs` · `crates/infrastructure/src/db/repo/content/wechat_live.rs` · `crates/services/src/content/wechat_live.rs` · `crates/api/admin/src/routes/content/wechat_live.rs` · `crates/api/public/src/routes/content/wechat_live.rs`
- [ ] `POST /adminapi/content/wechatLive/addToRoom` — 添加已导入商品到直播间 · `WechatLiveController#addGoods` · 权限 `wechatLiveModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/content/wechatLive/create` — 创建直播间 · `WechatLiveController#create` · 权限 `wechatLiveModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/content/wechatLive/delete` — 删除直播间 · `WechatLiveController#delete` · 权限 `wechatLiveModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/content/wechatLive/detail` — 获取直播间详情 · `WechatLiveController#detail` · 权限 `wechatLive` · auth=`jwt+perm`
- [ ] `GET /adminapi/content/wechatLive/finderLiveRecord` — 获取视频号当前的直播记录 · `WechatLiveController#getFinderLiveRecord` · 权限 `wechatLive` · auth=`jwt+perm`
- [ ] `GET /adminapi/content/wechatLive/list` — 获取直播间列表 · `WechatLiveController#list` · 权限 `wechatLive` · auth=`jwt+perm`
- [ ] `GET /adminapi/content/wechatLive/promote/{roomId}` — 获取直播间推广信息 · `WechatLiveController#getPromoteData` · 权限 `wechatLive` · auth=`jwt+perm`
- [ ] `GET /adminapi/content/wechatLive/pushUrl/{roomId}` — 获取推流地址 · `WechatLiveController#getPushUrl` · 权限 `wechatLive` · auth=`jwt+perm`
- [ ] `GET /adminapi/content/wechatLive/refresh` — 刷新直播间信息 · `WechatLiveController#refresh` · 权限 `wechatLive` · auth=`jwt+perm`
- [ ] `POST /adminapi/content/wechatLive/removeFromRoom` — 从直播间移除商品 · `WechatLiveController#removeGoods` · 权限 `wechatLiveModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/content/wechatLive/room/{roomId}` — 获取直播间商品列表 · `WechatLiveController#getGoods` · 权限 `wechatLive` · auth=`jwt+perm`
- [ ] `GET /adminapi/content/wechatLive/syncFromWechat` — 从微信同步直播间 · `WechatLiveController#syncFromWechat` · 权限 `wechatLive` · auth=`jwt+perm`
- [ ] `POST /adminapi/content/wechatLive/syncToWechat` — 同步到微信 · `WechatLiveController#syncToWechat` · 权限 `wechatLiveModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/content/wechatLive/update` — 更新直播间 · `WechatLiveController#update` · 权限 `wechatLiveModifyManage` · auth=`jwt+perm`

#### content::gallery_pic — 10 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\GalleryPicController.java`
- 目标四层：`crates/core/src/models/content/gallery_pic.rs` · `crates/infrastructure/src/db/repo/content/gallery_pic.rs` · `crates/services/src/content/gallery_pic.rs` · `crates/api/admin/src/routes/content/gallery_pic.rs` · `crates/api/public/src/routes/content/gallery_pic.rs`
- [ ] `POST /adminapi/setting/galleryPic/batchDel` — 批量删除素材 · `GalleryPicController#batchDel` · 权限 `galleryPicModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/galleryPic/batchDownload` — 批量下载素材 · `GalleryPicController#batchDownload` · 权限 `galleryPicManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/galleryPic/createFromExternal` — 通过外部链接创建图片 · `GalleryPicController#createFromExternal` · 权限 `galleryPicManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/galleryPic/del` — 删除相册图片 · `GalleryPicController#del` · 权限 `galleryPicModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/galleryPic/detail` — 图片详情 · `GalleryPicController#detail` · 权限 `galleryPicManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/galleryPic/list` — 相册图片列表 · `GalleryPicController#list` · 权限 `decorate,galleryPicManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/galleryPic/sort` — 图片手动排序 · `GalleryPicController#sort` · 权限 `galleryPicModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/galleryPic/transfer` — 批量移动素材 · `GalleryPicController#transfer` · 权限 `galleryPicModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/galleryPic/updateField` — 更新权限字段 · `GalleryPicController#updateField` · 权限 `galleryPicModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/galleryPic/uploadImg` — 上传图片 · `GalleryPicController#uploadImg` · 权限 `galleryPicManage` · auth=`jwt+perm`

#### content::gallery — 9 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\GalleryController.java`
- 目标四层：`crates/core/src/models/content/gallery.rs` · `crates/infrastructure/src/db/repo/content/gallery.rs` · `crates/services/src/content/gallery.rs` · `crates/api/admin/src/routes/content/gallery.rs` · `crates/api/public/src/routes/content/gallery.rs`
- [ ] `POST /adminapi/setting/gallery/batch` — 批量操作相册 · `GalleryController#batch` · 权限 `galleryModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/gallery/create` — 添加相册 · `GalleryController#create` · 权限 `galleryModifyManage,decorateModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/gallery/del` — 删除相册 · `GalleryController#del` · 权限 `galleryModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/gallery/detail` — 相册详情 · `GalleryController#detail` · 权限 `galleryManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/gallery/list` — 相册列表 · `GalleryController#list` · 权限 `decorate,galleryManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/gallery/move` — 分组转移/素材移动 · `GalleryController#move` · 权限 `galleryModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/gallery/sort` — 分组排序 · `GalleryController#sort` · 权限 `galleryModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/gallery/tree` — 统一分组树 · `GalleryController#tree` · 权限 `decorate,galleryManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/gallery/update` — 修改相册 · `GalleryController#update` · 权限 `galleryModifyManage` · auth=`jwt+perm`

#### content::friend_links — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\FriendLinksController.java`
- 目标四层：`crates/core/src/models/content/friend_links.rs` · `crates/infrastructure/src/db/repo/content/friend_links.rs` · `crates/services/src/content/friend_links.rs` · `crates/api/admin/src/routes/content/friend_links.rs` · `crates/api/public/src/routes/content/friend_links.rs`
- [ ] `POST /adminapi/setting/friendLinks/batch` — 批量操作 · `FriendLinksController#batch` · 权限 `friendLinksBatchManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/friendLinks/create` — 创建 · `FriendLinksController#create` · 权限 `friendLinksUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/friendLinks/del` — 删除 · `FriendLinksController#del` · 权限 `friendLinksDelManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/friendLinks/detail` — 获取详情 · `FriendLinksController#detail` · 权限 `friendLinksManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/friendLinks/list` — 获取列表 · `FriendLinksController#list` · 权限 `friendLinksManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/friendLinks/update` — 更新 · `FriendLinksController#update` · 权限 `friendLinksUpdateManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/friendLinks/updateField` — 更新字段 · `FriendLinksController#updateField` · 权限 `friendLinksUpdateManage` · auth=`jwt+perm`

#### content::gallery_video — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\GalleryVideoController.java`
- 目标四层：`crates/core/src/models/content/gallery_video.rs` · `crates/infrastructure/src/db/repo/content/gallery_video.rs` · `crates/services/src/content/gallery_video.rs` · `crates/api/admin/src/routes/content/gallery_video.rs` · `crates/api/public/src/routes/content/gallery_video.rs`
- [ ] `POST /adminapi/setting/galleryVideo/batch` — 鎵归噺鎿嶄綔鐩稿唽 · `GalleryVideoController#batch` · 权限 `galleryVideoManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/galleryVideo/create` — 娣诲姞瑙嗛鐩稿唽 · `GalleryVideoController#create` · 权限 `galleryVideoManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/galleryVideo/del` — 鍒犻櫎瑙嗛鐩稿唽 · `GalleryVideoController#del` · 权限 `galleryVideoManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/galleryVideo/detail` — 瑙嗛鐩稿唽璇︽儏 · `GalleryVideoController#detail` · 权限 `galleryVideoManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/galleryVideo/getAllCategory` — 瑙嗛鐩稿唽鏍戝舰鍒楄〃 · `GalleryVideoController#treeList` · 权限 `productManage,galleryVideoManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/galleryVideo/list` — 瑙嗛鐩稿唽鍒楄〃 · `GalleryVideoController#list` · 权限 `productManage,galleryVideoManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/galleryVideo/update` — 淇敼瑙嗛鐩稿唽 · `GalleryVideoController#update` · 权限 `galleryVideoManage` · auth=`jwt+perm`

#### content::gallery_video_info — 6 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\GalleryVideoInfoController.java`
- 目标四层：`crates/core/src/models/content/gallery_video_info.rs` · `crates/infrastructure/src/db/repo/content/gallery_video_info.rs` · `crates/services/src/content/gallery_video_info.rs` · `crates/api/admin/src/routes/content/gallery_video_info.rs` · `crates/api/public/src/routes/content/gallery_video_info.rs`
- [ ] `POST /adminapi/setting/galleryVideoInfo/create` — 淇濆瓨瑙嗛淇℃伅 · `GalleryVideoInfoController#save` · 权限 `galleryVideoManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/galleryVideoInfo/del` — 鍒犻櫎鐩稿唽瑙嗛 · `GalleryVideoInfoController#del` · 权限 `galleryVideoManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/galleryVideoInfo/detail` — 瑙嗛淇℃伅璇︽儏 · `GalleryVideoInfoController#detail` · 权限 `productManage,galleryVideoManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/setting/galleryVideoInfo/list` — 鐩稿唽瑙嗛鍒楄〃 · `GalleryVideoInfoController#list` · 权限 `productManage,galleryVideoManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/galleryVideoInfo/update` — 淇敼瑙嗛淇℃伅 · `GalleryVideoInfoController#update` · 权限 `galleryVideoManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/setting/galleryVideoInfo/uploadVideo` — 涓婁紶瑙嗛 · `GalleryVideoInfoController#uploadVideo` · 权限 `galleryVideoManage` · auth=`jwt+perm`

#### content::wechat_auto_reply — 6 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\wechat\WechatAutoReplyController.java`
- 目标四层：`crates/core/src/models/content/wechat_auto_reply.rs` · `crates/infrastructure/src/db/repo/content/wechat_auto_reply.rs` · `crates/services/src/content/wechat_auto_reply.rs` · `crates/api/admin/src/routes/content/wechat_auto_reply.rs` · `crates/api/public/src/routes/content/wechat_auto_reply.rs`
- [ ] `GET /adminapi/wechat/autoReply/default` — 获取默认自动回复 · `WechatAutoReplyController#getDefault` · 权限 `wechatAutoReply` · auth=`jwt+perm`
- [ ] `POST /adminapi/wechat/autoReply/default/save` — 保存默认自动回复 · `WechatAutoReplyController#saveDefault` · 权限 `wechatAutoReplyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/wechat/autoReply/loginSuccess` — 获取扫码登录成功回复 · `WechatAutoReplyController#getLoginSuccess` · 权限 `wechatAutoReply` · auth=`anonymous`
- [ ] `POST /adminapi/wechat/autoReply/loginSuccess/save` — 保存扫码登录成功回复 · `WechatAutoReplyController#saveLoginSuccess` · 权限 `wechatAutoReplyManage` · auth=`anonymous`
- [ ] `GET /adminapi/wechat/autoReply/subscribe` — 获取关注后自动回复 · `WechatAutoReplyController#getSubscribe` · 权限 `wechatAutoReply` · auth=`jwt+perm`
- [ ] `POST /adminapi/wechat/autoReply/subscribe/save` — 保存关注后自动回复 · `WechatAutoReplyController#saveSubscribe` · 权限 `wechatAutoReplyManage` · auth=`jwt+perm`

#### content::wechat_keyword_rule — 5 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\wechat\WechatKeywordRuleController.java`
- 目标四层：`crates/core/src/models/content/wechat_keyword_rule.rs` · `crates/infrastructure/src/db/repo/content/wechat_keyword_rule.rs` · `crates/services/src/content/wechat_keyword_rule.rs` · `crates/api/admin/src/routes/content/wechat_keyword_rule.rs` · `crates/api/public/src/routes/content/wechat_keyword_rule.rs`
- [ ] `POST /adminapi/wechat/keywordRule/del` — 批量删除规则 · `WechatKeywordRuleController#del` · 权限 `wechatKeywordReplyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/wechat/keywordRule/detail` — 规则详情 · `WechatKeywordRuleController#detail` · 权限 `wechatKeywordReply` · auth=`jwt+perm`
- [ ] `GET /adminapi/wechat/keywordRule/list` — 规则列表 · `WechatKeywordRuleController#list` · 权限 `wechatKeywordReply` · auth=`jwt+perm`
- [ ] `POST /adminapi/wechat/keywordRule/save` — 新增或编辑规则 · `WechatKeywordRuleController#save` · 权限 `wechatKeywordReplyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/wechat/keywordRule/setEnabled` — 批量启停规则 · `WechatKeywordRuleController#setEnabled` · 权限 `wechatKeywordReplyManage` · auth=`jwt+perm`

#### content::wechat_video_account — 5 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\content\WechatVideoAccountController.java`
- 目标四层：`crates/core/src/models/content/wechat_video_account.rs` · `crates/infrastructure/src/db/repo/content/wechat_video_account.rs` · `crates/services/src/content/wechat_video_account.rs` · `crates/api/admin/src/routes/content/wechat_video_account.rs` · `crates/api/public/src/routes/content/wechat_video_account.rs`
- [ ] `POST /adminapi/content/wechatVideoAccount/create` — 新增视频号账号 · `WechatVideoAccountController#create` · 权限 `videoNumberModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/content/wechatVideoAccount/delete` — 删除视频号账号 · `WechatVideoAccountController#delete` · 权限 `videoNumberModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/content/wechatVideoAccount/detail` — 获取视频号账号详情 · `WechatVideoAccountController#detail` · 权限 `videoNumber` · auth=`jwt+perm`
- [ ] `GET /adminapi/content/wechatVideoAccount/list` — 获取视频号账号列表 · `WechatVideoAccountController#list` · 权限 `videoNumber` · auth=`jwt+perm`
- [ ] `POST /adminapi/content/wechatVideoAccount/update` — 更新视频号账号 · `WechatVideoAccountController#update` · 权限 `videoNumberModifyManage` · auth=`jwt+perm`

#### content::article_batch — 3 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\content\ArticleController.java`；`tigshop-api\src\main\java\com\tigshop\api\controller\article\ArticleController.java`
- 目标四层：`crates/core/src/models/content/article_batch.rs` · `crates/infrastructure/src/db/repo/content/article_batch.rs` · `crates/services/src/content/article_batch.rs` · `crates/api/admin/src/routes/content/article_batch.rs` · `crates/api/public/src/routes/content/article_batch.rs`
- [ ] `POST /adminapi/content/article/batch` — 批量操作 · `ArticleController#batch` · 权限 `articleModifyManage` · auth=`jwt+perm`
- [ ] `GET /api/article/article/issueInfo` — 资讯类文章详情 · `ArticleController#issueInfo` · auth=`optional`
- [ ] `GET /api/article/category/indexBzzxList` — 资讯类文章列表，包含文章 · `ArticleController#childrenArticleListWithCategory` · auth=`optional`

#### content::article_category — 3 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\content\ArticleCategoryController.java`
- 目标四层：`crates/core/src/models/content/article_category.rs` · `crates/infrastructure/src/db/repo/content/article_category.rs` · `crates/services/src/content/article_category.rs` · `crates/api/admin/src/routes/content/article_category.rs` · `crates/api/public/src/routes/content/article_category.rs`
- [ ] `POST /adminapi/content/articleCategory/batch` — 批量操作 · `ArticleCategoryController#batch` · 权限 `articleCategoryModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/content/articleCategory/update` — 更新文章分类 · `ArticleCategoryController#update` · 权限 `articleCategoryModifyManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/content/articleCategory/updateField` — 更新文章分类字段 · `ArticleCategoryController#updateField` · 权限 `articleCategoryModifyManage` · auth=`jwt+perm`

#### content::decorate_template — 3 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\decorate\DecorateTemplateController.java`
- 目标四层：`crates/core/src/models/content/decorate_template.rs` · `crates/infrastructure/src/db/repo/content/decorate_template.rs` · `crates/services/src/content/decorate_template.rs` · `crates/api/admin/src/routes/content/decorate_template.rs` · `crates/api/public/src/routes/content/decorate_template.rs`
- [ ] `GET /adminapi/decorate/template/category` — 模板分类树 · `DecorateTemplateController#category`
- [ ] `GET /adminapi/decorate/template/detail` — 模板页面详情 · `DecorateTemplateController#detail`
- [ ] `GET /adminapi/decorate/template/list` — 模板页面列表 · `DecorateTemplateController#list`

#### content::wechat_live_goods — 3 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\content\WechatLiveGoodsController.java`
- 目标四层：`crates/core/src/models/content/wechat_live_goods.rs` · `crates/infrastructure/src/db/repo/content/wechat_live_goods.rs` · `crates/services/src/content/wechat_live_goods.rs` · `crates/api/admin/src/routes/content/wechat_live_goods.rs` · `crates/api/public/src/routes/content/wechat_live_goods.rs`
- [ ] `POST /adminapi/content/wechatLiveGoods/import` — 导入直播商品主表 · `WechatLiveGoodsController#importGoods` · 权限 `wechatLiveProductModifyManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/content/wechatLiveGoods/list` — 获取直播商品主表列表 · `WechatLiveGoodsController#getGoodsLibrary` · 权限 `wechatLiveProduct` · auth=`jwt+perm`
- [ ] `GET /adminapi/content/wechatLiveGoods/query` — 查询直播商品 · `WechatLiveGoodsController#queryGoods` · 权限 `wechatLiveProduct` · auth=`jwt+perm`

#### content::wechat_live_order_sync — 3 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\content\WechatLiveOrderSyncController.java`
- 目标四层：`crates/core/src/models/content/wechat_live_order_sync.rs` · `crates/infrastructure/src/db/repo/content/wechat_live_order_sync.rs` · `crates/services/src/content/wechat_live_order_sync.rs` · `crates/api/admin/src/routes/content/wechat_live_order_sync.rs` · `crates/api/public/src/routes/content/wechat_live_order_sync.rs`
- [ ] `GET /adminapi/content/wechatLiveOrderSync/logs` — 同步日志列表 · `WechatLiveOrderSyncController#logs` · 权限 `wechatLiveManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/content/wechatLiveOrderSync/reconcile` — 触发对账 · `WechatLiveOrderSyncController#reconcile` · 权限 `wechatLiveManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/content/wechatLiveOrderSync/retry/{logId}` — 重试同步 · `WechatLiveOrderSyncController#retry` · 权限 `wechatLiveManage` · auth=`jwt+perm`

#### content::wechat_menu — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\wechat\WechatMenuController.java`
- 目标四层：`crates/core/src/models/content/wechat_menu.rs` · `crates/infrastructure/src/db/repo/content/wechat_menu.rs` · `crates/services/src/content/wechat_menu.rs` · `crates/api/admin/src/routes/content/wechat_menu.rs` · `crates/api/public/src/routes/content/wechat_menu.rs`
- [ ] `GET /adminapi/wechat/menu` — 获取微信菜单详情 · `WechatMenuController#get` · 权限 `wechatMenu` · auth=`jwt+perm`
- [ ] `POST /adminapi/wechat/menu/saveAndPublish` — 保存并发布微信菜单 · `WechatMenuController#saveAndPublish` · 权限 `wechatMenuManage` · auth=`jwt+perm`

#### content::api_wechat_live — 1 条

- Java 源文件：`tigshop-api\src\main\java\com\tigshop\api\controller\content\ApiWechatLiveController.java`
- 目标四层：`crates/core/src/models/content/api_wechat_live.rs` · `crates/infrastructure/src/db/repo/content/api_wechat_live.rs` · `crates/services/src/content/api_wechat_live.rs` · `crates/api/admin/src/routes/content/api_wechat_live.rs` · `crates/api/public/src/routes/content/api_wechat_live.rs`
- [ ] `GET /api/content/wechatVideoAccount/finderLiveRecord` — 获取视频号当前的直播记录 · `ApiWechatLiveController#getFinderLiveRecord` · auth=`optional`

#### content::decorate_promote — 1 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\decorate\DecoratePromoteController.java`
- 目标四层：`crates/core/src/models/content/decorate_promote.rs` · `crates/infrastructure/src/db/repo/content/decorate_promote.rs` · `crates/services/src/content/decorate_promote.rs` · `crates/api/admin/src/routes/content/decorate_promote.rs` · `crates/api/public/src/routes/content/decorate_promote.rs`
- [ ] `POST /adminapi/decorate/promote/generateShortLink` — 根据长链接生成装修推广短链 · `DecoratePromoteController#generateShortLink`

#### content::wechat_kefu_capability — 1 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\setting\wechat\WechatKefuCapabilityController.java`
- 目标四层：`crates/core/src/models/content/wechat_kefu_capability.rs` · `crates/infrastructure/src/db/repo/content/wechat_kefu_capability.rs` · `crates/services/src/content/wechat_kefu_capability.rs` · `crates/api/admin/src/routes/content/wechat_kefu_capability.rs` · `crates/api/public/src/routes/content/wechat_kefu_capability.rs`
- [ ] `GET /adminapi/wechat/keywordRule/capability` — 探测客服消息接口是否可用 · `WechatKefuCapabilityController#capability` · 权限 `wechatKeywordReply` · auth=`jwt+perm`

### A.12 `analytics`

#### analytics::panel — 7 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\panel\PanelController.java`
- 目标四层：`crates/core/src/models/analytics/panel.rs` · `crates/infrastructure/src/db/repo/analytics/panel.rs` · `crates/services/src/analytics/panel.rs` · `crates/api/admin/src/routes/analytics/panel.rs` · `crates/api/public/src/routes/analytics/panel.rs`
- [ ] `GET /adminapi/panel/panel/index` — 首页面板 · `PanelController#index` · 权限 `consoleManage` · auth=`jwt+perm`
- [ ] `POST /adminapi/panel/panel/saveShortcutMenu` — 保存首页快捷功能配置 · `PanelController#saveShortcutMenu`
- [ ] `GET /adminapi/panel/panel/searchMenu` — 一键直达 · `PanelController#searchMenu` · 权限 `consoleManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/panel/panel/shortcutMenu` — 获取首页快捷功能配置 · `PanelController#shortcutMenu`
- [ ] `GET /adminapi/panel/panel/statisticalData` — 首页趋势图表数据（按天数筛选） · `PanelController#statisticalData` · 权限 `consoleManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/panel/panel/vendorIndex` — 首页供应商面板 · `PanelController#vendorIndex` · 权限 `consoleVendorManage` · auth=`jwt+perm`
- [ ] `GET /adminapi/panel/panel/vendorStatisticalData` — 首页供应商收益统计图表数据（按天数筛选） · `PanelController#vendorStatisticalData` · 权限 `consoleVendorManage` · auth=`jwt+perm`

#### analytics::sales_statistics — 5 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\panel\SalesStatisticsController.java`
- 目标四层：`crates/core/src/models/analytics/sales_statistics.rs` · `crates/infrastructure/src/db/repo/analytics/sales_statistics.rs` · `crates/services/src/analytics/sales_statistics.rs` · `crates/api/admin/src/routes/analytics/sales_statistics.rs` · `crates/api/public/src/routes/analytics/sales_statistics.rs`
- [ ] `GET /adminapi/panel/salesStatistics/list` — 销售统计数据 · `SalesStatisticsController#list` · 权限 `statisticsOrder` · auth=`jwt+perm`
- [ ] `GET /adminapi/panel/salesStatistics/salesDetail` — 销售明细 · `SalesStatisticsController#salesDetail` · 权限 `statisticsSale` · auth=`jwt+perm`
- [ ] `GET /adminapi/panel/salesStatistics/salesIndicators` — 销售指标 · `SalesStatisticsController#salesIndicators` · 权限 `saleTargets` · auth=`jwt+perm`
- [ ] `GET /adminapi/panel/salesStatistics/salesProductDetail` — 销售商品明细 · `SalesStatisticsController#salesProductDetail` · 权限 `statisticsSale` · auth=`jwt+perm`
- [ ] `GET /adminapi/panel/salesStatistics/salesRanking` — 销售排行 · `SalesStatisticsController#salesRanking` · 权限 `consumerRanking,statisticsGoods` · auth=`jwt+perm`

### A.13 `im`

#### im::im_conversation — 23 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\im\ImConversationController.java`；`tigshop-api\src\main\java\com\tigshop\api\controller\im\ImConversationController.java`
- 目标四层：`crates/core/src/models/im/im_conversation.rs` · `crates/infrastructure/src/db/repo/im/im_conversation.rs` · `crates/services/src/im/im_conversation.rs` · `crates/api/admin/src/routes/im/im_conversation.rs` · `crates/api/public/src/routes/im/im_conversation.rs`
- [ ] `POST /im/conversation/conversation/batch` — 批量操作 · `ImConversationController#batch`
- [ ] `GET /im/conversation/conversation/consultHistory` — 获取历史会话 · `ImConversationController#consultHistory`
- [ ] `POST /im/conversation/conversation/create` — 创建 · `ImConversationController#create`
- [ ] `POST /im/conversation/conversation/del` — 删除 · `ImConversationController#del`
- [ ] `GET /im/conversation/conversation/detail` — 获取详情 · `ImConversationController#detail`
- [ ] `GET /im/conversation/conversation/list` — 获取列表 · `ImConversationController#list`
- [ ] `POST /im/conversation/conversation/saveRemark` — 保存备注 · `ImConversationController#saveRemark`
- [ ] `GET /im/conversation/conversation/search` — 搜索 · `ImConversationController#search`
- [ ] `POST /im/conversation/conversation/transfer` — 转接 · `ImConversationController#transfer`
- [ ] `POST /im/conversation/conversation/updateField` — 更新字段 · `ImConversationController#updateField`
- [ ] `GET /im/conversation/conversation/waitServantList` — 获取待接入的客户列表 · `ImConversationController#waitServantList`
- [ ] `POST /im/conversation/conversation/batch` — 批量操作 · `ImConversationController#batch` · auth=`optional`
- [ ] `GET /im/conversation/conversation/consultHistory` — 获取历史会话 · `ImConversationController#consultHistory` · auth=`optional`
- [ ] `POST /im/conversation/conversation/create` — 创建 · `ImConversationController#create` · auth=`optional`
- [ ] `POST /im/conversation/conversation/del` — 删除 · `ImConversationController#del` · auth=`optional`
- [ ] `GET /im/conversation/conversation/detail` — 获取详情 · `ImConversationController#detail` · auth=`optional`
- [ ] `GET /im/conversation/conversation/frontSearch` — 前台搜索 · `ImConversationController#frontSearch` · auth=`optional`
- [ ] `GET /im/conversation/conversation/list` — 获取列表 · `ImConversationController#list` · auth=`optional`
- [ ] `POST /im/conversation/conversation/saveRemark` — 保存备注 · `ImConversationController#saveRemark` · auth=`optional`
- [ ] `GET /im/conversation/conversation/search` — 搜索 · `ImConversationController#search` · auth=`optional`
- [ ] `POST /im/conversation/conversation/transfer` — 转接 · `ImConversationController#transfer` · auth=`optional`
- [ ] `POST /im/conversation/conversation/updateField` — 更新字段 · `ImConversationController#updateField` · auth=`optional`
- [ ] `GET /im/conversation/conversation/waitServantList` — 获取待接入的客户列表 · `ImConversationController#waitServantList` · auth=`optional`

#### im::admin_user_kefu — 9 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\im\AdminUserKefuController.java`
- 目标四层：`crates/core/src/models/im/admin_user_kefu.rs` · `crates/infrastructure/src/db/repo/im/admin_user_kefu.rs` · `crates/services/src/im/admin_user_kefu.rs` · `crates/api/admin/src/routes/im/admin_user_kefu.rs` · `crates/api/public/src/routes/im/admin_user_kefu.rs`
- [ ] `POST /adminapi/im/adminUserKefu/batch` — 批量删除客服人员 · `AdminUserKefuController#batch` · 权限 `kefuManageModify` · auth=`jwt+perm`
- [ ] `POST /adminapi/im/adminUserKefu/create` — 创建客服人员 · `AdminUserKefuController#create` · 权限 `kefuManageModify` · auth=`jwt+perm`
- [ ] `POST /adminapi/im/adminUserKefu/del` — 删除客服人员 · `AdminUserKefuController#del` · 权限 `kefuManageModify` · auth=`jwt+perm`
- [ ] `GET /adminapi/im/adminUserKefu/detail` — 获取详情 · `AdminUserKefuController#detail` · 权限 `kefuManageList` · auth=`jwt+perm`
- [ ] `GET /adminapi/im/adminUserKefu/info` — 获取客服人员信息 · `AdminUserKefuController#info` · 权限 `kefuManageList` · auth=`jwt+perm`
- [ ] `GET /adminapi/im/adminUserKefu/list` — 获取列表 · `AdminUserKefuController#list` · 权限 `kefuManageList` · auth=`jwt+perm`
- [ ] `GET /adminapi/im/adminUserKefu/roles` — 获取客服角色列表 · `AdminUserKefuController#roles` · 权限 `kefuManageList` · auth=`jwt+perm`
- [ ] `POST /adminapi/im/adminUserKefu/update` — 更新客服人员 · `AdminUserKefuController#update` · 权限 `kefuManageModify` · auth=`jwt+perm`
- [ ] `POST /adminapi/im/adminUserKefu/updateField` — 更新字段 · `AdminUserKefuController#updateField` · 权限 `kefuManageModify` · auth=`jwt+perm`

#### im::admin_shop_kefu_config — 2 条

- Java 源文件：`tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\im\AdminShopKefuConfigController.java`
- 目标四层：`crates/core/src/models/im/admin_shop_kefu_config.rs` · `crates/infrastructure/src/db/repo/im/admin_shop_kefu_config.rs` · `crates/services/src/im/admin_shop_kefu_config.rs` · `crates/api/admin/src/routes/im/admin_shop_kefu_config.rs` · `crates/api/public/src/routes/im/admin_shop_kefu_config.rs`
- [ ] `GET /adminapi/im/shopKefuConfig/accessConfig/detail` — 获取接入规则 · `AdminShopKefuConfigController#detail` · 权限 `kefuAccessSetting` · auth=`jwt+perm`
- [ ] `POST /adminapi/im/shopKefuConfig/accessConfig/save` — 保存接入规则 · `AdminShopKefuConfigController#save` · 权限 `kefuAccessSetting` · auth=`jwt+perm`

---

## 附录 B. 基线接口（81 条，已实现、不在切片清单中）

这些接口在 220 切片清单生成之前已实现；codemod 已把它们并入 §3 的 19 个基线 feature。
下表给出接口、Java 控制器与**迁移后**的 Rust 实现文件（`check_api_coverage.py` 的静态提取结果），
用于回归核对：这些接口已计入 635 基线，重构时不得回退。

| # | 接口 | Java 控制器 | 迁移后 Rust 文件 |
|---|------|-------------|------------------|
| 1 | `GET /adminapi/authority/adminUser/list` | AdminUserController | `api/admin/src/routes/slice011.rs:64` |
| 2 | `GET /adminapi/authority/adminUser/mineDetail` | AdminUserController | `api/admin/src/routes/slice011.rs:65` |
| 3 | `GET /api/article/category/getArticleCategoryList` | ArticleCategoryClientController | `api/public/src/routes/content.rs:34` |
| 4 | `GET /api/article/category/list` | ArticleCategoryClientController | `api/public/src/routes/content.rs:35` |
| 5 | `POST /adminapi/content/articleCategory/create` | ArticleCategoryController | `api/admin/src/routes/content.rs:49` |
| 6 | `POST /adminapi/content/articleCategory/del` | ArticleCategoryController | `api/admin/src/routes/content.rs:50` |
| 7 | `GET /adminapi/content/articleCategory/detail` | ArticleCategoryController | `api/admin/src/routes/content.rs:48` |
| 8 | `GET /adminapi/content/articleCategory/list` | ArticleCategoryController | `api/admin/src/routes/content.rs:46` |
| 9 | `GET /adminapi/content/articleCategory/tree` | ArticleCategoryController | `api/admin/src/routes/content.rs:47` |
| 10 | `POST /adminapi/content/article/create` | ArticleController | `api/admin/src/routes/content.rs:42` |
| 11 | `POST /adminapi/content/article/del` | ArticleController | `api/admin/src/routes/content.rs:45` |
| 12 | `GET /adminapi/content/article/detail` | ArticleController | `api/admin/src/routes/content.rs:41` |
| 13 | `GET /adminapi/content/article/list` | ArticleController | `api/admin/src/routes/content.rs:40` |
| 14 | `POST /adminapi/content/article/update` | ArticleController | `api/admin/src/routes/content.rs:43` |
| 15 | `POST /adminapi/content/article/updateField` | ArticleController | `api/admin/src/routes/content.rs:44` |
| 16 | `GET /api/article/article/list` | ArticleController | `api/public/src/routes/content.rs:32` |
| 17 | `GET /api/article/article/newsInfo` | ArticleController | `api/public/src/routes/content.rs:33` |
| 18 | `GET /adminapi/product/brand/list` | BrandController | `api/admin/src/routes/slice005.rs:125` |
| 19 | `POST /api/cart/cart/addToCart` | CartController | `api/public/src/lib.rs:166` |
| 20 | `POST /api/cart/cart/clear` | CartController | `api/public/src/lib.rs:170` |
| 21 | `GET /api/cart/cart/list` | CartController | `api/public/src/lib.rs:165` |
| 22 | `POST /api/cart/cart/removeItem` | CartController | `api/public/src/lib.rs:169` |
| 23 | `POST /api/cart/cart/updateCheck` | CartController | `api/public/src/lib.rs:168` |
| 24 | `POST /api/cart/cart/updateItem` | CartController | `api/public/src/lib.rs:167` |
| 25 | `GET /adminapi/product/category/getAllCategory` | CategoryController | `api/admin/src/routes/catalog.rs:37` |
| 26 | `GET /api/category/category/all` | CategoryController | `api/public/src/routes/catalog.rs:23` |
| 27 | `POST /api/order/check/submit` | CheckController | `api/public/src/lib.rs:172` |
| 28 | `POST /api/user/login/signin` | ClientLoginFilter | `api/public/src/lib.rs:195` |
| 29 | `GET /api/promotion/coupon/listByIds` | CouponClientController | `api/public/src/routes/coupon.rs:32` |
| 30 | `GET /adminapi/promotion/coupon/detail` | CouponController | `api/admin/src/routes/coupon.rs:33` |
| 31 | `POST /adminapi/promotion/coupon/issue/send` | CouponController | `api/admin/src/routes/coupon.rs:36` |
| 32 | `GET /adminapi/promotion/coupon/issue/users` | CouponController | `api/admin/src/routes/coupon.rs:35` |
| 33 | `GET /adminapi/promotion/coupon/list` | CouponController | `api/admin/src/routes/coupon.rs:31` |
| 34 | `GET /adminapi/promotion/coupon/listByIds` | CouponController | `api/admin/src/routes/coupon.rs:32` |
| 35 | `GET /adminapi/promotion/coupon/validList` | CouponController | `api/admin/src/routes/coupon.rs:34` |
| 36 | `POST /adminapi/login/signin` | LoginFilter | `api/admin/src/lib.rs:319` |
| 37 | `GET /adminapi/setting/logisticsCompany/list` | LogisticsCompanyController | `api/admin/src/routes/logistics.rs:27` |
| 38 | `GET /adminapi/user/memberTagGroup/list` | MemberTagGroupController | `api/admin/src/routes/member.rs:25` |
| 39 | `POST /adminapi/order/order/cancelOrderByAdmin` | OrderController | `api/admin/src/routes/slice002.rs:52` |
| 40 | `POST /adminapi/order/order/deliver` | OrderController | `api/admin/src/routes/slice002.rs:51` |
| 41 | `GET /adminapi/order/order/detail` | OrderController | `api/admin/src/routes/slice002.rs:50` |
| 42 | `GET /adminapi/order/order/list` | OrderController | `api/admin/src/routes/slice002.rs:49` |
| 43 | `POST /api/order/pay/notify/lakala` | PayController | `api/public/src/routes/paylog.rs:43` |
| 44 | `POST /api/order/pay/notify/{payType}` | PayController | `api/public/src/routes/paylog.rs:44` |
| 45 | `POST /api/order/pay/notify/{payType}` | PayController | `api/public/src/routes/paylog.rs:44` |
| 46 | `POST /api/order/pay/refundNotify/{payType}` | PayController | `api/public/src/routes/paylog.rs:45` |
| 47 | `POST /api/order/pay/refundNotify/{payType}` | PayController | `api/public/src/routes/paylog.rs:45` |
| 48 | `POST /adminapi/finance/payLog/create` | PaylogController | `api/admin/src/routes/paylog.rs:33` |
| 49 | `GET /adminapi/finance/payLog/detail` | PaylogController | `api/admin/src/routes/paylog.rs:32` |
| 50 | `GET /adminapi/finance/payLog/list` | PaylogController | `api/admin/src/routes/paylog.rs:31` |
| 51 | `POST /adminapi/finance/payLog/update` | PaylogController | `api/admin/src/routes/paylog.rs:34` |
| 52 | `GET /api/user/pointsLog/list` | PointsLogController | `api/public/src/routes/member.rs:25` |
| 53 | `GET /adminapi/product/product/detail` | ProductController | `api/admin/src/routes/slice001.rs:80` |
| 54 | `GET /adminapi/product/product/list` | ProductController | `api/admin/src/routes/slice001.rs:79` |
| 55 | `GET /api/product/product/detail` | ProductController | `api/public/src/routes/slice001.rs:38` |
| 56 | `GET /api/product/product/detail` | ProductController | `api/public/src/routes/slice001.rs:38` |
| 57 | `GET /api/product/product/list` | ProductController | `api/public/src/lib.rs:192` |
| 58 | `POST /api/user/regist/registAct` | RegisterController | `api/public/src/lib.rs:196` |
| 59 | `POST /adminapi/promotion/seckill/create` | SeckillController | `api/admin/src/routes/seckill.rs:34` |
| 60 | `POST /adminapi/promotion/seckill/del` | SeckillController | `api/admin/src/routes/seckill.rs:36` |
| 61 | `GET /adminapi/promotion/seckill/detail` | SeckillController | `api/admin/src/routes/seckill.rs:33` |
| 62 | `GET /adminapi/promotion/seckill/list` | SeckillController | `api/admin/src/routes/seckill.rs:32` |
| 63 | `POST /adminapi/promotion/seckill/update` | SeckillController | `api/admin/src/routes/seckill.rs:35` |
| 64 | `GET /adminapi/setting/shippingTpl/list` | ShippingTplController | `api/admin/src/routes/logistics.rs:26` |
| 65 | `GET /adminapi/setting/shippingType/list` | ShippingTypeController | `api/admin/src/routes/slice210.rs:100` |
| 66 | `POST /adminapi/promotion/timeDiscount/create` | TimeDiscountController | `api/admin/src/routes/time_discount.rs:35` |
| 67 | `POST /adminapi/promotion/timeDiscount/del` | TimeDiscountController | `api/admin/src/routes/time_discount.rs:37` |
| 68 | `GET /adminapi/promotion/timeDiscount/detail` | TimeDiscountController | `api/admin/src/routes/time_discount.rs:34` |
| 69 | `GET /adminapi/promotion/timeDiscount/list` | TimeDiscountController | `api/admin/src/routes/time_discount.rs:33` |
| 70 | `POST /adminapi/promotion/timeDiscount/update` | TimeDiscountController | `api/admin/src/routes/time_discount.rs:36` |
| 71 | `GET /api/user/user/detail` | UserController | `api/public/src/lib.rs:178` |
| 72 | `POST /api/user/coupon/claim` | UserCouponController | `api/public/src/routes/coupon.rs:42` |
| 73 | `POST /api/user/coupon/del` | UserCouponController | `api/public/src/routes/coupon.rs:43` |
| 74 | `GET /api/user/coupon/detail` | UserCouponController | `api/public/src/routes/coupon.rs:41` |
| 75 | `GET /api/user/coupon/getList` | UserCouponController | `api/public/src/routes/coupon.rs:31` |
| 76 | `GET /api/user/coupon/list` | UserCouponController | `api/public/src/routes/coupon.rs:40` |
| 77 | `POST /api/user/order/cancelOrder` | UserOrderController | `api/public/src/lib.rs:176` |
| 78 | `GET /api/user/order/detail` | UserOrderController | `api/public/src/lib.rs:175` |
| 79 | `GET /api/user/order/list` | UserOrderController | `api/public/src/lib.rs:174` |
| 80 | `GET /adminapi/user/userPointsLog/list` | UserPointsLogController | `api/admin/src/routes/member.rs:26` |
| 81 | `GET /adminapi/user/userRank/list` | UserRankController | `api/admin/src/routes/member.rs:24` |

> 合计 **81** 条基线接口。

---

## 4. 如何认领并完成一个 feature

### 4.1 认领

1. 在 §2 对应 ctx 表中挑一行（建议从 P0 开始；同 ctx 内任意 feature 相互独立）。
2. 读该 feature 的 Java 源文件（表中「Java 控制器」列；完整路径见附录 A 对应小节）与附录 A 的接口清单。
3. 读 `domain-map.json` 中该切片条目：`ctx / feature / type_prefix / coded / split[]`；多控制器切片先按 `split[]` 拆文件。
4. 在共享任务板上登记 `ctx::feature` + 预期四层文件路径（写作用域），避免与他人冲突。

### 4.2 实现（四层，同名文件）

| 层 | 文件 | 内容 | 禁止 |
|----|------|------|------|
| core | `crates/core/src/models/<ctx>/<feature>.rs` | `#[derive(Serialize, Deserialize)]` DTO/VO/Query、纯函数、单测 | SQL/IO |
| infrastructure | `crates/infrastructure/src/db/repo/<ctx>/<feature>.rs` | SQLx 查询、行映射、返回 `Result<T>` | 业务分支 |
| services | `crates/services/src/<ctx>/<feature>.rs` | 用例编排、`pool.begin()/commit()` 事务边界、领域校验 | HTTP 类型 |
| api | `crates/api/admin|public/src/routes/<ctx>/<feature>.rs` | `ROUTE_TABLE`、`routes(&State)`、`implemented_routes()`、handler | SQL、>1 层业务分支 |

约定：

- query/body DTO 必须 `#[serde(rename_all = "camelCase")]`；
- 路径逐字符对齐 `docs/api-contract.json`；`ROUTE_TABLE` 与 `routes()` 一一对应并由单测断言；
- handler 只做「解析参数 → 调 service → 包响应」；错误码/响应结构与 Java 保持一致（行为不变）；
- 在域聚合器追加：`<ctx>/mod.rs`（`pub mod <feature>;` + `Services` 字段）、`routes/<ctx>/mod.rs`（merge）、
  `db/repo/<ctx>/mod.rs`、`core/models/<ctx>/mod.rs`；`AdminState/PublicState` 只加域字段，禁止平铺 80 字段。

### 4.3 完成定义（DoD，全部满足才算完成）

| 门禁 | 命令 | 通过标准 |
|------|------|----------|
| 编译 | `cargo check --workspace --all-targets` | 0 error |
| 测试 | `cargo test --workspace` | 0 failed（含 `ROUTE_TABLE` ↔ `routes()` 断言） |
| 接口覆盖 | `python rust/tools/check_api_coverage.py` | 不低于 **635/1564**，且本 feature 接口只增不减 |
| 路由冲突 | `python rust/tools/check_route_conflicts.py --strict` | 0 重复 |
| SQL 校验 | `python rust/tools/check_sql_columns.py --strict` | 0 未知表/列 |
| 结构门禁 | `python rust/tools/check_structure.py --strict` | 0 违规（禁止 `slice` 命名、层级越界、空壳注册） |

补充（feature 级）：

- [ ] 附录 A 中该 feature 的所有 `- [ ]` 接口全部在 `implemented_routes()` 中可达（不是「已注册但返回未实现」）。
- [ ] 该 feature 在四层各有一个同名文件，路径与 `domain-map.json` 一致。
- [ ] 无空壳：不允许只注册路由不实现（也不允许只写 handler 不接 DB）。
- [ ] 若该 feature 属于多控制器切片（`slice-001/112/137/158/194`），原切片文件已按 `split[]` 拆开且旧文件已删除。
- [ ] 提交信息含 `ctx::feature`，便于 `git log --follow` 追溯旧实现。

---

*本文件由 `domain-map.json` + `_migration-map.json` + `check_api_coverage.py` 静态快照生成；结构变更必须先改 `ARCHITECTURE.md`。*
