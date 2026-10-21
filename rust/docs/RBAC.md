# RBAC 方法级权限（已实测）

> 对齐 Java `@PreAuthorize("@pms.hasPermission('xxx')")`（`PermissionHandler`），
> 权限表**由契约自动生成**，无需逐接口手写注解。

## 1. 实现方式

| 部分 | 位置 |
|---|---|
| 权限表生成 | `tools/compat/gen_admin_permissions.py` → `crates/api/admin/src/generated_permissions.rs`（864 个带权限端点） |
| 权限来源 | `admin_user.auth_list`（JSON 数组，兼容嵌套 JSON 字符串脏数据） |
| 校验语义 | 任意一个所需权限匹配即放行；`all` 为超级权限；支持 `*` 通配（Spring `PatternMatchUtils.simpleMatch`） |
| 缓存 | 进程内按 `admin_id` 缓存 30s（`PermissionService::invalidate` 可手动失效） |
| 中间件 | `crates/api/admin/src/permissions.rs`，挂在 `/adminapi` 受保护路由上，`require_auth` 之后执行 |
| 拒绝响应 | **HTTP 403 + `{"code":1001,"msg":"参数校验异常"}`**，与 Java `JwtAccessDeniedHandler` 一致 |

重新生成权限表（契约变更后）：

```bash
cd rust && python tools/compat/gen_admin_permissions.py
```

## 2. 联调账号

```bash
mysql -h 127.0.0.1 -u root -proot --default-character-set=utf8mb4 tigshop-java \
  -e "source rust/tools/compat/seed_rbac_users.sql"
```

| 账号 | auth_list | 预期 |
|---|---|---|
| `admin` | `["all"]` | 全部接口 200 |
| `rbac_op` | `["orderExport"]` | `GET /adminapi/order/order/list`（无权限要求）200；`GET /adminapi/product/product/list` **403/code 1001** |
| `rbac_op2` | `["productManage"]` | `GET /adminapi/product/product/list` 200 |

密码均为 `admin123`。

## 3. 实测结果

```
admin     : product/list 200, order/list 200
rbac_op   : product/list -> HTTP 403 {"code":1001,"msg":"参数校验异常"}
            order/list   -> HTTP 200
rbac_op2  : product/list -> HTTP 200
```

回归脚本：[`tools/compat/rbac_smoke.ps1`](../tools/compat/rbac_smoke.ps1)（Windows PowerShell）。

## 4. 注意

1. 嵌套路由中 `req.uri()` 会去掉 `/adminapi` 前缀，中间件使用 `OriginalUri` 取完整契约路径（已处理）。
2. 登录/公开接口（`login/signin`、`health` 等）不在权限表内 → 不校验。
3. 权限缓存 30s：修改 `auth_list` 后最多 30s 生效；管理员改自己权限后可调用 `PermissionService::invalidate`。
4. `all` 语义做了简化：Java 会按 `adminType` 再查 `authority` 表做 scope 校验；Rust 当前将 `all` 视为管理员类型内的全部权限（平台/店铺超管均适用），后续如需严格隔离可扩展。
