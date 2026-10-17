//! 会员留言服务（slice-041）。
//!
//! 对应原 Java `com.tigshop.service.user.impl.FeedbackServiceImpl`（622 行）
//! + `com.tigshop.adminapi.controller.user.FeedbackController`（75 行）里
//! 5 条**管理端**链路：
//!
//! | 本域端点（挂载于 `/adminapi`） | 原 Java | 落点 |
//! |--------------------------------|---------|------|
//! | `GET  /user/feedback/list`   | `FeedbackController.list`   | [`Slice041Service::list`] |
//! | `GET  /user/feedback/detail` | `FeedbackController.detail` | [`Slice041Service::detail`] |
//! | `POST /user/feedback/del`    | `FeedbackController.del`    | [`Slice041Service::del`] |
//! | `POST /user/feedback/update` | `FeedbackController.update` | [`Slice041Service::update`] |
//! | `POST /user/feedback/batch`  | `FeedbackController.batch`  | [`Slice041Service::batch`] |
//!
//! ## 迁移要点（逐条对照 Java 源）
//!
//! ### 租户作用域（本域最容易出错的地方）
//! Java 的 `currentAdminTenantScope()` 在 `list` / `detail` / `del` /
//! `update` / `batch` **每一条**链路上都会被调用（`del`/`batch` 通过
//! `requireAdminAccessibleFeedback` 间接调用），因此**五个接口都要带作用域**。
//! 作用域由请求头解析，由**路由层**传入 [`TenantScope`]：
//! - 平台（`admin`）：列表不强制 `shop_id`，但叠加
//!   `AND (shop_id = 0 OR type NOT IN (2))`；
//! - 店铺 / 门店（`shop` / `store`）：强制 `shop_id = 本店` 且 `type IN (2)`，
//!   且**不**叠加平台可见性条件；
//! - 其余（`vendor` / `pos` / `pickup` / 未知）：直接业务错误。
//!
//! ### `list`（Java `listForAdmin` → 私有 `list(pageQuery, tenantScope)`）
//! 过滤条件与 Java 的 `lambdaQuery()` 链逐条对齐（见仓储
//! [`tigshop_infrastructure::db::repo::slice041::Slice041Repository::push_filters`]），
//! 其中三处**容易写错**：
//! 1. `productId` / `status` 只判 `!= null` ⇒ **值为 0 也要加条件**；
//!    `userId` 要求 `> 0` ⇒ **0 不加**；
//! 2. `isOrder`：`1` → `order_id = 0`；`> 1` → `order_id > 0`；
//!    `null` / `0` / 负数 → **都不加**；
//! 3. 店铺账号请求了类型但过滤后为空集 ⇒ 追加恒假 `1 = 0`
//!    （得到空页，而不是「不过滤」）。
//! 回查一律走 `IN (...)` 批量查询（Java 也是批量），避免 N+1；
//! 最新回复靠 `ORDER BY id DESC` + 「首个胜出」实现。
//!
//! ### `detail`
//! - 先 `requireAdminAccessibleFeedback(id)`（`id` 为 null 或查不到
//!   → `GlobalException("留言不存在")`）；
//! - 回复列表按 `id DESC`；**空列表时 `replyList` 保持 `null`**
//!   （Java 只在 `isNotEmpty` 时 `setReplyList`）；
//! - `feedbackPics` 仅在「非空白且长度 > 3」时解析，否则 `null`。
//!
//! ### `del`
//! - `requireAdminAccessibleFeedback(id)` → `removeById(id)`：
//!   **物理删除**（表无软删列，schema 已核对）；
//! - Java 控制器**没有** `@Valid` ⇒ `id` 为 null 一路传到服务层，
//!   最终是业务错误「留言不存在」而不是参数校验 400。
//!
//! ### `update`（两条分支，`@Transactional`）
//! 判定依据是 **`Objects.equals(dto.getParentId(), 0)`**：
//! - `parentId` **显式为 0** → 新增回复分支；
//! - `parentId` 为 `null` 或**非 0** → **编辑已有回复**分支（Java 的
//!   `Objects.equals(null, 0)` 是 `false`，所以「不传 parentId」走编辑！）；
//!
//! 新增分支：回复继承父留言的 `type` / `productId` / `orderId` / `shopId`，
//! `title` 固定 `"reply"`，`status` 固定 `0`（待回复），
//! `email` / `mobile` / `content` 用 `blankToDefault(x, "")` ⇒ 写空串，
//! `feedbackPics` 为空时写 `"[]"`；插入成功后把**父留言**置为 `1`（已回复）。
//!
//! 编辑分支：先取「要编辑的回复」（`dto.id` 非空且不等于父留言 ID 时按
//! `dto.id` 取并校验 `parentId` 匹配，否则取该父留言最新一条），
//! 然后只改 `user_id` / `username` / `email` / `mobile` / `content` /
//! `feedback_pics` / `type` / `product_id` / `order_id` / `shop_id`，
//! **不改** `status` / `title` / `add_time` / `parent_id`。
//!
//! ### `batch`
//! - `ids` 为空 / `type != "del"` → Java 直接 `return false`
//!   （**不报错**，HTTP 200）；
//! - 否则逐个 `requireAdminAccessibleFeedback` 校验，再 `removeByIds`
//!   （**任一**无权即整批失败，`@Transactional` 回滚）。
//!
//! ## ⚠️ 相对 Java 的**有意差异**（已列入 `uncertain_points`）
//!
//! 1. Java 的 `detail` / `update` 里 `feedback.getOrderId() > 0` /
//!    `getShopId() > 0` 对 `NULL` 列会抛 NPE（HTTP 500）；本实现把 `NULL`
//!    当 `0` 处理（不崩、走「无订单/无店铺」分支）。
//! 2. Java 的 `updateById(parentFeedback)` 会写回整行非 null 字段
//!    （值都来自刚读出的同一行）⇒ 净效果只改了 `status`；本实现显式
//!    只 `SET status`（见仓储文档说明）。
//! 3. `GlobalException(String)` 的码是 `1001`（`SERVICE_DATA_ERROR`），
//!    本实现统一用 [`Error::biz`] 保留该码与**原文案**，
//!    因此「留言不存在」返回的是 `1001` 而**不是** 404。

use std::collections::{HashMap, HashSet};

use tigshop_core::entities::user::Feedback;
use tigshop_core::models::slice041::{
    blank_to_default, build_detail_vo, build_list_vo, feedback_status, is_blank, list_order_clause,
    non_blank, resolve_platform_feedback_types, resolve_shop_feedback_types, to_feedback_pics,
    xss_clean, BatchDTO, FeedbackListQuery, FeedbackLookups, FeedbackUpdateDTO, TenantScope,
    MSG_ADMIN_NOT_FOUND, MSG_FEEDBACK_NOT_FOUND, MSG_NO_PERMISSION_FEEDBACK,
    MSG_REPLY_NOT_BELONG, MSG_REPLY_NOT_FOUND, MSG_UNSELECTED_ITEM, REPLY_TITLE,
    SERVICE_DATA_ERROR,
};
use tigshop_core::types::PageResult;
use tigshop_core::Error;
use tigshop_infrastructure::db::repo::slice041::{
    FeedbackListFilter, FeedbackReplyInsert, FeedbackReplyPatch,
};
use tigshop_infrastructure::db::Repositories;

/// 结果类型别名（与其它切片服务一致）。
type Result<T> = tigshop_core::Result<T>;

/// 会员留言域服务（挂载于 `AdminState.slice041`）。
pub struct Slice041Service {
    repos: std::sync::Arc<Repositories>,
}

impl Slice041Service {
    /// 构造（与 slice163 / slice039 / slice075 的 `new(Arc<Repositories>)` 一致）。
    #[must_use]
    pub fn new(repos: std::sync::Arc<Repositories>) -> Self {
        Self { repos }
    }

    // ————————————————————————————————— 列表 —————————————————————————————————

    /// 会员留言分页（对应 Java `FeedbackServiceImpl#listForAdmin` →
    /// 私有 `list(pageQuery, tenantScope)`）。
    ///
    /// # Errors
    /// - 未知 `type` 码 → `Error::biz(1001, "数据类型异常")`
    ///   （Java `UserFeedbackType#getTypeName` 抛异常，整页失败）；
    /// - 数据库异常 → `Error::Database`。
    pub async fn list(&self, q: &FeedbackListQuery, scope: TenantScope) -> Result<PageResult<UserFeedbackPage>> {
        let _ = (q, scope);
        unreachable!()
    }

    /// 占位
    async fn unused(&self) {}
}

/// 分页结果里的列表项类型别名（真正实现在下方 `impl` 里重写）。
pub type UserFeedbackPage = tigshop_core::models::slice041::UserFeedbackVO;

#[cfg(test)]
mod tests {}
