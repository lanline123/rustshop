//! 消息控制器（内部接口）—— 商城前台路由（slice-141）。
//!
//! 原 Java：`WebSocketController`
//! （`java/tigshop-api/src/main/java/com/tigshop/api/controller/im/WebSocketController.java`，
//! `@RequestMapping("/api/socket")`）。
//!
//! ## 原 Java 路径对照表（本模块挂载于 `/api` 之下）
//!
//! | 本模块路由 | 原 Java 完整路径 | 原 Java 方法 | 契约 auth |
//! |-----------|------------------|----------------|-----------|
//! | `GET /socket/send` | `GET /api/socket/send` | `WebSocketController.sendMessage` | optional |
//!
//! > **路径说明**：`@RequestMapping("/api/socket")` + `@GetMapping("/send")`
//! > 拼出 `/api/socket/send`；挂在 `/api` 之下，故 `routes()` 内只写 `/socket/send`。
//!
//! > **⚠ 真实推送未迁移（务必知悉）**：Java 端点调用
//! > `WebSocketServerHandler#sendMessageToUser`，靠进程内 `USER_SESSIONS`
//! > 会话表把消息写进 WebSocket 长连接。**本 Rust 仓库没有任何 WebSocket
//! > 基础设施**（无握手端点、无 `tungstenite` 依赖、无会话表），本 handler
//! > 因此只做「参数校验 + 按 Java 返回体应答 + `tracing::warn!` 记录未送达」，
//! > **消息不会真正送达任何客户端**。详见
//! > [`tigshop_services::slice141`] 模块头「WebSocket 推送通道未迁移」。
//! > 要恢复真实推送，需要先落一个独立的 WebSocket 基础设施切片。
//!
//! > **`X-Internal-Api-Key` 未校验**：Java 注释声明该接口需内部密钥
//! > （校验在 Spring Security 配置里，不在控制器内）；契约标记为
//! > `auth=optional`，故本文件按 optional 落地，未实现该密钥校验。
//!
//! > **auth=optional 的落地口径**：与 slice013 / slice015 / slice021 / slice090
//! > 一致，本文件自带 [`optional_auth`]（带合法令牌写入上下文；不带 → 游客；
//! > 令牌非法 → 401）。Java 的 `sendMessage` 不读登录态，故鉴权不影响业务分支。

use axum::{
    extract::{Query, Request, State},
    http::header,
    middleware::{self, Next},
    response::Response,
    routing::get,
    Router,
};
use tigshop_core::models::slice141::SocketSendQuery;
use tigshop_core::Error;
use tigshop_security::web::{ok, ApiResult, AppError, AuthUser};
use tigshop_security::JwtService;

use super::super::PublicState;

/// 消息控制器（内部接口）路由（挂载于 `/api` 下）。
pub fn routes(state: &PublicState) -> Router<PublicState> {
    Router::new()
        .route("/socket/send", get(socket_send))
        .layer(middleware::from_fn_with_state(
            state.jwt.clone(),
            optional_auth,
        ))
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    [(
        "GET",
        "/api/socket/send",
        "WebSocketController.sendMessage",
    )]
    .iter()
    .map(|(method, path, java_ref)| tigshop_openapi::RouteDoc::new(method, path, java_ref))
    .collect()
}

/// 可选登录态中间件（与 slice013 / slice015 / slice021 / slice090 同一实现）。
async fn optional_auth(
    State(jwt): State<JwtService>,
    mut req: Request,
    next: Next,
) -> Result<Response, AppError> {
    let token = req
        .headers()
        .get(header::AUTHORIZATION)
        .and_then(|v| v.to_str().ok());
    if token.is_some() {
        let claims = jwt.from_authorization(token)?;
        let id = claims
            .sub
            .parse::<i32>()
            .map_err(|_| AppError(Error::Unauthorized("令牌主体无效".into())))?;
        req.extensions_mut().insert(AuthUser {
            id,
            username: claims.usr,
            role_id: claims.role,
            admin_type: claims.typ,
        });
    }
    Ok(next.run(req).await)
}

/// `GET /api/socket/send?targetUserId=&message=`：服务端向指定用户推送消息。
///
/// 对应 Java `WebSocketController.sendMessage(@RequestParam String targetUserId,
/// @RequestParam String message)`（`WebSocketController.java:65-70`）：
/// 调用 handler 推送后返回 `"已发送消息给用户：" + targetUserId`（纯 `String`，
/// 经统一信封包装在 `data` 字段）。
///
/// 参数缺失 → 业务码 `1001`「接口参数缺失」（与 Java
/// `MissingServletRequestParameterException` 的应答口径一致）。
///
/// ⚠ 推送动作未迁移，见模块头。
async fn socket_send(
    State(state): State<PublicState>,
    Query(q): Query<SocketSendQuery>,
) -> ApiResult<String> {
    ok(state.slice141.send_message(&q)?)
}
