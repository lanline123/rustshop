import json,sys,collections
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
d=json.load(open("docs/api-contract.json",encoding="utf-8"))
e=d["endpoints"]
def info(x):
    s=x["source"].split(":")[0]; parts=s.split("\\")
    mod=s.split("\\")[0]
    if "controller" in parts:
        i=parts.index("controller"); pkg="/".join(parts[i+1:-1]) or "(root)"
    else: pkg="(noncontroller)"
    return mod,pkg,x["controller"]
c=collections.Counter(info(x) for x in e)
for (mod,pkg,ctrl),n in sorted(c.items()):
    print("%-32s %-22s %-36s %d"%(mod,pkg,ctrl,n))
