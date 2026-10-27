# Java Admin → React + Semi 迁移策略

## 现状量化

旧系统源码：

```text
Vue 文件：1170
列表页：156
编辑页：104
其他页面/弹窗/组件：910
API 模块：194
```

继续按页面手写不可行。必须先把“重复结构”抽象掉，再按模块并行迁移。

## 核心思路

不是“把 Vue 翻译成 React”，而是：

```text
旧源码 = 需求与字段说明书
Semi 后台 = 统一页面引擎 + 业务配置
```

大部分后台页面都是同一类结构：

```text
筛选区 + 表格 + 分页 + 新增/编辑抽屉 + 批量操作 + 状态切换
```

这类页面用配置生成，而不是每个页面重写。

## 目标架构

```text
src/framework/
  ListPage.tsx       列表页引擎：筛选、分页、工具栏、批量操作
  FormDrawer.tsx     表单抽屉引擎：锚点、分组、横向标签、字段渲染
  FieldRenderer.tsx  字段渲染器：input/select/date/number/switch/upload/editor
  apiAdapter.ts      旧接口路径/参数适配
  types.ts           PageConfig / FieldConfig / ColumnConfig
src/business/
  product/config.ts
  order/config.ts
  content/config.ts
  ...
src/components/
  ImageUploader.tsx
  RichTextEditor.tsx
  RemoteSelect.tsx
  CategorySelect.tsx
  BrandSelect.tsx
  RegionSelect.tsx
  GalleryPicker.tsx
```

一个普通列表页最终应接近：

```ts
export default () => (
  <ListPage
    title="品牌管理"
    api="/product/brand/list"
    rowKey="brandId"
    filters={[
      { field: 'keyword', label: '品牌名称', type: 'input' },
      { field: 'isShow', label: '显示状态', type: 'select', options: showOptions },
    ]}
    columns={[
      { title: '品牌名称', dataIndex: 'brandName' },
      { title: '排序', dataIndex: 'sortOrder' },
    ]}
    form={brandForm}
  />
);
```

## 组件映射

| 旧组件 | Semi 方案 |
| --- | --- |
| el-input | Form.Input |
| el-input-number | Form.InputNumber |
| el-select | Form.Select / RemoteSelect |
| el-date-picker | Form.DatePicker |
| el-switch | Form.Switch |
| el-radio-group | Form.RadioGroup |
| el-checkbox-group | Form.CheckboxGroup |
| el-table | Table |
| el-dialog | Modal |
| el-drawer | SideSheet |
| el-tabs | Tabs |
| el-tree | Tree |
| el-upload | ImageUploader / Upload |
| 富文本 | RichTextEditor |
| 相册选择 | GalleryPicker |

## 迁移阶段

### 阶段 1：迁移引擎（优先）

```text
ListPage
FormDrawer
FieldRenderer
API adapter
通用选择器/上传/富文本
```

目标：后续页面只写配置和少量自定义交互。

### 阶段 2：模块并行

按旧系统模块拆分，每个模块一个独立负责人/Agent：

```text
商品 product
订单 order
会员 user
内容 content
营销 promotion
财务 finance
设置 setting
店铺/商户 shop/merchant
```

每个模块只改自己的 `src/pages/<module>` 和 `src/business/<module>`，共享框架不改。

### 阶段 3：复杂页面

只对复杂页面做手工迁移：

```text
商品编辑
订单详情/发货
营销活动
装修
多规格/属性
权限
```

### 阶段 4：回归

```text
tsc/build
Playwright 登录 + 页面冒烟
关键页面截图
接口 200/业务码校验
```

## 自动抽取

已提供：

```bash
node tools/extract-admin-inventory.mjs
```

输出：

```text
docs/admin-migration-inventory.json
```

包含每个旧页面的：

```text
文件路径
页面类型（list/form/other）
API 模块
API 方法
表单 label
prop / v-model 字段
```

后续可基于它生成页面配置骨架，再由 AI 补齐。

## 页面迁移审计

迁移完成后必须跑：

```bash
node tools/audit-admin-page.mjs user/user/List rust/web/admin-semi/src/pages/Members.tsx
```

脚本会对照旧页面清单，输出：

```text
旧页面标签 → 新页面缺失标签
旧页面字段 → 新页面缺失字段
```

## PageSpec 功能审计（强制）

每个页面必须有一份 spec：

```text
rust/web/admin-semi/src/pages/specs/members.json
```

包含：

```text
旧页面路径
Drawer 宽度
筛选项
表格列
行操作
批量操作
排序
后端未就绪能力
```

运行：

```bash
node tools/audit-page-spec.mjs rust/web/admin-semi/src/pages/specs/members.json
```

必须输出：

```text
PASS: PageSpec 校验通过
```

否则该页面不能算迁移完成。

这样能避免“页面看似做完，实际漏筛选/漏字段/漏操作”的问题。

迁移完成标准：

```text
1. audit 脚本无高优先级缺失
2. PageSpec 审计 PASS
3. 抽屉宽度与原版一致或明确说明
4. 新增/编辑字段与 $t label 对照完成
5. 列表筛选、列、行操作、批量操作逐项对照
6. 真实登录 + 页面截图验证
```

## 并行方式

```text
Lead：框架、路由、布局、共享组件、最终集成
Agent A：商品模块
Agent B：订单模块
Agent C：会员模块
Agent D：内容模块
Agent E：营销模块
Agent F：财务/设置模块
```

约束：

```text
每个 Agent 只写自己的目录
共享组件由 Lead 先完成
每批完成后 build + 页面冒烟
```

## 预期速度

当前手写：约 1-3 页/轮，且容易漏字段。

迁移引擎完成后：

```text
简单列表页：30-60 行配置
标准表单页：100-200 行配置
复杂页面：手工迁移
```

配合并行 Agent，整体速度可以提升 5-10 倍，重点是减少重复劳动，而不是让 AI 盲目翻译 1170 个文件。
