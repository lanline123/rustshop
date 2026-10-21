UPDATE authority
SET authority_sn = 'intraCityDeliveryManage',
    child_auth = '[{"authName":"编辑","authSn":"storeIntraCityDeliveryUpdateManage"}]'
WHERE authority_id = 20000163;

UPDATE `message_template` SET `message_id` = 8, `type` = 7, `template_name` = '获取验证码', `to_userid` = '', `template_id` = '', `template_num` = '', `content` = '验证码：${code}（邮箱绑定，请完成验证），如非本人操作，请忽略本邮件' WHERE `id` = 30;

ALTER TABLE `shop_local_delivery_config`
    MODIFY COLUMN `delivery_radius` decimal(11, 2) NOT NULL DEFAULT 0.00 COMMENT '配送半径（公里）';


CREATE TABLE `buyer_show` (
                              `buyer_show_id` mediumint NOT NULL AUTO_INCREMENT COMMENT '自增ID，买家秀ID',
                              `range_data` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '关联商品数据 （json）',
                              `user_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '用户id（非真实用户为0）',
                              `buyer_photo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '买家头像',
                              `buyer_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '买家名称',
                              `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '文字内容',
                              `group_data` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '所属分组（json）',
                              `add_time` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建时间 ',
                              `sort_order` mediumint DEFAULT '0' COMMENT '排序',
                              `status` tinyint NOT NULL COMMENT '状态（0：待审核，1：通过，2：拒绝）',
                              `is_show` tinyint NOT NULL COMMENT '是否公开（0：隐藏，1：公开）',
                              PRIMARY KEY (`buyer_show_id`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='买家秀';

CREATE TABLE `buyer_show_comment` (
                                      `buyer_show_comment_id` mediumint NOT NULL AUTO_INCREMENT COMMENT '自增ID，买家秀评论ID',
                                      `parent_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '上级评论id',
                                      `buyer_show_id` mediumint NOT NULL COMMENT '买家秀ID',
                                      `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0：待审核，1：通过，2：拒绝）',
                                      `is_top` tinyint(3) unsigned zerofill NOT NULL DEFAULT '000' COMMENT '是否置顶（0：否，1：是）',
                                      `is_show` tinyint NOT NULL COMMENT '是否显示（0：隐藏，1：公开）',
                                      `user_id` int unsigned NOT NULL DEFAULT '0' COMMENT '用户id（非真实用户为0）',
                                      `user_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户名称',
                                      `user_photo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户头像',
                                      `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '评论内容',
                                      `add_time` bigint unsigned NOT NULL DEFAULT '0' COMMENT '发布时间',
                                      `comment_pic` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图片评论',
                                      PRIMARY KEY (`buyer_show_comment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=91 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='买家秀评论';

CREATE TABLE `buyer_show_gallery` (
                                      `pic_id` mediumint unsigned NOT NULL AUTO_INCREMENT COMMENT '相册ID',
                                      `buyer_show_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '买家秀id ',
                                      `pic_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '实际图片url ',
                                      `pic_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '图片说明信息 ',
                                      `pic_thumb` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '微缩图片url ',
                                      `pic_original` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '原图url',
                                      `pic_large` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '高清图片url ',
                                      `sort_order` int unsigned NOT NULL DEFAULT '1' COMMENT '排序',
                                      PRIMARY KEY (`pic_id`)
) ENGINE=InnoDB AUTO_INCREMENT=121 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='买家秀相册';

CREATE TABLE `buyer_show_group` (
                                    `buyer_show_group_id` mediumint NOT NULL AUTO_INCREMENT COMMENT '自增ID，买家秀分组ID',
                                    `group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分组名称',
                                    `product_group_id` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品分组id（json）',
                                    `shop_id` int unsigned NOT NULL DEFAULT '0' COMMENT '店铺id',
                                    PRIMARY KEY (`buyer_show_group_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='买家秀分组';

CREATE TABLE `buyer_show_like` (
                                   `id` mediumint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `buyer_show_id` mediumint NOT NULL COMMENT '买家秀ID',
                                   `user_id` mediumint NOT NULL COMMENT '用户ID',
                                   `add_time` bigint unsigned NOT NULL DEFAULT '0' COMMENT '发布时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_comment_user` (`buyer_show_id`,`user_id`),
                                   KEY `idx_user` (`user_id`),
                                   KEY `idx_buyer_show` (`buyer_show_id`)
) ENGINE=InnoDB AUTO_INCREMENT=125 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='买家秀点赞表';

CREATE TABLE `buyer_show_video` (
                                    `id` mediumint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
                                    `video_id` mediumint unsigned NOT NULL COMMENT '视频ID',
                                    `buyer_show_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '买家秀d',
                                    `video_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '视频url',
                                    `video_cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '视频封面',
                                    `format` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '视频格式后缀',
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='买家秀视频';

CREATE TABLE `comment_like` (
                                `id` mediumint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `comment_id` mediumint NOT NULL COMMENT '评论ID',
                                `user_id` mediumint NOT NULL COMMENT '用户ID',
                                `add_time` bigint unsigned NOT NULL DEFAULT '0' COMMENT '发布时间',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_comment_user` (`comment_id`,`user_id`),
                                KEY `idx_user` (`user_id`),
                                KEY `idx_comment` (`comment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评论点赞表';

INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('buyerShowEnabled', '1', 1760576775, 1, 'admin', 1760576775, 1, 'admin', 0);
INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('buyerShowMobileShowName', '买家秀', 1760576775, 1, 'admin', 1760576775, 1, 'admin', 0);
INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('buyerShowGuidanceContent', '评论发布', 1760576775, 1, 'admin', 1760576775, 1, 'admin', 0);
INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ('buyerShowTemplateStyle', '0', 1760576775, 1, 'admin', 1760576775, 1, 'admin', 0);

UPDATE `decorate_discrete` SET `decorate_sn` = 'mobileNav', `decorate_name` = '移动端导航', `data` = '{\"navList\":[{\"picActiveThumb\":\"https://oss.tigshop.com/img/gallery/demo/16802475870v7jHejQtgFvn7Yd6e!!pic200x200.png\",\"picActiveUrl\":\"https://oss.tigshop.com/img/gallery/demo/16802475870v7jHejQtgFvn7Yd6e!!pic.png\",\"picId\":\"583\",\"picThumb\":\"https://oss.tigshop.com/img/gallery/demo/1680247587QG2y44h7a7f0M1dx9T!!pic200x200.png\",\"picUrl\":\"https://oss.tigshop.com/img/gallery/demo/1680247587QG2y44h7a7f0M1dx9T!!pic.png\",\"picName\":\"ico_1\",\"picTitle\":\"首页\",\"picLink\":{\"path\":\"base\",\"label\":\"商城首页\",\"name\":\"商城首页\",\"link\":\"/\"}},{\"picTitle\":\"分类\",\"picThumb\":\"https://oss.tigshop.com/img/gallery/demo/16802476274VrIgYXSKI9GN9O3yG!!pic200x200.png\",\"picUrl\":\"https://oss.tigshop.com/img/gallery/demo/16802476274VrIgYXSKI9GN9O3yG!!pic.png\",\"picActiveThumb\":\"https://oss.tigshop.com/img/gallery/demo/1680247634WITeORzHMPv2y72Nxr!!pic200x200.png\",\"picActiveUrl\":\"https://oss.tigshop.com/img/gallery/demo/1680247634WITeORzHMPv2y72Nxr!!pic.png\",\"picLink\":{\"path\":\"base\",\"label\":\"分类页面（仅分类）\",\"name\":\"分类页面（仅分类）\",\"link\":\"list\"},\"picId\":\"585\",\"picName\":\"ico_2\"},{\"picTitle\":\"买家秀\",\"picThumb\":\"https://oss.tigshop.com/img/gallery/202510/1760602140ZGiA9QykUSp7qbReLh.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200\",\"picUrl\":\"https://oss.tigshop.com/img/gallery/202510/1760602140ZGiA9QykUSp7qbReLh.jpeg\",\"picActiveThumb\":\"https://oss.tigshop.com/img/gallery/202510/1760602137zsynfEFJGSqabrb7VO.jpeg?x-oss-process=image/resize,m_pad,h_200,h_200\",\"picActiveUrl\":\"https://oss.tigshop.com/img/gallery/202510/1760602137zsynfEFJGSqabrb7VO.jpeg\",\"picLink\":{\"path\":\"base\",\"label\":\"买家秀\",\"name\":\"买家秀\",\"link\":\"buyerShow\"},\"picId\":1730,\"picName\":\"1760602140ZGiA9QykUSp7qbReLh\"},{\"picActiveThumb\":\"https://oss.tigshop.com/img/gallery/demo/1680247659hhhxwNMyQg7kBDrxnc!!pic200x200.png\",\"picActiveUrl\":\"https://oss.tigshop.com/img/gallery/demo/1680247659hhhxwNMyQg7kBDrxnc!!pic.png\",\"picId\":\"587\",\"picThumb\":\"https://oss.tigshop.com/img/gallery/demo/1680247656ZxrHGLra5jt3Wausu3!!pic200x200.png\",\"picUrl\":\"https://oss.tigshop.com/img/gallery/demo/1680247656ZxrHGLra5jt3Wausu3!!pic.png\",\"picName\":\"ico_3\",\"picTitle\":\"购物车\",\"picLink\":{\"path\":\"base\",\"label\":\"购物车\",\"name\":\"购物车\",\"link\":\"cart\"}},{\"picActiveThumb\":\"https://oss.tigshop.com/img/gallery/demo/1680247682VX4iwQSO82hyzUedDz!!pic200x200.png\",\"picActiveUrl\":\"https://oss.tigshop.com/img/gallery/demo/1680247682VX4iwQSO82hyzUedDz!!pic.png\",\"picId\":\"589\",\"picThumb\":\"https://oss.tigshop.com/img/gallery/demo/1680247679vqWSO1Ci9sAGJCzzZa!!pic200x200.png\",\"picUrl\":\"https://oss.tigshop.com/img/gallery/demo/1680247679vqWSO1Ci9sAGJCzzZa!!pic.png\",\"picName\":\"ico_4\",\"picTitle\":\"我的\",\"picLink\":{\"path\":\"base\",\"label\":\"会员首页\",\"name\":\"会员首页\",\"link\":\"member\"}}],\"picList\":[{\"picActiveThumb\":\"https://oss.tigshop.com/img/gallery/202403/17111886010Pg60qIrJEzwe3tw06.gif?x-oss-process=image/resize,m_pad,h_200,h_200\",\"picActiveUrl\":\"https://oss.tigshop.com/img/gallery/202403/17111886010Pg60qIrJEzwe3tw06.gif\",\"picId\":\"1003\",\"picThumb\":\"https://oss.tigshop.com/img/gallery/202403/17111886010Pg60qIrJEzwe3tw06.gif?x-oss-process=image/resize,m_pad,h_200,h_200\",\"picUrl\":\"https://oss.tigshop.com/img/gallery/202403/17111886010Pg60qIrJEzwe3tw06.gif\",\"picName\":\"face\",\"picTitle\":\"首页\",\"picLink\":{\"path\":\"base\",\"label\":\"商城首页\",\"name\":\"/\",\"link\":\"/\",\"appLink\":\"home/index\"}},\"\",\"\"],\"backgroundColor\":\"\",\"backgroundPic\":{\"picUrl\":\"https://oss.tigshop.com/img/gallery/202312/1701745194DrxHETqOSNvfk7pg2U.jpg\",\"picThumb\":\"https://oss.tigshop.com/img/gallery/202312/1701745194DrxHETqOSNvfk7pg2U.jpg?x-oss-process=image/resize,m_lfit,h_200,h_200\",\"picId\":\"940\",\"picName\":\"02b5283dff3aa3d7400x400_big\"}}', `shop_id` = 0 WHERE `decorate_sn` = 'mobileNav' and `shop_id` = 0;

INSERT INTO `authority`(`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES ( 'buyerShowManage', '买家秀', 10000413, 50, 1, '[{\"authName\":\"保存买家秀配置\",\"authSn\":\"buyerShowSettingsUpdateManage\"},{\"authName\":\"编辑\",\"authSn\":\"buyerShowUpdateManage\"}]', '', '', 0, 'admin');
