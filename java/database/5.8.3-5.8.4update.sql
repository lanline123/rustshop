ALTER TABLE `user_address`
    ADD COLUMN `position_address` varchar(512) DEFAULT '' COMMENT '定位地址' AFTER `address`;