//! 推荐 域服务（slice-130）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-130.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 推荐 域服务。
pub struct Slice130Service {
    repos: Arc<Repositories>,
}

impl Slice130Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice130::Slice130Repository {
        &self.repos.slice130
    }
}
