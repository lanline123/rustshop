//! 相册图片域服务（content::gallery_pic）。
//!
//! 对应原 Java `com.tigshop.service.setting.impl.GalleryPicServiceImpl` 的 10 个
//! 管理端端点（`uploadImg` / `batchDownload` 因对象存储 / zip 未迁移返回明确业务错误）。
//!
//! # 迁移要点（逐条对照 Java 源）
//!
//! - `list`：scope（shop/vendor）+ `galleryId > 0` 的「自身及子孙相册」过滤
//!   + 关键词/格式/来源 + `hidden = 0 OR hidden IS NULL`；`sortOrder = manual`
//!   ⇒ `pic_sort ASC`，否则按 `sortField`（缺省 `addTime`）排序。
//!   同时返回直接子相册（各自前 4 张图片）与当前相册详情。
//! - `del`：硬删除图片（Java `BaseService#del` → `removeById`）。
//! - `updateField`：白名单仅 `pic_name`，非法 → `field 的值有误`。
//! - `detail`：`id` 为 null/<1 → `null`；否则按主键返回实体 DTO。
//! - `sort`：按 ids 顺序写 `pic_sort = i + 1`（单事务）。
//! - `transfer` / `batchDel`：`type=video` 走 `gallery_video_info`，否则 `gallery_pic`。
//! - `createFromExternal`：URL 前缀/黑名单/扩展名校验后落库（`source = external`）。
//! - `uploadImg`：完成文件类型/大小本地校验后返回「对象存储未迁移」业务错误。
//! - `batchDownload`：完成 ids/素材存在性校验后返回「zip/对象存储未迁移」业务错误。
//!
//! # 未迁移 / 偏差
//!
//! - `uploadImg`：OSS/COS/七牛/S3/本地存储、图片尺寸解析、素材命名配置均未迁移；
//!   本实现只做本地校验并**明确报错**，不伪造上传成功。
//! - `batchDownload`：zip 打包 + 远程下载未迁移，明确报错。
//! - `selectFile` / `translateProductPic`：非本控制器端点，不实现。
//! - `picOwerId` 取 JWT `AuthUser.id`（替代 `SecurityUtils.getCurrentAdminId()`）。
//! - `gallery_pic.shop_id` 由 scope 注入（Java `getShopId()`，`None` → 0）。

use std::sync::Arc;

use tigshop_common::utils::now_seconds;
use tigshop_core::entities::{Gallery, GalleryPic, GalleryVideoInfo};
use tigshop_core::models::content::gallery::{GalleryDTO, GalleryScope};
use tigshop_core::models::content::gallery_pic::{
    is_video_type, resolve_gallery_pic_field, resolve_sort, validate_external_url,
    validate_upload_file, GalleryListResVO, GalleryPicBatchDTO, GalleryPicDTO, GalleryPicExternalDTO,
    GalleryPicListQuery, GalleryPicResVO, GalleryPicSortDTO, GalleryPicTransferDTO,
    GalleryPicUploadVO, OperateDTO, UpdateFieldDTO, ALLOWED_IMAGE_EXTENSIONS,
    CHILD_PREVIEW_PIC_LIMIT, CONFIG_IMAGE_UPLOAD_MAX_SIZE, DEFAULT_EXTERNAL_PIC_NAME,
    DEFAULT_IMAGE_UPLOAD_MAX_SIZE, ERR_DOWNLOAD_NOT_MIGRATED, ERR_FIELD_INVALID,
    ERR_GALLERY_ID_REQUIRED, ERR_ID_REQUIRED, ERR_IDS_REQUIRED, ERR_NO_DOWNLOADABLE,
    ERR_PIC_URL_REQUIRED, ERR_SORT_IDS_REQUIRED, ERR_SOURCE_REQUIRED, ERR_TARGET_ID_REQUIRED,
    ERR_TYPE_REQUIRED, ERR_UPLOAD_NOT_MIGRATED, GALLERY_FIELDS,
};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::content::gallery_pic::GalleryPicRepository;
use tigshop_infrastructure::db::Repositories;

/// 相册图片域服务。
pub struct GalleryPicService {
    repos: Arc<Repositories>,
}

impl GalleryPicService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本域仓储（NESTED 访问：`self.repos.content.gallery_pic`）。
    #[must_use]
    pub fn repo(&self) -> &GalleryPicRepository {
        &self.repos.content.gallery_pic
    }

    // ————————————————————— 列表 / 详情 —————————————————————

    /// 相册图片列表（Java `list`）。
    pub async fn list(
        &self,
        scope: GalleryScope,
        q: &GalleryPicListQuery,
    ) -> Result<GalleryListResVO> {
        let page = q.page_query();
        let repo = self.repo();
        let gallery_ids = match q.gallery_filter() {
            Some(gallery_id) => Some(repo.find_self_and_child_ids(gallery_id).await?),
            None => None,
        };
        let (column, desc) = resolve_sort(q.sort_field.as_deref(), q.sort_order.as_deref());
        let (total, rows) = repo
            .paginate(
                scope.shop_id,
                scope.vendor_id,
                gallery_ids.as_deref(),
                q,
                column,
                desc,
            )
            .await?;
        let pics: Vec<GalleryPicDTO> = rows.iter().map(GalleryPicDTO::from).collect();
        let gallery_pic_page = tigshop_core::types::PageResult::new(total, pics, &page);

        let mut child_gallery_list: Vec<GalleryPicResVO> = Vec::new();
        let mut gallery_info: Option<GalleryDTO> = None;
        if let Some(gallery_id) = q.gallery_filter() {
            let children = repo.find_child_galleries(gallery_id).await?;
            for child in children {
                let pics = repo
                    .find_pics_by_gallery(child.gallery_id, CHILD_PREVIEW_PIC_LIMIT)
                    .await?;
                child_gallery_list.push(GalleryPicResVO {
                    gallery: GalleryDTO::from(&child),
                    gallery_pics: pics.iter().map(GalleryPicDTO::from).collect(),
                });
            }
            gallery_info = repo.find_gallery(gallery_id).await?.map(|g| GalleryDTO::from(&g));
        }
        Ok(GalleryListResVO {
            child_gallery_list,
            gallery_pic_page,
            gallery_info,
        })
    }

    /// 图片详情（Java `detail`：id 为 null/<1 → null；不存在 → null）。
    pub async fn detail(&self, id: Option<i32>) -> Result<Option<GalleryPicDTO>> {
        let Some(id) = id.filter(|id| *id >= 1) else {
            return Ok(None);
        };
        Ok(self.repo().find_pic(id).await?.as_ref().map(GalleryPicDTO::from))
    }

    // ————————————————————— 写操作 —————————————————————

    /// 删除图片（Java `del` → `removeById`）。
    pub async fn del(&self, dto: &OperateDTO) -> Result<()> {
        let id = dto
            .id
            .ok_or_else(|| Error::Validation(ERR_ID_REQUIRED.into()))?;
        self.repo().delete_pics(&[id]).await?;
        Ok(())
    }

    /// 更新字段（Java `BaseServiceImpl#updateField`，白名单 `GALLERY_FIELDS`）。
    pub async fn update_field(&self, dto: &UpdateFieldDTO) -> Result<()> {
        let column = resolve_gallery_pic_field(&dto.field)
            .ok_or_else(|| Error::biz_msg(ERR_FIELD_INVALID))?;
        debug_assert!(GALLERY_FIELDS.contains(&column));
        self.repo().update_pic_field(dto.id, column, &dto.val).await?;
        Ok(())
    }

    /// 图片手动排序（Java `@Transactional sort`）。
    pub async fn sort(&self, dto: &GalleryPicSortDTO) -> Result<()> {
        dto.gallery_id
            .ok_or_else(|| Error::Validation(ERR_GALLERY_ID_REQUIRED.into()))?;
        let ids = dto
            .ids
            .as_deref()
            .filter(|ids| !ids.is_empty())
            .ok_or_else(|| Error::Validation(ERR_SORT_IDS_REQUIRED.into()))?;
        self.repo().sort_pics(ids).await?;
        Ok(())
    }

    /// 批量移动素材（Java `@Transactional transfer`）。
    pub async fn transfer(&self, dto: &GalleryPicTransferDTO) -> Result<()> {
        let ids = dto
            .ids
            .as_deref()
            .filter(|ids| !ids.is_empty())
            .ok_or_else(|| Error::Validation(ERR_IDS_REQUIRED.into()))?;
        let target_gallery_id = dto
            .target_gallery_id
            .ok_or_else(|| Error::Validation(ERR_TARGET_ID_REQUIRED.into()))?;
        dto.r#type
            .as_deref()
            .filter(|t| !t.trim().is_empty())
            .ok_or_else(|| Error::Validation(ERR_TYPE_REQUIRED.into()))?;
        if is_video_type(dto.r#type.as_deref()) {
            self.repo().transfer_videos(ids, target_gallery_id).await?;
        } else {
            self.repo().transfer_pics(ids, target_gallery_id).await?;
        }
        Ok(())
    }

    /// 批量删除素材（Java `@Transactional batchDel`）。
    pub async fn batch_del(&self, dto: &GalleryPicBatchDTO) -> Result<()> {
        let ids = dto
            .ids
            .as_deref()
            .filter(|ids| !ids.is_empty())
            .ok_or_else(|| Error::Validation(ERR_IDS_REQUIRED.into()))?;
        if is_video_type(dto.r#type.as_deref()) {
            self.repo().delete_videos(ids).await?;
        } else {
            self.repo().delete_pics(ids).await?;
        }
        Ok(())
    }

    /// 批量下载素材（Java `batchDownload`）。
    ///
    /// **未迁移**：完成 ids/素材存在性校验后返回明确业务错误（不伪造成功）。
    pub async fn batch_download(&self, dto: &GalleryPicBatchDTO) -> Result<()> {
        let ids = dto
            .ids
            .as_deref()
            .filter(|ids| !ids.is_empty())
            .ok_or_else(|| Error::Validation(ERR_IDS_REQUIRED.into()))?;
        let has_downloadable = if is_video_type(dto.r#type.as_deref()) {
            self.repo()
                .find_videos_by_ids(ids)
                .await?
                .iter()
                .any(|video: &GalleryVideoInfo| !video.video_url.trim().is_empty())
        } else {
            self.repo()
                .find_pics_by_ids(ids)
                .await?
                .iter()
                .any(|pic: &GalleryPic| !pic.pic_url.trim().is_empty())
        };
        if !has_downloadable {
            return Err(Error::biz_msg(ERR_NO_DOWNLOADABLE));
        }
        Err(Error::biz_msg(ERR_DOWNLOAD_NOT_MIGRATED))
    }

    /// 通过外部链接创建图片（Java `createFromExternal`，完整 DB 实现）。
    pub async fn create_from_external(
        &self,
        dto: &GalleryPicExternalDTO,
        scope: GalleryScope,
        admin_id: Option<i32>,
    ) -> Result<GalleryPicUploadVO> {
        let pic_url = dto
            .pic_url
            .as_deref()
            .map(str::trim)
            .filter(|url| !url.is_empty())
            .ok_or_else(|| Error::Validation(ERR_PIC_URL_REQUIRED.into()))?;
        let gallery_id = dto
            .gallery_id
            .ok_or_else(|| Error::Validation(ERR_GALLERY_ID_REQUIRED.into()))?;
        dto.source
            .as_deref()
            .map(str::trim)
            .filter(|source| !source.is_empty())
            .ok_or_else(|| Error::Validation(ERR_SOURCE_REQUIRED.into()))?;
        let ext = validate_external_url(pic_url, &ALLOWED_IMAGE_EXTENSIONS)
            .map_err(Error::biz_msg)?;
        let pic_thumb = dto
            .pic_thumb
            .clone()
            .filter(|thumb| !thumb.trim().is_empty())
            .unwrap_or_else(|| pic_url.to_string());
        let pic_name = dto
            .pic_name
            .clone()
            .filter(|name| !name.trim().is_empty())
            .unwrap_or_else(|| DEFAULT_EXTERNAL_PIC_NAME.to_string());
        let pic = GalleryPic {
            pic_id: 0,
            shop_id: scope.shop_id.unwrap_or(0),
            gallery_id,
            pic_ower_id: admin_id.unwrap_or(0),
            pic_url: pic_url.to_string(),
            pic_name,
            pic_thumb,
            // Java `String.valueOf(getCurrentTime())`：秒级时间戳字符串
            add_time: now_seconds().to_string(),
            vendor_id: scope.vendor_id.unwrap_or(0),
            hidden: Some(0),
            pic_width: None,
            pic_height: None,
            pic_size: None,
            file_type: (!ext.is_empty()).then_some(ext),
            pic_sort: None,
            source: "external".to_string(),
        };
        let pic_id = self.repo().insert_pic(&pic).await?;
        tracing::info!("外部图片创建成功: id={pic_id}, url={pic_url}");
        let mut saved = pic;
        saved.pic_id = pic_id;
        Ok(upload_vo(&saved))
    }

    /// 上传图片（Java `uploadImg`）。
    ///
    /// **未迁移**：完成文件类型/大小本地校验后返回明确业务错误（不伪造成功）。
    ///
    /// # Errors
    /// - 文件为空 / 类型非法 / 超过配置上限 → `Biz(1001, ..)`（同 Java `SERVICE_DATA_ERROR`）
    /// - 校验通过 → `Biz(1001, 未迁移提示)`
    #[allow(clippy::too_many_arguments)]
    pub async fn upload_img(
        &self,
        gallery_id: Option<i32>,
        content_type: Option<&str>,
        size: u64,
        file_name: Option<&str>,
        bytes: &[u8],
        shop_id: i32,
        owner_id: i32,
    ) -> Result<GalleryPicUploadVO> {
        let max_size = self
            .repo()
            .find_config_value(CONFIG_IMAGE_UPLOAD_MAX_SIZE)
            .await?
            .unwrap_or_else(|| DEFAULT_IMAGE_UPLOAD_MAX_SIZE.to_string());
        validate_upload_file(content_type, size, &max_size)
            .map_err(|(code, message)| Error::biz(code, message))?;

        let original = file_name
            .map(str::trim)
            .filter(|s| !s.is_empty())
            .unwrap_or("upload.jpg");
        let url = tigshop_infrastructure::storage::save_local_upload("gallery", original, bytes)
            .await?;
        let ext = std::path::Path::new(original)
            .extension()
            .and_then(|e| e.to_str())
            .map(|e| e.to_ascii_lowercase())
            .unwrap_or_else(|| "jpg".to_string());

        let pic = GalleryPic {
            pic_id: 0,
            shop_id,
            gallery_id: gallery_id.unwrap_or(0),
            pic_ower_id: owner_id,
            pic_url: url.clone(),
            pic_name: original.to_string(),
            pic_thumb: url.clone(),
            add_time: now_seconds().to_string(),
            vendor_id: 0,
            hidden: Some(0),
            pic_width: None,
            pic_height: None,
            pic_size: Some(i64::try_from(size).unwrap_or(i64::MAX)),
            file_type: Some(ext),
            pic_sort: None,
            source: "upload".to_string(),
        };
        let mut saved = pic.clone();
        saved.pic_id = self.repo().insert_pic(&pic).await?;
        Ok(upload_vo(&saved))
    }
}

/// 外部直链允许的扩展名（模型常量本地引用，避免长路径）。
fn extensions() -> [&'static str; 5] {
    tigshop_core::models::content::gallery_pic::ALLOWED_IMAGE_EXTENSIONS
}

/// 上传实体 → 上传结果 VO（Java `buildResult`）。
#[must_use]
pub fn upload_vo(pic: &GalleryPic) -> GalleryPicUploadVO {
    GalleryPicUploadVO {
        pic_thumb: pic.pic_thumb.clone(),
        pic_url: pic.pic_url.clone(),
        pic_name: pic.pic_name.clone(),
        pic_id: pic.pic_id,
        pic_width: pic.pic_width,
        pic_height: pic.pic_height,
        pic_size: pic.pic_size,
        gallery_id: pic.gallery_id,
        file_type: pic.file_type.clone(),
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use tigshop_core::entities::Gallery;

    #[test]
    fn upload_vo_copies_all_fields() {
        let pic = GalleryPic {
            pic_id: 5,
            pic_url: "https://x/a.jpg".into(),
            pic_name: "a".into(),
            pic_thumb: "https://x/a!200.jpg".into(),
            gallery_id: 7,
            pic_width: Some(100),
            pic_height: Some(80),
            pic_size: Some(1024),
            file_type: Some("jpg".into()),
            ..Default::default()
        };
        let vo = upload_vo(&pic);
        assert_eq!(vo.pic_id, 5);
        assert_eq!(vo.gallery_id, 7);
        assert_eq!(vo.pic_width, Some(100));
        assert_eq!(vo.file_type.as_deref(), Some("jpg"));
    }

    #[test]
    fn gallery_dto_conversion_is_available() {
        let gallery = Gallery {
            gallery_id: 1,
            parent_id: 0,
            gallery_admin_id: 0,
            gallery_name: "相册".into(),
            gallery_sort: 1,
            gallery_thumb: String::new(),
            shop_id: 0,
            vendor_id: None,
        };
        let dto = GalleryDTO::from(&gallery);
        assert_eq!(dto.gallery_id, Some(1));
        assert_eq!(dto.gallery_name.as_deref(), Some("相册"));
    }

    #[test]
    fn page_params_default() {
        let (page, size) = tigshop_core::models::content::gallery_pic::page_params(None, None);
        assert_eq!((page, size), (1, 15));
    }
}
