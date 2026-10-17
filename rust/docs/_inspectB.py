import json, sys, os, re
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

ROOT = r'D:\tigshop\java-b2b2b-pro-v5.8.57'
crates = os.path.join(ROOT, 'rust', 'crates')
found = {}
for dirpath, dirnames, filenames in os.walk(crates):
    for fn in filenames:
        if re.fullmatch(r'slice\d{3}\.rs', fn):
            found.setdefault(fn, []).append(os.path.relpath(os.path.join(dirpath, fn)), )
print('distinct sliceNNN.rs files on disk:', len(found))
# list crates dirs
print('crates top:', sorted(os.listdir(crates)))
for d in sorted(os.listdir(crates)):
    p = os.path.join(crates, d)
    if os.path.isdir(p):
        print(' ', d, sorted(os.listdir(p)))
