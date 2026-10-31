# B8 主流程修复实现记录

## 已完成

### 商户进件

```text
GET  /adminapi/finance/merchantApplication/localMedia
GET  /adminapi/finance/merchantApplication/wechatBanks
GET  /adminapi/finance/merchantApplication/wechatBankBranches
GET  /adminapi/finance/merchantApplication/wechatBankCities
GET  /adminapi/finance/merchantApplication/wechatBankNames
GET  /adminapi/finance/merchantApplication/wechatBankProvinces
POST /adminapi/finance/merchantApplication/abandon
POST /adminapi/finance/merchantApplication/delete
POST /adminapi/finance/merchantApplication/reapply
POST /adminapi/finance/merchantApplication/retry
```

银行列表暂返回空数组；放弃/删除/重新申请/重试更新 `merchant_application` 状态与审计字段。

### 供应商

补齐 GET 方法别名：

```text
GET /vendor/vendorWithdraw/list
GET /vendor/vendorShopBind/list
GET /vendor/adminVendorUser/list
GET /vendor/product/platform/pageList
GET/POST /vendor/stock/getImportErrorData
```

新增更新：

```text
POST /vendor/vendor/update
POST /vendor/product/update
```

### 礼品卡

```text
POST /adminapi/promotion/giftCards/create
POST /adminapi/promotion/giftCards/update
```

### AI 图片 / 地图

```text
GET  /adminapi/file/aiImage/getTranslateConfig
POST /adminapi/file/aiImage/translate
POST /adminapi/file/aiImage/inpaint
POST /adminapi/file/aiImage/localesList
GET  /adminapi/setting/config/aiImageSettings
POST /adminapi/setting/config/saveAiImage
GET  /adminapi/setting/config/amapSettings
POST /adminapi/setting/config/saveAmapSettings
```

翻译无凭据时原样返回源文本；图片修复明确返回未配置。

### 商品原图下载

```text
GET /api/product/product/downloadRawImages
```

路由已注册；压缩包实现仍返回“未迁移”。

## 验证

```text
cargo check -p tigshop-admin-api -p tigshop-api
Finished
```

路由差异：

```text
B7：missing=36
B8：missing=9
```

剩余 9 条多为静态扫描误报：

```text
stripe/xendit/antom 设置（动态注册）
vendor saleStatus / stock 导入错误（同路径 GET+POST）
order/pay/notify/stripe（方法不一致）
```

## 提交

```text
55944eda feat: fill B8 merchant application, vendor, AI image, and gift card gaps
```

## 未完成

```text
商品原图压缩包实际生成
微信银行列表 / 本地媒体真实数据
AI 翻译与去水印/修复服务
订单支付通知方法对齐
完整二进制 build / 部署
```

## 2026-10-31 补充：商品原图 ZIP

已实现：

```text
GET /api/product/product/downloadRawImages?id=
```

流程：

```text
登录校验（未登录 401）
读取商品主图/原图/大图/缩略图
读取 product_gallery
相对 URL 按请求 Host 补全
逐张下载（单张≤20MB，最多120张）
写入内存 ZIP
返回 application/zip + Content-Disposition
```

依赖：

```text
zip 2.5
reqwest 已有
```

验证：

```text
cargo check -p tigshop-api
cargo test -p tigshop-api --lib
131 passed
```

## 2026-10-31 补充：礼品卡 Excel

已实现：

```text
GET  /promotion/giftCards/importTemplate   xlsx 模板
GET  /promotion/giftCards/exportList       xlsx 导出
POST /promotion/giftCards/importList       xlsx 导入
```

导入规则：

```text
表头：卡号 / 密码
卡号长度 <8 失败
密码长度 6-16 失败
重复卡号失败
成功数累加 availability_stock
返回 successCount / failCount / failList
```

## 2026-10-31 补充：AI 图片翻译

配置键对齐 Java：

```text
aiImageEnabled
aiImageAuthorization
aiImageProjectId
aiImageTranslateProductText
```

接口：

```text
GET  /file/aiImage/getTranslateConfig  返回 sourceLanguageList
POST /file/aiImage/translate           调用蜂鸟 /api/v1/img/translate
POST /file/aiImage/localesList         已接真实 locales
```

翻译流程：

```text
校验启用/Authorization/ProjectId
imgUrls 1-10 张
逐张调用蜂鸟翻译
下载 translatedUrl
转存 gallery_pic
返回 results（originalUrl/translatedUrl/success/errorMsg）
```

仍待补：

```text
AI 智能消除（OCR + 遮罩 + eliminate + 轮询）
```

### AI 智能消除

```text
POST /file/aiImage/inpaint
```

流程：

```text
蜂鸟 OCR 创建任务
轮询 OCR 结果 texts
下载原图
生成黑底白框遮罩 PNG
遮罩上传 gallery
蜂鸟 eliminate 创建任务
轮询消除结果
结果图转存 gallery
```

## 2026-10-31 补充

### Antom 回调验签

```text
payload = METHOD + " " + path + "\n" + clientId + "." + requestTime + "." + body
SHA256withRSA
signature 头 URL 解码后 base64
公钥 antomPublicKey
```

处理：

```text
notifyType 含 REFUND -> 按 refundRequestId 更新退款状态
PAYMENT_RESULT + resultStatus=S -> paymentRequestId 截取 paySn -> mark_paid
```

### 电子卡券模板

```text
GET /product/eCardGroup/list?isDownload>0
返回 xlsx：卡券名称 / 卡号密码
```

## 2026-10-31 补充：微信支付 v3 直连下单

`GET /api/order/pay/create` 的 `wechat*` 分支已实现：

```text
pc          -> /v3/pay/transactions/native  code_url
h5          -> /v3/pay/transactions/h5      h5_url
wechat      -> /v3/pay/transactions/jsapi   prepay_id + JSAPI 签名
app         -> /v3/pay/transactions/app     prepay_id + APP 签名
```

请求：

```text
V3 待签名串 METHOD\nPATH\nTIMESTAMP\nNONCE\nBODY\n
Authorization: WECHATPAY2-SHA256-RSA2048
```

前置：

```text
订单归属/未支付校验
创建待支付 paylog
pcDomain 回调地址
微信配置：mchid/appId/serial/privateKey/publicKey/apiV3Key
```

依赖真实商户凭据与证书。

## 2026-10-31 补充：支付宝下单

`GET /api/order/pay/create` 的 `alipay/ali` 分支已实现：

```text
pc  -> alipay.trade.page.pay  HTML 自动提交
h5  -> alipay.trade.wap.pay   HTML 自动提交
app -> alipay.trade.app.pay   orderInfo 签名串
```

参数：

```text
app_id / method / format / charset / sign_type=RSA2
timestamp / version / notify_url / biz_content
```

签名：

```text
key 升序 k=v&...
SHA256withRSA
```

前置：

```text
创建待支付 paylog
pcDomain 生成 notify_url
支付宝配置：alipayAppid / alipayRsaPrivateKey / alipayRsaPublicKey
```

## 2026-10-31 补充：PayPal 下单

`GET /api/order/pay/create` 的 `paypal` 分支已实现：

```text
POST /v1/oauth2/token  client_credentials
POST /v2/checkout/orders intent=CAPTURE
purchase_units.amount.currency_code/value
invoice_id=paySn
application_context.return_url/cancel_url
返回 links[rel=approve].href
```

## 2026-10-31 补充：Apple Pay 支付面板

`GET /api/order/pay/create` 的 `applepay` 分支已实现：

```text
创建待支付 paylog
读取 applePayDisplayName / CountryCode / Currency / SupportedNetworks
返回 applePay:
  paySn / countryCode / currencyCode / merchantDisplayName
  supportedNetworks / merchantCapabilities / total
```

## 2026-10-31 补充：Stripe / Xendit 下单

Stripe：

```text
POST https://api.stripe.com/v1/checkout/sessions
mode=payment
line_items[0][price_data][currency/unit_amount/product_data][name]
metadata[pay_sn]/metadata[order_id]
payment_intent_data[metadata][...]
返回 url
```

Xendit：

```text
POST {xenditBaseUrl}/v2/invoices
external_id=paySn
amount/currency/description
success_redirect_url/failure_redirect_url
invoice_duration/payment_methods/items
返回 invoice_url，并回写 paylog.transaction_id
```

## 2026-10-31 补充：Antom 下单

```text
POST {antomGatewayUrl}/ams/api/v1/payments/createPaymentSession
```

请求：

```text
productCode=CASHIER_PAYMENT
productScene=CHECKOUT_PAYMENT
paymentRequestId=paySn-{8位}
paymentAmount/order/goods
paymentNotifyUrl/paymentRedirectUrl
```

签名：

```text
POST /ams/api/v1/payments/createPaymentSession\n
clientId.requestTime.body
SHA256withRSA
header: algorithm=RSA256,keyVersion=1,signature=...
```

返回 normalUrl，并回写 paylog.transaction_id。

## 2026-10-31 补充：用户登出令牌吊销

```text
POST /api/user/user/logout
```

实现：

```text
读取 Authorization Bearer
解析 JWT exp
写入进程内吊销表
require_auth 每次校验吊销表
过期项自动清理
```

说明：

```text
单实例部署可用；多实例需换 Redis 黑名单
```

## 2026-10-31 补充：会员消费排行导出

```text
GET /adminapi/panel/statisticsUser/userConsumptionRanking?isExport=1
```

返回 xlsx：

```text
会员名称 / 手机号 / 订单数 / 消费总额
```

非导出仍返回 JSON 分页。

## 2026-10-31 补充：渠道退款方法

`PayService::refund_via_channel` 按 `pay_code` 分发：

```text
stripe  -> POST /v1/refunds
paypal  -> OAuth2 + POST /v2/payments/captures/{captureId}/refund
xendit  -> POST /refunds（X-IDEMPOTENCY-KEY=refund_sn）
```

依赖：

```text
paylog.transaction_id（Stripe payment_intent / PayPal captureId / Xendit invoice_id）
渠道配置
```

说明：退款审核流程的最终调用点尚未接入，本轮先落渠道方法。

## 2026-10-31 补充：退款审核接入渠道

`RefundApplyService::audit(Processing)` 现在：

```text
校验可退额度
状态改为处理中
查找订单最后一笔成功 paylog
仅 stripe / paypal / xendit 走自动退款
创建 paylog_refund
写 refund_apply_paylog_refund 关联
回写 refund_apply.paylog_refund_id
调用 refund_via_channel
同步成功（SUCCESS/COMPLETED）则 mark_processed
```

其它渠道保持处理中，等待人工或后续补渠道方法。

## 2026-10-31 补充：微信/支付宝退款

微信 v3：

```text
POST /v3/refund/domestic/refunds
out_trade_no / out_refund_no
amount.refund / amount.total / currency
notify_url=/api/order/pay/refundNotify/wechat
```

支付宝：

```text
method=alipay.trade.refund
out_trade_no / refund_amount / out_request_no=refund_sn
RSA2 签名 POST gateway
```

退款审核自动处理渠道扩展为：

```text
wechat / alipay / stripe / paypal / xendit
```

## 2026-10-31 补充：Antom 退款

```text
POST {antomGatewayUrl}/ams/api/v1/payments/refund
refundRequestId=refundSn
paymentId=paylog.transaction_id
refundAmount.currency/value
refundNotifyUrl
```

签名同下单。退款审核自动处理渠道扩展为：

```text
wechat / alipay / stripe / paypal / xendit / antom
```

## 2026-10-31 补充：管理员登出吊销

```text
POST /adminapi/user/user/logout
```

读取 Authorization Bearer，解析 exp 并写入吊销表；require_auth 后续校验。

## 2026-10-31 补充：微信退款回调

```text
POST /api/order/pay/refundNotify/wechat
```

处理：

```text
Wechatpay 头验签
AES-256-GCM 解密 resource
out_refund_no / refund_status
SUCCESS -> paylog_refund 成功 + 回写退款金额
CLOSED/ABNORMAL -> paylog_refund 失败
```

## 2026-10-31 补充：商户入驻审核通过开店

`MerchantApplyService::audit` 通过后新增：

```text
创建 merchant 行
按联系人手机号查找/创建 admin_user（admin123321）
创建 shop
创建 merchant_user
创建 admin_user_shop（role_id=0，auth_list=["all"]）
```

仍待补：

```text
admin role 初始化
运费模板初始化
装修模板初始化
保证金账户/规则初始化
审核通过/失败通知
```

## 2026-10-31 补充：商户开店角色初始化

审核通过开店时新增：

```text
店铺管理员角色 store_admin（authority_list=["all"]）
店铺员工角色 store_employee（authority_list=[]）
admin_user.role_id 指向店铺管理员
admin_user_shop.role_id 指向店铺管理员
```

## 2026-10-31 补充：商户结算账户初始化

审核通过开店时新增：

```text
merchant_account
account_type=1
account_name=个人 corporate_name / 企业 company_name
account_no=merchantData.bank_card
bank_name=merchantData.bank_deposit
bank_branch=merchantData.bank_branch
shop_id=新店铺
```

个体工商户且银行卡为空的，按 Java 规则跳过。

## 2026-10-31 补充：商户开店运费模板与装修页

审核通过开店时新增：

```text
shipping_tpl：默认模板 / 1天内 / 不包邮 / 计价方式1 / 默认
shipping_tpl_info：运送方式1，首件1/首价1/续件1/续价1
decorate：默认模板，Java InitDecorateDataConstants.DECORATE_DATA
```
