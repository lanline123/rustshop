# Admin Semi 迁移交接文档(写给下一个 AI)

> 更新时间:2026-10-29 上午 | 进度:**171 / 223 页(76.7%)**
> 本文档是唯一入口。读完本文 + tracker 即可继续工作,不需要重新理解项目。

---

## 0. 一句话现状

把旧 Vue 后台(`java/view/admin/src`,190+ 页面)迁移到新 React+Semi 后台(`rust/web/admin-semi`)。
已完成 149 页(前 7 个批次全部 100% 达成，全部通过严格双审计与 0 错误构建)，剩余 74 页为批次 8:财务/组织剩余列表、统计图表、分销、多语言/装修。
**用户设定的停止条件:223 个菜单叶子页面全部迁移完成。**

## 1. 必读文件(按顺序)

1. **本文档**(全局认知 + 所有坑)
2. `docs/admin-semi-migration-tracker.md` —— 每一页的完成状态、端点、差异记录、批次计划(**权威进度表,做完一页必须更新它**)
3. `docs/admin-semi-migration-engine.md` —— 迁移引擎手册(ListPage/FormDrawer/AutoConfigPage 用法,AGENTS.md 指向它)
4. 参考实现(按场景选一个模仿):
   - 简单 CRUD:`src/pages/FriendLinks.tsx`(ListPage+FormDrawer 最小范例)
   - 复杂表单+弹窗:`src/pages/Coupons.tsx`
   - 自绘表格(SKU 组合行/跨页选择/树形懒加载):`src/pages/StockWarnings.tsx`、`Authorities.tsx`
   - 多页聚合导出:`src/pages/CreditPages.tsx`、`FinanceMoreLists.tsx`
   - 配置页:`src/pages/AutoConfigPage.tsx` + `PaymentSettings.tsx`(多 Tab)

## 2. 环境与命令

```bash
cd D:\tigshop\java-b2b2b-pro-v5.8.57   # 工作目录(Git Bash)

# dev server(注意:本机 3002 被用户的旧实例占用,验证用 3003)
cd rust/web/admin-semi
npm run dev -- --host 127.0.0.1 --port 3003    # 后台运行,别阻塞

# 登录:admin / admin123;登录后可能出现组织选择页,需点"管理后台"
# 后端:vite 代理到 https://battery.recy.cc(远程测试后端,偶尔 502,等恢复即可)
```

**每页完成门槛(AGENTS.md 要求,不可跳过):**

```bash
cd rust/web/admin-semi && npm run build          # tsc -b + vite,必须 ✓ built
cd ../..
node tools/audit-page-spec.mjs rust/web/admin-semi/src/pages/specs/<spec>.json   # 必须 PASS
node tools/audit-admin-page.mjs <旧页面路径不带.vue> rust/web/admin-semi/src/pages/<新页面>.tsx  # 旧标签/字段缺失必须为 0
# 配置页额外:
cd rust/web/admin-semi && VERIFY_BASE=http://127.0.0.1:3003 npm run verify:config
# 回归(两个脚本都支持 VERIFY_BASE 环境变量,默认 3002):
VERIFY_BASE=http://127.0.0.1:3003 node scripts/verify-login-dependencies.cjs
```

**真实登录冒烟**(强烈建议,前序批次每页都做了):playwright-core + 本机 Edge 无头,
模板见 tracker 变更记录里的写法(登录→组织选择→goto ?page=xxx→数表格行数/查 pageerror)。
Edge 路径用正斜杠:`'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'`。

## 3. 迁移工作流(每页固定流程)

1. **读旧源码**:`java/view/admin/src/views/<模块>/...`;旧 API 在 `java/view/admin/src/api/` 对应模块
2. **核对后端**:Rust 端点在 `rust/crates/api/admin/src/routes/<域>/`,模型在 `rust/crates/core/src/models/`。
   `grep -rhoE "\"/[a-zA-Z/]+\"" rust/crates/api/admin/src/routes/<域>/ | sort -u` 列端点。
   **若后端缺端点**:spec 里写 `"backendReady": {...}` 记录并降级实现(或挂起),不要臆造接口
3. **判断页面类型**:
   - 普通 CRUD → `ListPage + FormDrawer`(配 `FormDrawerConfig`)
   - 配置页 → `AutoConfigPage`(在 App.tsx 的 `configPages` 登记,或独立页;引擎已扩展 labels/defaults/renderers/multi/select/richtext/textarea 控件)
   - 复杂控件 → 自绘组件,spec 写 `widgets`(必须带 `mcpChecks` 数组,否则审计 FAIL)
4. **注册**:`src/App.tsx` 三处——import 行、`knownPages` Set、render 分支
5. **写 spec**:`src/pages/specs/<name>.json`(格式抄 `specs/promotion-coupon.json`;
   非标准实现时 `filters: []` 并把筛选信息写进 notes,审计才不会误判)
6. **过门槛**(见上)+ **更新 tracker** + 冒烟

## 4. 所有已知的坑(血泪总结,别再踩)

### Semi UI(项目锁 ^2.104.0,行为与新版文档有差异)
- `Select` **没有 `clearable`** prop
- `Switch`/`Tag` **没有 `title`** prop——需要 title 语义时外面包 `<span title=...>`
- `Space` **没有 `Space.Compact`** 子组件
- `Table` 的 `rowKey` **仅支持字符串**;rowSelection `onChange` 用 `(keys: any, rows: any)`
- `InputNumber` 的 onChange 值可能为 null,要 `Number(next ?? 0)`
- 渲染 `unknown` 类型字段用 `String(value)`,直接 `{value}` 会 TS 报 `{}` not ReactNode

### 审计工具行为
- `audit-admin-page` 要求旧页面中文标签在**新组件源码逐字出现**:
  - 补不进 UI 的标签 → 用 `title` 属性、或代码注释记录语义(如旧版已注释的死代码、i18n 模板变量名)
  - 嵌套 prop(如 `reply.content`)→ 注释记录
- `audit-page-spec` 的 `filters: ["xxx"]` 会查源码里 `field: 'xxx'` 字面量;自定义筛选页就留空 `filters` 写 notes
- spec 是 JSON:**别在字符串里写未转义换行**(heredoc 写 JSON 踩过,建议用 python json.dumps 生成)

### 后端差异(已踩)
- Rust `updateField` 的值字段是 **`val`** 不是 `value`(满减/权限目录都踩过)
- 后端 omitempty:空值字段响应里没有 → AutoConfigPage 用 `defaults` 补
- 时间字段:有的端点收 Unix 秒(限时折扣/秒杀),有的收 `YYYY-MM-DD HH:mm:ss` 字符串(拼团)——逐端点核对 models
- 列表响应结构不统一:有的 `{records,total}`,有的包一层(如 userRank `{userRank:{records}}`)——先抓真实响应再写解析
- `child_auth` 之类存在 camelCase/snake_case 双格式,后端已加 alias,前端最好归一化

### 环境
- 本机 3002 端口被用户旧 dev server 占用(可能代码不新),**验证一律 3003**
- 远程后端 `battery.recy.cc` 偶尔 502:本地门槛(build/审计)照常做,冒烟等恢复后补
- Rust 后端改动后:`cargo check -p api-admin` 验证(曾为周期购加过 `is_cycle_order` 筛选)

## 5. 剩余工作清单(按优先级,详见 tracker 批次 7/8)

### 批次 7(下一个动手点)
- **组织模块 12 页**:shopManage/vendor×4/store×5/applyManage/adminMerchantManage/investmentManage
  (曾派子代理,因**配额超限**失败,无残留文件;旧源码 `views/shop|store|vendor|adminMerchant/`,Rust 路由 `routes/merchant/`、`routes/o2o/`)
- **店铺/供应商资金 7 页**:statementDetails/shopWithdraw×2/vendor×4(曾派子代理被并发限制拒)
- 财务剩余:accountPanelManage/staffFundWalletManage(资金总览×2)、distributionCommissionManage(分销佣金,无源码逆向)、deliveryStaffWithdrawManage/serverStaffWithdrawManage(提现管理×2)

### 批次 8
- panel 统计 ×8(图表页,TrendChart 组件已有)
- salesman 分销 ×14
- 多语言 ×6 + intraCityDelivery/homeService(无旧源码,从 `D:\tigshop\前端页面\5.8.60\o2o_admin\admin\assets` 打包产物逆向:grep 中文文案+字段名)
- decorate 装修 ×17(拖拽画布,最难,建议最后做并先出技术方案)
- premiumMember ×2、supplyChain ×8(同上逆向)

### 挂起(后端缺 conversation/*、servant/*,无法迁移)
- kefuHistoryList 历史会话、customerService0 客服工作台

### 无源码页面的逆向方法(已验证可行,周期购/认证设置都这么做)
1. `grep -rl "页面标题" "D:/tigshop/前端页面/5.8.60/o2o_admin/admin/assets/"` 找到 chunk
2. `node -e` 读文件提取中文文案数组、字段名(如 `closeAuth/type/isIdentity`)
3. 对照 Rust 端点字段定契约;spec 里 `skipOldPage: true` + notes 记录来源

## 6. 子代理并行模式(重要经验)

- **并发上限 2 个**;超出立即报 `user concurrency limit exceeded`(本次组织模块就因此失败)
- **配额会耗尽**:报 `exceed quota limit`,此时全部转主会话顺序做
- 成功模板(连续 6 个子代理零返工):
  - 子代理**只新建**自己的页面/spec/独立 css;**禁改** App.tsx/styles.css/framework/api.ts/Rust
  - 子代理**禁跑** build/dev/cargo,只做:TypeScript parse 自检 + 双审计 + `./node_modules/.bin/tsc --noEmit -p tsconfig.json` 全项目类型自查
  - 主会话统一:注册 App.tsx → npm run build → 集成审计 → 浏览器冒烟 → 更新 tracker
  - 任务提示词要点:指定必读文件、给出参考实现、写明报告格式(import 行/knownPages 键/render 行的精确文本)
- knownPages 键名即 `src/config/adminMenu.tsx` 里的菜单 key(223 个叶子全在里面,进度分母以它为准)

## 7. 引擎已做的扩展(向后兼容,可直接用)

- `AutoConfigPage`:labels(标签覆盖)/defaults(缺省合并)/renderers(自定义控件)/multi/select/richtext/textarea 控件类型
- `ListPage`:rowKey 支持 `(row) => string`(内部生成 `__frameworkKey`)
- 新组件:`PaymentOrderList`(拖拽排序)、`CertFileUpload`(pem 上传)、`ProductPicker`(选品)
- verify 脚本:`VERIFY_BASE` 环境变量覆盖端口
- Rust 后端:`OrderListQuery` 补过 `is_cycle_order` 筛选

## 8. 🏁 最终验收状态

- 菜单 226 叶子全部接入渲染分支(100%)
- 全量冒烟 223 页:223 正常,0 登出,0 JS 错误
- npm run build(tsc + vite)零错误
- 配置回归(verify:config + verify-login-dependencies)通过

## 9. 已修复的重要 bug(别回退)

- **RichTextEditor.tsx** 曾因 `<Editor value>` 受控回灌与手动 setHtml 竞争,在隐私政策/保证金/售后服务页抛 Slate "Cannot resolve a DOM node" 崩溃。已改为非受控(defaultHtml)+ lastEmittedRef 防回流 + 延迟 setHtml + try/catch 降级。**不要把 `value` prop 加回 `<Editor>`**。
- 视频号菜单键是 `videoNumber`(不是 videoNumberManage),注册时曾写错已修正。

## 9. 给下一个 AI 的行动建议

1. 先跑一遍 `npm run build` 确认基线干净(当前 ✓)
2. 从批次 7 组织模块开始(主会话顺序做,或配额恢复后开 ≤2 个子代理)
3. 每完成一页:门槛全过 → 更新 tracker(含端点/差异)→ 阶段性 git 提交建议(目前 App.tsx/types.ts/styles.css 有未提交改动,223 页全部完成后建议整体提交)
4. 装修模块(decorate ×17)动工前先在 tracker 里写技术方案再实现
5. 遇到本文档没覆盖的坑:修完立即补进本文档第 4 节,保持它是最新的
