//! 仓储层：替代原 Java MyBatis Plus Mapper。
//!
//! 每个仓储方法返回 `tigshop_core::Result<T>`，SQL 错误统一映射为
//! `Error::Database`。

pub mod admin_user;
pub mod cart;
pub mod catalog;
pub mod content;
pub mod coupon;
pub mod logistics;
pub mod member;
pub mod order;
pub mod paylog;
pub mod product;
pub mod refund_apply;
pub mod seckill;
pub mod time_discount;
pub mod user;
