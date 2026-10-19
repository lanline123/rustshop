# Excel 导入 / 导出（已实测）

> Java 侧依赖 EasyExcel/POI + `ExcelUtils` + `@ResponseExcel` 直接写 `HttpServletResponse`；
> Rust 侧用 `rust_xlsxwriter`（写）+ `calamine`（读）统一封装，表头/列顺序对齐 Java `@ExcelProperty`。

## 1. 基础设施

| 能力 | 位置 |
|---|---|
| 生成 xlsx（首行表头） | `tigshop_services::system::excel::build_xlsx(headers, rows, sheet)` |
| 读取 xlsx（首个工作表 → `Vec<Vec<String>>`） | `tigshop_services::system::excel::read_xlsx(bytes)` |
| 下载响应（content-type + attachment 文件名） | `crates/api/admin/src/excel_response.rs` |

## 2. 已实现端点（实测）

| 端点 | 说明 | 实测 |
|---|---|---|
| `GET /adminapi/product/stockWarning/downloadTemplate` | 库存预警导入模板（商品编码/SKU编码/下限/上限） | xlsx ✓ |
| `GET /adminapi/product/stockWarning/export?ids=608_0,592_0` | 库存预警导出（12 列，列头与 Java `StockWarningExcelVO` 一致） | xlsx ✓ |
| `GET /adminapi/product/productBatch/productBatchDeal` | 商品批量导出（19 列，对齐 `ProductImportExcelVO`） | xlsx ✓（约 114KB） |
| `GET /adminapi/product/stock/downloadTemplate` | 商品出入库导入模板（商品名称/编码/规格名称/数量/备注） | xlsx ✓ |
| `POST /adminapi/product/stockWarning/import` | 库存预警 Excel 导入（multipart `file` + `shopId`） | 解析 + 校验 + 落库 ✓ |

### 导入实测

```
上传 1 行合法数据（SN000008 + SKU SN000008-1，下限 3 / 上限 30）
→ {"totalCount":1,"successCount":1,"failCount":0,"failItems":[]}
上传 2 行非法数据（多规格未填 SKU / 编码不存在）
→ {"totalCount":2,"successCount":0,"failCount":2,"failItems":[...fail reasons...]}
```

导入解析列位置：`商品编码 | SKU编码 | 下限 | 上限`（首行为表头，自动跳过空行）。

## 3. 新增 Excel 端点的标准做法

```rust
// 导出
let rows = state.catalog.xxx.export(&query).await?;       // 业务行
let headers = ["列1", "列2"];
let data: Vec<Vec<String>> = rows.iter().map(|r| vec![r.a.clone(), r.b.to_string()]).collect();
let bytes = tigshop_services::system::excel::build_xlsx(&headers, &data, "sheet")?;
return Ok(crate::excel_response::xlsx_response(bytes, "export.xlsx"));

// 导入
let sheet = tigshop_services::system::excel::read_xlsx(&bytes)?;
let rows = sheet.iter().skip(1).map(|row| XxxImportRow { ... }).collect::<Vec<_>>();
let result = state.catalog.xxx.import_rows(&rows).await?;
```

## 4. 尚未接 Excel 的端点（后续按同一模式补）

- 商品库存批量导入/调拨导入（`product/stock/batchImport`、`batchImportAllocate` 等 8 个）
- 导入失败数据 `getBatchDeliverImportErrorData`、发货模板下载 `downloadDeliverTemplate`
- 对账单导出 `finance/statement/exportStatement`、`exportStatementStatistics`
- 客户/会员导出、微信直播商品导入等

> 已完成（2026-10-19）：商品批量导入 `productBatchModify`、批量发货导入 `batchDeliverImport`
> （真实 xlsx + JSON/CSV）、电子卡密导入 `product/eCardGroup/import`、订单导出 `orderExport`（xlsx 二进制）。
> 其余端点仍返回「未迁移」或 JSON 行数据；Excel 基础能力已就绪，按 §3 模式逐个替换即可。
