//! 提示管理 域服务（slice-132）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-132.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 提示管理 域服务。
pub struct Slice132Service {
    repos: Arc<Repositories>,
}

impl Slice132Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice132::Slice132Repository {
        &self.repos.slice132
    }
}
