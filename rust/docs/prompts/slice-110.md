你是 TigShop Java(Spring Boot 3.3 + MyBatis Plus) → Rust(Axum 0.8 + SQLx 0.8 + tokio)
重构项目的**切片实现者**。工作目录：D:\tigshop\java-b2b2b-pro-v5.8.57

# 你的切片
- slice_id: slice-110   业务域 tag: 店铺表   接口数: 8
- 你**独占**的文件（只有你能创建/修改，绝不与其他切片冲突）：
  - crates/api/admin/src/routes/slice110.rs
  - crates/services/src/slice110.rs
  - crates/infrastructure/src/db/repo/slice110.rs

# 硬约束（违反即判定失败，会被自动校验抓回）
1. **路由路径逐字符照抄**下面 apis[].path，禁止"顺手简化"。真实路径常出现
   控制器前缀重复一次，例如 `/adminapi/product/product/list`、
   `/api/cart/cart/list`、`/api/user/order/list` —— 这不是笔误，必须照抄。
   路径不参与编译校验，写错 cargo check 全绿但前端 404。
2. **SQL 表名/列名**必须来自权威 schema，禁止推测。查询方式统一用运行时
   `sqlx::query_as::<_, T>(...)`，**不要用 `query!` 宏**（本机无 DATABASE_URL，
   宏会编译失败）。列名写错 `cargo check` 也查不出，只有
   `python tools/check_sql_columns.py --strict` 能查。
3. 金额一律 `rust_decimal::Decimal`，**禁用 f64/f32**。
4. 库存扣减必须写成 `UPDATE ... WHERE stock >= ?` 形式（原子防超卖）。
5. 你只改上面"独占"的文件（它们**可能已存在为空骨架**——那就直接**原地覆盖**为
   真实实现，并删掉文件头里的「骨架」字样），以及（必要时）在
   crates/core/src/models/ 下新建/补全本域 DTO。**不要改任何共享文件**。
6. 不要改 crates/core/src/entities/**（321 张表实体已 100% 自动生成）。
7. **防重复注册**：本片接口清单里标了 `[已实现]` 的接口**已由其它文件实现**，
   你**绝不要**在本片重复注册同一 `(method, path)` —— axum `Router::merge`
   遇到重复路径会在**服务启动时 panic**，而 `cargo check` 完全查不出。
   只实现**未标注**的接口。
8. **保持公开签名不变**：若独占文件已存在（骨架），必须沿用现有结构体名、
   `Repository::new(pool: MySqlPool)` / `pool_clone()`、
   `Service::new(repos: Arc<Repositories>)`、`routes(state)`、
   `implemented_routes()` 的签名 —— 共享文件已按这些签名注册，改了会编译失败。
   已注册的切片**无需**再返回 needs_registration（保持为空对象即可）。

# 现有代码结构（照它的样子写，先看样板再动手）
- 路由样板：crates/api/admin/src/routes/seckill.rs（注意它顶部有
  「原 Java 路径对照表」注释，你也要写；并有 `implemented_routes()`
  返回 RouteDoc 列表供覆盖率统计——**你的文件必须提供同名同签名函数**）
- 服务样板：crates/services/src/seckill.rs
- 仓储样板：crates/infrastructure/src/db/repo/seckill.rs
- 路由挂载前缀：admin 挂 `/adminapi` 下（routes() 内写 `/product/product/list`
  这种**去掉 /adminapi 前缀**的形式）；public 挂 `/api` 下同理。
- 路由函数签名：`pub fn routes(state: &AdminState) -> Router<AdminState>`
  （public 侧是 `&PublicState` / `Router<PublicState>`）
- 响应统一：`ok(...)` 返回 `ApiResult<T>`；错误用 `tigshop_core::Error`

# 本仓库已知高频错误（都真实发生过，务必避免）
- 金额一律 `rust_decimal::Decimal`，**禁用 f64/f32**。
- `rust_decimal` **没有** `ceil_dp`/`to_i32`；向上取整用
  `round_dp_with_strategy(n, RoundingStrategy::ToPositiveInfinity)`；
  取整数需 `use rust_decimal::prelude::ToPrimitive;` 后 `to_i64()`。
  Java `HALF_UP` → `RoundingStrategy::MidpointAwayFromZero`。
- `tigshop_core::Error` 是**枚举**，**没有** `.message` 字段；消息用 `e.to_string()`。
  没有 `Error::forbidden`（用 `Error::Forbidden` 或 `Error::biz(code, msg)`）。
- **不要** `use rand::`（该依赖未声明）；随机串用 `tigshop_common::utils::random_hex()`。
- 在 `crates/core/src/models/**` 内引用 core 自身用 `crate::` 前缀，**不要** `tigshop_core::`。
- 不要写 `const fn` 里对 `&str` 的 `match`（E0658）；不要重复声明同一结构体字段。
- **查询/请求体 DTO 必须加 `#[serde(rename_all = "camelCase")]`**：前端传 camelCase，
  缺了会被静默解析成 `None`（如 `?groupId=1` 变 `None`）或直接 400。
- SQL 只用运行时 `sqlx::query` / `sqlx::query_as::<_, T>(...)` / `QueryBuilder`；
  **禁用** `query!`/`query_as!` 宏（本机无 DATABASE_URL）。
- 表名/列名用 `python tools/schema_parser.py --table <表名>` 核对；列名写错编译查不出。
- 权限：本仓库目前只做登录态校验，`@PreAuthorize` 权限矩阵**尚未实现**，不要编造。
- 单测断言值必须自己算对（本仓库多次出现把期望日期/金额/比例算错的测试）。

# 权威资料（自己读，不要凭记忆）
- 本片 Java 源控制器：tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\shop\ShopFundsController.java
- 权威 schema：`java/database/pure.sql`；
  查某表最终列定义用：
  `cd D:\tigshop\java-b2b2b-pro-v5.8.57\rust && python tools/schema_parser.py --table <表名>`
- 契约全量：`rust/docs/api-contract.json`；
  校验覆盖率：`cd D:\tigshop\java-b2b2b-pro-v5.8.57\rust && python tools/check_api_coverage.py`

# 本片接口清单（权威，逐条实现；path 必须逐字符一致）

> ⚠️ 本片 8 个接口中，**已实现 0 个（下方标 [已实现]）**，你只需实现其余 **8 个**。重复注册已实现路径会导致服务启动 panic。
- GET    /adminapi/merchant/shopAccount/list                  | ShopFundsController.list | auth=jwt perm=- |          | src=tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\shop\ShopFundsController.java:53
- GET    /adminapi/merchant/shopAccount/detail                | ShopFundsController.detail | auth=jwt perm=- |          | src=tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\shop\ShopFundsController.java:59
- POST   /adminapi/merchant/shopAccount/create                | ShopFundsController.create | auth=jwt perm=- |          | src=tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\shop\ShopFundsController.java:65
- POST   /adminapi/merchant/shopAccount/update                | ShopFundsController.update | auth=jwt perm=- |          | src=tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\shop\ShopFundsController.java:71
- POST   /adminapi/merchant/shopAccount/del                   | ShopFundsController.del | auth=jwt perm=- |          | src=tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\shop\ShopFundsController.java:77
- POST   /adminapi/merchant/shopAccount/updateField           | ShopFundsController.updateField | auth=jwt perm=- |          | src=tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\shop\ShopFundsController.java:83
- POST   /adminapi/merchant/shopAccount/batch                 | ShopFundsController.batch | auth=jwt perm=- |          | src=tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\shop\ShopFundsController.java:89
- GET    /adminapi/merchant/shopAccount/applyShopAgreement    | ShopFundsController.applyShopAgreement | auth=jwt perm=- |          | src=tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller\shop\ShopFundsController.java:95

# 交付要求
1. 写好代码，每个 handler 带简短 `///` 注释标明对应原 Java 方法。
2. 为服务层纯逻辑补 `#[cfg(test)]` 单测（金额计算/状态流转/参数校验）。
3. **不要自己跑 cargo check**（本机 16 核 / 13.7GB，并发编译会 OOM 且拖慢
   他人；编译由主会话统一串行做）。
4. 返回严格 JSON（不要额外散文）：
{
  "slice_id": "slice-110",
  "status": "done" | "partial" | "blocked",
  "files_created": ["相对仓库根的路径", ...],
  "routes_implemented": ["GET /adminapi/...", ...],
  "needs_registration": {
      "admin_routes_mod": "pub mod sliceNNN;",
      "admin_state_field": "pub sliceNNN: Arc<SliceNNNService>,",
      "admin_merge_line": ".merge(sliceNNN::routes(state))",
      "admin_route_doc_line": "out.extend(sliceNNN::implemented_routes());",
      "public_routes_mod": "...",
      "public_state_field": "...",
      "public_merge_line": "...",
      "public_route_doc_line": "...",
      "services_mod": "pub mod sliceNNN;",
      "repo_mod": "pub mod sliceNNN;",
      "repo_field": "pub sliceNNN: SliceNNNRepository,",
      "repo_init": "sliceNNN: SliceNNNRepository::new(pool.clone()),",
      "main_services": "let sliceNNN = Arc::new(SliceNNNService::new(repos.clone()));",
      "main_admin_state": "sliceNNN: sliceNNN.clone(),",
      "main_public_state": "sliceNNN: sliceNNN.clone(),"
  },
  "uncertain_points": ["诚实列出：路由语义不明/业务规则矛盾/Java 逻辑读不懂的地方"],
  "notes": "简短说明"
}
⚠️ uncertain_points 必须诚实填写。猜出来的代码能过编译和单测，但上线就错，
且极难发现。**宁可标 uncertain 也不要编造业务规则**。

【共享文件——主会话独占，严禁你修改，改了就是越界】
- crates/api/admin/src/lib.rs（含 AdminState 定义 + router()）
- crates/api/public/src/lib.rs（含 PublicState 定义 + router()）
- crates/api/{admin,public}/src/routes/mod.rs
- crates/services/src/lib.rs、crates/infrastructure/src/db/repo/mod.rs
- crates/bin/src/main.rs、任何 Cargo.toml、crates/core/src/**
你需要新增的 mod 声明 / 状态字段 / 路由挂载，一律写进返回值的
needs_registration 字段，由主会话统一合并。


