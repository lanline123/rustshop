import re,os,sys,collections
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
pats=collections.Counter()
for base in ["crates/core/src/models","crates/infrastructure/src/db/repo","crates/services/src","crates/api/admin/src","crates/api/public/src"]:
    for dp,_,fs in os.walk(base):
        for f in fs:
            if not f.endswith(".rs"): continue
            t=open(os.path.join(dp,f),encoding="utf-8",errors="replace").read()
            for m in re.finditer(r'\bsuper(::super)*::(\w+)',t):
                pats[m.group(0)]+=1
for k,v in pats.most_common(25): print("%-30s %d"%(k,v))
