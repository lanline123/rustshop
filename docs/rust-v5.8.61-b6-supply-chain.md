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
