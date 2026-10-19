# 后续开发交接文档（NEXT AI HANDOFF）

> 生成时间：2026-10-19 19:xx（本地时区 Asia/Shanghai）
> 代码基线：仓库 `D:/tigshop/java-b2b2b-pro-v5.8.57`，分支 `master`，最新 HEAD `6ca6fe36`（见 §0.1）
> Rust 根目录：`D:/tigshop/java-b2b2b-pro-v5.8.57/rust`
> 本文目标：让接手者在 30 分钟内跑起来，明确**哪些已完成不要重做、哪些必须做、每项怎么验收**。
> 阅读顺序：本文 → `docs/CAPABILITY-MATRIX.md` → `docs/ARCHITECTURE.md` → 相关 feature 文档。

---

## 0. 一分钟上手（TL;DR）

1. 当前 **MySQL / Rust 服务都没有在运行**（`MySQL84` 服务 Stopped，8080/8181 无监听）；先按 §2 启动。
2. 工作树**不是干净的**：约 **82 个未提交路径**（并行 lane 的「多租户 shop_id + 存储多后端 + MQ 抽象 + 商品批量 + 运费计算」）。**先评审并提交这批改动，再开新工作**（见 §4）。
3. 接口层已 1564/1564 全覆盖（`check_api_coverage` 100%）；剩余是**行为补齐、外部凭据联调、部署工程化**。
4. 按 §5 的 Wave 顺序做；每个 Wave 的验收命令见 §7。
5. 没有把握时以 **Java 源码** 为准：`D:/tigshop/java-b2b2b-pro-v5.8.57/java`；前端在 `java/view`。

### 0.1 最新快照（2026-10-20 00:20，优先阅读，§1–§5 为 19:00 快照）

- **HEAD `f8aa8e64`，工作树干净**；自 `18a8397e` 新增 11 个 commit（审计、IM 已读、订单导出、
  站内信人群、图库 zip、视频、批量发货模板、店铺资金、装修模板代理、商品自动上下架、metrics/Docker、对账单导出）。
- `cargo test --workspace` 全绿；四项门禁 PASS；契约覆盖率 100%（路由口径）。
- **行为占位降到 30 处（7 文件），全部依赖外部服务/凭据**（微信直播/微信登录/ES/买家秀随机资料 API/小程序码）。
- 本轮已完成（无凭据 P0）：
  - 审计日志：请求级 actor/IP（`tigshop_security::audit`）+ Java 已实现调用点约 30 个写点；
  - IM：已读回执 WS 双向推送、视频上传（本地存储）；
  - 交易：订单导出真实 xlsx、批量发货模板/失败数据 xlsx、代客配送（已有实现清理）；
  - 会员：站内信按标签/统一人群结构圈选；会员消息日志人群解析；
  - 内容：图库批量 zip 下载、gallery 视频树/排序/移动、公众号菜单本地落库、装修模板 OpenTemplate 代理；
  - 商户：店铺资金 create（`shop` 入库）；
  - 定时：商品自动上下架循环（`product_auto_status`）；
  - Excel：库存预警导入/失败数据 xlsx、对账单/统计导出 xlsx；
  - 工程化：`/metrics`（Prometheus 文本）、Docker MySQL init 改为转换后的 `pure.sql`。
- **当前真正剩余（按优先级）**：
  1. 外部凭据/服务：微信直播 13、微信登录 7、ES 分词/全量同步、买家秀虚拟用户外部随机资料、小程序码/scene 2。
  2. 定时/MQ：保证金到期、分账调度、微信直播订单同步、售后自动收货/退款结算/自动评价、
     门店/分销/供应商结算、ES 同步重试。
  3. Excel 余量：商品库存批量导入/调拨（8 个端点）、会员/客户导入导出。
  4. 结算/活动残留：代客整单改价金额、定金尾款超时/取消、拼团退款/虚拟成团/限购、
     活动会员人群/满赠、限时折扣（`time_discount`）接入结算。
  5. IM 自动回复/MQ；物流平台第三方轨迹（本地兜底轨迹已实现）。
  6. 多租户/权限：会员与营销维度继续贯穿；RBAC `all` scope、权限缓存主动失效。
  7. 工程化：前端 dist、DB 集成/E2E、生产密钥/CORS/Redis 多实例；Docker 初始化与 nginx 参考配置已提供，需在有 Docker 环境实测。
- **任务板**：pending/in_progress 已清零（历史 Wave 任务按 feature 覆盖率 100% 关闭）。
- **外部凭据**（微信支付/支付宝/PayPal/微信生态/短信厂商/对象存储/ES/翻译）仍按 §5 表 A 处理。
---

## 1. 当前基线（19:00 快照）

| 项 | 值 / 状态 |
|---|---|
| git HEAD | `6ca6fe36`（19:00 快照为 559b6685；见 §0.1） |
| git 根目录 | `D:/tigshop/java-b2b2b-pro-v5.8.57`（Rust 在 `rust/` 子目录，提交路径带 `rust/` 前缀） |
| 未提交改动 | **0（工作树干净）**；原 82 个改动已提交（见 §0.1） |
| 编译 | `cargo check -p tigshop-server` / `cargo test --workspace` 通过（2026-10-19 21:20 实测） |
| 测试 | `cargo test --workspace` 通过（同上） |
| 门禁 | `check_api_coverage` 100%（未实现 0）、路由冲突 0、SQL 列 PASS、结构 PASS |
| 服务 | 未运行；二进制 `rust/crates/target/debug/tigshop.exe` 存在 |
| MySQL | 服务 `MySQL84` 停止；数据目录实际为 `D:/tigshop-mysql/data`（注意：旧 `HANDOVER.md` 写成 `D:/igshop-mysql/data`，是错的） |
| 磁盘 | D 盘约 23.8GB 可用；`crates/target` 约 73.7GB（大量 build 日志在 `rust/` 根目录，已被 gitignore） |

> **口径提醒**：`check_api_coverage` 的 100% 是「路由/方法存在」口径，不代表行为等价。
> 行为缺口以 `docs/CAPABILITY-MATRIX.md` + 代码中的 `NOT_MIGRATED` 分支为准。
> 当前 `services/src` 里仍有 **96 处 `NOT_MIGRATED` 常量**，集中在 20 个 feature 文件（见 §5 Wave4）。

---

## 2. 环境与启动

### 2.1 关键路径

| 用途 | 路径 |
|---|---|
| Rust 工作区 | `D:/tigshop/java-b2b2b-pro-v5.8.57/rust`（`cargo` workspace 实际在 `rust/crates/Cargo.toml`） |
| Java 原实现 | `D:/tigshop/java-b2b2b-pro-v5.8.57/java` |
| 前端 | `D:/tigshop/java-b2b2b-pro-v5.8.57/java/view/{admin,Tigshop-Pc,uniapp}` |
| Rust 配置 | `rust/config.yaml`（可用 `TIG_<PATH>` 环境变量覆盖） |
| 上传目录 | `rust/uploads`（`file.upload_dir=./uploads`），需从 `rust/` 目录启动 |
| MySQL | `C:/Program Files/MySQL/MySQL Server 8.4/bin/mysqld.exe`，数据目录 `D:/tigshop-mysql/data`，库 `tigshop-java`，账号 `root/root` |
| Rust 工具链 | `RUSTUP_HOME=D:/rustup`、`CARGO_HOME=D:/cargo`、cargo `D:/cargo/bin/cargo.exe` |
| 交接工具 | `rust/tools/compat/*`（种子/冒烟/权限表/signed schema） |

### 2.2 启动 MySQL（当前停止）

优先（需要管理员 shell）：

```powershell
Start-Service MySQL84
```

若没有管理员权限（当前会话就是这种情况），用独立控制台启动：

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" `
  --basedir="C:\Program Files\MySQL\MySQL Server 8.4" `
  --datadir="D:\tigshop-mysql\data" `
  --port=3306 --console --mysqlx=0
```

验证：

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -h 127.0.0.1 -P 3306 -u root -proot `
  -e "SELECT VERSION(); SHOW DATABASES LIKE 'tigshop-java';"
```

### 2.3 换库/重新导入 dump 后必做（否则 sqlx 解码报错）

1. **UNSIGNED → SIGNED**：

```powershell
cd D:\tigshop\java-b2b2b-pro-v5.8.57\rust
python tools/compat/mysql_signed_schema.py gen  ..\alters.sql
python tools/compat/mysql_signed_schema.py apply ..\alters.sql
```

2. **JSON 列 → TEXT**（Rust 实体用 `String` 解码，JSON 类型会报 mismatched types）：

```sql
ALTER TABLE cart MODIFY extra_data TEXT NULL;
ALTER TABLE groupon_promotion_item MODIFY group_num TEXT NOT NULL;
ALTER TABLE groupon_promotion_detail MODIFY group_price TEXT NOT NULL;
```

> 新增的 JSON 列若实体按 `String` 解码，同样处理后把 ALTER 加到 `tools/compat/README.md`。

### 2.4 构建 / 启动 Rust 服务

```powershell
# 1) 先杀掉旧进程（Windows 会锁 exe，导致 link failed）
Get-Process tigshop -ErrorAction SilentlyContinue | Stop-Process -Force

# 2) 构建（workspace 根是 rust/crates）
cd D:\tigshop\java-b2b2b-pro-v5.8.57\rust\crates
$env:RUSTUP_HOME='D:\rustup'; $env:CARGO_HOME='D:\cargo'
& D:\cargo\bin\cargo.exe build -p tigshop-server -j 4

# 3) 启动（必须从 rust/ 目录，依赖 config.yaml 与 ./uploads）
cd D:\tigshop\java-b2b2b-pro-v5.8.57\rust
$env:RUST_LOG="info,tigshop=info,sqlx=warn"
Start-Process -FilePath ".\crates\target\debug\tigshop.exe" `
  -RedirectStandardOutput ".\run.out.log" -RedirectStandardError ".\run.err.log" -WindowStyle Hidden

# 4) 健康检查
curl.exe -s -o NUL -w "admin:%{http_code} " "http://127.0.0.1:8080/adminapi/health"
curl.exe -s -o NUL -w "api:%{http_code}`n" "http://127.0.0.1:8181/api/health"
```

- 首次全量构建可能 10+ 分钟；增量 1-6 分钟。
- 若链接报 `link.exe ... exit code 1318` 或 LNK1318：通常是磁盘/内存不足，删除增量目录后重试：
  `Remove-Item -Recurse -Force crates\target\debug\incremental`（会触发较慢重建，但可释放几十 GB）。
- 没有 Redis / RabbitMQ / Elasticsearch / Docker 时服务仍可启动（Redis 自动降级进程内缓存）。

### 2.5 测试账号 / 数据

| 账号 | 密码 | 说明 |
|---|---|---|
| `admin` | `admin123` | 平台超管（authority `["all"]`） |
| `testbuyer` | `admin123` | C 端会员（余额 950；被冒烟脚本改过积分/订单/优惠券状态） |
| `rbac_op` | `admin123` | 仅 `orderExport` 权限 |
| `rbac_op2` | `admin123` | 仅 `productManage` 权限 |

种子：`tools/compat/seed_test_member.sql`、`seed_rbac_users.sql`；各 feature 还有自己的 `seed_*.sql`。
**demo 库中可能残留历史测试订单/购物车/优惠券**，跑冒烟前按对应 seed 重置；交付前建议用 `pure.sql` 重建。

### 2.6 定时任务开关

```powershell
$env:TIG_SCHEDULER__ENABLED="false"   # 关闭全部
$env:TIG_SCHEDULER__DELAY_SECS="3"    # 启动延迟
$env:TIG_SCHEDULER__CANCEL_SECS="5"
$env:TIG_SCHEDULER__RECEIVE_SECS="5"
$env:TIG_SCHEDULER__GROUPON_SECS="3"  # 拼团过期扫描（新增）
```

---

## 3. 已完成能力（不要重做）

以下均已实现、有实测/文档；修改前先看对应文档，避免回退：

| 能力 | 关键提交/文档 |
|---|---|
| v2 结构重构（14 上下文/feature 分层，1564 路由全覆盖，门禁工具） | `MIGRATION-REPORT.md`、`ARCHITECTURE.md`、`domain-map.json` |
| 登录/RBAC/权限表（864 条）、C 端滑块验证码 | `RBAC.md`、`CAPTCHA-VERIFY.md` |
| 支付协议骨架（微信 v3 签名/验签/AES-GCM、支付宝 RSA2、PayPal 配置） | `PAYMENT-CHANNELS.md` |
| 支付回调验签、后台确认线下收款 + paylog 同步 | `PAYMENT-UPLOAD-VERIFY.md` |
| 定时任务：订单超时取消 / 自动收货 / 拼团过期 | `SCHEDULER.md` |
| Excel 基础能力 + 库存预警/商品批量导出/出入库模板 | `EXCEL.md` |
| 短信（阿里云）/SMTP/测试邮件、验证码 | `SMS-EMAIL.md` |
| IM WebSocket `/ws`、消息双向推送（会员↔管理员） | `IM-WEBSOCKET.md` |
| 结算 v2：运费模板、积分抵扣、优惠券店铺范围/多店分摊、账期、预售展示 | `CHECKOUT-V2.md` |
| 秒杀引擎（活动价/库存/限购） | `ACTIVITY-PRESALE.md` §1 |
| 定金预售下单/定金/尾款状态机（线下/余额/回调） | `ACTIVITY-PRESALE.md` §2 |
| 拼团引擎（加购上下文/阶梯价/开团/参团/成团/过期） | `ACTIVITY-PRESALE.md` §4 |
| 满减/满折引擎（阶梯/循环/范围，与秒杀/拼团叠加） | `ACTIVITY-PRESALE.md` §5 |
| 微信 API 客户端基础（access_token/kfList/客服文本/code2session） | `WECHAT-CLIENT.md` |
| 后台确认收款、测试邮件、IM 推送等 P2 修复 | `HANDOVER.md` §3 注记 |

> **注意**：`HANDOVER.md` 有些段落已过时（例如“定金预售 set_paid 直接拒绝”“sendTestEmail 占位”“IM 未接推送”均已修复）。接手后第一件事是刷新 HANDOVER（见 §9）。

---

## 4. 未提交改动（历史，已在 2026-10-19 20:xx 全部提交）

> 本节保留为历史参考：原 82 个改动已拆成多个 commit（HEAD `6ca6fe36`），当前工作树干净。
> 如需查看当时的改动：`git log --oneline 559b6685..6ca6fe36`。

| 模块/文件 | 改动内容 | 注意点 |
|---|---|---|
| `crates/security/src/jwt.rs`、`web.rs` | `Claims/AuthUser` 新增 `shop_id`；新增 `access_with_shop/refresh_with_shop/issue_pair_with_shop`；AuthUser 增加 `is_platform/scoped_shop_id`；中间件优先取 claims.shop_id，兼容 `X-Shop-Id` | 这是**多租户 shop_id 的基础**，后续要贯穿查询 |
| 约 60 个 `api/**` 路由/测试 | 构造 `AuthUser` 时补 `shop_id`（解决新增字段编译） | 只是机械适配，需确认没有把平台账号误设为店铺 |
| `crates/infrastructure/src/storage/mod.rs` | 新增 `AliyunOssStorage`/`TencentCosStorage`/`S3Storage`（S3 兼容 MinIO），带单测 | 只是**驱动层**；上传/视频/相册/批量下载等业务是否已切到这些驱动需逐项确认（`gallery*` 仍是占位） |
| `crates/infrastructure/src/queue/mod.rs` | 新增 exchanges/routing_keys 常量（SMS/订单取消/售后/结算/分账/翻译/ES 同步等）、`MqMessage`、`publish/publish_delayed`、测试 | 目前是**抽象/发布能力**；没有 RabbitMQ 时的降级策略、消费者链路要补 |
| `crates/services/src/catalog/product_manage.rs`（+464 行）、`infrastructure/.../product_manage.rs` | `delete_scoped/batch_edit_scoped/batch_modify_scoped` 等按 shop_id 限定批量操作 | 需补 API 测试：店铺账号只能操作本店商品 |
| `crates/services/src/logistics/shipping_list.rs`（+80 行） | `match_and_calc_freight`（运费匹配计算） | 与结算页 `calculate_shipping` 的口径要统一，避免两套算法 |
| `crates/services/src/trade/check.rs` | `region_match` 严格对齐 Java `fetchRegion`（末级节点/全国 `1`）；改为 `pub` | 已带测试，确认与运费模板区域数据兼容 |
| `crates/infrastructure/src/db/repo/member/member_lists.rs`、`services/.../member_lists.rs` | 会员列表查询增强 | 与多租户/权限过滤一起验收 |
| `Dockerfile` | 补 `COPY api/openapi/Cargo.toml`、`curl`、`COPY template` | 仍需修 compose 初始化（§5 Wave7） |
| `crates/Cargo.toml` | dev 依赖 `debug=0`（规避 Windows LLVM OOM），移除多余空行 | 会影响调试信息，确认可接受 |
| `template/`（新） | `o2o商品导入模板.xlsx`、`多商户商品导入模板.xlsx` | 与 Dockerfile/导入接口配套 |

---

## 5. 剩余工作（按 Wave 执行）

> 每个 feature 的“完成定义”：四层文件 + 注册 + 真实行为 + 单测 + 门禁 + 文档 + 冒烟（如适用）。
> 不要用「返回业务错误占位」交付；如确实依赖外部凭据，保留明确错误并写文档标注。

### Wave 0（0.5–1 天）：合并未提交改动 + 刷新文档

- 审阅/提交 §4；确认 `cargo test --workspace`、四项门禁全绿。
- 刷新 `docs/HANDOVER.md`：把已完成项（预售状态机、paylog 同步、测试邮件、IM 推送、结算 v2/v3、活动引擎）从“待办”移到“已完成”，重写 §3/§6。
- 刷新 `docs/domain-roadmap.md`：旧基线“929 剩余接口”已不符合当前 1564/1564 路由覆盖；要么重新生成，要么改为引用 `CAPABILITY-MATRIX` 的行为缺口口径。
- 验收：文档无过时结论；`git status` 干净；门禁绿。

### Wave 1（1–2 天）：完成多租户 `shop_id` 贯穿

**现状**：未提交改动已引入 JWT/AuthUser `shop_id` 与部分商品作用域查询；登录签发路径、会员/订单/营销等查询尚未全部使用。

任务：

1. 登录签发：admin 登录按 `admin_type`/店铺绑定签 `shop_id`；会员/店铺账号路径确认；刷新 token 同样携带。
2. 查询隔离：`AuthUser::scoped_shop_id()` 注入 repo 查询（`WHERE shop_id = ?`）；平台账号（0/None）不过滤。
3. 写操作校验：商品、订单、店铺、优惠券、售后、物流等创建/修改校验归属。
4. 测试：两个店铺账号 × 列表/详情/写操作，断言互不可见/不可改；平台账号可见全部。
5. 文档：`CAPABILITY-MATRIX` §4 多租户行改为 ✅/🟡 并写清范围。

验收：新增集成/接口测试；脏数据不串店；RBAC + 多租户组合不越权。

### Wave 2（2–3 天）：结算 v3 + 活动引擎收尾

| 子项 | 现状 | 落点 / Java 参照 | 验收 |
|---|---|---|---|
| O2O 配送/自提点 | `OrderCheckVO.deliveryOption` 恒 None；`delivery_type=2` 仅免运费 | `services/trade/check.rs`、`trade/proxy_shopping_delivery.rs`、`shop_local_delivery_config`；Java `OrderCheckServiceImpl` 配送选项/起送价 | 自提/同城配送返回选项、起送价校验、下单落库 |
| 发票随单 | `OrderCheckParam` 无 `invoiceData`；`order_invoice_client` 只查询/申请 | `order` 表发票字段、`order_invoice`；Java `OrderCheckServiceImpl`/`OrderInvoiceService` | 下单保存发票信息，订单详情可见 |
| 地区税率 | `taxRate/taxRateAmount` 恒 0 | `services/payment/tax_rate.rs`（已有 CRUD）、`tax_rate` 表、`order` 税字段；Java `resolveOrderTaxRate` | 按收货地区匹配税率并计入 totalAmount |
| 代客下单/改价 | 仅 `hasChangePrice` 标记 | `cart_global_change_price`、`OrderCheckParam.enableChangePrice`、`proxy_shopping_order_check`；Java 改价金额+运费分摊 | 改价金额落单、分摊正确，订单/结算一致 |
| 拼团收尾 | 无虚拟成团/失败退款/限购 | `services/trade/groupon.rs`、营销/退款域；Java `GrouponPromotionRecordServiceImpl` 的 `expirationTime` 虚拟成团与退款 | 过期失败可退款/虚拟补人；`order_limit_num` 生效 |
| 活动人群/赠品 | 满减未按会员等级/人群过滤；满赠(type3)未接 | `product_promotion.limit_user_rank/member_*`、`ProductGift`；Java `ProductPromotionServiceImpl` | 人群不匹配不参与；满赠返回 gift |
| 定金预售收尾 | 尾款时间窗/超时取消/退款未接 | `order_presale`、`paylog`、scheduler；Java `FinalPaymentParam`/`OrderServiceImpl` | 超时关单/退款/库存回滚有测试 |

### Wave 3（2–3 天）：定时任务 / MQ 广度

**现状**：已有轮询框架（`services/system/scheduler.rs`）：订单超时取消、自动收货、拼团过期、账期更新、商品自动上下架；未提交改动已加 MQ 抽象/routing keys。

待补任务（无凭据可先做轮询；有 RabbitMQ 再接消费者）：

1. 微信直播订单同步 `WechatLiveOrderSyncTask`（依赖微信凭据，可先写客户端骨架）。
2. 保证金到期扫描 `DepositDueCheckTask`。
3. ✅ 账期更新 `CreditUserPeriodUpdateTask`（已完成）。
4. 分账调度 `ProfitSharingScheduleTask`（依赖支付渠道）。
5. ✅ 商品定时上下架 `ProductAutoStatusTask`（已完成）。
6. 售后自动收货、退款结算、自动评价（MQ 消费链路）。
7. 门店结算、分销员结算、供应商结算、礼品卡/提货自动入库、订单完成送积分、ES 批量/单条同步失败重试。

验收：每个任务可配置开关、幂等、可观测日志；用 fixture 时间戳验证一轮扫描结果；文档更新 `SCHEDULER.md` §4。

### Wave 4（3–5 天）：补齐行为占位（按域）

先用命令重新扫描（结果可能随 Wave 1-3 变化）：

```powershell
cd D:\tigshop\java-b2b2b-pro-v5.8.57\rust\crates
Get-ChildItem -Recurse services/src,api -Include *.rs |
  Select-String "NOT_MIGRATED|未迁移" |
  Group-Object Path | Sort-Object Count -Descending |
  Select-Object Count,Name
```

当前重点（96 处常量 + 硬编码占位，按业务价值排序）：

| 域 | 文件 | 缺口 / Java 参照 |
|---|---|---|
| 内容 | `content/wechat_menu.rs`（6） | 菜单本地 CRUD/发布；Java `WechatMenuController` |
| 内容 | `content/wechat_live.rs`(12)、`wechat_live_goods.rs`(5)、`wechat_live_order_sync.rs`(5)、`api_wechat_live.rs`(3) | 视频号直播房间/商品库/订单同步；需微信凭据；无凭据部分先做本地 CRUD/落库 |
| 内容 | `content/gallery.rs`(6)、`gallery_pic.rs`(12)、`gallery_video_info.rs`(7) | 相册上传/批量下载 zip/视频首帧（FFmpeg）+ 新存储驱动；Java `GalleryPicController` |
| 内容 | `content/decorate_template.rs`(10) | 装修模板资源包/渲染；Java `DecorateTemplateService` |
| 内容 | `content/seo.rs`(23) | 翻译/ES 依赖；本地 SEO CRUD 可先做，翻译路径待凭据 |
| 会员 | `member/user_center.rs`(13)、`member/login.rs`(9)、`member/user_manage.rs`(4) | 个人中心安全/绑定/注销、第三方登录（微信需凭据） |
| 交易 | `trade/proxy_shopping_delivery.rs`(6)、`proxy_shopping_order_check.rs`、`order_admin.rs`、`aftersales.rs` | 代客下单/发货、后台订单动作、售后自动链路 |
| 系统 | `system/util.rs`(8) | 工具类端点（按 Java `ToolsController` 对齐） |
| 商品 | `catalog/product_manage_extra.rs`(6)、`product_stock.rs`、`stock_warning.rs`、`search_admin.rs` | ES 同步/搜索、库存预警联动、Redis 锁 |
| 商户 | `merchant/shop_funds.rs`(4) | 店铺资金总览/流水 |
| 营销 | `marketing/gift_card.rs`(3) | 礼品卡兑换/状态 |
| IM | `im/im_message.rs`(1) | 视频上传；自动回复/MQ（已读回执已完成） |

验收：模块头删除“未迁移”声明；实现真实行为；有单测；相关冒烟或 SQL 断言；更新 `CAPABILITY-MATRIX` 行。

### Wave 5（2–3 天）：支付扩展（task-43 交接）

先读 `docs/_task43-handoff.md`，其中已给出 4 个 feature 的字段/错误文案/注册命令：

1. `payment/recharge_order`：`/api/user/rechargeOrder/*`（list/setting/paymentList/update/pay/checkStatus/create）。
2. `payment/apple_pay`：`/api/order/pay/applepay/*` + `/api/order/pay/notify/ums`（验签/PSP 未迁移部分保留明确错误）。
3. `payment/profit_sharing`（admin）：分账列表/批量提交/手动完结/取消。
4. `payment/sub_merchant`（admin）：子商户列表/绑定/解绑。

验收：四层文件齐全并注册；契约覆盖不下降；DB 操作有测试；错误文案与 Java 一致；`CAPABILITY-MATRIX`/`PAYMENT-CHANNELS.md` 更新。

### Wave 6（1–2 天）：Excel 余量 / 审计日志 / IM 收尾

| 子项 | 端点/落点 | 说明 |
|---|---|---|
| 商品批量导入 | `product/productBatch/productBatchModify` | 解析映射已可复用 |
| 库存批量导入/调拨 | `product/stock/batchImport`、`batchImportAllocate` 等 8 个 | 套 `EXCEL.md` §3 模式 |
| 订单导出 | `order/order/orderExport` | 列对齐 Java VO |
| 批量发货导入 | `order/order/batchDeliverImport` | 解析 + 更新物流单号 |
| 电子卡密导入 | `product/eCardGroup/import` | 卡密文件解析 |
| 对账单导出 | `finance/statement/exportStatement`、`exportStatementStatistics` | 财务口径确认 |
| 客户/会员导出、微信直播商品导入 | 见 `EXCEL.md` §4 | 后续 |
| 审计日志 | `services/identity/admin_log.rs` | 在品牌/评论/卡密/商品等修改处调用 `createByLogInfo` 等价实现 |
| IM 已读回执 | `services/im/im_message.rs` + `security/ws_registry.rs` | ✅ 已实现：`set_read_*` 推 `{"type":"read","data":[{shopId}]}`（双向）；自动回复需 MQ/轮询 |

验收：xlsx 内容/列头人工校验；导入失败明细与 Java 一致；审计记录可查；IM 双端实测（参考 `tools/compat/im_push_smoke.ps1`）。

### Wave 7（2–3 天）：部署 / 可观测性 / 测试分层

1. **Docker compose 修正**：已改为挂载 `deploy/mysql-init/00-pure.sql`（由 `tools/compat/prepare_docker_mysql.py` 从 `pure.sql` 去除 `INT UNSIGNED` 生成），不再执行大量 update/demo 脚本；本机无 Docker，需在有 Docker 的环境验证。
2. **Nginx + 前端 dist**：配 `/adminapi`→8080、`/api`→8181、`/static`→8080、`/ws`→8181（Upgrade）；实际构建 `java/view/*` 并放 dist。
3. **可观测性**：已暴露 `/metrics`（存活/版本/uptime）；请求量/延迟/DB 连接/调度任务指标与告警待补。
4. **测试分层**：补 DB 集成测试（可用本机 MySQL + 独立测试库）、接口回放/E2E、前端联调脚本；当前只有单元测试（覆盖率 100% 是路径口径）。
5. **生产配置**：替换 `jwt.secret`（默认值）、收紧 CORS（现在 Any）、设置上传目录/大小、确认 MySQL/Redis 地址；补 migrations 或明确以 Java dump 建库。
6. **多实例**：Redis 接管 WS 会话/验证码/登录失败计数/权限缓存；调度任务加分布式锁（或接 MQ）。
7. **清理**：`rust/` 根目录约 100+ 个 `build*.log/check_*.log/test_*.log`（已 gitignore，但交付前建议清掉）；demo 库测试订单/fixture 清理。

验收：`docker compose up` 可启动（有 Docker 环境）；nginx 反代后前端登录/下单/支付回调/WS 全通；`/metrics` 可抓；E2E 在 CI 或脚本中可重复。

---

## 6. 提交与协作规范

1. **一个 feature 一个 commit**：四层文件 + 注册 + 测试 + 文档一起提交；消息用 `feat(<ctx>): ...` / `fix(...)` / `docs(...)`。
2. 改共享文件（`*/mod.rs`、`AdminState/PublicState`、`Cargo.toml`、`bin/src/main.rs`）的 PR 由一人合并，避免并行冲突。
3. 每轮提交前必跑 §7 门禁；测试失败不允许提交。
4. 如果多 Agent 并行：按 `ctx::feature` 划分写范围；共享注册文件由 Lead 统一追加；任务板上写清 write scope。
5. **不要重置/覆盖当前工作树**：它是另一条 lane 的成果；先审阅、测试、提交，再继续。

---

## 7. 验证手册

### 7.1 全量门禁（从 `rust/` 目录）

```powershell
cd D:\tigshop\java-b2b2b-pro-v5.8.57\rust\crates
$env:RUSTUP_HOME='D:\rustup'; $env:CARGO_HOME='D:\cargo'
& D:\cargo\bin\cargo.exe test --workspace -j 4        # 全绿

cd ..
python tools/check_api_coverage.py                    # 覆盖率 100%，未实现 0
python tools/check_route_conflicts.py                 # 冲突 0
python tools/check_sql_columns.py --strict            # PASS
python tools/check_structure.py --strict              # PASS
```

### 7.2 冒烟脚本（按需重置 seed）

| 脚本 | 种子 | 预期（详见文档） |
|---|---|---|
| `tools/compat/captcha_e2e.py` | 无需 | 滑块生成/校验/二次校验 |
| `tools/compat/ws_smoke.ps1` / `ws_push_smoke.ps1` | 无需 | 欢迎语/心跳/推送 |
| `tools/compat/rbac_smoke.ps1` | `seed_rbac_users.sql` | 无权限 403，有权限 200 |
| `tools/compat/im_push_smoke.ps1` | 会员+管理员各一登录 | 会员→管理员、管理员→会员双向 WS 消息 |
| `tools/compat/checkout_v2_smoke.ps1` | `seed_checkout_v2.sql` | 运费 4/积分 1 元/券 10/账期/预售定金 |
| `tools/compat/seckill_smoke.ps1` | `seed_seckill.sql` | 78→50、库存 5→4、限购拒绝 |
| `tools/compat/presale_smoke.ps1` | `seed_presale.sql` | 定金 5、尾款 27、最终已付 32 |
| `tools/compat/groupon_smoke.ps1` | `seed_groupon.sql` | 35→10、开团/参团 status=2、过期 status=3 |
| `tools/compat/fullreduce_smoke.ps1` | `seed_fullreduce.sql` | 490→387（优惠 103） |

> PowerShell 执行方式：`powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\compat\xxx.ps1`（本机无 `pwsh`）。

### 7.3 手工检查 SQL 示例

```sql
SELECT order_id,total_amount,paid_amount,unpaid_amount,pay_status,order_type
FROM `order` ORDER BY order_id DESC LIMIT 5;
SELECT * FROM order_presale ORDER BY presale_order_id DESC LIMIT 3;
SELECT groupon_promotion_record_id,join_type,join_status,min_group_num
FROM groupon_promotion_record ORDER BY groupon_promotion_record_id DESC LIMIT 5;
```

---

## 8. 已知坑与约束（务必先看）

1. **JSON 列必须转 TEXT**：`cart.extra_data`、`groupon_promotion_item.group_num`、`groupon_promotion_detail.group_price`；否则 sqlx 报 `mismatched types ... JSON`。
2. **UNSIGNED 必须转 SIGNED**：重新导入 dump 后跑 `tools/compat/mysql_signed_schema.py`，否则 `INT UNSIGNED` 解码失败。
3. **Axum 0.8 路径参数**用 `{param}`，不能用 `:param`；根路由（`/im/**`）不带 `/adminapi` 前缀（前端 request 用 `prefix: "/im"` 覆盖 baseURL），改路由前先看契约与前端调用。
4. **Windows 构建锁**：重编前必须 `Stop-Process tigshop`；否则 `failed to remove ... tigshop.exe`。
5. **磁盘/内存**：target 已有 ~74GB；链接失败（1318/LNK1318）先删 `crates/target/debug/incremental`；`Cargo.toml` 里 dev deps `debug=0` 是为了规避 Windows LLVM OOM。
6. **PowerShell 引号**：带中文/多层引号的 `python -c` 极易被 PowerShell 吃掉；**写 `.py`/`.ps1` 文件再执行**。控制台显示 UTF-8 可能乱码，以文件内容/API JSON 为准。
7. **服务必须在 `rust/` 目录启动**：依赖 `config.yaml` 和 `./uploads`；在别处启动会找不到配置/上传目录。
8. **时间类型不统一**：订单表多用 `i32` 秒，拼团/新表多用 `i64` 秒；取当前时间用 `tigshop_common::utils::now_seconds() as i32/i64`，不要混用。
9. **金额用 `rust_decimal::Decimal`**，JSON 序列化为字符串；DB 列 `DECIMAL` 不要用 f64。
10. **MySQL 服务启动权限**：当前普通会话不能 `Start-Service MySQL84`，用 §2.2 手动 `mysqld --console`。
11. **测试有共享全局状态**（WS registry 等）：新测试涉及全局注册表时加串行锁，避免并行 flaky（已有 `registry_test_lock` 示例）。
12. **demo 数据会被冒烟改脏**：跑前先 seed；测试订单/优惠券 id 会变化，不要硬编码到新测试。
13. **不要提交** `../_diag/`、`../_page.html`；`rust/*.log` 已 gitignore，但会产生大量磁盘占用。

---

## 9. 文档索引与刷新规则

| 文档 | 定位 | 状态 |
|---|---|---|
| `docs/CAPABILITY-MATRIX.md` | 能力状态总览（按能力/域） | 相对准确，随 Wave 更新 |
| `docs/HANDOVER.md` | 环境/评审/待办 | **部分过时**（§3 已完成的项仍列在待办），Wave 0 刷新 |
| `docs/ARCHITECTURE.md`、`domain-map.json` | 结构契约（14 上下文/feature） | 权威 |
| `docs/domain-roadmap.md` | 旧 slice 基线剩余接口统计 | **过时**（929 vs 当前 1564 路由全覆盖），建议重生成或删 |
| `docs/MIGRATION-REPORT.md` | v2 重构历史报告 | 历史，不再更新 |
| `docs/CHECKOUT-V2.md`、`ACTIVITY-PRESALE.md` | 结算/活动口径与实测 | 当前 |
| `docs/PAYMENT-CHANNELS.md`、`PAYMENT-UPLOAD-VERIFY.md` | 支付协议/回调 | 当前 |
| `docs/SCHEDULER.md`、`EXCEL.md`、`IM-WEBSOCKET.md`、`RBAC.md`、`SMS-EMAIL.md`、`WECHAT-CLIENT.md` | 各 feature 说明 | 当前，Wave 3/4/6 需更新 |
| `docs/_task43-handoff.md` | 支付扩展（recharge/apple/profit/sub_merchant）详细交接 | 待执行 |
| `docs/RUN-RUST-FRONTEND.md` | 本地运行/前端部署 | 当前 |
| `docs/TOOLING.md`、`tools/compat/README.md` | 工具/种子/冒烟 | 当前 |

规则：每完成一个 Wave/feature，更新 `CAPABILITY-MATRIX` 对应行 + 该 feature 文档；不要让 HANDOVER 再次腐化。

---

## 10. 建议的首日行动计划（可直接照做）

1. **0.5h**：启动 MySQL（§2.2）→ 验证库/表；若报 UNSIGNED/JSON 错误执行 §2.3。
2. **0.5h**：构建并启动 Rust（§2.4），健康检查 + admin/testbuyer 登录。
3. **1h**：跑 §7 全量门禁；确认当前工作树通过。
4. **1h**：审阅 §4 未提交 diff，按逻辑拆分提交（至少：security/shop_id、storage、queue、catalog/logistics）。
5. **0.5h**：刷新 `HANDOVER.md`（Wave 0）。
6. **剩余**：按 Wave 1（多租户 shop_id）开工；先做两个店铺账号的隔离测试，再改查询。

---

## 11. 需要人类确认的问题（不要自行假设）

1. **RBAC `all` 语义**：平台超管是否可见所有店铺；店铺超管是否只能本店（当前 Rust `all` = 全部权限）。
2. **调度方案**：继续轮询（单实例够用）还是必须接 RabbitMQ（多实例/精确延迟）？
3. **多租户隔离强度**：是否所有业务表都必须强制 `shop_id`（含历史数据迁移/默认值）？
4. **支付渠道优先级**：微信/支付宝/PayPal 谁先联调；其它渠道是否还需要。
5. **对象存储/ES/翻译**：用哪家（OSS/COS/S3/MinIO）、是否必须上 ES、翻译供应商。
6. **数据重置**：交付前是否允许用 `pure.sql` 重建 demo 库（会丢失测试订单/fixture）。
7. **部署形态**：单机 Docker Compose 还是多实例 K8s（影响 Redis/MQ/WS 方案）。

---

## 12. 交接完成标准（Checklist）

- [ ] MySQL/Rust 可启动，health 200，admin/testbuyer 可登录。
- [ ] `cargo test --workspace` 全绿；四项门禁 PASS。
- [ ] 未提交改动已审阅并提交，`git status` 干净（忽略仓库外临时产物）。
- [ ] `HANDOVER.md` / `CAPABILITY-MATRIX.md` 已刷新，不再把已完成项列为待办。
- [ ] Wave 1 至少完成「登录签发 + 查询隔离 + 双店铺测试」。
- [ ] 新 feature 有单测/接口测试与文档，不留“未迁移”占位。
- [ ] 交付前 Docker/nginx/`/metrics` 至少有一份可复现的部署说明。
