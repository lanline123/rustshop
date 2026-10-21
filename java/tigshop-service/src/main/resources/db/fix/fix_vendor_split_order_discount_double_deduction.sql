-- ============================================================================
-- 修复 vendor 拆单子订单优惠金额被重复扣减的历史数据
-- 关联代码修复点：OrderServiceImpl.createSplitOrder
--   原实现使用 item.price（已扣减满减后的价格）累加 product_amount，导致
--   updateOrderMoney 再用 total = product_amount - discount_amount 计算时，
--   discount 被减了两次，使 total_amount 比 paid_amount 少了一份 discount。
-- 本脚本只修复严格匹配该 BUG 模式的拆单子订单：
--   1. 必须是拆单子单 (parent_order_id > 0)
--   2. 必须是 vendor 子单 (vendor_id IS NOT NULL)
--   3. 必须存在满减/满折类 discount_amount > 0
--   4. paid_amount - total_amount 恰好等于 discount_amount
--   5. order_item 中 SUM(price * quantity) = order.product_amount
--      且 SUM(origin_price * quantity) = order.product_amount + discount_amount
-- 满足全部条件才认为是本次 BUG，直接将 product_amount / total_amount 各
-- 加回一份 discount_amount，使 total_amount = paid_amount。
-- 同时把派生字段 unrefund_amount 清零（其原值就是 paid_amount - total_amount）。
-- ============================================================================

START TRANSACTION;

-- 1) 修复前快照（可选打印）
SELECT 'BEFORE' AS phase, o.order_id, o.order_sn, o.vendor_id, o.parent_order_id,
       o.product_amount, o.discount_amount, o.tax_rate_amount,
       o.total_amount, o.paid_amount, o.balance, o.unrefund_amount
FROM `order` o
JOIN (
    SELECT order_id,
           SUM(CASE WHEN is_gift = 0 THEN origin_price * quantity ELSE 0 END) AS sum_origin,
           SUM(CASE WHEN is_gift = 0 THEN price * quantity ELSE 0 END) AS sum_price
    FROM order_item GROUP BY order_id
) items ON items.order_id = o.order_id
WHERE o.parent_order_id > 0
  AND o.vendor_id IS NOT NULL
  AND o.is_del = 0
  AND o.discount_amount > 0
  AND ROUND(o.paid_amount - o.total_amount, 2) = ROUND(o.discount_amount, 2)
  AND ROUND(items.sum_price, 2) = ROUND(o.product_amount, 2)
  AND ROUND(items.sum_origin, 2) = ROUND(o.product_amount + o.discount_amount, 2);

-- 2) 更新 product_amount、total_amount、unrefund_amount
UPDATE `order` o
JOIN (
    SELECT order_id,
           SUM(CASE WHEN is_gift = 0 THEN origin_price * quantity ELSE 0 END) AS sum_origin,
           SUM(CASE WHEN is_gift = 0 THEN price * quantity ELSE 0 END) AS sum_price
    FROM order_item GROUP BY order_id
) items ON items.order_id = o.order_id
SET o.product_amount = o.product_amount + o.discount_amount,
    o.total_amount   = o.total_amount   + o.discount_amount,
    o.unrefund_amount = 0
WHERE o.parent_order_id > 0
  AND o.vendor_id IS NOT NULL
  AND o.is_del = 0
  AND o.discount_amount > 0
  AND ROUND(o.paid_amount - o.total_amount, 2) = ROUND(o.discount_amount, 2)
  AND ROUND(items.sum_price, 2) = ROUND(o.product_amount, 2)
  AND ROUND(items.sum_origin, 2) = ROUND(o.product_amount + o.discount_amount, 2);

-- 3) 修复后快照
SELECT 'AFTER' AS phase, order_id, order_sn, vendor_id, parent_order_id,
       product_amount, discount_amount, tax_rate_amount,
       total_amount, paid_amount, balance, unrefund_amount
FROM `order`
WHERE order_id IN (1350, 1353, 1359);

COMMIT;
