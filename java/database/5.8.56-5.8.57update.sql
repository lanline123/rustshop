-- =============================================================
-- v5.8.56 -> v5.8.57 增量脚本
-- 内容：D1182 库存预警自定义配置；D1184 消息中心分类优化；D1194 店铺/门店合规管理
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- =============================================================


-- #############################################################
-- 来源：D1182-stock-warning-config.sql
-- #############################################################

-- D1182-stock-warning-config：库存预警支持自定义配置。
-- 库存预警阈值由硬编码 100 升级为「按经营主体开关 + 按商品/SKU 配置上下限」。
-- 建表使用 CREATE TABLE IF NOT EXISTS，authority DML 幂等，可重复执行。

-- 库存预警通知总开关：按经营主体隔离。
-- shop_id = 0 表示平台自营（虚拟主体，不在 shop 表），> 0 为 shop 表中的店铺或门店。
-- 无记录时业务侧视为开启，保证升级后存量环境预警能力不中断。
CREATE TABLE IF NOT EXISTS `stock_warning_setting`
(
    `shop_id`        int             NOT NULL COMMENT '归属主体：0=平台自营，>0=店铺或门店（shop.shop_id）',
    `notify_enabled` tinyint         NOT NULL DEFAULT 1 COMMENT '库存预警通知总开关：0=关闭，1=开启',
    `create_time`    bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建时间（Unix秒）',
    `update_time`    bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新时间（Unix秒）',
    PRIMARY KEY (`shop_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='库存预警通知总开关（按经营主体隔离）';

-- 库存预警规则：商品/SKU 维度上下限。
-- sku_id = 0 表示单规格商品（SPU 级规则）。
-- last_warn_status 保存最近一次已发送的预警状态，用于同一状态不重复发送；库存恢复正常后可重新发送。
CREATE TABLE IF NOT EXISTS `stock_warning_rule`
(
    `id`               int unsigned    NOT NULL AUTO_INCREMENT COMMENT '主键',
    `shop_id`          int             NOT NULL DEFAULT 0 COMMENT '归属主体：0=平台自营，>0=店铺或门店（shop.shop_id）',
    `product_id`       int             NOT NULL COMMENT 'product.product_id',
    `sku_id`           int             NOT NULL DEFAULT 0 COMMENT 'product_sku.sku_id；0=单规格商品',
    `warn_enabled`     tinyint         NOT NULL DEFAULT 1 COMMENT '预警开关：0=关闭，1=开启；关闭时保留已填上下限',
    `lower_limit`      int             NOT NULL DEFAULT 0 COMMENT '库存下限：实时库存小于该值为低库存',
    `upper_limit`      int             NOT NULL DEFAULT 0 COMMENT '库存上限：实时库存大于该值为高库存',
    `last_warn_status` tinyint         NOT NULL DEFAULT 0 COMMENT '最近一次预警状态：0=正常，1=低库存，2=高库存，3=无货；用于防重复发送',
    `last_warn_time`   bigint unsigned NOT NULL DEFAULT 0 COMMENT '最近一次预警发送时间（Unix秒）',
    `is_delete`        tinyint         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=有效，1=删除',
    `create_time`      bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建时间（Unix秒）',
    `update_time`      bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新时间（Unix秒）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_shop_product_sku` (`shop_id`, `product_id`, `sku_id`),
    KEY `idx_product_sku` (`product_id`, `sku_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='库存预警规则（商品/SKU 维度上下限）';

-- 供应商库存预警通知总开关：按 vendor_id 隔离。
CREATE TABLE IF NOT EXISTS `vendor_stock_warning_setting`
(
    `vendor_id`       int             NOT NULL COMMENT '供应商 ID',
    `notify_enabled`  tinyint         NOT NULL DEFAULT 1 COMMENT '库存预警通知总开关：0=关闭，1=开启',
    `create_time`     bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建时间（Unix秒）',
    `update_time`     bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新时间（Unix秒）',
    PRIMARY KEY (`vendor_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='供应商库存预警通知总开关';

-- 供应商库存预警规则：以 vendor_product / vendor_product_sku 为目标。
CREATE TABLE IF NOT EXISTS `vendor_stock_warning_rule`
(
    `id`                    int unsigned    NOT NULL AUTO_INCREMENT COMMENT '主键',
    `vendor_id`             int             NOT NULL COMMENT '供应商 ID',
    `product_id`            bigint          NOT NULL COMMENT 'vendor_product.id',
    `sku_id`                bigint          NOT NULL COMMENT 'vendor_product_sku.id；单规格商品规则为0',
    `warn_enabled`          tinyint         NOT NULL DEFAULT 1 COMMENT '预警开关：0=关闭，1=开启',
    `lower_limit`           int             NOT NULL DEFAULT 0 COMMENT '库存下限',
    `upper_limit`           int             NOT NULL DEFAULT 0 COMMENT '库存上限',
    `last_warn_status`      tinyint         NOT NULL DEFAULT 0 COMMENT '最近一次预警状态：0=正常，1=低库存，2=高库存，3=无货',
    `last_warn_time`        bigint unsigned NOT NULL DEFAULT 0 COMMENT '最近一次预警时间（Unix秒）',
    `is_del`                tinyint         NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=有效，1=删除',
    `create_time`           bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建时间（Unix秒）',
    `update_time`           bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新时间（Unix秒）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_vendor_product_sku` (`vendor_id`, `product_id`, `sku_id`),
    KEY `idx_vendor_product_sku` (`product_id`, `sku_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='供应商库存预警规则';

-- 库存预警菜单：挂在「库存管理」（authority_sn = stockControl）下，平台、店铺、门店三端各一份。
INSERT INTO `authority`
(`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`,
 `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'stockWarningManage',
       '库存预警',
       parent.`authority_id`,
       58,
       1,
       '[{"auth_name":"编辑","auth_sn":"stockWarningUpdateManage"}]',
       'product/stock_warning/list/',
       '',
       0,
       parent.`admin_type`
FROM `authority` parent
WHERE parent.`authority_sn` = 'stockControl'
  AND parent.`admin_type` IN ('admin', 'shop', 'store')
  AND NOT EXISTS (SELECT 1
                  FROM `authority` existing
                  WHERE existing.`authority_sn` = 'stockWarningManage'
                    AND existing.`admin_type` = parent.`admin_type`);

-- 供应商端库存父菜单使用 vendorStockManage（admin_type = 'vendor'）。
INSERT INTO `authority`
(`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`,
 `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'stockWarningManage',
       '库存预警',
       parent.`authority_id`,
       55,
       1,
       '[{"auth_name":"编辑","auth_sn":"stockWarningUpdateManage"}]',
       'product/stock_warning/list/',
       '',
       0,
       'vendor'
FROM `authority` parent
WHERE parent.`authority_sn` = 'vendorStockManage'
  AND parent.`admin_type` = 'vendor'
  AND NOT EXISTS (SELECT 1
                  FROM `authority` existing
                  WHERE existing.`authority_sn` = 'stockWarningManage'
                    AND existing.`admin_type` = 'vendor');


-- #############################################################
-- 来源：D1184-msgCenterCategoryOptimize.sql
-- #############################################################

-- D1184 消息中心分类优化
-- 仅更新 admin_msg.msg_type 注释；历史消息编号与数据不迁移，分类由 AdminMsgTypeEnum 兼容映射。
-- 执行一次：MySQL 5.7+/8.x
ALTER TABLE `admin_msg`
    MODIFY COLUMN `msg_type` tinyint(3) UNSIGNED NOT NULL DEFAULT 0
    COMMENT '消息类型：11新订单、12已付款订单、13订单完成、21商品库存预警、22商品无货、23商品下架、24商品审核通知、25商品咨询、26商品询价、31订单取消、32售后申请、33提现申请、34发票资质审核、35发票申请、36售后退款、37线下支付审核、41店铺入驻申请、42店铺资质修改、43店铺违规、44保证金预警、51系统消息、52待办任务、53意见反馈（历史兼容）、54发货提醒、55建议、56投诉、57其它反馈；15/16订单问题/订单咨询（历史兼容，不展示分类）';


-- #############################################################
-- 来源：D1198-bugfix.sql
-- #############################################################

-- D1198-bugfix：同步 2025 年重庆市行政区划调整。
-- 官方口径：新增两江新区（500157）；原江北区全部划入两江新区；
-- 原渝北区的大湾、统景、大盛、兴隆、茨竹 5 镇划入北碚区，其余划入两江新区。
-- 历史订单保存下单时地址快照，本脚本不修改 `order`。

-- 1. 新增两江新区。first_word 保持与现有重庆区县数据一致为空，列表使用地区全拼二级排序。
INSERT INTO `region`
    (`region_id`, `level`, `region_name`, `parent_id`, `is_hot`, `sort_order`, `first_word`)
SELECT 500157, 4, '两江新区', 500100, 0, 50, ''
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `region` WHERE `region_id` = 500157
);

-- 2. 原江北区全部划入两江新区：迁移当前会员收货地址的地区 ID 与名称快照。
UPDATE `user_address`
SET `region_ids`       = REPLACE(`region_ids`, '500105', '500157'),
    `region_names`     = REPLACE(`region_names`, '江北区', '两江新区'),
    `address`          = REPLACE(`address`, '江北区', '两江新区'),
    `address_data`     = REPLACE(`address_data`, '江北区', '两江新区'),
    `formatted_address` = REPLACE(`formatted_address`, '江北区', '两江新区'),
    `position_address` = REPLACE(`position_address`, '江北区', '两江新区')
WHERE CASE
          WHEN JSON_VALID(`region_ids`) = 1
              THEN JSON_CONTAINS(`region_ids`, '500105', '$')
          ELSE 0
      END = 1;

-- 3. 原渝北区明确属于 5 个划转镇的地址迁入北碚区。
-- 只按完整镇名识别，避免把同名道路、楼盘误判为行政区划。
UPDATE `user_address`
SET `region_ids`       = REPLACE(`region_ids`, '500112', '500109'),
    `region_names`     = REPLACE(`region_names`, '渝北区', '北碚区'),
    `address`          = REPLACE(`address`, '渝北区', '北碚区'),
    `address_data`     = REPLACE(`address_data`, '渝北区', '北碚区'),
    `formatted_address` = REPLACE(`formatted_address`, '渝北区', '北碚区'),
    `position_address` = REPLACE(`position_address`, '渝北区', '北碚区')
WHERE CASE
          WHEN JSON_VALID(`region_ids`) = 1
              THEN JSON_CONTAINS(`region_ids`, '500112', '$')
          ELSE 0
      END = 1
  AND CONCAT_WS(' ', `address`, `address_line_two`, `building_name`, `address_data`,
                `formatted_address`, `position_address`)
      REGEXP '大湾镇|统景镇|大盛镇|兴隆镇|茨竹镇';

-- 4. 原渝北区中能由完整镇街名称确认属于两江新区的地址，迁入两江新区。
-- 没有完整镇街信息的记录保持原值，禁止在无法区分上述 5 镇时盲目迁移。
UPDATE `user_address`
SET `region_ids`       = REPLACE(`region_ids`, '500112', '500157'),
    `region_names`     = REPLACE(`region_names`, '渝北区', '两江新区'),
    `address`          = REPLACE(`address`, '渝北区', '两江新区'),
    `address_data`     = REPLACE(`address_data`, '渝北区', '两江新区'),
    `formatted_address` = REPLACE(`formatted_address`, '渝北区', '两江新区'),
    `position_address` = REPLACE(`position_address`, '渝北区', '两江新区')
WHERE CASE
          WHEN JSON_VALID(`region_ids`) = 1
              THEN JSON_CONTAINS(`region_ids`, '500112', '$')
          ELSE 0
      END = 1
  AND CONCAT_WS(' ', `address`, `address_line_two`, `building_name`, `address_data`,
                `formatted_address`, `position_address`)
      REGEXP '人和街道|大石坝街道|大竹林街道|寸滩街道|水土街道|双凤桥街道|双龙湖街道|五里店街道|天宫殿街道|王家街道|龙山街道|龙塔街道|龙溪街道|石马河街道|仙桃街道|礼嘉街道|江北城街道|回兴街道|观音桥街道|华新街街道|两路街道|金山街道|宝圣湖街道|复兴街道|铁山坪街道|悦来街道|鸳鸯街道|郭家沱街道|康美街道|翠云街道|蔡家岗街道|木耳镇|五宝镇|龙兴镇|玉峰山镇|石船镇|古路镇|鱼嘴镇|施家梁镇|复盛镇|洛碛镇|童家溪镇';

-- 执行后人工回查仍无法判定归属的原渝北区当前收货地址：
-- SELECT `address_id`, `region_ids`, `region_names`, `address`, `formatted_address`
-- FROM `user_address`
-- WHERE CASE
--           WHEN JSON_VALID(`region_ids`) = 1
--               THEN JSON_CONTAINS(`region_ids`, '500112', '$')
--           ELSE 0
--       END = 1;

-- 5. 小票打印菜单按版本运行时分流：O2O 使用收银台设置，其余版本使用商城/店铺设置。
-- 数据库同时保留新旧两套菜单，由后端根据版本过滤，保证同一增量脚本可用于所有版本。
UPDATE `authority`
SET `is_show` = 1,
    `child_auth` = CASE
        WHEN `authority_sn` = 'baseReceiptManage' AND `admin_type` = 'admin'
            THEN '[{"authName":"编辑","authSn":"baseReceiptUpdateManage"}]'
        ELSE '[{"authName":"编辑","authSn":"shopBaseReceiptUpdateManage"}]'
    END
WHERE (`authority_sn` = 'baseReceiptManage' AND `admin_type` = 'admin')
   OR (`authority_sn` = 'shopBaseReceiptManage' AND `admin_type` IN ('shop', 'store'));

UPDATE `authority`
SET `is_show` = 1,
    `child_auth` = '[{"authName":"编辑","authSn":"posReceiptPrintUpdateManage"}]'
WHERE `authority_sn` = 'posReceiptPrint'
  AND `admin_type` IN ('admin', 'shop', 'store');
-- 分账设置编辑权限：仅拥有该权限的后台账号可保存分账配置。
UPDATE `authority`
SET `child_auth` = JSON_ARRAY_APPEND(
    COALESCE(NULLIF(`child_auth`, ''), JSON_ARRAY()),
    '$', JSON_OBJECT('auth_name', '编辑', 'auth_sn', 'profitsharingSettingUpdateManage')
)
WHERE `authority_sn` = 'profitsharingSettingManage'
  AND `admin_type` = 'admin'
  AND JSON_SEARCH(`child_auth`, 'one', 'profitsharingSettingUpdateManage', NULL, '$[*].auth_sn') IS NULL;
