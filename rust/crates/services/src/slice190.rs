//! 设置-Apple Pay支付 域服务（slice-190）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-190.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 设置-Apple Pay支付 域服务。
pub struct Slice190Service {
    repos: Arc<Repositories>,
}

impl Slice190Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice190::Slice190Repository {
        &self.repos.slice190
    }
}
