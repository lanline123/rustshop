-- =============================================================
-- 5.8.36 -> 5.8.37 增量脚本
-- 内容（本次三大需求 + bugfix）：
--   1. 支付：Apple Pay / 汇付斗拱 / 拉卡拉 三渠道 config 初始化
--   2. 店铺会员：shop_member 表 + 会员标签三表 shop_id + 总开关 config + 菜单
--   3. O2O 进店模式：进店规则菜单 + 进店模式/首次进店/销售员气泡 config
--   4. bugfix：D1076 素材中心权限四端回补
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- 幂等说明：config/authority/建表均有守卫；shop_member 的 ALTER ADD COLUMN 为一次性执行
-- =============================================================

SET NAMES utf8mb4;

-- #############################################################
-- 一、支付渠道（Apple Pay / 汇付 / 拉卡拉）
-- #############################################################

-- ============ 1.1 Apple Pay ============

-- 是否开启 Apple Pay
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'useApplePay', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'useApplePay');

-- 商户标识（Merchant ID）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'applePayMerchantId', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'applePayMerchantId');

-- 商户展示名
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'applePayDisplayName', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'applePayDisplayName');

-- 国家/地区（ISO 3166 两位码）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'applePayCountryCode', 'CN', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'applePayCountryCode');

-- 国家/地区列表（隐藏配置，界面不展示；供国家下拉选项，与 applePayCurrencyList 机制一致）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'applePayCountryList', '[{"name":"中国","value":"CN"},{"name":"中国香港","value":"HK"},{"name":"美国","value":"US"},{"name":"英国","value":"GB"},{"name":"日本","value":"JP"},{"name":"澳大利亚","value":"AU"},{"name":"加拿大","value":"CA"},{"name":"新加坡","value":"SG"},{"name":"德国","value":"DE"},{"name":"法国","value":"FR"}]', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'applePayCountryList');

-- 货币（ISO 4217 三位码）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'applePayCurrency', 'CNY', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'applePayCurrency');

-- 货币列表（隐藏配置，界面不展示；与 stripeCurrencyList 机制一致）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'applePayCurrencyList', '[{"name":"人民币","value":"CNY"},{"name":"美元","value":"USD"},{"name":"港币","value":"HKD"},{"name":"欧元","value":"EUR"},{"name":"英镑","value":"GBP"},{"name":"日元","value":"JPY"},{"name":"澳元","value":"AUD"},{"name":"加元","value":"CAD"},{"name":"新加坡元","value":"SGD"}]', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'applePayCurrencyList');

-- 支持卡组织（逗号分隔）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'applePaySupportedNetworks', 'chinaUnionPay,visa,masterCard', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'applePaySupportedNetworks');

-- 商户身份证书已上传状态
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'applePayMerchantIdentityCert', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'applePayMerchantIdentityCert');

-- 商户身份证书私钥已上传状态
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'applePayMerchantIdentityKey', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'applePayMerchantIdentityKey');

-- 支付处理证书私钥已上传状态
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'applePayProcessingPrivateKey', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'applePayProcessingPrivateKey');

-- 收单机构（ums/stripe/adyen/generic）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'applePayAcquirerProvider', 'generic', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'applePayAcquirerProvider');

-- 银联商务签名算法（SHA256/SM3）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'applePayUmsSignType', 'SHA256', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'applePayUmsSignType');

-- 收单网关地址
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'applePayAcquirerGateway', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'applePayAcquirerGateway');

-- 收单商户号
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'applePayAcquirerMerchantNo', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'applePayAcquirerMerchantNo');

-- 收单密钥
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'applePayAcquirerKey', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'applePayAcquirerKey');

-- ============ 1.2 汇付斗拱 huifu ============

-- -------------------------------------------------------------
-- 1. 汇付支付配置项（幂等：仅当 biz_code 不存在时插入）
--    useHuifu 单一总开关默认 '0'（关闭）；开启后微信/支付宝均可用。密钥/网关默认空。
-- -------------------------------------------------------------
INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'useHuifu', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'useHuifu');

INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'huifuSysId', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'huifuSysId');

INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'huifuProductId', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'huifuProductId');

INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'huifuMerchantId', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'huifuMerchantId');

INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'huifuRsaPrivateKey', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'huifuRsaPrivateKey');

INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'huifuPublicKey', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'huifuPublicKey');

INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'huifuGateway', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'huifuGateway');

-- -------------------------------------------------------------
-- 2. paymentOrder 追加 huifu 父级（若已存在 paymentOrder 且未包含 huifu 才追加）
--    huifu 为父级，代码内映射 huifuWechat/huifuAlipay。
-- -------------------------------------------------------------
UPDATE `config`
SET `biz_val` = CONCAT(`biz_val`, ',huifu'), `update_time` = UNIX_TIMESTAMP()
WHERE `biz_code` = 'paymentOrder'
  AND `biz_val` IS NOT NULL
  AND `biz_val` <> ''
  AND `biz_val` NOT LIKE '%huifu%';

-- ============ 1.3 拉卡拉 lakala ============

-- 是否启用拉卡拉支付（0-关闭，1-开启）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'useLakala', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'useLakala' AND `is_del` = 0);

-- 拉卡拉开放平台 AppId
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'lakalaAppId', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'lakalaAppId' AND `is_del` = 0);

-- 拉卡拉商户号（merchantNo）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'lakalaMerchantNo', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'lakalaMerchantNo' AND `is_del` = 0);

-- 拉卡拉终端号（termNo）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'lakalaTermNo', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'lakalaTermNo' AND `is_del` = 0);

-- 拉卡拉商户证书序列号（serialNo）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'lakalaSerialNo', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'lakalaSerialNo' AND `is_del` = 0);

-- 拉卡拉商户私钥（字符串，PKCS8）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'lakalaMerchantPrivateKey', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'lakalaMerchantPrivateKey' AND `is_del` = 0);

-- 拉卡拉开放平台证书（字符串，.cer 内容）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'lakalaPlatformCert', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'lakalaPlatformCert' AND `is_del` = 0);

-- 拉卡拉报文对称加密 SM4 密钥（可选）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'lakalaSm4Key', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'lakalaSm4Key' AND `is_del` = 0);

-- 拉卡拉网关地址（生产 https://s2.lakala.com，测试 https://test.wsmsd.cn/sit）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'lakalaGatewayUrl', 'https://s2.lakala.com', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'lakalaGatewayUrl' AND `is_del` = 0);

-- 拉卡拉沙箱/测试环境开关（0-生产，1-沙箱）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'lakalaSandbox', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'lakalaSandbox' AND `is_del` = 0);

-- 拉卡拉微信子 AppId（公众号 JSAPI 场景）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'lakalaWxSubAppid', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'lakalaWxSubAppid' AND `is_del` = 0);

-- 拉卡拉微信小程序 AppId（小程序场景）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'lakalaWxMiniAppid', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'lakalaWxMiniAppid' AND `is_del` = 0);

-- #############################################################
-- 二、店铺会员（D1116-shopMember）
-- #############################################################

-- =============================================================
-- A. 店铺-会员绑定表
-- 开关开启后用户在某店下单支付即成为该店会员；按 (shop_id,user_id) 唯一。
-- order_amount/order_count 按「已支付」累加；join_time 首次入会；last_consume_time 最近已支付。
-- =============================================================
CREATE TABLE IF NOT EXISTS `shop_member` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`           INT          NOT NULL DEFAULT 0 COMMENT '关联 user.user_id',
  `shop_id`           INT          NOT NULL DEFAULT 0 COMMENT '关联 shop.shop_id（门店/店铺统一）',
  `order_count`       INT          NOT NULL DEFAULT 0 COMMENT '该店累计已支付订单数',
  `order_amount`      DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '该店累计消费金额（已支付）',
  `join_time`         BIGINT       DEFAULT NULL COMMENT '入会时间（首次成为会员，Unix秒）',
  `last_consume_time` BIGINT       DEFAULT NULL COMMENT '最近消费时间（最近一次已支付，Unix秒）',
  `create_time`       BIGINT       DEFAULT NULL COMMENT '创建时间',
  `update_time`       BIGINT       DEFAULT NULL COMMENT '更新时间',
  `is_del`            TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除；0-否，1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_user` (`shop_id`, `user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺会员绑定表';

-- =============================================================
-- B. 会员标签三表新增 shop_id（DEFAULT 0 → 历史数据自动归平台）
-- 说明：MySQL 5.7/8.0 无「ADD COLUMN IF NOT EXISTS」通用语法，
--       升级脚本按顺序单次执行；若已手工补过列，请忽略对应报错。
-- =============================================================
ALTER TABLE `member_tag_group` ADD COLUMN `shop_id` INT NOT NULL DEFAULT 0 COMMENT '店铺ID，0=平台' AFTER `id`;
ALTER TABLE `member_tag`       ADD COLUMN `shop_id` INT NOT NULL DEFAULT 0 COMMENT '店铺ID，0=平台' AFTER `id`;
ALTER TABLE `user_member_tag`  ADD COLUMN `shop_id` INT NOT NULL DEFAULT 0 COMMENT '店铺ID，0=平台' AFTER `id`;

-- =============================================================
-- C. 平台「店铺会员」总开关 + 店铺客户设置（默认全关；CR-20260708-1530）
--   shopMemberEnabled     店铺会员功能总开关
--   shopMemberDesensitize 店铺客户信息脱敏（开启后店铺端客户电话/姓名脱敏）
--   shopMemberAllowAdd    店铺添加客户
--   shopMemberAllowImport 店铺导入客户
-- =============================================================
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'shopMemberEnabled', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'shopMemberEnabled' AND `is_del` = 0);

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'shopMemberDesensitize', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'shopMemberDesensitize' AND `is_del` = 0);

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'shopMemberAllowAdd', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'shopMemberAllowAdd' AND `is_del` = 0);

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'shopMemberAllowImport', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'shopMemberAllowImport' AND `is_del` = 0);

-- =============================================================
-- D. 后台菜单（authority；按 authority_sn + admin_type 幂等，authority_id 自增）
--   平台端：会员管理(10000422) 下「店铺会员」（店铺列表 + 会员列表，只读；含总开关设置按钮）
--   店铺端/门店端：新增顶级「客户」菜单，下挂「客户管理(会员列表)」「客户标签」
--   权限 authority_sn 与控制器 @PreAuthorize 对应：
--     shopMemberSettingManage=平台总开关设置；shopMemberManage=平台店铺会员只读；
--     shopMemberCustomerManage=店铺端会员列表；memberTagManage=标签管理(平台/店铺/门店复用)
-- =============================================================

-- ---- 平台端：会员设置（会员管理 10000422 下）----
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'shopMemberSettingManage','会员设置', 10000422, 50, 1, '[{"auth_name":"设置","auth_sn":"shopMemberSettingManage"}]', '', '', 0, 'admin'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='shopMemberSettingManage' AND `admin_type`='admin');

-- ---- 店铺端(shop)：顶级「会员」----
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'user','会员', 0, 55, 1, '[]', '', 'iconfont-tig icon-huiyuan', 0, 'shop'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='memberCustomer' AND `admin_type`='shop');
-- 店铺 会员 --> 会员管理
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'userManagement','会员管理', (SELECT `authority_id` FROM (SELECT `authority_id` FROM `authority` WHERE `authority_sn`='user' AND `admin_type`='shop' LIMIT 1) p), 50, 1, '[]', '', 'iconfont-tig icon-huiyuan', 0, 'shop'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='userManagement' AND `admin_type`='shop');

-- ---- 店铺端(shop)：客户管理（会员列表）----
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'shopMemberCustomerManage','会员管理',
  (SELECT `authority_id` FROM (SELECT `authority_id` FROM `authority` WHERE `authority_sn`='userManagement' AND `admin_type`='shop' LIMIT 1) p),
  10, 1, '[]', '', 'iconfont-tig icon-wodezhuye', 0, 'shop'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='shopMemberCustomerManage' AND `admin_type`='shop');

-- ---- 店铺端(shop)：客户标签 ----
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'memberTagManage','会员标签',
  (SELECT `authority_id` FROM (SELECT `authority_id` FROM `authority` WHERE `authority_sn`='userManagement' AND `admin_type`='shop' LIMIT 1) p),
  20, 1, '[{"auth_name":"编辑","auth_sn":"memberTagUpdateManage"}]', '', '', 0, 'shop'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='memberTagManage' AND `admin_type`='shop');

-- ---- 门店端(store)：顶级「客户」----
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'user','会员', 0, 55, 1, '[]', '', 'iconfont-tig icon-huiyuan', 0, 'store'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='memberCustomer' AND `admin_type`='store');

INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'userManagement','会员管理', (SELECT `authority_id` FROM (SELECT `authority_id` FROM `authority` WHERE `authority_sn`='user' AND `admin_type`='store' LIMIT 1) p), 50, 1, '[]', '', '', 0, 'store'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='userManagement' AND `admin_type`='store');

-- ---- 门店端(store)：客户管理（会员列表）----
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'shopMemberCustomerManage','会员管理',
  (SELECT `authority_id` FROM (SELECT `authority_id` FROM `authority` WHERE `authority_sn`='userManagement' AND `admin_type`='store' LIMIT 1) p),
  10, 1, '[]', '', 'iconfont-tig icon-wodezhuye', 0, 'store'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='shopMemberCustomerManage' AND `admin_type`='store');

-- ---- 门店端(store)：客户标签 ----
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'memberTagManage','会员标签',
  (SELECT `authority_id` FROM (SELECT `authority_id` FROM `authority` WHERE `authority_sn`='userManagement' AND `admin_type`='store' LIMIT 1) p),
  20, 1, '[{"auth_name":"编辑","auth_sn":"memberTagUpdateManage"}]', '', '', 0, 'store'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='memberTagManage' AND `admin_type`='store');

-- #############################################################
-- 三、O2O 进店模式（D1114-o2o-store-order）
-- #############################################################

-- 清理历史误用的 authority_id（旧值 100003900），改用 100005901
-- 存量库该菜单已按旧 id 写入，若不删除，下方 INSERT 的 NOT EXISTS 守卫会命中旧行导致新 id 写不进去
-- 幂等：无旧行时不命中；已迁移到新 id 的库不匹配旧 id，均安全
DELETE FROM `authority` WHERE `authority_id` = 100003900 AND `authority_sn` = 'storeEntryRuleSetting';

-- 后台菜单：进店规则（父级 39=商城设置，与小程序设置同级）
-- child_auth 含「编辑」子权限 storeEntryRuleUpdateManage，用于控制保存进店规则的权限
INSERT INTO `authority`
    (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 100005901, 'storeEntryRuleSetting', '进店规则', 39, 50, 1, '[{"authName":"编辑","authSn":"storeEntryRuleUpdateManage"}]', '', '', 0, 'admin'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn` = 'storeEntryRuleSetting');

-- 存量行补齐编辑子权限（幂等）
UPDATE `authority`
SET `child_auth` = '[{"authName":"编辑","authSn":"storeEntryRuleUpdateManage"}]'
WHERE `authority_sn` = 'storeEntryRuleSetting';

-- 进店规则默认配置：1-模式一
INSERT INTO `config`
    (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'storeEntryRule', '1', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'storeEntryRule');

-- =============================================================
-- 进店规则扩展（CR-20260708-1109）
-- storeEntryRule 语义扩为进店模式 1-4；新增首次进店规则 + 销售员进店气泡 5 项配置
-- 后端仅存储，选店逻辑由前端实现。幂等：NOT EXISTS 判断，可重复执行
-- =============================================================

-- 首次进店方式：1-按规则进店（默认）
INSERT INTO `config`
    (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'storeEntryFirstType', '1', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'storeEntryFirstType');

-- 首次进店规则清单（默认三条规则全启用，按优先级 1/2/3）
INSERT INTO `config`
    (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'storeEntryFirstRules', '[{"ruleType":1,"enabled":1,"priority":1},{"ruleType":2,"enabled":1,"priority":2},{"ruleType":3,"enabled":1,"priority":3}]', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'storeEntryFirstRules');

-- 按指定门店进网时的门店ID：0-未选
INSERT INTO `config`
    (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'storeEntryDesignatedShopId', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'storeEntryDesignatedShopId');

-- 销售员进店气泡：1-进入推广店铺门店（默认）
INSERT INTO `config`
    (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'salesmanEntryBubbleType', '1', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'salesmanEntryBubbleType');

-- 销售员进店气泡指定门店ID：0-未选
INSERT INTO `config`
    (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'salesmanEntryShopId', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'salesmanEntryShopId');

-- #############################################################
-- 四、bugfix：素材中心权限回补（D1121-bugfix）
-- #############################################################

-- ------------------------------------------------------------
-- 平台端 admin（父菜单锚点：content）
-- ------------------------------------------------------------
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'materialManagement', '素材管理',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='content' AND `admin_type`='admin' LIMIT 1) a),
       6, 1, '[]', '', 'iconfont-tig icon-jingying', 0, 'admin'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='content' AND `admin_type`='admin' LIMIT 1) c)
  AND NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='admin') m);

INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'materialCenter', '素材中心',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='admin' LIMIT 1) a),
       50, 1, '[{"authName":"相册修改","authSn":"galleryModifyManage"},{"authName":"相册图片修改","authSn":"galleryPicModifyManage"},{"authName":"相册管理","authSn":"galleryManage"},{"authName":"相册图片管理","authSn":"galleryPicManage"},{"authName":"视频管理","authSn":"galleryVideoManage"}]', '', '', 0, 'admin'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='admin' LIMIT 1) p)
  AND NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='materialCenter' AND `admin_type`='admin') m);

UPDATE `authority`
SET `child_auth` = '[{"authName":"相册修改","authSn":"galleryModifyManage"},{"authName":"相册图片修改","authSn":"galleryPicModifyManage"},{"authName":"相册管理","authSn":"galleryManage"},{"authName":"相册图片管理","authSn":"galleryPicManage"},{"authName":"视频管理","authSn":"galleryVideoManage"}]'
WHERE `authority_sn`='materialCenter' AND `admin_type`='admin'
  AND (`child_auth` IS NULL OR `child_auth`='' OR `child_auth`='[]' OR `child_auth` NOT LIKE '%galleryVideoManage%');

-- ------------------------------------------------------------
-- 店铺端 shop（父菜单锚点：setting）
-- ------------------------------------------------------------
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'materialManagement', '素材管理',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='setting' AND `admin_type`='shop' LIMIT 1) a),
       6, 1, '[]', '', 'iconfont-tig icon-jingying', 0, 'shop'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='setting' AND `admin_type`='shop' LIMIT 1) c)
  AND NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='shop') m);

INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'materialCenter', '素材中心',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='shop' LIMIT 1) a),
       50, 1, '[{"authName":"相册修改","authSn":"galleryModifyManage"},{"authName":"相册图片修改","authSn":"galleryPicModifyManage"},{"authName":"相册管理","authSn":"galleryManage"},{"authName":"相册图片管理","authSn":"galleryPicManage"},{"authName":"视频管理","authSn":"galleryVideoManage"}]', '', '', 0, 'shop'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='shop' LIMIT 1) p)
  AND NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='materialCenter' AND `admin_type`='shop') m);

UPDATE `authority`
SET `child_auth` = '[{"authName":"相册修改","authSn":"galleryModifyManage"},{"authName":"相册图片修改","authSn":"galleryPicModifyManage"},{"authName":"相册管理","authSn":"galleryManage"},{"authName":"相册图片管理","authSn":"galleryPicManage"},{"authName":"视频管理","authSn":"galleryVideoManage"}]'
WHERE `authority_sn`='materialCenter' AND `admin_type`='shop'
  AND (`child_auth` IS NULL OR `child_auth`='' OR `child_auth`='[]' OR `child_auth` NOT LIKE '%galleryVideoManage%');

-- ------------------------------------------------------------
-- 门店端 store（父菜单锚点：setting）
-- ------------------------------------------------------------
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'materialManagement', '素材管理',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='setting' AND `admin_type`='store' LIMIT 1) a),
       6, 1, '[]', '', 'iconfont-tig icon-jingying', 0, 'store'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='setting' AND `admin_type`='store' LIMIT 1) c)
  AND NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='store') m);

INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'materialCenter', '素材中心',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='store' LIMIT 1) a),
       50, 1, '[{"authName":"相册修改","authSn":"galleryModifyManage"},{"authName":"相册图片修改","authSn":"galleryPicModifyManage"},{"authName":"相册管理","authSn":"galleryManage"},{"authName":"相册图片管理","authSn":"galleryPicManage"},{"authName":"视频管理","authSn":"galleryVideoManage"}]', '', '', 0, 'store'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='store' LIMIT 1) p)
  AND NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='materialCenter' AND `admin_type`='store') m);

UPDATE `authority`
SET `child_auth` = '[{"authName":"相册修改","authSn":"galleryModifyManage"},{"authName":"相册图片修改","authSn":"galleryPicModifyManage"},{"authName":"相册管理","authSn":"galleryManage"},{"authName":"相册图片管理","authSn":"galleryPicManage"},{"authName":"视频管理","authSn":"galleryVideoManage"}]'
WHERE `authority_sn`='materialCenter' AND `admin_type`='store'
  AND (`child_auth` IS NULL OR `child_auth`='' OR `child_auth`='[]' OR `child_auth` NOT LIKE '%galleryVideoManage%');

-- ------------------------------------------------------------
-- 供应商端 vendor（父菜单锚点：setting）
-- ------------------------------------------------------------
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'materialManagement', '素材管理',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='setting' AND `admin_type`='vendor' LIMIT 1) a),
       6, 1, '[]', '', 'iconfont-tig icon-jingying', 0, 'vendor'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='setting' AND `admin_type`='vendor' LIMIT 1) c)
  AND NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='vendor') m);

INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'materialCenter', '素材中心',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='vendor' LIMIT 1) a),
       50, 1, '[{"authName":"相册修改","authSn":"galleryModifyManage"},{"authName":"相册图片修改","authSn":"galleryPicModifyManage"},{"authName":"相册管理","authSn":"galleryManage"},{"authName":"相册图片管理","authSn":"galleryPicManage"},{"authName":"视频管理","authSn":"galleryVideoManage"}]', '', '', 0, 'vendor'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='vendor' LIMIT 1) p)
  AND NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='materialCenter' AND `admin_type`='vendor') m);

UPDATE `authority`
SET `child_auth` = '[{"authName":"相册修改","authSn":"galleryModifyManage"},{"authName":"相册图片修改","authSn":"galleryPicModifyManage"},{"authName":"相册管理","authSn":"galleryManage"},{"authName":"相册图片管理","authSn":"galleryPicManage"},{"authName":"视频管理","authSn":"galleryVideoManage"}]'
WHERE `authority_sn`='materialCenter' AND `admin_type`='vendor'
  AND (`child_auth` IS NULL OR `child_auth`='' OR `child_auth`='[]' OR `child_auth` NOT LIKE '%galleryVideoManage%');
