INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (20001001, 'storeAmapSetting', '地图设置', 55, 50, 1,
        '[{\"authName\":\"保存\",\"authSn\":\"storeSettingsUpdateManage\"}]', '', '', 0, 'admin');

UPDATE `authority`
SET `child_auth` = '[{\"auth_name\":\"编辑\",\"auth_sn\":\"productModifyManage\"},{\"auth_name\":\"相册修改\",\"auth_sn\":\"galleryModifyManage\"},{\"auth_name\":\"相册图片修改\",\"auth_sn\":\"galleryPicModifyManage\"},{\"auth_name\":\"相册管理\",\"auth_sn\":\"galleryManage\"},{\"auth_name\":\"相册图片管理\",\"auth_sn\":\"galleryPicManage\"},{\"auth_name\":\"视频管理\",\"auth_sn\":\"galleryVideoManage\"}]'
WHERE `authority_sn` = 'productManage'
  AND admin_type != 'admin';

-- 库存管理新增相关表
ALTER TABLE `product`
    ADD COLUMN `cost_price` decimal(10, 2) DEFAULT NULL COMMENT '成本价';

ALTER TABLE `product`
    ADD COLUMN `product_unit` varchar(10) DEFAULT NULL COMMENT '商品单位';

CREATE TABLE `product_stock_record`
(
    `product_stock_id` int     NOT NULL AUTO_INCREMENT,
    `stock_code`       varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '仓库编号',
    `in_or_out`        tinyint NOT NULL                                             DEFAULT '0' COMMENT '类型（1=入库，2=出库）',
    `type`             int                                                          DEFAULT NULL COMMENT '11=采购入库，12=退货入库，10=其他入库，21=过期出库，22=订单出库，23=其他出库',
    `add_time`         int                                                          DEFAULT NULL COMMENT '创建时间',
    `add_by`           varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '经办人',
    `comment`          text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '备注',
    `shop_id`          int                                                          DEFAULT NULL COMMENT '门店id',
    PRIMARY KEY (`product_stock_id`) USING BTREE,
    KEY                `product_stock_id` (`product_stock_id`) USING BTREE,
    KEY                `stock_code` (`stock_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='商品出入库记录';

CREATE TABLE `product_stock_record_detail`
(
    `record_detail_id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `stock_record_id`  int DEFAULT NULL COMMENT '库存记录id',
    `product_id`       int DEFAULT NULL COMMENT '商品id',
    `sku_id`           int DEFAULT NULL COMMENT '商品规格id',
    `quantity`         int unsigned DEFAULT '0' COMMENT '出入库数量',
    PRIMARY KEY (`record_detail_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='商品库存记录明细表';

CREATE TABLE `product_stock_adjust_record`
(
    `adjust_record_id`       int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sku_id`                 int                                                          DEFAULT NULL COMMENT '商品规格id',
    `adjust_type`            tinyint                                                      DEFAULT NULL COMMENT '调整类型（1=增量更新，0=覆盖更新）',
    `quantity`               int                                                          DEFAULT '0' COMMENT '调整数量',
    `add_by`                 varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作账号	',
    `add_time`               bigint                                                       DEFAULT NULL COMMENT '创建时间',
    `product_id`             int                                                          DEFAULT NULL COMMENT '商品id',
    `shop_id`                int                                                          DEFAULT NULL COMMENT '门店id',
    `change_before_quantity` int unsigned DEFAULT '0' COMMENT '调整前库存',
    `change_after_quantity`  int unsigned DEFAULT '0' COMMENT '调整后库存',
    PRIMARY KEY (`adjust_record_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='商品库存调整记录表';

CREATE TABLE `product_stock_allocate_record`
(
    `allocate_id`                 int NOT NULL AUTO_INCREMENT COMMENT '调拨主键',
    `allocate_code`               varchar(13) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  DEFAULT NULL COMMENT '调拨单号',
    `outbound_organization_id`    int                                                           DEFAULT NULL COMMENT '出库组织',
    `warehousing_organization_id` int                                                           DEFAULT NULL COMMENT '入库组织',
    `status`                      int                                                           DEFAULT NULL COMMENT '单据状态（1=待审核，2待调出，3=已完成，4=已驳回，5=已作废）	',
    `add_by`                      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '经办人',
    `add_time`                    bigint                                                        DEFAULT NULL COMMENT '创建时间	',
    `stock_in_comment`            text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '入库备注',
    `stock_out_comment`           text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '出库备注',
    `stock_allocate_comment`      text COLLATE utf8mb4_general_ci COMMENT '调拨备注',
    PRIMARY KEY (`allocate_id`) USING BTREE,
    KEY                           `adjust_record_id` (`allocate_id`) USING BTREE,
    KEY                           `allocate_code` (`allocate_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='库存调拨表';

CREATE TABLE `product_stock_allocate_record_detail`
(
    `allocate_detail_id` int NOT NULL AUTO_INCREMENT COMMENT '调拨详情主键',
    `allocate_id`        int DEFAULT NULL COMMENT '调拨单主键',
    `product_id`         int DEFAULT NULL COMMENT '商品id',
    `sku_id`             int DEFAULT NULL COMMENT '商品sku',
    `quantity`           int DEFAULT NULL COMMENT '调拨数量',
    `stock_in_quantity`  int unsigned DEFAULT '0' COMMENT '实际入库数量',
    `stock_out_quantity` int unsigned DEFAULT '0' COMMENT '实际出库数量',
    PRIMARY KEY (`allocate_detail_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='商品调拨详情';

CREATE TABLE `product_stock_allocate_operation_record`
(
    `allocate_operation_id`  int NOT NULL AUTO_INCREMENT COMMENT '调拨操作记录id',
    `allocate_id`            int                                                          DEFAULT NULL COMMENT '调拨记录id',
    `operation_type`         varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作描述',
    `add_time`               bigint                                                       DEFAULT NULL COMMENT '操作时间',
    `review_rejected_reason` text COLLATE utf8mb4_general_ci COMMENT '审核驳回原因',
    `add_by`                 varchar(32) COLLATE utf8mb4_general_ci                       DEFAULT NULL COMMENT '操作人',
    PRIMARY KEY (`allocate_operation_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='调拨操作记录表';

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (null, 'inventoryQueryManage', '库存查询', 10000411, 50, 1, '[]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (null, 'inventoryAdjustmentManage', '库存调整', 10000411, 52, 1, '[]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (null, 'inventoryQueryManage', '库存查询', 10000454, 50, 1, '[]', '', '', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (null, 'inventoryAdjustmentManage', '库存调整', 10000454, 50, 1, '[]', '', '', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (null, 'inventoryQueryManage', '库存查询', 20000043, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (null, 'inventoryAdjustmentManage', '库存调整', 20000043, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (null, 'inventoryGoodsArePutOutOfStorageManage', '商品出库', 10000411, 52, 1, '[]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (null, 'inventoryGoodsArePutOutOfStorageManage', '商品出库', 10000454, 50, 1, '[]', '', '', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (null, 'inventoryGoodsArePutOutOfStorageManage', '商品出库', 20000043, 50, 1, '[]', '', '', 0, 'store');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (null, 'inventoryGoodsArePutIntoStorageManage', '商品入库', 10000411, 51, 1, '[]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (null, 'inventoryGoodsArePutIntoStorageManage', '商品入库', 10000454, 50, 1, '[]', '', '', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (null, 'inventoryGoodsArePutIntoStorageManage', '商品入库', 20000043, 50, 1, '[]', '', '', 0, 'store');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (null, 'storeInventoryTransferManage', '库存调拨', 10000411, 54, 1, '[]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (null, 'storeInventoryTransferManage', '库存调拨', 10000454, 50, 1, '[]', '', '', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (null, 'storeInventoryTransferManage', '库存调拨', 20000043, 50, 1, '[]', '', '', 0, 'store');

