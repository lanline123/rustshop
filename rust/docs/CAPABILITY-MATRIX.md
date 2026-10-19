# TigShop Rust 后端能力矩阵（当前状态）

> 目标口径：**前端不改**，Rust 后端可替换原 Java 服务；需要第三方凭据的能力完成“协议 + 配置骨架”，
> 并在本文标注「待凭据联调」。最近一次全量验证：`cargo test --workspace` 全绿、契约覆盖率 100%、
> 路由冲突 0、SQL 列校验通过。

## 1. 运行与兼容层

| 能力 | 状态 | 验证/说明 |
|---|---|---|
| MySQL 运行（demo.sql） | ✅ | 需执行 `tools/compat/mysql_signed_schema.py` 归一化 UNSIGNED→SIGNED |
| Redis | ✅ 可选 | 缺失时自动降级**进程内内存缓存**（验证码/会话可用，单实例） |
| 服务启动（8080/8181） | ✅ | `/adminapi/health`、`/api/health` 200 |
| 登录（admin/C） | ✅ | 返回 `token/adminType`；C 端支持手机号或用户名；失败 5 次触发滑块验证码（1002） |
| 分页/字段兼容 | ✅ | `records/total/size/current/pages`、`size` 参数、`message` 字段 |
| 静态资源 & 本地上传 | ✅ | `/static` 由服务直接提供；nginx 需反代 `/static` |
| 上传（相册图/视频/支付证书/APP 包） | ✅ | 返回 `/static/...`，实测可访问 |

## 2. 交易 / 支付

| 能力 | 状态 | 验证/说明 |
|---|---|---|
| 结算页（订单确认） | ✅ v2 | 运费模板、积分抵扣、优惠券店铺范围/多店分摊、账期、预售定金、改价标记已实现；实测见 `CHECKOUT-V2.md`。剩余：秒杀/拼团/满减活动、O2O、发票随单、税率、定金预售下单 |
| 下单（含地址/运费占位） | ✅ | 修复无 SKU 商品库存扣减、`order_extension` 缺列；地址快照、优惠券抵扣已接入 |
| 余额支付（全额/部分抵扣） | ✅ | 实测：`returnType=2` 全额已付、部分抵扣 `onlineAmount` |
| 线下汇款 + 后台确认收款 | ✅ | `pay/create?type=offline` + `order/order/setPaid` 实测；确认收款同步待支付 paylog → 已支付（0→1） |
| 管理端测试邮件 | ✅ | `setting/config/sendTestEmail` 已接线，实测 SMTP 链路执行（demo 凭据 535，需真实账号） |
| 微信支付 v3 协议 | 🟡 骨架 | RSA 签名/验签/AES-GCM 解密单测通过；下单/退款待商户凭据 |
| 支付宝协议 | 🟡 骨架 | RSA2 签名/验签单测通过；下单待沙箱凭据 |
| PayPal | 🟡 骨架 | 配置/网关/OAuth2 URL 就绪；下单/webhook 待凭据 |
| 汇付/拉卡拉/易宝/云支付/YaBand/Apple Pay | ⛔ 待凭据 | 配置服务存在，签名/下单未实现 |
| 支付回调安全 | ✅ | `notify/{payType}` 已强制验签，未验签报文不再落库 |

## 3. 会员 / 验证码 / 通知

| 能力 | 状态 | 验证/说明 |
|---|---|---|
| AJ-Captcha 滑块验证码 | ✅ | 生成/校验/AES 坐标/一次性二次校验；登录阈值 5 次 |
| 短信验证码 | ✅ 阿里云 | RPC HMAC-SHA1 签名；`TIG_SMS__MOCK=1` 本地联调；其它云厂商待接 |
| 邮件验证码 | ✅ SMTP | lettre 发送；demo 凭据实测返回 535（链路已执行），需真实 SMTP |
| 微信 API 客户端基础 | 🟡 骨架 | access_token 缓存、客服列表/客服文本消息、小程序 code2session、统一 errcode 处理；已接客服能力探测；见 `WECHAT-CLIENT.md` |
| 微信生态（OAuth/JSAPI/公众号/小程序码/视频号） | ⛔ 待凭据 | 客户端基础已就绪，具体业务端点待凭据后逐个接入 |

## 4. IM / 异步任务 / 权限

| 能力 | 状态 | 验证/说明 |
|---|---|---|
| IM WebSocket `/ws` | ✅ | 欢迎语、心跳回显、无效 token 拒绝、服务端推送；IM 消息收发已接 WS 双向推送（实测）；多端会话；进程内表（多实例需 Redis） |
| 定时任务（订单超时取消/自动收货） | ✅ | 轮询 + 幂等条件更新（替代 Java RabbitMQ 延迟队列），可配 `autoCancelOrderMinute`/`autoDeliveryDays` |
| MQ（RabbitMQ） | ⛔ 未使用 | Rust 以轮询定时任务替代延迟队列；多实例可加 Redis 锁或接 `lapin` |
| RBAC 方法级权限 | ✅ | 契约自动生成 864 条权限；`all` 超管；403+code1001 与 Java 一致；30s 缓存 |
| 多租户 shop_id 隔离 | 🟡 部分 | 读请求头兜底，JWT 未签发 shop_id；多店铺隔离不完整 |

## 5. 数据导入导出

| 能力 | 状态 | 验证/说明 |
|---|---|---|
| xlsx 写出/读取基础设施 | ✅ | `rust_xlsxwriter` + `calamine`，单测 roundtrip |
| 库存预警 模板/导出/导入 | ✅ | 实测 xlsx 列头正确；导入成功/失败明细正确 |
| 商品批量导出、出入库模板 | ✅ | 实测 xlsx 下载 |
| 订单导出、批量发货导入、对账单导出、电子卡密导入等 | ⛔ 待补 | 基础设施已就绪，按 `docs/EXCEL.md` §3 模式逐个替换 |

## 6. 文档索引

- 运行/前端部署：`docs/RUN-RUST-FRONTEND.md`
- 交接（凭据/评审/其它待办）：`docs/HANDOVER.md`
- 兼容层与验证：`docs/PAYMENT-UPLOAD-VERIFY.md`、`docs/CAPTCHA-VERIFY.md`
- 基础设施：`docs/IM-WEBSOCKET.md`、`docs/SCHEDULER.md`、`docs/RBAC.md`、`docs/EXCEL.md`、`docs/SMS-EMAIL.md`
- 支付渠道：`docs/PAYMENT-CHANNELS.md`
- 交付物工具：`tools/compat/*`（signed schema、测试会员/RBAC 种子、captcha/ws/rbac 冒烟脚本、权限表生成）

## 7. 回归命令

```bash
cd rust/crates && cargo test --workspace
cd rust && python tools/check_api_coverage.py
python tools/check_route_conflicts.py
python tools/check_sql_columns.py --strict
python tools/check_structure.py --strict
```
