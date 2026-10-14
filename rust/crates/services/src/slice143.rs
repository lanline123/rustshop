//! 版本 域服务（slice-143）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-143.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 版本 域服务。
pub struct Slice143Service {
    repos: Arc<Repositories>,
}

impl Slice143Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice143::Slice143Repository {
        &self.repos.slice143
    }
}
