#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""切片批次流水线：合并注册 -> 一致性/SQL/路由冲突校验 -> 串行 cargo check。

把原先「每批手工跑 4 个工具 + cargo」固化成一个可重复命令，避免漏步。
本脚本**不修改任何 Rust 源码**（合并只依赖 docs/registrations/ 下的登记文件，
由主会话写入；子代理从不碰共享文件）。

用法::

    python tools/batch_pipeline.py                 # 全流程（含 cargo check）
    python tools/batch_pipeline.py --no-build      # 跳过 cargo check（快速自检）
"""
from __future__ import annotations

import argparse
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
RUST = os.path.dirname(HERE)
CRATES = os.path.join(RUST, "crates")

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
sys.stderr.reconfigure(encoding="utf-8", errors="replace")

PY = sys.executable


def run(title, cmd, cwd=RUST, allow_fail=False):
    print("\n" + "=" * 72)
    print(">>> %s" % title)
    print("=" * 72)
    env = dict(os.environ)
    # 本机 13.7GB；并发编译会 OOM（曾出现 rust_oom / STATUS_STACK_BUFFER_OVERRUN）
    env["CARGO_BUILD_JOBS"] = "1"
    proc = subprocess.run(cmd, cwd=cwd, env=env, capture_output=True)
    out = proc.stdout.decode("utf-8", "replace")
    err = proc.stderr.decode("utf-8", "replace")
    tail = "\n".join((out + err).splitlines()[-40:])
    print(tail)
    ok = proc.returncode == 0
    print("\n--> %s: %s" % (title, "OK" if ok else "FAILED (exit %d)" % proc.returncode))
    if not ok and not allow_fail:
        sys.exit(proc.returncode)
    return ok


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--no-build", action="store_true")
    args = ap.parse_args()

    # 1) 合并注册（幂等，可反复执行）
    run(
        "合并切片注册",
        [PY, os.path.join("tools", "register_slices.py"), "--dir",
         os.path.join("docs", "registrations")],
    )

    # 2) 注册一致性（插错块 / 漏插 / public-only 插进 AdminState 等）
    run("注册一致性校验", [PY, os.path.join("tools", "verify_registrations.py"), "--strict"])

    # 3) SQL 表名/列名（cargo check 查不出的错）
    run("SQL 列名校验", [PY, os.path.join("tools", "check_sql_columns.py"), "--strict"])

    # 4) axum 重复路由（重复会在服务启动时 panic）
    run("路由冲突校验", [PY, os.path.join("tools", "check_route_conflicts.py"), "--strict"])

    # 5) 覆盖率报告（仅展示，不设阈值以免中途误判失败）
    run("API 覆盖率", [PY, os.path.join("tools", "check_api_coverage.py")], allow_fail=True)

    if args.no_build:
        print("\n已跳过 cargo check（--no-build）")
        return 0

    # 6) 串行编译（唯一的编译入口）
    run(
        "cargo check --workspace",
        ["cargo", "check", "--workspace", "--message-format", "short"],
        cwd=CRATES,
    )

    # 7) 串行测试
    run("cargo test --workspace", ["cargo", "test", "--workspace"], cwd=CRATES)

    print("\n" + "=" * 72)
    print("✅ 全流程通过")
    print("=" * 72)
    return 0


if __name__ == "__main__":
    sys.exit(main())
