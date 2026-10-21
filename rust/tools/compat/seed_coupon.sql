-- 结算页优惠券联调种子（满 100 减 10，用户 1 = testbuyer）
-- 用法：mysql ... tigshop-java -e "source rust/tools/compat/seed_coupon.sql"
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
