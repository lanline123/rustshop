import json,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
sl={s["slice_id"]:s for s in json.load(open("docs/slices.json",encoding="utf-8"))}
for sid in ["slice-135","slice-136","slice-200","slice-217","slice-216","slice-026","slice-203","slice-014","slice-002","slice-012"]:
    s=sl[sid]; print("\n==",sid,s["tag"],s["controllers"])
    for a in s["apis"]: print("   ",a["method"],a["path"])
