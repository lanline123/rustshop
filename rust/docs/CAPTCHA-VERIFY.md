# 行为验证码（AJ-Captcha blockPuzzle）联调记录

> 已实测：管理端与 C 端共用同一套 Rust 实现，协议与前端 `components/verifition`（AJ-Captcha Vue）兼容。

## 1. 接口

| 接口 | 说明 |
|---|---|
| `GET/POST /adminapi/common/verification/captcha` | 生成滑块验证码（匿名，`/adminapi/common/verification/**` 在 Java 白名单中） |
| `POST /api/common/verification/captcha` | C 端生成（契约端点，同时兼容 GET） |
| `POST /adminapi/common/verification/check`、`POST /api/common/verification/check` | 校验滑块坐标 |

`captcha` 返回（`data` 内）：

```json
{
  "repCode": 0,
  "repMsg": "",
  "originalImageBase64": "iVBORw0KGgo...(310x155 PNG)",
  "jigsawImageBase64": "iVBORw0KGgo...(47x155 PNG)",
  "token": "6ade0595-...",
  "secretKey": "8tsbKT6mHlsMOLLx",
  "captchaType": "blockPuzzle"
}
```

## 2. 协议细节（与前端一致）

- 前端拖拽后提交 `{captchaType, token, pointJson}`，其中
  `pointJson = AES-128-ECB( JSON({x, y:5.0}) , key=secretKey )`，PKCS7 填充、base64 输出
  （对应前端 `utils/ase.ts` 的 CryptoJS `mode:ECB / padding:Pkcs7`）。
- 服务端校验 `x` 与拼图缺口位置误差 **≤ 5px**；容差、TTL 120s。
- 校验成功后前端会提交给登录接口：
  `captchaVerification = AES(token + "---" + pointJson, secretKey)`；
  登录接口在「失败次数 ≥ 5」时校验该串，**校验成功后一次性失效**（防重放）。
- 状态存储为进程内 `Mutex<HashMap>`（单实例部署）。多实例部署需换成 Redis。

## 3. 登录失败阈值（与 Java `ERROR_THRESHOLD` 对齐）

- 管理端：`POST /adminapi/login/signin`（`verifyToken` 或 `captchaVerification` 字段均可）；
- C 端：`POST /api/user/login/signin`；
- 连续 5 次密码错误后，正确密码也会先返回 **`code=1002`「请完成安全验证」**，
  前端据此弹出滑块；携带有效 `captchaVerification` 后登录成功并清零计数；
- 成功登录自动清零失败计数。

## 4. 实测结果

```
GET  /adminapi/common/verification/captcha   -> 200, PNG 310x155 + PNG 47x155 + token + secretKey
POST /adminapi/common/verification/check     -> 坐标正确 {code:0,data:true}；错误坐标 {code:1001}
5 次错误密码后正确密码                         -> {code:1002,"请完成安全验证"}
携带 AES(captchaVerification) 再登录           -> {code:0,"success"}（计数清零）
C 端同样：5 次错误后 -> 1002
```

自动化脚本（本地联调用，含图片缺口识别 + AES 加密 + 完整登录流程）：
[`tools/compat/captcha_e2e.py`](../tools/compat/captcha_e2e.py)。

## 5. 已知限制

- 单实例内存态；多实例需 Redis 共享（`token/secretKey/失败计数`）。
- 背景图为程序生成的渐变噪点（非图库照片），PNG 响应约 100KB；可通过降低噪点进一步压缩。
- C 端短信/邮件验证码仍为未迁移状态（与滑块验证码是两条链路）。

## 6. 下一步

IM WebSocket（axum `ws` + 会话表）→ 定时任务/MQ → RBAC 权限矩阵。
