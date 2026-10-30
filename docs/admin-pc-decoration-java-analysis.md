# 原版 Java 系统 PC 端装修机制说明

> 目标：说明 TigShop Java 原版如何通过 Admin 装修编辑器，把配置保存为 JSON，再由 PC 商城动态渲染。
>
> 适用范围：PC 首页、PC 自定义页、PC 登录页、PC 搜索页、PC 公共头部。
>
> 结论先行：**原版不是在线写 HTML，而是“结构化 JSON + 模块组件注册表 + 运行时动态渲染”。**

---

## 1. 总体链路

```text
Admin 装修编辑器
  → 选择/拖拽模块
  → 模块 Settings 组件修改配置
  → 生成 pageModule + moduleList JSON
  → 存草稿 draft_data
  → 保存并发布 data
  → PC 请求公开接口
  → 后端读取 decorate.data
  → PC 根据 module.type 找到对应组件
  → 组件用 module 配置渲染
```

核心分层：

```text
编辑器层：Admin Vue 页面
存储层：decorate 表 data / draft_data
接口层：/adminapi/decorate/*、/api/home/home/*
渲染层：Nuxt/Vue3 PC 应用
```

---

## 2. 数据模型

核心表：`decorate`
模型文件：

- [Decorate.java](../java/tigshop-bean/src/main/java/com/tigshop/bean/model/decorate/Decorate.java)

关键字段：

| 字段 | 含义 |
|---|---|
| `decorate_id` | 页面装修 ID |
| `decorate_title` | 页面名称 |
| `decorate_type` | 1=H5，2=PC，3=PC 自定义页 |
| `page_type` | 1=首页，5=登录页，6=搜索页，8=商品详情页等 |
| `is_home` | 是否该类型的默认/首页记录 |
| `shop_id` | 0=平台，>0=店铺 |
| `status` | 1=已发布，0=未发布 |
| `data` | 已发布页面 JSON |
| `draft_data` | 草稿 JSON |
| `parent_id` / `locale_id` | 多语言子页 |
| `binded_h5_id` | PC 自定义页可绑定的 H5 页面 |

PC 首页对应：

```text
decorate_type = 2
page_type      = 1
is_home        = 1
shop_id        = 0
status         = 1
```

相关枚举：

- `DecorateType`：H5=1 / PC=2 / CUSTOM=3
- `DecoratePageType`：HOME=1 / MEMBER=2 / ACTIVITY=3 / CATEGORY=4 / LOGIN=5 / SEARCH=6 / NAVIGATION=7 / H5_PRODUCT_DETAIL=8 / O2O_PLATFORM_HOME=9

---

## 3. 页面 JSON 契约

`decorate.data` 是页面级 JSON，核心结构：

```json
{
  "pageModule": {
    "type": "page",
    "style": 0,
    "backgroundColor": "#f2f3f5",
    "backgroundImage": {},
    "backgroundRepeat": 1,
    "backgroundSize": 1,
    "homeStyleVersion": "new",
    "pcHeader": {
      "logoPic": {},
      "mainNav": [],
      "memberCenterItems": []
    }
  },
  "moduleList": [
    {
      "type": "pc-home-hero",
      "url": "pc",
      "label": "营销通栏",
      "moduleIndex": 1790000000000,
      "isShow": true,
      "fixed": false,
      "module": {
        "left": {},
        "center": {},
        "right": {}
      }
    },
    {
      "type": "pc-product",
      "url": "pc",
      "label": "商品模块",
      "moduleIndex": 1790000000001,
      "isShow": true,
      "module": {
        "productSource": 1,
        "productIds": [],
        "showTitle": 1
      }
    }
  ]
}
```

字段说明：

| 字段 | 说明 |
|---|---|
| `pageModule` | 页面级配置：背景、PC 头部、新旧版标识 |
| `moduleList` | 页面模块列表，顺序即渲染顺序 |
| `type` | 模块类型，前后端契约的稳定标识 |
| `url` | 模块所属目录/平台分类，如 `pc`、`pcCustomPage` |
| `moduleIndex` | 模块实例唯一标识，也用作渲染 key |
| `isShow` | 是否显示；后端下发时会过滤 false |
| `fixed` | 固定模块，不可删除/复制/隐藏 |
| `module` | 模块自己的配置对象，由设置面板产生 |

重要原则：

```text
pageModule 管“页面”
moduleList[].module 管“模块”
后端不解析 module 内部字段，只整体存 JSON
```

---

## 4. Admin 端编辑器

### 4.1 路由

PC 编辑器路由：

```text
/decorate/pc
  → editor/Index.vue
  → decorateType = 2

/decorate/pcCustomPage
  → editor/Index.vue
  → decorateType = 3
```

路由定义：

- [constantsRoutes.ts](../java/view/admin/src/router/constantsRoutes.ts)

PC 装修管理页：

- `/decorate-info/pc-decorate/list`：首页装修
- `/decorate-info/pc-decorate/custom`：自定义页装修
- `/decorate-info/pc-decorate/search-decorate`：搜索页装修
- `/decorate-info/pc-decorate/login-decorate`：登录页装修

### 4.2 编辑器入口

- [editor/Index.vue](../java/view/admin/src/views/decorate/decorate/editor/Index.vue)

核心结构：

```text
顶部栏：退出编辑、页面名称、预览、存至草稿、保存并发布
左侧：模块工具栏 ModuleToolbar
中间：画布，模块拖拽排序、选中、上移下移、显示隐藏、复制删除
右侧：设置面板 SettingPanel
底部：页面设置入口
```

旧版 `decorate/Index.vue` 仍保留，兼容旧入口；当前路由使用的是 `editor/Index.vue`。

### 4.3 模块注册表

- [module-registry.ts](../java/view/admin/src/views/decorate/decorate/editor/config/module-registry.ts)

注册定义：

```ts
registerModule({
  type: "pc-home-hero",
  label: "营销通栏",
  url: "pc",
  platform: "pc-home",
  toolbarGroup: "pcNew",
  preview: () => import("./Preview.vue"),
  settings: () => import("./Settings.vue"),
  getDefaultValue: () => ({ ... })
});
```

重要字段：

| 字段 | 说明 |
|---|---|
| `type` | 冻结契约，不能随意改名 |
| `url` | 模块目录，同 type 不同平台靠它区分 |
| `platform` | 适用页面平台 |
| `toolbarGroup` | 工具栏分组 |
| `preview` | 画布预览组件 |
| `settings` | 右侧设置组件 |
| `getDefaultValue` | 新增模块时的默认值 |
| `visible` | 是否在工具栏显示 |
| `placement` | top/bottom 位置约束 |
| `exclusive` | 是否独占 |
| `shopOnly` | 是否仅店铺端显示 |

模块注册入口：

- [modules/index.ts](../java/view/admin/src/views/decorate/decorate/editor/modules/index.ts)

```ts
import "./pcHome/pc-image-ad";
import "./pcHome/pc-image-nav";
import "./pcHome/pc-home-hero";
import "./pcHome/pc-product";
import "./pcHome/pc-home-coupon";
import "./pcHome/pc-home-seckill";
...
```

### 4.4 编辑器状态

- [decorateEditor.ts](../java/view/admin/src/store/decorateEditor.ts)

主要状态：

```text
decorateId
decorateType
pageType
homeStyleVersion   // new / old
decorateTitle
pageModule
moduleList
activeModuleIndex
editMode           // page / module / pcHeader
hasDraft
isDirty
localeId / parentId / isDefault
```

关键动作：

```text
addModule
removeModule
moveModule
toggleModuleVisibility
selectModule
updateModule
selectPage
selectPcHeader
setHomeStyleVersion
getSaveData
markAsSaved
initDecorate
```

### 4.5 保存数据出口

[decorateEditor.ts:620](../java/view/admin/src/store/decorateEditor.ts#L620)

```ts
function getSaveData(): DecorateData {
  const pm = cloneDeep(pageModule.value);

  if (decorateType.value === DecorateType.PC_HOME) {
    pm.homeStyleVersion = homeStyleVersion.value;

    // 旧版不携带 pcHeader，避免前台误判为新版
    if (homeStyleVersion.value !== "new") {
      delete pm.pcHeader;
    }
  } else {
    delete pm.pcHeader;
  }

  return {
    pageModule: pm,
    moduleList: cloneDeep(moduleList.value)
  };
}
```

注意：

```text
PC 首页才会保存 homeStyleVersion
PC 首页新版才保存 pageModule.pcHeader
其他页面不携带 pcHeader
```

### 4.6 存草稿

[editor/Index.vue:766](../java/view/admin/src/views/decorate/decorate/editor/Index.vue#L766)

```ts
await saveDraft(buildSavePayload(store.getSaveData()));
```

请求：

```text
POST /adminapi/decorate/decorate/saveDraft
```

请求体：

```json
{
  "id": 100,
  "decorateType": 2,
  "pageType": 1,
  "localeId": 0,
  "parentId": 0,
  "data": {
    "pageModule": {},
    "moduleList": []
  }
}
```

后端：

- [DecorateController.java](../java/tigshop-adminapi/src/main/java/com/tigshop/adminapi/controller/decorate/DecorateController.java)
- [DecorateServiceImpl.java:692](../java/tigshop-service/src/main/java/com/tigshop/service/decorate/impl/DecorateServiceImpl.java#L692)

```text
saveDecorateToDraft
  → 写入 decorate.draft_data
  → 不修改 data
  → 不影响线上 PC 页面
```

### 4.7 发布

[editor/Index.vue:784](../java/view/admin/src/views/decorate/decorate/editor/Index.vue#L784)

```text
POST /adminapi/decorate/decorate/publish
```

后端：

[DecorateServiceImpl.java:719](../java/tigshop-service/src/main/java/com/tigshop/service/decorate/impl/DecorateServiceImpl.java#L719)

```text
publishDecorate
  → data = 新 JSON
  → status = 1
  → draft_data = ""
  → update_time = 当前时间
  → invalidateHomeDecorateAfterCommit()
```

发布后才替换线上数据并清理首页装修缓存。

### 4.8 预览

预览流程：

```text
1. 先 saveDraft 保存当前编辑内容
2. 打开 PC 页面并带 previewId
   /?previewId=<decorateId>
3. 后端预览接口优先读 draft_data
4. 前台立即看到未发布效果
```

预览不会修改 `data`，但会写 `draft_data`。

---

## 5. 后端公开接口

公开 PC 首页接口：

- [HomeController.java](../java/tigshop-api/src/main/java/com/tigshop/api/controller/home/HomeController.java)

```text
GET /api/home/home/pcIndex
```

参数：

```text
previewId    预览用，装修 ID
id           指定已发布 ID
decorateType 自定义页类型
```

逻辑：

```text
无 previewId：
  getPcHomeDecorate()
  → 查询 decorate_type=2
  → page_type=1
  → is_home=1
  → status=1
  → shop_id=0
  → locale_id=0

有 previewId / id：
  getPcPreviewDecorate()
  → 按主键查询
  → draft_data 优先
  → 其次 data
```

相关接口：

| 接口 | 用途 |
|---|---|
| `/api/home/home/pcIndex` | PC 首页装修 |
| `/api/home/home/pcHeader` | PC 公共头部 |
| `/api/home/home/pcLogin` | PC 登录页装修 |
| `/api/home/home/pcSearch` | PC 搜索页装修 |
| `/api/home/home/index` | H5 首页 |

### 5.1 后端加载逻辑

[DecorateServiceImpl.java:1093](../java/tigshop-service/src/main/java/com/tigshop/service/decorate/impl/DecorateServiceImpl.java#L1093)

`loadDecorateModule` 做以下事情：

```text
1. 按 type/pageType/isHome/status/shopId/localeId 查询记录
2. 没有记录时使用内置默认装修数据
3. 读取 decorate.data
4. JSON 字段 snake_case → camelCase
5. 反序列化为 DecorateModuleListVO
6. 过滤 isShow=false 的模块
7. 处理 X-Locale-Code 多语言子页
8. 特殊模块补充数据（如 catNav、specialOffer）
9. 返回 moduleList + pageModule
```

### 5.2 缓存

`getDecorateModule` 使用：

```text
homeDecorateCache.get(cacheKey, loader)
```

缓存 key 包含：

```text
type / pageType / isHome / status
```

发布、删除、批量操作后调用：

```text
invalidateHomeDecorateAfterCommit()
```

确保 PC 下一次请求拿到新数据。

### 5.3 多语言

请求头：

```text
X-Locale-Code
```

后端查找：

```text
parent_id = 默认装修 ID
locale_id = 当前语言 ID
```

存在语言子页时，用子页覆盖默认页。

---

## 6. PC 前台渲染

PC 是 Nuxt/Vue3 应用。

### 6.1 首页入口

- [pages/index.vue](../java/view/Tigshop-Pc/pages/index.vue)
- [api/home/home.ts](../java/view/Tigshop-Pc/api/home/home.ts)

```ts
const { data: homeData } = await useAsyncData(
  "o2o-pc-home-decorate",
  () => getHomeData({ previewId: previewId.value }),
  { watch: [previewId] }
);
```

```ts
export const getHomeData = (params: {}) => {
  return request<HomeDataResult>({
    url: "home/home/pcIndex",
    method: "get",
    params
  });
};
```

拿到数据后写入 `homeStore`：

```text
moduleList
pageModule
decorateId
previewId
```

### 6.2 新旧版本分流

- [isNewHome.ts](../java/view/Tigshop-Pc/decorate/isNewHome.ts)

```ts
export function isNewHome(pageModule, moduleList) {
  const v = pageModule?.homeStyleVersion;
  if (v === "new") return true;
  if (v === "old") return false;
  return moduleList.some((m) => String(m?.type ?? "").includes("-"));
}
```

规则：

```text
homeStyleVersion = "new" → 新渲染器
homeStyleVersion = "old" → 旧硬编码链路
无标记：
  模块 type 含 "-"（kebab） → 新版
  否则                       → 旧版
```

### 6.3 新版渲染器

- [DecorateRenderer.vue](../java/view/Tigshop-Pc/decorate/components/DecorateRenderer.vue)

```vue
<template v-for="entry in resolvedModules" :key="entry.key">
  <component
    :is="entry.component"
    :module="entry.item.module"
  />
</template>
```

职责：

```text
1. 过滤 isShow=false
2. 按 type 查注册表
3. 未注册 type 跳过，开发环境警告
4. 前 4 个模块支持同步 critical 组件
5. 其余模块异步懒加载
```

### 6.4 PC 组件注册表

- [registry/pc.ts](../java/view/Tigshop-Pc/decorate/registry/pc.ts)

```ts
const PC_MODULE_REGISTRY = {
  "pc-image-ad": PcImageAd,
  "pc-image-nav": PcImageNav,
  "pc-image-square-ad": PcImageSquareAd,
  "pc-image-hotarea": PcImageHotarea,
  "pc-product": PcProduct,
  "pc-divider": PcDivider,
  "pc-spacer": PcSpacer,
  "pc-notice": PcNotice,
  "pc-title-ad": PcTitleAd,
  "pc-home-quick-nav": PcQuickNav,
  "pc-home-coupon": PcCoupon,
  "pc-home-groupon": PcGroupon,
  "pc-home-seckill": PcSeckill,
  "pc-product-group": PcProductGroup,
  "pc-home-hero": PcHomeHero,
  "pc-category-nav": PcCategoryNavWrapper,
  "pc-recommend-product": PcRecommendProduct
};
```

组件只接收：

```text
module 配置对象
```

### 6.5 旧版渲染

`pages/index.vue` 保留旧链路：

```vue
<HomeBanner v-if="item.type == 'pcBanner'" />
<HomeSeckill v-if="item.type == 'pcSeckill'" />
<HomeCatProductNormal v-if="item.type == 'pcCatProductNormal'" />
<HomeCatProductWide v-if="item.type == 'pcCatProductWide'" />
<HomeCatProductSimple v-if="item.type == 'pcCatProductSimple'" />
<HomeImageAd v-if="item.type == 'pcImageAd'" />
<HomeRecommendProduct v-if="item.type == 'pcRecommendProduct'" />
...
```

旧版模块 type 是 camelCase：

```text
pcBanner
pcSeckill
pcCoupon
pcCatProductNormal
pcCatProductWide
pcCatProductSimple
pcCatWide
pcImageAd
pcRecommendProduct
pcCustomAd1
pcCustomAd2
pcCategoryNav
pcCategoryNav3
pcGroupon
```

新版模块 type 是 kebab-case：

```text
pc-image-ad
pc-product
pc-home-hero
pc-home-coupon
pc-home-groupon
pc-home-seckill
pc-category-nav
pc-recommend-product
...
```

### 6.6 模块数据从哪来

装修 JSON 只保存“配置”，不保存实时业务数据。

| 模块 | 运行时额外请求 |
|---|---|
| 商品模块 | 商品列表/推荐接口 |
| 优惠券模块 | 优惠券接口 |
| 秒杀模块 | 秒杀接口 |
| 拼团模块 | 拼团接口 |
| 分类导航 | 分类接口 |
| 图片广告 | 直接用配置里的图片 URL |
| 标题/公告/分割线 | 直接用配置 |

所以：

```text
装修配置决定“怎么展示”
业务接口决定“展示什么数据”
```

### 6.7 PC 公共头部

PC 头部不放在 `moduleList`，而是：

```text
pageModule.pcHeader
```

后台通过：

```text
GET /api/home/home/pcHeader
```

单独返回给前台。

前台在首页顶部渲染公共头部，常见配置：

```text
logoPic
logoWidth
showMainNav
mainNavPaddingTop / Bottom
cartPic
memberCenterItems
benefitNoticeGroups
```

发布时：

```text
只有 PC 首页新版才保存 pcHeader
旧版删除 pcHeader
其他页面不保存 pcHeader
```

---

## 7. PC 其他装修页面

### 7.1 登录页

```text
decorate_type = 2
page_type = 5
```

接口：

```text
/api/home/home/pcLogin
```

PC 端：

```text
decorateApi.getPcLogin()
→ DecorateModuleListVO
```

用途：

```text
登录方式
背景图 / 背景色
头部高度 / 阴影
```

### 7.2 搜索页

```text
decorate_type = 2
page_type = 6
```

接口：

```text
/api/home/home/pcSearch
```

如果后台没配置，返回空 VO，PC 回退默认搜索布局。

### 7.3 自定义页

```text
decorate_type = 3
```

后台发布后生成独立自定义页，PC 通过自定义页 ID / 预览 ID 渲染。

---

## 8. 完整时序

```text
Admin 打开 /decorate/pc?id=100
  ↓
editor/Index.vue 初始化 store
  ↓
GET /adminapi/decorate/decorate/detail?id=100&decorateType=2
  ↓
后端返回 data/draftData/pageModule/moduleList
  ↓
编辑器渲染画布 + 工具栏 + 设置面板
  ↓
用户拖拽模块、改配置
  ↓
store.moduleList / pageModule 变化
  ↓
点“存至草稿”
  → POST saveDraft → draft_data
  ↓
点“预览”
  → saveDraft
  → 打开 /?previewId=100
  → GET /api/home/home/pcIndex?previewId=100
  → 后端读 draft_data
  ↓
点“保存并发布”
  → POST publish
  → data = 新 JSON
  → status = 1
  → draft_data = ""
  → 清缓存
  ↓
用户访问 PC 首页
  → GET /api/home/home/pcIndex
  → 后端读已发布 data
  → 返回 moduleList + pageModule
  → DecorateRenderer 按 type 渲染组件
  → 组件用 module 配置和业务接口渲染
```

---

## 9. 关键设计点

### 9.1 模块契约稳定

```text
type 是前后端契约
一旦发布，不能随意改名
旧 type 必须保留兼容
```

`pcHome` 与 `pcCustomPage` 可能存在同 type 不同实现，靠：

```text
type + url
```

共同定位模块定义。

### 9.2 保存整体透传

编辑器保存时不做字段白名单剔除：

```text
未知字段原样保留
旧字段不丢
新字段可透传
```

这是旧装修数据兼容的关键。

### 9.3 草稿与发布分离

```text
draft_data：编辑中，可覆盖，不影响线上
data：已发布，PC 实际使用
```

### 9.4 页面级与模块级分离

```text
pageModule：背景、PC 头部、homeStyleVersion
moduleList[].module：单个模块配置
```

### 9.5 新旧版共存

```text
homeStyleVersion = new → 新渲染器
homeStyleVersion = old → 旧渲染链路
```

如果没有版本标记，则按模块 type 是否包含 `-` 推断。

### 9.6 后端不解析模块业务字段

模块配置是自由 JSON，后端只负责：

```text
存储
读取
camelCase 转换
过滤 isShow
多语言覆盖
缓存
```

模块字段的校验/默认值由前端设置面板负责。

---

## 10. 迁移到 React + Semi 的注意事项

如果要在 React 后台复刻原版 PC 装修：

```text
1. 复用同一份 JSON 契约
   - pageModule / moduleList
   - type / url / moduleIndex / isShow / module
   - homeStyleVersion
   - pcHeader

2. 建 React 模块注册表
   type → { Preview, Settings, getDefaultValue }

3. 保留草稿/发布/预览语义
   - saveDraft → draft_data
   - publish → data + status=1 + 清缓存
   - previewId → 读 draft_data

4. 保留旧模块兼容
   - camelCase 旧模块不能删
   - kebab-case 新模块不能随便改名

5. 页面设置与模块设置分开
   - 页面背景、PC 头部放 pageModule
   - 单模块配置放 module

6. 不要在前端写死模块顺序
   - moduleList 顺序就是渲染顺序
   - moduleIndex 作为 key
```

---

## 11. 文件索引

### Admin 编辑器

| 文件 | 作用 |
|---|---|
| `java/view/admin/src/router/constantsRoutes.ts` | PC 装修编辑路由 |
| `java/view/admin/src/views/decorate/decorate/editor/Index.vue` | PC 装修编辑器入口 |
| `java/view/admin/src/store/decorateEditor.ts` | 编辑器状态与保存数据出口 |
| `java/view/admin/src/views/decorate/decorate/editor/config/module-registry.ts` | 模块注册表 |
| `java/view/admin/src/views/decorate/decorate/editor/modules/index.ts` | 模块自注册入口 |
| `java/view/admin/src/views/decorate/decorate/editor/modules/pcHome/*` | PC 新版模块 |
| `java/view/admin/src/views/decorate/decorate/editor/modules/pc/*` | PC 旧版模块 |
| `java/view/admin/src/views/decorate/decorate/editor/modules/pcCustomPage/*` | PC 自定义页模块 |

### 后端

| 文件 | 作用 |
|---|---|
| `java/tigshop-adminapi/.../decorate/DecorateController.java` | 装修管理接口 |
| `java/tigshop-bean/.../model/decorate/Decorate.java` | decorate 表模型 |
| `java/tigshop-bean/.../enums/decorate/DecorateType.java` | 装修类型枚举 |
| `java/tigshop-bean/.../enums/decorate/DecoratePageType.java` | 页面类型枚举 |
| `java/tigshop-service/.../decorate/impl/DecorateServiceImpl.java` | 保存/发布/查询/缓存/多语言 |
| `java/tigshop-api/.../home/HomeController.java` | PC 公开装修接口 |

### PC 前台

| 文件 | 作用 |
|---|---|
| `java/view/Tigshop-Pc/pages/index.vue` | PC 首页入口 |
| `java/view/Tigshop-Pc/api/home/home.ts` | 首页装修接口 |
| `java/view/Tigshop-Pc/decorate/components/DecorateRenderer.vue` | 新版动态渲染器 |
| `java/view/Tigshop-Pc/decorate/registry/pc.ts` | PC 模块组件注册表 |
| `java/view/Tigshop-Pc/decorate/isNewHome.ts` | 新旧版判定 |
| `java/view/Tigshop-Pc/decorate/normalize.ts` | type 归一化与别名 |
| `java/view/Tigshop-Pc/decorate/components/pc/*` | PC 新版模块组件 |
