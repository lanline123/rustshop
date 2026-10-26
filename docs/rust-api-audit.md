# Rust 接口实现审计（对照 Tigshop Java Apifox 文档）

- 文档接口（HTTP，按 method+path 去重）：1600
- Rust 源码中能对应到：1587
- 未找到对应实现：13

> 说明：这是静态审计。判断依据是 Rust 源码中是否出现该路径（含去掉 `/api`、`/adminapi` 前缀后的形式）。
> “未找到”不一定是完全没写，也可能是路径由常量拼接、特征开关或尚未迁移。

## 缺失分布

| 分组 | 数量 |
| --- | --- |
| 后台adminapi / authority --- 权限组 / 供应商管理 | 7 |
| 后台adminapi / promotion --- 营销组 / 微信直播 | 2 |
| 后台adminapi / promotion --- 营销组 / 抽奖活动管理 | 1 |
| 后台adminapi / buyershow --- 买家秀组 / 买家秀分组管理 | 1 |
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

### 后台adminapi / promotion --- 营销组 / 微信直播（2）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/promotion/wechatLive/update` | 更新 |
| GET | `/adminapi/promotion/wechatLive/detail` | 获取详情 |

### 后台adminapi / promotion --- 营销组 / 抽奖活动管理（1）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/promotion/lotteryActivity/updateStatus` | 更新活动状态 |

### 后台adminapi / buyershow --- 买家秀组 / 买家秀分组管理（1）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/adminapi/promotion/buyerShow/group/del/{id}` | 删除买家秀分组 |

### 前台api / user --- 会员中心 / 登录控制器（1）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| POST | `/api/user/login/sendCode` | 发送短信验证码（自动化测试专用） |

### 前台api / promotion----营销 / 礼品卡（1）

| 方法 | 路径 | 名称 |
| --- | --- | --- |
| GET | `/api/promotion/giftCardGroups/showCardPassword` | 礼品卡密码显示 |
