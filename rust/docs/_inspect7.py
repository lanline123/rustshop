import json, sys, os, re
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

with open(r'rust/docs/slices.json', encoding='utf-8') as f:
    slices = json.load(f)
with open(r'rust/docs/api-contract.json', encoding='utf-8') as f:
    ac = json.load(f)
eps = ac['endpoints']
with open(r'rust/docs/api-coverage.json', encoding='utf-8') as f:
    cov = json.load(f)
print('coverage keys:', list(cov.keys())[:20] if isinstance(cov, dict) else type(cov))

sl_ctrl = set()
for s in slices:
    sl_ctrl.update(s['controllers'])
ct_ctrl = set(e['controller'] for e in eps)
print('slice controllers:', len(sl_ctrl))
print('contract controllers:', len(ct_ctrl))
print('in slices not contract:', sorted(sl_ctrl - ct_ctrl))
print('in contract not slices:', sorted(ct_ctrl - sl_ctrl))

# tiers / modules
from collections import Counter
print('tiers:', Counter(s['tier'] for s in slices))
print('modules combos:', Counter(tuple(s['modules']) for s in slices))
print('api_count total:', sum(s['api_count'] for s in slices))
