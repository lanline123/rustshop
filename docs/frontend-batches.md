# 前端补齐进度

## 现状

- 后端接口审计：`docs/rust-api-audit.md` 未实现 **0**
- H5 源：`java/view/uniapp`
- PC 源：`java/public/pc` / `java/view/Tigshop-Pc`
- Admin 源：`java/view/admin`
- H5 线上：`https://battery.recy.cc/mobile/`（移动 UA）

## H5

### 2026-10-26 配送员端页面

新增：

```text
src/api/o2o/delivery.ts
src/pages/delivery/list.vue
src/pages/delivery/detail.vue
src/pages/delivery/wallet.vue
pages.json -> pages/delivery 分包（list/detail/wallet）
```

对应接口：

```text
GET  /api/o2o/deliveryOrder/grabList
GET  /api/o2o/deliveryOrder/myList
GET  /api/o2o/deliveryOrder/detail
POST /api/o2o/deliveryOrder/grab
POST /api/o2o/deliveryOrder/confirmPickup
POST /api/o2o/deliveryOrder/complete
POST /api/o2o/deliveryOrder/cancel
GET  /api/o2o/staffWallet/myWallet
GET  /api/o2o/staffWallet/myLogs
GET  /api/o2o/staffAccount/list
POST /api/o2o/staffWallet/applyWithdraw
```

构建部署：

```text
zip sha256：f8127aed9344da01a7a38aa2e5b97264f306676f364a725cfca1505ab584e605
备份：/www/backup/mobile.bak.20261026114146
```

线上验证：

```text
/mobile/ -> 200（移动 UA）
assets/pages-delivery-list.Dt9JNF-9.js   -> 200
assets/pages-delivery-detail.CGfVI-Mo.js -> 200
assets/pages-delivery-wallet.BGxwEHHV.js -> 200
```

### 2026-10-26 次卡页面

新增：

```text
src/api/cycleCard/cycleCard.ts
src/pages/cycleCard/list.vue
src/pages/cycleCard/detail.vue
pages.json -> pages/cycleCard 分包（list/detail）
```

对应接口：

```text
GET /api/cycleCard/list
GET /api/cycleCard/detail/{cardId}
GET /api/cycleCard/checkUsable/{cardId}
GET /api/cycleCard/refundInfo/{cardId}
```

构建部署：

```text
npm run build:h5 → dist/build/h5
zip sha256：80c74657e04cfbb43b2c7eb9a59ca6e36d2d31c47a16696639673f9b0c363762
备份：/www/backup/mobile.bak.20261026113843
```

线上验证：

```text
/mobile/ -> 200（移动 UA）
/mobile/assets/pages-cycleCard-list.Cniq99zs.js   -> 200
/mobile/assets/pages-cycleCard-detail.CucJK029.js -> 200
```

### H5 待补

```text
OAuth 回调页 /pages/oauth/callback
配送员端订单/钱包/提现页面
服务人员端页面
次卡兑换/退款申请交互
```

## PC

### 2026-10-26 OAuth 回调页

新增静态页：

```text
java/public/oauth/callback/index.html
```

- 读取 `oauthPath` / `oauthReturnUrl` cookie
- 调用 `POST /api/user/oauth/callback/{path}`
- 写入 `accessToken` cookie 后回跳
- 线上：`https://battery.recy.cc/oauth/callback` → 200

> 采用静态页而非重建 Nuxt，避免覆盖现有 PC 构建产物。

### 待补

```text
/oauth/callback 页面
次卡相关页面
O2O 相关页面
```

## Admin

### 待补

```text
财务员工资金/提现页面
O2O 标签/服务人员/区域/派送订单/钱包页面
POS 收银台页面
供应商管理页面
```

> Admin 页面为编译后的 SPA（`java/public/admin` / `java/view/admin`），新增页面需确认构建链后再操作。
