//! 管理端后台消息路由（system::admin_msg）。
//!
//! 对应原 Java `com.tigshop.adminapi.controller.msg.AdminMsgController`
//! （`@RequestMapping("/adminapi/msg/adminMsg")`，7 个端点，全部需登录）。
//!
//! > **身份/可见范围**：Java 用请求头 `x-admin-type` + `x-shop-id`/`x-vendor-id`；
//! > Rust JWT 只有 `admin_type`，`shopId`/`vendorId` 用查询参数兜底：
//! > 说明：JWT 尚无 vendor 声明，vendor 账号只能依赖查询参数 `vendorId`
//! > 兜底（无法做可信 vendor 归属校验）；已列为剩余点，待 JWT 增加 vendor_id 后再修。
//! > `admin_type == "vendor"` → 供应商视角（`vendorId`），否则店铺/平台视角
//! > （`shopId`，`admin`/缺失 → 平台视角，额外过滤 `is_broadcast = 1`）。
//!
//! > **msgCount**：Java 为 SSE 长连接；Rust 未接入 SSE，返回明确 1001 业务错误。

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Extension, Json, Router,
};
use serde::Deserialize;
use tigshop_core::models::system::admin_msg::{
    AdminMsgBroadcastVO, AdminMsgListQuery, AdminMsgTypeListVO, AdminMsgVO, HomeMsgSummaryVO,
    MsgScope,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult, AuthUser};

use crate::AdminState;

/// 本域 7 条路由（挂载路径 + 原 Java 方法名）。
const ROUTE_TABLE: [(&str, &str, &str); 7] = [
    ("GET", "/msg/adminMsg/msgCount", "AdminMsgController.getMsgCount"),
    ("GET", "/msg/adminMsg/msgTypeArr", "AdminMsgController.msgTypeArr"),
    ("GET", "/msg/adminMsg/list", "AdminMsgController.list"),
    ("GET", "/msg/adminMsg/broadcastList", "AdminMsgController.broadcastList"),
    ("GET", "/msg/adminMsg/homeSummary", "AdminMsgController.homeSummary"),
    ("POST", "/msg/adminMsg/setReaded", "AdminMsgController.setReaded"),
    ("POST", "/msg/adminMsg/setAllReaded", "AdminMsgController.setAllReaded"),
];

/// 后台消息域路由（挂载于 `/adminapi` 下）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/msg/adminMsg/msgCount", get(msg_count))
        .route("/msg/adminMsg/msgTypeArr", get(msg_type_arr))
        .route("/msg/adminMsg/list", get(list))
        .route("/msg/adminMsg/broadcastList", get(broadcast_list))
        .route("/msg/adminMsg/homeSummary", get(home_summary))
        .route("/msg/adminMsg/setReaded", post(set_readed))
        .route("/msg/adminMsg/setAllReaded", post(set_all_readed))
}

/// 本域已实现路由（完整契约路径）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, java_ref)| {
            tigshop_openapi::RouteDoc::new(method, &format!("/adminapi{path}"), java_ref)
        })
        .collect()
}

/// 已读入参（Java `AdminMsgUpdateDTO.msgId`）。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
struct ReadedBody {
    msg_id: Option<i32>,
}

/// 解析可见范围（Java `getAdminType` + `getShopId`/`getVendorId`）。
fn scope_of(auth: &AuthUser, q: &AdminMsgListQuery) -> MsgScope {
    if auth.admin_type.as_deref() == Some("vendor") {
        return MsgScope::Vendor {
            vendor_id: q.vendor_id.unwrap_or(0),
        };
    }
    // 店铺账号：强制本店；平台账号：允许显式 shopId 过滤（缺省 0）。
    let platform = auth.is_platform();
    let shop_id = if platform {
        q.shop_id.unwrap_or(0)
    } else {
        auth.scoped_shop_id().unwrap_or(0)
    };
    MsgScope::Shop { shop_id, platform }
}

async fn msg_count(State(state): State<AdminState>) -> ApiResult<()> {
    state.system.admin_msg.msg_count()?;
    ok(())
}

async fn msg_type_arr(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    Query(q): Query<AdminMsgListQuery>,
) -> ApiResult<Vec<AdminMsgTypeListVO>> {
    ok(state
        .system
        .admin_msg
        .msg_type_arr(scope_of(&auth, &q))
        .await?)
}

async fn list(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    Query(q): Query<AdminMsgListQuery>,
) -> ApiResult<PageResult<AdminMsgVO>> {
    let scope = scope_of(&auth, &q);
    ok(state.system.admin_msg.list(scope, &q).await?)
}

async fn broadcast_list(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    Query(q): Query<AdminMsgListQuery>,
) -> ApiResult<PageResult<AdminMsgBroadcastVO>> {
    let scope = scope_of(&auth, &q);
    ok(state.system.admin_msg.broadcast_list(scope, &q).await?)
}

async fn home_summary(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    Query(q): Query<AdminMsgListQuery>,
) -> ApiResult<HomeMsgSummaryVO> {
    let scope = scope_of(&auth, &q);
    ok(state.system.admin_msg.home_summary(scope).await?)
}

async fn set_readed(
    State(state): State<AdminState>,
    Json(body): Json<ReadedBody>,
) -> ApiResult<bool> {
    ok(state.system.admin_msg.set_readed(body.msg_id).await?)
}

async fn set_all_readed(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    Query(q): Query<AdminMsgListQuery>,
) -> ApiResult<u64> {
    let scope = scope_of(&auth, &q);
    ok(state.system.admin_msg.set_all_readed(scope).await?)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn implemented_routes_match_contract_exactly() {
        let expected = [
            ("GET", "/adminapi/msg/adminMsg/msgCount"),
            ("GET", "/adminapi/msg/adminMsg/msgTypeArr"),
            ("GET", "/adminapi/msg/adminMsg/list"),
            ("GET", "/adminapi/msg/adminMsg/broadcastList"),
            ("GET", "/adminapi/msg/adminMsg/homeSummary"),
            ("POST", "/adminapi/msg/adminMsg/setReaded"),
            ("POST", "/adminapi/msg/adminMsg/setAllReaded"),
        ];
        let docs = implemented_routes();
        assert_eq!(docs.len(), 7);
        for (doc, (m, p)) in docs.iter().zip(expected.iter()) {
            assert_eq!(doc.method, *m);
            assert_eq!(doc.path, *p);
        }
    }

    #[test]
    fn scope_resolution_matches_identity_model() {
        let vendor = AuthUser {
            id: 1,
            username: "v".into(),
            role_id: None,
            admin_type: Some("vendor".into()),
            shop_id: None,
        };
        let q = AdminMsgListQuery {
            vendor_id: Some(9),
            ..Default::default()
        };
        assert_eq!(scope_of(&vendor, &q), MsgScope::Vendor { vendor_id: 9 });

        let admin = AuthUser {
            id: 1,
            username: "a".into(),
            role_id: None,
            admin_type: Some("admin".into()),
            shop_id: None,
        };
        assert!(scope_of(&admin, &AdminMsgListQuery::default()).is_platform());

        let shop = AuthUser {
            id: 2,
            username: "s".into(),
            role_id: None,
            admin_type: Some("shop".into()),
            shop_id: None,
        };
        let q = AdminMsgListQuery {
            shop_id: Some(3),
            ..Default::default()
        };
        assert_eq!(
            scope_of(&shop, &q),
            MsgScope::Shop { shop_id: 3, platform: false }
        );
    }
}
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn scope_uses_trusted_identity() {
        let admin = AuthUser::new(1, "a".into(), None, Some("admin".into()), Some(0));
        let q = AdminMsgListQuery {
            shop_id: Some(9),
            ..Default::default()
        };
        match scope_of(&admin, &q) {
            MsgScope::Shop { shop_id, platform } => {
                assert_eq!(shop_id, 9);
                assert!(platform);
            }
            other => panic!("unexpected {other:?}"),
        }
        // 店铺账号传其它店铺 shopId ⇒ 强制本店
        let shop = AuthUser::new(2, "s".into(), None, Some("shop".into()), Some(5));
        match scope_of(&shop, &q) {
            MsgScope::Shop { shop_id, platform } => {
                assert_eq!(shop_id, 5);
                assert!(!platform);
            }
            other => panic!("unexpected {other:?}"),
        }
    }
}
