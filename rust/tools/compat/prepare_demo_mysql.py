import re
from pathlib import Path

src = Path(r"D:\tigshop\java-b2b2b-pro-v5.8.57\java\database\demo.sql")
out = Path(r"D:\tigshop\java-b2b2b-pro-v5.8.57\rust\deploy\mysql-init\01-demo-signed.sql")

type_re = re.compile(
    r"\b(tinyint|smallint|mediumint|int|bigint)(?:\s*\(\s*\d+\s*\))?"
    r"(?:\s+unsigned(?:\s+zerofill)?|\s+zerofill)?",
    re.IGNORECASE,
)

print("Reading demo.sql...")
text = src.read_text(encoding="utf-8", errors="replace")

# 1. Strip unsigned from integer types (for sqlx Rust models)
converted = type_re.sub(lambda m: m.group(1), text)

# 2. Replace json type with longtext (for MySQL 5.6 compatibility)
json_re = re.compile(r"(`[a-zA-Z0-9_]+`\s+)json\b", re.IGNORECASE)
converted = json_re.sub(r"\1longtext", converted)

# 3. Replace MySQL 8.0 collation utf8mb4_0900_ai_ci with utf8mb4_general_ci
converted = re.sub(r"utf8mb4_0900_ai_ci", "utf8mb4_general_ci", converted, flags=re.IGNORECASE)

with open(out, "w", encoding="utf-8", newline="\n") as f:
    f.write(converted)

print(f"Successfully wrote {out.stat().st_size} bytes to {out}")
