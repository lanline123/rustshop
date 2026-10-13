UPDATE `message_template`
SET `content` = '尊敬的${userNickName}：您的认证申请已受理，正在处理中，请耐心等待。提交时间：${submitTime}查询方式：登录【个人中心】查看进度,审核期间账户功能正常使用。'
WHERE message_id = 9
  AND type = 7;


UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"translationsModifyManage\"},{\"authName\":\"\\u4e00\\u952e\\u7ffb\\u8bd1\",\"authSn\":\"translationsMultipleManage\"},{\"authName\":\"\\u6dfb\\u52a0\\u7ffb\\u8bd1\",\"authSn\":\"translationsSingleManage\"}]'
WHERE `authority_sn` = 'translationContentManage';

UPDATE `authority`
SET `authority_sn`   = 'AfterSalesServiceManage',
    `authority_name` = '商品售后服务',
    `parent_id`      = 10000410,
    `sort_order`     = 55,
    `is_show`        = 1,
    `child_auth`     = '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"settingSaveAfterSalesManage\"}]',
    `route_link`     = 'setting/config/after_sales_service/',
    `authority_ico`  = '',
    `is_system`      = 0,
    `admin_type`     = 'admin'
WHERE `authority_id` = 267;

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (30001003, 'StoreDecorate', '门店装修', 20000020, 3, 1,
        '[{\"authName\":\"编辑\",\"authSn\":\"StoreDecorateUpdateManager\"}]', '', '', 0, 'store');

DELETE
FROM
    decorate
WHERE
    shop_id in (SELECT shop_id from shop where shop_type = 2)
  AND
    decorate_type = 1;