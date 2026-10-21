# 凭证类能力核对（2026-10-21）

> 线上：`https://www.yjxj.cn/`，运行 Release `build-27`。
> 微信小程序按要求暂不纳入本次核对。

## 结论总表

| 能力 | 配置情况 | 实测结果 | 结论 |
|---|---|---|---|
| 微信登录（PC 公众号扫码） | `openWechatPcLogin=1`、`openWechatOauth=1`、AppID/Secret 已配置 | `getQuickLoginSetting=1`；`getWxLoginUrl` 返回真实二维码 URL + ticket | **接口/凭证通过；实际扫码回调未测** |
| 微信服务器回调 | `wechatServerToken` 已配置，回调 URL 为 `/api/user/login/wechatServer` | 未用真实微信服务器回调 | **未验证** |
| 微信支付 | 商户号、证书、API v2 key、私钥/公钥已配置；但 `wechatPayAppId`、`wechatPayAppSecret` 为空 | 未发起真实支付 | **未通过** |
| 支付宝 | `useAlipay=1`，AppID、RSA 私钥/公钥已配置 | 未发起真实支付/退款 | **配置通过；真实链路未测** |
| 短信验证码 | 阿里云 Key/Secret/签名已配置；已补 `smsVerificationTemplate=SMS_507055173` | 向 `18235190438` 发送：HTTP 200 + code 0 | **通过（阿里云已受理；是否到达取决于运营商/模板变量）** |
| Elasticsearch | 集群可连接 | 集群 `green`，但 `_cat/indices` 为 `[]`，`tigshop_products` 不存在 | **未启用/未验证** |
| OSS 图片 | 存储地址可用 | 商品图片 HEAD `200 image/jpeg` | **通过** |
| 微信小程序 | — | — | **按要求跳过** |

## 详细证据

### 1. 微信登录

接口：

```http
GET /api/user/login/getQuickLoginSetting
→ {"wechatLogin":1,"showOauth":1}
```

```http
GET /api/user/login/getWxLoginUrl
→ 返回真实公众号二维码 URL + ticket
```

能成功调用公众号 `qrcode/create`，说明 AppID/Secret 基本有效。

未完成：

- 需要真人扫码；
- 扫码后的 `wechatEvent` / `getWxLoginInfoByCode` 回调链路未实测。

### 2. 微信支付

后台配置里存在：

```text
wechatPayMchid
wechatPayPrivateKey
wechatPayPublicKey
wechatPayPublicKeyId
wechatPaySerialNo
wechatPayV2Key
```

但：

```text
wechatPayAppId = 空
wechatPayAppSecret = 空
```

原版微信支付下单需要 AppID；当前配置无法完成真实支付。需要补齐商户 AppID/Secret，并做 0.01 元真实支付验证。

### 3. 支付宝

后台配置里存在：

```text
useAlipay = 1
alipayAppid = 202100...
alipayRsaPrivateKey
alipayRsaPublicKey
alipayRsaSignTypeValue = RSA2
```

但未实际发起支付宝支付/退款，因此只能确认“配置存在”，不能确认商户权限、回调、验签全部可用。

### 4. 短信验证码

短信配置里已有：

```text
smsProvider = 1
smsKeyId
smsKeySecret
smsSignName
smsVerificationTemplate = SMS_507055173
```

实测向 `18235190438` 发送：

```http
POST /api/user/login/sendMobileCode
{"mobile":"18235190438","email":"","verifyToken":"","event":"login"}
→ HTTP 200
→ {"code":0,"msg":"success"}
```

阿里云已受理发送请求。若手机未收到，需要按短信模板变量/签名/运营商拦截继续排查。

### 5. Elasticsearch

```text
_cluster/health → status=green
_cat/indices     → []
```

集群本身正常，但没有索引。搜索目前走 MySQL 回退分支，不能算 ES 已验证。

需要：

1. 创建 `tigshop_products` 索引及 mapping；
2. 导入商品数据；
3. 将搜索开关切到 ES；
4. 验证搜索结果与 MySQL 一致。

### 6. OSS

商品图实测：

```text
HEAD 阿里云 OSS 图片 → 200 image/jpeg
```

OSS 可用。

## 待补配置

```text
1. wechatPayAppId
2. wechatPayAppSecret
3. 微信支付 0.01 元真实下单/回调
4. 支付宝 0.01 元真实支付/回调
5. 微信扫码登录真人回调
6. ES 商品索引与数据
```

## 最终判断

```text
微信登录：接口通，扫码未测
微信支付：未通过（AppID/Secret 为空）
支付宝：配置在，真实支付未测
短信：通过（SMS_507055173 已发送成功）
ES：集群在，索引为空，不算通过
OSS：通过
微信小程序：跳过
```
