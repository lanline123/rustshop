//! 装修模板库 域服务（slice-176）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-176.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 装修模板库 域服务。
pub struct Slice176Service {
    repos: Arc<Repositories>,
}

impl Slice176Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice176::Slice176Repository {
        &self.repos.slice176
    }
}
