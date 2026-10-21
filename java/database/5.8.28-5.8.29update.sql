-- 修复：product.product_stock 字段容量过小
-- 现状：smallint(5) UNSIGNED，上限 65535
-- 问题：与代码常量 PRODUCT_STOCK_MAX_LIMIT = 999999 不匹配，
--      添加预约商品等场景填入大库存时报"数据类型异常"
-- 方案：调整为 int(11) UNSIGNED，与 product_sku.sku_stock / gift_stock / seckill_stock
--      等其它库存字段保持一致；老数据 100% 兼容（原字段最大 65535，远低于新上限）
-- 风险：ALTER TABLE MODIFY 对大表会锁表，建议在业务低峰执行
ALTER TABLE `product`
    MODIFY COLUMN `product_stock` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '商品库存数量';

-- 服务费
-- =============================================================
-- 5.8.27 -> 5.8.32 合并增量脚本
-- 内容：
--   ① product_sku / store_product_sku / order_item 配送员服务费字段
--   ② product_sku / store_product_sku / order_item 服务员服务费字段
--   ③ product / store_product 单规格商品服务费字段
--   ④ 配送员/服务员钱包 + 钱包流水
--   ⑤ 提现申请单
--   ⑥ config 种子数据：配送员 10 项 + 服务员 10 项 + 门店商品图片配置 1 项（共 21 条）
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- 范围：仅 O2O 版本数据库
-- =============================================================


-- =============================================================
-- ① 商品配送员服务费字段（平台 SKU / 门店覆盖 / 订单明细快照）
-- =============================================================

ALTER TABLE `product_sku`
    ADD COLUMN `delivery_service_fee_type`  TINYINT       NOT NULL DEFAULT 0    COMMENT '配送员服务费类型：0=固定金额 1=按比例' AFTER `cost_price`,
    ADD COLUMN `delivery_service_fee_value` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '配送员服务费值：固定金额(元)或按比例(%)' AFTER `delivery_service_fee_type`;

ALTER TABLE `store_product_sku`
    ADD COLUMN `delivery_service_fee_type`  TINYINT       NOT NULL DEFAULT 0    COMMENT '配送员服务费类型（门店覆盖）',
    ADD COLUMN `delivery_service_fee_value` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '配送员服务费值（门店覆盖）';

ALTER TABLE `order_item`
    ADD COLUMN `delivery_service_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '配送员服务费(单件快照，仅O2O写入)';


-- =============================================================
-- ② 商品服务员服务费字段（平台 SKU / 门店覆盖 / 订单明细快照）
-- =============================================================

ALTER TABLE `product_sku`
    ADD COLUMN `server_service_fee_type`  TINYINT       NOT NULL DEFAULT 0    COMMENT '服务员服务费类型：0=固定金额 1=按比例' AFTER `delivery_service_fee_value`,
    ADD COLUMN `server_service_fee_value` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '服务员服务费值：固定金额(元)或按比例(%)' AFTER `server_service_fee_type`;

ALTER TABLE `store_product_sku`
    ADD COLUMN `server_service_fee_type`  TINYINT       NOT NULL DEFAULT 0    COMMENT '服务员服务费类型（门店覆盖）',
    ADD COLUMN `server_service_fee_value` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '服务员服务费值（门店覆盖）';

ALTER TABLE `order_item`
    ADD COLUMN `server_service_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '服务员服务费(单件快照，仅O2O上门服务订单写入)';

-- =============================================================
-- ③ 单规格商品服务费字段（product / store_product 顶层）
-- =============================================================

ALTER TABLE `product`
    ADD COLUMN `delivery_service_fee_type`  TINYINT       NOT NULL DEFAULT 0    COMMENT '配送员服务费类型（平台层，仅单规格商品使用）：0=固定金额 1=按比例',
    ADD COLUMN `delivery_service_fee_value` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '配送员服务费值（平台层，仅单规格商品使用）：固定金额(元)或按比例(%)',
    ADD COLUMN `server_service_fee_type`    TINYINT       NOT NULL DEFAULT 0    COMMENT '服务员服务费类型（平台层，仅单规格商品使用）：0=固定金额 1=按比例',
    ADD COLUMN `server_service_fee_value`   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '服务员服务费值（平台层，仅单规格商品使用）：固定金额(元)或按比例(%)';

ALTER TABLE `store_product`
    ADD COLUMN `delivery_service_fee_type`  TINYINT       NOT NULL DEFAULT 0    COMMENT '配送员服务费类型（门店覆盖，仅单规格商品使用）：0=固定金额 1=按比例',
    ADD COLUMN `delivery_service_fee_value` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '配送员服务费值（门店覆盖，仅单规格商品使用）',
    ADD COLUMN `server_service_fee_type`    TINYINT       NOT NULL DEFAULT 0    COMMENT '服务员服务费类型（门店覆盖，仅单规格商品使用）：0=固定金额 1=按比例',
    ADD COLUMN `server_service_fee_value`   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '服务员服务费值（门店覆盖，仅单规格商品使用）';


-- =============================================================
-- ③-2 门店覆盖商品主图、详情页、相册字段
-- =============================================================

ALTER TABLE `store_product`
    ADD COLUMN `pic_url`      VARCHAR(500) DEFAULT NULL COMMENT '门店覆盖主图（config=1时使用，NULL表示未覆盖）',
    ADD COLUMN `product_desc` TEXT         DEFAULT NULL COMMENT '门店覆盖详情页（config=1时使用，NULL表示未覆盖）';

ALTER TABLE `product_gallery`
    ADD COLUMN `shop_id` BIGINT NOT NULL DEFAULT 0 COMMENT '门店ID，0=平台';

ALTER TABLE `product_gallery`
    ADD INDEX `idx_product_shop` (`product_id`, `shop_id`);


-- =============================================================
-- ④ 配送员/服务员钱包 + 钱包流水
-- =============================================================

CREATE TABLE `service_staff_wallet` (
                                        `wallet_id`      INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '钱包ID',
                                        `staff_id`       INT           NOT NULL COMMENT '关联 service_staff.staff_id',
                                        `balance`        DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '可用余额',
                                        `frozen_balance` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '冻结余额',
                                        `total_income`   DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计收入',
                                        `create_time`    BIGINT        DEFAULT NULL COMMENT '创建时间',
                                        `update_time`    BIGINT        DEFAULT NULL COMMENT '更新时间',
                                        PRIMARY KEY (`wallet_id`),
                                        UNIQUE KEY `uk_staff_id` (`staff_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='O2O配送员/服务员钱包';

CREATE TABLE `service_staff_wallet_log` (
                                            `log_id`        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '流水ID',
                                            `staff_id`      INT             NOT NULL COMMENT '配送员/服务员ID',
                                            `order_id`      INT             DEFAULT NULL COMMENT '关联订单ID(周期购为子订单ID)',
                                            `withdraw_id`   INT             DEFAULT NULL COMMENT '关联提现单ID',
                                            `amount`        DECIMAL(10,2)   NOT NULL COMMENT '变动金额：正=入账 负=扣减',
                                            `balance_after` DECIMAL(12,2)   NOT NULL COMMENT '变动后余额',
                                            `frozen_after`  DECIMAL(12,2)   NOT NULL DEFAULT 0.00 COMMENT '变动后冻结余额',
                                            `business_type` TINYINT         NOT NULL DEFAULT 1 COMMENT '业务来源：1=配送服务费 2=服务员服务费',
                                            `type`          TINYINT         NOT NULL COMMENT '类型：1=服务费入账 2=提现完成(实扣冻结) 3=扣减 4=提现申请(余额→冻结) 5=提现驳回(冻结→回退余额)',
                                            `remark`        VARCHAR(255)    DEFAULT NULL COMMENT '备注',
                                            `create_time`   BIGINT          DEFAULT NULL COMMENT '创建时间',
                                            PRIMARY KEY (`log_id`),
                                            KEY `idx_staff_id` (`staff_id`),
                                            KEY `idx_order_id` (`order_id`),
                                            KEY `idx_withdraw_id` (`withdraw_id`),
                                            KEY `idx_staff_business_type` (`staff_id`, `business_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='O2O配送员/服务员钱包流水';

ALTER TABLE `service_staff_wallet_log`
    ADD COLUMN `transaction_sn` VARCHAR(60) DEFAULT NULL COMMENT '交易编号：订单编号或提现编号' AFTER `withdraw_id`;


-- =============================================================
-- ⑤ 配送员/服务员提现申请单
-- =============================================================

CREATE TABLE `service_staff_withdraw` (
                                          `withdraw_id`     INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '提现单ID',
                                          `withdraw_sn`     VARCHAR(40)   NOT NULL COMMENT '交易编号（业务唯一）',
                                          `staff_id`        INT           NOT NULL COMMENT '配送员/服务员ID',
                                          `business_type`   TINYINT       NOT NULL DEFAULT 1 COMMENT '业务来源：1=配送服务费 2=服务员服务费',
                                          `amount`          DECIMAL(10,2) NOT NULL COMMENT '申请提现金额',
                                          `status`          TINYINT       NOT NULL DEFAULT 0 COMMENT '0=待审核 1=已打款 2=已驳回',
                                          `account_id`     INT            NOT NULL COMMENT '关联 user_withdraw_account.account_id',
                                          `account_type`    TINYINT       NOT NULL COMMENT '冗余：1银行卡 2支付宝 3微信',
                                          `account_name`    VARCHAR(60)   NOT NULL COMMENT '冗余收款人姓名',
                                          `account_no`      VARCHAR(120)  NOT NULL COMMENT '冗余收款账号',
                                          `bank_name`       VARCHAR(120)  DEFAULT '' COMMENT '冗余开户行',
                                          `admin_remark`    VARCHAR(255)  DEFAULT '' COMMENT '管理员备注',
                                          `payment_voucher` VARCHAR(500)  DEFAULT '' COMMENT '打款凭证图片URL',
                                          `reject_reason`   VARCHAR(255)  DEFAULT '' COMMENT '拒绝理由',
                                          `audit_admin_id`  INT           DEFAULT 0 COMMENT '审核管理员ID',
                                          `audit_time`     BIGINT         DEFAULT NULL COMMENT '审核时间',
                                          `apply_time`     BIGINT         NOT NULL COMMENT '申请时间',
                                          `frequency_unit`  VARCHAR(10)   NOT NULL DEFAULT 'day' COMMENT '快照：申请时的频次单位 day/week/month',
                                          `period_start`    BIGINT        NOT NULL COMMENT '快照：所属计数周期起（含）',
                                          `period_end`      BIGINT        NOT NULL COMMENT '快照：所属计数周期止（不含）',
                                          `create_time`    BIGINT         DEFAULT NULL COMMENT '创建时间',
                                          `update_time`    BIGINT         DEFAULT NULL COMMENT '更新时间',
                                          `is_delete`      TINYINT        NOT NULL DEFAULT 0 COMMENT '0=有效 1=删除',
                                          PRIMARY KEY (`withdraw_id`),
                                          UNIQUE KEY `uk_withdraw_sn` (`withdraw_sn`),
                                          KEY `idx_staff_business_period` (`staff_id`, `business_type`, `period_start`, `is_delete`),
                                          KEY `idx_status_apply_time` (`status`, `apply_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='配送员/服务员提现申请';


-- =============================================================
-- ⑥ config 种子数据：配送员 10 项 + 服务员 10 项 + 门店商品图片配置 1 项（共 21 条）
-- =============================================================

INSERT INTO `config` (`biz_code`, `biz_val`,
                      `create_time`, `create_by_id`, `create_by_name`,
                      `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES
-- 配送员服务费 + 提现规则
('storeAssignDeliveryServiceFee',         '0',       UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('deliveryServiceFeeEnabled',             '1',       UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('defaultDeliveryServiceFeeRate',         '0.00',    UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('deliveryStaffWithdrawalEnabled',        '1',       UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('deliveryStaffWithdrawalMinAmount',      '100.00',  UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('deliveryStaffWithdrawalFrequencyUnit',  'day',     UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('deliveryStaffWithdrawalFrequencyCount', '1',       UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('deliveryStaffWithdrawalReviewMethod',   '1',       UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('deliveryStaffWithdrawalDescription',    '',        UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('deliveryStaffWithdrawalReceiptMethod',  '[1,2,3]', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
-- 服务员服务费 + 提现规则
('storeAssignServerServiceFee',         '0',       UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('serverServiceFeeEnabled',             '1',       UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('defaultServerServiceFeeRate',         '0.00',    UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('serverStaffWithdrawalEnabled',        '1',       UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('serverStaffWithdrawalMinAmount',      '100.00',  UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('serverStaffWithdrawalFrequencyUnit',  'day',     UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('serverStaffWithdrawalFrequencyCount', '1',       UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('serverStaffWithdrawalReviewMethod',   '1',       UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('serverStaffWithdrawalDescription',    '',        UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
('serverStaffWithdrawalReceiptMethod',  '[1,2,3]', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
-- 门店商品图片/详情/相册覆盖配置
('storeAssignProductImage',             '0',       UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);


-- =============================================================
-- ⑦ authority 权限菜单：员工收益管理（挂 finance 下，合并配送员+服务员）
-- =============================================================

-- 员工收益管理（一级分组，挂 finance id=12 下）
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                         `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001090, 'staffFundManagement', '员工收益管理', 12, 55, 1, '[]', '', 'iconfont-tig icon-zijinguanli', 0, 'admin');

-- 配送设置
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                         `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001091, 'deliveryFundSettingsManage', '配送设置', 40001090, 50, 1,
        '[{"auth_name":"编辑","auth_sn":"deliveryFundSettingsEditManage"}]',
        'finance/delivery_fund_management/settings/', '', 0, 'admin');

-- 服务设置
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                         `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001092, 'serverFundSettingsManage', '服务设置', 40001090, 51, 1,
        '[{"auth_name":"编辑","auth_sn":"serverFundSettingsEditManage"}]',
        'finance/server_fund_management/settings/', '', 0, 'admin');

-- 资金总览（合并：配送员+服务员）
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                         `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001093, 'staffFundWalletManage', '资金总览', 40001090, 52, 1, '[]',
        'finance/staff_fund_wallet/overview/', '', 0, 'admin');

-- 资金日志（合并：配送员+服务员）
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                         `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001094, 'staffFundWalletLogManage', '资金日志', 40001090, 53, 1, '[]',
        'finance/staff_fund_wallet_log/list/', '', 0, 'admin');

-- 配送员提现管理
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                         `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001095, 'deliveryStaffWithdrawManage', '配送员提现管理', 40001090, 54, 1,
        '[{"auth_name":"审核","auth_sn":"deliveryStaffWithdrawAuditManage"}]',
        'finance/delivery_staff_withdraw/list/', '', 0, 'admin');

-- 服务员提现管理
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                         `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001096, 'serverStaffWithdrawManage', '服务员提现管理', 40001090, 55, 1,
        '[{"auth_name":"审核","auth_sn":"serverStaffWithdrawAuditManage"}]',
        'finance/server_staff_withdraw/list/', '', 0, 'admin');


-- D1072-renovationPromotion 装修推广 & 商品推广分享信息
-- 装修表新增分享信息字段
ALTER TABLE `decorate` ADD COLUMN `share_info` TEXT DEFAULT NULL COMMENT '分享信息（JSON：shareTitle/shareDesc/shareImage）' AFTER `binded_h5_id`;

-- 商品表新增分享信息字段
ALTER TABLE `product` ADD COLUMN `share_info` TEXT DEFAULT NULL COMMENT '分享信息（JSON：productName/productPrice/picUrl）' AFTER `attributes_tpl_id`;


-- D1075: 跨境 X/Twitter 第三方登录 - 默认配置项
-- 关联：.ai-os/specs/twitter-login.md
-- 表：config（实际表名，参考 ConfigPO @TableName("config")）
-- 列：biz_code / biz_val（数据列）+ BasePO 基础列（create_time / create_by_id / create_by_name / update_time / update_by_id / update_by_name / is_del）
-- 幂等策略：WHERE NOT EXISTS 包裹 INSERT，重复执行安全（不依赖 biz_code 唯一索引）

INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'twitterLoginOn', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'twitterLoginOn' AND `is_del` = 0);

INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'twitterClientId', '', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'twitterClientId' AND `is_del` = 0);

INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'twitterClientSecret', '', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'twitterClientSecret' AND `is_del` = 0);

