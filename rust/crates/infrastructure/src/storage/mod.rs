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

/// 上传根目录（`TIG_FILE__UPLOAD_DIR`，缺省 `./uploads`，相对进程工作目录）。
#[must_use]
pub fn upload_root() -> String {
    std::env::var("TIG_FILE__UPLOAD_DIR").unwrap_or_else(|_| "./uploads".to_string())
}

/// 保存上传文件到本地磁盘，返回前端可直接访问的 `/static/...` URL。
///
/// 文件名使用「毫秒时间戳 + 进程内序号 + 原扩展名」，避免中文/路径穿越问题；
/// `kind` 用于分目录（`gallery` / `video` / `config` / `app`）。
pub async fn save_local_upload(kind: &str, original_name: &str, bytes: &[u8]) -> Result<String> {
    use std::sync::atomic::{AtomicU64, Ordering};
    static SEQ: AtomicU64 = AtomicU64::new(0);

    let ext = std::path::Path::new(original_name)
        .extension()
        .and_then(|e| e.to_str())
        .map(|e| e.to_ascii_lowercase())
        .filter(|e| !e.is_empty() && e.len() <= 8 && e.chars().all(|c| c.is_ascii_alphanumeric()))
        .unwrap_or_else(|| "bin".to_string());
    let stamp = std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .map(|d| d.as_millis())
        .unwrap_or(0);
    let seq = SEQ.fetch_add(1, Ordering::Relaxed);
    let safe_kind: String = kind
        .chars()
        .filter(|c| c.is_ascii_alphanumeric() || *c == '_' || *c == '-')
        .collect();
    let key = format!("{safe_kind}/{stamp}_{seq}.{ext}");
    let storage = LocalStorage::new(upload_root());
    storage.put(&key, bytes.to_vec(), "").await
}


/// 同步版本的本地上传（供同步 service / handler 调用，内部 `std::fs`）。
pub fn save_local_upload_blocking(kind: &str, original_name: &str, bytes: &[u8]) -> Result<String> {
    use std::sync::atomic::{AtomicU64, Ordering};
    static SEQ: AtomicU64 = AtomicU64::new(0);

    let ext = std::path::Path::new(original_name)
        .extension()
        .and_then(|e| e.to_str())
        .map(|e| e.to_ascii_lowercase())
        .filter(|e| !e.is_empty() && e.len() <= 8 && e.chars().all(|c| c.is_ascii_alphanumeric()))
        .unwrap_or_else(|| "bin".to_string());
    let stamp = std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .map(|d| d.as_millis())
        .unwrap_or(0);
    let seq = SEQ.fetch_add(1, Ordering::Relaxed);
    let safe_kind: String = kind
        .chars()
        .filter(|c| c.is_ascii_alphanumeric() || *c == '_' || *c == '-')
        .collect();
    let rel = format!("{safe_kind}/{stamp}_{seq}.{ext}");
    let path = std::path::Path::new(&upload_root()).join(&rel);
    if let Some(parent) = path.parent() {
        std::fs::create_dir_all(parent)
            .map_err(|e| Error::Internal(format!("创建目录失败: {e}")))?;
    }
    std::fs::write(&path, bytes).map_err(|e| Error::Internal(format!("写入文件失败: {e}")))?;
    Ok(format!("/static/{rel}"))
}
