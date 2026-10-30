# B9 POS 多门店结构对齐记录

## 数据库备份

```text
/www/backup/tigshopmall-pre-b9-20261031-0236.sql.gz
SHA256：d0daf62dad06a61b42527ef0f565ef5031682a08e311d4bff6f14fa726daa12d
```

## POS 表补列

生产 `pos_table` / `pos_table_area` 原为早期自定义结构，v5.8.61 的 `CREATE TABLE` 因表存在被跳过。

本轮幂等补列并保留旧列：

```text
pos_table:
  table_no
  seat_count
  sort_order
  table_status
  dining_count
  open_admin_id
  open_time
  session_sn
  last_order_id
  transfer_from_table_no

pos_table_area:
  sort_order
  status
  update_time
  is_delete
```

回填：

```text
pos_table.table_no = table_name
pos_table.seat_count = capacity
pos_table.last_order_id = order_id
pos_table_area.sort_order = sort
pos_table_area.update_time = add_time
```

## Rust 同步

`infrastructure/src/db/repo/pos/cashier.rs`：

```text
area insert/update 同步 sort_order / status / update_time
area delete 改为 is_delete=1
table insert/update 同步 table_no / seat_count
开台/清台 同步 table_status / last_order_id / open_time
查询过滤 is_delete=0
```

旧列保留并继续写入，避免破坏已有 POS 接口；新列供 v5.8.61 使用。

## 验证

```text
cargo check -p tigshop-infrastructure
Finished
```

生产列查询已确认：

```text
table_no
seat_count
table_status
last_order_id
transfer_from_table_no
sort_order
status
update_time
is_delete
```

## 未完成

```text
Rust POS DTO/API 切换到 v5.8.61 桌台状态机
开台会话 session_sn / 桌台订单绑定
转台 transfer_from_table_no
桌台清台/自动清台
标签打印 worker
多门店保证金
完整二进制 build / 部署
```

## 结论

本轮完成 POS 表结构兼容层；业务状态机仍未完全切换。
