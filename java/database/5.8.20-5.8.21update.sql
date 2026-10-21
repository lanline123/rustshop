CREATE TABLE `admin_api_token` (
                                   `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
                                   `admin_id` int NOT NULL COMMENT '关联管理员ID',
                                   `token_hash` varchar(64) NOT NULL COMMENT 'Token的SHA-256哈希',
                                   `token_prefix` varchar(8) NOT NULL COMMENT 'Token前8位，用于展示标识',
                                   `name` varchar(100) NOT NULL DEFAULT '' COMMENT 'Token名称/备注',
                                   `created_at` bigint NOT NULL COMMENT '创建时间戳',
                                   `last_used_at` bigint NULL COMMENT '最后使用时间戳',
                                   `expires_at` bigint NULL COMMENT '过期时间戳，NULL表示永不过期',
                                   `is_active` tinyint NOT NULL DEFAULT 1 COMMENT '是否有效：1 有效 0 已吊销',
                                   PRIMARY KEY (`id`),
                                   UNIQUE INDEX `uk_token_hash` (`token_hash`),
                                   INDEX `idx_admin_id` (`admin_id`)
) ENGINE=InnoDB COMMENT='管理员API Token表';

-- 预约商品多规格
ALTER TABLE product_attributes ADD COLUMN sort_order INT DEFAULT 0 COMMENT '排序';

ALTER TABLE `product_reservation_stock`
    ADD COLUMN `sku_id` INT NOT NULL DEFAULT 0 COMMENT 'SKU ID' AFTER `shop_id`;

ALTER TABLE `product_reservation_stock`
DROP INDEX `uniq_stock`,
    ADD UNIQUE KEY `uniq_stock` (`shop_id`, `product_id`, `sku_id`, `stock_date`, `product_reservation_time_id`),
    ADD KEY `idx_product_sku_date` (`product_id`, `sku_id`, `stock_date`);

ALTER TABLE `order_reservations`
    ADD COLUMN `product_reservation_time_id` INT NULL COMMENT '预约时间段ID' AFTER `order_id`,
    ADD COLUMN `sku_id` INT NOT NULL DEFAULT 0 COMMENT 'SKU ID' AFTER `product_reservation_time_id`;

UPDATE `product_reservation_stock` prs
    INNER JOIN (
    SELECT `product_id`, MIN(`sku_id`) AS `default_sku_id`
    FROM `product_sku`
    GROUP BY `product_id`
    HAVING COUNT(*) = 1
    ) sku ON sku.`product_id` = prs.`product_id`
    SET prs.`sku_id` = sku.`default_sku_id`
WHERE prs.`sku_id` = 0;

UPDATE `order_reservations` oor
    INNER JOIN (
    SELECT oi.`order_id`,
    oi.`product_id`,
    oi.`sku_id`,
    MIN(oi.`item_id`) AS `item_id`
    FROM `order_item` oi
    GROUP BY oi.`order_id`, oi.`product_id`, oi.`sku_id`
    ) item ON item.`order_id` = oor.`order_id`
    INNER JOIN `product_reservation_time` prt
    ON prt.`product_id` = item.`product_id`
    AND prt.`start_time` = oor.`start_time`
    AND prt.`end_time` = oor.`end_time`
    SET oor.`product_reservation_time_id` = prt.`product_reservation_time_id`,
        oor.`sku_id` = CASE
        WHEN item.`sku_id` IS NULL THEN 0
        ELSE item.`sku_id`
END
WHERE oor.`product_reservation_time_id` IS NULL
   OR oor.`sku_id` = 0;

-- 以上是预约商品多规格


ALTER TABLE `shop`
    ADD COLUMN `ship_info` text COMMENT '发货信息' AFTER `kefu_inlet`,
    ADD COLUMN `receive_info` text COMMENT '收货信息' AFTER `ship_info`;

ALTER TABLE `vendor`
    ADD COLUMN `ship_info` text COMMENT '发货信息JSON' AFTER `kefu_phone`,
    ADD COLUMN `receive_info` text COMMENT '收货信息JSON' AFTER `ship_info`;



INSERT INTO `authority`( `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES ('shopShipReceiveInfoManage', '配送设置', 10000322, 50, 1, '[]', '', '', 0, 'shop');
INSERT INTO `authority`( `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES ('vendorShipReceiveInfoManage', '配送设置', 11000012, 50, 1, '[]', '', '', 0, 'vendor');
INSERT INTO `authority`( `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES ('shopShipReceiveInfoManage', '配送设置', 20000028, 50, 1, '[]', '', '', 0, 'store');


INSERT INTO `config`( `biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ( 'afterSalesReview', '1', 1773911320, 1, 'admin', 1773911320, 1, 'admin', 0);
INSERT INTO `config`( `biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ( 'financialReview', '1', 1773911320, 1, 'admin', 1773911320, 1, 'admin', 0);
INSERT INTO `config`( `biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ( 'autoConfirmReceiptDays', '7', 1773911320, 1, 'admin', 1773911320, 1, 'admin', 0);

-- 微信直播间字段持久化升级脚本（简版）
-- 说明：直接执行，适合全新环境或字段尚未添加的环境

ALTER TABLE `wechat_live`
    ADD COLUMN `live_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '直播类型：0-手机直播，1-推流' AFTER `share_img`,
    ADD COLUMN `close_like` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否关闭点赞：0-开启，1-关闭' AFTER `live_type`,
    ADD COLUMN `close_goods` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否关闭货架：0-开启，1-关闭' AFTER `close_like`,
    ADD COLUMN `close_comment` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否关闭评论：0-开启，1-关闭' AFTER `close_goods`,
    ADD COLUMN `close_kf` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否关闭客服：0-开启，1-关闭' AFTER `close_comment`,
    ADD COLUMN `close_replay` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否关闭回放：0-开启，1-关闭' AFTER `close_kf`,
    ADD COLUMN `is_feeds_public` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否开启官方收录：0-关闭，1-开启' AFTER `close_replay`,
    ADD COLUMN `feeds_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '官方收录封面' AFTER `is_feeds_public`,
    ADD COLUMN `anchor_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '主播ID' AFTER `cover_img`,
    ADD COLUMN `anchor_openid` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '主播微信openid' AFTER `anchor_id`,
    ADD COLUMN `creator_openid` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '创建者openid' AFTER `anchor_img`;


-- 微信直播商品主表
-- 创建时间: 2024-12-27
-- 说明: 用于存储已导入的微信直播商品主数据

CREATE TABLE IF NOT EXISTS `wechat_live_goods` (
                                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                   `room_id` int DEFAULT NULL COMMENT '历史字段：直播间ID',
                                                   `wechat_goods_id` int DEFAULT NULL COMMENT '微信商品ID',
                                                   `product_id` bigint DEFAULT NULL COMMENT '系统商品ID',
                                                   `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称',
    `cover_img` varchar(500) DEFAULT NULL COMMENT '商品封面图',
    `price` bigint DEFAULT NULL COMMENT '商品价格（分）',
    `price_type` int DEFAULT NULL COMMENT '价格类型：1-一口价，2-价格区间，3-显示折扣价',
    `url` varchar(500) DEFAULT NULL COMMENT '商品小程序路径',
    `audit_status` int DEFAULT NULL COMMENT '商品状态：0-未审核，1-审核中，2-审核通过，3-审核驳回',
    `sort` int DEFAULT 0 COMMENT '历史字段：直播间商品排序',
    `is_show` int DEFAULT 0 COMMENT '历史字段：是否显示：0-显示，1-隐藏',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `shop_id` int DEFAULT NULL COMMENT '店铺ID',
    PRIMARY KEY (`id`),
    KEY `idx_shop_id` (`shop_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信直播商品主表';

-- 微信直播间商品关联表
CREATE TABLE IF NOT EXISTS `wechat_live_room_goods` (
                                                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                        `room_id` int NOT NULL COMMENT '直播间ID',
                                                        `wechat_live_goods_id` bigint NOT NULL COMMENT '直播商品主表ID',
                                                        `wechat_goods_id` int DEFAULT NULL COMMENT '微信商品ID',
                                                        `sort` int DEFAULT 0 COMMENT '直播间商品排序',
                                                        `is_show` int DEFAULT 0 COMMENT '是否显示：0-显示，1-隐藏',
                                                        `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                        `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                        `shop_id` int DEFAULT NULL COMMENT '店铺ID',
                                                        PRIMARY KEY (`id`),
    UNIQUE KEY `uk_room_goods` (`room_id`,`wechat_live_goods_id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_room_wechat_goods` (`room_id`,`wechat_goods_id`),
    KEY `idx_shop_id` (`shop_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信直播间商品关联表';

-- 微信直播订单归因关系表
CREATE TABLE IF NOT EXISTS `wechat_live_order_rel` (
                                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                       `order_id` int NOT NULL COMMENT '订单ID',
                                                       `order_item_id` int NOT NULL COMMENT '订单项ID',
                                                       `wechat_live_id` int DEFAULT NULL COMMENT '直播主键ID',
                                                       `room_id` int DEFAULT NULL COMMENT '直播房间ID',
                                                       `wechat_goods_id` int DEFAULT NULL COMMENT '微信直播商品ID',
                                                       `product_id` int DEFAULT NULL COMMENT '系统商品ID',
                                                       `share_open_id` varchar(128) DEFAULT NULL COMMENT '分享人OpenId',
    `viewer_open_id` varchar(128) DEFAULT NULL COMMENT '观看人OpenId',
    `scene` varchar(64) DEFAULT NULL COMMENT '直播场景值',
    `custom_params` varchar(500) DEFAULT NULL COMMENT '直播透传参数',
    `sub_source` varchar(32) DEFAULT 'wechatLive' COMMENT '渠道子来源',
    `sync_status` tinyint DEFAULT 0 COMMENT '同步状态：0待同步，1成功，2失败，3跳过，4部分同步',
    `last_sync_time` bigint DEFAULT NULL COMMENT '最近同步时间',
    `shop_id` int DEFAULT NULL COMMENT '店铺ID',
    `add_time` bigint DEFAULT NULL COMMENT '创建时间',
    `update_time` bigint DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_item_id` (`order_item_id`),
    KEY `idx_room_goods` (`room_id`,`wechat_goods_id`),
    KEY `idx_product_id_sub_source` (`product_id`,`sub_source`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信直播订单归因关系表';

-- 类型修正：微信商品ID使用int（与微信直播接口goodsId一致）
ALTER TABLE `wechat_live_goods`
    MODIFY COLUMN `wechat_goods_id` int DEFAULT NULL COMMENT '微信商品ID',
    ADD COLUMN `product_sku_id` bigint NOT NULL DEFAULT 0 COMMENT '系统SKU ID，0表示无规格' AFTER `product_id`,
    ADD KEY `idx_wechat_goods_id` (`wechat_goods_id`),
    ADD KEY `idx_product_sku_shop` (`product_id`,`product_sku_id`,`shop_id`);

ALTER TABLE `wechat_live_order_rel`
    MODIFY COLUMN `wechat_goods_id` int DEFAULT NULL COMMENT '微信直播商品ID';

-- 从旧直播商品表结构迁移直播间关联数据到新关联表
INSERT IGNORE INTO `wechat_live_room_goods`
(`room_id`, `wechat_live_goods_id`, `wechat_goods_id`, `sort`, `is_show`, `create_time`, `update_time`, `shop_id`)
SELECT `room_id`, `id`, `wechat_goods_id`, IFNULL(`sort`, 0), IFNULL(`is_show`, 0), `create_time`, `update_time`, `shop_id`
FROM `wechat_live_goods`
WHERE `room_id` IS NOT NULL;

-- 微信直播同步日志表
CREATE TABLE IF NOT EXISTS `wechat_live_sync_log` (
                                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                      `order_id` int DEFAULT NULL COMMENT '订单ID',
                                                      `aftersale_id` int DEFAULT NULL COMMENT '售后ID',
                                                      `refund_id` int DEFAULT NULL COMMENT '退款ID',
                                                      `room_id` int DEFAULT NULL COMMENT '直播房间ID',
                                                      `event_type` varchar(64) NOT NULL COMMENT '事件类型',
    `direction` varchar(16) NOT NULL COMMENT '同步方向：OUTBOUND/INBOUND',
    `sync_status` tinyint DEFAULT 0 COMMENT '同步状态：0待同步，1成功，2失败，3跳过，4部分同步',
    `idempotent_key` varchar(128) DEFAULT NULL COMMENT '幂等键',
    `request_data` longtext COMMENT '请求数据',
    `response_data` longtext COMMENT '响应数据',
    `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
    `retry_count` int DEFAULT 0 COMMENT '重试次数',
    `last_sync_time` bigint DEFAULT NULL COMMENT '最近同步时间',
    `next_retry_time` bigint DEFAULT NULL COMMENT '下次重试时间',
    `shop_id` int DEFAULT NULL COMMENT '店铺ID',
    `add_time` bigint DEFAULT NULL COMMENT '创建时间',
    `update_time` bigint DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_event` (`order_id`,`event_type`),
    KEY `idx_status_direction` (`sync_status`,`direction`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_idempotent_key` (`idempotent_key`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信直播同步日志表';

-- 视频号账号关联表
CREATE TABLE IF NOT EXISTS `wechat_video_account` (
                                                      `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                      `finder_id` varchar(64) NOT NULL DEFAULT '' COMMENT '视频号ID',
    `add_time` bigint NOT NULL DEFAULT 0 COMMENT '关联时间（创建时间）',
    `update_time` bigint NOT NULL DEFAULT 0 COMMENT '更新时间',
    `shop_id` int NOT NULL DEFAULT 0 COMMENT '店铺ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_shop_finder_id` (`shop_id`,`finder_id`),
    KEY `idx_add_time` (`add_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频号账号关联表';


ALTER TABLE wechat_live ADD COLUMN sub_anchor_wechat VARCHAR(255) DEFAULT '' COMMENT '副主播微信账号';

ALTER TABLE wechat_live ADD COLUMN audit_status int NOT NULL DEFAULT 0 COMMENT '审核状态：0-未审核，1-审核中，2-审核通过，3-审核驳回';

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (50002050, 'live', '直播', 0, 50, 1, '[]', '', 'iconfont-tig icon-xiaochengxuzhibo', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (50001028, 'wechatLiveManagement', '小程序直播', 50002050, 50, 1, '[]', '', 'iconfont-tig icon-xiaochengxuzhibo', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (50001029, 'wechatLive', '直播间', 50001028, 50, 1, '[{"auth_name":"编辑","auth_sn":"wechatLiveModifyManage"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (50001030, 'wechatLiveProduct', '直播商品', 50001028, 50, 1, '[{"auth_name":"编辑","auth_sn":"wechatLiveProductModifyManage"}]', '', '', 0, 'admin');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (50001031, 'videoNumberManagement', '视频号', 50002050, 50, 1, '[]', '', 'iconfont-tig icon-shipinhaozhibo', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (50001032, 'videoNumber', '视频号列表', 50001031, 50, 1, '[{"auth_name":"编辑","auth_sn":"videoNumberModifyManage"}]', '', '', 0, 'admin');

-- wechat_live 表添加 create_time 列
ALTER TABLE `wechat_live` ADD COLUMN `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

-- O2O 开启代客下单权限（幂等）
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (30001031, 'proxyShoppingManage', '代客下单', 10000414, 56, 1, '[]', '', '', 0, 'admin')
    ON DUPLICATE KEY UPDATE
                         `authority_sn` = VALUES(`authority_sn`),
                         `authority_name` = VALUES(`authority_name`),
                         `parent_id` = VALUES(`parent_id`),
                         `sort_order` = VALUES(`sort_order`),
                         `is_show` = VALUES(`is_show`),
                         `child_auth` = VALUES(`child_auth`),
                         `route_link` = VALUES(`route_link`),
                         `authority_ico` = VALUES(`authority_ico`),
                         `is_system` = VALUES(`is_system`),
                         `admin_type` = VALUES(`admin_type`);

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (30001032, 'proxyShoppingCheck', '代客下单结算', 10000414, 55, 0, '[]', '', '', 0, 'admin')
    ON DUPLICATE KEY UPDATE
                         `authority_sn` = VALUES(`authority_sn`),
                         `authority_name` = VALUES(`authority_name`),
                         `parent_id` = VALUES(`parent_id`),
                         `sort_order` = VALUES(`sort_order`),
                         `is_show` = VALUES(`is_show`),
                         `child_auth` = VALUES(`child_auth`),
                         `route_link` = VALUES(`route_link`),
                         `authority_ico` = VALUES(`authority_ico`),
                         `is_system` = VALUES(`is_system`),
                         `admin_type` = VALUES(`admin_type`);

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001018, 'proxyShoppingManage', '代客下单', 10000444, 50, 1, '[]', '', '', 0, 'shop')
    ON DUPLICATE KEY UPDATE
                         `authority_sn` = VALUES(`authority_sn`),
                         `authority_name` = VALUES(`authority_name`),
                         `parent_id` = VALUES(`parent_id`),
                         `sort_order` = VALUES(`sort_order`),
                         `is_show` = VALUES(`is_show`),
                         `child_auth` = VALUES(`child_auth`),
                         `route_link` = VALUES(`route_link`),
                         `authority_ico` = VALUES(`authority_ico`),
                         `is_system` = VALUES(`is_system`),
                         `admin_type` = VALUES(`admin_type`);

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001019, 'proxyShoppingCheck', '代客下单结算', 10000444, 50, 0, '[]', '', '', 0, 'shop')
    ON DUPLICATE KEY UPDATE
                         `authority_sn` = VALUES(`authority_sn`),
                         `authority_name` = VALUES(`authority_name`),
                         `parent_id` = VALUES(`parent_id`),
                         `sort_order` = VALUES(`sort_order`),
                         `is_show` = VALUES(`is_show`),
                         `child_auth` = VALUES(`child_auth`),
                         `route_link` = VALUES(`route_link`),
                         `authority_ico` = VALUES(`authority_ico`),
                         `is_system` = VALUES(`is_system`),
                         `admin_type` = VALUES(`admin_type`);

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001027, 'proxyShoppingManage', '代客下单', 20000037, 50, 1, '[]', '', '', 0, 'store')
    ON DUPLICATE KEY UPDATE
                         `authority_sn` = VALUES(`authority_sn`),
                         `authority_name` = VALUES(`authority_name`),
                         `parent_id` = VALUES(`parent_id`),
                         `sort_order` = VALUES(`sort_order`),
                         `is_show` = VALUES(`is_show`),
                         `child_auth` = VALUES(`child_auth`),
                         `route_link` = VALUES(`route_link`),
                         `authority_ico` = VALUES(`authority_ico`),
                         `is_system` = VALUES(`is_system`),
                         `admin_type` = VALUES(`admin_type`);

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001028, 'proxyShoppingCheck', '代客下单结算', 20000037, 50, 0, '[]', '', '', 0, 'store')
    ON DUPLICATE KEY UPDATE
                         `authority_sn` = VALUES(`authority_sn`),
                         `authority_name` = VALUES(`authority_name`),
                         `parent_id` = VALUES(`parent_id`),
                         `sort_order` = VALUES(`sort_order`),
                         `is_show` = VALUES(`is_show`),
                         `child_auth` = VALUES(`child_auth`),
                         `route_link` = VALUES(`route_link`),
                         `authority_ico` = VALUES(`authority_ico`),
                         `is_system` = VALUES(`is_system`),
                         `admin_type` = VALUES(`admin_type`);


CREATE TABLE `pickup_contact` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `user_id` int NOT NULL COMMENT '客户ID',
                                  `contact_name` varchar(64) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '联系人名称',
                                  `contact_phone` varchar(32) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '联系人电话',
                                  `create_time` bigint DEFAULT NULL COMMENT '创建时间',
                                  `update_time` bigint DEFAULT NULL COMMENT '更新时间',
                                  `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '删除标志；0-未删除，1-已删除',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='自提联系人表';


-- 优惠券发放批次表
CREATE TABLE `coupon_issue_batch` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '批次ID',
                                      `coupon_id` int NOT NULL COMMENT '优惠券活动ID',
                                      `user_count` int NOT NULL DEFAULT 0 COMMENT '发放用户数',
                                      `coupon_count` int NOT NULL DEFAULT 0 COMMENT '发放张数',
                                      `issue_time` bigint NOT NULL COMMENT '发放时间（时间戳）',
                                      `source_type` int NOT NULL DEFAULT 0 COMMENT '发放来源：0-用户领取，1-抽奖中奖，2-手动发放',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_coupon_id` (`coupon_id`),
                                      KEY `idx_issue_time` (`issue_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='优惠券发放批次表';

-- 优惠券发放日志表
CREATE TABLE `coupon_issue_log` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                    `batch_id` bigint NOT NULL COMMENT '发放批次ID',
                                    `coupon_id` int NOT NULL COMMENT '优惠券活动ID',
                                    `user_coupon_id` int NOT NULL COMMENT '用户优惠券ID',
                                    `user_id` int NOT NULL COMMENT '用户ID',
                                    `issue_time` bigint NOT NULL COMMENT '发放时间（时间戳）',
                                    `source_type` int NOT NULL DEFAULT 0 COMMENT '发放来源：0-用户领取，1-抽奖中奖，2-手动发放',
                                    PRIMARY KEY (`id`),
                                    KEY `idx_batch_id` (`batch_id`),
                                    KEY `idx_coupon_id` (`coupon_id`),
                                    KEY `idx_user_id` (`user_id`),
                                    KEY `idx_user_coupon_id` (`user_coupon_id`),
                                    KEY `idx_issue_time` (`issue_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='优惠券发放日志表';



INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (50002030, 'wechatLiveMiniAppSetting', '小程序设置', 39, 50, 1, '[]', '', '', 0, 'admin');