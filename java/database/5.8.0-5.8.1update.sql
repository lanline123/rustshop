
ALTER TABLE `product`
    MODIFY COLUMN `paid_content` TEXT NULL COMMENT '付费内容';


ALTER TABLE `product`
    ADD COLUMN `is_need_re_import_vendor_product` tinyint NULL DEFAULT 0 COMMENT '供应商商品是否需要重新导入';

ALTER TABLE `shop`
    ADD COLUMN `auto_erase_zero` TINYINT NOT NULL DEFAULT '0' COMMENT '店内收银自动抹零：0关闭 1开启' AFTER `shop_show_category`,
    ADD COLUMN `auto_deduction_points` TINYINT NOT NULL DEFAULT '0' COMMENT '收银积分基于订单自动抵扣：0关闭 1开启' AFTER `auto_erase_zero`,
    ADD COLUMN `default_collection_method` TINYINT NOT NULL DEFAULT '1' COMMENT '收银机默认收款方式：(默认)1-现金支付 2-扫码支付 3-余额支付 4-积分支付' AFTER `auto_deduction_points`,
    ADD COLUMN `auto_print_online_order` TINYINT NOT NULL DEFAULT '1' COMMENT '线上订单自动打印：0关闭 1开启' AFTER `default_collection_method`,
    ADD COLUMN `auto_print_offline_order` TINYINT NOT NULL DEFAULT '1' COMMENT '线下订单自动打印：0关闭 1开启' AFTER `auto_print_online_order`,
    ADD COLUMN `shop_common_remarks` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '店铺常用备注列表(JSON数组)' AFTER `auto_print_offline_order`;

ALTER TABLE `order`
    ADD COLUMN `discount_change_price` decimal(10, 2) NULL AFTER `pos_user_id`;

ALTER TABLE `order_item`
    ADD COLUMN `admin_change_pre_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '后台改价前价格';