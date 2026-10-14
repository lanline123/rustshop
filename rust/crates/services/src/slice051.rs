//! 全局搜索 域服务（slice-051）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-051.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 全局搜索 域服务。
pub struct Slice051Service {
    repos: Arc<Repositories>,
}

impl Slice051Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice051::Slice051Repository {
        &self.repos.slice051
    }
}
