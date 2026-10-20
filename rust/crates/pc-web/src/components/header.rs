use leptos::prelude::*;

#[component]
pub fn TopBar() -> impl IntoView {
    view! {
        <div class="top-bar">
            <div class="container flex justify-between align-center" style="height: 32px; line-height: 32px; font-size: 12px; color: #666; margin: 0 auto; max-width: 1200px;">
                <div class="flex align-center">
                    <span style="margin-right: 15px;">"欢迎光临 TigShop 商城！"</span>
                    <a href="/login" class="top-bar-common-fontcolor" style="color: #ff3838; margin-right: 10px;">"你好，请登录"</a>
                    <span style="color: #ccc; margin-right: 10px;">"|"</span>
                    <a href="/login" style="color: #666;">"免费注册"</a>
                </div>
                <div class="flex align-center" style="gap: 15px;">
                    <a href="/order/list" style="color: #666;">"我的订单"</a>
                    <span style="color: #ccc;">"|"</span>
                    <a href="/member" style="color: #666;">"个人中心"</a>
                    <span style="color: #ccc;">"|"</span>
                    <a href="/cart" style="color: #666;">"购物车"</a>
                    <span style="color: #ccc;">"|"</span>
                    <a href="/wholesale" style="color: #666;">"企业采购"</a>
                    <span style="color: #ccc;">"|"</span>
                    <span style="color: #666; cursor: pointer;">"客户服务"</span>
                </div>
            </div>
        </div>
    }
}

#[component]
pub fn PageHeader() -> impl IntoView {
    view! {
        <div class="header" style="background: #fff; padding: 20px 0; border-bottom: 1px solid #f0f0f0;">
            <div class="container flex justify-between align-center" style="margin: 0 auto; max-width: 1200px;">
                // Logo
                <div class="logo">
                    <a href="/">
                        <img src="/static/images/logo_blue.png" alt="TigShop" style="height: 48px; object-fit: contain;"/>
                    </a>
                </div>

                // 搜索框区域
                <div class="search-area" style="flex: 0 1 560px;">
                    <form action="/search" method="get" class="flex" style="border: 2px solid #ff3838; border-radius: 4px; overflow: hidden;">
                        <input
                            type="text"
                            name="keyword"
                            placeholder="请输入搜索商品关键字、品牌或类目..."
                            style="flex: 1; border: none; padding: 10px 15px; font-size: 14px; outline: none;"
                        />
                        <button
                            type="submit"
                            style="background: #ff3838; color: #fff; border: none; padding: 0 25px; font-size: 15px; cursor: pointer; font-weight: bold;"
                        >
                            "搜索"
                        </button>
                    </form>
                    // 热门搜索词
                    <div class="hot-keywords flex" style="margin-top: 6px; font-size: 12px; color: #999; gap: 12px;">
                        <span>"热门搜索："</span>
                        <a href="/search?keyword=手机" style="color: #ff3838;">"5G手机"</a>
                        <a href="/search?keyword=笔记本" style="color: #666;">"轻薄笔记本"</a>
                        <a href="/search?keyword=耳机" style="color: #666;">"降噪耳机"</a>
                        <a href="/search?keyword=坚果" style="color: #666;">"每日坚果"</a>
                        <a href="/search?keyword=家电" style="color: #666;">"智能家电"</a>
                    </div>
                </div>

                // 购物车快捷浮动块
                <div class="cart-btn">
                    <a
                        href="/cart"
                        class="flex align-center justify-center"
                        style="border: 1px solid #e5e5e5; padding: 8px 18px; border-radius: 4px; color: #333; text-decoration: none; font-size: 13px; background: #fafafa; gap: 8px;"
                    >
                        <img src="/static/icons/cart.svg" alt="购物车" style="width: 18px; height: 18px;"/>
                        <span>"我的购物车"</span>
                        <span style="background: #ff3838; color: #fff; font-size: 11px; padding: 1px 6px; border-radius: 10px; font-weight: bold;">"0"</span>
                    </a>
                </div>
            </div>
        </div>
    }
}

#[component]
pub fn NavBar() -> impl IntoView {
    view! {
        <nav class="nav-bar" style="background: #fff; border-bottom: 2px solid #ff3838;">
            <div class="container flex align-center" style="margin: 0 auto; max-width: 1200px;">
                // 全部分类标题
                <div
                    class="category-btn flex align-center justify-between"
                    style="width: 200px; background: #ff3838; color: #fff; padding: 0 15px; height: 42px; font-size: 14px; font-weight: bold; cursor: pointer; box-sizing: border-box;"
                >
                    <span>"全部商品分类"</span>
                    <span style="font-size: 12px;">"▼"</span>
                </div>

                // 快捷导航链接
                <div class="nav-links flex align-center" style="margin-left: 20px; gap: 30px; font-size: 15px; font-weight: bold;">
                    <a href="/" style="color: #ff3838; text-decoration: none; padding: 10px 0;">"首页"</a>
                    <a href="/seckill" style="color: #333; text-decoration: none; padding: 10px 0;">"秒杀专区"</a>
                    <a href="/coupon" style="color: #333; text-decoration: none; padding: 10px 0;">"领券中心"</a>
                    <a href="/wholesale" style="color: #333; text-decoration: none; padding: 10px 0;">"企业批发"</a>
                    <a href="/exchange" style="color: #333; text-decoration: none; padding: 10px 0;">"积分商城"</a>
                </div>
            </div>
        </nav>
    }
}

#[component]
pub fn Header() -> impl IntoView {
    view! {
        <header class="mall-header">
            <TopBar/>
            <PageHeader/>
            <NavBar/>
        </header>
    }
}
