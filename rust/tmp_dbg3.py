import sys,os
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
import importlib.util
spec=importlib.util.spec_from_file_location("cov","tools/check_api_coverage.py")
cov=importlib.util.module_from_spec(spec); spec.loader.exec_module(cov)
f="crates/api/admin/src/routes/slice014.rs"
text=cov.strip_comments(cov.read(f))
out=[]
for m in cov.ROUTE_START_RE.finditer(text):
    path=m.group("path"); op=text.index("(",m.start()); tail=cov.extract_route_args(text,op)
    for hm in cov.METHOD_CALL_RE.finditer(tail):
        out.append((hm.group(1).upper(),path))
for o in out: print(o)
