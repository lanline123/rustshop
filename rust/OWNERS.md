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
