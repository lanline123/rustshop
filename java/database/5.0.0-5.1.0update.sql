-- 添加注释
ALTER TABLE `admin_user` MODIFY COLUMN `admin_type` varchar(60) NOT NULL DEFAULT 'admin' COMMENT '管理员类型，admin:管理员，shop:店铺，suppliers:供应商，vendor:供货商';
ALTER TABLE `admin_role`
    ADD COLUMN `vendor_id` int(11) NOT NULL DEFAULT '0' COMMENT '供应商ID',
    MODIFY COLUMN `admin_type` varchar(255) DEFAULT 'admin' COMMENT 'admin（平台）/ shop（店铺）/ vendor（供应商）';

ALTER TABLE `gallery_video`
    ADD COLUMN `vendor_id` int NOT NULL DEFAULT '0' COMMENT '供应商id';

ALTER TABLE `gallery_video_info`
    ADD COLUMN `vendor_id` int NOT NULL DEFAULT '0' COMMENT '供应商id';

ALTER TABLE `gallery` ADD COLUMN `vendor_id` int NULL DEFAULT 0 COMMENT '供应商 ID' AFTER `shop_id`;
ALTER TABLE `gallery_pic` ADD COLUMN `vendor_id` int NOT NULL DEFAULT 0 COMMENT '供应商 ID' AFTER `add_time`;
ALTER TABLE `product_sku` ADD COLUMN `vendor_product_sku_id` int NULL DEFAULT NULL COMMENT '供应商商品规格 ID' AFTER `cost_price`;

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000000, 'vendor', '供应商', 0, 1, 1, '[]', '', 'iconfont-tig icon-gongying', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000001, 'consoleManage', '概览', 11000000, 1, 1, '[]', '', 'iconfont-tig icon-yingxiaogailan', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000002, 'orderManagement', '订单管理', 11000000, 2, 1, '[]', '', 'iconfont-tig icon-dingdanguanli', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000003, 'orderManage', '全部订单', 11000002, 50, 1, '[]', '', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000004, 'deliverManage', '发货管理', 11000002, 50, 1, '[]', '', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000005, 'orderExportManage', '订单导出', 11000002, 50, 1, '[]', ' ', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000006, 'aftersaleManagement', '售后管理', 11000000, 3, 1, '[]', '', 'iconfont-tig icon-shouhouguanli', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000007, 'aftersalesManage', '售后订单', 11000006, 50, 1, '[]', '', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000008, 'commodityManage', '商品管理', 11000000, 4, 1, '[]', '', 'iconfont-tig icon-shangpinguanli', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000009, 'productManage', '商品列表', 11000008, 50, 1, '[]', '', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000010, 'bindShopManager', '关联店铺', 11000008, 50, 1, '[]', '', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000011, 'setting', '设置', 0, 2, 1, '[]', '', 'iconfont-tig icon-shezhi', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000012, 'vendorSetting', '供应商设置', 11000011, 2, 1, '[]', ' ', 'iconfont-tig icon-shezhi', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000013, 'vendorInfoManage', '供应商信息', 11000012, 3, 1, '[]', '', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000014, 'vendorSettingManage', '供应商设置', 11000012, 1, 1, '[]', '', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000015, 'accountManagement', '账号管理', 11000011, 50, 1, '[]', '', 'iconfont-tig icon-shezhi', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000016, 'accountEditingManage', '账号设置', 11000015, 2, 1, '[]', '', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000017, 'vendorRoleManage', '角色管理', 11000015, 3, 1, '[]', '', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000018, 'EmployeeManagement', '员工管理', 11000015, 1, 1, '[]', '', 'iconfont-tig icon-31shezhi', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11000019, 'EmployeeLogManagement', '操作日志', 11000015, 1, 1, '[]', ' ', 'iconfont-tig icon-31shezhi', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001013, 'capitalfund', '财务中心', 11000000, 5, 1, '[]', '', 'iconfont-tig icon-31tianmaobao', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001014, 'dashboardManage', '资金概览', 11001013, 1, 1, '[]', '', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001015, 'balanceManage', '供应商资金', 11001013, 2, 1, '[]', '', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001016, 'withdrawManage', '提现管理', 11001013, 3, 1, '[]', '', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001017, 'financialLogManage', '资金明细', 11001013, 4, 1, '[]', '', '', 0, 'vendor');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001000, 'vendor', '供应商', 0, 5, 1, '[]', '', 'iconfont-tig icon-gongying', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001001, 'vendorManage', '供应商管理', 11001000, 50, 1, '[]', '', 'iconfont-tig icon-gongying', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001002, 'vendorProductManage', '供应商商品', 11001001, 50, 1, '[{\"authName\":\"编辑\",\"authSn\":\"vendorProductUpdateManage\"},{\"authName\":\"审核\",\"authSn\":\"vendorProductAuditManage\"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001003, 'vendorOrderManage', '供应商订单', 11001001, 50, 1, '[{\"authName\":\"发货\",\"authSn\":\"orderDeliverManage\"},{\"authName\":\"收货\",\"authSn\":\"orderConfirmReceiptManage\"},{\"authName\":\"修改收货人\",\"authSn\":\"orderModifyConsigneeManage\"},{\"authName\":\"修改配送信息\",\"authSn\":\"orderModifyShippingManage\"},{\"authName\":\"修改订单金额\",\"authSn\":\"orderModifyMoneyManage\"},{\"authName\":\"取消订单\",\"authSn\":\"cancelOrderManage\"},{\"authName\":\"订单确认\",\"authSn\":\"setConfirmManage\"},{\"authName\":\"软删除\",\"authSn\":\"delOrderManage\"},{\"authName\":\"拆单\",\"authSn\":\"splitStoreOrderManage\"},{\"authName\":\"设为已支付\",\"authSn\":\"setPaidManage\"},{\"authName\":\"修改商品信息\",\"authSn\":\"modifyProductManage\"},{\"authName\":\"设置商家备注\",\"authSn\":\"setAdminNoteManage\"},{\"authName\":\"添加订单日志\",\"authSn\":\"orderLogModifyManage\"},{\"authName\":\"催发货\",\"authSn\":\"remindDeliver\"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001004, 'vendorListManage', '供应商列表', 11001001, 50, 1, '[{\"authName\":\"供应商修改\",\"authSn\":\"vendorListUpdateManage\"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001005, 'vendorSettingManage', '供应商设置', 11001007, 50, 1, '[{\"authName\":\"供应商设置修改\",\"authSn\":\"vendorSettingUpdateManage\"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001007, 'vendorSetting', '供应商设置', 11001000, 50, 1, '[]', '', 'iconfont-tig icon-xitongshezhi', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001008, 'vendorUserManage', '管理员列表', 11001007, 50, 0, '[{\"authName\":\"管理员修改权限\",\"authSn\":\"vendorUserUpdateManage\"}]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001020, 'vendorFundsManagement', '资金管理', 11001000, 50, 1, '[]', '', 'iconfont-tig icon-jiaoyiguanli', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001021, 'vendorAccountManage', '资金概览', 11001020, 50, 1, '[]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001022, 'vendorFundsManage', '供应商资金', 11001020, 50, 1, '[]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001023, 'vendorAccountRaplyManage', '提现管理', 11001020, 50, 1, '[]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001024, 'vendorFinancialLogManage', '资金日志', 11001020, 50, 1, '[]', '', '', 0, 'admin');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001009, 'protocolManagement', '协议管理', 10, 50, 1, '[{\"authName\":\"协议修改\",\"authSn\":\"protocolUpdateManagement\"}]', '', 'iconfont-tig icon-dingdanguanli', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001010, 'serviceAgreement', '服务协议', 11001009, 50, 1, '[]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001011, 'privacyAgreement', '隐私政策', 11001009, 50, 1, '[]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001012, 'afterSaleService', '售后服务', 11001009, 50, 1, '[]', '', '', 0, 'admin');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001025, 'accountManage', '账号管理', 10000428, 52, 1, '[{\"authName\":\"绑定\",\"authSn\":\"adminAccountBindMainAccount\"},{\"authName\":\"编辑\",\"authSn\":\"adminAccountUpdateManager\"}]', '', '', 0, 'admin');

UPDATE `authority` SET `authority_sn` = 'orderManage', `authority_name` = '全部订单', `parent_id` = 10000414, `sort_order` = 50, `is_show` = 1, `child_auth` = '[{\"authName\":\"发货\",\"authSn\":\"orderDeliverManage\"},{\"authName\":\"收货\",\"authSn\":\"orderConfirmReceiptManage\"},{\"authName\":\"修改收货人\",\"authSn\":\"orderModifyConsigneeManage\"},{\"authName\":\"修改配送信息\",\"authSn\":\"orderModifyShippingManage\"},{\"authName\":\"修改订单金额\",\"authSn\":\"orderModifyMoneyManage\"},{\"authName\":\"取消订单\",\"authSn\":\"cancelOrderManage\"},{\"authName\":\"订单确认\",\"authSn\":\"setConfirmManage\"},{\"authName\":\"软删除\",\"authSn\":\"delOrderManage\"},{\"authName\":\"拆单\",\"authSn\":\"splitStoreOrderManage\"},{\"authName\":\"设为已支付\",\"authSn\":\"setPaidManage\"},{\"authName\":\"修改商品信息\",\"authSn\":\"modifyProductManage\"},{\"authName\":\"设置商家备注\",\"authSn\":\"setAdminNoteManage\"},{\"authName\":\"添加订单日志\",\"authSn\":\"orderLogModifyManage\"},{\"authName\":\"催发货\",\"authSn\":\"remindDeliver\"}]', `route_link` = 'order/list/', `authority_ico` = '', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 31;

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001059, 'statementDownload', '对账单下载', 10000291, 50, 1, '[]', '', '', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001060, 'statementDetails', '对账单明细', 10000291, 50, 1, '[]', '', '', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001061, 'vendorStatementDownload', '对账单下载', 11001013, 50, 1, '[]', '', '', 0, 'vendor');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001062, 'vendorStatementDetails', '对账单明细', 11001013, 50, 1, '[]', '', '', 0, 'vendor');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001018, 'vendorSetting', '供应商设置', 10000463, 50, 1, '[]', '', 'iconfont-tig icon-xitongshezhi', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001019, 'vendorSettingManage', '价格设置', 11001018, 50, 1, '[]', '', '', 0, 'shop');

CREATE TABLE `vendor` (
                          `vendor_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID，供应商ID',
                          `vendor_logo` varchar(255) DEFAULT '' COMMENT '供应商logo',
                          `add_time` int(11) DEFAULT '0' COMMENT '供应商创建时间',
                          `contact_mobile` varchar(255) DEFAULT '' COMMENT '联系电话',
                          `vendor_data` text COMMENT '供应商信息JSON',
                          `person_data` text COMMENT '个人信息JSON',
                          `status` tinyint(2) DEFAULT '1' COMMENT '状态 1开启 2关闭',
                          `type` tinyint(1) DEFAULT '1' COMMENT '类型 1个人 2企业',
                          `vendor_name` varchar(255) DEFAULT '' COMMENT '供应商名称',
                          `kefu_phone` varchar(20) DEFAULT '' COMMENT '客服电话',
                          `vendor_money` decimal(10,2) DEFAULT '0.00' COMMENT '供应商资金',
                          `frozen_money` decimal(10,2) DEFAULT '0.00' COMMENT '冻结资金',
                          `description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '简介',
                          `last_login_time` int(13) unsigned NOT NULL DEFAULT '0' COMMENT '最后登录时间',
                          PRIMARY KEY (`vendor_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='供应商表';

CREATE TABLE `vendor_account_log` (
                                      `vendor_account_log_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                      `add_time` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '添加时间',
                                      `vendor_money` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '供应商资金',
                                      `frozen_money` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '供应商冻结资金',
                                      `type` VARCHAR(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '资金变动类型，例如1-提现',
                                      `remarks` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '备注',
                                      `new_vendor_money` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '现供应商资金',
                                      `new_frozen_money` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '现供应商冻结资金',
                                      `vendor_id` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '供应商ID',
                                      PRIMARY KEY (`vendor_account_log_id`) USING BTREE,
                                      KEY `vendor_id` (`vendor_id`) USING BTREE,
                                      KEY `type` (`type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='供应商资金变化';

CREATE TABLE `vendor_account` (
                                  `account_id` INT(5) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '账号ID',
                                  `vendor_id` MEDIUMINT(8) UNSIGNED NOT NULL DEFAULT '0' COMMENT '供应商ID',
                                  `account_type` TINYINT(1) UNSIGNED NOT NULL DEFAULT '1' COMMENT '提现账号类型，1：银行卡，2：支付宝，3：微信',
                                  `account_name` VARCHAR(60) NOT NULL DEFAULT '' COMMENT '姓名',
                                  `account_no` TEXT NOT NULL COMMENT '银行卡号或账号',
                                  `bank_name` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '银行名称',
                                  `add_time` INT(11) DEFAULT '0' COMMENT '添加时间',
                                  `bank_branch` VARCHAR(255) DEFAULT '' COMMENT '分行名称',
                                  PRIMARY KEY (`account_id`) USING BTREE,
                                  KEY `account_type` (`account_type`) USING BTREE,
                                  KEY `vendor_id` (`vendor_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='供应商账户表';

CREATE TABLE `vendor_withdraw`  (
                                                     `vendor_withdraw_log_id` int NOT NULL AUTO_INCREMENT COMMENT '提现记录ID',
                                                     `vendor_id` mediumint UNSIGNED NOT NULL DEFAULT 0 COMMENT '所属供应商',
                                                     `amount` decimal(10, 2) UNSIGNED NOT NULL DEFAULT 0.00 COMMENT '提现金额',
                                                     `status` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0待审核，2审核不通过，3完成，4待打款',
                                                     `add_time` int UNSIGNED NOT NULL COMMENT '提现时间',
                                                     `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '提现备注',
                                                     `vendor_account_id` int NULL DEFAULT NULL COMMENT '提现到账户ID',
                                                     `account_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '提现时账户信息数据',
                                                     `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核备注',
                                                     `payment_voucher` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '打款凭证',
                                                     `withdraw_sn` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提现单号',
                                                     PRIMARY KEY (`vendor_withdraw_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '供应商提现记录' ROW_FORMAT = DYNAMIC;


CREATE TABLE `admin_user_vendor` (
                                     `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                                     `admin_id` int(11) NOT NULL DEFAULT '0' COMMENT '管理员ID',
                                     `user_id` int(11) NOT NULL DEFAULT '0' COMMENT '会员ID',
                                     `vendor_id` int(11) NOT NULL DEFAULT '0' COMMENT '供应商ID',
                                     `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '员工名称',
                                     `email` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '邮箱',
                                     `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '头像',
                                     `auth_list` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '员工权限 [JSON]',
                                     `is_using` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否停用：1 是，0 否',
                                     `is_admin` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否为管理员：1 是，0 否',
                                     `add_time` int(11) DEFAULT NULL COMMENT '创建时间',
                                     `role_id` smallint unsigned NOT NULL DEFAULT '0' COMMENT '权限组ID',
                                     PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='供应商员工列表';

ALTER TABLE `shop`
    ADD COLUMN `vendor_set_price_type` tinyint DEFAULT '3' COMMENT '供应商设价方式(1按比例，2-按固定数值加价，3-默认售价)',
    ADD COLUMN `vendor_set_price_auto_value` decimal(10,2) DEFAULT NULL COMMENT '智能设价（百分比或固定数值）';

-- 添加字段
ALTER TABLE `aftersales`
    ADD COLUMN `vendor_id` INT(11) DEFAULT NULL COMMENT '供应商id';

-- 添加索引
ALTER TABLE `aftersales`
    ADD INDEX `idx_vendor_id` (`vendor_id`);

-- 添加字段
ALTER TABLE `product`
    ADD COLUMN `vendor_product_id` INT(11) DEFAULT NULL COMMENT '供应商产品表id',
    ADD COLUMN `vendor_id` INT(11) DEFAULT NULL COMMENT '供应商id';

-- 添加索引
ALTER TABLE `product`
    ADD INDEX `idx_vendor_product_id` (`vendor_product_id`),
    ADD INDEX `idx_vendor_id` (`vendor_id`);

-- 添加字段
ALTER TABLE `order`
    ADD COLUMN `vendor_id` INT(11) DEFAULT NULL COMMENT '供应商id';

-- 添加索引
ALTER TABLE `order`
    ADD INDEX `idx_vendor_id` (`vendor_id`);

-- 添加字段
ALTER TABLE `admin_msg`
    ADD COLUMN `vendor_id` INT(11) DEFAULT 0 COMMENT '供应商id';

-- 添加索引
ALTER TABLE `admin_msg`
    ADD INDEX `idx_vendor_id` (`vendor_id`);

-- 添加字段
ALTER TABLE `order_item`
    ADD COLUMN `vendor_product_id` INT(11) DEFAULT NULL COMMENT '供应商产品表id',
    ADD COLUMN `vendor_product_sku_id` INT(11) DEFAULT NULL COMMENT '供应商产品sku表id',
    ADD COLUMN `vendor_id` INT(11) DEFAULT NULL COMMENT '供应商id';

-- 添加索引
ALTER TABLE `order_item`
    ADD INDEX `idx_vendor_product_id` (`vendor_product_id`),
    ADD INDEX `idx_vendor_product_sku_id` (`vendor_product_sku_id`),
    ADD INDEX `idx_vendor_id` (`vendor_id`);

CREATE TABLE `vendor_shop_bind` (
                                    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                                    `vendor_id` int(11) NOT NULL COMMENT '供应商ID',
                                    `shop_id` int(11) NOT NULL COMMENT '店铺ID',
                                    `add_time` int(11) DEFAULT NULL COMMENT '创建时间',
                                    PRIMARY KEY ( `id` ) USING BTREE
) ENGINE = INNODB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC COMMENT = '供应商店铺绑定表';


create table vendor_product
(
    id                       bigint auto_increment comment 'ID'
        primary key,
    product_name             varchar(255) not null comment '商品名称',
    product_brand_id         int          null comment '商品品牌ID',
    product_category_id      int          not null comment '商品类目ID',
    product_sn_generate_type tinyint      not null comment '商品编码生成类型；1-系统生成，2-手动输入',
    product_brief            varchar(255) null comment '商品描述',
    product_state            tinyint      not null comment '商品状态；0-断供，1-在售',
    sku_type                 tinyint      not null comment '规格类型，1-单规格，2-多规格',
    product_desc             text         null comment '商品详情',
    audit_state              tinyint      not null comment '商品审核状态；0-待审核，1-审核通过，2-审核失败',
    pic_url                  varchar(255) not null comment '商品实际图片',
    pic_thumb                varchar(255) not null comment '商品缩略图片',
    pic_original             varchar(255) not null comment '商品原始图片',
    is_recycle               tinyint      not null comment '是否回收；0-否，1-是',
    vendor_id                int          not null comment '供应商 ID',
    create_time              bigint       not null comment '创建时间',
    create_by_id             bigint       not null comment '创建人主键',
    create_by_name           varchar(255) null comment '创建人名称',
    update_time              bigint       not null comment '更新时间',
    update_by_id             bigint       not null comment '编辑人主键',
    update_by_name           varchar(255) not null comment '更新人名称',
    is_del                   tinyint      not null comment '是否删除；0-否，1-是'
) ENGINE = INNODB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC COMMENT '供应商商品';

create table vendor_product_audit_log
(
    id                bigint auto_increment comment 'ID'
        primary key,
    vendor_product_id bigint       null comment '供应商商品 ID',
    audit_state       tinyint      null comment '审核状态；0-待审核，1-审核通过，2-审核失败',
    audit_fail_reason varchar(255) null comment '审核失败原因',
    vendor_id         int          not null comment '供应商 ID',
    create_time       bigint       not null comment '创建时间',
    create_by_id      bigint       not null comment '创建人主键',
    create_by_name    varchar(255) null comment '创建人名称',
    update_time       bigint       not null comment '更新时间',
    update_by_id      bigint       not null comment '编辑人主键',
    update_by_name    varchar(255) not null comment '更新人名称',
    is_del            tinyint      not null comment '是否删除；0-否，1-是'
) ENGINE = INNODB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC COMMENT '供应商商品审核记录';

create index vendor_product_audit_log_vendor_product_id_index
    on vendor_product_audit_log (vendor_product_id);

create table vendor_product_gallery
(
    id                bigint auto_increment comment 'ID'
        primary key,
    vendor_product_id int                     not null comment '供应商商品 ID',
    pic_url           text                    not null comment '实际图片url ',
    pic_desc          varchar(255) default '' not null comment '图片说明信息 ',
    pic_thumb         text                    not null comment '微缩图片url ',
    pic_original      text                    not null comment '原图url',
    pic_large         text                    not null comment '高清图片url ',
    sort_order        int                     not null comment '排序',
    vendor_id         int                     not null comment '供应商 ID',
    create_time       bigint                  not null comment '创建时间',
    create_by_id      bigint                  not null comment '创建人主键',
    create_by_name    varchar(255)            null comment '创建人名称',
    update_time       bigint                  not null comment '更新时间',
    update_by_id      bigint                  not null comment '编辑人主键',
    update_by_name    varchar(255)            not null comment '更新人名称',
    is_del            tinyint                 not null comment '是否删除；0-否，1-是'
) ENGINE = INNODB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC COMMENT '供应商商品相册';

create index vendor_product_gallery_vendor_product_id_index
    on vendor_product_gallery (vendor_product_id);

create table vendor_product_sku
(
    id                      bigint auto_increment comment 'ID'
        primary key,
    vendor_product_id       int                     not null comment '供应商商品 ID',
    sku_attr_val            varchar(255) default '' not null comment '商品规格值（拼接）',
    sku_attr_json           json                    null comment '商品规格值（json）',
    sku_sn                  varchar(60)  default '' null comment '规格编码',
    sku_tsn                 varchar(120) default '' null comment '规格条形码',
    sku_weight              decimal(10, 2)          null comment '规格重量',
    sku_stock               int                     null comment '规格库存',
    supply_price            decimal(10, 2)          not null comment '供货价',
    supply_price_limit_type tinyint                 not null comment '限售金额类型；1-按金额，2-按比例',
    supply_price_limit_val  decimal(10, 2)          not null comment '限售金额值',
    sales_volume            int                     not null comment '规格销量',
    vendor_id               int                     not null comment '供应商 ID',
    create_time             bigint                  not null comment '创建时间',
    create_by_id            bigint                  not null comment '创建人主键',
    create_by_name          varchar(255)            null comment '创建人名称',
    update_time             bigint                  not null comment '更新时间',
    update_by_id            bigint                  not null comment '编辑人主键',
    update_by_name          varchar(255)            not null comment '更新人名称',
    is_del                  tinyint                 not null comment '是否删除；0-否，1-是'
) ENGINE = INNODB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC COMMENT '供货商商品规格';

create index vendor_product_sku_vendor_product_id_index
    on vendor_product_sku (vendor_product_id);

create table vendor_product_sku_attr
(
    id                bigint auto_increment comment 'ID '
        primary key,
    vendor_product_id int                     not null comment '供应商商品 ID ',
    attr_type         tinyint                 not null comment '属性类型，0-普通属性，1-规格属性，2-附加规格',
    attr_name         varchar(80)             not null comment '属性所属类型的名称',
    attr_value        varchar(120)            not null comment '具体属性的值 ',
    attr_price        decimal(10, 2)          null comment '属性价格',
    attr_color        varchar(80)             null comment '属性颜色',
    attr_pic          varchar(255) default '' null comment '属性图片',
    attr_pic_thumb    varchar(255) default '' null comment '属性缩略图',
    vendor_id         int                     not null comment '供应商 ID',
    create_time       bigint                  not null comment '创建时间',
    create_by_id      bigint                  not null comment '创建人主键',
    create_by_name    varchar(255)            null comment '创建人名称',
    update_time       bigint                  not null comment '更新时间',
    update_by_id      bigint                  not null comment '编辑人主键',
    update_by_name    varchar(255)            not null comment '更新人名称',
    is_del            tinyint                 not null comment '是否删除；0-否，1-是'
) ENGINE = INNODB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC COMMENT '供应商商品规格属性';

create index vendor_product_sku_attr_vendor_product_id_index
    on vendor_product_sku_attr (vendor_product_id);

create table vendor_product_sku_stock_log
(
    id                    bigint auto_increment comment 'ID'
        primary key,
    vendor_product_id     int          not null comment '供货商商品ID',
    vendor_product_sku_id int          not null comment '供应商商品规格 ID',
    operation_type        int          not null comment '操作类型；1-增加，2-减少',
    before_stock          int          not null comment '变更前库存',
    change_num            int          not null comment '变动数量',
    after_stock           int          not null comment '变更后库存',
    biz_type              smallint     not null comment '业务类型；1-商品新增，2-库存编辑',
    biz_remark            varchar(255) null comment '业务备注',
    vendor_id             int          not null comment '供应商 ID',
    create_time           bigint       not null comment '创建时间',
    create_by_id          bigint       not null comment '创建人主键',
    create_by_name        varchar(255) null comment '创建人名称',
    update_time           bigint       not null comment '更新时间',
    update_by_id          bigint       not null comment '编辑人主键',
    update_by_name        varchar(255) not null comment '更新人名称',
    is_del                tinyint      not null comment '是否删除；0-否，1-是'
) ENGINE = INNODB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC COMMENT '供货商商品规格库存日志';

create table vendor_product_video
(
    id                bigint auto_increment comment 'ID'
        primary key,
    vendor_product_id int                                     not null comment '供货商商品 ID',
    video_url         varchar(255) collate utf8mb4_unicode_ci not null comment '视频url',
    video_cover       varchar(255)                            not null comment '视频封面',
    format            varchar(255)                            not null comment '视频格式后缀',
    vendor_id         int                                     not null comment '供应商 ID',
    create_time       bigint                                  not null comment '创建时间',
    create_by_id      bigint                                  not null comment '创建人主键',
    create_by_name    varchar(255)                            null comment '创建人名称',
    update_time       bigint                                  not null comment '更新时间',
    update_by_id      bigint                                  not null comment '编辑人主键',
    update_by_name    varchar(255)                            not null comment '更新人名称',
    is_del            tinyint                                 not null comment '是否删除；0-否，1-是'
) ENGINE = INNODB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC COMMENT '供应商商品视频';

create index vendor_product_video_vendor_product_id_index
    on vendor_product_video (vendor_product_id);



CREATE TABLE `vendor_settlement_order` (
                                           `vendor_settlement_order_id` int NOT NULL AUTO_INCREMENT,
                                           `order_id` int DEFAULT NULL COMMENT '订单ID',
                                           `shop_id` int DEFAULT NULL COMMENT '店铺ID',
                                           `vendor_id` int DEFAULT NULL COMMENT '供应商ID',
                                           `amount` decimal(10,2) DEFAULT NULL COMMENT '收益总额',
                                           `add_time` int DEFAULT NULL COMMENT '生成时间',
                                           PRIMARY KEY (`vendor_settlement_order_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10035 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='供应商结算订单表';


ALTER TABLE `shop_withdraw`
    ADD COLUMN `withdraw_sn` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '提现单号';

ALTER TABLE `shop`
    ADD COLUMN `service_fee_rate` decimal(10, 2) NULL COMMENT '服务费比例',
    ADD COLUMN `fee_rate` decimal(10, 2) NULL COMMENT '手续费比例';

ALTER TABLE `vendor`
    ADD COLUMN `service_fee_rate` decimal(10, 2) NULL COMMENT '服务费比例',
    ADD COLUMN `fee_rate` decimal(10, 2) NULL COMMENT '手续费比例';

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001056, 'accountListManage', '账户管理', 10000291, 50, 1, '[]', '', '', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001057, 'accountListManage', '账户列表', 11001013, 50, 1, '[]', '', '', 0, 'vendor');

UPDATE `authority` SET `authority_sn` = 'userfundsManagement', `authority_name` = '会员资金管理', `parent_id` = 12, `sort_order` = 50, `is_show` = 1, `child_auth` = '[]', `route_link` = '', `authority_ico` = 'iconfont-tig icon-zijinguanli', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 10000440;
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001048, 'shopUserfundsManagement', '店铺资金管理', 12, 50, 1, '[]', '', 'iconfont-tig icon-duoyuyan', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001052, 'vendorUserfundsManagement', '供应商资金管理', 12, 50, 1, '[]', '', 'iconfont-tig icon-dianpuguanli', 0, 'admin');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001045, 'profitsharingManage', '分账管理', 12, 50, 1, '[]', '', 'iconfont-tig icon-tuikuanguanli', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001046, 'profitsharingSettingManage', '分账设置', 11001045, 50, 1, '[]', '', '', 0, 'admin');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001049, 'statementDownload', '对账单下载', 11001048, 50, 1, '[]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001050, 'statementDetails', '对账单明细', 11001048, 50, 1, '[]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001053, 'vendorStatementDownload', '对账单下载', 11001052, 50, 1, '[]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001054, 'vendorStatementDetails', '对账单明细', 11001052, 50, 1, '[]', '', '', 0, 'admin');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001051, 'shopWithdrawApplyManage', '提现管理', 11001048, 50, 1, '[]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001055, 'vendorWithdrawApplyManage', '提现管理', 11001052, 50, 1, '[]', '', '', 0, 'admin');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001044, 'distributionCommissionManage', '分销佣金', 10000440, 50, 1, '[]', '', '', 0, 'admin');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001047, 'withdrawalSettingManage', '提现设置', 11001045, 50, 1, '[]', '', '', 0, 'admin');


UPDATE `authority` SET `authority_sn` = 'adminMerchant', `authority_name` = '商户管理', `parent_id` = 11001058, `sort_order` = 54, `is_show` = 1, `child_auth` = '[]', `route_link` = 'adminMerchant/apply/list/', `authority_ico` = 'iconfont-tig icon-31quanbushangpin', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 276;
UPDATE `authority` SET `authority_sn` = 'shopManagement', `authority_name` = '店铺管理', `parent_id` = 11001058, `sort_order` = 50, `is_show` = 1, `child_auth` = '[]', `route_link` = '', `authority_ico` = 'iconfont-tig icon-dianpuguanli', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 10000416;
UPDATE `authority` SET `authority_sn` = 'vendorManage', `authority_name` = '供应商管理', `parent_id` = 11001058, `sort_order` = 50, `is_show` = 1, `child_auth` = '[]', `route_link` = '', `authority_ico` = 'iconfont-tig icon-gongying', `is_system` = 0, `admin_type` = 'admin' WHERE `authority_id` = 11001001;


INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (11001058, 'organize', '组织', 0, 50, 1, '[]', '', 'iconfont-tig icon-zuzhi', 0, 'admin');

-- ----------------------------
-- Table structure for statement
-- ----------------------------
DROP TABLE IF EXISTS `statement`;
CREATE TABLE `statement`  (
                              `statement_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `shop_id` int(11) NULL DEFAULT NULL COMMENT '店铺ID/门店id',
                              `record_id` int(20) NULL DEFAULT NULL COMMENT '单据Id',
                              `record_sn` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '单据流水号',
                              `record_time` bigint(20) NULL DEFAULT NULL COMMENT '下单时间',
                              `settlement_time` bigint(20) NULL DEFAULT NULL COMMENT '结算时间',
                              `vendor_id` int(11) NULL DEFAULT NULL COMMENT '供应商id',
                              `account_type` int(11) NULL DEFAULT NULL COMMENT '账户类型1.账户余额',
                              `type` int(11) NULL DEFAULT NULL COMMENT '类型：1.手续费 2.店铺提现收支 3.店铺订单收支等',
                              `entry_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '入账方式：1. 自动 2.手动',
                              `payment_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '支付方式（如微信、支付宝等）',
                              `account_balance` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
                              `amount` decimal(10, 2) NOT NULL COMMENT '交易金额（正为收入，负为支出）',
                              `gmt_create` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '记录创建时间',
                              `statement_year` int(11) NULL DEFAULT NULL COMMENT '当前记录年',
                              `statement_month` int(11) NULL DEFAULT NULL COMMENT '当前记录月',
                              `statement_day` int(11) NULL DEFAULT NULL COMMENT '当前记录日',
                              `settlement_status` int(11) NULL DEFAULT NULL COMMENT '结算状态：1.待结算 2.已结算',
                              PRIMARY KEY (`statement_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '对账单明细表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of statement
-- ----------------------------
INSERT INTO `statement` VALUES (1, 3, 1, '20250701174526959823', 1754976581, 1754976581, 1001, 1, 1, '1', 'wechat', 0.00, 1.00, '1754976581', 2025, 8, 30, 1);

-- ----------------------------
-- Table structure for statement_download
-- ----------------------------
DROP TABLE IF EXISTS `statement_download`;
CREATE TABLE `statement_download`  (
                                       `statement_download_id` bigint(20) NOT NULL,
                                       `vendor_id` int(20) NULL DEFAULT NULL COMMENT '供应商id',
                                       `shop_id` int(20) NULL DEFAULT NULL COMMENT '店铺id',
                                       `gmt_create` bigint(20) NULL DEFAULT NULL COMMENT '申请时间',
                                       `start_time` bigint(20) NULL DEFAULT NULL COMMENT '筛选开始时间',
                                       `end_time` bigint(20) NULL DEFAULT NULL COMMENT '筛选结束时间',
                                       `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
                                       PRIMARY KEY (`statement_download_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '对账单下载' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of statement_download
-- ----------------------------

-- ----------------------------
-- Table structure for statement_output
-- ----------------------------
DROP TABLE IF EXISTS `statement_output`;
CREATE TABLE `statement_output`  (
                                     `statement_output_id` bigint(20) NOT NULL,
                                     `shop_id` int(20) NULL DEFAULT NULL COMMENT '店铺id',
                                     `vendor_id` int(20) NULL DEFAULT NULL COMMENT '供应商id',
                                     `income` decimal(10, 2) NULL DEFAULT NULL COMMENT '收入金额',
                                     `expenditure` decimal(10, 2) NULL DEFAULT NULL COMMENT '支出金额',
                                     `gmt_create` bigint(20) NULL DEFAULT NULL COMMENT '出账时间',
                                     `record_sn` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '单据sn',
                                     `record_type` int(2) NULL DEFAULT NULL COMMENT '单据类型',
                                     `record_id` int(20) NULL DEFAULT NULL COMMENT '单据id',
                                     `settlement_status` bigint(20) NULL DEFAULT NULL COMMENT '入账状态',
                                     PRIMARY KEY (`statement_output_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('vendorAgreement', '供应商协议', 1744769895, 0, '', 1744769895, 0, '', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('vendorProductNeedCheck', '1', 1755156958, 1, 'admin', 1755156958, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('vendorMaxSubAdministrator', '10', 1755156958, 1, 'admin', 1755156958, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('vendorSetPriceType', '1', 1755156958, 1, 'admin', 1755156958, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('vendorSetPriceAutoValue', '50.00', 1755156958, 1, 'admin', 1755156958, 1, 'admin', 0);


INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('withdrawalReceiptMethod', '[1, 2, 3]', 1755162305, 1, 'admin', 1755162305, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('withdrawalEnabled', '1', 1755162305, 1, 'admin', 1755162305, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('minAmount', '5', 1755162305, 1, 'admin', 1755162305, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('maxAmount', '10', 1755162305, 1, 'admin', 1755162305, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('withdrawalFrequencyUnit', '1', 1755162305, 1, 'admin', 1755162305, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('withdrawalFrequencyCount', '1', 1755162305, 1, 'admin', 1755162305, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('withdrawalReviewMethod', '1', 1755162305, 1, 'admin', 1755162305, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('withdrawalDescription', '每日一次 限额5', 1755162305, 1, 'admin', 1755162305, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('billingNode', '1', 1755162307, 1, 'admin', 1755162307, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('collectionNode', '1', 1755162307, 1, 'admin', 1755162307, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('collectionTimeSetting', '0', 1755162307, 1, 'admin', 1755162307, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('collectionMethod', '1', 1755162307, 1, 'admin', 1755162307, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('collectionAccountType', '1', 1755162307, 1, 'admin', 1755162307, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('splitPaymentMethod', '1', 1755162307, 1, 'admin', 1755162307, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('storeGeneralServiceFeeRate', '10', 1755162307, 1, 'admin', 1755162307, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('storeWithdrawalFeeRate', '10', 1755162307, 1, 'admin', 1755162307, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('storefrontGeneralServiceFeeRate', '0', 1755162307, 1, 'admin', 1755162307, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('storefrontWithdrawalFeeRate', '0', 1755162307, 1, 'admin', 1755162307, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('supplierGeneralServiceFeeRate', '10', 1755162307, 1, 'admin', 1755162307, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('supplierWithdrawalFeeRate', '10', 1755162307, 1, 'admin', 1755162307, 1, 'admin', 0);


alter table user_balance_log add before_balance decimal(10, 2) not null comment '更新前余额' after user_id;
alter table user_balance_log add after_balance decimal(10, 2) not null comment '更新后余额' after balance;

DELETE FROM `authority`
WHERE (`authority_sn` = 'shop' AND `admin_type` = 'admin')
   OR (`authority_sn` = 'vendor' AND `admin_type` = 'admin');

UPDATE authority t SET t.parent_id = 11001001 WHERE t.authority_id = 11001005