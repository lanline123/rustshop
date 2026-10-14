//! 鐢ㄦ埛绛惧埌涓績 域服务（slice-214）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-214.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 鐢ㄦ埛绛惧埌涓績 域服务。
pub struct Slice214Service {
    repos: Arc<Repositories>,
}

impl Slice214Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice214::Slice214Repository {
        &self.repos.slice214
    }
}
