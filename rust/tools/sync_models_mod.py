#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""自动把 core/src/models/ 下已存在但未声明的 sliceNNN.rs 补进 models/mod.rs。

背景：子代理被禁止修改 models/mod.rs（共享文件），但每片新建的 DTO 文件
必须被声明才会参与编译；漏声明会让 `cargo check` 报 unresolved import。
本脚本只做「幂等追加声明行」，不碰任何别的文件。

用法::

    python tools/sync_models_mod.py            # 报告并修复
    python tools/sync_models_mod.py --dry-run  # 只报告
"""
import argparse
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
RUST = os.path.dirname(HERE)
MODELS = os.path.join(RUST, "crates", "core", "src", "models")
MOD_RS = os.path.join(MODELS, "mod.rs")

sys.stdout.reconfigure(encoding="utf-8", errors="replace")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()

    text = open(MOD_RS, encoding="utf-8").read()
    files = sorted(
        f[:-3] for f in os.listdir(MODELS)
        if f.startswith("slice") and f.endswith(".rs")
    )
    missing = [n for n in files if not re.search(r"pub mod %s;" % re.escape(n), text)]

    print("models/ 下 slice 文件 %d 个，未声明 %d 个" % (len(files), len(missing)))
    if not missing:
        print("✅ 无需修复")
        return 0
    print("待补声明: %s" % ", ".join(missing))
    if args.dry_run:
        return 0

    # 插到 "// ——— 切片域" 标记区之后；若无标记则插到第一个 pub mod slice 之前
    marker = "// ——— 切片域（并行重构，按序号）———"
    # 每个声明独占一行（曾误用 `";".join` 生成 `pub mod a;;pub mod b;` 导致语法错误）
    block = "".join("pub mod %s;\n" % m for m in missing)
    if marker in text:
        text = text.replace(marker, marker + "\n" + block, 1)
    else:
        idx = text.find("pub mod slice")
        if idx < 0:
            print("找不到插入锚点", file=sys.stderr)
            return 1
        text = text[:idx] + marker + "\n" + block + text[idx:]

    with open(MOD_RS, "w", encoding="utf-8") as fh:
        fh.write(text)
    print("已写入 models/mod.rs：%s" % ", ".join(missing))
    return 0


if __name__ == "__main__":
    sys.exit(main())
