# TigShop Java → Rust 结构重构报告（v2）

> 日期：2026-10-17　基线：`df28f2ca`（迁移前）→ 本文对应提交（迁移后）
> 结论：**旧「编号切片」结构已整体推翻，替换为 14 个限界上下文的领域分层结构；接口覆盖、路由、SQL、单测全部与迁移前等价或更好。**

---

## 1. 一句话结论

旧实现把 220 个 API 切片机械拆成 `sliceNNN.rs` × 4 层（529 个文件、4,738 处编号引用），
产生 115 字段上帝状态、591 行手工装配、59 个「可编译但 0 路由」的空壳。
本次重构**全部废弃编号切片**，按业务域重组为 14 个限界上下文 × 94 个 feature，
状态收敛为 13/11 个域字段，并新增可机器判定的结构门禁。
**行为保持不变：635/1564 接口、645 条路由、1041 条 SQL 与迁移前逐项相同。**

---

## 2. 迁移前的问题（实测）

| 问题 | 证据 |
|------|------|
| 编号命名无业务语义 | 529 个 `sliceNNN` 文件；4,738 处 `sliceNNN` 引用 |
| 横向割裂 | 一个功能跨 4 个 crate（models/repo/service/routes）且同编号 |
| 上帝对象 | `AdminState` 115 字段、`PublicState` 56 字段、`Repositories` 148 字段、`main.rs` 591 行 |
| 空壳注册 | 59 个切片「已注册、可编译、0 路由」，接口 100% 不可达（236 个文件） |
| 双轨实现 | 早期 20 个域文件与切片并存，同一控制器被拆到两个 feature |
| 门禁失真 | 旧工具按「文件存在/含 async fn」判进度，与真实路由可达性漂移 |

---

## 3. 目标结构与落地

```
crates/
├── core/src/models/<ctx>/<feature>.rs                  # DTO / Query / VO
├── infrastructure/src/db/repo/<ctx>/<feature>.rs       # SQLx 仓储（按域分组 Repositories）
├── services/src/<ctx>/<feature>.rs                     # 用例服务（域内 Services 聚合）
├── api/{admin,public}/src/{routes,root}/<ctx>/<feature>.rs  # 薄路由层
├── security/ · common/ · api/openapi/                  # 横切能力
└── bin/src/main.rs                                     # 唯一组合根（91 服务构造 + 14 域聚合）
```

14 个限界上下文：`identity / merchant / wholesale / catalog / trade / payment /
marketing / member / logistics / content / system / im / analytics / openapi`。
Feature 以业务资源命名（`product_manage`、`order_admin`、`user_company`、`config_basic`…），
禁止数字编号。

---

## 4. 执行过程（可复现）

| 步骤 | 工具 | 结果 |
|------|------|------|
| 1. 分类与映射 | `tools/restructure/_draft_map.py` | 220 切片 → 73 个真实实现 + 59 个空壳 + 88 未开工；0 ctx/feature 冲突 |
| 2. 结构性搬迁 | `tools/restructure/migrate.py` | 368 次移动 + 236 次删除；模块树/类型名/路径/状态字段全部重写；聚合器重生成 |
| 3. 清理零路由壳 | `tools/restructure/cleanup_routes.py` | 删除 5 个不贡献路由的路由文件（含 2 个被 `content::decorate` 接管的 PcCatFloor/PcNavigation） |
| 4. 域分组（Stage-2） | `tools/restructure/group_state.py` | 91 个服务 → 14 个 `Services` 聚合；State 从 74/36 字段降到 13/11 |
| 5. 权威映射回写 | `tools/restructure/sync_domain_map.py` | `docs/domain-map.json`：`slices[]` + 19 个 `baseline[]` + 251 个 `controllers[]` |
| 6. 结构门禁 | `tools/check_structure.py`（新增） | `--strict` exit 0：0 切片命名 / 0 布局越界 / 0 空壳 / 0 依赖越界 / 0 上帝状态 |

---

## 5. 迁移前后对比

| 指标 | 迁移前 | 结构重构后 | 迁移推进后（wave1-3） |
|------|--------|-----------|----------------------|
| `sliceNNN` 文件 | 529 | **0** | **0** |
| `sliceNNN` 引用 | 4,738 | **0** | **0** |
| 业务模块 | 编号切片 × 4 层 | **14 ctx × 94 feature** | **14 ctx × 140+ feature** |
| 空壳路由模块 | 59 切片 / 236 文件 | **0** | **0** |
| `AdminState` / `PublicState` | 115 / 56 字段 | **13 / 11 字段** | **14 / 14 字段** |
| `Repositories` | 148 字段平铺 / 540 行 | 90 字段平铺 | **14 个域聚合** |
| `services/lib.rs` | 165 行（含死代码 AppState） | **15 行** | 15 行 |
| `bin/main.rs` | 591 行 | **435 行** | ~460 行（140+ 服务显式装配） |
| 接口覆盖率 | 635 / 1564（40.6%） | 635 / 1564（不变） | **1564 / 1564（100%）** |
| 已注册路由 | 645 | 645 | **1564** |
| 路由冲突 | 0 | 0 | **0** |
| SQL 语句 / 未知表列 | 1041 / 0 | 1041 / 0 | **2300+ / 0** |
| 单测 | 基线 5 个失败 | 全绿（2505） | **全绿（4316 passed / 0 failed）** |
| 结构门禁 `--strict` | 不存在 | exit 0 | **exit 0** |
| 文件行尾 | 32 个 CRLF 混用 | 被重写文件统一 LF | 保持 LF |

### 5.1 已迁移批次（工作单位 = feature）

| 批次 | 内容 | 接口 |
|------|------|------|
| Pilot | `system/csrf`（引入管理端匿名路由能力 `URL_WHITELIST`） | 2 |
| Wave 1 | catalog/comment(13)、identity/admin_role(7)、logistics/print(8)+print_log(1)、catalog/price_inquiry(5)+product_group(8) | 42 |
| Wave 2 | system/config_basic(102)、identity/admin_account(7)+system/tips_manage(1)+system/log(1)、catalog/product_batch(2)+search_admin(5)+product_inventory_log(1)+product_search(1)、catalog/e_card_group(8)+e_card(7)、catalog/services(7)+exchange(3) | 189 |
| Wave 3 | trade/order_admin(30)、member/login(16)、im/im_conversation(23)、wholesale 域 4 个 feature(27) | 96 |
| Wave 4 | content/home(16)+wechat_live(14)、merchant/merchant_apply(8)+deposit_account(8)、payment/merchant_application(10)+category_fee_rule(7)+order_invoice(7)、member/shop_member(9) | 96 |
| Wave 4b | merchant/deposit_recharge(10)+deposit_refund(7) | 17 |
| Wave 5 | groupon_promotion(12)+coupon_manage(9)+salesman_content(10)、refund_log(7)+with_draw_apply_client(7)+credit_settlement(6)+credit_user(6)+user_invoice(7)、gallery(9)+gallery_pic(10) | 83 |
| Wave 6+7 | merchant 8 features、trade proxy_shopping_cart/check/user_order、payment sub_merchant/profit_sharing/user_recharge_order/user_withdraw_apply/pay、member 4 features、marketing gift_card(+groups)、system licensed/util、im kefu 2 features | 139 |
| Wave 8 | catalog/product_manage(39)、marketing 7 features、content 3 features、trade 2、payment 3、analytics panel、system admin_msg | 271 |
| Wave 9 | payment recharge_order/statement/apple_pay、marketing 12 features、content 11 features、trade 6、member 4、merchant 4、system 2、analytics sales_statistics | 147 |
| Wave 10 | payment 渠道配置 4 + refund_apply_admin/account_panel/credit_order、marketing groupon_records/groupon_promotion_record/overview/promotion/seckill_batch/product_style_admin/lottery、member user_price_inquiry/member_audience/user_feedback_client/user_sign | 48 |

> 迁移推进期还修复了两个结构级缺陷：① Stage-1 生成的 **root 路由聚合器为空**（`/im/**`、`/img/**`、`/s/**` 实际未挂载）；② `register_feature.py` 只支持旧的扁平 `Repositories`，已升级为按上下文聚合，并新增 `sync_routes.py`（路由聚合器从磁盘文件派生）与 `group_repositories.py`（仓储分域聚合）。


---

## 6. 顺带修复的真实缺陷

1. **5 个历史遗留测试失败**（均非本次结构改动引入，迁移前基线即失败）：
   - `UserTrendsQuery` 对前端字符串参数 `dateType`/`isExport` 严格反序列化导致整请求 400 → 改为宽松解析（与 Java 容错一致）；
   - `parse_grow_setting("[1,2]")` 被 serde 按「具名结构体接受数组」解析成 `Some`，与 Java `parseObject` 只接受对象不符 → 增加对象校验；
   - `user_rank` / `feedback` 仓储两处测试断言与常量/Java 语义不符（`FROM user_rank ` 尾随空格、`order_id` 误判、platform 空 `shop_id` 口径）→ 按 Java 源码校正测试口径。
2. **空壳注册风险消除**：59 个「可编译不可达」切片及其注册全部删除，避免覆盖率虚报。
3. **SQL 门禁空转修复**：`check_sql_columns.py` 的 `glob("*.rs")` 改为 `rglob`，并加「扫描文件数 < 80 直接失败」下限；迁移后仍实测 105 文件 / 1041 条 SQL。
4. **结构门禁新增**：`check_structure.py` 可判定切片命名、模块布局、空壳路由、依赖方向、上帝状态、切片残留，`--strict` 作为 CI 门禁。

---

## 7. 已知遗留（按优先级，已写入 ARCHITECTURE.md §10）

1. **多控制器聚合 feature 拆分**：`catalog/product_manage`（43 接口，4 控制器）、
   `openapi/workbuddy`（28 接口）、`analytics/statistics`（2 控制器）需按
   `domain-map.json#split[]` 进一步拆分；
2. **bootstrap 内联路由下沉**：`api/{admin,public}/src/lib.rs` 仍内联约 20 条早期路由；
3. **RBAC 权限矩阵**：864 条接口带权限码，当前 `require_auth` 只校验登录态；
4. **事务 / MQ / 定时任务 / i18n**：沿用评审 `docs/architecture-review.md` §6 的路线。

---

## 8. 继续迁移的操作手册（下一批可直接照做）

```bash
# 1. 看还剩什么（按 ctx/feature 聚合、P0→P2）
cd rust && python tools/coverage_by_domain.py          # 按域覆盖率
python tools/plan_domains.py --batch-size 6            # 剩余特性批次计划

# 2. 为一批 feature 生成精确契约（method/path/handler/Java 源文件行号）
python tools/restructure/gen_wave_specs.py --features trade/order_log,payment/pay --out docs/_waveN-specs.json

# 3. 每个实现者只写自己的四层文件（严禁共享文件、严禁 cargo）
#    规范见 docs/ARCHITECTURE.md §4；模板：system/csrf（最小）、catalog/brand（完整四层）

# 4. Lead 统一注册（可在实现者并行期间/之后串行执行；幂等）
python tools/restructure/register_feature.py --ctx <ctx> --feature <f> \
  --service-type <F>Service --layers models,repo,service,admin_route \
  [--ctor-args "repos.clone(), cache.clone(), jwt.clone()"]

# 5. 编译 + 修整合错误（唯一编译入口，串行）
cd crates && cargo check --workspace --all-targets

# 6. 验收（全部必须通过，再提交）
cargo test --workspace                             # 0 failed
cd .. && python tools/check_api_coverage.py        # 覆盖率只增不减
python tools/check_route_conflicts.py --strict     # 0 重复
python tools/check_sql_columns.py --strict         # 0 未知表列
python tools/check_structure.py --strict           # exit 0
```

约定与坑（wave1-4 实测）：
- **仓储访问一律 NESTED**：`self.repos.<ctx>.<feature>.<method>()`。
- 新增 feature 若写 admin 匿名白名单路径，用 `public_routes(&AdminState)`；public 侧三种形态：
  `routes(&PublicState)`（自带 optional_auth/require_auth）、`public_routes()`（无状态匿名）、
  `anonymous_routes(&PublicState)`（需要 state 的匿名）。
- root 路由（`/im/**`、`/img/**`、`/s/**`）放在 `api/*/src/root/<ctx>/<feature>.rs`，
  并**手工**把新模块加入 `root/<ctx>/mod.rs` 的 `merge_protected/merge_public` 与
  `implemented_routes()`（`sync_routes.py` 不处理 root 聚合器）。
- 大 feature 用 `ROUTE_TABLE` + 循环注册时，覆盖率工具现在会解析 `ROUTE_TABLE`，无需写 102 个
  `.route("...")` 字面量。
- 测试里时区/金额/掩码断言必须对照 Java 工具类（`AnonymousMaskUtils`、`StringUtils.maskMiddleHalf`、
  `BigDecimal.setScale(2, HALF_UP)` 会补零）——wave1/3 各抓到过一类“实现对、测试错”的情况。
- 多租户 `shop_id` 仍来自请求头/query（JWT 无 shop_id），属已知全局缺口，按 Java `HeaderUtils`
  分支实现并在模块头标注。


## 9.5 迁移完成情况

**契约内 1564/1564 接口全部有 Rust 实现（100%）**，`check_api_coverage.py` 未实现 = 0。

已迁移范围：14 个 bounded context 的全部 feature（identity / merchant / wholesale / catalog / trade /
payment / marketing / member / logistics / content / system / im / analytics / openapi），每 feature 都有
models + repo + service + route 四层与路由表一致性测试。

**仍有明确「未迁移」业务错误（诚实降级，不伪造数据）的依赖**：
- 外部渠道 SDK：微信支付/支付宝/PayPal/Apple Pay/拉卡拉/汇付/易宝/云支付/YaBand 的下单与验签，
  微信小程序码/视频号/公众号菜单推送/客服会话、对象存储上传（OSS/COS/S3）、Excel 导入导出、
  AJ-Captcha、短信/邮件发送、物流平台查询、电子发票平台、分账渠道 API。
- 跨域计算引擎：商品价格/促销/优惠券/运费试算、订单结算、秒杀/拼团库存活动计算、优惠券匹配缓存等
  在少数结算页接口中保留端点和校验后返回「未迁移」错误。
- 横切面：RBAC 方法级权限矩阵（当前仅登录态）、部分 Redis 缓存失效广播、MQ/定时任务、多租户
  `shop_id` 的 JWT claim（用请求头/query 兜底）、i18n 翻译包。

这些均在对应模块头文档化，并在 `ARCHITECTURE.md §10` 的横切债务表中有汇总。

## 9.6 前端兼容层与「只用 Rust 跑起来」

原 100% 契约覆盖只保证「路径/方法存在」，不等于现有前端可用。本轮补齐了前端兼容层，实际验证通过的链路：
MySQL（demo.sql）+ Redis 可缺省 → Rust 服务启动（8080/8181）→ 管理员登录（返回 	oken/adminType）→
菜单权限 → 分页列表（
ecords/size/current/pages）→ 图片/视频上传（本地存储 + /static）→ 静态资源访问。

关键改动：
- 登录 LoginVO 同时输出 	oken/dminType（前端读这两个字段）；
- PageResult 序列化同时输出 Java MyBatis-Plus 形状与历史字段，请求参数兼容 size；
- 响应体同时输出 msg 与 message；
- 修正 login/signin、health、/api 公开路由未挂在契约前缀下的问题；
- 本地上传（galleryPic/uploadImg、galleryVideoInfo/uploadVideo）+ /static 静态服务；
- MySQL UNSIGNED 归一化为 signed（工具 	ools/compat/mysql_signed_schema.py）；
- 修正 product_manage 列表占位符/绑定数不一致（MySQL 1835）与 axum 0.8 路由参数写法。

详细运行步骤、nginx 代理示例与冒烟清单见 docs/RUN-RUST-FRONTEND.md。

仍未迁移（页面/流程不可用，但服务可启动）：支付渠道（全部 create 返回未迁移）、短信/邮件验证码、
微信生态、Excel 导入导出、IM WebSocket、定时任务/MQ、RBAC 方法级权限、多租户 header 完整隔离、
支付证书/APP 更新包上传。

## 9. 参考文档

- `docs/ARCHITECTURE.md` —— 目标结构契约与验收门禁（权威）
- `docs/architecture-review.md` —— 迁移前独立评审（含 12 项风险与 M1–M6 必须项）
- `docs/domain-map.json` —— 权威映射（220 切片 + 19 baseline feature + 251 控制器）
- `docs/domain-roadmap.md` —— 剩余待实现接口的按域路线图（随覆盖率推进）
- `docs/TOOLING.md` —— 门禁工具用法与退出码
- `docs/coverage-by-domain.json` —— 按域覆盖率快照
