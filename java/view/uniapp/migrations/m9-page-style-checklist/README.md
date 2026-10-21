# M9 · 整体页面样式待校验清单（交付物三）

> 配合《登录页装修与页面样式校验》计划 —— **交付物三**。
> 前置：交付物一（三版本登录页融合）已由同事完成。
> **参考蓝本已定（2026-07-24）：`vortmall-uniapp`**（`~/workspace/vortmall/vortmall-uniapp`）。vortmall 同样从 tigshop 演化而来、且**已完成三版本融合**（store/serviceStaff/langSet/giftCard 全都有），是本次样式对齐的**目标来源**。
> 创建：2026-07-23 ｜ 决策更新：2026-07-24

---

## 〇·〇、对齐原则（2026-07-24 与业主确认，全程遵守）

1. **平替非重写**：蓝本（overseas/vortmall）保留的东西一律不删、不改名、不简化。
2. **蓝本优先级**：通用功能**以 overseas（跨境）样式为准**；若 vortmall 的做法**能实现且不影响功能**，则对齐 vortmall。
3. **偏离必先停**：凡我判断"蓝本不好/有风险/可优化"的，**动手前先确认**，绝不先斩后奏、不臆造。
4. **涉及功能逻辑的改动不碰**（如抽奖 position 映射），保持原样。
5. **i18n 插值 key（含 `{0}` 等占位符）必须维护进本地语言包** `src/locale/zh.json`+`en.json`——否则小程序/App 会原样输出 `{0}`（见 `locale/index.ts` 的 `preprocessMessages`）。非插值 key 可走 fallback。

---

## 〇、工作方式与红线

- **节奏**：清单确认 → 每页**先做 `xxx.v1.vue` 休眠备份（不注册/不 import，单独 commit）** → 再单独小改 → 分别在 标准/O2O/跨境 三构建下目视验收。
- **禁改（他人负责）**：`product`（商详）、`search`、~~`cart`~~+`cartJump`。只读其公共依赖并知会 owner。
  - `cart` 已于 2026-08-03 由业主重新开单，独立为 **M10**（见 `migrations/m10-cart-style/README.md`）；`cartJump` 仍未动。
- **两维度不混用**：平台差异 `#ifdef`（外层），版本差异 `isOverseas()`/`isO2o()`+`@vort-if`（内层）。
- **零变化红线**：`isOverseas()===false`（标准/o2o）时 DOM/样式/路由 param **完全不变**；i18n 替换不影响中文展示。
- **迁移 ≠ 重构**：不改 props/命名/结构；顺手修 `Number(options.id)` 雪花 id（改 string 透传）。

---

## 一、对比基准（四仓）

| 角色 | 仓库 | 说明 |
|------|------|------|
| 标准版 | `Tigshop-Uniapp` | 通用电商基线 |
| **融合基座（要改）** | `Tigshop-o2o-Uniapp` | 靠 `isOverseas()`/`@vort-if` 同时服务 标准/o2o/跨境 |
| 跨境版 | `tigshop-overseas-uniapp` | 海外 UI（mainHeader/i18n/第三方登录）——可作 `isOverseas()` 分支现成蓝本 |
| **参考蓝本** | `vortmall-uniapp` | **已完成融合的目标样式**，`vort-*` 组件 + 扁平卡片 |

**vortmall 融合套路（贯穿全清单）**：`vort-layout`+`$t()` 标题；底栏 `vort-button2`；卡片扁平（页面 `#f5f5f5` 底、卡片白底无圆角、`margin-bottom:14rpx`、字号 24.5rpx）；`isO2o()`/`isOverseas()` 运行时分支 + `@vort-if` 构建裁切；整页海外 UI 用 `isOverseas()` 切、页内文案/图片用 `layoutRef.isZh` 切。

> 说明：o2o 现用 `tig-*` 组件。对齐时 **`tig-*`↔`vort-*`/`up-*` 视为等价**，重点对齐**样式与 i18n**，不强制换组件库（除非该组件行为差异导致视觉不一致）。

---

## 二、逐页决策表（2026-07-24 已拍板）

图例：✅要改（参考 vortmall）｜⏸先标记暂不处理｜❌不改｜🔎需先看差异再定

| # | 页面/组件 | 决策 | 说明 |
|---|-----------|------|------|
| 1 | `login` | ❌（另行处理） | 你单独弄，不在本交付物 |
| 2 | `order/check` + 拆卡 | ✅ 已完成 | 对齐 vortmall：拆卡架构 + 扁平卡 + 双行底栏（2026-07-28，详见 §二·六） |
| 3 | `address/list` | ✅ | 对齐 vortmall：flat 列表 + 复制 + `vort-button2`；保留 o2o swipe/无效地址 tag |
| 4 | `index` 首页壳 | ✅（装修，本交付物不改） | 属装修模块，非本交付物页面样式对齐范围；2026-07-30 业主确认关单 |
| 5 | `member` 个人中心 | ✅（装修，本交付物不改） | 属装修模块，非本交付物页面样式对齐范围；2026-07-30 业主确认关单 |
| 6 | `seckill/list` | ✅ | 跨境整页 UI 用 `isOverseas()` 分支（mainHeader/进度条/折扣标/图片按钮） |
| 7 | `buyerShow` | ✅ | 补 mainHeader/i18n/分享组件/弹层文案 |
| 8 | `im` | ✅ | 对齐 vortmall emoji 渲染链（`renderTextWithEmoji.ts`+`escapeHtml.ts`+HTML rich-text） |
| 9 | `user/order/info`+`list` | ✅ 已完成（样式+功能补齐） | 对齐 vortmall；2026-07-28 功能核对关单（见 §二·五） |
| 10 | `coupon` | ✅ 已完成（限定） | 对齐 vortmall：券名 `$t`、规则入口+弹层、修国内 `{1}` 插值 bug；不改领取态逻辑/按钮体系（2026-07-30） | ✅ `index.v1.vue` |
| 11 | `salesman` | ❌ | 样式无明显区别 |
| 12 | `register`/`reset` | ❌ | 不改 |
| 13 | `groupon` | ✅（限定） | **只做「英文排版模式」+ i18n 相关**：poster `isEnglishMode` 英文海报换行、`$t(productName)`、`word-break`、list 图标体系统一（详见 §四-13） |
| 14 | `lottery` | ✅ | 对齐 vortmall：`prizeEntryImage` 可配、空态 i18n、中奖 `<text>`、position+1 修复 |
| 15 | `exchange/list` | 🔎→见 §四-15 | 你说没区别；差异集中在**头部标题显隐 + 背景图国内/海外切换**（o2o 中文误用海外图） |
| 16 | `giftCard` | ✅确认无需整模块切割 | 跨境确有礼品卡，三版本共用一套即可；仅**按需**补 `transfer` 海外步骤 UI + `order/info` 进店分支（详见 §四-16） |
| 17 | `shop` | ✅ 已完成 | list i18n + 顶栏配色保留绑定 + scene/previewId/分享补齐（见 §四-17） |
| 18 | `article` | ❌ | 不改 |
| 19 | `activity` | ❌ | 不改 |
| 20 | `store`/`storeList` | ✅（限 i18n） | 只有 o2o 有；做 i18n 化（key 与 vortmall 同名，中文即 key） |
| 21 | `serviceStaff` | ✅确认只有 o2o | 标准/跨境均无此目录；仅 o2o+vortmall 有。非三版本视觉对齐问题 |
| 22 | `locationAddress`/`region` | ❌ | 不动 |
| 23 | `langSet` | ✅（加门禁） | 加运行时门禁：整页 `@vort-if overseas` + `onLoad` 非海外回退；会员入口 `!isOverseas()` 过滤（参考 vortmall） |
| 24 | `cart` 购物车 | ✅ 已完成（**独立为 M10**） | 2026-08-03 业主重新开单。纯视觉平替（不装修化）：顶栏 `cartHeard` 内嵌「编辑」、列表扁平卡、结算底栏对齐 cart-footer **样式二（单行）** + 编辑态底栏、空态横排。vortmall 没有的功能一律注释保留。详见 `migrations/m10-cart-style/README.md` | ✅ `index.v1.vue` |
| 25 | `cartJump` | ✅ 已完成（M10 同步） | 2026-08-03 业主拍板同步样式。以 cart 为蓝本；`cartHeard :show-back`；不引入 mainHeader（保返回键）。详见 `migrations/m10-cart-style/README.md` §五 |

**执行池（✅）**：2、3、6、7、8、9、13、14、20、23 ＋ 追加的 15、16、17。
**池子状态（2026-07-30）：执行池已全部完成**；#4/#5 属装修关单；#10 coupon 限定项（券名/规则弹层/{1} bug）已完成。#20 `store/` 非 P0 正文 i18n 另开一轮。

---

## 二·五、执行进度（滚动更新）

| # | 页面 | 状态 | 改动摘要 | v1 备份 |
|---|------|------|----------|---------|
| 23 | langSet | ✅ 已完成 | 仅加 `onLoad` 运行时门禁 `!isOverseas()`（navigateBack→switchTab member）。**未**在模板包 `@vort-if overseas`——本仓无 @vort-if 裁剪插件（`vite.config.ts` 仅 uni+Optimization，`@vort-if` 只是 `vort:check` 校验标记），整段包裹会留空 `<template>` 成隐患，且违反守则「不切碎模板」；整页文件级隔离已由 `.vort/feature-manifest.json` 的 `overseas.includes: src/pages/langSet` 负责。o2o 无 App 内 langSet 入口，无需改会员页。⚠️ 该文件有一处**既有**类型错误 `configStore.setHomeDecorateData(null)`（非本次引入） | 无（纯增量门禁） |
| 14 | lottery | ✅ 已完成（已拉回对齐 overseas） | 奖品入口图改装修可配 `prizeEntryImage`（缺省回退原静态图）；空态 `$t('暂无中奖记录')`；类型补 `prizeEntryImage?`。**中奖节点** `<view class="winner-text">`→`<text>` + CSS 改为 overseas 的 `.winner-item text` 嵌套（原先我擅自保留旧写法，已改正）。标题/剩余次数/活动规则(不sanitize) 三项 **overseas 本就与 o2o 一致，保持不动**；`position+1` **按业主要求不碰**（涉及中奖落点功能逻辑） | 无 |
| 3 | address/list | ✅ 已完成 | 模板+样式对齐 vortmall 扁平版：flat 列表(顶部分割线/无圆角)、user-info(名+电话)、address-desc(地址+复制)、operation 行(方形 radio「默认地址」+ edit/delete 图标)；去 swipe/去左侧 radio 列/去「默认」tag；底栏 `tig-button2 theme height:73rpx`+plus。**保留** o2o 脚本/API/`addressId`/`refreshAddress` 事件/`@vort-if overseas` 无效地址 tag | ✅ `list.v1.vue` |

| 17 | shop | ✅ 已完成 | list 4 处 `$t`；顶栏配色**保留绑定**（对齐标准版）；`index` 补 scene/previewId + `onShareAppMessage` + API `previewId`（从标准版平替） | 无 |
| 15 | exchange/list | ✅ 已完成（已拉回对齐 overseas） | 头部对齐 overseas：加 `layoutRef` ref；大标题 `v-if="!layoutRef.isZh"`；**修复中文误用海外图 bug**（新增 `exchangeBg` 国内 banner 作默认，`.not-zh` 才切海外图）。副标题**拉回 overseas 形式** `$t("当前积分{0}", [points])`（原先我擅自保留 o2o 的 `integralName` 拼接，已改正）；插值 key `当前积分{0}` 已维护进 `zh.json`+`en.json` | ✅ `list.v1.vue` |
| 8 | im emoji | ✅ 已完成 | 新建共享 `src/utils/escapeHtml.ts` + `im/src/renderTextWithEmoji.ts`（保留 `oss.tigshop.com` 基址，**补 `\n`→`<br/>` 避免丢换行**）；`emojiHtml.vue` 改 HTML 字符串渲染。`index.vue` 文本消息**按 vortmall 保留 `isEmojiText` 判断**：纯文本走原生 `<text>`（最安全、原生换行/长按复制），含 `[表情]` 才走 `emojiHtml`；`replaceText` 改名 `isEmojiText`，删除不再使用的本地 `escapeHtml`；`list.vue` 预览走 emojiHtml。三仓 emoji 管线统一、XSS 转义完整 | 无（渲染逻辑重构） |
| 13 | groupon | ✅ 已完成（限定范围） | 海报 `poster.vue` 的 `drawText` 换成 overseas 版（`langCode`+`isEnglishMode`+`measureText` 中英文按宽度换行，替换旧固定 10 字符切行）；商品名传参 + `grouponItem.vue`/`mylist.vue` 商品名补 `$t()`。**已拉回**：list 活动规则加 `style="word-break:break-all"`、grouponItem `.detail-title` 加 `word-break:normal`（对齐 overseas，原先我擅自跳过，已改正） | 无（targeted） |

| 6 | seckill/list | ✅ 已完成 | **业主定：不做版本分支，全构建统一用跨境样式**（单一布局）。照 overseas 忠实实现（红顶栏 head-box 自带返回按钮 + 火焰 label-box + 独立进度条「热销/已售罄」+ 折扣 `优惠{0}%` 标签 + 图片抢购按钮 + 白底 page），**去掉 `<mainHeader>`**（用跨境自带红顶栏返回）。功能点保留：`shopId` 透传、SEO、商品边框、`getDiscount`、`seckillPercentage`。移除 o2o 旧的渐变头图/进度条内嵌等。插值 key `优惠{0}%` 已进 zh/en.json。`list.v1.vue` 已覆盖为同事 `overseas.h5Style`+`mainHeader` 提交版（2763843），主文件仍为跨境样式 | ✅ `list.v1.vue`（同事版） |

| 7 | buyerShow | ✅ 已完成（i18n + video-icon；无 mainHeader/分享） | 对齐 overseas i18n：index/add/comments/CloseTip/CommentList/tabs/Waterfall；`buyerShowItem` 修 `video-icon` 仅视频显示。**未**引 `mainHeader`（沿用 `tig-layout` 标题）；**不迁** `buyerShowShare`/`buyerShowPoster`（2026-07-30 业主拍板关单：标准版与跨境版均无此能力，vortmall 有不代表 tig 上了该功能；且现网 vortmall 代码里亦查无这两个组件，`buyerShowDetail` 的 `poster` 仅为视频封面属性）。保留 o2o `shouldShowSoldOut`/`swiperHeight` | 无 |

| 16 | giftCard | ✅ 已完成 | `transfer.vue`：**不做版本分支**，全构建统一用跨境文字步骤 UI（步骤 2–4 文案，去掉截图 flow2–4）。`order/info.vue`：`handleToShop` 对齐 vortmall，走 `toStoreOrShopHome`+`isO2o()`。utils 按 vortmall 同款补 `buildQueryString`/`getStoreOrShopHomeUrl`/`toStoreOrShopHome` | 无 |

| 9 | user/order/list | ✅ 已完成（含功能补齐） | **业主拍板：列表整体照 vortmall，不折中**。头部行改「状态名 + 金额 + `icon-xiangyou`」（去订单号/复制）；商品区单商品大图详情、多商品 `scroll-view` 横滑图；「支付方式/总额/应付」整块按 vortmall 注释保留；去掉「订单详情」按钮（卡右上箭头 + 商品 `navigator` 进详情）；标题栏+tabs 收进 `.order-sticky-header`（非海外 `tig-navbar :fixed="false"` 入流，海外 `mainHeader position="static"`；`tig-layout :overflow="false"` 保 sticky）；tabs 白底 + `border-top`，窄屏/英文响应式 `scrollable`+padding；按钮全换 `tig-button2`。**保留 o2o 功能**：提货凭证、预约时间、周期购/次卡、拼团再购、赠品/预售/拼团标、extraSkuData、autoDeliveryDays。**补齐（2026-07-28 按 vortmall 复核）**：`sortField/sortOrder`；预售尾款未到禁用。**不展示**（vortmall 无 / 业主确认删）：共 X 件、订单号复制、支付行、订单详情按钮、多商品纵列 | ✅ `list.v1.vue`（勿改） |
| 9 | user/order/info | ✅ 已完成（含功能补齐） | 壳 `tig-layout`（无 title）+ `tig-navbar $t('订单详情')`，客服移 navbar 右上角；地址上移 `address-box`；卡片重排 + 可折叠金额卡；底栏 `tig-button2`，预售尾款并入宽按钮+禁用态。**保留 o2o**：周期购/CyclePlan、配送员服务员、税费、运费文案矩阵、售后/pos/自提门禁等。**补齐（2026-07-28 按 vortmall 复核）**：抽奖单 `orderType !== 14` 底栏总闸 + 单品/整单售后。**不展示**（vortmall 无）：「(含增值税)」 | ✅ `info.v1.vue`（勿改） |

> #9 **状态：样式对齐 + 功能补齐均已完成**（2026-07-28 业主确认可标记）。
> #9 i18n：插值 key `订单将在{0}天后自动收货`、`共{0}张` 已进 zh/en；其余非插值新 key 中文回退，英文属 M4。
> #9 功能核对结论：真补 = `orderType!==14` + `sortField/sortOrder`；跨境独有「共 X 件」「(含增值税)」按 vortmall 蓝本不补；会话确认删除的展示（订单号复制/支付行/订单详情按钮/多商品纵列等）不回补。v1 备份为客户原始代码，**禁止改动**。
> #9 目视验收：登录态下标准/o2o/跨境三构建自测即可（非阻塞关单）。

> **mainHeader 依赖说明**：overseas 秒杀/买家秀/首页顶部原用 `components/mainHeader`(589 行，M5 已结案定为「外贸风 H5 皮肤专项」缓做、接入碰红线)。业主决定 **这些页一律用 o2o 头 / 跨境自带顶栏、不引 mainHeader**。#7 buyerShow 同此处理。

| 10 | coupon | ✅ 已完成（限定） | 券名 `$t(couponName)`（list+detail）；`couponDesc` 改为「规则」入口 + `tig-popup` 弹层；修国内分支 ``有效期{1}天`` → ``${useDay}``；插值 key `领券{0}天后生效…`/`领券当日起{0}天内可用` 补进 zh/en。**未**改领取态/防连点/按钮体系 | ✅ `index.v1.vue` |

| 20 | store/storeList i18n | ✅ 已完成（P0） | **storeList 已完成**（`f8a7089`）；**store/ 目录 P0 可见文案已完成**（2026-07-30，见 §二·七）；①正文半 i18n / ②中文状态判断 / ③单字标签 **均不处理**（跨境无对标页；业主拍板） | 无（纯 i18n） |

| 2 | order/check + 拆卡 | ✅ 已完成 | 拆卡架构（含三处文件级拆分，业主 2026-07-28 拍板"全做"）+ 扁平卡 + 双行底栏；见下方 §二·六 详述 | ✅ `check.v1.vue` + `couponInfo.v1.vue` / `storeCard.v1.vue` / `totalCard.v1.vue` |

| 24 | cart 购物车 | ✅ 已完成（M10，2026-08-03） | 顶栏 `cartHeard`（navbarContent + 编辑）；列表扁平化；计步器 `tig-number-stepper(is-delete)`；底栏对齐 cart-footer **样式二单行**（全选 + 合计/优惠/明细 + 旁侧圆角去结算）+ 编辑态底栏；空态横排。详见 `migrations/m10-cart-style/README.md` | ✅ `index.v1.vue` |

> #2 **状态：拆卡 + 样式对齐均已完成**（2026-07-30 业主确认可标记关单）。
> #2 目视验收：登录态下标准/o2o/跨境三构建自测即可（非阻塞关单）。重点看 o2o 自提场景（自提点 + 自提时间）与预约到店 `flowType=9`——这两条路径走的是新拆出的 `storeCardStore`。
> #2 前置修复：`.env.development` 补 `VITE_IS_STORE = 1`（原缺失会让 dev 下 `isO2o()` 恒 false，结算页掉到非 o2o 版）。详见 §二·六。

> 待验收（累计 11 页 + storeList）：需在 标准/o2o/跨境 构建下各目视。commit 未执行（你自行提）。
> ⚠️ i18n 新增 key（店铺街/商品数/进店/积分兑换/人团/商品名 等）在中文走 key 回退零变化，**英文翻译属 M4**（missing-key 上报）。

---

## 二·六、#2 `order/check` 拆卡详述（2026-07-28）

业主拍板 **§四-2 全做**，含三处文件级拆分。改动如下。

### 新增/重写文件（`src/pages/order/src/`）

| 文件 | 状态 | 说明 |
|---|---|---|
| `productItem.vue` | 新增 | 结算页商品行（vortmall 蓝本布局：155rpx 图 + 24.5rpx 标题/规格 + 价格行）。**保留 o2o 能力**：活动标 `activityCard`（type 1/6）、`originPrice` 划线价。未引 vortmall 的 `% off`（本仓无 `calcDiscountPercent`，跨境亦用划线价） |
| `storeCard.vue` | 重写为**非 o2o 版**（标准/跨境） | 照 vortmall：单商品走 `productItem` 大图行、多商品横滑缩略图 +「N 件」+ 全部商品弹层；赠品；只读「配送方式」行；`presaleCard` 只在首店展示一次；附加规格弹层。扁平卡（无圆角、`padding:20rpx 28rpx`、`margin-bottom:14rpx`、`font-size:24.5rpx`） |
| `storeCardStore.vue` | 新增 = 原 `storeCard.vue` | **o2o 全功能版**，o2o 能力一个没删（自提点/自提时间/周期购 `cyclePop`/预约 `dateStockCard`/`extraSkuData`/附加服务/次卡/预售/起送提示）。仅改：`.store-card` 与 `.card-tip` 去圆角 + `margin-bottom:14rpx`；补 `$t()`（还差/就能起送/自提时间/自提点/附加/请选择 + 星期与时间映射表用 `t()`） |
| `totalCard.vue` | 重写为**非 o2o 版** | 照 vortmall：`小计(N件)`/附加费用/配送费用/使用余额/`{integralName}减免`/**订单合计 + 共减**（带上分割线）。**保留**：`@vort-if overseas` 税费行 + 税费说明弹层、`orderSendPoint` 赠分提示、`共优惠` 明细入口（vortmall 把这个入口丢了，按「不删功能」保留） |
| `totalCardStore.vue` | 新增 = 原 `totalCard.vue` | **o2o 版**，保留次卡抵扣/账期/运费行显隐矩阵（自提不显示、同城与上门即使 0 也显示）/运费规则弹窗入口/跨境税费。仅去圆角 + `margin-bottom:14rpx` + `font-size:24.5rpx` |
| `couponInfo.vue` | 瘦身为**仅优惠券** | 只剩券行（`-¥金额` + `icon-fanhui`）与券选择弹层；弹层按钮换 `tig-button2`。原「积分/余额/账期」整块移出 |
| `pointsCard.vue` | 新增 | 积分行 + 积分输入弹层。按 vortmall 把「最多可用」从行内移到弹层（`该订单最多可用 N`），行内改为 `-¥抵扣额`。**保留** `integralName` 动态名与跨境汇率换算 `displayPointsAmount` |
| `balanceCard.vue` | 新增 | 余额行 + 开关 + 「支付 ¥x」。保留跨境 `format-price` 换算分支、跳转账户页 |
| `creditCard.vue` | 新增（**o2o 独有，vortmall 无**） | 账期行 + 剩余额度/逾期与停用文案 + `switch-mask` 禁用遮罩 |

### `check.vue` 改动

- **壳**：`title="提交订单"` → `:title="$t('结账')" :border-bottom="true"`；`:deep(.layout)` 灰底 `#f5f5f5` + `min-height:100vh`。
- **模块顺序**改为 vortmall 序：地址/配送 → **店铺卡** → **支付方式** → 备注 → 优惠券 → `num-card`(积分+余额+账期) → 发票 → 批发提示 → 合计 → 次卡规则。（原来支付方式在店铺卡之前）
- **`isO2o()` 分支**：`storeCardStore`/`stroeCard`、`totalCardStore`/`totalCard` 两组，外面包 `@vort-if o2o` 标记。
- **抵扣卡组装**：新增 `showCouponCard`/`showPointsCard`/`showBalanceCard`/`showCreditCard` 四个 computed（把原来那条 `!useGiftCard && (…)` 长条件按卡拆开，`flowType != 3` 判断从组件内提到页面级）；余额⇄账期互斥的 ref 由 `couponInfoRef` 换成 `balanceCardRef` / `creditCardRef`（`onShow` 重置同步改）。
- **双行底栏**：`tig-fixed-placeholder :height="218"` + `check-info`（标签 + 总额 +「共减」）+ `tig-button2 height:70rpx theme`。**保留** o2o 的「定金/仍需支付」切换（新增 `isPresaleDeposit` computed）与 `flowType==3` 积分兑换展示；超范围提示改 `beyond-tips` 绝对定位并补 `$t()`。
- 保留：`class="store-card"`（`submit()` 里 `createSelectorQuery` 依赖它滚动定位）、`refreshAddress`/`refreshUserPickup`/`refreshStorePickup` 事件、`tabDeliveryTypes` 收敛、`@vort-if common.wholesale` 批发提示、`@vort-if o2o.cycle` 次卡规则。

### 顺带扁平化的既有卡片

`remark` / `paymentMode` / `invoiceInfo` / `subCardRule` 去圆角 + `padding:20rpx 28rpx`(或 21rpx) + `margin-bottom:14rpx` + `font-size:24.5rpx`；`addressInfo` 去圆角 + `margin-bottom:14rpx` + 「请选择收货地址」补 `$t`；`deliveryTypeCard` 内 `.card` 去圆角；`pickupUserCard` 默认 `borderRadius` 改 `"0"` 并补 `$t()`，`check.vue` 用 `.pickup-user-card-wrap { margin-bottom:14rpx }` 包裹（对齐 vortmall）。

### ⚠️ 顺带修的前提 bug：`.env.development` 缺 `VITE_IS_STORE`

`isO2o()` 读构建期变量 `VITE_IS_STORE`。`.env.production` 有 `VITE_IS_STORE = 1`，但 **`.env.development` 没定义** → dev 下 `isO2o()` 恒为 `false`。若不修，本次 `isO2o()` 分支会让 **dev 环境的结算页掉到非 o2o 版**，自提点/自提时间/周期购/预约/运费入口/次卡/账期全部消失。已在 `.env.development` 补 `VITE_IS_STORE = 1`（与 production 一致）。
> 该缺失同时影响 #16 giftCard 的 `handleToShop`（`toStoreOrShopHome`+`isO2o()`），一并修好。

### 说明与已知取舍

- 本仓**无 `@vort-if` 裁剪插件**（同 #23 结论），`storeCard`/`storeCardStore` 两份都会进包（`pages/order` 是分包，实测 692K，主包无影响）。文件级拆分的收益是**运行时分流**与与 vortmall 的结构对齐，不是体积。
- vortmall `storeCardStore` 另有 `isPickupTimeEnabled` / 「无需选择自提时间」等**功能性**增强，属功能而非样式，本轮未纳入。
- vortmall 非 o2o `storeCard` 的商品弹层有自定义关闭图标（依赖 `order/close.png`），本仓无该静态资源，改用 `tig-popup` 自带 closeable。
- 验证：`type-check` 通过（仅 2 条既有 `TS6504` 与本次无关）、`vort:check` 配对通过、`eslint src/pages/order` 无新增问题（残留均为 `pay.vue`/`payment.vue`/`presaleCard.vue`/`animation.vue` 既有）、`build:h5` 与 `build:mp-weixin` 均成功。
- i18n：本轮**未新增插值 key**；顺带把既有插值 key `已选择{0}种` 补进 `zh.json`+`en.json`（原先缺失，小程序/App 下跨境会原样输出 `{0}`）。其余新 key 中文走回退零变化，英文属 M4。

---

## 二·七、#20 `store/` 目录 P0 i18n（2026-07-30）

业主拍板「先做 P0 可见文案：`store/index`、`select`、`searchStore`、`selfPick`、`src/*` + 各页 `tig-layout` 标题，跳过状态判断类改造」。key 一律 **中文即 key**，与 vortmall 同名（`已售`/`人评`/`距您`/`共减`/`已选商品`/`每件预计到手`/`购物车`/`预估合计：`/`门店相册`/`公告`/`营业时间`/`全部`/`距离最近`/`切换城市` 等已核对一致）。

| 文件 | 改动 |
|---|---|
| `store/index.vue` | 搜索占位（顶栏 + 旧版头）、「当前地址附近无可配送门店…」、「送出」、营业时间、已关注/关注、已售、人评、周期购/次卡兑换标、action-sheet 取消；脚本 `t()`：同步选中失败 toast、自提/配送 tab 默认标题（装修配置的自定义标题不动）、暂无联系电话/位置信息、`getDistanceText` 的 `距您${}` 改 `t("距您") + formatDistance()`（对齐 vortmall，不引插值 key） |
| `store/select/index.vue` | 附近/关注 tab（`cover-view` 与普通 view 两套）、搜索门店占位、取消；脚本 `t()`：定位中、暂无位置信息、进入门店/拨打电话 |
| `store/searchStore.vue` | `tig-layout` 标题、搜索门店占位、取消×2；脚本同上 |
| `store/selfPick.vue` | 定位中、请输入搜索门店地址、定位中.../重新定位、距离最近、取消；脚本 `t()` 暂无联系电话×2 |
| `store/src/cartPop.vue` | 已选商品、共减、清空、每件预计到手；两个 `showModal`（提示/确认要删除该商品吗？/确认要清空购物车吗？/取消/确认） |
| `store/src/footerCard.vue` | 购物车、预估合计：、去结算 |
| `store/src/infoPop.vue` | 门店相册、门店信息、营业时间、门店电话、门店地址、公告；toast 暂无联系电话 |
| `store/src/settlement.vue` | 去结算 |
| `store/src/storeItemCard.vue` | 当前选择、距您 |
| `store/src/mainCategory.vue` | 点击收起 |
| `store/src/subCategory.vue` | 全部 |
| `tig-layout` 标题 | `comment.vue`「门店评价」、`contacts/list.vue`「联系人管理」、`order/voucher.vue`「提货凭证」、`searchStore.vue`「搜索门店」；`contacts/editContacts.vue` 的 navbar `title` ref（新增/编辑联系人）改 `t()` |

### 有意跳过（需业主确认后再动）

- **`store/src/storeNav.vue`**：模板本就是 `$t(item.label)`、label 为中文，等于已 i18n，**无需改**（vortmall 在 data 里又包了一层 `t()`，属重复翻译，未跟）。
- **单字/拆字标签**：`mainCategory` 竖排「全」「部」、商品「拼」团标 —— **不处理**（2026-07-30）：跨境无 `store/`；vortmall store 同样硬编码未包 `$t`/无英文态。跨境别处拼团标是完整词 `$t("拼团")`，非单字窄框，不可对标。
- **中文状态判断**：`order/info.vue`、`reserveInfo.vue`、`order/src/subCardContent.vue` 里的 `statusName === '已过期'` / `orderStatusName !== '待支付'` 等 —— **不处理**（2026-07-30，业主拍板；属功能改动）。
- **未纳入 P0 的内容文案**（order/contacts 正文半 i18n）—— **不处理**（2026-07-30）：跨境无 `store/order`/`store/contacts`，无可对标项。说明：融合蓝本 vortmall 在 contacts 与核销相关文案上比 o2o 更全 `$t`，但自提前缀/状态描述等两边仍半 i18n；按「以跨境为准、跨境没有就不做」关单。

### 验证

本轮 **未新增插值 key**（全部为非插值，中文走 key 回退零变化），无需改 `zh.json`/`en.json`；英文翻译属 M4。`type-check` 通过（仅 2 条既有 `TS6504`）、`vort:check` 配对通过、`eslint src/pages/store` 报错行与本次改动行无重叠（均为既有，`editContacts` 反而少一条缺分号）、`build:h5` 与 `build:mp-weixin` 均成功。

> ⚠️ 目视重点：小程序/App 端 `store/select` 的「附近/关注」在 `cover-view` 里，插值渲染需真机确认；`store/index` 的自提/配送 tab 若后台配了自定义标题，应仍显示后台文案（只有缺省才走 `t()`）。

---

## 三、执行顺序建议（每项独立 v1 备份 + 独立 commit）

按「风险可控 / 收益 / 体量」排：

1. **P0 独立小页**：`23 langSet 门禁` → `14 lottery` → `3 address/list`（改动清晰、易验收）
2. **P1 i18n 批量**：`20 store/storeList i18n` → `13 groupon 英文排版+i18n` → `8 im emoji 链`
3. **P1 跨境整页分支**：`6 seckill`（`isOverseas()` 整页分支，可直接抄 overseas 同名文件）→ `7 buyerShow`
4. ~~**P2 结算重头（体量最大，最后做）**：`2 order/check 拆卡` → `9 user/order/info+list`~~（✅ 均已完成）

> 每步完成后 `type-check` + `build:h5` + `build:mp-weixin`（主包 < 2MB），三版本各目视一遍再关单。

---

## 四、各页对齐要点（vortmall 蓝本，含路径行号）

> 通用红线：所有跨境专属改动包在 `isOverseas()`/`@vort-if overseas`；标准/o2o 零变化。

### 2 · order/check + 拆卡（体量最大）
vortmall：`vortmall-uniapp/src/pages/order/check.vue` + `order/components/*`
- 壳：`tig-layout title="提交订单"` → 灰底 `#f5f5f5` + `$t('结账')`；模块顺序 地址→店铺卡→支付→备注→抵扣→发票→合计。
- **拆卡**：把 o2o 合并式 `couponInfo`（券+积分+余额+账期）拆为 `couponInfo`(仅券)+`pointsCard`+`balanceCard`；`storeCard` 抽 `productItem`，用 `isO2o()` 切 `storeCard`/`storeCardStore`；`totalCard`/`totalCardStore` 拆分。
- 底栏：单行 `tig-button` → 双行 `check-info` + `vort-button2 height:70rpx`。
- 卡片：去 `border-radius:18rpx`，统一 `padding:20rpx 28rpx; margin-bottom:14rpx; font-size:24.5rpx`；箭头 `icon-fanhui`。
- **保留 o2o 能力**（用标记包，勿删）：`@vort-if common.wholesale` 批发提示；`@vort-if o2o.cycle` 次卡/subCardRule；账期；`tabDeliveryTypes`；`refreshAddress` 事件。

### 3 · address/list
vortmall：`address/list.vue`
- 壳 `$t('收货地址')`；列表 flat（`border-top:1rpx #e5e7eb`、项 `padding:16rpx 32rpx`、无圆角）；底部 operation 行（默认 radio + edit/delete 图标）；**新增地址行「复制」**；底栏 `vort-button2 height:73rpx theme`+plus。
- **保留 o2o**：`@vort-if overseas` 无效地址 tag、`refreshAddress` 事件（check 依赖）；swipe 可保留为增强但样式向 vort 靠。字段 `addressId` 保持。

### 6 · seckill/list
vortmall：`seckill/list.vue`（跨境独立仓 `tigshop-overseas-uniapp/.../seckill/list.vue` 结构几乎一致，可直接作分支蓝本）
- `isOverseas()` 双分支：跨境分支 = mainHeader(sticky) + 红顶栏 + **标题下独立库存进度条**（158×10rpx 渐变 + 热销/已售罄）+ 折扣`%`标签 + 价格 `#1a1a1a 30rpx` + 图片按钮；标准/o2o 分支保留现有背景图版。
- 补类型 `seckill.d.ts` 的 `productPrice`；确认 `fire.png`/`seckill.png`/`overseas-seckillTit.png` 资源。保留 o2o `shopId`/`useProductStyle`/`setPageMeta` 在标准分支。

### 7 · buyerShow
vortmall：`buyerShow/index.vue`+`buyerShowDetail.vue`+`components/*`
- P0：禁用态补 `mainHeader`；固定头布局对齐（`padding-top:199rpx`、z-index 400/399）；FAB `publishFabStyle` 动态 bottom；全量 i18n（标题/搜索/Tab/param）；`buyerShowCloseTip` 文案 i18n。
- P1：详情迁入 `buyerShowShare`/`buyerShowPoster`（`isOverseas()` 控 Facebook 分享）；路由 param 统一 `id`；`buyerShowItem` 修 video-icon 条件 + 点赞逻辑；`comments`/`CommentList`/`WaterfallLayout`/`addBuyerShow` 全量 i18n。
- 可选：`tig-layout` 补 `--nav-title-height`（对齐 `vort-layout`）。

### 8 · im emoji 渲染链
vortmall：`im/components/renderTextWithEmoji.ts` + `src/utils/escapeHtml.ts`
- 新建 o2o `src/utils/escapeHtml.ts`、`im/src/renderTextWithEmoji.ts`（表情 OSS 基址保持 `oss.tigshop.com`）。
- 改 `emojiHtml.vue` → HTML 字符串 + `renderTextWithEmojiHtml`；`index.vue` L51-53 `replaceText`→`isEmojiText`（无 emoji 用 `<text>`，删内联 escapeHtml）；`list.vue` L26 改 `rich-text :nodes="renderTextWithEmojiHtml(...)"`。
- 增强项（vortmall 未做）：如需保留换行，`\n`→`<br/>`。

### 9 · user/order/info + list
vortmall：`user/order/info.vue`+`list.vue`
- info：`vort-navbar $t('订单详情')`（o2o title 现误为「我的订单」）；地址/自提/预约块上移到 `order-info-steps` 内 `address-box`；卡片 flat、label 色 `#1a1a1a`；底栏 `vort-button2`；保留 `@vort-if o2o.cycle`/`o2o.serviceStaff`。
- list：加 `mainHeader`+`order-sticky-header`；列表头改「状态+金额+箭头」（弱化订单号）；商品区单/多布局；按钮 `vort-button2`；保留 subCard/预约时间（`@vort-if o2o.cycle`）。

### 13 · groupon（**只做英文排版 + i18n**）
- **英文海报排版（你点名要加）**：把跨境/vortmall 的 `drawText`（`groupon/.../poster.vue` 的 `langCode`+`isEnglishMode`+`canvas.measureText` 英文单词换行逻辑）合入 o2o `groupon/src/poster.vue`（现为固定每 10 字符切行，无英文分词）；商品名传参先 `t(productName)`。
- i18n：`grouponItem.vue`/`mylist.vue`/`poster.vue` 商品名补 `$t(item.productName)`；活动规则加 `word-break:break-all/normal`。
- 图标体系：list 页「活动规则/价格排序」`uni-icons` → `iconfont-h5`（对齐 o2o/vortmall；注：**o2o 现已是 iconfont，此项主要针对跨境分支若合入时保持一致**）。

### 14 · lottery
vortmall：`lottery/index.vue`（overseas 已部分对齐）
- `title` i18n；中奖滚动 `<view class="winner-text">`→`<text>` + vort CSS；新增 `prizeEntryImageSrc` computed 读 `decorateConfig.prizeEntryImage`；剩余次数占位符 i18n；空态 `$t('暂无中奖记录')`+居中；**修复** grid position 需 `+1`（`res.position+1`，潜在 bug）；活动规则 `sanitizeHtml`。

### 20 · store/storeList i18n
vortmall key 规律：**中文文案本身作 `$t()` key**（如 `$t('试试搜索门店或服务')`）。
- 首批：`storeList/index.vue`（L16/26/43/90/121/127/131/157/364/382）+ `storeList/src/storeFilter.vue`（L18/29/30）。
- `store/` 整目录硬编码点较多（`index.vue`、`select/index.vue`、`searchStore.vue`、`src/*`、`selfPick.vue`、`contacts/*`、`comment.vue` 等，详见勘察附录），建议本项 scope 先锁 storeList + store 主页面，`store/order/*` 视需要另开一轮。

### 23 · langSet 门禁
参考 vortmall（多层防护，o2o 已有 pages.json/permission/feature-manifest 的 `@vort-if overseas`，仅缺页面守卫与入口过滤）：
- `langSet/index.vue`：整页 `@vort-if overseas`；调试兜底可加 `onLoad`：`if(!isOverseas()){ uni.navigateBack({fail:()=>uni.switchTab({url:'/pages/member/index'})}); return; }`。
- `member/components/decorate/account/index.vue`：加 `!isOverseas()` 过滤「国家/地区和语言」入口 + 默认链接 `@vort-if overseas`（对齐 vortmall L94-116）。

---

## 五、🔎 需你先看差异再定的项（我已查清具体不同点）

### 15 · exchange/list —— 你说「没区别」，实际差异在头部
- **标题显隐**：o2o `.header-title` **始终显示** `${integralName}兑换`（L7）；vortmall/跨境用 `layoutRef.isZh` 控制——**中文隐藏大标题、只留副标题**，英文才显示 `$t("积分兑换")`。
- **背景图（关键 bug 级）**：o2o 只定义了 `overseasExchangeBg` 并**直接绑定**（L67-69/L93-102），即**中文环境也用了海外装饰图** `overseas-exchange-bg.png`；vortmall/跨境中文用国内图 `exchange-bg.png`、英文经 layout `.not-zh` 才切海外图。这是融合遗漏。
- 列表瀑布流本体差异极小（仅 vortmall 未传 `styleMap` 商品边框）。
- **建议**：值得改**头部区域**（补 `exchangeBg` + `layoutRef.isZh` 标题显隐 + `.not-zh` 背景切换），列表不用动。→ 要不要纳入执行池？

### 16 · giftCard —— 确认无需整模块切割（你猜对了）
- 跨境、vortmall 都有 giftCard，且**三版本共用一套分包**（feature-manifest 里 giftCard 不在任何 slice）。**不需要**按版本剥离。
- vortmall 仅有两处**运行时**分支（非整模块）：`transfer.vue` 海外用文字步骤 UI（非海外用截图流程）；`order/info.vue` 进店逻辑 `toStoreOrShopHome`+`isO2o()`。o2o 目前都固定走非海外/旧逻辑。
- **建议**：giftCard 保持共用即可；上面两个按需分支要不要补？（不补也不影响国内）

### 17 · shop —— 已关单（2026-07-30）
- **① list i18n**：✅「店铺街/商品数/收藏数/进店」包 `$t`（中文即 key）。
- **② 顶栏配色**：✅ **保留绑定**（对齐标准版 tig；零变化。vortmall/跨境不绑不跟）。
- **③ scene/previewId**：✅ 从标准版平替——`getShopDecorate(id, previewId?)`；`index` 解 `scene` + `previewId`、透传 decorate、补 `onShareAppMessage`。

### 21 · serviceStaff —— 确认只有 o2o
- `Tigshop-Uniapp`（标准）、`tigshop-overseas-uniapp`（跨境）**均无** `serviceStaff/` 目录；仅 `Tigshop-o2o-Uniapp` 与 `vortmall` 有。属 O2O 独有工作台，**非三版本视觉对齐问题**，样式层无需动。

---

## 六、待你拍板

> 原先 1、2 两问（执行顺序、🔎 三项是否追加）**已于 2026-07-24～28 全部拍板并执行完毕**，§三 顺序已走完、15/16/17 已纳入并完成。

交付物三执行池已清空；#4/#5/#10 已关单；#24 cart 已由 M10 完成。剩下这些还没定：

0. **#25 `cartJump`**：✅ 已于 2026-08-03 同步样式（选 ①）。详见 `migrations/m10-cart-style/README.md` §五。


1. **#20 `store/` 剩余 i18n**：P0 可见文案已于 2026-07-30 完成（§二·七）。① 正文半 i18n、② 中文状态判断、③ 单字标签 —— **三项均不处理**（②③ 业主拍板；① 跨境无 `store/order`/`contacts` 无可对标）。
2. **#17 shop 两项产品决策（2026-07-30 已拍板）**：
   - ② 顶栏配色：**保留绑定**（对齐标准版 tig，零变化；vortmall/跨境不绑属融合遗漏，不跟）。
   - ③ scene/previewId：**已补齐**——从标准版平替：`getShopDecorate` 加可选 `previewId`；`shop/index.vue` 解 `scene` + `previewId`、透传 decorate、补 `onShareAppMessage`。无参进店路径零变化。
3. **英文翻译**：不在本交付物。M9 只包 `$t()` / 中文 key；英文文案统一归 **M4**，此处仅备忘。
