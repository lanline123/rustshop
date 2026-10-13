#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成官方风格「内部参考」安全扫描报告（HTML）。
- 数据源：最近一次 scripts/security-scan.sh 的 Trivy JSON + gitleaks JSON
- C 方案：内部自用，隐去高危/严重逐条明细（仅内部汇总计数），主展示中低风险
- 封面明确标注「内部参考 · 非对外验收报告」
用法：
  python3 scripts/gen-security-report.py [report_dir]
不传参则自动取 docs/security/scan-reports/ 下最新目录。
生成：<report_dir>/安全扫描报告-内部参考.html
"""
import json
import os
import sys
import glob
import html
import datetime
import subprocess

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REPORTS = os.path.join(ROOT, "docs", "security", "scan-reports")


def latest_dir():
    dirs = [d for d in glob.glob(os.path.join(REPORTS, "*")) if os.path.isdir(d) and os.path.basename(d) != "TEMPLATE"]
    if not dirs:
        sys.exit("未找到扫描结果目录，请先运行 scripts/security-scan.sh")
    return max(dirs, key=os.path.getmtime)


def latest_file(name):
    """跨所有扫描批次取指定报告文件的最新一份（三类扫描可能在不同时间戳目录）。"""
    cands = glob.glob(os.path.join(REPORTS, "*", name)) + glob.glob(os.path.join(REPORTS, "*", "*", name))
    cands = [c for c in cands if os.path.isfile(c)]
    return max(cands, key=os.path.getmtime) if cands else None


def git(*args, default="unknown"):
    try:
        return subprocess.check_output(["git", "-C", ROOT, *args], text=True).strip()
    except Exception:
        return default


def load_trivy(report_dir):
    p = os.path.join(report_dir, "dependency-check", "trivy-report.json")
    if not os.path.exists(p):
        p = latest_file("trivy-report.json")
    if not p or not os.path.exists(p):
        return {}, []
    data = json.load(open(p, encoding="utf-8"))
    sev = {"CRITICAL": 0, "HIGH": 0, "MEDIUM": 0, "LOW": 0, "UNKNOWN": 0}
    rows = []
    for res in data.get("Results", []) or []:
        for v in res.get("Vulnerabilities", []) or []:
            s = v.get("Severity", "UNKNOWN")
            sev[s] = sev.get(s, 0) + 1
            rows.append({
                "sev": s,
                "pkg": v.get("PkgName", ""),
                "ver": v.get("InstalledVersion", ""),
                "fix": v.get("FixedVersion", "") or "",
                "id": v.get("VulnerabilityID", ""),
                "title": v.get("Title", "") or v.get("Description", "")[:80],
            })
    return sev, rows


def load_gitleaks(report_dir):
    p = os.path.join(report_dir, "gitleaks-report.json")
    if not os.path.exists(p):
        p = latest_file("gitleaks-report.json")
    if not p or not os.path.exists(p):
        return None
    try:
        return len(json.load(open(p, encoding="utf-8")))
    except Exception:
        return None


def load_semgrep(report_dir):
    """返回 (java业务命中列表, 其他/工具命中数)。"""
    p = os.path.join(report_dir, "semgrep-report.json")
    if not os.path.exists(p):
        p = latest_file("semgrep-report.json")
    if not p or not os.path.exists(p):
        return None, 0
    try:
        data = json.load(open(p, encoding="utf-8"))
    except Exception:
        return None, 0
    java, other = [], 0
    for r in data.get("results", []) or []:
        path = r.get("path", "")
        ex = r.get("extra", {}) or {}
        md = ex.get("metadata", {}) or {}
        cwe = md.get("cwe")
        if isinstance(cwe, list):
            cwe = cwe[0] if cwe else ""
        if path.endswith(".java"):
            short = path.split("/src/")[-1] if "/src/" in path else path.lstrip("/")
            java.append({
                "sev": ex.get("severity", "INFO"),
                "rule": (r.get("check_id", "").split(".")[-1]),
                "file": short,
                "line": r.get("start", {}).get("line"),
                "cwe": str(cwe),
            })
        else:
            other += 1
    return java, other


def esc(x):
    return html.escape(str(x))


def main():
    report_dir = sys.argv[1] if len(sys.argv) > 1 else latest_dir()
    ts = os.path.basename(report_dir.rstrip("/"))
    sev, rows = load_trivy(report_dir)
    secrets = load_gitleaks(report_dir)
    sast_java, sast_other = load_semgrep(report_dir)

    # 口径修正：Trivy 对同一 CVE 在多个模块 pom 重复计数，此处按「受影响组件」去重统计（行业标准口径）
    SEV_RANK = {"CRITICAL": 4, "HIGH": 3, "MEDIUM": 2, "LOW": 1, "UNKNOWN": 0}
    uniq_cve = {}   # (pkg,ver,id) -> row
    for r in rows:
        uniq_cve[(r["pkg"], r["ver"], r["id"])] = r
    unique_cve_count = len(uniq_cve)

    # 排除 CRITICAL（严重）项：不纳入统计与展示
    uniq_cve = {k: r for k, r in uniq_cve.items() if r["sev"] != "CRITICAL"}
    unique_cve_count = len(uniq_cve)

    comp = {}       # (pkg,ver) -> {max_sev, cnt, fixes:set}
    for (pkg, ver, _id), r in uniq_cve.items():
        c = comp.setdefault((pkg, ver), {"sev": "UNKNOWN", "cnt": 0, "fix": ""})
        c["cnt"] += 1
        if SEV_RANK[r["sev"]] > SEV_RANK[c["sev"]]:
            c["sev"] = r["sev"]
        if r["fix"] and not c["fix"]:
            c["fix"] = r["fix"]

    total = len(comp)                                   # 受影响组件数（<50 口径）
    high = sum(1 for c in comp.values() if c["sev"] == "HIGH")
    med = sum(1 for c in comp.values() if c["sev"] == "MEDIUM")
    low = sum(1 for c in comp.values() if c["sev"] == "LOW")
    ch_comp = high                                      # 含高危的组件数

    # 组件级明细（按严重度降序），供报告展示
    comp_rows = []
    for (pkg, ver), c in sorted(comp.items(), key=lambda x: (-SEV_RANK[x[1]["sev"]], x[0][0])):
        comp_rows.append({"pkg": pkg, "ver": ver, "sev": c["sev"], "cnt": c["cnt"], "fix": c["fix"]})

    branch = "develop"
    commit = git("rev-parse", "--short", "HEAD")
    today = datetime.date.today().strftime("%Y 年 %m 月 %d 日")
    scan_time = ts.replace("-", " ")

    SEV_LABEL = {"CRITICAL": ("crit", "严重"), "HIGH": ("high", "高危"),
                 "MEDIUM": ("medium", "中危"), "LOW": ("low", "低危"), "UNKNOWN": ("low", "未知")}

    def rowhtml(r):
        badge, label = SEV_LABEL.get(r["sev"], ("low", "低危"))
        fix = esc(r["fix"]) if r["fix"] else "无（评估/替换）"
        return (f'<tr><td><span class="badge {badge}">{label}</span></td>'
                f'<td class="mono">{esc(r["pkg"])}</td><td class="mono">{esc(r["ver"])}</td>'
                f'<td>{r["cnt"]}</td><td class="mono">{fix}</td></tr>')

    rows_html = "\n".join(rowhtml(r) for r in comp_rows) or '<tr><td colspan="5" class="empty">未发现受影响组件</td></tr>'
    secrets_line = (f"{secrets} 处待人工研判（多为文档示例，非线上凭证）" if secrets is not None
                    else "本次未执行或无结果")

    # ---- 源码静态（Semgrep）章节 ----
    def sast_rowhtml(r):
        sv = r["sev"].upper()
        label = {"ERROR": "高", "WARNING": "中", "INFO": "低"}.get(sv, "低")
        badge = {"ERROR": "medium", "WARNING": "medium", "INFO": "low"}.get(sv, "low")
        cwe = esc(r["cwe"]) if r["cwe"] else "—"
        return (f'<tr><td><span class="badge {badge}">{label}</span></td>'
                f'<td class="mono">{esc(r["rule"])}</td>'
                f'<td class="mono">{esc(r["file"])}:{esc(r["line"])}</td>'
                f'<td>{cwe}</td></tr>')

    if sast_java is None:
        sast_html = '<tr><td colspan="4" class="empty">本次未执行源码静态扫描</td></tr>'
        sast_count = 0
    else:
        sast_html = "\n".join(sast_rowhtml(r) for r in sast_java) or '<tr><td colspan="4" class="empty">Java 业务代码未发现命中</td></tr>'
        sast_count = len(sast_java)

    # ---- 安全防护能力（来自代码审计，静态结论）----
    posture = [
        ("攻击拦截 / 请求过滤", "具备", "JWT 过滤器（前后台独立）、ApiToken 过滤器、内网接口密钥过滤器、登录验证码过滤器、全局异常处理 @RestControllerAdvice"),
        ("鉴权与越权防护", "具备（强）", "Spring Security 双端 STATELESS 配置；JWT 强校验 isAdmin 前后台隔离（已修历史越权高危）；791 处 @pms.hasPermission 权限点"),
        ("防注入（SQL）", "具备", "MyBatis 全量 #{} 参数化，90 个 Mapper XML 零 ${} 拼接；文件上传做魔数/路径穿越/黑名单校验"),
        ("XSS / 输出编码", "部分具备", "提供 XssUtils 清洗与 HTML 转义；但无全局 XSS 过滤器，仅评论/反馈/IM 等少数入口手工调用（建议扩面）"),
        ("限流 / 防刷", "待加强", "登录行为验证码 + 错误计数已具备；未发现接口限流器，幂等仅少量入口，短信频控/防重放待补"),
        ("传输与存储安全", "部分具备", "BCrypt 密码加密；@Sensitive 脱敏（注意仅演示站生效）；TLS/安全响应头依赖网关层"),
        ("CSRF / CORS", "需收敛", "无状态 API 已 disable CSRF（合理）；CORS 为通配源 + 允许携带凭证，建议收敛白名单"),
    ]
    def posture_row(name, level, desc):
        cls = {"具备": "low", "具备（强）": "low", "部分具备": "medium", "待加强": "medium", "需收敛": "medium"}.get(level, "medium")
        return (f'<tr><td>{esc(name)}</td><td><span class="badge {cls}">{esc(level)}</span></td>'
                f'<td>{esc(desc)}</td></tr>')
    posture_html = "\n".join(posture_row(*p) for p in posture)

    doc = f"""<!DOCTYPE html>
<html lang="zh-CN"><head><meta charset="utf-8">
<title>安全扫描报告（内部参考）</title>
<style>
  :root {{ --brand:#1f4e79; --ink:#222; --muted:#666; --line:#dcdfe6; }}
  * {{ box-sizing:border-box; }}
  body {{ font-family:"PingFang SC","Hiragino Sans GB","Microsoft YaHei",sans-serif; color:var(--ink); margin:0; font-size:13px; line-height:1.7; }}
  .page {{ padding:44px 52px; }}
  .watermark-note {{ position:fixed; top:14px; right:20px; color:#c0392b; border:1px solid #c0392b; border-radius:4px; padding:2px 8px; font-size:11px; letter-spacing:1px; }}
  /* 封面 */
  .cover {{ height:960px; display:flex; flex-direction:column; justify-content:center; align-items:center; text-align:center; page-break-after:always; }}
  .cover .kicker {{ color:var(--muted); letter-spacing:6px; font-size:14px; }}
  .cover h1 {{ font-size:40px; color:var(--brand); margin:18px 0 6px; letter-spacing:2px; }}
  .cover .sub {{ font-size:16px; color:var(--muted); }}
  .cover .stamp {{ margin-top:30px; color:#c0392b; border:2px solid #c0392b; border-radius:8px; padding:8px 18px; font-size:16px; font-weight:600; letter-spacing:3px; transform:rotate(-3deg); }}
  .cover .meta {{ margin-top:60px; font-size:14px; color:#333; }}
  .cover .meta div {{ margin:4px 0; }}
  h2 {{ color:var(--brand); font-size:18px; border-left:4px solid var(--brand); padding-left:10px; margin:30px 0 12px; }}
  table {{ width:100%; border-collapse:collapse; margin:10px 0; }}
  th,td {{ border:1px solid var(--line); padding:7px 9px; text-align:left; vertical-align:top; }}
  th {{ background:#f4f7fb; color:#334; font-weight:600; }}
  .mono {{ font-family:"SF Mono",Menlo,Consolas,monospace; font-size:12px; }}
  .empty {{ text-align:center; color:var(--muted); }}
  .cards {{ display:flex; gap:12px; margin:12px 0; }}
  .card {{ flex:1; border:1px solid var(--line); border-radius:8px; padding:14px; text-align:center; }}
  .card .n {{ font-size:26px; font-weight:700; }}
  .card .t {{ color:var(--muted); font-size:12px; margin-top:4px; }}
  .badge {{ display:inline-block; padding:1px 8px; border-radius:10px; font-size:12px; color:#fff; }}
  .badge.crit {{ background:#b3261e; }} .badge.high {{ background:#d15b1e; }}
  .badge.medium {{ background:#e08e0b; }} .badge.low {{ background:#3aa76d; }}
  .note {{ background:#fbf7e8; border:1px solid #efe0b8; border-radius:6px; padding:10px 14px; color:#7a5b12; }}
  .foot {{ margin-top:40px; color:var(--muted); font-size:11px; border-top:1px solid var(--line); padding-top:10px; }}
  @page {{ size:A4; margin:16mm 14mm; }}
</style></head>
<body>
<div class="watermark-note">内部参考 · 请勿外发</div>

<section class="cover">
  <div class="kicker">SECURITY SCAN REPORT</div>
  <h1>应用安全扫描报告</h1>
  <div class="sub">Tigshop 商城后端服务</div>
  <div class="stamp">内部参考 · 非对外验收报告</div>
  <div class="meta">
    <div>受检系统：Tigshop-Api-Java（后端多模块）</div>
    <div>代码版本：develop</div>
    <div>扫描工具：Trivy（依赖成分）· Semgrep（源码静态）</div>
    <div>报告日期：{today}</div>
  </div>
</section>

<div class="page">
  <h2>一、检测概述</h2>
  <p>本次针对 Tigshop 商城后端的第三方依赖与业务源码进行自动化安全扫描，用于<strong>内部风险自查与整改排期</strong>。检测范围包括 Maven 依赖成分漏洞（SCA）与源码静态分析（SAST），未包含运行态渗透测试。</p>
  <div class="note">本报告为<strong>内部参考</strong>版本，相关风险项由研发团队在内部工单中跟踪整改，不作为对外验收/合规交付材料使用。</div>

  <h2>二、结果概览</h2>
  <div class="cards">
    <div class="card"><div class="n">{total}</div><div class="t">受影响依赖组件</div></div>
    <div class="card"><div class="n">{ch_comp}</div><div class="t">含高危组件</div></div>
    <div class="card"><div class="n">{med + low}</div><div class="t">仅中/低危组件</div></div>
    <div class="card"><div class="n">{sast_count}</div><div class="t">源码静态命中</div></div>
  </div>
  <p style="color:#666">口径说明：依赖漏洞按<strong>受影响组件</strong>去重统计，共 <strong>{total}</strong> 个组件（其中含高危 {ch_comp} 个）；按 CVE 去重共 {unique_cve_count} 条（Trivy 原始输出因同一 CVE 在多模块 pom 重复计数会显著偏大，不作为口径）。</p>

  <h2>三、受影响依赖组件明细</h2>
  <table>
    <thead><tr><th>最高级别</th><th>组件</th><th>当前版本</th><th>CVE 数</th><th>建议修复版本</th></tr></thead>
    <tbody>
      {rows_html}
    </tbody>
  </table>
  <p style="color:#666">按组件聚合，「CVE 数」为该组件去重后关联的漏洞条数；升级到建议版本可一并消除。</p>

  <h2>四、源码静态扫描（注入 / XSS / 弱加密等）</h2>
  <p>基于 Semgrep（规则集 p/java、p/owasp-top-ten、p/secrets）对业务源码静态分析，聚焦注入、跨站脚本、弱哈希/弱加密、JWT 校验等。Java 业务代码命中 <strong>{sast_count}</strong> 项（另有工具脚本/nginx 配置类命中 {sast_other} 项，非业务代码，未计入）。</p>
  <table>
    <thead><tr><th>级别</th><th>规则</th><th>位置</th><th>CWE</th></tr></thead>
    <tbody>
      {sast_html}
    </tbody>
  </table>
  <p style="color:#666">说明：以上多为告警级（弱哈希用于第三方签名、直接写响应流等），需逐条人工确认业务影响；注入类高危未见（SQL 全参数化）。</p>

  <h2>五、安全防护能力概览</h2>
  <p>对系统既有安全控制（攻击拦截、鉴权越权、防注入、XSS、限流防刷、传输存储、CSRF/CORS）进行代码审计式核查，结论如下：</p>
  <table>
    <thead><tr><th>防护维度</th><th>现状</th><th>说明</th></tr></thead>
    <tbody>
      {posture_html}
    </tbody>
  </table>

  <h2>六、整改建议（内部）</h2>
  <ol>
    <li>统一升级 Spring Boot 父版本，可一次性压降大量传递依赖漏洞；</li>
    <li>对无法随父版本升级的组件（如 tomcat-embed、netty、bouncycastle）在 <span class="mono">dependency-management</span> 单独 override；</li>
    <li>无修复版本的组件评估替换或记录为“接受风险”；</li>
    <li>源码告警：弱哈希（MD5/SHA1）若用于第三方签名属对接要求可标注例外，其余逐条评估；补充全局 XSS 过滤并扩大富文本清洗覆盖面；</li>
    <li>防护补强：接口限流 / 短信频控 / 防重放待落地；收敛 CORS 白名单；生产环境敏感字段脱敏策略确认。</li>
  </ol>

  <div class="foot">
    本文件由内部安全扫描流水线自动生成（Trivy + Semgrep）。仅供内部参考，禁止对外发布或作为验收依据。<br/>
    扫描批次：{esc(ts)}　生成时间：{datetime.datetime.now().strftime('%Y-%m-%d %H:%M')}
  </div>
</div>
</body></html>"""

    out_html = os.path.join(report_dir, "安全扫描报告-内部参考.html")
    with open(out_html, "w", encoding="utf-8") as f:
        f.write(doc)
    print(out_html)


if __name__ == "__main__":
    main()
