
DROP TABLE IF EXISTS `lottery_activity`;
CREATE TABLE `lottery_activity` (
                                    `activity_id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '活动ID',
                                    `activity_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '活动名称',
                                    `start_time` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '活动开始时间(秒级时间戳)',
                                    `end_time` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '活动结束时间(秒级时间戳)',
                                    `cost_points` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '每次抽奖消耗积分',
                                    `daily_limit` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '每日最大抽奖次数(0表示不限制)',
                                    `total_limit` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '最大抽奖次数(0表示不限制)',
                                    `win_limit` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '每人最多中奖次数(0表示不限制)',
                                    `decorate_config` json NULL COMMENT '装修配置(JSON格式):bgImage,bgColor,showWinnerList,activityRules',
                                    `grid_config` json NULL COMMENT '九宫格配置(JSON数组):position,prizeId,probability',
                                    `no_win_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '谢谢惠顾' COMMENT '未中奖名称(未选择奖品的位置显示)',
                                    `no_win_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '未中奖图片URL(未选择奖品的位置显示)',
                                    `no_win_tips` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '很遗憾，再接再厉！' COMMENT '未中奖提示语(弹窗显示)',
                                    `shop_id` int(11) unsigned NOT NULL DEFAULT 0 COMMENT '店铺ID(平台为0)',
                                    `is_delete` tinyint(1) unsigned NOT NULL DEFAULT 0 COMMENT '是否删除:0-否,1-是(软删除)',
                                    `create_time` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '创建时间(秒级时间戳)',
                                    `update_time` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '更新时间(秒级时间戳)',
                                    PRIMARY KEY (`activity_id`) USING BTREE,
                                    INDEX `idx_time` (`start_time`,`end_time`) USING BTREE,
                                    INDEX `idx_shop_id` (`shop_id`) USING BTREE,
                                    INDEX `idx_is_delete` (`is_delete`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='抽奖活动表' ROW_FORMAT=Dynamic;


DROP TABLE IF EXISTS `lottery_prize`;
CREATE TABLE `lottery_prize` (
                                 `prize_id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '奖品ID',
                                 `activity_id` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '活动ID',
                                 `prize_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '奖品名称',
                                 `prize_type` tinyint(2) unsigned NOT NULL DEFAULT 1 COMMENT '奖品类型:1-积分,2-商品,3-优惠券',
                                 `prize_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '奖品值(积分数量/商品ID/优惠券ID)',
                                 `prize_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '奖品图片URL',
                                 `prize_stock` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '奖品库存',
                                 `prize_stock_used` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '已使用库存',
                                 `create_time` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '创建时间(秒级时间戳)',
                                 `update_time` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '更新时间(秒级时间戳)',
                                 PRIMARY KEY (`prize_id`) USING BTREE,
                                 INDEX `idx_activity_id` (`activity_id`) USING BTREE,
                                 INDEX `idx_prize_type` (`prize_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='活动奖品表' ROW_FORMAT=Dynamic;


DROP TABLE IF EXISTS `lottery_record`;
CREATE TABLE `lottery_record` (
                                  `record_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '记录ID',
                                  `activity_id` int unsigned NOT NULL DEFAULT '0' COMMENT '活动ID',
                                  `user_id` int unsigned NOT NULL DEFAULT '0' COMMENT '用户ID',
                                  `prize_id` int unsigned NOT NULL DEFAULT '0' COMMENT '中奖奖品ID(0表示未中奖)',
                                  `prize_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '奖品名称',
                                  `prize_type` tinyint unsigned NOT NULL DEFAULT '4' COMMENT '奖品类型:1-积分,2-商品,3-优惠券,4-谢谢惠顾',
                                  `prize_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '奖品值',
                                  `prize_image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '奖品图片',
                                  `cost_points` int unsigned NOT NULL DEFAULT '0' COMMENT '消耗积分',
                                  `is_win` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否中奖:0-未中奖,1-中奖',
                                  `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '奖品状态:0-未领取,1-已领取,2-已过期',
                                  `receive_time` int unsigned DEFAULT NULL COMMENT '领取时间(秒级时间戳)',
                                  `create_time` int unsigned NOT NULL DEFAULT '0' COMMENT '抽奖时间(秒级时间戳)',
                                  PRIMARY KEY (`record_id`) USING BTREE,
                                  KEY `idx_activity_user` (`activity_id`,`user_id`) USING BTREE,
                                  KEY `idx_user_time` (`user_id`,`create_time`) USING BTREE,
                                  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='抽奖记录表';


DROP TABLE IF EXISTS `lottery_user_stat`;
CREATE TABLE `lottery_user_stat` (
                                     `stat_id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '统计ID',
                                     `activity_id` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '活动ID',
                                     `user_id` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '用户ID',
                                     `total_count` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '累计抽奖次数',
                                     `win_count` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '累计中奖次数',
                                     `today_count` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '今日抽奖次数',
                                     `today_date` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '今日日期(YYYY-MM-DD)',
                                     `last_lottery_time` int(10) unsigned NULL DEFAULT NULL COMMENT '最后抽奖时间(秒级时间戳)',
                                     `create_time` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '创建时间(秒级时间戳)',
                                     `update_time` int(10) unsigned NOT NULL DEFAULT 0 COMMENT '更新时间(秒级时间戳)',
                                     PRIMARY KEY (`stat_id`) USING BTREE,
                                     UNIQUE INDEX `uk_activity_user` (`activity_id`,`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户抽奖统计表' ROW_FORMAT=Dynamic;


INSERT INTO `authority`(`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30001025, 'lotteryManage', '抽奖', 10000413, 50, 1, '[{\"authName\":\"编辑\",\"authSn\":\"lotteryActivityModifyManage\"}]', '', '', 0, 'admin');


-- =========================================
-- 配送功能模块 - 数据库变更脚本
-- 模块: 服务人员管理（配送员/服务员）
-- 执行时间: 2024-12-11
-- =========================================

-- =========================================
-- 1. 服务人员表 (service_staff)
-- 说明: 配送员和服务员共用一张表，通过 type 字段区分
-- =========================================

CREATE TABLE IF NOT EXISTS `service_staff` (
                                               `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(50) NOT NULL COMMENT '姓名',
    `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
    `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
    `phone` varchar(20) NOT NULL COMMENT '联系电话',
    `member_id` int(11) DEFAULT NULL COMMENT '关联会员用户ID',
    `store_id` int(11) NOT NULL COMMENT '所属门店ID',
    `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '类型：1-配送员，2-服务员',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '账号状态：0-禁用，1-启用',
    `work_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '工作状态：0-已下班，1-空闲中，2-配送中/服务中',
    `completed_order_count` int(11) NOT NULL DEFAULT '0' COMMENT '已完成订单数',
    `completed_order_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '已完成订单金额',
    `create_time` int(11) NOT NULL DEFAULT '0' COMMENT '创建时间',
    `update_time` int(11) NOT NULL DEFAULT '0' COMMENT '更新时间',
    `is_delete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`id`),
    KEY `idx_store_id` (`store_id`),
    KEY `idx_member_id` (`member_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_work_status` (`work_status`),
    KEY `idx_store_phone` (`store_id`, `phone`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务人员表';


-- =========================================
-- 回滚脚本 (如需回滚请执行以下SQL)
-- =========================================
-- DROP TABLE IF EXISTS `service_staff`;


-- =========================================
-- 同城配送配置表新增字段 - 执行脚本
-- 执行时间: 2024-12-12
-- =========================================

-- 1. 新增派单模式字段（1-抢单模式，2-手动派单）
ALTER TABLE `shop_local_delivery_config`
    ADD COLUMN `order_dispatch_mode` tinyint NOT NULL DEFAULT 1 COMMENT '派单模式：1-抢单模式，2-手动派单' AFTER `distance_pricing_enabled`;

-- 2. 新增取货时拍照设置字段
ALTER TABLE `shop_local_delivery_config`
    ADD COLUMN `pickup_photo_enabled` tinyint NOT NULL DEFAULT 0 COMMENT '是否开启取货时拍照：0-关闭，1-开启' AFTER `order_dispatch_mode`;

-- 3. 新增配送完成拍照设置字段
ALTER TABLE `shop_local_delivery_config`
    ADD COLUMN `delivery_complete_photo_enabled` tinyint NOT NULL DEFAULT 0 COMMENT '是否开启配送完成拍照：0-关闭，1-开启' AFTER `pickup_photo_enabled`;


-- =========================================
-- 门店预约上门服务配置模块 - 数据库变更脚本
-- 模块: 门店预约上门服务配置（参考同城配送配置实现）
-- 执行时间: 2024-12-12
-- =========================================

-- =========================================
-- 1. 门店预约上门服务配置表 (shop_home_service_config)
-- 说明: 用于配置门店预约上门服务的相关设置
-- =========================================

CREATE TABLE IF NOT EXISTS `shop_home_service_config` (
                                                          `shop_home_service_config_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `shop_id` int(11) NOT NULL COMMENT '门店ID',
    `enabled` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否开启预约上门服务：0-关闭，1-开启',
    `service_radius` decimal(10,2) DEFAULT '0.00' COMMENT '服务半径（公里）',
    `home_fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '上门费（元）',
    `distance_pricing_enabled` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否开启距离溢价：0-关闭，1-开启',
    `order_dispatch_mode` tinyint(4) NOT NULL DEFAULT '1' COMMENT '派单模式：1-抢单模式，2-手动派单',
    `before_service_photo_enabled` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否开启服务前拍照：0-关闭，1-开启',
    `after_service_photo_enabled` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否开启服务后拍照：0-关闭，1-开启',
    `add_time` int(11) NOT NULL DEFAULT '0' COMMENT '添加时间',
    PRIMARY KEY (`shop_home_service_config_id`),
    UNIQUE KEY `uk_shop_id` (`shop_id`),
    KEY `idx_enabled` (`enabled`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店预约上门服务配置表';


-- =========================================
-- 2. 门店预约上门服务距离溢价表 (shop_home_service_distance_pricing)
-- 说明: 用于配置预约上门服务的距离溢价规则
-- =========================================

CREATE TABLE IF NOT EXISTS `shop_home_service_distance_pricing` (
                                                                    `shop_home_service_distance_pricing_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `shop_home_service_config_id` int(11) NOT NULL COMMENT '预约上门服务配置ID，关联 shop_home_service_config.shop_home_service_config_id',
    `distance_threshold` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '超过多少公里',
    `distance_unit` decimal(10,2) NOT NULL DEFAULT '1.00' COMMENT '每增加多少公里',
    `additional_fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '上门费增加多少元',
    `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '排序，数值越小越优先',
    `add_time` int(11) NOT NULL DEFAULT '0' COMMENT '添加时间',
    PRIMARY KEY (`shop_home_service_distance_pricing_id`),
    KEY `idx_config_id` (`shop_home_service_config_id`),
    KEY `idx_sort_order` (`sort_order`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店预约上门服务距离溢价表';

-- =========================================
-- 回滚脚本 (如需回滚请执行以下SQL)
-- =========================================
-- DROP TABLE IF EXISTS `shop_home_service_distance_pricing`;
-- DROP TABLE IF EXISTS `shop_home_service_config`;


-- =========================================
-- 派送订单表 (delivery_order)
-- 说明: 关联商城订单与配送员/服务员，记录派送任务的状态流转和执行信息
-- 执行时间: 2024-12-16
-- =========================================

CREATE TABLE IF NOT EXISTS `delivery_order` (
                                                `delivery_order_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '派送订单ID',
    `delivery_order_sn` varchar(50) NOT NULL COMMENT '派送订单编号',
    `order_id` int(11) NOT NULL COMMENT '关联商城订单ID',
    `order_sn` varchar(50) NOT NULL COMMENT '关联商城订单编号',
    `shop_id` int(11) NOT NULL COMMENT '门店ID',
    `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '类型：1-配送订单，2-服务订单',
    `dispatch_mode` tinyint(4) NOT NULL DEFAULT '1' COMMENT '派单模式：1-抢单模式，2-手动派单',
    `status` tinyint(4) NOT NULL DEFAULT '10' COMMENT '状态：10-待派发，20-待取货/待服务，30-配送中/服务中，40-已完成，50-已取消',
    `staff_id` int(11) DEFAULT NULL COMMENT '配送员/服务员ID',
    `staff_name` varchar(50) DEFAULT NULL COMMENT '配送员/服务员姓名',
    `staff_phone` varchar(20) DEFAULT NULL COMMENT '配送员/服务员电话',
    `consignee` varchar(50) NOT NULL COMMENT '收货人/服务对象姓名',
    `mobile` varchar(20) NOT NULL COMMENT '收货人/服务对象电话',
    `address` varchar(255) NOT NULL COMMENT '配送地址/服务地址',
    `region_names` varchar(255) DEFAULT NULL COMMENT '地区名称（省市区）',
    `latitude` decimal(10,6) DEFAULT NULL COMMENT '配送地址/服务地址纬度',
    `longitude` decimal(10,6) DEFAULT NULL COMMENT '配送地址/服务地址经度',
    `pickup_address` varchar(255) DEFAULT NULL COMMENT '取货地址（门店地址）',
    `pickup_phone` varchar(20) DEFAULT NULL COMMENT '取货电话',
    `pickup_latitude` decimal(10,6) DEFAULT NULL COMMENT '取货地址纬度',
    `pickup_longitude` decimal(10,6) DEFAULT NULL COMMENT '取货地址经度',
    `service_time` int(11) DEFAULT NULL COMMENT '预约服务时间（Unix时间戳）',
    `order_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单金额',
    `buyer_note` varchar(500) DEFAULT NULL COMMENT '买家备注',
    `dispatch_time` int(11) DEFAULT NULL COMMENT '派单时间（Unix时间戳）',
    `grab_time` int(11) DEFAULT NULL COMMENT '抢单时间（Unix时间戳）',
    `pickup_time` int(11) DEFAULT NULL COMMENT '取货时间/开始服务时间（Unix时间戳）',
    `complete_time` int(11) DEFAULT NULL COMMENT '完成时间（Unix时间戳）',
    `cancel_time` int(11) DEFAULT NULL COMMENT '取消时间（Unix时间戳）',
    `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
    `cancel_by` tinyint(4) DEFAULT NULL COMMENT '取消方：1-配送员/服务员，2-后台管理员，3-系统',
    `pickup_photo` varchar(1000) DEFAULT NULL COMMENT '取货照片（JSON数组）',
    `complete_photo` varchar(1000) DEFAULT NULL COMMENT '完成照片（JSON数组）',
    `before_service_photo` varchar(1000) DEFAULT NULL COMMENT '服务前照片（JSON数组）',
    `after_service_photo` varchar(1000) DEFAULT NULL COMMENT '服务后照片（JSON数组）',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` int(11) NOT NULL DEFAULT '0' COMMENT '创建时间（Unix时间戳）',
    `update_time` int(11) NOT NULL DEFAULT '0' COMMENT '更新时间（Unix时间戳）',
    `is_delete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除：0-否，1-是',
    PRIMARY KEY (`delivery_order_id`),
    UNIQUE KEY `uk_delivery_order_sn` (`delivery_order_sn`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_shop_id` (`shop_id`),
    KEY `idx_staff_id` (`staff_id`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`),
    KEY `idx_dispatch_mode` (`dispatch_mode`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_shop_status` (`shop_id`, `status`),
    KEY `idx_staff_status` (`staff_id`, `status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='派送订单表';

ALTER TABLE `delivery_order`
    ADD COLUMN `pickup_region_names` varchar(100) NULL COMMENT '取货地址（门店地址） 省市区' AFTER `longitude`;

UPDATE `authority` SET `authority_sn` = 'cashRegisterManage', `authority_name` = '员工管理', `parent_id` = 11001058, `sort_order` = 50, `is_show` = 1, `child_auth` = '[]', `route_link` = '', `authority_ico` = 'iconfont-tig icon-yonghutongji', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 20100011;

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30001020, 'servicemanList', '服务员管理', 20100011, 50, 1, '[{"authName":"编辑","authSn":"serviceStaffModifyManage"}]', '', '', 0, 'admin');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30001019, 'deliverymanList', '配送员管理', 20100011, 0, 1, '[{"authName":"编辑","authSn":"serviceStaffModifyManage"}]', '', '', 0, 'admin');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30001023, 'homeServiceManage', '上门服务', 55, 50, 1, '[{"authName":"编辑","authSn":"storeHomeServiceUpdateManage"}]', '', '', 0, 'admin');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30001024, 'homeServiceManage', '上门服务', 55, 50, 1, '[{"authName":"编辑","authSn":"storeHomeServiceUpdateManage"}]', '', '', 0, 'store');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30001022, 'servicemanList', '服务员管理', 20100009, 50, 1, '[{"authName":"编辑","authSn":"serviceStaffModifyManage"}]', '', '', 0, 'store');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30001021, 'deliverymanList', '配送员管理', 20100009, 50, 1, '[{"authName":"编辑","authSn":"serviceStaffModifyManage"}]', '', '', 0, 'store');


