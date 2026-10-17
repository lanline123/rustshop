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
use tigshop_services::identity::auth::AuthService;
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
    let identity_auth = Arc::new(AuthService::new(repos.clone(), jwt.clone(), cfg.jwt.clone()));
    let catalog_product = Arc::new(ProductService::new(repos.clone(), cache.clone()));
    let trade_cart = Arc::new(CartService::new(repos.clone(), (*catalog_product).clone()));
    let trade_order = Arc::new(OrderService::new(
        repos.clone(),
        (*catalog_product).clone(),
        (*trade_cart).clone(),
    ));
    let member_user = Arc::new(UserService::new(repos.clone()));
    let marketing_coupon = Arc::new(CouponService::new(repos.clone()));
    let content_article = Arc::new(ContentService::new(repos.clone()));
    let catalog_category_all = Arc::new(CatalogService::new(repos.clone()));
    let logistics_shipping_list = Arc::new(LogisticsService::new(repos.clone()));
    let member_member_lists = Arc::new(MemberService::new(repos.clone()));
    let marketing_seckill = Arc::new(SeckillService::new(repos.clone()));
    let marketing_time_discount = Arc::new(TimeDiscountService::new(repos.clone()));
    let payment_paylog = Arc::new(PaylogService::new(repos.clone()));
    let payment_refund_apply = Arc::new(RefundApplyService::new(repos.clone()));
    let payment_balance_pay = Arc::new(BalancePayService::new(repos.clone()));
    let catalog_product_manage = Arc::new(ProductManageService::new(repos.clone()));
    let analytics_statistics = Arc::new(StatisticsService::new(repos.clone()));
    let merchant_merchant_client = Arc::new(MerchantClientService::new(repos.clone()));
    let member_user_rank = Arc::new(UserRankService::new(repos.clone()));
    let member_feedback = Arc::new(FeedbackService::new(repos.clone()));
    let merchant_vendor_deposit_shop = Arc::new(VendorDepositShopService::new(repos.clone()));
    let member_member_tag_group = Arc::new(MemberTagGroupService::new(repos.clone()));
    let logistics_shipping_type = Arc::new(ShippingTypeService::new(repos.clone()));
    let system_config_client = Arc::new(ConfigClientService::new(repos.clone()));
    let payment_tax_rate = Arc::new(TaxRateService::new(repos.clone()));
    let system_example = Arc::new(ExampleService::new(repos.clone()));
    let openapi_workbuddy = Arc::new(WorkbuddyService::new(repos.clone()));
    let merchant_shop_product_category = Arc::new(ShopProductCategoryService::new(repos.clone()));
    let marketing_product_style_admin = Arc::new(ProductStyleAdminService::new(repos.clone()));
    let member_member_tag = Arc::new(MemberTagService::new(repos.clone()));
    let member_user_company = Arc::new(UserCompanyService::new(repos.clone()));
    let content_mobile_cat_nav = Arc::new(MobileCatNavService::new(repos.clone()));
    let logistics_logistics_company = Arc::new(LogisticsCompanyService::new(repos.clone()));
    let logistics_shipping_tpl = Arc::new(ShippingTplService::new(repos.clone()));
    let merchant_order_config = Arc::new(OrderConfigService::new(repos.clone()));
    let system_region = Arc::new(RegionService::new(repos.clone()));
    let catalog_attributes_tpl = Arc::new(AttributesTplService::new(repos.clone()));
    // proxy_shopping_product 需要 product_manage 作为依赖，只在此处构造一次
    // （此前被注册工具重复插入 4 次，其中 3 次参数个数错误）。
    let trade_proxy_shopping_product = Arc::new(ProxyShoppingProductService::new(repos.clone(), catalog_product_manage.clone()));
    let content_share = Arc::new(ShareService::new(repos.clone()));
    let marketing_salesman_material_category = Arc::new(SalesmanMaterialCategoryService::new(repos.clone()));
    let logistics_logistics_company_config = Arc::new(LogisticsCompanyConfigService::new(repos.clone()));
    let system_message_type = Arc::new(MessageTypeService::new(repos.clone()));
    let system_region_client = Arc::new(RegionClientService::new(repos.clone()));
    let system_area_code = Arc::new(AreaCodeService::new(repos.clone()));
        let system_mail_templates = Arc::new(MailTemplatesService::new(repos.clone()));
                let logistics_print_config = Arc::new(PrintConfigService::new(repos.clone()));
                            let system_cache_manage = Arc::new(CacheManageService::new(repos.clone()));
                                                                                                                                                        let identity_admin_log = Arc::new(AdminLogService::new(repos.clone()));
    let payment_user_balance_log = Arc::new(UserBalanceLogService::new(repos.clone()));
        let im_web_socket = Arc::new(WebSocketService::new(repos.clone()));
                let content_wechat_live_sync_callback = Arc::new(WechatLiveSyncCallbackService::new(repos.clone()));
        let merchant_shop_client_category = Arc::new(ShopClientCategoryService::new(repos.clone()));
        let logistics_sunmi_print_callback = Arc::new(SunmiPrintCallbackService::new(repos.clone()));
            let member_user_points_log = Arc::new(UserPointsLogService::new(repos.clone()));
    let trade_proxy_shopping_order_check = Arc::new(ProxyShoppingOrderCheckService::new(repos.clone()));
    let trade_proxy_shopping_pickup_contact = Arc::new(ProxyShoppingPickupContactService::new(repos.clone()));
    let trade_proxy_shopping_user_address = Arc::new(ProxyShoppingUserAddressService::new(repos.clone()));
    let trade_proxy_shopping_user_coupon = Arc::new(ProxyShoppingUserCouponService::new(repos.clone()));
    let trade_proxy_shopping_user = Arc::new(ProxyShoppingUserService::new(repos.clone()));
            let payment_paylog_batch = Arc::new(PaylogBatchService::new(repos.clone()));
    let catalog_short_url = Arc::new(ShortUrlService::new(repos.clone()));
    let im_im_message = Arc::new(ImMessageService::new(repos.clone()));
    let system_files = Arc::new(FilesService::new(repos.clone()));
        let im_im_servant = Arc::new(ImServantService::new(repos.clone()));
    let marketing_buyer_show_comment = Arc::new(BuyerShowCommentService::new(repos.clone()));
    let marketing_buyer_show = Arc::new(BuyerShowService::new(repos.clone()));
    let marketing_buyer_show_group = Arc::new(BuyerShowGroupService::new(repos.clone()));
    let marketing_buyer_show_client = Arc::new(BuyerShowClientService::new(repos.clone()));
    let content_seo = Arc::new(SeoService::new(repos.clone()));
    let im_im_config = Arc::new(ImConfigService::new(repos.clone()));
    let system_app_version_admin = Arc::new(AppVersionAdminService::new(repos.clone()));
    let content_pc_navigation = Arc::new(PcNavigationService::new(repos.clone()));
    let content_pc_cat_floor = Arc::new(PcCatFloorService::new(repos.clone()));
    let system_pc = Arc::new(PcService::new(repos.clone()));
    let wholesale_wholesale_client = Arc::new(WholesaleClientService::new(repos.clone()));
    let trade_order_admin = Arc::new(OrderAdminService::new(repos.clone()));
    let catalog_product_stock = Arc::new(ProductStockService::new(repos.clone()));
    let catalog_category = Arc::new(CategoryService::new(repos.clone()));
    let catalog_brand = Arc::new(BrandService::new(repos.clone()));
    let catalog_stock_warning = Arc::new(StockWarningService::new(repos.clone()));
    let member_user_center = Arc::new(UserCenterService::new(repos.clone()));
    let trade_aftersales = Arc::new(AftersalesService::new(repos.clone()));
    let trade_aftersales_client = Arc::new(AftersalesClientService::new(repos.clone()));
    let identity_authority = Arc::new(AuthorityService::new(repos.clone()));
    let identity_admin_user = Arc::new(AdminUserService::new(repos.clone()));
    let member_user_manage = Arc::new(UserManageService::new(repos.clone()));
    let merchant_shop_client = Arc::new(ShopClientService::new(repos.clone()));
    // O2O 开关由 `ShopManageService::new` 内部从环境变量 `TIG_IS_O2O` 读取
    // （对应原 Java `tigshopProperties.isO2o()`，配置服务未迁移）。
    let merchant_shop_manage = Arc::new(ShopManageService::new(repos.clone()));
    let merchant_shop = Arc::new(ShopService::new(repos.clone()));
    let content_decorate = Arc::new(DecorateService::new(repos.clone()));
    let marketing_salesman = Arc::new(SalesmanService::new(repos.clone()));

    let admin_state = AdminState {
        // —— identity ——
        identity_auth: identity_auth.clone(),
        identity_authority: identity_authority.clone(),
        identity_admin_user: identity_admin_user.clone(),
        identity_admin_log: identity_admin_log.clone(),
        // —— merchant ——
        merchant_shop_manage: merchant_shop_manage.clone(),
        merchant_shop: merchant_shop.clone(),
        merchant_order_config: merchant_order_config.clone(),
        merchant_shop_product_category: merchant_shop_product_category.clone(),
        merchant_vendor_deposit_shop: merchant_vendor_deposit_shop.clone(),
        // —— catalog ——
        catalog_product: catalog_product.clone(),
        catalog_category_all: catalog_category_all.clone(),
        catalog_product_manage: catalog_product_manage.clone(),
        catalog_product_stock: catalog_product_stock.clone(),
        catalog_category: catalog_category.clone(),
        catalog_brand: catalog_brand.clone(),
        catalog_stock_warning: catalog_stock_warning.clone(),
        catalog_attributes_tpl: catalog_attributes_tpl.clone(),
        // —— trade ——
        trade_order: trade_order.clone(),
        trade_order_admin: trade_order_admin.clone(),
        trade_aftersales: trade_aftersales.clone(),
        trade_proxy_shopping_user: trade_proxy_shopping_user.clone(),
        trade_proxy_shopping_user_coupon: trade_proxy_shopping_user_coupon.clone(),
        trade_proxy_shopping_product: trade_proxy_shopping_product.clone(),
        trade_proxy_shopping_user_address: trade_proxy_shopping_user_address.clone(),
        trade_proxy_shopping_pickup_contact: trade_proxy_shopping_pickup_contact.clone(),
        trade_proxy_shopping_order_check: trade_proxy_shopping_order_check.clone(),
        // —— payment ——
        payment_paylog: payment_paylog.clone(),
        payment_refund_apply: payment_refund_apply,
        payment_paylog_batch: payment_paylog_batch.clone(),
        payment_user_balance_log: payment_user_balance_log.clone(),
        payment_tax_rate: payment_tax_rate.clone(),
        // —— marketing ——
        marketing_coupon: marketing_coupon.clone(),
        marketing_seckill: marketing_seckill.clone(),
        marketing_time_discount: marketing_time_discount.clone(),
        marketing_buyer_show_comment: marketing_buyer_show_comment.clone(),
        marketing_buyer_show: marketing_buyer_show.clone(),
        marketing_buyer_show_group: marketing_buyer_show_group.clone(),
        marketing_salesman: marketing_salesman.clone(),
        marketing_salesman_material_category: marketing_salesman_material_category.clone(),
        marketing_product_style_admin: marketing_product_style_admin.clone(),
        // —— member ——
        member_member_lists: member_member_lists.clone(),
        member_user_manage: member_user_manage.clone(),
        member_user_points_log: member_user_points_log.clone(),
        member_user_company: member_user_company.clone(),
        member_member_tag: member_member_tag.clone(),
        member_member_tag_group: member_member_tag_group.clone(),
        member_feedback: member_feedback.clone(),
        member_user_rank: member_user_rank.clone(),
        // —— logistics ——
        logistics_shipping_list: logistics_shipping_list.clone(),
        logistics_print_config: logistics_print_config.clone(),
        logistics_logistics_company_config: logistics_logistics_company_config.clone(),
        logistics_shipping_tpl: logistics_shipping_tpl.clone(),
        logistics_logistics_company: logistics_logistics_company.clone(),
        logistics_shipping_type: logistics_shipping_type.clone(),
        // —— content ——
        content_article: content_article.clone(),
        content_seo: content_seo.clone(),
        content_pc_navigation: content_pc_navigation.clone(),
        content_pc_cat_floor: content_pc_cat_floor.clone(),
        content_decorate: content_decorate.clone(),
        content_mobile_cat_nav: content_mobile_cat_nav.clone(),
        // —— system ——
        system_app_version_admin: system_app_version_admin.clone(),
        system_files: system_files.clone(),
        system_cache_manage: system_cache_manage.clone(),
        system_mail_templates: system_mail_templates.clone(),
        system_area_code: system_area_code.clone(),
        system_message_type: system_message_type.clone(),
        system_region: system_region.clone(),
        system_example: system_example.clone(),
        // —— im ——
        im_im_config: im_im_config.clone(),
        im_im_servant: im_im_servant.clone(),
        im_im_message: im_im_message.clone(),
        // —— analytics ——
        analytics_statistics: analytics_statistics.clone(),
        // —— openapi ——
        openapi_workbuddy: openapi_workbuddy.clone(),
        // —— misc ——
        jwt: (*jwt).clone(),
    };
    let public_state = PublicState {
        // —— merchant ——
        merchant_shop: merchant_shop,
        merchant_shop_client: merchant_shop_client.clone(),
        merchant_shop_client_category: merchant_shop_client_category.clone(),
        merchant_merchant_client: merchant_merchant_client.clone(),
        // —— wholesale ——
        wholesale_wholesale_client: wholesale_wholesale_client.clone(),
        // —— catalog ——
        catalog_product: catalog_product.clone(),
        catalog_category_all: catalog_category_all,
        catalog_product_manage: catalog_product_manage.clone(),
        catalog_category: catalog_category.clone(),
        catalog_short_url: catalog_short_url.clone(),
        // —— trade ——
        trade_cart: trade_cart.clone(),
        trade_order: trade_order.clone(),
        trade_aftersales_client: trade_aftersales_client.clone(),
        // —— payment ——
        payment_paylog: payment_paylog,
        payment_balance_pay: payment_balance_pay,
        // —— marketing ——
        marketing_coupon: marketing_coupon,
        marketing_seckill: marketing_seckill,
        marketing_time_discount: marketing_time_discount,
        marketing_salesman: marketing_salesman,
        marketing_buyer_show_client: marketing_buyer_show_client.clone(),
        // —— member ——
        member_user: member_user,
        member_member_lists: member_member_lists,
        member_user_center: member_user_center.clone(),
        // —— logistics ——
        logistics_shipping_list: logistics_shipping_list,
        logistics_sunmi_print_callback: logistics_sunmi_print_callback.clone(),
        // —— content ——
        content_article: content_article,
        content_decorate: content_decorate,
        content_seo: content_seo.clone(),
        content_wechat_live_sync_callback: content_wechat_live_sync_callback.clone(),
        content_share: content_share.clone(),
        // —— system ——
        system_pc: system_pc.clone(),
        system_region_client: system_region_client.clone(),
        system_config_client: system_config_client.clone(),
        // —— im ——
        im_im_message: im_im_message.clone(),
        im_web_socket: im_web_socket.clone(),
        // —— misc ——
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