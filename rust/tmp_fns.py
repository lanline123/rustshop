import re,os,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
for base in ["crates/api/admin/src/routes","crates/api/public/src/routes","crates/api/admin/src/root","crates/api/public/src/root"]:
    if not os.path.isdir(base): continue
    print("==",base)
    for f in sorted(os.listdir(base)):
        if not f.endswith(".rs") or f=="mod.rs": continue
        t=open(os.path.join(base,f),encoding="utf-8",errors="replace").read()
        fns=re.findall(r'pub fn (\w+)\(',t)
        print("  %-16s %s"%(f,", ".join(fns)))
