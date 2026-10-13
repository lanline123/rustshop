alter table area_code
    modify code varchar(64) default '' not null comment '区号';

UPDATE admin_user_vendor SET is_using = 0 WHERE is_admin = 1;

alter table user_balance_log
    add before_frozen_balance decimal(10, 2) not null comment '增加或减少之前的被冻结的余额' after frozen_balance;

alter table user_balance_log
    add after_frozen_balance decimal(10, 2) not null comment '增加或减少之后的被冻结的余额' after before_frozen_balance;

ALTER TABLE `statement_output`
    ADD COLUMN `real_settlement` int NULL DEFAULT NULL COMMENT '真实的结算状态：0未结算 1结算',
    ADD COLUMN `real_settlement_time` int NULL DEFAULT NULL COMMENT '结算时间';

CREATE TABLE `record_rate` (
   `record_rate_id` int NOT NULL AUTO_INCREMENT,
   `record_id` int DEFAULT NULL COMMENT '单据id',
   `record_type` int DEFAULT NULL COMMENT '单据类型',
   `shop_service_fee` decimal(10,2) DEFAULT NULL COMMENT '店铺费率',
   `shop_withdrawal_fee` decimal(10,2) DEFAULT NULL COMMENT '店铺手续费',
   `storefront_service_fee` decimal(10,2) DEFAULT NULL COMMENT '门店服务费',
   `storefront_withdrawal_fee` decimal(10,2) DEFAULT NULL COMMENT '门店手续费',
   `supplier_service_fee` decimal(10,2) DEFAULT NULL COMMENT '供应商服务费',
   `supplier_withdrawal_fee` decimal(10,2) DEFAULT NULL COMMENT '供应商手续费',
   `gmt_create` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '记录当前时间',
   `shop_id` int DEFAULT NULL COMMENT '店铺id',
   `vendor_id` int DEFAULT NULL COMMENT '供应商id',
   PRIMARY KEY (`record_rate_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='记录费率表';

INSERT INTO translations (id, translation_name, translation_key, data_type, shop_id) VALUES (5576, '显示 {0} 个结果', '6c1a2785bd498df50fa6245bc4adab22', 0, 0);
INSERT INTO translations_data (id, locale_id, translation_name, translation_key, translation_value, data_type, data_id) VALUES (37422, 1, '显示 {0} 个结果', '6c1a2785bd498df50fa6245bc4adab22', '显示 {0} 个结果', 0, 5576);
INSERT INTO translations_data (id, locale_id, translation_name, translation_key, translation_value, data_type, data_id) VALUES (37423, 2, '显示 {0} 个结果', '6c1a2785bd498df50fa6245bc4adab22', 'Showing {0} results', 0, 5576);
