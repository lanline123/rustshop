import json,sys
sys.stdout.reconfigure(encoding="utf-8",errors="replace")
sl={s["slice_id"]:s for s in json.load(open("docs/slices.json",encoding="utf-8"))}
for sid in ["slice-022","slice-024","slice-025","slice-073","slice-111","slice-091","slice-092"]:
    s=sl[sid]; print(sid, s["tag"], s["controllers"], s["api_count"])
    for a in s["apis"][:4]: print("   ",a["method"],a["path"])
