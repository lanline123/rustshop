//! 分销客户交易服务（marketing::salesman_customer_transaction）。
//!
//! 对应原 Java `SalesmanOrderServiceImpl#listCustomerTransaction`
//! （1 个管理端端点 `GET /adminapi/salesman/customerTransaction/list`）。
//!
//! # 语义（逐条对照 Java）
//! - `salesmanId` 必填（Java `CustomerTransactionPageQuery.salesmanId`）。
//! - 分页 `salesman_order JOIN order`，按 `order.add_time` 区间过滤，
//!   `order_id DESC`（Java Mapper XML 未显式排序，本实现固定倒序）。
//! - 回填分销员会员名与买家会员名（Java 分别查 `Salesman`/`User`）。
//! - `isExport` 忽略（差异 1）。
//!
//! # 金额
//! `total_amount` / `paid_amount` 为 `Decimal`。

use std::collections::HashMap;
use std::sync::Arc;

use tigshop_core::models::marketing::salesman_customer_transaction::{
    format_time, parse_time, CustomerTransactionQuery, CustomerTransactionVO, DEFAULT_PAGE_SIZE,
    MAX_PAGE_SIZE, MSG_SALESMAN_ID_REQUIRED,
};
use tigshop_core::types::{PageQuery, PageResult};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::marketing::salesman_customer_transaction::SalesmanCustomerTransactionRepository;
use tigshop_infrastructure::db::Repositories;

/// 客户交易服务（挂载于 `AdminState.marketing.salesman_customer_transaction`）。
pub struct SalesmanCustomerTransactionService {
    repos: Arc<Repositories>,
}

impl SalesmanCustomerTransactionService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    #[must_use]
    pub fn repo(&self) -> &SalesmanCustomerTransactionRepository {
        &self.repos.marketing.salesman_customer_transaction
    }

    /// 客户交易分页（Java `listCustomerTransaction`）。
    ///
    /// # Errors
    /// 缺 `salesmanId` → `Validation(salesmanId不能为空)`；数据库错误 → `Database`。
    pub async fn list(
        &self,
        q: &CustomerTransactionQuery,
    ) -> Result<PageResult<CustomerTransactionVO>> {
        let salesman_id = q
            .salesman_id
            .ok_or_else(|| Error::Validation(MSG_SALESMAN_ID_REQUIRED.into()))?;
        let page = PageQuery {
            page: q.page.unwrap_or(1),
            page_size: q.page_size.unwrap_or(DEFAULT_PAGE_SIZE).min(MAX_PAGE_SIZE),
        };
        let (limit, offset) = page.limit_offset();
        let start = parse_time(q.start_time.as_deref());
        let end = parse_time(q.end_time.as_deref());
        let (total, rows) = self
            .repo()
            .paginate(salesman_id, start, end, limit, offset)
            .await?;
        if rows.is_empty() {
            return Ok(PageResult::new(total, Vec::new(), &page));
        }
        let salesman_names: HashMap<i32, Option<String>> = self
            .repo()
            .salesman_names(&[salesman_id])
            .await?
            .into_iter()
            .collect();
        let buyer_ids: Vec<i32> = rows.iter().map(|r| r.user_id).collect();
        let usernames: HashMap<i32, Option<String>> = self
            .repo()
            .usernames(&buyer_ids)
            .await?
            .into_iter()
            .collect();
        let salesman_name = salesman_names.get(&salesman_id).cloned().flatten();
        let list: Vec<CustomerTransactionVO> = rows
            .iter()
            .map(|r| CustomerTransactionVO {
                salesman_id: r.salesman_id,
                salesman_name: salesman_name.clone(),
                order_id: r.order_id,
                order_sn: r.order_sn.clone(),
                user_id: r.user_id,
                username: usernames.get(&r.user_id).cloned().flatten(),
                add_time: format_time(r.add_time),
                total_amount: r.total_amount,
                paid_amount: r.paid_amount,
                order_status: r.order_status,
            })
            .collect();
        Ok(PageResult::new(total, list, &page))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn missing_salesman_id_is_validation_error() {
        let err = Error::Validation(MSG_SALESMAN_ID_REQUIRED.into());
        assert!(matches!(err, Error::Validation(_)));
        assert_eq!(MSG_SALESMAN_ID_REQUIRED, "salesmanId不能为空");
    }

    #[test]
    fn page_defaults_match_base_page() {
        let q = CustomerTransactionQuery::default();
        assert_eq!(q.page.unwrap_or(1), 1);
        assert_eq!(q.page_size.unwrap_or(DEFAULT_PAGE_SIZE), 15);
        assert_eq!(MAX_PAGE_SIZE, 100);
    }
}
