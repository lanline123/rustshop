import json, sys, os, re
sys.stdout.reconfigure(encoding='utf-8', errors='replace')
ROOT = r'D:\tigshop\java-b2b2b-pro-v5.8.57'
with open(os.path.join(ROOT, r'rust/docs/slices.json'), encoding='utf-8') as f:
    slices = json.load(f)
with open(os.path.join(ROOT, r'rust/docs/_impl_state.json'), encoding='utf-8') as f:
    state = json.load(f)
impl = {(e['method'].upper(), e['path']) for e in state['implemented']}

from collections import defaultdict
byslice = {s['slice_id']: s for s in slices}
ctl2slices = defaultdict(list)
for s in slices:
    for c in set(s['controllers']):
        ctl2slices[c].append(s['slice_id'])
print('=== controller names in 2+ slices ===')
for c, ids in sorted(ctl2slices.items()):
    if len(ids) > 1:
        print(c, ids)
print()
print('=== slices with >1 distinct controller names ===')
for s in slices:
    cs = sorted(set(s['controllers']))
    if len(cs) > 1:
        print(s['slice_id'], cs, 'mods=', s['modules'])
print()
# endpoint detail for selected slices
sel = ['slice-001','slice-051','slice-073','slice-084','slice-085','slice-111','slice-153','slice-156','slice-160','slice-161','slice-168','slice-202','slice-101','slice-094','slice-117','slice-130','slice-132','slice-141','slice-171','slice-172','slice-206','slice-207','slice-019','slice-143','slice-187','slice-188','slice-189','slice-059','slice-065','slice-066','slice-067','slice-112','slice-158','slice-137','slice-194']
for sid in sel:
    s = byslice[sid]
    print('### %s tag=%s mods=%s ctrls=%s' % (sid, s['tag'], s['modules'], s['controllers']))
    for a in s['apis']:
        mark = 'I' if (a['method'].upper(), a['path']) in impl else ' '
        print('  %s %-6s %-58s %-6s %s' % (mark, a['method'], a['path'], a['controller'], a['summary'][:24]))
    print()
