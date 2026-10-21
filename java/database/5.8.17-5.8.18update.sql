ALTER TABLE `merchant`
    ADD COLUMN `is_delete` TINYINT(1) NULL DEFAULT 0 COMMENT 'null或者0表示未删除 1 表示删除' AFTER `status`;

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('orderSendPointNode', '1', 1754384713, 1, 'admin', 1754384713, 1, 'admin', 0);

-- 会员订单已完成后多少天赠送积分；0表示立即赠送，大于0表示延迟N天后赠送
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('orderCompleteSendPointDays', '0', 1754384713, 1, 'admin', 1754384713, 1, 'admin', 0);

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001040, 'shopWithdrawApplySummary', '提现汇总', 11001048, 50, 1, '[]', '', '', 0, 'admin');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001041, 'vendorWithdrawApplySummary', '提现汇总', 11001052, 0, 1, '[]', '', '', 0, 'admin');

UPDATE `authority` SET `authority_sn` = 'cycleBuyManage', `authority_name` = '周期购订单', `parent_id` = 10000414, `sort_order` = 50, `is_show` = 1, `child_auth` = '[]', `route_link` = '', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 30001037;
UPDATE `authority` SET `authority_sn` = 'cycleBuyManage', `authority_name` = '周期购订单', `parent_id` = 20000037, `sort_order` = 50, `is_show` = 1, `child_auth` = '[]', `route_link` = '', `authority_ico` = '', `is_system` = 0, `admin_type` = 'store' WHERE `authority_id` = 40001026;


UPDATE config SET biz_val = '0' WHERE biz_code = 'useYabanpay';
UPDATE config SET biz_val = '0' WHERE biz_code = 'useYabanpayWechat';
UPDATE config SET biz_val = '0' WHERE biz_code = 'useYabanpayAlipay';