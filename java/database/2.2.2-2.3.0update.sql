# 会员表增加状态字段
ALTER TABLE `user` ADD COLUMN `status` tinyint(2) NULL DEFAULT 1 COMMENT '状态；0-禁用，1-启用';

# 默认设置项调整
UPDATE `config` SET biz_val='/static/mini/images/common/default_tech_support.png' where biz_code = 'defaultTechSupport';

# 权限调整
UPDATE `authority` SET `authority_sn` = 'configManage', `authority_name` = '商城设置', `parent_id` = 15, `sort_order` = 50, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u4fdd\\u5b58\\u5546\\u57ce\\u57fa\\u7840\\u8bbe\\u7f6e\",\"auth_sn\":\"saveBasicManage\"},{\"auth_name\":\"\\u57fa\\u7840\\u8bbe\\u7f6e\\u66f4\\u65b0\",\"auth_sn\":\"settingSaveManage\"},{\"auth_name\":\"\\u8bbe\\u7f6e\\u7f16\\u8f91\",\"auth_sn\":\"settingUpdateManage\"},{\"auth_name\":\"\\u53d1\\u9001\\u6d4b\\u8bd5\\u90ae\\u4ef6\",\"auth_sn\":\"sendTestEmailModifyManage\"},{\"auth_name\":\"\\u751f\\u6210\\u5e73\\u53f0\\u8bc1\\u4e66\",\"auth_sn\":\"platformCertificateModifyManage\"},{\"auth_name\":\"\\u76f8\\u518c\\u4fee\\u6539\",\"auth_sn\":\"galleryModifyManage\"},{\"auth_name\":\"\\u76f8\\u518c\\u56fe\\u7247\\u4fee\\u6539\",\"auth_sn\":\"galleryPicModifyManage\"},{\"auth_name\":\"\\u76f8\\u518c\\u7ba1\\u7406\",\"auth_sn\":\"galleryManage\"},{\"auth_name\":\"\\u76f8\\u518c\\u56fe\\u7247\\u7ba1\\u7406\",\"auth_sn\":\"galleryPicManage\"},{\"auth_name\":\"\\u89c6\\u9891\\u7ba1\\u7406\",\"auth_sn\":\"galleryVideoManage\"}]', `route_link` = 'setting/config/base/', `authority_ico` = 'iconfont-tig icon-shangchengshezhi', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 39;

UPDATE `authority` SET `authority_sn` = 'shipping', `authority_name` = '配送设置组', `parent_id` = 15, `sort_order` = 51, `is_show` = 1, `child_auth` = '[]', `route_link` = 'setting/shipping/', `authority_ico` = 'iconfont-tig icon-peisongshezhi', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 55;

UPDATE `authority` SET `authority_sn` = 'themeStyleManage', `authority_name` = '主题风格', `parent_id` = 9, `sort_order` = 49, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"themeCategoryDecorateManage\"}]', `route_link` = 'decorate/theme_style/info/', `authority_ico` = 'iconfont-tig icon-zhutifengge', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 246;

UPDATE `authority` SET `authority_sn` = 'AfterSalesServiceManage', `authority_name` = '售后服务设置', `parent_id` = 38, `sort_order` = 55, `is_show` = 0, `child_auth` = '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"settingSaveAfterSalesManage\"}]', `route_link` = 'setting/config/after_sales_service/', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 267;

UPDATE `authority` SET `authority_sn` = 'a', `authority_name` = '配送设置', `parent_id` = 55, `sort_order` = 50, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"settingSaveShippingManage\"}]', `route_link` = 'setting/config/logistics/', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 10000384;

UPDATE `authority` SET `authority_sn` = 'baseBasicManage', `authority_name` = '基础信息', `parent_id` = 39, `sort_order` = 50, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"saveBasicManage\"}]', `route_link` = '', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 10000397;

UPDATE `authority` SET `authority_sn` = 'baseProductManage', `authority_name` = '商品设置', `parent_id` = 39, `sort_order` = 50, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"saveSettingProductManage\"}]', `route_link` = '', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 10000398;

UPDATE `authority` SET `authority_sn` = 'baseShoppingManage', `authority_name` = '交易设置', `parent_id` = 39, `sort_order` = 50, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"saveSettingShoppingManage\"}]', `route_link` = '', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 10000399;

UPDATE `authority` SET `authority_sn` = 'baseOrderManage', `authority_name` = '订单设置', `parent_id` = 39, `sort_order` = 50, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"saveSettingsOrderManage\"}]', `route_link` = '', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 10000400;

UPDATE `authority` SET `authority_sn` = 'baseNoticeManage', `authority_name` = '通知设置', `parent_id` = 10000405, `sort_order` = 50, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"saveSettingNotifyManage\"}]', `route_link` = '', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 10000401;

UPDATE `authority` SET `authority_sn` = 'baseServiceManage', `authority_name` = '客服设置', `parent_id` = 39, `sort_order` = 50, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"saveSettingKefuManage\"}]', `route_link` = '', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 10000402;

UPDATE `authority` SET `authority_sn` = 'globalSetting', `authority_name` = '全局设置', `parent_id` = 38, `sort_order` = 50, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"saveSettingsGlobalManage\"}]', `route_link` = '', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 10000403;

UPDATE `authority` SET `authority_sn` = 'loginSetting', `authority_name` = '登录设置', `parent_id` = 38, `sort_order` = 51, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"saveSettingsSaveLoginManage\"}]', `route_link` = '', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 10000420;

UPDATE `authority` SET `authority_sn` = 'paymentSetting', `authority_name` = '支付设置', `parent_id` = 38, `sort_order` = 52, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"uploadFileModifyManage\"}]', `route_link` = '', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 10000439;

UPDATE `authority` SET `authority_sn` = 'userAuthenticationManage', `authority_name` = '认证设置', `parent_id` = 10000423, `sort_order` = 1, `is_show` = 1, `child_auth` = '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"saveAuthSettingsManage\"}]', `route_link` = '', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 10000460;

INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'customerTransactionsManage', '客户成交', '10000360', '50', '1', '[]', '', '', '0', 'admin'
WHERE NOT EXISTS (
    SELECT 1 FROM `authority` WHERE `authority_sn` = 'customerTransactionsManage'
);

-- Step 1：完整备份结构 + 数据（推荐方式）
CREATE TABLE bak_salesman LIKE salesman;
INSERT INTO bak_salesman SELECT * FROM salesman;
CREATE TABLE bak_salesman_customer LIKE salesman_customer;
INSERT INTO bak_salesman_customer SELECT * FROM salesman_customer;
CREATE TABLE bak_salesman_group LIKE salesman_group;
INSERT INTO bak_salesman_group SELECT * FROM salesman_group;
CREATE TABLE bak_salesman_order LIKE salesman_order;
INSERT INTO bak_salesman_order SELECT * FROM salesman_order;
CREATE TABLE bak_salesman_product LIKE salesman_product;
INSERT INTO bak_salesman_product SELECT * FROM salesman_product;

-- Step 2：清空表
TRUNCATE TABLE salesman;
TRUNCATE TABLE salesman_customer;
TRUNCATE TABLE salesman_group;
TRUNCATE TABLE salesman_order;
TRUNCATE TABLE salesman_product;

-- Step 3：设置自增 ID 从 10000 开始
ALTER TABLE salesman AUTO_INCREMENT = 10000;
ALTER TABLE salesman_customer AUTO_INCREMENT = 10000;
ALTER TABLE salesman_group AUTO_INCREMENT = 10000;
ALTER TABLE salesman_order AUTO_INCREMENT = 10000;
ALTER TABLE salesman_product AUTO_INCREMENT = 10000;


UPDATE `salesman_config` SET `data` = '{"code":"salesmanConfig","level":[{"condition":{"inviteSales":{"checked":false,"value":"2"},"salesAmount":{"checked":false,"value":"2"},"salesInviteUsers":{"checked":false,"value":"2"},"selfBuyAmount":{"checked":false,"value":"2"}},"downSalesmanRate":"1","id":1,"name":"普通分销员","rate":"1"},{"condition":{"salesAmount":{"checked":false,"title":"推广金额","unit":"元","value":"20"},"salesInviteUsers":{"checked":true,"title":"发展客户数","unit":"人","value":"5"},"selfBuyAmount":{"checked":false,"title":"自购金额","unit":"元","value":"10"}},"downSalesmanRate":"3","id":2,"name":"精品分销员","rate":"2"},{"condition":{"salesAmount":{"checked":true,"disabled":false,"title":"推广金额","unit":"元","value":"21"},"salesInviteUsers":{"checked":true,"disabled":true,"title":"发展客户数","unit":"人","value":"7"},"selfBuyAmount":{"checked":true,"disabled":false,"title":"自购金额","unit":"元","value":"21"}},"downSalesmanRate":"4","id":3,"name":"稀有分销员","rate":"3"}],"saleType":1}' WHERE `code` = 'salesmanConfig' AND `shop_id` = 0;

DELETE FROM `mail_templates` WHERE template_code = 'register_code';
INSERT INTO `mail_templates` (`template_code`, `is_html`, `template_subject`, `template_content`, `last_modify`, `last_send`, `type`) VALUES ('register_code', 1, '登录注册验证码', '<p>验证码：{$code}（邮箱绑定，请完成验证），如非本人操作，请忽略本消息</p>', 1750660659, 0, 'template');

-- 删除已存在的记录
DELETE FROM authority WHERE authority_sn = 'appVersionManage';
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES ('appVersionManage', 'APP版本', 38, 53, 1, '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"appVersionUpdateManage\"}]', 'setting/app-version/list', '', 0, 'admin');

update config set biz_val = '1' where biz_code = 'poweredBy'