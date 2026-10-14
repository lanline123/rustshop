//! 设置-拉卡拉支付 域服务（slice-191）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-191.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 设置-拉卡拉支付 域服务。
pub struct Slice191Service {
    repos: Arc<Repositories>,
}

impl Slice191Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice191::Slice191Repository {
        &self.repos.slice191
    }
}
