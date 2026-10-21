-- 结算页 v3 联调种子（O2O 自提 / 发票随单 / 地区税率）
-- 用法：& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -h 127.0.0.1 -P 3306 -u root -proot tigshop-java -e "source D:/tigshop/java-b2b2b-pro-v5.8.57/rust/tools/compat/seed_checkout_v3.sql"

-- 1) 先导入 v2 基础种子（模板 900001、商品 136/4、券 900001/900002）
SOURCE D:/tigshop/java-b2b2b-pro-v5.8.57/rust/tools/compat/seed_checkout_v2.sql;

-- 2) 税率种子：为地区 110100（北京市辖区）设置 6.5% 税率
DELETE FROM tax_rate WHERE region_id = 110100 OR tax_rate_id = 900001;
INSERT INTO tax_rate
  (tax_rate_id, region_id, region_name, rate, is_active, remark, create_time, update_time)
VALUES
  (900001, 110100, '北京市辖区', 6.5000, 1, 'v3测试税率', UNIX_TIMESTAMP(), UNIX_TIMESTAMP());

-- 3) 地址 900001 确保包含 110100 地区
UPDATE user_address
SET region_ids = '[110000, 110100, 110101]',
    region_names = '["北京市","市辖区","东城区"]'
WHERE address_id = 900001;

-- 4) 清理历史发票测试数据
DELETE FROM order_invoice WHERE company_code = '91360100TEST12345X';
