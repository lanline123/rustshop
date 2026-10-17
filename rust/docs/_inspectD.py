import json, sys, os, re, importlib.util
sys.stdout.reconfigure(encoding='utf-8', errors='replace')
ROOT = r'D:\tigshop\java-b2b2b-pro-v5.8.57'
spec = importlib.util.spec_from_file_location('cov', os.path.join(ROOT, r'rust/tools/check_api_coverage.py'))
cov = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cov)
contract = cov.load_contract(os.path.join(ROOT, r'rust/docs/api-contract.json'))
routes = cov.collect_rust_routes(os.path.join(ROOT, 'rust', 'crates'))
have = {(r['method'].upper(), r['path']) for r in routes}
have_norm = {(m, p.rstrip('/') or '/') for m, p in have}
implemented, missing = [], []
for ep in contract['endpoints']:
    k = (ep['method'].upper(), ep['path'])
    if k in have or (k[0], k[1].rstrip('/') or '/') in have_norm:
        implemented.append(ep)
    else:
        missing.append(ep)
print('implemented', len(implemented), 'missing', len(missing))
json.dump({'implemented': implemented, 'missing': missing},
          open(os.path.join(ROOT, r'rust/docs/_impl_state.json'), 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
