/**
 * 批量补齐 en.ts 空值：调用 Google 免费翻译接口（client=gtx）
 * 占位符 {xxx} 先替换为哨兵再还原，避免被翻译。
 *
 * 用法：
 *   node scripts/i18n-fill-en.mjs [--limit N] [--concurrency 3] [--dry-run]
 */
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const enPath = path.join(ROOT, "src/i18n/locales/en.ts");

function parseArgs(argv) {
    const opts = { limit: 0, concurrency: 4, dryRun: false, delayMs: 80 };
    for (let i = 2; i < argv.length; i++) {
        const a = argv[i];
        if (a === "--limit") opts.limit = Number(argv[++i]) || 0;
        else if (a === "--concurrency") opts.concurrency = Number(argv[++i]) || 4;
        else if (a === "--delay") opts.delayMs = Number(argv[++i]) || 80;
        else if (a === "--dry-run") opts.dryRun = true;
    }
    return opts;
}

function loadLocale(file) {
    let src = fs.readFileSync(file, "utf8").replace(/^\uFEFF/, "");
    src = src.replace(/^[\s\S]*?export\s+default\s*/, "");
    src = src.replace(/as\s+Record<[\s\S]*?>\s*;?\s*$/, "");
    src = src.trim().replace(/;+$/, "");
    return new Function(`return (${src});`)();
}

function writeLocale(file, obj) {
    const keys = Object.keys(obj).sort((a, b) => a.localeCompare(b, "zh-CN"));
    const entries = keys.map((k) => `    ${JSON.stringify(k)}: ${JSON.stringify(obj[k] ?? "")}`);
    fs.writeFileSync(file, `export default {\n${entries.join(",\n")}\n} as Record<string, string>;\n`, "utf8");
}

function protectPlaceholders(text) {
    const map = [];
    const protectedText = text.replace(/\{[a-zA-Z_][a-zA-Z0-9_]*\}/g, (m) => {
        const token = `__PH${map.length}__`;
        map.push(m);
        return token;
    });
    return { protectedText, map };
}

function restorePlaceholders(text, map) {
    let out = text;
    for (let i = 0; i < map.length; i++) {
        out = out.replace(new RegExp(`__PH${i}__`, "gi"), map[i]);
        // 有时翻译会吃掉下划线或加空格
        out = out.replace(new RegExp(`__\\s*PH\\s*${i}\\s*__`, "gi"), map[i]);
    }
    return out;
}

async function translateOne(text) {
    const { protectedText, map } = protectPlaceholders(text);
    const url =
        "https://translate.googleapis.com/translate_a/single?client=gtx&sl=zh-CN&tl=en&dt=t&q=" +
        encodeURIComponent(protectedText);
    const res = await fetch(url, {
        headers: { "User-Agent": "Mozilla/5.0" }
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    const translated = (data?.[0] || []).map((seg) => seg?.[0] || "").join("");
    return restorePlaceholders(translated, map).trim() || text;
}

async function mapPool(items, concurrency, worker) {
    const results = new Array(items.length);
    let cursor = 0;
    async function run() {
        while (cursor < items.length) {
            const i = cursor++;
            results[i] = await worker(items[i], i);
        }
    }
    await Promise.all(Array.from({ length: Math.min(concurrency, items.length) }, () => run()));
    return results;
}

function sleep(ms) {
    return new Promise((r) => setTimeout(r, ms));
}

async function main() {
    const opts = parseArgs(process.argv);
    const en = loadLocale(enPath);
    let empties = Object.keys(en).filter((k) => en[k] === "" || en[k] == null);
    console.log(`[i18n-fill-en] 空值 ${empties.length} 条`);
    if (opts.limit > 0) empties = empties.slice(0, opts.limit);
    console.log(`[i18n-fill-en] 本次处理 ${empties.length} 条，concurrency=${opts.concurrency}`);

    let ok = 0;
    let fail = 0;
    const failed = [];

    await mapPool(empties, opts.concurrency, async (key, idx) => {
        try {
            const enText = await translateOne(key);
            en[key] = enText;
            ok++;
            if ((idx + 1) % 50 === 0 || idx === empties.length - 1) {
                console.log(`  进度 ${idx + 1}/${empties.length}  ok=${ok} fail=${fail}`);
            }
            if (opts.delayMs) await sleep(opts.delayMs);
        } catch (e) {
            fail++;
            failed.push({ key, error: String(e.message || e) });
            if (fail <= 5) console.warn(`  失败: ${JSON.stringify(key)} -> ${e.message}`);
            await sleep(300);
        }
    });

    console.log(`[i18n-fill-en] 完成 ok=${ok} fail=${fail}`);
    if (opts.dryRun) {
        console.log("dry-run，不写盘。样例：");
        for (const k of empties.slice(0, 10)) console.log(`  ${JSON.stringify(k)} => ${JSON.stringify(en[k])}`);
        return;
    }
    writeLocale(enPath, en);
    let remain = 0;
    for (const k of Object.keys(en)) if (en[k] === "" || en[k] == null) remain++;
    console.log(`[i18n-fill-en] 已写入 ${enPath}，剩余空值 ${remain}`);
    if (failed.length) {
        fs.writeFileSync(path.join(ROOT, ".tmp-i18n-fill-failed.json"), JSON.stringify(failed, null, 2));
        console.log(`[i18n-fill-en] 失败清单已写 .tmp-i18n-fill-failed.json`);
    }
}

main().catch((e) => {
    console.error(e);
    process.exit(1);
});
