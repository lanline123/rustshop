//! 前台微信视频号直播记录路由（content::api_wechat_live）。
//!
//! 对应 Java `GET /api/content/wechatVideoAccount/finderLiveRecord`（auth=optional）。
//! 契约 auth=optional：本文件自带 [`optional_auth`]；接口返回明确「未迁移」业务错误。

use axum::{
    extract::{Query, Request, State},
    http::header,
    middleware::{self, Next},
    response::Response,
    routing::get,
    Router,
};
use tigshop_core::models::content::api_wechat_live::{FinderLiveRecordQuery, FinderLiveRecordVO};
use tigshop_core::types::PageResult;
use tigshop_core::Error;
use tigshop_openapi::RouteDoc;
use tigshop_security::web::{ok, ApiResult, AppError, AuthUser};
use tigshop_security::JwtService;

use crate::PublicState;

/// 本域 1 条路由。
const ROUTE_TABLE: [(&str, &str, &str); 1] = [(
    "GET",
    "/content/wechatVideoAccount/finderLiveRecord",
    "WechatVideoAccountController.getFinderLiveRecord",
)];

/// 微信视频号直播记录路由（挂载于 `/api` 下）。
pub fn routes(state: &PublicState) -> Router<PublicState> {
    Router::new()
        .route(
            "/content/wechatVideoAccount/finderLiveRecord",
            get(finder_live_record),
        )
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

/// `GET /api/content/wechatVideoAccount/finderLiveRecord`：直播记录（未迁移）。
async fn finder_live_record(
    State(state): State<PublicState>,
    Query(q): Query<FinderLiveRecordQuery>,
) -> ApiResult<PageResult<FinderLiveRecordVO>> {
    ok(state.content.api_wechat_live.finder_live_record(&q).await?)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn implemented_routes_match_contract() {
        let docs = implemented_routes();
        assert_eq!(docs.len(), 1);
        assert_eq!(docs[0].method, "GET");
        assert_eq!(
            docs[0].path,
            "/api/content/wechatVideoAccount/finderLiveRecord"
        );
    }

    #[test]
    fn route_table_prefixed() {
        assert!(ROUTE_TABLE[0].1.starts_with("/content/wechatVideoAccount/"));
    }
}
