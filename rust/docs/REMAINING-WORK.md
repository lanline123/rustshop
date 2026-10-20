# TigShop Rust 迁移剩余工作清单

> 基线：`master`，HEAD `bf50fb17`（2026-10-20，本文档之前的代码提交）
> 适用范围：微信生态、搜索引擎、翻译、短信/邮件、对象存储、支付、前端迁移与工程化收尾。
> 本文只列**未完成项**和需要外部输入的阻塞项；已完成项见 §1 与 `NEXT-AI-HANDOFF.md`。

---

## 0. 当前工作树状态（先看）

**当前工作树不是干净的**，在继续开发前必须先处理：

- 约 **286 个 tracked 变更**；
- **约 15,188 个未跟踪文件**集中在 `rust/web/`，包含 `node_modules/`、`dist/`、`*.tsbuildinfo`；
- `crates/pc-web`（Leptos SSR）已被删除，替代前端在 `rust/web/{admin,pc}`，但尚未提交；
- 部分未提交后端改动（`config_basic`、`api/public`、`Cargo.toml` 等）与前端迁移混在一起；
- 当前工作树 `cargo test -p tigshop-services -p tigshop-admin-api --lib` 有 **1 个失败**：
  `services::system::config_basic::tests::placeholder_errors_cover_unmigrated_kinds_only`，
  原因是未提交的 `config_basic` 实现与旧占位测试不一致。

**建议动作**：

1. 先评审/提交前端迁移与 `config_basic` 在途改动；
2. 给 `rust/web/` 增加 `.gitignore`：至少排除 `node_modules/`、`dist/`、`*.tsbuildinfo`、`.vite/`；
3. 修复 PC 构建错误后再继续功能迁移。

---

## 1. 已完成（本轮/近期，供核对，不重复做）

| 能力 | 状态 | 代码/提交 |
|---|---|---|
| 公众号安全模式回调（签名、AES、echostr、SCAN/subscribe） | ✅ | `f7413ff6` |
| 扫码 openid 轮询、自动注册、会员 JWT | ✅ | `f7413ff6` |
| 公众号 OAuth code 登录（access_token + userinfo） | ✅ | `94c123c6` |
| PC 扫码登录二维码（qrcode/create） | ✅ | `94c123c6` |
| 小程序手机号获取（getuserphonenumber） | ✅ | `94c123c6` |
| 微信客服能力探测/菜单推送 | ✅ | 已有实现 |
| 短信发送（阿里云）、验证码校验、绑定手机/重置令牌 | ✅ 已实测 | 现有 `system::notify` + `member::login` |
| 阿里云 OSS 真实上传/删除、DB 配置选择 | ✅ 已实测 | `38915759` |
| 商品关键词分词（Jieba，替代原 ES 占位） | ✅ | `576503c2` |
| 火山引擎翻译客户端 + SEO 三个端点代码 | ✅ 代码完成 | `fb1db52a`（账户欠费，见 §2.3） |
| 订单/售后/代客下单后置动作、定时任务、保证金生命周期、提现余额联动 | ✅ | `8eef3f19`、`07174c19`、`73f41bbc`、`3195eb82`、`84659770` 等 |
| 后台手机号修改/验证码复用短信通道 | ✅ 已实测 | `06ba7827` |
| 代客领券 `promotion.member_scope` 人群匹配 | ✅ | `9a271ca0` |
| 前台商品详情 O2O 门店商品覆盖 | ✅ 已实测 | `b5cadd65`、`bf50fb17` |
| 代客下单可用性：门店价/库存、会员折扣、距离 | ✅ | `b5cadd65`、`bf50fb17` |
| 礼品卡平台店铺名/logo 读取 `shopName`/`shopLogo` | ✅ | `03b30ed2` |
| H5 默认导航模板兜底 | ✅ 已有 | `DEFAULT_NAV_H5_DECORATE_DATA` |
| SMTP 邮件（163 SSL/465） | ✅ 已实测 | 配置入库；Rust `sendTestEmail` 返回成功 |
| 火山翻译 / SEO 标识生成 | ✅ 已实测 | 充值后 `TranslateText` 与 Rust SEO 接口返回 `test-product` |
| 快递鸟签名/1002 轨迹/1007 面单 | ✅ 代码完成 | `66ced96f`；真实账号返回「没有可用套餐」 |
| C 端图片/视频/头像/IM 上传走 OSS | ✅ | `c79ec970` |
| 催发货 vendor 后台消息 | ✅ | `b2da5837` |
| 售后退货地址三级兜底 | ✅ | `b2da5837` |
| 商品管理客户端 6 端点租户校验 | ✅ | `d1f48658` |
| 取消订单返还积分/余额/优惠券/秒杀/礼品卡/拼团 | ✅ | `7338ebae`、`c32d3d5a`、`9a723d7c` |
| 确认收货/取消订单后台消息 | ✅ | `6333486e`、`7338ebae` |
| 售后创建退款申请、财务审核关闭自动处理 | ✅ | `3c719806` |
| 供应商售后消息 + `after_sales_review` 开关 | ✅ | `3c719806`、`e97540b9` |
| 整单售后释放优惠券占用 | ✅ | `3532b9f0` |
| 待付尾款定金预售禁止取消 | ✅ | `79592b40` |

---

## 2. 未完成项

## P0：阻塞上线/阻塞切换的前端与外部能力

### 2.1 前端迁移（最大工作量）

**现状**（2026-10-20 重构后，详见 `docs/FRONTEND-REFACTOR-STATUS.md`）：

| 指标 | 原版 admin | 重构 admin | 原版 PC | 重构 PC |
|---|---:|---:|---:|---:|
| `.vue` 文件 | 1329 | **13** | 430 | **16** |
| 可迁移页面 | 1166 | **14** | 200 | **21** |
| 业务路由 | 394 | **16** | 105 | **16** |
| Pinia store | 18 | **0** | 15 | **2** |
| i18n | 完整 | **无** | 完整 | **无** |

已完成：Schema 表单/表格新增编辑、商品/订单/会员/优惠券核心页、PC 购物车→结算→支付→订单主链路、地址/个人中心、真实 API 封装、构建与契约检查全绿。

**当前结果**（2026-10-20）：

- `web/admin` 与 `web/pc` 生产构建均通过；
- 管理端契约调用全部命中，PC 38/38 命中；
- 已提交 `rust/web`；工作树仅剩证书文件未跟踪；
- 仍需按域继续补页面：Admin 多规格商品/售后/代客下单/账期/导出/装修引擎，
  PC 会员其余页/活动页/支付渠道页/结算增强/i18n/SSR。

**完成标准**：

- 每次提交前 `pnpm --filter @tigshop/admin build`、`@tigshop/pc build` 通过；
- `python tools/check_frontend.py contract --strict` 0 未命中；
- 按 `docs/FRONTEND-REFACTOR-STATUS.md` 的 P0/P1 清单继续迁移；
- 真实登录、商品、订单、支付、会员、营销页面可联调。

### 2.2 微信直播：商品 / 订单同步 / 回调建单

**现状**：Rust 仍是明确「未迁移」占位：

- `services/src/content/wechat_live_goods.rs`
- `services/src/content/wechat_live_order_sync.rs`
- `services/src/content/wechat_live_sync_callback.rs`
- `services/src/content/wechat_live.rs`（房间/推流/直播记录）

**缺失依赖**：

- 小程序 `AppID` / `AppSecret`（`wechatMiniProgramAppId` / `wechatMiniProgramSecret`）；
- 视频号直播 / 小店 API 权限；
- 直播商品库、订单同步接口权限；
- 回调验签/建单所需配置。

**完成标准**：

- 直播商品导入/列表/查询真实调用微信 API；
- 订单同步日志/重试/对账落库；
- 回调命中后能创建订单、幂等、失败可重试；
- 不回退到「未迁移」错误。

### 2.3 火山引擎翻译（SEO）✅

已充值并实测通过：

- 有效 AK/SK 调用 `TranslateText` 返回 `test product`；
- Rust `translateToSeoIdentification` 返回 `test-product`；
- 批量分类/商品 SEO 代码已就绪（732 个分类 / 406 个商品待生成，执行前评估翻译费用）。

> 另一组 AK 仍返回 `InvalidAccessKey`，以当前 `config` 表中可用的一组为准。

### 2.4 微信支付（Rust 在线支付/退款）

**现状**：

- APIv2 密钥已实测可用（`ORDERNOTEXIST` 说明签名正确）；
- APIv3 商户证书/私钥/序列号实测可完成请求签名；
- APIv3 当前返回「无平台证书，请使用微信支付公钥」；
- Rust **在线支付下单/退款/分账/进件/线上提现** 仍未实现；
- Rust `load_channel_configs` 目前按 PEM 读配置，但 Java 后台 `wechatPayPrivateKey` 等是整数标记，实际证书是文件，需要迁移证书加载方式。

**缺失**：

- 微信支付公钥 `pub_key.pem` + 公钥 ID（或平台证书）；
- 支付回调 notify URL；
- 证书安全存放目录/环境变量方案。

**完成标准**：

- 下单、查询、退款、回调验签、退款回调全链路；
- 分账/进件按渠道能力补齐；
- 商户证书从安全路径加载，不写入 git。

### 2.5 SMTP 邮件 ✅

已使用 `lanline123@163.com` + 163 客户端授权码完成：
- `smtp.163.com:465` SSL 登录成功；
- 自测邮件发送成功；
- Rust `sendTestEmail` 返回 200。

配置项：`smtpHost=smtp.163.com`、`smtpPort=465`、`smtpSsl=1`、
`smtpUser=lanline123@163.com`、`smtpMail=lanline123@163.com`、`smtpPass=<授权码>`。

### 2.6 快递鸟 ✅（代码完成，账号套餐阻塞）

**已实现**：

- `services/src/logistics/kdniao.rs`：签名、1002 轨迹、1007 电子面单；
- 后台 `kdniaoBusinessId` / `kdniaoApiKey` 读取与 `shippingSettings` 回显；
- 管理端/用户端 `shippingInfo` 已接入实时 1002 查询；
- 电子面单发货（`shipping_method=4`）已接入 1007，并写 `logistics_api_log`；
- 单测覆盖签名、Success 兼容、轨迹反序列化、面单序列化。

**真实验证**：

- 新 EBusinessID `1309995` + API Key 签名正确；
- 极兔 `JT3175428264802` 查询已到快递鸟，但账号返回
  `业务错误[没有可用套餐]`；
- 需在快递鸟后台购买/开通查询/面单套餐后，才能返回真实轨迹与面单。

**注意**：

- `DataSign = Base64( MD5(RequestData + ApiKey) 的十六进制字符串 )`；
- SF/KYSY/ZTO 查询需 `CustomerName`（手机号后四位）。

### 2.7 ES 全量同步

**现状**：

- `getParticiple` 已改为 Jieba 本地分词（与 Java 一致，不依赖 ES）；
- `initAllToEs` 仍是占位。

**需要提供**：

- ES 地址、端口、用户名、密码；
- 索引名、mapping、是否使用 IK；
- 是否需要增量同步/失败重试。

**完成标准**：

- 全量商品批量写入 `_bulk`；
- 索引不存在时可创建；
- 同步失败有重试/日志；
- 前端搜索能命中。

---

## P1：微信/公众号真实联调与剩余小项

### 2.8 公众号 access_token 白名单

- 本地测试机出口 IP `223.160.165.166` 仍被微信拒绝（40164）；
- 服务器 IP `101.37.194.11` 需要加入公众号后台 IP 白名单；
- 加入后需实测：access_token、客服列表、菜单推送、PC 二维码。

### 2.9 C 端同步上传走 OSS ✅

- 会员图片/视频/头像上传已改为异步全局存储；
- IM 视频上传已改为异步全局存储；
- 后台异步上传本就使用全局 OSS；
- 剩余：APP 更新包上传（`upload_app_file`）仍走本地，属于运维低频路径，可后续单独迁移。

### 2.10 其他业务小项

- 订单/售后剩余边界：催发货已写 vendor 管理员消息（`b2da5837`）、售后退货地址
  已做 vendor/shop/平台三级兜底；后台取消已返还积分/余额/优惠券并写后台消息；
  部分发货拆单已实现；售后已创建退款申请并在财务审核关闭时自动置处理中；
  仍剩定金预售、退款渠道实际执行/结算单等；
- 微信公众号/小程序真实扫码登录全链路（等 IP 白名单 + 小程序凭据）；
- 多租户：`catalog/product_manage_client` 6 端点已加店铺范围校验（`d1f48658`）；
  `admin_msg.msgCount` 已是 SSE + TenantContext；`groupon_promotion` public
  端点已存在，剩余平台列表语义/少量 public 查询需复核；
- 文档过期描述清理：已清理 admin_user / WebSocket / SMTP / gift-card / KDN 等高价值
  模块头；其余大量注释仍是旧描述，需持续清理。

---

## 3. 外部凭据/配置清单（不写入代码）

| 用途 | 需要的配置 | 当前状态 |
|---|---|---|
| 公众号 | appId / appSecret / serverToken / EncodingAESKey / IP 白名单 | 凭据已有；IP 白名单待加 |
| 小程序/直播 | miniProgramAppId / miniProgramSecret / 直播权限 | 缺失 |
| 微信支付 | mchid / APIv2 key / APIv3 key / 商户证书 / 微信支付公钥 | 证书已有；公钥缺失 |
| 阿里云 OSS | AK/SK / bucket / endpoint | ✅ 已验证 |
| 火山翻译 | AccessKey / SecretKey / 已开通翻译服务 | ✅ 已验证 |
| SMTP | 163 邮箱用户名 / 授权码 / 发件人 | ✅ 已验证 |
| 快递鸟 | EBusinessID / API Key | ✅ 凭据/客户端已完成；账号需购买套餐 |
| ES | URL / 账号密码 / 索引名 / 分词器 | 缺失 |

配置项优先通过后台「设置」或环境变量注入；**不要把 Secret 写入仓库**。
生产环境建议使用密钥管理/环境变量，并定期轮换。

---

## 4. 验证命令

```powershell
cd rust/crates
cargo check --workspace
cargo test -p tigshop-services -p tigshop-infrastructure -p tigshop-admin-api --lib -j 2

cd ..
python tools/check_api_coverage.py
python tools/check_route_conflicts.py
python tools/check_sql_columns.py --strict
python tools/check_structure.py --strict

cd web
pnpm --filter @tigshop/admin build
pnpm --filter @tigshop/pc build
python ../tools/check_frontend.py contract --strict
```

当前基线注意：

- 工作树有未提交改动时，测试失败不一定来自 HEAD；
- 本机 MSVC `cargo test --workspace` 偶发 `rustc STATUS_STACK_BUFFER_OVERRUN`，可用三 crate 测试替代；
- `cargo fmt --all -- --check` 目前不通过，需统一格式化。

---

## 5. 建议执行顺序

1. **收敛工作树**：提交/评审前端迁移，补 `.gitignore`，修复 PC 构建和 admin 契约 3 条错路径；
2. **补前端平台骨架与 P0 域**；
3. **拿小程序凭据 + IP 白名单**，完成微信直播/公众号真实联调；
4. **拿微信支付公钥**，实现 Rust 在线支付/退款/回调；
5. **快递鸟购买查询/面单套餐**，用真实运单验证轨迹与面单；
6. **ES 全量同步**；
7. 最后做 Docker/MySQL E2E、CI、文档清理。

---

## 6. 不要重复实现

以下已经完成，不要回退成占位：

- 公众号安全模式回调、扫码事件、自动注册；
- OAuth code 登录、PC 二维码、小程序手机号；
- 阿里云 OSS 上传/删除；
- Jieba 分词；
- 火山翻译 SigV4 客户端与 SEO 批量代码；
- 短信发送/验证码/绑定手机；
- 售后/订单/代客下单后置动作、定时任务、保证金/提现。

遇到旧注释「未迁移」时，先读当前实现，不要按注释直接重写。
