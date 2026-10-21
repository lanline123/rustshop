-- =============================================================
-- 5.8.52 -> 5.8.53 增量脚本
-- 内容：v5.8.53 需求与 Bug 迭代增量归档
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- 来源：D1139-mobile-fuse、D1158-scm、D1162-reasonConfig、D1165-product-sku-status、D1174-distInvCustView
-- =============================================================

-- =============================================================
-- 来源文件：database/D1139-mobile-fuse.sql
-- =============================================================
-- =============================================================
-- 分支 D1139-mobile-fuse 增量脚本
-- 内容：商品详情页装修（h5ProductDetail / pageType=8）
--       移动端搜索页装修（h5Search / decorate_type=1 + page_type=6）
--   1. page_type 枚举注释补充 8=商品详情页（仅改注释，字段已存在）
--   2. 商品详情页、移动端搜索页不再由 SQL 预置默认 JSON
--      默认数据统一由 ProductPageDecorateDefaults 维护，并由 DecorateServiceImpl
--      根据 tigshop.is-overseas 选择普通版或跨境版；后台首次进入时创建真实装修记录。
--      普通版：商品详情 A1、搜索 layoutStyle=1；跨境版：商品详情 A3、搜索 layoutStyle=2。
--      数据库已有装修始终优先，禁止通过升级脚本覆盖客户已保存的数据。
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- 幂等：ALTER 仅改注释，可重复执行
-- =============================================================

SET NAMES utf8mb4;

-- 1. page_type 枚举注释补充 8=商品详情页（幂等）
ALTER TABLE `decorate`
    MODIFY COLUMN `page_type` TINYINT NOT NULL DEFAULT 1
    COMMENT '页面类型:1首页 2个人中心 3活动 4分类 5登录 6搜索 8商品详情页';

-- =============================================================
-- 来源文件：database/D1158-scm.sql
-- =============================================================
-- =============================================================================
-- D1158-scm 自建供应商采购入库增量脚本
-- DDL 说明：本文件的 ALTER/CREATE 仅执行一次；请通过发布迁移台账控制重复执行。
-- DML 说明：配置和菜单均按业务键幂等补齐。
-- 边界：不修改 suppliers，不写任何 *update.sql 或基线 dump。
-- =============================================================================

ALTER TABLE `product_stock_record`
    MODIFY COLUMN `stock_code` varchar(32) DEFAULT NULL COMMENT '仓库编号';

-- DDL 逐列/逐索引预检，保证已执行过旧版 D1158 的库可安全重跑。
SET @schema_name = DATABASE();

SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor' AND COLUMN_NAME='source_type'), 'SELECT 1', 'ALTER TABLE `vendor` ADD COLUMN `source_type` tinyint NOT NULL DEFAULT 1 COMMENT ''来源类型：1入驻供应商，2自建供应商'' AFTER `vendor_id`');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor' AND COLUMN_NAME='shop_id'), 'SELECT 1', 'ALTER TABLE `vendor` ADD COLUMN `shop_id` int NOT NULL DEFAULT 0 COMMENT ''自建供应商所属组织'' AFTER `source_type`');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor' AND COLUMN_NAME='vendor_code'), 'SELECT 1', 'ALTER TABLE `vendor` ADD COLUMN `vendor_code` varchar(80) DEFAULT NULL COMMENT ''自建供应商编码'' AFTER `shop_id`');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor' AND COLUMN_NAME='purchase_category_id'), 'SELECT 1', 'ALTER TABLE `vendor` ADD COLUMN `purchase_category_id` bigint DEFAULT NULL COMMENT ''采购供应商分类ID'' AFTER `vendor_code`');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor' AND COLUMN_NAME='self_vendor_name_key'), 'SELECT 1', 'ALTER TABLE `vendor` ADD COLUMN `self_vendor_name_key` varchar(320) GENERATED ALWAYS AS (CASE WHEN `source_type` = 2 THEN CONCAT(`shop_id`, '':'', TRIM(`vendor_name`)) ELSE NULL END) STORED');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor' AND COLUMN_NAME='self_vendor_code_key'), 'SELECT 1', 'ALTER TABLE `vendor` ADD COLUMN `self_vendor_code_key` varchar(160) GENERATED ALWAYS AS (CASE WHEN `source_type` = 2 AND `vendor_code` IS NOT NULL AND TRIM(`vendor_code`) <> '''' THEN CONCAT(`shop_id`, '':'', TRIM(`vendor_code`)) ELSE NULL END) STORED');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor' AND INDEX_NAME='uk_vendor_self_name'), 'SELECT 1', 'ALTER TABLE `vendor` ADD UNIQUE KEY `uk_vendor_self_name` (`self_vendor_name_key`)');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor' AND INDEX_NAME='uk_vendor_self_code'), 'SELECT 1', 'ALTER TABLE `vendor` ADD UNIQUE KEY `uk_vendor_self_code` (`self_vendor_code_key`)');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor' AND INDEX_NAME='idx_vendor_source_shop_status'), 'SELECT 1', 'ALTER TABLE `vendor` ADD KEY `idx_vendor_source_shop_status` (`source_type`, `shop_id`, `status`)');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor' AND INDEX_NAME='idx_vendor_purchase_category'), 'SELECT 1', 'ALTER TABLE `vendor` ADD KEY `idx_vendor_purchase_category` (`purchase_category_id`)');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE `vendor` SET `source_type` = 1 WHERE `source_type` IS NULL OR `source_type` = 0;

SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor_account' AND COLUMN_NAME='bank_region_ids'), 'SELECT 1', 'ALTER TABLE `vendor_account` ADD COLUMN `bank_region_ids` varchar(255) DEFAULT NULL COMMENT ''开户地区ID列表JSON'' AFTER `bank_branch`');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor_account' AND COLUMN_NAME='payment_method'), 'SELECT 1', 'ALTER TABLE `vendor_account` ADD COLUMN `payment_method` varchar(80) DEFAULT NULL COMMENT ''付款方式原始值'' AFTER `bank_region_ids`');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor_product' AND COLUMN_NAME='source_product_id'), 'SELECT 1', 'ALTER TABLE `vendor_product` ADD COLUMN `source_product_id` int DEFAULT NULL COMMENT ''来源店铺/门店商品ID'' AFTER `vendor_id`');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor_product' AND COLUMN_NAME='active_source_product_key'), 'SELECT 1', 'ALTER TABLE `vendor_product` ADD COLUMN `active_source_product_key` varchar(100) GENERATED ALWAYS AS (CASE WHEN `source_product_id` IS NOT NULL AND `is_del` = 0 THEN CONCAT(`vendor_id`, '':'', `source_product_id`) ELSE NULL END) STORED');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor_product' AND INDEX_NAME='uk_vendor_product_source'), 'SELECT 1', 'ALTER TABLE `vendor_product` ADD UNIQUE KEY `uk_vendor_product_source` (`active_source_product_key`)');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor_product' AND INDEX_NAME='idx_vendor_product_source_state'), 'SELECT 1', 'ALTER TABLE `vendor_product` ADD KEY `idx_vendor_product_source_state` (`source_product_id`, `product_state`, `is_del`)');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor_product_sku' AND COLUMN_NAME='source_sku_id'), 'SELECT 1', 'ALTER TABLE `vendor_product_sku` ADD COLUMN `source_sku_id` int DEFAULT NULL COMMENT ''来源店铺/门店SKU ID，无规格为0'' AFTER `vendor_product_id`');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor_product_sku' AND COLUMN_NAME='min_purchase_qty'), 'SELECT 1', 'ALTER TABLE `vendor_product_sku` ADD COLUMN `min_purchase_qty` int NOT NULL DEFAULT 1 COMMENT ''最低采购数量'' AFTER `source_sku_id`');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor_product_sku' AND COLUMN_NAME='active_source_sku_key'), 'SELECT 1', 'ALTER TABLE `vendor_product_sku` ADD COLUMN `active_source_sku_key` varchar(100) GENERATED ALWAYS AS (CASE WHEN `source_sku_id` IS NOT NULL AND `is_del` = 0 THEN CONCAT(`vendor_product_id`, '':'', `source_sku_id`) ELSE NULL END) STORED');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor_product_sku' AND INDEX_NAME='uk_vendor_product_source_sku'), 'SELECT 1', 'ALTER TABLE `vendor_product_sku` ADD UNIQUE KEY `uk_vendor_product_source_sku` (`active_source_sku_key`)');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@schema_name AND TABLE_NAME='vendor_product_sku' AND INDEX_NAME='idx_vendor_product_source_sku'), 'SELECT 1', 'ALTER TABLE `vendor_product_sku` ADD KEY `idx_vendor_product_source_sku` (`source_sku_id`, `is_del`)');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `purchase_supplier_category` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `shop_id` int NOT NULL COMMENT '组织ID，0为平台自营',
    `category_name` varchar(100) NOT NULL COMMENT '分类名称',
    `sort_order` int NOT NULL DEFAULT 50,
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，2停用',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` bigint NOT NULL DEFAULT 0,
    `create_by_id` bigint NOT NULL DEFAULT 0,
    `create_by_name` varchar(255) NOT NULL DEFAULT '',
    `update_time` bigint NOT NULL DEFAULT 0,
    `update_by_id` bigint NOT NULL DEFAULT 0,
    `update_by_name` varchar(255) NOT NULL DEFAULT '',
    `is_del` tinyint NOT NULL DEFAULT 0,
    `active_name_key` varchar(160)
        GENERATED ALWAYS AS (CASE WHEN `is_del` = 0 THEN CONCAT(`shop_id`, ':', TRIM(`category_name`)) ELSE NULL END) STORED,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_purchase_supplier_category_name` (`active_name_key`),
    KEY `idx_purchase_supplier_category_shop` (`shop_id`, `sort_order`),
    KEY `idx_purchase_supplier_category_shop_status` (`shop_id`, `status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购供应商分类';

CREATE TABLE IF NOT EXISTS `purchase_setting` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `shop_id` int NOT NULL COMMENT '组织ID；仅存shop_id大于0的覆盖配置',
    `auto_finish_days` int DEFAULT NULL COMMENT '预计送达后自动结束天数，空为继承',
    `allow_over_receive` tinyint DEFAULT NULL COMMENT '是否允许超收，空为继承',
    `create_time` bigint NOT NULL DEFAULT 0,
    `create_by_id` bigint NOT NULL DEFAULT 0,
    `create_by_name` varchar(255) NOT NULL DEFAULT '',
    `update_time` bigint NOT NULL DEFAULT 0,
    `update_by_id` bigint NOT NULL DEFAULT 0,
    `update_by_name` varchar(255) NOT NULL DEFAULT '',
    `is_del` tinyint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_purchase_setting_shop` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='组织采购设置';

CREATE TABLE IF NOT EXISTS `purchase_order` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `order_sn` varchar(64) NOT NULL COMMENT '采购订单号',
    `type` tinyint NOT NULL DEFAULT 1 COMMENT '单据类型：1采购订单，2采购入库单',
    `voucher_url` varchar(512) DEFAULT NULL COMMENT '入库凭证图片地址，仅采购入库单使用',
    `shop_id` int NOT NULL COMMENT '组织ID，0为平台自营',
    `vendor_id` int NOT NULL,
    `vendor_name` varchar(255) NOT NULL DEFAULT '' COMMENT '供应商名称快照',
    `vendor_code` varchar(80) NOT NULL DEFAULT '' COMMENT '供应商编码快照',
    `vendor_contact` varchar(255) NOT NULL DEFAULT '' COMMENT '供应商主联系方式快照',
    `vendor_account_snapshot` text COMMENT '银行账户快照JSON',
    `status` tinyint NOT NULL COMMENT '1待入库，2部分入库，3已完成，4已关闭',
    `expected_delivery_date` bigint NOT NULL COMMENT '预计送达日期',
    `shipping_method` tinyint NOT NULL COMMENT '1物流，2无需物流',
    `logistics_id` int DEFAULT NULL,
    `logistics_name` varchar(120) DEFAULT NULL,
    `tracking_no` varchar(120) DEFAULT NULL,
    `remark` varchar(500) DEFAULT NULL,
    `finish_reason` varchar(500) DEFAULT NULL,
    `product_amount` decimal(15,2) NOT NULL DEFAULT 0.00,
    `ordered_quantity` int NOT NULL DEFAULT 0,
    `received_quantity` int NOT NULL DEFAULT 0,
    `finish_time` bigint DEFAULT NULL,
    `finish_by_id` bigint DEFAULT NULL,
    `finish_by_name` varchar(255) DEFAULT NULL,
    `version` int NOT NULL DEFAULT 0,
    `create_time` bigint NOT NULL DEFAULT 0,
    `create_by_id` bigint NOT NULL DEFAULT 0,
    `create_by_name` varchar(255) NOT NULL DEFAULT '',
    `update_time` bigint NOT NULL DEFAULT 0,
    `update_by_id` bigint NOT NULL DEFAULT 0,
    `update_by_name` varchar(255) NOT NULL DEFAULT '',
    `is_del` tinyint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_purchase_order_sn` (`order_sn`),
    KEY `idx_purchase_order_shop_status` (`shop_id`, `status`, `expected_delivery_date`),
    KEY `idx_purchase_order_shop_type_status` (`shop_id`, `type`, `status`, `create_time`),
    KEY `idx_purchase_order_vendor_date` (`shop_id`, `vendor_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购订单';

CREATE TABLE IF NOT EXISTS `purchase_order_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `order_id` bigint NOT NULL,
    `vendor_product_id` bigint NOT NULL,
    `vendor_product_sku_id` bigint NOT NULL,
    `product_id` int NOT NULL COMMENT '来源商品ID',
    `sku_id` int NOT NULL DEFAULT 0 COMMENT '来源SKU ID，无规格为0',
    `product_name` varchar(768) NOT NULL DEFAULT '',
    `sku_name` varchar(255) NOT NULL DEFAULT '',
    `product_sn` varchar(120) NOT NULL DEFAULT '',
    `sku_sn` varchar(120) NOT NULL DEFAULT '',
    `pic_thumb` varchar(512) NOT NULL DEFAULT '',
    `product_unit` varchar(40) NOT NULL DEFAULT '',
    `quantity` int NOT NULL,
    `received_quantity` int NOT NULL DEFAULT 0,
    `purchase_price` decimal(15,2) NOT NULL,
    `amount` decimal(15,2) NOT NULL,
    `create_time` bigint NOT NULL DEFAULT 0,
    `create_by_id` bigint NOT NULL DEFAULT 0,
    `create_by_name` varchar(255) NOT NULL DEFAULT '',
    `update_time` bigint NOT NULL DEFAULT 0,
    `update_by_id` bigint NOT NULL DEFAULT 0,
    `update_by_name` varchar(255) NOT NULL DEFAULT '',
    `is_del` tinyint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_purchase_order_item_sku` (`order_id`, `vendor_product_sku_id`),
    KEY `idx_purchase_order_item_product` (`product_id`, `sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购订单明细';

CREATE TABLE IF NOT EXISTS `purchase_receipt` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `receipt_sn` varchar(64) NOT NULL,
    `order_id` bigint NOT NULL,
    `shop_id` int NOT NULL,
    `vendor_id` int NOT NULL,
    `business_date` bigint NOT NULL,
    `status` tinyint NOT NULL COMMENT '1待确认，2已确认，3已失效',
    `total_quantity` int NOT NULL DEFAULT 0,
    `total_amount` decimal(15,2) NOT NULL DEFAULT 0.00,
    `stock_record_id` int DEFAULT NULL,
    `reconciliation_id` bigint DEFAULT NULL COMMENT '对账占用ID',
    `remark` varchar(500) DEFAULT NULL,
    `confirm_time` bigint DEFAULT NULL,
    `confirm_by_id` bigint DEFAULT NULL,
    `confirm_by_name` varchar(255) DEFAULT NULL,
    `version` int NOT NULL DEFAULT 0,
    `create_time` bigint NOT NULL DEFAULT 0,
    `create_by_id` bigint NOT NULL DEFAULT 0,
    `create_by_name` varchar(255) NOT NULL DEFAULT '',
    `update_time` bigint NOT NULL DEFAULT 0,
    `update_by_id` bigint NOT NULL DEFAULT 0,
    `update_by_name` varchar(255) NOT NULL DEFAULT '',
    `is_del` tinyint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_purchase_receipt_sn` (`receipt_sn`),
    KEY `idx_purchase_receipt_shop_status_date` (`shop_id`, `status`, `business_date`),
    KEY `idx_purchase_receipt_shop_status_create` (`shop_id`, `status`, `create_time`),
    KEY `idx_purchase_receipt_order` (`order_id`, `status`),
    KEY `idx_purchase_receipt_vendor_reconciliation` (`shop_id`, `vendor_id`, `reconciliation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购入库单';

CREATE TABLE IF NOT EXISTS `purchase_receipt_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `receipt_id` bigint NOT NULL,
    `order_item_id` bigint NOT NULL,
    `product_id` int NOT NULL,
    `sku_id` int NOT NULL DEFAULT 0,
    `product_name` varchar(768) NOT NULL DEFAULT '',
    `sku_name` varchar(255) NOT NULL DEFAULT '',
    `product_sn` varchar(120) NOT NULL DEFAULT '',
    `sku_sn` varchar(120) NOT NULL DEFAULT '',
    `pic_thumb` varchar(512) NOT NULL DEFAULT '',
    `product_unit` varchar(40) NOT NULL DEFAULT '',
    `quantity` int NOT NULL,
    `returned_quantity` int NOT NULL DEFAULT 0,
    `purchase_price` decimal(15,2) NOT NULL,
    `amount` decimal(15,2) NOT NULL,
    `create_time` bigint NOT NULL DEFAULT 0,
    `create_by_id` bigint NOT NULL DEFAULT 0,
    `create_by_name` varchar(255) NOT NULL DEFAULT '',
    `update_time` bigint NOT NULL DEFAULT 0,
    `update_by_id` bigint NOT NULL DEFAULT 0,
    `update_by_name` varchar(255) NOT NULL DEFAULT '',
    `is_del` tinyint NOT NULL DEFAULT 0,
    `active_order_item_key` varchar(100)
        GENERATED ALWAYS AS (CASE WHEN `is_del` = 0 THEN CONCAT(`receipt_id`, ':', `order_item_id`) ELSE NULL END) STORED,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_purchase_receipt_item_order_item` (`active_order_item_key`),
    KEY `idx_purchase_receipt_item_product` (`product_id`, `sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购入库明细';

CREATE TABLE IF NOT EXISTS `purchase_return` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `return_sn` varchar(64) NOT NULL,
    `order_id` bigint NOT NULL,
    `receipt_id` bigint NOT NULL,
    `shop_id` int NOT NULL,
    `vendor_id` int NOT NULL,
    `business_date` bigint NOT NULL,
    `status` tinyint NOT NULL COMMENT '1待确认，2已确认',
    `total_quantity` int NOT NULL DEFAULT 0,
    `total_amount` decimal(15,2) NOT NULL DEFAULT 0.00,
    `stock_record_id` int DEFAULT NULL,
    `reconciliation_id` bigint DEFAULT NULL COMMENT '对账占用ID',
    `reason` varchar(500) DEFAULT NULL,
    `remark` varchar(500) DEFAULT NULL,
    `confirm_time` bigint DEFAULT NULL,
    `confirm_by_id` bigint DEFAULT NULL,
    `confirm_by_name` varchar(255) DEFAULT NULL,
    `version` int NOT NULL DEFAULT 0,
    `create_time` bigint NOT NULL DEFAULT 0,
    `create_by_id` bigint NOT NULL DEFAULT 0,
    `create_by_name` varchar(255) NOT NULL DEFAULT '',
    `update_time` bigint NOT NULL DEFAULT 0,
    `update_by_id` bigint NOT NULL DEFAULT 0,
    `update_by_name` varchar(255) NOT NULL DEFAULT '',
    `is_del` tinyint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_purchase_return_sn` (`return_sn`),
    KEY `idx_purchase_return_shop_status_date` (`shop_id`, `status`, `business_date`),
    KEY `idx_purchase_return_shop_status_create` (`shop_id`, `status`, `create_time`),
    KEY `idx_purchase_return_receipt` (`receipt_id`, `status`),
    KEY `idx_purchase_return_vendor_reconciliation` (`shop_id`, `vendor_id`, `reconciliation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购退货单';

CREATE TABLE IF NOT EXISTS `purchase_return_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `return_id` bigint NOT NULL,
    `receipt_item_id` bigint NOT NULL,
    `product_id` int NOT NULL,
    `sku_id` int NOT NULL DEFAULT 0,
    `product_name` varchar(768) NOT NULL DEFAULT '',
    `sku_name` varchar(255) NOT NULL DEFAULT '',
    `product_sn` varchar(120) NOT NULL DEFAULT '',
    `sku_sn` varchar(120) NOT NULL DEFAULT '',
    `pic_thumb` varchar(512) NOT NULL DEFAULT '',
    `product_unit` varchar(40) NOT NULL DEFAULT '',
    `quantity` int NOT NULL,
    `original_price` decimal(15,2) NOT NULL,
    `return_price` decimal(15,2) NOT NULL,
    `amount` decimal(15,2) NOT NULL,
    `reason` varchar(500) DEFAULT NULL COMMENT '单项退货原因',
    `create_time` bigint NOT NULL DEFAULT 0,
    `create_by_id` bigint NOT NULL DEFAULT 0,
    `create_by_name` varchar(255) NOT NULL DEFAULT '',
    `update_time` bigint NOT NULL DEFAULT 0,
    `update_by_id` bigint NOT NULL DEFAULT 0,
    `update_by_name` varchar(255) NOT NULL DEFAULT '',
    `is_del` tinyint NOT NULL DEFAULT 0,
    `active_receipt_item_key` varchar(100)
        GENERATED ALWAYS AS (CASE WHEN `is_del` = 0 THEN CONCAT(`return_id`, ':', `receipt_item_id`) ELSE NULL END) STORED,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_purchase_return_item_receipt_item` (`active_receipt_item_key`),
    KEY `idx_purchase_return_item_product` (`product_id`, `sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购退货明细';

CREATE TABLE IF NOT EXISTS `purchase_reconciliation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `reconciliation_sn` varchar(64) NOT NULL,
    `shop_id` int NOT NULL,
    `vendor_id` int NOT NULL,
    `status` tinyint NOT NULL COMMENT '1待对账，2已对账，3已驳回，4已作废',
    `receipt_amount` decimal(15,2) NOT NULL DEFAULT 0.00,
    `return_amount` decimal(15,2) NOT NULL DEFAULT 0.00,
    `total_amount` decimal(15,2) NOT NULL DEFAULT 0.00,
    `supplier_snapshot` text NOT NULL COMMENT '供应商与联系人快照JSON',
    `bank_snapshot` text COMMENT '银行账户快照JSON',
    `remark` varchar(500) DEFAULT NULL,
    `review_remark` varchar(500) DEFAULT NULL,
    `review_time` bigint DEFAULT NULL,
    `review_by_id` bigint DEFAULT NULL,
    `review_by_name` varchar(255) DEFAULT NULL,
    `version` int NOT NULL DEFAULT 0,
    `create_time` bigint NOT NULL DEFAULT 0,
    `create_by_id` bigint NOT NULL DEFAULT 0,
    `create_by_name` varchar(255) NOT NULL DEFAULT '',
    `update_time` bigint NOT NULL DEFAULT 0,
    `update_by_id` bigint NOT NULL DEFAULT 0,
    `update_by_name` varchar(255) NOT NULL DEFAULT '',
    `is_del` tinyint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_purchase_reconciliation_sn` (`reconciliation_sn`),
    KEY `idx_purchase_reconciliation_shop_status` (`shop_id`, `status`, `create_time`),
    KEY `idx_purchase_reconciliation_vendor` (`shop_id`, `vendor_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购对账单';

CREATE TABLE IF NOT EXISTS `purchase_reconciliation_source` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `reconciliation_id` bigint NOT NULL,
    `source_type` tinyint NOT NULL COMMENT '1入库单，2退货单',
    `source_id` bigint NOT NULL,
    `source_sn` varchar(64) NOT NULL,
    `business_date` bigint NOT NULL,
    `source_amount` decimal(15,2) NOT NULL,
    `create_time` bigint NOT NULL DEFAULT 0,
    `create_by_id` bigint NOT NULL DEFAULT 0,
    `create_by_name` varchar(255) NOT NULL DEFAULT '',
    `update_time` bigint NOT NULL DEFAULT 0,
    `update_by_id` bigint NOT NULL DEFAULT 0,
    `update_by_name` varchar(255) NOT NULL DEFAULT '',
    `is_del` tinyint NOT NULL DEFAULT 0,
    `active_source_key` varchar(120)
        GENERATED ALWAYS AS (CASE WHEN `is_del` = 0
                                  THEN CONCAT(`reconciliation_id`, ':', `source_type`, ':', `source_id`) ELSE NULL END) STORED,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_purchase_reconciliation_source` (`active_source_key`),
    KEY `idx_purchase_reconciliation_source_document` (`source_type`, `source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购对账来源单据快照';

CREATE TABLE IF NOT EXISTS `purchase_reconciliation_item` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `reconciliation_id` bigint NOT NULL,
    `source_type` tinyint NOT NULL,
    `source_id` bigint NOT NULL,
    `source_item_id` bigint NOT NULL,
    `product_id` int NOT NULL,
    `sku_id` int NOT NULL DEFAULT 0,
    `product_name` varchar(768) NOT NULL DEFAULT '',
    `sku_name` varchar(255) NOT NULL DEFAULT '',
    `product_sn` varchar(120) NOT NULL DEFAULT '',
    `sku_sn` varchar(120) NOT NULL DEFAULT '',
    `pic_thumb` varchar(512) NOT NULL DEFAULT '',
    `product_unit` varchar(40) NOT NULL DEFAULT '',
    `quantity` int NOT NULL,
    `price` decimal(15,2) NOT NULL,
    `amount` decimal(15,2) NOT NULL,
    `create_time` bigint NOT NULL DEFAULT 0,
    `create_by_id` bigint NOT NULL DEFAULT 0,
    `create_by_name` varchar(255) NOT NULL DEFAULT '',
    `update_time` bigint NOT NULL DEFAULT 0,
    `update_by_id` bigint NOT NULL DEFAULT 0,
    `update_by_name` varchar(255) NOT NULL DEFAULT '',
    `is_del` tinyint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_purchase_reconciliation_item_main` (`reconciliation_id`, `source_type`, `source_id`),
    KEY `idx_purchase_reconciliation_item_product` (`product_id`, `sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购对账商品快照';

CREATE TABLE IF NOT EXISTS `purchase_operation_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `business_type` varchar(40) NOT NULL,
    `business_id` bigint NOT NULL,
    `action` varchar(40) NOT NULL,
    `from_status` tinyint DEFAULT NULL,
    `to_status` tinyint DEFAULT NULL,
    `remark` varchar(500) DEFAULT NULL,
    `create_time` bigint NOT NULL DEFAULT 0,
    `create_by_id` bigint NOT NULL DEFAULT 0,
    `create_by_name` varchar(255) NOT NULL DEFAULT '',
    `update_time` bigint NOT NULL DEFAULT 0,
    `update_by_id` bigint NOT NULL DEFAULT 0,
    `update_by_name` varchar(255) NOT NULL DEFAULT '',
    `is_del` tinyint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_purchase_operation_log_business` (`business_type`, `business_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购业务操作日志';

START TRANSACTION;

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'purchaseAllowOverReceive', '1', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'purchaseAllowOverReceive' AND `is_del` = 0);

INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'supplyChain', '供应链', 0, 56, 1, '[]', '', 'iconfont-tig icon-gongying', 0, types.`admin_type`
FROM (SELECT 'admin' AS `admin_type` UNION ALL SELECT 'shop' UNION ALL SELECT 'store') types
WHERE NOT EXISTS (
    SELECT 1 FROM `authority` a WHERE a.`authority_sn` = 'supplyChain' AND a.`admin_type` = types.`admin_type`
);

-- 平台端一级目录按 sort_order 倒序、authority_id 正序返回；
-- 供应链与订单使用同一 sort_order，依靠既有订单更小的 authority_id 稳定排在其后。
UPDATE `authority` supply_chain
LEFT JOIN (
    SELECT `admin_type`, MAX(`sort_order`) AS `sort_order`
    FROM `authority`
    WHERE `authority_sn` = 'order'
      AND `parent_id` = 0
      AND `admin_type` IN ('admin', 'shop', 'store')
    GROUP BY `admin_type`
) order_menu ON order_menu.`admin_type` = supply_chain.`admin_type`
SET supply_chain.`authority_name` = '供应链',
    supply_chain.`parent_id` = 0,
    supply_chain.`sort_order` = COALESCE(order_menu.`sort_order`, 56),
    supply_chain.`is_show` = 1,
    supply_chain.`child_auth` = '[]',
    supply_chain.`route_link` = '',
    supply_chain.`authority_ico` = 'iconfont-tig icon-gongying',
    supply_chain.`is_system` = 0
WHERE supply_chain.`authority_sn` = 'supplyChain'
  AND supply_chain.`admin_type` IN ('admin', 'shop', 'store');

INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT menu_groups.`authority_sn`, menu_groups.`authority_name`, root.`authority_id`, menu_groups.`sort_order`, 1,
       '[]', menu_groups.`route_link`, menu_groups.`authority_ico`, 0, root.`admin_type`
FROM (
    SELECT MIN(`authority_id`) AS `authority_id`, `admin_type`
    FROM `authority`
    WHERE `authority_sn` = 'supplyChain'
      AND `admin_type` IN ('admin', 'shop', 'store')
    GROUP BY `admin_type`
) root
JOIN (
    SELECT 'purchaseManagement' AS `authority_sn`, '采购管理' AS `authority_name`, 50 AS `sort_order`,
           'supply_chain/purchase_order/list/' AS `route_link`, 'iconfont-tig icon-kucunguanli' AS `authority_ico`
    UNION ALL SELECT 'selfVendorManagement', '供应商管理', 51, 'supply_chain/supplier/list/', 'iconfont-tig icon-gongying'
    UNION ALL SELECT 'supplyChainSetting', '设置', 52, 'supply_chain/purchase_setting/index/', 'iconfont-tig icon-shezhi'
) menu_groups
WHERE NOT EXISTS (
    SELECT 1 FROM `authority` existed
    WHERE existed.`authority_sn` = menu_groups.`authority_sn`
      AND existed.`admin_type` = root.`admin_type`
);

UPDATE `authority` menu_group
JOIN `authority` root
  ON root.`authority_sn` = 'supplyChain'
 AND root.`admin_type` = menu_group.`admin_type`
JOIN (
    SELECT 'purchaseManagement' AS `authority_sn`, '采购管理' AS `authority_name`, 50 AS `sort_order`,
           'supply_chain/purchase_order/list/' AS `route_link`, 'iconfont-tig icon-kucunguanli' AS `authority_ico`
    UNION ALL SELECT 'selfVendorManagement', '供应商管理', 51, 'supply_chain/supplier/list/', 'iconfont-tig icon-gongying'
    UNION ALL SELECT 'supplyChainSetting', '设置', 52, 'supply_chain/purchase_setting/index/', 'iconfont-tig icon-shezhi'
) menu_groups ON menu_groups.`authority_sn` = menu_group.`authority_sn`
SET menu_group.`authority_name` = menu_groups.`authority_name`,
    menu_group.`parent_id` = root.`authority_id`,
    menu_group.`sort_order` = menu_groups.`sort_order`,
    menu_group.`is_show` = 1,
    menu_group.`child_auth` = '[]',
    menu_group.`route_link` = menu_groups.`route_link`,
    menu_group.`authority_ico` = menu_groups.`authority_ico`,
    menu_group.`is_system` = 0
WHERE menu_group.`admin_type` IN ('admin', 'shop', 'store');

INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT pages.`authority_sn`, pages.`authority_name`, parent.`authority_id`, pages.`sort_order`, 1,
       CONCAT('[{"authName":"编辑","authSn":"', pages.`edit_sn`, '"}]'), pages.`route_link`, '', 0, parent.`admin_type`
FROM (
    SELECT MIN(`authority_id`) AS `authority_id`, `authority_sn`, `admin_type`
    FROM `authority`
    WHERE `authority_sn` IN ('purchaseManagement', 'selfVendorManagement', 'supplyChainSetting')
      AND `admin_type` IN ('admin', 'shop', 'store')
    GROUP BY `authority_sn`, `admin_type`
) parent
JOIN (
    SELECT 'purchaseOrderManage' AS `authority_sn`, '采购订单' AS `authority_name`, 50 AS `sort_order`,
           'purchaseOrderModifyManage' AS `edit_sn`, 'supply_chain/purchase_order/list/' AS `route_link`, 'purchaseManagement' AS `parent_sn`
    UNION ALL SELECT 'purchaseReceiptManage', '采购入库单', 51, 'purchaseReceiptModifyManage', 'supply_chain/purchase_receipt/list/', 'purchaseManagement'
    UNION ALL SELECT 'purchaseReturnManage', '采购退货单', 52, 'purchaseReturnModifyManage', 'supply_chain/purchase_return/list/', 'purchaseManagement'
    UNION ALL SELECT 'purchaseReconciliationManage', '采购对账单', 53, 'purchaseReconciliationModifyManage', 'supply_chain/purchase_reconciliation/list/', 'purchaseManagement'
    UNION ALL SELECT 'selfVendorManage', '供应商资料', 50, 'selfVendorModifyManage', 'supply_chain/supplier/list/', 'selfVendorManagement'
    UNION ALL SELECT 'selfVendorCategoryManage', '供应商分类', 51, 'selfVendorCategoryModifyManage', 'supply_chain/supplier_category/list/', 'selfVendorManagement'
    UNION ALL SELECT 'selfVendorProductManage', '供应商商品', 52, 'selfVendorProductModifyManage', 'supply_chain/supplier_product/list/', 'selfVendorManagement'
    UNION ALL SELECT 'purchaseSettingManage', '采购设置', 50, 'purchaseSettingModifyManage', 'supply_chain/purchase_setting/index/', 'supplyChainSetting'
) pages ON pages.`parent_sn` = parent.`authority_sn`
WHERE NOT EXISTS (
      SELECT 1 FROM `authority` existed
      WHERE existed.`authority_sn` = pages.`authority_sn` AND existed.`admin_type` = parent.`admin_type`
  );

UPDATE `authority` page
JOIN `authority` parent
  ON parent.`admin_type` = page.`admin_type`
JOIN (
    SELECT 'purchaseOrderManage' AS `authority_sn`, '采购订单' AS `authority_name`, 50 AS `sort_order`,
           'purchaseOrderModifyManage' AS `edit_sn`, 'supply_chain/purchase_order/list/' AS `route_link`, 'purchaseManagement' AS `parent_sn`
    UNION ALL SELECT 'purchaseReceiptManage', '采购入库单', 51, 'purchaseReceiptModifyManage', 'supply_chain/purchase_receipt/list/', 'purchaseManagement'
    UNION ALL SELECT 'purchaseReturnManage', '采购退货单', 52, 'purchaseReturnModifyManage', 'supply_chain/purchase_return/list/', 'purchaseManagement'
    UNION ALL SELECT 'purchaseReconciliationManage', '采购对账单', 53, 'purchaseReconciliationModifyManage', 'supply_chain/purchase_reconciliation/list/', 'purchaseManagement'
    UNION ALL SELECT 'selfVendorManage', '供应商资料', 50, 'selfVendorModifyManage', 'supply_chain/supplier/list/', 'selfVendorManagement'
    UNION ALL SELECT 'selfVendorCategoryManage', '供应商分类', 51, 'selfVendorCategoryModifyManage', 'supply_chain/supplier_category/list/', 'selfVendorManagement'
    UNION ALL SELECT 'selfVendorProductManage', '供应商商品', 52, 'selfVendorProductModifyManage', 'supply_chain/supplier_product/list/', 'selfVendorManagement'
    UNION ALL SELECT 'purchaseSettingManage', '采购设置', 50, 'purchaseSettingModifyManage', 'supply_chain/purchase_setting/index/', 'supplyChainSetting'
) pages
  ON pages.`authority_sn` = page.`authority_sn`
 AND pages.`parent_sn` = parent.`authority_sn`
SET page.`authority_name` = pages.`authority_name`,
    page.`parent_id` = parent.`authority_id`,
    page.`sort_order` = pages.`sort_order`,
    page.`is_show` = 1,
    page.`child_auth` = CONCAT('[{"authName":"编辑","authSn":"', pages.`edit_sn`, '"}]'),
    page.`route_link` = pages.`route_link`,
    page.`authority_ico` = '',
    page.`is_system` = 0
WHERE page.`admin_type` IN ('admin', 'shop', 'store');

COMMIT;


-- D1170 采购退货明细数量快照增量
-- 目标：采购退货单在保存时固化「退货前可退数量 / 本次退货数量 / 退货后剩余可退数量」，
--       退货单详情不再动态重算，可回看每次选择进货单时的 before / after。
-- 特性：结构补丁可重复执行；不删除表、不删除列、不改动历史业务数据。
-- 说明：三列可空，本次上线前的历史退货明细保持 NULL，表示当时没有留下快照，不做事后推算。

SET @schema_name = DATABASE();

SET @ddl = IF(
    EXISTS(SELECT 1 FROM information_schema.columns
           WHERE table_schema = @schema_name AND table_name = 'purchase_return_item' AND column_name = 'returnable_quantity'),
    'SELECT 1',
    'ALTER TABLE `purchase_return_item` ADD COLUMN `returnable_quantity` int DEFAULT NULL COMMENT ''退货前可退数量：入库数量-已确认退货数量'' AFTER `quantity`'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS(SELECT 1 FROM information_schema.columns
           WHERE table_schema = @schema_name AND table_name = 'purchase_return_item' AND column_name = 'available_quantity'),
    'SELECT 1',
    'ALTER TABLE `purchase_return_item` ADD COLUMN `available_quantity` int DEFAULT NULL COMMENT ''退货前可用数量：可退数量再扣同入库单其他待确认退货占用'' AFTER `returnable_quantity`'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF(
    EXISTS(SELECT 1 FROM information_schema.columns
           WHERE table_schema = @schema_name AND table_name = 'purchase_return_item' AND column_name = 'returnable_quantity_after'),
    'SELECT 1',
    'ALTER TABLE `purchase_return_item` ADD COLUMN `returnable_quantity_after` int DEFAULT NULL COMMENT ''退货后剩余可退数量：退货前可退数量-本次退货数量'' AFTER `available_quantity`'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =============================================================
-- 来源文件：database/D1162-reasonConfig.sql
-- =============================================================
-- D1162：售后与买家取消未支付订单共用原因配置
-- 影响表：config
-- 幂等策略：仅在不存在有效 biz_code 时插入，不覆盖管理员现有配置
-- 版本范围：标准版、海外版、O2O 版公共配置

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'afterSalesReasonEnabled', '1', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `config`
    WHERE `biz_code` = 'afterSalesReasonEnabled' AND `is_del` = 0
);

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'afterSalesReasonList', '["多拍/拍错/不喜欢","未按约定时间发货","协商一致退款","地址/电话填错了","其他"]', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `config`
    WHERE `biz_code` = 'afterSalesReasonList' AND `is_del` = 0
);

-- =============================================================
-- 来源文件：database/D1165-product-sku-status.sql
-- =============================================================
-- 商品 SKU 增加可恢复的启用/禁用状态；旧数据默认保持启用。本 DDL 仅执行一次。
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='product_sku' AND COLUMN_NAME='is_enabled'), 'SELECT 1', 'ALTER TABLE `product_sku` ADD COLUMN `is_enabled` tinyint unsigned NOT NULL DEFAULT ''1'' COMMENT ''是否启用：0-禁用，1-启用'' AFTER `sku_price`');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =============================================================
-- 来源文件：database/D1174-distInvCustView.sql
-- =============================================================
-- D1174：记录客户与分销员建立绑定关系时的渠道。
-- 历史绑定关系没有该信息，保持空字符串，禁止从订单 order_source 反推。
-- MySQL 5.7/8.0 不支持 ADD COLUMN IF NOT EXISTS，使用 information_schema 预检保证脚本可重复执行。
SET @d1174_channel_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'salesman_customer'
      AND COLUMN_NAME = 'channel'
);
SET @d1174_channel_sql := IF(
    @d1174_channel_exists = 0,
    'ALTER TABLE `salesman_customer` ADD COLUMN `channel` varchar(80) NOT NULL DEFAULT '''' COMMENT ''客户绑定渠道'' AFTER `add_time`',
    'SELECT 1'
);
PREPARE d1174_channel_stmt FROM @d1174_channel_sql;
EXECUTE d1174_channel_stmt;
DEALLOCATE PREPARE d1174_channel_stmt;

SET @d1174_remark_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'salesman_customer'
      AND COLUMN_NAME = 'remark'
);
SET @d1174_remark_sql := IF(
    @d1174_remark_exists = 0,
    'ALTER TABLE `salesman_customer` ADD COLUMN `remark` varchar(255) NOT NULL DEFAULT '''' COMMENT ''分销员对客户的备注'' AFTER `channel`',
    'SELECT 1'
);
PREPARE d1174_remark_stmt FROM @d1174_remark_sql;
EXECUTE d1174_remark_stmt;
DEALLOCATE PREPARE d1174_remark_stmt;
