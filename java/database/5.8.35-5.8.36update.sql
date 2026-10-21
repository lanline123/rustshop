-- =============================================================
-- v5.8.35 -> v5.8.36 增量脚本
-- 内容：
--   1. D1105 工作台首页快捷功能配置表 panel_shortcut_menu
--   2. D1108 供应商库存管理（3 张 vendor 库存单据表 + vendor 权限）
--   3. D1115 商品规格图片上传范围配置 specImageMode
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- =============================================================

SET NAMES utf8mb4;

-- =============================================================
-- 1. D1105 工作台首页快捷功能配置
-- 按店铺存储商家管理员「已固定到首页的功能列表」，实现跨设备同步。
-- 联合唯一键 admin_id + shop_id，整体覆盖保存。
-- =============================================================
CREATE TABLE IF NOT EXISTS `panel_shortcut_menu` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `admin_id` int(11) NOT NULL DEFAULT '0' COMMENT '商家管理员ID（取自 token）',
    `shop_id` int(11) NOT NULL DEFAULT '0' COMMENT '店铺ID（取自请求头 X-Shop-Id）',
    `menu_keys` text COMMENT '[json]有序的功能 key 字符串数组，数组顺序=首页展示顺序',
    `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间（秒级时间戳）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_shop` (`admin_id`, `shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作台首页快捷功能配置';

-- =============================================================
-- 2. D1108 供应商库存管理
-- 3 张 vendor 作用域库存单据表（镜像店铺端 product_stock_record* / product_stock_adjust_record）；
-- 库存日志复用现有 vendor_product_sku_stock_log，不新建表。
-- =============================================================

-- 2.1 供应商商品出入库记录（单头）
CREATE TABLE `vendor_product_stock_record`
(
    `product_stock_id` int         NOT NULL AUTO_INCREMENT,
    `stock_code`       varchar(19) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '单据编号',
    `in_or_out`        tinyint     NOT NULL DEFAULT '0' COMMENT '类型（1=入库，2=出库）',
    `type`             int                  DEFAULT NULL COMMENT '11=采购入库，12=退货入库，10=其他入库，21=过期出库，22=订单出库，23=其他出库',
    `add_time`         int                  DEFAULT NULL COMMENT '创建时间',
    `add_by`           varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '经办人',
    `comment`          text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '备注',
    `vendor_id`        int                  DEFAULT NULL COMMENT '供应商id',
    PRIMARY KEY (`product_stock_id`) USING BTREE,
    KEY                `idx_vendor_id` (`vendor_id`) USING BTREE,
    KEY                `idx_stock_code` (`stock_code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='供应商商品出入库记录';

-- 2.2 供应商商品出入库记录明细
CREATE TABLE `vendor_product_stock_record_detail`
(
    `record_detail_id`      int    NOT NULL AUTO_INCREMENT COMMENT '主键',
    `stock_record_id`       int             DEFAULT NULL COMMENT '出入库单id',
    `vendor_product_id`     int             DEFAULT NULL COMMENT '供应商商品id（vendor_product.id）',
    `vendor_product_sku_id` bigint          DEFAULT NULL COMMENT '供应商商品规格id（vendor_product_sku.id）',
    `quantity`              int unsigned    DEFAULT '0' COMMENT '出入库数量',
    PRIMARY KEY (`record_detail_id`) USING BTREE,
    KEY                     `idx_stock_record_id` (`stock_record_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='供应商商品出入库记录明细';

-- 2.3 供应商商品库存调整记录（单表，无明细）
CREATE TABLE `vendor_product_stock_adjust_record`
(
    `adjust_record_id`       int    NOT NULL AUTO_INCREMENT COMMENT '主键',
    `vendor_product_id`      int             DEFAULT NULL COMMENT '供应商商品id（vendor_product.id）',
    `vendor_product_sku_id`  bigint          DEFAULT NULL COMMENT '供应商商品规格id（vendor_product_sku.id）',
    `adjust_type`            tinyint         DEFAULT NULL COMMENT '调整类型（1=增量更新，0=覆盖更新）',
    `quantity`               int             DEFAULT '0' COMMENT '调整数量',
    `change_before_quantity` int unsigned    DEFAULT '0' COMMENT '调整前库存',
    `change_after_quantity`  int unsigned    DEFAULT '0' COMMENT '调整后库存',
    `add_by`                 varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作账号',
    `add_time`               bigint          DEFAULT NULL COMMENT '创建时间',
    `vendor_id`              int             DEFAULT NULL COMMENT '供应商id',
    PRIMARY KEY (`adjust_record_id`) USING BTREE,
    KEY                      `idx_vendor_id` (`vendor_id`) USING BTREE,
    KEY                      `idx_vendor_product_sku_id` (`vendor_product_sku_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='供应商商品库存调整记录';

-- 2.4 权限 authority（admin_type = 'vendor'）
-- 菜单组「库存管理」+ 五个子菜单（库存列表 / 商品入库 / 商品出库 / 库存调整 / 库存日志）
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (11000050, 'vendorStockManage', '库存管理', 11000000, 5, 1, '[]', '', 'iconfont-tig icon-kucunguanli', 0, 'vendor');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (11000051, 'vendorProductStockManage', '库存列表', 11000050, 50, 1, '[]', '', '', 0, 'vendor');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (11000054, 'vendorProductStockInManage', '商品入库', 11000050, 51, 1, '[{"authName":"入库","authSn":"vendorProductStockInUpdateManage"}]', '', '', 0, 'vendor');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (11000055, 'vendorProductStockOutManage', '商品出库', 11000050, 52, 1, '[{"authName":"出库","authSn":"vendorProductStockOutUpdateManage"}]', '', '', 0, 'vendor');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (11000052, 'vendorProductStockAdjustManage', '库存调整', 11000050, 53, 1, '[{"authName":"调整","authSn":"vendorProductStockAdjustUpdateManage"}]', '', '', 0, 'vendor');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (11000053, 'vendorProductInventoryLogManage', '库存日志', 11000050, 54, 1, '[]', '', '', 0, 'vendor');

-- =============================================================
-- 3. D1115 商品规格图片上传范围配置
-- specImageMode: 1-仅第一组规格可上传图片，2-全部规格可上传图片；默认 1
-- 幂等：INSERT ... SELECT ... WHERE NOT EXISTS，重复执行不重复插入
-- =============================================================
INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'specImageMode', '1', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'specImageMode' AND `is_del` = 0);


-- 1. 新增图片本地化映射表
CREATE TABLE IF NOT EXISTS `image_i18n` (
                                            `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                                            `locale_id`     INT          NOT NULL COMMENT '语言id',
                                            `data_type`     TINYINT      NOT NULL COMMENT '数据类型：7=商品图片 8=规格图片',
                                            `original_key`  CHAR(32)     NOT NULL COMMENT '原图URL的MD5',
    `original_url`  VARCHAR(512) NOT NULL COMMENT '原图URL',
    `localized_url` VARCHAR(512) NOT NULL COMMENT '本地化后的图URL',
    `add_time`      INT          DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_locale_type_key` (`locale_id`, `data_type`, `original_key`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='海外站图片本地化映射';

-- 2. 备份本次迁移涉及的源数据（translations_data 中的图片行）
--    说明：本次迁移是纯新增——只往 image_i18n 插数据，从不修改/删除 translations_data，
--          源表本身不会被破坏；此快照为额外保险，便于审计与极端情况下的人工核对/还原。
--    幂等：CREATE TABLE IF NOT EXISTS ... AS SELECT，备份表已存在则整体跳过、不重复写入。
--    回滚后清理：DROP TABLE IF EXISTS `translations_data_img_bak_5834`;
CREATE TABLE IF NOT EXISTS `translations_data_img_bak_5834` AS
SELECT * FROM `translations_data`
WHERE `translation_value` IS NOT NULL
  AND `translation_value` <> ''
  AND (
    `data_type` = 7
        OR (
        `data_type` = 8
            AND (
            `translation_name` LIKE 'http%'
                OR `translation_name` LIKE 'img/%'
                OR `translation_name` LIKE '%!!pic%'
                OR `translation_name` LIKE '%.jpg'
                OR `translation_name` LIKE '%.jpeg'
                OR `translation_name` LIKE '%.png'
                OR `translation_name` LIKE '%.webp'
                OR `translation_name` LIKE '%.gif'
            )
        )
    );

-- 3. 迁移历史图片译文（保留 translations_data 原行）
--    data_type=7 全量迁移；data_type=8 仅迁移"图片行"（translation_name 形如 URL/路径），
--    规格文本行保持在 translations_data 不动。
--    original_key 取 MD5(translation_name) 而非历史 translation_key：
--    新代码 ImageI18nService.resolve 用 MD5(运行时图片URL) 查表，运行时 URL 即 translation_name；
--    历史上有少量行（如带 !!pic 后缀的图）translation_key 与 MD5(translation_name) 不一致，
--    若照拷 translation_key 会导致这些图迁移后查不到、回退原图，故统一按 MD5(translation_name) 计算。
INSERT INTO `image_i18n` (`locale_id`, `data_type`, `original_key`, `original_url`, `localized_url`, `add_time`)
SELECT `locale_id`, `data_type`, MD5(`translation_name`), `translation_name`, `translation_value`, UNIX_TIMESTAMP()
FROM `translations_data`
WHERE `translation_value` IS NOT NULL
  AND `translation_value` <> ''
  AND (
    `data_type` = 7
        OR (
        `data_type` = 8
            AND (
            `translation_name` LIKE 'http%'
                OR `translation_name` LIKE 'img/%'
                OR `translation_name` LIKE '%!!pic%'
                OR `translation_name` LIKE '%.jpg'
                OR `translation_name` LIKE '%.jpeg'
                OR `translation_name` LIKE '%.png'
                OR `translation_name` LIKE '%.webp'
                OR `translation_name` LIKE '%.gif'
            )
        )
    )
    ON DUPLICATE KEY UPDATE `localized_url` = VALUES(`localized_url`);
