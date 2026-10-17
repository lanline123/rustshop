import os,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
n=0
for dp,_,fs in os.walk("crates"):
    if "target" in dp.split(os.sep): continue
    for f in fs:
        if not f.endswith(".rs"): continue
        t=open(os.path.join(dp,f),encoding="utf-8",errors="replace").read()
        if "AppState" in t:
            for i,l in enumerate(t.splitlines(),1):
                if "AppState" in l: print(os.path.join(dp,f),i,l.strip()); n+=1
print("total",n)
