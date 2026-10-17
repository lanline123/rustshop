import json, sys, os
sys.stdout.reconfigure(encoding='utf-8', errors='replace')
ROOT = r'D:\tigshop\java-b2b2b-pro-v5.8.57'
DOCS = os.path.join(ROOT, 'rust', 'docs')
mm = json.load(open(os.path.join(DOCS, '_migration-map.json'), encoding='utf-8'))
LAYER = {
    'models': 'crates/core/src/models/%s/%s.rs',
    'repo': 'crates/infrastructure/src/db/repo/%s/%s.rs',
    'service': 'crates/services/src/%s/%s.rs',
    'admin_route': 'crates/api/admin/src/routes/%s/%s.rs',
    'public_route': 'crates/api/public/src/routes/%s/%s.rs',
}
miss = 0
have_all = 0
real = [s for s in mm['slices'] if s.get('real')]
for s in real:
    files = s.get('files') or {}
    exp = []
    for k, tpl in LAYER.items():
        if k in files:
            exp.append(tpl % (s['ctx'], s['feature']))
    existing = [p for p in exp if os.path.isfile(os.path.join(ROOT, 'rust', p))]
    if len(existing) != len(exp):
        miss += 1
        if miss <= 15:
            print('MISSING', s['slice_id'], s['ctx'], s['feature'])
            for p in exp:
                if not os.path.isfile(os.path.join(ROOT, 'rust', p)):
                    print('   ', p)
    else:
        have_all += 1
print('real slices:', len(real), 'all target files present:', have_all, 'with missing:', miss)
