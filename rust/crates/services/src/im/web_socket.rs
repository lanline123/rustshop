//! 消息控制器（内部接口 / WebSocket 推送）域服务（im::web_socket）。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/im::web_socket.md`。
//!
//! ## ⚠ WebSocket 推送通道**未迁移**（重要）
//!
//! 原 Java `WebSocketController.sendMessage`（`java/tigshop-api/src/main/java/
//! com/tigshop/api/controller/im/WebSocketController.java:67`）依赖
//! `WebSocketServerHandler`（Spring `@Component` + `TextWebSocketHandler`），
//! 其内部是一张**进程内在线会话表**：
//!
//! ```java
//! Set<WebSocketSession> sessions = USER_SESSIONS.get(userId);   // ConcurrentHashMap
//! if (sessions != null && !sessions.isEmpty()) { ...sendMessage(TextMessage)... }
//! else { System.out.println("⚠️ 用户未在线: userId=" + userId); }
//! ```
//!
//! 本 Rust 仓库**没有任何 WebSocket 基础设施**（核查命令：
//! `grep -rln "WebSocket\|tungstenite\|ws://" crates --include=*.rs` 只命中
//! 本切片的文件；`Cargo.toml` 中亦无 `tokio-tungstenite` 依赖），
//! 既没有 `/api/socket` 握手端点、也没有 `USER_SESSIONS` 会话表。
//!
//! 因此本服务**刻意不伪造一条可用的推送链路**：
//! 1. 参数校验、返回体结构、成功文案 100% 对齐 Java；
//! 2. 「推送」这一步退化为 `tracing::warn!` 记录（等价于 Java 的
//!    「用户未在线」分支——Java 在离线时也**只打印日志、不报错**）；
//! 3. 由 [`WebSocketService::send_message`] 返回与 Java 完全相同的字符串。
//!
//! 换言之：**接口能通、响应体一致，但消息不会真的送达任何浏览器**。
//! 真正落地需要补 WebSocket 握手端点 + 会话注册表（或改用 Redis pub/sub
//! 跨进程广播），属于独立的基础设施切片，不在本片范围。
//!
//! ## 另一处 Java 侧门禁未迁移
//!
//! Java 注释声明该接口「仅供内部服务调用，需携带 `X-Internal-Api-Key` 请求头」，
//! 但该校验**不在控制器里**（控制器只有 `@RequestParam`），而是依赖
//! Spring Security 配置。契约把这条标为 `auth=optional`，Rust 侧按 optional
//! 落地（见 `routes/web_socket.rs` 的 `optional_auth`），**未**实现
//! `X-Internal-Api-Key` 校验；若线上依赖它做内网隔离，需在网关侧补齐。

use std::sync::Arc;

use tigshop_core::models::im::web_socket::validate_query;
use tigshop_core::models::im::web_socket::SocketSendQuery;
use tigshop_core::Result;
use tigshop_infrastructure::db::Repositories;

/// 消息控制器（内部接口）域服务。
pub struct WebSocketService {
    repos: Arc<Repositories>,
}

impl WebSocketService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储（当前实现不落库，保留以固定构造签名）。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::im::web_socket::WebSocketRepository {
        &self.repos.im.web_socket
    }

    /// 服务器端主动发消息给指定用户（对应 Java `WebSocketController.sendMessage`）。
    ///
    /// 行为对照：
    /// - 参数缺失 → 业务码 `1001`「接口参数缺失」（Java
    ///   `MissingServletRequestParameterException` 分支）；
    /// - 参数齐备 → 返回 `"已发送消息给用户：" + targetUserId`（Java `:69`），
    ///   与 Java 一样**不区分在线/离线**（Java 离线也只是打印日志后同样返回成功文案）。
    ///
    /// ⚠ 推送本身未迁移，见模块头「WebSocket 推送通道**未迁移**」。
    pub fn send_message(&self, q: &SocketSendQuery) -> Result<String> {
        let (target_user_id, message) = validate_query(q)?;
        // Java: USER_SESSIONS.get(userId) → 本构建无会话表，等价于「用户未在线」分支
        tracing::warn!(
            target_user_id = %target_user_id,
            message_len = message.chars().count(),
            "WebSocket 推送通道未迁移（无会话注册表），本次调用未真正送达任何客户端"
        );
        Ok(success_text(&target_user_id))
    }
}

/// Java `WebSocketServerHandler#sendMessageToUser` 里「用户不在线」的判定位置。
/// 本构建始终视为不在线——保留该纯函数以便单测锁定文案，将来接上会话表时
/// 只需让它返回真实的在线会话数。
#[must_use]
pub fn online_session_count(_target_user_id: &str) -> usize {
    0
}

/// 成功应答文案（对应 Java `"已发送消息给用户：" + targetUserId`，`:69`）。
#[must_use]
pub fn success_text(target_user_id: &str) -> String {
    format!("已发送消息给用户：{target_user_id}")
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn success_text_is_the_java_literal() {
        assert_eq!(
            success_text("false::123"),
            "已发送消息给用户：false::123"
        );
        assert_eq!(success_text("true::1"), "已发送消息给用户：true::1");
        // 空串也要原样拼进文案（Java 无 @NotBlank）
        assert_eq!(success_text(""), "已发送消息给用户：");
    }

    #[test]
    fn online_session_count_is_always_zero_until_ws_migrated() {
        // 锁定「推送未迁移」这一事实：任何用户都视为离线
        assert_eq!(online_session_count("false::1"), 0);
        assert_eq!(online_session_count(""), 0);
    }

    #[test]
    fn missing_params_surface_java_error_code_and_msg() {
        let q = SocketSendQuery {
            target_user_id: Some("true::1".into()),
            message: None,
        };
        let err = validate_query(&q).unwrap_err();
        match err {
            tigshop_core::Error::Biz { code, msg } => {
                assert_eq!(code, 1001);
                assert_eq!(msg, "接口参数缺失");
            }
            other => panic!("期望 Biz，得到 {other:?}"),
        }
    }
}