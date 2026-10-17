//! merchant 领域仓储。

pub mod deposit_account;
pub mod deposit_recharge;
pub mod deposit_refund;
pub mod merchant_apply;
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
    pub merchant_apply: merchant_apply::MerchantApplyRepository,
    pub deposit_account: deposit_account::DepositAccountRepository,
    pub deposit_recharge: deposit_recharge::DepositRechargeRepository,
    pub deposit_refund: deposit_refund::DepositRefundRepository,
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
            merchant_apply: merchant_apply::MerchantApplyRepository::new(pool.clone()),
            deposit_account: deposit_account::DepositAccountRepository::new(pool.clone()),
            deposit_recharge: deposit_recharge::DepositRechargeRepository::new(pool.clone()),
            deposit_refund: deposit_refund::DepositRefundRepository::new(pool.clone()),
        }
    }
}
