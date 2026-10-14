//! 前台用户反馈留言 域服务（slice-065）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-065.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 前台用户反馈留言 域服务。
pub struct Slice065Service {
    repos: Arc<Repositories>,
}

impl Slice065Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice065::Slice065Repository {
        &self.repos.slice065
    }
}
