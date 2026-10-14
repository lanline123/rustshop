//! 打印日志 域服务（slice-118）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-118.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 打印日志 域服务。
pub struct Slice118Service {
    repos: Arc<Repositories>,
}

impl Slice118Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice118::Slice118Repository {
        &self.repos.slice118
    }
}
