//! 微信直播商品 域服务（slice-113）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-113.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 微信直播商品 域服务。
pub struct Slice113Service {
    repos: Arc<Repositories>,
}

impl Slice113Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice113::Slice113Repository {
        &self.repos.slice113
    }
}
