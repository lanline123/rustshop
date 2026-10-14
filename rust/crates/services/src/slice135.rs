//! 文章 域服务（slice-135）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-135.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 文章 域服务。
pub struct Slice135Service {
    repos: Arc<Repositories>,
}

impl Slice135Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice135::Slice135Repository {
        &self.repos.slice135
    }
}
