# IM WebSocket 联调记录（已实测）

> 对齐 Java `tigshop-api` 的 `/ws`（`WebSocketConfig` + `WebSocketServerHandler`），
> 前端管理端（客服工作台）、PC 商城、uniapp 共用同一握手协议。

## 1. 连接协议

```
ws://<host>/ws?token=<JWT accessToken>&platform=<pc|admin|...>
```

- 管理端 8080、商城端 8181 **都**提供 `/ws`（nginx 只需代理一次到任一端口；建议 `/ws` → 8181）。
- token 来自登录响应 `accessToken`（JWT），必须是 access token；无效/过期/缺少会收到一条文本提示后断开。
- 用户键与 Java 完全一致：`{isAdmin}::{userId}`，如 `true::1`（管理员）、`false::1`（会员）；同一用户多端连接都会收到推送。
- 连接成功服务端先发：`连接成功！你的 userId 是：{isAdmin}::{userId}，设备：{platform}`。
- 客户端每 15s 发送 `{"type":"heartBeat","data":{}}`，服务端**原样回显**（与 Java 一致）。
- 服务端主动推送：`GET /api/socket/send?targetUserId=false::1&message=...`（Java `WebSocketController.sendMessage`），
  在线时真实写入长连接，离线时仍返回 Java 相同成功文案（只记日志）。

## 2. 实测结果

```
admin  ws 8080: welcome "连接成功！你的 userId 是：true::1，设备：admin"
                heartbeat echo {"type":"heartBeat","data":{}}  ✅
member ws 8181: welcome "连接成功！你的 userId 是：false::1，设备：pc"
                heartbeat echo ✅
invalid token : "token 无效或已过期"  ✅
push          : 在线连接收到 "hello-from-server"；离线用户返回成功文案 ✅
```

回归脚本：[`tools/compat/ws_smoke.ps1`](../tools/compat/ws_smoke.ps1)（Windows PowerShell 5.1 可用）。

## 3. 实现位置

| 部分 | 位置 |
|---|---|
| 会话注册表（`USER_SESSIONS`） | `crates/security/src/ws_registry.rs` |
| 握手/连接处理、回显、心跳 | `crates/security/src/ws.rs` |
| 路由 `/ws` | `crates/api/public/src/lib.rs`、`crates/api/admin/src/lib.rs` |
| 服务端推送 API | `crates/services/src/im/web_socket.rs` → `ws_registry::send_to_user` |

## 4. 部署注意

nginx 增加：

```nginx
location /ws {
    proxy_pass http://127.0.0.1:8181;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_read_timeout 3600s;
}
```

前端可通过 `VITE_SOCKET_URL` 覆盖地址（与 Java 行为一致）。

## 5. 已知限制

- 会话表为进程内状态：单实例可用；多实例需换 Redis pub/sub（`ws_registry` 接口不变）。
- 客服消息的完整业务链路（会话/消息 REST 接口）沿用现有 IM 域实现；IM 消息收发已接 WS 推送
  （`send_as_user` → 管理员、`send_as_admin` → 会员，实测双向；`tools/compat/im_push_smoke.ps1`）。
  已读回执推送与 RabbitMQ 自动回复仍未迁移。
