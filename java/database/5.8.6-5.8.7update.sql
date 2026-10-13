-- Stripe 支付配置
-- 是否启用Stripe支付（0-关闭，1-开启）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('useStripe', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Stripe货币类型（默认usd）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('stripeCurrency', 'usd', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Stripe密钥（Secret Key）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('stripeSecretKey', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Stripe公钥（Publishable Key）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('stripePublishableKey', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Stripe Webhook密钥
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('stripeWebhookSecret', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Stripe货币列表
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('stripeCurrencyList', '[{"name":"美元","value":"usd"},{"name":"欧元","value":"eur"},{"name":"英镑","value":"gbp"},{"name":"日元","value":"jpy"},{"name":"加元","value":"cad"},{"name":"澳元","value":"aud"},{"name":"港元","value":"hkd"},{"name":"新加坡元","value":"sgd"},{"name":"人民币","value":"cny"},{"name":"新西兰元","value":"nzd"},{"name":"瑞士法郎","value":"chf"},{"name":"丹麦克朗","value":"dkk"},{"name":"挪威克朗","value":"nok"},{"name":"瑞典克朗","value":"sek"},{"name":"墨西哥比索","value":"mxn"},{"name":"巴西雷亚尔","value":"brl"},{"name":"印度卢比","value":"inr"},{"name":"泰铢","value":"thb"},{"name":"韩元","value":"krw"},{"name":"马来西亚林吉特","value":"myr"}]', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);


INSERT INTO `translations_data` (`id`, `locale_id`, `translation_name`, `translation_key`, `translation_value`, `data_type`, `data_id`) VALUES (null, 1, '订单金额未满足stripe支付的最低金额要求,请选择其他支付方式', 'a611fd9f59433ae8538a20cbcbcc4a72', '订单金额未满足stripe支付的最低金额要求,请选择其他支付方式', 0, NULL);
INSERT INTO `translations_data` (`id`, `locale_id`, `translation_name`, `translation_key`, `translation_value`, `data_type`, `data_id`) VALUES (null, 2, '订单金额未满足stripe支付的最低金额要求,请选择其他支付方式', 'a611fd9f59433ae8538a20cbcbcc4a72', 'The order amount does not meet the minimum amount requirement for stripe payment, please select another payment method.', 0, NULL);
INSERT INTO `translations` (`id`, `translation_name`, `translation_key`, `data_type`, `shop_id`) VALUES (null, '订单金额未满足stripe支付的最低金额要求,请选择其他支付方式', 'a611fd9f59433ae8538a20cbcbcc4a72', 0, 0);

UPDATE authority set authority_ico = "iconfont-tig icon-user-auth" where authority_ico = "iconfont-tig icon-shimingrenzheng";