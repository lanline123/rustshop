# R1 · 商品详情融合 —— 公共层封装方案

> 阶段二 R1 模块任务书（配合《docs/o2o大融合-需求方案.md》§6.2 新旧双页面、《docs/o2o大融合-执行章程.md》R1）。
> 本文只锁"公共层怎么抽"，具体每个 Task 的 diff 在 S3 拆分后再逐个执行。
> 创建日期：2026-07-22

---

## 〇、已确认的三个前置决策（2026-07-22）

1. **备份方式**：`src/pages/product` 已**复制**一份为 `src/pages/productV1`（纯快照，`pages.json` 与所有跳转均未改动，`productV1` 不注册路由）。原 `product` 保持可用。
2. **公共层落点**：只在 `Tigshop-o2o-Uniapp` **单仓内**建公共层（`src/pages/product/composables` + `src/pages/product/components`），严守大融合"只改 o2o 单仓"边界；`vortmall / overseas / 标准` 三仓只作**接口设计与结构参考**，不跨仓共享物理文件、不引入软链/发包。
3. **3 种风格形态**：在 o2o 融合仓内做**一套代码**，靠**后台装修配置驱动 + `isXxx()`** 切出 vortmall 风 / o2o 风 / overseas 风三种商详呈现。

---

## 一、目标与范围

### IN（本模块做）
- 把 o2o `index.vue`（1656 行）里可复用的**逻辑**抽成公共 `composables`（以 o2o 现有实现为基座，参考 vortmall 接口设计）。
- 把"价格区 / 底部操作栏"等抽成**装修化公共组件**（参考 vortmall `ProductPriceSection` / `ProductActionBar`）。
- 统一四仓 `product` 下的**公共展示组件**基线（命名、props 对齐）。
- 建立**装修配置 → 风格切换**的骨架（哪些字段驱动哪种风格）。

### OUT（本模块不做）
- 不改支付/结算链路（属 M2/M7）。
- 不动 `pages.json` 路由与全仓跳转（新旧双页面的路由分流留到"页面壳重排"子任务，本模块先做公共层）。
- 不照搬 vortmall/overseas 的业务实现，不做整页顶替。
- 不合并 API/类型（o2o 类型为权威源，只增量补字段）。

---

## 二、四仓 `product` 组件对照矩阵（2026-07-22 实测）

页面壳四仓一致：`index.vue` / `comment.vue` / `consult.vue` / `groupBuying.vue`。

| 能力 | o2o（基座） | 标准 Tigshop | overseas 跨境 | vortmall（最先进·蓝本） |
|---|---|---|---|---|
| 图片轮播 | productImg + productSwiper | 同左 | productImg | productImg |
| 标题/价格 | productTitleInfo | productTitleInfo | productTitleInfo | **ProductPriceSection** |
| 底部操作栏 | 内联在 index | 内联在 index | 内联在 index | **ProductActionBar** |
| 详情描述 | productContent | productContent | descriptionCard + productDescription | **descriptionCard** |
| 评价 | productComment | productComment | commentItem + evaluateCard | **commentItem + evaluateCard** |
| 售后/服务 | afterSaleService | afterSaleService | afterSaleCard + serviceCard | **afterSaleCard + serviceCard** |
| 规格/属性 | 内联 + specification 弹窗 | 内联 | attrCard + skuCard | attrCard |
| 拼团 | productGroupon / More / Title | 同左 | 同左 | productGroupon / More / Title |
| 优惠券 | productCoupon | productCoupon | productCoupon | productCoupon |
| 预售标 | productPresaleTitle | productPresaleTitle | productPresaleTitle | ProductPresale |
| 折扣/秒杀标 | productDiscountTitle / productSeckillTitle | 同左 | productDiscountTitle | （并入价格区） |
| 分享 | productShare | productShare | productShare | productShare |
| 店铺信息 | productShopInfo | productShopInfo | productShopInfo | productShopInfo |
| 导航 | productNav | productNav | productNav | productNav |
| 错误页 | error | error | error | error |
| 视频描述 | ✗ | DescriptionVideo | DescriptionVideo | ✗ |
| **周期购/次卡** | **cyclebuyContent/Title、subCardContent/Title（o2o 独有）** | ✗ | ✗ | ✗ |
| 其它 o2o 独有 | expandableText、menuPop、animation | animation | explain | — |
| **逻辑分层** | 全堆在 index.vue | 全堆在 index.vue | 全堆在 index.vue | **composables/ 4 个 hooks + 单测** |

**两大流派**：
- **老式**（o2o / 标准）：`productContent / productComment / productSwiper / afterSaleService`。
- **卡片化**（overseas / vortmall）：`descriptionCard / commentItem / evaluateCard / afterSaleCard / serviceCard / attrCard`。

**结论**：公共展示组件（`productImg / productNav / productShare / productShopInfo / productCoupon / productGroupon* / error`）四仓语义一致，可作共用基线；卡片化组件是"vortmall/overseas 风"的差异层；周期购/次卡是 o2o 独有、须 `@vort-if o2o.cycle` 保护。

---

## 三、公共 hooks 设计（基座 = o2o，接口参考 vortmall）

> vortmall 已把商详拆成 `useProductDetail / useProductType / useProductAction / useProductCollect` 四个 composable（带类型、带单测），且 `useProductDetail` 内已内置 `isO2o()` 门店分支——是理想蓝本。但 **API/类型结构与 o2o 分化**（见下），因此**以 o2o 现有逻辑为准重写**，只借接口形状，不搬实现。

### 3.0 迁移硬约束（务必遵守）
- o2o 用函数式 API `getProductDetail(params)`；vortmall 用 `productApi.getDetail()`。**保留 o2o 的 API 调用方式**。
- o2o 类型 `ProductItem.productId` 为 `number`，存量 `Number(option.id)`。抽 hooks 时**顺手把 id 链路改 string 透传**（方案 §8.1 第 10 条，雪花 id 隐患）。
- 周期购/次卡/预售/活动样式/SEO/scene 短码是 o2o 相比蓝本多出的部分，抽 hooks 时**用 `@vort-if o2o.cycle` 等标记保护**，不能因为蓝本没有就丢掉。

### 3.1 `useProductDetail`（数据获取，最核心）
- **职责**：拉商品详情 / 兑换详情、落地 product/pic/video/attr/sku/desc/service/rank，处理门店参数（O2O）、SEO（H5）、店铺与门店详情、活动样式、周期购/次卡回填、loading 时序（`detailLoaded + specLoaded` 双闸）。
- **o2o 增量（相对蓝本）**：`exchange` 分支、`cycleConfig` 回填、`fetchActivityStyle`、`getStoreDetailData`、H5 `setPageMeta/setJsonLd/clearJsonLd`、`refreshStoreInfo` 事件。
- **输出**：`product, picList, videoList, attrList, skuList, descArr, serviceList, rankDetail, productPrice, productStock, shopId, storeInfo, shopInfo, cycleInfo, isCycleBuy, isSubCard, loadEndStatus, detailLoaded, failLoad, getProductDetail(), getStoreDetailData(), getShopInfo()`。

### 3.2 `useProductType`（商品类型判断）
- **职责**：`isGroupon / isSeckill / isDiscount / isReservation / isVirtual / isPaidContent`，`productTypeText / reserveModeText / reserveTipText`。
- **o2o 增量**：`isCycleBuy / isSubCard / isSubCardReservationProduct`（`@vort-if o2o.cycle`）、`isPresale`（预售）。
- 蓝本可几乎直接复用接口形状。

### 3.3 `useProductAction`（操作逻辑）
- **职责**：`showShare / handleShare`、规格弹窗 `showSpecification / showGrouponPopup / showReservePopup`、拼团 `handleShowMore / handleJoinGroup / getGrouponRecords / twoGroupList / grouponRecordInfo`。
- **o2o 增量**：加购动画 `getShowAnimation / getAnimationPosition / styleVariable`、购物车角标 `getCartCount`、分销 `saveSalesmanProducts`。

### 3.4 `useProductCollect`（收藏）
- **职责**：`isCollect / toggleCollect`，随 productId 变化自动刷新。
- 蓝本可直接对齐接口（注意 o2o 的收藏 API 名/参数）。

### 3.5 落点
```
src/pages/product/composables/
├── index.ts                # 统一导出
├── useProductDetail.ts
├── useProductType.ts
├── useProductAction.ts
└── useProductCollect.ts
```
> 与 vortmall 目录约定一致，便于后续对照维护。

---

## 四、公共装修样式方案（一套代码 · 装修配置 + isXxx() 切 3 风格）

### 4.1 装修化组件（抽独立可配置组件）
- `components/ProductActionBar.vue`：底部操作栏（首页/客服/购物车 + 加购/立即购买/开团/预约/兑换按钮矩阵），把 index 内联的底部栏抽出。**（T6 已完成）**
- **价格区（原 `ProductPriceSection` 规划）**：o2o 现有 `src/pages/product/src/productTitleInfo.vue` 已承担价格区职责（价格 + 秒杀/折扣/拼团/预售/周期购/次卡活动标题矩阵 + 名称卡片 + 收藏/分享），独立度与装修化程度已足够，**不新建 `ProductPriceSection.vue`**，避免打散稳定组件、违背"行为零变化"。后续风格切换时价格区以 `productTitleInfo` 为 o2o 门店风实现，卡片风另接（T7）。

### 4.2 风格切换机制（骨架）
- **一个 `productStyle` 决策入口**：优先读**后台装修配置**（商详风格字段，需与 `Tigshop-Admin` 对齐字段名，S2 跟后端/后台核实）；无配置时按 `isXxx()` 兜底：
  - `isOverseas()` → overseas 卡片风；
  - `isO2o()` → o2o 门店风（默认基座）；
  - 其余 → vortmall 标准风。
- 组件内根据 `productStyle` 选择渲染"老式"或"卡片化"子组件；**平台差异仍走 `#ifdef`，与风格维度正交**。

### 4.3 待确认（S2）
- 后台装修是否已有"商详风格/模块开关"配置项？字段名与数据结构？（决定 4.2 是配置驱动还是纯 `isXxx()`）
- 卡片化子组件（descriptionCard/commentItem/…）是从 vortmall 还是 overseas 取基线？两者需三方对照选优。

---

## 五、@vort-if 标记与 manifest 策略
- 周期购/次卡逻辑与组件：`@vort-if o2o.cycle`（延续存量 86 处语法）。
- 门店/exchange 门店分支：`isO2o()` 运行时 + `@vort-if o2o` 断链点标记。
- overseas 卡片风独有组件：`@vort-if overseas` 包在 import/注册处。
- 标记只打"断链点"（import、模块注册、pages.json），不切碎模板（方案 §3.3 教训 1）。

---

## 六、建议 Task 拆分（每个 ≤800 行 diff / ≤15 文件，S3 定稿）
- **T1**：建 `composables/` 骨架 + `useProductCollect`（最小、可独立验证）。
- **T2**：`useProductType`（纯判断，零副作用，风险最低）。
- **T3**：`useProductAction`（操作 + 拼团 + 动画）。
- **T4**：`useProductDetail`（数据获取，最重，含门店/周期购/SEO；单独一 Task）。
- **T5**：`index.vue` 接线到 composables（页面壳瘦身，行为零变化）。
- **T6** ✅：抽 `ProductActionBar` 底部操作栏（o2o 风原样、行为零变化）；价格区沿用 `productTitleInfo`（决策见 §4.1，不新建 `ProductPriceSection`）。
- **T7**：风格切换骨架 + 卡片化组件基线接入（依赖 S2 后台字段核实）。

> 进展：T1–T6 已完成，`vue-tsc --noEmit` 仅剩 2 个既有 `allowJs` 报错（`tig-slide.vue.js` / `VerifyPoints.vue.js`），与本次改动无关。

> 每个 Task 独立 commit；`isStore()` 置 0 回归时 o2o 站行为必须与改动前完全一致。

---

## 七、风险
- **id 类型漂移**：o2o number ↔ 蓝本 string，抽取时统一 string，注意 specification 弹窗等下游联动。
- **loading 时序**：o2o 的 `detailLoaded + specLoaded` 双闸逻辑易被重构破坏，抽 hooks 时须原样保留。
- **周期购/次卡**：蓝本无此逻辑，切勿在参考蓝本时丢失 o2o 独有分支。
- **装修字段依赖后端**：4.2 的配置驱动需后台/后端 ready，未 ready 则先纯 `isXxx()` 兜底并记录。
