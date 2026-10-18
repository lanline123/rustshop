//! 微信直播商品库域服务（content::wechat_live_goods）。
//!
//! 三个端点均依赖微信开放平台「直播商品库」API（导入/审核/查询），
//! 未迁移 → 保留端点与本地校验后返回明确「未迁移」业务错误，不编造商品数据。

use std::sync::Arc;

use tigshop_core::models::content::wechat_live_goods::{
    validate_import, validate_query, WechatLiveGoodsImportParam, WechatLiveGoodsPageQuery,
    WechatLiveGoodsQuery, WechatLiveGoodsVO, MSG_NOT_MIGRATED,
};
use tigshop_core::types::PageResult;
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::content::wechat_live_goods::WechatLiveGoodsRepository;
use tigshop_infrastructure::db::Repositories;

/// 微信直播商品库服务。
pub struct WechatLiveGoodsService {
    repos: Arc<Repositories>,
}

impl WechatLiveGoodsService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本域仓储（NESTED：`self.repos.content.wechat_live_goods`）。
    #[must_use]
    pub fn repo(&self) -> &WechatLiveGoodsRepository {
        &self.repos.content.wechat_live_goods
    }

    /// 导入商品到微信直播商品库（未迁移）。
    pub async fn import(&self, param: &WechatLiveGoodsImportParam) -> Result<()> {
        validate_import(param).map_err(Error::Validation)?;
        Err(Error::biz_msg(MSG_NOT_MIGRATED))
    }

    /// 商品库列表（未迁移）。
    pub async fn list(
        &self,
        _q: &WechatLiveGoodsPageQuery,
    ) -> Result<PageResult<WechatLiveGoodsVO>> {
        Err(Error::biz_msg(MSG_NOT_MIGRATED))
    }

    /// 单商品查询（未迁移）。
    pub async fn query(&self, param: &WechatLiveGoodsQuery) -> Result<Option<WechatLiveGoodsVO>> {
        validate_query(param).map_err(Error::Validation)?;
        Err(Error::biz_msg(MSG_NOT_MIGRATED))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn validation_runs_before_not_migrated() {
        assert!(validate_import(&WechatLiveGoodsImportParam::default()).is_err());
        assert!(MSG_NOT_MIGRATED.contains("尚未迁移"));
    }
}
