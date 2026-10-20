use leptos::prelude::*;

#[component]
pub fn TopBar() -> impl IntoView {
    view! {
        <div class="quick-top-bar" style="background: #f5f5f5; border-bottom: 1px solid #e5e5e5; height: 30px; line-height: 30px; font-size: 12px; color: #666; width: 100%; box-sizing: border-box; position: relative; z-index: 1000;">
            <div class="container" style="width: 1190px; max-width: 1190px; min-width: 1190px; margin: 0 auto; display: flex; align-items: center; justify-content: space-between; height: 30px; box-sizing: border-box;">
                // 左侧: 配送区域选择 (送至: 北京 朝阳区)
                <div class="region-select" style="display: flex; align-items: center; cursor: pointer; height: 30px;">
                    <span class="iconfont-pc icon-dizhi" style="font-size: 14px; color: var(--general); margin-right: 4px;"></span>
                    <span style="color: #666;">"送至："</span>
                    <span style="color: #333; font-weight: 500; margin-left: 2px;">"北京 朝阳区"</span>
                    <span class="iconfont-pc icon-xiajiantou" style="font-size: 9px; color: #999; margin-left: 4px;"></span>
                </div>

                // 右侧: 快捷导航与用户通道
                <div class="quick-nav-right" style="display: flex; align-items: center; height: 30px;">
                    // 登录 / 注册
                    <div style="display: flex; align-items: center; padding: 0 10px;">
                        <a href="/login" class="top-login-link" style="color: var(--general); font-weight: 500; text-decoration: none;">"你好，请登录"</a>
                        <span style="width: 1px; height: 12px; background: #ddd; margin: 0 8px; display: inline-block;"></span>
                        <a href="/login" class="top-register-link" style="color: #999; text-decoration: none;">"免费注册"</a>
                    </div>

                    <span style="width: 1px; height: 12px; background: #e0e0e0; display: inline-block;"></span>

                    // 站内消息
                    <div style="display: flex; align-items: center; padding: 0 10px;">
                        <a href="/member" class="top-link" style="color: #666; text-decoration: none; display: flex; align-items: center; gap: 4px;">
                            <span class="iconfont-pc icon-xiaoxi" style="font-size: 13px; color: #999;"></span>
                            <span>"站内消息"</span>
                        </a>
                    </div>

                    <span style="width: 1px; height: 12px; background: #e0e0e0; display: inline-block;"></span>

                    // 我的订单
                    <div style="display: flex; align-items: center; padding: 0 10px;">
                        <a href="/order/list" class="top-link" style="color: #666; text-decoration: none; display: flex; align-items: center; gap: 4px;">
                            <span class="iconfont-pc icon-dingdan" style="font-size: 13px; color: #999;"></span>
                            <span>"我的订单"</span>
                        </a>
                    </div>

                    <span style="width: 1px; height: 12px; background: #e0e0e0; display: inline-block;"></span>

                    // 客户服务
                    <div style="display: flex; align-items: center; padding: 0 10px; cursor: pointer;">
                        <a href="#" class="top-link" style="color: #666; text-decoration: none; display: flex; align-items: center; gap: 4px;">
                            <span>"客户服务"</span>
                            <span class="iconfont-pc icon-xiajiantou" style="font-size: 9px; color: #999;"></span>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    }
}

#[component]
pub fn PageHeader() -> impl IntoView {
    let site_config = use_context::<crate::loader::SiteConfig>().unwrap_or_default();
    let logo_url = site_config.logo.clone();
    let navs = site_config.navigations.clone();

    view! {
        <div class="header" style="border-bottom: 1px solid #ddd;">
            <div class="top-area container">
                <div class="wrapper" style="display: flex; align-items: center; justify-content: space-between; padding: 15px 0;">
                    // Logo
                    <div class="logo">
                        <a href="/" class="alv1">
                            <img src=logo_url alt="TigShop" style="width: 190px; height: 55px; object-fit: contain;"/>
                        </a>
                    </div>

                    // 搜索区域
                    <div class="top-search" style="max-width: 620px; flex: 1; margin: 0 40px;">
                        <form action="/search" method="get" class="search-form" style="display: flex; height: 38px; border: 2px solid var(--general); box-sizing: border-box;">
                            <input
                                type="text"
                                name="keyword"
                                autocomplete="off"
                                placeholder="搜索商品、品牌或分类"
                                class="search-input"
                                style="flex: 1; border: none; padding: 0 14px; font-size: 13px; outline: none; background: #fff;"
                            />
                            <button
                                type="submit"
                                class="search-submit iconfont-pc icon-sousuo"
                                style="width: 72px; height: 100%; background: var(--general); color: #fff; border: none; cursor: pointer; font-size: 18px; display: flex; align-items: center; justify-content: center;"
                            ></button>
                        </form>
                        <div class="hot-keywords acea-row" style="margin-top: 6px; font-size: 12px; color: #999; gap: 14px;">
                            <span style="color: #666;">"热门："</span>
                            <a href="/search?keyword=iPhone" style="color: var(--general);">"iPhone 16"</a>
                            <a href="/search?keyword=华为" style="color: #999;">"华为 Mate 60"</a>
                            <a href="/search?keyword=戴森" style="color: #999;">"戴森 V12"</a>
                            <a href="/search?keyword=索尼" style="color: #999;">"索尼耳机"</a>
                            <a href="/search?keyword=坚果" style="color: #999;">"每日坚果"</a>
                        </div>
                    </div>

                    // 我的购物车
                    <div class="cart" style="width: 140px; height: 38px; border: 1px solid #ddd; background: #fff; position: relative;">
                        <div class="cart_info" style="height: 100%;">
                            <a href="/cart" class="acea-row row-center-wrapper" style="display: flex; align-items: center; justify-content: center; height: 100%; color: #666; font-size: 13px; text-decoration: none;">
                                <span class="iconfont-pc icon-shiwu-gouwuche" style="font-size: 18px; color: var(--general); margin-right: 6px;"></span>
                                <span class="my-cart">"我的购物车"</span>
                                <em style="position: absolute; right: -5px; top: -5px; background: var(--general); color: #fff; border-radius: 10px; padding: 0 6px; font-size: 11px; height: 18px; line-height: 18px; font-style: normal; font-weight: bold;">"0"</em>
                            </a>
                        </div>
                    </div>
                </div>

                // 全局导航条 (DefNav - 依据后台装修 pc_navigation 实时渲染)
                <div class="top-nav top-nav-index" style="position: relative; height: 38px;">
                    <div class="nav-bd container flex align-end" style="display: flex; align-items: flex-end; height: 100%;">
                        <div class="main-nav-link" style="width: 190px; height: 38px; line-height: 38px; background: var(--main-bg); color: #fff; font-size: 14px; font-weight: 700; cursor: pointer; text-align: center;">
                            <div class="acea-row row-center-wrapper" style="display: flex; align-items: center; justify-content: center; height: 100%;">
                                <div>"全部商品分类"</div>
                                <div class="iconfont-pc icon-xiajiantou" style="font-size: 12px; margin-left: 10px;"></div>
                            </div>
                        </div>
                        <ul class="sub-nav" style="display: flex; list-style: none; margin: 0; padding: 0; height: 38px; align-items: center;">
                            {
                                navs.into_iter().enumerate().map(|(idx, n)| {
                                    let color = if idx == 0 { "var(--general)" } else { "#333" };
                                    let fw = if idx == 0 { "bold" } else { "normal" };
                                    view! {
                                        <li>
                                            <a href=n.link class="alv1" style=format!("font-size: 15px; padding: 0 16px; color: {}; font-weight: {}; height: 38px; line-height: 38px; display: block;", color, fw)>
                                                {n.title}
                                            </a>
                                        </li>
                                    }
                                }).collect::<Vec<_>>()
                            }
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    }
}

#[component]
pub fn Header() -> impl IntoView {
    view! {
        <header>
            <TopBar/>
            <PageHeader/>
        </header>
    }
}
