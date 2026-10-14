//! 前台用户账户信息 域服务（slice-073）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-073.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 前台用户账户信息 域服务。
pub struct Slice073Service {
    repos: Arc<Repositories>,
}

impl Slice073Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice073::Slice073Repository {
        &self.repos.slice073
    }
}
