DELETE FROM authority WHERE authority_sn in ('suppliers', 'suppliersManage');
alter table order_item
    add vendor_product_supply_price decimal(10, 2) comment '供应商产品供货价';
ALTER TABLE `shop_account_log`
    MODIFY COLUMN `new_shop_money` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '现店铺资金',
    MODIFY COLUMN `new_frozen_money` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '现店铺冻结资金';
-- 添加字段
ALTER TABLE `aftersales_log`
    ADD COLUMN `shop_id` INT(11) DEFAULT 0 COMMENT 'shopId',
    ADD COLUMN `vendor_id` INT(11) DEFAULT 0 COMMENT '供应商id';

UPDATE authority SET authority_sn = 'mobileDecorateManage', authority_name = '页面管理', parent_id = 27, sort_order = 50, is_show = 1, child_auth = '[{"authName":"编辑","authSn":"mobileDecorateModifyManage"},{"authName":"新增","authSn":"decorateCreateManage"}]', route_link = 'decorate/mobile_decorate/list/', authority_ico = '', is_system = 0, admin_type = 'admin' WHERE authority_id = 28;
UPDATE authority SET authority_sn = 'pcDecorateManage', authority_name = '首页装修', parent_id = 62, sort_order = 50, is_show = 1, child_auth = '[{"authName":"发布","authSn":"decoratePublishManage"},{"authName":"设为首页","authSn":"decorateSetHomeManage"},{"authName":"编辑","authSn":"decorateModifyManage"},{"authName":"模块编辑","authSn":"decorateDiscreteModifyManage"},{"authName":"存入草稿","authSn":"decorateSaveDraftManage"},{"authName":"新增","authSn":"decorateCreateManage"}]', route_link = 'decorate/pc_decorate/list/', authority_ico = '', is_system = 0, admin_type = 'admin' WHERE authority_id = 63;
