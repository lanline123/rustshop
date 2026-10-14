//! 设置-易宝支付 域服务（slice-192）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-192.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 设置-易宝支付 域服务。
pub struct Slice192Service {
    repos: Arc<Repositories>,
}

impl Slice192Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice192::Slice192Repository {
        &self.repos.slice192
    }
}
