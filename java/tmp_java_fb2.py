import re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
t=open("tigshop-service/src/main/java/com/tigshop/service/user/impl/FeedbackServiceImpl.java",encoding="utf-8",errors="replace").read()
lines=t.splitlines()
for i,l in enumerate(lines,1):
    if "shopId != null && shopId > 0" in l or "SHOP_FEEDBACK" in l or "shopId == null || shopId <= 0" in l:
        print("---- line",i)
        print("\n".join(lines[max(0,i-12):i+8]))
