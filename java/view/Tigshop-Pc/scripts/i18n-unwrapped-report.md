# 跨境版未包裹 i18n 的中文文案扫描报告

生成命令：`node scripts/check-i18n-wrap.mjs`

## 总览

- 扫描文件：677（整文件忽略 2）
- 已正确包裹的中文 `$t/t` 调用：4653
- 经 `i18n-ignore` 显式豁免：11
- 可疑未包裹条目：377

| 档位 | 高 | 中 | 低 | 合计 |
| --- | --- | --- | --- | --- |
| A. 跨境版主链路（必须处理） | 53 | 0 | 85 | 138 |
| B. 安装向导 pages/install（部署工具，通常不随跨境版对外） | 44 | 0 | 0 | 44 |
| C. 随多商户 / B2B 特性（跨境版含该特性时需处理） | 4 | 0 | 1 | 5 |
| D. v1 归档文件（未启用，切回时才需处理） | 97 | 0 | 25 | 122 |
| E. 跨境版已裁剪（可忽略） | 48 | 0 | 20 | 68 |

判定口径：

- 本项目以中文原文作为 i18n key，故“源码出现中文且未进入 `$t()/t()`”即视为漏翻。
- 已排除：`<style>` 块、注释、语言包、`types/`、`assets/`、`docs/`、`migrations/`、`server/`。
- 已排除“子组件内部再 `$t(prop)`”的静态属性（如 `CommonHeader title` / `TopJumpBar tltle`），运行时可翻译。
- 严重度：高＝界面直接展示；中＝需人工确认是否展示；低＝日志 / 开发期错误 / 逻辑比较值。
- 中文确需保留时（提交给后端的字段值、按语言分支的字面量等），在该行加 `i18n-ignore` 或上一行加 `i18n-ignore-next-line`，并写明原因。

整文件忽略清单：

- `pages/chat/src/emojiElements.ts`：表情 [xx] 标记的协议 token，前后端与历史消息共用，不可翻译
- `pages/chat/src/emojiElements.v1.ts`：同上（v1 归档）

## A. 跨境版主链路（必须处理）

### components/navigation/src/Navigation.vue （11 条，高 11）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 35 | 高 | script | 脚本字符串 | 家电 | `catname: "家电",` |
| 39 | 高 | script | 配置项文案 | 数码 | `name: "3C数码",` |
| 40 | 高 | script | 脚本字符串 | 数码 / 空调 / 冰箱 / 厨卫大电 | `arr: ["3C数码", "空调", "冰箱", "厨卫大电"]` |
| 43 | 高 | script | 配置项文案 | 手机 | `name: "手机",` |
| 44 | 高 | script | 脚本字符串 | 手机 / 平板电脑 / 笔记本 / 智能手表 / 智能手环 / 电子辞典 | `arr: ["手机", "平板电脑", "笔记本", "智能手表", "智能手环", "电子辞典"]` |
| 49 | 高 | script | 配置项文案 | 价格 | `name: "价格",` |
| 53 | 高 | script | 配置项文案 | 品牌 | `name: "品牌",` |
| 54 | 高 | script | 脚本字符串 | 苹果 / 华为 | `arr: ["苹果", "华为"]` |
| 57 | 高 | script | 配置项文案 | 型号 | `name: "CPU型号",` |
| 58 | 高 | script | 脚本字符串 | 晓龙8系列 / 骁龙782 | `arr: ["晓龙8系列", "骁龙782"]` |
| 61 | 高 | script | 脚本字符串 | 暗紫色 支持移动联通电信5G 双卡双待手机 | `productName: "Apple iPhone 14 Pro Max (A2896) 256GB 暗紫色 支持移动联通电信5G 双卡双待手机"` |

### pages/order/check.vue （15 条，高 7）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 1103 | 高 | script | 脚本字符串 | 上门服务规则 | `if (isReserveHome.value) return "上门服务规则";` |
| 1104 | 高 | script | 脚本字符串 | 配送规则 | `if (deliveryType.value === 3) return "配送规则";` |
| 1105 | 高 | script | 脚本字符串 | 运费规则 | `return "运费规则";` |
| 1107 | 高 | script | 脚本字符串 | 上门服务规则 / 配送规则 | `const shippingRuleStoreTitleSuffix = computed(() => (isReserveHome.value ? "上门服务规则" : "配送规则"));` |
| 1108 | 高 | script | 脚本字符串 | 根据商家设置服务距离进行收费 / 根据商家设置配送距离进行收费 | `const shippingRuleDesc = computed(() => (isReserveHome.value ? "根据商家设置服务距离进行收费" : "根据商家设置配送距离进行收费"));` |
| 1109 | 高 | script | 脚本字符串 | 基础上门服务费 / 基础配送费 | `const shippingRuleBaseFeeText = computed(() => (isReserveHome.value ? "基础上门服务费" : "基础配送费"));` |
| 1110 | 高 | script | 脚本字符串 | 上门服务费增加 / 配送费增加 | `const shippingRuleAdditionalFeeText = computed(() => (isReserveHome.value ? "上门服务费增加" : "配送费增加"));` |
| 912 | 低 | script | 日志输出 | 获取地址列表失败 | `console.error("[order/check] 获取地址列表失败", e);` |
| 930 | 低 | script | 日志输出 | 设置默认地址失败 | `console.error("[order/check] 设置默认地址失败", e);` |
| 945 | 低 | script | 日志输出 | 获取支付方式失败 | `console.error("[order/check] 获取支付方式失败", e);` |
| 1148 | 低 | script | 日志输出 | 获取联系人列表失败 | `console.error("[order/check] 获取联系人列表失败", e);` |
| 1162 | 低 | script | 日志输出 | 获取自提时间模板失败 | `console.error("[order/check] 获取自提时间模板失败", e);` |
| 1493 | 低 | script | 日志输出 | 结算页初始化失败 | `console.error("[order/check] 结算页初始化失败", error);` |
| 1607 | 低 | script | 日志输出 | 切换收货地址失败 | `console.error("[order/check] 切换收货地址失败", error);` |
| 1708 | 低 | script | 日志输出 | 更新优惠券失败 | `console.error("[order/check] 更新优惠券失败", error);` |

### components/article/Newslist.vue （7 条，高 7）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 6 | 高 | template | 静态属性 alt="…" | 苹果推出储蓄服务年化收益率超4 / 每笔消费最高返现3 | `<a href="/news/detail" alt="苹果推出储蓄服务年化收益率超4％ 每笔消费最高返现3％">` |
| 11 | 高 | template | 静态属性 title="…" | 苹果推出储蓄服务年化收益率超4 / 每笔消费最高返现3 | `<a class="pd-title" title="苹果推出储蓄服务年化收益率超4％ 每笔消费最高返现3％" href="/news/detail">` |
| 12 | 高 | template | 模板文本 | 公告 / 苹果推出储蓄服务年化收益率超4 / 每笔消费最高返现3 | `<span class="yin_agn">公告</span>苹果推出储蓄服务年化收益率超4％ 每笔消费最高返现3％` |
| 17 | 高 | template | 模板文本 | 苹果公司周一宣布通过高盛银行发行Apple | `苹果公司周一宣布通过高盛银行发行Apple` |
| 18 | 高 | template | 模板文本 | 储蓄卡，年化收益率为4.15%。苹果在官方网站上称，这一利率水平达到美国全国平均利率水平的10倍。 | `Card储蓄卡，年化收益率为4.15%。苹果在官方网站上称，这一利率水平达到美国全国平均利率水平的10倍。` |
| 23 | 高 | template | 模板文本 | 发布时间：2023-06-26 13 | `<span class="time">发布时间：2023-06-26 13:47:20</span>` |
| 25 | 高 | template | 模板文本 | 相关链接 | `<a class="redBtn" href="/news/detail" target="_blank">相关链接</a>` |

### components/member/order/MemberOrderList.vue （6 条，高 4）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 342 | 高 | script | 配置项文案 | 全部订单 | `{ value: 0, label: "全部订单" },` |
| 343 | 高 | script | 配置项文案 | 最近三个月订单 | `{ value: 1, label: "最近三个月订单" },` |
| 344 | 高 | script | 配置项文案 | 最近六个月订单 | `{ value: 2, label: "最近六个月订单" },` |
| 345 | 高 | script | 配置项文案 | 最近一年内订单 | `{ value: 3, label: "最近一年内订单" }` |
| 180 | 低 | template | 逻辑比较值 | 已过期 | `item.cycleCardInfo?.statusName !== '已过期' &&` |
| 192 | 低 | template | 逻辑比较值 | 已确认，待发货 | `<div v-else-if="item.orderStatusName === '已确认，待发货' && isGiftGardOrder(item)">-</div>` |

### utils/map/loader.ts （5 条，高 4）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 41 | 高 | script | 脚本字符串 | 天地图 SDK 加载超时，请检查网络或 tk 是否有效 | `reject(new Error("天地图 SDK 加载超时，请检查网络或 tk 是否有效"));` |
| 58 | 高 | script | 脚本字符串 | 天地图 SDK 仅能在客户端加载 | `return Promise.reject(new Error("天地图 SDK 仅能在客户端加载"));` |
| 64 | 高 | script | 脚本字符串 | 未配置天地图 JS tk，请先在后台地图设置中填写 | `return Promise.reject(new Error("未配置天地图 JS tk，请先在后台地图设置中填写"));` |
| 76 | 高 | script | 脚本字符串 | 天地图 SDK 加载失败，请检查网络连接 | `reject(new Error("天地图 SDK 加载失败，请检查网络连接"));` |
| 21 | 低 | script | 开发期错误 | 未配置高德地图 JS Key，请先在后台地图设置中填写 | `throw new Error("未配置高德地图 JS Key，请先在后台地图设置中填写");` |

### components/member/joining/JoiningOrderList.vue （4 条，高 4）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 212 | 高 | script | 配置项文案 | 全部订单 | `{ value: 0, label: "全部订单" },` |
| 213 | 高 | script | 配置项文案 | 最近三个月订单 | `{ value: 1, label: "最近三个月订单" },` |
| 214 | 高 | script | 配置项文案 | 最近六个月订单 | `{ value: 2, label: "最近六个月订单" },` |
| 215 | 高 | script | 配置项文案 | 最近一年内订单 | `{ value: 3, label: "最近一年内订单" }` |

### utils/map/navigation.ts （4 条，高 2）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 21 | 高 | script | 脚本字符串 | 目的地 | `const name = destination.name \|\| "目的地";` |
| 92 | 高 | script | 脚本字符串 | 目的地 | `const name = encodeURIComponent(destination.name \|\| "目的地");` |
| 79 | 低 | script | 开发期错误 | 天地图位置链接生成失败 | `throw new Error("天地图位置链接生成失败");` |
| 85 | 低 | script | 开发期错误 | 天地图位置链接生成失败 | `throw new Error(result.message \|\| "天地图位置链接生成失败");` |

### pages/activity/pcGroupon/index.vue （2 条，高 2）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 529 | 高 | script | 脚本字符串 | 距离结束仅剩 / 距结束 | `return props.module.style == 1 ? "距离结束仅剩" : "距结束";` |
| 531 | 高 | script | 脚本字符串 | 距离开始仅剩 / 距开始 | `return props.module.style == 1 ? "距离开始仅剩" : "距开始";` |

### pages/member/browsingHistory/List.vue （2 条，高 2）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 110 | 高 | script | 配置项文案 | 收藏的商品 | `{ value: "收藏的商品", url: "/member/collectProduct/list", size: 0 },` |
| 111 | 高 | script | 配置项文案 | 收藏的店铺 | `{ value: "收藏的店铺", url: "/member/collectShop/list", size: 0 }` |

### pages/member/collectProduct/List.vue （2 条，高 2）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 106 | 高 | script | 配置项文案 | 收藏的商品 | `{ value: "收藏的商品", url: "/member/collectProduct/list", size: 0 },` |
| 107 | 高 | script | 配置项文案 | 收藏的店铺 | `{ value: "收藏的店铺", url: "/member/collectShop/list", size: 0 }` |

### pages/member/point/List.vue （2 条，高 2）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 5 | 高 | template | 模板文本 | 明细 | `<CommonHeader :title="'${commonStore.integralName}明细'"></CommonHeader>` |
| 6 | 高 | template | 模板文本 | 明细 | `<TopJumpBar :tltle="'${commonStore.integralName}明细'"></TopJumpBar>` |

### pages/order/src/InvoiceInfo.vue （2 条，高 2）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 168 | 高 | template | 模板文本 | 甘肃 甘南 舟曲县 | `<div class="txt">甘肃 甘南 舟曲县</div>` |
| 260 | 高 | script | 脚本字符串 | 纳税人识别号错误，请检查 | `return "纳税人识别号错误，请检查";` |

### pages/joining/groupByInvited.vue （2 条，高 1）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 217 | 高 | script | 脚本字符串 | 待成团 / 拼团成功 / 拼团失败 | `const joinStatusInfo = ref(["待成团", "拼团成功", "拼团失败"]);` |
| 74 | 低 | template | 逻辑比较值 | 待加入 | `<span>{{ item.username === "待加入" ? $t("待加入") : item.username }}</span>` |

### pages/member/group/Info.vue （2 条，高 1）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 325 | 高 | script | 脚本字符串 | 待成团 / 拼团成功 / 拼团失败 | `const joinStatusInfo = ref(["待成团", "拼团成功", "拼团失败"]);` |
| 75 | 低 | template | 逻辑比较值 | 待加入 / 待加入 | `<span :style="[{overflow: isEn && item.username === '待加入' ? 'visible' : 'hidden'}]">{{ item.username === "待加入" ? $t("待加入") : item.username }}</span>` |

### pages/member/collectShop/List.vue （1 条，高 1）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 227 | 高 | script | 脚本字符串 | 热销 | `item.current = "热销";` |

### utils/request.ts （1 条，高 1）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 171 | 高 | script | 脚本字符串 | 服务器连接失败，请检查网络 | `message: "服务器连接失败，请检查网络"` |

### pages/chat/index.vue （13 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 165 | 低 | script | 日志输出 | 连接成功 | `console.log("WebSocket 连接成功");` |
| 169 | 低 | script | 日志输出 | 收到消息 | `console.log("收到消息", event.data);` |
| 172 | 低 | script | 日志输出 | 解析后的数据 | `console.log("解析后的数据", data);` |
| 187 | 低 | script | 日志输出 | 消息解析失败 | `console.error("WebSocket 消息解析失败:", error);` |
| 192 | 低 | script | 日志输出 | 连接关闭 | `console.log("WebSocket 连接关闭");` |
| 196 | 低 | script | 日志输出 | 连接错误 | `console.error("WebSocket 连接错误");` |
| 264 | 低 | script | 日志输出 | 获取消息列表失败 | `console.error("获取消息列表失败:", error);` |
| 286 | 低 | script | 日志输出 | 获取店铺详情失败 | `console.error("获取店铺详情失败:", error);` |
| 305 | 低 | script | 日志输出 | 获取商品详情失败 | `console.error("获取商品详情失败:", error);` |
| 337 | 低 | script | 日志输出 | 获取订单详情失败 | `console.error("获取订单详情失败:", error);` |
| 548 | 低 | script | 日志输出 | 发送商品失败 | `console.error("发送商品失败:", error);` |
| 591 | 低 | script | 日志输出 | 发送订单失败 | `console.error("发送订单失败:", error);` |
| 617 | 低 | script | 日志输出 | 设置已读失败 | `console.error("设置已读失败:", error);` |

### utils/websocket.ts （9 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 86 | 低 | script | 日志输出 | 未连接 | `console.error("[WebSocket] 未连接");` |
| 93 | 低 | script | 日志输出 | 初始化连接中... | `this.log("WebSocket", '初始化连接中... ${this.url}');` |
| 108 | 低 | script | 日志输出 | 连接成功 / 等待服务端数据推送 | `this.log("WebSocket", '连接成功,等待服务端数据推送[onopen]... ${this.url}');` |
| 119 | 低 | script | 日志输出 | 连接断开 | `this.log("WebSocket", '连接断开[onclose]... ${this.url}');` |
| 129 | 低 | script | 日志输出 | 连接异常 | `this.log("WebSocket", '连接异常[onerror]... ${this.url}');` |
| 140 | 低 | script | 日志输出 | 尝试重连... | `this.log("WebSocket", '尝试重连... (${this.reconnectAttempts}/${this.maxReconnectAttempts}) ${this.url}');` |
| 147 | 低 | script | 日志输出 | 最大重连失败，终止重连 | `this.log("WebSocket", '最大重连失败，终止重连: ${this.url}');` |
| 174 | 低 | script | 日志输出 | 发送心跳数据... | `this.log("WebSocket", "发送心跳数据...");` |
| 176 | 低 | script | 日志输出 | 未连接 | `console.error("[WebSocket] 未连接");` |

### utils/util.ts （5 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 174 | 低 | script | 注释残留 | 处理 null、undefined 或非对象类型 | `// 处理 null、undefined 或非对象类型` |
| 178 | 低 | script | 注释残留 | 处理 Date 类型 | `// 处理 Date 类型` |
| 182 | 低 | script | 注释残留 | 处理 Array 类型 | `// 处理 Array 类型` |
| 186 | 低 | script | 注释残留 | 处理 Object 类型 | `// 处理 Object 类型` |
| 201 | 低 | script | 注释残留 | 静态资源输出 | `* 静态资源输出` |

### decorate/components/pc/pcHeaderRuntime/components/PcHeaderOverseasRuntime.vue （3 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 294 | 低 | script | 逻辑比较值 | 阴影 | `if (["1", "shadow", "阴影"].includes(raw)) return "shadow";` |
| 295 | 低 | script | 逻辑比较值 | 边框 | `if (["2", "border", "line", "边框"].includes(raw)) return "border";` |
| 296 | 低 | script | 逻辑比较值 | 不显示 / 无 | `if (["3", "none", "hidden", "no", "不显示", "无"].includes(raw)) return "none";` |

### decorate/components/pc/pcHeaderRuntime/components/PcHeaderSimpleRuntime.vue （3 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 293 | 低 | script | 逻辑比较值 | 阴影 | `if (["1", "shadow", "阴影"].includes(raw)) return "shadow";` |
| 294 | 低 | script | 逻辑比较值 | 边框 | `if (["2", "border", "line", "边框"].includes(raw)) return "border";` |
| 295 | 低 | script | 逻辑比较值 | 不显示 / 无 | `if (["3", "none", "hidden", "no", "不显示", "无"].includes(raw)) return "none";` |

### pages/article/src/productItem.vue （3 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 90 | 低 | script | 日志输出 | 获取商品 / 详情失败 | `console.error('获取商品 ${item.id} 详情失败:', error);` |
| 103 | 低 | script | 日志输出 | 获取商品 / 详情失败 | `console.error('获取商品 ${item} 详情失败:', error);` |
| 113 | 低 | script | 日志输出 | 获取商品列表失败 | `console.error("获取商品列表失败:", error);` |

### pages/giftCard/src/giftCardActions.vue （3 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 228 | 低 | script | 正则字面量 | 卡号 | `const numberMatch = lines[0].match(/^(?:卡号\|Card No\.?\|Card Number)(?:：\|:)\s*(.+)$/i);` |
| 229 | 低 | script | 正则字面量 | 卡密 | `const pwdMatch = lines[1].match(/^(?:卡密\|Card Pwd\|Card Password)(?:：\|:)\s*(.+)$/i);` |
| 265 | 低 | script | 日志输出 | 无法读取剪切板 | `console.log("无法读取剪切板:", error);` |

### pages/member/order/src/OrderProductTable.vue （3 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 77 | 低 | template | 逻辑比较值 | 已过期 | `formState.cycleCardInfo?.statusName !== '已过期' &&` |
| 246 | 低 | script | 逻辑比较值 | 自提 | `if (shippingTypeName.includes("自提")) return true;` |
| 264 | 低 | script | 逻辑比较值 | 无需配送 | `return shippingTypeName.includes("无需配送");` |

### components/product/src/productVideo.vue （2 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 183 | 低 | script | 日志输出 | 播放失败 | `console.error("播放失败:", err);` |
| 278 | 低 | script | 日志输出 | 全屏切换失败 | `console.error("全屏切换失败:", err);` |

### decorate/components/pc/pcHeaderRuntime/components/PcHeaderTopBarRuntime.vue （2 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 293 | 低 | script | 日志输出 | 加载地区失败 | `console.error("[PcHeaderTopBarRuntime.ensureRegionData] 加载地区失败:", error);` |
| 304 | 低 | script | 日志输出 | 获取购物车数量失败 | `console.error("[PcHeaderTopBarRuntime.loadCartCount] 获取购物车数量失败:", error);` |

### pages/chat/src/ChatInput.vue （2 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 235 | 低 | script | 日志输出 | 图片上传失败 | `console.error("图片上传失败:", error);` |
| 364 | 低 | script | 日志输出 | 视频上传失败 | `console.error("视频上传失败:", error);` |

### pages/chat/src/ChatList.vue （2 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 211 | 低 | script | 日志输出 | 获取会话列表失败 | `console.error("获取会话列表失败:", error);` |
| 233 | 低 | script | 日志输出 | 筛选会话列表失败 | `console.error("筛选会话列表失败:", error);` |

### pages/order/src/presaleSteps.vue （2 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 269 | 低 | script | 逻辑比较值 | 自提 | `if (total?.isPickup \|\| shippingTypeName.includes("自提")) return 2;` |
| 270 | 低 | script | 逻辑比较值 | 同城 | `if (total?.deliveryOrderInfo?.type === 1 \|\| shippingTypeName.includes("同城")) return 3;` |

### pages/search/components/Filter.vue （2 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 261 | 低 | script | 日志输出 | 获取搜索过滤器失败 | `console.error("[Filter._getSearchFilter] 获取搜索过滤器失败:", error);` |
| 584 | 低 | script | 日志输出 | 获取筛选项失败 | `console.error("获取筛选项失败:", error);` |

### pages/search/components/useSearchFilterData.ts （2 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 44 | 低 | script | 日志输出 | 获取搜索筛选项失败 | `console.error("[useSearchFilterData.fetchFilter] 获取搜索筛选项失败:", error);` |
| 61 | 低 | script | 日志输出 | 获取属性筛选项失败 | `console.error("[useSearchFilterData.fetchEsFilter] 获取属性筛选项失败:", error);` |

### components/common/components/DefSearchNew.vue （1 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 207 | 低 | script | 日志输出 | 搜索关键词失败 | `console.error("[DefSearchNew.onSearchKeyup] 搜索关键词失败:", error);` |

### components/common/EsFilter.vue （1 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 163 | 低 | script | 日志输出 | 获取筛选项失败 | `console.error("获取筛选项失败:", error);` |

### components/member/login/BaseLogin.vue （1 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 202 | 低 | script | 日志输出 | 获取CSRF Token失败 | `console.error("获取CSRF Token失败:", error);` |

### components/member/login/OverseasLogin.vue （1 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 262 | 低 | script | 日志输出 | 获取CSRF Token失败 | `console.error("获取CSRF Token失败:", error);` |

### components/product/ProductInfo.vue （1 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 841 | 低 | script | 日志输出 | 获取预售优惠活动失败 | `console.error("获取预售优惠活动失败", error);` |

### composables/useSearchPage.ts （1 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 288 | 低 | script | 开发期错误 | 必须在 search 容器组件内使用 | `throw new Error("[useSearchPageContext] 必须在 search 容器组件内使用");` |

### decorate/components/DecorateRenderer.vue （1 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 45 | 低 | script | 日志输出 | 装修模块类型 / 未在 decorate/registry/pc.ts 注册，已跳过渲染。 | `console.warn('[DecorateRenderer] 装修模块类型 "${type}" 未在 decorate/registry/pc.ts 注册，已跳过渲染。');` |

### decorate/components/pc/PcHomeHero.vue （1 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 991 | 低 | script | 正则字面量 | 万亿KMB | `const match = text.match(/^(.*?)([万亿KMB])$/);` |

### pages/article/issue/Index.vue （1 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 124 | 低 | script | 日志输出 | 帮助 | `console.log("帮助", result);` |

### pages/member/coupon/List.vue （1 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 76 | 低 | script | 日志输出 | 优惠券22 | `console.log("优惠券22",result.records)` |

### pages/member/order/src/OrderInfoHeader.vue （1 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 105 | 低 | template | 逻辑比较值 | 已过期 | `formState.cycleCardInfo?.statusName !== '已过期'` |

### pages/member/order/src/OrderStatusSteps.vue （1 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 61 | 低 | script | 逻辑比较值 | 同城 | `if (shippingTypeName.includes("同城")) return 3;` |

### pages/search/components/CatGoodsList.vue （1 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 163 | 低 | script | 日志输出 | 获取搜索商品列表失败 | `console.error("[CatGoodsList.getSearchProduct] 获取搜索商品列表失败:", error);` |

### store/common.ts （1 条，高 0）

| 行 | 严重度 | 区域 | 类型 | 中文片段 | 代码 |
| --- | --- | --- | --- | --- | --- |
| 234 | 低 | script | 日志输出 | 获取分类楼层失败 | `console.error("[common.ensureAllCate] 获取分类楼层失败:", error);` |

## B. 安装向导 pages/install（部署工具，通常不随跨境版对外）

共 44 条（高 44）。加 `--all` 查看明细。

## C. 随多商户 / B2B 特性（跨境版含该特性时需处理）

共 5 条（高 4）。加 `--all` 查看明细。

## D. v1 归档文件（未启用，切回时才需处理）

共 122 条（高 97）。加 `--all` 查看明细。

## E. 跨境版已裁剪（可忽略）

共 68 条（高 48）。加 `--all` 查看明细。
