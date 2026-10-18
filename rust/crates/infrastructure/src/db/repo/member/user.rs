//! `user` 仓储，替代 `UserMapper`。

use rust_decimal::Decimal;
use sqlx::MySqlPool;
use tigshop_core::entities::User;
use tigshop_core::{Error, Result};

#[derive(Clone)]
pub struct UserRepository {
    pool: MySqlPool,
}

impl UserRepository {
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    /// 按 ID 查询。
    pub async fn find_by_id(&self, user_id: i32) -> Result<Option<User>> {
        sqlx::query_as::<_, User>("SELECT * FROM user WHERE user_id = ? LIMIT 1")
            .bind(user_id)
            .fetch_optional(&self.pool)
            .await
            .map_err(map_err)
    }

    /// 按手机号查询。
    /// 按用户名查询（前端「账号」登录兼容）。
    pub async fn find_by_username(&self, username: &str) -> Result<Option<User>> {
        sqlx::query_as::<_, User>("SELECT * FROM user WHERE username = ? LIMIT 1")
            .bind(username)
            .fetch_optional(&self.pool)
            .await
            .map_err(map_err)
    }

    pub async fn find_by_mobile(&self, mobile: &str) -> Result<Option<User>> {
        sqlx::query_as::<_, User>("SELECT * FROM user WHERE mobile = ? LIMIT 1")
            .bind(mobile)
            .fetch_optional(&self.pool)
            .await
            .map_err(map_err)
    }

    /// 注册插入，返回 user_id。
    pub async fn insert(&self, user: &User) -> Result<i32> {
        let r = sqlx::query(
            "INSERT INTO user
             (username, mobile, mobile_validated, nickname, password, balance,
              points, reg_time, status, user_type)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        )
        .bind(&user.username)
        .bind(&user.mobile)
        .bind(user.mobile_validated)
        .bind(user.nickname.as_deref().unwrap_or_default())
        .bind(&user.password)
        .bind(user.balance)
        .bind(user.points)
        .bind(user.reg_time)
        .bind(user.status)
        .bind(user.user_type)
        .execute(&self.pool)
        .await
        .map_err(map_err)?;
        Ok(r.last_insert_id() as i32)
    }

    /// 更新昵称/头像。
    pub async fn update_profile(
        &self,
        user_id: i32,
        nickname: Option<&str>,
        avatar: Option<&str>,
    ) -> Result<()> {
        let affected = sqlx::query(
            "UPDATE user SET
               nickname = COALESCE(?, nickname),
               avatar = COALESCE(?, avatar)
             WHERE user_id = ?",
        )
        .bind(nickname)
        .bind(avatar)
        .bind(user_id)
        .execute(&self.pool)
        .await
        .map_err(map_err)?
        .rows_affected();
        if affected == 0 {
            return Err(Error::NotFound("用户不存在".into()));
        }
        Ok(())
    }

    /// 余额入账（balance = balance + amount，结果不会为负）。
    pub async fn adjust_balance(&self, user_id: i32, amount: Decimal) -> Result<()> {
        sqlx::query("UPDATE user SET balance = balance + ? WHERE user_id = ?")
            .bind(amount)
            .bind(user_id)
            .execute(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(())
    }

    /// 余额出账（原子条件扣减，余额不足时 rows_affected = 0）。
    pub async fn try_debit_balance(&self, user_id: i32, amount: Decimal) -> Result<bool> {
        let affected = sqlx::query(
            "UPDATE user SET balance = balance - ? WHERE user_id = ? AND balance >= ?",
        )
        .bind(amount)
        .bind(user_id)
        .bind(amount)
        .execute(&self.pool)
        .await
        .map_err(map_err)?
        .rows_affected();
        Ok(affected > 0)
    }

    /// 会员分页查询（发放对象选择器）。
    ///
    /// 关键字模糊匹配用户名/昵称/手机号/邮箱，对齐 Java
    /// `UserCouponServiceImpl#searchIssueUsers` 的搜索口径。
    pub async fn paginate_members(
        &self,
        keyword: Option<&str>,
        page: &tigshop_core::types::PageQuery,
    ) -> Result<(u64, Vec<User>)> {
        let like = keyword
            .map(str::trim)
            .filter(|k| !k.is_empty())
            .map(|k| format!("%{k}%"));
        let (limit, offset) = page.limit_offset();

        let where_sql = "WHERE user_type = 1 AND (? IS NULL OR username LIKE ? OR nickname LIKE ? OR mobile LIKE ? OR email LIKE ?)";

        let count_sql = format!("SELECT COUNT(*) FROM user {where_sql}");
        let (total,): (i64,) = sqlx::query_as(&count_sql)
            .bind(&like)
            .bind(&like)
            .bind(&like)
            .bind(&like)
            .bind(&like)
            .fetch_one(&self.pool)
            .await
            .map_err(map_err)?;

        let list_sql = format!("SELECT * FROM user {where_sql} ORDER BY user_id DESC LIMIT ? OFFSET ?");
        let rows = sqlx::query_as::<_, User>(&list_sql)
            .bind(&like)
            .bind(&like)
            .bind(&like)
            .bind(&like)
            .bind(&like)
            .bind(limit)
            .bind(offset)
            .fetch_all(&self.pool)
            .await
            .map_err(map_err)?;

        Ok((total.max(0) as u64, rows))
    }
}

fn map_err(e: sqlx::Error) -> Error {
    match e {
        sqlx::Error::RowNotFound => Error::NotFound("用户不存在".into()),
        other => Error::Database(other.to_string()),
    }
}