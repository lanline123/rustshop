ALTER TABLE `order`
    MODIFY COLUMN `logistics_id` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '快递公司id/为0则表示商家配送或无需配送' AFTER `shipping_method`;

-- 为 admin_user 表添加软删除字段
ALTER TABLE `admin_user` ADD COLUMN `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否删除：1 是 0 否' AFTER `initial_password`;

-- 为 promotion 表添加适用会员范围字段（营销活动可选择适用会员）
ALTER TABLE `promotion` ADD COLUMN `member_range` tinyint(1) NOT NULL DEFAULT 0 COMMENT '适用会员范围：0-全部会员，1-指定标记的会员' AFTER `is_delete`;
ALTER TABLE `promotion` ADD COLUMN `member_marks` varchar(255) DEFAULT NULL COMMENT '选择的会员标记列表（JSON格式，如[1,2,3]）' AFTER `member_range`;

-- 拼团活动表增加PC活动图片字段
ALTER TABLE `groupon_promotion`
    ADD COLUMN `pc_groupon_image` varchar(500) DEFAULT NULL COMMENT 'pc活动图片' AFTER `groupon_image`;

-- 修改原活动图片字段注释为h5活动图片
ALTER TABLE `groupon_promotion`
    MODIFY COLUMN `groupon_image` varchar(500) DEFAULT NULL COMMENT 'h5活动图片';


-- 收银台商品来源配置
-- 添加shop表的pos_product_source字段
ALTER TABLE `shop` ADD COLUMN `pos_product_source` varchar(255) NULL COMMENT '收银台商品来源(JSON数组)：0-全部商品，1-门店自建商品，2-平台分配商品，3-供应商商品' AFTER `shop_common_remarks`;

ALTER TABLE `shop`
    ADD COLUMN `is_delete` tinyint NULL COMMENT '是否删除  null或者0标识未删除  1 表示已删除' AFTER `pos_product_source`;

-- 为 logistics_company 表添加启用状态字段
ALTER TABLE `logistics_company` ADD COLUMN `is_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1启用，0禁用' AFTER `api_logistics_code`;


ALTER TABLE `logistics_company`
    MODIFY COLUMN `logistics_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '配送方式的快递公司代号' AFTER `logistics_id`;


-- ============================================
-- 周期购功能：线上数据库整合升级脚本（增量/幂等）
-- 生成日期：2026-01-13
-- 适用场景：线上已有数据，升级/补齐周期购相关表结构与字段
-- 重要说明：
--   1) 本脚本不会 DROP TABLE，不会删除数据
--   2) 通过 information_schema 检查后再 ALTER，尽量保证可重复执行
--   3) 执行前请务必备份数据库（结构+数据）
-- ============================================

SELECT '开始执行【周期购】线上整合升级脚本...' AS info, NOW() AS exec_time;

-- ==========================================
-- 一、创建周期购相关表（不存在则创建）
-- ==========================================

-- 1) 商品周期购配置表：product_cycle
CREATE TABLE IF NOT EXISTS `product_cycle` (
                                               `cycle_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '周期购ID',
                                               `product_id` mediumint unsigned NOT NULL COMMENT '商品ID',
                                               `setting_mode` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '设置模式：1-统一设置，2-SKU单独设置',
                                               `cycle_specs` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[]' COMMENT '周期规格数组JSON，如：[6,7,8,12]',
    `times_per_period` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '每期次数',
    `cycle_type` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '配送周期类型：1-每日一次，2-每周一次，3-每月一次，4-多周一次，5-多月一次，6-每周多次，7-每月多次',
    `cycle_interval` tinyint unsigned DEFAULT NULL COMMENT '周期间隔（多周一次/多月一次时使用）',
    `times_per_cycle` tinyint unsigned DEFAULT NULL COMMENT '每周期期数（每周多次/每月多次时使用）',
    `delivery_weekdays` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '可配送星期JSON数组，如：[1,3,5]',
    `delivery_days` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '可配送日期JSON数组，如：[1,15,28]',
    `delivery_time_slots` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '[]' COMMENT '可配送时间段JSON数组，如：["09:00-11:00","14:00-16:00"]',
    `order_cutoff_days` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '截单提前天数（买家需提前X天下单）',
    `order_cutoff_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '截单时间点（如：10:35，在此时间前下单才能最近配送）',
    `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time` int unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
    `update_time` int unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
    PRIMARY KEY (`cycle_id`),
    UNIQUE KEY `uk_product_id` (`product_id`),
    KEY `idx_cycle_type` (`cycle_type`),
    KEY `idx_status` (`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品周期购配置表';

-- 2) 商品周期购规格明细表：product_cycle_spec
CREATE TABLE IF NOT EXISTS `product_cycle_spec` (
                                                    `cycle_spec_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '规格明细ID',
                                                    `cycle_id` int unsigned NOT NULL COMMENT '关联周期购配置ID',
                                                    `product_id` mediumint unsigned NOT NULL COMMENT '商品ID',
                                                    `sku_id` int unsigned DEFAULT NULL COMMENT 'SKU ID（统一设置时为NULL）',
                                                    `period_num` tinyint unsigned NOT NULL COMMENT '周期规格（期数）',
                                                    `period_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '周期规格名称',
    `original_price` decimal(10,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '划线价',
    `cycle_price` decimal(10,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '周期价（实际售价）',
    `disabled` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '禁用状态：0-启用，1-禁用（SKU单独设置时，表示该SKU不支持此周期规格）',
    `create_time` int unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
    `update_time` int unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
    PRIMARY KEY (`cycle_spec_id`),
    KEY `idx_cycle_id` (`cycle_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_period_num` (`period_num`),
    KEY `idx_disabled` (`disabled`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品周期购规格明细表';

-- 3) 订单周期购计划表：order_cycle
CREATE TABLE IF NOT EXISTS `order_cycle` (
                                             `order_cycle_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '周期购订单ID',
                                             `order_id` int unsigned NOT NULL COMMENT '主订单ID',
                                             `order_sn` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '主订单号',
    `user_id` int unsigned NOT NULL COMMENT '用户ID',
    `shop_id` int unsigned NOT NULL DEFAULT '0' COMMENT '店铺ID',
    `product_id` mediumint unsigned NOT NULL COMMENT '商品ID',
    `sku_id` int unsigned DEFAULT NULL COMMENT 'SKU ID',
    `period_num` tinyint unsigned NOT NULL COMMENT '购买期数',
    `times_per_period` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '每期次数',
    `quantity` smallint unsigned NOT NULL DEFAULT '1' COMMENT '购买数量（每次配送件数）',
    `total_times` smallint unsigned NOT NULL COMMENT '总配送次数（期数×每期次数）',
    `total_quantity` smallint unsigned NOT NULL COMMENT '总配送商品数量（总次数×购买数量）',
    `completed_times` smallint unsigned NOT NULL DEFAULT '0' COMMENT '已完成次数',
    `cycle_type` tinyint unsigned NOT NULL COMMENT '配送周期类型：1-每日一次，2-每周一次，3-每月一次，4-多周一次，5-多月一次，6-每周多次，7-每月多次',
    `cycle_interval` tinyint unsigned DEFAULT NULL COMMENT '周期间隔',
    `times_per_cycle` tinyint unsigned DEFAULT NULL COMMENT '每周期期数',
    `selected_weekdays` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户选择的配送星期JSON数组',
    `selected_days` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户选择的配送日期JSON数组',
    `selected_time_slot` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户选择的配送时间段',
    `next_delivery_date` date DEFAULT NULL COMMENT '下次配送日期',
    `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '状态：0-待支付，1-进行中，2-已暂停，3-已完成，4-已取消',
    `create_time` int unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
    `update_time` int unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
    `parent_order_data` mediumtext COMMENT '父订单信息JSON（用于定时任务生成子订单时创建拆分日志和派送订单）',
    PRIMARY KEY (`order_cycle_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_sn` (`order_sn`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_shop_id` (`shop_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_status` (`status`),
    KEY `idx_next_delivery_date` (`next_delivery_date`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci COMMENT='订单周期购计划表';

-- 4) 周期购配送计划表：order_cycle_delivery
CREATE TABLE IF NOT EXISTS `order_cycle_delivery` (
                                                      `delivery_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '配送计划ID',
                                                      `order_cycle_id` int unsigned NOT NULL COMMENT '关联周期购计划ID',
                                                      `order_id` int unsigned NOT NULL COMMENT '主订单ID',
                                                      `sub_order_id` int unsigned DEFAULT NULL COMMENT '子订单ID（生成后关联）',
                                                      `sub_order_sn` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '子订单号',
    `period_index` tinyint unsigned NOT NULL COMMENT '第几期（从1开始）',
    `time_index` tinyint unsigned NOT NULL COMMENT '期内第几次（从1开始）',
    `delivery_date` date NOT NULL COMMENT '计划配送日期',
    `delivery_date_text` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '配送日期文本，如：12月30日（周一）',
    `delivery_time_slot` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '配送时间段',
    `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '状态：0-待生成，1-已生成待发货，2-已发货，3-已完成，4-已取消',
    `generate_time` int unsigned DEFAULT NULL COMMENT '订单生成时间',
    `create_time` int unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
    `update_time` int unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
    PRIMARY KEY (`delivery_id`),
    KEY `idx_order_cycle_id` (`order_cycle_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_sub_order_id` (`sub_order_id`),
    KEY `idx_delivery_date` (`delivery_date`),
    KEY `idx_status` (`status`),
    KEY `idx_status_date` (`status`, `delivery_date`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci COMMENT='周期购配送计划表';

-- ==========================================
-- 二、补齐/升级字段（存在则跳过）
-- ==========================================

-- 1) product 表：delivery_mode
SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'product'
      AND column_name = 'delivery_mode'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `product` ADD COLUMN `delivery_mode` tinyint unsigned NOT NULL DEFAULT ''1'' COMMENT ''交付模式：1-单次交付，2-多次交付（周期购）'' AFTER `product_type`',
    'SELECT ''product.delivery_mode字段已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- product 表：idx_delivery_mode
SET @index_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'product'
      AND index_name = 'idx_delivery_mode'
);
SET @sql = IF(@index_exists = 0,
    'ALTER TABLE `product` ADD INDEX `idx_delivery_mode` (`delivery_mode`)',
    'SELECT ''product.idx_delivery_mode索引已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 历史数据迁移：delivery_mode 为 NULL 的补为 1（如果字段是可空的历史版本）
SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'product'
      AND column_name = 'delivery_mode'
      AND is_nullable = 'YES'
);
SET @sql = IF(@column_exists > 0,
    'UPDATE `product` SET `delivery_mode` = 1 WHERE `delivery_mode` IS NULL',
    'SELECT ''product.delivery_mode为NOT NULL或字段不存在，跳过NULL迁移'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) order 表：周期购相关字段（is_cycle_order / parent_order_id / parent_order_sn / delivery_date / delivery_time_slot / period_index / time_index）
SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'order'
      AND column_name = 'is_cycle_order'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `order` ADD COLUMN `is_cycle_order` tinyint unsigned NOT NULL DEFAULT ''0'' COMMENT ''是否周期购订单：0-否，1-是'' AFTER `order_type`',
    'SELECT ''order.is_cycle_order字段已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'order'
      AND column_name = 'parent_order_id'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `order` ADD COLUMN `parent_order_id` int unsigned DEFAULT NULL COMMENT ''父订单ID（周期购子订单关联主订单）'' AFTER `is_cycle_order`',
    'SELECT ''order.parent_order_id字段已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'order'
      AND column_name = 'parent_order_sn'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `order` ADD COLUMN `parent_order_sn` varchar(20) DEFAULT NULL COMMENT ''父订单号（周期购子订单关联主订单）'' AFTER `parent_order_id`',
    'SELECT ''order.parent_order_sn字段已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'order'
      AND column_name = 'delivery_date'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `order` ADD COLUMN `delivery_date` date DEFAULT NULL COMMENT ''计划配送日期（周期购子订单）''',
    'SELECT ''order.delivery_date字段已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'order'
      AND column_name = 'delivery_time_slot'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `order` ADD COLUMN `delivery_time_slot` varchar(20) DEFAULT NULL COMMENT ''配送时间段（周期购子订单）''',
    'SELECT ''order.delivery_time_slot字段已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'order'
      AND column_name = 'period_index'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `order` ADD COLUMN `period_index` tinyint unsigned DEFAULT NULL COMMENT ''第几期（周期购子订单）''',
    'SELECT ''order.period_index字段已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'order'
      AND column_name = 'time_index'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `order` ADD COLUMN `time_index` tinyint unsigned DEFAULT NULL COMMENT ''期内第几次（周期购子订单）''',
    'SELECT ''order.time_index字段已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- order 表：索引 idx_is_cycle_order / idx_parent_order_id
SET @index_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'order'
      AND index_name = 'idx_is_cycle_order'
);
SET @sql = IF(@index_exists = 0,
    'ALTER TABLE `order` ADD INDEX `idx_is_cycle_order` (`is_cycle_order`)',
    'SELECT ''order.idx_is_cycle_order索引已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'order'
      AND index_name = 'idx_parent_order_id'
);
SET @sql = IF(@index_exists = 0,
    'ALTER TABLE `order` ADD INDEX `idx_parent_order_id` (`parent_order_id`)',
    'SELECT ''order.idx_parent_order_id索引已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) cart 表：cycle_spec_id（周期购规格ID）
SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'cart'
      AND column_name = 'cycle_spec_id'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `cart` ADD COLUMN `cycle_spec_id` int unsigned DEFAULT NULL COMMENT ''周期购规格ID（周期购商品时使用）''',
    'SELECT ''cart.cycle_spec_id字段已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4) product_cycle 表：order_cutoff_days / order_cutoff_time（老版本表可能缺失）
SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'product_cycle'
      AND column_name = 'order_cutoff_days'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `product_cycle` ADD COLUMN `order_cutoff_days` tinyint unsigned NOT NULL DEFAULT ''0'' COMMENT ''截单提前天数（买家需提前X天下单）'' AFTER `delivery_time_slots`',
    'SELECT ''product_cycle.order_cutoff_days字段已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'product_cycle'
      AND column_name = 'order_cutoff_time'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `product_cycle` ADD COLUMN `order_cutoff_time` varchar(10) DEFAULT NULL COMMENT ''截单时间点（如：10:35，在此时间前下单才能最近配送）'' AFTER `order_cutoff_days`',
    'SELECT ''product_cycle.order_cutoff_time字段已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5) order_cycle 表：parent_order_data（老版本表可能缺失）
SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'order_cycle'
      AND column_name = 'parent_order_data'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `order_cycle` ADD COLUMN `parent_order_data` mediumtext COMMENT ''父订单信息JSON（用于定时任务生成子订单时创建拆分日志和派送订单）'' AFTER `update_time`',
    'SELECT ''order_cycle.parent_order_data字段已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6) product_cycle_spec 表：disabled + idx_disabled（老版本表可能缺失）
SET @column_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'product_cycle_spec'
      AND column_name = 'disabled'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `product_cycle_spec` ADD COLUMN `disabled` tinyint unsigned NOT NULL DEFAULT 0 COMMENT ''禁用状态：0-启用，1-禁用（SKU单独设置时，表示该SKU不支持此周期规格）'' AFTER `cycle_price`',
    'SELECT ''product_cycle_spec.disabled字段已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
    SELECT COUNT(*) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'product_cycle_spec'
      AND index_name = 'idx_disabled'
);
SET @sql = IF(@index_exists = 0,
    'ALTER TABLE `product_cycle_spec` ADD INDEX `idx_disabled` (`disabled`)',
    'SELECT ''product_cycle_spec.idx_disabled索引已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ==========================================
-- 三、简单校验（可选）
-- ==========================================
SELECT
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'product_cycle') AS has_product_cycle,
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'product_cycle_spec') AS has_product_cycle_spec,
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'order_cycle') AS has_order_cycle,
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'order_cycle_delivery') AS has_order_cycle_delivery;

SELECT '周期购线上整合升级脚本执行完成！' AS info, NOW() AS done_time;

-- ==========================================
-- 四、回滚提示（不建议线上直接回滚；如需回滚请走备份恢复）
-- ==========================================
-- 本脚本为“增量升级”，线上如需回滚，请优先采用：
--   1) 备份恢复（推荐）
--   2) 参考 database/cycle_order_rollback.sql（会删除周期购相关表与字段，数据会丢失）

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('paymentOrder', '', 1768268258, 1, 'admin', 1768268258, 1, 'admin', 0);

INSERT INTO `authority`(`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30001037, 'cycleBuyManage', '周期购计划', 10000414, 50, 1, '[]', '', '', 0, 'admin');

INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('cycleOrderAdvanceDays', '1', 1768275854, 1, 'admin', 1768275854, 1, 'admin', 0);

INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('cycleOrderAdvanceHours', '0', 1768275854, 1, 'admin', 1768275854, 1, 'admin', 0);

ALTER TABLE `product`
    MODIFY COLUMN `product_name` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品的名称 ' AFTER `product_id`;