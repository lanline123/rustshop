//! 设置 域服务（slice-189）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-189.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 设置 域服务。
pub struct Slice189Service {
    repos: Arc<Repositories>,
}

impl Slice189Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice189::Slice189Repository {
        &self.repos.slice189
    }
}
