import re,os,sys,collections
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
pat=re.compile(r'(Slice\d{3}|slice\d{3}|SLICE\d{3})')
cnt=collections.Counter(); perfile=collections.Counter()
for dp,_,fs in os.walk("crates"):
    if "target" in dp.split(os.sep) or "target-verify" in dp.split(os.sep): continue
    for f in fs:
        if not f.endswith(".rs"): continue
        p=os.path.join(dp,f); t=open(p,encoding="utf-8",errors="replace").read()
        for m in pat.finditer(t): cnt[m.group(1)]+=1; perfile[p]+=1
print("total matches:",sum(cnt.values()))
print("identifier forms:")
forms=collections.Counter(re.sub(r'\d{3}','NNN',k) for k in cnt)
for k,v in forms.most_common(40): print("  %-24s %d"%(k,v))
print("\nfiles touched:",len(perfile))
