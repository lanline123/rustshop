//! 购物车管理 域服务（slice-200）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-200.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 购物车管理 域服务。
pub struct Slice200Service {
    repos: Arc<Repositories>,
}

impl Slice200Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice200::Slice200Repository {
        &self.repos.slice200
    }
}
