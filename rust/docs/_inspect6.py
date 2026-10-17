import json, sys, os, re
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

with open(r'rust/docs/api-contract.json', encoding='utf-8') as f:
    ac = json.load(f)
eps = ac['endpoints']

def parse(src):
    p = src.split(':')[0].replace('/', '\\')
    parts = p.split('\\')
    # module = first segment (tigshop-adminapi etc)
    mod = parts[0]
    m = re.search(r'controller\\([^\\]+)\\', p)
    return mod, (m.group(1) if m else '')

cj = {}
for e in eps:
    mod, pkg = parse(e['source'])
    if mod.startswith('tigshop-security'):
        mod = e['module']
    else:
        mod = e['module']
    cj.setdefault(mod, set()).add(e['controller'])

print('contract names per module:', {k: len(v) for k, v in cj.items()})

jv = {}
for mod_dir, mod in [('tigshop-adminapi','adminapi'), ('tigshop-api','api')]:
    root = os.path.join('java', mod_dir, 'src', 'main', 'java', 'com', 'tigshop', mod, 'controller')
    s = set()
    for dirpath, dirnames, filenames in os.walk(root):
        for fn in filenames:
            if fn.endswith('Controller.java'):
                s.add(fn[:-5])
    jv[mod] = s
print('java names per module:', {k: len(v) for k, v in jv.items()})

for mod in ['adminapi','api']:
    print(mod, 'contract-not-java:', sorted(cj.get(mod,set()) - jv[mod]))
    print(mod, 'java-not-contract:', sorted(jv[mod] - cj.get(mod,set())))

# find ProfitSharingNotifyController
for dirpath, dirnames, filenames in os.walk('java'):
    for fn in filenames:
        if fn == 'ProfitSharingNotifyController.java':
            print('FOUND', os.path.join(dirpath, fn))
