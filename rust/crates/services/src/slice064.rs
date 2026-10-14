//! 分销模式设置 域服务（slice-064）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-064.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 分销模式设置 域服务。
pub struct Slice064Service {
    repos: Arc<Repositories>,
}

impl Slice064Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice064::Slice064Repository {
        &self.repos.slice064
    }
}
