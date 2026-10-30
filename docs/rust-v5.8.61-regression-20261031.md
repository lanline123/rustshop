# 全量构建与回归记录（2026-10-31）

## 1. 全量编译

```text
cargo check --workspace
Finished
```

```text
cargo test --workspace --lib
所有测试通过
```

概览：

```text
tigshop-admin-api   599 passed
services            1586 passed
core                1616 passed
infrastructure      554 passed
其它 crate          129 / 6 / 8 / 18 passed
```

修复了一个既有测试缺口：`/setting/config/{stripe,xendit,antom}Settings|save*` 6 条规格已在端点表中，但漏在 `ROUTE_TABLE` 与测试断言里；已补齐为 108 条。

## 2. 完整二进制

```text
cargo build -p tigshop-server
Finished in 5m 29s
```

产物：

```text
rust/crates/target/debug/tigshop.exe
```

## 3. 本地启动冒烟

数据库：

```text
mysql://root:root@127.0.0.1:3307/tigshop_migtest
```

服务：

```text
api   127.0.0.1:18181
admin 127.0.0.1:18080
```

健康检查：

```text
GET /api/health           200 UP
GET /adminapi/health      200 UP
```

公开接口：

```text
GET /api/product/product/list?page=1&size=2   200
GET /api/search/image/capabilities            200
```

后台登录：

```text
POST /adminapi/login/signin   success
```

新能力冒烟：

```text
GET /adminapi/product/product/saleStatus?productId=621  200
GET /adminapi/merchant/shopGroup/list?shopType=1         200
GET /adminapi/supplyChain/supplier/list                  200
GET /adminapi/shop/tip/list                              200
GET /adminapi/setting/imageSearch/detail                 200
GET /adminapi/order/offlinePaymentClaim/list             200
```

全部返回 `code=0`。

## 4. 路由契约

```text
Java v5.8.61 静态解析：1743
Rust 路由字面量：2104
missing：3
```

剩余 3 条：

```text
GET  /order/pay/notify/stripe   Rust 为 POST
POST /vendor/product/platform/saleStatus  同路径 GET+POST，静态扫描误报
POST /vendor/stock/getImportErrorData     同路径 GET+POST，静态扫描误报
```

## 5. 结论

```text
编译：通过
测试：通过
二进制：构建成功
本地启动：成功
核心新接口冒烟：通过
```

仍未完成：

```text
生产部署
B4 阿里云图库真实检索
B6 部分采购保存/供应商商品保存
B9 POS 桌台状态机
前端同步
```
