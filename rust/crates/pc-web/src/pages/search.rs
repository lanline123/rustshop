use leptos::prelude::*;
use crate::components::product_card::ProductCard;

#[component]
pub fn SearchPage() -> impl IntoView {
    // 模拟搜索结果
    let search_results = vec![
        (1001, "Apple iPhone 16 Pro Max 256GB 原色钛金属 5G全网通手机", "8999.00", "9999.00", "/static/images/goods_empty.png", 2850, vec!["自营", "热卖", "分期免息"]),
        (1003, "华为 Mate 60 Pro 12GB+512GB 雅川青 卫星通话 超可靠玄武架构", "6999.00", "6999.00", "/static/images/goods_empty.png", 5630, vec!["自营", "现货即发"]),
        (1005, "Sony WH-1000XM5 无线头戴式主动降噪蓝牙耳机 铂金银", "2299.00", "2799.00", "/static/images/goods_empty.png", 1890, vec!["秒杀", "Hi-Res"]),
        (1009, "小米 14 Ultra 徕卡光学全焦段四摄 骁龙8Gen3 摄影旗舰手机 16GB+512GB", "6499.00", "6999.00", "/static/images/goods_empty.png", 3210, vec!["自营", "赠摄影手柄"]),
        (1010, "Apple iPad Air 11英寸 M2芯片 128GB WLAN版 深空灰色", "4799.00", "4799.00", "/static/images/goods_empty.png", 4120, vec!["自营", "教育特惠"]),
        (1011, "ROG 魔霸新锐 2026 16英寸游戏笔记本电脑 (i9-13980HX 16G 1TB RTX4060)", "8999.00", "9999.00", "/static/images/goods_empty.png", 890, vec!["电竞热卖"]),
    ];

    view! {
        <div class="search-page" style="background: #f6f6f6; padding: 20px 0 40px 0;">
            <div class="container" style="max-width: 1200px; margin: 0 auto;">
                // 面包屑导航
                <div class="breadcrumb" style="font-size: 13px; color: #666; margin-bottom: 15px;">
                    <a href="/" style="color: #666; text-decoration: none;">"首页"</a>
                    <span style="margin: 0 8px;">">"</span>
                    <span style="color: #333; font-weight: bold;">"全部搜索结果"</span>
                </div>

                // 筛选条件框
                <div class="filter-box" style="background: #fff; border: 1px solid #e8e8e8; border-radius: 6px; padding: 15px 20px; margin-bottom: 20px; font-size: 13px;">
                    // 品牌筛选
                    <div class="filter-row flex align-center" style="padding: 10px 0; border-bottom: 1px dashed #eee;">
                        <span style="width: 80px; color: #999;">"品牌："</span>
                        <div class="flex" style="gap: 18px; flex-wrap: wrap;">
                            <a href="#" style="color: #ff3838; font-weight: bold; text-decoration: none;">"全部"</a>
                            <a href="#" style="color: #444; text-decoration: none;">"Apple (苹果)"</a>
                            <a href="#" style="color: #444; text-decoration: none;">"华为 (HUAWEI)"</a>
                            <a href="#" style="color: #444; text-decoration: none;">"小米 (XIAOMI)"</a>
                            <a href="#" style="color: #444; text-decoration: none;">"索尼 (SONY)"</a>
                            <a href="#" style="color: #444; text-decoration: none;">"联想 (Lenovo)"</a>
                        </div>
                    </div>
                    // 价格区间
                    <div class="filter-row flex align-center" style="padding: 10px 0;">
                        <span style="width: 80px; color: #999;">"价格区间："</span>
                        <div class="flex align-center" style="gap: 10px;">
                            <input type="text" placeholder="¥ 最低价" style="width: 80px; padding: 4px 8px; border: 1px solid #ddd; border-radius: 3px; font-size: 12px;"/>
                            <span>"-"</span>
                            <input type="text" placeholder="¥ 最高价" style="width: 80px; padding: 4px 8px; border: 1px solid #ddd; border-radius: 3px; font-size: 12px;"/>
                            <button style="background: #ff3838; color: #fff; border: none; padding: 4px 12px; border-radius: 3px; cursor: pointer; font-size: 12px;">"确定"</button>
                        </div>
                    </div>
                </div>

                // 排序工具条
                <div class="sort-bar flex justify-between align-center" style="background: #fff; border: 1px solid #e8e8e8; border-radius: 6px; padding: 10px 20px; margin-bottom: 20px; font-size: 13px;">
                    <div class="flex align-center" style="gap: 20px;">
                        <span style="color: #999;">"排序："</span>
                        <a href="#" style="color: #ff3838; font-weight: bold; text-decoration: none;">"综合"</a>
                        <a href="#" style="color: #444; text-decoration: none;">"销量 ▼"</a>
                        <a href="#" style="color: #444; text-decoration: none;">"新品"</a>
                        <a href="#" style="color: #444; text-decoration: none;">"价格 ▲"</a>
                    </div>
                    <div style="color: #888; font-size: 12px;">
                        "共找到 "{search_results.len()}" 件相关商品"
                    </div>
                </div>

                // 商品列表网格
                <div class="grid" style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px;">
                    {
                        search_results.into_iter().map(|(id, name, price, market, pic, sales, item_tags)| {
                            view! {
                                <ProductCard
                                    id=id
                                    name=name.to_string()
                                    price=price.to_string()
                                    market_price=market.to_string()
                                    pic_thumb=pic.to_string()
                                    sales=sales
                                    badges={item_tags.into_iter().map(|t| t.to_string()).collect::<Vec<_>>()}
                                />
                            }
                        }).collect::<Vec<_>>()
                    }
                </div>

                // 分页器
                <div class="pagination flex justify-center align-center" style="margin-top: 35px; gap: 8px;">
                    <button style="border: 1px solid #ddd; background: #fff; padding: 6px 14px; border-radius: 4px; cursor: pointer; color: #999;">"上一页"</button>
                    <button style="border: 1px solid #ff3838; background: #ff3838; color: #fff; padding: 6px 14px; border-radius: 4px; cursor: pointer; font-weight: bold;">"1"</button>
                    <button style="border: 1px solid #ddd; background: #fff; padding: 6px 14px; border-radius: 4px; cursor: pointer; color: #666;">"2"</button>
                    <button style="border: 1px solid #ddd; background: #fff; padding: 6px 14px; border-radius: 4px; cursor: pointer; color: #666;">"3"</button>
                    <button style="border: 1px solid #ddd; background: #fff; padding: 6px 14px; border-radius: 4px; cursor: pointer; color: #666;">"下一页"</button>
                </div>
            </div>
        </div>
    }
}
