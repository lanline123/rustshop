# Rust 接口实现审计（对照 Tigshop Java Apifox 文档）

- 文档接口（HTTP，按 method+path 去重）：1600
- Rust 源码中能对应到：1556
- 未找到对应实现：44

> 说明：这是静态审计。判断依据是 Rust 源码中是否出现该路径（含去掉 `/api`、`/adminapi` 前缀后的形式）。
> “未找到”不一定是完全没写，也可能是路径由常量拼接、特征开关或尚未迁移。

## 缺失分布

| 分组 | 数量 |
| --- | --- |
| 后台adminapi / authority --- 权限组 / 供应商管理 | 7 |
| 后台adminapi / vendor --- 供应商组 / 供应商员工管理 | 5 |
| 前台api / cyclecard --- 次卡组 / 次卡权益 | 5 |
| 后台adminapi / pos --- 收银台组 / 收银台员工管理 | 4 |
| 后台adminapi / pos --- 收银台组 / 收银台订单结算 | 3 |
| 后台adminapi / vendor --- 供应商组 / 供应商资金变化 | 3 |
| 后台adminapi / promotion --- 营销组 / 微信直播 | 2 |
| 后台adminapi / pos --- 收银台组 / 收银台充值 | 2 |
| 后台adminapi / pos --- 收银台组 / 收银台会员优惠券 | 2 |
| 后台adminapi / pos --- 收银台组 / 收银购物车管理 | 2 |
| 后台adminapi / vendor --- 供应商组 / 供应商商品控制器 | 2 |
| 后台adminapi / promotion --- 营销组 / 抽奖活动管理 | 1 |
| 后台adminapi / buyershow --- 买家秀组 / 买家秀分组管理 | 1 |
| 后台adminapi / pos --- 收银台组 / 收银会员 | 1 |
| 后台adminapi / pos --- 收银台组 / 收银台订单预约 | 1 |
| 后台adminapi / vendor --- 供应商组 / 供应商商品平台端控制器 | 1 |
| 前台api / user --- 会员中心 / 登录控制器 | 1 |
| 前台api / promotion----营销 / 礼品卡 | 1 |

## 缺失接口明细

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

### 后台adminapi / pos --- 收银台组 / 收银台员工管理（4）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/pos/adminUserPos/update` | 更新收银台员工 |
| POST | `/adminapi/pos/adminUserPos/updateField` | 更新字段 |
| POST | `/adminapi/pos/adminUserPos/create` | 创建收银台员工 |
| GET | `/adminapi/pos/adminUserPos/info` | 获取收银台员工信息 |

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

### 后台adminapi / promotion --- 营销组 / 微信直播（2）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/promotion/wechatLive/update` | 更新 |
| GET | `/adminapi/promotion/wechatLive/detail` | 获取详情 |

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

### 后台adminapi / vendor --- 供应商组 / 供应商商品控制器（2）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/vendor/product/create` | 创建 |
| GET | `/adminapi/vendor/product/detail` | 供应商商品详情 |

### 后台adminapi / promotion --- 营销组 / 抽奖活动管理（1）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/promotion/lotteryActivity/updateStatus` | 更新活动状态 |

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
