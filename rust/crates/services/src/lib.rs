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
//! - [`refund_apply`]：退款申请（`RefundApplyServiceImpl`）—— 额度校验/审核流
//! - [`balance_pay`]：余额支付 —— 首个真实支付渠道（抵扣上限/原子扣款/幂等结算）

pub mod auth;
pub mod balance_pay;
pub mod cart;
pub mod catalog;
pub mod content;
pub mod coupon;
pub mod decorate;
pub mod logistics;
pub mod member;
pub mod order;
pub mod payment;
pub mod paylog;
pub mod product;
pub mod refund_apply;
pub mod salesman;
pub mod seckill;
pub mod shop;
pub mod slice044;
pub mod slice039;
pub mod slice210;
pub mod slice207;
pub mod slice163;
pub mod slice157;
pub mod slice137;
pub mod slice108;
pub mod slice085;
pub mod slice038;
pub mod slice037;
pub mod slice220;
pub mod slice209;
pub mod slice201;
pub mod slice185;
pub mod slice092;
pub mod slice081;
pub mod slice172;
pub mod slice166;
pub mod slice144;
pub mod slice139;
pub mod slice091;
pub mod slice075;
pub mod slice217;
pub mod slice205;
pub mod slice192;
pub mod slice186;
pub mod slice170;
pub mod slice120;
pub mod slice093;
pub mod slice082;
pub mod slice056;
pub mod slice216;
pub mod slice214;
pub mod slice213;
pub mod slice208;
pub mod slice206;
pub mod slice203;
pub mod slice200;
pub mod slice199;
pub mod slice197;
pub mod slice193;
pub mod slice191;
pub mod slice190;
pub mod slice189;
pub mod slice181;
pub mod slice176;
pub mod slice174;
pub mod slice171;
pub mod slice168;
pub mod slice160;
pub mod slice148;
pub mod slice136;
pub mod slice135;
pub mod slice133;
pub mod slice130;
pub mod slice124;
pub mod slice116;
pub mod slice114;
pub mod slice113;
pub mod slice102;
pub mod slice098;
pub mod slice084;
pub mod slice073;
pub mod slice066;
pub mod slice065;
pub mod slice064;
pub mod slice063;
pub mod slice059;
pub mod slice051;
pub mod slice047;
pub mod slice036;
pub mod slice034;
pub mod slice164;
pub mod slice147;
pub mod slice143;
pub mod slice141;
pub mod slice132;
pub mod slice118;
pub mod slice117;
pub mod slice115;
pub mod slice111;
pub mod slice106;
pub mod slice094;
pub mod slice090;
pub mod slice053;
pub mod slice100;
pub mod slice042;
pub mod slice032;
pub mod slice031;
pub mod slice030;
pub mod slice029;
pub mod slice028;
pub mod slice027;
pub mod slice188;
pub mod slice187;
pub mod slice026;
pub mod slice156;
pub mod slice140;
pub mod slice134;
pub mod slice097;
pub mod slice095;
pub mod slice025;
pub mod slice024;
pub mod slice023;
pub mod slice022;
pub mod slice021;
pub mod slice020;
pub mod slice019;
pub mod slice018;
pub mod slice017;
pub mod slice016;
pub mod slice015;
pub mod slice001;
pub mod slice014;
pub mod slice013;
pub mod slice012;
pub mod slice011;
pub mod slice010;
pub mod slice009;
pub mod slice008;
pub mod slice007;
pub mod slice006;
pub mod slice004;
pub mod slice003;
pub mod slice002;
pub mod slice005;
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
