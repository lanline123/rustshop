#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成子代理的切片实现指令（携带历次批次踩坑沉淀的硬性约束）。

为什么单独抽出来：前两批反复出现同类编译/测试失败（raw string 提前闭合、
serde(default) 缺 Default 派生、孤儿规则 From 实现、Option 与裸值不匹配、
RouteDoc 是 String 而非 &str、macro_rules 位置、connect_lazy 需要 tokio 等）。
把「已证实的踩坑清单」固化进提示词，可显著降低主会话的返工量。

用法::

    python tools/slice_instruction.py slice-185            # 打印该片的完整指令
"""
import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
RUST = os.path.dirname(HERE)

sys.stdout.reconfigure(encoding="utf-8", errors="replace")

PITFALLS = r"""
## 已证实的编译 / 测试踩坑清单（前两批反复出现，务必逐条自查）

1. **原始字符串提前闭合**：`r#"..."#` 在内容里出现 `"#`（例如
   `"catColor":"#fff"`）就会提前结束。改用 `r##"..."##`。
2. **`#[serde(default)]` 必须配合 `Default`**：结构体上写了 `#[serde(default)]`
   就必须 `#[derive(..., Default)]`，否则 E0277。
3. **孤儿规则**：不能为「两个都在别的 crate 定义的类型」写 `impl From<A> for B`
   （E0117）。改用本 crate 内的自由函数。
4. **`RouteDoc` 的 `method`/`path` 是 `String`**，不是 `&str`。测试里构造期望值
   请用 `Vec<(String, String)>` 或 `.to_string()`。
5. **`macro_rules!` 必须先定义后使用**（同文件内按行序）。放在 `impl` 之前。
6. **`connect_lazy` / 任何需要 Tokio 运行时的代码，测试必须用
   `#[tokio::test] async fn`**，不能用 `#[test]`。
7. **`tigshop_core::Result` 会遮蔽 `std::result::Result`**：若你要写「返回
   `Result<T, D::Error>` 的 serde 辅助函数」，必须写全 `std::result::Result<...>`，
   否则会命中本 crate 的别名而报「type alias takes 1 generic argument」。
8. **Option 与裸值不要凭感觉**：调用点已解包 `Option` 就写裸类型，别两边混。
   改签名前先 `grep` 出**全部**调用点。
9. **金额一律 `rust_decimal::Decimal`**，禁止 f64/f32。
10. **JSON 序列化的 null 语义要按「本域实际用的工具」判断，不要想当然**：
    - Hutool `JSONUtil.toJsonStr` → `JSONConfig.ignoreNullValue` 默认 **true**，
      **丢弃** null 字段。
    - 项目自有 `com.tigshop.common.utils.JsonUtil` → 是 **Jackson**
      (`ObjectMapper.writeValueAsString`) + `Include.ALWAYS` + `INDENT_OUTPUT`，
      **保留** null 字段且**美化输出**。
    拿不准就先去 Java 侧确认用的是哪一个，再决定要不要 `skip_serializing_if`。
11. **写测试时不要手数绑定/占位符个数**：用 `sql.matches('?').count()` 之类
    的表达式，或直接断言关键子串，避免数错。
12. **`is_empty()` 之类断言要和实现里真正的写入条件一致**（例如实现判
    `Some(Some(_))`，`is_empty()` 就不能只判 `.is_none()`）。
13. **不要写「测试和实现互相印证」的假绿**：如果实现有 bug，测试断言同一个
    错误行为也会通过。请对着 Java 源码写期望值（本批已有实例：slice002 的
    `is_store_splited` 分支被判反，测试与实现一起错）。
"""


def main():
    if len(sys.argv) < 2:
        print("用法: python tools/slice_instruction.py slice-185", file=sys.stderr)
        return 1
    sid = sys.argv[1]
    key = sid.replace("-", "")
    plan = json.load(open(os.path.join(RUST, "docs", "batches.json"), encoding="utf-8"))
    meta = None
    for batch in plan["batches"]:
        for s in batch:
            if s["slice_id"] == sid:
                meta = s
                break
        if meta:
            break
    if meta is None:
        print("未在 batches.json 找到 %s" % sid, file=sys.stderr)
        return 1

    mods = meta["modules"]
    print(
        "请先运行这条命令，读取你的完整切片任务书，然后严格按任务书执行：\n\n"
        "cd D:\\tigshop\\java-b2b2b-pro-v5.8.57\\rust ; "
        "python tools/slice_prompt.py %s\n\n"
        "## 本片信息\n"
        "- slice_id: `%s`（业务域「%s」，接口数 %d）\n"
        "- 涉及 Java 控制器: %s\n"
        "- 独占文件（**只允许创建/修改这些**，以及可选的 "
        "`crates/core/src/models/%s.rs`）:\n%s\n"
        "- 模块: %s\n\n"
        "## 硬性纪律\n"
        "- **严禁运行 cargo / cargo check / build / test**（并发编译会 OOM 并阻塞其他任务）。\n"
        "  核对列名用 `python tools/schema_parser.py --table <表名>`。\n"
        "- **严禁修改任何共享文件**，包括但不限于：`core/src/models/mod.rs`、"
        "`api/*/src/routes/mod.rs`、`api/*/src/lib.rs`、`services/src/lib.rs`、"
        "`infrastructure/src/db/repo/mod.rs`、`db/mod.rs`、`bin/src/main.rs`、"
        "任何 `Cargo.toml`、`core/src/entities/**`。\n"
        "  若你新建了 `core/src/models/%s.rs`，**不要**自己加 `pub mod` 声明，"
        "在 `notes` 里说明即可（主会话统一补）。\n"
        "- 可参考的既有实现样本：`crates/api/admin/src/routes/slice001.rs`、"
        "`crates/services/src/slice075.rs`、`crates/infrastructure/src/db/repo/slice075.rs`。\n"
        "- 只做运行时 SQL（`sqlx` 的 `query`/`query_as`/`QueryBuilder`），"
        "**禁止** `query!` / `query_as!` 宏（本机无 DATABASE_URL）。\n"
        "- 路由路径**逐字符**照抄契约；`implemented_routes()` 里写**完整**契约路径。\n"
        "- 业务规则无法从 Java 源码确定时，**绝不编造**：实现其余部分，"
        "把疑点写进 `uncertain_points`，`status` 填 `partial`。\n"
        % (
            sid,
            sid,
            meta["tag"],
            meta["api_count"],
            json.dumps(meta["controllers"], ensure_ascii=False),
            key,
            "\n".join("  - `%s`" % f for f in meta["owned_files"]),
            json.dumps(mods, ensure_ascii=False),
            key,
        )
    )
    print(PITFALLS)
    print(
        "## 交付\n"
        "最后**只输出**任务书要求的那个 JSON 对象（`slice_id` / `status` / "
        "`files_created` / `routes_implemented` / `needs_registration` / "
        "`uncertain_points` / `notes`），不要额外散文。"
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
