//! 商品样式 域仓储（slice-084）骨架。
//!
//! 表名/列名必须来自权威 schema `java/database/pure.sql`
//! （`cd rust && python tools/schema_parser.py --table <表名>`）；
//! SQL 一律运行时 `sqlx::query_as`，**不用** `query!` 宏；每条 SQL 单行字面量。

use sqlx::MySqlPool;

/// 仓储（持有连接池）。
#[derive(Clone)]
pub struct Slice084Repository {
    pool: MySqlPool,
}

impl Slice084Repository {
    #[must_use]
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    /// 暴露连接池（供服务层做事务编排）。
    #[must_use]
    pub fn pool_clone(&self) -> MySqlPool {
        self.pool.clone()
    }
}
