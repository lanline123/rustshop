# M0.5 · 单边 bug 双向补齐（o2o 站自身缺陷）· 任务书

> **阶段**：S1 立项（范围待确认）
> **泳道**：A（o2o 站体验线）｜**开关/分支**：默认分支（全部是 o2o 站自身 bug，不打版本开关）
> **依赖**：M0（开关总闸）已完成 ｜**风险**：中（③ 涉及原生定位，需真机回归）
> **参考**：需求方案 §5.0(M0.5)、§8.2、§8.3；执行章程 §二 M0.5、§三 DoD；对比清单 §五

---

## 〇、S1 立项已订正的关键前提（实测 2026-07-22，与纲领不同）

纲领 §5.0 对 M0.5 的描述有两处与当前代码不符，立项时已核实订正：

| 纲领说法 | 实测 | 影响本模块范围 |
|---|---|---|
| IM XSS：o2o `pages/im/src/emojiHtml.vue` **直渲染 HTML** 未转义 | **不准**：o2o `emojiHtml.vue` 已用 `<rich-text :nodes="nodes">` + **node 数组**（`{type:"text",text}`）渲染，用户文本不当 HTML 解析，**本身已 XSS 安全**。真正的 XSS 漏洞在 **`pages/im/list.vue:26`**：`v-html="item.lastMessage[0].content.content"` 直渲染会话内容 | ①的靶子改为 `list.vue` 的 `v-html`，**不动已安全的 emojiHtml** |
| M0.5 需"合并 o2o 两套定位实现" | 属实但需界定深度：`utils/location.ts`（含 H5 gcoord WGS84→GCJ02 转换）与 `hooks/useServiceLocation.ts`（`getCurrentLocation` **无坐标转换** → H5 距离偏差数百米）确实并存 | ③拆成"iOS 挂死修复"+"坐标转换补齐"，是否彻底合并见待确认决策 |

**三仓 IM 会话预览渲染对照**（决定①的修法）：

| 仓库 | `list.vue` 消息预览写法 | 安全 | 表情图片 |
|---|---|---|---|
| 标准(母仓) | `<text>{{ getLastMessagePreview(item) }}</text>` 纯文本插值 | ✅ | ✗（显示 `[微笑]` 文本） |
| 跨境 | `<rich-text :nodes="renderTextWithEmojiHtml(...)">`（helper 内 escapeHtml + 表情） | ✅ | ✓ |
| **o2o** | `<view v-html="...content.content">` | ❌ **XSS** | ✗ |

---

## 一、范围（IN / OUT）

### IN
- **① IM XSS 修复**：消除 `pages/im/list.vue:26` 的 `v-html` 用户内容直渲染。修法见待确认决策 D1。
- **② `tig-navbar` 返回键防抖**：给 o2o `components/tig/tig-navbar/tig-navbar.vue` 的 `handleIcon`（现 L65-81 无防护）加 `backLock` 400ms 防抖，防连点退两层（对齐母仓 L64-74 实现）。
- **③ iOS 定位挂死修复**：`utils/location.ts` 的 `checkLocationPermission`（L66-83）`#ifdef APP-PLUS` 只有 `plus.android.requestPermissions`；iOS 下 `plus.android` 为 undefined → Promise 永不 resolve → 定位链路挂死。补 iOS 分支（iOS 权限由系统在 `getLocation` 时弹窗，分支内 `resolve(true)` 放行）。
- **④ 定位坐标转换补齐**：`hooks/useServiceLocation.ts` 的 `getCurrentLocation`（L67-84）H5 端缺 gcoord WGS84→GCJ02 转换（`utils/location.ts` L39-44 已有），补齐消除 H5 距离偏差；深度见 D2。

### OUT
- ❌ 改动已安全的 `emojiHtml.vue`（node 数组方案保留）。
- ❌ 全站 `v-html` 大排查（本模块只收口 IM；其余 v-html 另立审计，见风险）。
- ❌ 任何版本开关（M0.5 全默认分支）。
- ❌ 跨境仓的 backLock/登录回跳（由其自身维护线处理，见纲领 §5.0）。

---

## 二、待确认决策（S1 需拍板）

**D1 · IM XSS 修法**（三选一）：
| 选项 | 做法 | 优点 | 代价 |
|---|---|---|---|
| **A 对齐母仓** | `list.vue` 改 `<text>{{ getLastMessagePreview(item) }}</text>` 纯文本 + 迁 `getLastMessagePreview` | 最安全、零新增、与母仓一致 | 会话列表预览表情显示为 `[微笑]` 文本 |
| **B 对齐跨境** | 迁 `utils/escapeHtml.ts` + `im/src/renderTextWithEmoji.ts`，`list.vue` 用 rich-text | 保留表情图片、并落地 escapeHtml 通用防御 | 引入跨境 helper（迁移量略大） |
| **C 复用 o2o 自有组件（推荐）** | `list.vue` 用现有 `<emojiHtml :content="..."/>`（node 数组，已安全） | 复用 o2o 已有安全组件、保留表情、零新增依赖、最贴合"迁移≠重构" | 需确认 emojiHtml 在 ellipsis 单行容器截断表现 OK |

> 推荐 **C**；若你更希望"顺带落地 escapeHtml.ts 通用防御工具"则选 B。

**D2 · 定位合并深度**：
- **最小（推荐）**：只做③iOS 修复 + ④给 `useServiceLocation.getCurrentLocation` 补 H5 gcoord 转换（与 `utils/location.ts` 一致），两套实现保留各自入口，仅消除行为差异。
- **彻底合并**：把 `useServiceLocation` 的取位逻辑收口到 `utils/location.ts` 的单一函数。**属重构、风险高**，建议不在 M0.5 做。

---

## 三、Task 拆分（S3 草案）

| Task | 内容 | 预估 diff | 平台回归 |
|---|---|---|---|
| T1 | ② tig-navbar backLock 防抖 | <30行 | H5 + MP 点击返回 |
| T2 | ① IM XSS（按 D1 选定方案） | <60行(C) / <90行(B) | H5 + MP IM 列表 |
| T3 | ③ iOS 定位分支 + ④ H5 坐标转换补齐 | <60行 | **iOS App 真机** + H5 + MP |

> 每 Task 独立 commit；触达文件顺手修 `Number(options.id)` 雪花 id（如 `im/index.vue` 有 6 处）——**仅本模块触达文件内**，不外扩。

---

## 四、DoD（执行章程 §三）

- [ ] ① IM 列表不再有用户内容 `v-html` 直渲染；构造 `<script>`/`<img onerror>` 文本消息不执行
- [ ] ② 快速连点返回键只退一层（H5 + MP）
- [ ] ③ iOS App 真机：定位授权弹窗正常、`getCurrentRegionInfo` 不再挂死
- [ ] ④ H5 端定位距离与 MP/App 一致（坐标转换生效）
- [ ] `npm run vort:check` 通过；`build:h5` + `build:mp-weixin` 通过，主包 <2MB
- [ ] `type-check` 不新增错误（基线 2 个幽灵 .vue.js 已知，见章程 §六）
- [ ] 开关回归：M0.5 全默认分支，o2o 站行为=修复后预期；不影响标准/跨境站
- [ ] 触达文件 `Number(options.id)` 雪花 id 隐患顺手改 string
- [ ] 每 Task 一 commit；完成打 tag `fusion-m0_5-done`；更新章程 §四/§五

---

## 五、边界与风险
- **iOS 定位需真机**：模拟器无法完整验证权限弹窗；③是 App 端挂死修复，必须真机过一遍（对比清单风险表）。
- **坐标转换回归**：④改动会影响门店距离/选店/同城配送范围判断（`check.vue` isBeyondDelivery、storeList 距离）——H5 端需对比修复前后距离，防"修一处偏差、引另一处"。
- **v-html 面更大**：本模块只收口 IM；建议 S2 顺带全仓 grep `v-html` 输出清单，超出 IM 的沉淀为后续审计任务，不在 M0.5 扩范围。
- **backLock 与 customBack**：o2o handleIcon 有 `customBack` 分支（emit back），防抖需同时覆盖 customBack 与默认两条路径，避免只锁一条。
