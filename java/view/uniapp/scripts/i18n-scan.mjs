#!/usr/bin/env node
/**
 * 扫描源码中未被 i18n 包裹的中文字面量。
 *
 * 本项目的 i18n 方案是「中文原文即 key」（src/store/i18n.ts 从服务端拉语言包后
 * setLocaleMessage），因此只要中文没进 t() / $t()，跨境版切到英文时就会原样露出中文。
 *
 * 用法：
 *   node scripts/i18n-scan.mjs [--root <项目目录>] [--scope overseas|all]
 *                              [--json <输出文件>] [--limit <每类条数>]
 *
 * --scope overseas 会排除 .vort/feature-manifest.json 中 o2o 独有路径，
 * 并把 @vort-if 内提及 o2o 的代码块降级为单独一类（跨境构建会裁掉）。
 */

import { readFileSync, readdirSync, statSync, writeFileSync, existsSync, mkdirSync } from "node:fs";
import { join, extname, resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const args = process.argv.slice(2);
const argOf = (name, def) => {
    const i = args.indexOf(`--${name}`);
    return i >= 0 && args[i + 1] ? args[i + 1] : def;
};

const SELF_DIR = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(argOf("root", resolve(SELF_DIR, "..")));
const SRC = join(ROOT, "src");
const SCOPE = argOf("scope", "overseas");
const LIMIT = Number(argOf("limit", "0"));
const JSON_OUT = argOf("json", "");

const CN = /[\u3400-\u4dbf\u4e00-\u9fff]/;
const SCAN_EXTS = new Set([".vue", ".ts", ".js"]);

/** 当前文件是否自带 safeT() 之类的「渲染时兜底翻译」包装 */
let fileHasLocalT = false;

/* ------------------------------------------------------------------ 工具 */

const rel = (p) => p.replace(ROOT, "").replace(/^[\\/]/, "").replace(/\\/g, "/");

function lineIndex(text) {
    const starts = [0];
    for (let i = 0; i < text.length; i++) if (text[i] === "\n") starts.push(i + 1);
    return (offset) => {
        let lo = 0;
        let hi = starts.length - 1;
        while (lo < hi) {
            const mid = (lo + hi + 1) >> 1;
            if (starts[mid] <= offset) lo = mid;
            else hi = mid - 1;
        }
        return lo + 1;
    };
}

function walk(dir, files) {
    for (const name of readdirSync(dir)) {
        if (name === "node_modules" || name === ".git" || name === "static" || name === "dist") continue;
        const full = join(dir, name);
        const st = statSync(full);
        if (st.isDirectory()) walk(full, files);
        else if (SCAN_EXTS.has(extname(name))) files.push(full);
    }
}

function parseJsonc(text) {
    return JSON.parse(
        text.replace(/\/\*[\s\S]*?\*\//g, "").replace(/(^|[^:"'])\/\/.*$/gm, "$1")
    );
}

/** feature-manifest 中某个业态（含其 modules）声明的所有 includes 路径 */
function featurePaths(manifest, feature) {
    const out = [];
    const collect = (node) => {
        if (!node || typeof node !== "object") return;
        for (const [k, v] of Object.entries(node)) {
            if (k === "includes" && Array.isArray(v)) out.push(...v);
            else if (v && typeof v === "object") collect(v);
        }
    };
    collect(manifest[feature]);
    return out.map((p) => p.replace(/\\/g, "/"));
}

/* -------------------------------------------------------- script 词法扫描 */

const WRAPPED_BEFORE = /(?:\$t|\bt|tFormat|tc|te|\$tc)\s*\(\s*(?:\/\*[^*]*\*\/\s*)?$/;
const isTranslateFn = (name) => /^(?:\$?t|\$?tc|te|tFormat|safeT)$/.test(name);

/**
 * 判断中文字面量是「给用户看的文案」还是「当标识符/枚举值用」。
 * 后者不需要翻译，但如果同一文案在别处被 $t() 包裹后参与比较，就是逻辑不一致。
 */
function classifyUsage(before, after, hasLocalT) {
    if (/(?:===|!==|==|!=|\bcase)\s*$/.test(before)) return "compare";
    // 会员装修模块的惯例：数据里存中文默认值，渲染时统一走本文件的 safeT()
    if (hasLocalT && /\b(?:label|picTitle|name|title)\s*:\s*$/.test(before)) return "indirect";
    if (/\b(?:includes|indexOf|startsWith|endsWith|has|delete|get|set)\s*\(\s*$/.test(before)) return "compare";
    if (/(?:===|!==|==|!=)\s*$/.test(after.trimStart())) return "compare";
    if (/\b(?:key|field|prop|value|type|code|id|status|state|tag|slot)\s*:\s*$/i.test(before)) return "ident";
    if (/\[\s*$/.test(before) && /\]\s*(?:=|\+\+|--)/.test(after)) return "ident";
    return "display";
}
const REGEX_PREV = new Set([..."(,=:[!&|?{};+-*%~^<>\n\r\t "]);

/**
 * 扫描一段 JS/TS 源，收集含中文的字符串字面量。
 * 会正确跳过注释、正则字面量，并递归处理模板字符串里的 ${} 表达式。
 */
function scanScript(text, start, end, push, callStack = []) {
    let i = start;
    let prevNonSpace;
    let lastIdent = "";
    while (i < end) {
        const c = text[i];

        // 维护所在调用链，用于识别 $t(cond ? "A" : "B") 这类「外层已包裹」的写法
        if (/[A-Za-z_$]/.test(c)) {
            let j = i;
            while (j < end && /[A-Za-z0-9_$]/.test(text[j])) j++;
            lastIdent = text.slice(i, j);
            prevNonSpace = text[j - 1];
            i = j;
            continue;
        }
        if (c === "(") {
            callStack.push(lastIdent);
            lastIdent = "";
            prevNonSpace = c;
            i++;
            continue;
        }
        if (c === ")") {
            callStack.pop();
            lastIdent = "";
            prevNonSpace = c;
            i++;
            continue;
        }

        if (c === "/" && text[i + 1] === "/") {
            while (i < end && text[i] !== "\n") i++;
            continue;
        }
        if (c === "/" && text[i + 1] === "*") {
            i += 2;
            while (i < end && !(text[i] === "*" && text[i + 1] === "/")) i++;
            i += 2;
            continue;
        }
        if (c === '"' || c === "'" || c === "`") {
            const quote = c;
            const s = i;
            i++;
            let content = "";
            let hasInterp = false;
            while (i < end) {
                if (text[i] === "\\") {
                    content += text[i + 1] ?? "";
                    i += 2;
                    continue;
                }
                if (text[i] === quote) break;
                if (quote === "`" && text[i] === "$" && text[i + 1] === "{") {
                    let depth = 1;
                    let j = i + 2;
                    while (j < end && depth > 0) {
                        if (text[j] === "{") depth++;
                        else if (text[j] === "}") {
                            depth--;
                            if (depth === 0) break;
                        }
                        j++;
                    }
                    scanScript(text, i + 2, j, push, [...callStack]);
                    hasInterp = true;
                    i = j + 1;
                    continue;
                }
                if (quote !== "`" && text[i] === "\n") break; // 未闭合，防跑飞
                content += text[i];
                i++;
            }
            if (CN.test(content)) {
                const before = text.slice(Math.max(0, s - 60), s);
                const after = text.slice(i + 1, i + 20);
                push({
                    offset: s,
                    text: content,
                    kind: "script",
                    wrapped: callStack.some(isTranslateFn) || WRAPPED_BEFORE.test(before),
                    // 拼接出来的 key 在语言包里永远不存在，vue-i18n 会原样回落成中文
                    dynamicKey: hasInterp || /\+\s*$/.test(before) || /^\s*\+/.test(after),
                    usage: classifyUsage(before, after, fileHasLocalT)
                });
            }
            i++;
            prevNonSpace = quote;
            lastIdent = "";
            continue;
        }
        if (c === "/" && (prevNonSpace === undefined || REGEX_PREV.has(prevNonSpace))) {
            i++;
            while (i < end && text[i] !== "\n") {
                if (text[i] === "\\") {
                    i += 2;
                    continue;
                }
                if (text[i] === "[") {
                    while (i < end && text[i] !== "]") {
                        if (text[i] === "\\") i++;
                        i++;
                    }
                }
                if (text[i] === "/") break;
                i++;
            }
            i++;
            prevNonSpace = "/";
            continue;
        }
        if (!/\s/.test(c)) prevNonSpace = c;
        i++;
    }
}

/* ------------------------------------------------------ template 结构扫描 */

function findTagEnd(text, from, end) {
    let i = from + 1;
    while (i < end) {
        const c = text[i];
        if (c === '"' || c === "'") {
            const q = c;
            i++;
            while (i < end && text[i] !== q) i++;
        } else if (c === ">") return i + 1;
        i++;
    }
    return end;
}

const ATTR_RE = /([@:#a-zA-Z_][\w:.\-[\]]*)\s*=\s*(?:"([^"]*)"|'([^']*)')/g;
/** 明显不是给用户看的属性（值里的中文属于数据/标识，不参与翻译判定） */
const DATA_ATTRS = /^(?:class|style|:?key|ref|id|:?name|data-|v-for|v-else|v-if|v-show|v-model)/;

function scanTag(text, from, to, push) {
    const inner = text.slice(from, to);
    if (/^<\/|^<!/.test(inner)) return;
    ATTR_RE.lastIndex = 0;
    let m;
    while ((m = ATTR_RE.exec(inner)) !== null) {
        const attr = m[1];
        const raw = m[2] !== undefined ? m[2] : m[3];
        if (!CN.test(raw)) continue;
        const valueOffset = from + m.index + m[0].indexOf(raw, attr.length);
        const isExpr = attr.startsWith(":") || attr.startsWith("@") || attr.startsWith("v-");
        if (isExpr) {
            scanScript(text, valueOffset, valueOffset + raw.length, push);
        } else {
            push({
                offset: valueOffset,
                text: raw,
                kind: "attr",
                attr,
                wrapped: false,
                soft: DATA_ATTRS.test(attr)
            });
        }
    }
}

function scanTextNode(text, from, to, push) {
    let i = from;
    while (i < to) {
        const open = text.indexOf("{{", i);
        if (open < 0 || open >= to) {
            emitPlain(text, i, to, push);
            return;
        }
        emitPlain(text, i, open, push);
        const close = text.indexOf("}}", open);
        const exprEnd = close < 0 || close > to ? to : close;
        scanScript(text, open + 2, exprEnd, push);
        i = exprEnd + 2;
    }
}

function emitPlain(text, from, to, push) {
    const raw = text.slice(from, to);
    if (!CN.test(raw)) return;
    const trimmed = raw.trim();
    push({
        offset: from + raw.indexOf(trimmed[0]),
        text: trimmed.replace(/\s+/g, " "),
        kind: "text",
        wrapped: false
    });
}

function scanTemplate(text, start, end, push) {
    let i = start;
    while (i < end) {
        if (text.startsWith("<!--", i)) {
            const j = text.indexOf("-->", i);
            i = j < 0 ? end : j + 3;
            continue;
        }
        // SFC 块之间偶尔有裸的 JS 块注释（如文件头的 @file 说明）
        if (text.startsWith("/*", i)) {
            const j = text.indexOf("*/", i);
            i = j < 0 ? end : j + 2;
            continue;
        }
        if (text[i] === "<") {
            const tagEnd = findTagEnd(text, i, end);
            scanTag(text, i, tagEnd, push);
            i = tagEnd;
            continue;
        }
        let j = text.indexOf("<", i);
        if (j < 0 || j > end) j = end;
        scanTextNode(text, i, j, push);
        i = j;
    }
}

/* ------------------------------------------------------------ @vort-if 区间 */

/** 返回 @vort-if 表达式提到某特性的 [start,end) 区间集合 */
function vortRanges(text, featureRe) {
    const ranges = [];
    const stack = [];
    const re = /@vort-(if\b[^\n]*?(?=-->|\*\/|$)|endif\b)/g;
    let m;
    while ((m = re.exec(text)) !== null) {
        if (m[0].startsWith("@vort-endif")) {
            const open = stack.pop();
            if (open && featureRe.test(open.expr)) ranges.push([open.start, m.index + m[0].length]);
        } else {
            stack.push({ start: m.index, expr: m[0].replace(/^@vort-if\s*/, "") });
        }
    }
    return ranges;
}

const inRanges = (ranges, offset) => ranges.some(([a, b]) => offset >= a && offset < b);

/* ------------------------------------------------------------------- 主流程 */

const manifest = existsSync(join(ROOT, ".vort", "feature-manifest.json"))
    ? parseJsonc(readFileSync(join(ROOT, ".vort", "feature-manifest.json"), "utf8"))
    : {};
const o2oPaths = SCOPE === "overseas" ? featurePaths(manifest, "o2o") : [];

const files = [];
walk(SRC, files);

const COMPARE_WITH_T = /(?:(?:===|!==|==|!=)\s*(?:\$t|\bt|tFormat)\s*\(\s*['"`][^'"`]*[\u4e00-\u9fff][^'"`]*['"`]|(?:\$t|\bt|tFormat)\s*\(\s*['"`][^'"`]*[\u4e00-\u9fff][^'"`]*['"`]\s*\)\s*(?:===|!==|==|!=))/g;

// 本地登记的占位符 key：只有这些 key 能安全带参数，其余 missing 时会原样输出 {0}
const localeKeys = new Set(
    existsSync(join(SRC, "locale", "zh.json")) ? Object.keys(JSON.parse(readFileSync(join(SRC, "locale", "zh.json"), "utf8"))) : []
);
const PLACEHOLDER = /\{\d+\}|\{[A-Za-z_][A-Za-z0-9_]*\}/;

const findings = [];
const wrapped = [];
const compareWithT = [];
const dynamicKeys = [];
let wrappedCount = 0;
let o2oScopedCount = 0;

for (const file of files) {
    const relPath = rel(file);
    if (o2oPaths.some((p) => relPath === p || relPath.startsWith(`${p}/`))) {
        continue;
    }
    const text = readFileSync(file, "utf8");
    if (!CN.test(text)) continue;
    const lineAt = lineIndex(text);
    fileHasLocalT = /\bconst\s+safeT\s*=/.test(text);
    const hits = [];
    const push = (hit) => hits.push(hit);

    // 与 $t() 结果做等值比较：状态值多为服务端/代码里的中文原文，切英文后必然失配
    for (const m of text.matchAll(COMPARE_WITH_T)) {
        // 两侧都过翻译函数（safeT(x) !== t("...")）是正确写法，跳过
        if (/(?:safeT|\$?t)\s*\([^()]*\)\s*$/.test(text.slice(Math.max(0, m.index - 40), m.index))) continue;
        const line = lineAt(m.index);
        compareWithT.push({
            file: relPath,
            line,
            code: (text.split(/\r?\n/)[line - 1] ?? "").trim().slice(0, 160)
        });
    }

    if (extname(file) === ".vue") {
        const skip = [];
        const blockRe = /<(script|style)\b[^>]*>([\s\S]*?)<\/\1>/g;
        let m;
        while ((m = blockRe.exec(text)) !== null) {
            const innerStart = m.index + m[0].indexOf(">", 0) + 1;
            const innerEnd = m.index + m[0].length - `</${m[1]}>`.length;
            skip.push([m.index, m.index + m[0].length]);
            if (m[1] === "script") scanScript(text, innerStart, innerEnd, push);
        }
        let cursor = 0;
        for (const [a, b] of skip) {
            if (a > cursor) scanTemplate(text, cursor, a, push);
            cursor = b;
        }
        if (cursor < text.length) scanTemplate(text, cursor, text.length, push);
    } else {
        scanScript(text, 0, text.length, push);
    }

    const o2oOnly = SCOPE === "overseas" ? vortRanges(text, /\bo2o\b/) : [];

    for (const hit of hits) {
        if (hit.wrapped) {
            wrappedCount++;
            const line = lineAt(hit.offset);
            wrapped.push({ file: relPath, line, text: hit.text });
            const unregistered = PLACEHOLDER.test(hit.text) && !localeKeys.has(hit.text);
            if (hit.dynamicKey || unregistered) {
                dynamicKeys.push({
                    file: relPath,
                    line,
                    text: hit.text,
                    kind: hit.dynamicKey ? "concat" : "unregistered",
                    code: (text.split(/\r?\n/)[line - 1] ?? "").trim().slice(0, 160)
                });
            }
            continue;
        }
        const line = lineAt(hit.offset);
        const lineText = text.split(/\r?\n/)[line - 1] ?? "";
        if (/console\.(log|warn|error|info|debug)/.test(lineText) && hit.kind === "script") continue;
        if (inRanges(o2oOnly, hit.offset)) {
            o2oScopedCount++;
            continue;
        }
        findings.push({
            file: relPath,
            line,
            kind: hit.kind,
            attr: hit.attr,
            usage: hit.usage || "display",
            soft: !!hit.soft,
            code: lineText.trim().slice(0, 160),
            text: hit.text.length > 80 ? `${hit.text.slice(0, 80)}…` : hit.text
        });
    }
}

/* ---------------------------------------------------------------- 输出报告 */

const byFile = new Map();
for (const f of findings) {
    if (!byFile.has(f.file)) byFile.set(f.file, []);
    byFile.get(f.file).push(f);
}
const sorted = [...byFile.entries()].sort((a, b) => b[1].length - a[1].length);

const hard = findings.filter((f) => !f.soft);
console.log(`[i18n-scan] 项目：${ROOT}`);
console.log(`[i18n-scan] 扫描范围：${SCOPE}（跳过 o2o 独有路径 ${o2oPaths.length} 条）`);
console.log(`[i18n-scan] 文件数：${files.length}，已包裹中文字面量：${wrappedCount}`);
console.log(`[i18n-scan] o2o 条件块内跳过：${o2oScopedCount}`);
console.log(
    `[i18n-scan] 未包裹中文：${findings.length}（其中 text=${findings.filter((f) => f.kind === "text").length}, ` +
        `attr=${findings.filter((f) => f.kind === "attr").length}, script=${findings.filter((f) => f.kind === "script").length}）`
);
console.log(`[i18n-scan] 涉及文件：${byFile.size}`);
console.log(`[i18n-scan] 与 $t() 做等值比较：${compareWithT.length}`);
console.log(`[i18n-scan] 拼接式 key（语言包必然失配）：${dynamicKeys.length}\n`);

const shown = args.includes("--quiet") ? [] : LIMIT > 0 ? sorted.slice(0, LIMIT) : sorted;
for (const [file, list] of shown) {
    console.log(`${file}  (${list.length})`);
    for (const f of list.sort((a, b) => a.line - b.line)) {
        const tag = f.kind === "attr" ? `attr:${f.attr}` : f.kind;
        console.log(`  ${String(f.line).padStart(5)}  [${tag}]${f.soft ? "(数据属性)" : ""} ${f.text}`);
    }
    console.log("");
}

if (JSON_OUT) {
    const outPath = resolve(JSON_OUT);
    mkdirSync(dirname(outPath), { recursive: true });
    writeFileSync(outPath, JSON.stringify({ root: ROOT, scope: SCOPE, findings, compareWithT, dynamicKeys, wrapped }, null, 2), "utf8");
    console.log(`[i18n-scan] 明细已写入 ${outPath}`);
}

// 默认只报告不阻断；CI 需要门禁时加 --strict
process.exitCode = args.includes("--strict") && hard.length > 0 ? 1 : 0;
