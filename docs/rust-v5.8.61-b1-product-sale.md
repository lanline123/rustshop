# B1 商品禁售实现记录

## 状态

```text
代码：已完成核心链路
编译：cargo check 通过
数据库：生产已建 product_sale_control / vendor_product_sale_control
部署：未部署
```

## 已实现

### 公开端

```text
GET /api/product/product/list      列表过滤禁售
GET /api/product/product/detail    详情禁售返回不存在
POST /api/search/search/getProduct 搜索过滤禁售
购物车/结算/下单                    check_purchasable 统一拦截
```

过滤规则：

```text
平台设置 shop_id=-1
门店设置 shop_id=商品所属店铺
供应商商品继承 vendor_product_sale_control
定时禁售：ban_at <= now 生效
定时恢复：recover_at <= now 恢复
```

SQL 位于：

```text
rust/crates/infrastructure/src/db/repo/catalog/product_sale.rs
ProductSaleRepository::sale_condition()
```

### 管理端

```text
GET  /adminapi/product/product/saleStatus
POST /adminapi/product/product/saleStatus
GET  /adminapi/vendor/product/platform/saleStatus
POST /adminapi/vendor/product/platform/saleStatus
```

数据表：

```text
product_sale_control
vendor_product_sale_control
```

模型：

```text
rust/crates/core/src/models/catalog/product_sale.rs
```

仓储：

```text
rust/crates/infrastructure/src/db/repo/catalog/product_sale.rs
```

## 验证

```text
cargo check -p tigshop-services -p tigshop-admin-api -p tigshop-api
Finished
```

本地克隆库 SQL 验证：

```text
禁售前商品数：377
插入 product_sale_control(sale_status=0)
过滤后商品数：376
禁售商品查询：0
```

## 未完成

```text
管理端商品列表 saleStatus 字段/筛选
管理端页面 UI
供应商平台列表 saleStatus 展示
店铺入驻限制 onboarding_required
完整二进制 build 与部署
```

## 说明

`cargo check` 已验证类型和路由编译；完整 `cargo build -p tigshop-server` 被中止，等 B1 部署前再执行。
