//! 微信直播商品库域仓储（content::wechat_live_goods）。
//!
//! 微信侧商品库 API 未迁移；本地 `wechat_live_goods` 缓存表未同步，故本仓储
//! 仅提供缓存行存在性检查（不返回数据），避免编造商品信息。

use sqlx::MySqlPool;

use tigshop_core::{Error, Result};

/// 微信直播商品库仓储（缓存表只读）。
#[derive(Clone)]
pub struct WechatLiveGoodsRepository {
    pool: MySqlPool,
}

impl WechatLiveGoodsRepository {
    #[must_use]
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    /// 暴露连接池。
    #[must_use]
    pub fn pool_clone(&self) -> MySqlPool {
        self.pool.clone()
    }

    /// 缓存行数量（仅诊断用；商品库数据以微信侧为准）。
    pub async fn count_cached(&self) -> Result<i64> {
        let row: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM wechat_live_goods")
            .fetch_one(&self.pool)
            .await
            .map_err(map_err)?;
        Ok(row.0)
    }
}

fn map_err(e: sqlx::Error) -> Error {
    Error::Database(e.to_string())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn repository_type_exists() {
        assert!(std::any::type_name::<WechatLiveGoodsRepository>().contains("WechatLiveGoods"));
    }
}
