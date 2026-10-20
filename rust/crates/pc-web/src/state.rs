use serde::{Deserialize, Serialize};

#[derive(Clone, Debug, Serialize, Deserialize, Default)]
pub struct UserState {
    pub is_logged_in: bool,
    pub user_id: i64,
    pub username: String,
    pub nickname: String,
    pub balance: String,
    pub points: i64,
    pub cart_count: usize,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct CategoryItem {
    pub id: i64,
    pub name: String,
    pub sub_categories: Vec<String>,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct ProductItem {
    pub id: i64,
    pub name: String,
    pub price: String,
    pub market_price: String,
    pub pic_thumb: String,
    pub sales: i64,
    pub tags: Vec<String>,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct CartItem {
    pub cart_id: i64,
    pub product_id: i64,
    pub name: String,
    pub sku_name: String,
    pub price: String,
    pub quantity: i32,
    pub pic_thumb: String,
    pub is_checked: bool,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct OrderItem {
    pub order_id: i64,
    pub order_sn: String,
    pub total_amount: String,
    pub order_status: i32,
    pub status_text: String,
    pub create_time: String,
    pub product_name: String,
    pub pic_thumb: String,
    pub quantity: i32,
}
