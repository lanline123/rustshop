//! 客服接入规则 域服务（slice-098）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-098.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 客服接入规则 域服务。
pub struct Slice098Service {
    repos: Arc<Repositories>,
}

impl Slice098Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice098::Slice098Repository {
        &self.repos.slice098
    }
}
