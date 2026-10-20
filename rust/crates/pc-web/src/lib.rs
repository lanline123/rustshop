use axum::{
    http::Uri,
    response::{Html, IntoResponse},
    Router,
};
use leptos_router::location::RequestUrl;
use tower_http::services::ServeDir;

pub mod app;
pub mod components;
pub mod pages;
pub mod state;

pub use app::App;

pub async fn render_app(uri: Uri) -> impl IntoResponse {
    use leptos::prelude::*;
    let path = uri
        .path_and_query()
        .map(|pq| pq.as_str())
        .unwrap_or("/")
        .to_string();
    let owner = Owner::new();
    let html = owner.with(|| {
        provide_context(RequestUrl::new(&path));
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
        assert!(html.contains("正品保证"), "Should contain service guarantee");
    }
}
