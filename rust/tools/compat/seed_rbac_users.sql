-- RBAC 联调账号（密码均为 admin123，与 demo admin 同 BCrypt 哈希）
-- 说明：admin_user.auth_list 为权限 JSON 数组；is_using=0 表示启用（Java 语义）。
DELETE FROM `admin_user` WHERE username IN ('rbac_op', 'rbac_op2');
INSERT INTO `admin_user`
 (username, admin_type, mobile, avatar, password, email, add_time, auth_list,
  user_id, suppliers_id, role_id, merchant_id, parent_id, shop_id, is_using,
  initial_password, is_deleted)
VALUES
 -- 仅 orderExport：可访问无权限要求的接口，访问 product/product/list 返回 403/code 1001
 ('rbac_op', 'admin', '13800000002', NULL,
  '$2b$10$KW/snmOoevmgubZFY9eOFeW3zsxQcJMnDmWEuDboVLm3.5VkG5BL6',
  'rbac_op@example.com', UNIX_TIMESTAMP(), '["orderExport"]',
  0, 0, 1, 0, 0, 0, 0, '', 0),
 -- 仅 productManage：可访问 product/product/list
 ('rbac_op2', 'admin', '13800000003', NULL,
  '$2b$10$KW/snmOoevmgubZFY9eOFeW3zsxQcJMnDmWEuDboVLm3.5VkG5BL6',
  'rbac_op2@example.com', UNIX_TIMESTAMP(), '["productManage"]',
  0, 0, 1, 0, 0, 0, 0, '', 0);
