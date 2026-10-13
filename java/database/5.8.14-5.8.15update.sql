-- ============================================
-- 账期支付功能：线上数据库整合升级脚本（增量/幂等）
-- 生成日期：2026-01-23
-- 适用场景：线上已有数据，升级/补齐账期支付相关表结构与字段
-- 重要说明：
--   1) 本脚本不会 DROP TABLE，不会删除数据
--   2) 通过 information_schema 检查后再 ALTER，尽量保证可重复执行
--   3) 执行前请务必备份数据库（结构+数据）
-- ============================================

SELECT '开始执行【账期支付】线上整合升级脚本...' AS info, NOW() AS exec_time;

-- ==========================================
-- 一、创建账期支付相关表（不存在则创建）
-- ==========================================

-- 1) 授信会员表：credit_user
CREATE TABLE IF NOT EXISTS `credit_user` (
                                             `credit_user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '授信会员ID',
    `user_id` int(11) NOT NULL COMMENT '会员ID',
    `credit_limit` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '信用额度（单位：元）',
    `used_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '已使用额度（单位：元）',
    `available_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '可用额度（单位：元）',
    `settlement_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '结算账期类型：1-月结30天，2-季结90天',
    `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `period_start_time` bigint(20) DEFAULT NULL COMMENT '当前结算周期起点（秒级时间戳），结算清空后重置',
    `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间（秒级时间戳）',
    `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间（秒级时间戳）',
    PRIMARY KEY (`credit_user_id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_status` (`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='授信会员表';

-- 2) 账期支付订单表：credit_order
CREATE TABLE IF NOT EXISTS `credit_order` (
                                              `credit_order_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '账期订单ID',
    `order_id` int(11) NOT NULL COMMENT '订单ID',
    `order_sn` varchar(64) NOT NULL DEFAULT '' COMMENT '订单号',
    `user_id` int(11) NOT NULL COMMENT '会员ID',
    `credit_user_id` int(11) NOT NULL COMMENT '授信会员ID',
    `credit_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '账期支付金额（单位：元）',
    `settlement_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '结算状态：0-未结算，1-已结算，2-已取消，3-退款中',
    `settlement_id` int(11) DEFAULT NULL COMMENT '结算记录ID',
    `settlement_sn` varchar(64) DEFAULT NULL COMMENT '回款流水单号',
    `order_time` bigint(20) DEFAULT NULL COMMENT '下单时间（秒级时间戳）',
    `settlement_time` bigint(20) DEFAULT NULL COMMENT '结算时间（秒级时间戳）',
    `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间（秒级时间戳）',
    `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间（秒级时间戳）',
    PRIMARY KEY (`credit_order_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_credit_user_id` (`credit_user_id`),
    KEY `idx_settlement_status` (`settlement_status`),
    KEY `idx_order_time` (`order_time`),
    KEY `idx_settlement_id` (`settlement_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账期支付订单表';

-- 3) 账期结算表：credit_settlement
CREATE TABLE IF NOT EXISTS `credit_settlement` (
                                                   `settlement_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '结算ID',
    `settlement_sn` varchar(64) NOT NULL DEFAULT '' COMMENT '回款流水单号',
    `user_id` int(11) NOT NULL COMMENT '会员ID',
    `credit_user_id` int(11) NOT NULL COMMENT '授信会员ID',
    `settlement_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '结算金额（单位：元）',
    `order_count` int(11) NOT NULL DEFAULT '0' COMMENT '结算订单数',
    `remark` varchar(500) DEFAULT '' COMMENT '备注',
    `settlement_time` bigint(20) DEFAULT NULL COMMENT '结算时间（秒级时间戳）',
    `settlement_status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '结算状态：1-已结算，2-已撤销',
    `cancel_time` bigint(20) DEFAULT NULL COMMENT '撤销时间（秒级时间戳）',
    `cancel_reason` varchar(500) DEFAULT NULL COMMENT '撤销原因',
    `operator_id` int(11) DEFAULT NULL COMMENT '操作人ID',
    `operator_name` varchar(64) DEFAULT '' COMMENT '操作人名称',
    `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间（秒级时间戳）',
    `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间（秒级时间戳）',
    PRIMARY KEY (`settlement_id`),
    UNIQUE KEY `uk_settlement_sn` (`settlement_sn`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_credit_user_id` (`credit_user_id`),
    KEY `idx_settlement_time` (`settlement_time`),
    KEY `idx_settlement_status` (`settlement_status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账期结算表';

-- ==========================================
-- 二、补齐字段/索引（幂等）
-- ==========================================

-- 1) order 表：新增账期支付字段 credit_amount / is_credit_order
SET @order_credit_amount_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'order'
      AND COLUMN_NAME = 'credit_amount'
);
SET @sql := IF(@order_credit_amount_exists = 0,
    'ALTER TABLE `order` ADD COLUMN `credit_amount` decimal(12,2) NOT NULL DEFAULT ''0.00'' COMMENT ''账期支付金额（单位：元）'' AFTER `balance`',
    'SELECT ''order.credit_amount 已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @order_is_credit_order_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'order'
      AND COLUMN_NAME = 'is_credit_order'
);
SET @sql := IF(@order_is_credit_order_exists = 0,
    'ALTER TABLE `order` ADD COLUMN `is_credit_order` tinyint(1) NOT NULL DEFAULT ''0'' COMMENT ''是否账期订单：0-否，1-是'' AFTER `credit_amount`',
    'SELECT ''order.is_credit_order 已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @order_is_credit_order_idx_exists := (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'order'
      AND INDEX_NAME = 'idx_is_credit_order'
);
SET @sql := IF(@order_is_credit_order_idx_exists = 0,
    'ALTER TABLE `order` ADD INDEX `idx_is_credit_order` (`is_credit_order`)',
    'SELECT ''order.idx_is_credit_order 已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) credit_user 表：补齐 period_start_time（老表升级场景）
SET @cu_period_start_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'credit_user'
      AND COLUMN_NAME = 'period_start_time'
);
SET @sql := IF(@cu_period_start_exists = 0,
    'ALTER TABLE `credit_user` ADD COLUMN `period_start_time` bigint(20) DEFAULT NULL COMMENT ''当前结算周期起点（秒级时间戳），结算清空后重置'' AFTER `status`',
    'SELECT ''credit_user.period_start_time 已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3) credit_order 表：补齐 settlement_status 注释（老表升级场景，非强依赖）
SET @co_settlement_status_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'credit_order'
      AND COLUMN_NAME = 'settlement_status'
);
SET @sql := IF(@co_settlement_status_exists = 1,
    'ALTER TABLE `credit_order` MODIFY COLUMN `settlement_status` tinyint(1) NOT NULL DEFAULT ''0'' COMMENT ''结算状态：0-未结算，1-已结算，2-已取消，3-退款中''',
    'SELECT ''credit_order.settlement_status 不存在（可能表未创建），跳过'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ==========================================
-- 三、老数据回填（幂等）
-- ==========================================

-- period_start_time 为空：按 create_time 回填（create_time 可能为毫秒或秒），再兜底当天0点
UPDATE `credit_user`
SET `period_start_time` = (
    CASE
        WHEN `create_time` IS NULL OR `create_time` = 0 THEN UNIX_TIMESTAMP(CURDATE())
        WHEN `create_time` > 1000000000000 THEN FLOOR(`create_time` / 1000)
        ELSE `create_time`
        END
    )
WHERE (`period_start_time` IS NULL OR `period_start_time` = 0);

INSERT INTO `authority`(`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES ('termPayManagement', '账期支付', 12, 50, 1, '[]', '', 'iconfont-tig icon-jiaoyiguanli', 0, 'admin');
SET @termPayManagementId = LAST_INSERT_ID();

INSERT INTO `authority`(`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES ('termOrderManage', '账期支付订单', @termPayManagementId, 50, 1, '[]', '', '', 0, 'admin');
INSERT INTO `authority`(`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES ('termPayManage', '账期结算', @termPayManagementId, 50, 1, '[{\"authName\":\"编辑\",\"authSn\":\"termPayModifyManage\"}]', '', '', 0, 'admin');
INSERT INTO `authority`(`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES ('termMemberManage', '授信客户', @termPayManagementId, 50, 1, '[{\"authName\":\"编辑\",\"authSn\":\"termMemberModifyManage\"}]', '', '', 0, 'admin');


SELECT '【账期支付】线上整合升级脚本执行完成' AS info, NOW() AS exec_time;

