-- 本地联调用测试会员（C 端）
-- 说明：demo.sql 的 user 表为空；这里插入一个可登录的测试会员。
-- 密码哈希复用 demo.sql 中 admin 账号的 BCrypt 哈希（对应明文 admin123），
-- 因此测试账号：用户名 testbuyer / 手机号 13800000001 / 密码 admin123。
-- 执行前请确认连接的是测试库（默认 tigshop-java）：
--   mysql -h 127.0.0.1 -u root -p --default-character-set=utf8mb4 tigshop-java \
--     -e "source rust/tools/compat/seed_test_member.sql"

DELETE FROM `user` WHERE username = 'testbuyer' OR mobile = '13800000001';

INSERT INTO `user`
    (username, avatar, mobile, mobile_validated, email, email_validated,
     nickname, password, balance, frozen_balance, commission_balance,
     points, growth_points, reg_time, last_ip, rank_id)
VALUES
    ('testbuyer', '', '13800000001', 1, 'testbuyer@example.com', 0,
     '测试买家',
     '$2b$10$KW/snmOoevmgubZFY9eOFeW3zsxQcJMnDmWEuDboVLm3.5VkG5BL6',
     1000.00, 0.00, 0.00,
     0, 0, UNIX_TIMESTAMP(), '127.0.0.1', 0);
