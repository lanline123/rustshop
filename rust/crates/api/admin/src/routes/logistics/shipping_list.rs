//! 管理端物流路由。
//! 对应原 Java `controller.setting.ShippingTplController` /
//! `LogisticsCompanyController`。
//!
//! > `ShippingTypeController` 已由 shipping_type 接管（见 `routes/shipping_type.rs`），
//! > 本文件不再注册 `/setting/shippingType/list`，避免重复路由导致启动 panic。
//!
//! TODO(域实现者)：对照 Java 控制器补全运费模板保存（含明细）、
//! 物流公司增删改。

use axum::{
    extract::{Query, State},
    routing::get,
    Router,
};
use tigshop_core::models::{ShippingTplListQuery, ShippingTplVO};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

/// 物流域路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/setting/shippingTpl/list", get(tpl_list))
        .route("/setting/logisticsCompany/list", get(company_list))
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    [
        ("GET", "/adminapi/setting/shippingTpl/list", "ShippingTplController.list"),
        ("GET", "/adminapi/setting/logisticsCompany/list", "LogisticsCompanyController.list"),
    ]
    .iter()
    .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, p, j))
    .collect()
}

/// `GET /adminapi/setting/shippingTpl/list`：运费模板分页。
async fn tpl_list(
    State(state): State<AdminState>,
    Query(q): Query<ShippingTplListQuery>,
) -> ApiResult<PageResult<ShippingTplVO>> {
    let page_query = tigshop_core::types::PageQuery {
        page: q.page.unwrap_or(1),
        page_size: q.page_size.unwrap_or(20),
    };
    let page = state.logistics_shipping_list.tpl_list(&q).await?;
    let list: Vec<ShippingTplVO> = page.list.iter().map(ShippingTplVO::from).collect();
    ok(PageResult::new(page.total, list, &page_query))
}

/// `GET /adminapi/setting/logisticsCompany/list`：物流公司列表。
async fn company_list(
    State(state): State<AdminState>,
) -> ApiResult<Vec<serde_json::Value>> {
    let list = state.logistics_shipping_list.companies().await?;
    let list: Vec<serde_json::Value> = list
        .iter()
        .map(|c| {
            // 库列名为 logistics_id / logistics_name（非 company_id / company_name）；
            // 对外 JSON 沿用原前端字段名。
            serde_json::json!({
                "companyId": c.logistics_id,
                "companyName": c.logistics_name,
                "isShow": c.is_show,
                "isEnabled": c.is_enabled,
            })
        })
        .collect();
    ok(list)
}