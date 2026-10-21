-- =============================================================
-- 5.8.29 -> 5.8.30 增量脚本
-- 内容：门店独立售价覆盖周期购/次卡价格（store_product_cycle_spec）
-- 背景：
--   「门店独立售价」(config.storeAssignProductPrice=1) 此前只覆盖
--   store_product.product_price / store_product_sku.sku_price，
--   而周期购/次卡的实际售价存在平台级 product_cycle_spec.cycle_price（无 shop_id），
--   导致门店编辑平台分配的周期购/次卡商品时，周期价/次卡售价无法独立保存。
-- 方案：
--   新增门店级周期价覆盖表 store_product_cycle_spec，按业务稳定键
--   (shop_id, product_id, sku_id, period_num) 覆盖，不污染平台共享的 product_cycle_spec，
--   也不受平台每次编辑「删除重建 product_cycle_spec」影响（cycle_spec_id 不稳定，故不作为键）。
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- 范围：仅 O2O 版本数据库
-- 关联：.ai-os/baseline-log/CR-20260601-142300-store-independent-cycle-price.md
-- =============================================================

CREATE TABLE IF NOT EXISTS `store_product_cycle_spec` (
    `store_cycle_spec_id` int unsigned    NOT NULL AUTO_INCREMENT COMMENT '门店周期价覆盖ID',
    `shop_id`             int unsigned    NOT NULL COMMENT '门店ID',
    `product_id`          mediumint unsigned NOT NULL COMMENT '商品ID',
    `sku_id`              int unsigned    NOT NULL DEFAULT 0 COMMENT 'SKU ID（统一设置时为0，与 product_cycle_spec.sku_id 的 NULL 对齐）',
    `period_num`          tinyint unsigned NOT NULL COMMENT '周期规格（期数），与 product_cycle_spec.period_num 对齐',
    `cycle_price`         decimal(10,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '门店覆盖周期价（实际售价）',
    `original_price`      decimal(10,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '门店覆盖划线价',
    `create_time`         int unsigned    NOT NULL DEFAULT '0' COMMENT '创建时间',
    `update_time`         int unsigned    NOT NULL DEFAULT '0' COMMENT '更新时间',
    PRIMARY KEY (`store_cycle_spec_id`),
    UNIQUE KEY `uk_shop_product_sku_period` (`shop_id`, `product_id`, `sku_id`, `period_num`),
    KEY `idx_shop_product` (`shop_id`, `product_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='门店周期购/次卡价格覆盖表（门店独立售价）';

ALTER TABLE `order` ADD COLUMN `is_valid` TINYINT(1) NOT NULL DEFAULT 1
  COMMENT '订单是否有效:1有效;0无效(支付后拆单父订单)' AFTER `is_store_splited`;



-- ============================================================
-- Antom（蚂蚁国际）支付配置 - 新增配置项
-- ============================================================
-- 接入文档：https://docs.antom.com/ac/aba/hosted_checkout
-- Antom 是蚂蚁集团旗下的国际支付产品，覆盖 Alipay+ 全球钱包网络
-- （含 AlipayHK、KakaoPay、Naver Pay、PayPay、GCash、TrueMoney、TouchNGo 等 25+ 钱包）
-- 以及主流国际信用卡。
-- 仅海外版生效，使用前需在 Antom Dashboard 申请 Client ID 与密钥。
-- ============================================================

-- 是否启用Antom支付（0-关闭，1-开启）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('useAntom', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Antom默认结算货币
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('antomCurrency', 'usd', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Antom 客户端 ID（Client ID）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('antomClientId', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Antom 商户私钥（RSA2048 PKCS8 Base64，用于签名）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('antomMerchantPrivateKey', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Antom 公钥（验签用）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('antomPublicKey', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Antom 网关地址（默认 SG/SEA 网关，欧洲：https://open-eu-global.alipay.com，北美：https://open-na.alipay.com）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('antomGatewayUrl', 'https://open-sea-global.alipay.com', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Antom 沙箱开关（0-生产，1-沙箱）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('antomSandbox', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Antom 支持的常用结算货币列表（前端下拉用，可在管理后台修改）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('antomCurrencyList',
        '[{"name":"美元","value":"usd"},{"name":"欧元","value":"eur"},{"name":"英镑","value":"gbp"},{"name":"港币","value":"hkd"},{"name":"新加坡元","value":"sgd"},{"name":"日元","value":"jpy"},{"name":"韩元","value":"krw"},{"name":"人民币","value":"cny"},{"name":"新台币","value":"twd"},{"name":"马来西亚林吉特","value":"myr"},{"name":"泰铢","value":"thb"},{"name":"菲律宾比索","value":"php"},{"name":"印尼盾","value":"idr"},{"name":"越南盾","value":"vnd"},{"name":"澳元","value":"aud"},{"name":"新西兰元","value":"nzd"},{"name":"加元","value":"cad"}]',
        UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- 翻译资源：Antom 错误提示
INSERT INTO `translations` (`id`, `translation_name`, `translation_key`, `data_type`, `shop_id`)
VALUES (null, '未配置Antom密钥或Client ID', 'antom.config.missing', 0, 0);

INSERT INTO `translations_data` (`id`, `locale_id`, `translation_name`, `translation_key`, `translation_value`, `data_type`, `data_id`)
VALUES (null, 1, '未配置Antom密钥或Client ID', 'antom.config.missing', '未配置Antom密钥或Client ID', 0, NULL);

INSERT INTO `translations_data` (`id`, `locale_id`, `translation_name`, `translation_key`, `translation_value`, `data_type`, `data_id`)
VALUES (null, 2, '未配置Antom密钥或Client ID', 'antom.config.missing', 'Antom merchant key or client ID is not configured', 0, NULL);

-- ============================================================
-- Xendit（东南亚聚合）支付配置 - 新增配置项
-- ============================================================
-- 接入文档：https://developers.xendit.co/api-reference/#create-invoice
-- Xendit 是东南亚老牌支付聚合，对印尼/菲律宾/马来/泰国/越南本地钱包
-- （OVO、DANA、ShopeePay、LinkAja、GCash、PayMaya、TouchNGo、TrueMoney、QRIS）支持最深。
-- 商户主体限制：仅 SG/ID/PH/MY/TH/VN 公司主体可注册，不接受中国大陆主体。
-- 国内主体客户请优先使用 Antom 通道。
-- 仅海外版生效，使用前需在 Xendit Dashboard 申请 Secret Key + Webhook Token。
-- ============================================================

-- 是否启用Xendit支付（0-关闭，1-开启）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('useXendit', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Xendit默认结算货币
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('xenditCurrency', 'idr', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Xendit Secret Key（服务端API调用）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('xenditSecretKey', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Xendit Public Key（前端嵌入用，可选）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('xenditPublicKey', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Xendit Webhook Verification Token
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('xenditWebhookToken', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Xendit API 基地址
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('xenditBaseUrl', 'https://api.xendit.co', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Xendit 沙箱标识（0-生产，1-沙箱；实际环境由 secret key 前缀决定）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('xenditSandbox', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Xendit 启用的支付方式（逗号分隔，留空显示Dashboard全部启用通道）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('xenditPaymentMethods', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Xendit 支付链接有效期（秒）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('xenditInvoiceDuration', '86400', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Xendit 支持的结算货币列表（前端下拉用，可在管理后台修改）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('xenditCurrencyList',
        '[{"name":"印尼盾","value":"idr"},{"name":"菲律宾比索","value":"php"},{"name":"马来西亚林吉特","value":"myr"},{"name":"泰铢","value":"thb"},{"name":"越南盾","value":"vnd"},{"name":"新加坡元","value":"sgd"},{"name":"美元","value":"usd"}]',
        UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- 翻译资源：Xendit 错误提示
INSERT INTO `translations` (`id`, `translation_name`, `translation_key`, `data_type`, `shop_id`)
VALUES (null, '未配置Xendit Secret Key', 'xendit.config.missing', 0, 0);

INSERT INTO `translations_data` (`id`, `locale_id`, `translation_name`, `translation_key`, `translation_value`, `data_type`, `data_id`)
VALUES (null, 1, '未配置Xendit Secret Key', 'xendit.config.missing', '未配置Xendit Secret Key', 0, NULL);

INSERT INTO `translations_data` (`id`, `locale_id`, `translation_name`, `translation_key`, `translation_value`, `data_type`, `data_id`)
VALUES (null, 2, '未配置Xendit Secret Key', 'xendit.config.missing', 'Xendit secret key is not configured', 0, NULL);
