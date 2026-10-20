use leptos::prelude::*;

#[component]
pub fn ProductCard(
    id: i32,
    name: String,
    price: String,
    market_price: String,
    pic_thumb: String,
    sales: i32,
    #[prop(default = vec![])] badges: Vec<String>,
) -> impl IntoView {
    let pic = if pic_thumb.is_empty() {
        "/static/images/goods_empty.png".to_string()
    } else {
        pic_thumb
    };

    view! {
        <div class="product-card" style="box-sizing: border-box; background: transparent;">
            <div class="car-info product-card-border-2 box-radius" style="background: #fff; border-radius: 6px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.06); transition: all 0.25s ease;">
                // 商品主图
                <div class="item-photo" style="position: relative; width: 100%; height: 210px; background: #fafafa; display: flex; align-items: center; justify-content: center;">
                    <a href=format!("/product/{}", id) class="item-content" target="_blank" style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;">
                        <img
                            class="image-box image-box-radius"
                            src=pic
                            alt=name.clone()
                            style="max-width: 90%; max-height: 90%; object-fit: contain; transition: transform 0.3s ease;"
                        />
                    </a>
                    // 促销标签
                    {
                        if !badges.is_empty() {
                            view! {
                                <div style="position: absolute; left: 8px; top: 8px; display: flex; gap: 4px;">
                                    {
                                        badges.into_iter().map(|b| {
                                            view! {
                                                <span style="background: var(--tag-bg); color: var(--tag-text); font-size: 11px; padding: 2px 6px; border-radius: 2px;">
                                                    {b}
                                                </span>
                                            }
                                        }).collect::<Vec<_>>()
                                    }
                                </div>
                            }.into_any()
                        } else {
                            ().into_any()
                        }
                    }
                </div>

                // 商品详情
                <div class="item-info" style="padding: 12px 14px 16px;">
                    <div class="item-name" style="height: 40px; overflow: hidden; margin-bottom: 8px;">
                        <a
                            href=format!("/product/{}", id)
                            class="item-product-name"
                            style="font-size: 14px; line-height: 20px; color: #333; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; text-overflow: ellipsis;"
                        >
                            {name}
                        </a>
                    </div>
                    <div class="item-action" style="display: flex; align-items: baseline; justify-content: space-between;">
                        <div class="item-price" style="color: var(--price); font-size: 20px; font-weight: bold; font-family: Arial, sans-serif;">
                            <span style="font-size: 13px; font-weight: normal; margin-right: 2px;">"¥"</span>
                            {price}
                            <span style="font-size: 12px; color: #bbb; text-decoration: line-through; margin-left: 6px; font-weight: normal;">
                                "¥"{market_price}
                            </span>
                        </div>
                        <div class="item-sales" style="font-size: 12px; color: #999;">
                            "已售 "{sales}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    }
}
