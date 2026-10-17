//! merchant 领域服务。

pub mod merchant_client;
pub mod order_config;
pub mod shop;
pub mod shop_client;
pub mod shop_client_category;
pub mod shop_manage;
pub mod shop_product_category;
pub mod vendor_deposit_shop;

use std::sync::Arc;

/// merchant 领域服务集合（由组合根装配一次，api 层只持有 `Arc<Services>`）。
#[derive(Clone)]
pub struct Services {
    pub merchant_client: Arc<merchant_client::MerchantClientService>,
    pub order_config: Arc<order_config::OrderConfigService>,
    pub shop: Arc<shop::ShopService>,
    pub shop_client: Arc<shop_client::ShopClientService>,
    pub shop_client_category: Arc<shop_client_category::ShopClientCategoryService>,
    pub shop_manage: Arc<shop_manage::ShopManageService>,
    pub shop_product_category: Arc<shop_product_category::ShopProductCategoryService>,
    pub vendor_deposit_shop: Arc<vendor_deposit_shop::VendorDepositShopService>,
}
