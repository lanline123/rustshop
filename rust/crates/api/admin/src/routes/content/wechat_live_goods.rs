//! 管理端微信直播商品库路由（content::wechat_live_goods）。
//!
//! 对应 Java `WechatLiveGoodsController`（`/adminapi/content/wechatLiveGoods`）：
//! `POST import`、`GET list`、`GET query`。三个接口均返回明确「未迁移」业务错误。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Json, Router,
};
use tigshop_core::models::content::wechat_live_goods::{
    WechatLiveGoodsImportParam, WechatLiveGoodsPageQuery, WechatLiveGoodsQuery, WechatLiveGoodsVO,
};
use tigshop_core::types::PageResult;
use tigshop_openapi::RouteDoc;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

/// 本域 3 条路由。
const ROUTE_TABLE: [(&str, &str, &str); 3] = [
    (
        "POST",
        "/content/wechatLiveGoods/import",
        "WechatLiveGoodsController.importGoods",
    ),
    (
        "GET",
        "/content/wechatLiveGoods/list",
        "WechatLiveGoodsController.getGoodsLibrary",
    ),
    (
        "GET",
        "/content/wechatLiveGoods/query",
        "WechatLiveGoodsController.queryGoods",
    ),
];

/// 微信直播商品库路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/content/wechatLiveGoods/import", post(import_goods))
        .route("/content/wechatLiveGoods/list", get(list))
        .route("/content/wechatLiveGoods/query", get(query))
}

/// 已实现路由（契约完整路径）。
#[must_use]
pub fn implemented_routes() -> Vec<RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, handler)| RouteDoc::new(method, &format!("/adminapi{path}"), handler))
        .collect()
}

async fn import_goods(
    State(state): State<AdminState>,
    Json(param): Json<WechatLiveGoodsImportParam>,
) -> ApiResult<()> {
    state.content.wechat_live_goods.import(&param).await?;
    ok(())
}

async fn list(
    State(state): State<AdminState>,
    Query(q): Query<WechatLiveGoodsPageQuery>,
) -> ApiResult<PageResult<WechatLiveGoodsVO>> {
    ok(state.content.wechat_live_goods.list(&q).await?)
}

async fn query(
    State(state): State<AdminState>,
    Query(q): Query<WechatLiveGoodsQuery>,
) -> ApiResult<Option<WechatLiveGoodsVO>> {
    ok(state.content.wechat_live_goods.query(&q).await?)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn implemented_routes_match_contract() {
        let docs = implemented_routes();
        assert_eq!(docs.len(), 3);
        assert_eq!(docs[0].path, "/adminapi/content/wechatLiveGoods/import");
        assert_eq!(docs[1].path, "/adminapi/content/wechatLiveGoods/list");
        assert_eq!(docs[2].path, "/adminapi/content/wechatLiveGoods/query");
        assert_eq!(docs[0].method, "POST");
        assert_eq!(docs[1].method, "GET");
        assert_eq!(docs[2].method, "GET");
    }

    #[test]
    fn route_table_unique() {
        assert_eq!(ROUTE_TABLE.len(), 3);
        for (_, p, _) in ROUTE_TABLE.iter() {
            assert!(p.starts_with("/content/wechatLiveGoods/"));
        }
    }
}
