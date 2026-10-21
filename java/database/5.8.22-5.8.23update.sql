-- 删除老询价管理
delete from `authority` where authority_id =  30001007;
delete from `authority` where authority_id =  30001008;

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001017, 'taxRateManage', '税率管理', 39, 50, 1, '[{\"authName\":\"编辑\",\"authSn\":\"taxRateModifyManage\"}]', '', '', 0, 'admin');

-- 会员标签组
CREATE TABLE `member_tag_group` (
                                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                    `group_name` VARCHAR(100) NOT NULL COMMENT '组名称',
                                    `is_top` INT NOT NULL DEFAULT 0 COMMENT '置顶权重；0-未置顶，正整数越大表示越新置顶',
                                    `create_time` BIGINT DEFAULT NULL COMMENT '创建时间',
                                    `create_by_id` BIGINT DEFAULT NULL COMMENT '创建人主键',
                                    `create_by_name` VARCHAR(50) DEFAULT NULL COMMENT '创建人名称',
                                    `update_time` BIGINT DEFAULT NULL COMMENT '更新时间',
                                    `update_by_id` BIGINT DEFAULT NULL COMMENT '编辑人主键',
                                    `update_by_name` VARCHAR(50) DEFAULT NULL COMMENT '更新人名称',
                                    `is_del` INT NOT NULL DEFAULT 0 COMMENT '是否删除；0-否，1-是',
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员标签组';

-- 会员标签
CREATE TABLE `member_tag` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                              `group_id` BIGINT NOT NULL COMMENT '所属标签组ID',
                              `tag_name` VARCHAR(100) NOT NULL COMMENT '标签名称',
                              `create_time` BIGINT DEFAULT NULL COMMENT '创建时间',
                              `create_by_id` BIGINT DEFAULT NULL COMMENT '创建人主键',
                              `create_by_name` VARCHAR(50) DEFAULT NULL COMMENT '创建人名称',
                              `update_time` BIGINT DEFAULT NULL COMMENT '更新时间',
                              `update_by_id` BIGINT DEFAULT NULL COMMENT '编辑人主键',
                              `update_by_name` VARCHAR(50) DEFAULT NULL COMMENT '更新人名称',
                              `is_del` INT NOT NULL DEFAULT 0 COMMENT '是否删除；0-否，1-是',
                              PRIMARY KEY (`id`),
                              KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员标签';

-- 用户会员标签关联
CREATE TABLE `user_member_tag` (
                                   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                   `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                   `tag_id` BIGINT NOT NULL COMMENT '标签ID',
                                   `create_time` BIGINT DEFAULT NULL COMMENT '打标时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_user_tag` (`user_id`, `tag_id`),
                                   KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会员标签关联';


-- 会员标签菜单权限（挂在"会员管理" parent_id=10000422 下）
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES (40001080, 'memberTagManage', '会员标签', 10000422, 54, 1, '[{"auth_name":"编辑","auth_sn":"memberTagUpdateManage"}]', 'user/member_tag/list/', '', 0, 'admin');


-- 商品样式配置表
CREATE TABLE `product_style` (
                                 `style_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '样式ID',
                                 `style_name` varchar(120) NOT NULL DEFAULT '' COMMENT '样式名称',
                                 `style_type` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '样式类型 1=边框背景 2=活动背景',
                                 `style_image` varchar(500) NOT NULL DEFAULT '' COMMENT '样式图片URL',
                                 `start_time` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '生效开始时间（活动背景必填）',
                                 `end_time` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '生效结束时间（活动背景必填）',
                                 `status` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态 0=禁用 1=启用',
                                 `shop_id` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '店铺ID，0=平台',
                                 `add_time` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
                                 `update_time` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新时间',
                                 `is_delete` tinyint(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',
                                 PRIMARY KEY (`style_id`) USING BTREE,
                                 KEY `idx_style_type` (`style_type`) USING BTREE,
                                 KEY `idx_shop_id` (`shop_id`) USING BTREE,
                                 KEY `idx_status` (`status`, `is_delete`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品样式配置表';

-- 商品样式关联表
CREATE TABLE `product_style_item` (
                                      `item_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关联ID',
                                      `style_id` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '样式ID',
                                      `item_type` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '关联类型 1=商品 2=分组 3=品牌',
                                      `ref_id` int(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT '关联目标ID（商品ID/分组ID/品牌ID）',
                                      `add_time` int(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建时间',
                                      PRIMARY KEY (`item_id`) USING BTREE,
                                      UNIQUE KEY `uk_style_type_ref` (`style_id`, `item_type`, `ref_id`) USING BTREE,
                                      KEY `idx_ref_id` (`item_type`, `ref_id`) USING BTREE,
                                      KEY `idx_style_id` (`style_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品样式关联表';


-- product_style 表新增 pc_image 字段
ALTER TABLE `product_style`
    ADD COLUMN `pc_image` varchar(500) NOT NULL DEFAULT '' COMMENT 'PC端样式图片URL' AFTER `style_image`;

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (40001032, 'promotionStyleManage', '营销样式', 5, 50, 1, '[{"authName":"编辑","authSn":"promotionStyleModifyManage"}]', '', 'iconfont-tig icon-yingxiaoyangshi', 0, 'admin');


INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (60002031, 'promotionStyleManage', '营销样式', 10005, 50, 1, '[{"authName":"编辑","authSn":"promotionStyleModifyManage"}]', '', 'iconfont-tig icon-yingxiaoyangshi', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (60002032, 'promotionStyleManage', '营销样式', 20000003, 50, 1, '[{"authName":"编辑","authSn":"promotionStyleModifyManage"}]', '', 'iconfont-tig icon-yingxiaoyangshi', 0, 'store');