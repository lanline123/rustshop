#!/usr/bin/env node
/**
 * 未包裹 i18n 的中文文案扫描（面向跨境版）
 *
 * 本项目 i18n 以中文原文作为 key（$t("中文") / t("中文")），所以“漏翻”等价于
 * “源码里出现中文，但没有进入 $t()/t()”。脚本流程：
 *   1) 屏蔽不需要翻译的区域：<style> 块、注释、$t()/t() 的整个实参表（含三元表达式）
 *   2) 在剩余文本里找中文，按所在区域与上下文判定可疑类型和严重度
 *   3) 识别“子组件内部再 $t(prop)”的动态翻译模式（如 CommonHeader title / TopJumpBar tltle），
 *      这类静态中文属性运行时可翻译，不算漏翻
 *   4) 结合 .vort/feature-manifest.json 与 @vort-if 块，判断该处代码在跨境包里是否保留
 *
 * 用法：
 *   node scripts/check-i18n-wrap.mjs                 # 生成报告（默认只详列 A 档）
 *   node scripts/check-i18n-wrap.mjs --all           # 详列所有档位
 *   node scripts/check-i18n-wrap.mjs --json=x.json   # 附带 JSON
 */

import { readFileSync, readdirSync, statSync, writeFileSync, mkdirSync } from "node:fs";
import { join, extname, relative, dirname, resolve, basename } from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = join(dirname(fileURLToPath(import.meta.url)), "..");

const SCAN_EXTS = new Set([".vue", ".ts", ".js", ".mjs", ".tsx", ".jsx"]);
const SKIP_DIRS = new Set([
    "node_modules",
    ".nuxt",
    ".output",
    ".git",
    ".vort",
    ".cursor",
    "dist",
    "public",
    "assets",
    "docs",
    "migrations",
    "scripts",
    "types",
    "server"
]);

// 内容本身是协议 token / 非展示数据，整文件跳过
const IGNORE_FILES = new Map([
    ["pages/chat/src/emojiElements.ts", "表情 [xx] 标记的协议 token，前后端与历史消息共用，不可翻译"],
    ["pages/chat/src/emojiElements.v1.ts", "同上（v1 归档）"]
]);

const CJK = /[\u4e00-\u9fff\u3400-\u4dbf]/;
const PUNCT_ONLY = /^[\s：，。！？、；「」『』（）《》〈〉…—·～\u3000]+$/;

const args = process.argv.slice(2);
const getArg = (name, fallback) => {
    const hit = args.find((a) => a.startsWith(`--${name}=`));
    return hit ? hit.slice(name.length + 3) : fallback;
};
const SHOW_ALL = args.includes("--all");
const OUT_MD = getArg("out", join(ROOT, "scripts", "i18n-unwrapped-report.md"));
const OUT_JSON = getArg("json", "");
/** 排查动态 key 传播为何漏判某文件：--debug-dyn=pages/order/check.vue */
const DEBUG_DYN = getArg("debug-dyn", "");

/* ============================================================== manifest */

const OVERSEAS_ROOTS = new Set(["overseas", "common"]);
const CONDITIONAL_ROOTS = new Set(["merchant", "b2b"]);

function loadManifestIncludes() {
    const includes = [];
    const raw = readFileSync(join(ROOT, ".vort", "feature-manifest.json"), "utf8");
    const json = JSON.parse(raw.replace(/\/\/.*$/gm, ""));
    const walk = (node, feature) => {
        if (!node || typeof node !== "object") return;
        for (const [key, value] of Object.entries(node)) {
            if (key === "includes") {
                if (Array.isArray(value)) for (const p of value) includes.push({ path: p, feature });
                continue;
            }
            if (key === "modules") {
                walk(value, feature);
                continue;
            }
            walk(value, feature ? `${feature}.${key}` : key);
        }
    };
    walk(json, "");
    return includes;
}

const MANIFEST_INCLUDES = loadManifestIncludes();
const CUT_FILE_EXT_RE = /\.(vue|ts|tsx|js|jsx|mjs|cjs|less|css|png|svg|json)$/;
const stripExt = (p) => p.replace(/\.d\.ts$/, "").replace(/\.(vue|ts|tsx|js|jsx|mjs|cjs)$/, "");

function matchCut(relPath) {
    const target = stripExt(relPath);
    for (const { path: inc, feature } of MANIFEST_INCLUDES) {
        const incNorm = stripExt(inc);
        if (CUT_FILE_EXT_RE.test(inc)) {
            if (target === incNorm) return feature;
        } else if (target === incNorm || target.startsWith(`${incNorm}/`)) {
            return feature;
        }
    }
    return null;
}

function bucketOfFeature(feature) {
    if (!feature) return "core";
    const root = feature.split(".")[0];
    if (OVERSEAS_ROOTS.has(root)) return "core";
    if (CONDITIONAL_ROOTS.has(root)) return "conditional";
    return "excluded";
}

function bucketOfExpr(expr) {
    const idents = expr.split(/[&|!()\s]+/).filter(Boolean);
    if (idents.length === 0) return "core";
    if (/!\s*overseas\b/.test(expr)) return "excluded";
    const buckets = idents.map(bucketOfFeature);
    if (buckets.includes("excluded")) return "excluded";
    if (buckets.includes("conditional")) return "conditional";
    return "core";
}

// 档位由重到轻：archived / installer 单独归档，避免淹没主链路
const BUCKET_RANK = { core: 0, installer: 1, conditional: 2, archived: 3, excluded: 4 };
const worseBucket = (a, b) => (BUCKET_RANK[b] > BUCKET_RANK[a] ? b : a);

function fileBucketOf(rel) {
    if (/(^|\/)pages\/install(\/|$)/.test(rel)) return "installer";
    if (/\.v1\.(vue|ts|js|less|css)$/.test(rel) || /\.v1\//.test(rel)) return "archived";
    return bucketOfFeature(matchCut(rel));
}

/* ============================================================== 文本屏蔽 */

function maskRange(chars, start, end) {
    for (let i = start; i < end && i < chars.length; i++) {
        if (chars[i] !== "\n") chars[i] = " ";
    }
}

function buildLineIndex(text) {
    const idx = new Array(text.length);
    let line = 0;
    for (let i = 0; i < text.length; i++) {
        idx[i] = line;
        if (text[i] === "\n") line++;
    }
    return idx;
}

/** SFC 顶层块归属（项目 prettier 风格保证 <template>/<script>/<style> 位于行首） */
function markRegions(text, isVue) {
    const lines = text.split("\n");
    if (!isVue) return new Array(lines.length).fill("script");
    const regions = new Array(lines.length).fill("none");
    let cur = "none";
    lines.forEach((line, i) => {
        const open = line.match(/^<(template|script|style)\b/);
        const close = line.match(/^<\/(template|script|style)>/);
        if (open) {
            cur = open[1];
            regions[i] = cur;
            return;
        }
        if (close) {
            regions[i] = cur;
            cur = "none";
            return;
        }
        regions[i] = cur;
    });
    return regions;
}

function maskStyleBlocks(chars, text) {
    const re = /^<style\b[\s\S]*?^<\/style>/gm;
    let m;
    while ((m = re.exec(text)) !== null) maskRange(chars, m.index, m.index + m[0].length);
}

function maskHtmlComments(chars, text) {
    const re = /<!--[\s\S]*?-->/g;
    let m;
    while ((m = re.exec(text)) !== null) maskRange(chars, m.index, m.index + m[0].length);
}

function skipString(text, start) {
    const quote = text[start];
    let i = start + 1;
    while (i < text.length) {
        if (text[i] === "\\") {
            i += 2;
            continue;
        }
        if (text[i] === quote) return i + 1;
        i++;
    }
    return text.length;
}

/** 只在 script 区域去注释；状态机跳过字符串，避免误伤 "https://" */
function maskJsComments(chars, text, regions) {
    const lineOf = buildLineIndex(text);
    let i = 0;
    while (i < text.length) {
        if (regions[lineOf[i]] !== "script") {
            i++;
            continue;
        }
        const c = text[i];
        if (c === '"' || c === "'" || c === "`") {
            i = skipString(text, i);
            continue;
        }
        if (c === "/" && text[i + 1] === "/") {
            let j = text.indexOf("\n", i);
            if (j === -1) j = text.length;
            maskRange(chars, i, j);
            i = j;
            continue;
        }
        if (c === "/" && text[i + 1] === "*") {
            const j = text.indexOf("*/", i + 2);
            const end = j === -1 ? text.length : j + 2;
            maskRange(chars, i, end);
            i = end;
            continue;
        }
        i++;
    }
}

/** 从 `(` 位置找配对 `)`，跳过字符串 */
function matchParen(text, openIdx) {
    let depth = 0;
    let i = openIdx;
    while (i < text.length) {
        const c = text[i];
        if (c === '"' || c === "'" || c === "`") {
            i = skipString(text, i);
            continue;
        }
        if (c === "(") depth++;
        else if (c === ")") {
            depth--;
            if (depth === 0) return i;
        }
        i++;
    }
    return -1;
}

// $t( / t( / i18n.t(，排除 format( / split( 这类以 t 结尾的标识符
const T_CALL_OPEN_RE = /(?<![A-Za-z0-9_$])(?:\$t|t)\s*\(/g;

/** 屏蔽翻译调用的整个实参表（含 `$t(cond ? 'a' : 'b')` 这类三元） */
function maskTranslatedArgs(chars, text) {
    T_CALL_OPEN_RE.lastIndex = 0;
    let m;
    let wrapped = 0;
    while ((m = T_CALL_OPEN_RE.exec(text)) !== null) {
        const openIdx = m.index + m[0].length - 1;
        const closeIdx = matchParen(text, openIdx);
        if (closeIdx === -1) continue;
        if (CJK.test(text.slice(openIdx, closeIdx))) wrapped++;
        maskRange(chars, openIdx + 1, closeIdx);
    }
    return wrapped;
}

/* =============================================== 子组件动态翻译 prop 识别 */

const DEFINE_PROPS_RE = /defineProps\s*(<[^>()]*>)?\s*\(/;

/**
 * 取出 defineProps 的实参文本与泛型类型名。
 * 必须做括号配对：`default: () => ({...})` 里的 `)` 会让非贪婪匹配提前收尾，
 * 从而漏掉后面所有 prop。
 */
function definePropsParts(script) {
    const m = DEFINE_PROPS_RE.exec(script);
    if (!m) return null;
    const openIdx = m.index + m[0].length - 1;
    const closeIdx = matchParen(script, openIdx);
    return {
        typeName: (m[1] || "").replace(/[<>]/g, "").trim(),
        body: closeIdx === -1 ? "" : script.slice(openIdx + 1, closeIdx)
    };
}

function collectPropNames(script) {
    const names = new Set();
    const parts = definePropsParts(script);
    if (parts) {
        const { body, typeName } = parts;
        for (const mm of body.matchAll(/(?:^|[\s{,;])["']?([A-Za-z_]\w*)["']?\s*[?]?\s*:/g)) names.add(mm[1]);
        for (const mm of body.matchAll(/["']([A-Za-z_]\w*)["']\s*(?:,|\])/g)) names.add(mm[1]);
        // defineProps<Props>() 的泛型只给出类型名，prop 名要回到 interface / type 声明里取
        if (/^[A-Za-z_]\w*$/.test(typeName)) {
            const decl = new RegExp(`(?:interface|type)\\s+${escapeRe(typeName)}\\s*=?\\s*\\{([\\s\\S]*?)\\n\\}`).exec(script);
            if (decl) for (const mm of decl[1].matchAll(/(?:^|[\s{,;])["']?([A-Za-z_]\w*)["']?\s*\??\s*:/g)) names.add(mm[1]);
        }
    }
    // withDefaults 的默认值对象同样是 prop 名的可靠来源
    const wd = /withDefaults\s*\(\s*defineProps[\s\S]*?,\s*\{([\s\S]*?)\}\s*\)/.exec(script);
    if (wd) for (const mm of wd[1].matchAll(/(?:^|[\s{,;])["']?([A-Za-z_]\w*)["']?\s*:/g)) names.add(mm[1]);
    for (const mm of script.matchAll(/props\.([A-Za-z_]\w*)/g)) names.add(mm[1]);
    return names;
}

/** 取出每个 t()/$t() 的第一个实参（即翻译 key）文本 */
function collectTKeyArgs(text) {
    const out = [];
    T_CALL_OPEN_RE.lastIndex = 0;
    let m;
    while ((m = T_CALL_OPEN_RE.exec(text)) !== null) {
        const openIdx = m.index + m[0].length - 1;
        const closeIdx = matchParen(text, openIdx);
        if (closeIdx === -1) continue;
        out.push(firstArg(text.slice(openIdx + 1, closeIdx)));
    }
    return out;
}

/** 按顶层逗号切出第一个实参 */
function firstArg(argText) {
    let depth = 0;
    let i = 0;
    while (i < argText.length) {
        const c = argText[i];
        if (c === '"' || c === "'" || c === "`") {
            i = skipString(argText, i);
            continue;
        }
        if ("([{".includes(c)) depth++;
        else if (")]}".includes(c)) depth--;
        else if (c === "," && depth === 0) return argText.slice(0, i);
        i++;
    }
    return argText;
}

/** 去掉字符串字面量，只留表达式骨架 */
function stripStrings(s) {
    let out = "";
    let i = 0;
    while (i < s.length) {
        const c = s[i];
        if (c === '"' || c === "'" || c === "`") {
            i = skipString(s, i);
            continue;
        }
        out += c;
        i++;
    }
    return out;
}

/** 取出所有 t()/$t() 实参里出现的标识符（含属性名） */
function collectTranslatedIdents(text) {
    const idents = new Set();
    for (const arg of collectTKeyArgs(text)) {
        for (const mm of stripStrings(arg).matchAll(/(?:props\.)?([A-Za-z_]\w*)/g)) idents.add(mm[1]);
    }
    return idents;
}

const IDENT_STOPWORDS = new Set([
    "props",
    "value",
    "String",
    "Number",
    "Boolean",
    "Object",
    "Array",
    "JSON",
    "Math",
    "true",
    "false",
    "null",
    "undefined",
    "computed",
    "ref",
    "reactive",
    "t"
]);

/**
 * 分析“同文件内动态 key 翻译”：
 *   - fields：被 $t(x.field) 取用的属性名，对应 `field: "中文"` 这类配置项
 *   - names ：被 $t(fn(...)) / $t(varName) 取用的函数或变量名
 *   - ranges：上述名字在本文件的声明区间（区间内的中文都会经外层 $t）
 */
function analyzeDynamicTranslation(text, isVue, dynPropsResolver, extra = {}) {
    const fields = new Set(extra.fields || []);
    const names = new Set(extra.names || []);
    for (const arg of collectTKeyArgs(text)) {
        const skel = stripStrings(arg);
        for (const mm of skel.matchAll(/\.([A-Za-z_]\w*)/g)) fields.add(mm[1]);
        for (const mm of skel.matchAll(/(?<![.\w$])([A-Za-z_]\w{1,})/g)) {
            if (!IDENT_STOPWORDS.has(mm[1])) names.add(mm[1]);
        }
    }

    if (isVue && dynPropsResolver) {
        // `:tltle="pageTitle"` 传给内部会 $t 的 prop：pageTitle 的声明处也算已翻译
        for (const mm of text.matchAll(/<([A-Z][\w.-]*)((?:[^>"']|"[^"]*"|'[^']*')*?)>/g)) {
            const dynProps = dynPropsResolver(mm[1]);
            if (!dynProps || dynProps.size === 0) continue;
            for (const am of mm[2].matchAll(/(?::|v-bind:)([\w.-]+)\s*=\s*"([^"]*)"/g)) {
                if (!dynProps.has(am[1])) continue;
                for (const im of stripStrings(am[2]).matchAll(/(?<![.\w$])([A-Za-z_]\w{1,})/g)) {
                    if (!IDENT_STOPWORDS.has(im[1])) names.add(im[1]);
                }
            }
        }
        // v-for 别名回溯：`v-for="tab in tabs"` 且 $t(tab) → tabs 的声明也算已翻译
        for (const mm of text.matchAll(/v-for\s*=\s*"\(?\s*([A-Za-z_]\w*)[^"]*?\)?\s+(?:in|of)\s+([A-Za-z_][\w.]*)/g)) {
            if (names.has(mm[1]) || fields.size) names.add(mm[2].split(".")[0]);
        }
    }

    const lines = text.split("\n");
    const ranges = [];
    const rangesFor = (name) => {
        const re = new RegExp(`(?:const|let|var|function)\\s+${escapeRe(name)}\\b`, "g");
        const out = [];
        let m;
        while ((m = re.exec(text)) !== null) {
            const r = declLineRange(text, m.index, lines);
            if (r) out.push(r);
        }
        return out;
    };

    let frontier = [...names];
    for (let pass = 0; pass < 3 && frontier.length; pass++) {
        const next = [];
        for (const name of frontier) {
            for (const r of rangesFor(name)) {
                ranges.push(r);
                // 被 $t 包裹的函数体里 return 的其它标识符，其定义处的中文同样会经过 $t
                // 被 $t 包裹的函数体里引用的其它标识符，其定义处的中文同样会流入 $t
                const body = lines.slice(r[0], r[1] + 1).join("\n");
                for (const im of stripStrings(body).matchAll(/(?<![.\w$])([A-Za-z_]\w{1,})/g)) {
                    if (IDENT_STOPWORDS.has(im[1]) || names.has(im[1])) continue;
                    names.add(im[1]);
                    next.push(im[1]);
                }
            }
        }
        frontier = next;
    }
    return { fields, names, ranges };
}

/**
 * 从声明起点扫到语句结束，返回 [起始行, 结束行]（0 基）。
 * 项目由 prettier 统一加分号，故 const/let/var 扫到深度 0 的 `;`；
 * function 声明无分号，改用花括号配对。箭头函数的形参括号会让深度短暂归零，
 * 因此不能一见 `)` 就收尾。
 */
function declLineRange(text, startIdx, lines) {
    const isFunctionDecl = /^function\b/.test(text.slice(startIdx));
    let i = startIdx;
    let depth = 0;
    let seenBrace = false;
    while (i < text.length) {
        const c = text[i];
        if (c === '"' || c === "'" || c === "`") {
            i = skipString(text, i);
            continue;
        }
        if ("([{".includes(c)) {
            depth++;
            if (c === "{") seenBrace = true;
        } else if (")]}".includes(c)) {
            depth--;
            if (isFunctionDecl && seenBrace && depth <= 0) {
                i++;
                break;
            }
        } else if (depth === 0 && c === ";") {
            break;
        }
        i++;
    }
    const startLine = text.slice(0, startIdx).split("\n").length - 1;
    const endLine = text.slice(0, Math.min(i, text.length)).split("\n").length - 1;
    if (endLine < startLine) return null;
    // 声明区间过长通常意味着漏了分号导致越界，宁可不排除
    if (endLine - startLine > 400) return null;
    return [startLine, endLine];
}

const OBJ_KEY_RE = /(?:^|[{,])\s*["']?([A-Za-z_]\w*)["']?\s*:\s*(["'`])((?:\\.|(?!\2)[\s\S])*?)\2/g;

/**
 * 该行是否是“对已被 $t 包裹的变量赋值”，如 `errorMessage.value = "视频播放失败"`。
 * 这类赋值往往在变量声明区间之外，但取值处统一走 $t(errorMessage)。
 */
function isAssignToDynamicName(line, names) {
    const m = /(?:^|[({;=]|\breturn\s+\(?)\s*([A-Za-z_]\w*)(?:\.value)?\s*=(?!=)/.exec(line);
    return !!m && names.has(m[1]);
}

/**
 * 该行是否是“组件自身会 t(props.x) 的 prop 默认值”。
 * 兼容单行 `x: { type: String, default: "中文" }` 与多行 defineProps 写法。
 */
function isTranslatedPropDefault(lines, i, props) {
    if (props.size === 0) return false;
    const line = lines[i];
    if (!/\bdefault\s*:/.test(line)) return false;
    const inline = /^\s*["']?([A-Za-z_]\w*)["']?\s*:\s*\{/.exec(line);
    if (inline) return props.has(inline[1]);
    for (let n = i - 1; n >= 0 && i - n <= 15; n--) {
        const owner = /^\s*["']?([A-Za-z_]\w*)["']?\s*:\s*\{\s*$/.exec(lines[n]);
        if (owner) return props.has(owner[1]);
    }
    return false;
}

/** 该行中文是否落在“会被 $t(x.field) 动态翻译”的配置项键上 */
function isDynamicField(line, snippet, fields) {
    OBJ_KEY_RE.lastIndex = 0;
    let m;
    while ((m = OBJ_KEY_RE.exec(line)) !== null) {
        if (m[3].includes(snippet) && fields.has(m[1])) return true;
    }
    return false;
}

/**
 * 判断组件把哪些 prop 交给了 t()：直接 t(props.x)，或经一层别名
 * （如 `const topTitle = props.tltle` 配 `$t(topTitle)`）。
 */
function analyzeDynamicProps(text) {
    const props = collectPropNames(text);
    if (props.size === 0) return new Set();
    let translated = collectTranslatedIdents(text);
    // 沿赋值链回溯，把别名展开为原始 prop 名
    for (let pass = 0; pass < 3; pass++) {
        const added = [];
        for (const id of translated) {
            const re = new RegExp(`(?:const|let|var)\\s+${escapeRe(id)}\\s*=([^;\\n]{0,200})`, "g");
            let m;
            while ((m = re.exec(text)) !== null) {
                for (const mm of m[1].matchAll(/(?:props\.)?([A-Za-z_]\w*)/g)) {
                    if (!translated.has(mm[1])) added.push(mm[1]);
                }
            }
        }
        if (added.length === 0) break;
        translated = new Set([...translated, ...added]);
    }
    return new Set([...props].filter((p) => translated.has(p)));
}

const escapeRe = (s) => s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

/** PascalCase 组件名候选（Nuxt 目录前缀 + 裸文件名） */
function componentNameCandidates(rel) {
    const noExt = rel.replace(/\.vue$/, "");
    const parts = noExt.split("/").filter((p) => p !== "components");
    const pascal = (s) => s.replace(/(^|[-_])(\w)/g, (_, __, c) => c.toUpperCase());
    const file = pascal(parts[parts.length - 1]);
    const names = new Set([file]);
    const dirs = parts.slice(0, -1).map(pascal);
    // Nuxt 会去掉与前一段重复的前缀，这里两种形态都登记
    names.add(dirs.join("") + file);
    if (dirs.length > 1) names.add(dirs.slice(0, 1).join("") + file);
    if (dirs.length > 0) names.add(dirs[dirs.length - 1] + file);
    return [...names].filter(Boolean);
}

/* ==================================================================== 分类 */

/** 在行内找出包含该片段的最贴合的引号字符串区间（取最短，避免命中外层属性引号） */
function quotedRangeOf(line, snippet) {
    // 同时按“成对引号”和“单一引号种类”扫描，取最短命中，才能拿到属性值里嵌套的内层字符串
    const patterns = [/(['"`])((?:\\.|(?!\1)[\s\S])*?)\1/g, /'([^']*)'/g, /"([^"]*)"/g, /`([^`]*)`/g];
    let best = null;
    for (const re of patterns) {
        re.lastIndex = 0;
        let m;
        while ((m = re.exec(line)) !== null) {
            const inner = m[2] ?? m[1];
            if (!inner.includes(snippet)) continue;
            if (!best || m[0].length < best.end - best.start) {
                best = { start: m.index, end: m.index + m[0].length, inner };
            }
        }
    }
    return best;
}

/** 片段是否落在正则字面量里 */
function inRegexLiteral(line, snippet) {
    const re = /(?:^|[=(,:[!&|?+\s])\/(?![*/])((?:\\.|\[[^\]]*\]|[^/\\\n])+)\/[gimsuy]*/g;
    let m;
    while ((m = re.exec(line)) !== null) {
        if (m[1].includes(snippet)) return true;
    }
    return false;
}

function classify(line, snippet, region, dynamicPropOk) {
    const trimmed = line.trim();
    if (/^(?:\*|\/\/|<!--)/.test(trimmed)) return { kind: "注释残留", severity: "low" };
    if (inRegexLiteral(line, snippet)) return { kind: "正则字面量", severity: "low" };
    // TS 字面量联合类型：): "已下架" | "已售罄" | "" =>
    if (/["'][^"']*["']\s*\|\s*["']/.test(line) && /[:)]\s*["']/.test(line)) {
        return { kind: "类型字面量", severity: "low" };
    }

    const q = quotedRangeOf(line, snippet);
    if (q) {
        const before = line.slice(Math.max(0, q.start - 20), q.start);
        if (/(?:===|!==|==|!=|>=|<=)\s*$/.test(before)) return { kind: "逻辑比较值", severity: "low" };
        if (/\.(?:includes|indexOf|startsWith|endsWith)\s*\(\s*$/.test(before)) return { kind: "逻辑比较值", severity: "low" };
        if (/\bcase\s*$/.test(before)) return { kind: "逻辑比较值", severity: "low" };
        if (/(?:includes|indexOf|some)\s*\(/.test(line) && /\[[^\]]*['"]/.test(line)) return { kind: "逻辑比较值", severity: "low" };
        if (/throw new \w*Error\s*\(/.test(line)) return { kind: "开发期错误", severity: "low" };
    }
    if (/(?:console|logger)\.(?:log|warn|error|info|debug)|(?:^|[^\w.])this\.log\s*\(/.test(line)) {
        return { kind: "日志输出", severity: "low" };
    }

    if (region === "template") {
        const attrRe = /(?<![:@\w.-])([A-Za-z_][\w.-]*)\s*=\s*(['"])([^'"]*)\2/g;
        let m;
        while ((m = attrRe.exec(line)) !== null) {
            if (!m[3].includes(snippet)) continue;
            const attr = m[1];
            if (dynamicPropOk && dynamicPropOk.has(attr)) {
                return { kind: `静态属性 ${attr}（子组件内部已 $t）`, severity: "ok" };
            }
            if (/^(?:src|href|to|name|ref|key|prop|class|style|type|value|id)$/.test(attr)) {
                return { kind: `静态属性 ${attr}`, severity: "medium" };
            }
            return { kind: `静态属性 ${attr}="…"`, severity: "high" };
        }
        if (/\{\{[\s\S]*?\}\}/.test(line)) return { kind: "模板插值", severity: "high" };
        return { kind: "模板文本", severity: "high" };
    }

    if (/(?:message|ElMessage|ElMessageBox|notification)\s*[.(]/.test(line)) return { kind: "提示 / 弹窗文案", severity: "high" };
    if (/\b(?:label|title|name|text|tip|tips|placeholder|desc|value|lable)\s*:/.test(line)) return { kind: "配置项文案", severity: "high" };
    return { kind: "脚本字符串", severity: "high" };
}

/* ==================================================================== 扫描 */

function collectFiles(dir, out = []) {
    for (const name of readdirSync(dir)) {
        const full = join(dir, name);
        let st;
        try {
            st = statSync(full);
        } catch {
            continue;
        }
        if (st.isDirectory()) {
            if (!SKIP_DIRS.has(name)) collectFiles(full, out);
        } else if (SCAN_EXTS.has(extname(name))) {
            out.push(full);
        }
    }
    return out;
}

const files = collectFiles(ROOT);
const relOf = (f) => relative(ROOT, f).replace(/\\/g, "/");

/** 把 import 路径规范为相对仓库根；裸包名返回 null */
function normalizeImport(spec, fileAbs) {
    const p = spec.split("?")[0];
    let rel;
    if (p.startsWith("~/") || p.startsWith("@/")) rel = p.slice(2);
    else if (p.startsWith("./") || p.startsWith("../")) rel = relative(ROOT, resolve(dirname(fileAbs), p)).replace(/\\/g, "/");
    else return null;
    return rel.replace(/\\/g, "/");
}

const IMPORT_RE = /\bimport\s+(?:type\s+)?([\w*${},\s]+?)\s+from\s*["']([^"']+)["']/g;

/** 取出 import 的本地绑定名与来源路径 */
function scanImportBindings(text) {
    const out = [];
    IMPORT_RE.lastIndex = 0;
    let m;
    while ((m = IMPORT_RE.exec(text)) !== null) {
        if (/^\s*import\s+type\b/.test(m[0])) continue;
        const clause = m[1];
        const locals = [];
        const braced = clause.match(/\{([^}]*)\}/);
        if (braced) {
            for (const part of braced[1].split(",")) {
                const name = part.trim().split(/\s+as\s+/).pop()?.trim();
                if (name) locals.push(name);
            }
        }
        const bare = clause.replace(/\{[^}]*\}/g, "").replace(/,/g, " ").trim();
        for (const w of bare.split(/\s+/)) {
            if (w && w !== "*" && !/^as$/.test(w)) locals.push(w);
        }
        out.push({ locals, spec: m[2] });
    }
    return out;
}

// 第一遍：登记组件的动态 prop、每个文件“经 $t(x.field) 取用的字段名”、以及 import 关系
const COMPONENT_DYNAMIC_PROPS = new Map(); // rel -> Set(prop)
const TRANSLATED_FIELDS = new Map(); // rel -> Set(field)
const EXTERNAL_DYN_NAMES = new Map(); // rel -> Set(name)  被别处 $t(imported(...)) 包裹的导出名
const NAME_TO_REL = new Map(); // 组件名候选 -> rel[]
const FILE_TEXT = new Map();

for (const f of files) {
    const rel = relOf(f);
    const text = readFileSync(f, "utf8");
    FILE_TEXT.set(rel, text);
    const fieldSet = new Set();
    for (const arg of collectTKeyArgs(text)) {
        for (const mm of stripStrings(arg).matchAll(/\.([A-Za-z_]\w*)/g)) fieldSet.add(mm[1]);
    }
    if (fieldSet.size) TRANSLATED_FIELDS.set(rel, fieldSet);
    if (extname(f) === ".vue") {
        const dyn = analyzeDynamicProps(text);
        if (dyn.size) COMPONENT_DYNAMIC_PROPS.set(rel, dyn);
        for (const n of componentNameCandidates(rel)) {
            if (!NAME_TO_REL.has(n)) NAME_TO_REL.set(n, []);
            NAME_TO_REL.get(n).push(rel);
        }
    }
}

// 跨文件：`$t(getBuyText(...))` 这类在调用点包裹的导入，其定义文件里的中文同样已被翻译
const REL_SET = new Set(FILE_TEXT.keys());
const resolveModule = (rel) => {
    if (REL_SET.has(rel)) return rel;
    for (const ext of [".ts", ".vue", ".js", ".mjs", "/index.ts", "/index.vue"]) {
        if (REL_SET.has(rel + ext)) return rel + ext;
    }
    return null;
};
for (const f of files) {
    const rel = relOf(f);
    const text = FILE_TEXT.get(rel);
    if (!text.includes("t(")) continue;
    const tIdents = collectTranslatedIdents(text);
    if (tIdents.size === 0) continue;
    for (const { locals, spec } of scanImportBindings(text)) {
        const norm = normalizeImport(spec, f);
        if (!norm) continue;
        const target = resolveModule(norm);
        if (!target || target === rel) continue;
        for (const local of locals) {
            if (!tIdents.has(local)) continue;
            if (!EXTERNAL_DYN_NAMES.has(target)) EXTERNAL_DYN_NAMES.set(target, new Set());
            EXTERNAL_DYN_NAMES.get(target).add(local);
        }
    }
}

/** 本文件渲染的子组件所翻译的字段名（父组件把 `field: "中文"` 传下去时由子组件 $t） */
function childTranslatedFields(text, ownerFile) {
    const out = new Set();
    for (const mm of text.matchAll(/<([A-Z][\w.-]*)/g)) {
        const tag = mm[1];
        const imp = new RegExp(`import\\s+${escapeRe(tag)}\\s+from\\s*["']([^"']+)["']`).exec(text);
        const cands = [];
        if (imp) {
            const norm = normalizeImport(imp[1], ownerFile);
            const target = norm && resolveModule(norm);
            if (target) cands.push(target);
        }
        cands.push(...(NAME_TO_REL.get(tag) || []));
        for (const rel of cands) {
            const fields = TRANSLATED_FIELDS.get(rel);
            if (fields) for (const x of fields) out.add(x);
        }
    }
    return out;
}

/** 解析当前文件里 <Tag> 对应的组件，返回其动态翻译 prop 集合 */
function dynamicPropsForTag(tag, ownerFile, ownerText) {
    const imp = new RegExp(`import\\s+${escapeRe(tag)}\\s+from\\s*["']([^"']+)["']`).exec(ownerText);
    if (imp) {
        let p = imp[1];
        let rel;
        if (p.startsWith("~/") || p.startsWith("@/")) rel = p.slice(2);
        else rel = relative(ROOT, resolve(dirname(ownerFile), p)).replace(/\\/g, "/");
        const hit = COMPONENT_DYNAMIC_PROPS.get(rel);
        if (hit) return hit;
    }
    const cands = NAME_TO_REL.get(tag) || [];
    const merged = new Set();
    for (const rel of cands) {
        const dyn = COMPONENT_DYNAMIC_PROPS.get(rel);
        if (dyn) for (const p of dyn) merged.add(p);
    }
    return merged;
}

const OPEN_RE = /^\s*(?:\/\/|<!--|\/\*|\*)\s*@vort-if\b(.*)$/;
const END_RE = /^\s*(?:\/\/|<!--|\/\*|\*)\s*@vort-endif\b/;

function markVortBuckets(lines) {
    const buckets = new Array(lines.length).fill("core");
    const stack = [];
    lines.forEach((line, i) => {
        if (END_RE.test(line)) {
            stack.pop();
            buckets[i] = "skip";
            return;
        }
        const m = line.match(OPEN_RE);
        if (m) {
            const expr = m[1]
                .replace(/-->\s*$/, "")
                .replace(/\*\/\s*$/, "")
                .trim();
            stack.push(bucketOfExpr(expr));
            buckets[i] = "skip";
            return;
        }
        buckets[i] = stack.reduce((acc, b) => worseBucket(acc, b), "core");
    });
    return buckets;
}

const SEG_RE = /[\u4e00-\u9fff][\u4e00-\u9fff\w\s：，。！？、；（）《》%\-/.+*·]*/g;

// 中文确实需要保留时的豁免标记（提交给后端的字段值、按语言分支的字面量等）
const IGNORE_INLINE_RE = /i18n-ignore(?!-next-line)/;
const IGNORE_NEXT_RE = /i18n-ignore-next-line/;
let ignoredInline = 0;

function scanFile(file) {
    const rel = relOf(file);
    if (IGNORE_FILES.has(rel)) return { findings: [], wrapped: 0, ignored: true };
    const text = readFileSync(file, "utf8");
    if (!CJK.test(text)) return { findings: [], wrapped: 0 };

    const isVue = extname(file) === ".vue";
    const lines = text.split("\n");
    const regions = markRegions(text, isVue);
    const vortBuckets = markVortBuckets(lines);
    const fBucket = fileBucketOf(rel);

    const chars = text.split("");
    if (isVue) {
        maskStyleBlocks(chars, text);
        maskHtmlComments(chars, text);
    }
    maskJsComments(chars, text, regions);
    const decommented = chars.join("");
    const dyn = analyzeDynamicTranslation(decommented, isVue, (tag) => dynamicPropsForTag(tag, file, text), {
        fields: isVue ? childTranslatedFields(decommented, file) : [],
        names: EXTERNAL_DYN_NAMES.get(rel) || []
    });
    // 本组件自己交给 t() 的 prop：其默认值与 withDefaults 默认值都无需再包裹
    const selfDynProps = isVue ? analyzeDynamicProps(decommented) : new Set();
    const dynKeys = new Set([...dyn.fields, ...selfDynProps]);
    const wrapped = maskTranslatedArgs(chars, decommented);
    const maskedLines = chars.join("").split("\n");
    const inDynRange = (i) => dyn.ranges.some(([s, e]) => i >= s && i <= e);

    if (DEBUG_DYN && rel === DEBUG_DYN) {
        console.log(`[debug] ${rel}`);
        console.log(`  names  : ${[...dyn.names].sort().join(", ")}`);
        console.log(`  fields : ${[...dyn.fields].sort().join(", ")}`);
        console.log(`  ranges : ${dyn.ranges.map(([s, e]) => `${s + 1}-${e + 1}`).join(", ")}`);
    }

    const findings = [];
    maskedLines.forEach((mline, i) => {
        if (!CJK.test(mline)) return;
        const region = isVue ? regions[i] : "script";
        if (region !== "template" && region !== "script") return;
        if (vortBuckets[i] === "skip") return;

        SEG_RE.lastIndex = 0;
        const segs = [];
        let m;
        while ((m = SEG_RE.exec(mline)) !== null) {
            const seg = m[0].trim();
            if (seg && !PUNCT_ONLY.test(seg)) segs.push(seg);
        }
        if (segs.length === 0) return;

        const rawLine = lines[i];
        // 显式豁免：本行 `i18n-ignore` 或上一行 `i18n-ignore-next-line`，注释里需写明原因
        if (IGNORE_INLINE_RE.test(rawLine) || IGNORE_NEXT_RE.test(lines[i - 1] || "")) {
            ignoredInline++;
            return;
        }
        // 同文件内经动态 key 翻译：$t(fn(...)) 的函数体、$t(v) 的变量声明、$t(x.field) 的配置项
        if (inDynRange(i)) return;
        if (isDynamicField(rawLine, segs[0], dynKeys)) return;
        // `errorMessage.value = "中文"`：赋值目标本身会被 $t 包裹，声明区间之外的赋值同样安全
        if (isAssignToDynamicName(rawLine, dyn.names)) return;
        // defineProps 里的 `default: "中文"`，且该 prop 在本组件内经 t(props.x) 翻译
        if (isTranslatedPropDefault(lines, i, selfDynProps)) return;

        let dynOk = null;
        if (region === "template") {
            const tagMatch = /<([A-Z][\w.-]*)/.exec(rawLine) || /<([a-z]+-[\w-]*)/.exec(rawLine);
            if (tagMatch) dynOk = dynamicPropsForTag(tagMatch[1], file, text);
        }
        const { kind, severity } = classify(rawLine, segs[0], region, dynOk);
        if (severity === "ok") return;

        findings.push({
            file: rel,
            line: i + 1,
            region,
            kind,
            severity,
            snippets: segs,
            bucket: worseBucket(fBucket, vortBuckets[i]),
            context: rawLine.trim().slice(0, 180)
        });
    });
    return { findings, wrapped };
}

const all = [];
let wrappedTotal = 0;
let scanned = 0;
let ignoredFiles = 0;
for (const f of files) {
    const r = scanFile(f);
    if (r.ignored) {
        ignoredFiles++;
        continue;
    }
    scanned++;
    wrappedTotal += r.wrapped;
    all.push(...r.findings);
}

/* ==================================================================== 输出 */

const BUCKETS = ["core", "installer", "conditional", "archived", "excluded"];
const BUCKET_TITLE = {
    core: "A. 跨境版主链路（必须处理）",
    installer: "B. 安装向导 pages/install（部署工具，通常不随跨境版对外）",
    conditional: "C. 随多商户 / B2B 特性（跨境版含该特性时需处理）",
    archived: "D. v1 归档文件（未启用，切回时才需处理）",
    excluded: "E. 跨境版已裁剪（可忽略）"
};
const SEV_ORDER = { high: 0, medium: 1, low: 2 };
const SEV_LABEL = { high: "高", medium: "中", low: "低" };

const byBucket = Object.fromEntries(BUCKETS.map((b) => [b, []]));
for (const f of all) byBucket[f.bucket].push(f);

const groupByFile = (list) => {
    const map = new Map();
    for (const f of list) {
        if (!map.has(f.file)) map.set(f.file, []);
        map.get(f.file).push(f);
    }
    return [...map.entries()].sort((a, b) => {
        const hi = (x) => x[1].filter((i) => i.severity === "high").length;
        return hi(b) - hi(a) || b[1].length - a[1].length;
    });
};

const countSev = (list, sev) => list.filter((f) => f.severity === sev).length;

const md = [];
md.push("# 跨境版未包裹 i18n 的中文文案扫描报告");
md.push("");
md.push(`生成命令：\`node scripts/check-i18n-wrap.mjs${SHOW_ALL ? " --all" : ""}\``);
md.push("");
md.push("## 总览");
md.push("");
md.push(`- 扫描文件：${scanned}（整文件忽略 ${ignoredFiles}）`);
md.push(`- 已正确包裹的中文 \`$t/t\` 调用：${wrappedTotal}`);
md.push(`- 经 \`i18n-ignore\` 显式豁免：${ignoredInline}`);
md.push(`- 可疑未包裹条目：${all.length}`);
md.push("");
md.push("| 档位 | 高 | 中 | 低 | 合计 |");
md.push("| --- | --- | --- | --- | --- |");
for (const b of BUCKETS) {
    const l = byBucket[b];
    md.push(`| ${BUCKET_TITLE[b]} | ${countSev(l, "high")} | ${countSev(l, "medium")} | ${countSev(l, "low")} | ${l.length} |`);
}
md.push("");
md.push("判定口径：");
md.push("");
md.push("- 本项目以中文原文作为 i18n key，故“源码出现中文且未进入 `$t()/t()`”即视为漏翻。");
md.push("- 已排除：`<style>` 块、注释、语言包、`types/`、`assets/`、`docs/`、`migrations/`、`server/`。");
md.push("- 已排除“子组件内部再 `$t(prop)`”的静态属性（如 `CommonHeader title` / `TopJumpBar tltle`），运行时可翻译。");
md.push("- 严重度：高＝界面直接展示；中＝需人工确认是否展示；低＝日志 / 开发期错误 / 逻辑比较值。");
md.push("- 中文确需保留时（提交给后端的字段值、按语言分支的字面量等），在该行加 `i18n-ignore` 或上一行加 `i18n-ignore-next-line`，并写明原因。");
md.push("");
if (IGNORE_FILES.size) {
    md.push("整文件忽略清单：");
    md.push("");
    for (const [f, why] of IGNORE_FILES) md.push(`- \`${f}\`：${why}`);
    md.push("");
}

for (const b of BUCKETS) {
    md.push(`## ${BUCKET_TITLE[b]}`);
    md.push("");
    const list = byBucket[b];
    if (list.length === 0) {
        md.push("无。");
        md.push("");
        continue;
    }
    if (b !== "core" && !SHOW_ALL) {
        md.push(`共 ${list.length} 条（高 ${countSev(list, "high")}）。加 \`--all\` 查看明细。`);
        md.push("");
        continue;
    }
    for (const [file, items] of groupByFile(list)) {
        items.sort((x, y) => SEV_ORDER[x.severity] - SEV_ORDER[y.severity] || x.line - y.line);
        md.push(`### ${file} （${items.length} 条，高 ${countSev(items, "high")}）`);
        md.push("");
        md.push("| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |");
        md.push("| --- | --- | --- | --- | --- | --- |");
        for (const it of items) {
            const snip = it.snippets.join(" / ").replace(/\|/g, "\\|");
            const ctx = it.context.replace(/\|/g, "\\|").replace(/`/g, "'");
            md.push(`| ${it.line} | ${SEV_LABEL[it.severity]} | ${it.region} | ${it.kind} | ${snip} | \`${ctx}\` |`);
        }
        md.push("");
    }
}

mkdirSync(dirname(OUT_MD), { recursive: true });
writeFileSync(OUT_MD, md.join("\n"), "utf8");
if (OUT_JSON) writeFileSync(OUT_JSON, JSON.stringify({ scanned, wrappedTotal, findings: all }, null, 2), "utf8");

console.log(`[i18n-wrap] scanned=${scanned} wrapped=${wrappedTotal} suspicious=${all.length}`);
for (const b of BUCKETS) {
    const l = byBucket[b];
    console.log(`[i18n-wrap] ${b.padEnd(12)} total=${String(l.length).padStart(4)} high=${String(countSev(l, "high")).padStart(4)}`);
}
console.log(`[i18n-wrap] report -> ${relative(ROOT, OUT_MD).replace(/\\/g, "/")}`);
