# 短信 / 邮件通道（部分实测）

> 对应 Java `SmsUtils`（阿里云/腾讯云/华为云/火山引擎）与 JavaMail/SMTP。
> Rust 侧已实现 **阿里云短信** + **SMTP 邮件**，腾讯云/华为云/火山引擎保留明确未迁移错误。

## 1. 短信

配置读取 `config` 表（设置页「短信配置」）：

| 配置项 | 说明 |
|---|---|
| `smsProvider` | 1=阿里云（已实现）；2/3/4 暂未迁移（返回明确错误） |
| `smsKeyId` / `smsKeySecret` | 阿里云 AccessKey |
| `smsSignName` | 短信签名 |
| `smsVerificationTemplate` 或环境变量 `TIG_SMS__TEMPLATE_CODE` | 验证码模板 Code |

- 校验码模板参数约定：`{"code":"123456"}`（与 Java 模板一致）。
- 签名算法：阿里云 RPC 签名 `HMAC-SHA1`（`SignatureVersion=1.0`），已用单元测试锁定（中文签名 percent-encode、固定输入可复现）。
- 本地联调：`TIG_SMS__MOCK=1` 只记录日志不真正发送（**不得用于生产**）：

```
WARN tigshop_services::system::notify: TIG_SMS__MOCK=1：短信仅记录日志（mobile=13800000001 code=784315）
INFO tigshop_services::member::login: 短信验证码已发送: mobile=13800000001 event=Some("login")
```

- 未配置时返回明确业务错误：`短信通道未配置：请在设置中填写 smsKeyId/smsKeySecret/smsSignName 与短信模板 ID`。
- 接入点：`POST /api/user/login/sendMobileCode`、`POST /adminapi/login/sendMobileCode`（验证码写入缓存后才发送）。

## 2. 邮件

配置读取 `config` 表：`smtpHost` / `smtpPort` / `smtpUser` / `smtpPass` / `smtpMail` / `smtpSsl`。

- 使用 `lettre`（Tokio + native-tls）发送 HTML 验证码邮件，5 分钟有效。
- 接入点：`POST /api/user/login/sendEmailCode`（以及 admin 登录页同源方法）。
- 实测：demo 库中 SMTP 配置指向不可用服务器 → 返回 `邮件发送失败: ... 535 authentication failed`，
  说明链路已真实执行；填入可用 SMTP 账号即可发送。
- 未配置时返回：`邮件服务未配置：请填写 smtpHost/smtpPort/smtpUser/smtpPass/smtpMail`。
- 管理端「发送测试邮件」`setting/config/sendTestEmail` 仍为占位（可复用 `notify::send_email_code` 的同款 SMTP 构建逻辑，下一步接）。

## 3. 配套：Redis 缺失自动降级为内存缓存

验证码/重置令牌依赖缓存；之前 Redis 不存在时直接返回「缓存服务不可用」。
现 `CacheService::init` 在 Redis 连接失败时自动降级为**进程内内存缓存**（带 TTL），单实例开发可直接联调：

```
WARN tigshop_infrastructure::cache: Redis 不可用（Redis 连接失败: timed out），降级为进程内内存缓存（单实例开发模式）
```

- 支持 get/set/del/incr/get_or_load，TTL 过期清理；
- 生产多实例仍必须启用 Redis（登录态/验证码跨实例共享）。

## 4. 下一步

- 腾讯云/华为云/火山引擎短信（签名算法分别为 TC3-HMAC-SHA256 等）；
- 管理端「发送测试邮件」接入；
- 微信生态（OAuth/JSAPI 支付/公众号菜单推送/小程序码/视频号接口）。
