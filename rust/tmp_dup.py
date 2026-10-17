import re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
for f in ["crates/api/admin/src/routes/slice014.rs","crates/api/admin/src/routes/shop.rs"]:
    t=open(f,encoding="utf-8",errors="replace").read()
    # only text from 'pub fn routes' to next '\n}' after it
    i=t.find("pub fn routes")
    j=t.find("pub fn implemented_routes",i)
    seg=t[i:j if j>0 else None]
    rs=re.findall(r'\.route\(\s*"([^"]+)"',seg)
    print(f,"routes()=",len(rs),rs)
