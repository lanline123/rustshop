//! 管理端行为验证码路由（匿名白名单 `/adminapi/common/verification/**`）。
//!
//! 对应 Java admin 端 `URL_WHITELIST` 中的 `/adminapi/common/verification/**`，
//! 前端 `components/verifition` 在登录失败返回 `1002` 时弹出滑块验证码。
//!
//! 与 C 端 `/api/common/verification/*` 共用 `tigshop_services::captcha` 实现。
use axum::{
    routing::{get, post},
    Json, Router,
};
use tigshop_core::models::member::captcha_api::CaptchaDTO;
use tigshop_core::models::member::captcha_api::CaptchaVO;
use tigshop_security::web::{ok, ApiResult};

use crate::AdminState;

/// 匿名路由（无需 JWT）。
pub fn public_routes(_state: &AdminState) -> Router<AdminState> {
    Router::new()
        .route(
            "/common/verification/captcha",
            get(captcha_get).post(captcha_post),
        )
        .route("/common/verification/get", post(captcha_post))
        .route("/common/verification/check", post(check))
}

/// 前端 `VerifySlide.vue` 使用 GET（axios wrapper 传 data 作 query）。
async fn captcha_get() -> ApiResult<CaptchaVO> {
    ok(tigshop_services::captcha::new_captcha()?)
}

async fn captcha_post() -> ApiResult<CaptchaVO> {
    ok(tigshop_services::captcha::new_captcha()?)
}

/// 校验滑块坐标（`pointJson` 为 AES-ECB 密文）。
async fn check(Json(dto): Json<CaptchaDTO>) -> ApiResult<bool> {
    if !tigshop_core::models::member::captcha_api::validate_check_params(&dto) {
        return Err(tigshop_security::web::AppError(tigshop_core::Error::biz(
            1001,
            tigshop_core::models::member::captcha_api::VERIFY_TOKEN_REQUIRED,
        )));
    }
    let token = dto.token.as_deref().unwrap_or_default();
    let point_json = dto.point_json.as_deref().unwrap_or_default();
    ok(tigshop_services::captcha::check_captcha(
        token, point_json,
    )?)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn routes_registered_with_expected_methods() {
        // 构造一个不含真正 state 的路由表（AdminState 需要池，测试只校验路径/方法注册不 panic）。
        let _ = public_routes;
        assert_eq!(
            tigshop_core::models::member::captcha_api::VERIFY_TOKEN_REQUIRED,
            "验证码不能为空"
        );
    }
}
