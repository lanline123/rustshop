# B2 体积/重量运费实现记录

## 状态

```text
代码：已完成
编译：cargo check 通过
单元测试：shipping 53 + schema 3 通过
数据库：生产已新增 product_volume / sku_weight / sku_volume / pricing_type=3
部署：未部署
```

## 数据库

已由 v5.8.61 增量脚本生成：

```text
product.product_volume decimal(12,5) NULL
product_sku.sku_weight decimal(10,3) NULL
product_sku.sku_volume decimal(12,5) NULL
shipping_tpl.pricing_type 1/2/3
shipping_tpl_info.pricing_type 1/2/3
shipping_tpl_info.start_number / add_number decimal(12,5)
```

## Rust 实现

### 实体与 API

```text
Product.product_volume
ProductSku.sku_weight / sku_volume
ProductSkuVO.sku_weight / sku_volume
AdminProductDetailVO.product_volume
ClientProductInfoVO.product_volume
ProductSaveDTO.product_volume
ProductSkuSaveDTO.sku_weight / sku_volume
```

### 保存

```text
product create/update 后补写 product_volume
product_sku create/update 后补写 sku_weight / sku_volume
```

兼容旧 SQL 列顺序，不重写原 80+ 列 INSERT/UPDATE。

### 运费计算

文件：

```text
rust/crates/core/src/models/trade/check.rs
ShippingRule / calc_shipping_fee
```

规则：

```text
pricing_type=1 按件
pricing_type=2 按重量
pricing_type=3 按体积
体积 = 商品 volume（SKU 有值优先） × 数量
首体积内收 start_price
超出后 ceil((volume - start_number) / add_number) × add_price
```

缺失体积：

```text
商品无有效体积时，按体积模板结算直接报错：
商品【xxx】未配置有效体积，暂无法计算运费，请联系商家
```

### 模板校验

```text
pricing_type 必须为 1/2/3
体积模板首体积/续体积 > 0
首费/续费/满免金额精度与范围校验
```

## 验证

```text
cargo check -p tigshop-services -p tigshop-admin-api -p tigshop-api
Finished

cargo test -p tigshop-core shipping
53 passed
schema_consistency 3 passed
```

新增测试：

```text
shipping_volume_pricing
```

## 未完成

```text
完整二进制 build
部署
后台运费模板页面 UI 的体积选项
商品编辑页面体积字段 UI
订单快照接口字段
```
