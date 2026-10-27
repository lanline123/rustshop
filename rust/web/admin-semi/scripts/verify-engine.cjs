const { chromium } = require('playwright-core');

(async () => {
  const browser = await chromium.launch({
    executablePath: 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
    headless: true,
  });
  const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } });
  page.on('pageerror', (err) => console.log('PAGEERROR', err.message));

  await page.goto('http://127.0.0.1:3002/', { waitUntil: 'networkidle' });
  await page.getByPlaceholder('请输入管理员账号').fill('admin');
  await page.getByPlaceholder('请输入密码').fill('admin123');
  await page.getByRole('button', { name: '登录' }).click();
  await page.waitForTimeout(2500);

  await page.getByText('用户', { exact: true }).click();
  await page.waitForTimeout(2000);
  console.log('member rows:', await page.locator('.semi-table-tbody .semi-table-row').count());

  await page.getByText('添加客户', { exact: true }).click();
  await page.waitForTimeout(800);
  console.log('drawer:', await page.locator('.semi-sidesheet-inner').count());
  await page.screenshot({ path: 'preview-members-engine.png' });

  await browser.close();
})();
