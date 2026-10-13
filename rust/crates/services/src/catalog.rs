//! 分类/品牌服务。
//! 对应原 Java `CategoryServiceImpl` / `BrandServiceImpl`。
//!
//! TODO(域实现者)：对照 Java 源补全分类树构建（含子孙收集）、
//! 前台可见性过滤、品牌详情。

use std::sync::Arc;

use tigshop_core::entities::Category;
use tigshop_core::models::{BrandVO, CategoryTreeNode};
use tigshop_core::types::{PageQuery, PageResult};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::Repositories;

pub struct CatalogService {
    repos: Arc<Repositories>,
}

impl CatalogService {
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 构建分类树（parent_id = 0 为根）。
    ///
    /// `include_hidden = false` 时仅保留前台显示（`is_show = 1`）的分类，
    /// 对应原 Java 前台 `CategoryServiceImpl` 的可见性过滤。
    pub async fn category_tree(&self, include_hidden: bool) -> Result<Vec<CategoryTreeNode>> {
        let all = self.repos.catalog.find_all_categories().await?;
        let visible: Vec<Category> = all
            .into_iter()
            .filter(|c| include_hidden || c.is_visible())
            .collect();
        Ok(build_tree(&visible, Category::ROOT_PARENT))
    }

    /// 分类详情。
    pub async fn category_detail(&self, category_id: i32) -> Result<Category> {
        self.repos
            .catalog
            .find_category(category_id)
            .await?
            .ok_or_else(|| Error::NotFound("分类不存在".into()))
    }

    /// 品牌分页。
    pub async fn brands(&self, keyword: Option<&str>, page: &PageQuery) -> Result<PageResult<BrandVO>> {
        let (total, rows) = self.repos.catalog.paginate_brands(keyword, page).await?;
        let list: Vec<BrandVO> = rows
            .iter()
            .map(|b| BrandVO {
                brand_id: b.brand_id,
                brand_name: Some(b.brand_name.clone()),
                is_show: Some(b.is_show),
            })
            .collect();
        Ok(PageResult::new(total, list, page))
    }
}

/// 递归构建分类树。
fn build_tree(all: &[Category], parent_id: i32) -> Vec<CategoryTreeNode> {
    all.iter()
        .filter(|c| c.parent_id == parent_id)
        .map(|c| CategoryTreeNode {
            category_id: c.category_id,
            category_name: Some(c.category_name.clone()),
            parent_id: Some(c.parent_id),
            sort: Some(c.sort_order),
            children: build_tree(all, c.category_id),
        })
        .collect()
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn tree_building() {
        let cats = vec![
            Category { category_id: 1, parent_id: 0, category_name: "根".into(), ..Default::default() },
            Category { category_id: 2, parent_id: 1, category_name: "子".into(), ..Default::default() },
        ];
        let tree = build_tree(&cats, Category::ROOT_PARENT);
        assert_eq!(tree.len(), 1);
        assert_eq!(tree[0].children.len(), 1);
    }
}
