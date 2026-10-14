//! 行为验证 域服务（slice-170）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-170.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 行为验证 域服务。
pub struct Slice170Service {
    repos: Arc<Repositories>,
}

impl Slice170Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice170::Slice170Repository {
        &self.repos.slice170
    }
}
