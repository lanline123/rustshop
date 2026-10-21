-- ============================================================
-- 5.8.32 -> 5.8.33 增量升级脚本
-- decorate 表补齐 page_type 字段
-- 背景：page_type 字段由 D1081-customized-decoration 功能引入，
--       但历史上未并入任何 update.sql，导致基线与存量库均缺失该列；
--       代码 com.tigshop.bean.model.decorate.Decorate 已使用 pageType 字段。
-- 幂等：通过 information_schema 判断列是否存在，存在则跳过。
-- ============================================================

-- decorate.page_type
SET @decorate_page_type_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'decorate'
      AND COLUMN_NAME = 'page_type'
);
SET @sql := IF(@decorate_page_type_exists = 0,
    'ALTER TABLE `decorate` ADD COLUMN `page_type` TINYINT NOT NULL DEFAULT 1 COMMENT ''页面类型:1首页 2个人中心页 3活动页 4分类页'' AFTER `decorate_type`',
    'SELECT ''decorate.page_type 已存在，跳过'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 历史数据归一化：异常值/空值统一置为 1（首页）
UPDATE `decorate` SET `page_type` = 1 WHERE `page_type` = 0 OR `page_type` IS NULL;

-- =============================================================
-- D1093 商品页风格（PC端装修 - 商品详情页新旧 UI 切换）
-- 1. 后台菜单：PC端装修 > 商品页风格（放在「自定义页装修」之后）
-- 2. 系统配置：商品详情页风格 productDetailStyleType（1-样式一/经典版，2-样式二/新版），默认 1
-- =============================================================

-- ---------------------------------------------
-- 1. 后台菜单权限：商品页风格
--    parent_id = 62（PC端装修），sort_order = 54（位于「自定义页装修」sort 53 之后）
--    child_auth 保存权限：pcProductStyleSaveManage
-- ---------------------------------------------
INSERT INTO `authority`
(`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'pcProductStyleManage', '商品页风格', 62, 54, 1,
       '[{\"authName\":\"保存\",\"authSn\":\"pcProductStyleSaveManage\"}]',
       'decorate/pc_decorate/product_style/', '', 0, 'admin'
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `authority`) AS t
    WHERE t.`authority_sn` = 'pcProductStyleManage' AND t.`admin_type` = 'admin'
);

-- ---------------------------------------------
-- 2. 系统配置：商品详情页风格，默认样式一
-- ---------------------------------------------
INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'productDetailStyleType', '1', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `config`) AS t
    WHERE t.`biz_code` = 'productDetailStyleType'
);
