import json, sys, io
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

with open(r'rust/docs/slices.json', encoding='utf-8') as f:
    slices = json.load(f)
print('slices.json type:', type(slices))
if isinstance(slices, dict):
    print('top keys:', list(slices.keys())[:20])
    for k, v in list(slices.items())[:3]:
        print(' ', k, type(v), (list(v.keys())[:15] if isinstance(v, dict) else len(v)))
    # find list
    for k, v in slices.items():
        if isinstance(v, list):
            print('LIST KEY:', k, len(v))
            slices = v
            break

print('n slices:', len(slices))
print('first slice keys:', sorted(slices[0].keys()))
print(json.dumps(slices[0], ensure_ascii=False, indent=1)[:3000])
print('--- second ---')
print(json.dumps(slices[1], ensure_ascii=False, indent=1)[:2000])
