ALTER TABLE aftersales ADD COLUMN `return_goods_tip` VARCHAR(255)  NOT NULL DEFAULT '' COMMENT '售后退货提示';

UPDATE `config`
SET `biz_val` = 'https://oss.tigshop.com/img/gallery/202504/1744354592RIBTOCaOK1DLrAlJ7a.jpeg'
WHERE `biz_code` = 'adminDarkLogo' AND `is_del` = 0;

UPDATE `config`
SET `biz_val` = 'https://oss.tigshop.com/img/gallery/202404/1713598967OT3Pc9o6dvgxav6z0W.png'
WHERE `biz_code` = 'adminLightLogo' AND `is_del` = 0;

UPDATE `config`
SET `biz_val` = ' - powered by tigshop'
WHERE `biz_code` = 'poweredBy' AND `is_del` = 0;

ALTER TABLE `admin_log`
    MODIFY COLUMN `user_id` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '该日志所记录的操作者id,同admin_user的user_id';