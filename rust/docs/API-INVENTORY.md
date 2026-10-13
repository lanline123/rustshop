# TigShop API 清单（由 Java 源码自动提取）

> 本文件由 `tools/java_api_parser.py` 从 Java 控制器的 Spring MVC 与
> Swagger 注解直接生成，是 Rust 重写时的**接口契约基线**。
> 请勿手工编辑；重新生成：`python tools/java_api_parser.py --all`。

| 指标 | 数值 |
|---|---|
| 控制器 | 251 |
| 接口总数 | 1564 |
| 有 @Operation 文档 | 1529 |
| 带权限表达式 | 864 |

## 模块概览

| 模块 | 接口数 | 说明 |
|---|---|---|
| `adminapi` | 1231 | 管理后台 `/adminapi` |
| `api` | 333 | 商城前台 `/api` |

## adminapi / (untagged)

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/messagepush/messagepush/push` |  | - | jwt |
| GET | `/adminapi/messagepush/messagepush/tigshopConfig` |  | - | jwt |
| GET | `/adminapi/openapi/mcp/v1/capabilities` |  | - | jwt |
| GET | `/adminapi/openapi/mcp/v1/categories` |  | - | jwt |
| POST | `/adminapi/openapi/mcp/v1/categories/execute` |  | - | jwt |
| POST | `/adminapi/openapi/mcp/v1/categories/prepare` |  | - | jwt |
| GET | `/adminapi/openapi/mcp/v1/connection/status` |  | - | jwt |
| GET | `/adminapi/openapi/mcp/v1/import/requirements` |  | - | jwt |
| GET | `/adminapi/openapi/mcp/v1/media` |  | - | jwt |
| POST | `/adminapi/openapi/mcp/v1/media/execute` |  | - | jwt |
| POST | `/adminapi/openapi/mcp/v1/media/prepare` |  | - | jwt |
| POST | `/adminapi/openapi/mcp/v1/media/upload` |  | - | jwt |
| POST | `/adminapi/openapi/mcp/v1/media/upload-token` |  | - | jwt |
| GET | `/adminapi/openapi/mcp/v1/products` |  | - | jwt |
| GET | `/adminapi/openapi/mcp/v1/products/analysis` |  | - | jwt |
| POST | `/adminapi/openapi/mcp/v1/products/batch/execute` |  | - | jwt |
| POST | `/adminapi/openapi/mcp/v1/products/batch/preview` |  | - | jwt |
| GET | `/adminapi/openapi/mcp/v1/products/context` |  | - | jwt |
| POST | `/adminapi/openapi/mcp/v1/products/import` |  | - | jwt |
| POST | `/adminapi/openapi/mcp/v1/products/validate` |  | - | jwt |
| GET | `/adminapi/openapi/mcp/v1/products/{id}` |  | - | jwt |
| POST | `/adminapi/openapi/mcp/v1/products/{id}/copy` |  | - | jwt |
| POST | `/adminapi/openapi/mcp/v1/products/{id}/delete` |  | - | jwt |
| POST | `/adminapi/openapi/mcp/v1/products/{id}/field` |  | - | jwt |
| GET | `/adminapi/openapi/mcp/v1/products/{id}/promotion-links` |  | - | jwt |
| POST | `/adminapi/openapi/mcp/v1/products/{id}/recycle` |  | - | jwt |
| POST | `/adminapi/openapi/mcp/v1/products/{id}/restore` |  | - | jwt |
| POST | `/adminapi/openapi/mcp/v1/products/{id}/status` |  | - | jwt |

## adminapi / PC分类抽屉

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/decorate/pcCatFloor/batch` | 批量操作 | `pcCatFloorModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/pcCatFloor/clearCache` | 清除缓存 | `pcCatFloorManage` | jwt+perm |
| POST | `/adminapi/decorate/pcCatFloor/create` | 创建 | `pcCatFloorModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/pcCatFloor/del` | 删除 | `pcCatFloorModifyManage` | jwt+perm |
| GET | `/adminapi/decorate/pcCatFloor/detail` | 获取详情 | `pcCatFloorManage` | jwt+perm |
| GET | `/adminapi/decorate/pcCatFloor/list` | 获取列表 | `pcCatFloorManage` | jwt+perm |
| POST | `/adminapi/decorate/pcCatFloor/update` | 更新 | `pcCatFloorModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/pcCatFloor/updateField` | 更新字段 | `pcCatFloorModifyManage` | jwt+perm |

## adminapi / PC导航栏

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/decorate/pcNavigation/batch` | 批量操作 | `pcNavigationModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/pcNavigation/create` | 创建 | `pcNavigationModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/pcNavigation/del` | 删除 | `pcNavigationModifyManage` | jwt+perm |
| GET | `/adminapi/decorate/pcNavigation/detail` | 获取详情 | `pcNavigationManage` | jwt+perm |
| GET | `/adminapi/decorate/pcNavigation/getParentNav` | 上级导航处理 | `pcNavigationManage` | jwt+perm |
| GET | `/adminapi/decorate/pcNavigation/list` | 获取列表 | `pcNavigationManage` | jwt+perm |
| GET | `/adminapi/decorate/pcNavigation/selectLink` | 选择链接地址 | `pcNavigationManage` | jwt+perm |
| POST | `/adminapi/decorate/pcNavigation/update` | 更新 | `pcNavigationModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/pcNavigation/updateField` | 更新字段 | `pcNavigationModifyManage` | jwt+perm |

## adminapi / app版本管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/setting/appVersion/detail` | 详情 | `appVersionManage` | jwt+perm |
| POST | `/adminapi/setting/appVersion/update` | 执行更新操作 | `appVersionUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/appVersion/uploadFile` | 上传更新文件 | `appVersionUpdateManage` | jwt+perm |

## adminapi / im配置

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/im/config/config/detail` | 获取详情 | - | jwt |
| POST | `/im/config/config/save` | 保存 | - | jwt |

## adminapi / seo

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/setting/seo/batchUpdateCategoryLink` | 商品类目链接批量更新 | - | jwt |
| POST | `/adminapi/setting/seo/batchUpdateProductLink` | 商品链接批量更新 | - | jwt |
| GET | `/adminapi/setting/seo/translateToSeoIdentification` | 中文转英文并格式化 | - | jwt |

## adminapi / 买家秀分组管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/buyerShow/group/create` | 新建买家秀分组 | `buyerShowUpdateManage` | jwt+perm |
| POST | `/adminapi/promotion/buyerShow/group/del` | 删除买家秀分组 | `buyerShowUpdateManage` | jwt+perm |
| GET | `/adminapi/promotion/buyerShow/group/detail` | 买家秀分组详情 | `buyerShowManage` | jwt+perm |
| GET | `/adminapi/promotion/buyerShow/group/list` | 买家秀分组列表 | `buyerShowManage` | jwt+perm |
| POST | `/adminapi/promotion/buyerShow/group/update` | 更新买家秀分组 | `buyerShowUpdateManage` | jwt+perm |

## adminapi / 买家秀管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/buyerShow/audit` | 审核（通过、拒绝） | `buyerShowUpdateManage` | jwt+perm |
| POST | `/adminapi/promotion/buyerShow/batch` | 批量操作（审核操作类型为audit） | `buyerShowUpdateManage` | jwt+perm |
| POST | `/adminapi/promotion/buyerShow/batchDel` | 批量删除买家秀 | `buyerShowDeleteManage` | jwt+perm |
| POST | `/adminapi/promotion/buyerShow/comment` | 评论买家秀 | `buyerShowUpdateManage` | jwt+perm |
| POST | `/adminapi/promotion/buyerShow/create` | 新建买家秀 | `buyerShowUpdateManage` | jwt+perm |
| POST | `/adminapi/promotion/buyerShow/del` | 删除买家秀 | `buyerShowDeleteManage` | jwt+perm |
| GET | `/adminapi/promotion/buyerShow/detail` | 买家秀详情 | `buyerShowManage` | jwt+perm |
| GET | `/adminapi/promotion/buyerShow/getBuyerInfo` | 获取买家信息（随机） | `buyerShowManage` | jwt+perm |
| GET | `/adminapi/promotion/buyerShow/info` | 生成虚拟用户信息 | - | jwt |
| GET | `/adminapi/promotion/buyerShow/list` | 买家秀列表 | `buyerShowManage` | jwt+perm |
| POST | `/adminapi/promotion/buyerShow/showOrHide` | 是否公开（公开、隐藏） | `buyerShowUpdateManage` | jwt+perm |
| POST | `/adminapi/promotion/buyerShow/update` | 更新买家秀 | `buyerShowUpdateManage` | jwt+perm |
| POST | `/adminapi/promotion/buyerShow/updateField` | 更新字段 | `buyerShowUpdateManage` | jwt+perm |

## adminapi / 买家秀评论管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/buyerShow/commentInfo/audit` | 审核（通过、拒绝） | `buyerShowUpdateManage` | jwt+perm |
| POST | `/adminapi/promotion/buyerShow/commentInfo/batch` | 批量操作（审核操作类型为audit） | `buyerShowUpdateManage` | jwt+perm |
| POST | `/adminapi/promotion/buyerShow/commentInfo/isTop` | 是否置顶（置顶、取消置顶） | `buyerShowUpdateManage` | jwt+perm |
| GET | `/adminapi/promotion/buyerShow/commentInfo/list` | 获取相关评论列表 | `buyerShowManage` | jwt+perm |
| POST | `/adminapi/promotion/buyerShow/commentInfo/showOrHide` | 是否公开（公开、隐藏） | `buyerShowUpdateManage` | jwt+perm |

## adminapi / 交易日志

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/finance/payLog/batch` | 批量操作 | `payLogBatchManage` | jwt+perm |
| POST | `/adminapi/finance/payLog/create` | 创建 | - | jwt |
| POST | `/adminapi/finance/payLog/del` | 删除 | `payLogBatchManage` | jwt+perm |
| GET | `/adminapi/finance/payLog/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/finance/payLog/list` | 获取列表 | - | jwt |
| POST | `/adminapi/finance/payLog/update` | 更新 | - | jwt |
| POST | `/adminapi/finance/payLog/updateField` | 更新字段 | - | jwt |

## adminapi / 代客下单

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/proxyShopping/user/detail` | 会员详情 | `proxyShoppingManage` | jwt+perm |
| GET | `/adminapi/proxyShopping/user/list` | 会员列表 | `proxyShoppingManage` | jwt+perm |

## adminapi / 代客下单优惠卷

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/proxyShopping/coupon/claim` | 领取优惠券 | `proxyShoppingManage` | jwt+perm |

## adminapi / 代客下单商品管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/proxyShopping/product/getProduct` | 商品列表 | `proxyShoppingManage` | jwt+perm |
| GET | `/adminapi/proxyShopping/product/getProductAvailability` | 商品可用数据 | `proxyShoppingManage` | jwt+perm |
| GET | `/adminapi/proxyShopping/product/productDetail` | 商品详情 | `proxyShoppingManage` | jwt+perm |

## adminapi / 代客下单地址管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/proxyShopping/address/create` | 收货地址列表 | `proxyShoppingManage` | jwt+perm |
| GET | `/adminapi/proxyShopping/address/getAllRegionTree` | 获取所有地区 | `proxyShoppingManage` | jwt+perm |
| GET | `/adminapi/proxyShopping/address/getRegionPath` | 根据regionId获取完整地区路径 | `proxyShoppingManage` | jwt+perm |
| POST | `/adminapi/proxyShopping/address/list` | 收货地址列表 | `proxyShoppingManage` | jwt+perm |
| POST | `/adminapi/proxyShopping/address/setSelected` | 设置选中状态 | `proxyShoppingManage` | jwt+perm |
| POST | `/adminapi/proxyShopping/address/update` | 修改收货地址 | `proxyShoppingManage` | jwt+perm |

## adminapi / 代客下单自提联系人管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/proxyShopping/pickupContact/create` | 创建自提联系人 | `proxyShoppingManage` | jwt+perm |
| POST | `/adminapi/proxyShopping/pickupContact/del` | 删除自提联系人 | `proxyShoppingManage` | jwt+perm |
| GET | `/adminapi/proxyShopping/pickupContact/detail` | 自提联系人详情 | `proxyShoppingManage` | jwt+perm |
| POST | `/adminapi/proxyShopping/pickupContact/list` | 自提联系人分页列表 | `proxyShoppingManage` | jwt+perm |
| GET | `/adminapi/proxyShopping/pickupContact/listByUserId` | 根据用户ID获取自提联系人列表 | `proxyShoppingManage` | jwt+perm |
| POST | `/adminapi/proxyShopping/pickupContact/update` | 修改自提联系人 | `proxyShoppingManage` | jwt+perm |

## adminapi / 代客下单订单结算

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/proxyShopping/order/check/changePrice` | 改价 | `proxyShoppingManage` | jwt+perm |
| POST | `/adminapi/proxyShopping/order/check/changePriceShow` | 改价展示 | `proxyShoppingManage` | jwt+perm |
| GET | `/adminapi/proxyShopping/order/check/getAvailablePaymentType` | 获取可用支付方式 | `proxyShoppingManage` | jwt+perm |
| POST | `/adminapi/proxyShopping/order/check/index` | 获取结算页信息 | `proxyShoppingManage` | jwt+perm |
| GET | `/adminapi/proxyShopping/order/check/pickupList` | 获取自提点列表 | `proxyShoppingManage` | jwt+perm |
| POST | `/adminapi/proxyShopping/order/check/submit` | 提交订单 | `proxyShoppingManage` | jwt+perm |
| POST | `/adminapi/proxyShopping/order/check/update` | 更新结算信息 | `proxyShoppingManage` | jwt+perm |
| POST | `/adminapi/proxyShopping/order/check/updateCoupon` | 更新优惠券 | `proxyShoppingManage` | jwt+perm |

## adminapi / 代客下单购物车管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/proxyShopping/cart/addToCart` | 添加商品到购物车 | `proxyShoppingManage` | jwt+perm |
| POST | `/adminapi/proxyShopping/cart/clearCart` | 清空购物车 | `proxyShoppingManage` | jwt+perm |
| GET | `/adminapi/proxyShopping/cart/getCount` | 获取购物车商品数量 | `proxyShoppingManage` | jwt+perm |
| POST | `/adminapi/proxyShopping/cart/list` | 获取购物车列表 | `proxyShoppingManage` | jwt+perm |
| POST | `/adminapi/proxyShopping/cart/removeItem` | 删除购物车商品 | `proxyShoppingManage` | jwt+perm |
| POST | `/adminapi/proxyShopping/cart/updateCheck` | 更新选中状态 | `proxyShoppingManage` | jwt+perm |
| POST | `/adminapi/proxyShopping/cart/updateItem` | 更新购物车商品 | `proxyShoppingManage` | jwt+perm |

## adminapi / 代客下单配送方式

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/proxyShopping/delivery/getDeliveryOptions` | 获取代客下单购物车商品配送方式 | `proxyShoppingManage` | jwt+perm |
| GET | `/adminapi/proxyShopping/delivery/getStoreShippingType` | 获取店铺配送方式 | `proxyShoppingManage` | jwt+perm |

## adminapi / 优惠券

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/coupon/batch` | 批量操作 | `promotionCouponModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/coupon/config` | 营销配置项 | `couponManage` | jwt+perm |
| POST | `/adminapi/promotion/coupon/create` | 创建 | `promotionCouponModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/coupon/del` | 删除 | `promotionCouponModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/coupon/detail` | 获取详情 | `couponManage` | jwt+perm |
| GET | `/adminapi/promotion/coupon/issue/batch` | 优惠券发放批次列表 | `couponManage` | jwt+perm |
| GET | `/adminapi/promotion/coupon/issue/log` | 优惠券发放日志列表 | `couponManage` | jwt+perm |
| POST | `/adminapi/promotion/coupon/issue/preview` | 手动发放优惠券预览 | `couponManage` | jwt+perm |
| POST | `/adminapi/promotion/coupon/issue/send` | 手动发放优惠券 | `promotionCouponModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/coupon/issue/users` | 手动发放会员搜索 | `couponManage` | jwt+perm |
| GET | `/adminapi/promotion/coupon/list` | 获取列表 | `decorate`, `couponManage` | jwt+perm |
| GET | `/adminapi/promotion/coupon/listByIds` | 按 ids 批量获取优惠券 | `decorate`, `couponManage` | jwt+perm |
| POST | `/adminapi/promotion/coupon/update` | 更新 | `promotionCouponModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/coupon/updateField` | 更新字段 | `promotionCouponModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/coupon/validList` | 查询平台有效优惠券列表 | `promotionCouponModifyManage` | jwt+perm |

## adminapi / 会员人群圈选

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/user/memberAudience/describe` | 人群条件文字描述 | - | jwt |
| POST | `/adminapi/user/memberAudience/estimate` | 预估人群命中人数 | - | jwt |

## adminapi / 会员企业认证

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/user/userCompany/audit` | 审核 | `userCertificationManage` | jwt+perm |
| POST | `/adminapi/user/userCompany/del` | 删除 | `userCertificationManage` | jwt+perm |
| GET | `/adminapi/user/userCompany/detail` | 获取详情 | `userCertificationManage` | jwt+perm |
| GET | `/adminapi/user/userCompany/list` | 获取列表 | `userCertificationManage` | jwt+perm |

## adminapi / 会员标签管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/user/memberTag/create` | 新增标签 | `memberTagUpdateManage` | jwt+perm |
| POST | `/adminapi/user/memberTag/del` | 删除标签 | `memberTagUpdateManage` | jwt+perm |
| GET | `/adminapi/user/memberTag/listByGroup` | 查询组下标签列表 | `memberTagManage` | jwt+perm |
| POST | `/adminapi/user/memberTag/update` | 更新标签 | `memberTagUpdateManage` | jwt+perm |

## adminapi / 会员标签组管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/user/memberTagGroup/cancelTop` | 取消置顶标签组 | `memberTagUpdateManage` | jwt+perm |
| POST | `/adminapi/user/memberTagGroup/create` | 新增标签组 | `memberTagUpdateManage` | jwt+perm |
| POST | `/adminapi/user/memberTagGroup/del` | 删除标签组 | `memberTagUpdateManage` | jwt+perm |
| GET | `/adminapi/user/memberTagGroup/detail` | 标签组详情 | `memberTagManage` | jwt+perm |
| GET | `/adminapi/user/memberTagGroup/list` | 标签组分页列表 | `memberTagManage` | jwt+perm |
| POST | `/adminapi/user/memberTagGroup/setTop` | 置顶标签组 | `memberTagUpdateManage` | jwt+perm |
| POST | `/adminapi/user/memberTagGroup/update` | 更新标签组 | `memberTagUpdateManage` | jwt+perm |

## adminapi / 会员消息管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/user/userMessageLog/batch` | 批量操作 | `userMessageLogModifyManage` | jwt+perm |
| POST | `/adminapi/user/userMessageLog/create` | 创建会员消息 | `userMessageLogModifyManage` | jwt+perm |
| POST | `/adminapi/user/userMessageLog/del` | 删除会员消息 | `userMessageLogModifyManage` | jwt+perm |
| GET | `/adminapi/user/userMessageLog/list` | 会员消息列表 | `messageLogManage` | jwt+perm |
| POST | `/adminapi/user/userMessageLog/recall` | 撤回 | `userMessageLogModifyManage` | jwt+perm |

## adminapi / 会员留言

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/user/feedback/batch` | 批量操作 | `feedbackModifyManage` | jwt+perm |
| POST | `/adminapi/user/feedback/del` | 删除会员留言 | `feedbackModifyManage` | jwt+perm |
| GET | `/adminapi/user/feedback/detail` | 会员留言详情 | `feedbackManage` | jwt+perm |
| GET | `/adminapi/user/feedback/list` | 会员留言列表 | `feedbackManage` | jwt+perm |
| POST | `/adminapi/user/feedback/update` | 更新会员留言 | `feedbackModifyManage` | jwt+perm |

## adminapi / 会员积分日志

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/user/userPointsLog/getPoints` | 获取会员积分 | `integralLogManage` | jwt+perm |
| GET | `/adminapi/user/userPointsLog/list` | 会员积分列表 | `integralLogManage` | jwt+perm |

## adminapi / 会员等级

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/user/userRank/batch` | 批量操作 | `userRankModifyManage` | jwt+perm |
| POST | `/adminapi/user/userRank/create` | 创建会员等级 | `userRankModifyManage` | jwt+perm |
| POST | `/adminapi/user/userRank/del` | 删除会员等级 | `userRankModifyManage` | jwt+perm |
| GET | `/adminapi/user/userRank/detail` | 会员等级详情 | `userLevelProManage`, `levelManageManage`, `userRankManage` | jwt+perm |
| GET | `/adminapi/user/userRank/list` | 获取会员等级列表（非Pro） | `userLevelProManage`, `levelManageManage`, `userRankManage` | jwt+perm |
| GET | `/adminapi/user/userRank/listByPro` | 获取会员等级列表 | `userLevelProManage`, `levelManageManage`, `userRankManage` | jwt+perm |
| POST | `/adminapi/user/userRank/update` | 更新会员等级 | `userRankModifyManage` | jwt+perm |
| POST | `/adminapi/user/userRank/updateField` | 更新会员等级字段 | `userRankModifyManage` | jwt+perm |

## adminapi / 会员管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/user/user/batch` | 批量操作 | `userBatchManage` | jwt+perm |
| POST | `/adminapi/user/user/batchImport` | 批量导入商品 | `userManage` | jwt+perm |
| POST | `/adminapi/user/user/create` | 创建会员 | `userCreateManage` | jwt+perm |
| POST | `/adminapi/user/user/del` | 删除会员 | `userDelManage` | jwt+perm |
| GET | `/adminapi/user/user/detail` | 会员详情 | `userManage` | jwt+perm |
| GET | `/adminapi/user/user/downloadUserImportTemplate` | 下载导入用户模版 | `userManage` | jwt+perm |
| POST | `/adminapi/user/user/fundManagement` | 会员资金管理 | `fundManagementManage` | jwt+perm |
| POST | `/adminapi/user/user/getImportErrorData` | 获取导入失败的数据 | `userManage` | anonymous |
| GET | `/adminapi/user/user/list` | 会员列表 | `userManage` | jwt+perm |
| POST | `/adminapi/user/user/logout` | 管理员退出登录 | `userManage` | anonymous |
| POST | `/adminapi/user/user/search` | 会员搜索 | `userManage` | jwt+perm |
| POST | `/adminapi/user/user/setAdminNote` | 设置会员备注和标记 | `userModifyManage` | jwt+perm |
| POST | `/adminapi/user/user/update` | 更新会员 | `userModifyManage` | jwt+perm |
| POST | `/adminapi/user/user/updateField` | 更新会员字段 | `userModifyFieldManage` | jwt+perm |
| GET | `/adminapi/user/user/userFundDetail` | 资金明细 | `userManage` | jwt+perm |

## adminapi / 供应商端保证金

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/deposit/vendor/frozenDetailList` | 冻结明细(平台扣款记录) | - | jwt |
| GET | `/adminapi/deposit/vendor/logList` | 供应商端交易流水 | - | jwt |
| GET | `/adminapi/deposit/vendor/overview` | 保证金总览 | - | jwt |
| GET | `/adminapi/deposit/vendor/protocol` | 保证金协议 | - | jwt |
| GET | `/adminapi/deposit/vendor/warning` | 首页保证金预警横幅 | - | jwt |

## adminapi / 保证金收款账户

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/deposit/payeeAccount/create` | 创建收款账户 | `depositPayeeAccountUpdate` | jwt+perm |
| POST | `/adminapi/deposit/payeeAccount/del` | 删除收款账户 | `depositPayeeAccountUpdate` | jwt+perm |
| GET | `/adminapi/deposit/payeeAccount/list` | 获取收款账户分页列表 | `depositPayeeAccountManage` | jwt+perm |
| POST | `/adminapi/deposit/payeeAccount/update` | 更新收款账户 | `depositPayeeAccountUpdate` | jwt+perm |

## adminapi / 保证金规则

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/deposit/rule/create` | 创建规则 | `depositRuleUpdate` | jwt+perm |
| POST | `/adminapi/deposit/rule/del` | 删除规则 | `depositRuleUpdate` | jwt+perm |
| GET | `/adminapi/deposit/rule/detail` | 获取规则详情 | `depositRuleManage` | jwt+perm |
| GET | `/adminapi/deposit/rule/list` | 获取规则列表 | `depositRuleManage` | jwt+perm |
| POST | `/adminapi/deposit/rule/status` | 启用/停用规则 | `depositRuleUpdate` | jwt+perm |
| POST | `/adminapi/deposit/rule/update` | 更新规则 | `depositRuleUpdate` | jwt+perm |

## adminapi / 保证金设置

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/deposit/setting/detail` | 获取保证金设置 | `depositSettingManage` | jwt+perm |
| POST | `/adminapi/deposit/setting/save` | 保存保证金设置 | `depositSettingSave` | jwt+perm |

## adminapi / 充值余额

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/rechargeSetting/batch` | 批量操作 | `rechargeSettingModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/rechargeSetting/create` | 创建 | `rechargeSettingModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/rechargeSetting/del` | 删除 | `rechargeSettingModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/rechargeSetting/detail` | 获取详情 | `rechargeSettingManage` | jwt+perm |
| GET | `/adminapi/promotion/rechargeSetting/list` | 获取列表 | `rechargeSettingManage` | jwt+perm |
| POST | `/adminapi/promotion/rechargeSetting/update` | 更新 | `rechargeSettingModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/rechargeSetting/updateField` | 更新字段 | `rechargeSettingModifyManage` | jwt+perm |

## adminapi / 充值记录

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/finance/userRechargeOrder/batch` | 批量操作 | `userRechargeOrderBatchManage` | jwt+perm |
| POST | `/adminapi/finance/userRechargeOrder/create` | 创建 | `userRechargeOrderUpdateManage` | jwt+perm |
| POST | `/adminapi/finance/userRechargeOrder/del` | 删除 | `userRechargeOrderDelManage` | jwt+perm |
| GET | `/adminapi/finance/userRechargeOrder/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/finance/userRechargeOrder/list` | 获取列表 | - | jwt |
| POST | `/adminapi/finance/userRechargeOrder/update` | 更新 | `userRechargeOrderUpdateManage` | jwt+perm |
| POST | `/adminapi/finance/userRechargeOrder/updateField` | 更新字段 | - | jwt |

## adminapi / 入驻申请

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/merchant/apply/audit` | 审核操作 | `merchantApplyModifyManage` | jwt+perm |
| POST | `/adminapi/merchant/apply/batch` | 批量操作 | `merchantApplyModifyManage` | jwt+perm |
| GET | `/adminapi/merchant/apply/config` | 获取状态参数 | - | jwt |
| POST | `/adminapi/merchant/apply/del` | 删除 | `merchantApplyModifyManage` | jwt+perm |
| GET | `/adminapi/merchant/apply/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/merchant/apply/list` | 获取列表 | - | jwt |
| POST | `/adminapi/merchant/apply/update` | 更新 | `merchantApplyModifyManage` | jwt+perm |
| POST | `/adminapi/merchant/apply/updateField` | 更新字段 | `merchantApplyModifyManage` | jwt+perm |

## adminapi / 全局搜索

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/search/aggregate` | 综合搜索 | `productManage` | jwt+perm |
| GET | `/adminapi/search/product` | 商品搜索 | `productManage` | jwt+perm |

## adminapi / 公众号关键字回复

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/wechat/keywordRule/del` | 批量删除规则 | `wechatKeywordReplyManage` | jwt+perm |
| GET | `/adminapi/wechat/keywordRule/detail` | 规则详情 | `wechatKeywordReply` | jwt+perm |
| GET | `/adminapi/wechat/keywordRule/list` | 规则列表 | `wechatKeywordReply` | jwt+perm |
| POST | `/adminapi/wechat/keywordRule/save` | 新增或编辑规则 | `wechatKeywordReplyManage` | jwt+perm |
| POST | `/adminapi/wechat/keywordRule/setEnabled` | 批量启停规则 | `wechatKeywordReplyManage` | jwt+perm |

## adminapi / 公众号关键字回复-客服能力探测

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/wechat/keywordRule/capability` | 探测客服消息接口是否可用 | `wechatKeywordReply` | jwt+perm |

## adminapi / 公众号自动回复

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/wechat/autoReply/default` | 获取默认自动回复 | `wechatAutoReply` | jwt+perm |
| POST | `/adminapi/wechat/autoReply/default/save` | 保存默认自动回复 | `wechatAutoReplyManage` | jwt+perm |
| GET | `/adminapi/wechat/autoReply/loginSuccess` | 获取扫码登录成功回复 | `wechatAutoReply` | anonymous |
| POST | `/adminapi/wechat/autoReply/loginSuccess/save` | 保存扫码登录成功回复 | `wechatAutoReplyManage` | anonymous |
| GET | `/adminapi/wechat/autoReply/subscribe` | 获取关注后自动回复 | `wechatAutoReply` | jwt+perm |
| POST | `/adminapi/wechat/autoReply/subscribe/save` | 保存关注后自动回复 | `wechatAutoReplyManage` | jwt+perm |

## adminapi / 分组标签

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/salesman/group/create` | 创建 | `salesmanGroupModifyManage` | jwt+perm |
| POST | `/adminapi/salesman/group/del` | 删除 | `salesmanGroupModifyManage` | jwt+perm |
| GET | `/adminapi/salesman/group/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/salesman/group/list` | 获取列表 | - | jwt |
| POST | `/adminapi/salesman/group/update` | 更新 | `salesmanGroupModifyManage` | jwt+perm |

## adminapi / 分账结算

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/finance/profitSharing/list` | 分账单分页列表 | - | jwt |
| POST | `/adminapi/finance/profitSharing/manualFinish` | 手动完结分账单 | `profitSharingSettlementFinish` | jwt+perm |
| POST | `/adminapi/finance/profitSharing/submitBatch` | 批量发起分账 | `profitSharingSettlementSubmit` | jwt+perm |

## adminapi / 分销业绩订单结算

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/salesman/order/batch` | 批量操作 | `salesmanOrderBatch` | jwt+perm |
| GET | `/adminapi/salesman/order/list` | 获取列表 | - | jwt |
| POST | `/adminapi/salesman/order/settlement` | 人工结算 | `salesmanSettlementManage` | jwt+perm |

## adminapi / 分销内容管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/salesman/content/batch` | 批量操作 | `salesmanContentModifyManage` | jwt+perm |
| GET | `/adminapi/salesman/content/config` | 获取配置 | - | jwt |
| POST | `/adminapi/salesman/content/create` | 创建 | `salesmanContentModifyManage` | jwt+perm |
| POST | `/adminapi/salesman/content/del` | 删除 | `salesmanContentModifyManage` | jwt+perm |
| GET | `/adminapi/salesman/content/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/salesman/content/list` | 获取列表 | - | jwt |
| POST | `/adminapi/salesman/content/update` | 更新 | `salesmanContentModifyManage` | jwt+perm |
| POST | `/adminapi/salesman/content/updateField` | 更新字段 | `salesmanContentModifyManage` | jwt+perm |

## adminapi / 分销员列表控制器

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/salesman/salesman/commissionDetails` | 分销员佣金详情 | - | jwt |
| POST | `/adminapi/salesman/salesman/create` | 创建 | `salesmanModifyManage` | jwt+perm |
| GET | `/adminapi/salesman/salesman/customerList` | 客户数量接口 | - | jwt |
| GET | `/adminapi/salesman/salesman/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/salesman/salesman/list` | 获取列表 | - | jwt |
| GET | `/adminapi/salesman/salesman/ranking` | 分销员排行 | - | jwt |
| GET | `/adminapi/salesman/salesman/salesmanList` | 分销员下拉列表 | - | jwt |
| GET | `/adminapi/salesman/salesman/statisticalDetails` | 分销员明细 | - | jwt |
| POST | `/adminapi/salesman/salesman/update` | 更新 | `salesmanModifyManage` | jwt+perm |

## adminapi / 分销商品

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/salesman/product/analysis` | 商品成交分析 | - | jwt |
| GET | `/adminapi/salesman/product/config` | 配置 | - | jwt |
| POST | `/adminapi/salesman/product/create` | 创建 | `salesmanProductModifyManage` | jwt+perm |
| GET | `/adminapi/salesman/product/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/salesman/product/list` | 获取列表 | - | jwt |
| POST | `/adminapi/salesman/product/update` | 更新 | `salesmanProductModifyManage` | jwt+perm |

## adminapi / 分销概览控制器

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/salesman/overview/coreSummary` | 获取详情 | - | jwt |
| GET | `/adminapi/salesman/overview/coreTrend` | 获取列表 | - | jwt |

## adminapi / 分销模式设置

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/salesman/config/detail` | 获取详情 | - | jwt |
| POST | `/adminapi/salesman/config/save` | 更新 | `salesmanConfigModifyManage` | jwt+perm |

## adminapi / 区号管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/setting/areaCode/batch` | 批量操作 | `areaCodeModifyManage` | jwt+perm |
| POST | `/adminapi/setting/areaCode/create` | 创建 | `areaCodeModifyManage` | jwt+perm |
| POST | `/adminapi/setting/areaCode/del` | 删除 | `areaCodeModifyManage` | jwt+perm |
| GET | `/adminapi/setting/areaCode/detail` | 获取详情 | `areaCodeManage` | jwt+perm |
| GET | `/adminapi/setting/areaCode/list` | 获取列表 | `areaCodeManage` | jwt+perm |
| POST | `/adminapi/setting/areaCode/update` | 更新 | `areaCodeModifyManage` | jwt+perm |
| POST | `/adminapi/setting/areaCode/updateField` | 更新字段 | `areaCodeModifyManage` | jwt+perm |

## adminapi / 友情链接

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/setting/friendLinks/batch` | 批量操作 | `friendLinksBatchManage` | jwt+perm |
| POST | `/adminapi/setting/friendLinks/create` | 创建 | `friendLinksUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/friendLinks/del` | 删除 | `friendLinksDelManage` | jwt+perm |
| GET | `/adminapi/setting/friendLinks/detail` | 获取详情 | `friendLinksManage` | jwt+perm |
| GET | `/adminapi/setting/friendLinks/list` | 获取列表 | `friendLinksManage` | jwt+perm |
| POST | `/adminapi/setting/friendLinks/update` | 更新 | `friendLinksUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/friendLinks/updateField` | 更新字段 | `friendLinksUpdateManage` | jwt+perm |

## adminapi / 发票申请

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/finance/orderInvoice/batch` | 批量操作 | `orderInvoiceBatchManage` | jwt+perm |
| POST | `/adminapi/finance/orderInvoice/create` | 创建 | `orderInvoiceUpdateManage` | jwt+perm |
| POST | `/adminapi/finance/orderInvoice/del` | 删除 | `orderInvoiceDelManage` | jwt+perm |
| GET | `/adminapi/finance/orderInvoice/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/finance/orderInvoice/list` | 获取列表 | - | jwt |
| POST | `/adminapi/finance/orderInvoice/update` | 更新 | `orderInvoiceUpdateManage` | jwt+perm |
| POST | `/adminapi/finance/orderInvoice/updateField` | 更新字段 | `orderInvoiceUpdateManage` | jwt+perm |

## adminapi / 发票资质

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/finance/userInvoice/batch` | 批量操作 | `userInvoiceBatchManage` | jwt+perm |
| POST | `/adminapi/finance/userInvoice/create` | 创建 | - | jwt |
| POST | `/adminapi/finance/userInvoice/del` | 删除 | `userInvoiceDelManage` | jwt+perm |
| GET | `/adminapi/finance/userInvoice/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/finance/userInvoice/list` | 获取列表 | - | jwt |
| POST | `/adminapi/finance/userInvoice/update` | 更新 | `userInvoiceUpdateManage` | jwt+perm |
| POST | `/adminapi/finance/userInvoice/updateField` | 更新字段 | - | jwt |

## adminapi / 品牌

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/product/brand/audit` | 品牌审核 | `brandManage`, `vendorProductManage`, `productManage` | jwt+perm |
| GET | `/adminapi/product/brand/auditList` | 品牌审核列表 | `brandManage`, `vendorProductManage`, `productManage` | jwt+perm |
| GET | `/adminapi/product/brand/auditWaitNum` | 获取待审核数量 | `brandManage`, `vendorProductManage`, `productManage` | jwt+perm |
| POST | `/adminapi/product/brand/batch` | 批量操作 | `brandModifyManage` | jwt+perm |
| POST | `/adminapi/product/brand/create` | 创建品牌 | `brandModifyManage` | jwt+perm |
| POST | `/adminapi/product/brand/del` | 删除品牌 | `brandModifyManage` | jwt+perm |
| GET | `/adminapi/product/brand/detail` | 品牌详情 | `brandManage`, `vendorProductManage`, `productManage` | jwt+perm |
| GET | `/adminapi/product/brand/list` | 品牌列表 | `decorate`, `brandManage`, `vendorProductManage`, `productManage` | jwt+perm |
| GET | `/adminapi/product/brand/search` | 品牌搜索 | `decorate`, `brandManage`, `vendorProductManage`, `productManage` | jwt+perm |
| POST | `/adminapi/product/brand/update` | 更新品牌 | `brandModifyManage` | jwt+perm |
| POST | `/adminapi/product/brand/updateField` | 更新品牌字段 | `brandModifyManage` | jwt+perm |
| POST | `/adminapi/product/brand/updateFirstWord` | 批量更新首字母 | `brandManage`, `vendorProductManage`, `productManage` | jwt+perm |

## adminapi / 品类费率规则

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/finance/categoryFeeRule/checkConflict` | 冲突预检 | `categoryFeeRuleManage` | jwt+perm |
| POST | `/adminapi/finance/categoryFeeRule/del` | 删除 | `categoryFeeRuleUpdate` | jwt+perm |
| GET | `/adminapi/finance/categoryFeeRule/detail` | 详情 | `categoryFeeRuleManage` | jwt+perm |
| POST | `/adminapi/finance/categoryFeeRule/list` | 分页列表 | `categoryFeeRuleManage` | jwt+perm |
| POST | `/adminapi/finance/categoryFeeRule/save` | 新建 | `categoryFeeRuleUpdate` | jwt+perm |
| POST | `/adminapi/finance/categoryFeeRule/update` | 编辑 | `categoryFeeRuleUpdate` | jwt+perm |
| POST | `/adminapi/finance/categoryFeeRule/updateStatus` | 启用/禁用 | `categoryFeeRuleUpdate` | jwt+perm |

## adminapi / 售后管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/order/aftersales/applyType` | 申请类型 | - | jwt |
| POST | `/adminapi/order/aftersales/complete` | 售后完结 | `aftersalesModifyManage` | jwt+perm |
| POST | `/adminapi/order/aftersales/create` | 创建 | - | jwt |
| GET | `/adminapi/order/aftersales/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/order/aftersales/list` | 获取列表 | - | jwt |
| POST | `/adminapi/order/aftersales/receive` | 售后确认收货接口 | `aftersalesModifyManage` | jwt+perm |
| POST | `/adminapi/order/aftersales/record` | 提交售后反馈记录 | `aftersalesModifyManage` | jwt+perm |
| POST | `/adminapi/order/aftersales/rejectReceive` | 售后拒收接口 | `aftersalesModifyManage` | jwt+perm |
| GET | `/adminapi/order/aftersales/returnGoodsStatus` | 退换货状态 | - | jwt |
| POST | `/adminapi/order/aftersales/update` | 更新 | `aftersalesModifyManage` | jwt+perm |

## adminapi / 商品

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/product/product/audit` | 产品审核 | `productModifyManage` | jwt+perm |
| POST | `/adminapi/product/product/auditAgain` | 产品再次审核 | `productModifyManage` | jwt+perm |
| POST | `/adminapi/product/product/batch` | 批量操作 | `productModifyManage` | jwt+perm |
| GET | `/adminapi/product/product/config` | 商品配置 | `productManage` | jwt+perm |
| POST | `/adminapi/product/product/copy` | 产品复制 | `productModifyManage` | jwt+perm |
| POST | `/adminapi/product/product/create` | 创建商品 | `productModifyManage` | jwt+perm |
| GET | `/adminapi/product/product/descImages` | 商品描述图片列表（供配置描述图本地化 dataType=7） | `productManage` | jwt+perm |
| GET | `/adminapi/product/product/detail` | 商品详情 | `productManage` | jwt+perm |
| POST | `/adminapi/product/product/getParticiple` | 更新关键词 | `productManage` | jwt+perm |
| GET | `/adminapi/product/product/getPromoteData` | 获取推广数据 | `productManage` | jwt+perm |
| GET | `/adminapi/product/product/getWaitingCheckedCount` | 待审核商品数量 | `productManage` | jwt+perm |
| POST | `/adminapi/product/product/initAllToEs` | 初始化所有商品到ES | `productManage` | jwt+perm |
| GET | `/adminapi/product/product/list` | 商品列表 | `decorate`, `productManage` | jwt+perm |
| GET | `/adminapi/product/product/post` | 商品推广（包含H5、PC、小程序） | `productManage` | jwt+perm |
| POST | `/adminapi/product/product/recycle` | 产品回收 | `productModifyManage` | jwt+perm |
| POST | `/adminapi/product/product/saveShareInfo` | 保存商品分享信息 | `productManage` | jwt+perm |
| GET | `/adminapi/product/product/selectProductList` | sku商品列表 | `productManage` | jwt+perm |
| GET | `/adminapi/product/product/shareInfo` | 获取商品分享信息 | `productManage` | jwt+perm |
| GET | `/adminapi/product/product/shippingTplList` | 运费模板 | `productManage` | jwt+perm |
| GET | `/adminapi/product/product/skuList` | 分页获取商品SKU列表（对应商品详情 productList，默认每页50条，支持关键字/库存过滤与排序） | `productManage` | jwt+perm |
| POST | `/adminapi/product/product/update` | 更新商品 | `productModifyManage` | jwt+perm |
| POST | `/adminapi/product/product/updateField` | 更新字段 | `productModifyManage` | jwt+perm |
| GET | `/adminapi/product/productBatch/downloadTemplate` | 下载商品导入模版 | `productBatchAddManage` | jwt+perm |
| GET | `/adminapi/product/productBatch/productBatchDeal` | 商品导出Excel | `productBatchAddManage` | jwt+perm |
| POST | `/adminapi/product/productBatch/productBatchEdit` | 商品批量修改 | `productBatchEditModifyManage` | jwt+perm |
| POST | `/adminapi/product/productBatch/productBatchModify` | 商品导入 | `productBatchModifyManage` | jwt+perm |
| GET | `/adminapi/product/productBatch/productService/list` | 商品服务列表 | `productBatchAddManage` | jwt+perm |

## adminapi / 商品分类

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/product/category/batch` | 批量操作商品分类 | `productCategoryModifyManage` | jwt+perm |
| POST | `/adminapi/product/category/create` | 创建商品分类 | `productCategoryModifyManage` | jwt+perm |
| POST | `/adminapi/product/category/del` | 删除商品分类 | `productCategoryModifyManage` | jwt+perm |
| GET | `/adminapi/product/category/detail` | 商品分类详情 | `categoryManage`, `productManage`, `vendorProductManage` | jwt+perm |
| GET | `/adminapi/product/category/getAllCategory` | 获取所有商品分类 | `decorate`, `categoryManage`, `productManage`, `vendorProductManage` | jwt+perm |
| GET | `/adminapi/product/category/getParentName` | 获取列表 | `categoryManage`, `productManage`, `vendorProductManage` | jwt+perm |
| GET | `/adminapi/product/category/list` | 商品分类列表 | `categoryManage`, `productManage`, `vendorProductManage` | jwt+perm |
| POST | `/adminapi/product/category/moveCat` | 移动商品分类 | `categoryManage`, `productManage`, `vendorProductManage` | jwt+perm |
| POST | `/adminapi/product/category/update` | 更新商品分类 | `productCategoryModifyManage` | jwt+perm |
| POST | `/adminapi/product/category/updateField` | 商品分类字段更新 | `productCategoryModifyManage` | jwt+perm |

## adminapi / 商品分组

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/product/productGroup/batch` | 批量操作 | `productGroupModifyManage` | jwt+perm |
| GET | `/adminapi/product/productGroup/clientList` | 获取列表 | `productGroupManage` | jwt+perm |
| POST | `/adminapi/product/productGroup/create` | 创建 | `productGroupModifyManage` | jwt+perm |
| POST | `/adminapi/product/productGroup/del` | 删除 | `productGroupModifyManage` | jwt+perm |
| GET | `/adminapi/product/productGroup/detail` | 获取详情 | `productGroupManage` | jwt+perm |
| GET | `/adminapi/product/productGroup/list` | 获取列表 | `productGroupManage` | jwt+perm |
| POST | `/adminapi/product/productGroup/update` | 更新 | `productGroupModifyManage` | jwt+perm |
| POST | `/adminapi/product/productGroup/updateField` | 更新字段 | `productGroupModifyManage` | jwt+perm |

## adminapi / 商品属性模板

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/product/productAttributesTpl/batch` | 批量操作商品属性模板 | `productAttributesTplModifyManage` | jwt+perm |
| POST | `/adminapi/product/productAttributesTpl/create` | 创建商品属性模板 | `productAttributesTplModifyManage` | jwt+perm |
| POST | `/adminapi/product/productAttributesTpl/del` | 删除商品属性模板 | `productAttributesTplModifyManage` | jwt+perm |
| GET | `/adminapi/product/productAttributesTpl/detail` | 商品属性模板详情 | `productAttributesTplManage` | jwt+perm |
| GET | `/adminapi/product/productAttributesTpl/list` | 商品属性模板列表 | `productAttributesTplManage` | jwt+perm |
| GET | `/adminapi/product/productAttributesTpl/listByCategory` | 按类目获取商品属性模板 | `productAttributesTplManage`, `productManage` | jwt+perm |
| POST | `/adminapi/product/productAttributesTpl/update` | 更新商品属性模板 | `productAttributesTplModifyManage` | jwt+perm |
| POST | `/adminapi/product/productAttributesTpl/updateField` | 更新商品属性模板字段 | `productAttributesTplModifyManage` | jwt+perm |

## adminapi / 商品库存管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/product/stock/adjustDetail` | 商品调整记录 | - | jwt |
| POST | `/adminapi/product/stock/auditAllocate` | 调拨审核 | `productStockAllocateUpdateManage` | jwt+perm |
| POST | `/adminapi/product/stock/batchImport` | 批量导入商品 | - | jwt |
| POST | `/adminapi/product/stock/batchImportAllocate` | 批量导入调拨商品 | - | jwt |
| POST | `/adminapi/product/stock/batchImportAllocateProduct` | 批量导入调拨商品 | - | jwt |
| POST | `/adminapi/product/stock/cancelAllocate` | 调拨作废 | `productStockAllocateUpdateManage` | jwt+perm |
| POST | `/adminapi/product/stock/createAllocate` | 新建调拨单 | `productStockAllocateUpdateManage` | jwt+perm |
| GET | `/adminapi/product/stock/downloadAllocateTemplate` | 下载调拨商品导入模版 | - | jwt |
| GET | `/adminapi/product/stock/downloadTemplate` | 下载商品导入模版 | - | jwt |
| POST | `/adminapi/product/stock/exportAllocate` | 调拨单导出 | - | jwt |
| GET | `/adminapi/product/stock/getAddByList` | 获取经办人集合 | - | jwt |
| POST | `/adminapi/product/stock/getAllocateProductErrorData` | 获取调拨商品导入失败的数据 | - | anonymous |
| GET | `/adminapi/product/stock/getAllocateProductStockPage` | 获取可调拨商品列表 | - | jwt |
| GET | `/adminapi/product/stock/getAllocateTemplate` | 下载导入调拨单Excel | - | jwt |
| POST | `/adminapi/product/stock/getBatchImportErrorData` | 获取导入调拨商品失败Excel | - | anonymous |
| POST | `/adminapi/product/stock/getImportErrorData` | 获取导入失败的数据 | - | anonymous |
| GET | `/adminapi/product/stock/getOrgList` | 组织集合 | - | jwt |
| GET | `/adminapi/product/stock/getRecordDetail` | 获取出入库详情 | - | jwt |
| GET | `/adminapi/product/stock/page` | 获取库存列表 | - | jwt |
| GET | `/adminapi/product/stock/productStockAllocateOperationDetail` | 库存调拨操作记录详情 | - | jwt |
| GET | `/adminapi/product/stock/productStockAllocateRecordDetail` | 库存调拨记录详情 | - | jwt |
| GET | `/adminapi/product/stock/productStockAllocateRecordPage` | 库存调拨记录 | - | jwt |
| GET | `/adminapi/product/stock/record/page` | 获取出入库列表 | - | jwt |
| POST | `/adminapi/product/stock/stockAdjust` | 商品库存调整 | `productStockAdjustUpdateManage` | jwt+perm |
| GET | `/adminapi/product/stock/stockDetail` | 获取出入库明细 | - | jwt |
| POST | `/adminapi/product/stock/stockIn` | 商品入库 | `productStockInUpdateManage` | jwt+perm |
| POST | `/adminapi/product/stock/stockInAllocate` | 调拨入库 | `productStockAllocateUpdateManage` | jwt+perm |
| POST | `/adminapi/product/stock/stockOut` | 商品出库 | `productStockOutUpdateManage` | jwt+perm |
| POST | `/adminapi/product/stock/stockOutAllocate` | 调拨出库 | `productStockAllocateUpdateManage` | jwt+perm |

## adminapi / 商品服务

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/product/productServices/batch` | 批量操作商品服务 | `productServicesModifyManage` | jwt+perm |
| POST | `/adminapi/product/productServices/create` | 创建商品服务 | `productServicesModifyManage` | jwt+perm |
| POST | `/adminapi/product/productServices/del` | 删除商品服务 | `productServicesModifyManage` | jwt+perm |
| GET | `/adminapi/product/productServices/detail` | 商品服务详情 | `productServicesManage` | jwt+perm |
| GET | `/adminapi/product/productServices/list` | 商品服务列表 | `productServicesManage` | jwt+perm |
| POST | `/adminapi/product/productServices/update` | 更新商品服务 | `productServicesModifyManage` | jwt+perm |
| POST | `/adminapi/product/productServices/updateField` | 商品服务字段修改 | `productServicesModifyManage` | jwt+perm |

## adminapi / 商品样式配置

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/productStyle/addProducts` | 批量添加关联商品 | `promotionStyleModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/productStyle/batch` | 批量操作 | `promotionStyleModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/productStyle/brandList` | 品牌列表（含商品数量） | `promotionStyleManage` | jwt+perm |
| POST | `/adminapi/promotion/productStyle/create` | 创建商品样式 | `promotionStyleModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/productStyle/del` | 删除商品样式 | `promotionStyleModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/productStyle/detail` | 商品样式详情 | `promotionStyleManage` | jwt+perm |
| GET | `/adminapi/promotion/productStyle/list` | 商品样式列表 | `promotionStyleManage` | jwt+perm |
| POST | `/adminapi/promotion/productStyle/removeProducts` | 批量移除关联商品 | `promotionStyleModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/productStyle/update` | 更新商品样式 | `promotionStyleModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/productStyle/updateField` | 更新商品样式字段 | `promotionStyleModifyManage` | jwt+perm |

## adminapi / 商家账户表

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/merchant/shopWithdraw/audit` | 审核提现 | `merchantShopWithdrawModifyManage` | jwt+perm |
| POST | `/adminapi/merchant/shopWithdraw/batch` | 批量操作 | - | jwt |
| GET | `/adminapi/merchant/shopWithdraw/config` | 获取配置 | - | jwt |
| POST | `/adminapi/merchant/shopWithdraw/create` | 创建 | `merchantShopWithdrawModifyManage` | jwt+perm |
| POST | `/adminapi/merchant/shopWithdraw/del` | 删除 | - | jwt |
| GET | `/adminapi/merchant/shopWithdraw/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/merchant/shopWithdraw/dualBalance` | 双余额查询（线下余额 + 三渠道子商户可用余额） | - | jwt |
| GET | `/adminapi/merchant/shopWithdraw/list` | 获取列表 | - | jwt |
| GET | `/adminapi/merchant/shopWithdraw/summaryList` | 店铺提现汇总列表 | - | jwt |
| POST | `/adminapi/merchant/shopWithdraw/update` | 更新 | `merchantShopWithdrawModifyManage` | jwt+perm |
| POST | `/adminapi/merchant/shopWithdraw/updateField` | 更新字段 | - | jwt |
| POST | `/adminapi/merchant/shopWithdraw/uploadPayVoucher` | 上传打款凭证 | `merchantShopWithdrawModifyManage` | jwt+perm |

## adminapi / 商户管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/merchant/merchant/batch` | 批量操作 | `merchantModifyManage` | jwt+perm |
| POST | `/adminapi/merchant/merchant/create` | 创建 | `merchantModifyManage` | jwt+perm |
| POST | `/adminapi/merchant/merchant/del` | 删除 | `merchantModifyManage` | jwt+perm |
| GET | `/adminapi/merchant/merchant/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/merchant/merchant/list` | 获取列表 | - | jwt |
| POST | `/adminapi/merchant/merchant/update` | 更新 | `merchantModifyManage` | jwt+perm |
| POST | `/adminapi/merchant/merchant/updateField` | 更新字段 | `merchantModifyManage` | jwt+perm |
| POST | `/adminapi/merchant/merchant/uploadLicense` | 入驻营业执照专用上传 | `merchantModifyManage` | jwt+perm |

## adminapi / 商户账户

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/merchant/account/batch` | 批量操作 | `accountModifyManage` | jwt+perm |
| GET | `/adminapi/merchant/account/config` | 获取配置 | - | jwt |
| POST | `/adminapi/merchant/account/create` | 创建 | `accountModifyManage` | jwt+perm |
| POST | `/adminapi/merchant/account/del` | 删除 | `accountModifyManage` | jwt+perm |
| GET | `/adminapi/merchant/account/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/merchant/account/list` | 获取列表 | - | jwt |
| POST | `/adminapi/merchant/account/update` | 更新 | `accountModifyManage` | jwt+perm |

## adminapi / 地区管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/setting/region/batch` | 批量操作 | `regionBatchManage` | jwt+perm |
| POST | `/adminapi/setting/region/create` | 新增地区 | `regionUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/region/del` | 删除 | `regionUpdateManage` | jwt+perm |
| GET | `/adminapi/setting/region/detail` | 地区详情 | `regionManage` | jwt+perm |
| GET | `/adminapi/setting/region/getAllRegionTree` | 获取所有地区 | `regionManage` | jwt+perm |
| GET | `/adminapi/setting/region/list` | 地区列表 | `regionManage` | jwt+perm |
| POST | `/adminapi/setting/region/update` | 更新地区 | `regionUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/region/updateField` | 更新字段 | `regionUpdateManage` | jwt+perm |

## adminapi / 子商户管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/finance/subMerchant/bind` | 手动绑定子商户 | `subMerchantBind` | jwt+perm |
| GET | `/adminapi/finance/subMerchant/list` | 子商户分页列表 | - | jwt |
| POST | `/adminapi/finance/subMerchant/unbind` | 解绑子商户 | `subMerchantUnbind` | jwt+perm |

## adminapi / 客户成交

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/salesman/customerTransaction/list` | 获取列表 | - | jwt |

## adminapi / 客服

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/im/servant/servant/getStatus` | 获取客服状态 | - | jwt |
| POST | `/im/servant/servant/modifyStatus` | 修改状态 | - | jwt |
| GET | `/im/servant/servant/transferList` | 获取列表 | - | jwt |

## adminapi / 客服人员管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/im/adminUserKefu/batch` | 批量删除客服人员 | `kefuManageModify` | jwt+perm |
| POST | `/adminapi/im/adminUserKefu/create` | 创建客服人员 | `kefuManageModify` | jwt+perm |
| POST | `/adminapi/im/adminUserKefu/del` | 删除客服人员 | `kefuManageModify` | jwt+perm |
| GET | `/adminapi/im/adminUserKefu/detail` | 获取详情 | `kefuManageList` | jwt+perm |
| GET | `/adminapi/im/adminUserKefu/info` | 获取客服人员信息 | `kefuManageList` | jwt+perm |
| GET | `/adminapi/im/adminUserKefu/list` | 获取列表 | `kefuManageList` | jwt+perm |
| GET | `/adminapi/im/adminUserKefu/roles` | 获取客服角色列表 | `kefuManageList` | jwt+perm |
| POST | `/adminapi/im/adminUserKefu/update` | 更新客服人员 | `kefuManageModify` | jwt+perm |
| POST | `/adminapi/im/adminUserKefu/updateField` | 更新字段 | `kefuManageModify` | jwt+perm |

## adminapi / 客服会话

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/im/conversation/conversation/batch` | 批量操作 | - | jwt |
| GET | `/im/conversation/conversation/consultHistory` | 获取历史会话 | - | jwt |
| POST | `/im/conversation/conversation/create` | 创建 | - | jwt |
| POST | `/im/conversation/conversation/del` | 删除 | - | jwt |
| GET | `/im/conversation/conversation/detail` | 获取详情 | - | jwt |
| GET | `/im/conversation/conversation/list` | 获取列表 | - | jwt |
| POST | `/im/conversation/conversation/saveRemark` | 保存备注 | - | jwt |
| GET | `/im/conversation/conversation/search` | 搜索 | - | jwt |
| POST | `/im/conversation/conversation/transfer` | 转接 | - | jwt |
| POST | `/im/conversation/conversation/updateField` | 更新字段 | - | jwt |
| GET | `/im/conversation/conversation/waitServantList` | 获取待接入的客户列表 | - | jwt |

## adminapi / 客服接入规则

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/im/shopKefuConfig/accessConfig/detail` | 获取接入规则 | `kefuAccessSetting` | jwt+perm |
| POST | `/adminapi/im/shopKefuConfig/accessConfig/save` | 保存接入规则 | `kefuAccessSetting` | jwt+perm |

## adminapi / 对账单

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/finance/statement/exportStatement` | 导出对账单 | - | jwt |
| GET | `/adminapi/finance/statement/exportStatementStatistics` | 导出对账单统计 | - | jwt |
| GET | `/adminapi/finance/statement/getStatementList` | 对账单列表 | - | jwt |
| GET | `/adminapi/finance/statement/getStatementQueryConfig` | 查询字段 | - | jwt |
| GET | `/adminapi/finance/statement/getStatementStatisticsList` | 对账单统计 | - | jwt |
| POST | `/adminapi/finance/statement/saveStatementDownload` | 保存对账单下载信息 | - | jwt |

## adminapi / 库存日志

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/product/productInventoryLog/list` | 获取列表 | `productInventoryLogManage` | jwt+perm |

## adminapi / 库存预警

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/product/stockWarning/batchSave` | 批量设置商品库存预警 | `stockWarningUpdateManage` | jwt+perm |
| GET | `/adminapi/product/stockWarning/downloadTemplate` | 下载库存预警导入模板 | `stockWarningManage` | jwt+perm |
| GET | `/adminapi/product/stockWarning/export` | 导出库存预警列表 | `stockWarningManage` | jwt+perm |
| POST | `/adminapi/product/stockWarning/getBatchImportErrorData` | 获取库存预警导入失败数据 | `stockWarningUpdateManage` | anonymous |
| POST | `/adminapi/product/stockWarning/getImportErrorData` | 获取库存预警导入失败数据 | `stockWarningUpdateManage` | anonymous |
| POST | `/adminapi/product/stockWarning/import` | 导入库存预警配置 | `stockWarningUpdateManage` | jwt+perm |
| GET | `/adminapi/product/stockWarning/list` | 获取库存预警列表 | `stockWarningManage` | jwt+perm |
| POST | `/adminapi/product/stockWarning/save` | 设置商品库存预警 | `stockWarningUpdateManage` | jwt+perm |
| GET | `/adminapi/product/stockWarning/setting` | 获取库存预警通知总开关 | `stockWarningManage` | jwt+perm |
| POST | `/adminapi/product/stockWarning/setting` | 保存库存预警通知总开关 | `stockWarningUpdateManage` | jwt+perm |

## adminapi / 店铺会员设置管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/user/shopMember/add` | 店铺新增客户 | `shopMemberCustomerManage` | jwt+perm |
| GET | `/adminapi/user/shopMember/detail` | 会员详情 | `shopMemberManage`, `shopMemberCustomerManage` | jwt+perm |
| GET | `/adminapi/user/shopMember/downloadImportTemplate` | 下载店铺客户导入模板 | `shopMemberCustomerManage` | jwt+perm |
| POST | `/adminapi/user/shopMember/import` | 店铺导入客户 | `shopMemberCustomerManage` | jwt+perm |
| POST | `/adminapi/user/shopMember/importErrorData` | 导出导入失败行 | `shopMemberCustomerManage` | anonymous |
| GET | `/adminapi/user/shopMember/list` | 会员列表 | `shopMemberManage`, `shopMemberCustomerManage` | jwt+perm |
| GET | `/adminapi/user/shopMember/setting/get` | 读取店铺会员总开关 | `shopMemberSettingManage` | jwt+perm |
| POST | `/adminapi/user/shopMember/setting/save` | 保存店铺会员总开关 | `shopMemberSettingManage` | jwt+perm |
| GET | `/adminapi/user/shopMember/shopList` | 平台端店铺列表（含会员数） | `shopMemberManage` | jwt+perm |

## adminapi / 店铺保证金交易明细

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/deposit/log/detail` | 交易明细详情 | `depositLogManage` | jwt+perm |
| GET | `/adminapi/deposit/log/list` | 全局交易明细分页 | `depositLogManage` | jwt+perm |

## adminapi / 店铺保证金充值

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/deposit/recharge/apply` | 店铺端发起充值申请 | - | jwt |
| POST | `/adminapi/deposit/recharge/audit` | 平台端审核充值单(通过入账/驳回) | `depositRechargeAudit` | jwt+perm |
| GET | `/adminapi/deposit/recharge/detail` | 平台端充值单详情 | `depositPaymentAuditManage` | jwt+perm |
| GET | `/adminapi/deposit/recharge/list` | 平台端充值单列表 | `depositPaymentAuditManage` | jwt+perm |
| GET | `/adminapi/deposit/recharge/payeeAccountOptions` | 充值可选收款账户列表(店铺/供应商端，仅启用) | - | jwt |
| GET | `/adminapi/deposit/recharge/shopPaymentAuditDetail` | 店铺端款项审核详情(按auditType取充值/退还单，校验归属) | - | jwt |
| GET | `/adminapi/deposit/recharge/shopPaymentAuditList` | 店铺端款项审核列表(充值+退还合并，按提交时间倒序) | - | jwt |
| POST | `/adminapi/deposit/recharge/vendorApply` | 供应商端发起充值申请 | - | jwt |
| GET | `/adminapi/deposit/recharge/vendorPaymentAuditDetail` | 供应商端款项审核详情(按auditType取充值/退还单，校验归属) | - | jwt |
| GET | `/adminapi/deposit/recharge/vendorPaymentAuditList` | 供应商端款项审核列表(充值+退还合并，按提交时间倒序) | - | jwt |

## adminapi / 店铺保证金账户

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/deposit/account/adjust` | 手动调整应缴 | `depositAdjust` | jwt+perm |
| GET | `/adminapi/deposit/account/adjustLogList` | 详情-调整记录 | `depositAccountManage` | jwt+perm |
| POST | `/adminapi/deposit/account/deduct` | 赔付扣除 | `depositDeduct` | jwt+perm |
| GET | `/adminapi/deposit/account/detail` | 账户详情-基本信息 | `depositAccountManage` | jwt+perm |
| GET | `/adminapi/deposit/account/list` | 账户分页总览 | `depositAccountManage` | jwt+perm |
| GET | `/adminapi/deposit/account/logList` | 详情-交易记录 | `depositAccountManage` | jwt+perm |
| GET | `/adminapi/deposit/account/runDueCheck` | 手动触发保证金到期扫描(测试/运维用,免登录免鉴权) | `depositAccountManage` | jwt+perm |
| POST | `/adminapi/deposit/account/sendWarning` | 发送保证金预警 | `depositSendWarning` | jwt+perm |

## adminapi / 店铺保证金退还

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/deposit/refund/apply` | 店铺端发起退还/提现申请 | - | jwt |
| POST | `/adminapi/deposit/refund/audit` | 平台端一步式审核退还单(通过即出账完成/驳回) | `depositRefundAudit` | jwt+perm |
| GET | `/adminapi/deposit/refund/detail` | 平台端退还单详情 | `depositPaymentAuditManage` | jwt+perm |
| GET | `/adminapi/deposit/refund/fullRefundCheck` | 店铺端查询能否全额退还保证金 | - | jwt |
| GET | `/adminapi/deposit/refund/list` | 平台端退还单列表 | `depositPaymentAuditManage` | jwt+perm |
| POST | `/adminapi/deposit/refund/vendorApply` | 供应商端发起退还/提现申请 | - | jwt |
| GET | `/adminapi/deposit/refund/vendorFullRefundCheck` | 供应商端查询能否全额退还保证金 | - | jwt |

## adminapi / 店铺信息

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/merchant/shop/batch` | 批量操作 | - | jwt |
| POST | `/adminapi/merchant/shop/choose` | 选择管理员、店铺、供应商或门店登录 | - | jwt |
| POST | `/adminapi/merchant/shop/create` | 创建 | `merchantShopModifyManage`, `storeSelfPackListUpdateManage`, `storeListUpdateManage` | jwt+perm |
| GET | `/adminapi/merchant/shop/currentDetail` | 获取当前登录用户的店铺信息 | - | jwt |
| POST | `/adminapi/merchant/shop/del` | 删除 | - | jwt |
| POST | `/adminapi/merchant/shop/delShop` | 删除店铺/门店/自提点 | `merchantShopModifyManage`, `storeSelfPackListUpdateManage`, `storeListUpdateManage` | jwt+perm |
| GET | `/adminapi/merchant/shop/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/merchant/shop/getStoreListByProductId` | 获取商品对应的门店列表 | - | jwt |
| GET | `/adminapi/merchant/shop/getVendorSetting` | 获取供应商配置 | `merchantShopModifyManage`, `storeSelfPackListUpdateManage`, `storeListUpdateManage`, `vendorSetting` | jwt+perm |
| GET | `/adminapi/merchant/shop/list` | 获取列表 | - | jwt |
| GET | `/adminapi/merchant/shop/myShop` | 获取当前登录用户的店铺信息 | - | jwt |
| POST | `/adminapi/merchant/shop/recordLoginSystem` | 记录上次登录端 | - | anonymous |
| POST | `/adminapi/merchant/shop/setPosLoginDefault` | 设置登录后默认进收银台 | - | anonymous |
| POST | `/adminapi/merchant/shop/setting` | 更新配置 | `merchantShopModifyManage`, `storeSelfPackListUpdateManage`, `storeListUpdateManage` | jwt+perm |
| GET | `/adminapi/merchant/shop/shopList` | 获取店铺标题和名称 | - | jwt |
| GET | `/adminapi/merchant/shop/staffShow` | 获取员工展示信息 | - | jwt |
| POST | `/adminapi/merchant/shop/update` | 更新 | `merchantShopModifyManage`, `storeSelfPackListUpdateManage`, `storeListUpdateManage` | jwt+perm |
| POST | `/adminapi/merchant/shop/updateField` | 更新字段 | `merchantShopModifyManage`, `storeSelfPackListUpdateManage`, `storeListUpdateManage` | jwt+perm |
| POST | `/adminapi/merchant/shop/updateInfo` | 更新店铺信息 | `merchantShopModifyManage`, `storeSelfPackListUpdateManage`, `storeListUpdateManage`, `shopSettingsUpdateManage` | jwt+perm |
| POST | `/adminapi/merchant/shop/updateVendorSetting` | 保存供应商配置 | `merchantShopModifyManage`, `storeSelfPackListUpdateManage`, `storeListUpdateManage` | jwt+perm |

## adminapi / 店铺员工管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/merchant/adminUserShop/batch` | 批量删除店铺员工 | `adminUserShopModifyManage` | jwt+perm |
| GET | `/adminapi/merchant/adminUserShop/checkIsAdmin` | 判断当前用户是否为当前店铺/门店的主账号 | - | jwt |
| POST | `/adminapi/merchant/adminUserShop/create` | 创建店铺员工 | `adminUserShopModifyManage` | jwt+perm |
| POST | `/adminapi/merchant/adminUserShop/del` | 删除店铺员工 | `adminUserShopModifyManage` | jwt+perm |
| GET | `/adminapi/merchant/adminUserShop/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/merchant/adminUserShop/info` | 获取店铺员工信息 | - | jwt |
| GET | `/adminapi/merchant/adminUserShop/list` | 获取列表 | - | jwt |
| POST | `/adminapi/merchant/adminUserShop/modifyUser` | 修改店铺员工用户 | `adminUserShopModifyManage` | jwt+perm |
| POST | `/adminapi/merchant/adminUserShop/update` | 更新店铺员工 | `adminUserShopModifyManage` | jwt+perm |

## adminapi / 店铺商品分类信息

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/merchant/shopProductCategory/batch` | 批量操作 | `merchantShopProductCategoryModifyManage` | jwt+perm |
| POST | `/adminapi/merchant/shopProductCategory/create` | 创建 | `merchantShopProductCategoryModifyManage` | jwt+perm |
| POST | `/adminapi/merchant/shopProductCategory/del` | 删除 | `merchantShopProductCategoryModifyManage` | jwt+perm |
| GET | `/adminapi/merchant/shopProductCategory/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/merchant/shopProductCategory/getAllCategory` | 获取所有分类 | - | jwt |
| GET | `/adminapi/merchant/shopProductCategory/getCategoryName` | 获取列表 | - | jwt |
| GET | `/adminapi/merchant/shopProductCategory/list` | 获取列表 | - | jwt |
| POST | `/adminapi/merchant/shopProductCategory/update` | 更新 | `merchantShopProductCategoryModifyManage` | jwt+perm |
| POST | `/adminapi/merchant/shopProductCategory/updateField` | 更新字段 | `merchantShopProductCategoryModifyManage` | jwt+perm |

## adminapi / 店铺端保证金

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/deposit/shop/frozenDetailList` | 冻结明细(平台扣款记录) | - | jwt |
| GET | `/adminapi/deposit/shop/logList` | 店铺端交易流水 | - | jwt |
| GET | `/adminapi/deposit/shop/overview` | 保证金总览 | - | jwt |
| GET | `/adminapi/deposit/shop/protocol` | 保证金协议 | - | jwt |
| GET | `/adminapi/deposit/shop/warning` | 首页保证金预警横幅 | - | jwt |

## adminapi / 店铺表

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/merchant/shopAccount/applyShopAgreement` | 申请入驻协议 | - | jwt |
| POST | `/adminapi/merchant/shopAccount/batch` | 批量操作 | - | jwt |
| POST | `/adminapi/merchant/shopAccount/create` | 创建 | - | jwt |
| POST | `/adminapi/merchant/shopAccount/del` | 删除 | - | jwt |
| GET | `/adminapi/merchant/shopAccount/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/merchant/shopAccount/list` | 获取列表 | - | jwt |
| POST | `/adminapi/merchant/shopAccount/update` | 更新 | - | jwt |
| POST | `/adminapi/merchant/shopAccount/updateField` | 更新字段 | - | jwt |

## adminapi / 店铺资金变化

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/merchant/shopAccount/index` | 获取统计数据 | - | jwt |
| GET | `/adminapi/merchant/shopAccount/logList` | 获取资金日志列表 | - | jwt |

## adminapi / 微信直播

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/content/wechatLive/addToRoom` | 添加已导入商品到直播间 | `wechatLiveModifyManage` | jwt+perm |
| POST | `/adminapi/content/wechatLive/create` | 创建直播间 | `wechatLiveModifyManage` | jwt+perm |
| POST | `/adminapi/content/wechatLive/delete` | 删除直播间 | `wechatLiveModifyManage` | jwt+perm |
| GET | `/adminapi/content/wechatLive/detail` | 获取直播间详情 | `wechatLive` | jwt+perm |
| GET | `/adminapi/content/wechatLive/finderLiveRecord` | 获取视频号当前的直播记录 | `wechatLive` | jwt+perm |
| GET | `/adminapi/content/wechatLive/list` | 获取直播间列表 | `wechatLive` | jwt+perm |
| GET | `/adminapi/content/wechatLive/promote/{roomId}` | 获取直播间推广信息 | `wechatLive` | jwt+perm |
| GET | `/adminapi/content/wechatLive/pushUrl/{roomId}` | 获取推流地址 | `wechatLive` | jwt+perm |
| GET | `/adminapi/content/wechatLive/refresh` | 刷新直播间信息 | `wechatLive` | jwt+perm |
| POST | `/adminapi/content/wechatLive/removeFromRoom` | 从直播间移除商品 | `wechatLiveModifyManage` | jwt+perm |
| GET | `/adminapi/content/wechatLive/room/{roomId}` | 获取直播间商品列表 | `wechatLive` | jwt+perm |
| GET | `/adminapi/content/wechatLive/syncFromWechat` | 从微信同步直播间 | `wechatLive` | jwt+perm |
| POST | `/adminapi/content/wechatLive/syncToWechat` | 同步到微信 | `wechatLiveModifyManage` | jwt+perm |
| POST | `/adminapi/content/wechatLive/update` | 更新直播间 | `wechatLiveModifyManage` | jwt+perm |

## adminapi / 微信直播商品

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/content/wechatLiveGoods/import` | 导入直播商品主表 | `wechatLiveProductModifyManage` | jwt+perm |
| GET | `/adminapi/content/wechatLiveGoods/list` | 获取直播商品主表列表 | `wechatLiveProduct` | jwt+perm |
| GET | `/adminapi/content/wechatLiveGoods/query` | 查询直播商品 | `wechatLiveProduct` | jwt+perm |

## adminapi / 微信直播订单同步

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/content/wechatLiveOrderSync/logs` | 同步日志列表 | `wechatLiveManage` | jwt+perm |
| POST | `/adminapi/content/wechatLiveOrderSync/reconcile` | 触发对账 | `wechatLiveManage` | jwt+perm |
| POST | `/adminapi/content/wechatLiveOrderSync/retry/{logId}` | 重试同步 | `wechatLiveManage` | jwt+perm |

## adminapi / 微信菜单

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/wechat/menu` | 获取微信菜单详情 | `wechatMenu` | jwt+perm |
| POST | `/adminapi/wechat/menu/saveAndPublish` | 保存并发布微信菜单 | `wechatMenuManage` | jwt+perm |

## adminapi / 打印日志

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/print/printLog/list` | 获取打印日志分页列表 | `baseReceiptManage`, `shopBaseReceiptManage`, `posReceiptPrint` | jwt+perm |

## adminapi / 打印机管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/print/print/create` | 新增打印机 | `baseReceiptUpdateManage`, `shopBaseReceiptUpdateManage`, `posReceiptPrintUpdateManage` | jwt+perm |
| POST | `/adminapi/print/print/del` | 删除 | `baseReceiptUpdateManage`, `shopBaseReceiptUpdateManage`, `posReceiptPrintUpdateManage` | jwt+perm |
| GET | `/adminapi/print/print/detail` | 获取详情 | `baseReceiptManage`, `shopBaseReceiptManage`, `posReceiptPrint` | jwt+perm |
| GET | `/adminapi/print/print/devicePrinters` | 查询收银台设备可用打印机 | - | jwt |
| POST | `/adminapi/print/print/hasEnabled` | 是否有启用的打印机 | - | jwt |
| GET | `/adminapi/print/print/list` | 获取列表 | `baseReceiptManage`, `shopBaseReceiptManage`, `posReceiptPrint` | jwt+perm |
| POST | `/adminapi/print/print/update` | 编辑打印机 | `baseReceiptUpdateManage`, `shopBaseReceiptUpdateManage`, `posReceiptPrintUpdateManage` | jwt+perm |
| POST | `/adminapi/print/print/updateField` | 更新字段 | `baseReceiptUpdateManage`, `shopBaseReceiptUpdateManage`, `posReceiptPrintUpdateManage` | jwt+perm |

## adminapi / 打印机配置管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/print/printConfig/getConfigsByPrintId` | 获取打印机配置 | `baseReceiptManage`, `shopBaseReceiptManage`, `posReceiptPrint` | jwt+perm |
| POST | `/adminapi/print/printConfig/print` | 打印订单 | - | jwt |
| POST | `/adminapi/print/printConfig/update` | 更新打印机配置 | `baseReceiptUpdateManage`, `shopBaseReceiptUpdateManage`, `posReceiptPrintUpdateManage` | jwt+perm |

## adminapi / 批发商品管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/wholesale/product/add` | 添加批发商品（从商品库选择） | - | jwt |
| POST | `/adminapi/wholesale/product/batchAdd` | 批量添加批发商品（一次选多个普通商品转批发） | - | jwt |
| POST | `/adminapi/wholesale/product/batchDelete` | 批量删除批发商品 | - | jwt |
| POST | `/adminapi/wholesale/product/buyRule/batchSave` | 起批量批量设置（CR-20260604-1042；一次给 N 个 product 应用同一对 dimension + moq） | - | jwt |
| GET | `/adminapi/wholesale/product/buyRule/detail` | 起批规则详情 | - | jwt |
| POST | `/adminapi/wholesale/product/buyRule/save` | 起批量设置（CR-20260604-0937：统一字段 moq） | - | jwt |
| POST | `/adminapi/wholesale/product/multiple/save` | 倍数购买保存（按 SKU 批量） | - | jwt |
| GET | `/adminapi/wholesale/product/page` | 批发商品分页 | - | jwt |
| POST | `/adminapi/wholesale/product/price/batchSetByGrade` | 批量设置类型售价 | - | jwt |
| POST | `/adminapi/wholesale/product/price/batchSetCommon` | 批量设置通用售价 | - | jwt |
| GET | `/adminapi/wholesale/product/price/detail` | 批发价详情 | - | jwt |
| POST | `/adminapi/wholesale/product/price/save` | 批发价保存（单规格 + 多规格统一） | - | jwt |
| GET | `/adminapi/wholesale/product/selectable/page` | 可选批发商品分页（当前操作者名下商品 + 剔除已配置批发；CR-20260603-1700 / CR-20260610 多租户） | - | jwt |

## adminapi / 批发商等级管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/wholesale/grade/delete` | 等级删除（软删；绑定批发商→5305 拦截，默认等级→5306 拦截） | `wholesalerGradeDeleteManage` | jwt+perm |
| GET | `/adminapi/wholesale/grade/list` | 等级列表 | - | jwt |
| POST | `/adminapi/wholesale/grade/save` | 等级新增/编辑 | `wholesalerGradeSaveManage` | jwt+perm |
| POST | `/adminapi/wholesale/grade/setStatus` | 等级启停 | `wholesalerGradeSetStatusManage` | jwt+perm |

## adminapi / 批发商管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/wholesale/wholesaler/add` | 添加批发商 | - | jwt |
| POST | `/adminapi/wholesale/wholesaler/changeGrade` | 批发商改等级 | - | jwt |
| POST | `/adminapi/wholesale/wholesaler/import` | 批量导入批发商 | - | jwt |
| GET | `/adminapi/wholesale/wholesaler/page` | 批发商分页 | - | jwt |
| POST | `/adminapi/wholesale/wholesaler/remove` | 按 ID 直接移除批发商身份 | - | jwt |
| POST | `/adminapi/wholesale/wholesaler/removeByGrade` | 按等级移除批发商 | - | jwt |
| GET | `/adminapi/wholesale/wholesaler/selectableUserPage` | 可选为批发商的会员分页（含 isWholesaler 标识，添加批发商选会员用） | - | jwt |
| POST | `/adminapi/wholesale/wholesaler/setStatus` | 批发商启停 | - | jwt |

## adminapi / 批发设置

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/wholesale/setting/get` | 查询批发设置 | - | jwt |
| POST | `/adminapi/wholesale/setting/save` | 保存批发设置 | - | jwt |

## adminapi / 抽奖活动管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/lotteryActivity/create` | 创建抽奖活动 | `lotteryActivityModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/lotteryActivity/delete` | 删除抽奖活动 | `lotteryActivityModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/lotteryActivity/detail` | 抽奖活动详情 | `lotteryManage` | jwt+perm |
| POST | `/adminapi/promotion/lotteryActivity/end` | 结束抽奖活动 | `lotteryActivityModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/lotteryActivity/list` | 抽奖活动列表 | `lotteryManage` | jwt+perm |
| POST | `/adminapi/promotion/lotteryActivity/update` | 编辑抽奖活动 | `lotteryActivityModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/lotteryActivity/userManage` | 用户管理列表 | `lotteryManage` | jwt+perm |
| GET | `/adminapi/promotion/lotteryActivity/userRecord` | 用户中奖详情列表 | `lotteryManage` | jwt+perm |

## adminapi / 拼团活动管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/promotion/groupon/clientList` | 获取客户端列表 | `grouponPromotionManage` | jwt+perm |
| POST | `/adminapi/promotion/groupon/create` | 创建 | `grouponPromotionModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/groupon/del` | 删除 | `grouponPromotionModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/groupon/detail` | 获取详情 | `grouponPromotionManage` | jwt+perm |
| POST | `/adminapi/promotion/groupon/finish` | 立即结束 | `grouponPromotionModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/groupon/list` | 获取列表 | `grouponPromotionManage` | jwt+perm |
| GET | `/adminapi/promotion/groupon/promotionPromote` | 活动推广 | `grouponPromotionManage` | jwt+perm |
| POST | `/adminapi/promotion/groupon/update` | 更新 | `grouponPromotionModifyManage` | jwt+perm |

## adminapi / 拼团活动记录管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/groupon/record/batchGroup` | 批量手动成团 | `grouponPromotionManage` | jwt+perm |
| GET | `/adminapi/promotion/groupon/record/groupMemberList` | 团员列表 | `grouponPromotionManage` | jwt+perm |
| POST | `/adminapi/promotion/groupon/record/manualGroup` | 手动成团 | `grouponPromotionManage` | jwt+perm |
| GET | `/adminapi/promotion/groupon/record/openJoinList` | 开团记录 | `grouponPromotionManage` | jwt+perm |
| GET | `/adminapi/promotion/groupon/record/orderList` | 订单记录 | `grouponPromotionManage` | jwt+perm |

## adminapi / 授信会员管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/credit/user/create` | 添加授信会员 | `termMemberModifyManage` | jwt+perm |
| POST | `/adminapi/credit/user/delete` | 删除授信会员 | `termMemberModifyManage` | jwt+perm |
| GET | `/adminapi/credit/user/detail` | 授信会员详情 | `termMemberManage` | jwt+perm |
| GET | `/adminapi/credit/user/list` | 授信会员列表 | `termMemberManage` | jwt+perm |
| POST | `/adminapi/credit/user/update` | 修改授信会员 | `termMemberModifyManage` | jwt+perm |
| POST | `/adminapi/credit/user/updateField` | 更新授信会员字段（用于启用/禁用等） | `termMemberModifyManage` | jwt+perm |

## adminapi / 提现申请

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/finance/userWithdrawApply/batch` | 批量操作 | `userWithdrawApplyBatchManage` | jwt+perm |
| POST | `/adminapi/finance/userWithdrawApply/create` | 创建 | `userWithdrawApplyUpdateManage` | jwt+perm |
| POST | `/adminapi/finance/userWithdrawApply/del` | 删除 | `userWithdrawApplyDelManage` | jwt+perm |
| GET | `/adminapi/finance/userWithdrawApply/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/finance/userWithdrawApply/list` | 获取列表 | - | jwt |
| POST | `/adminapi/finance/userWithdrawApply/update` | 更新 | `userWithdrawApplyUpdateManage` | jwt+perm |
| POST | `/adminapi/finance/userWithdrawApply/updateField` | 更新字段 | `userWithdrawApplyUpdateManage` | jwt+perm |

## adminapi / 提示管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/common/tipsManage/list` | 提示管理列表 | - | jwt |

## adminapi / 搴楅摵璁㈠崟閰嶇疆

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/order/config/detail` | 鑾峰彇閰嶇疆璇︽儏 | - | jwt |
| POST | `/adminapi/order/config/save` | 淇濆瓨閰嶇疆 | `orderConfigModifyManage` | jwt+perm |

## adminapi / 文件访问控制器

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/app/update/{timestamp}/{filename}` |  | - | jwt |
| GET | `/img/gallery/{timestamp}/{filename}` |  | - | jwt |
| GET | `/video/gallery/{timestamp}/{filename}` |  | - | jwt |

## adminapi / 文章

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/content/article/batch` | 批量操作 | `articleModifyManage` | jwt+perm |
| POST | `/adminapi/content/article/create` | 创建文章 | `articleModifyManage` | jwt+perm |
| POST | `/adminapi/content/article/del` | 删除文章 | `articleModifyManage` | jwt+perm |
| GET | `/adminapi/content/article/detail` | 文章详情 | `articleManage` | jwt+perm |
| GET | `/adminapi/content/article/list` | 文章列表 | `decorate`, `articleManage` | jwt+perm |
| POST | `/adminapi/content/article/update` | 更新文章 | `articleModifyManage` | jwt+perm |
| POST | `/adminapi/content/article/updateField` | 更新文章字段 | `articleModifyManage` | jwt+perm |

## adminapi / 文章分类

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/content/articleCategory/batch` | 批量操作 | `articleCategoryModifyManage` | jwt+perm |
| POST | `/adminapi/content/articleCategory/create` | 创建文章分类 | `articleCategoryModifyManage` | jwt+perm |
| POST | `/adminapi/content/articleCategory/del` | 删除文章分类 | `articleCategoryModifyManage` | jwt+perm |
| GET | `/adminapi/content/articleCategory/detail` | 文章分类详情 | `articleCategoryManage` | jwt+perm |
| GET | `/adminapi/content/articleCategory/list` | 文章分类列表 | `articleCategoryManage` | jwt+perm |
| GET | `/adminapi/content/articleCategory/tree` | 文章分类树 | `articleCategoryManage` | jwt+perm |
| POST | `/adminapi/content/articleCategory/update` | 更新文章分类 | `articleCategoryModifyManage` | jwt+perm |
| POST | `/adminapi/content/articleCategory/updateField` | 更新文章分类字段 | `articleCategoryModifyManage` | jwt+perm |

## adminapi / 权限

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/authority/authority/batch` | 批量操作 | `authorityBatchManage` | jwt+perm |
| POST | `/adminapi/authority/authority/create` | 创建权限 | `authorityUpdateManage` | jwt+perm |
| POST | `/adminapi/authority/authority/del` | 删除权限 | `authorityDelManage` | jwt+perm |
| GET | `/adminapi/authority/authority/detail` | 获取权限详情 | - | jwt |
| GET | `/adminapi/authority/authority/getAllAuthority` | 获取所有权限 | - | jwt |
| GET | `/adminapi/authority/authority/getAuthority` | 获取所有权限 | - | jwt |
| GET | `/adminapi/authority/authority/getAuthorityParentName` | 获取父级权限名称 | - | jwt |
| GET | `/adminapi/authority/authority/list` | 获取权限列表 | - | jwt |
| POST | `/adminapi/authority/authority/update` | 更新权限 | `authorityUpdateManage` | jwt+perm |
| POST | `/adminapi/authority/authority/updateField` | 更新权限字段 | `authorityUpdateManage` | jwt+perm |

## adminapi / 活动赠品

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/productGift/batch` | 批量操作 | `productGiftModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/productGift/create` | 创建 | `productGiftModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/productGift/del` | 删除 | `productGiftModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/productGift/detail` | 获取详情 | `productGiftManage` | jwt+perm |
| GET | `/adminapi/promotion/productGift/list` | 获取列表 | `productGiftManage` | jwt+perm |
| POST | `/adminapi/promotion/productGift/update` | 更新 | `productGiftModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/productGift/updateField` | 更新字段 | `productGiftModifyManage` | jwt+perm |

## adminapi / 消息分类

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/setting/messageType/detail` | 消息分类详情 | `messageTypeManage` | jwt+perm |
| GET | `/adminapi/setting/messageType/list` | 消息分类列表 | `messageTypeManage` | jwt+perm |
| POST | `/adminapi/setting/messageType/miniProgramMessageTemplate` | 小程序模板生成 | `miniProgramMessageTemplateManage` | jwt+perm |
| POST | `/adminapi/setting/messageType/update` | 修改消息分类 | `messageTypeUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/messageType/updateField` | 修改消息分类单个字段 | `messageTypeUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/messageType/wechatMessageTemplate` | 公众号模板生成 | `wechatMessageTemplate` | jwt+perm |

## adminapi / 消息列表

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/im/conversation/message/list` | 获取列表 | - | jwt |
| GET | `/im/conversation/message/receivedList` | 客服消息播报记录列表 | - | jwt |
| POST | `/im/conversation/message/send` | 发送消息 | - | jwt |
| POST | `/im/conversation/message/setRead` | 设置已读 | - | jwt |
| POST | `/im/conversation/message/uploadVideo` | 上传视频 | - | jwt |

## adminapi / 满减活动

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/productPromotion/batch` | 批量操作 | `productPromotionModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/productPromotion/config` | 获取配置 | - | jwt |
| GET | `/adminapi/promotion/productPromotion/conflict` | 活动冲突列表 | - | jwt |
| POST | `/adminapi/promotion/productPromotion/create` | 创建 | `productPromotionModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/productPromotion/del` | 删除 | `productPromotionModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/productPromotion/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/promotion/productPromotion/list` | 获取列表 | - | jwt |
| POST | `/adminapi/promotion/productPromotion/update` | 更新 | `productPromotionModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/productPromotion/updateField` | 更新字段 | `productPromotionModifyManage` | jwt+perm |

## adminapi / 物流公司配置

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/setting/logisticsCompanyConfig/create` | 绑定物流公司 | `logisticsCompanyConfigEditManage` | jwt+perm |
| POST | `/adminapi/setting/logisticsCompanyConfig/del` | 解绑物流公司 | `logisticsCompanyConfigEditManage` | jwt+perm |
| GET | `/adminapi/setting/logisticsCompanyConfig/detail` | 获取配置详情 | `logisticsCompanyConfigManage`, `posOrder`, `shopLogisticsCompanyConfigManage`, `vendorLogisticsCompanyConfigManage` | jwt+perm |
| GET | `/adminapi/setting/logisticsCompanyConfig/list` | 获取当前租户物流公司配置列表 | `logisticsCompanyConfigManage`, `posOrder`, `shopLogisticsCompanyConfigManage`, `vendorLogisticsCompanyConfigManage` | jwt+perm |
| POST | `/adminapi/setting/logisticsCompanyConfig/setDefault` | 设置默认物流公司 | `logisticsCompanyConfigEditManage` | jwt+perm |
| POST | `/adminapi/setting/logisticsCompanyConfig/update` | 更新物流公司配置 | `logisticsCompanyConfigEditManage` | jwt+perm |
| POST | `/adminapi/setting/logisticsCompanyConfig/updateField` | 更新字段（启用/禁用等） | `logisticsCompanyConfigEditManage` | jwt+perm |

## adminapi / 瑙嗛鐩稿唽淇℃伅鍚庡彴绠＄悊鎺ュ彛

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/setting/galleryVideoInfo/create` | 淇濆瓨瑙嗛淇℃伅 | `galleryVideoManage` | jwt+perm |
| POST | `/adminapi/setting/galleryVideoInfo/del` | 鍒犻櫎鐩稿唽瑙嗛 | `galleryVideoManage` | jwt+perm |
| GET | `/adminapi/setting/galleryVideoInfo/detail` | 瑙嗛淇℃伅璇︽儏 | `productManage`, `galleryVideoManage` | jwt+perm |
| GET | `/adminapi/setting/galleryVideoInfo/list` | 鐩稿唽瑙嗛鍒楄〃 | `productManage`, `galleryVideoManage` | jwt+perm |
| POST | `/adminapi/setting/galleryVideoInfo/update` | 淇敼瑙嗛淇℃伅 | `galleryVideoManage` | jwt+perm |
| POST | `/adminapi/setting/galleryVideoInfo/uploadVideo` | 涓婁紶瑙嗛 | `galleryVideoManage` | jwt+perm |

## adminapi / 瑙嗛鐩稿唽鍚庡彴绠＄悊鎺ュ彛

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/setting/galleryVideo/batch` | 鎵归噺鎿嶄綔鐩稿唽 | `galleryVideoManage` | jwt+perm |
| POST | `/adminapi/setting/galleryVideo/create` | 娣诲姞瑙嗛鐩稿唽 | `galleryVideoManage` | jwt+perm |
| POST | `/adminapi/setting/galleryVideo/del` | 鍒犻櫎瑙嗛鐩稿唽 | `galleryVideoManage` | jwt+perm |
| GET | `/adminapi/setting/galleryVideo/detail` | 瑙嗛鐩稿唽璇︽儏 | `galleryVideoManage` | jwt+perm |
| GET | `/adminapi/setting/galleryVideo/getAllCategory` | 瑙嗛鐩稿唽鏍戝舰鍒楄〃 | `productManage`, `galleryVideoManage` | jwt+perm |
| GET | `/adminapi/setting/galleryVideo/list` | 瑙嗛鐩稿唽鍒楄〃 | `productManage`, `galleryVideoManage` | jwt+perm |
| POST | `/adminapi/setting/galleryVideo/update` | 淇敼瑙嗛鐩稿唽 | `galleryVideoManage` | jwt+perm |

## adminapi / 用户余额日志

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/finance/userBalanceLog/list` | 获取列表 | - | jwt |

## adminapi / 用户标签关联管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/user/userMemberTag/batchBindTags` | 批量给用户打标签 | `memberTagUpdateManage` | jwt+perm |
| POST | `/adminapi/user/userMemberTag/batchUnbindTags` | 批量取消用户标签 | `memberTagUpdateManage` | jwt+perm |
| POST | `/adminapi/user/userMemberTag/bindTags` | 给用户打标签 | `memberTagUpdateManage` | jwt+perm |
| GET | `/adminapi/user/userMemberTag/listByUser` | 查询用户的所有标签 | `memberTagManage` | jwt+perm |
| POST | `/adminapi/user/userMemberTag/unbindTags` | 取消用户标签 | `memberTagUpdateManage` | jwt+perm |

## adminapi / 电子卡券

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/product/eCard/batch` | 批量操作 | `eCardManage` | jwt+perm |
| POST | `/adminapi/product/eCard/create` | 创建 | `eCardManage` | jwt+perm |
| POST | `/adminapi/product/eCard/del` | 删除 | `eCardManage` | jwt+perm |
| GET | `/adminapi/product/eCard/detail` | 获取详情 | `eCardManage` | jwt+perm |
| GET | `/adminapi/product/eCard/list` | 获取列表 | `eCardManage` | jwt+perm |
| POST | `/adminapi/product/eCard/update` | 更新 | `eCardManage` | jwt+perm |
| POST | `/adminapi/product/eCard/updateField` | 更新字段 | `eCardManage` | jwt+perm |

## adminapi / 电子卡券组

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/product/eCardGroup/batch` | 批量操作 | `eCardManage` | jwt+perm |
| POST | `/adminapi/product/eCardGroup/create` | 创建 | `eCardManage` | jwt+perm |
| POST | `/adminapi/product/eCardGroup/del` | 删除 | `eCardManage` | jwt+perm |
| GET | `/adminapi/product/eCardGroup/detail` | 获取详情 | `eCardManage` | jwt+perm |
| POST | `/adminapi/product/eCardGroup/import` | 批量导入 | `eCardManage` | jwt+perm |
| GET | `/adminapi/product/eCardGroup/list` | 获取列表 | `eCardManage` | jwt+perm |
| POST | `/adminapi/product/eCardGroup/update` | 更新 | `eCardManage` | jwt+perm |
| POST | `/adminapi/product/eCardGroup/updateField` | 更新字段 | `eCardManage` | jwt+perm |

## adminapi / 登录

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/login/signin` | 管理后台登录（用户名/手机号 + 密码/验证码） | - | anonymous |

## adminapi / 登录控制器

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/login/sendMobileCode` | 发送短信验证码 | - | anonymous |

## adminapi / 相册图片管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/setting/galleryPic/batchDel` | 批量删除素材 | `galleryPicModifyManage` | jwt+perm |
| POST | `/adminapi/setting/galleryPic/batchDownload` | 批量下载素材 | `galleryPicManage` | jwt+perm |
| POST | `/adminapi/setting/galleryPic/createFromExternal` | 通过外部链接创建图片 | `galleryPicManage` | jwt+perm |
| POST | `/adminapi/setting/galleryPic/del` | 删除相册图片 | `galleryPicModifyManage` | jwt+perm |
| GET | `/adminapi/setting/galleryPic/detail` | 图片详情 | `galleryPicManage` | jwt+perm |
| GET | `/adminapi/setting/galleryPic/list` | 相册图片列表 | `decorate`, `galleryPicManage` | jwt+perm |
| POST | `/adminapi/setting/galleryPic/sort` | 图片手动排序 | `galleryPicModifyManage` | jwt+perm |
| POST | `/adminapi/setting/galleryPic/transfer` | 批量移动素材 | `galleryPicModifyManage` | jwt+perm |
| POST | `/adminapi/setting/galleryPic/updateField` | 更新权限字段 | `galleryPicModifyManage` | jwt+perm |
| POST | `/adminapi/setting/galleryPic/uploadImg` | 上传图片 | `galleryPicManage` | jwt+perm |

## adminapi / 相册管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/setting/gallery/batch` | 批量操作相册 | `galleryModifyManage` | jwt+perm |
| POST | `/adminapi/setting/gallery/create` | 添加相册 | `galleryModifyManage`, `decorateModifyManage` | jwt+perm |
| POST | `/adminapi/setting/gallery/del` | 删除相册 | `galleryModifyManage` | jwt+perm |
| GET | `/adminapi/setting/gallery/detail` | 相册详情 | `galleryManage` | jwt+perm |
| GET | `/adminapi/setting/gallery/list` | 相册列表 | `decorate`, `galleryManage` | jwt+perm |
| POST | `/adminapi/setting/gallery/move` | 分组转移/素材移动 | `galleryModifyManage` | jwt+perm |
| POST | `/adminapi/setting/gallery/sort` | 分组排序 | `galleryModifyManage` | jwt+perm |
| GET | `/adminapi/setting/gallery/tree` | 统一分组树 | `decorate`, `galleryManage` | jwt+perm |
| POST | `/adminapi/setting/gallery/update` | 修改相册 | `galleryModifyManage` | jwt+perm |

## adminapi / 示例

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/example/example/batch` | 批量操作 | `exampleBatch` | jwt+perm |
| POST | `/adminapi/example/example/create` | 创建 | `exampleCreate` | jwt+perm |
| POST | `/adminapi/example/example/del` | 删除 | `exampleDel` | jwt+perm |
| GET | `/adminapi/example/example/detail` | 获取详情 | `exampleDetail` | jwt+perm |
| GET | `/adminapi/example/example/list` | 获取列表 | `exampleList` | jwt+perm |
| POST | `/adminapi/example/example/update` | 更新 | `exampleUpdate` | jwt+perm |
| POST | `/adminapi/example/example/updateField` | 更新字段 | `exampleUpdateField` | jwt+perm |

## adminapi / 礼品卡

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/giftCards/delete` | 礼品卡删除 | `giftCardUpdateManage` | jwt+perm |
| GET | `/adminapi/promotion/giftCards/detail` | 礼品卡详情 | - | jwt |
| GET | `/adminapi/promotion/giftCards/exportList` | 礼品卡列表导出 | - | jwt |
| GET | `/adminapi/promotion/giftCards/giftCardExchangeDetail` | 礼品卡兑换详情 | - | jwt |
| POST | `/adminapi/promotion/giftCards/importList` | 礼品卡列表导入 | `giftCardUpdateManage` | jwt+perm |
| GET | `/adminapi/promotion/giftCards/importTemplate` | 礼品卡导入模板 | - | jwt |
| GET | `/adminapi/promotion/giftCards/list` | 礼品卡列表 | - | jwt |
| POST | `/adminapi/promotion/giftCards/updateCardStatus` | 礼品卡修改可用状态 | - | jwt |

## adminapi / 礼品卡分组

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/giftCardGroups/create` | 创建礼品卡分组 | `giftCardUpdateManage` | jwt+perm |
| POST | `/adminapi/promotion/giftCardGroups/delete` | 删除礼品卡分组 | `giftCardUpdateManage` | jwt+perm |
| GET | `/adminapi/promotion/giftCardGroups/detail` | 礼品卡分组详情 | - | jwt |
| GET | `/adminapi/promotion/giftCardGroups/getGiftCardGroups` | 获取礼品卡分组 | - | jwt |
| POST | `/adminapi/promotion/giftCardGroups/giftCardGroupProductsUpdate` | 礼品卡绑定商品管理 | `giftCardUpdateManage` | jwt+perm |
| GET | `/adminapi/promotion/giftCardGroups/list` | 礼品卡分组列表 | - | jwt |
| POST | `/adminapi/promotion/giftCardGroups/update` | 修改礼品卡分组 | `giftCardUpdateManage` | jwt+perm |
| POST | `/adminapi/promotion/giftCardGroups/updateStatus` | 修改礼品卡分组状态 | `giftCardUpdateManage` | jwt+perm |

## adminapi / 积分商品

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/pointsExchange/batch` | 批量操作 | `promotionPointsExchangeModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/pointsExchange/create` | 创建 | `promotionPointsExchangeModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/pointsExchange/del` | 删除 | `promotionPointsExchangeModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/pointsExchange/detail` | 获取详情 | `pointsExchangeManage` | jwt+perm |
| GET | `/adminapi/promotion/pointsExchange/list` | 获取列表 | `pointsExchangeManage` | jwt+perm |
| POST | `/adminapi/promotion/pointsExchange/update` | 更新 | `promotionPointsExchangeModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/pointsExchange/updateField` | 更新字段 | `promotionPointsExchangeModifyManage` | jwt+perm |

## adminapi / 积分签到

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/signInSetting/batch` | 批量操作签到配置 | `signInSettingModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/signInSetting/create` | 创建 | `signInSettingModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/signInSetting/del` | 删除签到配置 | `signInSettingModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/signInSetting/detail` | 获取详情 | `signInSettingManage` | jwt+perm |
| GET | `/adminapi/promotion/signInSetting/list` | 获取列表 | `signInSettingManage` | jwt+perm |
| POST | `/adminapi/promotion/signInSetting/update` | 更新 | `signInSettingModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/signInSetting/updateField` | 更新字段 | `signInSettingModifyManage` | jwt+perm |

## adminapi / 税率管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/setting/taxRate/create` | 新增税率 | `taxRateUpdateManage`, `taxRateModifyManage` | jwt+perm |
| POST | `/adminapi/setting/taxRate/del` | 删除税率 | `taxRateUpdateManage`, `taxRateModifyManage` | jwt+perm |
| GET | `/adminapi/setting/taxRate/detail` | 税率详情 | `taxRateManage` | jwt+perm |
| GET | `/adminapi/setting/taxRate/list` | 税率列表 | `taxRateManage` | jwt+perm |
| POST | `/adminapi/setting/taxRate/update` | 更新税率 | `taxRateUpdateManage`, `taxRateModifyManage` | jwt+perm |

## adminapi / 管理员列表

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/authority/adminUser/apiToken/generate` | 生成API Token | - | jwt |
| GET | `/adminapi/authority/adminUser/apiToken/list` | 查询API Token列表 | - | jwt |
| POST | `/adminapi/authority/adminUser/apiToken/revoke` | 吊销API Token | - | jwt |
| POST | `/adminapi/authority/adminUser/batch` | 批量操作 | `adminUserBatchManage` | jwt+perm |
| GET | `/adminapi/authority/adminUser/config` | 配置 | `adminUserManage`, `shopAdminUserManageConfig`, `EmployeeManagement` | jwt+perm |
| POST | `/adminapi/authority/adminUser/create` | 创建管理员 | `adminUserUpdateManage` | jwt+perm |
| POST | `/adminapi/authority/adminUser/del` | 删除管理员 | `adminUserDelManage` | jwt+perm |
| GET | `/adminapi/authority/adminUser/detail` | 管理员详情 | `adminUserManage` | jwt+perm |
| GET | `/adminapi/authority/adminUser/getCode` | 获取验证码 | - | jwt |
| GET | `/adminapi/authority/adminUser/list` | 查询管理员列表 | `adminUserManage` | jwt+perm |
| GET | `/adminapi/authority/adminUser/mineDetail` | 当前管理员详情 | - | jwt |
| POST | `/adminapi/authority/adminUser/modifyManageAccounts` | 修改管理员账号 | - | jwt |
| POST | `/adminapi/authority/adminUser/update` | 更新管理员 | `adminUserUpdateManage` | jwt+perm |

## adminapi / 管理员日志

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/authority/adminLog/list` | 管理员日志列表 | `adminLogManage`, `EmployeeLogManagement` | jwt+perm |

## adminapi / 管理员消息

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/msg/adminMsg/broadcastList` | 语音播报内容列表（含商品摘要，供播报抽屉一次性渲染） | - | jwt |
| GET | `/adminapi/msg/adminMsg/homeSummary` | 首页消息摘要（未读总数 + 最新一条） | - | jwt |
| GET | `/adminapi/msg/adminMsg/list` | 获取列表 | - | jwt |
| GET | `/adminapi/msg/adminMsg/msgCount` |  | - | jwt |
| GET | `/adminapi/msg/adminMsg/msgTypeArr` | 获取消息类别 | - | jwt |
| POST | `/adminapi/msg/adminMsg/setAllReaded` | 设置全部已读 | - | jwt |
| POST | `/adminapi/msg/adminMsg/setReaded` | 设置为单个已读 | - | jwt |

## adminapi / 素材分类

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/salesman/category/batch` | 批量操作 | - | jwt |
| POST | `/adminapi/salesman/category/create` | 创建 | `salesmanCategoryModifyManage` | jwt+perm |
| POST | `/adminapi/salesman/category/del` | 删除 | `salesmanCategoryModifyManage` | jwt+perm |
| GET | `/adminapi/salesman/category/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/salesman/category/list` | 获取列表 | - | jwt |
| POST | `/adminapi/salesman/category/update` | 更新 | `salesmanCategoryModifyManage` | jwt+perm |
| POST | `/adminapi/salesman/category/updateField` | 更新字段 | - | jwt |

## adminapi / 素材管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/salesman/material/batch` | 批量操作 | `salesmanMaterialModifyManage` | jwt+perm |
| POST | `/adminapi/salesman/material/create` | 创建 | `salesmanMaterialModifyManage` | jwt+perm |
| POST | `/adminapi/salesman/material/del` | 删除 | `salesmanMaterialModifyManage` | jwt+perm |
| GET | `/adminapi/salesman/material/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/salesman/material/list` | 获取列表 | - | jwt |
| POST | `/adminapi/salesman/material/update` | 更新 | `salesmanMaterialModifyManage` | jwt+perm |
| POST | `/adminapi/salesman/material/updateField` | 更新字段 | `salesmanMaterialModifyManage` | jwt+perm |

## adminapi / 营销管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/promotion/create` | 创建 | `promotionManage` | jwt+perm |
| GET | `/adminapi/promotion/promotion/getPromotionCount` | 获取活动数量 | `promotionManage` | jwt+perm |
| GET | `/adminapi/promotion/promotion/list` | 获取列表 | `promotionManage` | jwt+perm |
| POST | `/adminapi/promotion/promotion/update` | 更新 | `promotionManage` | jwt+perm |

## adminapi / 表单令牌校验

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/common/csrf/create` |  | - | jwt |

## adminapi / 装修导入导出模块

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/decorate/decorateShare/batch` | 批量操作 | `decorateShareBatch` | jwt+perm |
| POST | `/adminapi/decorate/decorateShare/create` | 创建 | `decorateModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/decorateShare/del` | 删除 | `decorateModifyManage` | jwt+perm |
| GET | `/adminapi/decorate/decorateShare/detail` | 获取详情 | `decorateModifyManage` | jwt+perm |
| GET | `/adminapi/decorate/decorateShare/import` | 导入 | - | jwt |
| GET | `/adminapi/decorate/decorateShare/list` | 获取列表 | - | jwt |
| GET | `/adminapi/decorate/decorateShare/share` | 分享 | - | jwt |
| POST | `/adminapi/decorate/decorateShare/update` | 更新 | `decorateModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/decorateShare/updateField` | 更新字段 | `decorateModifyManage` | jwt+perm |

## adminapi / 装修推广模块

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/decorate/promote/generateShortLink` | 根据长链接生成装修推广短链 | - | jwt |
| POST | `/adminapi/decorate/promote/saveShareInfo` | 保存装修分享信息 | - | jwt |
| GET | `/adminapi/decorate/promote/shareInfo` | 获取装修分享信息 | - | jwt |

## adminapi / 装修模块管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/decorate/decorateDiscrete/detail` | 获取详情 | `pcDecorateOtherManage`, `mobileShopDecorate` | jwt+perm |
| GET | `/adminapi/decorate/decorateDiscrete/memberDecorateData` | 个人中心基础数据 | `pcDecorateOtherManage`, `mobileShopDecorate` | jwt+perm |
| POST | `/adminapi/decorate/decorateDiscrete/update` | 更新 | `decorateDiscreteModifyManage` | jwt+perm |

## adminapi / 装修模板库

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/decorate/template/category` | 模板分类树 | - | jwt |
| GET | `/adminapi/decorate/template/detail` | 模板页面详情 | - | jwt |
| GET | `/adminapi/decorate/template/list` | 模板页面列表 | - | jwt |

## adminapi / 装修管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/decorate/decorate/batch` | 批量操作 | `decorateModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/decorate/bindingCustomDecorate` | 绑定已发布的页面 | `decorateModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/decorate/bindingH5Decorate` | 绑定已发布的H5页面 | `decorateModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/decorate/cancelBinding` | 绑定已发布的页面 | `decorateModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/decorate/copy` | 复制 | - | jwt |
| POST | `/adminapi/decorate/decorate/create` | 创建 | `decorateCreateManage` | jwt+perm |
| POST | `/adminapi/decorate/decorate/del` | 删除 | `decorateModifyManage` | jwt+perm |
| GET | `/adminapi/decorate/decorate/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/decorate/decorate/getCustomDecorateList` | H5获取自定义装修列表 | - | jwt |
| GET | `/adminapi/decorate/decorate/getH5DecorateList` | 获取H5装修列表 | - | jwt |
| GET | `/adminapi/decorate/decorate/list` | 获取列表 | - | jwt |
| GET | `/adminapi/decorate/decorate/loadDraftData` | 加载草稿 | - | jwt |
| POST | `/adminapi/decorate/decorate/publish` | 发布装修 | `decoratePublishManage` | jwt+perm |
| POST | `/adminapi/decorate/decorate/saveDraft` | 保存装修草稿 | - | jwt |
| POST | `/adminapi/decorate/decorate/setHome` | 设置为首页 | `decorateSetHomeManage` | jwt+perm |
| POST | `/adminapi/decorate/decorate/update` | 更新 | `decorateModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/decorate/updateCustomDecorate` | 自定义页面更新 | `decorateModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/decorate/updateField` | 更新字段 | `decorateModifyManage` | jwt+perm |

## adminapi / 视频号账号

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/content/wechatVideoAccount/create` | 新增视频号账号 | `videoNumberModifyManage` | jwt+perm |
| POST | `/adminapi/content/wechatVideoAccount/delete` | 删除视频号账号 | `videoNumberModifyManage` | jwt+perm |
| GET | `/adminapi/content/wechatVideoAccount/detail` | 获取视频号账号详情 | `videoNumber` | jwt+perm |
| GET | `/adminapi/content/wechatVideoAccount/list` | 获取视频号账号列表 | `videoNumber` | jwt+perm |
| POST | `/adminapi/content/wechatVideoAccount/update` | 更新视频号账号 | `videoNumberModifyManage` | jwt+perm |

## adminapi / 角色

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/authority/adminRole/batch` | 批量操作 | `adminRoleUpdateManage` | jwt+perm |
| POST | `/adminapi/authority/adminRole/create` | 创建角色 | `adminRoleUpdateManage` | jwt+perm |
| POST | `/adminapi/authority/adminRole/del` | 删除角色 | `adminRoleDelManage` | jwt+perm |
| GET | `/adminapi/authority/adminRole/detail` | 角色详情 | `adminRoleManage`, `ShopRoleManage`, `vendorRoleManage` | jwt+perm |
| GET | `/adminapi/authority/adminRole/list` | 角色列表 | `adminRoleManage`, `ShopRoleManage`, `vendorRoleManage` | jwt+perm |
| POST | `/adminapi/authority/adminRole/update` | 更新角色 | `adminRoleUpdateManage` | jwt+perm |
| POST | `/adminapi/authority/adminRole/updateField` | 更新字段 | `adminRoleUpdateManage` | jwt+perm |

## adminapi / 订单商品

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/order/orderLog/create` | 创建 | `orderLogModifyManage` | jwt+perm |
| GET | `/adminapi/order/orderLog/list` | 获取列表 | - | jwt |

## adminapi / 订单管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/order/order/batch` | 批量操作 | `splitStoreOrderManage` | jwt+perm |
| POST | `/adminapi/order/order/batchDeliverImport` | 批量发货Excel导入 | `orderDeliverManage` | jwt+perm |
| POST | `/adminapi/order/order/cancelOrder` | 取消订单 | `cancelOrderManage` | jwt+perm |
| POST | `/adminapi/order/order/cancelOrderByAdmin` | 后台取消订单 | - | jwt |
| POST | `/adminapi/order/order/changeOrderStatus` | 修改订单状态 | - | jwt |
| POST | `/adminapi/order/order/confirmReceipt` | 确认收货 | `orderConfirmReceiptManage` | jwt+perm |
| POST | `/adminapi/order/order/delOrder` | 删除订单 | `delOrderManage` | jwt+perm |
| POST | `/adminapi/order/order/deliver` | 发货 | `orderDeliverManage` | jwt+perm |
| GET | `/adminapi/order/order/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/order/order/downloadDeliverTemplate` | 下载批量发货模板 | - | jwt |
| GET | `/adminapi/order/order/exportItemInfo` | 标签详情 | - | jwt |
| POST | `/adminapi/order/order/getAddProductInfo` | 添加商品时获取商品信息 | - | jwt |
| POST | `/adminapi/order/order/getBatchDeliverImportErrorData` | 获取批量发货导入失败的数据（导出错误Excel） | `orderDeliverManage` | anonymous |
| GET | `/adminapi/order/order/getExportItemList` | 订单导出标签列表 | - | jwt |
| GET | `/adminapi/order/order/getOrderPageConfig` | 获取订单列表配置 | - | jwt |
| GET | `/adminapi/order/order/list` | 获取列表 | - | jwt |
| POST | `/adminapi/order/order/modifyConsignee` | 修改收货人信息 | `orderModifyConsigneeManage` | jwt+perm |
| POST | `/adminapi/order/order/modifyMoney` | 修改订单金额 | `orderModifyMoneyManage` | jwt+perm |
| POST | `/adminapi/order/order/modifyProduct` | 修改商品信息 | `modifyProductManage` | jwt+perm |
| POST | `/adminapi/order/order/modifyShipping` | 修改配送信息 | `orderModifyShippingManage` | jwt+perm |
| GET | `/adminapi/order/order/orderExport` | 订单导出 | - | jwt |
| GET | `/adminapi/order/order/orderPrint` | 打印订单 | - | jwt |
| GET | `/adminapi/order/order/orderPrintBill` | 打印电子面单 | - | jwt |
| GET | `/adminapi/order/order/parentDetail` | 获取父订单详情 | - | jwt |
| GET | `/adminapi/order/order/printSeveral` | 批量打印 | - | jwt |
| POST | `/adminapi/order/order/remindDeliver` | 催发货 | `remindDeliver` | jwt+perm |
| POST | `/adminapi/order/order/saveExportItem` | 保存导出标签信息 | - | jwt |
| POST | `/adminapi/order/order/setAdminNote` | 设置管理员备注 | `setAdminNoteManage` | jwt+perm |
| POST | `/adminapi/order/order/setConfirm` | 设置订单确认 | `setConfirmManage` | jwt+perm |
| POST | `/adminapi/order/order/setPaid` | 设置已付款 | `setPaidManage` | jwt+perm |
| GET | `/adminapi/order/order/severalDetail` | 批量详情 | - | jwt |
| GET | `/adminapi/order/order/shippingInfo` | 物流信息 | - | jwt |
| POST | `/adminapi/order/order/splitStoreOrder` | 订单拆分 | `splitStoreOrderManage` | jwt+perm |
| GET | `/adminapi/order/order/statusCount` | 统计各订单状态的数量 | - | jwt |

## adminapi / 订单配置

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/shop/orderConfig/batch` | 批量操作 | - | jwt |
| POST | `/adminapi/shop/orderConfig/create` | 创建 | - | jwt |
| POST | `/adminapi/shop/orderConfig/del` | 删除 | - | jwt |
| GET | `/adminapi/shop/orderConfig/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/shop/orderConfig/list` | 获取列表 | - | jwt |
| POST | `/adminapi/shop/orderConfig/update` | 更新 | - | jwt |
| POST | `/adminapi/shop/orderConfig/updateField` | 更新字段 | - | jwt |

## adminapi / 许可证

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/setting/licensed/index` | 授权信息 | - | jwt |
| POST | `/adminapi/setting/licensed/saveLicensed` | 保存授权 | `licensed` | jwt+perm |
| POST | `/adminapi/setting/licensed/update` | 更新授权 | `licensedModifyManage` | jwt+perm |

## adminapi / 设置

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/setting/config/afterSalesSettings` | 获取售后配置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/aliPaySettings` | 获取系统设置-支付宝支付设置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/amapAllSettings` | 获取web Amap 配置 | - | jwt |
| GET | `/adminapi/setting/config/authSettings` | 获取会员认证配置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/basicConfig` | 获取基础配置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/basicPaySettings` | 获取系统设置-支付基础设置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/basicSettings` | 获取商城设置-基础信息 | `configManage`, `config` | jwt+perm |
| GET | `/adminapi/setting/config/buyerShowSettings` | 获取买家秀配置 | `buyerShowManage` | jwt+perm |
| GET | `/adminapi/setting/config/categoryDecorateSettings` | 获取分类页装修配置 | `config`, `decorate` | jwt+perm |
| GET | `/adminapi/setting/config/commissionWithdrawalSettings` | 获取分销佣金提现配置 | - | jwt |
| POST | `/adminapi/setting/config/createPlatformCertificate` | 生成平台证书 | `uploadFileModifyManage` | jwt+perm |
| POST | `/adminapi/setting/config/generateWorkbuddyKey` | 生成 MCP 密钥 | `saveSettingsGlobalManage` | jwt+perm |
| GET | `/adminapi/setting/config/getAdmin` | 根据配置编码获取配置 | - | jwt |
| GET | `/adminapi/setting/config/getAdminBase` | admin 初始化配置 | - | jwt |
| GET | `/adminapi/setting/config/getBase` | 根据配置编码获取配置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/getCashierOrderSettings` | 获取收银台订单设置 | - | jwt |
| GET | `/adminapi/setting/config/getDingConfig` | 获取钉钉配置 | - | jwt |
| GET | `/adminapi/setting/config/getIcon` | 获取图标icon | `config` | jwt+perm |
| GET | `/adminapi/setting/config/getLoginProtocol` | 获取登录协议 | - | anonymous |
| GET | `/adminapi/setting/config/getLoginProtocolContent` | 获取登录协议内容  termsOfService:服务协议；privacyPolicy:隐私政策；afterSalesService:售后服务 | - | anonymous |
| GET | `/adminapi/setting/config/getPersonalizedSetting` | 获取个性化设置 | - | jwt |
| GET | `/adminapi/setting/config/getWXWorkConfig` | 获取企业微信配置 | - | jwt |
| GET | `/adminapi/setting/config/globalSettings` | 获取系统设置-全局设置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/kefuSettings` | 获取商城设置-客服设置 | `configManage`, `config`, `kefuSetting` | jwt+perm |
| GET | `/adminapi/setting/config/loginSettings` | 获取系统设置-登录设置 | `config` | anonymous |
| GET | `/adminapi/setting/config/mailSettings` | 获取系统设置-邮箱配置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/mapSettings` | 获取地图服务商配置 | - | jwt |
| GET | `/adminapi/setting/config/memberCenterDecorateSettings` | 获取个人中心页装修配置 | `mobileUserManage` | jwt+perm |
| GET | `/adminapi/setting/config/memberWithdrawalSettings` | 获取会员余额提现配置 | - | jwt |
| GET | `/adminapi/setting/config/merchantSettings` | 获取商户配置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/miniProgramRiskSettings` | 获取小程序风控配置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/mobileSearchStyleSettings` | 获取移动端搜索风格配置 | `config`, `decorate` | jwt+perm |
| GET | `/adminapi/setting/config/notifySettings` | 获取消息设置-通知配置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/offlinePaySettings` | 获取系统设置-线下支付设置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/orderSettings` | 获取商城设置-订单设置 | `configManage`, `config` | jwt+perm |
| GET | `/adminapi/setting/config/payPalSettings` | 获取系统设置-PayPal支付设置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/productDetailStyleSettings` | 获取商品详情页风格配置 | `config`, `decorate` | jwt+perm |
| GET | `/adminapi/setting/config/productSettings` | 获取商城设置-商品设置 | `configManage`, `config`, `productManage` | jwt+perm |
| GET | `/adminapi/setting/config/profitSharingSettings` | 获取结算与分账配置 | - | jwt |
| POST | `/adminapi/setting/config/revealWorkbuddyKey` | 复制 MCP 密钥 | `isPlatformSuperAdmin` | jwt+perm |
| POST | `/adminapi/setting/config/saveAfterSales` | 保存售后配置 | `settingSaveAfterSalesManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveAliPay` | 保存系统设置-支付宝支付设置 | `uploadFileModifyManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveAllAmapSettings` | 保存web Amap 配置 | `storeSettingsUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveAuth` | 保存会员认证配置 | `saveAuthSettingsManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveBasic` | 保存商城设置-基础信息 | `saveBasicManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveBasicPay` | 保存系统设置-支付基础设置 | `uploadFileModifyManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveBuyerShow` | 保存买家秀配置 | `buyerShowSettingsUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveCashierOrderSettings` | 保存收银台订单设置 | - | jwt |
| POST | `/adminapi/setting/config/saveCategoryDecorate` | 保存分类页装修配置 | `themeCategoryDecorateManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveCommissionWithdrawal` | 保存分销佣金提现配置 | - | jwt |
| POST | `/adminapi/setting/config/saveDingConfig` | 保存钉钉配置 | - | jwt |
| POST | `/adminapi/setting/config/saveGlobal` | 保存系统设置-全局设置 | `saveSettingsGlobalManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveKefu` | 保存商城设置-客服设置 | `saveSettingKefuManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveLogin` | 保存系统设置-登录设置 | `saveSettingsSaveLoginManage` | anonymous |
| POST | `/adminapi/setting/config/saveLoginProtocol` | 保存登录协议 | `protocolUpdateManagement` | anonymous |
| POST | `/adminapi/setting/config/saveMail` | 保存系统设置-邮箱配置 | `mailTemplatesUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveMapSettings` | 保存地图服务商配置 | `storeSettingsUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveMemberCenterDecorate` | 保存个人中心页装修配置 | `mobileUserManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveMemberWithdrawal` | 保存会员余额提现配置 | - | jwt |
| POST | `/adminapi/setting/config/saveMerchant` | 保存商户配置 | `adminMerchantSettingsUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveMiniProgramRisk` | 保存小程序风控配置 | `uploadFileModifyManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveMobileSearchStyle` | 保存移动端搜索风格配置 | `mobileSearchStyleSaveManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveNotify` | 保存消息设置-通知配置 | `saveSettingNotifyManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveOfflinePay` | 保存系统设置-线下支付设置 | `uploadFileModifyManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveOrder` | 保存商城设置-订单设置 | `saveSettingsOrderManage` | jwt+perm |
| POST | `/adminapi/setting/config/savePayPal` | 保存系统设置-PayPal支付设置 | `uploadFileModifyManage` | jwt+perm |
| POST | `/adminapi/setting/config/savePersonalizedSetting` | 保存个性化设置 | - | jwt |
| POST | `/adminapi/setting/config/saveProduct` | 保存商城设置-商品设置 | `saveSettingProductManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveProductDetailStyle` | 保存商品详情页风格配置 | `pcProductStyleSaveManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveProfitSharing` | 保存结算与分账配置 | `profitsharingSettingUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveSearch` | 保存商品搜索配置 | - | jwt |
| POST | `/adminapi/setting/config/saveSearchStyle` | 保存搜索页风格配置 | `pcSearchStyleSaveManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveShipping` | 保存配送设置-配送设置 | `settingSaveShippingManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveShop` | 保存店铺配置 | `shopSettingsUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveShopping` | 保存商城设置-交易设置 | `saveSettingShoppingManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveStore` | 保存门店配置 | `storeSettingsUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveStoreSelfOperated` | 保存平台门店配置 | `storeSettingsUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveThemeStyle` | 保存主题风格配置 | `themeStyleUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveVendor` | 保存供应商配置 | `vendorSettingUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveWXWorkConfig` | 保存企业微信配置 | - | jwt |
| POST | `/adminapi/setting/config/saveWechatLive` | 保存小程序直播/视频号独立小程序配置 | `uploadFileModifyManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveWechatPay` | 保存系统设置-微信支付设置 | `uploadFileModifyManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveWithdrawal` | 保存提现配置 | - | jwt |
| POST | `/adminapi/setting/config/saveYaBandPay` | 保存系统设置-YaBandPay支付设置 | `uploadFileModifyManage` | jwt+perm |
| POST | `/adminapi/setting/config/saveYunPay` | 保存系统设置-云支付设置 | `uploadFileModifyManage` | jwt+perm |
| GET | `/adminapi/setting/config/searchSettings` | 商品搜索配置 | - | jwt |
| GET | `/adminapi/setting/config/searchStyleSettings` | 获取搜索页风格配置 | `config`, `decorate` | jwt+perm |
| POST | `/adminapi/setting/config/sendTestEmail` | 发送测试邮件 | `sendTestEmailModifyManage` | jwt+perm |
| GET | `/adminapi/setting/config/shippingSettings` | 获取配送设置-配送设置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/shopSettings` | 获取店铺配置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/shoppingSettings` | 获取商城设置-交易设置 | `configManage`, `config` | jwt+perm |
| GET | `/adminapi/setting/config/storeSelfOperatedSettings` | 获取平台门店配置 | - | jwt |
| GET | `/adminapi/setting/config/storeSettings` | 获取门店配置 | - | jwt |
| GET | `/adminapi/setting/config/themeStyleSettings` | 获取主题风格配置 | `config`, `decorate` | jwt+perm |
| POST | `/adminapi/setting/config/uploadFile` | 上传密钥文件 | `uploadFileModifyManage` | jwt+perm |
| GET | `/adminapi/setting/config/vendorSettings` | 获取供应商配置 | `vendorSettingManage` | jwt+perm |
| GET | `/adminapi/setting/config/wechatLiveSettings` | 获取小程序直播/视频号独立小程序配置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/wechatPaySettings` | 获取系统设置-微信支付设置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/withdrawalSettings` | 获取提现配置 | - | jwt |
| GET | `/adminapi/setting/config/workbuddySettings` | 获取 MCP 密钥设置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/yaBandPaySettings` | 获取系统设置-YaBandPay支付设置 | `config` | jwt+perm |
| GET | `/adminapi/setting/config/yunPaySettings` | 获取系统设置-云支付设置 | `config` | jwt+perm |

## adminapi / 设置-Apple Pay支付

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/setting/config/applePaySettings` | 获取系统设置-Apple Pay支付设置 | `config` | jwt+perm |
| POST | `/adminapi/setting/config/saveApplePay` | 保存系统设置-Apple Pay支付设置 | `uploadFileModifyManage` | jwt+perm |

## adminapi / 设置-拉卡拉支付

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/setting/config/lakalaSettings` | 获取系统设置-拉卡拉支付设置 | `config` | jwt+perm |
| POST | `/adminapi/setting/config/saveLakala` | 保存系统设置-拉卡拉支付设置 | `uploadFileModifyManage` | jwt+perm |

## adminapi / 设置-易宝支付

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/setting/config/saveYeepay` | 保存系统设置-易宝支付设置 | `uploadFileModifyManage` | jwt+perm |
| GET | `/adminapi/setting/config/yeepaySettings` | 获取系统设置-易宝支付设置 | `config` | jwt+perm |

## adminapi / 设置-汇付斗拱支付

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/setting/config/huifuSettings` | 获取系统设置-汇付斗拱支付设置 | `config` | jwt+perm |
| POST | `/adminapi/setting/config/saveHuifu` | 保存系统设置-汇付斗拱支付设置 | `uploadFileModifyManage` | jwt+perm |

## adminapi / 访问统计控制器

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/panel/statisticsAccess/accessStatistics` | 访问统计数据 | `statisticsAccess` | jwt+perm |
| GET | `/adminapi/panel/statisticsUser/addUserTrends` | 新增会员趋势 | - | jwt |
| GET | `/adminapi/panel/statisticsUser/userConsumptionRanking` | 会员消费排行 | - | jwt |
| GET | `/adminapi/panel/statisticsUser/userStatisticsPanel` | 用户统计面板 | - | jwt |

## adminapi / 评价管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/product/comment/batch` | 批量操作 | `commentModifyManage` | jwt+perm |
| POST | `/adminapi/product/comment/create` | 创建 | `commentModifyManage` | jwt+perm |
| POST | `/adminapi/product/comment/del` | 删除 | `commentModifyManage` | jwt+perm |
| GET | `/adminapi/product/comment/detail` | 获取详情 | `commentManage` | jwt+perm |
| GET | `/adminapi/product/comment/list` | 获取列表 | `commentManage` | jwt+perm |
| POST | `/adminapi/product/comment/replyComment` | 回复评论 | `commentModifyManage` | jwt+perm |
| POST | `/adminapi/product/comment/update` | 更新 | `commentModifyManage` | jwt+perm |
| POST | `/adminapi/product/comment/updateField` | 更新字段 | `commentModifyManage` | jwt+perm |

## adminapi / 账号管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/admin/adminAccount/bindMainAccount` | 根据主账号ID和店铺ID/供应商ID绑定账号 | `adminAccountBindMainAccount` | jwt+perm |
| POST | `/adminapi/admin/adminAccount/deleteAdminUser` | 删除管理员账号 | `adminAccountUpdateManager` | jwt+perm |
| GET | `/adminapi/admin/adminAccount/getMainAccount` | 根据店铺或供应商ID查询主账号信息 | - | jwt |
| GET | `/adminapi/admin/adminAccount/pageAdminUser` | 账号管理列表 | - | jwt |
| GET | `/adminapi/admin/adminAccount/pageShopOrVendor` | 根据主账号ID和类型查询账号列表 | - | jwt |
| POST | `/adminapi/admin/adminAccount/updateMainAccount` | 修改主账号信息 | `adminAccountUpdateManager` | jwt+perm |
| POST | `/adminapi/admin/adminAccount/updateMainAccountPwd` | 修改主账号密码 | `adminAccountUpdateManager` | jwt+perm |

## adminapi / 账户总览

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/finance/accountPanel/list` | 获取数据 | - | jwt |

## adminapi / 账期结算管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/credit/settlement/cancel` | 撤销结算 | `termPayModifyManage` | jwt+perm |
| GET | `/adminapi/credit/settlement/history` | 历史结算列表 | `termPayManage` | jwt+perm |
| GET | `/adminapi/credit/settlement/historyOrders` | 历史结算订单明细 | `termPayManage` | jwt+perm |
| GET | `/adminapi/credit/settlement/pendingOrders` | 待结算订单列表 | `termPayManage` | jwt+perm |
| POST | `/adminapi/credit/settlement/submit` | 确认结算 | `termPayModifyManage` | jwt+perm |
| GET | `/adminapi/credit/settlement/userList` | 结算会员列表 | `termPayManage` | jwt+perm |

## adminapi / 账期订单管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/credit/order/list` | 账期订单列表 | `termOrderManage` | jwt+perm |

## adminapi / 运费模板

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/setting/shippingTpl/batch` | 批量操作 | `shippingTplBatchManage` | jwt+perm |
| GET | `/adminapi/setting/shippingTpl/config` | 获取运费模版 | `shippingTplManage`, `shopShippingTplManage` | jwt+perm |
| POST | `/adminapi/setting/shippingTpl/create` | 创建 | `shippingTplUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/shippingTpl/del` | 删除 | `shippingTplDelManage` | jwt+perm |
| GET | `/adminapi/setting/shippingTpl/detail` | 获取详情 | `shippingTplManage`, `shopShippingTplManage` | jwt+perm |
| GET | `/adminapi/setting/shippingTpl/list` | 获取列表 | `shippingTplManage`, `shopShippingTplManage` | jwt+perm |
| POST | `/adminapi/setting/shippingTpl/setDefault` | 设置默认运费模板 | `shippingTplUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/shippingTpl/update` | 更新 | `shippingTplUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/shippingTpl/updateField` | 更新字段 | `shippingTplUpdateManage` | jwt+perm |

## adminapi / 进件管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/finance/merchantApplication/apply` | 提交进件申请（店铺/供应商端） | - | jwt |
| POST | `/adminapi/finance/merchantApplication/approve` | 审核通过（提交微信进件） | `merchantApplicationApprove` | jwt+perm |
| GET | `/adminapi/finance/merchantApplication/current` | 当前登录主体最新进件申请（店铺/供应商端） | - | jwt |
| GET | `/adminapi/finance/merchantApplication/detail` | 进件申请详情 | `merchantApplicationManage` | jwt+perm |
| GET | `/adminapi/finance/merchantApplication/list` | 进件申请分页列表（平台审核列表） | `merchantApplicationManage` | jwt+perm |
| GET | `/adminapi/finance/merchantApplication/media` | 进件证件原图预览 | `merchantApplicationManage` | jwt+perm |
| POST | `/adminapi/finance/merchantApplication/refresh` | 刷新进件状态 | `merchantApplicationRefresh` | jwt+perm |
| POST | `/adminapi/finance/merchantApplication/reject` | 审核拒绝 | `merchantApplicationReject` | jwt+perm |
| POST | `/adminapi/finance/merchantApplication/submit` | 平台直接提交进件（进件管理） | `merchantApplicationSubmit` | jwt+perm |
| POST | `/adminapi/finance/merchantApplication/uploadMedia` | 上传进件媒体文件 | - | jwt |

## adminapi / 退款申请

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/finance/refundApply/audit` | 审核 | `refundApplyUpdateManage` | jwt+perm |
| POST | `/adminapi/finance/refundApply/batch` | 批量操作 | - | jwt |
| GET | `/adminapi/finance/refundApply/config` | 获取审核状态配置 | - | jwt |
| POST | `/adminapi/finance/refundApply/create` | 创建 | - | jwt |
| POST | `/adminapi/finance/refundApply/del` | 删除 | - | jwt |
| GET | `/adminapi/finance/refundApply/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/finance/refundApply/list` | 获取列表 | - | jwt |
| POST | `/adminapi/finance/refundApply/offlineAudit` | 确认线下转账 | `refundApplyUpdateManage` | jwt+perm |
| POST | `/adminapi/finance/refundApply/update` | 更新 | `refundApplyUpdateManage` | jwt+perm |
| POST | `/adminapi/finance/refundApply/updateField` | 更新字段 | - | jwt |

## adminapi / 退款记录

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/finance/refundLog/batch` | 批量操作 | - | jwt |
| POST | `/adminapi/finance/refundLog/create` | 创建 | - | jwt |
| POST | `/adminapi/finance/refundLog/del` | 删除 | - | jwt |
| GET | `/adminapi/finance/refundLog/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/finance/refundLog/list` | 获取列表 | - | jwt |
| POST | `/adminapi/finance/refundLog/update` | 更新 | - | jwt |
| POST | `/adminapi/finance/refundLog/updateField` | 更新字段 | - | jwt |

## adminapi / 邮箱模板管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/setting/mailTemplates/list` | 邮箱模板列表 | `mailTemplateManage` | jwt+perm |
| POST | `/adminapi/setting/mailTemplates/update` | 更新邮件模板 | `mailTemplatesUpdateManage` | jwt+perm |

## adminapi / 配置管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/common/cacheManage/cleanup` | 清除缓存 | - | jwt |

## adminapi / 配送方式配置信息

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/setting/logisticsCompany/batch` | 批量操作 | `logisticsCompanyBatchManage` | jwt+perm |
| POST | `/adminapi/setting/logisticsCompany/create` | 创建 | `logisticsCompanyUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/logisticsCompany/del` | 删除 | `logisticsCompanyDelManage` | jwt+perm |
| GET | `/adminapi/setting/logisticsCompany/detail` | 获取详情 | `logisticsCompanyManage`, `orderManage`, `posOrder`, `vendorOrderManage`, `logisticsCompanyConfigManage`, `shopLogisticsCompanyConfigManage`, `vendorLogisticsCompanyConfigManage`, `posWorkbench` | jwt+perm |
| GET | `/adminapi/setting/logisticsCompany/list` | 获取列表 | `logisticsCompanyManage`, `orderManage`, `posOrder`, `vendorOrderManage`, `logisticsCompanyConfigManage`, `shopLogisticsCompanyConfigManage`, `vendorLogisticsCompanyConfigManage`, `posWorkbench` | jwt+perm |
| POST | `/adminapi/setting/logisticsCompany/syncKdn` | 同步快递鸟物流公司到系统 | `logisticsCompanyUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/logisticsCompany/syncKuaidi100` | 同步快递100物流公司到系统 | `logisticsCompanyUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/logisticsCompany/update` | 更新 | `logisticsCompanyUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/logisticsCompany/updateField` | 更新字段 | `logisticsCompanyUpdateManage` | jwt+perm |

## adminapi / 配送类型

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/setting/shippingType/batch` | 批量操作 | `shippingTypeBatchManage` | jwt+perm |
| POST | `/adminapi/setting/shippingType/create` | 创建 | `shippingTypeUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/shippingType/del` | 删除 | `shippingTypeDelManage` | jwt+perm |
| GET | `/adminapi/setting/shippingType/detail` | 获取详情 | `shippingTypeManage` | jwt+perm |
| GET | `/adminapi/setting/shippingType/list` | 获取列表 | `shippingTypeManage` | jwt+perm |
| POST | `/adminapi/setting/shippingType/update` | 更新 | `shippingTypeUpdateManage` | jwt+perm |
| POST | `/adminapi/setting/shippingType/updateField` | 更新字段 | `shippingTypeUpdateManage` | jwt+perm |

## adminapi / 鍟嗗搧璇环

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/product/priceInquiry/batch` | 鎵归噺鎿嶄綔 | `priceInquiryModifyManage` | jwt+perm |
| POST | `/adminapi/product/priceInquiry/del` | 鍒犻櫎 | `priceInquiryModifyManage` | jwt+perm |
| GET | `/adminapi/product/priceInquiry/detail` | 鑾峰彇璇︽儏 | - | jwt |
| GET | `/adminapi/product/priceInquiry/list` | 鑾峰彇鍒楄〃 | - | jwt |
| POST | `/adminapi/product/priceInquiry/reply` | 鍥炲 | `priceInquiryModifyManage` | jwt+perm |

## adminapi / 鎷煎洟娲诲姩鍟嗗搧绠＄悊

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/groupon/item/batch` | 鎵归噺鎿嶄綔 | `grouponPromotionManage` | jwt+perm |
| POST | `/adminapi/promotion/groupon/item/create` | 鍒涘缓 | `grouponPromotionModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/groupon/item/del` | 鍒犻櫎 | `grouponPromotionModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/groupon/item/detail` | 鑾峰彇璇︽儏 | `grouponPromotionManage` | jwt+perm |
| GET | `/adminapi/promotion/groupon/item/list` | 鑾峰彇鍒楄〃 | `grouponPromotionManage` | jwt+perm |
| GET | `/adminapi/promotion/groupon/item/post` | 鎺ㄥ箍 | `grouponPromotionManage` | jwt+perm |
| GET | `/adminapi/promotion/groupon/item/productList` | 鎷煎洟鍟嗗搧鍒楄〃 | `grouponPromotionManage` | jwt+perm |
| POST | `/adminapi/promotion/groupon/item/update` | 鏇存柊 | `grouponPromotionModifyManage` | jwt+perm |

## adminapi / 销售统计控制器

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/panel/salesStatistics/list` | 销售统计数据 | `statisticsOrder` | jwt+perm |
| GET | `/adminapi/panel/salesStatistics/salesDetail` | 销售明细 | `statisticsSale` | jwt+perm |
| GET | `/adminapi/panel/salesStatistics/salesIndicators` | 销售指标 | `saleTargets` | jwt+perm |
| GET | `/adminapi/panel/salesStatistics/salesProductDetail` | 销售商品明细 | `statisticsSale` | jwt+perm |
| GET | `/adminapi/panel/salesStatistics/salesRanking` | 销售排行 | `consumerRanking`, `statisticsGoods` | jwt+perm |

## adminapi / 限时折扣

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/timeDiscount/batch` | 批量操作 | `timeDiscountModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/timeDiscount/create` | 创建 | `timeDiscountModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/timeDiscount/del` | 删除 | `timeDiscountModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/timeDiscount/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/promotion/timeDiscount/list` | 获取列表 | - | jwt |
| POST | `/adminapi/promotion/timeDiscount/update` | 更新 | `timeDiscountModifyManage` | jwt+perm |

## adminapi / 限时秒杀

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/promotion/seckill/batch` | 批量操作 | `seckillModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/seckill/create` | 创建 | `seckillModifyManage` | jwt+perm |
| POST | `/adminapi/promotion/seckill/del` | 删除 | `seckillModifyManage` | jwt+perm |
| GET | `/adminapi/promotion/seckill/detail` | 获取详情 | `seckillManage` | jwt+perm |
| GET | `/adminapi/promotion/seckill/list` | 获取列表 | `seckillManage` | jwt+perm |
| GET | `/adminapi/promotion/seckill/listForDecorate` | 获取秒杀商品列表 | `seckillManage` | jwt+perm |
| GET | `/adminapi/promotion/seckill/listForHomePreview` | 装修预览：秒杀商品列表（与 C 端首页同口径） | `seckillManage` | jwt+perm |
| POST | `/adminapi/promotion/seckill/update` | 更新 | `seckillModifyManage` | jwt+perm |

## adminapi / 面板控制器

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/adminapi/panel/panel/index` | 首页面板 | `consoleManage` | jwt+perm |
| POST | `/adminapi/panel/panel/saveShortcutMenu` | 保存首页快捷功能配置 | - | jwt |
| GET | `/adminapi/panel/panel/searchMenu` | 一键直达 | `consoleManage` | jwt+perm |
| GET | `/adminapi/panel/panel/shortcutMenu` | 获取首页快捷功能配置 | - | jwt |
| GET | `/adminapi/panel/panel/statisticalData` | 首页趋势图表数据（按天数筛选） | `consoleManage` | jwt+perm |
| GET | `/adminapi/panel/panel/vendorIndex` | 首页供应商面板 | `consoleVendorManage` | jwt+perm |
| GET | `/adminapi/panel/panel/vendorStatisticalData` | 首页供应商收益统计图表数据（按天数筛选） | `consoleVendorManage` | jwt+perm |

## adminapi / 首页分类栏（H5）

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/adminapi/decorate/mobileCatNav/batch` | 批量操作 | `mobileCatNavModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/mobileCatNav/create` | 创建 | `mobileCatNavModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/mobileCatNav/del` | 删除 | `mobileCatNavModifyManage` | jwt+perm |
| GET | `/adminapi/decorate/mobileCatNav/detail` | 获取详情 | - | jwt |
| GET | `/adminapi/decorate/mobileCatNav/list` | 获取列表 | - | jwt |
| POST | `/adminapi/decorate/mobileCatNav/update` | 更新 | `mobileCatNavModifyManage` | jwt+perm |
| POST | `/adminapi/decorate/mobileCatNav/updateField` | 更新字段 | `mobileCatNavModifyManage` | jwt+perm |

## api / C端-批发

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/wholesale/cart/addBatch` | 批量加车（仅批发商） | - | optional |
| GET | `/api/wholesale/me` | 我是否批发商 | - | optional |
| GET | `/api/wholesale/product/buyRule` | 商品起批规则 | - | optional |

## api / PC

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/common/pc/getCatFloor` | 获取阶梯菜单 | - | optional |
| GET | `/api/common/pc/getNav` | 获取导航 | - | optional |

## api / seo

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/sys/seo/articles` | 站点地图-文章列表 | - | optional |
| GET | `/api/sys/seo/brands` | 站点地图-品牌列表 | - | optional |
| GET | `/api/sys/seo/categories` | 站点地图-分类列表 | - | optional |
| GET | `/api/sys/seo/getCategoryIdBySeoLink` | 商品分类seoLink转商品分类ID | - | optional |
| GET | `/api/sys/seo/products` | 站点地图-商品列表 | - | optional |

## api / 买家秀

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/buyerShow/buyerShow/add` | 添加买家秀信息 | - | optional |
| POST | `/api/buyerShow/buyerShow/comment` | 评论买家秀信息 | - | optional |
| POST | `/api/buyerShow/buyerShow/comment/like` | 买家秀评论点赞/取消点赞 | - | optional |
| GET | `/api/buyerShow/buyerShow/comment/pageList` | 评论分页列表 | - | optional |
| GET | `/api/buyerShow/buyerShow/detail` | 获取买家秀详情 | - | optional |
| POST | `/api/buyerShow/buyerShow/favorite` | 买家秀收藏/取消收藏 | - | optional |
| POST | `/api/buyerShow/buyerShow/followAuthor` | 关注/取关买家秀作者 | - | optional |
| GET | `/api/buyerShow/buyerShow/groupList` | 分组列表 | - | optional |
| POST | `/api/buyerShow/buyerShow/like` | 买家秀点赞/取消点赞 | - | optional |
| GET | `/api/buyerShow/buyerShow/list` | 获取买家秀列表 | - | optional |
| GET | `/api/buyerShow/buyerShow/share` | 买家秀分享落地页 | - | optional |
| GET | `/api/buyerShow/buyerShow/videoFeed` | 买家秀视频流（只返回含视频的买家秀，游标翻页） | - | optional |

## api / 分销业绩订单结算

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/salesman/order/list` | 获取列表 | - | optional |

## api / 分销内容管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/salesman/content/detail` | 获取详情 | - | optional |
| GET | `/api/salesman/content/list` | 获取列表 | - | optional |

## api / 分销员分组

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/salesman/material/category` | 获取分类 | - | optional |
| GET | `/api/salesman/material/detail` | 获取详情 | - | optional |
| GET | `/api/salesman/material/list` | 获取列表 | - | optional |

## api / 分销员控制器

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/salesman/salesman/customerDetail` | 获取我的客户详情 | - | optional |
| GET | `/api/salesman/salesman/customerList` | 获取我的客户列表 | - | optional |
| POST | `/api/salesman/salesman/customerRemark` | 更新客户备注 | - | optional |
| GET | `/api/salesman/salesman/detail` | 获取详情 | - | optional |
| GET | `/api/salesman/salesman/userInfo` | 获取分销员用户基础信息 | - | optional |

## api / 分销商品

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/salesman/product/detail` | 客户端分销商品详情 | - | optional |
| GET | `/api/salesman/product/list` | 获取列表 | - | optional |

## api / 前台优惠券

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/promotion/coupon/listByIds` | 按 ids 批量获取优惠券（前台展示用） | - | optional |

## api / 前台用户中心

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/user/user/checkModifyPasswordMobileCode` | 校验手机验证码 | - | optional |
| POST | `/api/user/user/close` | 禁用用户 | - | optional |
| GET | `/api/user/user/collectionShop` | 收藏店铺 | - | optional |
| GET | `/api/user/user/creditDetail` | 用户账期支付信息详情 | - | optional |
| POST | `/api/user/user/delHistoryProduct` | 删除最近浏览 | - | optional |
| GET | `/api/user/user/detail` | 会员详情 | - | optional |
| POST | `/api/user/user/emailValidate` | 邮箱验证 | - | optional |
| POST | `/api/user/user/emailValidateNew` | 邮箱验证 | - | optional |
| GET | `/api/user/user/historyProduct` | 最近浏览 | - | optional |
| GET | `/api/user/user/levelInfo` | 获取用户权益信息 | - | optional |
| GET | `/api/user/user/levelList` | 会员等级列表 | - | optional |
| POST | `/api/user/user/logout` | 前台用户退出登录 | - | anonymous? |
| GET | `/api/user/user/memberCenter` | 会员中心 | - | optional |
| POST | `/api/user/user/mobileValidate` | 手机验证 | - | optional |
| POST | `/api/user/user/modifyAvatar` | 修改头像 | - | optional |
| POST | `/api/user/user/modifyEmail` | 修改邮箱 | - | optional |
| POST | `/api/user/user/modifyMobile` | 修改手机号 | - | optional |
| POST | `/api/user/user/sendEmailCodeByEmailValidate` | 邮箱修改获取验证码 | - | optional |
| POST | `/api/user/user/sendEmailCodeByModifyEmail` | 邮箱修改新邮箱获取验证码 | - | optional |
| POST | `/api/user/user/sendMobileCodeByMobileValidate` | 手机修改获取验证码 | - | optional |
| POST | `/api/user/user/sendMobileCodeByModifyMobile` | 手机修改新手机获取验证码 | - | optional |
| POST | `/api/user/user/sendMobileCodeByModifyPassword` | 发送手机验证码 | - | optional |
| POST | `/api/user/user/updateInformation` | 更新会员信息 | - | optional |
| POST | `/api/user/user/uploadImg` | 上传图片 | - | optional |
| POST | `/api/user/user/uploadVideo` | 上传视频 | - | optional |

## api / 前台用户优惠券列表

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/user/coupon/claim` | 领取优惠券 | - | optional |
| POST | `/api/user/coupon/del` | 删除会员优惠券 | - | optional |
| GET | `/api/user/coupon/detail` | 会员优惠券详情 | - | optional |
| GET | `/api/user/coupon/getList` | 优惠券列表 | - | optional |
| GET | `/api/user/coupon/list` | 会员优惠券列表 | - | optional |

## api / 前台用户反馈留言

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/user/feedback/list` | 订单咨询/留言列表 | - | optional |
| POST | `/api/user/feedback/submit` | 提交留言 | - | optional |

## api / 前台用户发票

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/user/orderInvoice/create` | 发票申请 | - | optional |
| GET | `/api/user/orderInvoice/detail` | 发票详情 | - | optional |
| POST | `/api/user/orderInvoice/update` | 更新发票申请数据 | - | optional |

## api / 前台用户售后

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/user/aftersales/applyData` | 售后详情 | - | optional |
| POST | `/api/user/aftersales/cancel` | 取消售后申请 | - | optional |
| GET | `/api/user/aftersales/config` | 售后配置 | - | optional |
| POST | `/api/user/aftersales/create` | 售后申请 | - | optional |
| GET | `/api/user/aftersales/detail` | 查看售后记录 | - | optional |
| GET | `/api/user/aftersales/detailLog` | 查看售后log记录 | - | optional |
| POST | `/api/user/aftersales/feedback` | 提交售后反馈记录 | - | optional |
| GET | `/api/user/aftersales/getRecord` | 售后申请记录 | - | optional |
| GET | `/api/user/aftersales/list` | 售后列表 | - | optional |
| POST | `/api/user/aftersales/update` | 售后申请修改 | - | optional |

## api / 前台用户增票资质发票

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/user/invoice/create` | 增票资质创建 | - | optional |
| GET | `/api/user/invoice/detail` | 增票资质详情 | - | optional |
| GET | `/api/user/invoice/getStatus` | 判断当前用户的增票资质是否审核通过 | - | optional |
| POST | `/api/user/invoice/update` | 增票资质修改 | - | optional |

## api / 前台用户提现

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/user/withdrawApply/accountDetail` | 提现账号详情 | - | optional |
| POST | `/api/user/withdrawApply/apply` | 提现申请 | - | optional |
| POST | `/api/user/withdrawApply/applyCommission` | 佣金提现申请（D1107） | - | optional |
| POST | `/api/user/withdrawApply/createAccount` | 创建提现账号 | - | optional |
| POST | `/api/user/withdrawApply/delAccount` | 删除提现账号 | - | optional |
| GET | `/api/user/withdrawApply/list` | 列表 | - | optional |
| POST | `/api/user/withdrawApply/updateAccount` | 修改提现账号 | - | optional |

## api / 前台用户站内信

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/user/message/del` | 删除 | - | optional |
| GET | `/api/user/message/list` | 获取列表 | - | optional |
| GET | `/api/user/message/unreadCount` | 获取未读消息统计 | - | optional |
| POST | `/api/user/message/updateAllRead` | 设置为全部已读 | - | optional |
| POST | `/api/user/message/updateMessageRead` | 设置为已读 | - | optional |
| POST | `/api/user/message/updateOrderMessageRead` | 设置订单消息为已读 | - | optional |

## api / 前台用户订单中心

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/user/order/buyAgain` | 再次购买 | - | optional |
| POST | `/api/user/order/cancelOrder` | 取消订单 | - | optional |
| POST | `/api/user/order/confirmReceipt` | 确认收货 | - | optional |
| POST | `/api/user/order/delOrder` | 删除订单 | - | optional |
| GET | `/api/user/order/detail` | 订单详情 | - | optional |
| GET | `/api/user/order/list` | 订单列表 | - | optional |
| GET | `/api/user/order/orderNum` | 订单数量 | - | optional |
| GET | `/api/user/order/reasonConfig` | 获取买家售后与取消订单共用原因配置 | - | optional |
| GET | `/api/user/order/shippingInfo` | 物流信息 | - | optional |

## api / 前台用户评论晒单

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/user/comment/detail` | 评论详情 | - | optional |
| POST | `/api/user/comment/evaluate` | 商品评价 / 晒单 | - | optional |
| GET | `/api/user/comment/list` | 已评价列表 | - | optional |
| GET | `/api/user/comment/showedList` | 晒单列表 | - | optional |
| GET | `/api/user/comment/subNum` | 评论晒单数量 | - | optional |

## api / 前台用户询价

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/user/priceInquiry/del` | 删除询价（仅用户侧隐藏） | - | optional |
| GET | `/api/user/priceInquiry/detail` | 获取当前登录用户询价详情 | - | optional |
| GET | `/api/user/priceInquiry/list` | 获取当前登录用户询价列表 | - | optional |
| POST | `/api/user/priceInquiry/reply` | 用户回复商家 | - | optional |

## api / 前台用户账户信息

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/user/account/commissionList` | 佣金账户明细列表（D1107） | - | optional |
| GET | `/api/user/account/list` | 账户信息列表 | - | optional |

## api / 前台用户账户充值信息

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/user/rechargeOrder/checkStatus` | 获取充值支付状态 | - | optional |
| POST | `/api/user/rechargeOrder/create` | 充值支付 | - | optional |
| GET | `/api/user/rechargeOrder/list` | 充值记录列表 | - | optional |
| POST | `/api/user/rechargeOrder/pay` | 充值支付 | - | optional |
| GET | `/api/user/rechargeOrder/paymentList` | 充值支付方式 | - | optional |
| GET | `/api/user/rechargeOrder/setting` | 充值配置 | - | optional |
| POST | `/api/user/rechargeOrder/update` | 充值申请 | - | optional |

## api / 商品

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/product/product/detail` | 商品详情 | - | optional |
| GET | `/api/product/product/detail` | 商品详情 | - | optional |
| GET | `/api/product/product/getBatchProductAvailability` | 批量获取商品可用活动与数据 | - | optional |
| GET | `/api/product/product/getComment` | 获取商品评论 | - | optional |
| GET | `/api/product/product/getCommentList` | 获取商品评论列表 | - | optional |
| GET | `/api/product/product/getCoupon` | 获取商品优惠券 | - | optional |
| GET | `/api/product/product/getFeedbackList` | 获取商品反馈列表 | - | optional |
| GET | `/api/product/product/getHistoryProduct` | 获取当前用户历史购买过的商品信息 | - | optional |
| GET | `/api/product/product/getPresaleDepositAmount` | 获取预售的定金价格 | - | optional |
| POST | `/api/product/product/getProductAmount` | 获取商品数量 | - | optional |
| GET | `/api/product/product/getProductAvailability` | 商品可用活动与数据 | - | optional |
| GET | `/api/product/product/getRelated` | 获取相关商品 | - | optional |
| GET | `/api/product/product/isCollect` | 商品是否收藏 | - | optional |
| GET | `/api/product/product/list` | 商品列表 | - | optional |
| POST | `/api/product/product/priceInquiry` | 商品询价 | - | optional |
| POST | `/api/product/product/promotion` | 商品活动 | - | optional |
| GET | `/api/product/product/share` | 商品分享落地页 | - | optional |
| GET | `/api/search/search/getAttributeSuggestions` | 获取属性搜索建议 | - | optional |
| POST | `/api/search/search/getFilter` | 商品筛选 | - | optional |
| POST | `/api/search/search/getProduct` | 商品列表 | - | optional |
| GET | `/api/search/searchGuess/index` | 搜索关键词联想 | - | optional |

## api / 商品分类

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/category/category/all` | 获取所有商品分类 | - | optional |
| GET | `/api/category/category/hot` | 获取热门分类 | - | optional |
| GET | `/api/category/category/list` | 获取指定分类下的所有子分类 | - | optional |
| GET | `/api/category/category/parentTree` | 获取父级分类 | - | optional |
| GET | `/api/category/category/relateArticle` | 获取相关文章 | - | optional |
| GET | `/api/category/category/relateBrand` | 获取相关品牌 | - | optional |
| GET | `/api/category/category/relateCategory` | 获取相关分类 | - | optional |
| GET | `/api/category/category/relateLookAlso` | 获取商品详情页中，看了还看商品 | - | optional |
| GET | `/api/category/category/relateRank` | 获取相关排行 | - | optional |

## api / 商品收藏

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/user/collectProduct/cancel` | 取消商品收藏 | - | optional |
| GET | `/api/user/collectProduct/list` | 商品收藏列表 | - | optional |
| POST | `/api/user/collectProduct/save` | 商品收藏 | - | optional |

## api / 商品样式

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/product/productStyle/activeStyle` | 获取商品当前生效样式 | - | optional |
| GET | `/api/product/productStyle/batchActiveStyle` | 批量获取商品生效样式 | - | optional |
| GET | `/api/product/productStyle/batchActiveStyleBySn` | 批量获取商品生效样式（通过商品编号SN） | - | optional |

## api / 商户

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/merchant/merchant/apply` | 申请入驻 | - | optional |
| GET | `/api/merchant/merchant/applyDetail` | 申请详情 | - | optional |
| GET | `/api/merchant/merchant/applyShopAgreement` | 申请入驻协议 | - | optional |
| GET | `/api/merchant/merchant/myApply` | 我的申请 | - | optional |
| POST | `/api/merchant/merchant/uploadLicense` | 入驻营业执照专用上传 | - | optional |

## api / 商米云打印回调

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/print/sunmi/callback` | 接收商米云打印回调（占位） | - | optional |

## api / 地区

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/sys/region/getCityListByCityName` | 用户地区列表 | - | optional |
| GET | `/api/sys/region/getCityRegion` | 用户地区列表 | - | optional |
| GET | `/api/sys/region/getProvinceList` | 省份列表 | - | optional |
| GET | `/api/sys/region/getRegion` | 地区列表 | - | optional |
| GET | `/api/sys/region/getRegionListByRegionId` | 获取子集通过RegionId | - | optional |
| GET | `/api/sys/region/getRegionPath` | 根据regionId获取完整地区路径 | - | optional |
| GET | `/api/sys/region/getUserRegion` | 用户地区列表 | - | optional |

## api / 客服会话

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/im/conversation/conversation/batch` | 批量操作 | - | optional |
| GET | `/im/conversation/conversation/consultHistory` | 获取历史会话 | - | optional |
| POST | `/im/conversation/conversation/create` | 创建 | - | optional |
| POST | `/im/conversation/conversation/del` | 删除 | - | optional |
| GET | `/im/conversation/conversation/detail` | 获取详情 | - | optional |
| GET | `/im/conversation/conversation/frontSearch` | 前台搜索 | - | optional |
| GET | `/im/conversation/conversation/list` | 获取列表 | - | optional |
| POST | `/im/conversation/conversation/saveRemark` | 保存备注 | - | optional |
| GET | `/im/conversation/conversation/search` | 搜索 | - | optional |
| POST | `/im/conversation/conversation/transfer` | 转接 | - | optional |
| POST | `/im/conversation/conversation/updateField` | 更新字段 | - | optional |
| GET | `/im/conversation/conversation/waitServantList` | 获取待接入的客户列表 | - | optional |

## api / 店铺

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/shop/shop/collect` | 店铺收藏 | - | optional |
| GET | `/api/shop/shop/decorate` | 店铺装修 | - | optional |
| GET | `/api/shop/shop/detail` | 店铺详情 | - | optional |
| GET | `/api/shop/shop/getCommentList` | 获取评论列表 | - | optional |
| GET | `/api/shop/shop/head` | 店铺头部装修 | - | optional |
| GET | `/api/shop/shop/list` | 店铺列表 | - | optional |
| GET | `/api/shop/shop/pickupDetail` | 自提点详情 | - | optional |
| GET | `/api/shop/shop/pickupList` | 店铺关联自提点 | - | optional |
| GET | `/api/shop/shop/storeDetail` | 门店详情 | - | optional |
| GET | `/api/shop/shop/storeList` | 店铺列表 | - | optional |

## api / 店铺分类

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/shop/category/parentTree` | 获取父级分类分列表 | - | optional |
| GET | `/api/shop/category/tree` | 获取分列表 | - | optional |

## api / 微信直播

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/content/wechatVideoAccount/finderLiveRecord` | 获取视频号当前的直播记录 | - | optional |

## api / 微信直播订单同步回调

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/content/wechatLiveSync/callback` | 接收微信直播同步回调 | - | optional |

## api / 微商名片邀请

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/salesman/salesman/acceptCardInvite` | 接受名片邀请，一键成为下级分销员 | - | optional |

## api / 抽奖活动

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/promotion/lottery/confirmReceive` | 确认领取商品奖品 | - | optional |
| POST | `/api/promotion/lottery/draw` | 执行抽奖 | - | optional |
| GET | `/api/promotion/lottery/info` | 获取抽奖活动信息 | - | optional |
| GET | `/api/promotion/lottery/myRecords` | 我的中奖记录 | - | optional |
| GET | `/api/promotion/lottery/winners` | 获取中奖名单 | - | optional |

## api / 拼团活动管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/promotion/groupon/detail` | 客户端详情 | - | optional |
| GET | `/api/promotion/groupon/getGrouponInfo` | 商品id获取拼团活动信息 | - | optional |
| GET | `/api/promotion/groupon/imageList` | 拼团图片列表 | - | optional |
| GET | `/api/promotion/groupon/itemList` | 拼团活动商品列表 | - | optional |

## api / 推荐

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/common/recommend/getProductIds` | 获取商品ids | - | optional |
| GET | `/api/common/recommend/guessLike` | 猜你喜欢 | - | optional |

## api / 文章

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/article/article/issueInfo` | 资讯类文章详情 | - | optional |
| GET | `/api/article/article/list` | 文章列表 | - | optional |
| GET | `/api/article/article/newsInfo` | 资讯类文章详情 | - | optional |
| GET | `/api/article/category/indexBzzxList` | 资讯类文章列表，包含文章 | - | optional |

## api / 文章分类

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/article/category/getArticleCategoryList` | 文章分类列表 | - | optional |
| GET | `/api/article/category/list` | 文章列表 | - | optional |

## api / 注册控制器

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/user/regist/registAct` | 会员注册 | - | optional |

## api / 消息列表

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/im/conversation/message/list` | 获取列表 | - | optional |
| POST | `/im/conversation/message/send` | 发送消息 | - | optional |
| POST | `/im/conversation/message/setRead` | 设置已读 | - | optional |

## api / 消息控制器（内部接口）

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/socket/send` | 服务器端发送消息 | - | optional |

## api / 版本

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/appVersion/getAppUpdate` | 详情 | - | optional |

## api / 用户实名认证

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/user/company/apply` | 实名申请 | - | optional |
| GET | `/api/user/company/detail` | 实名认证详情 | - | optional |
| GET | `/api/user/company/myApply` | 我的实名认证申请 | - | optional |

## api / 用户收货地址

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/user/address/create` | 创建收货地址 | - | optional |
| POST | `/api/user/address/del` | 删除收货地址 | - | optional |
| GET | `/api/user/address/detail` | 收货地址详情 | - | optional |
| GET | `/api/user/address/list` | 收货地址列表 | - | optional |
| POST | `/api/user/address/setSelected` | 设置选中状态 | - | optional |
| GET | `/api/user/address/tagList` | 收货地址常用标签列表 | - | optional |
| POST | `/api/user/address/update` | 修改收货地址 | - | optional |

## api / 登录

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/user/login/signin` | 会员登录（用户名/手机号 + 密码/验证码） | - | anonymous? |

## api / 登录控制器

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/user/login/bindMobile` | 绑定手机 | - | anonymous? |
| POST | `/api/user/login/bindWechat` | 绑定微信公众号 | - | anonymous? |
| POST | `/api/user/login/checkEmail` | 校验邮箱,仅用于重置密码 | - | anonymous? |
| POST | `/api/user/login/checkMobile` | 校验手机号,仅用于重置密码 | - | anonymous? |
| POST | `/api/user/login/forgetPassword` | 修改密码 | - | anonymous? |
| POST | `/api/user/login/getMobile` | 获取用户手机号 | - | anonymous? |
| GET | `/api/user/login/getQuickLoginSetting` | 获取快捷登录设置 | - | anonymous? |
| GET | `/api/user/login/getWxLoginInfoByCode` | 通过微信code获得微信用户信息 | - | anonymous? |
| GET | `/api/user/login/getWxLoginUrl` | 获得微信授权url | - | anonymous? |
| POST | `/api/user/login/sendEmailCode` | 发送邮箱验证码 | - | anonymous? |
| POST | `/api/user/login/sendMobileCode` | 发送短信验证码 | - | anonymous? |
| GET | `/api/user/login/unbindWechat` | 解绑微信公众号 | - | anonymous? |
| POST | `/api/user/login/updateUserOpenid` | 获取用户openid | - | anonymous? |
| POST | `/api/user/login/wechatEvent` | 检测用户扫码后处理事件 | - | anonymous? |
| ANY | `/api/user/login/wechatServer` | 微信服务器校验 | - | anonymous? |

## api / 短链

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/s/{code}` |  | - | optional |

## api / 礼品卡

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/promotion/giftCardGroups/claim` | 领取礼品卡 | - | optional |
| GET | `/api/promotion/giftCardGroups/detail` | 礼品卡详情 | - | optional |
| POST | `/api/promotion/giftCardGroups/exchange` | 兑换礼品卡 | - | optional |
| GET | `/api/promotion/giftCardGroups/getTransferLink` | 获取转赠链接 | - | optional |
| GET | `/api/promotion/giftCardGroups/isGiftCardExpired` | 礼品卡是否过期 | - | optional |
| POST | `/api/promotion/giftCardGroups/transfer` | 转赠礼品卡 | - | optional |

## api / 积分兑换

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/product/exchange/addToCart` | 添加积分商品购物车 | - | optional |
| GET | `/api/product/exchange/detail` | 积分兑换详情 | - | optional |
| GET | `/api/product/exchange/list` | 获取列表 | - | optional |

## api / 积分列表

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/user/pointsLog/list` | 积分列表 | - | optional |

## api / 统计基础表

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/common/log` | 创建日志记录 | - | optional |

## api / 行为验证

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/common/verification/captcha` | 获取验证码 | - | anonymous? |
| POST | `/api/common/verification/check` | 校验验证码 | - | optional |
| POST | `/api/common/verification/get` | 获取验证码(废弃) | - | optional |

## api / 表单令牌校验

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/common/csrf/create` |  | - | optional |

## api / 装修分享模板

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/home/share/import` | 装修分享导入 | - | optional |

## api / 装修组件控制器

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/decorate/discrete/getOpenAdvertising` | 获取开屏广告 | - | optional |

## api / 订单支付

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/order/pay/checkStatus` | 检测订单支付状态 | - | optional |
| GET | `/api/order/pay/create` | 支付订单创建（尾款支付/普通订单支付） | - | optional |
| GET | `/api/order/pay/createDepositPayment` | 预售定金支付创建 | - | optional |
| GET | `/api/order/pay/getPayLog` | 检测订单支付状态 | - | optional |
| GET | `/api/order/pay/index` | 订单支付 | - | optional |
| POST | `/api/order/pay/notify/lakala` | 拉卡拉支付/退款回调 | - | optional |
| POST | `/api/order/pay/notify/{payType}` | 支付回调 | - | optional |
| POST | `/api/order/pay/notify/{payType}` | 支付回调 | - | optional |
| GET | `/api/order/pay/paymentList` | 支付方式列表（下单前收银台，无需订单） | - | optional |
| POST | `/api/order/pay/refundNotify/{payType}` | 退款回调地址 | - | optional |
| POST | `/api/order/pay/refundNotify/{payType}` | 退款回调地址（表单） | - | optional |

## api / 订单支付-Apple Pay

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/order/pay/applepay/confirm` | Apple Pay 支付确认（自建原生流程：原始 token） | - | optional |
| POST | `/api/order/pay/applepay/confirmByPaymentMethod` | Apple Pay 支付确认（PSP 前端驱动：paymentMethodId，如 Stripe） | - | optional |
| POST | `/api/order/pay/applepay/validateMerchant` | Apple Pay 商户校验 | - | optional |
| POST | `/api/order/pay/notify/ums` | 银联商务(UMS) Apple Pay 支付结果异步通知 | - | optional |

## api / 订单结算

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/order/check/getAvailablePaymentType` | 获取可用支付方式 | - | optional |
| GET | `/api/order/check/getInvoice` | 获得上次订单发票信息 | - | optional |
| GET | `/api/order/check/getStoreShippingType` | 获得店铺配送方式 | - | optional |
| POST | `/api/order/check/index` | 获取列表 | - | optional |
| POST | `/api/order/check/submit` | 提交订单 | - | optional |
| POST | `/api/order/check/update` | 更新 | - | optional |
| POST | `/api/order/check/updateCoupon` | 更新优惠券 | - | optional |

## api / 购物车管理

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| POST | `/api/cart/cart/addToCart` | 添加购物车 | - | optional |
| POST | `/api/cart/cart/clear` | 清空购物车 | - | optional |
| GET | `/api/cart/cart/getCount` | 获取数量 | - | optional |
| GET | `/api/cart/cart/getCouponDiscount` | 获取优惠券折扣 | - | optional |
| GET | `/api/cart/cart/list` | 获取列表 | - | optional |
| POST | `/api/cart/cart/removeItem` | 删除购物车商品 | - | optional |
| POST | `/api/cart/cart/updateCheck` | 更新选中状态 | - | optional |
| POST | `/api/cart/cart/updateItem` | 更新购物车商品 | - | optional |

## api / 部分通用功能的控制器

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/common/util/miniCode` | 获取小程序二维码 | - | optional |
| GET | `/api/common/util/miniScene` | 解析小程序码 scene 短码 | - | optional |
| GET | `/api/common/util/qrCode` | 获取二维码 | - | optional |

## api / 配置

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/common/config/afterSalesService` | 获取售后服务 | - | optional |
| GET | `/api/common/config/base` | 获取基础配置 | - | optional |
| GET | `/api/common/config/getLoginProtocol` | 获取登录协议 | - | anonymous? |
| GET | `/api/common/config/getLoginProtocolContent` | 获取登录协议内容  termsOfService:服务协议；privacyPolicy:隐私政策；afterSalesService:售后服务 | - | anonymous? |
| GET | `/api/common/config/initConfigSettings` | 获取初始化配置项 | - | optional |
| GET | `/api/common/config/mobileAreaCode` | 获取移动端区号 | - | optional |
| GET | `/api/common/config/mobileSearchStyle` | 获取移动端搜索风格；1-普通商品搜索，2-门店聚合搜索 | - | optional |
| GET | `/api/common/config/productDetailStyle` | 获取商品详情页风格；1-样式一，2-样式二 | - | optional |
| GET | `/api/common/config/searchStyle` | 获取搜索页风格；1-样式一，2-样式二 | - | optional |
| GET | `/api/common/config/themeSettings` | 获取主题配置项 | - | optional |

## api / 鎷煎洟璁板綍绠＄悊

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/promotion/groupon/record/details` | 鎷煎洟娲诲姩璇︽儏 | - | optional |
| GET | `/api/promotion/groupon/record/list` | 鎷煎洟娲诲姩璁板綍鍒楄〃 | - | optional |

## api / 鐢ㄦ埛绛惧埌涓績

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/user/sign/index` | 绛惧埌棣栭〉 | - | optional |
| GET | `/api/user/sign/sign` | 绛惧埌 | - | optional |

## api / 首页

| 方法 | 路径 | 说明 | 权限 | 登录 |
|---|---|---|---|---|
| GET | `/api/home/home/friendLinks` | 友情链接 | - | optional |
| GET | `/api/home/home/getCoupon` | 首页优惠券 | - | optional |
| GET | `/api/home/home/getCustomerServiceConfig` | 获取前台客户配置 | - | optional |
| GET | `/api/home/home/getRecommend` | 首页 | - | optional |
| GET | `/api/home/home/getSeckill` | 获取秒杀 | - | optional |
| GET | `/api/home/home/h5Category` | H5分类页装修 | - | optional |
| GET | `/api/home/home/h5Member` | H5个人中心装修 | - | optional |
| GET | `/api/home/home/h5ProductDetail` | H5商品详情页装修 | - | optional |
| GET | `/api/home/home/h5Search` | 移动端搜索页装修 | - | optional |
| GET | `/api/home/home/index` | 首页 | - | optional |
| GET | `/api/home/home/memberDecorate` | 个人中心 | - | optional |
| GET | `/api/home/home/mobileLogin` | 移动端登录页装修 | - | anonymous? |
| GET | `/api/home/home/mobileNav` | 移动端导航 | - | optional |
| GET | `/api/home/home/o2oIndex` | 门店首页装修 | - | optional |
| GET | `/api/home/home/pcHeader` | PC头部装修配置 | - | optional |
| GET | `/api/home/home/pcIndex` | PC端首页 | - | optional |
| GET | `/api/home/home/pcLogin` | PC登录页装修 | - | anonymous? |
| GET | `/api/home/home/pcSearch` | PC搜索页装修 | - | optional |
