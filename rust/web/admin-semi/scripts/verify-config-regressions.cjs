const { chromium } = require('playwright-core');

const BASE = 'http://127.0.0.1:3002';
const failures = [];

async function login(page) {
  await page.goto(`${BASE}/`, { waitUntil: 'networkidle' });
  await page.getByPlaceholder('请输入管理员账号').fill('admin');
  await page.getByPlaceholder('请输入密码').fill('admin123');
  await page.getByRole('button', { name: '登录' }).click();
  await page.waitForTimeout(2400);
}

async function fieldByLabel(page, label) {
  return page.locator('.auto-config-field').filter({ hasText: label }).first();
}

async function main() {
  const browser = await chromium.launch({
    executablePath: 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
    headless: true,
  });
  const page = await browser.newPage({ viewport: { width: 1440, height: 1100 } });
  page.on('pageerror', (error) => failures.push(`运行时报错: ${error.message}`));
  await login(page);

  // 1. switch 与标签中心线对齐
  await page.goto(`${BASE}/?page=baseBasicManage`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1200);
  const switchMetrics = await page.evaluate(() => {
    const field = [...document.querySelectorAll('.auto-config-field')].find(
      (item) => item.querySelector('label')?.innerText === '经营许可证开关',
    );
    if (!field) return null;
    const label = field.querySelector('label');
    const control = field.querySelector('.semi-switch');
    if (!label || !control) return null;
    const labelStyle = getComputedStyle(label);
    const labelRect = label.getBoundingClientRect();
    const controlRect = control.getBoundingClientRect();
    return {
      labelCenter:
        labelRect.top + parseFloat(labelStyle.paddingTop) + parseFloat(labelStyle.lineHeight) / 2,
      controlCenter: controlRect.top + controlRect.height / 2,
    };
  });
  if (!switchMetrics) failures.push('未找到经营许可证开关字段');
  else if (Math.abs(switchMetrics.labelCenter - switchMetrics.controlCenter) > 2.5) {
    failures.push(
      `开关与标签未对齐: label=${switchMetrics.labelCenter}, switch=${switchMetrics.controlCenter}`,
    );
  }

  // 2. 存储类型联动 + 帮助文本位置
  await page.goto(`${BASE}/?page=globalSetting`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1600);
  const storage = page.locator('.auto-config-section').filter({ hasText: '存储设置' });
  await storage.getByText('腾讯云 COS', { exact: true }).click();
  await page.waitForTimeout(250);
  let labels = await storage.locator('.auto-config-field > label').allInnerTexts();
  if (!labels.includes('SecretId') || labels.includes('AccessKeyId')) {
    failures.push('腾讯云 COS 存储字段未按类型联动');
  }
  await storage.getByText('本地存储', { exact: true }).click();
  await page.waitForTimeout(250);
  labels = await storage.locator('.auto-config-field > label').allInnerTexts();
  if (labels.length !== 2 || !labels.includes('图片访问域名')) {
    failures.push('本地存储字段未按类型联动');
  }
  await storage.getByText('阿里云 OSS', { exact: true }).click();
  await page.waitForTimeout(250);

  const helpMetrics = await page.evaluate(() => {
    const field = [...document.querySelectorAll('.auto-config-field')].find(
      (item) => item.querySelector('label')?.innerText === '图片访问域名' && item.querySelector('.auto-config-field-help'),
    );
    if (!field) return null;
    const label = field.querySelector('label');
    const input = field.querySelector('.semi-input-wrapper');
    const help = field.querySelector('.auto-config-field-help');
    if (!label || !input || !help) return null;
    const labelStyle = getComputedStyle(label);
    const labelRect = label.getBoundingClientRect();
    const inputRect = input.getBoundingClientRect();
    const helpRect = help.getBoundingClientRect();
    return {
      labelCenter:
        labelRect.top + parseFloat(labelStyle.paddingTop) + parseFloat(labelStyle.lineHeight) / 2,
      inputCenter: inputRect.top + inputRect.height / 2,
      helpTop: helpRect.top,
      inputBottom: inputRect.bottom,
    };
  });
  if (!helpMetrics) failures.push('未找到图片访问域名帮助文本');
  else {
    if (Math.abs(helpMetrics.labelCenter - helpMetrics.inputCenter) > 2.5) {
      failures.push('有帮助文本时，控件与标签中心线不一致');
    }
    if (helpMetrics.helpTop < helpMetrics.inputBottom) {
      failures.push('帮助文本未显示在控件下方');
    }
  }

  // 3. 订单设置：售后审核联动自动同意退货\n  await page.goto(${BASE}/?page=baseOrderManage, { waitUntil: 'networkidle' });\n  await page.waitForTimeout(1200);\n  const review = page.locator('.auto-config-field').filter({ hasText: '售后审核' }).first();\n  const autoReturn = page.locator('.auto-config-field').filter({ hasText: '自动同意退货申请' });\n  if ((await review.locator('.semi-switch-checked').count()) > 0) {\n    await review.locator('.semi-switch').click();\n    await page.waitForTimeout(250);\n  }\n  if ((await autoReturn.count()) !== 0) failures.push('关闭售后审核后，自动同意退货申请仍然显示');\n  await review.locator('.semi-switch').click();\n  await page.waitForTimeout(250);\n  if ((await autoReturn.count()) !== 1) failures.push('开启售后审核后，自动同意退货申请未显示');\n\n  // 4. 不再把所有存储字段平铺
  if (labels.length > 8) failures.push('存储设置仍存在字段平铺');

  await page.screenshot({ path: 'preview-config-regressions.png' });
  await browser.close();

  if (failures.length) {
    console.error('FAIL');
    failures.forEach((item) => console.error(`  - ${item}`));
    process.exit(1);
  }
  console.log('PASS: 配置页开关/帮助文本/存储联动检查通过');
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
