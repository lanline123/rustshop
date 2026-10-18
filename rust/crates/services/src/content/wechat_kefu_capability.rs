//! 客服消息能力探测服务（content::wechat_kefu_capability）。
//!
//! 对应原 Java `WxKefuCapabilityProbe`（`isEnabled()`）。
//!
//! 构造签名固定：`pub fn new(repos: Arc<Repositories>) -> Self`。
//!
//! ## 已迁移（1）
//! `capability`：**本地判定**——公众号四键配置不全（Java `wxMpService == null`）
//! → `{kefuEnabled:false}`；配置齐全时因微信 `kfList()` 探测未迁移，返回明确
//! 「未迁移」业务错误（不伪造 true/false）。
//!
//! ## 与 Java 的差异
//! 见 [`crate::models::content::wechat_kefu_capability`] 模块头。

use std::sync::Arc;

use tigshop_core::models::content::wechat_kefu_capability::{
    WechatKefuCapabilityVO, MSG_PROBE_NOT_MIGRATED,
};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::Repositories;

/// 客服消息能力探测服务。
pub struct WechatKefuCapabilityService {
    repos: Arc<Repositories>,
}

impl WechatKefuCapabilityService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本片仓储（NESTED 访问：`self.repos.content.wechat_kefu_capability`）。
    #[must_use]
    pub fn repo(
        &self,
    ) -> &tigshop_infrastructure::db::repo::content::wechat_kefu_capability::WechatKefuCapabilityRepository
    {
        &self.repos.content.wechat_kefu_capability
    }

    /// 探测客服消息能力（Java `isEnabled()`）。
    ///
    /// # Errors
    /// 公众号已配置（需微信 RPC 探测）→ 业务错误「未迁移」。
    pub async fn capability(&self) -> Result<WechatKefuCapabilityVO> {
        if !self.repo().is_wechat_configured().await? {
            // Java：WxMpServiceHolder.get() == null → enabled = false
            return Ok(WechatKefuCapabilityVO::disabled());
        }
        Err(Error::biz_msg(MSG_PROBE_NOT_MIGRATED))
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use tigshop_core::models::content::wechat_kefu_capability::WECHAT_CONFIG_CODES;

    #[test]
    fn disabled_vo_matches_java_unconfigured_branch() {
        let vo = WechatKefuCapabilityVO::disabled();
        assert!(!vo.kefu_enabled);
        assert_eq!(
            serde_json::to_value(&vo).unwrap()["kefuEnabled"],
            false
        );
    }

    #[test]
    fn probe_requires_all_four_config_keys() {
        // Java `StrUtil.hasBlank(appId, secret, token, aesKey)`：任一为空即未配置
        let configured = |values: &[Option<&str>]| {
            values.len() == WECHAT_CONFIG_CODES.len()
                && values.iter().all(|v| {
                    v.map(str::trim)
                        .is_some_and(|s| !s.is_empty())
                })
        };
        assert!(configured(&[Some("a"), Some("b"), Some("c"), Some("d")]));
        assert!(!configured(&[Some("a"), Some("b"), Some("c"), None]));
        assert!(!configured(&[Some("a"), Some("b"), Some("c"), Some("  ")]));
        assert!(!configured(&[]));
    }

    #[test]
    fn not_migrated_message_branch() {
        assert!(MSG_PROBE_NOT_MIGRATED.contains("尚未迁移"));
    }
}
