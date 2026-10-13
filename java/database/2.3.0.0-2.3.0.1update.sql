DELETE FROM authority WHERE authority_sn = 'a';
DELETE FROM authority WHERE authority_sn = 'logisticsManage';
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (10000384, 'logisticsManage', '配送设置', 55, 50, 1, '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"settingSaveShippingManage\"}]', 'setting/config/logistics/', '', 0, 'admin');
INSERT INTO `config` (`id`, `biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES (6862, 'alipayMobileAppid', '2021004141601111', 1751938370, 1, 'admin', 1751938370, 1, 'admin', 0);

INSERT INTO authority (authority_sn, authority_name, parent_id, sort_order, is_show, child_auth, route_link, authority_ico, is_system, admin_type) VALUES ('mobileSplashAdManage', '开屏广告', 27, 50, 1, '[]', '', '', 0, 'admin');