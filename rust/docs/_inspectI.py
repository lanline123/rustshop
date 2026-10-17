import json, sys, os
sys.stdout.reconfigure(encoding='utf-8', errors='replace')
ROOT = r'D:\tigshop\java-b2b2b-pro-v5.8.57'
with open(os.path.join(ROOT, r'rust/docs/slices.json'), encoding='utf-8') as f:
    slices = json.load(f)
byslice = {s['slice_id']: s for s in slices}
for sid in ['slice-165','slice-185','slice-133','slice-106','slice-156','slice-168','slice-178','slice-175','slice-048','slice-083','slice-100','slice-036','slice-059','slice-060','slice-061','slice-063','slice-166','slice-167','slice-016','slice-130','slice-042','slice-040','slice-043','slice-073']:
    s = byslice[sid]
    print('### %s tag=%s mods=%s' % (sid, s['tag'], s['modules']))
    for a in s['apis']:
        print('   %-6s %-58s %-34s %s' % (a['method'], a['path'], a['controller'], a['summary'][:20]))
