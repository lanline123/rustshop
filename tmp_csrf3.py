import os,re,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
roots=["view","public","docs","01-系统概述与环境搭建.md","05-API 接口文档大全.md"]
hits=[]
for r in roots:
    if os.path.isfile(r):
        t=open(r,encoding="utf-8",errors="replace").read()
        if "csrf" in t.lower(): hits.append(r)
        continue
    for dp,_,fs in os.walk(r):
        for f in fs:
            if f.endswith((".js",".vue",".php",".md",".ts")):
                p=os.path.join(dp,f)
                try: t=open(p,encoding="utf-8",errors="replace").read()
                except: continue
                if "csrf" in t.lower(): hits.append(p)
print(len(hits))
for h in hits[:10]: print(h)
