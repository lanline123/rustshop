ALTER TABLE `shop_product_category`
    ADD COLUMN `category_pic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '分类图片';

-- 1. 多门店使用原有 shop 表
ALTER TABLE `shop`
    ADD COLUMN `shop_type`             tinyint        DEFAULT '1' COMMENT '1店铺，2门店，3自提点',
    ADD COLUMN `store_parent_id`       int unsigned   DEFAULT NULL COMMENT '店铺父级id',
    ADD COLUMN `shop_cover_picture`    varchar(255)   DEFAULT NULL COMMENT '门店封面',
    ADD COLUMN `shop_show_picture`     mediumtext     DEFAULT NULL COMMENT '门店相册',
    ADD COLUMN `shop_contact_json`     text           DEFAULT NULL COMMENT '联系方式',
    ADD COLUMN `shop_open_close_json`  text           DEFAULT NULL COMMENT '运营时间',
    ADD COLUMN `shop_region_id`        varchar(255)   DEFAULT NULL COMMENT 'region表id',
    ADD COLUMN `shop_detailed_address` varchar(255)   DEFAULT NULL COMMENT '门店详细地址',
    ADD COLUMN `shop_longitude`        DECIMAL(10, 6) DEFAULT NULL COMMENT '店铺经度',
    ADD COLUMN `shop_latitude`         DECIMAL(10, 6) DEFAULT NULL COMMENT '店铺纬度',
    ADD COLUMN `shop_star`             DECIMAL(10, 6) DEFAULT 0 COMMENT '评星',
    ADD COLUMN `shop_total_star`       int            DEFAULT 0 COMMENT '总评价分数',
    ADD COLUMN `shop_star_num`         int            DEFAULT 0 COMMENT '评价数量',
    ADD COLUMN `shop_sales`            int            DEFAULT 0 COMMENT '销量';

ALTER TABLE `shop`
    ADD COLUMN `use_shop_category` int ZEROFILL NULL DEFAULT 0 COMMENT '是否使用门店分类';

ALTER TABLE `shop`
    ADD COLUMN `shop_show_category` text COLLATE utf8mb4_unicode_ci COMMENT '店铺选择展示分类';

-- 2. 产品表添加 产品类型（门店商品）
ALTER TABLE `product`
    ADD COLUMN `shop_pickup_tpl_id` bigint DEFAULT NULL COMMENT '到店自提模板 ID',
    MODIFY COLUMN `product_type` tinyint(1) DEFAULT '1' COMMENT '商品类型 1实物商品 2虚拟商品 3卡密商品 4付费内容 5预约到店 6预约上门';

-- 3. 添加门店商品-门店关联表
CREATE TABLE `area_store_manager`
(
    `area_store_manager_id` INT UNSIGNED                                                  NOT NULL AUTO_INCREMENT COMMENT '区域门店管理',
    `area_store_name`       varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '区域门店名称 ',
    `sort_order`            int(11)                                                                DEFAULT '50' COMMENT '排序',
    `create_time`           bigint                                                        NOT NULL DEFAULT '0' COMMENT '创建时间',
    `update_time`           bigint                                                        NOT NULL DEFAULT '0' COMMENT '更新时间',
    PRIMARY KEY (`area_store_manager_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='区域门店管理';

-- 区域门店与门店关联表
CREATE TABLE `area_store_manager_shop`
(
    `area_store_manager_shop_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '区域门店绑定id',
    `area_store_manager_id`      INT UNSIGNED NOT NULL COMMENT '区域门店ID',
    `shop_id`                    INT UNSIGNED NOT NULL COMMENT '门店ID',
    `create_time`                bigint       NOT NULL DEFAULT '0' COMMENT '创建时间',
    PRIMARY KEY (`area_store_manager_shop_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='区域门店绑定门店表';

-- 提货字段设置 默认数据，text 不支持默认只能从java这边新增
CREATE TABLE `shop_pickup_config`
(
    `shop_pickup_config_id` bigint AUTO_INCREMENT COMMENT 'ID',
    `shop_id`               bigint  NOT NULL COMMENT '门店ID，关联shop表',
    `pickup_name`           varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '到店自提' COMMENT '自定义名称（该名称会显示在买家可选择的配送方式中，默认名称"到店自提"）',
    `status`                tinyint NOT NULL                        DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `pickup_filed`          text COLLATE utf8mb4_unicode_ci COMMENT '提货字段设置',
    `create_time`           bigint  NOT NULL                        DEFAULT '0' COMMENT '创建时间',
    `update_time`           bigint  NOT NULL                        DEFAULT '0' COMMENT '更新时间',
    PRIMARY KEY (`shop_pickup_config_id`),
    UNIQUE KEY `uk_shop_id` (`shop_id`),
    KEY `idx_status` (`status`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC COMMENT '门店自提配置表';

CREATE TABLE `shop_pickup_tpl`
(
    `shop_pickup_tpl_id` bigint AUTO_INCREMENT COMMENT 'ID',
    `shop_id`            bigint                                  NOT NULL COMMENT '门店ID，关联shop表',
    `tpl_name`           varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板名称',
    `stocking_status`    tinyint                                 NOT NULL DEFAULT 0 COMMENT '完成备货状态：0-禁用，1-启用',
    `stocking_json`      text COLLATE utf8mb4_unicode_ci                  DEFAULT NULL COMMENT '完成备货JSON',
    `pickup_time_status` tinyint                                 NOT NULL DEFAULT 0 COMMENT '自提时间状态：0-禁用，1-启用',
    `pickup_time_json`   text COLLATE utf8mb4_unicode_ci                  DEFAULT NULL COMMENT '自提时间JSON',
    `pickup_end_status`  tinyint                                 NOT NULL DEFAULT 0 COMMENT '提货有效期状态：0-禁用，1-启用',
    `pickup_end_json`    text COLLATE utf8mb4_unicode_ci                  DEFAULT NULL COMMENT '提货有效期JSON',
    `is_default`         tinyint unsigned                        NOT NULL DEFAULT '0' COMMENT '是否默认',
    `create_time`        bigint                                  NOT NULL DEFAULT '0' COMMENT '创建时间',
    `update_time`        bigint                                  NOT NULL DEFAULT '0' COMMENT '更新时间',
    PRIMARY KEY (`shop_pickup_tpl_id`)
) ENGINE = INNODB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC COMMENT '门店自提模板表';

CREATE TABLE `tip`
(
    `tip_id`   int unsigned                       NOT NULL AUTO_INCREMENT COMMENT '门店标签id',
    `tip_name` varchar(120) CHARACTER SET utf8mb4 NOT NULL DEFAULT '' COMMENT '门店标签名称 ',
    `status`   int                                         DEFAULT '1' COMMENT '0 禁用，1 启用',
    `add_time` bigint                             NOT NULL DEFAULT '0' COMMENT '创建时间',
    PRIMARY KEY (`tip_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='标签';

CREATE TABLE `store_tip`
(
    `store_tip_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '门店标签id',
    `tip_id`       int unsigned NOT NULL COMMENT '门店标签id',
    `shop_id`      int unsigned NOT NULL COMMENT '门店id',
    `sort`         int                   DEFAULT '1' COMMENT '排序',
    `add_time`     bigint       NOT NULL DEFAULT '0' COMMENT '创建时间',
    PRIMARY KEY (`store_tip_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='门店关联标签';

CREATE TABLE `order_pickup_item`
(
    `order_pickup_id`    int NOT NULL AUTO_INCREMENT,
    `shop_id`            int                                    DEFAULT NULL COMMENT '门店id',
    `pickup_id`          int                                    DEFAULT NULL COMMENT '提货点id',
    `order_id`           int                                    DEFAULT NULL COMMENT '订单id',
    `expect_pickup_time` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '自提时间',
    `user_pickup_id`     int         null comment '用户自提信息',
    PRIMARY KEY (`order_pickup_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='订单提货关联表';


CREATE TABLE `order_pickup_shipment`
(
    `shipment_id`     int NOT NULL AUTO_INCREMENT,
    `order_id`        int                                    DEFAULT NULL COMMENT '订单id',
    `shop_id`         int                                    DEFAULT NULL COMMENT '门店id',
    `pickup_id`       int                                    DEFAULT NULL COMMENT '自提点id',
    `pickup_sn`       varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '提货码',
    `pickup_qr_code`  text COLLATE utf8mb4_general_ci COMMENT '提货二维码',
    `shipment_status` tinyint(1)                             DEFAULT NULL COMMENT '发货状态',
    `check_time`      int                                    DEFAULT NULL COMMENT '核销时间',
    PRIMARY KEY (`shipment_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- 添加权限菜单
-- 平台权限
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (11002001, 'storeManage', '门店管理', 11001058, 50, 1, '[]', '', 'iconfont-tig icon-duoyuyan', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (11002002, 'storeListManage', '门店列表', 11002001, 50, 1,
        '[{"authName":"编辑","authSn":"storeListUpdateManage"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (11002003, 'storeSettingsManage', '门店设置', 11002001, 50, 1,
        '[{"authName":"编辑","authSn":"storeSettingsUpdateManage"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (11002004, 'storeAreaManage', '区域管理', 11002001, 50, 1,
        '[{"authName":"编辑","authSn":"storeAreaUpdateManage"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (11002005, 'storeSelfPackManage', '自提点管理', 11001058, 50, 1, '[]', '', 'iconfont-tig icon-kucunguanli', 0,
        'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (11002006, 'storeSelfPackListManage', '自提点列表', 11002005, 50, 1,
        '[{"authName":"编辑","authSn":"storeSelfPackListUpdateManage"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (11002007, 'storePickupSettingManage', '自提设置', 55, 50, 1,
        '[{"authName":"编辑","authSn":"storePickupSettingUpdateManage"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                         `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`,
                         `admin_type`)
VALUES (11002008, 'storeTipManage', '标签管理', 11002001, 50, 1,
        '[{"authName":"编辑","authSn":"storeTipUpdateManage"}]', '', '', 0, 'admin');


-- 门店权限
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000000, 'shopManage', '店铺列表', 281, 50, 1, '[]', 'shop/list/', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000001, 'accountManage', '店铺资金列表', 281, 50, 1, '[]', 'shop/account/list/', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000002, 'panel', '店铺', 0, 50, 1, '[]', 'panel/index/', 'iconfont-tig icon-jingying', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000003, 'promotion', '营销', 0, 56, 1, '[]', 'promotion/coupon/list/', 'iconfont-tig icon-maiyizengyi', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000004, 'productManage', '商品列表', 20000039, 50, 1,
        '[{"auth_name":"\\u7f16\\u8f91","auth_sn":"productModifyManage"}]', 'product/list/', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000005, 'shopAccountRaplyManage', '提现申请', 281, 50, 1, '[]', 'shop/shopAccountRaply/list/', '', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000006, 'capitalfund', '财务中心', 20000002, 7, 1, '[]', 'capitalfund/dashboard/index/',
        'iconfont-tig icon-31tianmaobao', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000007, 'dashboardManage', '资金概览', 20000006, 50, 1, '[]', 'capitalfund/dashboard/index/', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000008, 'balanceManage', '店铺资金', 20000006, 50, 1, '[]', 'capitalfund/balance/list/', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000009, 'withdrawManage', '提现管理', 20000006, 50, 1, '[]', 'capitalfund/withdraw/index/', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000011, 'consoleManage', '概览', 20000002, 1, 1, '[]', 'panel/index/', 'iconfont-tig icon-yingxiaogailan', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000012, 'statisticsOrder', '销售概览', 20000036, 50, 1, '[]', 'panel/statistics_order/list/', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000013, 'statisticsSale', '销售明细', 20000036, 50, 1, '[]', 'panel/statistics_sale/list/', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000014, 'statisticsGoods', '销售排行', 20000036, 50, 1, '[]', 'panel/statistics_goods/list/', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000016, 'orderManage', '全部订单', 20000037, 50, 1,
        '[{"auth_name":"\\u53d1\\u8d27","auth_sn":"orderDeliverManage"},{"auth_name":"\\u6536\\u8d27","auth_sn":"orderConfirmReceiptManage"},{"auth_name":"\\u4fee\\u6539\\u6536\\u8d27\\u4eba","auth_sn":"orderModifyConsigneeManage"},{"auth_name":"\\u4fee\\u6539\\u914d\\u9001\\u4fe1\\u606f","auth_sn":"orderModifyShippingManage"},{"auth_name":"\\u4fee\\u6539\\u8ba2\\u5355\\u91d1\\u989d","auth_sn":"orderModifyMoneyManage"},{"auth_name":"\\u53d6\\u6d88\\u8ba2\\u5355","auth_sn":"cancelOrderManage"},{"auth_name":"\\u8ba2\\u5355\\u786e\\u8ba4","auth_sn":"setConfirmManage"},{"auth_name":"\\u8f6f\\u5220\\u9664","auth_sn":"delOrderManage"},{"auth_name":"\\u62c6\\u5355","auth_sn":"splitStoreOrderManage"},{"auth_name":"\\u8bbe\\u4e3a\\u5df2\\u652f\\u4ed8","auth_sn":"setPaidManage"},{"auth_name":"\\u4fee\\u6539\\u5546\\u54c1\\u4fe1\\u606f","auth_sn":"modifyProductManage"},{"auth_name":"\\u8bbe\\u7f6e\\u5546\\u5bb6\\u5907\\u6ce8","auth_sn":"setAdminNoteManage"},{"auth_name":"\\u6dfb\\u52a0\\u8ba2\\u5355\\u65e5\\u5fd7","auth_sn":"orderLogModifyManage"}]',
        'order/list/', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000017, 'aftersalesManage', '售后订单', 20000038, 50, 1,
        '[{"auth_name":"\\u7f16\\u8f91","auth_sn":"aftersalesModifyManage"}]', 'order/aftersales/list', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000018, 'orderExportManage', '订单导出', 20000037, 50, 1, '[]', 'order/order_export/list/', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000019, 'productGroupManage', '商品分组', 20000039, 50, 1,
        '[{"auth_name":"\\u7f16\\u8f91","auth_sn":"productGroupModifyManage"}]', 'product/product_group/list/', '', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000020, 'merchantSetting', '店铺设置', 20000050, 1, 1, '[]', 'merchant_setting/mobile_shop_decorate',
        'iconfont-tig icon-31shezhi', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000021, 'couponManage', '优惠券', 20000046, 1, 1,
        '[{"auth_name":"\\u7f16\\u8f91","auth_sn":"promotionCouponModifyManage"}]', 'promotion/coupon/list/', '', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000022, 'seckillManage', '限时秒杀', 20000046, 2, 1,
        '[{"auth_name":"\\u7f16\\u8f91","auth_sn":"seckillModifyManage"}]', 'promotion/seckill/list/', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000023, 'productActivityManage', '优惠活动', 20000046, 3, 1,
        '[{"auth_name":"\\u7f16\\u8f91","auth_sn":"productPromotionModifyManage"}]', 'promotion/product_activity/list',
        '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000024, 'ShopTeamManage', '店铺设置', 20000020, 2, 1, '[]', 'merchant_setting/shop_team/', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000025, 'EmployeeManagement', '员工管理', 20000047, 4, 1, '[]', 'merchant_setting/employee/list/', '', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000026, 'ShopInfoManage', '店铺信息', 20000020, 3, 1, '[]', 'merchant_setting/shop_info', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000027, 'ShopRoleManage', '角色管理', 20000047, 5, 1, '[]', 'merchant_setting/shop_role/list/', '', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000028, 'shopShipping', '配送设置', 20000050, 2, 1, '[]', 'merchant_setting/shop_shipping/',
        'iconfont-tig icon-peisongshezhi', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000029, 'shopShippingTplManage', '运费模板', 20000028, 52, 1, '[]',
        'merchant_setting/shop_shipping/shop_shipping_tpl/list/', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000030, 'productGiftManage', '活动赠品', 20000046, 53, 1, '[]', 'promotion/product_gift/list', '', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000031, 'EmployeeLogManagement', '操作日志', 20000047, 50, 1, '[]', 'merchant_setting/employeeLog/list/', '',
        0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000032, 'eCardManage', '电子卡券', 20000046, 50, 1, '[]', 'promotion/e_card/list/', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000033, 'fullReductionManage', '满减活动', 20000046, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000034, 'rewardGiftManage', '满赠活动', 20000046, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000035, 'limitdiscountManage', '限时折扣', 20000046, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000036, 'salesOverview', '销售统计', 20000002, 5, 1, '[]', '', 'iconfont-tig icon-xiaoshoutongji', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000037, 'orderManagement', '订单管理', 20000002, 2, 1, '[]', '', 'iconfont-tig icon-dingdanguanli', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000038, 'aftersaleManagement', '售后管理', 20000002, 3, 1, '[]', '', 'iconfont-tig icon-shouhouguanli', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000039, 'commodityManage', '商品管理', 20000002, 4, 1, '[]', '', 'iconfont-tig icon-shangpinguanli', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000040, 'brandManage', '品牌管理', 20000039, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000041, 'commentManage', '评价管理', 20000039, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000042, 'productAttributesTplManage', '属性模板', 20000039, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000043, 'stockControl', '库存管理', 20000002, 6, 1, '[]', '', 'iconfont-tig icon-kucunguanli', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000044, 'productInventoryLogManage', '库存日志', 20000043, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000045, 'promotionManage', '营销概览', 20000003, 50, 1, '[]', '', 'iconfont-tig icon-yingxiaogailan', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000046, 'promotionOverview', '营销管理', 20000003, 50, 1, '[]', '', 'iconfont-tig icon-yingxiaoguanli', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000047, 'accountManagement', '账号管理', 20000050, 3, 1, '[]', '', 'iconfont-tig icon-guanlizhanghao', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000048, 'accountEditingManage', '账号设置', 20000047, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000049, 'financialLogManage', '资金明细', 20000006, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000050, 'setting', '设置', 0, 50, 1, '[]', '', 'iconfont-tig icon-shezhi', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000051, 'deliverManage', '发货管理', 20000037, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000052, 'salesman', '分销', 0, 50, 1, '[]', '', 'iconfont-tig icon-fenxiao', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000053, 'overviewManage', '分销概览', 20000052, 50, 1, '[]', '', 'iconfont-tig icon-yingxiaogailan', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000054, 'salesmanProduct', '分销商品', 20000052, 50, 1, '[]', '', 'iconfont-tig icon-fenxiaoshangpin', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000055, 'salesmanProductManage', '商品佣金管理', 20000054, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000056, 'AnalysisTableManage', '成交分析', 20000054, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000057, 'performanceSettlement', '分销结算', 20000052, 50, 1, '[]', '', 'iconfont-tig icon-fenxiaojiesuan', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000058, 'performanceSettlementManage', '业绩结算', 20000057, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000059, 'noticeManage', '内容管理', 20000052, 50, 1, '[]', '', 'iconfont-tig icon-wenzhangguanli', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000060, 'materialManage', '素材管理', 20000059, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000061, 'vendorSetting', '供应商设置', 20000050, 50, 1, '[]', '', 'iconfont-tig icon-xitongshezhi', 0,
        'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000062, 'vendorSettingManage', '价格设置', 20000061, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000063, 'shopBaseReceiptManage', '小票打印', 20000020, 50, 1,
        '[{"authName":"编辑","authSn":"shopBaseReceiptUpdateManage"}]', '', '', 0, 'store');
INSERT INTO authority (authority_id, authority_sn, authority_name, parent_id, sort_order, is_show, child_auth,
                       route_link, authority_ico, is_system, admin_type)
VALUES (20000064, 'accountListManage', '账户管理', 20000006, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (20000065, 'storePickupSettingManage', '自提设置', 20000028, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (20000066, 'statementDownload', '对账单下载', 20000006, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                         `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`,
                         `admin_type`)
VALUES (20000067, 'statementDetails', '对账单明细', 20000006, 50, 1, '[]', '', '', 0, 'store');


-- 自提点相关权限
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (20000100, 'panel', '店铺', 0, 50, 1, '[]', 'panel/index/', 'iconfont-tig icon-jingying', 0, 'pickup');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (20000114, 'orderManage', '全部订单', 20000135, 50, 1,
        '[{\"auth_name\":\"\\u53d1\\u8d27\",\"auth_sn\":\"orderDeliverManage\"},{\"auth_name\":\"\\u6536\\u8d27\",\"auth_sn\":\"orderConfirmReceiptManage\"},{\"auth_name\":\"\\u4fee\\u6539\\u6536\\u8d27\\u4eba\",\"auth_sn\":\"orderModifyConsigneeManage\"},{\"auth_name\":\"\\u4fee\\u6539\\u914d\\u9001\\u4fe1\\u606f\",\"auth_sn\":\"orderModifyShippingManage\"},{\"auth_name\":\"\\u4fee\\u6539\\u8ba2\\u5355\\u91d1\\u989d\",\"auth_sn\":\"orderModifyMoneyManage\"},{\"auth_name\":\"\\u53d6\\u6d88\\u8ba2\\u5355\",\"auth_sn\":\"cancelOrderManage\"},{\"auth_name\":\"\\u8ba2\\u5355\\u786e\\u8ba4\",\"auth_sn\":\"setConfirmManage\"},{\"auth_name\":\"\\u8f6f\\u5220\\u9664\",\"auth_sn\":\"delOrderManage\"},{\"auth_name\":\"\\u62c6\\u5355\",\"auth_sn\":\"splitStoreOrderManage\"},{\"auth_name\":\"\\u8bbe\\u4e3a\\u5df2\\u652f\\u4ed8\",\"auth_sn\":\"setPaidManage\"},{\"auth_name\":\"\\u4fee\\u6539\\u5546\\u54c1\\u4fe1\\u606f\",\"auth_sn\":\"modifyProductManage\"},{\"auth_name\":\"\\u8bbe\\u7f6e\\u5546\\u5bb6\\u5907\\u6ce8\",\"auth_sn\":\"setAdminNoteManage\"},{\"auth_name\":\"\\u6dfb\\u52a0\\u8ba2\\u5355\\u65e5\\u5fd7\",\"auth_sn\":\"orderLogModifyManage\"}]',
        'order/list/', '', 0, 'pickup');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (20000123, 'EmployeeManagement', '员工管理', 20000145, 4, 1, '[]', 'merchant_setting/employee/list/', '', 0,
        'pickup');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (20000125, 'ShopRoleManage', '角色管理', 20000145, 5, 1, '[]', 'merchant_setting/shop_role/list/', '', 0,
        'pickup');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (20000129, 'EmployeeLogManagement', '操作日志', 20000145, 50, 1, '[]', 'merchant_setting/employeeLog/list/', '',
        0, 'pickup');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (20000135, 'orderManagement', '订单管理', 20000100, 2, 1, '[]', '', 'iconfont-tig icon-dingdanguanli', 0,
        'pickup');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (20000145, 'accountManagement', '账号管理', 20000148, 3, 1, '[]', '', 'iconfont-tig icon-guanlizhanghao', 0,
        'pickup');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (20000146, 'accountEditingManage', '账号设置', 20000145, 50, 1, '[]', '', '', 0, 'pickup');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (20000148, 'setting', '设置', 0, 50, 1, '[]', '', 'iconfont-tig icon-shezhi', 0, 'pickup');


-- 5. 其他(config 获取开发中其他问题）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('storePostAllocationStatus', '0', 1755840068, 1, 'admin', 1755840068, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('storeIndependentGoods', '0', 1755840068, 1, 'admin', 1755840068, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('storeAssignProductName', '0', 1755840068, 1, 'admin', 1755840068, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('storeAssignProductPrice', '0', 1755840068, 1, 'admin', 1755840068, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('storeUseSoloProductStock', '1', 1755840068, 1, 'admin', 1755840068, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('storeUseTotalProductStock', '0', 1755840068, 1, 'admin', 1755840068, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('storeCustomSubmitShippingType', '0', 1755840068, 1, 'admin', 1755840068, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('clientDefaultUse', '0', 1755840068, 1, 'admin', 1755840068, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('storeShowOtherCityStore', '0', 1755840068, 1, 'admin', 1755840068, 1, 'admin', 0);


-- 新增自营的到店自提配置
INSERT INTO `shop_pickup_config` (`shop_id`, `pickup_name`, `status`, `pickup_filed`, `create_time`, `update_time`)
VALUES (0, '门店自提', 1,
        '[{\"fieldKey\":\"pickupPoint\",\"fieldName\":\"自提点\",\"required\":true,\"visible\":true},{\"fieldKey\":\"pickupTime\",\"fieldName\":\"提货时间\",\"required\":true,\"visible\":true},{\"fieldKey\":\"pickuperPhone\",\"fieldName\":\"提货人手机号\",\"required\":true,\"visible\":true},{\"fieldKey\":\"pickuperName\",\"fieldName\":\"提货人姓名\",\"required\":false,\"visible\":false}]',
        1756351953, 1756351953);
INSERT INTO `shop_pickup_tpl` (`shop_id`, `tpl_name`, `stocking_status`, `stocking_json`, `pickup_time_status`,
                               `pickup_time_json`, `pickup_end_status`, `pickup_end_json`, `is_default`, `create_time`,
                               `update_time`)
VALUES (0, '默认自提模板', 0, NULL, 0, NULL, 0, NULL, 1, 1756351953, 1756351953);

create table product_org
(
    product_org_id bigint auto_increment comment 'ID'
        primary key,
    product_id     bigint  not null comment '商品ID',
    org_type       tinyint not null comment '分配组织类型；1-门店，2-区域',
    org_id         bigint  not null comment '分配组织 ID',
    add_time       bigint  not null comment '创建时间'
)
    comment '商品组织分配';

create table store_product
(
    store_product_id bigint auto_increment comment 'ID'
        primary key,
    product_id       int            null comment '商品id',
    product_name     varchar(255)   null comment '商品名称',
    product_price    decimal(10, 2) null comment '商品价格',
    product_status   tinyint        not null comment '商品状态；1-上架，0-下架',
    product_stock    int            null comment '产品库存',
    shop_id          bigint         not null comment '门店id',
    add_time         bigint         not null comment '创建时间',
    card_group_id    int     DEFAULT '0' COMMENT '关联的电子卡券分组',
    is_delete        tinyint DEFAULT NULL COMMENT '是否删除'
)
    comment '分配店铺商品';

ALTER TABLE `store_product`
    ADD COLUMN `shop_category_id` int NULL COMMENT '店铺分类id' AFTER `is_delete`;

create table store_product_sku
(
    store_product_sku_id bigint auto_increment comment 'ID'
        primary key,
    product_id           bigint         not null comment '商品ID',
    product_sku_id       bigint         not null comment '商品 sku ID',
    sku_price            decimal(10, 2) null comment '商品价格',
    sku_stock            int            null comment 'sku库存',
    add_time             bigint         not null comment '创建时间',
    store_product_id     bigint         NOT NULL COMMENT '门店商品id'
)
    comment '分配店铺 sku';

alter table product
    add is_shop_pickup tinyint default 0 not null comment '是否到店自提；0-否，1-是';

ALTER TABLE `product`
    ADD COLUMN `is_logistics`     tinyint default 1 not null COMMENT '是否物流发货0-否，1-是',
    ADD COLUMN `is_shop_delivery` tinyint default 0 not null COMMENT '是否门店配送0-否，1-是';

CREATE TABLE `user_pickup_info`
(
    `user_pickup_id`   mediumint unsigned                                           NOT NULL AUTO_INCREMENT COMMENT '自增id',
    `user_id`          mediumint unsigned                                           NOT NULL DEFAULT '0' COMMENT '用户id',
    `consignee`        varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '收货人的名字',
    `mobile`           varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '收货人的手机号',
    `mobile_area_code` varchar(20) COLLATE utf8mb4_general_ci                                DEFAULT NULL COMMENT '区号',
    `is_default`       tinyint unsigned                                             NOT NULL DEFAULT '0' COMMENT '是否默认',
    `is_selected`      tinyint unsigned                                             NOT NULL DEFAULT '0' COMMENT '是否选中',
    PRIMARY KEY (`user_pickup_id`) USING BTREE,
    KEY `user_id` (`user_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC COMMENT =' 用户自提信息表';

-- 地域表更新
UPDATE `region`
SET `level`       = 2,
    `region_name` = '北京市',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 110000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '北京市',
    `parent_id`   = 110000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 110100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东城区',
    `parent_id`   = 110100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 110101;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西城区',
    `parent_id`   = 110100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 110102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '朝阳区',
    `parent_id`   = 110100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 110105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丰台区',
    `parent_id`   = 110100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 110106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石景山区',
    `parent_id`   = 110100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 110107;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海淀区',
    `parent_id`   = 110100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 110108;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '门头沟区',
    `parent_id`   = 110100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 110109;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '房山区',
    `parent_id`   = 110100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 110111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '通州区',
    `parent_id`   = 110100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 110112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '顺义区',
    `parent_id`   = 110100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 110113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昌平区',
    `parent_id`   = 110100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 110114;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大兴区',
    `parent_id`   = 110100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 110115;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '怀柔区',
    `parent_id`   = 110100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 110116;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平谷区',
    `parent_id`   = 110100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 110117;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '密云区',
    `parent_id`   = 110100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 110118;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '延庆区',
    `parent_id`   = 110100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 110119;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '天津市',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 120000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '天津市',
    `parent_id`   = 120000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 120100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '和平区',
    `parent_id`   = 120100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 120101;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河东区',
    `parent_id`   = 120100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 120102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河西区',
    `parent_id`   = 120100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 120103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南开区',
    `parent_id`   = 120100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 120104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河北区',
    `parent_id`   = 120100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 120105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '红桥区',
    `parent_id`   = 120100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 120106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东丽区',
    `parent_id`   = 120100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 120110;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西青区',
    `parent_id`   = 120100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 120111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '津南区',
    `parent_id`   = 120100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 120112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北辰区',
    `parent_id`   = 120100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 120113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武清区',
    `parent_id`   = 120100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 120114;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宝坻区',
    `parent_id`   = 120100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 120115;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '滨海新区',
    `parent_id`   = 120100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 120116;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁河区',
    `parent_id`   = 120100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 120117;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '静海区',
    `parent_id`   = 120100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 120118;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蓟州区',
    `parent_id`   = 120100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 120119;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '河北省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 130000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '石家庄市',
    `parent_id`   = 130000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 130100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长安区',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 130102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桥西区',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 130104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新华区',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 130105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '井陉矿区',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 130107;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '裕华区',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 130108;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '藁城区',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 130109;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鹿泉区',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130110;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '栾城区',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '井陉县',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 130121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '正定县',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 130123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '行唐县',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 130125;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '灵寿县',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130126;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高邑县',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 130127;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '深泽县',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 130128;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '赞皇县',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 130129;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '无极县',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 130130;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平山县',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 130131;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '元氏县',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 130132;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '赵县',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 130133;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石家庄高新技术产业开发区',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 130171;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石家庄循环化工园区',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 130172;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '辛集市',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 130181;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '晋州市',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 130183;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新乐市',
    `parent_id`   = 130100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 130184;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '唐山市',
    `parent_id`   = 130000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 130200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '路南区',
    `parent_id`   = 130200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '路北区',
    `parent_id`   = 130200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '古冶区',
    `parent_id`   = 130200,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 130204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '开平区',
    `parent_id`   = 130200,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 130205;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丰南区',
    `parent_id`   = 130200,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 130207;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丰润区',
    `parent_id`   = 130200,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 130208;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '曹妃甸区',
    `parent_id`   = 130200,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 130209;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '滦南县',
    `parent_id`   = 130200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乐亭县',
    `parent_id`   = 130200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130225;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '迁西县',
    `parent_id`   = 130200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 130227;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玉田县',
    `parent_id`   = 130200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 130229;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '唐山高新技术产业开发区',
    `parent_id`   = 130200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 130273;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河北唐山海港经济开发区',
    `parent_id`   = 130200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 130274;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '遵化市',
    `parent_id`   = 130200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 130281;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '迁安市',
    `parent_id`   = 130200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 130283;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '滦州市',
    `parent_id`   = 130200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130284;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '秦皇岛市',
    `parent_id`   = 130000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 130300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海港区',
    `parent_id`   = 130300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 130302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '山海关区',
    `parent_id`   = 130300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 130303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北戴河区',
    `parent_id`   = 130300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 130304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '抚宁区',
    `parent_id`   = 130300,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 130306;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青龙满族自治县',
    `parent_id`   = 130300,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 130321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昌黎县',
    `parent_id`   = 130300,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 130322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '卢龙县',
    `parent_id`   = 130300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '秦皇岛市经济技术开发区',
    `parent_id`   = 130300,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 130371;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北戴河新区',
    `parent_id`   = 130300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 130372;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '经济技术开发区',
    `parent_id`   = 130300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 130390;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '邯郸市',
    `parent_id`   = 130000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 130400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '邯山区',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 130402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丛台区',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 130403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '复兴区',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 130404;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '峰峰矿区',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 130406;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '肥乡区',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 130407;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永年区',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 130408;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临漳县',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '成安县',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 130424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大名县',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 130425;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '涉县',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 130426;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '磁县',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 130427;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '邱县',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 130430;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鸡泽县',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 130431;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '广平县',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 130432;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '馆陶县',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 130433;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '魏县',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 130434;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '曲周县',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 130435;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '邯郸经济技术开发区',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 130471;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '邯郸冀南新区',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 130473;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武安市',
    `parent_id`   = 130400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 130481;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '邢台市',
    `parent_id`   = 130000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 130500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '襄都区',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 130502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '信都区',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 130503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '任泽区',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 130505;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南和区',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 130506;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临城县',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '内丘县',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 130523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '柏乡县',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 130524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '隆尧县',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130525;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁晋县',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 130528;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巨鹿县',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 130529;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新河县',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 130530;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '广宗县',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 130531;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平乡县',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 130532;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '威县',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 130533;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清河县',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 130534;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临西县',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130535;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河北邢台经济开发区',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 130571;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南宫市',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 130581;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沙河市',
    `parent_id`   = 130500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 130582;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '保定市',
    `parent_id`   = 130000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 130600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '竞秀区',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 130602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莲池区',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130606;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '满城区',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 130607;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清苑区',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 130608;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '徐水区',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 130609;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '涞水县',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阜平县',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 130624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '定兴县',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 130626;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '唐县',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 130627;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高阳县',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 130628;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '容城县',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 130629;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '涞源县',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130630;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '望都县',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 130631;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安新县',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 130632;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '易县',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 130633;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '曲阳县',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 130634;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蠡县',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130635;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '顺平县',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 130636;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '博野县',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 130637;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雄县',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 130638;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '保定高新技术产业开发区',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 130671;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '保定白沟新城',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 130672;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '涿州市',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 130681;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '定州市',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 130682;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安国市',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 130683;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高碑店市',
    `parent_id`   = 130600,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 130684;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '张家口市',
    `parent_id`   = 130000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 130700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桥东区',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 130702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桥西区',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 130703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宣化区',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 130705;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '下花园区',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 130706;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万全区',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 130708;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '崇礼区',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 130709;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '张北县',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 130722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '康保县',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 130723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沽源县',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 130724;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '尚义县',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 130725;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蔚县',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 130726;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阳原县',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 130727;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '怀安县',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 130728;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '怀来县',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 130730;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '涿鹿县',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 130731;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '赤城县',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 130732;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '张家口市察北管理区',
    `parent_id`   = 130700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 130772;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '承德市',
    `parent_id`   = 130000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 130800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '双桥区',
    `parent_id`   = 130800,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 130802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '双滦区',
    `parent_id`   = 130800,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 130803;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鹰手营子矿区',
    `parent_id`   = 130800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 130804;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '承德县',
    `parent_id`   = 130800,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 130821;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴隆县',
    `parent_id`   = 130800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 130822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '滦平县',
    `parent_id`   = 130800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130824;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '隆化县',
    `parent_id`   = 130800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 130825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丰宁满族自治县',
    `parent_id`   = 130800,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 130826;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宽城满族自治县',
    `parent_id`   = 130800,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 130827;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '围场满族蒙古族自治县',
    `parent_id`   = 130800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 130828;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '承德高新技术产业开发区',
    `parent_id`   = 130800,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 130871;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平泉市',
    `parent_id`   = 130800,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 130881;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '沧州市',
    `parent_id`   = 130000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 130900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新华区',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 130902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '运河区',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 130903;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沧县',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 130921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青县',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 130922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东光县',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 130923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海兴县',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 130924;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盐山县',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 130925;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '肃宁县',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 130926;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南皮县',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 130927;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吴桥县',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 130928;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '献县',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 130929;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '孟村回族自治县',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 130930;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河北沧州经济开发区',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 130971;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沧州高新技术产业开发区',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 130972;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沧州渤海新区',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 130973;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泊头市',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 130981;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '任丘市',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 130982;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黄骅市',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 130983;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河间市',
    `parent_id`   = 130900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 130984;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '廊坊市',
    `parent_id`   = 130000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 131000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安次区',
    `parent_id`   = 131000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 131002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '广阳区',
    `parent_id`   = 131000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 131003;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '固安县',
    `parent_id`   = 131000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 131022;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永清县',
    `parent_id`   = 131000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 131023;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '香河县',
    `parent_id`   = 131000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 131024;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大城县',
    `parent_id`   = 131000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 131025;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '文安县',
    `parent_id`   = 131000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 131026;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大厂回族自治县',
    `parent_id`   = 131000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 131028;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '廊坊经济技术开发区',
    `parent_id`   = 131000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 131071;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '霸州市',
    `parent_id`   = 131000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 131081;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三河市',
    `parent_id`   = 131000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 131082;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '开发区',
    `parent_id`   = 131000,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 131090;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '衡水市',
    `parent_id`   = 130000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 131100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桃城区',
    `parent_id`   = 131100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 131102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '冀州区',
    `parent_id`   = 131100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 131103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '枣强县',
    `parent_id`   = 131100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 131121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武邑县',
    `parent_id`   = 131100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 131122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武强县',
    `parent_id`   = 131100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 131123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '饶阳县',
    `parent_id`   = 131100,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 131124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安平县',
    `parent_id`   = 131100,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 131125;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '故城县',
    `parent_id`   = 131100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 131126;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '景县',
    `parent_id`   = 131100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 131127;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阜城县',
    `parent_id`   = 131100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 131128;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河北衡水经济开发区',
    `parent_id`   = 131100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 131171;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '衡水滨湖新区',
    `parent_id`   = 131100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 131172;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '深州市',
    `parent_id`   = 131100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 131182;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '山西省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 140000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '太原市',
    `parent_id`   = 140000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 140100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '小店区',
    `parent_id`   = 140100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 140105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '迎泽区',
    `parent_id`   = 140100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '杏花岭区',
    `parent_id`   = 140100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 140107;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '尖草坪区',
    `parent_id`   = 140100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 140108;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万柏林区',
    `parent_id`   = 140100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 140109;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '晋源区',
    `parent_id`   = 140100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 140110;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清徐县',
    `parent_id`   = 140100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 140121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阳曲县',
    `parent_id`   = 140100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '娄烦县',
    `parent_id`   = 140100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 140123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '古交市',
    `parent_id`   = 140100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 140181;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '大同市',
    `parent_id`   = 140000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 140200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新荣区',
    `parent_id`   = 140200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 140212;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平城区',
    `parent_id`   = 140200,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 140213;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '云冈区',
    `parent_id`   = 140200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140214;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '云州区',
    `parent_id`   = 140200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140215;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阳高县',
    `parent_id`   = 140200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天镇县',
    `parent_id`   = 140200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 140222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '广灵县',
    `parent_id`   = 140200,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 140223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '灵丘县',
    `parent_id`   = 140200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 140224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浑源县',
    `parent_id`   = 140200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 140225;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '左云县',
    `parent_id`   = 140200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 140226;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '山西大同经济开发区',
    `parent_id`   = 140200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 140271;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '阳泉市',
    `parent_id`   = 140000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '城区',
    `parent_id`   = 140300,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 140302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '矿区',
    `parent_id`   = 140300,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 140303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '郊区',
    `parent_id`   = 140300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 140311;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平定县',
    `parent_id`   = 140300,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 140321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盂县',
    `parent_id`   = 140300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140322;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '长治市',
    `parent_id`   = 140000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 140400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '潞州区',
    `parent_id`   = 140400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 140403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '上党区',
    `parent_id`   = 140400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 140404;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '屯留区',
    `parent_id`   = 140400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 140405;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '潞城区',
    `parent_id`   = 140400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 140406;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '襄垣县',
    `parent_id`   = 140400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 140423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平顺县',
    `parent_id`   = 140400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 140425;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黎城县',
    `parent_id`   = 140400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 140426;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '壶关县',
    `parent_id`   = 140400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 140427;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长子县',
    `parent_id`   = 140400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 140428;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武乡县',
    `parent_id`   = 140400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 140429;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沁县',
    `parent_id`   = 140400,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 140430;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沁源县',
    `parent_id`   = 140400,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 140431;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '山西长治高新技术产业园区',
    `parent_id`   = 140400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 140471;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '晋城市',
    `parent_id`   = 140000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 140500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '城区',
    `parent_id`   = 140500,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 140502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沁水县',
    `parent_id`   = 140500,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 140521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阳城县',
    `parent_id`   = 140500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '陵川县',
    `parent_id`   = 140500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 140524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泽州县',
    `parent_id`   = 140500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 140525;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高平市',
    `parent_id`   = 140500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 140581;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '朔州市',
    `parent_id`   = 140000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 140600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '朔城区',
    `parent_id`   = 140600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 140602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平鲁区',
    `parent_id`   = 140600,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 140603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '山阴县',
    `parent_id`   = 140600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 140621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '应县',
    `parent_id`   = 140600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '右玉县',
    `parent_id`   = 140600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '山西朔州经济开发区',
    `parent_id`   = 140600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 140671;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '怀仁市',
    `parent_id`   = 140600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 140681;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '晋中市',
    `parent_id`   = 140000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 140700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '榆次区',
    `parent_id`   = 140700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '太谷区',
    `parent_id`   = 140700,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 140703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '榆社县',
    `parent_id`   = 140700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140721;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '左权县',
    `parent_id`   = 140700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 140722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '和顺县',
    `parent_id`   = 140700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 140723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昔阳县',
    `parent_id`   = 140700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 140724;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '寿阳县',
    `parent_id`   = 140700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 140725;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '祁县',
    `parent_id`   = 140700,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 140727;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平遥县',
    `parent_id`   = 140700,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 140728;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '灵石县',
    `parent_id`   = 140700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 140729;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '介休市',
    `parent_id`   = 140700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 140781;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '运城市',
    `parent_id`   = 140000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盐湖区',
    `parent_id`   = 140800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临猗县',
    `parent_id`   = 140800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 140821;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万荣县',
    `parent_id`   = 140800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 140822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '闻喜县',
    `parent_id`   = 140800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 140823;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '稷山县',
    `parent_id`   = 140800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 140824;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新绛县',
    `parent_id`   = 140800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 140825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '绛县',
    `parent_id`   = 140800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 140826;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '垣曲县',
    `parent_id`   = 140800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140827;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '夏县',
    `parent_id`   = 140800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 140828;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平陆县',
    `parent_id`   = 140800,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 140829;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芮城县',
    `parent_id`   = 140800,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 140830;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永济市',
    `parent_id`   = 140800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140881;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河津市',
    `parent_id`   = 140800,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 140882;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '忻州市',
    `parent_id`   = 140000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 140900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '忻府区',
    `parent_id`   = 140900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 140902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '定襄县',
    `parent_id`   = 140900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 140921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五台县',
    `parent_id`   = 140900,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 140922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '代县',
    `parent_id`   = 140900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 140923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '繁峙县',
    `parent_id`   = 140900,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 140924;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁武县',
    `parent_id`   = 140900,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 140925;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '静乐县',
    `parent_id`   = 140900,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 140926;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '神池县',
    `parent_id`   = 140900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 140927;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五寨县',
    `parent_id`   = 140900,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 140928;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岢岚县',
    `parent_id`   = 140900,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 140929;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河曲县',
    `parent_id`   = 140900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 140930;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '保德县',
    `parent_id`   = 140900,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 140931;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '偏关县',
    `parent_id`   = 140900,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 140932;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五台山风景名胜区',
    `parent_id`   = 140900,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 140971;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '原平市',
    `parent_id`   = 140900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 140981;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '临汾市',
    `parent_id`   = 140000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 141000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '尧都区',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 141002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '曲沃县',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 141021;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '翼城县',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 141022;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '襄汾县',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 141023;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洪洞县',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 141024;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '古县',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 141025;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安泽县',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 141026;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浮山县',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 141027;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吉县',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 141028;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乡宁县',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 141029;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大宁县',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 141030;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '隰县',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 141031;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永和县',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 141032;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蒲县',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 141033;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汾西县',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 141034;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '侯马市',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 141081;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '霍州市',
    `parent_id`   = 141000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 141082;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '吕梁市',
    `parent_id`   = 140000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 141100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '离石区',
    `parent_id`   = 141100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 141102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '文水县',
    `parent_id`   = 141100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 141121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '交城县',
    `parent_id`   = 141100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 141122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴县',
    `parent_id`   = 141100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 141123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临县',
    `parent_id`   = 141100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 141124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '柳林县',
    `parent_id`   = 141100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 141125;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石楼县',
    `parent_id`   = 141100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 141126;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岚县',
    `parent_id`   = 141100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 141127;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '方山县',
    `parent_id`   = 141100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 141128;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中阳县',
    `parent_id`   = 141100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 141129;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '交口县',
    `parent_id`   = 141100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 141130;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '孝义市',
    `parent_id`   = 141100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 141181;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汾阳市',
    `parent_id`   = 141100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 141182;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '内蒙古自治区',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 150000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '呼和浩特市',
    `parent_id`   = 150000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 150100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新城区',
    `parent_id`   = 150100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 150102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '回民区',
    `parent_id`   = 150100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 150103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玉泉区',
    `parent_id`   = 150100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 150104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '赛罕区',
    `parent_id`   = 150100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 150105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '土默特左旗',
    `parent_id`   = 150100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 150121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '托克托县',
    `parent_id`   = 150100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 150122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '和林格尔县',
    `parent_id`   = 150100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 150123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清水河县',
    `parent_id`   = 150100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 150124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武川县',
    `parent_id`   = 150100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 150125;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '呼和浩特经济技术开发区',
    `parent_id`   = 150100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 150172;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '包头市',
    `parent_id`   = 150000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 150200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东河区',
    `parent_id`   = 150200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 150202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昆都仑区',
    `parent_id`   = 150200,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 150203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青山区',
    `parent_id`   = 150200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 150204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石拐区',
    `parent_id`   = 150200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 150205;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '白云鄂博矿区',
    `parent_id`   = 150200,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 150206;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '九原区',
    `parent_id`   = 150200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 150207;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '土默特右旗',
    `parent_id`   = 150200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 150221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '固阳县',
    `parent_id`   = 150200,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 150222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '达尔罕茂明安联合旗',
    `parent_id`   = 150200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 150223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '包头稀土高新技术产业开发区',
    `parent_id`   = 150200,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 150271;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '乌海市',
    `parent_id`   = 150000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 150300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海勃湾区',
    `parent_id`   = 150300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 150302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海南区',
    `parent_id`   = 150300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 150303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌达区',
    `parent_id`   = 150300,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 150304;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '赤峰市',
    `parent_id`   = 150000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 150400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '红山区',
    `parent_id`   = 150400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 150402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '元宝山区',
    `parent_id`   = 150400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 150403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '松山区',
    `parent_id`   = 150400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 150404;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿鲁科尔沁旗',
    `parent_id`   = 150400,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 150421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巴林左旗',
    `parent_id`   = 150400,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 150422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巴林右旗',
    `parent_id`   = 150400,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 150423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '林西县',
    `parent_id`   = 150400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 150424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '克什克腾旗',
    `parent_id`   = 150400,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 150425;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '翁牛特旗',
    `parent_id`   = 150400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 150426;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '喀喇沁旗',
    `parent_id`   = 150400,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 150428;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁城县',
    `parent_id`   = 150400,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 150429;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '敖汉旗',
    `parent_id`   = 150400,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 150430;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '通辽市',
    `parent_id`   = 150000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 150500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '科尔沁区',
    `parent_id`   = 150500,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 150502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '科尔沁左翼中旗',
    `parent_id`   = 150500,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 150521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '科尔沁左翼后旗',
    `parent_id`   = 150500,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 150522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '开鲁县',
    `parent_id`   = 150500,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 150523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '库伦旗',
    `parent_id`   = 150500,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 150524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '奈曼旗',
    `parent_id`   = 150500,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 150525;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '扎鲁特旗',
    `parent_id`   = 150500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 150526;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '通辽经济技术开发区',
    `parent_id`   = 150500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 150571;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '霍林郭勒市',
    `parent_id`   = 150500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 150581;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '鄂尔多斯市',
    `parent_id`   = 150000,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 150600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东胜区',
    `parent_id`   = 150600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 150602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '康巴什区',
    `parent_id`   = 150600,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 150603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '达拉特旗',
    `parent_id`   = 150600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 150621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '准格尔旗',
    `parent_id`   = 150600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 150622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鄂托克前旗',
    `parent_id`   = 150600,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 150623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鄂托克旗',
    `parent_id`   = 150600,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 150624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '杭锦旗',
    `parent_id`   = 150600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 150625;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌审旗',
    `parent_id`   = 150600,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 150626;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '伊金霍洛旗',
    `parent_id`   = 150600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 150627;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '呼伦贝尔市',
    `parent_id`   = 150000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 150700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海拉尔区',
    `parent_id`   = 150700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 150702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '扎赉诺尔区',
    `parent_id`   = 150700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 150703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿荣旗',
    `parent_id`   = 150700,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 150721;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莫力达瓦达斡尔族自治旗',
    `parent_id`   = 150700,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 150722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鄂伦春自治旗',
    `parent_id`   = 150700,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 150723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鄂温克族自治旗',
    `parent_id`   = 150700,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 150724;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '陈巴尔虎旗',
    `parent_id`   = 150700,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 150725;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新巴尔虎左旗',
    `parent_id`   = 150700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 150726;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新巴尔虎右旗',
    `parent_id`   = 150700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 150727;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '满洲里市',
    `parent_id`   = 150700,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 150781;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '牙克石市',
    `parent_id`   = 150700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 150782;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '扎兰屯市',
    `parent_id`   = 150700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 150783;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '额尔古纳市',
    `parent_id`   = 150700,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 150784;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '根河市',
    `parent_id`   = 150700,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 150785;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '巴彦淖尔市',
    `parent_id`   = 150000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 150800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临河区',
    `parent_id`   = 150800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 150802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五原县',
    `parent_id`   = 150800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 150821;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '磴口县',
    `parent_id`   = 150800,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 150822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌拉特前旗',
    `parent_id`   = 150800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 150823;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌拉特中旗',
    `parent_id`   = 150800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 150824;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌拉特后旗',
    `parent_id`   = 150800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 150825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '杭锦后旗',
    `parent_id`   = 150800,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 150826;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '乌兰察布市',
    `parent_id`   = 150000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 150900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '集宁区',
    `parent_id`   = 150900,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 150902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '卓资县',
    `parent_id`   = 150900,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 150921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '化德县',
    `parent_id`   = 150900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 150922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '商都县',
    `parent_id`   = 150900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 150923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴和县',
    `parent_id`   = 150900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 150924;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凉城县',
    `parent_id`   = 150900,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 150925;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '察哈尔右翼前旗',
    `parent_id`   = 150900,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 150926;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '察哈尔右翼中旗',
    `parent_id`   = 150900,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 150927;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '察哈尔右翼后旗',
    `parent_id`   = 150900,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 150928;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '四子王旗',
    `parent_id`   = 150900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 150929;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丰镇市',
    `parent_id`   = 150900,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 150981;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '兴安盟',
    `parent_id`   = 150000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 152200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌兰浩特市',
    `parent_id`   = 152200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 152201;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿尔山市',
    `parent_id`   = 152200,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 152202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '科尔沁右翼前旗',
    `parent_id`   = 152200,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 152221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '科尔沁右翼中旗',
    `parent_id`   = 152200,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 152222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '扎赉特旗',
    `parent_id`   = 152200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 152223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '突泉县',
    `parent_id`   = 152200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 152224;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '锡林郭勒盟',
    `parent_id`   = 150000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 152500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '二连浩特市',
    `parent_id`   = 152500,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 152501;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '锡林浩特市',
    `parent_id`   = 152500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 152502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿巴嘎旗',
    `parent_id`   = 152500,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 152522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '苏尼特左旗',
    `parent_id`   = 152500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 152523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '苏尼特右旗',
    `parent_id`   = 152500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 152524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东乌珠穆沁旗',
    `parent_id`   = 152500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 152525;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西乌珠穆沁旗',
    `parent_id`   = 152500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 152526;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '太仆寺旗',
    `parent_id`   = 152500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 152527;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '镶黄旗',
    `parent_id`   = 152500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 152528;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '正镶白旗',
    `parent_id`   = 152500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 152529;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '正蓝旗',
    `parent_id`   = 152500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 152530;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '多伦县',
    `parent_id`   = 152500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 152531;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌拉盖管委会',
    `parent_id`   = 152500,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 152571;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '阿拉善盟',
    `parent_id`   = 150000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 152900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿拉善左旗',
    `parent_id`   = 152900,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 152921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿拉善右旗',
    `parent_id`   = 152900,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 152922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '额济纳旗',
    `parent_id`   = 152900,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 152923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '内蒙古阿拉善经济开发区',
    `parent_id`   = 152900,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 152971;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '辽宁省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 210000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '沈阳市',
    `parent_id`   = 210000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 210100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '和平区',
    `parent_id`   = 210100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 210102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沈河区',
    `parent_id`   = 210100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 210103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大东区',
    `parent_id`   = 210100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 210104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '皇姑区',
    `parent_id`   = 210100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 210105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铁西区',
    `parent_id`   = 210100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 210106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '苏家屯区',
    `parent_id`   = 210100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 210111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浑南区',
    `parent_id`   = 210100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 210112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沈北新区',
    `parent_id`   = 210100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 210113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '于洪区',
    `parent_id`   = 210100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 210114;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '辽中区',
    `parent_id`   = 210100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 210115;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '康平县',
    `parent_id`   = 210100,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 210123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '法库县',
    `parent_id`   = 210100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 210124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新民市',
    `parent_id`   = 210100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 210181;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '经济技术开发区',
    `parent_id`   = 210100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 210190;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '大连市',
    `parent_id`   = 210000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 210200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中山区',
    `parent_id`   = 210200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 210202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西岗区',
    `parent_id`   = 210200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 210203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沙河口区',
    `parent_id`   = 210200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 210204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '甘井子区',
    `parent_id`   = 210200,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 210211;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '旅顺口区',
    `parent_id`   = 210200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 210212;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金州区',
    `parent_id`   = 210200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 210213;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '普兰店区',
    `parent_id`   = 210200,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 210214;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长海县',
    `parent_id`   = 210200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 210224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '瓦房店市',
    `parent_id`   = 210200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 210281;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '庄河市',
    `parent_id`   = 210200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 210283;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '鞍山市',
    `parent_id`   = 210000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 210300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铁东区',
    `parent_id`   = 210300,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 210302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铁西区',
    `parent_id`   = 210300,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 210303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '立山区',
    `parent_id`   = 210300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 210304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '千山区',
    `parent_id`   = 210300,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 210311;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '台安县',
    `parent_id`   = 210300,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 210321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岫岩满族自治县',
    `parent_id`   = 210300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 210323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海城市',
    `parent_id`   = 210300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 210381;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高新区',
    `parent_id`   = 210300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 210390;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '抚顺市',
    `parent_id`   = 210000,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 210400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新抚区',
    `parent_id`   = 210400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 210402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东洲区',
    `parent_id`   = 210400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 210403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '望花区',
    `parent_id`   = 210400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 210404;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '顺城区',
    `parent_id`   = 210400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 210411;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '抚顺县',
    `parent_id`   = 210400,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 210421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新宾满族自治县',
    `parent_id`   = 210400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 210422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清原满族自治县',
    `parent_id`   = 210400,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 210423;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '本溪市',
    `parent_id`   = 210000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 210500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平山区',
    `parent_id`   = 210500,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 210502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '溪湖区',
    `parent_id`   = 210500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 210503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '明山区',
    `parent_id`   = 210500,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 210504;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南芬区',
    `parent_id`   = 210500,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 210505;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '本溪满族自治县',
    `parent_id`   = 210500,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 210521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桓仁满族自治县',
    `parent_id`   = 210500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 210522;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '丹东市',
    `parent_id`   = 210000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 210600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '元宝区',
    `parent_id`   = 210600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 210602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '振兴区',
    `parent_id`   = 210600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 210603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '振安区',
    `parent_id`   = 210600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 210604;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宽甸满族自治县',
    `parent_id`   = 210600,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 210624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东港市',
    `parent_id`   = 210600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 210681;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凤城市',
    `parent_id`   = 210600,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 210682;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '锦州市',
    `parent_id`   = 210000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 210700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '古塔区',
    `parent_id`   = 210700,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 210702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凌河区',
    `parent_id`   = 210700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 210703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '太和区',
    `parent_id`   = 210700,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 210711;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黑山县',
    `parent_id`   = 210700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 210726;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '义县',
    `parent_id`   = 210700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 210727;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凌海市',
    `parent_id`   = 210700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 210781;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北镇市',
    `parent_id`   = 210700,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 210782;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '经济技术开发区',
    `parent_id`   = 210700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 210793;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '营口市',
    `parent_id`   = 210000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 210800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '站前区',
    `parent_id`   = 210800,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 210802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西市区',
    `parent_id`   = 210800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 210803;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鲅鱼圈区',
    `parent_id`   = 210800,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 210804;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '老边区',
    `parent_id`   = 210800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 210811;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盖州市',
    `parent_id`   = 210800,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 210881;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大石桥市',
    `parent_id`   = 210800,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 210882;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '阜新市',
    `parent_id`   = 210000,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 210900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海州区',
    `parent_id`   = 210900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 210902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新邱区',
    `parent_id`   = 210900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 210903;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '太平区',
    `parent_id`   = 210900,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 210904;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清河门区',
    `parent_id`   = 210900,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 210905;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '细河区',
    `parent_id`   = 210900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 210911;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阜新蒙古族自治县',
    `parent_id`   = 210900,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 210921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '彰武县',
    `parent_id`   = 210900,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 210922;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '辽阳市',
    `parent_id`   = 210000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 211000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '白塔区',
    `parent_id`   = 211000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 211002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '文圣区',
    `parent_id`   = 211000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 211003;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宏伟区',
    `parent_id`   = 211000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 211004;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '弓长岭区',
    `parent_id`   = 211000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 211005;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '太子河区',
    `parent_id`   = 211000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 211011;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '辽阳县',
    `parent_id`   = 211000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 211021;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '灯塔市',
    `parent_id`   = 211000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 211081;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '盘锦市',
    `parent_id`   = 210000,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 211100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '双台子区',
    `parent_id`   = 211100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 211102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴隆台区',
    `parent_id`   = 211100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 211103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大洼区',
    `parent_id`   = 211100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 211104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盘山县',
    `parent_id`   = 211100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 211122;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '铁岭市',
    `parent_id`   = 210000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 211200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '银州区',
    `parent_id`   = 211200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 211202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清河区',
    `parent_id`   = 211200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 211204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铁岭县',
    `parent_id`   = 211200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 211221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西丰县',
    `parent_id`   = 211200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 211223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昌图县',
    `parent_id`   = 211200,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 211224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '调兵山市',
    `parent_id`   = 211200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 211281;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '开原市',
    `parent_id`   = 211200,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 211282;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '朝阳市',
    `parent_id`   = 210000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 211300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '双塔区',
    `parent_id`   = 211300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 211302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙城区',
    `parent_id`   = 211300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 211303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '朝阳县',
    `parent_id`   = 211300,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 211321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '建平县',
    `parent_id`   = 211300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 211322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '喀喇沁左翼蒙古族自治县',
    `parent_id`   = 211300,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 211324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北票市',
    `parent_id`   = 211300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 211381;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凌源市',
    `parent_id`   = 211300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 211382;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '葫芦岛市',
    `parent_id`   = 210000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 211400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '连山区',
    `parent_id`   = 211400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 211402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙港区',
    `parent_id`   = 211400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 211403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南票区',
    `parent_id`   = 211400,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 211404;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '绥中县',
    `parent_id`   = 211400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 211421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '建昌县',
    `parent_id`   = 211400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 211422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴城市',
    `parent_id`   = 211400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 211481;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '吉林省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 220000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '长春市',
    `parent_id`   = 220000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 220100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南关区',
    `parent_id`   = 220100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 220102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宽城区',
    `parent_id`   = 220100,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 220103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '朝阳区',
    `parent_id`   = 220100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 220104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '二道区',
    `parent_id`   = 220100,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 220105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '绿园区',
    `parent_id`   = 220100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 220106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '双阳区',
    `parent_id`   = 220100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 220112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '九台区',
    `parent_id`   = 220100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 220113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '农安县',
    `parent_id`   = 220100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 220122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长春经济技术开发区',
    `parent_id`   = 220100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 220171;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长春净月高新技术产业开发区',
    `parent_id`   = 220100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 220172;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长春高新技术产业开发区',
    `parent_id`   = 220100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 220173;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长春汽车经济技术开发区',
    `parent_id`   = 220100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 220174;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '榆树市',
    `parent_id`   = 220100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 220182;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '德惠市',
    `parent_id`   = 220100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 220183;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '公主岭市',
    `parent_id`   = 220100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 220184;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '经济技术开发区',
    `parent_id`   = 220100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 220192;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '吉林市',
    `parent_id`   = 220000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 220200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昌邑区',
    `parent_id`   = 220200,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 220202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙潭区',
    `parent_id`   = 220200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 220203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '船营区',
    `parent_id`   = 220200,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 220204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丰满区',
    `parent_id`   = 220200,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 220211;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永吉县',
    `parent_id`   = 220200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 220221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吉林经济开发区',
    `parent_id`   = 220200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 220271;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吉林高新技术产业开发区',
    `parent_id`   = 220200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 220272;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蛟河市',
    `parent_id`   = 220200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 220281;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桦甸市',
    `parent_id`   = 220200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 220282;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '舒兰市',
    `parent_id`   = 220200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 220283;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '磐石市',
    `parent_id`   = 220200,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 220284;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '四平市',
    `parent_id`   = 220000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 220300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铁西区',
    `parent_id`   = 220300,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 220302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铁东区',
    `parent_id`   = 220300,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 220303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '梨树县',
    `parent_id`   = 220300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 220322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '伊通满族自治县',
    `parent_id`   = 220300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 220323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '双辽市',
    `parent_id`   = 220300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 220382;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '辽源市',
    `parent_id`   = 220000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 220400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙山区',
    `parent_id`   = 220400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 220402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西安区',
    `parent_id`   = 220400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 220403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东丰县',
    `parent_id`   = 220400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 220421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东辽县',
    `parent_id`   = 220400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 220422;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '通化市',
    `parent_id`   = 220000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 220500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东昌区',
    `parent_id`   = 220500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 220502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '二道江区',
    `parent_id`   = 220500,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 220503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '通化县',
    `parent_id`   = 220500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 220521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '辉南县',
    `parent_id`   = 220500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 220523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '柳河县',
    `parent_id`   = 220500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 220524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '梅河口市',
    `parent_id`   = 220500,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 220581;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '集安市',
    `parent_id`   = 220500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 220582;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '白山市',
    `parent_id`   = 220000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 220600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浑江区',
    `parent_id`   = 220600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 220602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江源区',
    `parent_id`   = 220600,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 220605;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '抚松县',
    `parent_id`   = 220600,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 220621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '靖宇县',
    `parent_id`   = 220600,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 220622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长白朝鲜族自治县',
    `parent_id`   = 220600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 220623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临江市',
    `parent_id`   = 220600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 220681;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '松原市',
    `parent_id`   = 220000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 220700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁江区',
    `parent_id`   = 220700,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 220702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '前郭尔罗斯蒙古族自治县',
    `parent_id`   = 220700,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 220721;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长岭县',
    `parent_id`   = 220700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 220722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乾安县',
    `parent_id`   = 220700,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 220723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吉林松原经济开发区',
    `parent_id`   = 220700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 220771;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '扶余市',
    `parent_id`   = 220700,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 220781;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '白城市',
    `parent_id`   = 220000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 220800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洮北区',
    `parent_id`   = 220800,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 220802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '镇赉县',
    `parent_id`   = 220800,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 220821;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '通榆县',
    `parent_id`   = 220800,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 220822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吉林白城经济开发区',
    `parent_id`   = 220800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 220871;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洮南市',
    `parent_id`   = 220800,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 220881;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大安市',
    `parent_id`   = 220800,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 220882;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '延边朝鲜族自治州',
    `parent_id`   = 220000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 222400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '延吉市',
    `parent_id`   = 222400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 222401;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '图们市',
    `parent_id`   = 222400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 222402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '敦化市',
    `parent_id`   = 222400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 222403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '珲春市',
    `parent_id`   = 222400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 222404;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙井市',
    `parent_id`   = 222400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 222405;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '和龙市',
    `parent_id`   = 222400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 222406;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汪清县',
    `parent_id`   = 222400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 222424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安图县',
    `parent_id`   = 222400,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 222426;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '黑龙江省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 230000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '哈尔滨市',
    `parent_id`   = 230000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 230100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '道里区',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 230102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南岗区',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 230103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '道外区',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 230104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平房区',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 230108;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '松北区',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 230109;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '香坊区',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 230110;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '呼兰区',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 230111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿城区',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 230112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '双城区',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 230113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '依兰县',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 230123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '方正县',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 230124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宾县',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 230125;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巴彦县',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 230126;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '木兰县',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 230127;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '通河县',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 230128;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '延寿县',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 230129;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '尚志市',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 230183;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五常市',
    `parent_id`   = 230100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 230184;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '齐齐哈尔市',
    `parent_id`   = 230000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 230200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙沙区',
    `parent_id`   = 230200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 230202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '建华区',
    `parent_id`   = 230200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 230203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铁锋区',
    `parent_id`   = 230200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 230204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昂昂溪区',
    `parent_id`   = 230200,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 230205;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '富拉尔基区',
    `parent_id`   = 230200,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 230206;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '碾子山区',
    `parent_id`   = 230200,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 230207;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '梅里斯达斡尔族区',
    `parent_id`   = 230200,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 230208;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙江县',
    `parent_id`   = 230200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 230221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '依安县',
    `parent_id`   = 230200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 230223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泰来县',
    `parent_id`   = 230200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 230224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '甘南县',
    `parent_id`   = 230200,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 230225;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '富裕县',
    `parent_id`   = 230200,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 230227;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '克山县',
    `parent_id`   = 230200,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 230229;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '克东县',
    `parent_id`   = 230200,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 230230;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '拜泉县',
    `parent_id`   = 230200,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 230231;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '讷河市',
    `parent_id`   = 230200,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 230281;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '鸡西市',
    `parent_id`   = 230000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 230300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鸡冠区',
    `parent_id`   = 230300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 230302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '恒山区',
    `parent_id`   = 230300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 230303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '滴道区',
    `parent_id`   = 230300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 230304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '梨树区',
    `parent_id`   = 230300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 230305;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '城子河区',
    `parent_id`   = 230300,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 230306;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '麻山区',
    `parent_id`   = 230300,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 230307;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鸡东县',
    `parent_id`   = 230300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 230321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '虎林市',
    `parent_id`   = 230300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 230381;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '密山市',
    `parent_id`   = 230300,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 230382;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '鹤岗市',
    `parent_id`   = 230000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 230400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '向阳区',
    `parent_id`   = 230400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 230402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '工农区',
    `parent_id`   = 230400,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 230403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南山区',
    `parent_id`   = 230400,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 230404;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴安区',
    `parent_id`   = 230400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 230405;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东山区',
    `parent_id`   = 230400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 230406;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴山区',
    `parent_id`   = 230400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 230407;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '萝北县',
    `parent_id`   = 230400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 230421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '绥滨县',
    `parent_id`   = 230400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 230422;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '双鸭山市',
    `parent_id`   = 230000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 230500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '尖山区',
    `parent_id`   = 230500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 230502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岭东区',
    `parent_id`   = 230500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 230503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '四方台区',
    `parent_id`   = 230500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 230505;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宝山区',
    `parent_id`   = 230500,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 230506;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '集贤县',
    `parent_id`   = 230500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 230521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '友谊县',
    `parent_id`   = 230500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 230522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宝清县',
    `parent_id`   = 230500,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 230523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '饶河县',
    `parent_id`   = 230500,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 230524;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '大庆市',
    `parent_id`   = 230000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 230600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '萨尔图区',
    `parent_id`   = 230600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 230602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙凤区',
    `parent_id`   = 230600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 230603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '让胡路区',
    `parent_id`   = 230600,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 230604;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '红岗区',
    `parent_id`   = 230600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 230605;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大同区',
    `parent_id`   = 230600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 230606;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '肇州县',
    `parent_id`   = 230600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 230621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '肇源县',
    `parent_id`   = 230600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 230622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '林甸县',
    `parent_id`   = 230600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 230623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '杜尔伯特蒙古族自治县',
    `parent_id`   = 230600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 230624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大庆高新技术产业开发区',
    `parent_id`   = 230600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 230671;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '伊春市',
    `parent_id`   = 230000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 230700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '伊美区',
    `parent_id`   = 230700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 230717;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌翠区',
    `parent_id`   = 230700,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 230718;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '友好区',
    `parent_id`   = 230700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 230719;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '嘉荫县',
    `parent_id`   = 230700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 230722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汤旺县',
    `parent_id`   = 230700,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 230723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丰林县',
    `parent_id`   = 230700,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 230724;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大箐山县',
    `parent_id`   = 230700,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 230725;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南岔县',
    `parent_id`   = 230700,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 230726;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金林区',
    `parent_id`   = 230700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 230751;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铁力市',
    `parent_id`   = 230700,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 230781;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '佳木斯市',
    `parent_id`   = 230000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 230800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '向阳区',
    `parent_id`   = 230800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 230803;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '前进区',
    `parent_id`   = 230800,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 230804;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东风区',
    `parent_id`   = 230800,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 230805;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '郊区',
    `parent_id`   = 230800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 230811;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桦南县',
    `parent_id`   = 230800,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 230822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桦川县',
    `parent_id`   = 230800,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 230826;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汤原县',
    `parent_id`   = 230800,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 230828;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '同江市',
    `parent_id`   = 230800,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 230881;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '富锦市',
    `parent_id`   = 230800,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 230882;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '抚远市',
    `parent_id`   = 230800,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 230883;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '七台河市',
    `parent_id`   = 230000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 230900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新兴区',
    `parent_id`   = 230900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 230902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桃山区',
    `parent_id`   = 230900,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 230903;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '茄子河区',
    `parent_id`   = 230900,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 230904;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '勃利县',
    `parent_id`   = 230900,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 230921;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '牡丹江市',
    `parent_id`   = 230000,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 231000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东安区',
    `parent_id`   = 231000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 231002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阳明区',
    `parent_id`   = 231000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 231003;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '爱民区',
    `parent_id`   = 231000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 231004;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西安区',
    `parent_id`   = 231000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 231005;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '林口县',
    `parent_id`   = 231000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 231025;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '绥芬河市',
    `parent_id`   = 231000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 231081;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海林市',
    `parent_id`   = 231000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 231083;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁安市',
    `parent_id`   = 231000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 231084;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '穆棱市',
    `parent_id`   = 231000,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 231085;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东宁市',
    `parent_id`   = 231000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 231086;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '黑河市',
    `parent_id`   = 230000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 231100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '爱辉区',
    `parent_id`   = 231100,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 231102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '逊克县',
    `parent_id`   = 231100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 231123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '孙吴县',
    `parent_id`   = 231100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 231124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北安市',
    `parent_id`   = 231100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 231181;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五大连池市',
    `parent_id`   = 231100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 231182;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '嫩江市',
    `parent_id`   = 231100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 231183;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '绥化市',
    `parent_id`   = 230000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 231200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北林区',
    `parent_id`   = 231200,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 231202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '望奎县',
    `parent_id`   = 231200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 231221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兰西县',
    `parent_id`   = 231200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 231222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青冈县',
    `parent_id`   = 231200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 231223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '庆安县',
    `parent_id`   = 231200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 231224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '明水县',
    `parent_id`   = 231200,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 231225;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '绥棱县',
    `parent_id`   = 231200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 231226;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安达市',
    `parent_id`   = 231200,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 231281;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '肇东市',
    `parent_id`   = 231200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 231282;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海伦市',
    `parent_id`   = 231200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 231283;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '大兴安岭地区',
    `parent_id`   = 230000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 232700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '漠河市',
    `parent_id`   = 232700,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 232701;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '呼玛县',
    `parent_id`   = 232700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 232721;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '塔河县',
    `parent_id`   = 232700,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 232722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '松岭区',
    `parent_id`   = 232700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 232790;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '呼中区',
    `parent_id`   = 232700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 232791;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '加格达奇区',
    `parent_id`   = 232700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 232792;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新林区',
    `parent_id`   = 232700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 232793;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '上海市',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 310000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '上海市',
    `parent_id`   = 310000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 310100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黄浦区',
    `parent_id`   = 310100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 310101;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '徐汇区',
    `parent_id`   = 310100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 310104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长宁区',
    `parent_id`   = 310100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 310105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '静安区',
    `parent_id`   = 310100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 310106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '普陀区',
    `parent_id`   = 310100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 310107;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '虹口区',
    `parent_id`   = 310100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 310109;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '杨浦区',
    `parent_id`   = 310100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 310110;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '闵行区',
    `parent_id`   = 310100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 310112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宝山区',
    `parent_id`   = 310100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 310113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '嘉定区',
    `parent_id`   = 310100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 310114;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浦东新区',
    `parent_id`   = 310100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 310115;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金山区',
    `parent_id`   = 310100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 310116;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '松江区',
    `parent_id`   = 310100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 310117;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青浦区',
    `parent_id`   = 310100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 310118;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '奉贤区',
    `parent_id`   = 310100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 310120;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '崇明区',
    `parent_id`   = 310100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 310151;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '江苏省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 320000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '南京市',
    `parent_id`   = 320000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 320100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玄武区',
    `parent_id`   = 320100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 320102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '秦淮区',
    `parent_id`   = 320100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 320104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '建邺区',
    `parent_id`   = 320100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 320105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鼓楼区',
    `parent_id`   = 320100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 320106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浦口区',
    `parent_id`   = 320100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 320111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '栖霞区',
    `parent_id`   = 320100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 320113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雨花台区',
    `parent_id`   = 320100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 320114;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江宁区',
    `parent_id`   = 320100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 320115;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '六合区',
    `parent_id`   = 320100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 320116;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '溧水区',
    `parent_id`   = 320100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 320117;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高淳区',
    `parent_id`   = 320100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 320118;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '无锡市',
    `parent_id`   = 320000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 320200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '锡山区',
    `parent_id`   = 320200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 320205;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '惠山区',
    `parent_id`   = 320200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 320206;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '滨湖区',
    `parent_id`   = 320200,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 320211;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '梁溪区',
    `parent_id`   = 320200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 320213;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新吴区',
    `parent_id`   = 320200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 320214;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江阴市',
    `parent_id`   = 320200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 320281;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宜兴市',
    `parent_id`   = 320200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 320282;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '徐州市',
    `parent_id`   = 320000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 320300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鼓楼区',
    `parent_id`   = 320300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 320302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '云龙区',
    `parent_id`   = 320300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 320303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '贾汪区',
    `parent_id`   = 320300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 320305;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泉山区',
    `parent_id`   = 320300,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 320311;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铜山区',
    `parent_id`   = 320300,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 320312;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丰县',
    `parent_id`   = 320300,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 320321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沛县',
    `parent_id`   = 320300,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 320322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '睢宁县',
    `parent_id`   = 320300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 320324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '徐州经济技术开发区',
    `parent_id`   = 320300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 320371;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新沂市',
    `parent_id`   = 320300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 320381;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '邳州市',
    `parent_id`   = 320300,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 320382;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '工业园区',
    `parent_id`   = 320300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 320391;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '常州市',
    `parent_id`   = 320000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 320400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天宁区',
    `parent_id`   = 320400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 320402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '钟楼区',
    `parent_id`   = 320400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 320404;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新北区',
    `parent_id`   = 320400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 320411;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武进区',
    `parent_id`   = 320400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 320412;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金坛区',
    `parent_id`   = 320400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 320413;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '溧阳市',
    `parent_id`   = 320400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 320481;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '苏州市',
    `parent_id`   = 320000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 320500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '虎丘区',
    `parent_id`   = 320500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 320505;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吴中区',
    `parent_id`   = 320500,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 320506;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '相城区',
    `parent_id`   = 320500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 320507;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '姑苏区',
    `parent_id`   = 320500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 320508;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吴江区',
    `parent_id`   = 320500,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 320509;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '苏州工业园区',
    `parent_id`   = 320500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 320571;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '常熟市',
    `parent_id`   = 320500,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 320581;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '张家港市',
    `parent_id`   = 320500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 320582;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昆山市',
    `parent_id`   = 320500,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 320583;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '太仓市',
    `parent_id`   = 320500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 320585;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '工业园区',
    `parent_id`   = 320500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 320590;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高新区',
    `parent_id`   = 320500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 320591;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '南通市',
    `parent_id`   = 320000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 320600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '通州区',
    `parent_id`   = 320600,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 320612;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '崇川区',
    `parent_id`   = 320600,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 320613;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海门区',
    `parent_id`   = 320600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 320614;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '如东县',
    `parent_id`   = 320600,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 320623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '启东市',
    `parent_id`   = 320600,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 320681;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '如皋市',
    `parent_id`   = 320600,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 320682;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海安市',
    `parent_id`   = 320600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 320685;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高新区',
    `parent_id`   = 320600,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 320691;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '连云港市',
    `parent_id`   = 320000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 320700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '连云区',
    `parent_id`   = 320700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 320703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海州区',
    `parent_id`   = 320700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 320706;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '赣榆区',
    `parent_id`   = 320700,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 320707;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东海县',
    `parent_id`   = 320700,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 320722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '灌云县',
    `parent_id`   = 320700,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 320723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '灌南县',
    `parent_id`   = 320700,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 320724;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '连云港经济技术开发区',
    `parent_id`   = 320700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 320771;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '淮安市',
    `parent_id`   = 320000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 320800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '淮安区',
    `parent_id`   = 320800,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 320803;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '淮阴区',
    `parent_id`   = 320800,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 320804;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清江浦区',
    `parent_id`   = 320800,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 320812;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洪泽区',
    `parent_id`   = 320800,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 320813;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '涟水县',
    `parent_id`   = 320800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 320826;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盱眙县',
    `parent_id`   = 320800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 320830;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金湖县',
    `parent_id`   = 320800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 320831;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '盐城市',
    `parent_id`   = 320000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 320900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '亭湖区',
    `parent_id`   = 320900,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 320902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盐都区',
    `parent_id`   = 320900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 320903;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大丰区',
    `parent_id`   = 320900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 320904;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '响水县',
    `parent_id`   = 320900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 320921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '滨海县',
    `parent_id`   = 320900,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 320922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阜宁县',
    `parent_id`   = 320900,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 320923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '射阳县',
    `parent_id`   = 320900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 320924;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '建湖县',
    `parent_id`   = 320900,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 320925;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盐城经济技术开发区',
    `parent_id`   = 320900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 320971;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东台市',
    `parent_id`   = 320900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 320981;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '扬州市',
    `parent_id`   = 320000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 321000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '广陵区',
    `parent_id`   = 321000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 321002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '邗江区',
    `parent_id`   = 321000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 321003;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江都区',
    `parent_id`   = 321000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 321012;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宝应县',
    `parent_id`   = 321000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 321023;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '扬州经济技术开发区',
    `parent_id`   = 321000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 321071;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仪征市',
    `parent_id`   = 321000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 321081;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高邮市',
    `parent_id`   = 321000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 321084;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '经济开发区',
    `parent_id`   = 321000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 321090;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '镇江市',
    `parent_id`   = 320000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 321100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '京口区',
    `parent_id`   = 321100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 321102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '润州区',
    `parent_id`   = 321100,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 321111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丹徒区',
    `parent_id`   = 321100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 321112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '镇江新区',
    `parent_id`   = 321100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 321150;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丹阳市',
    `parent_id`   = 321100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 321181;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '扬中市',
    `parent_id`   = 321100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 321182;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '句容市',
    `parent_id`   = 321100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 321183;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '泰州市',
    `parent_id`   = 320000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 321200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海陵区',
    `parent_id`   = 321200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 321202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高港区',
    `parent_id`   = 321200,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 321203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '姜堰区',
    `parent_id`   = 321200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 321204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泰州医药高新技术产业开发区',
    `parent_id`   = 321200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 321271;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴化市',
    `parent_id`   = 321200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 321281;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '靖江市',
    `parent_id`   = 321200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 321282;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泰兴市',
    `parent_id`   = 321200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 321283;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '宿迁市',
    `parent_id`   = 320000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 321300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宿城区',
    `parent_id`   = 321300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 321302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宿豫区',
    `parent_id`   = 321300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 321311;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沭阳县',
    `parent_id`   = 321300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 321322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泗阳县',
    `parent_id`   = 321300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 321323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泗洪县',
    `parent_id`   = 321300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 321324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宿迁经济技术开发区',
    `parent_id`   = 321300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 321371;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '浙江省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 330000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '杭州市',
    `parent_id`   = 330000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 330100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '上城区',
    `parent_id`   = 330100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 330102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '拱墅区',
    `parent_id`   = 330100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 330105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西湖区',
    `parent_id`   = 330100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 330106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '滨江区',
    `parent_id`   = 330100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 330108;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '萧山区',
    `parent_id`   = 330100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 330109;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '余杭区',
    `parent_id`   = 330100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 330110;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '富阳区',
    `parent_id`   = 330100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 330111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临安区',
    `parent_id`   = 330100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 330112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临平区',
    `parent_id`   = 330100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 330113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '钱塘区',
    `parent_id`   = 330100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 330114;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桐庐县',
    `parent_id`   = 330100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 330122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '淳安县',
    `parent_id`   = 330100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 330127;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '建德市',
    `parent_id`   = 330100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 330182;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '宁波市',
    `parent_id`   = 330000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 330200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海曙区',
    `parent_id`   = 330200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 330203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江北区',
    `parent_id`   = 330200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 330205;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北仑区',
    `parent_id`   = 330200,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 330206;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '镇海区',
    `parent_id`   = 330200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 330211;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鄞州区',
    `parent_id`   = 330200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 330212;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '奉化区',
    `parent_id`   = 330200,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 330213;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '象山县',
    `parent_id`   = 330200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 330225;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁海县',
    `parent_id`   = 330200,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 330226;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '余姚市',
    `parent_id`   = 330200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 330281;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '慈溪市',
    `parent_id`   = 330200,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 330282;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '温州市',
    `parent_id`   = 330000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 330300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鹿城区',
    `parent_id`   = 330300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 330302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙湾区',
    `parent_id`   = 330300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 330303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '瓯海区',
    `parent_id`   = 330300,
    `is_hot`      = 0,
    `first_word`  = 'O'
WHERE `region_id` = 330304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洞头区',
    `parent_id`   = 330300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 330305;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永嘉县',
    `parent_id`   = 330300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 330324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平阳县',
    `parent_id`   = 330300,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 330326;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '苍南县',
    `parent_id`   = 330300,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 330327;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '文成县',
    `parent_id`   = 330300,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 330328;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泰顺县',
    `parent_id`   = 330300,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 330329;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '瑞安市',
    `parent_id`   = 330300,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 330381;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乐清市',
    `parent_id`   = 330300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 330382;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙港市',
    `parent_id`   = 330300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 330383;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '嘉兴市',
    `parent_id`   = 330000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 330400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南湖区',
    `parent_id`   = 330400,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 330402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '秀洲区',
    `parent_id`   = 330400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 330411;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '嘉善县',
    `parent_id`   = 330400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 330421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海盐县',
    `parent_id`   = 330400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 330424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海宁市',
    `parent_id`   = 330400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 330481;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平湖市',
    `parent_id`   = 330400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 330482;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桐乡市',
    `parent_id`   = 330400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 330483;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '湖州市',
    `parent_id`   = 330000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 330500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吴兴区',
    `parent_id`   = 330500,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 330502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南浔区',
    `parent_id`   = 330500,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 330503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '德清县',
    `parent_id`   = 330500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 330521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长兴县',
    `parent_id`   = 330500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 330522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安吉县',
    `parent_id`   = 330500,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 330523;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '绍兴市',
    `parent_id`   = 330000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 330600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '越城区',
    `parent_id`   = 330600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 330602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '柯桥区',
    `parent_id`   = 330600,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 330603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '上虞区',
    `parent_id`   = 330600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 330604;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新昌县',
    `parent_id`   = 330600,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 330624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '诸暨市',
    `parent_id`   = 330600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 330681;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '嵊州市',
    `parent_id`   = 330600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 330683;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '金华市',
    `parent_id`   = 330000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 330700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '婺城区',
    `parent_id`   = 330700,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 330702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金东区',
    `parent_id`   = 330700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 330703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武义县',
    `parent_id`   = 330700,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 330723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浦江县',
    `parent_id`   = 330700,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 330726;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '磐安县',
    `parent_id`   = 330700,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 330727;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兰溪市',
    `parent_id`   = 330700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 330781;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '义乌市',
    `parent_id`   = 330700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 330782;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东阳市',
    `parent_id`   = 330700,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 330783;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永康市',
    `parent_id`   = 330700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 330784;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '衢州市',
    `parent_id`   = 330000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 330800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '柯城区',
    `parent_id`   = 330800,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 330802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '衢江区',
    `parent_id`   = 330800,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 330803;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '常山县',
    `parent_id`   = 330800,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 330822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '开化县',
    `parent_id`   = 330800,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 330824;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙游县',
    `parent_id`   = 330800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 330825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江山市',
    `parent_id`   = 330800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 330881;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '舟山市',
    `parent_id`   = 330000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 330900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '定海区',
    `parent_id`   = 330900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 330902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '普陀区',
    `parent_id`   = 330900,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 330903;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岱山县',
    `parent_id`   = 330900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 330921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '嵊泗县',
    `parent_id`   = 330900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 330922;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '台州市',
    `parent_id`   = 330000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 331000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '椒江区',
    `parent_id`   = 331000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 331002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黄岩区',
    `parent_id`   = 331000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 331003;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '路桥区',
    `parent_id`   = 331000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 331004;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三门县',
    `parent_id`   = 331000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 331022;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天台县',
    `parent_id`   = 331000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 331023;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仙居县',
    `parent_id`   = 331000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 331024;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '温岭市',
    `parent_id`   = 331000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 331081;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临海市',
    `parent_id`   = 331000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 331082;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玉环市',
    `parent_id`   = 331000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 331083;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '丽水市',
    `parent_id`   = 330000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 331100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莲都区',
    `parent_id`   = 331100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 331102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青田县',
    `parent_id`   = 331100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 331121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '缙云县',
    `parent_id`   = 331100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 331122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '遂昌县',
    `parent_id`   = 331100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 331123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '松阳县',
    `parent_id`   = 331100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 331124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '云和县',
    `parent_id`   = 331100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 331125;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '庆元县',
    `parent_id`   = 331100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 331126;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '景宁畲族自治县',
    `parent_id`   = 331100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 331127;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙泉市',
    `parent_id`   = 331100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 331181;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '安徽省',
    `parent_id`   = 1,
    `is_hot`      = 1,
    `first_word`  = 'A'
WHERE `region_id` = 340000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '合肥市',
    `parent_id`   = 340000,
    `is_hot`      = 1,
    `first_word`  = 'H'
WHERE `region_id` = 340100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '瑶海区',
    `parent_id`   = 340100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 340102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '庐阳区',
    `parent_id`   = 340100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 340103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蜀山区',
    `parent_id`   = 340100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 340104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '包河区',
    `parent_id`   = 340100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 340111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长丰县',
    `parent_id`   = 340100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 340121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '肥东县',
    `parent_id`   = 340100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 340122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '肥西县',
    `parent_id`   = 340100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 340123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '庐江县',
    `parent_id`   = 340100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 340124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '合肥高新技术产业开发区',
    `parent_id`   = 340100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 340171;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '合肥经济技术开发区',
    `parent_id`   = 340100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 340172;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '合肥新站高新技术产业开发区',
    `parent_id`   = 340100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 340173;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巢湖市',
    `parent_id`   = 340100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 340181;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高新技术开发区',
    `parent_id`   = 340100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 340190;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '经济技术开发区',
    `parent_id`   = 340100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 340191;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '芜湖市',
    `parent_id`   = 340000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 340200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '镜湖区',
    `parent_id`   = 340200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 340202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鸠江区',
    `parent_id`   = 340200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 340207;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '弋江区',
    `parent_id`   = 340200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 340209;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湾沚区',
    `parent_id`   = 340200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 340210;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '繁昌区',
    `parent_id`   = 340200,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 340212;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南陵县',
    `parent_id`   = 340200,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 340223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '无为市',
    `parent_id`   = 340200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 340281;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '蚌埠市',
    `parent_id`   = 340000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 340300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙子湖区',
    `parent_id`   = 340300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 340302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蚌山区',
    `parent_id`   = 340300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 340303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '禹会区',
    `parent_id`   = 340300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 340304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '淮上区',
    `parent_id`   = 340300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 340311;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '怀远县',
    `parent_id`   = 340300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 340321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五河县',
    `parent_id`   = 340300,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 340322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '固镇县',
    `parent_id`   = 340300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 340323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蚌埠市高新技术开发区',
    `parent_id`   = 340300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 340371;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蚌埠市经济开发区',
    `parent_id`   = 340300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 340372;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '淮南市',
    `parent_id`   = 340000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 340400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大通区',
    `parent_id`   = 340400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 340402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '田家庵区',
    `parent_id`   = 340400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 340403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '谢家集区',
    `parent_id`   = 340400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 340404;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '八公山区',
    `parent_id`   = 340400,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 340405;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '潘集区',
    `parent_id`   = 340400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 340406;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凤台县',
    `parent_id`   = 340400,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 340421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '寿县',
    `parent_id`   = 340400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 340422;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '马鞍山市',
    `parent_id`   = 340000,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 340500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '花山区',
    `parent_id`   = 340500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 340503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雨山区',
    `parent_id`   = 340500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 340504;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '博望区',
    `parent_id`   = 340500,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 340506;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '当涂县',
    `parent_id`   = 340500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 340521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '含山县',
    `parent_id`   = 340500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 340522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '和县',
    `parent_id`   = 340500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 340523;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '淮北市',
    `parent_id`   = 340000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 340600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '杜集区',
    `parent_id`   = 340600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 340602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '相山区',
    `parent_id`   = 340600,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 340603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '烈山区',
    `parent_id`   = 340600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 340604;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '濉溪县',
    `parent_id`   = 340600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 340621;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '铜陵市',
    `parent_id`   = 340000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 340700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铜官区',
    `parent_id`   = 340700,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 340705;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '义安区',
    `parent_id`   = 340700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 340706;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '郊区',
    `parent_id`   = 340700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 340711;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '枞阳县',
    `parent_id`   = 340700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 340722;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '安庆市',
    `parent_id`   = 340000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 340800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '迎江区',
    `parent_id`   = 340800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 340802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大观区',
    `parent_id`   = 340800,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 340803;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宜秀区',
    `parent_id`   = 340800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 340811;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '怀宁县',
    `parent_id`   = 340800,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 340822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '太湖县',
    `parent_id`   = 340800,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 340825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宿松县',
    `parent_id`   = 340800,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 340826;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '望江县',
    `parent_id`   = 340800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 340827;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岳西县',
    `parent_id`   = 340800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 340828;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桐城市',
    `parent_id`   = 340800,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 340881;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '潜山市',
    `parent_id`   = 340800,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 340882;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '黄山市',
    `parent_id`   = 340000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 341000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '屯溪区',
    `parent_id`   = 341000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 341002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黄山区',
    `parent_id`   = 341000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 341003;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '徽州区',
    `parent_id`   = 341000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 341004;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '歙县',
    `parent_id`   = 341000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 341021;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '休宁县',
    `parent_id`   = 341000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 341022;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黟县',
    `parent_id`   = 341000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 341023;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '祁门县',
    `parent_id`   = 341000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 341024;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '滁州市',
    `parent_id`   = 340000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 341100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '琅琊区',
    `parent_id`   = 341100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 341102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南谯区',
    `parent_id`   = 341100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 341103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '来安县',
    `parent_id`   = 341100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 341122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '全椒县',
    `parent_id`   = 341100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 341124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '定远县',
    `parent_id`   = 341100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 341125;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凤阳县',
    `parent_id`   = 341100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 341126;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天长市',
    `parent_id`   = 341100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 341181;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '明光市',
    `parent_id`   = 341100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 341182;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '阜阳市',
    `parent_id`   = 340000,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 341200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '颍州区',
    `parent_id`   = 341200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 341202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '颍东区',
    `parent_id`   = 341200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 341203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '颍泉区',
    `parent_id`   = 341200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 341204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临泉县',
    `parent_id`   = 341200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 341221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '太和县',
    `parent_id`   = 341200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 341222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阜南县',
    `parent_id`   = 341200,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 341225;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '颍上县',
    `parent_id`   = 341200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 341226;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阜阳合肥现代产业园区',
    `parent_id`   = 341200,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 341271;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '界首市',
    `parent_id`   = 341200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 341282;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '宿州市',
    `parent_id`   = 340000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 341300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '埇桥区',
    `parent_id`   = 341300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 341302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '砀山县',
    `parent_id`   = 341300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 341321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '萧县',
    `parent_id`   = 341300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 341322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '灵璧县',
    `parent_id`   = 341300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 341323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泗县',
    `parent_id`   = 341300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 341324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宿州马鞍山现代产业园区',
    `parent_id`   = 341300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 341371;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宿州经济技术开发区',
    `parent_id`   = 341300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 341372;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '经济开发区',
    `parent_id`   = 341300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 341390;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '六安市',
    `parent_id`   = 340000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 341500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金安区',
    `parent_id`   = 341500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 341502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '裕安区',
    `parent_id`   = 341500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 341503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '叶集区',
    `parent_id`   = 341500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 341504;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '霍邱县',
    `parent_id`   = 341500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 341522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '舒城县',
    `parent_id`   = 341500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 341523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金寨县',
    `parent_id`   = 341500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 341524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '霍山县',
    `parent_id`   = 341500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 341525;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '亳州市',
    `parent_id`   = 340000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 341600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '谯城区',
    `parent_id`   = 341600,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 341602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '涡阳县',
    `parent_id`   = 341600,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 341621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蒙城县',
    `parent_id`   = 341600,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 341622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '利辛县',
    `parent_id`   = 341600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 341623;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '池州市',
    `parent_id`   = 340000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 341700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '贵池区',
    `parent_id`   = 341700,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 341702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东至县',
    `parent_id`   = 341700,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 341721;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石台县',
    `parent_id`   = 341700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 341722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青阳县',
    `parent_id`   = 341700,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 341723;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '宣城市',
    `parent_id`   = 340000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 341800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宣州区',
    `parent_id`   = 341800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 341802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '郎溪县',
    `parent_id`   = 341800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 341821;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泾县',
    `parent_id`   = 341800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 341823;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '绩溪县',
    `parent_id`   = 341800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 341824;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '旌德县',
    `parent_id`   = 341800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 341825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宣城市经济开发区',
    `parent_id`   = 341800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 341871;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁国市',
    `parent_id`   = 341800,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 341881;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '广德市',
    `parent_id`   = 341800,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 341882;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '福建省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 350000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '福州市',
    `parent_id`   = 350000,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 350100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鼓楼区',
    `parent_id`   = 350100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 350102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '台江区',
    `parent_id`   = 350100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 350103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仓山区',
    `parent_id`   = 350100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 350104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '马尾区',
    `parent_id`   = 350100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 350105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '晋安区',
    `parent_id`   = 350100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 350111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长乐区',
    `parent_id`   = 350100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 350112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '闽侯县',
    `parent_id`   = 350100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 350121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '连江县',
    `parent_id`   = 350100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 350122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '罗源县',
    `parent_id`   = 350100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 350123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '闽清县',
    `parent_id`   = 350100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 350124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永泰县',
    `parent_id`   = 350100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 350125;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平潭县',
    `parent_id`   = 350100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 350128;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '福清市',
    `parent_id`   = 350100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 350181;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '厦门市',
    `parent_id`   = 350000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 350200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '思明区',
    `parent_id`   = 350200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 350203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海沧区',
    `parent_id`   = 350200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 350205;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湖里区',
    `parent_id`   = 350200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 350206;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '集美区',
    `parent_id`   = 350200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 350211;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '同安区',
    `parent_id`   = 350200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 350212;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '翔安区',
    `parent_id`   = 350200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 350213;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '莆田市',
    `parent_id`   = 350000,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 350300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '城厢区',
    `parent_id`   = 350300,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 350302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '涵江区',
    `parent_id`   = 350300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 350303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '荔城区',
    `parent_id`   = 350300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 350304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '秀屿区',
    `parent_id`   = 350300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 350305;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仙游县',
    `parent_id`   = 350300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 350322;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '三明市',
    `parent_id`   = 350000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 350400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三元区',
    `parent_id`   = 350400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 350404;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沙县区',
    `parent_id`   = 350400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 350405;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '明溪县',
    `parent_id`   = 350400,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 350421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清流县',
    `parent_id`   = 350400,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 350423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁化县',
    `parent_id`   = 350400,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 350424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大田县',
    `parent_id`   = 350400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 350425;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '尤溪县',
    `parent_id`   = 350400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 350426;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '将乐县',
    `parent_id`   = 350400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 350428;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泰宁县',
    `parent_id`   = 350400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 350429;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '建宁县',
    `parent_id`   = 350400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 350430;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永安市',
    `parent_id`   = 350400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 350481;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '泉州市',
    `parent_id`   = 350000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 350500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鲤城区',
    `parent_id`   = 350500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 350502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丰泽区',
    `parent_id`   = 350500,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 350503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洛江区',
    `parent_id`   = 350500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 350504;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泉港区',
    `parent_id`   = 350500,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 350505;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '惠安县',
    `parent_id`   = 350500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 350521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安溪县',
    `parent_id`   = 350500,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 350524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永春县',
    `parent_id`   = 350500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 350525;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '德化县',
    `parent_id`   = 350500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 350526;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金门县',
    `parent_id`   = 350500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 350527;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石狮市',
    `parent_id`   = 350500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 350581;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '晋江市',
    `parent_id`   = 350500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 350582;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南安市',
    `parent_id`   = 350500,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 350583;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '漳州市',
    `parent_id`   = 350000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 350600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芗城区',
    `parent_id`   = 350600,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 350602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙文区',
    `parent_id`   = 350600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 350603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙海区',
    `parent_id`   = 350600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 350604;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长泰区',
    `parent_id`   = 350600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 350605;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '云霄县',
    `parent_id`   = 350600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 350622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '漳浦县',
    `parent_id`   = 350600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 350623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '诏安县',
    `parent_id`   = 350600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 350624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东山县',
    `parent_id`   = 350600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 350626;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南靖县',
    `parent_id`   = 350600,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 350627;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平和县',
    `parent_id`   = 350600,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 350628;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '华安县',
    `parent_id`   = 350600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 350629;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '南平市',
    `parent_id`   = 350000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 350700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '延平区',
    `parent_id`   = 350700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 350702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '建阳区',
    `parent_id`   = 350700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 350703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '顺昌县',
    `parent_id`   = 350700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 350721;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浦城县',
    `parent_id`   = 350700,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 350722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '光泽县',
    `parent_id`   = 350700,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 350723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '松溪县',
    `parent_id`   = 350700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 350724;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '政和县',
    `parent_id`   = 350700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 350725;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '邵武市',
    `parent_id`   = 350700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 350781;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武夷山市',
    `parent_id`   = 350700,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 350782;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '建瓯市',
    `parent_id`   = 350700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 350783;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '龙岩市',
    `parent_id`   = 350000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 350800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新罗区',
    `parent_id`   = 350800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 350802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永定区',
    `parent_id`   = 350800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 350803;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长汀县',
    `parent_id`   = 350800,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 350821;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '上杭县',
    `parent_id`   = 350800,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 350823;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武平县',
    `parent_id`   = 350800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 350824;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '连城县',
    `parent_id`   = 350800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 350825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '漳平市',
    `parent_id`   = 350800,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 350881;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '宁德市',
    `parent_id`   = 350000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 350900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蕉城区',
    `parent_id`   = 350900,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 350902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '霞浦县',
    `parent_id`   = 350900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 350921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '古田县',
    `parent_id`   = 350900,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 350922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '屏南县',
    `parent_id`   = 350900,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 350923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '寿宁县',
    `parent_id`   = 350900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 350924;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '周宁县',
    `parent_id`   = 350900,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 350925;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '柘荣县',
    `parent_id`   = 350900,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 350926;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '福安市',
    `parent_id`   = 350900,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 350981;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '福鼎市',
    `parent_id`   = 350900,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 350982;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '江西省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 360000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '南昌市',
    `parent_id`   = 360000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 360100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东湖区',
    `parent_id`   = 360100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 360102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西湖区',
    `parent_id`   = 360100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 360103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青云谱区',
    `parent_id`   = 360100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 360104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青山湖区',
    `parent_id`   = 360100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 360111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新建区',
    `parent_id`   = 360100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 360112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '红谷滩区',
    `parent_id`   = 360100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 360113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南昌县',
    `parent_id`   = 360100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 360121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安义县',
    `parent_id`   = 360100,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 360123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '进贤县',
    `parent_id`   = 360100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 360124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '经济技术开发区',
    `parent_id`   = 360100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 360190;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高新区',
    `parent_id`   = 360100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 360192;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '景德镇市',
    `parent_id`   = 360000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 360200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昌江区',
    `parent_id`   = 360200,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 360202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '珠山区',
    `parent_id`   = 360200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 360203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浮梁县',
    `parent_id`   = 360200,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 360222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乐平市',
    `parent_id`   = 360200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 360281;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '萍乡市',
    `parent_id`   = 360000,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 360300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安源区',
    `parent_id`   = 360300,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 360302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湘东区',
    `parent_id`   = 360300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 360313;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莲花县',
    `parent_id`   = 360300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 360321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '上栗县',
    `parent_id`   = 360300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 360322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芦溪县',
    `parent_id`   = 360300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 360323;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '九江市',
    `parent_id`   = 360000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 360400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '濂溪区',
    `parent_id`   = 360400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 360402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浔阳区',
    `parent_id`   = 360400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 360403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '柴桑区',
    `parent_id`   = 360400,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 360404;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武宁县',
    `parent_id`   = 360400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 360423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '修水县',
    `parent_id`   = 360400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 360424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永修县',
    `parent_id`   = 360400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 360425;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '德安县',
    `parent_id`   = 360400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 360426;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '都昌县',
    `parent_id`   = 360400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 360428;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湖口县',
    `parent_id`   = 360400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 360429;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '彭泽县',
    `parent_id`   = 360400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 360430;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '瑞昌市',
    `parent_id`   = 360400,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 360481;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '共青城市',
    `parent_id`   = 360400,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 360482;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '庐山市',
    `parent_id`   = 360400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 360483;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '经济技术开发区',
    `parent_id`   = 360400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 360490;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '新余市',
    `parent_id`   = 360000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 360500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '渝水区',
    `parent_id`   = 360500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 360502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '分宜县',
    `parent_id`   = 360500,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 360521;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '鹰潭市',
    `parent_id`   = 360000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 360600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '月湖区',
    `parent_id`   = 360600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 360602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '余江区',
    `parent_id`   = 360600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 360603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '贵溪市',
    `parent_id`   = 360600,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 360681;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '赣州市',
    `parent_id`   = 360000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 360700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '章贡区',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 360702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南康区',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 360703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '赣县区',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 360704;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '信丰县',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 360722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大余县',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 360723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '上犹县',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 360724;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '崇义县',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 360725;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安远县',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 360726;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '定南县',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 360728;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '全南县',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 360729;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁都县',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 360730;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '于都县',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 360731;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴国县',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 360732;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '会昌县',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 360733;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '寻乌县',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 360734;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石城县',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 360735;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '瑞金市',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 360781;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙南市',
    `parent_id`   = 360700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 360783;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '吉安市',
    `parent_id`   = 360000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 360800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吉州区',
    `parent_id`   = 360800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 360802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青原区',
    `parent_id`   = 360800,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 360803;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吉安县',
    `parent_id`   = 360800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 360821;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吉水县',
    `parent_id`   = 360800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 360822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '峡江县',
    `parent_id`   = 360800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 360823;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新干县',
    `parent_id`   = 360800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 360824;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永丰县',
    `parent_id`   = 360800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 360825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泰和县',
    `parent_id`   = 360800,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 360826;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '遂川县',
    `parent_id`   = 360800,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 360827;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万安县',
    `parent_id`   = 360800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 360828;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安福县',
    `parent_id`   = 360800,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 360829;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永新县',
    `parent_id`   = 360800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 360830;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '井冈山市',
    `parent_id`   = 360800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 360881;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '宜春市',
    `parent_id`   = 360000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 360900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '袁州区',
    `parent_id`   = 360900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 360902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '奉新县',
    `parent_id`   = 360900,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 360921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万载县',
    `parent_id`   = 360900,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 360922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '上高县',
    `parent_id`   = 360900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 360923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宜丰县',
    `parent_id`   = 360900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 360924;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '靖安县',
    `parent_id`   = 360900,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 360925;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铜鼓县',
    `parent_id`   = 360900,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 360926;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丰城市',
    `parent_id`   = 360900,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 360981;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '樟树市',
    `parent_id`   = 360900,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 360982;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高安市',
    `parent_id`   = 360900,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 360983;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '抚州市',
    `parent_id`   = 360000,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 361000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临川区',
    `parent_id`   = 361000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 361002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东乡区',
    `parent_id`   = 361000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 361003;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南城县',
    `parent_id`   = 361000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 361021;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黎川县',
    `parent_id`   = 361000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 361022;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南丰县',
    `parent_id`   = 361000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 361023;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '崇仁县',
    `parent_id`   = 361000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 361024;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乐安县',
    `parent_id`   = 361000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 361025;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宜黄县',
    `parent_id`   = 361000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 361026;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金溪县',
    `parent_id`   = 361000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 361027;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '资溪县',
    `parent_id`   = 361000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 361028;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '广昌县',
    `parent_id`   = 361000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 361030;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '上饶市',
    `parent_id`   = 360000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 361100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '信州区',
    `parent_id`   = 361100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 361102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '广丰区',
    `parent_id`   = 361100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 361103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '广信区',
    `parent_id`   = 361100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 361104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玉山县',
    `parent_id`   = 361100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 361123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铅山县',
    `parent_id`   = 361100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 361124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '横峰县',
    `parent_id`   = 361100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 361125;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '弋阳县',
    `parent_id`   = 361100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 361126;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '余干县',
    `parent_id`   = 361100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 361127;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鄱阳县',
    `parent_id`   = 361100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 361128;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万年县',
    `parent_id`   = 361100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 361129;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '婺源县',
    `parent_id`   = 361100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 361130;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '德兴市',
    `parent_id`   = 361100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 361181;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '山东省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 370000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '济南市',
    `parent_id`   = 370000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 370100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '历下区',
    `parent_id`   = 370100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 370102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '市中区',
    `parent_id`   = 370100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 370103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '槐荫区',
    `parent_id`   = 370100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 370104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天桥区',
    `parent_id`   = 370100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 370105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '历城区',
    `parent_id`   = 370100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 370112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长清区',
    `parent_id`   = 370100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 370113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '章丘区',
    `parent_id`   = 370100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 370114;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '济阳区',
    `parent_id`   = 370100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 370115;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莱芜区',
    `parent_id`   = 370100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 370116;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '钢城区',
    `parent_id`   = 370100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 370117;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平阴县',
    `parent_id`   = 370100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 370124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '商河县',
    `parent_id`   = 370100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 370126;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '济南高新技术产业开发区',
    `parent_id`   = 370100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 370171;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高新区',
    `parent_id`   = 370100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 370190;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '青岛市',
    `parent_id`   = 370000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 370200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '市南区',
    `parent_id`   = 370200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 370202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '市北区',
    `parent_id`   = 370200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 370203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黄岛区',
    `parent_id`   = 370200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 370211;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '崂山区',
    `parent_id`   = 370200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 370212;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '李沧区',
    `parent_id`   = 370200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 370213;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '城阳区',
    `parent_id`   = 370200,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 370214;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '即墨区',
    `parent_id`   = 370200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 370215;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青岛高新技术产业开发区',
    `parent_id`   = 370200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 370271;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '胶州市',
    `parent_id`   = 370200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 370281;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平度市',
    `parent_id`   = 370200,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 370283;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莱西市',
    `parent_id`   = 370200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 370285;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '开发区',
    `parent_id`   = 370200,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 370290;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '淄博市',
    `parent_id`   = 370000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 370300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '淄川区',
    `parent_id`   = 370300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 370302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '张店区',
    `parent_id`   = 370300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 370303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '博山区',
    `parent_id`   = 370300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 370304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临淄区',
    `parent_id`   = 370300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 370305;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '周村区',
    `parent_id`   = 370300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 370306;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桓台县',
    `parent_id`   = 370300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 370321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高青县',
    `parent_id`   = 370300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 370322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沂源县',
    `parent_id`   = 370300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 370323;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '枣庄市',
    `parent_id`   = 370000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 370400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '市中区',
    `parent_id`   = 370400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 370402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '薛城区',
    `parent_id`   = 370400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 370403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '峄城区',
    `parent_id`   = 370400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 370404;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '台儿庄区',
    `parent_id`   = 370400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 370405;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '山亭区',
    `parent_id`   = 370400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 370406;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '滕州市',
    `parent_id`   = 370400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 370481;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '东营市',
    `parent_id`   = 370000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 370500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东营区',
    `parent_id`   = 370500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 370502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河口区',
    `parent_id`   = 370500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 370503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '垦利区',
    `parent_id`   = 370500,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 370505;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '利津县',
    `parent_id`   = 370500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 370522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '广饶县',
    `parent_id`   = 370500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 370523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东营经济技术开发区',
    `parent_id`   = 370500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 370571;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东营港经济开发区',
    `parent_id`   = 370500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 370572;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '烟台市',
    `parent_id`   = 370000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 370600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芝罘区',
    `parent_id`   = 370600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 370602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '福山区',
    `parent_id`   = 370600,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 370611;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '牟平区',
    `parent_id`   = 370600,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 370612;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莱山区',
    `parent_id`   = 370600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 370613;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蓬莱区',
    `parent_id`   = 370600,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 370614;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '烟台高新技术产业开发区',
    `parent_id`   = 370600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 370671;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '烟台经济技术开发区',
    `parent_id`   = 370600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 370672;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙口市',
    `parent_id`   = 370600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 370681;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莱阳市',
    `parent_id`   = 370600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 370682;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莱州市',
    `parent_id`   = 370600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 370683;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '招远市',
    `parent_id`   = 370600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 370685;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '栖霞市',
    `parent_id`   = 370600,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 370686;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海阳市',
    `parent_id`   = 370600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 370687;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '开发区',
    `parent_id`   = 370600,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 370690;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '潍坊市',
    `parent_id`   = 370000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 370700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '潍城区',
    `parent_id`   = 370700,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 370702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '寒亭区',
    `parent_id`   = 370700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 370703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '坊子区',
    `parent_id`   = 370700,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 370704;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '奎文区',
    `parent_id`   = 370700,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 370705;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临朐县',
    `parent_id`   = 370700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 370724;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昌乐县',
    `parent_id`   = 370700,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 370725;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '潍坊滨海经济技术开发区',
    `parent_id`   = 370700,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 370772;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青州市',
    `parent_id`   = 370700,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 370781;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '诸城市',
    `parent_id`   = 370700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 370782;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '寿光市',
    `parent_id`   = 370700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 370783;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安丘市',
    `parent_id`   = 370700,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 370784;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高密市',
    `parent_id`   = 370700,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 370785;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昌邑市',
    `parent_id`   = 370700,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 370786;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '开发区',
    `parent_id`   = 370700,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 370790;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高新区',
    `parent_id`   = 370700,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 370791;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '济宁市',
    `parent_id`   = 370000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 370800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '任城区',
    `parent_id`   = 370800,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 370811;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兖州区',
    `parent_id`   = 370800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 370812;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '微山县',
    `parent_id`   = 370800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 370826;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鱼台县',
    `parent_id`   = 370800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 370827;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金乡县',
    `parent_id`   = 370800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 370828;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '嘉祥县',
    `parent_id`   = 370800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 370829;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汶上县',
    `parent_id`   = 370800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 370830;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泗水县',
    `parent_id`   = 370800,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 370831;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '梁山县',
    `parent_id`   = 370800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 370832;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '济宁高新技术产业开发区',
    `parent_id`   = 370800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 370871;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '曲阜市',
    `parent_id`   = 370800,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 370881;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '邹城市',
    `parent_id`   = 370800,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 370883;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高新区',
    `parent_id`   = 370800,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 370890;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '泰安市',
    `parent_id`   = 370000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 370900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泰山区',
    `parent_id`   = 370900,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 370902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岱岳区',
    `parent_id`   = 370900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 370911;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁阳县',
    `parent_id`   = 370900,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 370921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东平县',
    `parent_id`   = 370900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 370923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新泰市',
    `parent_id`   = 370900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 370982;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '肥城市',
    `parent_id`   = 370900,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 370983;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '威海市',
    `parent_id`   = 370000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 371000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '环翠区',
    `parent_id`   = 371000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 371002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '文登区',
    `parent_id`   = 371000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 371003;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '威海火炬高技术产业开发区',
    `parent_id`   = 371000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 371071;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '威海经济技术开发区',
    `parent_id`   = 371000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 371072;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '荣成市',
    `parent_id`   = 371000,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 371082;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乳山市',
    `parent_id`   = 371000,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 371083;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '经济技术开发区',
    `parent_id`   = 371000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 371091;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '日照市',
    `parent_id`   = 370000,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 371100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东港区',
    `parent_id`   = 371100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 371102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岚山区',
    `parent_id`   = 371100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 371103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五莲县',
    `parent_id`   = 371100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 371121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莒县',
    `parent_id`   = 371100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 371122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '日照经济技术开发区',
    `parent_id`   = 371100,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 371171;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '临沂市',
    `parent_id`   = 370000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 371300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兰山区',
    `parent_id`   = 371300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 371302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '罗庄区',
    `parent_id`   = 371300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 371311;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河东区',
    `parent_id`   = 371300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 371312;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沂南县',
    `parent_id`   = 371300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 371321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '郯城县',
    `parent_id`   = 371300,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 371322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沂水县',
    `parent_id`   = 371300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 371323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兰陵县',
    `parent_id`   = 371300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 371324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '费县',
    `parent_id`   = 371300,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 371325;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平邑县',
    `parent_id`   = 371300,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 371326;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莒南县',
    `parent_id`   = 371300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 371327;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蒙阴县',
    `parent_id`   = 371300,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 371328;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临沭县',
    `parent_id`   = 371300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 371329;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临沂高新技术产业开发区',
    `parent_id`   = 371300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 371371;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '德州市',
    `parent_id`   = 370000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 371400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '德城区',
    `parent_id`   = 371400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 371402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '陵城区',
    `parent_id`   = 371400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 371403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁津县',
    `parent_id`   = 371400,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 371422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '庆云县',
    `parent_id`   = 371400,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 371423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临邑县',
    `parent_id`   = 371400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 371424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '齐河县',
    `parent_id`   = 371400,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 371425;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平原县',
    `parent_id`   = 371400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 371426;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '夏津县',
    `parent_id`   = 371400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 371427;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武城县',
    `parent_id`   = 371400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 371428;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '德州运河经济开发区',
    `parent_id`   = 371400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 371472;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乐陵市',
    `parent_id`   = 371400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 371481;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '禹城市',
    `parent_id`   = 371400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 371482;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '聊城市',
    `parent_id`   = 370000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 371500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东昌府区',
    `parent_id`   = 371500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 371502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '茌平区',
    `parent_id`   = 371500,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 371503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阳谷县',
    `parent_id`   = 371500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 371521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莘县',
    `parent_id`   = 371500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 371522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东阿县',
    `parent_id`   = 371500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 371524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '冠县',
    `parent_id`   = 371500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 371525;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高唐县',
    `parent_id`   = 371500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 371526;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临清市',
    `parent_id`   = 371500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 371581;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '滨州市',
    `parent_id`   = 370000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 371600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '滨城区',
    `parent_id`   = 371600,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 371602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沾化区',
    `parent_id`   = 371600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 371603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '惠民县',
    `parent_id`   = 371600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 371621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阳信县',
    `parent_id`   = 371600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 371622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '无棣县',
    `parent_id`   = 371600,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 371623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '博兴县',
    `parent_id`   = 371600,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 371625;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '邹平市',
    `parent_id`   = 371600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 371681;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '菏泽市',
    `parent_id`   = 370000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 371700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '牡丹区',
    `parent_id`   = 371700,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 371702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '定陶区',
    `parent_id`   = 371700,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 371703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '曹县',
    `parent_id`   = 371700,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 371721;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '单县',
    `parent_id`   = 371700,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 371722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '成武县',
    `parent_id`   = 371700,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 371723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巨野县',
    `parent_id`   = 371700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 371724;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '郓城县',
    `parent_id`   = 371700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 371725;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鄄城县',
    `parent_id`   = 371700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 371726;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东明县',
    `parent_id`   = 371700,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 371728;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '菏泽经济技术开发区',
    `parent_id`   = 371700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 371771;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '菏泽高新技术开发区',
    `parent_id`   = 371700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 371772;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '河南省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 410000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '郑州市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 410100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中原区',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 410102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '二七区',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 410103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '管城回族区',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 410104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金水区',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 410105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '上街区',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 410106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '惠济区',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 410108;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中牟县',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 410122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '郑州经济技术开发区',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 410171;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '郑州高新技术产业开发区',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 410172;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '郑州航空港经济综合实验区',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 410173;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巩义市',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 410181;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '荥阳市',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 410182;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新密市',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 410183;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新郑市',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 410184;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '登封市',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 410185;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高新技术开发区',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 410190;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '经济技术开发区',
    `parent_id`   = 410100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 410191;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '开封市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 410200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙亭区',
    `parent_id`   = 410200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 410202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '顺河回族区',
    `parent_id`   = 410200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 410203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鼓楼区',
    `parent_id`   = 410200,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 410204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '禹王台区',
    `parent_id`   = 410200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 410205;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '祥符区',
    `parent_id`   = 410200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 410212;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '杞县',
    `parent_id`   = 410200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 410221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '通许县',
    `parent_id`   = 410200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 410222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '尉氏县',
    `parent_id`   = 410200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 410223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兰考县',
    `parent_id`   = 410200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 410225;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '洛阳市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 410300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '老城区',
    `parent_id`   = 410300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 410302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西工区',
    `parent_id`   = 410300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 410303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '瀍河回族区',
    `parent_id`   = 410300,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 410304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '涧西区',
    `parent_id`   = 410300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 410305;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吉利区',
    `parent_id`   = 410300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 410306;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '偃师区',
    `parent_id`   = 410300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 410307;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '孟津区',
    `parent_id`   = 410300,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 410308;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洛龙区',
    `parent_id`   = 410300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 410311;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新安县',
    `parent_id`   = 410300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 410323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '栾川县',
    `parent_id`   = 410300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 410324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '嵩县',
    `parent_id`   = 410300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 410325;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汝阳县',
    `parent_id`   = 410300,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 410326;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宜阳县',
    `parent_id`   = 410300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 410327;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洛宁县',
    `parent_id`   = 410300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 410328;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '伊川县',
    `parent_id`   = 410300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 410329;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '平顶山市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 410400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新华区',
    `parent_id`   = 410400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 410402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '卫东区',
    `parent_id`   = 410400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 410403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石龙区',
    `parent_id`   = 410400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 410404;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湛河区',
    `parent_id`   = 410400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 410411;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宝丰县',
    `parent_id`   = 410400,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 410421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '叶县',
    `parent_id`   = 410400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 410422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鲁山县',
    `parent_id`   = 410400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 410423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '郏县',
    `parent_id`   = 410400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 410425;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平顶山高新技术产业开发区',
    `parent_id`   = 410400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 410471;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '舞钢市',
    `parent_id`   = 410400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 410481;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汝州市',
    `parent_id`   = 410400,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 410482;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '安阳市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 410500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '文峰区',
    `parent_id`   = 410500,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 410502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北关区',
    `parent_id`   = 410500,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 410503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '殷都区',
    `parent_id`   = 410500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 410505;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙安区',
    `parent_id`   = 410500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 410506;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安阳县',
    `parent_id`   = 410500,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 410522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汤阴县',
    `parent_id`   = 410500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 410523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '滑县',
    `parent_id`   = 410500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 410526;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '内黄县',
    `parent_id`   = 410500,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 410527;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '林州市',
    `parent_id`   = 410500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 410581;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '开发区',
    `parent_id`   = 410500,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 410590;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '鹤壁市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 410600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鹤山区',
    `parent_id`   = 410600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 410602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '山城区',
    `parent_id`   = 410600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 410603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '淇滨区',
    `parent_id`   = 410600,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 410611;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浚县',
    `parent_id`   = 410600,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 410621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '淇县',
    `parent_id`   = 410600,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 410622;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '新乡市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 410700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '红旗区',
    `parent_id`   = 410700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 410702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '卫滨区',
    `parent_id`   = 410700,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 410703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凤泉区',
    `parent_id`   = 410700,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 410704;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '牧野区',
    `parent_id`   = 410700,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 410711;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新乡县',
    `parent_id`   = 410700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 410721;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '获嘉县',
    `parent_id`   = 410700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 410724;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '原阳县',
    `parent_id`   = 410700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 410725;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '延津县',
    `parent_id`   = 410700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 410726;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '封丘县',
    `parent_id`   = 410700,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 410727;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新乡高新技术产业开发区',
    `parent_id`   = 410700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 410771;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新乡经济技术开发区',
    `parent_id`   = 410700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 410772;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '卫辉市',
    `parent_id`   = 410700,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 410781;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '辉县市',
    `parent_id`   = 410700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 410782;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长垣市',
    `parent_id`   = 410700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 410783;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '焦作市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 410800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '解放区',
    `parent_id`   = 410800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 410802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中站区',
    `parent_id`   = 410800,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 410803;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '马村区',
    `parent_id`   = 410800,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 410804;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '山阳区',
    `parent_id`   = 410800,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 410811;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '修武县',
    `parent_id`   = 410800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 410821;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '博爱县',
    `parent_id`   = 410800,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 410822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武陟县',
    `parent_id`   = 410800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 410823;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '温县',
    `parent_id`   = 410800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 410825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '焦作城乡一体化示范区',
    `parent_id`   = 410800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 410871;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沁阳市',
    `parent_id`   = 410800,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 410882;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '孟州市',
    `parent_id`   = 410800,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 410883;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '濮阳市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 410900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '华龙区',
    `parent_id`   = 410900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 410902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清丰县',
    `parent_id`   = 410900,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 410922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南乐县',
    `parent_id`   = 410900,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 410923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '范县',
    `parent_id`   = 410900,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 410926;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '台前县',
    `parent_id`   = 410900,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 410927;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '濮阳县',
    `parent_id`   = 410900,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 410928;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河南濮阳工业园区',
    `parent_id`   = 410900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 410971;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '许昌市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 411000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '魏都区',
    `parent_id`   = 411000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 411002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '建安区',
    `parent_id`   = 411000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 411003;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鄢陵县',
    `parent_id`   = 411000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 411024;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '襄城县',
    `parent_id`   = 411000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 411025;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '许昌经济技术开发区',
    `parent_id`   = 411000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 411071;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '禹州市',
    `parent_id`   = 411000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 411081;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长葛市',
    `parent_id`   = 411000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 411082;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '漯河市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 411100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '源汇区',
    `parent_id`   = 411100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 411102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '郾城区',
    `parent_id`   = 411100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 411103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '召陵区',
    `parent_id`   = 411100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 411104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '舞阳县',
    `parent_id`   = 411100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 411121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临颍县',
    `parent_id`   = 411100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 411122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '漯河经济技术开发区',
    `parent_id`   = 411100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 411171;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '三门峡市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 411200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湖滨区',
    `parent_id`   = 411200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 411202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '陕州区',
    `parent_id`   = 411200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 411203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '渑池县',
    `parent_id`   = 411200,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 411221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '卢氏县',
    `parent_id`   = 411200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 411224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河南三门峡经济开发区',
    `parent_id`   = 411200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 411271;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '义马市',
    `parent_id`   = 411200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 411281;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '灵宝市',
    `parent_id`   = 411200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 411282;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '南阳市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 411300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宛城区',
    `parent_id`   = 411300,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 411302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '卧龙区',
    `parent_id`   = 411300,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 411303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南召县',
    `parent_id`   = 411300,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 411321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '方城县',
    `parent_id`   = 411300,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 411322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西峡县',
    `parent_id`   = 411300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 411323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '镇平县',
    `parent_id`   = 411300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 411324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '内乡县',
    `parent_id`   = 411300,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 411325;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '淅川县',
    `parent_id`   = 411300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 411326;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '社旗县',
    `parent_id`   = 411300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 411327;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '唐河县',
    `parent_id`   = 411300,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 411328;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新野县',
    `parent_id`   = 411300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 411329;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桐柏县',
    `parent_id`   = 411300,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 411330;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南阳市城乡一体化示范区',
    `parent_id`   = 411300,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 411372;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '邓州市',
    `parent_id`   = 411300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 411381;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '商丘市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 411400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '梁园区',
    `parent_id`   = 411400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 411402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '睢阳区',
    `parent_id`   = 411400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 411403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '民权县',
    `parent_id`   = 411400,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 411421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '睢县',
    `parent_id`   = 411400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 411422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁陵县',
    `parent_id`   = 411400,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 411423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '柘城县',
    `parent_id`   = 411400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 411424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '虞城县',
    `parent_id`   = 411400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 411425;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '夏邑县',
    `parent_id`   = 411400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 411426;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永城市',
    `parent_id`   = 411400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 411481;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '信阳市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 411500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浉河区',
    `parent_id`   = 411500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 411502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平桥区',
    `parent_id`   = 411500,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 411503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '罗山县',
    `parent_id`   = 411500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 411521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '光山县',
    `parent_id`   = 411500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 411522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新县',
    `parent_id`   = 411500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 411523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '商城县',
    `parent_id`   = 411500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 411524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '固始县',
    `parent_id`   = 411500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 411525;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '潢川县',
    `parent_id`   = 411500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 411526;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '淮滨县',
    `parent_id`   = 411500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 411527;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '息县',
    `parent_id`   = 411500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 411528;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '周口市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 411600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '川汇区',
    `parent_id`   = 411600,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 411602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '淮阳区',
    `parent_id`   = 411600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 411603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '扶沟县',
    `parent_id`   = 411600,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 411621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西华县',
    `parent_id`   = 411600,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 411622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '商水县',
    `parent_id`   = 411600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 411623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沈丘县',
    `parent_id`   = 411600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 411624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '郸城县',
    `parent_id`   = 411600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 411625;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '太康县',
    `parent_id`   = 411600,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 411627;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鹿邑县',
    `parent_id`   = 411600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 411628;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河南周口经济开发区',
    `parent_id`   = 411600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 411671;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '项城市',
    `parent_id`   = 411600,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 411681;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '经济开发区',
    `parent_id`   = 411600,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 411690;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '驻马店市',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 411700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '驿城区',
    `parent_id`   = 411700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 411702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西平县',
    `parent_id`   = 411700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 411721;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '上蔡县',
    `parent_id`   = 411700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 411722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平舆县',
    `parent_id`   = 411700,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 411723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '正阳县',
    `parent_id`   = 411700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 411724;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '确山县',
    `parent_id`   = 411700,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 411725;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泌阳县',
    `parent_id`   = 411700,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 411726;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汝南县',
    `parent_id`   = 411700,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 411727;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '遂平县',
    `parent_id`   = 411700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 411728;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新蔡县',
    `parent_id`   = 411700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 411729;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '省直辖县',
    `parent_id`   = 410000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 419000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '济源市',
    `parent_id`   = 419000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 419001;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '湖北省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 420000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '武汉市',
    `parent_id`   = 420000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 420100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江岸区',
    `parent_id`   = 420100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 420102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江汉区',
    `parent_id`   = 420100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 420103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '硚口区',
    `parent_id`   = 420100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 420104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汉阳区',
    `parent_id`   = 420100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 420105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武昌区',
    `parent_id`   = 420100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 420106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青山区',
    `parent_id`   = 420100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 420107;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洪山区',
    `parent_id`   = 420100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 420111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东西湖区',
    `parent_id`   = 420100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 420112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汉南区',
    `parent_id`   = 420100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 420113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蔡甸区',
    `parent_id`   = 420100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 420114;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江夏区',
    `parent_id`   = 420100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 420115;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黄陂区',
    `parent_id`   = 420100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 420116;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新洲区',
    `parent_id`   = 420100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 420117;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '黄石市',
    `parent_id`   = 420000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 420200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黄石港区',
    `parent_id`   = 420200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 420202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西塞山区',
    `parent_id`   = 420200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 420203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '下陆区',
    `parent_id`   = 420200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 420204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铁山区',
    `parent_id`   = 420200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 420205;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阳新县',
    `parent_id`   = 420200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 420222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大冶市',
    `parent_id`   = 420200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 420281;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '十堰市',
    `parent_id`   = 420000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 420300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '茅箭区',
    `parent_id`   = 420300,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 420302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '张湾区',
    `parent_id`   = 420300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 420303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '郧阳区',
    `parent_id`   = 420300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 420304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '郧西县',
    `parent_id`   = 420300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 420322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '竹山县',
    `parent_id`   = 420300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 420323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '竹溪县',
    `parent_id`   = 420300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 420324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '房县',
    `parent_id`   = 420300,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 420325;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丹江口市',
    `parent_id`   = 420300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 420381;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '宜昌市',
    `parent_id`   = 420000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 420500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西陵区',
    `parent_id`   = 420500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 420502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '伍家岗区',
    `parent_id`   = 420500,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 420503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '点军区',
    `parent_id`   = 420500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 420504;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '猇亭区',
    `parent_id`   = 420500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 420505;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '夷陵区',
    `parent_id`   = 420500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 420506;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '远安县',
    `parent_id`   = 420500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 420525;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴山县',
    `parent_id`   = 420500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 420526;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '秭归县',
    `parent_id`   = 420500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 420527;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长阳土家族自治县',
    `parent_id`   = 420500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 420528;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五峰土家族自治县',
    `parent_id`   = 420500,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 420529;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宜都市',
    `parent_id`   = 420500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 420581;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '当阳市',
    `parent_id`   = 420500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 420582;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '枝江市',
    `parent_id`   = 420500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 420583;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '经济开发区',
    `parent_id`   = 420500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 420590;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '襄阳市',
    `parent_id`   = 420000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 420600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '襄城区',
    `parent_id`   = 420600,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 420602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '樊城区',
    `parent_id`   = 420600,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 420606;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '襄州区',
    `parent_id`   = 420600,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 420607;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南漳县',
    `parent_id`   = 420600,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 420624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '谷城县',
    `parent_id`   = 420600,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 420625;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '保康县',
    `parent_id`   = 420600,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 420626;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '老河口市',
    `parent_id`   = 420600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 420682;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '枣阳市',
    `parent_id`   = 420600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 420683;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宜城市',
    `parent_id`   = 420600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 420684;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '鄂州市',
    `parent_id`   = 420000,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 420700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '梁子湖区',
    `parent_id`   = 420700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 420702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '华容区',
    `parent_id`   = 420700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 420703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鄂城区',
    `parent_id`   = 420700,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 420704;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '荆门市',
    `parent_id`   = 420000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 420800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东宝区',
    `parent_id`   = 420800,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 420802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '掇刀区',
    `parent_id`   = 420800,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 420804;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沙洋县',
    `parent_id`   = 420800,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 420822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '钟祥市',
    `parent_id`   = 420800,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 420881;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '京山市',
    `parent_id`   = 420800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 420882;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '孝感市',
    `parent_id`   = 420000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 420900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '孝南区',
    `parent_id`   = 420900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 420902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '孝昌县',
    `parent_id`   = 420900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 420921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大悟县',
    `parent_id`   = 420900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 420922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '云梦县',
    `parent_id`   = 420900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 420923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '应城市',
    `parent_id`   = 420900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 420981;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安陆市',
    `parent_id`   = 420900,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 420982;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汉川市',
    `parent_id`   = 420900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 420984;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '荆州市',
    `parent_id`   = 420000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 421000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沙市区',
    `parent_id`   = 421000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 421002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '荆州区',
    `parent_id`   = 421000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 421003;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '公安县',
    `parent_id`   = 421000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 421022;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江陵县',
    `parent_id`   = 421000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 421024;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石首市',
    `parent_id`   = 421000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 421081;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洪湖市',
    `parent_id`   = 421000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 421083;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '松滋市',
    `parent_id`   = 421000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 421087;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '监利市',
    `parent_id`   = 421000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 421088;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '黄冈市',
    `parent_id`   = 420000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 421100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黄州区',
    `parent_id`   = 421100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 421102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '团风县',
    `parent_id`   = 421100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 421121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '红安县',
    `parent_id`   = 421100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 421122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '罗田县',
    `parent_id`   = 421100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 421123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '英山县',
    `parent_id`   = 421100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 421124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浠水县',
    `parent_id`   = 421100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 421125;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蕲春县',
    `parent_id`   = 421100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 421126;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黄梅县',
    `parent_id`   = 421100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 421127;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙感湖管理区',
    `parent_id`   = 421100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 421171;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '麻城市',
    `parent_id`   = 421100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 421181;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武穴市',
    `parent_id`   = 421100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 421182;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '咸宁市',
    `parent_id`   = 420000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 421200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '咸安区',
    `parent_id`   = 421200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 421202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '嘉鱼县',
    `parent_id`   = 421200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 421221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '通城县',
    `parent_id`   = 421200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 421222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '崇阳县',
    `parent_id`   = 421200,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 421223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '通山县',
    `parent_id`   = 421200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 421224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '赤壁市',
    `parent_id`   = 421200,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 421281;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '随州市',
    `parent_id`   = 420000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 421300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '曾都区',
    `parent_id`   = 421300,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 421303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '随县',
    `parent_id`   = 421300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 421321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '广水市',
    `parent_id`   = 421300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 421381;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '恩施土家族苗族自治州',
    `parent_id`   = 420000,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 422800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '恩施市',
    `parent_id`   = 422800,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 422801;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '利川市',
    `parent_id`   = 422800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 422802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '建始县',
    `parent_id`   = 422800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 422822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巴东县',
    `parent_id`   = 422800,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 422823;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宣恩县',
    `parent_id`   = 422800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 422825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '咸丰县',
    `parent_id`   = 422800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 422826;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '来凤县',
    `parent_id`   = 422800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 422827;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鹤峰县',
    `parent_id`   = 422800,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 422828;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '省直辖县',
    `parent_id`   = 420000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 429000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仙桃市',
    `parent_id`   = 429000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 429004;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '潜江市',
    `parent_id`   = 429000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 429005;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天门市',
    `parent_id`   = 429000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 429006;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '神农架林区',
    `parent_id`   = 429000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 429021;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '湖南省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 430000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '长沙市',
    `parent_id`   = 430000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 430100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芙蓉区',
    `parent_id`   = 430100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 430102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天心区',
    `parent_id`   = 430100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 430103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岳麓区',
    `parent_id`   = 430100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 430104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '开福区',
    `parent_id`   = 430100,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 430105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雨花区',
    `parent_id`   = 430100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 430111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '望城区',
    `parent_id`   = 430100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 430112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长沙县',
    `parent_id`   = 430100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 430121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浏阳市',
    `parent_id`   = 430100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 430181;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁乡市',
    `parent_id`   = 430100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 430182;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '株洲市',
    `parent_id`   = 430000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 430200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '荷塘区',
    `parent_id`   = 430200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 430202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芦淞区',
    `parent_id`   = 430200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 430203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石峰区',
    `parent_id`   = 430200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 430204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天元区',
    `parent_id`   = 430200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 430211;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '渌口区',
    `parent_id`   = 430200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 430212;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '攸县',
    `parent_id`   = 430200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 430223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '茶陵县',
    `parent_id`   = 430200,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 430224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '炎陵县',
    `parent_id`   = 430200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 430225;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '云龙示范区',
    `parent_id`   = 430200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 430271;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '醴陵市',
    `parent_id`   = 430200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 430281;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '湘潭市',
    `parent_id`   = 430000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 430300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雨湖区',
    `parent_id`   = 430300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 430302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岳塘区',
    `parent_id`   = 430300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 430304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湘潭县',
    `parent_id`   = 430300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 430321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湘潭九华示范区',
    `parent_id`   = 430300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 430373;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湘乡市',
    `parent_id`   = 430300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 430381;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '韶山市',
    `parent_id`   = 430300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 430382;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '衡阳市',
    `parent_id`   = 430000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 430400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '珠晖区',
    `parent_id`   = 430400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 430405;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雁峰区',
    `parent_id`   = 430400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 430406;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石鼓区',
    `parent_id`   = 430400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 430407;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蒸湘区',
    `parent_id`   = 430400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 430408;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南岳区',
    `parent_id`   = 430400,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 430412;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '衡阳县',
    `parent_id`   = 430400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 430421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '衡南县',
    `parent_id`   = 430400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 430422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '衡山县',
    `parent_id`   = 430400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 430423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '衡东县',
    `parent_id`   = 430400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 430424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '祁东县',
    `parent_id`   = 430400,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 430426;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '耒阳市',
    `parent_id`   = 430400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 430481;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '常宁市',
    `parent_id`   = 430400,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 430482;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '邵阳市',
    `parent_id`   = 430000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 430500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '双清区',
    `parent_id`   = 430500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 430502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大祥区',
    `parent_id`   = 430500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 430503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北塔区',
    `parent_id`   = 430500,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 430511;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新邵县',
    `parent_id`   = 430500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 430522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '邵阳县',
    `parent_id`   = 430500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 430523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '隆回县',
    `parent_id`   = 430500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 430524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洞口县',
    `parent_id`   = 430500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 430525;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '绥宁县',
    `parent_id`   = 430500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 430527;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新宁县',
    `parent_id`   = 430500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 430528;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '城步苗族自治县',
    `parent_id`   = 430500,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 430529;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武冈市',
    `parent_id`   = 430500,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 430581;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '邵东市',
    `parent_id`   = 430500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 430582;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '岳阳市',
    `parent_id`   = 430000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 430600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岳阳楼区',
    `parent_id`   = 430600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 430602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '云溪区',
    `parent_id`   = 430600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 430603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '君山区',
    `parent_id`   = 430600,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 430611;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岳阳县',
    `parent_id`   = 430600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 430621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '华容县',
    `parent_id`   = 430600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 430623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湘阴县',
    `parent_id`   = 430600,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 430624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平江县',
    `parent_id`   = 430600,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 430626;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汨罗市',
    `parent_id`   = 430600,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 430681;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临湘市',
    `parent_id`   = 430600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 430682;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '常德市',
    `parent_id`   = 430000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 430700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武陵区',
    `parent_id`   = 430700,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 430702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鼎城区',
    `parent_id`   = 430700,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 430703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安乡县',
    `parent_id`   = 430700,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 430721;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汉寿县',
    `parent_id`   = 430700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 430722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '澧县',
    `parent_id`   = 430700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 430723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临澧县',
    `parent_id`   = 430700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 430724;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桃源县',
    `parent_id`   = 430700,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 430725;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石门县',
    `parent_id`   = 430700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 430726;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '津市市',
    `parent_id`   = 430700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 430781;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '张家界市',
    `parent_id`   = 430000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 430800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永定区',
    `parent_id`   = 430800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 430802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武陵源区',
    `parent_id`   = 430800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 430811;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '慈利县',
    `parent_id`   = 430800,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 430821;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桑植县',
    `parent_id`   = 430800,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 430822;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '益阳市',
    `parent_id`   = 430000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 430900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '资阳区',
    `parent_id`   = 430900,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 430902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '赫山区',
    `parent_id`   = 430900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 430903;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南县',
    `parent_id`   = 430900,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 430921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桃江县',
    `parent_id`   = 430900,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 430922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安化县',
    `parent_id`   = 430900,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 430923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '益阳市大通湖管理区',
    `parent_id`   = 430900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 430971;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沅江市',
    `parent_id`   = 430900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 430981;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '郴州市',
    `parent_id`   = 430000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 431000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北湖区',
    `parent_id`   = 431000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 431002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '苏仙区',
    `parent_id`   = 431000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 431003;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桂阳县',
    `parent_id`   = 431000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 431021;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宜章县',
    `parent_id`   = 431000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 431022;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永兴县',
    `parent_id`   = 431000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 431023;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '嘉禾县',
    `parent_id`   = 431000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 431024;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临武县',
    `parent_id`   = 431000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 431025;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汝城县',
    `parent_id`   = 431000,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 431026;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桂东县',
    `parent_id`   = 431000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 431027;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安仁县',
    `parent_id`   = 431000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 431028;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '资兴市',
    `parent_id`   = 431000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 431081;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '永州市',
    `parent_id`   = 430000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 431100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '零陵区',
    `parent_id`   = 431100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 431102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '冷水滩区',
    `parent_id`   = 431100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 431103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东安县',
    `parent_id`   = 431100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 431122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '双牌县',
    `parent_id`   = 431100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 431123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '道县',
    `parent_id`   = 431100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 431124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江永县',
    `parent_id`   = 431100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 431125;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁远县',
    `parent_id`   = 431100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 431126;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蓝山县',
    `parent_id`   = 431100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 431127;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新田县',
    `parent_id`   = 431100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 431128;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江华瑶族自治县',
    `parent_id`   = 431100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 431129;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '祁阳市',
    `parent_id`   = 431100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 431181;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '怀化市',
    `parent_id`   = 430000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 431200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鹤城区',
    `parent_id`   = 431200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 431202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中方县',
    `parent_id`   = 431200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 431221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沅陵县',
    `parent_id`   = 431200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 431222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '辰溪县',
    `parent_id`   = 431200,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 431223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '溆浦县',
    `parent_id`   = 431200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 431224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '会同县',
    `parent_id`   = 431200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 431225;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '麻阳苗族自治县',
    `parent_id`   = 431200,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 431226;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新晃侗族自治县',
    `parent_id`   = 431200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 431227;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芷江侗族自治县',
    `parent_id`   = 431200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 431228;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '靖州苗族侗族自治县',
    `parent_id`   = 431200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 431229;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '通道侗族自治县',
    `parent_id`   = 431200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 431230;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '怀化市洪江管理区',
    `parent_id`   = 431200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 431271;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洪江市',
    `parent_id`   = 431200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 431281;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '娄底市',
    `parent_id`   = 430000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 431300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '娄星区',
    `parent_id`   = 431300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 431302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '双峰县',
    `parent_id`   = 431300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 431321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新化县',
    `parent_id`   = 431300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 431322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '冷水江市',
    `parent_id`   = 431300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 431381;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '涟源市',
    `parent_id`   = 431300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 431382;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '湘西土家族苗族自治州',
    `parent_id`   = 430000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 433100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吉首市',
    `parent_id`   = 433100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 433101;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泸溪县',
    `parent_id`   = 433100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 433122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凤凰县',
    `parent_id`   = 433100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 433123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '花垣县',
    `parent_id`   = 433100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 433124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '保靖县',
    `parent_id`   = 433100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 433125;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '古丈县',
    `parent_id`   = 433100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 433126;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永顺县',
    `parent_id`   = 433100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 433127;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙山县',
    `parent_id`   = 433100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 433130;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '广东省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 440000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '广州市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 440100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '荔湾区',
    `parent_id`   = 440100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 440103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '越秀区',
    `parent_id`   = 440100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 440104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海珠区',
    `parent_id`   = 440100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 440105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天河区',
    `parent_id`   = 440100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 440106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '白云区',
    `parent_id`   = 440100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 440111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黄埔区',
    `parent_id`   = 440100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 440112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '番禺区',
    `parent_id`   = 440100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 440113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '花都区',
    `parent_id`   = 440100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 440114;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南沙区',
    `parent_id`   = 440100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 440115;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '从化区',
    `parent_id`   = 440100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 440117;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '增城区',
    `parent_id`   = 440100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 440118;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '韶关市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 440200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武江区',
    `parent_id`   = 440200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 440203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浈江区',
    `parent_id`   = 440200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 440204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '曲江区',
    `parent_id`   = 440200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 440205;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '始兴县',
    `parent_id`   = 440200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 440222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仁化县',
    `parent_id`   = 440200,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 440224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '翁源县',
    `parent_id`   = 440200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 440229;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乳源瑶族自治县',
    `parent_id`   = 440200,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 440232;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新丰县',
    `parent_id`   = 440200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 440233;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乐昌市',
    `parent_id`   = 440200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 440281;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南雄市',
    `parent_id`   = 440200,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 440282;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '深圳市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 440300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '罗湖区',
    `parent_id`   = 440300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 440303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '福田区',
    `parent_id`   = 440300,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 440304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南山区',
    `parent_id`   = 440300,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 440305;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宝安区',
    `parent_id`   = 440300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 440306;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙岗区',
    `parent_id`   = 440300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 440307;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盐田区',
    `parent_id`   = 440300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 440308;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙华区',
    `parent_id`   = 440300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 440309;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '坪山区',
    `parent_id`   = 440300,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 440310;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '光明区',
    `parent_id`   = 440300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 440311;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '珠海市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 440400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '香洲区',
    `parent_id`   = 440400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 440402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '斗门区',
    `parent_id`   = 440400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 440403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金湾区',
    `parent_id`   = 440400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 440404;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '汕头市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 440500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙湖区',
    `parent_id`   = 440500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 440507;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金平区',
    `parent_id`   = 440500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 440511;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '濠江区',
    `parent_id`   = 440500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 440512;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '潮阳区',
    `parent_id`   = 440500,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 440513;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '潮南区',
    `parent_id`   = 440500,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 440514;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '澄海区',
    `parent_id`   = 440500,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 440515;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南澳县',
    `parent_id`   = 440500,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 440523;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '佛山市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 440600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '禅城区',
    `parent_id`   = 440600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 440604;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南海区',
    `parent_id`   = 440600,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 440605;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '顺德区',
    `parent_id`   = 440600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 440606;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三水区',
    `parent_id`   = 440600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 440607;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高明区',
    `parent_id`   = 440600,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 440608;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '江门市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 440700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蓬江区',
    `parent_id`   = 440700,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 440703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江海区',
    `parent_id`   = 440700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 440704;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新会区',
    `parent_id`   = 440700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 440705;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '台山市',
    `parent_id`   = 440700,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 440781;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '开平市',
    `parent_id`   = 440700,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 440783;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鹤山市',
    `parent_id`   = 440700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 440784;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '恩平市',
    `parent_id`   = 440700,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 440785;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '湛江市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 440800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '赤坎区',
    `parent_id`   = 440800,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 440802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '霞山区',
    `parent_id`   = 440800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 440803;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '坡头区',
    `parent_id`   = 440800,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 440804;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '麻章区',
    `parent_id`   = 440800,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 440811;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '遂溪县',
    `parent_id`   = 440800,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 440823;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '徐闻县',
    `parent_id`   = 440800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 440825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '廉江市',
    `parent_id`   = 440800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 440881;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雷州市',
    `parent_id`   = 440800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 440882;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吴川市',
    `parent_id`   = 440800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 440883;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '经济技术开发区',
    `parent_id`   = 440800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 440890;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '茂名市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 440900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '茂南区',
    `parent_id`   = 440900,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 440902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '电白区',
    `parent_id`   = 440900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 440904;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高州市',
    `parent_id`   = 440900,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 440981;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '化州市',
    `parent_id`   = 440900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 440982;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '信宜市',
    `parent_id`   = 440900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 440983;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '肇庆市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 441200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '端州区',
    `parent_id`   = 441200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 441202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鼎湖区',
    `parent_id`   = 441200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 441203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高要区',
    `parent_id`   = 441200,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 441204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '广宁县',
    `parent_id`   = 441200,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 441223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '怀集县',
    `parent_id`   = 441200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 441224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '封开县',
    `parent_id`   = 441200,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 441225;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '德庆县',
    `parent_id`   = 441200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 441226;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '四会市',
    `parent_id`   = 441200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 441284;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '惠州市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 441300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '惠城区',
    `parent_id`   = 441300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 441302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '惠阳区',
    `parent_id`   = 441300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 441303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '博罗县',
    `parent_id`   = 441300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 441322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '惠东县',
    `parent_id`   = 441300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 441323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙门县',
    `parent_id`   = 441300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 441324;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '梅州市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 441400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '梅江区',
    `parent_id`   = 441400,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 441402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '梅县区',
    `parent_id`   = 441400,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 441403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大埔县',
    `parent_id`   = 441400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 441422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丰顺县',
    `parent_id`   = 441400,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 441423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五华县',
    `parent_id`   = 441400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 441424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平远县',
    `parent_id`   = 441400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 441426;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蕉岭县',
    `parent_id`   = 441400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 441427;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴宁市',
    `parent_id`   = 441400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 441481;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '汕尾市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 441500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '城区',
    `parent_id`   = 441500,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 441502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海丰县',
    `parent_id`   = 441500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 441521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '陆河县',
    `parent_id`   = 441500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 441523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '陆丰市',
    `parent_id`   = 441500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 441581;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '河源市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 441600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '源城区',
    `parent_id`   = 441600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 441602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '紫金县',
    `parent_id`   = 441600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 441621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙川县',
    `parent_id`   = 441600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 441622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '连平县',
    `parent_id`   = 441600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 441623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '和平县',
    `parent_id`   = 441600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 441624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东源县',
    `parent_id`   = 441600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 441625;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '阳江市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 441700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江城区',
    `parent_id`   = 441700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 441702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阳东区',
    `parent_id`   = 441700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 441704;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阳西县',
    `parent_id`   = 441700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 441721;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阳春市',
    `parent_id`   = 441700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 441781;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '清远市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 441800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清城区',
    `parent_id`   = 441800,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 441802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清新区',
    `parent_id`   = 441800,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 441803;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '佛冈县',
    `parent_id`   = 441800,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 441821;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阳山县',
    `parent_id`   = 441800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 441823;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '连山壮族瑶族自治县',
    `parent_id`   = 441800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 441825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '连南瑶族自治县',
    `parent_id`   = 441800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 441826;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '英德市',
    `parent_id`   = 441800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 441881;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '连州市',
    `parent_id`   = 441800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 441882;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '东莞市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 441900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中堂镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 441901;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南城街道',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 441903;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长安镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 441904;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东坑镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 441905;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '樟木头镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 441906;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莞城街道',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 441907;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石龙镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 441908;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桥头镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 441909;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万江街道',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 441910;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '麻涌镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 441911;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '虎门镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 441912;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '谢岗镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 441913;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石碣镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 441914;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '茶山镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 441915;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东城街道',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 441916;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洪梅镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 441917;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '道滘镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 441918;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高埗镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 441919;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '企石镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 441920;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凤岗镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 441921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大岭山镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 441922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '松山湖',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 441923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清溪镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 441924;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '望牛墩镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 441925;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '厚街镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 441926;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '常平镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 441927;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '寮步镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 441928;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石排镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 441929;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '横沥镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 441930;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '塘厦镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 441931;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黄江镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 441932;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大朗镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 441933;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东莞港',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 441934;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东莞生态园',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 441935;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沙田镇',
    `parent_id`   = 441900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 441990;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '中山市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 442000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南头镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 442001;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '神湾镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 442002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东凤镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 442003;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五桂山街道',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 442004;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黄圃镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 442005;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '小榄镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 442006;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石岐街道',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 442007;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '横栏镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 442008;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三角镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 442009;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三乡镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 442010;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '港口镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 442011;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沙溪镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 442012;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '板芙镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 442013;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东升镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 442015;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阜沙镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 442016;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '民众镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 442017;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东区街道',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 442018;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '火炬开发区街道办事处',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 442019;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西区街道',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 442020;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南区街道',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 442021;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '古镇镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 442022;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '坦洲镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 442023;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大涌镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 442024;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南朗镇',
    `parent_id`   = 442000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 442025;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '潮州市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 445100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湘桥区',
    `parent_id`   = 445100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 445102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '潮安区',
    `parent_id`   = 445100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 445103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '饶平县',
    `parent_id`   = 445100,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 445122;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '揭阳市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 445200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '榕城区',
    `parent_id`   = 445200,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 445202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '揭东区',
    `parent_id`   = 445200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 445203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '揭西县',
    `parent_id`   = 445200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 445222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '惠来县',
    `parent_id`   = 445200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 445224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '普宁市',
    `parent_id`   = 445200,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 445281;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '云浮市',
    `parent_id`   = 440000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 445300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '云城区',
    `parent_id`   = 445300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 445302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '云安区',
    `parent_id`   = 445300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 445303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新兴县',
    `parent_id`   = 445300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 445321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '郁南县',
    `parent_id`   = 445300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 445322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '罗定市',
    `parent_id`   = 445300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 445381;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '广西自治区',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 450000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '南宁市',
    `parent_id`   = 450000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 450100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴宁区',
    `parent_id`   = 450100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 450102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青秀区',
    `parent_id`   = 450100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 450103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江南区',
    `parent_id`   = 450100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 450105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西乡塘区',
    `parent_id`   = 450100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 450107;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '良庆区',
    `parent_id`   = 450100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 450108;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '邕宁区',
    `parent_id`   = 450100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 450109;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武鸣区',
    `parent_id`   = 450100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 450110;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '隆安县',
    `parent_id`   = 450100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 450123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '马山县',
    `parent_id`   = 450100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 450124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '上林县',
    `parent_id`   = 450100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 450125;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宾阳县',
    `parent_id`   = 450100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 450126;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '横州市',
    `parent_id`   = 450100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 450181;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '柳州市',
    `parent_id`   = 450000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 450200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '城中区',
    `parent_id`   = 450200,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 450202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鱼峰区',
    `parent_id`   = 450200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 450203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '柳南区',
    `parent_id`   = 450200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 450204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '柳北区',
    `parent_id`   = 450200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 450205;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '柳江区',
    `parent_id`   = 450200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 450206;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '柳城县',
    `parent_id`   = 450200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 450222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鹿寨县',
    `parent_id`   = 450200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 450223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '融安县',
    `parent_id`   = 450200,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 450224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '融水苗族自治县',
    `parent_id`   = 450200,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 450225;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三江侗族自治县',
    `parent_id`   = 450200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 450226;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '桂林市',
    `parent_id`   = 450000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 450300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '秀峰区',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 450302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '叠彩区',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 450303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '象山区',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 450304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '七星区',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 450305;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雁山区',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 450311;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临桂区',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 450312;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阳朔县',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 450321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '灵川县',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 450323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '全州县',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 450324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴安县',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 450325;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永福县',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 450326;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '灌阳县',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 450327;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙胜各族自治县',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 450328;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '资源县',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 450329;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平乐县',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 450330;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '恭城瑶族自治县',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 450332;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '荔浦市',
    `parent_id`   = 450300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 450381;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '梧州市',
    `parent_id`   = 450000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 450400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万秀区',
    `parent_id`   = 450400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 450403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长洲区',
    `parent_id`   = 450400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 450405;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙圩区',
    `parent_id`   = 450400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 450406;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '苍梧县',
    `parent_id`   = 450400,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 450421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '藤县',
    `parent_id`   = 450400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 450422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蒙山县',
    `parent_id`   = 450400,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 450423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岑溪市',
    `parent_id`   = 450400,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 450481;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '北海市',
    `parent_id`   = 450000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 450500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海城区',
    `parent_id`   = 450500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 450502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '银海区',
    `parent_id`   = 450500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 450503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铁山港区',
    `parent_id`   = 450500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 450512;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '合浦县',
    `parent_id`   = 450500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 450521;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '防城港市',
    `parent_id`   = 450000,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 450600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '港口区',
    `parent_id`   = 450600,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 450602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '防城区',
    `parent_id`   = 450600,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 450603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '上思县',
    `parent_id`   = 450600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 450621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东兴市',
    `parent_id`   = 450600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 450681;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '钦州市',
    `parent_id`   = 450000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 450700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '钦南区',
    `parent_id`   = 450700,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 450702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '钦北区',
    `parent_id`   = 450700,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 450703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '灵山县',
    `parent_id`   = 450700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 450721;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浦北县',
    `parent_id`   = 450700,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 450722;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '贵港市',
    `parent_id`   = 450000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 450800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '港北区',
    `parent_id`   = 450800,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 450802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '港南区',
    `parent_id`   = 450800,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 450803;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '覃塘区',
    `parent_id`   = 450800,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 450804;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平南县',
    `parent_id`   = 450800,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 450821;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桂平市',
    `parent_id`   = 450800,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 450881;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '玉林市',
    `parent_id`   = 450000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 450900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玉州区',
    `parent_id`   = 450900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 450902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '福绵区',
    `parent_id`   = 450900,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 450903;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '容县',
    `parent_id`   = 450900,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 450921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '陆川县',
    `parent_id`   = 450900,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 450922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '博白县',
    `parent_id`   = 450900,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 450923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴业县',
    `parent_id`   = 450900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 450924;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北流市',
    `parent_id`   = 450900,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 450981;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '百色市',
    `parent_id`   = 450000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 451000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '右江区',
    `parent_id`   = 451000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 451002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '田阳区',
    `parent_id`   = 451000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 451003;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '田东县',
    `parent_id`   = 451000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 451022;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '德保县',
    `parent_id`   = 451000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 451024;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '那坡县',
    `parent_id`   = 451000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 451026;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凌云县',
    `parent_id`   = 451000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 451027;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乐业县',
    `parent_id`   = 451000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 451028;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '田林县',
    `parent_id`   = 451000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 451029;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西林县',
    `parent_id`   = 451000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 451030;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '隆林各族自治县',
    `parent_id`   = 451000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 451031;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '靖西市',
    `parent_id`   = 451000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 451081;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平果市',
    `parent_id`   = 451000,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 451082;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '贺州市',
    `parent_id`   = 450000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 451100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '八步区',
    `parent_id`   = 451100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 451102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平桂区',
    `parent_id`   = 451100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 451103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昭平县',
    `parent_id`   = 451100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 451121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '钟山县',
    `parent_id`   = 451100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 451122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '富川瑶族自治县',
    `parent_id`   = 451100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 451123;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '河池市',
    `parent_id`   = 450000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 451200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金城江区',
    `parent_id`   = 451200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 451202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宜州区',
    `parent_id`   = 451200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 451203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南丹县',
    `parent_id`   = 451200,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 451221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天峨县',
    `parent_id`   = 451200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 451222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凤山县',
    `parent_id`   = 451200,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 451223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东兰县',
    `parent_id`   = 451200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 451224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '罗城仫佬族自治县',
    `parent_id`   = 451200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 451225;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '环江毛南族自治县',
    `parent_id`   = 451200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 451226;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巴马瑶族自治县',
    `parent_id`   = 451200,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 451227;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '都安瑶族自治县',
    `parent_id`   = 451200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 451228;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大化瑶族自治县',
    `parent_id`   = 451200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 451229;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '来宾市',
    `parent_id`   = 450000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 451300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴宾区',
    `parent_id`   = 451300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 451302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '忻城县',
    `parent_id`   = 451300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 451321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '象州县',
    `parent_id`   = 451300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 451322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武宣县',
    `parent_id`   = 451300,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 451323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金秀瑶族自治县',
    `parent_id`   = 451300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 451324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '合山市',
    `parent_id`   = 451300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 451381;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '崇左市',
    `parent_id`   = 450000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 451400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江州区',
    `parent_id`   = 451400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 451402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '扶绥县',
    `parent_id`   = 451400,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 451421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁明县',
    `parent_id`   = 451400,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 451422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙州县',
    `parent_id`   = 451400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 451423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大新县',
    `parent_id`   = 451400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 451424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天等县',
    `parent_id`   = 451400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 451425;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凭祥市',
    `parent_id`   = 451400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 451481;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '海南省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 460000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '海口市',
    `parent_id`   = 460000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 460100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '秀英区',
    `parent_id`   = 460100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 460105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙华区',
    `parent_id`   = 460100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 460106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '琼山区',
    `parent_id`   = 460100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 460107;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '美兰区',
    `parent_id`   = 460100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 460108;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '三亚市',
    `parent_id`   = 460000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 460200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海棠区',
    `parent_id`   = 460200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 460202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吉阳区',
    `parent_id`   = 460200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 460203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天涯区',
    `parent_id`   = 460200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 460204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '崖州区',
    `parent_id`   = 460200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 460205;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '三沙市',
    `parent_id`   = 460000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 460300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西沙群岛',
    `parent_id`   = 460300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 460321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南沙群岛',
    `parent_id`   = 460300,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 460322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中沙群岛的岛礁及其海域',
    `parent_id`   = 460300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 460323;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '儋州市',
    `parent_id`   = 460000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 460400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '那大镇',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 460401;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '和庆镇',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 460402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南丰镇',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 460403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大成镇',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 460404;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雅星镇',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 460405;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兰洋镇',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 460406;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '光村镇',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 460407;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '木棠镇',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 460408;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海头镇',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 460409;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '峨蔓镇',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 460410;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '王五镇',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 460411;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '白马井镇',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 460412;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中和镇',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 460413;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '排浦镇',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 460414;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东成镇',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 460415;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新州镇',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 460416;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洋浦经济开发区',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 460417;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '华南热作学院',
    `parent_id`   = 460400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 460418;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '省直辖县',
    `parent_id`   = 460000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 469000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五指山市',
    `parent_id`   = 469000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 469001;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '琼海市',
    `parent_id`   = 469000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 469002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '文昌市',
    `parent_id`   = 469000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 469005;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万宁市',
    `parent_id`   = 469000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 469006;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东方市',
    `parent_id`   = 469000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 469007;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '定安县',
    `parent_id`   = 469000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 469021;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '屯昌县',
    `parent_id`   = 469000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 469022;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '澄迈县',
    `parent_id`   = 469000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 469023;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临高县',
    `parent_id`   = 469000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 469024;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '白沙黎族自治县',
    `parent_id`   = 469000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 469025;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昌江黎族自治县',
    `parent_id`   = 469000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 469026;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乐东黎族自治县',
    `parent_id`   = 469000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 469027;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '陵水黎族自治县',
    `parent_id`   = 469000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 469028;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '保亭黎族苗族自治县',
    `parent_id`   = 469000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 469029;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '琼中黎族苗族自治县',
    `parent_id`   = 469000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 469030;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '重庆市',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 500000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '重庆市',
    `parent_id`   = 500000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 500100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万州区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 500101;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '涪陵区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 500102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '渝中区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 500103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大渡口区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 500104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江北区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 500105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沙坪坝区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 500106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '九龙坡区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 500107;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南岸区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 500108;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北碚区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 500109;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '綦江区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 500110;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大足区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 500111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '渝北区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 500112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巴南区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 500113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黔江区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 500114;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长寿区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 500115;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江津区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 500116;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '合川区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 500117;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永川区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 500118;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南川区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 500119;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '璧山区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 500120;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铜梁区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 500151;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '潼南区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 500152;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '荣昌区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 500153;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '开州区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 500154;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '梁平区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 500155;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武隆区',
    `parent_id`   = 500100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 500156;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '县',
    `parent_id`   = 500000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 500200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '城口县',
    `parent_id`   = 500200,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 500229;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丰都县',
    `parent_id`   = 500200,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 500230;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '垫江县',
    `parent_id`   = 500200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 500231;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '忠县',
    `parent_id`   = 500200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 500233;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '云阳县',
    `parent_id`   = 500200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 500235;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '奉节县',
    `parent_id`   = 500200,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 500236;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巫山县',
    `parent_id`   = 500200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 500237;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巫溪县',
    `parent_id`   = 500200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 500238;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石柱土家族自治县',
    `parent_id`   = 500200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 500240;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '秀山土家族苗族自治县',
    `parent_id`   = 500200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 500241;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '酉阳土家族苗族自治县',
    `parent_id`   = 500200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 500242;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '彭水苗族土家族自治县',
    `parent_id`   = 500200,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 500243;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '四川省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 510000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '成都市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 510100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '锦江区',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 510104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青羊区',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 510105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金牛区',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 510106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武侯区',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 510107;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '成华区',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 510108;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙泉驿区',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 510112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青白江区',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 510113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新都区',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 510114;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '温江区',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 510115;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '双流区',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 510116;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '郫都区',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 510117;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新津区',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 510118;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金堂县',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 510121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大邑县',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 510129;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蒲江县',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 510131;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '都江堰市',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 510181;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '彭州市',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 510182;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '邛崃市',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 510183;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '崇州市',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 510184;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '简阳市',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 510185;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高新区',
    `parent_id`   = 510100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 510191;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '自贡市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 510300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '自流井区',
    `parent_id`   = 510300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 510302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '贡井区',
    `parent_id`   = 510300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 510303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大安区',
    `parent_id`   = 510300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 510304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沿滩区',
    `parent_id`   = 510300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 510311;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '荣县',
    `parent_id`   = 510300,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 510321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '富顺县',
    `parent_id`   = 510300,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 510322;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '攀枝花市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 510400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东区',
    `parent_id`   = 510400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 510402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西区',
    `parent_id`   = 510400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 510403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仁和区',
    `parent_id`   = 510400,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 510411;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '米易县',
    `parent_id`   = 510400,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 510421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盐边县',
    `parent_id`   = 510400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 510422;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '泸州市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 510500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江阳区',
    `parent_id`   = 510500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 510502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '纳溪区',
    `parent_id`   = 510500,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 510503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙马潭区',
    `parent_id`   = 510500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 510504;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泸县',
    `parent_id`   = 510500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 510521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '合江县',
    `parent_id`   = 510500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 510522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '叙永县',
    `parent_id`   = 510500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 510524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '古蔺县',
    `parent_id`   = 510500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 510525;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '德阳市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 510600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '旌阳区',
    `parent_id`   = 510600,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 510603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '罗江区',
    `parent_id`   = 510600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 510604;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中江县',
    `parent_id`   = 510600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 510623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '广汉市',
    `parent_id`   = 510600,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 510681;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '什邡市',
    `parent_id`   = 510600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 510682;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '绵竹市',
    `parent_id`   = 510600,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 510683;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '绵阳市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 510700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '涪城区',
    `parent_id`   = 510700,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 510703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '游仙区',
    `parent_id`   = 510700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 510704;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安州区',
    `parent_id`   = 510700,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 510705;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三台县',
    `parent_id`   = 510700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 510722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盐亭县',
    `parent_id`   = 510700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 510723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '梓潼县',
    `parent_id`   = 510700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 510725;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北川羌族自治县',
    `parent_id`   = 510700,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 510726;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平武县',
    `parent_id`   = 510700,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 510727;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江油市',
    `parent_id`   = 510700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 510781;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高新区',
    `parent_id`   = 510700,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 510791;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '广元市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 510800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '利州区',
    `parent_id`   = 510800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 510802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昭化区',
    `parent_id`   = 510800,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 510811;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '朝天区',
    `parent_id`   = 510800,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 510812;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '旺苍县',
    `parent_id`   = 510800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 510821;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青川县',
    `parent_id`   = 510800,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 510822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '剑阁县',
    `parent_id`   = 510800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 510823;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '苍溪县',
    `parent_id`   = 510800,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 510824;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '遂宁市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 510900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '船山区',
    `parent_id`   = 510900,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 510903;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安居区',
    `parent_id`   = 510900,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 510904;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蓬溪县',
    `parent_id`   = 510900,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 510921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大英县',
    `parent_id`   = 510900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 510923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '射洪市',
    `parent_id`   = 510900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 510981;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '内江市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 511000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '市中区',
    `parent_id`   = 511000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 511002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东兴区',
    `parent_id`   = 511000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 511011;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '威远县',
    `parent_id`   = 511000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 511024;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '资中县',
    `parent_id`   = 511000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 511025;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '隆昌市',
    `parent_id`   = 511000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 511083;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '乐山市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 511100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '市中区',
    `parent_id`   = 511100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 511102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沙湾区',
    `parent_id`   = 511100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 511111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五通桥区',
    `parent_id`   = 511100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 511112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金口河区',
    `parent_id`   = 511100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 511113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '犍为县',
    `parent_id`   = 511100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 511123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '井研县',
    `parent_id`   = 511100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 511124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '夹江县',
    `parent_id`   = 511100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 511126;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沐川县',
    `parent_id`   = 511100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 511129;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '峨边彝族自治县',
    `parent_id`   = 511100,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 511132;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '马边彝族自治县',
    `parent_id`   = 511100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 511133;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '峨眉山市',
    `parent_id`   = 511100,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 511181;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '南充市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 511300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '顺庆区',
    `parent_id`   = 511300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 511302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高坪区',
    `parent_id`   = 511300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 511303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '嘉陵区',
    `parent_id`   = 511300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 511304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南部县',
    `parent_id`   = 511300,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 511321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '营山县',
    `parent_id`   = 511300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 511322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蓬安县',
    `parent_id`   = 511300,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 511323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仪陇县',
    `parent_id`   = 511300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 511324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西充县',
    `parent_id`   = 511300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 511325;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阆中市',
    `parent_id`   = 511300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 511381;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '眉山市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 511400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东坡区',
    `parent_id`   = 511400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 511402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '彭山区',
    `parent_id`   = 511400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 511403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仁寿县',
    `parent_id`   = 511400,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 511421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洪雅县',
    `parent_id`   = 511400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 511423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丹棱县',
    `parent_id`   = 511400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 511424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青神县',
    `parent_id`   = 511400,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 511425;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '宜宾市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 511500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '翠屏区',
    `parent_id`   = 511500,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 511502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南溪区',
    `parent_id`   = 511500,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 511503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '叙州区',
    `parent_id`   = 511500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 511504;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江安县',
    `parent_id`   = 511500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 511523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长宁县',
    `parent_id`   = 511500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 511524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高县',
    `parent_id`   = 511500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 511525;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '珙县',
    `parent_id`   = 511500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 511526;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '筠连县',
    `parent_id`   = 511500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 511527;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴文县',
    `parent_id`   = 511500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 511528;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '屏山县',
    `parent_id`   = 511500,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 511529;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '广安市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 511600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '广安区',
    `parent_id`   = 511600,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 511602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '前锋区',
    `parent_id`   = 511600,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 511603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岳池县',
    `parent_id`   = 511600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 511621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武胜县',
    `parent_id`   = 511600,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 511622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '邻水县',
    `parent_id`   = 511600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 511623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '华蓥市',
    `parent_id`   = 511600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 511681;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '达州市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 511700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '通川区',
    `parent_id`   = 511700,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 511702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '达川区',
    `parent_id`   = 511700,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 511703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宣汉县',
    `parent_id`   = 511700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 511722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '开江县',
    `parent_id`   = 511700,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 511723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大竹县',
    `parent_id`   = 511700,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 511724;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '渠县',
    `parent_id`   = 511700,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 511725;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万源市',
    `parent_id`   = 511700,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 511781;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '雅安市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 511800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雨城区',
    `parent_id`   = 511800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 511802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '名山区',
    `parent_id`   = 511800,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 511803;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '荥经县',
    `parent_id`   = 511800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 511822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汉源县',
    `parent_id`   = 511800,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 511823;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石棉县',
    `parent_id`   = 511800,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 511824;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天全县',
    `parent_id`   = 511800,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 511825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芦山县',
    `parent_id`   = 511800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 511826;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宝兴县',
    `parent_id`   = 511800,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 511827;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '巴中市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 511900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巴州区',
    `parent_id`   = 511900,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 511902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '恩阳区',
    `parent_id`   = 511900,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 511903;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '通江县',
    `parent_id`   = 511900,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 511921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南江县',
    `parent_id`   = 511900,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 511922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平昌县',
    `parent_id`   = 511900,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 511923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巴中经济开发区',
    `parent_id`   = 511900,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 511971;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '资阳市',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 512000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雁江区',
    `parent_id`   = 512000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 512002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安岳县',
    `parent_id`   = 512000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 512021;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乐至县',
    `parent_id`   = 512000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 512022;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '阿坝藏族羌族自治州',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 513200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '马尔康市',
    `parent_id`   = 513200,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 513201;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汶川县',
    `parent_id`   = 513200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 513221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '理县',
    `parent_id`   = 513200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 513222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '茂县',
    `parent_id`   = 513200,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 513223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '松潘县',
    `parent_id`   = 513200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 513224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '九寨沟县',
    `parent_id`   = 513200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 513225;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金川县',
    `parent_id`   = 513200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 513226;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '小金县',
    `parent_id`   = 513200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 513227;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黑水县',
    `parent_id`   = 513200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 513228;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '壤塘县',
    `parent_id`   = 513200,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 513230;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿坝县',
    `parent_id`   = 513200,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 513231;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '若尔盖县',
    `parent_id`   = 513200,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 513232;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '红原县',
    `parent_id`   = 513200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 513233;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '甘孜藏族自治州',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 513300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '康定市',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 513301;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泸定县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 513322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丹巴县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 513323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '九龙县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 513324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雅江县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 513325;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '道孚县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 513326;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '炉霍县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 513327;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '甘孜县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 513328;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新龙县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 513329;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '德格县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 513330;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '白玉县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 513331;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石渠县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 513332;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '色达县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 513333;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '理塘县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 513334;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巴塘县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 513335;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乡城县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 513336;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '稻城县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 513337;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '得荣县',
    `parent_id`   = 513300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 513338;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '凉山彝族自治州',
    `parent_id`   = 510000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 513400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西昌市',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 513401;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '会理市',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 513402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '木里藏族自治县',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 513422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盐源县',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 513423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '德昌县',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 513424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '会东县',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 513426;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁南县',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 513427;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '普格县',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 513428;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '布拖县',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 513429;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金阳县',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 513430;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昭觉县',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 513431;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '喜德县',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 513432;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '冕宁县',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 513433;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '越西县',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 513434;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '甘洛县',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 513435;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '美姑县',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 513436;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雷波县',
    `parent_id`   = 513400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 513437;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '贵州省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 520000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '贵阳市',
    `parent_id`   = 520000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 520100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南明区',
    `parent_id`   = 520100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 520102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '云岩区',
    `parent_id`   = 520100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 520103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '花溪区',
    `parent_id`   = 520100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 520111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌当区',
    `parent_id`   = 520100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 520112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '白云区',
    `parent_id`   = 520100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 520113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '观山湖区',
    `parent_id`   = 520100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 520115;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '开阳县',
    `parent_id`   = 520100,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 520121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '息烽县',
    `parent_id`   = 520100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 520122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '修文县',
    `parent_id`   = 520100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 520123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清镇市',
    `parent_id`   = 520100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 520181;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '六盘水市',
    `parent_id`   = 520000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 520200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '钟山区',
    `parent_id`   = 520200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 520201;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '六枝特区',
    `parent_id`   = 520200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 520203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '水城区',
    `parent_id`   = 520200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 520204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盘州市',
    `parent_id`   = 520200,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 520281;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '遵义市',
    `parent_id`   = 520000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 520300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '红花岗区',
    `parent_id`   = 520300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 520302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汇川区',
    `parent_id`   = 520300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 520303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '播州区',
    `parent_id`   = 520300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 520304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桐梓县',
    `parent_id`   = 520300,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 520322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '绥阳县',
    `parent_id`   = 520300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 520323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '正安县',
    `parent_id`   = 520300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 520324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '道真仡佬族苗族自治县',
    `parent_id`   = 520300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 520325;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '务川仡佬族苗族自治县',
    `parent_id`   = 520300,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 520326;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凤冈县',
    `parent_id`   = 520300,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 520327;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湄潭县',
    `parent_id`   = 520300,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 520328;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '余庆县',
    `parent_id`   = 520300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 520329;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '习水县',
    `parent_id`   = 520300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 520330;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '赤水市',
    `parent_id`   = 520300,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 520381;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仁怀市',
    `parent_id`   = 520300,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 520382;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '安顺市',
    `parent_id`   = 520000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 520400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西秀区',
    `parent_id`   = 520400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 520402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平坝区',
    `parent_id`   = 520400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 520403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '普定县',
    `parent_id`   = 520400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 520422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '镇宁布依族苗族自治县',
    `parent_id`   = 520400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 520423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '关岭布依族苗族自治县',
    `parent_id`   = 520400,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 520424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '紫云苗族布依族自治县',
    `parent_id`   = 520400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 520425;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '毕节市',
    `parent_id`   = 520000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 520500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '七星关区',
    `parent_id`   = 520500,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 520502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大方县',
    `parent_id`   = 520500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 520521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金沙县',
    `parent_id`   = 520500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 520523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '织金县',
    `parent_id`   = 520500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 520524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '纳雍县',
    `parent_id`   = 520500,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 520525;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '威宁彝族回族苗族自治县',
    `parent_id`   = 520500,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 520526;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '赫章县',
    `parent_id`   = 520500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 520527;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黔西市',
    `parent_id`   = 520500,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 520581;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '铜仁市',
    `parent_id`   = 520000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 520600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '碧江区',
    `parent_id`   = 520600,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 520602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万山区',
    `parent_id`   = 520600,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 520603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江口县',
    `parent_id`   = 520600,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 520621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玉屏侗族自治县',
    `parent_id`   = 520600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 520622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石阡县',
    `parent_id`   = 520600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 520623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '思南县',
    `parent_id`   = 520600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 520624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '印江土家族苗族自治县',
    `parent_id`   = 520600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 520625;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '德江县',
    `parent_id`   = 520600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 520626;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沿河土家族自治县',
    `parent_id`   = 520600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 520627;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '松桃苗族自治县',
    `parent_id`   = 520600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 520628;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '黔西南布依族苗族自治州',
    `parent_id`   = 520000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 522300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴义市',
    `parent_id`   = 522300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 522301;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴仁市',
    `parent_id`   = 522300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 522302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '普安县',
    `parent_id`   = 522300,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 522323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '晴隆县',
    `parent_id`   = 522300,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 522324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '贞丰县',
    `parent_id`   = 522300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 522325;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '望谟县',
    `parent_id`   = 522300,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 522326;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '册亨县',
    `parent_id`   = 522300,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 522327;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安龙县',
    `parent_id`   = 522300,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 522328;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '黔东南苗族侗族自治州',
    `parent_id`   = 520000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 522600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凯里市',
    `parent_id`   = 522600,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 522601;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黄平县',
    `parent_id`   = 522600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 522622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '施秉县',
    `parent_id`   = 522600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 522623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三穗县',
    `parent_id`   = 522600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 522624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '镇远县',
    `parent_id`   = 522600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 522625;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岑巩县',
    `parent_id`   = 522600,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 522626;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天柱县',
    `parent_id`   = 522600,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 522627;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '锦屏县',
    `parent_id`   = 522600,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 522628;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '剑河县',
    `parent_id`   = 522600,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 522629;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '台江县',
    `parent_id`   = 522600,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 522630;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黎平县',
    `parent_id`   = 522600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 522631;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '榕江县',
    `parent_id`   = 522600,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 522632;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '从江县',
    `parent_id`   = 522600,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 522633;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雷山县',
    `parent_id`   = 522600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 522634;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '麻江县',
    `parent_id`   = 522600,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 522635;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丹寨县',
    `parent_id`   = 522600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 522636;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '黔南布依族苗族自治州',
    `parent_id`   = 520000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 522700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '都匀市',
    `parent_id`   = 522700,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 522701;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '福泉市',
    `parent_id`   = 522700,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 522702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '荔波县',
    `parent_id`   = 522700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 522722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '贵定县',
    `parent_id`   = 522700,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 522723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '瓮安县',
    `parent_id`   = 522700,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 522725;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '独山县',
    `parent_id`   = 522700,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 522726;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平塘县',
    `parent_id`   = 522700,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 522727;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '罗甸县',
    `parent_id`   = 522700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 522728;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长顺县',
    `parent_id`   = 522700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 522729;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙里县',
    `parent_id`   = 522700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 522730;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '惠水县',
    `parent_id`   = 522700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 522731;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三都水族自治县',
    `parent_id`   = 522700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 522732;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '云南省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 530000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '昆明市',
    `parent_id`   = 530000,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 530100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五华区',
    `parent_id`   = 530100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 530102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盘龙区',
    `parent_id`   = 530100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 530103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '官渡区',
    `parent_id`   = 530100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 530111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西山区',
    `parent_id`   = 530100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 530112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东川区',
    `parent_id`   = 530100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 530113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '呈贡区',
    `parent_id`   = 530100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 530114;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '晋宁区',
    `parent_id`   = 530100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 530115;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '富民县',
    `parent_id`   = 530100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 530124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宜良县',
    `parent_id`   = 530100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 530125;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石林彝族自治县',
    `parent_id`   = 530100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 530126;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '嵩明县',
    `parent_id`   = 530100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 530127;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '禄劝彝族苗族自治县',
    `parent_id`   = 530100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 530128;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '寻甸回族彝族自治县',
    `parent_id`   = 530100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 530129;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安宁市',
    `parent_id`   = 530100,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 530181;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '曲靖市',
    `parent_id`   = 530000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 530300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '麒麟区',
    `parent_id`   = 530300,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 530302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沾益区',
    `parent_id`   = 530300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 530303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '马龙区',
    `parent_id`   = 530300,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 530304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '陆良县',
    `parent_id`   = 530300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 530322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '师宗县',
    `parent_id`   = 530300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 530323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '罗平县',
    `parent_id`   = 530300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 530324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '富源县',
    `parent_id`   = 530300,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 530325;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '会泽县',
    `parent_id`   = 530300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 530326;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宣威市',
    `parent_id`   = 530300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 530381;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '玉溪市',
    `parent_id`   = 530000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 530400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '红塔区',
    `parent_id`   = 530400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 530402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江川区',
    `parent_id`   = 530400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 530403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '通海县',
    `parent_id`   = 530400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 530423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '华宁县',
    `parent_id`   = 530400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 530424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '易门县',
    `parent_id`   = 530400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 530425;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '峨山彝族自治县',
    `parent_id`   = 530400,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 530426;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新平彝族傣族自治县',
    `parent_id`   = 530400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 530427;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '元江哈尼族彝族傣族自治县',
    `parent_id`   = 530400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 530428;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '澄江市',
    `parent_id`   = 530400,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 530481;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '保山市',
    `parent_id`   = 530000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 530500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '隆阳区',
    `parent_id`   = 530500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 530502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '施甸县',
    `parent_id`   = 530500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 530521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙陵县',
    `parent_id`   = 530500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 530523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昌宁县',
    `parent_id`   = 530500,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 530524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '腾冲市',
    `parent_id`   = 530500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 530581;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '昭通市',
    `parent_id`   = 530000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 530600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昭阳区',
    `parent_id`   = 530600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 530602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鲁甸县',
    `parent_id`   = 530600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 530621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巧家县',
    `parent_id`   = 530600,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 530622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盐津县',
    `parent_id`   = 530600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 530623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大关县',
    `parent_id`   = 530600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 530624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永善县',
    `parent_id`   = 530600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 530625;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '绥江县',
    `parent_id`   = 530600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 530626;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '镇雄县',
    `parent_id`   = 530600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 530627;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '彝良县',
    `parent_id`   = 530600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 530628;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '威信县',
    `parent_id`   = 530600,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 530629;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '水富市',
    `parent_id`   = 530600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 530681;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '丽江市',
    `parent_id`   = 530000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 530700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '古城区',
    `parent_id`   = 530700,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 530702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玉龙纳西族自治县',
    `parent_id`   = 530700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 530721;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永胜县',
    `parent_id`   = 530700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 530722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '华坪县',
    `parent_id`   = 530700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 530723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁蒗彝族自治县',
    `parent_id`   = 530700,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 530724;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '普洱市',
    `parent_id`   = 530000,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 530800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '思茅区',
    `parent_id`   = 530800,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 530802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁洱哈尼族彝族自治县',
    `parent_id`   = 530800,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 530821;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '墨江哈尼族自治县',
    `parent_id`   = 530800,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 530822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '景东彝族自治县',
    `parent_id`   = 530800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 530823;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '景谷傣族彝族自治县',
    `parent_id`   = 530800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 530824;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '镇沅彝族哈尼族拉祜族自治县',
    `parent_id`   = 530800,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 530825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江城哈尼族彝族自治县',
    `parent_id`   = 530800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 530826;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '孟连傣族拉祜族佤族自治县',
    `parent_id`   = 530800,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 530827;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '澜沧拉祜族自治县',
    `parent_id`   = 530800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 530828;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西盟佤族自治县',
    `parent_id`   = 530800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 530829;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '临沧市',
    `parent_id`   = 530000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 530900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临翔区',
    `parent_id`   = 530900,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 530902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凤庆县',
    `parent_id`   = 530900,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 530921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '云县',
    `parent_id`   = 530900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 530922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永德县',
    `parent_id`   = 530900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 530923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '镇康县',
    `parent_id`   = 530900,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 530924;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '双江拉祜族佤族布朗族傣族自治县',
    `parent_id`   = 530900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 530925;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '耿马傣族佤族自治县',
    `parent_id`   = 530900,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 530926;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沧源佤族自治县',
    `parent_id`   = 530900,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 530927;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '楚雄彝族自治州',
    `parent_id`   = 530000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 532300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '楚雄市',
    `parent_id`   = 532300,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 532301;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '禄丰市',
    `parent_id`   = 532300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 532302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '双柏县',
    `parent_id`   = 532300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 532322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '牟定县',
    `parent_id`   = 532300,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 532323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南华县',
    `parent_id`   = 532300,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 532324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '姚安县',
    `parent_id`   = 532300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 532325;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大姚县',
    `parent_id`   = 532300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 532326;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永仁县',
    `parent_id`   = 532300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 532327;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '元谋县',
    `parent_id`   = 532300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 532328;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武定县',
    `parent_id`   = 532300,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 532329;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '红河哈尼族彝族自治州',
    `parent_id`   = 530000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 532500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '个旧市',
    `parent_id`   = 532500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 532501;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '开远市',
    `parent_id`   = 532500,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 532502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蒙自市',
    `parent_id`   = 532500,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 532503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '弥勒市',
    `parent_id`   = 532500,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 532504;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '屏边苗族自治县',
    `parent_id`   = 532500,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 532523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '建水县',
    `parent_id`   = 532500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 532524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石屏县',
    `parent_id`   = 532500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 532525;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泸西县',
    `parent_id`   = 532500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 532527;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '元阳县',
    `parent_id`   = 532500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 532528;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '红河县',
    `parent_id`   = 532500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 532529;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金平苗族瑶族傣族自治县',
    `parent_id`   = 532500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 532530;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '绿春县',
    `parent_id`   = 532500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 532531;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河口瑶族自治县',
    `parent_id`   = 532500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 532532;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '文山壮族苗族自治州',
    `parent_id`   = 530000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 532600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '文山市',
    `parent_id`   = 532600,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 532601;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '砚山县',
    `parent_id`   = 532600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 532622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西畴县',
    `parent_id`   = 532600,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 532623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '麻栗坡县',
    `parent_id`   = 532600,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 532624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '马关县',
    `parent_id`   = 532600,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 532625;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丘北县',
    `parent_id`   = 532600,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 532626;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '广南县',
    `parent_id`   = 532600,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 532627;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '富宁县',
    `parent_id`   = 532600,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 532628;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '西双版纳傣族自治州',
    `parent_id`   = 530000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 532800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '景洪市',
    `parent_id`   = 532800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 532801;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '勐海县',
    `parent_id`   = 532800,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 532822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '勐腊县',
    `parent_id`   = 532800,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 532823;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '大理白族自治州',
    `parent_id`   = 530000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 532900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大理市',
    `parent_id`   = 532900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 532901;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '漾濞彝族自治县',
    `parent_id`   = 532900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 532922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '祥云县',
    `parent_id`   = 532900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 532923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宾川县',
    `parent_id`   = 532900,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 532924;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '弥渡县',
    `parent_id`   = 532900,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 532925;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南涧彝族自治县',
    `parent_id`   = 532900,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 532926;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巍山彝族回族自治县',
    `parent_id`   = 532900,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 532927;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永平县',
    `parent_id`   = 532900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 532928;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '云龙县',
    `parent_id`   = 532900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 532929;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洱源县',
    `parent_id`   = 532900,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 532930;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '剑川县',
    `parent_id`   = 532900,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 532931;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鹤庆县',
    `parent_id`   = 532900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 532932;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '德宏傣族景颇族自治州',
    `parent_id`   = 530000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 533100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '瑞丽市',
    `parent_id`   = 533100,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 533102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芒市',
    `parent_id`   = 533100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 533103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '梁河县',
    `parent_id`   = 533100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 533122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盈江县',
    `parent_id`   = 533100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 533123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '陇川县',
    `parent_id`   = 533100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 533124;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '怒江傈僳族自治州',
    `parent_id`   = 530000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 533300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泸水市',
    `parent_id`   = 533300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 533301;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '福贡县',
    `parent_id`   = 533300,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 533323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '贡山独龙族怒族自治县',
    `parent_id`   = 533300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 533324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兰坪白族普米族自治县',
    `parent_id`   = 533300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 533325;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '迪庆藏族自治州',
    `parent_id`   = 530000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 533400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '香格里拉市',
    `parent_id`   = 533400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 533401;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '德钦县',
    `parent_id`   = 533400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 533422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '维西傈僳族自治县',
    `parent_id`   = 533400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 533423;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '西藏自治区',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 540000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '拉萨市',
    `parent_id`   = 540000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 540100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '城关区',
    `parent_id`   = 540100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 540102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '堆龙德庆区',
    `parent_id`   = 540100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 540103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '达孜区',
    `parent_id`   = 540100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 540104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '林周县',
    `parent_id`   = 540100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 540121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '当雄县',
    `parent_id`   = 540100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 540122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '尼木县',
    `parent_id`   = 540100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 540123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '曲水县',
    `parent_id`   = 540100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 540124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '墨竹工卡县',
    `parent_id`   = 540100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 540127;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '日喀则市',
    `parent_id`   = 540000,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 540200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桑珠孜区',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 540202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南木林县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 540221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江孜县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 540222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '定日县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 540223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '萨迦县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 540224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '拉孜县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 540225;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昂仁县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 540226;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '谢通门县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 540227;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '白朗县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 540228;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仁布县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 540229;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '康马县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 540230;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '定结县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 540231;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仲巴县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 540232;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '亚东县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 540233;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吉隆县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 540234;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '聂拉木县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 540235;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '萨嘎县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 540236;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岗巴县',
    `parent_id`   = 540200,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 540237;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '昌都市',
    `parent_id`   = 540000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 540300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '卡若区',
    `parent_id`   = 540300,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 540302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '江达县',
    `parent_id`   = 540300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 540321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '贡觉县',
    `parent_id`   = 540300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 540322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '类乌齐县',
    `parent_id`   = 540300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 540323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丁青县',
    `parent_id`   = 540300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 540324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '察雅县',
    `parent_id`   = 540300,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 540325;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '八宿县',
    `parent_id`   = 540300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 540326;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '左贡县',
    `parent_id`   = 540300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 540327;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芒康县',
    `parent_id`   = 540300,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 540328;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洛隆县',
    `parent_id`   = 540300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 540329;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '边坝县',
    `parent_id`   = 540300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 540330;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '林芝市',
    `parent_id`   = 540000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 540400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巴宜区',
    `parent_id`   = 540400,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 540402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '工布江达县',
    `parent_id`   = 540400,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 540421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '墨脱县',
    `parent_id`   = 540400,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 540423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '波密县',
    `parent_id`   = 540400,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 540424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '察隅县',
    `parent_id`   = 540400,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 540425;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '朗县',
    `parent_id`   = 540400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 540426;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '米林市',
    `parent_id`   = 540400,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 540481;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '山南市',
    `parent_id`   = 540000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 540500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乃东区',
    `parent_id`   = 540500,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 540502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '扎囊县',
    `parent_id`   = 540500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 540521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '贡嘎县',
    `parent_id`   = 540500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 540522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桑日县',
    `parent_id`   = 540500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 540523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '琼结县',
    `parent_id`   = 540500,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 540524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '曲松县',
    `parent_id`   = 540500,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 540525;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '措美县',
    `parent_id`   = 540500,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 540526;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洛扎县',
    `parent_id`   = 540500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 540527;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '加查县',
    `parent_id`   = 540500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 540528;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '隆子县',
    `parent_id`   = 540500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 540529;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '浪卡子县',
    `parent_id`   = 540500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 540531;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '错那市',
    `parent_id`   = 540500,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 540581;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '那曲市',
    `parent_id`   = 540000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 540600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '色尼区',
    `parent_id`   = 540600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 540602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '嘉黎县',
    `parent_id`   = 540600,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 540621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '比如县',
    `parent_id`   = 540600,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 540622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '聂荣县',
    `parent_id`   = 540600,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 540623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安多县',
    `parent_id`   = 540600,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 540624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '申扎县',
    `parent_id`   = 540600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 540625;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '索县',
    `parent_id`   = 540600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 540626;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '班戈县',
    `parent_id`   = 540600,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 540627;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巴青县',
    `parent_id`   = 540600,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 540628;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '尼玛县',
    `parent_id`   = 540600,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 540629;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '双湖县',
    `parent_id`   = 540600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 540630;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '阿里地区',
    `parent_id`   = 540000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 542500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '普兰县',
    `parent_id`   = 542500,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 542521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '札达县',
    `parent_id`   = 542500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 542522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '噶尔县',
    `parent_id`   = 542500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 542523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '日土县',
    `parent_id`   = 542500,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 542524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '革吉县',
    `parent_id`   = 542500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 542525;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '改则县',
    `parent_id`   = 542500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 542526;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '措勤县',
    `parent_id`   = 542500,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 542527;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '陕西省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 610000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '西安市',
    `parent_id`   = 610000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 610100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新城区',
    `parent_id`   = 610100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 610102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '碑林区',
    `parent_id`   = 610100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 610103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莲湖区',
    `parent_id`   = 610100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 610104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '灞桥区',
    `parent_id`   = 610100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 610111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '未央区',
    `parent_id`   = 610100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 610112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雁塔区',
    `parent_id`   = 610100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 610113;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阎良区',
    `parent_id`   = 610100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 610114;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临潼区',
    `parent_id`   = 610100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 610115;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长安区',
    `parent_id`   = 610100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 610116;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高陵区',
    `parent_id`   = 610100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 610117;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鄠邑区',
    `parent_id`   = 610100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 610118;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蓝田县',
    `parent_id`   = 610100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 610122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '周至县',
    `parent_id`   = 610100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 610124;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '铜川市',
    `parent_id`   = 610000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 610200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '王益区',
    `parent_id`   = 610200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 610202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '印台区',
    `parent_id`   = 610200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 610203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '耀州区',
    `parent_id`   = 610200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 610204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宜君县',
    `parent_id`   = 610200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 610222;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '宝鸡市',
    `parent_id`   = 610000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 610300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '渭滨区',
    `parent_id`   = 610300,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 610302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金台区',
    `parent_id`   = 610300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 610303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '陈仓区',
    `parent_id`   = 610300,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 610304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凤翔区',
    `parent_id`   = 610300,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 610305;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岐山县',
    `parent_id`   = 610300,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 610323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '扶风县',
    `parent_id`   = 610300,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 610324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '眉县',
    `parent_id`   = 610300,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 610326;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '陇县',
    `parent_id`   = 610300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 610327;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '千阳县',
    `parent_id`   = 610300,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 610328;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '麟游县',
    `parent_id`   = 610300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 610329;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凤县',
    `parent_id`   = 610300,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 610330;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '太白县',
    `parent_id`   = 610300,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 610331;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '咸阳市',
    `parent_id`   = 610000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 610400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '秦都区',
    `parent_id`   = 610400,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 610402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '杨陵区',
    `parent_id`   = 610400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 610403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '渭城区',
    `parent_id`   = 610400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 610404;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三原县',
    `parent_id`   = 610400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 610422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泾阳县',
    `parent_id`   = 610400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 610423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乾县',
    `parent_id`   = 610400,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 610424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '礼泉县',
    `parent_id`   = 610400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 610425;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永寿县',
    `parent_id`   = 610400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 610426;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长武县',
    `parent_id`   = 610400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 610428;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '旬邑县',
    `parent_id`   = 610400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 610429;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '淳化县',
    `parent_id`   = 610400,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 610430;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武功县',
    `parent_id`   = 610400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 610431;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴平市',
    `parent_id`   = 610400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 610481;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '彬州市',
    `parent_id`   = 610400,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 610482;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '渭南市',
    `parent_id`   = 610000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 610500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临渭区',
    `parent_id`   = 610500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 610502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '华州区',
    `parent_id`   = 610500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 610503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '潼关县',
    `parent_id`   = 610500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 610522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大荔县',
    `parent_id`   = 610500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 610523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '合阳县',
    `parent_id`   = 610500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 610524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '澄城县',
    `parent_id`   = 610500,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 610525;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '蒲城县',
    `parent_id`   = 610500,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 610526;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '白水县',
    `parent_id`   = 610500,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 610527;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '富平县',
    `parent_id`   = 610500,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 610528;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '韩城市',
    `parent_id`   = 610500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 610581;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '华阴市',
    `parent_id`   = 610500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 610582;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '延安市',
    `parent_id`   = 610000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 610600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宝塔区',
    `parent_id`   = 610600,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 610602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安塞区',
    `parent_id`   = 610600,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 610603;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '延长县',
    `parent_id`   = 610600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 610621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '延川县',
    `parent_id`   = 610600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 610622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '志丹县',
    `parent_id`   = 610600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 610625;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吴起县',
    `parent_id`   = 610600,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 610626;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '甘泉县',
    `parent_id`   = 610600,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 610627;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '富县',
    `parent_id`   = 610600,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 610628;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洛川县',
    `parent_id`   = 610600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 610629;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宜川县',
    `parent_id`   = 610600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 610630;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黄龙县',
    `parent_id`   = 610600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 610631;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黄陵县',
    `parent_id`   = 610600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 610632;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '子长市',
    `parent_id`   = 610600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 610681;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '汉中市',
    `parent_id`   = 610000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 610700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汉台区',
    `parent_id`   = 610700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 610702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南郑区',
    `parent_id`   = 610700,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 610703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '城固县',
    `parent_id`   = 610700,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 610722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洋县',
    `parent_id`   = 610700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 610723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西乡县',
    `parent_id`   = 610700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 610724;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '勉县',
    `parent_id`   = 610700,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 610725;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁强县',
    `parent_id`   = 610700,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 610726;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '略阳县',
    `parent_id`   = 610700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 610727;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '镇巴县',
    `parent_id`   = 610700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 610728;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '留坝县',
    `parent_id`   = 610700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 610729;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '佛坪县',
    `parent_id`   = 610700,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 610730;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '榆林市',
    `parent_id`   = 610000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 610800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '榆阳区',
    `parent_id`   = 610800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 610802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '横山区',
    `parent_id`   = 610800,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 610803;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '府谷县',
    `parent_id`   = 610800,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 610822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '靖边县',
    `parent_id`   = 610800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 610824;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '定边县',
    `parent_id`   = 610800,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 610825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '绥德县',
    `parent_id`   = 610800,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 610826;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '米脂县',
    `parent_id`   = 610800,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 610827;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '佳县',
    `parent_id`   = 610800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 610828;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吴堡县',
    `parent_id`   = 610800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 610829;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清涧县',
    `parent_id`   = 610800,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 610830;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '子洲县',
    `parent_id`   = 610800,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 610831;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '神木市',
    `parent_id`   = 610800,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 610881;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '安康市',
    `parent_id`   = 610000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 610900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汉滨区',
    `parent_id`   = 610900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 610902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汉阴县',
    `parent_id`   = 610900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 610921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石泉县',
    `parent_id`   = 610900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 610922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁陕县',
    `parent_id`   = 610900,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 610923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '紫阳县',
    `parent_id`   = 610900,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 610924;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岚皋县',
    `parent_id`   = 610900,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 610925;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平利县',
    `parent_id`   = 610900,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 610926;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '镇坪县',
    `parent_id`   = 610900,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 610927;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '白河县',
    `parent_id`   = 610900,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 610929;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '旬阳市',
    `parent_id`   = 610900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 610981;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '商洛市',
    `parent_id`   = 610000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 611000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '商州区',
    `parent_id`   = 611000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 611002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洛南县',
    `parent_id`   = 611000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 611021;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丹凤县',
    `parent_id`   = 611000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 611022;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '商南县',
    `parent_id`   = 611000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 611023;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '山阳县',
    `parent_id`   = 611000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 611024;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '镇安县',
    `parent_id`   = 611000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 611025;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '柞水县',
    `parent_id`   = 611000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 611026;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '甘肃省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 620000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '兰州市',
    `parent_id`   = 620000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 620100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '城关区',
    `parent_id`   = 620100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 620102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '七里河区',
    `parent_id`   = 620100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 620103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西固区',
    `parent_id`   = 620100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 620104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安宁区',
    `parent_id`   = 620100,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 620105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '红古区',
    `parent_id`   = 620100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 620111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永登县',
    `parent_id`   = 620100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 620121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '皋兰县',
    `parent_id`   = 620100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 620122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '榆中县',
    `parent_id`   = 620100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 620123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兰州新区',
    `parent_id`   = 620100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 620171;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '嘉峪关市',
    `parent_id`   = 620000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 620200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '市辖区',
    `parent_id`   = 620200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 620201;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雄关区',
    `parent_id`   = 620200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 620290;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长城区',
    `parent_id`   = 620200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 620291;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '镜铁区',
    `parent_id`   = 620200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 620292;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新城镇',
    `parent_id`   = 620200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 620293;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '峪泉镇',
    `parent_id`   = 620200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 620294;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '文殊镇',
    `parent_id`   = 620200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 620295;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '金昌市',
    `parent_id`   = 620000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 620300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金川区',
    `parent_id`   = 620300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 620302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永昌县',
    `parent_id`   = 620300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 620321;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '白银市',
    `parent_id`   = 620000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 620400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '白银区',
    `parent_id`   = 620400,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 620402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平川区',
    `parent_id`   = 620400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 620403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '靖远县',
    `parent_id`   = 620400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 620421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '会宁县',
    `parent_id`   = 620400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 620422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '景泰县',
    `parent_id`   = 620400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 620423;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '天水市',
    `parent_id`   = 620000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 620500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '秦州区',
    `parent_id`   = 620500,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 620502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '麦积区',
    `parent_id`   = 620500,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 620503;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清水县',
    `parent_id`   = 620500,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 620521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '秦安县',
    `parent_id`   = 620500,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 620522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '甘谷县',
    `parent_id`   = 620500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 620523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武山县',
    `parent_id`   = 620500,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 620524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '张家川回族自治县',
    `parent_id`   = 620500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 620525;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '武威市',
    `parent_id`   = 620000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 620600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凉州区',
    `parent_id`   = 620600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 620602;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '民勤县',
    `parent_id`   = 620600,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 620621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '古浪县',
    `parent_id`   = 620600,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 620622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天祝藏族自治县',
    `parent_id`   = 620600,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 620623;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '张掖市',
    `parent_id`   = 620000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 620700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '甘州区',
    `parent_id`   = 620700,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 620702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '肃南裕固族自治县',
    `parent_id`   = 620700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 620721;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '民乐县',
    `parent_id`   = 620700,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 620722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临泽县',
    `parent_id`   = 620700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 620723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高台县',
    `parent_id`   = 620700,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 620724;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '山丹县',
    `parent_id`   = 620700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 620725;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '平凉市',
    `parent_id`   = 620000,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 620800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '崆峒区',
    `parent_id`   = 620800,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 620802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泾川县',
    `parent_id`   = 620800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 620821;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '灵台县',
    `parent_id`   = 620800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 620822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '崇信县',
    `parent_id`   = 620800,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 620823;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '庄浪县',
    `parent_id`   = 620800,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 620825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '静宁县',
    `parent_id`   = 620800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 620826;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '华亭市',
    `parent_id`   = 620800,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 620881;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '酒泉市',
    `parent_id`   = 620000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 620900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '肃州区',
    `parent_id`   = 620900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 620902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金塔县',
    `parent_id`   = 620900,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 620921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '瓜州县',
    `parent_id`   = 620900,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 620922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '肃北蒙古族自治县',
    `parent_id`   = 620900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 620923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿克塞哈萨克族自治县',
    `parent_id`   = 620900,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 620924;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玉门市',
    `parent_id`   = 620900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 620981;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '敦煌市',
    `parent_id`   = 620900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 620982;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '庆阳市',
    `parent_id`   = 620000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 621000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西峰区',
    `parent_id`   = 621000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 621002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '庆城县',
    `parent_id`   = 621000,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 621021;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '环县',
    `parent_id`   = 621000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 621022;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '华池县',
    `parent_id`   = 621000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 621023;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '合水县',
    `parent_id`   = 621000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 621024;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '正宁县',
    `parent_id`   = 621000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 621025;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宁县',
    `parent_id`   = 621000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 621026;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '镇原县',
    `parent_id`   = 621000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 621027;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '定西市',
    `parent_id`   = 620000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 621100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安定区',
    `parent_id`   = 621100,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 621102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '通渭县',
    `parent_id`   = 621100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 621121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '陇西县',
    `parent_id`   = 621100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 621122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '渭源县',
    `parent_id`   = 621100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 621123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临洮县',
    `parent_id`   = 621100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 621124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '漳县',
    `parent_id`   = 621100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 621125;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岷县',
    `parent_id`   = 621100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 621126;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '陇南市',
    `parent_id`   = 620000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 621200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '武都区',
    `parent_id`   = 621200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 621202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '成县',
    `parent_id`   = 621200,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 621221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '文县',
    `parent_id`   = 621200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 621222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宕昌县',
    `parent_id`   = 621200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 621223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '康县',
    `parent_id`   = 621200,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 621224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西和县',
    `parent_id`   = 621200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 621225;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '礼县',
    `parent_id`   = 621200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 621226;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '徽县',
    `parent_id`   = 621200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 621227;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '两当县',
    `parent_id`   = 621200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 621228;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '临夏回族自治州',
    `parent_id`   = 620000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 622900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临夏市',
    `parent_id`   = 622900,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 622901;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临夏县',
    `parent_id`   = 622900,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 622921;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '康乐县',
    `parent_id`   = 622900,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 622922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永靖县',
    `parent_id`   = 622900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 622923;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '广河县',
    `parent_id`   = 622900,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 622924;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '和政县',
    `parent_id`   = 622900,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 622925;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东乡族自治县',
    `parent_id`   = 622900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 622926;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '积石山保安族东乡族撒拉族自治县',
    `parent_id`   = 622900,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 622927;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '甘南藏族自治州',
    `parent_id`   = 620000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 623000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '合作市',
    `parent_id`   = 623000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 623001;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '临潭县',
    `parent_id`   = 623000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 623021;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '卓尼县',
    `parent_id`   = 623000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 623022;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '舟曲县',
    `parent_id`   = 623000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 623023;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '迭部县',
    `parent_id`   = 623000,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 623024;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玛曲县',
    `parent_id`   = 623000,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 623025;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '碌曲县',
    `parent_id`   = 623000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 623026;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '夏河县',
    `parent_id`   = 623000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 623027;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '青海省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 630000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '西宁市',
    `parent_id`   = 630000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 630100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '城东区',
    `parent_id`   = 630100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 630102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '城中区',
    `parent_id`   = 630100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 630103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '城西区',
    `parent_id`   = 630100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 630104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '城北区',
    `parent_id`   = 630100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 630105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湟中区',
    `parent_id`   = 630100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 630106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大通回族土族自治县',
    `parent_id`   = 630100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 630121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湟源县',
    `parent_id`   = 630100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 630123;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '海东市',
    `parent_id`   = 630000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 630200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乐都区',
    `parent_id`   = 630200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 630202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平安区',
    `parent_id`   = 630200,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 630203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '民和回族土族自治县',
    `parent_id`   = 630200,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 630222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '互助土族自治县',
    `parent_id`   = 630200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 630223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '化隆回族自治县',
    `parent_id`   = 630200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 630224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '循化撒拉族自治县',
    `parent_id`   = 630200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 630225;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '海北藏族自治州',
    `parent_id`   = 630000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 632200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '门源回族自治县',
    `parent_id`   = 632200,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 632221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '祁连县',
    `parent_id`   = 632200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 632222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海晏县',
    `parent_id`   = 632200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 632223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '刚察县',
    `parent_id`   = 632200,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 632224;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '黄南藏族自治州',
    `parent_id`   = 630000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 632300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '同仁市',
    `parent_id`   = 632300,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 632301;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '尖扎县',
    `parent_id`   = 632300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 632322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泽库县',
    `parent_id`   = 632300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 632323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '河南蒙古族自治县',
    `parent_id`   = 632300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 632324;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '海南藏族自治州',
    `parent_id`   = 630000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 632500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '共和县',
    `parent_id`   = 632500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 632521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '同德县',
    `parent_id`   = 632500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 632522;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '贵德县',
    `parent_id`   = 632500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 632523;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴海县',
    `parent_id`   = 632500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 632524;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '贵南县',
    `parent_id`   = 632500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 632525;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '果洛藏族自治州',
    `parent_id`   = 630000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 632600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玛沁县',
    `parent_id`   = 632600,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 632621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '班玛县',
    `parent_id`   = 632600,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 632622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '甘德县',
    `parent_id`   = 632600,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 632623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '达日县',
    `parent_id`   = 632600,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 632624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '久治县',
    `parent_id`   = 632600,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 632625;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玛多县',
    `parent_id`   = 632600,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 632626;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '玉树藏族自治州',
    `parent_id`   = 630000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 632700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玉树市',
    `parent_id`   = 632700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 632701;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '杂多县',
    `parent_id`   = 632700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 632722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '称多县',
    `parent_id`   = 632700,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 632723;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '治多县',
    `parent_id`   = 632700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 632724;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '囊谦县',
    `parent_id`   = 632700,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 632725;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '曲麻莱县',
    `parent_id`   = 632700,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 632726;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '海西蒙古族藏族自治州',
    `parent_id`   = 630000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 632800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '格尔木市',
    `parent_id`   = 632800,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 632801;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '德令哈市',
    `parent_id`   = 632800,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 632802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '茫崖市',
    `parent_id`   = 632800,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 632803;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌兰县',
    `parent_id`   = 632800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 632821;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '都兰县',
    `parent_id`   = 632800,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 632822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天峻县',
    `parent_id`   = 632800,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 632823;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大柴旦行政委员会',
    `parent_id`   = 632800,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 632857;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '宁夏自治区',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 640000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '银川市',
    `parent_id`   = 640000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 640100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兴庆区',
    `parent_id`   = 640100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 640104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西夏区',
    `parent_id`   = 640100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 640105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金凤区',
    `parent_id`   = 640100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 640106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永宁县',
    `parent_id`   = 640100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 640121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '贺兰县',
    `parent_id`   = 640100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 640122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '灵武市',
    `parent_id`   = 640100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 640181;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '石嘴山市',
    `parent_id`   = 640000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 640200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大武口区',
    `parent_id`   = 640200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 640202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '惠农区',
    `parent_id`   = 640200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 640205;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平罗县',
    `parent_id`   = 640200,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 640221;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '吴忠市',
    `parent_id`   = 640000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 640300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '利通区',
    `parent_id`   = 640300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 640302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '红寺堡区',
    `parent_id`   = 640300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 640303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盐池县',
    `parent_id`   = 640300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 640323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '同心县',
    `parent_id`   = 640300,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 640324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青铜峡市',
    `parent_id`   = 640300,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 640381;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '固原市',
    `parent_id`   = 640000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 640400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '原州区',
    `parent_id`   = 640400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 640402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西吉县',
    `parent_id`   = 640400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 640422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '隆德县',
    `parent_id`   = 640400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 640423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泾源县',
    `parent_id`   = 640400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 640424;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '彭阳县',
    `parent_id`   = 640400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 640425;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '中卫市',
    `parent_id`   = 640000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 640500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沙坡头区',
    `parent_id`   = 640500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 640502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中宁县',
    `parent_id`   = 640500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 640521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海原县',
    `parent_id`   = 640500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 640522;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '新疆',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 650000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '乌鲁木齐市',
    `parent_id`   = 650000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 650100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '天山区',
    `parent_id`   = 650100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 650102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沙依巴克区',
    `parent_id`   = 650100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 650103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新市区',
    `parent_id`   = 650100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 650104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '水磨沟区',
    `parent_id`   = 650100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 650105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '头屯河区',
    `parent_id`   = 650100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 650106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '达坂城区',
    `parent_id`   = 650100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 650107;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '米东区',
    `parent_id`   = 650100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 650109;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌鲁木齐县',
    `parent_id`   = 650100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 650121;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '克拉玛依市',
    `parent_id`   = 650000,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 650200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '独山子区',
    `parent_id`   = 650200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 650202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '克拉玛依区',
    `parent_id`   = 650200,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 650203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '白碱滩区',
    `parent_id`   = 650200,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 650204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌尔禾区',
    `parent_id`   = 650200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 650205;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '吐鲁番市',
    `parent_id`   = 650000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 650400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高昌区',
    `parent_id`   = 650400,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 650402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鄯善县',
    `parent_id`   = 650400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 650421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '托克逊县',
    `parent_id`   = 650400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 650422;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '哈密市',
    `parent_id`   = 650000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 650500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '伊州区',
    `parent_id`   = 650500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 650502;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巴里坤哈萨克自治县',
    `parent_id`   = 650500,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 650521;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '伊吾县',
    `parent_id`   = 650500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 650522;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '昌吉回族自治州',
    `parent_id`   = 650000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 652300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昌吉市',
    `parent_id`   = 652300,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 652301;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阜康市',
    `parent_id`   = 652300,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 652302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '呼图壁县',
    `parent_id`   = 652300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 652323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玛纳斯县',
    `parent_id`   = 652300,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 652324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '奇台县',
    `parent_id`   = 652300,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 652325;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吉木萨尔县',
    `parent_id`   = 652300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 652327;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '木垒哈萨克自治县',
    `parent_id`   = 652300,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 652328;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '博尔塔拉蒙古自治州',
    `parent_id`   = 650000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 652700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '博乐市',
    `parent_id`   = 652700,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 652701;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿拉山口市',
    `parent_id`   = 652700,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 652702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '精河县',
    `parent_id`   = 652700,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 652722;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '温泉县',
    `parent_id`   = 652700,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 652723;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '巴音郭楞蒙古自治州',
    `parent_id`   = 650000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 652800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '库尔勒市',
    `parent_id`   = 652800,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 652801;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '轮台县',
    `parent_id`   = 652800,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 652822;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '尉犁县',
    `parent_id`   = 652800,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 652823;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '若羌县',
    `parent_id`   = 652800,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 652824;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '且末县',
    `parent_id`   = 652800,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 652825;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '焉耆回族自治县',
    `parent_id`   = 652800,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 652826;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '和静县',
    `parent_id`   = 652800,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 652827;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '和硕县',
    `parent_id`   = 652800,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 652828;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '博湖县',
    `parent_id`   = 652800,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 652829;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '阿克苏地区',
    `parent_id`   = 650000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 652900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿克苏市',
    `parent_id`   = 652900,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 652901;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '库车市',
    `parent_id`   = 652900,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 652902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '温宿县',
    `parent_id`   = 652900,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 652922;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沙雅县',
    `parent_id`   = 652900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 652924;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新和县',
    `parent_id`   = 652900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 652925;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '拜城县',
    `parent_id`   = 652900,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 652926;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌什县',
    `parent_id`   = 652900,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 652927;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿瓦提县',
    `parent_id`   = 652900,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 652928;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '柯坪县',
    `parent_id`   = 652900,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 652929;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '克孜勒苏柯尔克孜自治州',
    `parent_id`   = 650000,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 653000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿图什市',
    `parent_id`   = 653000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 653001;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿克陶县',
    `parent_id`   = 653000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 653022;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿合奇县',
    `parent_id`   = 653000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 653023;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌恰县',
    `parent_id`   = 653000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 653024;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '喀什地区',
    `parent_id`   = 650000,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 653100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '喀什市',
    `parent_id`   = 653100,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 653101;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '疏附县',
    `parent_id`   = 653100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 653121;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '疏勒县',
    `parent_id`   = 653100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 653122;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '英吉沙县',
    `parent_id`   = 653100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 653123;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泽普县',
    `parent_id`   = 653100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 653124;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莎车县',
    `parent_id`   = 653100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 653125;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '叶城县',
    `parent_id`   = 653100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 653126;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '麦盖提县',
    `parent_id`   = 653100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 653127;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '岳普湖县',
    `parent_id`   = 653100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 653128;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '伽师县',
    `parent_id`   = 653100,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 653129;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巴楚县',
    `parent_id`   = 653100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 653130;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '塔什库尔干塔吉克自治县',
    `parent_id`   = 653100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 653131;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '和田地区',
    `parent_id`   = 650000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 653200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '和田市',
    `parent_id`   = 653200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 653201;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '和田县',
    `parent_id`   = 653200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 653221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '墨玉县',
    `parent_id`   = 653200,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 653222;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '皮山县',
    `parent_id`   = 653200,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 653223;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '洛浦县',
    `parent_id`   = 653200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 653224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '策勒县',
    `parent_id`   = 653200,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 653225;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '于田县',
    `parent_id`   = 653200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 653226;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '民丰县',
    `parent_id`   = 653200,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 653227;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '伊犁哈萨克自治州',
    `parent_id`   = 650000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 654000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '伊宁市',
    `parent_id`   = 654000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 654002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '奎屯市',
    `parent_id`   = 654000,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 654003;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '霍尔果斯市',
    `parent_id`   = 654000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 654004;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '伊宁县',
    `parent_id`   = 654000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 654021;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '察布查尔锡伯自治县',
    `parent_id`   = 654000,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 654022;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '霍城县',
    `parent_id`   = 654000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 654023;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '巩留县',
    `parent_id`   = 654000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 654024;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新源县',
    `parent_id`   = 654000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 654025;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昭苏县',
    `parent_id`   = 654000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 654026;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '特克斯县',
    `parent_id`   = 654000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 654027;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '尼勒克县',
    `parent_id`   = 654000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 654028;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '塔城地区',
    `parent_id`   = 650000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 654200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '塔城市',
    `parent_id`   = 654200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 654201;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌苏市',
    `parent_id`   = 654200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 654202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沙湾市',
    `parent_id`   = 654200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 654203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '额敏县',
    `parent_id`   = 654200,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 654221;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '托里县',
    `parent_id`   = 654200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 654224;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '裕民县',
    `parent_id`   = 654200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 654225;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '和布克赛尔蒙古自治县',
    `parent_id`   = 654200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 654226;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '阿勒泰地区',
    `parent_id`   = 650000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 654300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿勒泰市',
    `parent_id`   = 654300,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 654301;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '布尔津县',
    `parent_id`   = 654300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 654321;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '富蕴县',
    `parent_id`   = 654300,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 654322;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '福海县',
    `parent_id`   = 654300,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 654323;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '哈巴河县',
    `parent_id`   = 654300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 654324;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '青河县',
    `parent_id`   = 654300,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 654325;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吉木乃县',
    `parent_id`   = 654300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 654326;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '自治区直辖县级行政区划',
    `parent_id`   = 650000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 659000;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石河子市',
    `parent_id`   = 659000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 659001;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿拉尔市',
    `parent_id`   = 659000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 659002;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '图木舒克市',
    `parent_id`   = 659000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 659003;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五家渠市',
    `parent_id`   = 659000,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 659004;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北屯市',
    `parent_id`   = 659000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 659005;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铁门关市',
    `parent_id`   = 659000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 659006;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '双河市',
    `parent_id`   = 659000,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 659007;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '可克达拉市',
    `parent_id`   = 659000,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 659008;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '昆玉市',
    `parent_id`   = 659000,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 659009;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '胡杨河市',
    `parent_id`   = 659000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 659010;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新星市',
    `parent_id`   = 659000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 659011;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '白杨市',
    `parent_id`   = 659000,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 659012;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '台湾省',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 710000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '台北市',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 710100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中正区',
    `parent_id`   = 710100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 710101;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大同区',
    `parent_id`   = 710100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中山区',
    `parent_id`   = 710100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 710103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '松山区',
    `parent_id`   = 710100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 710104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大安区',
    `parent_id`   = 710100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万华区',
    `parent_id`   = 710100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 710106;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '信义区',
    `parent_id`   = 710100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710107;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '士林区',
    `parent_id`   = 710100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 710108;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北投区',
    `parent_id`   = 710100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 710109;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '内湖区',
    `parent_id`   = 710100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 710110;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南港区',
    `parent_id`   = 710100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 710111;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '文山区',
    `parent_id`   = 710100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 710112;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '其它区',
    `parent_id`   = 710100,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710199;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '高雄市',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 710200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新兴区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710201;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '前金区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芩雅区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盐埕区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 710204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鼓山区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 710205;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '旗津区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710206;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '前镇区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710207;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三民区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 710208;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '左营区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 710209;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '楠梓区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 710210;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '小港区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710211;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '苓雅区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 710241;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仁武区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 710242;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大社区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710243;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '冈山区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 710244;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '路竹区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 710245;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿莲区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 710246;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '田寮区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 710247;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '燕巢区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 710248;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桥头区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710249;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '梓官区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 710250;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '弥陀区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 710251;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永安区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 710252;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湖内区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 710253;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凤山区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 710254;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大寮区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710255;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '林园区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 710256;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鸟松区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 710257;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大树区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710258;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '旗山区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710259;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '美浓区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 710260;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '六龟区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 710261;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '内门区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 710262;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '杉林区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 710263;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '甲仙区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 710264;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桃源区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 710265;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '那玛夏区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 710266;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '茂林区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 710267;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '茄萣区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710268;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '其它区',
    `parent_id`   = 710200,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710299;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '台南市',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 710300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中西区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 710301;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 710303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 710304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安平区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 710305;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安南区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 710306;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永康区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 710339;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '归仁区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 710340;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新化区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710341;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '左镇区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 710342;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玉井区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 710343;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '楠西区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 710344;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南化区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 710345;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仁德区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 710346;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '关庙区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 710347;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙崎区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 710348;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '官田区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 710349;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '麻豆区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 710350;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '佳里区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 710351;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西港区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710352;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '七股区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710353;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '将军区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 710354;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '学甲区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710355;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北门区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 710356;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新营区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710357;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '后壁区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 710358;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '白河区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 710359;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东山区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710360;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '六甲区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 710361;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '下营区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710362;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '柳营区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 710363;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盐水区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 710364;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '善化区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 710365;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大内区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710366;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '山上区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 710367;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新市区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710368;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安定区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 710369;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '其它区',
    `parent_id`   = 710300,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710399;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '台中市',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 710400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 710401;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710402;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 710403;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710404;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 710405;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北屯区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 710406;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西屯区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710407;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南屯区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 710408;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '太平区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 710431;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大里区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710432;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雾峰区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 710433;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌日区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 710434;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丰原区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 710435;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '后里区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 710436;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石冈区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 710437;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东势区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710438;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '和平区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 710439;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新社区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710440;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '潭子区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 710441;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大雅区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710442;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '神冈区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 710443;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大肚区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710444;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沙鹿区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 710445;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙井区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 710446;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '梧栖区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 710447;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '清水区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710448;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大甲区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710449;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '外埔区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 710450;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大安区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710451;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '其它区',
    `parent_id`   = 710400,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710499;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '金门县',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 710500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金沙镇',
    `parent_id`   = 710500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 710507;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金湖镇',
    `parent_id`   = 710500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 710508;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金宁乡',
    `parent_id`   = 710500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 710509;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金城镇',
    `parent_id`   = 710500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 710510;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '烈屿乡',
    `parent_id`   = 710500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 710511;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌坵乡',
    `parent_id`   = 710500,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 710512;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '南投县',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 710600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南投市',
    `parent_id`   = 710600,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 710614;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中寮乡',
    `parent_id`   = 710600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 710615;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '草屯镇',
    `parent_id`   = 710600,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 710616;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '国姓乡',
    `parent_id`   = 710600,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 710617;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '埔里镇',
    `parent_id`   = 710600,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 710618;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仁爱乡',
    `parent_id`   = 710600,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 710619;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '名间乡',
    `parent_id`   = 710600,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 710620;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '集集镇',
    `parent_id`   = 710600,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 710621;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '水里乡',
    `parent_id`   = 710600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 710622;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鱼池乡',
    `parent_id`   = 710600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 710623;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '信义乡',
    `parent_id`   = 710600,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710624;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '竹山镇',
    `parent_id`   = 710600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 710625;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鹿谷乡',
    `parent_id`   = 710600,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 710626;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '基隆市',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 710700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仁爱区',
    `parent_id`   = 710700,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 710701;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '信义区',
    `parent_id`   = 710700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710702;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中正区',
    `parent_id`   = 710700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 710703;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中山区',
    `parent_id`   = 710700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 710704;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '安乐区',
    `parent_id`   = 710700,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 710705;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '暖暖区',
    `parent_id`   = 710700,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 710706;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '七堵区',
    `parent_id`   = 710700,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710707;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '其它区',
    `parent_id`   = 710700,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710799;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '新竹市',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东区',
    `parent_id`   = 710800,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710801;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北区',
    `parent_id`   = 710800,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 710802;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '香山区',
    `parent_id`   = 710800,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710803;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '其它区',
    `parent_id`   = 710800,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710899;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '嘉义市',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 710900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东区',
    `parent_id`   = 710900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 710901;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西区',
    `parent_id`   = 710900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 710902;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '其它区',
    `parent_id`   = 710900,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 710999;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '新北市',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万里区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 711130;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '板桥区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 711132;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '汐止区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711133;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '深坑区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 711134;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石碇区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 711135;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '瑞芳区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 711136;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平溪区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 711137;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '双溪区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 711138;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '贡寮区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 711139;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新店区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711140;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '坪林区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 711141;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '乌来区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 711142;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永和区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 711143;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中和区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 711144;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '土城区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 711145;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三峡区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 711146;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '树林区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 711147;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莺歌区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 711148;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三重区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 711149;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新庄区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711150;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泰山区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 711151;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '林口区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 711152;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芦洲区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 711153;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五股区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 711154;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '八里区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 711155;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '淡水区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 711156;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三芝区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 711157;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '石门区',
    `parent_id`   = 711100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 711158;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '宜兰县',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 711200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宜兰市',
    `parent_id`   = 711200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 711287;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '头城镇',
    `parent_id`   = 711200,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 711288;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '礁溪乡',
    `parent_id`   = 711200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 711289;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '壮围乡',
    `parent_id`   = 711200,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 711290;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '员山乡',
    `parent_id`   = 711200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 711291;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '罗东镇',
    `parent_id`   = 711200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 711292;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三星乡',
    `parent_id`   = 711200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 711293;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大同乡',
    `parent_id`   = 711200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 711294;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五结乡',
    `parent_id`   = 711200,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 711295;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '冬山乡',
    `parent_id`   = 711200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 711296;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '苏澳镇',
    `parent_id`   = 711200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 711297;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南澳乡',
    `parent_id`   = 711200,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 711298;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '钓鱼台',
    `parent_id`   = 711200,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 711299;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '新竹县',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '竹北市',
    `parent_id`   = 711300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 711387;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湖口乡',
    `parent_id`   = 711300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 711388;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新丰乡',
    `parent_id`   = 711300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711389;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新埔镇',
    `parent_id`   = 711300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711390;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '关西镇',
    `parent_id`   = 711300,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 711391;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芎林乡',
    `parent_id`   = 711300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711392;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '宝山乡',
    `parent_id`   = 711300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 711393;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '竹东镇',
    `parent_id`   = 711300,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 711394;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '五峰乡',
    `parent_id`   = 711300,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 711395;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '横山乡',
    `parent_id`   = 711300,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 711396;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '尖石乡',
    `parent_id`   = 711300,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 711397;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北埔乡',
    `parent_id`   = 711300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 711398;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '峨眉乡',
    `parent_id`   = 711300,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 711399;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '桃园县',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 711400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中坜区',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 711414;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平镇区',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 711415;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '杨梅区',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 711417;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新屋区',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711418;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '观音区',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 711419;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桃园区',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 711420;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龟山区',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 711421;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '八德区',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 711422;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大溪区',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 711423;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大园区',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 711425;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芦竹区',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 711426;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中坜市',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 711487;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '平镇市',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 711488;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龙潭乡',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 711489;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '杨梅市',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 711490;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新屋乡',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711491;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '观音乡',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 711492;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '桃园市',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 711493;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '龟山乡',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 711494;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '八德市',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 711495;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大溪镇',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 711496;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '复兴乡',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 711497;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大园乡',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 711498;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芦竹乡',
    `parent_id`   = 711400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 711499;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '苗栗县',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 711500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '头份市',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 711520;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '竹南镇',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 711582;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '头份镇',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 711583;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三湾乡',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 711584;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南庄乡',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 711585;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '狮潭乡',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 711586;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '后龙镇',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 711587;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '通霄镇',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 711588;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '苑里镇',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 711589;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '苗栗市',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 711590;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '造桥乡',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 711591;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '头屋乡',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 711592;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '公馆乡',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 711593;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大湖乡',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 711594;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泰安乡',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 711595;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '铜锣乡',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 711596;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三义乡',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 711597;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西湖乡',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711598;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '卓兰镇',
    `parent_id`   = 711500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 711599;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '彰化县',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 711700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '员林市',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 711736;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '彰化市',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 711774;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芬园乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 711775;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '花坛乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 711776;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '秀水乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711777;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鹿港镇',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 711778;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '福兴乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 711779;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '线西乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711780;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '和美镇',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 711781;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '伸港乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 711782;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '员林镇',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 711783;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '社头乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 711784;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '永靖乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 711785;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '埔心乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 711786;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '溪湖镇',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711787;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大村乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 711788;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '埔盐乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 711789;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '田中镇',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 711790;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北斗镇',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 711791;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '田尾乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 711792;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '埤头乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 711793;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '溪州乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711794;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '竹塘乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 711795;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '二林镇',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 711796;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大城乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 711797;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '芳苑乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 711798;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '二水乡',
    `parent_id`   = 711700,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 711799;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '嘉义县',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 711900;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '番路乡',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 711982;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '梅山乡',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 711983;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '竹崎乡',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 711984;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '阿里山乡',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 711985;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中埔乡',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 711986;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大埔乡',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 711987;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '水上乡',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 711988;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鹿草乡',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 711989;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '太保市',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 711990;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '朴子市',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 711991;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东石乡',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 711992;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '六脚乡',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 711993;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新港乡',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711994;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '民雄乡',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 711995;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大林镇',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 711996;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '溪口乡',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 711997;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '义竹乡',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 711998;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '布袋镇',
    `parent_id`   = 711900,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 711999;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '云林县',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 712100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '斗南镇',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 712180;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大埤乡',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 712181;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '虎尾镇',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 712182;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '土库镇',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 712183;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '褒忠乡',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 712184;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东势乡',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 712185;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '台西乡',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 712186;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '仑背乡',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 712187;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '麦寮乡',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 712188;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '斗六市',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 712189;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '林内乡',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 712190;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '古坑乡',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 712191;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莿桐乡',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 712192;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西螺镇',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 712193;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '二仑乡',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'E'
WHERE `region_id` = 712194;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北港镇',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 712195;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '水林乡',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 712196;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '口湖乡',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 712197;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '四湖乡',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 712198;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '元长乡',
    `parent_id`   = 712100,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 712199;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '屏东县',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 712400;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '崁顶乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 712451;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '屏东市',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 712467;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '三地门乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 712468;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '雾台乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 712469;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玛家乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 712470;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '九如乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 712471;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '里港乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 712472;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '高树乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 712473;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '盐埔乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 712474;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长治乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 712475;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '麟洛乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 712476;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '竹田乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 712477;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '内埔乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 712478;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万丹乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 712479;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '潮州镇',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 712480;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '泰武乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 712481;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '来义乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 712482;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万峦乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 712483;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莰顶乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 712484;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新埤乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 712485;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南州乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 712486;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '林边乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 712487;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东港镇',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 712488;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '琉球乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 712489;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '佳冬乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 712490;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新园乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 712491;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '枋寮乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 712492;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '枋山乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 712493;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '春日乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 712494;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '狮子乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 712495;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '车城乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 712496;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '牡丹乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 712497;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '恒春镇',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 712498;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '满州乡',
    `parent_id`   = 712400,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 712499;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '台东县',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 712500;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '台东市',
    `parent_id`   = 712500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 712584;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '绿岛乡',
    `parent_id`   = 712500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 712585;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '兰屿乡',
    `parent_id`   = 712500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 712586;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '延平乡',
    `parent_id`   = 712500,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 712587;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '卑南乡',
    `parent_id`   = 712500,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 712588;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '鹿野乡',
    `parent_id`   = 712500,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 712589;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '关山镇',
    `parent_id`   = 712500,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 712590;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '海端乡',
    `parent_id`   = 712500,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 712591;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '池上乡',
    `parent_id`   = 712500,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 712592;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东河乡',
    `parent_id`   = 712500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 712593;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '成功镇',
    `parent_id`   = 712500,
    `is_hot`      = 0,
    `first_word`  = 'C'
WHERE `region_id` = 712594;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '长滨乡',
    `parent_id`   = 712500,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 712595;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '金峰乡',
    `parent_id`   = 712500,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 712596;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大武乡',
    `parent_id`   = 712500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 712597;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '达仁乡',
    `parent_id`   = 712500,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 712598;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '太麻里乡',
    `parent_id`   = 712500,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 712599;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '花莲县',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 712600;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '花莲市',
    `parent_id`   = 712600,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 712686;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '新城乡',
    `parent_id`   = 712600,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 712687;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '太鲁阁',
    `parent_id`   = 712600,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 712688;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '秀林乡',
    `parent_id`   = 712600,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 712689;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '吉安乡',
    `parent_id`   = 712600,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 712690;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '寿丰乡',
    `parent_id`   = 712600,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 712691;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '凤林镇',
    `parent_id`   = 712600,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 712692;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '光复乡',
    `parent_id`   = 712600,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 712693;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '丰滨乡',
    `parent_id`   = 712600,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 712694;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '瑞穗乡',
    `parent_id`   = 712600,
    `is_hot`      = 0,
    `first_word`  = 'R'
WHERE `region_id` = 712695;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '万荣乡',
    `parent_id`   = 712600,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 712696;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '玉里镇',
    `parent_id`   = 712600,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 712697;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '卓溪乡',
    `parent_id`   = 712600,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 712698;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '富里乡',
    `parent_id`   = 712600,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 712699;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '澎湖县',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'P'
WHERE `region_id` = 712700;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '马公市',
    `parent_id`   = 712700,
    `is_hot`      = 0,
    `first_word`  = 'M'
WHERE `region_id` = 712794;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西屿乡',
    `parent_id`   = 712700,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 712795;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '望安乡',
    `parent_id`   = 712700,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 712796;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '七美乡',
    `parent_id`   = 712700,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 712797;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '白沙乡',
    `parent_id`   = 712700,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 712798;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湖西乡',
    `parent_id`   = 712700,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 712799;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '连江县',
    `parent_id`   = 710000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 712800;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南竿乡',
    `parent_id`   = 712800,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 712896;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北竿乡',
    `parent_id`   = 712800,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 712897;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东引乡',
    `parent_id`   = 712800,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 712898;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '莒光乡',
    `parent_id`   = 712800,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 712899;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '香港',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 810000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '香港岛',
    `parent_id`   = 810000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 810100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '中西区',
    `parent_id`   = 810100,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 810101;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '湾仔区',
    `parent_id`   = 810100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 810102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '东区',
    `parent_id`   = 810100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 810103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '南区',
    `parent_id`   = 810100,
    `is_hot`      = 0,
    `first_word`  = 'N'
WHERE `region_id` = 810104;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '九龙',
    `parent_id`   = 810000,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 810200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '九龙城区',
    `parent_id`   = 810200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 810201;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '油尖旺区',
    `parent_id`   = 810200,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 810202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '深水埗区',
    `parent_id`   = 810200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 810203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '黄大仙区',
    `parent_id`   = 810200,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 810204;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '观塘区',
    `parent_id`   = 810200,
    `is_hot`      = 0,
    `first_word`  = 'G'
WHERE `region_id` = 810205;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '新界',
    `parent_id`   = 810000,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 810300;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '北区',
    `parent_id`   = 810300,
    `is_hot`      = 0,
    `first_word`  = 'B'
WHERE `region_id` = 810301;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大埔区',
    `parent_id`   = 810300,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 810302;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '沙田区',
    `parent_id`   = 810300,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 810303;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '西贡区',
    `parent_id`   = 810300,
    `is_hot`      = 0,
    `first_word`  = 'X'
WHERE `region_id` = 810304;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '元朗区',
    `parent_id`   = 810300,
    `is_hot`      = 0,
    `first_word`  = 'Y'
WHERE `region_id` = 810305;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '屯门区',
    `parent_id`   = 810300,
    `is_hot`      = 0,
    `first_word`  = 'T'
WHERE `region_id` = 810306;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '荃湾区',
    `parent_id`   = 810300,
    `is_hot`      = 0,
    `first_word`  = 'Q'
WHERE `region_id` = 810307;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '葵青区',
    `parent_id`   = 810300,
    `is_hot`      = 0,
    `first_word`  = 'K'
WHERE `region_id` = 810308;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '离岛区',
    `parent_id`   = 810300,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 810309;
UPDATE `region`
SET `level`       = 2,
    `region_name` = '澳门',
    `parent_id`   = 1,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 820000;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '澳门半岛',
    `parent_id`   = 820000,
    `is_hot`      = 0,
    `first_word`  = 'A'
WHERE `region_id` = 820100;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '花地玛堂区',
    `parent_id`   = 820100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 820102;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '花王堂区',
    `parent_id`   = 820100,
    `is_hot`      = 0,
    `first_word`  = 'H'
WHERE `region_id` = 820103;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '望德堂区',
    `parent_id`   = 820100,
    `is_hot`      = 0,
    `first_word`  = 'W'
WHERE `region_id` = 820104;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '大堂区',
    `parent_id`   = 820100,
    `is_hot`      = 0,
    `first_word`  = 'D'
WHERE `region_id` = 820105;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '风顺堂区',
    `parent_id`   = 820100,
    `is_hot`      = 0,
    `first_word`  = 'F'
WHERE `region_id` = 820106;
UPDATE `region`
SET `level`       = 3,
    `region_name` = '离岛',
    `parent_id`   = 820000,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 820200;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '嘉模堂区',
    `parent_id`   = 820200,
    `is_hot`      = 0,
    `first_word`  = 'J'
WHERE `region_id` = 820202;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '路氹填海区',
    `parent_id`   = 820200,
    `is_hot`      = 0,
    `first_word`  = 'L'
WHERE `region_id` = 820203;
UPDATE `region`
SET `level`       = 4,
    `region_name` = '圣方济各堂区',
    `parent_id`   = 820200,
    `is_hot`      = 0,
    `first_word`  = 'S'
WHERE `region_id` = 820204;
UPDATE `region`
SET `level`       = 1,
    `region_name` = '中国',
    `parent_id`   = 0,
    `is_hot`      = 0,
    `first_word`  = 'Z'
WHERE `region_id` = 1;
