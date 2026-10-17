import json, sys
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

with open(r'rust/docs/api-inventory.json', encoding='utf-8') as f:
    inv = json.load(f)
ctrls = inv['controllers']
print('n controllers:', len(ctrls))
print('keys of first:', sorted(ctrls[0].keys()))
print(json.dumps({k: (v if not isinstance(v, list) else ('<%d items>' % len(v))) for k, v in ctrls[0].items()}, ensure_ascii=False, indent=1))

# all names / modules / source files
import os
names = [(c['class_name'], c['module'], c['source_file']) for c in ctrls]
print('unique class names:', len(set(n for n, m, s in names)))
print('unique (name,module):', len(set((n, m) for n, m, s in names)))
from collections import Counter
print(Counter(m for n, m, s in names))
eps_sum = sum(len(c['endpoints']) for c in ctrls)
print('total endpoints in controllers:', eps_sum)

# zero-endpoint controllers
zero = [(c['class_name'], c['module'], c['source_file']) for c in ctrls if not c['endpoints']]
print('zero-endpoint controllers:', len(zero))
for z in zero:
    print('  ', z)
