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
