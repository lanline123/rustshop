-- 结算页 v2 联调种子（运费模板 / 积分 / 店铺券 / 账期 / 预售）
-- 用法：mysql ... tigshop-java -e "source rust/tools/compat/seed_checkout_v2.sql"

-- 1) 运费模板 900001：按重量，首重 1kg/2 元，续重 1kg/1 元，不满额包邮
DELETE FROM shipping_tpl_info WHERE shipping_tpl_id = 900001 OR id = 900001;
DELETE FROM shipping_tpl WHERE shipping_tpl_id = 900001;
INSERT INTO shipping_tpl
 (shipping_tpl_id, shipping_time, shipping_tpl_name, is_free, pricing_type, is_default, shop_id)
VALUES
 (900001, '24小时内发货', '结算v2测试模板', 0, 2, 0, 0);
INSERT INTO shipping_tpl_info
 (id, shipping_type_id, shipping_tpl_id, is_free, is_default, region_data,
  start_number, start_price, add_number, add_price, free_price, pricing_type)
VALUES
 (900001, 1, 900001, 0, 1, '{"areaRegions":[]}',
  1.00000, 2.00, 1.00000, 1.00, 0.00, 2);

-- 2) 商品 136（35 元）使用该模板，重量 2.5kg → 运费 = 2 + ceil(1.5/1)*1 = 4
UPDATE product SET shipping_tpl_id = 900001, product_weight = 2.5, free_shipping = 0
WHERE product_id = 136;

-- 3) 积分：用户 1 预置 500（integralScale=1 → 100 积分抵 1 元；integralPercent=50）
UPDATE user SET points = 500 WHERE user_id = 1;

-- 4) 全局券 900001（满 100 减 10）：重置为未使用
DELETE FROM user_coupon WHERE id = 900001;
DELETE FROM coupon WHERE coupon_id = 900001;
INSERT INTO coupon
 (coupon_id, coupon_name, coupon_money, coupon_discount, coupon_desc, coupon_type,
  send_range, send_range_data, min_order_amount, send_start_date, send_end_date,
  send_type, use_day, use_start_date, use_end_date, is_show, is_global, is_new_user,
  enabled_click_get, shop_id, is_delete, limit_num, delay_day, send_num,
  max_order_amount, coupon_unit, reduce_type, add_time)
VALUES
 (900001, '满100减10', 10.00, 10.0, '满100元可用', 1,
  0, '[]', 100.00, UNIX_TIMESTAMP() - 3600, UNIX_TIMESTAMP() + 2592000,
  0, 30, UNIX_TIMESTAMP() - 3600, UNIX_TIMESTAMP() + 2592000, 1, 1, 0,
  1, 0, 0, 1, 0, 1,
  0.00, 1, 1, UNIX_TIMESTAMP());
INSERT INTO user_coupon
 (id, coupon_id, coupon_sn, user_id, used_time, order_id, start_date, end_date, source_type)
VALUES
 (900001, 900001, 'TESTCPN900001', 1, 0, 0, UNIX_TIMESTAMP() - 3600, UNIX_TIMESTAMP() + 2592000, 0);

-- 5) 店铺券 900002（shop_id=1，满 10 减 5）：验证店铺范围（购物车为店铺 0 时应禁用）
DELETE FROM user_coupon WHERE id = 900002;
DELETE FROM coupon WHERE coupon_id = 900002;
INSERT INTO coupon
 (coupon_id, coupon_name, coupon_money, coupon_discount, coupon_desc, coupon_type,
  send_range, send_range_data, min_order_amount, send_start_date, send_end_date,
  send_type, use_day, use_start_date, use_end_date, is_show, is_global, is_new_user,
  enabled_click_get, shop_id, is_delete, limit_num, delay_day, send_num,
  max_order_amount, coupon_unit, reduce_type, add_time)
VALUES
 (900002, '店铺满10减5', 5.00, 0.0, '仅限店铺 1', 1,
  0, '[]', 10.00, UNIX_TIMESTAMP() - 3600, UNIX_TIMESTAMP() + 2592000,
  0, 30, UNIX_TIMESTAMP() - 3600, UNIX_TIMESTAMP() + 2592000, 1, 0, 0,
  1, 1, 0, 1, 0, 1,
  0.00, 1, 1, UNIX_TIMESTAMP());
INSERT INTO user_coupon
 (id, coupon_id, coupon_sn, user_id, used_time, order_id, start_date, end_date, source_type)
VALUES
 (900002, 900002, 'TESTCPN900002', 1, 0, 0, UNIX_TIMESTAMP() - 3600, UNIX_TIMESTAMP() + 2592000, 0);

-- 6) 账期授信：用户 1 额度 1000 元，月结 30 天
DELETE FROM credit_user WHERE credit_user_id = 900001;
INSERT INTO credit_user
 (credit_user_id, user_id, credit_limit, used_amount, available_amount,
  settlement_type, status, period_start_time, create_time, update_time)
VALUES
 (900001, 1, 1000.00, 0.00, 1000.00, 1, 1,
  UNIX_TIMESTAMP(), UNIX_TIMESTAMP(), UNIX_TIMESTAMP());
