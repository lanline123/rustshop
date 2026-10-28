# Admin Semi 迁移引擎使用手册

给其他 AI / 开发者的入口文档。按本文件执行即可直接开始迁移，不需要重新理解整个项目。

## 1. 引擎位置

```text
rust/web/admin-semi/
  src/framework/ListPage.tsx       列表页引擎
  src/framework/FormDrawer.tsx     表单抽屉引擎
  src/framework/FieldRenderer.tsx  字段渲染引擎
  src/pages/AutoConfigPage.tsx     配置页引擎
  src/layout/AdminLayout.tsx       菜单、主题、布局
  src/config/adminMenu.tsx         菜单与页面标题
  src/pages/specs/*.json           PageSpec 页面契约
  scripts/verify-*.cjs             交互/视觉回归脚本
tools/audit-page-spec.mjs          PageSpec 审计
tools/audit-admin-page.mjs         旧页面标签/字段审计
docs/admin-semi-migration-strategy.md  总体迁移策略
```

## 2. 其他 AI 直接开工命令

```bash
cd rust/web/admin-semi
npm install
npm run dev -- --host 127.0.0.1 --port 3002
```

登录：

```text
admin / admin123
```

或者：

```text
http://127.0.0.1:3002/?demo=1
```

完成一个页面后必须跑：

```bash
npm run build

cd ../..
node tools/audit-page-spec.mjs rust/web/admin-semi/src/pages/specs/<spec>.json
node tools/audit-admin-page.mjs <旧页面路径不带.vue> rust/web/admin-semi/src/pages/<页面>.tsx
```

配置页额外跑：

```bash
cd rust/web/admin-semi
npm run verify:config
node scripts/verify-login-dependencies.cjs
```

## 3. 页面类型与选择

### 普通列表页

使用：

```text
ListPage + FormDrawer
```

参考：

```text
src/pages/FriendLinks.tsx
src/pages/TaxRates.tsx
src/pages/ShippingTypes.tsx
```

必须提供 PageSpec，包含：

```text
filters / columns / actions / batch / table
```

### 复杂控件

先查 Semi MCP 文档，再写 PageSpec：

```json
{
  "widgets": {
    "authorityList": {
      "type": "authority-select",
      "component": "rust/web/admin-semi/src/components/AuthorityTree.tsx",
      "requires": ["authority-select-head", "authority-child-more"],
      "mcpChecks": ["Checkbox", "Dropdown"]
    }
  },
  "interactionChecks": [
    "rust/web/admin-semi/scripts/verify-login-dependencies.cjs"
  ]
}
```

审计会检查：

```text
widgets 是否存在
mcpChecks 是否记录
requires 实现标记是否存在
interactionChecks 脚本是否存在
```

### 配置页

使用 `AutoConfigPage`，不要再手写一套表单。

在 `src/App.tsx` 的 `configPages` 中登记：

```ts
pageKey: {
  title: '页面标题',
  getApi: '/setting/config/xxxSettings',
  saveApi: '/setting/config/saveXxx',
}
```

## 4. AutoConfigPage 声明式能力

### sections

来自 `src/config/configSections.ts`，用于按原版分组和字段顺序渲染。

⚠️ 原版分组标题可能使用单引号或双引号：

```vue
<div class="title">{{ $t('基础设置') }}</div>
<div class="title">{{ $t("微信登录") }}</div>
```

解析时必须同时支持，否则整页会落到“其它”。

### controls

```ts
controls: {
  navTheme: 'segmented',
  primaryColor: 'color',
  layout: 'segmented',
  versionInfoHidden: 'radio',
  storageType: 'options',
  openWechatOauth: 'radio',
}
```

支持：

```text
radio        是/否（固定 1/0）
options      自定义选项（保留数字/字符串原始值）
segmented    按钮式单选
color        颜色色块
switch       默认 0/1 开关
```

### options

```ts
options: {
  storageType: [
    { label: '本地存储', value: 0 },
    { label: '阿里云 OSS', value: 1 },
    { label: '腾讯云 COS', value: 2 },
  ],
  navTheme: [
    { label: '浅色', value: 'light' },
    { label: '深色', value: 'dark' },
  ],
}
```

⚠️ `RadioGroup` 默认值和 option 值必须类型一致：

```text
数字 1 + 字符串 "1" ⇒ 选项看似存在，但不会选中
```

### rules

```ts
rules: {
  storageLocalUrl: { visibleWhen: { field: 'storageType', equals: 0 } },
  storageOssUrl: { visibleWhen: { field: 'storageType', equals: 1 } },
  wechatRegisterBindPhone: {
    disabledWhen: {
      any: [
        { field: 'openWechatOauth', equals: 0 },
        { field: 'openWechatPcLogin', equals: 0 },
      ],
    },
  },
}
```

### fieldGroups

多个字段作为同一模块显示/隐藏：

```ts
fieldGroups: [
  {
    title: '公众号授权配置',
    description: '配置微信公众号授权登录所需的 AppID、密钥与服务器校验信息。',
    tag: '微信',
    icon: '微',
    fields: ['wechatAppId', 'wechatAppSecret', 'wechatServerToken', 'wechatServerSecret'],
    visibleWhen: { field: 'openWechatOauth', equals: 1 },
  },
]
```

注意：

```text
fieldGroup 渲染在所属 section 内部
如果要求与“微信登录”同级，应拆成独立 section，而不是 fieldGroup
```

### fieldHelp

字段下方小字：

```ts
fieldHelp: {
  storageOssUrl: '参考格式： https://oss.***.com/ ，域名结尾必须带斜杠 /',
  storageOssAccessKeyId: '编辑完成后，点击下方提交按钮确认修改，请确保填写正确的 AccessKeyId',
}
```

有帮助文本时，字段必须顶部对齐，保持控件原位置：

```css
.auto-config-field:has(.auto-config-field-help) {
  align-items: start;
}
```

### hiddenFields

```ts
hiddenFields: ['storageSaveFullPath']
```

仅用于原版已隐藏、接口仍返回的遗留字段。

## 5. 本次踩过的坑（必须避免）

### 1. 后端字段命名不一致

数据库 `child_auth` 同时存在：

```json
{"authName":"编辑","authSn":"..."}
{"auth_name":"编辑","auth_sn":"..."}
```

Rust DTO 必须加 `serde(alias = "auth_name")`，并补双格式测试。

### 2. 自定义控件只读 values

`formApi.setValue` 不保证自定义控件立即重渲染。必须使用：

```text
src/framework/useControlledField.ts
```

否则复选框/权限树“点不动”。

### 3. 单选值类型不匹配

```text
storageType 数字 1
RadioGroup value="1"
```

表现为数据正确但未选中。`options` 控件必须传原始类型。

### 4. 帮助文本导致控件偏移

字段行默认 `align-items: center`，加入帮助文本后整块居中，输入框会相对标签上移。

修复：

```css
.auto-config-field:has(.auto-config-field-help) {
  align-items: start;
}
```

### 5. 开关与标签不在一条中线

开关只有 24px，标签行高按输入框算。

修复：

```css
.auto-config-field:has(.semi-switch) > label {
  padding-top: 0;
  line-height: 44px;
}

.auto-config-field:has(.semi-switch) .semi-switch {
  transform: translateY(2px);
}
```

### 6. 原版条件字段不能平铺

存储设置必须按 `storageType` 只显示对应云厂商字段，否则会出现多个“图片访问域名”。

### 7. 只做选项，没有做功能

后台风格不能只保存 `navTheme/primaryColor/layout`，必须调用：

```text
src/theme.ts applyAppearance
```

并切换：

```text
body[theme-mode="dark"]
--primary
app-shell layout-top / layout-mix
AdminLayout 顶部/混合布局
adminLightLogo
```

### 8. 原版顺序被接口返回顺序打乱

`sections.fields` 决定顺序，不能直接 `entries.filter(...)`。

### 9. 二级菜单字体/滚动条

- 隐藏滚动条但保留滚动：`scrollbar-width:none` + `::-webkit-scrollbar{display:none}`
- 分组标题与菜单项统一 13.5px

### 10. Logo 点击

左上角品牌位应返回 dashboard：

```ts
onClick={() => onPageChange('dashboard')}
```

## 6. 完成定义（Definition of Done）

```text
npm run build 通过
PageSpec PASS
audit-admin-page 旧标签/字段缺失 0
交互脚本 PASS
真实登录截图正常
表单页 verify-form-layout PASS
配置页 verify:config PASS
```

## 7. 给其他 AI 的启动提示词

可以直接把下面这段发给其他 AI：

```text
你在 D:\tigshop\java-b2b2b-pro-v5.8.57 工作。
先读 docs/admin-semi-migration-engine.md，严格按其中的迁移引擎执行。
旧后台源码在 java/view/admin/src，新后台在 rust/web/admin-semi。
先判断页面类型：
- 普通 CRUD 用 ListPage + FormDrawer
- 配置页用 AutoConfigPage
- 复杂控件先查 Semi MCP 文档并写 widgets/mcpChecks/interactionChecks
完成后必须：
cd rust/web/admin-semi && npm run build
cd ../.. && node tools/audit-page-spec.mjs <对应spec.json>
node tools/audit-admin-page.mjs <旧页面路径> rust/web/admin-semi/src/pages/<新页面>.tsx
并跑对应 verify 脚本。
不要跳过 PageSpec、审计和真实登录验证。
```
