//! payment 领域仓储。

pub mod category_fee_rule;
pub mod merchant_application;
pub mod order_invoice;
pub mod paylog;
pub mod paylog_batch;
pub mod refund_apply;
pub mod tax_rate;
pub mod user_balance_log;

use crate::db::DbPool;

#[derive(Clone)]
pub struct Repositories {
    pub paylog: paylog::PaylogRepository,
    pub paylog_batch: paylog_batch::PaylogBatchRepository,
    pub refund_apply: refund_apply::RefundApplyRepository,
    pub tax_rate: tax_rate::TaxRateRepository,
    pub user_balance_log: user_balance_log::UserBalanceLogRepository,
    pub merchant_application: merchant_application::MerchantApplicationRepository,
    pub category_fee_rule: category_fee_rule::CategoryFeeRuleRepository,
    pub order_invoice: order_invoice::OrderInvoiceRepository,
}

impl Repositories {
    #[must_use]
    pub fn new(pool: DbPool) -> Self {
        Self {
            paylog: paylog::PaylogRepository::new(pool.clone()),
            paylog_batch: paylog_batch::PaylogBatchRepository::new(pool.clone()),
            refund_apply: refund_apply::RefundApplyRepository::new(pool.clone()),
            tax_rate: tax_rate::TaxRateRepository::new(pool.clone()),
            user_balance_log: user_balance_log::UserBalanceLogRepository::new(pool.clone()),
            merchant_application: merchant_application::MerchantApplicationRepository::new(pool.clone()),
            category_fee_rule: category_fee_rule::CategoryFeeRuleRepository::new(pool.clone()),
            order_invoice: order_invoice::OrderInvoiceRepository::new(pool.clone()),
        }
    }
}
