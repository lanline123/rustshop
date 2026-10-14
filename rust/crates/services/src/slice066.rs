//! 前台用户发票 域服务（slice-066）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-066.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 前台用户发票 域服务。
pub struct Slice066Service {
    repos: Arc<Repositories>,
}

impl Slice066Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice066::Slice066Repository {
        &self.repos.slice066
    }
}
