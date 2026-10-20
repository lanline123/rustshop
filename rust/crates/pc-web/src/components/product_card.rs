use leptos::prelude::*;

#[component]
pub fn ProductCard(
    #[prop(into)] id: i64,
    #[prop(into)] name: String,
    #[prop(into)] price: String,
    #[prop(into, default = String::new())] market_price: String,
    #[prop(into)] pic_thumb: String,
    #[prop(into, default = 0)] sales: i64,
    #[prop(into, default = Vec::new())] badges: Vec<String>,
) -> impl IntoView {
    let detail_url = format!("/product/{}", id);
    let img_src = if pic_thumb.is_empty() {
        "/static/images/goods_empty.png".to_string()
    } else {
        pic_thumb
    };

    view! {
        <div
            class="goods-card"
            style="background: #fff; border: 1px solid #eee; border-radius: 6px; padding: 12px; transition: all 0.25s ease; position: relative; display: flex; flex-direction: column; overflow: hidden;"
            onmouseover="this.style.borderColor='#ff3838'; this.style.boxShadow='0 4px 16px rgba(0,0,0,0.08)';"
            onmouseout="this.style.borderColor='#eee'; this.style.boxShadow='none';"
        >
            // 商品主图
            <a href=detail_url.clone() class="pic-wrap flex justify-center align-center" style="display: block; width: 100%; height: 210px; overflow: hidden; background: #fafafa; border-radius: 4px;">
                <img
                    src=img_src
                    alt=name.clone()
                    style="max-width: 100%; max-height: 100%; object-fit: contain; transition: transform 0.3s;"
                    onmouseover="this.style.transform='scale(1.05)';"
                    onmouseout="this.style.transform='scale(1)';"
                />
            </a>

            // 价格与原价
            <div class="price-row flex align-baseline" style="margin-top: 12px; gap: 8px;">
                <span class="price-symbol" style="color: #ff3838; font-size: 14px; font-weight: bold;">"¥"</span>
                <span class="price-val" style="color: #ff3838; font-size: 20px; font-weight: bold; line-height: 1;">{price}</span>
                {
                    if !market_price.is_empty() {
                        view! {
                            <span class="market-price" style="color: #bbb; font-size: 12px; text-decoration: line-through;">
                                "¥"{market_price}
                            </span>
                        }.into_any()
                    } else {
                        view! {}.into_any()
                    }
                }
            </div>

            // 标签列表
            {
                if !badges.is_empty() {
                    view! {
                        <div class="tags flex" style="margin-top: 6px; gap: 4px; flex-wrap: wrap;">
                            {
                                badges.into_iter().map(|tag| {
                                    view! {
                                        <span style="background: #fff0f0; color: #ff3838; font-size: 11px; padding: 1px 5px; border-radius: 2px; border: 1px solid #ffd4d4;">
                                            {tag}
                                        </span>
                                    }
                                }).collect::<Vec<_>>()
                            }
                        </div>
                    }.into_any()
                } else {
                    view! {}.into_any()
                }
            }

            // 商品标题
            <a
                href=detail_url.clone()
                class="goods-title"
                style="margin-top: 8px; font-size: 13px; color: #333; line-height: 1.5; height: 38px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; text-decoration: none;"
                title=name.clone()
            >
                {name.clone()}
            </a>

            // 底部销量与快捷入口
            <div class="bottom-info flex justify-between align-center" style="margin-top: 10px; font-size: 12px; color: #999;">
                <span>"已售 "{sales}" 件"</span>
                <a
                    href=detail_url
                    style="background: #fff0f0; color: #ff3838; border: 1px solid #ff3838; padding: 2px 8px; border-radius: 3px; text-decoration: none; font-size: 11px;"
                >
                    "查看详情"
                </a>
            </div>
        </div>
    }
}
