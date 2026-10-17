//! 物流服务：运费模板/配送方式/物流公司。
//! 对应原 Java `ShippingTplServiceImpl` / `LogisticsCompanyServiceImpl` 等。
//!
//! TODO(域实现者)：对照 Java 源补全运费计算（按重量/件数、区域首续费用）。

use std::sync::Arc;

use rust_decimal::Decimal;
use tigshop_core::entities::{LogisticsCompany, ShippingTpl, ShippingType};
use tigshop_core::models::ShippingTplListQuery;
use tigshop_core::types::{PageQuery, PageResult};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::Repositories;

pub struct LogisticsService {
    repos: Arc<Repositories>,
}

impl LogisticsService {
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 运费模板分页。
    pub async fn tpl_list(&self, q: &ShippingTplListQuery) -> Result<PageResult<ShippingTpl>> {
        let page = PageQuery {
            page: q.page.unwrap_or(1),
            page_size: q.page_size.unwrap_or(20),
        };
        let (total, list) = self
            .repos.logistics.shipping_list
            .paginate_tpls(q.keyword.as_deref(), &page)
            .await?;
        Ok(PageResult::new(total, list, &page))
    }

    /// 运费模板详情。
    pub async fn tpl_detail(&self, tpl_id: i32) -> Result<ShippingTpl> {
        self.repos.logistics.shipping_list
            .find_tpl(tpl_id)
            .await?
            .ok_or_else(|| Error::NotFound("运费模板不存在".into()))
    }

    /// 启用的物流公司。
    pub async fn companies(&self) -> Result<Vec<LogisticsCompany>> {
        self.repos.logistics.shipping_list.find_enabled_companies().await
    }

    /// 配送方式。
    pub async fn shipping_types(&self) -> Result<Vec<ShippingType>> {
        self.repos.logistics.shipping_list.find_all_types().await
    }

    /// 计算运费（简化版：首件/首重 + 续件/续重）。
    ///
    /// TODO(域实现者)：接入 shipping_tpl_info 的区域明细与包邮门槛。
    pub fn calc_freight(
        first_price: Decimal,
        next_price: Decimal,
        first_qty: i32,
        next_qty: i32,
        quantity: i32,
    ) -> Decimal {
        if quantity <= first_qty {
            return first_price;
        }
        let extra = quantity - first_qty;
        let extra_units = extra.div_euclid(next_qty.max(1))
            + i32::from(extra.rem_euclid(next_qty.max(1)) != 0);
        first_price + next_price * Decimal::from(extra_units)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::str::FromStr;

    /// 金额字面量解析（`from_scientific` 只接受科学计数法，此处用常规解析）。
    fn money(s: &str) -> Decimal {
        Decimal::from_str(s).expect("测试金额字面量应合法")
    }

    #[test]
    fn freight_calc_first_tier_only() {
        // 首 1 件 10 元，续每 2 件 5 元；共 1 件 => 仅首件费
        assert_eq!(
            LogisticsService::calc_freight(money("10"), money("5"), 1, 2, 1),
            Decimal::from(10)
        );
    }

    #[test]
    fn freight_calc_rounds_extra_units_up() {
        // 共 5 件 => 10 + ceil((5-1)/2)*5 = 10 + 2*5 = 20
        assert_eq!(
            LogisticsService::calc_freight(money("10"), money("5"), 1, 2, 5),
            Decimal::from(20)
        );
        // 共 2 件 => 10 + ceil(1/2)*5 = 15（不足一档也按一档计）
        assert_eq!(
            LogisticsService::calc_freight(money("10"), money("5"), 1, 2, 2),
            Decimal::from(15)
        );
    }

    #[test]
    fn freight_calc_exact_multiple_no_extra_tier() {
        // 共 3 件 => 10 + ceil(2/2)*5 = 15（正好整除，不多计一档）
        assert_eq!(
            LogisticsService::calc_freight(money("10"), money("5"), 1, 2, 3),
            Decimal::from(15)
        );
    }

    #[test]
    fn freight_calc_zero_quantity_returns_first_price() {
        assert_eq!(
            LogisticsService::calc_freight(money("8.50"), money("3"), 1, 1, 0),
            money("8.50")
        );
    }

    #[test]
    fn freight_calc_guards_zero_next_qty() {
        // next_qty = 0 时不应除零 panic（内部以 max(1) 兜底）
        let fee = LogisticsService::calc_freight(money("10"), money("5"), 1, 0, 4);
        assert!(fee > money("10"));
    }
}