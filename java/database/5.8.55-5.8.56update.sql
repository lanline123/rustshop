-- =============================================================
-- 5.8.55 -> 5.8.56 增量脚本
-- 内容：D1181、D1188、D1191、D1192 需求 SQL 合并归档
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- =============================================================

-- >>> 来源：database/D1181-premium-member.sql
-- D1181 付费会员完整增量（合并主脚本、right-rule、claim-conditions、dev1）
-- 在已初始化的 Tigshop 数据库中执行，菜单依赖现有 authority 表及平台端 user 菜单。
-- 建表、旧库补列、时间字段升级与种子/菜单写入均支持重复执行。
-- DDL 会隐式提交；末尾事务仅覆盖种子与菜单 DML，不保证整份脚本原子执行。
SET NAMES utf8mb4;

-- 1. 完整表结构（新库直接获得最终字段）
CREATE TABLE IF NOT EXISTS `premium_member_card` (
  `card_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '付费会员卡ID',
  `card_name` varchar(50) NOT NULL DEFAULT '' COMMENT '卡名称',
  `member_notice` varchar(500) NOT NULL DEFAULT '' COMMENT '会员须知',
  `member_scope` text COMMENT '领卡条件[JSON]',
  `card_face_color` varchar(20) NOT NULL DEFAULT '' COMMENT '卡主题色',
  `card_face_image` varchar(255) NOT NULL DEFAULT '' COMMENT '卡面图片(完整URL)',
  `trial_enabled` tinyint unsigned NOT NULL DEFAULT '0',
  `trial_price` decimal(10,2) unsigned NOT NULL DEFAULT '0.00',
  `trial_days` int unsigned NOT NULL DEFAULT '0',
  `trial_limit` int unsigned NOT NULL DEFAULT '1',
  `renew_remind_enabled` tinyint unsigned NOT NULL DEFAULT '0',
  `renew_remind_days` int unsigned NOT NULL DEFAULT '7',
  `pay_types` varchar(30) NOT NULL DEFAULT 'online' COMMENT '支付方式：online在线支付、balance余额支付，逗号分隔',
  `reward_points` int unsigned NOT NULL DEFAULT '0',
  `reward_balance` decimal(10,2) unsigned NOT NULL DEFAULT '0.00',
  `reward_coupon_ids` varchar(255) NOT NULL DEFAULT '',
  `reward_repeat` tinyint unsigned NOT NULL DEFAULT '0',
  `share_mini_title` varchar(50) NOT NULL DEFAULT '', `share_mini_desc` varchar(120) NOT NULL DEFAULT '', `share_mini_image` varchar(255) NOT NULL DEFAULT '',
  `share_h5_title` varchar(50) NOT NULL DEFAULT '', `share_h5_desc` varchar(120) NOT NULL DEFAULT '', `share_h5_image` varchar(255) NOT NULL DEFAULT '',
  `status` tinyint unsigned NOT NULL DEFAULT '0',
  `is_show` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '用户端是否展示：0否1是',
  `is_delete` tinyint unsigned NOT NULL DEFAULT '0',
  `sort_order` smallint unsigned NOT NULL DEFAULT '50', `add_time` int NOT NULL DEFAULT '0', `update_time` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`card_id`), KEY `idx_status` (`status`,`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='付费会员卡';

CREATE TABLE IF NOT EXISTS `premium_member_card_tier` (
  `tier_id` int unsigned NOT NULL AUTO_INCREMENT, `card_id` int unsigned NOT NULL DEFAULT '0',
  `duration` int unsigned NOT NULL DEFAULT '0', `duration_unit` tinyint unsigned NOT NULL DEFAULT '1',
  `price` decimal(10,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '支付金额',
  `renew_price` decimal(10,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '续费优惠值（基于支付金额的优惠，非成交价）；0=不优惠',
  `renew_price_type` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '续费优惠类型：1=元减免，2=折（折数/10）',
  `is_recommend` tinyint unsigned NOT NULL DEFAULT '0', `sort_order` smallint unsigned NOT NULL DEFAULT '50',
  PRIMARY KEY (`tier_id`), KEY `idx_card` (`card_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='付费会员卡档位';

CREATE TABLE IF NOT EXISTS `premium_member_right` (
  `right_id` int unsigned NOT NULL AUTO_INCREMENT, `right_type` tinyint unsigned NOT NULL DEFAULT '0',
  `right_key` varchar(30) NOT NULL DEFAULT '', `show_name` varchar(50) NOT NULL DEFAULT '',
  `icon` varchar(255) NOT NULL DEFAULT '', `description` varchar(255) NOT NULL DEFAULT '',
  `is_builtin` tinyint unsigned NOT NULL DEFAULT '0', `status` tinyint unsigned NOT NULL DEFAULT '1',
  `sort_order` smallint unsigned NOT NULL DEFAULT '50', `add_time` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`right_id`), KEY `idx_right_key` (`right_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='付费会员权益中心';

CREATE TABLE IF NOT EXISTS `premium_member_card_right` (
  `id` int unsigned NOT NULL AUTO_INCREMENT, `card_id` int unsigned NOT NULL DEFAULT '0', `right_id` int unsigned NOT NULL DEFAULT '0',
  `rule_id` int unsigned NULL DEFAULT NULL COMMENT '所选权益规则ID',
  `right_value` varchar(50) NOT NULL DEFAULT '', `sort_order` smallint unsigned NOT NULL DEFAULT '50',
  PRIMARY KEY (`id`), UNIQUE KEY `uk_card_right` (`card_id`,`right_id`), KEY `idx_right` (`right_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='付费会员卡权益关联';

CREATE TABLE IF NOT EXISTS `premium_member_order` (
  `order_id` int unsigned NOT NULL AUTO_INCREMENT, `order_sn` varchar(30) NOT NULL DEFAULT '',
  `user_id` mediumint unsigned NOT NULL DEFAULT '0', `card_id` int unsigned NOT NULL DEFAULT '0', `tier_id` int unsigned NOT NULL DEFAULT '0',
  `card_name_snapshot` varchar(50) NOT NULL DEFAULT '', `duration` int unsigned NOT NULL DEFAULT '0', `duration_unit` tinyint unsigned NOT NULL DEFAULT '1',
  `amount` decimal(10,2) unsigned NOT NULL DEFAULT '0.00', `buy_type` tinyint unsigned NOT NULL DEFAULT '1', `status` tinyint unsigned NOT NULL DEFAULT '0',
  `pay_sn` varchar(30) NOT NULL DEFAULT '', `pay_code` varchar(30) NOT NULL DEFAULT '', `balance_amount` decimal(10,2) unsigned NOT NULL DEFAULT '0.00', `unpaid_amount` decimal(10,2) unsigned DEFAULT NULL, `reward_granted` tinyint unsigned NOT NULL DEFAULT '0',
  `claim_profile` text NULL COMMENT '领卡资料快照JSON',
  `agreement_accepted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否同意协议',
  `agreement_version` varchar(64) NOT NULL DEFAULT '' COMMENT '协议版本快照',
  `add_time` int NOT NULL DEFAULT '0', `paid_time` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`order_id`), UNIQUE KEY `uk_order_sn` (`order_sn`), KEY `idx_user_status` (`user_id`,`status`), KEY `idx_pay_sn` (`pay_sn`), KEY `idx_card` (`card_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='付费会员订单';

CREATE TABLE IF NOT EXISTS `user_premium_card` (
  `id` int unsigned NOT NULL AUTO_INCREMENT, `user_id` mediumint unsigned NOT NULL DEFAULT '0', `card_id` int unsigned NOT NULL DEFAULT '0',
  `order_id` int unsigned NOT NULL DEFAULT '0', `card_no` varchar(30) NOT NULL DEFAULT '', `start_time` bigint NOT NULL DEFAULT '0' COMMENT '生效时间(秒级时间戳)', `end_time` bigint NOT NULL DEFAULT '0' COMMENT '到期时间(秒级时间戳，须用bigint：续期/长期卡会超过INT上限2038-01-19)',
  `status` tinyint unsigned NOT NULL DEFAULT '1', `remind_sent` tinyint unsigned NOT NULL DEFAULT '0', `add_time` bigint NOT NULL DEFAULT '0' COMMENT '添加时间(秒级时间戳)',
  PRIMARY KEY (`id`), UNIQUE KEY `uk_order` (`order_id`), KEY `idx_user_status_end` (`user_id`,`status`,`end_time`), KEY `idx_card_no` (`card_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户持有的付费会员卡';

CREATE TABLE IF NOT EXISTS `premium_member_decorate` (
  `id` int unsigned NOT NULL AUTO_INCREMENT, `card_id` int unsigned NOT NULL DEFAULT '0',
  `hide_card_no` tinyint unsigned NOT NULL DEFAULT '0', `hide_qrcode` tinyint unsigned NOT NULL DEFAULT '0', `hide_validity` tinyint unsigned NOT NULL DEFAULT '0',
  `rights_display_type` tinyint unsigned NOT NULL DEFAULT '1', `rights_display_config` text, `promo_config` text,
  `update_time` int NOT NULL DEFAULT '0', PRIMARY KEY (`id`), UNIQUE KEY `uk_card` (`card_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='付费会员装修配置';

CREATE TABLE IF NOT EXISTS `premium_member_right_rule` (
  `rule_id` int unsigned NOT NULL AUTO_INCREMENT,
  `right_id` int unsigned NOT NULL DEFAULT '0',
  `rule_name` varchar(50) NOT NULL DEFAULT '',
  `rule_value` varchar(50) NOT NULL DEFAULT '',
  `status` tinyint unsigned NOT NULL DEFAULT '1',
  `sort_order` smallint unsigned NOT NULL DEFAULT '50',
  `add_time` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`rule_id`), UNIQUE KEY `uk_right_rule_name` (`right_id`,`rule_name`), KEY `idx_right_status` (`right_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='付费会员权益规则';

-- 2. 旧库兼容升级：逐列检查，兼容仅执行过部分补丁的数据库。
SET @db := DATABASE();
SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='premium_member_card' AND COLUMN_NAME='is_show')=0,
 'ALTER TABLE `premium_member_card` ADD COLUMN `is_show` tinyint unsigned NOT NULL DEFAULT 1 COMMENT ''用户端是否展示：0否1是'' AFTER `status`', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='premium_member_card' AND COLUMN_NAME='pay_types')=0,
 'ALTER TABLE `premium_member_card` ADD COLUMN `pay_types` varchar(30) NOT NULL DEFAULT ''online'' COMMENT ''支付方式：online在线支付、balance余额支付，逗号分隔'' AFTER `renew_remind_days`', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='premium_member_card_tier' AND COLUMN_NAME='renew_price_type')=0,
 'ALTER TABLE `premium_member_card_tier` ADD COLUMN `renew_price_type` tinyint unsigned NOT NULL DEFAULT 1 COMMENT ''续费优惠类型：1=元减免，2=折（折数/10）'' AFTER `renew_price`', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='premium_member_card_right' AND COLUMN_NAME='rule_id')=0,
 'ALTER TABLE `premium_member_card_right` ADD COLUMN `rule_id` int unsigned NULL DEFAULT NULL COMMENT ''所选权益规则ID'' AFTER `right_id`', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='premium_member_order' AND COLUMN_NAME='balance_amount')=0,
 'ALTER TABLE `premium_member_order` ADD COLUMN `balance_amount` decimal(10,2) unsigned NOT NULL DEFAULT 0 COMMENT ''本订单已使用的余额金额'' AFTER `pay_code`', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='premium_member_order' AND COLUMN_NAME='unpaid_amount')=0,
 'ALTER TABLE `premium_member_order` ADD COLUMN `unpaid_amount` decimal(10,2) unsigned DEFAULT NULL COMMENT ''本订单剩余待支付金额'' AFTER `balance_amount`', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='premium_member_order' AND COLUMN_NAME='claim_profile')=0,
 'ALTER TABLE `premium_member_order` ADD COLUMN `claim_profile` text NULL COMMENT ''领卡资料快照JSON'' AFTER `reward_granted`', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='premium_member_order' AND COLUMN_NAME='agreement_accepted')=0,
 'ALTER TABLE `premium_member_order` ADD COLUMN `agreement_accepted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT ''是否同意协议'' AFTER `claim_profile`', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='premium_member_order' AND COLUMN_NAME='agreement_version')=0,
 'ALTER TABLE `premium_member_order` ADD COLUMN `agreement_version` varchar(64) NOT NULL DEFAULT '''' COMMENT ''协议版本快照'' AFTER `agreement_accepted`', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 持卡有效期使用秒级时间戳；逐列升级，兼容部分时间列已改为 BIGINT 的情况。
SET @sql := IF((SELECT DATA_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='user_premium_card' AND COLUMN_NAME='start_time')='int',
 'ALTER TABLE `user_premium_card` MODIFY COLUMN `start_time` bigint NOT NULL DEFAULT 0 COMMENT ''生效时间(秒级时间戳)''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF((SELECT DATA_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='user_premium_card' AND COLUMN_NAME='end_time')='int',
 'ALTER TABLE `user_premium_card` MODIFY COLUMN `end_time` bigint NOT NULL DEFAULT 0 COMMENT ''到期时间(秒级时间戳，须用bigint：续期/长期卡会超过INT上限2038-01-19)''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := IF((SELECT DATA_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='user_premium_card' AND COLUMN_NAME='add_time')='int',
 'ALTER TABLE `user_premium_card` MODIFY COLUMN `add_time` bigint NOT NULL DEFAULT 0 COMMENT ''添加时间(秒级时间戳)''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 3. 内置权益与平台端菜单（幂等 DML）
START TRANSACTION;

INSERT INTO `premium_member_right` (`right_type`,`right_key`,`show_name`,`description`,`is_builtin`,`status`,`sort_order`,`add_time`)
SELECT 1,'discount','会员折扣','购买商品享受专属折扣',1,1,10,UNIX_TIMESTAMP() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `premium_member_right` WHERE `right_key`='discount');
INSERT INTO `premium_member_right` (`right_type`,`right_key`,`show_name`,`description`,`is_builtin`,`status`,`sort_order`,`add_time`)
SELECT 2,'pointsMultiple','多倍积分','下单按倍数赠送积分',1,1,20,UNIX_TIMESTAMP() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `premium_member_right` WHERE `right_key`='pointsMultiple');
INSERT INTO `premium_member_right` (`right_type`,`right_key`,`show_name`,`description`,`is_builtin`,`status`,`sort_order`,`add_time`)
SELECT 3,'freeShipping','会员包邮','订单免运费',1,1,30,UNIX_TIMESTAMP() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `premium_member_right` WHERE `right_key`='freeShipping');

-- D1181：平台端会员菜单补充
-- 会员（user）
--   ├─ 付费会员（premiumMemberManagement）
--   │  ├─ 付费会员（premiumMemberManage）
--   │  └─ 权益中心（premiumMemberRightManage）
-- 仅写入 admin 端；不修改既有会员菜单，也不向 shop/store 端写入。
-- 所有写入按 authority_sn + admin_type 幂等，可安全重复执行。

-- 分组：挂在现有平台端「会员」顶级菜单下。
INSERT INTO `authority`
    (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`,
     `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT
    'premiumMemberManagement', '付费会员', parent.`authority_id`, 51, 1, '[]',
    '', 'iconfont-tig icon-huiyuan', 0, 'admin'
FROM `authority` parent
WHERE parent.`authority_sn` = 'user'
  AND parent.`admin_type` = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM `authority` existing
      WHERE existing.`authority_sn` = 'premiumMemberManagement'
        AND existing.`admin_type` = 'admin'
  );

-- 页面：付费会员卡（保留统一编辑权限点供后台页面使用）。
INSERT INTO `authority`
    (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`,
     `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT
    'premiumMemberManage', '付费会员', parent.`authority_id`, 50, 1,
    '[{"authName":"编辑","authSn":"premiumMemberUpdateManage"}]',
    'user/premium_member/card/list/', '', 0, 'admin'
FROM `authority` parent
WHERE parent.`authority_sn` = 'premiumMemberManagement'
  AND parent.`admin_type` = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM `authority` existing
      WHERE existing.`authority_sn` = 'premiumMemberManage'
        AND existing.`admin_type` = 'admin'
  );

-- 页面：权益中心。
INSERT INTO `authority`
    (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`,
     `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT
    'premiumMemberRightManage', '权益中心', parent.`authority_id`, 51, 1,
    '[{"authName":"编辑","authSn":"premiumMemberUpdateManage"}]',
    'user/premium_member/right/list/', '', 0, 'admin'
FROM `authority` parent
WHERE parent.`authority_sn` = 'premiumMemberManagement'
  AND parent.`admin_type` = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM `authority` existing
      WHERE existing.`authority_sn` = 'premiumMemberRightManage'
        AND existing.`admin_type` = 'admin'
  );

-- 对已存在的本次菜单只同步层级和显示属性；既有其他 authority 不受影响。
UPDATE `authority` menu
JOIN `authority` parent
  ON parent.`authority_sn` = 'user'
 AND parent.`admin_type` = 'admin'
SET menu.`authority_name` = '付费会员',
    menu.`parent_id` = parent.`authority_id`,
    menu.`sort_order` = 51,
    menu.`is_show` = 1,
    menu.`child_auth` = '[]',
    menu.`route_link` = '',
    menu.`authority_ico` = 'iconfont-tig icon-huiyuan',
    menu.`is_system` = 0
WHERE menu.`authority_sn` = 'premiumMemberManagement'
  AND menu.`admin_type` = 'admin';

UPDATE `authority` page
JOIN `authority` parent
  ON parent.`authority_sn` = 'premiumMemberManagement'
 AND parent.`admin_type` = 'admin'
SET page.`parent_id` = parent.`authority_id`,
    page.`authority_name` = '付费会员',
    page.`sort_order` = 50,
    page.`is_show` = 1,
    page.`child_auth` = '[{"authName":"编辑","authSn":"premiumMemberUpdateManage"}]',
    page.`route_link` = 'user/premium_member/card/list/',
    page.`authority_ico` = '',
    page.`is_system` = 0
WHERE page.`authority_sn` = 'premiumMemberManage'
  AND page.`admin_type` = 'admin';

UPDATE `authority` page
JOIN `authority` parent
  ON parent.`authority_sn` = 'premiumMemberManagement'
 AND parent.`admin_type` = 'admin'
SET page.`parent_id` = parent.`authority_id`,
    page.`authority_name` = '权益中心',
    page.`sort_order` = 51,
    page.`is_show` = 1,
    page.`child_auth` = '[{"authName":"编辑","authSn":"premiumMemberUpdateManage"}]',
    page.`route_link` = 'user/premium_member/right/list/',
    page.`authority_ico` = '',
    page.`is_system` = 0
WHERE page.`authority_sn` = 'premiumMemberRightManage'
  AND page.`admin_type` = 'admin';

COMMIT;

-- >>> 来源：database/D1188-attrTemplate.sql
-- D1188 属性模板绑定商品类目
ALTER TABLE product_attributes_tpl
    ADD COLUMN category_id int unsigned DEFAULT NULL COMMENT '关联商品类目ID' AFTER shop_id;

ALTER TABLE product_attributes_tpl
    ADD INDEX idx_product_attributes_tpl_category_id (category_id);

-- >>> 来源：database/D1191-device-code-printer-binding.sql
-- D1191 收银台设备码与打印机绑定
-- DDL 仅执行一次；各语句使用 information_schema 检查，重复执行不会重复新增列/表。

SET @d1191_pos_device_table = (
    SELECT COUNT(*) FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pos_device'
);
SET @d1191_pos_device_ddl = IF(
    @d1191_pos_device_table = 0,
    'CREATE TABLE `pos_device` (
       `id` int unsigned NOT NULL AUTO_INCREMENT,
       `shop_id` int NOT NULL DEFAULT 0 COMMENT ''店铺ID'',
       `device_name` varchar(64) NOT NULL COMMENT ''设备名称'',
       `device_code` varchar(64) NOT NULL COMMENT ''店铺内唯一设备码'',
       `status` tinyint NOT NULL DEFAULT 1 COMMENT ''1启用 2停用'',
       `last_used_time` bigint DEFAULT NULL COMMENT ''最后使用时间'',
       `add_time` bigint NOT NULL DEFAULT 0,
       `update_time` bigint NOT NULL DEFAULT 0,
       PRIMARY KEY (`id`),
       UNIQUE KEY `uk_pos_device_shop_code` (`shop_id`,`device_code`),
       KEY `idx_pos_device_shop_status` (`shop_id`,`status`)
     ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''收银台设备''',
    'SELECT 1'
);
PREPARE d1191_pos_device_stmt FROM @d1191_pos_device_ddl;
EXECUTE d1191_pos_device_stmt;
DEALLOCATE PREPARE d1191_pos_device_stmt;

SET @d1191_binding_table = (
    SELECT COUNT(*) FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'print_pos_device'
);
SET @d1191_binding_ddl = IF(
    @d1191_binding_table = 0,
    'CREATE TABLE `print_pos_device` (
       `id` int unsigned NOT NULL AUTO_INCREMENT,
       `shop_id` int NOT NULL DEFAULT 0 COMMENT ''店铺ID'',
       `print_id` int NOT NULL COMMENT ''打印机ID'',
       `device_code` varchar(64) NOT NULL COMMENT ''收银台设备码'',
       `add_time` bigint NOT NULL DEFAULT 0,
       PRIMARY KEY (`id`),
       UNIQUE KEY `uk_print_pos_device` (`print_id`),
       KEY `idx_print_pos_device_shop_code` (`shop_id`,`device_code`)
     ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''打印机收银台设备绑定（单设备）''',
    'SELECT 1'
);
PREPARE d1191_binding_stmt FROM @d1191_binding_ddl;
EXECUTE d1191_binding_stmt;
DEALLOCATE PREPARE d1191_binding_stmt;

-- 绑定关系由多设备收口为单设备：先清理同一打印机的历史重复绑定（保留最早一条），再把唯一键收敛到 print_id。
DELETE binding FROM `print_pos_device` binding
JOIN (
    SELECT `print_id`, MIN(`id`) AS keep_id FROM `print_pos_device` GROUP BY `print_id`
) kept ON binding.`print_id` = kept.`print_id` AND binding.`id` > kept.keep_id;

SET @d1191_binding_idx_cols = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'print_pos_device' AND INDEX_NAME = 'uk_print_pos_device'
);
SET @d1191_binding_idx_drop = IF(
    @d1191_binding_idx_cols > 1,
    'ALTER TABLE `print_pos_device` DROP INDEX `uk_print_pos_device`',
    'SELECT 1'
);
PREPARE d1191_binding_idx_drop_stmt FROM @d1191_binding_idx_drop;
EXECUTE d1191_binding_idx_drop_stmt;
DEALLOCATE PREPARE d1191_binding_idx_drop_stmt;

SET @d1191_binding_idx_exists = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'print_pos_device' AND INDEX_NAME = 'uk_print_pos_device'
);
SET @d1191_binding_idx_add = IF(
    @d1191_binding_idx_exists = 0,
    'ALTER TABLE `print_pos_device` ADD UNIQUE KEY `uk_print_pos_device` (`print_id`)',
    'SELECT 1'
);
PREPARE d1191_binding_idx_add_stmt FROM @d1191_binding_idx_add;
EXECUTE d1191_binding_idx_add_stmt;
DEALLOCATE PREPARE d1191_binding_idx_add_stmt;

SET @d1191_device_code_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'admin_user_shop' AND COLUMN_NAME = 'device_code'
);
SET @d1191_device_code_ddl = IF(
    @d1191_device_code_exists = 0,
    'ALTER TABLE `admin_user_shop` ADD COLUMN `device_code` varchar(64) NULL COMMENT ''收银员绑定的收银台设备码''',
    'SELECT 1'
);
PREPARE d1191_device_code_stmt FROM @d1191_device_code_ddl;
EXECUTE d1191_device_code_stmt;
DEALLOCATE PREPARE d1191_device_code_stmt;

SET @d1191_default_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'print' AND COLUMN_NAME = 'is_default'
);
SET @d1191_default_ddl = IF(
    @d1191_default_exists = 0,
    'ALTER TABLE `print` ADD COLUMN `is_default` tinyint NOT NULL DEFAULT 0 COMMENT ''店铺默认打印机 1是0否''',
    'SELECT 1'
);
PREPARE d1191_default_stmt FROM @d1191_default_ddl;
EXECUTE d1191_default_stmt;
DEALLOCATE PREPARE d1191_default_stmt;

-- 收银台设备管理菜单（幂等：按 authority_sn + admin_type 判断是否已存在）
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'posCashierDeviceList', '设备管理',
       (SELECT id
        FROM (SELECT `authority_id` AS id
              FROM `authority`
              WHERE `authority_sn` = 'posSetting'
                AND `admin_type` = 'admin'
              LIMIT 1) parent),
       50, 1, '[]', '', '', 0, 'admin'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `authority`
    WHERE `authority_sn` = 'posCashierDeviceList'
      AND `admin_type` = 'admin'
);

INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'posCashierDeviceList', '设备管理',
       (SELECT id
        FROM (SELECT `authority_id` AS id
              FROM `authority`
              WHERE `authority_sn` = 'posSetting'
                AND `admin_type` = 'store'
              LIMIT 1) parent),
       50, 1, '[]', '', '', 0, 'store'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `authority`
    WHERE `authority_sn` = 'posCashierDeviceList'
      AND `admin_type` = 'store'
);

-- >>> 来源：database/D1192-bugfix.sql
-- D1192 收银台自动打印按前端选择设备路由
-- DDL 仅执行一次；使用 information_schema 检查，重复执行不会重复新增列。

SET @d1192_order_device_code_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'order'
      AND COLUMN_NAME = 'device_code'
);
SET @d1192_order_device_code_ddl = IF(
    @d1192_order_device_code_exists = 0,
    'ALTER TABLE `order` ADD COLUMN `device_code` varchar(64) NULL COMMENT ''收银台支付时选择的打印设备码'' AFTER `pos_user_id`',
    'SELECT 1'
);
PREPARE d1192_order_device_code_stmt FROM @d1192_order_device_code_ddl;
EXECUTE d1192_order_device_code_stmt;
DEALLOCATE PREPARE d1192_order_device_code_stmt;

-- >>> 来源：database/D1091-wechat-auto-profitsharing.sql
-- D1180：部分退款后按净额分账。部署新代码前执行；此 DDL 仅执行一次。
-- NULL 保留历史记录按原交易总额回退的语义，不对历史已发送请求猜测或回填基数。
ALTER TABLE `profit_sharing_order`
    ADD COLUMN `sharing_base_amount` DECIMAL(12,2) NULL DEFAULT NULL
    COMMENT '首次实际分账时的计费基数，历史空值沿用total_amount' AFTER `total_amount`;

-- 店铺资金按账户/渠道区分。部署新代码前执行；以下 DDL 仅执行一次。
-- 历史日志保留为实际发生变动的线下账户，不按订单支付方式重标或虚构线上流水。
ALTER TABLE `shop_account_log`
    ADD COLUMN `balance_type` TINYINT NOT NULL DEFAULT 1 COMMENT '资金账户：1线下，2线上',
    ADD COLUMN `pay_channel` VARCHAR(16) NULL COMMENT '线上渠道WECHAT/HUIFU/ALIPAY',
    ADD COLUMN `change_amount` DECIMAL(12,2) NULL COMMENT '资金变动额，历史空值按旧日志规则计算',
    ADD COLUMN `event_key` VARCHAR(128) NULL COMMENT '资金事件唯一键，防止重复记账',
    MODIFY COLUMN `new_shop_money` DECIMAL(10,2) NULL DEFAULT '0.00' COMMENT '变动后余额；线上历史余额未知时为空',
    ADD UNIQUE KEY `uk_shop_account_event` (`event_key`),
    ADD KEY `idx_shop_account_balance_time` (`shop_id`, `balance_type`, `pay_channel`, `add_time`);

-- 新线上提现不冻结线下资金；NULL 标识旧数据，需核账后处理，不自动返还历史冻结。
ALTER TABLE `shop_withdraw`
    ADD COLUMN `offline_frozen_amount` DECIMAL(12,2) NULL DEFAULT NULL COMMENT '本申请冻结的线下金额，新线上为0，历史NULL待核账',
    ADD COLUMN `channel_sub_mch_id` VARCHAR(128) NULL DEFAULT NULL COMMENT '申请时渠道子商户号快照，避免绑定变更影响出金和查单';
