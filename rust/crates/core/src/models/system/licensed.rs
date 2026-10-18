//! 许可证（system::licensed）模型层。
//!
//! 对应 Java `LicensedController`
//! （`java/tigshop-adminapi/.../controller/setting/LicensedController.java`，
//! `@RequestMapping("/adminapi/setting/licensed")`），3 个端点。
//! 配置项存放于 `config` 表（`biz_code`/`biz_val`），键名来自 Java
//! `SettingsEnum`（`java/tigshop-bean/.../enums/setting/SettingsEnum.java:540-545`）。

use serde::{Deserialize, Serialize};

/// 授权码（Java `SettingsEnum.LICENSE`）。
pub const CODE_LICENSE: &str = "license";
/// 授权类型（Java `SettingsEnum.LICENSED_TYPE`）。
pub const CODE_LICENSED_TYPE: &str = "licensedType";
/// 授权类型名称（Java `SettingsEnum.LICENSED_TYPE_NAME`）。
pub const CODE_LICENSED_TYPE_NAME: &str = "licensedTypeName";
/// 是否企业用户（Java `SettingsEnum.IS_ENTERPRISE`）。
pub const CODE_IS_ENTERPRISE: &str = "isEnterprise";
/// 去版权（Java `SettingsEnum.DE_COPYRIGHT`）。
pub const CODE_DE_COPYRIGHT: &str = "deCopyright";
/// 授权域名（Java `SettingsEnum.AUTHORIZED_DOMAIN`）。
pub const CODE_AUTHORIZED_DOMAIN: &str = "authorizedDomain";

/// 本 feature 全部配置键（读取顺序固定）。
pub const LICENSED_CODES: [&str; 6] = [
    CODE_LICENSE,
    CODE_LICENSED_TYPE,
    CODE_LICENSED_TYPE_NAME,
    CODE_IS_ENTERPRISE,
    CODE_DE_COPYRIGHT,
    CODE_AUTHORIZED_DOMAIN,
];

/// 授权信息（Java `LicensedSettingsVO`）。
#[derive(Debug, Clone, Default, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct LicensedSettingsVO {
    /// 授权码（`getLicensedIndex` 返回前做隐藏：保留前 5 位、15 位后保留）
    pub license: String,
    /// 授权类型
    pub licensed_type: String,
    /// 授权类型名称
    pub licensed_type_name: String,
    /// 是否企业用户
    pub is_enterprise: Option<i32>,
    /// 去版权
    pub de_copyright: Option<i32>,
    /// 授权域名
    pub authorized_domain: String,
}

/// 更新授权（Java `LicensedEditParam`）。
#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct LicensedEditParam {
    /// 授权码
    pub license: String,
    /// 授权域名
    pub domain: String,
}

/// 保存授权（Java `LicensedSaveParam`）。
#[derive(Debug, Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct LicensedSaveParam {
    /// 授权码
    pub license: Option<String>,
    /// 授权类型
    pub licensed_type: Option<String>,
    /// 授权类型名称
    pub licensed_type_name: Option<String>,
    /// 是否企业用户
    pub is_enterprise: Option<i32>,
    /// 去版权
    pub de_copyright: Option<i32>,
    /// 授权域名
    pub authorized_domain: Option<String>,
}

/// 字符串隐藏（Java `StringUtils.hide(str, 5, 15)`）：保留前 5 位与第 15 位之后，
/// 中间替换为 `*`；长度不足时按 Java 行为只返回星号。
#[must_use]
pub fn hide(secret: &str, start: usize, end: usize) -> String {
    let chars: Vec<char> = secret.chars().collect();
    if chars.len() <= start || chars.len() <= end {
        return "*".repeat(chars.len().min(start));
    }
    let head: String = chars[..start].iter().collect();
    let tail: String = chars[end..].iter().collect();
    format!("{head}****{tail}")
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn hide_keeps_head_and_tail() {
        assert_eq!(hide("ABCDEFGHIJKLMNOPQRSTUV", 5, 15), "ABCDE****PQRSTUV");
        assert_eq!(hide("short", 5, 15), "*****");
        assert_eq!(hide("", 5, 15), "");
    }

    #[test]
    fn settings_vo_serializes_camel_case() {
        let vo = LicensedSettingsVO {
            license: "ABC".into(),
            licensed_type: "trial".into(),
            licensed_type_name: "试用".into(),
            is_enterprise: Some(1),
            de_copyright: Some(0),
            authorized_domain: "shop.example.com".into(),
        };
        let j = serde_json::to_value(&vo).unwrap();
        assert_eq!(j["licensedType"], "trial");
        assert_eq!(j["isEnterprise"], 1);
        assert_eq!(j["authorizedDomain"], "shop.example.com");
    }

    #[test]
    fn edit_param_reads_camel_case() {
        let p: LicensedEditParam =
            serde_json::from_value(serde_json::json!({"license":"X","domain":"a.com"})).unwrap();
        assert_eq!(p.license, "X");
        assert_eq!(p.domain, "a.com");
    }
}
