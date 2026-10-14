//! 限时秒杀 域服务（slice-217）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-217.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 限时秒杀 域服务。
pub struct Slice217Service {
    repos: Arc<Repositories>,
}

impl Slice217Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice217::Slice217Repository {
        &self.repos.slice217
    }
}
