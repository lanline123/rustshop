//! 保证金设置 域服务（slice-047）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-047.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 保证金设置 域服务。
pub struct Slice047Service {
    repos: Arc<Repositories>,
}

impl Slice047Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice047::Slice047Repository {
        &self.repos.slice047
    }
}
