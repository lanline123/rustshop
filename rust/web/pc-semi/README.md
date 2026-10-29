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

已完成：

```text
Next.js App Router 骨架
SSR 首页 /api/home/home/pcIndex
装修 JSON 类型契约
新版模块注册表
PC 公共头部
图片广告 / 图文导航 / 图片魔方 / 热区 / 公告 / 标题 / 分割线 / 空白
快捷导航 / 分类导航 / 营销通栏 / 商品 / 推荐商品 / 商品分组
优惠券 / 秒杀 / 拼团 / 分组 Banner（基础版）
商品模块接 /api/product/product/list
```

待补齐：

```text
商品详情 / SKU
分类 / 搜索
购物车
结算 / 支付
订单 / 售后
会员中心
活动完整交互
IM
OAuth 回调
多语言
SEO 细节 / sitemap
装修模块完整样式 parity
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
