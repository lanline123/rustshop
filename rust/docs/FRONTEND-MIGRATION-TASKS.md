# 前端迁移任务清单（admin-web / pc-web → 对齐 Rust 后端）

> **目的**：把「重构后的两个前端」与「原版 `java/view/{admin,Tigshop-Pc}`」之间的差距，
> 拆成**可按域认领、可验收、可并行**的工作单元。
>
> **前提（本清单的基线）**：后端已是 Rust（`rust/crates`），契约覆盖率
> `1564/1564 = 100%`（`python rust/tools/check_api_coverage.py`）。
> **因此本清单只讨论前端缺口；后端接口不需要再造，只需前端对准契约。**
>
> **审计日期**：2026-10-20　·　**审计基线 HEAD**：`84659770`
> **口径**：所有「原版 X / 重构 Y」均为源码静态实测，复现命令见 §8。
>
> **✅ 已确认的决策（2026-10-20，业务方拍板）**：
> - **D0 路线**：重写前端（不是「前端不改」）。
> - **D2 UI 库**：**只用 Element Plus**，不引入 ant-design-vue；版本用最新。
> - **D3**：（随 D2）采用最新版 Element Plus。
> - **D5 SEO**：PC 端开启 SEO 优化。
> - **D6 目录**：前端放 `rust/web/{admin,pc}`，与 `rust/crates`（Rust workspace）区分开。
> - **Schema-Driven**：采纳「元数据驱动」方案，但**定位为分层能力**（见 §12），
>   覆盖约 25–30% 的 CRUD 页面，其余业务交互页按域手写。
> - **元数据前置**：接受先做 Rust DTO 结构化元数据标注。

---

## 0. 结论摘要（先看这一节）

**当前两个前端不是 1:1 复刻，是原型级 Demo。**

| 指标 | 原版 admin | 重构 admin-web | 原版 PC | 重构 pc-web |
|---|---|---|---|---|
| `.vue` 文件 | 1329 | **9** | 430 | **10** |
| 源码行数 | 406,431 | **4,522**（1.1%） | 145,953 | **3,109**（2.1%） |
| 可迁移页面（`views`/`pages` 下） | 1166 个 `.vue` / 282,236 行 | **9 个 / 3,316 行** | 200 个 / 68,008 行 | **10 个 / 2,867 行** |
| 业务路由 | 394 path / 316 懒加载页 | **15 业务页** | 105 路由页 | **6**（5.7%） |
| 业务域目录 | 28 | **4 个 view** | 20 | **6 个 view** |
| 接口调用去重 | 1217 | **31**（4 条路径错） | 223 | **16**（2 条路径错） |
| pinia store | 18 | **0** | 15 | **0** |
| i18n | zh 603KB + en 674KB | **无** | 162 key / `$t()` 2915 次 | **无** |

**五个必须先修的正确性缺陷**（不是「缺功能」，是「做错了」，详见 §7）：
接口路径错 6 条、参数命名 snake/camel 错配、订单状态码错、装修数据结构错、批量删除 payload 错。

---

## 1. 工作单元定义与验收

### 1.1 单元粒度

一个任务 = **一个业务域**（对应原版一个 `views/<域>` 或 `pages/<域>` 目录）。
域内再按「页面组」拆子任务。**不要按文件拆**——原版单页常常 500–2000 行，按文件拆无法估时。

### 1.2 每个域的固定交付物

| # | 交付物 | 说明 |
|---|---|---|
| 1 | `src/api/<域>.ts` | 该域全部接口封装；**路径逐字符对齐 `rust/docs/api-contract.json`** |
| 2 | `src/views/<域>/*.vue` | 页面组件，按原版目录结构 1:1 命名 |
| 3 | `src/types/<域>.d.ts` | 请求/响应类型，字段名与 Rust VO 的 `camelCase` 序列化一致 |
| 4 | `src/store/<域>.ts`（如需） | 跨页共享状态 |
| 5 | `src/router` 路由项 | 挂到 `asyncRoutes` 等价结构，`meta.title` 与后端菜单一致 |

### 1.3 验收（每域必过）

```powershell
# 1) 构建
cd admin-web  ; pnpm build        # 或 cd pc-web
# 2) 接口契约比对 —— 0 条未命中
cd ..\rust ; python tools\check_frontend.py contract --app admin-web --strict
# 3) 该域页面在浏览器可打开、列表有数据、增改删有回显
# 4) 与原版页面逐屏对比（截图对照，见 §9）
```

> ⚠️ **`pnpm build` 目前只是 `vue-tsc -b && vite build`，不做契约校验**。
> 必须把 §7.1 的 `check_frontend.py contract --strict` 加进 CI，否则「页面能开但接口 404」会持续混入。

---

## 2. 优先级与批次

| 批次 | 内容 | 依赖 | 估时（人日） |
|---|---|---|---|
| **B0** | §7 全部正确性缺陷修复 + §7.1 契约校验工具 + **§11 架构决策** | 无 | 2–4 |
| **B1** | 平台骨架：动态菜单/权限、pinia、i18n、请求层、通用组件 | B0 | 10–12 |
| **B2** | admin P0 域：`authority` / `panel` / `product` / `setting` | B1 | 30–35 |
| **B3** | admin P1 域：`order` / `finance` / `user` / `merchant` / `shop` / `store` / `adminMerchant` | B2 | 45–50 |
| **B4** | admin P2 域：`decorate` / `promotion` / `content` / `salesman` / `im` / `wholesale` / `gallery` / 小域 | B3 | 60–70 |
| **B5** | PC 交易主链路：装修引擎 → 首页/搜索/详情 → 购物车 → 结算 → 支付 → 订单 | B1 | 35–40 |
| **B6** | PC 其余域：`member` 46 页、`activity`、`joining`、`giftCard`、`shop`、`article`、`chat`、`join`、`seckill`、`coupon`、`exchange` | B5 | 40–45 |
| **B7** | PC 横切：SEO/sitemap/robots/i18n/真 404 | B5 | 10–12 |

**总计约 232–267 人日**（单人）；按 4 人并行约 12–16 周。

> **估时依据**：原版 admin 可迁移 1166 个 `.vue` / 282,236 行，PC 200 个 / 68,008 行，合计 **约 35 万行**。
> 按「有原版可抄、Rust 接口已 100% 就绪」的移植效率 **800–1200 行/人日** 折算 → 290–440 人日纯编码；
> 扣除可复用部分（schema 化 CRUD、通用组件、纯样式页）后取 **232–267 人日**。
> ⚠️ `decorate`（527 个 `.vue` / 117,885 行）与 PC `member`（65 个 / 17,939 行）是两块最大的单体，
> 建议单独排期、不要与其它域混在同一批次里做。

---

## 3. 管理端（admin-web）按域任务

### 3.1 域清单（按工作量降序）

> 口径：`.vue` 数与该域行数**含域内 `src/` 子组件**（它们是页面的一部分，不能漏算）。
> 「路由页」列给出可直接挂路由的页面数，用于拆子任务。

| # | 域 | 原版 `.vue`（含 src） | 原版行数 | 契约接口（adminapi） | 重构现状 | 批次 |
|---|---|---|---|---|---|---|
| A01 | `decorate` | 527 | 117,885 | 60 | 1 个 1078 行编辑器（自造物料） | B4 |
| A02 | `product` | 81 | 26,365 | 140 | 仅分类列表 schema | B2 |
| A03 | `promotion` | 88 | 23,815 | 148 | 仅优惠券列表 schema | B4 |
| A04 | `order` | 61 | 19,941 | 48 | 1 个只读列表 schema | B3 |
| A05 | `setting` | 77 | 19,341 | 218 | 4 个配置表单 schema（2 个路径错） | B2 |
| A06 | `finance` | 55 | 12,155 | 83 | **无** | B3 |
| A07 | `content` | 36 | 8,100 | 40 | 仅文章列表 schema | B4 |
| A08 | `user` | 36 | 8,096 | 66 | **无**（列表路径写错） | B3 |
| A09 | `im` | 22 | 7,024 | 11 | **无** | B4 |
| A10 | `salesman` | 29 | 6,728 | 50 | **无** | B4 |
| A11 | `merchant` | 31 | 6,501 | 83 | 仅店铺列表 schema | B3 |
| A12 | `panel` | 18 | 4,778 | 16 | 1 个 Dashboard（图表字段错） | B2 |
| A13 | `authority` | 22 | 4,151 | 31 | 仅管理员列表 schema | B2 |
| A14 | `wholesale` | 17 | 3,971 | 27 | **无** | B4 |
| A15 | `adminMerchant` | 17 | 3,805 | （归 merchant） | **无** | B3 |
| A16 | `store` | 12 | 3,026 | （归 merchant） | **无** | B3 |
| A17 | `shop` | 11 | 2,418 | 7 | **无** | B3 |
| A18 | `gallery` | 8 | 1,325 | 10 | **无** | B4 |
| A19 | `link` | 6 | 887 | — | **无** | B4 |
| A20 | `login` | 2 | 809 | 16 | 1 个 249 行登录页（缺滑块/手机登录/i18n） | B1 |
| A21 | `install` | 5 | 481 | — | **无** | B4 |
| A22 | `print` | 3 | 411 | 12 | **无** | B4 |
| A23 | `ico` / `sso` | 2 | 223 | — | **无** | B4 |
| A24 | `file` / `organize` / `pos` / `vendor` | 0 | 0 | — | **无** | 空目录，跳过 |
| | **合计** | **1166** | **282,236** | 1231 | **9 个 .vue / 3,316 行** | |

> 契约接口数按 `api-contract.json` 路径首段统计，与前端域非严格 1:1（如 `vendor` 69 条归 `adminMerchant`）。

### 3.2 各域子任务（示例：A04 订单，其余同构）

**A04 `order`（61 个 `.vue` / 19,941 行 / 48 接口）**

| 子任务 | 原版来源 | 说明 |
|---|---|---|
| A04-1 订单列表 | `views/order/order/List.vue` | 状态 tab（0–5）、支付/发货状态筛选、批量发货、导出 |
| A04-2 订单详情 | `views/order/order/src/*`（24 文件） | 改价、改地址、发货、物流跟踪、操作日志、支付记录、子订单 |
| A04-3 售后管理 | `views/order/aftersales/*`（7 文件） | 审核、拒绝、退款、退货收货 |
| A04-4 代客下单 | `views/order/proxyShopping/*`（19 文件） | 选会员 → 选商品 → 代付 |
| A04-5 账期订单 | `views/order/termPay/*`（9 文件） | 账期结算、对账 |
| A04-6 订单导出 | `views/order/orderExport` | Excel 导出（Rust 侧已实现 xlsx） |

> **注意**：A04-2 的 24 个详情子组件里有 8 个是「弹窗/抽屉」，不是独立路由，必须与列表同批交付，否则列表页的按钮全是死链。

### 3.3 平台骨架（B1，所有域的前置）

| # | 任务 | 原版参照 | 验收 |
|---|---|---|---|
| S1 | 动态菜单/路由 | `src/permission.ts` + `utils/menus.ts` + `store/menu.ts` | 用 `authority/getAuthority` 返回的菜单树挂载路由；`adminType` 区分平台/店铺后台 |
| S2 | 按钮级权限 | `utils/authorize.ts` | `v-auth` 指令或 `hasPermission()`，按契约 `permissions[]` 校验 |
| S3 | 多标签页 + keep-alive | `layouts/base/lib/pageTabs.vue` | 打开/关闭/切换不丢状态 |
| S4 | 主题系统 | `store/theme.ts` + `navTheme` | 明/暗主题、布局（topMenu/default）可切 |
| S5 | pinia 基础设施 | `store/*`（18 个） | 至少 app/user/menu/theme 四个基础 store |
| S6 | i18n | `src/i18n/{zh,en}.ts` | `$t()` 可用，语言可切，locale 持久化 |
| S7 | 请求层对齐 | `utils/request.ts` | 见 §7.2 |
| S8 | 通用组件库 | `components/*`（31 目录） | TigTable/TigForm/TigDialog/TigTabs/SvgIcon/MobileCode/Verifition 等 |

---

## 4. PC 商城（pc-web）按域任务

### 4.1 域清单

> 口径：`.vue` 数含域内 `src/`、`components/` 子组件（页面组成部件）。
> 「路由页」列 = 可直接挂路由的页面（排除 `components/`、`src/`）。

| # | 域 | 原版 `.vue`（含 src） | 原版行数 | 路由页 | 重构现状 | 批次 |
|---|---|---|---|---|---|---|
| P01 | `member` 会员中心 | 65 | 17,939 | 46 | 1 页（订单列表+取消） | B6 |
| P02 | `activity` 活动/装修预览 | 16 | 6,753 | 15 | **无** | B6 |
| P03 | `chat` 客服 | 12 | 6,043 | 2 | **无** | B6 |
| P04 | `order` 下单+支付 | 19 | 6,033 | 10 | **无**（按钮只 toast） | B5 |
| P05 | `search` 搜索 | 11 | 5,507 | 1 | 1 页（分类/排序失效） | B5 |
| P06 | `cart` 购物车 | 7 | 4,638 | 1 | 1 页（平铺，无店铺分组） | B5 |
| P07 | `shop` 店铺 | 18 | 4,207 | 4 | **无** | B6 |
| P08 | `item` 商品详情 | 5 | 2,960 | 1 | 1 页（规格/评论/优惠券缺） | B5 |
| P09 | `search.v1` 旧版搜索 | 7 | 2,819 | 1 | **无** | B6 |
| P10 | `join` 入驻 | 9 | 2,780 | 6 | **无** | B6 |
| P11 | `joining` 拼团 | 4 | 2,584 | 2 | **无** | B6 |
| P12 | `article` 文章/帮助 | 6 | 1,947 | 4 | **无** | B6 |
| P13 | `giftCard` 礼品卡 | 4 | 1,346 | 1 | **无** | B6 |
| P14 | `install` 安装向导 | 5 | 528 | 1 | **无** | B6 |
| P15 | `coupon` 优惠券 | 2 | 435 | 2 | **无** | B6 |
| P16 | `seckill` 秒杀 | 1 | 370 | 1 | **无** | B6 |
| P17 | `shopCategory` | 3 | 370 | 1 | **无** | B6 |
| P18 | `exchange` 兑换 | 2 | 261 | 2 | **无** | B6 |
| P19 | `pcMobile` / `list` | 2 | 143 | 2 | **无** | B6 |
| P20 | 根级（`index.vue` / `[...slug].vue`） | 2 | 345 | 2 | 首页有（自造版式），404 页无 | B5 |
| | **合计** | **200** | **68,008** | **105** | **10 个 .vue / 2,867 行** | |

### 4.2 PC 装修引擎（B5 前置，独立任务）

**这是 PC 端最大的单块工作，也是首页能否显示的前提。**

| # | 任务 | 原版参照 | 说明 |
|---|---|---|---|
| P-D1 | 装修数据结构 | `types/decorate/editorModule.d.ts` `DecorateData` | `{ pageModule, moduleList[] }`；`moduleList[] = { type, moduleIndex, isShow, module, style }` |
| P-D2 | 渲染器 | `decorate/components/DecorateRenderer.vue` | 注册表查表 + `isShow` 过滤 + 未注册类型静默丢弃 + async 组件缓存 + `critical-module-count=4` 同步首屏 |
| P-D3 | **17 个 kebab 模块** | `decorate/registry/pc.ts` | `pc-image-ad`、`pc-image-nav`、`pc-image-square-ad`、`pc-image-hotarea`、`pc-product`、`pc-divider`、`pc-spacer`、`pc-notice`、`pc-title-ad`、`pc-home-quick-nav`、`pc-home-coupon`、`pc-home-groupon`、`pc-home-seckill`、`pc-product-group`、`pc-home-hero`、`pc-category-nav`、`pc-recommend-product` |
| P-D4 | **13 个 camelCase 模块**（旧版） | `pages/index.vue` | `pcBanner`、`pcCatWide`、`pcSeckill`、`pcGroupon`、`pcCategoryNav`、`pcCoupon`、`pcCatProductNormal`、`pcCatProductWide`、`pcCatProductSimple`、`pcCustomAd1`、`pcCustomAd2`、`pcImageAd`、`pcRecommendProduct` |
| P-D5 | 别名归一化 | `decorate/normalize.ts` | `pc-category-nav-3→pc-category-nav3`、`pc-white-line→pc-divider`、`pc-white-blank→pc-spacer` |
| P-D6 | 新旧首页分流 | `decorate/isNewHome.ts` | `pageModule.homeStyleVersion` 优先；否则按 type 是否含 `-` 推断 |
| P-D7 | 数据源 | `/api/home/home/pcIndex` | **不是** `/api/decorate/decorate/detail`（见 §7.3） |

**模块组件规模参考**（用于估时）：`PcHomeHero` 2350 行、`PcGrouponLegacy` 2281、`PcSeckillLegacy` 1881、`PcProductGroup` 488、`PcRecommendProduct` 339、`PcProduct` 318。

### 4.3 PC 交易主链路（B5 核心，当前完全缺失）

```
购物车 ──▶ 结算页 order/check ──▶ 支付 order/pay ──▶ 回调 ──▶ 订单详情
  ✅          ❌ 全缺                ❌ 全缺         ❌      ❌
```

| # | 任务 | 原版参照 | 关键点 |
|---|---|---|---|
| P-T1 | 结算页 | `pages/order/check.vue`（1601 行）+ `src/{Address,CouponInfo,IntegralAndBalance,InvoiceInfo,PayType,presaleSteps,RuleInput}` | 调 `order/check/{index,update,updateCoupon,getInvoice,getStoreShippingType}` + `order/check/submit` |
| P-T2 | 地址选择 | `member/address/{List,EditRegion}` + `sys/region/*` | 省市区级联 |
| P-T3 | 优惠券选择 | `member/coupon/List` + `cart/cart/getCouponDiscount` | 试算抵扣 |
| P-T4 | 发票 | `member/userInvoice/*` + `order/check/getInvoice` | 增票资质 |
| P-T5 | 支付页 + 9 渠道 | `pages/order/pay.vue`（552）+ `alipay/huifu/lakala/paypal/wechat/yabanpay/yeepay/yunpay` | 调 `order/pay/{index,create,paymentList,checkStatus}` |
| P-T6 | 支付回调/轮询 | 各渠道页 | `checkStatus` 轮询 + 结果页 |
| P-T7 | 订单详情 | `member/order/Info.vue` + `src/{OrderInfoHeader,OrderProductTable,OrderStatusSteps,OrderDeliveryInfo,OrderEcard,OrderGiftCard}` | 状态步骤条 |
| P-T8 | 售后/退款 | `member/return/{List,Info,Log,LogInfo}` | 调 `user/afterSales/*` |

> **风险提示**：Rust 侧 `payment` 域**全部渠道的 `create` 仍返回「渠道未迁移」**
>（见 `docs/RUN-RUST-FRONTEND.md` §6）。**P-T5 的前端可以先做，但真实支付联调需要渠道凭据**，
> 建议 P-T5 拆成「页面+协议骨架」和「凭据联调」两个子任务分别验收。

---

## 5. 横切能力（两端口共用）

| # | 能力 | 原版 | 重构 | 批次 |
|---|---|---|---|---|
| X1 | i18n | admin zh603K/en674K；PC 162 key | **0** | B1 |
| X2 | pinia 状态 | admin 18 / PC 15 | **0** | B1 |
| X3 | SVG 图标体系 | admin `vite-plugin-svg-icons` + `SvgIcon.vue` | **0** | B1 |
| X4 | 图片 URL 封装 | PC `utils/format.ts` `imageFormat/pickPic/pickPicLarge/pickSkuPic` | **0**（且有 6 处引用不存在的 `/default-goods.png`） | B5 |
| X5 | xlsx 导入导出 | admin `xlsx` 依赖 | 前端 CSV 兜底 | B4 |
| X6 | 富文本 | admin `@wangeditor/editor` | **0** | B4 |
| X7 | 滑块验证码 | admin `vue3-puzzle-vcode` / PC `verifition` | **0** | B1 |
| X8 | WebSocket 客服 | admin `im` + PC `chat` | **0**（Rust 侧 `/ws` 已实现） | B6 |
| X9 | 地图选点 | admin `@amap/amap-jsapi-loader` | **0** | B3 |
| X10 | 二维码 | admin `qrcode` | **0** | B4 |
| X11 | SSR/SEO | PC Nuxt SSR + sitemap + robots + 真 404 | **0** | B7 |
| X12 | 主题/暗色 | admin `store/theme.ts` 双主题 | 硬编码 | B1 |

---

## 6. 重构版已有的「看似可用实则占位」清单

交付前必须清理，否则验收会误判为已完成。

| 位置 | 现状 | 应改为 |
|---|---|---|
| `admin-web/src/schemas/order/orderList.ts:54` | 「查看详情」= `ElMessage.info('查看订单详情: xxx')` | 跳转订单详情页（A04-2） |
| `admin-web/src/views/decorate/DecorateEditor.vue:534-541` | 发布失败 `catch` 里也弹「发布成功」 | 如实报错 |
| `admin-web/src/components/schema/SchemaTable.vue:397` | 批量删除给 `del` 发 `{ids}`（后端要 `{id}`） | 改走 `/batch`（§7.5） |
| `admin-web/src/components/schema/SchemaTable.vue:407` | 「导出」= 前端拼 CSV | 走 Rust xlsx 导出接口 |
| `pc-web/src/views/CartView.vue:190-196` | 「去结算」= toast + 跳 `/member` | 跳结算页（P-T1） |
| `pc-web/src/views/MemberView.vue:185-187` | 「去支付」= 只 toast | 跳支付页（P-T5） |
| `pc-web/src/components/Header.vue:66` | 「全部商品分类」= 跳 `/search` | 分类大下拉（P-D3 `pc-category-nav`） |
| `pc-web/src/components/Header.vue:136` | 「手机端」= toast | 二维码/真链接 |
| `pc-web/src/components/Footer.vue` | 5 列 20 条帮助链接全 `javascript:;` | 接 `friendLinks` / 文章接口 |
| `pc-web/src/views/HomeView.vue:18,30,55` | 只认 3 个自造类型 `pcNewBanner/pcNewCube/pcNewFloor` | 换成 30 个真实类型（P-D3/D4） |
| `pc-web/src/views/ProductDetailView.vue:118` | `product.attrList?.attrList` 路径错 | `attrList.{normal,spe,extra}[].attrList` |
| `admin-web/src/views/decorate/DecorateEditor.vue:387-389` | 物料库是自造的 3+3+3 个类型 | 对齐原版 30 个真实模块类型 |

> `pcNewBanner/pcNewCube/pcNewFloor` 全仓核实：**只存在于 `admin-web` 的编辑器和 `pc-web` 的 HomeView**，
> 原版 `java/view` 命中 0 —— 是重构过程自造的，与真实装修数据不兼容。

---

## 7. 正确性缺陷（B0，必须先修）

这些不是「功能缺失」，是**已经写错**的代码，会让现有页面在对接 Rust 时静默出错或 404。

### 7.1 先补一个契约校验工具（已完成）

工具已落地：**`rust/tools/check_frontend.py`**。

```powershell
cd rust
python tools/check_frontend.py contract              # 两个前端全查
python tools/check_frontend.py contract --app pc-web --strict   # 有未命中则 exit 1
python tools/check_frontend.py scale                 # 按域规模统计
python tools/check_frontend.py decorate              # 装修类型覆盖
```

能力：
- 扫描 `admin-web/src`、`pc-web/src` 里的 `url:` / `apiList:` / `apiSave:` / `apiDelete:` / `switchApi:` 字面量；
- 自动补 `/adminapi` / `/api` 前缀后与 `docs/api-contract.json` 比对；
- 未命中时打印**文件:行号**；
- `--strict` 时 exit 1。

**当前实测结果**（2026-10-20）：
```
admin-web: 调用 31 条，命中 27，未命中 4
pc-web   : 调用 16 条，命中 14，未命中 2
[strict] 6 条接口路径在契约中不存在 —— 必须修正
```

> 建议纳入 CI / `pnpm build` 前置。否则「页面能开但接口 404」会持续混入。

### 7.2 请求层（S7）

| 项 | 原版 | 重构 | 处理 |
|---|---|---|---|
| 认证头 | `Authorization: Bearer <accessToken>` | `Bearer <tig_token>` | 统一；admin 用 `accessToken` 键名 |
| 业务头 | `X-Client-Type`、`X-Locale-Code`、`Secret`（AES 时间戳） | 仅 `X-CLIENT-TYPE`（+ admin 的 `X-ADMIN-TYPE`/`X-Shop-Id`） | Rust 无 Secret 校验 → **可不补 Secret**，但 `X-Locale-Code` 建议补 |
| 响应解包 | `code===0` → 取 `data` | 同 | ✅ 一致 |
| 401 | 清 token + 跳登录 | 同 | ✅ 一致 |
| 错误提示 | 读 `message` | 读 `message` | ✅ 一致（Rust 同时输出 `msg`/`message`） |

### 7.3 错误接口路径（6 条，必须改）

| 前端调用 | 实际契约路径 | 影响 |
|---|---|---|
| `GET /adminapi/member/user/list`<br>`admin-web/src/schemas/member/userList.ts:5` | `GET /adminapi/user/user/list` | 会员列表**永远空** |
| `GET /adminapi/setting/config/payConfigSettings`<br>`admin-web/src/schemas/setting/payment.ts:5` | `GET /adminapi/setting/config/basicPaySettings`（或分渠道 `wechatPaySettings`/`aliPaySettings`/…） | 支付配置页**报错** |
| `POST /adminapi/setting/config/savePayConfig`<br>`admin-web/src/schemas/setting/payment.ts:6` | `POST /adminapi/setting/config/saveBasicPay`（或 `saveWeChatPay` 等） | 保存**报错** |
| `POST /adminapi/decorate/decorate/save`<br>`admin-web/src/views/decorate/DecorateEditor.vue:534` | `POST /adminapi/decorate/decorate/publish` | 发布**404** |
| `POST /adminapi/common/upload/upload`<br>`admin-web/src/components/widgets/ImageUpload.vue:38` | `POST /adminapi/setting/galleryPic/uploadImg`（或 `galleryVideoInfo/uploadVideo`） | 图片上传**404** |
| `GET /api/decorate/decorate/detail`<br>`pc-web/src/views/HomeView.vue:196` | `GET /api/home/home/pcIndex` | 装修数据**取不到** |
| `GET /api/common/config/basic`<br>`pc-web/src/components/Footer.vue:72` | `GET /api/common/config/base` 或 `initConfigSettings` | 页脚电话/ICP**恒空** |

### 7.4 参数命名错配（静默丢参数）

Rust 侧 query DTO 统一 `#[serde(rename_all = "camelCase")]`，前端发的是 snake_case：

| 前端发送 | 后端期望 | 位置 | 后果 |
|---|---|---|---|
| `category_id` | `categoryId` | `pc-web/src/views/SearchView.vue:133` | 分类筛选失效 |
| `sort_by` / `sort_order` | `sortBy` / `sortOrder` | `SearchView.vue:137-144`、`HomeView.vue:226-227` | 排序失效 |
| `order_status` | `orderStatus` | `pc-web/src/views/MemberView.vue:140-142` | 订单 tab 筛选失效 |

> 例外：`size` 在 `ProductListQuery` 里显式加了 `alias = "size"`，可用。

### 7.5 批量删除 payload 错

`admin-web/src/components/schema/SchemaTable.vue:397` 给 `apiDelete` 端点发 `{ ids: [...] }`，
但 Rust 的 `del` 收 `OperateDTO { id }`（`category_del`/`coupon del`/`adminUser del` 均如此）。

- 单个删除 `{ id }` ✅ 正确（`:376`）
- 批量删除应改走 `/batch` + `BatchDTO { ids, type: "del" }`
- 现状：`{ids}` 反序列化后 `id=None` → 服务层报「ID不能为空」或删 0 条

### 7.6 订单状态码错

| 前端判定 | `pc-web/src/views/MemberView.vue:111-115` | Rust/Java 实际 |
|---|---|---|
| 0 | 待付款 | `0 = UNPAID` ✅ |
| 1 | 待发货 | `1 = PAID` ✅ |
| 2 | 配送中 | `2 = SHIPPED` ✅ |
| **3** | **已完成** ❌ | **`3 = CANCELED`** |
| **4** | **已取消** ❌ | **`4 = INVALID`** |
| — | 缺 | **`5 = FINISHED`** |

依据：`rust/crates/services/src/trade/order.rs:27-37`；`java/tigshop-bean/.../enums/order/OrderStatusEnum.java`。
**后果**：已完成订单显示为「已取消」；`orderStatus=3` 的 tab 实际查的是已取消单。

`admin-web/src/schemas/order/orderList.ts:20-26,39-45` 同样是错的（把 3 当已完成、4 当已取消）。

### 7.7 装修数据结构错

`admin-web/src/views/decorate/DecorateEditor.vue:501,526` 把 `data` 传成 `JSON.stringify(数组)` 字符串：

```js
data: JSON.stringify(pageModules.value)   // 前端 body 里是字符串
```

Rust `DecorateSaveDTO.data: Option<serde_json::Value>` 会把它原样存成 **JSON 字符串**；
回读时 `parse_json_opt` 得到 `Value::String` 而非对象，`module_vo` 找的 `pageModule`/`moduleList` 键**不存在**
→ **装修存进去也渲染不出来**。

正确结构（原版 `DecorateData`）：
```json
{ "pageModule": {...}, "moduleList": [ { "type": "...", "moduleIndex": "...", "isShow": true, "module": {...} } ] }
```
`data` 必须是 **JSON 对象**，不是 JSON 字符串。

---

## 8. 复现命令（审计数据来源）

```powershell
cd D:\tigshop\java-b2b2b-pro-v5.8.57\rust

# 接口契约比对（权威）
python tools\check_frontend.py contract
python tools\check_frontend.py contract --strict      # 6 条未命中 → exit 1

# 按域规模统计（含域内 src/ 组件；这是工作量口径）
python tools\check_frontend.py scale

# 装修模块类型覆盖
python tools\check_frontend.py decorate               # 原版 30 种 / 重构 0 种

# 原始规模
(Get-ChildItem -Recurse ..\java\view\admin\src -File -Filter *.vue).Count      # 1329
(Get-ChildItem -Recurse ..\admin-web\src -File -Filter *.vue).Count            # 9
(Get-ChildItem -Recurse ..\java\view\Tigshop-Pc -File -Filter *.vue).Count     # 430
(Get-ChildItem -Recurse ..\pc-web\src -File -Filter *.vue).Count               # 10
```

> 口径铁律：**进度只认接口覆盖率 + 契约比对**，不认「文件存在」或「页面能打开」。

---

## 9. 验收方法（视觉 1:1 对照）

原版已有构建产物可直接对照，无需起 Java 服务：

| 对照源 | 路径 | 用途 |
|---|---|---|
| 原版管理端 dist | `java/public/admin/`（2594 文件） | 逐页截图对照 |
| 原版 PC dist | `java/public/pc/`（2128 文件，含 `_nuxt`） | 逐页截图对照 |
| 原版源码 | `java/view/{admin,Tigshop-Pc}` | 交互/字段级对照 |

**建议流程**：每域交付时，对「原版 dist 页面」与「新页面」各截 1 张同尺寸图，附在 PR 里。
像素级不必强求，但**布局骨架、字段顺序、按钮位置、状态文案必须一致**。

---

## 10. 与既有文档的关系

| 文档 | 关系 |
|---|---|
| `docs/ARCHITECTURE.md` | 后端结构契约（本清单不动后端） |
| `docs/RUN-RUST-FRONTEND.md` | **旧的「前端不改」方案**——已被 D0 决策取代：改为重写前端并对齐契约 |
| `docs/CAPABILITY-MATRIX.md` | 后端能力矩阵；本清单 §4.3 的支付渠道阻塞引用它 |
| `docs/MULTI-TENANT.md` | 前端必须按 `adminType` 区分平台/店铺后台（S1） |
| `docs/domain-roadmap.md` | 后端域路线图（已 100% 完成）；本清单是其前端对应物 |

> ⚠️ **`docs/RUN-RUST-FRONTEND.md` 的「前端代码不改，只改 nginx」路线已被本审计证伪**：
> 原版前端调用 1217 + 223 条接口，而 Rust 契约虽 100% 覆盖，但原版前端并未验证过。
> 若继续沿用「前端不改」，需要逐个验证 1440 条调用的兼容性；重写前端反而工作量更可控。
>
> **✅ 已决策（2026-10-20）：走「重写」路线。** 详见 §11。

---

## 11. 架构决策记录（B0 的一部分）

> 决策日期：2026-10-20　·　决策人：业务方　·　状态：**已定，实施中**

### D0 路线选择：重写 ✅ 已决策

放弃「前端不改、只换后端」方案，按本清单重写 `admin-web` / `pc-web`。
`java/view/{admin,Tigshop-Pc}` 降级为**对照基准**，不再是部署来源。

### D4 Leptos PC 前端下线 ✅ 已完成（2026-10-20）

**问题**：仓库曾有三套 PC 前端并存 —— 原版 Nuxt、`rust/crates/pc-web`（Leptos SSR）、`pc-web/`（Vue SPA）。
Leptos 版通过 `main.rs` 的 `.fallback(tigshop_pc_web::render_app)` 挂在 8181 端口上，
会导致 8181 的 `/` 返回 Leptos 页面，与 Vue SPA 冲突。

**已执行的变更**：

| 文件 | 变更 |
|---|---|
| `rust/crates/pc-web/` | **整目录删除**（266 文件；备份 `rust/backups/pc-web-backup-leptos`，逐字节校验一致） |
| `rust/crates/Cargo.toml` | `members` 移除 `"pc-web"`；移除 `leptos` / `leptos_axum` / `leptos_router` workspace 依赖 |
| `rust/crates/bin/Cargo.toml` | 移除 `tigshop-pc-web` 依赖 |
| `rust/crates/bin/src/main.rs` | 移除 `.fallback(tigshop_pc_web::render_app)`，留注释说明历史与禁止回退 |
| `rust/tools/check_structure.py` | `ALLOWED_DEPS` 移除 `pc-web` 条目及 `bin` 对它的依赖 |
| `rust/deploy/nginx.conf` | 明确 8181 只出 JSON；补充 PC SPA 的 base 路径注意事项 |
| `rust/deploy/README.md` | 前端构建来源改为 `admin-web/` / `pc-web/` |

**验收**：
```powershell
cd rust/crates
cargo check --workspace --all-targets    # Finished，0 error
cd .. ; python tools/check_structure.py --strict   # PASS
python tools/check_api_coverage.py        # 1564/1564 = 100%（无回归）
```

> ⚠️ **不要恢复** `pc-web` crate 或该 fallback。备份仅供查阅历史实现，不作为可运行代码。

### D1–D3、D5、D6 待决策

| # | 决策 | 建议 | 状态 |
|---|---|---|---|
| D1 | 是否建 pnpm workspace + `packages/shared` | **建**。抽 request 基座/类型/常量，避免两端分叉 | 待定 |
| D2 | admin 是否引入 `ant-design-vue` | **引入**。原版 608 处导入 / 416 处 `<a-xxx>` 标签，不引入则需逐个翻译 | 待定 |
| D3 | `element-plus` 版本 | **钉到原版 2.11.7**，或先跑官方 migration guide 比对（当前装 2.14.7） | 待定 |
| D5 | PC 的 SEO 补法 | 沿用原版 `nuxt generate` 思路：构建时预渲染 + 静态 `sitemap.xml`/`robots.txt`/`404.html` | 待定 |
| D6 | 路由 base | admin 部署在 `/admin`（原版 `VITE_BASE_DIR=admin`）、pc 在 `/`；当前 `vite.config.ts` **未设 `base`** | 待定 |

> **D5 说明**：原版 PC 的 `.env.production` 是 `VITE_SSR = 0`，构建产物为 `200.html` + `404.html` + `__NUXT__`
> 注入，即 **SPA 静态生成，不是 SSR**。因此重构版用纯 Vite SPA **与原版生产形态一致**，
> 不算架构倒退；需要补的只是 sitemap/robots/真 404 这三样静态产物。
