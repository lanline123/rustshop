//! 积分兑换 域服务（slice-160）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-160.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 积分兑换 域服务。
pub struct Slice160Service {
    repos: Arc<Repositories>,
}

impl Slice160Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice160::Slice160Repository {
        &self.repos.slice160
    }
}
