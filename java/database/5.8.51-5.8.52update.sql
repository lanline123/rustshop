-- =============================================================
-- 5.8.51 -> 5.8.52 增量脚本
-- 内容：D1143 天地图、D1151 语言域名映射、D1152 买家秀视频流、D1163 Bug 修复、D1164 店铺门店商品咨询
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- =============================================================

-- ============================================================================
-- D1143-tianditu — 天地图 Provider 接入与多地图自由切换
-- ============================================================================
-- 需求基线：.ai-os/baseline-log/BL-20260729-1553-tianditu-map-provider-align.md
-- 设计基线：.ai-os/baseline-log/BL-20260730-1021-tianditu-design-confirmed.md
--
-- 本脚本全部语句幂等，可重复执行。
-- 无 DDL，仅新增 config 初始化数据。
-- 默认值取自 com.tigshop.bean.enums.setting.SettingsEnum。
-- ============================================================================

-- 地图服务商开关（默认 amap-高德地图，保证存量行为零变化）
-- 取值：amap-高德地图  tianditu-天地图
-- 说明：ConfigServiceImpl.getConfigVal 在 config 表无该行时返回空串（不读枚举默认值），
--       Router 对空串同样回落 amap；此处显式落库是为了让后台设置页与运维能读到明确当前值。
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'mapProvider', 'amap', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'mapProvider' AND `is_del` = 0);

-- 天地图两套 tk：预置空串占位行（与 twitterClientId / applePayMerchantId 范式一致）。
-- 天地图控制台只有「浏览器端 / 服务器端 / 安卓端」三种应用类型，没有小程序类型，
-- 小程序场景走后端中转复用 tiandituWebTk，因此不设小程序 tk 配置项。
-- 不预置真实密钥；管理员在后台「地图设置」填入后由 saveMapSettings 覆盖写入。
-- 预置空串的收益：SettingsHelper.getRaw 不会因无行回退成 "0" 再被 encryptData 掩码成 "*"，
-- 后台输入框初始为空，requireTk() 对空串仍判定为未配置。
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'tiandituWebTk', '', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'tiandituWebTk' AND `is_del` = 0);

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'tiandituJsTk', '', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'tiandituJsTk' AND `is_del` = 0);

-- 清理本分支早期版本预置过的 tiandituMiniTk 占位行。
-- 该项已随「天地图无小程序应用类型」的结论下线，代码侧已无枚举与消费方，留在库里是孤儿行。
-- 仅本分支未发布期间产生，不涉及任何线上真实凭据；语句幂等，重复执行无副作用。
DELETE FROM `config` WHERE `biz_code` = 'tiandituMiniTk';

-- ============================================================
-- D1151 语言域名映射
-- 说明：MySQL 不支持 ADD COLUMN IF NOT EXISTS，以下 DDL 仅执行一次
-- ============================================================

ALTER TABLE `locales`
    ADD COLUMN `domain` varchar(255) NOT NULL DEFAULT '' COMMENT '该语言对应的访问域名，如 en.domain.com，不含协议与端口，为空表示不启用域名跳转' AFTER `language`;

-- ##########################################################################
-- D1152 买家秀视频流（类抖音）
-- 说明：
--   · 对齐 vortmall-api D0089-buyerShowVideoFeed 的功能口径。
--   · 全脚本幂等，可重复执行。
-- ##########################################################################

-- ============================================================
-- buyer_show_video 新增 sort_order 字段
-- 用途：图片与视频按 sort_order 混排，第一条是视频则该买家秀判定为「视频类」，
--       进入视频流；平局时视频优先。与 buyer_show_gallery.sort_order 同一量纲。
-- ============================================================
SET @col_video_sort_order := (SELECT COUNT(*) FROM information_schema.COLUMNS
                              WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'buyer_show_video' AND COLUMN_NAME = 'sort_order');
SET @sql_video_sort_order := IF(@col_video_sort_order = 0,
    'ALTER TABLE `buyer_show_video` ADD COLUMN `sort_order` int unsigned NOT NULL DEFAULT 0 COMMENT ''排序'' AFTER `format`',
    'SELECT 1');
PREPARE stmt FROM @sql_video_sort_order;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 买家秀收藏表：buyer_show_favorite
-- 收藏数不冗余到主表，与点赞数一致按本表实时统计。
-- ============================================================
CREATE TABLE IF NOT EXISTS `buyer_show_favorite` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `buyer_show_id` int unsigned NOT NULL COMMENT '买家秀ID',
  `user_id` int unsigned NOT NULL COMMENT '用户ID',
  `add_time` bigint unsigned NOT NULL DEFAULT '0' COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_buyer_show_user` (`buyer_show_id`,`user_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_buyer_show` (`buyer_show_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='买家秀收藏表';

-- ============================================================
-- 用户关注关系表：user_follow（关注者 user_id -> 被关注作者 author_id）
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_follow` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int unsigned NOT NULL COMMENT '关注者用户ID',
  `author_id` int unsigned NOT NULL COMMENT '被关注作者用户ID',
  `add_time` bigint unsigned NOT NULL DEFAULT '0' COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_author` (`user_id`,`author_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_author` (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户关注关系表';

-- ============================================================
-- 脚本结束
-- ============================================================

-- D1163 后台操作日志按操作终端作用域隔离（仅执行一次）
ALTER TABLE `admin_log`
    ADD COLUMN `scope_type` varchar(16) NOT NULL DEFAULT 'legacy' COMMENT '操作作用域：admin-平台，shop-店铺，vendor-供应商，legacy-历史未标注' AFTER `user_id`,
    ADD COLUMN `scope_id` int unsigned NOT NULL DEFAULT '0' COMMENT '作用域ID：平台/历史为0，店铺为shop_id，供应商为vendor_id' AFTER `scope_type`,
    ADD KEY `idx_scope_type_id_time` (`scope_type`, `scope_id`, `log_time`);

-- 店铺客服配置按店铺隔离（仅执行一次）
ALTER TABLE `shop_kefu_config`
    ADD COLUMN `kefu_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '客服选择:0无客服 1腾讯云客服 2企业微信客服 3自定义客服 4TigChart客服' AFTER `shop_id`,
    ADD COLUMN `kefu_yzf_type` tinyint(1) NOT NULL DEFAULT 0 COMMENT '腾讯云客服打开方式:0链接 1弹窗' AFTER `kefu_type`,
    ADD COLUMN `kefu_yzf_sign` varchar(255) NOT NULL DEFAULT '' COMMENT '腾讯云客服sign' AFTER `kefu_yzf_type`,
    ADD COLUMN `kefu_workwx_id` varchar(255) NOT NULL DEFAULT '' COMMENT '企业微信客服ID' AFTER `kefu_yzf_sign`,
    ADD COLUMN `corp_id` varchar(255) NOT NULL DEFAULT '' COMMENT '企业微信企业ID' AFTER `kefu_workwx_id`,
    ADD COLUMN `kefu_code` varchar(500) NOT NULL DEFAULT '' COMMENT '客服链接' AFTER `corp_id`,
    ADD COLUMN `kefu_code_blank` tinyint(1) NOT NULL DEFAULT 0 COMMENT '客服链接打开方式:0链接 1弹窗' AFTER `kefu_code`,
    ADD COLUMN `kefu_whatsapp_phone` varchar(50) NOT NULL DEFAULT '' COMMENT 'WhatsApp客服号码' AFTER `kefu_code_blank`,
    ADD COLUMN `kefu_phone` varchar(50) NOT NULL DEFAULT '' COMMENT '客服热线电话' AFTER `kefu_whatsapp_phone`,
    ADD COLUMN `kefu_time` varchar(100) NOT NULL DEFAULT '' COMMENT '服务时间' AFTER `kefu_phone`;

-- D1164 会员留言下放店铺/门店
-- authority DML 可重复执行；以下索引 DDL 仅执行一次。

INSERT INTO `authority`
    (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`,
     `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT
    'feedbackManage', '商品咨询', parent.`authority_id`, 30, 1,
    '[{"auth_name":"编辑","auth_sn":"feedbackModifyManage"}]',
    'user/feedback/list/', '', 0, parent.`admin_type`
FROM `authority` parent
WHERE parent.`authority_sn` = 'userManagement'
  AND parent.`admin_type` IN ('shop', 'store')
  AND NOT EXISTS (
      SELECT 1
      FROM `authority` existing
      WHERE existing.`authority_sn` = 'feedbackManage'
        AND existing.`admin_type` = parent.`admin_type`
  );

-- 已存在的店铺/门店菜单同步改名；平台端会员留言菜单保持不变。
UPDATE `authority`
SET `authority_name` = '商品咨询'
WHERE `authority_sn` = 'feedbackManage'
  AND `admin_type` IN ('shop', 'store');

-- 历史店铺自有商品咨询可由商品权威归属唯一确定；O2O 平台商品的历史门店无法可靠反推，保持原值。
UPDATE `feedback` feedback
INNER JOIN `product` product ON product.`product_id` = feedback.`product_id`
SET feedback.`shop_id` = product.`shop_id`
WHERE feedback.`parent_id` = 0
  AND feedback.`type` = 2
  AND (feedback.`shop_id` IS NULL OR feedback.`shop_id` = 0)
  AND product.`shop_id` > 0;

-- 历史回复继承父留言的租户及业务上下文；重复执行结果不变。
UPDATE `feedback` reply
INNER JOIN `feedback` parent ON parent.`id` = reply.`parent_id`
SET reply.`shop_id` = parent.`shop_id`,
    reply.`type` = parent.`type`,
    reply.`product_id` = parent.`product_id`,
    reply.`order_id` = parent.`order_id`
WHERE reply.`parent_id` > 0
  AND (
      NOT (reply.`shop_id` <=> parent.`shop_id`)
      OR NOT (reply.`type` <=> parent.`type`)
      OR NOT (reply.`product_id` <=> parent.`product_id`)
      OR NOT (reply.`order_id` <=> parent.`order_id`)
  );

ALTER TABLE `feedback`
    ADD INDEX `idx_feedback_shop_type_parent_id` (`shop_id`, `type`, `parent_id`, `id`),
    ADD INDEX `idx_feedback_product_shop_parent_type` (`product_id`, `shop_id`, `parent_id`, `type`);

-- D1163-bugfix: 统一 store_product.add_time 为 Unix 秒级时间戳
-- 幂等：仅处理 13 位毫秒级时间戳，执行后值会降到秒级阈值以下。
UPDATE store_product
SET add_time = add_time DIV 1000
WHERE add_time >= 100000000000;

-- 供应商结算以订单维度幂等：同一订单最多生成一条供应商结算单。
-- 该迁移不会清理历史数据；若现有表存在重复 order_id，需先人工处理后再执行。
SET @vendor_settlement_order_unique_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'vendor_settlement_order'
      AND index_name = 'uk_vendor_settlement_order_order_id'
);
SET @vendor_settlement_order_unique_index_sql := IF(
    @vendor_settlement_order_unique_index_exists = 0,
    'ALTER TABLE `vendor_settlement_order` ADD UNIQUE KEY `uk_vendor_settlement_order_order_id` (`order_id`)',
    'SELECT 1'
);
PREPARE vendor_settlement_order_unique_index_statement FROM @vendor_settlement_order_unique_index_sql;
EXECUTE vendor_settlement_order_unique_index_statement;
DEALLOCATE PREPARE vendor_settlement_order_unique_index_statement;

-- ==========================================================================
-- D1166-crossBorderAddressAdapt — 跨境收货地址结构化字段
-- ==========================================================================
-- 仅新增结构和中国大陆根节点标识，不执行海外国家/地区种子导入。
-- 以下 DDL 通过 information_schema 判断字段是否存在，可重复执行。

SET @d1166_schema_name := DATABASE();

SET @d1166_column_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @d1166_schema_name AND TABLE_NAME = 'region' AND COLUMN_NAME = 'standard_code'
);
SET @d1166_sql := IF(@d1166_column_exists = 0,
    'ALTER TABLE `region` ADD COLUMN `standard_code` varchar(8) NULL COMMENT ''国家或地区稳定标准代码'' AFTER `first_word`',
    'SELECT 1');
PREPARE d1166_stmt FROM @d1166_sql;
EXECUTE d1166_stmt;
DEALLOCATE PREPARE d1166_stmt;

SET @d1166_column_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @d1166_schema_name AND TABLE_NAME = 'region' AND COLUMN_NAME = 'sort_order'
);
SET @d1166_sql := IF(@d1166_column_exists = 0,
    'ALTER TABLE `region` ADD COLUMN `sort_order` int NOT NULL DEFAULT 50 COMMENT ''排序值，数值越小越靠前'' AFTER `is_hot`',
    'SELECT 1');
PREPARE d1166_stmt FROM @d1166_sql;
EXECUTE d1166_stmt;
DEALLOCATE PREPARE d1166_stmt;

SET @d1166_sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @d1166_schema_name AND TABLE_NAME = 'user_address' AND COLUMN_NAME = 'country_code') = 0,
    'ALTER TABLE `user_address` ADD COLUMN `country_code` varchar(8) NULL COMMENT ''国家或地区标准代码'' AFTER `address_id`', 'SELECT 1');
PREPARE d1166_stmt FROM @d1166_sql; EXECUTE d1166_stmt; DEALLOCATE PREPARE d1166_stmt;
SET @d1166_sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @d1166_schema_name AND TABLE_NAME = 'user_address' AND COLUMN_NAME = 'country_name') = 0,
    'ALTER TABLE `user_address` ADD COLUMN `country_name` varchar(100) NULL COMMENT ''国家或地区名称'' AFTER `country_code`', 'SELECT 1');
PREPARE d1166_stmt FROM @d1166_sql; EXECUTE d1166_stmt; DEALLOCATE PREPARE d1166_stmt;
SET @d1166_sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @d1166_schema_name AND TABLE_NAME = 'user_address' AND COLUMN_NAME = 'international_mobile') = 0,
    'ALTER TABLE `user_address` ADD COLUMN `international_mobile` varchar(32) NULL COMMENT ''E.164 国际手机号'' AFTER `mobile_area_code`', 'SELECT 1');
PREPARE d1166_stmt FROM @d1166_sql; EXECUTE d1166_stmt; DEALLOCATE PREPARE d1166_stmt;
SET @d1166_sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @d1166_schema_name AND TABLE_NAME = 'user_address' AND COLUMN_NAME = 'address_line_two') = 0,
    'ALTER TABLE `user_address` ADD COLUMN `address_line_two` varchar(255) NULL COMMENT ''补充地址'' AFTER `address`', 'SELECT 1');
PREPARE d1166_stmt FROM @d1166_sql; EXECUTE d1166_stmt; DEALLOCATE PREPARE d1166_stmt;
SET @d1166_sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @d1166_schema_name AND TABLE_NAME = 'user_address' AND COLUMN_NAME = 'building_name') = 0,
    'ALTER TABLE `user_address` ADD COLUMN `building_name` varchar(100) NULL COMMENT ''建筑名称'' AFTER `address_line_two`', 'SELECT 1');
PREPARE d1166_stmt FROM @d1166_sql; EXECUTE d1166_stmt; DEALLOCATE PREPARE d1166_stmt;
SET @d1166_sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @d1166_schema_name AND TABLE_NAME = 'user_address' AND COLUMN_NAME = 'floor') = 0,
    'ALTER TABLE `user_address` ADD COLUMN `floor` varchar(100) NULL COMMENT ''楼层'' AFTER `building_name`', 'SELECT 1');
PREPARE d1166_stmt FROM @d1166_sql; EXECUTE d1166_stmt; DEALLOCATE PREPARE d1166_stmt;
SET @d1166_sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @d1166_schema_name AND TABLE_NAME = 'user_address' AND COLUMN_NAME = 'unit') = 0,
    'ALTER TABLE `user_address` ADD COLUMN `unit` varchar(100) NULL COMMENT ''单元'' AFTER `floor`', 'SELECT 1');
PREPARE d1166_stmt FROM @d1166_sql; EXECUTE d1166_stmt; DEALLOCATE PREPARE d1166_stmt;
SET @d1166_sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @d1166_schema_name AND TABLE_NAME = 'user_address' AND COLUMN_NAME = 'address_data') = 0,
    'ALTER TABLE `user_address` ADD COLUMN `address_data` text NULL COMMENT ''结构化地址 JSON'' AFTER `unit`', 'SELECT 1');
PREPARE d1166_stmt FROM @d1166_sql; EXECUTE d1166_stmt; DEALLOCATE PREPARE d1166_stmt;
SET @d1166_sql := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @d1166_schema_name AND TABLE_NAME = 'user_address' AND COLUMN_NAME = 'formatted_address') = 0,
    'ALTER TABLE `user_address` ADD COLUMN `formatted_address` varchar(512) NULL COMMENT ''服务端生成的完整地址'' AFTER `address_data`', 'SELECT 1');
PREPARE d1166_stmt FROM @d1166_sql; EXECUTE d1166_stmt; DEALLOCATE PREPARE d1166_stmt;

UPDATE `region`
SET `standard_code` = 'CN'
WHERE `parent_id` = 0
  AND (`region_id` = 1 OR `region_name` IN ('中国', '中国大陆'))
  AND (`standard_code` IS NULL OR TRIM(`standard_code`) = '');

SET @d1166_schema_name := NULL;
SET @d1166_column_exists := NULL;
SET @d1166_sql := NULL;
