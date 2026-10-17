# -*- coding: utf-8 -*-
"""Build rust/docs/domain-map.json (v2 taxonomy) for the TigShop Java->Rust restructure.

Authoritative inputs:
  rust/docs/_migration-map.json  codemod map: 220 slices x ctx/feature/type_prefix,
                                 `real` (73 real implementations), `files`, `deleted`
  rust/docs/slices.json          220 slices: controllers / java_sources / apis / owned_files
  rust/docs/api-inventory.json   251 controller records (authoritative controller list)
  rust/docs/_impl_state.json     live extract of check_api_coverage.py (635 implemented / 929 missing)

Rules enforced (per Lead instruction):
  * the 73 `real` slices keep _migration-map.json ctx/feature/type_prefix VERBATIM;
  * every other slice is coded=false and may only use names that do not collide with
    real features, the 19 relocated baseline features, or another slice feature;
  * `coded` is written explicitly on every slice.
"""
import json, sys, os, re

sys.stdout.reconfigure(encoding='utf-8', errors='replace')
ROOT = r'D:\tigshop\java-b2b2b-pro-v5.8.57'
DOCS = os.path.join(ROOT, 'rust', 'docs')
CRATES = os.path.join(ROOT, 'rust', 'crates')

CONTEXTS = ["identity", "merchant", "wholesale", "catalog", "trade", "payment",
            "marketing", "member", "logistics", "content", "system", "im",
            "analytics", "openapi"]

# 19 baseline (pre-slice) modules being relocated as features by the codemod.
# These names are RESERVED: no slice may claim them.
RESERVED = {
    ("identity", "auth"),
    ("catalog", "product"), ("catalog", "category_all"),
    ("trade", "cart"), ("trade", "order"),
    ("payment", "paylog"), ("payment", "refund_apply"),
    ("payment", "balance_pay"), ("payment", "gateway"),
    ("marketing", "coupon"), ("marketing", "seckill"),
    ("marketing", "time_discount"), ("marketing", "salesman"),
    ("member", "user"), ("member", "member_lists"),
    ("content", "article"), ("content", "decorate"),
    ("logistics", "shipping_list"),
    ("merchant", "shop"),
}

# Collision fixes for *non-real* slices only (no file is moved by these).
# (slice_id -> (ctx, feature, type_prefix, reason))
OVERRIDES = {
    # _migration-map.json reuses `salesman` for two slices and the name is also a
    # relocated baseline feature (marketing/salesman).
    "slice-060": ("marketing", "salesman_admin", "SalesmanAdmin",
                  "renamed from marketing/salesman: baseline feature marketing/salesman is reserved; admin salesman list"),
    "slice-061": ("marketing", "salesman_client", "SalesmanClient",
                  "renamed from marketing/salesman: duplicate of slice-060 and baseline feature marketing/salesman reserved"),
    # `salesman_material` used by slice-059 and slice-167.
    "slice-059": ("marketing", "salesman_material_client", "SalesmanMaterialClient",
                  "renamed from marketing/salesman_material: duplicate of slice-167; this slice is the api/client side"),
    # `comment` used by slice-071 and slice-195.
    "slice-071": ("catalog", "comment_client", "CommentClient",
                  "renamed from catalog/comment: duplicate of slice-195; this slice is the api/client side"),
    # `decorate_discrete` used by slice-175 and slice-178.
    "slice-178": ("content", "decorate_discrete_client", "DecorateDiscreteClient",
                  "renamed from content/decorate_discrete: duplicate of slice-175; this slice is the api/client side"),
    # reserved baseline names
    "slice-035": ("marketing", "coupon_manage", "CouponManage",
                  "renamed from marketing/coupon: baseline feature marketing/coupon is reserved"),
    "slice-177": ("content", "decorate_manage", "DecorateManage",
                  "renamed from content/decorate: baseline feature content/decorate is reserved"),
    "slice-203": ("payment", "refund_apply_admin", "RefundApplyAdmin",
                  "renamed from payment/refund_apply: baseline feature payment/refund_apply is reserved"),
}

# Multi-controller slices: extra features beyond the slice's primary feature.
# The codemod moves the whole file to the primary feature; workers split it manually.
SPLITS = {
    "slice-001": [
        ("catalog", "product_batch", "ProductBatch", ["ProductBatchController"]),
        ("catalog", "product_search", "ProductSearch", ["SearchController", "SearchGuessController"]),
    ],
    "slice-112": [
        ("content", "wechat_live", "WechatLive", ["WechatLiveController"]),
    ],
    "slice-137": [
        ("openapi", "message_push", "MessagePush", ["MessagePushController"]),
        ("openapi", "workbuddy_product_import", "WorkBuddyProductImport", ["WorkBuddyProductImportController"]),
        ("openapi", "workbuddy_product_intelligence", "WorkBuddyProductIntelligence", ["WorkBuddyProductIntelligenceController"]),
    ],
    "slice-158": [
        ("marketing", "gift_card_client", "GiftCardClient", ["GiftCardGroupsController"]),
    ],
    "slice-194": [
        ("analytics", "statistics_user", "StatisticsUser", ["StatisticsUserController"]),
    ],
}

# Controllers not covered by any slice: relocated baseline features, security
# filters, @RestControllerAdvice classes, and the 5 pre-slice controllers.
EXTRA_CONTROLLERS = {
    # Spring Security login filters -> baseline identity/auth
    ("LoginFilter", "adminapi", "filter"): ("identity", "auth"),
    ("ClientLoginFilter", "api", "filter"): ("identity", "auth"),
    # pre-slice implemented controllers -> relocated baseline feature they live in
    ("CouponClientController", "api", "promotion"): ("marketing", "coupon"),
    ("UserCouponController", "api", "user"): ("marketing", "coupon"),
    ("ArticleCategoryClientController", "api", "article"): ("content", "article"),
    ("PointsLogController", "api", "user"): ("member", "member_lists"),
    ("RegisterController", "api", "user"): ("identity", "auth"),
    ("ProfitSharingNotifyController", "api", "finance"): ("payment", "gateway"),
    # advice classes (0 endpoints) -> owning feature
    ("WorkBuddyApiExceptionHandler", "adminapi", "openapi"): ("openapi", "workbuddy"),
    ("SeoResponseAdvice", "api", "handler"): ("content", "seo"),
    ("GlobalExceptionHandler", "common", "handler"): ("system", "error_handling"),
    ("GlobalResponseAdvice", "common", "handler"): ("system", "error_handling"),
}
EXTRA_FEATURES = {("system", "error_handling")}

LAYER_TARGET = {
    "models": "crates/core/src/models/%s/%s.rs",
    "repo": "crates/infrastructure/src/db/repo/%s/%s.rs",
    "service": "crates/services/src/%s/%s.rs",
    "admin_route": "crates/api/admin/src/routes/%s/%s.rs",
    "public_route": "crates/api/public/src/routes/%s/%s.rs",
}


def load(p):
    with open(os.path.join(DOCS, p), encoding='utf-8') as f:
        return json.load(f)


mm = load('_migration-map.json')
slices_json = load('slices.json')
inventory = load('api-inventory.json')
impl_state = load('_impl_state.json')
mm_by_id = {s['slice_id']: s for s in mm['slices']}
sj_by_id = {s['slice_id']: s for s in slices_json}
real_ids = {s['slice_id'] for s in mm['slices'] if s.get('real') is True}

assert len(mm_by_id) == 220 and len(sj_by_id) == 220


def norm_module(seg):
    seg = seg.lower()
    return {'tigshop-adminapi': 'adminapi', 'tigshop-api': 'api',
            'tigshop-common': 'common', 'tigshop-security': 'security'}.get(seg, seg)


def triple_from_source(src):
    path = src.split(':')[0].replace('/', '\\')
    parts = path.split('\\')
    mod = norm_module(parts[0])
    if mod == 'security':
        mod = 'adminapi' if 'tigshop-security-admin' in path else 'api'
    m = re.search(r"\\(controller|handler|filter)\\(.+)$", path)
    pkg = ''
    if m:
        sub = m.group(2).split('\\')[:-1]
        pkg = sub[0] if sub else m.group(1)
    return (os.path.basename(path)[:-5], mod, pkg)


PASCAL_RE = re.compile(r"^[A-Z][A-Za-z0-9]*$")

# ---------------------------------------------------------------------------
# 1. slices[]
# ---------------------------------------------------------------------------
out_slices = []
final = {}   # slice_id -> dict(ctx, feature, type_prefix, coded, splitfeats, controllers)
for sid in sorted(mm_by_id):
    src = mm_by_id[sid]
    sj = sj_by_id[sid]
    ctx, feature, tp = src['ctx'], src['feature'], src['type_prefix']
    notes = [src.get('notes') or '']
    if sid in OVERRIDES:
        ctx, feature, tp, reason = OVERRIDES[sid]
        notes.append(reason)
    coded = sid in real_ids
    if not coded and (src.get('deleted') or (src.get('files') and not src.get('real'))):
        notes.append("coded=false: empty scaffold, deleted/dropped by the restructure codemod")
    entry = {
        "slice_id": sid,
        "ctx": ctx,
        "feature": feature,
        "type_prefix": tp,
        "coded": coded,
        "java_modules": src.get('java_modules') or sorted({triple_from_source(x)[1] for x in sj['java_sources']}),
        "controllers": list(sj['controllers']),
    }
    splitfeats = []
    if sid in SPLITS:
        split = []
        for sctx, sf, stp, ctls in SPLITS[sid]:
            split.append({"ctx": sctx, "feature": sf, "type_prefix": stp, "controllers": ctls})
            splitfeats.append((sctx, sf, stp, ctls))
        entry["split"] = split
        notes.append("multi-controller slice: split manually into " +
                     ", ".join("%s/%s" % (x[0], x[1]) for x in splitfeats))
    note_txt = '; '.join(n for n in notes if n)
    if note_txt:
        entry["notes"] = note_txt
    out_slices.append(entry)
    final[sid] = {"ctx": ctx, "feature": feature, "tp": tp, "coded": coded,
                  "split": splitfeats, "controllers": list(sj['controllers'])}

# ---------------------------------------------------------------------------
# 2. endpoint -> (ctx, feature)
# ---------------------------------------------------------------------------
ep_feature = {}
for sid in final:
    meta = final[sid]
    split_map = {}
    for _sctx, sf, _stp, ctls in meta['split']:
        for c in ctls:
            split_map[c] = sf
    for a in sj_by_id[sid]['apis']:
        mod = norm_module(a['source'].split(':')[0].replace('/', '\\').split('\\')[0])
        key = (mod, a['method'].upper(), a['path'])
        if key in ep_feature:
            raise SystemExit("duplicate endpoint %r" % (key,))
        ep_feature[key] = (sid, meta['ctx'], split_map.get(a['controller'], meta['feature']))

# ---------------------------------------------------------------------------
# 3. controllers[] (251 unique controller+module+package)
# ---------------------------------------------------------------------------
triple_slice = {}
for sid in final:
    for js in sj_by_id[sid]['java_sources']:
        triple_slice.setdefault(triple_from_source(js), sid)

controllers_out = []
ctrl_meta = {}
for c in inventory['controllers']:
    tri = triple_from_source(c['source_file'])
    cls, mod, pkg = tri
    if tri in triple_slice:
        sid = triple_slice[tri]
        meta = final[sid]
        split_map = {}
        for _sctx, sf, _stp, ctls in meta['split']:
            for cc in ctls:
                split_map[cc] = sf
        ctx = meta['ctx']
        feat = split_map.get(cls, meta['feature'])
    elif tri in EXTRA_CONTROLLERS:
        ctx, feat = EXTRA_CONTROLLERS[tri]
    else:
        raise SystemExit("UNMAPPED CONTROLLER: %r" % (tri,))
    controllers_out.append({"controller": cls, "module": mod, "package": pkg,
                            "ctx": ctx, "feature": feat})
    ctrl_meta[tri] = {"source_file": c['source_file'].replace('/', '\\'),
                      "endpoint_count": len(c['endpoints'])}

# ---------------------------------------------------------------------------
# 4. write domain-map.json
# ---------------------------------------------------------------------------
domain_map = {"version": "5.8.57", "contexts": CONTEXTS,
              "slices": out_slices, "controllers": controllers_out}
with open(os.path.join(DOCS, 'domain-map.json'), 'w', encoding='utf-8', newline='\n') as f:
    json.dump(domain_map, f, ensure_ascii=False, indent=2)
    f.write('\n')
print('domain-map.json written: %d slices, %d controllers' % (len(out_slices), len(controllers_out)))

# ---------------------------------------------------------------------------
# 5. validation
# ---------------------------------------------------------------------------
errors, warns = [], []
ctx_set = set(CONTEXTS)
# 5.1 slice universe / real
if len(out_slices) != 220:
    errors.append("slices %d != 220" % len(out_slices))
if {s['slice_id'] for s in out_slices} != set(mm_by_id):
    errors.append("slice ids do not match _migration-map.json")
coded_n = sum(1 for s in out_slices if s['coded'])
if coded_n != 73:
    errors.append("coded %d != 73 real" % coded_n)
if {s['slice_id'] for s in out_slices if s['coded']} != real_ids:
    errors.append("coded set != real set")
# 5.2 real slices must match _migration-map.json verbatim
diff = []
for s in out_slices:
    if not s['coded']:
        continue
    m = mm_by_id[s['slice_id']]
    for field in ('ctx', 'feature', 'type_prefix'):
        if s[field] != m[field]:
            diff.append((s['slice_id'], field, m[field], s[field]))
    if s['slice_id'] in OVERRIDES:
        errors.append("OVERRIDE touched real slice %s" % s['slice_id'])
print("real slices ctx/feature/type_prefix diff vs _migration-map.json: %d" % len(diff))
for d in diff:
    errors.append("real slice mismatch: %r" % (d,))
# 5.3 feature slots: unique, ctx valid, not reserved
slot = {}
all_feats = {}
for s in out_slices:
    feats = [(s['ctx'], s['feature'], s['type_prefix'])] + \
            [(f[0], f[1], f[2]) for f in final[s['slice_id']]['split']]
    for ctx, feat, tp in feats:
        key = (ctx, feat)
        if ctx not in ctx_set:
            errors.append("unknown ctx %r (%s)" % (ctx, s['slice_id']))
        if key in slot:
            errors.append("ctx::feature collision %r (%s vs %s)" % (key, slot[key], s['slice_id']))
        slot[key] = s['slice_id']
        if key in RESERVED:
            errors.append("slice %s claimed RESERVED baseline feature %r" % (s['slice_id'], key))
        if not PASCAL_RE.match(tp):
            errors.append("bad type_prefix %r for %r" % (tp, key))
        if tp in all_feats:
            warns.append("type_prefix %r reused by %r and %r" % (tp, all_feats[tp], key))
        all_feats[tp] = key
# 5.4 controllers
if len(controllers_out) != 251:
    errors.append("controllers %d != 251" % len(controllers_out))
tri_keys = [(c['controller'], c['module'], c['package']) for c in controllers_out]
if len(set(tri_keys)) != 251:
    errors.append("controller triples not unique")
inv_tri = {triple_from_source(c['source_file']) for c in inventory['controllers']}
if set(tri_keys) != inv_tri:
    errors.append("controller coverage mismatch missing=%r extra=%r" %
                  (sorted(inv_tri - set(tri_keys)), sorted(set(tri_keys) - inv_tri)))
allowed_extra = RESERVED | EXTRA_FEATURES
for c in controllers_out:
    key = (c['ctx'], c['feature'])
    if c['ctx'] not in ctx_set:
        errors.append("controller %s unknown ctx" % c['controller'])
    if key not in slot and key not in allowed_extra:
        errors.append("controller %s -> %r is neither a slice slot nor a reserved/extra feature" % (c['controller'], key))
# every slice controller represented
for s in out_slices:
    for ctl in s['controllers']:
        if not any(c['controller'] == ctl for c in controllers_out):
            errors.append("slice %s controller %s missing from controllers[]" % (s['slice_id'], ctl))
# 5.5 endpoints
ep_total = sum(len(sj_by_id[sid]['apis']) for sid in final)
if ep_total != 1483:
    errors.append("slice endpoints %d != 1483" % ep_total)
if len(ep_feature) != ep_total:
    errors.append("endpoint map size %d != %d" % (len(ep_feature), ep_total))
missing_keys = {(e['module'], e['method'].upper(), e['path']) for e in impl_state['missing']}
unmapped = [e for e in impl_state['missing']
            if (e['module'], e['method'].upper(), e['path']) not in ep_feature]
if unmapped:
    errors.append("%d missing endpoints unmapped" % len(unmapped))
# 5.6 real slices' target files must already exist on disk (codemod ran)
real_missing = []
for sid in real_ids:
    m = mm_by_id[sid]
    files = m.get('files') or {}
    for layer, rel in files.items():
        tpl = LAYER_TARGET.get(layer)
        if not tpl:
            continue
        target = tpl % (m['ctx'], m['feature'])
        if not os.path.isfile(os.path.join(ROOT, 'rust', target)):
            real_missing.append((sid, target))
if real_missing:
    errors.append("%d real slice target files missing on disk (e.g. %r)" % (len(real_missing), real_missing[:3]))

print('coded (real) slices : %d' % coded_n)
print('non-coded slices    : %d (59 deleted scaffolds + 88 never started)' % (len(out_slices) - coded_n))
print('ctx::feature slots  : %d (unique, collisions=%d)' %
      (len(slot), len(out_slices) + sum(len(final[x]['split']) for x in final) - len(slot)))
print('type_prefix reused  : %d' % len(warns))
print('controllers         : %d unique-%d' % (len(controllers_out), len(set(tri_keys))))
print('missing endpoints   : %d mapped=%d' % (len(impl_state['missing']), len(impl_state['missing']) - len(unmapped)))
print('real target files   : %d missing' % len(real_missing))
print('errors              : %d' % len(errors))
for e in errors:
    print('  ERROR:', e)
for w in warns:
    print('  WARN :', w)

# save state for roadmap builder
from collections import Counter, defaultdict
ctx_stats = defaultdict(lambda: {'slices': 0, 'real': 0, 'scaffold': 0, 'never': 0,
                                 'features': 0, 'remaining': 0})
for sid in final:
    meta = final[sid]
    mm_s = mm_by_id[sid]
    st = ctx_stats[meta['ctx']]
    st['slices'] += 1
    if meta['coded']:
        st['real'] += 1
    elif mm_s.get('deleted'):
        st['scaffold'] += 1
    else:
        st['never'] += 1
    st['features'] += 1 + len(meta['split'])
state = {
    'final': {sid: {'ctx': v['ctx'], 'feature': v['feature'], 'coded': v['coded'],
                    'split': [list(x) for x in v['split']],
                    'controllers': v['controllers']} for sid, v in final.items()},
    'ep_feature': {'%s %s %s' % k: v for k, v in ep_feature.items()},
    'missing_eps': impl_state['missing'],
    'ctrl_meta': {'%s|%s|%s' % k: v for k, v in ctrl_meta.items()},
    'mm': {sid: {'real': mm_by_id[sid].get('real'),
                 'deleted': mm_by_id[sid].get('deleted'),
                 'notes': mm_by_id[sid].get('notes') or '',
                 'files': mm_by_id[sid].get('files') or {},
                 'api_count': mm_by_id[sid]['api_count'],
                 'tag': mm_by_id[sid].get('tag') or ''} for sid in sorted(final)},
    'reserved': sorted(list(RESERVED)),
}
with open(os.path.join(DOCS, '_domain_state.json'), 'w', encoding='utf-8') as f:
    json.dump(state, f, ensure_ascii=False)

print('\nctx distribution:')
print('  %-10s slices real scaffold never features' % '')
for ctx in CONTEXTS:
    st = ctx_stats[ctx]
    print('  %-10s %6d %4d %8d %5d %8d' % (ctx, st['slices'], st['real'], st['scaffold'],
                                          st['never'], st['features']))
if errors:
    sys.exit(1)
print('\nOK')
