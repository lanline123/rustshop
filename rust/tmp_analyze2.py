import json,os,sys,collections
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
sl=json.load(open("docs/slices.json",encoding="utf-8"))
def exists(k):
    return any(os.path.isfile(p) for p in [
        "crates/core/src/models/%s.rs"%k,"crates/infrastructure/src/db/repo/%s.rs"%k,
        "crates/services/src/%s.rs"%k,"crates/api/admin/src/routes/%s.rs"%k,
        "crates/api/public/src/routes/%s.rs"%k,"crates/api/admin/src/root/%s.rs"%k,
        "crates/api/public/src/root/%s.rs"%k])
coded=[s for s in sl if exists(s["slice_id"].replace("-",""))]
# controller duplicates among coded
ctr=collections.defaultdict(list)
for s in coded:
    for c in s["controllers"]: ctr[c].append(s["slice_id"])
dups={c:v for c,v in ctr.items() if len(v)>1}
print("duplicate controllers among coded slices:",len(dups))
for c,v in dups.items(): print("  ",c,v)
# multi-controller coded
print("\nmulti-controller coded:")
for s in coded:
    if len(s["controllers"])>1:
        print("  ",s["slice_id"],s["api_count"],s["controllers"])
# controller source packages for coded slices
print("\ncontroller -> java package (coded):")
seen=set()
for s in coded:
    for c in s["controllers"]:
        for src in s["java_sources"]:
            if c+".java" in src:
                parts=src.split("\\")
                if "controller" in parts:
                    i=parts.index("controller"); pkg="/".join(parts[i+1:-1]) or "(root)"
                    if (c,pkg) not in seen:
                        seen.add((c,pkg))
print("unique controller-package pairs:",len(seen))
pkgs=collections.Counter(p for _,p in seen)
print("packages:",dict(sorted(pkgs.items())))
