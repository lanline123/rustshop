#!/usr/bin/env node
/**
 * @vort-if 标记配对校验（轻量前置检查，挂在 build 前）
 *
 * 校验三件事，任一不过则以非 0 退出、阻断构建：
 *   1) 配对：每个 @vort-if 必须有对应 @vort-endif（支持嵌套，用栈匹配）
 *   2) 悬空：不能出现多余的 @vort-endif
 *   3) 特性名合法：表达式里每个标识符的“根段”必须在允许集合内
 *      （允许集合 = 版本基名 + feature-manifest.json 里出现的所有特性键）
 *
 * 目的：漏标/错配对若拖到最终“裁剪纯版本包”那天才暴露，排查成本极高，
 * 因此把校验前置到每次 build。语法定义见 docs/o2o大融合-需求方案.md §3.3。
 */

import { readFileSync, readdirSync, statSync, existsSync } from "node:fs";
import { join, extname, relative, dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = join(dirname(fileURLToPath(import.meta.url)), "..");

// 只扫描源代码文件；跳过产物、依赖、.vort 自身（本文件含标记字面量，避免自扫描误报）
const SCAN_EXTS = new Set([".vue", ".ts", ".js", ".mjs", ".tsx", ".jsx"]);
const SKIP_DIRS = new Set([
    "node_modules", ".nuxt", ".nuxt-dev", ".output", "dist", ".git", ".vort", "public", "assets",
]);

// 版本基名（即使 manifest 未列为键也允许，作为 @vort-if 表达式的合法根）
const BASE_FEATURES = new Set(["merchant", "b2b", "overseas", "o2o", "vendor", "common"]);

/** 读取 feature-manifest.json（含 // 注释，先去注释再解析），递归收集所有特性键 */
function loadManifest() {
    const features = new Set(BASE_FEATURES);
    // 所有特性的 includes：{ path: 相对仓库根路径, feature: 所属特性（点分，如 merchant.shop） }
    const includes = [];
    try {
        const raw = readFileSync(join(ROOT, ".vort", "feature-manifest.json"), "utf8");
        const stripped = raw.replace(/\/\/.*$/gm, "");
        const json = JSON.parse(stripped);
        const walk = (node, feature) => {
            if (!node || typeof node !== "object") return;
            for (const [key, value] of Object.entries(node)) {
                if (key === "includes") {
                    if (Array.isArray(value)) {
                        for (const p of value) includes.push({ path: p, feature });
                    }
                    continue;
                }
                if (key === "modules") {
                    // modules 不单独占一段特性名，沿用父特性继续下钻
                    walk(value, feature);
                    continue;
                }
                // key 为特性名，拼成点分特性路径（如 merchant.shop / b2b.batchBuy / common.wholesale）
                const fp = feature ? `${feature}.${key}` : key;
                features.add(key);
                walk(value, fp);
            }
        };
        walk(json, "");
    } catch (e) {
        console.warn(`[vort-if] 读取 feature-manifest.json 失败，仅用基名校验：${e.message}`);
    }
    return { features, includes };
}

const { features: ALLOWED, includes: MANIFEST_INCLUDES } = loadManifest();

/** 收集待扫描文件 */
function collectFiles(dir, out) {
    for (const name of readdirSync(dir)) {
        const full = join(dir, name);
        const st = statSync(full);
        if (st.isDirectory()) {
            if (!SKIP_DIRS.has(name)) collectFiles(full, out);
        } else if (SCAN_EXTS.has(extname(name))) {
            out.push(full);
        }
    }
    return out;
}

/** 校验表达式里每个标识符的根段是否合法，返回非法根段数组 */
function invalidTokens(expr) {
    const bad = [];
    // 运算符 & | ! ( ) 作分隔，取出标识符（允许点号分段）
    const idents = expr.split(/[&|!()\s]+/).filter(Boolean);
    if (idents.length === 0) bad.push("(空表达式)");
    for (const id of idents) {
        const root = id.split(".")[0];
        if (!ALLOWED.has(root)) bad.push(id);
    }
    return bad;
}

// 已知源码/资源扩展名，用于区分 includes 项是“文件”还是“目录”
const CUT_FILE_EXT_RE = /\.(vue|ts|tsx|js|jsx|mjs|cjs|less|css|png|svg|json)$/;

/** 去掉源码扩展名，便于“不带扩展名的 import”与“带扩展名的 includes 项”比对 */
function stripExt(p) {
    return p.replace(/\.d\.ts$/, "").replace(/\.(vue|ts|tsx|js|jsx|mjs|cjs)$/, "");
}

/** 把 import 路径规范化为“相对仓库根”的路径；裸包名（node_modules）返回 null */
function normalizeImport(spec, fileAbs) {
    const p = spec.split("?")[0];
    let rel;
    if (p.startsWith("~/") || p.startsWith("@/")) rel = p.slice(2);
    else if (p.startsWith("./") || p.startsWith("../")) rel = relative(ROOT, resolve(dirname(fileAbs), p));
    else if (p.startsWith("/")) rel = p.slice(1);
    else return null;
    return rel.replace(/\\/g, "/");
}

/** 判断相对路径是否命中某个裁剪项；命中返回 { inc, feature }，否则 null */
function matchCut(relPath) {
    const target = stripExt(relPath);
    for (const { path: inc, feature } of MANIFEST_INCLUDES) {
        const incNorm = stripExt(inc);
        if (CUT_FILE_EXT_RE.test(inc)) {
            // 文件项：去扩展名后精确相等
            if (target === incNorm) return { inc, feature };
        } else {
            // 目录项：等于目录本身或以“目录/”为前缀
            if (target === incNorm || target.startsWith(`${incNorm}/`)) return { inc, feature };
        }
    }
    return null;
}

// 静态 import / re-export / 动态 import 的路径提取（\s 含换行，兼容多行 import）
const IMPORT_RES = [
    { re: /\bimport\s+(?:type\s+)?[\w*${},\s]+?\s+from\s*["']([^"']+)["']/g, typeable: true },
    { re: /\bexport\s+(?:type\s+)?(?:\*|\{[^}]*\}|[\w*,\s]+)\s+from\s*["']([^"']+)["']/g, typeable: true },
    { re: /\bimport\s*\(\s*["']([^"']+)["']\s*\)/g, typeable: false },
    { re: /\bimport\s*["']([^"']+)["']/g, typeable: false }
];

/** 扫描文本收集 import 路径；跳过 import type / export type（类型在编译期擦除，不触发加载） */
function scanImports(text) {
    const out = [];
    const seen = new Set();
    for (const { re, typeable } of IMPORT_RES) {
        re.lastIndex = 0;
        let m;
        while ((m = re.exec(text)) !== null) {
            const spec = m[1];
            if (typeable && /^\s*(?:import|export)\s+type\b/.test(m[0])) continue;
            // 定位路径所在行（多行 import 也能对到 from 那一行）
            const idx = m.index + m[0].lastIndexOf(spec);
            const line = text.slice(0, idx).split("\n").length;
            const key = `${line}|${spec}`;
            if (seen.has(key)) continue;
            seen.add(key);
            out.push({ spec, line });
        }
    }
    return out;
}

// 仅识别“独立标记注释”：@vort-if / @vort-endif 必须紧跟注释开头（// 、<!-- 、/* 、JSDoc 的 *），
// 从而避免散文注释里顺带提到 @vort-if 被误判（如 “跨境风 @vort-if overseas”）。
const OPEN_RE = /^\s*(?:\/\/|<!--|\/\*|\*)\s*@vort-if\b(.*)$/;
const END_RE = /^\s*(?:\/\/|<!--|\/\*|\*)\s*@vort-endif\b/;

function checkFile(file, errors) {
    const rel = relative(ROOT, file).replace(/\\/g, "/");
    const text = readFileSync(file, "utf8");
    const lines = text.split(/\r?\n/);
    const stack = []; // { line, expr }
    const protectedLines = new Set(); // 落在 @vort-if..@vort-endif 区间内的行号
    lines.forEach((line, i) => {
        const ln = i + 1;
        if (END_RE.test(line)) {
            if (stack.length === 0) {
                errors.push(`${rel}:${ln}  多余的 @vort-endif（没有对应的 @vort-if）`);
            } else {
                const open = stack.pop();
                for (let k = open.line; k <= ln; k++) protectedLines.add(k);
            }
            return;
        }
        const m = line.match(OPEN_RE);
        if (m) {
            // 去掉尾部注释闭合符 --> 或 */，再 trim
            const expr = m[1].replace(/-->\s*$/, "").replace(/\*\/\s*$/, "").trim();
            const bad = invalidTokens(expr);
            if (bad.length) {
                errors.push(`${rel}:${ln}  未知特性名 [${bad.join(", ")}]，允许集合见 feature-manifest.json`);
            }
            stack.push({ line: ln, expr });
        }
    });
    for (const open of stack) {
        errors.push(`${rel}:${open.line}  @vort-if ${open.expr} 缺少对应的 @vort-endif`);
    }

    // 该文件本身若在裁剪清单内，会随特性一起删除，其内部引用无需再校验
    if (matchCut(rel)) return;

    // 保留文件不得在 @vort-if 块外静态引用“会被裁剪”的模块，否则该特性关闭时必然 ENOENT
    for (const { spec, line } of scanImports(text)) {
        const relImport = normalizeImport(spec, file);
        if (!relImport) continue;
        const hit = matchCut(relImport);
        if (!hit) continue;
        if (protectedLines.has(line)) continue;
        errors.push(
            `${rel}:${line}  未包 @vort-if 却静态引用了「${hit.feature}」特性模块 ${spec}（命中裁剪项 ${hit.inc}）；` +
                `请用 @vort-if ${hit.feature} 包裹该 import，或确认此文件应随 ${hit.feature} 一并裁剪`
        );
    }
}

const files = collectFiles(ROOT, []);
const errors = [];
for (const f of files) checkFile(f, errors);

// feature-manifest.json 的 includes 路径必须真实存在（防止裁剪清单里出现拼错/失效路径）
for (const { path: inc } of MANIFEST_INCLUDES) {
    if (!existsSync(join(ROOT, inc))) {
        errors.push(`feature-manifest.json  includes 路径不存在：${inc}`);
    }
}

if (errors.length) {
    console.error(`\n[vort-if] 校验未通过，共 ${errors.length} 处问题：\n`);
    for (const e of errors) console.error("  ✗ " + e);
    console.error("\n修复后再构建。语法见 docs/o2o大融合-需求方案.md §3.3。\n");
    process.exit(1);
}

console.log(`[vort-if] 校验通过（扫描 ${files.length} 个文件；manifest includes ${MANIFEST_INCLUDES.length} 项路径均存在；允许特性：${[...ALLOWED].join(", ")}）`);
