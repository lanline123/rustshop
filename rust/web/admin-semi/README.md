# 万物新生 Admin（React + Semi）

基于 `Vite + React 18 + TypeScript + Semi UI` 重新设计的运营后台，功能对齐 Java Admin。

## 开发

```bash
npm install
npm run dev
```

默认地址：

```text
http://127.0.0.1:3002/
```

开发服务器已代理：

```text
/adminapi -> http://127.0.0.1:8080
/static   -> http://127.0.0.1:8080
```

## 登录

- 使用现有管理员账号登录 `/adminapi/login/signin`
- 没有账号或后端未启动时，可访问：

```text
http://127.0.0.1:3002/?demo=1
```

进入演示模式，使用内置演示数据。

## 已接入真实接口

```text
GET /adminapi/panel/panel/index          数据看板
GET /adminapi/product/product/list       商品列表
GET /adminapi/order/order/list           订单列表
GET /adminapi/user/user/list             会员列表
GET /adminapi/content/article/list       文章列表
GET /adminapi/content/articleCategory/list 文章分类
```

## 已实现页面

```text
登录
数据看板
商品管理（新增 / 编辑 / 上下架 / 删除，右侧抽屉，5 个区块：基本信息 / 销售信息 / 物流配送 / 商品详情 / 高级信息；支持图片上传、多规格 SKU、富文本详情）
订单管理（列表 / 详情 / 备注 / 改价 / 取消 / 发货，右侧抽屉）
会员管理
内容管理（文章 / 分类）
```

以下模块已预留菜单，带“规划中”标记：

```text
售后管理
营销中心
财务中心
系统设置
```

## 构建

```bash
npm run build
```

产物：

```text
dist/
```

## UI 自动验证

先启动 dev server，再执行：

```bash
npm run verify:ui
```

脚本会使用本机 Edge 真实登录，验证看板、订单列表、订单详情抽屉，并输出截图。

## 说明

- 当前是 B 方案：借 Semi 重新设计，功能对齐 Java Admin。
- 商品、订单、会员、文章列表已使用真实接口；新增/编辑弹窗、详情页、权限控制仍待迁移。
- 演示模式数据在 `src/mock.ts`。
