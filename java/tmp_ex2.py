import os,re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
p="tigshop-service/src/main/java/com/tigshop/service/promotion/impl/PointsExchangeServiceImpl.java"
t=open(p,encoding="utf-8",errors="replace").read().splitlines()
print("\n".join("%d %s"%(i+1,l) for i,l in enumerate(t[60:110],60)))
