-- 拼团联调种子：普通团（2 人成团，40 元）
-- 用法：mysql ... tigshop-java -e "source rust/tools/compat/seed_groupon.sql"

-- 活动 900001：商品 6，2 人成团 40 元，1 小时有效
DELETE FROM groupon_promotion_detail WHERE groupon_promotion_id = 900001;
DELETE FROM groupon_promotion_item WHERE groupon_promotion_id = 900001;
DELETE FROM groupon_promotion_record WHERE groupon_promotion_id = 900001;
DELETE FROM groupon_promotion WHERE groupon_promotion_id = 900001;
INSERT INTO groupon_promotion
 (groupon_promotion_id, name, start_time, end_time, group_type, expiration_time,
  groupon_image, pc_groupon_image, share_image, groupon_explain, shop_id, add_time)
VALUES
 (900001, '拼团测试-2人团', UNIX_TIMESTAMP() - 3600, UNIX_TIMESTAMP() + 86400, 1, 3600,
  '', '', '', '', 0, UNIX_TIMESTAMP());
INSERT INTO groupon_promotion_item
 (groupon_promotion_item_id, groupon_promotion_id, product_id, group_type, group_num,
  order_limit_num, is_auto_group, min_group_num, sort, add_time, min_group_price)
VALUES
 (900001, 900001, 136, 1, '[{"groupNum":2,"groupNumKey":1}]',
  0, 0, 2, 0, UNIX_TIMESTAMP(), 10.00);
INSERT INTO groupon_promotion_detail
 (groupon_detail_id, groupon_promotion_id, groupon_promotion_item_id, product_id, sku_id,
  group_price, limit_num, is_join, add_time)
VALUES
 (900001, 900001, 900001, 136, 0,
  '[{"price":10,"groupNumKey":1}]', 100, 1, UNIX_TIMESTAMP());

-- 活动 900002：商品 136，2 人成团 10 元，5 秒过期（验证过期定时任务）
DELETE FROM groupon_promotion_detail WHERE groupon_promotion_id = 900002;
DELETE FROM groupon_promotion_item WHERE groupon_promotion_id = 900002;
DELETE FROM groupon_promotion_record WHERE groupon_promotion_id = 900002;
DELETE FROM groupon_promotion WHERE groupon_promotion_id = 900002;
INSERT INTO groupon_promotion
 (groupon_promotion_id, name, start_time, end_time, group_type, expiration_time,
  groupon_image, pc_groupon_image, share_image, groupon_explain, shop_id, add_time)
VALUES
 (900002, '拼团测试-过期', UNIX_TIMESTAMP() - 3600, UNIX_TIMESTAMP() + 86400, 1, 5,
  '', '', '', '', 0, UNIX_TIMESTAMP());
INSERT INTO groupon_promotion_item
 (groupon_promotion_item_id, groupon_promotion_id, product_id, group_type, group_num,
  order_limit_num, is_auto_group, min_group_num, sort, add_time, min_group_price)
VALUES
 (900002, 900002, 6, 1, '[{"groupNum":2,"groupNumKey":1}]',
  0, 0, 2, 0, UNIX_TIMESTAMP(), 10.00);
INSERT INTO groupon_promotion_detail
 (groupon_detail_id, groupon_promotion_id, groupon_promotion_item_id, product_id, sku_id,
  group_price, limit_num, is_join, add_time)
VALUES
 (900002, 900002, 900002, 6, 0,
  '[{"price":10,"groupNumKey":1}]', 100, 1, UNIX_TIMESTAMP());

SELECT groupon_promotion_id, name, expiration_time FROM groupon_promotion WHERE groupon_promotion_id IN (900001, 900002);
