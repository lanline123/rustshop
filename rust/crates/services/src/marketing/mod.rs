//! marketing 领域服务。

pub mod buyer_show;
pub mod buyer_show_client;
pub mod buyer_show_comment;
pub mod buyer_show_group;
pub mod coupon;
pub mod coupon_manage;
pub mod gift_card;
pub mod gift_card_groups;
pub mod groupon_promotion;
pub mod product_style_admin;
pub mod salesman;
pub mod salesman_content;
pub mod salesman_material_category;
pub mod seckill;
pub mod time_discount;

use std::sync::Arc;

/// marketing 领域服务集合（由组合根装配一次，api 层只持有 `Arc<Services>`）。
#[derive(Clone)]
pub struct Services {
    pub buyer_show: Arc<buyer_show::BuyerShowService>,
    pub buyer_show_client: Arc<buyer_show_client::BuyerShowClientService>,
    pub buyer_show_comment: Arc<buyer_show_comment::BuyerShowCommentService>,
    pub buyer_show_group: Arc<buyer_show_group::BuyerShowGroupService>,
    pub coupon: Arc<coupon::CouponService>,
    pub product_style_admin: Arc<product_style_admin::ProductStyleAdminService>,
    pub salesman: Arc<salesman::SalesmanService>,
    pub salesman_material_category: Arc<salesman_material_category::SalesmanMaterialCategoryService>,
    pub seckill: Arc<seckill::SeckillService>,
    pub time_discount: Arc<time_discount::TimeDiscountService>,
    pub groupon_promotion: Arc<groupon_promotion::GrouponPromotionService>,
    pub coupon_manage: Arc<coupon_manage::CouponManageService>,
    pub salesman_content: Arc<salesman_content::SalesmanContentService>,
    pub gift_card: Arc<gift_card::GiftCardService>,
    pub gift_card_groups: Arc<gift_card_groups::GiftCardGroupsService>,
}
