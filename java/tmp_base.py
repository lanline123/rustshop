import os,re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
for dp,_,fs in os.walk("."):
    for f in fs:
        if f=="BaseServiceImpl.java":
            p=os.path.join(dp,f); t=open(p,encoding="utf-8",errors="replace").read()
            i=t.find("buildSortOrder")
            while i>=0:
                seg=t[i:i+900]
                if "void buildSortOrder" in seg or "buildSortOrder(" in seg:
                    print("==",p)
                    print(seg[:900]); print()
                    break
                i=t.find("buildSortOrder",i+1)
