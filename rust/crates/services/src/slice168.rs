//! 统计基础表 域服务（slice-168）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-168.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 统计基础表 域服务。
pub struct Slice168Service {
    repos: Arc<Repositories>,
}

impl Slice168Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice168::Slice168Repository {
        &self.repos.slice168
    }
}
