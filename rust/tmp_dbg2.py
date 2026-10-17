import sys,os,collections
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
import importlib.util
spec=importlib.util.spec_from_file_location("cov","tools/check_api_coverage.py")
cov=importlib.util.module_from_spec(spec); spec.loader.exec_module(cov)
rows=[]
root=os.path.join("crates","api","admin","src")
for dirpath,_d,filenames in os.walk(root):
    for fn in filenames:
        if not fn.endswith(".rs"): continue
        full=os.path.join(dirpath,fn)
        text=cov.strip_comments(cov.read(full))
        for m in cov.ROUTE_START_RE.finditer(text):
            rows.append((os.path.relpath(full,"crates"),m.group("path")))
cnt=collections.Counter(r[0] for r in rows)
print("files with routes:",len(cnt))
for f,n in sorted(cnt.items()):
    if "slice014" in f or "slice015" in f: print(f,n)
print("slice014 in list?", any("slice014" in r[0] for r in rows))
print("\nraw file has .route count:",open("crates/api/admin/src/routes/slice014.rs",encoding="utf-8",errors="replace").read().count(".route("))
t=cov.strip_comments(cov.read("crates/api/admin/src/routes/slice014.rs"))
print("after strip .route count:",t.count(".route("))
print("ROUTE_START matches:",len(list(cov.ROUTE_START_RE.finditer(t))))
