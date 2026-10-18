//! marketing 领域仓储。

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

use crate::db::DbPool;

#[derive(Clone)]
pub struct Repositories {
    pub buyer_show: buyer_show::BuyerShowRepository,
    pub buyer_show_client: buyer_show_client::BuyerShowClientRepository,
    pub buyer_show_comment: buyer_show_comment::BuyerShowCommentRepository,
    pub buyer_show_group: buyer_show_group::BuyerShowGroupRepository,
    pub coupon: coupon::CouponRepository,
    pub product_style_admin: product_style_admin::ProductStyleAdminRepository,
    pub salesman: salesman::SalesmanRepository,
    pub salesman_material_category: salesman_material_category::SalesmanMaterialCategoryRepository,
    pub seckill: seckill::SeckillRepository,
    pub time_discount: time_discount::TimeDiscountRepository,
    pub groupon_promotion: groupon_promotion::GrouponPromotionRepository,
    pub coupon_manage: coupon_manage::CouponManageRepository,
    pub salesman_content: salesman_content::SalesmanContentRepository,
    pub gift_card: gift_card::GiftCardRepository,
    pub gift_card_groups: gift_card_groups::GiftCardGroupsRepository,
}

impl Repositories {
    #[must_use]
    pub fn new(pool: DbPool) -> Self {
        Self {
            buyer_show: buyer_show::BuyerShowRepository::new(pool.clone()),
            buyer_show_client: buyer_show_client::BuyerShowClientRepository::new(pool.clone()),
            buyer_show_comment: buyer_show_comment::BuyerShowCommentRepository::new(pool.clone()),
            buyer_show_group: buyer_show_group::BuyerShowGroupRepository::new(pool.clone()),
            coupon: coupon::CouponRepository::new(pool.clone()),
            product_style_admin: product_style_admin::ProductStyleAdminRepository::new(pool.clone()),
            salesman: salesman::SalesmanRepository::new(pool.clone()),
            salesman_material_category: salesman_material_category::SalesmanMaterialCategoryRepository::new(pool.clone()),
            seckill: seckill::SeckillRepository::new(pool.clone()),
            time_discount: time_discount::TimeDiscountRepository::new(pool.clone()),
            groupon_promotion: groupon_promotion::GrouponPromotionRepository::new(pool.clone()),
            coupon_manage: coupon_manage::CouponManageRepository::new(pool.clone()),
            salesman_content: salesman_content::SalesmanContentRepository::new(pool.clone()),
            gift_card: gift_card::GiftCardRepository::new(pool.clone()),
            gift_card_groups: gift_card_groups::GiftCardGroupsRepository::new(pool.clone()),
        }
    }
}
