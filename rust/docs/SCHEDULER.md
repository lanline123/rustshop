# 定时任务 / 延迟任务说明（已实测）

> Java 用 RabbitMQ 延迟队列（`x-delay`）+ `RabbitMqConsumer` 实现订单超时取消、
> 自动收货、自动售后、分账等延迟逻辑。Rust 侧**不依赖 MQ**，改为
> 「轮询扫描 + 幂等条件更新」，单实例即可运行；多实例后续可加 Redis 锁或换 MQ。

## 1. 已实现任务

| 任务 | 默认节奏 | 逻辑 | 配置项 |
|---|---|---|---|
| `order_auto_cancel` | 每 60s | 扫描 `pay_status=1 AND order_status=0 AND add_time <= now-autoCancelOrderMinute*60`，调用订单取消（状态条件更新 + 回滚库存） | `config.autoCancelOrderMinute`（分钟） |
| `order_auto_receive` | 每 300s | 扫描 `order_status=2 AND shipping_time <= now-autoDeliveryDays*86400`，置 `order_status=5`、写 `received_time` | `config.autoDeliveryDays`（天，支持小数） |
| `groupon_expire` | 每 60s | 扫描待成团且超过截止时间的开团记录，整团置失败并释放 | — |
| `credit_user_period_update` | 每 3600s | 按授信账期推进 `credit_user.period_start_time` | `TIG_SCHEDULER__CREDIT_PERIOD_SECS` |
| `product_auto_status` | 每 60s | 扫描 `product_auto_status` 到期任务，执行商品上下架（含卡密组启用校验） | `TIG_SCHEDULER__PRODUCT_AUTO_SECS` |
| `aftersales_auto_receive` | 每 3600s | 扫描回寄后超过 `autoConfirmReceiptDays` 的售后，系统自动确认收货 | `TIG_SCHEDULER__AFTERSALES_RECEIPT_SECS` |

- 配置为空/≤0 时任务跳过（与 Java 默认关闭一致）。
- 任务幂等：状态用 `WHERE` 条件更新，重复执行不会重复回滚库存/重复收货。

## 2. 环境变量（运维/测试）

| 变量 | 默认 | 说明 |
|---|---|---|
| `TIG_SCHEDULER__ENABLED` | true | `false`/`0` 关闭全部定时任务 |
| `TIG_SCHEDULER__DELAY_SECS` | 30 | 启动后首次延迟 |
| `TIG_SCHEDULER__CANCEL_SECS` | 60 | 取消任务扫描间隔 |
| `TIG_SCHEDULER__RECEIVE_SECS` | 300 | 自动收货扫描间隔 |
| `TIG_SCHEDULER__CREDIT_PERIOD_SECS` | 3600 | 授信账期扫描间隔 |
| `TIG_SCHEDULER__PRODUCT_AUTO_SECS` | 60 | 商品自动上下架扫描间隔 |
| `TIG_SCHEDULER__AFTERSALES_RECEIPT_SECS` | 3600 | 售后自动确认收货扫描间隔 |
| `TIG_SCHEDULER__DEPOSIT_DUE_SECS` | 86400 | 保证金到期扫描间隔 |

启动日志：`定时任务已启动：订单超时取消 / 自动收货 / 拼团过期 / 账期更新 / 商品自动上下架`。

## 3. 实测（本地 fixture）

1. 配置 `autoCancelOrderMinute=1`、`autoDeliveryDays=0.5`；
2. 造 1 笔 `add_time=now-7200` 未付款订单、1 笔 `shipping_time=now-50000` 已发货订单；
3. 以 `TIG_SCHEDULER__DELAY_SECS=3`、`*_SECS=5` 启动；
4. 结果：
   ```
   超时取消订单 3 笔      （含 fixture SCHEDTEST1 → order_status=3）
   自动确认收货 1 笔      （SCHEDTEST2 → order_status=5, received_time 已写）
   ```

## 4. 未迁移的定时/延迟任务（后续）

仍依赖外部能力的任务保持未迁移或仅有接口占位：

- `WechatLiveOrderSyncTask`（视频号订单同步，依赖微信 API 凭据）
- ✅ `DepositDueCheckTask`（已实现基础双缓冲扫描：处罚/恢复/全店下架）
- `ProfitSharingScheduleTask`（分账调度，依赖支付渠道）
- 退款结算、自动评价等 MQ 消费链路（售后自动确认收货已完成）

实现时可直接复用 `crates/services/src/system/scheduler.rs` 的轮询框架。
