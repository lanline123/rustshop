#!/usr/bin/env node
/**
 * @vort-if 标记配对 / 嵌套 / 特性名合法性校验脚本
 *
 * 背景与依据：需求方案 §3.3「教训3 校验前置」+「教训2 单一数据源」。
 * 单一数据源 = .vort/feature-manifest.json（合法特性名从此文件推导，禁止另建清单）。
 *
 * 校验项：
 *   1. 配对：每个 @vort-if 必须有匹配的 @vort-endif（error）
 *   2. 嵌套：不得出现多余的 @vort-endif、不得有未闭合的 @vort-if（error）
 *   3. 特性名：@vort-if 表达式里的特性标识必须是 manifest 中登记的合法特性（error）
 *   4. manifest 路径存在性：includes 里登记的路径必须真实存在（warn-only，首版不阻断）
 *
 * 退出码：存在 error → 1；仅 warn 或全绿 → 0。
 * 挂载：首版通过 `npm run vort:check` 手动/CI 调用，暂不进 prebuild（零阻断）。
 */

import { readFileSync, existsSync, readdirSync, statSync } from "node:fs";
import { join, extname, dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = resolve(__dirname, "..");
const SRC = join(ROOT, "src");
const MANIFEST = join(ROOT, ".vort", "feature-manifest.json");

const SCAN_EXTS = new Set([".vue", ".ts", ".js", ".mjs", ".json", ".wxs"]);
const IF_RE = /@vort-if\b([^\n]*?)(?:-->|\*\/|$)/;
const IF_GLOBAL = /@vort-if\b/;
const ENDIF_RE = /@vort-endif\b/;

const errors = [];
const warnings = [];

/** 去掉 JSONC 注释后解析 */
function parseJsonc(text) {
    const noBlock = text.replace(/\/\*[\s\S]*?\*\//g, "");
    const noLine = noBlock.replace(/(^|[^:])\/\/.*$/gm, "$1");
    return JSON.parse(noLine);
}

/** 从 manifest 递归收集合法特性名（含点号子特性），如 o2o、o2o.cycle、common.pay.applePay */
function collectFeatures(node, prefix, set) {
    if (!node || typeof node !== "object") return;
    for (const [key, val] of Object.entries(node)) {
        if (key === "includes") continue;
        if (key === "modules") {
            collectFeatures(val, prefix, set);
            continue;
        }
        const name = prefix ? `${prefix}.${key}` : key;
        set.add(name);
        if (val && typeof val === "object") collectFeatures(val, name, set);
    }
}

/** 收集 manifest 里所有 includes 路径，做存在性校验 */
function collectIncludes(node, out) {
    if (!node || typeof node !== "object") return;
    for (const [key, val] of Object.entries(node)) {
        if (key === "includes" && Array.isArray(val)) {
            for (const p of val) out.push(p);
        } else if (val && typeof val === "object") {
            collectIncludes(val, out);
        }
    }
}

/** 表达式里提取特性标识：按 & | ! ( ) 空白切分 */
function extractIdents(expr) {
    return expr
        .replace(/[()!&|]/g, " ")
        .split(/\s+/)
        .map((s) => s.trim())
        .filter(Boolean);
}

function walk(dir, files) {
    for (const name of readdirSync(dir)) {
        if (name === "node_modules" || name === ".git") continue;
        const full = join(dir, name);
        const st = statSync(full);
        if (st.isDirectory()) walk(full, files);
        else if (SCAN_EXTS.has(extname(name))) files.push(full);
    }
}

function checkFile(file, legalFeatures) {
    const text = readFileSync(file, "utf8");
    if (!IF_GLOBAL.test(text) && !ENDIF_RE.test(text)) return;
    const lines = text.split(/\r?\n/);
    const stack = [];
    lines.forEach((line, i) => {
        const lineNo = i + 1;
        // 一行可能同时含 if / endif，逐 token 处理（按出现次序）
        const tokens = [];
        let m;
        const scan = new RegExp("@vort-(if\\b[^\\n]*?(?=-->|\\*/|$)|endif\\b)", "g");
        while ((m = scan.exec(line)) !== null) {
            tokens.push({ raw: m[0], idx: m.index });
        }
        for (const tk of tokens) {
            if (/^@vort-endif\b/.test(tk.raw)) {
                if (stack.length === 0) {
                    errors.push(`${rel(file)}:${lineNo}  多余的 @vort-endif（无匹配的 @vort-if）`);
                } else {
                    stack.pop();
                }
            } else {
                const exprMatch = tk.raw.match(IF_RE);
                const expr = exprMatch ? exprMatch[1].trim() : "";
                stack.push({ lineNo, expr });
                if (!expr) {
                    errors.push(`${rel(file)}:${lineNo}  @vort-if 缺少特性表达式`);
                } else {
                    for (const id of extractIdents(expr)) {
                        if (!legalFeatures.has(id)) {
                            errors.push(`${rel(file)}:${lineNo}  未知特性名 "${id}"（不在 feature-manifest.json 中）：${expr}`);
                        }
                    }
                }
            }
        }
    });
    for (const open of stack) {
        errors.push(`${rel(file)}:${open.lineNo}  未闭合的 @vort-if（缺少 @vort-endif）：${open.expr}`);
    }
}

function rel(p) {
    return p.replace(ROOT + "\\", "").replace(ROOT + "/", "").replace(/\\/g, "/");
}

function main() {
    if (!existsSync(MANIFEST)) {
        console.error(`[vort:check] 找不到 manifest：${MANIFEST}`);
        process.exit(1);
    }
    const manifest = parseJsonc(readFileSync(MANIFEST, "utf8"));

    const legalFeatures = new Set();
    collectFeatures(manifest, "", legalFeatures);

    // manifest 路径存在性（warn-only）
    const includes = [];
    collectIncludes(manifest, includes);
    for (const p of includes) {
        const abs = join(ROOT, p);
        const withVue = abs.endsWith(".vue") || abs.endsWith(".ts") || abs.endsWith(".d.ts");
        const exists = existsSync(abs) || existsSync(`${abs}.vue`) || existsSync(`${abs}.ts`) || existsSync(`${abs}.d.ts`);
        if (!exists && !withVue) {
            warnings.push(`manifest includes 路径不存在：${p}`);
        } else if (!exists) {
            warnings.push(`manifest includes 路径不存在：${p}`);
        }
    }

    const files = [];
    walk(SRC, files);
    // pages.json 在 src 下已被扫到；如有主包 pages.json 亦覆盖
    for (const f of files) checkFile(f, legalFeatures);

    console.log(`[vort:check] 合法特性名：${[...legalFeatures].sort().join(", ")}`);
    console.log(`[vort:check] 扫描文件数：${files.length}`);

    if (warnings.length) {
        console.log(`\n[vort:check] 警告（${warnings.length}，不阻断）：`);
        for (const w of warnings) console.log(`  ⚠ ${w}`);
    }
    if (errors.length) {
        console.error(`\n[vort:check] 错误（${errors.length}）：`);
        for (const e of errors) console.error(`  ✗ ${e}`);
        console.error(`\n[vort:check] 校验未通过。`);
        process.exit(1);
    }
    console.log(`\n[vort:check] 配对/嵌套/特性名校验通过。`);
}

main();
