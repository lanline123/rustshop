//! 系统设置（基础/商品/购物等配置读写） 域服务（slice-187）骨架。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`。
//! 详见 `rust/docs/prompts/slice-187.md`。

use std::sync::Arc;

use tigshop_infrastructure::db::Repositories;

/// 系统设置（基础/商品/购物等配置读写） 域服务。
pub struct Slice187Service {
    repos: Arc<Repositories>,
}

impl Slice187Service {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储。
    #[must_use]
    pub fn repo(&self) -> &tigshop_infrastructure::db::repo::slice187::Slice187Repository {
        &self.repos.slice187
    }
}
