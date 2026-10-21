import os,re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
for dp,_,fs in os.walk("."):
    if "StringUtils.java" in fs:
        p=os.path.join(dp,"StringUtils.java")
        t=open(p,encoding="utf-8",errors="replace").read()
        i=t.find("maskMiddleHalf")
        print("==",p)
        print(t[i-200:i+900])
        break
