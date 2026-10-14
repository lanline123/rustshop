//! 订单商品 域服务（slice-181）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-181.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 订单商品 域服务。
pub struct Slice181Service {
    repos: Arc<Repositories>,
}

impl Slice181Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice181::Slice181Repository {
        &self.repos.slice181
    }
}
