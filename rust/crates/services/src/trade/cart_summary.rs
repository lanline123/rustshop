//! 购物车数量/优惠券试算域服务（trade::cart_summary）。
//!
//! - `get_count`：完整 DB 实现（`SUM(quantity)`，排除代客下单购物车）。
//! - `get_coupon_discount`：保留端点与 Java `@NotNull` 校验后返回明确「未迁移」业务错误
//!   （优惠券/促销计算引擎未迁移），不编造金额。

use std::sync::Arc;

use tigshop_core::models::trade::cart_summary::{
    validate_coupon_id, CartCouponDiscountVO, MSG_COUPON_NOT_MIGRATED,
};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::trade::cart_summary::CartSummaryRepository;
use tigshop_infrastructure::db::Repositories;

/// 购物车汇总服务。
pub struct CartSummaryService {
    repos: Arc<Repositories>,
}

impl CartSummaryService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本域仓储（NESTED：`self.repos.trade.cart_summary`）。
    #[must_use]
    pub fn repo(&self) -> &CartSummaryRepository {
        &self.repos.trade.cart_summary
    }

    /// 购物车数量（Java `getCount`）。
    pub async fn get_count(&self, user_id: i32, shop_id: Option<i32>) -> Result<i32> {
        let total = self.repo().sum_quantity(user_id, shop_id).await?;
        Ok(i32::try_from(total).unwrap_or(i32::MAX))
    }

    /// 优惠券试算（未迁移：明确业务错误）。
    pub async fn get_coupon_discount(
        &self,
        _user_id: i32,
        coupon_id: Option<i32>,
    ) -> Result<CartCouponDiscountVO> {
        validate_coupon_id(coupon_id).map_err(Error::Validation)?;
        Err(Error::biz_msg(MSG_COUPON_NOT_MIGRATED))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn coupon_id_required_before_not_migrated() {
        assert!(validate_coupon_id(None).is_err());
        assert!(MSG_COUPON_NOT_MIGRATED.contains("尚未迁移"));
    }
}
