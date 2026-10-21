-- 满减/满折联调种子：平台满减（满100减10，满300减50，阶梯）
-- 用法：mysql ... tigshop-java -e "source rust/tools/compat/seed_fullreduce.sql"
DELETE FROM product_promotion WHERE promotion_id IN (900001, 900002);
INSERT INTO product_promotion
 (promotion_id, promotion_name, start_time, end_time, limit_user_rank, `range`, range_data,
  min_order_amount, max_order_amount, promotion_type, promotion_type_data,
  is_available, sort_order, shop_id, rules_type, unit)
VALUES
 (900001, '满减测试-阶梯满减', UNIX_TIMESTAMP() - 3600, UNIX_TIMESTAMP() + 86400, '',
  0, '[]', 100.00, 0.00, 1,
  '[{"minAmount":100,"reduce":10},{"minAmount":300,"reduce":50}]',
  1, 0, 0, 1, 1),
 (900002, '满折测试-9折', UNIX_TIMESTAMP() - 3600, UNIX_TIMESTAMP() + 86400, '',
  0, '[]', 100.00, 0.00, 2,
  '[{"minAmount":100,"reduce":9}]',
  1, 0, 0, 1, 1);

SELECT promotion_id, promotion_name, promotion_type, rules_type, unit, min_order_amount
FROM product_promotion WHERE promotion_id IN (900001, 900002);
