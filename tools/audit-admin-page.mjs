#!/usr/bin/env node
/**
 * 对照旧 Vue 页面，审计当前 React 页面是否漏掉字段/标签。
 *
 * 用法：
 *   node tools/audit-admin-page.mjs user/user/List rust/web/admin-semi/src/pages/Members.tsx
 */
import fs from 'node:fs';
import path from 'node:path';

const [, , pageRel, componentPath] = process.argv;
if (!pageRel || !componentPath) {
  console.error('用法: node tools/audit-admin-page.mjs <旧页面相对路径> <新页面文件>');
  process.exit(1);
}

const inventoryPath = path.resolve('docs/admin-migration-inventory.json');
if (!fs.existsSync(inventoryPath)) {
  console.error('缺少 docs/admin-migration-inventory.json，请先运行 tools/extract-admin-inventory.mjs');
  process.exit(1);
}

const inventory = JSON.parse(fs.readFileSync(inventoryPath, 'utf8'));
const normalized = pageRel.replace(/\\/g, '/').replace(/^\/+/, '');
const page =
  inventory.pages.find((item) => item.rel === normalized) ||
  inventory.pages.find((item) => item.rel === `${normalized}.vue`) ||
  inventory.pages.find((item) => item.rel.endsWith(normalized));

if (!page) {
  console.error(`未找到旧页面: ${normalized}`);
  process.exit(1);
}

const source = fs.readFileSync(path.resolve(componentPath), 'utf8');
const ignored = new Set([
  '全部',
  '操作',
  '确定',
  '取消',
  '提交',
  '搜索',
  '重置',
  '查看',
  '编辑',
  '删除',
  '状态',
  '启用',
  '禁用',
  '更多',
]);

const clean = (value) =>
  String(value)
    .replace(/\$t\('([^']+)'.*/, '$1')
    .replace(/[{}$]/g, '')
    .trim();

const labels = [...new Set(page.labels.map(clean).filter((label) => label && !ignored.has(label)))].filter(
  (label) => !/^[A-Za-z]/.test(label) && !label.includes('{') && !label.includes('`'),
);
const missingLabels = labels.filter(
  (label) => !source.includes(label) && !source.includes(label.replace(/\*$/, '')),
);

const props = [...new Set(page.props)].filter(Boolean);
const missingProps = props.filter((prop) => !source.includes(prop));

console.log(`旧页面: ${page.rel}`);
console.log(`新页面: ${componentPath}`);
console.log(`旧标签: ${labels.length}，新页面缺失: ${missingLabels.length}`);
if (missingLabels.length) {
  console.log('\n可能缺失的标签：');
  missingLabels.forEach((label) => console.log(`  - ${label}`));
}
console.log(`\n旧字段 prop: ${props.length}，新页面缺失: ${missingProps.length}`);
if (missingProps.length) {
  console.log('\n可能缺失的字段：');
  missingProps.forEach((prop) => console.log(`  - ${prop}`));
}
