# B6 供应商 / 采购链实现记录

## 已完成

新增 45 条路由（`/adminapi/supplyChain/*`）：

```text
供应商 supplier：list/detail/create/update/status/delete/qualification upload
供应商分类 supplierCategory：list/save/status/delete
供应商商品 supplierProduct：list/summary/selection/detail/status/delete/batchDelete/save/batchSave
采购单 purchaseOrder：list/detail/create/update/finish
入库单 purchaseReceipt：list/detail/save
对账单 purchaseReconciliation：list/detail/candidates/export/create/update/approve/reject/resubmit/void
退货单 purchaseReturn：list/detail/save/confirm/delete
采购设置 purchaseSetting：detail/save
```

表：

```text
vendor
purchase_supplier_category
vendor_product
purchase_order / purchase_order_item
purchase_receipt / purchase_receipt_item
purchase_reconciliation / purchase_reconciliation_item / purchase_reconciliation_source
purchase_return / purchase_return_item
purchase_setting
purchase_operation_log
```

实现方式：

```text
通用分页查询 + 通用行转 JSON
供应商/分类基础 CRUD
供应商商品状态/删除
采购类列表、详情、状态动作
采购设置读写
```

## 验证

```text
cargo check -p tigshop-admin-api
Finished
```

路由差异：

```text
B5：missing=98
B6：missing=53
```

## 当前限制

以下是可调通路由但业务动作仍未完整迁移：

```text
供应商商品 save / batchSave
采购单 create / update / finish
入库单 save
对账单 create / update
退货单 save
供应商资质上传
库存流水联动
采购金额/数量校验
对账审批后的资金/库存副作用
```

这些接口目前返回明确的“未迁移”业务错误，不伪造成功。

## 提交

```text
f21ca508 feat(supplychain): add supplier and purchase chain route baseline
```

## 下一轮

继续 B6 深水区：

```text
采购单创建 + 明细
入库单保存 + 库存写入
退货单保存 + 库存回滚
对账单创建/审批/驳回/作废
供应商商品绑定
```

## 2026-10-31 补充：采购单主链路

已把采购单从“未迁移”改为可用：

```text
POST /supplyChain/purchaseOrder/create
POST /supplyChain/purchaseOrder/update
POST /supplyChain/purchaseOrder/finish
```

create：

```text
校验供应商
生成 PO 单号
写 purchase_order
按 vendor_product_sku 写 purchase_order_item
汇总金额/数量
状态=1 待入库
```

update：

```text
仅未完成/未关闭状态可改
更新预计到货、配送方式、物流、运单、备注
```

finish：

```text
状态 1/2 → 3 已完成
写 finish_time / finish_reason
```

detail 现在返回 `items` 明细。

验证：

```text
cargo check -p tigshop-admin-api
Finished
```

## 2026-10-31 补充 2：入库/退货

已实现：

```text
POST /supplyChain/purchaseReceipt/save
POST /supplyChain/purchaseReturn/save
```

入库：

```text
仅关联采购订单（直采入库未迁移）
校验订单状态 1/2
按 orderItemId 入库
写 purchase_receipt + purchase_receipt_item
增加 product_stock / product_sku.sku_stock
累加 purchase_order_item.received_quantity
订单全部入库 → 3 已完成，否则 2 部分入库
```

退货：

```text
校验采购入库单已确认
校验可退数量
写 purchase_return + purchase_return_item
扣减 product_stock / sku_stock
更新 receipt_item.returned_quantity
```

验证：

```text
cargo check -p tigshop-admin-api
Finished
```

## 2026-10-31 补充 3：对账单

已实现：

```text
POST /supplyChain/purchaseReconciliation/create
POST /supplyChain/purchaseReconciliation/update
POST /supplyChain/purchaseReconciliation/approve
POST /supplyChain/purchaseReconciliation/reject
POST /supplyChain/purchaseReconciliation/resubmit
POST /supplyChain/purchaseReconciliation/void
```

create/update：

```text
校验供应商
生成 PC 单号
写入供应商快照
按 sourceType=1/2 汇总入库单/退货单金额
写 purchase_reconciliation_source
total_amount = receipt_amount - return_amount
```

状态流转：

```text
1 待对账 → 2 已对账
1 待对账 → 3 已驳回（必须填原因）
3 已驳回 → 1 待对账
3 已驳回 → 4 已作废
```

只允许已驳回的对账单修改。

验证：

```text
cargo check -p tigshop-admin-api
Finished
```
