# TigShop Rust 工具链（v2 结构门禁）

> 适用：`rust/crates` 的 v2 域结构迁移与日常开发。
> 结构契约见 [`ARCHITECTURE.md`](ARCHITECTURE.md)；本文只说明**怎么跑门禁、各工具查什么、退出码是什么**。

所有脚本都是**只读静态扫描**（唯一例外：`coverage_by_domain.py` 会写
`docs/coverage-by-domain.json`，`plan_domains.py --out` 会写计划 JSON），
不编译、不连数据库、不需要网络。

## 0. 命令总览

在 `rust/` 目录下运行（脚本内部按自身位置解析 `rust/` 根，不依赖当前目录）：

| 工具 | 作用 | 命令 | 写入 |
|------|------|------|------|
| `check_api_coverage.py` | 接口覆盖率（**进度唯一权威口径**） | `python tools/check_api_coverage.py` | `docs/api-coverage.json`（`--json`） |
| `check_route_conflicts.py` | axum 重复路由（启动 panic） | `python tools/check_route_conflicts.py --strict` | — |
| `check_sql_columns.py` | 仓储 SQL 表/列校验 | `python tools/check_sql_columns.py --strict` | — |
| `check_structure.py` | v2 结构门禁（本文 §1） | `python tools/check_structure.py --strict` | — |
| `coverage_by_domain.py` | 按 ctx/feature 的覆盖率 | `python tools/coverage_by_domain.py` | `docs/coverage-by-domain.json` |
| `plan_domains.py` | 分域实施批次计划 | `python tools/plan_domains.py --out docs/domain-plan.json` | 指定 `--out` |
| `check_frontend.py` | **前端**接口契约校验 / 规模统计 / 装修类型覆盖 | `python tools/check_frontend.py contract --strict`<br>`python tools/check_frontend.py scale`<br>`python tools/check_frontend.py decorate` | — |
| `verify_frontend_doc.py` | 校验 `FRONTEND-MIGRATION-TASKS.md` 数字与实测一致 | `python tools/verify_frontend_doc.py` | — |

合并前必跑（ARCHITECTURE §7）：`cargo check --workspace --all-targets`、
`cargo test --workspace`，以及上表中 `check_api_coverage.py`（不低于基线且只增不减）、
`check_route_conflicts.py --strict`、`check_sql_columns.py --strict`、
`check_structure.py --strict`。**这些 Python 门禁不跑 cargo**；cargo 由主会话串行执行。

## 1. `check_structure.py` —— v2 结构门禁

```bash
cd rust
python tools/check_structure.py            # 报告（不阻塞，exit 0）
python tools/check_structure.py --strict   # 有违规 -> exit 1（CI 门禁）
python tools/check_structure.py --json     # stdout 只输出 JSON
```

检查项与判据：

| 类别 | 判据 | strict |
|------|------|--------|
| 切片命名文件 | `crates/*/src` 下路径含 `sliceNNN`（`\bslice\d{3}\b`） | 违规 1 |
| 切片 mod 声明 | 源码中 `mod sliceNNN;` | 违规 1 |
| 模块树布局越界 | 见下表「合法布局」 | 违规 1 |
| 未知 ctx | 布局第二层目录名不在 14 个限界上下文内 | 违规 1 |
| 空壳路由 | `routes()` 内无任何 `.route(`，或 `implemented_routes()` 返回空 `Vec` 字面量 | 违规 1 |
| 依赖方向 | 源码 `use tigshop_<crate>` 形成禁止的 crate 依赖边（见下表） | 违规 1 |
| 上帝状态 | `AdminState`/`PublicState` 字段数 > 20，或 `Repositories` 字段声明行 > 200 | 违规 1（非 strict 仅警告） |
| 切片残留引用 | 非切片文件里仍引用 `sliceNNN`（use 路径/类型名/状态字段） | 仅警告 |

**合法布局**（其余位置一律不扫描布局；`entities/`、`cache/` 等按 ARCHITECTURE 放行）：

- `core/src/models/<ctx>/<feature>.rs`（+ `<ctx>/mod.rs`）
- `infrastructure/src/db/repo/<ctx>/<feature>.rs`（+ `<ctx>/mod.rs`）
- `services/src/<ctx>/<feature>.rs`（+ `<ctx>/mod.rs`）
- `api/{admin,public}/src/{routes,root}/<ctx>/<feature>.rs`（+ `<ctx>/mod.rs`）
- 各 crate `src/lib.rs`、`routes/mod.rs`、`root/mod.rs`、`models/mod.rs`、`db/repo/mod.rs`

**允许的依赖方向**（ARCHITECTURE §2 的传递闭包；只解析源码 `use`，不解析 Cargo.toml）：

```
core:            （不得依赖任何工作区 crate）
common:          core
security:        core, common
infrastructure:  core, common
services:        core, common, infrastructure, security
api/admin:       core, common, infrastructure, security, services, api/openapi
api/public:      core, common, infrastructure, security, services, api/openapi
api/openapi:     （无）
bin:             以上全部
```

违规示例：`core -> services`、`infrastructure -> services`、`services -> api/admin`、
`services -> api/public`、`api/admin -> api/public`。crate 内用自身包名（如
`core` 里写 `use tigshop_core::...`）单独记为 `self` 违规，应改用 `crate::`。

**ctx 来源**：优先读 `docs/domain-map.json` 的 `contexts`；文件缺失或损坏时回退
ARCHITECTURE §3 的 14 域并在报告/JSON 中注明来源（门禁不因 domain-map 暂缺而报错）。

**`--json` 结构**（节选）：`scanned`、`contexts`/`contexts_source`、`counts`、
`violations.{slice_files,slice_mod_decls,layout,unknown_ctx,empty_scaffolds,dependencies[,god_state]}`、
`warnings.{slice_refs,god_state}`、`god_state_measured`、`blocking`、`ok`、`exit_code`。

## 2. `coverage_by_domain.py` —— 按 ctx/feature 覆盖率

```bash
cd rust
python tools/coverage_by_domain.py                 # 表格 + 写 docs/coverage-by-domain.json
python tools/coverage_by_domain.py --json          # stdout 只输出 JSON
python tools/coverage_by_domain.py --top 30        # 待办特性展示 30 条
python tools/coverage_by_domain.py --strict        # 仍有未实现接口 -> exit 1
```

- **提取器唯一**：直接 `import check_api_coverage` 并调用 `collect_rust_routes()` /
  `analyse()`，不复制语义——两套口径漂移曾导致误判进度（见 `plan_batches.py` 注释）。
- **归属**：`docs/domain-map.json` 的 `controllers[]`（controller+module）优先；
  其次 `slices[]` + `docs/slices.json` 的 apis（支持 `split[]` 按 controller 拆特性）；
  仍无归属时退化为 `feature=snake_case(controller)`、`ctx=_unknown_`，并在报告/JSON 中
  显式给出 `unmapped_endpoints` 数量（不静默编造归属）。
- 输出 `docs/coverage-by-domain.json` 字段：`total/implemented/pending/percent`、
  `unmapped_endpoints`、`by_ctx`、`by_feature[]`（含 `pending_endpoints[]`，是
  `plan_domains.py` 的输入）。

## 3. `plan_domains.py` —— 分域实施批次计划

```bash
cd rust
python tools/plan_domains.py                                  # 摘要
python tools/plan_domains.py --out docs/domain-plan.json      # 写计划
python tools/plan_domains.py --batch-size 8 --out x.json
python tools/plan_domains.py --coverage docs/coverage-by-domain.json --out x.json
```

- 输入：`docs/domain-map.json`（**必需**）+ `docs/api-contract.json`；覆盖率默认
  实时计算（`coverage_by_domain.build_report`，同一口径），也可用 `--coverage` 读取
  已生成的 JSON。
- 工作单位 = 一个 `ctx/feature`（一次提交只动该 feature 的四层文件 + 域聚合器）。
- 优先级缺省：**P0** identity/system/catalog/logistics/openapi；**P1**
  merchant/wholesale/trade/payment/member；**P2** marketing/content/analytics/im。
  若 `domain-map.json` 的 `slices[]` 显式给出 `priority`（P0/P1/P2），以显式值为准。
- 排序：P0→P2 → ctx 顺序（ARCHITECTURE §3）→ 待办接口数降序 → feature 名；
  分批时**同一批不混优先级**。
- 每个特性的计划字段：`ctx`、`feature`、`endpoint_count`、`total_endpoints`、
  `controllers`、`modules`、`java_sources`、`target_files`、`pending_endpoints[]`。
  `target_files` 固定四层：`core` / `infrastructure` / `services` / `api`（`api` 可能是
  1~2 个文件：同一 feature 可同时有 admin 与 public 路由）。
- 计划 JSON 顶层：`batch_size`、`total_pending_features`、`total_pending_endpoints`、
  `per_priority`、`batches.[{batch,priority,features[]}]`。

> 本工具取代 slice 时代的 `plan_batches.py` / `slice_prompt.py`：工作单位从
> 「编号切片」变为「ctx/feature」，且不再生成「共享文件注册片段」（v2 的聚合器由
> 本域 `mod.rs` 自行维护）。

## 4. 对 `docs/domain-map.json` 的依赖

`docs/domain-map.json` 由迁移前置任务（domain-taxonomy）生成，是唯一权威归属表。
本工具链假设其结构为：

```jsonc
{
  "version": "5.8.57",
  "contexts": ["identity", "...14 个..."],
  "slices": [{
    "slice_id": "slice-001", "ctx": "catalog", "feature": "product_manage",
    "coded": true, "java_modules": ["adminapi", "api"],
    "controllers": ["ProductBatchController", "ProductController"],
    "split": [{"feature": "product_batch", "controllers": ["ProductBatchController"]}]
    // 可选 "priority": "P0"
  }],
  "controllers": [{"controller": "AdminRoleController", "module": "adminapi",
                   "package": "authority", "ctx": "identity", "feature": "admin_role"}]
}
```

兼容性约定：

- `slices[]` 与 `docs/slices.json` 按 `slice_id` 对齐（容忍 `-`/`_` 差异）；
- `split[]` 存在时按 controller 归入子 feature；
- 缺 `domain-map.json` 时：`check_structure` 用内置 14 域继续工作；
  `coverage_by_domain` 退化为 `_unknown_` 归属并告警；`plan_domains` 直接
  **exit 2**（没有权威归属就不该派工）。

## 5. 退出码约定

统一遵循：**0 = 通过/成功；1 = 发现违规；2 = 工具错误**（路径不存在、文件损坏、
契约缺失等）。`--strict` 只影响「1 还是 0」；`--json` 不影响退出码。

| 工具 | 0 | 1 | 2 |
|------|---|---|---|
| `check_structure.py` | 通过 | `--strict` 且有违规 | 扫描根不存在/无源文件 |
| `coverage_by_domain.py` | 成功 | `--strict` 且仍有未实现接口 | 契约缺失/不可解析/写入失败 |
| `plan_domains.py` | 成功 | — | 缺 domain-map/契约不可读/写入失败 |
| `check_frontend.py contract` | 全部命中契约 | `--strict` 且有未命中路径 | 契约缺失/不可解析 |
| `check_frontend.py scale` / `decorate` | 成功 | — | 契约缺失/不可解析 |
| `verify_frontend_doc.py` | 数字一致 | 有数字不一致 | 文档不存在 |

## 6. Windows / 编码注意事项

- 全部脚本 UTF-8 输出（`sys.stdout.reconfigure(encoding="utf-8", errors="replace")`），
  写入文件均为 UTF-8（无 BOM）。
- 本仓库绝对 Windows 路径下 `grep` 工具不可靠，请用 `Select-String -Encoding utf8`
  或 Python；PowerShell 5.x 的 `>` 重定向会写成 UTF-16，保存 JSON 请用
  `Out-File -Encoding utf8` 或让工具直接写文件。
- Python 命令为 `python`（本机 3.12）。**不要**并发跑 `cargo`（16 核/13.7GB，易 OOM）。
