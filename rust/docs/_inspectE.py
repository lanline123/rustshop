import json, sys, os, re
sys.stdout.reconfigure(encoding='utf-8', errors='replace')
ROOT = r'D:\tigshop\java-b2b2b-pro-v5.8.57'

with open(os.path.join(ROOT, r'rust/docs/slices.json'), encoding='utf-8') as f:
    slices = json.load(f)
with open(os.path.join(ROOT, r'rust/docs/_impl_state.json'), encoding='utf-8') as f:
    state = json.load(f)

impl = {(e['method'].upper(), e['path']) for e in state['implemented']}

def pkg_of(src):
    p = src.split(':')[0].replace('/', '\\')
    parts = p.split('\\')
    mod = parts[0]
    m = re.search(r'controller\\([^\\]+)\\', p)
    return mod, (m.group(1) if m else '')

# disk slice files
crates = os.path.join(ROOT, 'rust', 'crates')
disk = set()
for dirpath, dirnames, filenames in os.walk(crates):
    for fn in filenames:
        m = re.fullmatch(r'slice(\d{3})\.rs', fn)
        if m:
            disk.add('slice-' + m.group(1))

lines = []
for s in slices:
    sid = s['slice_id']
    pkgs = sorted(set(pkg_of(x)[1] for x in s['java_sources']))
    mods = sorted(set(pkg_of(x)[0] for x in s['java_sources']))
    coded = sid in disk
    nimpl = sum(1 for a in s['apis'] if (a['method'].upper(), a['path']) in impl)
    lines.append('%s %-4s %-3s coded=%d impl=%2d/%2d mods=%-14s pkgs=%-22s tag=%s ctrls=%s' % (
        sid, s['tier'], ('Y' if coded else 'n'), 1 if coded else 0, nimpl, s['api_count'],
        ','.join(m.replace('tigshop-','') for m in mods), ','.join(pkgs), s['tag'], ','.join(s['controllers'])))
print('\n'.join(lines))
print()
print('slices endpoints total', sum(s['api_count'] for s in slices))
print('impl endpoints total', sum(1 for s in slices for a in s['apis'] if (a['method'].upper(), a['path']) in impl))
print('coded slices', sum(1 for s in slices if s['slice_id'] in disk))
