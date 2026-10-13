ALTER TABLE user_message ADD COLUMN type TINYINT(1) NOT NULL DEFAULT '0' COMMENT '1:订单通知';

UPDATE user_message
SET type = 1
WHERE
    title IN ('会员下单时发送短信', '用户支付成功通知', '发货通知', '退款成功');


-- ----------------------------
-- Table structure for virtual_user_info
-- ----------------------------
DROP TABLE IF EXISTS `virtual_user_info`;
CREATE TABLE `virtual_user_info`  (
                                      `id` int(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增id号',
                                      `photo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '头像',
                                      `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
                                      PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '虚拟用户信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of virtual_user_info
-- ----------------------------
INSERT INTO `virtual_user_info` VALUES (1, 'https://oss.tigshop.com/img/gallery/202512/17649142272eb354IdKjGeDvvQPy.jpeg', '夏天离不开的冰淇淋');
INSERT INTO `virtual_user_info` VALUES (2, 'https://oss.tigshop.com/img/gallery/202512/1764914228gTVhbhtRPbmLrYHJpL.jpeg', '散场荒人');
INSERT INTO `virtual_user_info` VALUES (3, 'https://oss.tigshop.com/img/gallery/202512/1764914229JLj89mhjYqL1TPGZpr.jpeg', '时间留下了对的人');
INSERT INTO `virtual_user_info` VALUES (4, 'https://oss.tigshop.com/img/gallery/202512/17649142303FEHRt3fwAOkJobVuA.jpeg', '惯用的方式已删除');
INSERT INTO `virtual_user_info` VALUES (5, 'https://oss.tigshop.com/img/gallery/202512/17649142311SJ0oMRckR31gnRab6.jpeg', '从未走远在你身边');
INSERT INTO `virtual_user_info` VALUES (7, 'https://oss.tigshop.com/img/gallery/202512/1764914295Ep7YDtKfFio37BZ4we.jpeg', '她');
INSERT INTO `virtual_user_info` VALUES (8, 'https://oss.tigshop.com/img/gallery/202512/1764914296Dbd3qHLdbfORreRbO0.jpeg', '思念是一种饼噢一种饼');
INSERT INTO `virtual_user_info` VALUES (9, 'https://oss.tigshop.com/img/gallery/202512/1764914297JByhOZ723aO5EQ00eH.jpeg', '我是灯泡因为我会发光');
INSERT INTO `virtual_user_info` VALUES (10, 'https://oss.tigshop.com/img/gallery/202512/1764914298fudrZ4diOjheWAs04m.jpeg', '不开心不要脸');
INSERT INTO `virtual_user_info` VALUES (11, 'https://oss.tigshop.com/img/gallery/202512/1764914583Lf5xxJfrATHxK5Zcd6.jpeg', '纵然情长');
INSERT INTO `virtual_user_info` VALUES (12, 'https://oss.tigshop.com/img/gallery/202512/1764914605ZuFQqXl6UWYZoQBYif.jpeg', '飞天小女警');
INSERT INTO `virtual_user_info` VALUES (13, 'https://oss.tigshop.com/img/gallery/202512/1764914616sqDm3jlj3ktNGF9jvj.jpeg', '蓝了她眼中海的少年');
INSERT INTO `virtual_user_info` VALUES (14, 'https://oss.tigshop.com/img/gallery/202512/1764914658hd1XhNi1V1ilrOfaBH.jpeg', '深鱼不泪不安眠');
INSERT INTO `virtual_user_info` VALUES (15, 'https://oss.tigshop.com/img/gallery/202512/1764914669PMHc7TOAveGlWiVwib.jpeg', '乱棋残局');
INSERT INTO `virtual_user_info` VALUES (16, 'https://oss.tigshop.com/img/gallery/202512/17649146800HcSdxQiYAgjRDOIhw.jpeg', '掏心');
INSERT INTO `virtual_user_info` VALUES (17, 'https://oss.tigshop.com/img/gallery/202512/1764914691ppuhtt4zqCnXyuWKRS.jpeg', '吻到情动时');
INSERT INTO `virtual_user_info` VALUES (18, 'https://oss.tigshop.com/img/gallery/202512/1764914703de29QuCkvo8DCoGkt5.jpeg', '没事找事睡死完事');
INSERT INTO `virtual_user_info` VALUES (19, 'https://oss.tigshop.com/img/gallery/202512/17649147144zVsY2KujPOOEraugl.jpeg', '逗比女王怪癖女神');
INSERT INTO `virtual_user_info` VALUES (20, 'https://oss.tigshop.com/img/gallery/202512/1764914739xQExT3hHyXxyvOyru3.jpeg', '迷离一场醉欢');
INSERT INTO `virtual_user_info` VALUES (21, 'https://oss.tigshop.com/img/gallery/202512/1764914750exqFWflR9ycfah5z3H.jpeg', '拽');
INSERT INTO `virtual_user_info` VALUES (22, 'https://oss.tigshop.com/img/gallery/202512/1764914761OVlRhFSTu59IQiO9zA.jpeg', '温森梅酒');
INSERT INTO `virtual_user_info` VALUES (23, 'https://oss.tigshop.com/img/gallery/202512/1764914772ntQJRLbVY2yca58cFI.jpeg', '骗够了告诉我');
INSERT INTO `virtual_user_info` VALUES (24, 'https://oss.tigshop.com/img/gallery/202512/1764914783trlRblSTPpOYvlDH8V.jpeg', '閻魔愛');
INSERT INTO `virtual_user_info` VALUES (25, 'https://oss.tigshop.com/img/gallery/202512/176491479422OagZr2PNkwEaqFNO.jpeg', '荒南');
INSERT INTO `virtual_user_info` VALUES (26, 'https://oss.tigshop.com/img/gallery/202512/1764914805dNAlz3SqSHBR92TFOE.jpeg', '跪下唱征服吧你');
INSERT INTO `virtual_user_info` VALUES (27, 'https://oss.tigshop.com/img/gallery/202512/1764915202UuP76vBCEbHQqQqngd.jpeg', '频回首');
INSERT INTO `virtual_user_info` VALUES (28, 'https://oss.tigshop.com/img/gallery/202512/1764915213zAbWMEwNvr3gdFZO6r.jpeg', '掰弯直男');
INSERT INTO `virtual_user_info` VALUES (29, 'https://oss.tigshop.com/img/gallery/202512/1764915224b1vhZugeW5ORg6r73r.jpeg', '爱不是非你不可');
INSERT INTO `virtual_user_info` VALUES (30, 'https://oss.tigshop.com/img/gallery/202512/1764915236v6YLn0ebi92soklb3g.jpeg', '御街行');
INSERT INTO `virtual_user_info` VALUES (31, 'https://oss.tigshop.com/img/gallery/202512/1764915247uljV7HXMiNUwxmTOHN.jpeg', '韩七录安初夏');
INSERT INTO `virtual_user_info` VALUES (32, 'https://oss.tigshop.com/img/gallery/202512/1764915260tc2p39BDz4ra84NT9F.jpeg', '爱上你就不会离开你');
INSERT INTO `virtual_user_info` VALUES (33, 'https://oss.tigshop.com/img/gallery/202512/1764915271yr416n4ssW5QPWHj2m.jpeg', '纵使容颜老闺蜜永不散');
INSERT INTO `virtual_user_info` VALUES (34, 'https://oss.tigshop.com/img/gallery/202512/1764915282WIx7gScrANMhmMVR2Y.jpeg', '姐是你触犯不起的神');
INSERT INTO `virtual_user_info` VALUES (35, 'https://oss.tigshop.com/img/gallery/202512/17649152935VUK3VR8emPHmE0PDd.jpeg', '唇边人');
INSERT INTO `virtual_user_info` VALUES (36, 'https://oss.tigshop.com/img/gallery/202512/176491530484Pc9mXZFPOtcsJgh8.jpeg', '拉面超人拉面怪兽');
INSERT INTO `virtual_user_info` VALUES (37, 'https://oss.tigshop.com/img/gallery/202512/17649153156GcFdlYa3CCtZ9qxOm.jpeg', '埃菲尔读不懂我的悲伤');
INSERT INTO `virtual_user_info` VALUES (38, 'https://oss.tigshop.com/img/gallery/202512/1764915326HaqjrpjDdoaXlYZDFV.jpeg', '勞資沒心隨便妳傷');
INSERT INTO `virtual_user_info` VALUES (39, 'https://oss.tigshop.com/img/gallery/202512/1764915337FytAwcO8wqYqXhLQkI.jpeg', '予你权和我');
INSERT INTO `virtual_user_info` VALUES (40, 'https://oss.tigshop.com/img/gallery/202512/1764915348lA04pPAAU2JMLNSna4.jpeg', '已打烊的小酒馆');
INSERT INTO `virtual_user_info` VALUES (41, 'https://oss.tigshop.com/img/gallery/202512/17649167758i3T682ffaT3l1O5cy.jpeg', '风把故事吹进整个树林');
INSERT INTO `virtual_user_info` VALUES (42, 'https://oss.tigshop.com/img/gallery/202512/1764916786mX8dULqX6mBAq2iJTG.jpeg', '矜贵');
INSERT INTO `virtual_user_info` VALUES (43, 'https://oss.tigshop.com/img/gallery/202512/1764916797HdTifW1vQOFDERRZwc.jpeg', '要离开趁早');
INSERT INTO `virtual_user_info` VALUES (44, 'https://oss.tigshop.com/img/gallery/202512/1764916808OQIfleE5Yulo7l6gY1.jpeg', '偶是你流泪的小骗子');
INSERT INTO `virtual_user_info` VALUES (45, 'https://oss.tigshop.com/img/gallery/202512/1764916819tAMMjYdZzaISCykTMQ.jpeg', '伴我一生长久可好');
INSERT INTO `virtual_user_info` VALUES (46, 'https://oss.tigshop.com/img/gallery/202512/1764916830AW6cifyPYetq0gmOJM.jpeg', '疲');
INSERT INTO `virtual_user_info` VALUES (47, 'https://oss.tigshop.com/img/gallery/202512/1764916841gNqogHFiFjtttOvKIM.jpeg', '寻歡');
INSERT INTO `virtual_user_info` VALUES (48, 'https://oss.tigshop.com/img/gallery/202512/1764916852h1qEDmAAko6HDyFbZ7.jpeg', '戏无你');
INSERT INTO `virtual_user_info` VALUES (49, 'https://oss.tigshop.com/img/gallery/202512/1764916863hjnVAJzIGU50NP8Ad0.jpeg', '赤脚走进荆棘丛林');
INSERT INTO `virtual_user_info` VALUES (50, 'https://oss.tigshop.com/img/gallery/202512/1764916875N5G3UFjFhDuXvZHCEU.jpeg', '巷中猫');
INSERT INTO `virtual_user_info` VALUES (51, 'https://oss.tigshop.com/img/gallery/202512/1764916886iKpsfjeaMLNhe8hMd1.jpeg', '许是非');
INSERT INTO `virtual_user_info` VALUES (52, 'https://oss.tigshop.com/img/gallery/202512/17649168970dEcQ6I1UGTICcGsGv.jpeg', '三年痛七年痒十年劫');
INSERT INTO `virtual_user_info` VALUES (53, 'https://oss.tigshop.com/img/gallery/202512/17649169082zLJc7XtOAiXnfI0AW.jpeg', '时间不是万能的解药');
INSERT INTO `virtual_user_info` VALUES (54, 'https://oss.tigshop.com/img/gallery/202512/1764918255Sbk8X0TrYyMNipF6bK.jpeg', '荒');
INSERT INTO `virtual_user_info` VALUES (55, 'https://oss.tigshop.com/img/gallery/202512/1764918267aeW68eEovY49uncKQQ.jpeg', '花开花飞花似画');
INSERT INTO `virtual_user_info` VALUES (56, 'https://oss.tigshop.com/img/gallery/202512/1764918278qoSl68FThalarwqv3t.jpeg', '思厄');
INSERT INTO `virtual_user_info` VALUES (57, 'https://oss.tigshop.com/img/gallery/202512/1764918289GSkSujEYOsOWoFgARr.jpeg', '清晨的第一缕阳光');
INSERT INTO `virtual_user_info` VALUES (58, 'https://oss.tigshop.com/img/gallery/202512/1764918300a1EEMAy8fKOqBlrtOo.jpeg', '爱一个人原来那么累');
INSERT INTO `virtual_user_info` VALUES (59, 'https://oss.tigshop.com/img/gallery/202512/1764918311onjOO1B4dc5jXBjx0a.jpeg', '无力说再见');
INSERT INTO `virtual_user_info` VALUES (60, 'https://oss.tigshop.com/img/gallery/202512/1764918322BR33h5QPNMVBUCthbO.jpeg', '萌腻儿');
INSERT INTO `virtual_user_info` VALUES (61, 'https://oss.tigshop.com/img/gallery/202512/1764918333Xrr5uImf9ddSGAot46.jpeg', '我喜欢的是曾经的你');
INSERT INTO `virtual_user_info` VALUES (62, 'https://oss.tigshop.com/img/gallery/202512/1764918344bjEg6OxTNL1xLcUMco.jpeg', '最后一丝无声的微笑');
INSERT INTO `virtual_user_info` VALUES (63, 'https://oss.tigshop.com/img/gallery/202512/1764918355QyMryCsF09drTEmuNg.jpeg', '媚');
INSERT INTO `virtual_user_info` VALUES (64, 'https://oss.tigshop.com/img/gallery/202512/1764918366XF2GvypH4OEPdhzwu9.jpeg', '微风吹来老故事');
INSERT INTO `virtual_user_info` VALUES (65, 'https://oss.tigshop.com/img/gallery/202512/1764918377YC7TpoP77uGpcnhItE.jpeg', '一见一场如故');
INSERT INTO `virtual_user_info` VALUES (66, 'https://oss.tigshop.com/img/gallery/202512/1764918388fnmau4O2BqqsahEyzf.jpeg', '失心疯baby');
INSERT INTO `virtual_user_info` VALUES (67, 'https://oss.tigshop.com/img/gallery/202512/1764918399d1sbCTibIb9GNGKGb7.jpeg', '老师你娘挂了卐回去');
INSERT INTO `virtual_user_info` VALUES (68, 'https://oss.tigshop.com/img/gallery/202512/1764918410tYpzk7HLY05XQ1XejS.jpeg', '我贼拉拉的爱你');
INSERT INTO `virtual_user_info` VALUES (69, 'https://oss.tigshop.com/img/gallery/202512/176491842141uzNDQJqdjU1oH6NS.jpeg', '霸道小萝莉温柔大男人');
INSERT INTO `virtual_user_info` VALUES (70, 'https://oss.tigshop.com/img/gallery/202512/1764918432Ztnzp62WgqFOWsYt1T.jpeg', '梦里有你说爱我的情景');
INSERT INTO `virtual_user_info` VALUES (71, 'https://oss.tigshop.com/img/gallery/202512/1764918443jhgsA5t9vCKVjfhJsR.jpeg', '清酒无饮烈酒烧侯');
INSERT INTO `virtual_user_info` VALUES (72, 'https://oss.tigshop.com/img/gallery/202512/1764918454PlSCPnJvPMMaNJh8yB.jpeg', '刺我一个吻');
INSERT INTO `virtual_user_info` VALUES (73, 'https://oss.tigshop.com/img/gallery/202512/1764918465BmeIa5KKlxBeFucfPV.jpeg', '栀友');
INSERT INTO `virtual_user_info` VALUES (74, 'https://oss.tigshop.com/img/gallery/202512/1764918660eIiLWposdqjTRm3uIJ.jpeg', '光阴似歌岁月如歌');
INSERT INTO `virtual_user_info` VALUES (75, 'https://oss.tigshop.com/img/gallery/202512/17649186714yUaLbTL0gYoagodN5.jpeg', '万死不辞');
INSERT INTO `virtual_user_info` VALUES (76, 'https://oss.tigshop.com/img/gallery/202512/17649186823eWTJkCNeUXjWceg2T.jpeg', 'ormosia红豆');
INSERT INTO `virtual_user_info` VALUES (77, 'https://oss.tigshop.com/img/gallery/202512/1764918693xLnJrkd9ncN26UxavL.jpeg', '我当你没来过');
INSERT INTO `virtual_user_info` VALUES (78, 'https://oss.tigshop.com/img/gallery/202512/1764918704iJuaLTzbkEDOdFPEIG.jpeg', '做自己的太阳');
INSERT INTO `virtual_user_info` VALUES (79, 'https://oss.tigshop.com/img/gallery/202512/1764918715wwIkqAxE3ppGfTUrjs.jpeg', '放你南飞');
INSERT INTO `virtual_user_info` VALUES (80, 'https://oss.tigshop.com/img/gallery/202512/1764918726XHMpXYh3KMUIPTVRek.jpeg', '不要叫大脸妹');
INSERT INTO `virtual_user_info` VALUES (81, 'https://oss.tigshop.com/img/gallery/202512/17649187370Vm49lIs4nPsWGt9ic.jpeg', '痛过之后的醒悟最刻骨');
INSERT INTO `virtual_user_info` VALUES (82, 'https://oss.tigshop.com/img/gallery/202512/1764918748JgZldCFWfhStZZDEQi.jpeg', '收起你那虚伪的嘴脸');
INSERT INTO `virtual_user_info` VALUES (83, 'https://oss.tigshop.com/img/gallery/202512/1764918759rk0GoACMrtc2BXkhKV.jpeg', '逆我拖出去斩');
INSERT INTO `virtual_user_info` VALUES (84, 'https://oss.tigshop.com/img/gallery/202512/1764918770eQBLw82YLTHTQQVOBr.jpeg', '想念成痴眼泪成诗');
INSERT INTO `virtual_user_info` VALUES (85, 'https://oss.tigshop.com/img/gallery/202512/1764918964Y5LglylxvVyo0S5jA0.jpeg', '梦涯');
INSERT INTO `virtual_user_info` VALUES (86, 'https://oss.tigshop.com/img/gallery/202512/1764918975zwWDFy9rvnJmXtVSo6.jpeg', '带刺怎么拥抱');
INSERT INTO `virtual_user_info` VALUES (87, 'https://oss.tigshop.com/img/gallery/202512/1764918986wkXrWa8ISY01czaVgw.jpeg', '心似海');
INSERT INTO `virtual_user_info` VALUES (88, 'https://oss.tigshop.com/img/gallery/202512/1764918997kylJK1CPitoVcebyOo.jpeg', '弦鼓');
INSERT INTO `virtual_user_info` VALUES (89, 'https://oss.tigshop.com/img/gallery/202512/1764919008hczyn08pXygnKqPMiK.jpeg', '赃');
INSERT INTO `virtual_user_info` VALUES (90, 'https://oss.tigshop.com/img/gallery/202512/1764920702P3FexIV6VOKcJefPEH.jpeg', '爱情算个Diao');
INSERT INTO `virtual_user_info` VALUES (91, 'https://oss.tigshop.com/img/gallery/202512/1764920713mJd09bpx0HNIDeA6O7.jpeg', '亲我是你的大姨夫');
INSERT INTO `virtual_user_info` VALUES (92, 'https://oss.tigshop.com/img/gallery/202512/1764920724wgNaEW2UTqLwD7FeG3.jpeg', '欢迎光临我的心世界');
INSERT INTO `virtual_user_info` VALUES (93, 'https://oss.tigshop.com/img/gallery/202512/1764920736QGhyCkdEHYY3YRSULf.jpeg', '苍');
INSERT INTO `virtual_user_info` VALUES (94, 'https://oss.tigshop.com/img/gallery/202512/1764920747MRKAGyRvMcJHM7aJUN.jpeg', '悄然不知');
INSERT INTO `virtual_user_info` VALUES (95, 'https://oss.tigshop.com/img/gallery/202512/1764920934FuE8Ys4LJK5ARJXo8m.jpeg', '毒情独终');
INSERT INTO `virtual_user_info` VALUES (96, 'https://oss.tigshop.com/img/gallery/202512/1764920945TpboBtdkfK4LknBXRI.jpeg', '森林屿麓深海屿渔');
INSERT INTO `virtual_user_info` VALUES (97, 'https://oss.tigshop.com/img/gallery/202512/1764920957qqSnVGw4JHk1yUH71E.jpeg', '当初的热忱开始消殆');
INSERT INTO `virtual_user_info` VALUES (98, 'https://oss.tigshop.com/img/gallery/202512/1764920968OvWOdBOMecIOHbZYhl.jpeg', '欠');
INSERT INTO `virtual_user_info` VALUES (99, 'https://oss.tigshop.com/img/gallery/202512/1764920980Ma9RrOs4SF2pyHt2rO.jpeg', '雪岭人未归恨甚晚');
INSERT INTO `virtual_user_info` VALUES (100, 'https://oss.tigshop.com/img/gallery/202512/1764920991Iwl3lNCuNue9H7LWP0.jpeg', '故事我不缺');

SET FOREIGN_KEY_CHECKS = 1;


-- =====================================================
-- 商品自动上下架表升级脚本
-- 支持多商户和多门店自动上下架功能
-- =====================================================

-- 创建商品自动上下架表（如果不存在）
DROP TABLE IF EXISTS `product_auto_status`;
CREATE TABLE `product_auto_status` (
                                       `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       `product_id` int(11) NOT NULL COMMENT '商品ID',
                                       `shop_id` int(11) NOT NULL DEFAULT '0' COMMENT '门店ID：0-平台商品，>0-门店商品',
                                       `operation_type` tinyint(1) NOT NULL COMMENT '操作类型：0-下架，1-上架',
                                       `scheduled_time` bigint(20) NOT NULL COMMENT '计划执行时间（时间戳）',
                                       `execute_time` bigint(20) DEFAULT NULL COMMENT '实际执行时间（时间戳）',
                                       `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '执行状态：0-待执行，1-执行成功，2-执行失败',
                                       `error_message` varchar(500) DEFAULT NULL COMMENT '失败原因',
                                       `retry_count` int(11) NOT NULL DEFAULT '0' COMMENT '重试次数',
                                       `create_time` bigint(20) NOT NULL COMMENT '创建时间',
                                       `update_time` bigint(20) NOT NULL COMMENT '更新时间',
                                       `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
                                       PRIMARY KEY (`id`),
                                       KEY `idx_product_id` (`product_id`),
                                       KEY `idx_shop_id` (`shop_id`),
                                       KEY `idx_scheduled_time` (`scheduled_time`),
                                       KEY `idx_status` (`status`),
                                       KEY `idx_operation_type` (`operation_type`),
                                       KEY `idx_product_shop` (`product_id`, `shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品自动上下架表';

-- 如果表已存在且没有 shop_id 字段，则添加该字段
-- ALTER TABLE `product_auto_status` ADD COLUMN `shop_id` int(11) NOT NULL DEFAULT '0' COMMENT '门店ID：0-平台商品，>0-门店商品' AFTER `product_id`;
-- ALTER TABLE `product_auto_status` ADD INDEX `idx_shop_id` (`shop_id`);
-- ALTER TABLE `product_auto_status` ADD INDEX `idx_product_shop` (`product_id`, `shop_id`);




ALTER TABLE `short_url`
    MODIFY COLUMN `short_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '短码，比如 aB12Xz' AFTER `short_url_id`;


ALTER TABLE `short_url`
    ADD COLUMN `type` tinyint NULL COMMENT 'Null和1 是h5       2是pc' AFTER `long_url`;


INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
VALUES ('searchSelfStore', '1', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- Stripe货币类型（默认usd）
-- INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
-- VALUES ('stripeCurrency', 'usd', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

ALTER TABLE `salesman_order`
    MODIFY COLUMN `status` tinyint NULL DEFAULT 0 COMMENT '结算状态0待结算1已结算2已失效' AFTER `amount`;

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30001007, 'quotationManage', '询价管理', 2, 53, 1, '[]', '', 'iconfont-tig icon-xunjiaguanli', 0, 'shop');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30001008, 'enquiryManage', '商品询价', 30001007, 50, 1, '[{"authName":"编辑","authSn":"priceInquiryModifyManage"}]', 'product/enquiry/list/', '', 0, 'shop');

INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30001009, 'quotationManage', '询价管理', 2, 53, 1, '[]', '', 'iconfont-tig icon-xunjiaguanli', 0, 'store');
INSERT INTO `authority` (`authority_id`, `authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`) VALUES (30001010, 'enquiryManage', '商品询价', 30001009, 50, 1, '[{"authName":"编辑","authSn":"priceInquiryModifyManage"}]', 'product/enquiry/list/', '', 0, 'store');

-- 修改商品名称字段 长度增加到200
ALTER TABLE `product`
    MODIFY COLUMN `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '商品的名称 ' AFTER `product_id`;