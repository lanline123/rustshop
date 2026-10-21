//! 前台用户订单发票路由（trade::order_invoice_client）。
//!
//! 对应 Java `OrderInvoiceClientController`（`/api/user/orderInvoice`）：
//! `GET /api/user/orderInvoice/detail`、`POST /api/user/orderInvoice/create`、
//! `POST /api/user/orderInvoice/update`。
//!
//! 契约 auth=optional：本文件自带 [`optional_auth`]；三个接口均需会员上下文，
//! 游客 → 401「请先登录」。

use axum::{
    extract::{Query, Request, State},
    http::header,
    middleware::{self, Next},
    response::Response,
    routing::{get, post},
    Extension, Json, Router,
};
use serde::Deserialize;
use tigshop_core::models::trade::order_invoice_client::{
    OrderInvoiceDetailVO, OrderInvoiceEditParam, OrderInvoiceSaveParam, MSG_LOGIN_REQUIRED,
};
use tigshop_core::Error;
use tigshop_openapi::RouteDoc;
use tigshop_security::web::{ok, ApiResult, AppError, AuthUser};
use tigshop_security::JwtService;

use crate::PublicState;

/// 本域 3 条路由。
const ROUTE_TABLE: [(&str, &str, &str); 3] = [
    ("GET", "/user/orderInvoice/detail", "OrderInvoiceClientController.detail"),
    ("POST", "/user/orderInvoice/create", "OrderInvoiceClientController.create"),
    ("POST", "/user/orderInvoice/update", "OrderInvoiceClientController.update"),
];

/// 订单发票路由（挂载于 `/api` 下）。
pub fn routes(state: &PublicState) -> Router<PublicState> {
    Router::new()
        .route("/user/orderInvoice/detail", get(detail))
        .route("/user/orderInvoice/create", post(create))
        .route("/user/orderInvoice/update", post(update))
        .layer(middleware::from_fn_with_state(
            state.jwt.clone(),
            optional_auth,
        ))
}

/// 已实现路由（契约完整路径）。
#[must_use]
pub fn implemented_routes() -> Vec<RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, handler)| RouteDoc::new(method, &format!("/api{path}"), handler))
        .collect()
}

/// 可选登录态中间件（与 content/seo.rs 一致）。
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
        let Ok(claims) = jwt.from_authorization(token) else { return Ok(next.run(req).await); };
        let Ok(id) = claims.sub.parse::<i32>() else { return Ok(next.run(req).await); };
        req.extensions_mut().insert(AuthUser {
            id,
            username: claims.usr,
            role_id: claims.role,
            admin_type: claims.typ,
            shop_id: claims.shop_id,
        });
    }
    Ok(next.run(req).await)
}

/// 当前会员 id，游客 → 401。
fn current_user_id(auth: Option<Extension<AuthUser>>) -> std::result::Result<i32, AppError> {
    auth.map(|Extension(user)| user.id)
        .filter(|id| *id > 0)
        .ok_or_else(|| AppError(Error::Unauthorized(MSG_LOGIN_REQUIRED.into())))
}

#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
struct InvoiceIdQuery {
    id: Option<i32>,
}

/// `GET /api/user/orderInvoice/detail`：发票详情。
async fn detail(
    State(state): State<PublicState>,
    auth: Option<Extension<AuthUser>>,
    Query(q): Query<InvoiceIdQuery>,
) -> ApiResult<Option<OrderInvoiceDetailVO>> {
    let user_id = current_user_id(auth)?;
    ok(state
        .trade
        .order_invoice_client
        .detail(user_id, q.id)
        .await?)
}

/// `POST /api/user/orderInvoice/create`：发票申请。
async fn create(
    State(state): State<PublicState>,
    auth: Option<Extension<AuthUser>>,
    Json(param): Json<OrderInvoiceSaveParam>,
) -> ApiResult<()> {
    let user_id = current_user_id(auth)?;
    state
        .trade
        .order_invoice_client
        .create(user_id, &param)
        .await?;
    ok(())
}

/// `POST /api/user/orderInvoice/update`：修改发票申请。
async fn update(
    State(state): State<PublicState>,
    auth: Option<Extension<AuthUser>>,
    Json(param): Json<OrderInvoiceEditParam>,
) -> ApiResult<()> {
    let user_id = current_user_id(auth)?;
    state
        .trade
        .order_invoice_client
        .update(user_id, &param)
        .await?;
    ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn implemented_routes_match_contract() {
        let docs = implemented_routes();
        assert_eq!(docs.len(), 3);
        assert_eq!(docs[0].path, "/api/user/orderInvoice/detail");
        assert_eq!(docs[1].method, "POST");
        assert_eq!(docs[2].path, "/api/user/orderInvoice/update");
    }

    #[test]
    fn routes_registers_all_table_paths() {
        assert_eq!(ROUTE_TABLE.len(), 3);
        for (_, p, _) in ROUTE_TABLE.iter() {
            assert!(p.starts_with("/user/orderInvoice/"));
        }
    }

    #[test]
    fn guest_rejected() {
        assert!(current_user_id(None).is_err());
    }
}
