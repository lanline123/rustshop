# 结算页 v2（价格引擎增强）

> 目标：结算页与下单金额口径一致，覆盖运费模板、积分抵扣、优惠券店铺范围与多店分摊、
> 账期支付、预售定金；秒杀/拼团/满减活动等营销引擎仍待独立排期。
>
> 代码：`crates/services/src/trade/check.rs`（结算）、`crates/services/src/trade/order.rs`（下单）、
> 纯函数：`crates/core/src/models/trade/check.rs`。

## 1. 已实现

| 能力 | 实现 | 验证 |
|---|---|---|
| 运费模板 | `product.shipping_tpl_id`（缺省回退店铺默认模板），`shipping_tpl + shipping_tpl_info`：包邮/满额包邮、按件/按重、首件首重 + 续件续重；地址地区命中区域，否则默认区域；自提（`deliveryType=2`）免运费 | 商品 136（35 元、2.5kg、模板首重 1kg/2 元 + 续重 1kg/1 元）→ 结算 `shippingFee=4`、下单 `order.shipping_fee=4`、`total=39.00` |
| 积分抵扣 | `integralScale`（每 100 积分抵多少元）、`integralPercent` + `integralPercentBaseAmount`（每 X 元最多用 Y 积分）、`usePoints` 开关；`availablePoints/usePoint/pointsAmount` 与 Java 同口径；下单扣减 `user.points` 并落 `order.use_points/points_amount` | 商品 238 元、积分 100 → `pointsAmount=1.00`、`availablePoints=114`；下单后 `user.points 500→400`、`order.points_amount=1.00` |
| 优惠券店铺范围 | `coupon.shop_id=0` 全局券；`shop_id>0` 仅限该店铺商品；门槛按适用店铺金额校验；不适用券进入 `disableCoupons`（`disableReason`） | 店铺 1 的券 900002 在店铺 0 购物车 → `disableCoupons` 命中且带原因 |
| 多店分摊 | 店铺券直接归店铺；全局券按各店商品金额比例分摊（最后一家承担舍入），`CartStoreTotal.discountCouponAmount` 合计等于 `total.couponAmount` | 全局券 900001（10 元）→ `couponAmount=10`、单店分摊 10 |
| 账期支付 | `useCredit` 配置 + `credit_user` 授信；返回 `creditPaymentInfo`（额度/已用/可用/账期类型/canUseCredit/disableReason）；`useCredit=1` 时 `creditAmount` 抵扣应付、与余额互斥 | `creditAmount=228.00`、`unpaidAmount=0.00` |
| 预售 | 读取 `product_presale`：全款/定金预售；定金按比例或固定金额（含向下取整）；返回 `productPresale`、`total.depositAmount/finalPaymentAmount` | 商品 264（定金预售，固定 5 元）→ `depositAmount=5.00`、`finalPaymentAmount=27.00` |
| 改价标记 | 购物车存在 `change_price_data` → `hasChangePrice=true` | 单元测试/字段透出 |
| 支付/配送回显 | 支付方式、配送方式、`shippingType` 表单原样回显 | 结算页联调 |

## 2. 金额公式（与 Java `getTotalFee` 对齐）

```
优惠券   = Σ 每张券（满减金额 / 折扣，封顶 max_order_amount，且不超过适用金额）
积分金额 = min(使用积分, 用户积分, 积分价值上限, 支付比例上限) × integralScale / 100
totalAmount = 商品金额 + 运费 - 优惠券 - 积分金额
unpaidAmount = totalAmount - 余额抵扣 - 账期抵扣
```

下单侧使用同一组纯函数（`calc_shipping_fee` / `calc_points` / `coupon_rule_discount`），
订单落库：`total_amount/product_amount/coupon_amount/points_amount/shipping_fee/use_points/balance`。

## 3. 仍待实现

- **活动价格**：秒杀/拼团/满减（`discountSeckillAmount` 等恒 0）、商品促销价规则；
- **O2O**：配送方式明细 `deliveryOption`、自提点/起送价校验、骑手配送；
- **发票**：结算页发票信息随单落库（当前仅 `orderInvoice` 查询/申请）；
- **代客下单整单改价**：改价金额与运费分摊（`hasChangePrice` 仅标记）；
- **税费**：`taxRate/taxRateAmount` 恒 0（地区税率表未接）；
- **定金预售下单**：定金订单创建/尾款支付状态机（当前结算页仅展示定金金额，下单仍按全款应付）。

## 4. 回归

```powershell
# 种子（MySQL，库 tigshop-java）
mysql ... -e "source rust/tools/compat/seed_checkout_v2.sql"

# 结算/下单冒烟（会员 testbuyer / 商品 136、4、264）
powershell -NoProfile -ExecutionPolicy Bypass -File rust/tools/compat/checkout_v2_smoke.ps1
```

实测输出（2026-10-19）：

```
A shipping: product=35.00 fee=4.00 total=39.00 storeFee={"0":"4.00"}
B points/coupon: product=238.00 coupon=10.00 points=1.00 availablePoints=114 usedPoint=100 total=227.00 unpaid=227.00
C coupon scope: enable=1 disable=1 disabledIds=900002 shopCouponDisabled=True
D credit: canUse=True available=1000.00 creditAmount=228.00 unpaid=0.00
E presale: saleMode=2 deposit=5.00 final=27.00 productAmount=32.00
F order: id=10 total=227.00 online=227.00     # 落库 points_amount=1.00 / use_points=100 / coupon=10 / 积分扣减
G order: id=11 total=39.00                     # 运费 4.00 随单落库
```


## 限时折扣接入（2026-10-20）

结算 `check` 与下单 `order` 在秒杀/拼团价基础上叠加 `time_discount`：
取当前进行中活动的最低价（折扣率/立减/固定价），累计到
`discountTimeDiscountAmount` 并计入应付与税费基数；下单落 `order_item.price`。
