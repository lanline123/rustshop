# battery.recy.cc 全站问题清单（2026-10-21）

> 环境：新服务器 `115.159.46.245`，站点 `https://battery.recy.cc`
> 运行：Rust Release `build-27`，数据库 `tigshopmall`（已执行兼容 SQL）
> 只记录问题，修复前不逐项改代码。

## Admin

### A1. `/admin/setting/base-notice` 发送测试邮件 500

- 操作：进入“基础设置 → 通知设置”，点击“发送测试邮件”
- 返回：`500`
- 错误：`内部错误: 邮件发送失败: permanent error (535): Error: authentication failed`
- 判断：SMTP 认证失败。需要核对 `smtpHost` / `smtpPort` / `smtpUser` / `smtpPassword` / SSL 配置；不是前端问题。

### A2. `/admin/setting/base-notice` 短信通知设置提交 500

- 操作：进入“基础设置 → 通知设置 → 短信通知设置”，修改后点击“提交”
- 返回：`500`
- 错误：
  ```text
  数据库错误: error returned from database: 1364 (HY000):
  Field 'create_by_id' doesn't have a default value
  ```
- 影响：所有通过后台保存到 `config` 表的设置都可能报同类错误（不只是短信通知）。
- 处理：已给 `tigshopmall` 全库审计字段补默认值（`fix_audit_defaults.sql`）；`saveNotify` 实测已恢复 200 + code 0。
- 代码侧仍需修：`config_basic` 的 `replace_in_tx` 仍建议显式写审计字段，避免换库重现。
- 代码已改：`replace_in_tx` INSERT 现在显式写 `create_by_id=0`、`create_by_name='system'`、`update_by_id=0`、`update_by_name='system'`（待 GitHub 编译）。

### A3. `/admin/product/list`

- 巡检结果：页面正常，无 4xx/5xx、无控制台异常。

### A4. `/admin/live/videoNumber/list`

- 巡检结果：页面正常，无 4xx/5xx、无控制台异常。

### A5. `/admin/finance/account-panel/list`

- 巡检结果：页面正常，无 4xx/5xx、无控制台异常。

### A6. `/admin/customer-service/history/list`

- 巡检结果：页面正常，无 4xx/5xx、无控制台异常。

### A7. 后台菜单链接异常

- 实际 DOM 中一级菜单链接是：
  ```text
  //product
  //order
  //promotion
  //setting
  ```
- 不是正常的 `/admin/product`。
- 影响：普通 `<a>` 点击可能被浏览器当成协议相对地址；子菜单也不能靠普通链接抓取。
- 待确认：前端路由 base 问题，还是 Rust 返回的菜单路径问题。

### A8. `/admin/product/inventory-stock-warning/list` 500

- 接口：`GET /adminapi/product/stockWarning/list?page=1&size=15`
- 返回：`500`
- 待查：库存预警仓储 SQL / 字段解码。
- 已定位：`stock_warning` 分页 SQL 的 `ORDER BY t.product_id / t.sku_id` 使用了派生表别名，MySQL 5.7 报 `Unknown column 't.product_id' in 'order clause'`。
- 代码已改：排序改为不带 `t.` 的列名，并同步单测期望（待 GitHub 编译）。

### A9. `/admin/order/deliverlist` 两个 404

- 接口：
  ```text
  GET /adminapi/o2o/localDelivery/getConfig
  GET /adminapi/o2o/homeService/getConfig
  ```
- 返回：`404`
- 前端提示：`加载同城配送配置失败`
- 判断：O2O 配送配置接口未实现或路由缺失。

### A10. `/admin/decorate-info/pc-decorate/list` 添加页面 422

- 操作：添加页面，填写名称/排序后提交
- 返回：`422`
- 已定位：前端提交 `sortOrder: "1"` 字符串，Rust `Option<i32>` 严格解析失败：
  ```text
  sortOrder: invalid type: string "1", expected i32
  ```
- 处理：`DecorateCreateDTO` / `DecorateUpdateDTO` / `DecorateSaveDTO` 数字字段增加宽松解析（代码已改，待 GitHub 编译）。

### A11. Admin 列表页巡检结果

已巡检 89 个后台页面，除 A8、A9 外均无 4xx/5xx/控制台异常（忽略浏览器 ORB/Tracking Prevention 噪声）。

## PC

### P1. `/item/SN000002` 商品详情 400

- 接口：`GET /api/product/product/detail?sn=SN000002`
- 返回：`{"code":400,"msg":"商品不存在"}`
- 判断：新库 `tigshopmall` 中没有 `SN000002` 这个商品，属于数据问题，不是 Rust 路由/解析错误。
- 需要换一个当前库存在的商品编号复测。

### P2. `/coupon` 优惠券详情 400/401

- 接口：`GET /api/user/coupon/detail`
- 未带 token 时返回 `401 缺少 Authorization 头`。
- 判断：该页面可能要求登录；需要用有效会员 token 复测。若登录后仍 400，再查 DTO/参数。

### P3. `/join/plan` Mixed Content

- 页面通过 HTTPS 加载，但配置里有 HTTP 图片：
  ```text
  http://oss.tigshop.com/...
  ```
- 判断：配置数据/存储 URL 不是 HTTPS，属于配置问题。

### P4. 其它出现的浏览器噪声

- `ERR_BLOCKED_BY_ORB`
- `Tracking Prevention blocked access to storage`
- `AbortError: AsyncData request cancelled by unmount/deduplication`
- `net::ERR_ABORTED`

以上不是业务接口失败，先不作为 Rust bug 处理。

## 待继续巡检

- PC 交互流程：登录、加购、结算、支付、售后申请
- Admin 交互流程：商品新增/编辑、订单发货、装修保存/发布、设置提交

