import re,os,sys,collections
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
pats=collections.Counter()
for dp,_,fs in os.walk("crates"):
    if "target" in dp.split(os.sep): continue
    for f in fs:
        if not f.endswith(".rs"): continue
        t=open(os.path.join(dp,f),encoding="utf-8",errors="replace").read()
        for m in re.finditer(r'use\s+tigshop_infrastructure[^;]*;',t):
            pats[m.group(0).replace("\n"," ")[:110]]+=1
for k,v in pats.most_common(20): print(v,k)
