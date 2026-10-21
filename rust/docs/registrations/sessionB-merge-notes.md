# 会话 B 的待合并登记（slice163 / slice210 / slice157 / slice194）

> 本文件由**第二个并行 AI 会话（会话 B）**维护，记录它需要主会话合并进共享文件的
> 明细。**合并动作由主会话执行**（会话 B 不碰共享文件、不跑 cargo）。
> 四个 `docs/registrations/sliceNNN.json` 已由会话 B 写入
> （`python tools/write_regs.py slice163 slice210 slice157 slice194`），
> 因此主会话直接跑 `python tools/batch_pipeline.py` 即可拾取。

## ⚠️⚠️ 结论第一条：`docs/slices.json` 漏登记了 78 个真实接口（必须先看）

会话 B 在核对切片 210 时发现：`slices.json` 给 slice-210 列了 **6** 个接口
（`detail/create/update/del/updateField/batch`），但
`docs/api-contract.json` 与 Java 控制器
`ShippingTypeController.java` 里**实际有 7 个** —— 漏掉的是
**`GET /adminapi/setting/shippingType/list`**。

进一步全量比对（`contract endpoints` vs **所有**切片 `apis` 的并集）：

| 项 | 数量 |
|----|------|
| 契约接口总数 | **1547** |
| `slices.json` 所有切片并集 | **1469** |
| **不在任何切片里的接口** | **78**（GET 46 / POST 32；adminapi 48 / api 30） |

**规律**：漏掉的绝大多数是 `list` / `detail` 这类 GET（共 46 条 GET），
疑似 `tools/make_slices.py` 生成切片时对同名 GET 做了去重或过滤。

**后果（严重）**：
只要一个切片作者**只**按 `slices.json` 的 `apis` 清单实现，
它交付的切片就会**静默漏掉** `/list` 这类接口；而这些接口
因为不在任何切片的 `owned_files` 里，**永远不会有代理去实现**，
覆盖率报告也只会把它们算进「未实现」总数，不会指出"没人认领"。

**已被此坑影响、且已经在仓库里的证据**（会话 B 实测）：
- slice-001 声明 43 / 契约 47（漏 `product/list`、`product/detail`）
- slice-002 声明 30 / 契约 34（漏 `order/list`、`order/detail` 等）
- slice-135 声明 3 / 契约 11（漏 8 条文章接口）
- slice-035 声明 9 / 契约 15（漏 6 条优惠券接口）

**建议（给主会话）**：
1. **不要**只按 `slices.json` 派发；派发前用
   `docs/api-contract.json` 与该切片 `java_sources` 的控制器**对齐一次**
   并补齐差异，否则这批切片交付后仍有 78 个（或更多）接口无人实现。
2. 会话 B 已按契约实现（见下）：**slice-210 实现 7 条（含 `/list`）、
   slice-157 实现 7 条、slice-163 实现 5 条、slice-194 实现 4 条**，
   即会话 B 主动补上了 `slices.json` 漏掉的 `/list`。
3. 建议给 `tools/status_report.py` 增加一个「契约有、但无任何切片声明」
   的差集报表（会话 B **没有**改该工具，因为它属于共享工具，留给主会话）。

会话 B 的复现命令（只读，不改任何文件）：

```powershell
cd D:\tigshop\java-b2b2b-pro-v5.8.57\rust
python -c "
import json
s=json.load(open('docs/slices.json',encoding='utf-8'))
c=json.load(open('docs/api-contract.json',encoding='utf-8'))
d=set()
for sl in s:
    for a in sl['apis']: d.add((a['method'].upper(),a['path']))
k=set((e['method'].upper(), e['path']) for e in c['endpoints'])
print('契约',len(k),'切片并集',len(d),'无人认领',len(k-d))
"
```

---

## ⚠️ 第二条：会话 B 实测到的另一个会话的**路由冲突**（会导致启动 panic）

会话 B 跑 `python tools/check_route_conflicts.py` 得到 **2 条跨文件冲突**：

```
[admin] GET /adminapi/merchant/shopProductCategory/detail
         <- api/admin/src/routes/shop.rs        (category_detail)
         <- api/admin/src/routes/slice108.rs    (detail)
[admin] GET /adminapi/merchant/shopProductCategory/list
         <- api/admin/src/routes/shop.rs        (category_list)
         <- api/admin/src/routes/slice108.rs    (list)
```

两条都是 `shop.rs`（既有实现）与 **`slice108.rs`（另一会话在本轮新写的）**
注册了同一路径。**axum 的 `Router::merge` 遇到重复路径会在启动时 panic**，
因此这批一旦合并注册，**8080 端口会起不来**。

会话 B 的处理：**不碰**这两个文件（不属于会话 B 的领地），仅登记。
请主会话在 `batch_pipeline.py` 的「路由冲突校验」步处理
（要么让 slice108 让出这两条，要么删除 `shop.rs` 中的重复项）。

> 注：会话 B 自己的 4 个切片经同一工具校验**无冲突**
> （因为会话 B 的路由文件尚未注册进 `routes/mod.rs`，
> 冲突工具只能看到已注册者；会话 B 另外用文本比对确认了
> 自己 4 个域共 23 条路径与仓库内已有 `.route(` 路径**零重叠**）。

---

## 会话 B 的交付清单（23 条路由）

### slice-163 税率管理（5 条，`adminapi` only）

独占文件：
- `crates/core/src/models/slice163.rs`
- `crates/infrastructure/src/db/repo/slice163.rs`
- `crates/services/src/slice163.rs`
- `crates/api/admin/src/routes/slice163.rs`

| 方法 | 完整契约路径 | Java handler |
|------|--------------|--------------|
| GET  | `/adminapi/setting/taxRate/list`   | `TaxRateController.list` |
| GET  | `/adminapi/setting/taxRate/detail` | `TaxRateController.detail` |
| POST | `/adminapi/setting/taxRate/create` | `TaxRateController.create` |
| POST | `/adminapi/setting/taxRate/update` | `TaxRateController.update` |
| POST | `/adminapi/setting/taxRate/del`    | `TaxRateController.del` |

`detail` 的查询参数名是 **`taxRateId`**（不是 `id`）。

### slice-210 配送类型（7 条，`adminapi` only，**已补齐 slices.json 漏掉的 `/list`**）

独占文件：
- `crates/core/src/models/slice210.rs`
- `crates/infrastructure/src/db/repo/slice210.rs`
- `crates/services/src/slice210.rs`
- `crates/api/admin/src/routes/slice210.rs`

| 方法 | 完整契约路径 | Java handler |
|------|--------------|--------------|
| GET  | `/adminapi/setting/shippingType/list`        | `ShippingTypeController.list` |
| GET  | `/adminapi/setting/shippingType/detail`      | `ShippingTypeController.detail` |
| POST | `/adminapi/setting/shippingType/create`      | `ShippingTypeController.create` |
| POST | `/adminapi/setting/shippingType/update`      | `ShippingTypeController.update` |
| POST | `/adminapi/setting/shippingType/del`         | `ShippingTypeController.del` |
| POST | `/adminapi/setting/shippingType/updateField` | `ShippingTypeController.updateField` |
| POST | `/adminapi/setting/shippingType/batch`       | `ShippingTypeController.batch` |

### slice-157 示例（7 条，`adminapi` only）

独占文件：
- `crates/core/src/models/slice157.rs`
- `crates/infrastructure/src/db/repo/slice157.rs`
- `crates/services/src/slice157.rs`
- `crates/api/admin/src/routes/slice157.rs`

| 方法 | 完整契约路径 | Java handler |
|------|--------------|--------------|
| GET  | `/adminapi/example/example/list`        | `ExampleController.list` |
| GET  | `/adminapi/example/example/detail`      | `ExampleController.detail` |
| POST | `/adminapi/example/example/create`      | `ExampleController.create` |
| POST | `/adminapi/example/example/update`      | `ExampleController.update` |
| POST | `/adminapi/example/example/del`         | `ExampleController.del` |
| POST | `/adminapi/example/example/updateField` | `ExampleController.updateField` |
| POST | `/adminapi/example/example/batch`       | `ExampleController.batch` |

### slice-194 访问统计（4 条，`adminapi` only）

独占文件：
- `crates/core/src/models/slice194.rs`
- `crates/infrastructure/src/db/repo/slice194.rs`
- `crates/services/src/slice194.rs`
- `crates/api/admin/src/routes/slice194.rs`

| 方法 | 完整契约路径 | Java handler |
|------|--------------|--------------|
| GET  | `/adminapi/panel/statisticsAccess/accessStatistics`       | `StatisticsAccessController.accessStatistics` |
| GET  | `/adminapi/panel/statisticsUser/addUserTrends`            | `StatisticsUserController.addUserTrends` |
| GET  | `/adminapi/panel/statisticsUser/userConsumptionRanking`   | `StatisticsUserController.userConsumptionRanking` |
| GET  | `/adminapi/panel/statisticsUser/userStatisticsPanel`      | `StatisticsUserController.userStatisticsPanel` |

---

## 主会话需要执行的合并步骤（会话 B 不执行）

```powershell
cd D:\tigshop\java-b2b2b-pro-v5.8.57\rust

# 1) 合并注册（幂等；会把上面 4 片的 mod 声明 / State 字段 / merge 行写进共享文件）
python tools/register_slices.py --dir docs/registrations

# 2) 门禁：一致性 / SQL 列名 / 路由冲突 / 覆盖率
python tools/verify_registrations.py --strict
python tools/check_sql_columns.py --strict
python tools/check_route_conflicts.py --strict     # 注意上面那 2 条既有冲突
python tools/check_api_coverage.py

# 3) 串行编译（唯一编译入口，-j1 防 OOM）
cd crates; $env:CARGO_BUILD_JOBS=1; cargo check --workspace -j 1
cargo test --workspace
```

### 需要主会话补的**共享文件**改动（会话 B 一律没碰）

| 文件 | 需要的改动 |
|------|-----------|
| `crates/core/src/models/mod.rs` | `pub mod slice163; pub mod slice210; pub mod slice157; pub mod slice194;` |
| `crates/infrastructure/src/db/repo/mod.rs` | 同上 4 个 `pub mod` |
| `crates/infrastructure/src/db/mod.rs` | `Repositories` 增加 4 个字段：`slice163` / `slice210` / `slice157` / `slice194`，类型 `SliceNNNRepository`，并在构造处 `SliceNNNRepository::new(pool.clone())` |
| `crates/services/src/lib.rs` | 同上 4 个 `pub mod` |
| `crates/api/admin/src/routes/mod.rs` | 同上 4 个 `pub mod` |
| `crates/api/admin/src/lib.rs` | `AdminState` 增加 4 个字段 `pub sliceNNN: Arc<SliceNNNService>,`；`routes::merge_all` 挂载 `.merge(sliceNNN::routes(state))`；`route_docs`（或等价处）`out.extend(sliceNNN::implemented_routes());` |
| `crates/bin/src/main.rs` | 装配 4 个服务：`let sliceNNN = Arc::new(SliceNNNService::new(repos.clone()));` 并注入 `AdminState` |

四个服务构造函数签名统一为
`pub fn new(repos: Arc<Repositories>) -> Self`（与 OWNERS.md 第 3 条一致）。

> 会话 B 未在本轮运行 `register_slices.py`，因为该脚本会写共享文件，
> 而另一个会话正在活动——两个会话同时改共享文件必然互相覆盖。
> 请主会话在**确认另一个会话已停止写共享文件**后再执行。
