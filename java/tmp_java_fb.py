import re,os,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
p="tigshop-service/src/main/java/com/tigshop/service/user/impl/FeedbackServiceImpl.java"
print("exists:",os.path.isfile(p))
if os.path.isfile(p):
    t=open(p,encoding="utf-8",errors="replace").read()
    for m in re.finditer(r".{0,120}(getShopId|shopId|ShopId).{0,200}",t):
        s=m.group(0)
        if "== 0" in s or "!= 0" in s or "null" in s.lower():
            print("---",s.replace("\n"," ")[:300])
