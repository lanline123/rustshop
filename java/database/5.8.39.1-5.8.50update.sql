-- =============================================================
-- v5.8.39.1 -> v5.8.50 增量脚本
-- 内容：
--   1) 售后流程优化：aftersales 新增 close_type 关闭/拒绝子类型（D1120）
--   2) 美团配送：门店美团配送配置表、delivery_order 运力字段、同城/美团配送开关（D1125）
--   3) 小程序头像图片鉴黄鉴违开关 miniImgSecCheck（D1141）
--   4) 商品评价开关与匿名评价：comment.is_anonymous + 两个开关（D1146）
--   5) 微信小程序分享是否需要登录 wechatShareNeedLogin（D1148）
--   6) 供应商售后审核开关 + 退款申请/会员等级子权限补齐（D1144-bugfix）
--   7) 易宝支付（YOP）接入配置项与支付方式排序（D1150-yibaoPay）
--   8) 美团门店营业执照有效期开始日期（D1144-bugfix）
-- 顺序说明：各段之间无依赖，按需求编号排列；每段内部 DDL 先于依赖它的 DML。
-- 幂等：整份脚本可重复执行。
--       config 类 DML 用 NOT EXISTS 保护；authority / admin_role / admin_user 用 NOT LIKE、
--       JSON_CONTAINS 守卫；加列类 DDL 因 MySQL 不支持 ADD COLUMN IF NOT EXISTS，
--       统一用 information_schema 逐列判定 + PREPARE/EXECUTE 动态执行，已存在则走 SELECT 1。
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- =============================================================

-- -------------------------------------------------------------
-- 1) 售后流程优化：新增买家寄回拒收原因（原 database/D1120-aftersales-process-optimization.sql）
-- -------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'aftersales'
      AND COLUMN_NAME = 'close_type'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `aftersales` ADD COLUMN `close_type` tinyint NOT NULL DEFAULT ''0'' COMMENT ''关闭/拒绝子类型：0 无，1 商家审核驳回，2 商家或供应商拒收''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -------------------------------------------------------------
-- 2) 美团配送（原 database/D1125-meituanDelivery.sql）
--    仅 O2O 版本可购（VORT 子开关 o2o.meituanDelivery，默认关）；
--    非 O2O 版本不含相关 Java 代码，但表/列在库中存在无副作用
--    （delivery_order.carrier_type 恒为自建=1）。
-- -------------------------------------------------------------

-- 2A. 门店美团配送配置表（每门店一行；平台持密钥，门店在此登记并注册到美团）
CREATE TABLE IF NOT EXISTS `shop_meituan_delivery_config` (
  `shop_meituan_delivery_config_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `shop_id`               int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '门店ID',
  `enabled`               tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否开启美团配送 0关闭 1开启',
  `mt_shop_id`            varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '美团门店ID（本系统生成，注册到美团的 shop_id）',
  `mt_shop_status`        tinyint(2) NOT NULL DEFAULT 0 COMMENT '美团门店状态 0未注册 10创建审核驳回 20创建审核通过 30创建成功 40上线可发单 50修改审核驳回 60修改审核通过',
  `goods_category`        int(11) NULL DEFAULT NULL COMMENT '门店经营一级品类（美团 category 代码，如 110）',
  `second_category`       int(11) NULL DEFAULT NULL COMMENT '门店经营二级品类（美团 second_category 代码，如 110002）',
  `delivery_service_codes` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '可用配送服务代码，逗号分隔（飞速达4002/快速达4011/及时达4012/集中送4013 等）',
  `pay_type`              tinyint(1) NOT NULL DEFAULT 1 COMMENT '支付方式 1账期支付 2余额支付',
  `trigger_mode`          tinyint(1) NOT NULL DEFAULT 1 COMMENT '触发模式 1手动呼叫 2付款后自动呼叫',
  `shop_name`             varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '美团门店名称（默认取 shop.shop_title，可覆盖）',
  `contact_name`          varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone`         varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `city_name`             varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '城市名称',
  `address`               varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '门店详细地址（默认取 shop.shop_detailed_address，可覆盖）',
  `longitude`             decimal(10, 6) NULL DEFAULT NULL COMMENT '门店经度',
  `latitude`              decimal(10, 6) NULL DEFAULT NULL COMMENT '门店纬度',
  `business_hours`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业时间（文本或 JSON）',
  -- 资质信息（美团门店注册审核材料）
  `enterprise_name`       varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '企业全称',
  `enterprise_address`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '企业地址',
  `business_license_no`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业执照编号',
  `license_long_term`     tinyint(1) NOT NULL DEFAULT 0 COMMENT '营业执照是否长期有效 0否 1是',
  `license_valid_to`      varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '营业执照有效期截止（long_term=1 时可空）',
  `establish_date`        varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '注册/成立日期',
  `license_authority`     varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发证登记机关',
  `license_approval_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '核准日期',
  `business_scope`        varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '经营范围',
  -- 法人/经营者信息
  `legal_person_name`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '法人/经营者姓名',
  `legal_person_id_card`  varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '法人/经营者身份证号',
  `legal_person_phone`    varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '法人/经营者手机号',
  `audit_remark`          varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '美团审核备注 / 驳回原因',
  `add_time`              int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '添加时间',
  `update_time`           int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
  PRIMARY KEY (`shop_meituan_delivery_config_id`) USING BTREE,
  UNIQUE INDEX `uk_shop_id`(`shop_id`) USING BTREE,
  INDEX `idx_mt_shop_id`(`mt_shop_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='门店美团配送配置' ROW_FORMAT=Dynamic;

-- 2B. delivery_order 增加美团运力字段
--     逐列判定：部分列已存在时只补缺失的列，整段可重复执行。
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'delivery_order' AND COLUMN_NAME = 'carrier_type'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `delivery_order` ADD COLUMN `carrier_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''运力类型 1自建配送 2美团配送''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'delivery_order' AND COLUMN_NAME = 'mt_delivery_id'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `delivery_order` ADD COLUMN `mt_delivery_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT ''合作方配送单号（发给美团的 delivery_id，本系统唯一）''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'delivery_order' AND COLUMN_NAME = 'mt_peisong_id'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `delivery_order` ADD COLUMN `mt_peisong_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT ''美团配送单号（美团返回的 mt_peisong_id）''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'delivery_order' AND COLUMN_NAME = 'mt_status'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `delivery_order` ADD COLUMN `mt_status` smallint(4) NULL DEFAULT NULL COMMENT ''美团订单状态 0待调度 20已接单 30已取货 50已送达 99已取消''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'delivery_order' AND COLUMN_NAME = 'mt_delivery_service_code'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `delivery_order` ADD COLUMN `mt_delivery_service_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT ''美团配送服务代码（飞速达/快速达/及时达等）''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'delivery_order' AND COLUMN_NAME = 'mt_cancel_code'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `delivery_order` ADD COLUMN `mt_cancel_code` int(11) NULL DEFAULT NULL COMMENT ''美团取消/异常代码''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'delivery_order' AND COLUMN_NAME = 'mt_distance'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `delivery_order` ADD COLUMN `mt_distance` int(11) NULL DEFAULT NULL COMMENT ''美团配送距离（米）''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'delivery_order' AND COLUMN_NAME = 'mt_fee'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `delivery_order` ADD COLUMN `mt_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT ''美团配送费（元）''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2C. 平台级配置键：美团配送总开关（appKey/appSecret/apiUrl 由后台按需写入不预置）
INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'meituanDeliveryEnabled', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'meituanDeliveryEnabled' AND `is_del` = 0);

-- 2D. 平台级同城配送总控配置键（默认：同城开、商家自配送开、第三方关；复用现有同城配送菜单）
INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'intraCityDeliveryEnabled', '1', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'intraCityDeliveryEnabled' AND `is_del` = 0);

INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'intraCitySelfDeliveryEnabled', '1', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'intraCitySelfDeliveryEnabled' AND `is_del` = 0);

INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'intraCityThirdPartyDeliveryEnabled', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'intraCityThirdPartyDeliveryEnabled' AND `is_del` = 0);

-- -------------------------------------------------------------
-- 3) 小程序头像图片鉴黄鉴违开关（原 database/D1141-imgSecCheckAudit.sql）
--    控制小程序端（X-Client-Type=miniProgram）上传头像时是否调用微信内容安全
--    接口（img_sec_check）：0-关闭（默认，兼容原有行为）；1-开启。
--    开关位于后台「商城设置 -> 小程序设置」页。
-- -------------------------------------------------------------
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'miniImgSecCheck', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `config` WHERE `biz_code` = 'miniImgSecCheck' AND `is_del` = 0
);

-- -------------------------------------------------------------
-- 4) 商品评价开关 / 匿名评价（原 database/D1146-openCloseEvaluateSwitch.sql）
-- -------------------------------------------------------------

-- 4A. comment 表新增匿名标记
--     只影响新提交的评价；存量数据默认 0（实名），展示行为与改动前一致。
--     注意：本列一旦承载真实匿名数据，删列会使原本匿名的评价回退为显示真名，
--     属隐私回退，回滚需谨慎。
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'comment'
      AND COLUMN_NAME = 'is_anonymous'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `comment` ADD COLUMN `is_anonymous` tinyint unsigned NOT NULL DEFAULT ''0'' COMMENT ''是否匿名评价；0-否，1-是'' AFTER `username`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4B. 商品评价设置 config 初始化
--     默认值取自 com.tigshop.bean.enums.setting.SettingsEnum。
--     两项默认均为 1-开启，保证升级后行为与改动前一致（评价功能不因升级而消失）。
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'productCommentEnabled', '1', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'productCommentEnabled' AND `is_del` = 0);

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'anonymousCommentEnabled', '1', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'anonymousCommentEnabled' AND `is_del` = 0);

-- -------------------------------------------------------------
-- 5) 微信小程序分享是否需要登录（原 database/D1148-share-need-login.sql）
--    新增登录设置配置项 wechatShareNeedLogin，默认 0（不需要登录）。
--    默认值与 com.tigshop.bean.enums.setting.SettingsEnum 保持一致。
-- -------------------------------------------------------------
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'wechatShareNeedLogin', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'wechatShareNeedLogin' AND t.`is_del` = 0);

-- -------------------------------------------------------------
-- 6) 供应商售后审核开关 + 权限补齐（原 database/D1144-bugfix.sql）
-- -------------------------------------------------------------

-- 6A. vendor 表新增「售后审核」开关列
--     默认 0（关闭），与存量供应商既有行为一致：关闭时供应商审核环节自动通过，
--     开启时必须供应商人工审核。
--     VORT 范围：s2b2c（vendor feature）；关闭 vendor 的版本不会用到该列，加列无副作用。
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'vendor'
      AND COLUMN_NAME = 'after_sales_review'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE `vendor` ADD COLUMN `after_sales_review` tinyint DEFAULT ''0'' COMMENT ''售后审核 0关闭 1开启'' AFTER `status`',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6B. 恢复退款申请子权限 refundApplyUpdateManage
--     RefundApplyController 审核/线下确认/更新接口校验 @PreAuthorize('refundApplyUpdateManage')，
--     但 authority.child_auth 在 2.1.1→2.2.0 被置空，存量库无法勾选该权限码，非 all 账号 403。
UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"refundApplyUpdateManage\"}]'
WHERE `authority_sn` = 'refundApplyManage'
  AND `admin_type` = 'admin'
  AND (
        `child_auth` IS NULL
     OR `child_auth` = ''
     OR `child_auth` = '[]'
     OR `child_auth` NOT LIKE '%refundApplyUpdateManage%'
  );

-- 6C. 权限传播：已有 refundApplyManage 的平台角色/账号补上编辑子权限
--     （["all"] 角色天然全量，跳过；已含该 sn 的跳过）
UPDATE `admin_role`
SET `authority_list` = JSON_ARRAY_APPEND(`authority_list`, '$', 'refundApplyUpdateManage')
WHERE `admin_type` = 'admin'
  AND JSON_VALID(`authority_list`)
  AND JSON_CONTAINS(`authority_list`, '"refundApplyManage"')
  AND NOT JSON_CONTAINS(`authority_list`, '"all"')
  AND NOT JSON_CONTAINS(`authority_list`, '"refundApplyUpdateManage"');

UPDATE `admin_user`
SET `auth_list` = JSON_ARRAY_APPEND(`auth_list`, '$', 'refundApplyUpdateManage')
WHERE JSON_VALID(`auth_list`)
  AND JSON_CONTAINS(`auth_list`, '"refundApplyManage"')
  AND NOT JSON_CONTAINS(`auth_list`, '"all"')
  AND NOT JSON_CONTAINS(`auth_list`, '"refundApplyUpdateManage"');

-- 6D. 会员等级设置（非 Pro）→ 编辑（userRankModifyManage）
--     UserRankController 原类级校验 'userRankManage' 在 authority 表无此 SN，已改为真实菜单 SN；
--     非 Pro 菜单 levelManageManage 的 child_auth 为空，此处补齐（Pro 侧已在 5.8.13→5.8.14 补过）。
UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"userRankModifyManage\"}]'
WHERE `authority_sn` = 'levelManageManage'
  AND `admin_type` = 'admin'
  AND (
        `child_auth` IS NULL
     OR `child_auth` = ''
     OR `child_auth` = '[]'
     OR `child_auth` NOT LIKE '%userRankModifyManage%'
  );

-- -------------------------------------------------------------
-- 7) 易宝支付（YOP）接入（原 database/D1150-yibaoPay.sql）
--    默认值取自 com.tigshop.bean.enums.setting.SettingsEnum：
--    useYeepay 默认 '0'（关闭），其余为空，升级后行为与改动前一致。
-- -------------------------------------------------------------

-- 7A. 是否启用易宝支付（0-关闭，1-开启；开启后微信/支付宝均可用）
INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'useYeepay', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'useYeepay');

-- 7B. 易宝应用 AppKey
INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'yeepayAppKey', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'yeepayAppKey');

-- 7C. 易宝发起方商户编号（平台/服务商 parentMerchantNo）
INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'yeepayParentMerchantNo', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'yeepayParentMerchantNo');

-- 7D. 易宝收款商户编号（merchantNo）
INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'yeepayMerchantNo', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'yeepayMerchantNo');

-- 7E. 易宝商户 RSA 私钥（加签/信封解密用）
INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'yeepayIsvPrivateKey', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'yeepayIsvPrivateKey');

-- 7F. 易宝网关地址（留空用 SDK 默认生产地址；沙箱联调时填沙箱地址）
INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'yeepayServerRoot', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `config`) AS t WHERE t.`biz_code` = 'yeepayServerRoot');

-- 7G. paymentOrder 追加 yeepay 父级（yeepay 为父级，代码内映射 yeepayWechat/yeepayAlipay）
UPDATE `config`
SET `biz_val` = CONCAT(`biz_val`, ',yeepay'), `update_time` = UNIX_TIMESTAMP()
WHERE `biz_code` = 'paymentOrder'
  AND `biz_val` IS NOT NULL
  AND `biz_val` <> ''
  AND `biz_val` NOT LIKE '%yeepay%';

-- -------------------------------------------------------------
-- 8) 美团门店营业执照有效期开始日期（原 database/D1144-bugfix.sql）
--    仅在 Tigshop 本地保存，不提交至美团；兼容重复执行。
-- -------------------------------------------------------------
SET @license_valid_from_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'shop_meituan_delivery_config'
      AND COLUMN_NAME = 'license_valid_from'
);

SET @license_valid_from_sql := IF(
    @license_valid_from_exists = 0,
    'ALTER TABLE `shop_meituan_delivery_config` ADD COLUMN `license_valid_from` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT ''营业执照有效期开始（yyyy-MM-dd）'' AFTER `license_long_term`',
    'SELECT ''shop_meituan_delivery_config.license_valid_from already exists'''
);

PREPARE license_valid_from_stmt FROM @license_valid_from_sql;
EXECUTE license_valid_from_stmt;
DEALLOCATE PREPARE license_valid_from_stmt;
