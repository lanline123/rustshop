-- 秒杀引擎联调种子：商品 6（78 元）秒杀价 50 元，库存 5，每人限购 1
-- 用法：mysql ... tigshop-java -e "source rust/tools/compat/seed_seckill.sql"
-- 注意：限购按「活动时间窗内该用户已购数量」统计；重复跑冒烟前删除历史测试订单或放宽 seckill_limit_num。
DELETE FROM seckill_item WHERE product_id = 6;
DELETE FROM seckill WHERE seckill_id = 900002;
INSERT INTO seckill
 (seckill_id, seckill_name, seckill_start_time, seckill_end_time, seckill_limit_num, product_id, shop_id)
VALUES
 (900002, '结算秒杀测试', UNIX_TIMESTAMP() - 1800, UNIX_TIMESTAMP() + 2592000, 1, 6, 0);
INSERT INTO seckill_item
 (rec_id, seckill_id, product_id, sku_id, seckill_price, seckill_stock, seckill_sales,
  seckill_start_time, seckill_end_time)
VALUES
 (900002, 900002, 6, 0, 50.00, 5, 0, UNIX_TIMESTAMP() - 1800, UNIX_TIMESTAMP() + 2592000);

SELECT rec_id, product_id, sku_id, seckill_price, seckill_stock, seckill_sales
FROM seckill_item WHERE product_id = 6;
