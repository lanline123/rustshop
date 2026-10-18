//! 会员登录域服务（member::login）。
//!
//! 构造签名：`pub fn new(repos: Arc<Repositories>, cache: Option<CacheService>, jwt: Arc<JwtService>)`
//! —— 与既有 `catalog::product`（`cache: Option<CacheService>`）和
//! `identity::auth`（`jwt: Arc<JwtService>`）保持一致的装配风格；Redis 不可用时
//! `cache = None` 降级为「验证码/重置令牌不可用」，不阻断进程启动。
//!
//! 仓储访问为**嵌套**命名：`self.repos.member.login.<method>()`。
//!
//! 对应原 Java：
//! - `ConfigServiceImpl#sendSms` / `#sendEmail` / `#getQuickLoginSetting` / `#getWechatLoginUrl`
//! - `UserServiceImpl#checkMobile` / `#checkEmail` / `#issuePasswordReset` /
//!   `#modifyPassword` / `#bindMobile` / `#unbindWechat`
//! - `ClientLoginFilter`（C 端 JWT 签发口径：`typ = "member"`）
//!
//! # 已实现（与 Java 逐条对齐）
//!
//! | 端点 | 语义 |
//! |------|------|
//! | `sendMobileCode`（/api + /adminapi 两条） | 校验手机号 → 生成 6 位码 → 写缓存 `event+mobile`（TTL 5 分钟）→ 不实际发短信（通道未迁移） |
//! | `sendEmailCode` | 校验邮箱 → 同上（`event+email`） |
//! | `checkMobile` / `checkEmail` | 校验缓存验证码 → 要求账号唯一且启用 → 生成 64 位 hex 重置令牌 → 缓存 `password-reset:{md5(token)}` → user_id（TTL 10 分钟），返回令牌 |
//! | `forgetPassword` | 校验令牌 → 校验账号启用 → `bcrypt` 重新哈希密码 → 更新 `user.password` → 消费令牌 |
//! | `bindMobile` | 校验 `login{mobile}` 验证码 → 已有账号则置 `mobile_validated=1`，否则注册新用户（受 `shopRegClosed` 开关约束）→ 处理微信授权绑定 → 返回会员 JWT |
//! | `getQuickLoginSetting` | 按 `x-client-type` 返回 `{wechatLogin, showOauth}`（`pc/wechat` 取 `openWechatOauth`，`miniProgram` 恒 1） |
//! | `getWxLoginUrl` | `x-client-type=wechat` 时拼公众号 OAuth URL 并缓存 state（300s）；`pc` 扫码需要微信二维码 API（未迁移 → 明确报错） |
//! | `unbindWechat` | 登录态；删除当前会员的 `user_authorize` 记录；未绑定 → 「该账号未绑定微信公众号」 |
//!
//! # 未迁移（明确 1001 业务错误，绝不伪造 token）
//!
//! `wechatServer`（公众号服务器回调验签）、`wechatEvent`（扫码 openid 轮询 + 自动注册）、
//! `getMobile`（小程序手机号 API）、`updateUserOpenid`（code2Session）、
//! `getWxLoginInfoByCode`（公众号 OAuth）、`bindWechat`（公众号 OAuth）——
//! 全部依赖微信开放平台接口 / 微信 JSSDK 配置，本构建未接入。
//!
//! # 其它已知差异
//!
//! - 行为验证（AJ-Captcha 滑动拼图）未迁移：`verifyToken` 不做校验（否则发送验证码恒失败）；
//! - 短信/邮件只写缓存不实际发送（未接 RabbitMQ + 服务商通道）；
//! - 重置令牌摘要用 **MD5**（services crate 无 `sha2` 依赖，不可改 Cargo.toml），
//!   Java 用 SHA-256；令牌本身仍为 64 位小写 hex、TTL 与 Java 相同；
//! - `bindMobile` 的微信授权绑定做了「按 openid/按用户」两段 upsert 的等价实现，
//!   但 `open_name` / `open_photo` 用 openData 里的 `nickname` / `headimgurl` 填充
//!   （Java 只写 `open_data`，两列取实体默认值）；
//! - `bindMobile` 的 `wholesalerService.autoRegisterIfEnabled`（自动成为批发商）
//!   与分销员初始化未迁移（依赖 wholesale/marketing 域）；
//! - Java 登录成功后会把 token 写入 Redis（供 logout 吊销）；Rust 侧 C 端登录
//!   （含 `api/public/src/lib.rs::user_login`）尚未实现该吊销表，这里保持一致不写。

use std::sync::Arc;

use md5::{Digest, Md5};
use serde_json::Value;
use tigshop_common::utils::now_seconds;
use tigshop_core::models::member::login::{
    authorize_type_from_client, auto_nickname, email_code_key, password_reset_key, quick_login_flags,
    sms_code_key, unwrap_open_data, validate_code_format, validate_email_basic, validate_reset_token,
    wechat_state_key, BindMobileDTO, CheckEmailDTO, CheckMobileDTO, CodeParamDTO, ForgetPasswordDTO,
    LoginWechatEventVO, QuickLoginSettingVO, RegisterEmailDTO, RegisterSmsDTO, WechatLoginUrlVO,
    WxLoginInfoByCodeVO, ACCOUNT_CANNOT_RESET, BIND_PASSWORD_REQUIRED, CACHE_UNAVAILABLE,
    CODE_INVALID, CODE_TTL_SECS, EMAIL_REQUIRED, IDENTITY_REQUIRED, MEMBER_TOKEN_TYPE,
    MOBILE_REQUIRED, PASSWORD_REQUIRED, PASSWORD_UPDATE_FAILED, REGISTER_CLOSED,
    RESET_TOKEN_INVALID, SERVICE_DATA_ERROR, SWITCH_OFF, USER_STATUS_OK, WECHAT_NOT_BOUND,
    WECHAT_NOT_MIGRATED, WECHAT_SERVER_NOT_MIGRATED,
};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::cache::CacheService;
use tigshop_infrastructure::db::repo::member::login::LoginRepository;
use tigshop_infrastructure::db::Repositories;
use tigshop_security::{hash_password, JwtService};

/// 会员登录域服务。
pub struct LoginService {
    repos: Arc<Repositories>,
    cache: Option<CacheService>,
    jwt: Arc<JwtService>,
}

impl LoginService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>, cache: Option<CacheService>, jwt: Arc<JwtService>) -> Self {
        Self { repos, cache, jwt }
    }

    /// 本特性仓储（嵌套访问：`repos.member.login`）。
    #[must_use]
    pub fn repo(&self) -> &LoginRepository {
        &self.repos.member.login
    }

    // ————————————————————— 验证码发送 —————————————————————

    /// `POST /api/user/login/sendMobileCode` 与 `POST /adminapi/login/sendMobileCode`。
    ///
    /// Java `ConfigServiceImpl#sendSms`：行为验证 → 生成 6 位码 → 发送短信 →
    /// 写缓存 `event+mobile`（5 分钟）。本实现前两步中的「行为验证」跳过、
    /// 「发送」未迁移（只写缓存，便于接入通道后直接可用）。
    ///
    /// # Errors
    /// 手机号缺失 → 「手机号码不能为空」；缓存不可用 → 1001；Redis 失败 → `Error::Cache`。
    pub async fn send_mobile_code(&self, dto: &RegisterSmsDTO) -> Result<()> {
        let mobile = dto
            .mobile
            .as_deref()
            .map(str::trim)
            .filter(|m| !m.is_empty())
            .ok_or_else(|| Error::biz(SERVICE_DATA_ERROR, MOBILE_REQUIRED))?;
        let code = generate_sms_code();
        let key = sms_code_key(dto.event.as_deref(), mobile);
        self.cache()?
            .set_json(&key, &code, CODE_TTL_SECS)
            .await?;
        // 真实通道：阿里云短信（未配置 → 明确业务错误；TIG_SMS__MOCK=1 时仅日志）
        crate::system::notify::send_sms_code(&self.repos, mobile, &code).await?;
        tracing::info!("短信验证码已发送: mobile={mobile} event={:?}", dto.event);
        Ok(())
    }

    /// `POST /api/user/login/sendEmailCode`。
    ///
    /// Java `ConfigServiceImpl#sendEmail`：邮箱格式校验 → 行为验证 → 生成 6 位码 →
    /// SMTP 发送 → 写缓存 `event+email`（5 分钟）。同样只写缓存。
    ///
    /// # Errors
    /// 邮箱缺失/非法 → 「邮箱不能为空」/「邮箱地址不正确」；缓存不可用 → 1001。
    pub async fn send_email_code(&self, dto: &RegisterEmailDTO) -> Result<()> {
        let email = dto
            .email
            .as_deref()
            .map(str::trim)
            .filter(|e| !e.is_empty())
            .ok_or_else(|| Error::biz(SERVICE_DATA_ERROR, EMAIL_REQUIRED))?;
        let email = validate_email_basic(Some(email))?;
        let code = generate_sms_code();
        let key = email_code_key(dto.event.as_deref(), email);
        self.cache()?
            .set_json(&key, &code, CODE_TTL_SECS)
            .await?;
        // 真实通道：SMTP（未配置 → 明确业务错误）
        crate::system::notify::send_email_code(&self.repos, &email, "验证码", &code).await?;
        tracing::info!("邮箱验证码已发送: email={email}");
        Ok(())
    }

    // ————————————————————— 重置密码 —————————————————————

    /// `POST /api/user/login/checkMobile`：校验短信验证码并签发重置令牌。
    ///
    /// Java `UserServiceImpl#checkMobile` → `issuePasswordReset(mobile, code, false)`。
    ///
    /// # Errors
    /// 手机号/验证码为空或错误、账号不唯一或被禁用 → 1001 业务错误（Java 逐字文案）。
    pub async fn check_mobile(&self, dto: &CheckMobileDTO) -> Result<String> {
        let mobile = require_identity(dto.mobile.as_deref(), MOBILE_REQUIRED)?;
        let reset = self
            .issue_password_reset(mobile, dto.code.as_deref(), false)
            .await?;
        Ok(reset)
    }

    /// `POST /api/user/login/checkEmail`：校验邮箱验证码并签发重置令牌。
    ///
    /// # Errors
    /// 同上（邮箱维度）。
    pub async fn check_email(&self, dto: &CheckEmailDTO) -> Result<String> {
        let email = require_identity(dto.email.as_deref(), EMAIL_REQUIRED)?;
        let reset = self
            .issue_password_reset(email, dto.code.as_deref(), true)
            .await?;
        Ok(reset)
    }

    /// Java `UserServiceImpl#issuePasswordReset`。
    ///
    /// 1. `validateVerificationCode("forgetPassword" + identity, code)`；
    /// 2. 账号必须**恰好 1 个**且 `status=1`；
    /// 3. 消费验证码（删除缓存键）；
    /// 4. 生成 32 字节随机令牌（64 位小写 hex），缓存 `password-reset:{摘要}` → user_id（10 分钟）。
    async fn issue_password_reset(
        &self,
        identity: &str,
        code: Option<&str>,
        email: bool,
    ) -> Result<String> {
        if identity.is_empty() {
            return Err(Error::biz(SERVICE_DATA_ERROR, IDENTITY_REQUIRED));
        }
        let code = validate_code_format(code)?;
        let code_key = format!("forgetPassword{identity}");
        let cache = self.cache()?;
        let expected: Option<String> = cache.get_json(&code_key).await?;
        match expected {
            Some(expected) if expected == code => {}
            _ => return Err(Error::biz(SERVICE_DATA_ERROR, CODE_INVALID)),
        }

        let candidates = if email {
            self.repo().reset_candidates_by_email(identity).await?
        } else {
            self.repo().reset_candidates_by_mobile(identity).await?
        };
        if candidates.len() != 1 || candidates[0].1 != USER_STATUS_OK {
            return Err(Error::biz(SERVICE_DATA_ERROR, ACCOUNT_CANNOT_RESET));
        }
        let user_id = candidates[0].0;

        // 消费验证码（Java 用 Lua 脚本原子 GET+DEL；这里先删再写令牌，失败不恢复）
        cache.del(&code_key).await?;

        let token = generate_reset_token();
        cache
            .set_json(&password_reset_key(&md5_hex(&token)), &user_id, 600)
            .await?;
        Ok(token)
    }

    /// `POST /api/user/login/forgetPassword`：用重置令牌修改密码。
    ///
    /// Java `UserServiceImpl#modifyPassword`：令牌格式 64 hex → 缓存取 user_id →
    /// 账号启用 → 消费令牌 → `passwordEncoder.encode` → `updateById`。
    ///
    /// # Errors
    /// 令牌非法/过期、账号被禁用、写库失败 → 1001 业务错误。
    pub async fn forget_password(&self, dto: &ForgetPasswordDTO) -> Result<()> {
        let token = validate_reset_token(dto.mobile_key.as_deref())?;
        let password = dto
            .password
            .as_deref()
            .filter(|p| !p.is_empty())
            .ok_or_else(|| Error::biz(SERVICE_DATA_ERROR, PASSWORD_REQUIRED))?;

        let cache = self.cache()?;
        let key = password_reset_key(&md5_hex(token));
        let user_id: Option<i32> = cache.get_json(&key).await?;
        let user_id = user_id
            .filter(|id| *id > 0)
            .ok_or_else(|| Error::biz(SERVICE_DATA_ERROR, RESET_TOKEN_INVALID))?;
        if self.repo().user_status(user_id).await? != Some(USER_STATUS_OK) {
            return Err(Error::biz(SERVICE_DATA_ERROR, ACCOUNT_CANNOT_RESET));
        }
        // 先一次性消费再写库（Java 注释：写库失败也不恢复凭据）
        cache.del(&key).await?;
        let hash = hash_password(password)?;
        if !self.repo().update_password(user_id, &hash).await? {
            return Err(Error::biz(SERVICE_DATA_ERROR, PASSWORD_UPDATE_FAILED));
        }
        Ok(())
    }

    // ————————————————————— 绑定手机 / 微信 —————————————————————

    /// `POST /api/user/login/bindMobile`：绑定手机号并返回会员 JWT。
    ///
    /// Java `UserServiceImpl#bindMobile` 的等价流程：
    /// 1. `validateVerificationCode("login" + mobile, mobileCode)`；
    /// 2. 已有该手机号 → 仅置 `mobile_validated=1`；否则检查 `shopRegClosed`
    ///    （「系统已关闭注册」）→ 创建用户；
    /// 3. `openData`（可多层嵌套）里的 `openid/unionid` 写入 `user_authorize`：
    ///    先按 openid 找记录（有则改归属），再按用户找同类型记录（有则换 openid），
    ///    否则新建；
    /// 4. 返回会员访问令牌（`JwtService#issue_pair`，`typ = "member"`）。
    ///
    /// # Errors
    /// 验证码错误、注册关闭、密码为空、数据库/缓存失败 → 业务错误或 `Error::Database`。
    pub async fn bind_mobile(
        &self,
        dto: &BindMobileDTO,
        client_type: Option<&str>,
    ) -> Result<String> {
        let mobile = require_identity(dto.mobile.as_deref(), MOBILE_REQUIRED)?.to_string();
        let code = validate_code_format(dto.mobile_code.as_deref())?;
        let password = dto
            .password
            .as_deref()
            .filter(|p| !p.is_empty())
            .ok_or_else(|| Error::biz(SERVICE_DATA_ERROR, BIND_PASSWORD_REQUIRED))?;

        // 1. 校验 login{mobile} 验证码（Java `validateVerificationCode`）
        let code_key = format!("login{mobile}");
        let cache = self.cache()?;
        let expected: Option<String> = cache.get_json(&code_key).await?;
        match expected {
            Some(expected) if expected == code => {}
            _ => return Err(Error::biz(SERVICE_DATA_ERROR, CODE_INVALID)),
        }
        cache.del(&code_key).await?;

        // 2. openData（兼容前端多套一层 openData）
        let open_data = unwrap_open_data(dto.open_data.as_ref());
        let openid = open_data
            .and_then(|d| d.get("openid"))
            .and_then(Value::as_str)
            .map(str::trim)
            .filter(|s| !s.is_empty())
            .map(str::to_string);
        let unionid = open_data
            .and_then(|d| d.get("unionid"))
            .and_then(Value::as_str)
            .map(str::trim)
            .filter(|s| !s.is_empty())
            .map(str::to_string);
        let nickname = open_data
            .and_then(|d| d.get("nickname"))
            .and_then(Value::as_str)
            .map(str::to_string);
        let avatar = open_data
            .and_then(|d| d.get("headimgurl"))
            .and_then(Value::as_str)
            .unwrap_or("")
            .to_string();

        // 3. 已注册 → 只更新手机验证状态；否则注册新用户
        let existing = self.repo().find_user_by_mobile(&mobile).await?;
        let (user_id, username) = match existing {
            Some((user_id, username, _status)) => {
                self.repo().set_mobile_validated(user_id).await?;
                (user_id, username)
            }
            None => {
                let closed = self
                    .repo()
                    .config_value("shopRegClosed")
                    .await?
                    .and_then(|v| parse_switch(Some(&v)))
                    .unwrap_or(SWITCH_OFF);
                if closed != SWITCH_OFF {
                    return Err(Error::biz(SERVICE_DATA_ERROR, REGISTER_CLOSED));
                }
                let username = self.generate_unique_username().await?;
                let nickname = nickname
                    .clone()
                    .unwrap_or_else(|| auto_nickname(None, &username));
                let hash = hash_password(password)?;
                let user_id = self
                    .repo()
                    .insert_registered_user(
                        &username,
                        &nickname,
                        &avatar,
                        &mobile,
                        &hash,
                        i32::try_from(now_seconds()).unwrap_or(0),
                        dto.referrer_user_id.unwrap_or(0),
                    )
                    .await?;
                (user_id, username)
            }
        };

        // 4. 微信授权绑定（Java `bindMobile` 的 user_authorize upsert 三支）
        if let Some(openid) = openid.as_deref() {
            let authorize_type = authorize_type_from_client(client_type);
            let open_data_json = open_data.map(|d| {
                let mut cleaned = d.clone();
                // Java 为了避免特殊字符把 nickname 从 open_data 里移除
                cleaned.remove("nickname");
                Value::Object(cleaned).to_string()
            });
            let open_name = nickname.clone().unwrap_or_default();
            let now = i32::try_from(now_seconds()).unwrap_or(0);
            if let Some((authorize_id, _owner)) = self
                .repo()
                .find_authorize_by_open(openid, authorize_type)
                .await?
            {
                self.repo()
                    .update_authorize_user(
                        authorize_id,
                        user_id,
                        unionid.as_deref(),
                        open_data_json.as_deref(),
                    )
                    .await?;
            } else if let Some((authorize_id, _)) =
                self.repo().find_authorize_by_user(user_id).await?
            {
                self.repo()
                    .update_authorize_open(
                        authorize_id,
                        openid,
                        unionid.as_deref(),
                        open_data_json.as_deref(),
                    )
                    .await?;
            } else {
                self.repo()
                    .insert_authorize(
                        authorize_type,
                        user_id,
                        openid,
                        open_data_json.as_deref(),
                        &open_name,
                        &avatar,
                        unionid.as_deref(),
                        now,
                    )
                    .await?;
            }
        }

        // 5. 签发会员访问令牌（与 `api/public/src/lib.rs::user_login` 同口径）
        self.issue_member_token(user_id, &username)
    }

    /// `GET /api/user/login/unbindWechat`（需登录）。
    ///
    /// Java `UserServiceImpl#unbindWechat`：当前会员必须已绑定公众号，
    /// 按 openid 删除 `user_authorize` 记录。
    ///
    /// # Errors
    /// 未绑定 → 「该账号未绑定微信公众号」；数据库失败 → `Error::Database`。
    pub async fn unbind_wechat(&self, user_id: i32) -> Result<()> {
        let Some((_, open_id)) = self.repo().find_authorize_by_user(user_id).await? else {
            return Err(Error::biz(SERVICE_DATA_ERROR, WECHAT_NOT_BOUND));
        };
        if open_id.trim().is_empty() {
            return Err(Error::biz(SERVICE_DATA_ERROR, WECHAT_NOT_BOUND));
        }
        self.repo().delete_authorize_by_open(&open_id).await?;
        Ok(())
    }

    // ————————————————————— 快捷登录 / 微信登录（部分未迁移） —————————————————————

    /// `GET /api/user/login/getQuickLoginSetting`。
    ///
    /// Java `ConfigServiceImpl#getQuickLoginSetting`：按 `x-client-type` 决定
    /// `wechatLogin` / `showOauth`（`wechat`/`pc` 取 `openWechatOauth`，
    /// `miniProgram` 恒 1，其它 0）。
    ///
    /// # Errors
    /// 数据库失败 → `Error::Database`。
    pub async fn quick_login_setting(
        &self,
        client_type: Option<&str>,
    ) -> Result<QuickLoginSettingVO> {
        let oauth = self
            .repo()
            .config_value("openWechatOauth")
            .await?
            .as_deref()
            .and_then(|v| parse_switch(Some(v)))
            .unwrap_or(SWITCH_OFF);
        let (wechat_login, show_oauth) = quick_login_flags(client_type, oauth);
        Ok(QuickLoginSettingVO {
            wechat_login,
            show_oauth,
        })
    }

    /// `GET /api/user/login/getWxLoginUrl?url=`：微信授权 URL。
    ///
    /// Java `ConfigServiceImpl#getWechatLoginUrl`：
    /// - `x-client-type=wechat` → 拼 `buildAuthorizationUrl(redirectUrl, snsapi_userinfo, state)`
    ///   并把 `state` 缓存 300 秒；
    /// - `x-client-type=pc` → 走微信「创建二维码」API（未迁移 → 明确 1001）。
    ///
    /// # Errors
    /// 未配置 `wechatAppId`、非 wechat 客户端、缓存不可用 → 1001 业务错误。
    pub async fn wx_login_url(
        &self,
        client_type: Option<&str>,
        redirect_url: Option<&str>,
    ) -> Result<WechatLoginUrlVO> {
        if !client_type.is_some_and(|c| c.eq_ignore_ascii_case("wechat")) {
            return Err(Error::biz(SERVICE_DATA_ERROR, WECHAT_NOT_MIGRATED));
        }
        let app_id = self
            .repo()
            .config_value("wechatAppId")
            .await?
            .filter(|v| !v.trim().is_empty())
            .ok_or_else(|| Error::biz(SERVICE_DATA_ERROR, "微信 APPID 未配置"))?;
        let state = uuid::Uuid::new_v4().simple().to_string();
        self.cache()?
            .set_str(&wechat_state_key(&state), "valid", 300)
            .await?;
        let redirect = percent_encode_uri(redirect_url.unwrap_or(""));
        let url = format!(
            "https://open.weixin.qq.com/connect/oauth2/authorize?appid={app_id}&redirect_uri={redirect}&response_type=code&scope=snsapi_userinfo&state={state}#wechat_redirect"
        );
        Ok(WechatLoginUrlVO { url, ticket: None })
    }

    /// `GET|POST /api/user/login/wechatServer`：微信公众号服务器消息回调。
    ///
    /// **未迁移**：Java `UserServiceImpl#wechatServer` 依赖微信公众号消息路由
    /// （验签 + XML 消息处理 + 扫码事件写 Redis）。
    ///
    /// # Errors
    /// 恒返回 1001 业务错误。
    pub fn wechat_server(&self) -> Result<()> {
        Err(Error::biz(SERVICE_DATA_ERROR, WECHAT_SERVER_NOT_MIGRATED))
    }

    /// `POST /api/user/login/wechatEvent`：扫码后前端轮询登录结果。
    ///
    /// **未迁移**：依赖 `wechatServer` 写入的 openid 缓存与自动注册链路。
    ///
    /// # Errors
    /// 恒返回 1001 业务错误。
    pub async fn wechat_event(&self, _dto: &tigshop_core::models::member::login::WechatEventDTO) -> Result<LoginWechatEventVO> {
        Err(Error::biz(SERVICE_DATA_ERROR, WECHAT_NOT_MIGRATED))
    }

    /// `POST /api/user/login/getMobile`：小程序获取手机号。
    ///
    /// **未迁移**：依赖微信 `getuserphonenumber` API。
    ///
    /// # Errors
    /// 恒返回 1001 业务错误。
    pub async fn get_mobile(&self, _dto: &CodeParamDTO) -> Result<String> {
        Err(Error::biz(SERVICE_DATA_ERROR, WECHAT_NOT_MIGRATED))
    }

    /// `POST /api/user/login/updateUserOpenid`（需登录）：小程序 code2Session 绑定 openid。
    ///
    /// **未迁移**：依赖微信 `code2Session` API。
    ///
    /// # Errors
    /// 恒返回 1001 业务错误。
    pub async fn update_user_openid(&self, _dto: &CodeParamDTO, _user_id: i32) -> Result<()> {
        Err(Error::biz(SERVICE_DATA_ERROR, WECHAT_NOT_MIGRATED))
    }

    /// `GET /api/user/login/getWxLoginInfoByCode`：公众号 OAuth code 换用户信息。
    ///
    /// **未迁移**：依赖微信 OAuth2 API。
    ///
    /// # Errors
    /// 恒返回 1001 业务错误。
    pub async fn wx_login_info_by_code(&self, _code: Option<&str>) -> Result<WxLoginInfoByCodeVO> {
        Err(Error::biz(SERVICE_DATA_ERROR, WECHAT_NOT_MIGRATED))
    }

    /// `POST /api/user/login/bindWechat`（需登录）：公众号授权绑定。
    ///
    /// **未迁移**：openid 需由微信 OAuth code 换取。
    ///
    /// # Errors
    /// 恒返回 1001 业务错误。
    pub async fn bind_wechat(
        &self,
        _dto: &tigshop_core::models::member::login::BindWechatParam,
        _user_id: i32,
    ) -> Result<()> {
        Err(Error::biz(SERVICE_DATA_ERROR, WECHAT_NOT_MIGRATED))
    }

    // ————————————————————— 内部 —————————————————————

    /// 缓存句柄（Redis 不可用时给出明确 1001，而不是伪造成功）。
    fn cache(&self) -> Result<&CacheService> {
        self.cache
            .as_ref()
            .ok_or_else(|| Error::biz(SERVICE_DATA_ERROR, CACHE_UNAVAILABLE))
    }

    /// 生成唯一用户名（Java `generateUniqueUsername`：随机串 + 冲突重试）。
    async fn generate_unique_username(&self) -> Result<String> {
        for _ in 0..5 {
            let candidate = format!(
                "u{}",
                &uuid::Uuid::new_v4().simple().to_string()[..12]
            );
            if !self.repo().username_exists(&candidate).await? {
                return Ok(candidate);
            }
        }
        Err(Error::biz(
            SERVICE_DATA_ERROR,
            "生成唯一用户名失败，请稍后重试",
        ))
    }

    /// 会员访问令牌（`sub=userId` / `usr=username` / `typ="member"`）。
    fn issue_member_token(&self, user_id: i32, username: &str) -> Result<String> {
        let (access, _refresh, _expires_in) = self.jwt.issue_pair(
            user_id.to_string(),
            username.to_string(),
            None,
            Some(MEMBER_TOKEN_TYPE.to_string()),
        )?;
        Ok(access)
    }
}

/// 必填身份（手机号/邮箱）：Java `StrUtil.isBlank(identity)` → 文案。
pub fn require_identity<'a>(value: Option<&'a str>, message: &str) -> Result<&'a str> {
    value
        .map(str::trim)
        .filter(|v| !v.is_empty())
        .ok_or_else(|| Error::biz(SERVICE_DATA_ERROR, message))
}

/// 生成 6 位数字验证码（Java `RandomUtil.randomInt(100000, 999999)`）。
#[must_use]
pub fn generate_sms_code() -> String {
    let n = uuid::Uuid::new_v4().as_u128() % 900_000 + 100_000;
    n.to_string()
}

/// 生成 32 字节随机重置令牌：64 位小写十六进制
/// （Java `SecureRandom.nextBytes(32)` + `HexFormat.of().formatHex`）。
#[must_use]
pub fn generate_reset_token() -> String {
    let a = uuid::Uuid::new_v4().simple().to_string();
    let b = uuid::Uuid::new_v4().simple().to_string();
    format!("{a}{b}")
}

/// MD5 小写十六进制（替代 Java `SecureUtil.sha256`，见模块文档「已知差异」）。
#[must_use]
pub fn md5_hex(input: &str) -> String {
    let mut hasher = Md5::new();
    hasher.update(input.as_bytes());
    hex::encode(hasher.finalize())
}

/// 解析配置开关：Java `Integer.parseInt(bizVal)`（空 → `None`，非法 → `None`）。
#[must_use]
pub fn parse_switch(raw: Option<&str>) -> Option<i32> {
    raw.map(str::trim).filter(|v| !v.is_empty())?.parse().ok()
}

/// 简化版 URI 组件编码（Java `buildAuthorizationUrl` 会对 redirect_uri 编码）。
///
/// 保留 RFC 3986 unreserved 字符，其余按 `%XX` 编码（UTF-8）。
#[must_use]
pub fn percent_encode_uri(input: &str) -> String {
    let mut out = String::with_capacity(input.len());
    for b in input.bytes() {
        let keep = b.is_ascii_alphanumeric() || matches!(b, b'-' | b'_' | b'.' | b'~');
        if keep {
            out.push(b as char);
        } else {
            out.push_str(&format!("%{b:02X}"));
        }
    }
    out
}

/// 未迁移能力判定（供路由/文档对齐；纯函数便于测试）。
#[must_use]
pub fn is_unmigrated_wechat_endpoint(handler: &str) -> bool {
    matches!(
        handler,
        "wechatServer"
            | "wechatEvent"
            | "getMobile"
            | "updateUserOpenId"
            | "getWxLoginInfoByCode"
            | "bindWechat"
    )
}

/// 让错误类型与 Java 的 `GlobalException` 默认 code 对齐（1001）的便捷构造。
#[must_use]
pub fn biz_error(message: &'static str) -> Error {
    Error::biz(SERVICE_DATA_ERROR, message)
}

#[cfg(test)]
mod tests {
    use super::*;
    use tigshop_core::models::member::login::CODE_INVALID;

    #[test]
    fn md5_hex_matches_known_vector() {
        // RFC 1321: md5("abc") = 900150983cd24fb0d6963f7d28e17f72
        assert_eq!(md5_hex("abc"), "900150983cd24fb0d6963f7d28e17f72");
        assert_eq!(md5_hex(""), "d41d8cd98f00b204e9800998ecf8427e");
    }

    #[test]
    fn generated_codes_and_tokens_have_java_shapes() {
        for _ in 0..20 {
            let code = generate_sms_code();
            assert_eq!(code.len(), 6, "{code}");
            assert!(code.bytes().all(|b| b.is_ascii_digit()));
            let n: i32 = code.parse().unwrap();
            assert!((100_000..=999_999).contains(&n));
        }
        for _ in 0..20 {
            let token = generate_reset_token();
            assert_eq!(token.len(), 64);
            assert!(token
                .bytes()
                .all(|b| b.is_ascii_digit() || (b'a'..=b'f').contains(&b)));
        }
    }

    #[test]
    fn switch_parsing_matches_java_integer_parse_behavior() {
        assert_eq!(parse_switch(Some("1")), Some(1));
        assert_eq!(parse_switch(Some("0")), Some(0));
        assert_eq!(parse_switch(Some("  1 ")), Some(1));
        assert_eq!(parse_switch(Some("")), None);
        assert_eq!(parse_switch(None), None);
        assert_eq!(parse_switch(Some("abc")), None);
    }

    #[test]
    fn percent_encoding_matches_oauth_redirect_requirement() {
        assert_eq!(
            percent_encode_uri("https://a.com/cb?x=1&y=中"),
            "https%3A%2F%2Fa.com%2Fcb%3Fx%3D1%26y%3D%E4%B8%AD"
        );
        assert_eq!(percent_encode_uri(""), "");
        assert_eq!(percent_encode_uri("abc-_.~"), "abc-_.~");
    }

    #[test]
    fn error_helpers_use_business_code_1001() {
        let e = biz_error(CODE_INVALID);
        match e {
            Error::Biz { code, msg } => {
                assert_eq!(code, 1001);
                assert_eq!(msg, "验证码错误或已过期，请重试");
            }
            other => panic!("应为业务错误: {other:?}"),
        }
        let e = require_identity(Some("  "), MOBILE_REQUIRED).unwrap_err();
        assert!(e.to_string().contains(MOBILE_REQUIRED));
        assert_eq!(require_identity(Some(" 138 "), MOBILE_REQUIRED).unwrap(), "138");
    }

    #[test]
    fn unmigrated_wechat_endpoints_are_exactly_the_six_java_flows() {
        for h in [
            "wechatServer",
            "wechatEvent",
            "getMobile",
            "updateUserOpenId",
            "getWxLoginInfoByCode",
            "bindWechat",
        ] {
            assert!(is_unmigrated_wechat_endpoint(h), "{h}");
        }
        for h in ["bindMobile", "unbindWechat", "checkMobile", "getWxLoginUrl"] {
            assert!(!is_unmigrated_wechat_endpoint(h), "{h}");
        }
    }
}
