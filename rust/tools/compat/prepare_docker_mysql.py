#!/usr/bin/env python3
"""Generate Docker MySQL init SQL from the Java ``pure.sql``.

The Java dump uses ``INT UNSIGNED`` / ``BIGINT UNSIGNED`` columns, while the
Rust sqlx models decode signed integers (i32/i64).  MySQL's own dump cannot be
imported directly or every out-of-range/negative value fails.  This script
strips the ``UNSIGNED`` modifier from integer type declarations and writes a
single init file for ``docker-entrypoint-initdb.d``.
"""

from __future__ import annotations

import io
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT.parent / "java" / "database" / "pure.sql"
OUT = ROOT / "deploy" / "mysql-init" / "00-pure.sql"

TYPE_RE = re.compile(
    r"\b(tinyint|smallint|mediumint|int|bigint)(?:\s*\(\s*\d+\s*\))?"
    r"(?:\s+unsigned(?:\s+zerofill)?|\s+zerofill)?",
    re.IGNORECASE,
)


def strip_unsigned(text: str) -> str:
    def repl(match: re.Match[str]) -> str:
        # 去掉显示宽度与 UNSIGNED/ZEROFILL；MySQL 8 对宽度无要求
        return match.group(1)

    return TYPE_RE.sub(repl, text)


def main() -> int:
    if not SRC.exists():
        sys.stderr.write(f"missing source: {SRC}\n")
        return 1
    text = SRC.read_text(encoding="utf-8", errors="replace")
    converted = strip_unsigned(text)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    with io.open(OUT, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(converted)
    print(f"wrote {OUT} ({OUT.stat().st_size} bytes)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
