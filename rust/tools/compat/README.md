# 前端兼容 / 运行工具

本目录的脚本用于「**不改前端**、用 Rust 后端替换原 Java 服务」的本地/测试环境。

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

## 完整运行步骤

见 [`docs/RUN-RUST-FRONTEND.md`](../../docs/RUN-RUST-FRONTEND.md)。
