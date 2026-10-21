# 前端兼容 / 运行工具

本目录的脚本用于「**不改前端**、用 Rust 后端替换原 Java 服务」的本地/测试环境。

## 数据库兼容修正（换库必看）

从 Java `pure.sql` 新建数据库后，直接导入合并版 SQL：

```bash
mysql -h 127.0.0.1 -u root -p 数据库名 < rust/数据库兼容修正.sql
```

它一次完成：

1. 整数 `UNSIGNED` → 有符号整数
2. `JSON` → `TEXT`
3. `DATE` → `VARCHAR(10)`

可重复执行，末尾输出三个 `remaining_*` 校验值，均应为 `0`。

## mysql_signed_schema.py

Java 的 MySQL dump 中大量主键/状态列是 `INT UNSIGNED`，而 Rust 模型按 `i32/i64` 解码
（sqlx 会报 `mismatched types ... INT UNSIGNED`）。Java 端被替换后可安全归一化为有符号整数
（dump 无外键约束；超范围哨兵值钳制到有符号上限）。

```bash
cd rust
python tools/compat/mysql_signed_schema.py gen  ../alters.sql   # 生成 ALTER
python tools/compat/mysql_signed_schema.py apply ../alters.sql  # 执行
```

换库或重新导入 dump 后需重新执行。执行完可这样验证：

```sql
SELECT COUNT(*) FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA='tigshop-java' AND COLUMN_TYPE LIKE '%unsigned%';  -- 0
```

## checkout_v2_smoke.ps1 / seed_checkout_v2.sql

结算页 v2（运费模板 / 积分抵扣 / 优惠券店铺范围与多店分摊 / 账期 / 预售）联调：

```powershell
mysql ... tigshop-java -e "source rust/tools/compat/seed_checkout_v2.sql"
powershell -NoProfile -ExecutionPolicy Bypass -File rust/tools/compat/checkout_v2_smoke.ps1
```

脚本输出 A~G 七段日志（运费、积分、店铺券禁用、账期、预售、下单落库、运费随单落库）。
口径与剩余项见 [`docs/CHECKOUT-V2.md`](../../docs/CHECKOUT-V2.md)。

## seckill_smoke.ps1 / seed_seckill.sql

秒杀引擎联调（商品 6：78 元 → 50 元、库存 5、限购 1）：

```powershell
mysql ... tigshop-java -e "source rust/tools/compat/seed_seckill.sql"
powershell -NoProfile -ExecutionPolicy Bypass -File rust/tools/compat/seckill_smoke.ps1
```

## presale_smoke.ps1 / seed_presale.sql

定金预售联调（商品 264：32 元、固定定金 5 元 → 尾款 27 元，两阶段支付）：

```powershell
mysql ... tigshop-java -e "source rust/tools/compat/seed_presale.sql"
powershell -NoProfile -ExecutionPolicy Bypass -File rust/tools/compat/presale_smoke.ps1
```

口径与未完成项见 [`docs/ACTIVITY-PRESALE.md`](../../docs/ACTIVITY-PRESALE.md)。

## 完整运行步骤

见 [`docs/RUN-RUST-FRONTEND.md`](../../docs/RUN-RUST-FRONTEND.md)。
## groupon_smoke.ps1 / seed_groupon.sql

拼团引擎联调（商品 136：35 元 → 拼团 10 元，2 人成团）：

```powershell
mysql ... tigshop-java -e "source rust/tools/compat/seed_groupon.sql"
powershell -NoProfile -ExecutionPolicy Bypass -File rust/tools/compat/groupon_smoke.ps1
```

## fullreduce_smoke.ps1 / seed_fullreduce.sql

满减/满折联调（商品 8：490 元，满减阶梯 + 9 折 + 演示数据满 50 减 10）：

```powershell
mysql ... tigshop-java -e "source rust/tools/compat/seed_fullreduce.sql"
powershell -NoProfile -ExecutionPolicy Bypass -File rust/tools/compat/fullreduce_smoke.ps1
```

## JSON 列兼容（必须）

MySQL dump 中以下列的 `JSON` 类型与 Rust `String` 解码不兼容，换库后需转 `TEXT`：

```sql
ALTER TABLE cart MODIFY extra_data TEXT NULL;
ALTER TABLE groupon_promotion_item MODIFY group_num TEXT NOT NULL;
ALTER TABLE groupon_promotion_detail MODIFY group_price TEXT NOT NULL;
```
