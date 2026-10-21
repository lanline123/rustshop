# M10 · 购物车页样式对齐 vortmall

> 属《M9 页面样式校验清单》的追加项（M9 §二决策表原将 `cart`/`cartJump` 标为「禁改（他人负责）」，2026-08-03 业主重新开单）。
> 参考蓝本：`vortmall-uniapp`（`~/workspace/vortmall/vortmall-uniapp`）。
> 创建：2026-08-03

---

## 〇、范围与业主拍板

| 项 | 结论 |
|---|---|
| 范围 | **只做视觉样式平替**，保持 o2o 现有单文件硬编码结构 |
| 装修化 | **不做**。vortmall 购物车已装修化（`components/modules/cart/cart-list`、`cart-footer` 走后台配置 + 页面内装修模块分发 + `previewId` 预览），需 `Tigshop-Admin` 配合，另行立项 |
| 顶栏/管理态 | **照 vortmall 改**；vortmall 没有的功能**一律注释保留、不删**（业主 2026-08-03 明确要求） |
| `cartJump` | **已同步样式**（2026-08-03 业主拍板同步改；见 §五） |

**视觉蓝本取自**（不抄装修配置层）：

- 列表：`vortmall-uniapp/src/components/modules/cart/cart-list/preview.scss`
- 底栏：`vortmall-uniapp/src/components/modules/cart/cart-footer/preview.scss`（**样式二** = 单行布局）+ 非装修等价物 `pages/cart/components/cartFooter.vue`
- 顶栏：`vortmall-uniapp/src/pages/cart/components/cartHeard.vue`
- 空态：`vortmall-uniapp/src/pages/cart/components/cartEmpty.vue`

---

## 一、改动清单

### 新增

| 文件 | 说明 |
|---|---|
| `src/pages/cart/index.v1.vue` | 休眠备份（不注册、不 import）。**禁止改动** |
| `src/pages/cartJump/index.v1.vue` | cartJump 休眠备份。**禁止改动** |
| `src/pages/cart/src/cartHeard.vue` | 购物车顶栏：`navbarContent`（fixed + placeholder + 状态栏）+ 标题 32rpx/700/`#1a1a1a` + 右侧「编辑/完成」28rpx。可选 `showBack`（返回键，cartJump 用）。用 flex 而非 absolute，以接收小程序端注入的 `paddingRight` 避让胶囊 |

### `src/pages/cart/index.vue`

**顶栏**

- `tig-navbar` → `cartHeard`，「管理」入口上移进 navbar 成「编辑/完成」；空车时 `show-edit-button=false`。
- 跨境 `mainHeader` 分支（`@vort-if overseas.h5Style`）原样保留。
- 原「已选 N 件 / 管理」灰条**未删**：跨境皮肤用 `mainHeader`、没有可承载入口的 navbar，故该灰条保留为 `v-if="isOverseasSkin"` 的跨境专用分支（包 `@vort-if overseas.h5Style`）。国内不再渲染。

**列表（对齐 `cart-list/preview.scss`）**

| 项 | 改前 | 改后 |
|---|---|---|
| 页面底色 | 继承 | `#f5f5f5` |
| 列表容器 | `padding:20rpx` + `gap:20rpx` | 无内边距，店铺块 `margin-top:14.5rpx`（首个 0） |
| 店铺卡 | `border-radius:10rpx` | 去圆角（扁平通栏白卡） |
| 店铺头 | `padding:20rpx 20rpx 0` | `padding:0 20rpx`，勾选框 `padding-left:8rpx` |
| 店铺名 | 28rpx bold `#333` | 27rpx `#888b94`，`padding-left:20rpx` |
| 商品行 | `padding:24rpx 20rpx 10rpx 80rpx` | `padding:0 30rpx 0 88rpx` |
| 勾选列 | 宽 80rpx + `padding-top:86rpx` 硬顶 | 宽 88rpx + `padding-left:28rpx` + flex 垂直居中，高 181rpx |
| 商品图 | 180rpx / 圆角 15rpx / 双层阴影 | 181rpx / 圆角 6rpx / 去阴影 |
| 信息区 | `padding-left:18rpx`，普通流 | `padding-left:24rpx`，flex 列 + `space-between` + `min-height:181rpx` |
| 标题 | 27rpx | 24rpx / 行高 32rpx |
| SKU | 灰底胶囊 22rpx `#969799` + 定高 48rpx 占位盒 | 纯文本 24.5rpx `#888b94`，上下 14rpx；**去掉占位盒**（无 SKU 时不再占位，与 vortmall 一致） |
| 活动标 | 无外边距 | 上下 14rpx |
| 左滑按钮 | 各 150rpx / 24rpx 字 | 各 125rpx / 22rpx 字（`movable-view` 宽度随之改 `calc(100% + 250rpx)`） |
| 卡片宽度 | 写死 690rpx | `100%`（通栏） |

- **顺手修**：禁用行 class 名 `cartItemDisabled`（模板）与 `.cart_item_disabled`（CSS）不匹配，导致售罄/下架行的 `opacity:.5` 从未生效。已改模板为 `cart_item_disabled`，与 vortmall `--disabled{opacity:.5}` 对齐。
- **计步器**：零售行 `tig-number-box` → `tig-number-stepper`（`is-delete`，数量为 1 时减号变删除图标 → 走 `handleDel` 确认弹窗），事件签名沿用仓内既有用法（`store/src/cartPop.vue`）。
  - ⚠️ `tig-number-stepper` 声明了 `disabled` 但内部未消费（vortmall 的 `vort-number-stepper` 同样如此）。为不丢原 `tig-number-box` 的禁用保护，在容器 `.cart-num-box` 上加 `.cart-num-box-disabled`（`pointer-events:none` + 降透明度）拦截售罄/下架行。
  - `handleOverlimit` 因此不再需要（减到下限走删除、加超库存由计步器内部 toast「库存不足」），**已注释保留**。

**底栏**

- 结算态改**单行**（对齐 `cart-footer` **样式二**；2026-08-03 业主由样式一改拍为样式二）：
  - 左：「全选」（square 复选框 + 27rpx `#888b94`）；打开优惠明细弹层时隐藏全选。
  - 右：价格区（上行「合计」+ 总价；下行「优惠：¥x」+「明细」箭头）+ 旁侧圆角 40rpx「去结算」按钮（高 72rpx，非全宽）。
- 编辑态底栏：左「全选」+ 右 240rpx 宽 / 36rpx 圆角「删除」按钮。
- 容器：`padding:0 28rpx` + `border-top:1px #f0f1f2`，去掉原 `border-bottom`。
- **注释保留（不删）**：
  - 「清空购物车」按钮 + `checkClearCart()` + `clearCart` import。
- **占位与定位**：`checkOutBarStyle` 里依赖 `tabbarStore.currentActiveValue` 的 tabbar/安全区偏移逻辑**保留**；占位改走 `--checkOutBar-height`：结算态 **100rpx**（样式二）/ 编辑态 104rpx / 空车 0。
- ~~样式一价格行底对齐~~：已随改拍为样式二不再适用（样式二价格区为 `baseline` + 上下两行）。
### `src/components/product/discountInfo.vue`（共用组件）

对齐 `vortmall-uniapp/src/components/product/discountInfo.vue`：

| 项 | 改前 | 改后 |
|---|---|---|
| 层级 | `:z-index="10"`，被底栏（88）和自绘 tabbar（98）压住，弹层露不出底部 | 去掉该 prop，回落 `tig-popup` 默认 201 → 从底部弹起并盖住底栏与 tabbar |
| 关闭按钮 | `up-popup` 内置 closeable 细叉 | `:show-close="false"` + 自绘灰底圆形 `staticResource("order/close.png")` 35rpx（该资源已实测存在） |
| 标题 | 28rpx 常规 | 32rpx / 700 / 行高 42rpx，`padding:35rpx 28rpx 28rpx` |
| 分隔线 | 无 | 商品总额下、合计上各一条 `#f5f6fa`，负边距拉到弹层左右边缘 |
| 行高 | `padding:20rpx 0` | 定高 60rpx，合计行 84rpx（加粗） |
| 字号 | 26rpx / 值 28rpx | 24.5rpx |
| 圆角 | 仅 popup 默认 | `.discount` 另加 `28rpx 28rpx 0 0` + `overflow:hidden` |
| 优惠券 / 其他优惠 | `hassub` 嵌套的灰色缩进子行 | 拍平为与「共优惠」同级的普通行（vortmall 结构） |
| 商品总额 | 无件数 | 追加 `(N件)`，取 `checkedCount ?? totalCount`；取不到（订单确认页）则不渲染 |

- ⚠️ **影响面**：该组件另被 `pages/cartJump/index.vue`、`pages/order/src/totalCard.vue`、`pages/order/src/totalCardStore.vue` 复用，三处优惠明细弹层同步变为 vortmall 样式。vortmall 侧同样是一个共用组件，故视为对齐而非回归；若业主要求订单页保持原样，需拆 `discountType` 分支。
- 文案保留 o2o 的「商品总额」（vortmall 为「商品总价」），避免动 i18n key。

### `src/pages/cart/src/cartEmpty.vue`

- 改为 vortmall 的居中横排：88rpx 图标框（图 70×56）+ 24.5rpx `#c2c4cc` 文案「还没有商品？」。
- 图片换用 vortmall 同款 `staticResource("cart/cart-empty.png")`——**已实测该资源在 tigshop 静态服务存在**（`static/mini/images/cart/cart-empty.png`，PNG 40×33；对照不存在路径返回 `{"code":404}`）。
- **高度自适应（业主二轮反馈）**：空态用 `flex:1 + min-height:320rpx`，配合 `.cart-box--empty { min-height: calc(100vh - var(--nav-height,97rpx) - var(--tabbar-height,0px)) }`。
  - 后台**未开**「猜你喜欢」：`recommend` 走 `v-if="guessLike.length && configStore.isGuestLikeGoods"` 不渲染 → 空态是唯一 flex 子项，吃满整屏剩余空间，**撑满**。
  - 后台**已开**：推荐位把容器撑过一屏、无自由空间可分配 → 空态回落 `min-height:320rpx`，**把推荐让出来**。
  - `--nav-height` 由 `tig-layout` 按 `statusBarHeight + 97rpx` 注入（含小程序状态栏）；`cartHeard` 实际 79.6rpx，故空态比整屏略矮十几 rpx，宁可短一点也不出现多余滚动。

### `src/components/tig/tig-checkbox/tig-checkbox.vue`（共用组件，增量）

- 补 `iconSize`、`activeColor` 两个可选 props，对齐 `vort-checkbox`。
- **零变化保证**：`iconSize` 默认 `""` 且以 `:icon-size="iconSize || undefined"` 传入 → 不传时 `up-checkbox` 仍用自身默认值；`activeColor` 默认 `""` → 仍回落主题色 `--general`。其余 30+ 处调用方行为不变。
- 描边加粗（`border-width:2px`）**只在购物车页**用 `:deep(.u-checkbox__icon-wrap)` 局部覆盖，不改全局。

---

## 二、有意偏离蓝本处（已在上文标注，汇总）

1. 跨境皮肤保留「已选 N 件 / 管理」灰条（vortmall 无跨境皮肤分支，o2o 若删则跨境丢失管理入口）。
2. 空态高度按「猜你喜欢」开关自适应（vortmall 推荐位是装修模块，空态恒撑满；o2o 推荐位是固定组件，需让位）。
3. 左滑仍用 `uni-swipe-action` + 具名插槽，未换 `up-swipe-action` + `options` 数组（降风险，仅对齐按钮尺寸/字号/配色）。
4. 计步器禁用态用容器 `pointer-events` 兜底，未改共用组件 `tig-number-stepper`。

---

## 三、保留未动的 o2o 能力

`cartProductStyleMap` 商品边框装修图、`formatActivityList` 的 `usedPromotions` 择券逻辑、`extraSkuData` 附加属性块与弹层、赠品块、`getProductUrl` 带 `shopId`、预售不混单校验、`@vort-if common.wholesale` 批发行（`WholesaleCartStepper` + `isRetailLine()` 门控 + 结算门禁）、`deliveryProductPop` O2O 配送方式选择、`recommend` 推荐位、`discountInfo` 优惠明细弹层。

---

## 四、验证

| 项 | 结果 |
|---|---|
| `npm run vort:check` | 通过（扫描 680 文件，配对/嵌套/特性名全绿） |
| `npm run type-check` | 通过——仅 2 条**既有** `TS6504`（`tig-slide.vue.js`、`VerifyPoints.vue.js`），本轮零新增 |
| `npm run build:h5` | 通过 |
| `npm run build:mp-weixin` | 通过；`pages/cart` 产物 152K（主包页，本轮新增仅 `cartHeard`） |
| `npx eslint src/pages/cart src/components/tig/tig-checkbox` | 零报错 |
| i18n | 本轮**未新增插值 key**，无需改 `zh.json`/`en.json`；新增非插值 key（`编辑`/`已减免`/`还没有商品？`/`删除`）中文走 key 回退，英文属 M4 |

### 待人工目视（非阻塞）

- 标准 / o2o / 跨境 三构建各看一遍。
- o2o 重点：自绘 tabbar 下底栏位置（结算态 100rpx / 编辑态 104rpx 两种高度切换不遮挡）、「去结算 → `deliveryProductPop` 配送方式选择 → 结算页」链路。
- 跨境重点：`mainHeader` 皮肤下灰条仍在、顶栏不重叠。
- 小程序端重点：`cartHeard` 胶囊避让、`tig-number-stepper` 减号命中区、左滑按钮宽度。

---

## 五、`cartJump` 同步（2026-08-03）

业主拍板选 ① 同步样式对齐。以已改完的 `cart/index.vue` 为蓝本生成，并保留 cartJump 作为「可 push 非 tab 页」的差异：

| 项 | 处理 |
|---|---|
| 备份 | `src/pages/cartJump/index.v1.vue`（休眠，不注册） |
| 顶栏 | 复用 `pages/cart/src/cartHeard.vue`，新增 `showBack` prop；cartJump 传 `:show-back="true"` |
| 跨境 `mainHeader` | **不引入**。原 cartJump 无 mainHeader；若照搬 cart 的 `isOverseasSkin` 分支，跨境下会丢掉返回键 |
| 灰条「已选/管理」 | 不保留——编辑入口已在 cartHeard，且本页始终有 navbar |
| 样式/底栏/空态/计步器 | 与 cart 同套（含二轮已修的空态高度、价格 `line-height:1`、`font-size:28rpx`、stepper 容器禁用） |
| 逻辑补齐 | 顺带对齐 cart 已有能力：`getProductUrl`+`shopId`、`formatActivityList(usedPromotions)`、`cartProductStyleMap`、`cart_item_disabled` class |
| 共用依赖 | `cartEmpty` / `discountInfo` / `detail` / `deliveryProductPop` 仍从 `@/pages/cart/src/` 引用 |

引用点未改：`pages/product/index.vue`、`pages/productV1/index.vue` 登录态仍进 `/pages/cartJump/index`。
