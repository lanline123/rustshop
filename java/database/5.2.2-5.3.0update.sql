CREATE TABLE reservation_tpl
(
    reservation_tpl_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '预约模板ID',
    template_name      VARCHAR(50)  NOT NULL COMMENT '模板名称',
    reservation_type   TINYINT      NOT NULL COMMENT '预约时间类型：0=无需提前,1=需提前N天,2=根据下单时间配置',
    advance_number     INT                   DEFAULT 0 COMMENT '需提前预约的天数，仅当reservation_type=1时有效',
    advance_unit       VARCHAR(1)            DEFAULT 'd' COMMENT '需提前预约的时间单位：d=天,h=小时，仅当reservation_type=1时有效',
    max_days           INT          NOT NULL COMMENT '最长允许预约天数',
    shop_id            INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '店铺id',
    is_default         tinyint      NOT NULL DEFAULT '0' COMMENT '是否默认，1表示默认',
    KEY idx_shop_id (shop_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='产品预约配置';

ALTER TABLE `store_product`
    ADD COLUMN `reservation_tpl_id` int DEFAULT NULL COMMENT '预约模板Id';

ALTER TABLE `product`
    ADD COLUMN `reservation_tpl_id` int DEFAULT NULL COMMENT '预约模板Id';

ALTER TABLE `order`
    MODIFY COLUMN `order_type` tinyint DEFAULT '1' COMMENT '订单类型：1普通订单，3积分订单，2拼团，5砍一砍 6 虚拟订单 7 付费商品订单 8 卡密订单 9预约到店 10 预约上门';

CREATE TABLE product_reservation_time
(
    product_reservation_time_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    product_id                  INT          NOT NULL COMMENT '产品ID',
    start_time                  VARCHAR(10)  NOT NULL COMMENT '开始时间，格式HH:mm',
    end_time                    VARCHAR(10)  NOT NULL COMMENT '结束时间，格式HH:mm',
    add_time                    int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间 ',
    KEY idx_product_id (product_id),
    UNIQUE KEY `uk_product_id_time` (`product_id`, `start_time`, `end_time`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='产品预约时间段配置';


CREATE TABLE product_reservation_stock
(
    product_reservation_stock_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id                   INT          NOT NULL COMMENT '产品ID',
    shop_id                      INT          NOT NULL COMMENT '门店ID',
    sku_id                       INT          NOT NULL DEFAULT 0 COMMENT 'SKU ID',
    product_reservation_time_id  INT          NOT NULL COMMENT '预约时间段ID，对应 product_reservation_time.id',
    stock_date                   VARCHAR(12)  NOT NULL COMMENT '库存日期，比如 2025-09-18',
    total_stock                  INT          NOT NULL COMMENT '总库存数',
    used_stock                   INT          NOT NULL DEFAULT 0 COMMENT '已用库存数',
    add_time                     int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间 ',
    UNIQUE KEY uniq_stock (`shop_id`, `product_id`, `sku_id`, `stock_date`, `product_reservation_time_id`),
    KEY idx_product_sku_date (product_id, sku_id, stock_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='产品预约日历库存';

CREATE TABLE order_reservations
(
    order_reservations_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_id              INT          NOT NULL COMMENT '产品ID',
    product_reservation_time_id INT    NULL COMMENT '预约时间段ID',
    sku_id                INT          NOT NULL DEFAULT 0 COMMENT 'SKU ID',
    reservation_date      VARCHAR(12)  NOT NULL COMMENT '库存日期，比如 2025-09-18',
    start_time            VARCHAR(10)  NOT NULL COMMENT '开始时间，格式HH:mm',
    end_time              VARCHAR(10)  NOT NULL COMMENT '结束时间，格式HH:mm',
    time_slot_description VARCHAR(50)  NOT NULL COMMENT '时间段描述',
    add_time              int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间 ',
    KEY idx_product_id (order_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC COMMENT ='产品预约信息';

-- admin 相关菜单
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (11002009, 'storeAppointmentTplManage', '预约模板', 55, 50, 1,
        '[{\"authName\":\"编辑\",\"authSn\":\"storeAppointmentTplUpdateManage\"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (11002010, 'storeInventoryCalendarManage', '库存日历', 10000411, 50, 1,
        '[{\"authName\":\"编辑\",\"authSn\":\"storeInventoryCalendarUpdateManage\"}]', '', '', 0, 'admin');
-- 门店相关菜单
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (20000068, 'storeAppointmentTplManage', '预约模板', 20000028, 50, 1,
        '[{\"authName\":\"编辑\",\"authSn\":\"storeAppointmentTplUpdateManage\"}]', '', '', 0, 'store');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (20000069, 'storeInventoryCalendarManage', '库存日历', 20000043, 50, 1,
        '[{\"authName\":\"编辑\",\"authSn\":\"storeInventoryCalendarUpdateManage\"}]', '', '', 0, 'store');

