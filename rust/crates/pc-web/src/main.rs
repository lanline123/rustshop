//! TigShop PC 商城前端主程序入口（Rust Leptos SSR + Axum + MySQL）

use tigshop_pc_web::router;

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    tracing_subscriber::fmt::init();

    let app = router();
    let port = std::env::var("PORT").unwrap_or_else(|_| "8181".to_string());
    let addr = format!("0.0.0.0:{port}");

    let listener = tokio::net::TcpListener::bind(&addr).await?;
    println!("\n=======================================================");
    println!("🚀 TigShop PC 商城 (Rust 全栈 Leptos SSR + MySQL) 服务已启动！");
    println!("👉 浏览器直接访问: http://localhost:{port}/");
    println!("👉 核心页面直达清单:");
    println!("   - 首页:     http://localhost:{port}/");
    println!("   - 商品检索: http://localhost:{port}/search");
    println!("   - 商品详情: http://localhost:{port}/product/4");
    println!("   - 购物车:   http://localhost:{port}/cart");
    println!("   - 结算中心: http://localhost:{port}/checkout");
    println!("   - 账号登录: http://localhost:{port}/login");
    println!("   - 会员中心: http://localhost:{port}/member");
    println!("=======================================================\n");

    axum::serve(listener, app.into_make_service()).await?;
    Ok(())
}
