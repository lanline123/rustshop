import json, sys, os, re
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

ROOT = r'D:\tigshop\java-b2b2b-pro-v5.8.57'
with open(os.path.join(ROOT, 'rust/docs/slices.json'), encoding='utf-8') as f:
    slices = json.load(f)

crates = os.path.join(ROOT, 'rust', 'crates')
disk = {}
for dirpath, dirnames, filenames in os.walk(crates):
    for fn in filenames:
        m = re.fullmatch(r'slice(\d{3})\.rs', fn)
        if m:
            disk.setdefault('slice-' + m.group(1), []).append(os.path.relpath(os.path.join(dirpath, fn), os.path.join(ROOT, 'rust')))

byslice = {s['slice_id']: s for s in slices}
print('disk slices:', len(disk), 'missing from slices.json:', set(disk) - set(byslice))
# slices with disk files but none in owned_files
for sid, files in sorted(disk.items()):
    s = byslice.get(sid)
    if not s:
        print('EXTRA on disk:', sid, files)
        continue
    owned = set(s['owned_files'])
    not_owned = [f for f in files if f not in owned]
    if not s['_coded'] if '_coded' in s else False:
        pass
    has_owned = any(os.path.exists(os.path.join(ROOT, 'rust', f)) for f in s['owned_files'])
    if not has_owned:
        print('DISK-BUT-NOT-OWNED:', sid, files, '| owned:', s['owned_files'])
# slices owned_files existing count
coded = sum(1 for s in slices if any(os.path.exists(os.path.join(ROOT,'rust',f)) for f in s['owned_files']))
print('coded by owned_files:', coded)
