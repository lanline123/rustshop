# -*- coding: utf-8 -*-
"""Probe: compute feature-level stats for the roadmap."""
import json, sys, os
from collections import defaultdict

sys.stdout.reconfigure(encoding='utf-8', errors='replace')
ROOT = r'D:\tigshop\java-b2b2b-pro-v5.8.57'
DOCS = os.path.join(ROOT, 'rust', 'docs')


def norm_module(seg):
    seg = seg.lower()
    return {'tigshop-adminapi': 'adminapi', 'tigshop-api': 'api',
            'tigshop-common': 'common', 'tigshop-security': 'security'}.get(seg, seg)


slices_json = json.load(open(os.path.join(DOCS, 'slices.json'), encoding='utf-8'))
domain_map = json.load(open(os.path.join(DOCS, 'domain-map.json'), encoding='utf-8'))
impl_state = json.load(open(os.path.join(DOCS, '_impl_state.json'), encoding='utf-8'))
missing_keys = {(e['module'], e['method'].upper(), e['path']) for e in impl_state['missing']}

dm = {s['slice_id']: s for s in domain_map['slices']}
ctrl_src = {}
for c in domain_map['controllers']:
    # source file recovered from api-inventory below
    pass
inventory = json.load(open(os.path.join(DOCS, 'api-inventory.json'), encoding='utf-8'))
for c in inventory['controllers']:
    src = c['source_file'].replace('/', '\\')
    seg = src.split('\\')[0]
    mod = norm_module(seg)
    if mod == 'security':
        mod = 'adminapi' if 'security-admin' in src else 'api'
    ctrl_src[(c['class_name'], mod)] = src

feat = {}
for sid, s in dm.items():
    sj = next(x for x in slices_json if x['slice_id'] == sid)
    split_map = {}
    for x in s.get('split', []):
        for c in x['controllers']:
            split_map[c] = x['feature']
    for a in sj['apis']:
        mod = norm_module(a['source'].split(':')[0].replace('/', '\\').split('\\')[0])
        f = split_map.get(a['controller'], s['feature'])
        key = (s['ctx'], f)
        st = feat.setdefault(key, {'ctx': s['ctx'], 'feature': f, 'slice': sid,
                                   'slices': [], 'controllers': set(), 'admin': 0,
                                   'client': 0, 'total': 0, 'remaining': 0,
                                   'coded': False, 'impl_total': 0})
        if sid not in st['slices']:
            st['slices'].append(sid)
        st['coded'] = st['coded'] or s['coded']
        st['total'] += 1
        st['controllers'].add((a['controller'], mod))
        if mod == 'adminapi':
            st['admin'] += 1
        else:
            st['client'] += 1
        if (mod, a['method'].upper(), a['path']) in missing_keys:
            st['remaining'] += 1
        else:
            st['impl_total'] += 1

print('feature slots:', len(feat))
print('remaining>0:', sum(1 for s in feat.values() if s['remaining'] > 0))
print('complete:', sum(1 for s in feat.values() if s['remaining'] == 0))
print('remaining sum:', sum(s['remaining'] for s in feat.values()))
print('total sum:', sum(s['total'] for s in feat.values()))
print('admin sum:', sum(s['admin'] for s in feat.values()), 'client sum:', sum(s['client'] for s in feat.values()))
print()
print('features with remaining>0 by ctx:')
from collections import Counter
c = Counter(s['ctx'] for s in feat.values() if s['remaining'] > 0)
r = Counter()
for s in feat.values():
    r[s['ctx']] += s['remaining']
for ctx in ['identity', 'system', 'catalog', 'trade', 'payment', 'member', 'merchant', 'wholesale', 'logistics', 'marketing', 'content', 'analytics', 'im', 'openapi']:
    print('  %-10s features_remaining=%2d remaining=%3d' % (ctx, c[ctx], r[ctx]))
