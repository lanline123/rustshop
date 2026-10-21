# 微信 API 客户端基础（协议层，待凭据联调）

> 对应 Java `WxMpService` / `WechatApiClient`。目标是先完成可复用的客户端与缓存，
> 微信商户/公众号凭据到位后逐个接业务端点（视频号、菜单、OAuth、JSAPI 等）。

## 1. 已实现

| 能力 | 接口 | 说明 |
|---|---|---|
| access_token 获取/缓存 | `cgi-bin/token` | 进程内缓存，7000s、提前 60s 过期；`invalidate_token` 可主动失效 |
| 客服列表探测 | `cgi-bin/customservice/getkflist` | 客服能力探测（`kefuEnabled`） |
| 客服文本消息 | `cgi-bin/message/custom/send` | 发送文本给 openid |
| 小程序 code2session | `sns/jscode2session` | code → openid/session_key（微信登录/支付前置） |
| 统一错误处理 | — | 微信 `errcode != 0` → 业务错误 `1001`，保留 errcode/errmsg |

位置：`crates/services/src/content/wechat_client.rs`；配置读取 `config` 表：
`wechatAppId` / `wechatAppSecret` / `wechatServerToken` / `wechatServerSecret`。

## 2. 已接线端点

- `GET /adminapi/wechat/keywordRule/capability`：公众号已配置 → 调客服列表探测；
  网络/微信接口异常按 Java 探测语义降级 `kefuEnabled=false`（实测未配置 → `false`）。

## 3. 实测

```
GET /adminapi/wechat/keywordRule/capability   （wechatAppId 为空）
→ 200 {"kefuEnabled":false}
```

服务层单测（`cargo test -p tigshop-services`）覆盖：配置缺失判定、微信响应成功/errcode 解析、
token 缓存过期、URL 编码。

## 4. 待凭据/待实现

- 公众号：OAuth 网页授权、JSAPI 支付签名、素材/菜单发布（`menu/create`）、模板消息/订阅消息、
  服务器消息验签与解密（`wechatServerToken` / EncodingAESKey）；
- 小程序：登录态换取、小程序码生成、订阅消息；
- 视频号：直播房间/推流地址/直播记录、直播商品库、订单同步回调解密与自动建单；
- 微信支付 v3 下单/退款（独立模块 `services/src/payment/channels.rs` 已有签名/验签/解密基础）。

需要提供：公众号/小程序 AppID+Secret、视频号权限、微信支付商户号与证书（见 `PAYMENT-CHANNELS.md`）。
