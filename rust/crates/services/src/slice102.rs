//! 店铺保证金交易明细 域服务（slice-102）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-102.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 店铺保证金交易明细 域服务。
pub struct Slice102Service {
    repos: Arc<Repositories>,
}

impl Slice102Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice102::Slice102Repository {
        &self.repos.slice102
    }
}
