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
use tigshop_services::slice001::Slice001Service;
use tigshop_services::slice172::Slice172Service;
use tigshop_services::slice166::Slice166Service;
use tigshop_services::slice144::Slice144Service;
use tigshop_services::slice139::Slice139Service;
use tigshop_services::slice091::Slice091Service;
use tigshop_services::slice075::Slice075Service;
use tigshop_services::slice217::Slice217Service;
use tigshop_services::slice205::Slice205Service;
use tigshop_services::slice192::Slice192Service;
use tigshop_services::slice186::Slice186Service;
use tigshop_services::slice170::Slice170Service;
use tigshop_services::slice120::Slice120Service;
use tigshop_services::slice093::Slice093Service;
use tigshop_services::slice082::Slice082Service;
use tigshop_services::slice056::Slice056Service;
use tigshop_services::slice216::Slice216Service;
use tigshop_services::slice214::Slice214Service;
use tigshop_services::slice213::Slice213Service;
use tigshop_services::slice208::Slice208Service;
use tigshop_services::slice206::Slice206Service;
use tigshop_services::slice203::Slice203Service;
use tigshop_services::slice200::Slice200Service;
use tigshop_services::slice199::Slice199Service;
use tigshop_services::slice197::Slice197Service;
use tigshop_services::slice193::Slice193Service;
use tigshop_services::slice191::Slice191Service;
use tigshop_services::slice190::Slice190Service;
use tigshop_services::slice189::Slice189Service;
use tigshop_services::slice181::Slice181Service;
use tigshop_services::slice176::Slice176Service;
use tigshop_services::slice174::Slice174Service;
use tigshop_services::slice171::Slice171Service;
use tigshop_services::slice168::Slice168Service;
use tigshop_services::slice160::Slice160Service;
use tigshop_services::slice148::Slice148Service;
use tigshop_services::slice136::Slice136Service;
use tigshop_services::slice135::Slice135Service;
use tigshop_services::slice133::Slice133Service;
use tigshop_services::slice130::Slice130Service;
use tigshop_services::slice124::Slice124Service;
use tigshop_services::slice116::Slice116Service;
use tigshop_services::slice114::Slice114Service;
use tigshop_services::slice113::Slice113Service;
use tigshop_services::slice102::Slice102Service;
use tigshop_services::slice098::Slice098Service;
use tigshop_services::slice084::Slice084Service;
use tigshop_services::slice073::Slice073Service;
use tigshop_services::slice066::Slice066Service;
use tigshop_services::slice065::Slice065Service;
use tigshop_services::slice064::Slice064Service;
use tigshop_services::slice063::Slice063Service;
use tigshop_services::slice059::Slice059Service;
use tigshop_services::slice051::Slice051Service;
use tigshop_services::slice047::Slice047Service;
use tigshop_services::slice036::Slice036Service;
use tigshop_services::slice034::Slice034Service;
use tigshop_services::slice164::Slice164Service;
use tigshop_services::slice147::Slice147Service;
use tigshop_services::slice143::Slice143Service;
use tigshop_services::slice141::Slice141Service;
use tigshop_services::slice132::Slice132Service;
use tigshop_services::slice118::Slice118Service;
use tigshop_services::slice117::Slice117Service;
use tigshop_services::slice115::Slice115Service;
use tigshop_services::slice111::Slice111Service;
use tigshop_services::slice106::Slice106Service;
use tigshop_services::slice094::Slice094Service;
use tigshop_services::slice090::Slice090Service;
use tigshop_services::slice053::Slice053Service;
use tigshop_services::slice100::Slice100Service;
use tigshop_services::slice042::Slice042Service;
use tigshop_services::slice032::Slice032Service;
use tigshop_services::slice031::Slice031Service;
use tigshop_services::slice030::Slice030Service;
use tigshop_services::slice029::Slice029Service;
use tigshop_services::slice028::Slice028Service;
use tigshop_services::slice027::Slice027Service;
use tigshop_services::slice188::Slice188Service;
use tigshop_services::slice187::Slice187Service;
use tigshop_services::slice026::Slice026Service;
use tigshop_services::slice156::Slice156Service;
use tigshop_services::slice140::Slice140Service;
use tigshop_services::slice134::Slice134Service;
use tigshop_services::slice097::Slice097Service;
use tigshop_services::slice095::Slice095Service;
use tigshop_services::slice025::Slice025Service;
use tigshop_services::slice024::Slice024Service;
use tigshop_services::slice023::Slice023Service;
use tigshop_services::slice022::Slice022Service;
use tigshop_services::slice021::Slice021Service;
use tigshop_services::slice020::Slice020Service;
use tigshop_services::slice019::Slice019Service;
use tigshop_services::slice018::Slice018Service;
use tigshop_services::slice017::Slice017Service;
use tigshop_services::slice016::Slice016Service;
use tigshop_services::slice015::Slice015Service;
use tigshop_services::slice002::Slice002Service;
use tigshop_services::slice003::Slice003Service;
use tigshop_services::slice004::Slice004Service;
use tigshop_services::slice005::Slice005Service;
use tigshop_services::slice006::Slice006Service;
use tigshop_services::slice007::Slice007Service;
use tigshop_services::slice008::Slice008Service;
use tigshop_services::slice009::Slice009Service;
use tigshop_services::slice010::Slice010Service;
use tigshop_services::slice011::Slice011Service;
use tigshop_services::slice012::Slice012Service;
use tigshop_services::slice013::Slice013Service;
use tigshop_services::slice014::Slice014Service;
use tigshop_services::balance_pay::BalancePayService;
use tigshop_services::paylog::PaylogService;
use tigshop_services::refund_apply::RefundApplyService;
use tigshop_services::time_discount::TimeDiscountService;
use tigshop_services::order::OrderService;
use tigshop_services::product::ProductService;
use tigshop_services::salesman::SalesmanService;
use tigshop_services::shop::ShopService;
use tigshop_services::decorate::DecorateService;
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
    let refund_apply = Arc::new(RefundApplyService::new(repos.clone()));
    let balance_pay = Arc::new(BalancePayService::new(repos.clone()));
    let slice001 = Arc::new(Slice001Service::new(repos.clone()));
    let slice172 = Arc::new(Slice172Service::new(repos.clone()));
    let slice166 = Arc::new(Slice166Service::new(repos.clone()));
    let slice144 = Arc::new(Slice144Service::new(repos.clone()));
    let slice139 = Arc::new(Slice139Service::new(repos.clone()));
    let slice091 = Arc::new(Slice091Service::new(repos.clone()));
    let slice075 = Arc::new(Slice075Service::new(repos.clone()));
    let slice029 = Arc::new(Slice029Service::new(repos.clone(), slice001.clone()));
    let slice217 = Arc::new(Slice217Service::new(repos.clone()));
    let slice205 = Arc::new(Slice205Service::new(repos.clone()));
    let slice192 = Arc::new(Slice192Service::new(repos.clone()));
    let slice186 = Arc::new(Slice186Service::new(repos.clone()));
    let slice170 = Arc::new(Slice170Service::new(repos.clone()));
    let slice120 = Arc::new(Slice120Service::new(repos.clone()));
    let slice093 = Arc::new(Slice093Service::new(repos.clone()));
    let slice082 = Arc::new(Slice082Service::new(repos.clone()));
    let slice056 = Arc::new(Slice056Service::new(repos.clone()));
    let slice216 = Arc::new(Slice216Service::new(repos.clone()));
    let slice214 = Arc::new(Slice214Service::new(repos.clone()));
    let slice213 = Arc::new(Slice213Service::new(repos.clone()));
    let slice208 = Arc::new(Slice208Service::new(repos.clone()));
    let slice206 = Arc::new(Slice206Service::new(repos.clone()));
    let slice203 = Arc::new(Slice203Service::new(repos.clone()));
    let slice200 = Arc::new(Slice200Service::new(repos.clone()));
    let slice199 = Arc::new(Slice199Service::new(repos.clone()));
    let slice197 = Arc::new(Slice197Service::new(repos.clone()));
    let slice193 = Arc::new(Slice193Service::new(repos.clone()));
    let slice191 = Arc::new(Slice191Service::new(repos.clone()));
    let slice190 = Arc::new(Slice190Service::new(repos.clone()));
    let slice189 = Arc::new(Slice189Service::new(repos.clone()));
    let slice181 = Arc::new(Slice181Service::new(repos.clone()));
    let slice176 = Arc::new(Slice176Service::new(repos.clone()));
    let slice174 = Arc::new(Slice174Service::new(repos.clone()));
    let slice171 = Arc::new(Slice171Service::new(repos.clone()));
    let slice168 = Arc::new(Slice168Service::new(repos.clone()));
    let slice160 = Arc::new(Slice160Service::new(repos.clone()));
    let slice148 = Arc::new(Slice148Service::new(repos.clone()));
    let slice136 = Arc::new(Slice136Service::new(repos.clone()));
    let slice135 = Arc::new(Slice135Service::new(repos.clone()));
    let slice133 = Arc::new(Slice133Service::new(repos.clone()));
    let slice130 = Arc::new(Slice130Service::new(repos.clone()));
    let slice124 = Arc::new(Slice124Service::new(repos.clone()));
    let slice116 = Arc::new(Slice116Service::new(repos.clone()));
    let slice114 = Arc::new(Slice114Service::new(repos.clone()));
    let slice113 = Arc::new(Slice113Service::new(repos.clone()));
    let slice102 = Arc::new(Slice102Service::new(repos.clone()));
    let slice098 = Arc::new(Slice098Service::new(repos.clone()));
    let slice084 = Arc::new(Slice084Service::new(repos.clone()));
    let slice073 = Arc::new(Slice073Service::new(repos.clone()));
    let slice066 = Arc::new(Slice066Service::new(repos.clone()));
    let slice065 = Arc::new(Slice065Service::new(repos.clone()));
    let slice064 = Arc::new(Slice064Service::new(repos.clone()));
    let slice063 = Arc::new(Slice063Service::new(repos.clone()));
    let slice059 = Arc::new(Slice059Service::new(repos.clone()));
    let slice051 = Arc::new(Slice051Service::new(repos.clone()));
    let slice047 = Arc::new(Slice047Service::new(repos.clone()));
    let slice036 = Arc::new(Slice036Service::new(repos.clone()));
    let slice034 = Arc::new(Slice034Service::new(repos.clone()));
    let slice029 = Arc::new(Slice029Service::new(repos.clone(), slice001.clone()));
    let slice164 = Arc::new(Slice164Service::new(repos.clone()));
    let slice147 = Arc::new(Slice147Service::new(repos.clone()));
    let slice143 = Arc::new(Slice143Service::new(repos.clone()));
    let slice141 = Arc::new(Slice141Service::new(repos.clone()));
    let slice132 = Arc::new(Slice132Service::new(repos.clone()));
    let slice118 = Arc::new(Slice118Service::new(repos.clone()));
    let slice117 = Arc::new(Slice117Service::new(repos.clone()));
    let slice115 = Arc::new(Slice115Service::new(repos.clone()));
    let slice111 = Arc::new(Slice111Service::new(repos.clone()));
    let slice106 = Arc::new(Slice106Service::new(repos.clone()));
    let slice094 = Arc::new(Slice094Service::new(repos.clone()));
    let slice090 = Arc::new(Slice090Service::new(repos.clone()));
    let slice053 = Arc::new(Slice053Service::new(repos.clone()));
    let slice100 = Arc::new(Slice100Service::new(repos.clone()));
    let slice042 = Arc::new(Slice042Service::new(repos.clone()));
    let slice032 = Arc::new(Slice032Service::new(repos.clone()));
    let slice031 = Arc::new(Slice031Service::new(repos.clone()));
    let slice030 = Arc::new(Slice030Service::new(repos.clone()));
    let slice029 = Arc::new(Slice029Service::new(repos.clone(), slice001.clone()));
    let slice028 = Arc::new(Slice028Service::new(repos.clone()));
    let slice027 = Arc::new(Slice027Service::new(repos.clone()));
    let slice188 = Arc::new(Slice188Service::new(repos.clone()));
    let slice187 = Arc::new(Slice187Service::new(repos.clone()));
    let slice026 = Arc::new(Slice026Service::new(repos.clone()));
    let slice156 = Arc::new(Slice156Service::new(repos.clone()));
    let slice140 = Arc::new(Slice140Service::new(repos.clone()));
    let slice134 = Arc::new(Slice134Service::new(repos.clone()));
    let slice097 = Arc::new(Slice097Service::new(repos.clone()));
    let slice095 = Arc::new(Slice095Service::new(repos.clone()));
    let slice025 = Arc::new(Slice025Service::new(repos.clone()));
    let slice024 = Arc::new(Slice024Service::new(repos.clone()));
    let slice023 = Arc::new(Slice023Service::new(repos.clone()));
    let slice022 = Arc::new(Slice022Service::new(repos.clone()));
    let slice021 = Arc::new(Slice021Service::new(repos.clone()));
    let slice020 = Arc::new(Slice020Service::new(repos.clone()));
    let slice019 = Arc::new(Slice019Service::new(repos.clone()));
    let slice018 = Arc::new(Slice018Service::new(repos.clone()));
    let slice017 = Arc::new(Slice017Service::new(repos.clone()));
    let slice016 = Arc::new(Slice016Service::new(repos.clone()));
    let slice015 = Arc::new(Slice015Service::new(repos.clone()));
    let slice002 = Arc::new(Slice002Service::new(repos.clone()));
    let slice003 = Arc::new(Slice003Service::new(repos.clone()));
    let slice004 = Arc::new(Slice004Service::new(repos.clone()));
    let slice005 = Arc::new(Slice005Service::new(repos.clone()));
    let slice006 = Arc::new(Slice006Service::new(repos.clone()));
    let slice007 = Arc::new(Slice007Service::new(repos.clone()));
    let slice008 = Arc::new(Slice008Service::new(repos.clone()));
    let slice009 = Arc::new(Slice009Service::new(repos.clone()));
    let slice010 = Arc::new(Slice010Service::new(repos.clone()));
    let slice011 = Arc::new(Slice011Service::new(repos.clone()));
    let slice012 = Arc::new(Slice012Service::new(repos.clone()));
    let slice013 = Arc::new(Slice013Service::new(repos.clone()));
    // O2O 开关由 `Slice014Service::new` 内部从环境变量 `TIG_IS_O2O` 读取
    // （对应原 Java `tigshopProperties.isO2o()`，配置服务未迁移）。
    let slice014 = Arc::new(Slice014Service::new(repos.clone()));
    let shop = Arc::new(ShopService::new(repos.clone()));
    let decorate = Arc::new(DecorateService::new(repos.clone()));
    let salesman = Arc::new(SalesmanService::new(repos.clone()));

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
        refund_apply,
        slice001: slice001.clone(),
        slice002: slice002.clone(),
        slice003: slice003.clone(),
        slice004: slice004.clone(),
        slice005: slice005.clone(),
        slice026: slice026.clone(),
        slice025: slice025.clone(),
        slice024: slice024.clone(),
        slice023: slice023.clone(),
        slice021: slice021.clone(),
        slice020: slice020.clone(),
        slice019: slice019.clone(),
        slice018: slice018.clone(),
        slice017: slice017.clone(),
        slice006: slice006.clone(),
        slice008: slice008.clone(),
        slice010: slice010.clone(),
        slice011: slice011.clone(),
        slice012: slice012.clone(),
        slice014: slice014.clone(),
        shop: shop.clone(),
        decorate: decorate.clone(),
        salesman: salesman.clone(),
        jwt: (*jwt).clone(),
            slice095: slice095.clone(),
        slice097: slice097.clone(),
        slice134: slice134.clone(),
        slice140: slice140.clone(),
        slice187: slice187.clone(),
        slice188: slice188.clone(),
        slice027: slice027.clone(),
        slice028: slice028.clone(),
        slice029: slice029.clone(),
        slice030: slice030.clone(),
        slice031: slice031.clone(),
        slice032: slice032.clone(),
        slice042: slice042.clone(),
        slice100: slice100.clone(),
        slice053: slice053.clone(),
        slice094: slice094.clone(),
        slice111: slice111.clone(),
        slice118: slice118.clone(),
        slice132: slice132.clone(),
        slice147: slice147.clone(),
        slice164: slice164.clone(),
        slice034: slice034.clone(),
        slice036: slice036.clone(),
        slice047: slice047.clone(),
        slice051: slice051.clone(),
        slice063: slice063.clone(),
        slice064: slice064.clone(),
        slice098: slice098.clone(),
        slice102: slice102.clone(),
        slice113: slice113.clone(),
        slice114: slice114.clone(),
        slice116: slice116.clone(),
        slice124: slice124.clone(),
        slice133: slice133.clone(),
        slice135: slice135.clone(),
        slice136: slice136.clone(),
        slice171: slice171.clone(),
        slice174: slice174.clone(),
        slice176: slice176.clone(),
        slice181: slice181.clone(),
        slice189: slice189.clone(),
        slice190: slice190.clone(),
        slice191: slice191.clone(),
        slice193: slice193.clone(),
        slice197: slice197.clone(),
        slice199: slice199.clone(),
        slice203: slice203.clone(),
        slice208: slice208.clone(),
        slice216: slice216.clone(),
        slice056: slice056.clone(),
        slice093: slice093.clone(),
        slice120: slice120.clone(),
        slice186: slice186.clone(),
        slice192: slice192.clone(),
        slice205: slice205.clone(),
        slice217: slice217.clone(),
        slice075: slice075.clone(),
        slice139: slice139.clone(),
        slice144: slice144.clone(),
        slice166: slice166.clone(),
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
        balance_pay,
        shop,
        decorate,
        salesman,
        slice001: slice001.clone(),
        slice004: slice004.clone(),
        slice007: slice007.clone(),
        slice009: slice009.clone(),
        slice013: slice013.clone(),
        slice015: slice015.clone(),
        slice016: slice016.clone(),
        slice021: slice021.clone(),
        slice022: slice022.clone(),
        jwt: (*jwt).clone(),
            slice156: slice156.clone(),
        slice097: slice097.clone(),
        slice140: slice140.clone(),
        slice090: slice090.clone(),
        slice106: slice106.clone(),
        slice115: slice115.clone(),
        slice117: slice117.clone(),
        slice141: slice141.clone(),
        slice143: slice143.clone(),
        slice059: slice059.clone(),
        slice065: slice065.clone(),
        slice066: slice066.clone(),
        slice073: slice073.clone(),
        slice084: slice084.clone(),
        slice130: slice130.clone(),
        slice135: slice135.clone(),
        slice148: slice148.clone(),
        slice160: slice160.clone(),
        slice168: slice168.clone(),
        slice171: slice171.clone(),
        slice200: slice200.clone(),
        slice206: slice206.clone(),
        slice213: slice213.clone(),
        slice214: slice214.clone(),
        slice082: slice082.clone(),
        slice170: slice170.clone(),
        slice091: slice091.clone(),
        slice172: slice172.clone(),
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
