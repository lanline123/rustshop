//! 部分通用功能的控制器 域服务（slice-206）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-206.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 部分通用功能的控制器 域服务。
pub struct Slice206Service {
    repos: Arc<Repositories>,
}

impl Slice206Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice206::Slice206Repository {
        &self.repos.slice206
    }
}
