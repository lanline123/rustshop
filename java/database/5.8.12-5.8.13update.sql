ALTER TABLE `product_gift`
    MODIFY COLUMN `gift_name` varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '赠品名称' AFTER `gift_id`;

ALTER TABLE `user_address` ADD COLUMN `is_system` tinyint NULL AFTER `is_default`;

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30001031, 'proxyShoppingManage', '代客下单', 10000414, 56, 1, '[]', '', '', 0, 'admin');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30001032, 'proxyShoppingCheck', '代客下单结算', 10000414, 55, 0, '[]', '', '', 0, 'admin');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001018, 'proxyShoppingManage', '代客下单', 10000444, 50, 1, '[]', '', '', 0, 'shop');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001019, 'proxyShoppingCheck', '代客下单结算', 10000444, 50, 0, '[]', '', '', 0, 'shop');

ALTER TABLE `user_authorize` CHARACTER SET = utf8mb4;