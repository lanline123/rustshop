import fs from 'node:fs';
import path from 'node:path';

const root = path.resolve(import.meta.dirname, '..');
const menuPath = path.join(root, 'rust/web/admin-semi/src/config/adminMenu.tsx');
const appPath = path.join(root, 'rust/web/admin-semi/src/App.tsx');
const menuSource = fs.readFileSync(menuPath, 'utf8');
const appSource = fs.readFileSync(appPath, 'utf8');

const start = menuSource.indexOf('export const navGroups: NavGroup[] = [');
const end = menuSource.indexOf('export const pageTitles', start);
let body = menuSource.slice(start, end).replace(/<Icon[^>]*\/>/g, 'null');
body = body.slice(body.indexOf('['), body.lastIndexOf(']') + 1);
const groups = Function(`return (${body});`)();

const leaves = [];
const walk = (items, group) => {
  for (const item of items || []) {
    if (item.children?.length) walk(item.children, group);
    else leaves.push({ key: item.key, label: item.label, group });
  }
};
for (const group of groups) walk(group.items, group.label);

const knownMatch = appSource.match(/const knownPages = new Set\(\[([\s\S]*?)\]\);/);
const known = new Set(
  knownMatch ? [...knownMatch[1].matchAll(/'([^']+)'/g)].map((match) => match[1]) : [],
);
const implemented = leaves.filter((leaf) => known.has(leaf.key));
const remaining = leaves.filter((leaf) => !known.has(leaf.key));

const byGroup = {};
for (const leaf of remaining) byGroup[leaf.group] = (byGroup[leaf.group] || 0) + 1;

console.log(
  JSON.stringify(
    {
      menuGroups: groups.length,
      menuLeaves: leaves.length,
      implemented: implemented.length,
      remaining: remaining.length,
      percent: `${((implemented.length / leaves.length) * 100).toFixed(1)}%`,
      remainingByGroup: byGroup,
    },
    null,
    2,
  ),
);
