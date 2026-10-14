//! 用户实名认证 域服务（slice-148）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-148.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 用户实名认证 域服务。
pub struct Slice148Service {
    repos: Arc<Repositories>,
}

impl Slice148Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice148::Slice148Repository {
        &self.repos.slice148
    }
}
