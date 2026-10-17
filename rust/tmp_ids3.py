import re,os,sys,collections
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
pat=re.compile(r'\b(Slice\d{3}[A-Za-z0-9_]*)\b')
cnt=collections.Counter()
for dp,_,fs in os.walk("crates"):
    if "target" in dp.split(os.sep): continue
    for f in fs:
        if not f.endswith(".rs"): continue
        t=open(os.path.join(dp,f),encoding="utf-8",errors="replace").read()
        for m in pat.finditer(t): cnt[m.group(1)]+=1
forms=collections.Counter(re.sub(r'\d{3}','NNN',k) for k in cnt)
for k,v in forms.most_common(50): print("%-40s %d"%(k,v))
