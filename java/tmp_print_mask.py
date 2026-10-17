import os,re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
# find mask functions used by print service
for p in ["tigshop-service/src/main/java/com/tigshop/service/print/impl/PrintServiceImpl.java"]:
    if not os.path.isfile(p): print("missing",p); continue
    t=open(p,encoding="utf-8",errors="replace").read()
    for i,l in enumerate(t.splitlines(),1):
        if re.search(r"mask|desens|hide|printSn|printKey|thirdKey",l,re.I):
            print(i,l.strip()[:160])
