//! payment 领域仓储。

pub mod category_fee_rule;
pub mod credit_settlement;
pub mod credit_user;
pub mod merchant_application;
pub mod order_invoice;
pub mod paylog;
pub mod paylog_batch;
pub mod refund_apply;
pub mod refund_log;
pub mod tax_rate;
pub mod user_balance_log;
pub mod user_invoice;
pub mod with_draw_apply_client;

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
    pub with_draw_apply_client: with_draw_apply_client::WithDrawApplyClientRepository,
    pub refund_log: refund_log::RefundLogRepository,
    pub credit_settlement: credit_settlement::CreditSettlementRepository,
    pub user_invoice: user_invoice::UserInvoiceRepository,
    pub credit_user: credit_user::CreditUserRepository,
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
            with_draw_apply_client: with_draw_apply_client::WithDrawApplyClientRepository::new(pool.clone()),
            refund_log: refund_log::RefundLogRepository::new(pool.clone()),
            credit_settlement: credit_settlement::CreditSettlementRepository::new(pool.clone()),
            user_invoice: user_invoice::UserInvoiceRepository::new(pool.clone()),
            credit_user: credit_user::CreditUserRepository::new(pool.clone()),
        }
    }
}
