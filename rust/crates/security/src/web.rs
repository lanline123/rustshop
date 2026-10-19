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

/// 从请求头解析客户端 IP（`X-Forwarded-For` 首个地址，其次 `X-Real-IP`）。
///
/// 缺省返回 `"unknown"`；用于管理审计日志，避免把 actor/IP 写成固定值。
#[must_use]
pub fn client_ip(headers: &axum::http::HeaderMap) -> String {
    headers
        .get("X-Forwarded-For")
        .and_then(|v| v.to_str().ok())
        .and_then(|s| s.split(',').next())
        .map(str::trim)
        .filter(|s| !s.is_empty())
        .or_else(|| {
            headers
                .get("X-Real-IP")
                .and_then(|v| v.to_str().ok())
                .map(str::trim)
                .filter(|s| !s.is_empty())
        })
        .unwrap_or("unknown")
        .to_string()
}

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
    /// 店铺 ID（0 或 None 为平台管理员；>0 为入驻商户/门店）
    pub shop_id: Option<i32>,
}

impl AuthUser {
    /// 构造包含店铺信息的完整身份
    pub fn new(
        id: i32,
        username: String,
        role_id: Option<i32>,
        admin_type: Option<String>,
        shop_id: Option<i32>,
    ) -> Self {
        Self {
            id,
            username,
            role_id,
            admin_type,
            shop_id,
        }
    }

    /// 构造普通会员/前台商城身份（无关联商户店铺）
    pub fn member(id: i32, username: String) -> Self {
        Self {
            id,
            username,
            role_id: None,
            admin_type: Some("member".into()),
            shop_id: None,
        }
    }

    /// 是否为平台总管理账号（shop_id 为 0 或 None，且不是 shop 账号类型）
    #[must_use]
    pub fn is_platform(&self) -> bool {
        match self.admin_type.as_deref() {
            Some("shop" | "pos") => false,
            _ => self.shop_id.unwrap_or(0) == 0,
        }
    }

    /// 获取需要进行租户隔离的店铺 ID
    /// - 平台账号返回 None（查询不限制店铺）
    /// - 商户账号返回 Some(shop_id)（必须限制 WHERE shop_id = ?）
    #[must_use]
    pub fn scoped_shop_id(&self) -> Option<i32> {
        if self.is_platform() {
            None
        } else {
            self.shop_id.filter(|&id| id > 0)
        }
    }
}

/// 认证中间件：校验 `Authorization: Bearer <JWT>`，
/// 通过后将已认证身份写入请求扩展。
///
/// 对应原 Java `JwtAuthenticationFilter.doFilterInternal` + `HeaderUtils.getShopId()`。
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

    // 优先从 Claims 中读取 shop_id；若未包含则兼容从 X-Shop-Id 请求头解析（对齐 Java HeaderUtils）
    let header_shop_id = req
        .headers()
        .get("x-shop-id")
        .or_else(|| req.headers().get("X-Shop-Id"))
        .and_then(|v| v.to_str().ok())
        .and_then(|v| v.trim().parse::<i32>().ok());

    let header_admin_type = req
        .headers()
        .get("x-admin-type")
        .or_else(|| req.headers().get("X-Admin-Type"))
        .and_then(|v| v.to_str().ok())
        .map(str::to_string);

    let effective_shop_id = claims.shop_id.or(header_shop_id);
    let effective_admin_type = claims.typ.or(header_admin_type);

    req.extensions_mut().insert(AuthUser {
        id,
        username: claims.usr,
        role_id: claims.role,
        admin_type: effective_admin_type,
        shop_id: effective_shop_id,
    });
    let ip = client_ip(req.headers());
    let actor = crate::audit::AuditActor {
        admin_id: id,
        ip,
    };
    Ok(crate::audit::with_actor(actor, next.run(req)).await)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn auth_user_tenant_isolation_rules() {
        // Platform admin (typ = "admin", shop_id = 0 or None)
        let platform_admin = AuthUser {
            id: 1,
            username: "admin".into(),
            role_id: Some(1),
            admin_type: Some("admin".into()),
            shop_id: Some(0),
        };
        assert!(platform_admin.is_platform());
        assert_eq!(platform_admin.scoped_shop_id(), None);

        // Merchant admin (typ = "merchant", shop_id = 88)
        let merchant_admin = AuthUser {
            id: 2,
            username: "vendor88".into(),
            role_id: Some(2),
            admin_type: Some("merchant".into()),
            shop_id: Some(88),
        };
        assert!(!merchant_admin.is_platform());
        assert_eq!(merchant_admin.scoped_shop_id(), Some(88));

        // Unknown/unspecified type with shop_id > 0
        let other_vendor = AuthUser {
            id: 3,
            username: "vendor99".into(),
            role_id: None,
            admin_type: None,
            shop_id: Some(99),
        };
        assert!(!other_vendor.is_platform());
        assert_eq!(other_vendor.scoped_shop_id(), Some(99));

        // Superadmin with no shop_id and no typ
        let superadmin = AuthUser {
            id: 1,
            username: "super".into(),
            role_id: Some(1),
            admin_type: None,
            shop_id: None,
        };
        assert!(superadmin.is_platform());
        assert_eq!(superadmin.scoped_shop_id(), None);
    }

    #[test]
    fn client_ip_prefers_forwarded_for() {
        let mut headers = axum::http::HeaderMap::new();
        assert_eq!(client_ip(&headers), "unknown");
        headers.insert("X-Real-IP", "10.0.0.2".parse().unwrap());
        assert_eq!(client_ip(&headers), "10.0.0.2");
        headers.insert("X-Forwarded-For", "1.2.3.4, 5.6.7.8".parse().unwrap());
        assert_eq!(client_ip(&headers), "1.2.3.4");
    }
}