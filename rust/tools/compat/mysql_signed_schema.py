import io
import subprocess
import sys

MYSQL = r"C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
DB = "tigshop-java"
HOST_ARGS = ["-h", "127.0.0.1", "-P", "3306", "-u", "root", "-proot"]

QUERY = """
SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_TYPE, IS_NULLABLE,
       COALESCE(COLUMN_DEFAULT, 'NULL'), EXTRA, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = '{db}' AND COLUMN_TYPE LIKE '%%unsigned%%'
ORDER BY TABLE_NAME, ORDINAL_POSITION;
""".format(db=DB)

# signed maximum for each unsigned integer base type
SIGNED_MAX = {
    "tinyint": 127,
    "smallint": 32767,
    "mediumint": 8388607,
    "int": 2147483647,
    "bigint": 9223372036854775807,
}


def build_alters():
    out = subprocess.run(
        [MYSQL, *HOST_ARGS, "--default-character-set=utf8mb4", "-N", "-B", "-e", QUERY],
        capture_output=True, text=True, encoding="utf-8", errors="replace")
    if out.returncode != 0:
        sys.stderr.write(out.stderr)
        raise SystemExit(out.returncode)

    lines = []
    for raw in out.stdout.splitlines():
        if not raw.strip():
            continue
        parts = raw.split("\t")
        table, column, data_type, column_type, is_nullable, default, extra = parts[:7]
        comment = parts[7] if len(parts) > 7 else ""
        signed = data_type
        max_value = SIGNED_MAX.get(data_type)
        if max_value is not None:
            lines.append(
                "UPDATE `%s` SET `%s` = %d WHERE `%s` > %d;"
                % (table, column, max_value, column, max_value))
        definition = signed
        definition += " NOT NULL" if is_nullable == "NO" else " NULL"
        if default not in ("NULL", "", "\\N"):
            definition += " DEFAULT %s" % default
        if "auto_increment" in extra:
            definition += " AUTO_INCREMENT"
        if comment:
            definition += " COMMENT '%s'" % comment.replace("\\", "\\\\").replace("'", "''")
        lines.append("ALTER TABLE `%s` MODIFY COLUMN `%s` %s;" % (table, column, definition))
    return lines


def main() -> int:
    mode = sys.argv[1] if len(sys.argv) > 1 else "gen"
    path = sys.argv[2] if len(sys.argv) > 2 else "alters.sql"
    lines = build_alters()
    if mode == "gen":
        with io.open(path, "w", encoding="utf-8", newline="\n") as fh:
            fh.write("\n".join(lines) + "\n")
        print("wrote", path, "statements:", len(lines))
        return 0

    sql = "\n".join(lines) + "\n"
    out = subprocess.run(
        [MYSQL, *HOST_ARGS, "--default-character-set=utf8mb4", DB],
        input=sql, capture_output=True, text=True, encoding="utf-8", errors="replace")
    print("exit:", out.returncode)
    if out.stdout.strip():
        print(out.stdout[-1500:])
    if out.stderr.strip():
        print(out.stderr[-3000:])
    return out.returncode


if __name__ == "__main__":
    sys.exit(main())
