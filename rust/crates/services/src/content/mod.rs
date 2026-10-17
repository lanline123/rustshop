//! content 领域服务。

pub mod article;
pub mod decorate;
pub mod home;
pub mod mobile_cat_nav;
pub mod pc_cat_floor;
pub mod pc_navigation;
pub mod seo;
pub mod share;
pub mod wechat_live;
pub mod wechat_live_sync_callback;

use std::sync::Arc;

/// content 领域服务集合（由组合根装配一次，api 层只持有 `Arc<Services>`）。
#[derive(Clone)]
pub struct Services {
    pub article: Arc<article::ContentService>,
    pub decorate: Arc<decorate::DecorateService>,
    pub mobile_cat_nav: Arc<mobile_cat_nav::MobileCatNavService>,
    pub pc_cat_floor: Arc<pc_cat_floor::PcCatFloorService>,
    pub pc_navigation: Arc<pc_navigation::PcNavigationService>,
    pub seo: Arc<seo::SeoService>,
    pub share: Arc<share::ShareService>,
    pub wechat_live_sync_callback: Arc<wechat_live_sync_callback::WechatLiveSyncCallbackService>,
    pub home: Arc<home::HomeService>,
    pub wechat_live: Arc<wechat_live::WechatLiveService>,
}
