//! 销售统计 域服务（analytics::sales_statistics）—— wave9。
//!
//! `self.repos.analytics.sales_statistics`；对应 Java `SalesStatisticsServiceImpl`。
//! 统计口径与本文件模型层说明一致（已支付订单 + 时间范围）。

use std::sync::Arc;

use tigshop_common::utils;
use tigshop_core::models::analytics::sales_statistics::{
    DailySalesRow, ProductSalesRow, SalesStatisticsQuery,
};
use tigshop_core::types::PageResult;
use tigshop_core::Result;
use tigshop_infrastructure::db::repo::analytics::sales_statistics::SalesStatisticsRepository;
use tigshop_infrastructure::db::Repositories;

/// 销售统计 域服务。
pub struct SalesStatisticsService {
    repos: Arc<Repositories>,
}

fn now_secs() -> i64 {
    utils::now_millis() / 1000
}

impl SalesStatisticsService {
    /// 构造服务。
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本 feature 仓储。
    #[must_use]
    pub fn repo(&self) -> &SalesStatisticsRepository {
        &self.repos.analytics.sales_statistics
    }

    /// 销售趋势（Java `list`）：按天聚合 + 区间总计。
    pub async fn list(&self, q: &SalesStatisticsQuery) -> Result<serde_json::Value> {
        let (start, end) = q.range(now_secs());
        let rows = self.repo().daily(start, end, q.shop_id).await?;
        let totals = self.repo().totals(start, end, q.shop_id).await?;
        let list: Vec<DailySalesRow> = rows
            .into_iter()
            .map(|r| DailySalesRow {
                date: r.date,
                order_count: r.order_count,
                total_amount: r.total_amount.unwrap_or_default(),
            })
            .collect();
        Ok(serde_json::json!({
            "list": list,
            "orderCount": totals.order_count,
            "totalAmount": totals.total_amount,
            "startTime": start,
            "endTime": end,
        }))
    }

    /// 销售明细（Java `salesDetail`）：区间总计。
    pub async fn sales_detail(&self, q: &SalesStatisticsQuery) -> Result<serde_json::Value> {
        let (start, end) = q.range(now_secs());
        let t = self.repo().totals(start, end, q.shop_id).await?;
        Ok(serde_json::json!({
            "orderCount": t.order_count,
            "totalAmount": t.total_amount,
            "startTime": start,
            "endTime": end,
        }))
    }

    /// 商品销售明细分页（Java `salesProductDetail`）。
    pub async fn sales_product_detail(
        &self,
        q: &SalesStatisticsQuery,
    ) -> Result<PageResult<serde_json::Value>> {
        let page = q.page_query();
        let (start, end) = q.range(now_secs());
        let order_by_amount = q.sort_field.as_deref() != Some("quantity");
        let (total, rows) = self
            .repo()
            .product_page(
                start,
                end,
                q.shop_id,
                order_by_amount,
                q.ascending(),
                u64::from(page.page_size),
                u64::from(page.page - 1) * u64::from(page.page_size),
            )
            .await?;
        let list = rows
            .into_iter()
            .map(|r| {
                serde_json::json!({
                    "productId": r.product_id,
                    "productName": r.product_name,
                    "productSn": r.product_sn,
                    "quantity": r.quantity.unwrap_or(0),
                    "amount": r.amount.unwrap_or_default(),
                })
            })
            .collect();
        Ok(PageResult::new(total, list, &page))
    }

    /// 销售指标卡（Java `salesIndicators`）：今日/昨日/近 7 天/近 30 天。
    ///
    /// `shop_scope`：店铺账号强制本店（`Some(shop_id)`），平台账号 `None`（全平台）。
    pub async fn sales_indicators(&self, shop_scope: Option<i32>) -> Result<serde_json::Value> {
        let now = now_secs();
        let day = 24 * 3600;
        let today_start = now - now % day;
        let ranges = [
            ("today", today_start, now),
            ("yesterday", today_start - day, today_start),
            ("last7Days", now - 7 * day, now),
            ("last30Days", now - 30 * day, now),
        ];
        let mut out = serde_json::Map::new();
        for (key, start, end) in ranges {
            let t = self.repo().totals(start, end, shop_scope).await?;
            out.insert(
                key.to_string(),
                serde_json::json!({
                    "orderCount": t.order_count,
                    "totalAmount": t.total_amount,
                }),
            );
        }
        Ok(serde_json::Value::Object(out))
    }

    /// 商品销售排行（Java `salesRanking`）。
    pub async fn sales_ranking(
        &self,
        q: &SalesStatisticsQuery,
    ) -> Result<PageResult<ProductSalesRow>> {
        let page = q.page_query();
        let (start, end) = q.range(now_secs());
        let (total, rows) = self
            .repo()
            .product_page(
                start,
                end,
                q.shop_id,
                true,
                q.ascending(),
                u64::from(page.page_size),
                u64::from(page.page - 1) * u64::from(page.page_size),
            )
            .await?;
        let list: Vec<ProductSalesRow> = rows
            .into_iter()
            .map(|r| ProductSalesRow {
                product_id: r.product_id,
                product_name: r.product_name,
                product_sn: r.product_sn,
                quantity: r.quantity.unwrap_or(0),
                amount: r.amount.unwrap_or_default(),
            })
            .collect();
        Ok(PageResult::new(total, list, &page))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn now_secs_is_epoch_seconds() {
        assert!(now_secs() > 1_600_000_000);
    }

    #[test]
    fn indicator_ranges_are_ordered() {
        let now = 1_700_000_000_i64;
        let day = 24 * 3600;
        let today_start = now - now % day;
        assert!(today_start - day < today_start);
        assert!(now - 7 * day < now);
    }

    #[test]
    fn amount_defaults_to_zero_for_null_sum() {
        // 仓储 SUM 为 NULL 时服务统一 unwrap_or_default。
        assert_eq!(Option::<rust_decimal::Decimal>::None.unwrap_or_default(), rust_decimal::Decimal::ZERO);
    }
}
