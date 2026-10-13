//! 管理端品牌路由。
//! 对应原 Java `com.tigshop.adminapi.controller.product.BrandController`
//! （`@RequestMapping("/adminapi/product/brand")`）。
//!
//! | 本模块路由 | 原 Java | 权限 |
//! |-----------|--------|------|
//! | `GET  /adminapi/product/brand/list` | `BrandController.list` | `decorate`/`brandManage`/`vendorProductManage`/`productManage` |
//! | `GET  /adminapi/product/brand/detail` | `BrandController.detail` | `brandManage`/`vendorProductManage`/`productManage` |
//! | `POST /adminapi/product/brand/del` | `BrandController.del` | `brandModifyManage` |
//! | `POST /adminapi/product/brand/create` | `BrandController.create` | `brandModifyManage` |
//! | `POST /adminapi/product/brand/update` | `BrandController.update` | `brandModifyManage` |
//! | `POST /adminapi/product/brand/updateField` | `BrandController.updateField` | `brandModifyManage` |
//! | `POST /adminapi/product/brand/batch` | `BrandController.batch` | `brandModifyManage` |
//! | `GET  /adminapi/product/brand/search` | `BrandController.search` | `decorate`/`brandManage`/`vendorProductManage`/`productManage` |
//! | `POST /adminapi/product/brand/updateFirstWord` | `BrandController.updateFirstWord` | `brandManage`/`vendorProductManage`/`productManage` |
//! | `GET  /adminapi/product/brand/auditList` | `BrandController.auditList` | `brandManage`/`vendorProductManage`/`productManage` |
//! | `POST /adminapi/product/brand/audit` | `BrandController.audit` | `brandManage`/`vendorProductManage`/`productManage` |
//! | `GET  /adminapi/product/brand/auditWaitNum` | `BrandController.auditWaitNum` | `brandManage`/`vendorProductManage`/`productManage` |
//!
//! > **权限说明**：Java 用 `@PreAuthorize("@pms.hasPermission(...)")` 在方法级
//! > 校验；Rust 侧 `require_auth` 目前只做登录态校验，权限矩阵尚未实现
//! > （与 catalog.rs 中分类域的已知缺口一致）。
//! > **多租户说明**：Java 通过 `HeaderUtils.getShopId()`（`x-admin-type` /
//! > `x-shop-id` 请求头）区分平台与店铺账号；Rust 侧 JWT 尚未签发
//! > `shop_id`，列表/创建/更新按显式参数处理（见 `slice005::BrandListQuery`
//! > 与各 DTO 的 `shopId` 字段），待鉴权中间件补齐后接入。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Json, Router,
};
use serde::Deserialize;
use tigshop_core::models::slice005::{
    BrandAuditDTO, BrandDTO, BrandListQuery, BrandSearchVO, BrandVO, BatchDTO, OperateDTO,
    UpdateFieldDTO,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult};

use super::super::AdminState;

/// 品牌域路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/product/brand/list", get(brand_list))
        .route("/product/brand/detail", get(brand_detail))
        .route("/product/brand/del", post(brand_del))
        .route("/product/brand/create", post(brand_create))
        .route("/product/brand/update", post(brand_update))
        .route("/product/brand/updateField", post(brand_update_field))
        .route("/product/brand/batch", post(brand_batch))
        .route("/product/brand/search", get(brand_search))
        .route("/product/brand/updateFirstWord", post(brand_update_first_word))
        .route("/product/brand/auditList", get(brand_audit_list))
        .route("/product/brand/audit", post(brand_audit))
        .route("/product/brand/auditWaitNum", get(brand_audit_wait_num))
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    [
        ("GET", "/adminapi/product/brand/list", "BrandController.list"),
        ("GET", "/adminapi/product/brand/detail", "BrandController.detail"),
        ("POST", "/adminapi/product/brand/del", "BrandController.del"),
        ("POST", "/adminapi/product/brand/create", "BrandController.create"),
        ("POST", "/adminapi/product/brand/update", "BrandController.update"),
        ("POST", "/adminapi/product/brand/updateField", "BrandController.updateField"),
        ("POST", "/adminapi/product/brand/batch", "BrandController.batch"),
        ("GET", "/adminapi/product/brand/search", "BrandController.search"),
        ("POST", "/adminapi/product/brand/updateFirstWord", "BrandController.updateFirstWord"),
        ("GET", "/adminapi/product/brand/auditList", "BrandController.auditList"),
        ("POST", "/adminapi/product/brand/audit", "BrandController.audit"),
        ("GET", "/adminapi/product/brand/auditWaitNum", "BrandController.auditWaitNum"),
    ]
    .iter()
    .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, p, j))
    .collect()
}

/// `GET /adminapi/product/brand/list`：品牌分页。
///
/// 对应 Java `BrandController.list`（`BrandListDTO` 查询参数）。
async fn brand_list(
    State(state): State<AdminState>,
    Query(q): Query<BrandListQuery>,
) -> ApiResult<PageResult<BrandVO>> {
    ok(state.slice005.list(&q).await?)
}

/// `GET /adminapi/product/brand/detail?id=`：品牌详情。
///
/// 对应 Java `BrandController.detail`（`@RequestParam Integer id`）。
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct IdQuery {
    id: i32,
}

async fn brand_detail(
    State(state): State<AdminState>,
    Query(q): Query<IdQuery>,
) -> ApiResult<BrandDTO> {
    ok(state.slice005.detail(q.id).await?)
}

/// `POST /adminapi/product/brand/del`：删除品牌。
///
/// 对应 Java `BrandController.del`（`OperateDTO`，取 `id`）。
async fn brand_del(
    State(state): State<AdminState>,
    Json(dto): Json<OperateDTO>,
) -> ApiResult<&'static str> {
    let id = dto
        .id
        .ok_or_else(|| tigshop_core::Error::Validation("ID不能为空".into()))?;
    state.slice005.delete(id).await?;
    ok("删除成功")
}

/// `POST /adminapi/product/brand/create`：创建品牌。
///
/// 对应 Java `BrandController.create`：返回**提示语文本**
/// （平台「添加成功」/ 店铺「添加成功,等待管理员审核」，
/// Java 语义由 `HeaderUtils.getShopId()` 决定，见模块头的多租户说明）。
async fn brand_create(
    State(state): State<AdminState>,
    Json(dto): Json<BrandDTO>,
) -> ApiResult<String> {
    // JWT 尚未携带 shop_id：店铺自建品牌显式传 shopId（前端已带），
    // 未传视为平台操作（shop_id = 0）
    let shop_id = dto.shop_id.unwrap_or(0);
    state.slice005.create(&dto, shop_id).await?;
    ok(if shop_id > 0 {
        "添加成功,等待管理员审核".to_string()
    } else {
        "添加成功".to_string()
    })
}

/// `POST /adminapi/product/brand/update`：更新品牌。
///
/// 对应 Java `BrandController.update`，返回提示语。
async fn brand_update(
    State(state): State<AdminState>,
    Json(dto): Json<BrandDTO>,
) -> ApiResult<&'static str> {
    let shop_id = dto.shop_id.unwrap_or(0);
    state.slice005.update(&dto, shop_id).await?;
    ok(if shop_id > 0 {
        "修改成功,等待管理员审核"
    } else {
        "修改成功"
    })
}

/// `POST /adminapi/product/brand/updateField`：更新品牌字段。
///
/// 对应 Java `BrandController.updateField`（`UpdateFieldDTO` +
/// `BRAND_FIELDS` 白名单）。
async fn brand_update_field(
    State(state): State<AdminState>,
    Json(dto): Json<UpdateFieldDTO>,
) -> ApiResult<bool> {
    let id = dto
        .id
        .ok_or_else(|| tigshop_core::Error::Validation("ID不能为空".into()))?;
    let field = dto
        .field
        .ok_or_else(|| tigshop_core::Error::Validation("字段名不能为空".into()))?;
    let val = dto
        .val
        .ok_or_else(|| tigshop_core::Error::Validation("字段值不能为空".into()))?;
    ok(state.slice005.update_field(id, &field, &val).await?)
}

/// `POST /adminapi/product/brand/batch`：批量操作。
///
/// 对应 Java `BrandController.batch`（`BatchDTO`，仅支持 `type = "del"`）。
async fn brand_batch(
    State(state): State<AdminState>,
    Json(dto): Json<BatchDTO>,
) -> ApiResult<u64> {
    let ids = dto.ids.unwrap_or_default();
    ok(state.slice005.batch(&ids, dto.r#type.as_deref()).await?)
}

/// `GET /adminapi/product/brand/search`：品牌搜索（下拉数据）。
///
/// 对应 Java `BrandController.search`（返回 `BrandSearchVO`）。
async fn brand_search(State(state): State<AdminState>) -> ApiResult<BrandSearchVO> {
    ok(state.slice005.search().await?)
}

/// `POST /adminapi/product/brand/updateFirstWord`：批量重算首字母。
///
/// 对应 Java `BrandController.updateFirstWord`（无请求体，返回 `void`）。
/// Java 前端拿不到返回值，这里额外带 `updated` 计数便于运维排查，
/// 不影响契约结构（`data` 为对象而非 void）。
async fn brand_update_first_word(
    State(state): State<AdminState>,
) -> ApiResult<serde_json::Value> {
    let updated = state.slice005.update_first_word().await?;
    ok(serde_json::json!({ "updated": updated }))
}

/// `GET /adminapi/product/brand/auditList`：品牌审核列表。
///
/// 对应 Java `BrandController.auditList`：复用 `list` 查询，
/// 强制 `isAudit = 1`（只看店铺品牌）。
async fn brand_audit_list(
    State(state): State<AdminState>,
    Query(mut q): Query<BrandListQuery>,
) -> ApiResult<PageResult<BrandVO>> {
    q.is_audit = Some(1);
    ok(state.slice005.list(&q).await?)
}

/// `POST /adminapi/product/brand/audit`：品牌审核。
///
/// 对应 Java `BrandController.audit`（`BrandAuditDTO`）。
async fn brand_audit(
    State(state): State<AdminState>,
    Json(dto): Json<BrandAuditDTO>,
) -> ApiResult<&'static str> {
    state.slice005.audit(&dto).await?;
    ok("审核成功")
}

/// `GET /adminapi/product/brand/auditWaitNum`：待审核数量。
///
/// 对应 Java `BrandController.auditWaitNum`（返回 `Long`）。
async fn brand_audit_wait_num(State(state): State<AdminState>) -> ApiResult<i64> {
    ok(state.slice005.audit_wait_num().await?)
}
