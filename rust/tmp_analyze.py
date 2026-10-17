import json,os,sys,collections
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
sl=json.load(open("docs/slices.json",encoding="utf-8"))
def paths(k):
    return ["crates/core/src/models/%s.rs"%k,
            "crates/infrastructure/src/db/repo/%s.rs"%k,
            "crates/services/src/%s.rs"%k,
            "crates/api/admin/src/routes/%s.rs"%k,
            "crates/api/public/src/routes/%s.rs"%k,
            "crates/api/admin/src/root/%s.rs"%k,
            "crates/api/public/src/root/%s.rs"%k]
have=[]
for s in sl:
    k=s["slice_id"].replace("-","")
    ex=[p for p in paths(k) if os.path.isfile(p)]
    if ex: have.append((s,ex))
print("slices with any file:",len(have))
mult=collections.Counter(len(s["controllers"]) for s,_ in have)
print("controllers per slice:",dict(sorted(mult.items())))
print("apis total for coded slices:",sum(s["api_count"] for s,_ in have))
# single controller slices
one=[s for s,_ in have if len(s["controllers"])==1]
print("single-controller coded slices:",len(one))
big=sorted(have,key=lambda x:-x[0]["api_count"])[:15]
print("\nlargest coded slices:")
for s,ex in big:
    print("  %s apis=%2d ctrls=%2d %s"%(s["slice_id"],s["api_count"],len(s["controllers"]),",".join(c.replace("Controller","") for c in s["controllers"])[:70]))
# files present breakdown
cnt=collections.Counter()
for s,ex in have:
    for p in ex: cnt[p.split("/")[2]+"/"+p.split("/")[3]]+=1
print("\nfile locations:",dict(cnt))
# empty scaffolds?
skel=0
for s,_ in have:
    k=s["slice_id"].replace("-","")
    p="crates/services/src/%s.rs"%k
    if os.path.isfile(p):
        t=open(p,encoding="utf-8",errors="replace").read()
        if "骨架" in t and "async fn" not in t: skel+=1
print("empty scaffolds among coded:",skel)
