# 跨境外贸风 H5 皮肤（overseas.h5Style）· 专项任务书

> 里程碑：P2 专项（由 M5 D1=c 拆出）· 开关：`isOverseas()` + `@vort-if overseas.h5Style` · 依赖：M4（tig-button2 已迁）
> 落地仓库：`Tigshop-All/Tigshop-o2o-Uniapp`
> 来源：[m5-overseas-header-countdown/README.md](../m5-overseas-header-countdown/README.md) §〇 D1=c、对比清单 §七
> 状态：**Wave 0 + Wave 1 执行中（2026-07-24）**

---

## 一、专项目标

把跨境独有的"外贸风"顶部栏 `components/mainHeader`(590行) 迁入 o2o，并按 `isOverseas()` 让核心页在跨境构建下渲染外贸风头部；纯 o2o / 标准构建（`VITE_IS_OVERSEAS=0`）经 `@vort-if overseas.h5Style` 整块裁切，行为零变化。

## 二、决策（用户 2026-07-24 拍板）

- **scope = components_first**：本次只做「组件迁入 + 前 3 个易接入页（index / cart / search）」验证，其余热点页分波后续。
- **productCate = shared_wrapper**：productCate（o2o 无 topHeader.vue、是 styleOne/Two/ThreeCate 三变体）留到最后一波，新建共享 `topHeader.vue` 壳，三 style 在 isOverseas 分支统一走它。

## 三、S2 三仓实测结论（2026-07-24）

- `mainHeader`(590行) 跨境独有；依赖 `configStore`（shopLogo/menuButtonInfo/**commonHeaderHeight**）、`userStore`（token/userInfo/logout）、`cartStore`（cartCount/fetchCartCount）、`getMemberCenter` API、`tig-button2`、`redirect/getElementRect/staticResource/imageFormat`、iconfont（caidan/gouwuche4/xinjianwenbenwendang/houtui）、主题变量（--main-text/--main-bg/--safe-top）。
- o2o 依赖齐备性核实：除 **`configStore.commonHeaderHeight` + `setCommonHeaderHeight`** 缺失外，其余全部具备（getMemberCenter L22、tig-button2 已迁、staticResource/getElementRect 在 utils、iconfont 四字形在、主题变量在 store/theme）。
- `@/static/images/common/close.png` 与 `right_arrows.png` **两仓均无实体文件**（跨境源码硬编码 `@/static/...`，`uni build` 视为硬错误）→ **调整**：改走 `staticResource('common/*.png')`（CDN），与本组件已有的 `default-user.png` 取法一致（属修正源码不一致，非重构）。
- `components/countdown`(217行) 实测**不被 mainHeader 消费**（跨境在商详页单独用）→ 不在 Wave 0 迁，留到 product 波。
- 8 页中 7 页可薄接入（在现有 `tig-layout`/`tig-navbar` 上方加或互斥换 mainHeader）；productCate 需三变体改造。

## 四、门控策略（沿用 M6 惯例）

- 组件文件：整文件 `@vort-if overseas.h5Style`。
- 页面内：`mainHeader` import 与模板分支包 `@vort-if overseas.h5Style` 块标记；模板分支用页级 `const isOverseasSkin = isOverseas()`（定义在 `@vort-if` 内，裁切后引用落 `undefined`→falsy→分支隐藏，与既有 wholesale 裁切模式一致）。
- `configStore.commonHeaderHeight` 为通用 store 字段（不打开关，o2o 构建下仅为未用的 0 值，无害）。

## 五、波次

| 波 | 范围 | 状态 |
|---|---|---|
| **Wave 0** | config.ts 补 commonHeaderHeight；mainHeader 迁入（整文件 @vort-if）；manifest 登记；双构建门 | 执行中 |
| **Wave 1** | index / cart / search 三页 isOverseas 接入；双构建 + OVERSEAS=1 h5 视觉自检 | 执行中 |
| **Wave 2** | product：mainHeader 叠 productNav 上方 + 迁 countdown + commonHeaderHeight→productNav 偏移变量链 | 完成 |
| **Wave 3** | search/result + seckill/list + user/order/list（title 模式） | 完成 |
| **Wave 4** | productCate：新建共享 topHeader 壳，三 style 走它 | 完成 |

## 六、排除（out of scope）

- decorate countdown 三方合并（product-style-border）：沿用 M5 D2=a 保留 o2o 现状。
- 支付（M2/M7）无关。

## 七、验证记录

- **Wave 0（2026-07-24）**：
  - `store/config.ts` 加 `commonHeaderHeight` state + `setCommonHeaderHeight` action（加性、无开关）。
  - `components/mainHeader/index.vue` faithful 迁入（path-based 整文件裁切，无 in-file 标记，对齐 tig-button2/langSet 惯例）；close/right_arrows 两图改走 `staticResource`。
  - `feature-manifest.json` `overseas.h5Style.includes = ["src/components/mainHeader"]`。
  - 门：`vort:check` 通过（647 文件、`overseas.h5Style` 合法、标记配对）；3 文件零 lint。
- **Wave 1（2026-07-24）**：
  - index/index.vue：`tig-layout` 首子节点加 `<mainHeader v-if="isOverseasSkin" position="sticky">`（import + const 分置 @vort-if，满足 import/first）。
  - cart/index.vue：`mainHeader v-if="isOverseasSkin"` ↔ `tig-navbar v-if="!isOverseasSkin"` 互斥（裁切后 tig-navbar 兜底）。
  - search/index.vue：`:title="isOverseasSkin ? '' : navbarTitle"`（overseas 去 title 避免双头部）+ `<mainHeader no-safe-top>`。
  - 门：`vort:check` 通过；3 页零 lint；`build:h5` 通过（DONE，编译的是外贸风开启的完整源码）。
  - 说明：`build:h5`=`uni build` 不做 @vort-if 裁切，编译=特性 ON 路径；OVERSEAS=0 裁切路径安全由 `vort:check` 标记配对保证（沿用 M6/M8 验证模型）。

- **Wave 2（2026-07-24）**：
  - `components/countdown/index.vue`（217行）faithful 迁入（去掉 2 行 1s 定时器内 `console.log` 调试噪声，对齐 o2o drop_console）；`overseas.h5Style.includes` 追加 `src/components/countdown`。
  - `product/index.vue`：`v-show="!loadEndStatus"` 内 `productNav` 上方加 `<mainHeader v-if="isOverseasSkin" position="sticky">`（gated import + const，未触碰 R1 composables / M8 批发块）。
  - `product/src/productNav.vue`：`.product-nav` 加 `:style="{ top: configStore.commonHeaderHeight + 'px' }"`（对齐跨境 productNav 偏移；o2o 构建 commonHeaderHeight=0 → top:0 = 原 fixed 行为，零变化；overseas 下 mainHeader 挂载写高度 → productNav 下移到其下方）。
  - 门：`vort:check` 通过（648 文件）；4 文件零 lint；`build:h5` 通过（DONE）。
  - 已知差异（留后续视觉波）：o2o productNav 是 hero 图上的**透明浮层**（白图标/半透背景），overseas 是 mainHeader 下的**实心导航**。本波只做结构叠放 + top 偏移，productNav 在 overseas 态的实心化配色未做（属视觉皮肤化，避免整页顶替，独立处理）。countdown 已作为皮肤资产迁入，其在 o2o 价格区的挂载点（跨境 seckill/discount 结束时间）属外贸风价格区渲染，与 R1/M8 热点重叠，未在本波接入。

- **Wave 3（2026-07-24）**：
  - `search/result.vue`：`:title="isOverseasSkin ? '' : navbarTitle"` + `<mainHeader class="main-header" position="sticky">`；`.header`（`position:fixed; top:var(--nav-height)`）overseas 态改 `:style="{ top: commonHeaderHeight+'px' }"` 避免与 mainHeader 重叠（o2o 态 undefined → 走 CSS 默认，零变化）。
  - `seckill/list.vue`：`.seckill-title` banner 上方预置 `<mainHeader v-if="isOverseasSkin" position="sticky">`（banner 在流内，无需偏移）。
  - `user/order/list.vue`：`:title="isOverseasSkin ? '' : '我的订单'"` + `<mainHeader position="sticky" title="我的订单">`（title 模式走 mainHeader #navbar 槽）。
  - 门：`vort:check` 通过（648 文件）；3 页零 lint；`build:h5` 通过（DONE）。
  - 已知差异（留视觉波）：result.vue `.header` 是 fixed（o2o 原设计），overseas 参照实现是 sticky；本波只保证不重叠，滚动时的 sticky/fixed 行为差异属视觉皮肤化，未拉平。

- **Wave 4（2026-07-24）**：productCate 硬骨头，`shared_wrapper` 决策落地。
  - 新建 `src/pages/productCate/src/topHeader.vue`（faithful 移植跨境同名壳：`mainHeader position="sticky"` + `#navbar` 搜索槽；`placeholder` prop 接分类页装修占位）；搜索图标 `oversea_sousuo.png` o2o 无本地文件 → 改走 `staticResource('home/oversea_sousuo.png')`（对齐 Wave 0 close/right_arrows 处理，保证 build:h5 不断）。
  - `feature-manifest.json` `overseas.h5Style.includes` 追加 `src/pages/productCate/src/topHeader.vue`（page-src 壳整文件路径裁切，真实 o2o 构建剔除，避免孤儿文件引用被裁的 mainHeader）。
  - `styleOneCate.vue`：`tig-navbar` ↔ `topHeader` 互斥；内联 `.header` 搜索行 overseas 态 `v-if="!isOverseasSkin"` 隐藏（topHeader 自带搜索条，避免双搜索）。
  - `styleTwoCate.vue` / `styleThreeCate.vue`：`tig-navbar` + `<search>` 组合 overseas 态整体隐藏，改走 `<topHeader :placeholder="searchPlaceholder">`。
  - 三 style 均加 gated `import topHeader from "./src/topHeader.vue"` + `import { isOverseas }` + `const isOverseasSkin = isOverseas()`。
  - 门：`vort:check` 通过（649 文件）；topHeader + 三 style 零 lint；`build:h5` 通过（DONE）。styleTwo/Three 出现的 `useProductStyle` 循环 reexport 警告为既有模式（多页共有），非本波引入。
  - 已知差异（留视觉波）：topHeader 搜索条为跨境实心边框样式；o2o 三 style 原搜索交互（styleOne 内联 icon-sousuo、styleTwo/Three 的 search 组件 needMenu 菜单）在 overseas 态被 topHeader 取代，菜单入口差异属外贸风信息架构，未拉平。

## 十、后续（视觉收尾 待确认）
- 视觉皮肤化收尾（独立小波）：productNav overseas 实心配色、result.vue `.header` sticky 化、countdown 价格区接入、productCate search 菜单入口在 overseas 下的取舍。
