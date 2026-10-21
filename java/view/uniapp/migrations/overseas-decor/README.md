# overseas.decor · 跨境专属装修模块迁移波（任务书 · 待执行）

> **来源**：装修模块三仓横向排查（2026-07-24）发现的「出入二」。o2o 装修分发器 `pages/customPage/modules/index.vue` 缺 6 个跨境独有模块分支，跨境后台配置这些模块类型时 o2o 端 fall-through 渲染空白。
> **配套**：出入一（`module?.isShow` 可见性回归）已于 commit `4aa7213` 修复。本书只覆盖出入二。
> **红线**：一次一模块、迁移≠重构、禁止整页顶替（本波是给分发器**加 v-else-if 分支** + 迁入组件文件，非顶替）、开关置 0 行为不变（全部门控，OVERSEAS=0 裁切后 o2o 零变化）、每 Task 一 commit（≤800 行 / ≤15 文件）。

## 一、缺口清单（跨境 → o2o，实测 2026-07-24）

三仓 `customPage/modules` 目录 diff + 分发器逐分支比对结论：

| 模块 | 行数 | 跨境分发器门控 | 关键依赖（待 S2 核实是否 o2o 已存在） | 建议 o2o 门控 |
|---|---|---|---|---|
| `searchBar2` | 64 | `@vort-if overseas.h5Style` | `formatFrame`(@/components/modules)、`redirect`、iconfont `icon-sousuo2` | `overseas.h5Style` |
| `mixedNotice` | 130 | 无（跨境常开） | `commonStyle` 助手、`imageFormat`、`urlFormat`、`redirect` | `overseas.decor` |
| `categoryNavA3` | 231 | 无（跨境常开） | `commonStyle` 助手、`imageFormat` | `overseas.decor` |
| `imageAd2` | 344 | `@vort-if overseas.h5Style` | `commonTitle`、`tig-swiper`、`formatFrame`、`urlFormat`、`redirect` | `overseas.h5Style` |
| `product2` | 544 | `@vort-if overseas.h5Style` | `masonry`、`formatFrame`、`getProductsList`、`useProductStyle` | `overseas.h5Style` |
| `seckill2` | 770 | `@vort-if overseas.h5Style` | `masonry`、`commonTitle`、`commonCountdown`、`getProductsList`、`redirect` | `overseas.h5Style` |

> **门控归属待确认**：跨境侧 4 个（searchBar2/imageAd2/product2/seckill2）在源分发器里就包在 `overseas.h5Style`，本书沿用；categoryNavA3/mixedNotice 跨境常开、无 h5Style 依赖，归 `overseas.decor`（`vort:check` 合法特性名已含 `overseas.decor`，manifest `overseas.decor.includes` 当前为空）。若希望 6 个统一归一个开关，执行前定夺。

## 二、o2o 侧现有依赖（需 S2 逐项核实存在性，不存在的要一并迁入）

- `@/components/modules` 的 `formatFrame` —— o2o 有 `components/modules/index.ts`，需确认导出 `formatFrame`。
- `commonTitle` / `commonCountdown` / `commonBuyButton` —— o2o `customPage/modules/` 下均存在同名目录（目录 diff 已确认），需确认接口一致。
- `masonry`（`@/components/masonry/masonry.vue`）—— 跨境首页/商品流用，o2o 待确认。
- `tig-swiper` —— 通用轮播，o2o 大概率有，待确认。
- `getProductsList`（`@/api/common`）—— 待确认 o2o 同签名。
- `commonStyle` 助手（categoryNavA3/mixedNotice 用）—— 来源待定位（可能是各自 `./src` 或公共 hook）。
- iconfont `icon-sousuo2` + `modules.css` 里 `module-ad-con / ad-style__ / ad-goods_*` 等类 —— 皮肤类，需确认 o2o `static/css/modules.css` 是否含，否则视觉塌陷。

## 三、波次规划（由易到难，单模块单 commit）

| Wave | 模块 | 依赖复杂度 | 要点 |
|---|---|---|---|
| **D-1** | `searchBar2` | 低 | 纯静态搜索条壳；核实 `icon-sousuo2` + modules.css 类；分发器加 `@vort-if overseas.h5Style` 分支 |
| **D-2** | `mixedNotice` | 低 | 混合公告；定位 `commonStyle` 来源；`@vort-if overseas.decor` 分支 |
| **D-3** | `categoryNavA3` | 中 | 分类导航三变体；同 `commonStyle`；`overseas.decor` 分支 |
| **D-4** | `imageAd2` | 中 | 依赖 `commonTitle`+`tig-swiper`；核实二者 o2o 存在 |
| **D-5** | `product2` | 高 | 依赖 `masonry`+`getProductsList`+`useProductStyle`（命中商品流热点，注意 R1）；单独 commit |
| **D-6** | `seckill2` | 高 | 最大；依赖 `masonry`+`commonCountdown`+秒杀数据；单独 commit |

每 Wave 步骤：迁入组件文件 → 分发器加**门控 v-else-if 分支**（带 `&& module?.isShow`，与出入一修复一致）→ 补依赖（若缺）→ 登记 manifest `includes`（文件落地后）→ 门禁。

## 四、分发器接入形态（示例，D-1）

o2o `customPage/modules/index.vue` 内，对应位置加：

```vue
<!-- @vort-if overseas.h5Style -->
<template v-else-if="module.type == 'searchBar2' && module?.isShow">
    <searchBar2 :module="module.module" />
</template>
<!-- @vort-endif -->
```

import 段同样 `@vort-if` 包裹。位置对齐跨境源分发器（searchBar2 紧跟 searchBar，product2 紧跟 productSlide，等）。

## 五、验证门（每 Wave）

- `npm run vort:check`：标记配对 + 特性名合法（`overseas.h5Style` / `overseas.decor` 均已在合法名单）。
- `npm run build:h5`：编译外贸风开启的完整源码路径（`build:h5` 不裁切，等价特性 ON）；OVERSEAS=0 裁切安全由标记配对保证。
- 零 lint。
- 回写本任务书对应 Wave 记录。

## 六、排除（out of scope）

- o2o 独有 `bannerA2 / storeNav / storeOrderCard`（门店业态）——非缺口，不动。
- 出入一（`module?.isShow`）——已在 `4aa7213` 修复，不重复。
- Admin 后台装修（这些模块类型的配置项下发）——本波只做 uniapp 渲染端；若后台无对应模块配置入口，需另立 Admin 侧任务。

## 七、执行记录

- **Wave D-1（2026-07-24）· searchBar2 · `overseas.h5Style`**：
  - S2 依赖核实全部在 o2o 就位：`formatFrame`（`components/modules/index.ts` 导出，形状一致）、`redirect`（`@/utils`）、`icon-sousuo2`（`static/font/iconfont.css` 存在）、`module-ad-con/module-ad-content`（`static/css/modules.css` 存在）；组件自带 scoped 样式，无外部皮肤类硬依赖。
  - 迁入 `src/pages/customPage/modules/searchBar2/index.vue`（69 行，faithful 原样复制，零改写）。
  - 分发器 `customPage/modules/index.vue`：searchBar 分支后插入 `@vort-if overseas.h5Style` 的 `searchBar2` v-else-if（带 `&& module?.isShow`，与出入一一致）；import 段同样 `@vort-if` 包裹。位置对齐跨境源（紧跟 searchBar）。
  - `feature-manifest.json` `overseas.h5Style.includes` 追加 `src/pages/customPage/modules/searchBar2`。
  - 门：`vort:check` 通过（650 文件）；2 文件零 lint；`build:h5` 通过（DONE）。

- **Wave D-2（2026-07-24）· mixedNotice · `overseas.decor`**：
  - S2 依赖核实：`formatCommonStyle`（`components/modules`）、`imageFormat`+`urlFormat`（`utils/format.ts` L5/L185）、`redirect`（`@/utils`）、`tig-swiper`（o2o banner 模块在用，全局注册）均就位。
  - 迁入 `src/pages/customPage/modules/mixedNotice/index.vue`（143 行，faithful 原样复制）。
  - 分发器：mixedProductDisplay 分支后插入 `@vort-if overseas.decor` 的 `mixedNotice` v-else-if（带 `&& module?.isShow`）；import 段 `@vort-if overseas.decor` 包裹。
  - `feature-manifest.json` `overseas.decor.includes` 追加 `src/pages/customPage/modules/mixedNotice`（此前为空）。
  - 门：`vort:check` 通过（651 文件）；2 文件零 lint；`build:h5` 通过（DONE）。

- **Wave D-3（2026-07-24）· categoryNavA3 · `overseas.decor`**：
  - S2 依赖核实：`formatCommonStyle`、`urlFormat`+`imageFormat`、`redirect`+`getElementRect`（`@/utils`，mainHeader 已在用）均就位；scroll-view 原生；scoped 样式自带。
  - 迁入 `src/pages/customPage/modules/categoryNavA3/index.vue`（251 行，faithful 原样复制，含 onWindowResize 指示条逻辑）。
  - 分发器：categoryNavA2 分支后插入 `@vort-if overseas.decor` 的 `categoryNavA3` v-else-if（带 `&& module?.isShow`）；import 段 `@vort-if overseas.decor`。
  - `feature-manifest.json` `overseas.decor.includes` 追加 `src/pages/customPage/modules/categoryNavA3`。
  - 门：`vort:check` 通过（652 文件）；2 文件零 lint；`build:h5` 通过（DONE）。

- **Wave D-4（2026-07-24）· imageAd2 · `overseas.h5Style`**：
  - S2 依赖核实：`commonTitle`（o2o 同目录存在，prop 接口 `module.showTitle` 一致）、`formatFrame`、`imageFormat`+`urlFormat`、`redirect`、`tig-swiper` 均就位。
  - **共享 hook 补齐**：`useImgInfo.getHeght` 跨境多一个可选 `column?` 参数（多列 swiper 按列均分高度 `(width/column)*ratio`）；o2o 缺此分支。改 `hooks/useDecorate.ts`：`getHeght(selector, column?)` 加 `if(column)` 分支，**不传 column 时行为与原来完全一致**（加性可选参数，对现有全部装修模块调用零回归），对齐母仓。未门控（惰性无害增强，OVERSEAS=0 时参数仅未被使用）。
  - 迁入 `src/pages/customPage/modules/imageAd2/index.vue`（359 行，faithful 原样复制）。
  - 分发器：imageAd 分支后插入 `@vort-if overseas.h5Style` 的 `imageAd2` v-else-if（带 `&& module?.isShow`）；import 段 `@vort-if overseas.h5Style`。
  - `feature-manifest.json` `overseas.h5Style.includes` 追加 `src/pages/customPage/modules/imageAd2`。
  - 门：`vort:check` 通过（653 文件）；3 文件零 lint；`build:h5` 通过（DONE）。

- **Wave D-5（2026-07-24）· product2 · `overseas.h5Style`**（命中商品流热点 R1）：
  - S2 依赖核实：`masonry`（`components/masonry/masonry.vue` 存在）、`formatFrame`、`getProductsList`（`api/common.ts` L236）、`useProductStyle`（`hooks/index.ts` L90）均就位。**契约核对**：o2o 现有 `product` 模块用完全相同的 `const { styleMap } = useProductStyle(productList)` + `productList.value = result.records`，masonry 接 `:commodity-list`+`:style-map`，零改写。
  - 迁入 `src/pages/customPage/modules/product2/index.vue`（556 行，faithful 原样复制；模板仅 masonry，主体为皮肤 CSS）。
  - 分发器：productSlide 分支后插入 `@vort-if overseas.h5Style` 的 `product2` v-else-if（带 `&& module?.isShow`，props `:module`+`:module-index` 对齐跨境源）；import 段 `@vort-if overseas.h5Style`。
  - `feature-manifest.json` `overseas.h5Style.includes` 追加 `src/pages/customPage/modules/product2`。
  - 门：`vort:check` 通过（654 文件）；2 文件零 lint；`build:h5` 通过（DONE）。build 有一条 `useCouponInfoMap`↔`hooks/index.ts` circular-chunk **既有告警**（o2o 现有 product 模块同样从 `@/hooks` barrel 导入，非本次新增），不影响构建。

- **Wave D-6（2026-07-24）· seckill2 + commonCountdown · `overseas.h5Style`**（最大，770 行）：
  - S2 依赖核实：`formatFrame`、`commonTitle`、`getHomeSeckill`（`api/home/home.ts` L145，返回 `SeckillListResponse`，`result.records` 契约与母仓一致）、`SeckillList` 类型（`types/home/home.d.ts` L174，含 `seckillPrice`/`seckkillData.seckillEndTime`）、`redirect`+`isShowB2bPriceText`（`utils/index.ts`）、`useProductStyle`、`tig-image`/`format-price`（o2o 全局组件）均就位。seckill2 **不用 masonry**（自带 goods-ad 栅格）。
  - **缺口补齐（依赖迁入）**：`commonCountdown`（`@/components/commonCountdown/index.vue`）o2o 缺失，一并迁入（146 行，依赖 uview-plus `up-count-down`，o2o 已装 uview-plus）。因仅 seckill2 引用，随 `overseas.h5Style` 一起门控（写入 includes，OVERSEAS=0 时与 seckill2 一起裁切，无孤儿）。
  - 迁入 `src/pages/customPage/modules/seckill2/index.vue`（805 行含皮肤 CSS，faithful 原样复制）。
  - 分发器：seckill 分支后插入 `@vort-if overseas.h5Style` 的 `seckill2` v-else-if（带 `&& module?.isShow`）；import 段 `@vort-if overseas.h5Style`。
  - `feature-manifest.json` `overseas.h5Style.includes` 追加 `src/pages/customPage/modules/seckill2` + `src/components/commonCountdown`。
  - 门：`vort:check` 通过（655 文件）；3 文件零 lint；`build:h5` 通过（DONE，同 product2 的 `@/hooks` circular-chunk 既有告警）。

---

## 八、收尾结论（2026-07-24）

6 个跨境专属装修模块全部迁入 o2o 分发器，**出入二闭合**：

| 模块 | 门控 | 迁入依赖补齐 |
|---|---|---|
| searchBar2 | overseas.h5Style | 无 |
| mixedNotice | overseas.decor | 无 |
| categoryNavA3 | overseas.decor | 无 |
| imageAd2 | overseas.h5Style | `useImgInfo.getHeght` 加可选 `column?`（加性、零回归） |
| product2 | overseas.h5Style | 无 |
| seckill2 | overseas.h5Style | `commonCountdown` 组件（随 h5Style 门控） |

**共 8 个新文件 + 分发器 6 组门控分支/import + `useDecorate.ts` 加性增强 + manifest 登记。** 全程 faithful 迁移、门控隔离（OVERSEAS=0 裁切后 o2o 零变化，由 vort:check 标记配对保证）、每波 vort:check + build:h5 + 零 lint 三门全绿。
