# TigShop Java to Rust 重构计划

## 📋 项目概述

### 1. 目标
将现有的 Java Spring Boot B2B2C 电商系统完全迁移到 Rust，利用 Rust 的性能优势、内存安全和并发特性。

### 2. 技术选型对比

| 原 Java 技术 | Rust 替代方案 | 说明 |
|-------------|--------------|------|
| Spring Boot 3.3.4 | Actix-web / Axum | 高性能 Web 框架 |
| JDK 21 | Rust 1.70+ | 现代语言版本 |
| MyBatis Plus 3.5.7 | SQLx + PostgreSQL/MySQL | 异步数据库访问 |
| Redisson | redis-rs / ioredis | Redis 客户端 |
| RabbitMQ (Spring AMQP) | amqpd / rabbitmq-client | 消息队列 |
| Spring Security | axum-jwt / actix-identity | 认证授权 |
| OpenFeign | reqwest | HTTP 客户端 |
| Jackson | serde + serde_json | JSON 序列化 |
| Lombok | derive macros | 代码生成 |
| EasyExcel | calamine / xlsx | Excel 处理 |
| Aliyun OSS SDK | aws-sdk-s3 + object_store | 对象存储 |
| Elasticsearch client | elastic-types + elastic | Elasticsearch 客户端 |
| FFmpeg/Javacv | ffmpeg-kit-rs | 视频处理 |

## 🏗️ 项目结构

```
tigshop-rust/
├── Cargo.toml                 # 工作区配置
├── rust-toolchain             # Rust 版本管理
│
├── crates/
│   ├── core/                  # 核心模块
│   │   ├── models/            # 领域模型
│   │   ├── entities/          # 数据库实体
│   │   └── types/             # 类型定义
│   │
│   ├── services/              # 业务逻辑层
│   │   ├── auth/              # 认证服务
│   │   ├── product/           # 商品服务
│   │   ├── order/             # 订单服务
│   │   ├── cart/              # 购物车
│   │   ├── payment/           # 支付服务
│   │   ├── coupon/            # 优惠券
│   │   ├── user/              # 用户服务
│   │   └── vendor/            # 商户服务
│   │
│   ├── api/                   # API 接口层
│   │   ├── admin/             # 管理后台 API
│   │   ├── public/            # 公开 API
│   │   └── openapi/           # 开放 API
│   │
│   ├── common/                # 通用模块
│   │   ├── config/            # 配置管理
│   │   ├── utils/             # 工具函数
│   │   ├── middleware/        # 中间件
│   │   └── error/             # 错误处理
│   │
│   ├── infrastructure/        # 基础设施层
│   │   ├── db/                # 数据库连接
│   │   ├── cache/             # 缓存层
│   │   ├── queue/             # 消息队列
│   │   ├── storage/           # 文件存储
│   │   └── integration/       # 第三方集成
│   │
│   ├── security/              # 安全模块
│   │   ├── jwt/               # JWT 令牌
│   │   ├── oauth/             # OAuth2
│   │   └── captcha/           # 验证码
│   │
│   └── bin/                   # 可执行文件
│       └── main.rs            # 主程序入口
│
├── migrations/                # 数据库迁移
│   ├── 001_create_users.sql
│   ├── 002_create_products.sql
│   └── ...
│
├── tests/                     # 集成测试
│
└── docker/                    # Docker 配置
    ├── Dockerfile
    └── docker-compose.yml
```

## 📦 核心依赖

### Cargo.toml 主要依赖

```toml
[workspace]
members = [
    "crates/core",
    "crates/services/*",
    "crates/api/*",
    "crates/common",
    "crates/infrastructure",
    "crates/security",
]

[workspace.dependencies]
# Web 框架
actix-web = "4"
actix-rt = "2"
actix-cors = "0.7"

# 异步运行时
tokio = { version = "1", features = ["full"] }
tokio-util = "0.7"

# 数据库
sqlx = { version = "0.8", features = ["runtime-tokio-native-tls", "postgres", "mysql", "chrono", "uuid", "json"] }
diesel = { version = "2", features = ["postgres"] } # 备选

# 缓存
redis = { version = "0.25", features = ["tokio-comp", "connection-manager"] }

# 序列化
serde = { version = "1", features = ["derive"] }
serde_json = "1"
serde_yaml = "0.9"

# 验证
validator = { version = "0.19", features = ["derive"] }

# 错误处理
anyhow = "1.0"
thiserror = "1.0"
octocrab = "0.1"

# HTTP 客户端
reqwest = { version = "0.12", features = ["json", "rustls-tls"] }
http = "1.0"

# JWT & 认证
jsonwebtoken = "9"
argon2 = "0.5"
bcrypt = "0.16"

# UUID
uuid = { version = "1", features = ["serde", "v4"] }

# 时间处理
chrono = { version = "0.4", features = ["serde"] }
time = "0.3"

# 配置管理
config = "0.14"
dotenvy = "0.15"

# 日志
tracing = "0.1"
tracing-subscriber = { version = "0.3", features = ["env-filter"] }
tracing-appender = "0.2"

# 日志记录
log = "0.4"

# 工具库
once_cell = "1.20"
lazy_static = "1.5"
async-trait = "0.1"

# 密码学
ring = "0.17"
sha2 = "0.10"
hex = "0.4"

# URL 处理
url = "2.5"
regex = "1.10"

# 随机数
rand = "0.8"

# 文件系统
walkdir = "2.5"
tempfile = "3.13"

# 文件上传
multipart = "0.18"

# 短信/邮件
 lettre = { version = "0.11", features = ["smtp-transport", "tokio1-native-tls"] }

# 第三方 SDK
tiktoken-rs = "0.5" # OpenAI tokenizer

# 任务调度
cron = "0.13"
scheduler = "0.10"

# 指标监控
prometheus = "0.13"
metrics = "0.23"

# 搜索
elastic = { version = "8", features = ["rustls"] }

# 二维码
qrcode = "0.14"

# 图片处理
image = "0.25"
image-compression = "0.2.1"

# 视频处理
ffmpeg-next = "6.0"

# PDF 生成
pdfgen = "0.2"

# Excel 处理
calamine = "0.25"
xlsx = "0.21"

# 分词搜索
tantivy = "0.22"

# 地图相关
geos = "0.10"
geo = "0.28"

# IP 定位
ipnetwork = "0.20"

# 国际化
i18n-embed = "0.15"
i18n-embed-fl = "0.9"

# WebSocket
ws = "0.10"
tokio-tungstenite = "0.24"

# gRPC (如果需要微服务通信)
tonic = "0.12"
tokio-grpc = "0.3"
```

## 🔄 迁移策略

### 阶段一：基础架构搭建（Week 1-2）

#### 1.1 初始化工作区
- 创建 Rust workspace 结构
- 配置 Cargo.toml 和各个 crate
- 设置 CI/CD流水线（GitHub Actions/GitLab CI）
- 配置开发环境（rust-analyzer, clippy, fmt）

#### 1.2 核心模块设计
- 定义统一的错误类型 `TigShopError`
- 实现日志系统（Tracing + Structured Logging）
- 配置管理模块化（支持多环境）
- 统一的时间戳处理（UTC 存储，本地时区展示）

#### 1.3 数据库层
- PostgreSQL/MySQL 双数据库支持
- 使用 SQLx 进行类型安全的查询
- 实现 Repository 模式
- 数据库迁移工具（SQLX migrate）

### 阶段二：核心业务模块（Week 3-8）

#### 2.1 用户系统
- [ ] User 实体模型（对应 AdminUser.java）
- [ ] 登录认证（JWT Token）
- [ ] 权限管理（RBAC 模型）
- [ ] 管理员管理
- [ ] 会员系统
- [ ] 收货地址管理

#### 2.2 商品系统
- [ ] Product 实体与 SKU 模型
- [ ] 分类树结构管理
- [ ] 品牌管理
- [ ] 商品 CRUD 操作
- [ ] 商品搜索（Elasticsearch 集成）
- [ ] 库存管理

#### 2.3 订单系统
- [ ] Order 订单实体
- [ ] 订单状态机
- [ ] 订单创建流程
- [ ] 订单取消/关闭
- [ ] 发货处理
- [ ] 售后退款

#### 2.4 购物车
- [ ] Cart Item 模型
- [ ] 加入购物车
- [ ] 数量修改
- [ ] 价格计算
- [ ] 促销活动关联

### 阶段三：支付与营销（Week 9-14）

#### 3.1 支付系统
- [ ] 微信支付（API v2/v3）
- [ ] 支付宝（当面支付、 APP 支付）
- [ ] PayPal 集成
- [ ] Stripe 集成
- [ ] 蚂蚁跨境支付
- [ ] 拉卡拉支付
- [ ] 易宝支付 YOP
- [ ] 余额支付
- [ ] 分账功能

#### 3.2 营销活动
- [ ] 优惠券系统
- [ ] 限时折扣
- [ ] 秒杀活动
- [ ] 拼团功能
- [ ] 满减满赠
- [ ] 积分体系
- [ ] 礼品卡

### 阶段四：内容与服务（Week 15-20）

#### 4.1 装修系统
- [ ] PC 端页面装修
- [ ] H5 移动端装修
- [ ] 导航配置
- [ ] 分类抽屉
- [ ] 模板库

#### 4.2 内容管理
- [ ] 文章管理系统
- [ ] 视频管理
- [ ] 相册管理
- [ ] 微信直播对接
- [ ] 视频号对接

#### 4.3 分销系统
- [ ] 分销员管理
- [ ] 佣金结算
- [ ] 业绩统计
- [ ] 多级分销

#### 4.4 商户管理
- [ ] 入驻申请流程
- [ ] 店铺管理
- [ ] 资金管理
- [ ] 客户管理

### 阶段五：基础设施完善（Week 21-24）

#### 5.1 消息队列
- [ ] RabbitMQ 集成
- [ ] 异步任务处理
- [ ] 事件驱动架构

#### 5.2 缓存优化
- [ ] Redis 缓存策略
- [ ] 热点数据预热
- [ ] 缓存一致性

#### 5.3 文件存储
- [ ] 本地存储
- [ ] 阿里云 OSS
- [ ] 腾讯云 COS
- [ ] 七牛云 Kodo
- [ ] AWS S3

#### 5.4 搜索引擎
- [ ] Elasticsearch 集成
- [ ] 商品全文搜索
- [ ] 分词查询
- [ ] 搜索建议

### 阶段六：前端适配与部署（Week 25-26）

#### 6.1 API 文档
- [ ] OpenAPI/Swagger 文档自动生成
- [ ] API 网关配置
- [ ] 速率限制

#### 6.2 部署
- [ ] Docker 镜像构建
- [ ] Kubernetes 部署配置
- [ ] CI/CD流水线

#### 6.3 监控
- [ ] Prometheus 指标收集
- [ ] Grafana 可视化
- [ ] 链路追踪（Jaeger）
- [ ] 健康检查

## 🎯 关键技术决策

### 1. Web 框架选择

**Actix-web vs Axum:**

| 特性 | Actix-web | Axum |
|------|-----------|------|
| 性能 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| 生态 | 成熟 | 成长中 |
| Tokio 原生 | ❌ | ✅ |
| 学习曲线 | 中等 | 平缓 |

**推荐：Axum**（与 Tokio 生态更融合，易于异步编程）

### 2. 数据库 ORM

**SQLx vs Diesel:**

- **SQLx**: 编译时查询验证，支持异步，轻量级
- **Diesel**: 纯同步，编译时安全性高

**推荐：SQLx**（异步优先，符合现代 Rust 实践）

### 3. 错误处理策略

```rust
// 使用 anyhow + thiserror 组合
pub type Result<T> = std::result::Result<T, TigShopError>;

#[derive(thiserror::Error, Debug)]
pub enum TigShopError {
    #[error("用户不存在：{0}")]
    UserNotFound(String),
    
    #[error("库存不足：{product_id}")]
    InsufficientStock { product_id: String },
    
    #[error("支付失败：{reason}")]
    PaymentFailed { reason: String },
    
    #[error("内部错误：{0}")]
    Internal(#[from] anyhow::Error),
}
```

### 4. 异步编程模型

```rust
use tokio::task;

// 并行处理多个请求
let handles = futures::future::join_all(tasks.iter().map(|task| {
    tokio::spawn(async move {
        process_task(task).await
    })
}));

let results = futures::future::join_all(handles).await;
```

### 5. 缓存策略

```rust
use redis::Client;

pub struct CacheService {
    client: Client,
    pool: ConnectionManager,
}

impl CacheService {
    pub async fn get_or_set<F, T>(&self, key: &str, ttl: u64, fetcher: F) -> Result<T>
    where
        F: FnOnce() -> Fut,
        Fut: std::future::Future<Output = Result<T>>,
    {
        match self.get(key).await {
            Ok(Some(value)) => Ok(value),
            Ok(None) => {
                let data = fetcher().await?;
                self.set_with_expiry(key, &data, ttl).await?;
                Ok(data)
            }
            Err(e) => Err(e.into()),
        }
    }
}
```

## 📝 编码规范

### 1. 代码风格

- 遵循 Rust 官方风格指南
- 使用 `rustfmt` 格式化代码
- 使用 `clippy` 检查潜在问题

### 2. 命名约定

- 模块名：snake_case
- 类型名：PascalCase
- 常量：SCREAMING_SNAKE_CASE
- 私有字段：_前缀

### 3. 文档规范

- 所有公共 API 必须有 doc comment
- 包含示例用法
- 更新 CHANGELOG.md

### 4. 测试规范

- 单元测试覆盖率 > 80%
- 集成测试覆盖关键业务流程
- 性能测试基准

## 🚀 性能优化目标

| 指标 | Java 原版 | Rust 版目标 |
|------|----------|------------|
| QPS | ~10,000 | ~50,000 |
| P99 Latency | ~200ms | < 50ms |
| 内存占用 | ~512MB | < 128MB |
| CPU 利用率 | 60-80% | < 30% |
| 启动时间 | ~30s | < 2s |

## 📊 迁移进度跟踪

### 里程碑

- **M1**: 基础框架完成（第 2 周）
- **M2**: 用户 + 商品系统完成（第 8 周）
- **M3**: 订单 + 支付系统完成（第 14 周）
- **M4**: 营销 + 分销系统完成（第 20 周）
- **M5**: 全部功能完成（第 24 周）
- **M6**: 生产环境上线（第 26 周）

## ⚠️ 风险与挑战

### 1. 团队学习曲线

- Rust 所有权和生命周期概念
- 异步编程模型
- 新生态库熟悉

### 2. 第三方集成

- Java SDK 转换为 Rust 封装
- 协议兼容性测试

### 3. 数据迁移

- MySQL → PostgreSQL 可能的数据类型转换
- 历史数据完整性验证

### 4. 性能调优

- 初始版本可能未达最优
- 需要根据实际负载调整

## 🔧 开发工具链

```bash
# 安装 Rust
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh

# 添加工具链
rustup install stable
rustup update
rustup component add clippy rustfmt

# 开发工具
cargo install cargo-watch    # 自动重新编译
cargo install cargo-watch    # 性能分析
cargo install cargo-udeps    # 依赖检查
```

## 📖 参考资料

### Rust 资源
- [The Rust Programming Language](https://doc.rust-lang.org/book/)
- [Tokio Documentation](https://tokio.rs/)
- [Actix-web Guide](https://actix.rs/)
- [SQLx Manual](https://docs.rs/sqlx/)

### 架构参考
- [Microservices Patterns](https://microservices.io/patterns/)
- [Rust Web Development](https://rustweb.dev/)

### 电商系统
- [OpenEJB](https://github.com/apache/tomee)
- [Sprout Commerce](https://github.com/sproutcommerce)

---

**下一步行动：**
1. 确认技术栈细节
2. 组建 Rust 开发团队
3. 制定详细 Sprint 计划
4. 开始第一阶段实施

---

*最后更新时间：2025-06-18*
