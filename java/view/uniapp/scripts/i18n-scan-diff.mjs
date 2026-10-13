#!/usr/bin/env node
/**
 * 把 i18n-scan.mjs 的两份结果做交叉判定，压掉噪声、给出可执行的分级清单。
 *
 * 判定依据（本项目「中文原文即 key」）：一个中文串只要在任何一处被 t()/$t() 包裹过，
 * 它就一定是可翻译的用户可见文案；那么同一个串在别处裸露，就是确定的漏包裹。
 *
 * 分级：
 *   A1  显示文案裸露，且同文案在基线跨境项目 / 本项目其它位置已包裹  → 确定漏翻译
 *   A2  显示文案裸露，融合项目独有，基线无对照                      → 疑似漏翻译
 *   A3  显示文案裸露，独立跨境项目同样裸露                          → 同源历史问题
 *   B1  标识符/比较用途，但同文件里同一文案又被 $t() 包裹           → i18n 一致性缺陷
 *   B2  纯标识符/枚举值                                            → 无需翻译
 *
 * 用法：node scripts/i18n-scan-diff.mjs <目标.json> <基线.json> [--out 报告.txt]
 */

import { readFileSync, writeFileSync, existsSync } from "node:fs";
import { resolve, dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const args = process.argv.slice(2);
const outIdx = args.indexOf("--out");
const OUT = outIdx >= 0 ? args[outIdx + 1] : "";
const [targetPath, baselinePath] = args.filter((a, i) => i !== outIdx && i !== outIdx + 1);
if (!targetPath || !baselinePath) {
    console.error("用法：node scripts/i18n-scan-diff.mjs <目标.json> <基线.json> [--out 报告.txt]");
    process.exit(2);
}

const target = JSON.parse(readFileSync(resolve(targetPath), "utf8"));
const baseline = JSON.parse(readFileSync(resolve(baselinePath), "utf8"));

const norm = (s) => s.trim().replace(/\s+/g, "").replace(/[：:]$/, "");

const dict = new Set();
for (const w of [...target.wrapped, ...baseline.wrapped]) dict.add(norm(w.text));

const wrappedInFile = (list) => {
    const m = new Map();
    for (const w of list) {
        if (!m.has(w.file)) m.set(w.file, new Set());
        m.get(w.file).add(norm(w.text));
    }
    return m;
};
const baselineWrappedByFile = wrappedInFile(baseline.wrapped);
const targetWrappedByFile = wrappedInFile(target.wrapped);
const baselineBare = new Set(baseline.findings.map((f) => norm(f.text)));

const IGNORE_FILE = join(dirname(fileURLToPath(import.meta.url)), "i18n-scan-ignore.json");
const ignores = existsSync(IGNORE_FILE) ? JSON.parse(readFileSync(IGNORE_FILE, "utf8")).entries : [];
const matchRule = (f) =>
    ignores.find(
        (r) =>
            f.file.startsWith(r.file) &&
            (!r.texts || r.texts.some((t) => norm(t) === norm(f.text))) &&
            (!r.usage || r.usage === f.usage)
    );

const buckets = { A1: [], A2: [], A3: [], B1: [], B2: [], D: [], IGN: [] };

for (const f of target.findings) {
    const rule = matchRule(f);
    if (rule) {
        buckets[rule.bucket === "D" ? "D" : "IGN"].push(f);
        continue;
    }
    const n = norm(f.text);
    const identLike = f.usage === "compare" || f.usage === "ident" || f.usage === "indirect" || f.soft;

    if (identLike) {
        const level = targetWrappedByFile.get(f.file)?.has(n) ? "B1" : "B2";
        buckets[level].push(f);
        continue;
    }
    if (baselineWrappedByFile.get(f.file)?.has(n)) buckets.A1.push({ ...f, why: "基线同名文件已包裹" });
    else if (dict.has(n)) buckets.A1.push({ ...f, why: "同文案在别处已包裹" });
    else if (baselineBare.has(n)) buckets.A3.push(f);
    else buckets.A2.push(f);
}

const groupByFile = (list) => {
    const m = new Map();
    for (const f of list) {
        if (!m.has(f.file)) m.set(f.file, []);
        m.get(f.file).push(f);
    }
    return [...m.entries()].sort((a, b) => b[1].length - a[1].length);
};

const LABEL = {
    A1: "A1 确定漏翻译：显示文案裸露，同文案在基线或本项目别处已被 $t() 包裹",
    A2: "A2 疑似漏翻译：显示文案裸露，融合项目独有，基线无对照",
    A3: "A3 同源历史问题：独立跨境项目同样裸露，不是本次融合引入",
    B1: "B1 待确认：中文当标识符/比较值用，同文件里同一文案又被 $t() 包裹",
    B2: "B2 标识符 / 枚举值 / 渲染时经 safeT() 兜底：不需要处理",
    D: "D 延后：与 C / B1 同簇的中文状态值，单独包裹会破坏等值比较，须连同比较逻辑一起改",
    IGN: "IGN 已人工核实的误报（见 scripts/i18n-scan-ignore.json）"
};

const lines = [];
const say = (s = "") => lines.push(s);

say("# O2O-Uniapp 跨境版本未包裹中文扫描报告");
say();
say(`目标：${target.root}（scope=${target.scope}）`);
say(`基线：${baseline.root}（已上线的独立跨境项目，用于判断某文案是否本就该翻译）`);

const compareWithT = (target.compareWithT ?? []).filter(
    (c) =>
        !ignores.some(
            (r) =>
                c.file.startsWith(r.file) &&
                (!r.texts || r.texts.some((t) => (c.code || "").includes(t)))
        )
);

if (compareWithT.length) {
    say();
    say("=".repeat(78));
    say("C 与 $t() 结果做等值比较：状态值多为中文原文，切英文后比较必然失配");
    say(`共 ${compareWithT.length} 处`);
    say("=".repeat(78));
    for (const c of compareWithT) {
        say(`  ${c.file}:${c.line}`);
        say(`         ${c.code}`);
    }
}

const dynamicKeys = (target.dynamicKeys ?? []).filter((d) => !ignores.some((r) => d.file.startsWith(r.file)));
if (dynamicKeys.length) {
    say();
    say("=".repeat(78));
    say("E 带占位符的 key：一旦语言包没有该 key，vue-i18n 原样返回 key 且不做插值，界面会显示字面 {0}");
    say(`共 ${dynamicKeys.length} 处`);
    say("  concat       = key 由字符串拼接而成，任何语言包都不可能收录，必然失配（确定缺陷）");
    say("  unregistered = key 自带占位符但未登记到 src/locale/zh.json，是否失配取决于服务端语言包（需人工确认）");
    say("  普通 key 失配只是回落成中文，尚可接受；带占位符的 key 失配会露出 {0}，所以单列一段");
    say("  修法：确保 key 已登记进 src/locale/zh.json 与 en.json；拼接式改成 tFormat(\"共{0}张\", [n])");
    say("=".repeat(78));
    for (const d of dynamicKeys) {
        say(`  ${d.file}:${d.line}  [${d.kind}] ${d.text}`);
        say(`         ${d.code}`);
    }
}

for (const level of ["A1", "A2", "B1", "D", "A3", "B2", "IGN"]) {
    const list = buckets[level];
    say();
    say("=".repeat(78));
    say(`${LABEL[level]}`);
    say(`共 ${list.length} 处 / ${groupByFile(list).length} 文件`);
    say("=".repeat(78));
    for (const [file, items] of groupByFile(list)) {
        say();
        say(`${file}  (${items.length})`);
        for (const f of items.sort((a, b) => a.line - b.line)) {
            const tag = f.kind === "attr" ? `attr:${f.attr}` : f.kind;
            say(`  L${String(f.line).padEnd(5)} [${tag}] ${f.text}`);
            if (f.code) say(`         ${f.code}`);
        }
    }
}

const summary =
    `A1=${buckets.A1.length}  A2=${buckets.A2.length}  A3=${buckets.A3.length}  ` +
    `B1=${buckets.B1.length}  B2=${buckets.B2.length}  D=${buckets.D.length}  IGN=${buckets.IGN.length}  ` +
    `C=${compareWithT.length}  E=${dynamicKeys.length}`;

say();
say(`汇总：${summary}（目标共 ${target.findings.length} 处裸露中文，字典 ${dict.size} 条）`);

const text = lines.join("\n");
if (OUT) {
    writeFileSync(resolve(OUT), `\ufeff${text}`, "utf8");
    console.log(`report: ${resolve(OUT)}  ${summary}`);
} else {
    console.log(text);
}
