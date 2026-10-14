//! 代客下单配送方式 域服务（slice-034）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-034.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 代客下单配送方式 域服务。
pub struct Slice034Service {
    repos: Arc<Repositories>,
}

impl Slice034Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice034::Slice034Repository {
        &self.repos.slice034
    }
}
