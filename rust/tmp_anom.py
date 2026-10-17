import json,os,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
sl=json.load(open("docs/slices.json",encoding="utf-8"))
def files(key):
    c={"models":f"crates/core/src/models/{key}.rs","repo":f"crates/infrastructure/src/db/repo/{key}.rs",
       "service":f"crates/services/src/{key}.rs","admin_route":f"crates/api/admin/src/routes/{key}.rs",
       "public_route":f"crates/api/public/src/routes/{key}.rs","admin_root":f"crates/api/admin/src/root/{key}.rs",
       "public_root":f"crates/api/public/src/root/{key}.rs"}
    return {k:v for k,v in c.items() if os.path.isfile(v)}
for s in sl:
    k=s["slice_id"].replace("-",""); f=files(k)
    if not f: continue
    svc=open(f["service"],encoding="utf-8",errors="replace").read() if "service" in f else ""
    async_="async fn" in svc; skel="骨架" in svc
    routes=[x for x in f if "route" in x or "root" in x]
    has_route=any(".route(" in open(f[x],encoding="utf-8",errors="replace").read() for x in routes)
    if (not async_) and (has_route or not skel):
        print("ANOMALY",s["slice_id"],"skel=",skel,"has_route=",has_route,"files=",list(f.keys()),"svc_bytes=",len(svc))
        print("   tag:",s["tag"],"apis:",s["api_count"],"ctrls:",s["controllers"])
print("\nempty scaffold size sample:")
n=0
for s in sl:
    k=s["slice_id"].replace("-",""); f=files(k)
    if not f or "service" not in f: continue
    svc=open(f["service"],encoding="utf-8",errors="replace").read()
    if "骨架" in svc and "async fn" not in svc:
        sizes={x:os.path.getsize(f[x]) for x in f}
        if n<5: print("  ",s["slice_id"],sizes)
        n+=1
print("scaffold count:",n)
