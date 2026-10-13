# TigShop 开源商城系统 - 第 5 部分：API 接口文档大全

---

## 目录

1. [接口概览](#1-接口概览)
2. [adminapi - 后台管理 API](#2-adminapi-后台管理-api)
   - 2.1 权限管理 (authority)
   - 2.2 商户管理 (merchant)
3. [common - 公共方法](#3-common-公共方法)
4. [content - 内容管理](#4-content-内容管理)
5. [decorate - 装修系统](#5-decorate-装修系统)
6. [promotion - 营销功能](#6-promotion-营销功能)
7. [setting - 系统配置](#7-setting-系统配置)
8. [salesman - 分销系统](#8-salesman-分销系统)

---

## 1. 接口概览

### 1.1 接口分类

| 模块 | 路径 | 描述 | 接口数量 |
|------|------|------|---------|
| **adminapi** | `/adminapi/` | 后台管理接口 | 200+ |
| **api** | `/api/` | 前台用户接口 | 150+ |
| **common** | `/common/` | 公共接口 | 20+ |
| **im** | `/im/` | 客服聊天接口 | 30+ |

### 1.2 认证机制

所有需要认证的接口都使用 JWT Token：

```http
Authorization: Bearer <token>
```

### 1.3 响应格式

#### 成功响应
```json
{
  "code": 200,
  "msg": "success",
  "data": { ... }
}
```

#### 失败响应
```json
{
  "code": 400,
  "msg": "参数错误",
  "data": null
}
```

### 1.4 通用分页参数

大多数列表接口支持以下分页参数：

| 参数 | 类型 | 说明 | 默认值 |
|------|------|------|--------|
| `page` | integer | 页码（从 1 开始） | 1 |
| `limit` | integer | 每页数量 | 15 |
| `keyword` | string | 关键词搜索 | - |
| `sort_field` | string | 排序字段 | created_at |
| `sort_order` | string | 排序方式：asc/desc | desc |

### 1.5 通用返回数据结构

#### 列表数据
```json
{
  "total": 100,
  "page": 1,
  "limit": 15,
  "list": [
    { ... },
    { ... }
  ]
}
```

#### 详情数据
```json
{
  "id": 1,
  "name": "商品名称",
  "price": 99.00
}
```

---

## 2. adminapi - 后台管理 API

### 2.1 权限管理 (authority)

#### 2.1.1 供应商管理

##### 2.1.1.1 获取供应商列表
```http
GET /adminapi/authority/supplier/list
```

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | integer | 否 | 页码 |
| limit | integer | 否 | 每页数量 |
| keyword | string | 否 | 搜索关键词 |
| status | integer | 否 | 状态筛选 |

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 50,
    "list": [
      {
        "id": 1,
        "supplier_name": "供应商 A",
        "contact_person": "张三",
        "phone": "13800138000",
        "email": "supplier@example.com",
        "status": 1,
        "created_at": "2025-01-15 10:30:00"
      }
    ]
  }
}
```

##### 2.1.1.2 获取供应商详情
```http
GET /adminapi/authority/supplier/info/:id
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "supplier_code": "SUP001",
    "supplier_name": "供应商 A",
    "contact_person": "张三",
    "phone": "13800138000",
    "email": "supplier@example.com",
    "address": "北京市朝阳区 xx 路 xx 号",
    "description": "优质供应商介绍",
    "status": 1
  }
}
```

##### 2.1.1.3 创建供应商
```http
POST /adminapi/authority/supplier/create
Content-Type: application/json
```

**请求体**:
```json
{
  "supplier_name": "供应商 B",
  "contact_person": "李四",
  "phone": "13900139000",
  "email": "new_supplier@example.com",
  "address": "上海市浦东新区 xx 路 xx 号",
  "description": "新供应商介绍",
  "status": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "新增成功",
  "data": {
    "id": 2
  }
}
```

##### 2.1.1.4 更新供应商
```http
POST /adminapi/authority/supplier/update
Content-Type: application/json
```

**请求体**:
```json
{
  "id": 2,
  "supplier_name": "供应商 B 更新",
  "contact_person": "王五",
  "phone": "13700137000",
  "status": 0
}
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "更新成功",
  "data": true
}
```

##### 2.1.1.5 更新供应商字段
```http
POST /adminapi/authority/supplier/updateField
Content-Type: application/json
```

**请求体**:
```json
{
  "id": 2,
  "field": "status",
  "value": 1
}
```

##### 2.1.1.6 删除供应商
```http
POST /adminapi/authority/supplier/delete
Content-Type: application/json
```

**请求体**:
```json
{
  "ids": [2, 3, 4]
}
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "删除成功",
  "data": true
}
```

##### 2.1.1.7 批量操作供应商
```http
POST /adminapi/authority/supplier/batchOperation
Content-Type: application/json
```

**请求体**:
```json
{
  "ids": [1, 2, 3],
  "operation": "enable",
  "params": {}
}
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "批量操作成功",
  "data": true
}
```

#### 2.1.2 权限组管理

##### 2.1.2.1 获取权限列表
```http
GET /adminapi/authority/permission/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "permissions": [
      {
        "id": 1,
        "name": "商品管理",
        "title": "商品管理",
        "pid": 0,
        "icon": "product",
        "path": "/product",
        "sort": 1,
        "status": 1
      },
      {
        "id": 2,
        "name": "product:list",
        "title": "商品列表",
        "pid": 1,
        "icon": "",
        "path": "",
        "sort": 1,
        "status": 1
      }
    ]
  }
}
```

##### 2.1.2.2 获取权限详情
```http
GET /adminapi/authority/permission/info/:id
```

##### 2.1.2.3 创建权限
```http
POST /adminapi/authority/permission/create
Content-Type: application/json
```

**请求体**:
```json
{
  "name": "product:delete",
  "title": "删除商品",
  "pid": 2,
  "icon": "delete",
  "sort": 3,
  "status": 1
}
```

##### 2.1.2.4 更新权限
```http
POST /adminapi/authority/permission/update
Content-Type: application/json
```

**请求体**:
```json
{
  "id": 3,
  "name": "product:delete",
  "title": "删除商品",
  "status": 1
}
```

##### 2.1.2.5 删除权限
```http
POST /adminapi/authority/permission/delete
Content-Type: application/json
```

**请求体**:
```json
{
  "id": 3
}
```

#### 2.1.3 管理员管理

##### 2.1.3.1 获取管理员列表
```http
GET /adminapi/authority/adminuser/list
```

**请求参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| username | string | 用户名搜索 |
| role_id | integer | 角色 ID 筛选 |
| page | integer | 页码 |
| limit | integer | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 20,
    "list": [
      {
        "id": 1,
        "username": "admin",
        "nickname": "超级管理员",
        "avatar": "http://example.com/avatar.jpg",
        "email": "admin@tigshop.com",
        "phone": "13800138000",
        "status": 1,
        "roles": [
          {
            "id": 1,
            "name": "超级管理员"
          }
        ]
      }
    ]
  }
}
```

##### 2.1.3.2 创建管理员
```http
POST /adminapi/authority/adminuser/create
Content-Type: application/json
```

**请求体**:
```json
{
  "username": "newadmin",
  "password": "Admin123!",
  "nickname": "新管理员",
  "email": "newadmin@tigshop.com",
  "phone": "13900139000",
  "role_ids": [2, 3],
  "status": 1
}
```

##### 2.1.3.3 更新管理员
```http
POST /adminapi/authority/adminuser/update
Content-Type: application/json
```

**请求体**:
```json
{
  "id": 5,
  "nickname": "更新后的昵称",
  "email": "updated@email.com",
  "status": 0
}
```

##### 2.1.3.4 修改管理员账号
```http
POST /adminapi/authority/adminuser/changeUsername
Content-Type: application/json
```

**请求体**:
```json
{
  "id": 5,
  "username": "new_username"
}
```

##### 2.1.3.5 删除管理员
```http
POST /adminapi/authority/adminuser/delete
Content-Type: application/json
```

**请求体**:
```json
{
  "ids": [5, 6, 7]
}
```

##### 2.1.3.6 获取验证码
```http
GET /adminapi/authority/adminuser/captcha
```

##### 2.1.3.7 获取当前管理员详情
```http
GET /adminapi/authority/adminuser/current
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "超级管理员",
    "email": "admin@tigshop.com",
    "avatar": "http://example.com/avatar.jpg",
    "roles": ["超级管理员"],
    "permissions": ["system:*"]
  }
}
```

#### 2.1.4 角色管理

##### 2.1.4.1 获取角色列表
```http
GET /adminapi/authority/role/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "roles": [
      {
        "id": 1,
        "name": "超级管理员",
        "description": "拥有所有权限",
        "status": 1,
        "permissions_count": 50,
        "admin_count": 1
      }
    ]
  }
}
```

##### 2.1.4.2 创建角色
```http
POST /adminapi/authority/role/create
Content-Type: application/json
```

**请求体**:
```json
{
  "name": "商品专员",
  "description": "负责商品管理的专员",
  "permissions": [
    {
      "id": 2,
      "name": "product:list"
    },
    {
      "id": 3,
      "name": "product:add"
    },
    {
      "id": 4,
      "name": "product:edit"
    }
  ],
  "status": 1
}
```

##### 2.1.4.3 更新角色
```http
POST /adminapi/authority/role/update
Content-Type: application/json
```

**请求体**:
```json
{
  "id": 2,
  "name": "商品专员（更新）",
  "description": "更新后的描述",
  "status": 1
}
```

##### 2.1.4.4 删除角色
```http
POST /adminapi/authority/role/delete
Content-Type: application/json
```

**请求体**:
```json
{
  "ids": [2, 3]
}
```

##### 2.1.4.5 获取角色详情
```http
GET /adminapi/authority/role/info/:id
```

#### 2.1.5 管理员日志

##### 2.1.5.1 获取操作日志列表
```http
GET /adminapi/authority/log/list
```

**请求参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| user_id | integer | 用户 ID 筛选 |
| action | string | 操作类型 |
| start_time | string | 开始时间 |
| end_time | string | 结束时间 |
| page | integer | 页码 |
| limit | integer | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 100,
    "list": [
      {
        "id": 1,
        "user_id": 1,
        "username": "admin",
        "action": "UPDATE",
        "module": "商品管理",
        "content": "更新了商品：Nike 运动鞋",
        "ip": "127.0.0.1",
        "created_at": "2025-01-15 10:30:00"
      }
    ]
  }
}
```

### 2.2 商户管理 (merchant)

#### 2.2.1 商家账户表

##### 2.2.1.1 获取商家账户列表
```http
GET /adminapi/merchant/account/list
```

**请求参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| merchant_id | integer | 商户 ID |
| account_type | integer | 账户类型 |
| page | integer | 页码 |
| limit | integer | 每页数量 |

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 50,
    "list": [
      {
        "id": 1,
        "merchant_id": 10,
        "account_no": "ACC001",
        "balance": 5000.00,
        "frozen_amount": 500.00,
        "total_income": 50000.00,
        "status": 1
      }
    ]
  }
}
```

##### 2.2.1.2 审核提现
```http
POST /adminapi/merchant/account/auditWithdraw
Content-Type: application/json
```

**请求体**:
```json
{
  "withdraw_id": 123,
  "status": 1,
  "remark": "审核通过"
}
```

##### 2.2.1.3 上传打款凭证
```http
POST /adminapi/merchant/account/uploadReceipt
Content-Type: multipart/form-data
```

**表单数据**:
- `withdraw_id`: 提现 ID
- `receipt_image`: 打款凭证图片（base64 或文件）

#### 2.2.2 店铺商品分类信息

##### 2.2.2.1 获取分类列表
```http
GET /adminapi/merchant/category/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "category_name": "电子产品",
      "parent_id": 0,
      "icon": "http://example.com/icon.png",
      "sort": 1
    }
  ]
}
```

##### 2.2.2.2 创建商品分类
```http
POST /adminapi/merchant/category/create
Content-Type: application/json
```

**请求体**:
```json
{
  "category_name": "家用电器",
  "parent_id": 0,
  "icon": "http://example.com/electronic.png",
  "sort": 2
}
```

#### 2.2.3 店铺表

##### 2.2.3.1 获取店铺列表
```http
GET /adminapi/merchant/shop/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "shops": [
      {
        "id": 1,
        "shop_name": "TigShop 旗舰店",
        "shop_logo": "http://example.com/logo.png",
        "shop_desc": "官方旗舰店铺",
        "owner_id": 10,
        "merchant_id": 1,
        "status": 1
      }
    ]
  }
}
```

##### 2.2.3.2 申请入驻协议
```http
GET /adminapi/merchant/shop/agreement
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "agreement_url": "http://example.com/agreement.pdf",
    "agreement_content": "平台入驻协议内容..."
  }
}
```

#### 2.2.4 店铺信息

##### 2.2.4.1 获取店铺详情
```http
GET /adminapi/merchant/shop/detail/:id
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "shop_name": "TigShop 旗舰店",
    "shop_logo": "http://example.com/logo.png",
    "shop_desc": "官方旗舰店铺",
    "shop_phone": "400-888-2700",
    "shop_email": "shop@example.com",
    "shop_address": "北京市朝阳区 xx 路",
    "owner_name": "店主姓名",
    "owner_phone": "13800138000",
    "license_number": "营业执照号",
    "business_scope": "经营范围",
    "status": 1,
    "created_at": "2025-01-15"
  }
}
```

##### 2.2.4.2 保存供应商配置
```http
POST /adminapi/merchant/shop/saveSupplierConfig
Content-Type: application/json
```

**请求体**:
```json
{
  "shop_id": 1,
  "config": {
    "shipping_fee_template_id": 1,
    "support_methods": ["delivery", "pickup"]
  }
}
```

#### 2.2.5 商户管理

##### 2.2.5.1 获取商户列表
```http
GET /adminapi/merchant/merchant/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 30,
    "list": [
      {
        "id": 1,
        "merchant_code": "MERCH001",
        "merchant_name": "某某公司",
        "legal_person": "法人姓名",
        "phone": "13900139000",
        "email": "company@example.com",
        "status": 1,
        "shop_count": 5
      }
    ]
  }
}
```

#### 2.2.6 入驻申请

##### 2.2.6.1 获取入驻申请列表
```http
GET /adminapi/merchant/apply/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 10,
    "list": [
      {
        "id": 1,
        "applicant_name": "申请人姓名",
        "company_name": "公司名称",
        "phone": "13800138000",
        "email": "apply@example.com",
        "apply_date": "2025-01-15",
        "status": 0
      }
    ]
  }
}
```

##### 2.2.6.2 审核入驻申请
```http
POST /adminapi/merchant/apply/audit
Content-Type: application/json
```

**请求体**:
```json
{
  "apply_id": 1,
  "status": 1,
  "remark": "资料齐全，审核通过"
}
```

#### 2.2.7 店铺员工管理

##### 2.2.7.1 获取员工列表
```http
GET /adminapi/merchant/shopemployee/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 5,
    "list": [
      {
        "id": 1,
        "shop_id": 1,
        "employee_name": "员工姓名",
        "employee_role": "客服",
        "phone": "13700137000",
        "email": "employee@example.com",
        "status": 1,
        "created_at": "2025-01-15"
      }
    ]
  }
}
```

##### 2.2.7.2 创建店铺员工
```http
POST /adminapi/merchant/shopemployee/create
Content-Type: application/json
```

**请求体**:
```json
{
  "shop_id": 1,
  "employee_name": "新员工",
  "employee_role": "运营",
  "phone": "13600136000",
  "email": "new@example.com",
  "password": "Employee123!"
}
```

##### 2.2.7.3 判断是否为店长
```http
GET /adminapi/merchant/shopemployee/isManager/:userId
```

#### 2.2.8 商户资金变化

##### 2.2.8.1 获取资金日志列表
```http
GET /adminapi/merchant/accountlog/list
```

**请求参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| merchant_id | integer | 商户 ID |
| log_type | integer | 类型：1 收入 2 支出 |
| start_time | string | 开始时间 |
| end_time | string | 结束时间 |

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "logs": [
      {
        "id": 1,
        "merchant_id": 1,
        "amount": 1000.00,
        "balance": 5000.00,
        "log_type": 1,
        "log_title": "订单收款",
        "remark": "订单号：ORD001",
        "created_at": "2025-01-15 10:30:00"
      }
    ]
  }
}
```

##### 2.2.8.2 获取统计数据
```http
GET /adminapi/merchant/accountlog/statistics
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total_balance": 5000.00,
    "this_month_income": 10000.00,
    "this_month_expense": 2000.00
  }
}
```

---

## 3. common - 公共方法

### 3.1 配置管理

#### 3.1.1 清除缓存
```http
GET /adminapi/common/cache/clear
```

### 3.2 提示管理

#### 3.2.1 获取提示信息列表
```http
GET /adminapi/common/message/list
```

### 3.3 表单令牌校验

#### 3.3.1 创建令牌
```http
GET /adminapi/common/token/create
```

---

## 4. content - 文章组

### 4.1 文章分类

#### 4.1.1 获取文章分类树
```http
GET /adminapi/content/category/tree
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "name": "新闻中心",
      "children": [
        {
          "id": 2,
          "name": "公司新闻",
          "children": []
        }
      ]
    }
  ]
}
```

#### 4.1.2 创建文章分类
```http
POST /adminapi/content/category/create
Content-Type: application/json
```

**请求体**:
```json
{
  "name": "技术分享",
  "parent_id": 0,
  "sort": 1
}
```

#### 4.1.3 获取文章列表
```http
GET /adminapi/content/article/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 50,
    "list": [
      {
        "id": 1,
        "title": "TigShop v5.0 发布",
        "summary": "TigShop 5.0 版本全新发布...",
        "cover_image": "http://example.com/image.jpg",
        "category_id": 1,
        "view_count": 1234,
        "status": 1,
        "created_at": "2025-01-15 10:30:00"
      }
    ]
  }
}
```

### 4.2 视频号账号

#### 4.2.1 新增视频号账号
```http
POST /adminapi/content/wechatvideoaccount/create
Content-Type: application/json
```

**请求体**:
```json
{
  "account_name": "TigShop 视频号",
  "account_id": "wxid_xxx",
  "access_token": "xxx",
  "refresh_token": "xxx"
}
```

### 4.3 微信直播

#### 4.3.1 同步到微信
```http
POST /adminapi/content/livestream/syncToWechat
```

**请求体**:
```json
{
  "livestream_id": 1,
  "sync_type": "create"
}
```

#### 4.3.2 刷新直播间信息
```http
POST /adminapi/content/livestream/refreshInfo/:id
```

#### 4.3.3 获取推流地址
```http
GET /adminapi/content/livestream/getPushUrl/:id
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "push_url": "rtmp://push.example.com/live/stream_key",
    "pull_url": "http://pull.example.com/live/stream_key.m3u8"
  }
}
```

#### 4.3.4 获取直播间推广信息
```http
GET /adminapi/content/livestream/promotion/:id
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "share_url": "https://pg.wt.qq.com/share/xxx",
    "qr_code": "http://example.com/qr.png"
  }
}
```

---

## 5. decorate - 装修组

### 5.1 装修导入导出模块

#### 5.1.1 获取装修列表
```http
GET /adminapi/decorate/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 10,
    "list": [
      {
        "id": 1,
        "name": "首页装修",
        "type": "home",
        "platform": "pc",
        "status": 1,
        "is_published": true,
        "updated_at": "2025-01-15 10:30:00"
      }
    ]
  }
}
```

#### 5.1.2 导入装修方案
```http
POST /adminapi/decorate/import
Content-Type: multipart/form-data
```

**表单数据**:
- `file`: 装修方案文件 (.json/.zip)
- `type`: 类型（home/category/product）

#### 5.1.3 分享装修方案
```http
POST /adminapi/decorate/share
Content-Type: application/json
```

**请求体**:
```json
{
  "decorate_id": 1,
  "type": "invite_code"
}
```

### 5.2 PC 导航栏

#### 5.2.1 获取导航列表
```http
GET /adminapi/decorate/pcnavigation/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "name": "首页",
      "url": "/",
      "target": "_self",
      "icon": "",
      "sort": 1,
      "parent_id": 0,
      "status": 1
    }
  ]
}
```

#### 5.2.2 选择链接地址
```http
GET /adminapi/decorate/pcnavigation/linkOptions
```

### 5.3 PC 分类抽屉

#### 5.3.1 获取分类抽屉列表
```http
GET /adminapi/decorate/pccategorydrawer/list
```

### 5.4 首页分类栏 (H5)

#### 5.4.1 获取 H5 分类栏列表
```http
GET /adminapi/decoratemodule/h5categories/list
```

### 5.5 装修管理

#### 5.5.1 创建装修方案
```http
POST /adminapi/decorate/create
Content-Type: application/json
```

**请求体**:
```json
{
  "name": "首页装修",
  "type": "home",
  "platform": "pc",
  "template_id": 1,
  "blocks": []
}
```

#### 5.5.2 保存装修草稿
```http
POST /adminapi/decorate/saveDraft
Content-Type: application/json
```

**请求体**:
```json
{
  "decorate_id": 1,
  "blocks": [...]
}
```

#### 5.5.3 发布装修
```http
POST /adminapi/decorate/publish
Content-Type: application/json
```

**请求体**:
```json
{
  "decorate_id": 1,
  "version": "1.0.0"
}
```

#### 5.5.4 加载装修草稿
```http
GET /adminapi/decorate/loadDraft/:decorateId
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "decorate_id": 1,
    "name": "首页装修",
    "blocks": [
      {
        "id": 1,
        "type": "banner",
        "props": {
          "images": [...]
        }
      }
    ]
  }
}
```

### 5.6 自定义装修

#### 5.6.1 自定义页面更新
```http
POST /adminapi/decorate/custompage/update
Content-Type: application/json
```

#### 5.6.2 获取 H5 装修列表
```http
GET /adminapi/decorate/custompage/h5list
```

### 5.7 装修模板库

#### 5.7.1 获取模板列表
```http
GET /adminapi/decoratetemplate/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "templates": [
      {
        "id": 1,
        "name": "电商模板",
        "preview": "http://example.com/preview.jpg",
        "category": "ecommerce"
      }
    ]
  }
}
```

---

## 6. promotion - 营销组

### 6.1 限时折扣

#### 6.1.1 获取限时折扣列表
```http
GET /adminapi/promotion/timediscount/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 20,
    "list": [
      {
        "id": 1,
        "name": "周末特惠",
        "product_ids": [1, 2, 3],
        "discount_rate": 0.8,
        "start_time": "2025-01-15 00:00:00",
        "end_time": "2025-01-15 23:59:59",
        "status": 1
      }
    ]
  }
}
```

#### 6.1.2 创建限时折扣活动
```http
POST /adminapi/promotion/timediscount/create
Content-Type: application/json
```

**请求体**:
```json
{
  "name": "周末特惠",
  "product_ids": [1, 2, 3],
  "discount_rate": 0.8,
  "start_time": "2025-01-15 00:00:00",
  "end_time": "2025-01-15 23:59:59",
  "status": 1
}
```

### 6.2 限时秒杀

#### 6.2.1 获取秒杀商品列表
```http
GET /adminapi/promotion/seckill/products
```

**请求参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| seckill_id | integer | 秒杀活动 ID |
| product_id | integer | 商品 ID |

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "products": [
      {
        "id": 1,
        "product_name": "Nike 运动鞋",
        "original_price": 599.00,
        "seckill_price": 299.00,
        "stock": 100,
        "sold_count": 50,
        "start_time": "2025-01-15 10:00:00",
        "end_time": "2025-01-15 12:00:00"
      }
    ]
  }
}
```

### 6.3 优惠券

#### 6.3.1 获取有效优惠券列表
```http
GET /adminapi/promotion/coupon/validlist
```

#### 6.3.2 手动发放优惠券
```http
POST /adminapi/promotion/coupon/manualIssue
Content-Type: application/json
```

**请求体**:
```json
{
  "coupon_id": 1,
  "user_ids": [1, 2, 3],
  "reason": "新年福利"
}
```

#### 6.3.3 手动发放优惠券预览
```http
POST /adminapi/promotion/coupon/manualIssuePreview
```

**请求体**:
```json
{
  "coupon_id": 1,
  "count": 100
}
```

#### 6.3.4 手动发放会员搜索
```http
POST /adminapi/promotion/coupon/memberSearch
```

**请求体**:
```json
{
  "keyword": "user",
  "page": 1,
  "limit": 10
}
```

#### 6.3.5 获取优惠券发放日志
```http
GET /adminapi/promotion/coupon/log/list
```

#### 6.3.6 获取优惠券发放批次列表
```http
GET /adminapi/promotion/coupon/batch/list
```

### 6.4 拼团活动

#### 6.4.1 获取拼团活动列表
```http
GET /adminapi/promotion/group/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 15,
    "list": [
      {
        "id": 1,
        "activity_name": "水果拼团",
        "product_id": 10,
        "group_size": 3,
        "group_price": 29.90,
        "original_price": 59.90,
        "start_time": "2025-01-15 00:00:00",
        "end_time": "2025-01-20 23:59:59",
        "status": 1,
        "group_count": 50
      }
    ]
  }
}
```

#### 6.4.2 立即结束活动
```http
POST /adminapi/promotion/group/end/:id
```

#### 6.4.3 活动推广
```http
POST /adminapi/promotion/group/promote
```

**请求体**:
```json
{
  "group_id": 1,
  "channel": "wechat"
}
```

#### 6.4.4 获取客户端列表
```http
GET /adminapi/promotion/group/client/list/:groupId
```

### 6.5 买家秀管理

#### 6.5.1 获取买家秀列表
```http
GET /adminapi/promotion/showcase/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 100,
    "list": [
      {
        "id": 1,
        "user_id": 10,
        "username": "张三",
        "product_id": 100,
        "product_name": "Nike 运动鞋",
        "images": ["http://example.com/img1.jpg"],
        "content": "质量很好，物流很快！",
        "likes": 20,
        "is_public": true,
        "status": 1,
        "created_at": "2025-01-15 10:30:00"
      }
    ]
  }
}
```

#### 6.5.2 审核买家秀
```http
POST /adminapi/promotion/showcase/audit
Content-Type: application/json
```

**请求体**:
```json
{
  "showcase_id": 1,
  "status": 1,
  "remark": "优秀买家秀，推荐展示"
}
```

#### 6.5.3 评论买家秀
```http
POST /adminapi/promotion/showcase/comment
```

**请求体**:
```json
{
  "showcase_id": 1,
  "content": "感谢分享！"
}
```

#### 6.5.4 生成虚拟用户信息
```http
POST /adminapi/promotion/showcase/generateFakeUser
```

### 6.6 礼品卡

#### 6.6.1 礼品卡列表
```http
GET /adminapi/promotion/giftcard/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 500,
    "list": [
      {
        "id": 1,
        "gift_card_code": "GC20250001",
        "value": 100.00,
        "status": 1,
        "issued_at": "2025-01-15",
        "used_at": null,
        "expired_at": "2025-12-31"
      }
    ]
  }
}
```

#### 6.6.2 礼品卡批量生成
```http
POST /adminapi/promotion/giftcard/batchGenerate
Content-Type: application/json
```

**请求体**:
```json
{
  "value": 100.00,
  "quantity": 100,
  "expired_days": 365
}
```

#### 6.6.3 礼品卡兑换详情
```http
GET /adminapi/promotion/giftcard/exchangeDetail/:exchangeId
```

### 6.7 抽奖活动

#### 6.7.1 编辑抽奖活动
```http
POST /adminapi/promotion/lottery/edit
```

#### 6.7.2 更新活动状态
```http
POST /adminapi/promotion/lottery/updateStatus
```

**请求体**:
```json
{
  "lottery_id": 1,
  "status": 1
}
```

#### 6.7.3 用户中奖详情列表
```http
GET /adminapi/promotion/lottery/winner/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "winners": [
      {
        "id": 1,
        "user_id": 10,
        "username": "张三",
        "prize_name": "iPhone 15",
        "prize_level": 1,
        "win_time": "2025-01-15 10:30:00",
        "prize_status": 1
      }
    ]
  }
}
```

---

## 7. setting - 配置组

### 7.1 配送类型

#### 7.1.1 获取配送类型列表
```http
GET /adminapi/setting/deliverytype/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "name": "快递配送",
      "code": "express",
      "enabled": true
    }
  ]
}
```

### 7.2 运费模板

#### 7.2.1 获取运费模板列表
```http
GET /adminapi/setting/freemodelist/template/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "templates": [
      {
        "id": 1,
        "name": "全国包邮",
        "description": "除偏远地区外全国包邮",
        "is_default": true
      }
    ]
  }
}
```

#### 7.2.2 设置默认运费模板
```http
POST /adminapi/setting/freemodelist/setDefaultTemplate
```

**请求体**:
```json
{
  "template_id": 1
}
```

### 7.3 地区管理

#### 7.3.1 根据 regionId 获取经纬度
```http
GET /adminapi/setting/region/coordinates/:regionId
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "lat": 39.9042,
    "lng": 116.4074,
    "name": "北京市"
  }
}
```

#### 7.3.2 获取所有地区
```http
GET /adminapi/setting/region/all
```

### 7.4 消息分类

#### 7.4.1 公众号模板生成
```http
POST /adminapi/setting/messageclass/wechattemplate
```

#### 7.4.2 小程序模板生成
```http
POST /adminapi/setting/messageclass/miniprogramtemplate
```

### 7.5 邮箱模板管理

#### 7.5.1 发送测试邮件
```http
POST /adminapi/setting/emailtemplate/sendtest
```

**请求体**:
```json
{
  "email": "test@example.com",
  "template_id": 1
}
```

### 7.6 视频相册

#### 7.6.1 上传视频
```http
POST /adminapi/setting/videoalbum/uploadVideo
Content-Type: multipart/form-data
```

**表单数据**:
- `file`: 视频文件
- `album_id`: 相册 ID
- `title`: 标题

#### 7.6.2 相册视频列表
```http
GET /adminapi/setting/videoalbum/video/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "videos": [
      {
        "id": 1,
        "album_id": 1,
        "title": "产品介绍视频",
        "cover": "http://example.com/cover.jpg",
        "duration": 120,
        "size": 52428800,
        "upload_time": "2025-01-15 10:30:00"
      }
    ]
  }
}
```

### 7.7 相册图片

#### 7.7.1 上传图片
```http
POST /adminapi/setting/albumimage/uploadImage
Content-Type: multipart/form-data
```

**表单数据**:
- `image`: 图片文件
- `album_id`: 相册 ID

#### 7.7.2 相册图片列表
```http
GET /adminapi/setting/albumimage/list
```

### 7.8 友情链接

#### 7.8.1 获取友链列表
```http
GET /adminapi/setting/friendlink/list
```

### 7.9 系统设置

#### 7.9.1 获取结算与分账配置
```http
GET /adminapi/setting/config/settlementAndDistribution
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "platform_fee_rate": 0.05,
    "settlement_period": 7,
    "auto_settlement": true
  }
}
```

#### 7.9.2 保存结算与分账配置
```http
POST /adminapi/setting/config/saveSettlementAndDistribution
```

**请求体**:
```json
{
  "platform_fee_rate": 0.05,
  "settlement_period": 7,
  "auto_settlement": true
}
```

#### 7.9.3 获取提现配置
```http
GET /adminapi/setting/config/withdrawal
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "min_withdraw_amount": 100.00,
    "max_withdraw_amount": 50000.00,
    "withdraw_fee": 1.00,
    "withdraw_day": "1-5"
  }
}
```

#### 7.9.4 保存提现配置
```http
POST /adminapi/setting/config/saveWithdrawal
```

#### 7.9.5 上传密钥文件
```http
POST /adminapi/setting/config/uploadKeyFile
Content-Type: multipart/form-data
```

**表单数据**:
- `key_file`: 私钥文件
- `cert_file`: 证书文件

#### 7.9.6 支付配置保存

**微信支付设置**:
```http
POST /adminapi/setting/config/saveWechatPayment
```

**请求体**:
```json
{
  "app_id": "wx1234567890",
  "mch_id": "1234567890",
  "api_key": "your_api_key",
  "certificate": "xxx"
}
```

**支付宝支付设置**:
```http
POST /adminapi/setting/config/saveAlipayPayment
```

**PayPal 支付设置**:
```http
POST /adminapi/setting/config/savePaypalPayment
```

**Stripe 支付设置**:
```http
POST /adminapi/setting/config/saveStripePayment
```

**云支付设置**:
```http
POST /adminapi/setting/config/saveCloudPayment
```

#### 7.9.7 主题风格配置
```http
GET /adminapi/setting/config/themeStyle
POST /adminapi/setting/config/saveThemeStyle
```

**请求体**:
```json
{
  "primary_color": "#1890ff",
  "layout": "classic"
}
```

#### 7.9.8 门店配置
```http
GET /adminapi/setting/config/shop
POST /adminapi/setting/config/saveShopConfig
```

**请求体**:
```json
{
  "enable_store": true,
  "default_delivery_method": "delivery"
}
```

### 7.10 许可证

#### 7.10.1 授权信息
```http
GET /adminapi/setting/license/info
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "license": "TIGSHOP-LICENSE-XXXXX",
    "domain": "www.tigshop.com",
    "expire_time": "2025-12-31",
    "status": "active"
  }
}
```

#### 7.10.2 保存授权
```http
POST /adminapi/setting/license/save
```

**请求体**:
```json
{
  "license": "TIGSHOP-LICENSE-XXXXX",
  "domain": "www.tigshop.com"
}
```

### 7.11 税率管理

#### 7.11.1 税率列表
```http
GET /adminapi/setting/taxrate/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "tax_rates": [
      {
        "id": 1,
        "name": "标准税率",
        "rate": 0.13,
        "description": "增值税 13%"
      }
    ]
  }
}
```

#### 7.11.2 新增税率
```http
POST /adminapi/setting/taxrate/create
```

**请求体**:
```json
{
  "name": "低税率",
  "rate": 0.06,
  "description": "增值税 6%"
}
```

### 7.12 SEO 优化

#### 7.12.1 商品链接批量更新
```http
POST /adminapi/setting/seo/batchUpdateProductLinks
```

#### 7.12.2 商品类目链接批量更新
```http
POST /adminapi/setting/seo/batchUpdateCategoryLinks
```

#### 7.12.3 中文转英文并格式化
```http
POST /adminapi/setting/seo/chineseToEnglish
```

**请求体**:
```json
{
  "text": "这是一段中文文本"
}
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "english_text": "zhengduanzhongwentben",
    "formatted": "zheng-duan-zhong-wen-ben-ben"
  }
}
```

### 7.13 物流公司配置

#### 7.13.1 绑定物流公司
```http
POST /adminapi/setting/logisticscompany/bind
```

**请求体**:
```json
{
  "express_code": "YTO",
  "express_name": "圆通速递"
}
```

#### 7.13.2 获取当前租户物流公司配置列表
```http
GET /adminapi/setting/logisticscompany/currentTenantList
```

### 7.14 APP 版本管理

#### 7.14.1 上传更新文件
```http
POST /adminapi/setting/appversion/uploadFile
Content-Type: multipart/form-data
```

**表单数据**:
- `file`: APP 安装包
- `platform`: iOS/Android
- `version`: 版本号
- `force_update`: 是否强制更新

#### 7.14.2 详情
```http
GET /adminapi/setting/appversion/detail/:id
```

---

## 8. salesman - 分销组

### 8.1 分销员列表

#### 8.1.1 获取分销员列表
```http
GET /adminapi/salesman/distributor/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 50,
    "list": [
      {
        "id": 1,
        "user_id": 10,
        "username": "张三",
        "level": 1,
        "commission_total": 5000.00,
        "commission_available": 3000.00,
        "order_count": 100,
        "customer_count": 80,
        "created_at": "2025-01-15 10:30:00",
        "status": 1
      }
    ]
  }
}
```

#### 8.1.2 创建分销员
```http
POST /adminapi/salesman/distributor/create
```

**请求体**:
```json
{
  "user_id": 10,
  "level": 1
}
```

#### 8.1.3 分销员明细
```http
GET /adminapi/salesman/distributor/detail/:id
```

#### 8.1.4 分销员排行
```http
GET /adminapi/salesman/distributor/ranking
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "rankings": [
      {
        "rank": 1,
        "user_id": 10,
        "username": "张三",
        "commission": 5000.00,
        "order_count": 100
      }
    ]
  }
}
```

#### 8.1.5 客户数量接口
```http
GET /adminapi/salesman/distributor/customerCount/:id
```

#### 8.1.6 分销员佣金详情
```http
GET /adminapi/salesman/distributor/commissionDetail/:id
```

### 8.2 分销商品

#### 8.2.1 分销商品列表
```http
GET /adminapi/salesman/distributorproduct/list
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "products": [
      {
        "id": 1,
        "product_id": 100,
        "product_name": "Nike 运动鞋",
        "commission_rate": 0.1,
        "commission_amount": 30.00,
        "sales_count": 500,
        "sales_amount": 150000.00
      }
    ]
  }
}
```

#### 8.2.2 商品成交分析
```http
GET /adminapi/salesman/distributorproduct/salesAnalysis/:productId
```

### 8.3 分销业绩订单结算

#### 8.3.1 批量操作
```http
POST /adminapi/salesman/salesrecordbatch/batchOperation
```

#### 8.3.2 获取列表
```http
GET /adminapi/salesman/salesrecordbatch/list
```

### 8.4 分销内容管理

#### 8.4.1 获取配置
```http
GET /adminapi/salesman/content/config
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "poster_template": "http://example.com/poster.png",
    "copywriting_templates": [
      "分享这款超好用的产品...",
      "强烈推荐这款产品..."
    ]
  }
}
```

### 8.5 分销模式设置

#### 8.5.1 获取分销模式配置
```http
GET /adminapi/salesman/distributionmode/config
```

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "mode": "multi_level",
    "levels": 2,
    "commission_rules": [
      {
        "level": 1,
        "rate": 0.1,
        "description": "一级分销佣金比例 10%"
      },
      {
        "level": 2,
        "rate": 0.05,
        "description": "二级分销佣金比例 5%"
      }
    ]
  }
}
```

#### 8.5.2 更新分销模式配置
```http
POST /adminapi/salesman/distributionmode/update
```

**请求体**:
```json
{
  "mode": "multi_level",
  "levels": 2,
  "commission_rules": [...]
}
```

### 8.6 素材分类

#### 8.6.1 获取素材分类列表
```http
GET /adminapi/salesman/materialcategory/list
```

---

*本文档由 TigShop 开发团队维护*  
*最后更新：2025-06-18*  
*文档版本：Part 5 - API Reference Guide*
