//! 管理端礼品卡路由（marketing::gift_card）。
//!
//! 原 Java：`com.tigshop.adminapi.controller.promotion.GiftCardController`
//! （`@RequestMapping("/adminapi/promotion/giftCards")`）。
//!
//! ## 原 Java 路径对照表（本模块挂载于 `/adminapi` 之下）
//!
//! | 本模块路由 | 原 Java 完整路径 | 原 Java 方法 | 契约 auth |
//! |-----------|------------------|----------------|-----------|
//! | `GET /promotion/giftCards/list` | `GET /adminapi/promotion/giftCards/list` | `list` | jwt |
//! | `GET /promotion/giftCards/detail` | `…/detail` | `detail` | jwt |
//! | `GET /promotion/giftCards/importTemplate` | `…/importTemplate` | `importTemplate` | jwt |
//! | `GET /promotion/giftCards/exportList` | `…/exportList` | `exportList` | jwt |
//! | `POST /promotion/giftCards/importList` | `…/importList` | `importList` | jwt+perm |
//! | `POST /promotion/giftCards/delete` | `…/delete` | `deleteGiftCards` | jwt+perm |
//! | `GET /promotion/giftCards/giftCardExchangeDetail` | `…/giftCardExchangeDetail` | `giftCardExchangeDetail` | jwt |
//! | `POST /promotion/giftCards/updateCardStatus` | `…/updateCardStatus` | `updateGiftCardsAvailableStatus` | jwt |
//!
//! > **Excel 未迁移**：`importTemplate` / `exportList` 返回 JSON 行数据（Java
//! > 写 xlsx 二进制）；`importList` 完成 multipart「文件非空」校验后返回明确
//! > 业务错误「Excel 批量导入功能尚未迁移」。与 `catalog::e_card_group` 一致。
//!
//! > **`delete` 入参**：Java `GiftCardIdParam.cardGroupsId`（String）实际承载
//! > **礼品卡 ID**，字段名照抄。

use axum::{
    extract::{Multipart, Query, State},
    routing::{get, post},
    Json, Router,
};
use serde::Deserialize;
use tigshop_core::models::marketing::gift_card::{
    GiftCardIdParam, GiftCardUpdateStatusDTO, GiftCardsExportVO, GiftCardsListQuery,
    GiftCardsTemplateVO, GiftCardsVO,
};
use tigshop_core::types::PageResult;
use tigshop_core::Error;
use tigshop_security::web::{ok, ApiResult, AppError};

use crate::AdminState;

/// 本域 8 条路由（去掉 `/adminapi` 前缀后的路径 + 原 Java handler）。
const ROUTE_TABLE: [(&str, &str, &str); 8] = [
    ("GET", "/promotion/giftCards/list", "GiftCardController.list"),
    ("GET", "/promotion/giftCards/detail", "GiftCardController.detail"),
    (
        "GET",
        "/promotion/giftCards/importTemplate",
        "GiftCardController.importTemplate",
    ),
    (
        "GET",
        "/promotion/giftCards/exportList",
        "GiftCardController.exportList",
    ),
    (
        "POST",
        "/promotion/giftCards/importList",
        "GiftCardController.importList",
    ),
    (
        "POST",
        "/promotion/giftCards/delete",
        "GiftCardController.deleteGiftCards",
    ),
    (
        "GET",
        "/promotion/giftCards/giftCardExchangeDetail",
        "GiftCardController.giftCardExchangeDetail",
    ),
    (
        "POST",
        "/promotion/giftCards/updateCardStatus",
        "GiftCardController.updateGiftCardsAvailableStatus",
    ),
];

/// 礼品卡管理路由（挂载于 `/adminapi` 下，外层统一套 `require_auth`）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/promotion/giftCards/list", get(list))
        .route("/promotion/giftCards/detail", get(detail))
        .route("/promotion/giftCards/importTemplate", get(import_template))
        .route("/promotion/giftCards/exportList", get(export_list))
        .route("/promotion/giftCards/importList", post(import_list))
        .route("/promotion/giftCards/delete", post(delete))
        .route(
            "/promotion/giftCards/giftCardExchangeDetail",
            get(gift_card_exchange_detail),
        )
        .route("/promotion/giftCards/updateCardStatus", post(update_card_status))
}

/// 本模块已实现路由（覆盖率统计，完整契约路径）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, handler)| {
            tigshop_openapi::RouteDoc::new(method, &format!("/adminapi{path}"), handler)
        })
        .collect()
}

/// `giftCardsId` 查询参数（Java `@RequestParam Integer giftCardsId`）。
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
struct GiftCardsIdQuery {
    gift_cards_id: i32,
}

/// `cardGroupsId` 查询参数（exportList）。
#[derive(Debug, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
struct CardGroupsIdQuery {
    card_groups_id: Option<i32>,
}

async fn list(
    State(state): State<AdminState>,
    Query(q): Query<GiftCardsListQuery>,
) -> ApiResult<PageResult<GiftCardsVO>> {
    ok(state.marketing.gift_card.list(&q).await?)
}

async fn detail(
    State(state): State<AdminState>,
    Query(q): Query<GiftCardsIdQuery>,
) -> ApiResult<GiftCardsVO> {
    ok(state.marketing.gift_card.admin_detail(q.gift_cards_id).await?)
}

/// `GET /promotion/giftCards/importTemplate`：导入模板（JSON 降级）。
async fn import_template(State(state): State<AdminState>) -> ApiResult<Vec<GiftCardsTemplateVO>> {
    ok(state.marketing.gift_card.import_template())
}

/// `GET /promotion/giftCards/exportList`：导出列表（JSON 降级，Java 为 xlsx）。
async fn export_list(
    State(state): State<AdminState>,
    Query(q): Query<CardGroupsIdQuery>,
) -> ApiResult<Vec<GiftCardsExportVO>> {
    let card_groups_id = q.card_groups_id.ok_or_else(|| {
        AppError(Error::Validation("cardGroupsId不能为空".into()))
    })?;
    ok(state.marketing.gift_card.export_list(card_groups_id).await?)
}

/// `POST /promotion/giftCards/importList`：Excel 批量导入（**未迁移**）。
async fn import_list(
    State(state): State<AdminState>,
    Query(q): Query<CardGroupsIdQuery>,
    mut multipart: Multipart,
) -> ApiResult<()> {
    let (form_group_id, file_present) = read_import_form(&mut multipart).await?;
    let _ = state
        .marketing
        .gift_card
        .import_list(q.card_groups_id.or(form_group_id), file_present)
        .await?;
    ok(())
}

/// 读取 multipart 表单：`cardGroupsId` 文本字段 + `file` 是否存在（不读文件内容）。
async fn read_import_form(multipart: &mut Multipart) -> Result<(Option<i32>, bool), Error> {
    let mut group_id: Option<i32> = None;
    while let Some(field) = multipart
        .next_field()
        .await
        .map_err(|e| Error::Validation(format!("解析上传表单失败: {e}")))?
    {
        let name = field.name().unwrap_or_default().to_string();
        if name == "file" {
            return Ok((group_id, true));
        }
        let text = field
            .text()
            .await
            .map_err(|e| Error::Validation(format!("读取表单字段失败: {e}")))?;
        if name == "cardGroupsId" {
            group_id = text.trim().parse::<i32>().ok();
        }
    }
    Ok((group_id, false))
}

async fn delete(
    State(state): State<AdminState>,
    Json(param): Json<GiftCardIdParam>,
) -> ApiResult<()> {
    state.marketing.gift_card.delete(&param).await?;
    ok(())
}

async fn gift_card_exchange_detail(
    State(state): State<AdminState>,
    Query(q): Query<GiftCardsIdQuery>,
) -> ApiResult<tigshop_core::models::marketing::gift_card::GiftCardDetailVO> {
    ok(state
        .marketing
        .gift_card
        .gift_card_exchange_detail(q.gift_cards_id)
        .await?)
}

async fn update_card_status(
    State(state): State<AdminState>,
    Json(dto): Json<GiftCardUpdateStatusDTO>,
) -> ApiResult<()> {
    state.marketing.gift_card.update_card_status(&dto).await?;
    ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn implemented_routes_match_java_contract() {
        let docs = implemented_routes();
        assert_eq!(docs.len(), 8);
        let expected = [
            ("GET", "/adminapi/promotion/giftCards/list"),
            ("GET", "/adminapi/promotion/giftCards/detail"),
            ("GET", "/adminapi/promotion/giftCards/importTemplate"),
            ("GET", "/adminapi/promotion/giftCards/exportList"),
            ("POST", "/adminapi/promotion/giftCards/importList"),
            ("POST", "/adminapi/promotion/giftCards/delete"),
            ("GET", "/adminapi/promotion/giftCards/giftCardExchangeDetail"),
            ("POST", "/adminapi/promotion/giftCards/updateCardStatus"),
        ];
        for (method, path) in expected {
            assert!(
                docs.iter().any(|d| d.method == method && d.path == path),
                "缺少 {method} {path}"
            );
        }
        let mut keys: Vec<String> = docs.iter().map(tigshop_openapi::RouteDoc::key).collect();
        let mut unique = keys.clone();
        unique.sort();
        unique.dedup();
        keys.sort();
        assert_eq!(unique, keys);
    }

    #[test]
    fn route_table_matches_registrations() {
        assert_eq!(ROUTE_TABLE.len(), 8);
        for (_, path, _) in ROUTE_TABLE.iter() {
            assert!(!path.contains("/adminapi"), "{path}");
            assert!(path.starts_with("/promotion/giftCards/"), "{path}");
        }
        assert_eq!(
            implemented_routes()[0].path,
            "/adminapi/promotion/giftCards/list"
        );
    }

    #[test]
    fn delete_param_uses_java_field_name() {
        // Java `GiftCardIdParam.cardGroupsId`（String）实际承载卡 ID
        let p: GiftCardIdParam =
            serde_json::from_value(serde_json::json!({"cardGroupsId": "42"})).unwrap();
        assert_eq!(p.gift_cards_id().unwrap(), 42);
    }
}
