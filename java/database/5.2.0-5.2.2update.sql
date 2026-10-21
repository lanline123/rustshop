-- 猜你喜欢相关
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('isGuestLikeGoods', '1', 1758610402, 1, 'admin', 1758610402, 1, 'admin', 0);
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('guestLikeGoodsName', '猜你喜欢', 1758610402, 1, 'admin', 1758610402, 1, 'admin', 0);
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES ('personalizedManage', '个性化设置', 39, 50, 1, '[]', '', '', 0, 'admin');

-- 请求日志相关
ALTER TABLE admin_log ADD COLUMN request_log_id int COMMENT '关联的请求日志ID';
CREATE TABLE `request_log` (
   `request_log_id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
   `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '地址',
   `http_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '请求类型',
   `class_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类名 + 方法名',
   `request_params` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '请求参数',
   `add_time` varchar(32) COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '操作时间',
   `admin_username` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '后台用户名称',
   `admin_id` int DEFAULT NULL COMMENT '后台用户id',
   `admin_ip` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'ip地址',
   `platform` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '平台信息',
   PRIMARY KEY (`request_log_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 其他
UPDATE `authority`
SET
    `child_auth` = '[{\"authName\":\"\\u7f16\\u8f91\",\"authSn\":\"shopSettingsUpdateManage\"}]'
WHERE
    authority_sn ='ShopInfoManage' and admin_type = 'shop';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"merchantShopProductCategoryModifyManage\"}]'
WHERE `authority_sn` = 'shopProductCategoryManage'
  AND `admin_type` = 'shop';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"brandModifyManage\"}]'
WHERE `authority_sn` = 'brandManage'
  AND `admin_type` = 'shop';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"commentModifyManage\"}]'
WHERE `authority_sn` = 'commentManage'
  AND `admin_type` = 'shop';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"productAttributesTplModifyManage\"}]'
WHERE `authority_sn` = 'productAttributesTplManage'
  AND `admin_type` = 'shop';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"accountModifyManage\"}]'
WHERE `authority_sn` = 'accountListManage'
  AND `admin_type` = 'shop';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"提现\",\"authSn\":\"merchantShopWithdrawModifyManage\"}]'
WHERE `authority_sn` = 'withdrawManage'
  AND `admin_type` = 'shop';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"timeDiscountModifyManage\"}]'
WHERE `authority_sn` = 'limitdiscountManage'
  AND `admin_type` = 'shop';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"productGiftModifyManage\"}]'
WHERE `authority_sn` = 'productGiftManage'
  AND `admin_type` = 'shop';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"salesmanProductModifyManage\"}]'
WHERE `authority_sn` = 'salesmanProductManage'
  AND `admin_type` = 'shop';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"salesmanProductModifyManage\"}]'
WHERE `authority_sn` = 'materialManage'
  AND `admin_type` = 'shop';

UPDATE `authority` SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"adminRoleUpdateManage\"}]' WHERE `authority_sn` = 'ShopRoleManage' AND `admin_type` = 'shop';
UPDATE `authority` SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"adminUserShopModifyManage\"}]' WHERE `authority_sn` = 'accountEditingManage' AND `admin_type` = 'shop';
UPDATE `authority` SET `child_auth` = '[{"authName":"配置获取","authSn":"shopAdminUserManageConfig"},{"authName":"编辑","authSn":"adminUserShopModifyManage"}]' WHERE `authority_sn` = 'EmployeeManagement' AND `admin_type` = 'shop';
UPDATE `authority` SET `child_auth` = '[{"authName":"编辑","authSn":"shippingTplUpdateManage"}]' WHERE `authority_sn` = 'shopShippingTplManage' AND `admin_type` = 'shop';
UPDATE `authority` SET `child_auth` = '[{"authName":"查看","authSn":"pcDecorateOtherManage"},{"authName":"编辑","authSn":"decorateDiscreteModifyManage"},{"authName":"新增","authSn":"decorateCreateManage"},{"authName":"编辑","authSn":"decorateModifyManage"}]' WHERE `authority_sn` = 'mobileShopDecorate' AND `admin_type` = 'shop';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"发货\",\"authSn\":\"orderDeliverManage\"},{\"authName\":\"收货\",\"authSn\":\"orderConfirmReceiptManage\"},{\"authName\":\"修改收货人\",\"authSn\":\"orderModifyConsigneeManage\"},{\"authName\":\"修改配送信息\",\"authSn\":\"orderModifyShippingManage\"},{\"authName\":\"修改订单金额\",\"authSn\":\"orderModifyMoneyManage\"},{\"authName\":\"取消订单\",\"authSn\":\"cancelOrderManage\"},{\"authName\":\"订单确认\",\"authSn\":\"setConfirmManage\"},{\"authName\":\"软删除\",\"authSn\":\"delOrderManage\"},{\"authName\":\"拆单\",\"authSn\":\"splitStoreOrderManage\"},{\"authName\":\"设为已支付\",\"authSn\":\"setPaidManage\"},{\"authName\":\"修改商品信息\",\"authSn\":\"modifyProductManage\"},{\"authName\":\"设置商家备注\",\"authSn\":\"setAdminNoteManage\"},{\"authName\":\"添加订单日志\",\"authSn\":\"orderLogModifyManage\"},{\"authName\":\"催发货\",\"authSn\":\"remindDeliver\"}]'
WHERE `authority_sn` = 'orderManage'
  AND `admin_type` = 'vendor';

UPDATE `authority`
SET `child_auth` = '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"aftersalesModifyManage\"}]'
WHERE `authority_sn` = 'aftersalesManage'
  AND `admin_type` = 'vendor';

UPDATE `authority`
SET `child_auth` = '[{\"auth_name\":\"\\u7f16\\u8f91\",\"auth_sn\":\"productModifyManage\"}]'
WHERE `authority_sn` = 'productManage'
  AND `admin_type` = 'vendor';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"vendorSettingUpdateManage\"}]'
WHERE `authority_sn` = 'vendorInfoManage'
  AND `admin_type` = 'vendor';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"vendorSettingUpdateManage\"}]'
WHERE `authority_sn` = 'vendorSettingManage'
  AND `admin_type` = 'vendor';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"adminRoleUpdateManage\"}]'
WHERE `authority_sn` = 'vendorRoleManage'
  AND `admin_type` = 'vendor';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"vendorUserUpdateManage\"}]'
WHERE `authority_sn` = 'EmployeeManagement'
  AND `admin_type` = 'vendor';
UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"brandModifyManage\"}]'
WHERE `authority_sn` = 'brandManage'
  AND `admin_type` = 'store';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"commentModifyManage\"}]'
WHERE `authority_sn` = 'commentManage'
  AND `admin_type` = 'store';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"productAttributesTplModifyManage\"}]'
WHERE `authority_sn` = 'productAttributesTplManage'
  AND `admin_type` = 'store';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"accountModifyManage\"}]'
WHERE `authority_sn` = 'accountManagement'
  AND `admin_type` = 'store';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"提现\",\"authSn\":\"merchantShopWithdrawModifyManage\"}]'
WHERE `authority_sn` = 'withdrawManage'
  AND `admin_type` = 'store';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"timeDiscountModifyManage\"}]'
WHERE `authority_sn` = 'limitdiscountManage'
  AND `admin_type` = 'store';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"productGiftModifyManage\"}]'
WHERE `authority_sn` = 'productGiftManage'
  AND `admin_type` = 'store';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"salesmanProductModifyManage\"}]'
WHERE `authority_sn` = 'salesmanProductManage'
  AND `admin_type` = 'store';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"salesmanProductModifyManage\"}]'
WHERE `authority_sn` = 'materialManage'
  AND `admin_type` = 'store';

UPDATE `authority` SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"adminRoleUpdateManage\"}]' WHERE `authority_sn` = 'ShopRoleManage' AND `admin_type` = 'store';
UPDATE `authority` SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"adminUserShopModifyManage\"}]' WHERE `authority_sn` = 'accountEditingManage' AND `admin_type` = 'store';
UPDATE `authority` SET `child_auth` = '[{"authName":"配置获取","authSn":"shopAdminUserManageConfig"},{"authName":"编辑","authSn":"adminUserShopModifyManage"}]' WHERE `authority_sn` = 'EmployeeManagement' AND `admin_type` = 'store';
UPDATE `authority` SET `child_auth` = '[{"authName":"编辑","authSn":"shippingTplUpdateManage"}]' WHERE `authority_sn` = 'shopShippingTplManage' AND `admin_type` = 'store';

UPDATE `authority` SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"adminRoleUpdateManage\"}]' WHERE `authority_sn` = 'ShopRoleManage' AND `admin_type` = 'pickup';
UPDATE `authority` SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"adminUserShopModifyManage\"}]' WHERE `authority_sn` = 'accountEditingManage' AND `admin_type` = 'pickup';
UPDATE `authority` SET `child_auth` = '[{"authName":"配置获取","authSn":"shopAdminUserManageConfig"},{"authName":"编辑","authSn":"adminUserShopModifyManage"}]' WHERE `authority_sn` = 'EmployeeManagement' AND `admin_type` = 'pickup';


UPDATE `authority`
SET `child_auth` = CONCAT(
        TRIM(TRAILING ']' FROM `child_auth`),
        ',{"auth_name":"列表","auth_sn":"vendorListManage"}]'
                   )
WHERE `authority_sn` = 'orderManage';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"pcDecorateOtherManage\"}]'
WHERE `authority_sn` = 'mobileShopDecorate'
  AND `admin_type` = 'shop';

UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"merchantShopModifyManage\"}]'
WHERE `authority_sn` = 'vendorSettingManage' AND `admin_type` = 'shop';
UPDATE `authority`
SET `child_auth` = '[{\"authName\":\"编辑\",\"authSn\":\"merchantShopModifyManage\"}]'
WHERE `authority_sn` = 'vendorSettingManage' AND `admin_type` = 'store';

UPDATE `authority`
SET `child_auth` = '[{"authName":"其他模块查看","authSn":"pcDecorateOtherManage"},{"authName":"编辑","authSn":"decorateDiscreteModifyManage"}]'
WHERE `authority_sn` = 'mobileShopDecorate' AND `admin_type` = 'shop';