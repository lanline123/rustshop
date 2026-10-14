//! 商品样式 域服务（slice-084）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-084.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 商品样式 域服务。
pub struct Slice084Service {
    repos: Arc<Repositories>,
}

impl Slice084Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice084::Slice084Repository {
        &self.repos.slice084
    }
}
