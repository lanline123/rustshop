use leptos::prelude::*;
use crate::components::product_card::ProductCard;

#[component]
pub fn SearchPage() -> impl IntoView {
    let search_results = vec![
        (1001, "Apple iPhone 16 Pro Max 256GB 原色钛金属 5G全网通手机", "8999.00", "9999.00", "/static/images/goods_empty.png", 2850, vec!["自营", "特惠", "免息"]),
        (1003, "华为 Mate 60 Pro 12GB+512GB 雅丹黑 鸿蒙旗舰 卫星通话", "6999.00", "6999.00", "/static/images/goods_empty.png", 5630, vec!["自营", "热销手机"]),
        (1005, "Sony WH-1000XM5 头戴式无线蓝牙降噪耳机 黑色", "2299.00", "2799.00", "/static/images/goods_empty.png", 1890, vec!["热卖榜", "Hi-Res"]),
        (1009, "小米 14 Ultra 徕卡光学全焦段四摄 骁龙8Gen3 影像旗舰手机 16GB+512GB", "6499.00", "6999.00", "/static/images/goods_empty.png", 3210, vec!["自营", "影像标杆"]),
        (1010, "Apple iPad Air 11英寸 M2芯片 128GB WLAN版 紫色平板电脑", "4799.00", "4799.00", "/static/images/goods_empty.png", 4120, vec!["自营", "限时特惠"]),
        (1011, "ROG 魔霸新锐 2026 16英寸游戏笔记本 (i9-13980HX 16G 1TB RTX4060)", "8999.00", "9999.00", "/static/images/goods_empty.png", 890, vec!["电竞神机"]),
        (1002, "联想 ThinkPad X1 Carbon 14英寸商务笔记本电脑", "11999.00", "13999.00", "/static/images/goods_empty.png", 1420, vec!["企业采购"]),
        (1008, "科沃斯 (ECOVACS) 地宝 T30 PRO 扫拖一体机 全自动基站", "3299.00", "3999.00", "/static/images/goods_empty.png", 870, vec!["智能家电"]),
    ];

    view! {
        <div class="search-page" style="background: #f2f3f5; min-height: 80vh; padding: 20px 0 50px;">
            <div class="container">
                // 面包屑
                <div class="breadcrumb acea-row row-middle" style="font-size: 13px; color: #666; margin-bottom: 15px; gap: 8px;">
                    <a href="/" style="color: #666;">"首页"</a>
                    <span>">"</span>
                    <span style="color: #333; font-weight: bold;">"全部搜索结果"</span>
                    <span style="color: #999;">"(共 8 件相关商品)"</span>
                </div>

                // 筛选面板
                <div class="filter-panel" style="background: #fff; border: 1px solid #e5e5e5; border-radius: 4px; padding: 15px 20px; margin-bottom: 20px;">
                    <div style="display: flex; align-items: center; padding: 8px 0; border-bottom: 1px dashed #eee;">
                        <span style="width: 80px; font-weight: bold; color: #666; font-size: 13px;">"品牌："</span>
                        <div style="display: flex; gap: 16px; flex-wrap: wrap; font-size: 13px; color: #333;">
                            <a href="#" style="color: var(--general); font-weight: bold;">"全部"</a>
                            <a href="#">"Apple (苹果)"</a>
                            <a href="#">"HUAWEI (华为)"</a>
                            <a href="#">"Xiaomi (小米)"</a>
                            <a href="#">"Sony (索尼)"</a>
                            <a href="#">"ThinkPad (联想)"</a>
                            <a href="#">"ASUS (华硕)"</a>
                            <a href="#">"Dyson (戴森)"</a>
                        </div>
                    </div>
                    <div style="display: flex; align-items: center; padding: 8px 0;">
                        <span style="width: 80px; font-weight: bold; color: #666; font-size: 13px;">"价格区间："</span>
                        <div style="display: flex; gap: 16px; font-size: 13px; color: #333;">
                            <a href="#" style="color: var(--general); font-weight: bold;">"全部"</a>
                            <a href="#">"0-500元"</a>
                            <a href="#">"500-2000元"</a>
                            <a href="#">"2000-5000元"</a>
                            <a href="#">"5000-10000元"</a>
                            <a href="#">"10000元以上"</a>
                        </div>
                    </div>
                </div>

                // 排序工具条 (sort-filter-wrap)
                <div class="sort-filter-wrap" style="background: #fff; border: 1px solid #e5e5e5; border-radius: 4px; padding: 8px 15px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between;">
                    <div class="sort-filter-left" style="display: flex; gap: 4px;">
                        <button class="sort-item active" style="border: 1px solid var(--general); background: var(--general); color: #fff; padding: 6px 16px; border-radius: 3px; font-size: 13px; cursor: pointer; font-weight: 500;">
                            "综合排序"
                        </button>
                        <button class="sort-item" style="border: 1px solid #ddd; background: #fff; color: #333; padding: 6px 16px; border-radius: 3px; font-size: 13px; cursor: pointer;">
                            "销量优先"
                        </button>
                        <button class="sort-item" style="border: 1px solid #ddd; background: #fff; color: #333; padding: 6px 16px; border-radius: 3px; font-size: 13px; cursor: pointer;">
                            "价格从低到高"
                        </button>
                        <button class="sort-item" style="border: 1px solid #ddd; background: #fff; color: #333; padding: 6px 16px; border-radius: 3px; font-size: 13px; cursor: pointer;">
                            "最新上架"
                        </button>
                    </div>
                    <div style="font-size: 12px; color: #999;">
                        "显示 1-8 件"
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
                <div class="pagination acea-row row-center-wrapper" style="margin-top: 35px; gap: 8px;">
                    <button style="border: 1px solid #ddd; background: #fff; padding: 6px 14px; border-radius: 4px; cursor: pointer; color: #999;">"上一页"</button>
                    <button style="border: 1px solid var(--general); background: var(--general); color: #fff; padding: 6px 14px; border-radius: 4px; cursor: pointer; font-weight: bold;">"1"</button>
                    <button style="border: 1px solid #ddd; background: #fff; padding: 6px 14px; border-radius: 4px; cursor: pointer; color: #666;">"2"</button>
                    <button style="border: 1px solid #ddd; background: #fff; padding: 6px 14px; border-radius: 4px; cursor: pointer; color: #666;">"3"</button>
                    <button style="border: 1px solid #ddd; background: #fff; padding: 6px 14px; border-radius: 4px; cursor: pointer; color: #666;">"下一页"</button>
                </div>
            </div>
        </div>
    }
}
