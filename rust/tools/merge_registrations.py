#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""把各切片子代理回传的 needs_registration 合并进共享文件。

设计：
- 共享文件主会话独占修改，此脚本只负责"幂等追加"，绝不覆盖已有内容。
- 每个切片用唯一标记 /* slice-NNN begin */ ... /* slice-NNN end */ 包裹，
  重复运行只更新该块，不会重复插入。
- 修改文件前先 git stash 友好的快照：调用方应已 git commit 基线。

用法：
  python tools/merge_registrations.py --results results.json
其中 results.json 是 [{slice_id, needs_registration:{...}}, ...]
"""
import argparse
import json
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(HERE))  # .../java-b2b2b-pro-v5.8.57

# 块名 -> (目标文件相对根目录, 目标字段说明)
# 实际插入点通过标记注释定位，见 INSERT_POINTS
INSERT_POINTS = {
    # admin routes/mod.rs
    "admin_routes_mod": ("rust/crates/api/admin/src/routes/mod.rs",
                         "// == slice auto-registration =="),
    # public routes/mod.rs
    "public_routes_mod": ("rust/crates/api/public/src/routes/mod.rs",
                          "// == slice auto-registration =="),
    # services lib.rs
    "services_mod": ("rust/crates/services/src/lib.rs",
                     "// == slice auto-registration =="),
    # repo mod.rs
    "repo_mod": ("rust/crates/infrastructure/src/db/repo/mod.rs",
                 "// == slice auto-registration =="),
    # admin lib.rs (state field)
    "admin_state_field": ("rust/crates/api/admin/src/lib.rs",
                          "    pub paylog: Arc<PaylogService>,  // auto-reg marker"),
    # admin merge line
    "admin_merge_line": ("rust/crates/api/admin/src/routes/mod.rs",
                         "    // == merge auto-registration =="),
    # admin route doc line
    "admin_route_doc_line": ("rust/crates/api/admin/src/routes/mod.rs",
                             "    // == doc auto-registration =="),
    # public state field
    "public_state_field": ("rust/crates/api/public/src/lib.rs",
                           "    pub paylog: Arc<PaylogService>,  // auto-reg marker"),
    # public merge line
    "public_merge_line": ("rust/crates/api/public/src/routes/mod.rs",
                          "    // == merge auto-registration =="),
    # public route doc line
    "public_route_doc_line": ("rust/crates/api/public/src/routes/mod.rs",
                              "    // == doc auto-registration =="),
    # repo field
    "repo_field": ("rust/crates/infrastructure/src/db/repo/mod.rs",
                   "    // == field auto-registration =="),
    # repo init
    "repo_init": ("rust/crates/infrastructure/src/db/repo/mod.rs",
                  "        // == init auto-registration =="),
    # main services
    "main_services": ("rust/crates/bin/src/main.rs",
                      "    // == services auto-registration =="),
    # main admin state
    "main_admin_state": ("rust/crates/bin/src/main.rs",
                         "        paylog: paylog.clone(),  // auto-reg marker"),
    # main public state
    "main_public_state": ("rust/crates/bin/src/main.rs",
                          "        paylog: paylog.clone(),  // auto-reg marker"),
}


def block(slice_id, content):
    return ("\n/* %s begin */\n%s\n/* %s end */\n" % (slice_id, content.strip(), slice_id))


def upsert_block(path, marker, slice_id, content):
    """在 marker 行之后插入/更新本切片的块，幂等。"""
    with open(path, encoding="utf-8") as f:
        text = f.read()
    begin = "/* %s begin */" % slice_id
    end = "/* %s end */" % slice_id
    # 移除旧的本切片块
    pat = re.compile(r"/\* %s begin \*/.*?/\* %s end \*/\n?" % (slice_id, slice_id), re.S)
    text = pat.sub("", text)
    if marker not in text:
        raise RuntimeError("marker 未找到于 %s: %s" % (path, marker))
    newblock = block(slice_id, content)
    text = text.replace(marker, marker + newblock, 1)
    with open(path, "w", encoding="utf-8") as f:
        f.write(text)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--results", required=True)
    args = ap.parse_args()
    results = json.load(open(args.results, encoding="utf-8"))

    # 收集全部需要插入的内容
    edits = {}  # (path, marker) -> {slice_id: content}
    for r in results:
        sid = r["slice_id"]
        reg = r.get("needs_registration", {})
        for key, val in reg.items():
            if not val or key not in INSERT_POINTS:
                continue
            path, marker = INSERT_POINTS[key]
            path = os.path.join(ROOT, path)
            edits.setdefault((path, marker), {})[sid] = val

    for (path, marker), by_slice in edits.items():
        for sid, val in by_slice.items():
            try:
                upsert_block(path, marker, sid, val)
                print("OK  %s -> %s (%s)" % (sid, os.path.relpath(path, ROOT), marker[:30]))
            except Exception as e:
                print("ERR %s: %s" % (sid, e), file=sys.stderr)

    print("合并完成。请随后人工 review 改动并运行 cargo check。")


if __name__ == "__main__":
    sys.exit(main())
