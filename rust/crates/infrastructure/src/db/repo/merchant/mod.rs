//! merchant 领域仓储。

pub mod merchant_client;
pub mod order_config;
pub mod shop;
pub mod shop_client;
pub mod shop_client_category;
pub mod shop_manage;
pub mod shop_product_category;
pub mod vendor_deposit_shop;

use crate::db::DbPool;

#[derive(Clone)]
pub struct Repositories {
    pub merchant_client: merchant_client::MerchantClientRepository,
    pub order_config: order_config::OrderConfigRepository,
    pub shop: shop::ShopRepository,
    pub shop_client: shop_client::ShopClientRepository,
    pub shop_client_category: shop_client_category::ShopClientCategoryRepository,
    pub shop_manage: shop_manage::ShopManageRepository,
    pub shop_product_category: shop_product_category::ShopProductCategoryRepository,
    pub vendor_deposit_shop: vendor_deposit_shop::VendorDepositShopRepository,
}

impl Repositories {
    #[must_use]
    pub fn new(pool: DbPool) -> Self {
        Self {
            merchant_client: merchant_client::MerchantClientRepository::new(pool.clone()),
            order_config: order_config::OrderConfigRepository::new(pool.clone()),
            shop: shop::ShopRepository::new(pool.clone()),
            shop_client: shop_client::ShopClientRepository::new(pool.clone()),
            shop_client_category: shop_client_category::ShopClientCategoryRepository::new(pool.clone()),
            shop_manage: shop_manage::ShopManageRepository::new(pool.clone()),
            shop_product_category: shop_product_category::ShopProductCategoryRepository::new(pool.clone()),
            vendor_deposit_shop: vendor_deposit_shop::VendorDepositShopRepository::new(pool.clone()),
        }
    }
}
