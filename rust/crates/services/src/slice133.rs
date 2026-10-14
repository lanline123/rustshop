//! 搴楅摵璁㈠崟閰嶇疆 域服务（slice-133）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-133.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 搴楅摵璁㈠崟閰嶇疆 域服务。
pub struct Slice133Service {
    repos: Arc<Repositories>,
}

impl Slice133Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice133::Slice133Repository {
        &self.repos.slice133
    }
}
