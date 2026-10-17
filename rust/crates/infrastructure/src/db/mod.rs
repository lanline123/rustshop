//! 数据库连接池与仓储入口。
//!
//! 对应原 Java HikariCP + MyBatis Plus：
//! - 连接池：SQLx `MySqlPool`（替代 HikariCP）
//! - 迁移：SQLx migrate（替代 Flyway/手工 SQL 脚本）
//! - 兼容原库 `connection-init-sql`：会话级去除 `ONLY_FULL_GROUP_BY`

use sqlx::mysql::{MySqlPool, MySqlPoolOptions};
use tigshop_common::AppConfig;

pub mod repo;

pub use repo::admin_user::AdminUserRepository;
pub use repo::cart::CartRepository;
pub use repo::catalog::CatalogRepository;
pub use repo::content::ArticleRepository;
pub use repo::coupon::CouponRepository;
pub use repo::decorate::DecorateRepository;
pub use repo::logistics::LogisticsRepository;
pub use repo::member::MemberRepository;
pub use repo::order::OrderRepository;
pub use repo::paylog::PaylogRepository;
pub use repo::product::ProductRepository;
pub use repo::refund_apply::RefundApplyRepository;
pub use repo::salesman::SalesmanRepository;
pub use repo::seckill::SeckillRepository;
pub use repo::shop::ShopRepository;
pub use repo::slice044::Slice044Repository;
pub use repo::slice039::Slice039Repository;
pub use repo::slice210::Slice210Repository;
pub use repo::slice207::Slice207Repository;
pub use repo::slice163::Slice163Repository;
pub use repo::slice157::Slice157Repository;
pub use repo::slice137::Slice137Repository;
pub use repo::slice108::Slice108Repository;
pub use repo::slice085::Slice085Repository;
pub use repo::slice038::Slice038Repository;
pub use repo::slice037::Slice037Repository;
pub use repo::slice220::Slice220Repository;
pub use repo::slice209::Slice209Repository;
pub use repo::slice201::Slice201Repository;
pub use repo::slice185::Slice185Repository;
pub use repo::slice092::Slice092Repository;
pub use repo::slice081::Slice081Repository;
pub use repo::slice172::Slice172Repository;
pub use repo::slice166::Slice166Repository;
pub use repo::slice144::Slice144Repository;
pub use repo::slice139::Slice139Repository;
pub use repo::slice091::Slice091Repository;
pub use repo::slice075::Slice075Repository;
pub use repo::slice217::Slice217Repository;
pub use repo::slice205::Slice205Repository;
pub use repo::slice192::Slice192Repository;
pub use repo::slice186::Slice186Repository;
pub use repo::slice170::Slice170Repository;
pub use repo::slice120::Slice120Repository;
pub use repo::slice093::Slice093Repository;
pub use repo::slice082::Slice082Repository;
pub use repo::slice056::Slice056Repository;
pub use repo::slice216::Slice216Repository;
pub use repo::slice214::Slice214Repository;
pub use repo::slice213::Slice213Repository;
pub use repo::slice208::Slice208Repository;
pub use repo::slice206::Slice206Repository;
pub use repo::slice203::Slice203Repository;
pub use repo::slice200::Slice200Repository;
pub use repo::slice199::Slice199Repository;
pub use repo::slice197::Slice197Repository;
pub use repo::slice193::Slice193Repository;
pub use repo::slice191::Slice191Repository;
pub use repo::slice190::Slice190Repository;
pub use repo::slice189::Slice189Repository;
pub use repo::slice181::Slice181Repository;
pub use repo::slice176::Slice176Repository;
pub use repo::slice174::Slice174Repository;
pub use repo::slice171::Slice171Repository;
pub use repo::slice168::Slice168Repository;
pub use repo::slice160::Slice160Repository;
pub use repo::slice148::Slice148Repository;
pub use repo::slice136::Slice136Repository;
pub use repo::slice135::Slice135Repository;
pub use repo::slice133::Slice133Repository;
pub use repo::slice130::Slice130Repository;
pub use repo::slice124::Slice124Repository;
pub use repo::slice116::Slice116Repository;
pub use repo::slice114::Slice114Repository;
pub use repo::slice113::Slice113Repository;
pub use repo::slice102::Slice102Repository;
pub use repo::slice098::Slice098Repository;
pub use repo::slice084::Slice084Repository;
pub use repo::slice073::Slice073Repository;
pub use repo::slice066::Slice066Repository;
pub use repo::slice065::Slice065Repository;
pub use repo::slice064::Slice064Repository;
pub use repo::slice063::Slice063Repository;
pub use repo::slice059::Slice059Repository;
pub use repo::slice051::Slice051Repository;
pub use repo::slice047::Slice047Repository;
pub use repo::slice036::Slice036Repository;
pub use repo::slice034::Slice034Repository;
pub use repo::slice164::Slice164Repository;
pub use repo::slice147::Slice147Repository;
pub use repo::slice143::Slice143Repository;
pub use repo::slice141::Slice141Repository;
pub use repo::slice132::Slice132Repository;
pub use repo::slice118::Slice118Repository;
pub use repo::slice117::Slice117Repository;
pub use repo::slice115::Slice115Repository;
pub use repo::slice111::Slice111Repository;
pub use repo::slice106::Slice106Repository;
pub use repo::slice094::Slice094Repository;
pub use repo::slice090::Slice090Repository;
pub use repo::slice053::Slice053Repository;
pub use repo::slice100::Slice100Repository;
pub use repo::slice042::Slice042Repository;
pub use repo::slice032::Slice032Repository;
pub use repo::slice031::Slice031Repository;
pub use repo::slice030::Slice030Repository;
pub use repo::slice029::Slice029Repository;
pub use repo::slice028::Slice028Repository;
pub use repo::slice027::Slice027Repository;
pub use repo::slice188::Slice188Repository;
pub use repo::slice187::Slice187Repository;
pub use repo::slice026::Slice026Repository;
pub use repo::slice156::Slice156Repository;
pub use repo::slice140::Slice140Repository;
pub use repo::slice134::Slice134Repository;
pub use repo::slice097::Slice097Repository;
pub use repo::slice095::Slice095Repository;
pub use repo::slice025::Slice025Repository;
pub use repo::slice024::Slice024Repository;
pub use repo::slice023::Slice023Repository;
pub use repo::slice022::Slice022Repository;
pub use repo::slice021::Slice021Repository;
pub use repo::slice020::Slice020Repository;
pub use repo::slice019::Slice019Repository;
pub use repo::slice018::Slice018Repository;
pub use repo::slice017::Slice017Repository;
pub use repo::slice016::Slice016Repository;
pub use repo::slice015::Slice015Repository;
pub use repo::slice001::Slice001Repository;
pub use repo::slice002::Slice002Repository;
pub use repo::slice003::Slice003Repository;
pub use repo::slice004::Slice004Repository;
pub use repo::slice005::Slice005Repository;
pub use repo::slice006::Slice006Repository;
pub use repo::slice007::Slice007Repository;
pub use repo::slice008::Slice008Repository;
pub use repo::slice009::Slice009Repository;
pub use repo::slice010::Slice010Repository;
pub use repo::slice011::Slice011Repository;
pub use repo::slice012::Slice012Repository;
pub use repo::slice013::Slice013Repository;
pub use repo::slice014::Slice014Repository;
pub use repo::time_discount::TimeDiscountRepository;
pub use repo::user::UserRepository;

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

/// 仓储集合：全局单例地持有各仓储，替代 Spring 的 @Autowired 注入。
#[derive(Clone)]
pub struct Repositories {
    pub admin_users: AdminUserRepository,
    pub products: ProductRepository,
    pub orders: OrderRepository,
    pub carts: CartRepository,
    pub users: UserRepository,
    pub coupons: CouponRepository,
    pub articles: ArticleRepository,
    pub catalog: CatalogRepository,
    pub logistics: LogisticsRepository,
    pub member: MemberRepository,
    pub seckills: SeckillRepository,
    pub time_discounts: TimeDiscountRepository,
    pub paylogs: PaylogRepository,
    pub refund_applies: RefundApplyRepository,
    pub shops: ShopRepository,
    pub decorates: DecorateRepository,
    pub salesmen: SalesmanRepository,
    pub slice001: Slice001Repository,
    pub slice002: Slice002Repository,
    pub slice003: Slice003Repository,
    pub slice004: Slice004Repository,
    pub slice005: Slice005Repository,
    pub slice006: Slice006Repository,
    pub slice007: Slice007Repository,
    pub slice008: Slice008Repository,
    pub slice009: Slice009Repository,
    pub slice010: Slice010Repository,
    pub slice011: Slice011Repository,
    pub slice012: Slice012Repository,
    pub slice013: Slice013Repository,
    pub slice014: Slice014Repository,
    pub slice015: Slice015Repository,
    pub slice016: Slice016Repository,
    pub slice017: Slice017Repository,
    pub slice018: Slice018Repository,
    pub slice019: Slice019Repository,
    pub slice020: Slice020Repository,
    pub slice021: Slice021Repository,
    pub slice022: Slice022Repository,
    pub slice023: Slice023Repository,
    pub slice024: Slice024Repository,
    pub slice025: Slice025Repository,
    pub slice026: Slice026Repository,
    pub slice095: Slice095Repository,
    pub slice097: Slice097Repository,
    pub slice134: Slice134Repository,
    pub slice140: Slice140Repository,
    pub slice156: Slice156Repository,
    pub slice187: Slice187Repository,
    pub slice188: Slice188Repository,
    pub slice027: Slice027Repository,
    pub slice028: Slice028Repository,
    pub slice029: Slice029Repository,
    pub slice030: Slice030Repository,
    pub slice031: Slice031Repository,
    pub slice032: Slice032Repository,
    pub slice042: Slice042Repository,
    pub slice100: Slice100Repository,
    pub slice053: Slice053Repository,
    pub slice090: Slice090Repository,
    pub slice094: Slice094Repository,
    pub slice106: Slice106Repository,
    pub slice111: Slice111Repository,
    pub slice115: Slice115Repository,
    pub slice117: Slice117Repository,
    pub slice118: Slice118Repository,
    pub slice132: Slice132Repository,
    pub slice141: Slice141Repository,
    pub slice143: Slice143Repository,
    pub slice147: Slice147Repository,
    pub slice164: Slice164Repository,
    pub slice034: Slice034Repository,
    pub slice036: Slice036Repository,
    pub slice047: Slice047Repository,
    pub slice051: Slice051Repository,
    pub slice059: Slice059Repository,
    pub slice063: Slice063Repository,
    pub slice064: Slice064Repository,
    pub slice065: Slice065Repository,
    pub slice066: Slice066Repository,
    pub slice073: Slice073Repository,
    pub slice084: Slice084Repository,
    pub slice098: Slice098Repository,
    pub slice102: Slice102Repository,
    pub slice113: Slice113Repository,
    pub slice114: Slice114Repository,
    pub slice116: Slice116Repository,
    pub slice124: Slice124Repository,
    pub slice130: Slice130Repository,
    pub slice133: Slice133Repository,
    pub slice135: Slice135Repository,
    pub slice136: Slice136Repository,
    pub slice148: Slice148Repository,
    pub slice160: Slice160Repository,
    pub slice168: Slice168Repository,
    pub slice171: Slice171Repository,
    pub slice174: Slice174Repository,
    pub slice176: Slice176Repository,
    pub slice181: Slice181Repository,
    pub slice189: Slice189Repository,
    pub slice190: Slice190Repository,
    pub slice191: Slice191Repository,
    pub slice193: Slice193Repository,
    pub slice197: Slice197Repository,
    pub slice199: Slice199Repository,
    pub slice200: Slice200Repository,
    pub slice203: Slice203Repository,
    pub slice206: Slice206Repository,
    pub slice208: Slice208Repository,
    pub slice213: Slice213Repository,
    pub slice214: Slice214Repository,
    pub slice216: Slice216Repository,
    pub slice056: Slice056Repository,
    pub slice082: Slice082Repository,
    pub slice093: Slice093Repository,
    pub slice120: Slice120Repository,
    pub slice170: Slice170Repository,
    pub slice186: Slice186Repository,
    pub slice192: Slice192Repository,
    pub slice205: Slice205Repository,
    pub slice217: Slice217Repository,
    pub slice075: Slice075Repository,
    pub slice091: Slice091Repository,
    pub slice139: Slice139Repository,
    pub slice144: Slice144Repository,
    pub slice166: Slice166Repository,
    pub slice172: Slice172Repository,
    pub slice081: Slice081Repository,
    pub slice092: Slice092Repository,
    pub slice185: Slice185Repository,
    pub slice201: Slice201Repository,
    pub slice209: Slice209Repository,
    pub slice220: Slice220Repository,
    pub slice037: Slice037Repository,
    pub slice038: Slice038Repository,
    pub slice085: Slice085Repository,
    pub slice108: Slice108Repository,
    pub slice137: Slice137Repository,
    pub slice157: Slice157Repository,
    pub slice163: Slice163Repository,
    pub slice207: Slice207Repository,
    pub slice210: Slice210Repository,
    pub slice039: Slice039Repository,
    pub slice044: Slice044Repository,
}

impl Repositories {
    pub fn new(pool: DbPool) -> Self {
        Self {
            admin_users: AdminUserRepository::new(pool.clone()),
            products: ProductRepository::new(pool.clone()),
            orders: OrderRepository::new(pool.clone()),
            carts: CartRepository::new(pool.clone()),
            users: UserRepository::new(pool.clone()),
            coupons: CouponRepository::new(pool.clone()),
            articles: ArticleRepository::new(pool.clone()),
            catalog: CatalogRepository::new(pool.clone()),
            logistics: LogisticsRepository::new(pool.clone()),
            member: MemberRepository::new(pool.clone()),
            seckills: SeckillRepository::new(pool.clone()),
            time_discounts: TimeDiscountRepository::new(pool.clone()),
            paylogs: PaylogRepository::new(pool.clone()),
            refund_applies: RefundApplyRepository::new(pool.clone()),
            shops: ShopRepository::new(pool.clone()),
            decorates: DecorateRepository::new(pool.clone()),
            salesmen: SalesmanRepository::new(pool.clone()),
            slice001: Slice001Repository::new(pool.clone()),
            slice002: Slice002Repository::new(pool.clone()),
            slice003: Slice003Repository::new(pool.clone()),
            slice004: Slice004Repository::new(pool.clone()),
            slice005: Slice005Repository::new(pool.clone()),
            slice006: Slice006Repository::new(pool.clone()),
            slice007: Slice007Repository::new(pool.clone()),
            slice008: Slice008Repository::new(pool.clone()),
            slice009: Slice009Repository::new(pool.clone()),
            slice010: Slice010Repository::new(pool.clone()),
            slice011: Slice011Repository::new(pool.clone()),
            slice012: Slice012Repository::new(pool.clone()),
            slice013: Slice013Repository::new(pool.clone()),
            slice014: Slice014Repository::new(pool.clone()),
            slice015: Slice015Repository::new(pool.clone()),
            slice016: Slice016Repository::new(pool.clone()),
            slice017: Slice017Repository::new(pool.clone()),
            slice018: Slice018Repository::new(pool.clone()),
            slice019: Slice019Repository::new(pool.clone()),
            slice020: Slice020Repository::new(pool.clone()),
            slice156: Slice156Repository::new(pool.clone()),
            slice140: Slice140Repository::new(pool.clone()),
            slice134: Slice134Repository::new(pool.clone()),
            slice097: Slice097Repository::new(pool.clone()),
            slice095: Slice095Repository::new(pool.clone()),
            slice021: Slice021Repository::new(pool.clone()),
            slice022: Slice022Repository::new(pool.clone()),
            slice023: Slice023Repository::new(pool.clone()),
            slice024: Slice024Repository::new(pool.clone()),
            slice025: Slice025Repository::new(pool.clone()),
            slice026: Slice026Repository::new(pool.clone()),
            slice187: Slice187Repository::new(pool.clone()),
            slice188: Slice188Repository::new(pool.clone()),
            slice027: Slice027Repository::new(pool.clone()),
            slice028: Slice028Repository::new(pool.clone()),
            slice029: Slice029Repository::new(pool.clone()),
            slice030: Slice030Repository::new(pool.clone()),
            slice031: Slice031Repository::new(pool.clone()),
            slice032: Slice032Repository::new(pool.clone()),
            slice042: Slice042Repository::new(pool.clone()),
            slice100: Slice100Repository::new(pool.clone()),
            slice053: Slice053Repository::new(pool.clone()),
            slice090: Slice090Repository::new(pool.clone()),
            slice094: Slice094Repository::new(pool.clone()),
            slice106: Slice106Repository::new(pool.clone()),
            slice111: Slice111Repository::new(pool.clone()),
            slice115: Slice115Repository::new(pool.clone()),
            slice117: Slice117Repository::new(pool.clone()),
            slice118: Slice118Repository::new(pool.clone()),
            slice132: Slice132Repository::new(pool.clone()),
            slice141: Slice141Repository::new(pool.clone()),
            slice143: Slice143Repository::new(pool.clone()),
            slice147: Slice147Repository::new(pool.clone()),
            slice164: Slice164Repository::new(pool.clone()),
            slice034: Slice034Repository::new(pool.clone()),
            slice036: Slice036Repository::new(pool.clone()),
            slice047: Slice047Repository::new(pool.clone()),
            slice051: Slice051Repository::new(pool.clone()),
            slice059: Slice059Repository::new(pool.clone()),
            slice063: Slice063Repository::new(pool.clone()),
            slice064: Slice064Repository::new(pool.clone()),
            slice065: Slice065Repository::new(pool.clone()),
            slice066: Slice066Repository::new(pool.clone()),
            slice073: Slice073Repository::new(pool.clone()),
            slice084: Slice084Repository::new(pool.clone()),
            slice098: Slice098Repository::new(pool.clone()),
            slice102: Slice102Repository::new(pool.clone()),
            slice113: Slice113Repository::new(pool.clone()),
            slice114: Slice114Repository::new(pool.clone()),
            slice116: Slice116Repository::new(pool.clone()),
            slice124: Slice124Repository::new(pool.clone()),
            slice130: Slice130Repository::new(pool.clone()),
            slice133: Slice133Repository::new(pool.clone()),
            slice135: Slice135Repository::new(pool.clone()),
            slice136: Slice136Repository::new(pool.clone()),
            slice148: Slice148Repository::new(pool.clone()),
            slice160: Slice160Repository::new(pool.clone()),
            slice168: Slice168Repository::new(pool.clone()),
            slice171: Slice171Repository::new(pool.clone()),
            slice174: Slice174Repository::new(pool.clone()),
            slice176: Slice176Repository::new(pool.clone()),
            slice181: Slice181Repository::new(pool.clone()),
            slice189: Slice189Repository::new(pool.clone()),
            slice190: Slice190Repository::new(pool.clone()),
            slice191: Slice191Repository::new(pool.clone()),
            slice193: Slice193Repository::new(pool.clone()),
            slice197: Slice197Repository::new(pool.clone()),
            slice199: Slice199Repository::new(pool.clone()),
            slice200: Slice200Repository::new(pool.clone()),
            slice203: Slice203Repository::new(pool.clone()),
            slice206: Slice206Repository::new(pool.clone()),
            slice208: Slice208Repository::new(pool.clone()),
            slice213: Slice213Repository::new(pool.clone()),
            slice214: Slice214Repository::new(pool.clone()),
            slice216: Slice216Repository::new(pool.clone()),
            slice056: Slice056Repository::new(pool.clone()),
            slice082: Slice082Repository::new(pool.clone()),
            slice093: Slice093Repository::new(pool.clone()),
            slice120: Slice120Repository::new(pool.clone()),
            slice170: Slice170Repository::new(pool.clone()),
            slice186: Slice186Repository::new(pool.clone()),
            slice192: Slice192Repository::new(pool.clone()),
            slice205: Slice205Repository::new(pool.clone()),
            slice217: Slice217Repository::new(pool.clone()),
            slice075: Slice075Repository::new(pool.clone()),
            slice091: Slice091Repository::new(pool.clone()),
            slice139: Slice139Repository::new(pool.clone()),
            slice144: Slice144Repository::new(pool.clone()),
            slice166: Slice166Repository::new(pool.clone()),
            slice172: Slice172Repository::new(pool.clone()),
            slice081: Slice081Repository::new(pool.clone()),
            slice092: Slice092Repository::new(pool.clone()),
            slice185: Slice185Repository::new(pool.clone()),
            slice201: Slice201Repository::new(pool.clone()),
            slice209: Slice209Repository::new(pool.clone()),
            slice220: Slice220Repository::new(pool.clone()),
            slice037: Slice037Repository::new(pool.clone()),
            slice038: Slice038Repository::new(pool.clone()),
            slice085: Slice085Repository::new(pool.clone()),
            slice108: Slice108Repository::new(pool.clone()),
            slice137: Slice137Repository::new(pool.clone()),
            slice157: Slice157Repository::new(pool.clone()),
            slice163: Slice163Repository::new(pool.clone()),
            slice207: Slice207Repository::new(pool.clone()),
            slice210: Slice210Repository::new(pool.clone()),
            slice039: Slice039Repository::new(pool.clone()),
            slice044: Slice044Repository::new(pool.clone()),
        }
    }
}
