//! TigShop Rust 服务入口。
//!
//! 装配顺序（对应原 Spring Boot 启动流程）：
//! 1. 加载配置（config.yaml + TIG_* 环境变量）
//! 2. 初始化日志（Tracing，替代 logback.xml）
//! 3. 连接 MySQL（含 ONLY_FULL_GROUP_BY 兼容）+ 可选迁移
//! 4. 连接 Redis（失败降级为无缓存模式）
//! 5. 构建服务层与路由
//! 6. 同时启动管理端（8080）与商城端（8181）监听

use std::sync::Arc;

use std::time::Duration;

use tower_http::cors::{Any, CorsLayer};
use tower_http::compression::CompressionLayer;
use tower_http::trace::TraceLayer;

use tigshop_admin_api::{router as admin_router, AdminState};
use tigshop_api::{router as public_router, PublicState};
use tigshop_common::{config::AppConfig, log, utils};
use tigshop_infrastructure::cache::CacheService;
use tigshop_infrastructure::db::{self, Repositories};
use tigshop_security::JwtService;
use tigshop_services::auth::AuthService;
use tigshop_services::cart::CartService;
use tigshop_services::catalog::CatalogService;
use tigshop_services::content::ContentService;
use tigshop_services::coupon::CouponService;
use tigshop_services::logistics::LogisticsService;
use tigshop_services::member::MemberService;
use tigshop_services::seckill::SeckillService;
use tigshop_services::paylog::PaylogService;
use tigshop_services::time_discount::TimeDiscountService;
use tigshop_services::order::OrderService;
use tigshop_services::product::ProductService;
use tigshop_services::user::UserService;

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    // 1. 配置
    let cfg = AppConfig::load()?;

    // 2. 日志
    log::init("logs");
    tracing::info!("TigShop Rust 服务启动中...");

    // 3. 数据库（最多重试 10 次，兼容 docker-compose 中 MySQL 慢启动）
    let pool = {
        let mut attempt = 0;
        loop {
            match db::init_pool(&cfg).await {
                Ok(p) => break p,
                Err(e) if attempt < 10 => {
                    attempt += 1;
                    tracing::warn!("数据库连接失败（第 {attempt} 次）: {e}，2 秒后重试");
                    tokio::time::sleep(Duration::from_secs(2)).await;
                }
                Err(e) => return Err(e),
            }
        }
    };

    // 可选：执行 migrations/
    if cfg.datasource.auto_migrate {
        db::run_migrations(&pool, "migrations").await?;
    }

    // 4. Redis（失败降级，不阻断启动）
    let cache = match CacheService::init(&cfg.redis.url()).await {
        Ok(c) => Some(c),
        Err(e) => {
            tracing::warn!("Redis 不可用，降级为无缓存模式: {e}");
            None
        }
    };

    // 5. 装配服务
    let repos = Arc::new(Repositories::new(pool));
    let jwt = Arc::new(JwtService::new(cfg.jwt.clone()));
    let auth = Arc::new(AuthService::new(repos.clone(), jwt.clone(), cfg.jwt.clone()));
    let product = Arc::new(ProductService::new(repos.clone(), cache.clone()));
    let cart = Arc::new(CartService::new(repos.clone(), (*product).clone()));
    let order = Arc::new(OrderService::new(
        repos.clone(),
        (*product).clone(),
        (*cart).clone(),
    ));
    let user = Arc::new(UserService::new(repos.clone()));
    let coupon = Arc::new(CouponService::new(repos.clone()));
    let content = Arc::new(ContentService::new(repos.clone()));
    let catalog = Arc::new(CatalogService::new(repos.clone()));
    let logistics = Arc::new(LogisticsService::new(repos.clone()));
    let member = Arc::new(MemberService::new(repos.clone()));
    let seckill = Arc::new(SeckillService::new(repos.clone()));
    let time_discount = Arc::new(TimeDiscountService::new(repos.clone()));
    let paylog = Arc::new(PaylogService::new(repos.clone()));

    let admin_state = AdminState {
        auth: auth.clone(),
        product: product.clone(),
        order: order.clone(),
        coupon: coupon.clone(),
        content: content.clone(),
        catalog: catalog.clone(),
        logistics: logistics.clone(),
        member: member.clone(),
        seckill: seckill.clone(),
        time_discount: time_discount.clone(),
        paylog: paylog.clone(),
        jwt: (*jwt).clone(),
    };
    let public_state = PublicState {
        product: product.clone(),
        cart: cart.clone(),
        order: order.clone(),
        user,
        coupon,
        content,
        catalog,
        logistics,
        member,
        seckill,
        time_discount,
        paylog,
        jwt: (*jwt).clone(),
    };

    // 6. CORS（开发期允许全部来源，生产按域名收紧）
    let cors = CorsLayer::new()
        .allow_origin(Any)
        .allow_methods(Any)
        .allow_headers(Any);

    let admin_app = admin_router(admin_state)
        .layer(cors.clone())
        .layer(CompressionLayer::new())
        .layer(TraceLayer::new_for_http());

    let public_app = public_router(public_state)
        .layer(cors)
        .layer(CompressionLayer::new())
        .layer(TraceLayer::new_for_http());

    // 7. 双端口监听（对应原 adminapi:8080 / api:8181）
    let admin_addr = format!("{}:{}", cfg.server.host, cfg.server.admin_port);
    let api_addr = format!("{}:{}", cfg.server.host, cfg.server.api_port);

    let listener_admin = tokio::net::TcpListener::bind(&admin_addr).await?;
    let listener_api = tokio::net::TcpListener::bind(&api_addr).await?;

    tracing::info!("管理端 API 监听: http://{admin_addr}");
    tracing::info!("商城端 API 监听: http://{api_addr}");
    tracing::info!("启动完成，当前时间戳: {}", utils::now_millis());

    tokio::select! {
        r = axum::serve(listener_admin, admin_app.into_make_service()) => {
            r.map_err(|e| anyhow::anyhow!("管理端服务异常: {e}"))?;
        }
        r = axum::serve(listener_api, public_app.into_make_service()) => {
            r.map_err(|e| anyhow::anyhow!("商城端服务异常: {e}"))?;
        }
        _ = tokio::signal::ctrl_c() => {
            tracing::info!("收到退出信号，服务关闭中...");
        }
    }

    Ok(())
}
