//! 系统设置（装修/样式类配置读写） 域服务（slice-188）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-188.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 系统设置（装修/样式类配置读写） 域服务。
pub struct Slice188Service {
    repos: Arc<Repositories>,
}

impl Slice188Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice188::Slice188Repository {
        &self.repos.slice188
    }
}
