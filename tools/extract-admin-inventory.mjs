#!/usr/bin/env node
/**
 * 扫描旧版 Java Admin 的 Vue 页面，抽取迁移所需的 API、表单字段和列表字段。
 * 输出 docs/admin-migration-inventory.json，供 AI/开发按模块并行迁移。
 */
import fs from 'node:fs';
import path from 'node:path';

const ROOT = path.resolve(process.argv[2] || 'java/view/admin/src');
const VIEWS = path.join(ROOT, 'views');
const OUT = path.resolve('docs/admin-migration-inventory.json');

function walk(dir) {
  const out = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) out.push(...walk(full));
    else if (entry.name.endsWith('.vue')) out.push(full);
  }
  return out;
}

function unique(values) {
  return [...new Set(values.filter(Boolean))];
}

function extract(file) {
  const source = fs.readFileSync(file, 'utf8');
  const rel = path.relative(VIEWS, file).replace(/\\/g, '/');
  const kind = /(^|\/)List\.vue$/i.test(rel)
    ? 'list'
    : /(^|\/)Info\.vue$/i.test(rel)
      ? 'form'
      : 'other';
  const apis = unique(
    [...source.matchAll(/from\s+["']@\/api\/([^"']+)["']/g)].map((m) => m[1]),
  );
  const apiNames = unique(
    [...source.matchAll(/import\s*\{([^}]+)\}\s*from\s*["']@\/api\//g)]
      .flatMap((m) => m[1].split(','))
      .map((s) => s.trim())
      .filter(Boolean),
  );
  const labels = unique(
    [
      ...[...source.matchAll(/:label="\$t\('([^']+)'\)"/g)].map((m) => m[1]),
      ...[...source.matchAll(/label="([^"]+)"/g)].map((m) => m[1]),
      ...[...source.matchAll(/:label="'([^']+)'"/g)].map((m) => m[1]),
    ].map((s) => s.trim()),
  );
  const props = unique(
    [...source.matchAll(/\bprop="([A-Za-z0-9_.]+)"/g)].map((m) => m[1]),
  );
  const models = unique(
    [...source.matchAll(/v-model(?::[A-Za-z]+)?="([A-Za-z0-9_.]+)"/g)].map((m) => m[1]),
  );
  return { rel, kind, apis, apiNames, labels, props, models };
}

const files = walk(VIEWS);
const pages = files.map(extract);
const byModule = {};
for (const page of pages) {
  const module = page.rel.split('/')[0] || 'root';
  byModule[module] ??= { total: 0, list: 0, form: 0, other: 0, pages: [] };
  byModule[module].total += 1;
  byModule[module][page.kind] += 1;
  byModule[module].pages.push(page);
}

const summary = {
  generatedAt: new Date().toISOString(),
  source: ROOT,
  totals: {
    vue: pages.length,
    list: pages.filter((p) => p.kind === 'list').length,
    form: pages.filter((p) => p.kind === 'form').length,
    other: pages.filter((p) => p.kind === 'other').length,
  },
  modules: Object.fromEntries(
    Object.entries(byModule)
      .sort((a, b) => b[1].total - a[1].total)
      .map(([name, value]) => [
        name,
        { total: value.total, list: value.list, form: value.form, other: value.other },
      ]),
  ),
  pages,
};

fs.mkdirSync(path.dirname(OUT), { recursive: true });
fs.writeFileSync(OUT, JSON.stringify(summary, null, 2));
console.log(`扫描完成: ${summary.totals.vue} 个 Vue 页面`);
console.log(`列表页 ${summary.totals.list}，编辑页 ${summary.totals.form}，其他 ${summary.totals.other}`);
console.log(`输出: ${OUT}`);
