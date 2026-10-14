//! 装修推广模块 域服务（slice-174）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-174.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 装修推广模块 域服务。
pub struct Slice174Service {
    repos: Arc<Repositories>,
}

impl Slice174Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice174::Slice174Repository {
        &self.repos.slice174
    }
}
