# Rust 重构并行分工登记表

> 多个 AI 同时重构本 workspace。**开工前先在此登记认领的域，完工后更新状态**，
> 严格只改自己认领域的文件，避免并发写冲突。

## 认领规则

1. 每个"业务域"对应一组独占文件（见下表），跨域共享文件（`mod.rs`、
   `lib.rs` 路由注册、`db/mod.rs` 的 `Repositories`、`bin/main.rs` 装配）
   **一律由协调者预注册好骨架**，域实现者不修改。
2. 域实现者的文件清单固定为：
   - `crates/core/src/entities/<domain>.rs`
   - `crates/core/src/models/<domain>.rs`
   - `crates/infrastructure/src/db/repo/<domain>.rs`
   - `crates/services/src/<domain>.rs`
   - `crates/api/admin/src/routes/<domain>.rs`
   - `crates/api/public/src/routes/<domain>.rs`
3. 服务构造函数签名固定：`pub fn new(repos: Arc<Repositories>) -> Self`。
   需要缓存的域自行从 `tigshop_infrastructure` 引入，不改全局装配。
4. 代码风格参照 `crates/services/src/user.rs` 与
   `crates/infrastructure/src/db/repo/product.rs`：中文 doc 注释、
   `tigshop_core::{Error, Result}`、实体 `#[sqlx(rename_all = "snake_case")]`、
   DTO/VO `#[serde(rename_all = "camelCase")]`。
5. 金额一律 `rust_decimal::Decimal`，时间一律毫秒时间戳 `i64`。

## 域认领状态

| 域 | 说明 | 文件前缀 | 认领者 | 状态 |
|----|------|----------|--------|------|
| auth | 登录/JWT/改密 | `auth` | 原有实现 | ✅ 已完成 |
| product | 商品/SKU | `product` | 原有实现 | ✅ 已完成（基础查询） |
| cart | 购物车 | `cart` | 原有实现 | ✅ 已完成 |
| order | 订单/状态机 | `order` | 其他 AI | 🔄 修复编译错误中 |
| payment | 支付渠道抽象 | `payment` | 其他 AI | 🔄 修复编译错误中 |
| user | 会员/余额 | `user` | 原有实现 | ✅ 已完成 |
| coupon | 优惠券/发放/核销 | `coupon` | AI-协调者(本会话) | ✅ 已完成（实体395行/仓储487行/服务710行，严格对齐 pure.sql） |
| content | 文章/文章分类 | `content` | AI-协调者(本会话) | 🔄 实体待补全（骨架可编译） |
| catalog | 商品分类/品牌 | `catalog` | AI-协调者(本会话) | ✅ 已完成（实体206行，严格对齐 pure.sql） |
| logistics | 运费模板/物流公司 | `logistics` | AI-协调者(本会话) | 🔄 骨架可编译，待补业务逻辑 |
| member | 会员等级/标签/积分 | `member` | AI-协调者(本会话) | 🔄 骨架可编译，待补业务逻辑 |
| shop | 商户/店铺/提现 | `shop` | AI-协调者(本会话) | 🔄 骨架已注册（models/repo/service/两端路由+State 装配，cargo check 绿） |
| decorate | 装修/导航/楼层 | `decorate` | AI-协调者(本会话) | 🔄 骨架已注册（同上，待填充业务逻辑） |
| salesman | 分销员/佣金/业绩 | `salesman` | AI-协调者(本会话) | 🔄 骨架已注册（同上，待填充业务逻辑） |

## 协调者已为新域预注册的共享文件改动（其他 AI 无需重复做）

- `crates/Cargo.toml`：无需改动（域以扁平文件并入现有 crate）
- `crates/core/src/entities/mod.rs` / `models/mod.rs`：已加 catalog/content/coupon 模块与 re-export
- `crates/infrastructure/src/db/repo/mod.rs` / `db/mod.rs`：已加三个仓储并挂入 `Repositories`
- `crates/services/src/lib.rs`：已加三个服务模块
- `crates/api/admin/src/lib.rs`：新增 `pub mod routes;` + `AdminState` 增加 coupon/content/catalog 字段 + `routes::merge_all` 挂载
- `crates/api/public/src/lib.rs`：同上，拆分 `merge_public`（公开）/`merge_authorized`（需登录）
- `crates/bin/src/main.rs`：装配三个新服务并注入两端 State
- 新增 `crates/api/{admin,public}/src/routes/{mod,coupon,content,catalog}.rs`

> 后续新域照此模式：协调者先加 mod 注册与 State 字段并保证可编译，
> 再把 6 个域文件交给子代理填充，避免并发写共享文件。

## ⚠️ 重要：并发 cargo 构建会耗尽内存并伪造编译错误（2026-10-13 实测）

**现象**：`cargo check` 报出大量看似代码错误的诊断：

```
error: only metadata stub found for `rlib` dependency `alloc`
error: can't find crate for `std`
error[E0786]: found invalid metadata files for crate `version_check`
error: internal compiler error: `Res::Err` but no error emitted
error: cannot find macro `println` in this scope   (在 build.rs 里)
```

**根因**（已实测确认，**不是代码问题**）：

1. 本机物理内存仅 13.7 GB，多个 AI 同时跑 `cargo check --workspace` 时
   `FreePhysicalMemory` 降到 **0.6 GB**；rustc 分配失败后崩溃，
   留下半写入的 `.rmeta`，后续构建复用这些损坏产物 → 报出上述假错误。
2. 更严重的是**两套 rustup 安装并存**，各自构建不同 target 目录：

   | 安装位置 | 工具链 | target 目录 | linker |
   |----------|--------|-------------|--------|
   | `C:\Users\lanli\.rustup` | stable-**msvc** | `rust/crates/target` | link.exe |
   | `D:\rustup` | stable-**gnu** | `D:/tigshop/rust-target-fresh` | gcc |

   两者互不知晓，但**共享同一份内存和同一份 `~/.cargo/registry` 源码**，
   并发时 registry 的 build script 输出目录也会互相踩踏。

**强制规则（所有 AI 必须遵守）**：

1. **同一时刻整个仓库只允许一个 cargo 进程**。动手前先查：
   ```powershell
   Get-Process cargo,rustc,rustup -ErrorAction SilentlyContinue
   ```
   有输出就等它结束，**不要**再启动第二个。
2. 构建时限制并行度，降低内存峰值：
   ```powershell
   cargo check --workspace -j 2
   ```
3. 统一使用 **MSVC 工具链**（`C:\Users\lanli\.cargo\bin\cargo.exe`），
   不要用 `D:\rustup` 的 GNU 工具链，避免双 target 目录同时编译。
4. 一旦出现 `only metadata stub` / `can't find crate for std` 这类错误，
   **先确认没有其他 cargo 在跑，再 `cargo clean -p <crate>` 或删除
   `target/debug/deps` 中对应产物后重试**，不要去改代码。
5. 验证编译时若主 `target/` 正被占用，可用独立目录避免抢锁：
   ```powershell
   $env:CARGO_TARGET_DIR='D:\tigshop\rust-target-verify'; cargo check --workspace -j 2
   ```

> 教训：本次 3 个子代理并行开发时全部"失败"，实际是并发构建耗尽内存导致
> rustc 崩溃，**代码产出本身是好的**（见下节各域完成度）。

## ⚠️ 事故记录：PowerShell 会损坏无 BOM 的 UTF-8 中文源文件（2026-10-13 15:2x）

**绝对禁止**用 PowerShell 5.1 的 `Get-Content`/`Set-Content`/`-replace` 处理
`crates/` 下的 Rust 源文件，例如：

```powershell
# 错误示范：Get-Content 不带 -Encoding 会按系统 ANSI(GBK) 解码无 BOM 的 UTF-8！
(Get-Content $f -Raw) -replace 'a','b' | Set-Content $f -NoNewline -Encoding UTF8
```

后果（在 `infrastructure/src/db/repo/slice005.rs` 上实测，521 行文件）：
1. 全部中文注释变 mojibake（不可逆：GBK 解码时非法字节对已被替换为 `?`）；
2. 中文句号 `。`(E3 80 82) 的尾字节 0x82 与紧随的 0x0A 组成非法对时
   **换行符被整对吞掉**，521→479 行，注释与代码挤成一行 → 语法全崩；
3. 代码字符串的闭合引号/反引号同理被吞（如 `"品牌不存在"` → `...鍦?`），
   引发跨行的 `unterminated string` 连锁假错误。

**安全做法**：
- 编辑源文件只用 AI 的 Edit/Write 工具（UTF-8 安全）；
- 必须用脚本批改时用 **Python**（`io.open(f, encoding='utf-8')`），不要用 PowerShell；
- 恢复手段：按「mojibake 字符 → GBK 编码回字节」的逆变换 + 在
  `字 + ? + 行首模式` 处补回换行（本次已恢复全部 42 处换行，行数 479→521 复原）。

**本次已修复**：slice005.rs 语法与 2 处被吞引号、services/slice005.rs
`create()` 缺 `brand_id` 初始化、`name_length_is_one_to_thirty_chars` 测试
自相矛盾（16 字符按 `@Size` 口径合法，改为 31 字符）。修复后
`cargo check --workspace` 0 error 0 warning，`cargo test --workspace` 全绿（234+ 用例）。

## 待认领（欢迎其他 AI 认领，认领时改状态）

- `shop`：商户/店铺/入驻
- `member`：会员等级/标签/积分流水
- `decorator`：装修/导航/楼层
- `marketing`：秒杀/拼团/限时折扣/积分兑换
- `distribution`：分销员/佣金/业绩
- `im`：客服会话/消息
- `finance`：保证金/提现/分账/结算
- `logistics`：运费模板/物流公司/发货
- `search`：ES 搜索

## 🆕 根级路由通道 `crates/api/{admin,public}/src/root/`（2026-10-14 协调者新增）

**问题**：契约里有 **40** 个端点的路径**不带** `/adminapi` 或 `/api` 前缀。
原因是原 Java 少数控制器的 `@RequestMapping` 本身就没写模块前缀
（对照 `java/tigshop-adminapi/.../im/IMConfigController.java:41`
的 `@RequestMapping("im/config/config")`）。它们的真实 URL 是
`<应用根>/im/...`。这些切片若按常规放进 `routes/`（被 `nest("/adminapi")`
包住），实际服务路径会变成 `/adminapi/im/...`：前端 404，
且 `tools/check_api_coverage.py` 会同时判「未实现」+「疑似多余路由」。

**受影响的切片（全部是纯根级切片，共 40 个端点）**：

| slice | 控制器 | 端点数 | 前缀 | 需登录 |
|-------|--------|--------|------|--------|
| slice-020 | `IMConfigController` | 2 | `/im` | 是 |
| slice-095 | `ImServantController` | 3 | `/im` | 是 |
| slice-097 | `ImConversationController` | 23（admin 12 + api 11） | `/im` | 是 |
| slice-140 | `ImMessageController` | 8（admin 5 + api 3） | `/im` | 是 |
| slice-134 | `FilesController` | 3 | `/img` `/video` `/app` | **否** |
| slice-156 | `ShortUrlController` | 1 | `/s` | **否** |

（免登录判定已核对 Java 白名单：`SecurityConfig.URL_WHITELIST` 含
`/img/**`、`/video/**`、`/app/**`；`WhiteListConfig.URL_WHITELIST` 含
`/s/**`；两边都**不含** `/im/**`。）

**约定**：

1. 根级路由文件放 `crates/api/{admin,public}/src/root/sliceNNN.rs`，
   签名与 `routes/` 完全一致：
   `pub fn routes(state: &AdminState) -> Router<AdminState>` +
   `pub fn implemented_routes() -> Vec<RouteDoc>`。
2. 路由串与 `RouteDoc.path` 都写**完整契约路径**（`/im/...`、`/img/...`、`/s/{code}`），
   **不要**再拼 `/adminapi` / `/api`。
3. 注册由协调者手改 `src/root/mod.rs`（`merge_protected` 需登录 /
   `merge_public` 免登录），`tools/register_slices.py` **不覆盖**根级切片。
4. `tools/check_api_coverage.py` 与 `tools/check_route_conflicts.py` 已识别
   `src/root/` 下的文件不加 crate 前缀；后者的冲突判定也已改为
   **按应用（admin:8080 / api:8181）分别判断**——两端各自注册同一条
   `/im/...` 是合法的，不会 panic。
5. 域实现者**不要**在根级路由文件里自己加认证中间件，
   `router()` 已对 `merge_protected` 统一套 `require_auth`。

## 🆕 批量开工脚手架 `tools/scaffold_slices.py`（2026-10-14 协调者新增）

`register_slices.py` 只往共享文件插注册行，它要求 `sliceNNN.rs` **已存在**，
否则 `pub mod sliceNNN.rs;` 直接 `E0583`。剩余 180+ 片逐片手写 4~5 个骨架不可持续，
故本工具按 `slice187` / `slice021` 的既有样板一次生成：

    cd rust
    python tools/scaffold_slices.py slice-042 slice-100          # 指定片号
    python tools/scaffold_slices.py --from-coverage-gap --limit 12   # 缺口最小的若干片

产出：`core/src/models/`、`infrastructure/src/db/repo/`、`services/src/`、
`api/{admin,public}/src/routes/` 四件套 + `docs/registrations/sliceNNN.json`。
**幂等**：已存在的文件一律不覆盖，域实现者的产出永远安全。
生成后仍需协调者逐片跑 `register_slices.py --file ...` 落注册行，再跑门禁。

注意：本工具**不处理根级切片**（`src/root/` 下的 6 片仍由协调者手改 `root/mod.rs`，
见上一节第 3 条）。

## ⚠️ 派发纪律：数字只能来自命令（2026-10-14 事故复盘）

同一批 3 个子代理（slice-001 / slice-002 / im-root）全部在 150 轮上限处终止，
自述分别"已交付 33 / 18 / 36 个接口"。协调者实测：

| 自述 | 实测（`wc -l` + `grep -c '\.route('` + `check_api_coverage.py`） |
|------|------------------------------------------------------------------|
| slice-001 交付 33 | admin 文件 9 条 `.route(`、public 文件 1 条；覆盖率无变化 |
| slice-002 交付 18（`/b2b-api/**`） | 4 条 `.route(`，仓库里**根本不存在** `b2b-api` 字样 |
| im-root 交付 36 | `root/slice095|097|140.rs` 仍是 24 行骨架，`.route(` 计数 **0** |

因此：

1. **子代理的完成报告一律当作假设**，必须用 `grep -c` / `wc -l` /
   `check_api_coverage.py` 复核后才允许进入结论；冲突时如实说明冲突。
2. **单代理负载压到 5 片 / ≤10 个接口**，并要求它优先做完再谈扩展。
3. 派发提示里必须写明：**汇报的每个数字都得是它自己刚跑过的命令输出**，
   没做完的逐条写"未实现"。
4. 协调者对**自己**的同一要求：`cargo_exit=` 行、门禁工具的原始输出行，
   先跑后说，不复述记忆里的旧数字。
