UPDATE `config` set `biz_code`='paypalClientSecret' where `biz_code`='paypalSecret';

DELETE FROM `authority` WHERE `authority_id` = 73;

UPDATE `authority` SET `authority_sn` = 'userLevelProManage', `authority_name` = '会员等级设置', `parent_id` = 11, `sort_order` = 50, `is_show` = 1, `child_auth` = '[]', `route_link` = 'user/level_manage_pro/list/', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 10000391;UPDATE `salesman_config` SET `shop_id` = 0, `code` = 'salesmanConfig' WHERE `id` = 2;
UPDATE `salesman_config` SET `shop_id` = 0, `code` = 'salesmanSettlement' WHERE `id` = 3;UPDATE `product_services` SET `product_service_name` = '售全球', `product_service_desc` = '支持收货地址为海外或港澳台地区', `ico_img` = 'https://oss.tigshop.com/img/gallery/202304/1680596906ioRSYX7aZHD2kJO7Rw!!pic.jpeg', `sort_order` = 50, `default_on` = 1, `shop_id` = 0 WHERE `product_service_id` = 1;
UPDATE `product_services` SET `product_service_name` = '商家发货&售后', `product_service_desc` = '由商家发货并提供售后服务', `ico_img` = 'https://oss.tigshop.com/img/gallery/202304/1680596906Am3McoaZDF4efyJVZc!!pic.jpeg', `sort_order` = 50, `default_on` = 0, `shop_id` = 0 WHERE `product_service_id` = 2;
UPDATE `product_services` SET `product_service_name` = '准时到达', `product_service_desc` = '选择准时到达服务，可以指定精确时间点收货哦。1231', `ico_img` = 'https://oss.tigshop.com/img/gallery/202304/1680596906tSvhiOycqCTztQvOnp!!pic.jpeg', `sort_order` = 50222, `default_on` = 1, `shop_id` = 0 WHERE `product_service_id` = 3;
UPDATE `product_services` SET `product_service_name` = '7天无理由退换货', `product_service_desc` = '7天无理由退换货，给客户最好的售后', `ico_img` = 'https://oss.tigshop.com/img/gallery/202403/17103019518SbHBxjrEQPjO4mLSc.jpg', `sort_order` = 222, `default_on` = 1, `shop_id` = 0 WHERE `product_service_id` = 5;
UPDATE `product_services` SET `product_service_name` = '24小时发货', `product_service_desc` = '快速发货', `ico_img` = 'https://oss.tigshop.com/img/gallery/202403/1710301951pyZQRtpigD0RS6KJ8I.jpeg', `sort_order` = 50, `default_on` = 1, `shop_id` = 0 WHERE `product_service_id` = 6;
UPDATE `product_services` SET `product_service_name` = '7天无理由退换货', `product_service_desc` = '七天无理由，24H发货', `ico_img` = 'https://oss.tigshop.com/img/gallery/202403/1710301951OCapOZNx0zbzfQoFxj.jpeg', `sort_order` = 50, `default_on` = 1, `shop_id` = 0 WHERE `product_service_id` = 8;

DROP TABLE IF EXISTS `authority`;
CREATE TABLE `authority`  (
                              `authority_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增id号',
                              `authority_sn` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '权限编号',
                              `authority_name` varchar(90) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '权限名称',
                              `parent_id` int(8) UNSIGNED NOT NULL DEFAULT 0 COMMENT '该权限的父类ID',
                              `sort_order` smallint(4) UNSIGNED NOT NULL DEFAULT 50 COMMENT '该权限在目录显示的的顺序,数字越大顺序越靠后,同数字,id在前的先显示',
                              `is_show` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否在目录显示 1显示; 0不显示',
                              `child_auth` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'JSON:子权限[{authName|authSn}]',
                              `route_link` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '路由链接',
                              `authority_ico` varchar(80) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '\r\nICO图标',
                              `is_system` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否系统目录，是的话只能有限编辑或隐藏，不能删除',
                              `admin_type` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT 'admin' COMMENT 'admin管理后台使用shop店铺使用',
                              PRIMARY KEY (`authority_id`) USING BTREE,
                              INDEX `authority_sn`(`authority_sn`) USING BTREE,
                              INDEX `parent_id`(`parent_id`) USING BTREE,
                              INDEX `is_show`(`is_show`) USING BTREE,
                              INDEX `sort_order`(`sort_order`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10000397 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '商城后台权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of authority
-- ----------------------------
INSERT INTO `authority` VALUES (1, 'panel', '面板', 0, 50, 1, '[]', 'panel/index/', 'iconfont-tig icon-jingying', 0, 'admin');
INSERT INTO `authority` VALUES (2, 'product', '商品', 0, 51, 1, '[]', 'product/list/', 'iconfont-tig icon-shangpin1', 0, 'admin');
INSERT INTO `authority` VALUES (3, 'productManage', '商品列表', 2, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"productModifyManage\"}]', 'product/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (4, 'categoryManage', '商品类目', 2, 52, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"productCategoryModifyManage\"}]', 'product/category/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (5, 'promotion', '营销', 0, 53, 1, '[]', 'promotion/manage/list/', 'iconfont-tig icon-maiyizengyi', 0, 'admin');
INSERT INTO `authority` VALUES (6, 'consoleManage', '控制台', 1, 50, 1, '[]', 'panel/index/', '', 0, 'admin');
INSERT INTO `authority` VALUES (8, 'order', '订单', 0, 54, 1, '[]', 'order/list/', 'iconfont-tig icon-dingdan1', 0, 'admin');
INSERT INTO `authority` VALUES (9, 'decorate', '装修', 0, 55, 1, '[]', 'decorate/theme_style/info/', 'iconfont-tig icon-zhuangxiu', 0, 'admin');
INSERT INTO `authority` VALUES (10, 'content', '内容', 0, 56, 1, '[]', 'content', 'iconfont-tig icon-gongsiqiye', 0, 'admin');
INSERT INTO `authority` VALUES (11, 'user', '会员', 0, 57, 1, '[]', 'user', 'iconfont-tig icon-huiyuan3', 0, 'admin');
INSERT INTO `authority` VALUES (12, 'finance', '财务', 0, 58, 1, '[]', 'finance/account_panel/list/', 'iconfont-tig icon-shuju', 0, 'admin');
INSERT INTO `authority` VALUES (14, 'authority', '权限', 0, 60, 1, '[]', 'authority/list/', 'iconfont-tig icon-quanxian1', 0, 'admin');
INSERT INTO `authority` VALUES (15, 'setting', '设置', 0, 61, 1, '[]', 'setting/config/base/', 'iconfont-tig icon-shezhi', 0, 'admin');
INSERT INTO `authority` VALUES (17, 'authorityManage', '权限目录', 14, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"authorityUpdateManage\"},{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"authorityDelManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"authorityBatchManage\"}]', 'authority/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (18, 'brandManage', '商品品牌', 2, 51, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"brandModifyManage\"}]', 'product/brand/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (19, 'productBatch', '商品批量处理', 2, 56, 1, '[]', 'product/product_batch/', '', 0, 'admin');
INSERT INTO `authority` VALUES (26, 'commentManage', '评价管理', 2, 53, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"commentModifyManage\"}]', 'product/comment/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (27, 'mobileDecorate', '移动端装修', 9, 50, 1, '[]', '-', '', 0, 'admin');
INSERT INTO `authority` VALUES (28, 'mobileDecorateManage', '页面管理', 27, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"mobileDecorateModifyManage\"}]', 'decorate/mobile_decorate/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (29, 'productAttributesTplManage', '属性模板', 2, 53, 1, '[{\"authName\":\"\\u5546\\u54c1\\u5c5e\\u6027\\u6a21\\u677f\\u7f16\\u8f91\",\"authSn\":\"productAttributesTplModifyManage\"},{\"authName\":\"\\u5546\\u54c1\\u5c5e\\u6027\\u7f16\\u8f91\",\"authSn\":\"productAttributesGroupModifyManage\"}]', 'product/product_attributes_tpl/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (30, 'productServicesManage', '商品服务列表', 2, 54, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"productServicesModifyManage\"}]', 'product/product_services/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (31, 'orderManage', '订单列表', 8, 50, 1, '[{\"authName\":\"\\u53d1\\u8d27\",\"authSn\":\"orderDeliverManage\"},{\"authName\":\"\\u6536\\u8d27\",\"authSn\":\"orderConfirmReceiptManage\"},{\"authName\":\"\\u4fee\\u6539\\u6536\\u8d27\\u4eba\",\"authSn\":\"orderModifyConsigneeManage\"},{\"authName\":\"\\u4fee\\u6539\\u914d\\u9001\\u4fe1\\u606f\",\"authSn\":\"orderModifyShippingManage\"},{\"authName\":\"\\u4fee\\u6539\\u8ba2\\u5355\\u91d1\\u989d\",\"authSn\":\"orderModifyMoneyManage\"},{\"authName\":\"\\u53d6\\u6d88\\u8ba2\\u5355\",\"authSn\":\"cancelOrderManage\"},{\"authName\":\"\\u8ba2\\u5355\\u786e\\u8ba4\",\"authSn\":\"setConfirmManage\"},{\"authName\":\"\\u8f6f\\u5220\\u9664\",\"authSn\":\"delOrderManage\"},{\"authName\":\"\\u62c6\\u5355\",\"authSn\":\"splitStoreOrderManage\"},{\"authName\":\"\\u8bbe\\u4e3a\\u5df2\\u652f\\u4ed8\",\"authSn\":\"setPaidManage\"},{\"authName\":\"\\u4fee\\u6539\\u5546\\u54c1\\u4fe1\\u606f\",\"authSn\":\"modifyProductManage\"},{\"authName\":\"\\u8bbe\\u7f6e\\u5546\\u5bb6\\u5907\\u6ce8\",\"authSn\":\"setAdminNoteManage\"},{\"authName\":\"\\u6dfb\\u52a0\\u8ba2\\u5355\\u65e5\\u5fd7\",\"authSn\":\"orderLogModifyManage\"}]', 'order/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (32, 'productAutoManage', '商品自动上下架', 2, 56, 0, '[]', 'product/product_auto/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (33, 'productInventoryLogManage', '商品库存日志', 2, 57, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"productInventoryLogModifyManage\"}]', 'product/product_inventory_log/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (34, 'productBatchImageManage', '图片批量处理', 19, 50, 0, '[]', 'product/product_batch/image/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (35, 'productBatchExportManage', '商品批量导出', 19, 51, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"productBatchExportUpdateManage\"}]', 'product/product_batch/export/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (36, 'productBatchAddManage', '商品批量上传', 19, 52, 1, '[{\"authName\":\"\\u6279\\u91cf\\u4e0a\\u4f20\",\"authSn\":\"productBatchModifyManage\"}]', 'product/product_batch/add/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (37, 'productBatchEditManage', '商品批量修改', 19, 54, 1, '[{\"authName\":\"\\u6279\\u91cf\\u4fee\\u6539\",\"authSn\":\"productBatchEditManage\"}]', 'product/product_batch/edit/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (38, 'config', '系统设置', 15, 50, 1, '[]', 'setting/cofnig/', '', 0, 'admin');
INSERT INTO `authority` VALUES (39, 'configManage', '商城设置', 38, 50, 1, '[{\"authName\":\"\\u4fdd\\u5b58\\u5546\\u57ce\\u57fa\\u7840\\u8bbe\\u7f6e\",\"authSn\":\"saveBasicManage\"},{\"authName\":\"\\u57fa\\u7840\\u8bbe\\u7f6e\\u66f4\\u65b0\",\"authSn\":\"settingSaveManage\"},{\"authName\":\"\\u8bbe\\u7f6e\\u7f16\\u8f91\",\"authSn\":\"settingUpdateManage\"},{\"authName\":\"\\u53d1\\u9001\\u6d4b\\u8bd5\\u90ae\\u4ef6\",\"authSn\":\"sendTestEmailModifyManage\"},{\"authName\":\"\\u4e0a\\u4f20api\\u6587\\u4ef6\",\"authSn\":\"uploadFileModifyManage\"},{\"authName\":\"\\u751f\\u6210\\u5e73\\u53f0\\u8bc1\\u4e66\",\"authSn\":\"platformCertificateModifyManage\"},{\"authName\":\"\\u76f8\\u518c\\u4fee\\u6539\",\"authSn\":\"galleryModifyManage\"},{\"authName\":\"\\u76f8\\u518c\\u56fe\\u7247\\u4fee\\u6539\",\"authSn\":\"galleryPicModifyManage\"},{\"authName\":\"\\u76f8\\u518c\\u7ba1\\u7406\",\"authSn\":\"galleryManage\"},{\"authName\":\"\\u76f8\\u518c\\u56fe\\u7247\\u7ba1\\u7406\",\"authSn\":\"galleryPicManage\"}]', 'setting/config/base/', '', 0, 'admin');
INSERT INTO `authority` VALUES (40, 'adminUserManage', '管理员列表', 14, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"adminUserUpdateManage\"},{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"adminUserDelManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"adminUserBatchManage\"},{\"authName\":\"\\u8d26\\u6237\\u4fee\\u6539\",\"authSn\":\"modifyManageAccountsManage\"},{\"authName\":\"\\u6d88\\u606f\\u8bbe\\u7f6e\",\"authSn\":\"adminMsgModifyManage\"}]', 'authority/admin_user/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (41, 'couponManage', '优惠券', 5, 1, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"promotionCouponModifyManage\"}]', 'promotion/coupon/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (44, 'articleManage', '文章列表', 10, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"articleModifyManage\"}]', 'content/article/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (45, 'articleCategoryManage', '文章分类', 10, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"articleCategoryModifyManage\"}]', 'content/article_category/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (46, 'exchangeOrderManage', '积分订单', 8, 50, 1, '[]', 'order/exchange_order/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (48, 'messageLogManage', '站内信推送', 11, 54, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"userMessageLogModifyManage\"}]', 'user/message_log/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (49, 'userManage', '会员列表', 11, 50, 1, '[{\"authName\":\"\\u6dfb\\u52a0\",\"authSn\":\"userCreateManage\"},{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"userModifyManage\"},{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"userDelManage\"},{\"authName\":\"\\u66f4\\u65b0\\u5b57\\u6bb5\",\"authSn\":\"userModifyFieldManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"userBatchManage\"},{\"authName\":\"\\u8d44\\u91d1\\u7ba1\\u7406\",\"authSn\":\"fundManagementManage\"}]', 'user/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (50, 'aftersalesManage', '售后申请', 8, 54, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"aftersalesModifyManage\"}]', 'order/aftersales/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (51, 'orderExportManage', '订单导出', 8, 55, 1, '[]', 'order/order_export/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (52, 'mailManage', '邮箱服务器设置', 38, 51, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"settingSaveMailManage\"}]', 'setting/config/mail/', '', 0, 'admin');
INSERT INTO `authority` VALUES (53, 'mailTemplateManage', '邮件模板设置', 38, 52, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"mailTemplatesUpdateManage\"}]', 'setting/config/mail_template/', '', 0, 'admin');
INSERT INTO `authority` VALUES (54, 'messageTypeManage', '消息设置', 38, 53, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"messageTypeUpdateManage\"},{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"messageTypeDelManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"messageTypeBatchManage\"},{\"authName\":\"\\u751f\\u6210\\u5c0f\\u7a0b\\u5e8f\\u6d88\\u606f\\u6a21\\u677f\",\"authSn\":\"miniProgramMessageTemplateManage\"},{\"authName\":\"\\u540c\\u6b65\\u5c0f\\u7a0b\\u5e8f\\u6d88\\u606f\\u6a21\\u677f\",\"authSn\":\"miniProgramMessageTemplateSyncManage\"},{\"authName\":\"\\u751f\\u6210\\u516c\\u4f17\\u53f7\\u6d88\\u606f\\u6a21\\u677f\",\"authSn\":\"wechatMessageTemplateManage\"},{\"authName\":\"\\u540c\\u6b65\\u516c\\u4f17\\u53f7\\u6d88\\u606f\\u6a21\\u677f\",\"authSn\":\"wechatMessageTemplateSyncManage\"},{\"authName\":\"\\u65e5\\u5fd7\\u7ba1\\u7406\",\"authSn\":\"accessLogModifyManage\"}]', 'setting/config/message_type/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (55, 'shipping', '配送/运费设置', 15, 51, 1, '[]', 'setting/shipping/', '', 0, 'admin');
INSERT INTO `authority` VALUES (56, 'logisticsCompanyManage', '物流公司', 55, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"logisticsCompanyUpdateManage\"},{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"logisticsCompanyDelManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"logisticsCompanyBatchManage\"}]', 'setting/shipping/logistics_company/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (57, 'shippingTypeManage', '配送类型', 55, 51, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"shippingTypeUpdateManage\"},{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"shippingTypeDelManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"shippingTypeBatchManage\"}]', 'setting/shipping/shipping_type/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (58, 'shippingTplManage', '运费模板', 55, 52, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"shippingTplUpdateManage\"},{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"shippingTplDelManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"shippingTplBatchManage\"}]', 'setting/shipping/shipping_tpl/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (59, 'seckillManage', '限时秒杀', 5, 2, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"seckillModifyManage\"}]', 'promotion/seckill/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (60, 'productActivityManage', '优惠活动', 5, 50, 0, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"productPromotionModifyManage\"}]', 'promotion/product_activity/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (61, 'regionManage', '地区列表', 15, 52, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"regionUpdateManage\"},{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"regionDelManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"regionBatchManage\"}]', 'setting/region/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (62, 'pcDecorate', 'PC端装修', 9, 50, 1, '[]', '-', '', 0, 'admin');
INSERT INTO `authority` VALUES (63, 'pcDecorateManage', '首页模板', 62, 50, 1, '[{\"authName\":\"\\u53d1\\u5e03\",\"authSn\":\"decoratePublishManage\"},{\"authName\":\"\\u8bbe\\u4e3a\\u9996\\u9875\",\"authSn\":\"decorateSetHomeManage\"},{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"decorateModifyManage\"},{\"authName\":\"\\u6a21\\u5757\\u7f16\\u8f91\",\"authSn\":\"decorateDiscreteModifyManage\"},{\"authName\":\"\\u5b58\\u5165\\u8349\\u7a3f\",\"authSn\":\"decorateSaveDraftManage\"}]', 'decorate/pc_decorate/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (64, 'appVersionManage', 'APP版本', 15, 56, 0, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"appVersionUpdateManage\"}]', 'setting/app_version/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (65, 'friendLinksManage', '友情链接', 15, 54, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"friendLinksUpdateManage\"},{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"friendLinksDelManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"friendLinksBatchManage\"}]', 'setting/friend_links/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (67, 'pointsExchangeManage', '积分商品', 5, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"promotionPointsExchangeModifyManage\"}]', 'promotion/points_exchange/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (68, 'signInSettingManage', '积分签到', 5, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"signInSettingModifyManage\"}]', 'promotion/sign_in_setting/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (69, 'rechargeSettingManage', '余额充值', 5, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"rechargeSettingModifyManage\"}]', 'promotion/recharge_setting/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (70, 'adminRoleManage', '角色管理', 14, 51, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"adminRoleUpdateManage\"},{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"adminRoleDelManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"adminRoleBatchManage\"}]', 'authority/admin_role/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (71, 'adminLogManage', '管理员日志', 14, 53, 1, '[]', 'authority/admin_log/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (72, 'suppliersManage', '供应商列表', 14, 54, 1, '[{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"suppliersDelManage\"},{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"suppliersUpdateManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"suppliersBatchManage\"}]', 'authority/suppliers/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (74, 'feedbackManage', '会员留言', 11, 53, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"feedbackModifyManage\"}]', 'user/feedback/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (76, 'pcDecorateOtherManage', '其它页面', 62, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"pcDecorateOtherUpdateManage\"}]', 'decorate/pc_decorate/other/', '', 0, 'admin');
INSERT INTO `authority` VALUES (77, 'userRight', '会员权益', 11, 52, 0, '[]', 'user/user_right/', '', 0, 'admin');
INSERT INTO `authority` VALUES (78, 'userTypeManage', '会员类型', 77, 50, 1, '[]', 'user/user_type/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (79, 'userRightManage', '会员权益', 77, 51, 1, '[]', 'user/user_right/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (80, 'agreementManage', '会员协议', 77, 52, 1, '[]', 'user/agreement/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (81, 'integralLogManage', '积分日志', 11, 56, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"userPointsLogModifyManage\"}]', 'user/integral_log/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (82, 'pcNavigationManage', 'PC导航栏', 62, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"pcNavigationModifyManage\"}]', 'decorate/pc_navigation/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (83, 'pcCatFloorManage', 'PC分类抽屉', 62, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"pcCatFloorModifyManage\"}]', 'decorate/pc_cat_floor/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (84, 'accountPanelManage', '账户总览', 12, 50, 1, '[]', 'finance/account_panel/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (85, 'userBalanceLogManage', '余额日志', 12, 53, 1, '[{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"userBalanceLogDelManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"userBalanceLogBatchManage\"}]', 'finance/user_balance_log/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (86, 'userRechargeOrderManage', '充值记录', 12, 52, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"userRechargeOrderUpdateManage\"},{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"userRechargeOrderDelManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"userRechargeOrderBatchManage\"}]', 'finance/user_recharge_order/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (87, 'userWithdrawApplyManage', '提现申请', 12, 51, 1, '[{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"userWithdrawApplyDelManage\"},{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"userWithdrawApplyUpdateManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"userWithdrawApplyBatchManage\"}]', 'finance/user_withdraw_apply/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (88, 'payLogManage', '交易日志', 12, 56, 1, '[{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"payLogDelManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"payLogBatchManage\"}]', 'finance/paylog/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (89, 'refundApplyManage', '退款申请', 12, 55, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"refundApplyUpdateManage\"}]', 'finance/refund_apply/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (90, 'userInvoice', '发票管理', 12, 53, 1, '[]', 'finance/user_invoice/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (91, 'userInvoiceManage', '发票资质管理', 90, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"userInvoiceUpdateManage\"},{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"userInvoiceDelManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"userInvoiceBatchManage\"}]', 'finance/user_invoice/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (92, 'orderInvoiceManage', '发票申请', 90, 51, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"orderInvoiceUpdateManage\"},{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"orderInvoiceDelManage\"},{\"authName\":\"\\u6279\\u91cf\\u64cd\\u4f5c\",\"authSn\":\"orderInvoiceBatchManage\"}]', 'finance/order_invoice/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (241, 'statisticsOrder', '销售统计', 1, 50, 1, '[]', 'panel/statistics_order/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (246, 'themeStyleManage', '主题风格', 9, 49, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"themeStyleUpdateManage\"}]', 'decorate/theme_style/info/', '', 0, 'admin');
INSERT INTO `authority` VALUES (250, 'paymentManage', '支付设置', 38, 100, 1, '[]', 'setting/config/payment', '', 0, 'admin');
INSERT INTO `authority` VALUES (251, 'statisticsSale', '销售明细', 1, 50, 1, '[]', 'panel/statistics_sale/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (252, 'saleTargets', '销售指标', 1, 50, 1, '[]', 'panel/sale_targets/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (253, 'statisticsGoods', '销售排行', 1, 50, 1, '[]', 'panel/statistics_goods/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (254, 'statisticsUser', '用户统计', 1, 50, 1, '[]', 'panel/statistics_user/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (255, 'statisticsAccess', '访问统计', 1, 50, 1, '[]', 'panel/statistics_access/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (256, 'consumerRanking', '消费排行', 1, 50, 1, '[]', 'panel/consumer_ranking/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (257, 'newMembers', '新增会员', 1, 50, 1, '[]', 'panel/new_members/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (259, 'mobileCatNavManage', '首页分类栏', 27, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"mobileCatNavModifyManage\"}]', 'decorate/mobile_cat_nav/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (262, 'ziyin', '自营店铺1', 261, 50, 1, '[]', 'ziying/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (263, 'xieyi', '协议', 0, 63, 0, '[]', 'xieyi/', 'lyecs-defined-iconfont lyecs-defined-icon-dianshi', 0, 'admin');
INSERT INTO `authority` VALUES (264, 'xiey', '协议1', 263, 50, 1, '[]', 'xiey/', '', 0, 'admin');
INSERT INTO `authority` VALUES (266, 'cesh', '测试1', 265, 50, 1, '[]', 'cesh', '', 0, 'admin');
INSERT INTO `authority` VALUES (267, 'AfterSalesServiceManage', '售后服务设置', 38, 55, 1, '[]', 'setting/config/after_sales_service/', '', 0, 'admin');
INSERT INTO `authority` VALUES (268, 'mobileNavManage', '导航栏设置', 27, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\\u5bfc\\u822a\\u680f\",\"authSn\":\"mobileNavUpdateManage\"}]', 'decorate/mobile_decorate/nav', '', 0, 'admin');
INSERT INTO `authority` VALUES (276, 'adminMerchant', '商户', 0, 54, 1, '[]', 'adminMerchant/apply/list/', 'iconfont-tig icon-31quanbushangpin', 0, 'admin');
INSERT INTO `authority` VALUES (278, 'applyManage', '入驻申请', 276, 49, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"merchantApplyModifyManage\"}]', 'adminMerchant/apply/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (279, 'adminMerchantManage', '商户管理', 276, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"merchantModifyManage\"},{\"authName\":\"\\u8d26\\u6237\\u7f16\\u8f91\",\"authSn\":\"accountModifyManage\"}]', 'adminMerchant/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (280, 'adminMerchantSettingsManage', '商户设置', 276, 51, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"adminMerchantSettingsUpdateManage\"}]', 'adminMerchant/merchantSettings/info', '', 0, 'admin');
INSERT INTO `authority` VALUES (282, 'shopManage', '店铺列表', 281, 50, 1, '[]', 'shop/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (283, 'accountManage', '店铺资金列表', 281, 50, 1, '[]', 'shop/account/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (284, 'refundLogManage', '退款记录', 12, 56, 1, '[]', 'finance/refund_log/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10001, 'panel', '面板', 0, 50, 1, '[]', 'panel/index/', 'iconfont-tig icon-jingying', 0, 'shop');
INSERT INTO `authority` VALUES (10002, 'product', '商品', 0, 51, 1, '[]', 'product/list/', 'iconfont-tig icon-shangpin1', 0, 'shop');
INSERT INTO `authority` VALUES (10005, 'promotion', '营销', 0, 56, 1, '[]', 'promotion/coupon/list/', 'iconfont-tig icon-maiyizengyi', 0, 'shop');
INSERT INTO `authority` VALUES (10008, 'order', '订单', 0, 52, 1, '[]', 'order/list/', 'iconfont-tig icon-dingdan1', 0, 'shop');
INSERT INTO `authority` VALUES (10000285, 'productManage', '商品列表', 10002, 50, 1, '[]', 'product/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000286, 'productGroupManage', '商品分组', 2, 52, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"productGroupModifyManage\"}]', 'product/product_group/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000287, 'shopAccountRaplyManage', '提现申请', 281, 50, 1, '[]', 'shop/shopAccountRaply/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000291, 'capitalfund', '资产', 0, 53, 1, '[]', 'capitalfund/dashboard/index/', 'iconfont-tig icon-31tianmaobao', 0, 'shop');
INSERT INTO `authority` VALUES (10000292, 'dashboardManage', '资产总览', 10000291, 50, 1, '[]', 'capitalfund/dashboard/index/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000293, 'balanceManage', '店铺余额', 10000291, 50, 1, '[]', 'capitalfund/balance/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000294, 'withdrawManage', '提现', 10000291, 50, 1, '[]', 'capitalfund/withdraw/index/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000296, 'mobileShopDecorate', '页面管理', 10000311, 1, 1, '[]', 'merchant_setting/mobile_shop_decorate', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000297, 'consoleManage', '控制台', 10001, 50, 1, '[]', 'panel/index/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000298, 'statisticsOrder', '销售统计', 10001, 50, 1, '[]', 'panel/statistics_order/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000299, 'statisticsSale', '销售明细', 10001, 50, 1, '[]', 'panel/statistics_sale/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000300, 'statisticsGoods', '销售排行', 10001, 50, 1, '[]', 'panel/statistics_goods/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000301, 'shopProductCategoryManage', '商品分类', 10002, 50, 1, '[]', 'product/shop_product_category/list', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000302, 'orderManage', '订单管理', 10008, 50, 1, '[]', 'order/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000303, 'aftersalesManage', '售后管理', 10008, 50, 1, '[]', 'order/aftersales/list', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000304, 'orderExportManage', '订单导出', 10008, 50, 1, '[]', 'order/order_export/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000305, 'productGroupManage', '商品分组', 10002, 50, 1, '[]', 'product/product_group/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000306, 'shop', '店铺', 0, 54, 1, '[]', 'shop/list/', 'iconfont-tig icon-guanli', 0, 'admin');
INSERT INTO `authority` VALUES (10000307, 'shopManage', '店铺管理', 10000306, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"merchantShopModifyManage\"},{\"authName\":\"\\u5e97\\u94fa\\u5546\\u54c1\\u5206\\u7c7b\\u7f16\\u8f91\",\"authSn\":\"merchantShopProductCategoryModifyManage\"},{\"authName\":\"\\u5e97\\u94fa\\u5458\\u5de5\\u7f16\\u8f91\",\"authSn\":\"adminUserShopModifyManage\"}]', 'shop/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000308, 'shopAccountManage', '资金概览', 10000306, 50, 1, '[]', 'shop/shopAccount/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000309, 'shopAccountRaplyManage', '提现申请', 10000306, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"merchantShopWithdrawModifyManage\"}]', 'shop/shopAccountRaply/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000311, 'merchantSetting', '设置', 0, 57, 1, '[]', 'merchant_setting/mobile_shop_decorate', 'iconfont-tig icon-31shezhi', 0, 'shop');
INSERT INTO `authority` VALUES (10000312, 'couponManage', '优惠券', 10005, 1, 1, '[]', 'promotion/coupon/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000313, 'seckillManage', '限时秒杀', 10005, 2, 1, '[]', 'promotion/seckill/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000314, 'productActivityManage', '优惠活动', 10005, 3, 0, '[]', 'promotion/product_activity/list', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000315, 'ShopTeamManage', '店铺设置', 10000311, 2, 1, '[]', 'merchant_setting/shop_team/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000317, 'EmployeeManagement', '员工列表', 10000311, 4, 1, '[]', 'merchant_setting/employee/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000318, 'ShopInfoManage', '店铺信息', 10000311, 3, 1, '[]', 'merchant_setting/shop_info', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000321, 'ShopRoleManage', '角色管理', 10000311, 5, 1, '[]', 'merchant_setting/shop_role/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000322, 'shopShipping', '配送/运费设置', 10000311, 51, 1, '[]', 'merchant_setting/shop_shipping/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000323, 'shopShippingTypeManage', '配送类型', 10000322, 51, 1, '[]', 'merchant_setting/shop_shipping/shop_shipping_type/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000324, 'shopShippingTplManage', '运费模板', 10000322, 52, 1, '[]', 'merchant_setting/shop_shipping/shop_shipping_tpl/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000325, 'fullReductionManage', '满减活动', 5, 3, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"fullReductionUpdateManage\"},{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"fullReductionDelManage\"}]', 'promotion/full_reduction/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000326, 'rewardGiftManage', '满赠活动', 5, 4, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"rewardGiftUpdateManage\"},{\"authName\":\"\\u5220\\u9664\",\"authSn\":\"rewardGiftDelManage\"}]', 'promotion/reward_gift/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000327, 'productGiftManage', '活动赠品', 10005, 53, 1, '[]', 'promotion/product_gift/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000328, 'productGiftManage', '活动赠品', 5, 10, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"productGiftModifyManage\"}]', 'promotion/product_gift/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000329, 'limitdiscountManage', '限时折扣', 5, 5, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"timeDiscountModifyManage\"}]', 'promotion/limitdiscount_gift/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000330, 'promotionManage', '营销管理', 5, 0, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"promotionUpdateManage\"}]', 'promotion/manage/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000331, 'investmentManage', '招商内容', 276, 50, 1, '[]', 'adminMerchant/investment/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000332, 'shopSettingsManage', '店铺设置', 10000306, 54, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"shopSettingsUpdateManage\"}]', 'shop/shopSettings/info', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000333, 'shopFundsManage', '店铺资金', 10000306, 50, 1, '[]', 'shop/shopFunds/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000334, 'financialLogManage', '资金日志', 10000306, 50, 1, '[]', 'shop/financialLog/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000335, 'EmployeeLogManagement', '员工操作日志', 10000311, 50, 1, '[]', 'merchant_setting/employeeLog/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000336, 'warehouseManage', '仓库管理', 10000347, 55, 1, '[]', 'cross/warehouse/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000337, 'salesman', '分销', 0, 64, 1, '[]', 'salesman/index/', 'iconfont-tig icon-fenxiao', 0, 'admin');
INSERT INTO `authority` VALUES (10000338, 'overviewManage', '分销概览', 10000337, 1, 1, '[]', 'salesman/index/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000339, 'promoterManage', '分销员列表', 10000360, 1, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"salesmanModifyManage\"}]', 'salesman/promoter/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000340, 'promoteModeManage', '分销模式', 10000337, 2, 1, '[{\"authName\":\"\\u7f16\\u8f91\\u914d\\u7f6e\",\"authSn\":\"salesmanConfigModifyManage\"}]', 'salesman/promoteMode/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000341, 'salesmanProduct', '分销商品', 10000337, 3, 1, '[]', 'salesman/product/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000342, 'performanceSettlementManage', '业绩结算', 10000361, 2, 1, '[{\"authName\":\"\\u4eba\\u5de5\\u7ed3\\u7b97\",\"authSn\":\"salesmanSettlementManage\"}]', 'salesman/performanceSettlement/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000343, 'salesmanDetailedManage', '分销员明细', 10000360, 4, 1, '[]', 'salesman/salesmanDetailed/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000344, 'performanceSettlementSettingManage', '结算方案设置', 10000361, 1, 1, '[]', 'salesman/performanceSettlement/setting/index', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000345, 'subgroupManage', '分销员分组', 10000360, 2, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"salesmanGroupModifyManage\"}]', 'salesman/subgroup/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000346, 'noticeManage', '内容管理', 10000337, 6, 1, '[]', 'salesman/notice/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000348, 'materialCategoryManage', '素材分类', 10000346, 2, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"salesmanCategoryModifyManage\"}]', 'salesman/materialCategory/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000349, 'materialManage', '素材管理', 10000346, 3, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"salesmanMaterialModifyManage\"}]', 'salesman/material/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000350, 'RankingTableManage', '分销员排行', 10000360, 3, 1, '[]', 'salesman/overview/rankingTable/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000351, 'AnalysisTableManage', '商品成交分析', 10000341, 3, 1, '[]', 'salesman/overview/analysisTable/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000353, 'testManage', '测试目录', 10000352, 50, 1, '[]', 'test/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000359, 'salesmanProductManage', '商品佣金管理', 10000341, 1, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"salesmanProductModifyManage\"}]', 'salesman/product/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000360, 'salesmanManage', '分销员管理', 10000337, 4, 1, '[]', 'salesman/promoter/list/\n', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000361, 'performanceSettlement', '分销结算', 10000337, 5, 1, '[]', 'salesman/performanceSettlement/setting/index/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000362, 'salesmanNoticeManage', '分销攻略', 10000346, 1, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"salesmanContentModifyManage\"}]', 'salesman/notice/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000363, 'licensed', '授权管理', 15, 69, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"licensedModifyManage\"}]', 'setting/licensed/index', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000373, 'multilingual', '多语言设置', 15, 50, 1, '[]', 'setting/multilingual/languagesList/list', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000374, 'languagesListManage', '语言列表', 10000373, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"localesModifyManage\"}]', 'setting/multilingual/languagesList/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000375, 'verbalAssociationManage', '语言关联', 10000373, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"localesRelationModifyManage\"}]', 'setting/multilingual/verbalAssociation/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000376, 'translationContentManage', '翻译内容', 10000373, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"translationsModifyManage\"}]', 'setting/multilingual/translationContent/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000377, 'currencyManagementManage', '币种管理', 10000373, 50, 1, '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"currencyModifyManage\"}]', 'setting/multilingual/currencyManagement/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000378, 'classifyManage', '分类设置', 27, 50, 1, '[]', 'decorate/mobile_decorate/classify/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000379, 'mobileUserManage', '用户中心设置', 27, 50, 1, '[]', 'decorate/mobile_decorate/user', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000381, 'merchantMultilingualManage', '多语言设置', 10000311, 50, 1, '[]', 'merchant_setting/shop_multilingual/shop_translationContent/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000382, 'shopTranslationContentManage', '翻译内容', 10000381, 50, 1, '[]', 'merchant_setting/shop_multilingual/shop_translationContent/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000383, 'liveManage', '微信直播', 5, 50, 1, '[]', 'promotion/live/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000384, 'logisticsManage', '物流设置', 38, 200, 1, '[]', 'setting/config/logistics/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000385, 'areaCodeManage', '区号管理', 10000373, 50, 1, '[]', 'setting/multilingual/areaCode/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000386, 'eCardManage', '电子卡券组', 5, 50, 1, '[]', 'promotion/e_card/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000387, 'eCardManage', '电子卡券组', 10005, 50, 1, '[]', 'promotion/e_card/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000390, 'userCertificationManage', '会员实名认证', 11, 50, 1, '[]', 'user/user_certification/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000391, 'userLevelProManage', '会员等级设置', 11, 50, 1, '[]', 'user/level_manage_pro/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000392, 'enquiryManage', '商品询价', 2, 50, 1, '[]', 'product/enquiry/list/', '', 0, 'admin');
INSERT INTO `authority` VALUES (10000393, 'fullReductionManage', '满减活动', 10005, 50, 1, '[]', 'promotion/full_reduction/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000394, 'rewardGiftManage', '满赠活动', 10005, 50, 1, '[]', 'promotion/reward_gift/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000395, 'limitdiscountManage', '限时折扣', 10005, 50, 1, '[]', 'promotion/limitdiscount_gift/list/', '', 0, 'shop');
INSERT INTO `authority` VALUES (10000396, 'levelManageManage', '会员等级设置', 11, 50, 1, '[]', 'user/level_manage/list/', '', 0, 'admin');

SET FOREIGN_KEY_CHECKS = 1;