//! Web 层公共组件：统一错误响应、JWT 认证中间件。
//!
//! 对应原 Java：
//! - `GlobalExceptionHandler` → [`AppError`]（`IntoResponse`）
//! - Spring Security `JwtAuthenticationFilter` → [`require_auth`] 中间件

use axum::{
    extract::{Request, State},
    http::{header, StatusCode},
    middleware::Next,
    response::{IntoResponse, Response},
    Json,
};
use tigshop_core::error::ApiResponse;
use tigshop_core::Error;

use crate::JwtService;

/// 全局错误响应包装：任意 `tigshop_core::Error` → 标准 JSON 响应。
///
/// ⚠️ 必须派生 `Debug`：测试里 `Result::expect(...)` / `unwrap()` 要求
/// 错误类型实现 `Debug`（否则 E0277: `AppError` doesn't implement `Debug`）。
#[derive(Debug)]
pub struct AppError(pub Error);

impl IntoResponse for AppError {
    fn into_response(self) -> Response {
        let status = self.0.status_code();
        let body: ApiResponse<()> = self.0.into();
        // 业务错误保持 HTTP 200（兼容原 Java ResultWrapper 风格）；
        // 认证/权限/参数错误使用真实 HTTP 状态码。
        let status = match status {
            StatusCode::OK => StatusCode::BAD_REQUEST,
            s => s,
        };
        (status, Json(body)).into_response()
    }
}

impl From<Error> for AppError {
    fn from(e: Error) -> Self {
        Self(e)
    }
}

impl From<anyhow::Error> for AppError {
    fn from(e: anyhow::Error) -> Self {
        Self(Error::Internal(e.to_string()))
    }
}

/// `Result<T>` 的 API 层别名。
pub type ApiResult<T> = Result<Json<ApiResponse<T>>, AppError>;

/// 成功响应便捷函数。
pub fn ok<T: serde::Serialize>(data: T) -> ApiResult<T> {
    Ok(Json(ApiResponse::success(data)))
}

/// 已认证身份（写入请求扩展，handler 用 `Extension<AuthUser>` 提取）。
#[derive(Debug, Clone)]
pub struct AuthUser {
    pub id: i32,
    pub username: String,
    pub role_id: Option<i32>,
    pub admin_type: Option<String>,
}

/// 认证中间件：校验 `Authorization: Bearer <JWT>`，
/// 通过后将已认证身份写入请求扩展。
///
/// 对应原 Java `JwtAuthenticationFilter.doFilterInternal`。
pub async fn require_auth(
    State(jwt): State<JwtService>,
    mut req: Request,
    next: Next,
) -> Result<Response, AppError> {
    let token = req
        .headers()
        .get(header::AUTHORIZATION)
        .and_then(|v| v.to_str().ok());

    let claims = jwt.from_authorization(token)?;
    let id = claims
        .sub
        .parse::<i32>()
        .map_err(|_| Error::Unauthorized("令牌主体无效".into()))?;
    req.extensions_mut().insert(AuthUser {
        id,
        username: claims.usr,
        role_id: claims.role,
        admin_type: claims.typ,
    });
    Ok(next.run(req).await)
}
