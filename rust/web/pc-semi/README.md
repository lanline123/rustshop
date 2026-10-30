# PC Semi（Next.js + React + Semi）

PC 商城新前端。只迁移新版装修模块（`pcHome` kebab-case），旧版 `pcBanner` / `pcCatProductNormal` 等不再进入新渲染器。

## 技术栈

```text
Next.js App Router
React 18
TypeScript
Semi Design
SCSS
```

## 启动

```bash
cd rust/web/pc-semi
npm install
npm run dev
```

默认端口：

```text
http://127.0.0.1:3004
```

后端默认代理到：

```text
https://battery.recy.cc
```

可用环境变量覆盖：

```bash
PC_API_BASE=https://battery.recy.cc npm run dev
```

## 当前范围

线上：

```text
https://battery.recy.cc/      新版 PC
https://battery.recy.cc/pc/   旧版 PC（保留回滚）
```

已完成：

```text
Next.js App Router + SSR
18 个 pcHome 装修模块注册与渲染
首页 / 商品详情 / 搜索 + 排序 + 价格筛选
购物车 / 结算 / 支付页
订单列表 / 订单详情 / 订单取消
会员中心 / 地址 / 优惠券 / 收藏 / 积分 / 余额 / 售后
登录 / 领券中心 / 帮助 / 注册入口
移动端基础适配
```

待补齐：

```text
支付渠道 SDK（后端支付创建未迁移）
文章内容页
短信注册完整流程
IM / OAuth / 多语言 / sitemap
装修模块极细节样式 parity
```

## 装修契约

首页数据来自：

```text
GET /api/home/home/pcIndex
GET /api/home/home/pcIndex?previewId=<id>
```

字段：

```text
pageModule
moduleList[].type
moduleList[].url
moduleList[].moduleIndex
moduleList[].isShow
moduleList[].module
```

注册表：

```text
src/decorate/components/modules.tsx
PC_MODULE_REGISTRY
```

新增模块步骤：

```text
1. src/decorate/components/modules.tsx 增加组件
2. PC_MODULE_REGISTRY 注册 type
3. 用真实装修数据验证
4. 补响应式样式
```

## 只保留新编辑器

新模块 type 为 kebab-case，例如：

```text
pc-home-hero
pc-image-ad
pc-product
pc-home-coupon
pc-home-seckill
pc-category-nav
```

旧模块 camelCase 不再实现：

```text
pcBanner
pcCatProductNormal
pcSeckill
...
```

如果历史页面仍是旧版，由 Admin 重新发布为新版后才会进入本渲染器。

## 目录

```text
app/
  layout.tsx
  page.tsx
  globals.scss
src/
  components/PcHeader.tsx
  decorate/
    types.ts
    components/common.tsx
    components/modules.tsx
  lib/api.ts
```

## 部署

线上已部署到 `https://battery.recy.cc/`，旧 PC 保留 `/pc/`。详见 [DEPLOY.md](./DEPLOY.md)。
