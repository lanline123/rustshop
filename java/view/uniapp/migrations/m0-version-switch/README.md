# M0 · 开关总闸（版本区分唯一入口）· 任务书

> **阶段**：🟡 代码完成待提交（2026-07-22）——isStore/isO2o + VITE_IS_STORE + drop_console + vort:check 脚本；build:h5+mp 通过、主包 1.44MB
> **泳道**：先行（一切前提）｜**开关/分支**：— ｜**风险**：低（无业务逻辑改动）
> **依赖**：无 ｜**被依赖**：所有后续模块
> **参考**：需求方案 §三、§1.2.1、§3.3；执行章程 §三 DoD；对比清单 §二

---

## 一、范围（IN / OUT）

### IN（本模块要做）
1. **新增开关函数**：`src/utils/index.ts` 加 `isStore()` + `isO2o()`（别名），读 `import.meta.env.VITE_IS_STORE == 1`，与已有 `isPro/isB2B/isOverseas/isDemo` 风格一致。
2. **补 env**：`.env.development` + `.env.production` 补 `VITE_IS_STORE`（o2o 站 =1）；按 §1.2.1 组合矩阵校准 `VITE_IS_B2B`/`VITE_IS_OVERSEAS`（o2o 站二者应为 0，当前 dev 为 1，需 S2 确认是否调整或保留开发便利值）。
3. **修 `vite.config.ts`**：`drop_console: false` → 按生产构建剥离（对齐标准/跨境仓写法，S2 定用 `true` 还是 `command === "build"`）。
4. **收口检查**：全仓禁止业务代码裸读 `import.meta.env.VITE_IS_*`，统一走 `utils/index.ts`；扫描现存裸读点，列清单（本模块只收口 M0 触达点，其余随各模块顺带）。
5. **`@vort-if` 配对校验脚本**：落地轻量脚本（校验 `@vort-if`/`@vort-endif` 配对、嵌套、特性名合法性、manifest 路径存在性），挂 build 前。**单一数据源**指向 `.vort/feature-manifest.json`（规避 vortmall ENOENT 教训）。

### OUT（本模块不做，交由后续模块）
- ❌ 给存量门店代码补 `isStore()` 包裹（随各业务模块顺带做，裁剪前全局扫尾）。
- ❌ 迁移任何业务功能（batch 属其它模块）。
- ❌ 清理 15 文件 `isOverseas` 残留分支（归 M6）。
- ❌ 工具链升级 TS/vue-tsc（独立 commit，见需求方案 §8.6）。

---

## 二、决策记录（已确认 2026-07-22）

1. **env 组合值**：纯 o2o 矩阵——`.env.production` 置 `VITE_IS_B2B=0`/`VITE_IS_OVERSEAS=0`/`VITE_IS_STORE=1`；`.env.development` 保留 B2B/OVERSEAS 便利值（不影响生产），补 `VITE_IS_STORE=1`。
2. **`drop_console` 取值**：`isBuild`（对齐跨境仓，o2o config 已是函数式）。已扫 console 副作用实参：全仓仅 `App.vue:108` 一处参数含函数调用（`sessionStorage.getItem`，纯读取无副作用），剥离安全。
3. **校验脚本形态**：新增 `scripts/vort-if-check.mjs` + `npm run vort:check`；**首版不进 prebuild**（零阻断）；配对/嵌套/特性名报 error，manifest 路径存在性 warn-only（实测当前 manifest 有 4 个空路径：deliveryCard/termPay/modules-live/modules-videoNumber，对应模块迁入时补齐）。

> S2 收口扫描结果：`import.meta.env.VITE_IS_*` 裸读**仅存在于 `utils/index.ts` 总闸本身**，业务代码零裸读，T4 无需改动（现状已合规）。

---

## 三、Task 拆分（S3 草案，确认范围后细化）

| Task | 内容 | 预估 diff | 可独立 revert |
|---|---|---|---|
| T1 | `utils/index.ts` 加 `isStore()/isO2o()` + 三份 env 补 `VITE_IS_STORE` | <50行 | ✓ |
| T2 | `vite.config.ts` 修 `drop_console` | <20行 | ✓ |
| T3 | `@vort-if` 配对校验脚本 + 挂 prebuild + manifest 路径存在性校验 | <200行 | ✓ |
| T4 | 裸读 `VITE_IS_*` 收口扫描 + M0 触达点改走 `isXxx()` | 视扫描结果 | ✓ |

---

## 四、DoD（执行章程 §三）

- [ ] `isStore()/isO2o()` 定义并读 `VITE_IS_STORE`；无裸读散落（M0 触达范围）
- [ ] 三份 env 含 `VITE_IS_STORE`，组合值符合确认结论
- [ ] `drop_console` 生产剥离生效
- [ ] `@vort-if` 校验脚本可跑、配对/特性名/manifest 路径校验通过，挂 build 前
- [ ] `npm run type-check` + `build:h5` + `build:mp-weixin` 通过，小程序主包 <2MB
- [ ] **开关回归**：`VITE_IS_STORE` 未定义/置0 构建，o2o 站行为与改动前完全一致
- [ ] 每 Task 一 commit；完成打 tag `fusion-m0-done`
- [ ] 更新执行章程 §四进度表

---

## 五、边界与风险
- `isStore()=1` 从未真实运行过，本模块只建开关不改门店代码，**回归重点=开关不影响现有无条件门店行为**。
- 校验脚本须与 `.vort/feature-manifest.json` 单一数据源，清单现有 4 个空路径项（deliveryCard/termPay/modules-live/modules-videoNumber）——脚本路径存在性校验 warn-only 提示，由对应模块（M1/M8/直播）迁入时补齐或移除，不阻断 M0。
