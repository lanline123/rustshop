-- =============================================================
-- 5.8.50 -> 5.8.51 增量脚本
-- 内容：多币种汇率、免费抽奖、汇付/支付宝分账、快递100、跨境邮政编码、收银/客服权限与员工角色迁移
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- =============================================================

-- =============================================================
-- D1124 海外版多币种汇率自动更新
-- 来源：database/D1124-featureMultiCurrencyAutoRate.sql
-- =============================================================

-- =============================================================
-- 分支：D1124-featureMultiCurrencyAutoRate
-- D1124 海外版多币种汇率自动更新（Open Exchange Rates）增量脚本（人工执行）
-- 内容：
--   1. currency 表新增 code 列（国际通用代号，ISO 4217）
--   2. 存量常见币种 code 补齐（best-effort，按名称匹配，不覆盖已填值）
--   3. config 表新增汇率自动更新相关配置默认值（幂等）
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- 说明：仅海外版启用该功能，其余版本执行本脚本无副作用（列存在但不被使用）
-- =============================================================

SET NAMES utf8mb4;

-- =============================================================
-- 1. currency 表新增 code 列
-- 国际通用代号(ISO 4217)，作为 Open Exchange Rates 汇率源匹配依据。
-- 存量库首次升级执行；重复执行时通过 information_schema 自动跳过。
-- =============================================================
SET @d1124_currency_code_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'currency' AND COLUMN_NAME = 'code') = 0,
    'ALTER TABLE `currency` ADD COLUMN `code` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '''' COMMENT ''国际通用代号(ISO 4217，如 USD/EUR/CNY)，汇率源匹配依据'' AFTER `rate`',
    'SELECT 1'
);
PREPARE d1124_currency_code_stmt FROM @d1124_currency_code_ddl;
EXECUTE d1124_currency_code_stmt;
DEALLOCATE PREPARE d1124_currency_code_stmt;

-- =============================================================
-- 2. 存量常见币种 code 补齐（best-effort）
-- 按币种名称匹配，仅在 code 为空时写入，不覆盖运营已填值。
-- 名称与运营实际录入可能不一致，未命中的币种仍需在后台手工补齐 code。
-- =============================================================
UPDATE `currency` SET `code` = 'CNY' WHERE `code` = '' AND `name` IN ('人民币', '人民币元');
UPDATE `currency` SET `code` = 'USD' WHERE `code` = '' AND `name` IN ('美元', '美金');
UPDATE `currency` SET `code` = 'EUR' WHERE `code` = '' AND `name` = '欧元';
UPDATE `currency` SET `code` = 'GBP' WHERE `code` = '' AND `name` = '英镑';
UPDATE `currency` SET `code` = 'JPY' WHERE `code` = '' AND `name` = '日元';
UPDATE `currency` SET `code` = 'HKD' WHERE `code` = '' AND `name` = '港币';
UPDATE `currency` SET `code` = 'AUD' WHERE `code` = '' AND `name` IN ('澳大利亚元', '澳元');
UPDATE `currency` SET `code` = 'CAD' WHERE `code` = '' AND `name` IN ('加拿大元', '加元');
UPDATE `currency` SET `code` = 'SGD' WHERE `code` = '' AND `name` = '新加坡元';
UPDATE `currency` SET `code` = 'INR' WHERE `code` = '' AND `name` = '印度卢比';
UPDATE `currency` SET `code` = 'MMK' WHERE `code` = '' AND `name` IN ('缅币', '缅甸元', '缅元', '缅甸币');
UPDATE `currency` SET `code` = 'THB' WHERE `code` = '' AND `name` = '泰铢';
UPDATE `currency` SET `code` = 'MYR' WHERE `code` = '' AND `name` = '马来西亚林吉特';
UPDATE `currency` SET `code` = 'IDR' WHERE `code` = '' AND `name` = '印尼盾';
UPDATE `currency` SET `code` = 'PHP' WHERE `code` = '' AND `name` = '菲律宾比索';
UPDATE `currency` SET `code` = 'VND' WHERE `code` = '' AND `name` = '越南盾';
UPDATE `currency` SET `code` = 'KRW' WHERE `code` = '' AND `name` = '韩元';

-- =============================================================
-- 3. config 表新增汇率自动更新相关配置默认值（幂等）
-- exchangeRateAutoSync         自动更新总开关；0-手动维护(默认)，1-自动更新
-- exchangeRateSyncIntervalHours 更新周期（小时），默认 1（每小时 tick 上限，跨境免费版配额友好）
-- exchangeRateAppId / exchangeRateLastSyncTime 无默认值，按需运行时写入，此处不预置
-- 幂等：INSERT ... SELECT ... WHERE NOT EXISTS，重复执行不重复插入
-- =============================================================
INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'exchangeRateAutoSync', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'exchangeRateAutoSync' AND `is_del` = 0);

INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'exchangeRateSyncIntervalHours', '1', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'exchangeRateSyncIntervalHours' AND `is_del` = 0);

-- =============================================================
-- D1132 免费抽奖
-- 来源：database/D1132-freeLottery.sql
-- =============================================================

/*
 * =====================================================
 * D1132 抽奖活动 - 支持配置是否扣减积分(免费抽奖)
 * =====================================================
 * 功能说明: 抽奖活动「基础抽奖参与条件」新增「无限制(免费)」选项,
 *          与原有「每次抽奖消耗积分」二选一。
 *          配合每日最大抽奖次数,实现每天可免费抽奖 xxx 次。
 * 创建日期: 2026-07-16
 *
 * 变更内容:
 * 1. lottery_activity 新增 cost_type 字段:
 *    0-无限制(免费,不扣积分)  1-消耗积分(默认,兼容存量活动)
 *
 * 回滚方案:
 *   ALTER TABLE `lottery_activity` DROP COLUMN `cost_type`;
 * =====================================================
 */

SET @d1132_cost_type_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'lottery_activity' AND COLUMN_NAME = 'cost_type') = 0,
    'ALTER TABLE `lottery_activity` ADD COLUMN `cost_type` tinyint(1) unsigned NOT NULL DEFAULT 1 COMMENT ''抽奖参与条件:0-无限制(免费),1-消耗积分'' AFTER `end_time`',
    'SELECT 1'
);
PREPARE d1132_cost_type_stmt FROM @d1132_cost_type_ddl;
EXECUTE d1132_cost_type_stmt;
DEALLOCATE PREPARE d1132_cost_type_stmt;

-- =============================================================
-- D1133 汇付/支付宝分账
-- 来源：database/D1133huifuProfit.sql
-- =============================================================

-- =============================================================
-- D1133 汇付/支付宝分账统一增量脚本
-- 独立脚本，按需单独执行；不并入 demo/pure/o2o/overseas 六基线。
-- 说明：
--   分账 4 张表（profit_sharing_order / profit_sharing_return_order /
--   sub_merchant / merchant_application）为微信分账一期(D1091)已建，汇付复用，
--   本文件不重复建表，统一包含 D1133 汇付/支付宝分账相关配置、列、索引与修复 SQL。
--   汇付收单一期配置见 database/huifupay.sql；支付宝收单/进件复用现有配置。
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- 幂等：可重复执行（配置按存在性插入/去重；列与索引变更先判存在再 ALTER）。
-- =============================================================

SET NAMES utf8mb4;

-- -------------------------------------------------------------
-- 1. 汇付分账二期配置项（默认关闭；延时冻结默认关，上线灰度后开启）
-- -------------------------------------------------------------
INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'huifuProfitSharingEnabled', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'huifuProfitSharingEnabled');

INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'huifuDelayAcctEnabled', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'huifuDelayAcctEnabled');

INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'huifuProfitSharingNotifyUrl', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'huifuProfitSharingNotifyUrl');

INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'huifuProfitSharingMaxRatio', '20.00', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'huifuProfitSharingMaxRatio');

INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'huifuProfitSharingRuleOrigin', '01', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'huifuProfitSharingRuleOrigin');

-- -------------------------------------------------------------
-- 2. 线上分账渠道单选（CR-20260717-142300）：默认 wechat，与现状微信线上分账等价
-- -------------------------------------------------------------
INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'profitSharingOnlineChannel', 'wechat', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'profitSharingOnlineChannel');

-- 分账总开关（三渠道级联父开关，CR-20260721-151900）：默认 1 开启（零回归，仅显式关闭时三渠道分账全部不生效）
INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'profitSharingEnabled', '1', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'profitSharingEnabled');

-- -------------------------------------------------------------
-- 3. 子商户表增加最大分账比例（汇付进件开通后回写；可重复执行）
-- -------------------------------------------------------------
SET @ddl = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'sub_merchant'
            AND COLUMN_NAME = 'max_split_ratio') = 0,
        'ALTER TABLE `sub_merchant` ADD COLUMN `max_split_ratio` DECIMAL(8,2) NULL DEFAULT NULL COMMENT ''最大分账比例(%)'' AFTER `application_id`',
        'SELECT 1'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -------------------------------------------------------------
-- 4. 店铺提现增加线上取现渠道列（CR-20260717-150900）：默认 WECHAT，兼容存量；可重复执行
-- -------------------------------------------------------------
SET @ddl_sw = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'shop_withdraw'
            AND COLUMN_NAME = 'pay_channel') = 0,
        'ALTER TABLE `shop_withdraw` ADD COLUMN `pay_channel` VARCHAR(20) NULL DEFAULT ''WECHAT'' COMMENT ''线上提现渠道：WECHAT-微信，HUIFU-汇付，ALIPAY-支付宝'' AFTER `channel_status`',
        'SELECT 1'
    )
);
PREPARE stmt_sw FROM @ddl_sw;
EXECUTE stmt_sw;
DEALLOCATE PREPARE stmt_sw;

-- -------------------------------------------------------------
-- 5. 线上提现「线上收款方式」多选（三渠道共用一套提现配置 CR-20260722-114500；
--    默认仅微信，后端缺失/空时兜底 WECHAT，零回归。汇付/支付宝独立限额/频次配置已废弃）
-- -------------------------------------------------------------
INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'onlineWithdrawChannels', '["WECHAT"]', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'onlineWithdrawChannels');

-- -------------------------------------------------------------
-- 6. shop_withdraw 索引（三渠道独立体系 CR-20260721-144356）：提现单号唯一 + 店铺/渠道/状态复合；幂等
-- -------------------------------------------------------------
SET @ddl_uk = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.STATISTICS
          WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'shop_withdraw'
            AND INDEX_NAME = 'uk_withdraw_sn') = 0,
        'ALTER TABLE `shop_withdraw` ADD UNIQUE INDEX `uk_withdraw_sn` (`withdraw_sn`)',
        'SELECT 1'
    )
);
PREPARE stmt_uk FROM @ddl_uk;
EXECUTE stmt_uk;
DEALLOCATE PREPARE stmt_uk;

SET @ddl_idx = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.STATISTICS
          WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'shop_withdraw'
            AND INDEX_NAME = 'idx_shop_channel_status') = 0,
        'ALTER TABLE `shop_withdraw` ADD INDEX `idx_shop_channel_status` (`shop_id`, `pay_channel`, `status`)',
        'SELECT 1'
    )
);
PREPARE stmt_idx FROM @ddl_idx;
EXECUTE stmt_idx;
DEALLOCATE PREPARE stmt_idx;

-- -------------------------------------------------------------
-- 7. 修复线上提现配置重复数据
--    每个配置键保留 id 最大的有效记录，旧记录软删除；可重复执行。
-- -------------------------------------------------------------
UPDATE `config` AS `older`
INNER JOIN `config` AS `newer`
    ON `newer`.`biz_code` = `older`.`biz_code`
    AND `newer`.`is_del` = 0
    AND `newer`.`id` > `older`.`id`
SET `older`.`is_del` = 1,
    `older`.`update_time` = UNIX_TIMESTAMP()
WHERE `older`.`is_del` = 0
  AND `older`.`biz_code` IN (
    'onlineWithdrawEnabled',
    'onlineMinAmount',
    'onlineMaxAmount',
    'onlineWithdrawalFrequencyUnit',
    'onlineWithdrawalFrequencyCount',
    'onlineWithdrawalReviewMethod',
    'onlineWithdrawalDescription'
  );

-- -------------------------------------------------------------
-- 8. 补齐存量 profit_sharing_order 的供应商分账字段与索引
--    存量数据均为店铺分账单，因此 vendor_id 默认 0、settlement_type 默认 1；可重复执行。
-- -------------------------------------------------------------
SET @ddl_ps_vendor_id = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'profit_sharing_order'
            AND COLUMN_NAME = 'vendor_id') = 0,
        'ALTER TABLE `profit_sharing_order` ADD COLUMN `vendor_id` BIGINT NOT NULL DEFAULT 0 COMMENT ''供应商ID(0表示店铺分账单)'' AFTER `shop_id`',
        'SELECT 1'
    )
);
PREPARE stmt_ps_vendor_id FROM @ddl_ps_vendor_id;
EXECUTE stmt_ps_vendor_id;
DEALLOCATE PREPARE stmt_ps_vendor_id;

SET @ddl_ps_settlement_type = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'profit_sharing_order'
            AND COLUMN_NAME = 'settlement_type') = 0,
        'ALTER TABLE `profit_sharing_order` ADD COLUMN `settlement_type` TINYINT NOT NULL DEFAULT 1 COMMENT ''结算主体类型:1店铺 2供应商'' AFTER `vendor_id`',
        'SELECT 1'
    )
);
PREPARE stmt_ps_settlement_type FROM @ddl_ps_settlement_type;
EXECUTE stmt_ps_settlement_type;
DEALLOCATE PREPARE stmt_ps_settlement_type;

SET @ddl_ps_vendor_status_idx = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.STATISTICS
          WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'profit_sharing_order'
            AND INDEX_NAME = 'idx_vendor_id_status') = 0,
        'ALTER TABLE `profit_sharing_order` ADD INDEX `idx_vendor_id_status` (`vendor_id`, `status`)',
        'SELECT 1'
    )
);
PREPARE stmt_ps_vendor_status_idx FROM @ddl_ps_vendor_status_idx;
EXECUTE stmt_ps_vendor_status_idx;
DEALLOCATE PREPARE stmt_ps_vendor_status_idx;

SET @ddl_ps_settlement_type_idx = (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.STATISTICS
          WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'profit_sharing_order'
            AND INDEX_NAME = 'idx_settlement_type') = 0,
        'ALTER TABLE `profit_sharing_order` ADD INDEX `idx_settlement_type` (`settlement_type`)',
        'SELECT 1'
    )
);
PREPARE stmt_ps_settlement_type_idx FROM @ddl_ps_settlement_type_idx;
EXECUTE stmt_ps_settlement_type_idx;
DEALLOCATE PREPARE stmt_ps_settlement_type_idx;

-- -------------------------------------------------------------
-- 9. 支付尝试级线上分账渠道快照（expand DDL）
--    历史数据保持 NULL；不设置业务默认值、不回填、不新增索引。
-- -------------------------------------------------------------
SET @profit_sharing_channel_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'paylog'
      AND COLUMN_NAME = 'profit_sharing_channel'
);

SET @profit_sharing_channel_ddl = IF(
    @profit_sharing_channel_exists = 0,
    'ALTER TABLE `paylog` ADD COLUMN `profit_sharing_channel` varchar(16) NULL COMMENT ''支付时线上分账渠道快照：WECHAT/HUIFU/ALIPAY，NULL表示未冻结分账资金'' AFTER `pay_code`',
    'SELECT 1'
);

PREPARE profit_sharing_channel_stmt FROM @profit_sharing_channel_ddl;
EXECUTE profit_sharing_channel_stmt;
DEALLOCATE PREPARE profit_sharing_channel_stmt;

-- -------------------------------------------------------------
-- 10. 支付宝直付通分账配置
--     默认关闭；收单/进件复用现有支付宝配置，仅补充分账配置与服务商授权信息。
-- -------------------------------------------------------------
INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'alipayProfitSharingEnabled', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'alipayProfitSharingEnabled');

INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'alipayDelayAcctEnabled', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'alipayDelayAcctEnabled');

INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'alipayProfitSharingNotifyUrl', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'alipayProfitSharingNotifyUrl');

INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'alipayProfitSharingMaxRatio', '30.00', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'alipayProfitSharingMaxRatio');

INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'alipayProfitSharingRuleOrigin', '01', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'alipayProfitSharingRuleOrigin');

-- 平台分账收款账户(PID/UID)：直付通平台服务商收款主体，联调填实际值（不再复用 alipayAppid）
INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'alipayProfitSharingPlatformId', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'alipayProfitSharingPlatformId');

-- 服务商代调授权令牌(app_auth_token)：服务商代调二级商户接口时使用，联调填实际值
INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'alipayAppAuthToken', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'alipayAppAuthToken');

-- 支付宝独立线上提现限额/频次配置已废弃（CR-20260722-114500）：三渠道共用一套提现配置，
-- 启用渠道由通用配置 onlineWithdrawChannels（多选）控制。

-- =============================================================
-- D1153 快递100
-- 来源：database/D1153-kuaidi100.sql
-- =============================================================

-- D1153-kuaidi100：快递100 对接 + 物流服务商切换
-- 当前分支：D1153-kuaidi100
-- 规则：基线 SQL / *update.sql 不可改；增量写入本文件；DML 幂等；DDL 标注仅执行一次。

-- ============================================================
-- 一、DDL（幂等：按列存在性动态补齐，支持部分执行后重跑）
-- ============================================================
SET @d1153_company_adds = CONCAT_WS(', ',
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company' AND COLUMN_NAME = 'kuaidi100_code') = 0, 'ADD COLUMN `kuaidi100_code` varchar(30) NOT NULL DEFAULT '''' COMMENT ''快递100快递公司编码（小写，如 shunfeng/yuantong；留空则走内置映射）'' AFTER `api_logistics_code`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company' AND COLUMN_NAME = 'kuaidi100_partner_id') = 0, 'ADD COLUMN `kuaidi100_partner_id` varchar(100) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-电子面单账户/月结账号 partnerId'' AFTER `kuaidi100_code`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company' AND COLUMN_NAME = 'kuaidi100_partner_key') = 0, 'ADD COLUMN `kuaidi100_partner_key` varchar(200) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-电子面单密码 partnerKey'' AFTER `kuaidi100_partner_id`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company' AND COLUMN_NAME = 'kuaidi100_partner_secret') = 0, 'ADD COLUMN `kuaidi100_partner_secret` varchar(200) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-电子面单密钥 partnerSecret'' AFTER `kuaidi100_partner_key`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company' AND COLUMN_NAME = 'kuaidi100_net') = 0, 'ADD COLUMN `kuaidi100_net` varchar(120) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-收件网点名 net'' AFTER `kuaidi100_partner_secret`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company' AND COLUMN_NAME = 'kuaidi100_check_man') = 0, 'ADD COLUMN `kuaidi100_check_man` varchar(60) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-承载快递员名 checkMan'' AFTER `kuaidi100_net`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company' AND COLUMN_NAME = 'kuaidi100_exp_type') = 0, 'ADD COLUMN `kuaidi100_exp_type` varchar(40) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-产品类型 expType'' AFTER `kuaidi100_check_man`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company' AND COLUMN_NAME = 'kuaidi100_temp_id') = 0, 'ADD COLUMN `kuaidi100_temp_id` varchar(60) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-主单模板ID tempId（IMAGE/HTML/CLOUD 网点面单必填）'' AFTER `kuaidi100_exp_type`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company' AND COLUMN_NAME = 'kuaidi100_child_temp_id') = 0, 'ADD COLUMN `kuaidi100_child_temp_id` varchar(60) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-子单模板ID childTempId'' AFTER `kuaidi100_temp_id`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company' AND COLUMN_NAME = 'kuaidi100_back_temp_id') = 0, 'ADD COLUMN `kuaidi100_back_temp_id` varchar(60) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-回单模板ID backTempId'' AFTER `kuaidi100_child_temp_id`', NULL)
);
SET @d1153_company_ddl = IF(@d1153_company_adds = '', 'SELECT 1', CONCAT('ALTER TABLE `logistics_company` ', @d1153_company_adds));
PREPARE d1153_company_stmt FROM @d1153_company_ddl;
EXECUTE d1153_company_stmt;
DEALLOCATE PREPARE d1153_company_stmt;

SET @d1153_config_adds = CONCAT_WS(', ',
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company_config' AND COLUMN_NAME = 'kuaidi100_partner_id') = 0, 'ADD COLUMN `kuaidi100_partner_id` varchar(100) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-电子面单账户/月结账号 partnerId'' AFTER `exp_type`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company_config' AND COLUMN_NAME = 'kuaidi100_partner_key') = 0, 'ADD COLUMN `kuaidi100_partner_key` varchar(200) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-电子面单密码 partnerKey'' AFTER `kuaidi100_partner_id`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company_config' AND COLUMN_NAME = 'kuaidi100_partner_secret') = 0, 'ADD COLUMN `kuaidi100_partner_secret` varchar(200) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-电子面单密钥 partnerSecret'' AFTER `kuaidi100_partner_key`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company_config' AND COLUMN_NAME = 'kuaidi100_net') = 0, 'ADD COLUMN `kuaidi100_net` varchar(120) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-收件网点名 net'' AFTER `kuaidi100_partner_secret`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company_config' AND COLUMN_NAME = 'kuaidi100_check_man') = 0, 'ADD COLUMN `kuaidi100_check_man` varchar(60) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-承载快递员名 checkMan'' AFTER `kuaidi100_net`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company_config' AND COLUMN_NAME = 'kuaidi100_exp_type') = 0, 'ADD COLUMN `kuaidi100_exp_type` varchar(40) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-产品类型 expType'' AFTER `kuaidi100_check_man`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company_config' AND COLUMN_NAME = 'kuaidi100_temp_id') = 0, 'ADD COLUMN `kuaidi100_temp_id` varchar(60) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-主单模板ID tempId（IMAGE/HTML/CLOUD 网点面单必填）'' AFTER `kuaidi100_exp_type`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company_config' AND COLUMN_NAME = 'kuaidi100_child_temp_id') = 0, 'ADD COLUMN `kuaidi100_child_temp_id` varchar(60) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-子单模板ID childTempId'' AFTER `kuaidi100_temp_id`', NULL),
    IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'logistics_company_config' AND COLUMN_NAME = 'kuaidi100_back_temp_id') = 0, 'ADD COLUMN `kuaidi100_back_temp_id` varchar(60) NOT NULL DEFAULT '''' COMMENT ''快递100电子面单-回单模板ID backTempId'' AFTER `kuaidi100_child_temp_id`', NULL)
);
SET @d1153_config_ddl = IF(@d1153_config_adds = '', 'SELECT 1', CONCAT('ALTER TABLE `logistics_company_config` ', @d1153_config_adds));
PREPARE d1153_config_stmt FROM @d1153_config_ddl;
EXECUTE d1153_config_stmt;
DEALLOCATE PREPARE d1153_config_stmt;

-- ============================================================
-- 二、DML（幂等：物流服务商切换 + 快递100 凭证配置项）
-- ============================================================

-- 物流服务商；1-快递鸟，2-快递100（默认快递鸟，兼容存量）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'logisticsProvider', '1', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'logisticsProvider' AND t.`is_del` = 0
);

-- 快递100 授权码 customer
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'kuaidi100Customer', '', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'kuaidi100Customer' AND t.`is_del` = 0
);

-- 快递100 授权 key
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'kuaidi100Key', '', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'kuaidi100Key' AND t.`is_del` = 0
);

-- 快递100 授权 secret（电子面单签名）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'kuaidi100Secret', '', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'kuaidi100Secret' AND t.`is_del` = 0
);

-- 快递100 云打印机设备码（printType=CLOUD 时必填）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'kuaidi100Siid', '', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'kuaidi100Siid' AND t.`is_del` = 0
);

-- 快递100 电子面单打印类型；默认 HTML（对齐快递鸟 PrintTemplate）
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'kuaidi100PrintType', 'HTML', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'kuaidi100PrintType' AND t.`is_del` = 0
);

-- ============================================================
-- 三、物流服务商快照与历史订单回填（幂等）
-- 发布回滚只回滚应用并保留新增列，禁止在本脚本中删除列。
-- ============================================================
SET @order_provider_column_exists = (
    SELECT COUNT(*)
    FROM `information_schema`.`columns`
    WHERE `table_schema` = DATABASE()
      AND `table_name` = 'order'
      AND `column_name` = 'logistics_provider'
);
SET @order_provider_ddl = IF(
    @order_provider_column_exists = 0,
    'ALTER TABLE `order` ADD COLUMN `logistics_provider` tinyint unsigned NOT NULL DEFAULT 0 COMMENT ''物流服务商：0历史未知或不适用，1快递鸟，2快递100'' AFTER `logistics_id`',
    'SELECT 1'
);
PREPARE order_provider_stmt FROM @order_provider_ddl;
EXECUTE order_provider_stmt;
DEALLOCATE PREPARE order_provider_stmt;

SET @log_provider_column_exists = (
    SELECT COUNT(*)
    FROM `information_schema`.`columns`
    WHERE `table_schema` = DATABASE()
      AND `table_name` = 'logistics_api_log'
      AND `column_name` = 'provider_type'
);
SET @log_provider_ddl = IF(
    @log_provider_column_exists = 0,
    'ALTER TABLE `logistics_api_log` ADD COLUMN `provider_type` tinyint unsigned NOT NULL DEFAULT 0 COMMENT ''实际创建面单的物流服务商：0未知，1快递鸟，2快递100'' AFTER `order_id`',
    'SELECT 1'
);
PREPARE log_provider_stmt FROM @log_provider_ddl;
EXECUTE log_provider_stmt;
DEALLOCATE PREPARE log_provider_stmt;

-- 回填前分布与无法确定的历史订单数。
SELECT COUNT(*) AS `total_count`,
       SUM(`o`.`logistics_provider` = 0) AS `provider_0_count`,
       SUM(`o`.`logistics_provider` = 1) AS `provider_1_count`,
       SUM(`o`.`logistics_provider` = 2) AS `provider_2_count`,
       SUM(
           `o`.`logistics_provider` = 0
           AND NOT (
               (TRIM(COALESCE(`lc`.`logistics_code`, '')) <> '' AND TRIM(COALESCE(`lc`.`kuaidi100_code`, '')) = '')
               OR (TRIM(COALESCE(`lc`.`logistics_code`, '')) = '' AND TRIM(COALESCE(`lc`.`kuaidi100_code`, '')) <> '')
           )
       ) AS `unresolved_count`
FROM `order` AS `o`
LEFT JOIN `logistics_company` AS `lc` ON `lc`.`logistics_id` = `o`.`logistics_id`;

-- 只回填服务商仍未知且公司编码可唯一确定的订单；双有、双空和公司缺失均保持 0。
UPDATE `order` AS `o`
INNER JOIN `logistics_company` AS `lc` ON `lc`.`logistics_id` = `o`.`logistics_id`
SET `o`.`logistics_provider` = CASE
    WHEN TRIM(COALESCE(`lc`.`logistics_code`, '')) <> ''
         AND TRIM(COALESCE(`lc`.`kuaidi100_code`, '')) = '' THEN 1
    WHEN TRIM(COALESCE(`lc`.`logistics_code`, '')) = ''
         AND TRIM(COALESCE(`lc`.`kuaidi100_code`, '')) <> '' THEN 2
    ELSE 0
END
WHERE `o`.`logistics_provider` = 0
  AND (
      (TRIM(COALESCE(`lc`.`logistics_code`, '')) <> '' AND TRIM(COALESCE(`lc`.`kuaidi100_code`, '')) = '')
      OR (TRIM(COALESCE(`lc`.`logistics_code`, '')) = '' AND TRIM(COALESCE(`lc`.`kuaidi100_code`, '')) <> '')
  );

-- 回填后分布与仍无法确定的历史订单数；重复执行时上方 UPDATE 影响行数应为 0。
SELECT COUNT(*) AS `total_count`,
       SUM(`o`.`logistics_provider` = 0) AS `provider_0_count`,
       SUM(`o`.`logistics_provider` = 1) AS `provider_1_count`,
       SUM(`o`.`logistics_provider` = 2) AS `provider_2_count`,
       SUM(`o`.`logistics_provider` = 0) AS `unresolved_count`
FROM `order` AS `o`;

-- =============================================================
-- D1159 跨境邮政编码
-- 来源：database/D1159-overseasPostalCode.sql
-- =============================================================

INSERT INTO `translations` (`id`, `translation_name`, `translation_key`, `data_type`, `shop_id`)
SELECT NULL, '邮政编码', 'b4a39e7383aaede109e395f84a43d885', 0, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `translations` WHERE `translation_key` = 'b4a39e7383aaede109e395f84a43d885' AND `data_type` = 0 AND `shop_id` = 0);
INSERT INTO `translations` (`id`, `translation_name`, `translation_key`, `data_type`, `shop_id`)
SELECT NULL, '请您填写邮政编码', '0b9240018bccde4d8a0209c4e694aca8', 0, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `translations` WHERE `translation_key` = '0b9240018bccde4d8a0209c4e694aca8' AND `data_type` = 0 AND `shop_id` = 0);

INSERT INTO `translations_data` (`id`, `locale_id`, `translation_name`, `translation_key`, `translation_value`, `data_type`, `data_id`)
SELECT NULL, 1, '邮政编码', 'b4a39e7383aaede109e395f84a43d885', '邮政编码', 0, NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `translations_data` WHERE `locale_id` = 1 AND `translation_key` = 'b4a39e7383aaede109e395f84a43d885' AND `data_type` = 0);
INSERT INTO `translations_data` (`id`, `locale_id`, `translation_name`, `translation_key`, `translation_value`, `data_type`, `data_id`)
SELECT NULL, 2, '邮政编码', 'b4a39e7383aaede109e395f84a43d885', 'Postal code', 0, NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `translations_data` WHERE `locale_id` = 2 AND `translation_key` = 'b4a39e7383aaede109e395f84a43d885' AND `data_type` = 0);
INSERT INTO `translations_data` (`id`, `locale_id`, `translation_name`, `translation_key`, `translation_value`, `data_type`, `data_id`)
SELECT NULL, 1, '请您填写邮政编码', '0b9240018bccde4d8a0209c4e694aca8', '请您填写邮政编码', 0, NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `translations_data` WHERE `locale_id` = 1 AND `translation_key` = '0b9240018bccde4d8a0209c4e694aca8' AND `data_type` = 0);
INSERT INTO `translations_data` (`id`, `locale_id`, `translation_name`, `translation_key`, `translation_value`, `data_type`, `data_id`)
SELECT NULL, 2, '请您填写邮政编码', '0b9240018bccde4d8a0209c4e694aca8', 'Please fill in the zip code.', 0, NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `translations_data` WHERE `locale_id` = 2 AND `translation_key` = '0b9240018bccde4d8a0209c4e694aca8' AND `data_type` = 0);

-- =============================================================
-- D1123 收银/客服权限与员工角色迁移
-- 来源：database/D1123-cashier-admin-refactor.sql
-- =============================================================

-- ============================================================
-- 1. 幂等 schema 准备（MySQL 5.7 / 8.x）
-- ============================================================
SET @d1123_has_role_code = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'admin_role'
      AND COLUMN_NAME = 'role_code'
);

SET @d1123_ddl = IF(
    @d1123_has_role_code = 0,
    'ALTER TABLE `admin_role` ADD COLUMN `role_code` varchar(50) NULL DEFAULT NULL COMMENT ''系统角色稳定标识(如 pos_cashier)'' AFTER `role_name`',
    'SELECT 1'
);
PREPARE d1123_ddl_stmt FROM @d1123_ddl;
EXECUTE d1123_ddl_stmt;
DEALLOCATE PREPARE d1123_ddl_stmt;

SET @d1123_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'admin_role'
      AND INDEX_NAME = 'uniq_role_code_shop_type'
);
SET @d1123_ddl = IF(
    @d1123_index_exists = 0,
    'ALTER TABLE `admin_role` ADD UNIQUE INDEX `uniq_role_code_shop_type` (`role_code`, `shop_id`, `admin_type`(20))',
    'SELECT 1'
);
PREPARE d1123_ddl_stmt FROM @d1123_ddl;
EXECUTE d1123_ddl_stmt;
DEALLOCATE PREPARE d1123_ddl_stmt;

SET @d1123_col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'admin_user_shop' AND COLUMN_NAME = 'work_on_time'
);
SET @d1123_ddl = IF(
    @d1123_col_exists = 0,
    'ALTER TABLE `admin_user_shop` ADD COLUMN `work_on_time` bigint(20) NULL DEFAULT NULL COMMENT ''收银上班/交班起始时间''',
    'SELECT 1'
);
PREPARE d1123_ddl_stmt FROM @d1123_ddl;
EXECUTE d1123_ddl_stmt;
DEALLOCATE PREPARE d1123_ddl_stmt;

SET @d1123_col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'admin_user_shop' AND COLUMN_NAME = 'default_pos_login'
);
SET @d1123_ddl = IF(
    @d1123_col_exists = 0,
    'ALTER TABLE `admin_user_shop` ADD COLUMN `default_pos_login` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''登录后是否默认进收银台 1是0否''',
    'SELECT 1'
);
PREPARE d1123_ddl_stmt FROM @d1123_ddl;
EXECUTE d1123_ddl_stmt;
DEALLOCATE PREPARE d1123_ddl_stmt;

SET @d1123_col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'admin_user_shop' AND COLUMN_NAME = 'last_login_system'
);
SET @d1123_ddl = IF(
    @d1123_col_exists = 0,
    'ALTER TABLE `admin_user_shop` ADD COLUMN `last_login_system` varchar(20) NOT NULL DEFAULT '''' COMMENT ''上次登录端(admin/shop/store/pos)''',
    'SELECT 1'
);
PREPARE d1123_ddl_stmt FROM @d1123_ddl;
EXECUTE d1123_ddl_stmt;
DEALLOCATE PREPARE d1123_ddl_stmt;

-- 收银菜单已统一迁入 groupPos：旧入口仅隐藏，不删除权限行，保留存量角色与接口权限兼容。
UPDATE `authority`
SET `is_show` = 0
WHERE (`authority_sn` = 'baseReceiptManage' AND `admin_type` = 'admin')
   OR (`authority_sn` = 'shopBaseReceiptManage' AND `admin_type` IN ('shop', 'store'))
   OR (`authority_sn` = 'cashierList' AND `admin_type` = 'admin')
   OR (`authority_sn` = 'shopCashierList' AND `admin_type` = 'store');

INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'groupPos','收银',0,30,1,'[]','','iconfont-tig icon-zijinguanli1',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='groupPos' AND `admin_type`='shop' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posWorkbench','收银工作台',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupPos' AND `admin_type`='shop' LIMIT 1) p),50,1,'[]','','',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posWorkbench' AND `admin_type`='shop' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posSetting','收银设置',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupPos' AND `admin_type`='shop' LIMIT 1) p),52,1,'[]','','',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posSetting' AND `admin_type`='shop' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posCashierManage','收银员管理',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupPos' AND `admin_type`='shop' LIMIT 1) p),53,1,'[]','','iconfont-tig icon-guanliyuan',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posCashierManage' AND `admin_type`='shop' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posOrderSetting','收银设置',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='posSetting' AND `admin_type`='shop' LIMIT 1) p),50,1,'[]','','',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posOrderSetting' AND `admin_type`='shop' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posReceiptPrint','小票打印',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='posSetting' AND `admin_type`='shop' LIMIT 1) p),51,1,'[]','','',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posReceiptPrint' AND `admin_type`='shop' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posCashierList','收银员列表',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='posCashierManage' AND `admin_type`='shop' LIMIT 1) p),50,1,'[{"authName":"编辑","authSn":"adminUserPosModifyManage"}]','','',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posCashierList' AND `admin_type`='shop' LIMIT 1) e);

INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'groupPos','收银',0,30,1,'[]','','iconfont-tig icon-shouyingongzuotai',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='groupPos' AND `admin_type`='admin' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posWorkbench','收银工作台',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupPos' AND `admin_type`='admin' LIMIT 1) p),50,1,'[]','','iconfont-tig icon-shouyingongzuotai',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posWorkbench' AND `admin_type`='admin' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posSetting','收银设置',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupPos' AND `admin_type`='admin' LIMIT 1) p),52,1,'[]','','iconfont-tig icon-shouyinshezhi',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posSetting' AND `admin_type`='admin' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posCashierManage','收银员管理',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupPos' AND `admin_type`='admin' LIMIT 1) p),53,1,'[]','','iconfont-tig icon-shouyinyuanguanli',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posCashierManage' AND `admin_type`='admin' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posOrderSetting','收银设置',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='posSetting' AND `admin_type`='admin' LIMIT 1) p),50,1,'[]','','',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posOrderSetting' AND `admin_type`='admin' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posReceiptPrint','小票打印',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='posSetting' AND `admin_type`='admin' LIMIT 1) p),51,1,'[]','','',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posReceiptPrint' AND `admin_type`='admin' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posCashierList','收银员列表',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='posCashierManage' AND `admin_type`='admin' LIMIT 1) p),50,1,'[{"authName":"编辑","authSn":"adminUserPosModifyManage"}]','','',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posCashierList' AND `admin_type`='admin' LIMIT 1) e);

INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'groupCustomerService','客服',0,29,1,'[]','','iconfont-tig icon-kefugongzuotai1',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='admin' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuWorkbench','客服工作台',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='admin' LIMIT 1) p),50,1,'[]','','iconfont-tig icon-kefugongzuotai1',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuWorkbench' AND `admin_type`='admin' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuHistory','历史会话',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='admin' LIMIT 1) p),51,1,'[]','','iconfont-tig icon-lishihuihua1',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuHistory' AND `admin_type`='admin' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuPersonal','个人设置',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='admin' LIMIT 1) p),52,1,'[]','','iconfont-tig icon-gerenshezhi',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuPersonal' AND `admin_type`='admin' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuSystemSetting','系统设置',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='admin' LIMIT 1) p),53,1,'[]','','iconfont-tig icon-xitongshezhi1',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuSystemSetting' AND `admin_type`='admin' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuHistoryList','历史会话列表',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuHistory' AND `admin_type`='admin' LIMIT 1) p),50,1,'[]','','',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuHistoryList' AND `admin_type`='admin' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuQuickReply','快捷回复',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuPersonal' AND `admin_type`='admin' LIMIT 1) p),50,1,'[]','','',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuQuickReply' AND `admin_type`='admin' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuAccountInfo','账户信息',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuPersonal' AND `admin_type`='admin' LIMIT 1) p),51,1,'[]','','',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuAccountInfo' AND `admin_type`='admin' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuSetting','客服设置',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuSystemSetting' AND `admin_type`='admin' LIMIT 1) p),50,1,'[{"authName":"编辑","authSn":"saveSettingKefuManage"}]','','',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuSetting' AND `admin_type`='admin' LIMIT 1) e);

INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'groupCustomerService','客服',0,29,1,'[]','','iconfont-tig icon-xiaoxishezhi',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='shop' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuWorkbench','客服工作台',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='shop' LIMIT 1) p),50,1,'[]','','',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuWorkbench' AND `admin_type`='shop' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuHistory','历史会话',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='shop' LIMIT 1) p),51,1,'[]','','',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuHistory' AND `admin_type`='shop' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuPersonal','个人设置',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='shop' LIMIT 1) p),52,1,'[]','','',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuPersonal' AND `admin_type`='shop' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuSystemSetting','系统设置',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='shop' LIMIT 1) p),53,1,'[]','','',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuSystemSetting' AND `admin_type`='shop' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuHistoryList','历史会话列表',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuHistory' AND `admin_type`='shop' LIMIT 1) p),50,1,'[]','','',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuHistoryList' AND `admin_type`='shop' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuQuickReply','快捷回复',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuPersonal' AND `admin_type`='shop' LIMIT 1) p),50,1,'[]','','',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuQuickReply' AND `admin_type`='shop' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuAccountInfo','账户信息',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuPersonal' AND `admin_type`='shop' LIMIT 1) p),51,1,'[]','','',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuAccountInfo' AND `admin_type`='shop' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuSetting','客服设置',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuSystemSetting' AND `admin_type`='shop' LIMIT 1) p),50,1,'[{"authName":"编辑","authSn":"saveSettingKefuManage"}]','','',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuSetting' AND `admin_type`='shop' LIMIT 1) e);

-- 旧菜单只按稳定业务键隐藏，不改写为新菜单、不删除数据行。
UPDATE `authority`
SET `is_show` = 0
WHERE `authority_sn` = 'baseServiceManage'
  AND `admin_type` = 'admin';

-- 保留已确认的收银菜单稳定键迁移：三端均已创建 posCashierList 种子。
UPDATE `admin_role`
SET `authority_list` = REPLACE(`authority_list`, 'shopCashierList', 'posCashierList')
WHERE `authority_list` LIKE '%shopCashierList%';

UPDATE `admin_user`
SET `auth_list` = REPLACE(`auth_list`, 'shopCashierList', 'posCashierList')
WHERE `auth_list` LIKE '%shopCashierList%';

UPDATE `admin_user_shop`
SET `auth_list` = REPLACE(`auth_list`, 'shopCashierList', 'posCashierList')
WHERE `auth_list` LIKE '%shopCashierList%';

UPDATE `admin_user_vendor`
SET `auth_list` = REPLACE(`auth_list`, 'shopCashierList', 'posCashierList')
WHERE `auth_list` LIKE '%shopCashierList%';

UPDATE `admin_user_pos`
SET `auth_list` = REPLACE(`auth_list`, 'shopCashierList', 'posCashierList')
WHERE `auth_list` LIKE '%shopCashierList%';

-- ============================================================
-- 2. 每店系统角色矩阵（稳定业务键幂等）
-- ============================================================
INSERT INTO `admin_role`
(`role_name`, `role_code`, `role_desc`, `authority_list`, `admin_type`, `shop_id`, `is_system`)
SELECT '客服', 'kefu_agent', '系统内置客服角色',
       '["groupCustomerService","kefuWorkbench","kefuHistory","kefuHistoryList","kefuPersonal","kefuQuickReply","kefuAccountInfo"]',
       'shop', target.`shop_id`, 1
FROM (
    SELECT 0 AS `shop_id`
    UNION
    SELECT s.`shop_id`
    FROM `shop` s
    WHERE s.`shop_type` IN (1, 2)
) target
WHERE NOT EXISTS (
    SELECT 1
    FROM `admin_role` r
    WHERE r.`role_code` = 'kefu_agent'
      AND r.`shop_id` = target.`shop_id`
      AND r.`admin_type` = 'shop'
);

INSERT INTO `admin_role`
(`role_name`, `role_code`, `role_desc`, `authority_list`, `admin_type`, `shop_id`, `is_system`)
SELECT '收银员', 'pos_cashier', '系统内置收银员角色',
       '["posCheckout","posOrder","posReservation","posVerification","posReturn","posShift","posSetting","posVip","groupPos","posWorkbench","posOrderSetting"]',
       'shop', target.`shop_id`, 1
FROM (
    SELECT 0 AS `shop_id`
    UNION
    SELECT s.`shop_id`
    FROM `shop` s
    WHERE s.`shop_type` = 2
) target
WHERE NOT EXISTS (
    SELECT 1
    FROM `admin_role` r
    WHERE r.`role_code` = 'pos_cashier'
      AND r.`shop_id` = target.`shop_id`
      AND r.`admin_type` = 'shop'
);

-- 校正已存在的系统角色：收银员精确 11 项，客服精确 7 项（含菜单结构根节点）。
-- role_code + is_system=1 同时作为运行时内部识别口径：普通角色管理和普通员工角色下拉按
-- role_code 隐藏这两类内置角色，客服/收银专用入口仍可按稳定键内部绑定；无需新增隐藏字段。
-- pos_cashier 明确不含 posReceiptPrint / posCashierManage / posCashierList。
UPDATE `admin_role` r
SET r.`role_name` = CASE r.`role_code`
        WHEN 'pos_cashier' THEN '收银员'
        WHEN 'kefu_agent' THEN '客服'
    END,
    r.`role_desc` = CASE r.`role_code`
        WHEN 'pos_cashier' THEN '系统内置收银员角色'
        WHEN 'kefu_agent' THEN '系统内置客服角色'
    END,
    r.`authority_list` = CASE r.`role_code`
        WHEN 'pos_cashier' THEN '["posCheckout","posOrder","posReservation","posVerification","posReturn","posShift","posSetting","posVip","groupPos","posWorkbench","posOrderSetting"]'
        WHEN 'kefu_agent' THEN '["groupCustomerService","kefuWorkbench","kefuHistory","kefuHistoryList","kefuPersonal","kefuQuickReply","kefuAccountInfo"]'
    END,
    r.`is_system` = 1
WHERE r.`role_code` IN ('pos_cashier', 'kefu_agent')
  AND r.`admin_type` = 'shop';

-- 本脚本只补齐 Schema、菜单和系统角色模板；
-- 不迁移存量 admin_user_pos / im_servant / admin_user 数据到 admin_user_shop。

INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'groupPos','收银',0,30,1,'[]','','iconfont-tig icon-shouyin',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='groupPos' AND `admin_type`='store' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posWorkbench','收银工作台',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupPos' AND `admin_type`='store' LIMIT 1) p),50,1,'[]','','iconfont-tig icon-shouyin',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posWorkbench' AND `admin_type`='store' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posSetting','收银设置',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupPos' AND `admin_type`='store' LIMIT 1) p),52,1,'[]','','iconfont-tig icon-31shezhi',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posSetting' AND `admin_type`='store' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posCashierManage','收银员管理',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupPos' AND `admin_type`='store' LIMIT 1) p),53,1,'[]','','iconfont-tig icon-shouyinyuan',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posCashierManage' AND `admin_type`='store' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posOrderSetting','收银设置',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='posSetting' AND `admin_type`='store' LIMIT 1) p),50,1,'[]','','',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posOrderSetting' AND `admin_type`='store' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posReceiptPrint','小票打印',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='posSetting' AND `admin_type`='store' LIMIT 1) p),51,1,'[]','','',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posReceiptPrint' AND `admin_type`='store' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'posCashierList','收银员列表',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='posCashierManage' AND `admin_type`='store' LIMIT 1) p),50,1,'[{"authName":"编辑","authSn":"adminUserPosModifyManage"}]','','',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='posCashierList' AND `admin_type`='store' LIMIT 1) e);

INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'groupCustomerService','客服',0,29,1,'[]','','iconfont-tig icon-kefu3',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='store' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuWorkbench','客服工作台',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='store' LIMIT 1) p),50,1,'[]','','iconfont-tig icon-kefu3',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuWorkbench' AND `admin_type`='store' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuHistory','历史会话',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='store' LIMIT 1) p),51,1,'[]','','iconfont-tig icon-lishihuihua',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuHistory' AND `admin_type`='store' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuPersonal','个人设置',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='store' LIMIT 1) p),52,1,'[]','','iconfont-tig icon-guanlizhanghao',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuPersonal' AND `admin_type`='store' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuSystemSetting','系统设置',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='store' LIMIT 1) p),53,1,'[]','','iconfont-tig icon-31shezhi',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuSystemSetting' AND `admin_type`='store' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuHistoryList','历史会话列表',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuHistory' AND `admin_type`='store' LIMIT 1) p),50,1,'[]','','',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuHistoryList' AND `admin_type`='store' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuQuickReply','快捷回复',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuPersonal' AND `admin_type`='store' LIMIT 1) p),50,1,'[]','','',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuQuickReply' AND `admin_type`='store' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuAccountInfo','账户信息',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuPersonal' AND `admin_type`='store' LIMIT 1) p),51,1,'[]','','',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuAccountInfo' AND `admin_type`='store' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuSetting','客服设置',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuSystemSetting' AND `admin_type`='store' LIMIT 1) p),50,1,'[{"authName":"编辑","authSn":"saveSettingKefuManage"}]','','',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuSetting' AND `admin_type`='store' LIMIT 1) e);

-- ============================================================
-- 客服人员管理（客服列表）+ 接入规则
-- ============================================================

-- ----------------------------
-- 接入规则配置表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `shop_kefu_config`
(
    `id`                     int(11)    NOT NULL AUTO_INCREMENT,
    `shop_id`                int(11)    NOT NULL DEFAULT 0 COMMENT '店铺ID',
    `auto_access_mode`       tinyint(1) NOT NULL DEFAULT 0 COMMENT '接入方式:0全部自动接入 1全部手动接入',
    `return_customer_access` tinyint(1) NOT NULL DEFAULT 0 COMMENT '回头客接入:0关闭 1优先接入上次客服',
    `create_time`            int(11)    NULL DEFAULT NULL COMMENT '创建时间',
    `modify_time`            int(11)    NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uniq_shop_id` (`shop_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '店铺客服接入配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 菜单权限（admin / shop / store 三端）
--   客服人员(kefuManage) > 客服列表(kefuManageList，带编辑子权限 kefuManageModify)
--   接入规则(kefuAccessSetting) 挂在 系统设置(kefuSystemSetting) 下
-- ----------------------------

-- ===== admin 端 =====
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuManage','客服管理',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='admin' LIMIT 1) p),52,1,'[]','','iconfont-tig icon-kefuguanli',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuManage' AND `admin_type`='admin' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuManageList','客服列表',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuManage' AND `admin_type`='admin' LIMIT 1) p),50,1,'[{"authName":"编辑","authSn":"kefuManageModify"}]','','',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuManageList' AND `admin_type`='admin' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuAccessSetting','接入规则',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuSystemSetting' AND `admin_type`='admin' LIMIT 1) p),51,1,'[]','','',0,'admin' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuAccessSetting' AND `admin_type`='admin' LIMIT 1) e);

-- ===== shop 端 =====
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuManage','客服人员',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='shop' LIMIT 1) p),52,1,'[]','','',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuManage' AND `admin_type`='shop' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuManageList','客服列表',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuManage' AND `admin_type`='shop' LIMIT 1) p),50,1,'[{"authName":"编辑","authSn":"kefuManageModify"}]','','',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuManageList' AND `admin_type`='shop' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuAccessSetting','接入规则',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuSystemSetting' AND `admin_type`='shop' LIMIT 1) p),51,1,'[]','','',0,'shop' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuAccessSetting' AND `admin_type`='shop' LIMIT 1) e);

-- ===== store 端 =====
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuManage','客服管理',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='groupCustomerService' AND `admin_type`='store' LIMIT 1) p),52,1,'[]','','iconfont-tig icon-xiaoxishezhi',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuManage' AND `admin_type`='store' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuManageList','客服列表',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuManage' AND `admin_type`='store' LIMIT 1) p),50,1,'[{"authName":"编辑","authSn":"kefuManageModify"}]','','',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuManageList' AND `admin_type`='store' LIMIT 1) e);
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'kefuAccessSetting','接入规则',(SELECT id FROM (SELECT `authority_id` id FROM `authority` WHERE `authority_sn`='kefuSystemSetting' AND `admin_type`='store' LIMIT 1) p),51,1,'[]','','',0,'store' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM `authority` WHERE `authority_sn`='kefuAccessSetting' AND `admin_type`='store' LIMIT 1) e);

-- ----------------------------
-- kefu_agent 角色：本脚本补齐系统角色模板矩阵；运行时对后续新建店铺/门店继续按稳定键 get-or-create
-- ----------------------------

-- admin_user_shop 在业务上仍以 admin_id + shop_id 唯一，服务层也按该组合查询。
-- 但客户历史库可能存在无法自动裁决的重复绑定，通用升级脚本不强制新增唯一索引，
-- 避免 MySQL DDL 自动提交后因重复数据失败；如需索引加固，应先由 DBA 单独治理存量重复绑定。

-- =============================================================================
-- D1157-bugfix 增量脚本
-- 目的：补齐 locales_lang 语言码字典，供后续数据库 dump 生成版本基线 SQL。
-- 数据源：database/2.3.2.3-5.0.0.0update.sql 中既有 129 条标准语言码。
-- 幂等：按 code 判断；已存在的语言码不更新、不重复插入，仅补缺失项。
-- 说明：六份基线 SQL 不在本分支手工修改，发版执行本脚本后由数据库 dump 更新。
-- =============================================================================

START TRANSACTION;

INSERT INTO `locales_lang` (`code`, `name`, `name_en`)
SELECT seed.`code`, seed.`name`, seed.`name_en`
FROM (
SELECT 'zh' AS `code`, '中文(简体)' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'zh-Hant' AS `code`, '中文(繁体)' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'zh-Hant-hk' AS `code`, '中文(香港繁体)' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'zh-Hant-tw' AS `code`, '中文(台湾繁体)' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'tn' AS `code`, '札那语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'vi' AS `code`, '越南语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'iu' AS `code`, '伊努克提图特语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'it' AS `code`, '意大利语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'id' AS `code`, '印尼语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'hi' AS `code`, '印地语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'en' AS `code`, '英语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ho' AS `code`, '希里莫图语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'he' AS `code`, '希伯来语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'es' AS `code`, '西班牙语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'el' AS `code`, '现代希腊语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'uk' AS `code`, '乌克兰语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ur' AS `code`, '乌尔都语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'tk' AS `code`, '土库曼语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'tr' AS `code`, '土耳其语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ti' AS `code`, '提格里尼亚语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ty' AS `code`, '塔希提语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'tl' AS `code`, '他加禄语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'to' AS `code`, '汤加语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'th' AS `code`, '泰语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ta' AS `code`, '泰米尔语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'te' AS `code`, '泰卢固语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'sl' AS `code`, '斯洛文尼亚语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'sk' AS `code`, '斯洛伐克语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ss' AS `code`, '史瓦帝语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'eo' AS `code`, '世界语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'sm' AS `code`, '萨摩亚语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'sg' AS `code`, '桑戈语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'st' AS `code`, '塞索托语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'sv' AS `code`, '瑞典语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ja' AS `code`, '日语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'tw' AS `code`, '契维语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'qu' AS `code`, '奇楚瓦语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'pt' AS `code`, '葡萄牙语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'pa' AS `code`, '旁遮普语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'no' AS `code`, '挪威语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'nb' AS `code`, '挪威布克莫尔语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'nr' AS `code`, '南恩德贝勒语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'my' AS `code`, '缅甸语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'bn' AS `code`, '孟加拉语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'mn' AS `code`, '蒙古语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'mh' AS `code`, '马绍尔语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'mk' AS `code`, '马其顿语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ml' AS `code`, '马拉亚拉姆语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'mr' AS `code`, '马拉提语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ms' AS `code`, '马来语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'lu' AS `code`, '卢巴卡丹加语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ro' AS `code`, '罗马尼亚语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'lt' AS `code`, '立陶宛语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'lv' AS `code`, '拉脱维亚语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'lo' AS `code`, '老挝语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'kj' AS `code`, '宽亚玛语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'hr' AS `code`, '克罗地亚语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'kn' AS `code`, '坎纳达语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ki' AS `code`, '基库尤语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'cs' AS `code`, '捷克语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ca' AS `code`, '加泰隆语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'nl' AS `code`, '荷兰语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ko' AS `code`, '韩语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ht' AS `code`, '海地克里奥尔语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'gu' AS `code`, '古吉拉特语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ka' AS `code`, '格鲁吉亚语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'kl' AS `code`, '格陵兰语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'km' AS `code`, '高棉语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'lg' AS `code`, '干达语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'kg' AS `code`, '刚果语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'fi' AS `code`, '芬兰语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'fj' AS `code`, '斐济语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'fr' AS `code`, '法语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ru' AS `code`, '俄语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ng' AS `code`, '恩敦加语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'de' AS `code`, '德语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'tt' AS `code`, '鞑靼语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'da' AS `code`, '丹麦语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ts' AS `code`, '聪加语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'cv' AS `code`, '楚瓦什语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'fa' AS `code`, '波斯语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'bs' AS `code`, '波斯尼亚语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'pl' AS `code`, '波兰语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'bi' AS `code`, '比斯拉玛语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'nd' AS `code`, '北恩德贝勒语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ba' AS `code`, '巴什基尔语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'bg' AS `code`, '保加利亚语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'az' AS `code`, '阿塞拜疆语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ar' AS `code`, '阿拉伯语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'af' AS `code`, '阿非利堪斯语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'sq' AS `code`, '阿尔巴尼亚语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ab' AS `code`, '阿布哈兹语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'os' AS `code`, '奥塞梯语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ee' AS `code`, '埃维语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'et' AS `code`, '爱沙尼亚语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ay' AS `code`, '艾马拉语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'lzh' AS `code`, '中文（文言文）' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'am' AS `code`, '阿姆哈拉语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ckb' AS `code`, '中库尔德语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'cy' AS `code`, '威尔士语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'gl' AS `code`, '加利西亚语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ha' AS `code`, '豪萨语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'hy' AS `code`, '亚美尼亚语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ig' AS `code`, '伊博语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'kmr' AS `code`, '北库尔德语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ln' AS `code`, '林加拉语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'nso' AS `code`, '北索托语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ny' AS `code`, '齐切瓦语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'om' AS `code`, '奥洛莫语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'sn' AS `code`, '绍纳语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'so' AS `code`, '索马里语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'sr' AS `code`, '塞尔维亚语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'sw' AS `code`, '斯瓦希里语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'xh' AS `code`, '科萨语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'yo' AS `code`, '约鲁巴语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'zu' AS `code`, '祖鲁语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'bo' AS `code`, '藏语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'nan' AS `code`, '闽南语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'wuu' AS `code`, '吴语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'yue' AS `code`, '粤语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'cmn' AS `code`, '西南官话' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'ug' AS `code`, '维吾尔语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'fuv' AS `code`, '尼日利亚富拉语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'hu' AS `code`, '匈牙利语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'kam' AS `code`, '坎巴语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'luo' AS `code`, '肯尼亚语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'rw' AS `code`, '基尼阿万达语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'umb' AS `code`, '卢欧语' AS `name`, NULL AS `name_en`
UNION ALL
SELECT 'wo' AS `code`, '沃洛夫语' AS `name`, NULL AS `name_en`
) AS seed
WHERE NOT EXISTS (
    SELECT 1
    FROM `locales_lang` existing
    WHERE existing.`code` = seed.`code`
);

COMMIT;
