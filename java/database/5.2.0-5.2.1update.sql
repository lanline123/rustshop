INSERT INTO `config` ( `biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES
    ('openWechatPcLogin', '0', 1758007754, 0, '', 1758007754, 0, '', 0);

ALTER TABLE `collect_product`
    ADD COLUMN `shop_id` mediumint UNSIGNED NOT NULL DEFAULT 0 COMMENT '店铺id';