//! 会员人群圈选 域服务（slice-036）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-036.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 会员人群圈选 域服务。
pub struct Slice036Service {
    repos: Arc<Repositories>,
}

impl Slice036Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice036::Slice036Repository {
        &self.repos.slice036
    }
}
