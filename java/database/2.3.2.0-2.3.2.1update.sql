ALTER TABLE `print`
    ADD COLUMN `auto_print` tinyint(1) DEFAULT '1' COMMENT '订单支付自动打印 1开启 2关闭';
