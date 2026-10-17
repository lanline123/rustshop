//! 数据库连接池与仓储入口。
//!
//! 对应原 Java HikariCP + MyBatis Plus：
//! - 连接池：SQLx `MySqlPool`（替代 HikariCP）
//! - 迁移：SQLx migrate（替代 Flyway/手工 SQL 脚本）
//! - 兼容原库 `connection-init-sql`：会话级去除 `ONLY_FULL_GROUP_BY`

use sqlx::mysql::{MySqlPool, MySqlPoolOptions};
use tigshop_common::AppConfig;

pub mod repo;


/// SQLx 连接池类型别名。
pub type DbPool = MySqlPool;

/// 初始化数据库连接池，并执行可选的迁移。
pub async fn init_pool(cfg: &AppConfig) -> anyhow::Result<DbPool> {
    let url = if cfg.datasource.url.is_empty() {
        // 回退到环境变量组合（TIG_DATASOURCE_URL 优先）
        let host = std::env::var("TIG_DATASOURCE_HOST").unwrap_or_else(|_| "127.0.0.1".into());
        let port = std::env::var("TIG_DATASOURCE_PORT").unwrap_or_else(|_| "3306".into());
        let db = std::env::var("TIG_DATASOURCE_DATABASE").unwrap_or_else(|_| "tigshop".into());
        let user = cfg.datasource.username.clone();
        let pass = cfg.datasource.password.clone();
        format!("mysql://{user}:{pass}@{host}:{port}/{db}")
    } else {
        cfg.datasource.url.clone()
    };

    let pool = MySqlPoolOptions::new()
        .max_connections(cfg.datasource.max_connections.max(1))
        .acquire_timeout(std::time::Duration::from_secs(10))
        .test_before_acquire(true)
        .connect(&url)
        .await
        .map_err(|e| anyhow::anyhow!("数据库连接失败: {e}"))?;

    // 等价于原 Hikari connection-init-sql：
    // SET SESSION sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))
    // 兼容历史 SQL（GROUP BY 未聚合字段）。
    sqlx::query(
        "SET SESSION sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))",
    )
    .execute(&pool)
    .await
    .map_err(|e| anyhow::anyhow!("设置 sql_mode 失败: {e}"))?;

    tracing::info!("数据库连接池初始化完成 (max_connections={})", cfg.datasource.max_connections);
    Ok(pool)
}

/// 执行 migrations/ 目录下的 SQL 迁移（按文件名排序，幂等记录于 _tig_migrations 表）。
pub async fn run_migrations(pool: &DbPool, dir: &str) -> anyhow::Result<()> {
    sqlx::query(
        "CREATE TABLE IF NOT EXISTS _tig_migrations (
            id VARCHAR(255) PRIMARY KEY,
            applied_at BIGINT NOT NULL
        )",
    )
    .execute(pool)
    .await?;

    let mut entries: Vec<_> = std::fs::read_dir(dir)
        .map_err(|e| anyhow::anyhow!("读取迁移目录失败 {dir}: {e}"))?
        .filter_map(|e| e.ok())
        .map(|e| e.path())
        .filter(|p| p.extension().map(|x| x == "sql").unwrap_or(false))
        .collect();
    entries.sort();

    for path in entries {
        let name = path
            .file_name()
            .map(|n| n.to_string_lossy().to_string())
            .unwrap_or_default();
        let already: Option<(String,)> =
            sqlx::query_as("SELECT id FROM _tig_migrations WHERE id = ?")
                .bind(&name)
                .fetch_optional(pool)
                .await?;
        if already.is_some() {
            continue;
        }
        let sql = std::fs::read_to_string(&path)?;
        // 去掉 SQL 行注释（-- 开头），避免被当作语句执行
        let cleaned: String = sql
            .lines()
            .filter(|l| !l.trim_start().starts_with("--"))
            .collect::<Vec<_>>()
            .join("\n");
        let mut tx = pool.begin().await?;
        // sqlx 默认不执行多语句，这里按分号逐条执行
        for stmt in cleaned.split(';').map(str::trim).filter(|s| !s.is_empty()) {
            sqlx::query(stmt).execute(&mut *tx).await.map_err(|e| {
                anyhow::anyhow!("迁移 {name} 执行失败 [{stmt}...]: {e}")
            })?;
        }
        sqlx::query("INSERT INTO _tig_migrations (id, applied_at) VALUES (?, ?)")
            .bind(&name)
            .bind(tigshop_common::utils::now_millis())
            .execute(&mut *tx)
            .await?;
        tx.commit().await?;
        tracing::info!("已应用迁移: {name}");
    }
    Ok(())
}

/// 全仓储聚合根（按限界上下文分组）。
#[derive(Clone)]
pub struct Repositories {
    pub identity_admin_log: repo::identity::admin_log::AdminLogRepository,
    pub identity_admin_user: repo::identity::admin_user::AdminUserRepository,
    pub identity_auth: repo::identity::auth::AdminUserRepository,
    pub identity_authority: repo::identity::authority::AuthorityRepository,
    pub merchant_merchant_client: repo::merchant::merchant_client::MerchantClientRepository,
    pub merchant_order_config: repo::merchant::order_config::OrderConfigRepository,
    pub merchant_shop: repo::merchant::shop::ShopRepository,
    pub merchant_shop_client: repo::merchant::shop_client::ShopClientRepository,
    pub merchant_shop_client_category: repo::merchant::shop_client_category::ShopClientCategoryRepository,
    pub merchant_shop_manage: repo::merchant::shop_manage::ShopManageRepository,
    pub merchant_shop_product_category: repo::merchant::shop_product_category::ShopProductCategoryRepository,
    pub merchant_vendor_deposit_shop: repo::merchant::vendor_deposit_shop::VendorDepositShopRepository,
    pub wholesale_wholesale_client: repo::wholesale::wholesale_client::WholesaleClientRepository,
    pub catalog_attributes_tpl: repo::catalog::attributes_tpl::AttributesTplRepository,
    pub catalog_brand: repo::catalog::brand::BrandRepository,
    pub catalog_category: repo::catalog::category::CategoryRepository,
    pub catalog_category_all: repo::catalog::category_all::CatalogRepository,
    pub catalog_product: repo::catalog::product::ProductRepository,
    pub catalog_product_manage: repo::catalog::product_manage::ProductManageRepository,
    pub catalog_product_stock: repo::catalog::product_stock::ProductStockRepository,
    pub catalog_short_url: repo::catalog::short_url::ShortUrlRepository,
    pub catalog_stock_warning: repo::catalog::stock_warning::StockWarningRepository,
    pub trade_aftersales: repo::trade::aftersales::AftersalesRepository,
    pub trade_aftersales_client: repo::trade::aftersales_client::AftersalesClientRepository,
    pub trade_cart: repo::trade::cart::CartRepository,
    pub trade_order: repo::trade::order::OrderRepository,
    pub trade_order_admin: repo::trade::order_admin::OrderAdminRepository,
    pub trade_proxy_shopping_order_check: repo::trade::proxy_shopping_order_check::ProxyShoppingOrderCheckRepository,
    pub trade_proxy_shopping_pickup_contact: repo::trade::proxy_shopping_pickup_contact::ProxyShoppingPickupContactRepository,
    pub trade_proxy_shopping_product: repo::trade::proxy_shopping_product::ProxyShoppingProductRepository,
    pub trade_proxy_shopping_user: repo::trade::proxy_shopping_user::ProxyShoppingUserRepository,
    pub trade_proxy_shopping_user_address: repo::trade::proxy_shopping_user_address::ProxyShoppingUserAddressRepository,
    pub trade_proxy_shopping_user_coupon: repo::trade::proxy_shopping_user_coupon::ProxyShoppingUserCouponRepository,
    pub payment_paylog: repo::payment::paylog::PaylogRepository,
    pub payment_paylog_batch: repo::payment::paylog_batch::PaylogBatchRepository,
    pub payment_refund_apply: repo::payment::refund_apply::RefundApplyRepository,
    pub payment_tax_rate: repo::payment::tax_rate::TaxRateRepository,
    pub payment_user_balance_log: repo::payment::user_balance_log::UserBalanceLogRepository,
    pub marketing_buyer_show: repo::marketing::buyer_show::BuyerShowRepository,
    pub marketing_buyer_show_client: repo::marketing::buyer_show_client::BuyerShowClientRepository,
    pub marketing_buyer_show_comment: repo::marketing::buyer_show_comment::BuyerShowCommentRepository,
    pub marketing_buyer_show_group: repo::marketing::buyer_show_group::BuyerShowGroupRepository,
    pub marketing_coupon: repo::marketing::coupon::CouponRepository,
    pub marketing_product_style_admin: repo::marketing::product_style_admin::ProductStyleAdminRepository,
    pub marketing_salesman: repo::marketing::salesman::SalesmanRepository,
    pub marketing_salesman_material_category: repo::marketing::salesman_material_category::SalesmanMaterialCategoryRepository,
    pub marketing_seckill: repo::marketing::seckill::SeckillRepository,
    pub marketing_time_discount: repo::marketing::time_discount::TimeDiscountRepository,
    pub member_feedback: repo::member::feedback::FeedbackRepository,
    pub member_member_lists: repo::member::member_lists::MemberRepository,
    pub member_member_tag: repo::member::member_tag::MemberTagRepository,
    pub member_member_tag_group: repo::member::member_tag_group::MemberTagGroupRepository,
    pub member_user: repo::member::user::UserRepository,
    pub member_user_center: repo::member::user_center::UserCenterRepository,
    pub member_user_company: repo::member::user_company::UserCompanyRepository,
    pub member_user_manage: repo::member::user_manage::UserManageRepository,
    pub member_user_points_log: repo::member::user_points_log::UserPointsLogRepository,
    pub member_user_rank: repo::member::user_rank::UserRankRepository,
    pub logistics_logistics_company: repo::logistics::logistics_company::LogisticsCompanyRepository,
    pub logistics_logistics_company_config: repo::logistics::logistics_company_config::LogisticsCompanyConfigRepository,
    pub logistics_print_config: repo::logistics::print_config::PrintConfigRepository,
    pub logistics_shipping_list: repo::logistics::shipping_list::LogisticsRepository,
    pub logistics_shipping_tpl: repo::logistics::shipping_tpl::ShippingTplRepository,
    pub logistics_shipping_type: repo::logistics::shipping_type::ShippingTypeRepository,
    pub logistics_sunmi_print_callback: repo::logistics::sunmi_print_callback::SunmiPrintCallbackRepository,
    pub content_article: repo::content::article::ArticleRepository,
    pub content_decorate: repo::content::decorate::DecorateRepository,
    pub content_mobile_cat_nav: repo::content::mobile_cat_nav::MobileCatNavRepository,
    pub content_pc_cat_floor: repo::content::pc_cat_floor::PcCatFloorRepository,
    pub content_pc_navigation: repo::content::pc_navigation::PcNavigationRepository,
    pub content_seo: repo::content::seo::SeoRepository,
    pub content_share: repo::content::share::ShareRepository,
    pub content_wechat_live_sync_callback: repo::content::wechat_live_sync_callback::WechatLiveSyncCallbackRepository,
    pub system_app_version_admin: repo::system::app_version_admin::AppVersionAdminRepository,
    pub system_area_code: repo::system::area_code::AreaCodeRepository,
    pub system_cache_manage: repo::system::cache_manage::CacheManageRepository,
    pub system_config_client: repo::system::config_client::ConfigClientRepository,
    pub system_example: repo::system::example::ExampleRepository,
    pub system_files: repo::system::files::FilesRepository,
    pub system_mail_templates: repo::system::mail_templates::MailTemplatesRepository,
    pub system_message_type: repo::system::message_type::MessageTypeRepository,
    pub system_pc: repo::system::pc::PcRepository,
    pub system_region: repo::system::region::RegionRepository,
    pub system_region_client: repo::system::region_client::RegionClientRepository,
    pub im_im_config: repo::im::im_config::ImConfigRepository,
    pub im_im_message: repo::im::im_message::ImMessageRepository,
    pub im_im_servant: repo::im::im_servant::ImServantRepository,
    pub im_web_socket: repo::im::web_socket::WebSocketRepository,
    pub analytics_statistics: repo::analytics::statistics::StatisticsRepository,
    pub openapi_workbuddy: repo::openapi::workbuddy::WorkbuddyRepository,
}

impl Repositories {
    #[must_use]
    pub fn new(pool: DbPool) -> Self {
        Self {
            identity_admin_log: repo::identity::admin_log::AdminLogRepository::new(pool.clone()),
            identity_admin_user: repo::identity::admin_user::AdminUserRepository::new(pool.clone()),
            identity_auth: repo::identity::auth::AdminUserRepository::new(pool.clone()),
            identity_authority: repo::identity::authority::AuthorityRepository::new(pool.clone()),
            merchant_merchant_client: repo::merchant::merchant_client::MerchantClientRepository::new(pool.clone()),
            merchant_order_config: repo::merchant::order_config::OrderConfigRepository::new(pool.clone()),
            merchant_shop: repo::merchant::shop::ShopRepository::new(pool.clone()),
            merchant_shop_client: repo::merchant::shop_client::ShopClientRepository::new(pool.clone()),
            merchant_shop_client_category: repo::merchant::shop_client_category::ShopClientCategoryRepository::new(pool.clone()),
            merchant_shop_manage: repo::merchant::shop_manage::ShopManageRepository::new(pool.clone()),
            merchant_shop_product_category: repo::merchant::shop_product_category::ShopProductCategoryRepository::new(pool.clone()),
            merchant_vendor_deposit_shop: repo::merchant::vendor_deposit_shop::VendorDepositShopRepository::new(pool.clone()),
            wholesale_wholesale_client: repo::wholesale::wholesale_client::WholesaleClientRepository::new(pool.clone()),
            catalog_attributes_tpl: repo::catalog::attributes_tpl::AttributesTplRepository::new(pool.clone()),
            catalog_brand: repo::catalog::brand::BrandRepository::new(pool.clone()),
            catalog_category: repo::catalog::category::CategoryRepository::new(pool.clone()),
            catalog_category_all: repo::catalog::category_all::CatalogRepository::new(pool.clone()),
            catalog_product: repo::catalog::product::ProductRepository::new(pool.clone()),
            catalog_product_manage: repo::catalog::product_manage::ProductManageRepository::new(pool.clone()),
            catalog_product_stock: repo::catalog::product_stock::ProductStockRepository::new(pool.clone()),
            catalog_short_url: repo::catalog::short_url::ShortUrlRepository::new(pool.clone()),
            catalog_stock_warning: repo::catalog::stock_warning::StockWarningRepository::new(pool.clone()),
            trade_aftersales: repo::trade::aftersales::AftersalesRepository::new(pool.clone()),
            trade_aftersales_client: repo::trade::aftersales_client::AftersalesClientRepository::new(pool.clone()),
            trade_cart: repo::trade::cart::CartRepository::new(pool.clone()),
            trade_order: repo::trade::order::OrderRepository::new(pool.clone()),
            trade_order_admin: repo::trade::order_admin::OrderAdminRepository::new(pool.clone()),
            trade_proxy_shopping_order_check: repo::trade::proxy_shopping_order_check::ProxyShoppingOrderCheckRepository::new(pool.clone()),
            trade_proxy_shopping_pickup_contact: repo::trade::proxy_shopping_pickup_contact::ProxyShoppingPickupContactRepository::new(pool.clone()),
            trade_proxy_shopping_product: repo::trade::proxy_shopping_product::ProxyShoppingProductRepository::new(pool.clone()),
            trade_proxy_shopping_user: repo::trade::proxy_shopping_user::ProxyShoppingUserRepository::new(pool.clone()),
            trade_proxy_shopping_user_address: repo::trade::proxy_shopping_user_address::ProxyShoppingUserAddressRepository::new(pool.clone()),
            trade_proxy_shopping_user_coupon: repo::trade::proxy_shopping_user_coupon::ProxyShoppingUserCouponRepository::new(pool.clone()),
            payment_paylog: repo::payment::paylog::PaylogRepository::new(pool.clone()),
            payment_paylog_batch: repo::payment::paylog_batch::PaylogBatchRepository::new(pool.clone()),
            payment_refund_apply: repo::payment::refund_apply::RefundApplyRepository::new(pool.clone()),
            payment_tax_rate: repo::payment::tax_rate::TaxRateRepository::new(pool.clone()),
            payment_user_balance_log: repo::payment::user_balance_log::UserBalanceLogRepository::new(pool.clone()),
            marketing_buyer_show: repo::marketing::buyer_show::BuyerShowRepository::new(pool.clone()),
            marketing_buyer_show_client: repo::marketing::buyer_show_client::BuyerShowClientRepository::new(pool.clone()),
            marketing_buyer_show_comment: repo::marketing::buyer_show_comment::BuyerShowCommentRepository::new(pool.clone()),
            marketing_buyer_show_group: repo::marketing::buyer_show_group::BuyerShowGroupRepository::new(pool.clone()),
            marketing_coupon: repo::marketing::coupon::CouponRepository::new(pool.clone()),
            marketing_product_style_admin: repo::marketing::product_style_admin::ProductStyleAdminRepository::new(pool.clone()),
            marketing_salesman: repo::marketing::salesman::SalesmanRepository::new(pool.clone()),
            marketing_salesman_material_category: repo::marketing::salesman_material_category::SalesmanMaterialCategoryRepository::new(pool.clone()),
            marketing_seckill: repo::marketing::seckill::SeckillRepository::new(pool.clone()),
            marketing_time_discount: repo::marketing::time_discount::TimeDiscountRepository::new(pool.clone()),
            member_feedback: repo::member::feedback::FeedbackRepository::new(pool.clone()),
            member_member_lists: repo::member::member_lists::MemberRepository::new(pool.clone()),
            member_member_tag: repo::member::member_tag::MemberTagRepository::new(pool.clone()),
            member_member_tag_group: repo::member::member_tag_group::MemberTagGroupRepository::new(pool.clone()),
            member_user: repo::member::user::UserRepository::new(pool.clone()),
            member_user_center: repo::member::user_center::UserCenterRepository::new(pool.clone()),
            member_user_company: repo::member::user_company::UserCompanyRepository::new(pool.clone()),
            member_user_manage: repo::member::user_manage::UserManageRepository::new(pool.clone()),
            member_user_points_log: repo::member::user_points_log::UserPointsLogRepository::new(pool.clone()),
            member_user_rank: repo::member::user_rank::UserRankRepository::new(pool.clone()),
            logistics_logistics_company: repo::logistics::logistics_company::LogisticsCompanyRepository::new(pool.clone()),
            logistics_logistics_company_config: repo::logistics::logistics_company_config::LogisticsCompanyConfigRepository::new(pool.clone()),
            logistics_print_config: repo::logistics::print_config::PrintConfigRepository::new(pool.clone()),
            logistics_shipping_list: repo::logistics::shipping_list::LogisticsRepository::new(pool.clone()),
            logistics_shipping_tpl: repo::logistics::shipping_tpl::ShippingTplRepository::new(pool.clone()),
            logistics_shipping_type: repo::logistics::shipping_type::ShippingTypeRepository::new(pool.clone()),
            logistics_sunmi_print_callback: repo::logistics::sunmi_print_callback::SunmiPrintCallbackRepository::new(pool.clone()),
            content_article: repo::content::article::ArticleRepository::new(pool.clone()),
            content_decorate: repo::content::decorate::DecorateRepository::new(pool.clone()),
            content_mobile_cat_nav: repo::content::mobile_cat_nav::MobileCatNavRepository::new(pool.clone()),
            content_pc_cat_floor: repo::content::pc_cat_floor::PcCatFloorRepository::new(pool.clone()),
            content_pc_navigation: repo::content::pc_navigation::PcNavigationRepository::new(pool.clone()),
            content_seo: repo::content::seo::SeoRepository::new(pool.clone()),
            content_share: repo::content::share::ShareRepository::new(pool.clone()),
            content_wechat_live_sync_callback: repo::content::wechat_live_sync_callback::WechatLiveSyncCallbackRepository::new(pool.clone()),
            system_app_version_admin: repo::system::app_version_admin::AppVersionAdminRepository::new(pool.clone()),
            system_area_code: repo::system::area_code::AreaCodeRepository::new(pool.clone()),
            system_cache_manage: repo::system::cache_manage::CacheManageRepository::new(pool.clone()),
            system_config_client: repo::system::config_client::ConfigClientRepository::new(pool.clone()),
            system_example: repo::system::example::ExampleRepository::new(pool.clone()),
            system_files: repo::system::files::FilesRepository::new(pool.clone()),
            system_mail_templates: repo::system::mail_templates::MailTemplatesRepository::new(pool.clone()),
            system_message_type: repo::system::message_type::MessageTypeRepository::new(pool.clone()),
            system_pc: repo::system::pc::PcRepository::new(pool.clone()),
            system_region: repo::system::region::RegionRepository::new(pool.clone()),
            system_region_client: repo::system::region_client::RegionClientRepository::new(pool.clone()),
            im_im_config: repo::im::im_config::ImConfigRepository::new(pool.clone()),
            im_im_message: repo::im::im_message::ImMessageRepository::new(pool.clone()),
            im_im_servant: repo::im::im_servant::ImServantRepository::new(pool.clone()),
            im_web_socket: repo::im::web_socket::WebSocketRepository::new(pool.clone()),
            analytics_statistics: repo::analytics::statistics::StatisticsRepository::new(pool.clone()),
            openapi_workbuddy: repo::openapi::workbuddy::WorkbuddyRepository::new(pool.clone()),
        }
    }
}
