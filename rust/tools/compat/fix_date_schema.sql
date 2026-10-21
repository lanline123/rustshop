-- 把当前数据库中的 DATE 列改成 VARCHAR(10)，修复 Rust/sqlx：
--   error occurred while decoding column "date": mismatched types;
--   Rust type `Option<String>` (as SQL type `VARCHAR`) is not compatible with SQL type `DATE`
--
-- 适用：Rust 实体里这些日期字段是 String/Option<String>。
-- 执行：
--   mysql -h 127.0.0.1 -u root -p tigshop < rust/tools/compat/fix_date_schema.sql
--
-- 说明：DATE 的文本形式就是 YYYY-MM-DD，转 VARCHAR(10) 不丢数据；
--       日期比较仍可按字典序工作。脚本最后输出 remaining_date_columns（应为 0）。

SET SESSION group_concat_max_len = 1024 * 1024;
SET FOREIGN_KEY_CHECKS = 0;

DROP PROCEDURE IF EXISTS tigshop_fix_date_columns;

DELIMITER $$
CREATE PROCEDURE tigshop_fix_date_columns()
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
       AND DATA_TYPE = 'date'
     ORDER BY TABLE_NAME, ORDINAL_POSITION;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO v_table, v_column, v_nullable, v_comment;
    IF done THEN
      LEAVE read_loop;
    END IF;

    SET @date_alter_sql = CONCAT(
      'ALTER TABLE `', v_table, '` MODIFY COLUMN `', v_column, '` VARCHAR(10)',
      IF(v_nullable = 'NO', ' NOT NULL', ' NULL'),
      IF(v_comment = '', '', CONCAT(' COMMENT ', QUOTE(v_comment)))
    );

    PREPARE date_alter_stmt FROM @date_alter_sql;
    EXECUTE date_alter_stmt;
    DEALLOCATE PREPARE date_alter_stmt;
  END LOOP;
  CLOSE cur;
END$$
DELIMITER ;

CALL tigshop_fix_date_columns();
DROP PROCEDURE tigshop_fix_date_columns;

SET FOREIGN_KEY_CHECKS = 1;

SELECT COUNT(*) AS remaining_date_columns
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE()
   AND DATA_TYPE = 'date';
