//! 装修模板域服务（content::decorate_template）。
//!
//! category/list/detail 均依赖未迁移的装修模板资源包与渲染引擎 →
//! 保留端点与本地校验（detail 的 id 必填），统一返回明确「未迁移」业务错误。

use std::sync::Arc;

use tigshop_core::models::content::decorate_template::{
    page_params, validate_detail_id, DecorateTemplateCategoryVO, DecorateTemplateDetailQuery,
    DecorateTemplateListQuery, DecorateTemplateVO, MSG_NOT_MIGRATED,
};
use tigshop_core::types::PageResult;
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::content::decorate_template::DecorateTemplateRepository;
use tigshop_infrastructure::db::Repositories;

/// 装修模板服务。
pub struct DecorateTemplateService {
    repos: Arc<Repositories>,
}

impl DecorateTemplateService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本域仓储（NESTED：`self.repos.content.decorate_template`）。
    #[must_use]
    pub fn repo(&self) -> &DecorateTemplateRepository {
        &self.repos.content.decorate_template
    }

    /// 模板分类（未迁移）。
    pub async fn category(&self) -> Result<Vec<DecorateTemplateCategoryVO>> {
        Err(Error::biz_msg(MSG_NOT_MIGRATED))
    }

    /// 模板列表（未迁移）。
    pub async fn list(
        &self,
        q: &DecorateTemplateListQuery,
    ) -> Result<PageResult<DecorateTemplateVO>> {
        let _ = page_params(q.page, q.size);
        Err(Error::biz_msg(MSG_NOT_MIGRATED))
    }

    /// 模板详情（未迁移；id 必填）。
    pub async fn detail(
        &self,
        q: &DecorateTemplateDetailQuery,
    ) -> Result<Option<DecorateTemplateVO>> {
        validate_detail_id(q.id).map_err(Error::Validation)?;
        Err(Error::biz_msg(MSG_NOT_MIGRATED))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn detail_validates_id() {
        assert!(validate_detail_id(None).is_err());
    }

    #[test]
    fn message_explicit() {
        assert!(MSG_NOT_MIGRATED.contains("尚未迁移"));
    }
}
