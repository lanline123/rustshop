import json,sys,collections
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
sl={s["slice_id"]:s for s in json.load(open("docs/slices.json",encoding="utf-8"))}
for sid in ["slice-001","slice-137","slice-194","slice-051","slice-012","slice-143","slice-024","slice-111","slice-085","slice-092","slice-187","slice-188","slice-189","slice-207"]:
    s=sl[sid]
    print("\n==",sid,"tag=",s["tag"],"apis=",s["api_count"],"ctrls=",s["controllers"])
    for a in s["apis"][:6]:
        print("   ",a["method"],a["path"])
    if s["api_count"]>6: print("    ...")
