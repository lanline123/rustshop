//! 退款申请 域服务（slice-203）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-203.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 退款申请 域服务。
pub struct Slice203Service {
    repos: Arc<Repositories>,
}

impl Slice203Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice203::Slice203Repository {
        &self.repos.slice203
    }
}
