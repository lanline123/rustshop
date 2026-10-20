use leptos::prelude::*;
use leptos_router::components::*;
use leptos_router::path;

use crate::components::header::Header;
use crate::components::footer::Footer;
use crate::pages::home::HomePage;
use crate::pages::search::SearchPage;
use crate::pages::product_detail::ProductDetailPage;
use crate::pages::cart::CartPage;
use crate::pages::checkout::CheckoutPage;
use crate::pages::login::LoginPage;
use crate::pages::member::MemberPage;

#[component]
pub fn App() -> impl IntoView {
    view! {
        <!DOCTYPE html>
        <html lang="zh-CN">
            <head>
                <meta charset="utf-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1"/>
                <title>"TigShop - 全栈 Rust 高性能商业商城系统"</title>
                <link rel="stylesheet" href="/static/css/mall.css"/>
            </head>
            <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';">
                <Router>
                    <Header/>
                    <main>
                        <Routes fallback=|| view! {
                            <div class="not-found" style="text-align: center; padding: 100px 0; font-size: 16px; color: #666;">
                                <h1 style="font-size: 72px; color: #ff3838; margin: 0 0 10px 0;">"404"</h1>
                                <p>"抱歉，您访问的页面不存在或已被移除"</p>
                                <a href="/" style="color: #ff3838; text-decoration: none; font-weight: bold; margin-top: 15px; display: inline-block;">"返回商城首页"</a>
                            </div>
                        }>
                            <Route path=path!("/") view=HomePage/>
                            <Route path=path!("/search") view=SearchPage/>
                            <Route path=path!("/product/:id") view=ProductDetailPage/>
                            <Route path=path!("/cart") view=CartPage/>
                            <Route path=path!("/checkout") view=CheckoutPage/>
                            <Route path=path!("/login") view=LoginPage/>
                            <Route path=path!("/member") view=MemberPage/>
                            <Route path=path!("/order/list") view=MemberPage/>
                        </Routes>
                    </main>
                    <Footer/>
                </Router>
            </body>
        </html>
    }
}
