# TigShop Java → Rust 结构重构报告（v2）

> 日期：2026-10-17　基线：`df28f2ca`（迁移前）→ 本文对应提交（迁移后）
> 结论：**旧「编号切片」结构已整体推翻，替换为 14 个限界上下文的领域分层结构；接口覆盖、路由、SQL、单测全部与迁移前等价或更好。**

---

## 1. 一句话结论

旧实现把 220 个 API 切片机械拆成 `sliceNNN.rs` × 4 层（529 个文件、4,738 处编号引用），
产生 115 字段上帝状态、591 行手工装配、59 个「可编译但 0 路由」的空壳。
本次重构**全部废弃编号切片**，按业务域重组为 14 个限界上下文 × 94 个 feature，
状态收敛为 13/11 个域字段，并新增可机器判定的结构门禁。
**行为保持不变：635/1564 接口、645 条路由、1041 条 SQL 与迁移前逐项相同。**

---

## 2. 迁移前的问题（实测）

| 问题 | 证据 |
|------|------|
| 编号命名无业务语义 | 529 个 `sliceNNN` 文件；4,738 处 `sliceNNN` 引用 |
| 横向割裂 | 一个功能跨 4 个 crate（models/repo/service/routes）且同编号 |
| 上帝对象 | `AdminState` 115 字段、`PublicState` 56 字段、`Repositories` 148 字段、`main.rs` 591 行 |
| 空壳注册 | 59 个切片「已注册、可编译、0 路由」，接口 100% 不可达（236 个文件） |
| 双轨实现 | 早期 20 个域文件与切片并存，同一控制器被拆到两个 feature |
| 门禁失真 | 旧工具按「文件存在/含 async fn」判进度，与真实路由可达性漂移 |

---

## 3. 目标结构与落地

```
crates/
├── core/src/models/<ctx>/<feature>.rs                  # DTO / Query / VO
├── infrastructure/src/db/repo/<ctx>/<feature>.rs       # SQLx 仓储（按域分组 Repositories）
├── services/src/<ctx>/<feature>.rs                     # 用例服务（域内 Services 聚合）
├── api/{admin,public}/src/{routes,root}/<ctx>/<feature>.rs  # 薄路由层
├── security/ · common/ · api/openapi/                  # 横切能力
└── bin/src/main.rs                                     # 唯一组合根（91 服务构造 + 14 域聚合）
```

14 个限界上下文：`identity / merchant / wholesale / catalog / trade / payment /
marketing / member / logistics / content / system / im / analytics / openapi`。
Feature 以业务资源命名（`product_manage`、`order_admin`、`user_company`、`config_basic`…），
禁止数字编号。

---

## 4. 执行过程（可复现）

| 步骤 | 工具 | 结果 |
|------|------|------|
| 1. 分类与映射 | `tools/restructure/_draft_map.py` | 220 切片 → 73 个真实实现 + 59 个空壳 + 88 未开工；0 ctx/feature 冲突 |
| 2. 结构性搬迁 | `tools/restructure/migrate.py` | 368 次移动 + 236 次删除；模块树/类型名/路径/状态字段全部重写；聚合器重生成 |
| 3. 清理零路由壳 | `tools/restructure/cleanup_routes.py` | 删除 5 个不贡献路由的路由文件（含 2 个被 `content::decorate` 接管的 PcCatFloor/PcNavigation） |
| 4. 域分组（Stage-2） | `tools/restructure/group_state.py` | 91 个服务 → 14 个 `Services` 聚合；State 从 74/36 字段降到 13/11 |
| 5. 权威映射回写 | `tools/restructure/sync_domain_map.py` | `docs/domain-map.json`：`slices[]` + 19 个 `baseline[]` + 251 个 `controllers[]` |
| 6. 结构门禁 | `tools/check_structure.py`（新增） | `--strict` exit 0：0 切片命名 / 0 布局越界 / 0 空壳 / 0 依赖越界 / 0 上帝状态 |

---

## 5. 迁移前后对比

| 指标 | 迁移前 | 迁移后 |
|------|--------|--------|
| `sliceNNN` 文件 | 529 | **0** |
| `sliceNNN` 引用 | 4,738 | **0** |
| 业务模块 | 编号切片 × 4 层 | **14 ctx × 94 feature** |
| 空壳路由模块 | 59 切片 / 236 文件 | **0** |
| `AdminState` / `PublicState` | 115 / 56 字段 | **13 / 11 字段** |
| `Repositories` | 148 字段平铺 / 540 行 | **14 个域聚合 / 277 行** |
| `services/lib.rs` | 165 行（含死代码 AppState） | **15 行** |
| `bin/main.rs` | 591 行 | **435 行**（单一装配路径） |
| 接口覆盖率 | 635 / 1564 = 40.6% | **635 / 1564 = 40.6%**（逐项不变） |
| 已注册路由 / 多余路由 | 645 / 16 | **645 / 16** |
| 路由冲突 | 0 | **0** |
| SQL 语句 / 未知表列 | 1041 / 0 | **1041 / 0** |
| 单测 | 基线存在 5 个失败（历史遗留） | **2505 passed / 0 failed** |
| 文件行尾 | 32 个 CRLF 混用 | 被重写文件统一 LF |

---

## 6. 顺带修复的真实缺陷

1. **5 个历史遗留测试失败**（均非本次结构改动引入，迁移前基线即失败）：
   - `UserTrendsQuery` 对前端字符串参数 `dateType`/`isExport` 严格反序列化导致整请求 400 → 改为宽松解析（与 Java 容错一致）；
   - `parse_grow_setting("[1,2]")` 被 serde 按「具名结构体接受数组」解析成 `Some`，与 Java `parseObject` 只接受对象不符 → 增加对象校验；
   - `user_rank` / `feedback` 仓储两处测试断言与常量/Java 语义不符（`FROM user_rank ` 尾随空格、`order_id` 误判、platform 空 `shop_id` 口径）→ 按 Java 源码校正测试口径。
2. **空壳注册风险消除**：59 个「可编译不可达」切片及其注册全部删除，避免覆盖率虚报。
3. **SQL 门禁空转修复**：`check_sql_columns.py` 的 `glob("*.rs")` 改为 `rglob`，并加「扫描文件数 < 80 直接失败」下限；迁移后仍实测 105 文件 / 1041 条 SQL。
4. **结构门禁新增**：`check_structure.py` 可判定切片命名、模块布局、空壳路由、依赖方向、上帝状态、切片残留，`--strict` 作为 CI 门禁。

---

## 7. 已知遗留（按优先级，已写入 ARCHITECTURE.md §10）

1. **多控制器聚合 feature 拆分**：`catalog/product_manage`（43 接口，4 控制器）、
   `openapi/workbuddy`（28 接口）、`analytics/statistics`（2 控制器）需按
   `domain-map.json#split[]` 进一步拆分；
2. **bootstrap 内联路由下沉**：`api/{admin,public}/src/lib.rs` 仍内联约 20 条早期路由；
3. **RBAC 权限矩阵**：864 条接口带权限码，当前 `require_auth` 只校验登录态；
4. **事务 / MQ / 定时任务 / i18n**：沿用评审 `docs/architecture-review.md` §6 的路线。

---

## 8. 参考文档

- `docs/ARCHITECTURE.md` —— 目标结构契约与验收门禁（权威）
- `docs/architecture-review.md` —— 迁移前独立评审（含 12 项风险与 M1–M6 必须项）
- `docs/domain-map.json` —— 权威映射（220 切片 + 19 baseline feature + 251 控制器）
- `docs/domain-roadmap.md` —— 929 条待实现接口的按域路线图
- `docs/TOOLING.md` —— 门禁工具用法与退出码
- `docs/coverage-by-domain.json` —— 按域覆盖率快照
