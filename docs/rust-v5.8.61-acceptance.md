# Rust v5.8.61 对齐验收清单（2026-10-31）

## 1. 源码与版本

```text
Java 基线：java-b2b2c-overseas-v5.8.61
当前 Rust：fix/lowfreq-wip
生产版本：build-126
```

## 2. method+path 契约

审计脚本：

```text
tools/route-diff-v5861.py
docs/rust-v5.8.61-route-diff.json
```

结果：

```text
Java 解析路由：1743
Rust 路由：2118
缺失：0
```

说明：

```text
Rust 额外 380 条为别名、兼容路由、辅助路由，不影响契约覆盖。
```

## 3. 数据库

已执行 v5.8.57 → v5.8.61 增量：

```text
5.8.57-5.8.58
5.8.58-5.8.59
5.8.59-5.8.60
5.8.60-5.8.61
```

备份：

```text
/www/backup/tigshopmall-pre5861-20261031-0041.sql.gz
/www/backup/tigshopmall-pre-b9-20261031-0236.sql.gz
```

已补齐：

```text
product_sale_control / vendor_product_sale_control
offline_payment_claim
label_* / shop_group* / image_search_* / currency_dictionary
translation_call_log / wechat_shipping_sync_task / map_call_log
product.product_volume / product_sku.sku_weight / sku_volume
order.table_id / table_no
POS 桌台/区域 v5.8.61 新列
```

## 4. 批次状态

```text
B1 商品禁售              完成（公开过滤 + 后台 saleStatus + 供应商禁售）
B2 体积/重量运费          完成（保存、计算、测试）
B3 线下支付认款           完成（提交/详情/历史/审核/入账）
B4 图片搜索               代码完成（阿里云签名/检索），待真实凭证联调
B5 多语言补充             完成（币种字典/更新/翻译别名）
B6 供应链                 主体完成（供应商/商品绑定/采购/入库/退货/对账）
B7 店铺分组/标签          完成
B8 主流程修复             大部分完成（商户申请/供应商/AI/礼品卡）
B9 POS                    结构与状态机完成，build-126 已上线
```

## 5. 构建与回归

```text
cargo check --workspace
通过
```

```text
cargo test --workspace --lib
599 + 131 + 6 + 1616 + 554 + 8 + 18 + 1586 全部通过
```

```text
GitHub Actions build-126
sha256 732fae346f054a7295e804ded49f24ad7511de8671e26b5d2dcc2b5ccbb59f8b
```

## 6. 生产验证

```text
服务：tigshop active
备份：/www/backup/tigshop.preB126.bak
```

```text
/api/health                       200
/adminapi/health                  200
/api/home/home/pcIndex            200
/api/product/product/list         200
/admin/                           200
admin login                       success
/adminapi/pos/table/list          200
```

## 7. 剩余行为缺口

```text
1. 阿里云图搜需要真实 AccessKey / 图库实例做端到端验收
2. 供应商资质上传仅路由，文件落库未完成
3. 采购订单修改不重算明细
4. 对账单详情未返回来源/明细快照
5. 商品原图 ZIP 下载未实现
6. AI 图片修复/真实翻译未接服务
7. 商户银行/本地媒体列表为空实现
8. 多门店标签打印 worker / 门店保证金未接
9. Admin/PC 前端尚未同步到 v5.8.61 新页面
10. POS 开台/转台仅本地验证，生产尚未做写入回归
```

## 8. 结论

```text
契约：已完整覆盖
数据库：已升级
构建/测试：通过
生产：build-126 在线
核心链路：大部分完成
行为细节：仍有第 7 节缺口
```

因此总目标尚未 100% 完成，继续保持 active。
