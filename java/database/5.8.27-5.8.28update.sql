-- admin_role 增加系统角色标识字段
ALTER TABLE `admin_role`
    ADD COLUMN `is_system` TINYINT(1) NOT NULL DEFAULT 0
    COMMENT '是否系统内置角色 0-否 1-是';

-- 兼容老数据：将已有的系统内置角色标记为 is_system=1
UPDATE `admin_role` SET `is_system` = 1 WHERE role_id = 1;


-- ============================================================
-- 品类服务费率规则（对齐微服务表名 category_service_fee_rule*）
-- 独立增量脚本；执行前请备份数据库。
-- ============================================================

-- 规则主表
CREATE TABLE IF NOT EXISTS `category_service_fee_rule` (
                                                           `id`               int          NOT NULL AUTO_INCREMENT      COMMENT '主键',
                                                           `rule_name`        varchar(50)  NOT NULL DEFAULT ''          COMMENT '规则名称',
    `target_type`      tinyint      NOT NULL DEFAULT 1           COMMENT '适用对象类型:1店铺 2供应商',
    `scope_type`       tinyint      NOT NULL DEFAULT 2           COMMENT '适用范围:1全部 2指定 3不包含',
    `service_fee_rate` decimal(5,2) NOT NULL DEFAULT 0.00        COMMENT '平台服务费率(%)',
    `remark`           varchar(200) NOT NULL DEFAULT ''          COMMENT '备注',
    `status`           tinyint      NOT NULL DEFAULT 1           COMMENT '状态:1启用 0禁用',
    `deleted`          int          NOT NULL DEFAULT 0           COMMENT '逻辑删除:0未删 非0已删',
    `create_time`      datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time`      datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_target_type_status` (`target_type`, `status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='类目服务费率规则';

CREATE TABLE IF NOT EXISTS `category_service_fee_rule_category` (
                                                                    `id`            int          NOT NULL AUTO_INCREMENT      COMMENT '主键',
                                                                    `rule_id`       int          NOT NULL DEFAULT 0           COMMENT '规则ID',
                                                                    `category_id`   int          NOT NULL DEFAULT 0           COMMENT '品类ID',
                                                                    `category_name` varchar(100) NOT NULL DEFAULT ''          COMMENT '品类名称(冗余)',
    `category_path` varchar(500) NOT NULL DEFAULT ''          COMMENT '品类路径(冗余)',
    `deleted`       int          NOT NULL DEFAULT 0           COMMENT '逻辑删除',
    `create_time`   datetime              DEFAULT CURRENT_TIMESTAMP,
    `modify_time`   datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_rule_id` (`rule_id`),
    KEY `idx_category_id` (`category_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='费率规则品类关联';

CREATE TABLE IF NOT EXISTS `category_service_fee_rule_target` (
                                                                  `id`          int      NOT NULL AUTO_INCREMENT      COMMENT '主键',
                                                                  `rule_id`     int      NOT NULL DEFAULT 0           COMMENT '规则ID',
                                                                  `target_id`   int      NOT NULL DEFAULT 0           COMMENT '目标ID(店铺ID或供应商ID)',
                                                                  `deleted`     int      NOT NULL DEFAULT 0           COMMENT '逻辑删除',
                                                                  `create_time` datetime          DEFAULT CURRENT_TIMESTAMP,
                                                                  `modify_time` datetime          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                                  PRIMARY KEY (`id`),
    KEY `idx_rule_id` (`rule_id`),
    KEY `idx_target_id` (`target_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='费率规则目标关联';

-- record_rate 扩展（幂等：无列则 ADD）
SET @dbname = DATABASE();
SET @tablename = 'record_rate';
SET @columnname = 'category_rate_details';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname
  ) > 0,
  'SELECT 1',
  'ALTER TABLE `record_rate` ADD COLUMN `category_rate_details` text DEFAULT NULL COMMENT ''按品类的费率明细(JSON)'' AFTER `supplier_withdrawal_fee`'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 菜单权限：挂在分账管理 profitsharingManage (authority_id=11001045) 下
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'categoryFeeRuleManage', '服务费率', 11001045, 52, 1,
       '[{"auth_name":"编辑","auth_sn":"categoryFeeRuleUpdate"}]',
       'finance/category_fee_rule/list/', '', 0, 'admin'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn` = 'categoryFeeRuleManage' AND `admin_type` = 'admin');



-- 内置超级管理员角色：按 admin_type / 店铺类型区分角色名称
-- 历史数据请仅通过本脚本修正；Java init*Role 仅负责新建时写入正确名称，不会在运行时改已有角色名
-- 执行前请备份 admin_role 表

-- 标记历史内置角色（role_desc 为系统内置且名称为「超级管理员」）
UPDATE `admin_role`
SET `is_system` = 1
WHERE `role_desc` = '系统内置角色'
  AND `role_name` = '超级管理员'
  AND (`is_system` IS NULL OR `is_system` = 0);

-- 平台
UPDATE `admin_role`
SET `role_name` = '平台超级管理员'
WHERE `is_system` = 1
  AND `admin_type` = 'admin'
  AND `role_name` IN ('超级管理员', '平台超级管理员');

-- 店铺
UPDATE `admin_role`
SET `role_name` = '店铺超级管理员', `admin_type` = 'shop'
WHERE `is_system` = 1
  AND `role_name` = '超级管理员'
  AND `admin_type` = 'shop'
  AND (`shop_id` = 0 OR `shop_id` IN (SELECT `shop_id` FROM `shop` WHERE `shop_type` = 1));

UPDATE `admin_role` ar
    INNER JOIN `shop` s ON ar.`shop_id` = s.`shop_id`
    SET ar.`role_name` = '店铺超级管理员', ar.`admin_type` = 'shop'
WHERE ar.`is_system` = 1
  AND ar.`role_name` = '超级管理员'
  AND s.`shop_type` = 1;

-- 门店（含 admin_type 仍为 shop 的历史数据）
UPDATE `admin_role` ar
    INNER JOIN `shop` s ON ar.`shop_id` = s.`shop_id`
    SET ar.`role_name` = '门店超级管理员', ar.`admin_type` = 'store'
WHERE ar.`is_system` = 1
  AND s.`shop_type` = 2
  AND (ar.`role_name` = '超级管理员' OR ar.`admin_type` = 'shop');

-- 自提点
UPDATE `admin_role` ar
    INNER JOIN `shop` s ON ar.`shop_id` = s.`shop_id`
    SET ar.`role_name` = '自提点超级管理员', ar.`admin_type` = 'pickup'
WHERE ar.`is_system` = 1
  AND s.`shop_type` = 3
  AND (ar.`role_name` = '超级管理员' OR ar.`admin_type` IN ('shop', 'store'));

-- 供应商
UPDATE `admin_role`
SET `role_name` = '供应商超级管理员'
WHERE `is_system` = 1
  AND `admin_type` = 'vendor'
  AND `role_name` IN ('超级管理员', '供应商超级管理员');

-- 收银台 POS
UPDATE `admin_role`
SET `role_name` = 'POS收银台超级管理员'
WHERE `is_system` = 1
  AND `admin_type` = 'pos'
  AND `role_name` IN ('超级管理员', 'POS收银台超级管理员');
