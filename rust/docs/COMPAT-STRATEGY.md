# 原版前端 + Rust 后端兼容策略

> 当前部署、待构建修复和回滚步骤见 [`HANDOFF-2026-10-21.md`](HANDOFF-2026-10-21.md)。

> 决策记录：2026-10-21
> 适用范围：TigShop 原版 PC / Admin 前端继续对接 Rust 后端

## 一、总原则

**保留原版前端，不切换到 `rust/web` 重构版。**

兼容工作只做两条线：

```text
1. 数据库兼容 SQL
2. Rust 后端的请求/响应契约兼容
```

不要采用以下方案：

- 不修改原版前端页面
- 不部署 `rust/web`
- 不把兼容逻辑塞进数据库视图/触发器/存储过程
- 不用只改数据库的方式强行替代 HTTP/JSON 层兼容

## 二、数据库兼容

换库、重新导入 Java `pure.sql` 后，先执行统一 SQL：

```bash
mysql -h 127.0.0.1 -u root -p 你的数据库名 < rust/数据库兼容修正.sql
```

文件：

```text
rust/数据库兼容修正.sql
```

处理内容：

```text
整数 UNSIGNED  → 有符号整数
JSON           → TEXT
DATE           → VARCHAR(10)
DATETIME       → VARCHAR(19)     ← 2026-10-21 新增
```

要求：

- 执行前备份
- 可重复执行
- 末尾的 `remaining_*` 校验值必须为 `0`
  （`unsigned_integer` / `json` / `date` / `datetime` 四项）
- `DECIMAL UNSIGNED` 不影响 Rust i32/i64，保持原样

数据库只负责 schema 和数据，不承担以下兼容：

- `Authorization: Bearer null`
- 字符串数字（如 `"cat":"17"`）
- HTTP 200/400/401 语义
- 响应 VO 嵌套结构
- 业务错误码

## 三、Rust 契约兼容

原版前端按 Java 行为调用接口，Rust 必须兼容这些隐式行为。

### 1. 请求参数宽松解析

原版常见行为：

```json
{"cat":"17"}
```

```http
GET ?startTime=2026-10-01&endTime=2026-10-21
```

```http
GET ?startTime=
```

Rust 侧要求：

- 数字 ID 同时接受数字和数字字符串
- 日期同时接受时间戳、`YYYY-MM-DD`、`YYYY-MM-DD HH:MM:SS`
- 空字符串参数按 `None` 处理
- 分页/page/size 等字段按原版默认值兜底

已处理或需持续检查的字段类型：

```text
cat / couponId / shopId / shopCategoryId / productStatus
startTime / endTime / startEndTime
dateType / statisticType / isExport
verifyToken / event
```

### 2. optional auth 行为

原版前端未登录时也会发送：

```http
Authorization: Bearer null
```

Rust 的 optional 路由必须：

- 合法 token → 写入用户上下文
- 无 token → 游客
- 空 token / 非法 token / 过期 token → 游客，不返回 401

真正的 `require_auth` 接口仍然必须返回 401。

### 3. HTTP 状态与业务错误

对齐 Java `ResultWrapper`：

```text
业务错误 Biz      → HTTP 200 + code/msg
参数校验错误      → HTTP 400
未登录/过期       → HTTP 401
无权限            → HTTP 403
资源不存在        → HTTP 404
内部错误          → HTTP 500
```

不要把所有业务错误统一映射为 HTTP 400。

### 4. 响应结构

原版前端按 Java VO 读取，Rust 返回结构必须逐页核对：

```text
PageResult: records / total / current / size / pages
物流公司:   logisticsId / logisticsName，不是 companyId/companyName
订单详情:   平铺 order 字段 + items，并包含 shopId
统计页:     salesData + salesStatisticsData.horizontalAxis/longitudinalAxis
会员趋势:   horizontalAxis / longitudinalAxis
```

原则：

- 前端怎么读，后端就怎么返回
- 不要只保证接口 200
- 路由覆盖 100% ≠ 响应结构 1:1

## 四、第三方配置

以下问题不是代码兼容问题，必须人工配置：

- 短信：`smsKeyId` / `smsKeySecret` / `smsSignName` / 短信模板 ID
- 微信登录：公众号 AppID / Secret / 回调域名
- 微信支付：商户号 / 证书 / API 密钥
- 支付宝：AppID / 私钥 / 公钥 / 回调
- Elasticsearch：地址、账号、索引

配置未完成时，接口应返回明确业务提示，而不是笼统的 HTTP 400。

## 五、页面回归清单

后续修订完成后，按页面验收：

### PC

```text
登录 / 注册 / 找回密码
首页
分类列表 /list/?cat=...
搜索
商品详情
购物车
结算
支付
订单列表 / 详情 / 售后
会员中心 / 地址 / 优惠券 / 收藏 / 消息
```

### Admin

```text
登录
控制台
订单 / 商品 / 会员
统计：销售统计、订单统计、商品统计、销售指标、新增会员
营销、财务、售后
设置：基础 / 配送 / 支付 / 短信
权限
```

每个页面记录：

```text
请求 URL
请求参数
HTTP 状态
响应 code
关键响应字段
前端是否报错
```

## 六、当前状态

- 原版前端：不修改
- `rust/web`：不部署
- 数据库：已有统一兼容 SQL
- Rust：已补请求/响应/认证/错误码兼容
- 最新契约兼容提交：`b26f0dfbdf039bd79a2c95709123db7551f4e06c`
- 服务器最新二进制：已部署 Release `build-25`（sha256 `64c591ba...`）
- 构建方式：GitHub Actions；后续仍由人工手动触发
- 尚未完成：短信通道、微信支付、微信登录、支付宝、ES 的真实业务配置验收

## 七、后续修订顺序

```text
1. 备份数据库
2. 导入 rust/数据库兼容修正.sql
3. 手动构建最新 Rust 后端
4. 部署并重启 tigshop
5. 按页面回归清单逐页验证
6. 只修 Rust 契约问题，不改原版前端
7. 第三方配置单独验收
```
