-- 礼品卡组表
DROP TABLE IF EXISTS `gift_card_groups`;
CREATE TABLE `gift_card_groups`  (
                                     `card_groups_id` int NOT NULL AUTO_INCREMENT COMMENT '卡组id',
                                     `group_type` tinyint NOT NULL COMMENT '1-储值卡 2-兑换卡',
                                     `card_type` tinyint NOT NULL COMMENT '1-电子卡 2-实体卡',
                                     `card_group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '卡组名称',
                                     `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '卡图片URL',
                                     `validity_type` tinyint NOT NULL COMMENT '有效期类型：0=永久，1=天数，2=固定日期',
                                     `validity_days` int NULL DEFAULT NULL COMMENT '有效期天数（当 validity_type=1 时）',
                                     `expire_time` int NULL DEFAULT NULL COMMENT '固定到期时间（当 validity_type=2 时）',
                                     `face_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '储值卡面额(当group_type=1 时)',
                                     `status` tinyint NOT NULL DEFAULT 1 COMMENT '1-开启 0-关闭',
                                     `add_time` int NULL DEFAULT NULL COMMENT '创建时间',
                                     `update_time` int NULL DEFAULT NULL COMMENT '更新时间',
                                     `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
                                     `shop_id` int NULL DEFAULT NULL COMMENT '店铺id',
                                     `availability_stock` int UNSIGNED NULL DEFAULT 0 COMMENT '可用库存数',
                                     PRIMARY KEY (`card_groups_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 66 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '礼品卡表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
-- 礼品卡表
DROP TABLE IF EXISTS `gift_cards`;
CREATE TABLE `gift_cards` (
                              `gift_cards_id` int NOT NULL AUTO_INCREMENT COMMENT '卡id',
                              `card_groups_id` int NOT NULL COMMENT '所属卡组ID',
                              `card_number` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '卡号',
                              `card_password` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '卡密码',
                              `gift_card_sn` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '礼品卡唯一识别码',
                              `is_available` tinyint DEFAULT '1' COMMENT '1-可用 0-不可用',
                              `status` tinyint DEFAULT '0' COMMENT '0-未使用 1-已使用 2-已过期',
                              `used_time` int DEFAULT NULL COMMENT '使用时间',
                              `user_id` int DEFAULT NULL COMMENT '使用者ID',
                              `add_time` int DEFAULT NULL COMMENT '添加时间',
                              `expired_time` int NOT NULL COMMENT '过期时间',
                              `claimed_user_id` int DEFAULT NULL COMMENT '已领取该卡的用户ID',
                              `claimed_status` tinyint DEFAULT '0' COMMENT '是否已领取，0=不可领取，1=可领取，2=已领取',
                              `order_time` int DEFAULT NULL COMMENT '下单时间',
                              PRIMARY KEY (`gift_cards_id`) USING BTREE,
                              UNIQUE KEY `idx_gift_card_num` (`card_number`) USING BTREE,
                              KEY `idx_status` (`status`),
                              KEY `card_sn` (`gift_card_sn`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=872 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='卡信息';

-- 礼品卡可兑换商品表
CREATE TABLE IF NOT EXISTS `gift_card_products` (
                                                    `gift_product_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '卡组商品id',
    `card_groups_id` int(11) NOT NULL COMMENT '卡组ID',
    `product_id` int(11) NOT NULL COMMENT '商品ID',
    `sku_id` int(11) NOT NULL DEFAULT 0 COMMENT 'SKU ID',
    PRIMARY KEY (`gift_product_id`),
    KEY `idx_card_groups_id` (`card_groups_id`),
    KEY `idx_product_id` (`product_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡兑换商品';

-- 订单与礼品卡关联表
CREATE TABLE IF NOT EXISTS `gift_card_order` (
                                                 `order_card_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单卡id',
    `order_id` int(11) NOT NULL COMMENT '订单id',
    `card_id` int(11) NOT NULL COMMENT '卡id',
    PRIMARY KEY (`order_card_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_card_id` (`card_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单卡关联';

-- Product 表新增礼品卡组字段
ALTER TABLE `product`
    ADD COLUMN `gift_card_groups_id` int(11) DEFAULT NULL COMMENT '礼品卡组id';

ALTER TABLE `order`
    ADD COLUMN `gift_card_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '礼品卡兑换金额';

--  权限相关
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (30001001, 'giftCardManage', '礼品卡', 10000413, 50, 1, '[{\"authName\":\"编辑\",\"authSn\":\"giftCardUpdateManage\"}]', '', '', 0, 'admin');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (30001002, 'giftCardManage', '礼品卡', 10000457, 50, 1, '[{\"authName\":\"编辑\",\"authSn\":\"giftCardUpdateManage\"}]', '', '', 0, 'shop');


ALTER TABLE `order_item`
    ADD COLUMN `gift_card_id` int NULL DEFAULT 0 COMMENT '礼品卡兑换订单(礼品卡id)';

ALTER TABLE `product`
    ADD COLUMN `attributes_tpl_id` int(11) DEFAULT NULL COMMENT '属性模板id';

ALTER TABLE `user` ADD COLUMN admin_note VARCHAR(255) NOT NULL DEFAULT '' COMMENT '会员标签';
ALTER TABLE `user` ADD COLUMN  mark TINYINT(1) NOT NULL DEFAULT '0'  COMMENT '标记12345';