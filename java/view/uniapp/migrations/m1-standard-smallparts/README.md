# M1 · 标准版通用小件补齐 · 任务书

> **阶段**：✅ 已完成（2026-07-22，代码待提交）——净改动=仅删 `appManage.ts`；backLock/cartJump 结案"保留/无需改动"；useProductCardBuy 移入 M8
> **泳道**：A（o2o 站体验线）｜**开关/分支**：默认分支
> **依赖**：M0 完成 ｜**风险**：中（cartJump 去留涉及购物车入口）
> **参考**：需求方案 §5.2(a)、§5.4；执行章程 §二 M1；对比清单 §一/§五

---

## 〇、S1 立项已订正的关键前提（实测 2026-07-22）

| 纲领 §5.2(a) 说法 | 实测 | 处置 |
|---|---|---|
| `useProductCardBuy` = "商品卡直购，通用默认分支，o2o 缺" | **实为 wholesale 耦合**：内部 `@vort-if common.wholesale` + 判 `firstWholesalePrice` + 走"批发 SKU 半窗"；且**标准版当前 0 消费点**（grep `isWholesaleCard`/`cardProductId` 无引用），是标准版自身的未接线预埋 | **移出 M1、移交 M8（B2B）**——它不是通用能力，且无消费点，M1 迁入会变 o2o 死代码。见决策 D3 |

**其余两项实测**：
- **appManage/appVersion**：o2o `appVersion.ts`（`useAppVersionStore`）为**在用**（`components/appUpdatePop/index.vue`、`utils/checkAppUpdate.ts` 消费；接口 `appVersion/getAppUpdate`）；`appManage.ts` **无任何消费点 = 死代码**。
- **cartJump**：o2o 仅 **1 处跳转点** `pages/product/index.vue:256`（`toPage(userStore.token ? '/pages/cartJump/index' : '/pages/cart/index')`）+ `pages.json` 分包 root；跨境仓已无 cartJump（直接用 cart）。

---

## 一、范围（IN / OUT）

### IN
- **① appManage 死代码清理**：删除 o2o 未使用的 `store/appManage.ts`（保留在用的 `appVersion.ts`）。做法见决策 D1。
- **② `levelDesc.vue` 三方对照取正**：`pages/user/levelCenter/src/levelDesc.vue`（三仓 217/199/197 行漂移），S2 三方 diff 判定漂移性质（业务差异/单边修复/噪音），取"最新正确"版本对齐到 o2o。
- **③ cartJump 去留评估 + 执行**：按决策 D2 结论，保留或删除（删除则改跳转点 + 去分包 + 全仓再确认无其它引用）。

### OUT
- ❌ `useProductCardBuy`（移交 M8，见 D3）。
- ❌ 任何 wholesale/B2B 代码（M8）。
- ❌ 标准版 `appPermission` 组件与权限弹窗（standard 独有，o2o 现无此需求；如需另立项）。
- ❌ 版本开关（M1 全默认分支）。

---

## 二、决策记录（已确认 2026-07-22）

- **D1 = 删除死代码**：直接删 `store/appManage.ts`。已核实全仓无 import（仅文件自身定义 + 文档引用），`store/index.ts` 仅 `createPinia()`，Pinia 按需注册 → 删除对 o2o 运行时/其它版本/构建均零影响。
- **D2 = 先 S2 比对、默认保留**：cartJump 是 cart 的近重复体（非版本特性，不可用开关切割）。先出 `cart` vs `cartJump` 差异清单；有 o2o 独有逻辑则保留，纯重复且回归通过才删。**在证据充分前默认保留、绝不盲删**。
- **D3 = 移交 M8**：`useProductCardBuy`（wholesale 耦合 + 零消费点）移出 M1，随 M8 消费点一起迁。

## 二·附、原待确认决策（存档）

**D1 · appManage 处理方式**：
| 选项 | 做法 | 优点 | 代价 |
|---|---|---|---|
| **删除死代码（推荐）** | 直接删 `store/appManage.ts` | 最小、消除 o2o 死代码 | 与母仓命名不一致（母仓用 appManage）；未来若从母仓迁"权限弹窗"需再对齐 |
| 重命名对齐母仓 | `appVersion.ts`→`appManage.ts` + 改 3 处消费点 + 删旧 | 命名与母仓统一，利于后续 merge | 改动面变大、纯改名收益有限 |

**D2 · cartJump 去留**：
| 选项 | 做法 | 风险 |
|---|---|---|
| **删除对齐跨境（推荐待验证）** | 跳转点改 `/pages/cart/index`、去 pages.json 分包、删 `pages/cartJump/` | 需确认 `cart/index` 已覆盖"已登录"场景全部逻辑（cartJump 与 cart 85% 重复，剩 15% 需比对） |
| 保留 | 不动 | 仓内长期存在近重复体，主包/维护成本 |

> D2 的删除结论**必须 S2 先做 cart vs cartJump 差异比对**确认 15% 差异无独有逻辑，再执行；本 S1 先定方向。

**D3 · useProductCardBuy 移交 M8**：确认将其从 M1 移出、并入 M8（B2B 批发）随消费点一起迁移？（推荐：是）

---

## 二·末、S2 执行结论（2026-07-22 实测）

**① appManage**：已删除 `store/appManage.ts`（死代码，零引用）。✅ 唯一实际代码改动。

**② levelDesc 三方对照结论 = o2o 无需改动**：
- overseas 独有：`消费满` 后加 `元`（`t("元")`）+ 首表 `line1` class + CSS 用 `line-height` 居中；**其中"元"在多币种场景反而可疑，不回迁**。
- standard 独有：仅 JSDoc 注释（纯注释，217 行来源）。
- o2o 与 standard 功能一致，**无缺失的单边 bug 修复**。按迁移≠重构/禁止格式化：**本项不改**。

**③ cartJump 去留结论 = 保留（不删）**：
- `git diff` 实测：cart vs cartJump 差 466 行。**两者共享同一套门店/自提/同城/deliveryType 逻辑**（`getDeliveryOptions`、pickup/local delivery、`/order/check?deliveryType=`），cartJump **无 o2o 独有逻辑丢失风险**。
- cartJump 的真正作用：**可返回的非 tab 购物车页**（`tig-navbar :show-left="true"`）——tabbar 的 `cart` 无法被 `navigateTo`，商品页登录态点购物车需要一个可 push、带返回键的副本。**删除会破坏该 UX**。
- 但发现**双向漂移/隐患**：cartJump 已落后 cart（缺 product-style 边框、`formatActivityList` 的 usedPromotions 2 参、`cartManage` 同步守卫），却又自带一个 cart 没有的 `isOverseas` extraSkuData 分支 → **登录态用户当前用的是更旧的 cartJump**。
- **处置**：本期**保留 cartJump**（honors "不破坏功能"）。消除重复应作为独立重构任务（把 cartJump 改造成复用 cart 逻辑的薄壳），**超出 M1 小件范围**，登记为后续候选。

**M1 净代码改动 = 仅删除 `appManage.ts`**；②③ 均为 S2 结案"无需改动/保留"，符合"迁移≠重构、不破坏各版本功能"。

## 三、Task 拆分（S3 草案，依赖决策）

| Task | 内容 | 预估 diff | 前置 |
|---|---|---|---|
| T1 | ① 删 `store/appManage.ts`（D1 选删除时） | <10行 | 确认无消费点（已核实） |
| T2 | ② `levelDesc.vue` 三方对照后对齐 o2o | 视 diff | S2 三方 diff |
| T3 | ③ cartJump 删除（D2 选删除且 S2 比对通过时）：改跳转点 + 去分包 + 删目录 | 中（删文件为主） | S2 cart/cartJump 比对 |

> 每 Task 独立 commit；触达文件顺手修 `Number(options.id)` 雪花 id（如 `product/index.vue` 有多处，仅本模块触达处）。

---

## 四、DoD（执行章程 §三）

- [ ] ① `appManage.ts` 删除后全仓无引用报错；App 更新弹窗链路（appVersion）正常
- [ ] ② `levelDesc` 会员等级说明展示正确（三仓取正版），无回归
- [ ] ③（若删 cartJump）已登录点购物车入口正常到达 cart，功能与删前一致；主包体积记录
- [ ] `vort:check` 通过；`build:h5` + `build:mp-weixin` 通过，主包 <2MB
- [ ] `type-check` 不新增错误
- [ ] 开关回归：M1 全默认分支，o2o 站行为=预期；标准/跨境站不受影响
- [ ] 触达文件 `Number(options.id)` 雪花 id 顺手改 string
- [ ] 每 Task 一 commit；完成打 tag `fusion-m1-done`；更新章程 §四/§五

---

## 五、边界与风险
- **cartJump 删除是最大风险点**：它是"已登录用户点购物车"的实际入口，删前必须 S2 逐行比对 cart vs cartJump 的 15% 差异，确认无自提/门店/次卡相关独有逻辑（o2o 特性可能藏在 cartJump 里），否则退回"保留"。
- **levelDesc 三方漂移**：可能混着单边 bug 修复，取正时按"最新正确修复"为准（不一定是 o2o 版本），并记录判定依据。
- **appManage 若选重命名**：属跨文件改名，注意 `defineStore("appVersion", ...)` 的 store id 字符串是否也要改（改则影响持久化 key，需评估）。
