import fs from 'node:fs';
import path from 'node:path';

const source = path.join(process.cwd(), 'node_modules/@douyinfe/semi-ui/dist/css/semi.min.css');
const target = path.join(process.cwd(), 'app/semi.css');

if (!fs.existsSync(source)) {
  throw new Error(`Semi CSS not found: ${source}`);
}

fs.copyFileSync(source, target);
console.log('[pc-semi] copied Semi CSS to app/semi.css');
