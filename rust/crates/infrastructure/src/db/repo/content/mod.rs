//! content 领域仓储。

pub mod article;
pub mod decorate;
pub mod mobile_cat_nav;
pub mod pc_cat_floor;
pub mod pc_navigation;
pub mod seo;
pub mod share;
pub mod wechat_live_sync_callback;

use crate::db::DbPool;

#[derive(Clone)]
pub struct Repositories {
    pub article: article::ArticleRepository,
    pub decorate: decorate::DecorateRepository,
    pub mobile_cat_nav: mobile_cat_nav::MobileCatNavRepository,
    pub pc_cat_floor: pc_cat_floor::PcCatFloorRepository,
    pub pc_navigation: pc_navigation::PcNavigationRepository,
    pub seo: seo::SeoRepository,
    pub share: share::ShareRepository,
    pub wechat_live_sync_callback: wechat_live_sync_callback::WechatLiveSyncCallbackRepository,
}

impl Repositories {
    #[must_use]
    pub fn new(pool: DbPool) -> Self {
        Self {
            article: article::ArticleRepository::new(pool.clone()),
            decorate: decorate::DecorateRepository::new(pool.clone()),
            mobile_cat_nav: mobile_cat_nav::MobileCatNavRepository::new(pool.clone()),
            pc_cat_floor: pc_cat_floor::PcCatFloorRepository::new(pool.clone()),
            pc_navigation: pc_navigation::PcNavigationRepository::new(pool.clone()),
            seo: seo::SeoRepository::new(pool.clone()),
            share: share::ShareRepository::new(pool.clone()),
            wechat_live_sync_callback: wechat_live_sync_callback::WechatLiveSyncCallbackRepository::new(pool.clone()),
        }
    }
}
