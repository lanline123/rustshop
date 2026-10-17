# -*- coding: utf-8 -*-
"""Build rust/docs/domain-roadmap.md from domain-map.json + _migration-map.json + live coverage."""
import json, sys, os, importlib.util
from collections import defaultdict

sys.stdout.reconfigure(encoding='utf-8', errors='replace')
ROOT = r'D:\tigshop\java-b2b2b-pro-v5.8.57'
DOCS = os.path.join(ROOT, 'rust', 'docs')

CTX_ORDER = ["identity", "system", "catalog", "trade", "payment", "member",
             "merchant", "wholesale", "logistics",
             "marketing", "content", "analytics", "im", "openapi"]
PRIO = {"identity": "P0", "system": "P0", "catalog": "P0",
        "trade": "P1", "payment": "P1", "member": "P1",
        "merchant": "P1", "wholesale": "P1", "logistics": "P1",
        "marketing": "P2", "content": "P2", "analytics": "P2",
        "im": "P2", "openapi": "P2"}
CTX_CN = {
    "identity": "管理员 / 角色 / 权限 / 登录 / 操作日志",
    "merchant": "店铺 / 商户 / 入驻 / 店铺员工 / 保证金 / 店铺资金",
    "wholesale": "批发商 / 等级 / 批发商品 / 批发设置",
    "catalog": "商品 / 分类 / 品牌 / 属性 / 库存 / 评价 / 搜索 / 电子卡券 / 询价",
    "trade": "购物车 / 订单 / 售后 / 退款 / 发票 / 结算 / 代客下单",
    "payment": "支付单 / 渠道 / 余额 / 充值 / 提现 / 账期 / 对账 / 分账",
    "marketing": "优惠券 / 秒杀 / 拼团 / 限时折扣 / 积分 / 礼品卡 / 抽奖 / 买家秀 / 分销",
    "member": "用户 / 会员等级 / 标签 / 实名 / 企业认证 / 站内信 / 留言 / 收藏 / 签到",
    "logistics": "运费模板 / 配送方式 / 物流公司 / 打印",
    "content": "文章 / 装修 / 素材 / 相册 / 导航 / SEO / 友链 / 首页 / 公众号 / 直播",
    "system": "设置 / 配置 / 地区 / 版本 / 模板 / 消息类型 / 文件 / CSRF / 缓存",
    "im": "客服 / 会话 / 消息 / 客服人员 / 接入规则",
    "analytics": "面板 / 统计 / 访问日志 / 销售统计",
    "openapi": "开放接口 / WorkBuddy / MCP",
}
CTX_DEPS = {
    "trade": "依赖 catalog（商品/SKU/库存）与 member（收货地址）；跨域依赖通过 `trade::Services::new(repos, &catalog, &member)` 显式传入。",
    "payment": "依赖 trade（订单/退款单）与 merchant（店铺资金）；账期/对账另需 member。",
    "marketing": "依赖 catalog（商品）、member（人群/等级）、trade（订单结算）。",
    "logistics": "依赖 trade（发货/订单）与 merchant（店铺地址）。",
    "content": "依赖 catalog（商品）用于装修/直播商品位。",
    "analytics": "只读依赖各域聚合，不反向依赖。",
    "im": "依赖 member（用户）与 merchant（店铺/客服配置）。",
    "openapi": "依赖 catalog/trade 的用例层，不直连其 repo。",
    "merchant": "依赖 payment（资金/保证金账户）与 member（店铺会员）。",
}
RESERVED_CN = {
    ("identity", "auth"): "登录/注册过滤器（Spring Security `setFilterProcessesUrl`）",
    ("catalog", "product"): "商品基线查询（`/api/product/product/detail|list` 等）",
    ("catalog", "category_all"): "全量分类树（`getAllCategory` / `/api/category/all`）",
    ("trade", "cart"): "购物车基线（`api/public/src/lib.rs` 早期路由）",
    ("trade", "order"): "订单基线（订单列表/详情/发货等早期实现）",
    ("payment", "paylog"): "交易日志基线",
    ("payment", "refund_apply"): "退款申请基线",
    ("payment", "balance_pay"): "余额支付",
    ("payment", "gateway"): "支付网关/回调（含分账回调 `ProfitSharingNotifyController`）",
    ("marketing", "coupon"): "优惠券基线（后台 + C 端领券）",
    ("marketing", "seckill"): "秒杀基线",
    ("marketing", "time_discount"): "限时折扣基线",
    ("marketing", "salesman"): "分销基线",
    ("member", "user"): "用户基线（`/api/user/user/detail` 等）",
    ("member", "member_lists"): "会员列表/积分明细基线",
    ("content", "article"): "文章基线（后台 CRUD + C 端列表，含 `ArticleCategoryClientController`）",
    ("content", "decorate"): "装修基线（`DecorateController` 18 个端点）",
    ("logistics", "shipping_list"): "配送/运费列表基线",
    ("merchant", "shop"): "店铺基线查询",
}


def norm_module(seg):
    seg = seg.lower()
    return {'tigshop-adminapi': 'adminapi', 'tigshop-api': 'api',
            'tigshop-common': 'common', 'tigshop-security': 'security'}.get(seg, seg)


state = json.load(open(os.path.join(DOCS, '_domain_state.json'), encoding='utf-8'))
dmap = json.load(open(os.path.join(DOCS, 'domain-map.json'), encoding='utf-8'))
mm = json.load(open(os.path.join(DOCS, '_migration-map.json'), encoding='utf-8'))
slices_json = json.load(open(os.path.join(DOCS, 'slices.json'), encoding='utf-8'))
inventory = json.load(open(os.path.join(DOCS, 'api-inventory.json'), encoding='utf-8'))
missing_eps = state['missing_eps']
impl_state = json.load(open(os.path.join(DOCS, '_impl_state.json'), encoding='utf-8'))
impl_eps = impl_state['implemented']
missing_keys = {(e['module'], e['method'].upper(), e['path']) for e in missing_eps}

# rust route sources (current tree, post-restructure)
spec = importlib.util.spec_from_file_location('cov', os.path.join(ROOT, r'rust/tools/check_api_coverage.py'))
cov = importlib.util.module_from_spec(spec)
spec.loader.exec_module(cov)
routes = cov.collect_rust_routes(os.path.join(ROOT, 'rust', 'crates'))
rust_src = {}
for r in routes:
    rust_src.setdefault((r['method'].upper(), r['path']), r['source'].replace('\\', '/'))

final = {k: v for k, v in state['final'].items()}
mm_meta = state['mm']
REAL = {sid for sid in final if final[sid]['coded']}
SCAFFOLD = {sid for sid in final if not final[sid]['coded'] and mm_meta[sid]['deleted']}
NEVER = set(final) - REAL - SCAFFOLD

ctrl_src = {}
for c in inventory['controllers']:
    src = c['source_file'].replace('/', '\\')
    seg = src.split('\\')[0]
    mod = norm_module(seg)
    if mod == 'security':
        mod = 'adminapi' if 'security-admin' in src else 'api'
    ctrl_src[(c['class_name'], mod)] = src

# ---------------------------------------------------------------------------
# feature stats
# ---------------------------------------------------------------------------
feat = {}
ep_rows = []
for sid in sorted(final):
    meta = final[sid]
    sj = next(x for x in slices_json if x['slice_id'] == sid)
    split_map = {}
    for _sctx, sf, _stp, ctls in meta['split']:
        for c in ctls:
            split_map[c] = sf
    for a in sj['apis']:
        mod = norm_module(a['source'].split(':')[0].replace('/', '\\').split('\\')[0])
        f = split_map.get(a['controller'], meta['feature'])
        key = (meta['ctx'], f)
        st = feat.setdefault(key, {
            'ctx': meta['ctx'], 'feature': f, 'slices': [], 'controllers': set(),
            'admin': 0, 'client': 0, 'total': 0, 'remaining': 0,
            'impl_total': 0, 'real': False, 'scaffold': False,
            'primary_slice': sid})
        if sid not in st['slices']:
            st['slices'].append(sid)
        st['real'] = st['real'] or (sid in REAL)
        st['scaffold'] = st['scaffold'] or (sid in SCAFFOLD)
        st['total'] += 1
        st['controllers'].add((a['controller'], mod))
        if mod == 'adminapi':
            st['admin'] += 1
        else:
            st['client'] += 1
        if (mod, a['method'].upper(), a['path']) in missing_keys:
            st['remaining'] += 1
            ep_rows.append((meta['ctx'], f, mod, a['method'].upper(), a['path'],
                            (a.get('summary') or '').strip(), a.get('handler', ''),
                            a['controller'], a.get('permissions') or [], a.get('auth', '')))
        else:
            st['impl_total'] += 1

# controllers for split features that own 0 endpoints (defensive)
for sid in final:
    for _sctx, sf, _stp, ctls in final[sid]['split']:
        key = (final[sid]['ctx'], sf)
        for c in ctls:
            for mod in ('adminapi', 'api'):
                if (c, mod) in ctrl_src:
                    feat.setdefault(key, {'ctx': final[sid]['ctx'], 'feature': sf,
                                          'slices': [sid], 'controllers': set(),
                                          'admin': 0, 'client': 0, 'total': 0, 'remaining': 0,
                                          'impl_total': 0, 'real': sid in REAL,
                                          'scaffold': sid in SCAFFOLD,
                                          'primary_slice': sid})['controllers'].add((c, mod))

for st in feat.values():
    st['srcs'] = sorted({ctrl_src[(c, m)] for c, m in st['controllers'] if (c, m) in ctrl_src})
    st['ctrls'] = sorted({c for c, m in st['controllers']})

ctx_remaining = defaultdict(int)
ctx_total = defaultdict(int)
ctx_feat_rem = defaultdict(int)
ctx_feat_done = defaultdict(int)
for st in feat.values():
    ctx_remaining[st['ctx']] += st['remaining']
    ctx_total[st['ctx']] += st['total']
    if st['remaining'] > 0:
        ctx_feat_rem[st['ctx']] += 1
    else:
        ctx_feat_done[st['ctx']] += 1
assert sum(ctx_remaining.values()) == len(missing_eps) == 929, (sum(ctx_remaining.values()), len(missing_eps))
assert sum(ctx_total.values()) == 1483

# baseline endpoints: implemented but absent from slices.json
sliced_keys = set()
for sid in final:
    sj = next(x for x in slices_json if x['slice_id'] == sid)
    for a in sj['apis']:
        mod = norm_module(a['source'].split(':')[0].replace('/', '\\').split('\\')[0])
        sliced_keys.add((mod, a['method'].upper(), a['path']))
baseline = [e for e in impl_eps if (e['module'], e['method'].upper(), e['path']) not in sliced_keys]
baseline.sort(key=lambda e: (e['controller'], e['path']))


def short_src(src):
    return src.replace('\\', '/').replace('src/main/java/', '')


def status_cell(st):
    sid = st['primary_slice']
    if st['real']:
        if st['impl_total'] == 0:
            return '真实实现（0 路由?）'
        return '部分（%d/%d）' % (st['impl_total'], st['total'])
    if st['scaffold']:
        return '未开始（脚手架已删）'
    return '未开始'


def md_escape(s):
    return s.replace('|', '\\|')


def slug(ctx, feature):
    return '%s::%s' % (ctx, feature)


out = []
w = out.append

w('# TigShop Rust 重构：剩余工作路线图（v2 · 14 个限界上下文）')
w('')
w('> **版本** 5.8.57　·　**结构契约** [`rust/docs/ARCHITECTURE.md`](ARCHITECTURE.md)（权威，v2）　·　**映射源** [`rust/docs/domain-map.json`](domain-map.json)')
w('>')
w('> **口径铁律**：进度只认 `python rust/tools/check_api_coverage.py`（静态提取真实注册路由）。')
w('> 本文件的「已实现 / 剩余」来自该工具对重构后 `rust/crates` 的最新静态提取（635 / 1564）。')
w('> 工作单位 = **一个 feature**（不是编号切片）；一次提交只动一个 feature 的四层文件 + 域聚合器。')
w('')
w('---')
w('')
w('## 0. 总览')
w('')
w('| 指标 | 数值 | 说明 |')
w('|------|------|------|')
w('| 契约接口总数 | **1564** | `docs/api-contract.json`（246 个有端点的控制器 + 5 个零端点类） |')
w('| 已实现 | **635** | 其中 554 条来自真实切片实现，81 条来自「切片清单生成前」的基线模块（附录 B） |')
w('| **剩余未实现** | **929** | 落在 **%d** 个 `ctx::feature` 上（附录 A 为逐条清单） |' % len({(r[0], r[1]) for r in ep_rows}))
w('| 切片 | 220 | **73 个真实实现**（coded=true，已由 codemod 迁入 14 个 ctx）；**59 个空脚手架**（已删除）；**88 个从未开始** |')
w('| 限界上下文 | 14 | `identity / merchant / wholesale / catalog / trade / payment / marketing / member / logistics / content / system / im / analytics / openapi` |')
w('| feature 槽位 | 228 | 220 个切片的 `ctx::feature` + 8 个多控制器切片的拆分子特性；其中 **%d** 个已无剩余工作 |' %
  sum(1 for st in feat.values() if st['remaining'] == 0))
w('| Java 控制器 | 251 | 全部映射到 `ctx::feature`（`domain-map.json.controllers[]`） |')
w('| 已迁移基线模块 | 19 | 早期手写领域文件已由 codemod 迁入 19 个 `ctx::feature`（§3），无需人工认领 |')
w('')
w('口径细节：')
w('')
w('1. **73 真实 / 59 脚手架 / 88 未开始。** `docs/_migration-map.json` 的 `real=true`（73）才是真实实现；')
w('   59 个切片文件是「已注册可编译、0 路由」的空脚手架，codemod 已删除；88 个切片从未生成代码。')
w('   因此 `domain-map.json` 中只有这 73 个切片 `coded=true`。')
w('2. **81 条基线接口不在任何切片中。** 它们在 220 切片清单生成之前就已实现（`slices.json` 的 1483 = 1564 − 81），')
w('   现已随 19 个基线模块一起迁入目标 feature（附录 B 给出接口与迁移后文件），不再有剩余工作。')
w('3. **多控制器切片需人工拆分。** `slice-001 / slice-112 / slice-137 / slice-158 / slice-194` 一个文件含多个控制器，')
w('   `domain-map.json` 的 `split[]` 给出子特性；其中 `slice-001 / 137 / 194` 是真实实现，必须手工拆成多个 feature 文件。')
w('4. **同名控制器已消歧。** 同名控制器跨切片共 13 组（`Search / User / AppVersion / BuyerShow / Account / ProductStyle / Region / Config / Comment / DecorateDiscrete / GiftCardGroups / Salesman / SalesmanMaterial`），')
w('   已全部映射到不同 feature；228 个槽位 0 冲突。其中 59 个脚手架切片用到的 8 个重名/保留名在 `domain-map.json` 中带后缀（见 §2 表与各切片 notes）。')
w('')
w('---')
w('')
w('## 1. 优先级与并行策略')
w('')
w('| 优先级 | 上下文 | 判定依据 |')
w('|--------|--------|----------|')
w('| **P0** | `identity` · `system` · `catalog` | 平台/基础设施：登录鉴权、配置、商品与分类是其余域的前置 |')
w('| **P1** | `trade` · `payment` · `member` · `merchant` · `wholesale` · `logistics` | B2B 交易主链路（订单/支付/会员/店铺/批发/履约） |')
w('| **P2** | `marketing` · `content` · `analytics` · `im` · `openapi` | 营销玩法、内容装修、统计、客服、开放接口：不阻塞主链路 |')
w('')
w('并行建议：')
w('')
w('- **先 P0 后 P1/P2**：同一 ctx 内的 feature 之间无写冲突，可并行认领；跨 ctx 有依赖的 feature 按 ctx 顺序领取。')
w('- **共享聚合点串行改**：`core/src/models/mod.rs`、`services/src/lib.rs`、`infrastructure/src/db/repo/mod.rs`、')
w('  `api/admin/src/routes/mod.rs`、`api/public/src/routes/mod.rs`、`AdminState/PublicState` 由每 ctx 一人统一追加。')
w('- **一个 feature = 一个提交**：四层文件 + 域 `mod.rs` 注册 + 聚合器注册 + 覆盖率只增不减。')
w('')
w('跨域依赖（装配时显式传入，禁止 `services` 反向依赖 `api`）：')
w('')
for ctx in CTX_ORDER:
    if ctx in CTX_DEPS:
        w('- `%s`：%s' % (ctx, CTX_DEPS[ctx]))
w('')
w('---')
w('')
w('## 2. 分域路线图')
w('')
w('> 每个 ctx 一节；表格只列 **仍有剩余接口** 的 feature（按剩余数降序），已完成特性在表下列出。')
w('> `admin` = `/adminapi`（`api/admin`），`client` = `/api`（`api/public`），单位：接口条数。')
w('')

num = 0
for ctx in CTX_ORDER:
    num += 1
    real_n = sum(1 for sid in REAL if final[sid]['ctx'] == ctx)
    scaffold_n = sum(1 for sid in SCAFFOLD if final[sid]['ctx'] == ctx)
    never_n = sum(1 for sid in NEVER if final[sid]['ctx'] == ctx)
    w('### 2.%d `%s`（%s）— %s' % (num, ctx, PRIO[ctx], CTX_CN[ctx]))
    w('')
    w('剩余 **%d** / 共 **%d** 条接口；**%d** 个特性待迁移，%d 个已完成。' %
      (ctx_remaining[ctx], ctx_total[ctx], ctx_feat_rem[ctx], ctx_feat_done[ctx]))
    w('切片：真实实现 %d · 脚手架（已删）%d · 未开始 %d。' % (real_n, scaffold_n, never_n))
    w('')
    rows = [st for st in feat.values() if st['ctx'] == ctx and st['remaining'] > 0]
    rows.sort(key=lambda x: (-x['remaining'], x['feature']))
    if rows:
        w('| # | feature | 切片 | Java 控制器（源文件） | admin | client | 剩余/总计 | 状态 |')
        w('|---|---------|------|----------------------|-------|--------|-----------|------|')
        for i, st in enumerate(rows, 1):
            ctrl_txt = '<br>'.join(
                '%s<br><sub>%s</sub>' % (c, short_src(ctrl_src[(c, m)]))
                for c, m in sorted(st['controllers']) if (c, m) in ctrl_src)
            w('| %d | `%s` | %s | %s | %d | %d | **%d**/%d | %s |' % (
                i, st['feature'], ', '.join(st['slices']), ctrl_txt,
                st['admin'], st['client'], st['remaining'], st['total'], status_cell(st)))
    done = sorted([st['feature'] for st in feat.values() if st['ctx'] == ctx and st['remaining'] == 0])
    if done:
        w('')
        w('已完成（无需认领）：' + '、'.join('`%s`' % d for d in done))
    w('')
    w('<details><summary>本域四层目标路径模板</summary>')
    w('')
    w('```')
    w('core         : crates/core/src/models/%s/<feature>.rs' % ctx)
    w('infra        : crates/infrastructure/src/db/repo/%s/<feature>.rs' % ctx)
    w('services     : crates/services/src/%s/<feature>.rs' % ctx)
    w('api admin    : crates/api/admin/src/routes/%s/<feature>.rs' % ctx)
    w('api public   : crates/api/public/src/routes/%s/<feature>.rs' % ctx)
    w('注册点       : core/models/mod.rs · db/repo/mod.rs · services/lib.rs · routes/mod.rs')
    w('               · services/%s/mod.rs 的 Services 字段 · AdminState/PublicState 域字段' % ctx)
    w('```')
    w('')
    w('</details>')
    w('')

# ---------------------------------------------------------------------------
# section 3: relocated baseline modules
# ---------------------------------------------------------------------------
w('---')
w('')
w('## 3. 已迁移的基线模块（19 个保留 feature，无需人工认领）')
w('')
w('这些 feature 在切片清单生成之前就已有手写实现，codemod 已把它们迁入 v2 目录。')
w('它们没有切片编号，`domain-map.json` 也不把它们当切片分配；名称已在对应 ctx 中保留，任何切片都不得占用同名 feature。')
w('')
w('| ctx::feature | 业务内容 | 涉及接口（基线，已实现） |')
w('|--------------|----------|--------------------------|')
for ctx, f in state['reserved']:
    cnt = sum(1 for e in baseline
              if any(c['controller'] == e['controller'] and c['ctx'] == ctx and c['feature'] == f
                     for c in dmap['controllers']))
    w('| `%s::%s` | %s | %d |' % (ctx, f, RESERVED_CN.get((ctx, f), ''), cnt))
w('')
w('> 合计 81 条基线接口（清单见附录 B）。这些接口已计入「已实现 635」，')
w('> 若在对应 feature 中重构实现，必须保持路径/响应不变并同步更新 `implemented_routes()`。')
w('')

# ---------------------------------------------------------------------------
# Appendix A
# ---------------------------------------------------------------------------
w('---')
w('')
w('## 附录 A. 未实现接口清单（929 条，按 `ctx::feature`）')
w('')
w('> 来自 `api-contract.json` 减去 `check_api_coverage.py` 已实现集合；')
w('> 每行：`METHOD path` · 摘要 · `Controller#handler` · 权限码（有则标注）。')
w('')

by_feat = defaultdict(list)
for row in ep_rows:
    by_feat[(row[0], row[1])].append(row)

an = 0
for ctx in CTX_ORDER:
    ctx_feats = sorted([k for k in by_feat if k[0] == ctx],
                       key=lambda k: (-len(by_feat[k]), k[1]))
    if not ctx_feats:
        continue
    an += 1
    w('### A.%d `%s`' % (an, ctx))
    w('')
    for key in ctx_feats:
        st = feat[key]
        w('#### %s — %d 条' % (slug(*key), len(by_feat[key])))
        w('')
        w('- Java 源文件：' + '；'.join('`%s`' % s for s in st['srcs']))
        w('- 目标四层：`crates/core/src/models/%s/%s.rs` · `crates/infrastructure/src/db/repo/%s/%s.rs` · '
          '`crates/services/src/%s/%s.rs` · `crates/api/admin/src/routes/%s/%s.rs` · '
          '`crates/api/public/src/routes/%s/%s.rs`'
          % (ctx, key[1], ctx, key[1], ctx, key[1], ctx, key[1], ctx, key[1]))
        rows_f = sorted(by_feat[key], key=lambda r: (r[2] != 'adminapi', r[4]))
        for _ctx, _f, mod, method, path, summary, handler, controller, perms, auth in rows_f:
            extra = []
            if summary:
                extra.append(md_escape(summary))
            extra.append('`%s#%s`' % (controller, handler))
            if isinstance(perms, str):
                perms = [p for p in perms.split(',') if p]
            if perms:
                extra.append('权限 `%s`' % ','.join(perms))
            if auth and auth != 'jwt':
                extra.append('auth=`%s`' % auth)
            w('- [ ] `%s %s` — %s' % (method, path, ' · '.join(extra)))
        w('')

# ---------------------------------------------------------------------------
# Appendix B
# ---------------------------------------------------------------------------
w('---')
w('')
w('## 附录 B. 基线接口（81 条，已实现、不在切片清单中）')
w('')
w('这些接口在 220 切片清单生成之前已实现；codemod 已把它们并入 §3 的 19 个基线 feature。')
w('下表给出接口、Java 控制器与**迁移后**的 Rust 实现文件（`check_api_coverage.py` 的静态提取结果），')
w('用于回归核对：这些接口已计入 635 基线，重构时不得回退。')
w('')
w('| # | 接口 | Java 控制器 | 迁移后 Rust 文件 |')
w('|---|------|-------------|------------------|')
for i, e in enumerate(baseline, 1):
    key = (e['method'].upper(), e['path'])
    w('| %d | `%s %s` | %s | `%s` |' % (i, e['method'], e['path'], e['controller'],
                                        rust_src.get(key, '?')))
w('')
w('> 合计 **%d** 条基线接口。' % len(baseline))
w('')

# ---------------------------------------------------------------------------
# section 4: how to pick up a feature + DoD
# ---------------------------------------------------------------------------
w('---')
w('')
w('## 4. 如何认领并完成一个 feature')
w('')
w('### 4.1 认领')
w('')
w('1. 在 §2 对应 ctx 表中挑一行（建议从 P0 开始；同 ctx 内任意 feature 相互独立）。')
w('2. 读该 feature 的 Java 源文件（表中「Java 控制器」列；完整路径见附录 A 对应小节）与附录 A 的接口清单。')
w('3. 读 `domain-map.json` 中该切片条目：`ctx / feature / type_prefix / coded / split[]`；多控制器切片先按 `split[]` 拆文件。')
w('4. 在共享任务板上登记 `ctx::feature` + 预期四层文件路径（写作用域），避免与他人冲突。')
w('')
w('### 4.2 实现（四层，同名文件）')
w('')
w('| 层 | 文件 | 内容 | 禁止 |')
w('|----|------|------|------|')
w('| core | `crates/core/src/models/<ctx>/<feature>.rs` | `#[derive(Serialize, Deserialize)]` DTO/VO/Query、纯函数、单测 | SQL/IO |')
w('| infrastructure | `crates/infrastructure/src/db/repo/<ctx>/<feature>.rs` | SQLx 查询、行映射、返回 `Result<T>` | 业务分支 |')
w('| services | `crates/services/src/<ctx>/<feature>.rs` | 用例编排、`pool.begin()/commit()` 事务边界、领域校验 | HTTP 类型 |')
w('| api | `crates/api/admin|public/src/routes/<ctx>/<feature>.rs` | `ROUTE_TABLE`、`routes(&State)`、`implemented_routes()`、handler | SQL、>1 层业务分支 |')
w('')
w('约定：')
w('')
w('- query/body DTO 必须 `#[serde(rename_all = "camelCase")]`；')
w('- 路径逐字符对齐 `docs/api-contract.json`；`ROUTE_TABLE` 与 `routes()` 一一对应并由单测断言；')
w('- handler 只做「解析参数 → 调 service → 包响应」；错误码/响应结构与 Java 保持一致（行为不变）；')
w('- 在域聚合器追加：`<ctx>/mod.rs`（`pub mod <feature>;` + `Services` 字段）、`routes/<ctx>/mod.rs`（merge）、')
w('  `db/repo/<ctx>/mod.rs`、`core/models/<ctx>/mod.rs`；`AdminState/PublicState` 只加域字段，禁止平铺 80 字段。')
w('')
w('### 4.3 完成定义（DoD，全部满足才算完成）')
w('')
w('| 门禁 | 命令 | 通过标准 |')
w('|------|------|----------|')
w('| 编译 | `cargo check --workspace --all-targets` | 0 error |')
w('| 测试 | `cargo test --workspace` | 0 failed（含 `ROUTE_TABLE` ↔ `routes()` 断言） |')
w('| 接口覆盖 | `python rust/tools/check_api_coverage.py` | 不低于 **635/1564**，且本 feature 接口只增不减 |')
w('| 路由冲突 | `python rust/tools/check_route_conflicts.py --strict` | 0 重复 |')
w('| SQL 校验 | `python rust/tools/check_sql_columns.py --strict` | 0 未知表/列 |')
w('| 结构门禁 | `python rust/tools/check_structure.py --strict` | 0 违规（禁止 `slice` 命名、层级越界、空壳注册） |')
w('')
w('补充（feature 级）：')
w('')
w('- [ ] 附录 A 中该 feature 的所有 `- [ ]` 接口全部在 `implemented_routes()` 中可达（不是「已注册但返回未实现」）。')
w('- [ ] 该 feature 在四层各有一个同名文件，路径与 `domain-map.json` 一致。')
w('- [ ] 无空壳：不允许只注册路由不实现（也不允许只写 handler 不接 DB）。')
w('- [ ] 若该 feature 属于多控制器切片（`slice-001/112/137/158/194`），原切片文件已按 `split[]` 拆开且旧文件已删除。')
w('- [ ] 提交信息含 `ctx::feature`，便于 `git log --follow` 追溯旧实现。')
w('')
w('---')
w('')
w('*本文件由 `domain-map.json` + `_migration-map.json` + `check_api_coverage.py` 静态快照生成；结构变更必须先改 `ARCHITECTURE.md`。*')

md = '\n'.join(out) + '\n'
open(os.path.join(DOCS, 'domain-roadmap.md'), 'w', encoding='utf-8', newline='\n').write(md)
print('domain-roadmap.md: %d lines, %d bytes' % (len(out), len(md.encode('utf-8'))))
print('features with remaining:', len(by_feat), 'checklist endpoints:', len(ep_rows))
print('complete features:', sum(1 for st in feat.values() if st['remaining'] == 0), '/', len(feat))
print('baseline endpoints:', len(baseline))
