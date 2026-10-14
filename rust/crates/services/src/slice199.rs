//! 账期订单管理 域服务（slice-199）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-199.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 账期订单管理 域服务。
pub struct Slice199Service {
    repos: Arc<Repositories>,
}

impl Slice199Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice199::Slice199Repository {
        &self.repos.slice199
    }
}
