//! 客户成交 域服务（slice-094）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-094.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 客户成交 域服务。
pub struct Slice094Service {
    repos: Arc<Repositories>,
}

impl Slice094Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice094::Slice094Repository {
        &self.repos.slice094
    }
}
