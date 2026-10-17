import json, sys, os, re
sys.stdout.reconfigure(encoding='utf-8', errors='replace')
ROOT = r'D:\tigshop\java-b2b2b-pro-v5.8.57'
with open(os.path.join(ROOT, r'rust/docs/api-inventory.json'), encoding='utf-8') as f:
    inv = json.load(f)
with open(os.path.join(ROOT, r'rust/docs/slices.json'), encoding='utf-8') as f:
    slices = json.load(f)
with open(os.path.join(ROOT, r'rust/docs/api-contract.json'), encoding='utf-8') as f:
    ac = json.load(f)
eps = ac['endpoints']

def parse_src(src):
    p = src.insert if False else src.split(':')[0].replace('/', '\\')
    parts = p.split('\\')
    mod = parts[0]
    m = re.search(r'controller\\([^\\]+)\\', p)
    pkg = m.group(1) if m else ''
    cls = os.path.basename(p)[:-5]
    return mod, pkg, cls

# java sources per slice -> triples
sl_triples = {}
for s in slices:
    for js in s['java_sources']:
        mod, pkg, cls = parse_src(js)
        sl_triples[(cls, mod, pkg)] = s['slice_id']
print('triples covered by slices.java_sources:', len(sl_triples))

# all triples from api-inventory controllers
all_tr = []
for c in inv['controllers']:
    rel = c['source_file'].replace('/', '\\')
    mod = rel.split('\\')[0]
    m = re.search(r'(?:controller|handler)\\([^\\]+)\\', rel)
    pkg = m.group(1) if m else ''
    all_tr.append(((c['class_name'], mod, pkg), c))
print('all inventory triples:', len(all_tr))
uncovered = [(t, c) for t, c in all_tr if t not in sl_triples]
print('uncovered:', len(uncovered))
for t, c in uncovered:
    print('  ', t, 'eps=', len(c['endpoints']))
print()
# triple collisions: same triple in 2+ slices?
from collections import defaultdict
rev = defaultdict(list)
for t, sid in sl_triples.items():
    rev[t].append(sid)
print('triples mapped from >1 slice:', {k: v for k, v in rev.items() if len(v) > 1})
print()
# endpoints not in any slice
sl_keys = set()
for s in slices:
    for a in s['apis']:
        sl_keys.add((a['method'].upper(), a['path']))
not_in_slices = [e for e in eps if (e['method'].upper(), e['path']) not in sl_keys]
print('endpoints not in slices:', len(not_in_slices), 'controllers:', sorted(set(e['controller'] for e in not_in_slices)))
