-- ----------------------------
-- Table structure for product_video
-- ----------------------------
DROP TABLE IF EXISTS `product_video`;
CREATE TABLE `product_video` (
                                 `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                 `video_id` mediumint(8) unsigned NOT NULL COMMENT '商品视频ID',
                                 `product_id` mediumint(8) unsigned NOT NULL DEFAULT '0' COMMENT '商品id',
                                 `video_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '视频url',
                                 `video_cover` varchar(255) NOT NULL COMMENT '视频封面',
                                 `format` varchar(255) NOT NULL COMMENT '视频格式后缀',
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='产品视频关联表';
-- ----------------------------
-- Records of product_video
-- ----------------------------


-- ----------------------------
-- Table structure for gallery_video_info
-- ----------------------------
DROP TABLE IF EXISTS `gallery_video_info`;
CREATE TABLE `gallery_video_info` (
                                      `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                      `shop_id` mediumint(8) unsigned NOT NULL DEFAULT '0' COMMENT '商户id',
                                      `gallery_id` mediumint(8) unsigned NOT NULL DEFAULT '0' COMMENT '相册id',
                                      `video_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '视频地址',
                                      `video_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '视频名称',
                                      `video_cover` varchar(255) DEFAULT NULL COMMENT '视频封面',
                                      `add_time` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
                                      `add_user_id` mediumint(8) unsigned NOT NULL DEFAULT '0' COMMENT '创建人id',
                                      `format` varchar(255) DEFAULT NULL COMMENT '视频格式后缀',
                                      `video_first_frame` varchar(255) DEFAULT NULL COMMENT '视频封面（第一帧）',
                                      `duration` varchar(255) DEFAULT NULL COMMENT '时长',
                                      `size` varchar(255) DEFAULT NULL COMMENT '大小',
                                      PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='视频相册';
-- ----------------------------
-- Records of gallery_video_info
-- ----------------------------

-- ----------------------------
-- Table structure for gallery_video
-- ----------------------------
DROP TABLE IF EXISTS `gallery_video`;
CREATE TABLE `gallery_video` (
                                 `id` mediumint(8) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                 `shop_id` mediumint(8) unsigned NOT NULL DEFAULT '0' COMMENT '商户id',
                                 `parent_id` mediumint(8) unsigned NOT NULL DEFAULT '0' COMMENT '父级id',
                                 `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '名称',
                                 `sort` mediumint(8) unsigned NOT NULL DEFAULT '50' COMMENT '排序',
                                 `add_time` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
                                 `add_user_id` mediumint(8) unsigned NOT NULL DEFAULT '1' COMMENT '创建人id',
                                 PRIMARY KEY (`id`) USING BTREE,
                                 KEY `idx_shop_parent` (`shop_id`,`parent_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='视频相册文件夹';
-- ----------------------------
-- Records of gallery_video
-- ----------------------------


UPDATE `config` SET `biz_val` = '/api/user/login/wechatServer' WHERE `biz_code` = 'wechatServerUrl' AND `is_del` = 0;