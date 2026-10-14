//! 表单令牌校验 域服务（slice-171）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-171.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 表单令牌校验 域服务。
pub struct Slice171Service {
    repos: Arc<Repositories>,
}

impl Slice171Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice171::Slice171Repository {
        &self.repos.slice171
    }
}
