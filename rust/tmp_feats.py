import json,os,sys,collections,re
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
dm=json.load(open("docs/domain-map.json",encoding="utf-8"))
# features from real slices + baseline
feats=[]
for s in dm["slices"]:
    if s.get("real"): feats.append((s["ctx"],s["feature"],"slice"))
for b in dm.get("baseline",[]): feats.append((b["ctx"],b["feature"],"baseline"))
c=collections.Counter(f for _,f,_ in feats)
print("total features:",len(feats),"dup feature names across ctx:",[(f,n) for f,n in c.items() if n>1])
# service types
types={}
for ctx,f,_ in feats:
    p="crates/services/src/%s/%s.rs"%(ctx,f)
    if not os.path.isfile(p): continue
    t=open(p,encoding="utf-8",errors="replace").read()
    ms=re.findall(r"pub struct (\w+Service)",t)
    types[(ctx,f)]=ms[0] if ms else None
print("features without *Service struct:",[k for k,v in types.items() if not v])
print("sample:",list(types.items())[:5])
