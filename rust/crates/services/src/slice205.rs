//! 邮箱模板管理 域服务（slice-205）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-205.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 邮箱模板管理 域服务。
pub struct Slice205Service {
    repos: Arc<Repositories>,
}

impl Slice205Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice205::Slice205Repository {
        &self.repos.slice205
    }
}
