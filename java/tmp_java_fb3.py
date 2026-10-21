import re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
t=open("tigshop-service/src/main/java/com/tigshop/service/user/impl/FeedbackServiceImpl.java",encoding="utf-8",errors="replace").read()
for i,l in enumerate(t.splitlines(),1):
    if "Objects.equals" in l or "getShopId()" in l and ("throw" in l):
        print(i,l.strip()[:160])
