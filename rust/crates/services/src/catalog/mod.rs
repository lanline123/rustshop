//! catalog 领域服务。

pub mod attributes_tpl;
pub mod brand;
pub mod category;
pub mod category_all;
pub mod comment;
pub mod price_inquiry;
pub mod product;
pub mod product_group;
pub mod product_manage;
pub mod product_stock;
pub mod short_url;
pub mod stock_warning;

use std::sync::Arc;

/// catalog 领域服务集合（由组合根装配一次，api 层只持有 `Arc<Services>`）。
#[derive(Clone)]
pub struct Services {
    pub attributes_tpl: Arc<attributes_tpl::AttributesTplService>,
    pub brand: Arc<brand::BrandService>,
    pub category: Arc<category::CategoryService>,
    pub category_all: Arc<category_all::CatalogService>,
    pub product: Arc<product::ProductService>,
    pub product_manage: Arc<product_manage::ProductManageService>,
    pub product_stock: Arc<product_stock::ProductStockService>,
    pub short_url: Arc<short_url::ShortUrlService>,
    pub stock_warning: Arc<stock_warning::StockWarningService>,
    pub comment: Arc<comment::CommentService>,
    pub price_inquiry: Arc<price_inquiry::PriceInquiryService>,
    pub product_group: Arc<product_group::ProductGroupService>,
}
