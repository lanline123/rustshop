//! 公众号关键字回复-客服能力探测 域服务（slice-053）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-053.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 公众号关键字回复-客服能力探测 域服务。
pub struct Slice053Service {
    repos: Arc<Repositories>,
}

impl Slice053Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice053::Slice053Repository {
        &self.repos.slice053
    }
}
