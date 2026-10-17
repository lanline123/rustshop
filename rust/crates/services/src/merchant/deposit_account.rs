//! 保证金账户域服务（merchant::deposit_account）。
//!
//! 对应 Java `DepositAccountAdminServiceImpl`（账户总览/详情/流水/调整记录/手动调整/扣除/预警）。
//!
//! # 已实现
//! `list`（分页）/`detail`/`logList`/`adjustLogList`/`adjust`（写调整记录 + 应缴快照）/
//! `deduct`（写平台扣除流水 + 扣减实缴）。
//!
//! # 未迁移
//! - `sendWarning`：站内信/短信通道（message 域）未迁移 → 明确 1001 业务错误。
//! - `runDueCheck`：`DepositDueCheckTask` + `DepositLifecycleService`（到期扫描/惩罚下架/
//!   恢复）未迁移 → 空操作成功（运维端点）。
//! - `depositStatus` 的「冻结(3)」依赖 `penalty_status`；本实现按该字段推断，
//!   列表的 `depositStatus` 过滤在内存中完成（Java 为 SQL 条件）。

use std::sync::Arc;

use rust_decimal::Decimal;
use tigshop_common::utils::now_seconds;
use tigshop_core::models::merchant::deposit_account::{
    available_amount, compute_deposit_status, deposit_status_text, log_type_text, money,
    owner_type_text, pending_amount, subject_type_text, DepositAccountDetailVO, DepositAccountListQuery,
    DepositAccountVO, DepositAdjustDTO, DepositDeductDTO, DepositLogVO, DepositSendWarningDTO,
    ADJUST_TYPE_INVALID, DEDUCT_AMOUNT_INVALID, OWNER_ID_INVALID, OWNER_SHOP, PARAM_INVALID,
    REQUIRED_MANUAL_INVALID, SERVICE_DATA_ERROR, SHOP_NOT_EXIST, VENDOR_NOT_EXIST,
};
use tigshop_core::types::{PageQuery, PageResult};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::merchant::deposit_account::{
    AccountRow, DepositAccountRepository,
};
use tigshop_infrastructure::db::Repositories;

/// 保证金账户域服务。
pub struct DepositAccountService {
    repos: Arc<Repositories>,
}

impl DepositAccountService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    #[must_use]
    pub fn repo(&self) -> &DepositAccountRepository {
        &self.repos.merchant.deposit_account
    }

    /// `GET /adminapi/deposit/account/list`。
    pub async fn list(&self, q: &DepositAccountListQuery) -> Result<PageResult<DepositAccountVO>> {
        let (page, size) = q.page_params();
        let (total, rows) = self.repo().paginate(q).await?;
        let mut list = Vec::with_capacity(rows.len());
        for row in &rows {
            let mut vo = self.account_vo(row).await?;
            let status = compute_deposit_status(required(row), row.paid, row.penalty_status);
            vo.deposit_status = status;
            vo.deposit_status_text = deposit_status_text(status).to_string();
            if let Some(filter) = q.deposit_status {
                if filter != status {
                    continue;
                }
            }
            list.push(vo);
        }
        let pq = PageQuery {
            page,
            page_size: size,
        };
        Ok(PageResult::new(total, list, &pq))
    }

    /// `GET /adminapi/deposit/account/detail?ownerType=&ownerId=|=shopId`。
    pub async fn detail(
        &self,
        owner_type: Option<i32>,
        owner_id: Option<i32>,
    ) -> Result<DepositAccountDetailVO> {
        let (owner_type, owner_id) = self.resolve_owner(owner_type, owner_id, false).await?;
        let row = self
            .repo()
            .find_by_owner(owner_type, owner_id)
            .await?
            .ok_or_else(|| Error::biz(SERVICE_DATA_ERROR, PARAM_INVALID))?;
        let required = required(&row);
        let status = compute_deposit_status(required, row.paid, row.penalty_status);
        let frozen = std::cmp::min(row.paid, required);
        Ok(DepositAccountDetailVO {
            owner_type,
            owner_type_text: owner_type_text(owner_type).into(),
            owner_id,
            owner_name: self.owner_name(owner_type, owner_id).await?,
            shop_id: (owner_type == OWNER_SHOP).then_some(owner_id),
            shop_title: if owner_type == OWNER_SHOP {
                self.repo().owner_name(1, owner_id).await?
            } else {
                None
            },
            subject_type: 0,
            subject_type_text: subject_type_text(0).into(),
            deposit_status: status,
            deposit_status_text: deposit_status_text(status).into(),
            required: required.to_string(),
            paid: row.paid.to_string(),
            frozen: frozen.to_string(),
            available: available_amount(row.paid, required).to_string(),
            pending: pending_amount(row.paid, required).to_string(),
        })
    }

    /// `GET /adminapi/deposit/account/logList`。
    pub async fn log_list(
        &self,
        owner_type: Option<i32>,
        owner_id: Option<i32>,
        page: Option<u64>,
        size: Option<u64>,
    ) -> Result<PageResult<DepositLogVO>> {
        let (owner_type, owner_id) = self.resolve_owner(owner_type, owner_id, false).await?;
        let page = page.unwrap_or(1).max(1);
        let size = size.unwrap_or(15).clamp(1, 100);
        let (total, rows) = self
            .repo()
            .paginate_logs(owner_type, owner_id, page, size)
            .await?;
        let owner_name = self.owner_name(owner_type, owner_id).await?;
        let list: Vec<DepositLogVO> = rows
            .iter()
            .map(|r| DepositLogVO {
                shop_deposit_log_id: r.shop_deposit_log_id,
                owner_type: r.owner_type,
                owner_type_text: owner_type_text(r.owner_type).into(),
                owner_id: r.owner_id,
                owner_name: owner_name.clone(),
                r#type: r.r#type,
                type_text: log_type_text(r.r#type).into(),
                change_amount: r.change_amount.to_string(),
                before_paid: r.before_paid.to_string(),
                after_paid: r.after_paid.to_string(),
                related_id: r.related_id,
                order_id: r.order_id,
                admin_id: r.admin_id,
                remark: r.remark.clone(),
                add_time: r.add_time.to_string(),
            })
            .collect();
        Ok(PageResult::new(
            total,
            list,
            &PageQuery {
                page,
                page_size: size,
            },
        ))
    }

    /// `GET /adminapi/deposit/account/adjustLogList`。
    pub async fn adjust_log_list(
        &self,
        owner_type: Option<i32>,
        owner_id: Option<i32>,
        page: Option<u64>,
        size: Option<u64>,
    ) -> Result<PageResult<tigshop_core::models::merchant::deposit_account::DepositAdjustLogVO>> {
        use tigshop_core::models::merchant::deposit_account::{adjust_source_text, DepositAdjustLogVO};
        let (owner_type, owner_id) = self.resolve_owner(owner_type, owner_id, false).await?;
        let page = page.unwrap_or(1).max(1);
        let size = size.unwrap_or(15).clamp(1, 100);
        let (total, rows) = self
            .repo()
            .paginate_adjust_logs(owner_type, owner_id, page, size)
            .await?;
        let owner_name = self.owner_name(owner_type, owner_id).await?;
        let mut list = Vec::with_capacity(rows.len());
        for r in &rows {
            list.push(DepositAdjustLogVO {
                shop_deposit_adjust_id: r.shop_deposit_adjust_id,
                owner_type: r.owner_type,
                owner_type_text: owner_type_text(r.owner_type).into(),
                owner_id: r.owner_id,
                owner_name: owner_name.clone(),
                shop_id: (r.owner_type == OWNER_SHOP).then_some(r.owner_id),
                before_required: r.before_required.to_string(),
                after_required: r.after_required.to_string(),
                source: r.source,
                source_text: adjust_source_text(r.source).into(),
                admin_id: r.admin_id,
                admin_name: self.repo().admin_name(r.admin_id).await?,
                reason: r.reason.clone(),
                add_time: r.add_time.to_string(),
            });
        }
        Ok(PageResult::new(
            total,
            list,
            &PageQuery {
                page,
                page_size: size,
            },
        ))
    }

    /// `POST /adminapi/deposit/account/adjust`：手动调整应缴。
    pub async fn adjust(&self, dto: &DepositAdjustDTO, admin_id: i32) -> Result<()> {
        let (owner_type, owner_id) = self.resolve_owner(dto.owner_type, dto.resolved_owner_id(), true).await?;
        let adjust_type = dto.adjust_type.unwrap_or(2);
        if adjust_type != 1 && adjust_type != 2 {
            return Err(Error::biz(SERVICE_DATA_ERROR, ADJUST_TYPE_INVALID));
        }
        if adjust_type == 2 {
            match dto.required_manual {
                Some(v) if v >= Decimal::ZERO => {}
                _ => return Err(Error::biz(SERVICE_DATA_ERROR, REQUIRED_MANUAL_INVALID)),
            }
        }
        let now = i32::try_from(now_seconds()).unwrap_or(0);
        self.repo()
            .apply_adjust(dto, owner_type, owner_id, admin_id, now)
            .await?;
        Ok(())
    }

    /// `POST /adminapi/deposit/account/deduct`：赔付扣除。
    pub async fn deduct(&self, dto: &DepositDeductDTO, admin_id: i32) -> Result<()> {
        let (owner_type, owner_id) = self.resolve_owner(dto.owner_type, dto.resolved_owner_id(), true).await?;
        match dto.amount {
            Some(v) if v > Decimal::ZERO => {}
            _ => return Err(Error::biz(SERVICE_DATA_ERROR, DEDUCT_AMOUNT_INVALID)),
        }
        let now = i32::try_from(now_seconds()).unwrap_or(0);
        if !self
            .repo()
            .apply_deduct(dto, owner_type, owner_id, admin_id, now)
            .await?
        {
            return Err(Error::biz(
                SERVICE_DATA_ERROR,
                if owner_type == OWNER_SHOP {
                    SHOP_NOT_EXIST
                } else {
                    VENDOR_NOT_EXIST
                },
            ));
        }
        Ok(())
    }

    /// `POST /adminapi/deposit/account/sendWarning`（**未迁移**：站内信/短信通道）。
    pub fn send_warning(&self, _dto: &DepositSendWarningDTO) -> Result<()> {
        Err(Error::biz(
            SERVICE_DATA_ERROR,
            "保证金预警发送未迁移：依赖站内信/短信/推送通道（message 域）",
        ))
    }

    /// `GET /adminapi/deposit/account/runDueCheck`（**未迁移**：生命周期扫描，空操作成功）。
    pub fn run_due_check(&self) -> Result<()> {
        tracing::info!("deposit runDueCheck 被调用：生命周期扫描（DepositDueCheckTask）未迁移，本次为空操作");
        Ok(())
    }

    async fn resolve_owner(
        &self,
        owner_type: Option<i32>,
        owner_id: Option<i32>,
        require_exists: bool,
    ) -> Result<(i32, i32)> {
        let owner_type = owner_type.unwrap_or(OWNER_SHOP);
        if owner_type != OWNER_SHOP && owner_type != 2 {
            return Err(Error::biz(SERVICE_DATA_ERROR, PARAM_INVALID));
        }
        let owner_id = owner_id.ok_or_else(|| Error::biz(SERVICE_DATA_ERROR, OWNER_ID_INVALID))?;
        if owner_id <= 0 {
            return Err(Error::biz(SERVICE_DATA_ERROR, OWNER_ID_INVALID));
        }
        if require_exists && !self.repo().owner_exists(owner_type, owner_id).await? {
            return Err(Error::biz(
                SERVICE_DATA_ERROR,
                if owner_type == OWNER_SHOP {
                    SHOP_NOT_EXIST
                } else {
                    VENDOR_NOT_EXIST
                },
            ));
        }
        Ok((owner_type, owner_id))
    }

    async fn owner_name(&self, owner_type: i32, owner_id: i32) -> Result<String> {
        Ok(self
            .repo()
            .owner_name(owner_type, owner_id)
            .await?
            .unwrap_or_default())
    }

    async fn account_vo(&self, row: &AccountRow) -> Result<DepositAccountVO> {
        let required = required(row);
        let status = compute_deposit_status(required, row.paid, row.penalty_status);
        Ok(DepositAccountVO {
            owner_type: row.owner_type,
            owner_type_text: owner_type_text(row.owner_type).into(),
            subject_type: 0,
            subject_type_text: subject_type_text(0).into(),
            owner_id: row.owner_id,
            owner_name: self.owner_name(row.owner_type, row.owner_id).await?,
            shop_id: (row.owner_type == OWNER_SHOP).then_some(row.owner_id),
            shop_title: None,
            merchant_name: None,
            business_category: None,
            required: required.to_string(),
            paid: money(row.paid).to_string(),
            pending: pending_amount(row.paid, required).to_string(),
            deposit_status: status,
            deposit_status_text: deposit_status_text(status).into(),
        })
    }
}

/// Java `required` 口径：手动值优先，否则取应缴快照。
fn required(row: &AccountRow) -> Decimal {
    row.required_manual.unwrap_or(row.required_snapshot)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn required_prefers_manual_value() {
        let row = AccountRow {
            shop_deposit_id: 1,
            owner_type: 1,
            owner_id: 2,
            paid: Decimal::ZERO,
            required_manual: Some(Decimal::new(500, 0)),
            required_snapshot: Decimal::new(100, 0),
            penalty_status: 0,
            add_time: 0,
        };
        assert_eq!(required(&row), Decimal::new(500, 0));
        let row = AccountRow {
            required_manual: None,
            ..row
        };
        assert_eq!(required(&row), Decimal::new(100, 0));
    }

    #[test]
    fn adjust_and_deduct_validations_match_java_messages() {
        // 这里只校验常量文案（服务方法需要 DB，纯逻辑在 models 中已测）
        assert_eq!(ADJUST_TYPE_INVALID, "调整方式非法");
        assert_eq!(REQUIRED_MANUAL_INVALID, "手动应缴金额非法");
        assert_eq!(DEDUCT_AMOUNT_INVALID, "扣除金额非法");
        assert_eq!(OWNER_ID_INVALID, "持有者id非法");
    }
}
