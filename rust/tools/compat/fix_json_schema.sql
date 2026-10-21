-- 把当前数据库中所有 JSON 列改成 TEXT，修复 Rust/sqlx 报：
--   error occurred while decoding column "extra_data": mismatched types;
--   Rust type `Option<String>` (as SQL type `VARCHAR`) is not compatible with SQL type `JSON`
--
-- 原因：Java dump 的 JSON 列在 Rust 实体里用 String/Option<String> 解码。
-- 执行：
--   mysql -h 127.0.0.1 -u root -p tigshop < rust/tools/compat/fix_json_schema.sql
--
-- 说明：
-- 1. 只处理 DATA_TYPE='json' 的列，转为 TEXT；
-- 2. JSON 列默认值通常是 NULL；转换时丢弃非常量默认值，避免 MySQL 报错；
-- 3. 需要几分钟时请耐心等待，脚本最后输出 remaining_json_columns（应为 0）。

SET SESSION group_concat_max_len = 1024 * 1024;
SET FOREIGN_KEY_CHECKS = 0;

DROP PROCEDURE IF EXISTS tigshop_fix_json_columns;

DELIMITER $$
CREATE PROCEDURE tigshop_fix_json_columns()
BEGIN
  DECLARE done INT DEFAULT 0;
  DECLARE v_table VARCHAR(64);
  DECLARE v_column VARCHAR(64);
  DECLARE v_nullable VARCHAR(3);
  DECLARE v_comment VARCHAR(2048);
  DECLARE v_ddl LONGTEXT;

  DECLARE cur CURSOR FOR
    SELECT TABLE_NAME, COLUMN_NAME, IS_NULLABLE, COLUMN_COMMENT
      FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND DATA_TYPE = 'json'
     ORDER BY TABLE_NAME, ORDINAL_POSITION;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO v_table, v_column, v_nullable, v_comment;
    IF done THEN
      LEAVE read_loop;
    END IF;

    SET @json_alter_sql = CONCAT(
      'ALTER TABLE `', v_table, '` MODIFY COLUMN `', v_column, '` TEXT',
      IF(v_nullable = 'NO', ' NOT NULL', ' NULL'),
      IF(v_comment = '', '', CONCAT(' COMMENT ', QUOTE(v_comment)))
    );

    PREPARE json_alter_stmt FROM @json_alter_sql;
    EXECUTE json_alter_stmt;
    DEALLOCATE PREPARE json_alter_stmt;
  END LOOP;
  CLOSE cur;
END$$
DELIMITER ;

CALL tigshop_fix_json_columns();
DROP PROCEDURE tigshop_fix_json_columns;

SET FOREIGN_KEY_CHECKS = 1;

SELECT COUNT(*) AS remaining_json_columns
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE()
   AND DATA_TYPE = 'json';
