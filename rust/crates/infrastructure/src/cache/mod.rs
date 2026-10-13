//! Redis 缓存服务，替代原 Java Redisson / Spring Data Redis。

use redis::aio::ConnectionManager;
use redis::{AsyncCommands, Client};
use tigshop_core::{Error, Result};

/// 异步 Redis 客户端封装（自动重连的 ConnectionManager）。
#[derive(Clone)]
pub struct CacheService {
    conn: ConnectionManager,
}

impl CacheService {
    /// 初始化（替代 Spring RedisTemplate 自动配置）。
    pub async fn init(url: &str) -> Result<Self> {
        let client = Client::open(url).map_err(|e| Error::Cache(format!("Redis URL 无效: {e}")))?;
        let conn = ConnectionManager::new(client)
            .await
            .map_err(|e| Error::Cache(format!("Redis 连接失败: {e}")))?;
        tracing::info!("Redis 缓存初始化完成: {url}");
        Ok(Self { conn })
    }

    /// GET，返回反序列化后的 JSON 值。
    pub async fn get_json<T: serde::de::DeserializeOwned>(&self, key: &str) -> Result<Option<T>> {
        let mut conn = self.conn.clone();
        let raw: Option<String> = conn
            .get(key)
            .await
            .map_err(|e| Error::Cache(e.to_string()))?;
        match raw {
            None => Ok(None),
            Some(s) => serde_json::from_str(&s)
                .map(Some)
                .map_err(|e| Error::Cache(format!("缓存值反序列化失败[{key}]: {e}"))),
        }
    }

    /// SET（带 TTL 秒）。
    pub async fn set_json<T: serde::Serialize>(
        &self,
        key: &str,
        value: &T,
        ttl_secs: u64,
    ) -> Result<()> {
        let payload = serde_json::to_string(value)
            .map_err(|e| Error::Cache(format!("缓存序列化失败: {e}")))?;
        let mut conn = self.conn.clone();
        conn.set_ex::<_, _, ()>(key, payload, ttl_secs)
            .await
            .map_err(|e| Error::Cache(e.to_string()))
    }

    /// 删除 key。
    pub async fn del(&self, key: &str) -> Result<()> {
        let mut conn = self.conn.clone();
        conn.del::<_, ()>(key)
            .await
            .map_err(|e| Error::Cache(e.to_string()))
    }

    /// 通用「读缓存→未命中则回源→写缓存」，替代原 Java 的 CacheUtil 模式。
    pub async fn get_or_load<T, F, Fut>(
        &self,
        key: &str,
        ttl_secs: u64,
        loader: F,
    ) -> Result<T>
    where
        T: serde::Serialize + serde::de::DeserializeOwned,
        F: FnOnce() -> Fut,
        Fut: std::future::Future<Output = Result<T>>,
    {
        if let Some(hit) = self.get_json::<T>(key).await? {
            return Ok(hit);
        }
        let value = loader().await?;
        // 缓存失败不阻断主流程
        if let Err(e) = self.set_json(key, &value, ttl_secs).await {
            tracing::warn!("写入缓存失败[{key}]: {e}");
        }
        Ok(value)
    }

    /// 原子自增（验证码/限购次数等计数场景）。
    pub async fn incr(&self, key: &str, ttl_secs: u64) -> Result<i64> {
        let mut conn = self.conn.clone();
        let n: i64 = conn
            .incr(key, 1i64)
            .await
            .map_err(|e| Error::Cache(e.to_string()))?;
        if n == 1 && ttl_secs > 0 {
            let _: bool = conn
                .expire(key, ttl_secs as i64)
                .await
                .map_err(|e| Error::Cache(e.to_string()))?;
        }
        Ok(n)
    }
}

#[cfg(test)]
mod tests {
    // 需要真实 Redis 的集成测试见 tests/ 目录（使用 testcontainers）。
}
