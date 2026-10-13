//! 业务服务层。
//!
//! 对应原 Java `tigshop-service` 模块：
//! - [`auth`]：登录/登出/令牌刷新（`AdminUserServiceImpl.login`）
//! - [`product`]：商品查询、上架状态、库存（`ProductServiceImpl`）
//! - [`cart`]：购物车增删改查与价格计算（`CartServiceImpl`）
//! - [`order`]：下单、状态流转、支付回调（`OrderServiceImpl`）
//! - [`payment`]：支付渠道抽象（微信/支付宝/PayPal...）
//! - [`user`]：会员注册/登录/资料（`UserServiceImpl`）
//! - [`seckill`]：限时秒杀（`SeckillServiceImpl`）
//! - [`time_discount`]：限时折扣（`TimeDiscountServiceImpl`）
//! - [`paylog`]：支付日志（`PaylogServiceImpl`）—— 支付单复用/分账快照/退款

pub mod auth;
pub mod cart;
pub mod catalog;
pub mod content;
pub mod coupon;
pub mod logistics;
pub mod member;
pub mod order;
pub mod payment;
pub mod paylog;
pub mod product;
pub mod seckill;
pub mod time_discount;
pub mod user;

use std::sync::Arc;

use tigshop_infrastructure::cache::CacheService;
use tigshop_infrastructure::db::Repositories;
use tigshop_security::JwtService;

/// 应用服务上下文：所有服务共享的依赖集合
/// （替代 Spring 的依赖注入容器）。
#[derive(Clone)]
pub struct AppState {
    pub repos: Arc<Repositories>,
    pub cache: Option<CacheService>,
    pub jwt: Arc<JwtService>,
}
