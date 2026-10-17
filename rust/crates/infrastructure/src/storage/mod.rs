//! 对象存储抽象，替代原 Java 的 Aliyun OSS / Tencent COS / AWS S3 SDK。
//!
//! 统一 `Storage` trait，按配置选择 local / oss / cos / s3 实现。

use tigshop_core::{Error, Result};

/// 存储后端抽象。
#[allow(async_fn_in_trait)] // 仅在本工作区内使用，不作为公开异步对象安全 trait
pub trait Storage: Send + Sync {
    /// 上传字节流，返回访问 URL。
    async fn put(&self, key: &str, bytes: Vec<u8>, content_type: &str) -> Result<String>;

    /// 删除对象。
    async fn delete(&self, key: &str) -> Result<()>;
}

/// 本地磁盘存储（对应原 Spring `file.upload-dir`）。
pub struct LocalStorage {
    pub base_dir: String,
    pub public_base_url: String,
}

impl LocalStorage {
    pub fn new(base_dir: impl Into<String>) -> Self {
        Self {
            base_dir: base_dir.into(),
            public_base_url: "/static".into(),
        }
    }
}

impl Storage for LocalStorage {
    async fn put(&self, key: &str, bytes: Vec<u8>, _content_type: &str) -> Result<String> {
        // 防路径穿越
        if key.contains("..") || key.starts_with('/') || key.contains('\\') {
            return Err(Error::Validation(format!("非法存储路径: {key}")));
        }
        let path = std::path::Path::new(&self.base_dir).join(key);
        if let Some(parent) = path.parent() {
            tokio::fs::create_dir_all(parent)
                .await
                .map_err(|e| Error::Internal(format!("创建目录失败: {e}")))?;
        }
        tokio::fs::write(&path, &bytes)
            .await
            .map_err(|e| Error::Internal(format!("写入文件失败: {e}")))?;
        Ok(format!("{}/{}", self.public_base_url, key))
    }

    async fn delete(&self, key: &str) -> Result<()> {
        if key.contains("..") {
            return Err(Error::Validation(format!("非法存储路径: {key}")));
        }
        let path = std::path::Path::new(&self.base_dir).join(key);
        match tokio::fs::remove_file(&path).await {
            Ok(()) => Ok(()),
            Err(e) if e.kind() == std::io::ErrorKind::NotFound => Ok(()),
            Err(e) => Err(Error::Internal(format!("删除文件失败: {e}"))),
        }
    }
}