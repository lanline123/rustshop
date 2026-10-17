import os,sys,re
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
for dp,_,fs in os.walk("crates"):
    if "target" in dp.split(os.sep): continue
    for f in fs:
        if f.endswith(".rs") and "tests" in dp.split(os.sep):
            t=open(os.path.join(dp,f),encoding="utf-8",errors="replace").read()
            refs=re.findall(r'(?:crate|tigshop_\w+)::[A-Za-z0-9_:]*slice\d+[A-Za-z0-9_:]*',t)
            if refs: print(os.path.join(dp,f),len(refs),refs[:5])
