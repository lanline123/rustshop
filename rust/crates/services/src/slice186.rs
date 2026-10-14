//! 许可证 域服务（slice-186）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-186.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 许可证 域服务。
pub struct Slice186Service {
    repos: Arc<Repositories>,
}

impl Slice186Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice186::Slice186Repository {
        &self.repos.slice186
    }
}
