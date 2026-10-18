//! 分销模式设置（marketing::salesman_config）仓储。
//!
//! 表 `salesman_config`（4 列）：`id` / `shop_id` / `code` / `data`
//! （`python tools/schema_parser.py --table salesman_config`）。
//! `data` 为整份配置 JSON；按 `code` 定位记录（Java
//! `lambdaQuery().eq(SalesmanConfig::getCode, code).one()`）。
//!
//! 查询统一运行时 `sqlx::query_as`；`save` 为「存在则更新、否则插入」。

use sqlx::MySqlPool;

use tigshop_core::{Error, Result};

/// 配置记录行。
#[derive(Debug, Clone, Default, sqlx::FromRow)]
pub struct SalesmanConfigRow {
    pub id: i32,
    pub shop_id: Option<i32>,
    pub code: Option<String>,
    pub data: Option<String>,
}

/// 配置仓储。
#[derive(Clone)]
pub struct SalesmanConfigRepository {
    pool: MySqlPool,
}

impl SalesmanConfigRepository {
    #[must_use]
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    /// 暴露连接池。
    #[must_use]
    pub fn pool_clone(&self) -> MySqlPool {
        self.pool.clone()
    }

    /// 按 code 查询配置（Java `eq(code).one()`）。
    pub async fn find_by_code(&self, code: &str) -> Result<Option<SalesmanConfigRow>> {
        sqlx::query_as::<_, SalesmanConfigRow>(
            "SELECT id, shop_id, code, data FROM salesman_config WHERE code = ? LIMIT 1",
        )
        .bind(code)
        .fetch_optional(&self.pool)
        .await
        .map_err(map_err)
    }

    /// 保存配置：存在则更新 `data`（及 `shop_id`），否则插入。
    ///
    /// 返回记录主键。
    pub async fn upsert(&self, code: &str, shop_id: i32, data: &str) -> Result<i32> {
        if let Some(row) = self.find_by_code(code).await? {
            sqlx::query("UPDATE salesman_config SET data = ?, shop_id = ? WHERE id = ?")
                .bind(data)
                .bind(shop_id)
                .bind(row.id)
                .execute(&self.pool)
                .await
                .map_err(map_err)?;
            return Ok(row.id);
        }
        let res = sqlx::query(
            "INSERT INTO salesman_config (shop_id, code, data) VALUES (?, ?, ?)",
        )
        .bind(shop_id)
        .bind(code)
        .bind(data)
        .execute(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(i32::try_from(res.last_insert_id()).unwrap_or(i32::MAX))
    }
}

fn map_err(e: sqlx::Error) -> Error {
    Error::Database(e.to_string())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn row_defaults_are_empty() {
        let row = SalesmanConfigRow::default();
        assert_eq!(row.id, 0);
        assert!(row.code.is_none());
        assert!(row.data.is_none());
    }
}
