# Rust v5.8.61 代码补齐状态（第 20 轮 / 构建前）

## 验证（仅编译与契约）

```text
cargo check --workspace
Finished

路由契约：
Java 1743 / Rust 2118 / missing 0 / extra 380
```

说明：按用户要求，本阶段只做代码补齐，不逐项跑接口验收，也未触发生产构建。

## 已补齐（本轮 20 轮内）

```text
B1 商品禁售/供应商禁售
B2 体积运费
B3 线下认款
B4 阿里云图搜：OSS 上传、检索、图库构建、check、单商品同步、商品变更入队、删除清图
B5 多语言/币种/翻译别名
B6 供应链：绑定、直采入库、详情、对账导出、资质上传
B7 店铺分组/标签
B8 主流程、礼品卡 Excel、电子卡券模板、AI 图片翻译/消除、图搜语言列表
B9 POS 桌台状态机（生产写入回归通过）
商户营业执照上传
Analytics 会员消费排行导出
用户/管理员登出 JWT 吊销
支付下单：微信 v3、支付宝、PayPal、Stripe、Xendit、Antom
支付回调验签：微信/支付宝/Stripe/Xendit/Antom
渠道退款：微信、支付宝、Stripe、PayPal、Xendit、Antom
退款审核自动调用渠道
```

## 仍未补齐（真实剩余）

### 支付

```text
Apple Pay validateMerchant / confirm / 收单
充值支付创建（user_recharge_order createPay）
子商户绑定（微信服务商 API）
分账（profit sharing）
汇付 / 拉卡拉 / 易宝 / YabandPay / YunPay 下单与退款
商户进件渠道提交/媒体上传（微信/收付通）
```

### 商户

```text
入驻审核通过后的完整开店链路：
  admin_user / shop / admin_user_shop / merchant_user
  admin role 初始化
  运费模板初始化
  装修模板初始化
  保证金账户初始化
  通知
```

### 内容 / 外部服务

```text
SEO 火山翻译
微信直播回调自动建单
微信直播/菜单/物流等外部 SDK 适配
```

### 前端

```text
Admin/PC 其余 v5.8.61 新页面
```

## 结论

```text
method+path 契约：0 缺失
全工作区编译：通过
生产部署：build-128（本阶段未更新）
```

代码层仍存在上述业务缺口，目标未 100% 完成，不能标记 complete。
后续应先补：
1. Apple Pay confirm + 充值支付 + 子商户/分账
2. 商户开店链路
3. 其余支付渠道
4. SEO/微信直播
5. 前端页面
全部补齐后再逐项验证、统一构建部署。
