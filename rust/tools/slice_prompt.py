#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""为某个切片生成子代理 prompt（把契约/SQL 护栏直接嵌进去，防臆造）。

输出给 workflow 使用；只拼装文本，不写 Rust。
用法: python tools/slice_prompt.py slice-001
"""
import json
import os
import sys

try:
    sys.stdout.reconfigure(encoding="utf-8")
except Exception:
    pass

HERE = os.path.dirname(os.path.abspath(__file__))
RUST = os.path.dirname(HERE)
ROOT = os.path.dirname(RUST)
SLICES = os.path.join(RUST, "docs", "slices.json")

SHARED = """\
【共享文件——主会话独占，严禁你修改，改了就是越界】
- crates/api/admin/src/lib.rs（含 AdminState 定义 + router()）
- crates/api/public/src/lib.rs（含 PublicState 定义 + router()）
- crates/api/{admin,public}/src/routes/mod.rs
- crates/services/src/lib.rs、crates/infrastructure/src/db/repo/mod.rs
- crates/bin/src/main.rs、任何 Cargo.toml、crates/core/src/**
你需要新增的 mod 声明 / 状态字段 / 路由挂载，一律写进返回值的
needs_registration 字段，由主会话统一合并。
"""

TEMPLATE = """\
你是 TigShop Java(Spring Boot 3.3 + MyBatis Plus) → Rust(Axum 0.8 + SQLx 0.8 + tokio)
重构项目的**切片实现者**。工作目录：{root}

# 你的切片
- slice_id: {slice_id}   业务域 tag: {tag}   接口数: {api_count}
- 你**独占**的文件（只有你能创建/修改，绝不与其他切片冲突）：
{owned}

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
5. 你只新建上面"独占"的文件，以及（必要时）在
   crates/core/src/models/ 下新建本域 DTO。**不要改任何共享文件**。
6. 不要改 crates/core/src/entities/**（321 张表实体已 100% 自动生成）。

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

# 权威资料（自己读，不要凭记忆）
- 本片 Java 源控制器：{java_src}
- 权威 schema：`java/database/pure.sql`；
  查某表最终列定义用：
  `cd {rust} && python tools/schema_parser.py --table <表名>`
- 契约全量：`rust/docs/api-contract.json`；
  校验覆盖率：`cd {rust} && python tools/check_api_coverage.py`

# 本片接口清单（权威，逐条实现；path 必须逐字符一致）
{apis}

# 交付要求
1. 写好代码，每个 handler 带简短 `///` 注释标明对应原 Java 方法。
2. 为服务层纯逻辑补 `#[cfg(test)]` 单测（金额计算/状态流转/参数校验）。
3. **不要自己跑 cargo check**（本机 16 核 / 13.7GB，并发编译会 OOM 且拖慢
   他人；编译由主会话统一串行做）。
4. 返回严格 JSON（不要额外散文）：
{{
  "slice_id": "{slice_id}",
  "status": "done" | "partial" | "blocked",
  "files_created": ["相对仓库根的路径", ...],
  "routes_implemented": ["GET /adminapi/...", ...],
  "needs_registration": {{
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
  }},
  "uncertain_points": ["诚实列出：路由语义不明/业务规则矛盾/Java 逻辑读不懂的地方"],
  "notes": "简短说明"
}}
⚠️ uncertain_points 必须诚实填写。猜出来的代码能过编译和单测，但上线就错，
且极难发现。**宁可标 uncertain 也不要编造业务规则**。

{shared}
"""


def main():
    sid = sys.argv[1]
    slices = json.load(open(SLICES, encoding="utf-8"))
    s = next((x for x in slices if x["slice_id"] == sid), None)
    if not s:
        print("no slice %s" % sid, file=sys.stderr)
        return 1

    apis = "\n".join(
        "- %-6s %-52s | %s.%s | auth=%s perm=%s | src=%s" % (
            a["method"], a["path"], a["controller"], a["handler"],
            a["auth"] or "-", a["permissions"] or "-", a["source"])
        for a in s["apis"])

    print(TEMPLATE.format(
        root=ROOT, rust=RUST,
        slice_id=s["slice_id"], tag=s["tag"], api_count=s["api_count"],
        owned="\n".join("  - " + f for f in s["owned_files"]),
        java_src="\n  - ".join(s["java_sources"][:12]) or "（见 apis[].source）",
        apis=apis, shared=SHARED))
    return 0


if __name__ == "__main__":
    sys.exit(main())
