# 活动选价 / 秒杀引擎 / 定金预售（v3 切片）

> 本轮目标（用户指定）：秒杀/拼团/满减活动引擎、购物车/下单层活动选价、库存与限购、拼团记录状态机、定金预售下单/尾款。
>
> **实际交付**：秒杀引擎（选价 + 库存 + 限购，端到端）与定金预售（下单 + 定金/尾款支付 + 后台确认 + 渠道回调结算，端到端）。
> **未交付**：拼团引擎、满减引擎（原因与下一步见 §4）。

## 1. 秒杀引擎（已完成）

### 选价（结算页 + 下单共用）

- 数据源：`seckill` + `seckill_item`（时间窗、`seckill_price`、`seckill_stock/seckill_sales`、`seckill_limit_num`）。
- 结算页 `order/check/index|update|updateCoupon` 自动匹配勾选商品的进行中秒杀：
  - `price = min(product.product_price, seckill_price)`；`CartVO.price` 为秒杀价、`productPrice/originPrice` 为原价；
  - 返回 `total.discountSeckillAmount`、`cartList[].total.discountSeckillAmount`；
  - 总价按秒杀价计算（`productAmount` 已含活动价，另给出优惠额）。
- 下单 `order/check/submit`：同一映射取价，订单明细 `price=秒杀价 / origin_price=原价`，
  `order.discount_amount` 记录活动优惠额，`total_amount` 为秒杀后应付。

### 库存与限购

- 事务内 `UPDATE seckill_item SET seckill_stock=seckill_stock-?, seckill_sales=seckill_sales+?
  WHERE rec_id=? AND seckill_stock-seckill_sales>=?`（防超卖；失败整体回滚）。
- 限购：`seckill_limit_num > 0` 时，统计活动窗口内该用户已购数量（`order_item JOIN order`），
  超出返回 `1001 超过秒杀商品限购数量`。

### 实测（`tools/compat/seckill_smoke.ps1`，商品 6：78 元 → 秒杀 50 元、库存 5、限购 1）

```
A checkout: productAmount=50.00 seckillDiscount=28.00 total=50.00 price=50.00
B submit:   id=15 total=50.00
C limit:    rejected -> {"code":1001,"msg":"超过秒杀商品限购数量"}
DB: order15 total=50 / discount_amount=28；order_item price=50 origin_price=78；
    seckill_item stock 5→4, sales 0→1
```

## 2. 定金预售（已完成）

### 下单

- 单商品且 `product.sale_mode=2` + `product_presale` 配置时创建预售订单：
  - `order.is_presale=1 / presale_type=2 / total_amount=全款 / unpaid_amount=定金`（本次应付定金）；
  - 同事务写 `order_presale`：`deposit_amount/final_payment_amount/presale_status=0/delivery_*`；
  - 定金计算：比例（`deposit_ratio`，可向下取整）或固定金额（`deposit_fixed_amount`），上限为应付金额。
- 预售价与优惠券/积分/运费同口径计算；响应 `onlineAmount=定金`。

### 支付与状态机

```
order_presale.presale_status: 0 待付定金 → 1 待付尾款 → 2 已完成
```

- **线下汇款/后台确认**：`pay/create?type=offline` 按订单当前 `unpaid` 生成支付单（定金/尾款自动切换）；
  `order/order/setPaid` 两阶段推进：定金确认 → `paid_amount+=定金、unpaid=尾款、status=1`；
  尾款确认 → 订单全额付清、`status=2`；同步 paylog 状态。
- **余额支付**：定金阶段付清后自动进入「待付尾款」，尾款付清后自动完成预售。
- **在线渠道回调**：`paylog.settle_order_by_pay_sn` 已接入微信/支付宝 notify，
  定金/尾款到账后推进订单与预售状态（顺带修复普通订单在线回调不更新订单状态的问题）。

### 实测（`tools/compat/presale_smoke.ps1`，商品 264：32 元、定金 5 元）

```
1 submit: id=14 total=32.00 online=5.00
2 deposit paylog amount=5.00
3 deposit confirmed
4 final paylog amount=27.00
5 final confirmed
DB: order14 total=32 paid=32 unpaid=0 pay_status=2；order_presale status=2
    paylogs 5.00 / 27.00 均 pay_status=1
```

## 3. 本轮新增接口/数据

- 仓储：`seckill.find_active_by_products / count_user_purchases / dec_stock_inc_sales_in_tx`；
  `payment.pay.insert_presale_in_tx / update_presale_deposit_paid / update_presale_final_paid / settle_presale_deposit`。
- 服务：`trade::check::load_seckill_prices`（结算/下单共用）、`trade::order` 预售下单/秒杀库存扣减、
  `payment.balance_pay` 预售两阶段、`payment.paylog.settle_order_by_pay_sn`。
- 字段：订单 `discount_amount` 参与下单落库；`order_presale` 全字段写入。

## 4. 未完成与下一步

### 拼团引擎（未交付）

- 现状：`groupon_promotion`（普通团/阶梯团）、`groupon_promotion_item`、`groupon_promotion_detail`
  （`group_price` 阶梯 JSON）、`groupon_promotion_record`（开团/参团记录）表与部分仓储已存在；
  `groupon_records` 仅客户端列表查询。
- 未做的原因：拼团需要「活动上下文」（开团/参团、`openGroupRecordId`、成团人数/阶梯价、过期失败退款），
  当前结算/下单参数与购物车不携带拼团上下文，需先对齐 Java 前端入参与 cart 流程，避免自动成团算错价。
- 下一步：按 `join_type/open_group_record_id/min_group_num/join_expiration_time` 状态机实现
  `open_group / join_group / mark_success / expire_fail`，下单事务内创建记录并校验人数/阶梯价。

### 满减引擎（未交付）

- `product_promotion` 表（`promotion_type/promotion_type_data/rules_type/unit/range_data`）当前无种子数据，
  Java 规则 JSON 语义需要从原 `ProductPromotionServiceImpl` 逐条确认（满额/满件、阶梯、叠加规则）。
- 下一步：先实现纯函数规则解析 + 单测，再接购物车店铺维度试算与下单。

### 其他限制

- 预售尾款支付时间窗校验、超时未付尾款自动取消/退款、预售取消库存返还尚未接入定时任务；
- 渠道回调依赖真实凭据（当前用线下/余额链路验证状态机）；拼团退款/失败自动处理未做。

## 5. 回归

```powershell
mysql ... tigshop-java -e "source rust/tools/compat/seed_seckill.sql"
powershell -NoProfile -ExecutionPolicy Bypass -File rust/tools/compat/seckill_smoke.ps1

mysql ... tigshop-java -e "source rust/tools/compat/seed_presale.sql"
powershell -NoProfile -ExecutionPolicy Bypass -File rust/tools/compat/presale_smoke.ps1
```
