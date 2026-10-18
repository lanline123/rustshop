//! trade 领域仓储。

pub mod aftersales;
pub mod aftersales_client;
pub mod cart;
pub mod order;
pub mod order_admin;
pub mod proxy_shopping_order_check;
pub mod proxy_shopping_pickup_contact;
pub mod proxy_shopping_product;
pub mod proxy_shopping_user;
pub mod proxy_shopping_user_address;
pub mod proxy_shopping_user_coupon;
pub mod user_order;

use crate::db::DbPool;

#[derive(Clone)]
pub struct Repositories {
    pub aftersales: aftersales::AftersalesRepository,
    pub aftersales_client: aftersales_client::AftersalesClientRepository,
    pub cart: cart::CartRepository,
    pub order: order::OrderRepository,
    pub order_admin: order_admin::OrderAdminRepository,
    pub proxy_shopping_order_check: proxy_shopping_order_check::ProxyShoppingOrderCheckRepository,
    pub proxy_shopping_pickup_contact: proxy_shopping_pickup_contact::ProxyShoppingPickupContactRepository,
    pub proxy_shopping_product: proxy_shopping_product::ProxyShoppingProductRepository,
    pub proxy_shopping_user: proxy_shopping_user::ProxyShoppingUserRepository,
    pub proxy_shopping_user_address: proxy_shopping_user_address::ProxyShoppingUserAddressRepository,
    pub proxy_shopping_user_coupon: proxy_shopping_user_coupon::ProxyShoppingUserCouponRepository,
    pub user_order: user_order::UserOrderRepository,
}

impl Repositories {
    #[must_use]
    pub fn new(pool: DbPool) -> Self {
        Self {
            aftersales: aftersales::AftersalesRepository::new(pool.clone()),
            aftersales_client: aftersales_client::AftersalesClientRepository::new(pool.clone()),
            cart: cart::CartRepository::new(pool.clone()),
            order: order::OrderRepository::new(pool.clone()),
            order_admin: order_admin::OrderAdminRepository::new(pool.clone()),
            proxy_shopping_order_check: proxy_shopping_order_check::ProxyShoppingOrderCheckRepository::new(pool.clone()),
            proxy_shopping_pickup_contact: proxy_shopping_pickup_contact::ProxyShoppingPickupContactRepository::new(pool.clone()),
            proxy_shopping_product: proxy_shopping_product::ProxyShoppingProductRepository::new(pool.clone()),
            proxy_shopping_user: proxy_shopping_user::ProxyShoppingUserRepository::new(pool.clone()),
            proxy_shopping_user_address: proxy_shopping_user_address::ProxyShoppingUserAddressRepository::new(pool.clone()),
            proxy_shopping_user_coupon: proxy_shopping_user_coupon::ProxyShoppingUserCouponRepository::new(pool.clone()),
            user_order: user_order::UserOrderRepository::new(pool.clone()),
        }
    }
}
