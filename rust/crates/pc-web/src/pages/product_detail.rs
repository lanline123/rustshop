use leptos::prelude::*;
use crate::loader::DetailData;

#[component]
pub fn ProductDetailPage() -> impl IntoView {
    let detail_opt = use_context::<DetailData>();

    let name = detail_opt.as_ref().map(|d| d.name.clone()).unwrap_or_else(|| "商品不存在或已下架".to_string());
    let sn = detail_opt.as_ref().map(|d| d.sn.clone()).unwrap_or_default();
    let price = detail_opt.as_ref().map(|d| d.price.clone()).unwrap_or_else(|| "0.00".to_string());
    let market_price = detail_opt.as_ref().map(|d| d.market_price.clone()).unwrap_or_else(|| "0.00".to_string());
    let stock = detail_opt.as_ref().map(|d| d.stock).unwrap_or(0);
    let sales = detail_opt.as_ref().map(|d| d.sales).unwrap_or(0);
    let pic = detail_opt.as_ref().map(|d| d.pic_thumb.clone()).unwrap_or_else(|| "/static/images/goods_empty.png".to_string());
    let desc = detail_opt.as_ref().map(|d| d.desc.clone()).unwrap_or_default();
    let skus = detail_opt.as_ref().map(|d| d.skus.clone()).unwrap_or_default();

    view! {
        <div class="product-detail-page" style="background: #f2f3f5; min-height: 80vh; padding: 20px 0 60px;">
            <div class="container">
                // 面包屑
                <div class="breadcrumb acea-row row-middle" style="font-size: 13px; color: #666; margin-bottom: 16px; gap: 8px;">
                    <a href="/" style="color: #666;">"首页"</a>
                    <span>">"</span>
                    <a href="/search" style="color: #666;">"商品列表"</a>
                    <span>">"</span>
                    <span style="color: #333; font-weight: bold;">{name.clone()}</span>
                </div>

                // 商品主体卡片 (图集 + 信息)
                <div class="detail-main" style="background: #fff; border: 1px solid #e5e5e5; border-radius: 4px; padding: 30px; display: grid; grid-template-columns: 460px 1fr; gap: 40px; margin-bottom: 25px;">
                    // 左侧: 相册主图与缩略图
                    <div class="gallery">
                        <div class="main-image" style="width: 460px; height: 460px; border: 1px solid #f0f0f0; border-radius: 4px; display: flex; align-items: center; justify-content: center; background: #fafafa; overflow: hidden;">
                            <img src=pic.clone() alt=name.clone() style="max-width: 90%; max-height: 90%; object-fit: contain;"/>
                        </div>
                        <div class="thumbnails flex" style="margin-top: 15px; gap: 12px; display: flex;">
                            <div style="width: 70px; height: 70px; border: 2px solid var(--general); border-radius: 4px; padding: 2px; cursor: pointer; display: flex; align-items: center; justify-content: center; background: #fff;">
                                <img src=pic.clone() style="max-width: 100%; max-height: 100%; object-fit: contain;"/>
                            </div>
                        </div>
                    </div>

                    // 右侧: 商品信息与购买表单
                    <div class="info">
                        <h1 style="font-size: 22px; font-weight: bold; color: #222; margin: 0 0 10px 0; line-height: 30px;">
                            {name.clone()}
                        </h1>
                        <p style="font-size: 13px; color: var(--general); margin: 0 0 16px 0; line-height: 20px;">
                            {format!("【商品编号：{}】正品保障，支持7天无理由退换货，全国顺丰包邮！", sn)}
                        </p>

                        // 价格栏
                        <div style="background: #fafafa; border-radius: 4px; padding: 18px 20px; margin-bottom: 20px;">
                            <div style="display: flex; align-items: baseline; gap: 10px;">
                                <span style="font-size: 13px; color: #666;">"商城售价："</span>
                                <span style="font-size: 32px; font-weight: bold; color: var(--price); font-family: Arial;">
                                    <span style="font-size: 20px;">"¥"</span>{price}
                                </span>
                                <span style="font-size: 13px; color: #999; text-decoration: line-through; margin-left: 10px;">
                                    {format!("市场参考价：¥{}", market_price)}
                                </span>
                            </div>
                            <div style="margin-top: 10px; font-size: 12px; color: #666; display: flex; gap: 24px;">
                                <span>{format!("累计销量：{} 件", sales)}</span>
                                <span>{format!("库存现货：{} 件", stock)}</span>
                                <span>"服务承诺：正品保证 · 闪电发货 · 售后无忧"</span>
                            </div>
                        </div>

                        // 规格选择 (SKU)
                        {
                            if !skus.is_empty() {
                                view! {
                                    <div class="sku-box" style="margin-bottom: 20px;">
                                        <div style="font-size: 13px; color: #666; margin-bottom: 8px;">"选择规格："</div>
                                        <div style="display: flex; gap: 10px; flex-wrap: wrap;">
                                            {
                                                skus.into_iter().enumerate().map(|(idx, s)| {
                                                    let border_style = if idx == 0 { "2px solid var(--general)" } else { "1px solid #ddd" };
                                                    let color_style = if idx == 0 { "var(--general)" } else { "#333" };
                                                    view! {
                                                        <div style=format!("border: {}; color: {}; padding: 6px 14px; border-radius: 3px; font-size: 13px; cursor: pointer; background: #fff;", border_style, color_style)>
                                                            {format!("{} (¥{})", s.sku_value, s.sku_price)}
                                                        </div>
                                                    }
                                                }).collect::<Vec<_>>()
                                            }
                                        </div>
                                    </div>
                                }.into_any()
                            } else {
                                view! { <div></div> }.into_any()
                            }
                        }

                        // 数量选择
                        <div class="quantity-box" style="margin-bottom: 25px; display: flex; align-items: center; gap: 12px;">
                            <span style="font-size: 13px; color: #666;">"购买数量："</span>
                            <div style="display: flex; border: 1px solid #ddd; border-radius: 3px; width: 110px; height: 32px; line-height: 32px; text-align: center;">
                                <button style="width: 32px; border: none; background: #fafafa; cursor: pointer; font-size: 16px;">"-"</button>
                                <input type="text" value="1" style="width: 46px; border: none; text-align: center; border-left: 1px solid #ddd; border-right: 1px solid #ddd;"/>
                                <button style="width: 32px; border: none; background: #fafafa; cursor: pointer; font-size: 16px;">"+"</button>
                            </div>
                            <span style="font-size: 12px; color: #999;">{format!("(库存 {} 件)", stock)}</span>
                        </div>

                        // 购买操作按钮
                        <div class="actions" style="display: flex; gap: 16px;">
                            <button style="background: #ffeded; color: var(--general); border: 1px solid var(--general); padding: 12px 36px; border-radius: 4px; font-size: 16px; font-weight: bold; cursor: pointer;">
                                "立即购买"
                            </button>
                            <button style="background: var(--general); color: #fff; border: 1px solid var(--general); padding: 12px 36px; border-radius: 4px; font-size: 16px; font-weight: bold; cursor: pointer; display: flex; align-items: center; gap: 8px;">
                                <span class="iconfont-pc icon-gouwuche"></span>
                                "加入购物车"
                            </button>
                        </div>
                    </div>
                </div>

                // 下方详情介绍与规格参数
                <div class="detail-bottom" style="background: #fff; border: 1px solid #e5e5e5; border-radius: 4px; padding: 25px;">
                    <div style="border-bottom: 2px solid var(--general); display: inline-block; padding-bottom: 8px; font-size: 16px; font-weight: bold; color: var(--general); margin-bottom: 20px;">
                        "商品图文详情"
                    </div>
                    <div class="content" style="font-size: 14px; line-height: 28px; color: #444;">
                        {
                            if desc.is_empty() {
                                view! {
                                    <div style="text-align: center; padding: 40px; color: #999;">
                                        "暂无更多图文详情介绍"
                                    </div>
                                }.into_any()
                            } else {
                                view! {
                                    <div inner_html=desc.clone()></div>
                                }.into_any()
                            }
                        }
                    </div>
                </div>
            </div>
        </div>
    }
}
