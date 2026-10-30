# B5 多语言补充实现记录

## 已完成

### 币种

```text
GET  /adminapi/lang/currency/dictionary
POST /adminapi/lang/currency/update
```

- dictionary 读取 `currency_dictionary`（id / name / code / numericCode）
- update 复用 `CurrencyService.update`，按 `CurrencyDTO.id` 更新

### 语言

```text
POST /adminapi/lang/locales/update
POST /adminapi/lang/localesRelation/update
```

- locales update 复用 `LocalesService.update`
- localesRelation update 复用 `LocalesRelationService.update`（与 updateField 同实现）

### 翻译

```text
POST /adminapi/lang/translations/create
POST /adminapi/lang/translations/batchCreate
POST /adminapi/lang/translations/batchTranslate
```

- create 与 createTranslations 同实现
- batchCreate：按 `translationName` 换行拆词条，`dataType[0]` 作为数据类型
- batchTranslate：未接翻译凭据，返回空映射，不伪造译文

## 验证

```text
cargo check -p tigshop-admin-api
Finished
```

路由差异：

```text
上一轮：missing=125
本轮：missing=98
```

## 提交

```text
febeb7d1 feat(lang): add currency dictionary and translation create aliases
```

## 未完成

```text
batchTranslate 真实机器翻译
翻译调用记录 translation_call_log
语言/货币缓存失效广播
完整二进制 build / 部署
```
