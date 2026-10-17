import json, sys, os, re
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

ROOT = r'D:\tigshop\java-b2b2b-pro-v5.8.57'
with open(os.path.join(ROOT, r'rust/docs/slices.json'), encoding='utf-8') as f:
    slices = json.load(f)
with open(os.path.join(ROOT, r'rust/docs/api-contract.json'), encoding='utf-8') as f:
    ac = json.load(f)
eps = ac['endpoints']

def exists(rel):
    return os.path.exists(os.path.join(ROOT, 'rust', rel))

coded, uncoded = [], []
for s in slices:
    files = [f for f in s['owned_files'] if os.path.exists(os.path.join(ROOT, 'rust', f))]
    s['_existing'] = files
    s['_coded'] = bool(files)
    (coded if s['_coded'] else uncoded).append(s)

print('slices:', len(slices), 'coded:', len(coded), 'uncoded:', len(uncoded))
print('coded api_count sum:', sum(s['api_count'] for s in coded))
print('uncoded api_count sum:', sum(s['api_count'] for s in uncoded))
print('total api_count:', sum(s['api_count'] for s in slices))

# how many coded slices have all 4 layer files
for s in coded:
    n = len(s['_existing'])
    if n != len(s['owned_files']):
        print('partial coded:', s['slice_id'], n, '/', len(s['owned_files']), s['_existing'])
