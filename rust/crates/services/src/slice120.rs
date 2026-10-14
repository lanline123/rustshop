//! 打印机配置管理 域服务（slice-120）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-120.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 打印机配置管理 域服务。
pub struct Slice120Service {
    repos: Arc<Repositories>,
}

impl Slice120Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice120::Slice120Repository {
        &self.repos.slice120
    }
}
