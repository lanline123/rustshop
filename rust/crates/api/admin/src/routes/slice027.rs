//! 管理端「代客下单 · 会员」路由（slice-027）。
//! 对应原 Java `com.tigshop.adminapi.controller.proxyshopping.ProxyShoppingUserController`
//! （`@RequestMapping("/adminapi/proxyShopping/user")`）。
//!
//! | 本模块路由（挂载于 `/adminapi`） | 原 Java | 权限（`@PreAuthorize`） |
//! |---------------------------------|---------|------------------------|
//! | `GET /proxyShopping/user/list` | `ProxyShoppingUserController.list` | `proxyShoppingManage` |
//! | `GET /proxyShopping/user/detail` | `ProxyShoppingUserController.detail` | `proxyShoppingManage` |
//!
//! > **路径说明**：控制器前缀 `/adminapi/proxyShopping/user` 中的
//! > `proxyShopping` 首字母小写、`S` 大写，**逐字符照抄**
//! > （`@RequestMapping` 原文如此），不是笔误。
//!
//! > **权限说明**：Java 用**类级**
//! > `@PreAuthorize("@pms.hasPermission('proxyShoppingManage')")` 做方法级校验；
//! > Rust 侧 `require_auth`（`api/admin/src/lib.rs::router` 统一套在
//! > `/adminapi` 子树外层）只做登录态校验，权限矩阵尚未实现
//! > （与 slice001 / slice002 / slice005 / slice012 / slice014 / slice018 的
//! > 已知缺口一致）。上表权限列仅作对照：当前**任何已登录管理员**都能调用
//! > 本域两条接口 —— 即「跨账号读取任意会员资料」目前**无权限门禁**。
//!
//! > **与 `/adminapi/user/user/list|detail`（slice-012）的关系**：
//! > 两个控制器调用**同一个** `UserServiceImpl` 方法，差异只有三处
//! > （`isExact` 覆盖、`password` 置 `null`、不加 `memberTags`），
//! > 由 `services/slice027.rs` 逐条复刻。两套路径**互不冲突**
//! > （校验工具：`python tools/check_route_conflicts.py --strict`）。
//!
//! > **响应结构说明**：Java 的 `Page<UserDTO>` 是 MyBatis-Plus 分页对象
//! > （`records/total/size/current/pages`），Rust 统一使用项目既有的
//! > `PageResult`（`list/total/totalPage/pageSize/page`）；
//! > `password` 按 Java `setPassword(null)` 序列化为 `null`（见 [`MemberVO`]）。
//!
//! > **`x-admin-type` 说明**：Java `HeaderUtils.getAdminType()` **只读请求头**
//! > （并会把 `shop` + `x-shop-type: 2|3` 细化为 `store` / `pickup`）。
//! > Rust 侧 `x-admin-type` 缺失时回退 JWT 的 `admin_type`
//! > （与 slice008 / 010 / 011 / 020 / 023 一致）；本域判定只看
//! > 「是否等于 `admin`」，故 `store` / `pickup` 的细化与否不影响结论。

use axum::{
    extract::{Query, State},
    http::HeaderMap,
    routing::get,
    Extension, Router,
};
use serde::{Deserialize, Serialize};
use tigshop_core::models::slice012::{UserItemVO, UserListQuery, UserMemberTagVO, UserVO};
use tigshop_core::types::PageResult;
use tigshop_security::web::{ok, ApiResult, AuthUser};

use super::super::AdminState;

/// 本域 2 条路由（去 `/adminapi` 前缀的路径 + 原 Java handler）。
///
/// [`implemented_routes`] 的唯一来源；顺序与原 Java `ProxyShoppingUserController`
/// 一致（`list` 在前，`detail` 在后）。
const ROUTE_TABLE: [(&str, &str, &str); 2] = [
    // (HTTP method, 本模块路径, 原 Java handler)
    (
        "GET",
        "/proxyShopping/user/list",
        "ProxyShoppingUserController.list",
    ),
    (
        "GET",
        "/proxyShopping/user/detail",
        "ProxyShoppingUserController.detail",
    ),
];

/// 代客下单会员域路由（挂载于 `/adminapi` 下）。
///
/// 路径逐字符对齐 Java `@RequestMapping("/adminapi/proxyShopping/user")` +
/// `@GetMapping("/list" | "/detail")`。
///
/// ⚠️ 路径必须写成**字符串字面量**（不能从 [`ROUTE_TABLE`] 里循环生成）：
/// `tools/check_api_coverage.py::collect_rust_routes` 用
/// `\.route\(\s*"(?P<path>...)"` 静态扫描字面量，循环拼接会让覆盖率统计
/// 看不见这两条路由（`cargo check` 与单元测试都查不出这个差异）。
pub fn routes(state: &AdminState) -> Router<AdminState> {
    let _ = state;
    Router::new()
        .route("/proxyShopping/user/list", get(proxy_shopping_user_list))
        .route(
            "/proxyShopping/user/detail",
            get(proxy_shopping_user_detail),
        )
}

/// 本域已实现路由（用于 `/openapi/coverage` 覆盖率统计）。
#[must_use]
pub fn implemented_routes() -> Vec<tigshop_openapi::RouteDoc> {
    ROUTE_TABLE
        .iter()
        .map(|(method, path, java_ref)| {
            tigshop_openapi::RouteDoc::new(method, &format!("/adminapi{path}"), java_ref)
        })
        .collect()
}

// ————————————————————————— 请求头 → 账号类型 —————————————————————————

/// 读取请求头文本值（缺失 / 非 UTF-8 / 纯空白 → `None`）。
///
/// 与 slice008 / 010 / 011 / 020 / 023 的 `header_str` 同一实现。
fn header_str(headers: &HeaderMap, name: &str) -> Option<String> {
    headers
        .get(name)
        .and_then(|v| v.to_str().ok())
        .map(str::trim)
        .filter(|s| !s.is_empty())
        .map(ToString::to_string)
}

/// 解析当前请求的账号类型（对应 Java `HeaderUtils.getAdminType()`）。
///
/// Java **只读** `x-admin-type` 请求头（`shop` + `x-shop-type: 2/3` 会细化为
/// `store` / `pickup`）；Rust 侧请求头缺失时回退 JWT 的 `admin_type`
/// （slice005/008/010/011/020/023 的统一约定）。本域只判定「是否等于
/// `admin`」，故 `x-shop-type` 的细化不影响结果（见模块头说明）。
fn resolve_admin_type(headers: &HeaderMap, auth: &AuthUser) -> Option<String> {
    header_str(headers, "x-admin-type").or_else(|| auth.admin_type.clone())
}

// ————————————————————————— 列表 / 详情 —————————————————————————

/// `GET /adminapi/proxyShopping/user/list`：代客下单「选会员」分页列表。
///
/// 对应 Java `ProxyShoppingUserController.list(UserListDTO userList)`
/// （`ProxyShoppingUserController.java:35-53`）：
/// 1. `HeaderUtils.getAdminType()` → 按账号类型覆盖 `isExact`
///    （只有平台账号 `admin` 才可能模糊匹配，其余一律精确）；
/// 2. `userService.list(userList)`；
/// 3. 返回前逐条 `setPassword(null)`。
///
/// 响应：与 `PageResult<UserVO>` 同构，但 `password` 恒为 `null`
/// （见 [`MemberVO`]）。
async fn proxy_shopping_user_list(
    State(state): State<AdminState>,
    Extension(auth): Extension<AuthUser>,
    headers: HeaderMap,
    Query(q): Query<UserListQuery>,
) -> ApiResult<PageResult<MemberVO>> {
    let admin_type = resolve_admin_type(&headers, &auth);
    let page = state.slice027.list(&q, admin_type.as_deref()).await?;
    let list: Vec<MemberVO> = page.list.into_iter().map(MemberVO::from).collect();
    ok(PageResult {
        total: page.total,
        list,
        total_page: page.total_page,
        page_size: page.page_size,
        page: page.page,
    })
}

/// `GET /adminapi/proxyShopping/user/detail?id=`：会员详情。
///
/// 对应 Java `ProxyShoppingUserController.detail(@RequestParam Integer id)`
/// （`ProxyShoppingUserController.java:55-63`）：
/// `userService.getUserAndUserRank(id)` + `setPassword(null)`；
/// **不**追加 `memberTags`（与 `/adminapi/user/user/detail` 的唯一结构差异，
/// 故返回 [`MemberItemVO`] 而不是直接复用 slice-012 的 `UserItemVO` 序列化）。
async fn proxy_shopping_user_detail(
    State(state): State<AdminState>,
    Query(q): Query<IdQuery>,
) -> ApiResult<MemberItemVO> {
    let id = q
        .id
        .ok_or_else(|| tigshop_core::Error::Validation("会员ID不能为空".into()))?;
    let UserItemVO { user, member_tags } = state.slice027.detail(id).await?;
    ok(MemberItemVO {
        user: MemberVO::from(user),
        member_tags,
    })
}

/// 详情查询参数（Java `@RequestParam Integer id`）。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
struct IdQuery {
    /// 会员ID
    id: Option<i32>,
}

// ————————————————————————— 响应结构（password 恒为 null） —————————————————————————

/// 代客下单会员条目（对应 Java `UserDTO`，但 `password` 恒为 `null`）。
///
/// 为什么不能直接序列化 slice-012 的 `UserVO`：它的 `password` 是 `String`，
/// `UserServiceImpl#convertToDTO` 恒写空串 `""`；而本域控制器
/// `user.setPassword(null)` 把它覆盖成 `null`。
/// 用 `#[serde(flatten)]` 包一层并**追加**一个同名的 `password` 字段，
/// 可在「其余字段结构完全一致」的前提下把 `password` 变成 `null`
/// （serde flatten + 同名字段：后写的覆盖前者的值，且只出现一次）。
#[derive(Debug, Clone, Serialize)]
struct MemberVO {
    /// 会员基础信息（平铺，字段与 slice-012 `UserVO` 完全一致）
    #[serde(flatten)]
    inner: UserVO,
    /// 密码：**恒为 `null`**（Java `user.setPassword(null)`）
    password: Option<String>,
}

impl From<UserVO> for MemberVO {
    fn from(inner: UserVO) -> Self {
        Self {
            inner,
            password: None,
        }
    }
}

/// 代客下单会员详情（对应 Java `UserItemVO`，`memberTags` 恒为空数组）。
///
/// Java 的 `UserItemVO` 是 `UserDTO` 的字段拷贝 + `memberTags`；这里用
/// `#[serde(flatten)]` + 覆写 `password` 复刻「平铺 + `memberTags`」结构。
#[derive(Debug, Clone, Serialize)]
struct MemberItemVO {
    /// 会员基础信息（平铺，`password` 为 `null`）
    #[serde(flatten)]
    user: MemberVO,
    /// 会员标签（代客下单控制器不追加 → 恒 `[]`）
    member_tags: Vec<UserMemberTagVO>,
}

// ————————————————————————— 单元测试 —————————————————————————

#[cfg(test)]
mod tests {
    use super::*;
    use axum::http::{HeaderName, HeaderValue};

    fn headers(pairs: &[(&str, &str)]) -> HeaderMap {
        let mut h = HeaderMap::new();
        for (k, v) in pairs {
            h.insert(
                HeaderName::from_bytes(k.as_bytes()).unwrap(),
                HeaderValue::from_str(v).unwrap(),
            );
        }
        h
    }

    /// 构造带 / 不带 JWT `admin_type` 的身份（`AuthUser` 未实现 `Default`）。
    fn auth_with_type(t: Option<&str>) -> AuthUser {
        AuthUser {
            id: 1,
            username: "tester".into(),
            role_id: None,
            admin_type: t.map(ToString::to_string),
        }
    }

    fn sample_user() -> UserVO {
        UserVO {
            user_id: 7,
            username: "alice".into(),
            mobile: "13800000000".into(),
            // Java `convertToDTO` 里先写成空串（slice-012 语义）
            password: String::new(),
            from_tag: 7,
            from_tag_name: "Admin".into(),
            ..Default::default()
        }
    }

    #[test]
    fn registered_paths_match_java_controller_literally() {
        // 逐字符核对 Java `@RequestMapping("/adminapi/proxyShopping/user")`
        // + `@GetMapping("/list" | "/detail")`。
        assert_eq!(ROUTE_TABLE.len(), 2);
        assert_eq!(ROUTE_TABLE[0].0, "GET");
        assert_eq!(ROUTE_TABLE[0].1, "/proxyShopping/user/list");
        assert_eq!(ROUTE_TABLE[0].2, "ProxyShoppingUserController.list");
        assert_eq!(ROUTE_TABLE[1].0, "GET");
        assert_eq!(ROUTE_TABLE[1].1, "/proxyShopping/user/detail");
        assert_eq!(ROUTE_TABLE[1].2, "ProxyShoppingUserController.detail");

        let docs = implemented_routes();
        assert_eq!(docs.len(), 2);
        // `implemented_routes` 必须带回 `/adminapi` 前缀（覆盖率统计口径）
        let paths: Vec<&str> = ROUTE_TABLE
            .iter()
            .map(|(_, p, _)| *p)
            .collect::<Vec<_>>();
        assert!(paths.iter().all(|p| p.starts_with("/proxyShopping/user")));
        assert!(!paths.iter().any(|p| p.contains("/adminapi")));
    }

    #[test]
    fn admin_type_prefers_header_then_jwt() {
        // Java 只读请求头；Rust 侧缺失时回退 JWT（既有约定）
        assert_eq!(
            resolve_admin_type(
                &headers(&[("x-admin-type", "admin")]),
                &auth_with_type(Some("shop"))
            )
            .as_deref(),
            Some("admin")
        );
        assert_eq!(
            resolve_admin_type(&headers(&[]), &auth_with_type(Some("shop"))).as_deref(),
            Some("shop")
        );
        assert_eq!(resolve_admin_type(&headers(&[]), &auth_with_type(None)), None);
        // 空白头视为缺失
        assert_eq!(
            resolve_admin_type(&headers(&[("x-admin-type", "   ")]), &auth_with_type(None)),
            None
        );
        // 大小写敏感：`Admin` 不是平台账号（Java `"admin".equals(...)`）
        assert_eq!(
            resolve_admin_type(&headers(&[("x-admin-type", "Admin")]), &auth_with_type(None))
                .as_deref(),
            Some("Admin")
        );
    }

    #[test]
    fn member_vo_serializes_password_as_null_and_keeps_fields_flat() {
        let mut vo = sample_user();
        // 模拟 slice-012 之前的中间态：密码里是非空值也必须被抹掉
        vo.password = "$2a$10$deadbeefdeadbeefdeadbe".into();
        let v = serde_json::to_value(MemberVO::from(vo)).unwrap();

        // Java `setPassword(null)` → JSON null（不是 ""，也不是缺字段）
        assert!(v.get("password").is_some(), "password 字段必须存在");
        assert!(v["password"].is_null(), "password 必须是 null");
        // 其余字段平铺在同一层（不应出现把 UserVO 包一层的键）
        assert_eq!(v["userId"], 7);
        assert_eq!(v["username"], "alice");
        assert_eq!(v["mobile"], "13800000000");
        assert_eq!(v["fromTagName"], "Admin");
        assert!(v.get("inner").is_none());
        assert!(v.get("user").is_none());
        // 密码明文/哈希绝不能出现在响应体里
        assert!(!v.to_string().contains("deadbeef"));
    }

    #[test]
    fn member_item_vo_flattens_and_carries_empty_member_tags() {
        let item = MemberItemVO {
            user: MemberVO::from(sample_user()),
            member_tags: Vec::new(),
        };
        let v = serde_json::to_value(&item).unwrap();
        assert_eq!(v["userId"], 7);
        assert_eq!(v["username"], "alice");
        assert!(v["password"].is_null());
        // 代客下单控制器不 setMemberTags → 空数组
        // 注意：`MemberItemVO.member_tags` 未加 `rename`，序列化键是 snake_case
        // 的 `member_tags`（与项目内多数 DTO 的 camelCase 口径不同，属既有实现）。
        assert_eq!(v["member_tags"].as_array().map(Vec::len), Some(0));
        // 不应把 user 再包一层
        assert!(v.get("user").is_none());
    }

    #[test]
    fn list_response_reuses_page_result_shape() {
        // 路由层用 `PageResult<MemberVO>`，字段名必须与 slice-012 的分页一致
        let page = PageResult {
            total: 1,
            list: vec![MemberVO::from(sample_user())],
            total_page: 1,
            page_size: 15,
            page: 1,
        };
        let v = serde_json::to_value(&page).unwrap();
        assert_eq!(v["total"], 1);
        // `PageResult` 只对 `page_size` 显式 rename 为 `pageSize`，
        // `total_page` 保持 snake_case（既有实现，勿臆改为 camelCase）。
        assert_eq!(v["total_page"], 1);
        assert_eq!(v["pageSize"], 15);
        assert_eq!(v["page"], 1);
        assert_eq!(v["list"][0]["userId"], 7);
        assert!(v["list"][0]["password"].is_null());
        // MyBatis-Plus 的字段名不应出现（项目统一口径）
        assert!(v.get("records").is_none());
        assert!(v.get("current").is_none());
    }

    #[test]
    fn id_query_is_deserialize_owned_and_camel_case() {
        // `Query<T>` 要求 `T: DeserializeOwned`；`#[serde(default)]` 需配合 `Default`
        let q: IdQuery = serde_json::from_value(serde_json::json!({})).unwrap();
        assert_eq!(q.id, None);
        let q: IdQuery = serde_json::from_value(serde_json::json!({"id": 12})).unwrap();
        assert_eq!(q.id, Some(12));
        // 非数字 id 必须报错（不能静默当成 None，否则 detail 会误报「会员ID不能为空」）
        assert!(serde_json::from_value::<IdQuery>(serde_json::json!({"id": "abc"})).is_err());
    }
}
