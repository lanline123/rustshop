//! IM 消息（ImMessageController） —— 根级路由（slice-140，admin 端）。
//!
//! **路径约定**：原 Java 该控制器 `@RequestMapping("/im/conversation/message")`
//! **不含** `/adminapi` 前缀，真实 URL 直接挂在应用根路径
//! （见 `crate::root` 模块文档）。因此本文件路由串写**完整契约路径**
//! （以 `/im` 开头），`implemented_routes()` 的 `RouteDoc.path` 同样写完整路径。
//!
//! | 本模块路由 | 原 Java | 登录 |
//! |-----------|--------|------|
//! | `GET  /im/conversation/message/list` | `ImMessageController.list` | 是 |
//! | `GET  /im/conversation/message/receivedList` | `ImMessageController.receivedList` | 是 |
//! | `POST /im/conversation/message/send` | `ImMessageController.send` | 是 |
//! | `POST /im/conversation/message/uploadVideo` | `ImMessageController.uploadVideo` | 是 |
//! | `POST /im/conversation/message/setRead` | `ImMessageController.setRead` | 是 |
//!
//! > **认证**：由 `crate::router()` 在根级 `merge_protected` 通道上统一附加
//! > `require_auth`（`/im/**` 不在 Java `URL_WHITELIST` 内），本文件不再自加中间件。
//! >
//! > **身份口径**（后台 = `getClientTypeByToken() == true`）：
//! > - `list` / `receivedList` / `send` 用 `SecurityUtils.getCurrentAdminId()`
//! >   ——Rust 侧即 JWT `sub`（[`AuthUser`] 的 `id`）；
//! > - `shopId` 用 `HeaderUtils.getShopId()`：`x-admin-type=admin` → `0`、
//! >   `shop`/`pos` → `x-shop-id`、其它 → `null`（与 slice-020 同口径，
//! >   `x-admin-type` 缺失时回退 JWT 的 `admin_type`）。
//! >
//! > **未迁移**：`uploadVideo` 依赖对象存储 + 视频抽帧，返回明确业务错误；
//! > `send` / `setRead` 的 WebSocket 推送与 `send` 的 RabbitMQ 自动回复均跳过
//! > （详见服务层模块头）。
//! >
//! > **kefuHistoryList 权限**：Java 后台 `list` 命中历史权限时放开 `servantId`
//! > 过滤；Rust 未迁移权限位，统一按最小权限（仅当前客服），见服务层说明。

use axum::{
    extract::{Multipart, Query, State},
    http::HeaderMap,
    routing::{get, post},
    Extension, Json, Router,
};
use tigshop_core::models::slice140::{
    ImMessageListDTO, ImMessageListVO, ImMessageReceivedListDTO, ImMessageSendDTO,
    ImMessageSendVO, ImSetReadDTO, UploadVideoInfoVO,
};
use tigshop_core::types::PageResult;
use tigshop_core::Error;
use tigshop_security::web::{ok, ApiResult, AppError, AuthUser};

use super::super::AdminState;

/// IM 消息域路由（挂载于应用**根路径**，不在 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/im/conversation/message/list", get(list))
        .route("/im/conversation/message/receivedList", get(received_list))
        .route("/im/conversation/message/send", post(send))
        .route("/im/conversation/message/uploadVideo", post(upload_video))
        .route("/im/conversation/message/setRead", post(set_read))
}

/// 本域已实现路由（`/openapi/coverage` 覆盖率统计，写**完整契约路径**）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    [
        ("GET", "/im/conversation/message/list", "ImMessageController.list"),
        (
            "GET",
            "/im/conversation/message/receivedList",
            "ImMessageController.receivedList",
        ),
        ("POST", "/im/conversation/message/send", "ImMessageController.send"),
        (
            "POST",
            "/im/conversation/message/uploadVideo",
            "ImMessageController.uploadVideo",
        ),
        (
            "POST",
            "/im/conversation/message/setRead",
            "ImMessageController.setRead",
        ),
    ]
    .iter()
    .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, p, j))
    .collect()
}

/// `GET /im/conversation/message/list`：消息列表（后台分支）。
async fn list(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    headers: HeaderMap,
    Query(dto): Query<ImMessageListDTO>,
) -> ApiResult<PageResult<ImMessageListVO>> {
    let shop_id = resolve_shop_id(&headers, &auth);
    ok(state.slice140.list_as_admin(&dto, shop_id, auth.id).await?)
}

/// `GET /im/conversation/message/receivedList`：客服消息播报记录列表。
async fn received_list(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    headers: HeaderMap,
    Query(dto): Query<ImMessageReceivedListDTO>,
) -> ApiResult<PageResult<ImMessageListVO>> {
    let shop_id = resolve_shop_id(&headers, &auth);
    ok(state.slice140.received_list(&dto, shop_id, auth.id).await?)
}

/// `POST /im/conversation/message/send`：发送消息（后台分支）。
async fn send(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    headers: HeaderMap,
    Json(dto): Json<ImMessageSendDTO>,
) -> ApiResult<ImMessageSendVO> {
    let shop_id = resolve_shop_id(&headers, &auth);
    ok(state.slice140.send_as_admin(&dto, auth.id, shop_id).await?)
}

/// `POST /im/conversation/message/uploadVideo`：上传视频（multipart）。**未迁移**。
async fn upload_video(
    State(state): State<AdminState>,
    mut multipart: Multipart,
) -> ApiResult<UploadVideoInfoVO> {
    let (file_name, bytes) = read_upload_file(&mut multipart).await?;
    ok(state.slice140.upload_video(file_name.as_deref(), &bytes)?)
}

/// `POST /im/conversation/message/setRead`：设置已读（后台分支）。
async fn set_read(
    State(state): State<AdminState>,
    headers: HeaderMap,
    Extension(auth): Extension<AuthUser>,
    Json(dto): Json<ImSetReadDTO>,
) -> ApiResult<bool> {
    let shop_id = resolve_shop_id(&headers, &auth);
    ok(state.slice140.set_read_as_admin(&dto, shop_id).await?)
}

/// 读取请求头文本值（缺失 / 非 UTF-8 / 全空白 → `None`）。
fn header_str(headers: &HeaderMap, name: &str) -> Option<String> {
    headers
        .get(name)
        .and_then(|v| v.to_str().ok())
        .map(str::trim)
        .filter(|s| !s.is_empty())
        .map(ToString::to_string)
}

/// 复刻 Java `HeaderUtils.getShopId()` 的三态（`admin`→0 / `shop`,`pos`→`x-shop-id` / 其它→null）。
fn resolve_shop_id(headers: &HeaderMap, auth: &AuthUser) -> Option<i32> {
    let admin_type = header_str(headers, "x-admin-type").or_else(|| auth.admin_type.clone());
    let shop_id = header_str(headers, "x-shop-id").and_then(|v| v.parse::<i32>().ok());
    match admin_type.as_deref() {
        Some("admin") => Some(0),
        Some("shop" | "pos") => shop_id,
        _ => None,
    }
}

/// 读取 `multipart/form-data` 的 `file` 字段（文件名 + 字节流）。
async fn read_upload_file(multipart: &mut Multipart) -> Result<(Option<String>, Vec<u8>), AppError> {
    let mut file_name: Option<String> = None;
    let mut bytes: Vec<u8> = Vec::new();
    while let Some(field) = multipart
        .next_field()
        .await
        .map_err(|e| AppError(Error::Validation(format!("解析上传表单失败: {e}"))))?
    {
        if field.name().unwrap_or_default() == "file" {
            file_name = field.file_name().map(str::to_string);
            bytes = field
                .bytes()
                .await
                .map_err(|e| AppError(Error::Validation(format!("读取上传文件失败: {e}"))))?
                .to_vec();
            break;
        }
    }
    Ok((file_name, bytes))
}
