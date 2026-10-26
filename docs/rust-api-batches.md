# Rust 缺失接口补齐计划（基于本地 Java + 跨境版源码）

## 结论

本地 `java/` 是 5.8.57 完整 Java 后端；跨境版源码在：

```text
D:\tigshop\java-b2b2c-overseas-v5.8.39-cd82043d4910-4bb8dc526b5c4974ac1dedb29ae6bfbc
```

两边合起来可以覆盖大部分缺失接口和前端页面，但不能在一次构建内全部完成，需要分批移植、验证、部署。

## 当前审计

```text
Apifox 唯一接口：1600
Rust 已有对应：1472
未找到：128
```

对 128 个缺失接口做源码命中检查（按方法级 Mapping/页面路径粗略匹配）：

```text
Java 后端源码可参考：84 / 129
前端源码可参考：97 / 129
```

剩余约 30~45 个接口在 5.8.39 跨境版里也没有，需要按 Apifox 契约新写，不能直接复制。

## 批次

### A：多语言 + 登录 + 海外支付

后端：

```text
/adminapi/lang/translations/*
/adminapi/lang/locales/*
/adminapi/lang/localesRelation/*
/adminapi/lang/currency/*
/api/user/oauth/*
/api/order/pay/notify/stripe
/api/order/pay/notify/xendit
/api/order/pay/notify/antom
/adminapi/setting/config/saveStripe|saveXendit|saveAntom
```

参考源码：

```text
java/tigshop-service/.../lang/
java/tigshop-service/.../oauth/
java/tigshop-service/.../pay/
java/tigshop-adminapi/.../setting/ConfigController.java
```

前端：

```text
java/view/admin/src/views/lang/
java/view/admin/src/views/setting/
```

### B：O2O 派送/服务人员/钱包

后端：

```text
/adminapi/o2o/*
/api/o2o/deliveryOrder/*
/api/o2o/serviceStaff/*
/api/o2o/deliveryOrder/wallet/*
```

前端：

```text
跨境版 view/uniapp/src/pages/store/
跨境版 view/uniapp/src/pages/member/components/decorate/
```

### C：财务员工资金/提现

```text
/adminapi/finance/staffFundWallet/*
/adminapi/finance/staffFundWithdraw/*
/adminapi/finance/serverStaffWithdraw/*
/adminapi/finance/deliveryStaffWithdraw/*
```

### D：POS 收银台

```text
/adminapi/pos/order/check/*
/adminapi/pos/cart/*
/adminapi/pos/user/*
/adminapi/pos/adminUserPos/*
```

### E：供应商/供应商员工

```text
/adminapi/vendor/*
/adminapi/authority/suppliers/*
```

### F：次卡 + 剩余配置/权限

```text
/api/cycleCard/*
/adminapi/setting/galleryVideo/treeList
/adminapi/setting/config/*
```

## 进度

### B 批构建部署（2026-10-26）

```text
run：40253375232
artifact：12621473908
sha256：c14093b938b36a8668e940f495c6e4af9cdb0e2fb7ab6e17748b1cbdfb1ad12d
```

部署结果：

```text
服务 active，health UP
备份：/www/wwwroot/tigshop-rust/tigshop.batchB.bak
```

线上验证：

```text
/api/o2o/deliveryOrder/grabList             → 200
/api/o2o/config/localDeliveryConfig?shopId=1 → 200
/api/o2o/config/homeServiceConfig?shopId=1   → 200
```

### 2026-10-26 B9：提现申请 + O2O 门店配置

```text
POST /api/o2o/staffWallet/applyWithdraw
GET  /api/o2o/config/localDeliveryConfig
GET  /api/o2o/config/homeServiceConfig
```

- applyWithdraw 单事务：校验余额 → 插入提现单 → 余额转冻结 → 写流水
- 同城配送/上门服务配置返回门店配置 + 距离溢价列表
- 审计未实现数：66 → 63
- commit：`8ecf1126`

### 2026-10-26 B8：收款账号 + 提现查询

```text
GET  /api/o2o/staffAccount/list
POST /api/o2o/staffAccount/save
POST /api/o2o/staffAccount/del
GET  /api/o2o/staffWallet/withdrawConfig
GET  /api/o2o/staffWallet/myWithdrawList
GET  /api/o2o/staffWallet/myWithdrawDetail
```

- 收款账号复用 `user_withdraw_account`（按当前会员 user_id）
- 提现规则按服务人员 type 读 `o2oDeliveryWithdrawConfig` / `o2oServerWithdrawConfig`
- 提现记录查询仅限本人
- `cargo check -p tigshop-api` 通过
- 审计未实现数：72 → 66
- commit：`35cec945`

### 2026-10-26 B7：服务人员资料 + 钱包基础

```text
GET  /api/o2o/serviceStaff/myInfo
POST /api/o2o/serviceStaff/updateWorkStatus
GET  /api/o2o/staffWallet/myWallet
GET  /api/o2o/staffWallet/myLogs
```

- 服务人员资料按 `member_id` 绑定
- 工作状态更新
- 钱包余额/冻结/累计收入
- 钱包流水分页
- 审计未实现数：76 → 72
- commit：`c43d8b2f`

### 2026-10-26 B6：O2O C 端派送订单

已补：

```text
POST /api/o2o/deliveryOrder/grab
POST /api/o2o/deliveryOrder/confirmPickup
POST /api/o2o/deliveryOrder/complete
POST /api/o2o/deliveryOrder/cancel
GET  /api/o2o/deliveryOrder/statistics
GET  /api/o2o/deliveryOrder/myList
GET  /api/o2o/deliveryOrder/myListTotalCount
GET  /api/o2o/deliveryOrder/grabList
GET  /api/o2o/deliveryOrder/detail
GET  /api/o2o/deliveryOrder/myListTabCounts
```

- 当前会员按 `service_staff.member_id` 绑定服务人员
- 抢单仅限 status=10、dispatch_mode=1
- 状态流转：20 待取货 → 30 服务中 → 40 完成；取消 50
- 列表支持 pending/processing/completed tab
- `cargo check -p tigshop-api` 通过
- 审计未实现数：84 → 76
- commit：`92d950b3`

### 2026-10-26 B5：O2O 商品分配 + 钱包后台

已补 4 条：

```text
GET  /adminapi/o2o/productOrg/info/{productId}
POST /adminapi/o2o/productOrg/distribution
GET  /adminapi/o2o/serviceStaffWallet/detail
GET  /adminapi/o2o/serviceStaffWallet/logList
```

- `product_org` 按 orgType 分组返回；distribution 先清空再重建
- 钱包详情含 totalWithdrawn；流水按 staffId 分页
- `cargo check -p tigshop-admin-api` 通过
- 审计未实现数：88 → 84
- commit：`d7c55e70`

### 2026-10-26 B4：O2O 派送订单后台

已补 4 条：

```text
GET  /adminapi/o2o/deliveryOrder/list
GET  /adminapi/o2o/deliveryOrder/detail
POST /adminapi/o2o/deliveryOrder/dispatch
POST /adminapi/o2o/deliveryOrder/cancel
```

- 列表支持店铺/类型/状态/配送员/关键词筛选
- 手动派单写入 staff 信息、status=20、dispatch_mode=2
- 取消置 status=50 并记录原因/时间
- `cargo check -p tigshop-admin-api` 通过
- 审计未实现数：94 → 88
- commit：`6d1498de`

### 2026-10-26 B3：O2O 区域管理

已补 5 条：

```text
GET  /adminapi/o2o/areaStoreManager/detail
POST /adminapi/o2o/areaStoreManager/create
POST /adminapi/o2o/areaStoreManager/update
POST /adminapi/o2o/areaStoreManager/bindShop
POST /adminapi/o2o/areaStoreManager/batch
```

- `area_store_manager` + `area_store_manager_shop` 联动
- create/update 自动重建店铺绑定
- `cargo check -p tigshop-admin-api` 通过
- 审计未实现数：99 → 94
- commit：`3f26125b`

### 2026-10-26 B2：O2O 服务人员

已补 8 条：

```text
GET  /adminapi/o2o/serviceStaff/list
GET  /adminapi/o2o/serviceStaff/detail
GET  /adminapi/o2o/serviceStaff/showMobile
POST /adminapi/o2o/serviceStaff/create
POST /adminapi/o2o/serviceStaff/update
POST /adminapi/o2o/serviceStaff/updateField
POST /adminapi/o2o/serviceStaff/del
POST /adminapi/o2o/serviceStaff/batch
```

- `service_staff` 表 CRUD；updateField 白名单
- `cargo check -p tigshop-admin-api` 通过
- 审计未实现数：107 → 99
- commit：`943659f6`

### 2026-10-26 B1：O2O 标签管理

在已有低代码路由基础上补齐：

```text
GET  /adminapi/o2o/tip/detail
POST /adminapi/o2o/tip/create
POST /adminapi/o2o/tip/update
POST /adminapi/o2o/tip/updateField
```

- `tip` 表 CRUD；updateField 仅允许 `tipName`/`status`
- `cargo check -p tigshop-admin-api` 通过
- 审计未实现数：111 → 107
- commit：`edde4878`

### 2026-10-26 A1：多语言路由已补

```text
POST /adminapi/lang/locales/create
POST /adminapi/lang/currency/create
POST /adminapi/lang/localesRelation/create
POST /adminapi/lang/translations/update
POST /adminapi/setting/galleryVideo/treeList   （getAllCategory 的文档契约别名）
```

- service 层原本已存在，本次补挂路由并加入 implemented_routes
- `cargo check -p tigshop-admin-api --lib` 通过
- `cargo test -p tigshop-admin-api --lib lang::` 6 passed
- 审计未实现数：128 → 123
- commit：`04d78abe`、`ae779a28`、`a0982481`

待继续：A2 OAuth（Google/Facebook 已迁移，Twitter 待补）、A3 海外支付回调/配置。

### 2026-10-26 A2：Google/Facebook OAuth

```text
GET  /api/user/oauth/render/{source}
GET  /api/user/oauth/callback/{source}
POST /api/user/oauth/callback/{source}
```

- 新增 `services/src/member/login.rs` 的 OAuth render/callback
- state 存缓存（10 分钟），Google/Facebook token 交换 + 用户信息拉取
- `user_authorize` 绑定，新用户自动注册并签发会员 JWT
- 已接入 `api/public` 匿名路由
- `cargo check -p tigshop-api --lib` 通过
- 审计未实现数：123 → 120
- commit：`ee8fb75e`

### 2026-10-26 A2.1：Twitter/X OAuth2 + PKCE

- render：生成 code_verifier / S256 challenge，存缓存 10 分钟
- callback：Basic Auth 换 token，`/2/users/me` 拉用户，绑定 `user_authorize`（type=103）
- 至此 Google / Facebook / Twitter 三条第三方登录均已迁移
- `cargo check -p tigshop-services` 通过
- commit：`8d9fd033`

### 2026-10-26 A3：支付配置（部分）

已补 6 条后台支付配置端点（schema-driven config）：

```text
GET  /adminapi/setting/config/stripeSettings
POST /adminapi/setting/config/saveStripe
GET  /adminapi/setting/config/xenditSettings
POST /adminapi/setting/config/saveXendit
GET  /adminapi/setting/config/antomSettings
POST /adminapi/setting/config/saveAntom
```

- `ENDPOINTS` 102 → 108，相关测试通过
- 审计未实现数：120 → 114
- commit：`a5ed9637`

待补：Stripe/Xendit/Antom 支付回调（需要验签与支付单创建链路）。

### 2026-10-26 A3.1：Stripe / Xendit 回调验签

```text
POST /api/order/pay/notify/stripe
POST /api/order/pay/notify/xendit
```

- Stripe：`Stripe-Signature` HMAC-SHA256 验签，只处理 `payment_intent.succeeded` / `checkout.session.completed`
- Xendit：`x-callback-token` 常量时间校验，只处理 `PAID` / `SETTLED`
- 成功后调用 `mark_paid_by_pay_sn` + `settle_order_by_pay_sn`
- Antom：路由已注册，但 RSA 验签未完成，返回明确 1001，不会接受支付通知
- `cargo check -p tigshop-api` 通过
- 审计未实现数：114 → 111
- commit：`1daf41bb`

### A 批构建部署（2026-10-26）

```text
run：40250056157
head：263f6e5e80de
artifact：12620537466
sha256：a14ec787b152e6b89eb23f40640d6301c91548bbe8ab512b4c058379c6ff9124
```

部署结果：

```text
服务 active，health UP
备份：/www/wwwroot/tigshop-rust/tigshop.batchA.bak
```

线上验证：

```text
/api/user/oauth/render/google   → 200，已生成 Google 授权地址
/api/user/oauth/render/facebook → 200，已生成 Facebook 授权地址
/api/user/oauth/render/twitter  → 1001 twitterClientId 未配置
```

说明：Twitter 代码已完成，但后台尚未配置 twitterClientId/Secret。

## 每批验收

```text
1. 从 Java 源码提取 DTO / 表 / 状态逻辑
2. Rust 补 model + repo + service + route
3. cargo check
4. 对照 Apifox 逐接口请求/响应验证
5. 前端补页面或组件
6. H5/admin 构建
7. 部署并更新 docs/rust-api-audit.md
```
