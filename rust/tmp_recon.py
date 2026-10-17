import json,sys,collections
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
dm=json.load(open("docs/domain-map.json",encoding="utf-8"))
mm=json.load(open("docs/_migration-map.json",encoding="utf-8"))
dmmap={s["slice_id"]:s for s in dm["slices"]}
mmm={s["slice_id"]:s for s in mm["slices"]}
mismatch=[]; missing=[]
for sid,s in mmm.items():
    if not s.get("real"): continue
    t=dmmap.get(sid)
    if not t: missing.append(sid); continue
    if (t["ctx"],t["feature"])!=(s["ctx"],s["feature"]): mismatch.append((sid,t["ctx"],t["feature"],s["ctx"],s["feature"]))
print("real slices:",sum(1 for s in mm["slices"] if s.get("real")))
print("mismatch:",len(mismatch),"missing:",missing)
for m in mismatch[:10]: print("  ",m)
print("domain-map coded count:",sum(1 for s in dm["slices"] if s.get("coded")))
# collisions within ctx for coded
c=collections.Counter((s["ctx"],s["feature"]) for s in dm["slices"] if s.get("coded"))
print("collisions:",[k for k,v in c.items() if v>1])
# baseline presence
print("has baseline key:", "baseline" in dm)
