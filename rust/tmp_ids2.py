import re,os,sys,collections
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
pat=re.compile(r'(Slice\d{3}[A-Za-z_]*|slice\d{3}[A-Za-z_]*)')
cnt=collections.Counter()
for dp,_,fs in os.walk("crates"):
    if "target" in dp.split(os.sep): continue
    for f in fs:
        if not f.endswith(".rs"): continue
        t=open(os.path.join(dp,f),encoding="utf-8",errors="replace").read()
        for m in pat.finditer(t): cnt[m.group(1)]+=1
for k,v in cnt.most_common(30): print("%-30s %d"%(k,v))
