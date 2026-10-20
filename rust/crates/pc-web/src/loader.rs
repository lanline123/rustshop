use std::sync::{Arc, OnceLock};
use sqlx::mysql::MySqlPoolOptions;
use tigshop_core::models::ProductListQuery;
use tigshop_infrastructure::db::Repositories;
use serde::{Deserialize, Serialize};

static REPOS: OnceLock<Option<Arc<Repositories>>> = OnceLock::new();

/// 获取基础设施仓储单例（自动连接本地 MySQL 8.4 业务数据库）
pub async fn get_repos() -> Option<Arc<Repositories>> {
    if let Some(cached) = REPOS.get() {
        return cached.clone();
    }

    let url = std::env::var("TIG_DATASOURCE_URL")
        .or_else(|_| std::env::var("DATABASE_URL"))
        .unwrap_or_else(|_| "mysql://root:root@127.0.0.1:3306/tigshop-java".to_string());

    let pool_res = MySqlPoolOptions::new()
        .max_connections(10)
        .acquire_timeout(std::time::Duration::from_secs(3))
        .connect(&url)
        .await;

    let res = match pool_res {
        Ok(pool) => {
            tracing::info!("✅ pc-web 成功直连 Rust MySQL 数据库: {}", url);
            Some(Arc::new(Repositories::new(pool)))
        }
        Err(e) => {
            tracing::warn!("⚠️ pc-web 直连 MySQL 数据库失败 ({url}): {e}，将使用演示数据保护");
            None
        }
    };

    let _ = REPOS.set(res.clone());
    res
}

// ---------------- 站点全局配置模型（主题配色/Logo/导航） ----------------
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct NavItemVO {
    pub title: String,
    pub link: String,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct SiteConfig {
    pub theme_css: String,
    pub logo: String,
    pub shop_title: String,
    pub navigations: Vec<NavItemVO>,
}

pub fn default_theme_css() -> String {
    r#"
                        --general: #ff4444;
                        --main-bg: #ff4444;
                        --btn-hover-bg: #ff6262;
                        --main-bg-gradient: #ff4444;
                        --main-text: #ffffff;
                        --vice-bg: #ff8855;
                        --vice-text: #ffffff;
                        --icon: #fc0000;
                        --price: #fc0000;
                        --tag-text: #cf0000;
                        --tag-bg: #fff2f2;
                        --pc-home-max-width: 1190px;
                        --pc-home-min-width: 1190px;
                        --pc-home-padding-x: 0px;
    "#.trim().to_string()
}

impl Default for SiteConfig {
    fn default() -> Self {
        Self {
            theme_css: default_theme_css(),
            logo: "/static/images/logo_blue.png".to_string(),
            shop_title: "TigShop".to_string(),
            navigations: vec![
                NavItemVO { title: "首页".into(), link: "/".into() },
                NavItemVO { title: "限时秒杀".into(), link: "/search".into() },
                NavItemVO { title: "新品上市".into(), link: "/search".into() },
                NavItemVO { title: "品牌特卖".into(), link: "/search".into() },
                NavItemVO { title: "领券中心".into(), link: "/search".into() },
                NavItemVO { title: "企业采购".into(), link: "/search".into() },
            ],
        }
    }
}

// ---------------- 首页数据模型 ----------------
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct CategoryItemVO {
    pub name: String,
    pub sub: String,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct ProductCardVO {
    pub id: i32,
    pub name: String,
    pub price: String,
    pub market_price: String,
    pub pic_thumb: String,
    pub sales: i32,
    pub tags: Vec<String>,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct HomeData {
    pub categories: Vec<CategoryItemVO>,
    pub recommend_products: Vec<ProductCardVO>,
    pub seckill_products: Vec<ProductCardVO>,
}

// ---------------- 搜索页数据模型 ----------------
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct SearchData {
    pub total: u64,
    pub keyword: String,
    pub page: u64,
    pub products: Vec<ProductCardVO>,
}

// ---------------- 详情页数据模型 ----------------
#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct SkuVO {
    pub sku_id: i32,
    pub sku_value: String,
    pub sku_price: String,
    pub sku_stock: i32,
}

#[derive(Clone, Debug, Serialize, Deserialize)]
pub struct DetailData {
    pub id: i32,
    pub name: String,
    pub sn: String,
    pub price: String,
    pub market_price: String,
    pub stock: i32,
    pub pic_url: String,
    pub pic_thumb: String,
    pub sales: i32,
    pub desc: String,
    pub skus: Vec<SkuVO>,
}

fn normalize_pic(pic: &str) -> String {
    if pic.is_empty() || pic == "0" {
        "/static/images/goods_empty.png".to_string()
    } else if pic.starts_with("http://") || pic.starts_with("https://") {
        pic.to_string()
    } else {
        format!("/{}", pic.trim_start_matches('/'))
    }
}

// ---------------- 数据加载工厂 ----------------
pub async fn load_site_config() -> SiteConfig {
    let mut config = SiteConfig::default();
    let repos_opt = get_repos().await;
    if let Some(repos) = repos_opt {
        // 1. 读取主题配色与 Logo
        if let Ok(map) = repos.content.home.config_vals(&["themeStyle", "shopLogo", "shopTitle"]).await {
            if let Some(Some(raw_theme)) = map.get("themeStyle") {
                if let Ok(val) = serde_json::from_str::<serde_json::Value>(raw_theme) {
                    if let Some(obj) = val.as_object() {
                        let mut css_rules = Vec::new();
                        for (k, v) in obj {
                            if k.starts_with("--") {
                                if let Some(c) = v.as_str() {
                                    if !c.is_empty() {
                                        css_rules.push(format!("{}: {};", k, c));
                                    }
                                }
                            }
                        }
                        if !css_rules.is_empty() {
                            css_rules.push("--pc-home-max-width: 1190px;".to_string());
                            css_rules.push("--pc-home-min-width: 1190px;".to_string());
                            css_rules.push("--pc-home-padding-x: 0px;".to_string());
                            config.theme_css = css_rules.join("\n                        ");
                        }
                    }
                }
            }

            if let Some(Some(logo)) = map.get("shopLogo") {
                if !logo.is_empty() {
                    config.logo = logo.clone();
                }
            }

            if let Some(Some(title)) = map.get("shopTitle") {
                if !title.is_empty() {
                    config.shop_title = title.clone();
                }
            }
        }

        // 2. 读取后端装修的 PC 导航条 (pc_navigation)
        if let Ok(rows) = sqlx::query_as::<_, (String, String)>(
            "SELECT title, link FROM pc_navigation WHERE is_show = 1 ORDER BY sort_order ASC LIMIT 8"
        )
        .fetch_all(&repos.pool)
        .await
        {
            if !rows.is_empty() {
                let mut navs = Vec::new();
                for (t, raw_l) in rows {
                    let mut href = "/search".to_string();
                    if let Ok(j) = serde_json::from_str::<serde_json::Value>(&raw_l) {
                        if let Some(l) = j.get("link").and_then(|v| v.as_str()) {
                            href = if l.starts_with('/') { l.to_string() } else { format!("/{}", l) };
                        }
                    } else if raw_l.starts_with('/') {
                        href = raw_l;
                    }
                    navs.push(NavItemVO { title: t, link: href });
                }
                if !navs.is_empty() {
                    config.navigations = navs;
                }
            }
        }
    }
    config
}

pub async fn load_home_data() -> HomeData {
    let repos_opt = get_repos().await;
    if let Some(repos) = repos_opt {
        // 1. 从 MySQL category 表读取真实一级分类及子分类
        let mut categories = Vec::new();
        if let Ok(cats) = repos.catalog.category.find_categories(true).await {
            let top_cats: Vec<_> = cats.iter().filter(|c| c.parent_id == 0).take(10).collect();
            for top in top_cats {
                let subs: Vec<String> = cats.iter()
                    .filter(|c| c.parent_id == top.category_id)
                    .take(3)
                    .map(|c| c.category_name.clone())
                    .collect();
                let sub_str = if subs.is_empty() {
                    top.category_name.clone()
                } else {
                    subs.join(" / ")
                };
                categories.push(CategoryItemVO {
                    name: top.category_name.clone(),
                    sub: sub_str,
                });
            }
        }

        // 2. 从 MySQL product 表读取精选推荐商品列表
        let query = ProductListQuery {
            product_status: Some(1),
            page: Some(1),
            page_size: Some(12),
            sort_by: Some("sales".into()),
            sort_order: Some("desc".into()),
            ..Default::default()
        };
        let mut recommend_products = Vec::new();
        if let Ok((_total, prods)) = repos.catalog.product.paginate(&query).await {
            for p in prods {
                recommend_products.push(ProductCardVO {
                    id: p.product_id,
                    name: p.product_name,
                    price: p.product_price.to_string(),
                    market_price: p.market_price.to_string(),
                    pic_thumb: normalize_pic(&p.pic_thumb),
                    sales: p.virtual_sales,
                    tags: vec!["自营".into(), "正品".into()],
                });
            }
        }

        // 3. 从 MySQL product 表读取秒杀商品列表（价格最低/促销商品）
        let seckill_query = ProductListQuery {
            product_status: Some(1),
            page: Some(1),
            page_size: Some(5),
            sort_by: Some("product_price".into()),
            sort_order: Some("asc".into()),
            ..Default::default()
        };
        let mut seckill_products = Vec::new();
        if let Ok((_total, prods)) = repos.catalog.product.paginate(&seckill_query).await {
            for p in prods {
                seckill_products.push(ProductCardVO {
                    id: p.product_id,
                    name: p.product_name,
                    price: p.product_price.to_string(),
                    market_price: p.market_price.to_string(),
                    pic_thumb: normalize_pic(&p.pic_thumb),
                    sales: p.virtual_sales,
                    tags: vec!["限时秒杀".into()],
                });
            }
        }

        if !categories.is_empty() && !recommend_products.is_empty() {
            return HomeData {
                categories,
                recommend_products,
                seckill_products,
            };
        }
    }

    fallback_home_data()
}

pub async fn load_search_data(keyword: Option<String>, page: u64) -> SearchData {
    let kw_str = keyword.unwrap_or_default();
    let repos_opt = get_repos().await;
    if let Some(repos) = repos_opt {
        let query = ProductListQuery {
            keyword: if kw_str.is_empty() { None } else { Some(kw_str.clone()) },
            product_status: Some(1),
            page: Some(page),
            page_size: Some(16),
            ..Default::default()
        };
        if let Ok((total, prods)) = repos.catalog.product.paginate(&query).await {
            let products = prods.into_iter().map(|p| ProductCardVO {
                id: p.product_id,
                name: p.product_name,
                price: p.product_price.to_string(),
                market_price: p.market_price.to_string(),
                pic_thumb: normalize_pic(&p.pic_thumb),
                sales: p.virtual_sales,
                tags: vec!["自营".into()],
            }).collect();

            return SearchData {
                total,
                keyword: kw_str,
                page,
                products,
            };
        }
    }

    SearchData {
        total: 10,
        keyword: kw_str,
        page: 1,
        products: fallback_products(),
    }
}

pub async fn load_product_detail(product_id: i32) -> Option<DetailData> {
    let repos_opt = get_repos().await;
    if let Some(repos) = repos_opt {
        if let Ok(Some(p)) = repos.catalog.product.find_by_id(product_id).await {
            let skus_entities = repos.catalog.product.find_skus(product_id).await.unwrap_or_default();
            let skus = skus_entities.into_iter().map(|s| SkuVO {
                sku_id: s.sku_id,
                sku_value: s.sku_value,
                sku_price: s.sku_price.map(|v| v.to_string()).unwrap_or_else(|| p.product_price.to_string()),
                sku_stock: s.sku_stock.unwrap_or(0),
            }).collect();

            let pic_main = normalize_pic(&p.pic_url);
            let pic_thumb = normalize_pic(&p.pic_thumb);

            return Some(DetailData {
                id: p.product_id,
                name: p.product_name,
                sn: p.product_sn,
                price: p.product_price.to_string(),
                market_price: p.market_price.to_string(),
                stock: p.product_stock,
                pic_url: pic_main,
                pic_thumb,
                sales: p.virtual_sales,
                desc: p.product_desc.unwrap_or_else(|| "正品行货，品质严选，假一赔十。".to_string()),
                skus,
            });
        }
    }
    None
}

// ---------------- 备选回退数据（DB 未就绪时保底） ----------------
pub fn fallback_home_data() -> HomeData {
    HomeData {
        categories: vec![
            CategoryItemVO { name: "手机数码".into(), sub: "手机 / 数码 / 摄影".into() },
            CategoryItemVO { name: "电脑办公".into(), sub: "笔记本 / 平板 / 台式机".into() },
            CategoryItemVO { name: "家用电器".into(), sub: "电视 / 空调 / 洗衣机".into() },
            CategoryItemVO { name: "服饰鞋包".into(), sub: "男装 / 女装 / 箱包".into() },
            CategoryItemVO { name: "美妆个护".into(), sub: "护肤 / 彩妆 / 香水".into() },
            CategoryItemVO { name: "食品生鲜".into(), sub: "生鲜 / 零食 / 茗茶".into() },
            CategoryItemVO { name: "母婴玩具".into(), sub: "奶粉 / 纸尿裤 / 玩具".into() },
            CategoryItemVO { name: "家居生活".into(), sub: "家纺 / 厨具 / 家具".into() },
            CategoryItemVO { name: "运动户外".into(), sub: "运动鞋 / 健身器材".into() },
            CategoryItemVO { name: "企业采购".into(), sub: "礼品 / 办公耗材 / 定制".into() },
        ],
        recommend_products: fallback_products(),
        seckill_products: fallback_products().into_iter().take(5).collect(),
    }
}

fn fallback_products() -> Vec<ProductCardVO> {
    vec![
        ProductCardVO {
            id: 1001,
            name: "Apple iPhone 16 Pro Max 256GB 原色钛金属 5G手机".into(),
            price: "8999.00".into(),
            market_price: "9999.00".into(),
            pic_thumb: "/static/images/goods_empty.png".into(),
            sales: 2850,
            tags: vec!["自营".into(), "特惠".into()],
        },
        ProductCardVO {
            id: 1002,
            name: "联想 ThinkPad X1 Carbon 14英寸英特尔酷睿Ultra7 商务笔记本".into(),
            price: "11999.00".into(),
            market_price: "13999.00".into(),
            pic_thumb: "/static/images/goods_empty.png".into(),
            sales: 1420,
            tags: vec!["企业采购".into()],
        },
        ProductCardVO {
            id: 1003,
            name: "华为 Mate 60 Pro 12GB+512GB 雅丹黑 全网通 卫星通话".into(),
            price: "6999.00".into(),
            market_price: "6999.00".into(),
            pic_thumb: "/static/images/goods_empty.png".into(),
            sales: 5630,
            tags: vec!["热销".into()],
        },
    ]
}

#[cfg(test)]
mod tests {
    use super::*;

    #[tokio::test]
    async fn test_db_loading() {
        let repos = get_repos().await;
        println!("REPOS: {:?}", repos.is_some());
        if let Some(r) = repos {
            let decorate_svc = tigshop_services::content::decorate::DecorateService::new(r.clone());
            match decorate_svc.pc_index(&tigshop_core::models::content::decorate::HomeDecorateQuery::default()).await {
                Ok(vo) => {
                    println!("PC Index Decorate ID: {:?}", vo.decorate_id);
                    if let Some(list) = vo.module_list {
                        println!("Module count: {}", list.len());
                        for (idx, m) in list.iter().enumerate() {
                            let t = m.get("type").and_then(|v| v.as_str()).unwrap_or("unknown");
                            let show = m.get("isShow").and_then(|v| v.as_bool()).unwrap_or(true);
                            println!("  #{}: type={}, isShow={}", idx, t, show);
                        }
                    }
                }
                Err(e) => println!("Decorate error: {:?}", e),
            }
        }
    }
}

