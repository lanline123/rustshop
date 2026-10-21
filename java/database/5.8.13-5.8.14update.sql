INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('storageS3AccessKey', 'test', 1768962847, 1, 'admin', 1768962847, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('storageS3SecretKey', 'test', 1768962847, 1, 'admin', 1768962847, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('storageS3Url', 'https://your.s3.ap-southeast-1.amazonaws.com/', 1768964661, 1, 'admin', 1768964661, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('storageS3Bucket', 'test-bucket', 1768964661, 1, 'admin', 1768964661, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('storageS3Region', 'us-east-1', 1768964661, 1, 'admin', 1768964661, 1, 'admin', 0);

UPDATE `authority` SET `authority_sn` = 'userLevelProManage', `authority_name` = '会员等级设置', `parent_id` = 10000422, `sort_order` = 50, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"编辑\",\"auth_sn\":\"userRankModifyManage\"}]', `route_link` = 'user/level_manage_pro/list/', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 10000391;


ALTER TABLE `order`
    MODIFY COLUMN `logistics_id` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '快递公司id/为0则表示商家配送或无需配送' AFTER `shipping_method`;



ALTER TABLE `gallery_pic`
    ADD COLUMN `hidden` tinyint NULL COMMENT '0 或者 null 不隐藏 1  表示隐藏' AFTER `vendor_id`;


INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001017, 'taxRateManage', '税率管理', 39, 50, 1, '[{\"authName\":\"编辑\",\"authSn\":\"taxRateModifyManage\"}]', '', '', 0, 'admin');

-- 税率管理相关数据库变更
-- 执行时间：2026-01-13
-- 功能：税率管理模块

-- 1. 创建税率表
CREATE TABLE IF NOT EXISTS `tax_rate` (
                                          `tax_rate_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '税率ID',
    `region_id` int(11) NOT NULL COMMENT '国家和地区ID',
    `region_name` varchar(100) DEFAULT NULL COMMENT '国家和地区名称',
    `rate` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '税率(百分比)',
    `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否生效:0-否,1-是',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间(秒级时间戳)',
    `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间(秒级时间戳)',
    PRIMARY KEY (`tax_rate_id`),
    KEY `idx_region_id` (`region_id`),
    KEY `idx_is_active` (`is_active`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='税率管理表';

-- 2. Order订单表增加税率相关字段
ALTER TABLE `order`
    ADD COLUMN `tax_rate` decimal(10,4) DEFAULT NULL COMMENT '税率(百分比)' AFTER `discount_change_price`,
ADD COLUMN `tax_rate_amount` decimal(10,2) DEFAULT NULL COMMENT '税率金额' AFTER `tax_rate`;

-- 3. user_coupon表增加来源类型字段（用于区分抽奖中奖的优惠券）
ALTER TABLE `user_coupon`
    ADD COLUMN `source_type` tinyint(1) DEFAULT '0' COMMENT '来源类型:0-普通领取,1-抽奖中奖' AFTER `end_date`;


INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('indexSearchShop', '0', 1768550389, 1, 'admin', 1768550389, 1, 'admin', 0);
