//! 业务模型 / DTO / 查询参数。
//!
//! 对应原 Java `tigshop-bean` 下的 `dto`、`param`、`query`、`vo` 包。
//! 命名保持与原接口 JSON 字段一致（camelCase，通过 serde rename 标注）。



pub use identity::auth::{LoginDTO, LoginVO};
pub use catalog::category_all::{
    BatchDTO, BrandVO, CategoryDTO, CategoryListQuery, CategoryTreeNode, MoveCatDTO, OperateDTO,
    UpdateFieldDTO,
};
pub use content::article::{
    ArticleCategoryTreeNode, ArticleCategoryVO, ArticleDetailVO, ArticleListQuery, ArticleVO,
};
pub use marketing::coupon::{
    CouponClientVO, CouponListQuery, CouponStatusCountVO, CouponVO, UserCouponClaimDTO,
    UserCouponIdDTO, UserCouponListQuery, UserCouponStatus, UserCouponVO,
};
pub use content::decorate::{DecorateListQuery, DecorateVO};
pub use logistics::shipping_list::{ShippingTplListQuery, ShippingTplVO};
pub use member::member_lists::{UserPointsLogQuery, UserRankVO};
pub use trade::order::{OrderCreateDTO, OrderCreateVO, OrderVO};
pub use payment::paylog::{
    PayOrderType, PayStatus, PaylogCreateDTO, PaylogListQuery, PaylogRefundVO, PaylogUpdateDTO,
    PaylogVO, RefundStatus,
};
pub use catalog::product::{ProductListQuery, ProductVO};
pub use marketing::seckill::{
    SeckillCreateDTO, SeckillItemDTO, SeckillItemVO, SeckillListQuery, SeckillPromotionVO,
    SeckillStatus, SeckillUpdateDTO, SeckillVO,
};
pub use payment::refund_apply::{
    OfflineAuditDTO, RefundApplyAuditDTO, RefundApplyCreateDTO, RefundApplyDetailVO,
    RefundApplyListQuery, RefundApplyStatus, RefundApplyUpdateDTO, RefundApplyVO, RefundLogVO,
    RefundType,
};
pub use marketing::salesman::{SalesmanListQuery, SalesmanVO};
pub use merchant::shop::{ShopListQuery, ShopVO};
pub use marketing::time_discount::{
    DiscountType, PromotionTimeStatus, TimeDiscountCreateDTO, TimeDiscountDetailVO,
    TimeDiscountItemDTO, TimeDiscountItemVO, TimeDiscountListQuery, TimeDiscountPromotionVO,
    TimeDiscountUpdateDTO,
};

pub mod identity;
pub mod merchant;
pub mod wholesale;
pub mod catalog;
pub mod trade;
pub mod payment;
pub mod marketing;
pub mod member;
pub mod logistics;
pub mod content;
pub mod system;
pub mod im;
pub mod analytics;
pub mod openapi;
