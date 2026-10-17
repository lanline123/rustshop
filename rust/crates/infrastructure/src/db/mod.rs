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
    pub identity: repo::identity::Repositories,
    pub merchant: repo::merchant::Repositories,
    pub wholesale: repo::wholesale::Repositories,
    pub catalog: repo::catalog::Repositories,
    pub trade: repo::trade::Repositories,
    pub payment: repo::payment::Repositories,
    pub marketing: repo::marketing::Repositories,
    pub member: repo::member::Repositories,
    pub logistics: repo::logistics::Repositories,
    pub content: repo::content::Repositories,
    pub system: repo::system::Repositories,
    pub im: repo::im::Repositories,
    pub analytics: repo::analytics::Repositories,
    pub openapi: repo::openapi::Repositories,
}

impl Repositories {
    #[must_use]
    pub fn new(pool: DbPool) -> Self {
        Self {
            identity: repo::identity::Repositories::new(pool.clone()),
            merchant: repo::merchant::Repositories::new(pool.clone()),
            wholesale: repo::wholesale::Repositories::new(pool.clone()),
            catalog: repo::catalog::Repositories::new(pool.clone()),
            trade: repo::trade::Repositories::new(pool.clone()),
            payment: repo::payment::Repositories::new(pool.clone()),
            marketing: repo::marketing::Repositories::new(pool.clone()),
            member: repo::member::Repositories::new(pool.clone()),
            logistics: repo::logistics::Repositories::new(pool.clone()),
            content: repo::content::Repositories::new(pool.clone()),
            system: repo::system::Repositories::new(pool.clone()),
            im: repo::im::Repositories::new(pool.clone()),
            analytics: repo::analytics::Repositories::new(pool.clone()),
            openapi: repo::openapi::Repositories::new(pool.clone()),
        }
    }
}
