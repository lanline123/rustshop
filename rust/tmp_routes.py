import re,sys,os,glob
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
for f in sorted(glob.glob("crates/api/admin/src/routes/*.rs")+glob.glob("crates/api/public/src/routes/*.rs")):
    if "slice" in os.path.basename(f): continue
    t=open(f,encoding="utf-8",errors="replace").read()
    routes=re.findall(r'\.route\(\s*"([^"]+)"',t)
    print("%-46s %2d  %s"%(f,len(routes),", ".join(routes[:8])))
print("\n=== admin lib.rs inline routes ===")
t=open("crates/api/admin/src/lib.rs",encoding="utf-8",errors="replace").read()
print("\n".join(re.findall(r'\.route\(\s*"([^"]+)"',t)))
print("\n=== public lib.rs inline routes ===")
t=open("crates/api/public/src/lib.rs",encoding="utf-8",errors="replace").read()
print("\n".join(re.findall(r'\.route\(\s*"([^"]+)"',t)))
