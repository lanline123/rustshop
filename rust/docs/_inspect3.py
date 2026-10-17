import json, sys, os, re
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

with open(r'rust/docs/api-contract.json', encoding='utf-8') as f:
    ac = json.load(f)
endpoints = ac['endpoints']

def pkg_of(src):
    p = src.split(':')[0].replace('/', '\\')
    m = re.search(r'controller\\([^\\]+)\\', p)
    return m.group(1) if m else ''

print('sample src:', endpoints[0]['source'])
print('pkg sample:', pkg_of(endpoints[0]['source']))

uniq = {}
for e in endpoints:
    pkg = pkg_of(e['source'])
    key = (e['controller'], e['module'], pkg)
    uniq.setdefault(key, []).append(e)
print('unique (controller,module,package):', len(uniq))
print('unique controller names:', len(set(c for c, m, p in uniq)))

from collections import Counter
c = Counter((m, p) for (nm, m, p) in uniq)
for k in sorted(c):
    print(k, c[k])

print()
print('--- controllers appearing 2+ times (module+pkg keys) ---')
byname = {}
for (nm, m, p) in uniq:
    byname.setdefault(nm, []).append((m, p))
for nm, lst in sorted(byname.items()):
    if len(lst) > 1:
        print(nm, lst)
