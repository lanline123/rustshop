# B3 线下支付认款实现记录

## 状态

```text
代码：核心链路已完成
编译：cargo check 通过
数据库：production 已建 offline_payment_claim
部署：未部署
```

## 数据表

```text
offline_payment_claim
```

字段：

```text
claim_id / order_id / order_sn / user_id / shop_id
claimed_amount / pay_stage
transfer_time / payer_name / payer_account / bank_serial_no / voucher_urls
remark / status / review_admin_id / review_time / paid_time
audit_remark / reject_reason / add_time / update_time
```

状态：

```text
0 待审核
1 审核通过
2 已驳回
```

阶段：

```text
0 全款
1 定金
2 尾款
```

## 公开端

```text
POST /api/user/order/offlinePaymentClaim/submit
GET  /api/user/order/offlinePaymentClaim/detail?orderId=
GET  /api/user/order/offlinePaymentClaim/history?orderId=
```

规则：

```text
订单必须属于当前用户
payTypeId = 3（线下支付）
payStatus = 0（未支付）
orderStatus = 0（待付款）
已有待审核认款或已通过未入账 → 拒绝重复提交
凭证 URL 不能为空
```

阶段金额：

```text
普通订单：unpaidAmount
定金预售：depositAmount - order.balance
尾款：unpaidAmount
```

## 管理端

```text
GET  /adminapi/order/offlinePaymentClaim/list
GET  /adminapi/order/offlinePaymentClaim/detail?claimId=
POST /adminapi/order/offlinePaymentClaim/audit
```

审核通过：

```text
校验认款阶段和金额仍与订单一致
认款单状态置为 1
普通订单 / 尾款：on_payment_success 入账
定金：settle_presale_deposit + update_presale_deposit_paid
写入 paid_time
写订单日志
```

审核驳回：

```text
必须填写驳回原因
状态置为 2
记录 reject_reason / audit_remark
写订单日志
```

## 验证

```text
cargo check -p tigshop-services -p tigshop-admin-api -p tigshop-api
Finished
```

## 未完成

```text
完整二进制 build
部署
RabbitMQ 异步入账（当前同步入账）
站内消息 / 后台消息 / 支付成功通知
管理端列表返回认款单附加信息
PC 订单详情/收银台认款 UI
```

## 取舍说明

v5.8.61 Java 通过 RabbitMQ 异步完成入账，Rust 当前采用同步入账，语义等价但少了消息重试链路。后续可替换为任务队列。
