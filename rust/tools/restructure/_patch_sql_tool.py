import io

p = "tools/check_sql_columns.py"
t = io.open(p, encoding="utf-8", errors="replace").read()
assert 'files = sorted(REPO_DIR.glob("*.rs"))' in t
t = t.replace('files = sorted(REPO_DIR.glob("*.rs"))',
              'files = sorted(REPO_DIR.rglob("*.rs"))')
old = 'print(f"\\n扫描 {len(files)} 个仓储文件 / {total_stmts} 条 SQL 语句")'
assert old in t
new = (old + "\n    if len(files) < 80:\n"
       '        print(f"\\n⚠️  仓储文件数仅 {len(files)}，低于下限 80：'
       '递归扫描可能未覆盖新目录结构", file=sys.stderr)\n'
       "        return 1")
t = t.replace(old, new)
io.open(p, "w", encoding="utf-8", newline="\n").write(t)
print("patched check_sql_columns.py")
