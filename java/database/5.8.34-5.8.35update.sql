-- =============================================================
-- 5.8.34 -> 5.8.35 增量脚本
-- 内容：
--   A. D1107 会员余额 / 分销佣金 账户隔离（user / user_balance_log / user_withdraw_apply）
--   B. 保证金模块（店铺 + 供应商，统一 owner 维度）：8 张表 + 后台菜单 + 站内信类型注释
--   C. D1111 跨境版会员中心装修补「发票管理」菜单
--   D. D1112 PC登录页装修菜单（PC端装修 > 登录页装修）
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- =============================================================

-- =============================================================
-- A. D1107 会员余额 / 分销佣金 账户隔离
-- 原则（需求「四、老客户数据处理」）：老客户数据全部归属余额，存量不回溯。
-- 本脚本只新增字段、不搬动任何资金；老客户余额保持不变，佣金账户从 0 开始，
-- 仅上线后新结算的佣金进入佣金账户。
-- =============================================================

-- 1) user 增加佣金账户字段（默认 0；老客户佣金不迁入此处，仍在 balance 中）
ALTER TABLE `user`
  ADD COLUMN `commission_balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '分销佣金可提现余额（上线后新结算佣金累加，老数据归余额）' AFTER `frozen_balance`,
  ADD COLUMN `frozen_commission_balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '分销佣金提现冻结余额' AFTER `commission_balance`;

-- 2) user_balance_log 加钱包维度（0=余额账户 1=佣金账户）；历史流水默认 0=余额，符合“老客户归余额”
ALTER TABLE `user_balance_log`
  ADD COLUMN `wallet_type` TINYINT NOT NULL DEFAULT 0 COMMENT '钱包类型：0余额账户 1佣金账户' AFTER `change_type`;

-- 3) user_withdraw_apply 加钱包维度（0=余额提现 1=佣金提现）
ALTER TABLE `user_withdraw_apply`
  ADD COLUMN `wallet_type` TINYINT NOT NULL DEFAULT 0 COMMENT '提现钱包：0余额 1佣金' AFTER `user_id`;

-- 说明：不对历史 user_balance_log 做佣金回标（老客户佣金属于余额）；
--      佣金提现为分销核心能力，不设单独开关，按可用佣金校验。

-- =============================================================
-- B. 保证金模块（统一 owner 维度：店铺 + 供应商）
-- 内容(8表)：账户、规则、规则类目关联、应缴调整记录、流水、充值申请、提现/退还申请、惩罚下架记录
-- owner 维度：owner_type 1店铺(SHOP) 2供应商(SUPPLIER)；owner_id 店铺为shopId,供应商为vendorId。
--   账户/流水/调整/充值/退还/下架 6 表按 (owner_type, owner_id) 维度；规则表(shop_deposit_rule
--   + shop_deposit_rule_category)与 owner 无关，保持不变。
-- =============================================================

-- -------------------------------------------------------------
-- 保证金账户(店铺/供应商通用，按 owner 维度)
-- -------------------------------------------------------------
CREATE TABLE `shop_deposit` (
  `shop_deposit_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `owner_type`      tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '持有者类型 1店铺 2供应商',
  `owner_id`        int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '持有者id(店铺为shopId,供应商为vendorId)',
  `paid`            decimal(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '实缴余额(净额)=充值入账-提现-退还-平台扣除',
  `required_manual` decimal(10,2) UNSIGNED NULL DEFAULT NULL COMMENT '手动应缴覆盖值,NULL=按规则实时算',
  `required_snapshot` decimal(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '上次应缴快照(类目变更diff用,记录调整前后值)',
  `collect_due_time`    int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '收取节点缓冲到期时间,0未触发',
  `supplement_due_time` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '补缴缓冲到期时间,0未触发',
  `collect_activated`   tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '收取是否已激活:0未激活(首单前) 1已激活;节点=上架收在上架激活,节点=首单收在首单激活',
  `penalty_status`  tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '惩罚状态 0正常 1限制上架 2全店下架(DepositPenaltyAction)',
  `status`          tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '账户状态 1正常',
  `add_time`        int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
  `update_time`     int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
  PRIMARY KEY (`shop_deposit_id`) USING BTREE,
  UNIQUE INDEX `uk_owner`(`owner_type`, `owner_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='保证金账户(店铺/供应商)' ROW_FORMAT=Dynamic;

-- -------------------------------------------------------------
-- 店铺保证金规则(主体类型×店铺类型×多类目×生效期)
-- 约束(应用层)：同一(主体类型×店铺类型×类目)下生效时间区间不可重叠
-- -------------------------------------------------------------
CREATE TABLE `shop_deposit_rule` (
  `shop_deposit_rule_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `rule_name`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '规则名称',
  `merchant_types` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '适用主体类型,逗号分隔 1个人 2企业(MerchantType)',
  `shop_types`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '适用店铺类型,逗号分隔 1店铺 2供应商(DepositRuleShopType)',
  `deposit_amount` decimal(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '保证金额',
  `start_time`     int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '生效开始',
  `end_time`       int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '生效结束',
  `status`         tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  `is_delete`      tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除',
  `add_time`       int(11) UNSIGNED NOT NULL DEFAULT 0,
  `update_time`    int(11) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`shop_deposit_rule_id`) USING BTREE,
  INDEX `status`(`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='店铺保证金规则' ROW_FORMAT=Dynamic;

-- -------------------------------------------------------------
-- 店铺保证金规则适用类目(一规则多类目)
-- -------------------------------------------------------------
CREATE TABLE `shop_deposit_rule_category` (
  `id`          int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `rule_id`     int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '规则id',
  `category_id` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '平台类目id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rule_category`(`rule_id`, `category_id`) USING BTREE,
  INDEX `category_id`(`category_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='店铺保证金规则适用类目' ROW_FORMAT=Dynamic;

-- -------------------------------------------------------------
-- 保证金流水(店铺/供应商通用，按 owner 维度)
-- -------------------------------------------------------------
CREATE TABLE `shop_deposit_log` (
  `shop_deposit_log_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `owner_type`    tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '持有者类型 1店铺 2供应商',
  `owner_id`      int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '持有者id(店铺为shopId,供应商为vendorId)',
  `type`          tinyint(2) UNSIGNED NOT NULL DEFAULT 0 COMMENT '流水类型 1充值入账 2提现出账 3退还出账 4平台扣除 5平台调整(双向)',
  `change_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '变动金额(带符号)',
  `before_paid`   decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '变动前实缴',
  `after_paid`    decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '变动后实缴',
  `related_id`    int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '关联单id(充值/退还/订单/售后)',
  `order_id`      int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '关联订单(平台扣除/赔付时)',
  `admin_id`      int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '操作管理员',
  `remark`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `add_time`      int(11) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`shop_deposit_log_id`) USING BTREE,
  INDEX `idx_owner_time`(`owner_type`, `owner_id`, `add_time`) USING BTREE,
  INDEX `idx_type_time`(`type`, `add_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='保证金流水(店铺/供应商)' ROW_FORMAT=Dynamic;

-- -------------------------------------------------------------
-- 保证金应缴调整记录(应缴变更历史，按 owner 维度)
-- -------------------------------------------------------------
CREATE TABLE `shop_deposit_adjust_log` (
  `shop_deposit_adjust_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `owner_type`      tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '持有者类型 1店铺 2供应商',
  `owner_id`        int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '持有者id(店铺为shopId,供应商为vendorId)',
  `before_required` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '调整前应缴',
  `after_required`  decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '调整后应缴',
  `source`          tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '来源 1按类目最高 2手动固定(DepositAdjustSource)',
  `admin_id`        int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '操作管理员',
  `reason`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '调整原因',
  `add_time`        int(11) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`shop_deposit_adjust_id`) USING BTREE,
  INDEX `idx_owner`(`owner_type`, `owner_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='保证金应缴调整记录' ROW_FORMAT=Dynamic;

-- -------------------------------------------------------------
-- 保证金充值申请(店铺/供应商通用，按 owner 维度)
-- -------------------------------------------------------------
CREATE TABLE `shop_deposit_recharge` (
  `shop_deposit_recharge_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `owner_type`     tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '持有者类型 1店铺 2供应商',
  `owner_id`       int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '持有者id(店铺为shopId,供应商为vendorId)',
  `amount`         decimal(10,2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '充值金额',
  `pay_voucher`    text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '转账凭证',
  `payee_account_id` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '充值到的收款账户id(shop_deposit_payee_account)',
  `payee_account_type` tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '收款账户类型快照 1银行卡 2支付宝 3微信',
  `payee_snapshot`   varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '收款账户快照JSON(类型/户名/账号/银行)',
  `remark`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '申请备注',
  `status`         tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0待审核 1已入账 2驳回',
  `audit_admin_id` int(11) UNSIGNED NOT NULL DEFAULT 0,
  `audit_time`     int(11) UNSIGNED NOT NULL DEFAULT 0,
  `audit_remark`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `add_time`       int(11) UNSIGNED NOT NULL DEFAULT 0,
  `update_time`    int(11) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`shop_deposit_recharge_id`) USING BTREE,
  INDEX `idx_owner`(`owner_type`, `owner_id`) USING BTREE,
  INDEX `idx_status_time`(`status`, `add_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='保证金充值申请' ROW_FORMAT=Dynamic;

-- -------------------------------------------------------------
-- 保证金收款账户(平台级，充值核对用)
-- -------------------------------------------------------------
CREATE TABLE `shop_deposit_payee_account` (
  `shop_deposit_payee_account_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `account_type` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '账户类型 1银行卡 2支付宝 3微信',
  `account_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '户名',
  `account_no`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '账号',
  `bank_name`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '银行名称',
  `bank_branch`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '银行支行',
  `status`       tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  `sort_order`   int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序',
  `add_time`     int(11) UNSIGNED NOT NULL DEFAULT 0,
  `update_time`  int(11) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`shop_deposit_payee_account_id`) USING BTREE,
  INDEX `idx_status_sort`(`status`, `sort_order`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='保证金收款账户' ROW_FORMAT=Dynamic;

-- -------------------------------------------------------------
-- 保证金提现/退还申请(店铺/供应商通用，按 owner 维度)
-- -------------------------------------------------------------
CREATE TABLE `shop_deposit_refund` (
  `shop_deposit_refund_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `owner_type`     tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '持有者类型 1店铺 2供应商',
  `owner_id`       int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '持有者id(店铺为shopId,供应商为vendorId)',
  `type`           tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '1部分退还 2全部退还',
  `amount`         decimal(10,2) UNSIGNED NOT NULL DEFAULT 0.00,
  `payee_info`     text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '收款信息',
  `reason`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status`         tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '0待审核 1已通过 2未通过',
  `pay_voucher`    text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '打款凭证',
  `audit_admin_id` int(11) UNSIGNED NOT NULL DEFAULT 0,
  `audit_time`     int(11) UNSIGNED NOT NULL DEFAULT 0,
  `audit_remark`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `add_time`       int(11) UNSIGNED NOT NULL DEFAULT 0,
  `update_time`    int(11) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`shop_deposit_refund_id`) USING BTREE,
  INDEX `idx_owner`(`owner_type`, `owner_id`) USING BTREE,
  INDEX `idx_status_time`(`status`, `add_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='保证金提现/退还申请' ROW_FORMAT=Dynamic;

-- -------------------------------------------------------------
-- 保证金惩罚下架记录(店铺/供应商通用，按 owner 维度)
-- -------------------------------------------------------------
CREATE TABLE `shop_deposit_offshelf_log` (
  `shop_deposit_offshelf_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `owner_type`   tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '持有者类型 1店铺 2供应商',
  `owner_id`     int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '持有者id(店铺为shopId,供应商为vendorId)',
  `product_id`   int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '被惩罚下架的商品id',
  `status`       tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '1已下架待恢复 2已恢复',
  `off_time`     int(11) UNSIGNED NOT NULL DEFAULT 0,
  `restore_time` int(11) UNSIGNED NOT NULL DEFAULT 0,
  `add_time`     int(11) UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`shop_deposit_offshelf_id`) USING BTREE,
  INDEX `idx_owner_status`(`owner_type`, `owner_id`, `status`) USING BTREE,
  INDEX `product_id`(`product_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='保证金惩罚下架记录' ROW_FORMAT=Dynamic;

-- =============================================================
-- 后台菜单注册（authority 表，幂等 INSERT）
-- 平台端：财务(12) 下新增父菜单「保证金管理」depositManage(sort 51，排在分账管理 50 之后)，
--   其下挂 6 个二级菜单（按原型图：设置/规则/账户/查询/款项审核/明细）；店铺端/供应商端各在财务中心下挂「保证金」入口。
-- child_auth 为按钮级权限（与控制器方法级 @PreAuthorize 一一对应）。
-- =============================================================

-- ---- 平台端父菜单：保证金管理 ----
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'depositManage','保证金管理', 12, 51, 1, '[]', '', 'iconfont-tig icon-baozhengjin', 0, 'admin'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='depositManage' AND `admin_type`='admin');

-- ---- 平台端二级菜单：保证金设置（全局设置-保存）----
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'depositSettingManage','保证金设置',
       (SELECT `authority_id` FROM (SELECT `authority_id` FROM `authority` WHERE `authority_sn`='depositManage' AND `admin_type`='admin' LIMIT 1) p),
       10, 1, '[{"auth_name":"保存","auth_sn":"depositSettingSave"}]',
       'finance/deposit_setting/index/', '', 0, 'admin'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='depositSettingManage' AND `admin_type`='admin');

-- ---- 平台端二级菜单：保证金规则（增删改）----
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'depositRuleManage','保证金规则',
       (SELECT `authority_id` FROM (SELECT `authority_id` FROM `authority` WHERE `authority_sn`='depositManage' AND `admin_type`='admin' LIMIT 1) p),
       20, 1, '[{"auth_name":"增删改","auth_sn":"depositRuleUpdate"}]',
       'finance/deposit_rule/list/', '', 0, 'admin'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='depositRuleManage' AND `admin_type`='admin');

-- ---- 平台端二级菜单：保证金账户（收款账户：微信/支付宝/银行卡，增删改）----
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'depositPayeeAccountManage','保证金账户',
       (SELECT `authority_id` FROM (SELECT `authority_id` FROM `authority` WHERE `authority_sn`='depositManage' AND `admin_type`='admin' LIMIT 1) p),
       30, 1, '[{"auth_name":"增删改","auth_sn":"depositPayeeAccountUpdate"}]',
       'finance/deposit_payee_account/list/', '', 0, 'admin'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='depositPayeeAccountManage' AND `admin_type`='admin');

-- ---- 平台端二级菜单：保证金查询（商户保证金台账：应缴/已交/待缴/状态 + 调整应缴/赔付扣除/发送预警）----
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'depositAccountManage','保证金查询',
       (SELECT `authority_id` FROM (SELECT `authority_id` FROM `authority` WHERE `authority_sn`='depositManage' AND `admin_type`='admin' LIMIT 1) p),
       40, 1, '[{"auth_name":"调整应缴","auth_sn":"depositAdjust"},{"auth_name":"赔付扣除","auth_sn":"depositDeduct"},{"auth_name":"发送预警","auth_sn":"depositSendWarning"}]',
       'finance/deposit_account/list/', '', 0, 'admin'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='depositAccountManage' AND `admin_type`='admin');

-- ---- 平台端二级菜单：款项审核（充值审核 + 退还审核 合并入口，页内分Tab）----
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'depositPaymentAuditManage','款项审核',
       (SELECT `authority_id` FROM (SELECT `authority_id` FROM `authority` WHERE `authority_sn`='depositManage' AND `admin_type`='admin' LIMIT 1) p),
       50, 1, '[{"auth_name":"充值审核","auth_sn":"depositRechargeAudit"},{"auth_name":"退还审核","auth_sn":"depositRefundAudit"}]',
       'finance/deposit_payment_audit/list/', '', 0, 'admin'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='depositPaymentAuditManage' AND `admin_type`='admin');

-- ---- 平台端二级菜单：交易明细（只读）----
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'depositLogManage','交易明细',
       (SELECT `authority_id` FROM (SELECT `authority_id` FROM `authority` WHERE `authority_sn`='depositManage' AND `admin_type`='admin' LIMIT 1) p),
       60, 1, '[]',
       'finance/deposit_log/list/', '', 0, 'admin'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='depositLogManage' AND `admin_type`='admin');

-- ---- 店铺端：财务中心(10000291) 下新增「保证金」入口 ----
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'shopDepositManage','保证金', 10000291, 50, 1, '[]', 'capitalfund/deposit/index/', '', 0, 'shop'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='shopDepositManage' AND `admin_type`='shop');

-- ---- 供应商端：财务中心(11001013) 下新增「保证金」入口 ----
INSERT INTO `authority` (`authority_sn`,`authority_name`,`parent_id`,`sort_order`,`is_show`,`child_auth`,`route_link`,`authority_ico`,`is_system`,`admin_type`)
SELECT 'vendorDepositManage','保证金', 11001013, 50, 1, '[]', 'capitalfund/deposit/index/', '', 0, 'vendor'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='vendorDepositManage' AND `admin_type`='vendor');

-- ---- 站内信消息类型注释登记：新增 44 保证金预警 ----
ALTER TABLE `admin_msg` MODIFY COLUMN `msg_type` tinyint(3) UNSIGNED NOT NULL DEFAULT 0
  COMMENT '消息类型,11:新订单,12:已付款订单,13:订单完成,21:商品库存预警,22:商品无货,23:商品下架;31:有效订单被取消,32:售后申请,41:店铺入驻申请,42:店铺资质修改,43:店铺违规,44:保证金预警';

-- =============================================================
-- C. D1111 跨境版会员中心装修补「发票管理」菜单
-- 目的：跨境版会员中心装修(decorate_discrete, decorate_sn='memberDecorate')的
--       已保存菜单数据缺少「发票管理」入口，导致 detail 接口不返回。
--       本脚本在「积分商城」菜单项之后补入「发票管理」菜单项。
-- 幂等：仅当该 memberDecorate 数据尚未包含 invoiceManagement 时才补入，
--       已存在发票管理的记录不会重复添加，其它装修数据不动。
-- =============================================================

UPDATE `decorate_discrete`
SET `data` = REPLACE(
        `data`,
        '"link":"/pages/exchange/list"}}',
        '"link":"/pages/exchange/list"}},{"type":"default","picTitle":"发票管理","picThumb":"https://oss.tigshop.com/static/user/fapiao.png","picUrl":"https://oss.tigshop.com/static/user/fapiao.png","picLink":{"path":"default","label":"发票管理","name":"发票管理","link":"/pages/user/invoiceManagement/index"}}'
    )
WHERE `decorate_sn` = 'memberDecorate'
  AND `data` LIKE '%/pages/exchange/list%'
  AND `data` NOT LIKE '%invoiceManagement%';


-- =============================================================
-- 营业执照类型配置(businessLicenseType)：存量库若已配置营业执照(businessLicense非空)
-- 则默认补入 ["icp"]；幂等，仅在缺失时插入。
-- =============================================================
INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'businessLicenseType', '["icp"]', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'businessLicense' AND `biz_val` IS NOT NULL AND `biz_val` <> '' AND `is_del` = 0)
  AND NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'businessLicenseType' AND `is_del` = 0);

-- =============================================================
-- D. D1112 PC登录页装修菜单（PC端装修 - 登录页装修列表，decorateType=2 & pageType=5）
-- 后台菜单：PC端装修 > 登录页装修（放在「搜索页装修」(56)之后，sort_order=57）
-- child_auth 复用「首页装修」的装修权限点（发布/设为首页/编辑/模块编辑/存入草稿/新增），不新增权限
-- route_link 为前端菜单路由，取值 '-'（与父级 PC端装修一致，具体子路由由前端约定）
-- 幂等：通过 NOT EXISTS 判断，已存在则跳过，可重复执行。
-- =============================================================
INSERT INTO `authority`
(`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'pcLoginDecorateManage', '登录页装修', 62, 57, 1,
       '[{\"authName\":\"发布\",\"authSn\":\"decoratePublishManage\"},{\"authName\":\"设为首页\",\"authSn\":\"decorateSetHomeManage\"},{\"authName\":\"编辑\",\"authSn\":\"decorateModifyManage\"},{\"authName\":\"模块编辑\",\"authSn\":\"decorateDiscreteModifyManage\"},{\"authName\":\"存入草稿\",\"authSn\":\"decorateSaveDraftManage\"},{\"authName\":\"新增\",\"authSn\":\"decorateCreateManage\"}]',
       '-', '', 0, 'admin'
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `authority`) AS t
    WHERE t.`authority_sn` = 'pcLoginDecorateManage' AND t.`admin_type` = 'admin'
);
