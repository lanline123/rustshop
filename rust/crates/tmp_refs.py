import re,os
t=open("api/admin/src/routes/content/decorate.rs",encoding="utf-8",errors="replace").read()
print("pcNavigation routes in decorate:",len(re.findall(r'\.route\(\s*"/decorate/pcNavigation',t)))
# references to pc_cat_floor / pc_navigation modules outside their own files
for name in ("pc_cat_floor","pc_navigation"):
    refs=[]
    for dp,_,fs in os.walk("."):
        if "target" in dp.split(os.sep): continue
        for f in fs:
            if not f.endswith(".rs"): continue
            p=os.path.join(dp,f)
            if name in p: continue
            tx=open(p,encoding="utf-8",errors="replace").read()
            if re.search(r"\b%s\b"%name,tx): refs.append(p)
    print(name,"external refs:",refs[:8])
