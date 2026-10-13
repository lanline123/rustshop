delete from `authority` where authority_id =  40001017;

-- 周期购配送计划表：新增库存扣减状态追踪字段
-- 用于精确判断子订单库存预警，解决"刚好扣完库存"被误报为库存不足的问题
ALTER TABLE order_cycle_delivery
    ADD COLUMN stock_deducted TINYINT(1) DEFAULT NULL COMMENT '库存是否已扣减：NULL-未知(旧数据)，0-未扣减(库存不足)，1-已扣减'
        AFTER generate_time;

ALTER TABLE product MODIFY COLUMN product_name VARCHAR(768) NOT NULL DEFAULT '' COMMENT '商品名称';

ALTER TABLE vendor_product MODIFY COLUMN product_name VARCHAR(768) NOT NULL DEFAULT '' COMMENT '商品名称';


INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001050, 'productBatch', '批量操作', 20000002, 51, 1, '[]', 'product/product_batch/', 'iconfont-tig icon-piliangcaozuo', 0, 'store');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001051, 'productBatchImageManage', '图片批量处理', 40001050, 55, 0, '[]', 'product/product_batch/image/list', '', 0, 'store');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001052, 'productBatchExportManage', '批量导出', 40001050, 51, 1, '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"productBatchExportUpdateManage\"}]', 'product/product_batch/export/list', '', 0, 'store');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001053, 'productBatchAddManage', '批量上传', 40001050, 52, 1, '[{\"auth_name\":\"\\u6279\\u91cf\\u4e0a\\u4f20\",\"auth_sn\":\"productBatchModifyManage\"}]', 'product/product_batch/add/list', '', 0, 'store');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001054, 'productBatchEditManage', '批量修改', 40001050, 54, 1, '[{\"auth_name\":\"\\u6279\\u91cf\\u4fee\\u6539\",\"auth_sn\":\"productBatchEditManage\"}]', 'product/product_batch/edit/list', '', 0, 'store');




INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001060, 'productBatch', '批量操作', 10001, 51, 1, '[]', 'product/product_batch/', 'iconfont-tig icon-piliangcaozuo', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001061, 'productBatchImageManage', '图片批量处理', 40001060, 55, 0, '[]', 'product/product_batch/image/list', '', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001062, 'productBatchExportManage', '批量导出', 40001060, 51, 1, '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"productBatchExportUpdateManage\"}]', 'product/product_batch/export/list', '', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001063, 'productBatchAddManage', '批量上传', 40001060, 52, 1, '[{\"auth_name\":\"\\u6279\\u91cf\\u4e0a\\u4f20\",\"auth_sn\":\"productBatchModifyManage\"}]', 'product/product_batch/add/list', '', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001064, 'productBatchEditManage', '批量修改', 40001060, 54, 1, '[{\"auth_name\":\"\\u6279\\u91cf\\u4fee\\u6539\",\"auth_sn\":\"productBatchEditManage\"}]', 'product/product_batch/edit/list', '', 0, 'shop');


INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001070, 'productBatch', '批量操作', 11000000, 51, 1, '[]', 'product/product_batch/', 'iconfont-tig icon-piliangcaozuo', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001071, 'productBatchImageManage', '图片批量处理', 40001070, 55, 0, '[]', 'product/product_batch/image/list', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001072, 'productBatchExportManage', '批量导出', 40001070, 51, 1, '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"productBatchExportUpdateManage\"}]', 'product/product_batch/export/list', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001073, 'productBatchAddManage', '批量上传', 40001070, 52, 1, '[{\"auth_name\":\"\\u6279\\u91cf\\u4e0a\\u4f20\",\"auth_sn\":\"productBatchModifyManage\"}]', 'product/product_batch/add/list', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001074, 'productBatchEditManage', '批量修改', 40001070, 54, 1, '[{\"auth_name\":\"\\u6279\\u91cf\\u4fee\\u6539\",\"auth_sn\":\"productBatchEditManage\"}]', 'product/product_batch/edit/list', '', 0, 'vendor');


CREATE TABLE `price_inquiry_reply_record`
(
    `id`         int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `inquiry_id` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '询价id',
    `reply_type` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '回复类型：1商家回复 2用户回复',
    `reply_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '回复人名称',
    `content`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '回复内容',
    `admin_id`   int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '后台管理员id',
    `user_id`    int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户id',
    `create_time` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '回复时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_inquiry_id` (`inquiry_id`) USING BTREE,
    KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '询价回复记录表' ROW_FORMAT = DYNAMIC;

ALTER TABLE `price_inquiry`
    ADD COLUMN `user_id` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户id' AFTER `shop_id`;

ALTER TABLE `price_inquiry`
    ADD INDEX `idx_user_id` (`user_id`);

ALTER TABLE `price_inquiry`
    ADD COLUMN `user_is_del` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户侧是否删除：0否 1是' AFTER `user_id`;

ALTER TABLE `price_inquiry`
    ADD INDEX `idx_user_id_user_is_del` (`user_id`, `user_is_del`);


INSERT INTO `authority`(`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001038, 'shopQuotationManage', '询价管理', 10001, 50, 1, '[]', '', 'iconfont-tig icon-xunjiaguanli', 0, 'shop');
INSERT INTO `authority`(`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001039, 'shopEnquiryManage', '商品询价', 40001038, 50, 1, '[]', '', '', 0, 'shop');
