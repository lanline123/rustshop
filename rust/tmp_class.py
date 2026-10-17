import json,os,sys,re,collections
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
sl=json.load(open("docs/slices.json",encoding="utf-8"))
def files(key):
    c={"models":f"crates/core/src/models/{key}.rs","repo":f"crates/infrastructure/src/db/repo/{key}.rs",
       "service":f"crates/services/src/{key}.rs","admin_route":f"crates/api/admin/src/routes/{key}.rs",
       "public_route":f"crates/api/public/src/routes/{key}.rs","admin_root":f"crates/api/admin/src/root/{key}.rs",
       "public_root":f"crates/api/public/src/root/{key}.rs"}
    return {k:v for k,v in c.items() if os.path.isfile(v)}
coded=[]; 
for s in sl:
    k=s["slice_id"].replace("-","")
    f=files(k)
    if f: coded.append((s["slice_id"],k,f))
print("coded:",len(coded))
e=collections.Counter()
for sid,k,f in coded:
    has_route = any(x in f for x in ("admin_route","public_route","admin_root","public_root"))
    svc_async = False; skel=False
    if "service" in f:
        t=open(f["service"],encoding="utf-8",errors="replace").read()
        svc_async="async fn" in t; skel=("骨架" in t and not svc_async)
    empty_route=True
    for x in ("admin_route","public_route","admin_root","public_root"):
        if x in f:
            t=open(f[x],encoding="utf-8",errors="replace").read()
            if ".route(" in t: empty_route=False
    e[(bool(has_route), svc_async, skel, empty_route, "service" in f)]+=1
print("classification:",dict(e))
print("\nno route file but service async:")
for sid,k,f in coded:
    has_route=any(x in f for x in ("admin_route","public_route","admin_root","public_root"))
    if "service" in f and not has_route:
        t=open(f["service"],encoding="utf-8",errors="replace").read()
        if "async fn" in t: print("  ",sid,k,list(f.keys()))
print("\nmodels-only:")
for sid,k,f in coded:
    if list(f.keys())==["models"]: print("  ",sid,k)
