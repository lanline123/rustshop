import os,re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
for p in ["tigshop-service/src/main/java/com/tigshop/service/product/impl/CommentServiceImpl.java"]:
    t=open(p,encoding="utf-8",errors="replace").read()
    for i,l in enumerate(t.splitlines(),1):
        if re.search(r"匿名|anonymous|mask|hide|substring|subSequence",l,re.I):
            print(i,l.strip()[:170])
