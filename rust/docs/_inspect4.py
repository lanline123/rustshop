import json, sys, os, re
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

with open(r'rust/docs/api-contract.json', encoding='utf-8') as f:
    ac = json.load(f)
endpoints = ac['endpoints']
print('endpoint_count field:', ac.get('endpoint_count'), 'actual:', len(endpoints))
print('controller_count field:', ac.get('controller_count'))

def pkg_of(src):
    p = src.split(':')[0].replace('/', '\\')
    m = re.search(r'controller\\([^\\]+)\\', p)
    return m.group(1) if m else ''

# empty controller or empty pkg
for e in endpoints:
    if not e['controller'] or not pkg_of(e['source']):
        print('ODD:', e['module'], e['controller'], '|', e['source'])

# unique (controller,module) pairs
pairs = set((e['controller'], e['module']) for e in endpoints)
print('unique (controller,module):', len(pairs))

# controllers per module
from collections import Counter
print(Counter(m for c, m in pairs))

# scan Java source tree
roots = [r'java\tigshop-adminapi\src\main\java\com\tigshop\adminapi\controller',
         r'java\tigshop-api\src\main\java\com\tigshop\api\controller']
total = 0
found = []
for root in roots:
    for dirpath, dirnames, filenames in os.walk(root):
        for fn in filenames:
            if fn.endswith('Controller.java'):
                total += 1
                found.append(os.path.join(dirpath, fn))
print('java *Controller.java files:', total)
# names
jnames = set(os.path.basename(p)[:-5] for p in found)
print('unique java controller names:', len(jnames))
cn = set(e['controller'] for e in endpoints)
print('in contract not in java:', sorted(cn - jnames))
print('in java not in contract:', sorted(jnames - cn))
