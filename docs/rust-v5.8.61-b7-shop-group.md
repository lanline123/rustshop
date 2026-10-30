# B7 店铺分组 / 店铺标签实现记录

## 已完成

### 店铺/门店分组

```text
GET  /adminapi/merchant/shopGroup/list
GET  /adminapi/merchant/shopGroup/detail
POST /adminapi/merchant/shopGroup/create
POST /adminapi/merchant/shopGroup/update
POST /adminapi/merchant/shopGroup/del
GET  /adminapi/merchant/shopGroup/shopList
GET  /adminapi/merchant/shopGroup/selectList
```

表：

```text
shop_group
shop_group_relation
```

实现：

```text
按 shopType 1/2 分页
关键词 groupName
详情返回 shopIds
创建/编辑全量替换关联
删除分组及关联
组内店铺分页（shop_id=0 返回平台自营）
selectList 只返回公开字段
```

### 店铺标签

```text
GET  /adminapi/shop/tip/list
GET  /adminapi/shop/tip/detail
POST /adminapi/shop/tip/create
POST /adminapi/shop/tip/update
POST /adminapi/shop/tip/del
POST /adminapi/shop/tip/batch
POST /adminapi/shop/tip/updateField
GET  /adminapi/shop/tip/selectAllTip
GET  /adminapi/shop/tip/tipConfig
```

表：

```text
tip
store_tip
```

实现：

```text
分页/详情
创建/修改
删除标签及 store_tip 关联
批量删除
字段更新：tipName/status
selectAllTip 只返回启用标签
```

### 公开端

```text
GET /api/shop/shop/groupDetail?groupId=&shopType=
```

返回：

```text
groupId / groupName / shopType / shopIds
```

## 验证

```text
cargo check -p tigshop-admin-api -p tigshop-api
Finished
```

路由差异：

```text
B6：missing=53
B7：missing=36
```

## 提交

```text
109346e3 feat(merchant): add shop groups and shop tips
```

## 未完成

```text
分组内店铺搜索的高级过滤
标签排序/批量启停
标签在店铺列表/详情中的装配
完整二进制 build / 部署
```
