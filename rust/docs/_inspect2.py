import json, sys, os
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

with open(r'rust/docs/api-contract.json', encoding='utf-8') as f:
    ac = json.load(f)
print('api-contract type:', type(ac))
if isinstance(ac, dict):
    print('top keys:', list(ac.keys())[:20])
    for k, v in ac.items():
        print(' ', k, type(v), (len(v) if isinstance(v, (list, dict)) else v))
    # find the endpoint list
    listkey = None
    for k, v in ac.items():
        if isinstance(v, list) and v and isinstance(v[0], dict):
            listkey = k
    print('listkey=', listkey)
    endpoints = ac[listkey] if listkey else None
else:
    endpoints = ac
print('n endpoints:', len(endpoints))
print('endpoint keys:', sorted(endpoints[0].keys()))
print(json.dumps(endpoints[0], ensure_ascii=False, indent=1))

# unique controllers
uniq = {}
for e in endpoints:
    uniq.setdefault((e.get('controller'), e.get('module'), e.get('package')), 0)
    uniq[(e.get('controller'), e.get('module'), e.get('package'))] += 1
print('unique (controller,module,package):', len(uniq))
mods = {}
for (c, m, p), n in uniq.items():
    mods[m] = mods.get(m, 0) + 1
print('modules:', mods)
# how many unique controller names
names = set(c for c, m, p in uniq)
print('unique controller names:', len(names))
