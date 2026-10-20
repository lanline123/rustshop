use axum::{
    http::Uri,
    response::{Html, IntoResponse},
    Router,
};
use leptos_router::location::RequestUrl;
use tower_http::services::ServeDir;

pub mod app;
pub mod components;
pub mod loader;
pub mod pages;
pub mod state;

pub use app::App;

fn percent_decode(s: &str) -> String {
    let mut bytes = Vec::new();
    let mut chars = s.bytes();
    while let Some(b) = chars.next() {
        if b == b'%' {
            if let (Some(h1), Some(h2)) = (chars.next(), chars.next()) {
                let hex_str = [h1, h2];
                if let Ok(s_hex) = std::str::from_utf8(&hex_str) {
                    if let Ok(val) = u8::from_str_radix(s_hex, 16) {
                        bytes.push(val);
                        continue;
                    }
                }
            }
        } else if b == b'+' {
            bytes.push(b' ');
            continue;
        }
        bytes.push(b);
    }
    String::from_utf8(bytes).unwrap_or_else(|_| s.to_string())
}

pub async fn render_app(uri: Uri) -> impl IntoResponse {
    use leptos::prelude::*;
    let path = uri
        .path_and_query()
        .map(|pq| pq.as_str())
        .unwrap_or("/")
        .to_string();

    // 路由分流预取真实数据库数据并注入 Leptos 上下文
    let home_data = if path == "/" || path.is_empty() {
        Some(loader::load_home_data().await)
    } else {
        None
    };

    let search_data = if path.starts_with("/search") {
        let query_str = uri.query().unwrap_or("");
        let kw = query_str
            .split('&')
            .find(|pair| pair.starts_with("keyword=") || pair.starts_with("q="))
            .and_then(|pair| pair.split('=').nth(1))
            .map(|s| percent_decode(s));
        Some(loader::load_search_data(kw, 1).await)
    } else {
        None
    };

    let detail_data = if path.starts_with("/product/") {
        let raw_id = path.trim_start_matches("/product/").split('?').next().unwrap_or("");
        let pid = raw_id.parse::<i32>().unwrap_or(0);
        if pid > 0 {
            loader::load_product_detail(pid).await
        } else {
            None
        }
    } else {
        None
    };

    let site_config = loader::load_site_config().await;

    let owner = Owner::new();
    let html = owner.with(|| {
        provide_context(RequestUrl::new(&path));
        provide_context(site_config);
        if let Some(hd) = home_data {
            provide_context(hd);
        }
        if let Some(sd) = search_data {
            provide_context(sd);
        }
        if let Some(dd) = detail_data {
            provide_context(dd);
        }
        view! { <App/> }.to_html()
    });
    Html(html)
}

/// 构建 PC 前端 Axum 路由器，支持 Leptos SSR 服务端直出渲染与静态资源托管
pub fn router() -> Router {
    Router::new()
        // 静态资源托管: /static/*，支持在项目根目录、rust目录或pc-web目录下自适应定位
        .nest_service(
            "/static",
            ServeDir::new("rust/crates/pc-web/public")
                .fallback(ServeDir::new("crates/pc-web/public"))
                .fallback(ServeDir::new("public")),
        )
        // 兼容 /assets/* 静态映射（支持 CSS 内部 url('/assets/...') 引用）
        .nest_service(
            "/assets",
            ServeDir::new("rust/crates/pc-web/public")
                .fallback(ServeDir::new("crates/pc-web/public"))
                .fallback(ServeDir::new("public")),
        )
        // 所有前端页面路由统一走 Leptos SSR 直出渲染
        .fallback(render_app)
}

#[cfg(test)]
mod tests {
    use super::*;
    use leptos::prelude::*;

    #[test]
    fn test_ssr_html_rendering() {
        let owner = Owner::new();
        let html = owner.with(|| {
            provide_context(RequestUrl::new("/"));
            view! { <App/> }.to_html()
        });
        assert!(html.contains("<!DOCTYPE html>"), "Should contain doctype");
        assert!(html.contains("mall.css"), "Should link mall.css stylesheet");
        assert!(html.contains("TigShop"), "Should contain brand name");
        assert!(html.contains("全部商品分类"), "Should contain category menu");
        assert!(html.contains("正品保障"), "Should contain service guarantee");
    }
}
