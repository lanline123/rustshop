# 交接文档（HANDOVER）

> **最新接手文档**：本文部分内容已过时；请先读 [`NEXT-AI-HANDOFF.md`](./NEXT-AI-HANDOFF.md)
> （2026-10-19 基线、未提交改动、剩余 Wave 任务、验收命令与已知坑）。本文保留环境/评审/历史背景。
>
> 更新时间：2026-10-19 · 代码基线：`rust/` @ 最新
> 当前环境：MySQL 8.4（`D:\tigshop-mysql\data`，库 `tigshop-java`）+ Rust 服务（8080/8181）
> 能力总览：[`CAPABILITY-MATRIX.md`](./CAPABILITY-MATRIX.md)；运行手册：[`RUN-RUST-FRONTEND.md`](./RUN-RUST-FRONTEND.md)

## 1. 环境与运行

| 项 | 值 |
|---|---|
| 管理端 / 商城端 | `http://127.0.0.1:8080/adminapi/*`、`http://127.0.0.1:8181/api/*` |
| 健康检查 | `/adminapi/health`、`/api/health`（均返回 `{"code":0,"data":"UP"}`） |
| 测试账号 | `admin/admin123`（全权限）、`testbuyer/admin123`（会员，余额 950）、`rbac_op/admin123`、`rbac_op2/admin123` |
| 测试数据种子 | `rust/tools/compat/seed_test_member.sql`、`seed_rbac_users.sql` |
| Redis | 可选；缺失自动降级进程内内存缓存（单实例） |
| 定时任务 | 随服务启动自动运行；`TIG_SCHEDULER__ENABLED=false` 可关闭 |

重启方式：

```powershell
# MySQL（当前为手工实例，无 Windows 服务）
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" `
  --basedir="C:\Program Files\MySQL\MySQL Server 8.4" `
  --datadir="D:\tigshop-mysql\data" --port=3306 --console --mysqlx=0

# Rust 服务（必须在 rust/ 目录下执行，依赖 config.yaml 与 ./uploads）
cd rust
cargo build -p tigshop-server
$env:RUST_LOG="info,tigshop=info,sqlx=warn"
./crates/target/debug/tigshop.exe
```

> ⚠️ 重新导入 dump 后必须执行 `python tools/compat/mysql_signed_schema.py gen/apply`（UNSIGNED→SIGNED），否则 sqlx 解码报错。

## 2. 需要你接手 / 评审

### 2.1 外部凭据联调（拿到凭据才能做完）

| 渠道 | 需要你提供 | 现状 / 用途 | 文档 |
|---|---|---|---|
| 微信支付 v3 | 商户号、AppID、API 证书序列号、商户私钥、微信支付公钥/平台证书、APIv3 密钥 | 协议已实现（RSA 签名/验签、AES-GCM 回调解密）；缺下单/退款/查单接口联调 | `PAYMENT-CHANNELS.md` |
| 支付宝 | 应用 AppID、应用私钥、支付宝公钥 | RSA2 签名/异步通知验签已实现；缺页面/APP 支付与退款 | 同上 |
| PayPal | Client ID / Secret、货币、环境（sandbox/prod） | 配置与网关就绪；缺 OAuth2 下单/webhook | 同上 |
| 微信生态 | 公众号/小程序 AppID+Secret、视频号权限 | OAuth/JSAPI/公众号菜单/客服/小程序码/视频号接口全部未迁移 | `CAPABILITY-MATRIX.md` §3 |
| 短信 | 阿里云 AK/签名/模板 ID（或腾讯云/华为云/火山密钥） | 阿里云已实现；其它厂商待接 | `SMS-EMAIL.md` |
| SMTP | 可用邮箱 SMTP 账号（host/port/user/pass/from） | 邮件链路已实现，实测 SMTP 真实连通 | `SMS-EMAIL.md` |
| 对象存储 | OSS/COS/S3/七牛 AK | 驱动层已实现（AliyunOssStorage/TencentCosStorage/S3Storage），缺业务切入与批量下载/视频截帧 | `CAPABILITY-MATRIX.md` §1 |
| 第三方物流（可选） | 快递100/快递鸟账号 | C 端物流轨迹查询未迁移 | 能力矩阵 §2 |
| ES（可选） | ES 地址 | 商品搜索当前走 SQL；ES 索引/分词/同步未迁移 | 能力矩阵 §5 |
| 机器翻译（可选） | 火山引擎翻译 AK/SK | 多语言/SEO 翻译 51 处引用未迁移 | 能力矩阵 §3 |

### 2.2 需要评审的技术决策

1. **RBAC `all` 语义**：Java 会按 `adminType` 再查 authority 表做 scope 校验；Rust 简化为「`all` = 全部权限」。是否需要严格区分平台/店铺超管？
2. **单实例内存态**：WebSocket 会话表、验证码、登录失败计数、Redis 降级缓存均为进程内。多实例部署前必须切 Redis（WS 广播/计数/验证码共享）。
3. **定时任务 vs MQ**：Java 用 RabbitMQ 延迟队列（订单取消/自动收货/售后/分账等）；Rust 已提供统一 MqMessage 与 in-memory/RabbitMQ 发送总线，单机可轮询降级，集群可上 RabbitMQ。
4. **支付回调响应格式**：微信返回 `{"code":"SUCCESS"}`、支付宝返回纯文本 `success`；需要按各渠道最终联调确认。
5. **前端兼容层字段**：登录同时返回 `token/adminType/accessToken/refreshToken`；分页同时返回 `records/...` 与历史字段；响应体同时有 `msg/message`。属于兼容冗余，后续如需精简需同步前端。
6. **安全默认值**：`config.yaml` 的 `jwt.secret` 仍是默认值、CORS 为 `Any`、上传目录本地；生产前必须替换 secret、收紧 CORS、确认 `TIG_FILE__UPLOAD_DIR`。
7. **错误码映射**：无权限返回 HTTP 403 + `code=1001/参数校验异常`（对齐 Java）；其它错误码需业务确认。
8. **C 端登录**：支持手机号或用户名（Java 仅手机号）；失败 5 次触发滑块验证码。

### 2.3 需要业务确认的口径

1. Excel 模板列与客户实际模板是否一致（库存预警 4 列、商品导出 19 列、出入库 5 列、多商户商品导入 19 列）。
2. 运费/优惠券/积分/满减的计算规则（已支持基础满减折、阶梯运费模板与优惠券分摊，见 CHECKOUT-V2）。
3. `autoCancelOrderMinute`（默认 15 分钟）、`autoDeliveryDays`（自动收货天数）的线上取值。
4. 线下汇款后台确认收款已同步把 `paylog` 状态置为已支付。
5. 多商户租户隔离：JWT 已支持 `shop_id`，商品/订单/商户接口已隔离；会员与营销维度进一步贯穿。

## 3. 已完成与需要处理项

### ✅ 已完成能力

- **多租户 `shop_id` 基础贯穿**：
  - JWT `Claims` 携带 `shop_id`，支持 `access_with_shop` 与 `scoped_shop_id()`；
  - 商品 CRUD、批量修改/删除、订单列表/详情、商户提现/审核均已实施店铺隔离校验；
- **商品批量导入（Excel `calamine` 解析）**：
  - `ProductManageService#batch_modify_scoped` 实现 xlsx 解析、分类智能识别 `resolve_cat_id`、SKU 规格/相册回写；
  - 容器镜像 `Dockerfile` 预置 `template/` 导入模板。
- **存储驱动与消息队列抽象**：
  - `infrastructure/src/storage`：支持 LocalStorage、AliyunOssStorage、TencentCosStorage、S3Storage 并带单测；
  - `infrastructure/src/queue`：支持标准 exchanges/routing_keys、普通与延迟消息，支持 in-memory 降级与 RabbitMQ；
- **运费匹配引擎**：
  - `shipping_list::match_and_calc_freight` 与结算页 `region_match` 精确匹配全国/末级节点；
- **结算页价格/优惠引擎 v2 + v3**：
  - 运费模板、积分抵扣、优惠券店铺范围/多店分摊、账期、预售定金、余额/支付回显；
  - 秒杀活动价、库存扣减与限购；定金预售两阶段支付；阶梯拼团与过期；阶梯/循环满减满折；
- **基础通信与测试**：
  - 管理端测试邮件真实链路；
  - 线下汇款确认同步 paylog 支付态；
  - IM REST + WebSocket 会员↔管理员双向实时推送。

### 🟡 进行中 — 微信生态链路（客户端基础已完成，业务端点待凭据）

已完成：`services/src/content/wechat_client.rs`（access_token 缓存、客服列表/客服文本消息、
小程序 code2session、统一 errcode 处理），客服能力探测已接线
（`GET /adminapi/wechat/keywordRule/capability` 实测未配置 → `kefuEnabled:false`），见 `WECHAT-CLIENT.md`。

待凭据后接：公众号 OAuth/JSAPI 支付/菜单发布/模板消息/服务器消息验签解密；小程序登录/小程序码/订阅消息；
视频号直播房间/推流地址/直播记录/直播商品库/订单同步回调解密与自动建单。

### P1 — 数据与基础设施

- **对象存储接入**：相册批量下载 zip、gallery/IM 视频上传已完成；视频首帧/时长仍需 FFmpeg。
- **ES 搜索**：商品搜索/后台搜索/分词/全量同步（`initAllToEs`）未迁移，当前 SQL 搜索。
- **翻译/i18n**：`translatePackage`/火山翻译未迁移（51 处引用），多语言文案与 SEO 翻译缺失。
- **物流轨迹**：快递100/快递鸟查询未迁移（`user_order.shippingInfo` 返回业务错误）。

### P2 — 交易/资金扩展

- 售后自动收货、退款结算、门店结算、分账调度（`ProfitSharingScheduleTask`）、保证金到期扫描、
  微信直播订单同步等定时/MQ 任务（见 `SCHEDULER.md` §4 未迁移清单；账期更新、商品自动上下架已完成）。
- 库存批量导入（`EXCEL.md` §4 清单）；
  电子卡密导入、商品批量导入、批量发货导入/失败数据/模板、订单导出、对账单导出、库存预警导入已完成。
- 管理操作审计日志：`admin_log` 写入服务 + 请求级审计上下文
  （`tigshop_security::audit` + `require_auth` 注入 actor/IP）；
  Java 已实现调用点均已接入（商品/秒杀/限时折扣/品牌/评论/卡券/物流/优惠券/售后/登录/员工/消息类型/APP 版本等，约 30 个写点）；
  `merchant_apply` 的「新增管理员/员工」链路本身未实现，功能落地时一并补审计。
- IM：已读回执 WS 推送、视频上传已完成；自动回复/MQ 仍待接。

### P2 — 平台能力

- 多租户 `shop_id`：JWT 未签发，部分接口用请求头兜底；会员/订单按店铺隔离不完整。
- RBAC：权限缓存 30s 且无主动失效 hook（改角色后最多 30s 生效）；`all` scope 见 §2.2。
- 验证码/登录失败计数为进程内（多实例需 Redis）；WS token 无法主动吊销（无 Redis 会话）。

## 4. 工程化 / 部署（未完成项）

1. **Docker**：`Dockerfile`、`docker-compose.yml` 已存在但**本环境无 Docker，未验证**。
   compose 把 `../java/database` 整个挂到 MySQL initdb，目录内含大量 `*update.sql` + `demo.sql/pure.sql`，
   直接启动会按字母序执行大量脚本，**需改成只初始化 `pure.sql` 并追加 signed-schema 转换**。
2. **migrations**：`rust/migrations/` 仅有 `001_core_tables.sql`；生产建库仍以 Java dump 为准。
3. **前端部署**：前端未构建 dist，仓库内无 nginx 配置文件（仅 `RUN-RUST-FRONTEND.md` 示例）；
   需实际配 `/adminapi`→8080、`/api`→8181、`/static`→8080、`/ws`→8181（Upgrade 头）。
4. **可观测性**：日志已落 `logs/`；已暴露基础 `/metrics`（存活/版本/uptime），请求量/延迟/DB/调度指标与告警待补。
5. **测试分层**：仅单元测试（约 4300+ 全绿），**缺 DB 集成测试、接口回放/E2E、前端联调脚本**；
   覆盖率 100% 是「路径/方法存在」口径，不等于行为等价。
6. **数据准备**：MySQL 为手工实例（无服务托管）；字符集 utf8mb4；每次重新导入 dump 后要执行 signed 转换；
   测试账号/fixture（订单 1–4、库存预警配置等）残留在 demo 库，交付前建议清理或用 `pure.sql` 重建。
7. **配置安全**：替换 `jwt.secret`、收紧 CORS、设置上传目录/大小限制；确认 `config.yaml` 中 MySQL/Redis 地址与生产一致。

## 5. 验收 / 回归命令

```bash
cd rust/crates && cargo test --workspace          # 全绿
cd rust && python tools/check_api_coverage.py     # 100% / 未实现 0
python tools/check_route_conflicts.py             # 冲突 0
python tools/check_sql_columns.py --strict        # 表/列一致
python tools/check_structure.py --strict          # 结构 PASS
```

冒烟脚本：`tools/compat/captcha_e2e.py`、`ws_smoke.ps1`、`ws_push_smoke.ps1`、`rbac_smoke.ps1`、
`checkout_v2_smoke.ps1`。

## 6. 建议接手顺序

1. 微信支付/支付宝/PayPal 沙箱联调（拿到凭据后，按 `PAYMENT-CHANNELS.md` 补下单/退款）。
2. 对象存储 / ES / 翻译 / 物流轨迹（按业务是否需要云能力）。
3. 微信生态链路（凭据 + HTTP 客户端骨架）。
4. 结算/O2O v3：O2O 配送/自提点、发票随单、地区税率；拼团退款/虚拟成团、预售尾款超时任务。
5. MQ/定时任务扩展、Excel 余量、审计日志、多租户。
6. Docker/nginx/监控/CI 与集成测试，完成可部署交付。
