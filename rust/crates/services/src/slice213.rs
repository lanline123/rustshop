//! 鎷煎洟璁板綍绠＄悊 域服务（slice-213）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-213.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 鎷煎洟璁板綍绠＄悊 域服务。
pub struct Slice213Service {
    repos: Arc<Repositories>,
}

impl Slice213Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice213::Slice213Repository {
        &self.repos.slice213
    }
}
