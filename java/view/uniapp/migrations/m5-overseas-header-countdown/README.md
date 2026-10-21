# M5 · 跨境 mainHeader / countdown（uniapp 端）

> 里程碑：P2 · 开关：`isOverseas()`（归属 `overseas.h5Style` 外贸风皮肤）
> 红线：迁移≠重构、**禁止整页顶替**、热点页并行红线、`isOverseas()` 守卫、每 Task 一 commit。
> 状态：**已结案（2026-07-22，零代码改动）**。决策 D1=c（mainHeader 皮肤另立专项）/ D2=a（decorate countdown 保留 o2o 现状）。

---

## 〇、结案结论（2026-07-22）
- **D1=c**：本期**不做** mainHeader / `components/countdown` 外贸风皮肤（接入 8 个热点页命中红线）。登记为独立专项「**跨境外贸风 H5 皮肤（overseas.h5Style）**」，含 `components/mainHeader`(589) + `components/countdown`(216) + 8 页 `isOverseas()` 接入 + decorate countdown 的 product-style-border 增强。
- **D2=a**：`pages/customPage/modules/countdown` **保留 o2o 现状不动**（o2o 的 `$t()` + `!showB2bText` b2b 门控是 o2o 需要的；overseas 的 product-style-border 属皮肤增强，随皮肤专项迁）。
- **M5 净代码改动 = 0**（分析结案，符合"迁移≠重构、不碰热点页、不破坏功能"）。无需构建验证。

---

## 一、S2 实测订正（2026-07-22，与清单 §一 出入）

清单原写"`components/countdown`(216) `mainHeader`(589) 跨境独有，迁入 M5"。逐文件核实后：

### 1.1 三个 countdown 需区分
| 资产 | o2o | 跨境 | 性质 |
|---|---|---|---|
| `components/countdown/index.vue`(216) | ✗ | ✓ | **跨境独有**：顶部促销倒计时条，被 mainHeader/商品页用 |
| `components/commonCountdown/index.vue` | ✓ | ✓ | 两仓都有（倒计时基础件） |
| `pages/customPage/modules/countdown/index.vue` | ✓ | ✓ | 两仓都有，**双向漂移**（见 1.3） |

### 1.2 mainHeader 是"跨页皮肤"，非独立小组件
- `components/mainHeader/index.vue`(589)：**跨境独有**，o2o 无。
- 依赖 `components/countdown`（外贸风顶部条）、configStore、cartStore 等。
- **被 8 个核心页消费**（`isOverseas()` 分支处应用）：`pages/index`、`pages/cart`、`pages/product`、`pages/search/index`、`pages/search/result`、`pages/seckill/list`、`pages/user/order/list`、`pages/productCate/src/topHeader`。
- → 迁组件本身易；**"让 o2o 用上"要改 8 个热点页**，属大范围改动，命中"禁止整页顶替 / 热点并行"红线，且与 `overseas.h5Style` 外贸风皮肤重叠。

### 1.3 decorate countdown 双向漂移（`pages/customPage/modules/countdown`）
- **o2o 独有**：`$t()` 包裹装修标题、`v-if=...&& !showB2bText`（`isShowB2bPriceText()` 的 b2b 价格文案门控）、commonCountdown 从 `@/pages/customPage/modules/commonCountdown` 引入。
- **overseas 独有**：`product-style-border`（styleMap borderImage）+ `useProductStyle` hook、commonCountdown 从 `@/components/commonCountdown` 引入、无 b2b 门控、无 `$t()`。
- → **不可单向取 overseas**：会丢 o2o 的 b2b 门控与 i18n。属需谨慎的三方合并。

## 二、结论
M5 名为"迁两个组件"，实为**跨境外贸风 H5 皮肤（overseas.h5Style）**的一部分：mainHeader + components/countdown 是皮肤资产，只有接入 8 个页面才有意义。这是一个**大模块**，宜与外贸风皮肤专项统一规划，不宜在"P2 小步"里贸然接入热点页。

## 三、决策点

- **D1 · mainHeader / components/countdown 范围**：
  - (a) **本期只迁组件资产**（`components/mainHeader` + `components/countdown`）进 o2o + 登记 `overseas.h5Style`，**不接入页面**（皮肤接入留待"外贸风皮肤"专项）。最小、不碰热点页。→ 但会产生"暂未使用"的组件。
  - (b) 迁组件 **+ 逐页接入 8 个热点页** `isOverseas()` 分支（大范围、需逐页 S2、工期大、命中热点并行红线）。
  - (c) **本期不做 mainHeader/components-countdown**，M5 收缩为"仅处理 decorate countdown 三方对照"（见 D2），mainHeader 皮肤整体另立专项。→ **推荐**（最贴合"一次一模块、不碰热点页"）。
- **D2 · decorate countdown 三方对照**：
  - (a) **保留 o2o 现状不动**（o2o 的 b2b 门控/`$t()` 是 o2o 需要的；overseas 的 product-style-border 属外贸风皮肤增强，随皮肤专项迁）。→ **推荐，最安全**。
  - (b) 三方合并：o2o 保留 b2b 门控/`$t()` 同时补 overseas 的 product-style-border（引 useProductStyle，注意 commonCountdown 路径差异）。

## 四、风险
| 级别 | 风险 | 处置 |
|---|---|---|
| 🔴 高 | mainHeader 接入 = 改 8 个热点页，命中"整页顶替/热点并行"红线 | D1 选 (a)/(c)，不在小步里接入 |
| 🟠 中 | decorate countdown 单向取 overseas 丢 o2o b2b/i18n | D2 选 (a) 保留；或 (b) 谨慎合并 |
| 🟡 低 | 迁入"暂未使用"组件属半成品 | D1(a) 时明确登记为皮肤资产预置 |
