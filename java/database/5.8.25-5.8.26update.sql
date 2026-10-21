UPDATE `authority` SET `authority_sn` = 'productBatchEditManage', `authority_name` = '批量修改', `parent_id` = 19, `sort_order` = 54, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u6279\\u91cf\\u4fee\\u6539\",\"auth_sn\":\"productBatchEditModifyManage\"}]', `route_link` = 'product/product_batch/edit/list', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 37;
UPDATE `authority` SET `authority_sn` = 'productBatchEditManage', `authority_name` = '批量修改', `parent_id` = 40001050, `sort_order` = 54, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u6279\\u91cf\\u4fee\\u6539\",\"auth_sn\":\"productBatchEditModifyManage\"}]', `route_link` = 'product/product_batch/edit/list', `authority_ico` = '', `is_system` = 0, `admin_type` = 'store' WHERE `authority_id` = 40001054;
UPDATE `authority` SET `authority_sn` = 'productBatchEditManage', `authority_name` = '批量修改', `parent_id` = 40001060, `sort_order` = 54, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u6279\\u91cf\\u4fee\\u6539\",\"auth_sn\":\"productBatchEditModifyManage\"}]', `route_link` = 'product/product_batch/edit/list', `authority_ico` = '', `is_system` = 0, `admin_type` = 'shop' WHERE `authority_id` = 40001064;
UPDATE `authority` SET `authority_sn` = 'productBatchEditManage', `authority_name` = '批量修改', `parent_id` = 40001070, `sort_order` = 54, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u6279\\u91cf\\u4fee\\u6539\",\"auth_sn\":\"productBatchEditModifyManage\"}]', `route_link` = 'product/product_batch/edit/list', `authority_ico` = '', `is_system` = 0, `admin_type` = 'vendor' WHERE `authority_id` = 40001074;




-- =============================================================
-- 公众号菜单与自动回复配置 - 建表 & 种子脚本
-- 配套：公众号菜单与自动回复配置-需求说明.md v2.0
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- 约定：表名 wechat_ 前缀；wechat_menu 参考 friend_links 风格，
--      不继承 BasePO（不加 create_by_* / is_del 等审计字段）；
--      config 表复用现有单级 biz_code / biz_val 结构（不改表）。
-- 执行顺序：按文件自上而下执行；可重复执行（DDL 用 IF NOT EXISTS，
--      config 种子用 INSERT ... SELECT ... WHERE NOT EXISTS 做幂等）。
-- 变更记录：
--   v1.1（2026-04-21）按仓库 config 表实际 schema 对齐；去掉 ON DUPLICATE KEY。
--   v1.2（2026-04-23）M2 关键字回复：新增三表（规则/关键字/回复）。
-- =============================================================

-- -------------------------------------------------------------
-- 1. wechat_menu  公众号自定义菜单（单例）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `wechat_menu` (
                                             `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                                             `menu_json`    MEDIUMTEXT   NOT NULL                COMMENT '后台自有结构的菜单树 JSON（含 id/排序字段），发布时转微信官方 button 结构',
                                             `published_at` DATETIME     NULL                    COMMENT '最近一次成功推送到微信的时间；NULL 表示仅保存未发布',
                                             `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                             PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公众号自定义菜单（单例）';

-- -------------------------------------------------------------
-- 2. config 表 KV 种子数据（幂等）
-- 说明：仓库现有 config 表结构（详见 database/pure.sql L24）为单级：
--       id, biz_code(varchar64), biz_val(text),
--       create_time/update_time(BIGINT, Unix 秒),
--       create_by_id/name, update_by_id/name, is_del(TINYINT)
--      biz_code 无唯一索引，故不能用 ON DUPLICATE KEY UPDATE；
--      这里用 INSERT ... SELECT ... WHERE NOT EXISTS 实现幂等。
--      两条 key 直接作为 biz_code（与 shopName / shopTitle 等现有约定一致）。
-- -------------------------------------------------------------
INSERT INTO `config` (`biz_code`, `biz_val`,
                      `create_time`, `create_by_id`, `create_by_name`,
                      `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'wechatSubscribeReply',
       '{"msgType":1,"content":"欢迎关注{shopName}，我是你的购物小助手～","newsTitle":null,"newsCoverUrl":null,"newsDescription":null,"newsUrl":null}',
       UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
    WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'wechatSubscribeReply' AND `is_del` = 0);

INSERT INTO `config` (`biz_code`, `biz_val`,
                      `create_time`, `create_by_id`, `create_by_name`,
                      `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'wechatDefaultReply',
       '{"msgType":1,"content":"你好 {nickname}，没能看懂你的消息，试试点击底部菜单吧～","newsTitle":null,"newsCoverUrl":null,"newsDescription":null,"newsUrl":null}',
       UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
    WHERE NOT EXISTS (SELECT 1 FROM `config` WHERE `biz_code` = 'wechatDefaultReply' AND `is_del` = 0);

-- -------------------------------------------------------------
-- 3. 权限点（authority）种子
-- 说明：参照 database/pure.sql L870（friendLinksManage）、L1008（licensedSetting）样例。
--      父节点 parent_id 取 15（"设置" 顶级分组，与 licensedSetting 同级）；
--      sort_order=54，排在 licensedSetting(55) 前一位。
--      子权限（编辑/删除/批量）走 child_auth JSON 字段，沿用 friendLinks 的惯例。
-- 可重复执行：使用 INSERT ... SELECT ... WHERE NOT EXISTS 做幂等。
-- -------------------------------------------------------------

-- 3.1 顶级分组：微信公众号
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                         `is_show`, `child_auth`, `route_link`, `authority_ico`,
                         `is_system`, `admin_type`)
SELECT 'wechatMp', '微信公众号', 15, 54, 1, '[]', '', 'iconfont-tig icon-weixin', 0, 'admin'
    WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn` = 'wechatMp' AND `admin_type` = 'admin');

-- 3.2 公众号菜单（查看 + 子权限：保存并发布）
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                         `is_show`, `child_auth`, `route_link`, `authority_ico`,
                         `is_system`, `admin_type`)
SELECT 'wechatMenu', '公众号菜单',
       (SELECT a.`authority_id` FROM (SELECT `authority_id` FROM `authority`
                                      WHERE `authority_sn` = 'wechatMp' AND `admin_type` = 'admin' LIMIT 1) a),
       10, 1,
       '[{"auth_name":"保存并发布","auth_sn":"wechatMenuManage"}]',
       'setting/wechat/menu/', '', 0, 'admin'
    WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn` = 'wechatMenu' AND `admin_type` = 'admin');

-- 3.3 关键字回复（查看 + 子权限：管理）
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                         `is_show`, `child_auth`, `route_link`, `authority_ico`,
                         `is_system`, `admin_type`)
SELECT 'wechatKeywordReply', '关键字回复',
       (SELECT a.`authority_id` FROM (SELECT `authority_id` FROM `authority`
                                      WHERE `authority_sn` = 'wechatMp' AND `admin_type` = 'admin' LIMIT 1) a),
       20, 1,
       '[{"auth_name":"新增/编辑/删除/启停","auth_sn":"wechatKeywordReplyManage"}]',
       'setting/wechat/keyword-reply/list/', '', 0, 'admin'
    WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn` = 'wechatKeywordReply' AND `admin_type` = 'admin');

-- 3.4 自动回复设置（查看 + 子权限：保存）
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`,
                         `is_show`, `child_auth`, `route_link`, `authority_ico`,
                         `is_system`, `admin_type`)
SELECT 'wechatAutoReply', '自动回复设置',
       (SELECT a.`authority_id` FROM (SELECT `authority_id` FROM `authority`
                                      WHERE `authority_sn` = 'wechatMp' AND `admin_type` = 'admin' LIMIT 1) a),
       30, 1,
       '[{"auth_name":"保存","auth_sn":"wechatAutoReplyManage"}]',
       'setting/wechat/auto-reply/', '', 0, 'admin'
    WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn` = 'wechatAutoReply' AND `admin_type` = 'admin');

-- =============================================================
-- M2 关键字回复三表
-- 需求依据：公众号菜单与自动回复配置-需求说明.md §8.2/8.3/8.4。
-- =============================================================

-- -------------------------------------------------------------
-- 4. wechat_keyword_rule  关键字规则主表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `wechat_keyword_rule` (
                                                     `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                                                     `rule_name`   VARCHAR(60)  NOT NULL                COMMENT '规则名称，≤60，全局唯一（不区分大小写，依赖 utf8mb4_general_ci + uk_rule_name）',
    `reply_mode`  TINYINT      NOT NULL DEFAULT 1      COMMENT '回复模式：1=随机回复一条 2=回复全部',
    `enabled`     TINYINT      NOT NULL DEFAULT 1      COMMENT '启用状态：0=停用 1=启用',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_name` (`rule_name`),
    KEY `idx_enabled` (`enabled`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公众号关键字回复-规则主表';

-- -------------------------------------------------------------
-- 5. wechat_keyword_rule_keyword  规则-关键字子表
-- 说明：rule_id 为逻辑外键（参考 wechat_keyword_rule.id），沿用项目惯例不建 FK 约束。
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `wechat_keyword_rule_keyword` (
                                                             `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                                                             `rule_id`     BIGINT       NOT NULL                COMMENT '所属规则 id（逻辑外键 wechat_keyword_rule.id）',
                                                             `keyword`     VARCHAR(30)  NOT NULL                COMMENT '关键字，≤30',
    `match_type`  TINYINT      NOT NULL DEFAULT 1      COMMENT '匹配方式：1=全匹配 2=半匹配',
    `sort_order`  INT          NOT NULL DEFAULT 0      COMMENT '规则内排序，0 起递增',
    PRIMARY KEY (`id`),
    KEY `idx_rule` (`rule_id`),
    KEY `idx_keyword_match` (`keyword`, `match_type`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公众号关键字回复-关键字子表';

-- -------------------------------------------------------------
-- 6. wechat_keyword_rule_reply  规则-回复内容子表
-- 说明：rule_id 为逻辑外键（参考 wechat_keyword_rule.id），沿用项目惯例不建 FK 约束。
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `wechat_keyword_rule_reply` (
                                                           `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
                                                           `rule_id`          BIGINT        NOT NULL                COMMENT '所属规则 id（逻辑外键 wechat_keyword_rule.id）',
                                                           `sort_order`       INT           NOT NULL DEFAULT 0      COMMENT '规则内排序，0 起递增',
                                                           `msg_type`         TINYINT       NOT NULL DEFAULT 1      COMMENT '消息类型：1=文本 2=图文',
                                                           `content_text`     TEXT          NULL                    COMMENT '文本内容（msg_type=1 必填，≤300 字符）',
                                                           `news_title`       VARCHAR(64)   NULL                    COMMENT '图文标题（msg_type=2）',
    `news_cover_url`   VARCHAR(500)  NULL                    COMMENT '图文封面图 URL（msg_type=2，HTTPS 公网可访问）',
    `news_description` VARCHAR(120)  NULL                    COMMENT '图文摘要（msg_type=2，≤120 字符）',
    `news_url`         VARCHAR(500)  NULL                    COMMENT '图文跳转链接（msg_type=2 必填）',
    PRIMARY KEY (`id`),
    KEY `idx_rule` (`rule_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公众号关键字回复-回复内容子表';

-- 文件完。



-- ===== 周期购次卡功能数据库变更 =====

-- 1. product_cycle 表新增字段
ALTER TABLE product_cycle ADD COLUMN delivery_type TINYINT NOT NULL DEFAULT 1 COMMENT '交付模式类型：1-按计划配送，2-按需兑换（次卡）' AFTER product_id;
ALTER TABLE product_cycle ADD COLUMN redeem_total_times INT DEFAULT NULL COMMENT '兑换总次数（次卡模式，固定次数）' AFTER delivery_type;
ALTER TABLE product_cycle ADD COLUMN effective_type TINYINT DEFAULT NULL COMMENT '生效时间类型：1-立即，2-次日，3-延迟' AFTER redeem_total_times;
ALTER TABLE product_cycle ADD COLUMN effective_delay_value INT DEFAULT NULL COMMENT '生效延迟数值' AFTER effective_type;
ALTER TABLE product_cycle ADD COLUMN effective_delay_unit TINYINT DEFAULT NULL COMMENT '生效延迟单位：1-天，2-小时' AFTER effective_delay_value;
ALTER TABLE product_cycle ADD COLUMN validity_type TINYINT DEFAULT NULL COMMENT '有效期类型：1-长期，2-当天，3-N天内，4-固定区间' AFTER effective_delay_unit;
ALTER TABLE product_cycle ADD COLUMN validity_value INT DEFAULT NULL COMMENT '有效期数值（N天/小时内有效时使用）' AFTER validity_type;
ALTER TABLE product_cycle ADD COLUMN validity_unit TINYINT DEFAULT NULL COMMENT '有效期单位：1-天，2-小时（validityType=3时使用）' AFTER validity_value;
ALTER TABLE product_cycle ADD COLUMN validity_start_date DATE DEFAULT NULL COMMENT '有效期开始日期（固定区间时使用）' AFTER validity_unit;
ALTER TABLE product_cycle ADD COLUMN validity_end_date DATE DEFAULT NULL COMMENT '有效期结束日期（固定区间时使用）' AFTER validity_start_date;

-- 2. product_cycle_spec 表新增字段
ALTER TABLE product_cycle_spec ADD COLUMN display_name VARCHAR(100) DEFAULT NULL COMMENT '次卡展示名称' AFTER period_name;

-- 3. order 表新增字段
ALTER TABLE `order` ADD COLUMN is_card_order TINYINT NOT NULL DEFAULT 0 COMMENT '是否次卡订单：0-否，1-是' AFTER is_cycle_order;

-- 4. 新建 order_cycle_card 表（次卡权益记录）
CREATE TABLE IF NOT EXISTS order_cycle_card (
                                                card_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '次卡权益ID',
                                                order_id INT NOT NULL COMMENT '关联主订单ID',
                                                order_sn VARCHAR(50) NOT NULL COMMENT '关联主订单号',
    user_id INT NOT NULL COMMENT '用户ID',
    shop_id INT NOT NULL DEFAULT 0 COMMENT '店铺ID',
    product_id INT NOT NULL COMMENT '商品ID',
    sku_id INT DEFAULT NULL COMMENT 'SKU ID',
    product_name VARCHAR(200) DEFAULT '' COMMENT '商品名称（冗余）',
    product_pic VARCHAR(500) DEFAULT '' COMMENT '商品图片（冗余）',
    sku_value VARCHAR(200) DEFAULT '' COMMENT 'SKU规格值（冗余）',
    total_times INT NOT NULL COMMENT '总兑换次数',
    redeemed_times INT NOT NULL DEFAULT 0 COMMENT '已兑换次数',
    remaining_times INT NOT NULL COMMENT '剩余兑换次数',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总支付金额（冗余，用于退款计算）',
    effective_type TINYINT NOT NULL DEFAULT 1 COMMENT '生效时间类型：1-立即，2-次日，3-延迟',
    effective_time BIGINT DEFAULT NULL COMMENT '生效时间（时间戳）',
    expire_time BIGINT DEFAULT NULL COMMENT '到期时间（时间戳，NULL表示长期有效）',
    validity_type TINYINT NOT NULL DEFAULT 1 COMMENT '有效期类型：1-长期，2-当天，3-N天内，4-固定区间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待生效，1-使用中，2-已用完，3-已过期，4-已退款',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号（防并发扣减）',
    create_time BIGINT DEFAULT NULL COMMENT '创建时间',
    update_time BIGINT DEFAULT NULL COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_order_id (order_id),
    INDEX idx_status (status),
    INDEX idx_expire_time (expire_time),
    INDEX idx_effective_time (effective_time)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='次卡权益记录表';

-- 5. 新建 order_cycle_card_redeem 表（兑换记录）
CREATE TABLE IF NOT EXISTS order_cycle_card_redeem (
                                                       redeem_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '兑换记录ID',
                                                       card_id INT NOT NULL COMMENT '关联次卡权益ID',
                                                       order_id INT NOT NULL COMMENT '关联主订单ID',
                                                       sub_order_id INT DEFAULT NULL COMMENT '生成的子订单ID',
                                                       sub_order_sn VARCHAR(50) DEFAULT NULL COMMENT '子订单号',
    user_id INT NOT NULL COMMENT '用户ID',
    shop_id INT NOT NULL DEFAULT 0 COMMENT '店铺ID',
    redeem_index INT NOT NULL COMMENT '兑换序号（从1开始）',
    unit_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '本次兑换金额（总额÷总次数）',
    delivery_method VARCHAR(50) DEFAULT NULL COMMENT '配送方式',
    address_id INT DEFAULT NULL COMMENT '收货地址ID',
    store_id INT DEFAULT NULL COMMENT '自提门店ID',
    delivery_date DATE DEFAULT NULL COMMENT '配送日期',
    delivery_time_slot VARCHAR(50) DEFAULT NULL COMMENT '配送时间段',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-处理中，1-已完成，2-已取消',
    create_time BIGINT DEFAULT NULL COMMENT '兑换时间',
    update_time BIGINT DEFAULT NULL COMMENT '更新时间',
    INDEX idx_card_id (card_id),
    INDEX idx_user_id (user_id),
    INDEX idx_sub_order_id (sub_order_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='次卡兑换记录表';

-- 购物车表新增兑换次卡ID字段
ALTER TABLE `cart` ADD COLUMN `redeem_card_id` INT DEFAULT NULL COMMENT '兑换次卡ID（非空即兑换模式）' AFTER `cycle_spec_id`;

-- 有效期日期字段升级为 DATETIME，支持时分秒精度
ALTER TABLE `product_cycle` MODIFY COLUMN `validity_start_date` DATETIME DEFAULT NULL COMMENT '有效期开始时间（固定区间时使用）';
ALTER TABLE `product_cycle` MODIFY COLUMN `validity_end_date` DATETIME DEFAULT NULL COMMENT '有效期结束时间（固定区间时使用）';


-- ============================================================
-- 物流公司配置表（租户绑定配置）
-- 版本：5.8.19
-- 功能：平台/店铺/门店/供应商物流管理
-- ============================================================

-- 1. 建表：logistics_company_config
CREATE TABLE IF NOT EXISTS `logistics_company_config` (
                                                          `config_id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
                                                          `logistics_id` int NOT NULL COMMENT '物流公司ID（关联 logistics_company.logistics_id）',
                                                          `shop_id` int NOT NULL DEFAULT 0 COMMENT '店铺ID（平台=0，店铺/门店>0）',
                                                          `vendor_id` int NOT NULL DEFAULT 0 COMMENT '供应商ID（非供应商=0，供应商>0）',
                                                          `customer_name` varchar(100) DEFAULT '' COMMENT '客户编码',
    `customer_pwd` varchar(100) DEFAULT '' COMMENT '密码',
    `month_code` varchar(100) DEFAULT '' COMMENT '月结号',
    `send_site` varchar(100) DEFAULT '' COMMENT '网点编码',
    `send_staff` varchar(100) DEFAULT '' COMMENT '网点名称',
    `exp_type` varchar(50) DEFAULT '' COMMENT '快递业务类型',
    `sort_order` int DEFAULT 50 COMMENT '排序',
    `is_default` tinyint DEFAULT 0 COMMENT '是否默认：1默认，0非默认',
    `is_enabled` tinyint DEFAULT 1 COMMENT '是否启用：1启用，0禁用',
    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_shop_vendor_logistics` (`shop_id`, `vendor_id`, `logistics_id`),
    KEY `idx_logistics_id` (`logistics_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流公司配置表';

-- 2. 菜单：新增【物流设置】菜单（admin/shop/store/vendor 各一条）
-- 注：route_link 为初始版本，前端会手动创建后替换

-- admin 端：挂在 shipping(id=55) 配送设置组下
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'logisticsCompanyConfigManage', '物流设置', 55, 53, 1, '[{"auth_name":"编辑","auth_sn":"logisticsCompanyConfigEditManage"}]', 'setting/shipping/logistics_company_config/list/', '', 0, 'admin'
    WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn` = 'logisticsCompanyConfigManage' AND `admin_type` = 'admin');

-- shop 端：挂在 shopShipping(id=10000322) 配送设置下
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'shopLogisticsCompanyConfigManage', '物流设置', 10000322, 53, 1, '[{"auth_name":"编辑","auth_sn":"logisticsCompanyConfigEditManage"}]', 'merchant_setting/shop_shipping/logistics_company_config/list/', '', 0, 'shop'
    WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn` = 'shopLogisticsCompanyConfigManage' AND `admin_type` = 'shop');

-- store 端：挂在 shopShipping(id=20000028) 配送设置下
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'shopLogisticsCompanyConfigManage', '物流设置', 20000028, 53, 1, '[{"auth_name":"编辑","auth_sn":"logisticsCompanyConfigEditManage"}]', 'merchant_setting/shop_shipping/logistics_company_config/list/', '', 0, 'store'
    WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn` = 'shopLogisticsCompanyConfigManage' AND `admin_type` = 'store');

-- vendor 端：挂在 vendorSetting(id=11000012) 供应商设置下
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'vendorLogisticsCompanyConfigManage', '物流设置', 11000012, 53, 1, '[{"auth_name":"编辑","auth_sn":"logisticsCompanyConfigEditManage"}]', 'vendor_setting/logistics_company_config/list/', '', 0, 'vendor'
    WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn` = 'vendorLogisticsCompanyConfigManage' AND `admin_type` = 'vendor');

-- 3. 数据迁移：将 logistics_company 中有凭证配置的记录迁移到 logistics_company_config
INSERT INTO `logistics_company_config` (`logistics_id`, `shop_id`, `vendor_id`, `customer_name`, `customer_pwd`, `month_code`, `send_site`, `send_staff`, `exp_type`, `sort_order`, `is_default`, `is_enabled`)
SELECT
    `logistics_id`,
    IFNULL(`shop_id`, 0),
    0,
    IFNULL(`customer_name`, ''),
    IFNULL(`customer_pwd`, ''),
    IFNULL(`month_code`, ''),
    IFNULL(`send_site`, ''),
    IFNULL(`send_staff`, ''),
    IFNULL(`exp_type`, ''),
    IFNULL(`sort_order`, 50),
    0,
    IFNULL(`is_enabled`, 1)
FROM `logistics_company`
WHERE (`customer_name` IS NOT NULL AND `customer_name` != '')
   OR (`customer_pwd` IS NOT NULL AND `customer_pwd` != '')
   OR (`month_code` IS NOT NULL AND `month_code` != '')
   OR (`send_site` IS NOT NULL AND `send_site` != '')
   OR (`send_staff` IS NOT NULL AND `send_staff` != '')
    ON DUPLICATE KEY UPDATE `config_id` = `config_id`;

