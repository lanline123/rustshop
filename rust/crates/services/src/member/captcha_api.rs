//! 行为验证码服务（member::captcha_api）。AJ-Captcha 未迁移，统一明确 1001 错误。

use std::sync::Arc;

use tigshop_core::models::member::captcha_api::{
    CaptchaDTO, CaptchaVO, CAPTCHA_NOT_MIGRATED, SERVICE_DATA_ERROR, VERIFY_TOKEN_REQUIRED,
};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::Repositories;

/// 验证码服务（无仓储访问；`repos` 仅为统一装配签名保留）。
pub struct CaptchaService {
    #[allow(dead_code)]
    repos: Arc<Repositories>,
}

impl CaptchaService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// `POST /api/common/verification/captcha`（未迁移）。
    pub fn captcha(&self) -> Result<CaptchaVO> {
        Err(Error::biz(SERVICE_DATA_ERROR, CAPTCHA_NOT_MIGRATED))
    }

    /// `POST /api/common/verification/get`（废弃端点，未迁移）。
    pub fn get(&self) -> Result<CaptchaVO> {
        Err(Error::biz(SERVICE_DATA_ERROR, CAPTCHA_NOT_MIGRATED))
    }

    /// `POST /api/common/verification/check`：本地校验后明确未迁移。
    pub fn check(&self, dto: &CaptchaDTO) -> Result<bool> {
        if !tigshop_core::models::member::captcha_api::validate_verify_token(dto) {
            return Err(Error::biz(SERVICE_DATA_ERROR, VERIFY_TOKEN_REQUIRED));
        }
        Err(Error::biz(SERVICE_DATA_ERROR, CAPTCHA_NOT_MIGRATED))
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use tigshop_core::models::member::captcha_api::CaptchaDTO;
    use tigshop_core::Error;

    #[test]
    fn check_requires_token_before_not_migrated() {
        // 通过构造一个空 repos 不可行（无 DB）；这里仅校验常量语义。
        assert_eq!(VERIFY_TOKEN_REQUIRED, "验证码不能为空");
        let dto = CaptchaDTO::default();
        assert!(!tigshop_core::models::member::captcha_api::validate_verify_token(&dto));
        let err = Error::biz(SERVICE_DATA_ERROR, CAPTCHA_NOT_MIGRATED);
        assert!(err.to_string().contains("未迁移"));
    }
}
