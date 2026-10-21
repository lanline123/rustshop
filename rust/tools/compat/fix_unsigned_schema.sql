-- 把当前数据库里所有 INT/TINYINT/SMALLINT/MEDIUMINT/BIGINT UNSIGNED 列
-- 改为有符号类型，修复 Rust/sqlx 报：
--   error occurred while decoding column "shop_id": mismatched types;
--   Rust type `i32` (as SQL type `INT`) is not compatible with SQL type `INT UNSIGNED`
--
-- 适用：服务器直接导入了 Java 原始 pure.sql（大量 INT UNSIGNED），而 Rust 模型
-- 使用 i32/i64。Java dump 无外键，因此逐列 ALTER 是安全的。
--
-- 执行：
--   mysql -h 127.0.0.1 -u root -p tigshop-java < rust/tools/compat/fix_unsigned_schema.sql
--
-- 执行前后可核对：
--   SELECT COUNT(*) FROM information_schema.COLUMNS
--    WHERE TABLE_SCHEMA=DATABASE() AND COLUMN_TYPE LIKE '%unsigned%';
--   期望：0
--
-- 注意：
-- 1. 会先执行 UPDATE 把超出有符号上限的值夹到上限（正常业务数据不应出现）。
-- 2. 1011 个列逐个 ALTER，数据量大时需要几分钟；建议先备份。
-- 3. 本脚本只处理整数列；若随后出现 JSON 解码错误，按
--    tools/compat/README.md 的 JSON 列表再转 TEXT。

SET SESSION group_concat_max_len = 1024 * 1024;
SET FOREIGN_KEY_CHECKS = 0;

DROP PROCEDURE IF EXISTS tigshop_fix_unsigned_columns;

DELIMITER $$
CREATE PROCEDURE tigshop_fix_unsigned_columns()
BEGIN
  DECLARE done INT DEFAULT 0;
  DECLARE v_table VARCHAR(64);
  DECLARE v_column VARCHAR(64);
  DECLARE v_data_type VARCHAR(64);
  DECLARE v_column_type VARCHAR(255);
  DECLARE v_nullable VARCHAR(3);
  DECLARE v_default VARCHAR(255);
  DECLARE v_extra VARCHAR(255);
  DECLARE v_comment VARCHAR(2048);
  DECLARE v_max BIGINT;
  DECLARE v_new_type VARCHAR(255);

  DECLARE cur CURSOR FOR
    SELECT TABLE_NAME,
           COLUMN_NAME,
           DATA_TYPE,
           COLUMN_TYPE,
           IS_NULLABLE,
           COLUMN_DEFAULT,
           EXTRA,
           COLUMN_COMMENT
      FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND DATA_TYPE IN ('tinyint', 'smallint', 'mediumint', 'int', 'bigint')
       AND COLUMN_TYPE LIKE '%unsigned%'
     ORDER BY TABLE_NAME, ORDINAL_POSITION;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO v_table, v_column, v_data_type, v_column_type,
                  v_nullable, v_default, v_extra, v_comment;
    IF done THEN
      LEAVE read_loop;
    END IF;

    -- 超出有符号上限的值先夹到上限，避免 ALTER 时 Out of range
    SET v_max = CASE v_data_type
      WHEN 'tinyint' THEN 127
      WHEN 'smallint' THEN 32767
      WHEN 'mediumint' THEN 8388607
      WHEN 'int' THEN 2147483647
      WHEN 'bigint' THEN 9223372036854775807
      ELSE NULL
    END;

    IF v_max IS NOT NULL THEN
      SET @clamp_sql = CONCAT(
        'UPDATE `', v_table, '` SET `', v_column, '` = ', v_max,
        ' WHERE `', v_column, '` > ', v_max
      );
      PREPARE clamp_stmt FROM @clamp_sql;
      EXECUTE clamp_stmt;
      DEALLOCATE PREPARE clamp_stmt;
    END IF;

    SET v_new_type = REPLACE(REPLACE(v_column_type, ' unsigned', ''), ' zerofill', '');

    SET @alter_sql = CONCAT(
      'ALTER TABLE `', v_table, '` MODIFY COLUMN `', v_column, '` ', v_new_type,
      IF(v_nullable = 'NO', ' NOT NULL', ' NULL'),
      IF(v_default IS NULL,
         IF(v_nullable = 'YES', ' DEFAULT NULL', ''),
         CONCAT(' DEFAULT ', QUOTE(v_default))),
      IF(LOCATE('auto_increment', v_extra) > 0, ' AUTO_INCREMENT', ''),
      IF(v_comment = '', '', CONCAT(' COMMENT ', QUOTE(v_comment)))
    );

    PREPARE alter_stmt FROM @alter_sql;
    EXECUTE alter_stmt;
    DEALLOCATE PREPARE alter_stmt;
  END LOOP;
  CLOSE cur;
END$$
DELIMITER ;

CALL tigshop_fix_unsigned_columns();
DROP PROCEDURE tigshop_fix_unsigned_columns;

SET FOREIGN_KEY_CHECKS = 1;

SELECT COUNT(*) AS remaining_unsigned_columns
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE()
   AND COLUMN_TYPE LIKE '%unsigned%';
