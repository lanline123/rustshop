# M4 · 跨境 langSet / oauth 迁入（uniapp 端）

> 里程碑：P2 · 开关：`isOverseas()` · 依赖：M3（i18n/currency，已完成）
> 红线：迁移≠重构、新页面进分包、`isOverseas()` 守卫、每 Task 一 commit。
> 状态：**部分完成（2026-07-22）**。决策 D1=a（oauth 阻塞不迁）/ D2=a（langSet + tig-button2 迁入）。langSet 已迁并双端构建通过；oauth/callback 挂起待"跨境登录整包"。

---

## 一、目标（原清单）
迁入跨境独有 `pages/langSet`(305行) + `pages/oauth/callback`(105行) 进 o2o 分包，`isOverseas()` 守卫。

## 二、S2 实测（2026-07-22）

### 2.1 `pages/oauth/callback.vue` ⚠️ 阻塞
- **文件存在**（overseas `git HEAD:src/pages/oauth/callback.vue`，工作树可 `git show` 取到），105 行，OAuth 固定回调页。
- 依赖问题（三重）：
  1. **入口缺失**：callback 的唯一触发来自 `pages/login/src/overseasLogin.vue`（设 `oauthPath` 并发起 Google/Facebook 授权）。**o2o 无 overseasLogin**（o2o 登录=`baseLogin`）。单迁 callback → 无任何入口，不可用。
  2. **API 签名分歧**：
     - o2o `getOauthCallback(path, redirectUri, data)` → `user/oauth/callback/${path}?redirectUri=`
     - 跨境 `getOauthCallback(path, data)` → `user/oauth/callback/${path}`
     - overseas 页按 2 参调用，直接 copy 会与 o2o 3 参 api 不匹配。
  3. **wholesale 依赖**：callback 内 `// @vort-if common.wholesale` 块 `import { useWholesaleStore } from "@/store/wholesale"`。o2o **无 store/wholesale**（属 M8），且 **vite 无 @vort-if 构建插件**（标记仅供切割管线，构建期不裁剪）→ 直接 copy 会 **编译失败（模块找不到）**。
- **结论**：oauth/callback 与"跨境登录整包（overseasLogin + oauth + 三方 API 对齐）"强耦合，**不适合在 M4 内单迁**。

### 2.2 `pages/langSet/index.vue` · 可迁（有前置）
- 305 行，语言+货币设置整页。依赖核对：
  - `store/i18n`（M3 ✅ 新签名 `getLangData(code,{clearLocaleGuard})` 兼容）、`store/currency` ✅、`store/config.setHomeDecorateData` ✅、`utils.currFullPath` ✅、`tig-layout` ✅、`tig-popup` ✅。
  - **`tig-button2` ❌**：o2o 只有 `tig-button`，无 `tig-button2`（跨境独有，带 ripple 动效）。
- 重叠：o2o **已有 `tig-select-lang.vue`**（弹窗版语言+货币切换，能力重叠），langSet 是整页版。
- 入口：overseas 中 langSet **无代码级 navigateTo**（仅 `pages.json` 分包 root + `permission.ts` 白名单），靠 URL / 装修菜单进入。

## 三、决策点

- **D1 · oauth/callback**：
  - (a) **本期不迁，标记阻塞**，待"跨境登录整包"（overseasLogin + oauth + API 对齐 + wholesale 归 M8）统一迁 → **推荐**。
  - (b) 迁 callback 并对齐 o2o 3 参 API + 剥离 wholesale 块，但接受"暂无入口、不可用"。
- **D2 · langSet**：
  - (a) 迁入 + **连带迁 `tig-button2` 组件**（保真度最高）→ 推荐。
  - (b) 迁入但改用 o2o 现有 `tig-button` 适配（少迁一个组件，UI 略有差异）。
  - (c) **不迁 langSet**（o2o 已有 tig-select-lang 覆盖语言/货币切换），M4 整体挂起，能力重叠不重复建设。

## 四、Task 拆分（依 D2 草案）
- T1（D2=a/b）：copy `pages/langSet/index.vue` 进分包；`pages.json` 加 `pages/langSet` root（`// @vort-if overseas` 包裹）；`permission.ts` 白名单加 `/pages/langSet/index`（overseas 守卫）。
- T2（D2=a）：copy `components/tig/tig-button2/tig-button2.vue`（依赖 `store/theme`、`utils.getElementRect`，需核对）。
- T3：`.vort/feature-manifest.json` 登记 `pages/langSet`（+ tig-button2）到 `overseas` includes。
- 验证：`vort:check` + mp-weixin/h5 双端构建。

## 四·末、实施结果（2026-07-22）
- ✅ T2 `src/components/tig/tig-button2/tig-button2.vue` 迁入（overseas 原样，依赖 `store/theme`/`utils.getElementRect` 已核实存在）。
- ✅ T1a `src/pages/langSet/index.vue` 迁入（overseas 原样，依赖 M3 i18n 新签名兼容）。
- ✅ T1b `src/pages.json` 加 `pages/langSet` 分包 root，`// @vort-if overseas` 包裹。
- ✅ T1c `src/utils/permission.ts` 白名单加 `/pages/langSet/index`，`// @vort-if overseas` 包裹。
- ✅ T3 `.vort/feature-manifest.json` overseas.includes 登记 `src/pages/langSet` + `src/components/tig/tig-button2`。
- ⏸ **oauth/callback 未迁**（D1=a），阻塞原因见 §2.1，待跨境登录整包。
- 验证：`vort:check` 通过（4 个既有 warn，无新增空路径）、`build:mp-weixin` 通过、`build:h5` 通过。

### 后续（登记）
- **跨境登录整包**（新模块，建议 M4.5 或并入 M7 前）：迁 `pages/login/src/overseasLogin.vue` + `pages/oauth/callback.vue` + 对齐 `getOauthCallback` API 签名 + wholesale 归 M8。

## 五、风险
| 级别 | 风险 | 处置 |
|---|---|---|
| 🔴 阻塞 | oauth/callback 无入口 + API 分歧 + wholesale 依赖 | D1 选 (a)，并入跨境登录整包 |
| 🟠 中 | langSet 与 tig-select-lang 能力重叠 | D2 决策是否重复建设 |
| 🟡 低 | tig-button2 连带依赖（theme/getElementRect） | 迁前核对 o2o 已具备 |
| 🟡 低 | 新页面进主包超限 | 强制分包 root |
