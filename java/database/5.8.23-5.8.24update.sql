-- =====================================================
-- 短信通知多服务商扩展 - 初始化配置数据（含模拟数据）
-- 说明：对已有数据库执行，初始化新增服务商的 config 配置项
-- 幂等：不存在则插入，已存在则更新
-- =====================================================

-- 短信服务商选择（默认1=阿里云）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsProvider', '1', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsProvider');
UPDATE `config` SET `biz_val` = '1', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsProvider';

-- ========== 腾讯云短信 ==========
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsTencentSecretId', 'AKIDz8krbsJ5yKBZQpn74WFkmLPx3EXAMPLE', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsTencentSecretId');
UPDATE `config` SET `biz_val` = 'AKIDz8krbsJ5yKBZQpn74WFkmLPx3EXAMPLE', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsTencentSecretId';

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsTencentSecretKey', 'Gu5t9xGARNpq86cd98joQYCN3EXAMPLE', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsTencentSecretKey');
UPDATE `config` SET `biz_val` = 'Gu5t9xGARNpq86cd98joQYCN3EXAMPLE', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsTencentSecretKey';

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsTencentSdkAppId', '1400006666', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsTencentSdkAppId');
UPDATE `config` SET `biz_val` = '1400006666', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsTencentSdkAppId';

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsTencentSignName', '佰商科技', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsTencentSignName');
UPDATE `config` SET `biz_val` = '佰商科技', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsTencentSignName';

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsTencentShopMobile', '15083807138', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsTencentShopMobile');
UPDATE `config` SET `biz_val` = '15083807138', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsTencentShopMobile';

-- ========== 华为云短信 ==========
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsHuaweiAppKey', 'c8RWg3ggEcyd4D3p94bf3Y7x1EXAMPLE', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsHuaweiAppKey');
UPDATE `config` SET `biz_val` = 'c8RWg3ggEcyd4D3p94bf3Y7x1EXAMPLE', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsHuaweiAppKey';

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsHuaweiAppSecret', 'Y3pSn4b2fWk9d6rJm8vEXAMPLE123456', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsHuaweiAppSecret');
UPDATE `config` SET `biz_val` = 'Y3pSn4b2fWk9d6rJm8vEXAMPLE123456', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsHuaweiAppSecret';

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsHuaweiChannel', 'csms12345678', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsHuaweiChannel');
UPDATE `config` SET `biz_val` = 'csms12345678', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsHuaweiChannel';

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsHuaweiSignName', '佰商科技', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsHuaweiSignName');
UPDATE `config` SET `biz_val` = '佰商科技', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsHuaweiSignName';

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsHuaweiEndpoint', 'https://smsapi.cn-north-4.myhuaweicloud.com:443', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsHuaweiEndpoint');
UPDATE `config` SET `biz_val` = 'https://smsapi.cn-north-4.myhuaweicloud.com:443', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsHuaweiEndpoint';

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsHuaweiShopMobile', '15083807138', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsHuaweiShopMobile');
UPDATE `config` SET `biz_val` = '15083807138', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsHuaweiShopMobile';

-- ========== 火山引擎短信 ==========
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsVolcengineAccessKey', 'AKLTMjI2ODQ1ZDdhEXAMPLE', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsVolcengineAccessKey');
UPDATE `config` SET `biz_val` = 'AKLTMjI2ODQ1ZDdhEXAMPLE', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsVolcengineAccessKey';

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsVolcengineSecretKey', 'TnpRek1qWTROamRoWVRVEXAMPLE', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsVolcengineSecretKey');
UPDATE `config` SET `biz_val` = 'TnpRek1qWTROamRoWVRVEXAMPLE', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsVolcengineSecretKey';

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsVolcengineSmsAccount', 'smsAccount00001', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsVolcengineSmsAccount');
UPDATE `config` SET `biz_val` = 'smsAccount00001', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsVolcengineSmsAccount';

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsVolcengineSignName', '佰商科技', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsVolcengineSignName');
UPDATE `config` SET `biz_val` = '佰商科技', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsVolcengineSignName';

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'smsVolcengineShopMobile', '15083807138', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'smsVolcengineShopMobile');
UPDATE `config` SET `biz_val` = '15083807138', `update_time` = UNIX_TIMESTAMP() WHERE `biz_code` = 'smsVolcengineShopMobile';
