//! payment 领域服务。

pub mod balance_pay;
pub mod category_fee_rule;
pub mod credit_settlement;
pub mod credit_user;
pub mod gateway;
pub mod merchant_application;
pub mod order_invoice;
pub mod pay;
pub mod paylog;
pub mod paylog_batch;
pub mod refund_apply;
pub mod refund_log;
pub mod tax_rate;
pub mod user_balance_log;
pub mod user_invoice;
pub mod user_recharge_order;
pub mod user_withdraw_apply;
pub mod with_draw_apply_client;

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
    pub merchant_application: Arc<merchant_application::MerchantApplicationService>,
    pub category_fee_rule: Arc<category_fee_rule::CategoryFeeRuleService>,
    pub order_invoice: Arc<order_invoice::OrderInvoiceService>,
    pub with_draw_apply_client: Arc<with_draw_apply_client::WithDrawApplyClientService>,
    pub refund_log: Arc<refund_log::RefundLogService>,
    pub credit_settlement: Arc<credit_settlement::CreditSettlementService>,
    pub user_invoice: Arc<user_invoice::UserInvoiceService>,
    pub credit_user: Arc<credit_user::CreditUserService>,
    pub user_recharge_order: Arc<user_recharge_order::UserRechargeOrderService>,
    pub user_withdraw_apply: Arc<user_withdraw_apply::UserWithdrawApplyService>,
    pub pay: Arc<pay::PayService>,
}
