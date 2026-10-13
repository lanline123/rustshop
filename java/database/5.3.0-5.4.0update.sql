-- 为用户收货地址表添加经纬度字段
ALTER TABLE `user_address`
    ADD COLUMN `latitude`  DECIMAL(10, 6) DEFAULT NULL COMMENT '纬度',
    ADD COLUMN `longitude` DECIMAL(10, 6) DEFAULT NULL COMMENT '经度';

-- 同城配送表
CREATE TABLE `shop_local_delivery_config`
(
    `shop_local_delivery_config_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增ID',
    `shop_id`                       INT            NOT NULL COMMENT '门店ID',
    `enabled`                       TINYINT        NOT NULL DEFAULT 0 COMMENT '是否开启同城配送 0=关闭 1=开启',
    `delivery_radius`               DECIMAL(8, 2)  NOT NULL DEFAULT 0.00 COMMENT '配送半径（公里）',
    `min_order_amount`              DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '起送价（元）',
    `base_delivery_fee`             DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '基础运费（元）',
    `distance_pricing_enabled`      TINYINT        NOT NULL DEFAULT 0 COMMENT '是否开启叠加溢价 0=关闭 1=开启',
    `add_time`                      INT UNSIGNED   NOT NULL DEFAULT 0 COMMENT '添加时间',
    UNIQUE KEY `uniq_shop_id` (`shop_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC
    COMMENT = '门店同城配送配置表';

-- 同城配送叠加溢价表
CREATE TABLE `shop_local_delivery_distance_pricing`
(
    `shop_local_delivery_distance_pricing_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增ID',
    `shop_local_delivery_config_id`           BIGINT         NOT NULL COMMENT '门店配送配置ID，关联 shop_local_delivery_config.shop_local_delivery_config_id',
    `distance_threshold`                      DECIMAL(8, 2)  NOT NULL COMMENT '超过多少公里',
    `distance_unit`                           DECIMAL(8, 2)  NOT NULL COMMENT '每增加多少公里',
    `additional_fee`                          DECIMAL(10, 2) NOT NULL COMMENT '配送增加多少元',
    `sort_order`                              INT            NOT NULL DEFAULT 0 COMMENT '排序，数值越小越优先',
    `add_time`                                INT UNSIGNED   NOT NULL DEFAULT 0 COMMENT '添加时间',
    KEY `idx_config_id` (`shop_local_delivery_config_id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  ROW_FORMAT = DYNAMIC
    COMMENT = '门店同城配送距离叠加溢价表';

-- config
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('storeSelfOperatedName', '官方自营', 1755840068, 1, 'admin', 1755840068, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('storeSelfOperatedLogo', '', 1755840068, 1, 'admin', 1755840068, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('storeSelfOperatedRegion', '', 1755840068, 1, 'admin', 1755840068, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('storeSelfOperatedAddress', '', 1755840068, 1, 'admin', 1755840068, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('storeSelfOperatedLongitude', '', 1755840068, 1, 'admin', 1755840068, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`,
                      `update_by_id`, `update_by_name`, `is_del`)
VALUES ('storeSelfOperatedLatitude', '', 1755840068, 1, 'admin', 1755840068, 1, 'admin', 0);


-- auth 表相关修改 admin 中相关编辑权限控制
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`,
                         `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (20000070, 'storeIntraCityDeliveryManage', '同城配送', 20000028, 50, 1,
        '[{"authName":"编辑","authSn":"storeIntraCityDeliveryUpdateManage"}]', '', '', 0, 'store');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (20000163, 'intraCityDeliveryManage', '同城配送', 55, 50, 1, '[]', '', '', 0, 'admin');

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"translationsModifyManage\"},{\"authName\":\"一键翻译\",\"authSn\":\"translationsMultipleManage\"}]'
WHERE `authority_sn` = 'translationContentManage';


delete from authority where authority_sn = 'mailManage';
delete from authority where authority_sn = 'mailTemplateManage';
ALTER TABLE `message_type`
    ADD COLUMN `is_wx_work` tinyint(1) NOT NULL DEFAULT -1 COMMENT '是否支持企业微信';

ALTER TABLE `message_type`
    ADD COLUMN `is_mail` tinyint(1) NOT NULL DEFAULT -1 COMMENT '是否支持邮件';

update message_type set is_wx_work = -1 ,is_mail = -1 ,is_ding = -1 where message_id = 1;
update message_type set is_wx_work = -1 ,is_mail = 1 ,is_ding = -1 where message_id = 2;
update message_type set is_wx_work = -1 ,is_mail = 1 ,is_ding = -1 where message_id = 3;
update message_type set is_wx_work = -1 ,is_mail = 1 ,is_ding = -1 where message_id = 4;
update message_type set is_wx_work = 1 ,is_mail = -1 ,is_ding = 1 where message_id = 5;
update message_type set is_wx_work = 1 ,is_mail = -1 ,is_ding = 1 where message_id = 6;
update message_type set is_wx_work = -1 ,is_mail = 1 ,is_ding = -1 where message_id = 7;
update message_type set is_wx_work = -1 ,is_mail = 1 ,is_ding = -1 where message_id = 8;
update message_type set is_wx_work = -1 ,is_mail = 1 ,is_ding = -1 where message_id = 9;
update message_type set is_wx_work = -1 ,is_mail = 1 ,is_ding = -1 where message_id = 10;
update message_type set is_wx_work = -1 ,is_mail = 1 ,is_ding = -1 where message_id = 11;

UPDATE `message_type` SET `name` = '会员下单商家信息', `describe` = '会员下单商家信息内容', `send_type` = 2, `is_wechat` = -1, `is_mini_program` = -1, `is_message` = -1, `is_msg` = 0, `is_app` = -1, `is_ding` = 1, `add_time` = 0, `is_wx_work` = 1, `is_mail` = -1 WHERE `message_id` = 5;
UPDATE `message_type` SET `name` = '会员支付完成商家信息', `describe` = '会员支付完成商家信息内容', `send_type` = 2, `is_wechat` = -1, `is_mini_program` = -1, `is_message` = -1, `is_msg` = 0, `is_app` = -1, `is_ding` = 1, `add_time` = 0, `is_wx_work` = 1, `is_mail` = -1 WHERE `message_id` = 6;

delete from message_template;
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (1, 2, 2, '支付成功通知', '', '7-mNFyRknyvJSPLIszd_yUjYe9oNau0qIsQoO8NZspc', '1081', '订单编号:{{character_string2.DATA}}\n下单时间:{{time1.DATA}}\n订单金额:{{amount4.DATA}}\n');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (2, 2, 1, '支付成功通知', '', 'AD-U53hemd9YaieF3GvEBXO4lF2F_pYTtnLxsMoolx0', '', '订单编号:{{character_string3.DATA}}\n支付时间:{{time7.DATA}}\n订单金额:{{amount4.DATA}}\n');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (3, 3, 1, '发货通知', '', 'Td9lSNss3CG5e_fVG2anMe-FO97lyxecBW7BvgNC24g', '', '物流公司:{{thing21.DATA}}\n发货单号:{{character_string18.DATA}}\n发货时间:{{time3.DATA}}\n收货人:{{thing17.DATA}}\n订货单号:{{character_string2.DATA}}\n');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (4, 3, 2, '发货通知', '', 'dK57qJ_x7Mspnb75XALYp-7hQAako7ze0bhvgDqYnL8', '1138', '快递公司:{{thing4.DATA}}\n快递单号:{{character_string5.DATA}}\n发货时间:{{date3.DATA}}\n收货人:{{thing8.DATA}}\n订单号:{{character_string2.DATA}}\n');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (5, 2, 4, '用户支付成功通知', '', '', '', '用户支付成功通知');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (6, 2, 3, '支付成功通知', '', 'SMS_129744372', '', '购物提醒，您已成功完成订单${order}支付，请留意我们的发货通知');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (7, 3, 3, '发货通知', '', 'SMS_491310207', '', '亲爱的客户，您购买的货物已通过${shipping_name}发出，单号${code}，请保持手机畅通，确认货物是否完好后签收哦，如有问题请联系我们的客服，我们将及时为您处理，再次感谢惠顾');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (8, 3, 4, '发货通知', '', '', '', '您的订单{order_sn}已经发货，物流公司：{logistics_name}，快递单号：{tracking_no}');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (9, 1, 1, '用户下单通知', '', 'dWLBCehYyGXpTk9uief4hcdp6mcahTYGtuLhpZLmmMc', '', '{{first.DATA}}\r\n下单时间：{{keyword1.DATA}}\r\n订单状态：{{keyword2.DATA}}\r\n{{remark.DATA}}');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (10, 4, 1, '退款成功通知', '', 'pjG63un863hh9rQyXt9P8BUTWJO3USgDbBELVtu3Kps', '', '订单编号:{{character_string5.DATA}}\n退款金额:{{amount2.DATA}}\n');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (11, 1, 3, '用户下单通知', '', 'SMS_491435136', '', '您有一个新的订单，订单号{order_sn},请及时支付，超时则会自动取消订单！');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (12, 1, 2, '用户下单通知', '', '', '', '您有一个新的订单，订单号{order_sn},请及时支付，超时则会自动取消订单！');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (13, 1, 4, '会员下单时发送短信', '', '', '', '您有一个新的订单，订单号{order_sn},请及时支付，超时则会自动取消订单！');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (14, 4, 2, '退款成功通知', '', '', '', '退款成功通知内容');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (15, 4, 3, '退款成功通知', '', 'SMS_491345158', '', '亲爱的客户，您购买的货物（订单号：${order}）已完成退款，如有问题请联系我们的客服，我们将及时为您处理，再次感谢惠顾。');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (16, 4, 4, '退款成功', '', '', '', '您的订单{order_sn}已完成退款，祝您购物愉快！');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (17, 5, 3, '会员下单商家短信', '', 'SMS_129764380', '', '有新的会员购物提醒，订单号${order}，订单额${fee}，请及时关注 ');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (18, 6, 3, '会员支付完成商家短信', '', 'SMS_129764381', '', '支付提醒，订单号${order}已经付款，订单额${fee}，提醒你及时发货 ');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (19, 7, 3, '发票开具成功短信通知', '', 'SMS_491440089', '', '尊敬的客户，您申请的发票已开具成功，请前往发票申请详情查看');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (20, 8, 3, '获取验证码', '', 'SMS_129764389', '', '验证码：${code}（手机绑定，请完成验证），如非本人操作，请忽略本短信');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (21, 6, 6, '会员支付完成商家钉钉', '08373455471186431,281061486728962258', '', '', '支付提醒，订单号${order}已经付款，订单额${fee}，提醒你及时发货');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (22, 5, 6, '会员下单商家短信', '281061486728962258,08373455471186431', '', '', '有新的会员购物提醒，订单号${order}，订单金额${fee}，请及时关注');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (23, 9, 3, '会员实名认证通知', '', 'SMS_475945968', '', '认证通知：您的${type_text}认证申请已受理,将在{$num}个工作日内完成审核,请耐心等待。');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (24, 10, 3, '商家入驻成功', '', 'SMS_476770016', '', '您的商家入驻申请审核成功，请到商户入驻页面查看。登录账号：${username}，初始密码：${password}。');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (25, 11, 3, '商家入驻审核失败', '', 'SMS_476705011', '', '您的商家入驻申请审核失败，请到商家入驻申请页面查看。');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (26, 11, 7, '商家入驻审核失败', '', '', '', '${merchantName}您申请入驻${shopName}的申请审核未通过，请修改信息重新发起申请');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (30, 8, 7, '获取验证码', '', '', '', '验证码：${code}（邮箱绑定，请完成验证），如非本人操作，请忽略本短信');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (31, 6, 8, '会员支付完成商家企业微信', '', '', '', '支付提醒，订单号${order}已经付款，订单额${fee}，提醒你及时发货');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (32, 7, 7, '发票开具成功短信通知', '', '', '', '尊敬的客户，您申请的发票已开具成功，请前往发票申请详情查看。');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (33, 5, 7, '会员下单商家短信', '', '', '', '有新的会员购物提醒，订单号${order}，订单金额${fee}，请及时关注');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (34, 5, 8, '会员下单商家短信', '', '', '', '有新的会员购物提醒，订单号${order}，订单金额${fee}，请及时关注');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (35, 4, 7, '退款成功通知', '', '', '', '(${shopTitle} )亲爱的${userName}，您的售后申请(${afterSaleSn}) 已退款成功,${totalRefund}元退款已返还至您的原有支付账户，请注意查收。退款时间:${refundTime}');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (36, 3, 7, '发货通知', '', '', '', '尊敬的客户，您购买的货物已通过${shippingName}发出，单号${code}，请保持手机畅通，确认货物是否完好后签收哦，如有疑问请联系客服，我们将及时为您处理，再次感谢惠顾');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (37, 2, 7, '支付成功通知', '', '', '', '(${shopTitle})亲爱的${userName}，您购买的${productName}已支付成功，将尽快为您发货。');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (38, 1, 7, '会员下单时发送邮件', '', '', '', '您有一个新的订单，订单号{order_sn},请及时支付，超时则会自动取消订单！');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (39, 9, 7, '会员实名认证通知', '', '', '', '认证通知：（${shopTitle}）亲爱的${userNickName}，您的实名认证已（${state}），请前往商城查看！');
INSERT INTO `message_template` (`id`, `message_id`, `type`, `template_name`, `to_userid`, `template_id`, `template_num`, `content`) VALUES (40, 10, 7, '商家入驻成功', '', '', '', '（${merchantName}）您的入驻申请审核成功，请到商户入驻页面查看。登录账号：${username}，初始密码：${password}。');
UPDATE `message_type` SET `name` = '商家入驻审核失败', `describe` = '商家入驻审核失败', `send_type` = 1, `is_wechat` = -1, `is_mini_program` = -1, `is_message` = -1, `is_msg` = 1, `is_app` = -1, `is_ding` = -1, `add_time` = -1, `is_wx_work` = -1, `is_mail` = 1 WHERE `message_id` = 11;