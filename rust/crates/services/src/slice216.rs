//! 限时折扣 域服务（slice-216）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-216.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 限时折扣 域服务。
pub struct Slice216Service {
    repos: Arc<Repositories>,
}

impl Slice216Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice216::Slice216Repository {
        &self.repos.slice216
    }
}
