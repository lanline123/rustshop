//! trade 领域服务。

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

use std::sync::Arc;

/// trade 领域服务集合（由组合根装配一次，api 层只持有 `Arc<Services>`）。
#[derive(Clone)]
pub struct Services {
    pub aftersales: Arc<aftersales::AftersalesService>,
    pub aftersales_client: Arc<aftersales_client::AftersalesClientService>,
    pub cart: Arc<cart::CartService>,
    pub order: Arc<order::OrderService>,
    pub order_admin: Arc<order_admin::OrderAdminService>,
    pub proxy_shopping_order_check: Arc<proxy_shopping_order_check::ProxyShoppingOrderCheckService>,
    pub proxy_shopping_pickup_contact: Arc<proxy_shopping_pickup_contact::ProxyShoppingPickupContactService>,
    pub proxy_shopping_product: Arc<proxy_shopping_product::ProxyShoppingProductService>,
    pub proxy_shopping_user: Arc<proxy_shopping_user::ProxyShoppingUserService>,
    pub proxy_shopping_user_address: Arc<proxy_shopping_user_address::ProxyShoppingUserAddressService>,
    pub proxy_shopping_user_coupon: Arc<proxy_shopping_user_coupon::ProxyShoppingUserCouponService>,
}
