//! 货币管理服务（lang::currency）。
//!
//! Java 来源（overseas）：
//! - `com.tigshop.adminapi.controller.lang.CurrencyController`
//! - `com.tigshop.service.lang.impl.CurrencyServiceImpl`
//!
//! b2b2b-pro 5.8.57 前端新增（无 Java 控制器对应，按任务约定最小实现）：
//! - `config` / `exchangeRateConfig`：读 `config` 表 `exchangeRateConfig`
//!   （缺省 `{}`）；
//! - `saveExchangeRateConfig`：写回 `config` 表同 code；
//! - `syncExchangeRateNow`：无汇率源凭据 → no-op（返回 code:0）；
//! - `translation`：机器翻译货币文案，无凭据时原样返回。
//!
//! Java `syncDefaultCurrencyToConfig`（主货币联动 `dollarSign` 配置）未迁移：
//! 该配置项属 PC 展示层快照，Rust 侧无消费方。

use std::sync::Arc;

use tigshop_core::entities::Currency;
use tigshop_core::models::lang::common::{BatchDTO, OperateDTO, UpdateFieldDTO, LANG_CRUD_FIELDS};
use tigshop_core::models::lang::currency::{CurrencyDTO, CurrencyListQuery, CurrencyVO};
use tigshop_core::types::{PageQuery, PageResult};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::Repositories;

/// 汇率配置在 `config` 表的 `biz_code`。
pub const CONFIG_EXCHANGE_RATE: &str = "exchangeRateConfig";

/// 货币服务（经 `AdminState.lang.currency()` 组合提供）。
pub struct CurrencyService {
    repos: Arc<Repositories>,
}

impl CurrencyService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    fn repo(&self) -> &tigshop_infrastructure::db::repo::lang::currency::CurrencyRepository {
        &self.repos.lang.currency
    }

    /// 分页列表（Java `list`：`name` LIKE）。
    ///
    /// # Errors
    /// 数据库错误 → `Error::Database`。
    pub async fn list(&self, q: &CurrencyListQuery) -> Result<PageResult<CurrencyVO>> {
        let (page, size) = q.page_params();
        let (total, rows) = self
            .repo()
            .paginate(
                q.name.as_deref().filter(|s| !s.is_empty()),
                "id ASC",
                size,
                (page - 1) * size,
            )
            .await?;
        Ok(PageResult::new(
            total,
            rows.iter().map(to_vo).collect(),
            &PageQuery {
                page,
                page_size: size,
            },
        ))
    }

    /// 详情（Java `detail`）。
    pub async fn detail(&self, id: i32) -> Result<Option<CurrencyVO>> {
        Ok(self.repo().find_by_id(id).await?.as_ref().map(to_vo))
    }

    /// 创建（Java `create`）。
    pub async fn create(&self, dto: &CurrencyDTO) -> Result<i32> {
        self.repo().insert(&dto_to_row(dto, 0)).await
    }

    /// 修改（Java `update`：`isDefault=1` 时先清空全部默认）。
    pub async fn update(&self, dto: &CurrencyDTO) -> Result<bool> {
        let id = dto
            .id
            .filter(|id| *id > 0)
            .ok_or_else(|| Error::Validation("id不能为空".into()))?;
        if dto.is_default == Some(1) {
            self.repo().clear_default().await?;
        }
        Ok(self.repo().update(&dto_to_row(dto, id)).await? > 0)
    }

    /// 单字段更新（Java `updateField`；`isDefault=1` 联动清空其余默认）。
    pub async fn update_field(&self, dto: &UpdateFieldDTO) -> Result<bool> {
        let id = dto
            .id
            .filter(|id| *id > 0)
            .ok_or_else(|| Error::Validation("id不能为空".into()))?;
        let field = dto
            .field
            .as_deref()
            .map(crate::lang::locales::camel_to_column)
            .ok_or_else(|| Error::Validation("field不能为空".into()))?;
        if !LANG_CRUD_FIELDS.contains(&field.as_str()) {
            return Err(Error::Validation(format!("不允许修改字段: {field}")));
        }
        let Some(value) = dto.val.as_deref() else {
            return Err(Error::Validation("val不能为空".into()));
        };
        if field == "is_default" && value == "1" {
            self.repo().clear_default().await?;
        }
        Ok(self.repo().update_field(id, &field, value).await? > 0)
    }

    /// 删除（Java `del`）。
    pub async fn del(&self, dto: &OperateDTO) -> Result<bool> {
        let id = dto
            .id
            .filter(|id| *id > 0)
            .ok_or_else(|| Error::Validation("id不能为空".into()))?;
        Ok(self.repo().delete_by_ids(&[id]).await? > 0)
    }

    /// 批量操作（Java `batch`：`type=del` 删除）。
    pub async fn batch(&self, dto: &BatchDTO) -> Result<bool> {
        let ids = dto.ids.clone().unwrap_or_default();
        match dto.r#type.as_deref() {
            Some("del") => Ok(self.repo().delete_by_ids(&ids).await? > 0),
            _ => Ok(false),
        }
    }

    /// `config`：读 `config` 表 `code=exchangeRateConfig`，缺省 `{}`。
    pub async fn config(&self) -> Result<serde_json::Value> {
        let raw = self
            .repos
            .system
            .config_basic
            .find_by_code(CONFIG_EXCHANGE_RATE)
            .await?;
        Ok(parse_config_json(raw.as_deref()))
    }

    /// `exchangeRateConfig`：同 [`Self::config`]（新前端拆分出的读取端点）。
    pub async fn exchange_rate_config(&self) -> Result<serde_json::Value> {
        self.config().await
    }

    /// `saveExchangeRateConfig`：写回 `config` 表
    /// （Java 无对应；任务约定存 `code=exchangeRateConfig`）。
    pub async fn save_exchange_rate_config(&self, value: serde_json::Value) -> Result<()> {
        let body = value.is_null().then(|| "{}".to_string()).unwrap_or_else(|| value.to_string());
        self.repos
            .system
            .config_basic
            .save_code(CONFIG_EXCHANGE_RATE, &body)
            .await
    }

    /// `syncExchangeRateNow`：汇率源（Java 侧火山引擎汇率 API）未配置
    /// 凭据时为 no-op，返回 `false`（无任何行被更新）。
    ///
    /// 若后续接入汇率源，按 `currency.code`（ISO 4217）拉取并
    /// `update_rate` 即可（仓储已备好该路径）。
    pub async fn sync_exchange_rate_now(&self) -> Result<bool> {
        tracing::info!("syncExchangeRateNow：未配置汇率源凭据，no-op");
        Ok(false)
    }

    /// `translation`：机器翻译（无凭据原样返回，见 `translate_text`）。
    pub async fn translation(&self, code: &str, text: &str) -> Result<String> {
        crate::lang::translations::machine_translate(&self.repos, code, text).await
    }
}

/// `config` 表存储值 → JSON（空/坏值一律 `{}` 兜底）。
pub fn parse_config_json(raw: Option<&str>) -> serde_json::Value {
    raw.and_then(|s| serde_json::from_str::<serde_json::Value>(s).ok())
        .filter(|v| v.is_object())
        .unwrap_or_else(|| serde_json::json!({}))
}

fn dto_to_row(dto: &CurrencyDTO, id: i32) -> Currency {
    Currency {
        id,
        name: dto.name.clone().unwrap_or_default(),
        symbol: dto.symbol.clone().unwrap_or_default(),
        is_default: dto.is_default.unwrap_or(0),
        rate: dto.rate.clone().unwrap_or_default(),
        code: dto.code.clone().unwrap_or_default(),
    }
}

fn to_vo(c: &Currency) -> CurrencyVO {
    CurrencyVO {
        id: Some(c.id),
        name: Some(c.name.clone()),
        symbol: Some(c.symbol.clone()),
        is_default: Some(c.is_default),
        rate: Some(c.rate.clone()),
        code: Some(c.code.clone()),
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn config_json_falls_back_to_empty_object() {
        assert_eq!(parse_config_json(None), serde_json::json!({}));
        assert_eq!(parse_config_json(Some("not json")), serde_json::json!({}));
        assert_eq!(
            parse_config_json(Some(r#"{"rateSource":"volc"}"#)),
            serde_json::json!({"rateSource":"volc"})
        );
        // 非对象（数组/标量）也兜底为 {}
        assert_eq!(parse_config_json(Some("[1,2]")), serde_json::json!({}));
    }
}
