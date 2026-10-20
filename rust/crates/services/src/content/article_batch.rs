//! 文章批量操作/前台文章域服务（content::article_batch）。
//!
//! - `batch`：仅 `type = del` 批量删除；ids 为空 →「未选择项目」。
//! - `issue_info`：id 有效 → 对应文章；id 缺省 → 最新一篇文章（均可能为 None）。
//! - `index_bzzx_list`：全部分类 + 每类前 5 篇上架文章（`childrenArticleListWithCategory`）。

use std::sync::Arc;

use tigshop_core::models::content::article_batch::{
    is_delete_batch, ArticleIssueQuery, ArticleVO, BatchDTO, CategoryWithArticlesVO,
    CATEGORY_ARTICLES_LIMIT, MSG_UNSELECTED_ITEM,
};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::repo::content::article_batch::ArticleBatchRepository;
use tigshop_infrastructure::db::Repositories;

/// 文章域服务。
pub struct ArticleBatchService {
    repos: Arc<Repositories>,
}

impl ArticleBatchService {
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本域仓储（NESTED：`self.repos.content.article_batch`）。
    #[must_use]
    pub fn repo(&self) -> &ArticleBatchRepository {
        &self.repos.content.article_batch
    }

    /// 批量操作（Java `batch`）。
    pub async fn batch(&self, dto: &BatchDTO) -> Result<()> {
        if dto.ids.is_empty() {
            return Err(Error::Validation(MSG_UNSELECTED_ITEM.into()));
        }
        if !is_delete_batch(&dto.r#type) {
            return Ok(());
        }
        self.repo().delete_articles(&dto.ids).await?;
        Ok(())
    }

    /// 文章信息（Java `issueInfo`）。
    pub async fn issue_info(&self, q: &ArticleIssueQuery) -> Result<Option<ArticleVO>> {
        let repo = self.repo();
        match q.id.filter(|id| *id > 0) {
            Some(id) => repo.find_article(id).await,
            None => repo.find_latest_article().await,
        }
    }

    /// 分类 + 文章列表（Java `childrenArticleListWithCategory`）。
    pub async fn index_bzzx_list(&self) -> Result<Vec<CategoryWithArticlesVO>> {
        self.repo()
            .list_categories_with_articles(CATEGORY_ARTICLES_LIMIT)
            .await
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn batch_requires_ids() {
        let dto = BatchDTO {
            ids: Vec::new(),
            r#type: "del".into(),
        };
        assert!(dto.ids.is_empty());
        assert!(MSG_UNSELECTED_ITEM.contains("未选择"));
    }

    #[test]
    fn limit_constant() {
        assert_eq!(CATEGORY_ARTICLES_LIMIT, 5);
    }
}
