//! 推荐 域服务（system::recommend）—— wave9。
//!
//! `self.repos.system.recommend`；对应 Java `ProductService.getProductList`
//! （推荐参数）与 `getProductIds`。
//!
//! ## 偏差
//! Java 推荐策略含个性化/热度窗口/历史行为（`ProductService` 内部），本实现
//! 按「精品优先 → 点击量 → 新品」的静态排序返回；`getProductIds` 返回同口径
//! 商品 ID 逗号串。

use std::sync::Arc;

use tigshop_core::models::system::recommend::{
    GuessLikeQuery, RecommendProductVO, DEFAULT_ID_COUNT,
};
use tigshop_core::types::PageResult;
use tigshop_core::Result;
use tigshop_infrastructure::db::repo::system::recommend::RecommendRepository;
use tigshop_infrastructure::db::Repositories;

/// 推荐 域服务。
pub struct RecommendService {
    repos: Arc<Repositories>,
}

impl RecommendService {
    /// 构造服务。
    #[must_use]
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    /// 本 feature 仓储。
    #[must_use]
    pub fn repo(&self) -> &RecommendRepository {
        &self.repos.system.recommend
    }

    /// 猜你喜欢（Java `RecommendController.getProductList`）。
    pub async fn guess_like(
        &self,
        q: &GuessLikeQuery,
    ) -> Result<PageResult<RecommendProductVO>> {
        let page = q.page_query();
        let (total, rows) = self.repo().guess_like(q).await?;
        if rows.is_empty() {
            return Ok(PageResult::empty(&page));
        }
        let list = rows
            .into_iter()
            .map(|r| RecommendProductVO {
                product_id: r.product_id,
                product_name: r.product_name,
                product_sn: r.product_sn,
                product_price: r.product_price,
                pic_url: r.pic_url,
                pic_thumb: r.pic_thumb,
                shop_id: r.shop_id,
                product_type: r.product_type,
                virtual_sales: r.virtual_sales.unwrap_or(0),
            })
            .collect();
        Ok(PageResult::new(total, list, &page))
    }

    /// 推荐商品 ID 串（Java `RecommendController.getProductIds`，逗号分隔）。
    pub async fn product_ids(&self) -> Result<String> {
        let ids = self.repo().recommend_ids(DEFAULT_ID_COUNT).await?;
        Ok(ids
            .iter()
            .map(i32::to_string)
            .collect::<Vec<_>>()
            .join(","))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn ids_join_is_comma_separated() {
        let ids: Vec<i32> = vec![3, 1, 2];
        let s = ids.iter().map(i32::to_string).collect::<Vec<_>>().join(",");
        assert_eq!(s, "3,1,2");
    }

    #[test]
    fn empty_ids_yield_empty_string() {
        let ids: Vec<i32> = Vec::new();
        let s = ids.iter().map(i32::to_string).collect::<Vec<_>>().join(",");
        assert_eq!(s, "");
    }

    #[test]
    fn default_count_positive() {
        assert!(DEFAULT_ID_COUNT > 0);
    }
}
