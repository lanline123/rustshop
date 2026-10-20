# Admin / PC 前端重构状态（2026-10-20）

> 基线：`rust/web` Vue 3 + Vite + Element Plus + Pinia；后端 Rust 契约 100%。
> 本文件记录本轮“按现有方案重构”的实际完成度与后续任务，避免与旧版
> `FRONTEND-MIGRATION-TASKS.md` 的审计数字混淆。

## 1. 架构

- `rust/web/shared`：统一请求基座（`code === 0` 解包、401 清理 token/跳登录、
  二进制响应原样返回、动态请求头）、通用类型/工具。
- `rust/web/admin`：Schema 驱动后台。
  - `components/schema/{SchemaForm,SchemaTable,types.ts}`：列表/查询/分页、
    行内开关、批量删除、新增/编辑双模式、详情回填、异步 options。
  - `views/business/`：商品、订单、会员、优惠券核心页。
  - `views/setting/*`、`views/panel`、`views/decorate` 保留。
- `rust/web/pc`：独立 Vue SPA。
  - `api/{index,types}.ts`：38 个真实接口封装，逐条对齐契约。
  - `stores/{user,cart}.ts`：Pinia 登录态/购物车角标。
  - `views/*`：首页、搜索、详情、购物车、结算、支付结果、订单、地址、个人中心。
  - `router`：懒加载 + `requiresAuth` + redirect 回跳 + 动态 title。

## 2. 本轮完成

### Admin

- Schema 表单支持 create/update（`apiCreate/apiUpdate/apiDetail`）。
- Schema 表格批量删除统一走 `/batch` + `BatchDTO { ids, type: 'del' }`；
  行内 switch 值统一转字符串。
- 图片上传修正到真实契约
  `/adminapi/setting/galleryPic/uploadImg`，按 `GalleryPicUploadVO.picUrl` 解析。
- 新增业务页：
  - `ProductListView`：列表 + 新增/编辑（分类树/品牌/价格/库存/主图/推荐位）。
  - `OrderListView`：列表 + 详情抽屉 + 发货 + 改价 + 物流时间线。
  - `MemberListView`：列表 + 详情抽屉。
  - `CouponListView`：列表 + 新增/编辑 + 批量删除。
- 路由/侧边栏接线；修正订单状态码、会员列字段。

### PC

- 真实交易链路：
  购物车 → `order/check/{index,update}` → 支付方式 → `order/check/submit`
  → `order/pay/{index,create,checkStatus,getPayLog}` → 订单列表/详情。
- 页面：
  - 首页：`/home/home/pcIndex` 装修数据 + 热销兜底；
  - 搜索：分页/分类/排序/价格区间/URL 同步；
  - 详情：SKU/属性/图册/加购/立即购买；
  - 购物车、结算、支付结果、订单列表/详情、地址 CRUD、个人中心；
  - Header/Footer 真实分类、购物车角标、登录态。
- 移除演示假数据、`javascript:;` 链接、自动成功 toast。
- 修复中文乱码与 `applySeo`/`resolveImageUrl` 构建错误。

### 构建与契约

```powershell
cd rust/web
pnpm --filter @tigshop/admin build   # ✅
pnpm --filter @tigshop/pc build      # ✅
cd ..
python tools/check_frontend.py contract --strict  # ✅ 0 未命中
```

后端：

```text
cargo check --workspace         ✅
tigshop-services 1571/0          ✅
tigshop-admin-api 590/0          ✅
四项门禁 PASS
```

## 3. 当前规模

| 指标 | Admin | PC |
|---|---:|---:|
| `.vue` 文件 | 13 | 16 |
| 路由 name | 16 | 16 |
| 契约调用 | 41/41 命中 | 52/52 命中 |

> 原版 admin 1329 `.vue`、PC 430 `.vue`；当前仍是核心链路，不是 1:1 复刻。

## 4. 未完成（按优先级）

### P0

1. **Admin 订单完整操作**：售后审核、退款、代客下单、账期、导出、
   拆单/改收货人/确认收货。
2. **PC 支付渠道页**：微信/支付宝/汇付等跳转与回调结果页（依赖支付后端）。
3. **PC 结算深化**：发票增票资质、配送方式切换、按店铺拆券、
   部分余额/积分自定义。
4. **前端运行时联调**：启动 Rust 后端 + MySQL，逐页验证列表/详情/提交。

### P1

6. Admin 设置页接入后端 `/setting/config/schema` 动态表单；支付设置拆分为
   微信/支付宝真实端点（当前临时复用微信端点）。
7. Admin 装修引擎 30 个模块逐像素还原（最大单块，527 个原版文件）。
8. PC 会员中心其余 40+ 页：售后/评价/优惠券/收藏/历史/消息/签到/礼品卡等。
9. PC 活动页：秒杀/拼团/优惠券/兑换/店铺/文章。
10. 登录方式：短信、微信扫码、找回密码。

### P2

11. i18n（zh/en）与语言切换。
12. SEO：SSR/sitemap/robots/canonical、真 404。
13. Element Plus 按需引入 + manualChunks，降低 2.5MB/1.28MB chunk。
14. 图片上传后端 `galleryPic/uploadImg` 的真实 OSS 落库（后端仍报对象存储未迁移）。
15. 浏览器 E2E / Playwright 冒烟。

## 5. 后续规则

- 新页面必须先在 `docs/api-contract.json` 找到真实路径，禁止自造接口。
- 优先扩展 Schema 能力（列/表单/子表/图片列表），不要为每个 CRUD 页手写重复代码。
- 业务交互页（订单详情、装修、售后）才手写 Vue。
- 每次提交前必须跑 admin/pc build + `check_frontend.py contract --strict`。
