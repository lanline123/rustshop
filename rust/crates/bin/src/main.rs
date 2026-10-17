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
use tigshop_services::catalog::comment::CommentService;
use tigshop_services::catalog::e_card::ECardService;
use tigshop_services::catalog::e_card_group::ECardGroupService;
use tigshop_services::catalog::exchange::ExchangeService;
use tigshop_services::catalog::price_inquiry::PriceInquiryService;
use tigshop_services::catalog::product_batch::ProductBatchService;
use tigshop_services::catalog::product_group::ProductGroupService;
use tigshop_services::catalog::product_inventory_log::ProductInventoryLogService;
use tigshop_services::catalog::product_search::ProductSearchService;
use tigshop_services::catalog::search_admin::SearchAdminService;
use tigshop_services::catalog::services::ServicesService;
use tigshop_services::identity::admin_account::AdminAccountService;
use tigshop_services::identity::admin_role::AdminRoleService;
use tigshop_services::identity::auth::AuthService;
use tigshop_services::logistics::print::PrintService;
use tigshop_services::logistics::print_log::PrintLogService;
use tigshop_services::system::config_basic::ConfigBasicService;
use tigshop_services::system::csrf::CsrfService;
use tigshop_services::system::log::LogService;
use tigshop_services::system::tips_manage::TipsManageService;
use tigshop_services::trade::cart::CartService;
use tigshop_services::catalog::category_all::CatalogService;
use tigshop_services::content::article::ContentService;
use tigshop_services::marketing::coupon::CouponService;
use tigshop_services::logistics::shipping_list::LogisticsService;
use tigshop_services::member::member_lists::MemberService;
use tigshop_services::marketing::seckill::SeckillService;
use tigshop_services::catalog::product_manage::ProductManageService;
use tigshop_services::analytics::statistics::StatisticsService;
use tigshop_services::merchant::merchant_client::MerchantClientService;
use tigshop_services::member::user_rank::UserRankService;
use tigshop_services::member::feedback::FeedbackService;
use tigshop_services::merchant::vendor_deposit_shop::VendorDepositShopService;
use tigshop_services::member::member_tag_group::MemberTagGroupService;
use tigshop_services::logistics::shipping_type::ShippingTypeService;
use tigshop_services::system::config_client::ConfigClientService;
use tigshop_services::payment::tax_rate::TaxRateService;
use tigshop_services::system::example::ExampleService;
use tigshop_services::openapi::workbuddy::WorkbuddyService;
use tigshop_services::merchant::shop_product_category::ShopProductCategoryService;
use tigshop_services::marketing::product_style_admin::ProductStyleAdminService;
use tigshop_services::member::member_tag::MemberTagService;
use tigshop_services::member::user_company::UserCompanyService;
use tigshop_services::content::mobile_cat_nav::MobileCatNavService;
use tigshop_services::logistics::logistics_company::LogisticsCompanyService;
use tigshop_services::logistics::shipping_tpl::ShippingTplService;
use tigshop_services::merchant::order_config::OrderConfigService;
use tigshop_services::system::region::RegionService;
use tigshop_services::catalog::attributes_tpl::AttributesTplService;
use tigshop_services::content::share::ShareService;
use tigshop_services::marketing::salesman_material_category::SalesmanMaterialCategoryService;
use tigshop_services::logistics::logistics_company_config::LogisticsCompanyConfigService;
use tigshop_services::system::message_type::MessageTypeService;
use tigshop_services::system::region_client::RegionClientService;
use tigshop_services::system::area_code::AreaCodeService;
use tigshop_services::system::mail_templates::MailTemplatesService;
use tigshop_services::logistics::print_config::PrintConfigService;
use tigshop_services::system::cache_manage::CacheManageService;
use tigshop_services::identity::admin_log::AdminLogService;
use tigshop_services::payment::user_balance_log::UserBalanceLogService;
use tigshop_services::im::web_socket::WebSocketService;
use tigshop_services::content::wechat_live_sync_callback::WechatLiveSyncCallbackService;
use tigshop_services::merchant::shop_client_category::ShopClientCategoryService;
use tigshop_services::logistics::sunmi_print_callback::SunmiPrintCallbackService;
use tigshop_services::member::user_points_log::UserPointsLogService;
use tigshop_services::trade::proxy_shopping_order_check::ProxyShoppingOrderCheckService;
use tigshop_services::trade::proxy_shopping_pickup_contact::ProxyShoppingPickupContactService;
use tigshop_services::trade::proxy_shopping_user_address::ProxyShoppingUserAddressService;
use tigshop_services::trade::proxy_shopping_product::ProxyShoppingProductService;
use tigshop_services::trade::proxy_shopping_user_coupon::ProxyShoppingUserCouponService;
use tigshop_services::trade::proxy_shopping_user::ProxyShoppingUserService;
use tigshop_services::payment::paylog_batch::PaylogBatchService;
use tigshop_services::catalog::short_url::ShortUrlService;
use tigshop_services::im::im_message::ImMessageService;
use tigshop_services::system::files::FilesService;
use tigshop_services::im::im_servant::ImServantService;
use tigshop_services::marketing::buyer_show_comment::BuyerShowCommentService;
use tigshop_services::marketing::buyer_show::BuyerShowService;
use tigshop_services::marketing::buyer_show_group::BuyerShowGroupService;
use tigshop_services::marketing::buyer_show_client::BuyerShowClientService;
use tigshop_services::content::seo::SeoService;
use tigshop_services::im::im_config::ImConfigService;
use tigshop_services::system::app_version_admin::AppVersionAdminService;
use tigshop_services::content::pc_navigation::PcNavigationService;
use tigshop_services::content::pc_cat_floor::PcCatFloorService;
use tigshop_services::system::pc::PcService;
use tigshop_services::wholesale::wholesale_client::WholesaleClientService;
use tigshop_services::trade::order_admin::OrderAdminService;
use tigshop_services::catalog::product_stock::ProductStockService;
use tigshop_services::catalog::category::CategoryService;
use tigshop_services::catalog::brand::BrandService;
use tigshop_services::catalog::stock_warning::StockWarningService;
use tigshop_services::member::user_center::UserCenterService;
use tigshop_services::trade::aftersales::AftersalesService;
use tigshop_services::trade::aftersales_client::AftersalesClientService;
use tigshop_services::identity::authority::AuthorityService;
use tigshop_services::identity::admin_user::AdminUserService;
use tigshop_services::member::user_manage::UserManageService;
use tigshop_services::merchant::shop_client::ShopClientService;
use tigshop_services::merchant::shop_manage::ShopManageService;
use tigshop_services::payment::balance_pay::BalancePayService;
use tigshop_services::payment::paylog::PaylogService;
use tigshop_services::payment::refund_apply::RefundApplyService;
use tigshop_services::marketing::time_discount::TimeDiscountService;
use tigshop_services::trade::order::OrderService;
use tigshop_services::catalog::product::ProductService;
use tigshop_services::marketing::salesman::SalesmanService;
use tigshop_services::merchant::shop::ShopService;
use tigshop_services::content::decorate::DecorateService;
use tigshop_services::member::user::UserService;

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
    let article = Arc::new(ContentService::new(repos.clone()));
    let category_all = Arc::new(CatalogService::new(repos.clone()));
    let shipping_list = Arc::new(LogisticsService::new(repos.clone()));
    let member_lists = Arc::new(MemberService::new(repos.clone()));
    let seckill = Arc::new(SeckillService::new(repos.clone()));
    let time_discount = Arc::new(TimeDiscountService::new(repos.clone()));
    let paylog = Arc::new(PaylogService::new(repos.clone()));
    let refund_apply = Arc::new(RefundApplyService::new(repos.clone()));
    let balance_pay = Arc::new(BalancePayService::new(repos.clone()));
    let product_manage = Arc::new(ProductManageService::new(repos.clone()));
    let statistics = Arc::new(StatisticsService::new(repos.clone()));
    let merchant_client = Arc::new(MerchantClientService::new(repos.clone()));
    let user_rank = Arc::new(UserRankService::new(repos.clone()));
    let feedback = Arc::new(FeedbackService::new(repos.clone()));
    let vendor_deposit_shop = Arc::new(VendorDepositShopService::new(repos.clone()));
    let member_tag_group = Arc::new(MemberTagGroupService::new(repos.clone()));
    let shipping_type = Arc::new(ShippingTypeService::new(repos.clone()));
    let config_client = Arc::new(ConfigClientService::new(repos.clone()));
    let tax_rate = Arc::new(TaxRateService::new(repos.clone()));
    let example = Arc::new(ExampleService::new(repos.clone()));
    let workbuddy = Arc::new(WorkbuddyService::new(repos.clone()));
    let shop_product_category = Arc::new(ShopProductCategoryService::new(repos.clone()));
    let product_style_admin = Arc::new(ProductStyleAdminService::new(repos.clone()));
    let member_tag = Arc::new(MemberTagService::new(repos.clone()));
    let user_company = Arc::new(UserCompanyService::new(repos.clone()));
    let mobile_cat_nav = Arc::new(MobileCatNavService::new(repos.clone()));
    let logistics_company = Arc::new(LogisticsCompanyService::new(repos.clone()));
    let shipping_tpl = Arc::new(ShippingTplService::new(repos.clone()));
    let order_config = Arc::new(OrderConfigService::new(repos.clone()));
    let region = Arc::new(RegionService::new(repos.clone()));
    let attributes_tpl = Arc::new(AttributesTplService::new(repos.clone()));
    // proxy_shopping_product 需要 product_manage 作为依赖，只在此处构造一次
    // （此前被注册工具重复插入 4 次，其中 3 次参数个数错误）。
    let proxy_shopping_product = Arc::new(ProxyShoppingProductService::new(repos.clone(), product_manage.clone()));
    let share = Arc::new(ShareService::new(repos.clone()));
    let salesman_material_category = Arc::new(SalesmanMaterialCategoryService::new(repos.clone()));
    let logistics_company_config = Arc::new(LogisticsCompanyConfigService::new(repos.clone()));
    let message_type = Arc::new(MessageTypeService::new(repos.clone()));
    let region_client = Arc::new(RegionClientService::new(repos.clone()));
    let area_code = Arc::new(AreaCodeService::new(repos.clone()));
        let mail_templates = Arc::new(MailTemplatesService::new(repos.clone()));
                let print_config = Arc::new(PrintConfigService::new(repos.clone()));
                            let cache_manage = Arc::new(CacheManageService::new(repos.clone()));
                                                                                                                                                        let admin_log = Arc::new(AdminLogService::new(repos.clone()));
    let user_balance_log = Arc::new(UserBalanceLogService::new(repos.clone()));
        let web_socket = Arc::new(WebSocketService::new(repos.clone()));
                let wechat_live_sync_callback = Arc::new(WechatLiveSyncCallbackService::new(repos.clone()));
        let shop_client_category = Arc::new(ShopClientCategoryService::new(repos.clone()));
        let sunmi_print_callback = Arc::new(SunmiPrintCallbackService::new(repos.clone()));
            let user_points_log = Arc::new(UserPointsLogService::new(repos.clone()));
    let proxy_shopping_order_check = Arc::new(ProxyShoppingOrderCheckService::new(repos.clone()));
    let proxy_shopping_pickup_contact = Arc::new(ProxyShoppingPickupContactService::new(repos.clone()));
    let proxy_shopping_user_address = Arc::new(ProxyShoppingUserAddressService::new(repos.clone()));
    let proxy_shopping_user_coupon = Arc::new(ProxyShoppingUserCouponService::new(repos.clone()));
    let proxy_shopping_user = Arc::new(ProxyShoppingUserService::new(repos.clone()));
            let paylog_batch = Arc::new(PaylogBatchService::new(repos.clone()));
    let short_url = Arc::new(ShortUrlService::new(repos.clone()));
    let im_message = Arc::new(ImMessageService::new(repos.clone()));
    let files = Arc::new(FilesService::new(repos.clone()));
        let im_servant = Arc::new(ImServantService::new(repos.clone()));
    let buyer_show_comment = Arc::new(BuyerShowCommentService::new(repos.clone()));
    let buyer_show = Arc::new(BuyerShowService::new(repos.clone()));
    let buyer_show_group = Arc::new(BuyerShowGroupService::new(repos.clone()));
    let buyer_show_client = Arc::new(BuyerShowClientService::new(repos.clone()));
    let seo = Arc::new(SeoService::new(repos.clone()));
    let im_config = Arc::new(ImConfigService::new(repos.clone()));
    let app_version_admin = Arc::new(AppVersionAdminService::new(repos.clone()));
    let pc_navigation = Arc::new(PcNavigationService::new(repos.clone()));
    let pc_cat_floor = Arc::new(PcCatFloorService::new(repos.clone()));
    let pc = Arc::new(PcService::new(repos.clone()));
    let wholesale_client = Arc::new(WholesaleClientService::new(repos.clone()));
    let order_admin = Arc::new(OrderAdminService::new(repos.clone()));
    let product_stock = Arc::new(ProductStockService::new(repos.clone()));
    let category = Arc::new(CategoryService::new(repos.clone()));
    let brand = Arc::new(BrandService::new(repos.clone()));
    let stock_warning = Arc::new(StockWarningService::new(repos.clone()));
    let user_center = Arc::new(UserCenterService::new(repos.clone()));
    let aftersales = Arc::new(AftersalesService::new(repos.clone()));
    let aftersales_client = Arc::new(AftersalesClientService::new(repos.clone()));
    let authority = Arc::new(AuthorityService::new(repos.clone()));
    let admin_user = Arc::new(AdminUserService::new(repos.clone()));
    let user_manage = Arc::new(UserManageService::new(repos.clone()));
    let shop_client = Arc::new(ShopClientService::new(repos.clone()));
    // O2O 开关由 `ShopManageService::new` 内部从环境变量 `TIG_IS_O2O` 读取
    // （对应原 Java `tigshopProperties.isO2o()`，配置服务未迁移）。
    let shop_manage = Arc::new(ShopManageService::new(repos.clone()));
    let shop = Arc::new(ShopService::new(repos.clone()));
    let decorate = Arc::new(DecorateService::new(repos.clone()));
    let salesman = Arc::new(SalesmanService::new(repos.clone()));

    let admin_role = Arc::new(AdminRoleService::new(repos.clone()));
    let admin_account = Arc::new(AdminAccountService::new(repos.clone()));
    // ---- identity 域 ----
    let identity = Arc::new(tigshop_services::identity::Services {
        admin_log: admin_log.clone(),
        admin_user: admin_user.clone(),
        auth: auth.clone(),
        authority: authority.clone(),
            admin_role: admin_role.clone(),
        admin_account: admin_account.clone(),
});
    // ---- merchant 域 ----
    let merchant = Arc::new(tigshop_services::merchant::Services {
        merchant_client: merchant_client.clone(),
        order_config: order_config.clone(),
        shop: shop.clone(),
        shop_client: shop_client.clone(),
        shop_client_category: shop_client_category.clone(),
        shop_manage: shop_manage.clone(),
        shop_product_category: shop_product_category.clone(),
        vendor_deposit_shop: vendor_deposit_shop.clone(),
    });
    // ---- wholesale 域 ----
    let wholesale = Arc::new(tigshop_services::wholesale::Services {
        wholesale_client: wholesale_client.clone(),
    });
    let comment = Arc::new(CommentService::new(repos.clone()));
    let price_inquiry = Arc::new(PriceInquiryService::new(repos.clone()));
    let product_group = Arc::new(ProductGroupService::new(repos.clone()));
    let e_card_group = Arc::new(ECardGroupService::new(repos.clone()));
    let e_card = Arc::new(ECardService::new(repos.clone()));
    let product_batch = Arc::new(ProductBatchService::new(repos.clone()));
    let search_admin = Arc::new(SearchAdminService::new(repos.clone()));
    let product_inventory_log = Arc::new(ProductInventoryLogService::new(repos.clone()));
    let product_search = Arc::new(ProductSearchService::new(repos.clone()));
    let services = Arc::new(ServicesService::new(repos.clone()));
    let exchange = Arc::new(ExchangeService::new(repos.clone()));
    // ---- catalog 域 ----
    let catalog = Arc::new(tigshop_services::catalog::Services {
        attributes_tpl: attributes_tpl.clone(),
        brand: brand.clone(),
        category: category.clone(),
        category_all: category_all.clone(),
        product: product.clone(),
        product_manage: product_manage.clone(),
        product_stock: product_stock.clone(),
        short_url: short_url.clone(),
        stock_warning: stock_warning.clone(),
            comment: comment.clone(),
        price_inquiry: price_inquiry.clone(),
        product_group: product_group.clone(),
        e_card_group: e_card_group.clone(),
        e_card: e_card.clone(),
        product_batch: product_batch.clone(),
        search_admin: search_admin.clone(),
        product_inventory_log: product_inventory_log.clone(),
        product_search: product_search.clone(),
        services: services.clone(),
        exchange: exchange.clone(),
});
    // ---- trade 域 ----
    let trade = Arc::new(tigshop_services::trade::Services {
        aftersales: aftersales.clone(),
        aftersales_client: aftersales_client.clone(),
        cart: cart.clone(),
        order: order.clone(),
        order_admin: order_admin.clone(),
        proxy_shopping_order_check: proxy_shopping_order_check.clone(),
        proxy_shopping_pickup_contact: proxy_shopping_pickup_contact.clone(),
        proxy_shopping_product: proxy_shopping_product.clone(),
        proxy_shopping_user: proxy_shopping_user.clone(),
        proxy_shopping_user_address: proxy_shopping_user_address.clone(),
        proxy_shopping_user_coupon: proxy_shopping_user_coupon.clone(),
    });
    // ---- payment 域 ----
    let payment = Arc::new(tigshop_services::payment::Services {
        balance_pay: balance_pay.clone(),
        paylog: paylog.clone(),
        paylog_batch: paylog_batch.clone(),
        refund_apply: refund_apply.clone(),
        tax_rate: tax_rate.clone(),
        user_balance_log: user_balance_log.clone(),
    });
    // ---- marketing 域 ----
    let marketing = Arc::new(tigshop_services::marketing::Services {
        buyer_show: buyer_show.clone(),
        buyer_show_client: buyer_show_client.clone(),
        buyer_show_comment: buyer_show_comment.clone(),
        buyer_show_group: buyer_show_group.clone(),
        coupon: coupon.clone(),
        product_style_admin: product_style_admin.clone(),
        salesman: salesman.clone(),
        salesman_material_category: salesman_material_category.clone(),
        seckill: seckill.clone(),
        time_discount: time_discount.clone(),
    });
    // ---- member 域 ----
    let member = Arc::new(tigshop_services::member::Services {
        feedback: feedback.clone(),
        member_lists: member_lists.clone(),
        member_tag: member_tag.clone(),
        member_tag_group: member_tag_group.clone(),
        user: user.clone(),
        user_center: user_center.clone(),
        user_company: user_company.clone(),
        user_manage: user_manage.clone(),
        user_points_log: user_points_log.clone(),
        user_rank: user_rank.clone(),
    });
    let print = Arc::new(PrintService::new(repos.clone()));
    let print_log = Arc::new(PrintLogService::new(repos.clone()));
    // ---- logistics 域 ----
    let logistics = Arc::new(tigshop_services::logistics::Services {
        logistics_company: logistics_company.clone(),
        logistics_company_config: logistics_company_config.clone(),
        print_config: print_config.clone(),
        shipping_list: shipping_list.clone(),
        shipping_tpl: shipping_tpl.clone(),
        shipping_type: shipping_type.clone(),
        sunmi_print_callback: sunmi_print_callback.clone(),
            print: print.clone(),
        print_log: print_log.clone(),
});
    // ---- content 域 ----
    let content = Arc::new(tigshop_services::content::Services {
        article: article.clone(),
        decorate: decorate.clone(),
        mobile_cat_nav: mobile_cat_nav.clone(),
        pc_cat_floor: pc_cat_floor.clone(),
        pc_navigation: pc_navigation.clone(),
        seo: seo.clone(),
        share: share.clone(),
        wechat_live_sync_callback: wechat_live_sync_callback.clone(),
    });
    let csrf = Arc::new(CsrfService::new(cache.clone()));
    let config_basic = Arc::new(ConfigBasicService::new(repos.clone()));
    let tips_manage = Arc::new(TipsManageService::new(repos.clone()));
    let log = Arc::new(LogService::new(repos.clone()));
    // ---- system 域 ----
    let system = Arc::new(tigshop_services::system::Services {
        app_version_admin: app_version_admin.clone(),
        area_code: area_code.clone(),
        cache_manage: cache_manage.clone(),
        config_client: config_client.clone(),
        example: example.clone(),
        files: files.clone(),
        mail_templates: mail_templates.clone(),
        message_type: message_type.clone(),
        pc: pc.clone(),
        region: region.clone(),
        region_client: region_client.clone(),
            csrf: csrf.clone(),
        config_basic: config_basic.clone(),
        tips_manage: tips_manage.clone(),
        log: log.clone(),
});
    // ---- im 域 ----
    let im = Arc::new(tigshop_services::im::Services {
        im_config: im_config.clone(),
        im_message: im_message.clone(),
        im_servant: im_servant.clone(),
        web_socket: web_socket.clone(),
    });
    // ---- analytics 域 ----
    let analytics = Arc::new(tigshop_services::analytics::Services {
        statistics: statistics.clone(),
    });
    // ---- openapi 域 ----
    let openapi = Arc::new(tigshop_services::openapi::Services {
        workbuddy: workbuddy.clone(),
    });

    let admin_state = AdminState {
        identity: identity.clone(),
        merchant: merchant.clone(),
        catalog: catalog.clone(),
        trade: trade.clone(),
        payment: payment.clone(),
        marketing: marketing.clone(),
        member: member.clone(),
        logistics: logistics.clone(),
        content: content.clone(),
        system: system.clone(),
        im: im.clone(),
        analytics: analytics.clone(),
        openapi: openapi.clone(),
        jwt: (*jwt).clone(),
    };
    let public_state = PublicState {
        merchant: merchant.clone(),
        wholesale: wholesale.clone(),
        catalog: catalog.clone(),
        trade: trade.clone(),
        payment: payment.clone(),
        marketing: marketing.clone(),
        member: member.clone(),
        logistics: logistics.clone(),
        content: content.clone(),
        system: system.clone(),
        im: im.clone(),
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
