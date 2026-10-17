import re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
for f in ["crates/api/admin/src/routes/refund_apply.rs","crates/api/admin/src/routes/slice203.rs","crates/api/admin/src/routes/shop.rs","crates/api/admin/src/routes/slice014.rs"]:
    t=open(f,encoding="utf-8",errors="replace").read()
    rs=re.findall(r'\.route\(\s*"([^"]+)"',t)
    print(f,len(rs))
    for r in rs[:12]: print("   ",r)
