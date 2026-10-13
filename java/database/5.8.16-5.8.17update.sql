UPDATE `authority` SET `authority_sn` = 'homeServiceManage', `authority_name` = '上门服务', `parent_id` = 20000028, `sort_order` = 50, `is_show` = 1, `child_auth` = '[]', `route_link` = '', `authority_ico` = '', `is_system` = 0, `admin_type` = 'store' WHERE `authority_id` = 30001024;


INSERT INTO `authority`(`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES ('giftCardManage', '礼品卡', 20000046, 50, 1, '[{\"authName\":\"编辑\",\"authSn\":\"giftCardUpdateManage\"}]', '', '', 0, 'store');
