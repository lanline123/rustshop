# M6 · 跨境结算 / 订单展示（uniapp 端）

> 里程碑：P2 · 开关：`isOverseas()` / 数据驱动 · 依赖：M3（i18n/currency，已完成）· 不含支付渠道（M7）
> 红线：迁移≠重构、**禁止整页顶替**、热点页并行红线（check.vue/format-price/request.ts 同刻只属一模块）、开关置0行为不变、每 Task ≤800行/≤15文件一 commit。
> 状态：**已完成（2026-07-23）**。Wave A/B/C 全部落地，vort:check + 双构建(OVERSEAS=0/1)通过（详见下方各波实施结果与整体验收）。
> **决策**：D1=用 `@vort-if overseas` 代码切割隔离版本功能（国内分支原样保留；只做功能不做样式）/ D2=a 做全部功能项、**跳过纯样式项**（P2 订单 Tab `scrollable` 不做）/ D3=b 券/积分/余额仅在 overseas 分支改 `format-price`、国内裸展示。

---

## 一、S2 盘点结论（三仓 explore 实测，2026-07-23）

### 关键发现
- **跨境 `order/check.vue` 内零 `isOverseas()`/`@vort-if overseas`**：跨境逻辑分散在子组件；o2o `check.vue` 反而已有 `@vort-if !overseas` 隐藏发票（o2o 优于跨境 standalone）→ **check.vue 主体已具备，不整文件覆盖，只按子组件增量**。
- **税费是纯数据驱动**：跨境 `totalCard.vue` L52 `v-if="Number(total.taxRateAmount)>0 || Number(total.taxRate)>0"`，**未用 isOverseas 守卫**。国内 o2o 后端不返回税费字段 → 天然隐藏，零售零变化。
- **积分抵扣汇率**：跨境把积分/余额拆到独立 `pointsCard.vue`/`balanceCard.vue`，用 `currencyStore.rate` 换算；o2o 三合一在 `couponInfo.vue`，积分行 `currency+pointsAmount` **无汇率换算**。
- **format-price 双重换算风险**：`format-price` 在 `isOverseas()` 下已自动乘 `rate`（M3）。凡改用 `format-price` 的地方**不得再手动乘 rate**。

### 缺口清单（跨境有 / o2o 缺）
| # | 项 | 跨境来源 | o2o 现状 | 优先级 |
|---|---|---|---|---|
| 1 | 结算税费行 + 说明弹窗 | `order/src/totalCard.vue` L52-86 | 无税费 | P0 |
| 2 | 详情税费行 | `user/order/info.vue` L393-411, L848-856 | `info.vue` L353-405 无 | P0 |
| 3 | `taxRate`/`taxRateAmount` 类型 | `types/user/order.d.ts` L272-273 + total 类型 | 无字段 | P0 |
| 4 | 税费 i18n 文案（税费/税费说明/3条说明） | 跨境 locale | o2o 缺 key | P0 |
| 5 | 积分抵扣汇率换算 | `pointsCard.vue` L77-83 | `couponInfo.vue` L21-22 无 rate | P0 |
| 6 | 券头/余额账户改 `format-price` | 跨境 couponInfo/balanceCard | o2o L8 手拼 currency / L39-40 裸 balance | P1 |
| 7 | `storeCard` 「已选择{0}种」i18n | `storeCard.vue` L169 `isOverseas()?$t(...)` | o2o L316 中文硬编码 | P1 |
| 8 | 无效地址 `isInvalid` 标签 | `addressInfo.vue` L10 / `address/list.vue` L12 | 均无 | P1 |
| 9 | 订单 Tab 非中文可滚 | `user/order/list.vue` `:scrollable="locale!=='zh'"` | o2o `:scrollable="false"` | P2 |

### 范围外（不迁）
- 跨境 `check.vue` 发票无裁切（o2o 已正确）。
- 跨境独立文件 `pointsCard.vue`/`balanceCard.vue`/`productItem.vue`（o2o 已有等价挂载点，只迁逻辑不迁文件结构）。
- O2O 独有子组件（deliveryTypeCard/pickupUserCard/…）保留。
- 海外支付渠道 → M7。

### 热点触碰（M3 已处理，复用即可）
`format-price`（复用、勿双重换算）、`useCurrency`/`currencyStore`（汇率源）、`request.ts` X-Locale-Code（不改）、locale（补 key）、`types/user/order.d.ts`（补字段）。

---

## 二、决策点（待用户确认）

### D1 · 守卫打法（税费/无效地址等展示项）
- **(a) 纯数据驱动，镜像跨境（推荐）**：`v-if="taxRateAmount>0"` 等，不加额外开关。最保真（迁移≠重构，跨境怎么写就怎么写）、国内后端不返字段→零售零变化。缺点：代码进所有构建包（体量极小）。
- (b) 数据驱动 + `@vort-if overseas` 裁切：零售构建整段 cut，包更干净。缺点：比跨境多做标记、维护成本。
- (c) `isOverseas()` 运行时 v-if：介于两者之间。

### D2 · 本轮范围
- (a) **全做 P0+P1+P2（9 项，推荐）**：一次补齐结算/订单展示跨境缺口。预计 ≤10 文件，按波拆 commit。
- (b) 仅 P0（税费链路 + 积分汇率，1-5 项），P1/P2 下轮。

### D3 · 券/积分/余额 `format-price` 统一（缺口 6）
- (a) 统一改 `format-price`（对齐跨境，自动换汇，推荐）：需确保去掉手动 rate/currency 拼接，防双重换算。
- (b) 仅在 `isOverseas()` 下改，国内保持裸展示：更保守但两套分支。

---

## 〇·A、Wave A 实施结果（2026-07-23，税费链路）
- `order/src/totalCard.vue`：`@vort-if overseas` 税费行（`v-if="Number(taxRateAmount)>0 || Number(taxRate)>0"` 数据驱动）+ `?` 触发 `tig-popup` 税费说明弹窗 + `showTaxRateTips` ref + `Total` 局部类型加 `taxRate?/taxRateAmount?` + tax 样式块。适配 o2o 类名 `total-card-item-left/right`（非跨境 `card-item-label/value`）。
- `user/order/info.vue`：`@vort-if overseas` 详情税费行（`showTaxAmount`/`taxRateAmountValue` 两 computed，用 `orderInfo.value as any` 访问，未改共享类型）。
- **locale 无需改**：两仓 `en.json` 均占位，翻译走后端动态下发（M3 `getLangListData`+`X-Locale-Code`）；国内构建税费段整体裁切。
- **类型**：沿用 M8 做法，`as any`/局部 alias 兜底，不动 `types/user/order.d.ts` 共享类型。
- 验证：`vort:check` 通过、2 文件零 lint、`build:h5`(prod, OVERSEAS=0 裁切) + `uni build --mode development`(OVERSEAS=1 保留分支) **双构建通过**。

## 〇·B、Wave B 实施结果（2026-07-23，积分汇率/券/余额币种）
- `order/src/couponInfo.vue`（D3=b，沿用该文件既有 `isOverseas()` 运行时三元约定，国内裸展示分支原样保留）：
  - **积分抵扣**（P0 功能核心）：新增 `displayPointsAmount` computed（`isOverseas() && rate` 时 `pointsAmount * rate` 再 `moneyFormat`，对齐跨境 `pointsCard`），模板 `isOverseas() ? currency+displayPointsAmount : 原裸拼接`。
  - **券头**：`isOverseas()` 分支用 `format-price`（自动币种换算），国内保持 `优惠+currency+couponAmount`。
  - **账户余额**：`isOverseas()` 分支用 `format-price`，国内保持 `{{ balance }}` 裸展示。
  - 余额支付金额 `balanceAmount` o2o 本已用 `format-price`，无需改。
  - 新增 import：`moneyFormat`、`useCurrencyStore`。
- 验证：couponInfo 零 lint；两分支均运行时编译，随 `build:h5` 一并校验。

## 〇·C、Wave C 实施结果（2026-07-23，文案/无效地址）
- `order/src/storeCard.vue`：`已选择N种` 文案改 `isOverseas() ? $t('已选择{0}种', N) : '已选择N种'`（对齐跨境 L169），新增 `isOverseas` import。
- `order/src/addressInfo.vue`：`@vort-if overseas` 新增 `<view v-if="data.isInvalid" class="invalid-tag">无效地址</view>` + invalid-tag 样式，数据驱动、国内裁切。
- `address/list.vue`：同上，地址列表项 `@vort-if overseas` isInvalid 标签 + 样式。
- **样式项跳过**：订单 Tab `scrollable`（P2 纯样式）按决策本轮不做。

## 〇·D、M6 整体验收（2026-07-23）
- 改动文件：`totalCard.vue`、`user/order/info.vue`、`couponInfo.vue`、`storeCard.vue`、`addressInfo.vue`、`address/list.vue`（6 文件）。
- 隔离手法：跨境新增 UI 用 `@vort-if overseas` 裁切（税费/无效地址）；币种/文案类内联切换沿用该文件既有 `isOverseas()` 运行时三元（券/积分/余额/storeCard）。国内分支全部原样保留、零售零变化。
- 验证全绿：`vort:check` 通过、6 文件零 lint、`uni build --mode development`(OVERSEAS=1 保留 overseas 分支) + `npm run build:h5`(OVERSEAS=0 裁切) **双构建均 DONE**。
- 未做（按决策/超范围）：订单 Tab scrollable（样式）；`format-price` 全局统一（D3 取 b，仅 overseas 分支改）。

## 三、波次计划（每波 ≤15 文件 / ≤800 行，一波一 commit）
- **Wave A · 税费链路**：`types`(taxRate 字段) + `order/src/totalCard.vue` + `user/order/info.vue` + locale 税费 key。
- **Wave B · 积分/券/余额币种**：`order/src/couponInfo.vue`（积分汇率 + 券/余额 format-price）。
- **Wave C · 文案/地址/UX**：`storeCard.vue` i18n、`addressInfo.vue`+`address/list.vue` isInvalid、`user/order/list.vue` scrollable。

**并行红线**：M6 的 check.vue 子组件与 M7 的 pay.vue 错峰；本轮不与其它模块并行改 format-price。

## 四、风险
| 级别 | 风险 | 处置 |
|---|---|---|
| 🟠 中 | format-price 双重换算（手动 rate + 组件 rate） | 改 format-price 处必删手动 rate/currency 拼接；逐处核对 |
| 🟠 中 | 税费字段 total 类型 merge | 只增字段不改原有；三方对照 |
| 🟡 低 | 国内后端不返 taxRate → 数据驱动天然隐藏 | 保留 `v-if` 数据判空；无需 isOverseas |
| 🟡 低 | i18n key 缺失 → 显示中文字面 | 补 key；国内构建即使不补也不渲染（数据门） |
