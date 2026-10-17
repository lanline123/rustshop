import json, sys
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

with open(r'rust/docs/api-inventory.json', encoding='utf-8') as f:
    inv = json.load(f)
print('type:', type(inv))
if isinstance(inv, dict):
    print('keys:', list(inv.keys())[:30])
    for k, v in inv.items():
        print(' ', k, type(v), len(v) if hasattr(v, '__len__') else v)
