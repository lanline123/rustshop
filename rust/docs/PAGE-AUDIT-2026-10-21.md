# 全站页面巡检记录（2026-10-21）

> 目的：用原版 PC / Admin 前端逐页实测，抓控制台异常与接口 4xx/5xx，汇总后集中修复。
> 账号：Admin `admin/admin123`；PC `123/123`。
> 巡检脚本：`_diag/audit_lite.py`（CDP 驱动 headless Edge，逐页落盘）。

## 巡检范围与方法

| 项 | 说明 |
|---|---|
| Admin 路由 | 从线上 Vue Router 实例 `getRoutes()` 导出，共 **179** 个页面 |
| PC 路由 | 手工列 40 个关键页面（登录/分类/详情/购物车/会员中心/结算/文章等） |
| 采集项 | `Runtime.exceptionThrown`、`console.error`、`Network.loadingFailed`、接口 4xx/5xx |
| 判定 | 任一接口 4xx/5xx 或出现异常 → 记 ERR |

---

## 一、已确认的缺陷（含根因与修复）

### P0-1 首页布局错乱（用户直接反馈）

- **页面**：`https://www.yjxj.cn/`
- **现象**：首屏 Banner/分类抽屉/推荐位与下方楼层宽度不一致，整页错位。
- **根因**：`GET /api/home/home/pcHeader` 在装修数据没有 `pageModule.pcHeader` 时，
  返回了 **空对象 `{}`**；Java 原版此处返回 **`null`**。
  - 前台 `resolvePcHeaderConfigFromResult`：`null` → 判定「未配置新版头部」→ 全站 `.container`
    用旧版固定 `1190px`。
  - 返回 `{}` → 被判定「已配置新版头部」→ 全站切到新版 `1264–1600` 流式宽度，
    但首页模块仍是旧版结构 → 布局错位。
- **修复**：`rust/crates/services/src/content/home.rs` `pc_header()`，
  `unwrap_or_else(|| json!({}))` → `unwrap_or(Value::Null)`。
- **部署后验证**：`curl /api/home/home/pcHeader` 应返回 `{"pcHeader": null}`。

### P0-2 首页楼层商品全部串成同一批（用户直接反馈）

- **页面**：`https://www.yjxj.cn/`（各「分类商品-窄/宽」楼层、「热门商品/推荐」模块）
- **现象**：不同楼层商品重复、与后台装修里配置的商品不一致。
- **根因**：原版前端用 `GET /api/product/product/list?ids=322,321,320` 精确指定商品，
  Java `ProductListDTO.ids` 同时做 **IN 过滤** + `ORDER BY FIELD(product_id, ids)` 排序。
  Rust 的 `ProductListQuery` **没有 `ids` 字段**，serde 直接丢弃 → 查询退化为
  「全表按 add_time 倒序」，所有楼层拿到同一批商品。
- **修复**：
  - `rust/crates/core/src/models/catalog/product.rs`：新增 `ids: Option<String>` + 单测。
  - `rust/crates/infrastructure/src/db/repo/catalog/product.rs`：`paginate()` 增加
    `AND product_id IN (...)` 与 `ORDER BY FIELD(product_id, ...)`（仅接受数字与逗号，
    非法输入降级为无筛选，避免放宽成全表）。
- **部署后验证**：`?ids=322,321,320` 只返回这 3 个 ID，且顺序一致。

### P0-3 代客下单「已选商品」永远为空（用户直接反馈）

- **页面**：`https://www.yjxj.cn/admin/order/proxy-shopping`
- **现象**：选择会员、选好商品并「加入购物车」后，右侧「已选商品」列表始终为空。
- **根因**：加购落库时的 `cart.type` 写错。
  - Java 两步：`dto.setType(adminShopping2CartType(product))` 只是**中间类型**，
    最后 `addCarts.forEach(cart -> cart.setType(dto.getFinalCartType()))`
    统一覆盖成 `TYPE_ADMIN_SHOPPING = 15`。
  - Rust 直接把中间类型 `cart_type`（普通商品 = **1**）写库，而
    `list` / `getCount` / `clearCart` / 冲突校验**全部按 `type = 15` 过滤** →
    加购「成功」但列表永远查不到。
- **连带影响**：代客下单商品混进 C 端购物车（`/api/cart/cart/getCount` 计数变多）。
- **修复**：`rust/crates/services/src/trade/proxy_shopping_cart.rs`，
  落库/合并查找统一使用 `CART_TYPE_ADMIN_SHOPPING`（=15）。

### P0-4 代客下单「已选商品」单价/小计恒为 `-`

- **页面**：同上
- **根因**：原版页面读 `row.price` / `row.subtotal`；Java `CartVO` 同时返回
  `originalPrice`、`price`、`subtotal`。Rust `CartItemVO` 只有 `originalPrice`。
- **修复**：`rust/crates/core/src/models/trade/proxy_shopping_cart.rs`
  增加 `price` / `subtotal`；`CartRow::to_vo()` 里 `subtotal = price * quantity`。

### P1-5 `/member/return/list` 已登录仍 401

- **页面**：`https://www.yjxj.cn/member/return/list`
- **现象**：`GET /api/user/aftersales/list` 返回 401「请先登录」，售后列表空白。
- **根因**：`aftersales_client` 模块的 10 个 handler 用
  `Option<Extension<AuthUser>>` 读登录态，但 `routes()` **没有挂 `optional_auth`
  中间件** → `AuthUser` 永远不进 extensions → 已登录也被判未登录。
- **旁证**：全仓 42 个 optional 路由里，只有本模块漏挂。
- **修复**：`rust/crates/api/public/src/routes/trade/aftersales_client.rs`
  补 `optional_auth` 中间件（与 `content/seo.rs` 同款，非法/空 token 当游客）。

### P1-6 客服人员页 400「无法获取当前店铺信息」

- **页面**：`https://www.yjxj.cn/admin/customer-service/agents/list`
- **现象**：`GET /adminapi/im/adminUserKefu/list` 返回 400，列表空。
- **根因**：Java `HeaderUtils.getShopId()` 对平台账号（`adminType=admin`）
  明确返回 **`0`**；Rust `current_shop_id()` 把「无 shop_id」当成参数校验失败返回 400。
- **修复**：`rust/crates/api/admin/src/routes/im/admin_user_kefu.rs`，
  平台账号映射为 `shop_id = 0`（与 Java 一致），不再 400。

### P1-7 服务费率页 422

- **页面**：`https://www.yjxj.cn/admin/finance/category-service-fee-rule`
- **现象**：`POST /adminapi/finance/categoryFeeRule/list` 返回 422，
  `status: invalid type: string "", expected i32`。
- **根因**：前端 `defaultParams` 把 `status` 初始化为空字符串 `""` 并原样提交；
  Jackson 会把 `""` 当 `null`，serde 严格解析直接拒绝整个请求。
- **修复**：`rust/crates/core/src/models/payment/category_fee_rule.rs`，
  `targetType` / `status` / `page` / `size` 改宽松解析（空串与非数字串 → `None`）。

### P1-8 客户成交页 400「salesmanId不能为空」

- **页面**：`https://www.yjxj.cn/admin/salesman/customer-transactions/list`
- **现象**：`GET /adminapi/salesman/customerTransaction/list` → 400。
- **根因**：Java `CustomerTransactionPageQuery.salesmanId` **没有 `@NotNull`**，
  原版页面把该字段初始化成空字符串 `""` 提交，Jackson 绑成 `null`
  → 查询「全部成交记录」正常返回。
  Rust 侧：① serde 严格解析 `""` 直接报错；② 服务层强制 `salesman_id` 必填。
- **修复**：
  - `core/src/models/marketing/salesman_customer_transaction.rs`：宽松解析。
  - `services/src/marketing/salesman_customer_transaction.rs`：取消必填，
    `salesmanId` 为空即不加过滤；分销员名按结果集批量回填。
  - `infrastructure/.../salesman_customer_transaction.rs`：`paginate` 接受
    `Option<i32>`，`None` 时不拼 `salesman_id = ?`。

### P1-9 分销员分组页 500（MySQL 保留字）

- **页面**：`/admin/salesman/promoter/list`、`/admin/salesman/salesman-detailed/list`、
  `/admin/salesman/subgroup/list`
- **现象**：`GET /adminapi/salesman/group/list` → 500，
  `1064 ... near 'describe, add_time, shop_id FROM salesman_group'`。
- **根因**：`describe` 是 **MySQL 保留字**，`GROUP_COLUMNS` 与
  `SALESMAN_GROUP_COLUMNS` 里裸写列名。
- **修复**：`infrastructure/.../salesman_group.rs` /
  `infrastructure/.../salesman_admin.rs` 全部改为 `` `describe` ``。

### P1-10 微信直播 / 关键词回复 / 菜单 500（DATETIME 解码）

- **页面**：`/admin/live`、`/admin/live/wechat-live/list`、
  `/admin/setting/wechat/keyword-reply/list`、`/admin/setting/wechat/menu`
- **现象**：`mismatched types; Rust type String is not compatible with SQL type DATETIME`。
- **根因**：兼容 SQL 只覆盖了 `INT UNSIGNED` / `JSON` / `DATE`，
  **漏了 `DATETIME`**；而 Rust 实体把 `datetime` 一律映射为 `String`。
- **修复**：`rust/数据库兼容修正.sql` 新增「第四步：DATETIME → VARCHAR(19)」
  （实测受影响 14 张表 / 32 列），并在校验段新增 `remaining_datetime_columns`。
  受影响表清单：
  ```text
  category_service_fee_rule / _category / _target
  merchant_application / merchant_application_media
  product_cycle
  profit_sharing_order / profit_sharing_return_order
  sub_merchant
  wechat_keyword_rule / wechat_live / wechat_live_goods
  wechat_live_room_goods / wechat_menu
  ```

### P0-11 会员留言页 500（QueryBuilder 占位符重复）

- **页面**：`/admin/user/feedback/list`
- **现象**：`GET /adminapi/user/feedback/list` → 500，
  `1064 (42000): ... near '?'`。
- **根因**：`sqlx::QueryBuilder::push()` 里写的 `?` 是**字面量**，不会绑定参数；
  只有 `push_bind()` 才会自己追加占位符并绑定值。
  代码写成了 `qb.push(format!(" AND type IN (?)", placeholders(n)))`
  再循环 `qb.push_bind(..)`，最终 SQL 变成 `AND type IN (?) ?` / `IN (?, ?) ?`
  → MySQL 收到未绑定的多余 `?` → 1064。
  公开端没报错是因为它走 `shop` 分支：`types` 已按 `SHOP_FEEDBACK_TYPES`
  过滤，且 `force_empty` 分支不拼占位符。
- **修复**：`infrastructure/src/db/repo/member/feedback.rs`
  - `type IN (...)` → `qb.push(" AND type IN (")` + `qb.separated(", ").push_bind(..)` + `")"`
  - `NOT IN (...)`（平台可见性条件）同样改为 `separated`
  - 新增两个单测锁定占位符数量（`always_filters_top_level_feedback` /
    `type_in_placeholders_match_binds`），并要求不得出现 `) ?`。

---

## 二、噪声（已确认非缺陷，不予处理）

| 页面 / 接口 | 原因 |
|---|---|
| `/admin/order/deliverlist` → `o2o/localDelivery/getConfig`、`o2o/homeService/getConfig` | 前端调了 O2O 专有配置接口，**原版 Java 后端也不存在该控制器**（仓库是 B2B2C 版，无 O2O 模块）；前端已 try/catch，仅控制台告警 |
| `/admin/setting/base-receipt` → `pos/device/list` | 同上：POS 设备接口属 O2O 收银台，B2B2C 无此控制器 |
| `/admin/print/waybill` → `order/orderPrintBill` 400 | 缺必填 `id`，直接打开打印页无订单上下文属正常 |
| `/admin/finance/shop-account/list` → `merchant/shopAccount/index` | 实际返回 404 业务码「店铺不存在」（平台账号无 shopId），非端点缺失 |
| `/admin/im`、`/admin/im/index`「收到非JSON格式数据」 | IM 走 WebSocket，首帧非 JSON 属正常握手 |
| `Tracking Prevention blocked access to storage` | Edge 隐私策略拦截 OSS 图片页 |
| `Mixed Content ... oss.tigshop.com` | 原版装修数据里的历史 http 图片（数据问题） |
| `ElementPlusError: [el-radio] label act as value` | ElementPlus 3.0 弃用告警，原版前端未升级 |
| `AsyncData request cancelled by deduplication` | Nuxt 并发去重的正常中断 |
| `ERR_INSUFFICIENT_RESOURCES` | 巡检机内存不足，非站点问题 |
| `favicon` / `ResizeObserver` | 浏览器内置噪声 |

## 三、巡检覆盖（完成）

| 范围 | 页数 | 有问题页面 | 结论 |
|---|---|---|---|
| Admin | **179 / 179** | 18 | 10 类根因，已全部修复或判定为噪声 |
| PC | 40 / 40 | 1 | 仅 P1-5（已修复） |

> 判定口径：接口 4xx/5xx 或前端异常。18 个 ERR 页面里，
> 10 个对应真实缺陷（P0-1..P0-4、P1-5..P1-11），
> 其余 8 个归入噪声（见上表）。

## 四、本轮修复清单（代码）

| 文件 | 修复 |
|---|---|
| `services/src/content/home.rs` | `pcHeader` 缺配置时返回 `null` 而非 `{}` |
| `core/src/models/catalog/product.rs` | `ProductListQuery` 新增 `ids` + 单测 |
| `infrastructure/db/repo/catalog/product.rs` | `ids` → `IN` 过滤 + `ORDER BY FIELD` |
| `services/src/trade/proxy_shopping_cart.rs` | 落库 `type` 固定为 15 |
| `core/src/models/trade/proxy_shopping_cart.rs` | `CartItemVO` 新增 `price` / `subtotal` |
| `infrastructure/db/repo/trade/proxy_shopping_cart.rs` | `to_vo` 计算 `subtotal` |
| `api/public/src/routes/trade/aftersales_client.rs` | 补 `optional_auth` 中间件 |
| `api/admin/src/routes/im/admin_user_kefu.rs` | 平台账号 shopId → 0 |
| `core/src/models/payment/category_fee_rule.rs` | 空串宽松解析 |
| `core/src/models/marketing/salesman_customer_transaction.rs` | `salesmanId` 可选 + 宽松解析 |
| `services/src/marketing/salesman_customer_transaction.rs` | 取消必填、批量回填分销员名 |
| `infrastructure/.../salesman_customer_transaction.rs` | `paginate` 接受 `Option<i32>` |
| `infrastructure/.../salesman_group.rs` | `` `describe` `` 加反引号 |
| `infrastructure/.../salesman_admin.rs` | `` `describe` `` 加反引号 |
| `infrastructure/.../member/feedback.rs` | QueryBuilder 占位符改为 `separated` + 单测 |
| `rust/数据库兼容修正.sql` | 新增 DATETIME → VARCHAR(19) |

## 五、复现命令

```bash
# Admin 巡检（分批，避免本机内存不足）
python _diag/audit_lite.py admin 0 45
python _diag/audit_lite.py admin 45 100
python _diag/audit_lite.py admin 100 112 3.5
python _diag/audit_lite.py admin 112 132 3.5
python _diag/audit_lite.py admin 132 155 3.5
python _diag/audit_lite.py admin 155 179 3.5
python _diag/fill_missing.py          # 补齐剩余页面

# PC 巡检
python _diag/audit_lite.py pc 0 40

# 导出线上真实 admin 路由（179 条）
python _diag/dump_admin_routes.py

# 静态检查
python _diag/check_optional_auth2.py   # optional_auth 覆盖
python _diag/scan_empty_string.py      # 前端空串 vs 后端严格数值
python _diag/scan_reserved_words.py    # 未加反引号的 MySQL 保留字
python _diag/scan_qb_placeholder.py    # QueryBuilder 占位符重复
python _diag/list_datetime_cols.py     # 含 datetime 列的表
```
