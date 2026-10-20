//! 许可证 域仓储（system::licensed）。
//!
//! 表 `config`（`biz_code` / `biz_val` / `is_del` …）；语义与
//! `repo/system/config_basic.rs` 一致：Java `SettingsHelper#set` 是
//! 「先删后插」，读取过滤 `is_del`。运行时 sqlx（**不**使用 `query!` 宏）。

use sqlx::MySqlPool;

use tigshop_core::models::system::licensed::LICENSED_CODES;
use tigshop_core::{Error, Result};

const ALIVE: &str = "(is_del IS NULL OR is_del = 0)";

/// 许可证仓储（持有连接池）。
#[derive(Clone)]
pub struct LicensedRepository {
    pool: MySqlPool,
}

fn map_err(e: sqlx::Error) -> Error {
    Error::Database(e.to_string())
}

impl LicensedRepository {
    /// 构造仓储。
    #[must_use]
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    /// 读取授权配置项（Java `ConfigService#getSettings` 的批量读）。
    pub async fn find_licensed(&self) -> Result<Vec<(String, String)>> {
        let placeholders = vec!["?"; LICENSED_CODES.len()].join(", ");
        let sql = format!(
            "SELECT biz_code, biz_val FROM config WHERE biz_code IN ({placeholders}) AND {ALIVE}"
        );
        let mut q = sqlx::query_as::<_, (String, Option<String>)>(&sql);
        for code in LICENSED_CODES {
            q = q.bind(code);
        }
        let rows = q.fetch_all(&self.pool).await.map_err(map_err)?;
        Ok(rows
            .into_iter()
            .filter_map(|(code, val)| val.map(|v| (code, v)))
            .collect())
    }

    /// 保存一组授权配置（Java `ConfigService#saveConfigSettings`）：
    /// 同一事务内逐项「先删后插」。
    pub async fn save_licensed(&self, pairs: &[(String, String)]) -> Result<()> {
        let mut tx = self.pool.begin().await.map_err(map_err)?;
        let now = tigshop_common::utils::now_millis();
        for (code, value) in pairs {
            sqlx::query("DELETE FROM config WHERE biz_code = ?")
                .bind(code)
                .execute(&mut *tx)
                .await
                .map_err(map_err)?;
            sqlx::query(
                "INSERT INTO config (biz_code, biz_val, create_time, update_time, is_del) \
                 VALUES (?, ?, ?, ?, 0)",
            )
            .bind(code)
            .bind(value)
            .bind(now)
            .bind(now)
            .execute(&mut *tx)
            .await
            .map_err(map_err)?;
        }
        tx.commit().await.map_err(map_err)?;
        Ok(())
    }
}
