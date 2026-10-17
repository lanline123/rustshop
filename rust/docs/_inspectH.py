import json, sys, os, re
sys.stdout.reconfigure(encoding='utf-8', errors='replace')
ROOT = r'D:\tigshop\java-b2b2b-pro-v5.8.57'
with open(os.path.join(ROOT, r'rust/docs/slices.json'), encoding='utf-8') as f:
    slices = json.load(f)
with open(os.path.join(ROOT, r'rust/docs/api-contract.json'), encoding='utf-8') as f:
    ac = json.load(f)
with open(os.path.join(ROOT, r'rust/docs/_impl_state.json'), encoding='utf-8') as f:
    state = json.load(f)
eps = ac['endpoints']
impl = {(e['method'].upper(), e['path']) for e in state['implemented']}
sl_keys = set()
for s in slices:
    for a in s['apis']:
        sl_keys.add((a['method'].upper(), a['path']))
notin = [e for e in eps if (e['method'].upper(), e['path']) not in sl_keys]
print('not in slices:', len(notin), '| of which implemented:', sum(1 for e in notin if (e['method'].upper(), e['path']) in impl))
for e in notin:
    m = 'I' if (e['method'].upper(), e['path']) in impl else ' '
    print(' %s %-6s %-60s %-38s %s' % (m, e['method'], e['path'], e['controller'], e['module']))
