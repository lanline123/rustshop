# v5.8.61 数据库升级记录（2026-10-31）

## 目标

将生产库 `tigshopmall` 从 v5.8.57 基线升级到 Java 跨境版 v5.8.61 的结构。

## 备份

```text
文件：/www/backup/tigshopmall-pre5861-20261031-0041.sql.gz
大小：1.9 MB
SHA256：422d43efb0ca7f1c6002996dbdf40008df0d816eb59c9fb2f28a50d5a74fac24
升级前表数：326
```

备份已通过 `gzip -t` 完整性校验。

## 升级脚本

来源：

```text
java-b2b2c-overseas-v5.8.61/database/
```

按顺序：

```text
5.8.57-5.8.58update.sql
5.8.58-5.8.59update.sql
5.8.59-5.8.60update.sql
5.8.60-5.8.61update.sql
```

## 验证方式

先在本机 MySQL 8.4 的独立克隆库 `tigshop_migtest` 执行全部脚本，确认：

```text
product_sale_control
offline_payment_claim
label_printer
shop_group
product.product_volume
product_sku.sku_weight / sku_volume
order.table_id / table_no
```

全部生成。

生产库首次执行时发现 `profit_sharing_order.expected_amount` 已存在，说明生产库此前已被部分升级。随后使用 `mysql --force` 继续执行其余增量。

## 升级后验证

已生成的关键对象：

```text
product_sale_control
vendor_product_sale_control
offline_payment_claim
label_template / label_printer / label_print_batch / label_print_task
label_daily_sequence / label_order_number
shop_group / shop_group_relation
currency_dictionary
image_search_image / image_search_sync_job
translation_call_log
wechat_shipping_sync_task
map_call_log
product.product_volume
product_sku.sku_weight / sku_volume
order.table_id / table_no
```

表/列级比对结果：

```text
新增表：缺失 0
新增列：仅发现 2 个未补
  pos_table.transfer_from_table_no
  pos_table_area.is_delete
```

## 已知问题：POS 表结构与 v5.8.61 不一致

生产库的 `pos_table` / `pos_table_area` 是早期自定义结构，v5.8.61 脚本里的 `CREATE TABLE` 因表已存在而跳过。

例如 v5.8.61 期望 `pos_table` 包含：

```text
table_no / seat_count / table_status / dining_count
open_admin_id / open_time / session_sn / last_order_id
transfer_from_table_no
```

而生产库当前是：

```text
table_name / capacity / status / order_id / ...
```

影响：

```text
POS 桌台/开台/转台/清台功能不能直接使用 v5.8.61 的 Rust 实现
需要单独做 POS 桌台结构迁移和数据映射
不影响 PC 商城商品/订单主链路
```

补齐 `transfer_from_table_no` 时因 `last_order_id` 不存在而失败，已停止，未做破坏性改名。

## 升级后线上状态

```text
/api/home/home/pcIndex                 200
/api/product/product/list              200
/api/product/product/detail?id=1528    200
/api/cart/cart/list                    200
admin login                            success
```

Rust 旧代码在新增列/新增表下可继续运行；新功能尚未接入。

## 后续必须补的 Rust 代码

```text
B1 商品禁售过滤 + saleStatus 接口
B2 体积/重量运费
B3 线下认款
B4 图片搜索
B5 多语言补充
B6 供应商/采购链
B7 店铺分组/标签
B8 主流程修复
B9 POS 桌台结构专用迁移
```

## 回滚

```bash
mysql -utigshopmall -p*** tigshopmall < /www/backup/tigshopmall-pre5861-20261031-0041.sql.gz
```

注意：回滚会丢失升级后新增的配置/权限/新表数据，执行前再次备份当前库。
