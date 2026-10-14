//! 微信直播订单同步 域服务（slice-114）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-114.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 微信直播订单同步 域服务。
pub struct Slice114Service {
    repos: Arc<Repositories>,
}

impl Slice114Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice114::Slice114Repository {
        &self.repos.slice114
    }
}
