//! 行为验证码 API（member::captcha_api）DTO / 纯函数。
//!
//! Java `com.tigshop.api.controller.user.CaptchaApiController`
//! （`/api/common/verification/captcha|get|check`，匿名白名单）。
//!
//! # 未迁移（明确报错，不伪造）
//! AJ-Captcha（滑动拼图，Redis + 第三方组件）未迁移：`captcha`/`get` 返回 1001
//! 「行为验证码服务未迁移」；`check` 先做本地格式校验（verifyToken 必填）再返回同一错误。

use serde::{Deserialize, Serialize};

pub const SERVICE_DATA_ERROR: i32 = 1001;
/// 验证码缺失。
pub const VERIFY_TOKEN_REQUIRED: &str = "验证码不能为空";
/// AJ-Captcha 未迁移。
pub const CAPTCHA_NOT_MIGRATED: &str =
    "行为验证码（AJ-Captcha）服务未迁移：本构建无法生成/校验滑动拼图验证码";

/// 验证码请求（Java AJ-Captcha `CaptchaVO` 的常用字段子集）。
#[derive(Debug, Clone, Default, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct CaptchaDTO {
    pub captcha_verification: Option<String>,
    pub verify_token: Option<String>,
}

/// 验证码返回（未迁移时为 null）。
#[derive(Debug, Clone, Default, Serialize, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct CaptchaVO {
    pub rep_code: i32,
    pub rep_msg: String,
    pub original_image_base64: Option<String>,
    pub jigsaw_image_base64: Option<String>,
    pub token: Option<String>,
}

/// 本地校验：verifyToken 必填（`check` 端点）。
#[must_use]
pub fn validate_verify_token(dto: &CaptchaDTO) -> bool {
    dto.verify_token
        .as_deref()
        .map(str::trim)
        .is_some_and(|v| !v.is_empty())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn verify_token_validation() {
        let dto = CaptchaDTO::default();
        assert!(!validate_verify_token(&dto));
        let dto = CaptchaDTO {
            verify_token: Some("  ".into()),
            ..Default::default()
        };
        assert!(!validate_verify_token(&dto));
        let dto = CaptchaDTO {
            verify_token: Some("abc".into()),
            ..Default::default()
        };
        assert!(validate_verify_token(&dto));
    }

    #[test]
    fn messages_are_explicit() {
        assert_eq!(VERIFY_TOKEN_REQUIRED, "验证码不能为空");
        assert!(CAPTCHA_NOT_MIGRATED.contains("未迁移"));
    }

    #[test]
    fn dto_serde_shapes() {
        let dto: CaptchaDTO =
            serde_json::from_str(r#"{"captchaVerification":"x","verifyToken":"y"}"#).unwrap();
        assert_eq!(dto.captcha_verification.as_deref(), Some("x"));
        assert_eq!(dto.verify_token.as_deref(), Some("y"));
    }
}
