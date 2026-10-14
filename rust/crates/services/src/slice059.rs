//! 分销员分组 域服务（slice-059）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-059.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 分销员分组 域服务。
pub struct Slice059Service {
    repos: Arc<Repositories>,
}

impl Slice059Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice059::Slice059Repository {
        &self.repos.slice059
    }
}
