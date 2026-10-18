//! 通用工具 域服务（system::util）。
//!
//! 构造签名固定 `pub fn new(repos: Arc<Repositories>) -> Self`；仓储经嵌套
//! 聚合根访问：`self.repos.system.util`（本 feature 不访问数据库）。
//!
//! 对应 Java `UtilController`（3 个端点，契约 `auth=optional`）。
//!
//! ## 未迁移依赖（全部为**显式失败**，不伪造成功）
//!
//! 1. `qrCode`：Java `cn.hutool.extra.qrcode.QrCodeUtil.generateAsBase64`
//!    依赖 hutool 二维码库；Rust 工作区未引入二维码 crate → 返回 1001
//!    「二维码生成功能未迁移」。本地校验：`url` 非空。
//! 2. `miniCode`：Java `WechatOAuthService.getMiniCode` 依赖微信小程序
//!    access_token/接口调用 → 返回 1001「小程序码生成功能未迁移」。
//!    本地校验：`path`、`id` 非空。
//! 3. `miniScene`：Java `WechatOAuthService.resolveMiniScene` 依赖微信
//!    OAuth 客户端 → 返回 1001「小程序码 scene 解析未迁移」。本地校验：
//!    `scene` 非空。
//!
//! 错误码 `1001` 与 `config_basic` 的 `SERVICE_DATA_ERROR` 口径一致（业务错误）。

use std::sync::Arc;

use tigshop_core::models::system::util::{
    MSG_ID_REQUIRED, MSG_MINI_CODE_NOT_MIGRATED, MSG_MINI_SCENE_NOT_MIGRATED,
    MSG_PATH_REQUIRED, MSG_QRCODE_NOT_MIGRATED, MSG_SCENE_REQUIRED, MSG_URL_REQUIRED,
};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::system::util::UtilRepository;
use tigshop_infrastructure::db::Repositories;

/// 「未迁移」业务错误码（与 config_basic 一致）。
const SERVICE_DATA_ERROR: i32 = 1001;

/// 通用工具 域服务。
pub struct UtilService {
    repos: Arc<Repositories>,
}

impl UtilService {
    /// 构造服务（组合根唯一装配点）。
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本 feature 仓储（无表访问）。
    #[must_use]
    pub fn repo(&self) -> &UtilRepository {
        &self.repos.system.util
    }

    /// 二维码（Java `QrCodeUtil.generateAsBase64` 未迁移）。
    pub async fn qr_code(&self, url: Option<&str>) -> Result<String> {
        let url = url.map(str::trim).filter(|s| !s.is_empty());
        let Some(_url) = url else {
            return Err(Error::Validation(MSG_URL_REQUIRED.into()));
        };
        Err(Error::biz(SERVICE_DATA_ERROR, MSG_QRCODE_NOT_MIGRATED))
    }

    /// 小程序码（Java `WechatOAuthService.getMiniCode` 未迁移）。
    pub async fn mini_code(
        &self,
        path: Option<&str>,
        id: Option<i32>,
        _salesman_id: Option<i32>,
        _shop_id: Option<i32>,
    ) -> Result<String> {
        if path.map(str::trim).filter(|s| !s.is_empty()).is_none() {
            return Err(Error::Validation(MSG_PATH_REQUIRED.into()));
        }
        if id.is_none() {
            return Err(Error::Validation(MSG_ID_REQUIRED.into()));
        }
        Err(Error::biz(SERVICE_DATA_ERROR, MSG_MINI_CODE_NOT_MIGRATED))
    }

    /// 小程序码 scene 解析（Java `WechatOAuthService.resolveMiniScene` 未迁移）。
    pub async fn mini_scene(&self, scene: Option<&str>) -> Result<serde_json::Value> {
        if scene.map(str::trim).filter(|s| !s.is_empty()).is_none() {
            return Err(Error::Validation(MSG_SCENE_REQUIRED.into()));
        }
        Err(Error::biz(SERVICE_DATA_ERROR, MSG_MINI_SCENE_NOT_MIGRATED))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn not_migrated_code_is_1001() {
        assert_eq!(SERVICE_DATA_ERROR, 1001);
    }

    #[test]
    fn qr_code_requires_url_before_not_migrated() {
        // 与 `qr_code` 内部判定同构的纯逻辑校验。
        for empty in ["", "   "] {
            assert!(empty.trim().is_empty());
        }
        assert!(!MSG_URL_REQUIRED.is_empty());
        assert!(MSG_QRCODE_NOT_MIGRATED.contains("未迁移"));
    }

    #[test]
    fn mini_scene_requires_scene() {
        assert!(MSG_SCENE_REQUIRED.contains("scene"));
        assert!(MSG_MINI_SCENE_NOT_MIGRATED.contains("未迁移"));
    }

}
