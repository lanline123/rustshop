ALTER TABLE `decorate` ADD COLUMN `locale_id` int NULL DEFAULT 0 COMMENT '关联语言默认为0' AFTER `update_time`, ADD COLUMN `parent_id` int NULL DEFAULT 0 COMMENT '父级id' AFTER `locale_id`;
DROP TABLE IF EXISTS `locales_lang`;
CREATE TABLE `locales_lang` (
                                `code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                `id` int NOT NULL AUTO_INCREMENT,
                                `name_en` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=130 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;


DELETE
FROM authority
WHERE authority_sn = 'mobileuserUserOverseasManage';
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`,
                         `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES ('mobileuserUserOverseasManage', '会员首页', 27, 50, 1, '[]', '', '', 0, 'admin');

DELETE
FROM authority
WHERE authority_sn = 'mobileUserManage';

INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`,
                         `route_link`, `authority_ico`, `is_system`, `admin_type`)
VALUES ('mobileUserManage', '会员首页', 27, 53, 1, '[]', 'decorate/mobile_decorate/user', '', 0, 'admin');

-- ----------------------------
-- Records of locales_lang
-- ----------------------------
BEGIN;
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('zh', '中文(简体)', 1, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('zh-Hant', '中文(繁体)', 2, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('zh-Hant-hk', '中文(香港繁体)', 3, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('zh-Hant-tw', '中文(台湾繁体)', 4, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('tn', '札那语', 5, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('vi', '越南语', 6, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('iu', '伊努克提图特语', 7, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('it', '意大利语', 8, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('id', '印尼语', 9, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('hi', '印地语', 10, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('en', '英语', 11, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ho', '希里莫图语', 12, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('he', '希伯来语', 13, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('es', '西班牙语', 14, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('el', '现代希腊语', 15, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('uk', '乌克兰语', 16, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ur', '乌尔都语', 17, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('tk', '土库曼语', 18, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('tr', '土耳其语', 19, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ti', '提格里尼亚语', 20, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ty', '塔希提语', 21, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('tl', '他加禄语', 22, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('to', '汤加语', 23, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('th', '泰语', 24, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ta', '泰米尔语', 25, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('te', '泰卢固语', 26, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('sl', '斯洛文尼亚语', 27, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('sk', '斯洛伐克语', 28, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ss', '史瓦帝语', 29, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('eo', '世界语', 30, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('sm', '萨摩亚语', 31, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('sg', '桑戈语', 32, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('st', '塞索托语', 33, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('sv', '瑞典语', 34, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ja', '日语', 35, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('tw', '契维语', 36, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('qu', '奇楚瓦语', 37, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('pt', '葡萄牙语', 38, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('pa', '旁遮普语', 39, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('no', '挪威语', 40, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('nb', '挪威布克莫尔语', 41, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('nr', '南恩德贝勒语', 42, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('my', '缅甸语', 43, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('bn', '孟加拉语', 44, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('mn', '蒙古语', 45, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('mh', '马绍尔语', 46, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('mk', '马其顿语', 47, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ml', '马拉亚拉姆语', 48, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('mr', '马拉提语', 49, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ms', '马来语', 50, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('lu', '卢巴卡丹加语', 51, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ro', '罗马尼亚语', 52, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('lt', '立陶宛语', 53, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('lv', '拉脱维亚语', 54, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('lo', '老挝语', 55, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('kj', '宽亚玛语', 56, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('hr', '克罗地亚语', 57, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('kn', '坎纳达语', 58, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ki', '基库尤语', 59, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('cs', '捷克语', 60, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ca', '加泰隆语', 61, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('nl', '荷兰语', 62, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ko', '韩语', 63, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ht', '海地克里奥尔语', 64, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('gu', '古吉拉特语', 65, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ka', '格鲁吉亚语', 66, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('kl', '格陵兰语', 67, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('km', '高棉语', 68, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('lg', '干达语', 69, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('kg', '刚果语', 70, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('fi', '芬兰语', 71, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('fj', '斐济语', 72, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('fr', '法语', 73, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ru', '俄语', 74, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ng', '恩敦加语', 75, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('de', '德语', 76, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('tt', '鞑靼语', 77, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('da', '丹麦语', 78, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ts', '聪加语', 79, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('cv', '楚瓦什语', 80, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('fa', '波斯语', 81, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('bs', '波斯尼亚语', 82, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('pl', '波兰语', 83, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('bi', '比斯拉玛语', 84, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('nd', '北恩德贝勒语', 85, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ba', '巴什基尔语', 86, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('bg', '保加利亚语', 87, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('az', '阿塞拜疆语', 88, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ar', '阿拉伯语', 89, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('af', '阿非利堪斯语', 90, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('sq', '阿尔巴尼亚语', 91, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ab', '阿布哈兹语', 92, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('os', '奥塞梯语', 93, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ee', '埃维语', 94, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('et', '爱沙尼亚语', 95, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ay', '艾马拉语', 96, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('lzh', '中文（文言文）', 97, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('am', '阿姆哈拉语', 98, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ckb', '中库尔德语', 99, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('cy', '威尔士语', 100, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('gl', '加利西亚语', 101, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ha', '豪萨语', 102, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('hy', '亚美尼亚语', 103, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ig', '伊博语', 104, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('kmr', '北库尔德语', 105, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ln', '林加拉语', 106, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('nso', '北索托语', 107, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ny', '齐切瓦语', 108, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('om', '奥洛莫语', 109, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('sn', '绍纳语', 110, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('so', '索马里语', 111, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('sr', '塞尔维亚语', 112, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('sw', '斯瓦希里语', 113, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('xh', '科萨语', 114, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('yo', '约鲁巴语', 115, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('zu', '祖鲁语', 116, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('bo', '藏语', 117, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('nan', '闽南语', 118, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('wuu', '吴语', 119, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('yue', '粤语', 120, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('cmn', '西南官话', 121, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('ug', '维吾尔语', 122, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('fuv', '尼日利亚富拉语', 123, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('hu', '匈牙利语', 124, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('kam', '坎巴语', 125, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('luo', '肯尼亚语', 126, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('rw', '基尼阿万达语', 127, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('umb', '卢欧语', 128, NULL);
INSERT INTO `locales_lang` (`code`, `name`, `id`, `name_en`) VALUES ('wo', '沃洛夫语', 129, NULL);
COMMIT;