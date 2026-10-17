import re,os,sys,collections
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
sigs=collections.Counter()
for base in ["crates/api/admin/src/routes","crates/api/public/src/routes","crates/api/admin/src/root","crates/api/public/src/root"]:
    if not os.path.isdir(base): continue
    for f in sorted(os.listdir(base)):
        if not f.endswith(".rs") or f=="mod.rs": continue
        t=open(os.path.join(base,f),encoding="utf-8",errors="replace").read()
        for m in re.finditer(r'pub fn routes\(([^)]*)\)\s*->\s*([^\{]+)\{',t):
            sigs[(base.split('/')[2], base.split('/')[4], m.group(1).strip().replace('state: ','').replace('&','&'), m.group(2).strip())]+=1
for k,v in sigs.most_common(20): print("%-8s %-6s args=%-20s ret=%-28s x%d"%(k[0],k[1],k[2][:20],k[3][:28],v))
