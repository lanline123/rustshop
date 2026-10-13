-- =============================================================
-- v5.8.38.1 -> v5.8.39 增量脚本
-- 内容：
--   1) 会员标签二期：promotion 新增 member_scope 人群圈选列（D1096）
--   2) 素材命名方式 / 阿里云短信通道 / 海外短信内容（D1119）
--   3) 移动端装修「搜索风格」配置项（D1145，不含后台菜单）
--   4) 全库字符集标准化 utf8mb4 / utf8mb4_general_ci（D1140）
--   5) 清洗商品描述图片被机翻污染的历史译文（D1140）
--   6) user_coupon.coupon_id tinyint -> int unsigned，修优惠券 id > 255 领取报错（D1140）
--   7) 平台自营门店展示信息配置补齐 storeSelfOperated*（D1140）
--   8) 新增权限点：语音播报 voiceBroadcastManage（D1140）
-- 顺序说明：字符集标准化置于第 4 段，使前三段新增的列一并被规范化；
--          第 5~7 段为后续归档的数据清洗与修复，与字符集转换无依赖。
-- 幂等：全部语句可重复执行。
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- =============================================================

-- -------------------------------------------------------------
-- 1) 会员标签应用（二期）人群圈选列（原 database/D1096--memberTags-v2.0.sql）
--    非空时优先于旧的 member_range / member_marks；旧字段与存量数据零改动、零迁移。
-- -------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'promotion'
      AND COLUMN_NAME = 'member_scope'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `promotion` ADD COLUMN `member_scope` JSON NULL COMMENT ''会员标签二期人群圈选(MemberAudienceDTO序列化JSON)，非空则优先于 member_range/member_marks'' AFTER `member_marks`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -------------------------------------------------------------
-- 2) 素材上传命名方式 / 阿里云短信通道 / 海外短信内容（原 database/D1119-materialSmsSetting.sql）
-- -------------------------------------------------------------

-- 2.1 素材上传命名方式（0-系统随机命名；1-保留原素材名。默认 0）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'materialUploadNameType', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `config` WHERE `biz_code` = 'materialUploadNameType' AND `is_del` = 0
);

-- 2.2 阿里云短信发送通道（1-国内；2-海外；1,2-按号码自动识别。默认 1）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsAliyunChannel', '1', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `config` WHERE `biz_code` = 'smsAliyunChannel' AND `is_del` = 0
);

-- 2.3 阿里云国际短信签名标识（默认空）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsSenderId', '', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `config` WHERE `biz_code` = 'smsSenderId' AND `is_del` = 0
);

-- 2.4 消息模板新增「海外短信内容」字段
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'message_template'
      AND COLUMN_NAME = 'content_oversea'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `message_template` ADD COLUMN `content_oversea` TEXT COMMENT ''海外短信内容'' AFTER `content`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -------------------------------------------------------------
-- 3) 移动端装修「搜索风格」（原 database/D1145-o2oProductSearchStyle.sql）
--    系统配置：mobileSearchStyleType（1-普通商品搜索，2-门店聚合搜索），默认 1
--    注意：原需求脚本中的后台菜单 authority(mobileSearchStyleManage) 插入不纳入本次发布，
--          菜单入口另行处理；此处只发配置项。
-- -------------------------------------------------------------
INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'mobileSearchStyleType', '1', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `config`) AS t
    WHERE t.`biz_code` = 'mobileSearchStyleType' AND t.`is_del` = 0
);

-- -------------------------------------------------------------
-- 4) 数据库字符集标准化：全库统一 utf8mb4 / utf8mb4_general_ci（原 database/D1140-bugfix.sql）
--    对当前连接所在库（DATABASE()）生效，六条产品线通用。
--    幂等：按 information_schema 实际状态挑表，已达标的表不会被再次 ALTER。
--    风险与执行窗口：
--      1. CONVERT TO 会重建表（copy/inplace 视版本而定），大表耗时较长且占用磁盘，请在低峰执行。
--      2. CONVERT TO 会把 BINARY/VARBINARY/BLOB 列一并转成 CHAR/VARCHAR/TEXT。已核对六个基线中
--         待转换的表均无二进制列；若目标库有自建二进制列表，请先单独排除。
--      3. utf8mb3_bin / utf8mb4_unicode_ci 转为 general_ci 后，字符串比较的大小写与重音敏感度
--         按 general_ci 规则生效（order.* 等原 bin 列由区分大小写变为不区分）。
--      4. utf8mb3 -> utf8mb4 每字符由 3 字节涨到 4 字节：宽表可能触碰 65535 字节行长上限、
--         长 varchar 索引可能触碰索引长度上限。首次在某个环境执行时逐表观察报错，
--         遇到 "Row size too large" / "key too long" 的表需单独收窄列宽后再转。
--      5. MySQL 为保持「可存字符数」不变，会把 utf8mb3 的 TEXT 提升为 MEDIUMTEXT（容量变大，
--         不丢数据、Java 侧无感）。基线实测触发该提升的列：
--           b2c / o2o：refund_apply.payment_voucher
--           overseas：refund_apply.payment_voucher、brand.brand_desc、brand.reject_remark、
--                     pc_cat_floor.hot_cat、product_attributes_tpl.tpl_data
-- -------------------------------------------------------------

-- 4.1 库默认字符集（新建表继承此默认，避免后续再次跑偏）
ALTER DATABASE CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 4.2 逐表转换：表默认排序规则或任一字符列排序规则不为 utf8mb4_general_ci 的表
DROP PROCEDURE IF EXISTS `sp_tigshop_normalize_charset`;

DELIMITER $$
CREATE PROCEDURE `sp_tigshop_normalize_charset`()
BEGIN
    DECLARE v_done INT DEFAULT 0;
    DECLARE v_table VARCHAR(64);
    DECLARE cur_tables CURSOR FOR
        SELECT t.TABLE_NAME
        FROM information_schema.TABLES t
        WHERE t.TABLE_SCHEMA = DATABASE()
          AND t.TABLE_TYPE = 'BASE TABLE'
          AND (
              t.TABLE_COLLATION <> 'utf8mb4_general_ci'
              OR EXISTS (
                  SELECT 1
                  FROM information_schema.COLUMNS c
                  WHERE c.TABLE_SCHEMA = t.TABLE_SCHEMA
                    AND c.TABLE_NAME = t.TABLE_NAME
                    AND c.COLLATION_NAME IS NOT NULL
                    AND c.COLLATION_NAME <> 'utf8mb4_general_ci'
              )
          )
        ORDER BY t.TABLE_NAME;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

    OPEN cur_tables;
    convert_loop: LOOP
        FETCH cur_tables INTO v_table;
        IF v_done = 1 THEN
            LEAVE convert_loop;
        END IF;
        SET @ddl = CONCAT('ALTER TABLE `', v_table, '` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END LOOP;
    CLOSE cur_tables;
END$$
DELIMITER ;

CALL `sp_tigshop_normalize_charset`();

DROP PROCEDURE IF EXISTS `sp_tigshop_normalize_charset`;

-- 4.3 执行后核验（应返回 0 行；非 0 行即为仍未达标的表/列）
-- SELECT TABLE_NAME, TABLE_COLLATION
-- FROM information_schema.TABLES
-- WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE'
--   AND TABLE_COLLATION <> 'utf8mb4_general_ci';
--
-- SELECT TABLE_NAME, COLUMN_NAME, CHARACTER_SET_NAME, COLLATION_NAME
-- FROM information_schema.COLUMNS
-- WHERE TABLE_SCHEMA = DATABASE()
--   AND COLLATION_NAME IS NOT NULL AND COLLATION_NAME <> 'utf8mb4_general_ci';


-- 删除权限点：PC装修-搜索页风格（pcSearchStyleManage，连带子权限 pcDecorateOtherUpdateManage）
-- 幂等：按 authority_sn + admin_type 删除，重复执行不报错。
DELETE FROM `authority` WHERE `authority_sn` = 'pcSearchStyleManage' AND `admin_type` = 'admin';



INSERT INTO `authority`
(`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'mobileSearchStyleManage', '搜索风格', 27, 50, 1,
       '[{\"authName\":\"保存\",\"authSn\":\"mobileSearchStyleSaveManage\"}]',
       'decorate/mobile_decorate/search_style/', '', 0, 'admin'
    WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `authority`) AS t
    WHERE t.`authority_sn` = 'mobileSearchStyleManage' AND t.`admin_type` = 'admin'
);


-- -------------------------------------------------------------
-- 5) 清洗商品描述图片被机翻污染的历史译文（原 database/D1140-bugfix.sql）
--    背景：ProductDescDTO.pic/cover 曾挂 @JsonTranslate(dataType=9)，资源 URL 被当作文本
--          送机翻，污染 translations_data，把同一张图的路径搞坏了大小写/空格
--          （img→Img、!!pic→!! pic / %20）。
--    安全边界：只还原「译文其实就是原图路径、仅被机翻改坏了大小写/空格」的行；
--          对「管理员真正配置了另一张本地化图」的行（译文是不同路径）绝不触碰。
--          判定口径：去掉空格与 %20、统一小写后，译文 == 原文 -> 判定为污染行，还原为原文。
--          用 BINARY 比较避免 _ci 排序规则把「仅大小写不同」的污染行漏判。
--    幂等：还原为 translation_name，重复执行为空操作。
-- -------------------------------------------------------------
UPDATE `translations_data`
SET `translation_value` = `translation_name`
WHERE (
        `translation_name` LIKE 'img/%'
     OR `translation_name` LIKE 'http%'
     OR `translation_name` LIKE '%!!pic%'
     OR `translation_name` LIKE '%.jpg'  OR `translation_name` LIKE '%.jpeg'
     OR `translation_name` LIKE '%.png'  OR `translation_name` LIKE '%.webp'
     OR `translation_name` LIKE '%.gif'  OR `translation_name` LIKE '%.mp4'
  )
  AND BINARY `translation_value` <> `translation_name`
  AND LOWER(REPLACE(REPLACE(`translation_value`, ' ', ''), '%20', ''))
    = LOWER(REPLACE(REPLACE(`translation_name`,  ' ', ''), '%20', ''));

-- -------------------------------------------------------------
-- 6) 修复优惠券 id > 255 时领取报错 Out of range value for column 'coupon_id'
--    根因：user_coupon.coupon_id 为 tinyint unsigned（上限 255），与 coupon.coupon_id
--          （int unsigned）不一致；领取写入 user_coupon 时超范围被 MySQL 截断拒绝。
--    幂等：按 information_schema 判定，已是 int unsigned 的库直接跳过，不重建表。
-- -------------------------------------------------------------
-- 说明：MySQL 5.7 的 COLUMN_TYPE 形如 int(10) unsigned、8.x 形如 int unsigned，
--      故用 DATA_TYPE + unsigned 关键字判定，避免版本差异导致重复 ALTER。
SET @col_ok = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_coupon'
      AND COLUMN_NAME = 'coupon_id'
      AND LOWER(DATA_TYPE) = 'int'
      AND LOCATE('unsigned', LOWER(COLUMN_TYPE)) > 0
);
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user_coupon'
      AND COLUMN_NAME = 'coupon_id'
);
SET @ddl = IF(@col_exists = 1 AND @col_ok = 0,
    'ALTER TABLE `user_coupon` MODIFY COLUMN `coupon_id` int unsigned NOT NULL DEFAULT ''0'' COMMENT ''优惠券类型ID''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -------------------------------------------------------------
-- 7) 平台门店（自营，shop_id=0）补齐展示信息配置
--    介绍 / 联系方式 / 营业状态 / 营业时间 / 门店标签，
--    默认值与 com.tigshop.bean.enums.setting.SettingsEnum 保持一致。
--    幂等：按 biz_code 唯一判定，已存在则跳过。
--    审计字段显式写入，兼容 overseas / 部分库 config 列为 NOT NULL 且无默认值的 schema。
-- -------------------------------------------------------------
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'storeSelfOperatedDescription', '', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'storeSelfOperatedDescription' AND t.`is_del` = 0);

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'storeSelfOperatedContactJson', '[]', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'storeSelfOperatedContactJson' AND t.`is_del` = 0);

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'storeSelfOperatedStatus', '1', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'storeSelfOperatedStatus' AND t.`is_del` = 0);

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'storeSelfOperatedOpenCloseJson', '', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'storeSelfOperatedOpenCloseJson' AND t.`is_del` = 0);

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'storeSelfOperatedTipsJson', '[]', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'storeSelfOperatedTipsJson' AND t.`is_del` = 0);

-- -------------------------------------------------------------
-- 8) 新增权限点：语音播报（voiceBroadcastManage）
--    幂等：按 authority_sn + admin_type 判定，已存在则跳过。
-- -------------------------------------------------------------
INSERT INTO `authority`
(`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'voiceBroadcastManage', '语音播报', 10000405, 54, 1, '[]', '', '', 0, 'admin'
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `authority`) AS t
    WHERE t.`authority_sn` = 'voiceBroadcastManage' AND t.`admin_type` = 'admin'
);