ALTER TABLE product
    ADD COLUMN seo_identification VARCHAR(512) COMMENT 'SEO标识' AFTER product_name;

ALTER TABLE category
    ADD COLUMN seo_identification VARCHAR(512) COMMENT 'SEO标识' AFTER category_name;

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('seoProductEnable', '0', 1758610402, 1, 'admin', 1758610402, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('seoCategoryEnable', '0', 1758610402, 1, 'admin', 1758610402, 1, 'admin', 0);


CREATE TABLE record_rate_new LIKE record_rate;
INSERT INTO record_rate_new (
    record_id, record_type, shop_service_fee, shop_withdrawal_fee,
    storefront_service_fee, storefront_withdrawal_fee,
    supplier_service_fee, supplier_withdrawal_fee,
    gmt_create, shop_id, vendor_id
)
SELECT
    record_id, record_type, shop_service_fee, shop_withdrawal_fee,
    storefront_service_fee, storefront_withdrawal_fee,
    supplier_service_fee, supplier_withdrawal_fee,
    gmt_create, shop_id, vendor_id
FROM record_rate;
RENAME TABLE record_rate TO record_rate_old, record_rate_new TO record_rate;


CREATE TABLE statement_output_new LIKE statement_output;
ALTER TABLE statement_output_new DROP PRIMARY KEY;
ALTER TABLE statement_output_new
    MODIFY statement_output_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY;
INSERT INTO statement_output_new (
    shop_id, vendor_id, income, expenditure, gmt_create,
    record_sn, record_type, record_id,
    settlement_status, real_settlement, real_settlement_time
)
SELECT
    shop_id, vendor_id, income, expenditure, gmt_create,
    record_sn, record_type, record_id,
    settlement_status, real_settlement, real_settlement_time
FROM statement_output;
RENAME TABLE statement_output TO statement_output_old;
RENAME TABLE statement_output_new TO statement_output;

alter table cart
    add extra_data json null comment '扩展数据';


CREATE TABLE `groupon_promotion` (
                                     `groupon_promotion_id` bigint NOT NULL AUTO_INCREMENT COMMENT '拼团 ID',
                                     `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '活动名称',
                                     `start_time` bigint NOT NULL COMMENT '开始时间',
                                     `end_time` bigint NOT NULL COMMENT '结束时间',
                                     `group_type` tinyint NOT NULL COMMENT '拼团类型；1-普通团，2-阶梯团',
                                     `expiration_time` bigint NOT NULL COMMENT '成团有效时间',
                                     `groupon_image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '活动图片',
                                     `share_image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '分享图',
                                     `groupon_explain` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '活动说明',
                                     `shop_id` int DEFAULT NULL COMMENT '所属店铺',
                                     `add_time` bigint NOT NULL COMMENT '创建时间',
                                     PRIMARY KEY (`groupon_promotion_id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='拼团活动';

CREATE TABLE `groupon_promotion_detail` (
                                            `groupon_detail_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                            `groupon_promotion_id` bigint NOT NULL COMMENT '拼团活动 ID',
                                            `groupon_promotion_item_id` bigint NOT NULL COMMENT '拼团商品 ID',
                                            `product_id` bigint NOT NULL COMMENT '商品ID',
                                            `sku_id` bigint NOT NULL COMMENT '规格 id',
                                            `group_price` json NOT NULL COMMENT '拼团价',
                                            `limit_num` int NOT NULL DEFAULT '0' COMMENT '可售数量',
                                            `is_join` tinyint NOT NULL COMMENT '是否参加；0-否，1-是',
                                            `add_time` bigint NOT NULL COMMENT '创建时间',
                                            PRIMARY KEY (`groupon_detail_id`),
                                            KEY `groupon_promotion_detail_groupon_promotion_id_index` (`groupon_promotion_id`),
                                            KEY `groupon_promotion_detail_groupon_promotion_item_id_index` (`groupon_promotion_item_id`),
                                            KEY `groupon_promotion_detail_product_id_index` (`product_id`),
                                            KEY `groupon_promotion_detail_sku_id_index` (`sku_id`)
) ENGINE=InnoDB AUTO_INCREMENT=108 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='拼团信息';

CREATE TABLE `groupon_promotion_item` (
                                          `groupon_promotion_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '拼团活动商品 ID',
                                          `groupon_promotion_id` bigint NOT NULL COMMENT '拼团活动 ID',
                                          `product_id` bigint NOT NULL COMMENT '商品ID',
                                          `group_type` tinyint NOT NULL COMMENT '拼团类型；1-普通团，2-阶梯团',
                                          `group_num` json NOT NULL COMMENT '成团人数（json）',
                                          `order_limit_num` int NOT NULL COMMENT '每单限购数量；0-不限购',
                                          `is_auto_group` tinyint NOT NULL COMMENT '是否自动成团；0-否，1-是',
                                          `min_group_num` int NOT NULL COMMENT '成团最低人数',
                                          `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
                                          `add_time` bigint NOT NULL COMMENT '创建时间',
                                          `min_group_price` decimal(10,2) DEFAULT NULL COMMENT '成团最低价',
                                          PRIMARY KEY (`groupon_promotion_item_id`),
                                          KEY `groupon_promotion_item_groupon_promotion_id_index` (`groupon_promotion_id`),
                                          KEY `groupon_promotion_item_product_id_index` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='拼团活动商品';

CREATE TABLE `groupon_promotion_record` (
                                            `groupon_promotion_record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '拼团记录 ID',
                                            `groupon_promotion_id` bigint NOT NULL COMMENT '拼团活动 ID',
                                            `groupon_promotion_item_id` bigint NOT NULL COMMENT '拼团活动商品 ID',
                                            `product_id` bigint NOT NULL COMMENT '商品ID',
                                            `product_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
                                            `join_type` tinyint NOT NULL COMMENT '参与类型；1-开团，2-参团',
                                            `join_status` tinyint NOT NULL COMMENT '拼团状态；1-待成团，2-拼团成功，3-拼团失败',
                                            `avatar` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户头像',
                                            `username` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户名',
                                            `user_id` bigint NOT NULL COMMENT '用户 ID',
                                            `order_id` bigint NOT NULL COMMENT '订单 ID',
                                            `order_sn` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号',
                                            `is_virtual` tinyint NOT NULL COMMENT '是否虚拟；0-否，1-是',
                                            `paid_amount` decimal(10,0) NOT NULL COMMENT '支付金额',
                                            `open_group_record_id` bigint NOT NULL COMMENT '开团记录 ID',
                                            `open_group_time` bigint NOT NULL COMMENT '开团时间',
                                            `group_captain_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '团长名称',
                                            `join_expiration_time` bigint NOT NULL COMMENT '成团截止时间',
                                            `min_group_num` int NOT NULL COMMENT '成团最低人数',
                                            `shop_id` int NOT NULL COMMENT '所属店铺',
                                            `add_time` bigint NOT NULL COMMENT '创建时间',
                                            PRIMARY KEY (`groupon_promotion_record_id`),
                                            KEY `groupon_promotion_record_groupon_promotion_id_index` (`groupon_promotion_id`),
                                            KEY `groupon_promotion_record_groupon_promotion_item_id_index` (`groupon_promotion_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=258 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='拼团记录';

CREATE TABLE `short_url` (
                             `short_url_id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                             `short_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '短码，比如 aB12Xz',
                             `long_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品详情真实地址',
                             `add_time` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '生成时间',
                             PRIMARY KEY (`short_url_id`) USING BTREE,
                             UNIQUE KEY `short_code` (`short_code`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `groupon_promotion_shops` (
                                           `groupon_shop_id` int NOT NULL AUTO_INCREMENT,
                                           `groupon_promotion_id` int DEFAULT NULL COMMENT '活动id',
                                           `shop_ids` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '适用店铺',
                                           PRIMARY KEY (`groupon_shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES ('grouponPromotionManage', '拼团活动', 10000413, 6, 1, '[{\"authName\":\"编辑拼团活动\",\"authSn\":\"grouponPromotionModifyManage\"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES ('grouponPromotionManage', '拼团活动', 10000457, 50, 1, '[{\"authName\":\"编辑拼团活动\",\"authSn\":\"grouponPromotionModifyManage\"}]', '', '', 0, 'shop');
UPDATE `decorate_discrete` SET `decorate_sn` = 'memberDecorate', `decorate_name` = 'memberDecorate', `data` = '[{\"type\":\"bannerList\",\"label\":\"广告位\",\"module\":{\"bannerList\":{\"picId\":\"1099\",\"picThumb\":\"https://oss.tigshop.com/img/gallery/202410/1730107769AvcCOeOycY2F60pmeb.png?x-oss-process=image/resize,m_pad,h_200,h_200\",\"picUrl\":\"https://oss.tigshop.com/img/gallery/202410/1730107769AvcCOeOycY2F60pmeb.png\",\"picName\":\"企业微信截图_b4cfec193c3d4236b7657a209c1546e2\",\"picTitle\":\"demo\",\"picLink\":{\"path\":\"base\",\"label\":\"账号信息\",\"name\":\"账号信息\",\"link\":\"member/profile/info\"}}}},{\"type\":\"menus\",\"label\":\"用户服务\",\"module\":{\"menuType\":\"1\",\"menuList\":[{\"type\":\"default\",\"picTitle\":\"账号管理\",\"picThumb\":\"https://oss.tigshop.com/static/user/zhanghaoguanli.png\",\"picUrl\":\"https://oss.tigshop.com/static/user/zhanghaoguanli.png\",\"picLink\":{\"path\":\"default\",\"label\":\"账号管理\",\"name\":\"账号管理\",\"link\":\"/pages/user/profile/index\"}},{\"type\":\"default\",\"picTitle\":\"收货地址\",\"picThumb\":\"https://oss.tigshop.com/static/user/shouhuodizhi.png\",\"picUrl\":\"https://oss.tigshop.com/static/user/shouhuodizhi.png\",\"picLink\":{\"path\":\"default\",\"label\":\"收货地址\",\"name\":\"收货地址\",\"link\":\"/pages/address/list\"}},{\"type\":\"default\",\"picTitle\":\"联系人管理\",\"picThumb\":\"https://oss.tigshop.com/img/gallery/202509/175790157950oQc07DHYyKmevrHE.jpeg\",\"picUrl\":\"https://oss.tigshop.com/img/gallery/202509/175790157950oQc07DHYyKmevrHE.jpeg\",\"picLink\":{\"path\":\"default\",\"label\":\"联系人管理\",\"name\":\"联系人管理\",\"link\":\"/pages/store/contacts/list\"}},{\"type\":\"default\",\"picTitle\":\"站内消息\",\"picThumb\":\"https://oss.tigshop.com/static/user/xiaoxi.png\",\"picUrl\":\"https://oss.tigshop.com/static/user/xiaoxi.png\",\"picLink\":{\"path\":\"default\",\"label\":\"站内消息\",\"name\":\"站内消息\",\"link\":\"/pages/user/messageLog/index\"}},{\"type\":\"default\",\"picTitle\":\"帮助中心\",\"picThumb\":\"https://oss.tigshop.com/static/user/issue.png\",\"picUrl\":\"https://oss.tigshop.com/static/user/issue.png\",\"picLink\":{\"path\":\"default\",\"label\":\"帮助中心\",\"name\":\"帮助中心\",\"link\":\"/pages/article/issue/list\"}},{\"type\":\"default\",\"picTitle\":\"资讯中心\",\"picThumb\":\"https://oss.tigshop.com/static/user/news.png\",\"picUrl\":\"https://oss.tigshop.com/static/user/news.png\",\"picLink\":{\"path\":\"default\",\"label\":\"资讯中心\",\"name\":\"资讯中心\",\"link\":\"/pages/article/news/list?id=bzgg\"}},{\"type\":\"default\",\"picTitle\":\"分销员中心\",\"picThumb\":\"https://oss.tigshop.com/static/user/salesmanIco.png\",\"picUrl\":\"https://oss.tigshop.com/static/user/salesmanIco.png\",\"picLink\":{\"path\":\"default\",\"label\":\"分销员中心\",\"name\":\"分销员中心\",\"link\":\"/pages/salesman/index\"}},{\"type\":\"default\",\"picTitle\":\"商家入驻\",\"picThumb\":\"https://oss.tigshop.com/static/user/shangjiaruzhu.png\",\"picUrl\":\"https://oss.tigshop.com/static/user/shangjiaruzhu.png\",\"picLink\":{\"path\":\"default\",\"label\":\"商家入驻\",\"name\":\"商家入驻\",\"link\":\"/pages/user/merchantEnter/principalType\"}},{\"type\":\"default\",\"picTitle\":\"积分商城\",\"picThumb\":\"https://oss.tigshop.com/static/user/jifen.png\",\"picUrl\":\"https://oss.tigshop.com/static/user/jifen.png\",\"picLink\":{\"path\":\"default\",\"label\":\"积分商城\",\"name\":\"积分商城\",\"link\":\"/pages/exchange/list\"}},{\"type\":\"default\",\"picTitle\":\"发票管理\",\"picThumb\":\"https://oss.tigshop.com/static/user/fapiao.png\",\"picUrl\":\"https://oss.tigshop.com/static/user/fapiao.png\",\"picLink\":{\"path\":\"default\",\"label\":\"发票管理\",\"name\":\"发票管理\",\"link\":\"/pages/user/invoiceManagement/index\"}},{\"type\":\"default\",\"picTitle\":\"联系人管理\",\"picThumb\":\"https://oss.tigshop.com/img/gallery/202509/175790157950oQc07DHYyKmevrHE.jpeg\",\"picUrl\":\"https://oss.tigshop.com/img/gallery/202509/175790157950oQc07DHYyKmevrHE.jpeg\",\"picLink\":{\"path\":\"default\",\"label\":\"联系人管理\",\"name\":\"联系人管理\",\"link\":\"/pages/store/contacts/list\"}},{\"type\":\"default\",\"picTitle\":\"我的拼团\",\"picThumb\":\"https://oss.tigshop.com/static/user/fapiao.png\",\"picUrl\":\"https://oss.tigshop.com/static/user/fapiao.png\",\"picLink\":{\"path\":\"default\",\"label\":\"我的拼团\",\"name\":\"我的拼团\",\"link\":\"/pages/groupon/mylist\"}}]}}]' WHERE `decorate_sn` = 'memberDecorate';


