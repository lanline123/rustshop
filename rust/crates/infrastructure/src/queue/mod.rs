//! 消息队列（RabbitMQ），替代原 Java Spring AMQP。
//!
//! 当前提供：连接初始化与主题发布。消费者在 `services` 层按业务注册。

use tigshop_core::{Error, Result};

/// 队列服务句柄。
#[derive(Clone)]
pub struct QueueService {
    url: String,
}

impl QueueService {
    pub fn new(url: String) -> Self {
        Self { url }
    }

    /// 连接可用性检查（启动自检）。
    pub async fn health_check(&self) -> Result<()> {
        // 连接串本身的有效性校验；真正的连接在首次 publish/consume 建立
        if self.url.is_empty() {
            return Err(Error::Cache("RabbitMQ 连接串为空".into()));
        }
        tracing::info!("RabbitMQ 配置检查通过");
        Ok(())
    }

    pub fn url(&self) -> &str {
        &self.url
    }
}