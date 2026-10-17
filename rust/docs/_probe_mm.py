import json, sys, os
sys.stdout.reconfigure(encoding='utf-8', errors='replace')
ROOT = r'D:\tigshop\java-b2b2b-pro-v5.8.57'
DOCS = os.path.join(ROOT, 'rust', 'docs')
mm = json.load(open(os.path.join(DOCS, '_migration-map.json'), encoding='utf-8'))
slices = mm['slices']

from collections import Counter, defaultdict
print('version:', mm['version'], 'contexts:', mm['contexts'])
print('real:', sum(1 for s in slices if s.get('real') is True))
print('scaffold(real False):', sum(1 for s in slices if s.get('real') is False))
print('never(real None):', sum(1 for s in slices if s.get('real') is None))
print('deleted flags:', Counter(s.get('deleted') for s in slices))
print()
print('=== real slices ===')
for s in slices:
    if s.get('real'):
        print('%-10s %-10s %-32s %-24s api=%d deleted=%s' % (s['slice_id'], s['ctx'], s['feature'], s['type_prefix'], s['api_count'], s.get('deleted')))
print()
print('=== non-real slices (ctx/feature from migration map) ===')
for s in slices:
    if not s.get('real'):
        print('%-10s %-10s %-32s %-22s api=%3d real=%-5s files=%s' % (s['slice_id'], s['ctx'], s['feature'], s['type_prefix'], s['api_count'], s.get('real'), bool(s.get('files'))))
