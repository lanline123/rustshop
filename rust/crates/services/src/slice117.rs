//! 微商名片邀请 域服务（slice-117）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-117.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 微商名片邀请 域服务。
pub struct Slice117Service {
    repos: Arc<Repositories>,
}

impl Slice117Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice117::Slice117Repository {
        &self.repos.slice117
    }
}
