# M8 · B2B 批发整块迁入（uniapp 端）

> 里程碑：P3 · 开关：`isB2B()` + `@vort-if b2b` · 依赖：M3（币种/i18n，已完成）
> 红线：一次一模块、迁移≠重构、**每 Task ≤800行/≤15文件、一 Task 一 commit**、开关置0行为不变。
> 状态：**Wave A~F 全部完成，M8 收官（2026-07-22）**。决策 D1=逐文件取超集(=跨境版) / D2=a(仅 Wave A) / D3=随 Wave E / Wave F 门控 D=b(裁切式 isB2B 守卫)。
> **收尾（2026-07-23）批发类型强化完成**：`SkuList` 本已含 `skuPrice?/price?` → 删 `product/index.vue` 冗余 `sku as {...}` cast；`check.d.ts` 给 `CheckItem` 加 `couponExcludeAmount?/pointsExcludeAmount?`、给 `Cart` 加 `wholesaleScope?/isWholesaleLine?/wholesaleMoq?/wholesaleMoqDimension?/wholesaleMultiple?`（均 `@vort-if common.wholesale` 标注）→ 删 `order/check.vue` 的 `(shop:any)/(c:any)/(result as any)` cast。cart/cartJump 的 `isRetailLine(goods:any)` 与该文件既有满屏 `any` 约定一致、cartList 未强类型，保留不动。验证：3 文件零 lint、`vort:check` 无 warn、`build:h5` 通过。

---

## 〇、Wave A 实施结果（2026-07-22）
- ✅ 16 个 wholesale 资产从跨境仓复制进 o2o（逐文件取超集；差异文件跨境=标准+i18n，国内 b2b 站 `t()` 回落中文零回归）。
- ✅ **manifest 新增 `common.wholesale` 特性**（`common.modules.wholesale.includes`），登记 api/components/types/store/utils/hooks 资产。**未改写迁移文件的 `@vort-if common.wholesale` 标记**（源仓保真、未来合并不冲突）。
- ✅ `useProductCardBuy.ts` 迁入（M1→M8，含 `@vort-if common.wholesale` 块级标记）。
- 验证：`vort:check` 通过（`common.wholesale` 已合法、扫描 608 文件、无新增空路径警告）、`build:mp-weixin` 通过、`build:h5` 通过。
- 说明：Wave A 资产为**休眠态**（尚无消费点引用，easycom 未触达 → 不进包），符合"资产先落地、消费后接线"。类型字段扩展（cart/product/order 的 wholesale 字段）随消费波次（C~F）补齐。

## 〇·B、Wave B 实施结果（2026-07-22）
- ✅ `utils/init.ts`：`arr.push(useWholesaleStore().loadIdentity())`（`@vort-if common.wholesale` 块）。启动预拉身份，未登录后端回 `isWholesaler=false` 不报错；`loadIdentity` 内 catch 兜底 → 即使 o2o 后端暂无 `/wholesale/me` 也不影响启动。
- ✅ `store/user.ts` `clear()`：`useWholesaleStore().reset()`（登出/切号清空批发态，防串号）。
- ✅ 登录成功强制刷新身份 `loadIdentity(true)`：`login/src/baseLogin.vue`(密码/手机登录)、`user/bindMobilePhone/index.vue`(绑手机)、`tig/tig-wechart-login.vue`(H5 授权 + 小程序手机号快捷登录 2 处)。
- 说明：接线全部用 `@vort-if common.wholesale` 块级标记（对齐 Wave A 资产的裁切模型），纯零售构建 cut 后整段消除；未启用 `isB2B()` 运行时守卫——与标准/跨境源仓一致（源仓亦仅用 `@vort-if` 裁切，不做运行时判分）。
- 验证：`vort:check` 通过（仅 4 条历史 includes 空路径警告，无新增），5 文件无 lint 报错。

## 〇·C、Wave C 实施结果（2026-07-22，商品详情）
- **关键发现（已与用户确认）**：o2o 三仓均已有 `b2b.batchBuy`（配置驱动「批量采购」tab，内嵌零售规格弹层）；`common.wholesale`（身份驱动阶梯价+独立弹层）是**并存互斥**特性——标准/跨境两者同存，o2o 独缺 wholesale。故本波为**纯增量**，不动既有 batchBuy。
- o2o `product/index.vue` 与标准/跨境结构差异大（O2O 周期购/次卡/预售/到店/POS，买按钮统一走 `showSpecification` 单漏斗，价格在 `productTitleInfo` 组件），按 o2o 结构定制接线：
  - 脚本：`@vort-if common.wholesale` 导入 SkuPopup/PriceTable/store/api/utils/types；新增 `wholesaleStore/wholesalePriceList/wholesaleProductPriceMode/wholesaleDisplayModel/wholesaleSkuRef/isWholesaleMode/wholesaleRetailMax/wholesaleBuyRule/loadWholesaleBuyRule/wholesaleMoq/onWholesaleAddSuccess`。
  - 详情加载：填充 `wholesalePriceList/wholesaleProductPriceMode`；批发态 `specLoaded=true` 手动关闭 loading（零售 specification 被移除不再触发 `@load-end`）+ 拉 `loadWholesaleBuyRule`。类型用局部 cast，未改共享类型。
  - handler：`showSpecification(arg)`/`showGrouponPoup` 顶部 `isWholesaleMode` 短路唤起 `wholesaleSkuRef.handleShowPopup(arg===false)`；沿用跨境「加购透传事件(truthy)→isQuick=false / 立即购买传 false→isQuick=true」判定，**「立即购买」按钮显式 `showSpecification(false)`**（零售路径忽略入参，行为零变化）。
  - 模板：`<specification v-if="!isWholesaleMode">` 与 `<WholesaleSkuPopup v-if="isWholesaleMode">` 互斥；`productTitleInfo` 前挂 `<WholesalePriceTable v-if="isWholesaleMode">`。
- `productTitleInfo.vue`：新增 `isWholesaleMode` prop（**常驻 default false**，非 @vort-if，裁切后详情页传 undefined→默认 false 走零售）；批发态用 `<template v-if="!isWholesaleMode">` 整体隐藏零售价/营销标题区（name-card 保留）。
- 验证：`vort:check` 通过（仅 4 条历史警告）、2 文件零 lint、`build:h5` 通过（DONE）。

## 〇·D、Wave D 实施结果（2026-07-22，购物车）
- **两个购物车都接线**：`pages/cart/index.vue`（tab 购物车）+ `pages/cartJump/index.vue`（**登录用户主购物车**——`product/index` 的购物车按钮 `userStore.token` 时跳 cartJump）。两文件同构，接线一致。
- 每文件 4 处（全 `@vort-if common.wholesale`）：① import `useWholesaleCart`+`WholesaleCartStepper`；② `const wholesaleCart = useWholesaleCart()` + `isRetailLine(goods)` 助手（切割关闭时恒 true，退回零售）；③ 计步器互斥：`<WholesaleCartStepper v-if="wholesaleCart.isWholesaleLine(goods)">`（倍数步进+删除）与 `<tig-number-box v-if="isRetailLine(goods)">`；④ `handleCheckout` 结算门禁：`validateCheckout(checkedLines)` 有文案则 toast 拦截。
- `isWholesaleLine` 用 `line.wholesaleScope`（批发商品身份），非 `isWholesaleLine` 布尔，避免减量到不达阶梯时误退零售 ±1。
- 坑：`vort:check` 会把注释里字面 `@vort-if` 当标记 → JSDoc 改用「wholesale 切割块」措辞。
- 验证：`vort:check` 通过、2 文件零 lint、`build:h5` 通过（DONE）。

## 〇·E、Wave E 实施结果（2026-07-22，卡片直购）
- **架构差异**：o2o **无 `masonry-row.vue`**（跨境专有），Wave E 落 6 文件（非 7）。`useProductCardBuy` 导出 `isWholesaleCard/cardProductId`（D3 随本波接线，已达成）。
- 两类接线模式：
  - **仅改价**（父级处理买）：`components/masonry/product-item.vue`（价格块加 `v-else-if isWholesaleCardComputed` 显示 `firstWholesalePrice`）、`pages/search/src/shopInfo.vue`。
  - **带弹窗**（自持 `WholesaleListSpec` + handler）：`components/product/card.vue`（批发价+起+选规格 CTA + 样式块）、`components/masonry/masonry.vue`、`pages/coupon/detail.vue`、`pages/productCate/styleThreeCate.vue`。
- 带弹窗统一范式：`handleBuy` 先 `item.isCheck=true; currentItem=item`，default 分支 `if(isWholesaleCard) { wholesaleProductId=cardProductId; return; }`；`WholesaleListSpec` v-model 绑 `wholesaleProductId`，`wholesaleLoadEnd({wholesale})` —— 非批发则回退零售（`productId=wholesaleProductId`），批发则 `handleShowPopup()`；与零售 `specification` 互斥。
- 全部 `@vort-if common.wholesale`，只增不改既有零售块（o2o 保留自身 `item.price`/`cyclePrice`/无 `show-text` 等差异，不动）。
- 验证：`vort:check` 通过（仅 4 历史警告）、6 文件零 lint、`build:h5` 通过（DONE）。

## 〇·F、Wave F 实施结果（2026-07-22，结算 + 门控补齐）
- **结算 `pages/order/check.vue`**（4 处，全 `@vort-if common.wholesale`）：① refs `wholesaleCouponExclude/wholesalePointsExclude` + `hasWholesaleLine`(任一 cart 行 `isWholesaleLine`) + `toNum` 助手；② 主加载 `getOrderCheckData` 后填充 `result.couponExcludeAmount/pointsExcludeAmount`（缺字段回落 ""）；③ 发票块后挂"批发商品不参与店铺优惠券/不赠送积分，已排除"提示 banner；④ 样式块。cart 行字段用 `any` cast，未改共享 `CartList` 类型。
- **门控补齐核对（3 文件，2 个证伪 / 1 个落地）**：
  - `order/src/couponInfo.vue`：跨境版**未使用 `isB2B()`**（任务书 1.3 记录不准）→ 无可迁。
  - `member/.../termPayCard.vue`：`v-if="showTermPay && isB2B()"` 属**账期/授信 termPay**，任务书 1.4 明确不在 M8 → 越界跳过。
  - `components/format-price/format-price.vue`：o2o 已有 `showB2bText`（条件 `showText && isIdentity===1`），跨境多 `isB2B()` 前置。**决策 D=b**：`@vort-if common.wholesale` 包裹一句 `if (!isB2B()) return false;` 前置守卫——纯零售构建裁切后走原 `isIdentity` 逻辑（**行为零变化，不触红线**），b2b/批发构建等价跨境 `isB2B() &&`。`isB2B` import 亦 `@vort-if` 包裹。
- 验证：`vort:check` 通过、2 文件零 lint、`build:h5` 通过（DONE）。

---

## ✅ M8 收官小结（Wave A~F）
- **资产**：16 wholesale 文件整块落地 + `common.wholesale` 特性注册。
- **接线**：身份生命周期(init/user/3登录点) → 商品详情(product/index+productTitleInfo) → 购物车(cart+cartJump) → 卡片直购(card/product-item/masonry/shopInfo/coupon-detail/styleThreeCate，6 文件，o2o 无 masonry-row) → 结算(order/check) → 门控(format-price)。
- **全程 `@vort-if common.wholesale` 块级裁切**：纯零售构建 cut 后零残留、零行为变化；b2b/批发构建启用。未用运行时 isB2B 分流（对齐源仓；仅 format-price 因已有 isIdentity 逻辑用裁切式 isB2B 守卫兜底）。
- **每波 `vort:check` + `build:h5` 双验证通过**。
- **遗留**：`types/{cart,product,order}` 的 wholesale 字段全程用局部 `any`/`as` cast 兜底（未改共享类型，零 TS 回归）；如需强类型可后续单独收敛。

### 决策记录
- **D1 = 逐文件取超集**：实测差异仅 `WholesaleCartStepper`(48行) 明显、`SkuPopup`(19)/`useWholesaleCart`(3)/`useWholesaleQuantity`(4，纯注释) 微差；本质"跨境=标准+i18n"，取跨境即取超集，非二选一。
- **D2 = a**：本次仅 Wave A 资产整块落地，不碰热点页。
- **D3 = 随 Wave E**：useProductCardBuy 已迁入，消费接线随 Wave E。

---

## 一、S2 盘点结论（2026-07-22，explore 实测）

### 1.1 o2o 缺失的核心资产（16 文件，标准+跨境有 / o2o 无）
```
src/api/wholesale/me.ts                         (17)
src/api/wholesale/product.ts                    (18)
src/components/wholesale/WholesaleCartStepper.vue   (std103 / ovr151 ⚠差48)
src/components/wholesale/WholesaleListSpec.vue      (105/107)
src/components/wholesale/WholesalePriceTable.vue    (265/267)
src/components/wholesale/WholesaleSkuPopup.vue      (std1244 / ovr1225 ⚠差19, i18n)
src/components/wholesale/WholesaleStepper.vue       (168/170)
src/types/wholesale/buyRule.d.ts                (31)
src/types/wholesale/cart.d.ts                   (38)
src/types/wholesale/identity.d.ts               (20)
src/types/wholesale/price.d.ts                  (34)
src/store/wholesale.ts                          (67)
src/utils/wholesale.ts                          (39)
src/hooks/useWholesaleCart.ts                   (std66 / ovr69)
src/hooks/useWholesaleQuantity.ts               (std79 / ovr75)
src/hooks/useProductCardBuy.ts                  (29, M1 移交)
```

### 1.2 内部依赖链
- `WholesaleListSpec` → `WholesaleSkuPopup` + api/store/types
- `WholesaleSkuPopup` → `WholesaleStepper` + hooks/api/utils/types
- `WholesaleStepper`/`WholesaleCartStepper` → `useWholesaleQuantity`
- `WholesalePriceTable` → `types/wholesale/price`
- 结论：**资产可整块先落地**（自成闭环），消费点接线再分波。

### 1.3 消费点（迁入后接线，全部 `isB2B()`/`@vort-if b2b` 守卫）
| 场景 | 文件 | 说明 |
|---|---|---|
| 启动身份 | `utils/init.ts` | 加 `useWholesaleStore().loadIdentity()`（b2b 守卫） |
| 登出/登录 reset | `store/user.ts` + `login/baseLogin` + `bindMobilePhone` + `tig-wechart-login` | 刷新批发身份 |
| 商品详情 | `pages/product/index.vue`(热) + `product/src/productTitleInfo.vue` | 接 SkuPopup/PriceTable/store/api |
| 购物车 | `pages/cart/index.vue`(热) | 接 useWholesaleCart + CartStepper |
| 优惠券/分类/搜索/卡片 | `coupon/detail`、`productCate/styleThreeCate`、`search/src/shopInfo`、`masonry/*`、`product/card.vue` | 接 useProductCardBuy + ListSpec |
| 结算 | `pages/order/check.vue`(热) | 批发券/积分排除提示 |
| 类型扩展 | `types/{cart,product,order}` | wholesale 字段 merge |
| isB2B 门控补齐 | `format-price.vue`、`order/src/couponInfo.vue`、`member/.../termPayCard.vue` | 标准/跨境有、o2o 缺 |

### 1.4 范围外（勿混入 M8）
- 询价 inquiry：三仓均无实现。
- 企业认证 userCertification / 账期 termPay 页面：三仓都有，非 wholesale 缺失，不在 M8。
- `overseasLogin`/`oauth`：属"跨境登录整包"专项（M4 遗留）。

---

## 二、标准 vs 跨境选优（S2）
- 11 个文件 0~2 行差，视为同源，取任一。
- **差异文件优先取跨境版**：`WholesaleCartStepper`(+48 i18n/删除确认)、`WholesaleSkuPopup`(+i18n)、`useWholesaleCart`(+i18n 文案)、`useWholesaleQuantity`。理由：**M3 后 o2o 已具备 i18n 基建，跨境版含 i18n 更贴合融合目标**（国内 b2b 站 `$t` 回落中文，零影响）。

---

## 三、波次计划（每波 ≤15 文件 / ≤800 行，一波一 commit）

- **Wave A · 资产整块落地（无消费接线）**：16 文件 copy 进 o2o（差异文件取跨境）+ `types/{cart,product,order}` wholesale 字段 merge + `.vort/feature-manifest.json` 登记 `b2b.includes`。产出可编译但暂无入口的闭环资产。
- **Wave B · 身份生命周期**：`utils/init.ts` loadIdentity + `store/user.ts` reset + 登录/绑手机/微信登录刷新（全 `isB2B()`/`@vort-if b2b` 守卫）。
- **Wave C · 商品详情**：`product/index.vue` + `productTitleInfo.vue` 接 SkuPopup/PriceTable。
- **Wave D · 购物车**：`cart/index.vue` 接 useWholesaleCart + CartStepper。
- **Wave E · 卡片直购**：`coupon/detail`、`productCate/styleThreeCate`、`search/shopInfo`、`masonry/*`、`product/card.vue` 接 useProductCardBuy + ListSpec。
- **Wave F · 结算 + 门控补齐**：`order/check.vue` 批发排除 + `format-price`/`couponInfo`/`termPayCard` 的 `isB2B()` 门控。

**热点并行红线**：product/index、cart/index、order/check、format-price、init 分属不同波次，不并行。

## 四、决策点
- **D1 · 选优基准**：(a) 差异文件取跨境版（含 i18n，推荐）；(b) 全取标准版；(c) 逐文件三方细比。
- **D2 · 本次执行范围**：(a) **先只做 Wave A 资产整块落地**（最小、可回归、不碰热点页，推荐）；(b) Wave A+B；(c) 一次推进到某指定 Wave。
- **D3 · useProductCardBuy 归属**：随 Wave E 消费点一起接（M1 已决 D3 移交 M8）。

## 五、风险
| 级别 | 风险 | 处置 |
|---|---|---|
| 🔴 高 | 触碰 product/cart/order/check 等多个热点页 | 分波、逐波 S2、不并行 |
| 🟠 中 | 类型字段 merge 冲突（cart/product/order） | 只增字段不改原有；三方对照 |
| 🟠 中 | 标准/跨境双实现漂移 | D1 选优（差异取跨境 i18n 版） |
| 🟡 低 | o2o prod b2b=0，资产落地即"暂未启用" | `@vort-if b2b` 保护，cut 后生产零残留 |
