# 在线支付渠道协议骨架（待商户凭据联调）

> 目标：在**没有沙箱商户凭据**的情况下完成协议层实现并用单测锁定；
> 拿到凭据后只差“调接口 + 联调”，不需要重写协议。
> 线下汇款与余额支付已完整可用（见 `docs/PAYMENT-UPLOAD-VERIFY.md`）。

## 1. 已实现（可单测验证）

| 渠道 | 能力 | 位置 |
|---|---|---|
| **微信支付 v3** | `WECHATPAY2-SHA256-RSA2048` 请求签名、回调 SHA256-RSA 验签、回调 `AES-256-GCM` 解密（校验 32 字节 APIv3 密钥 / 12 字节 nonce）、签名串格式 | `services/src/payment/channels.rs` |
| **支付宝** | 待签名串生成（按 key 排序、跳过 `sign/sign_type/空值`）、RSA2（SHA256withRSA）签名、异步通知 RSA2 验签、`trade_status` 判定 | 同上 |
| **PayPal** | 配置校验、sandbox/production 网关、OAuth2 token URL | 同上 |
| 支付回调路由 | `POST /api/order/pay/notify/{payType}` 已改为**先验签再落库**：微信（验签+解密）→ 取 `out_trade_no` 标记 paylog 已支付，返回 `{"code":"SUCCESS"}`；支付宝 → 验签后返回 `success`；未知渠道返回明确错误 | `api/public/src/routes/payment/paylog.rs` |
| 下单入口 | `GET /api/order/pay/create?type=wechat/alipay/paypal` 现在会读取配置并给出**缺哪几项**的可操作错误；配置齐全时返回「协议骨架已就绪，待沙箱联调」 | `services/src/payment/pay.rs` |

单元测试（`cargo test -p tigshop-services channels`，11 项全过）覆盖：
- 微信签名串 / Authorization 头格式；
- RSA 签名→验签 roundtrip（含篡改失败）；
- AES-256-GCM 回调解密 roundtrip（密钥/nonce 长度校验）；
- 支付宝排序待签名串、RSA2 签名验签（含篡改失败）；
- 渠道配置缺失检测、PayPal 网关地址。

## 2. 配置项（设置 → 支付设置，存 `config` 表）

### 微信支付 v3
| 配置项 | 说明 |
|---|---|
| `wechatPayMchid` | 商户号 |
| `wechatPayAppId` | 应用 AppID |
| `wechatPaySerialNo` | 商户 API 证书序列号 |
| `wechatPayPrivateKey` | 商户私钥（PKCS#8 PEM 内容） |
| `wechatPayPublicKey` | 微信支付公钥 / 平台证书（PEM） |
| `wechatPayPublicKeyId` | 微信支付公钥 ID |
| `wechatPayKey` | APIv3 密钥（32 字节） |

### 支付宝
| 配置项 | 说明 |
|---|---|
| `alipayAppid` | 应用 AppID |
| `alipayRsaPrivateKey` | 应用私钥（PKCS#8 PEM） |
| `alipayRsaPublicKey` | 支付宝公钥（PEM） |

### PayPal
| 配置项 | 说明 |
|---|---|
| `paypalClientId` / `paypalClientSecret` | REST 应用凭据 |
| 网关 | 当前默认 sandbox；生产切换 `PaypalConfig.sandbox=false` |

## 3. 实测（无凭据状态）

```
GET /api/order/pay/create?id=1&type=wechat
→ 1001 微信支付未配置：缺少 wechatPayMchid/wechatPayAppId/wechatPaySerialNo/wechatPayKey（设置-支付设置）

GET /api/order/pay/create?id=1&type=alipay
→ 1001 支付宝未配置：缺少 alipayAppid/alipayRsaPrivateKey/alipayRsaPublicKey（设置-支付设置）

POST /api/order/pay/notify/wechat（伪造报文）
→ 1001 微信支付未配置：缺少 wechatPayKey（APIv3 密钥）   ← 不再信任裸 JSON 回调

POST /api/order/pay/notify/stripe
→ 1001 支付渠道 stripe 回调验签待凭据联调（协议骨架已就绪）
```

## 4. 拿到凭据后的剩余工作（按渠道）

1. **微信 v3**：JSAPI/APP/Native 下单（`POST /v3/pay/transactions/*`，用 `wechat_sign` 生成 Authorization）、
   查单、退款、退款回调；`create` 返回 `payParams`（prepay_id/签名参数）。
2. **支付宝**：`alipay.trade.page.pay` / `wap.pay` / `app.pay` 表单生成（`alipay_rsa2_sign` 已就绪）、
   退款、对账单下载。
3. **PayPal**：OAuth2 token → `/v2/checkout/orders` → capture → webhook 验签（调用 PayPal verify-webhook-signature API）。
4. 汇付/拉卡拉/易宝/云支付/YaBand/Apple Pay 等：参考现有 `*_config` 服务与 Java 签名工具类，
   复用 `channels.rs` 的 RSA/哈希工具。

## 5. 安全说明

- 回调路由已**不再信任未验签的 JSON**：验签失败/缺配置直接 1001，不会误标支付成功；
- 微信签名串使用请求实际路径（`OriginalUri`），与渠道签名规则一致；
- 支付密钥仅存数据库 `config` 表（与 Java 一致），日志不打印密钥内容。
