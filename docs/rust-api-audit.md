# Rust 接口实现审计（对照 Tigshop Java Apifox 文档）

- 文档接口（HTTP，按 method+path 去重）：1600
- Rust 源码中能对应到：1493
- 未找到对应实现：107

> 说明：这是静态审计。判断依据是 Rust 源码中是否出现该路径（含去掉 `/api`、`/adminapi` 前缀后的形式）。
> “未找到”不一定是完全没写，也可能是路径由常量拼接、特征开关或尚未迁移。

## 缺失分布

| 分组 | 数量 |
| --- | --- |
| 前台api / o2o --- 多门店 / 派送订单（配送员 / 服务员端） | 10 |
| 后台adminapi / o2o --- 多门店 / 服务人员管理 | 8 |
| 后台adminapi / authority --- 权限组 / 供应商管理 | 7 |
| 前台api / o2o --- 多门店 / 配送员 / 服务员钱包（C 端） | 6 |
| 后台adminapi / o2o --- 多门店 / 区域管理 | 5 |
| 后台adminapi / vendor --- 供应商组 / 供应商员工管理 | 5 |
| 前台api / cyclecard --- 次卡组 / 次卡权益 | 5 |
| 后台adminapi / finance --- 财务组 / 员工资金总览 | 4 |
| 后台adminapi / pos --- 收银台组 / 收银台员工管理 | 4 |
| 后台adminapi / o2o --- 多门店 / 派送订单管理 | 4 |
| 后台adminapi / finance --- 财务组 / 员工提现管理 | 3 |
| 后台adminapi / finance --- 财务组 / 服务员提现管理 | 3 |
| 后台adminapi / finance --- 财务组 / 配送员提现管理 | 3 |
| 后台adminapi / pos --- 收银台组 / 收银台订单结算 | 3 |
| 后台adminapi / vendor --- 供应商组 / 供应商资金变化 | 3 |
| 前台api / o2o --- 多门店 / 配送员收款账号（C 端） | 3 |
| 后台adminapi / promotion --- 营销组 / 微信直播 | 2 |
| 后台adminapi / finance --- 财务组 / 配送员资金管理 - 配送设置 | 2 |
| 后台adminapi / finance --- 财务组 / 员工资金日志 | 2 |
| 后台adminapi / pos --- 收银台组 / 收银台充值 | 2 |
| 后台adminapi / pos --- 收银台组 / 收银台会员优惠券 | 2 |
| 后台adminapi / pos --- 收银台组 / 收银购物车管理 | 2 |
| 后台adminapi / o2o --- 多门店 / 商品分配管理 | 2 |
| 后台adminapi / o2o --- 多门店 / 配送员钱包（后台） | 2 |
| 后台adminapi / vendor --- 供应商组 / 供应商商品控制器 | 2 |
| 前台api / o2o --- 多门店 / 服务人员（配送员 / 服务员端） | 2 |
| 前台api / o2o --- 多门店 / O2O配置（前端） | 2 |
| 后台adminapi / promotion --- 营销组 / 抽奖活动管理 | 1 |
| 后台adminapi / finance --- 财务组 / 对账单 | 1 |
| 后台adminapi / finance --- 财务组 / 服务员资金管理 - 服务设置 | 1 |
| 后台adminapi / buyershow --- 买家秀组 / 买家秀分组管理 | 1 |
| 后台adminapi / pos --- 收银台组 / 收银会员 | 1 |
| 后台adminapi / pos --- 收银台组 / 收银台订单预约 | 1 |
| 后台adminapi / vendor --- 供应商组 / 供应商商品平台端控制器 | 1 |
| 前台api / user --- 会员中心 / 登录控制器 | 1 |
| 前台api / promotion----营销 / 礼品卡 | 1 |

## 缺失接口明细

### 前台api / o2o --- 多门店 / 派送订单（配送员 / 服务员端）（10）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/api/o2o/deliveryOrder/grab` | 抢单 |
| POST | `/api/o2o/deliveryOrder/confirmPickup` | 确认取货/开始服务 |
| POST | `/api/o2o/deliveryOrder/complete` | 完成配送/服务 |
| POST | `/api/o2o/deliveryOrder/cancel` | 取消派送订单 |
| GET | `/api/o2o/deliveryOrder/statistics` | 统计数据 |
| GET | `/api/o2o/deliveryOrder/myList` | 我的派送订单列表 |
| GET | `/api/o2o/deliveryOrder/myListTotalCount` | 我的派送订单总数 |
| GET | `/api/o2o/deliveryOrder/grabList` | 待抢单列表 |
| GET | `/api/o2o/deliveryOrder/detail` | 派送订单详情 |
| GET | `/api/o2o/deliveryOrder/myListTabCounts` | 我的派送订单各 tab 数量 |

### 后台adminapi / o2o --- 多门店 / 服务人员管理（8）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/o2o/serviceStaff/update` | 更新服务人员 |
| POST | `/adminapi/o2o/serviceStaff/updateField` | 更新指定字段（状态等） |
| POST | `/adminapi/o2o/serviceStaff/del` | 删除服务人员 |
| POST | `/adminapi/o2o/serviceStaff/create` | 创建服务人员 |
| POST | `/adminapi/o2o/serviceStaff/batch` | 批量删除服务人员 |
| GET | `/adminapi/o2o/serviceStaff/showMobile` | 获取服务人员详情 |
| GET | `/adminapi/o2o/serviceStaff/list` | 获取服务人员列表 |
| GET | `/adminapi/o2o/serviceStaff/detail` | 获取服务人员详情 |

### 后台adminapi / authority --- 权限组 / 供应商管理（7）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/authority/suppliers/update` | 更新 |
| POST | `/adminapi/authority/suppliers/updateField` | 更新字段 |
| POST | `/adminapi/authority/suppliers/del` | 删除 |
| POST | `/adminapi/authority/suppliers/create` | 创建 |
| POST | `/adminapi/authority/suppliers/batch` | 批量操作 |
| GET | `/adminapi/authority/suppliers/list` | 获取列表 |
| GET | `/adminapi/authority/suppliers/detail` | 获取详情 |

### 前台api / o2o --- 多门店 / 配送员 / 服务员钱包（C 端）（6）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/api/o2o/staffWallet/applyWithdraw` | 提交提现申请（按 staff.type 路由 delivery / server service） |
| GET | `/api/o2o/staffWallet/withdrawConfig` | 查询提现规则（按 staff.type 路由 delivery / server 配置） |
| GET | `/api/o2o/staffWallet/myWithdrawList` | 查询个人提现申请记录（按 staff.type 自动过滤业务类型） |
| GET | `/api/o2o/staffWallet/myWithdrawDetail` | 查询个人提现申请详情 |
| GET | `/api/o2o/staffWallet/myWallet` | 查询个人钱包余额 |
| GET | `/api/o2o/staffWallet/myLogs` | 查询个人钱包流水 |

### 后台adminapi / o2o --- 多门店 / 区域管理（5）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/o2o/areaStoreManager/update` | 区域编辑 |
| POST | `/adminapi/o2o/areaStoreManager/create` | 区域添加 |
| POST | `/adminapi/o2o/areaStoreManager/bindShop` | 区域绑定店铺 |
| POST | `/adminapi/o2o/areaStoreManager/batch` | 批量操作 |
| GET | `/adminapi/o2o/areaStoreManager/detail` | 区域详情 |

### 后台adminapi / vendor --- 供应商组 / 供应商员工管理（5）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/vendor/adminVendorUser/update` | 更新供应商员工 |
| POST | `/adminapi/vendor/adminVendorUser/modifyUser` | 修改供应商员工用户 |
| POST | `/adminapi/vendor/adminVendorUser/create` | 创建供应商员工 |
| GET | `/adminapi/vendor/adminVendorUser/info` | 获取供应商员工信息 |
| GET | `/adminapi/vendor/adminVendorUser/detail` | 获取详情 |

### 前台api / cyclecard --- 次卡组 / 次卡权益（5）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| GET | `/api/cycleCard/refundInfo/{cardId}` | 查询可退款信息 |
| GET | `/api/cycleCard/list` | 我的次卡列表 |
| GET | `/api/cycleCard/detail/{cardId}` | 次卡详情 |
| GET | `/api/cycleCard/checkUsable/{cardId}` | 校验次卡是否可使用 |
| GET | `/api/cycleCard/checkAftersales/{orderId}` | 校验次卡订单是否可申请售后 |

### 后台adminapi / finance --- 财务组 / 员工资金总览（4）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| GET | `/adminapi/finance/staffFundWallet/staffList` | 员工资金列表：按员工维度展示资金信息，支持姓名/门店/状态/businessType 筛选，支持按资金字段排序 |
| GET | `/adminapi/finance/staffFundWallet/ranking` | 收益排行：按员工维度统计总收益/可用收益/不可用收益/已提现金额，支持排序和分页 |
| GET | `/adminapi/finance/staffFundWallet/overview` | 资金总览：总收益 + 可用收益 + 不可用收益（提现申请中） + 已提现金额 |
| GET | `/adminapi/finance/staffFundWallet/incomeTrend` | 收入趋势线图：按天累计 |

### 后台adminapi / pos --- 收银台组 / 收银台员工管理（4）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/pos/adminUserPos/update` | 更新收银台员工 |
| POST | `/adminapi/pos/adminUserPos/updateField` | 更新字段 |
| POST | `/adminapi/pos/adminUserPos/create` | 创建收银台员工 |
| GET | `/adminapi/pos/adminUserPos/info` | 获取收银台员工信息 |

### 后台adminapi / o2o --- 多门店 / 派送订单管理（4）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/o2o/deliveryOrder/dispatch` | 手动派单 |
| POST | `/adminapi/o2o/deliveryOrder/cancel` | 取消派送订单 |
| GET | `/adminapi/o2o/deliveryOrder/list` | 获取派送订单列表 |
| GET | `/adminapi/o2o/deliveryOrder/detail` | 获取派送订单详情 |

### 后台adminapi / finance --- 财务组 / 员工提现管理（3）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/finance/staffFundWithdraw/audit` | 审核提现申请（已打款 / 已驳回） |
| GET | `/adminapi/finance/staffFundWithdraw/list` | 提现申请列表（分页+筛选）；businessType 不传则混合展示配送员+服务员 |
| GET | `/adminapi/finance/staffFundWithdraw/detail` | 提现申请详情 |

### 后台adminapi / finance --- 财务组 / 服务员提现管理（3）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/finance/serverStaffWithdraw/audit` | 审核提现申请（已打款 / 已驳回） |
| GET | `/adminapi/finance/serverStaffWithdraw/list` | 提现申请列表（分页+筛选） |
| GET | `/adminapi/finance/serverStaffWithdraw/detail` | 提现申请详情 |

### 后台adminapi / finance --- 财务组 / 配送员提现管理（3）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/finance/deliveryStaffWithdraw/audit` | 审核提现申请（已打款 / 已驳回） |
| GET | `/adminapi/finance/deliveryStaffWithdraw/list` | 提现申请列表（分页+筛选） |
| GET | `/adminapi/finance/deliveryStaffWithdraw/detail` | 提现申请详情 |

### 后台adminapi / pos --- 收银台组 / 收银台订单结算（3）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/pos/order/check/update` | 更新 |
| POST | `/adminapi/pos/order/check/submit` | 提交订单 |
| POST | `/adminapi/pos/order/check/index` | 获取列表 |

### 后台adminapi / vendor --- 供应商组 / 供应商资金变化（3）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/vendor/vendorAccount/update` | 编辑 |
| POST | `/adminapi/vendor/vendorAccount/create` | 创建 |
| GET | `/adminapi/vendor/vendorAccount/detail` | 获取详情 |

### 前台api / o2o --- 多门店 / 配送员收款账号（C 端）（3）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/api/o2o/staffAccount/save` | 新增或修改收款账号 |
| POST | `/api/o2o/staffAccount/del` | 删除收款账号 |
| GET | `/api/o2o/staffAccount/list` | 查询个人收款账号列表 |

### 后台adminapi / promotion --- 营销组 / 微信直播（2）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/promotion/wechatLive/update` | 更新 |
| GET | `/adminapi/promotion/wechatLive/detail` | 获取详情 |

### 后台adminapi / finance --- 财务组 / 配送员资金管理 - 配送设置（2）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/finance/deliveryFundManagement/settings/save` | 保存配送设置 |
| GET | `/adminapi/finance/deliveryFundManagement/settings/detail` | 获取配送设置（9 项配置） |

### 后台adminapi / finance --- 财务组 / 员工资金日志（2）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| GET | `/adminapi/finance/staffFundWalletLog/walletDetail` | 查询某员工当前钱包余额（资金日志页右侧抽屉用） |
| GET | `/adminapi/finance/staffFundWalletLog/list` | 资金日志列表（分页+筛选）；businessType 不传则混合展示配送员+服务员流水 |

### 后台adminapi / pos --- 收银台组 / 收银台充值（2）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/pos/user/rechargeOrder/pay` | 充值支付 |
| GET | `/adminapi/pos/user/rechargeOrder/paymentList` | 充值支付方式 |

### 后台adminapi / pos --- 收银台组 / 收银台会员优惠券（2）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| GET | `/adminapi/pos/user/coupon/list` | 会员优惠券列表 |
| GET | `/adminapi/pos/user/coupon/getList` | 优惠券列表 |

### 后台adminapi / pos --- 收银台组 / 收银购物车管理（2）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/pos/cart/updateCheck` | 更新选中状态 |
| GET | `/adminapi/pos/cart/getCount` | 获取数量 |

### 后台adminapi / o2o --- 多门店 / 商品分配管理（2）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/o2o/productOrg/distribution` | 分配组织 |
| GET | `/adminapi/o2o/productOrg/info/{productId}` | 分配组织详情 |

### 后台adminapi / o2o --- 多门店 / 配送员钱包（后台）（2）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| GET | `/adminapi/o2o/serviceStaffWallet/logList` | 查询钱包流水分页 |
| GET | `/adminapi/o2o/serviceStaffWallet/detail` | 查询配送员钱包详情 |

### 后台adminapi / vendor --- 供应商组 / 供应商商品控制器（2）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/vendor/product/create` | 创建 |
| GET | `/adminapi/vendor/product/detail` | 供应商商品详情 |

### 前台api / o2o --- 多门店 / 服务人员（配送员 / 服务员端）（2）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/api/o2o/serviceStaff/updateWorkStatus` | 更新工作状态 |
| GET | `/api/o2o/serviceStaff/myInfo` | 获取个人信息详情 |

### 前台api / o2o --- 多门店 / O2O配置（前端）（2）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| GET | `/api/o2o/config/localDeliveryConfig` | 获取门店同城配送配置 |
| GET | `/api/o2o/config/homeServiceConfig` | 获取门店上门服务配置 |

### 后台adminapi / promotion --- 营销组 / 抽奖活动管理（1）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/promotion/lotteryActivity/updateStatus` | 更新活动状态 |

### 后台adminapi / finance --- 财务组 / 对账单（1）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| GET | `/adminapi/finance/statement/getStatementDownloadList` | 对账单下载列表 |

### 后台adminapi / finance --- 财务组 / 服务员资金管理 - 服务设置（1）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/finance/serverFundManagement/settings/save` | 保存服务设置 |

### 后台adminapi / buyershow --- 买家秀组 / 买家秀分组管理（1）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/promotion/buyerShow/group/del/{id}` | 删除买家秀分组 |

### 后台adminapi / pos --- 收银台组 / 收银会员（1）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| GET | `/adminapi/pos/user/getUserByMobile` | 通过手机号获取用户信息 |

### 后台adminapi / pos --- 收银台组 / 收银台订单预约（1）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| GET | `/adminapi/pos/order/OrderReservation/list` | 获取列表 |

### 后台adminapi / vendor --- 供应商组 / 供应商商品平台端控制器（1）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| GET | `/adminapi/vendor/product/platform/detail` | 供应商商品详情 |

### 前台api / user --- 会员中心 / 登录控制器（1）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/api/user/login/sendCode` | 发送短信验证码（自动化测试专用） |

### 前台api / promotion----营销 / 礼品卡（1）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| GET | `/api/promotion/giftCardGroups/showCardPassword` | 礼品卡密码显示 |
