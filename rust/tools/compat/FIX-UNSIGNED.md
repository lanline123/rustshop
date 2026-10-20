# 修复 MySQL `INT UNSIGNED` 导致的 sqlx 解码错误

## 现象

```text
数据库错误: error occurred while decoding column "admin_id": mismatched types;
Rust type `i32` (as SQL type `INT`) is not compatible with SQL type `INT UNSIGNED`
```

同类错误还会出现在 `shop_id`、`role_id`、`user_id`、`id` 等列。

## 原因

- Java 原始 `java/database/pure.sql` 中大量主键/状态列是 `INT UNSIGNED`；
- Rust 模型使用 `i32/i64`；
- sqlx 按 MySQL 返回的列类型严格校验，`INT UNSIGNED` 不能解码到 `i32`；
- 本地开发库已经转换为有符号（`information_schema` 中 unsigned 数量为 0），
  服务器上直接导入 Java dump 的库没有转换。

这与前端版本无关；最新前端调用这个接口也会得到同样的后端错误。

## 修复（推荐：整库转换）

前提：已经备份数据库。Java dump 没有外键，逐列 ALTER 是安全的。

```bash
mysql -h 127.0.0.1 -u root -p tigshop-java \
  < rust/tools/compat/fix_unsigned_schema.sql
```

脚本会：

1. 遍历当前库所有 `tinyint/smallint/mediumint/int/bigint unsigned` 列；
2. 把超出有符号上限的值夹到上限；
3. `ALTER TABLE ... MODIFY COLUMN ...` 去掉 `unsigned`；
4. 保留 `NOT NULL`、`DEFAULT`、`AUTO_INCREMENT`、`COMMENT`；
5. 最后输出 `remaining_unsigned_columns`，应为 `0`。

验证：

```sql
SELECT COUNT(*) FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = 'tigshop-java'
   AND COLUMN_TYPE LIKE '%unsigned%';
-- 0
```

然后重启 Rust 服务。

## 最小临时修复（只让登录先通）

如果暂时不能整库转换，至少转换登录相关表：

```sql
ALTER TABLE `admin_user`
  MODIFY COLUMN `admin_id` INT NOT NULL AUTO_INCREMENT COMMENT '自增id号,管理员代码',
  MODIFY COLUMN `suppliers_id` INT NOT NULL DEFAULT 0 COMMENT '供应商id',
  MODIFY COLUMN `role_id` INT NOT NULL COMMENT '角色id',
  MODIFY COLUMN `is_using` TINYINT NOT NULL DEFAULT 0 COMMENT '是否停用：1 是/0 否',
  MODIFY COLUMN `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：1 是/0 否';

ALTER TABLE `admin_role`
  MODIFY COLUMN `role_id` INT NOT NULL AUTO_INCREMENT COMMENT '角色ID';
```

但其它页面仍会继续报同类错误，建议整库转换。

## JSON 列

如果整数列修完后出现 JSON 解码错误，再按
`rust/tools/compat/README.md` 中列出的 JSON 列转 `TEXT`。
