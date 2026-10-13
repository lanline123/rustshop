INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES
    ('storageQiniuUrl', 'http://t6rwjrwr2.hd-bkt.clouddn.com/', 1745391561, 0, '', 1745391561, 0, '', 0);

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES
    ('storageQiniuAccessKey', 'qinniuaccesskey', 1745391561, 0, '', 1745391561, 0, '', 0);

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES
    ('storageQiniuSecretKey', 'qiniusecret', 1745391561, 0, '', 1745391561, 0, '', 0);

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES
    ('storageQiniuBucket', 'tigshop', 1745391561, 0, '', 1745391561, 0, '', 0);

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES
    ('storageQiniuRegion', '华东', 1745391561, 0, '', 1745391561, 0, '', 0);


-- 关闭云支付
update config set biz_val = 0 where biz_code = 'useYunpay' and is_del = 0 ;


ALTER TABLE `product` ADD COLUMN `sale_mode` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '销售模式：0-现货销售，1-全款预售，2-定金预售' AFTER `product_type`;

CREATE TABLE IF NOT EXISTS `product_presale` (
                                                 `presale_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '预售ID',
                                                 `product_id` mediumint unsigned NOT NULL COMMENT '商品ID',
                                                 `sale_mode` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '销售模式：1-全款预售，2-定金预售',
                                                 `deposit_type` tinyint unsigned DEFAULT NULL COMMENT '定金收取方式：1-按百分比，2-按固定金额（仅定金预售）',
                                                 `deposit_ratio` tinyint unsigned DEFAULT NULL COMMENT '定金比例：1-99（仅按百分比）',
                                                 `is_round_down` tinyint unsigned DEFAULT '0' COMMENT '是否向下取整：0-否，1-是（仅按百分比）',
                                                 `deposit_fixed_amount` decimal(10,2) unsigned DEFAULT NULL COMMENT '固定定金金额（仅按固定金额）',
    `final_payment_type` tinyint unsigned DEFAULT NULL COMMENT '尾款支付时间类型：1-相对时间，2-固定时间（仅用于展示）',
    `final_payment_relative_days` smallint unsigned DEFAULT NULL COMMENT '支付定金后N天开始支付尾款（相对时间）',
    `final_payment_duration_days` smallint unsigned DEFAULT NULL COMMENT '尾款支付期限-天（相对时间）',
    `final_payment_duration_hours` tinyint unsigned DEFAULT NULL COMMENT '尾款支付期限-小时（相对时间）',
    `final_payment_start_time` int unsigned DEFAULT NULL COMMENT '尾款支付开始时间（固定时间）',
    `final_payment_end_time` int unsigned DEFAULT NULL COMMENT '尾款支付结束时间（固定时间）',
    `delivery_type` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '发货时间类型：1-相对时间，2-指定日期',
    `delivery_relative_days` smallint unsigned DEFAULT NULL COMMENT '支付全款后N天发货（相对时间）',
    `delivery_relative_hours` tinyint unsigned DEFAULT NULL COMMENT '支付全款后N小时发货（相对时间）',
    `delivery_date` int unsigned DEFAULT NULL COMMENT '发货日期（指定日期）',
    `status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time` int unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
    `update_time` int unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
    PRIMARY KEY (`presale_id`),
    UNIQUE KEY `uk_product_id` (`product_id`),
    KEY `idx_sale_mode` (`sale_mode`),
    KEY `idx_status` (`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品预售信息表';


CREATE TABLE `order_presale` (
                                 `presale_order_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '预售订单ID',
                                 `order_id` int unsigned NOT NULL COMMENT '订单ID',
                                 `order_sn` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号',
                                 `product_id` mediumint unsigned NOT NULL COMMENT '商品ID',
                                 `sale_mode` tinyint unsigned NOT NULL COMMENT '销售模式：1-全款预售，2-定金预售',
                                 `deposit_amount` decimal(10,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '定金金额（定金预售）',
                                 `final_payment_amount` decimal(10,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '尾款金额（定金预售）',
                                 `deposit_paid_time` int unsigned DEFAULT NULL COMMENT '定金支付时间',
                                 `final_payment_start_time` int unsigned DEFAULT NULL COMMENT '尾款开始支付时间（实际计算值）',
                                 `final_payment_paid_time` int unsigned DEFAULT NULL COMMENT '尾款支付时间',
                                 `presale_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '预售状态：0-待支付定金，1-待支付尾款，2-已完成支付',
                                 `delivery_days` smallint unsigned DEFAULT NULL COMMENT '发货天数（相对时间）',
                                 `delivery_date` int unsigned DEFAULT NULL COMMENT '发货日期（指定日期）',
                                 `create_time` int unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
                                 `update_time` int unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
                                 PRIMARY KEY (`presale_order_id`),
                                 UNIQUE KEY `uk_order_id` (`order_id`),
                                 KEY `idx_order_sn` (`order_sn`),
                                 KEY `idx_presale_status` (`presale_status`),
                                 KEY `idx_final_payment_start_time` (`final_payment_start_time`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单预售信息表';


ALTER TABLE `order`
    ADD COLUMN `is_presale` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否预售订单：0-否，1-是' AFTER `order_type`,
ADD COLUMN `presale_type` tinyint unsigned DEFAULT NULL COMMENT '预售类型：1-全款预售，2-定金预售' AFTER `is_presale`;


CREATE TABLE IF NOT EXISTS `refund_apply_paylog_refund` (
                                                            `refund_apply_paylog_refund_id` int NOT NULL AUTO_INCREMENT COMMENT '关联ID',
                                                            `refund_apply_id` int NOT NULL COMMENT '退款申请ID',
                                                            `paylog_refund_id` int NOT NULL COMMENT '退款流水ID',
                                                            PRIMARY KEY (`refund_apply_paylog_refund_id`),
    KEY `idx_refund_apply_id` (`refund_apply_id`),
    KEY `idx_paylog_refund_id` (`paylog_refund_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='退款申请与退款流水关联表';

ALTER TABLE `decorate`
    ADD COLUMN `sort_order` int NULL DEFAULT 0 COMMENT '排序';

ALTER TABLE `decorate`
    ADD COLUMN `binded_h5_id` int NULL DEFAULT 0 COMMENT '绑定的h5页面id（decorate_type = 3 生效）';


INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (null, 'pcDecorateCustomManage', '自定义页装修', 62, 53, 1, '[{\"authName\":\"编辑\",\"authSn\":\"decorateModifyManage\"}]', '', '', 0, 'admin');