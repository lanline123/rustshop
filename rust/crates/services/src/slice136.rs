//! 文章分类 域服务（slice-136）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-136.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 文章分类 域服务。
pub struct Slice136Service {
    repos: Arc<Repositories>,
}

impl Slice136Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice136::Slice136Repository {
        &self.repos.slice136
    }
}
