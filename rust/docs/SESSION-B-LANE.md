# ⚠️ 并行会话领地声明（会话 B — 2026-10-14 15:5x）

> 本文件由**第二个并行 AI 会话（会话 B）** 创建，用于向会话 A 声明独占领地，
> 避免两个会话同时改同一批文件。**会话 A 无需修改本文件，也不要认领下列切片。**

## 会话 B 认领的切片（adminapi-only，22 个接口）

| slice | 域 | 接口数 | 独占文件（会话 B 唯一写入者） |
|-------|----|--------|------------------------------|
| slice-163 | 税率管理 `TaxRateController` | 5 | `crates/api/admin/src/routes/slice163.rs`<br>`crates/services/src/slice163.rs`<br>`crates/infrastructure/src/db/repo/slice163.rs`<br>`crates/core/src/models/slice163.rs`（可选） |
| slice-210 | 配送类型 `ShippingTypeController` | 6 | `.../slice210.rs` 三件套 + `core/src/models/slice210.rs`（可选） |
| slice-157 | 示例 `ExampleController` | 7 | `.../slice157.rs` 三件套 + `core/src/models/slice157.rs`（可选） |
| slice-194 | 访问统计 `StatisticsAccessController`+`StatisticsUserController` | 4 | `.../slice194.rs` 三件套 + `core/src/models/slice194.rs`（可选） |

**开工前已核对**：上述 12 个文件名在仓库中**均不存在**（`Get-ChildItem -Recurse -Filter slice163.rs` 等均为空），
且这 4 片在 `tools/status_report.py --pending` 中均为 `files=0/3`，与会话 A 在飞的
037/038/085/108/137/207 无交集。

## 会话 B 的自我约束（与会话 A 的既有纪律一致）

1. **只创建上表列出的独占文件**，其中 `core/src/models/sliceNNN.rs` 若创建，
   **不自己改** `core/src/models/mod.rs`，把 `pub mod` 声明写进“待合并登记”。
2. **绝不修改任何共享文件**：`api/{admin,public}/src/lib.rs`、`routes/mod.rs`、
   `services/src/lib.rs`、`infrastructure/src/db/repo/mod.rs`、`db/mod.rs`、
   `bin/src/main.rs`、任何 `Cargo.toml`、`core/src/entities/**`。
3. **绝不运行 `cargo` / `cargo check` / `cargo test`**（本机 13.7GB，
   并发编译会 OOM 并伪造编译错误；且会话 A 拥有唯一编译入口）。
4. 只写运行时 SQL（`sqlx::query` / `query_as` / `QueryBuilder`），
   禁用 `query!` / `query_as!` 宏（无 DATABASE_URL）。
5. 金额用 `rust_decimal::Decimal`，时间为毫秒 `i64`，路由路径逐字符照抄契约。

## 待会话 A / 主会话统一合并的登记（会话 B 绝不自己动共享文件）

`docs/registrations/slice163.json`、`slice210.json`、`slice157.json`、`slice194.json`
四个登记文件由**会话 B 写入**（该目录是 append-only 的每片独立小文件，
两个会话各自写自己的文件名，不冲突）；**合并动作**
（`python tools/batch_pipeline.py`）由主会话串行执行。

会话 B 需要的合并行（`needs_registration` 明细）记录在
`docs/registrations/sliceNNN.json` 同目录的 `sessionB-merge-notes.md` 中。

## 边界

- 会话 B **不**执行 `register_slices.py` / `batch_pipeline.py`（会改共享文件）。
- 会话 B **不**执行 `git commit` / `git checkout` / `git stash`（会影响会话 A 的工作区）。
- 若会话 A 已先行认领上述任一切片，请直接在本文件下方追加一行说明，会话 B 会立刻让出。
