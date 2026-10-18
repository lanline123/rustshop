//! 短信 / 邮件通道（替代 Java `SmsUtils` / JavaMail）。
//!
//! - 短信：读取 `config` 表 `smsProvider` / `smsKeyId` / `smsKeySecret` / `smsSignName`
//!   与模板 ID（`TIG_SMS__TEMPLATE_CODE` 环境变量，或 config `smsVerificationTemplate`），
//!   目前实现**阿里云短信 SendSms**（RPC 签名 HMAC-SHA1）；
//!   `TIG_SMS__MOCK=1` 时仅记录日志（本地联调）。
//! - 邮件：读取 `smtpHost/smtpPort/smtpUser/smtpPass/smtpMail/smtpSsl`，用 `lettre` 发送。
//!
//! 未配置凭据时返回明确业务错误（不伪造发送成功）。
use std::collections::HashMap;

use hmac::{Hmac, Mac};
use sha1::Sha1;
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::Repositories;

type HmacSha1 = Hmac<Sha1>;

const CFG_SMS_PROVIDER: &str = "smsProvider";
const CFG_SMS_KEY_ID: &str = "smsKeyId";
const CFG_SMS_KEY_SECRET: &str = "smsKeySecret";
const CFG_SMS_SIGN_NAME: &str = "smsSignName";
const CFG_SMS_TEMPLATE: &str = "smsVerificationTemplate";
const CFG_SMTP_HOST: &str = "smtpHost";
const CFG_SMTP_PORT: &str = "smtpPort";
const CFG_SMTP_USER: &str = "smtpUser";
const CFG_SMTP_PASS: &str = "smtpPass";
const CFG_SMTP_MAIL: &str = "smtpMail";
const CFG_SMTP_SSL: &str = "smtpSsl";

const SMS_KEYS: [&str; 6] = [
    CFG_SMS_PROVIDER,
    CFG_SMS_KEY_ID,
    CFG_SMS_KEY_SECRET,
    CFG_SMS_SIGN_NAME,
    CFG_SMS_TEMPLATE,
    CFG_SMTP_SSL,
];
const MAIL_KEYS: [&str; 7] = [
    CFG_SMTP_HOST,
    CFG_SMTP_PORT,
    CFG_SMTP_USER,
    CFG_SMTP_PASS,
    CFG_SMTP_MAIL,
    CFG_SMTP_SSL,
    CFG_SMS_TEMPLATE,
];

/// 配置读取（`config` 表）。
async fn load(repos: &Repositories, keys: &[&str]) -> Result<HashMap<String, String>> {
    let pairs = repos.system.config_basic.find_by_codes(keys).await?;
    Ok(pairs.into_iter().collect())
}

fn env_override(map: &mut HashMap<String, String>, key: &str, env: &str) {
    if let Ok(v) = std::env::var(env) {
        if !v.trim().is_empty() {
            map.insert(key.to_string(), v);
        }
    }
}

/// 阿里云短信 RPC 签名（HMAC-SHA1，`SignatureVersion=1.0`）。
///
/// 返回带签名与公共参数的完整 query 字符串（测试可对固定输入断言）。
#[must_use]
pub fn aliyun_signed_query(
    access_key_id: &str,
    access_key_secret: &str,
    params: &[(&str, String)],
) -> String {
    fn percent_encode(input: &str) -> String {
        let mut out = String::with_capacity(input.len());
        for byte in input.as_bytes() {
            match byte {
                b'A'..=b'Z' | b'a'..=b'z' | b'0'..=b'9' | b'-' | b'_' | b'.' | b'~' => {
                    out.push(*byte as char);
                }
                _ => out.push_str(&format!("%{byte:02X}")),
            }
        }
        out
    }

    let mut all: Vec<(String, String)> = params
        .iter()
        .map(|(k, v)| ((*k).to_string(), v.clone()))
        .collect();
    all.push(("AccessKeyId".into(), access_key_id.to_string()));
    all.push(("Format".into(), "JSON".into()));
    all.push(("SignatureMethod".into(), "HMAC-SHA1".into()));
    all.push(("SignatureVersion".into(), "1.0".into()));
    all.push(("Version".into(), "2017-05-25".into()));
    all.push(("RegionId".into(), "cn-hangzhou".into()));
    all.sort_by(|a, b| a.0.cmp(&b.0));

    let canonical = all
        .iter()
        .map(|(k, v)| format!("{}={}", percent_encode(k), percent_encode(v)))
        .collect::<Vec<_>>()
        .join("&");
    let string_to_sign = format!("GET&%2F&{}", percent_encode(&canonical));
    let mut mac = HmacSha1::new_from_slice(format!("{access_key_secret}&").as_bytes())
        .expect("HMAC 支持任意长度 key");
    mac.update(string_to_sign.as_bytes());
    let signature = base64_encode(&mac.finalize().into_bytes());
    format!("{canonical}&Signature={}", percent_encode(&signature))
}

fn base64_encode(bytes: &[u8]) -> String {
    use base64::Engine;
    base64::engine::general_purpose::STANDARD.encode(bytes)
}

/// 发送短信验证码。
///
/// # Errors
/// 未配置短信通道 / 模板缺失 / 网络或网关返回失败。
pub async fn send_sms_code(repos: &Repositories, mobile: &str, code: &str) -> Result<()> {
    if std::env::var("TIG_SMS__MOCK").is_ok_and(|v| v == "1") {
        tracing::warn!("TIG_SMS__MOCK=1：短信仅记录日志（mobile={mobile} code={code}）");
        return Ok(());
    }
    let mut cfg = load(repos, &SMS_KEYS).await?;
    env_override(&mut cfg, CFG_SMS_KEY_ID, "TIG_SMS__KEY_ID");
    env_override(&mut cfg, CFG_SMS_KEY_SECRET, "TIG_SMS__KEY_SECRET");
    env_override(&mut cfg, CFG_SMS_SIGN_NAME, "TIG_SMS__SIGN_NAME");
    env_override(&mut cfg, CFG_SMS_TEMPLATE, "TIG_SMS__TEMPLATE_CODE");

    let provider = cfg.get(CFG_SMS_PROVIDER).map(String::as_str).unwrap_or("1");
    if provider != "1" {
        return Err(Error::biz(
            1001,
            format!("短信服务商（{provider}）暂未迁移，当前支持阿里云（smsProvider=1）"),
        ));
    }
    let key_id = cfg.get(CFG_SMS_KEY_ID).map(String::as_str).unwrap_or_default();
    let key_secret = cfg
        .get(CFG_SMS_KEY_SECRET)
        .map(String::as_str)
        .unwrap_or_default();
    let sign_name = cfg
        .get(CFG_SMS_SIGN_NAME)
        .map(String::as_str)
        .unwrap_or_default();
    let template = cfg.get(CFG_SMS_TEMPLATE).map(String::as_str).unwrap_or_default();
    if key_id.is_empty() || key_secret.is_empty() || sign_name.is_empty() || template.is_empty() {
        return Err(Error::biz(
            1001,
            "短信通道未配置：请在设置中填写 smsKeyId/smsKeySecret/smsSignName 与短信模板 ID",
        ));
    }

    let nonce = uuid::Uuid::new_v4().to_string();
    let timestamp = chrono::Utc::now().format("%Y-%m-%dT%H:%M:%SZ").to_string();
    let params = [
        ("Action", "SendSms".to_string()),
        ("PhoneNumbers", mobile.to_string()),
        ("SignName", sign_name.to_string()),
        ("TemplateCode", template.to_string()),
        ("TemplateParam", format!("{{\"code\":\"{code}\"}}")),
        ("SignatureNonce", nonce),
        ("Timestamp", timestamp),
    ];
    let query = aliyun_signed_query(key_id, key_secret, &params);
    let url = format!("https://dysmsapi.aliyuncs.com/?{query}");
    let client = reqwest::Client::new();
    let resp = client
        .get(url)
        .timeout(std::time::Duration::from_secs(15))
        .send()
        .await
        .map_err(|e| Error::Internal(format!("短信发送失败: {e}")))?;
    let status = resp.status();
    let body = resp.text().await.unwrap_or_default();
    if !status.is_success() {
        return Err(Error::Internal(format!("短信网关返回 {status}: {body}")));
    }
    let json: serde_json::Value = serde_json::from_str(&body).unwrap_or_default();
    if json.get("Code").and_then(serde_json::Value::as_str) == Some("OK") {
        Ok(())
    } else {
        Err(Error::Internal(format!("短信发送失败: {body}")))
    }
}

/// 发送邮件验证码（SMTP）。
///
/// # Errors
/// SMTP 未配置 / 发送失败。
/// 发送测试邮件（管理端「发送测试邮件」按钮）。
///
/// # Errors
/// SMTP 未配置 / 发送失败。
pub async fn send_test_email(repos: &Repositories, to: &str) -> Result<()> {
    send_email(
        repos,
        to,
        "TigShop 测试邮件",
        "<p>这是一封测试邮件，收到说明 SMTP 配置可用。</p>",
    )
    .await
}

/// 发送邮件验证码（SMTP）。
///
/// # Errors
/// SMTP 未配置 / 发送失败。
pub async fn send_email_code(repos: &Repositories, to: &str, subject: &str, code: &str) -> Result<()> {
    send_email(
        repos,
        to,
        subject,
        &format!("<p>您的验证码是：<b>{code}</b>，5 分钟内有效。</p>"),
    )
    .await
}

/// 通用 SMTP 发信（HTML 正文）。
///
/// # Errors
/// SMTP 未配置 / 发送失败。
pub async fn send_email(
    repos: &Repositories,
    to: &str,
    subject: &str,
    html_body: &str,
) -> Result<()> {
    let cfg = load(repos, &MAIL_KEYS).await?;
    let host = cfg.get(CFG_SMTP_HOST).cloned().unwrap_or_default();
    let port = cfg
        .get(CFG_SMTP_PORT)
        .and_then(|v| v.trim().parse::<u16>().ok())
        .unwrap_or(465);
    let user = cfg.get(CFG_SMTP_USER).cloned().unwrap_or_default();
    let pass = cfg.get(CFG_SMTP_PASS).cloned().unwrap_or_default();
    let from = cfg
        .get(CFG_SMTP_MAIL)
        .cloned()
        .filter(|v| !v.trim().is_empty())
        .unwrap_or_else(|| user.clone());
    let ssl = cfg.get(CFG_SMTP_SSL).map(String::as_str).unwrap_or("1") == "1";
    if host.is_empty() || user.is_empty() || pass.is_empty() || from.is_empty() {
        return Err(Error::biz(
            1001,
            "邮件服务未配置：请填写 smtpHost/smtpPort/smtpUser/smtpPass/smtpMail",
        ));
    }

    use lettre::message::header::ContentType;
    use lettre::transport::smtp::authentication::Credentials;
    use lettre::{AsyncSmtpTransport, AsyncTransport, Message, Tokio1Executor};

    let email = Message::builder()
        .from(
            from.parse()
                .map_err(|_| Error::Internal("smtpMail 格式不正确".into()))?,
        )
        .to(to
            .parse()
            .map_err(|_| Error::Validation("邮箱地址不正确".into()))?)
        .subject(subject)
        .header(ContentType::TEXT_HTML)
        .body(html_body.to_string())
        .map_err(|e| Error::Internal(format!("邮件构建失败: {e}")))?;

    let builder = if ssl {
        AsyncSmtpTransport::<Tokio1Executor>::relay(&host)
            .map_err(|e| Error::Internal(format!("SMTP 配置失败: {e}")))?
    } else {
        AsyncSmtpTransport::<Tokio1Executor>::starttls_relay(&host)
            .map_err(|e| Error::Internal(format!("SMTP 配置失败: {e}")))?
    }
    .port(port)
    .credentials(Credentials::new(user, pass));
    builder
        .build()
        .send(email)
        .await
        .map_err(|e| Error::Internal(format!("邮件发送失败: {e}")))?;
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn aliyun_query_is_deterministic_and_contains_signature() {
        let params = [
            ("Action", "SendSms".to_string()),
            ("PhoneNumbers", "13800000001".to_string()),
            ("SignName", "测试签名".to_string()),
            ("TemplateCode", "SMS_123".to_string()),
            ("TemplateParam", "{\"code\":\"123456\"}".to_string()),
            ("SignatureNonce", "fixed-nonce".to_string()),
            ("Timestamp", "2026-01-01T00:00:00Z".to_string()),
        ];
        let q1 = aliyun_signed_query("key-id", "secret", &params);
        let q2 = aliyun_signed_query("key-id", "secret", &params);
        assert_eq!(q1, q2);
        assert!(q1.contains("Signature="));
        assert!(q1.contains("Action=SendSms"));
        // 中文签名必须被 percent-encode
        assert!(q1.contains("%E6%B5%8B%E8%AF%95%E7%AD%BE%E5%90%8D"));
    }

    #[test]
    fn aliyun_signature_differs_with_secret() {
        let params = [("Action", "SendSms".to_string())];
        assert_ne!(
            aliyun_signed_query("k", "s1", &params),
            aliyun_signed_query("k", "s2", &params)
        );
    }
}
