//! 地区 —— 商城前台路由（system::region_client）。
//!
//! 原 Java：`com.tigshop.api.controller.sys.RegionController`
//! （`java/tigshop-api/src/main/java/com/tigshop/api/controller/sys/RegionController.java`，
//! `@RequestMapping("/api/sys/region")`，类注释「地区控制器」）。
//!
//! ## 原 Java 路径对照表（本模块挂载于 `/api` 之下）
//!
//! | 本模块路由 | 原 Java 完整路径 | 原 Java 方法 | 契约 auth |
//! |-----------|------------------|--------------|-----------|
//! | `GET /sys/region/getRegion` | `GET /api/sys/region/getRegion` | `RegionController.getRegion` (`:35`) | optional |
//! | `GET /sys/region/getProvinceList` | `GET /api/sys/region/getProvinceList` | `RegionController.getProvinceList` (`:42`) | optional |
//! | `GET /sys/region/getUserRegion` | `GET /api/sys/region/getUserRegion` | `RegionController.getUserRegion` (`:48`) | optional |
//! | `GET /sys/region/getCityRegion` | `GET /api/sys/region/getCityRegion` | `RegionController.getCityRegion` (`:54`) | optional |
//! | `GET /sys/region/getCityListByCityName` | `GET /api/sys/region/getCityListByCityName` | `RegionController.getCityListByCityName` (`:60`) | optional |
//! | `GET /sys/region/getRegionListByRegionId` | `GET /api/sys/region/getRegionListByRegionId` | `RegionController.getRegionListByRegionId` (`:67`) | optional |
//! | `GET /sys/region/getRegionPath` | `GET /api/sys/region/getRegionPath` | `RegionController.getRegionPath` (`:74`) | optional |
//!
//! > **路径说明**：`@RequestMapping("/api/sys/region")` + 各 `@GetMapping("/xxx")`
//! > 拼出上表完整路径。本域**没有**「控制器名重复一次」的形式
//! > （对照购物车 `/api/cart/cart/list`），也**没有** `/adminapi` 版本 ——
//! > 另有一个同名 `RegionController` 在
//! > `java/tigshop-adminapi/.../controller/setting/RegionController.java`，
//! > 那个属于管理端切片，**不在本切片范围**。
//! > 本文件挂在 `/api` 之下，故 `routes()` 内只写 `/sys/region/...`。
//!
//! > **返回类型**：Java 的 7 个接口全部返回
//! > `com.tigshop.bean.model.setting.Region`（实体）或其 `List` / `List<List>`，
//! > **不使用** `vo.sys.RegionVO`。因此出参字段就是实体的 8 列。
//! > Rust 侧直接返回 `tigshop_core::entities::Region`（`entity` 已派生
//! > `Serialize`，`sqlx(rename_all = "snake_case")` →
//! > 序列化为 `region_id` / `region_name` / `first_word` / `standard_code`
//! > 等 **snake_case**；原 Java 为 camelCase `regionId` / `regionName` /
//! > `firstWord` / `standardCode`）。该字段名大小写差异属**全仓库既有统一
//! > 口径**，本切片不单方面偏离，差异已记入 `uncertain_points`。
//!
//! > **auth=optional 的落地口径**：Java 侧这 7 个接口在 `permitAll` 之下，
//! > 但 JWT 过滤器仍在链上（带合法令牌 → 写入上下文；不带 → 游客；
//! > **令牌非法 → 401**）。本仓库 `public/src/lib.rs` 的公开路由组
//! > （[`routes::merge_public`]）没有可选登录态中间件，本文件沿用
//! > shop_client / wholesale_client / seo / sunmi_print_callback 的既有约定，自带
//! > [`optional_auth`]。7 个接口均**不读**登录态（Java 方法体里也没有
//! > `StpUtil` / 用户上下文），鉴权只影响「非法令牌是否 401」。
//!
//! > **`regionIds` 参数形态**：Java 是
//! > `@RequestParam(value = "regionIds", required = false) String regionIds`，
//! > 接收逗号分隔串。Rust 侧保持 `String`（不做 `Vec` 反序列化），
//! > 否则 `?regionIds=1,110000` 会在提取阶段 400。

use axum::{
    extract::{Query, Request, State},
    http::header,
    middleware::{self, Next},
    response::Response,
    routing::get,
    Router,
};
use serde::Deserialize;
use tigshop_core::entities::Region;
use tigshop_core::models::system::region_client::CityRegionVO;
use tigshop_core::Error;
use tigshop_security::web::{ok, ApiResult, AppError, AuthUser};
use tigshop_security::JwtService;

use crate::PublicState;

/// 地区域路由（挂载于 `/api` 下）。
pub fn routes(state: &PublicState) -> Router<PublicState> {
    Router::new()
        .route("/sys/region/getRegion", get(get_region))
        .route("/sys/region/getProvinceList", get(get_province_list))
        .route("/sys/region/getUserRegion", get(get_user_region))
        .route("/sys/region/getCityRegion", get(get_city_region))
        .route("/sys/region/getCityListByCityName", get(get_city_list_by_city_name))
        .route("/sys/region/getRegionListByRegionId", get(get_region_list_by_region_id))
        .route("/sys/region/getRegionPath", get(get_region_path))
        .layer(middleware::from_fn_with_state(
            state.jwt.clone(),
            optional_auth,
        ))
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    [
        ("GET", "/api/sys/region/getRegion", "RegionController.getRegion"),
        (
            "GET",
            "/api/sys/region/getProvinceList",
            "RegionController.getProvinceList",
        ),
        (
            "GET",
            "/api/sys/region/getUserRegion",
            "RegionController.getUserRegion",
        ),
        (
            "GET",
            "/api/sys/region/getCityRegion",
            "RegionController.getCityRegion",
        ),
        (
            "GET",
            "/api/sys/region/getCityListByCityName",
            "RegionController.getCityListByCityName",
        ),
        (
            "GET",
            "/api/sys/region/getRegionListByRegionId",
            "RegionController.getRegionListByRegionId",
        ),
        (
            "GET",
            "/api/sys/region/getRegionPath",
            "RegionController.getRegionPath",
        ),
    ]
    .iter()
    .map(|(method, path, java_ref)| tigshop_openapi::RouteDoc::new(method, path, java_ref))
    .collect()
}

/// 可选登录态中间件（与 shop_client / wholesale_client / seo / sunmi_print_callback 同一实现）。
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
        let claims = jwt.from_authorization(token)?;
        let id = claims
            .sub
            .parse::<i32>()
            .map_err(|_| AppError(Error::Unauthorized("令牌主体无效".into())))?;
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

/// `GET /api/sys/region/getRegion?regionIds=1,110000` 的查询参数。
#[derive(Debug, Default, Deserialize)]
struct RegionIdsQuery {
    /// 逗号分隔的地区 ID 串（Java 侧是 `String`，不是数组）
    #[serde(default, rename = "regionIds")]
    region_ids: Option<String>,
}

/// `GET /api/sys/region/getRegionListByRegionId?regionId=110000` 的查询参数。
#[derive(Debug, Deserialize)]
struct RegionIdQuery {
    /// 地区 ID（Java 侧 `@RequestParam(value = "regionId")`，必填）
    #[serde(rename = "regionId")]
    region_id: i32,
}

/// `GET /api/sys/region/getCityListByCityName?cityName=北京` 的查询参数。
#[derive(Debug, Default, Deserialize)]
struct CityNameQuery {
    /// 城市名（Java 侧无 `@RequestParam` 注解，按形参名绑定，非必填）
    #[serde(default, rename = "cityName")]
    city_name: Option<String>,
}

/// `GET /api/sys/region/getRegion`：地区级联列表。
///
/// 对应 Java `RegionController.getRegion(@RequestParam(value = "regionIds",
/// required = false) String regionIds)`（`RegionController.java:35`）→
/// `RegionServiceImpl#getRegionByIds`。
///
/// 返回 `List<List<Region>>`：按传入 ID 串逐层给出候选子节点列表。
/// `regionIds` 缺省时只返回**默认国家**（`config.defaultCountry`）的下一级。
async fn get_region(
    State(state): State<PublicState>,
    Query(q): Query<RegionIdsQuery>,
) -> ApiResult<Vec<Vec<Region>>> {
    ok(state.system.region_client.region_cascade(q.region_ids.as_deref()).await?)
}

/// `GET /api/sys/region/getProvinceList`：省份列表。
///
/// 对应 Java `RegionController.getProvinceList()`（`RegionController.java:42`）→
/// `RegionServiceImpl#getProvinceList`：`level = 2`，名字去掉「省」「市」
/// 「自治区」，按 `first_word` 排序。
async fn get_province_list(State(state): State<PublicState>) -> ApiResult<Vec<Region>> {
    ok(state.system.region_client.province_list().await?)
}

/// `GET /api/sys/region/getUserRegion`：用户所在地区。
///
/// 对应 Java `RegionController.getUserRegion()`（`RegionController.java:48`）→
/// `RegionServiceImpl#getUserRegion`。
///
/// ⚠️ Java 会先按来访 IP 定位城市名（`Ip2RegionUtil.getRegionByIp()`），
/// 定位不到/内网时兜底 `"北京"`。Rust 侧未接入 ip2region，
/// 目前**固定走兜底分支**（等价于 Java 在 IP 为空时的行为）。
async fn get_user_region(State(state): State<PublicState>) -> ApiResult<Region> {
    ok(state.system.region_client.user_region().await?)
}

/// `GET /api/sys/region/getCityRegion`：城市列表（热门 + 全量）。
///
/// 对应 Java `RegionController.getCityRegion()`（`RegionController.java:54`）→
/// `RegionServiceImpl#getCityRegion`，返回 `CityRegionVO{hot, city}`。
///
/// Java 结果缓存在 Redis（key `city_pinying_list`）；Rust 侧未接 Redis，
/// 每次实查。
async fn get_city_region(State(state): State<PublicState>) -> ApiResult<CityRegionVO> {
    ok(state.system.region_client.city_region().await?)
}

/// `GET /api/sys/region/getCityListByCityName?cityName=...`：按名称模糊查城市。
///
/// 对应 Java `RegionController.getCityListByCityName(String cityName)`
/// （`RegionController.java:60`）→ `RegionServiceImpl#getCityListByCityName`：
/// `like(regionName, cityName) AND level = 3`。
///
/// Java 未标注 `required = false`，缺参时绑定为 `null` → `like(regionName, null)`
/// 在 MyBatis-Plus 中生成 `region_name LIKE NULL`（恒假）→ 返回空列表；
/// 本实现同样对缺参返回空列表，行为一致。
async fn get_city_list_by_city_name(
    State(state): State<PublicState>,
    Query(q): Query<CityNameQuery>,
) -> ApiResult<Vec<Region>> {
    let Some(name) = q.city_name.as_deref() else {
        return ok(Vec::new());
    };
    ok(state.system.region_client.city_list_by_name(name).await?)
}

/// `GET /api/sys/region/getRegionListByRegionId?regionId=...`：取直接子节点。
///
/// 对应 Java `RegionController.getRegionListByRegionId(@RequestParam(value =
/// "regionId") Integer regionId)`（`RegionController.java:67`）→
/// `RegionServiceImpl#getRegionByParentId`。
///
/// `regionId` 必填：缺失由 axum 提取器直接 400
/// （Java 侧 `parentId == null` 抛 `GlobalException(PARAM_ERROR)`）。
async fn get_region_list_by_region_id(
    State(state): State<PublicState>,
    Query(q): Query<RegionIdQuery>,
) -> ApiResult<Vec<Region>> {
    ok(state.system.region_client.region_list_by_parent_id(q.region_id).await?)
}

/// `GET /api/sys/region/getRegionPath?regionId=...`：完整地区路径（根 → 叶）。
///
/// 对应 Java `RegionController.getRegionPath(@RequestParam("regionId")
/// Integer regionId)`（`RegionController.java:74`）→
/// `RegionServiceImpl#getRegionPath`：向上回溯到默认国家（未配置则到根），
/// 最后反序。
async fn get_region_path(
    State(state): State<PublicState>,
    Query(q): Query<RegionIdQuery>,
) -> ApiResult<Vec<Region>> {
    ok(state.system.region_client.region_path(q.region_id).await?)
}