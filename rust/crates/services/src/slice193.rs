//! 设置-汇付斗拱支付 域服务（slice-193）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-193.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 设置-汇付斗拱支付 域服务。
pub struct Slice193Service {
    repos: Arc<Repositories>,
}

impl Slice193Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice193::Slice193Repository {
        &self.repos.slice193
    }
}
