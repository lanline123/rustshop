//! 商品查询与展示 DTO，对应原 Java `dto/product`、`query/product`、`vo/product`。

use rust_decimal::Decimal;
use serde::{Deserialize, Serialize};

/// 商品分页查询参数，对应原 Java `ProductPageQuery`。
#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase", default)]
pub struct ProductListQuery {
    /// 关键词（商品名/编号）
    pub keyword: Option<String>,
    pub category_id: Option<i32>,
    pub brand_id: Option<i32>,
    pub shop_id: Option<i32>,
    pub product_status: Option<i32>,
    /// 价格区间
    pub price_min: Option<Decimal>,
    pub price_max: Option<Decimal>,
    /// 排序字段：sales-销量 price-价格 add_time-上架时间
    pub sort_by: Option<String>,
    pub sort_order: Option<String>,
    pub page: Option<u64>,
    pub page_size: Option<u64>,
}

/// 商品列表 VO（含原 Java `@TableField(exist = false)` 的门店扩展字段）。
#[derive(Debug, Clone, Default, Serialize, Deserialize)]
#[serde(rename_all = "camelCase", default)]
pub struct ProductVO {
    pub product_id: i32,
    pub product_name: String,
    pub product_sn: String,
    pub product_price: Decimal,
    pub market_price: Decimal,
    pub product_stock: i32,
    pub product_status: i32,
    pub category_id: i32,
    pub brand_id: i32,
    pub shop_id: i32,
    pub pic_url: String,
    pub pic_thumb: String,
    pub sales: i32,
    /// 上架时间（库中为 int unsigned 秒级时间戳）
    pub add_time: i32,
    // —— 原 Java exist=false 的门店扩展字段 ——
    pub store_product_name: Option<String>,
    pub store_product_price: Option<Decimal>,
    pub store_pic_url: Option<String>,
    pub store_product_status: Option<i32>,
    pub store_product_stock: Option<i32>,
}