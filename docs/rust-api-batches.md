# Rust 缺失接口补齐计划（基于本地 Java + 跨境版源码）

## 结论

本地 `java/` 是 5.8.57 完整 Java 后端；跨境版源码在：

```text
D:\tigshop\java-b2b2c-overseas-v5.8.39-cd82043d4910-4bb8dc526b5c4974ac1dedb29ae6bfbc
```

两边合起来可以覆盖大部分缺失接口和前端页面，但不能在一次构建内全部完成，需要分批移植、验证、部署。

## 当前审计

```text
Apifox 唯一接口：1600
Rust 已有对应：1472
未找到：128
```

对 128 个缺失接口做源码命中检查（按方法级 Mapping/页面路径粗略匹配）：

```text
Java 后端源码可参考：84 / 129
前端源码可参考：97 / 129
```

剩余约 30~45 个接口在 5.8.39 跨境版里也没有，需要按 Apifox 契约新写，不能直接复制。

## 批次

### A：多语言 + 登录 + 海外支付

后端：

```text
/adminapi/lang/translations/*
/adminapi/lang/locales/*
/adminapi/lang/localesRelation/*
/adminapi/lang/currency/*
/api/user/oauth/*
/api/order/pay/notify/stripe
/api/order/pay/notify/xendit
/api/order/pay/notify/antom
/adminapi/setting/config/saveStripe|saveXendit|saveAntom
```

参考源码：

```text
java/tigshop-service/.../lang/
java/tigshop-service/.../oauth/
java/tigshop-service/.../pay/
java/tigshop-adminapi/.../setting/ConfigController.java
```

前端：

```text
java/view/admin/src/views/lang/
java/view/admin/src/views/setting/
```

### B：O2O 派送/服务人员/钱包

后端：

```text
/adminapi/o2o/*
/api/o2o/deliveryOrder/*
/api/o2o/serviceStaff/*
/api/o2o/deliveryOrder/wallet/*
```

前端：

```text
跨境版 view/uniapp/src/pages/store/
跨境版 view/uniapp/src/pages/member/components/decorate/
```

### C：财务员工资金/提现

```text
/adminapi/finance/staffFundWallet/*
/adminapi/finance/staffFundWithdraw/*
/adminapi/finance/serverStaffWithdraw/*
/adminapi/finance/deliveryStaffWithdraw/*
```

### D：POS 收银台

```text
/adminapi/pos/order/check/*
/adminapi/pos/cart/*
/adminapi/pos/user/*
/adminapi/pos/adminUserPos/*
```

### E：供应商/供应商员工

```text
/adminapi/vendor/*
/adminapi/authority/suppliers/*
```

### F：次卡 + 剩余配置/权限

```text
/api/cycleCard/*
/adminapi/setting/galleryVideo/treeList
/adminapi/setting/config/*
```

## 进度

### 2026-10-26 A1：多语言路由已补

```text
POST /adminapi/lang/locales/create
POST /adminapi/lang/currency/create
POST /adminapi/lang/localesRelation/create
POST /adminapi/lang/translations/update
POST /adminapi/setting/galleryVideo/treeList   （getAllCategory 的文档契约别名）
```

- service 层原本已存在，本次补挂路由并加入 implemented_routes
- `cargo check -p tigshop-admin-api --lib` 通过
- 审计未实现数：128 → 123
- commit：`04d78abe`、`ae779a28`

待继续：A2 OAuth、A3 海外支付回调/配置。

## 每批验收

```text
1. 从 Java 源码提取 DTO / 表 / 状态逻辑
2. Rust 补 model + repo + service + route
3. cargo check
4. 对照 Apifox 逐接口请求/响应验证
5. 前端补页面或组件
6. H5/admin 构建
7. 部署并更新 docs/rust-api-audit.md
```
