-- =============================================================
-- v5.8.33 -> v5.8.34 增量脚本
-- 内容：素材中心(D1076) / 入驻申请审核人(D1089) / 微信支付线上分账(D1091)
--       / 微商模式(D1100) / 搜索页风格(D1103) / 搜索页装修菜单
-- 目标库：MySQL 8.x / 5.7+（utf8mb4）
-- =============================================================

-- =============================================================
-- D1076 素材中心：gallery_pic / gallery_video_info 字段补齐 + 素材管理菜单权限
-- =============================================================
ALTER TABLE `gallery_pic` ADD COLUMN `pic_width` INT DEFAULT NULL COMMENT '图片宽度' AFTER `hidden`;
ALTER TABLE `gallery_pic` ADD COLUMN `pic_height` INT DEFAULT NULL COMMENT '图片高度' AFTER `pic_width`;
ALTER TABLE `gallery_pic` ADD COLUMN `pic_size` BIGINT DEFAULT NULL COMMENT '图片大小（字节）' AFTER `pic_height`;
ALTER TABLE `gallery_pic` ADD COLUMN `file_type` VARCHAR(20) DEFAULT NULL COMMENT '文件格式（如png、jpg、webp）' AFTER `pic_size`;
ALTER TABLE `gallery_pic` ADD COLUMN `pic_sort` INT DEFAULT 0 COMMENT '手动排序值' AFTER `file_type`;

-- 外部图片/视频直链接入 - source 字段
ALTER TABLE `gallery_pic` ADD COLUMN `source` VARCHAR(20) NOT NULL DEFAULT 'upload' COMMENT '来源：upload=本地上传, external=外部直链' AFTER `pic_sort`;
ALTER TABLE `gallery_video_info` ADD COLUMN `source` VARCHAR(20) NOT NULL DEFAULT 'upload' COMMENT '来源：upload=本地上传, external=外部直链' AFTER `vendor_id`;
ALTER TABLE `gallery_video_info` ADD COLUMN `description` varchar(255) DEFAULT NULL COMMENT '视频简介' AFTER `video_cover`;
-- 新增权限（自增 authority_id，父级按 authority_sn 解析，WHERE NOT EXISTS 幂等可重复执行）
-- 平台端
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'materialManagement', '素材管理',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='content' AND `admin_type`='admin' LIMIT 1) a),
       6, 1, '[]', '', 'iconfont-tig icon-jingying', 0, 'admin'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='admin');

INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'materialCenter', '素材中心',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='admin' LIMIT 1) a),
       50, 1, '[{"authName":"相册修改","authSn":"galleryModifyManage"},{"authName":"相册图片修改","authSn":"galleryPicModifyManage"},{"authName":"相册管理","authSn":"galleryManage"},{"authName":"相册图片管理","authSn":"galleryPicManage"},{"authName":"视频管理","authSn":"galleryVideoManage"}]', '', '', 0, 'admin'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='materialCenter' AND `admin_type`='admin');

-- 店铺端
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'materialManagement', '素材管理',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='setting' AND `admin_type`='shop' LIMIT 1) a),
       6, 1, '[]', '', 'iconfont-tig icon-jingying', 0, 'shop'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='shop');

INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'materialCenter', '素材中心',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='shop' LIMIT 1) a),
       50, 1, '[{"authName":"相册修改","authSn":"galleryModifyManage"},{"authName":"相册图片修改","authSn":"galleryPicModifyManage"},{"authName":"相册管理","authSn":"galleryManage"},{"authName":"相册图片管理","authSn":"galleryPicManage"},{"authName":"视频管理","authSn":"galleryVideoManage"}]', '', '', 0, 'shop'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='materialCenter' AND `admin_type`='shop');

-- 门店端
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'materialManagement', '素材管理',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='setting' AND `admin_type`='store' LIMIT 1) a),
       6, 1, '[]', '', 'iconfont-tig icon-jingying', 0, 'store'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='store');

INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'materialCenter', '素材中心',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='store' LIMIT 1) a),
       50, 1, '[{"authName":"相册修改","authSn":"galleryModifyManage"},{"authName":"相册图片修改","authSn":"galleryPicModifyManage"},{"authName":"相册管理","authSn":"galleryManage"},{"authName":"相册图片管理","authSn":"galleryPicManage"},{"authName":"视频管理","authSn":"galleryVideoManage"}]', '', '', 0, 'store'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='materialCenter' AND `admin_type`='store');

-- 供应商端
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'materialManagement', '素材管理',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='setting' AND `admin_type`='vendor' LIMIT 1) a),
       6, 1, '[]', '', 'iconfont-tig icon-jingying', 0, 'vendor'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='vendor');

INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'materialCenter', '素材中心',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='materialManagement' AND `admin_type`='vendor' LIMIT 1) a),
       50, 1, '[{"authName":"相册修改","authSn":"galleryModifyManage"},{"authName":"相册图片修改","authSn":"galleryPicModifyManage"},{"authName":"相册管理","authSn":"galleryManage"},{"authName":"相册图片管理","authSn":"galleryPicManage"},{"authName":"视频管理","authSn":"galleryVideoManage"}]', '', '', 0, 'vendor'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='materialCenter' AND `admin_type`='vendor');

-- =============================================================
-- D1089 入驻申请审核信息：merchant_apply 新增 audit_admin_id 字段
-- =============================================================
ALTER TABLE `merchant_apply`
  ADD COLUMN `audit_admin_id` int(11) NULL DEFAULT 0
  COMMENT '审核管理员ID(admin_user.admin_id)'
  AFTER `audit_time`;

-- =============================================================
-- D1091 微信支付线上分账 - 数据表与菜单
-- =============================================================

-- 1. 分账单
CREATE TABLE IF NOT EXISTS `profit_sharing_order` (
                                                      `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '分账单ID',
                                                      `out_order_no`     VARCHAR(64)   NOT NULL COMMENT '商户分账单号(幂等键)',
    `order_id`         BIGINT        NOT NULL COMMENT '订单ID',
    `order_sn`         VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '订单编号',
    `transaction_id`   VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '微信交易号',
    `pay_channel`      VARCHAR(32)   NOT NULL DEFAULT 'WECHAT' COMMENT '支付渠道',
    `shop_id`          BIGINT        NOT NULL COMMENT '店铺ID',
    `vendor_id`        BIGINT        NOT NULL DEFAULT 0 COMMENT '供应商ID(0表示店铺分账单)',
    `settlement_type`  TINYINT       NOT NULL DEFAULT 1 COMMENT '结算主体类型:1店铺 2供应商',
    `sub_mch_id`       VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '子商户号',
    `settlement_id`    BIGINT        NOT NULL DEFAULT 0 COMMENT '关联结算单ID(statement_output)',
    `operator_id`      BIGINT        NOT NULL DEFAULT 0 COMMENT '手动分账操作人',
    `total_amount`     DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额(元)',
    `split_amount`     DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '分账金额=服务费+分销佣金(元)',
    `service_fee`      DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '服务费(元)',
    `commission_fee`   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '分销佣金(元)',
    `service_fee_rate` DECIMAL(5,2)  DEFAULT NULL COMMENT '服务费率(%)',
    `status`           TINYINT       NOT NULL DEFAULT 0 COMMENT '状态:0待分账 1分账中 2成功 3失败',
    `channel_order_id` VARCHAR(128)  NOT NULL DEFAULT '' COMMENT '渠道分账单号',
    `finish_status`    TINYINT       NOT NULL DEFAULT 0 COMMENT '完结状态:0未完结 1已完结',
    `finish_time`      DATETIME      DEFAULT NULL COMMENT '完结时间',
    `trigger_type`     TINYINT       NOT NULL DEFAULT 1 COMMENT '触发方式:1手动 2自动',
    `result_desc`      VARCHAR(512)  NOT NULL DEFAULT '' COMMENT '结果描述',
    `retry_count`      INT           NOT NULL DEFAULT 0 COMMENT '重试次数',
    `is_del`           INT           NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 非0已删',
    `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_out_order_no` (`out_order_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_shop_id_status` (`shop_id`, `status`),
    KEY `idx_vendor_id_status` (`vendor_id`, `status`),
    KEY `idx_settlement_type` (`settlement_type`),
    KEY `idx_transaction_id` (`transaction_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='微信分账单';

-- 2. 分账回退单
CREATE TABLE IF NOT EXISTS `profit_sharing_return_order` (
                                                             `id`                      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '分账回退单ID',
                                                             `profit_sharing_order_id` BIGINT        NOT NULL COMMENT '关联分账单ID',
                                                             `out_return_no`           VARCHAR(64)   NOT NULL COMMENT '商户回退单号',
    `out_order_no`            VARCHAR(64)   NOT NULL COMMENT '原分账单号',
    `order_id`                BIGINT        NOT NULL COMMENT '订单ID',
    `refund_apply_id`         BIGINT        NOT NULL DEFAULT 0 COMMENT '退款申请ID',
    `sub_mch_id`              VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '子商户号',
    `aftersale_id`            BIGINT        NOT NULL DEFAULT 0 COMMENT '售后单ID(兼容)',
    `return_amount`           DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '回退金额(元)',
    `pay_channel`             VARCHAR(32)   NOT NULL DEFAULT 'WECHAT' COMMENT '支付渠道',
    `status`                  TINYINT       NOT NULL DEFAULT 0 COMMENT '状态:0待处理 1处理中 2成功 3失败',
    `channel_return_id`       VARCHAR(128)  NOT NULL DEFAULT '' COMMENT '渠道回退单号',
    `transaction_id`          VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '微信交易号',
    `result_desc`             VARCHAR(512)  NOT NULL DEFAULT '' COMMENT '结果描述',
    `retry_count`             INT           NOT NULL DEFAULT 0 COMMENT '重试次数',
    `is_del`                  INT           NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 非0已删',
    `create_time`             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time`             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_out_return_no` (`out_return_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_profit_sharing_order_id` (`profit_sharing_order_id`),
    KEY `idx_refund_apply_id` (`refund_apply_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='微信分账回退单';

-- 3. 二级商户映射
CREATE TABLE IF NOT EXISTS `sub_merchant` (
                                              `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '子商户映射ID',
                                              `shop_id`        BIGINT       NOT NULL COMMENT '店铺ID',
                                              `pay_channel`    VARCHAR(32)  NOT NULL DEFAULT 'WECHAT' COMMENT '支付渠道',
    `sub_mch_id`     VARCHAR(64)  NOT NULL COMMENT '子商户号',
    `sub_appid`      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '子商户AppID',
    `merchant_name`  VARCHAR(128) NOT NULL DEFAULT '' COMMENT '商户名称',
    `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0待激活 1已激活 2已冻结',
    `application_id` BIGINT       NOT NULL DEFAULT 0 COMMENT '关联进件申请ID',
    `is_del`         INT          NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 非0已删',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_shop_channel` (`shop_id`, `pay_channel`),
    KEY `idx_sub_mch_id` (`sub_mch_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='二级商户映射';

-- 4. 商户进件申请
CREATE TABLE IF NOT EXISTS `merchant_application` (
                                                      `id`                 BIGINT        NOT NULL AUTO_INCREMENT COMMENT '进件申请ID',
                                                      `shop_id`            BIGINT        NOT NULL COMMENT '店铺ID',
                                                      `pay_channel`        VARCHAR(32)   NOT NULL DEFAULT 'WECHAT' COMMENT '支付渠道',
    `business_code`      VARCHAR(128)  NOT NULL COMMENT '业务申请编号(幂等键)',
    `applyment_id`       VARCHAR(64)   DEFAULT NULL COMMENT '微信申请单号',
    `organization_type`  VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '组织类型',
    `merchant_name`      VARCHAR(256)  NOT NULL DEFAULT '' COMMENT '商户名称',
    `contact_name`       VARCHAR(128)  NOT NULL DEFAULT '' COMMENT '联系人姓名',
    `contact_phone`      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '联系人电话',
    `id_card_number_enc` TEXT          DEFAULT NULL COMMENT '身份证号(加密)',
    `bank_account_enc`   TEXT          DEFAULT NULL COMMENT '银行账号(加密)',
    `status`             VARCHAR(32)   NOT NULL DEFAULT 'DRAFT' COMMENT '状态:DRAFT/SUBMITTED/CHECKING/AUDITING/ACCOUNT_NEED_VERIFY/NEED_SIGN/FINISH/REJECTED',
    `sub_mch_id`         VARCHAR(64)   DEFAULT NULL COMMENT '成功后返回的子商户号',
    `sign_url`           VARCHAR(512)  DEFAULT NULL COMMENT '签约链接',
    `reject_reason`      VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '驳回原因',
    `request_snapshot`   TEXT          DEFAULT NULL COMMENT '提交请求快照(脱敏JSON)',
    `is_del`             INT           NOT NULL DEFAULT 0 COMMENT '逻辑删除:0未删 非0已删',
    `create_time`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_business_code` (`business_code`),
    KEY `idx_shop_id` (`shop_id`),
    KEY `idx_status` (`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商户进件申请';

-- 5. shop_withdraw 新增线上提现相关字段
ALTER TABLE `shop_withdraw`
    ADD COLUMN `withdraw_type` TINYINT NOT NULL DEFAULT 1
    COMMENT '提现方式:1线下 2线上(微信API)' AFTER `withdraw_sn`,
    ADD COLUMN `channel_withdraw_id` VARCHAR(128) NOT NULL DEFAULT ''
        COMMENT '微信提现单号' AFTER `withdraw_type`,
    ADD COLUMN `channel_status` VARCHAR(32) NOT NULL DEFAULT ''
        COMMENT '微信侧状态' AFTER `channel_withdraw_id`;

-- =============================================================
-- 6. 配置项（线上分账 + 线上提现）
-- =============================================================
INSERT INTO `config` (`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES
                                                                                                                                                             ('profitSharingCoolingDays', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
                                                                                                                                                             ('autoProfitSharingEnabled', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
                                                                                                                                                             ('autoProfitSharingDays', '7', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
                                                                                                                                                             ('onlineWithdrawEnabled', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
                                                                                                                                                             ('onlineMinAmount', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
                                                                                                                                                             ('onlineMaxAmount', '0', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
                                                                                                                                                             ('onlineWithdrawalFrequencyUnit', '1', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
                                                                                                                                                             ('onlineWithdrawalFrequencyCount', '1', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
                                                                                                                                                             ('onlineWithdrawalReviewMethod', '1', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0),
                                                                                                                                                             ('onlineWithdrawalDescription', '', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0);

-- =============================================================
-- 7. 菜单权限（挂在「分账管理」profitsharingManage 下）
-- =============================================================
-- 进件管理
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'merchantApplicationManage', '进件管理',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='profitsharingManage' AND `admin_type`='admin' LIMIT 1) a),
       60, 1,
       '[{"auth_name":"提交进件","auth_sn":"merchantApplicationSubmit"},{"auth_name":"刷新状态","auth_sn":"merchantApplicationRefresh"}]',
       'finance/merchantApplication/list/', '', 0, 'admin'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='merchantApplicationManage' AND `admin_type`='admin');

-- 子商户管理
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'subMerchantManage', '子商户管理',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='profitsharingManage' AND `admin_type`='admin' LIMIT 1) a),
       61, 1,
       '[{"auth_name":"绑定","auth_sn":"subMerchantBind"},{"auth_name":"解绑","auth_sn":"subMerchantUnbind"}]',
       'finance/subMerchant/list/', '', 0, 'admin'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='subMerchantManage' AND `admin_type`='admin');

-- 分账结算
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'profitSharingSettlementManage', '分账结算',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='profitsharingManage' AND `admin_type`='admin' LIMIT 1) a),
       62, 1,
       '[{"auth_name":"发起分账","auth_sn":"profitSharingSettlementSubmit"},{"auth_name":"手动完结","auth_sn":"profitSharingSettlementFinish"}]',
       'finance/profitSharing/list/', '', 0, 'admin'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='profitSharingSettlementManage' AND `admin_type`='admin');


-- =============================================================
-- 进件管理重构：店铺/门店/供应商端申请 + 平台端审核
-- merchant_application 表增量字段
-- =============================================================

-- 申请主体类型：SHOP(店铺/门店) / VENDOR(供应商)
ALTER TABLE `merchant_application`
    ADD COLUMN `applicant_type` VARCHAR(16) NOT NULL DEFAULT 'SHOP'
    COMMENT '申请主体类型:SHOP店铺/门店 VENDOR供应商' AFTER `shop_id`,
    ADD COLUMN `applicant_id` BIGINT NOT NULL DEFAULT 0
        COMMENT '申请主体ID:SHOP=shopId VENDOR=vendorId' AFTER `applicant_type`,
    ADD COLUMN `applicant_name` VARCHAR(256) NOT NULL DEFAULT ''
        COMMENT '申请主体名称(店铺名/供应商名,冗余展示)' AFTER `applicant_id`,
    ADD COLUMN `audit_admin_id` INT NOT NULL DEFAULT 0
        COMMENT '审核人ID(平台管理员)' AFTER `reject_reason`,
    ADD COLUMN `audit_time` DATETIME DEFAULT NULL
        COMMENT '审核时间' AFTER `audit_admin_id`;

-- 历史数据回填：原有记录均为店铺主体，applicant_id 用 shop_id
UPDATE `merchant_application`
SET `applicant_type` = 'SHOP', `applicant_id` = `shop_id`
WHERE `applicant_id` = 0;

-- 主体索引，便于按当前登录主体查询最新申请
ALTER TABLE `merchant_application`
    ADD KEY `idx_applicant` (`applicant_type`, `applicant_id`);

-- status 字段注释补充新增的 PENDING_REVIEW(平台待审核)
ALTER TABLE `merchant_application`
    MODIFY COLUMN `status` VARCHAR(32) NOT NULL DEFAULT 'DRAFT'
    COMMENT '状态:DRAFT草稿/PENDING_REVIEW平台待审核/SUBMITTED已提交/CHECKING资料校验中/AUDITING审核中/ACCOUNT_NEED_VERIFY待账户验证/NEED_SIGN待签约/FINISH完成/REJECTED已驳回';

-- =============================================================
-- 权限种子调整
-- =============================================================

-- 1) 平台端「进件管理」菜单：页内含「进件管理」「进件审核」两个 tab，
--    操作权限合并为 提交进件 / 刷新状态 / 审核通过 / 审核拒绝
UPDATE `authority`
SET `child_auth` = '[{"auth_name":"提交进件","auth_sn":"merchantApplicationSubmit"},{"auth_name":"刷新状态","auth_sn":"merchantApplicationRefresh"},{"auth_name":"审核通过","auth_sn":"merchantApplicationApprove"},{"auth_name":"审核拒绝","auth_sn":"merchantApplicationReject"}]'
WHERE `authority_sn` = 'merchantApplicationManage' AND `admin_type` = 'admin';

-- 注：进件审核为「进件管理」页内 tab，复用 merchantApplicationManage 菜单，不再单独建菜单

-- 2) 店铺/门店端「进件申请」菜单（挂在财务中心 capitalfund 下）
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'merchantApplyManage', '进件申请',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='capitalfund' AND `admin_type`='shop' LIMIT 1) a),
       80, 1, '[]',
       'capitalfund/merchant-application/apply', '', 0, 'shop'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='merchantApplyManage' AND `admin_type`='shop');

-- 3) 供应商端「进件申请」菜单（挂在财务中心 capitalfund 下）
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'merchantApplyManage', '进件申请',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='capitalfund' AND `admin_type`='vendor' LIMIT 1) a),
       80, 1, '[]',
       'capitalfund/merchant-application/apply', '', 0, 'vendor'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='merchantApplyManage' AND `admin_type`='vendor');

-- 4) 门店端「进件申请」菜单（o2o 业态，挂在财务中心 capitalfund/store 下）
--    门店本质是 shop_type=2 的店铺，登录时 getAdminType() 返回 store，按 admin_type='store' 渲染菜单。
--    EXISTS 守卫：仅当存在门店版 capitalfund 时才插入，避免 b2c 库产生孤儿菜单行。
INSERT INTO `authority` (`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'merchantApplyManage', '进件申请',
       (SELECT a.authority_id FROM (SELECT authority_id FROM `authority` WHERE `authority_sn`='capitalfund' AND `admin_type`='store' LIMIT 1) a),
       80, 1, '[]',
       'capitalfund/merchant-application/apply', '', 0, 'store'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='capitalfund' AND `admin_type`='store')
  AND NOT EXISTS (SELECT 1 FROM `authority` WHERE `authority_sn`='merchantApplyManage' AND `admin_type`='store');

-- =============================================================
-- 权限传播：存量账号/角色为显式白名单（非 ["all"]）时不会自动获得新菜单，
-- 需将新 sn 追加到「已拥有父菜单」的存量权限列表中（幂等，可重复执行）
-- =============================================================

-- 店铺账号：已有 capitalfund 的，补 merchantApplyManage
UPDATE `admin_user_shop`
SET `auth_list` = JSON_ARRAY_APPEND(`auth_list`, '$', 'merchantApplyManage')
WHERE JSON_VALID(`auth_list`)
  AND JSON_CONTAINS(`auth_list`, '"capitalfund"')
  AND NOT JSON_CONTAINS(`auth_list`, '"merchantApplyManage"');

-- 店铺角色：已有 capitalfund 的，补 merchantApplyManage（all 角色天然全量，跳过）
UPDATE `admin_role`
SET `authority_list` = JSON_ARRAY_APPEND(`authority_list`, '$', 'merchantApplyManage')
WHERE `admin_type` = 'shop' AND JSON_VALID(`authority_list`)
  AND JSON_CONTAINS(`authority_list`, '"capitalfund"')
  AND NOT JSON_CONTAINS(`authority_list`, '"all"')
  AND NOT JSON_CONTAINS(`authority_list`, '"merchantApplyManage"');

-- 供应商账号：已有 capitalfund 的，补 merchantApplyManage
UPDATE `admin_user_vendor`
SET `auth_list` = JSON_ARRAY_APPEND(`auth_list`, '$', 'merchantApplyManage')
WHERE JSON_VALID(`auth_list`)
  AND JSON_CONTAINS(`auth_list`, '"capitalfund"')
  AND NOT JSON_CONTAINS(`auth_list`, '"merchantApplyManage"');

-- 供应商角色：已有 capitalfund 的，补 merchantApplyManage
UPDATE `admin_role`
SET `authority_list` = JSON_ARRAY_APPEND(`authority_list`, '$', 'merchantApplyManage')
WHERE `admin_type` = 'vendor' AND JSON_VALID(`authority_list`)
  AND JSON_CONTAINS(`authority_list`, '"capitalfund"')
  AND NOT JSON_CONTAINS(`authority_list`, '"all"')
  AND NOT JSON_CONTAINS(`authority_list`, '"merchantApplyManage"');

-- 门店角色：已有 capitalfund 的，补 merchantApplyManage（门店端角色 admin_type='store'，all 角色天然全量，跳过）
-- 注：门店主账号存在 admin_user_shop（shop_type=2），上方 admin_user_shop 回填已覆盖门店账号，无需单独补账号
UPDATE `admin_role`
SET `authority_list` = JSON_ARRAY_APPEND(`authority_list`, '$', 'merchantApplyManage')
WHERE `admin_type` = 'store' AND JSON_VALID(`authority_list`)
  AND JSON_CONTAINS(`authority_list`, '"capitalfund"')
  AND NOT JSON_CONTAINS(`authority_list`, '"all"')
  AND NOT JSON_CONTAINS(`authority_list`, '"merchantApplyManage"');

-- 平台账号角色：已含进件管理菜单的，补审核操作权限（通过/拒绝），审核 tab 复用同一菜单
UPDATE `admin_role`
SET `authority_list` = JSON_ARRAY_APPEND(JSON_ARRAY_APPEND(`authority_list`, '$', 'merchantApplicationApprove'), '$', 'merchantApplicationReject')
WHERE `admin_type` = 'admin' AND JSON_VALID(`authority_list`)
  AND JSON_CONTAINS(`authority_list`, '"merchantApplicationManage"')
  AND NOT JSON_CONTAINS(`authority_list`, '"all"')
  AND NOT JSON_CONTAINS(`authority_list`, '"merchantApplicationApprove"');

-- =============================================================
-- D1100 微商模式：salesman_customer 唯一索引
-- =============================================================
ALTER TABLE `salesman_customer` ADD UNIQUE KEY `uk_user_id` (`user_id`);

-- =============================================================
-- D1103 搜索页风格（PC端装修 - 搜索结果页新旧 UI 切换）
-- 1. 后台菜单：PC端装修 > 搜索页风格（放在「商品页风格」之后）
-- 2. 系统配置：searchStyleType（1-样式一/经典版，2-样式二/新版），默认 1
-- 幂等：通过 NOT EXISTS 判断，已存在则跳过。
-- =============================================================
INSERT INTO `authority`
(`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'pcSearchStyleManage', '搜索页风格', 62, 55, 1,
       '[{\"authName\":\"保存\",\"authSn\":\"pcSearchStyleSaveManage\"}]',
       'decorate/pc_decorate/search_style/', '', 0, 'admin'
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `authority`) AS t
    WHERE t.`authority_sn` = 'pcSearchStyleManage' AND t.`admin_type` = 'admin'
);

INSERT INTO `config`
(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`)
SELECT 'searchStyleType', '1', UNIX_TIMESTAMP(), 0, '', UNIX_TIMESTAMP(), 0, '', 0
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `config`) AS t
    WHERE t.`biz_code` = 'searchStyleType'
);

-- =============================================================
-- 搜索页装修（PC端装修 - 搜索页装修列表，decorateType=2 & pageType=6）
-- 后台菜单：PC端装修 > 搜索页装修（放在「搜索页风格」之后，sort_order=56）
-- child_auth 复用「首页装修」的装修权限点（发布/设为首页/编辑/模块编辑/存入草稿/新增），不新增权限
-- route_link 为前端菜单路由：默认取列表页并按 decorateType=2&pageType=6 过滤（如前端实际路由不同请调整）
-- 幂等：通过 NOT EXISTS 判断，已存在则跳过。
-- =============================================================
INSERT INTO `authority`
(`authority_sn`, `authority_name`, `parent_id`, `sort_order`, `is_show`, `child_auth`, `route_link`, `authority_ico`, `is_system`, `admin_type`)
SELECT 'pcSearchDecorateManage', '搜索页装修', 62, 56, 1,
       '[{\"authName\":\"发布\",\"authSn\":\"decoratePublishManage\"},{\"authName\":\"设为首页\",\"authSn\":\"decorateSetHomeManage\"},{\"authName\":\"编辑\",\"authSn\":\"decorateModifyManage\"},{\"authName\":\"模块编辑\",\"authSn\":\"decorateDiscreteModifyManage\"},{\"authName\":\"存入草稿\",\"authSn\":\"decorateSaveDraftManage\"},{\"authName\":\"新增\",\"authSn\":\"decorateCreateManage\"}]',
       '-', '', 0, 'admin'
WHERE NOT EXISTS (
    SELECT 1 FROM (SELECT * FROM `authority`) AS t
    WHERE t.`authority_sn` = 'pcSearchDecorateManage' AND t.`admin_type` = 'admin'
);
