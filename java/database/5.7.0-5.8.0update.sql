ALTER TABLE `vendor_product`
    ADD COLUMN `pic_large` varchar(255) NOT NULL DEFAULT '' COMMENT '高清图片url' AFTER `pic_original`;

ALTER TABLE `product`
    ADD COLUMN `pic_large` varchar(255) NOT NULL DEFAULT '' COMMENT '高清图片url' AFTER `pic_original`;

ALTER TABLE `user_message`
    ADD COLUMN `message_template` text NULL AFTER `content`;

ALTER TABLE product_stock_record MODIFY COLUMN `stock_code` VARCHAR (19) DEFAULT NULL COMMENT '仓库编号';

delete  from authority where authority_sn = 'inventoryGoodsArePutOutOfStorageManage';
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30000001, 'inventoryGoodsArePutOutOfStorageManage', '商品出库', 10000411, 52, 1, '[{"authName":"编辑","authSn":"productStockOutUpdateManage"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30000002, 'inventoryGoodsArePutOutOfStorageManage', '商品出库', 10000454, 50, 1, '[{"authName":"编辑","authSn":"productStockOutUpdateManage"}]', '', '', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30000003, 'inventoryGoodsArePutOutOfStorageManage', '商品出库', 20000043, 50, 1, '[{"authName":"编辑","authSn":"productStockOutUpdateManage"}]', '', '', 0, 'store');


delete  from authority where authority_sn = 'inventoryGoodsArePutIntoStorageManage';
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30000007, 'inventoryGoodsArePutIntoStorageManage', '商品入库', 10000411, 51, 1, '[{"authName":"编辑","authSn":"productStockInUpdateManage"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30000008, 'inventoryGoodsArePutIntoStorageManage', '商品入库', 10000454, 50, 1, '[{"authName":"编辑","authSn":"productStockInUpdateManage"}]', '', '', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30000009, 'inventoryGoodsArePutIntoStorageManage', '商品入库', 20000043, 50, 1, '[{"authName":"编辑","authSn":"productStockInUpdateManage"}]', '', '', 0, 'store');


delete  from authority where authority_sn = 'inventoryAdjustmentManage';
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30000013, 'inventoryAdjustmentManage', '库存调整', 10000411, 52, 1, '[{"authName":"编辑","authSn":"productStockAdjustUpdateManage"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30000014, 'inventoryAdjustmentManage', '库存调整', 10000454, 50, 1, '[{"authName":"编辑","authSn":"productStockAdjustUpdateManage"}]', '', '', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30000015, 'inventoryAdjustmentManage', '库存调整', 20000043, 50, 1, '[{"authName":"编辑","authSn":"productStockAdjustUpdateManage"}]', '', '', 0, 'store');


delete  from authority where authority_sn = 'storeInventoryTransferManage';
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30000019, 'storeInventoryTransferManage', '库存调拨', 10000411, 54, 1, '[{"authName":"编辑","authSn":"productStockAllocateUpdateManage"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30000020, 'storeInventoryTransferManage', '库存调拨', 10000454, 50, 1, '[{"authName":"编辑","authSn":"productStockAllocateUpdateManage"}]', '', '', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30000021, 'storeInventoryTransferManage', '库存调拨', 20000043, 50, 1, '[{"authName":"编辑","authSn":"productStockAllocateUpdateManage"}]', '', '', 0, 'store');

-- 修改供应商商品管理权限
UPDATE authority
SET child_auth = '[{"authName":"新增","authSn":"vendorProductCreateManage"},{"authName":"编辑","authSn":"vendorProductUpdateManage"},{"authName":"断供","authSn":"vendorProductSupplyCutManage"}]'
WHERE
    authority_sn = 'productManage'
  AND admin_type = 'vendor';

ALTER TABLE `order` ADD COLUMN cancel_remark VARCHAR(255) DEFAULT NULL COMMENT '取消原因';

ALTER TABLE `vendor_withdraw`
    ADD COLUMN `handling_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '手续费' AFTER `amount`,
    ADD COLUMN `handling_rate` decimal(10, 2) NULL DEFAULT NULL COMMENT '手续费率' AFTER `handling_fee`;

ALTER TABLE `shop_withdraw`
    ADD COLUMN `handling_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '手续费' AFTER `amount`,
    ADD COLUMN `handling_rate` decimal(10, 2) NULL DEFAULT NULL COMMENT '手续费率' AFTER `handling_fee`;

-- 收银台相关
INSERT INTO `authority`(`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                        `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (20100001, 'posCheckout', '收银', 0, 50, 1, '[]', '', 'iconfont-pos icon-shouyintai', 0, 'pos'),
       (20100002, 'posOrder', '订单', 0, 50, 1, '[]', '', 'iconfont-pos icon-dingdan', 0, 'pos'),
       (20100003, 'posReservation', '预约', 0, 50, 1, '[]', '', 'iconfont-pos icon-yuyue', 0, 'pos'),
       (20100004, 'posVerification', '核销', 0, 50, 1, '[]', '', 'iconfont-pos icon-hexiao', 0, 'pos'),
       (20100005, 'posReturn', '售后', 0, 50, 1, '[]', '', 'iconfont-pos icon-tuihuo', 0, 'pos'),
       (20100006, 'posShift', '交班', 0, 50, 1, '[]', '', 'iconfont-pos icon-jiaoban', 0, 'pos'),
       (20100007, 'posSetting', '系统配置', 0, 50, 1, '[]', '', 'iconfont-pos icon-xitongpeizhi', 0, 'pos'),
       (20100008, 'posVip', '会员', 0, 50, 1, '[]', '', 'iconfont-pos icon-huiyuan', 0, 'pos'),
       (20100009, 'shopCashRegisterManage', '收银管理', 20000050, 50, 1, '[]', '', 'iconfont-tig icon-yonghutongji', 0,'store'),
       (20100010, 'shopCashierList', '收银员列表', 20100009, 50, 1, '[{"authName":"编辑","authSn":"adminUserPosModifyManage"}]', '', '', 0, 'store'),
       (20100011, 'cashRegisterManage', '收银管理', 11001058, 50, 1, '[]', '', 'iconfont-tig icon-yonghutongji', 0, 'admin'),
       (20100012, 'cashierList', '收银员列表', 20100011, 50, 1, '[{"authName":"编辑","authSn":"adminUserPosModifyManage"}]', '', '', 0, 'admin');



CREATE TABLE `admin_user_pos`
(
    `id`           int unsigned                                                 NOT NULL AUTO_INCREMENT,
    `admin_id`     int unsigned                                                 NOT NULL DEFAULT '0' COMMENT '管理员id',
    `shop_id`      int unsigned                                                 NOT NULL DEFAULT '0' COMMENT '收银关联门店id',
    `username`     varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '员工名称',
    `email`        varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '邮箱',
    `avatar`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         DEFAULT '' COMMENT '头像',
    `auth_list`    text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '员工权限[json]',
    `is_using`     tinyint unsigned                                             NOT NULL DEFAULT '0' COMMENT '是否停用：1 是 0 否',
    `add_time`     int unsigned                                                          DEFAULT NULL COMMENT '创建时间',
    `role_id`      smallint unsigned                                            NOT NULL DEFAULT '0' COMMENT '权限组id',
    `work_on_time` int                                                                   DEFAULT NULL COMMENT '上班开始时间（交班使用）',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_adminId_shopId` (`admin_id`,`shop_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC COMMENT ='收银员工列表';

ALTER TABLE `user`
    ADD COLUMN `user_type` tinyint NULL DEFAULT 0 COMMENT '0会员，1游客' AFTER `status`;



CREATE TABLE `user_cash_log`  (
                                  `cash_log_id` int NOT NULL AUTO_INCREMENT COMMENT '现金日志id',
                                  `order_id` int NOT NULL COMMENT '订单ID',
                                  `order_type` tinyint NOT NULL COMMENT '1 充值订单 2 订单',
                                  `cash_amount` decimal(10, 2) NOT NULL COMMENT '现金实际金额',
                                  `cash_change_amount` decimal(10, 2) NOT NULL COMMENT '找零金额',
                                  PRIMARY KEY (`cash_log_id`) USING BTREE,
                                  INDEX `order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户现金日志记录' ROW_FORMAT = Dynamic;

CREATE TABLE `pos_pending_order` (
                                     `pending_id` int NOT NULL AUTO_INCREMENT COMMENT '挂单ID',
                                     `user_id` int NOT NULL COMMENT '用户id',
                                     `admin_id` int NOT NULL COMMENT '收银员id',
                                     `shop_id` int DEFAULT NULL COMMENT '店铺id',
                                     `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '挂单备注',
                                     `status` tinyint DEFAULT NULL COMMENT '状态：0=挂单中，1=已取单(生成订单)，2=已取消，3=已过期',
                                     `add_time` int DEFAULT NULL COMMENT '创建时间',
                                     `update_time` int DEFAULT NULL COMMENT '更新时间',
                                     PRIMARY KEY (`pending_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='挂单记录表';


ALTER TABLE `cart`
    ADD COLUMN `change_price_data` json NULL COMMENT '改价数据' AFTER `extra_data`;

CREATE TABLE `cart_global_change_price`  (
                                             `user_id` int NOT NULL,
                                             `change_price_type` tinyint NOT NULL,
                                             `change_price_value` decimal(10, 2) NOT NULL,
                                             PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;


ALTER TABLE `order`
    ADD COLUMN `cash_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '现金金额',
    ADD COLUMN `cash_change_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '现金金额(找零)',
    ADD COLUMN `round_off_dime_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '抹角金额',
    ADD COLUMN `round_off_cents_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '抹分金额',
    ADD COLUMN `pos_user_id` int DEFAULT '0' COMMENT '后台管理员id',
    ADD COLUMN `change_price_data` json NULL;

ALTER TABLE `order_item`
    ADD COLUMN `change_price_data` json NULL COMMENT '改价data(涉及到的购物车的改价数据)';