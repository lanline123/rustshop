//! 商品收藏 域服务（slice-082）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-082.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 商品收藏 域服务。
pub struct Slice082Service {
    repos: Arc<Repositories>,
}

impl Slice082Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice082::Slice082Repository {
        &self.repos.slice082
    }
}
