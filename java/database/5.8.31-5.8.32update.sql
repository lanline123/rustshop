-- ============================================================
-- AI 图片翻译语言配置表（蜂鸟AI 图片翻译/消除）
-- 一语言一行 + is_source / is_target 双开关；
-- 运行期由 AiImageServiceImpl 组装翻译源语言列表与可用目标语言。
-- 幂等：CREATE TABLE IF NOT EXISTS + INSERT IGNORE
-- ============================================================

CREATE TABLE IF NOT EXISTS `ai_translate_language` (
                                                       `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                       `lang_code`   VARCHAR(20)  NOT NULL COMMENT '语言代码，如 zh / en / zh-tw',
    `lang_name`   VARCHAR(50)  NOT NULL COMMENT '语言显示名称',
    `is_source`   TINYINT      NOT NULL DEFAULT 1 COMMENT '是否可作为翻译源：0否 1是',
    `is_target`   TINYINT      NOT NULL DEFAULT 1 COMMENT '是否可作为翻译目标：0否 1是',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序号（升序）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_lang_code` (`lang_code`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI图片翻译语言配置';

-- 初始化 33 种语言（仅作为目标的语言：zh-tw, fil, he, uk, bn, cs, km, kk, ur）
INSERT IGNORE INTO `ai_translate_language`
    (`lang_code`, `lang_name`, `is_source`, `is_target`, `sort`)
VALUES
    ('zh',    '简体中文',    1, 1, 1),
    ('zh-tw', '繁体中文',    0, 1, 2),
    ('en',    '英语',        1, 1, 3),
    ('ja',    '日语',        1, 1, 4),
    ('ko',    '韩语',        1, 1, 5),
    ('th',    '泰语',        1, 1, 6),
    ('ms',    '马来西亚语',  1, 1, 7),
    ('vi',    '越南语',      1, 1, 8),
    ('id',    '印尼语',      1, 1, 9),
    ('fil',   '菲律宾语',    0, 1, 10),
    ('ar',    '阿拉伯语',    1, 1, 11),
    ('tr',    '土耳其语',    1, 1, 12),
    ('ru',    '俄罗斯语',    1, 1, 13),
    ('es',    '西班牙语',    1, 1, 14),
    ('pt',    '葡萄牙语',    1, 1, 15),
    ('de',    '德语',        1, 1, 16),
    ('fr',    '法语',        1, 1, 17),
    ('it',    '意大利语',    1, 1, 18),
    ('nl',    '荷兰语',      1, 1, 19),
    ('pl',    '波兰语',      1, 1, 20),
    ('sv',    '瑞典语',      1, 1, 21),
    ('da',    '丹麦语',      1, 1, 22),
    ('el',    '希腊语',      1, 1, 23),
    ('hu',    '匈牙利语',    1, 1, 24),
    ('he',    '希伯来语',    0, 1, 25),
    ('uk',    '乌克兰语',    0, 1, 26),
    ('bn',    '孟加拉语',    0, 1, 27),
    ('cs',    '捷克语',      0, 1, 28),
    ('km',    '高棉语',      0, 1, 29),
    ('ro',    '罗马尼亚语',  1, 1, 30),
    ('kk',    '哈萨克语',    0, 1, 31),
    ('ur',    '乌尔都语',    0, 1, 32),
    ('my',    '缅甸语',      1, 1, 33);


-- ============================================================
-- 商品图片 AI 翻译/消除：译文来源标识
-- translations_data 增加 translation_source 列，
-- 仅商品图片场景（data_type=7）写入：0-未知/人工 1-AI翻译 2-AI智能消除。
-- ============================================================

ALTER TABLE `translations_data`
    ADD COLUMN `translation_source` tinyint NULL DEFAULT 0 COMMENT '译文来源：0-未知/人工 1-AI翻译 2-AI智能消除' AFTER `data_id`;

-- 添加权限
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (100003877, 'aiImageSettingManage', '图片智能处理', 10000373, 50, 1, '[{"auth_name":"\\u7f16\\u8f91","auth_sn":"configAiImageUpdate"}]', '', '', 0, 'admin');

-- =============================================================
-- D1084-adminMsgTts 增量脚本
-- 内容：admin_msg 增加 is_broadcast 标记位，解耦“消息中心展示”与“语音播报”
-- 背景：
--   售后申请会写入 admin_msg(msg_type=32)，SSE 语音播报按该表增量统计触发。
--   当后台“售后审核”关闭（afterSalesReview=0，即自动审核通过）时，售后无需人工
--   处理，不应再语音播报打扰，但消息中心仍需保留这条记录可查。
-- 方案：
--   新增 is_broadcast 标记位。SSE 播报计数(getNewMsgCountByType)只统计 is_broadcast=1
--   的消息；消息中心列表/未读统计不过滤该字段，照常展示。
--   自动审核产生的售后申请消息写入时置 is_broadcast=0。
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- 范围：全版本
-- =============================================================

ALTER TABLE `admin_msg` ADD COLUMN `is_broadcast` tinyint NOT NULL DEFAULT 1
    COMMENT '是否参与语音播报: 1-播报, 0-不播报' AFTER `related_data`;


-- =============================================================
-- B2B 批发商体系（一期全量批发能力）— 增量 SQL
-- 关联：.ai-os/specs/wholesaler-design.md / wholesaler-api-contract.md
-- VORT 范围：common.wholesale（CR-20260610-162600 自 b2b.wholesale 迁入；关闭版字段保留无害）
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- 约定：时间字段 BIGINT(Unix秒) DEFAULT NULL；逻辑删除 is_delete TINYINT；无物理外键；幂等可重复执行
-- =============================================================


-- =============================================================
-- 一、DDL — 5 张新表
-- =============================================================

-- ① 批发商身份
CREATE TABLE IF NOT EXISTS `wholesaler` (
                                            `wholesaler_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '批发商ID',
                                            `user_id`       INT          NOT NULL COMMENT '关联 user.user_id',
                                            `grade_id`      INT          NOT NULL COMMENT '关联 wholesaler_grade.grade_id',
                                            `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
                                            `source`        TINYINT      NOT NULL COMMENT '来源：1=注册自动 2=后台导入 3=手动添加',
                                            `shop_id`       INT          NOT NULL DEFAULT 0 COMMENT '多店铺隔离（一期固定0）',
                                            `remark`        VARCHAR(255) NOT NULL DEFAULT '' COMMENT '备注',
    `is_delete`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=有效 1=删除',
    `create_time`   BIGINT       DEFAULT NULL COMMENT '创建时间(Unix秒)',
    `update_time`   BIGINT       DEFAULT NULL COMMENT '更新时间(Unix秒)',
    PRIMARY KEY (`wholesaler_id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_grade_id` (`grade_id`),
    KEY `idx_status` (`status`)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='B2B批发商身份表';

-- ② 批发商等级
CREATE TABLE IF NOT EXISTS `wholesaler_grade` (
                                                  `grade_id`    INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '等级ID',
                                                  `grade_name`  VARCHAR(50)  NOT NULL COMMENT '等级名称',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    `sort_order`  INT          NOT NULL DEFAULT 50 COMMENT '排序值，升序',
    `remark`      VARCHAR(255) NOT NULL DEFAULT '' COMMENT '备注',
    `is_delete`   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=有效 1=删除',
    `create_time` BIGINT       DEFAULT NULL COMMENT '创建时间(Unix秒)',
    `update_time` BIGINT       DEFAULT NULL COMMENT '更新时间(Unix秒)',
    PRIMARY KEY (`grade_id`),
    KEY `idx_status` (`status`),
    KEY `idx_sort` (`sort_order`)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='B2B批发商等级';

-- ③ 商品级批发配置（1 product : 1 row）
-- CR-20260604-0937：moq_spu 重命名为 moq（含义随 moq_dimension 解释）
-- CR-20260610-162600：新增 shop_id（归属店铺，0=平台；商家端多租户隔离用）
CREATE TABLE IF NOT EXISTS `product_wholesale_config` (
                                                          `id`                     INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                                                          `product_id`             INT          NOT NULL COMMENT 'product.product_id',
                                                          `shop_id`                INT          NOT NULL DEFAULT 0 COMMENT '归属店铺：0=平台 >0=店铺（随 product.shop_id 冗余）',
                                                          `multi_spec_price_mode`  TINYINT      NOT NULL DEFAULT 1 COMMENT '多规格价格模式：1=商品级混批 2=SKU级独立',
                                                          `moq_dimension`          TINYINT      NOT NULL DEFAULT 1 COMMENT '起批维度：1=SPU 2=SKU',
                                                          `moq`                    INT          NOT NULL DEFAULT 1 COMMENT '起批数(moq_dimension=1时为加总起批,=2时为每SKU起批)',
                                                          `status`                 TINYINT      NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
                                                          `is_delete`              TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=有效 1=删除',
                                                          `create_time`            BIGINT       DEFAULT NULL COMMENT '创建时间(Unix秒)',
                                                          `update_time`            BIGINT       DEFAULT NULL COMMENT '更新时间(Unix秒)',
                                                          PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_id` (`product_id`),
    KEY `idx_status` (`status`),
    KEY `idx_shop_id` (`shop_id`)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='B2B批发商品级配置';

-- 已部署环境增量（init 建表后追加列的库执行；新库可跳过）：
-- ALTER TABLE `product_wholesale_config`
--     ADD COLUMN `shop_id` INT NOT NULL DEFAULT 0 COMMENT '归属店铺：0=平台 >0=店铺（随 product.shop_id 冗余）' AFTER `product_id`,
--     ADD KEY `idx_shop_id` (`shop_id`);
-- UPDATE `product_wholesale_config` c JOIN `product` p ON p.`product_id` = c.`product_id` SET c.`shop_id` = p.`shop_id` WHERE c.`shop_id` = 0;

-- ④ SKU 级批发配置（倍数购买）
-- CR-20260604-0937：删除 moq_sku 字段（起批数统一到 product_wholesale_config.moq）；表保留承载 multiple
CREATE TABLE IF NOT EXISTS `product_wholesale_sku_config` (
                                                              `id`          INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                                                              `product_id`  INT          NOT NULL COMMENT 'product.product_id',
                                                              `sku_id`      INT          NOT NULL COMMENT 'product_sku.sku_id',
                                                              `multiple`    INT          NOT NULL DEFAULT 1 COMMENT '倍数购买(1=不限制) 任何dimension下都按SKU',
                                                              `is_delete`   TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=有效 1=删除',
                                                              `create_time` BIGINT       DEFAULT NULL COMMENT '创建时间(Unix秒)',
                                                              `update_time` BIGINT       DEFAULT NULL COMMENT '更新时间(Unix秒)',
                                                              PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_sku` (`product_id`, `sku_id`),
    KEY `idx_product_id` (`product_id`)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='B2B批发SKU级配置(倍数购买)';

-- ⑤ 三维批发价（SKU × Grade × Tier）
CREATE TABLE IF NOT EXISTS `product_wholesale_price` (
                                                         `id`              INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
                                                         `product_id`      INT           NOT NULL COMMENT 'product.product_id',
                                                         `sku_id`          INT           NOT NULL DEFAULT 0 COMMENT '0=SPU兜底 >0=具体SKU',
                                                         `grade_id`        INT           NOT NULL DEFAULT 0 COMMENT '0=通用价 >0=特定等级',
                                                         `tier_min_qty`    INT           NOT NULL DEFAULT 1 COMMENT '阶梯起量',
                                                         `price_mode`      TINYINT       NOT NULL DEFAULT 1 COMMENT '1=折扣换算 2=直接金额',
                                                         `discount`        DECIMAL(4,2)  NOT NULL DEFAULT 0.00 COMMENT '折率[0.10,9.99] price_mode=1时用',
    `wholesale_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '固定批发价 price_mode=2时用',
    `status`          TINYINT       NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    `is_delete`       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=有效 1=删除',
    `create_time`     BIGINT        DEFAULT NULL COMMENT '创建时间(Unix秒)',
    `update_time`     BIGINT        DEFAULT NULL COMMENT '更新时间(Unix秒)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_combo` (`product_id`, `sku_id`, `grade_id`, `tier_min_qty`, `is_delete`),
    KEY `idx_product_grade` (`product_id`, `grade_id`, `status`)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT ='B2B批发价(SKU×Grade×Tier)';


-- =============================================================
-- 二、ALTER — cart / order_item 加批发字段（VORT 关闭版字段保留无害）
-- 执行注意：ALTER 在大表上会锁表，发布脚本注明「业务低峰执行」。
-- =============================================================

-- ⑥ cart 表加批发字段（幂等：information_schema 预检，列已存在则跳过；MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS）
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cart' AND COLUMN_NAME = 'is_wholesale_line');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `cart` ADD COLUMN `is_wholesale_line` TINYINT NOT NULL DEFAULT 0 COMMENT ''B2B批发:1=批发行 0=零售行''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cart' AND COLUMN_NAME = 'wholesale_grade_id');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `cart` ADD COLUMN `wholesale_grade_id` INT NOT NULL DEFAULT 0 COMMENT ''B2B批发:加车时等级快照(0=无)''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ⑦ order_item 表加批发字段（幂等：information_schema 预检，列已存在则跳过）
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_item' AND COLUMN_NAME = 'is_wholesale_line');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `order_item` ADD COLUMN `is_wholesale_line` TINYINT NOT NULL DEFAULT 0 COMMENT ''B2B批发:1=批发行 0=零售行''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_item' AND COLUMN_NAME = 'wholesale_grade_id');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `order_item` ADD COLUMN `wholesale_grade_id` INT NOT NULL DEFAULT 0 COMMENT ''B2B批发:等级快照(0=无)''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_item' AND COLUMN_NAME = 'wholesale_base_price');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `order_item` ADD COLUMN `wholesale_base_price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT ''B2B批发:批发基准价快照(冻结,审计/退款用,design §6)''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- =============================================================
-- 三、config 种子数据：批发体系 4 项（幂等：WHERE NOT EXISTS）
-- 注：config 表逻辑删除列为 is_del（历史遗留，与新表 is_delete 不一致，必须遵循）
-- =============================================================

INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'wholesaleEnabled', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'wholesaleEnabled' AND `is_del` = 0);

INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'wholesaleAutoRegister', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'wholesaleAutoRegister' AND `is_del` = 0);

INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'wholesaleDefaultGradeId', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'wholesaleDefaultGradeId' AND `is_del` = 0);

INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'wholesaleGivePoints', '0', UNIX_TIMESTAMP(), 1, 'admin', UNIX_TIMESTAMP(), 1, 'admin', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'wholesaleGivePoints' AND `is_del` = 0);


-- =============================================================
-- 四、authority 权限菜单：批发（3 级结构：顶级 → 2 分组 → 4 叶子菜单）
-- authority_id 段：40001110-40001125（预留 16 个）
-- 结构（对应 design §9.1）：
--   40001110 批发 (parent=0, 顶级)
--   ├── 40001115 批发商     (parent=40001110, 二级分组)
--   │   ├── 40001111 批发商列表  (parent=40001115)
--   │   ├── 40001112 等级管理    (parent=40001115)
--   │   └── 40001113 批发设置    (parent=40001115)
--   └── 40001116 批发商品   (parent=40001110, 二级分组)
--       └── 40001114 批发商品列表(parent=40001116)
-- 幂等：INSERT IGNORE 不重复插，UPDATE 二次执行结果不变
-- =============================================================

-- 批发顶级
INSERT IGNORE INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                                 `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001110, 'wholesaleManage', '批发', 0, 60, 1, '[]', '', 'iconfont-tig icon-pifashang', 0, 'admin');

-- 二级分组：批发商
INSERT IGNORE INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                                 `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001115, 'wholesalerGroup', '批发商', 40001110, 10, 1, '[]', '', 'iconfont-tig icon-pifashang', 0, 'admin');

-- 二级分组：批发商品
INSERT IGNORE INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                                 `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001116, 'wholesaleProductGroup', '批发商品', 40001110, 20, 1, '[]', '', 'iconfont-tig icon-pifashangpin', 0, 'admin');

-- 批发商列表（三级，归属"批发商"分组）
INSERT IGNORE INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                                 `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001111, 'wholesalerManage', '批发商列表', 40001115, 10, 1,
        '[{"auth_name":"添加","auth_sn":"wholesalerAddManage"},{"auth_name":"批量导入","auth_sn":"wholesalerImportManage"},{"auth_name":"启停","auth_sn":"wholesalerSetStatusManage"},{"auth_name":"改等级","auth_sn":"wholesalerChangeGradeManage"},{"auth_name":"按等级移除","auth_sn":"wholesalerRemoveByGradeManage"},{"auth_name":"移除","auth_sn":"wholesalerRemoveManage"}]',
        'wholesale/wholesaler/list/', '', 0, 'admin');

-- 等级管理（三级，归属"批发商"分组）
INSERT IGNORE INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                                 `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001112, 'wholesalerGradeManage', '等级管理', 40001115, 20, 1,
        '[{"auth_name":"保存","auth_sn":"wholesalerGradeSaveManage"},{"auth_name":"启停","auth_sn":"wholesalerGradeSetStatusManage"},{"auth_name":"删除","auth_sn":"wholesalerGradeDeleteManage"}]',
        'wholesale/grade/list/', '', 0, 'admin');

-- 批发设置（三级，归属"批发商"分组）
INSERT IGNORE INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                                 `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001113, 'wholesaleSettingManage', '批发设置', 40001115, 30, 1,
        '[{"auth_name":"保存","auth_sn":"wholesaleSettingSaveManage"}]',
        'wholesale/setting/', '', 0, 'admin');

-- 批发商品列表（三级，归属"批发商品"分组）
INSERT IGNORE INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                                 `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001114, 'wholesaleProductManage', '批发商品列表', 40001116, 10, 1,
        '[{"auth_name":"添加","auth_sn":"wholesaleProductAddManage"},{"auth_name":"删除","auth_sn":"wholesaleProductDeleteManage"},{"auth_name":"批发价","auth_sn":"wholesaleProductPriceManage"},{"auth_name":"起批量","auth_sn":"wholesaleProductBuyRuleManage"},{"auth_name":"倍数购买","auth_sn":"wholesaleProductMultipleManage"}]',
        'wholesale/product/list/', '', 0, 'admin');

-- 商家端：批发商品列表（CR-20260610-162600 / CD-039 多租户；同 authority_sn 双端各一条是项目先例，
-- 挂商家端"商品管理"分组 10000450 下；接口按 x-shop-id 自动隔离数据）
INSERT IGNORE INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                                 `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001117, 'wholesaleProductManage', '批发商品列表', 10000450, 60, 1,
        '[{"auth_name":"添加","auth_sn":"wholesaleProductAddManage"},{"auth_name":"删除","auth_sn":"wholesaleProductDeleteManage"},{"auth_name":"批发价","auth_sn":"wholesaleProductPriceManage"},{"auth_name":"起批量","auth_sn":"wholesaleProductBuyRuleManage"},{"auth_name":"倍数购买","auth_sn":"wholesaleProductMultipleManage"}]',
        'wholesale/product/list/', '', 0, 'shop');

-- 菜单图标刷新（存量库幂等）：INSERT IGNORE 不更新已存在行，用 UPDATE 同步 authority_ico
UPDATE `authority` SET `authority_ico` = 'iconfont-tig icon-pifashang' WHERE `authority_id` = 40001110;
UPDATE `authority` SET `authority_ico` = 'iconfont-tig icon-pifashang' WHERE `authority_id` = 40001115;
UPDATE `authority` SET `authority_ico` = 'iconfont-tig icon-pifashangpin'  WHERE `authority_id` = 40001116;


-- =============================================================
-- 五、初始化数据：默认等级「一级批发商」+ 默认等级 ID 同步到 config（TASK-WS-106）
-- =============================================================

-- 创建「一级批发商」（幂等）
INSERT INTO `wholesaler_grade`
(`grade_name`, `status`, `sort_order`, `remark`, `is_delete`, `create_time`, `update_time`)
SELECT '一级批发商', 1, 10, '系统初始化默认等级', 0, UNIX_TIMESTAMP(), UNIX_TIMESTAMP()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `wholesaler_grade` WHERE `grade_name`='一级批发商' AND `is_delete`=0);

-- 把 config.wholesaleDefaultGradeId 同步为该等级的 ID（仅当当前值为 '0' 时更新，避免覆盖客户已配置）
UPDATE `config` c
    JOIN `wholesaler_grade` g ON g.`grade_name` = '一级批发商' AND g.`is_delete` = 0
    SET c.`biz_val` = CAST(g.`grade_id` AS CHAR),
        c.`update_time` = UNIX_TIMESTAMP()
WHERE c.`biz_code` = 'wholesaleDefaultGradeId'
  AND c.`is_del` = 0
  AND c.`biz_val` = '0';
