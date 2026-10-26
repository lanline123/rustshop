# 前端补齐进度

## 现状

- 后端接口审计：`docs/rust-api-audit.md` 未实现 **0**
- H5 源：`java/view/uniapp`
- PC 源：`java/public/pc` / `java/view/Tigshop-Pc`
- Admin 源：`java/view/admin`
- H5 线上：`https://battery.recy.cc/mobile/`（移动 UA）

## H5

### 2026-10-26 服务人员个人中心

新增：

```text
src/pages/staff/profile.vue
pages.json -> pages/staff 分包（profile）
```

对应接口：

```text
GET  /api/o2o/serviceStaff/myInfo
POST /api/o2o/serviceStaff/updateWorkStatus
GET  /api/o2o/staffWallet/myWallet
```

功能：

```text
服务人员资料 / 完成订单数 / 完成金额
下班 / 空闲 / 忙碌 状态切换
跳转钱包、配送工作台
```

构建部署：

```text
zip sha256：6bb1e310ab88bd8a3665be3bee893a58b82ac0f7863eaf734e42e3bd35ebc145
备份：/www/backup/mobile.bak.20261026114613
线上：pages-staff-profile.BcVXBKHn.js -> 200
```

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

### 2026-10-26 次卡页面（静态页）

新增：

```text
java/public/cycleCard/index.html
```

- 读取 `accessToken` cookie 调 `/api/cycleCard/list`
- 列表 / 详情 / 可用性检查
- 线上：`https://battery.recy.cc/cycleCard` → 200
- 2026-10-26 配送工作台新增「个人」入口，跳转 `/pages/staff/profile`
- 最新 H5 备份：`/www/backup/mobile.bak.20261026115754`

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

### 2026-10-26 员工收益管理页面

新增页面：

```text
src/api/finance/staffFund.ts
src/views/finance/staffFundWallet/Overview.vue
src/views/finance/staffFundWalletLog/List.vue
src/views/finance/deliveryStaffWithdraw/List.vue
src/views/finance/fundSettings/Index.vue
```

新增路由（`router/asyncRoutes/admin/finance.ts`，名称对齐权限表）：

```text
staffFundManagement
  deliveryFundSettingsManage     finance/delivery-fund-management/settings
  serverFundSettingsManage       finance/server-fund-management/settings
  staffFundWalletManage          finance/staff-fund-wallet/overview
  staffFundWalletLogManage       finance/staff-fund-wallet-log/list
  deliveryStaffWithdrawManage    finance/delivery-staff-withdraw/list
  serverStaffWithdrawManage      finance/server-staff-withdraw/list
```

构建部署：

```text
npm install → 878 packages
NODE_OPTIONS=--max-old-space-size=8192 npm run build-only → 成功
zip sha256：9426cb3bf06e5090cde98526756902e7bdeb664ce1e61c446dfe19bcd302a3ab
备份：/www/backup/admin.bak.20261026115402
```

线上验证：

```text
/admin/ → 200
/admin/assets/Overview-oof508e9.js → 200
/admin/assets/staffFund-BauBLyJn.js → 200
```

线上权限菜单已确认存在：

```text
staffFundManagement         员工收益管理
deliveryFundSettingsManage  配送设置
serverFundSettingsManage    服务设置
staffFundWalletManage       资金总览
staffFundWalletLogManage    资金日志
deliveryStaffWithdrawManage 配送员提现管理
serverStaffWithdrawManage   服务员提现管理
```

## 线上验收（2026-10-26）

```text
https://battery.recy.cc/adminapi/health → 200
https://battery.recy.cc/admin/           → 200
https://battery.recy.cc/oauth/callback   → 200
https://battery.recy.cc/cycleCard        → 200
https://battery.recy.cc/mobile/          → 200
```

后端接口审计：`docs/rust-api-audit.md` 未实现 **0**。

## 结论

- 后端 128 个缺失接口已全部补齐并部署
- H5：次卡、配送员订单/钱包、服务人员个人中心已上线
- PC：OAuth 回调、次卡页面已上线
- Admin：员工收益/提现/资金日志/设置页面已上线，权限菜单已核对

## 仍未覆盖

- 独立 POS 前端（`o2o.recy.cc`）源码不在当前工作区
- H5 次卡兑换/退款申请需要后端新增业务接口，当前仅展示与可用性校验
- PC 端 O2O 页面为静态补充页，未并入 Nuxt 构建

## 待补

```text
O2O 标签/服务人员/区域/派送订单/钱包页面（Admin 部分页面）
POS 收银台页面
供应商管理页面
```

