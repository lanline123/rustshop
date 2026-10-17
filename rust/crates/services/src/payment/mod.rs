//! payment 领域服务。

pub mod balance_pay;
pub mod gateway;
pub mod paylog;
pub mod paylog_batch;
pub mod refund_apply;
pub mod tax_rate;
pub mod user_balance_log;

use std::sync::Arc;

/// payment 领域服务集合（由组合根装配一次，api 层只持有 `Arc<Services>`）。
#[derive(Clone)]
pub struct Services {
    pub balance_pay: Arc<balance_pay::BalancePayService>,
    pub paylog: Arc<paylog::PaylogService>,
    pub paylog_batch: Arc<paylog_batch::PaylogBatchService>,
    pub refund_apply: Arc<refund_apply::RefundApplyService>,
    pub tax_rate: Arc<tax_rate::TaxRateService>,
    pub user_balance_log: Arc<user_balance_log::UserBalanceLogService>,
}
