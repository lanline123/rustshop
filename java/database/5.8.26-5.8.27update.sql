INSERT INTO `config`(`biz_code`, `biz_val`, `create_time`, `create_by_id`, `create_by_name`, `update_time`, `update_by_id`, `update_by_name`, `is_del`) VALUES ( 'useCredit', '1', 1778229115, 1, 'admin', 1778229115, 1, 'admin', 0);

-- ============================================================================
-- 临时 DDL 草案：商米云小票打印对接 v1
-- 本文件为 TASK-W1-02 的临时产出，wave 8 (TASK-W8-01) 时将通过 sql-distributor skill
-- 分发到 6 个基线文件（pure.sql / demo.sql / tigshop_o2o_*.sql / tigshop_overseas_*.sql）
-- 和对应的增量 update SQL 文件。本文件本身不会被生产环境直接执行。
-- ============================================================================

-- ============================================================================
-- 1. 打印任务日志表
-- ============================================================================
CREATE TABLE IF NOT EXISTS `print_log` (
                                           `log_id`           BIGINT        NOT NULL AUTO_INCREMENT                          COMMENT '打印日志ID',
                                           `shop_id`          INT           NOT NULL DEFAULT 0                                COMMENT '店铺id',
                                           `print_id`         INT           NOT NULL                                          COMMENT '打印机id',
                                           `platform`         TINYINT(1)    NOT NULL                                          COMMENT '平台 1=飞鹅云 2=商米云',
    `order_id`         INT           NOT NULL                                          COMMENT '订单id',
    `print_sn`         VARCHAR(255)  CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '打印机SN快照',
    `trigger_type`     TINYINT       NOT NULL DEFAULT 1                                COMMENT '触发方式 1=自动 2=手动补打',
    `status`           TINYINT       NOT NULL DEFAULT 0                                COMMENT '状态 0=待推送 1=推送成功 2=打印成功 3=推送失败 4=打印机离线',
    `error_code`       VARCHAR(64)   CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '平台错误码',
    `error_msg`        VARCHAR(512)  CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '错误消息',
    `task_no`          VARCHAR(128)  CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '平台任务号（商米回调匹配键）',
    `push_response`    TEXT          CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci                COMMENT '推送API原始响应（截断存储）',
    `callback_payload` TEXT          CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci                COMMENT '回调原始报文（商米云）',
    `retry_count`      INT           NOT NULL DEFAULT 0                                COMMENT 'MQ重试次数',
    `pushed_time`      INT                    DEFAULT 0                                COMMENT '推送时间（Unix时间戳，秒）',
    `printed_time`     INT                    DEFAULT 0                                COMMENT '打印成功时间（Unix时间戳，秒）',
    `add_time`         INT                    DEFAULT 0                                COMMENT '创建时间（Unix时间戳，秒）',
    `update_time`      INT                    DEFAULT 0                                COMMENT '更新时间（Unix时间戳，秒）',
    PRIMARY KEY (`log_id`) USING BTREE,
    KEY `idx_shop_add_time` (`shop_id`, `add_time`),
    KEY `idx_order`         (`order_id`),
    KEY `idx_task_no`       (`task_no`)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_general_ci
    ROW_FORMAT = DYNAMIC
    COMMENT ='打印任务日志表';

-- ============================================================================
-- 2. 现有 print 表 platform 字段注释修正（从 "1飞鹅云 " 改为 "1飞鹅云 2商米云"）
--    注意：本语句仅修改注释，不修改字段类型或默认值，生产环境安全可重复执行。
-- ============================================================================
ALTER TABLE `print`
    MODIFY COLUMN `platform` TINYINT(1) DEFAULT 1 COMMENT '第三方平台 1=飞鹅云 2=商米云';

ALTER TABLE `print_config`
    ADD COLUMN `paper_size` TINYINT NOT NULL DEFAULT 58
    COMMENT '纸张尺寸（毫米）：58=58mm，80=80mm。历史数据默认 58mm'
        AFTER `type`;

ALTER TABLE `print`
    ADD COLUMN `voice_enabled` TINYINT NOT NULL DEFAULT 1
    COMMENT '语音播报开关：1=开启，2=关闭'
        AFTER `auto_print`;

ALTER TABLE `print`
    ADD COLUMN `voice_content_type` TINYINT NOT NULL DEFAULT 0
    COMMENT '语音播报内容：0=订单号+金额，1=仅金额'
        AFTER `voice_enabled`;
