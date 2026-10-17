import sys,os,collections
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
import importlib.util
spec=importlib.util.spec_from_file_location("cov","tools/check_api_coverage.py")
cov=importlib.util.module_from_spec(spec); spec.loader.exec_module(cov)
rows=[]
for crate,prefix in cov.CRATE_PREFIX.items():
    root=os.path.join("crates","api",crate,"src")
    for dirpath,_d,filenames in os.walk(root):
        sub=os.path.relpath(dirpath,root).replace("\\","/")
        eff="" if sub=="root" or sub.startswith("root/") else prefix
        for fn in filenames:
            if not fn.endswith(".rs"): continue
            full=os.path.join(dirpath,fn); text=cov.strip_comments(cov.read(full)); rel=os.path.relpath(full,"crates")
            for m in cov.ROUTE_START_RE.finditer(text):
                path=m.group("path"); op=text.index("(",m.start()); tail=cov.extract_route_args(text,op)
                for hm in cov.METHOD_CALL_RE.finditer(tail):
                    rows.append((hm.group(1).upper(), cov.join_path(eff,path), rel))
print("rows from admin routes/shop.rs:",[r for r in rows if r[2].endswith("routes\\shop.rs") or r[2].endswith("routes/shop.rs")])
print("rows matching merchant/shop/list:",[r for r in rows if "merchant/shop/list" in r[1]])
