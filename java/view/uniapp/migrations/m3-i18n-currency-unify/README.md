# M3 · i18n / currency 统一（uniapp 端）

> 里程碑：P1.5 · 开关：通用（无 `isXxx` 整模块开关，靠 `isOverseas()` 守卫动态路径）
> 红线：迁移≠重构、**开关置0行为不变**、统一后 **o2o/标准站表现零变化**、每 Task 一 commit。
> 状态：**已完成（2026-07-22）**。决策 D1=a / D2=a / D3+D4=a / 标准仓=a（本期只落 o2o）。vort:check + mp-weixin + h5 均通过。

---

## 一、目标

把跨境仓（`tigshop-overseas-uniapp`）已经打磨成熟的 i18n / currency 基础设施收敛进 o2o 基座，消除 o2o/标准版当前的写死与隐患，同时**对国内站（o2o/标准 prod，`VITE_IS_OVERSEAS=0`）保持零行为变化**。

---

## 二、S2 三方实测对照（2026-07-22）

### 2.1 `store/i18n.ts`（o2o 91 / 标准 91 / 跨境 212）
- **o2o 与标准逐字节相同**，跨境是唯一健壮源。
- o2o/标准现状问题：
  1. `setLangCode` 结尾**无条件 `uni.setLocale("zh-Hans")`**（第72行写死），任何语言最终都被强制中文。
  2. 在 store setup 里用 `useI18n()`（第10行）——依赖已 install 的 i18n 插件 + active instance，App/weex 端有 `createInstanceContext failed`/白屏风险。
- 跨境版增强（均为"能力/健壮性"，对国内 langCode=zh 场景**结果一致**）：
  - `toUniLocale()`：业务码→框架 locale（zh-Hant/zh-Hans/en/fr/es），防止系统文案原样输出 + App indexOf 崩溃。
  - `syncFrameworkLocale()` + `__locale_synced` 守卫：Android `uni.setLocale` 会杀进程重启，做幂等防重启循环。
  - 用 `i18n.global` 直接操作，**不在 setup 调 `useI18n()`**（消除白屏隐患）。
  - `getLangData` 返回 bool（App 端是否需 reLaunch）；`clearCache()`。

### 2.2 `locale/index.ts`（o2o 仅 default `{zh,en}` 原始 / 跨境含 `preprocessMessages`+`localMessages`）
- 跨境 `preprocessMessages` 在**构建期**把 `{0}`/`{name}` 占位符改写成 Message Function，绕过小程序/App 沙箱禁用的 `new Function` 运行时编译。
- **含义**：o2o 当前在**小程序/App 端**，凡带 `{0}`/`{name}` 占位符的文案会**原样显示**（如"已售{0}件"）。H5 无此问题。→ 采纳 = 修复小程序/App 占位符（对 H5 零影响）。

### 2.3 `hooks/useCurrency.ts`（o2o 27 / 跨境 44）
- 跨境多 `baseCurrency`/`baseCurrencyName`（钱包/分销类金额恒用后台主货币，不随展示币种切换；币名走 `t()` 翻译）。
- **纯新增返回字段**，原 `currency`/`currencyName` 逻辑逐字节一致 → 对现有消费点零影响。
- 依赖 `@/utils/i18n` 的 `t`（o2o **缺此文件**）。

### 2.4 `utils/i18n.ts`（跨境有 / o2o 无）
- `t`/`setTranslationFunction`/`formatI18nParams`/`tFormat`。纯工具，未接线前零副作用。

### 2.5 `main.ts`
- o2o：`app.use(pinia).use(i18n)...` 无 `setTranslationFunction` 接线、无全局 `{0}` 兜底包裹。
- 跨境：`setTranslationFunction(i18n.global.t)` + 全局 `t` 包一层做 `{0}`/`{name}` 兜底。

### 2.6 `utils/init.ts` ⚠️ 行为分歧最大处
- o2o：**`uni.setLocale("zh-Hans")` 写死** + 加载 config/tabbar/theme/service；**不调 i18nStore.init()、不拉语言/币种列表**。
- 跨境：`useI18nStore().isCache()/init()`（会 `getDefaultLang`+`getLang` 网络请求）+ `getLangListData()` + `fetchCurrencyList()` + wholesale identity（带 `@vort-if common.wholesale`）。
- **矛盾**：直接采纳跨境 init → 国内站也会走动态语言拉取（新增 getDefaultLang/getLang/getLangList 网络调用 + 依赖后端语言接口），**违反"国内零变化"**。

### 2.7 `utils/request.ts`（热点，尽量不碰）
- o2o：`"X-Locale-Code": isOverseas() ? i18nStore.langCode || uni.getLocale() : ""` —— **已按 isOverseas() 正确门控**，国内发 `""`。
- 跨境：`"X-Locale-Code": i18nStore.langCode || ""`（无门控，因为它整站是跨境）。
- **结论**：o2o 版已是"融合正确态"，**request.ts 无需改动**。

---

## 三、决策点（S2 拍板项）

- **D1 · init.ts 动态语言路径**：
  - (a) 用 `isOverseas()` 守卫：国内保持 `uni.setLocale("zh-Hans")` 写死路径不变；仅跨境走 i18nStore 动态 init。→ **国内零变化（推荐）**
  - (b) 全量采纳跨境动态 init（国内也走后端语言接口）→ 违反零变化红线。
- **D2 · locale 占位符修复（preprocessMessages）**：
  - (a) 采纳（修复小程序/App 上 `{0}` 占位符，H5 零影响；属跨端 bug 修复）→ **推荐**
  - (b) 暂不动，仅收敛 store/currency（占位符问题另立单）。
- **D3 · store/i18n.ts 健壮版**：以跨境 212 行替换 o2o/标准 91 行（消除写死 zh-Hans + useI18n 白屏隐患）。国内 langCode=zh 时结果一致。→ 采纳（默认）。
- **D4 · useCurrency baseCurrency + utils/i18n.ts + main.ts 接线**：纯新增能力，零现有影响 → 采纳（默认）。

---

## 四、Task 拆分（依决策，草案）

- T1：新增 `utils/i18n.ts`（copy 跨境）+ `main.ts` 接线 `setTranslationFunction` + 全局 `{0}` 兜底。
- T2：`locale/index.ts` 增 `preprocessMessages`/`localMessages`，default 改为预处理版（依 D2）。
- T3：`store/i18n.ts` 换健壮版（依 D3）；标准仓同源可后续同步。
- T4：`hooks/useCurrency.ts` 增 `baseCurrency`/`baseCurrencyName`（依 D4）。
- T5：`init.ts` 按 D1 加 `isOverseas()` 守卫；保留国内写死路径。
- 验证：`vort:check` + H5/mp-weixin 双端构建 + 国内站冒烟（语言仍中文、金额展示不变）。

### 实施结果（2026-07-22）
- ✅ T1 `src/utils/i18n.ts` 新增 + `main.ts` 接线 `setTranslationFunction` + MP-WEIXIN `{0}` 兜底。
- ✅ T2 `src/locale/index.ts` 增 `preprocessMessages`/`localMessages`，default 改预处理版。
- ✅ T3 `src/store/i18n.ts` 换 overseas 健壮版（toUniLocale + Android 重启守卫 + i18n.global，消除写死 zh-Hans 与 useI18n 白屏隐患）。
- ✅ T4 `src/hooks/useCurrency.ts` 增 `baseCurrency`/`baseCurrencyName`（原字段逐字节不变）。
- ✅ T5 `src/utils/init.ts` 加 `isOverseas()` 守卫：国内保留 `uni.setLocale("zh-Hans")` 写死路径 + 不拉语言/币种；仅跨境走动态 init（先定 langCode 再发请求，保 X-Locale-Code 时序）。保留 o2o 独有 `getStoreEntryRuleData()`。
- ⏹ `request.ts` 未改动（o2o 已按 `isOverseas()` 门控，属融合正确态）。
- ⏹ 标准仓 `store/i18n.ts` 未同步（本期只落 o2o，登记后续）。
  - 【2026-07-23 重分类】此项**移出 o2o 大融合 cleanup 范围**：属对另一产品仓（`tigshop-uniapp`）的独立 bugfix，非"往 o2o 合功能"。标注为**标准仓独立事项，待单独立项**，本融合项目不再跟踪。落地前需先在标准仓核对 `toUniLocale`/`i18n.global`/Android 重启守卫等配套依赖是否齐全，不可照搬 212 行版本。
- 验证：`vort:check` 通过（4 个既有 manifest warn）、`build:mp-weixin` 通过、`build:h5` 通过。circular-dep 警告为既有基线（hooks/index.ts re-export，与本次无关）。

---

## 五、风险

| 级别 | 风险 | 处置 |
|---|---|---|
| 🟠 中 | init 动态路径误伤国内（多网络调用/依赖后端语言接口） | D1 选 (a) `isOverseas()` 守卫，国内路径原样 |
| 🟠 中 | `store/i18n` 换版触发 App 端 locale 行为差异 | 国内 langCode 恒 zh，`toUniLocale→zh-Hans`，结果一致；App 端反而修复白屏隐患 |
| 🟡 低 | preprocessMessages default 改写影响 H5 | H5 无占位符运行时编译问题，preprocess 对无占位符串原样下发 → 零影响 |
| 🟡 低 | 标准仓 store/i18n 未同步（仍写死） | 本期只落 o2o；标准仓同步登记为后续 |
