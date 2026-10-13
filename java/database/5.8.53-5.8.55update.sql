-- =============================================================
-- 5.8.53 -> 5.8.55 增量脚本
-- 内容：D1161、D1175、D1176、D1180、D1185 需求增量
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- =============================================================

-- D1161 O2O 批发商品按门店独立配置
-- DDL 仅执行一次；执行前请备份数据库。
-- 本脚本只扩展配置作用域，不为门店自动创建或复制批发价。

ALTER TABLE `product_wholesale_price`
    ADD COLUMN `shop_id` int NOT NULL DEFAULT '0'
        COMMENT '批发配置作用域：0=平台，>0=门店'
        AFTER `product_id`;

ALTER TABLE `product_wholesale_sku_config`
    ADD COLUMN `shop_id` int NOT NULL DEFAULT '0'
        COMMENT '批发配置作用域：0=平台，>0=门店'
        AFTER `product_id`;

-- D1175：商品询价增加可选邮箱地址
ALTER TABLE `price_inquiry`
    ADD COLUMN `email` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '联系邮箱' AFTER `mobile`;

-- D1176-signin-config：签到配置支持自由增加，并按排序值、添加时间展示。
-- DDL 仅执行一次：目标库已存在新增列时不要重复执行。
ALTER TABLE `sign_in_setting`
    ADD COLUMN `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序' AFTER `day_num`,
    ADD COLUMN `create_time` int unsigned NOT NULL DEFAULT 0 COMMENT '添加时间' AFTER `sort_order`;

-- D1180：微信商户进件媒体元数据
CREATE TABLE IF NOT EXISTS `merchant_application_media` (
    `id`                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '进件媒体ID',
    `application_id`        BIGINT       NOT NULL DEFAULT 0 COMMENT '进件申请ID，0表示未绑定',
    `uploader_admin_id`     INT          NOT NULL COMMENT '上传后台账号ID',
    `applicant_type`       VARCHAR(16)  NOT NULL DEFAULT '' COMMENT '申请主体类型:SHOP/VENDOR',
    `applicant_id`         BIGINT       NOT NULL DEFAULT 0 COMMENT '申请主体ID',
    `media_role`           TINYINT      NOT NULL DEFAULT 0 COMMENT '媒体角色:0未绑定 1营业执照 2身份证人像面 3身份证国徽面',
    `initial_media_id_hash` CHAR(64)     NULL COMMENT '初始MediaID的SHA-256摘要',
    `object_name`           VARCHAR(512) NOT NULL DEFAULT '' COMMENT '私有OSS随机对象键',
    `file_ext`              VARCHAR(10)  NOT NULL DEFAULT '' COMMENT '文件扩展名',
    `content_type`          VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '文件MIME类型',
    `file_size`             BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小，单位字节',
    `status`                TINYINT      NOT NULL DEFAULT 0 COMMENT '状态:0上传中 1临时 2已绑定 3待删除 4已删除',
    `retry_count`           INT          NOT NULL DEFAULT 0 COMMENT '删除重试次数',
    `next_retry_time`       DATETIME     NULL COMMENT '下次补偿时间',
    `expire_time`           DATETIME     NULL COMMENT '当前生命周期到期时间',
    `last_error_code`       VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '脱敏错误分类',
    `is_del`                INT          NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 非0已删',
    `create_time`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_uploader_media_hash` (`uploader_admin_id`, `initial_media_id_hash`),
    KEY `idx_application_role_status` (`application_id`, `media_role`, `status`),
    KEY `idx_cleanup` (`status`, `expire_time`, `next_retry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商户进件媒体元数据';

-- D1185：收银台扫码自动加入购物车。
-- 幂等增加门店级配置列；已存在时不重复执行 DDL。
SET @d1185_add_auto_add_cart_after_scan = IF(
    (SELECT COUNT(*)
       FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'shop'
        AND COLUMN_NAME = 'auto_add_cart_after_scan') = 0,
    'ALTER TABLE `shop` ADD COLUMN `auto_add_cart_after_scan` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''扫码后自动加入购物车：0关闭 1开启'' AFTER `default_collection_method`',
    'SELECT 1'
);
PREPARE d1185_stmt FROM @d1185_add_auto_add_cart_after_scan;
EXECUTE d1185_stmt;
DEALLOCATE PREPARE d1185_stmt;

-- D1161：历史批发数据回填及索引调整
UPDATE `product_wholesale_price` AS p
INNER JOIN `product_wholesale_config` AS c
        ON c.`product_id` = p.`product_id`
SET p.`shop_id` = c.`shop_id`
WHERE c.`shop_id` > 0;

UPDATE `product_wholesale_sku_config` AS s
INNER JOIN `product_wholesale_config` AS c
        ON c.`product_id` = s.`product_id`
SET s.`shop_id` = c.`shop_id`
WHERE c.`shop_id` > 0;

ALTER TABLE `product_wholesale_config`
    DROP INDEX `uk_product_id`,
    DROP INDEX `idx_shop_id`,
    DROP INDEX `idx_status`,
    ADD UNIQUE KEY `uk_shop_product` (`shop_id`, `product_id`),
    ADD KEY `idx_shop_status` (`shop_id`, `status`, `is_delete`, `product_id`);

ALTER TABLE `product_wholesale_price`
    DROP INDEX `uk_combo`,
    DROP INDEX `idx_product_grade`,
    ADD UNIQUE KEY `uk_shop_combo`
        (`shop_id`, `product_id`, `sku_id`, `grade_id`, `tier_min_qty`, `is_delete`),
    ADD KEY `idx_shop_product_grade`
        (`shop_id`, `product_id`, `grade_id`, `status`, `is_delete`),
    ADD KEY `idx_shop_grade_status`
        (`shop_id`, `grade_id`, `status`, `is_delete`, `product_id`);

ALTER TABLE `product_wholesale_sku_config`
    DROP INDEX `uk_product_sku`,
    DROP INDEX `idx_product_id`,
    ADD UNIQUE KEY `uk_shop_product_sku` (`shop_id`, `product_id`, `sku_id`);

ALTER TABLE `store_product`
    ADD KEY `idx_shop_product_status`
        (`shop_id`, `product_id`, `product_status`, `is_delete`);

ALTER TABLE `store_product_sku`
    ADD KEY `idx_store_product_sku`
        (`store_product_id`, `product_sku_id`);

-- D1161：O2O 门店批发商品权限
INSERT INTO `authority`
    (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
     `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT
    40001118,
    'wholesaleProductManage',
    '批发商品列表',
    20000039,
    60,
    1,
    '[{"auth_name":"添加","auth_sn":"wholesaleProductAddManage"},{"auth_name":"删除","auth_sn":"wholesaleProductDeleteManage"},{"auth_name":"批发价","auth_sn":"wholesaleProductPriceManage"},{"auth_name":"起批量","auth_sn":"wholesaleProductBuyRuleManage"},{"auth_name":"倍数购买","auth_sn":"wholesaleProductMultipleManage"}]',
    'wholesale/product/list/',
    '',
    0,
    'store'
WHERE NOT EXISTS (
    SELECT 1 FROM `authority`
    WHERE `authority_id` = 40001118
       OR (`authority_sn` = 'wholesaleProductManage' AND `admin_type` = 'store')
);

UPDATE `admin_role`
SET `authority_list` = JSON_ARRAY_APPEND(`authority_list`, '$', 'wholesaleProductManage')
WHERE `admin_type` = 'store'
  AND JSON_VALID(`authority_list`)
  AND JSON_CONTAINS(`authority_list`, '"commodityManage"')
  AND NOT JSON_CONTAINS(`authority_list`, '"all"')
  AND NOT JSON_CONTAINS(`authority_list`, '"wholesaleProductManage"');

UPDATE `admin_user_shop`
SET `auth_list` = JSON_ARRAY_APPEND(`auth_list`, '$', 'wholesaleProductManage')
WHERE `role_id` <= 0
  AND JSON_VALID(`auth_list`)
  AND JSON_CONTAINS(`auth_list`, '"commodityManage"')
  AND NOT JSON_CONTAINS(`auth_list`, '"all"')
  AND NOT JSON_CONTAINS(`auth_list`, '"wholesaleProductManage"');

-- D1180：买家秀删除权限
UPDATE `authority`
SET `child_auth` = JSON_ARRAY_APPEND(
        COALESCE(NULLIF(`child_auth`, ''), JSON_ARRAY()),
        '$',
        JSON_OBJECT('authName', '删除', 'authSn', 'buyerShowDeleteManage')
    )
WHERE `authority_sn` = 'buyerShowManage'
  AND `admin_type` = 'admin'
  AND (`child_auth` IS NULL OR `child_auth` NOT LIKE '%buyerShowDeleteManage%');
