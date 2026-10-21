# 多租户隔离（平台 / 店铺 / 门店 / 供应商）

> 本文是多租户实现的权威说明。覆盖 JWT 身份、认证中间件、统一 scope 辅助、各业务域落点、N/A 判定与剩余项。
> 最近更新：2026-10-20，对应 HEAD `0215353f` 之后的集成提交。

## 1. 租户模型

| 账号类型 | `admin_type` | 身份字段 | 数据范围 |
|---|---|---|---|
| 平台管理员 | `admin`（或 `AdminUser` 无店铺） | `shop_id=None` | 全平台 |
| 店铺管理员 | `shop` / `store` / `pos` / `merchant` | `shop_id>0` | 仅本店 |
| 供应商/供货商 | `vendor` / `suppliers` | `vendor_id>0` | 仅本供应商 |
| 前台会员 | `member` | 会员 id | 仅本人数据（C 端按 user_id） |

**可信边界**：JWT 声明是唯一可信来源。认证中间件会用声明值覆盖请求头
`x-shop-id` / `x-admin-type` / `x-vendor-id`，客户端伪造这些头不再生效；
仅当旧令牌既无 `typ` 也无 `shop_id` 时，才允许请求头作为兼容回退。

## 2. 核心类型

| 类型 | 位置 | 用途 |
|---|---|---|
| `Claims { shop_id, vendor_id }` | `security/src/jwt.rs` | 登录/刷新签发租户声明 |
| `AuthUser` | `security/src/web.rs` | Handler 提取身份；提供 `is_platform/scoped_shop_id/is_vendor` |
| `AuthUser::resolve_shop_scope` | 同上 | 列表：店铺强制本店，跨店 query 403；平台可筛选 |
| `AuthUser::require_shop_scope` | 同上 | 写操作：店铺固定本店；平台必须显式 shopId |
| `AuthUser::ensure_shop_access` | 同上 | 行级归属校验 |
| `TenantContext { admin_type, shop_id, vendor_id }` | 同上 | 中间件写入请求扩展；支持 `shop_scope/vendor_scope/require_shop/can_access_*` |
| `TenantScope`（marketing/content/im） | `infrastructure/db/repo/marketing/tenant.rs` | task-local 请求作用域；`current_list_filter/current_ensure_access/current_write_shop_id` |
| `support::tenant::{platform_only, require_platform}` | `api/admin/src/support/tenant.rs` | 平台专属路由中间件/守卫 |

## 3. 强制约定

1. **列表**：店铺账号必须强制 `shop_id=本店`；平台账号不过滤（可传 shopId 筛选）；供应商按 `vendor_id`。
2. **详情 / 更新 / 删除 / 启停 / 审核**：先取行，再 `ensure_shop_access(row.shop_id)` / `ensure_access`；跨租户一律 403。
3. **创建**：店铺账号强制写本店 `shop_id`；平台账号可显式指定，或落 0（平台级数据）。
4. **平台专属**：系统配置、财务配置、平台菜单等使用 `platform_only` 中间件或 `require_platform`。
5. **无 `shop_id` 列的根表**：通过父级/关联表归属过滤，或明确判定平台级 N/A 并在文件头写明依据。
6. **请求头零信任**：新代码不得直接解析 `x-shop-id/x-admin-type/x-vendor-id` 作为身份；必须用 `Extension<AuthUser>` / `Extension<TenantContext>` / `TenantScope`。中间件覆写只作为旧代码的兼容层。

## 4. 域覆盖矩阵

| 域 | 状态 | 说明 |
|---|---|---|
| identity | ✅ | admin_user 列表/详情/增改删批量按店铺；authority 变更平台专属；admin_role 按角色作用域；admin_log 按作用域过滤 |
| catalog | ✅ | 商品/品牌/属性/库存/出入库/价格咨询/评论/分组/电子卡组/搜索/库存日志；product_manage_client 6 端点仍需补 |
| logistics | ✅ | 运费模板/运费类型/物流公司配置/打印/打印配置/打印日志；平台级 logistics_company 保持全局 |
| trade | ✅ | 订单管理全写路径、售后、代客购物车/商品/配送/下单、订单日志、店铺订单配置、自提点列表 |
| merchant | ✅ | 店铺/店铺账号/商户账号/保证金系列/提现/店铺分类/入驻/资金；vendor 走 `TenantContext.vendor_scope` |
| marketing | ✅ | 优惠券/秒杀/拼团/满减/满赠/限时折扣/礼品卡/买家秀/分销/抽奖/营销概览；根表无 shop_id 的按 N/A 或父表映射 |
| content | ✅ | 图库/视频/装修/微信素材；平台素材 `shop_id=0` 全账号可读、仅平台可写，店铺素材仅本店 |
| im | ✅ | 客服配置/客服用户/IM 配置/客服/会话/消息：身份可信化 + 现有 shop 参数链路 |
| member | ✅ | 平台会员/店铺会员/标签/等级/反馈/消息日志；无 shop 列域平台专属 |
| payment | ✅ | 分账/对账/提现/退款/进件/账期/发票/余额；支付配置与财务配置平台专属 |
| system | ✅ | 站内信按 scope；系统配置/邮件模板/消息类型/提示/APP 版本/缓存/授权平台专属；地区写平台、读开放 |
| analytics | ✅ | 经营看板/统计/销售统计按店铺；vendorIndex/vendorStatisticalData 按供应商；平台全局口径保留 |

## 5. 明确 N/A（表无 `shop_id` 且 Java 无店铺语义）

已核对 `java/database/pure.sql` 建表与原 Java 服务：
`buyer_show`、`buyer_show_comment`、`points_exchange`、`recharge_setting`、
`sign_in_setting`、`verification`、`csrf`、`example`、`e_card`、`category*`、
`logistics_company`、`category_service_fee_rule`、`cache_manage`、`licensed`。
这些域保持平台级；若未来要店铺隔离，需先加列并回填，再按第 3 节约定改造。

## 6. 验证

```pwsh
cd rust/crates
cargo test -p tigshop-services -p tigshop-infrastructure -p tigshop-admin-api --lib -j 2
cargo check --workspace
```

门禁（在 `rust/` 下）：

```pwsh
python tools/check_api_coverage.py
python tools/check_route_conflicts.py
python tools/check_sql_columns.py --strict
python tools/check_structure.py --strict
```

## 7. 新增接口检查清单

- [ ] Handler 使用 `Extension<AuthUser>` 或 `Extension<TenantContext>`，不读原始租户请求头。
- [ ] 列表把 `auth.scoped_shop_id()` / `TenantScope::current_list_filter()` 传入 repo 查询。
- [ ] 详情/写操作先取行并 `ensure_shop_access` / `current_ensure_access`。
- [ ] create 用 `require_shop_scope` / `write_shop_id` 强制落店。
- [ ] 平台专属端点挂 `platform_only`。
- [ ] 增加“越权 shopId → 403 / 列表强制过滤 / 平台放行”单测。
- [ ] 表无 shop_id 时写明父表映射或 N/A 依据。

## 8. 已知剩余（非阻塞）

1. `catalog/product_manage_client` 6 端点仍以 query/body shopId 为主，待补归属校验。
2. `groupon_promotion.post` 与 public 客户端端点属 C 端/只读推广，未纳入管理端租户范围。
3. `admin_msg.msgCount` 是 SSE 桩（返回 1001），未做 scope；`homeSummary` 已按 scope。
4. 部分 vendor 结算明细表未迁移，保持 0 占位（与 Java 相同）。
5. 平台账号列表语义统一为“不过滤”；若产品要求“平台只看 shop_id=0”需另行调整。
6. 本机 MySQL/Docker 未启动，多租户行为通过单测/静态校验验证，尚无真实双店铺 E2E。
