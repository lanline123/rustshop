//! 店铺分类 域服务（slice-106）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-106.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 店铺分类 域服务。
pub struct Slice106Service {
    repos: Arc<Repositories>,
}

impl Slice106Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice106::Slice106Repository {
        &self.repos.slice106
    }
}
