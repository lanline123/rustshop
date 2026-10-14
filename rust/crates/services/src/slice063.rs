//! 分销概览控制器 域服务（slice-063）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-063.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 分销概览控制器 域服务。
pub struct Slice063Service {
    repos: Arc<Repositories>,
}

impl Slice063Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice063::Slice063Repository {
        &self.repos.slice063
    }
}
