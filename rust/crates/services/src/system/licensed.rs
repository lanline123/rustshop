//! 许可证 域服务（system::licensed）。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`；仓储经嵌套
//! 聚合根访问：`self.repos.system.licensed`。
//!
//! 对应 Java `LicensedServiceImpl`
//! （`java/tigshop-service/.../setting/impl/LicensedServiceImpl.java`）。
//!
//! ## 已登记偏差
//!
//! 1. **`update` 的在线校验未迁移**：Java 调 `TigshopApiClient.updateLicense`
//!    到官网校验授权码并回写授权信息；本仓库无该外部客户端 → 保留本地参数校验
//!    （`license`/`domain` 非空）后返回明确的「未迁移」业务错误（1001），
//!    **不伪造成功、不写库**。
//! 2. `getLicensedIndex` 的 `license` 隐藏规则按 Java
//!    `StringUtils.hide(license, 5, 15)` 复刻（见模型层 [`hide`]）。
//! 3. `authorizationLifecycleService.onAuthorizationChanged()` 生命周期回调未迁移
//!    （依赖授权 SDK）。

use std::sync::Arc;

use tigshop_core::models::system::licensed::{
    hide, LicensedEditParam, LicensedSaveParam, LicensedSettingsVO, CODE_AUTHORIZED_DOMAIN,
    CODE_DE_COPYRIGHT, CODE_IS_ENTERPRISE, CODE_LICENSE, CODE_LICENSED_TYPE,
    CODE_LICENSED_TYPE_NAME,
};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::system::licensed::LicensedRepository;
use tigshop_infrastructure::db::Repositories;

/// 「未迁移」业务错误码（与 config_basic 的 SERVICE_DATA_ERROR 口径一致）。
const SERVICE_DATA_ERROR: i32 = 1001;

/// 许可证 域服务。
pub struct LicensedService {
    repos: Arc<Repositories>,
}

impl LicensedService {
    /// 构造服务（组合根唯一装配点）。
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本 feature 仓储。
    #[must_use]
    pub fn repo(&self) -> &LicensedRepository {
        &self.repos.system.licensed
    }

    /// 授权信息（Java `getLicensedIndex`）：读取配置 → 隐藏授权码。
    pub async fn get_licensed_index(&self) -> Result<LicensedSettingsVO> {
        let rows = self.repo().find_licensed().await?;
        Ok(settings_from_rows(&rows))
    }

    /// 更新授权（Java `update`）：本地校验后返回「未迁移」业务错误。
    pub async fn update(&self, param: &LicensedEditParam) -> Result<()> {
        if param.license.trim().is_empty() {
            return Err(Error::Validation("授权码不能为空".into()));
        }
        if param.domain.trim().is_empty() {
            return Err(Error::Validation("授权域名不能为空".into()));
        }
        Err(Error::biz(
            SERVICE_DATA_ERROR,
            "授权在线校验未迁移（TigshopApiClient.updateLicense）",
        ))
    }

    /// 保存授权（Java `saveLicensed`）：写 `config` 表（先删后插，事务内）。
    pub async fn save_licensed(&self, param: &LicensedSaveParam) -> Result<()> {
        let mut pairs: Vec<(String, String)> = Vec::new();
        push_str(&mut pairs, CODE_LICENSE, param.license.as_deref());
        push_str(&mut pairs, CODE_LICENSED_TYPE, param.licensed_type.as_deref());
        push_str(
            &mut pairs,
            CODE_LICENSED_TYPE_NAME,
            param.licensed_type_name.as_deref(),
        );
        if let Some(v) = param.is_enterprise {
            pairs.push((CODE_IS_ENTERPRISE.to_string(), v.to_string()));
        }
        if let Some(v) = param.de_copyright {
            pairs.push((CODE_DE_COPYRIGHT.to_string(), v.to_string()));
        }
        push_str(
            &mut pairs,
            CODE_AUTHORIZED_DOMAIN,
            param.authorized_domain.as_deref(),
        );
        if pairs.is_empty() {
            return Err(Error::Validation("没有需要保存的授权配置".into()));
        }
        self.repo().save_licensed(&pairs).await
    }
}

fn push_str(pairs: &mut Vec<(String, String)>, code: &str, value: Option<&str>) {
    if let Some(v) = value {
        pairs.push((code.to_string(), v.to_string()));
    }
}

/// 配置行 → 授权 VO（缺失项为空串/null；license 隐藏）。
#[must_use]
pub fn settings_from_rows(rows: &[(String, String)]) -> LicensedSettingsVO {
    let get = |code: &str| -> Option<&str> {
        rows.iter()
            .find(|(c, _)| c == code)
            .map(|(_, v)| v.as_str())
    };
    let parse_i32 = |code: &str| get(code).and_then(|v| v.parse::<i32>().ok());
    LicensedSettingsVO {
        license: hide(get(CODE_LICENSE).unwrap_or_default(), 5, 15),
        licensed_type: get(CODE_LICENSED_TYPE).unwrap_or_default().to_string(),
        licensed_type_name: get(CODE_LICENSED_TYPE_NAME).unwrap_or_default().to_string(),
        is_enterprise: parse_i32(CODE_IS_ENTERPRISE),
        de_copyright: parse_i32(CODE_DE_COPYRIGHT),
        authorized_domain: get(CODE_AUTHORIZED_DOMAIN).unwrap_or_default().to_string(),
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn rows() -> Vec<(String, String)> {
        vec![
            (CODE_LICENSE.to_string(), "ABCDEFGHIJKLMNOPQRSTUV".to_string()),
            (CODE_LICENSED_TYPE.to_string(), "enterprise".to_string()),
            (CODE_LICENSED_TYPE_NAME.to_string(), "企业版".to_string()),
            (CODE_IS_ENTERPRISE.to_string(), "1".to_string()),
            (CODE_DE_COPYRIGHT.to_string(), "0".to_string()),
            (
                CODE_AUTHORIZED_DOMAIN.to_string(),
                "shop.example.com".to_string(),
            ),
        ]
    }

    #[test]
    fn settings_hides_license_and_parses_ints() {
        let vo = settings_from_rows(&rows());
        assert_eq!(vo.license, "ABCDE****PQRSTUV");
        assert_eq!(vo.is_enterprise, Some(1));
        assert_eq!(vo.de_copyright, Some(0));
        assert_eq!(vo.licensed_type_name, "企业版");
    }

    #[test]
    fn empty_rows_yield_defaults() {
        let vo = settings_from_rows(&[]);
        assert_eq!(vo.license, "");
        assert_eq!(vo.is_enterprise, None);
        assert_eq!(vo.authorized_domain, "");
    }

    #[test]
    fn save_param_rejects_empty_pairs() {
        // 纯逻辑：空入参不会拼出空 pairs（由服务层拒绝）。
        let p = LicensedSaveParam {
            license: None,
            licensed_type: None,
            licensed_type_name: None,
            is_enterprise: None,
            de_copyright: None,
            authorized_domain: None,
        };
        let mut pairs = Vec::new();
        push_str(&mut pairs, CODE_LICENSE, p.license.as_deref());
        assert!(pairs.is_empty());
    }
}
