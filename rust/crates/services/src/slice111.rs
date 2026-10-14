//! 店铺资金变化 域服务（slice-111）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-111.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 店铺资金变化 域服务。
pub struct Slice111Service {
    repos: Arc<Repositories>,
}

impl Slice111Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice111::Slice111Repository {
        &self.repos.slice111
    }
}
