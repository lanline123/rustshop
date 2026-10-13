//! 商城前台优惠券路由。
//! 对应原 Java：
//! - `com.tigshop.api.controller.user.UserCouponController`（`/api/user/coupon`）
//! - `com.tigshop.api.controller.promotion.CouponClientController`（`/api/promotion/coupon`）
//!
//! 路径与原 Java 控制器保持一致：
//! | 路由 | 原 Java |
//! |------|---------|
//! | `GET  /api/user/coupon/getList` | `UserCouponController.getList`（公开可领取列表） |
//! | `GET  /api/user/coupon/list`    | `UserCouponController.list`（我的优惠券） |
//! | `GET  /api/user/coupon/detail`  | `UserCouponController.detail` |
//! | `POST /api/user/coupon/claim`   | `UserCouponController.claim` |
//! | `POST /api/user/coupon/del`     | `UserCouponController.del` |
//! | `GET  /api/promotion/coupon/listByIds` | `CouponClientController.listByIds` |

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Extension, Json, Router,
};
use serde::Deserialize;
use tigshop_core::models::{CouponListQuery, UserCouponClaimDTO, UserCouponIdDTO, UserCouponListQuery};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult, AuthUser};

use super::super::PublicState;

/// 公开路由：可领取优惠券列表、按 ID 批量查询（游客可访问）。
pub fn public_routes() -> Router<PublicState> {
    Router::new()
        .route("/user/coupon/getList", get(available_list))
        .route("/promotion/coupon/listByIds", get(list_by_ids))
}

/// 需登录路由：领取、我的优惠券（外层已套 `require_auth`）。
///
/// 原 Java 没有 `/api/user/coupon/my`，故不注册该别名。
pub fn authorized_routes() -> Router<PublicState> {
    Router::new()
        .route("/user/coupon/list", get(my_coupons))
        .route("/user/coupon/detail", get(my_coupon_detail))
        .route("/user/coupon/claim", post(claim))
        .route("/user/coupon/del", post(delete_my_coupon))
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    [
        ("GET", "/api/user/coupon/getList", "UserCouponController.getList"),
        ("GET", "/api/promotion/coupon/listByIds", "CouponClientController.listByIds"),
        ("GET", "/api/user/coupon/list", "UserCouponController.list"),
        ("GET", "/api/user/coupon/detail", "UserCouponController.detail"),
        ("POST", "/api/user/coupon/claim", "UserCouponController.claim"),
        ("POST", "/api/user/coupon/del", "UserCouponController.del"),
    ]
    .iter()
    .map(|(m, p, j)| tigshop_openapi::RouteDoc::new(m, p, j))
    .collect()
}

/// `GET /api/user/coupon/getList`：可领取的优惠券列表。
///
/// 携带有效登录态时返回 `isReceive` 标记并过滤会员等级不匹配的券；
/// 游客则隐藏会员限定券。
async fn available_list(
    State(state): State<PublicState>,
    auth: Option<Extension<AuthUser>>,
    Query(q): Query<CouponListQuery>,
) -> ApiResult<PageResult<tigshop_core::models::CouponClientVO>> {
    // 公开路由未挂 require_auth，登录态为可选
    let page = match auth.map(|Extension(a)| a).filter(|a| a.id > 0) {
        Some(user) => state.coupon.claimable_list_for_user(user.id, &q).await?,
        None => state.coupon.claimable_list(&q).await?,
    };
    ok(page)
}

#[derive(Debug, Deserialize)]
struct IdsQuery {
    /// 逗号分隔的券 ID，如 `1,2,3`
    ids: String,
}

/// `GET /api/promotion/coupon/listByIds`：按 ID 批量查询券。
async fn list_by_ids(
    State(state): State<PublicState>,
    Query(q): Query<IdsQuery>,
) -> ApiResult<Vec<tigshop_core::models::CouponClientVO>> {
    let ids: Vec<i32> = q
        .ids
        .split(',')
        .filter_map(|s| s.trim().parse::<i32>().ok())
        .collect();
    ok(state.coupon.list_by_ids(&ids).await?)
}

/// `GET /api/user/coupon/list`：我的优惠券（需登录）。
async fn my_coupons(
    State(state): State<PublicState>,
    Extension(auth): Extension<AuthUser>,
    Query(q): Query<UserCouponListQuery>,
) -> ApiResult<PageResult<tigshop_core::models::UserCouponVO>> {
    ok(state.coupon.my_coupons(auth.id, &q).await?)
}

/// `GET /api/user/coupon/detail?id=`：券详情（含当前用户领取情况）。
async fn my_coupon_detail(
    State(state): State<PublicState>,
    Extension(auth): Extension<AuthUser>,
    Query(q): Query<IdQuery>,
) -> ApiResult<serde_json::Value> {
    let coupon = state.coupon.detail(q.id).await?;
    let usable = state.coupon.my_usable_coupons(auth.id).await?;
    let is_receive = usable.iter().any(|uc| uc.coupon_id == q.id);
    ok(serde_json::json!({
        "couponId": coupon.coupon_id,
        "couponName": coupon.coupon_name,
        "couponMoney": coupon.coupon_money,
        "couponDiscount": coupon.coupon_discount,
        "couponDesc": coupon.coupon_desc,
        "couponType": coupon.coupon_type,
        "minOrderAmount": coupon.min_order_amount,
        "sendType": coupon.send_type,
        "useDay": coupon.use_day,
        "useStartDate": coupon.use_start_date,
        "useEndDate": coupon.use_end_date,
        "isNewUser": coupon.is_new_user,
        "isReceive": i32::from(is_receive),
    }))
}

#[derive(Debug, Deserialize)]
struct IdQuery {
    id: i32,
}

/// `POST /api/user/coupon/claim`：领取优惠券（需登录）。
async fn claim(
    State(state): State<PublicState>,
    Extension(auth): Extension<AuthUser>,
    Json(dto): Json<UserCouponClaimDTO>,
) -> ApiResult<i32> {
    ok(state.coupon.claim(auth.id, dto.coupon_id).await?)
}

/// `POST /api/user/coupon/del`：删除我的优惠券（需登录）。
async fn delete_my_coupon(
    State(state): State<PublicState>,
    Extension(auth): Extension<AuthUser>,
    Json(dto): Json<UserCouponIdDTO>,
) -> ApiResult<bool> {
    state.coupon.delete_my_coupon(auth.id, dto.id).await?;
    ok(true)
}
