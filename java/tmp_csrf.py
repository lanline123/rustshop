import os,re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
hits=[]
for dp,_,fs in os.walk("."):
    if "/target/" in dp: continue
    for f in fs:
        if not f.endswith(".java"): continue
        p=os.path.join(dp,f)
        t=open(p,encoding="utf-8",errors="replace").read()
        if re.search(r"csrf|CSRF",t,re.I):
            hits.append(p)
print(len(hits))
for h in hits[:12]: print(h)
