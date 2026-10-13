UPDATE `authority`
SET parent_id = 10001
WHERE authority_id = 30001007;

UPDATE `authority`
SET parent_id = 20000002
WHERE authority_id = 30001009;

ALTER TABLE `shipping_tpl_info`
    MODIFY COLUMN `start_number` decimal(12, 5) NOT NULL DEFAULT 0.0 COMMENT '起重或起件' AFTER `region_data`,
    MODIFY COLUMN `add_number` decimal(12, 5) NOT NULL DEFAULT 0.0 COMMENT '每增重或增价' AFTER `start_price`;

