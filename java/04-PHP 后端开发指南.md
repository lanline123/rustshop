# TigShop 开源商城系统 - 第 4 部分：PHP 后端开发指南

---

## 目录

1. [项目概述](#1-项目概述)
2. [项目架构与目录结构](#2-项目架构与目录结构)
3. [开发规范详解](#3-开发规范详解)
4. [开发配置](#4-开发配置)
5. [新增业务完整流程](#5-新增业务完整流程)
6. [接口文档使用](#6-接口文档使用)
7. [常见问题与优化](#7-常见问题与优化)

---

## 1. 项目概述

### 1.1 技术栈

TigShop PHP 后端基于 ThinkPHP 框架构建，采用高性能的 Swoole 运行模式，提供强大的电商 API 服务能力。

**核心组件:**

| 层次 | 组件 | 技术选型 | 版本 |
|------|------|----------|------|
| **核心框架** | ThinkPHP | PHP Web 开发框架 | 6.x+ |
| **运行环境** | Swoole | 高性能异步引擎 | 4.8+/5.x+ |
| **ORM 框架** | Query Builder / Eloquent | ORM 数据访问 | - |
| **API 文档** | ApiPost / Swagger | RESTful 接口文档 | latest |
| **数据库** | MySQL | 关系型数据库 | 8.0+ |
| **缓存** | Redis | 分布式缓存 | 6.x+ |
| **消息队列** | RabbitMQ | 异步消息处理 | 5.x+ |
| **邮件服务** | PHPMailer | 邮件发送 | 6.x+ |
| **Excel 处理** | PHPSpreadsheet | Excel 导入导出 | 1.x+ |
| **验证码** | TigCaptcha | 图形验证码 | - |

### 1.2 架构设计

```
┌──────────────────────────────────────────────────────────┐
│                    Nginx Server                          │
│                  (反向代理/负载均衡)                      │
└───────────────────────┬──────────────────────────────────┘
                        ↓
┌──────────────────────────────────────────────────────────┐
│              Swoole HTTP Server (FastCGI)                │
│         (高并发异步请求处理)                              │
└───────────────────────┬──────────────────────────────────┘
                        ↓
┌──────────────────────────────────────────────────────────┐
│                 ThinkPHP Router                          │
│              (路由分发/中间件)                             │
└───────────────────────┬──────────────────────────────────┘
                        ↓
┌──────────────────────────────────────────────────────────┐
│                   Controller Layer                       │
│   ┌─────────────┐  ┌─────────────┐  ┌──────────┐        │
│   │ Admin API   │  │   User API  │  │  Common  │        │
│   │ (后台管理)   │  │  (前台用户)  │  │ (公共接口)│        │
│   └──────┬──────┘  └──────┬──────┘  └────┬─────┘        │
└──────────┴────────────────┴──────────────┴──────────────┘
                        ↓
┌──────────────────────────────────────────────────────────┐
│                   Service Layer                           │
│   ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│   │Merchant  │ │Product   │ │ Order    │ │Promotion │   │
│   │ Service  │ │ Service  │ │ Service  │ │ Service  │   │
│   └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘   │
└────────┼───────────┼────────────┼────────────┼─────────┘
         │           │            │            │
         ↓           ↓            ↓            ↓
┌──────────────────────────────────────────────────────────┐
│                   Model Layer                            │
│   ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│   │Model     │ │Model     │ │Model     │ │Model     │   │
│   └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘   │
└────────┼───────────┼────────────┼────────────┼─────────┘
         │           │            │            │
         ↓           ↓            ↓            ↓
┌──────────────────────────────────────────────────────────┐
│                     Data Layer                            │
│        MySQL      Redis     RabbitMQ                      │
└──────────────────────────────────────────────────────────┘
```

### 1.3 性能特点

**Swoole 优势**:
- ✅ 高并发：单线程支持数千并发连接
- ✅ 低延迟：异步非阻塞 I/O
- ✅ 高性能：C 级别底层实现
- ✅ 长连接：WebSocket/TCP 支持

**ThinkPHP 特性**:
- ✅ 简洁优雅：遵循 PSR 规范
- ✅ 模块化设计：清晰的 MVC 分层
- ✅ 丰富的扩展：第三方库生态完善
- ✅ 文档友好：中文文档齐全

---

## 2. 项目架构与目录结构

### 2.1 整体项目结构

```
tigshop-api-php/                                    # PHP 后端根目录
├── app/                                            # 应用主目录 ⭐⭐⭐
│   ├── adminapi/                                  # 后台管理 API 模块 ⭐
│   │   ├── controller/                            # 控制器目录 ⭐⭐⭐
│   │   │   ├── authority/                         # 权限管理控制器
│   │   │   │   ├── AdminRole.php                  # 角色管理
│   │   │   │   ├── AdminUser.php                  # 管理员管理
│   │   │   │   └── Authority.php                  # 权限管理
│   │   │   ├── merchant/                          # 商户管理控制器
│   │   │   │   ├── Account.php                    # 商户银行账户
│   │   │   │   ├── Apply.php                      # 入驻申请
│   │   │   │   ├── Merchant.php                   # 商户管理
│   │   │   │   ├── Shop.php                       # 店铺管理
│   │   │   │   ├── ShopAccount.php                # 资产控制
│   │   │   │   └── ShopWithdraw.php               # 提现管理
│   │   │   ├── order/                             # 订单管理控制器
│   │   │   │   ├── Aftersales.php                 # 售后管理
│   │   │   │   ├── Order.php                      # 订单管理
│   │   │   │   └── OrderLog.php                   # 订单日志
│   │   │   ├── panel/                             # 后台面板控制器
│   │   │   │   ├── Panel.php                      # 控制台
│   │   │   │   ├── SalesStatistics.php            # 销售统计
│   │   │   │   ├── StatisticsAccess.php           # 访问统计
│   │   │   │   └── StatisticsUser.php             # 用户统计
│   │   │   ├── product/                           # 商品管理控制器
│   │   │   │   ├── Brand.php                      # 品牌管理
│   │   │   │   ├── Category.php                   # 分类管理
│   │   │   │   ├── Product.php                    # 商品管理
│   │   │   │   └── ProductBatch.php               # 批量处理
│   │   │   ├── promotion/                         # 营销管理控制器
│   │   │   │   ├── Coupon.php                     # 优惠券
│   │   │   │   ├── ProductGift.php                # 活动赠品
│   │   │   │   ├── ProductPromotion.php           # 满减满赠
│   │   │   │   ├── Promotion.php                  # 优惠活动
│   │   │   │   ├── RechargeSetting.php            # 充值余额
│   │   │   │   ├── Seckill.php                    # 秒杀管理
│   │   │   │   └── TimeDiscount.php               # 限时折扣
│   │   │   ├── setting/                           # 配置管理控制器
│   │   │   │   ├── Config.php                     # 配置管理
│   │   │   │   └── FriendLinks.php                # 友情链接
│   │   │   └── user/                              # 用户管理控制器
│   │   │       ├── Feedback.php                   # 会员留言
│   │   │       └── User.php                       # 会员管理
│   │   │
│   │   ├── common/                                # 公共控制器目录
│   │   │   ├── CacheManage.php                    # 缓存管理
│   │   │   ├── Tool.php                           # 工具管理
│   │   │   └── Verification.php                   # 验证管理
│   │   │
│   │   ├── login/                                 # 登录控制器
│   │   │   └── Login.php                          # 统一登录入口
│   │   │
│   │   ├── middleware/                            # 中间件目录 ⭐⭐
│   │   │   ├── CheckAuthor.php                    # 鉴权中间件
│   │   │   └── JWT.php                            # JWT 登录验证
│   │   │
│   │   ├── route/                                 # 路由配置文件
│   │   │   ├── authority.php                      # 权限路由
│   │   │   ├── common.php                         # 公共路由
│   │   │   ├── login.php                          # 登录路由
│   │   │   ├── merchant.php                       # 商户路由
│   │   │   ├── order.php                          # 订单路由
│   │   │   ├── panel.php                          # 面板路由
│   │   │   ├── product.php                        # 商品路由
│   │   │   ├── promotion.php                      # 营销路由
│   │   │   ├── setting.php                        # 配置路由
│   │   │   └── user.php                           # 会员路由
│   │   │
│   │   ├── AdminBaseController.php                # 后台基础控制器
│   │   ├── common.php                             # 后台公共函数文件
│   │   ├── middleware.php                         # 后台全局中间件
│   │   └── BaseController.php                     # 通用基础控制器
│   │
│   ├── api/                                       # 前台 API 模块 ⭐⭐⭐⭐⭐
│   │   ├── controller/                            # 前台控制器目录
│   │   │   ├── cart/                              # 购物车控制器
│   │   │   │   └── Cart.php                       # 购物车操作
│   │   │   ├── category/                          # 商品分类控制器
│   │   │   │   └── Category.php                   # 分类展示
│   │   │   ├── common/                            # 前台公共控制器
│   │   │   │   └── Config.php                     # 配置信息
│   │   │   ├── home/                              # 首页控制器
│   │   │   │   └── Home.php                       # 前台首页
│   │   │   ├── order/                             # 订单控制器
│   │   │   │   ├── Check.php                      # 订单验证
│   │   │   │   └── Pay.php                        # 支付处理
│   │   │   ├── product/                           # 商品控制器
│   │   │   │   └── Product.php                    # 商品详情/列表
│   │   │   └── user/                              # 会员中心控制器
│   │   │       ├── Account.php                    # 账户明细
│   │   │       ├── Aftersales.php                 # 售后服务
│   │   │       └── User.php                       # 用户中心
│   │   │
│   │   ├── lang/                                  # 语言包目录
│   │   ├── middleware/                            # 前台中间件
│   │   │   ├── JWT.php                            # JWT 中间件
│   │   │   └── CheckLogin.php                     # 登录验证
│   │   │
│   │   ├── route/                                 # 前台路由配置
│   │   │   ├── cart.php                           # 购物车路由
│   │   │   ├── category.php                       # 分类路由
│   │   │   ├── common.php                         # 公共路由
│   │   │   ├── home.php                           # 首页路由
│   │   │   ├── order.php                          # 订单路由
│   │   │   ├── product.php                        # 商品路由
│   │   │   └── user.php                           # 会员路由
│   │   │
│   │   ├── IndexBaseController.php                # 前台基础控制器
│   │   ├── common.php                             # 前台公共函数
│   │   ├── middleware.php                         # 前台中间件定义
│   │   └── index.php                              # 前台入口文件（可选）
│   │
│   ├── constant/                                  # 常量定义目录
│   │   └── ResponseCode.php                       # 响应状态码常量
│   │
│   ├── im/                                        # 客服系统模块
│   │   ├── controller/                            # 客服控制器
│   │   │   ├── conversation/                      # 会话控制
│   │   │   │   └── Conversation.php               # 会话管理
│   │   │   └── Servant/                           # 客服控制
│   │   │       └── Servant.php                    # 客服操作
│   │   │
│   │   ├── library/                               # WebSocket 配置
│   │   │   ├── ImSocketClient.php                 # Socket 客户端
│   │   │   └── WebSocketClient.php                # WebSocket 客户端
│   │   │
│   │   ├── listener/                              # WebSocket 监听器
│   │   │   ├── WebsocketConnect.php               # 连接事件
│   │   │   ├── WebsocketEvent.php                 # 事件处理
│   │   │   └── WebsocketOpen.php                  # 打开事件
│   │   │
│   │   ├── middleware/                            # 客服中间件
│   │   │   ├── CheckServantLogin.php              # 客服登录校验
│   │   │   ├── CheckUserLogin.php                 # 用户登录校验
│   │   │   └── JWT.php                            # JWT 校验
│   │   │
│   │   ├── model/                                 # 客服模型
│   │   │   ├── BaseModel.php                      # 基础模型
│   │   │   ├── Conversation.php                   # 会话模型
│   │   │   ├── Message.php                        # 消息模型
│   │   │   └── Servant.php                        # 客服模型
│   │   │
│   │   ├── route/                                 # 客服路由
│   │   │   ├── conversation.php                   # 会话路由
│   │   │   └── servant.php                        # 客服路由
│   │   │
│   │   ├── service/                               # 客服服务层
│   │   │   ├── conversation/                      # 会话服务
│   │   │   │   ├── ConversationService.php        # 会话服务
│   │   │   │   └── MessageService.php             # 消息服务
│   │   │   └── servant/                           # 客服服务
│   │   │       └── ServantService.php             # 客服服务
│   │   │
│   │   └── middleware.php                         # 客服中间件
│   │
│   ├── job/                                       # 定时任务目录 ⭐⭐
│   │   ├── order/                                 # 订单相关任务
│   │   │   └── OrderCancelJob.php                 # 取消订单任务
│   │   ├── BaseJob.php                            # 任务基类
│   │   ├── MessageJob.php                         # 发送站内信
│   │   ├── MiniProgramJob.php                     # 发送订阅消息
│   │   ├── SmsJob.php                             # 发送短信队列
│   │   └── WechatJob.php                          # 发送公众号消息
│   │
│   ├── model/                                     # 模型目录 ⭐⭐⭐⭐⭐
│   │   ├── authority/                             # 权限模型
│   │   │   ├── AdminRole.php                      # 角色模型
│   │   │   ├── AdminUser.php                      # 管理员模型
│   │   │   └── Authority.php                      # 权限模型
│   │   │
│   │   ├── category/                              # 分类模型
│   │   │   └── category.php                       # 商品分类模型
│   │   │
│   │   ├── merchant/                              # 商户模型
│   │   │   ├── Apply.php                          # 商户申请表
│   │   │   ├── Merchant.php                       # 商户表
│   │   │   └── MerchantAccount.php                # 商户资产表
│   │   │
│   │   ├── order/                                 # 订单模型
│   │   │   ├── Aftersales.php                     # 售后单
│   │   │   ├── Cart.php                           # 购物车表
│   │   │   └── Order.php                          # 订单表
│   │   │
│   │   ├── product/                               # 商品模型
│   │   │   ├── Brand.php                          # 品牌表
│   │   │   ├── Category.php                       # 商品分类表
│   │   │   └── Product.php                        # 商品表
│   │   │
│   │   ├── promotion/                             # 营销模型
│   │   │   ├── Coupon.php                         # 优惠券表
│   │   │   ├── ProductGift.php                    # 赠品表
│   │   │   ├── ProductPromotion.php               # 营销活动表
│   │   │   └── Promotion.php                      # 优惠表
│   │   │
│   │   ├── setting/                               # 配置模型
│   │   │   ├── Config.php                         # 配置表
│   │   │   ├── FriendLinks.php                    # 友链表
│   │   │   └── Region.php                         # 地区表
│   │   │
│   │   ├── user/                                  # 用户模型
│   │   │   ├── CollectProduct.php                 # 商品收藏表
│   │   │   ├── Feedback.php                       # 留言表
│   │   │   ├── UserCoupon.php                     # 会员优惠券
│   │   │   └── ...其他用户相关模型
│   │   │
│   │   └── BaseModel.php                          # 基础模型类
│   │
│   ├── service/                                   # 服务层目录 ⭐⭐⭐⭐⭐
│   │   ├── admin/                                 # 后台服务层
│   │   │   ├── authority/                         # 权限服务
│   │   │   │   ├── AdminRoleService.php           # 角色服务
│   │   │   │   ├── AuthorityService.php           # 权限服务
│   │   │   │   └── AdminUserService.php           # 管理员服务
│   │   │   │
│   │   │   ├── order/                             # 订单服务
│   │   │   │   ├── AftersalesService.php          # 售后服务
│   │   │   │   ├── CartService.php                # 购物车服务
│   │   │   │   └── OrderDetailService.php         # 订单详情服务
│   │   │   │
│   │   │   └── user/                              # 用户服务
│   │   │       ├── CollectProductService.php      # 商品收藏服务
│   │   │       ├── FeedbackService.php            # 留言服务
│   │   │       └── UserService.php                # 会员服务
│   │   │
│   │   ├── index/                                 # 前台服务层
│   │   │   ├── cart/                              # 购物车服务
│   │   │   │   └── CartService.php                # 购物车服务
│   │   │   ├── merchant/                          # 商户服务
│   │   │   │   ├── ApplyService.php               # 商户服务
│   │   │   │   └── ShopService.php                # 店铺服务
│   │   │   └── promotion/                         # 营销服务
│   │   │       └── PromotionService.php           # 营销服务
│   │   │
│   │   ├── core/                                  # 核心服务层 ⭐⭐⭐
│   │   │   ├── merchant/                          # 商户核心服务
│   │   │   │   ├── ApplyCoreService.php           # 申请核心服务
│   │   │   │   └── MerchantCoreService.php        # 商户核心服务
│   │   │   │
│   │   │   └── order/                             # 订单核心服务
│   │   │       └── OrderCoreService.php           # 订单核心服务
│   │   │
│   │   └── BaseService.php                        # 基础服务类
│   │
│   ├── validate/                                  # 验证器目录 ⭐⭐⭐
│   │   ├── authority/                             # 权限验证器
│   │   │   ├── AdminRoleValidate.php              # 角色验证
│   │   │   └── AuthorityValidate.php              # 权限验证
│   │   │
│   │   └── user/                                  # 用户验证器
│   │       ├── FeedbackValidate.php               # 留言验证
│   │       └── UserValidate.php                   # 用户验证
│   │
│   ├── AppService.php                             # 应用服务类
│   ├── BaseController.php                          # 控制器基类
│   ├── common.php                                  # 应用公共函数
│   ├── event.php                                   # 事件定义
│   ├── ExceptionHandle.php                         # 异常处理器
│   ├── middleware.php                              # 全局中间件定义
│   ├── provider.php                                # 容器 Provider 定义
│   ├── Request.php                                 # 重写 Request 类
│   └── service.php                                 # 系统服务定义
│
├── config/                                        # 配置文件目录 ⭐⭐⭐⭐⭐
│   ├── annotation.php                             # 注解配置
│   ├── app.php                                    # 应用配置
│   ├── cache.php                                  # 缓存配置
│   ├── captcha.php                                # 验证码配置
│   ├── console.php                                # 控制台配置
│   ├── cookie.php                                 # Cookie 配置
│   ├── database.php                               # 数据库配置 ⭐⭐⭐⭐⭐
│   ├── filesystem.php                             # 文件磁盘配置
│   ├── jwt.php                                    # JWT 配置 ⭐⭐⭐
│   ├── lang.php                                   # 多语言配置
│   ├── log.php                                    # 日志配置
│   ├── middlewware.php                            # 中间件配置
│   ├── queue.php                                  # 队列配置
│   ├── route.php                                  # URL 和路由配置
│   ├── session.php                                # Session 配置
│   ├── shop.php                                   # 商城设置项 ⭐⭐⭐⭐
│   ├── swoole.php                                 # Swoole 配置 ⭐⭐⭐⭐
│   ├── tigCaptcha.php                             # 验证码配置
│   ├── trace.php                                  # Trace 配置
│   ├── verification.php                           # 滑块验证器
│   └── view.php                                   # 视图配置
│
├── public/                                        # WEB 目录（对外访问） ⭐⭐⭐⭐⭐
│   ├── img/                                       # 图片资源目录
│   ├── static/                                    # 静态文件目录
│   │   ├── css/                                   # CSS 文件
│   │   ├── js/                                    # JavaScript 文件
│   │   └── fonts/                                 # 字体文件
│   │
│   ├── favicon.ico                                # 网站图标
│   ├── index.php                                  # 入口文件 ⭐⭐⭐⭐⭐
│   └── router.php                                 # FastCGI 支持
│
├── readme/                                        # README 文件
│   └── pic/                                       # README 图片资源
│       └── logo.php                               # Logo 相关文件
│
├── route/                                         # 路由定义目录
│   └── app.php                                    # 路由定义文件
│
├── runtime/                                       # 运行时目录（可写）
│   ├── cache/                                     # 缓存文件
│   ├── log/                                       # 日志文件
│   └── temp/                                      # 临时文件
│
├── tool/                                          # 工具类目录 ⭐⭐⭐⭐
│   ├── exceptions/                                # 异常抛出文件
│   │   └── ApiException.php                       # 业务异常类
│   │
│   ├── log/                                       # 日志文件目录
│   │
│   ├── tig/                                       # Tig 应用文件
│   │   └── captcha/                               # 验证码文件
│   │       ├── CacheManager.php                   # 清除缓存类
│   │       └── Http.php                           # HTTP 请求类
│   │
│   ├── traits/                                    # Traits 功能片段
│   │   └── OutputTrait.php                        # 格式输出 Trait
│   │
│   └── utils/                                     # 工具类目录 ⭐⭐⭐⭐⭐
│       ├── Config.php                             # 配置读取类
│       ├── Excel.php                              # Excel 导出/导入类
│       ├── Format.php                             # 格式化工具类
│       ├── PHPMailerWrapper.php                   # 邮件发送类
│       ├── TigQueue.php                           # 队列操作类
│       ├── Time.php                               # 时间处理类
│       └── Util.php                               # 通用工具类
│
├── .example.env                                   # 环境变量示例文件
├── composer.json                                  # Composer 依赖配置 ⭐⭐⭐⭐⭐
├── README.md                                      # 项目说明文件
├── think                                          # 命令行入口文件 ⭐⭐⭐
└── vendor/                                        # 第三方依赖（Composer 安装）
```

### 2.2 核心模块说明

#### app/adminapi - 后台管理 API
为平台管理员提供全面的管理功能接口，涵盖商户、商品、订单、系统配置等所有后台管理需求。

**核心功能**:
- RBAC 权限管理系统
- 商户审核与管理
- 商品全生命周期管理
- 订单处理与售后
- 营销活动配置
- 数据统计分析

#### app/api - 前台 API
为用户提供完整的购物体验接口，包括商品浏览、购物流程、会员中心等功能。

**核心功能**:
- 商品展示与搜索
- 购物车管理
- 订单创建与支付
- 会员登录注册
- 个人中心管理
- 实时客服沟通

#### app/model - 数据模型
封装所有数据库表对应的 Model 类，提供数据访问和数据操作能力。

**特点**:
- 继承 BaseModel，统一方法调用
- 支持关联查询（OneToOne、OneToMany、ManyToMany）
- 软删除支持
- 自动时间戳管理

#### app/service - 业务服务层
实现复杂的业务逻辑，是系统的核心业务处理层。

**三级分层**:
1. **普通 Service**: 基础 CRUD 业务
2. **Admin/Index Service**: 按前端类型划分
3. **Core Service**: 核心复杂业务逻辑

#### tool/utils - 工具类库
提供通用的工具方法，避免重复代码编写。

**主要工具**:
- 配置读取工具 Config.php
- Excel 导入导出工具 Excel.php
- 邮件发送 PHPMailerWrapper.php
- HTTP 请求 Http.php
- 队列操作 TigQueue.php
- 时间处理 Time.php

---

## 3. 开发规范详解

### 3.1 命名规范

#### 文件名规范

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| **控制器** | PascalCase + Controller | `UserController.php`, `OrderController.php` |
| **模型** | PascalCase | `User.php`, `Product.php`, `Brand.php` |
| **验证器** | PascalCase + Validate | `UserValidate.php`, `OrderValidate.php` |
| **服务层** | Service | `UserService.php`, `ProductService.php` |
| **路由** | snake_case.php | `user.php`, `order.php`, `product.php` |
| **配置文件** | snake_case.php | `database.php`, `jwt.php`, `shop.php` |

#### 类名规范

```php
<?php
// ✅ 正确示例
namespace app\adminapi\controller;

use app\BaseController;

class UserController extends BaseController
{
    // ...
}

namespace app\model;

use app\model\BaseModel;

class User extends BaseModel
{
    // ...
}

namespace app\service\index\cart;

class CartService
{
    // ...
}
```

**关键点**:
- 控制器以 `Controller` 后缀结尾
- 模型直接以表名命名（复数或单数）
- 验证器以 `Validate` 或 `Validate.php` 结尾
- 服务层使用 `Service` 后缀

#### 方法命名规范

```php
<?php
// ✅ 推荐 - 动词 + 名词
public function getUserInfo($userId) { }
public function createOrder($data) { }
public function updateUserInfo($data) { }
public function deleteProduct($id) { }
public function listProducts($query) { }

// ❌ 不推荐
public function get() { }                    // 语义不清
public function doSomething() { }             // 过于笼统
public function handle() { }                  // 职责不明
```

#### 变量命名规范

```php
<?php
// ✅ 正确示例
$userId = 123;
$userName = "张三";
$orderList = [];
$productData = [];

// ❌ 错误示例
$id = 123;                    // 缺少语义
$name = "张三";               // 缺少范围限定
$data = [];                   // 过于泛化
```

#### 常量命名规范

```php
<?php
// ✅ 正确示例
const STATUS_SUCCESS = 'success';
const MAX_RETRY_COUNT = 3;
const CACHE_USER_PREFIX = 'user:';

// ❌ 错误示例
$maxCount = 3;                 // 不是常量
$status = '成功';               // 包含中文
```

### 3.2 数据库命名规范

#### 表命名
```sql
-- ✅ 推荐
user_info                    -- 用户信息表
order_detail                 -- 订单详情表
product_category             -- 商品分类表
merchant_apply               -- 商户申请表
admin_role                   -- 管理员角色表

-- ❌ 不推荐
u                           -- 太简短
userinfo                    -- 缺少分隔
t_user                      -- 多余前缀
```

#### 字段命名
```sql
-- ✅ 推荐
user_id                     -- 主键
user_name                   -- 用户名
order_status                -- 订单状态
created_at                  -- 创建时间
updated_at                  -- 更新时间

-- ❌ 不推荐
uid                        -- 过度简写
name                       -- 缺少范围限定
addtime                    -- 不规范缩写
```

#### 索引命名
```sql
-- ✅ 标准
PRIMARY KEY                 -- 主键索引
INDEX idx_user_id          -- 普通索引
UNIQUE INDEX uk_user_name  -- 唯一索引
KEY key_order_status       -- KEY 关键字

-- 命名约定
idx_表名_字段名
uk_表名_字段名
```

### 3.3 代码结构与 MVC 规范

#### Controller 层规范

**基础控制器**:
```php
<?php
namespace app;

use think\App;
use think\exception\HttpException;
use think\Response;

abstract class BaseController
{
    protected $app;
    protected $request;
    
    public function __construct(App $app)
    {
        $this->app = $app;
        $this->request = $app->request;
    }
    
    /**
     * 返回成功响应
     */
    protected function success($data = [], $msg = 'success', $code = 200)
    {
        return json([
            'code' => $code,
            'msg' => $msg,
            'data' => $data
        ]);
    }
    
    /**
     * 返回失败响应
     */
    protected function error($msg = 'error', $code = 400, $data = [])
    {
        return json([
            'code' => $code,
            'msg' => $msg,
            'data' => $data
        ]);
    }
    
    /**
     * JSON 响应
     */
    protected function json($data = [], $code = 200, $msg = 'success')
    {
        return json(['code' => $code, 'msg' => $msg, 'data' => $data]);
    }
}
```

**具体控制器**:
```php
<?php
namespace app\adminapi\controller\product;

use app\adminapi\BaseController;
use app\common\exception\ApiException;
use app\model\product\Product;
use app\model\product\Category;
use app\service\index\product\ProductService;
use think\Request;
use think\Validate;

/**
 * 商品管理控制器
 */
class ProductController extends BaseController
{
    protected $productService;
    protected $productModel;
    protected $categoryModel;
    
    public function initialize()
    {
        parent::initialize();
        
        // 初始化服务层
        $this->productService = new ProductService();
        $this->productModel = new Product();
        $this->categoryModel = new Category();
    }
    
    /**
     * 商品列表
     */
    public function list()
    {
        try {
            $params = $this->request->param();
            
            // 参数验证
            $validate = new Validate([
                ['page', 'require|integer', '页码不能为空'],
                ['limit', 'require|integer', '每页数量不能为空']
            ]);
            
            if (!$validate->check($params)) {
                return $this->error($validate->getError());
            }
            
            // 获取列表
            $result = $this->productService->getList($params);
            
            return $this->success($result);
            
        } catch (ApiException $e) {
            return $this->error($e->getMessage(), $e->getCode());
        } catch (\Exception $e) {
            return $this->error('系统错误', 500);
        }
    }
    
    /**
     * 商品详情
     */
    public function info()
    {
        try {
            $id = $this->request->param('id', 0, 'int');
            
            if (!$id) {
                return $this->error('ID 不能为空');
            }
            
            $product = $this->productModel->getProductDetail($id);
            
            if (!$product) {
                return $this->error('商品不存在');
            }
            
            return $this->success($product);
            
        } catch (\Exception $e) {
            return $this->error('查询失败');
        }
    }
    
    /**
     * 新增商品
     */
    public function add()
    {
        try {
            $data = $this->request->post();
            
            // 验证参数
            $validate = new Validate([
                ['productName', 'require', '商品名称不能为空'],
                ['categoryId', 'require|integer', '分类必须有效'],
                ['price', 'require|number', '价格必须为数字']
            ], [
                'productName.require' => '商品名称不能为空',
                'categoryId.require' => '请选择分类',
                'price.number' => '价格必须为数字'
            ]);
            
            if (!$validate->check($data)) {
                return $this->error($validate->getError());
            }
            
            // 保存商品
            $result = $this->productService->create($data);
            
            return $this->success($result, '添加成功');
            
        } catch (ApiException $e) {
            return $this->error($e->getMessage(), $e->getCode());
        } catch (\Exception $e) {
            return $this->error('添加失败');
        }
    }
    
    /**
     * 修改商品
     */
    public function edit()
    {
        try {
            $id = $this->request->post('id', 0, 'int');
            
            if (!$id) {
                return $this->error('ID 不能为空');
            }
            
            $data = $this->request->post();
            $data['id'] = $id;
            
            $result = $this->productService->update($data);
            
            return $this->success($result, '修改成功');
            
        } catch (\Exception $e) {
            return $this->error('修改失败');
        }
    }
    
    /**
     * 删除商品
     */
    public function delete()
    {
        try {
            $ids = $this->request->post('ids');
            
            if (!is_array($ids)) {
                $ids = [$ids];
            }
            
            $result = $this->productService->delete($ids);
            
            return $this->success($result, '删除成功');
            
        } catch (\Exception $e) {
            return $this->error('删除失败');
        }
    }
}
```

**关键要点**:
- 继承 `BaseController`
- 初始化服务层和模型层
- 统一的错误处理（try-catch）
- 参数验证使用 `think\Validate`
- 返回结果格式统一

#### Middleware 中间件规范

**JWT 认证中间件**:
```php
<?php
namespace app\adminapi\middleware;

use app\common\exception\ApiException;
use Closure;
use think\Request;

class JWT
{
    public function handle($request, Closure $next)
    {
        try {
            // 获取 Token
            $token = $request->header('Authorization', '');
            
            if (empty($token)) {
                throw new ApiException('请先登录', 401);
            }
            
            // 去除 Bearer 前缀
            $token = str_replace('Bearer ', '', $token);
            
            // 解析 Token
            $jwtConfig = config('jwt');
            $payload = \Firebase\JWT\JWT::decode($token, 
                $jwtConfig['key'], 
                $jwtConfig['algorithms']
            );
            
            // 将用户信息存入 request
            $request->setUserInfo((array)$payload);
            
        } catch (\Exception $e) {
            throw new ApiException('身份认证失败', 401);
        }
        
        return $next($request);
    }
}
```

#### Model 模型规范

**基础模型**:
```php
<?php
namespace app\model;

use think\Model;
use think\Db;

class BaseModel extends Model
{
    protected $autoWriteTimestamp = true;  // 启用时间戳
    protected $createTime = 'created_at';   // 创建时间字段
    protected $updateTime = false;          // 关闭更新时间自动填充
    
    /**
     * 关联查询
     */
    public function getRelationQuery($relationField = null)
    {
        // 实现关联查询逻辑
    }
    
    /**
     * 获取统计数据
     */
    public function getStatistics($field, $type = 'count')
    {
        return $this->$type($field);
    }
    
    /**
     * 软删除
     */
    protected static function boot()
    {
        static::onDeleting(function ($model) {
            if ($model->deleted_at) {
                return false;
            }
            
            $model->deleted_at = time();
            return false;  // 返回 false 表示不会真正删除
        });
    }
}
```

**具体模型**:
```php
<?php
namespace app\model;

use app\model\BaseModel;

class Product extends BaseModel
{
    // 指定表名（不含前缀）
    protected $table = 'product';
    
    // 主键
    protected $pk = 'id';
    
    // 自动写入时间戳
    protected $autoWriteTimestamp = true;
    
    /**
     * 关联商品分类
     */
    public function category()
    {
        return $this->belongsTo(Category::class, 'category_id', 'id');
    }
    
    /**
     * 关联品牌
     */
    public function brand()
    {
        return $this->belongsTo(Brand::class, 'brand_id', 'id');
    }
    
    /**
     * 关联 SKU
     */
    public function skus()
    {
        return $this->hasMany(ProductSku::class, 'product_id', 'id');
    }
    
    /**
     * 获取商品详情（含关联数据）
     */
    public function getProductDetail($id)
    {
        return $this->alias('p')
            ->field('p.*, c.category_name, b.brand_name')
            ->join('category c', 'p.category_id = c.id', 'left')
            ->join('brand b', 'p.brand_id = b.id', 'left')
            ->where('p.id', $id)
            ->find();
    }
    
    /**
     * 搜索商品
     */
    public function search($keyword, $categoryId = null)
    {
        $where = [];
        
        if ($keyword) {
            $where[] = ['name', 'like', "%{$keyword}%"];
        }
        
        if ($categoryId) {
            $where[] = ['category_id', '=', $categoryId];
        }
        
        return $this->where($where)->select();
    }
}
```

**关键要点**:
- 继承 `BaseModel`
- 使用关联关系定义
- 提供常用查询方法
- 符合 PSR-4 命名规范

### 3.4 安全规范

#### SQL 注入防护

```php
<?php
// ✅ 正确 - 使用参数绑定
$product = Db::name('product')->where('id', $id)->find();

// ❌ 错误 - 字符串拼接
$product = Db::query("SELECT * FROM product WHERE id = {$id}");
```

#### XSS 防护

```php
<?php
// 输入时转义
$content = htmlspecialchars($input, ENT_QUOTES, 'UTF-8');

// 或使用框架自带方法
$content = htmlentities($input);

// 模板中输出时转义
<?= $html_content ?>  // 自动转义
```

#### 敏感数据加密

```php
<?php
// 密码加密
$password = password_hash($plainPassword, PASSWORD_BCRYPT);

// 验证密码
if (password_verify($inputPassword, $hash)) {
    // 密码正确
}

// 敏感信息脱敏
function maskPhone($phone) {
    return substr_replace($phone, '****', 3, 4);
}

$maskedPhone = maskPhone('13812345678');  // 138****5678
```

### 3.5 注释规范

#### 类注释

```php
<?php
/**
 * 商品模型
 * 负责商品信息的数据操作
 * 
 * @author TigShop Team
 * @version 1.0
 * @since 2024-07-18
 */
class Product extends BaseModel
{
    // ...
}
```

#### 方法注释

```php
<?php
/**
 * 获取商品列表
 * 
 * @param array $params 查询参数
 * @return array 商品列表数据
 * @throws ApiException 参数错误或查询失败
 */
public function getList($params = [])
{
    // ...
}
```

#### 业务逻辑注释

```php
<?php
// 注意：此处需要检查库存是否充足，防止超卖
if ($stock < $quantity) {
    throw new ApiException('库存不足');
}

// TODO: 2025-09-01 需要添加优惠券核销逻辑
// FIXME: 当前并发场景下可能出现库存负数问题
// WARNING: 此方法会触发积分变更，请谨慎调用
```

---

## 4. 开发配置

### 4.1 Composer 配置

#### composer.json 示例

```json
{
  "name": "tigshop/tigshop-api-php",
  "description": "TigShop PHP Backend API",
  "type": "project",
  "keywords": [
    "ecommerce",
    "b2b2c",
    "saaS"
  ],
  "license": "MIT",
  "authors": [
    {
      "name": "TigShop Team",
      "email": "support@tigshop.com"
    }
  ],
  "require": {
    "php": ">=8.0",
    "topthink/framework": "^6.0",
    "topthink/think-orm": "^2.0",
    "firebase/php-jwt": "^6.0",
    "phpmailer/phpmailer": "^6.5",
    "phpoffice/phpspreadsheet": "^1.25",
    "ext-swoole": "*",
    "ext-pdo": "*",
    "ext-redis": "*",
    "topthink/think-image": "^1.0",
    "guzzlehttp/guzzle": "^7.0"
  },
  "require-dev": {
    "phpunit/phpunit": "^9.0",
    "friendsofphp/php-cs-fixer": "^3.0"
  },
  "autoload": {
    "psr-4": {
      "app\\": "app/"
    },
    "files": [
      "app/common.php"
    ]
  },
  "config": {
    "preferred-install": "dist",
    "sort-packages": true,
    "platform-check": false
  },
  "scripts": {
    "post-autoload-dump": [
      "php think clear"
    ]
  }
}
```

### 4.2 环境变量配置

#### .example.env

```env
# 应用配置
APP_NAME=TigShop API
APP_DEBUG=true
APP_URL=http://localhost:8080

# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=tigshop
DB_USER=root
DB_PASSWORD=your_password
DB_PREFIX=tig_

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DB=0

# JWT 配置
JWT_KEY=your-secret-key-for-jwt
JWT_ALGORITHM=HS256
JWT_EXPIRE=3600

# RabbitMQ 配置
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASS=guest
RABBITMQ_VHOST=/

# 邮箱配置
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=your-email@example.com
MAIL_PASSWORD=your-password
MAIL_ENCRYPTION=tls

# 验证码配置
CAPTCHA_SECRET_KEY=your-captcha-secret-key

# 文件上传配置
UPLOAD_MAX_SIZE=104857600
UPLOAD_ROOT_PATH=./public/upload
```

### 4.3 数据库配置

#### config/database.php

```php
<?php
return [
    // 默认数据库配置
    'default' => env('database.driver', 'mysql'),
    
    // 连接配置
    'connections' => [
        'mysql' => [
            'type' => 'mysql',
            'host' => env('database.host', 'localhost'),
            'port' => env('database.port', 3306),
            'database' => env('database.name', 'tigshop'),
            'username' => env('database.user', 'root'),
            'password' => env('database.password', ''),
            'charset' => 'utf8mb4',
            'prefix' => env('database.prefix', 'tig_'),
            'collation' => 'utf8mb4_unicode_ci',
            'pool' => [
                'size' => 10,
                'timeout' => 30,
                'retry' => 3,
                'check_interval' => 5
            ],
            'debug' => env('app.debug', false)
        ]
    ],
    
    // 读写分离配置
    'master_slave' => [
        'read_master_percent' => 100,
        'slave' => []
    ],
    
    // 事务配置
    'transaction_timeout' => 60,
];
```

### 4.4 JWT 配置

#### config/jwt.php

```php
<?php
return [
    // 密钥（生产环境一定要修改）
    'key' => env('JWT_KEY', 'change-this-secret-key'),
    
    // 算法
    'algorithm' => env('JWT_ALGORITHM', 'HS256'),
    
    // 过期时间（秒）
    'expire' => env('JWT_EXPIRE', 7200),
    
    // 刷新令牌
    'refresh_expire' => env('JWT_REFRESH_EXPIRE', 604800),
    
    // 黑名单
    'blacklist' => [
        'enabled' => true,
        'store' => 'redis'
    ],
    
    // 授权头名称
    'authorization_header' => 'Authorization'
];
```

### 4.5 Swoole 配置

#### config/swoole.php

```php
<?php
return [
    // 模式：1-server 模式，2-http 模式
    'mode' => 2,
    
    // host
    'host' => '0.0.0.0',
    
    // port
    'port' => 9501,
    
    // server name
    'server_name' => 'tigshop-api',
    
    // worker num
    'worker_num' => 4,
    
    // task worker num
    'task_worker_num' => 2,
    
    // task max requests
    'task_max_requests' => 10000,
    
    // daemon mode
    'daemonize' => 0,
    
    // pid file
    'pid_file' => runtime_path() . 'tigshop.pid',
    
    // log file
    'log_file' => runtime_path() . 'swoole.log',
    
    // log level
    'log_level' => 1,
    
    // document root
    'document_root' => public_path(),
    
    // router
    'router' => []
];
```

---

## 5. 新增业务完整流程

以下以 **Brand（品牌管理）** 为例，演示如何从零开始开发一个新业务模块。

### Step 1: 数据库建表

```sql
CREATE TABLE `tig_brand` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '品牌 ID',
  `brand_name` varchar(50) NOT NULL COMMENT '品牌名称',
  `brand_logo` varchar(255) DEFAULT NULL COMMENT '品牌 Logo URL',
  `brand_desc` text COMMENT '品牌描述',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序权重',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态:0 禁用 1 启用',
  `created_at` int DEFAULT NULL COMMENT '创建时间',
  `updated_at` int DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_brand_name` (`brand_name`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品品牌表';
```

### Step 2: 创建模型

**位置**: `app/model/Brand.php`

```php
<?php
namespace app\model;

use app\model\BaseModel;

/**
 * 品牌模型
 */
class Brand extends BaseModel
{
    // 表名
    protected $table = 'brand';
    
    // 主键
    protected $pk = 'id';
    
    // 自动时间戳
    protected $autoWriteTimestamp = true;
    protected $createTime = 'created_at';
    protected $updateTime = 'updated_at';
    
    /**
     * 获取品牌列表
     */
    public function getBrandList($params = [])
    {
        $where = [];
        
        // 名称搜索
        if (!empty($params['name'])) {
            $where[] = ['brand_name', 'like', '%' . $params['name'] . '%'];
        }
        
        // 状态筛选
        if (isset($params['status'])) {
            $where[] = ['status', '=', $params['status']];
        }
        
        // 分页
        $page = isset($params['page']) ? intval($params['page']) : 1;
        $limit = isset($params['limit']) ? intval($params['limit']) : 15;
        
        // 排序
        $order = isset($params['sort_field']) ? $params['sort_field'] . ' ' . $params['sort_order'] : 'sort_order ASC, created_at DESC';
        
        $list = $this->where($where)
            ->order($order)
            ->page($page, $limit)
            ->select();
        
        foreach ($list as &$item) {
            $item->created_at = date('Y-m-d H:i:s', $item->created_at);
            $item->updated_at = date('Y-m-d H:i:s', $item->updated_at);
        }
        
        return $list;
    }
    
    /**
     * 获取品牌详情
     */
    public function getBrandDetail($id)
    {
        $brand = $this->find($id);
        
        if ($brand) {
            $brand->created_at = date('Y-m-d H:i:s', $brand->created_at);
            $brand->updated_at = date('Y-m-d H:i:s', $brand->updated_at);
        }
        
        return $brand;
    }
    
    /**
     * 创建品牌
     */
    public function createBrand($data)
    {
        $data['created_at'] = time();
        $data['updated_at'] = time();
        
        return $this->allowField(true)->save($data);
    }
    
    /**
     * 更新品牌
     */
    public function updateBrand($id, $data)
    {
        $data['updated_at'] = time();
        
        return $this->allowField(true)->where('id', $id)->update($data);
    }
    
    /**
     * 删除品牌
     */
    public function deleteBrands($ids)
    {
        return $this->whereIn('id', $ids)->delete();
    }
}
```

### Step 3: 创建控制器

**位置**: `app/adminapi/controller/product/Brand.php`

```php
<?php
namespace app\adminapi\controller\product;

use app\adminapi\BaseController;
use app\common\exception\ApiException;
use app\model\Brand;
use think\Validate;
use think\Request;

/**
 * 品牌管理控制器
 */
class BrandController extends BaseController
{
    protected $brandModel;
    
    public function initialize()
    {
        parent::initialize();
        $this->brandModel = new Brand();
    }
    
    /**
     * 品牌列表
     */
    public function list()
    {
        try {
            $params = $this->request->param();
            
            // 参数验证
            $validate = new Validate([
                ['page', 'require|integer', '页码必填'],
                ['limit', 'require|integer', '数量必填']
            ], [
                'page.require' => '页码不能为空',
                'page.integer' => '页码必须为整数',
                'limit.require' => '数量不能为空',
                'limit.integer' => '数量必须为整数'
            ]);
            
            if (!$validate->check($params)) {
                return $this->error($validate->getError());
            }
            
            // 获取列表
            $brands = $this->brandModel->getBrandList($params);
            
            return $this->success($brands);
            
        } catch (ApiException $e) {
            return $this->error($e->getMessage(), $e->getCode());
        } catch (\Exception $e) {
            return $this->error('查询失败');
        }
    }
    
    /**
     * 品牌详情
     */
    public function info()
    {
        try {
            $id = input('id/d');
            
            if (!$id) {
                return $this->error('ID 不能为空');
            }
            
            $brand = $this->brandModel->getBrandDetail($id);
            
            if (!$brand) {
                return $this->error('品牌不存在');
            }
            
            return $this->success($brand);
            
        } catch (\Exception $e) {
            return $this->error('查询失败');
        }
    }
    
    /**
     * 新增品牌
     */
    public function add()
    {
        try {
            $data = $this->request->post();
            
            // 参数验证
            $validate = new Validate([
                ['brand_name', 'require', '品牌名称不能为空'],
                ['sort_order', 'integer', '排序必须为数字']
            ], [
                'brand_name.require' => '品牌名称不能为空'
            ]);
            
            if (!$validate->check($data)) {
                return $this->error($validate->getError());
            }
            
            // 检查重名
            $exists = $this->brandModel->where('brand_name', $data['brand_name'])->find();
            if ($exists) {
                return $this->error('品牌名称已存在');
            }
            
            // 创建
            $result = $this->brandModel->createBrand($data);
            
            if ($result) {
                return $this->success([], '创建成功');
            } else {
                return $this->error('创建失败');
            }
            
        } catch (ApiException $e) {
            return $this->error($e->getMessage(), $e->getCode());
        } catch (\Exception $e) {
            return $this->error('创建失败');
        }
    }
    
    /**
     * 编辑品牌
     */
    public function edit()
    {
        try {
            $id = input('id/d');
            
            if (!$id) {
                return $this->error('ID 不能为空');
            }
            
            $data = $this->request->post();
            $data['id'] = $id;
            
            // 参数验证
            $validate = new Validate([
                ['brand_name', 'require', '品牌名称不能为空']
            ]);
            
            if (!$validate->check($data)) {
                return $this->error($validate->getError());
            }
            
            // 检查重名（排除自己）
            $exists = $this->brandModel->where('brand_name', $data['brand_name'])
                ->where('id', '<>', $id)
                ->find();
            
            if ($exists) {
                return $this->error('品牌名称已存在');
            }
            
            // 更新
            $result = $this->brandModel->updateBrand($id, $data);
            
            if ($result !== false) {
                return $this->success([], '更新成功');
            } else {
                return $this->error('更新失败');
            }
            
        } catch (\Exception $e) {
            return $this->error('更新失败');
        }
    }
    
    /**
     * 删除品牌
     */
    public function delete()
    {
        try {
            $ids = input('ids/a');
            
            if (empty($ids)) {
                return $this->error('ID 不能为空');
            }
            
            $result = $this->brandModel->deleteBrands($ids);
            
            if ($result !== false) {
                return $this->success([], '删除成功');
            } else {
                return $this->error('删除失败');
            }
            
        } catch (\Exception $e) {
            return $this->error('删除失败');
        }
    }
}
```

### Step 4: 配置路由

**位置**: `app/adminapi/route/product.php`

```php
<?php
// 品牌管理路由
return [
    // 品牌列表
    ['GET|list', 'product/Brand/list', ''],
    
    // 品牌详情
    ['GET|info/:id', 'product/Brand/info', ''],
    
    // 新增品牌
    ['POST|add', 'product/Brand/add', ''],
    
    // 编辑品牌
    ['POST|edit', 'product/Brand/edit', ''],
    
    // 删除品牌
    ['POST|delete', 'product/Brand/delete', '']
];
```

或者在 `app/adminapi/route/adminapi.php` 中统一注册：

```php
<?php
return [
    // ... 其他路由
    
    // 品牌管理
    'product/brand' => [
        ['GET', 'list'],
        ['GET', 'info/<id>'],
        ['POST', 'add'],
        ['POST', 'edit'],
        ['POST', 'delete']
    ]
];
```

### Step 5: 测试接口

**Postman 测试**:

```bash
# 获取品牌列表
GET http://localhost:8080/adminapi/product/brand/list?page=1&limit=15

# 获取品牌详情
GET http://localhost:8080/adminapi/product/brand/info?id=1

# 新增品牌
POST http://localhost:8080/adminapi/product/brand/add
Body (JSON):
{
  "brand_name": "Nike",
  "brand_desc": "知名运动品牌",
  "sort_order": 10,
  "status": 1
}

# 编辑品牌
POST http://localhost:8080/adminapi/product/brand/edit
Body (JSON):
{
  "id": 1,
  "brand_name": "Adidas",
  "sort_order": 20
}

# 删除品牌
POST http://localhost:8080/adminapi/product/brand/delete
Body (JSON):
{
  "ids": [1, 2, 3]
}
```

---

## 6. 接口文档使用

### 6.1 ApiPost 在线文档

TigShop 集成了 ApiPost 作为 API 文档管理和协作工具。

**访问地址**: https://s.apifox.cn/b2b655d3-894e-49e2-b74d-a59c4d4240cb

**主要功能**:
- 接口列表导航
- 请求参数说明
- 响应数据结构
- 在线调试
- 团队协作文档

### 6.2 文档注释规范

```php
<?php
/**
 * 品牌管理接口集合
 * @module 品牌管理
 */
class BrandController extends BaseController
{
    /**
     * 获取品牌列表
     * @route GET /product/brand/list
     * @param integer $page 页码
     * @param integer $limit 每页数量
     * @return array 品牌列表数据
     */
    public function list()
    {
        // ...
    }
    
    /**
     * 新增品牌
     * @route POST /product/brand/add
     * @param string $brand_name 品牌名称
     * @param string $brand_desc 品牌描述
     * @param integer $sort_order 排序
     * @param integer $status 状态
     * @return boolean 是否成功
     */
    public function add()
    {
        // ...
    }
}
```

---

## 7. 常见问题与优化

### 7.1 启动问题

#### 问题 1: Swoole 扩展未安装
```bash
# 检查 Swoole 是否安装
php -m | grep swoole

# 如果没有安装，安装 Swoole
pecl install swoole

# 或编译安装
cd swoole-src && phpize && ./configure && make && make install
```

#### 问题 2: Composer 依赖安装失败
```bash
# 清理缓存
composer clear-cache

# 升级 Composer
composer self-update

# 重新安装
composer install --no-dev
```

### 7.2 性能优化

#### 1. 开启 Opcache
```ini
; php.ini
opcache.enable=1
opcache.memory_consumption=128
opcache.interned_strings_buffer=8
opcache.max_accelerated_files=4000
opcache.revalidate_freq=60
```

#### 2. Redis 缓存优化
```php
<?php
// 缓存商品列表
Cache::set('products_list_' . $key, $data, 3600);

// 获取缓存
$data = Cache::get('products_list_' . $key);
if (!$data) {
    $data = $this->productModel->getProductList($params);
    Cache::set('products_list_' . $key, $data, 3600);
}
```

#### 3. 数据库查询优化
```php
<?php
// 使用索引
$where = [['status', '=', 1], ['category_id', '=', $id]];

// 只查询需要的字段
$list = $this->field('id,name,price,image')->select();

// 避免 N+1 查询
$this->relation('category,brand')->select();
```

### 7.3 调试技巧

#### 1. 开启调试模式
```php
// config/app.php
'debug' => true,
```

#### 2. 日志记录
```php
<?php
// 记录日志
\Log::info('商品创建成功', ['id' => $id, 'name' => $name]);
\Log::error('订单支付失败', ['order_id' => $orderId]);
```

#### 3. 数据库慢查询
```sql
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
```

---

*本文档由 TigShop 开发团队维护*  
*最后更新：2024-07-18*  
*文档版本：Part 4 - PHP Backend Development Guide*
