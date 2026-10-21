//! 积分签到配置域仓储（marketing::sign_in_setting）。
//! 表 `sign_in_setting`（6 列，主键 id）。

use sqlx::{MySql, MySqlPool, QueryBuilder};

use tigshop_core::entities::promotion::SignInSetting;
use tigshop_core::models::marketing::sign_in_setting::{
    limit_offset, SignInSettingListQuery,
};
use tigshop_core::{Error, Result};

const SELECT: &str = "SELECT id, name, points, day_num, sort_order, create_time FROM sign_in_setting";

/// 积分签到配置域仓储。
#[derive(Clone)]
pub struct SignInSettingRepository {
    pool: MySqlPool,
}

impl SignInSettingRepository {
    #[must_use]
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    pub async fn paginate(
        &self,
        q: &SignInSettingListQuery,
        limit: i64,
        offset: i64,
    ) -> Result<(i64, Vec<SignInSetting>)> {
        let keyword = q.effective_keyword();
        let mut cq: QueryBuilder<'_, MySql> =
            QueryBuilder::new("SELECT COUNT(*) FROM sign_in_setting WHERE 1 = 1");
        if let Some(keyword) = keyword {
            cq.push(" AND name LIKE ");
            cq.push_bind(format!("%{keyword}%"));
        }
        let total: (i64,) = cq.build_query_as().fetch_one(&self.pool).await.map_err(map_err)?;
        let mut rq: QueryBuilder<'_, MySql> = QueryBuilder::new(format!("{SELECT} WHERE 1 = 1"));
        if let Some(keyword) = keyword {
            rq.push(" AND name LIKE ");
            rq.push_bind(format!("%{keyword}%"));
        }
        rq.push(q.order_clause());
        rq.push(format!(" LIMIT {limit} OFFSET {offset}"));
        let rows: Vec<SignInSetting> = rq.build_query_as().fetch_all(&self.pool).await.map_err(map_err)?;
        Ok((total.0, rows))
    }

    pub async fn find_by_id(&self, id: i32) -> Result<Option<SignInSetting>> {
        sqlx::query_as::<_, SignInSetting>(&format!("{SELECT} WHERE id = ? LIMIT 1"))
            .bind(id)
            .fetch_optional(&self.pool)
            .await
            .map_err(map_err)
    }

    /// 最大 `day_num`（Java `orderByDesc(dayNum).last("limit 1")`）。
    pub async fn max_day_num(&self) -> Result<Option<i32>> {
        let row: Option<(Option<i32>,)> =
            sqlx::query_as("SELECT day_num FROM sign_in_setting ORDER BY day_num DESC LIMIT 1")
                .fetch_optional(&self.pool)
                .await
                .map_err(map_err)?;
        Ok(row.and_then(|(v,)| v))
    }

    pub async fn insert(&self, entity: &SignInSetting) -> Result<i32> {
        let res = sqlx::query(
            "INSERT INTO sign_in_setting (name, points, day_num, sort_order, create_time) VALUES (?, ?, ?, ?, ?)",
        )
        .bind(&entity.name)
        .bind(entity.points)
        .bind(entity.day_num)
        .bind(entity.sort_order)
        .bind(entity.create_time)
        .execute(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(i32::try_from(res.last_insert_id()).unwrap_or_default())
    }

    pub async fn update(&self, entity: &SignInSetting) -> Result<u64> {
        let res = sqlx::query(
            "UPDATE sign_in_setting SET name = ?, points = ?, day_num = ?, sort_order = ?, create_time = ? WHERE id = ?",
        )
        .bind(&entity.name)
        .bind(entity.points)
        .bind(entity.day_num)
        .bind(entity.sort_order)
        .bind(entity.create_time)
        .bind(entity.id)
        .execute(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(res.rows_affected())
    }

    pub async fn delete(&self, id: i32) -> Result<bool> {
        let res = sqlx::query("DELETE FROM sign_in_setting WHERE id = ?")
            .bind(id)
            .execute(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(res.rows_affected() > 0)
    }

    pub async fn delete_batch(&self, ids: &[i32]) -> Result<u64> {
        if ids.is_empty() {
            return Ok(0);
        }
        let mut qb: QueryBuilder<'_, MySql> = QueryBuilder::new("DELETE FROM sign_in_setting WHERE id IN (");
        {
            let mut sep = qb.separated(", ");
            for id in ids {
                sep.push_bind(*id);
            }
        }
        qb.push(")");
        let res = qb.build().execute(&self.pool).await.map_err(map_err)?;
        Ok(res.rows_affected())
    }

    pub async fn update_field(&self, id: i32, column: &str, val: &str) -> Result<u64> {
        debug_assert!(
            tigshop_core::models::marketing::sign_in_setting::UPDATE_FIELDS.contains(&column)
        );
        let sql = format!("UPDATE sign_in_setting SET {column} = ? WHERE id = ?");
        let res = sqlx::query(&sql).bind(val).bind(id).execute(&self.pool).await.map_err(map_err)?;
        Ok(res.rows_affected())
    }

    #[must_use]
    pub fn page_window(page: Option<i64>, size: Option<i64>) -> (i64, i64) {
        limit_offset(page, size)
    }
}

fn map_err(e: sqlx::Error) -> Error {
    Error::Database(e.to_string())
}

#[cfg(test)]
mod tests {
    use super::*;
    use tigshop_core::models::marketing::sign_in_setting::COLUMNS;

    #[test]
    fn select_matches_columns() {
        let selected = SELECT.trim_start_matches("SELECT ").split(" FROM ").next().unwrap();
        assert_eq!(selected, COLUMNS.join(", "));
    }
}
