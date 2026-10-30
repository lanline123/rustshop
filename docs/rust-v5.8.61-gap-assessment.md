# Rust 后端对照 Java 跨境版 v5.8.61 差异评估

> 评估日期：2026-10-31
> 当前 Rust 基线：`java-b2b2b-pro-v5.8.57` + 后续增量迁移
> 新源码：`java-b2b2c-overseas-v5.8.61-d0fd28bcce96-4b28228ec4ff43febb4caff86778577c`

## 结论

```text
需要更新，但不能整体替换。
```

原因：

1. 当前 Rust 对应的是 **B2B2B 专业版 v5.8.57**。
2. 新源码是 **跨境版 B2C v5.8.61**，既跨了 4 个版本，也换了产品线。
3. Rust 已经吸收了一部分跨境版能力（多语言、货币、部分海外接口），但仍有大量 v5.8.58–v5.8.61 新功能未迁移。
4. 新版本数据库以外的新表/新列为主，Rust 通常可以和“新增列”共存，但新功能不会自动生效。

建议：

```text
按功能分批同步到 Rust
而不是用 v5.8.61 源码覆盖 Rust
```

## 代码规模差异

| 项目 | v5.8.57 | v5.8.61 |
| --- | ---: | ---: |
| `tigshop-api` Java 文件 | 76 | 82 |
| `tigshop-adminapi` Java 文件 | 184 | 209 |
| `tigshop-service` Java 文件 | 960 | 1075 |

## 新增公开接口（举例）

```text
/api/common/currency/getCurrency
/api/common/i18n/getDefaultLocale
/api/common/i18n/getLocales
/api/common/i18n/getLocaleTranslations
/api/common/translate/translate
/api/search/image/capabilities
/api/search/image/search
/api/search/image/results
/api/user/order/offlinePaymentClaim/submit
/api/user/order/offlinePaymentClaim/detail
/api/user/order/offlinePaymentClaim/history
/api/user/oauth/render/{source}
/api/user/oauth/callback/{source}
```

## 新增 Admin 接口（举例）

```text
/adminapi/order/offlinePaymentClaim/list|detail|audit
/adminapi/setting/imageSearch/regions|detail|save|check|build|syncProduct|task|retry
/adminapi/lang/currency/*
/adminapi/lang/locales/*
/adminapi/lang/translations/*
/adminapi/merchant/shopGroup/*
/adminapi/shop/tip/*
/adminapi/supplychain/purchase*
/adminapi/vendor/*
```

Rust 当前缺少：

```text
offlinePaymentClaim
imageSearch
label printing
productSaleControl
shopGroup
多数 supplychain 新接口
```

Rust 已有部分：

```text
多语言 / 货币基础接口
POS 桌台部分基础表
vendor_product 的 sku_weight
```

## v5.8.58–v5.8.61 主要变化

### v5.8.58

```text
组织分组 / 门店分组
O2O 餐饮桌台
商户入驻个体工商户
拆单发货
店铺标签
提现手续费说明
```

### v5.8.59

```text
供应商商品批量重新导入
桌台金额缓存
翻译回退
登录/权限安全修复
```

### v5.8.60

```text
以图搜商品
商品规格重量/体积 + 体积计费
翻译调用记录
微信发货补传
```

### v5.8.61

```text
多门店标签打印
线下支付认款审核闭环
商品详情原图压缩包下载
商品禁售（独立于上下架）
门店保证金
```

## 对当前 PC 商城影响最大的部分

### P0：优先同步

```text
1. product_sale_control 商品禁售
   - 禁售商品不再出现在列表/搜索/详情
   - 当前 Rust 没有该过滤

2. product_volume / product_sku.sku_volume
   - 运费按体积计费
   - 当前 Rust 只有重量，没有体积

3. offline_payment_claim
   - PC 线下支付认款
   - 当前 Rust 完全没有

4. 订单修复
   - 线下未付款超时取消
   - 定金预售余额抵扣状态
   - 积分兑换卡密售后
   - 批发规格改价
   - 取消订单并发/幂等

5. shop tips / 店铺标签
   - 前台店铺展示
```

### P1：海外版能力

```text
以图搜商品
翻译调用记录
OAuth 登录
供应商 / 采购 / 对账
门店分组
```

### P2：POS / 多门店

```text
标签打印
桌台/开台/转台
门店保证金
门店设备码
```

## 数据库影响

新增迁移：

```text
5.8.57-5.8.58update.sql
5.8.58-5.8.59update.sql
5.8.59-5.8.60update.sql
5.8.60-5.8.61update.sql
```

主要新表：

```text
pos_table_area / pos_table
shop_group / shop_group_relation
map_call_log
translation_call_log
image_search_sync_job / image_search_image
wechat_shipping_sync_task
currency_dictionary
offline_payment_claim
product_sale_control / vendor_product_sale_control
label_template / label_printer / label_print_batch / label_print_task
label_daily_sequence / label_order_number
```

主要新列：

```text
cart.table_id
order.table_id / table_no / table_area_name / dining_count
product.product_volume
product_sku.sku_weight / sku_volume
shop.pos_industry_type
profit_sharing_order 线下差额字段
merchant_application 放弃/软删字段
```

## 建议实施顺序

```text
第 1 步：只做数据库兼容与只读字段
  - 确认新增列不破坏 Rust SELECT/INSERT
  - 补 product_sale_control 过滤

第 2 步：运费体积 + 商品规格体积
  - 下单金额、运费试算、订单快照

第 3 步：线下认款
  - 提交认款、凭证、后台审核、入账

第 4 步：订单修复同步
  - 取消、超时、预售、积分卡密、批发改价

第 5 步：海外能力
  - 图片搜索、OAuth、翻译记录、供应商

第 6 步：POS/多门店
  - 标签打印、桌台、保证金
```

## 不建议现在做的事

```text
不要把 v5.8.61 整个 Java 工程直接当作 Rust 迁移基线
不要直接执行全部 SQL 迁移后立即上线
不要用跨境版 PC 前端覆盖当前已上线 PC
```

原因：

```text
产品线不同（B2B2B pro vs overseas B2C）
当前 Rust API 审计基线是旧版 Apifox 1600 条
新版本新增/变更接口未做 Rust 侧契约审计
前端 PC 新版是跨境风，当前线上 PC 是标准版
```

## 推荐下一步

```text
1. 先做一次 v5.8.61 API 契约清单导出
2. 和 Rust 路由表做 method+path diff
3. 产出缺失接口清单
4. 从 P0 商品禁售 + 体积运费 + 线下认款开始分批迁移
5. 每批 cargo check/build + 数据库兼容测试 + 线上灰度
```

## 当前结论

```text
Rust 不需要“重写”，但需要“持续跟进”。
当前最值得先更新的是：
商品禁售过滤
体积运费
线下支付认款
订单已知修复
```
