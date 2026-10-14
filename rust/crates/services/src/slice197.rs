//! 账户总览 域服务（slice-197）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-197.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 账户总览 域服务。
pub struct Slice197Service {
    repos: Arc<Repositories>,
}

impl Slice197Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice197::Slice197Repository {
        &self.repos.slice197
    }
}
