# task-43 调研交接（payment/recharge_order + apple_pay + profit_sharing + sub_merchant）

> 来源：catalog-pricing 在 task-43 中完成调研后上下文耗尽，仅落地
> `crates/core/src/models/payment/recharge_order.rs`（models 层，未注册）。
> 接手者可直接复用该文件，并按下列契约补 repo/service/route。

## 1. payment/recharge_order（public，7）`/api/user/rechargeOrder/*`
- list：`user_withdraw_apply(wallet_type=0)` + `user_recharge_order` **合并**，按 addTimeLong 倒序分页，状态过滤 `status != -1`。
- setting：`recharge_setting` `is_show=1` 分页（request.isShow 未使用）。
- paymentList：`getAvailablePayment("recharge")`（无 offline）。
- update：amount 命中 `recharge_setting.money` 精确行→discount；否则 `id=recharge_id AND is_show=1`；缺失→「充值金额错误」；落库 postscript「会员充值」、add_time=now，返回 orderId。
- pay：orderId/userId 校验→订单存在（「充值申请不存在」）且 `status != 1`（「充值申请已支付」），返回 UserRechargeOrderPayVO + user（「用户不存在」）。
- checkStatus：同上校验；status>0 直接返回；否则 paylog(orderType=1) 口径（微信付款码轮询分支未迁移）。
- create：type 为空「未选择支付方式」；cash「请重新选择充值支付方式」；其余本地校验后返回渠道未迁移错误。
- i18n：error.param.missing=参数缺失 等（zh 值已在调研中确认）。

## 2. payment/apple_pay（public，4）
- `POST /api/order/pay/applepay/{validateMerchant,confirm,confirmByPaymentMethod}` + `POST /api/order/pay/notify/ums`，全部 optional auth。
- 三个确认/校验端点：paySn→paylog 归属校验（找不到「找不到原支付记录」；order_type=1 走充值单归属，否则订单归属；越权「订单不存在」；未登录「用户未登录」），随后 ApplePayService 验签/PSP → 未迁移错误。
- notify/ums：payload 非空校验后未迁移（无法验签）。

## 3. payment/profit_sharing（admin，3）
- list：`profit_sharing_order` 分页（settlementType/shopId/vendorId/status/finishStatus；status 支持数字或枚举名大小写，空不过滤）；VO 回填 shopName/vendorName、statusDesc/finishStatusDesc/triggerType/canFinish/canCancel/createTime/modifyTime。
- submitBatch：settlementIds 空→「订单ID列表不能为空」；逐 id 校验 order 存在（「订单不存在: {id}」）；随后渠道分账 API→未迁移。
- manualFinish：「分账单不存在」/「分账单已完结, 无需重复操作」；canCancel=status=0(PENDING) 且 transaction_id 空→本地作废（status=4 CANCELLED, finish_status=1, finish_time, operator, result_desc「手动作废: 未发起过渠道分账」；条件更新失败「分账状态已变化，不能作废，请刷新后重试」）；status 非 SUCCESS/PROCESSING→「分账单当前状态不允许完结, status={status}」；transaction_id 空→「分账单缺少transactionId, 无法完结」；否则渠道 finishOrder→未迁移。
- 枚举：PENDING0/PROCESSING1/SUCCESS2/FAILED3/CANCELLED4；UNFINISHED0/FINISHED1；MANUAL1/AUTO2。

## 4. payment/sub_merchant（admin，3）
- list：`sub_merchant` 分页（shopId/payChannel eq/status eq/subMchId eq）+ shopName 回填（shop_id=0→「自营店铺」，O2O 形态「自营门店」）。
- bind：channel 默认 WECHAT；subMchId 空→「子商户号不能为空」；随后渠道 isSubMerchantOwned（「子商户号[..]不在当前服务商名下…」）→本地校验后未迁移（不落库）。
- unbind：完全 DB 可实现——getById 缺失→「子商户不存在」，否则 status=FROZEN(2) + modify_time 更新。
- 枚举：INACTIVE0/ACTIVE1/FROZEN2（待激活/已激活/已冻结）。

## 表
recharge_setting(5 列) / profit_sharing_order(28 列, i64 id, datetime 字符串列) / sub_merchant(12 列, i64)——实体已存在（finance.rs / promotion.rs），用 `schema_parser --table` 复核列名。

## 注册（Lead）
4 条 register_feature：
- recharge_order：`--ctx payment --feature recharge_order --service-type RechargeOrderService --layers models,repo,service,public_route`
- apple_pay：`--ctx payment --feature apple_pay --service-type ApplePayService --layers models,repo,service,public_route`
- profit_sharing：`--ctx payment --feature profit_sharing --service-type ProfitSharingService --layers models,repo,service,admin_route`
- sub_merchant：`--ctx payment --feature sub_merchant --service-type SubMerchantService --layers models,repo,service,admin_route`
