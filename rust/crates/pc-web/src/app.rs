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
                // 引入 TigShop 官方 3 套阿里云矢量图标库
                <link rel="stylesheet" href="https://at.alicdn.com/t/c/font_4441878_nmmffdd6bwr.css"/>
                <link rel="stylesheet" href="https://at.alicdn.com/t/c/font_400590_7he137t8862.css"/>
                <link rel="stylesheet" href="https://at.alicdn.com/t/c/font_4389188_4f7gj6caz7w.css"/>
                // 引入解构后的全量 CSS 样式库
                <link rel="stylesheet" href="/static/css/mall.css"/>
                <style>
                    "
                    :root, body, .app {
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
                    }
                    body {
                        background-color: #f2f3f5;
                        color: #666;
                        font-size: 12px;
                        font-family: PingFang SC, Microsoft Yahei, Avenir, Segoe UI, sans-serif;
                        margin: 0;
                        padding: 0;
                        -webkit-font-smoothing: antialiased;
                    }
                    .container {
                        width: 100%;
                        max-width: 1190px;
                        min-width: 1190px;
                        margin: 0 auto;
                        box-sizing: border-box;
                        clear: both;
                    }
                    a {
                        text-decoration: none;
                        color: inherit;
                    }
                    "
                </style>
            </head>
            <body>
                <div class="app">
                    <Router>
                        <Header/>
                        <main>
                            <Routes fallback=|| view! {
                                <div class="not-found" style="text-align: center; padding: 100px 0; font-size: 16px; color: #666;">
                                    <h1 style="font-size: 72px; color: var(--general); margin: 0 0 10px 0;">"404"</h1>
                                    <p>"抱歉，您访问的页面不存在或已被移除"</p>
                                    <a href="/" style="color: var(--general); text-decoration: none; font-weight: bold; margin-top: 15px; display: inline-block;">"返回商城首页"</a>
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
                </div>
            </body>
        </html>
    }
}
