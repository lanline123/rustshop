//! 业务模型 / DTO / 查询参数。
//!
//! 对应原 Java `tigshop-bean` 下的 `dto`、`param`、`query`、`vo` 包。
//! 命名保持与原接口 JSON 字段一致（camelCase，通过 serde rename 标注）。

pub mod auth;
pub mod catalog;
pub mod content;
pub mod coupon;
pub mod logistics;
pub mod member;
pub mod order;
pub mod paylog;
pub mod product;
pub mod promotion;
pub mod time_discount;

pub use auth::{LoginDTO, LoginVO};
pub use catalog::{BrandVO, CategoryTreeNode};
pub use content::{
    ArticleCategoryTreeNode, ArticleCategoryVO, ArticleDetailVO, ArticleListQuery, ArticleVO,
};
pub use coupon::{
    CouponClientVO, CouponListQuery, CouponStatusCountVO, CouponVO, UserCouponClaimDTO,
    UserCouponIdDTO, UserCouponListQuery, UserCouponStatus, UserCouponVO,
};
pub use logistics::{ShippingTplListQuery, ShippingTplVO};
pub use member::{UserPointsLogQuery, UserRankVO};
pub use order::{OrderCreateDTO, OrderCreateVO, OrderVO};
pub use paylog::{
    PayOrderType, PayStatus, PaylogCreateDTO, PaylogListQuery, PaylogRefundVO, PaylogUpdateDTO,
    PaylogVO, RefundStatus,
};
pub use product::{ProductListQuery, ProductVO};
pub use promotion::{
    SeckillCreateDTO, SeckillItemDTO, SeckillItemVO, SeckillListQuery, SeckillPromotionVO,
    SeckillStatus, SeckillUpdateDTO, SeckillVO,
};
pub use time_discount::{
    DiscountType, PromotionTimeStatus, TimeDiscountCreateDTO, TimeDiscountDetailVO,
    TimeDiscountItemDTO, TimeDiscountItemVO, TimeDiscountListQuery, TimeDiscountPromotionVO,
    TimeDiscountUpdateDTO,
};
