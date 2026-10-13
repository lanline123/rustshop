-- =============================================================
-- v5.8.37 -> v5.8.38 增量升级脚本
-- 内容：
--   1) 店铺 ID 随机 INT 方案一期 DDL（加宽窄 shop_id 列 + shop 主键去自增）
--   2) 收银台订单设置 config 初始化 + 下线「PC装修-其它页面」权限点
--   3) PC端装修 - 搜索页装修 后台菜单权限
--   4) PC端装修 - 登录页装修 后台菜单权限
--   5) 地区名称长度上限放宽到 200 字符
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- 幂等：DDL 用 MODIFY COLUMN 可重复执行；DML 用 NOT EXISTS / 按键删除保护，可重复执行。
-- =============================================================

-- -------------------------------------------------------------
-- 1) 店铺 ID 改「随机不连续 INT」方案 - 一期 DDL
--    背景：新建店铺改用 9-10 位随机未占用整数作为 shop_id（隐藏 /shop/63 这类可枚举小 id）。
--    幂等：MODIFY COLUMN 可重复执行（已是目标类型时为无操作）；非破坏性，不改动存量数据。
-- -------------------------------------------------------------
ALTER TABLE `order`                 MODIFY COLUMN `shop_id` int unsigned NOT NULL DEFAULT 0 COMMENT '店铺id';
ALTER TABLE `order_item`            MODIFY COLUMN `shop_id` int unsigned NOT NULL DEFAULT 0 COMMENT '店铺ID';
ALTER TABLE `order_log`             MODIFY COLUMN `shop_id` int unsigned NOT NULL DEFAULT 0 COMMENT '店铺id';
ALTER TABLE `comment`               MODIFY COLUMN `shop_id` int unsigned NOT NULL DEFAULT 0 COMMENT '所属店铺';
ALTER TABLE `decorate`              MODIFY COLUMN `shop_id` int unsigned NOT NULL DEFAULT 0 COMMENT '所属店铺';
ALTER TABLE `refund_apply`          MODIFY COLUMN `shop_id` int unsigned NOT NULL DEFAULT 0 COMMENT '店铺id';
ALTER TABLE `shipping_tpl`          MODIFY COLUMN `shop_id` int unsigned NOT NULL DEFAULT 0 COMMENT '店铺id';
ALTER TABLE `shipping_type`         MODIFY COLUMN `shop_id` int unsigned NOT NULL DEFAULT 0 COMMENT '店铺id';
ALTER TABLE `shop_withdraw`         MODIFY COLUMN `shop_id` int unsigned NOT NULL DEFAULT 0 COMMENT '所属店铺';
ALTER TABLE `feedback`              MODIFY COLUMN `shop_id` int unsigned NULL     DEFAULT 0 COMMENT '店铺id';
ALTER TABLE `gallery`               MODIFY COLUMN `shop_id` int unsigned NOT NULL DEFAULT 0;
ALTER TABLE `gallery_pic`           MODIFY COLUMN `shop_id` int unsigned NOT NULL DEFAULT 0 COMMENT '店铺id';
ALTER TABLE `gallery_video`         MODIFY COLUMN `shop_id` int unsigned NOT NULL DEFAULT 0 COMMENT '商户id';
ALTER TABLE `gallery_video_info`    MODIFY COLUMN `shop_id` int unsigned NOT NULL DEFAULT 0 COMMENT '商户id';
ALTER TABLE `shop_product_category` MODIFY COLUMN `shop_id` int unsigned NOT NULL DEFAULT 0 COMMENT '所属店铺';
ALTER TABLE `product_package`       MODIFY COLUMN `store_id` int unsigned NOT NULL DEFAULT 0;
ALTER TABLE `shop`                  MODIFY COLUMN `shop_id` int NOT NULL COMMENT '店铺id(随机分配)';

-- -------------------------------------------------------------
-- 2) 收银台订单设置 config 初始化（saveCashierOrderSettings 涉及的 biz_code）
--    默认值取自 com.tigshop.bean.enums.setting.SettingsEnum。
--    幂等：config 采用 NOT EXISTS 保护，已存在的不再插入。
-- -------------------------------------------------------------
-- 店内收银自动抹零（默认 0-关闭）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'autoEraseZero', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'autoEraseZero' AND `is_del` = 0);

-- 收银积分基于订单自动抵扣（默认 0-关闭）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'autoDeductionPoints', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'autoDeductionPoints' AND `is_del` = 0);

-- 收银默认收款方式（默认 1-现金支付）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'defaultCollectionMethod', '1', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'defaultCollectionMethod' AND `is_del` = 0);

-- 线上订单自动打印（默认 1-开启）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'autoPrintOnlineOrder', '1', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'autoPrintOnlineOrder' AND `is_del` = 0);

-- 线下订单自动打印（默认 1-开启）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'autoPrintOfflineOrder', '1', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'autoPrintOfflineOrder' AND `is_del` = 0);

-- 店铺常用备注列表（默认空数组）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'shopCommonRemarks', '[]', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'shopCommonRemarks' AND `is_del` = 0);

-- 收银台商品来源（默认 [0]-全部商品）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'posProductSource', '[0]', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'posProductSource' AND `is_del` = 0);

-- 删除权限点：PC装修-其它页面（pcDecorateOtherManage，连带子权限 pcDecorateOtherUpdateManage）
-- 幂等：按 authority_sn + admin_type 删除，重复执行不报错。
DELETE FROM `authority` WHERE `authority_sn` = 'pcDecorateOtherManage' AND `admin_type` = 'admin';

-- -------------------------------------------------------------
-- 3) PC端装修 - 搜索页装修 后台菜单权限（sort_order=56）
--    child_auth 复用「首页装修」装修权限点，不新增权限。
--    幂等：NOT EXISTS 判断，已存在则跳过。
-- -------------------------------------------------------------
INSERT INTO `authority`
(`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'pcSearchDecorateManage', '搜索页装修', 62, 56, 1,
       '[{\"authName\":\"发布\",\"authSn\":\"decoratePublishManage\"},{\"authName\":\"设为首页\",\"authSn\":\"decorateSetHomeManage\"},{\"authName\":\"编辑\",\"authSn\":\"decorateModifyManage\"},{\"authName\":\"模块编辑\",\"authSn\":\"decorateDiscreteModifyManage\"},{\"authName\":\"存入草稿\",\"authSn\":\"decorateSaveDraftManage\"},{\"authName\":\"新增\",\"authSn\":\"decorateCreateManage\"}]',
       '-', '', 0, 'admin'
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `authority`) AS t
    WHERE t.`authority_sn` = 'pcSearchDecorateManage' AND t.`admin_type` = 'admin'
);

-- -------------------------------------------------------------
-- 4) PC端装修 - 登录页装修 后台菜单权限（sort_order=57）
--    child_auth 复用「首页装修」装修权限点，不新增权限。
--    幂等：NOT EXISTS 判断，已存在则跳过。
-- -------------------------------------------------------------
INSERT INTO `authority`
(`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'pcLoginDecorateManage', '登录页装修', 62, 57, 1,
       '[{\"authName\":\"发布\",\"authSn\":\"decoratePublishManage\"},{\"authName\":\"设为首页\",\"authSn\":\"decorateSetHomeManage\"},{\"authName\":\"编辑\",\"authSn\":\"decorateModifyManage\"},{\"authName\":\"模块编辑\",\"authSn\":\"decorateDiscreteModifyManage\"},{\"authName\":\"存入草稿\",\"authSn\":\"decorateSaveDraftManage\"},{\"authName\":\"新增\",\"authSn\":\"decorateCreateManage\"}]',
       '-', '', 0, 'admin'
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `authority`) AS t
    WHERE t.`authority_sn` = 'pcLoginDecorateManage' AND t.`admin_type` = 'admin'
);

-- -------------------------------------------------------------
-- 5) 地区名称长度上限放宽到 200 字符（原 D1037-bugfix.sql）
--    幂等：MODIFY COLUMN 可重复执行；仅扩大字段上限，不修改已有地区数据。
-- -------------------------------------------------------------
ALTER TABLE `region`
    MODIFY COLUMN `region_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci
    NOT NULL DEFAULT '' COMMENT '地区的名字';
