//! 配置管理 域服务（slice-208）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-208.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 配置管理 域服务。
pub struct Slice208Service {
    repos: Arc<Repositories>,
}

impl Slice208Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice208::Slice208Repository {
        &self.repos.slice208
    }
}
