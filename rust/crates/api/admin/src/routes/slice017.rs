//! 管理端 PC 分类抽屉（首页分类栏）路由（slice-017）。
//! 对应原 Java `com.tigshop.adminapi.controller.decorate.PcCatFloorController`
//! （`@RequestMapping("/adminapi/decorate/pcCatFloor")`，
//! 类级 `@PreAuthorize("@pms.hasPermission('pcCatFloorManage')")`）。
//!
//! | 本模块路由（挂载于 `/adminapi`） | 原 Java | 权限（`@PreAuthorize`） |
//! |----------------------------------|---------|------------------------|
//! | `GET  /decorate/pcCatFloor/list` | `PcCatFloorController.list` | `pcCatFloorManage` |
//! | `GET  /decorate/pcCatFloor/detail` | `PcCatFloorController.detail` | `pcCatFloorManage` |
//! | `POST /decorate/pcCatFloor/create` | `PcCatFloorController.create` | `pcCatFloorModifyManage` |
//! | `POST /decorate/pcCatFloor/update` | `PcCatFloorController.update` | `pcCatFloorModifyManage` |
//! | `POST /decorate/pcCatFloor/del` | `PcCatFloorController.del` | `pcCatFloorModifyManage` |
//! | `POST /decorate/pcCatFloor/updateField` | `PcCatFloorController.updateField` | `pcCatFloorModifyManage` |
//! | `POST /decorate/pcCatFloor/batch` | `PcCatFloorController.batch` | `pcCatFloorModifyManage` |
//! | `POST /decorate/pcCatFloor/clearCache` | `PcCatFloorController.clearCache` | `pcCatFloorManage` |
//!
//! > **路径说明**：`@RequestMapping` 的值是 `/adminapi/decorate/pcCatFloor`，
//! > 方法级只有 `/list` 这种短路径，**没有**控制器前缀重复。因此最终路径是
//! > `/adminapi/decorate/pcCatFloor/list`（不是 `.../pcCatFloor/pcCatFloor/list`），
//! > 与 `docs/api-contract.json` 逐字符一致。
//!
//! > **权限说明**：Java 用 `@PreAuthorize("@pms.hasPermission(...)")` 在类级 +
//! > 方法级校验；Rust 侧 `require_auth` 目前只做登录态校验，权限矩阵尚未实现
//! > （与 seckill.rs / slice002.rs / slice014.rs 的已知缺口一致）。
//! > 上表「权限」列仅作对照，当前**任何已登录管理员**都能调用本域全部接口。
//!
//! > **⚠️ 合并必读（路由冲突，务必先读再合）**
//! >
//! > `crates/api/admin/src/routes/decorate.rs`（**共享文件**）已注册了与本模块
//! > **完全相同的 8 条路径 + method**（`decorate.rs` 第 85–92 行）：
//! > `/decorate/pcCatFloor/{list,detail,create,update,del,updateField,batch,clearCache}`。
//! >
//! > axum 0.8 的 `Router::merge` 对「同路径 + 同 method」重复注册会**直接 panic**
//! > （`Overlapping method route`），而 `routes/mod.rs::merge_all` 会同时合并
//! > `decorate::routes(state)` 与本模块，因此**若两边都注册，服务启动即崩**。
//! >
//! > 处置（与 slice014.rs 面对 `shop.rs` 冲突时完全一致）：
//! > **本模块 `routes()` 中这 8 条注册全部注释掉**，由 `decorate.rs` 的现有实现
//! > 先响应，保证服务能启动；`implemented_routes()` 仍声明这 8 条
//! > （它们确实已实现，只是当前挂载点在 `decorate.rs`，覆盖率统计不受影响）。
//! > 所有 handler 均已写好，作为**合并目标**保留。
//! >
//! > **主会话合并时请二选一**：
//! >
//! > 1. **推荐**：删除 `decorate.rs` 中这 8 条 `.route(...)` 注册
//! >    （第 85–92 行）与 `decorate::implemented_routes()` 里对应的 8 条记录
//! >    （第 154–161 行），然后取消本文件 [`routes`] 里的注释。
//! >    理由：`decorate.rs` 版本与本域语义有多处**可复现的偏差**：
//! >    - `updateField` 白名单写成「仅 `is_show` / `sort_order`」，而 Java 是
//! >      `CheckFieldConstants.GENERAL_FIELDS = {is_show, sort_order, status}`，
//! >      且 `decorate.rs` 把 `field` 做了 `-`→`_` 替换、只接受 4 种写法，
//! >      错误文案也不是 Java 的「字段值不合法」；
//! >    - `batch` 在 `type != "del"` 时**报错**，而 Java
//! >      `BaseServiceImpl#batch` 是**静默返回 `false`**（HTTP 200）；
//! >    - `batch` 不支持空 `ids`（Java `removeByIds([])` 返回 `false` 且不产生 SQL）；
//! >    - `list` 缺 `sortField` / `sortOrder`（Java `buildSortOrder` 会用它们排序），
//! >      且 `isShow` 不合法值（如 2）会**真的去过滤**，Java 是忽略该条件；
//! >    - `create` 把库列 `category_names` 写成「按全角逗号切分 `cat_floor_name`」
//! >      的结果，而 Java 写的是 `JsonUtil.toJson(createDTO.getCategoryNames())`
//! >      （原始自定义名数组）；
//! >    - `create` / `update` 里 `isShow` 缺省给了 `1`，Java 实体该字段为 `null`
//! >      时应写入 `0`（列 NOT NULL 无默认值）；
//! >    - `update` 是「先整行查出来再逐字段改」的 partial 实现，与 Java
//! >      `updateById` 的 `FieldStrategy.NOT_NULL` 语义不完全等价，且
//! >      `catFloorName` / `categoryIds` 只在显式传 `categoryIds` 时才重算；
//! >    - `detail` 在记录不存在时返回 `Error::NotFound`（与 Java 返回 `null`
//! >      体不同），但 `list` 的 VO 把 `categoryIds` / `categoryNames` /
//! >      `brandIds` 由 Java 的 `null` 变成了 `[]`。
//! > 2. 或保持现状（`decorate.rs` 继续响应这 8 条），删除本文件 [`routes`] 的
//! >    注释块与 `implemented_routes()` 的声明即可。
//! >
//! > 注意：`tools/check_api_coverage.py` 按 (method, path) **去重**后统计，
//! > 两处声明不会重复计数，也不会造成覆盖率虚高。
//!
//! > **返回结构说明**：Java `list` 返回 MyBatis-Plus `Page<PcCatFloorVO>`
//! > （`records/total/size/current/pages`），Rust 统一使用项目既有
//! > `PageResult`（`list/total/totalPage/pageSize/page`），与
//! > slice001/002/005/010/014 一致。其余 7 个端点 Java 返回 `void` 或
//! > 实体，Rust 统一包在 `ApiResponse` 里（`data` 为 `true` / 文案 / 新主键）。

// 合并前 `routes()` 里的 8 条注册全部处于注释状态（见上面「合并必读」），
// 于是：`axum::routing::{get, post}` 只在注释文本里出现（unused_imports）、
// 形参 `state` 未被使用（unused_variables）、8 个 handler 全部不可达
// （dead_code）。取消那 8 行注释后，本 `allow` 应当一并删除。
#![allow(unused_imports, unused_variables, dead_code)]

use axum::{
    extract::{Query, State},
    routing::{get, post},
    Json, Router,
};
use serde::Deserialize;
use tigshop_core::models::slice017::{
    BatchDTO, OperateDTO, PcCatFloorCreateDTO, PcCatFloorListQuery, PcCatFloorUpdateDTO,
    PcCatFloorVO, UpdateFieldDTO,
};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult};

use super::super::AdminState;

/// 本域全部 8 条路由（去掉 `/adminapi` 前缀后的路径 + 原 Java handler）。
///
/// **这是 [`implemented_routes`] 的唯一来源**，与 [`routes`] 里的注册一一对应
/// （`routes()` 当前因与 `decorate.rs` 冲突而注释掉注册，见模块头）。
const ROUTE_TABLE: [(&str, &str, &str); 8] = [
    // (HTTP method, 本模块路径, 原 Java handler)
    ("GET", "/decorate/pcCatFloor/list", "PcCatFloorController.list"),
    (
        "GET",
        "/decorate/pcCatFloor/detail",
        "PcCatFloorController.detail",
    ),
    (
        "POST",
        "/decorate/pcCatFloor/create",
        "PcCatFloorController.create",
    ),
    (
        "POST",
        "/decorate/pcCatFloor/update",
        "PcCatFloorController.update",
    ),
    ("POST", "/decorate/pcCatFloor/del", "PcCatFloorController.del"),
    (
        "POST",
        "/decorate/pcCatFloor/updateField",
        "PcCatFloorController.updateField",
    ),
    (
        "POST",
        "/decorate/pcCatFloor/batch",
        "PcCatFloorController.batch",
    ),
    (
        "POST",
        "/decorate/pcCatFloor/clearCache",
        "PcCatFloorController.clearCache",
    ),
];

/// PC 分类抽屉域路由（挂载于 `/adminapi` 下，全局 `require_auth` 已生效）。
///
/// ⚠️ 当前 8 条注册**全部注释**：`routes/decorate.rs` 已注册同一路径 + 同一
/// method，axum 0.8 对重叠路由直接 panic（详见模块头「合并必读」）。
/// 按模块头方案 1 删掉 `decorate.rs` 那 8 条后，取消下面注释即可整体启用。
///
/// 注意文件顶部的 `#![allow(unused_imports, unused_variables, dead_code)]`
/// 只在「注册被注释掉」的当前状态下需要，取消注释后应一并删除。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    Router::new()
    // TODO(合并): 删除 decorate.rs 第 85–92 行的 8 条重复注册后，取消以下注释
    // .route("/decorate/pcCatFloor/list", get(floor_list))
    // .route("/decorate/pcCatFloor/detail", get(floor_detail))
    // .route("/decorate/pcCatFloor/create", post(floor_create))
    // .route("/decorate/pcCatFloor/update", post(floor_update))
    // .route("/decorate/pcCatFloor/del", post(floor_del))
    // .route("/decorate/pcCatFloor/updateField", post(floor_update_field))
    // .route("/decorate/pcCatFloor/batch", post(floor_batch))
    // .route("/decorate/pcCatFloor/clearCache", post(floor_clear_cache))
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
///
/// 8 条全部声明：服务层与 handler 均已实现，当前挂载点在共享的
/// `routes/decorate.rs`（路径归属见模块头「合并必读」）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, java_ref)| {
            let full = format!("/adminapi{path}");
            tigshop_openapi::RouteDoc::new(method, &full, java_ref)
        })
        .collect()
}

/// `detail` 的查询参数（Java `@RequestParam Integer id`）。
#[derive(Debug, Deserialize)]
struct IdQuery {
    /// 分类抽屉 ID（必填）
    id: Option<i32>,
}

/// `GET /adminapi/decorate/pcCatFloor/list`：分类抽屉分页。
///
/// 对应 Java `PcCatFloorController.list(PcCatFloorListDTO listDTO)`
/// → `PcCatFloorServiceImpl#list`。
///
/// ⚠️ 与 Java 的一处差异：Java 控制器**没有** `@Validated`，
/// 所以 `PcCatFloorCreateDTO` / `PcCatFloorUpdateDTO` 上的
/// `@Size` / `@NotNull` 都不会生效（详见 `create` / `update` 注释）。
async fn floor_list(
    State(state): State<AdminState>,
    Query(q): Query<PcCatFloorListQuery>,
) -> ApiResult<PageResult<PcCatFloorVO>> {
    ok(state.slice017.list(&q).await?)
}

/// `GET /adminapi/decorate/pcCatFloor/detail?id=`：分类抽屉详情。
///
/// 对应 Java `PcCatFloorController.detail(@RequestParam Integer id)`。
/// 记录不存在时 Java 返回 `null` 体，本实现返回 `404 分类抽屉不存在`
/// （项目内 slice002/005 的既有约定）。
async fn floor_detail(
    State(state): State<AdminState>,
    Query(q): Query<IdQuery>,
) -> ApiResult<PcCatFloorVO> {
    let id = q
        .id
        .filter(|v| *v > 0)
        .ok_or_else(|| tigshop_core::Error::Validation("ID不能为空".into()))?;
    ok(state.slice017.detail(id).await?)
}

/// `POST /adminapi/decorate/pcCatFloor/create`：创建分类抽屉。
///
/// 对应 Java `PcCatFloorController.create(@RequestBody @Validated
/// PcCatFloorCreateDTO createDTO)` → `PcCatFloorServiceImpl#create`。
///
/// Java 返回 `void`；此处回传新建主键，便于前端跳转编辑
/// （与 slice014 的 `create` 处理方式一致）。
async fn floor_create(
    State(state): State<AdminState>,
    Json(dto): Json<PcCatFloorCreateDTO>,
) -> ApiResult<serde_json::Value> {
    let id = state.slice017.create(&dto).await?;
    ok(serde_json::json!({ "catFloorId": id }))
}

/// `POST /adminapi/decorate/pcCatFloor/update`：更新分类抽屉。
///
/// 对应 Java `PcCatFloorController.update(@RequestBody @Validated
/// PcCatFloorUpdateDTO updateDTO)` → `PcCatFloorServiceImpl#update`。
/// Java 返回 `void`，这里回传是否命中记录。
async fn floor_update(
    State(state): State<AdminState>,
    Json(dto): Json<PcCatFloorUpdateDTO>,
) -> ApiResult<bool> {
    ok(state.slice017.update(&dto).await?)
}

/// `POST /adminapi/decorate/pcCatFloor/del`：删除分类抽屉（硬删除）。
///
/// 对应 Java `PcCatFloorController.del(@RequestBody OperateDTO operateDTO)`
/// → `PcCatFloorServiceImpl#del`（无软删标记列，就是 `removeById`）。
async fn floor_del(
    State(state): State<AdminState>,
    Json(dto): Json<OperateDTO>,
) -> ApiResult<bool> {
    ok(state.slice017.del(&dto).await?)
}

/// `POST /adminapi/decorate/pcCatFloor/updateField`：更新单个字段。
///
/// 对应 Java `PcCatFloorController.updateField(@RequestBody UpdateFieldDTO
/// updateField)` → `updateField(updateField, GENERAL_FIELDS)`，
/// 白名单为 `CheckFieldConstants.GENERAL_FIELDS`
/// （`{is_show, sort_order, status}`）。
async fn floor_update_field(
    State(state): State<AdminState>,
    Json(dto): Json<UpdateFieldDTO>,
) -> ApiResult<bool> {
    ok(state.slice017.update_field(&dto).await?)
}

/// `POST /adminapi/decorate/pcCatFloor/batch`：批量操作（仅 `del`）。
///
/// 对应 Java `PcCatFloorController.batch(@RequestBody BatchDTO batchDTO)`
/// → `BaseServiceImpl#batch`：`type != "del"` 时**静默返回 `false`**
/// （HTTP 200，不是报错）。
async fn floor_batch(
    State(state): State<AdminState>,
    Json(dto): Json<BatchDTO>,
) -> ApiResult<bool> {
    ok(state.slice017.batch(&dto).await?)
}

/// `POST /adminapi/decorate/pcCatFloor/clearCache`：清除分类抽屉缓存。
///
/// 对应 Java `PcCatFloorController.clearCache()` →
/// `redisCache.deleteObject("cat")`。
///
/// ⚠️ Rust 侧**没有**装修楼层缓存层（也没有 `SeoResponseAdvice` 写入的
/// `seo_link_category::getCatFloor*`），因此这是**空实现**，
/// 接口保留以兼容前端调用。详见 `Slice017Service::clear_cache` 的说明。
async fn floor_clear_cache(State(state): State<AdminState>) -> ApiResult<&'static str> {
    state.slice017.clear_cache();
    ok("清除成功")
}
