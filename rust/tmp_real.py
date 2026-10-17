import json,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
d=json.load(open("docs/_migration-map.json",encoding="utf-8"))
real=[s for s in d["slices"] if s.get("real")]
print("real:",len(real))
for s in sorted(real,key=lambda x:(x["ctx"],x["feature"])):
    print("  %-10s %-10s %-22s api=%2d files=%s"%(s["slice_id"],s["ctx"],s["feature"],s["api_count"],",".join(sorted(s["files"]))))
