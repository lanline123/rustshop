# TigShop 开源商城系统 - 第 3 部分：JAVA 后端开发指南

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

TigShop JAVA 后端基于 Spring Boot 生态体系构建，采用微服务架构思想，主要技术栈如下：

| 层次 | 组件 | 技术选型 | 版本 |
|------|------|----------|------|
| **核心框架** | Spring Boot | Spring Framework | 2.7.x+ |
| **ORM 框架** | MyBatis Plus | ORM 增强工具 | 3.5.x+ |
| **API 文档** | Swagger/Knife4j | RESTful 接口文档 | 4.x+ |
| **安全框架** | Spring Security** | 身份验证/授权 | 5.7.x+ |
| **服务调用** | OpenFeign** | 远程服务调用 | 3.x+ |
| **消息队列** | RabbitMQ** | 异步消息处理 | 5.x+ |
| **缓存** | Redis** | 分布式缓存 | 6.x+ |
| **数据库连接池** | HikariCP** | JDBC 连接池 | 4.x+ |
| **日志** | Logback + Lombok** | 日志记录/代码简化 | latest |

### 1.2 架构设计

```
┌──────────────────────────────────────────────────────────┐
│                    API Gateway Layer                      │
│                  (nginx / Kong / Spring Cloud)            │
└───────────────────────┬──────────────────────────────────┘
                        ↓
┌──────────────────────────────────────────────────────────┐
│                 Application Layer                         │
│   ┌─────────────┐  ┌─────────────┐  ┌──────────────┐    │
│   │ Admin API   │  │   User API  │  │   Common API │    │
│   │ (后台管理)   │  │  (前台用户)  │  │  (公共接口)  │    │
│   └──────┬──────┘  └──────┬──────┘  └──────┬───────┘    │
└──────────┴────────────────┴────────────────┴────────────┘
                        ↓
┌──────────────────────────────────────────────────────────┐
│                   Service Layer                           │
│   ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│   │ Merchant │ │ Product  │ │ Order    │ │ Promotion│   │
│   │ Service  │ │ Service  │ │ Service  │ │ Service  │   │
│   └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘   │
└────────┼───────────┼────────────┼────────────┼─────────┘
         │           │            │            │
         ↓           ↓            ↓            ↓
┌──────────────────────────────────────────────────────────┐
│                   Persistence Layer                       │
│   ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│   │ Mapper   │ │ Mapper   │ │ Mapper   │ │ Mapper   │   │
│   └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘   │
└────────┼───────────┼────────────┼────────────┼─────────┘
         │           │            │            │
         ↓           ↓            ↓            ↓
┌──────────────────────────────────────────────────────────┐
│                     Data Layer                            │
│        MySQL     Redis     RabbitMQ      Elasticsearch   │
└──────────────────────────────────────────────────────────┘
```

---

## 2. 项目架构与目录结构

### 2.1 整体项目结构

```
tigshop-api-java/                                    # 项目根目录
├── database/                                        # 数据库相关文件
│   ├── sql/                                         # SQL 脚本
│   ├── data/                                        # 初始化数据
│   └── migration/                                   # 数据库迁移脚本
│
├── logs/                                            # 日志文件目录
├── opt/                                             # 可选配置或扩展资源目录
├── public/                                          # 静态资源目录（前端打包后）
├── rabbitmq-plugins/                                # RabbitMQ 插件目录
│
├── tigshop-adminapi/                               # 后台管理 API 模块 ⭐
│   ├── src/main/java/com/tigshop/admin/
│   │   ├── controller/                             # 控制器层
│   │   │   ├── authority/                          # 权限管理控制器
│   │   │   │   ├── AdminRoleController.java       # 角色管理
│   │   │   │   ├── AdminUserController.java       # 管理员管理
│   │   │   │   └── AuthorityController.java       # 权限管理
│   │   │   ├── merchant/                           # 商户管理控制器
│   │   │   │   ├── AccountController.java         # 商户账户
│   │   │   │   ├── ApplyController.java           # 入驻申请
│   │   │   │   ├── MerchantController.java        # 商户管理
│   │   │   │   ├── ShopController.java            # 店铺管理
│   │   │   │   ├── ShopAccountController.java     # 资产控制
│   │   │   │   └── ShopWithdrawController.java    # 提现管理
│   │   │   ├── order/                              # 订单管理控制器
│   │   │   │   ├── AftersalesController.java      # 售后管理
│   │   │   │   ├── OrderController.java           # 订单管理
│   │   │   │   └── OrderLogController.java        # 订单日志
│   │   │   ├── panel/                              # 后台面板控制器
│   │   │   │   ├── PanelController.java           # 控制台
│   │   │   │   ├── SalesStatisticsController.java # 销售统计
│   │   │   │   ├── StatisticsAccessController.java# 访问统计
│   │   │   │   └── StatisticsUserController.java  # 用户统计
│   │   │   ├── product/                            # 商品管理控制器
│   │   │   │   ├── BrandController.java           # 品牌管理
│   │   │   │   ├── CategoryController.java        # 分类管理
│   │   │   │   ├── ProductController.java         # 商品管理
│   │   │   │   └── ProductBatchController.java    # 批量处理
│   │   │   ├── promotion/                          # 营销管理控制器
│   │   │   │   ├── CouponController.java          # 优惠券
│   │   │   │   ├── ProductGiftController.java     # 活动赠品
│   │   │   │   ├── ProductPromotionController.java# 满减满赠
│   │   │   │   ├── PromotionController.java       # 优惠活动
│   │   │   │   ├── RechargeSettingController.java # 充值余额
│   │   │   │   ├── SeckillController.java         # 秒杀管理
│   │   │   │   └── TimeDiscountController.java    # 限时折扣
│   │   │   ├── setting/                            # 配置管理控制器
│   │   │   │   ├── ConfigController.java          # 配置管理
│   │   │   │   └── FriendLinksController.java     # 友情链接
│   │   │   └── user/                               # 用户管理控制器
│   │   │       ├── FeedbackController.java        # 会员留言
│   │   │       └── UserController.java            # 会员管理
│   │   │
│   │   ├── common/                                 # 公共控制器
│   │   │   ├── CacheManageController.java         # 缓存管理
│   │   │   ├── ToolController.java                # 工具管理
│   │   │   └── VerificationController.java        # 验证管理
│   │   │
│   │   ├── login/                                  # 登录控制器
│   │   │   └── LoginController.java               # 统一登录入口
│   │   │
│   │   ├── config/                                 # 配置类
│   │   │   ├── SwaggerConfig.java                 # Swagger 配置
│   │   │   ├── SecurityConfig.java                # 安全配置
│   │   │   ├── MybatisPlusConfig.java             # MyBatis Plus 配置
│   │   │   └── RedisConfig.java                   # Redis 配置
│   │   │
│   │   ├── exception/                              # 异常处理
│   │   │   ├── GlobalExceptionHandler.java        # 全局异常处理器
│   │   │   └── BusinessExceptionHandler.java      # 业务异常处理
│   │   │
│   │   ├── interceptor/                            # 拦截器
│   │   │   └── PermissionInterceptor.java         # 权限拦截器
│   │   │
│   │   ├── aspect/                                 # 切面
│   │   │   ├── LogAspect.java                     # 操作日志切面
│   │   │   └── RetryAspect.java                   # 重试切面
│   │   │
│   │   ├── job/                                    # 定时任务
│   │   │   ├── OrderCancelJob.java                # 取消订单任务
│   │   │   ├── MessageJob.java                    # 发送站内信
│   │   │   ├── MiniProgramJob.java                # 发送订阅消息
│   │   │   ├── SmsJob.java                        # 发送短信队列
│   │   │   └── WechatJob.java                     # 发送公众号消息
│   │   │
│   │   ├── middleware/                             # 中间件
│   │   │   ├── AllowCrossDomainFilter.java        # 跨域过滤器
│   │   │   └── ReptilesFilter.java                # 防爬虫过滤器
│   │   │
│   │   └── adminapiApplication.java               # 启动类
│   │
│   ├── src/main/resources/
│   │   ├── application.yml                          # 主配置文件
│   │   ├── application-dev.yml                      # 开发环境配置
│   │   ├── application-pro.yml                      # 生产环境配置
│   │   ├── application-test.yml                     # 测试环境配置
│   │   └── logback-spring.xml                      # 日志配置
│   │
│   └── src/test/java/                              # 测试代码
│
├── tigshop-api/                                     # 前台 API 模块 ⭐
│   ├── src/main/java/com/tigshop/api/
│   │   ├── controller/                              # 前台控制器
│   │   │   ├── cart/                               # 购物车控制器
│   │   │   │   └── CartController.java
│   │   │   ├── category/                           # 商品分类控制器
│   │   │   │   └── CategoryController.java
│   │   │   ├── home/                               # 首页控制器
│   │   │   │   └── HomeController.java
│   │   │   ├── order/                              # 订单控制器
│   │   │   │   ├── CheckController.java           # 订单验证
│   │   │   │   └── PayController.java             # 支付处理
│   │   │   ├── product/                            # 商品控制器
│   │   │   │   └── ProductController.java
│   │   │   └── user/                               # 会员中心控制器
│   │   │       ├── AccountController.java         # 账户明细
│   │   │       ├── AftersalesController.java      # 售后服务
│   │   │       └── UserController.java            # 用户中心
│   │   │
│   │   ├── library/                                # 外部库集成
│   │   │   ├── PaymentLibrary.java                # 支付集成
│   │   │   └── ImSocketClient.java                # IM 通信
│   │   │
│   │   └── apiApplication.java                     # 启动类
│   │
│   └── src/main/resources/
│       ├── application.yml
│       └── application-dev.yml
│
├── tigshop-bean/                                    # 实体类模块 ⭐
│   └── src/main/java/com/tigshop/bean/
│       └── model/                                  # 模型定义
│           ├── authority/                          # 权限相关实体
│           │   ├── AdminRole.java                 # 角色表
│           │   ├── AdminUser.java                 # 管理员表
│           │   └── Authority.java                 # 权限表
│           │
│           ├── merchant/                           # 商户相关实体
│           │   ├── Apply.java                     # 入驻申请表
│           │   ├── Merchant.java                  # 商户表
│           │   ├── MerchantAccount.java           # 商户资产表
│           │   └── Shop.java                      # 店铺表
│           │
│           ├── order/                              # 订单相关实体
│           │   ├── Aftersales.java                # 售后单表
│           │   ├── Cart.java                      # 购物车表
│           │   └── Order.java                     # 订单表
│           │
│           ├── product/                            # 商品相关实体
│           │   ├── Brand.java                     # 品牌表
│           │   ├── Category.java                  # 商品分类表
│           │   └── Product.java                   # 商品表
│           │
│           ├── promotion/                          # 营销相关实体
│           │   ├── Coupon.java                    # 优惠券表
│           │   ├── ProductGift.java               # 赠品表
│           │   └── Promotion.java                 # 营销活动表
│           │
│           ├── setting/                            # 配置相关实体
│           │   ├── Config.java                    # 配置表
│           │   ├── FriendLinks.java               # 友链表
│           │   └── Region.java                    # 地区表
│           │
│           └── user/                               # 用户相关实体
│               ├── CollectProduct.java            # 商品收藏表
│               ├── Feedback.java                  # 留言表
│               └── UserCoupon.java                # 会员优惠券表
│
├── tigshop-common/                                  # 公共工具模块 ⭐
│   └── src/main/java/com/tigshop/common/
│       ├── util/                                   # 工具类
│       │   ├── Config.java                        # 配置读取工具
│       │   ├── Excel.java                         # Excel 导出工具
│       │   ├── Format.java                        # 格式化工具
│       │   ├── HttpUtil.java                      # HTTP 请求工具
│       │   ├── PHPMailerWrapper.java              # 邮件工具
│       │   ├── TigQueue.java                      # 队列工具
│       │   ├── TimeUtil.java                      # 时间工具
│       │   └── Util.java                          # 通用工具
│       │
│       ├── exception/                              # 异常类
│       │   └── ApiException.java                  # 业务异常类
│       │
│       └── traits/                                 # 功能片段
│           └── OutputTrait.java                   # 格式化输出片段
│
├── tigshop-feign/                                   # Feign 客户端模块 ⭐
│   └── src/main/java/com/tigshop/feign/
│       └── client/                                 # Feign 客户端接口
│           ├── ProductServiceClient.java          # 商品服务调用
│           └── UserServiceClient.java             # 用户服务调用
│
├── tigshop-security/                                # 安全认证模块 ⭐
│   └── src/main/java/com/tigshop/security/
│       └── filter/                                 # 安全过滤器
│           ├── JwtAuthenticationFilter.java       # JWT 认证过滤器
│           └── PermissionCheckFilter.java         # 权限检查过滤器
│
├── tigshop-service/                                 # 核心业务模块 ⭐⭐⭐
│   └── src/main/java/com/tigshop/service/
│       ├── mapper/                                 # Mapper 接口
│       │   ├── authority/                         # 权限 Mapper
│       │   ├── merchant/                          # 商户 Mapper
│       │   ├── order/                             # 订单 Mapper
│       │   ├── product/                           # 商品 Mapper
│       │   ├── promotion/                         # 营销 Mapper
│       │   ├── setting/                           # 配置 Mapper
│       │   └── user/                              # 用户 Mapper
│       │
│       └── service/                                # 服务实现
│           ├── authority/                         # 权限服务
│           │   ├── AdminRoleService.java          # 角色服务
│           │   ├── AdminUserService.java          # 管理员服务
│           │   └── AuthorityService.java          # 权限服务
│           │
│           ├── merchant/                          # 商户服务
│           │   ├── ApplyService.java              # 入驻申请服务
│           │   ├── MerchantCoreService.java       # 商户核心服务
│           │   ├── ApplyCoreService.java          # 申请核心服务
│           │   └── ShopService.java               # 店铺服务
│           │
│           ├── order/                             # 订单服务
│           │   ├── AftersalesService.java         # 售后服务
│           │   ├── CartService.java               # 购物车服务
│           │   ├── OrderDetailService.java        # 订单详情服务
│           │   └── OrderCoreService.java          # 订单核心服务
│           │
│           ├── product/                           # 商品服务
│           │   ├── BrandService.java              # 品牌服务
│           │   ├── CategoryService.java           # 分类服务
│           │   └── ProductService.java            # 商品服务
│           │
│           ├── promotion/                         # 营销服务
│           │   ├── PromotionService.java          # 营销服务
│           │   ├── CouponService.java             # 优惠券服务
│           │   └── ProductGiftService.java        # 赠品服务
│           │
│           └── user/                              # 用户服务
│               ├── CollectProductService.java     # 商品收藏服务
│               ├── FeedbackService.java           # 留言服务
│               └── UserService.java               # 会员服务
│
├── view/                                            # 视图层资源（前端）
├── .env                                             # 环境变量文件
├── .gitignore                                       # Git 忽略配置
├── build-and-run.bat                               # Windows 构建运行脚本
├── build-and-run.sh                                # Linux/macOS 构建运行脚本
├── docker-compose.yml                              # Docker Compose 配置
├── mvnw                                            # Maven Wrapper (Unix)
├── mvnw.cmd                                        # Maven Wrapper (Windows)
└── pom.xml                                         # Maven 配置文件
```

### 2.2 核心模块说明

#### tigshop-adminapi - 后台管理 API
负责为管理后台提供所有业务接口，包括商户管理、商品管理、订单管理、系统配置等功能。

**特点**:
- 完整的 RBAC 权限控制
- 详细的操作日志记录
- 支持批量操作
- 丰富的统计报表

#### tigshop-api - 前台 API
为移动端、PC 端用户提供购物相关的接口服务。

**特点**:
- 高性能查询优化
- 完善的缓存策略
- 灵活的促销支持
- 实时库存扣减

#### tigshop-bean - 实体类模块
定义所有数据库表对应的 Java Bean 对象。

**特点**:
- 统一的注解配置
- 完善的字段注释
- 支持多语言描述

#### tigshop-service - 核心业务模块
实现所有业务逻辑，是整个系统的核心。

**特点**:
- 分层清晰（Controller → Service → Mapper）
- 事务管理完善
- 支持复杂业务场景
- 易于扩展和复用

#### tigshop-common - 公共工具模块
提供通用的工具类和辅助方法。

**特点**:
- 无状态设计
- 线程安全
- 性能优化

---

## 3. 开发规范详解

### 3.1 命名规范

#### 包名规范
```java
// ✅ 正确示例
com.tigshop.admin.controller
com.tigshop.service.order
com.tigshop.bean.product

// ❌ 错误示例
com.TigShop.Admin.Controller // 大驼峰
com.tigshopadmin // 没有模块分隔
```

**规则**:
- 全部小写字母
- 使用点号分隔层级
- 标准顺序：`公司域名。项目名称。模块名。层名`

#### 类名规范
```java
// Controller 层
public class UserController { }           // 用户控制器
public class ProductController { }         // 商品控制器

// Service 层
public interface UserService { }           // 用户服务接口
public class UserServiceImpl implements UserService { } // 实现类

// Mapper 层
@Mapper
public interface UserMapper { }            // 用户数据访问层

// Bean 层
@TableName("user_info")
@Data
public class UserInfo { }                  // 用户实体类
```

**规则**:
- Controller: `{业务}Controller`
- Service Interface: `{业务}Service`
- Service Impl: `{业务}ServiceImpl`
- Mapper: `{业务}Mapper`
- Bean: `{业务}` 或 `{业务}DO`

#### 方法名规范
```java
// ✅ 动词 + 名词结构
getUserById(Long id)
createOrder(OrderDTO order)
updateUserInfo(UserInfo user)
deleteProduct(Long productId)
listProducts(ProductQuery query)

// ❌ 避免
get()                           // 语义不明确
doSomething()                   // 过于笼统
handle()                        // 职责不清
```

**规则**:
- get/查询：`get{Entity}/{queryType}`
- create/新增：`create{Entity}`
- update/修改：`update{Entity}`
- delete/删除：`delete{Entity}`
- list/列表：`list{Entity}s` 或 `query{Entity}s`

#### 变量名规范
```java
// ✅ 正确示例
private Long userId;
private String userName;
private List<Order> orderList;
private Map<String, Object> params;

// ❌ 错误示例
private long id;            // 缺少语义
private String name;        // 缺少范围限定
private List list;          // 泛型缺失
```

**规则**:
- 基本类型：`is/isFlag/enable/count/total`
- 对象引用：`{EntityName}`
- 集合：`{EntityName}s/List/{EntityName}sList`
- 映射：`map/mapDict`

#### 常量名规范
```java
// ✅ 正确示例
public static final int MAX_RETRY_COUNT = 3;
public static final String STATUS_SUCCESS = "success";
public static final String CACHE_USER_PREFIX = "user:";

// ❌ 错误示例
int maxCount = 3;              // 不是常量
String status = "成功";         // 包含中文
```

**规则**:
- 全大写字母
- 单词间用下划线分隔
- 语义明确

### 3.2 数据库命名规范

#### 表命名
```sql
-- ✅ 推荐
user_info               -- 用户信息表
order_detail            -- 订单详情表
product_category        -- 商品分类表
merchant_apply          -- 商户申请表
admin_role              -- 管理员角色表

-- ❌ 不推荐
u                       -- 太简短
userinfo                -- 缺少分隔
t_user                  -- 多余的前缀
```

#### 字段命名
```sql
-- ✅ 推荐
user_id                 -- 主键
user_name               -- 用户名
order_status            -- 订单状态
created_at              -- 创建时间
updated_at              -- 更新时间

-- ❌ 不推荐
uid                     -- 过于简写
name                    -- 缺少范围
addtime                  -- 不规范的缩写
```

#### 索引命名
```sql
-- ✅ 推荐
PRIMARY KEY             -- 主键索引
idx_user_id             -- 唯一索引
idx_order_status        -- 普通索引
uk_user_name            -- 唯一约束

-- 命名规则
PK_表名
IDX_表名_字段名
UK_表名_字段名
FK_表名_关联表名
```

### 3.3 代码结构与分层

#### Controller 层规范
```java
@RestController
@RequestMapping("/admin/product")
@Tag(name = "商品管理", description = "商品信息 CRUD 操作")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取商品列表
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('product:list')")
    @Operation(summary = "商品列表查询")
    public AjaxResult list(ProductQuery query) {
        return success(productService.list(query));
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "商品详情查询")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(productService.getById(id));
    }

    /**
     * 新增商品
     */
    @PostMapping
    @PreAuthorize("@ss.hasPermi('product:add')")
    @Operation(summary = "新增商品")
    public AjaxResult add(@Validated @RequestBody ProductDTO dto) {
        return toAjax(productService.save(dto));
    }

    /**
     * 修改商品
     */
    @PutMapping
    @PreAuthorize("@ss.hasPermi('product:edit')")
    @Operation(summary = "修改商品")
    public AjaxResult edit(@Validated @RequestBody ProductDTO dto) {
        return toAjax(productService.update(dto));
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPermi('product:remove')")
    @Operation(summary = "批量删除商品")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(productService.removeByIds(Arrays.asList(ids)));
    }
}
```

**关键要点**:
- 使用 RESTful 风格的路径设计
- 参数校验使用 `@Validated`
- 返回结果统一使用 `AjaxResult`
- 添加 Swagger 注解便于生成文档
- 权限控制使用 `@PreAuthorize`

#### Service 层规范
```java
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> 
        implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public IPage<Product> pageList(Page<Product> page, ProductQuery query) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        
        // 条件构建
        wrapper.eq(query.getCategoryId() != null, "category_id", query.getCategoryId())
               .like(StrUtil.isNotBlank(query.getKeyword()), "product_name", query.getKeyword())
               .orderByDesc("created_at");
        
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveProduct(ProductDTO dto) {
        // 1. 参数校验
        if (!isValid(dto)) {
            throw new BusinessException("参数校验失败");
        }
        
        // 2. 转换为实体
        Product product = BeanUtil.copyProperties(dto, Product.class);
        
        // 3. 设置默认值
        product.setCreateTime(LocalDateTime.now());
        product.setStatus(1);
        
        // 4. 保存数据
        boolean result = this.save(product);
        
        // 5. 保存关联数据
        if (result && CollectionUtil.isNotEmpty(dto.getImages())) {
            saveProductImages(product.getId(), dto.getImages());
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProduct(ProductDTO dto) {
        // 1. 检查商品是否存在
        Product existing = this.getById(dto.getId());
        if (existing == null) {
            throw new BusinessException("商品不存在");
        }
        
        // 2. 更新数据
        Product product = BeanUtil.copyProperties(dto, Product.class);
        product.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(product);
    }
}
```

**关键要点**:
- 使用 `@RequiredArgsConstructor` 自动注入 final 属性
- 事务控制使用 `@Transactional`
- 异常处理要捕获并抛出业务异常
- 参数校验在 Service 层二次确认
- 复杂的业务逻辑拆分多个方法

#### Mapper 层规范
```java
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 根据分类 ID 查询商品数量
     */
    @Select("SELECT COUNT(*) FROM product WHERE category_id = #{categoryId}")
    int countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 查询商品详细信息（包含品牌、分类）
     */
    @SelectProvider(type = ProductSqlProvider.class, method = "selectProductWithDetail")
    Product selectProductWithDetail(@Param("productId") Long productId);

    /**
     * 批量更新商品状态
     */
    @Update("UPDATE product SET status = #{status}, updated_at = NOW() WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close>')")
    int batchUpdateStatus(@Param("status") Integer status, @Param("ids") List<Long> ids);
}
```

**关键要点**:
- 继承 `BaseMapper<T>` 获得基础 CRUD 能力
- 复杂查询使用 `@Select` 或 XML 方式
- 使用 `@Param` 指定参数名称
- 批量操作优先使用 MP 提供的批量方法

### 3.4 安全规范

#### SQL 注入防护
```java
// ✅ 正确 - 使用参数绑定
@Select("SELECT * FROM user WHERE username = #{username}")
User getByUsername(@Param("username") String username);

// ❌ 错误 - 字符串拼接
String sql = "SELECT * FROM user WHERE username = '" + username + "'";
```

#### XSS 防护
```java
// ✅ 正确 - 转义输出
@Component
public class XssFilter implements Filter {
    public void doFilter(ServletRequest request, ServletResponse response, 
                         FilterChain chain) {
        // 对输入进行 HTML 转义
        String input = ((HttpServletRequest)request).getParameter("content");
        String safeInput = StringEscapeUtils.escapeHtml4(input);
        // 继续处理
    }
}

// 模板中使用 Escaped 输出
<span th:text="${userInput}"></span>
```

#### 敏感数据加密
```java
// ✅ 密码加密
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public void register(UserRegisterDTO dto) {
        // 密码加密存储
        User user = new User();
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        save(user);
    }
}

// ✅ 敏感信息脱敏
@JsonIgnore
public String getPassword() {
    return "***";
}

// 或使用注解
@JsonSerialize(using = MaskSerializer.class)
public String getPhone() {
    return "138****1234";
}
```

### 3.5 注释规范

#### 类级注释
```java
/**
 * 商品服务层实现类
 * 负责商品的增删改查及相关业务逻辑处理
 * 
 * @author TigShop Team
 * @since 2025-04-15
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> 
        implements ProductService {
```

#### 方法级注释
```java
/**
 * 根据 ID 获取商品详情
 * 包含商品基本信息、规格 SKU、库存信息等
 *
 * @param id 商品 ID
 * @return 商品详细信息
 * @throws BusinessException 如果商品不存在
 */
@Override
public ProductVO getProductDetail(Long id) {
    // ... 实现代码
}
```

#### 业务逻辑注释
```java
// 注意：此处需要校验库存是否充足，避免超卖情况
if (stock < quantity) {
    throw new BusinessException("库存不足");
}

// TODO: 2025-05-01 需要添加优惠券核销逻辑
// FIXME: 当前并发情况下可能出现库存负数问题
// WARNING: 此方法会触发积分变更事件，需谨慎调用
```

---

## 4. 开发配置

### 4.1 Maven 配置

#### settings.xml 配置
```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          https://maven.apache.org/xsd/settings-1.0.0.xsd">
  
  <!-- 镜像配置 -->
  <mirrors>
    <!-- 阿里云 Maven 仓库 -->
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
    
    <!-- Gitee 仓库 -->
    <mirror>
      <id>gitee</id>
      <mirrorOf>gitee</mirrorOf>
      <name>Gitee 公共仓库</name>
      <url>https://gitee.com/tigshop-work/java-tigshop/raw/master</url>
    </mirror>
  </mirrors>
  
  <!-- 代理配置（如果需要） -->
  <!--
  <proxies>
    <proxy>
      <id>http-proxy</id>
      <active>true</active>
      <protocol>http</protocol>
      <host>proxy.example.com</host>
      <port>8080</port>
    </proxy>
  </proxies>
  -->
</settings>
```

位置：`~/.m2/settings.xml`

### 4.2 应用配置文件

#### application.yml - 主配置
```yaml
spring:
  application:
    name: tigshop-adminapi
  
  profiles:
    active: dev
  
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/tigshop?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    
  redis:
    host: localhost
    port: 6379
    password:
    database: 0
    timeout: 10000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-value: 1
      logic-not-delete-value: 0

server:
  port: 8080
  servlet:
    context-path: /adminapi
    
knife4j:
  enable: true
  setting:
    language: zh_cn
```

#### application-dev.yml - 开发环境
```yaml
logging:
  level:
    com.tigshop: debug
    org.springframework.web: debug

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tigshop_dev?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    
  redis:
    host: localhost
    port: 6379
    
rabbitmq:
  host: localhost
  port: 5672
  
# 关闭 Thymeleaf 缓存（开发时方便调试）
spring:
  thymeleaf:
    cache: false
```

#### application-pro.yml - 生产环境
```yaml
logging:
  level:
    com.tigshop: info
    org.springframework.web: warn

spring:
  datasource:
    url: jdbc:mysql://prod-db:3306/tigshop?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
      
  redis:
    host: prod-redis
    port: 6379
    password: ${REDIS_PASSWORD}
    
rabbitmq:
  host: prod-rabbitmq
  port: 5672
  password: ${RABBITMQ_PASSWORD}

knife4j:
  enable: false  # 生产环境建议关闭
```

### 4.3 环境变量配置

#### .env 文件
```properties
# 开发环境配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=tigshop
DB_USER=root
DB_PASSWORD=your_password

REDIS_HOST=localhost
REDIS_PORT=6379

RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672

SERVER_PORT=8080
```

使用方式：
```java
@Value("${DB_HOST}")
private String dbHost;

@Value("${SERVER_PORT:${default:8080}}")
private int serverPort;
```

---

## 5. 新增业务完整流程

以下以 **Brand（品牌管理）** 功能为例，演示如何从零开始开发一个新业务模块。

### Step 1: 创建实体类

#### 1.1 定位数据库表
查看数据库中的 `brand` 表结构：
```sql
DESC brand;
```

典型结构：
```sql
CREATE TABLE `brand` (
  `brand_id` bigint NOT NULL AUTO_INCREMENT COMMENT '品牌 ID',
  `brand_name` varchar(50) NOT NULL COMMENT '品牌名称',
  `brand_logo` varchar(255) DEFAULT NULL COMMENT '品牌 Logo',
  `brand_desc` text COMMENT '品牌描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态:0 禁用 1 启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`brand_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 1.2 创建 Java 实体类
```java
package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商品品牌表
 * @TableName brand
 */
@TableName(value ="brand")
@ApiModel(value = "商品品牌表")
@Data
public class Brand {

    @TableId(value = "brand_id", type = IdType.AUTO)
    @ApiModelProperty(value = "品牌 ID", required = true)
    private Long brandId;

    @ApiModelProperty(value = "品牌名称", required = true)
    @TableField("brand_name")
    private String brandName;

    @ApiModelProperty(value = "品牌 Logo URL")
    @TableField("brand_logo")
    private String brandLogo;

    @ApiModelProperty(value = "品牌描述")
    @TableField("brand_desc")
    private String brandDesc;

    @ApiModelProperty(value = "排序权重")
    @TableField("sort_order")
    private Integer sortOrder;

    @ApiModelProperty(value = "状态：0 禁用，1 启用")
    @TableField("status")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    @TableField("updated_at")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

**注解说明**:
- `@TableName`: 指定对应的数据库表名
- `@TableId`: 指定主键字段及自增策略
- `@TableField`: 指定数据库字段映射
- `@ApiModelProperty`: Swagger 文档说明

#### 1.3 使用 MyBatisX 逆向工程（可选）
如果使用 IDEA，可以使用 MyBatisX 插件自动生成实体类：
1. 安装 MyBatisX 插件
2. 右键数据库表 → Generate MyBatis Element
3. 选择生成实体类
4. 自动完成上述步骤

### Step 2: 创建 Mapper 接口

#### 2.1 定位 Mapper 目录
路径：`tigshop-service/src/main/java/com/tigshop/service/mapper/product/`

#### 2.2 创建 Mapper 接口
```java
package com.tigshop.service.mapper.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tigshop.bean.model.product.Brand;
import org.apache.ibatis.annotations.Mapper;

/**
 * 品牌数据访问层
 */
@Mapper
public interface BrandMapper extends BaseMapper<Brand> {
    // 可以在此定义自定义的 SQL 方法
}
```

**关键点**:
- 继承 `BaseMapper<T>` 获得基础的 CRUD 方法
- `@Mapper` 注解让 Spring 扫描到这个 Mapper

### Step 3: 创建 Service 接口

#### 3.1 定位 Service 目录
路径：`tigshop-service/src/main/java/com/tigshop/service/service/product/`

#### 3.2 创建 Service 接口
```java
package com.tigshop.service.service.product;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.model.product.Brand;
import com.tigshop.service.dto.BrandDTO;
import com.tigshop.service.vo.BrandVO;

import java.util.List;

/**
 * 品牌服务接口
 */
public interface BrandService extends IService<Brand> {
    
    /**
     * 获取品牌列表
     */
    List<BrandVO> list(BrandQuery query);
    
    /**
     * 获取品牌详情
     */
    BrandVO getById(Long brandId);
    
    /**
     * 新增品牌
     */
    boolean save(BrandDTO dto);
    
    /**
     * 更新品牌
     */
    boolean update(BrandDTO dto);
    
    /**
     * 删除品牌
     */
    boolean removeByIds(List<Long> brandIds);
}
```

### Step 4: 实现 Service 接口

#### 4.1 创建实现类
路径：`tigshop-service/src/main/java/com/tigshop/service/service/product/impl/`

```java
package com.tigshop.service.service.product.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.service.service.product.BrandService;
import com.tigshop.bean.model.product.Brand;
import com.tigshop.service.mapper.product.BrandMapper;
import com.tigshop.service.dto.BrandDTO;
import com.tigshop.service.vo.BrandVO;
import com.tigshop.service.dto.BrandQuery;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 品牌服务实现类
 */
@Service
@RequiredArgsConstructor
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> 
        implements BrandService {

    private final BrandMapper brandMapper;

    @Override
    public PageInfo<BrandVO> list(BrandQuery query) {
        // 分页
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        
        // 查询条件构建
        return PageHelper.startPage(query.getPageNum(), query.getPageSize())
                .doSelectPageInfo(() -> {
                    List<BrandVO> brands = baseMapper.selectList(
                        new QueryWrapper<Brand>()
                            .eq(query.getBrandId() != null, "brand_id", query.getBrandId())
                            .like(query.getKeyword() != null, "brand_name", query.getBrandName())
                            .eq(query.getStatus() != null, "status", query.getStatus())
                            .orderByAsc("sort_order")
                    ).stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
                    
                    return brands;
                });
    }

    @Override
    public BrandVO getById(Long brandId) {
        Brand brand = baseMapper.selectById(brandId);
        return convertToVO(brand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(BrandDTO dto) {
        Brand brand = convertToEntity(dto);
        brand.setCreatedAt(java.time.LocalDateTime.now());
        return baseMapper.insert(brand) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(BrandDTO dto) {
        Brand brand = convertToEntity(dto);
        brand.setUpdatedAt(java.time.LocalDateTime.now());
        return baseMapper.updateById(brand) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(List<Long> brandIds) {
        return baseMapper.deleteBatchIds(brandIds) > 0;
    }

    /**
     * 转换 VO
     */
    private BrandVO convertToVO(Brand brand) {
        if (brand == null) return null;
        BrandVO vo = new BrandVO();
        org.springframework.beans.BeanUtils.copyProperties(brand, vo);
        return vo;
    }

    /**
     * 转换实体
     */
    private Brand convertToEntity(BrandDTO dto) {
        Brand brand = new Brand();
        org.springframework.beans.BeanUtils.copyProperties(dto, brand);
        return brand;
    }
}
```

**关键点**:
- 继承 `ServiceImpl<BaseMapper<T>, T>`
- `@Service` 注解让 Spring 扫描
- `@Transactional` 保证事务一致性
- 使用 `BeanUtils` 进行对象转换

### Step 5: 创建 DTO 和 VO 类

#### 5.1 创建 DTO（数据传输对象）
```java
package com.tigshop.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "品牌 DTO")
public class BrandDTO {

    @ApiModelProperty(value = "品牌 ID（更新时使用）")
    private Long brandId;

    @ApiModelProperty(value = "品牌名称", required = true)
    @NotBlank(message = "品牌名称不能为空")
    private String brandName;

    @ApiModelProperty(value = "品牌 Logo URL")
    private String brandLogo;

    @ApiModelProperty(value = "品牌描述")
    private String brandDesc;

    @ApiModelProperty(value = "排序")
    private Integer sortOrder;

    @ApiModelProperty(value = "状态：0 禁用，1 启用")
    private Integer status;
}
```

#### 5.2 创建 VO（视图对象）
```java
package com.tigshop.service.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@ApiModel(value = "品牌 VO")
public class BrandVO {

    @ApiModelProperty(value = "品牌 ID")
    private Long brandId;

    @ApiModelProperty(value = "品牌名称")
    private String brandName;

    @ApiModelProperty(value = "品牌 Logo URL")
    private String brandLogo;

    @ApiModelProperty(value = "品牌描述")
    private String brandDesc;

    @ApiModelProperty(value = "排序")
    private Integer sortOrder;

    @ApiModelProperty(value = "状态：0 禁用，1 启用")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updatedAt;
}
```

#### 5.3 创建 Query 查询对象
```java
package com.tigshop.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "品牌查询参数")
public class BrandQuery {

    @ApiModelProperty(value = "品牌 ID")
    private Long brandId;

    @ApiModelProperty(value = "品牌名称关键词")
    private String brandName;

    @ApiModelProperty(value = "状态筛选")
    private Integer status;

    @ApiModelProperty(value = "页码，默认 1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页数量，默认 10")
    private Integer pageSize = 10;
}
```

### Step 6: 创建 Controller

#### 6.1 定位 Controller 目录
路径：`tigshop-adminapi/src/main/java/com/tigshop/admin/controller/product/`

#### 6.2 创建 Controller
```java
package com.tigshop.admin.controller.product;

import com.tigshop.common.util.AjaxResult;
import com.tigshop.service.dto.BrandDTO;
import com.tigshop.service.dto.BrandQuery;
import com.tigshop.service.vo.BrandVO;
import com.tigshop.service.service.product.BrandService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 品牌管理控制器
 */
@RestController
@RequestMapping("/product/brand")
@Tag(name = "品牌管理", description = "商品信息中的品牌管理")
@ApiTags({"品牌管理"})
@CrossOrigin
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 品牌列表
     */
    @GetMapping("/list")
    @Operation(summary = "品牌列表查询")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 401, message = "未登录")
    })
    public AjaxResult list(BrandQuery query) {
        PageInfo<BrandVO> pageInfo = brandService.list(query);
        return success(pageInfo);
    }

    /**
     * 品牌详情
     */
    @GetMapping("/{brandId}")
    @Operation(summary = "品牌详情查询")
    @ApiParam(name = "brandId", value = "品牌 ID", required = true)
    public AjaxResult getInfo(@PathVariable Long brandId) {
        BrandVO brand = brandService.getById(brandId);
        if (brand == null) {
            return error("品牌不存在");
        }
        return success(brand);
    }

    /**
     * 新增品牌
     */
    @PostMapping
    @Operation(summary = "新增品牌")
    public AjaxResult add(@Validated @RequestBody BrandDTO dto) {
        boolean result = brandService.save(dto);
        return result ? success("新增成功") : error("新增失败");
    }

    /**
     * 更新品牌
     */
    @PutMapping
    @Operation(summary = "更新品牌")
    public AjaxResult edit(@Validated @RequestBody BrandDTO dto) {
        boolean result = brandService.update(dto);
        return result ? success("更新成功") : error("更新失败");
    }

    /**
     * 批量删除品牌
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "批量删除品牌")
    @ApiParam(name = "ids", value = "品牌 ID 数组", required = true)
    public AjaxResult remove(@PathVariable Long[] ids) {
        boolean result = brandService.removeByIds(List.of(ids));
        return result ? success("删除成功") : error("删除失败");
    }
}
```

**关键要点**:
- `@RestController`: 标记为控制器，返回 JSON
- `@RequestMapping`: 定义路由前缀
- `@Tag`: Swagger 分组标签
- `@Validated`: 参数校验
- `@PathVariable`: 路径变量绑定
- `@RequestBody`: JSON 请求体绑定

### Step 7: 注册路由

Java 应用中不需要像 PHP 那样单独注册路由，Spring Boot 会自动扫描所有带 `@RequestMapping` 的 Controller。

确保 Controller 在启动类扫描范围内即可。

### Step 8: 测试接口

#### 方式 1: 使用 Knife4j/Swagger UI
1. 启动服务：`mvn spring-boot:run`
2. 访问：`http://localhost:8080/adminapi/doc.html`
3. 在 UI 中测试各个接口

#### 方式 2: 使用 Postman
```bash
GET http://localhost:8080/adminapi/product/brand/list?pageNum=1&pageSize=10
Content-Type: application/json

GET http://localhost:8080/adminapi/product/brand/1
Authorization: Bearer <token>
```

#### 方式 3: 单元测试
```java
@SpringBootTest
class BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testListBrands() throws Exception {
        mockMvc.perform(get("/adminapi/product/brand/list"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.code").value(200));
    }
}
```

---

## 6. 接口文档使用

### 6.1 Knife4j 在线文档

TigShop 集成了 Knife4j 作为 API 文档生成工具。

#### 访问地址
- 开发环境：`http://localhost:8080/adminapi/doc.html`
- 生产环境：根据实际部署地址访问

#### 主要功能
- 接口列表展示
- 参数说明
- 在线调试
- 响应示例
- 导入导出

### 6.2 编写 API 文档注释

#### Controller 级别
```java
/**
 * 品牌管理接口集合
 */
@Tag(name = "品牌管理", description = "商品信息中的品牌 CRUD 操作")
@RestController
```

#### 方法级别
```java
/**
 * 获取品牌列表
 * @param query 查询参数
 * @return 分页品牌列表
 */
@GetMapping("/list")
@Operation(summary = "品牌列表查询", description = "支持分页、筛选等多条件查询")
@ApiResponses({
    @ApiResponse(code = 200, message = "成功", response = PageInfo.class),
    @ApiResponse(code = 401, message = "未授权")
})
public AjaxResult list(BrandQuery query) {
    // ...
}
```

#### 参数级别
```java
/**
 * 品牌 ID
 */
@ApiParam(name = "brandId", value = "品牌 ID", required = true, example = "1")
@PathVariable("brandId") Long brandId
```

---

## 7. 常见问题与优化

### 7.1 启动报错

#### 问题 1: 端口被占用
```
Error: Could not open listen socket. BindException: Address already in use: bind
```

**解决方案**:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /F /PID <PID>

# Linux
lsof -i:8080
kill -9 <PID>
```

#### 问题 2: 数据库连接失败
```
Communications link failure
```

**解决方案**:
1. 检查 MySQL 服务是否启动
2. 验证数据库账号密码
3. 确认数据库存在
4. 检查网络连接

### 7.2 性能优化

#### 1. 慢查询优化
```sql
-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;  -- 超过 2 秒的查询

-- 使用 EXPLAIN 分析
EXPLAIN SELECT * FROM product WHERE category_id = 1;
```

#### 2. Redis 缓存优化
```java
@Cacheable(value = "brands", key = "#query")
public List<BrandVO> getCachedBrands(BrandQuery query) {
    return brandService.list(query);
}

@CacheEvict(value = "brands", allEntries = true)
public void evictBrandCache() {
    // 缓存失效
}
```

#### 3. 数据库连接池优化
```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5          # 最小空闲连接
      maximum-pool-size: 20    # 最大连接数
      connection-timeout: 30000 # 连接超时时间
      idle-timeout: 600000      # 空闲超时
      max-lifetime: 1800000     # 生命周期
```

### 7.3 调试技巧

#### 1. 断点调试
在 IDE 中添加断点：
- IntelliJ IDEA: Debug 模式启动
- VS Code: 使用 Debugger 插件

#### 2. 日志分析
```yaml
logging:
  level:
    com.tigshop.service: debug
    com.tigshop.mapper: trace
```

#### 3. 接口测试工具
- Knife4j 在线调试
- Postman
- Thunder Client

---

*本文档由 TigShop 开发团队维护*  
*最后更新：2025-04-15*  
*文档版本：Part 3 - JAVA Backend Development Guide*
