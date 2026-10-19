-- 定金预售联调种子：商品 264（32 元）固定定金 5 元，尾款 27 元
-- 用法：mysql ... tigshop-java -e "source rust/tools/compat/seed_presale.sql"
UPDATE product SET sale_mode = 2 WHERE product_id = 264;
DELETE FROM product_presale WHERE product_id = 264;
INSERT INTO product_presale
 (presale_id, product_id, sale_mode, deposit_type, deposit_ratio, is_round_down,
  deposit_fixed_amount, final_payment_type, final_payment_relative_days,
  final_payment_duration_days, final_payment_duration_hours,
  final_payment_start_time, final_payment_end_time,
  delivery_type, delivery_relative_days, delivery_relative_hours, delivery_date,
  status, create_time, update_time)
VALUES
 (900001, 264, 2, 2, NULL, 0,
  5.00, 1, 7,
  7, 0,
  0, 0,
  1, 7, 0, 0,
  1, UNIX_TIMESTAMP(), UNIX_TIMESTAMP());

SELECT presale_id, product_id, sale_mode, deposit_type, deposit_fixed_amount
FROM product_presale WHERE product_id = 264;
