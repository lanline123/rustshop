//! IM 会话（ImConversationController） 域服务（slice-097）骨架。
//!
//! 对应原 Java 控制器：详见本切片 `rust/docs/prompts/slice-097.md`。
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// IM 会话（ImConversationController） 域服务。
pub struct Slice097Service {
    repos: Arc<Repositories>,
}

impl Slice097Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice097::Slice097Repository {
        &self.repos.slice097
    }
}
